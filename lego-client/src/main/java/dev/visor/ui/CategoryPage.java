package dev.visor.ui;

import dev.visor.module.Category;
import dev.visor.module.Module;
import dev.visor.module.ModuleManager;
import dev.visor.render.UiCanvas;
import dev.visor.render.text.FontAtlas;
import dev.visor.sound.VisorSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.Nullable;

/**
 * A category (or search results): compact header, filter chips and a two column grid of module
 * cards. Left click toggles a module, right click (or its gear) opens the module's settings.
 */
public final class CategoryPage {
	public enum Filter { ALL, ENABLED, DISABLED }

	private static final float CARD_H = 94;
	private static final float GAP = 12;

	private final UiAnimations anim;
	private final MainPanel panel;
	private Filter filter = Filter.ALL;
	private float scroll;
	private float scrollTarget;

	CategoryPage(MainPanel panel, UiAnimations anim) {
		this.panel = panel;
		this.anim = anim;
	}

	public void reset() {
		this.filter = Filter.ALL;
		this.scroll = 0;
		this.scrollTarget = 0;
	}

	/**
	 * @param category the category shown, or null for search results across all modules
	 */
	public void render(UiCanvas c, UiInput in, FontAtlas f, @Nullable Category category, String query, float x0, float x1, float time) {
		List<Module> source = category != null ? ModuleManager.of(category) : ModuleManager.all();
		List<Module> matching = new ArrayList<>();
		String q = query.trim().toLowerCase(Locale.ROOT);
		for (Module module : source) {
			if (q.isEmpty() || module.name().toLowerCase(Locale.ROOT).contains(q) || module.description().toLowerCase(Locale.ROOT).contains(q)) {
				matching.add(module);
			}
		}
		int enabled = 0;
		for (Module module : matching) {
			if (module.enabled()) {
				enabled++;
			}
		}

		// Header: icon tile, title, counts.
		String title = category != null ? category.displayName() : "Search";
		String icon = category != null ? category.icon() : "magnifying-glass";
		int tint = category != null ? category.color() : Theme.accentBright();
		Widgets.iconTile(c, x0, 26, 52, icon, tint, 1.0F);
		c.textMiddle(f.semibold, title, x0 + 68, 44, 24, Theme.TEXT);
		String subtitle = category != null
			? matching.size() + " modules · " + enabled + " enabled"
			: matching.size() + (matching.size() == 1 ? " result" : " results") + " for “" + query.trim() + "”";
		c.textMiddle(f.regular, subtitle, x0 + 68, 68, 14, Theme.TEXT_SECONDARY);

		// Filter chips.
		float chipY = 100;
		float chipX = x0;
		String[] labels = {"All", "Enabled", "Disabled"};
		int[] counts = {matching.size(), enabled, matching.size() - enabled};
		for (int i = 0; i < labels.length; i++) {
			Filter value = Filter.values()[i];
			String count = Integer.toString(counts[i]);
			if (Widgets.chip(c, in, this.anim, f, "filter." + i, labels[i], count, chipX, chipY, this.filter == value)) {
				this.filter = value;
				this.scrollTarget = 0;
			}
			chipX += Widgets.chipWidth(f, labels[i], count) + 8;
		}
		if (category != null) {
			String hint = "Right click a module for its settings";
			c.icon("info", false, x1 - f.regular.width(hint, 12.5F) - 12, chipY + 16, 15, Theme.TEXT_MUTED);
			c.textRight(f.regular, hint, x1, chipY + 16, 12.5F, Theme.TEXT_MUTED);
		}

		List<Module> visible = new ArrayList<>();
		for (Module module : matching) {
			if (this.filter == Filter.ALL || (this.filter == Filter.ENABLED) == module.enabled()) {
				visible.add(module);
			}
		}

		// Grid.
		float top = 146;
		float bottom = MainPanel.HEIGHT - 14;
		float cardW = (x1 - x0 - GAP) * 0.5F;
		int rows = (visible.size() + 1) / 2;
		float contentH = rows * (CARD_H + GAP) - GAP;
		float maxScroll = Math.max(0, contentH - (bottom - top) + 6);
		if (in.inside(x0, top, x1 - x0, bottom - top) && in.scroll != 0) {
			this.scrollTarget = Math.max(0, Math.min(maxScroll, this.scrollTarget - in.scroll * 80));
		}
		this.scrollTarget = Math.min(this.scrollTarget, maxScroll);
		this.scroll = this.anim.get("grid.scroll", this.scrollTarget, 15.0F);

		c.pushClip(x0 - 8, top - 6, x1 - x0 + 16, bottom - top + 12);
		for (int i = 0; i < visible.size(); i++) {
			float x = x0 + (i % 2) * (cardW + GAP);
			float y = top + (i / 2) * (CARD_H + GAP) - this.scroll;
			if (y > bottom || y + CARD_H < top - 6) {
				continue;
			}
			boolean inView = y + CARD_H > top && y < bottom;
			UiInput cardInput = inView && in.inside(x0 - 8, top, x1 - x0 + 16, bottom - top) ? in : UiInput.INERT;
			this.card(c, cardInput, f, visible.get(i), x, y, cardW, time);
		}
		if (visible.isEmpty()) {
			String empty = this.filter == Filter.ENABLED ? "No modules enabled yet" : (this.filter == Filter.DISABLED ? "Everything is enabled" : "Nothing found");
			c.icon(this.filter == Filter.ENABLED ? "toggle-left" : "magnifying-glass", false, (x0 + x1) * 0.5F, top + 80, 40, Theme.TEXT_MUTED);
			c.textCentered(f.medium, empty, (x0 + x1) * 0.5F, top + 126, 16, Theme.TEXT_SECONDARY);
		}
		c.popClip();
		if (this.scroll > 1) {
			c.roundRectGradient(x0 - 8, top - 6, x1 - x0 + 16, 22, 0, Theme.CONTENT, Theme.alpha(Theme.CONTENT, 0));
		}
		if (this.scroll < maxScroll - 1) {
			c.roundRectGradient(x0 - 8, bottom - 16, x1 - x0 + 16, 22, 0, Theme.alpha(Theme.CONTENT, 0), Theme.CONTENT);
		}
	}

