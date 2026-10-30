using System.Globalization;
using Duelcliping.Core.Encoders;
using Duelcliping.Core.Logging;
using Duelcliping.Core.Media;
using Duelcliping.Core.Settings;

namespace Duelcliping.Core.Library;

/// <summary>A part of the source video that is kept in the output.</summary>
public readonly record struct CutSegment(TimeSpan Start, TimeSpan End)
{
    public TimeSpan Duration => End - Start;
}

/// <summary>A cut / trim / export job. The output is the concatenation of <see cref="Segments"/>.</summary>
public sealed record EditRequest
{
    public required string Input { get; init; }
    public required string Output { get; init; }
    /// <summary>Parts to keep, in output order. Empty = the whole video.</summary>
    public IReadOnlyList<CutSegment> Segments { get; init; } = Array.Empty<CutSegment>();
    /// <summary>
    /// False: stream copy (instant, lossless; each cut snaps to the keyframe before its start).
    /// True: re-encode (frame-exact cuts, optional downscale / frame rate change).
    /// </summary>
    public bool ReEncode { get; init; }
    public EncoderInfo? Encoder { get; init; }
    public int? MaxHeight { get; init; }
    public int? FrameRate { get; init; }
    public int BitrateKbps { get; init; } = 12000;
}

/// <summary>
/// The built-in video cutter. Keeps any number of segments of a clip or recording and joins them
/// into a new file. Lossless mode cuts with stream copy; exact mode re-encodes with the detected
/// hardware encoder so cuts land on the exact frame.
/// </summary>
public static class ClipEditor
{
    private static readonly CultureInfo Inv = CultureInfo.InvariantCulture;
    private static string Sec(TimeSpan t) => t.TotalSeconds.ToString("0.###", Inv);

    /// <summary>Sorts, clamps and merges overlapping segments; throws when nothing would remain.</summary>
    public static IReadOnlyList<CutSegment> Normalize(IReadOnlyList<CutSegment> segments, TimeSpan duration)
    {
        if (segments.Count == 0) return new[] { new CutSegment(TimeSpan.Zero, duration) };
        var list = segments
            .Select(s => new CutSegment(Clamp(s.Start, duration), Clamp(s.End, duration)))
            .Where(s => s.Duration > TimeSpan.FromMilliseconds(50))
            .OrderBy(s => s.Start)
            .ToList();
        var merged = new List<CutSegment>();
        foreach (var s in list)
        {
            if (merged.Count > 0 && s.Start <= merged[^1].End)
                merged[^1] = merged[^1] with { End = s.End > merged[^1].End ? s.End : merged[^1].End };
            else merged.Add(s);
        }
        if (merged.Count == 0) throw new ArgumentException("The selection is empty – keep at least one part of the video.");
        return merged;
    }

    private static TimeSpan Clamp(TimeSpan t, TimeSpan duration) =>
        t < TimeSpan.Zero ? TimeSpan.Zero : duration > TimeSpan.Zero && t > duration ? duration : t;

    /// <summary>Runs the job. <paramref name="progress"/> receives 0..1.</summary>
    public static async Task<string> RunAsync(EditRequest request, IProgress<double>? progress, CancellationToken ct)
    {
        var info = await MediaProbe.ProbeAsync(request.Input, ct).ConfigureAwait(false)
                   ?? throw new InvalidOperationException("The video could not be read.");
        var segments = Normalize(request.Segments, TimeSpan.FromSeconds(info.DurationSeconds));
        double total = segments.Sum(s => s.Duration.TotalSeconds);
        var tempOutput = request.Output + ".partial";
        Log.Info("Editor", $"{(request.ReEncode ? "Exact" : "Lossless")} cut of {request.Input}: " +
                           string.Join(", ", segments.Select(s => $"{Sec(s.Start)}-{Sec(s.End)}")) + $" -> {request.Output}");
        try
        {
            if (request.ReEncode)
            {
                await RunFfmpegAsync(BuildReEncodeArgs(request, segments, info.AudioTracks, tempOutput), total, 0, 1, progress, ct).ConfigureAwait(false);
            }
            else if (segments.Count == 1)
            {
                await RunFfmpegAsync(BuildCopyArgs(request.Input, segments[0], tempOutput, OutputFormat(request.Output)), total, 0, 1, progress, ct).ConfigureAwait(false);
            }
            else
            {
                await CopyConcatAsync(request, segments, total, tempOutput, progress, ct).ConfigureAwait(false);
            }
            if (!File.Exists(tempOutput)) throw new InvalidOperationException("ffmpeg produced no output.");
            File.Move(tempOutput, request.Output, overwrite: true);
        }
        catch
        {
            try { File.Delete(tempOutput); } catch { /* best effort */ }
            throw;
        }
        progress?.Report(1);
        return request.Output;
    }

