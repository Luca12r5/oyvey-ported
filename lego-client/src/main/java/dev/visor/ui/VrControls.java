package dev.visor.ui;

import dev.visor.browser.BrowserSession;
import dev.visor.browser.MediaState;
import dev.visor.render.UiCanvas;
import dev.visor.render.UiResources;
import dev.visor.render.text.FontAtlas;
import dev.visor.render.text.SdfFont;
import dev.visor.sound.VisorSounds;
import dev.visor.vr.VrFormat;
import dev.visor.vr.VrMode;
import java.util.Locale;
import org.jspecify.annotations.Nullable;

/**
 * The controls floating below the view while watching in VR: title, the time line, play/pause,
 * ten seconds back or ahead, sound, the VR layout of the video and the way out.
 */
public final class VrControls {
	public static final float WIDTH = 900.0F;
	public static final float HEIGHT = 196.0F;
	private static final float PAD = 30.0F;
	private static final float BAR_X = 98.0F;
	private static final float BAR_W = WIDTH - BAR_X * 2.0F;

	private final UiAnimations anim = new UiAnimations();
	private boolean seeking;
	private float seekFraction;

	public void render(UiCanvas c, UiInput in, float dt, float time, VrMode vr, @Nullable MediaState media) {
		FontAtlas f = UiResources.atlas();
		BrowserSession session = BrowserSession.get();
		this.anim.beginFrame(dt);
		c.shadow(0, 10, WIDTH, HEIGHT, 26, 40, 0x88000000);
		c.roundRect(0, 0, WIDTH, HEIGHT, 26, 0xF0141517);
		c.roundRectGradient(0, 0, WIDTH, HEIGHT * 0.5F, 26, 0x12FFFFFF, 0x00FFFFFF);
		c.roundRectStroke(0, 0, WIDTH, HEIGHT, 26, 1.0F, 0x1CFFFFFF);

		// Title row, with the way out on the right.
		float row1 = 42;
		float exitW = 132;
		float exitX = WIDTH - PAD - exitW;
		if (this.pill(c, in, f, "vr.exit", "sign-out", "Exit VR", exitX, row1 - 20, exitW, 40, Theme.mix(Theme.accent(), Theme.accentBright(), 0.3F))) {
			vr.exit();
		}
		float titleRight = exitX - 16;
		float tx = PAD;
		boolean ad = media != null && media.ad();
		if (ad) {
			c.roundRect(tx, row1 - 12, 40, 24, 6, 0xFFF5B400);
			c.textCentered(f.bold, "Ad", tx + 20, row1, 13, 0xFF1A1A1A);
			tx += 52;
			if (media.skippable()) {
				float skipW = 116;
				if (this.pill(c, in, f, "vr.skip", "arrow-right", "Skip ad", titleRight - skipW, row1 - 20, skipW, 40, 0xFF3A3D41)) {
					session.mediaCommand("skip", 0, 0);
				}
				titleRight -= skipW + 12;
			}
		}
		String title = media == null ? "Waiting for the video…" : printable(f.semibold, media.title());
		c.textFit(f.semibold, title.isEmpty() ? "Video" : title, tx, row1, 17, titleRight - tx, Theme.TEXT);

		// Time line.
		float row2 = 98;
		double duration = media != null ? media.duration() : 0.0;
		double now = media != null ? media.time() : 0.0;
		float played = duration > 0.0 ? (float) Math.min(1.0, now / duration) : 0.0F;
		boolean hover = in.hover("vr.seek", BAR_X - 8, row2 - 16, BAR_W + 16, 32) && duration > 0.0;
		if (in.dragging("vr.seek") && duration > 0.0) {
			this.seeking = true;
			this.seekFraction = Math.max(0.0F, Math.min(1.0F, (in.x - BAR_X) / BAR_W));
		} else if (this.seeking) {
			this.seeking = false;
			session.mediaCommand("seekTo", this.seekFraction, 0);
			VisorSounds.play(VisorSounds.CLICK, 0.5F);
		}
		float shown = this.seeking ? this.seekFraction : this.anim.get("vr.played", played, 20.0F);
		float grow = this.anim.get("vr.seek.h", hover || this.seeking, 18.0F);
		float trackH = 6 + grow * 3;
		c.roundRect(BAR_X, row2 - trackH * 0.5F, BAR_W, trackH, trackH * 0.5F, 0x33FFFFFF);
		c.roundRect(BAR_X, row2 - trackH * 0.5F, Math.max(trackH, BAR_W * shown), trackH, trackH * 0.5F, Theme.accentBright());
		float knob = 9 + grow * 4;
		c.shadow(BAR_X + BAR_W * shown - knob, row2 - knob + 1.5F, knob * 2, knob * 2, knob, 5, 0x55000000);
		c.circle(BAR_X + BAR_W * shown, row2, knob, Theme.TEXT);
		String elapsed = clock(this.seeking ? this.seekFraction * duration : now);
		c.textRight(f.medium, elapsed, BAR_X - 14, row2, 13.5F, Theme.TEXT_SECONDARY);
		c.textMiddle(f.medium, duration > 0.0 ? clock(duration) : "Live", BAR_X + BAR_W + 14, row2, 13.5F, Theme.TEXT_SECONDARY);

		// Buttons.
		float row3 = 152;
		if (Widgets.iconButton(c, in, this.anim, "vr.recenter", "arrows-clockwise", PAD + 22, row3, 22, 0xFF2C2F33)) {
			vr.recenter();
			VisorSounds.play(VisorSounds.RECENTER, 0.6F);
		}
		boolean muted = media != null && media.muted();
		if (Widgets.iconButton(c, in, this.anim, "vr.mute", muted ? "speaker-slash" : "speaker-high", PAD + 76, row3, 22, 0xFF2C2F33)) {
			session.mediaCommand("mute", 0, 0);
			VisorSounds.play(VisorSounds.CLICK, 0.6F);
		}
		// Play and skip sit in the middle of the room left between the small buttons and the layouts.
		float center = WIDTH * 0.5F - 108;
		if (this.skip(c, in, f, "vr.back", "arrow-counter-clockwise", center - 74, row3)) {
			session.mediaCommand("seek", -10, 0);
		}
		if (this.skip(c, in, f, "vr.ahead", "arrow-clockwise", center + 74, row3)) {
			session.mediaCommand("seek", 10, 0);
		}
		boolean paused = media == null || media.paused();
		boolean playHover = in.hover("vr.play", center - 30, row3 - 30, 60, 60);
		float ph = this.anim.get("vr.play.h", playHover, 18.0F);
		float pp = this.anim.get("vr.play.p", in.held("vr.play"), 30.0F);
		float r = 28 * (1.0F + ph * 0.05F - pp * 0.07F);
		c.shadow(center - r, row3 - r + 3, r * 2, r * 2, r, 12, Theme.alpha(Theme.accentBright(), 0.35F + ph * 0.25F));
		c.circle(center, row3, r, Theme.mix(Theme.TEXT, 0xFFE9ECF1, ph));
		if (paused) {
			c.playTriangle(center + r * 0.08F, row3, r * 0.62F, r * 0.72F, r * 0.08F, 0xFF141517);
		} else {
			c.roundRect(center - r * 0.30F, row3 - r * 0.34F, r * 0.20F, r * 0.68F, r * 0.06F, 0xFF141517);
			c.roundRect(center + r * 0.10F, row3 - r * 0.34F, r * 0.20F, r * 0.68F, r * 0.06F, 0xFF141517);
		}
		if (in.clicked("vr.play")) {
			session.mediaCommand("toggle", 0, 0);
			VisorSounds.play(VisorSounds.CLICK, 0.7F);
		}
		this.formats(c, in, f, vr, WIDTH - PAD, row3);
	}

