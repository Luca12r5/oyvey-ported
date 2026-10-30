using System.Collections.ObjectModel;
using System.Windows.Input;
using System.Windows.Media;
using Duelcliping.App.Controls;
using Duelcliping.App.Services;
using Duelcliping.Core.Library;
using Duelcliping.Core.Performance;
using Duelcliping.Core.Recording;
using Duelcliping.Core.Settings;
using Duelcliping.Core.Storage;

namespace Duelcliping.App.ViewModels;

public sealed class DashboardViewModel : ObservableObject
{
    private readonly AppController _app;
    private readonly MainViewModel _main;
    private EngineStatus? _status;
    private PerformanceSnapshot? _perf;
    private StorageUsage? _storage;

    public DashboardViewModel(AppController app, MainViewModel main)
    {
        _app = app;
        _main = main;
        SaveCommand = new AsyncCommand(p => _app.SaveClipAsync(Convert.ToDouble(p, System.Globalization.CultureInfo.InvariantCulture)),
            _ => _app.Settings.Current.ReplayBufferEnabled);
        SaveCustomCommand = new AsyncCommand(() => _app.SaveClipAsync(_app.Settings.Current.CustomClipSeconds), () => _app.Settings.Current.ReplayBufferEnabled);
        ToggleRecordingCommand = new AsyncCommand(_app.ToggleManualRecordingAsync);
        OpenSettingsCommand = new RelayCommand(() => _main.Navigate(Page.Settings));
        OpenLibraryCommand = new RelayCommand(() => _main.Navigate(Page.Library));
        OpenClipCommand = new AsyncCommand(p => _main.OpenInEditorAsync(((ClipCardViewModel)p!).FilePath));
    }

    public ICommand SaveCommand { get; }
    /// <summary>Saves a clip of the selected clip length (the same as the clip hotkey).</summary>
    public ICommand SaveClipCommand => _saveClip ??= new AsyncCommand(() => _app.SaveClipAsync(S.ShortClipSeconds), () => S.ReplayBufferEnabled);
    private ICommand? _saveClip;

    /// <summary>How long a clip is when you press the clip hotkey.</summary>
    public int ClipLength
    {
        get => S.ShortClipSeconds;
        set
        {
            _app.Settings.Update(s => s.ShortClipSeconds = value);
            OnPropertiesChanged(nameof(Len15), nameof(Len30), nameof(Len60), nameof(Len120), nameof(Len300), nameof(ShortcutHint), nameof(ClipLengthHint));
        }
    }
    public bool ProfileMaxFps
    {
        get => S.Profile == RecordingProfile.MaxFps;
        set { if (value) { _app.Settings.Update(s => s.Profile = RecordingProfile.MaxFps); OnPropertiesChanged(nameof(ProfileMaxFps), nameof(ProfileCustom), nameof(ProfileHint)); } }
    }
    public bool ProfileCustom
    {
        get => S.Profile == RecordingProfile.Custom;
        set { if (value) { _app.Settings.Update(s => s.Profile = RecordingProfile.Custom); OnPropertiesChanged(nameof(ProfileMaxFps), nameof(ProfileCustom), nameof(ProfileHint)); } }
    }
    public string ProfileHint => S.Profile == RecordingProfile.MaxFps
        ? "Max FPS: 30 FPS clips, at most 1080p – the least load on your game."
        : "Custom: your resolution, frame rate and quality from Settings.";

    public bool Len15 { get => ClipLength == 15; set { if (value) ClipLength = 15; } }
    public bool Len30 { get => ClipLength == 30; set { if (value) ClipLength = 30; } }
    public bool Len60 { get => ClipLength == 60; set { if (value) ClipLength = 60; } }
    public bool Len120 { get => ClipLength == 120; set { if (value) ClipLength = 120; } }
    public bool Len300 { get => ClipLength == 300; set { if (value) ClipLength = 300; } }
    public string ClipLengthHint => $"Press {S.HotkeySaveShort} in any game or app – even fullscreen – to clip the last {FormatLength(S.ShortClipSeconds)}.";
    private static string FormatLength(int seconds) => seconds < 60 ? $"{seconds} seconds" : seconds % 60 == 0 ? $"{seconds / 60} minute{(seconds >= 120 ? "s" : "")}" : $"{seconds} seconds";
    public ICommand SaveCustomCommand { get; }
    public ICommand ToggleRecordingCommand { get; }
    public ICommand OpenSettingsCommand { get; }
    public ICommand OpenLibraryCommand { get; }
    public ICommand OpenClipCommand { get; }

    public ObservableCollection<ClipCardViewModel> RecentClips { get; } = new();

    private AppSettings S => _app.Settings.Current;

    public bool ReplayEnabled
    {
        get => S.ReplayBufferEnabled;
        set { _app.SetReplayEnabled(value); OnPropertyChanged(); }
    }

    public string CustomSaveLabel => $"{S.CustomClipSeconds} s";
    public string ShortcutHint => $"{S.HotkeySaveShort} saves {S.ShortClipSeconds} s · {S.HotkeySaveLong} saves {S.LongClipSeconds} s · {S.HotkeyToggleRecording} records";

    public string StateTitle => _status?.State switch
    {
        _ when _status?.IsManualRecording == true => "Recording",
        EngineState.Running => "Replay buffer active",
        EngineState.Starting => "Starting…",
        EngineState.Restarting => "Restarting capture…",
        EngineState.Faulted => "Recording problem",
        _ => _app.StartupError != null ? "Cannot record" : "Idle",
    };