    private static string OutputFormat(string output) =>
        Path.GetExtension(output).Equals(".mkv", StringComparison.OrdinalIgnoreCase) ? "matroska" : "mp4";

    private static IEnumerable<string> ContainerArgs(string format) =>
        format == "mp4" ? new[] { "-movflags", "+faststart", "-f", "mp4" } : new[] { "-f", format };

    public static List<string> BuildCopyArgs(string input, CutSegment segment, string output, string format)
    {
        var args = new List<string> { "-hide_banner", "-loglevel", "error", "-nostats", "-progress", "pipe:2", "-y" };
        if (segment.Start > TimeSpan.Zero) args.AddRange(new[] { "-ss", Sec(segment.Start) });
        args.AddRange(new[] { "-i", input, "-t", Sec(segment.Duration), "-map", "0:v:0", "-map", "0:a?", "-c", "copy", "-avoid_negative_ts", "make_zero" });
        args.AddRange(ContainerArgs(format));
        args.Add(output);
        return args;
    }

    /// <summary>Lossless multi-cut: each segment is stream-copied to a temporary MKV, then joined with the concat demuxer.</summary>
    private static async Task CopyConcatAsync(EditRequest request, IReadOnlyList<CutSegment> segments, double total, string output,
        IProgress<double>? progress, CancellationToken ct)
    {
        var workDir = Path.Combine(Path.GetTempPath(), "duelcliping-cut-" + Guid.NewGuid().ToString("N"));
        Directory.CreateDirectory(workDir);
        try
        {
            var parts = new List<string>();
            double done = 0;
            for (int i = 0; i < segments.Count; i++)
            {
                // Matroska parts join without timestamp collisions (TS/MP4 parts produce duplicate DTS at the seams).
                var part = Path.Combine(workDir, $"part{i:000}.mkv");
                double share = segments[i].Duration.TotalSeconds / total * 0.8;
                await RunFfmpegAsync(BuildCopyArgs(request.Input, segments[i], part, "matroska"), segments[i].Duration.TotalSeconds, done, share, progress, ct)
                    .ConfigureAwait(false);
                done += share;
                parts.Add(part);
            }
            var list = Path.Combine(workDir, "list.txt");
            await File.WriteAllLinesAsync(list, parts.Select(p => $"file '{p.Replace("'", "'\\''")}'"), ct).ConfigureAwait(false);
            var args = new List<string>
            {
                "-hide_banner", "-loglevel", "error", "-nostats", "-progress", "pipe:2", "-y",
                "-f", "concat", "-safe", "0", "-i", list,
                "-map", "0:v:0", "-map", "0:a?", "-c", "copy", "-avoid_negative_ts", "make_zero",
            };
            args.AddRange(ContainerArgs(OutputFormat(request.Output)));
            args.Add(output);
            await RunFfmpegAsync(args, total, 0.8, 0.2, progress, ct).ConfigureAwait(false);
        }
        finally
        {
            try { Directory.Delete(workDir, true); } catch { /* temp */ }
        }
    }