	private void card(UiCanvas c, UiInput in, FontAtlas f, Module module, float x, float y, float w, float time) {
		String id = "card." + module.id();
		boolean hovered = in.hover(id, x, y, w, CARD_H);
		float hover = this.anim.get(id + ".h", hovered, 16.0F);
		float press = this.anim.get(id + ".p", in.held(id), 30.0F);
		float on = this.anim.get(id + ".on", module.enabled(), 14.0F);
		int tint = module.category().color();

		c.push();
		c.scale(1.0F + hover * 0.012F - press * 0.02F, x + w * 0.5F, y + CARD_H * 0.5F);
		c.shadow(x, y + 5, w, CARD_H, 14, 8 + hover * 10, Theme.alpha(0xFF000000, 0.18F + hover * 0.18F));
		c.roundRect(x, y, w, CARD_H, 14, Theme.mix(Theme.CARD, Theme.CARD_HOVER, hover * 0.8F));
		// Enabled modules get a faint wash of their category colour.
		if (on > 0.01F) {
			c.roundRectGradientH(x, y, w, CARD_H, 14, Theme.alpha(tint, 0.16F * on), Theme.alpha(tint, 0.0F));
		}
		if (hover > 0.01F) {
			c.roundRectStroke(x, y, w, CARD_H, 14, 1.5F, Theme.alpha(0x30FFFFFF, hover));
		}

		Widgets.iconTile(c, x + 16, y + 21, 52, module.icon(), tint, 0.25F + 0.75F * on);
		float textX = x + 84;
		float textW = w - 84 - 70;
		c.textFit(f.medium, module.name(), textX, y + 36, 16, textW, Theme.TEXT);
		c.textFit(f.regular, module.description(), textX, y + 59, 12.5F, textW, Theme.TEXT_SECONDARY);

		// Right side: switch, keybind chip and settings gear.
		float sx = x + w - Widgets.SWITCH_W - 16;
		float sy = y + 20;
		if (Widgets.toggle(c, in, this.anim, id + ".switch", sx, sy, module.enabled())) {
			module.toggle();
		}
		float gearX = x + w - 30;
		float gearY = y + CARD_H - 26;
		if (Widgets.iconButton(c, in, this.anim, id + ".gear", "sliders-horizontal", gearX, gearY, 13, 0x00FFFFFF)) {
			this.panel.openSettings(module);
		}
		if (module.keybind().bound()) {
			String key = module.keybind().keyName();
			float kw = f.semibold.width(key, 11) + 12;
			c.roundRect(gearX - 22 - kw, gearY - 9, kw, 18, 5, 0x1FFFFFFF);
			c.textCentered(f.semibold, key, gearX - 22 - kw * 0.5F, gearY, 11, Theme.TEXT_SECONDARY);
		}
		c.pop();

		// Clicking the card body toggles; right click opens settings.
		if (in.clicked(id)) {
			module.toggle();
			VisorSounds.play(module.enabled() ? VisorSounds.TOGGLE_ON : VisorSounds.TOGGLE_OFF, 0.8F);
		}
		if (in.secondaryClicked(x, y, w, CARD_H)) {
			this.panel.openSettings(module);
		}
	}
}
