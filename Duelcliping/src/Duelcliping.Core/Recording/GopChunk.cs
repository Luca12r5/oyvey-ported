namespace Duelcliping.Core.Recording;

/// <summary>
/// One group of pictures worth of transport stream: every TS packet from a video keyframe
/// up to (but excluding) the next keyframe. Chunks are the unit of eviction and of clip
/// extraction, so every clip starts on a decodable keyframe.
/// Chunks are reference counted: the buffer holds one reference, and every clip writer or
/// recording sink that reads the chunk holds another, so blocks only go back to the pool
/// once nobody reads them anymore.
/// </summary>
public sealed class GopChunk
{
    private readonly BlockPool _pool;
    private readonly List<byte[]> _blocks = new(4);
    private int _refCount = 1;
    private int _usedInLastBlock;
    private long _length;

    internal GopChunk(BlockPool pool, long id, int generation, long startPts, byte[] psiPackets)
    {
        _pool = pool;
        Id = id;
        Generation = generation;
        StartPts = startPts;
        EndPts = startPts;
        PsiPackets = psiPackets;
        CreatedUtc = DateTime.UtcNow;
    }

    public long Id { get; }
    public int Generation { get; }
    /// <summary>Unwrapped 90 kHz PTS of the keyframe that starts this chunk.</summary>
    public long StartPts { get; }
    /// <summary>Highest video PTS seen inside this chunk.</summary>
    public long EndPts { get; internal set; }
    /// <summary>PAT + PMT packets valid for this chunk, written in front of extracted clips.</summary>
    public byte[] PsiPackets { get; }
    public DateTime CreatedUtc { get; }
    public bool Sealed { get; private set; }
    public long Length => Volatile.Read(ref _length);
    public double DurationSeconds => (EndPts - StartPts) / 90000.0;

    /// <summary>Appends one TS packet. Only called by the single stream reader thread.</summary>
    internal void Append(ReadOnlySpan<byte> packet)
    {
        if (_blocks.Count == 0 || _usedInLastBlock + packet.Length > BlockPool.BlockSize)
        {
            var block = _pool.Rent();
            lock (_blocks) _blocks.Add(block);
            _usedInLastBlock = 0;
        }
        packet.CopyTo(_blocks[^1].AsSpan(_usedInLastBlock));
        _usedInLastBlock += packet.Length;
        Volatile.Write(ref _length, _length + packet.Length);
    }

    internal void Seal() => Sealed = true;

    public bool TryAddRef()
    {
        while (true)
        {
            int current = Volatile.Read(ref _refCount);
            if (current <= 0) return false;
            if (Interlocked.CompareExchange(ref _refCount, current + 1, current) == current) return true;
        }
    }

    public void Release()
    {
        if (Interlocked.Decrement(ref _refCount) != 0) return;
        lock (_blocks)
        {
            foreach (var block in _blocks) _pool.Return(block);
            _blocks.Clear();
        }
    }

    /// <summary>
    /// Writes the first <paramref name="length"/> bytes of the chunk. Safe to call while the reader
    /// thread is still appending, because bytes below <paramref name="length"/> are never modified.
    /// </summary>
    public void WriteTo(Stream destination, long length)
    {
        byte[][] blocks;
        lock (_blocks) blocks = _blocks.ToArray();
        long remaining = length;
        foreach (var block in blocks)
        {
            if (remaining <= 0) break;
            int count = (int)Math.Min(remaining, BlockPool.BlockSize);
            destination.Write(block, 0, count);
            remaining -= count;
        }
    }
}
