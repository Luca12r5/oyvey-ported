package dev.visor.ui;

import dev.visor.VisorController;
import dev.visor.browser.BrowserSession;
import dev.visor.module.ModuleManager;
import dev.visor.render.UiCanvas;
import dev.visor.render.UiResources;
import dev.visor.render.text.FontAtlas;
import dev.visor.setting.VisorSettings;
import dev.visor.sound.VisorSounds;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.minecraft.client.Minecraft;

/** The bar floating below the windows: status, profile, the apps and the system buttons. */
public final class Dock {
	public static final float WIDTH = 820.0F;
	public static final float HEIGHT = 96.0F;
	private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("h:mm", Locale.ROOT);
	private static final DateTimeFormatter AMPM = DateTimeFormatter.ofPattern("a", Locale.ENGLISH);

	private static final int MODULES = 0;
	private static final int BROWSER = 1;
	private static final int FRIENDS = 2;
	private static final int NOTIFICATIONS = 3;
	private static final int PIN = 4;
	private static final int RECENTER = 5;
	private static final int SETTINGS = 6;
	private static final String[] ICONS = {"dots-nine", "compass", "users-three", "bell", "push-pin", "arrows-clockwise", "gear-six"};

	private final UiAnimations anim = new UiAnimations();

	public void render(UiCanvas c, UiInput in, float dt, MainPanel panel) {
		FontAtlas f = UiResources.atlas();
		VisorController controller = VisorController.get();
		this.anim.beginFrame(dt);
		c.shadow(0, 6, WIDTH, HEIGHT, 20, 26, 0x73000000);
		c.roundRect(0, 0, WIDTH, HEIGHT, 20, Theme.DOCK);
		c.roundRectGradient(0, 0, WIDTH, HEIGHT * 0.5F, 20, 0x0FFFFFFF, 0x00FFFFFF);
		c.roundRectStroke(0, 0, WIDTH, HEIGHT, 20, 1.0F, 0x17FFFFFF);

		// Status block: clock, connectivity and live stats.
		LocalTime now = LocalTime.now();
		float clockEnd = c.textMiddle(f.medium, now.format(CLOCK), 30, 34, 17, Theme.TEXT);
		c.textMiddle(f.medium, now.format(AMPM).toUpperCase(Locale.ROOT), clockEnd + 4, 35, 10, Theme.TEXT_SECONDARY);
		c.icon("wifi-high", true, 158, 33, 18, Theme.TEXT);
		Minecraft minecraft = Minecraft.getInstance();
		c.icon("gauge", false, 38, 64, 15, Theme.TEXT_SECONDARY);
		c.textMiddle(f.regular, minecraft.getFps() + " fps", 50, 64, 13, Theme.TEXT_SECONDARY);
		c.icon("pulse", false, 118, 64, 15, Theme.TEXT_SECONDARY);
		c.textMiddle(f.regular, ping(minecraft) + " ms", 130, 64, 13, Theme.TEXT_SECONDARY);

		c.rect(206, 24, 1, HEIGHT - 48, Theme.DIVIDER);

		// Profile.
		float ax = 252;
		boolean profileHover = in.hover("dock.profile", ax - 30, 18, 60, 60);
		float ph = this.anim.get("dock.profile.h", profileHover, 18.0F);
		float pp = this.anim.get("dock.profile.p", in.held("dock.profile"), 30.0F);
		float size = 48 * (1.0F + ph * 0.06F - pp * 0.06F);
		if (ph > 0.01F) {
			c.circle(ax, HEIGHT * 0.5F, size * 0.5F + 5, Theme.alpha(Theme.accentBright(), ph * 0.9F));
		}
		Avatars.draw(c, Avatars.localSkin(), ax, HEIGHT * 0.5F, size);
		c.circle(ax + 17, HEIGHT * 0.5F + 17, 8, Theme.DOCK | 0xFF000000);
		c.circle(ax + 17, HEIGHT * 0.5F + 17, 6, Theme.ONLINE);
		if (in.clicked("dock.profile")) {
			controller.showHome();
			panel.showHome();
		}

		// Apps and system buttons.
		boolean browserApp = controller.app() == VisorController.App.BROWSER;
		boolean browserRunning = BrowserSession.get().running();
		boolean pinned = !VisorSettings.follows();
		float startX = 340;
		float spacing = (WIDTH - 40 - startX) / (ICONS.length - 1);
		for (int i = 0; i < ICONS.length; i++) {
			float bx = startX + i * spacing;
			float by = HEIGHT * 0.5F;
			String id = "dock." + i;
			boolean hovered = in.hover(id, bx - 30, by - 30, 60, 60);
			float hover = this.anim.get(id + ".h", hovered, 18.0F);
			float press = this.anim.get(id + ".p", in.held(id), 32.0F);
			boolean activeState = switch (i) {
				case MODULES -> !browserApp && !controller.sheetOpen();
				case BROWSER -> browserApp;
				case PIN -> pinned;
				case SETTINGS -> controller.sheetOpen();
				default -> false;
			};
			float on = this.anim.get(id + ".on", activeState, 14.0F);
			float radius = 25 * (0.85F + 0.15F * hover) * (1.0F - press * 0.08F);
			if (hover > 0.01F || on > 0.01F) {
				c.circle(bx, by, radius, Theme.alpha(Theme.DOCK_HOVER, Math.max(hover, on * 0.9F)));
			}
			int color = Theme.mix(0xFFB8BBC0, Theme.TEXT, Math.max(hover, on));
			if (i == PIN) {
				color = Theme.mix(color, Theme.accentBright(), on * 0.85F);
			}
			c.icon(ICONS[i], true, bx, by, 26 * (1.0F - press * 0.08F), color);
			if (on > 0.01F) {
				c.roundRect(bx - 8, HEIGHT - 12, 16, 4, 2, Theme.alpha(Theme.accentBright(), on));
			} else if (i == BROWSER && browserRunning) {
				// The browser keeps running in the background: a small dot like an open app.
				c.circle(bx, HEIGHT - 10, 2.5F, 0xCCFFFFFF);
			}
			if (i == NOTIFICATIONS) {
				c.circle(bx + 10, by - 11, 7, Theme.DOCK | 0xFF000000);
				c.circle(bx + 10, by - 11, 5, 0xFFFF4D5E);
			}
			if (hover > 0.01F) {
				String label = switch (i) {
					case MODULES -> "Modules · " + ModuleManager.enabledCount() + " on";
					case BROWSER -> browserApp ? "Browser · back to home" : "Browser";
					case FRIENDS -> "Friends";
					case NOTIFICATIONS -> "Notifications";
					case PIN -> pinned ? "Panels stay in place" : "Panels follow you";
					case RECENTER -> "Recenter";
					default -> "Settings";
				};
				float tw = f.medium.width(label, 13) + 24;
				float ty = -40 - (1.0F - hover) * 6;
				c.pushAlpha(hover);
				c.shadow(bx - tw * 0.5F, ty + 2, tw, 30, 15, 10, 0x55000000);
				c.roundRect(bx - tw * 0.5F, ty, tw, 30, 15, 0xF2232528);
				c.textCentered(f.medium, label, bx, ty + 15, 13, Theme.TEXT);
				c.popAlpha();
			}
			if (in.clicked(id)) {
				switch (i) {
					case MODULES -> {
						panel.closeSheet();
						controller.showHome();
						panel.showHome();
					}
					case BROWSER -> controller.toggleBrowser();
					case NOTIFICATIONS -> VisorSounds.play(VisorSounds.NOTIFY, 0.7F);
					case PIN -> {
						VisorSettings.PANEL_ANCHOR.set(pinned ? VisorSettings.FOLLOW_ME : VisorSettings.STAY);
						VisorSounds.play(pinned ? VisorSounds.TOGGLE_OFF : VisorSounds.TOGGLE_ON, 0.8F);
					}
					case RECENTER -> controller.recenter();
					case SETTINGS -> {
						controller.showHome();
						panel.openHeadsetSettings();
					}
					default -> VisorSounds.play(VisorSounds.CLICK, 0.7F);
				}
			}
		}
		if (in.unhandledSecondary()) {
			controller.showHome();
			panel.openHeadsetSettings();
		}
	}

	private static int ping(Minecraft minecraft) {
		if (minecraft.getConnection() == null || minecraft.player == null) {
			return 0;
		}
		var info = minecraft.getConnection().getPlayerInfo(minecraft.player.getUUID());
		return info == null ? 0 : info.getLatency();
	}
}
