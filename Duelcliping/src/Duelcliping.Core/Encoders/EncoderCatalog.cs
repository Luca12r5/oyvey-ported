using System.Text.Json;
using Duelcliping.Core.Logging;
using Duelcliping.Core.Media;
using Duelcliping.Core.Settings;

namespace Duelcliping.Core.Encoders;

public enum EncoderVendor { Nvidia, Amd, Intel, Software }

public sealed record EncoderInfo(EncoderVendor Vendor, VideoCodec Codec, string FfmpegName)
{
    public bool IsHardware => Vendor != EncoderVendor.Software;

    public string DisplayName => Vendor switch
    {
        EncoderVendor.Nvidia => $"NVIDIA NVENC {CodecName}",
        EncoderVendor.Amd => $"AMD AMF {CodecName}",
        EncoderVendor.Intel => $"Intel Quick Sync {CodecName}",
        _ => $"Software (CPU) {CodecName}",
    };

    public string CodecName => Codec switch
    {
        VideoCodec.H264 => "H.264",
        VideoCodec.Hevc => "HEVC",
        _ => "AV1",
    };
}

public sealed record EncoderSelection(EncoderInfo Encoder, string? Notice);

/// <summary>
/// Detects which encoders really work on this machine. Being listed by <c>ffmpeg -encoders</c> is not
/// enough (every build lists NVENC), so each candidate encodes a few frames as a functional test.
/// Results are cached on disk so start-up does not pay for the probes every time.
/// </summary>
public sealed class EncoderCatalog
{
    private static readonly (EncoderVendor Vendor, VideoCodec Codec, string Name)[] Candidates =
    {
        (EncoderVendor.Nvidia, VideoCodec.H264, "h264_nvenc"),
        (EncoderVendor.Nvidia, VideoCodec.Hevc, "hevc_nvenc"),
        (EncoderVendor.Nvidia, VideoCodec.Av1, "av1_nvenc"),
        (EncoderVendor.Amd, VideoCodec.H264, "h264_amf"),
        (EncoderVendor.Amd, VideoCodec.Hevc, "hevc_amf"),
        (EncoderVendor.Amd, VideoCodec.Av1, "av1_amf"),
        (EncoderVendor.Intel, VideoCodec.H264, "h264_qsv"),
        (EncoderVendor.Intel, VideoCodec.Hevc, "hevc_qsv"),
        (EncoderVendor.Intel, VideoCodec.Av1, "av1_qsv"),
        (EncoderVendor.Software, VideoCodec.H264, "libx264"),
    };

    private readonly string _cachePath;
    private List<EncoderInfo> _available = new();

    public EncoderCatalog(string cachePath) => _cachePath = cachePath;

    public IReadOnlyList<EncoderInfo> Available => _available;
    public bool IsDetected { get; private set; }

    public async Task DetectAsync(bool forceRefresh, CancellationToken ct = default)
    {
        if (!forceRefresh && TryLoadCache()) { IsDetected = true; return; }

        var listed = await FfmpegRunner.RunFfmpegAsync(new[] { "-hide_banner", "-encoders" }, ct, TimeSpan.FromSeconds(20)).ConfigureAwait(false);
        if (!listed.Success) throw new InvalidOperationException("ffmpeg could not be started to list encoders: " + listed.ErrorSummary);

        // Probe one encoder at a time: consumer GPUs allow only a few concurrent NVENC/AMF sessions,
        // and other apps (Discord, Steam, browsers) may already hold some. Parallel probes made
        // working encoders look broken. A failing hardware encoder gets one more try.
        var found = new List<EncoderInfo>();
        foreach (var c in Candidates.Where(c => listed.StdOut.Contains(" " + c.Name + " ", StringComparison.Ordinal)))
        {
            // Skip further codecs of a vendor whose H.264 encoder does not work at all.
            if (c.Codec != VideoCodec.H264 && c.Vendor != EncoderVendor.Software &&
                !found.Any(e => e.Vendor == c.Vendor)) continue;
            bool ok = await ProbeAsync(c.Name, c.Codec, ct).ConfigureAwait(false);
            if (!ok && c.Vendor != EncoderVendor.Software && c.Codec == VideoCodec.H264)
            {
                await Task.Delay(1000, ct).ConfigureAwait(false);
                ok = await ProbeAsync(c.Name, c.Codec, ct).ConfigureAwait(false);
            }
            if (ok) found.Add(new EncoderInfo(c.Vendor, c.Codec, c.Name));
        }

        _available = found;
        IsDetected = true;
        Log.Info("Encoders", "Working encoders: " + (_available.Count == 0 ? "none" : string.Join(", ", _available.Select(e => e.FfmpegName))));
        // Never remember a result without a hardware encoder: a temporary driver/GPU problem must not
        // leave recording stuck on the CPU. The next start probes again.
        if (HasHardware) SaveCache();
        else TryDeleteCache();
    }

