using System.Diagnostics;
using System.IO;
using Duelcliping.App.Interop;
using Duelcliping.Core.Logging;
using Duelcliping.Core.Performance;
using Microsoft.Win32;

namespace Duelcliping.App.Services;

/// <summary>"Start with Windows" via the per-user Run key (no admin rights, no scheduled task).</summary>
public static class StartupManager
{
    private const string RunKey = @"Software\Microsoft\Windows\CurrentVersion\Run";
    private const string ValueName = "Duelcliping";

    public static bool IsEnabled()
    {
        using var key = Registry.CurrentUser.OpenSubKey(RunKey);
        return key?.GetValue(ValueName) != null;
    }

    public static void Apply(bool enabled)
    {
        try
        {
            using var key = Registry.CurrentUser.OpenSubKey(RunKey, writable: true) ?? Registry.CurrentUser.CreateSubKey(RunKey);
            if (enabled)
            {
                var exe = Environment.ProcessPath ?? Path.Combine(AppContext.BaseDirectory, "Duelcliping.exe");
                key.SetValue(ValueName, $"\"{exe}\" --background");
            }
            else if (key.GetValue(ValueName) != null)
            {
                key.DeleteValue(ValueName, throwOnMissingValue: false);
            }
        }
        catch (Exception ex)
        {
            Log.Error("Startup", "Could not update the Windows startup entry", ex);
            throw new InvalidOperationException("Windows did not allow changing the startup entry: " + ex.Message, ex);
        }
    }
}

/// <summary>System CPU from GetSystemTimes and GPU engine utilisation from the "GPU Engine" counters (same source as Task Manager).</summary>
public sealed class WindowsSystemMetrics : ISystemMetrics, IDisposable
{
    private ulong _lastIdle, _lastKernel, _lastUser;
    private readonly Dictionary<string, PerformanceCounter> _gpuCounters = new();
    private DateTime _lastInstanceRefresh = DateTime.MinValue;
    private bool _gpuUnavailable;
    private readonly object _lock = new();

    public double? SystemCpuPercent()
    {
        if (!NativeMethods.GetSystemTimes(out var idle, out var kernel, out var user)) return null;
        ulong di = idle.Value - _lastIdle, dk = kernel.Value - _lastKernel, du = user.Value - _lastUser;
        bool first = _lastKernel == 0;
        _lastIdle = idle.Value; _lastKernel = kernel.Value; _lastUser = user.Value;
        ulong total = dk + du; // kernel time includes idle time
        if (first || total == 0) return null;
        return Math.Clamp((total - di) * 100.0 / total, 0, 100);
    }

    public double? GpuPercent(IReadOnlyCollection<int>? processIds) => Gpu(processIds, engineFilter: null);

    public double? GpuEncoderPercent(IReadOnlyCollection<int> processIds) => Gpu(processIds, "engtype_VideoEncode");

    private double? Gpu(IReadOnlyCollection<int>? pids, string? engineFilter)
    {
        lock (_lock)
        {
            if (_gpuUnavailable) return null;
            try
            {
                RefreshInstances();
                // Sum per engine type (per process filter), then take the busiest engine – Task Manager's definition.
                var perEngine = new Dictionary<string, double>();
                foreach (var (instance, counter) in _gpuCounters)
                {
                    if (engineFilter != null && !instance.Contains(engineFilter, StringComparison.OrdinalIgnoreCase)) continue;
                    if (pids != null && !pids.Any(p => instance.StartsWith($"pid_{p}_", StringComparison.Ordinal))) continue;
                    int e = instance.IndexOf("engtype_", StringComparison.Ordinal);
                    var engine = e >= 0 ? instance[e..] : instance;
                    float v;
                    try { v = counter.NextValue(); }
                    catch (InvalidOperationException) { continue; } // instance ended
                    perEngine[engine] = perEngine.GetValueOrDefault(engine) + v;
                }
                return perEngine.Count == 0 ? 0 : Math.Min(100, perEngine.Values.Max());
            }
            catch (Exception ex)
            {
                _gpuUnavailable = true;
                Log.Warn("Metrics", "GPU counters are not available on this system: " + ex.Message);
                return null;
            }
        }
    }

    private void RefreshInstances()
    {
        if (DateTime.UtcNow - _lastInstanceRefresh < TimeSpan.FromSeconds(10)) return;
        _lastInstanceRefresh = DateTime.UtcNow;
        var category = new PerformanceCounterCategory("GPU Engine");
        var names = category.GetInstanceNames().ToHashSet();
        foreach (var gone in _gpuCounters.Keys.Where(k => !names.Contains(k)).ToList())
        {
            _gpuCounters[gone].Dispose();
            _gpuCounters.Remove(gone);
        }
        foreach (var name in names.Where(n => !_gpuCounters.ContainsKey(n)))
            _gpuCounters[name] = new PerformanceCounter("GPU Engine", "Utilization Percentage", name, readOnly: true);
    }

    public void Dispose()
    {
        lock (_lock)
        {
            foreach (var c in _gpuCounters.Values) c.Dispose();
            _gpuCounters.Clear();
        }
    }
}

