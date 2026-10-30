using System.Diagnostics;
using System.Runtime.InteropServices;
using Duelcliping.Core.Logging;

namespace Duelcliping.Core.Audio;

/// <summary>Raw sample format delivered by a capture device.</summary>
public readonly record struct AudioFormat(int SampleRate, int Channels, int BitsPerSample, bool IsFloat)
{
    public int BlockAlign => Channels * BitsPerSample / 8;
    public override string ToString() => $"{SampleRate} Hz, {Channels} ch, {BitsPerSample}-bit {(IsFloat ? "float" : "PCM")}";
}

/// <summary>
/// A platform audio capture (WASAPI loopback, WASAPI microphone, test tone ...). Implementations call
/// the <see cref="AudioPump"/> they are attached to from their own capture thread.
/// </summary>
public interface IAudioCaptureSource : IDisposable
{
    string Name { get; }
    /// <summary>Starts capturing and delivering samples to <paramref name="pump"/>.</summary>
    void Start(AudioPump pump);
    void Stop();
}

/// <summary>
/// Converts device audio to the pipeline format (48 kHz stereo float), applies volume and writes
/// it to ffmpeg at the pace of a real-time clock.
///
/// The clock matters: WASAPI loopback delivers no packets at all while nothing is playing and a
/// disconnected device delivers nothing either. Without padding, audio would drift away from the
/// video. The pump therefore inserts silence whenever the device falls behind the clock and drops
/// samples when it runs ahead, so the audio track always has exactly real-time length.
/// </summary>
public sealed class AudioPump : IDisposable
{
    public const int OutputRate = 48000;
    public const int OutputChannels = 2;
    private const int TickMs = 10;
    private const int PadThresholdFrames = OutputRate * 60 / 1000;   // insert silence when >60 ms behind
    private const int MaxQueuedFrames = OutputRate * 150 / 1000;     // drop data when >150 ms ahead

    private readonly Stream _output;
    private readonly float[] _ring;
    private readonly object _ringLock = new();
    private int _ringStart;
    private int _ringCount;
    private readonly Thread _thread;
    private volatile bool _running;
    private long _framesWritten;
    private readonly Stopwatch _clock = new();
    private ResamplerState _resampler;

    public AudioPump(string name, Stream output, double volume)
    {
        Name = name;
        _output = output;
        Volume = volume;
        _ring = new float[OutputRate * OutputChannels * 2]; // 2 seconds
        _thread = new Thread(Run) { IsBackground = true, Name = "Duelcliping.Audio." + name, Priority = ThreadPriority.AboveNormal };
    }

    public string Name { get; }
    /// <summary>Linear gain, 0..2. Can be changed while running.</summary>
    public double Volume { get; set; }
    public long FramesWritten => Interlocked.Read(ref _framesWritten);
    public long SilenceFramesInserted { get; private set; }
    public long FramesDropped { get; private set; }
    public bool Faulted { get; private set; }

    public void Start()
    {
        _running = true;
        _clock.Start();
        _thread.Start();
    }

    /// <summary>Accepts interleaved device samples in any common format; converts and queues them.</summary>
    public void Push(ReadOnlySpan<byte> data, AudioFormat format)
    {
        if (!_running || data.Length < format.BlockAlign || format.BlockAlign == 0) return;
        int frames = data.Length / format.BlockAlign;

        // Convert to stereo float at device rate.
        var stereo = System.Buffers.ArrayPool<float>.Shared.Rent(frames * 2);
        try
        {
            ToStereoFloat(data, format, frames, stereo);
            if (format.SampleRate == OutputRate)
            {
                Enqueue(stereo.AsSpan(0, frames * 2));
            }
            else
            {
                int maxOut = (int)((long)frames * OutputRate / format.SampleRate) + 4;
                var resampled = System.Buffers.ArrayPool<float>.Shared.Rent(maxOut * 2);
                try
                {
                    int produced = Resample(stereo.AsSpan(0, frames * 2), format.SampleRate, resampled.AsSpan(0, maxOut * 2));
                    Enqueue(resampled.AsSpan(0, produced * 2));
                }
                finally { System.Buffers.ArrayPool<float>.Shared.Return(resampled); }
            }
        }
        finally { System.Buffers.ArrayPool<float>.Shared.Return(stereo); }
    }