    public bool HasHardware => _available.Any(e => e.IsHardware);

    private void TryDeleteCache()
    {
        try { if (File.Exists(_cachePath)) File.Delete(_cachePath); }
        catch (IOException) { }
    }

    private static async Task<bool> ProbeAsync(string encoder, VideoCodec codec, CancellationToken ct)
    {
        // AV1 is buffered as MPEG-TS like everything else; older ffmpeg builds can mux but not demux
        // AV1 in TS, so for AV1 the probe writes a real TS file and reads it back.
        string? tsFile = codec == VideoCodec.Av1 ? Path.Combine(Path.GetTempPath(), $"moments-probe-{Guid.NewGuid():N}.ts") : null;
        var args = new List<string>
        {
            "-hide_banner", "-loglevel", "error",
            "-f", "lavfi", "-i", "color=c=black:s=1280x720:r=30",
            "-frames:v", "5", "-pix_fmt", "nv12", "-c:v", encoder, "-b:v", "2M",
        };
        args.AddRange(tsFile == null ? new[] { "-f", "null", "-" } : new[] { "-y", "-f", "mpegts", tsFile });
        try
        {
            var result = await FfmpegRunner.RunFfmpegAsync(args, ct, TimeSpan.FromSeconds(20)).ConfigureAwait(false);
            if (!result.Success)
            {
                Log.Info("Encoders", $"{encoder} is not usable: {result.ErrorSummary}");
                return false;
            }
            if (tsFile == null) return true;
            bool readable = await TransportStreamCarriesAv1Async(tsFile, ct).ConfigureAwait(false);
            if (!readable) Log.Info("Encoders", $"{encoder} works, but this ffmpeg build cannot read AV1 from MPEG-TS; AV1 disabled");
            return readable;
        }
        catch (Exception ex) when (ex is not OperationCanceledException)
        {
            Log.Warn("Encoders", $"Probe of {encoder} failed: {ex.Message}");
            return false;
        }
        finally
        {
            if (tsFile != null) try { File.Delete(tsFile); } catch { /* temp file */ }
        }
    }

    public static async Task<bool> TransportStreamCarriesAv1Async(string tsFile, CancellationToken ct = default)
    {
        var probe = await FfmpegRunner.RunFfprobeAsync(new[] { "-v", "error", "-select_streams", "v:0", "-show_entries", "stream=codec_name", "-of", "csv=p=0", tsFile }, ct)
            .ConfigureAwait(false);
        return probe.Success && probe.StdOut.Trim() == "av1";
    }

