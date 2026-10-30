using Duelcliping.Core.Logging;
using Duelcliping.Core.Media;

namespace Duelcliping.Core.Recording;

/// <summary>
/// In-memory rolling buffer of the already encoded MPEG-TS stream.
///
/// The encoder runs continuously and its output is indexed on the fly: every video keyframe
/// starts a new <see cref="GopChunk"/>. Chunks older than <see cref="MaxDurationSeconds"/> are
/// evicted and their memory blocks re-used. Saving a clip therefore never re-encodes anything
/// and never touches the disk until the user actually asks for a clip.
/// </summary>
public sealed class ReplayBuffer : IDisposable
{
    private const long PtsWrap = 1L << 33;

    private readonly object _lock = new();
    private readonly List<GopChunk> _chunks = new();
    private GopChunk? _open;
    private int _generation;
    private long _nextChunkId;

    private int _pmtPid = -1;
    private int _videoPid = -1;
    private byte[]? _pat;
    private byte[]? _pmt;
    private long _lastRawPts = -1;
    private long _ptsOffset;
    private long _latestPts = -1;
    private bool _warnedByteCap;

    public ReplayBuffer(BlockPool? pool = null) => Pool = pool ?? new BlockPool();

    public BlockPool Pool { get; }

    /// <summary>How much history to keep. Changing it takes effect on the next keyframe.</summary>
    public double MaxDurationSeconds { get; set; } = 120;

    /// <summary>Hard memory cap; protects the system if the encoder produces far more data than expected.</summary>
    public long MaxBytes { get; set; } = 2L * 1024 * 1024 * 1024;

    public int Generation { get { lock (_lock) return _generation; } }

    /// <summary>Raised on the stream reader thread when a chunk is complete. Handlers must not block.</summary>
    public event Action<GopChunk>? ChunkSealed;

    /// <summary>Raised on the reader thread when a new chunk starts (i.e. on every keyframe).</summary>
    public event Action<GopChunk>? ChunkStarted;

    public double BufferedSeconds
    {
        get
        {
            lock (_lock)
            {
                var first = _chunks.Count > 0 ? _chunks[0] : _open;
                return first == null || _latestPts < 0 ? 0 : (_latestPts - first.StartPts) / 90000.0;
            }
        }
    }

    public long BufferedBytes
    {
        get
        {
            lock (_lock) return _chunks.Sum(c => c.Length) + (_open?.Length ?? 0);
        }
    }

    public long? OpenChunkId { get { lock (_lock) return _open?.Id; } }

    /// <summary>Starts a new, independent stream (called whenever the encoder process (re)starts).</summary>
    public void BeginGeneration()
    {
        List<GopChunk> dropped;
        GopChunk? sealedChunk;
        lock (_lock)
        {
            sealedChunk = SealOpenLocked();
            dropped = new List<GopChunk>(_chunks);
            _chunks.Clear();
            _generation++;
            _pmtPid = _videoPid = -1;
            _pat = _pmt = null;
            _lastRawPts = -1;
            _ptsOffset = 0;
            _latestPts = -1;
        }
        if (sealedChunk != null) RaiseSealed(sealedChunk);
        foreach (var chunk in dropped) chunk.Release();
        if (dropped.Count > 0) Log.Info("ReplayBuffer", $"Started stream generation {_generation}; discarded {dropped.Count} chunks of the previous stream");
    }

    /// <summary>Seals the open chunk so sinks receive the last GOP (called when the encoder stops).</summary>
    public void EndGeneration()
    {
        GopChunk? sealedChunk;
        lock (_lock) sealedChunk = SealOpenLocked();
        if (sealedChunk != null) RaiseSealed(sealedChunk);
    }

    /// <summary>Feeds packet-aligned transport stream data. Called from a single reader thread.</summary>
    public void Write(ReadOnlySpan<byte> data)
    {
        for (int offset = 0; offset + TsPacket.Size <= data.Length; offset += TsPacket.Size)
            WritePacket(data.Slice(offset, TsPacket.Size));
    }

    private void WritePacket(ReadOnlySpan<byte> packet)
    {
        int pid = TsPacket.Pid(packet);
        if (pid == TsPacket.NullPid) return;

        if (pid == TsPacket.PatPid && TsPacket.PayloadUnitStart(packet))
        {
            int pmt = TsPacket.ParsePatForPmtPid(packet);
            if (pmt >= 0) { _pmtPid = pmt; _pat = packet.ToArray(); }
        }
        else if (pid == _pmtPid && TsPacket.PayloadUnitStart(packet))
        {
            int video = TsPacket.ParsePmtForVideoPid(packet);
            if (video >= 0) { _videoPid = video; _pmt = packet.ToArray(); }
        }

        if (pid == _videoPid && TsPacket.PayloadUnitStart(packet))
        {
            int payload = TsPacket.PayloadOffset(packet);
            if (payload >= 0 && TsPacket.TryReadPesPts(packet.Slice(payload), out long rawPts))
            {
                long pts = Unwrap(rawPts);
                if (TsPacket.RandomAccessIndicator(packet))
                {
                    StartChunk(pts);
                }
                if (_open != null && pts > _open.EndPts) _open.EndPts = pts;
                if (pts > _latestPts) _latestPts = pts;
            }
        }

        // Everything before the first keyframe cannot be decoded on its own and is dropped.
        _open?.Append(packet);
    }

