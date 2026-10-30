using System.Globalization;
using System.Text;
using Duelcliping.Core.Encoders;
using Duelcliping.Core.Media;
using Duelcliping.Core.Settings;

namespace Duelcliping.Core.Library;

public enum TransitionStyle { None, Fade, Dissolve, Wipe, Slide, Circle, Flash }
public enum ColorLook { None, Vivid, Cinematic, BlackWhite, Warm, Cool, Retro, Vignette }
public enum TitlePlacement { Top, Center, Bottom }

/// <summary>Creative edits applied on top of the cut: music, transitions, colour, speed, fades and a title.</summary>
public sealed record EditEffects
{
    /// <summary>Background music file (any audio format ffmpeg reads). Loops if shorter than the video.</summary>
    public string? MusicPath { get; init; }
    public double MusicVolume { get; init; } = 0.8;
    /// <summary>Volume of the clip's own sound (all tracks mixed), 0 = mute.</summary>
    public double OriginalVolume { get; init; } = 1.0;
    public TransitionStyle Transition { get; init; } = TransitionStyle.None;
    public double TransitionSeconds { get; init; } = 0.5;
    public ColorLook Look { get; init; } = ColorLook.None;
    /// <summary>Playback speed 0.5 .. 2.0.</summary>
    public double Speed { get; init; } = 1.0;
    public bool FadeIn { get; init; }
    public bool FadeOut { get; init; }
    public string? Title { get; init; }
    public TitlePlacement TitlePosition { get; init; } = TitlePlacement.Bottom;
    /// <summary>Show the title for the first N seconds of the output; 0 = the whole video.</summary>
    public double TitleSeconds { get; init; } = 4;

    public bool HasAny =>
        !string.IsNullOrWhiteSpace(MusicPath) || Math.Abs(OriginalVolume - 1) > 0.001 || Transition != TransitionStyle.None ||
        Look != ColorLook.None || Math.Abs(Speed - 1) > 0.001 || FadeIn || FadeOut || !string.IsNullOrWhiteSpace(Title);
}

/// <summary>Builds the single ffmpeg filter graph that renders cut + effects in one encode pass.</summary>
public static class EffectsRenderer
{
    private static readonly CultureInfo Inv = CultureInfo.InvariantCulture;
    private static string F(double v) => v.ToString("0.###", Inv);

    /// <summary>Length of the rendered video (transitions overlap neighbouring parts, speed scales time).</summary>
    public static double OutputSeconds(IReadOnlyList<CutSegment> segments, EditEffects fx)
    {
        double d = TransitionDuration(segments, fx);
        double joined = segments.Sum(s => s.Duration.TotalSeconds) - d * (segments.Count - 1);
        return joined / Math.Clamp(fx.Speed, 0.5, 2.0);
    }

    private static double TransitionDuration(IReadOnlyList<CutSegment> segments, EditEffects fx)
    {
        if (fx.Transition == TransitionStyle.None || segments.Count < 2) return 0;
        double shortest = segments.Min(s => s.Duration.TotalSeconds);
        return Math.Clamp(fx.TransitionSeconds, 0.1, Math.Max(0.1, shortest / 2 - 0.05));
    }

    private static string XfadeName(TransitionStyle t) => t switch
    {
        TransitionStyle.Dissolve => "dissolve",
        TransitionStyle.Wipe => "wipeleft",
        TransitionStyle.Slide => "slideleft",
        TransitionStyle.Circle => "circleopen",
        TransitionStyle.Flash => "fadewhite",
        _ => "fade",
    };

    private static string? LookFilter(ColorLook look) => look switch
    {
        ColorLook.Vivid => "eq=saturation=1.35:contrast=1.08",
        ColorLook.Cinematic => "eq=contrast=1.12:saturation=0.85,colorbalance=rs=-0.05:bs=0.08:rh=0.08:bh=-0.05",
        ColorLook.BlackWhite => "hue=s=0,eq=contrast=1.1",
        ColorLook.Warm => "colorbalance=rm=0.08:gm=0.02:bm=-0.08",
        ColorLook.Cool => "colorbalance=rm=-0.06:bm=0.08",
        ColorLook.Retro => "curves=preset=vintage",
        ColorLook.Vignette => "vignette=angle=PI/4.5",
        _ => null,
    };