/// <summary>
/// Detects when a fullscreen (exclusive or borderless) application is in the foreground. Polls the
/// foreground window every 3 seconds – three cheap Win32 calls – instead of installing any hooks.
/// </summary>
public sealed class GameActivityDetector : IDisposable
{
    private readonly Timer _timer;
    private bool _active;

    public GameActivityDetector()
    {
        _timer = new Timer(_ => Poll(), null, TimeSpan.FromSeconds(2), TimeSpan.FromSeconds(3));
    }

    public bool IsGameActive => _active;
    public string? ActiveProcessName { get; private set; }
    public event Action<bool>? Changed;

    private void Poll()
    {
        try
        {
            bool active = false;
            string? name = null;
            var hwnd = NativeMethods.GetForegroundWindow();
            if (hwnd != IntPtr.Zero && hwnd != NativeMethods.GetShellWindow() && hwnd != NativeMethods.GetDesktopWindow()
                && NativeMethods.GetWindowRect(hwnd, out var rect))
            {
                var monitor = NativeMethods.MonitorFromWindow(hwnd, 2);
                var info = new NativeMethods.MONITORINFO { cbSize = System.Runtime.InteropServices.Marshal.SizeOf<NativeMethods.MONITORINFO>() };
                if (NativeMethods.GetMonitorInfo(monitor, ref info))
                {
                    var m = info.rcMonitor;
                    active = rect.Left <= m.Left && rect.Top <= m.Top && rect.Right >= m.Right && rect.Bottom >= m.Bottom;
                }
                if (active)
                {
                    NativeMethods.GetWindowThreadProcessId(hwnd, out var pid);
                    if (pid == Environment.ProcessId) active = false;
                    else
                    {
                        try { using var p = Process.GetProcessById((int)pid); name = p.ProcessName; }
                        catch (ArgumentException) { }
                        if (name is "explorer" or "ApplicationFrameHost" or "TextInputHost") active = false;
                    }
                }
            }
            ActiveProcessName = active ? name : null;
            if (active != _active)
            {
                _active = active;
                Log.Info("GameDetect", active ? $"Fullscreen application in foreground: {name}" : "No fullscreen application in foreground");
                Changed?.Invoke(active);
            }
        }
        catch (Exception ex)
        {
            Log.Warn("GameDetect", ex.Message);
        }
    }

    public void Dispose() => _timer.Dispose();
}

/// <summary>Short, soft confirmation sound synthesised in memory (no asset file, no audio library).</summary>
public static class SoundEffects
{
    private static byte[]? _saved;

    public static void PlayClipSaved()
    {
        try
        {
            _saved ??= Synthesize(new[] { (880.0, 0.06), (1318.5, 0.09) });
            using var player = new System.Media.SoundPlayer(new MemoryStream(_saved));
            player.Play();
        }
        catch (Exception ex)
        {
            Log.Warn("Sound", ex.Message);
        }
    }

    private static byte[] Synthesize((double Freq, double Seconds)[] notes)
    {
        const int rate = 44100;
        var samples = new List<short>();
        foreach (var (freq, seconds) in notes)
        {
            int n = (int)(rate * seconds);
            for (int i = 0; i < n; i++)
            {
                double env = Math.Min(1, i / (rate * 0.005)) * Math.Exp(-4.0 * i / n);
                samples.Add((short)(Math.Sin(2 * Math.PI * freq * i / rate) * env * 7000));
            }
        }
        using var ms = new MemoryStream();
        using var w = new BinaryWriter(ms);
        int dataBytes = samples.Count * 2;
        w.Write("RIFF"u8.ToArray()); w.Write(36 + dataBytes); w.Write("WAVE"u8.ToArray());
        w.Write("fmt "u8.ToArray()); w.Write(16); w.Write((short)1); w.Write((short)1); w.Write(rate); w.Write(rate * 2); w.Write((short)2); w.Write((short)16);
        w.Write("data"u8.ToArray()); w.Write(dataBytes);
        foreach (var s in samples) w.Write(s);
        return ms.ToArray();
    }
}

public static class ShellHelpers
{
    public static void RevealInExplorer(string path)
    {
        if (File.Exists(path)) Process.Start(new ProcessStartInfo("explorer.exe", $"/select,\"{path}\"") { UseShellExecute = true });
        else if (Directory.Exists(path)) Process.Start(new ProcessStartInfo("explorer.exe", $"\"{path}\"") { UseShellExecute = true });
    }

    public static void OpenWithDefaultApp(string path) => Process.Start(new ProcessStartInfo(path) { UseShellExecute = true });

    /// <summary>Deletes to the Recycle Bin so a mistaken delete can be undone.</summary>
    public static void DeleteToRecycleBin(string path) =>
        Microsoft.VisualBasic.FileIO.FileSystem.DeleteFile(path,
            Microsoft.VisualBasic.FileIO.UIOption.OnlyErrorDialogs,
            Microsoft.VisualBasic.FileIO.RecycleOption.SendToRecycleBin);
}
