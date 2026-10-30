namespace Duelcliping.Core.Settings;

public enum CaptureResolution { Native, P720, P1080, P1440, P2160 }
public enum ContainerFormat { Mp4, Mkv }
public enum VideoCodec { H264, Hevc, Av1 }
public enum QualityPreset { Low, Medium, High, Ultra, Custom }
public enum EncoderPreference { Auto, Nvidia, Amd, Intel, Software }
public enum AppTheme { Dark, Light }
public enum OverlayCorner { TopLeft, TopRight, BottomLeft, BottomRight }

[Flags]
public enum HotkeyModifiers { None = 0, Alt = 1, Control = 2, Shift = 4, Win = 8 }

/// <summary>A global keyboard shortcut: Win32 virtual-key code plus modifier flags.</summary>
public sealed record HotkeyBinding(HotkeyModifiers Modifiers, int VirtualKey)
{
    public static readonly HotkeyBinding None = new(HotkeyModifiers.None, 0);
    public bool IsEmpty => VirtualKey == 0;

    public override string ToString()
    {
        if (IsEmpty) return "Not set";
        var parts = new List<string>();
        if (Modifiers.HasFlag(HotkeyModifiers.Control)) parts.Add("Ctrl");
        if (Modifiers.HasFlag(HotkeyModifiers.Alt)) parts.Add("Alt");
        if (Modifiers.HasFlag(HotkeyModifiers.Shift)) parts.Add("Shift");
        if (Modifiers.HasFlag(HotkeyModifiers.Win)) parts.Add("Win");
        parts.Add(VirtualKeyNames.Name(VirtualKey));
        return string.Join(" + ", parts);
    }
}

public static class VirtualKeyNames
{
    public static string Name(int vk) => vk switch
    {
        >= 0x70 and <= 0x87 => "F" + (vk - 0x6F),
        >= 0x30 and <= 0x39 => ((char)vk).ToString(),
        >= 0x41 and <= 0x5A => ((char)vk).ToString(),
        >= 0x60 and <= 0x69 => "Num " + (vk - 0x60),
        0x13 => "Pause",
        0x14 => "Caps Lock",
        0x2C => "Print Screen",
        0x91 => "Scroll Lock",
        0x2D => "Insert",
        0x2E => "Delete",
        0x24 => "Home",
        0x23 => "End",
        0x21 => "Page Up",
        0x22 => "Page Down",
        0x6A => "Num *",
        0x6B => "Num +",
        0x6D => "Num -",
        0x6E => "Num .",
        0x6F => "Num /",
        0xC0 => "`",
        _ => $"Key 0x{vk:X2}",
    };
}

/// <summary>
/// All user configurable options. Plain mutable POCO so it round-trips through JSON;
/// <see cref="Normalize"/> clamps everything to supported ranges after loading.
/// </summary>
public sealed class AppSettings
{
    public const int SettingsVersion = 1;
    public static readonly int[] SupportedFrameRates = { 30, 60, 120, 144 };
    public static readonly int[] QuickClipDurations = { 15, 30, 60, 120, 300 };

    public int Version { get; set; } = SettingsVersion;

    // Recording
    public bool ReplayBufferEnabled { get; set; } = true;
    public int ReplayBufferSeconds { get; set; } = 120;
    public string MonitorDeviceName { get; set; } = "";
    public CaptureResolution Resolution { get; set; } = CaptureResolution.P1080;
    public int FrameRate { get; set; } = 60;
    public VideoCodec Codec { get; set; } = VideoCodec.H264;
    public ContainerFormat Container { get; set; } = ContainerFormat.Mp4;
    public QualityPreset Quality { get; set; } = QualityPreset.High;
    public int CustomBitrateKbps { get; set; } = 20000;
    public EncoderPreference Encoder { get; set; } = EncoderPreference.Auto;
    public bool CaptureCursor { get; set; } = true;
    public int ShortClipSeconds { get; set; } = 30;
    public int LongClipSeconds { get; set; } = 60;
    public int CustomClipSeconds { get; set; } = 45;