    /// <summary>Exact multi-cut: trim/atrim per segment and the concat filter, encoded once.</summary>
    public static List<string> BuildReEncodeArgs(EditRequest r, IReadOnlyList<CutSegment> segments, int audioTracks, string output)
    {
        var encoder = r.Encoder ?? new EncoderInfo(EncoderVendor.Software, VideoCodec.H264, "libx264");
        var graph = new List<string>();
        var concatInputs = new System.Text.StringBuilder();
        for (int i = 0; i < segments.Count; i++)
        {
            var s = segments[i];
            graph.Add($"[0:v:0]trim=start={Sec(s.Start)}:end={Sec(s.End)},setpts=PTS-STARTPTS[v{i}]");
            concatInputs.Append($"[v{i}]");
            for (int a = 0; a < audioTracks; a++)
            {
                graph.Add($"[0:a:{a}]atrim=start={Sec(s.Start)}:end={Sec(s.End)},asetpts=PTS-STARTPTS[a{i}_{a}]");
                concatInputs.Append($"[a{i}_{a}]");
            }
        }
        var outLabels = "[vcat]" + string.Concat(Enumerable.Range(0, audioTracks).Select(a => $"[acat{a}]"));
        graph.Add($"{concatInputs}concat=n={segments.Count}:v=1:a={audioTracks}{outLabels}");

        var post = new List<string>();
        if (r.MaxHeight is { } h) post.Add($"scale=-2:'min({h},ih)':flags=bicubic");
        if (r.FrameRate is { } fps) post.Add($"fps={fps}");
        post.Add(encoder.IsHardware ? "format=nv12" : "format=yuv420p");
        graph.Add($"[vcat]{string.Join(',', post)}[vout]");

        var args = new List<string>
        {
            "-hide_banner", "-loglevel", "error", "-nostats", "-progress", "pipe:2", "-y",
            "-i", r.Input,
            "-filter_complex", string.Join(';', graph),
            "-map", "[vout]",
        };
        for (int a = 0; a < audioTracks; a++) args.AddRange(new[] { "-map", $"[acat{a}]" });
        args.AddRange(new[]
        {
            "-c:v", encoder.FfmpegName,
            "-b:v", $"{r.BitrateKbps}k", "-maxrate", $"{r.BitrateKbps * 3 / 2}k", "-bufsize", $"{r.BitrateKbps * 2}k",
        });
        args.AddRange(encoder.Vendor switch
        {
            EncoderVendor.Nvidia => new[] { "-preset", "p5", "-rc", "vbr" },
            EncoderVendor.Amd => new[] { "-quality", "quality", "-rc", "vbr_peak" },
            EncoderVendor.Intel => new[] { "-preset", "medium" },
            _ => new[] { "-preset", "fast" },
        });
        if (audioTracks > 0) args.AddRange(new[] { "-c:a", "aac", "-b:a", "192k" });
        args.AddRange(ContainerArgs(OutputFormat(r.Output)));
        args.Add(output);
        return args;
    }

    private static async Task RunFfmpegAsync(List<string> args, double seconds, double offset, double share, IProgress<double>? progress, CancellationToken ct)
    {
        var result = await FfmpegRunner.RunFfmpegAsync(args, ct, TimeSpan.FromHours(2), System.Diagnostics.ProcessPriorityClass.BelowNormal, line =>
        {
            if (progress == null || seconds <= 0 || !line.StartsWith("out_time_us=")) return;
            if (long.TryParse(line.AsSpan(12), NumberStyles.Integer, Inv, out var us))
                progress.Report(offset + share * Math.Clamp(us / 1e6 / seconds, 0, 1));
        }).ConfigureAwait(false);
        if (!result.Success) throw new InvalidOperationException("Cutting failed: " + result.ErrorSummary);
    }

    /// <summary>Output path next to the source: "Name (cut).mp4", "Name (cut 2).mp4", ...</summary>
    public static string SuggestOutputPath(string input, string suffix = "cut")
    {
        var dir = Path.GetDirectoryName(input)!;
        var name = Path.GetFileNameWithoutExtension(input);
        var ext = Path.GetExtension(input).Equals(".mkv", StringComparison.OrdinalIgnoreCase) ? ".mkv" : ".mp4";
        var path = Path.Combine(dir, $"{name} ({suffix}){ext}");
        for (int i = 2; File.Exists(path); i++) path = Path.Combine(dir, $"{name} ({suffix} {i}){ext}");
        return path;
    }
}
