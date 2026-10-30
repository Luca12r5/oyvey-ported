using System.Diagnostics;
using System.Globalization;
using System.IO.Pipes;
using Duelcliping.Core.Audio;
using Duelcliping.Core.Encoders;
using Duelcliping.Core.Logging;
using Duelcliping.Core.Media;

namespace Duelcliping.Core.Recording;

public sealed record AudioSourceSpec(string Key, string Title, Func<IAudioCaptureSource?> Factory, double Volume);

/// <summary>Live counters reported by ffmpeg's <c>-progress</c> output.</summary>
public sealed class PipelineProgress
{
    public long Frames;
    public double Fps;
    public long DroppedFrames;
    public long DuplicatedFrames;
    public double Speed;
    public long OutTimeUs;
    public long BytesReceived;
    public DateTime LastDataUtc = DateTime.UtcNow;
    public DateTime StartedUtc = DateTime.UtcNow;
    public double BacklogMs;
    public double MinLagMs = double.MaxValue;
}

/// <summary>
/// One running ffmpeg capture process together with its audio pipes. A pipeline is disposable and
/// never restarted; the <see cref="RecordingEngine"/> creates a fresh one after every failure.
/// </summary>
internal sealed class CapturePipeline : IDisposable
{
    private readonly PipelineConfig _config;
    private readonly IReadOnlyList<AudioSourceSpec> _audioSpecs;
    private readonly ReplayBuffer _buffer;
    private readonly List<NamedPipeServerStream> _pipes = new();
    private readonly List<AudioPump> _pumps = new();
    private readonly List<IAudioCaptureSource> _sources = new();
    private readonly Queue<string> _stderrTail = new();
    private readonly object _audioLock = new();
    private Process? _process;
    private Thread? _stdoutThread;
    private Thread? _stderrThread;
    private volatile bool _stopping;

    public CapturePipeline(PipelineConfig config, IReadOnlyList<AudioSourceSpec> audioSpecs, ReplayBuffer buffer)
    {
        _config = config;
        _audioSpecs = audioSpecs;
        _buffer = buffer;
    }

    public PipelineConfig Config => _config;
    public PipelineProgress Progress { get; } = new();
    public int? ProcessId => _process?.Id;
    public Process? Process => _process;

    public string ErrorSummary
    {
        get
        {
            lock (_stderrTail)
            {
                var text = string.Join(" | ", _stderrTail.TakeLast(4));
                return string.IsNullOrWhiteSpace(text) ? "ffmpeg stopped without an error message" : text;
            }
        }
    }

