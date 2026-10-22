package dev.visor.ui;

import dev.visor.VisorController;
import dev.visor.donut.DrDonutt;
import dev.visor.module.Module;
import dev.visor.render.UiCanvas;
import dev.visor.render.UiResources;
import dev.visor.render.text.FontAtlas;
import dev.visor.setting.BoolSetting;
import dev.visor.setting.ColorSetting;
import dev.visor.setting.KeySetting;
import dev.visor.setting.ModeSetting;
import dev.visor.setting.Setting;
import dev.visor.setting.SettingGroup;
import dev.visor.setting.SliderSetting;
import dev.visor.setting.VisorSettings;
import dev.visor.sound.VisorSounds;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Modal settings window floating over the main panel. Opens for a module (right click on its
 * card) or for the headset itself (right click anywhere else, or the gear buttons).
 */
public final class SettingsSheet {
	private static final float W = 620;
	private static final float H = 500;
	private static final float ROW = 62;
	private static final float GROUP = 38;

	private final UiAnimations anim = new UiAnimations();
	private final Spring open = new Spring(0.0F, 15.0F);
	private @Nullable Content content;
	private @Nullable Content closing;
	private float scroll;
	private float scrollTarget;

	/** What a sheet shows. */
	public record Content(String key, String title, String subtitle, String icon, int tint, @Nullable Module module, List<SettingGroup> groups) {
		public static Content of(Module module) {
			return new Content("module." + module.id(), module.name(), module.description(), module.icon(), module.category().color(), module, module.groups());
		}

		public static Content headset() {
			return new Content("headset", "Settings", "Interface, hands, effects, sound, browser and video", "gear-six", 0xFF8A8F98, null, VisorSettings.GROUPS);
		}

		public static Content browser() {
			return new Content("browser", "Browser", "Page zoom, sound, search, fullscreen and VR video", "globe-simple", 0xFF3B82F6, null,
				List.of(VisorSettings.BROWSER, VisorSettings.VIDEO));
		}
	}

	public boolean isOpen() {
		return this.content != null;
	}

	/** Whether the sheet still covers the panel (open, or animating closed). */
	public boolean blocksInput() {
		return this.content != null || this.open.value() > 0.02F;
	}

	public void show(Content content) {
		boolean wasOpen = this.content != null;
		if (wasOpen && this.content.key().equals(content.key())) {
			return;
		}
		this.content = content;
		this.scroll = 0;
		this.scrollTarget = 0;
		if (!wasOpen) {
			this.open.snap(Math.min(this.open.value(), 0.6F));
			VisorSounds.play(VisorSounds.MENU_OPEN, 0.8F);
		} else {
			VisorSounds.play(VisorSounds.TAB, 0.6F);
		}
		this.open.target(1.0F);
	}

	public void close() {
		if (this.content != null) {
			this.closing = this.content;
			this.content = null;
			this.open.target(0.0F);
			VisorController.get().stopKeyboardCapture();
			VisorSounds.play(VisorSounds.MENU_CLOSE, 0.8F);
		}
	}

