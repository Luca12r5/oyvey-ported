using System.Globalization;
using System.Text.Json;
using Duelcliping.Core.Logging;
using Duelcliping.Core.Media;
using Duelcliping.Core.Recording;
using Duelcliping.Core.Settings;

namespace Duelcliping.Core.Library;

public sealed class ClipMetadata
{
    public long SizeBytes { get; set; }
    public DateTime LastWriteUtc { get; set; }
    public DateTime CreatedUtc { get; set; }
    public double DurationSeconds { get; set; }
    public int Width { get; set; }
    public int Height { get; set; }
    public double FrameRate { get; set; }
    public string VideoCodec { get; set; } = "";
    public int AudioTracks { get; set; }
    public bool IsPinned { get; set; }
    public bool ProbeFailed { get; set; }
}

public sealed record ClipItem(string FilePath, ClipMetadata Meta)
{
    public string FileName => Path.GetFileName(FilePath);
    public string Title => Path.GetFileNameWithoutExtension(FilePath);
    public bool IsRecording => Title.StartsWith("Recording ", StringComparison.OrdinalIgnoreCase);
    public string Resolution => Meta.Height > 0 ? $"{Meta.Width}×{Meta.Height}" : "—";
}

public enum ClipSort { Newest, Oldest, Longest, Shortest, Largest, Name }

/// <summary>
/// The clip library: a folder of video files plus a small JSON index with probed metadata and
/// cached thumbnails (in a hidden <c>.duelcliping</c> sub folder). Files added or removed by hand
/// are picked up by <see cref="ScanAsync"/>.
/// </summary>
public sealed class ClipLibrary
{
    public static readonly string[] VideoExtensions = { ".mp4", ".mkv", ".ts", ".mov" };
    private static readonly JsonSerializerOptions JsonOptions = new() { WriteIndented = false };

    private readonly object _lock = new();
    private readonly SemaphoreSlim _probeGate = new(1, 1);
    private Dictionary<string, ClipMetadata> _index = new(StringComparer.OrdinalIgnoreCase);
    private string _directory;

    public ClipLibrary(string directory)
    {
        _directory = directory;
        LoadIndex();
    }

    public string Directory => _directory;
    public string DataDirectory => Path.Combine(_directory, ".duelcliping");
    public string ThumbnailDirectory => Path.Combine(DataDirectory, "thumbs");
    private string IndexPath => Path.Combine(DataDirectory, "index.json");

    /// <summary>Deletes a file. Replaced by the app with a recycle-bin aware implementation.</summary>
    public Action<string> DeleteFile { get; set; } = File.Delete;

    public event Action? Changed;

    public void ChangeDirectory(string directory)
    {
        lock (_lock)
        {
            _directory = directory;
            _index = new(StringComparer.OrdinalIgnoreCase);
        }
        LoadIndex();
        Changed?.Invoke();
    }

    /// <summary>Lists clips, probing new or changed files. Probing is serialized and low priority.</summary>
    public async Task<IReadOnlyList<ClipItem>> ScanAsync(CancellationToken ct = default)
    {
        System.IO.Directory.CreateDirectory(_directory);
        var files = new DirectoryInfo(_directory).EnumerateFiles()
            .Where(f => VideoExtensions.Contains(f.Extension, StringComparer.OrdinalIgnoreCase))
            .ToList();

        var result = new List<ClipItem>(files.Count);
        bool dirty = false;
        foreach (var file in files)
        {
            ct.ThrowIfCancellationRequested();
            ClipMetadata? meta;
            lock (_lock) _index.TryGetValue(file.Name, out meta);
            if (meta == null || meta.SizeBytes != file.Length || meta.LastWriteUtc != file.LastWriteTimeUtc)
            {
                var pinned = meta?.IsPinned ?? false;
                meta = await ProbeAsync(file, ct).ConfigureAwait(false);
                meta.IsPinned = pinned;
                lock (_lock) _index[file.Name] = meta;
                dirty = true;
            }
            result.Add(new ClipItem(file.FullName, meta));
        }

        lock (_lock)
        {
            var existing = new HashSet<string>(files.Select(f => f.Name), StringComparer.OrdinalIgnoreCase);
            foreach (var stale in _index.Keys.Where(k => !existing.Contains(k)).ToList())
            {
                _index.Remove(stale);
                dirty = true;
            }
        }
        if (dirty) SaveIndex();
        return result;
    }

    public static IEnumerable<ClipItem> Sort(IEnumerable<ClipItem> clips, ClipSort sort) => sort switch
    {
        ClipSort.Oldest => clips.OrderBy(c => c.Meta.CreatedUtc),
        ClipSort.Longest => clips.OrderByDescending(c => c.Meta.DurationSeconds),
        ClipSort.Shortest => clips.OrderBy(c => c.Meta.DurationSeconds),
        ClipSort.Largest => clips.OrderByDescending(c => c.Meta.SizeBytes),
        ClipSort.Name => clips.OrderBy(c => c.Title, StringComparer.CurrentCultureIgnoreCase),
        _ => clips.OrderByDescending(c => c.Meta.CreatedUtc),
    };

