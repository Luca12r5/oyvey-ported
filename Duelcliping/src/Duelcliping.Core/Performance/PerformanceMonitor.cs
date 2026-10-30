using System.Diagnostics;
using Duelcliping.Core.Recording;

namespace Duelcliping.Core.Performance;

/// <summary>Platform specific system counters (implemented with Windows APIs by the app).</summary>
public interface ISystemMetrics
{
    /// <summary>Total CPU utilisation of the machine since the previous call, 0..100.</summary>
    double? SystemCpuPercent();
    /// <summary>GPU utilisation (busiest engine, like Task Manager), optionally limited to the given processes.</summary>
    double? GpuPercent(IReadOnlyCollection<int>? processIds);
    /// <summary>Video encode engine utilisation of the given processes, if the driver reports it.</summary>
    double? GpuEncoderPercent(IReadOnlyCollection<int> processIds);
}

public sealed class NullSystemMetrics : ISystemMetrics
{
    public double? SystemCpuPercent() => null;
    public double? GpuPercent(IReadOnlyCollection<int>? processIds) => null;
    public double? GpuEncoderPercent(IReadOnlyCollection<int> processIds) => null;
}

/// <summary>CPU time and memory of a set of processes, measured from OS counters.</summary>
public sealed class ProcessMeter
{
    private readonly Dictionary<int, TimeSpan> _lastCpu = new();
    private DateTime _lastSample = DateTime.UtcNow;

    public (double CpuPercent, long WorkingSetBytes, long PrivateBytes) Sample(IEnumerable<int> processIds)
    {
        var now = DateTime.UtcNow;
        double wall = Math.Max((now - _lastSample).TotalMilliseconds, 1);
        _lastSample = now;
        double cpuMs = 0;
        long ws = 0, priv = 0;
        var seen = new HashSet<int>();
        foreach (var pid in processIds)
        {
            if (!seen.Add(pid)) continue;
            try
            {
                using var p = Process.GetProcessById(pid);
                var cpu = p.TotalProcessorTime;
                if (_lastCpu.TryGetValue(pid, out var previous)) cpuMs += (cpu - previous).TotalMilliseconds;
                _lastCpu[pid] = cpu;
                ws += p.WorkingSet64;
                priv += p.PrivateMemorySize64;
            }
            catch (Exception ex) when (ex is ArgumentException or InvalidOperationException or System.ComponentModel.Win32Exception)
            {
                _lastCpu.Remove(pid);
            }
        }
        foreach (var gone in _lastCpu.Keys.Where(k => !seen.Contains(k)).ToList()) _lastCpu.Remove(gone);
        return (cpuMs / wall / Environment.ProcessorCount * 100, ws, priv);
    }
}

public sealed record PerformanceSnapshot(
    DateTime TimestampUtc,
    double AppCpuPercent,
    long AppWorkingSetBytes,
    long AppPrivateBytes,
    double? SystemCpuPercent,
    double? SystemGpuPercent,
    double? AppGpuPercent,
    double? EncoderEnginePercent,
    EngineStatus Engine,
    double? LastDiskWriteMBps);

/// <summary>
/// Samples Moments's own footprint (UI process + ffmpeg) plus system counters. Sampling only
/// runs while somebody holds a lease (dashboard visible, benchmark running), so the monitor itself
/// costs nothing while you play.
/// </summary>
public sealed class PerformanceMonitor : IDisposable
{
    private readonly RecordingEngine _engine;
    private readonly ISystemMetrics _system;
    private readonly ProcessMeter _meter = new();
    private readonly object _lock = new();
    private Timer? _timer;
    private int _leases;

    public PerformanceMonitor(RecordingEngine engine, ISystemMetrics system)
    {
        _engine = engine;
        _system = system;
    }

    public TimeSpan Interval { get; set; } = TimeSpan.FromSeconds(2);
    /// <summary>GPU engine counters are comparatively expensive to read; only the Performance page and benchmark enable them.</summary>
    public bool IncludeGpu { get; set; }
    public PerformanceSnapshot? Latest { get; private set; }
    public double? LastDiskWriteMBps { get; set; }
    public event Action<PerformanceSnapshot>? Sampled;

    public IDisposable Acquire()
    {
        lock (_lock)
        {
            if (_leases++ == 0) _timer = new Timer(_ => SampleNow(), null, TimeSpan.Zero, Interval);
        }
        return new Lease(this);
    }

    private void ReleaseLease()
    {
        lock (_lock)
        {
            if (--_leases > 0) return;
            _timer?.Dispose();
            _timer = null;
        }
    }

    public PerformanceSnapshot SampleNow()
    {
        var status = _engine.GetStatus();
        var pids = new List<int> { Environment.ProcessId };
        if (status.FfmpegProcessId is int ff) pids.Add(ff);
        var (cpu, ws, priv) = _meter.Sample(pids);
        var snapshot = new PerformanceSnapshot(
            DateTime.UtcNow, cpu, ws, priv,
            _system.SystemCpuPercent(),
            IncludeGpu ? _system.GpuPercent(null) : null,
            IncludeGpu ? _system.GpuPercent(pids) : null,
            IncludeGpu && status.FfmpegProcessId is int f ? _system.GpuEncoderPercent(new[] { f }) : null,
            status,
            LastDiskWriteMBps);
        Latest = snapshot;
        Sampled?.Invoke(snapshot);
        return snapshot;
    }

    public void Dispose()
    {
        lock (_lock)
        {
            _timer?.Dispose();
            _timer = null;
            _leases = 0;
        }
    }

    private sealed class Lease : IDisposable
    {
        private PerformanceMonitor? _owner;
        public Lease(PerformanceMonitor owner) => _owner = owner;
        public void Dispose() => Interlocked.Exchange(ref _owner, null)?.ReleaseLease();
    }
}
