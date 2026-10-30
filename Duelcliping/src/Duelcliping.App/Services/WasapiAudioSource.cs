using Duelcliping.Core.Audio;
using Duelcliping.Core.Logging;
using NAudio.CoreAudioApi;
using NAudio.CoreAudioApi.Interfaces;
using NAudio.Wave;

namespace Duelcliping.App.Services;

public sealed record AudioDeviceInfo(string Id, string Name);

public static class AudioDevices
{
    public static IReadOnlyList<AudioDeviceInfo> List(DataFlow flow)
    {
        try
        {
            using var enumerator = new MMDeviceEnumerator();
            return enumerator.EnumerateAudioEndPoints(flow, DeviceState.Active)
                .Select(d => { using (d) return new AudioDeviceInfo(d.ID, d.FriendlyName); })
                .ToList();
        }
        catch (Exception ex)
        {
            Log.Error("Audio", "Could not list audio devices", ex);
            return Array.Empty<AudioDeviceInfo>();
        }
    }
}

/// <summary>
/// WASAPI capture in shared mode: loopback of an output device (game / system audio) or a
/// microphone. Event driven, no polling. If the device disappears (headset unplugged) or the
/// Windows default device changes, the capture re-opens the right device automatically; the
/// <see cref="AudioPump"/> fills the gap with silence so the track stays in sync.
/// </summary>
public sealed class WasapiAudioSource : IAudioCaptureSource, IMMNotificationClient
{
    private static readonly Guid IeeeFloatSubType = new("00000003-0000-0010-8000-00aa00389b71"); // KSDATAFORMAT_SUBTYPE_IEEE_FLOAT

    private readonly DataFlow _flow;
    private readonly string _deviceId; // empty = follow the Windows default device
    private readonly object _lock = new();
    private readonly MMDeviceEnumerator _enumerator = new();
    private IWaveIn? _capture;
    private AudioPump? _pump;
    private AudioFormat _format;
    private volatile bool _running;
    private Timer? _reopenTimer;

    public WasapiAudioSource(DataFlow flow, string deviceId)
    {
        _flow = flow;
        _deviceId = deviceId;
    }

    public string Name { get; private set; } = "";

    public void Start(AudioPump pump)
    {
        _pump = pump;
        _running = true;
        _enumerator.RegisterEndpointNotificationCallback(this);
        Open();
    }

    private void Open()
    {
        lock (_lock)
        {
            if (!_running) return;
            CloseCapture();
            MMDevice? device = null;
            try
            {
                device = string.IsNullOrEmpty(_deviceId)
                    ? _enumerator.GetDefaultAudioEndpoint(_flow, Role.Console)
                    : _enumerator.GetDevice(_deviceId);
                if (device.State != DeviceState.Active) throw new InvalidOperationException("device is not active");
                Name = device.FriendlyName;
                IWaveIn capture = _flow == DataFlow.Render
                    ? new WasapiLoopbackCapture(device)
                    : new WasapiCapture(device, true, 50);
                var wf = capture.WaveFormat;
                bool isFloat = wf.Encoding == WaveFormatEncoding.IeeeFloat ||
                               (wf is WaveFormatExtensible ext && ext.SubFormat == IeeeFloatSubType);
                _format = new AudioFormat(wf.SampleRate, wf.Channels, wf.BitsPerSample, isFloat);
                capture.DataAvailable += OnData;
                capture.RecordingStopped += OnStopped;
                capture.StartRecording();
                _capture = capture;
                Log.Info("Audio", $"{(_flow == DataFlow.Render ? "Loopback" : "Microphone")} capture opened: {Name} ({_format})");
            }
            catch (Exception ex)
            {
                device?.Dispose();
                Log.Warn("Audio", $"Audio device unavailable ({ex.Message}); retrying in 3 s, recording silence meanwhile");
                ScheduleReopen(TimeSpan.FromSeconds(3));
            }
        }
    }

    private void OnData(object? sender, WaveInEventArgs e)
    {
        if (e.BytesRecorded > 0) _pump?.Push(e.Buffer.AsSpan(0, e.BytesRecorded), _format);
    }

    private void OnStopped(object? sender, StoppedEventArgs e)
    {
        if (!_running) return;
        Log.Warn("Audio", $"{Name}: capture stopped ({e.Exception?.Message ?? "device lost"}); reconnecting");
        ScheduleReopen(TimeSpan.FromSeconds(1));
    }

    private void ScheduleReopen(TimeSpan delay)
    {
        _reopenTimer?.Dispose();
        _reopenTimer = new Timer(_ => Open(), null, delay, Timeout.InfiniteTimeSpan);
    }

    private void CloseCapture()
    {
        if (_capture == null) return;
        var capture = _capture;
        _capture = null;
        capture.DataAvailable -= OnData;
        capture.RecordingStopped -= OnStopped;
        try { capture.StopRecording(); } catch { /* device already gone */ }
        capture.Dispose();
    }

    public void Stop()
    {
        _running = false;
        _reopenTimer?.Dispose();
        try { _enumerator.UnregisterEndpointNotificationCallback(this); } catch { /* not registered */ }
        lock (_lock) CloseCapture();
    }

    public void Dispose()
    {
        Stop();
        _enumerator.Dispose();
    }

    // IMMNotificationClient: re-open when the device we follow changes or comes back.
    public void OnDefaultDeviceChanged(DataFlow flow, Role role, string defaultDeviceId)
    {
        if (flow == _flow && role == Role.Console && string.IsNullOrEmpty(_deviceId)) ScheduleReopen(TimeSpan.FromMilliseconds(500));
    }

    public void OnDeviceStateChanged(string deviceId, DeviceState newState)
    {
        if (deviceId == _deviceId && newState == DeviceState.Active) ScheduleReopen(TimeSpan.FromMilliseconds(500));
    }

    public void OnDeviceAdded(string pwstrDeviceId) { }
    public void OnDeviceRemoved(string deviceId) { }
    public void OnPropertyValueChanged(string pwstrDeviceId, PropertyKey key) { }
}
