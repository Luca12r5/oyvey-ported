using Duelcliping.Core.Encoders;
using Duelcliping.Core.Logging;
using Duelcliping.Core.Settings;

namespace Duelcliping.Core.Recording;

public enum EngineState { Stopped, Starting, Running, Restarting, Faulted }

/// <summary>Everything the engine needs to capture; built by the application from the settings.</summary>
public sealed record CaptureRequest
{
    public required CaptureSource Source { get; init; }
    public required MonitorTarget Monitor { get; init; }
    public required EncoderSelection Encoder { get; init; }
    /// <summary>CPU encoder used only if every hardware path failed. Null disables the fallback.</summary>
    public EncoderInfo? SoftwareFallback { get; init; }
    public required CaptureResolution Resolution { get; init; }
    public required int FrameRate { get; init; }
    public required QualityPreset Quality { get; init; }
    public int CustomBitrateKbps { get; init; } = 20000;
    public bool CaptureCursor { get; init; } = true;
    public bool PerformanceMode { get; init; } = true;
    public bool SeparateAudioTracks { get; init; } = true;
    public IReadOnlyList<AudioSourceSpec> Audio { get; init; } = Array.Empty<AudioSourceSpec>();
    public bool ReplayEnabled { get; init; } = true;
    public double BufferSeconds { get; init; } = 120;

    public IReadOnlyList<string> TrackTitles =>
        Audio.Count == 0 ? Array.Empty<string>() :
        SeparateAudioTracks || Audio.Count == 1 ? Audio.Select(a => a.Title).ToArray() : new[] { "Game + Microphone" };
}

public sealed record EngineStatus(
    EngineState State,
    string? Error,
    string? EncoderName,
    string? PipelineDescription,
    int OutputWidth,
    int OutputHeight,
    int FrameRate,
    int BitrateKbps,
    double CaptureFps,
    long Frames,
    long DroppedFrames,
    long DuplicatedFrames,
    double EncoderSpeed,
    double BacklogMs,
    double BufferedSeconds,
    long BufferedBytes,
    double IncomingMbps,
    bool IsManualRecording,
    double ManualRecordingSeconds,
    int? FfmpegProcessId,
    DateTime? RunningSinceUtc,
    long SilenceFramesInserted);

/// <summary>
/// Supervises the capture pipeline: starts ffmpeg, keeps the replay buffer fed, restarts after
/// failures, steps down to more compatible capture paths when a path does not work on this machine,
/// and serves clip saves and manual recordings. Completely independent of any UI.
/// </summary>
public sealed class RecordingEngine : IAsyncDisposable
{
    private readonly SemaphoreSlim _control = new(1, 1);
    private readonly object _statusLock = new();
    private CaptureRequest? _request;
    private CancellationTokenSource? _cts;
    private Task? _supervisor;
    private CapturePipeline? _pipeline;
    private RecordingSink? _sink;
    private volatile bool _stopRequested;
    private EngineState _state = EngineState.Stopped;
    private string? _error;
    private int _keyframesThisGeneration;
    private DateTime? _runningSince;
    private long _lastBytes;
    private DateTime _lastBytesAt = DateTime.UtcNow;
    private double _incomingMbps;

    public RecordingEngine()
    {
        Buffer = new ReplayBuffer();
        Buffer.ChunkStarted += _ =>
        {
            if (Interlocked.Increment(ref _keyframesThisGeneration) == 1) OnFirstKeyframe();
        };
    }

    public ReplayBuffer Buffer { get; }
    public EngineState State { get { lock (_statusLock) return _state; } }
    public bool IsManualRecording => _sink != null;
    public CaptureRequest? Request => _request;

    /// <summary>State changes; raised on a background thread.</summary>
    public event Action<EngineState, string?>? StateChanged;
    /// <summary>Human readable notices (fallbacks, restarts); raised on a background thread.</summary>
    public event Action<string>? Notice;
    /// <summary>Manual recording stopped by itself (disk full, error). Argument is the reason.</summary>
    public event Action<string>? ManualRecordingFailed;