	/** Round button with an arrow and "10" in it. */
	private boolean skip(UiCanvas c, UiInput in, FontAtlas f, String id, String icon, float cx, float cy) {
		boolean hovered = in.hover(id, cx - 24, cy - 24, 48, 48);
		float hover = this.anim.get(id + ".h", hovered, 18.0F);
		float press = this.anim.get(id + ".p", in.held(id), 30.0F);
		float r = 24 * (1.0F - press * 0.08F);
		if (hover > 0.01F) {
			c.circle(cx, cy, r, Theme.alpha(0xFF3A3D41, hover));
		}
		int color = Theme.mix(0xFFC4C7CC, Theme.TEXT, hover);
		c.icon(icon, false, cx, cy, 38 * (1.0F - press * 0.08F), color);
		c.textCentered(f.bold, "10", cx, cy + 1, 10.5F, color);
		boolean clicked = in.clicked(id);
		if (clicked) {
			VisorSounds.play(VisorSounds.CLICK, 0.6F);
		}
		return clicked;
	}

	private boolean pill(UiCanvas c, UiInput in, FontAtlas f, String id, String icon, String label, float x, float y, float w, float h, int fill) {
		boolean hovered = in.hover(id, x, y, w, h);
		float hover = this.anim.get(id + ".h", hovered, 18.0F);
		float press = this.anim.get(id + ".p", in.held(id), 30.0F);
		c.push();
		c.scale(1.0F - press * 0.04F, x + w * 0.5F, y + h * 0.5F);
		c.roundRect(x, y, w, h, h * 0.5F, Theme.mix(fill, Theme.mix(fill, 0xFFFFFFFF, 0.25F), hover));
		float textW = f.semibold.width(label, 14);
		float start = x + (w - textW - 26) * 0.5F;
		c.icon(icon, false, start + 9, y + h * 0.5F, 18, Theme.TEXT);
		c.textMiddle(f.semibold, label, start + 26, y + h * 0.5F, 14, Theme.TEXT);
		c.pop();
		boolean clicked = in.clicked(id);
		if (clicked) {
			VisorSounds.play(VisorSounds.CLICK, 0.7F);
		}
		return clicked;
	}

