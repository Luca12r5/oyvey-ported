using System.Runtime.CompilerServices;
using System.Windows.Input;
using Duelcliping.App.Services;
using Duelcliping.Core.Encoders;
using Duelcliping.Core.Recording;
using Duelcliping.Core.Settings;
using NAudio.CoreAudioApi;

namespace Duelcliping.App.ViewModels;

public sealed record Option<T>(T Value, string Label);

/// <summary>Every property writes straight through <see cref="SettingsManager.Update"/>, which persists atomically.</summary>
public sealed class SettingsViewModel : ObservableObject
{
    private readonly AppController _app;
    private HotkeyAction? _capturing;
    private string? _hotkeyMessage;

    public SettingsViewModel(AppController app)
    {
        _app = app;
        _app.Settings.Changed += (_, _) => System.Windows.Application.Current.Dispatcher.InvokeAsync(RefreshAll);
        BrowseFolderCommand = new RelayCommand(BrowseFolder);
        OpenFolderCommand = new RelayCommand(() => ShellHelpers.RevealInExplorer(S.ClipsDirectory));
        OpenLogsCommand = new RelayCommand(() => ShellHelpers.RevealInExplorer(Core.Logging.Log.Directory ?? _app.DataDirectory));
        RedetectCommand = new AsyncCommand(async () => { await _app.RedetectEncodersAsync(); RefreshAll(); });
        CaptureHotkeyCommand = new RelayCommand(p => { _app.Hotkeys.Suspend(); Capturing = (HotkeyAction)p!; HotkeyMessage = "Press the new shortcut… (Esc cancels, Backspace clears)"; });
        RefreshDevicesCommand = new RelayCommand(LoadDevices);
        BrowsePresentMonCommand = new RelayCommand(BrowsePresentMon);
        LoadDevices();
    }

    private AppSettings S => _app.Settings.Current;
    private void Update(Action<AppSettings> change, [CallerMemberName] string? name = null)
    {
        try { _app.Settings.Update(change); }
        catch (Exception ex) { _app.RaiseToast(new Toast(ToastKind.Error, "Settings not saved", ex.Message)); }
        OnPropertyChanged(name);
    }

    public ICommand BrowseFolderCommand { get; }
    public ICommand OpenFolderCommand { get; }
    public ICommand OpenLogsCommand { get; }
    public ICommand RedetectCommand { get; }
    public ICommand CaptureHotkeyCommand { get; }
    public ICommand RefreshDevicesCommand { get; }
    public ICommand BrowsePresentMonCommand { get; }