    /// <summary>Applies a new configuration, restarting the pipeline only when it has to run.</summary>
    public async Task ApplyAsync(CaptureRequest request)
    {
        await _control.WaitAsync().ConfigureAwait(false);
        try
        {
            _request = request;
            Buffer.MaxDurationSeconds = request.ReplayEnabled ? request.BufferSeconds : 2;
            Buffer.MaxBytes = EstimateBufferCapBytes(request);
            await StopSupervisorAsync().ConfigureAwait(false);
            if (ShouldRun) StartSupervisor();
        }
        finally { _control.Release(); }
    }

    private bool ShouldRun => _request != null && (_request.ReplayEnabled || _sink != null);

    /// <summary>Estimated memory use of the replay buffer for the given settings.</summary>
    public static long EstimateBufferBytes(PipelineConfig config, double seconds) =>
        (long)((config.BitrateKbps + 400) * 1000L / 8 * (seconds + 2));

    private static long EstimateBufferCapBytes(CaptureRequest r)
    {
        var cfg = ToPipelineConfig(r, r.Source, PipelinePath.ZeroCopy, r.Encoder.Encoder);
        // Rate control may overshoot; allow 2x the nominal size before force-evicting.
        return Math.Max(256L * 1024 * 1024, EstimateBufferBytes(cfg, r.ReplayEnabled ? r.BufferSeconds : 2) * 2);
    }

    public async Task StopAsync()
    {
        await _control.WaitAsync().ConfigureAwait(false);
        try
        {
            _request = _request == null ? null : _request with { ReplayEnabled = false };
            if (_sink != null) await StopManualRecordingCoreAsync().ConfigureAwait(false);
            await StopSupervisorAsync().ConfigureAwait(false);
            Buffer.Clear();
            SetState(EngineState.Stopped, null);
        }
        finally { _control.Release(); }
    }

    public void SetVolume(string key, double volume) => _pipeline?.SetVolume(key, volume);

    private void StartSupervisor()
    {
        _cts = new CancellationTokenSource();
        _stopRequested = false;
        var request = _request!;
        var token = _cts.Token;
        _supervisor = Task.Run(() => SuperviseAsync(request, token));
    }

    private async Task StopSupervisorAsync()
    {
        if (_cts == null) return;
        // Stop ffmpeg gracefully first so the tail of the stream reaches the buffer, then cancel.
        _stopRequested = true;
        _pipeline?.Stop(TimeSpan.FromSeconds(3));
        _cts.Cancel();
        try { if (_supervisor != null) await _supervisor.ConfigureAwait(false); }
        catch (OperationCanceledException) { }
        catch (Exception ex) { Log.Error("Engine", "Supervisor ended with an error", ex); }
        _cts.Dispose();
        _cts = null;
        _supervisor = null;
        SetState(EngineState.Stopped, null);
    }

    internal static IReadOnlyList<(CaptureSource Source, PipelinePath Path, EncoderInfo Encoder)> BuildPlan(CaptureRequest r)
    {
        var plan = new List<(CaptureSource, PipelinePath, EncoderInfo)>();
        var encoder = r.Encoder.Encoder;
        if (r.Source == CaptureSource.TestPattern)
        {
            plan.Add((CaptureSource.TestPattern, PipelinePath.SystemMemory, encoder));
            return plan;
        }
        // GDI capture and CPU frame copies cost a lot of game FPS, so they are never used silently:
        // only GPU paths are tried, and the CPU encoder only when the user selected it or no
        // hardware encoder exists at all.
        if (encoder.IsHardware)
        {
            plan.Add((CaptureSource.DesktopDuplication, PipelinePath.ZeroCopy, encoder));
            plan.Add((CaptureSource.DesktopDuplication, PipelinePath.SystemMemory, encoder));
        }
        var software = encoder.IsHardware ? r.SoftwareFallback : encoder;
        if (software != null) plan.Add((CaptureSource.DesktopDuplication, PipelinePath.SystemMemory, software));
        return plan;
    }

