using System.Diagnostics;
using System.Text;
using Duelcliping.Core.Logging;

namespace Duelcliping.Core.Media;

/// <summary>Locates the bundled ffmpeg/ffprobe binaries.</summary>
public static class FfmpegLocator
{
    private static string? _ffmpeg;
    private static string? _ffprobe;

    public static string Ffmpeg => _ffmpeg ??= Find("ffmpeg");
    public static string Ffprobe => _ffprobe ??= Find("ffprobe");

    /// <summary>Overrides the search (used by tests and portable builds).</summary>
    public static void Configure(string ffmpegPath, string ffprobePath)
    {
        _ffmpeg = ffmpegPath;
        _ffprobe = ffprobePath;
    }

    public static bool IsAvailable => File.Exists(Ffmpeg) || Ffmpeg == ExecutableName("ffmpeg");

    private static string ExecutableName(string tool) => OperatingSystem.IsWindows() ? tool + ".exe" : tool;

    private static string Find(string tool)
    {
        var exe = ExecutableName(tool);
        var baseDir = AppContext.BaseDirectory;
        foreach (var candidate in new[]
                 {
                     Path.Combine(baseDir, "ffmpeg", exe),
                     Path.Combine(baseDir, exe),
                 })
        {
            if (File.Exists(candidate)) return candidate;
        }

        var pathVar = Environment.GetEnvironmentVariable("PATH") ?? "";
        foreach (var dir in pathVar.Split(Path.PathSeparator, StringSplitOptions.RemoveEmptyEntries))
        {
            try
            {
                var candidate = Path.Combine(dir.Trim(), exe);
                if (File.Exists(candidate)) return candidate;
            }
            catch (ArgumentException) { /* malformed PATH entry */ }
        }
        return exe;
    }
}

public sealed record FfmpegResult(int ExitCode, string StdOut, string StdErr)
{
    public bool Success => ExitCode == 0;

    /// <summary>The last few meaningful stderr lines, suitable for an error message.</summary>
    public string ErrorSummary
    {
        get
        {
            var lines = StdErr.Split('\n', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries)
                .Where(l => !l.StartsWith("frame=") && !l.StartsWith("size=")).TakeLast(4);
            var text = string.Join(" | ", lines);
            return string.IsNullOrWhiteSpace(text) ? $"ffmpeg exited with code {ExitCode}" : text;
        }
    }
}

/// <summary>Runs short-lived ffmpeg/ffprobe jobs (probing, remuxing, thumbnails, exports).</summary>
public static class FfmpegRunner
{
    public static Task<FfmpegResult> RunFfmpegAsync(IEnumerable<string> args, CancellationToken ct = default, TimeSpan? timeout = null,
        ProcessPriorityClass priority = ProcessPriorityClass.BelowNormal, Action<string>? onStdErrLine = null) =>
        RunAsync(FfmpegLocator.Ffmpeg, args, ct, timeout, priority, onStdErrLine);

    public static Task<FfmpegResult> RunFfprobeAsync(IEnumerable<string> args, CancellationToken ct = default, TimeSpan? timeout = null) =>
        RunAsync(FfmpegLocator.Ffprobe, args, ct, timeout ?? TimeSpan.FromSeconds(30), ProcessPriorityClass.BelowNormal, null);

    public static ProcessStartInfo CreateStartInfo(string exe, IEnumerable<string> args)
    {
        var psi = new ProcessStartInfo(exe)
        {
            UseShellExecute = false,
            CreateNoWindow = true,
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            RedirectStandardInput = true,
            StandardErrorEncoding = Encoding.UTF8,
        };
        foreach (var a in args) psi.ArgumentList.Add(a);
        return psi;
    }

    public static async Task<FfmpegResult> RunAsync(string exe, IEnumerable<string> args, CancellationToken ct, TimeSpan? timeout,
        ProcessPriorityClass priority, Action<string>? onStdErrLine)
    {
        var argList = args.ToList();
        using var process = new Process { StartInfo = CreateStartInfo(exe, argList) };
        var stdout = new StringBuilder();
        var stderr = new StringBuilder();
        process.OutputDataReceived += (_, e) => { if (e.Data != null) lock (stdout) stdout.AppendLine(e.Data); };
        process.ErrorDataReceived += (_, e) =>
        {
            if (e.Data == null) return;
            lock (stderr)
            {
                if (stderr.Length < 256 * 1024) stderr.AppendLine(e.Data);
            }
            onStdErrLine?.Invoke(e.Data);
        };

        try
        {
            if (!process.Start()) throw new InvalidOperationException("Process did not start");
        }
        catch (System.ComponentModel.Win32Exception ex)
        {
            throw new FileNotFoundException($"Could not start {Path.GetFileName(exe)}. Is ffmpeg installed next to Moments? ({ex.Message})", exe, ex);
        }

        TrySetPriority(process, priority);
        process.BeginOutputReadLine();
        process.BeginErrorReadLine();

        using var timeoutCts = timeout.HasValue ? new CancellationTokenSource(timeout.Value) : new CancellationTokenSource();
        using var linked = CancellationTokenSource.CreateLinkedTokenSource(ct, timeoutCts.Token);
        try
        {
            await process.WaitForExitAsync(linked.Token).ConfigureAwait(false);
        }
        catch (OperationCanceledException)
        {
            TryKill(process);
            if (ct.IsCancellationRequested) throw;
            Log.Warn("ffmpeg", $"Timed out: {Path.GetFileName(exe)} {string.Join(' ', argList)}");
            return new FfmpegResult(-1, stdout.ToString(), stderr + "\nTimed out");
        }
        process.WaitForExit(); // flush async readers
        return new FfmpegResult(process.ExitCode, stdout.ToString(), stderr.ToString());
    }

    public static void TrySetPriority(Process process, ProcessPriorityClass priority)
    {
        try { process.PriorityClass = priority; }
        catch (Exception ex) when (ex is InvalidOperationException or System.ComponentModel.Win32Exception or PlatformNotSupportedException)
        {
            // Raising priority may need privileges on Linux; lowering is best effort.
        }
    }

    public static void TryKill(Process process)
    {
        try { if (!process.HasExited) process.Kill(entireProcessTree: true); }
        catch (Exception ex) when (ex is InvalidOperationException or System.ComponentModel.Win32Exception) { }
    }

    /// <summary>Quotes a value for use inside an ffmpeg filter graph option.</summary>
    public static string EscapeFilterValue(string value) =>
        value.Replace("\\", "/").Replace(":", "\\:").Replace("'", "\\'");
}
