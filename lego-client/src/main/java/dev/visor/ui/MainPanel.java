package dev.visor.ui;

import dev.visor.VisorController;
import dev.visor.module.Category;
import dev.visor.module.Module;
import dev.visor.module.ModuleManager;
import dev.visor.render.UiCanvas;
import dev.visor.render.UiResources;
import dev.visor.render.text.FontAtlas;
import dev.visor.sound.VisorSounds;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/**
 * The main curved panel: navigation on the left, the current page in the middle (home, a
 * category, or search results), friends on the right, and the settings sheet on top.
 */
public final class MainPanel {
	public static final float WIDTH = 1240.0F;
	public static final float HEIGHT = 580.0F;
	static final float SIDEBAR_W = 250.0F;
	static final float RIGHT_X = 930.0F;
	private static final int HOME = -1;

	public String clientName = "Visor";

	private final UiAnimations anim = new UiAnimations();
	private final HomePage home = new HomePage(this, this.anim);
	private final CategoryPage categoryPage = new CategoryPage(this, this.anim);
	private final SettingsSheet sheet = new SettingsSheet();
	private final TextField search = new TextField(32);
	private final Spring pageReveal = new Spring(1.0F, 11.0F);
	private final List<Friend> friends = List.of(
		new Friend("Emily", "Hypixel · Lobby", true, "map-pin"),
		new Friend("Michelle", "SkyBlock · Private Island", true, "phone-plus"),
		new Friend("Terris", "Singleplayer · Survival", true, "phone-plus"),
		new Friend("Rebecca", "Offline", false, "chats-teardrop")
	);
	private int page = HOME;
	private float dt;

	private record Friend(String name, String status, boolean online, String action) {
		Identifier skin() {
			return Avatars.defaultSkin(this.name);
		}
	}

	// ------------------------------------------------------------------------------------------
	// Navigation API (also used by the dock and the controller)

	public void showHome() {
		this.navigate(HOME);
	}

	public void showCategory(Category category) {
		this.navigate(category.ordinal());
	}

	private void navigate(int target) {
		boolean searching = !this.search.isEmpty();
		if (target == this.page && !searching) {
			return;
		}
		this.page = target;
		this.search.clear();
		VisorController.get().unfocusText(this.search);
		VisorController.get().unfocusText(this.home.webSearch());
		this.categoryPage.reset();
		this.pageReveal.snap(0.0F);
		this.pageReveal.target(1.0F);
		VisorSounds.play(VisorSounds.TAB, 0.7F);
	}

	public void openSettings(Module module) {
		this.sheet.show(SettingsSheet.Content.of(module));
	}

	public void openHeadsetSettings() {
		this.sheet.show(SettingsSheet.Content.headset());
	}

	public boolean sheetOpen() {
		return this.sheet.isOpen();
	}

	public void closeSheet() {
		this.sheet.close();
	}

	public TextField searchField() {
		return this.search;
	}

	// ------------------------------------------------------------------------------------------

	public void render(UiCanvas c, UiInput in, float dt, float time) {
		FontAtlas f = UiResources.atlas();
		this.anim.beginFrame(dt);
		this.dt = dt;
		UiInput pageInput = this.sheet.blocksInput() ? UiInput.INERT : in;

		c.shadow(0, 8, WIDTH, HEIGHT, Theme.PANEL_RADIUS, 34, 0x66000000);
		c.roundRect(0, 0, WIDTH, HEIGHT, Theme.PANEL_RADIUS, Theme.SIDEBAR);
		c.rect(SIDEBAR_W, 0, RIGHT_X - SIDEBAR_W, HEIGHT, Theme.CONTENT);
		c.roundRectStroke(0, 0, WIDTH, HEIGHT, Theme.PANEL_RADIUS, 1.0F, 0x14FFFFFF);

		this.sidebar(c, pageInput, f);
		this.content(c, pageInput, f, time);
		this.friendsColumn(c, pageInput, f);

		// Clicking anywhere outside the search field ends typing.
		if (pageInput.pressed && VisorController.get().isFocused(this.search) && !"search".equals(pageInput.hovered())) {
			VisorController.get().unfocusText(this.search);
		}
		if (pageInput.pressed && VisorController.get().isFocused(this.home.webSearch()) && !"home.web".equals(pageInput.hovered())) {
			VisorController.get().unfocusText(this.home.webSearch());
		}
		// Right click on empty space opens the headset settings.
		if (!this.sheet.blocksInput() && in.unhandledSecondary()) {
			this.openHeadsetSettings();
		}
		this.sheet.render(c, in, dt, time, WIDTH, HEIGHT);
	}