    internal static PipelineConfig ToPipelineConfig(CaptureRequest r, CaptureSource source, PipelinePath path, EncoderInfo encoder) => new()
    {
        Source = source,
        Path = path,
        Monitor = r.Monitor,
        Encoder = encoder,
        Resolution = r.Resolution,
        FrameRate = r.FrameRate,
        Quality = r.Quality,
        CustomBitrateKbps = r.CustomBitrateKbps,
        CaptureCursor = r.CaptureCursor,
        PerformanceMode = r.PerformanceMode,
        SeparateAudioTracks = r.SeparateAudioTracks,
    };

    private static string Describe(PipelineConfig c) =>
        $"{c.Encoder.DisplayName} · " + (c.Source, c.Path) switch
        {
            (CaptureSource.DesktopDuplication, PipelinePath.ZeroCopy) => "GPU zero-copy",
            (CaptureSource.DesktopDuplication, _) => "GPU capture, CPU conversion (slower)",
            (CaptureSource.Gdi, _) => "GDI capture (compatibility)",
            _ => "test pattern",
        };

    private async Task SuperviseAsync(CaptureRequest request, CancellationToken ct)
    {
        var plan = BuildPlan(request);
        if (plan.Count == 0)
        {
            SetState(EngineState.Faulted, "No usable capture path is available.");
            return;
        }
        int step = 0;
        var runtimeFailures = new Queue<DateTime>();
        if (request.Encoder.Notice != null) Notice?.Invoke(request.Encoder.Notice);

        while (!ct.IsCancellationRequested)
        {
            var (source, path, encoder) = plan[step];
            var config = ToPipelineConfig(request, source, path, encoder);
            Buffer.BeginGeneration();
            Interlocked.Exchange(ref _keyframesThisGeneration, 0);
            SetState(_state == EngineState.Running ? EngineState.Restarting : EngineState.Starting, null);

            var pipeline = new CapturePipeline(config, request.Audio, Buffer);
            DateTime started = DateTime.UtcNow;
            string reason;
            try
            {
                _pipeline = pipeline;
                pipeline.Start();
                reason = await MonitorAsync(pipeline, ct).ConfigureAwait(false);
            }
            catch (FileNotFoundException ex)
            {
                SetState(EngineState.Faulted, ex.Message);
                return;
            }
            catch (OperationCanceledException)
            {
                reason = "stopped";
            }
            finally
            {
                if (!_stopRequested) pipeline.Stop(TimeSpan.FromSeconds(2));
                Buffer.EndGeneration();
                pipeline.Dispose();
                _pipeline = null;
            }
            if (_stopRequested || ct.IsCancellationRequested) break;
            bool startupFailed = Volatile.Read(ref _keyframesThisGeneration) == 0;

            if (startupFailed)
            {
                Log.Warn("Engine", $"Capture path failed to start ({Describe(config)}): {reason}");
                if (step + 1 < plan.Count)
                {
                    var next = plan[step + 1];
                    step++;
                    string message = next.Encoder.IsHardware || !encoder.IsHardware
                        ? $"Switching to a more compatible capture mode ({Describe(ToPipelineConfig(request, next.Source, next.Path, next.Encoder))})."
                        : "Hardware encoding failed on this system. Recording continues with CPU encoding, which can lower game FPS.";
                    Notice?.Invoke(message);
                    continue;
                }
                SetState(EngineState.Faulted, "Recording could not be started: " + reason);
                step = 0;
                await Task.Delay(TimeSpan.FromSeconds(15), ct).ConfigureAwait(false);
                continue;
            }

            // The pipeline worked and then stopped (display mode change, driver reset, game switch ...).
            runtimeFailures.Enqueue(DateTime.UtcNow);
            while (runtimeFailures.Count > 0 && DateTime.UtcNow - runtimeFailures.Peek() > TimeSpan.FromMinutes(1)) runtimeFailures.Dequeue();
            Log.Warn("Engine", $"Capture stopped after {(DateTime.UtcNow - started).TotalSeconds:0} s: {reason}. Restarting.");
            if (runtimeFailures.Count >= 5)
            {
                SetState(EngineState.Faulted, "Recording keeps stopping: " + reason + " Retrying in 30 seconds.");
                await Task.Delay(TimeSpan.FromSeconds(30), ct).ConfigureAwait(false);
                runtimeFailures.Clear();
            }
            else
            {
                SetState(EngineState.Restarting, null);
                await Task.Delay(TimeSpan.FromSeconds(1), ct).ConfigureAwait(false);
            }
        }
    }

