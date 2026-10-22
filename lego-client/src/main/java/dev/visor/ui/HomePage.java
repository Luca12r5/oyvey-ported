package dev.visor.ui;

import dev.visor.VisorController;
import dev.visor.browser.BrowserShortcuts;
import dev.visor.donut.DonutPreview;
import dev.visor.donut.DrDonutt;
import dev.visor.module.Module;
import dev.visor.module.ModuleManager;
import dev.visor.render.UiCanvas;
import dev.visor.render.text.FontAtlas;
import dev.visor.sound.VisorSounds;
import java.util.ArrayList;
import java.util.List;

/** The start page: the DrDonutt banner, the web (search and quick links) and recommended modules. */
public final class HomePage {
	private static final String[] FEATURED = {"zoom", "togglesprint", "keystrokes", "coords", "freelook", "crosshair", "waypoints", "colorfilter", "spotify", "drdonutt"};
	static final int FROSTING = 0xFFFF7EB6;
	static final int FROSTING_DARK = 0xFFD9327F;
	private static final int[] SPRINKLES = {0xFFFFE066, 0xFF7DF9FF, 0xFFFFFFFF, 0xFFB4F06C, 0xFFFFB067, 0xFFB98CFF};

	private final UiAnimations anim;
	private final MainPanel panel;
	private final DonutPreview preview = new DonutPreview();
	private final TextField webSearch = new TextField(256);
	private float scroll;
	private float scrollTarget;

	HomePage(MainPanel panel, UiAnimations anim) {
		this.panel = panel;
		this.anim = anim;
		this.webSearch.onSubmit(text -> {
			String url = BrowserShortcuts.resolve(text);
			if (url != null) {
				this.webSearch.clear();
				VisorController.get().showBrowser(url);
			}
		});
	}

	TextField webSearch() {
		return this.webSearch;
	}

	public void render(UiCanvas c, UiInput in, FontAtlas f, float x0, float x1, float time) {
		this.donutBanner(c, in, f, x0, 78, x1 - x0, 156, time);
		this.web(c, in, f, x0, x1, 258);
		this.recommended(c, in, f, x0, x1, 420);
	}