    /// <summary>Queues samples that are already 48 kHz interleaved stereo float.</summary>
    public void PushPipelineFormat(ReadOnlySpan<float> stereo48k)
    {
        if (_running) Enqueue(stereo48k);
    }

    private static void ToStereoFloat(ReadOnlySpan<byte> data, AudioFormat f, int frames, float[] dest)
    {
        int ch = f.Channels;
        for (int i = 0; i < frames; i++)
        {
            var frame = data.Slice(i * f.BlockAlign, f.BlockAlign);
            float l = Sample(frame, 0, f);
            float r = ch > 1 ? Sample(frame, 1, f) : l;
            if (ch > 2)
            {
                // Fold center (and LFE-free surrounds) into stereo for 5.1/7.1 devices.
                float center = ch > 2 ? Sample(frame, 2, f) * 0.707f : 0f;
                float sl = ch > 5 ? Sample(frame, 4, f) * 0.5f : 0f;
                float sr = ch > 5 ? Sample(frame, 5, f) * 0.5f : 0f;
                l += center + sl;
                r += center + sr;
            }
            dest[i * 2] = l;
            dest[i * 2 + 1] = r;
        }
    }

    private static float Sample(ReadOnlySpan<byte> frame, int channel, AudioFormat f)
    {
        int bytes = f.BitsPerSample / 8;
        var s = frame.Slice(channel * bytes, bytes);
        if (f.IsFloat) return bytes == 4 ? BitConverter.ToSingle(s) : (float)BitConverter.ToDouble(s);
        return bytes switch
        {
            2 => BitConverter.ToInt16(s) / 32768f,
            3 => ((s[0] << 8 | s[1] << 16 | s[2] << 24) >> 8) / 8388608f,
            4 => BitConverter.ToInt32(s) / 2147483648f,
            _ => (s[0] - 128) / 128f,
        };
    }

    private struct ResamplerState
    {
        public double Position;   // fractional read position relative to the current input block
        public float LastL, LastR;
        public bool HasLast;
    }

    /// <summary>Linear interpolation resampler that keeps phase across calls.</summary>
    private int Resample(ReadOnlySpan<float> input, int inRate, Span<float> output)
    {
        int inFrames = input.Length / 2;
        if (inFrames == 0) return 0;
        double step = (double)inRate / OutputRate;
        int produced = 0;
        ref var st = ref _resampler;
        double pos = st.Position;
        while (produced * 2 + 1 < output.Length)
        {
            int index = (int)Math.Floor(pos);
            if (index + 1 >= inFrames) break;
            double frac = pos - index;
            float l0, r0;
            if (index < 0) { l0 = st.HasLast ? st.LastL : input[0]; r0 = st.HasLast ? st.LastR : input[1]; }
            else { l0 = input[index * 2]; r0 = input[index * 2 + 1]; }
            float l1 = input[(index + 1) * 2], r1 = input[(index + 1) * 2 + 1];
            output[produced * 2] = (float)(l0 + (l1 - l0) * frac);
            output[produced * 2 + 1] = (float)(r0 + (r1 - r0) * frac);
            produced++;
            pos += step;
        }
        st.Position = pos - inFrames; // last input frame becomes index -1 of the next block
        st.LastL = input[(inFrames - 1) * 2];
        st.LastR = input[(inFrames - 1) * 2 + 1];
        st.HasLast = true;
        return produced;
    }

