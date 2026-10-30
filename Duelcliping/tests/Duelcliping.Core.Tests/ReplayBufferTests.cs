using Duelcliping.Core.Media;
using Duelcliping.Core.Recording;
using Duelcliping.Core.Settings;

namespace Duelcliping.Core.Tests;

public class ReplayBufferTests
{
    private static async Task<byte[]> MakeTsAsync(TempDir dir, int seconds, string codec = "libx264")
    {
        var path = Path.Combine(dir.Path, "src.ts");
        var args = new List<string> { "-v", "error", "-f", "lavfi", "-i", "testsrc2=s=320x240:r=30", "-f", "lavfi", "-i", "sine=f=440:sample_rate=48000",
            "-t", seconds.ToString(), "-c:v", codec, "-g", "30", "-pix_fmt", "yuv420p" };
        if (codec == "libx264") args.AddRange(new[] { "-flags:v", "+global_header", "-bsf:v", "dump_extra=freq=keyframe" });
        args.AddRange(new[] { "-c:a", "aac", "-f", "mpegts", path });
        var r = await FfmpegRunner.RunFfmpegAsync(args);
        Assert.True(r.Success, r.StdErr);
        return await File.ReadAllBytesAsync(path);
    }

    private static void Feed(ReplayBuffer buffer, byte[] ts)
    {
        // Feed in odd-sized slices aligned to packets like the pipe reader does.
        for (int i = 0; i < ts.Length; i += TsPacket.Size * 37)
            buffer.Write(ts.AsSpan(i, Math.Min(TsPacket.Size * 37, ts.Length - i)));
    }

    [Fact]
    public async Task Indexes_one_chunk_per_keyframe()
    {
        using var dir = new TempDir();
        var ts = await MakeTsAsync(dir, 6);
        using var buffer = new ReplayBuffer { MaxDurationSeconds = 600 };
        int chunks = 0;
        buffer.ChunkStarted += _ => chunks++;
        buffer.BeginGeneration();
        Feed(buffer, ts);
        Assert.InRange(chunks, 6, 7);
        Assert.InRange(buffer.BufferedSeconds, 5.5, 6.1);
    }

    [Fact]
    public async Task Evicts_history_beyond_configured_duration()
    {
        using var dir = new TempDir();
        var ts = await MakeTsAsync(dir, 10);
        using var buffer = new ReplayBuffer { MaxDurationSeconds = 3 };
        buffer.BeginGeneration();
        Feed(buffer, ts);
        Assert.InRange(buffer.BufferedSeconds, 3, 4.1);
        Assert.True(buffer.BufferedBytes < ts.Length / 2);
    }

    [Theory]
    [InlineData(ContainerFormat.Mp4)]
    [InlineData(ContainerFormat.Mkv)]
    public async Task Snapshot_is_written_as_valid_clip(ContainerFormat container)
    {
        using var dir = new TempDir();
        var ts = await MakeTsAsync(dir, 8);
        using var buffer = new ReplayBuffer { MaxDurationSeconds = 60 };
        buffer.BeginGeneration();
        Feed(buffer, ts);

        using var snapshot = buffer.Snapshot(3)!;
        Assert.NotNull(snapshot);
        Assert.False(snapshot.IsShorterThanRequested);
        var result = await ClipWriter.SaveAsync(snapshot, dir.Path, "clip", container, new[] { "Game" });

        Assert.True(result.Remuxed, result.Warning);
        Assert.EndsWith(container == ContainerFormat.Mp4 ? ".mp4" : ".mkv", result.FilePath);
        Assert.False(File.Exists(Path.Combine(dir.Path, "clip.ts.partial")));
        var info = await Probe.RunAsync(result.FilePath);
        Assert.Equal(1, info.VideoStreams);
        Assert.Equal(1, info.AudioStreams);
        // Clips start at the keyframe at or before the requested point (GOP = 1 s).
        Assert.InRange(info.Duration, 2.9, 4.5);
        Assert.Equal("", await Probe.DecodeErrorsAsync(result.FilePath));
    }

    [Fact]
    public async Task Snapshot_reports_when_less_than_requested_is_available()
    {
        using var dir = new TempDir();
        var ts = await MakeTsAsync(dir, 3);
        using var buffer = new ReplayBuffer();
        buffer.BeginGeneration();
        Feed(buffer, ts);
        using var snapshot = buffer.Snapshot(30)!;
        Assert.True(snapshot.IsShorterThanRequested);
        Assert.InRange(snapshot.AvailableSeconds, 2.5, 3.1);
    }

    [Fact]
    public async Task Snapshot_keeps_memory_alive_while_buffer_evicts()
    {
        using var dir = new TempDir();
        var ts = await MakeTsAsync(dir, 6);
        using var buffer = new ReplayBuffer { MaxDurationSeconds = 600 };
        buffer.BeginGeneration();
        Feed(buffer, ts);
        using var snapshot = buffer.Snapshot(5)!;
        buffer.Clear(); // would return all blocks to the pool if the snapshot did not hold references
        Feed(buffer, new byte[0]);
        var result = await ClipWriter.SaveAsync(snapshot, dir.Path, "after-clear", ContainerFormat.Mp4, new[] { "Game" });
        Assert.True(result.Remuxed);
        Assert.Equal("", await Probe.DecodeErrorsAsync(result.FilePath));
    }

    [Fact]
    public async Task Av1_stream_can_be_buffered_and_remuxed()
    {
        var encoders = await FfmpegRunner.RunFfmpegAsync(new[] { "-hide_banner", "-encoders" });
        if (!encoders.StdOut.Contains(" libsvtav1 ")) return; // encoder not present in this ffmpeg build
        using var dir = new TempDir();
        var ts = await MakeTsAsync(dir, 4, "libsvtav1");
        if (!await Duelcliping.Core.Encoders.EncoderCatalog.TransportStreamCarriesAv1Async(Path.Combine(dir.Path, "src.ts")))
            return; // this ffmpeg build cannot demux AV1 from TS; the catalog disables AV1 in that case
        using var buffer = new ReplayBuffer();
        buffer.BeginGeneration();
        Feed(buffer, ts);
        using var snapshot = buffer.Snapshot(2)!;
        Assert.NotNull(snapshot);
        var result = await ClipWriter.SaveAsync(snapshot, dir.Path, "av1", ContainerFormat.Mp4, new[] { "Game" });
        Assert.True(result.Remuxed, result.Warning);
        Assert.Equal("", await Probe.DecodeErrorsAsync(result.FilePath));
    }
}
