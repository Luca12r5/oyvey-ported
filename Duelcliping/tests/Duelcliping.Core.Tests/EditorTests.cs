using Duelcliping.Core.Library;
using Duelcliping.Core.Media;
using Duelcliping.Core.Settings;

namespace Duelcliping.Core.Tests;

public class EditorTests
{
    private static async Task<string> MakeVideoAsync(TempDir dir, string name = "src.mp4", int seconds = 10)
    {
        var path = Path.Combine(dir.Path, name);
        var r = await FfmpegRunner.RunFfmpegAsync(new[]
        {
            "-v", "error", "-f", "lavfi", "-i", $"testsrc2=s=320x240:r=30:d={seconds}",
            "-f", "lavfi", "-i", $"sine=f=440:d={seconds}", "-f", "lavfi", "-i", $"sine=f=880:d={seconds}",
            "-map", "0", "-map", "1", "-map", "2", "-c:v", "libx264", "-g", "30", "-pix_fmt", "yuv420p", "-c:a", "aac", path,
        });
        Assert.True(r.Success, r.StdErr);
        return path;
    }

    private static TimeSpan S(double s) => TimeSpan.FromSeconds(s);

    [Fact]
    public void Normalize_merges_overlaps_and_rejects_empty()
    {
        var n = ClipEditor.Normalize(new[] { new CutSegment(S(5), S(8)), new CutSegment(S(1), S(3)), new CutSegment(S(2), S(4)) }, S(10));
        Assert.Equal(new[] { new CutSegment(S(1), S(4)), new CutSegment(S(5), S(8)) }, n);
        Assert.Throws<ArgumentException>(() => ClipEditor.Normalize(new[] { new CutSegment(S(3), S(3)) }, S(10)));
        Assert.Equal(S(10), ClipEditor.Normalize(new[] { new CutSegment(S(9), S(50)) }, S(10))[0].End);
    }

    [Theory]
    [InlineData(false)]
    [InlineData(true)]
    public async Task Removes_middle_part_and_keeps_all_audio_tracks(bool exact)
    {
        using var dir = new TempDir();
        var src = await MakeVideoAsync(dir);
        var output = ClipEditor.SuggestOutputPath(src);
        var progress = new List<double>();
        await ClipEditor.RunAsync(new EditRequest
        {
            Input = src,
            Output = output,
            Segments = new[] { new CutSegment(S(0), S(3)), new CutSegment(S(6), S(10)) },
            ReEncode = exact,
        }, new Progress<double>(progress.Add), CancellationToken.None);

        var info = await Probe.RunAsync(output);
        Assert.Equal(2, info.AudioStreams);
        if (exact) Assert.InRange(info.Duration, 6.9, 7.15);
        else Assert.InRange(info.Duration, 6.5, 8.2); // keyframe aligned
        var errors = await Probe.DecodeErrorsAsync(output);
        Assert.True(errors == "", errors);
        Assert.EndsWith("(cut).mp4", output);
        Assert.False(File.Exists(output + ".partial"));
    }

    [Fact]
    public async Task Single_trim_with_downscale_to_mkv()
    {
        using var dir = new TempDir();
        var src = await MakeVideoAsync(dir);
        var output = Path.Combine(dir.Path, "out.mkv");
        await ClipEditor.RunAsync(new EditRequest
        {
            Input = src, Output = output, Segments = new[] { new CutSegment(S(2), S(5)) },
            ReEncode = true, MaxHeight = 120,
        }, null, CancellationToken.None);
        var info = await Probe.RunAsync(output);
        Assert.Equal(120, info.Height);
        Assert.Contains("matroska", info.FormatName);
        Assert.InRange(info.Duration, 2.9, 3.15);
    }
}