	/** How the video wraps around: the five layouts as a segmented control, right-aligned. */
	private void formats(UiCanvas c, UiInput in, FontAtlas f, VrMode vr, float right, float cy) {
		VrFormat[] formats = VrFormat.values();
		float size = 13.0F;
		float pad = 12.0F;
		float h = 36.0F;
		float[] widths = new float[formats.length];
		float total = 4.0F;
		for (int i = 0; i < formats.length; i++) {
			widths[i] = f.medium.width(formats[i].label, size) + pad * 2.0F;
			total += widths[i];
		}
		float x0 = right - total;
		c.roundRect(x0, cy - h * 0.5F, total, h, h * 0.5F, 0xFF26282B);
		float cursor = x0 + 2.0F;
		float selectedX = cursor;
		float selectedW = widths[0];
		for (int i = 0; i < formats.length; i++) {
			if (formats[i] == vr.format()) {
				selectedX = cursor;
				selectedW = widths[i];
			}
			cursor += widths[i];
		}
		float sx = this.anim.get("vr.format.x", selectedX, 18.0F);
		float sw = this.anim.get("vr.format.w", selectedW, 18.0F);
		c.roundRect(sx, cy - h * 0.5F + 2.0F, sw, h - 4.0F, (h - 4.0F) * 0.5F, Theme.accent());
		cursor = x0 + 2.0F;
		for (int i = 0; i < formats.length; i++) {
			String id = "vr.format." + i;
			boolean hovered = in.hover(id, cursor, cy - h * 0.5F, widths[i], h);
			boolean selected = formats[i] == vr.format();
			c.textCentered(f.medium, formats[i].label, cursor + widths[i] * 0.5F, cy, size, selected || hovered ? Theme.TEXT : Theme.TEXT_SECONDARY);
			if (in.clicked(id) && !selected) {
				vr.setFormat(formats[i]);
				VisorSounds.play(VisorSounds.TAB, 0.7F);
			}
			cursor += widths[i];
		}
	}

	/** The title without what the font cannot draw (emoji and the like). */
	private static String printable(SdfFont font, String text) {
		StringBuilder out = new StringBuilder(text.length());
		text.codePoints().filter(codepoint -> codepoint == ' ' || font.has(codepoint)).forEach(out::appendCodePoint);
		return out.toString().replaceAll("\\s+", " ").trim();
	}

	private static String clock(double seconds) {
		long total = Math.max(0L, Math.round(seconds));
		long hours = total / 3600L;
		long minutes = total / 60L % 60L;
		long rest = total % 60L;
		return hours > 0 ? String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, rest) : String.format(Locale.ROOT, "%d:%02d", minutes, rest);
	}
}
