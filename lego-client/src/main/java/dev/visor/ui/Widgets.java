package dev.visor.ui;

import dev.visor.VisorController;
import dev.visor.browser.BrowserShortcuts;
import dev.visor.render.UiCanvas;
import dev.visor.render.text.FontAtlas;
import dev.visor.render.text.SdfFont;
import dev.visor.setting.ColorSetting;
import dev.visor.setting.KeySetting;
import dev.visor.setting.ModeSetting;
import dev.visor.setting.SliderSetting;
import dev.visor.sound.VisorSounds;

/** Interactive controls shared by the pages and the settings sheet. */
public final class Widgets {
	public static final float SWITCH_W = 42.0F;
	public static final float SWITCH_H = 24.0F;
	private static long lastSliderTick;

	private Widgets() {
	}

	/** Animated on/off switch. Returns true when clicked (the caller flips the value). */
	public static boolean toggle(UiCanvas c, UiInput in, UiAnimations anim, String id, float x, float y, boolean on) {
		boolean hovered = in.hover(id, x - 6, y - 6, SWITCH_W + 12, SWITCH_H + 12);
		float hover = anim.get(id + ".h", hovered, 18.0F);
		float press = anim.get(id + ".p", in.held(id), 30.0F);
		drawSwitch(c, anim, id, x, y, on, hover, press);
		boolean clicked = in.clicked(id);
		if (clicked) {
			VisorSounds.play(on ? VisorSounds.TOGGLE_OFF : VisorSounds.TOGGLE_ON, 0.8F);
		}
		return clicked;
	}

	public static void drawSwitch(UiCanvas c, UiAnimations anim, String id, float x, float y, boolean on, float hover, float press) {
		float t = anim.get(id + ".on", on, 16.0F);
		int track = Theme.mix(Theme.CONTROL_OFF, Theme.accentBright(), t);
		track = Theme.mix(track, 0xFFFFFFFF, hover * 0.08F);
		c.roundRect(x, y, SWITCH_W, SWITCH_H, SWITCH_H * 0.5F, track);
		float knob = SWITCH_H - 6;
		float stretch = press * 4.0F;
		float kx = x + 3 + (SWITCH_W - knob - 6 - stretch) * t;
		c.shadow(kx, y + 3 + 1, knob + stretch, knob, knob * 0.5F, 3, 0x40000000);
		c.roundRect(kx, y + 3, knob + stretch, knob, knob * 0.5F, Theme.TEXT);
	}

	/** Horizontal slider with the value printed to its left; drag to change. */
	public static void slider(UiCanvas c, UiInput in, UiAnimations anim, FontAtlas f, String id, SliderSetting s, float x, float cy, float w) {
		float trackH = 6;
		boolean hovered = in.hover(id, x - 10, cy - 14, w + 20, 28);
		boolean dragging = in.dragging(id);
		if (dragging) {
			double before = s.get();
			s.setFraction((in.x - x) / w);
			if (s.get() != before && System.currentTimeMillis() - lastSliderTick > 35) {
				lastSliderTick = System.currentTimeMillis();
				VisorSounds.play(VisorSounds.SLIDER, 0.6F, 0.9F + s.fraction() * 0.25F);
			}
		}
		float hover = anim.get(id + ".h", hovered || dragging, 18.0F);
		float drag = anim.get(id + ".d", dragging, 22.0F);
		float shown = anim.get(id + ".v", s.fraction(), dragging ? 40.0F : 18.0F);
		c.roundRect(x, cy - trackH * 0.5F, w, trackH, trackH * 0.5F, Theme.CONTROL_OFF);
		c.roundRect(x, cy - trackH * 0.5F, Math.max(trackH, w * shown), trackH, trackH * 0.5F, Theme.accentBright());
		float knob = 16 + hover * 2 + drag * 2;
		float kx = x + w * shown;
		if (drag > 0.01F) {
			c.circle(kx, cy, knob * 0.5F + 7 * drag, Theme.alpha(Theme.accentBright(), 0.25F * drag));
		}
		c.shadow(kx - knob * 0.5F, cy - knob * 0.5F + 1.5F, knob, knob, knob * 0.5F, 4, 0x50000000);
		c.circle(kx, cy, knob * 0.5F, Theme.TEXT);
		String value = s.format();
		c.textRight(f.medium, value, x - 14, cy, 14, Theme.mix(Theme.TEXT_SECONDARY, Theme.TEXT, hover));
	}

