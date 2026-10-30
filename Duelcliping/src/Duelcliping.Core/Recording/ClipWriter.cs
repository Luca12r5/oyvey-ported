using System.Diagnostics;
using Duelcliping.Core.Encoders;
using Duelcliping.Core.Logging;
using Duelcliping.Core.Media;
using Duelcliping.Core.Settings;

namespace Duelcliping.Core.Recording;

public sealed class InsufficientDiskSpaceException : IOException
{
    public InsufficientDiskSpaceException(string message) : base(message) { }
}

public sealed record ClipSaveResult(string FilePath, double DurationSeconds, long Bytes, bool Remuxed, string? Warning, double DiskWriteMBps);

/// <summary>
/// Turns buffered transport stream into a finished clip file:
/// raw TS is written to <c>name.ts.partial</c>, remuxed (stream copy, no re-encode) into MP4/MKV,
/// and only then renamed to its final name. If remuxing fails the raw TS is kept as a playable
/// <c>.ts</c> file, so a clip is never lost silently.
/// </summary>
public static class ClipWriter
{
    public const string PartialSuffix = ".ts.partial";
    private const long SafetyMarginBytes = 200L * 1024 * 1024;

    public static string Extension(ContainerFormat container) => container == ContainerFormat.Mp4 ? ".mp4" : ".mkv";

    public static string UniquePath(string directory, string baseName, string extension)
    {
        var path = Path.Combine(directory, baseName + extension);
        for (int i = 2; File.Exists(path) || File.Exists(path + ".partial"); i++)
            path = Path.Combine(directory, $"{baseName} ({i}){extension}");
        return path;
    }

    public static void EnsureFreeSpace(string directory, long requiredBytes)
    {
        try
        {
            var root = Path.GetPathRoot(Path.GetFullPath(directory));
            if (string.IsNullOrEmpty(root)) return;
            var drive = new DriveInfo(root);
            if (drive.AvailableFreeSpace < requiredBytes + SafetyMarginBytes)
                throw new InsufficientDiskSpaceException(
                    $"Not enough disk space on {drive.Name}: {drive.AvailableFreeSpace / (1024 * 1024)} MB free, {(requiredBytes + SafetyMarginBytes) / (1024 * 1024)} MB needed.");
        }
        catch (ArgumentException) { /* network paths: cannot be checked up front */ }
    }

    public static async Task<ClipSaveResult> SaveAsync(ClipSnapshot snapshot, string directory, string baseName, ContainerFormat container,
        IReadOnlyList<string> trackTitles, CancellationToken ct = default)
    {
        Directory.CreateDirectory(directory);
        EnsureFreeSpace(directory, snapshot.TotalBytes * 2);

        var finalPath = UniquePath(directory, baseName, Extension(container));
        var tsPath = Path.ChangeExtension(finalPath, null) + PartialSuffix;

        var sw = Stopwatch.StartNew();
        await Task.Run(() =>
        {
            using var fs = new FileStream(tsPath, FileMode.CreateNew, FileAccess.Write, FileShare.Read, 1 << 20, FileOptions.SequentialScan);
            snapshot.WriteTo(fs);
            fs.Flush(flushToDisk: true);
        }, ct).ConfigureAwait(false);
        sw.Stop();
        double mbps = snapshot.TotalBytes / (1024.0 * 1024.0) / Math.Max(sw.Elapsed.TotalSeconds, 0.001);

        string? warning = snapshot.IsShorterThanRequested
            ? $"Only {snapshot.AvailableSeconds:0} s were buffered (requested {snapshot.RequestedSeconds:0} s)."
            : null;
        var result = await FinalizeAsync(tsPath, finalPath, container, trackTitles, ct).ConfigureAwait(false);
        return result with { DurationSeconds = snapshot.AvailableSeconds, Warning = Combine(warning, result.Warning), DiskWriteMBps = mbps };
    }

    /// <summary>Remuxes a finished <c>.ts.partial</c> into its final container.</summary>
    public static async Task<ClipSaveResult> FinalizeAsync(string tsPath, string finalPath, ContainerFormat container,
        IReadOnlyList<string> trackTitles, CancellationToken ct = default)
    {
        var tmpOut = finalPath + ".partial";
        var args = FfmpegCommandBuilder.BuildRemuxArgs(tsPath, tmpOut, container, trackTitles);
        args.InsertRange(args.IndexOf("-i"), new[] { "-f", "mpegts" });
        FfmpegResult remux;
        try
        {
            remux = await FfmpegRunner.RunFfmpegAsync(args, ct, TimeSpan.FromMinutes(30)).ConfigureAwait(false);
        }
        catch (FileNotFoundException ex)
        {
            remux = new FfmpegResult(-1, "", ex.Message);
        }

        if (remux.Success && File.Exists(tmpOut) && new FileInfo(tmpOut).Length > 0)
        {
            File.Move(tmpOut, finalPath, overwrite: false);
            TryDelete(tsPath);
            var bytes = new FileInfo(finalPath).Length;
            Log.Info("ClipWriter", $"Saved {finalPath} ({bytes / 1024 / 1024.0:0.0} MB)");
            return new ClipSaveResult(finalPath, 0, bytes, true, null, 0);
        }

        // Keep the raw stream: it is a valid, playable file even if the container conversion failed.
        TryDelete(tmpOut);
        var rawPath = UniquePath(Path.GetDirectoryName(finalPath)!, Path.GetFileNameWithoutExtension(finalPath), ".ts");
        File.Move(tsPath, rawPath);
        Log.Error("ClipWriter", $"Remux to {Path.GetExtension(finalPath)} failed ({remux.ErrorSummary}); kept raw stream as {rawPath}");
        return new ClipSaveResult(rawPath, 0, new FileInfo(rawPath).Length, false,
            $"The clip could not be converted to {Path.GetExtension(finalPath).TrimStart('.').ToUpperInvariant()} and was saved as a .ts file instead.", 0);
    }

    private static string? Combine(string? a, string? b) => a == null ? b : b == null ? a : a + " " + b;

    internal static void TryDelete(string path)
    {
        try { if (File.Exists(path)) File.Delete(path); }
        catch (Exception ex) { Log.Warn("ClipWriter", $"Could not delete {path}: {ex.Message}"); }
    }
}
