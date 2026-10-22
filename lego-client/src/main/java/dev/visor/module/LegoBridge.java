package dev.visor.module;

import dev.lego.core.Modules;
import dev.visor.setting.BoolSetting;
import dev.visor.setting.ColorSetting;
import dev.visor.setting.ModeSetting;
import dev.visor.setting.Setting;
import dev.visor.setting.SliderSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shows the LEGO client's real modules in the Visor menu. Each LEGO module becomes a Visor
 * {@link Module} whose on/off state, keybind and settings read from and write to the LEGO module
 * (which keeps saving them as before), so there is one set of modules, not two.
 */
final class LegoBridge {
	private LegoBridge() {
	}

	/** Phosphor icon for a LEGO icon name (the menu's icon font has no LEGO icons). */
	private static final Map<String, String> ICONS = Map.ofEntries(
		Map.entry("layout", "squares-four"), Map.entry("sparkle", "sparkle"), Map.entry("wand", "magic-wand"),
		Map.entry("boost", "rocket-launch"), Map.entry("music", "music-notes"), Map.entry("armor", "shield"),
		Map.entry("shield", "shield"), Map.entry("totem", "medal"), Map.entry("vignette", "circle-half"),
		Map.entry("tilt", "arrows-clockwise"), Map.entry("target", "target"), Map.entry("skull", "skull"),
		Map.entry("rain", "cloud"), Map.entry("cloud", "cloud"), Map.entry("potion", "drop"), Map.entry("heart", "heart"),
		Map.entry("flame", "fire"), Map.entry("filter", "faders"), Map.entry("zoom", "binoculars"), Map.entry("wing", "feather"),
		Map.entry("waypoint", "flag"), Map.entry("user", "user"), Map.entry("trail", "footprints"), Map.entry("tag", "tag"),
		Map.entry("sword", "sword"), Map.entry("sun", "sun"), Map.entry("sprint", "person-simple-run"), Map.entry("speed", "gauge"),
		Map.entry("gauge", "gauge"), Map.entry("sound", "speaker-high"), Map.entry("snow", "snowflake"), Map.entry("sneak", "sneaker-move"),
		Map.entry("sky", "mountains"), Map.entry("ring", "circles-three"), Map.entry("respawn", "arrow-counter-clockwise"),
		Map.entry("pin", "push-pin"), Map.entry("particles", "sparkle"), Map.entry("palette", "palette"), Map.entry("number", "list"),
		Map.entry("mute", "speaker-slash"), Map.entry("moon", "moon"), Map.entry("memory", "cpu"), Map.entry("macro", "terminal-window"),
		Map.entry("list", "list"), Map.entry("kill", "skull"), Map.entry("keys", "keyboard"), Map.entry("jump", "arrows-out"),
		Map.entry("image", "image"), Map.entry("hitmarker", "crosshair-simple"), Map.entry("hit", "lightning"), Map.entry("gg", "trophy"),
		Map.entry("gem", "star"), Map.entry("freelook", "eye"), Map.entry("fps", "chart-line-up"), Map.entry("food", "fish"),
		Map.entry("firefly", "lightbulb"), Map.entry("entity", "users"), Map.entry("emote", "hand-palm"), Map.entry("crosshair", "crosshair"),
		Map.entry("copy", "clipboard-text"), Map.entry("compass", "compass"), Map.entry("clock", "clock"), Map.entry("cinema", "film-strip"),
		Map.entry("box", "cube"), Map.entry("bell", "bell"), Map.entry("aura", "sparkle"), Map.entry("arrow", "arrow-right"),
		Map.entry("biome", "tree"), Map.entry("light", "lightbulb"), Map.entry("chat", "chat-circle-dots"), Map.entry("server", "globe"),
		Map.entry("ping", "wifi-high"), Map.entry("wifi", "wifi-high"), Map.entry("visuals", "sparkle"), Map.entry("hud", "squares-four")
	);