    public static IEnumerable<ClipItem> Filter(IEnumerable<ClipItem> clips, string? query)
    {
        if (string.IsNullOrWhiteSpace(query)) return clips;
        var terms = query.Split(' ', StringSplitOptions.RemoveEmptyEntries);
        return clips.Where(c => terms.All(t =>
            c.Title.Contains(t, StringComparison.CurrentCultureIgnoreCase) ||
            c.Resolution.Contains(t, StringComparison.OrdinalIgnoreCase) ||
            c.Meta.CreatedUtc.ToLocalTime().ToString("d", CultureInfo.CurrentCulture).Contains(t, StringComparison.OrdinalIgnoreCase)));
    }

    private async Task<ClipMetadata> ProbeAsync(FileInfo file, CancellationToken ct)
    {
        var meta = new ClipMetadata
        {
            SizeBytes = file.Length,
            LastWriteUtc = file.LastWriteTimeUtc,
            CreatedUtc = file.CreationTimeUtc < file.LastWriteTimeUtc ? file.CreationTimeUtc : file.LastWriteTimeUtc,
        };
        await _probeGate.WaitAsync(ct).ConfigureAwait(false);
        try
        {
            var probe = await MediaProbe.ProbeAsync(file.FullName, ct).ConfigureAwait(false);
            if (probe == null)
            {
                meta.ProbeFailed = true;
                return meta;
            }
            meta.DurationSeconds = probe.DurationSeconds;
            meta.Width = probe.Width;
            meta.Height = probe.Height;
            meta.FrameRate = probe.FrameRate;
            meta.VideoCodec = probe.VideoCodec;
            meta.AudioTracks = probe.AudioTracks;
        }
        finally { _probeGate.Release(); }
        return meta;
    }

    // ---------------------------------------------------------------- thumbnails

    public string ThumbnailPath(ClipItem clip) =>
        Path.Combine(ThumbnailDirectory, $"{Hash(clip.FileName)}-{clip.Meta.SizeBytes:x}.jpg");

    /// <summary>Returns the cached thumbnail, extracting it with ffmpeg on first use.</summary>
    public async Task<string?> GetThumbnailAsync(ClipItem clip, CancellationToken ct = default)
    {
        var path = ThumbnailPath(clip);
        if (File.Exists(path)) return path;
        System.IO.Directory.CreateDirectory(ThumbnailDirectory);
        MarkHidden(DataDirectory);
        double at = clip.Meta.DurationSeconds > 0 ? Math.Min(1.5, clip.Meta.DurationSeconds / 3) : 0;
        var tmp = path + ".tmp.jpg";
        var result = await FfmpegRunner.RunFfmpegAsync(new[]
        {
            "-hide_banner", "-loglevel", "error", "-y",
            "-ss", at.ToString("0.###", CultureInfo.InvariantCulture), "-i", clip.FilePath,
            "-frames:v", "1", "-vf", "scale=480:-2", "-q:v", "5", "-threads", "1", tmp,
        }, ct, TimeSpan.FromSeconds(30), System.Diagnostics.ProcessPriorityClass.Idle).ConfigureAwait(false);
        if (!result.Success || !File.Exists(tmp))
        {
            Log.Warn("Library", $"Thumbnail failed for {clip.FileName}: {result.ErrorSummary}");
            return null;
        }
        File.Move(tmp, path, overwrite: true);
        return path;
    }

    // ---------------------------------------------------------------- file operations

    public static string ValidateName(string name)
    {
        name = name.Trim();
        if (name.Length == 0) throw new ArgumentException("The name cannot be empty.");
        if (name.Length > 150) throw new ArgumentException("The name is too long (150 characters max).");
        var invalid = Path.GetInvalidFileNameChars().Concat(new[] { '<', '>', ':', '"', '/', '\\', '|', '?', '*' }).ToHashSet();
        var bad = name.Where(invalid.Contains).Distinct().ToArray();
        if (bad.Length > 0) throw new ArgumentException($"The name cannot contain: {string.Join(" ", bad)}");
        if (name.EndsWith('.') || name.StartsWith('.')) throw new ArgumentException("The name cannot start or end with a dot.");
        return name;
    }

