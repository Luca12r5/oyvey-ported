using System.Collections.Concurrent;

namespace Duelcliping.Core.Recording;

/// <summary>
/// Pool of fixed-size byte blocks backing the replay buffer. Re-using blocks keeps the
/// steady-state recording loop free of managed allocations, so the GC never has to
/// compact hundreds of megabytes of video while a game is running.
/// </summary>
public sealed class BlockPool
{
    public const int BlockSize = 188 * 1394; // ~256 KiB, always a whole number of TS packets

    private readonly ConcurrentBag<byte[]> _free = new();
    private int _freeCount;
    private long _allocated;

    /// <summary>Upper bound of idle blocks kept around for re-use (the rest is released to the GC).</summary>
    public int MaxIdleBlocks { get; set; } = 64;

    public long AllocatedBytes => Interlocked.Read(ref _allocated) * BlockSize;
    public long IdleBytes => (long)Volatile.Read(ref _freeCount) * BlockSize;

    public byte[] Rent()
    {
        if (_free.TryTake(out var block))
        {
            Interlocked.Decrement(ref _freeCount);
            return block;
        }
        Interlocked.Increment(ref _allocated);
        return GC.AllocateUninitializedArray<byte>(BlockSize);
    }

    public void Return(byte[] block)
    {
        if (block.Length != BlockSize) return;
        if (Volatile.Read(ref _freeCount) >= MaxIdleBlocks)
        {
            Interlocked.Decrement(ref _allocated);
            return;
        }
        Interlocked.Increment(ref _freeCount);
        _free.Add(block);
    }

    /// <summary>Drops all idle blocks (e.g. after the buffer was shrunk or recording stopped).</summary>
    public void Trim()
    {
        while (_free.TryTake(out _))
        {
            Interlocked.Decrement(ref _freeCount);
            Interlocked.Decrement(ref _allocated);
        }
    }
}
