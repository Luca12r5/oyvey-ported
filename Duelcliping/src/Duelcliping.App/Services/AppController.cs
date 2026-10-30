using System.Diagnostics;
using System.IO;
using Duelcliping.App.Interop;
using Duelcliping.Core.Audio;
using Duelcliping.Core.Encoders;
using Duelcliping.Core.Library;
using Duelcliping.Core.Logging;
using Duelcliping.Core.Performance;
using Duelcliping.Core.Recording;
using Duelcliping.Core.Settings;
using Duelcliping.Core.Storage;
using Microsoft.Win32;
using NAudio.CoreAudioApi;

namespace Duelcliping.App.Services;

public enum ToastKind { Success, Info, Warning, Error }

public sealed record Toast(ToastKind Kind, string Title, string Message, string? FilePath = null);

/// <summary>
/// Composition root and coordinator. Owns the recording engine, settings, library, hotkeys and
/// monitors, and translates settings into a <see cref="CaptureRequest"/>. The UI only talks to this
/// class; the engine never knows a UI exists.
/// </summary>
public sealed class AppController : IAsyncDisposable
{
    private readonly SemaphoreSlim _applyGate = new(1, 1);
    private readonly WindowsSystemMetrics _metrics = new();
    private CancellationTokenSource? _displayDebounce;
    private bool _clipSaveInProgress;

    public AppController()
    {
        var dataDir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "Duelcliping");
        DataDirectory = dataDir;
        Log.Initialize(Path.Combine(dataDir, "logs"));
        Settings = new SettingsManager(SettingsManager.DefaultPath());
        Settings.Load();
        Encoders = new EncoderCatalog(Path.Combine(dataDir, "encoders.json"));
        Engine = new RecordingEngine();
        Library = new ClipLibrary(Settings.Current.ClipsDirectory) { DeleteFile = ShellHelpers.DeleteToRecycleBin };
        Storage = new StorageManager(Library);
        Performance = new PerformanceMonitor(Engine, _metrics);
        GameDetector = new GameActivityDetector();
        Hotkeys = new HotkeyManager();