    // ------------------------------------------------------------- option lists
    public IReadOnlyList<Option<string>> Monitors => _app.Displays.Select(d => new Option<string>(d.DeviceName, d.Label))
        .Prepend(new Option<string>("", "Primary monitor")).ToList();
    public IReadOnlyList<Option<CaptureResolution>> Resolutions { get; } = new[]
    {
        new Option<CaptureResolution>(CaptureResolution.Native, "Native (monitor resolution)"),
        new Option<CaptureResolution>(CaptureResolution.P720, "720p"),
        new Option<CaptureResolution>(CaptureResolution.P1080, "1080p"),
        new Option<CaptureResolution>(CaptureResolution.P1440, "1440p"),
        new Option<CaptureResolution>(CaptureResolution.P2160, "4K (2160p)"),
    };
    public IReadOnlyList<Option<int>> FrameRates { get; } = AppSettings.SupportedFrameRates.Select(f => new Option<int>(f, $"{f} FPS")).ToList();
    public IReadOnlyList<Option<VideoCodec>> Codecs => new[]
    {
        new Option<VideoCodec>(VideoCodec.H264, "H.264 (most compatible)"),
        new Option<VideoCodec>(VideoCodec.Hevc, "H.265 / HEVC" + (Has(VideoCodec.Hevc) ? "" : " – not supported by your GPU")),
        new Option<VideoCodec>(VideoCodec.Av1, "AV1" + (Has(VideoCodec.Av1) ? "" : " – not supported by your GPU")),
    };
    private bool Has(VideoCodec codec) => _app.Encoders.Available.Any(e => e.Codec == codec && e.IsHardware);
    public IReadOnlyList<Option<ContainerFormat>> Containers { get; } = new[]
    {
        new Option<ContainerFormat>(ContainerFormat.Mp4, "MP4"),
        new Option<ContainerFormat>(ContainerFormat.Mkv, "MKV"),
    };
    public IReadOnlyList<Option<QualityPreset>> Qualities { get; } = new[]
    {
        new Option<QualityPreset>(QualityPreset.Low, "Low"),
        new Option<QualityPreset>(QualityPreset.Medium, "Medium"),
        new Option<QualityPreset>(QualityPreset.High, "High"),
        new Option<QualityPreset>(QualityPreset.Ultra, "Ultra"),
        new Option<QualityPreset>(QualityPreset.Custom, "Custom bitrate"),
    };
    public IReadOnlyList<Option<EncoderPreference>> EncoderChoices => new[]
    {
        new Option<EncoderPreference>(EncoderPreference.Auto, "Automatic (best hardware encoder)"),
        new Option<EncoderPreference>(EncoderPreference.Nvidia, "NVIDIA NVENC" + Avail(EncoderVendor.Nvidia)),
        new Option<EncoderPreference>(EncoderPreference.Amd, "AMD AMF" + Avail(EncoderVendor.Amd)),
        new Option<EncoderPreference>(EncoderPreference.Intel, "Intel Quick Sync" + Avail(EncoderVendor.Intel)),
        new Option<EncoderPreference>(EncoderPreference.Software, "CPU (x264) – lowers game FPS"),
    };
    private string Avail(EncoderVendor v) => _app.Encoders.Available.Any(e => e.Vendor == v) ? "" : " – not found";
    public IReadOnlyList<Option<OverlayCorner>> OverlayCorners { get; } = new[]
    {
        new Option<OverlayCorner>(OverlayCorner.TopLeft, "Top left"),
        new Option<OverlayCorner>(OverlayCorner.TopRight, "Top right"),
        new Option<OverlayCorner>(OverlayCorner.BottomLeft, "Bottom left"),
        new Option<OverlayCorner>(OverlayCorner.BottomRight, "Bottom right"),
    };
    public IReadOnlyList<Option<AppTheme>> Themes { get; } = new[]
    {
        new Option<AppTheme>(AppTheme.Dark, "Dark"),
        new Option<AppTheme>(AppTheme.Light, "Light"),
    };
    public IReadOnlyList<Option<int>> BufferLengths { get; } = new[] { 15, 30, 60, 120, 180, 300, 600 }
        .Select(s => new Option<int>(s, s < 60 ? $"{s} seconds" : $"{s / 60} minute{(s >= 120 ? "s" : "")}")).ToList();

    public IReadOnlyList<Option<string>> OutputDevices { get; private set; } = Array.Empty<Option<string>>();
    public IReadOnlyList<Option<string>> InputDevices { get; private set; } = Array.Empty<Option<string>>();

    private void LoadDevices()
    {
        OutputDevices = AudioDevices.List(DataFlow.Render).Select(d => new Option<string>(d.Id, d.Name)).Prepend(new Option<string>("", "Windows default output")).ToList();
        InputDevices = AudioDevices.List(DataFlow.Capture).Select(d => new Option<string>(d.Id, d.Name)).Prepend(new Option<string>("", "Windows default microphone")).ToList();
        OnPropertiesChanged(nameof(OutputDevices), nameof(InputDevices), nameof(SystemAudioDevice), nameof(MicrophoneDevice));
    }

    // ------------------------------------------------------------- recording
    public bool ReplayEnabled { get => S.ReplayBufferEnabled; set => Update(s => s.ReplayBufferEnabled = value); }
    public int BufferSeconds { get => S.ReplayBufferSeconds; set { Update(s => s.ReplayBufferSeconds = value); OnPropertyChanged(nameof(MemoryEstimate)); } }
    public string Monitor { get => S.MonitorDeviceName; set => Update(s => s.MonitorDeviceName = value ?? ""); }
    public CaptureResolution Resolution { get => S.Resolution; set { Update(s => s.Resolution = value); OnPropertyChanged(nameof(MemoryEstimate)); } }
    public int FrameRate { get => S.FrameRate; set { Update(s => s.FrameRate = value); OnPropertyChanged(nameof(MemoryEstimate)); } }
    public VideoCodec Codec { get => S.Codec; set { Update(s => s.Codec = value); OnPropertyChanged(nameof(EncoderStatus)); } }
    public ContainerFormat Container { get => S.Container; set => Update(s => s.Container = value); }
    public QualityPreset Quality { get => S.Quality; set { Update(s => s.Quality = value); OnPropertiesChanged(nameof(IsCustomBitrate), nameof(MemoryEstimate)); } }
    public bool IsCustomBitrate => S.Quality == QualityPreset.Custom;
    public double CustomBitrateMbps { get => S.CustomBitrateKbps / 1000.0; set { Update(s => s.CustomBitrateKbps = (int)(value * 1000)); OnPropertyChanged(nameof(MemoryEstimate)); } }
    public EncoderPreference Encoder { get => S.Encoder; set { Update(s => s.Encoder = value); OnPropertyChanged(nameof(EncoderStatus)); } }
    public bool CaptureCursor { get => S.CaptureCursor; set => Update(s => s.CaptureCursor = value); }
    public int ShortClipSeconds { get => S.ShortClipSeconds; set => Update(s => s.ShortClipSeconds = value); }
    public int LongClipSeconds { get => S.LongClipSeconds; set => Update(s => s.LongClipSeconds = value); }
    public int CustomClipSeconds { get => S.CustomClipSeconds; set => Update(s => s.CustomClipSeconds = value); }

