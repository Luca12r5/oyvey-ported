using Duelcliping.Core.Audio;
using Duelcliping.Core.Encoders;
using Duelcliping.Core.Recording;
using Duelcliping.Core.Settings;

namespace Duelcliping.Core.Tests;

/// <summary>
/// End-to-end tests: a real ffmpeg process encodes a live test pattern plus two audio tracks fed
/// through the audio pumps; clips and manual recordings are saved and verified with ffprobe.
/// </summary>
public class EngineTests
{
    private static CaptureRequest Request(bool replay = true, bool separate = true) => new()
    {
        Source = CaptureSource.TestPattern,
        Monitor = MonitorTarget.Primary(640, 360),
        Encoder = new EncoderSelection(new EncoderInfo(EncoderVendor.Software, VideoCodec.H264, "libx264"), null),
        Resolution = CaptureResolution.Native,
        FrameRate = 30,
        Quality = QualityPreset.Low,
        SeparateAudioTracks = separate,
        Audio = new[]
        {
            new AudioSourceSpec("game", "Game Audio", () => new ToneSource(44100), 1.0),
            new AudioSourceSpec("mic", "Microphone", () => null, 1.0), // missing device: silent track
        },
        ReplayEnabled = replay,
        BufferSeconds = 6,
    };

    private static async Task WaitFor(Func<bool> condition, TimeSpan timeout)
    {
        var until = DateTime.UtcNow + timeout;
        while (!condition())
        {
            if (DateTime.UtcNow > until) throw new TimeoutException();
            await Task.Delay(100);
        }
    }

    [Fact]
    public async Task Replay_buffer_records_and_saves_clip_with_two_audio_tracks()
    {
        using var dir = new TempDir();
        await using var engine = new RecordingEngine();
        await engine.ApplyAsync(Request());
        await WaitFor(() => engine.State == EngineState.Running, TimeSpan.FromSeconds(20));
        await Task.Delay(TimeSpan.FromSeconds(9));

        var status = engine.GetStatus();
        Assert.Equal(EngineState.Running, status.State);
        Assert.InRange(status.BufferedSeconds, 5, 7.5); // capped by the 6 s buffer
        Assert.True(status.Frames > 200, $"frames={status.Frames}");

        var result = await engine.SaveClipAsync(4, dir.Path, ContainerFormat.Mp4);
        Assert.True(result.Remuxed, result.Warning);
        var info = await Probe.RunAsync(result.FilePath);
        Assert.Equal(1, info.VideoStreams);
        Assert.Equal(2, info.AudioStreams);
        Assert.Equal(640, info.Width);
        Assert.InRange(info.Duration, 3.8, 5.6);
        Assert.Equal("", await Probe.DecodeErrorsAsync(result.FilePath));
    }

    [Fact]
    public async Task Manual_recording_captures_full_session_without_replay_buffer()
    {
        using var dir = new TempDir();
        await using var engine = new RecordingEngine();
        await engine.ApplyAsync(Request(replay: false, separate: false));
        Assert.Equal(EngineState.Stopped, engine.State); // nothing runs while idle

        await engine.StartManualRecordingAsync(dir.Path, ContainerFormat.Mkv);
        await WaitFor(() => engine.State == EngineState.Running, TimeSpan.FromSeconds(20));
        await Task.Delay(TimeSpan.FromSeconds(5));
        var results = await engine.StopManualRecordingAsync();

        Assert.Single(results);
        var file = results[0].FilePath;
        Assert.EndsWith(".mkv", file);
        var info = await Probe.RunAsync(file);
        Assert.Equal(1, info.AudioStreams); // mixed into one track
        Assert.InRange(info.Duration, 4, 7);
        Assert.Equal("", await Probe.DecodeErrorsAsync(file));
        Assert.Empty(Directory.GetFiles(dir.Path, "*.partial"));
        await WaitFor(() => engine.State == EngineState.Stopped, TimeSpan.FromSeconds(5));
    }

