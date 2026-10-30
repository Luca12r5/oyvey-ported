using System.Globalization;
using Duelcliping.Core.Settings;

namespace Duelcliping.Core.Encoders;

/// <summary>
/// Translates a <see cref="PipelineConfig"/> into the ffmpeg command line of the capture process.
/// The process writes an MPEG transport stream to stdout, which feeds the in-memory replay buffer.
/// </summary>
public static class FfmpegCommandBuilder
{
    private static string I(double v) => v.ToString(CultureInfo.InvariantCulture);

    public static List<string> BuildCaptureArgs(PipelineConfig c)
    {
        var args = new List<string>
        {
            "-hide_banner", "-loglevel", "warning", "-nostats",
            "-progress", "pipe:2", "-stats_period", "1",
        };

        var (width, height) = c.OutputSize;
        int audioInputOffset = 0;
        string videoGraph;

        switch (c.Source)
        {
            case CaptureSource.DesktopDuplication:
                if (c.Monitor.AdapterIndex > 0)
                {
                    // ddagrab captures outputs of the adapter its D3D11 device lives on.
                    args.AddRange(new[] { "-init_hw_device", $"d3d11va=dda:{c.Monitor.AdapterIndex}", "-filter_hw_device", "dda" });
                }
                videoGraph = $"ddagrab=output_idx={c.Monitor.OutputIndex}:framerate={c.FrameRate}:draw_mouse={(c.CaptureCursor ? 1 : 0)}:dup_frames=1"
                             + DdaVideoChain(c, width, height);
                break;
            case CaptureSource.Gdi:
                args.AddRange(new[]
                {
                    "-f", "gdigrab", "-framerate", c.FrameRate.ToString(CultureInfo.InvariantCulture),
                    "-offset_x", c.Monitor.Left.ToString(CultureInfo.InvariantCulture),
                    "-offset_y", c.Monitor.Top.ToString(CultureInfo.InvariantCulture),
                    "-video_size", $"{c.Monitor.Width}x{c.Monitor.Height}",
                    "-draw_mouse", c.CaptureCursor ? "1" : "0",
                    "-thread_queue_size", "64",
                    "-i", "desktop",
                });
                audioInputOffset = 1;
                videoGraph = "[0:v]" + SystemMemoryChain(c, width, height);
                break;
            default:
                videoGraph = $"testsrc2=size={c.Monitor.Width}x{c.Monitor.Height}:rate={c.FrameRate},realtime"
                             + "," + SystemMemoryChain(c, width, height);
                break;
        }

        foreach (var audio in c.AudioInputs)
        {
            args.AddRange(new[]
            {
                "-f", "f32le", "-ar", audio.SampleRate.ToString(CultureInfo.InvariantCulture),
                "-ac", audio.Channels.ToString(CultureInfo.InvariantCulture),
                "-thread_queue_size", "1024", "-i", audio.Path,
            });
        }

        var filter = videoGraph + "[v]";
        bool mixAudio = !c.SeparateAudioTracks && c.AudioInputs.Count > 1;
        if (mixAudio)
        {
            var inputs = string.Concat(Enumerable.Range(0, c.AudioInputs.Count).Select(i => $"[{i + audioInputOffset}:a]"));
            filter += $";{inputs}amix=inputs={c.AudioInputs.Count}:duration=longest:normalize=0[aout]";
        }
        args.AddRange(new[] { "-filter_complex", filter, "-map", "[v]" });

        if (mixAudio) args.AddRange(new[] { "-map", "[aout]" });
        else for (int i = 0; i < c.AudioInputs.Count; i++) args.AddRange(new[] { "-map", $"{i + audioInputOffset}:a" });

        args.AddRange(EncoderArgs(c));

        if (c.AudioInputs.Count > 0)
            args.AddRange(new[] { "-c:a", "aac", "-b:a", "160k", "-ar", "48000" });

        args.AddRange(new[]
        {
            "-max_muxing_queue_size", "1024",
            "-muxdelay", "0", "-muxpreload", "0",
            "-f", "mpegts", "pipe:1",
        });
        return args;
    }