    public string EncoderStatus
    {
        get
        {
            if (_app.Encoders.Available.Count == 0) return _app.StartupError ?? "Detecting encoders…";
            try
            {
                var sel = _app.Encoders.Select(S.Codec, S.Encoder);
                var found = string.Join(", ", _app.Encoders.Available.Where(e => e.IsHardware).Select(e => e.DisplayName));
                return $"Using {sel.Encoder.DisplayName}. {sel.Notice ?? ""} Detected: {(found.Length > 0 ? found : "no hardware encoder")}.";
            }
            catch (InvalidOperationException ex) { return ex.Message; }
        }
    }

    public string MemoryEstimate
    {
        get
        {
            var display = Interop.DisplayEnumerator.Find(_app.Displays, S.MonitorDeviceName);
            var (w, h) = PipelineConfig.ComputeOutputSize(display?.Width ?? 1920, display?.Height ?? 1080, S.Resolution);
            int kbps = PipelineConfig.ComputeBitrateKbps(S.Quality, S.CustomBitrateKbps, w, h, S.FrameRate, S.Codec);
            long bytes = (long)((kbps + 400) * 1000L / 8 * (S.ReplayBufferSeconds + 2));
            return $"{w}×{h} at {kbps / 1000.0:0.#} Mbps · the replay buffer uses about {Controls.Format.Bytes(bytes)} of RAM (no disk writes).";
        }
    }

    // ------------------------------------------------------------- audio
    public bool CaptureSystemAudio { get => S.CaptureSystemAudio; set => Update(s => s.CaptureSystemAudio = value); }
    public string SystemAudioDevice { get => S.SystemAudioDeviceId; set => Update(s => s.SystemAudioDeviceId = value ?? ""); }
    public double SystemAudioVolume { get => S.SystemAudioVolume * 100; set => Update(s => s.SystemAudioVolume = value / 100); }
    public bool CaptureMicrophone { get => S.CaptureMicrophone; set => Update(s => s.CaptureMicrophone = value); }
    public string MicrophoneDevice { get => S.MicrophoneDeviceId; set => Update(s => s.MicrophoneDeviceId = value ?? ""); }
    public double MicrophoneVolume { get => S.MicrophoneVolume * 100; set => Update(s => s.MicrophoneVolume = value / 100); }
    public bool SeparateAudioTracks { get => S.SeparateAudioTracks; set => Update(s => s.SeparateAudioTracks = value); }

    // ------------------------------------------------------------- storage
    public string ClipsDirectory => S.ClipsDirectory;
    public double MaxStorageGb { get => S.MaxStorageGb; set => Update(s => s.MaxStorageGb = Math.Round(value)); }
    public bool AutoDelete { get => S.AutoDeleteOldClips; set => Update(s => s.AutoDeleteOldClips = value); }
    public int AutoDeleteDays { get => S.AutoDeleteAfterDays; set => Update(s => s.AutoDeleteAfterDays = value); }

    private void BrowseFolder()
    {
        using var dialog = new System.Windows.Forms.FolderBrowserDialog
        {
            Description = "Choose where Duelcliping saves clips and recordings",
            UseDescriptionForTitle = true,
            SelectedPath = S.ClipsDirectory,
        };
        if (dialog.ShowDialog() != System.Windows.Forms.DialogResult.OK) return;
        try
        {
            System.IO.Directory.CreateDirectory(dialog.SelectedPath);
            var probe = System.IO.Path.Combine(dialog.SelectedPath, ".duelcliping-write-test");
            System.IO.File.WriteAllText(probe, "");
            System.IO.File.Delete(probe);
            Update(s => s.ClipsDirectory = dialog.SelectedPath, nameof(ClipsDirectory));
        }
        catch (Exception ex)
        {
            _app.RaiseToast(new Toast(ToastKind.Error, "Folder not usable", "Duelcliping cannot write to that folder: " + ex.Message));
        }
    }