	public void render(UiCanvas c, UiInput in, float dt, float time, float panelW, float panelH) {
		float t = this.open.update(dt);
		Content shown = this.content != null ? this.content : this.closing;
		if (t < 0.002F || shown == null) {
			if (this.content == null) {
				this.closing = null;
			}
			return;
		}
		FontAtlas f = UiResources.atlas();
		this.anim.beginFrame(dt);
		boolean interactive = this.content != null && t > 0.85F;
		UiInput input = interactive ? in : UiInput.INERT;

		// Dim the panel behind the sheet.
		c.roundRect(0, 0, panelW, panelH, Theme.PANEL_RADIUS, Theme.alpha(0xFF000000, 0.55F * t));

		float x = (panelW - W) * 0.5F;
		float y = (panelH - H) * 0.5F;
		c.push();
		c.pushAlpha(Math.min(1.0F, t * 1.4F));
		float scale = 0.94F + 0.06F * Ease.outBack(Math.min(1.0F, t));
		c.scale(scale, x + W * 0.5F, y + H * 0.5F);
		c.translate(0, (1.0F - t) * 18.0F);

		c.shadow(x, y + 14, W, H, 20, 40, 0x99000000);
		c.roundRect(x, y, W, H, 20, Theme.SHEET);
		c.roundRectGradient(x, y, W, 90, 20, 0x0AFFFFFF, 0x00FFFFFF);
		c.roundRectStroke(x, y, W, H, 20, 1.0F, 0x1AFFFFFF);

		// Header.
		Widgets.iconTile(c, x + 26, y + 24, 54, shown.icon(), shown.tint(), shown.module() == null || shown.module().enabled() ? 1.0F : 0.55F);
		c.textMiddle(f.semibold, shown.title(), x + 96, y + 42, 22, Theme.TEXT);
		c.textFit(f.regular, shown.subtitle(), x + 96, y + 66, 14, W - 190, Theme.TEXT_SECONDARY);
		if (Widgets.iconButton(c, input, this.anim, "sheet.close", "x", x + W - 40, y + 46, 18, 0xFF3A3D40)) {
			this.close();
		}
		c.rect(x + 24, y + 100, W - 48, 1, Theme.DIVIDER);

		// Body (scrollable).
		float bodyTop = y + 102;
		float bodyBottom = y + H - 64;
		float contentH = this.contentHeight(shown);
		float maxScroll = Math.max(0, contentH - (bodyBottom - bodyTop));
		if (input.inside(x, bodyTop, W, bodyBottom - bodyTop) && input.scroll != 0) {
			this.scrollTarget = Math.max(0, Math.min(maxScroll, this.scrollTarget - input.scroll * 70));
		}
		this.scrollTarget = Math.min(this.scrollTarget, maxScroll);
		this.scroll = this.anim.get("sheet.scroll." + shown.key(), this.scrollTarget, 16.0F);
		c.pushClip(x, bodyTop, W, bodyBottom - bodyTop);
		float cy = bodyTop + 8 - this.scroll;
		Module module = shown.module();
		if (module instanceof DrDonutt) {
			// Doctor Donut first gets his own card: summon him, bring him back or switch him off.
			UiInput cardInput = input.inside(x, bodyTop, W, bodyBottom - bodyTop) ? input : UiInput.INERT;
			cy = DonutCard.get().render(c, cardInput, this.anim, f, x + 24, cy + 6, W - 48, time) + 4;
		}
		if (module != null) {
			cy = this.groupTitle(c, f, "Module", x, cy);
			cy = this.row(c, input, f, "Enabled", module.enabled() ? "The module is running" : "The module is switched off", x, cy, bodyTop, bodyBottom, (rowIn, right, mid) -> {
				if (Widgets.toggle(c, rowIn, this.anim, "sheet.enabled", right - Widgets.SWITCH_W, mid - Widgets.SWITCH_H * 0.5F, module.enabled())) {
					module.toggle();
				}
			});
			KeySetting key = module.keybind();
			cy = this.row(c, input, f, key.name(), "Press to toggle the module outside the headset", x, cy, bodyTop, bodyBottom, (rowIn, right, mid) -> {
				boolean capturing = VisorController.get().isCapturing(key);
				if (Widgets.keyButton(c, rowIn, this.anim, f, "sheet.key", key, right, mid, capturing, time)) {
					VisorController.get().startKeyCapture(key);
				}
			});
		}
		for (SettingGroup group : shown.groups()) {
			cy = this.groupTitle(c, f, group.title(), x, cy);
			for (Setting<?> setting : group.settings()) {
				if (!setting.isVisible()) {
					continue;
				}
				cy = this.settingRow(c, input, f, shown, setting, x, cy, bodyTop, bodyBottom, time);
			}
		}
		c.popClip();
		if (maxScroll > 1) {
			float trackH = bodyBottom - bodyTop - 16;
			float barH = Math.max(30, trackH * (bodyBottom - bodyTop) / contentH);
			float barY = bodyTop + 8 + (trackH - barH) * (this.scroll / maxScroll);
			c.roundRect(x + W - 9, barY, 4, barH, 2, 0x33FFFFFF);
		}

		// Footer.
		c.rect(x + 24, bodyBottom, W - 48, 1, Theme.DIVIDER);
		float fy = bodyBottom + 32;
		boolean resetHover = input.hover("sheet.reset", x + 22, fy - 18, 180, 36);
		float rh = this.anim.get("sheet.reset.h", resetHover, 18.0F);
		if (rh > 0.01F) {
			c.roundRect(x + 22, fy - 17, 172, 34, 17, Theme.alpha(0xFF3A3D40, rh));
		}
		c.icon("arrow-counter-clockwise", false, x + 42, fy, 17, Theme.mix(Theme.TEXT_SECONDARY, Theme.TEXT, rh));
		c.textMiddle(f.medium, "Reset to defaults", x + 58, fy, 14, Theme.mix(Theme.TEXT_SECONDARY, Theme.TEXT, rh));
		if (input.clicked("sheet.reset")) {
			for (SettingGroup group : shown.groups()) {
				for (Setting<?> setting : group.settings()) {
					setting.reset();
				}
			}
			VisorSounds.play(VisorSounds.RECENTER, 0.7F);
		}
		float doneW = 104;
		boolean doneHover = input.hover("sheet.done", x + W - 26 - doneW, fy - 18, doneW, 36);
		float dh = this.anim.get("sheet.done.h", doneHover, 18.0F);
		float dp = this.anim.get("sheet.done.p", input.held("sheet.done"), 30.0F);
		c.push();
		c.scale(1.0F - dp * 0.04F, x + W - 26 - doneW * 0.5F, fy);
		c.roundRect(x + W - 26 - doneW, fy - 18, doneW, 36, 18, Theme.mix(Theme.accent(), Theme.accentBright(), dh));
		c.textCentered(f.semibold, "Done", x + W - 26 - doneW * 0.5F, fy, 15, Theme.TEXT);
		c.pop();
		if (input.clicked("sheet.done")) {
			this.close();
		}

		c.popAlpha();
		c.pop();

		// Clicking (or right clicking) next to the sheet closes it.
		if (interactive && (in.pressed || in.secondaryPressed) && !in.inside(x, y, W, H)) {
			this.close();
		}
		if (interactive) {
			in.unhandledSecondary();
		}
	}