    public ClipItem Rename(ClipItem clip, string newTitle)
    {
        newTitle = ValidateName(newTitle);
        var target = Path.Combine(Path.GetDirectoryName(clip.FilePath)!, newTitle + Path.GetExtension(clip.FilePath));
        if (string.Equals(target, clip.FilePath, StringComparison.Ordinal)) return clip;
        if (File.Exists(target) && !string.Equals(target, clip.FilePath, StringComparison.OrdinalIgnoreCase))
            throw new IOException($"A clip named \"{newTitle}\" already exists.");
        var oldThumb = ThumbnailPath(clip);
        File.Move(clip.FilePath, target);
        var renamed = clip with { FilePath = target };
        lock (_lock)
        {
            _index.Remove(clip.FileName);
            _index[renamed.FileName] = clip.Meta;
        }
        try { if (File.Exists(oldThumb)) File.Move(oldThumb, ThumbnailPath(renamed), overwrite: true); }
        catch (IOException) { /* thumbnail will be regenerated */ }
        SaveIndex();
        Log.Info("Library", $"Renamed {clip.FileName} -> {renamed.FileName}");
        Changed?.Invoke();
        return renamed;
    }

    public void Delete(ClipItem clip)
    {
        DeleteFile(clip.FilePath);
        try { var thumb = ThumbnailPath(clip); if (File.Exists(thumb)) File.Delete(thumb); }
        catch (IOException) { }
        lock (_lock) _index.Remove(clip.FileName);
        SaveIndex();
        Log.Info("Library", $"Deleted {clip.FileName}");
        Changed?.Invoke();
    }

    public void SetPinned(ClipItem clip, bool pinned)
    {
        clip.Meta.IsPinned = pinned;
        lock (_lock) _index[clip.FileName] = clip.Meta;
        SaveIndex();
    }

    public void NotifyChanged() => Changed?.Invoke();

    /// <summary>Remuxes recordings interrupted by a crash or power loss (left as <c>.ts.partial</c>).</summary>
    public async Task<IReadOnlyList<ClipSaveResult>> RecoverInterruptedAsync(ContainerFormat container, CancellationToken ct = default)
    {
        var recovered = new List<ClipSaveResult>();
        if (!System.IO.Directory.Exists(_directory)) return recovered;
        foreach (var stale in System.IO.Directory.GetFiles(_directory, "*.partial").Where(f => !f.EndsWith(ClipWriter.PartialSuffix)))
            ClipWriter.TryDelete(stale); // half-written remux outputs; the .ts.partial source is still there
        foreach (var ts in System.IO.Directory.GetFiles(_directory, "*" + ClipWriter.PartialSuffix))
        {
            ct.ThrowIfCancellationRequested();
            if (new FileInfo(ts).Length == 0) { ClipWriter.TryDelete(ts); continue; }
            var baseName = Path.GetFileName(ts)[..^ClipWriter.PartialSuffix.Length];
            var final = ClipWriter.UniquePath(_directory, baseName + " (recovered)", ClipWriter.Extension(container));
            Log.Warn("Library", $"Recovering interrupted recording {ts}");
            recovered.Add(await ClipWriter.FinalizeAsync(ts, final, container, Array.Empty<string>(), ct).ConfigureAwait(false));
        }
        if (recovered.Count > 0) Changed?.Invoke();
        return recovered;
    }

    private void LoadIndex()
    {
        try
        {
            if (!File.Exists(IndexPath)) return;
            var loaded = JsonSerializer.Deserialize<Dictionary<string, ClipMetadata>>(File.ReadAllText(IndexPath), JsonOptions);
            if (loaded != null) lock (_lock) _index = new(loaded, StringComparer.OrdinalIgnoreCase);
        }
        catch (Exception ex)
        {
            Log.Warn("Library", "Library index unreadable; it will be rebuilt: " + ex.Message);
        }
    }

    private void SaveIndex()
    {
        try
        {
            System.IO.Directory.CreateDirectory(DataDirectory);
            MarkHidden(DataDirectory);
            string json;
            lock (_lock) json = JsonSerializer.Serialize(_index, JsonOptions);
            var tmp = IndexPath + ".tmp";
            File.WriteAllText(tmp, json);
            File.Move(tmp, IndexPath, overwrite: true);
        }
        catch (Exception ex)
        {
            Log.Warn("Library", "Could not save library index: " + ex.Message);
        }
    }

    private static void MarkHidden(string dir)
    {
        if (!OperatingSystem.IsWindows()) return;
        try
        {
            var info = new DirectoryInfo(dir);
            if (info.Exists && !info.Attributes.HasFlag(FileAttributes.Hidden)) info.Attributes |= FileAttributes.Hidden;
        }
        catch (IOException) { }
    }

    private static string Hash(string value)
    {
        var bytes = System.Security.Cryptography.SHA1.HashData(System.Text.Encoding.UTF8.GetBytes(value.ToLowerInvariant()));
        return Convert.ToHexString(bytes, 0, 8).ToLowerInvariant();
    }
}
