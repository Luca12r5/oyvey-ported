using System.Windows.Input;
using Duelcliping.App.Controls;
using Duelcliping.App.Interop;
using Duelcliping.App.Services;
using Duelcliping.Core.Performance;

namespace Duelcliping.App.ViewModels;

public sealed record BenchmarkRow(string Metric, string Off, string On, string Delta);

public sealed class PerformanceViewModel : ObservableObject
{
    private readonly AppController _app;
    private PerformanceSnapshot? _snapshot;
    private CancellationTokenSource? _cts;
    private bool _running;
    private string _benchmarkStatus = "";
    private IReadOnlyList<BenchmarkRow> _rows = Array.Empty<BenchmarkRow>();
    private int _phaseSeconds = 30;
    private string? _reportPath;

    public PerformanceViewModel(AppController app)
    {
        _app = app;
        StartBenchmarkCommand = new AsyncCommand(RunBenchmarkAsync, () => !IsRunning);
        CancelBenchmarkCommand = new RelayCommand(() => _cts?.Cancel(), () => IsRunning);
        OpenReportCommand = new RelayCommand(() => ShellHelpers.RevealInExplorer(_reportPath!), () => _reportPath != null);
    }

    public ICommand StartBenchmarkCommand { get; }
    public ICommand CancelBenchmarkCommand { get; }
    public ICommand OpenReportCommand { get; }

    public void Update(PerformanceSnapshot s)
    {
        _snapshot = s;
        OnPropertyChanged(string.Empty);
    }

    private static string Pct(double? v) => v is double d ? $"{d:0.0}%" : "n/a";

    public string AppCpu => _snapshot == null ? "—" : Pct(_snapshot.AppCpuPercent);
    public string AppRam => _snapshot == null ? "—" : Format.Bytes(_snapshot.AppPrivateBytes);
    public string AppGpu => Pct(_snapshot?.AppGpuPercent);
    public string EncoderEngine => Pct(_snapshot?.EncoderEnginePercent);
    public string SystemCpu => Pct(_snapshot?.SystemCpuPercent);
    public string SystemGpu => Pct(_snapshot?.SystemGpuPercent);
    public string CaptureFps => _snapshot?.Engine is { } e && e.CaptureFps > 0 ? $"{e.CaptureFps:0.0} / {e.FrameRate}" : "—";
    public string Dropped => _snapshot?.Engine is { } e ? $"{e.DroppedFrames} dropped · {e.DuplicatedFrames} repeated" : "—";
    public string Backlog => _snapshot?.Engine is { } e ? $"{e.BacklogMs:0} ms" : "—";
    public string EncoderSpeed => _snapshot?.Engine is { } e && e.EncoderSpeed > 0 ? $"{e.EncoderSpeed:0.00}×" : "—";
    public string Throughput => _snapshot?.Engine is { } e ? $"{e.IncomingMbps:0.0} Mbps" : "—";
    public string BufferMemory => _snapshot?.Engine is { } e ? Format.Bytes(e.BufferedBytes) : "—";
    public string DiskWrite => _snapshot?.LastDiskWriteMBps is double d ? $"{d:0} MB/s (last clip)" : "no clip saved yet";
    public string Pipeline => _snapshot?.Engine.PipelineDescription ?? "Not recording";
    public string Uptime => _snapshot?.Engine.RunningSinceUtc is DateTime t ? AppController.FormatDuration((DateTime.UtcNow - t).TotalSeconds) : "—";
    public string Silence => _snapshot?.Engine is { } e ? $"{e.SilenceFramesInserted / 48000.0:0.0} s padded" : "—";

    public bool IsRunning { get => _running; private set => Set(ref _running, value); }
    public string BenchmarkStatus { get => _benchmarkStatus; private set => Set(ref _benchmarkStatus, value); }
    public IReadOnlyList<BenchmarkRow> Rows { get => _rows; private set => Set(ref _rows, value); }
    public bool HasResults => Rows.Count > 0;
    public bool Phase30 { get => _phaseSeconds == 30; set { if (value) { _phaseSeconds = 30; OnPropertyChanged(nameof(Phase60)); } } }
    public bool Phase60 { get => _phaseSeconds == 60; set { if (value) { _phaseSeconds = 60; OnPropertyChanged(nameof(Phase30)); } } }
    public string PresentMonState => string.IsNullOrEmpty(_app.Settings.Current.PresentMonPath)
        ? "PresentMon is not configured (Settings → Performance). Without it, in-game FPS and 1% lows are not measured; CPU, GPU and memory still are."
        : "In-game FPS, 1% lows and frame-time consistency are measured with PresentMon for the game you switch to.";