	/** Segmented control for a few short options, right-aligned at {@code right}. */
	public static void segmented(UiCanvas c, UiInput in, UiAnimations anim, FontAtlas f, String id, ModeSetting s, float right, float cy) {
		float size = 13.5F;
		float pad = 14;
		float h = 30;
		float[] widths = new float[s.modes().size()];
		float total = 4;
		for (int i = 0; i < widths.length; i++) {
			widths[i] = f.medium.width(s.modes().get(i), size) + pad * 2;
			total += widths[i];
		}
		float x0 = right - total;
		c.roundRect(x0, cy - h * 0.5F, total, h, h * 0.5F, Theme.FIELD);
		float cursor = x0 + 2;
		float selectedX = cursor;
		float selectedW = widths[0];
		for (int i = 0; i < widths.length; i++) {
			if (i == s.index()) {
				selectedX = cursor;
				selectedW = widths[i];
			}
			cursor += widths[i];
		}
		float sx = anim.get(id + ".x", selectedX, 18.0F);
		float sw = anim.get(id + ".w", selectedW, 18.0F);
		c.shadow(sx, cy - h * 0.5F + 3, sw, h - 4, (h - 4) * 0.5F, 4, 0x33000000);
		c.roundRect(sx, cy - h * 0.5F + 2, sw, h - 4, (h - 4) * 0.5F, Theme.accent());
		cursor = x0 + 2;
		for (int i = 0; i < widths.length; i++) {
			String option = s.modes().get(i);
			String optionId = id + "." + i;
			boolean hovered = in.hover(optionId, cursor, cy - h * 0.5F, widths[i], h);
			boolean selected = i == s.index();
			int color = selected ? Theme.TEXT : (hovered ? Theme.TEXT : Theme.TEXT_SECONDARY);
			c.textCentered(f.medium, option, cursor + widths[i] * 0.5F, cy, size, color);
			if (in.clicked(optionId) && !selected) {
				s.set(option);
				VisorSounds.play(VisorSounds.TAB, 0.7F);
			}
			cursor += widths[i];
		}
	}

	/** Row of colour swatches, right-aligned. */
	public static void swatches(UiCanvas c, UiInput in, UiAnimations anim, String id, ColorSetting s, float right, float cy) {
		int[] palette = s.palette();
		float r = 10;
		float gap = 10;
		float x = right - palette.length * (r * 2 + gap) + gap;
		for (int i = 0; i < palette.length; i++) {
			float cx = x + r + i * (r * 2 + gap);
			String swatchId = id + "." + i;
			boolean hovered = in.hover(swatchId, cx - r - 4, cy - r - 4, r * 2 + 8, r * 2 + 8);
			boolean selected = s.argb() == palette[i];
			float sel = anim.get(swatchId + ".s", selected, 18.0F);
			float hover = anim.get(swatchId + ".h", hovered, 18.0F);
			if (sel > 0.01F) {
				c.circleStroke(cx, cy, r + 4.5F, 2.0F, Theme.alpha(Theme.TEXT, sel));
			}
			c.circle(cx, cy, r * (1.0F + hover * 0.08F), palette[i]);
			if ((palette[i] & 0xFFFFFF) > 0xE0E0E0) {
				c.circleStroke(cx, cy, r, 1.0F, 0x33000000);
			}
			if (in.clicked(swatchId) && !selected) {
				s.set(palette[i]);
				VisorSounds.play(VisorSounds.CLICK, 0.7F);
			}
		}
	}

	/**
	 * Key binding button; returns true when clicked (the caller starts capturing the next key).
	 */
	public static boolean keyButton(UiCanvas c, UiInput in, UiAnimations anim, FontAtlas f, String id, KeySetting s, float right, float cy, boolean capturing, float time) {
		String label = capturing ? "Press a key…" : s.keyName();
		float size = 13.5F;
		float w = Math.max(64, f.medium.width(label, size) + 30);
		float h = 30;
		float x = right - w;
		boolean hovered = in.hover(id, x, cy - h * 0.5F, w, h);
		float hover = anim.get(id + ".h", hovered, 18.0F);
		float cap = anim.get(id + ".c", capturing, 14.0F);
		int fill = Theme.mix(Theme.FIELD, Theme.mix(Theme.FIELD, 0xFFFFFFFF, 0.08F), hover);
		fill = Theme.mix(fill, Theme.accent(), cap);
		c.roundRect(x, cy - h * 0.5F, w, h, 8, fill);
		if (capturing) {
			float pulse = 0.5F + 0.5F * (float) Math.sin(time * 6.0F);
			c.roundRectStroke(x - 2, cy - h * 0.5F - 2, w + 4, h + 4, 10, 2.0F, Theme.alpha(Theme.accentBright(), 0.5F + pulse * 0.5F));
		}
		int color = s.bound() || capturing ? Theme.TEXT : Theme.TEXT_MUTED;
		c.textCentered(f.medium, label, x + w * 0.5F, cy, size, color);
		boolean clicked = in.clicked(id);
		if (clicked) {
			VisorSounds.play(VisorSounds.CLICK, 0.7F);
		}
		return clicked;
	}

