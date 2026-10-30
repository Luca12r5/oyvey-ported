using System.Runtime.InteropServices;
using System.Windows.Interop;
using Duelcliping.App.Interop;
using Duelcliping.Core.Logging;
using Duelcliping.Core.Settings;

namespace Duelcliping.App.Services;

public enum HotkeyAction { SaveShort, SaveLong, ToggleRecording, OpenApp }

/// <summary>
/// System-wide hotkeys via RegisterHotKey on a hidden message-only window. RegisterHotKey is handled
/// by the OS input thread, so it works while a game has focus without any keyboard hook, polling or
/// input latency for the game.
/// </summary>
public sealed class HotkeyManager : IDisposable
{
    private readonly HwndSource _window;
    private readonly Dictionary<int, HotkeyAction> _registered = new();

    public HotkeyManager()
    {
        var parameters = new HwndSourceParameters("Duelcliping.Hotkeys") { ParentWindow = new IntPtr(-3), WindowStyle = 0 }; // HWND_MESSAGE
        _window = new HwndSource(parameters);
        _window.AddHook(WndProc);
    }

    public event Action<HotkeyAction>? Pressed;

    /// <summary>Failures from the last <see cref="Apply"/>, e.g. a key already taken by another program.</summary>
    public IReadOnlyDictionary<HotkeyAction, string> Failures { get; private set; } = new Dictionary<HotkeyAction, string>();

    public void Apply(AppSettings settings)
    {
        UnregisterAll();
        var failures = new Dictionary<HotkeyAction, string>();
        Register(HotkeyAction.SaveShort, settings.HotkeySaveShort, failures);
        Register(HotkeyAction.SaveLong, settings.HotkeySaveLong, failures);
        Register(HotkeyAction.ToggleRecording, settings.HotkeyToggleRecording, failures);
        Register(HotkeyAction.OpenApp, settings.HotkeyOpenApp, failures);
        Failures = failures;
    }

    private void Register(HotkeyAction action, HotkeyBinding binding, Dictionary<HotkeyAction, string> failures)
    {
        if (binding.IsEmpty) return;
        int id = (int)action + 1;
        uint mods = NativeMethods.MOD_NOREPEAT;
        if (binding.Modifiers.HasFlag(HotkeyModifiers.Alt)) mods |= 0x1;
        if (binding.Modifiers.HasFlag(HotkeyModifiers.Control)) mods |= 0x2;
        if (binding.Modifiers.HasFlag(HotkeyModifiers.Shift)) mods |= 0x4;
        if (binding.Modifiers.HasFlag(HotkeyModifiers.Win)) mods |= 0x8;
        if (NativeMethods.RegisterHotKey(_window.Handle, id, mods, (uint)binding.VirtualKey))
        {
            _registered[id] = action;
            Log.Info("Hotkeys", $"{action} = {binding}");
        }
        else
        {
            int error = Marshal.GetLastWin32Error();
            string message = error == NativeMethods.ERROR_HOTKEY_ALREADY_REGISTERED
                ? $"{binding} is already used by another program."
                : $"{binding} could not be registered (error {error}).";
            failures[action] = message;
            Log.Warn("Hotkeys", $"{action}: {message}");
        }
    }

    /// <summary>Releases all keys (while the user records a new shortcut, so the keys reach the window).</summary>
    public void Suspend() => UnregisterAll();

    private void UnregisterAll()
    {
        foreach (var id in _registered.Keys) NativeMethods.UnregisterHotKey(_window.Handle, id);
        _registered.Clear();
    }

    private IntPtr WndProc(IntPtr hwnd, int msg, IntPtr wParam, IntPtr lParam, ref bool handled)
    {
        if (msg == NativeMethods.WM_HOTKEY && _registered.TryGetValue(wParam.ToInt32(), out var action))
        {
            handled = true;
            Pressed?.Invoke(action);
        }
        return IntPtr.Zero;
    }

    /// <summary>Keys that games use constantly; binding them without a modifier would break gameplay.</summary>
    public static string? GameConflictWarning(HotkeyBinding binding)
    {
        if (binding.IsEmpty || binding.Modifiers != HotkeyModifiers.None) return null;
        int vk = binding.VirtualKey;
        bool letterOrDigit = vk is >= 0x30 and <= 0x39 or >= 0x41 and <= 0x5A;
        bool common = vk is 0x20 or 0x09 or 0x0D or 0x1B or 0x10 or 0x11 or 0x12 or 0xA0 or 0xA1 or 0xA2 or 0xA3 or 0xA4 or 0xA5
            or 0x25 or 0x26 or 0x27 or 0x28 or 0x70 or 0x71 or 0x72 or 0x73 or 0x74;
        return letterOrDigit || common
            ? $"{binding} is used by most games. Pick an F-key from F6–F12, a numpad key or add Ctrl/Alt."
            : null;
    }

    public void Dispose()
    {
        UnregisterAll();
        _window.RemoveHook(WndProc);
        _window.Dispose();
    }
}