    private async Task RunBenchmarkAsync()
    {
        IsRunning = true;
        Rows = Array.Empty<BenchmarkRow>();
        _reportPath = null;
        _cts = new CancellationTokenSource();
        try
        {
            for (int i = 10; i > 0; i--)
            {
                BenchmarkStatus = $"Switch to your game now – measuring starts in {i} s…";
                await Task.Delay(1000, _cts.Token);
            }
            string? game = null;
            if (NativeMethods.ForegroundProcessId() is int pid && pid != Environment.ProcessId)
            {
                try { using var p = System.Diagnostics.Process.GetProcessById(pid); game = p.ProcessName + ".exe"; }
                catch (ArgumentException) { }
            }
            var runner = new BenchmarkRunner(_app.Performance, _app.Engine, async enabled =>
            {
                var request = _app.Engine.Request;
                if (request != null && request.ReplayEnabled != enabled) await _app.Engine.ApplyAsync(request with { ReplayEnabled = enabled });
            });
            var options = new BenchmarkOptions(TimeSpan.FromSeconds(_phaseSeconds),
                string.IsNullOrEmpty(_app.Settings.Current.PresentMonPath) ? null : _app.Settings.Current.PresentMonPath, game);
            var report = await runner.RunAsync(options, System.IO.Path.Combine(_app.DataDirectory, "benchmarks"),
                new Progress<string>(s => BenchmarkStatus = s), _cts.Token);
            // Restore exactly what the user configured.
            await _app.ApplyCaptureAsync();
            _reportPath = report.ReportPath;
            Rows = BuildRows(report);
            BenchmarkStatus = $"Done{(game != null ? $" · measured {game}" : "")}. " +
                              (report.RecordingOn.GameError ?? "") + (report.ReportPath != null ? " Report saved." : "");
        }
        catch (OperationCanceledException)
        {
            BenchmarkStatus = "Benchmark cancelled.";
            await _app.ApplyCaptureAsync();
        }
        catch (Exception ex)
        {
            BenchmarkStatus = "Benchmark failed: " + ex.Message;
            await _app.ApplyCaptureAsync();
        }
        finally
        {
            IsRunning = false;
            OnPropertyChanged(nameof(HasResults));
            CommandManager.InvalidateRequerySuggested();
        }
    }

    private static IReadOnlyList<BenchmarkRow> BuildRows(BenchmarkReport r)
    {
        var off = r.RecordingOff;
        var on = r.RecordingOn;
        string D(double? a, double? b, string unit = "") => a is double x && b is double y ? $"{y - x:+0.0;-0.0;0.0}{unit}" : "—";
        string V(double? v, string fmt = "0.0", string unit = "") => v is double d ? d.ToString(fmt) + unit : "n/a";
        var rows = new List<BenchmarkRow>();
        if (off.Game != null || on.Game != null)
        {
            rows.Add(new("Game average FPS", V(off.Game?.AverageFps), V(on.Game?.AverageFps),
                r.FpsDeltaPercent is double f ? $"{f:+0.0;-0.0;0.0}%" : "—"));
            rows.Add(new("Game 1% low FPS", V(off.Game?.OnePercentLowFps), V(on.Game?.OnePercentLowFps),
                r.OnePercentLowDeltaPercent is double l ? $"{l:+0.0;-0.0;0.0}%" : "—"));
            rows.Add(new("Frame time std. dev.", V(off.Game?.FrameTimeStdDevMs, "0.00", " ms"), V(on.Game?.FrameTimeStdDevMs, "0.00", " ms"),
                D(off.Game?.FrameTimeStdDevMs, on.Game?.FrameTimeStdDevMs, " ms")));
        }
        rows.Add(new("System CPU", V(off.SystemCpuPercent, unit: "%"), V(on.SystemCpuPercent, unit: "%"), D(off.SystemCpuPercent, on.SystemCpuPercent, " pt")));
        rows.Add(new("System GPU", V(off.SystemGpuPercent, unit: "%"), V(on.SystemGpuPercent, unit: "%"), D(off.SystemGpuPercent, on.SystemGpuPercent, " pt")));
        rows.Add(new("Duelcliping CPU", V(off.AppCpuPercent, unit: "%"), V(on.AppCpuPercent, unit: "%"), D(off.AppCpuPercent, on.AppCpuPercent, " pt")));
        rows.Add(new("Duelcliping GPU", V(off.AppGpuPercent, unit: "%"), V(on.AppGpuPercent, unit: "%"), D(off.AppGpuPercent, on.AppGpuPercent, " pt")));
        rows.Add(new("Video encoder engine", V(off.EncoderEnginePercent, unit: "%"), V(on.EncoderEnginePercent, unit: "%"), "—"));
        rows.Add(new("Duelcliping memory", Format.Bytes(off.PeakAppMemoryBytes), Format.Bytes(on.PeakAppMemoryBytes),
            Format.Bytes(Math.Max(0, on.PeakAppMemoryBytes - off.PeakAppMemoryBytes))));
        rows.Add(new("Recording FPS", "—", $"{on.CaptureFps:0.0}", ""));
        rows.Add(new("Dropped frames", "—", on.DroppedFrames.ToString(), ""));
        rows.Add(new("Max encoder backlog", "—", $"{on.MaxBacklogMs:0} ms", ""));
        return rows;
    }
}