	private interface RowControl {
		void draw(UiInput input, float right, float centerY);
	}

	private float groupTitle(UiCanvas c, FontAtlas f, String title, float x, float y) {
		c.text(f.semibold, title.toUpperCase(java.util.Locale.ROOT), x + 30, y + 26, 11.5F, Theme.TEXT_MUTED, 0, 1.3F, 0);
		return y + GROUP;
	}

	private float row(UiCanvas c, UiInput in, FontAtlas f, String name, String description, float x, float y, float top, float bottom, RowControl control) {
		boolean visible = y + ROW > top && y < bottom;
		UiInput rowIn = visible && in.inside(x, Math.max(top, y), W, Math.min(bottom, y + ROW) - Math.max(top, y)) ? in : UiInput.INERT;
		if (visible) {
			float mid = y + ROW * 0.5F;
			c.textMiddle(f.medium, name, x + 30, mid - 9, 15, Theme.TEXT);
			c.textFit(f.regular, description, x + 30, mid + 12, 12.5F, 250, Theme.TEXT_MUTED);
			control.draw(rowIn, x + W - 30, mid);
			c.rect(x + 30, y + ROW - 1, W - 60, 1, 0x0FFFFFFF);
		}
		return y + ROW;
	}

	private float settingRow(UiCanvas c, UiInput in, FontAtlas f, Content shown, Setting<?> setting, float x, float y, float top, float bottom, float time) {
		String id = "sheet." + shown.key() + "." + setting.id();
		return this.row(c, in, f, setting.name(), setting.description(), x, y, top, bottom, (rowIn, right, mid) -> {
			if (setting instanceof BoolSetting bool) {
				if (Widgets.toggle(c, rowIn, this.anim, id, right - Widgets.SWITCH_W, mid - Widgets.SWITCH_H * 0.5F, bool.on())) {
					bool.toggle();
				}
			} else if (setting instanceof SliderSetting slider) {
				Widgets.slider(c, rowIn, this.anim, f, id, slider, right - 190, mid, 190);
			} else if (setting instanceof ModeSetting mode) {
				Widgets.segmented(c, rowIn, this.anim, f, id, mode, right, mid);
			} else if (setting instanceof ColorSetting color) {
				Widgets.swatches(c, rowIn, this.anim, id, color, right, mid);
			} else if (setting instanceof KeySetting key) {
				boolean capturing = VisorController.get().isCapturing(key);
				if (Widgets.keyButton(c, rowIn, this.anim, f, id, key, right, mid, capturing, time)) {
					VisorController.get().startKeyCapture(key);
				}
			}
		});
	}

	private float contentHeight(Content shown) {
		float h = 16;
		if (shown.module() instanceof DrDonutt) {
			h += DonutCard.HEIGHT + 10;
		}
		if (shown.module() != null) {
			h += GROUP + ROW * 2;
		}
		for (SettingGroup group : shown.groups()) {
			h += GROUP;
			for (Setting<?> setting : group.settings()) {
				if (setting.isVisible()) {
					h += ROW;
				}
			}
		}
		return h;
	}
}
