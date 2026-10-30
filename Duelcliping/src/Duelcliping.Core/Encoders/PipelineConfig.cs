using Duelcliping.Core.Settings;

namespace Duelcliping.Core.Encoders;

public enum CaptureSource
{
    /// <summary>DXGI Desktop Duplication via ffmpeg's ddagrab: frames stay in GPU memory.</summary>
    DesktopDuplication,
    /// <summary>GDI BitBlt capture: CPU based, last resort for systems without DXGI duplication.</summary>
    Gdi,
    /// <summary>Synthetic test pattern; used by the automated tests and the pipeline self-test.</summary>
    TestPattern,
}

/// <summary>
/// How frames travel from capture to encoder. The engine starts with the cheapest path and only
/// steps down when ffmpeg reports that the path is not supported on this machine.
/// </summary>
public enum PipelinePath
{
    /// <summary>
    /// Captured texture → GPU video processor (scale_d3d11, fixed-function hardware) → hardware encoder.
    /// Scaling does not use the shader/CUDA cores the game renders with.
    /// </summary>
    VideoProcessor,
    /// <summary>Captured texture → (GPU scale) → hardware encoder. No copy through system memory.</summary>
    ZeroCopy,
    /// <summary>Captured texture → system memory → CPU scale/convert → encoder. Works everywhere.</summary>
    SystemMemory,
}

public sealed record AudioInput(string Path, string Title, int SampleRate = 48000, int Channels = 2);

public sealed record MonitorTarget(int AdapterIndex, int OutputIndex, int Left, int Top, int Width, int Height, string DeviceName)
{
    public static MonitorTarget Primary(int width, int height) => new(0, 0, 0, 0, width, height, "");
}

public sealed record PipelineConfig
{
    public required CaptureSource Source { get; init; }
    public required PipelinePath Path { get; init; }
    public required MonitorTarget Monitor { get; init; }
    public required EncoderInfo Encoder { get; init; }
    public required CaptureResolution Resolution { get; init; }
    public required int FrameRate { get; init; }
    public required QualityPreset Quality { get; init; }
    public int CustomBitrateKbps { get; init; } = 20000;
    public bool CaptureCursor { get; init; } = true;
    public bool PerformanceMode { get; init; } = true;
    public bool SeparateAudioTracks { get; init; } = true;
    public IReadOnlyList<AudioInput> AudioInputs { get; init; } = Array.Empty<AudioInput>();

    public (int Width, int Height) OutputSize => ComputeOutputSize(Monitor.Width, Monitor.Height, Resolution);
    public bool NeedsScaling => OutputSize != (Monitor.Width, Monitor.Height);
    public int BitrateKbps => ComputeBitrateKbps(Quality, CustomBitrateKbps, OutputSize.Width, OutputSize.Height, FrameRate, Encoder.Codec);

    public static int TargetHeight(CaptureResolution resolution) => resolution switch
    {
        CaptureResolution.P720 => 720,
        CaptureResolution.P1080 => 1080,
        CaptureResolution.P1440 => 1440,
        CaptureResolution.P2160 => 2160,
        _ => 0,
    };

    /// <summary>
    /// Output size for a capture of <paramref name="nativeWidth"/>x<paramref name="nativeHeight"/>.
    /// Keeps the aspect ratio, never upscales and always returns even dimensions (required by 4:2:0).
    /// </summary>
    public static (int Width, int Height) ComputeOutputSize(int nativeWidth, int nativeHeight, CaptureResolution resolution)
    {
        int targetHeight = TargetHeight(resolution);
        if (targetHeight == 0 || targetHeight >= nativeHeight)
            return (nativeWidth & ~1, nativeHeight & ~1);
        int width = (int)Math.Round(nativeWidth * (double)targetHeight / nativeHeight);
        return (width & ~1, targetHeight & ~1);
    }

    /// <summary>Bitrate for a quality preset, scaled from a 1080p60 H.264 reference by pixels and frame rate.</summary>
    public static int ComputeBitrateKbps(QualityPreset quality, int customKbps, int width, int height, int fps, VideoCodec codec)
    {
        if (quality == QualityPreset.Custom) return Math.Clamp(customKbps, 1000, 150_000);
        double reference = quality switch
        {
            QualityPreset.Low => 8000,
            QualityPreset.Medium => 15000,
            QualityPreset.High => 25000,
            _ => 40000,
        };
        double pixelFactor = Math.Pow(width * (double)height / (1920 * 1080), 0.85);
        double fpsFactor = Math.Pow(fps / 60.0, 0.7);
        double codecFactor = codec == VideoCodec.H264 ? 1.0 : 0.7;
        return (int)Math.Clamp(reference * pixelFactor * fpsFactor * codecFactor, 1500, 150_000);
    }
}