	// ------------------------------------------------------------------------------------------

	private void sidebar(UiCanvas c, UiInput in, FontAtlas f) {
		c.icon("virtual-reality", true, 44, 52, 30, Theme.accentBright());
		c.textMiddle(f.bold, this.clientName, 68, 52, 26, Theme.TEXT);

		float itemX = 14;
		float itemW = SIDEBAR_W - 28;
		float startY = 96;
		// Rows share the room above the settings entry (LEGO and Visor categories: 13 rows).
		int rows = Category.values().length + 1;
		float room = HEIGHT - 62 - 16 - startY;
		float gap = rows > 9 ? 3 : 6;
		float itemH = Math.max(24, Math.min(42, room / rows - gap));
		float textSize = itemH < 34 ? 13 : 16;
		float iconSize = itemH < 34 ? 16 : 20;
		int selectedRow = this.search.isEmpty() ? this.page + 1 : -10;
		float targetY = startY + Math.max(0, selectedRow) * (itemH + gap);
		float highlightY = this.anim.get("nav.highlight", targetY, 16.0F);
		float highlightA = this.anim.get("nav.highlight.a", selectedRow >= 0, 14.0F);
		if (highlightA > 0.01F) {
			c.roundRect(itemX, highlightY, itemW, itemH, 7, Theme.alpha(Theme.accent(), highlightA));
		}

		Category[] categories = Category.values();
		for (int row = 0; row <= categories.length; row++) {
			Category category = row == 0 ? null : categories[row - 1];
			String name = category == null ? "Home" : category.displayName();
			String icon = category == null ? "house" : category.icon();
			float y = startY + row * (itemH + gap);
			String id = "nav." + row;
			boolean hovered = in.hover(id, itemX, y, itemW, itemH);
			boolean active = row == selectedRow;
			float hover = this.anim.get(id + ".h", hovered && !active, 18.0F);
			if (hover > 0.01F) {
				c.roundRect(itemX, y, itemW, itemH, 7, Theme.alpha(0xFF2C2E31, hover));
			}
			float press = this.anim.get(id + ".p", in.held(id), 30.0F);
			c.push();
			c.scale(1.0F - press * 0.03F, itemX + itemW * 0.5F, y + itemH * 0.5F);
			float on = this.anim.get(id + ".on", active, 14.0F);
			int color = Theme.mix(0xFFC4C7CC, Theme.TEXT, Math.max(on, hover * 0.6F));
			c.icon(icon, active, itemX + 24, y + itemH * 0.5F, iconSize, color);
			c.textMiddle(active ? f.semibold : f.medium, name, itemX + 46, y + itemH * 0.5F, textSize, color);
			if (category != null) {
				int count = ModuleManager.enabledCount(category);
				if (count > 0) {
					String badge = Integer.toString(count);
					float bw = f.semibold.width(badge, 12) + 14;
					float by = y + itemH * 0.5F;
					c.roundRect(itemX + itemW - bw - 10, by - 9, bw, 18, 9, Theme.alpha(Theme.TEXT, 0.12F + on * 0.08F));
					c.textCentered(f.semibold, badge, itemX + itemW - bw * 0.5F - 10, by, 12, color);
				}
			}
			c.pop();
			if (in.clicked(id)) {
				this.navigate(category == null ? HOME : category.ordinal());
			}
		}

		float sy = HEIGHT - 62;
		c.rect(24, sy - 12, SIDEBAR_W - 48, 1, Theme.DIVIDER);
		boolean settingsHover = in.hover("nav.settings", itemX, sy, itemW, itemH);
		float sh = this.anim.get("nav.settings.h", settingsHover, 18.0F);
		if (sh > 0.01F) {
			c.roundRect(itemX, sy, itemW, itemH, 7, Theme.alpha(0xFF2C2E31, sh));
		}
		int settingsColor = Theme.mix(0xFFC4C7CC, Theme.TEXT, sh);
		c.icon("gear-six", false, itemX + 24, sy + itemH * 0.5F, 20, settingsColor);
		c.textMiddle(f.medium, "Settings", itemX + 46, sy + itemH * 0.5F, 16, settingsColor);
		if (in.clicked("nav.settings")) {
			this.openHeadsetSettings();
		}
	}

