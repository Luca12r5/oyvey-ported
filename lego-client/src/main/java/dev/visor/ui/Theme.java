package dev.visor.ui;

import dev.visor.setting.VisorSettings;

/** Colours (ARGB) and metrics of the interface. */
public final class Theme {
	public static final int SIDEBAR = 0xFF1F2123;
	public static final int CONTENT = 0xFF26282A;
	public static final int CARD = 0xFF333536;
	public static final int CARD_HOVER = 0xFF44474A;
	public static final int CARD_PRESSED = 0xFF2C2E30;
	public static final int SHEET = 0xFF2A2C2F;
	public static final int FIELD = 0xFF35383B;
	public static final int CONTROL_OFF = 0xFF4A4D51;
	public static final int ONLINE = 0xFF6DE841;
	public static final int DOCK = 0xF2151517;
	public static final int DOCK_HOVER = 0xFF2A2C2F;
	public static final int TEXT = 0xFFFFFFFF;
	public static final int TEXT_SECONDARY = 0xFFB0B3B8;
	public static final int TEXT_MUTED = 0xFF8A8D91;
	public static final int DIVIDER = 0x1FFFFFFF;
	public static final int SHADOW = 0x99000000;
	public static final int DANGER = 0xFFFF5A6A;

	public static final float PANEL_RADIUS = 14.0F;
	public static final float CARD_RADIUS = 10.0F;

	private Theme() {
	}

	/** The user-chosen accent: highlights, toggles, cursor. */
	public static int accentBright() {
		return VisorSettings.ACCENT.argb();
	}

	/** Deeper accent used for filled selections (#2F7BFF becomes about #1D4C9E). */
	public static int accent() {
		return mix(accentBright(), 0xFF000000, 0.38F);
	}

	public static int cursor() {
		return mix(accentBright(), 0xFFFFFFFF, 0.08F);
	}

	public static int alpha(int color, float alpha) {
		int a = Math.round((color >>> 24) * Math.max(0.0F, Math.min(1.0F, alpha)));
		return (color & 0x00FFFFFF) | a << 24;
	}

	public static int mix(int a, int b, float t) {
		return dev.visor.render.UiCanvas.lerpColor(a, b, t);
	}
}
