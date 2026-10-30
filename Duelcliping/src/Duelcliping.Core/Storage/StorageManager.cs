using Duelcliping.Core.Library;
using Duelcliping.Core.Logging;
using Duelcliping.Core.Settings;

namespace Duelcliping.Core.Storage;

public sealed record StorageUsage(long ClipBytes, int ClipCount, long DriveFreeBytes, long DriveTotalBytes, long QuotaBytes)
{
    public double QuotaFraction => QuotaBytes <= 0 ? 0 : Math.Min(1, (double)ClipBytes / QuotaBytes);
    public bool OverQuota => ClipBytes > QuotaBytes;
}

public sealed record CleanupResult(int DeletedCount, long FreedBytes, string? Warning);

/// <summary>Reports storage use and applies the retention rules (age limit and size quota).</summary>
public sealed class StorageManager
{
    private readonly ClipLibrary _library;

    public StorageManager(ClipLibrary library) => _library = library;

    public static StorageUsage Measure(IReadOnlyList<ClipItem> clips, string directory, double quotaGb)
    {
        long free = 0, total = 0;
        try
        {
            var root = Path.GetPathRoot(Path.GetFullPath(directory));
            if (!string.IsNullOrEmpty(root))
            {
                var drive = new DriveInfo(root);
                free = drive.AvailableFreeSpace;
                total = drive.TotalSize;
            }
        }
        catch (Exception ex) when (ex is IOException or ArgumentException or UnauthorizedAccessException) { }
        return new StorageUsage(clips.Sum(c => c.Meta.SizeBytes), clips.Count, free, total, (long)(quotaGb * 1024 * 1024 * 1024));
    }

    /// <summary>
    /// Deletes clips that are older than the configured age, then the oldest clips until the library
    /// fits the quota. Pinned clips are never deleted. Without auto-delete nothing is removed; an
    /// over-quota library only produces a warning.
    /// </summary>
    public async Task<CleanupResult> EnforceAsync(AppSettings settings, CancellationToken ct = default)
    {
        var clips = await _library.ScanAsync(ct).ConfigureAwait(false);
        var usage = Measure(clips, _library.Directory, settings.MaxStorageGb);
        if (!settings.AutoDeleteOldClips)
        {
            return new CleanupResult(0, 0, usage.OverQuota
                ? $"Your clips use {usage.ClipBytes / 1073741824.0:0.0} GB, more than the {settings.MaxStorageGb:0} GB limit. Enable automatic deletion or remove clips."
                : null);
        }

        var candidates = clips.Where(c => !c.Meta.IsPinned).OrderBy(c => c.Meta.CreatedUtc).ToList();
        var toDelete = new List<ClipItem>();
        var cutoff = DateTime.UtcNow - TimeSpan.FromDays(settings.AutoDeleteAfterDays);
        toDelete.AddRange(candidates.Where(c => c.Meta.CreatedUtc < cutoff));

        long remaining = usage.ClipBytes - toDelete.Sum(c => c.Meta.SizeBytes);
        foreach (var clip in candidates.Except(toDelete))
        {
            if (remaining <= usage.QuotaBytes) break;
            toDelete.Add(clip);
            remaining -= clip.Meta.SizeBytes;
        }

        int deleted = 0;
        long freed = 0;
        foreach (var clip in toDelete)
        {
            ct.ThrowIfCancellationRequested();
            try
            {
                _library.Delete(clip);
                deleted++;
                freed += clip.Meta.SizeBytes;
            }
            catch (Exception ex) when (ex is IOException or UnauthorizedAccessException)
            {
                Log.Warn("Storage", $"Could not delete {clip.FileName}: {ex.Message}");
            }
        }
        if (deleted > 0) Log.Info("Storage", $"Retention removed {deleted} clips ({freed / 1048576.0:0} MB)");
        string? warning = remaining > usage.QuotaBytes ? "The storage limit is exceeded by pinned clips." : null;
        return new CleanupResult(deleted, freed, warning);
    }
}