    /// <summary>
    /// Picks the encoder for the requested codec and vendor preference. Hardware always wins over
    /// software: if the requested codec has no hardware encoder, a hardware H.264 encoder is used
    /// instead and a notice explains why.
    /// </summary>
    public EncoderSelection Select(VideoCodec codec, EncoderPreference preference)
    {
        if (_available.Count == 0)
            throw new InvalidOperationException("No working video encoder was found. Reinstall Moments or update your graphics driver.");

        var vendorOrder = new List<EncoderVendor> { EncoderVendor.Nvidia, EncoderVendor.Amd, EncoderVendor.Intel };
        EncoderVendor? preferred = preference switch
        {
            EncoderPreference.Nvidia => EncoderVendor.Nvidia,
            EncoderPreference.Amd => EncoderVendor.Amd,
            EncoderPreference.Intel => EncoderVendor.Intel,
            EncoderPreference.Software => EncoderVendor.Software,
            _ => null,
        };

        if (preferred == EncoderVendor.Software)
        {
            var sw = _available.FirstOrDefault(e => e.Vendor == EncoderVendor.Software);
            if (sw != null)
                return new EncoderSelection(sw, codec == VideoCodec.H264 ? "Software encoding uses the CPU and can lower game FPS." :
                    "Software encoding only supports H.264. Software encoding uses the CPU and can lower game FPS.");
        }
        if (preferred is { } p && p != EncoderVendor.Software)
        {
            vendorOrder.Remove(p);
            vendorOrder.Insert(0, p);
        }

        foreach (var vendor in vendorOrder)
        {
            var match = _available.FirstOrDefault(e => e.Vendor == vendor && e.Codec == codec);
            if (match != null)
            {
                string? notice = preferred is { } want && want != vendor ? $"{want} encoder is not available; using {match.DisplayName}." : null;
                return new EncoderSelection(match, notice);
            }
        }

        foreach (var vendor in vendorOrder)
        {
            var h264 = _available.FirstOrDefault(e => e.Vendor == vendor && e.Codec == VideoCodec.H264);
            if (h264 != null)
                return new EncoderSelection(h264, $"Your GPU has no hardware {CodecLabel(codec)} encoder; using {h264.DisplayName} instead.");
        }

        var fallback = _available.First(e => e.Vendor == EncoderVendor.Software);
        return new EncoderSelection(fallback, "No hardware encoder was found. Recording uses the CPU (libx264), which can lower game FPS.");
    }

    private static string CodecLabel(VideoCodec codec) => codec switch
    {
        VideoCodec.H264 => "H.264",
        VideoCodec.Hevc => "HEVC",
        _ => "AV1",
    };

    private sealed record CacheFile(string FfmpegPath, long FfmpegSize, DateTime WrittenUtc, List<EncoderInfo> Encoders);

    private bool TryLoadCache()
    {
        try
        {
            if (!File.Exists(_cachePath)) return false;
            var cache = JsonSerializer.Deserialize<CacheFile>(File.ReadAllText(_cachePath));
            if (cache == null || cache.FfmpegPath != FfmpegLocator.Ffmpeg || cache.FfmpegSize != FfmpegSize()) return false;
            if (DateTime.UtcNow - cache.WrittenUtc > TimeSpan.FromDays(7) || !cache.Encoders.Any(e => e.IsHardware)) return false;
            _available = cache.Encoders;
            Log.Info("Encoders", "Using cached encoder detection: " + string.Join(", ", _available.Select(e => e.FfmpegName)));
            return true;
        }
        catch (Exception ex)
        {
            Log.Warn("Encoders", "Ignoring unreadable encoder cache: " + ex.Message);
            return false;
        }
    }

    private void SaveCache()
    {
        try
        {
            Directory.CreateDirectory(Path.GetDirectoryName(_cachePath)!);
            File.WriteAllText(_cachePath, JsonSerializer.Serialize(new CacheFile(FfmpegLocator.Ffmpeg, FfmpegSize(), DateTime.UtcNow, _available)));
        }
        catch (Exception ex)
        {
            Log.Warn("Encoders", "Could not write encoder cache: " + ex.Message);
        }
    }

    private static long FfmpegSize()
    {
        try { return File.Exists(FfmpegLocator.Ffmpeg) ? new FileInfo(FfmpegLocator.Ffmpeg).Length : 0; }
        catch { return 0; }
    }

    /// <summary>Allows tests to inject a known encoder list.</summary>
    internal void SetAvailableForTesting(IEnumerable<EncoderInfo> encoders)
    {
        _available = encoders.ToList();
        IsDetected = true;
    }
}
