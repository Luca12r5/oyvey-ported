using System.Text.Json;
using System.Text.Json.Serialization;
using Duelcliping.Core.Logging;

namespace Duelcliping.Core.Settings;

/// <summary>
/// Loads and persists <see cref="AppSettings"/>. Writes are atomic (temp file + replace) so a crash
/// or power loss mid-save can never leave a truncated settings file behind.
/// </summary>
public sealed class SettingsManager
{
    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        WriteIndented = true,
        Converters = { new JsonStringEnumConverter() },
        ReadCommentHandling = JsonCommentHandling.Skip,
        AllowTrailingCommas = true,
    };

    private readonly object _gate = new();
    private AppSettings _current;

    public SettingsManager(string filePath)
    {
        FilePath = filePath;
        _current = new AppSettings();
        _current.Normalize();
    }

    public string FilePath { get; }

    /// <summary>A snapshot of the active settings. Treat as read-only; use <see cref="Update"/> to change.</summary>
    public AppSettings Current
    {
        get { lock (_gate) return _current; }
    }

    /// <summary>Raised after settings changed and were persisted. Argument is (old, new).</summary>
    public event Action<AppSettings, AppSettings>? Changed;

    public static string DefaultPath() => Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "Duelcliping", "settings.json");

    public void Load()
    {
        AppSettings loaded;
        try
        {
            if (File.Exists(FilePath))
            {
                var json = File.ReadAllText(FilePath);
                loaded = JsonSerializer.Deserialize<AppSettings>(json, JsonOptions) ?? new AppSettings();
                Log.Info("Settings", $"Loaded settings from {FilePath}");
            }
            else
            {
                loaded = new AppSettings();
                Log.Info("Settings", "No settings file found; using defaults");
            }
        }
        catch (Exception ex) when (ex is JsonException or IOException or UnauthorizedAccessException or NotSupportedException)
        {
            Log.Error("Settings", $"Settings file is unreadable; backing it up and using defaults", ex);
            TryBackupCorrupt();
            loaded = new AppSettings();
        }

        loaded.Normalize();
        lock (_gate) _current = loaded;
        Save(loaded);
    }

    /// <summary>Applies a mutation to a copy of the current settings, persists it and notifies listeners.</summary>
    public AppSettings Update(Action<AppSettings> mutate)
    {
        AppSettings old, updated;
        lock (_gate)
        {
            old = _current;
            updated = _current.Clone();
            mutate(updated);
            updated.Normalize();
            _current = updated;
        }
        Save(updated);
        Changed?.Invoke(old, updated);
        return updated;
    }

    private void Save(AppSettings settings)
    {
        try
        {
            Directory.CreateDirectory(Path.GetDirectoryName(FilePath)!);
            var tmp = FilePath + ".tmp";
            var bytes = JsonSerializer.SerializeToUtf8Bytes(settings, JsonOptions);
            using (var fs = new FileStream(tmp, FileMode.Create, FileAccess.Write, FileShare.None))
            {
                fs.Write(bytes);
                fs.Flush(flushToDisk: true);
            }
            File.Move(tmp, FilePath, overwrite: true);
        }
        catch (Exception ex)
        {
            Log.Error("Settings", "Failed to save settings", ex);
            throw new IOException($"Settings could not be saved to {FilePath}: {ex.Message}", ex);
        }
    }

    private void TryBackupCorrupt()
    {
        try
        {
            if (File.Exists(FilePath))
                File.Copy(FilePath, FilePath + $".corrupt-{DateTime.Now:yyyyMMddHHmmss}", overwrite: true);
        }
        catch (Exception ex)
        {
            Log.Warn("Settings", "Could not back up corrupt settings file: " + ex.Message);
        }
    }
}
