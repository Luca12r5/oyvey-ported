using System.Globalization;
using Duelcliping.Core.Logging;

namespace Duelcliping.Core.Performance;

public sealed record FrameTimeStats(int Frames, double AverageFps, double OnePercentLowFps, double PointOnePercentLowFps,
    double AverageFrameTimeMs, double FrameTimeStdDevMs, double P99FrameTimeMs)
{
    /// <summary>
    /// Computes frame statistics from frame times in milliseconds. "1% low" is the frame rate
    /// equivalent of the 99th percentile frame time (the common definition used by PresentMon/CapFrameX).
    /// </summary>
    public static FrameTimeStats? From(IReadOnlyList<double> frameTimesMs)
    {
        var valid = frameTimesMs.Where(f => f > 0 && f < 5000).ToArray();
        if (valid.Length < 10) return null;
        Array.Sort(valid);
        double mean = valid.Average();
        double variance = valid.Sum(f => (f - mean) * (f - mean)) / valid.Length;
        double p99 = Percentile(valid, 0.99);
        double p999 = Percentile(valid, 0.999);
        return new FrameTimeStats(valid.Length, 1000 / mean, 1000 / p99, 1000 / p999, mean, Math.Sqrt(variance), p99);
    }

    private static double Percentile(double[] sorted, double p)
    {
        double rank = p * (sorted.Length - 1);
        int lo = (int)Math.Floor(rank), hi = (int)Math.Ceiling(rank);
        return sorted[lo] + (sorted[hi] - sorted[lo]) * (rank - lo);
    }
}

/// <summary>
/// Measures a game's frame times with Intel PresentMon (an external, free tool that reads the
/// Windows ETW present events). Duelcliping does not inject into games, so this is the only honest
/// way to measure in-game FPS. PresentMon needs administrator rights or the
/// "Performance Log Users" group.
/// </summary>
public static class PresentMonCapture
{
    public static async Task<FrameTimeStats?> CaptureAsync(string presentMonExe, string processName, TimeSpan duration, CancellationToken ct)
    {
        if (!File.Exists(presentMonExe)) throw new FileNotFoundException("PresentMon was not found.", presentMonExe);
        var csv = Path.Combine(Path.GetTempPath(), $"duelcliping-presentmon-{Guid.NewGuid():N}.csv");
        var args = new[]
        {
            "--process_name", processName,
            "--output_file", csv,
            "--timed", ((int)duration.TotalSeconds).ToString(CultureInfo.InvariantCulture),
            "--terminate_after_timed",
            "--stop_existing_session",
            "--no_console_stats",
        };
        try
        {
            var result = await Media.FfmpegRunner.RunAsync(presentMonExe, args, ct, duration + TimeSpan.FromSeconds(30),
                System.Diagnostics.ProcessPriorityClass.BelowNormal, null).ConfigureAwait(false);
            if (!File.Exists(csv))
                throw new InvalidOperationException("PresentMon produced no data: " + result.ErrorSummary +
                                                    " (run Duelcliping as administrator or join the 'Performance Log Users' group)");
            return From(await File.ReadAllLinesAsync(csv, ct).ConfigureAwait(false));
        }
        finally
        {
            try { File.Delete(csv); } catch { /* temp file */ }
        }
    }

    /// <summary>Parses PresentMon 1.x ("MsBetweenPresents") and 2.x ("FrameTime") CSV output.</summary>
    public static FrameTimeStats? From(IReadOnlyList<string> lines)
    {
        if (lines.Count < 2) return null;
        var header = lines[0].Split(',');
        int column = Array.FindIndex(header, h => h.Trim().Equals("FrameTime", StringComparison.OrdinalIgnoreCase));
        if (column < 0) column = Array.FindIndex(header, h => h.Trim().Equals("MsBetweenPresents", StringComparison.OrdinalIgnoreCase));
        if (column < 0)
        {
            Log.Warn("PresentMon", "Unknown CSV format: " + lines[0]);
            return null;
        }
        var times = new List<double>(lines.Count);
        foreach (var line in lines.Skip(1))
        {
            var cells = line.Split(',');
            if (cells.Length > column && double.TryParse(cells[column], NumberStyles.Float, CultureInfo.InvariantCulture, out var ms))
                times.Add(ms);
        }
        return FrameTimeStats.From(times);
    }
}