	/**
	 * The big DrDonutt banner. A click (or right click) opens his settings, where he is summoned,
	 * adjusted and switched off again; the little x next to "DANCING NOW" sends him home at once.
	 */
	private void donutBanner(UiCanvas c, UiInput in, FontAtlas f, float bx, float by, float bw, float bh, float time) {
		DrDonutt donut = DrDonutt.get();
		String id = "drdonutt";
		boolean hovered = in.hover(id, bx, by, bw, bh);
		float hover = this.anim.get(id + ".h", hovered, 12.0F);
		float press = this.anim.get(id + ".p", in.held(id), 30.0F);
		boolean dancing = donut.enabled();

		c.push();
		c.scale(1.0F + hover * 0.008F - press * 0.014F, bx + bw * 0.5F, by + bh * 0.5F);
		c.shadow(bx, by + 6, bw, bh, 14, 18 + hover * 12, Theme.alpha(0xFF000000, 0.35F + hover * 0.15F));
		if (hover > 0.01F) {
			c.shadow(bx, by, bw, bh, 14, 34, Theme.alpha(FROSTING, 0.32F * hover));
		}
		c.aurora(bx, by, bw, bh, 12, 0.62F, 1, 0xFFFFFFFF);
		c.roundRectGradient(bx, by, bw, bh * 0.55F, 12, 0x1CFFFFFF, 0x00FFFFFF);
		c.pushClip(bx + 6, by + 3, bw - 12, bh - 6);
		sprinkles(c, bx, by, bw, bh, time);
		c.popClip();

		// Doctor Donut himself, live and dancing, in front of a giant donut.
		float pw = 200;
		float px = bx + bw - pw - 6;
		float cx = px + pw * 0.5F;
		float cy = by + bh * 0.5F;
		frostedDonut(c, cx, cy, 64, time);
		c.shadow(cx - 34, by + bh - 13, 68, 7, 3.5F, 9, 0x70000000);
		float yaw = (float) Math.sin(time * 0.65F) * 26.0F;
		if (!this.preview.draw(c, px, by, pw, bh, donut.danceTime(time), yaw)) {
			c.arc(cx, cy, 15, 3.5F, time * 5.0F, 4.2F, 0xCCFFFFFF);
		}

		// Chip: what is new, or that he is out there dancing right now.
		float tx = bx + 26;
		float chipY = by + 16;
		String chip = dancing ? "DANCING NOW" : "NEW · 3D · WITH MUSIC";
		float chipW = f.bold.width(chip, 11, 1.4F) + 42;
		c.roundRect(tx - 4, chipY, chipW, 24, 12, 0x52000000);
		if (dancing) {
			float pulse = 0.5F + 0.5F * (float) Math.sin(time * 5.0F);
			c.circle(tx + 10, chipY + 12, 4.0F + pulse * 3.0F, Theme.alpha(Theme.ONLINE, 0.35F * (1.0F - pulse)));
			c.circle(tx + 10, chipY + 12, 4.0F, Theme.ONLINE);
		} else {
			c.icon("music-notes", true, tx + 10, chipY + 12, 14, Theme.TEXT);
		}
		c.text(f.bold, chip, tx + 23, chipY + 12 + f.bold.capHeight * 5.5F, 11, Theme.TEXT, 0, 1.4F, 0);
		if (dancing) {
			// A little x next to the chip sends him home.
			String stop = id + ".stop";
			float sx = tx - 4 + chipW + 16;
			float sy = chipY + 12;
			float stopHover = this.anim.get(stop + ".h", in.hover(stop, sx - 13, sy - 13, 26, 26), 18.0F);
			c.circle(sx, sy, 12, Theme.mix(0x52000000, Theme.TEXT, stopHover));
			c.icon("x", false, sx, sy, 13, Theme.mix(Theme.TEXT, FROSTING_DARK, stopHover));
			if (in.clicked(stop)) {
				donut.setEnabled(false);
				VisorSounds.play(VisorSounds.TOGGLE_OFF, 0.8F);
			}
		}

		// The call to action, nice and big.
		float line1 = by + 76;
		c.text(f.bold, "Click here to get", tx, line1 + 2, 34, 0x59000000, 0, -0.3F, 4.0F);
		c.text(f.bold, "Click here to get", tx, line1, 34, Theme.TEXT, 0, -0.3F, 0);
		float line2 = by + 137;
		c.text(f.bold, "DrDonutt.", tx, line2 + 3, 62, 0x66000000, 0, -1.0F, 5.0F);
		float end = c.text(f.bold, "DrDonutt.", tx, line2, 62, Theme.TEXT, 0, -1.0F, 0);
		float ax = end + 30 + hover * 5;
		float ay = line2 - f.bold.capHeight * 62 * 0.5F;
		c.shadow(ax - 19, ay - 19 + 3, 38, 38, 19, 8, Theme.alpha(0xFF000000, 0.25F));
		c.circle(ax, ay, 19, Theme.mix(0x40FFFFFF, Theme.TEXT, hover));
		c.icon("arrow-right", false, ax, ay, 20, Theme.mix(Theme.TEXT, FROSTING_DARK, hover));

		if (hover > 0.01F) {
			c.roundRectStroke(bx - 3, by - 3, bw + 6, bh + 6, 15, 2.5F, Theme.alpha(Theme.TEXT, hover * 0.9F));
		}
		c.pop();

		boolean clicked = in.clicked(id);
		if (in.secondaryClicked(bx, by, bw, bh) || clicked) {
			this.panel.openSettings(donut);
		}
	}

	/** Sugar sprinkles slowly drifting down through the banner. */
	static void sprinkles(UiCanvas c, float x, float y, float w, float h, float time) {
		for (int i = 0; i < 28; i++) {
			float column = fract(i * 0.618034F + 0.11F);
			float speed = 6.0F + fract(i * 0.3711F) * 10.0F;
			float fall = fract(i * 0.2357F + time * speed / (h + 24));
			float sx = x + 12 + column * (w - 24);
			float sy = y - 12 + fall * (h + 24);
			boolean upright = (i & 1) == 0;
			float sw = upright ? 3.5F : 10.0F;
			float sh = upright ? 10.0F : 3.5F;
			c.roundRect(sx - sw * 0.5F, sy - sh * 0.5F, sw, sh, 1.75F, Theme.alpha(SPRINKLES[i % SPRINKLES.length], 0.42F));
		}
	}

