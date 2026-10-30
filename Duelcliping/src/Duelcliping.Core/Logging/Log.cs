using System.Collections.Concurrent;
using System.Text;

namespace Duelcliping.Core.Logging;

public enum LogLevel { Debug, Info, Warning, Error }

/// <summary>
/// Minimal, allocation-light file logger. Messages are queued and flushed by a single
/// background thread that sleeps until there is work, so logging never blocks the
/// recording hot path on disk I/O.
/// </summary>
public static class Log
{
    private const long MaxFileBytes = 5 * 1024 * 1024;
    private const int KeptFiles = 5;

    private static readonly BlockingCollection<string> Queue = new(new ConcurrentQueue<string>(), 10_000);
    private static Thread? _writer;
    private static string? _directory;
    private static StreamWriter? _stream;
    private static long _written;

    public static LogLevel MinimumLevel { get; set; } = LogLevel.Info;
    public static string? CurrentFile { get; private set; }
    public static string? Directory => _directory;

    /// <summary>Raised for warnings and errors so the UI can surface them.</summary>
    public static event Action<LogLevel, string>? Important;

    public static void Initialize(string directory)
    {
        if (_writer != null) return;
        _directory = directory;
        System.IO.Directory.CreateDirectory(directory);
        Rotate();
        _writer = new Thread(WriterLoop) { IsBackground = true, Name = "Moments.Log", Priority = ThreadPriority.BelowNormal };
        _writer.Start();
        Info("Log", $"Moments log started ({Environment.OSVersion}, .NET {Environment.Version}, {Environment.ProcessorCount} logical CPUs)");
    }

    public static void Debug(string source, string message) => Write(LogLevel.Debug, source, message);
    public static void Info(string source, string message) => Write(LogLevel.Info, source, message);
    public static void Warn(string source, string message) => Write(LogLevel.Warning, source, message);

    public static void Error(string source, string message, Exception? ex = null) =>
        Write(LogLevel.Error, source, ex == null ? message : $"{message}: {ex}");

    public static void Write(LogLevel level, string source, string message)
    {
        if (level >= LogLevel.Warning) Important?.Invoke(level, message);
        if (level < MinimumLevel) return;
        var line = $"{DateTime.Now:yyyy-MM-dd HH:mm:ss.fff} [{LevelTag(level)}] {source}: {message}";
        if (_writer == null)
        {
            Console.Error.WriteLine(line);
            return;
        }
        Queue.TryAdd(line);
    }

    public static void Flush(TimeSpan timeout)
    {
        var until = DateTime.UtcNow + timeout;
        while (Queue.Count > 0 && DateTime.UtcNow < until) Thread.Sleep(10);
        lock (Queue) _stream?.Flush();
    }

    private static string LevelTag(LogLevel level) => level switch
    {
        LogLevel.Debug => "DBG",
        LogLevel.Info => "INF",
        LogLevel.Warning => "WRN",
        _ => "ERR",
    };

    private static void WriterLoop()
    {
        foreach (var line in Queue.GetConsumingEnumerable())
        {
            try
            {
                lock (Queue)
                {
                    if (_stream == null) continue;
                    _stream.WriteLine(line);
                    _written += line.Length + 2;
                    if (Queue.Count == 0) _stream.Flush();
                    if (_written > MaxFileBytes) Rotate();
                }
            }
            catch
            {
                // Logging must never take the application down.
            }
        }
    }

    private static void Rotate()
    {
        try
        {
            _stream?.Dispose();
            var dir = _directory!;
            var files = new DirectoryInfo(dir).GetFiles("moments-*.log").OrderByDescending(f => f.CreationTimeUtc).ToList();
            foreach (var old in files.Skip(KeptFiles - 1))
            {
                try { old.Delete(); } catch { /* in use or locked; ignore */ }
            }
            CurrentFile = Path.Combine(dir, $"moments-{DateTime.Now:yyyyMMdd-HHmmss}.log");
            _stream = new StreamWriter(new FileStream(CurrentFile, FileMode.Append, FileAccess.Write, FileShare.ReadWrite), new UTF8Encoding(false));
            _written = 0;
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine("Log rotation failed: " + ex.Message);
            _stream = null;
        }
    }
}
