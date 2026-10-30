using System.Text.Json;
using Duelcliping.Core.Logging;
using Duelcliping.Core.Recording;

namespace Duelcliping.Core.Performance;

public sealed record BenchmarkOptions(TimeSpan PhaseDuration, string? PresentMonPath, string? GameProcessName);

public sealed record BenchmarkPhase(
    string Name,
    int Samples,
    double AppCpuPercent,
    double? SystemCpuPercent,
    double? SystemGpuPercent,
    double? AppGpuPercent,
    double? EncoderEnginePercent,
    long PeakAppMemoryBytes,
    double CaptureFps,
    long DroppedFrames,
    double MaxBacklogMs,
    FrameTimeStats? Game,
    string? GameError);

public sealed record BenchmarkReport(
    DateTime StartedUtc,
    string? GameProcess,
    double PhaseSeconds,
    string? Pipeline,
    BenchmarkPhase RecordingOff,
    BenchmarkPhase RecordingOn,
    string? ReportPath)
{
    public double? FpsDeltaPercent => RecordingOff.Game is { } off && RecordingOn.Game is { } on && off.AverageFps > 0
        ? (on.AverageFps - off.AverageFps) / off.AverageFps * 100 : null;
    public double? OnePercentLowDeltaPercent => RecordingOff.Game is { } off && RecordingOn.Game is { } on && off.OnePercentLowFps > 0
        ? (on.OnePercentLowFps - off.OnePercentLowFps) / off.OnePercentLowFps * 100 : null;
}

/// <summary>
/// A/B benchmark: measures the same workload with recording disabled and enabled and reports the
/// differences in CPU, GPU, memory and – if PresentMon is available – the game's FPS, 1% lows and
/// frame-time consistency. All values are measured; nothing is estimated.
/// </summary>
public sealed class BenchmarkRunner
{
    private readonly PerformanceMonitor _monitor;
    private readonly RecordingEngine _engine;
    private readonly Func<bool, Task> _setRecording;

    public BenchmarkRunner(PerformanceMonitor monitor, RecordingEngine engine, Func<bool, Task> setRecording)
    {
        _monitor = monitor;
        _engine = engine;
        _setRecording = setRecording;
    }

    public async Task<BenchmarkReport> RunAsync(BenchmarkOptions options, string reportDirectory, IProgress<string>? progress, CancellationToken ct)
    {
        var started = DateTime.UtcNow;
        bool gpu = _monitor.IncludeGpu;
        _monitor.IncludeGpu = true;
        bool wasRecording = _engine.State is EngineState.Running or EngineState.Starting or EngineState.Restarting;
        try
        {
            progress?.Report("Phase 1 of 2: recording disabled");
            await _setRecording(false).ConfigureAwait(false);
            await Task.Delay(TimeSpan.FromSeconds(3), ct).ConfigureAwait(false); // let the system settle
            var off = await MeasureAsync("Recording off", options, progress, ct).ConfigureAwait(false);

            progress?.Report("Phase 2 of 2: starting the recorder");
            await _setRecording(true).ConfigureAwait(false);
            var until = DateTime.UtcNow + TimeSpan.FromSeconds(30);
            while (_engine.State != EngineState.Running && DateTime.UtcNow < until)
                await Task.Delay(250, ct).ConfigureAwait(false);
            if (_engine.State != EngineState.Running)
                throw new InvalidOperationException("The recorder did not start: " + (_engine.GetStatus().Error ?? "unknown error"));
            await Task.Delay(TimeSpan.FromSeconds(3), ct).ConfigureAwait(false);
            var on = await MeasureAsync("Recording on", options, progress, ct).ConfigureAwait(false);

            var report = new BenchmarkReport(started, options.GameProcessName, options.PhaseDuration.TotalSeconds,
                _engine.GetStatus().PipelineDescription, off, on, null);
            var path = Save(report, reportDirectory);
            return report with { ReportPath = path };
        }
        finally
        {
            _monitor.IncludeGpu = gpu;
            await _setRecording(wasRecording).ConfigureAwait(false);
        }
    }

    private async Task<BenchmarkPhase> MeasureAsync(string name, BenchmarkOptions options, IProgress<string>? progress, CancellationToken ct)
    {
        Task<FrameTimeStats?>? game = null;
        if (!string.IsNullOrWhiteSpace(options.PresentMonPath) && !string.IsNullOrWhiteSpace(options.GameProcessName))
            game = PresentMonCapture.CaptureAsync(options.PresentMonPath!, options.GameProcessName!, options.PhaseDuration, ct);

        var samples = new List<PerformanceSnapshot>();
        _monitor.SampleNow(); // primes CPU deltas
        var startStatus = _engine.GetStatus();
        var end = DateTime.UtcNow + options.PhaseDuration;
        while (DateTime.UtcNow < end)
        {
            await Task.Delay(1000, ct).ConfigureAwait(false);
            samples.Add(_monitor.SampleNow());
            progress?.Report($"{name}: {(int)(end - DateTime.UtcNow).TotalSeconds} s left");
        }

        FrameTimeStats? stats = null;
        string? gameError = null;
        if (game != null)
        {
            try { stats = await game.ConfigureAwait(false); }
            catch (Exception ex) when (ex is not OperationCanceledException)
            {
                gameError = ex.Message;
                Log.Warn("Benchmark", "Frame time capture failed: " + ex.Message);
            }
        }
        else gameError = "PresentMon not configured; in-game FPS was not measured.";

        static double? Avg(IEnumerable<double?> values)
        {
            var list = values.Where(v => v.HasValue).Select(v => v!.Value).ToList();
            return list.Count == 0 ? null : list.Average();
        }

        var endStatus = _engine.GetStatus();
        return new BenchmarkPhase(
            name, samples.Count,
            samples.Count == 0 ? 0 : samples.Average(s => s.AppCpuPercent),
            Avg(samples.Select(s => s.SystemCpuPercent)),
            Avg(samples.Select(s => s.SystemGpuPercent)),
            Avg(samples.Select(s => s.AppGpuPercent)),
            Avg(samples.Select(s => s.EncoderEnginePercent)),
            samples.Count == 0 ? 0 : samples.Max(s => s.AppPrivateBytes),
            samples.Where(s => s.Engine.State == EngineState.Running).Select(s => s.Engine.CaptureFps).DefaultIfEmpty(0).Average(),
            Math.Max(0, endStatus.DroppedFrames - startStatus.DroppedFrames),
            samples.Select(s => s.Engine.BacklogMs).DefaultIfEmpty(0).Max(),
            stats, gameError);
    }

    private static string? Save(BenchmarkReport report, string directory)
    {
        try
        {
            Directory.CreateDirectory(directory);
            var path = Path.Combine(directory, $"benchmark-{report.StartedUtc.ToLocalTime():yyyyMMdd-HHmmss}.json");
            File.WriteAllText(path, JsonSerializer.Serialize(report, new JsonSerializerOptions { WriteIndented = true }));
            return path;
        }
        catch (Exception ex)
        {
            Log.Warn("Benchmark", "Could not save report: " + ex.Message);
            return null;
        }
    }
}