    [Fact]
    public async Task Manual_recording_runs_alongside_replay_buffer()
    {
        using var dir = new TempDir();
        await using var engine = new RecordingEngine();
        await engine.ApplyAsync(Request());
        await WaitFor(() => engine.State == EngineState.Running, TimeSpan.FromSeconds(20));
        await Task.Delay(2000);
        await engine.StartManualRecordingAsync(dir.Path, ContainerFormat.Mp4);
        await Task.Delay(4000);
        var clip = await engine.SaveClipAsync(3, dir.Path, ContainerFormat.Mp4);
        var results = await engine.StopManualRecordingAsync();
        Assert.Equal(EngineState.Running, engine.State); // replay buffer keeps running
        Assert.Single(results);
        var info = await Probe.RunAsync(results[0].FilePath);
        Assert.InRange(info.Duration, 3.5, 6.5);
        Assert.Equal("", await Probe.DecodeErrorsAsync(results[0].FilePath));
        Assert.True(File.Exists(clip.FilePath));
    }

    [Fact]
    public async Task Engine_restarts_after_encoder_process_dies()
    {
        await using var engine = new RecordingEngine();
        await engine.ApplyAsync(Request());
        await WaitFor(() => engine.State == EngineState.Running, TimeSpan.FromSeconds(20));
        var pid = engine.GetStatus().FfmpegProcessId!.Value;
        System.Diagnostics.Process.GetProcessById(pid).Kill();
        await WaitFor(() => engine.GetStatus().FfmpegProcessId is int p && p != pid && engine.State == EngineState.Running, TimeSpan.FromSeconds(20));
    }

    [Fact]
    public async Task Saving_with_buffer_disabled_gives_clear_error()
    {
        using var dir = new TempDir();
        await using var engine = new RecordingEngine();
        await engine.ApplyAsync(Request(replay: false));
        var ex = await Assert.ThrowsAsync<InvalidOperationException>(() => engine.SaveClipAsync(30, dir.Path, ContainerFormat.Mp4));
        Assert.Contains("turned off", ex.Message);
    }
}

public class AudioPumpTests
{
    [Fact]
    public async Task Pads_silence_in_real_time_when_device_delivers_nothing()
    {
        using var output = new MemoryStream();
        using var pump = new AudioPump("test", output, 1.0);
        var clock = System.Diagnostics.Stopwatch.StartNew();
        pump.Start();
        await Task.Delay(1000);
        pump.Dispose();
        double elapsed = clock.Elapsed.TotalSeconds;
        double seconds = output.Length / 8.0 / AudioPump.OutputRate;
        // The track must follow the real-time clock: never ahead of it, at most ~60 ms (pad threshold) + one tick behind.
        Assert.InRange(seconds, elapsed - 0.12, elapsed + 0.001);
        Assert.True(pump.SilenceFramesInserted > 0);
    }

    [Fact]
    public async Task Resamples_44k1_to_48k_at_correct_length_and_applies_volume()
    {
        using var output = new MemoryStream();
        using var pump = new AudioPump("test", output, 0.5);
        using var tone = new ToneSource(44100);
        var clock = System.Diagnostics.Stopwatch.StartNew();
        pump.Start();
        tone.Start(pump);
        await Task.Delay(1500);
        tone.Stop();
        pump.Dispose();
        double elapsed = clock.Elapsed.TotalSeconds;
        var bytes = output.ToArray();
        var samples = new float[bytes.Length / 4];
        Buffer.BlockCopy(bytes, 0, samples, 0, samples.Length * 4);
        double seconds = samples.Length / 2.0 / AudioPump.OutputRate;
        Assert.InRange(seconds, elapsed - 0.15, elapsed + 0.001);
        float peak = samples[^24000..].Max(Math.Abs);
        // tone amplitude 8000/32768 = 0.244, half volume ≈ 0.122
        Assert.InRange(peak, 0.10, 0.14);
    }
}
