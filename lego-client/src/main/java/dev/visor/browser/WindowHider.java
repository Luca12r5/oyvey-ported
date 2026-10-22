package dev.visor.browser;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import dev.visor.VisorClient;

/**
 * Windows only: the headset browser is a normal browser window parked far off screen. This keeps
 * it out of the taskbar and Alt+Tab, and stops it from ever taking focus away from the game.
 */
final class WindowHider {
	private static final int WS_EX_TOOLWINDOW = 0x00000080;
	private static final int WS_EX_APPWINDOW = 0x00040000;
	private static final int WS_EX_NOACTIVATE = 0x08000000;
	private static final int SW_HIDE = 0;
	private static final int SW_SHOWNOACTIVATE = 4;
	private static volatile boolean unavailable;

	private WindowHider() {
	}

	/** @return how many windows of the process were changed */
	static int hide(long pid) {
		if (unavailable || !BrowserLocator.isWindows()) {
			return 0;
		}
		try {
			User32 user32 = User32.INSTANCE;
			int[] changed = {0};
			user32.EnumWindows((hwnd, data) -> {
				IntByReference owner = new IntByReference();
				user32.GetWindowThreadProcessId(hwnd, owner);
				if (owner.getValue() == pid && user32.IsWindowVisible(hwnd)) {
					int style = user32.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE);
					int wanted = (style | WS_EX_TOOLWINDOW | WS_EX_NOACTIVATE) & ~WS_EX_APPWINDOW;
					if (wanted != style) {
						// The taskbar only notices the new style when the window is shown again.
						user32.ShowWindow(hwnd, SW_HIDE);
						user32.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, wanted);
						user32.ShowWindow(hwnd, SW_SHOWNOACTIVATE);
						changed[0]++;
					}
				}
				return true;
			}, null);
			return changed[0];
		} catch (Throwable t) {
			unavailable = true;
			VisorClient.LOGGER.debug("Could not keep the headset browser out of the taskbar", t);
			return 0;
		}
	}
}