        Settings.Changed += OnSettingsChanged;
        Hotkeys.Pressed += OnHotkey;
        Engine.Notice += message => RaiseToast(new Toast(ToastKind.Warning, "Recording", message));
        Engine.StateChanged += (state, error) =>
        {
            if (state == EngineState.Faulted && error != null) RaiseToast(new Toast(ToastKind.Error, "Recording problem", error));
            StatusChanged?.Invoke();
        };
        Engine.ManualRecordingFailed += reason => RaiseToast(new Toast(ToastKind.Error, "Recording stopped", reason));
        GameDetector.Changed += OnGameActivityChanged;
        SystemEvents.DisplaySettingsChanged += OnDisplaySettingsChanged;
        SystemEvents.PowerModeChanged += OnPowerModeChanged;
    }

    public string DataDirectory { get; }
    public SettingsManager Settings { get; }
    public EncoderCatalog Encoders { get; }
    public RecordingEngine Engine { get; }
    public ClipLibrary Library { get; }
    public StorageManager Storage { get; }
    public PerformanceMonitor Performance { get; }
    public GameActivityDetector GameDetector { get; }
    public HotkeyManager Hotkeys { get; }
    public IReadOnlyList<DisplayInfo> Displays { get; private set; } = Array.Empty<DisplayInfo>();
    public EncoderSelection? ActiveEncoder { get; private set; }
    public string? StartupError { get; private set; }

    /// <summary>Raised (any thread) for notifications shown as toasts.</summary>
    public event Action<Toast>? ToastRaised;
    /// <summary>Raised (any thread) when recording state or configuration changed.</summary>
    public event Action? StatusChanged;
    /// <summary>Raised (any thread) when a clip or recording file was created.</summary>
    public event Action<string>? ClipCreated;
    public event Action? OpenRequested;

    public async Task StartAsync()
    {
        var s = Settings.Current;
        Hotkeys.Apply(s);
        ApplyProcessPriority();
        // The installer may have created the autostart entry; adopt it on the first run instead of removing it.
        if (Settings.IsFirstRun && StartupManager.IsEnabled()) s = Settings.Update(x => x.StartWithWindows = true);
        try { StartupManager.Apply(s.StartWithWindows); }
        catch (InvalidOperationException ex) { RaiseToast(new Toast(ToastKind.Warning, "Startup", ex.Message)); }

        Displays = DisplayEnumerator.Enumerate();
        try
        {
            await Encoders.DetectAsync(forceRefresh: false).ConfigureAwait(false);
        }
        catch (Exception ex)
        {
            StartupError = "No video encoder could be started: " + ex.Message;
            Log.Error("Startup", StartupError, ex);
            RaiseToast(new Toast(ToastKind.Error, "Duelcliping cannot record", StartupError));
        }

        // Finish recordings interrupted by a crash or power loss before anything else touches the folder.
        _ = Task.Run(async () =>
        {
            try
            {
                var recovered = await Library.RecoverInterruptedAsync(s.Container).ConfigureAwait(false);
                foreach (var r in recovered)
                {
                    RaiseToast(new Toast(ToastKind.Info, "Recording recovered", Path.GetFileName(r.FilePath), r.FilePath));
                    ClipCreated?.Invoke(r.FilePath);
                }
            }
            catch (Exception ex) { Log.Error("Startup", "Recovery failed", ex); }
        });

        if (Encoders.IsDetected && Encoders.Available.Count > 0) await ApplyCaptureAsync().ConfigureAwait(false);
        _ = RunRetentionAsync();
        StatusChanged?.Invoke();
    }

    public async Task RedetectEncodersAsync()
    {
        await Encoders.DetectAsync(forceRefresh: true).ConfigureAwait(false);
        StartupError = null;
        await ApplyCaptureAsync().ConfigureAwait(false);
    }

    private CaptureRequest BuildRequest(AppSettings s)
    {
        var display = DisplayEnumerator.Find(Displays, s.MonitorDeviceName)
                      ?? throw new InvalidOperationException("No monitor was found to record.");
        var selection = Encoders.Select(s.Codec, s.Encoder);
        ActiveEncoder = selection;
        var audio = new List<AudioSourceSpec>();
        if (s.CaptureSystemAudio)
            audio.Add(new AudioSourceSpec("game", "Game Audio", () => new WasapiAudioSource(DataFlow.Render, s.SystemAudioDeviceId), s.SystemAudioVolume));
        if (s.CaptureMicrophone)
            audio.Add(new AudioSourceSpec("mic", "Microphone", () => new WasapiAudioSource(DataFlow.Capture, s.MicrophoneDeviceId), s.MicrophoneVolume));

        return new CaptureRequest
        {
            Source = CaptureSource.DesktopDuplication,
            Monitor = display.ToTarget(),
            Encoder = selection,
            // No silent CPU encoding: it costs game FPS. Used only when chosen in the settings.
            SoftwareFallback = null,
            Resolution = s.Resolution,
            FrameRate = s.FrameRate,
            Quality = s.Quality,
            CustomBitrateKbps = s.CustomBitrateKbps,
            CaptureCursor = s.CaptureCursor,
            PerformanceMode = s.PerformanceMode,
            SeparateAudioTracks = s.SeparateAudioTracks,
            Audio = audio,
            ReplayEnabled = s.ReplayBufferEnabled,
            BufferSeconds = s.ReplayBufferSeconds,
        };
    }

    public async Task ApplyCaptureAsync()
    {
        await _applyGate.WaitAsync().ConfigureAwait(false);
        try
        {
            var request = BuildRequest(Settings.Current);
            await Engine.ApplyAsync(request).ConfigureAwait(false);
        }
        catch (Exception ex)
        {
            Log.Error("Controller", "Could not apply recording settings", ex);
            RaiseToast(new Toast(ToastKind.Error, "Recording settings", ex.Message));
        }
        finally
        {
            _applyGate.Release();
            StatusChanged?.Invoke();
        }
    }

    /// <summary>Settings that require the capture pipeline to restart when they change.</summary>
    private static bool CaptureChanged(AppSettings a, AppSettings b) =>
        a.ReplayBufferEnabled != b.ReplayBufferEnabled || a.ReplayBufferSeconds != b.ReplayBufferSeconds ||
        a.MonitorDeviceName != b.MonitorDeviceName || a.Resolution != b.Resolution || a.FrameRate != b.FrameRate ||
        a.Codec != b.Codec || a.Quality != b.Quality || a.CustomBitrateKbps != b.CustomBitrateKbps || a.Encoder != b.Encoder ||
        a.CaptureCursor != b.CaptureCursor || a.CaptureSystemAudio != b.CaptureSystemAudio || a.SystemAudioDeviceId != b.SystemAudioDeviceId ||
        a.CaptureMicrophone != b.CaptureMicrophone || a.MicrophoneDeviceId != b.MicrophoneDeviceId ||
        a.SeparateAudioTracks != b.SeparateAudioTracks || a.PerformanceMode != b.PerformanceMode;

    private void OnSettingsChanged(AppSettings old, AppSettings now)
    {
        if (!old.HotkeySaveShort.Equals(now.HotkeySaveShort) || !old.HotkeySaveLong.Equals(now.HotkeySaveLong) ||
            !old.HotkeyToggleRecording.Equals(now.HotkeyToggleRecording) || !old.HotkeyOpenApp.Equals(now.HotkeyOpenApp))
        {
            System.Windows.Application.Current?.Dispatcher.Invoke(() => Hotkeys.Apply(now));
        }
        if (old.StartWithWindows != now.StartWithWindows)
        {
            try { StartupManager.Apply(now.StartWithWindows); }
            catch (InvalidOperationException ex) { RaiseToast(new Toast(ToastKind.Error, "Startup", ex.Message)); }
        }
        if (old.ClipsDirectory != now.ClipsDirectory) Library.ChangeDirectory(now.ClipsDirectory);
        if (old.PerformanceMode != now.PerformanceMode) ApplyProcessPriority();

        if (Math.Abs(old.SystemAudioVolume - now.SystemAudioVolume) > 0.001) Engine.SetVolume("game", now.SystemAudioVolume);
        if (Math.Abs(old.MicrophoneVolume - now.MicrophoneVolume) > 0.001) Engine.SetVolume("mic", now.MicrophoneVolume);

        if (CaptureChanged(old, now) && Encoders.Available.Count > 0) _ = ApplyCaptureAsync();
        if (old.MaxStorageGb != now.MaxStorageGb || old.AutoDeleteOldClips != now.AutoDeleteOldClips || old.AutoDeleteAfterDays != now.AutoDeleteAfterDays)
            _ = RunRetentionAsync();
        StatusChanged?.Invoke();
    }

    private void ApplyProcessPriority()
    {
        try
        {
            using var self = Process.GetCurrentProcess();
            self.PriorityClass = Settings.Current.PerformanceMode ? ProcessPriorityClass.BelowNormal : ProcessPriorityClass.Normal;
        }
        catch (Exception ex) { Log.Warn("Controller", "Could not change process priority: " + ex.Message); }
    }

    private void OnGameActivityChanged(bool active)
    {
        // While a game is fullscreen, trim our working set once so the game gets the memory.
        if (active && Settings.Current.PerformanceMode)
        {
            GC.Collect(2, GCCollectionMode.Optimized, blocking: false);
            Engine.Buffer.Pool.Trim();
        }
        StatusChanged?.Invoke();
    }

    private void OnHotkey(HotkeyAction action)
    {
        var s = Settings.Current;
        switch (action)
        {
            case HotkeyAction.SaveShort: _ = SaveClipAsync(s.ShortClipSeconds); break;
            case HotkeyAction.SaveLong: _ = SaveClipAsync(s.LongClipSeconds); break;
            case HotkeyAction.ToggleRecording: _ = ToggleManualRecordingAsync(); break;
            case HotkeyAction.OpenApp: OpenRequested?.Invoke(); break;
        }
    }

    public async Task<ClipSaveResult?> SaveClipAsync(double seconds)
    {
        if (_clipSaveInProgress)
        {
            RaiseToast(new Toast(ToastKind.Info, "Saving", "A clip is already being saved."));
            return null;
        }
        _clipSaveInProgress = true;
        try
        {
            var s = Settings.Current;
            var result = await Engine.SaveClipAsync(seconds, s.ClipsDirectory, s.Container).ConfigureAwait(false);
            Performance.LastDiskWriteMBps = result.DiskWriteMBps;
            if (s.PlaySoundOnSave) SoundEffects.PlayClipSaved();
            if (s.NotifyOnSave || result.Warning != null)
                RaiseToast(new Toast(result.Warning == null ? ToastKind.Success : ToastKind.Warning,
                    $"Clip saved · {FormatDuration(result.DurationSeconds)}", result.Warning ?? Path.GetFileName(result.FilePath), result.FilePath));
            ClipCreated?.Invoke(result.FilePath);
            _ = RunRetentionAsync();
            return result;
        }
        catch (Exception ex) when (ex is InvalidOperationException or IOException or UnauthorizedAccessException)
        {
            Log.Error("Controller", "Clip could not be saved", ex);
            RaiseToast(new Toast(ToastKind.Error, "Clip not saved", ex.Message));
            return null;
        }
        finally { _clipSaveInProgress = false; }
    }

    public async Task ToggleManualRecordingAsync()
    {
        try
        {
            var s = Settings.Current;
            if (Engine.IsManualRecording)
            {
                var results = await Engine.StopManualRecordingAsync().ConfigureAwait(false);
                foreach (var r in results)
                {
                    RaiseToast(new Toast(r.Remuxed ? ToastKind.Success : ToastKind.Warning,
                        $"Recording saved · {FormatDuration(r.DurationSeconds)}", r.Warning ?? Path.GetFileName(r.FilePath), r.FilePath));
                    ClipCreated?.Invoke(r.FilePath);
                }
                if (s.PlaySoundOnSave) SoundEffects.PlayClipSaved();
            }
            else
            {
                if (Engine.Request == null) throw new InvalidOperationException(StartupError ?? "Recording is not configured yet.");
                await Engine.StartManualRecordingAsync(s.ClipsDirectory, s.Container).ConfigureAwait(false);
                RaiseToast(new Toast(ToastKind.Info, "Recording started", $"Press {s.HotkeyToggleRecording} again to stop."));
            }
        }
        catch (Exception ex) when (ex is InvalidOperationException or IOException or UnauthorizedAccessException)
        {
            Log.Error("Controller", "Manual recording toggle failed", ex);
            RaiseToast(new Toast(ToastKind.Error, "Recording", ex.Message));
        }
        StatusChanged?.Invoke();
    }

    public void SetReplayEnabled(bool enabled) => Settings.Update(s => s.ReplayBufferEnabled = enabled);

    private async Task RunRetentionAsync()
    {
        try
        {
            // Retention scans and deletes files; never do that while a game is fullscreen.
            while (GameDetector.IsGameActive && Settings.Current.PerformanceMode) await Task.Delay(TimeSpan.FromSeconds(30)).ConfigureAwait(false);
            var result = await Storage.EnforceAsync(Settings.Current).ConfigureAwait(false);
            if (result.DeletedCount > 0) RaiseToast(new Toast(ToastKind.Info, "Storage", $"Removed {result.DeletedCount} old clip(s) to stay within your storage limit."));
            if (result.Warning != null) RaiseToast(new Toast(ToastKind.Warning, "Storage", result.Warning));
        }
        catch (Exception ex) { Log.Warn("Controller", "Retention failed: " + ex.Message); }
    }

    private void OnDisplaySettingsChanged(object? sender, EventArgs e)
    {
        // Resolution / monitor changes (often caused by games switching modes): re-read monitors and
        // restart the capture once things settled.
        _displayDebounce?.Cancel();
        var cts = _displayDebounce = new CancellationTokenSource();
        _ = Task.Run(async () =>
        {
            try
            {
                await Task.Delay(1500, cts.Token).ConfigureAwait(false);
                var displays = DisplayEnumerator.Enumerate();
                bool changed = !displays.SequenceEqual(Displays);
                Displays = displays;
                if (changed && Encoders.Available.Count > 0)
                {
                    Log.Info("Controller", "Display configuration changed; restarting capture");
                    await ApplyCaptureAsync().ConfigureAwait(false);
                }
            }
            catch (OperationCanceledException) { }
        });
    }

    private void OnPowerModeChanged(object sender, PowerModeChangedEventArgs e)
    {
        if (e.Mode == PowerModes.Resume && Encoders.Available.Count > 0)
        {
            Log.Info("Controller", "System resumed; restarting capture");
            _ = Task.Delay(3000).ContinueWith(_ => ApplyCaptureAsync());
        }
    }

    public void RaiseToast(Toast toast) => ToastRaised?.Invoke(toast);
    public void NotifyClipCreated(string path) => ClipCreated?.Invoke(path);

    public static string FormatDuration(double seconds)
    {
        var t = TimeSpan.FromSeconds(Math.Max(0, seconds));
        return t.TotalHours >= 1 ? t.ToString(@"h\:mm\:ss") : t.ToString(@"m\:ss");
    }

    public async ValueTask DisposeAsync()
    {
        SystemEvents.DisplaySettingsChanged -= OnDisplaySettingsChanged;
        SystemEvents.PowerModeChanged -= OnPowerModeChanged;
        Hotkeys.Dispose();
        GameDetector.Dispose();
        Performance.Dispose();
        try
        {
            if (Engine.IsManualRecording) await Engine.StopManualRecordingAsync().ConfigureAwait(false);
            await Engine.DisposeAsync().ConfigureAwait(false);
        }
        catch (Exception ex) { Log.Error("Controller", "Shutdown error", ex); }
        _metrics.Dispose();
        Log.Info("Controller", "Duelcliping stopped");
        Log.Flush(TimeSpan.FromSeconds(1));
    }
}
