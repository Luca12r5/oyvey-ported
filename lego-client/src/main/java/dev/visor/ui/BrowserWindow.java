package dev.visor.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import dev.visor.VisorController;
import dev.visor.browser.BrowserSession;
import dev.visor.browser.BrowserShortcuts;
import dev.visor.browser.MediaState;
import dev.visor.render.BrowserTexture;
import dev.visor.render.UiCanvas;
import dev.visor.render.UiResources;
import dev.visor.render.text.FontAtlas;
import dev.visor.setting.VisorSettings;
import dev.visor.sound.VisorSounds;
import dev.visor.vr.VrFormat;
import dev.visor.vr.VrMode;
import java.util.List;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

/**
 * The browser app: a large curved window with a toolbar (back, forward, reload, address bar and
 * window buttons) above either the live web page or the start page with search and quick links.
 * Right click anywhere opens the browser settings.
 *
 * <p>When the page goes fullscreen the window turns into a cinema screen: the toolbar and frame
 * fade away and the page grows to the whole width, while the controller makes the window large
 * and flat and dims the world around it.</p>
 */
public final class BrowserWindow {
	public static final float WIDTH = 1180.0F;
	public static final float HEIGHT = 734.0F;
	private static final float PAD = 14.0F;
	private static final float BAR_H = 58.0F;
	public static final float CONTENT_X = PAD;
	public static final float CONTENT_Y = PAD + BAR_H;
	public static final float CONTENT_W = WIDTH - PAD * 2.0F;
	public static final float CONTENT_H = HEIGHT - CONTENT_Y - PAD;
	/** The page as a cinema screen: the whole width, in the page's 16:9 shape, centred. */
	public static final float CINEMA_H = WIDTH * BrowserSession.VIEW_HEIGHT / BrowserSession.VIEW_WIDTH;
	public static final float CINEMA_Y = (HEIGHT - CINEMA_H) * 0.5F;
	private static final float CONTENT_RADIUS = 10.0F;
	private static final float BUTTON_Y = PAD + BAR_H * 0.5F - 3.0F;
	/** How long the "Esc to exit" hint stays after a video went fullscreen. */
	private static final float HINT_SECONDS = 3.0F;

	private final UiAnimations anim = new UiAnimations();
	private final SettingsSheet sheet = new SettingsSheet();
	private final TextField address = new TextField(2048);
	private final TextField search = new TextField(256);
	private boolean startPage = true;
	private boolean pageDown;
	private boolean muted;
	private int openedVersion = -1;
	private float cinemaTime;
	private final float[] glow = new float[12];

	public BrowserWindow() {
		this.address.onSubmit(this::go);
		this.search.onSubmit(this::go);
	}

	// ------------------------------------------------------------------------------------------
	// API for the controller and the dock

	/** Opens a page, or the start page when {@code url} is null (starting the browser if needed). */
	public void open(@Nullable String url) {
		if (url != null) {
			this.startPage = false;
			this.openedVersion = BrowserTexture.get().pageVersion();
		} else if (!BrowserSession.get().running()) {
			this.startPage = true;
		}
		BrowserSession.get().open(url);
	}

	public boolean showsStartPage() {
		return this.startPage;
	}

	public void openSettings() {
		this.sheet.show(SettingsSheet.Content.browser());
	}

	public boolean sheetOpen() {
		return this.sheet.isOpen();
	}

	public void closeSheet() {
		this.sheet.close();
	}

	/** The browser was closed: next time the window opens on the start page. */
	public void reset() {
		this.startPage = true;
		this.pageDown = false;
		this.sheet.close();
		this.search.clear();
		BrowserTexture.get().clear();
		this.openedVersion = BrowserTexture.get().pageVersion();
	}

	// ------------------------------------------------------------------------------------------