	/** A giant pink frosted donut with a slowly turning ring of sprinkles. */
	static void frostedDonut(UiCanvas c, float cx, float cy, float radius, float time) {
		float hole = radius * 0.36F;
		float middle = (radius + hole) * 0.5F;
		float thickness = radius - hole;
		float full = (float) (Math.PI * 2.0);
		c.shadow(cx - radius, cy - radius + 7, radius * 2, radius * 2, radius, 16, 0x40000000);
		c.arc(cx, cy, middle, thickness, 0, full, 0xFFE2A35E);
		c.arc(cx, cy, middle, thickness * 0.62F, 0, full, 0xFFC9853F);
		c.arc(cx, cy - 1.5F, middle, thickness * 0.80F, 0, full, FROSTING);
		c.arc(cx, cy - 2.0F, middle + thickness * 0.22F, thickness * 0.10F, -1.25F, 2.2F, 0x8CFFFFFF);
		for (int i = 0; i < 16; i++) {
			double angle = i * Math.PI * 2.0 / 16.0 + time * 0.22 + fract(i * 0.477F) * 0.35;
			float r = middle + (fract(i * 0.731F) - 0.5F) * thickness * 0.45F;
			float sx = cx + (float) Math.sin(angle) * r;
			float sy = cy - 1.5F - (float) Math.cos(angle) * r;
			boolean upright = (i % 3) == 0;
			float sw = upright ? 3.0F : 8.0F;
			float sh = upright ? 8.0F : 3.0F;
			c.roundRect(sx - sw * 0.5F, sy - sh * 0.5F, sw, sh, 1.5F, SPRINKLES[i % SPRINKLES.length]);
		}
	}

	private static float fract(float value) {
		return value - (float) Math.floor(value);
	}

	/** Search the web straight from home, plus a row of quick links that open the browser. */
	private void web(UiCanvas c, UiInput in, FontAtlas f, float x0, float x1, float titleY) {
		c.textMiddle(f.semibold, "Browse the Web", x0, titleY, 19, Theme.TEXT);
		String open = "Open browser";
		float openW = f.medium.width(open, 14);
		boolean openHover = in.hover("home.browser", x1 - openW - 26, titleY - 13, openW + 26, 26);
		float oh = this.anim.get("home.browser.h", openHover, 18.0F);
		int openColor = Theme.mix(Theme.TEXT_SECONDARY, Theme.accentBright(), oh);
		c.textRight(f.medium, open, x1 - 20, titleY, 14, openColor);
		c.icon("caret-right", false, x1 - 8, titleY, 15, openColor);
		if (in.clicked("home.browser")) {
			VisorController.get().showBrowser(null);
		}

		Widgets.searchPill(c, in, this.anim, f, "home.web", this.webSearch, x0, titleY + 16, x1 - x0, 40,
			BrowserShortcuts.searchPlaceholder(), "magnifying-glass", 14);

		List<BrowserShortcuts.Shortcut> shortcuts = BrowserShortcuts.all();
		float cell = (x1 - x0) / Math.max(1, shortcuts.size());
		float tileY = titleY + 92;
		for (int i = 0; i < shortcuts.size(); i++) {
			float cx = x0 + cell * (i + 0.5F);
			BrowserShortcuts.Shortcut shortcut = shortcuts.get(i);
			if (Widgets.shortcutTile(c, in, this.anim, f, "home.shortcut." + i, shortcut, cx, tileY, 44, 12)) {
				VisorController.get().showBrowser(shortcut.url());
			}
		}
	}