    // ------------------------------------------------------------- hotkeys
    public HotkeyAction? Capturing { get => _capturing; private set { Set(ref _capturing, value); OnPropertyChanged(nameof(IsCapturing)); } }
    public bool IsCapturing => Capturing != null;
    public string? HotkeyMessage { get => _hotkeyMessage; private set => Set(ref _hotkeyMessage, value); }
    public string HotkeySaveShort => Label(S.HotkeySaveShort, HotkeyAction.SaveShort);
    public string HotkeySaveLong => Label(S.HotkeySaveLong, HotkeyAction.SaveLong);
    public string HotkeyToggleRecording => Label(S.HotkeyToggleRecording, HotkeyAction.ToggleRecording);
    public string HotkeyOpenApp => Label(S.HotkeyOpenApp, HotkeyAction.OpenApp);

    private string Label(HotkeyBinding binding, HotkeyAction action) =>
        Capturing == action ? "Press keys…" : binding + (_app.Hotkeys.Failures.TryGetValue(action, out var f) ? "  ⚠ " + f : "");

    /// <summary>Called by the view with the next key press while capturing.</summary>
    public void ApplyCapturedKey(int virtualKey, HotkeyModifiers modifiers, bool cancel, bool clear)
    {
        if (Capturing is not { } action) return;
        Capturing = null;
        _app.Hotkeys.Apply(S); // re-register (again below if the binding changes)
        if (cancel) { HotkeyMessage = null; RefreshHotkeys(); return; }
        var binding = clear ? HotkeyBinding.None : new HotkeyBinding(modifiers, virtualKey);
        var warning = Services.HotkeyManager.GameConflictWarning(binding);
        if (warning != null) { HotkeyMessage = warning; RefreshHotkeys(); return; }
        var s = S;
        var others = new[] { (HotkeyAction.SaveShort, s.HotkeySaveShort), (HotkeyAction.SaveLong, s.HotkeySaveLong),
            (HotkeyAction.ToggleRecording, s.HotkeyToggleRecording), (HotkeyAction.OpenApp, s.HotkeyOpenApp) };
        if (!binding.IsEmpty && others.Any(o => o.Item1 != action && o.Item2.Equals(binding)))
        {
            HotkeyMessage = $"{binding} is already used by another Duelcliping action.";
            RefreshHotkeys();
            return;
        }
        Update(st =>
        {
            switch (action)
            {
                case HotkeyAction.SaveShort: st.HotkeySaveShort = binding; break;
                case HotkeyAction.SaveLong: st.HotkeySaveLong = binding; break;
                case HotkeyAction.ToggleRecording: st.HotkeyToggleRecording = binding; break;
                case HotkeyAction.OpenApp: st.HotkeyOpenApp = binding; break;
            }
        });
        HotkeyMessage = _app.Hotkeys.Failures.TryGetValue(action, out var failure) ? failure : null;
        RefreshHotkeys();
    }

    private void RefreshHotkeys() =>
        OnPropertiesChanged(nameof(HotkeySaveShort), nameof(HotkeySaveLong), nameof(HotkeyToggleRecording), nameof(HotkeyOpenApp));

    // ------------------------------------------------------------- general
    public bool StartWithWindows { get => S.StartWithWindows; set => Update(s => s.StartWithWindows = value); }
    public bool StartMinimized { get => S.StartMinimized; set => Update(s => s.StartMinimized = value); }
    public bool CloseToTray { get => S.CloseToTray; set => Update(s => s.CloseToTray = value); }
    public bool OverlayEnabled { get => S.OverlayEnabled; set => Update(s => s.OverlayEnabled = value); }
    public OverlayCorner OverlayPosition { get => S.OverlayPosition; set => Update(s => s.OverlayPosition = value); }
    public double OverlayOpacity { get => S.OverlayOpacity * 100; set => Update(s => s.OverlayOpacity = value / 100); }
    public bool PlaySound { get => S.PlaySoundOnSave; set => Update(s => s.PlaySoundOnSave = value); }
    public bool Notify { get => S.NotifyOnSave; set => Update(s => s.NotifyOnSave = value); }
    public bool PerformanceMode { get => S.PerformanceMode; set => Update(s => s.PerformanceMode = value); }
    public AppTheme Theme { get => S.Theme; set => Update(s => s.Theme = value); }
    public string PresentMonPath => string.IsNullOrEmpty(S.PresentMonPath) ? "Not set" : S.PresentMonPath;

    private void BrowsePresentMon()
    {
        var dialog = new Microsoft.Win32.OpenFileDialog { Title = "Select PresentMon.exe", Filter = "PresentMon|PresentMon*.exe|Programs|*.exe" };
        if (dialog.ShowDialog() == true) Update(s => s.PresentMonPath = dialog.FileName, nameof(PresentMonPath));
    }

    public void RefreshAll() => OnPropertyChanged(string.Empty);
}