    private long Unwrap(long rawPts)
    {
        if (_lastRawPts >= 0 && rawPts < _lastRawPts - PtsWrap / 2) _ptsOffset += PtsWrap;
        _lastRawPts = rawPts;
        return rawPts + _ptsOffset;
    }

    private void StartChunk(long pts)
    {
        var psi = new byte[(_pat != null ? TsPacket.Size : 0) + (_pmt != null ? TsPacket.Size : 0)];
        if (_pat != null) _pat.CopyTo(psi, 0);
        if (_pmt != null) _pmt.CopyTo(psi, _pat != null ? TsPacket.Size : 0);

        GopChunk? sealedChunk;
        List<GopChunk>? evicted = null;
        GopChunk created;
        lock (_lock)
        {
            sealedChunk = SealOpenLocked();
            created = _open = new GopChunk(Pool, _nextChunkId++, _generation, pts, psi);
            long cutoff = (long)(MaxDurationSeconds * 90000);
            while (_chunks.Count > 1 && pts - _chunks[1].StartPts >= cutoff)
            {
                (evicted ??= new()).Add(_chunks[0]);
                _chunks.RemoveAt(0);
            }
            long total = _chunks.Sum(c => c.Length);
            while (_chunks.Count > 1 && total > MaxBytes)
            {
                total -= _chunks[0].Length;
                (evicted ??= new()).Add(_chunks[0]);
                _chunks.RemoveAt(0);
                if (!_warnedByteCap)
                {
                    _warnedByteCap = true;
                    Log.Warn("ReplayBuffer", $"Replay buffer reached its memory cap of {MaxBytes / (1024 * 1024)} MB; the buffer is shorter than configured. Lower bitrate or buffer length.");
                }
            }
        }
        if (evicted != null) foreach (var c in evicted) c.Release();
        if (sealedChunk != null) RaiseSealed(sealedChunk);
        ChunkStarted?.Invoke(created);
    }

    private GopChunk? SealOpenLocked()
    {
        if (_open == null) return null;
        var chunk = _open;
        _open = null;
        chunk.Seal();
        _chunks.Add(chunk);
        return chunk;
    }

    private void RaiseSealed(GopChunk chunk)
    {
        var handler = ChunkSealed;
        if (handler == null) return;
        foreach (Action<GopChunk> h in handler.GetInvocationList())
        {
            try { h(chunk); }
            catch (Exception ex) { Log.Error("ReplayBuffer", "Chunk sink failed", ex); }
        }
    }

    /// <summary>
    /// Captures the newest <paramref name="seconds"/> of video (starting at the keyframe at or before
    /// that point). The returned snapshot holds references, so eviction cannot free its memory while
    /// the clip is written. Returns null when nothing is buffered yet.
    /// </summary>
    public ClipSnapshot? Snapshot(double seconds)
    {
        lock (_lock)
        {
            var all = new List<GopChunk>(_chunks);
            if (_open != null) all.Add(_open);
            if (all.Count == 0 || _latestPts < 0) return null;

            long target = _latestPts - (long)(seconds * 90000);
            int startIndex = 0;
            for (int i = all.Count - 1; i >= 0; i--)
            {
                if (all[i].StartPts <= target) { startIndex = i; break; }
            }

            var parts = new List<ClipSnapshot.Part>(all.Count - startIndex);
            for (int i = startIndex; i < all.Count; i++)
            {
                if (all[i].TryAddRef()) parts.Add(new ClipSnapshot.Part(all[i], all[i].Length));
            }
            if (parts.Count == 0) return null;
            double available = (_latestPts - parts[0].Chunk.StartPts) / 90000.0;
            return new ClipSnapshot(parts, available, seconds, available + 0.5 < seconds);
        }
    }

    public void Clear()
    {
        List<GopChunk> dropped;
        lock (_lock)
        {
            dropped = new List<GopChunk>(_chunks);
            if (_open != null) dropped.Add(_open);
            _chunks.Clear();
            _open = null;
            _latestPts = -1;
        }
        foreach (var c in dropped) c.Release();
        Pool.Trim();
    }

    public void Dispose() => Clear();
}

/// <summary>A referenced, immutable view of buffered chunks that can be written out as a clip.</summary>
public sealed class ClipSnapshot : IDisposable
{
    public readonly record struct Part(GopChunk Chunk, long Length);

    private bool _disposed;

    internal ClipSnapshot(List<Part> parts, double availableSeconds, double requestedSeconds, bool truncated)
    {
        Parts = parts;
        AvailableSeconds = availableSeconds;
        RequestedSeconds = requestedSeconds;
        IsShorterThanRequested = truncated;
    }

    public IReadOnlyList<Part> Parts { get; }
    public double AvailableSeconds { get; }
    public double RequestedSeconds { get; }
    public bool IsShorterThanRequested { get; }
    public long TotalBytes => Parts.Sum(p => p.Length) + (Parts.Count > 0 ? Parts[0].Chunk.PsiPackets.Length : 0);

    /// <summary>Writes a self-contained transport stream (PSI tables first, then all chunks).</summary>
    public void WriteTo(Stream destination)
    {
        ObjectDisposedException.ThrowIf(_disposed, this);
        if (Parts.Count == 0) return;
        destination.Write(Parts[0].Chunk.PsiPackets);
        foreach (var part in Parts) part.Chunk.WriteTo(destination, part.Length);
    }

    public void Dispose()
    {
        if (_disposed) return;
        _disposed = true;
        foreach (var part in Parts) part.Chunk.Release();
    }
}
