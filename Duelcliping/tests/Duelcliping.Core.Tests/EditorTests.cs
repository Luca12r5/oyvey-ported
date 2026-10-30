using Duelcliping.Core.Library;
using Duelcliping.Core.Media;
using Duelcliping.Core.Settings;

namespace Duelcliping.Core.Tests;

public class EditorTests
{
    private static async Task<string> MakeVideoAsync(TempDir dir, string name = "src.mp4", int seconds = 10, int gop = 30)
    {
        var path = Path.Combine(dir.Path, name);
        var r = await FfmpegRunner.RunFfmpegAsync(new[]
        {
            "-v", "error", "-f", "lavfi", "-i", $"testsrc2=s=320x240:r=30:d={seconds}",
            "-f", "lavfi", "-i", $"sine=f=440:d={seconds}", "-f", "lavfi", "-i", $"sine=f=880:d={seconds}",
            "-map", "0", "-map", "1", "-map", "2", "-c:v", "libx264", "-g", gop.ToString(), "-pix_fmt", "yuv420p", "-c:a", "aac", path,
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

    [Fact]
    public async Task Lossless_cut_switches_to_exact_when_keyframes_are_far_apart()
    {
        using var dir = new TempDir();
        var src = await MakeVideoAsync(dir, gop: 300); // a single keyframe, like many downloaded videos
        var output = Path.Combine(dir.Path, "out.mp4");
        var result = await ClipEditor.RunAsync(new EditRequest
        {
            Input = src, Output = output,
            Segments = new[] { new CutSegment(S(0), S(2)), new CutSegment(S(6), S(8)) },
        }, null, CancellationToken.None);
        Assert.True(result.ReEncoded);
        Assert.NotNull(result.Notice);
        Assert.InRange((await Probe.RunAsync(output)).Duration, 3.9, 4.15);
    }

    [Fact]
    public async Task Lossless_cut_stays_lossless_for_one_second_gop()
    {
        using var dir = new TempDir();
        var src = await MakeVideoAsync(dir);
        var result = await ClipEditor.RunAsync(new EditRequest
        {
            Input = src, Output = Path.Combine(dir.Path, "out.mp4"),
            Segments = new[] { new CutSegment(S(2.5), S(7)) },
        }, null, CancellationToken.None);
        Assert.False(result.ReEncoded);
    }
}

public class EffectsTests
{
    private static TimeSpan S(double s) => TimeSpan.FromSeconds(s);

    private static async Task<(string Video, string Music)> MakeMediaAsync(TempDir dir)
    {
        var video = Path.Combine(dir.Path, "src.mp4");
        var r = await FfmpegRunner.RunFfmpegAsync(new[]
        {
            "-v", "error", "-f", "lavfi", "-i", "testsrc2=s=320x240:r=30:d=10",
            "-f", "lavfi", "-i", "sine=f=440:d=10", "-f", "lavfi", "-i", "sine=f=880:d=10",
            "-map", "0", "-map", "1", "-map", "2", "-c:v", "libx264", "-g", "30", "-pix_fmt", "yuv420p", "-c:a", "aac", video,
        });
        Assert.True(r.Success, r.StdErr);
        var music = Path.Combine(dir.Path, "music.mp3");
        r = await FfmpegRunner.RunFfmpegAsync(new[] { "-v", "error", "-f", "lavfi", "-i", "sine=f=330:d=3", "-c:a", "libmp3lame", music });
        if (!r.Success) // ffmpeg without mp3 encoder: use wav
        {
            music = Path.ChangeExtension(music, ".wav");
            r = await FfmpegRunner.RunFfmpegAsync(new[] { "-v", "error", "-f", "lavfi", "-i", "sine=f=330:d=3", music });
        }
        Assert.True(r.Success, r.StdErr);
        return (video, music);
    }

    [Theory]
    [InlineData(TransitionStyle.Fade, ColorLook.Cinematic, 1.0, 6.5)]
    [InlineData(TransitionStyle.Slide, ColorLook.BlackWhite, 2.0, 3.25)]
    [InlineData(TransitionStyle.None, ColorLook.Retro, 0.5, 14.0)]
    public async Task Renders_transitions_looks_speed_music_fades_and_title(TransitionStyle transition, ColorLook look, double speed, double expected)
    {
        using var dir = new TempDir();
        var (video, music) = await MakeMediaAsync(dir);
        var output = Path.Combine(dir.Path, "edit.mp4");
        var result = await ClipEditor.RunAsync(new EditRequest
        {
            Input = video,
            Output = output,
            Segments = new[] { new CutSegment(S(0), S(3)), new CutSegment(S(6), S(10)) },
            Effects = new EditEffects
            {
                Transition = transition, TransitionSeconds = 0.5, Look = look, Speed = speed,
                MusicPath = music, MusicVolume = 0.5, OriginalVolume = 0.8, FadeIn = true, FadeOut = true,
                Title = "GG – what a moment!", TitlePosition = TitlePlacement.Bottom,
            },
        }, null, CancellationToken.None);

        Assert.True(result.ReEncoded);
        var info = await Probe.RunAsync(output);
        Assert.Equal(1, info.AudioStreams); // all tracks + music mixed
        Assert.InRange(info.Duration, expected - 0.15, expected + 0.15);
        var errors = await Probe.DecodeErrorsAsync(output);
        Assert.True(errors == "", errors);
    }

    [Fact]
    public void No_effects_means_no_reencode_flag()
    {
        Assert.False(new EditEffects().HasAny);
        Assert.True(new EditEffects { Speed = 1.5 }.HasAny);
        Assert.Equal(6.5, EffectsRenderer.OutputSeconds(new[] { new CutSegment(S(0), S(3)), new CutSegment(S(6), S(10)) },
            new EditEffects { Transition = TransitionStyle.Fade, TransitionSeconds = 0.5 }), 3);
    }
}