    public void Start()
    {
        var audioInputs = new List<AudioInput>();
        var connectTasks = new List<(Task Task, NamedPipeServerStream Pipe, AudioSourceSpec Spec)>();
        foreach (var spec in _audioSpecs)
        {
            string name = $"moments_{Environment.ProcessId}_{Guid.NewGuid():N}";
            var pipe = new NamedPipeServerStream(name, PipeDirection.Out, 1, PipeTransmissionMode.Byte, PipeOptions.Asynchronous, 0, 1 << 20);
            _pipes.Add(pipe);
            string path = OperatingSystem.IsWindows()
                ? $@"\\.\pipe\{name}"
                : "unix:" + Path.Combine(Path.GetTempPath(), "CoreFxPipe_" + name);
            audioInputs.Add(new AudioInput(path, spec.Title));
            connectTasks.Add((pipe.WaitForConnectionAsync(), pipe, spec));
        }

        var config = _config with { AudioInputs = audioInputs };
        var args = FfmpegCommandBuilder.BuildCaptureArgs(config);
        Log.Info("Pipeline", $"Starting capture: {_config.Encoder.FfmpegName}, {_config.Source}/{_config.Path}, " +
                             $"{config.OutputSize.Width}x{config.OutputSize.Height}@{_config.FrameRate}, {config.BitrateKbps} kbps, audio tracks: {audioInputs.Count}");
        Log.Debug("Pipeline", "ffmpeg " + string.Join(' ', args.Select(a => a.Contains(' ') ? $"\"{a}\"" : a)));

        var psi = FfmpegRunner.CreateStartInfo(FfmpegLocator.Ffmpeg, args);
        _process = new Process { StartInfo = psi, EnableRaisingEvents = true };
        try
        {
            _process.Start();
        }
        catch (System.ComponentModel.Win32Exception ex)
        {
            throw new FileNotFoundException("ffmpeg.exe could not be started. Reinstall Moments to restore it.", FfmpegLocator.Ffmpeg, ex);
        }
        // With a hardware encoder ffmpeg needs almost no CPU, but its capture thread must react at
        // once (a delayed frame release stalls the desktop compositor). Only a CPU encoder, which
        // needs a lot of CPU time, runs below the game.
        FfmpegRunner.TrySetPriority(_process, _config.Encoder.IsHardware ? ProcessPriorityClass.AboveNormal : ProcessPriorityClass.BelowNormal);
        // Do NOT lower the GPU priority of the capture process: with Desktop Duplication the desktop
        // compositor waits until the capturing process releases each frame. A starved capture
        // process therefore stalls presentation for every application, including the game.
        if (_config.Source == CaptureSource.DesktopDuplication) GpuPriority.TrySetHigh(_process);
        Progress.StartedUtc = DateTime.UtcNow;
        Progress.LastDataUtc = DateTime.UtcNow;

        _stdoutThread = new Thread(ReadStdout) { IsBackground = true, Name = "Moments.Stream", Priority = ThreadPriority.AboveNormal };
        _stderrThread = new Thread(ReadStderr) { IsBackground = true, Name = "Moments.FfmpegLog", Priority = ThreadPriority.BelowNormal };
        _stdoutThread.Start();
        _stderrThread.Start();

        // Each audio pump starts the moment ffmpeg opens its pipe: ffmpeg opens inputs one after
        // another and may wait for data on one before opening the next.
        foreach (var (task, pipe, spec) in connectTasks)
        {
            task.ContinueWith(t =>
            {
                if (t.IsFaulted || _stopping) return;
                StartAudio(pipe, spec);
            }, TaskScheduler.Default);
        }
    }

    private void StartAudio(NamedPipeServerStream pipe, AudioSourceSpec spec)
    {
        lock (_audioLock)
        {
            if (_stopping) return;
            var pump = new AudioPump(spec.Key, pipe, spec.Volume);
            _pumps.Add(pump);
            pump.Start();
            try
            {
                var source = spec.Factory();
                if (source == null)
                {
                    Log.Warn("Audio", $"{spec.Title}: device not available; recording silence on this track");
                    return;
                }
                _sources.Add(source);
                source.Start(pump);
                Log.Info("Audio", $"{spec.Title}: capturing from {source.Name}");
            }
            catch (Exception ex)
            {
                Log.Error("Audio", $"{spec.Title}: could not start capture; recording silence on this track", ex);
            }
        }
    }

    public void SetVolume(string key, double volume)
    {
        lock (_audioLock)
            foreach (var pump in _pumps.Where(p => p.Name == key)) pump.Volume = volume;
    }

    public IReadOnlyList<(string Key, long Silence, long Dropped, bool Faulted)> AudioHealth()
    {
        lock (_audioLock) return _pumps.Select(p => (p.Name, p.SilenceFramesInserted, p.FramesDropped, p.Faulted)).ToList();
    }

    private void ReadStdout()
    {
        var stream = _process!.StandardOutput.BaseStream;
        var buffer = new byte[TsPacket.Size * 512];
        int filled = 0;
        try
        {
            while (true)
            {
                int read = stream.Read(buffer, filled, buffer.Length - filled);
                if (read <= 0) break;
                filled += read;
                Progress.LastDataUtc = DateTime.UtcNow;
                Interlocked.Add(ref Progress.BytesReceived, read);

                int start = 0;
                // Resynchronise if the stream is not packet aligned (should never happen with ffmpeg).
                while (start < filled && buffer[start] != TsPacket.SyncByte) start++;
                int usable = (filled - start) / TsPacket.Size * TsPacket.Size;
                if (usable > 0) _buffer.Write(buffer.AsSpan(start, usable));
                int rest = filled - start - usable;
                if (rest > 0) Buffer.BlockCopy(buffer, start + usable, buffer, 0, rest);
                filled = rest;
            }
        }
        catch (Exception ex) when (ex is IOException or ObjectDisposedException)
        {
            if (!_stopping) Log.Warn("Pipeline", "Capture stream closed: " + ex.Message);
        }
        catch (Exception ex)
        {
            Log.Error("Pipeline", "Stream reader crashed", ex);
            Kill();
        }
    }