	private void recommended(UiCanvas c, UiInput in, FontAtlas f, float x0, float x1, float sy) {
		c.textMiddle(f.semibold, "Recommended For You", x0, sy, 19, Theme.TEXT);
		List<Module> featured = new ArrayList<>();
		for (String id : FEATURED) {
			Module module = ModuleManager.get(id);
			if (module != null) {
				featured.add(module);
			}
		}
		long enabledHere = featured.stream().filter(Module::enabled).count();
		c.textRight(f.regular, enabledHere + " enabled", x1 - 30, sy, 14, Theme.TEXT_MUTED);
		c.icon("caret-down", false, x1 - 8, sy, 16, Theme.TEXT_SECONDARY);

		float cy = sy + 18;
		float cardW = 152;
		float cardH = MainPanel.HEIGHT - cy - 20;
		float gap = 12;
		float contentW = featured.size() * (cardW + gap) - gap;
		float maxScroll = Math.max(0, contentW - (x1 - x0));
		if (in.inside(x0, cy, x1 - x0, cardH) && in.scroll != 0) {
			this.scrollTarget = Math.max(0, Math.min(maxScroll, this.scrollTarget - in.scroll * 90));
		}
		this.scroll = this.anim.get("home.scroll", this.scrollTarget, 14.0F);
		c.pushClip(x0 - 6, cy - 6, x1 - x0 + 12, cardH + 16);
		for (int i = 0; i < featured.size(); i++) {
			float mx = x0 + i * (cardW + gap) - this.scroll;
			if (mx > x1 + 10 || mx + cardW < x0 - 10) {
				continue;
			}
			boolean inView = mx + cardW > x0 && mx < x1;
			this.card(c, inView ? in : UiInput.INERT, f, featured.get(i), mx, cy, cardW, cardH);
		}
		c.popClip();
		if (this.scroll > 1) {
			c.roundRectGradientH(x0 - 6, cy - 4, 36, cardH + 8, 0, Theme.CONTENT, Theme.alpha(Theme.CONTENT, 0));
		}
		if (this.scroll < maxScroll - 1) {
			c.roundRectGradientH(x1 - 30, cy - 4, 36, cardH + 8, 0, Theme.alpha(Theme.CONTENT, 0), Theme.CONTENT);
		}
	}

	private void card(UiCanvas c, UiInput in, FontAtlas f, Module module, float x, float y, float w, float h) {
		String id = "home." + module.id();
		boolean hovered = in.hover(id, x, y, w, h);
		float hover = this.anim.get(id + ".h", hovered, 16.0F);
		float press = this.anim.get(id + ".p", in.held(id), 30.0F);
		float on = this.anim.get(id + ".on", module.enabled(), 14.0F);
		float lift = hover * 4 - press * 2;
		c.push();
		c.scale(1.0F + hover * 0.025F - press * 0.03F, x + w * 0.5F, y + h * 0.5F);
		c.translate(0, -lift);
		c.shadow(x, y + 6, w, h, 12, 10 + hover * 10, Theme.alpha(0xFF000000, 0.25F + hover * 0.2F));
		float thumbH = h - 44;
		c.roundRect(x, y, w, h, 10, Theme.mix(Theme.CARD, Theme.CARD_HOVER, hover));
		c.pushClip(x, y, w, thumbH);
		int palette = switch (module.category()) {
			case COMBAT -> 6;
			case MOVEMENT -> 3;
			case RENDER -> 2;
			case PLAYER -> 5;
			case WORLD -> 0;
			case MISC -> 4;
		};
		c.aurora(x, y, w, thumbH + 12, 10, (module.id().hashCode() & 0xFFFF) / 6553.6F, palette, Theme.mix(0xFF8C8C8C, 0xFFFFFFFF, 0.55F + on * 0.45F));
		c.popClip();
		c.icon(module.icon(), true, x + w * 0.5F, y + thumbH * 0.5F, 38, Theme.alpha(Theme.TEXT, 0.85F + hover * 0.15F));
		c.textFit(f.medium, module.name(), x + 12, y + thumbH + 22, 14, w - 62, Theme.TEXT);
		float sw = 32;
		float sh = 18;
		float sx = x + w - sw - 10;
		float syy = y + thumbH + 13;
		c.roundRect(sx, syy, sw, sh, sh * 0.5F, Theme.mix(Theme.CONTROL_OFF, Theme.accentBright(), on));
		float knob = sh - 4;
		c.circle(sx + 2 + knob * 0.5F + (sw - knob - 4) * on, syy + sh * 0.5F, knob * 0.5F, Theme.TEXT);
		if (hover > 0.01F) {
			c.roundRectStroke(x - 2, y - 2, w + 4, h + 4, 12, 2.0F, Theme.alpha(Theme.accentBright(), hover * 0.9F));
		}
		c.pop();
		if (in.clicked(id)) {
			module.toggle();
			VisorSounds.play(module.enabled() ? VisorSounds.TOGGLE_ON : VisorSounds.TOGGLE_OFF, 0.8F);
		}
		if (in.secondaryClicked(x, y, w, h)) {
			this.panel.openSettings(module);
		}
	}
}