	public void render(UiCanvas c, UiInput in, float dt, float time) {
		FontAtlas f = UiResources.atlas();
		VisorController controller = VisorController.get();
		BrowserSession session = BrowserSession.get();
		this.anim.beginFrame(dt);
		session.setZoom(VisorSettings.BROWSER_ZOOM.floatValue());
		session.setMediaVolume(this.muted ? 0.0F : VisorSettings.BROWSER_VOLUME.floatValue());
		session.setFrameRate(VisorSettings.BROWSER_FPS.is("30 fps") ? 30 : 60);
		if (session.running() && !this.startPage) {
			session.markVisible();
		}

		float cinema = controller.cinema();
		this.cinemaTime = controller.cinemaTarget() ? this.cinemaTime + dt : 0.0F;
		UiInput pageIn = this.sheet.blocksInput() ? UiInput.INERT : in;
		UiInput chromeIn = cinema > 0.05F ? UiInput.INERT : pageIn;
		c.pushAlpha(1.0F - cinema);
		c.shadow(0, 8, WIDTH, HEIGHT, Theme.PANEL_RADIUS, 34, 0x66000000);
		c.roundRect(0, 0, WIDTH, HEIGHT, Theme.PANEL_RADIUS, Theme.SIDEBAR);
		c.roundRectStroke(0, 0, WIDTH, HEIGHT, Theme.PANEL_RADIUS, 1.0F, 0x14FFFFFF);
		this.toolbar(c, chromeIn, f, session, controller, time);
		c.popAlpha();
		this.content(c, pageIn, f, session, controller, time, cinema, dt);

		// Clicking somewhere else ends typing in the address bar or the start page search.
		if (pageIn.pressed && controller.isFocused(this.address) && !"browser.address".equals(pageIn.hovered())) {
			controller.unfocusText(this.address);
		}
		if (pageIn.pressed && controller.isFocused(this.search) && !"browser.search".equals(pageIn.hovered())) {
			controller.unfocusText(this.search);
		}
		if (!this.sheet.blocksInput() && cinema < 0.05F && in.unhandledSecondary()) {
			this.openSettings();
		}
		this.sheet.render(c, in, dt, time, WIDTH, HEIGHT);
	}

	private void toolbar(UiCanvas c, UiInput in, FontAtlas f, BrowserSession s, VisorController controller, float time) {
		float cy = BUTTON_Y;
		boolean page = !this.startPage && s.ready();
		if (this.button(c, in, "browser.back", "arrow-left", 38, cy, page && s.canGoBack())) {
			s.back();
		}
		if (this.button(c, in, "browser.forward", "arrow-right", 78, cy, page && s.canGoForward())) {
			s.forward();
		}
		boolean loading = page && s.loading();
		if (this.button(c, in, "browser.reload", loading ? "x" : "arrow-clockwise", 118, cy, page)) {
			if (loading) {
				s.stopLoading();
			} else {
				s.reload();
			}
		}

		float rx = WIDTH - 38;
		if (this.button(c, in, "browser.close", "x", rx, cy, true)) {
			controller.closeBrowser();
		}
		if (this.button(c, in, "browser.minimize", "minus", rx - 40, cy, true)) {
			controller.showHome();
		}
		if (this.button(c, in, "browser.external", "arrow-square-out", rx - 80, cy, page && s.url().startsWith("http"))) {
			Util.getPlatform().openUri(s.url());
		}
		if (this.button(c, in, "browser.home", "house-simple", rx - 120, cy, true)) {
			this.startPage = true;
			controller.stopPageTyping(false);
		}
		if (this.button(c, in, "browser.mute", this.muted ? "speaker-slash" : "speaker-high", rx - 160, cy, true)) {
			this.muted = !this.muted;
		}
		MediaState media = page ? s.media() : null;
		if (this.button(c, in, "browser.fullscreen", "arrows-out", rx - 200, cy, media != null)) {
			if (s.pageFullscreen()) {
				s.exitFullscreen();
			} else {
				s.mediaCommand("fullscreen", 0, 0);
			}
		}
		// The VR button grows into "Watch in VR" while the page plays a 360° or 180° video.
		VrMode vr = VrMode.get();
		float offer = this.anim.get("browser.vr.offer", vr.offers(media), 10.0F);
		float vrRight = rx - 223;
		float vrWidth = 34 + offer * 112;
		if (this.vrButton(c, in, f, vrRight - vrWidth, cy, vrWidth, offer, media != null, time)) {
			vr.enter(media.format().isEmpty() ? VrFormat.FLAT : VrFormat.of(media.format()));
		}

		float ax = 148;
		float aw = vrRight - vrWidth - 12 - ax;
		boolean loadingBar = loading;
		this.addressBar(c, in, f, s, controller, ax, cy - 19, aw, 38);
		float load = this.anim.get("browser.loading", loadingBar, 9.0F);
		if (load > 0.01F) {
			float x0 = ax + 18;
			float w = aw - 36;
			float t = (time * 0.85F) % 1.0F;
			float segment = w * 0.3F;
			c.pushClip(x0, cy + 21, w, 4);
			c.roundRect(x0 + (w + segment) * t - segment, cy + 21.5F, segment, 2.5F, 1.25F, Theme.alpha(Theme.accentBright(), load));
			c.popClip();
		}
		this.tooltip(c, in, f, s);
	}