	static List<Module> modules() {
		List<Module> out = new ArrayList<>();
		for (dev.lego.core.Module lego : Modules.ALL) {
			Category category = Category.of(lego.category);
			if (category != null && lego.isToggleable()) {
				out.add(new Bridged(lego, category));
			}
		}
		return out;
	}

	/** Re-reads every bridged module's state from the LEGO side (cheap; once per tick). */
	static void pull(List<Module> modules) {
		for (Module m : modules) {
			if (m instanceof Bridged b) {
				b.pull();
			}
		}
	}

	static final class Bridged extends Module {
		final dev.lego.core.Module lego;
		private final List<Runnable> pulls = new ArrayList<>();

		Bridged(dev.lego.core.Module lego, Category category) {
			super(lego.id, lego.name, lego.description, ICONS.getOrDefault(lego.icon == null ? "" : lego.icon, "puzzle-piece"), category);
			this.lego = lego;
			List<Setting<?>> settings = new ArrayList<>();
			for (dev.lego.core.Setting s : lego.settings) {
				Setting<?> v = this.convert(s);
				if (v != null) {
					settings.add(v);
				}
			}
			if (!settings.isEmpty()) {
				this.group("Einstellungen", settings.toArray(new Setting<?>[0]));
			}
			this.keybind().mirror(lego.key < 0 ? dev.visor.setting.KeySetting.NONE : lego.key);
			this.keybind().onChange(() -> {
				Integer k = this.keybind().get();
				lego.key = k == null ? -1 : k;
				Modules.scheduleSave();
			});
			this.pulls.add(() -> this.keybind().mirror(lego.key < 0 ? dev.visor.setting.KeySetting.NONE : lego.key));
		}

		private Setting<?> convert(dev.lego.core.Setting s) {
			String id = s.name.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
			if (s instanceof dev.lego.core.Setting.Bool b) {
				BoolSetting v = new BoolSetting(id, s.name, "", b.value);
				v.onChange(() -> { b.value = v.get(); Modules.scheduleSave(); });
				this.pulls.add(() -> v.mirror(b.value));
				return v;
			}
			if (s instanceof dev.lego.core.Setting.Num n) {
				SliderSetting v = new SliderSetting(id, s.name, "", n.value, n.min, n.max, n.step, n.unit == null ? "" : n.unit);
				v.onChange(() -> { n.set(v.get()); Modules.scheduleSave(); });
				this.pulls.add(() -> v.mirror(n.value));
				return v;
			}
			if (s instanceof dev.lego.core.Setting.Mode m && m.options.length > 0) {
				ModeSetting v = new ModeSetting(id, s.name, "", m.get(), m.options);
				v.onChange(() -> { m.load(v.get()); Modules.scheduleSave(); });
				this.pulls.add(() -> v.mirror(m.get()));
				return v;
			}
			if (s instanceof dev.lego.core.Setting.Color c) {
				int[] palette = dev.lego.core.Setting.Color.PALETTE;
				ColorSetting v = new ColorSetting(id, s.name, "", palette[Math.max(0, Math.min(palette.length - 1, c.index))], palette);
				v.onChange(() -> {
					for (int i = 0; i < palette.length; i++) {
						if (palette[i] == v.get()) {
							c.index = i;
						}
					}
					Modules.scheduleSave();
				});
				this.pulls.add(() -> v.mirror(palette[Math.max(0, Math.min(palette.length - 1, c.index))]));
				return v;
			}
			return null; // actions and special settings stay in the LEGO screens
		}

		void pull() {
			for (Runnable r : this.pulls) {
				r.run();
			}
		}

		@Override
		public boolean enabled() {
			return this.lego.enabled;
		}

		@Override
		public void setEnabled(boolean enabled) {
			if (this.lego.enabled != enabled) {
				this.lego.setEnabled(enabled);
				Modules.scheduleSave();
			}
		}

		@Override
		public void toggle() {
			this.setEnabled(!this.lego.enabled);
		}
	}
}