    public string StateDetail
    {
        get
        {
            if (_status == null) return "";
            if (_status.Error != null) return _status.Error;
            if (_app.StartupError != null && _status.State == EngineState.Stopped) return _app.StartupError;
            if (_status.IsManualRecording) return $"{AppController.FormatDuration(_status.ManualRecordingSeconds)} recorded · press {S.HotkeyToggleRecording} to stop";
            if (_status.State == EngineState.Running)
                return $"Last {AppController.FormatDuration(_status.BufferedSeconds)} ready to save · up to {AppController.FormatDuration(S.ReplayBufferSeconds)} in memory";
            return S.ReplayBufferEnabled ? "" : "Replay buffer is off. Turn it on to save clips after the fact.";
        }
    }

    public Brush StateBrush => (Brush)System.Windows.Application.Current.FindResource(_status switch
    {
        { IsManualRecording: true } => "RecordingBrush",
        { State: EngineState.Running } => "SuccessBrush",
        { State: EngineState.Faulted } => "DangerBrush",
        { State: EngineState.Starting or EngineState.Restarting } => "WarningBrush",
        _ => "TextTertiaryBrush",
    });

    public string RailStateText => _status switch
    {
        { IsManualRecording: true } => "REC",
        { State: EngineState.Running } => "LIVE",
        { State: EngineState.Faulted } => "ERROR",
        { State: EngineState.Starting or EngineState.Restarting } => "…",
        _ => "OFF",
    };

    public string RecordButtonText => _status?.IsManualRecording == true ? "Stop" : "Record";
    public bool IsManualRecording => _status?.IsManualRecording == true;

    public string ModeText => (_status?.IsManualRecording, S.ReplayBufferEnabled) switch
    {
        (true, true) => "Replay + Recording",
        (true, false) => "Recording",
        (_, true) => "Replay buffer",
        _ => "Off",
    };

    public string ResolutionText => _status is { OutputWidth: > 0 } st ? $"{st.OutputWidth}×{st.OutputHeight}" : S.Resolution switch
    {
        CaptureResolution.P720 => "720p", CaptureResolution.P1080 => "1080p", CaptureResolution.P1440 => "1440p",
        CaptureResolution.P2160 => "4K", _ => "Native",
    };

    public string FpsText => _status is { State: EngineState.Running } st && st.CaptureFps > 0
        ? $"{S.FrameRate} FPS · live {st.CaptureFps:0}" : $"{S.FrameRate} FPS";

    public string EncoderText => _status?.EncoderName ?? _app.ActiveEncoder?.Encoder.DisplayName ?? "Detecting…";
    public string PipelineText => _status?.PipelineDescription ?? (_app.ActiveEncoder?.Notice ?? "");
    public string BitrateText => _status is { BitrateKbps: > 0 } st ? $"{st.BitrateKbps / 1000.0:0.#} Mbps" : "—";

    public string StorageText => _storage == null ? "—" : $"{Format.Bytes(_storage.ClipBytes)} of {S.MaxStorageGb:0} GB";
    public string StorageDetail => _storage == null ? "" : $"{_storage.ClipCount} clips · {Format.Bytes(_storage.DriveFreeBytes)} free on drive";
    public double StorageFraction => _storage?.QuotaFraction ?? 0;

    public string CpuText => _perf == null ? "—" : $"{_perf.AppCpuPercent:0.0}%";
    public string RamText => _perf == null ? "—" : Format.Bytes(_perf.AppPrivateBytes);
    public string GpuText => _perf?.AppGpuPercent is double g ? $"{g:0.0}%" : "n/a";
    public string BufferMemoryText => _status == null ? "—" : Format.Bytes(_status.BufferedBytes);
    public string DroppedText => _status == null ? "—" : $"{_status.DroppedFrames}";
    public string PerformanceModeText => !S.PerformanceMode ? "Off" :
        _app.GameDetector.IsGameActive ? $"Active · {_app.GameDetector.ActiveProcessName ?? "game"} in focus" : "On · waiting for a game";

    public void Refresh()
    {
        _status = _app.Engine.GetStatus();
        _perf = _app.Performance.Latest;
        OnPropertiesChanged(nameof(StateTitle), nameof(StateDetail), nameof(StateBrush), nameof(RecordButtonText), nameof(RailStateText), nameof(IsManualRecording),
            nameof(ModeText), nameof(ResolutionText), nameof(FpsText), nameof(EncoderText), nameof(PipelineText), nameof(BitrateText),
            nameof(CpuText), nameof(RamText), nameof(GpuText), nameof(BufferMemoryText), nameof(DroppedText), nameof(PerformanceModeText),
            nameof(ReplayEnabled), nameof(CustomSaveLabel), nameof(ShortcutHint), nameof(ClipLengthHint),
            nameof(Len15), nameof(Len30), nameof(Len60), nameof(Len120), nameof(Len300));
    }

    public async Task RefreshClipsAsync()
    {
        var clips = await _app.Library.ScanAsync();
        _storage = StorageManager.Measure(clips, _app.Library.Directory, S.MaxStorageGb);
        var recent = ClipLibrary.Sort(clips, ClipSort.Newest).Take(6).ToList();
        RecentClips.Clear();
        foreach (var clip in recent)
        {
            var card = new ClipCardViewModel(clip);
            card.EnsureThumbnail(_app.Library);
            RecentClips.Add(card);
        }
        OnPropertiesChanged(nameof(StorageText), nameof(StorageDetail), nameof(StorageFraction), nameof(HasClips));
    }

    public bool HasClips => RecentClips.Count > 0;
}