	private void content(UiCanvas c, UiInput in, FontAtlas f, float time) {
		float x0 = SIDEBAR_W + 20;
		float x1 = RIGHT_X - 20;
		boolean searching = !this.search.isEmpty();
		float reveal = this.pageReveal.update(this.dt);

		this.searchBox(c, in, f, x1 - 254, 26, 216, 36, time);
		if (Widgets.iconButton(c, in, this.anim, "header.customize", "sliders-horizontal", x1 - 16, 44, 17, 0xFF33363A)) {
			this.openHeadsetSettings();
		}

		c.pushAlpha(reveal);
		c.push();
		c.translate((1.0F - reveal) * 24.0F, 0);
		if (searching) {
			this.categoryPage.render(c, in, f, null, this.search.text(), x0, x1, time);
		} else if (this.page == HOME) {
			Avatars.draw(c, Avatars.localSkin(), x0 + 19, 44, 38);
			float end = c.textMiddle(f.regular, "Welcome back, ", x0 + 50, 44, 16, Theme.TEXT);
			end = c.textMiddle(f.semibold, playerName(), end, 44, 16, Theme.TEXT);
			c.textMiddle(f.regular, ".", end, 44, 16, Theme.TEXT);
			this.home.render(c, in, f, x0, x1, time);
		} else {
			this.categoryPage.render(c, in, f, Category.values()[this.page], "", x0, x1, time);
		}
		c.pop();
		c.popAlpha();
	}

	private void searchBox(UiCanvas c, UiInput in, FontAtlas f, float x, float y, float w, float h, float time) {
		VisorController controller = VisorController.get();
		boolean focused = controller.isFocused(this.search);
		boolean hovered = in.hover("search", x, y, w, h);
		float hover = this.anim.get("search.h", hovered, 18.0F);
		float focus = this.anim.get("search.f", focused, 16.0F);
		c.roundRect(x, y, w, h, h * 0.5F, Theme.mix(Theme.mix(0xFF2E3033, 0xFF383B3E, hover), 0xFF3A3D41, focus));
		if (focus > 0.01F) {
			c.roundRectStroke(x, y, w, h, h * 0.5F, 1.5F, Theme.alpha(Theme.accentBright(), focus));
		}
		c.icon("magnifying-glass", false, x + 20, y + h * 0.5F, 17, Theme.mix(Theme.TEXT_SECONDARY, Theme.TEXT, Math.max(hover, focus)));
		float textX = x + 36;
		if (this.search.isEmpty()) {
			c.textMiddle(f.regular, focused ? "Type to search" : "Search modules", textX, y + h * 0.5F, 14, Theme.TEXT_MUTED);
			if (focused && this.search.caretVisible()) {
				c.rect(textX - 2, y + 10, 1.5F, h - 20, Theme.accentBright());
			}
		} else {
			String text = f.regular.ellipsize(this.search.text(), 14, w - 70);
			float end = c.textMiddle(f.regular, text, textX, y + h * 0.5F, 14, Theme.TEXT);
			if (focused && this.search.caretVisible()) {
				c.rect(end + 1, y + 10, 1.5F, h - 20, Theme.accentBright());
			}
			if (Widgets.iconButton(c, in, this.anim, "search.clear", "x", x + w - 18, y + h * 0.5F, 10, 0x00FFFFFF)) {
				this.search.clear();
				this.categoryPage.reset();
				VisorSounds.play(VisorSounds.CLICK, 0.6F);
			}
		}
		if (in.clicked("search") && !focused) {
			controller.focusText(this.search);
			VisorSounds.play(VisorSounds.CLICK, 0.6F);
		}
	}