	private boolean button(UiCanvas c, UiInput in, String id, String icon, float cx, float cy, boolean enabled) {
		float r = 17;
		boolean hovered = in.hover(id, cx - r, cy - r, r * 2, r * 2) && enabled;
		float hover = this.anim.get(id + ".h", hovered, 18.0F);
		float press = this.anim.get(id + ".p", enabled && in.held(id), 30.0F);
		if (hover > 0.01F) {
			c.circle(cx, cy, r * (1.0F - press * 0.08F), Theme.alpha(0xFF3A3D41, hover));
		}
		int color = enabled ? Theme.mix(0xFFC4C7CC, Theme.TEXT, hover) : 0xFF56595E;
		c.icon(icon, false, cx, cy, 19 * (1.0F - press * 0.08F), color);
		boolean clicked = enabled && in.clicked(id);
		if (clicked) {
			VisorSounds.play(VisorSounds.CLICK, 0.6F);
		}
		return clicked;
	}

	/** The goggles button; as a pill with "Watch in VR" (and a soft pulse) while {@code offer} is 1. */
	private boolean vrButton(UiCanvas c, UiInput in, FontAtlas f, float x, float cy, float w, float offer, boolean enabled, float time) {
		String id = "browser.vr";
		float h = 34;
		boolean hovered = in.hover(id, x, cy - h * 0.5F, w, h) && enabled;
		float hover = this.anim.get(id + ".h", hovered, 18.0F);
		float press = this.anim.get(id + ".p", enabled && in.held(id), 30.0F);
		c.push();
		c.scale(1.0F - press * 0.05F, x + w * 0.5F, cy);
		if (offer > 0.01F) {
			float pulse = 0.5F + 0.5F * (float) Math.sin(time * 3.2F);
			c.shadow(x, cy - h * 0.5F + 2, w, h, h * 0.5F, 10 + pulse * 6, Theme.alpha(Theme.accentBright(), offer * (0.25F + 0.2F * pulse)));
			c.roundRect(x, cy - h * 0.5F, w, h, h * 0.5F, Theme.alpha(Theme.mix(Theme.accent(), Theme.accentBright(), 0.35F + hover * 0.4F), offer));
		}
		if (hover > 0.01F && offer < 0.99F) {
			c.roundRect(x, cy - h * 0.5F, w, h, h * 0.5F, Theme.alpha(0xFF3A3D41, hover * (1.0F - offer)));
		}
		int color = enabled ? Theme.mix(Theme.mix(0xFFC4C7CC, Theme.TEXT, hover), Theme.TEXT, offer) : 0xFF56595E;
		float iconX = x + 17;
		c.icon("virtual-reality", offer > 0.5F, iconX, cy, 20, color);
		if (offer > 0.01F) {
			c.pushClip(x, cy - h * 0.5F, w - 6, h);
			c.pushAlpha(offer);
			c.textMiddle(f.semibold, "Watch in VR", iconX + 16, cy, 13.5F, Theme.TEXT);
			c.popAlpha();
			c.popClip();
		}
		c.pop();
		boolean clicked = enabled && in.clicked(id);
		if (clicked) {
			VisorSounds.play(VisorSounds.CLICK, 0.7F);
		}
		return clicked;
	}