    private void ReadStderr()
    {
        try
        {
            string? line;
            var reader = _process!.StandardError;
            while ((line = reader.ReadLine()) != null)
            {
                int eq = line.IndexOf('=');
                if (eq > 0 && !line.AsSpan(0, eq).Contains(' ') && !line.StartsWith('['))
                {
                    // -progress output: "key=value" (values may be padded, e.g. "speed=   1x")
                    ParseProgress(line.AsSpan(0, eq), line.AsSpan(eq + 1).Trim());
                    continue;
                }
                if (string.IsNullOrWhiteSpace(line)) continue;
                lock (_stderrTail)
                {
                    _stderrTail.Enqueue(line.Trim());
                    while (_stderrTail.Count > 20) _stderrTail.Dequeue();
                }
                Log.Info("ffmpeg", line.Trim());
            }
        }
        catch (Exception ex) when (ex is IOException or ObjectDisposedException or InvalidOperationException) { }
    }

    private void ParseProgress(ReadOnlySpan<char> key, ReadOnlySpan<char> value)
    {
        var p = Progress;
        var inv = CultureInfo.InvariantCulture;
        switch (key)
        {
            case "frame": if (long.TryParse(value, NumberStyles.Integer, inv, out var f)) p.Frames = f; break;
            case "fps": if (double.TryParse(value, NumberStyles.Float, inv, out var fps)) p.Fps = fps; break;
            case "drop_frames": if (long.TryParse(value, NumberStyles.Integer, inv, out var d)) p.DroppedFrames = d; break;
            case "dup_frames": if (long.TryParse(value, NumberStyles.Integer, inv, out var u)) p.DuplicatedFrames = u; break;
            case "speed":
                if (double.TryParse(value.TrimEnd('x'), NumberStyles.Float, inv, out var s)) p.Speed = s;
                break;
            case "out_time_us":
                if (long.TryParse(value, NumberStyles.Integer, inv, out var t) && t > 0)
                {
                    p.OutTimeUs = t;
                    // Wall time minus media time = how far encoding lags behind capture.
                    // The minimum is the constant start-up offset; anything above it is backlog.
                    double lag = (DateTime.UtcNow - p.StartedUtc).TotalMilliseconds - t / 1000.0;
                    if (lag < p.MinLagMs) p.MinLagMs = lag;
                    p.BacklogMs = Math.Max(0, lag - p.MinLagMs);
                }
                break;
        }
    }

    public async Task<int> WaitForExitAsync(CancellationToken ct)
    {
        await _process!.WaitForExitAsync(ct).ConfigureAwait(false);
        _stdoutThread?.Join(2000);
        return _process.ExitCode;
    }

    public bool HasExited
    {
        get
        {
            try { return _process?.HasExited ?? true; }
            catch (InvalidOperationException) { return true; }
        }
    }

    /// <summary>Asks ffmpeg to finish cleanly ('q'), so the last GOP is flushed, then kills it if needed.</summary>
    public void Stop(TimeSpan grace)
    {
        _stopping = true;
        lock (_audioLock) foreach (var pump in _pumps) pump.BeginStop();
        if (_process != null && !HasExited)
        {
            try
            {
                _process.StandardInput.Write('q');
                _process.StandardInput.Flush();
            }
            catch (Exception ex) when (ex is IOException or ObjectDisposedException or InvalidOperationException) { }
            if (!_process.WaitForExit((int)grace.TotalMilliseconds)) Kill();
            _stdoutThread?.Join(2000);
        }
        StopAudio();
    }

    public void Kill()
    {
        if (_process != null) FfmpegRunner.TryKill(_process);
    }

    private void StopAudio()
    {
        lock (_audioLock)
        {
            foreach (var source in _sources)
            {
                try { source.Stop(); source.Dispose(); }
                catch (Exception ex) { Log.Warn("Audio", "Error while stopping audio source: " + ex.Message); }
            }
            _sources.Clear();
            foreach (var pump in _pumps) pump.Dispose();
            _pumps.Clear();
        }
        foreach (var pipe in _pipes)
        {
            try { pipe.Dispose(); } catch { /* already broken */ }
        }
    }

    public void Dispose()
    {
        if (!_stopping) Stop(TimeSpan.FromSeconds(2));
        _process?.Dispose();
    }
}