    private void Enqueue(ReadOnlySpan<float> samples)
    {
        lock (_ringLock)
        {
            int capacity = _ring.Length;
            if (samples.Length > capacity) samples = samples[^capacity..];
            int overflow = _ringCount + samples.Length - capacity;
            if (overflow > 0)
            {
                _ringStart = (_ringStart + overflow) % capacity;
                _ringCount -= overflow;
                FramesDropped += overflow / 2;
            }
            int writePos = (_ringStart + _ringCount) % capacity;
            int first = Math.Min(samples.Length, capacity - writePos);
            samples[..first].CopyTo(_ring.AsSpan(writePos));
            samples[first..].CopyTo(_ring.AsSpan(0));
            _ringCount += samples.Length;
        }
    }

    private void Run()
    {
        var buffer = new float[OutputRate * OutputChannels / 2]; // up to 500 ms per write
        try
        {
            while (_running)
            {
                Thread.Sleep(TickMs);
                long expectedFrames = (long)(_clock.Elapsed.TotalSeconds * OutputRate);
                long deficit = expectedFrames - _framesWritten;
                if (deficit <= 0) continue;

                int framesToWrite;
                lock (_ringLock)
                {
                    int available = _ringCount / 2;
                    if (available > deficit + MaxQueuedFrames)
                    {
                        // Device runs ahead of the clock (e.g. burst after a stall): drop the oldest data.
                        int drop = (int)(available - deficit);
                        _ringStart = (_ringStart + drop * 2) % _ring.Length;
                        _ringCount -= drop * 2;
                        FramesDropped += drop;
                        available -= drop;
                    }
                    framesToWrite = (int)Math.Min(Math.Min(deficit, available), buffer.Length / 2);
                    CopyOut(buffer.AsSpan(0, framesToWrite * 2));
                }

                long stillMissing = deficit - framesToWrite;
                int silence = 0;
                if (stillMissing > PadThresholdFrames)
                {
                    // The device is silent or gone: keep the track continuous with silence.
                    silence = (int)Math.Min(stillMissing - PadThresholdFrames / 3, buffer.Length / 2 - framesToWrite);
                    buffer.AsSpan(framesToWrite * 2, silence * 2).Clear();
                    SilenceFramesInserted += silence;
                }

                int total = framesToWrite + silence;
                if (total == 0) continue;
                ApplyVolume(buffer.AsSpan(0, total * 2));
                _output.Write(MemoryMarshal.AsBytes(buffer.AsSpan(0, total * 2)));
                Interlocked.Add(ref _framesWritten, total);
            }
        }
        catch (Exception ex) when (ex is IOException or ObjectDisposedException)
        {
            if (_running)
            {
                Faulted = true;
                Log.Warn("Audio", $"{Name}: audio pipe closed ({ex.Message})");
            }
        }
        catch (Exception ex)
        {
            Faulted = true;
            Log.Error("Audio", $"{Name}: audio pump crashed", ex);
        }
    }

    private void CopyOut(Span<float> destination)
    {
        int capacity = _ring.Length;
        int first = Math.Min(destination.Length, capacity - _ringStart);
        _ring.AsSpan(_ringStart, first).CopyTo(destination);
        _ring.AsSpan(0, destination.Length - first).CopyTo(destination[first..]);
        _ringStart = (_ringStart + destination.Length) % capacity;
        _ringCount -= destination.Length;
    }

    private void ApplyVolume(Span<float> samples)
    {
        float gain = (float)Volume;
        if (Math.Abs(gain - 1f) < 0.001f) return;
        for (int i = 0; i < samples.Length; i++)
            samples[i] = Math.Clamp(samples[i] * gain, -1f, 1f);
    }

    /// <summary>Stops writing without waiting for the pump thread (used right before ffmpeg is told to quit).</summary>
    public void BeginStop() => _running = false;

    public void Dispose()
    {
        _running = false;
        if (_thread.IsAlive) _thread.Join(500);
    }
}