    private static string DdaVideoChain(PipelineConfig c, int width, int height)
    {
        if (c.Path == PipelinePath.SystemMemory || !c.Encoder.IsHardware)
            return ",hwdownload,format=bgra," + SystemMemoryChain(c, width, height);

        return c.Encoder.Vendor switch
        {
            EncoderVendor.Nvidia => c.NeedsScaling ? $",hwmap=derive_device=cuda,scale_cuda=w={width}:h={height}" : "",
            EncoderVendor.Amd => c.NeedsScaling ? $",vpp_amf=w={width}:h={height}" : "",
            EncoderVendor.Intel => ",hwmap=derive_device=qsv,format=qsv,vpp_qsv=" + (c.NeedsScaling ? $"w={width}:h={height}:" : "") + "format=nv12",
            _ => ",hwdownload,format=bgra," + SystemMemoryChain(c, width, height),
        };
    }

    private static string SystemMemoryChain(PipelineConfig c, int width, int height)
    {
        string pixelFormat = c.Encoder.IsHardware ? "nv12" : "yuv420p";
        return $"scale=w={width}:h={height}:flags=fast_bilinear,format={pixelFormat}";
    }

    public static List<string> EncoderArgs(PipelineConfig c)
    {
        int bitrate = c.BitrateKbps;
        int gop = c.FrameRate; // one keyframe per second: clip starts are at most 1s early
        var args = new List<string>
        {
            "-c:v", c.Encoder.FfmpegName,
            "-g", gop.ToString(CultureInfo.InvariantCulture),
            "-b:v", $"{bitrate}k",
            "-maxrate", $"{(int)(bitrate * 1.5)}k",
            "-bufsize", $"{bitrate * 2}k",
        };

        bool fast = c.PerformanceMode;
        switch (c.Encoder.Vendor)
        {
            case EncoderVendor.Nvidia:
                // p1 + ultra-low-latency: the least encoder work per frame, no look-ahead, no B-frames.
                args.AddRange(new[] { "-preset", fast ? "p1" : "p3", "-tune", fast ? "ull" : "ll", "-rc", "vbr", "-rc-lookahead", "0", "-bf", "0",
                    "-multipass", "disabled", "-spatial-aq", "0", "-temporal-aq", "0" });
                break;
            case EncoderVendor.Amd:
                args.AddRange(new[] { "-usage", "lowlatency", "-quality", fast ? "speed" : "balanced", "-rc", "vbr_peak", "-bf", "0" });
                break;
            case EncoderVendor.Intel:
                args.AddRange(new[] { "-preset", fast ? "veryfast" : "faster", "-bf", "0" });
                break;
            default:
                args.AddRange(new[] { "-preset", fast ? "ultrafast" : "veryfast", "-tune", "zerolatency", "-bf", "0" });
                break;
        }

        if (c.Encoder.Codec != VideoCodec.Av1)
        {
            // Put SPS/PPS(/VPS) in front of every keyframe so any keyframe in the replay buffer can start a clip.
            args.AddRange(new[] { "-flags:v", "+global_header", "-bsf:v", "dump_extra=freq=keyframe" });
        }
        return args;
    }

    /// <summary>Arguments to turn a buffered transport stream into the final clip container (stream copy only).</summary>
    public static List<string> BuildRemuxArgs(string inputTs, string output, ContainerFormat container, IReadOnlyList<string> audioTrackTitles)
    {
        var args = new List<string>
        {
            "-hide_banner", "-loglevel", "error", "-y",
            "-fflags", "+genpts+discardcorrupt",
            "-i", inputTs,
            "-map", "0:v:0", "-map", "0:a?",
            "-c", "copy",
            "-avoid_negative_ts", "make_zero",
        };
        for (int i = 0; i < audioTrackTitles.Count; i++)
            args.AddRange(new[] { $"-metadata:s:a:{i}", $"title={audioTrackTitles[i]}" });
        if (container == ContainerFormat.Mp4)
            args.AddRange(new[] { "-movflags", "+faststart", "-f", "mp4" });
        else
            args.AddRange(new[] { "-f", "matroska" });
        args.Add(output);
        return args;
    }
}
