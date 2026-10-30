using System.Text.Json;
using Duelcliping.Core.Audio;
using Duelcliping.Core.Media;

namespace Duelcliping.Core.Tests;

/// <summary>Generates a 440 Hz tone in 10 ms packets from its own thread, like a real capture device.</summary>
public sealed class ToneSource : IAudioCaptureSource
{
    private readonly int _rate;
    private Thread? _thread;
    private volatile bool _running;

    public ToneSource(int rate = 48000) => _rate = rate;
    public string Name => "Test tone";

    public void Start(AudioPump pump)
    {
        _running = true;
        _thread = new Thread(() =>
        {
            var format = new AudioFormat(_rate, 2, 16, false);
            int frames = _rate / 100;
            var data = new byte[frames * 4];
            long n = 0;
            var sw = System.Diagnostics.Stopwatch.StartNew();
            long sent = 0;
            while (_running)
            {
                Thread.Sleep(5);
                while (sent < sw.Elapsed.TotalSeconds * _rate)
                {
                    for (int i = 0; i < frames; i++, n++)
                    {
                        short v = (short)(Math.Sin(2 * Math.PI * 440 * n / _rate) * 8000);
                        BitConverter.TryWriteBytes(data.AsSpan(i * 4), v);
                        BitConverter.TryWriteBytes(data.AsSpan(i * 4 + 2), v);
                    }
                    pump.Push(data, format);
                    sent += frames;
                }
            }
        }) { IsBackground = true };
        _thread.Start();
    }

    public void Stop() { _running = false; _thread?.Join(); }
    public void Dispose() => Stop();
}

public sealed class TempDir : IDisposable
{
    public TempDir() { Path = System.IO.Path.Combine(System.IO.Path.GetTempPath(), "moments-test-" + Guid.NewGuid().ToString("N")); Directory.CreateDirectory(Path); }
    public string Path { get; }
    public void Dispose() { try { Directory.Delete(Path, true); } catch { } }
}

public static class Probe
{
    public sealed record Info(double Duration, int VideoStreams, int AudioStreams, int Width, int Height, string FormatName);

    public static async Task<Info> RunAsync(string file)
    {
        var r = await FfmpegRunner.RunFfprobeAsync(new[] { "-v", "error", "-print_format", "json", "-show_format", "-show_streams", file });
        Assert.True(r.Success, r.StdErr);
        using var doc = JsonDocument.Parse(r.StdOut);
        var streams = doc.RootElement.GetProperty("streams").EnumerateArray().ToList();
        var video = streams.First(s => s.GetProperty("codec_type").GetString() == "video");
        var fmt = doc.RootElement.GetProperty("format");
        return new Info(
            double.Parse(fmt.GetProperty("duration").GetString()!, System.Globalization.CultureInfo.InvariantCulture),
            streams.Count(s => s.GetProperty("codec_type").GetString() == "video"),
            streams.Count(s => s.GetProperty("codec_type").GetString() == "audio"),
            video.GetProperty("width").GetInt32(), video.GetProperty("height").GetInt32(),
            fmt.GetProperty("format_name").GetString()!);
    }

    /// <summary>Decodes the whole file; returns ffmpeg's error output (empty when the file is clean).</summary>
    public static async Task<string> DecodeErrorsAsync(string file)
    {
        var r = await FfmpegRunner.RunFfmpegAsync(new[] { "-v", "error", "-i", file, "-f", "null", "-" });
        return r.Success ? r.StdErr.Trim() : "exit " + r.ExitCode + ": " + r.StdErr;
    }
}