    // Audio
    public bool CaptureSystemAudio { get; set; } = true;
    public string SystemAudioDeviceId { get; set; } = "";
    public double SystemAudioVolume { get; set; } = 1.0;
    public bool CaptureMicrophone { get; set; }
    public string MicrophoneDeviceId { get; set; } = "";
    public double MicrophoneVolume { get; set; } = 1.0;
    public bool SeparateAudioTracks { get; set; } = true;

    // Storage
    public string ClipsDirectory { get; set; } = "";
    public double MaxStorageGb { get; set; } = 50;
    public bool AutoDeleteOldClips { get; set; }
    public int AutoDeleteAfterDays { get; set; } = 30;

    // Hotkeys (defaults: F8 / F9 / F10 / F11)
    public HotkeyBinding HotkeySaveShort { get; set; } = new(HotkeyModifiers.None, 0x77);
    public HotkeyBinding HotkeySaveLong { get; set; } = new(HotkeyModifiers.None, 0x78);
    public HotkeyBinding HotkeyToggleRecording { get; set; } = new(HotkeyModifiers.None, 0x79);
    public HotkeyBinding HotkeyOpenApp { get; set; } = new(HotkeyModifiers.None, 0x7A);

    // Startup
    public bool StartWithWindows { get; set; }
    public bool StartMinimized { get; set; } = true;
    public bool CloseToTray { get; set; } = true;

    // Overlay
    /// <summary>Short "Saved" badge for 2.5 s after saving a clip.</summary>
    public bool OverlayEnabled { get; set; } = true;
    /// <summary>
    /// Permanent REC/REPLAY indicator. Off by default: any always-on-top window over a windowed or
    /// borderless game prevents Windows' direct-flip presentation and can cost a lot of FPS.
    /// </summary>
    public bool OverlayAlways { get; set; }
    /// <summary>No notification popups while a fullscreen game is focused (sound only).</summary>
    public bool QuietWhileGaming { get; set; } = true;
    public OverlayCorner OverlayPosition { get; set; } = OverlayCorner.TopRight;
    public double OverlayOpacity { get; set; } = 0.85;

    // Notifications
    public bool PlaySoundOnSave { get; set; } = true;
    public bool NotifyOnSave { get; set; } = true;

    // Performance
    public bool PerformanceMode { get; set; } = true;
    public string PresentMonPath { get; set; } = "";

    // Appearance
    public AppTheme Theme { get; set; } = AppTheme.Dark;

    public static string DefaultClipsDirectory() =>
        Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyVideos), "Duelcliping");

    public void Normalize()
    {
        ReplayBufferSeconds = Math.Clamp(ReplayBufferSeconds, 10, 600);
        if (!SupportedFrameRates.Contains(FrameRate))
            FrameRate = SupportedFrameRates.OrderBy(f => Math.Abs(f - FrameRate)).First();
        CustomBitrateKbps = Math.Clamp(CustomBitrateKbps, 1000, 150_000);
        ShortClipSeconds = Math.Clamp(ShortClipSeconds, 5, 600);
        LongClipSeconds = Math.Clamp(LongClipSeconds, 5, 600);
        CustomClipSeconds = Math.Clamp(CustomClipSeconds, 5, 600);
        SystemAudioVolume = Math.Clamp(SystemAudioVolume, 0, 2);
        MicrophoneVolume = Math.Clamp(MicrophoneVolume, 0, 2);
        MaxStorageGb = Math.Clamp(MaxStorageGb, 1, 100_000);
        AutoDeleteAfterDays = Math.Clamp(AutoDeleteAfterDays, 1, 3650);
        OverlayOpacity = Math.Clamp(OverlayOpacity, 0.3, 1);
        if (string.IsNullOrWhiteSpace(ClipsDirectory)) ClipsDirectory = DefaultClipsDirectory();
        HotkeySaveShort ??= HotkeyBinding.None;
        HotkeySaveLong ??= HotkeyBinding.None;
        HotkeyToggleRecording ??= HotkeyBinding.None;
        HotkeyOpenApp ??= HotkeyBinding.None;
        MonitorDeviceName ??= "";
        SystemAudioDeviceId ??= "";
        MicrophoneDeviceId ??= "";
        PresentMonPath ??= "";
        Version = SettingsVersion;
    }

    public AppSettings Clone()
    {
        var copy = (AppSettings)MemberwiseClone();
        return copy; // all members are value types, strings or immutable records
    }
}
