using Duelcliping.Core.Encoders;
using Duelcliping.Core.Recording;
using Duelcliping.Core.Settings;

namespace Duelcliping.Core.Tests;

public class PipelineTests
{
    private static CaptureRequest Request(int width, int height, CaptureResolution res, EncoderVendor vendor = EncoderVendor.Nvidia) => new()
    {
        Source = CaptureSource.DesktopDuplication,
        Monitor = MonitorTarget.Primary(width, height),
        Encoder = new EncoderSelection(new EncoderInfo(vendor, VideoCodec.H264, vendor == EncoderVendor.Nvidia ? "h264_nvenc" : "h264_amf"), null),
        Resolution = res,
        FrameRate = 30,
        Quality = QualityPreset.Medium,
    };

    [Fact]
    public void Scaling_tries_the_video_processor_first_then_cuda_then_system_memory()
    {
        var plan = RecordingEngine.BuildPlan(Request(2560, 1440, CaptureResolution.P1080));
        Assert.Equal(new[] { PipelinePath.VideoProcessor, PipelinePath.ZeroCopy, PipelinePath.SystemMemory }, plan.Select(p => p.Path));
        var args = string.Join(' ', FfmpegCommandBuilder.BuildCaptureArgs(RecordingEngine.ToPipelineConfig(
            Request(2560, 1440, CaptureResolution.P1080), CaptureSource.DesktopDuplication, PipelinePath.VideoProcessor, plan[0].Encoder)));
        Assert.Contains("scale_d3d11=width=1920:height=1080", args);
        Assert.DoesNotContain("cuda", args);
        Assert.DoesNotContain("gdigrab", args);
    }

    [Fact]
    public void Native_resolution_needs_no_scaler_at_all()
    {
        var plan = RecordingEngine.BuildPlan(Request(1920, 1080, CaptureResolution.Native));
        Assert.Equal(PipelinePath.ZeroCopy, plan[0].Path);
        var args = string.Join(' ', FfmpegCommandBuilder.BuildCaptureArgs(RecordingEngine.ToPipelineConfig(
            Request(1920, 1080, CaptureResolution.Native), CaptureSource.DesktopDuplication, PipelinePath.ZeroCopy, plan[0].Encoder)));
        Assert.DoesNotContain("scale", args);
        Assert.DoesNotContain("hwdownload", args);
    }

    [Fact]
    public void No_cpu_encoder_fallback_unless_requested()
    {
        var plan = RecordingEngine.BuildPlan(Request(2560, 1440, CaptureResolution.P1080));
        Assert.All(plan, p => Assert.True(p.Encoder.IsHardware));
    }
}