	/** Rounded icon tile with a soft diagonal gradient; {@code vivid} 0 = neutral grey, 1 = full colour. */
	public static void iconTile(UiCanvas c, float x, float y, float size, String icon, int color, float vivid) {
		int top = Theme.mix(0xFF4A4D52, Theme.mix(color, 0xFFFFFFFF, 0.12F), vivid);
		int bottom = Theme.mix(0xFF3A3D41, Theme.mix(color, 0xFF000000, 0.30F), vivid);
		float radius = size * 0.28F;
		if (vivid > 0.01F) {
			c.shadow(x, y + size * 0.12F, size, size, radius, size * 0.30F, Theme.alpha(color, 0.35F * vivid));
		}
		c.roundRectGradient(x, y, size, size, radius, top, bottom);
		c.roundRectGradient(x, y, size, size * 0.5F, radius, 0x22FFFFFF, 0x00FFFFFF);
		c.icon(icon, true, x + size * 0.5F, y + size * 0.5F, size * 0.52F, Theme.mix(0xFFB8BBC0, Theme.TEXT, vivid));
	}

	/** Small rounded button with an icon; returns true when clicked. */
	public static boolean iconButton(UiCanvas c, UiInput in, UiAnimations anim, String id, String icon, float cx, float cy, float radius, int idle) {
		boolean hovered = in.hover(id, cx - radius, cy - radius, radius * 2, radius * 2);
		float hover = anim.get(id + ".h", hovered, 18.0F);
		float press = anim.get(id + ".p", in.held(id), 30.0F);
		float r = radius * (1.0F - press * 0.08F);
		c.circle(cx, cy, r, Theme.mix(idle, Theme.mix(idle, 0xFFFFFFFF, 0.14F), hover));
		c.icon(icon, false, cx, cy, radius * 0.95F, Theme.mix(Theme.TEXT_SECONDARY, Theme.TEXT, hover));
		return in.clicked(id);
	}

	/** Pill shaped choice chip; returns true when clicked. */
	public static boolean chip(UiCanvas c, UiInput in, UiAnimations anim, FontAtlas f, String id, String label, String count, float x, float y, boolean selected) {
		float size = 13.5F;
		float labelW = f.medium.width(label, size);
		float countW = count.isEmpty() ? 0 : f.semibold.width(count, 12) + 16;
		float w = labelW + countW + 30;
		float h = 32;
		boolean hovered = in.hover(id, x, y, w, h);
		float hover = anim.get(id + ".h", hovered, 18.0F);
		float sel = anim.get(id + ".s", selected, 16.0F);
		int fill = Theme.mix(Theme.mix(0xFF2E3033, 0xFF3A3D40, hover), Theme.accent(), sel);
		c.roundRect(x, y, w, h, h * 0.5F, fill);
		int text = Theme.mix(Theme.mix(Theme.TEXT_SECONDARY, Theme.TEXT, hover), Theme.TEXT, sel);
		c.textMiddle(f.medium, label, x + 15, y + h * 0.5F, size, text);
		if (!count.isEmpty()) {
			float cx = x + 15 + labelW + 8;
			c.roundRect(cx, y + 8, countW - 8, 16, 8, Theme.alpha(Theme.TEXT, 0.10F + sel * 0.12F));
			c.textCentered(f.semibold, count, cx + (countW - 8) * 0.5F, y + 16, 12, text);
		}
		boolean clicked = in.clicked(id);
		if (clicked && !selected) {
			VisorSounds.play(VisorSounds.TAB, 0.6F);
		}
		return clicked;
	}

