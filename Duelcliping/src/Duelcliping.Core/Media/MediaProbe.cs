using System.Globalization;
using System.Text.Json;
using Duelcliping.Core.Logging;

namespace Duelcliping.Core.Media;

public sealed record MediaInfo(double DurationSeconds, int Width, int Height, double FrameRate, string VideoCodec, int AudioTracks);

/// <summary>Reads stream information with ffprobe.</summary>
public static class MediaProbe
{
    public static async Task<MediaInfo?> ProbeAsync(string file, CancellationToken ct = default)
    {
        FfmpegResult result;
        try
        {
            result = await FfmpegRunner.RunFfprobeAsync(new[]
            {
                "-v", "error", "-print_format", "json", "-show_format", "-show_streams", file,
            }, ct).ConfigureAwait(false);
        }
        catch (FileNotFoundException ex)
        {
            Log.Error("Probe", ex.Message);
            return null;
        }
        if (!result.Success)
        {
            Log.Warn("Probe", $"ffprobe failed for {file}: {result.ErrorSummary}");
            return null;
        }
        try
        {
            using var doc = JsonDocument.Parse(result.StdOut);
            var root = doc.RootElement;
            double duration = 0;
            if (root.TryGetProperty("format", out var format) && format.TryGetProperty("duration", out var d))
                double.TryParse(d.GetString(), NumberStyles.Float, CultureInfo.InvariantCulture, out duration);
            int width = 0, height = 0, audio = 0;
            double fps = 0;
            string codec = "";
            if (root.TryGetProperty("streams", out var streams))
            {
                foreach (var s in streams.EnumerateArray())
                {
                    var type = s.TryGetProperty("codec_type", out var t) ? t.GetString() : null;
                    if (type == "audio") audio++;
                    if (type != "video" || width != 0) continue;
                    width = s.TryGetProperty("width", out var w) ? w.GetInt32() : 0;
                    height = s.TryGetProperty("height", out var h) ? h.GetInt32() : 0;
                    codec = s.TryGetProperty("codec_name", out var c) ? c.GetString() ?? "" : "";
                    fps = ParseRate(s.TryGetProperty("avg_frame_rate", out var r) ? r.GetString() : null);
                    if (fps <= 0) fps = ParseRate(s.TryGetProperty("r_frame_rate", out var rr) ? rr.GetString() : null);
                    if (duration <= 0 && s.TryGetProperty("duration", out var sd))
                        double.TryParse(sd.GetString(), NumberStyles.Float, CultureInfo.InvariantCulture, out duration);
                }
            }
            return new MediaInfo(duration, width, height, Math.Round(fps, 2), codec, audio);
        }
        catch (Exception ex) when (ex is JsonException or InvalidOperationException or FormatException)
        {
            Log.Warn("Probe", $"Unexpected ffprobe output for {file}: {ex.Message}");
            return null;
        }
    }

    public static double ParseRate(string? rate)
    {
        if (string.IsNullOrEmpty(rate)) return 0;
        var parts = rate.Split('/');
        if (parts.Length == 2 &&
            double.TryParse(parts[0], NumberStyles.Float, CultureInfo.InvariantCulture, out var n) &&
            double.TryParse(parts[1], NumberStyles.Float, CultureInfo.InvariantCulture, out var dd) && dd > 0)
            return n / dd;
        return double.TryParse(rate, NumberStyles.Float, CultureInfo.InvariantCulture, out var v) ? v : 0;
    }
}