    /// <summary>Waits until the pipeline exits, hangs, or fails to deliver a keyframe; returns the reason.</summary>
    private async Task<string> MonitorAsync(CapturePipeline pipeline, CancellationToken ct)
    {
        var exit = pipeline.WaitForExitAsync(ct);
        while (true)
        {
            var finished = await Task.WhenAny(exit, Task.Delay(1000, ct)).ConfigureAwait(false);
            if (_stopRequested) return "stopped";
            ct.ThrowIfCancellationRequested();
            if (finished == exit)
            {
                int code = await exit.ConfigureAwait(false);
                return $"ffmpeg exited with code {code}: {pipeline.ErrorSummary}";
            }
            var p = pipeline.Progress;
            UpdateThroughput(p);
            bool hasVideo = Volatile.Read(ref _keyframesThisGeneration) > 0;
            if (!hasVideo && DateTime.UtcNow - p.StartedUtc > TimeSpan.FromSeconds(20))
            {
                pipeline.Kill();
                return "no video arrived within 20 seconds. " + pipeline.ErrorSummary;
            }
            if (hasVideo && DateTime.UtcNow - p.LastDataUtc > TimeSpan.FromSeconds(10))
            {
                pipeline.Kill();
                return "the capture stalled for 10 seconds";
            }
        }
    }

    private void UpdateThroughput(PipelineProgress p)
    {
        var now = DateTime.UtcNow;
        long bytes = Interlocked.Read(ref p.BytesReceived);
        double seconds = (now - _lastBytesAt).TotalSeconds;
        if (seconds >= 1)
        {
            long delta = bytes - _lastBytes;
            if (delta >= 0) _incomingMbps = delta * 8 / 1_000_000.0 / seconds;
            _lastBytes = bytes;
            _lastBytesAt = now;
        }
    }

    private void OnFirstKeyframe()
    {
        _lastBytes = 0;
        _lastBytesAt = DateTime.UtcNow;
        lock (_statusLock) _runningSince ??= DateTime.UtcNow;
        SetState(EngineState.Running, null);
    }

    private void SetState(EngineState state, string? error)
    {
        bool changed;
        lock (_statusLock)
        {
            changed = _state != state || _error != error;
            _state = state;
            _error = error;
            if (state is EngineState.Stopped or EngineState.Faulted) _runningSince = null;
        }
        if (!changed) return;
        if (error != null) Log.Error("Engine", error);
        else Log.Info("Engine", "State: " + state);
        StateChanged?.Invoke(state, error);
    }

    public EngineStatus GetStatus()
    {
        var pipeline = _pipeline;
        var config = pipeline?.Config;
        var p = pipeline?.Progress;
        var sink = _sink;
        long silence = pipeline?.AudioHealth().Sum(a => a.Silence) ?? 0;
        lock (_statusLock)
        {
            return new EngineStatus(
                _state, _error,
                config?.Encoder.DisplayName,
                config == null ? null : Describe(config),
                config?.OutputSize.Width ?? 0, config?.OutputSize.Height ?? 0,
                config?.FrameRate ?? _request?.FrameRate ?? 0,
                config?.BitrateKbps ?? 0,
                p?.Fps ?? 0, p?.Frames ?? 0, p?.DroppedFrames ?? 0, p?.DuplicatedFrames ?? 0,
                p?.Speed ?? 0, p?.BacklogMs ?? 0,
                Buffer.BufferedSeconds, Buffer.BufferedBytes,
                _state == EngineState.Running ? _incomingMbps : 0,
                sink != null, sink?.RecordedSeconds ?? 0,
                pipeline?.ProcessId,
                _runningSince,
                silence);
        }
    }

