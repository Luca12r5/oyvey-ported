using System.Threading.Channels;
using Duelcliping.Core.Logging;
using Duelcliping.Core.Settings;

namespace Duelcliping.Core.Recording;

/// <summary>
/// Manual ("full video") recording. It shares the encoder with the replay buffer: completed GOP
/// chunks are streamed to disk as they are sealed, so a manual recording costs no extra encoding
/// work at all. Data goes to <c>.ts.partial</c> files, which survive crashes and are remuxed on stop
/// (or on the next start-up). If the capture restarts with a different stream (for example after a
/// resolution change) a new part file is started instead of corrupting the current one.
/// </summary>
public sealed class RecordingSink : IAsyncDisposable
{
    private const long MinFreeBytes = 1024L * 1024 * 1024;

    private readonly Channel<GopChunk> _queue = Channel.CreateUnbounded<GopChunk>(new UnboundedChannelOptions { SingleReader = true });
    private readonly string _directory;
    private readonly string _baseName;
    private readonly ContainerFormat _container;
    private readonly IReadOnlyList<string> _trackTitles;
    private readonly long _firstChunkId;
    private readonly Task _writerTask;
    private readonly List<string> _finishedParts = new();
    private readonly List<ClipSaveResult> _results = new();
    private FileStream? _file;
    private string? _currentTsPath;
    private string? _currentFinalPath;
    private int _currentGeneration = -1;
    private long _stopAfterChunkId = long.MaxValue;
    private double _durationSeconds;
    private long _bytes;

    public RecordingSink(string directory, string baseName, ContainerFormat container, IReadOnlyList<string> trackTitles, long firstChunkId)
    {
        _directory = directory;
        _baseName = baseName;
        _container = container;
        _trackTitles = trackTitles;
        _firstChunkId = firstChunkId;
        StartedUtc = DateTime.UtcNow;
        Directory.CreateDirectory(directory);
        ClipWriter.EnsureFreeSpace(directory, MinFreeBytes);
        _writerTask = Task.Run(WriterLoop);
    }

    public DateTime StartedUtc { get; }
    public double RecordedSeconds => Volatile.Read(ref _durationSeconds);
    public long RecordedBytes => Interlocked.Read(ref _bytes);
    public bool IsStopping { get; private set; }
    public string? Error { get; private set; }

    /// <summary>Raised (on a worker thread) when the sink stops by itself, e.g. because the disk is full.</summary>
    public event Action<string>? Failed;

    /// <summary>Called by the replay buffer on its reader thread for every sealed chunk.</summary>
    public void OnChunkSealed(GopChunk chunk)
    {
        if (chunk.Id < _firstChunkId || chunk.Id > Interlocked.Read(ref _stopAfterChunkId)) return;
        if (!chunk.TryAddRef()) return;
        if (!_queue.Writer.TryWrite(chunk)) chunk.Release();
    }

    /// <summary>
    /// Requests the recording to end after the chunk that is currently being recorded
    /// (<paramref name="lastChunkId"/>), then finalizes all part files.
    /// </summary>
    public async Task<IReadOnlyList<ClipSaveResult>> StopAsync(long? lastChunkId, TimeSpan timeout)
    {
        IsStopping = true;
        Interlocked.Exchange(ref _stopAfterChunkId, lastChunkId ?? long.MinValue);
        // The chunk being recorded right now is sealed at the next keyframe (≤ 1 s); wait for it.
        var until = DateTime.UtcNow + timeout;
        while (lastChunkId.HasValue && Interlocked.Read(ref _lastWrittenId) < lastChunkId.Value
               && !_writerTask.IsCompleted && DateTime.UtcNow < until)
        {
            await Task.Delay(50).ConfigureAwait(false);
        }
        _queue.Writer.TryComplete();
        await _writerTask.ConfigureAwait(false);
        return _results;
    }

    private long _lastWrittenId = -1;
    private double _partSeconds;

    private async Task WriterLoop()
    {
        try
        {
            await foreach (var chunk in _queue.Reader.ReadAllAsync().ConfigureAwait(false))
            {
                try
                {
                    if (Error == null) WriteChunk(chunk);
                }
                finally
                {
                    chunk.Release();
                }
            }
        }
        catch (Exception ex)
        {
            Fail("Recording failed: " + ex.Message, ex);
        }
        finally
        {
            await CloseCurrentPartAsync().ConfigureAwait(false);
        }
    }

    private void WriteChunk(GopChunk chunk)
    {
        if (chunk.Generation != _currentGeneration)
        {
            // New stream (capture restarted): finish the previous part synchronously-safe and start another.
            if (_file != null) CloseCurrentPartAsync().GetAwaiter().GetResult();
            OpenPart(chunk.Generation);
            _file!.Write(chunk.PsiPackets);
        }

        chunk.WriteTo(_file!, chunk.Length);
        Interlocked.Add(ref _bytes, chunk.Length);
        double seconds = Math.Max(chunk.DurationSeconds, 0);
        Volatile.Write(ref _durationSeconds, _durationSeconds + seconds);
        _partSeconds += seconds;
        Interlocked.Exchange(ref _lastWrittenId, chunk.Id);

        if (chunk.Id % 30 == 0)
        {
            _file!.Flush(flushToDisk: false);
            try { ClipWriter.EnsureFreeSpace(_directory, MinFreeBytes); }
            catch (InsufficientDiskSpaceException ex) { Fail(ex.Message + " The recording was stopped and saved.", null); }
        }
    }

    private void OpenPart(int generation)
    {
        _currentGeneration = generation;
        string name = _finishedParts.Count == 0 ? _baseName : $"{_baseName} part {_finishedParts.Count + 1}";
        _currentFinalPath = ClipWriter.UniquePath(_directory, name, ClipWriter.Extension(_container));
        _currentTsPath = Path.ChangeExtension(_currentFinalPath, null) + ClipWriter.PartialSuffix;
        _file = new FileStream(_currentTsPath, FileMode.CreateNew, FileAccess.Write, FileShare.Read, 1 << 20, FileOptions.SequentialScan);
        Log.Info("Recording", $"Recording to {_currentTsPath}");
    }

    private async Task CloseCurrentPartAsync()
    {
        if (_file == null) return;
        var file = _file;
        _file = null;
        try
        {
            file.Flush(flushToDisk: true);
            await file.DisposeAsync().ConfigureAwait(false);
            var result = await ClipWriter.FinalizeAsync(_currentTsPath!, _currentFinalPath!, _container, _trackTitles).ConfigureAwait(false);
            _results.Add(result with { DurationSeconds = _partSeconds });
            _partSeconds = 0;
            _finishedParts.Add(result.FilePath);
        }
        catch (Exception ex)
        {
            Log.Error("Recording", $"Could not finalize {_currentTsPath}; it will be recovered on next start", ex);
        }
    }

    private void Fail(string message, Exception? ex)
    {
        if (Error != null) return;
        Error = message;
        Log.Error("Recording", message, ex);
        _queue.Writer.TryComplete();
        Failed?.Invoke(message);
    }

    public async ValueTask DisposeAsync()
    {
        _queue.Writer.TryComplete();
        try { await _writerTask.ConfigureAwait(false); } catch { /* already logged */ }
    }
}