	private void tooltip(UiCanvas c, UiInput in, FontAtlas f, BrowserSession s) {
		String id = in.hovered();
		String label = id == null ? null : switch (id) {
			case "browser.back" -> "Back";
			case "browser.forward" -> "Forward";
			case "browser.reload" -> "Reload";
			case "browser.close" -> "Close browser";
			case "browser.minimize" -> "Back to the home panel";
			case "browser.external" -> "Open on the desktop";
			case "browser.home" -> "Start page";
			case "browser.mute" -> this.muted ? "Unmute" : "Mute";
			case "browser.fullscreen" -> s.pageFullscreen() ? "Exit fullscreen" : "Fullscreen video";
			case "browser.vr" -> "Watch the video in VR";
			default -> null;
		};
		float show = this.anim.get("browser.tooltip", label != null, 14.0F);
		if (label == null || show < 0.01F) {
			return;
		}
		float x = Math.max(PAD, Math.min(WIDTH - PAD, in.x));
		float tw = f.medium.width(label, 12.5F) + 22;
		float tx = Math.max(PAD, Math.min(WIDTH - PAD - tw, x - tw * 0.5F));
		float ty = BUTTON_Y + 26;
		c.pushAlpha(show);
		c.shadow(tx, ty + 2, tw, 28, 14, 10, 0x55000000);
		c.roundRect(tx, ty, tw, 28, 14, 0xF2232528);
		c.textCentered(f.medium, label, tx + tw * 0.5F, ty + 14, 12.5F, Theme.TEXT);
		c.popAlpha();
	}

	private void addressBar(UiCanvas c, UiInput in, FontAtlas f, BrowserSession s, VisorController controller, float x, float y, float w, float h) {
		if (controller.isFocused(this.address)) {
			Widgets.searchPill(c, in, this.anim, f, "browser.address", this.address, x, y, w, h, BrowserShortcuts.searchPlaceholder(), "magnifying-glass", 14);
			return;
		}
		boolean hovered = in.hover("browser.address", x, y, w, h);
		float hover = this.anim.get("browser.address.h", hovered, 18.0F);
		c.roundRect(x, y, w, h, h * 0.5F, Theme.mix(0xFF2E3033, 0xFF383B3E, hover));
		float cy = y + h * 0.5F;
		String url = this.startPage ? "" : s.url();
		if (url.isEmpty() || "about:blank".equals(url)) {
			url = "";
			c.icon("magnifying-glass", false, x + 22, cy, 17, Theme.TEXT_SECONDARY);
			c.textFit(f.regular, BrowserShortcuts.searchPlaceholder(), x + 40, cy, 14, w - 62, Theme.TEXT_MUTED);
		} else {
			boolean secure = url.startsWith("https://");
			c.icon(secure ? "lock-simple" : "globe-simple", false, x + 22, cy, 16, secure ? Theme.TEXT_SECONDARY : Theme.TEXT_MUTED);
			String rest = url.replaceFirst("^[a-zA-Z][a-zA-Z0-9+.-]*://", "");
			int slash = rest.indexOf('/');
			String host = slash < 0 ? rest : rest.substring(0, slash);
			String path = slash < 0 ? "" : rest.substring(slash);
			float maxWidth = w - 62;
			float hostWidth = f.medium.width(host, 14);
			if (hostWidth >= maxWidth || path.isEmpty() || "/".equals(path)) {
				c.textFit(f.medium, host, x + 40, cy, 14, maxWidth, Theme.TEXT);
			} else {
				float end = c.textMiddle(f.medium, host, x + 40, cy, 14, Theme.TEXT);
				c.textFit(f.regular, path, end, cy, 14, maxWidth - hostWidth, Theme.TEXT_MUTED);
			}
		}
		if (in.clicked("browser.address")) {
			this.address.setText(url);
			this.address.selectAll();
			controller.focusText(this.address);
			VisorSounds.play(VisorSounds.CLICK, 0.6F);
		}
	}