    // ---------------------------------------------------------------- clips

    /// <summary>Saves the newest <paramref name="seconds"/> of the replay buffer as a clip.</summary>
    public async Task<ClipSaveResult> SaveClipAsync(double seconds, string directory, ContainerFormat container, CancellationToken ct = default)
    {
        var request = _request ?? throw new InvalidOperationException("Recording is not configured.");
        if (!request.ReplayEnabled) throw new InvalidOperationException("The replay buffer is turned off. Enable it to save clips.");
        using var snapshot = Buffer.Snapshot(seconds)
            ?? throw new InvalidOperationException(State == EngineState.Running
                ? "The replay buffer is still empty. Try again in a moment."
                : "The replay buffer is not running" + (_error != null ? ": " + _error : "."));
        string baseName = $"Clip {DateTime.Now:yyyy-MM-dd HH-mm-ss}";
        return await ClipWriter.SaveAsync(snapshot, directory, baseName, container, request.TrackTitles, ct).ConfigureAwait(false);
    }

    // ---------------------------------------------------------------- manual recording

    public async Task StartManualRecordingAsync(string directory, ContainerFormat container)
    {
        await _control.WaitAsync().ConfigureAwait(false);
        try
        {
            if (_sink != null) return;
            var request = _request ?? throw new InvalidOperationException("Recording is not configured.");
            long firstChunk = Buffer.OpenChunkId ?? long.MaxValue;
            var sink = new RecordingSink(directory, $"Recording {DateTime.Now:yyyy-MM-dd HH-mm-ss}", container, request.TrackTitles,
                firstChunk == long.MaxValue ? 0 : firstChunk);
            sink.Failed += reason =>
            {
                ManualRecordingFailed?.Invoke(reason);
                _ = StopManualRecordingAsync();
            };
            Buffer.ChunkSealed += sink.OnChunkSealed;
            _sink = sink;
            if (_supervisor == null)
            {
                // Replay buffer is off: run the capture only for this recording.
                Buffer.MaxDurationSeconds = 2;
                StartSupervisor();
            }
            Log.Info("Engine", "Manual recording started");
        }
        finally { _control.Release(); }
    }

    public async Task<IReadOnlyList<ClipSaveResult>> StopManualRecordingAsync()
    {
        await _control.WaitAsync().ConfigureAwait(false);
        try
        {
            return await StopManualRecordingCoreAsync().ConfigureAwait(false);
        }
        finally { _control.Release(); }
    }

    private async Task<IReadOnlyList<ClipSaveResult>> StopManualRecordingCoreAsync()
    {
        var sink = _sink;
        if (sink == null) return Array.Empty<ClipSaveResult>();
        long? lastChunk = _supervisor != null ? Buffer.OpenChunkId : null;
        bool stopCapture = _request is { ReplayEnabled: false };
        if (stopCapture)
        {
            // Stopping ffmpeg flushes and seals the last chunk, so the sink receives everything.
            await StopSupervisorAsync().ConfigureAwait(false);
        }
        var results = await sink.StopAsync(lastChunk, TimeSpan.FromSeconds(5)).ConfigureAwait(false);
        Buffer.ChunkSealed -= sink.OnChunkSealed;
        _sink = null;
        await sink.DisposeAsync().ConfigureAwait(false);
        Log.Info("Engine", $"Manual recording stopped: {string.Join(", ", results.Select(r => r.FilePath))}");
        return results;
    }

    public async ValueTask DisposeAsync()
    {
        await StopAsync().ConfigureAwait(false);
        Buffer.Dispose();
    }
}