	/** Width a {@link #chip} will take, for laying out rows. */
	public static float chipWidth(FontAtlas f, String label, String count) {
		return f.medium.width(label, 13.5F) + (count.isEmpty() ? 0 : f.semibold.width(count, 12) + 16) + 30;
	}
	/** App style tile of a web shortcut: brand colour, white logo, name underneath. Returns true when clicked. */
	public static boolean shortcutTile(
		UiCanvas c, UiInput in, UiAnimations anim, FontAtlas f, String id, BrowserShortcuts.Shortcut shortcut, float cx, float cy, float size, float labelSize
	) {
		float half = size * 0.5F;
		boolean hovered = in.hover(id, cx - half - 8, cy - half - 6, size + 16, size + labelSize + 24);
		float hover = anim.get(id + ".h", hovered, 18.0F);
		float press = anim.get(id + ".p", in.held(id), 30.0F);
		int color = shortcut.color();
		float radius = size * 0.28F;
		c.push();
		c.scale(1.0F + hover * 0.07F - press * 0.07F, cx, cy);
		c.translate(0, -hover * 2.0F);
		c.shadow(cx - half, cy - half + size * 0.10F, size, size, radius, size * (0.22F + hover * 0.14F), Theme.alpha(color, 0.30F + hover * 0.30F));
		c.roundRectGradient(cx - half, cy - half, size, size, radius, Theme.mix(color, 0xFFFFFFFF, 0.14F), Theme.mix(color, 0xFF000000, 0.24F));
		c.roundRectGradient(cx - half, cy - half, size, half, radius, 0x29FFFFFF, 0x00FFFFFF);
		c.icon(shortcut.icon(), true, cx, cy, size * 0.54F, Theme.TEXT);
		if (hover > 0.01F) {
			c.roundRectStroke(cx - half - 3.5F, cy - half - 3.5F, size + 7, size + 7, radius + 3.5F, 2.0F, Theme.alpha(Theme.TEXT, hover * 0.85F));
		}
		c.pop();
		String name = f.medium.ellipsize(shortcut.name(), labelSize, size + 34);
		c.textCentered(f.medium, name, cx, cy + half + labelSize * 0.5F + 9, labelSize, Theme.mix(Theme.TEXT_SECONDARY, Theme.TEXT, hover));
		boolean clicked = in.clicked(id);
		if (clicked) {
			VisorSounds.play(VisorSounds.CLICK, 0.7F);
		}
		return clicked;
	}

	/**
	 * Pill shaped search field bound to a {@link TextField}; clicking it starts typing. The field's
	 * own submit action runs on Enter.
	 */
	public static void searchPill(
		UiCanvas c, UiInput in, UiAnimations anim, FontAtlas f, String id, TextField field, float x, float y, float w, float h,
		String placeholder, String icon, float textSize
	) {
		VisorController controller = VisorController.get();
		boolean focused = controller.isFocused(field);
		boolean hovered = in.hover(id, x, y, w, h);
		float hover = anim.get(id + ".h", hovered, 18.0F);
		float focus = anim.get(id + ".f", focused, 16.0F);
		float cy = y + h * 0.5F;
		c.shadow(x, y + 3, w, h, h * 0.5F, 10, Theme.alpha(0xFF000000, 0.18F + focus * 0.12F));
		c.roundRect(x, y, w, h, h * 0.5F, Theme.mix(Theme.mix(0xFF2E3033, 0xFF383B3E, hover), 0xFF3A3D41, focus));
		if (focus > 0.01F) {
			c.roundRectStroke(x, y, w, h, h * 0.5F, 1.5F, Theme.alpha(Theme.accentBright(), focus));
		}
		float iconX = x + h * 0.5F + 2;
		c.icon(icon, false, iconX, cy, textSize * 1.3F, Theme.mix(Theme.TEXT_SECONDARY, Theme.TEXT, Math.max(hover, focus)));
		float textX = iconX + textSize * 1.25F;
		float maxWidth = x + w - h * 0.5F - textX;
		if (field.isEmpty()) {
			c.textFit(f.regular, focused ? "Type and press Enter" : placeholder, textX, cy, textSize, maxWidth, Theme.TEXT_MUTED);
			if (focused && field.caretVisible()) {
				c.rect(textX - 2, cy - textSize * 0.62F, 1.5F, textSize * 1.24F, Theme.accentBright());
			}
		} else {
			String shown = tail(f.regular, field.text(), textSize, maxWidth - 4);
			float textW = f.regular.width(shown, textSize);
			if (focused && field.allSelected()) {
				c.roundRect(textX - 3, cy - textSize * 0.72F, textW + 6, textSize * 1.44F, 4, Theme.alpha(Theme.accentBright(), 0.40F));
			}
			float end = c.textMiddle(f.regular, shown, textX, cy, textSize, Theme.TEXT);
			if (focused && !field.allSelected() && field.caretVisible()) {
				c.rect(end + 1, cy - textSize * 0.62F, 1.5F, textSize * 1.24F, Theme.accentBright());
			}
		}
		if (in.clicked(id) && !focused) {
			controller.focusText(field);
			VisorSounds.play(VisorSounds.CLICK, 0.6F);
		}
	}

	/** The end of {@code text} that fits in {@code maxWidth}, with a leading ellipsis when cut. */
	public static String tail(SdfFont font, String text, float size, float maxWidth) {
		if (font.width(text, size) <= maxWidth) {
			return text;
		}
		int low = 0;
		int high = text.length();
		while (low < high) {
			int mid = (low + high) >>> 1;
			if (font.width("…" + text.substring(mid), size) <= maxWidth) {
				high = mid;
			} else {
				low = mid + 1;
			}
		}
		return "…" + text.substring(low);
	}
}