	private void friendsColumn(UiCanvas c, UiInput in, FontAtlas f) {
		float x0 = RIGHT_X + 16;
		float x1 = WIDTH - 16;
		float titleEnd = c.textMiddle(f.semibold, "Friend List", x0 + 4, 52, 19, Theme.TEXT);
		if (Widgets.iconButton(c, in, this.anim, "friends.add", "user-plus", titleEnd + 22, 52, 14, 0xFF2C2E31)) {
			VisorSounds.play(VisorSounds.CLICK, 0.7F);
		}
		float seeAllW = f.medium.width("See All", 15);
		boolean seeAll = in.hover("friends.all", x1 - seeAllW - 8, 38, seeAllW + 12, 28);
		c.textRight(f.medium, "See All", x1 - 4, 50, 15, seeAll ? Theme.accentBright() : Theme.TEXT);
		if (in.clicked("friends.all")) {
			VisorSounds.play(VisorSounds.CLICK, 0.7F);
		}

		float y = 78;
		float h = 62;
		for (int i = 0; i < this.friends.size(); i++) {
			Friend friend = this.friends.get(i);
			String id = "friend." + i;
			boolean hovered = in.hover(id, x0, y, x1 - x0, h);
			float hover = this.anim.get(id + ".h", hovered, 16.0F);
			float press = this.anim.get(id + ".p", in.held(id), 30.0F);
			c.push();
			c.scale(1.0F + hover * 0.015F - press * 0.025F, (x0 + x1) * 0.5F, y + h * 0.5F);
			c.roundRect(x0, y, x1 - x0, h, Theme.CARD_RADIUS, Theme.mix(Theme.CARD, Theme.CARD_HOVER, hover));
			if (hover > 0.01F) {
				c.roundRectStroke(x0, y, x1 - x0, h, Theme.CARD_RADIUS, 1.5F, Theme.alpha(0x33FFFFFF, hover));
			}
			float cy = y + h * 0.5F;
			Avatars.draw(c, friend.skin(), x0 + 30, cy, 40);
			if (friend.online()) {
				c.circle(x0 + 44, cy + 14, 7, Theme.mix(Theme.CARD, Theme.CARD_HOVER, hover));
				c.circle(x0 + 44, cy + 14, 5, Theme.ONLINE);
			}
			c.textFit(f.medium, friend.name(), x0 + 60, cy - 9, 16, x1 - x0 - 110, Theme.TEXT);
			c.textFit(f.regular, friend.status(), x0 + 60, cy + 12, 13, x1 - x0 - 110, Theme.TEXT_SECONDARY);
			if (Widgets.iconButton(c, in, this.anim, id + ".action", friend.action(), x1 - 28, cy, 18, 0x00FFFFFF)) {
				VisorSounds.play(VisorSounds.CLICK, 0.7F);
			}
			c.pop();
			y += h + 8;
		}

		float sy = y + 30;
		c.textMiddle(f.semibold, "Scoreboards", x0 + 4, sy, 19, Theme.TEXT);
		float cardY = sy + 20;
		float cardH = HEIGHT - cardY - 16;
		boolean hovered = in.hover("session", x0, cardY, x1 - x0, cardH);
		float hover = this.anim.get("session.h", hovered, 16.0F);
		c.roundRect(x0, cardY, x1 - x0, cardH, Theme.CARD_RADIUS, Theme.mix(Theme.CARD, Theme.CARD_HOVER, hover));
		float cy = cardY + cardH * 0.5F;
		Avatars.draw(c, Avatars.localSkin(), x0 + 30, cy, 40);
		c.circle(x0 + 44, cy + 14, 7, Theme.mix(Theme.CARD, Theme.CARD_HOVER, hover));
		c.circle(x0 + 44, cy + 14, 5, Theme.ONLINE);
		c.textMiddle(f.regular, "Modules enabled", x0 + 60, cy - 9, 15, Theme.TEXT_SECONDARY);
		c.textMiddle(f.semibold, ModuleManager.enabledCount() + " / " + ModuleManager.all().size(), x0 + 60, cy + 12, 16, Theme.TEXT);
	}

	private static String playerName() {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.player != null ? minecraft.player.getGameProfile().name() : minecraft.getUser().getName();
	}
}