	private void content(UiCanvas c, UiInput in, FontAtlas f, BrowserSession s, VisorController controller, float time, float cinema, float dt) {
		// In fullscreen the page grows from its place under the toolbar to the whole width.
		float x = Ease.lerp(cinema, CONTENT_X, 0.0F);
		float y = Ease.lerp(cinema, CONTENT_Y, CINEMA_Y);
		float w = Ease.lerp(cinema, CONTENT_W, WIDTH);
		float h = Ease.lerp(cinema, CONTENT_H, CINEMA_H);
		float radius = Ease.lerp(cinema, CONTENT_RADIUS, 4.0F);
		if (this.startPage) {
			this.releasePointer(s);
			this.startPage(c, in, f, x, y, w, h, time);
			return;
		}
		BrowserTexture texture = BrowserTexture.get();
		if (cinema > 0.01F && VisorSettings.AMBIENT_GLOW.on()) {
			this.ambientGlow(c, texture.pageEdges(), x, y, w, h, cinema, dt);
		}
		c.roundRect(x, y, w, h, radius, cinema > 0.5F ? 0xFF000000 : 0xFF141518);
		switch (s.status()) {
			case READY -> {
				GpuTextureView view = texture.page();
				if (view != null && texture.pageVersion() != this.openedVersion) {
					c.image(view, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR), x, y, w, h, radius, 0xFFFFFFFF);
				} else {
					this.spinner(c, f, x + w * 0.5F, y + h * 0.5F, time, "Loading…");
				}
				this.pageInput(c, in, f, s, controller, x, y, w, h);
				this.exitHint(c, f, x, y, w, cinema);
			}
			case STARTING, IDLE -> {
				this.releasePointer(s);
				this.spinner(c, f, x + w * 0.5F, y + h * 0.5F, time, "Starting " + s.browserName() + "…");
			}
			case NO_BROWSER -> {
				this.releasePointer(s);
				this.message(c, in, f, x, y, w, h, "No browser found",
					"Install Google Chrome or Microsoft Edge to browse the web in the headset.", null);
			}
			case FAILED -> {
				this.releasePointer(s);
				if (this.message(c, in, f, x, y, w, h, "The browser stopped", s.problem(), "Restart")) {
					String last = s.url();
					s.open(last.isEmpty() || "about:blank".equals(last) ? null : last);
				}
			}
		}
	}

	/**
	 * Light of the video spilling round the cinema screen: each side glows in the colours along
	 * that edge of the picture, following them smoothly.
	 */
	private void ambientGlow(UiCanvas c, int[] edges, float x, float y, float w, float h, float cinema, float dt) {
		float follow = 1.0F - (float) Math.exp(-dt * 4.0F);
		for (int side = 0; side < 4; side++) {
			int color = edges[side];
			for (int channel = 0; channel < 3; channel++) {
				float target = ((color >> (16 - channel * 8)) & 0xFF) / 255.0F;
				this.glow[side * 3 + channel] += (target - this.glow[side * 3 + channel]) * follow;
			}
		}
		float strength = 0.62F * cinema;
		float bandW = w * 0.08F;
		float bandH = h * 0.10F;
		c.shadow(x - bandW, y + h * 0.06F, bandW * 2, h * 0.88F, bandW, 170, this.glowColor(0, strength));
		c.shadow(x + w - bandW, y + h * 0.06F, bandW * 2, h * 0.88F, bandW, 170, this.glowColor(1, strength));
		c.shadow(x + w * 0.06F, y - bandH, w * 0.88F, bandH * 2, bandH, 140, this.glowColor(2, strength * 0.8F));
		c.shadow(x + w * 0.06F, y + h - bandH, w * 0.88F, bandH * 2, bandH, 140, this.glowColor(3, strength * 0.8F));
	}

	/** A glow colour, brightened so that dark scenes still give a little light. */
	private int glowColor(int side, float alpha) {
		float r = this.glow[side * 3];
		float g = this.glow[side * 3 + 1];
		float b = this.glow[side * 3 + 2];
		float max = Math.max(0.08F, Math.max(r, Math.max(g, b)));
		float lift = Math.min(1.6F, 0.75F / max);
		int ri = Math.min(255, Math.round(r * lift * 255.0F));
		int gi = Math.min(255, Math.round(g * lift * 255.0F));
		int bi = Math.min(255, Math.round(b * lift * 255.0F));
		return Theme.alpha(0xFF000000 | ri << 16 | gi << 8 | bi, alpha);
	}

	/** For a few seconds after a video went fullscreen: how to get out again. */
	private void exitHint(UiCanvas c, FontAtlas f, float x, float y, float w, float cinema) {
		float show = cinema * Ease.smooth(Math.min(1.0F, this.cinemaTime / 0.35F)) * (1.0F - Ease.smooth((this.cinemaTime - HINT_SECONDS) / 0.5F));
		if (show < 0.01F) {
			return;
		}
		float tw = f.medium.width("to exit fullscreen", 15) + 70;
		float tx = x + (w - tw) * 0.5F;
		float ty = y + 26 - (1.0F - show) * 8.0F;
		c.pushAlpha(show);
		c.shadow(tx, ty + 3, tw, 40, 20, 14, 0x77000000);
		c.roundRect(tx, ty, tw, 40, 20, 0xE6141517);
		c.roundRect(tx + 10, ty + 9, 34, 22, 6, 0x33FFFFFF);
		c.textCentered(f.semibold, "Esc", tx + 27, ty + 20, 11.5F, Theme.TEXT);
		c.textMiddle(f.medium, "to exit fullscreen", tx + 52, ty + 20, 15, Theme.TEXT);
		c.popAlpha();
	}

	/** Pointer and wheel go to the page; a click on a text box starts typing on the page. */
	private void pageInput(UiCanvas c, UiInput in, FontAtlas f, BrowserSession s, VisorController controller, float x, float y, float w, float h) {
		boolean over = in.inside(x, y, w, h);
		float cssX = Math.max(0.0F, Math.min(s.cssWidth() - 1.0F, (in.x - x) / w * s.cssWidth()));
		float cssY = Math.max(0.0F, Math.min(s.cssHeight() - 1.0F, (in.y - y) / h * s.cssHeight()));
		if (over) {
			in.hover("browser.page", x, y, w, h);
		}
		if (over || this.pageDown) {
			s.mouseMove(cssX, cssY);
		}
		if (over && in.pressed) {
			this.pageDown = true;
			s.mouseButton(true, cssX, cssY);
		}
		if (this.pageDown && (in.released || !controller.mouseDown())) {
			this.pageDown = false;
			s.mouseButton(false, cssX, cssY);
		}
		if (over && in.scroll != 0.0F) {
			s.wheel(cssX, cssY, -in.scroll * 100.0F);
		}
		if (in.pressed && !over && controller.isPageTyping()) {
			controller.stopPageTyping(false);
		}
		if (s.pollFocusChange()) {
			if (s.editableFocused()) {
				controller.startPageTyping();
			} else {
				controller.stopPageTyping(false);
			}
		}

		float typing = this.anim.get("browser.typing", controller.isPageTyping(), 14.0F);
		if (typing > 0.01F) {
			String label = "Typing on the page  ·  Esc to stop";
			float tw = f.medium.width(label, 13) + 58;
			float tx = x + (w - tw) * 0.5F;
			float ty = y + h - 54 + (1.0F - typing) * 10.0F;
			c.pushAlpha(typing);
			c.shadow(tx, ty + 3, tw, 36, 18, 14, 0x77000000);
			c.roundRect(tx, ty, tw, 36, 18, 0xF2202226);
			c.roundRectStroke(tx, ty, tw, 36, 18, 1.5F, Theme.alpha(Theme.accentBright(), 0.9F));
			c.icon("keyboard", true, tx + 25, ty + 18, 19, Theme.accentBright());
			c.textMiddle(f.medium, label, tx + 44, ty + 18, 13, Theme.TEXT);
			c.popAlpha();
		}
	}

	private void releasePointer(BrowserSession s) {
		if (this.pageDown) {
			this.pageDown = false;
			s.mouseButton(false, 0, 0);
		}
	}

	private void startPage(UiCanvas c, UiInput in, FontAtlas f, float x, float y, float w, float h, float time) {
		c.roundRectGradient(x, y, w, h, CONTENT_RADIUS, 0xFF24272C, 0xFF16171A);
		c.pushClip(x, y, w, h);
		c.shadow(x + w * 0.5F - 320, y - 150, 640, 170, 85, 170, Theme.alpha(Theme.accentBright(), 0.20F));
		c.popClip();
		float cx = x + w * 0.5F;
		c.circle(cx, y + 112, 30, Theme.alpha(Theme.accentBright(), 0.18F));
		c.icon("globe-simple", false, cx, y + 112, 34, Theme.mix(Theme.accentBright(), Theme.TEXT, 0.35F));
		c.textCentered(f.semibold, "Where do you want to go?", cx, y + 176, 28, Theme.TEXT);
		float sw = 640;
		float sh = 54;
		Widgets.searchPill(c, in, this.anim, f, "browser.search", this.search, cx - sw * 0.5F, y + 210, sw, sh,
			BrowserShortcuts.searchPlaceholder(), "magnifying-glass", 16);

		List<BrowserShortcuts.Shortcut> shortcuts = BrowserShortcuts.all();
		float cellW = 132;
		float rowH = 118;
		for (int i = 0; i < shortcuts.size(); i++) {
			int column = i % 4;
			int row = i / 4;
			int inRow = Math.min(4, shortcuts.size() - row * 4);
			float tx = cx - inRow * cellW * 0.5F + column * cellW + cellW * 0.5F;
			float ty = y + 352 + row * rowH;
			if (Widgets.shortcutTile(c, in, this.anim, f, "browser.shortcut." + i, shortcuts.get(i), tx, ty, 66, 14)) {
				this.go(shortcuts.get(i).url());
			}
		}
		c.textCentered(f.regular, "Right click for browser settings  ·  Click a text box on a page to type, Esc to stop",
			cx, y + h - 26, 12.5F, Theme.TEXT_MUTED);
	}

	private void go(String text) {
		String url = BrowserShortcuts.resolve(text);
		if (url == null) {
			return;
		}
		VisorController controller = VisorController.get();
		controller.unfocusText(this.address);
		controller.unfocusText(this.search);
		this.search.clear();
		this.startPage = false;
		this.openedVersion = BrowserTexture.get().pageVersion();
		BrowserSession.get().open(url);
		VisorSounds.play(VisorSounds.TAB, 0.6F);
	}

	private void spinner(UiCanvas c, FontAtlas f, float cx, float cy, float time, String label) {
		float start = (time * 5.5F) % 6.2831855F;
		c.arc(cx, cy - 18, 18, 3.5F, start, 4.2F, Theme.accentBright());
		c.textCentered(f.medium, label, cx, cy + 28, 15, Theme.TEXT_SECONDARY);
	}

	private boolean message(UiCanvas c, UiInput in, FontAtlas f, float x, float y, float w, float h, String title, String body, @Nullable String button) {
		float cx = x + w * 0.5F;
		float cy = y + h * 0.5F;
		Widgets.iconTile(c, cx - 30, cy - 124, 60, "warning-circle", 0xFFF59E0B, 1.0F);
		c.textCentered(f.semibold, title, cx, cy - 28, 22, Theme.TEXT);
		c.textCentered(f.regular, f.regular.ellipsize(body, 14, w - 140), cx, cy + 4, 14, Theme.TEXT_SECONDARY);
		if (button == null) {
			return false;
		}
		float bw = 124;
		float bh = 38;
		float bx = cx - bw * 0.5F;
		float by = cy + 36;
		boolean hovered = in.hover("browser.retry", bx, by, bw, bh);
		float hover = this.anim.get("browser.retry.h", hovered, 18.0F);
		c.roundRect(bx, by, bw, bh, bh * 0.5F, Theme.mix(Theme.accent(), Theme.accentBright(), hover));
		c.textCentered(f.semibold, button, cx, by + bh * 0.5F, 14, Theme.TEXT);
		boolean clicked = in.clicked("browser.retry");
		if (clicked) {
			VisorSounds.play(VisorSounds.CLICK, 0.7F);
		}
		return clicked;
	}
}