    /// <summary>Bold system font for the title (Segoe UI Bold on Windows, DejaVu/Liberation on Linux).</summary>
    public static string? FindFont()
    {
        var candidates = OperatingSystem.IsWindows()
            ? new[] { "segoeuib.ttf", "arialbd.ttf", "arial.ttf" }.Select(f => Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.Fonts), f))
            : new[]
            {
                "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", "/usr/share/fonts/dejavu/DejaVuSans-Bold.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf", "/usr/share/fonts/liberation-sans/LiberationSans-Bold.ttf",
            };
        return candidates.FirstOrDefault(File.Exists);
    }

    public static List<string> BuildArgs(EditRequest r, IReadOnlyList<CutSegment> segments, MediaInfo info, string output, string? titleTextFile)
    {
        var fx = r.Effects ?? new EditEffects();
        var encoder = r.Encoder ?? new EncoderInfo(EncoderVendor.Software, VideoCodec.H264, "libx264");
        double speed = Math.Clamp(fx.Speed, 0.5, 2.0);
        int fps = (int)Math.Clamp(Math.Round(r.FrameRate ?? (info.FrameRate > 0 ? info.FrameRate : 60)), 10, 240);
        int height = Math.Min(r.MaxHeight ?? info.Height, info.Height > 0 ? info.Height : 1080) & ~1;
        int width = info.Height > 0 ? ((int)Math.Round(info.Width * (double)height / info.Height)) & ~1 : 1920;
        int n = segments.Count;
        double d = TransitionDuration(segments, fx);
        bool hasSource = info.AudioTracks > 0;
        bool hasMusic = !string.IsNullOrWhiteSpace(fx.MusicPath);
        double total = OutputSeconds(segments, fx);
        var g = new List<string>();

        // Video: one branch per kept part, normalised so transitions can blend them.
        g.Add(n == 1 ? "[0:v:0]null[vsrc0]" : "[0:v:0]split=" + n + string.Concat(Enumerable.Range(0, n).Select(i => $"[vsrc{i}]")));
        for (int i = 0; i < n; i++)
            g.Add($"[vsrc{i}]trim=start={F(segments[i].Start.TotalSeconds)}:end={F(segments[i].End.TotalSeconds)},setpts=PTS-STARTPTS," +
                  $"fps={fps},scale={width}:{height}:flags=bicubic,setsar=1,format=yuv420p[v{i}]");

        // Audio: all source tracks mixed into one stereo stream, then cut the same way.
        if (hasSource)
        {
            string mix = info.AudioTracks == 1
                ? "[0:a:0]aformat=sample_rates=48000:channel_layouts=stereo"
                : string.Concat(Enumerable.Range(0, info.AudioTracks).Select(t => $"[0:a:{t}]")) +
                  $"amix=inputs={info.AudioTracks}:duration=longest:normalize=0,aformat=sample_rates=48000:channel_layouts=stereo";
            g.Add(mix + (n == 1 ? "[asrc0]" : ",asplit=" + n + string.Concat(Enumerable.Range(0, n).Select(i => $"[asrc{i}]"))));
            for (int i = 0; i < n; i++)
                g.Add($"[asrc{i}]atrim=start={F(segments[i].Start.TotalSeconds)}:end={F(segments[i].End.TotalSeconds)},asetpts=PTS-STARTPTS[a{i}]");
        }

        // Join the parts: plain concatenation or a transition between each pair.
        if (n == 1)
        {
            g.Add("[v0]null[vj]");
            if (hasSource) g.Add("[a0]anull[aj]");
        }
        else if (d <= 0)
        {
            var inputs = string.Concat(Enumerable.Range(0, n).Select(i => hasSource ? $"[v{i}][a{i}]" : $"[v{i}]"));
            g.Add($"{inputs}concat=n={n}:v=1:a={(hasSource ? 1 : 0)}[vj]" + (hasSource ? "[aj]" : ""));
        }
        else
        {
            string vPrev = "v0", aPrev = "a0";
            double length = segments[0].Duration.TotalSeconds;
            for (int i = 1; i < n; i++)
            {
                string vOut = i == n - 1 ? "vj" : $"vx{i}";
                g.Add($"[{vPrev}][v{i}]xfade=transition={XfadeName(fx.Transition)}:duration={F(d)}:offset={F(length - d)}[{vOut}]");
                if (hasSource)
                {
                    string aOut = i == n - 1 ? "aj" : $"ax{i}";
                    g.Add($"[{aPrev}][a{i}]acrossfade=d={F(d)}[{aOut}]");
                    aPrev = aOut;
                }
                vPrev = vOut;
                length += segments[i].Duration.TotalSeconds - d;
            }
        }

        // Video finishing: speed, colour look, fades, title.
        var post = new List<string>();
        if (Math.Abs(speed - 1) > 0.001) post.Add($"setpts=PTS/{F(speed)}");
        if (LookFilter(fx.Look) is { } look) post.Add(look);
        if (fx.FadeIn) post.Add($"fade=t=in:st=0:d={F(Math.Min(0.8, total / 4))}");
        if (fx.FadeOut) post.Add($"fade=t=out:st={F(Math.Max(0, total - Math.Min(0.8, total / 4)))}:d={F(Math.Min(0.8, total / 4))}");
        if (titleTextFile != null && FindFont() is { } font)
        {
            string y = fx.TitlePosition switch
            {
                TitlePlacement.Top => "h*0.08",
                TitlePlacement.Center => "(h-text_h)/2",
                _ => "h*0.92-text_h",
            };
            string enable = fx.TitleSeconds > 0 ? $":enable='lt(t,{F(fx.TitleSeconds)})'" : "";
            post.Add($"drawtext=fontfile='{FfmpegRunner.EscapeFilterValue(font)}':textfile='{FfmpegRunner.EscapeFilterValue(titleTextFile)}'" +
                     $":fontcolor=white:fontsize=h/13:borderw=3:bordercolor=black@0.55:x=(w-text_w)/2:y={y}{enable}");
        }
        post.Add(encoder.IsHardware ? "format=nv12" : "format=yuv420p");
        g.Add($"[vj]{string.Join(',', post)}[vout]");

        // Audio finishing: speed (pitch preserved), volume, fades, background music.
        bool hasAudioOut = hasSource || hasMusic;
        if (hasSource)
        {
            var apost = new List<string>();
            if (Math.Abs(speed - 1) > 0.001) apost.Add($"atempo={F(speed)}");
            apost.Add($"volume={F(Math.Clamp(fx.OriginalVolume, 0, 2))}");
            g.Add($"[aj]{string.Join(',', apost)}[aorig]");
        }
        if (hasMusic)
            g.Add($"[1:a:0]aformat=sample_rates=48000:channel_layouts=stereo,volume={F(Math.Clamp(fx.MusicVolume, 0, 2))}," +
                  $"atrim=0:{F(total)},asetpts=PTS-STARTPTS[amus]");
        if (hasAudioOut)
        {
            string fades = (fx.FadeIn ? $",afade=t=in:st=0:d={F(Math.Min(0.8, total / 4))}" : "") +
                           (fx.FadeOut ? $",afade=t=out:st={F(Math.Max(0, total - Math.Min(0.8, total / 4)))}:d={F(Math.Min(0.8, total / 4))}" : "");
            if (hasSource && hasMusic) g.Add($"[aorig][amus]amix=inputs=2:duration=first:normalize=0{fades}[aout]");
            else g.Add($"[{(hasSource ? "aorig" : "amus")}]anull{fades}[aout]");
        }

        var args = new List<string> { "-hide_banner", "-loglevel", "error", "-nostats", "-progress", "pipe:2", "-y", "-i", r.Input };
        if (hasMusic) args.AddRange(new[] { "-stream_loop", "-1", "-i", fx.MusicPath! });
        args.AddRange(new[] { "-filter_complex", string.Join(';', g), "-map", "[vout]" });
        if (hasAudioOut) args.AddRange(new[] { "-map", "[aout]", "-c:a", "aac", "-b:a", "192k" });
        args.AddRange(new[]
        {
            "-c:v", encoder.FfmpegName,
            "-b:v", $"{r.BitrateKbps}k", "-maxrate", $"{r.BitrateKbps * 3 / 2}k", "-bufsize", $"{r.BitrateKbps * 2}k",
            "-t", F(total),
        });
        args.AddRange(encoder.Vendor switch
        {
            EncoderVendor.Nvidia => new[] { "-preset", "p5", "-rc", "vbr" },
            EncoderVendor.Amd => new[] { "-quality", "quality", "-rc", "vbr_peak" },
            EncoderVendor.Intel => new[] { "-preset", "medium" },
            _ => new[] { "-preset", "fast" },
        });
        bool mkv = Path.GetExtension(r.Output).Equals(".mkv", StringComparison.OrdinalIgnoreCase);
        args.AddRange(mkv ? new[] { "-f", "matroska" } : new[] { "-movflags", "+faststart", "-f", "mp4" });
        args.Add(output);
        return args;
    }

    /// <summary>Writes the title to a UTF-8 file for drawtext (avoids all filter-graph escaping problems).</summary>
    public static string? WriteTitleFile(EditEffects? fx)
    {
        if (fx == null || string.IsNullOrWhiteSpace(fx.Title)) return null;
        var path = Path.Combine(Path.GetTempPath(), $"moments-title-{Guid.NewGuid():N}.txt");
        File.WriteAllText(path, fx.Title.Trim(), new UTF8Encoding(false));
        return path;
    }
}
