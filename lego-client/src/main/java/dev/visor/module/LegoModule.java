package dev.visor.module;

import dev.lego.core.Setting;
import dev.visor.setting.BoolSetting;
import dev.visor.setting.ColorSetting;
import dev.visor.setting.ModeSetting;
import dev.visor.setting.SliderSetting;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * A LEGO module shown in the headset menu. It keeps no state of its own: switches and settings
 * write straight into the LEGO module (which saves them), and {@link #mirror()} shows the LEGO
 * values here again, so the click GUI and the headset always agree.
 */
public final class LegoModule extends Module {
	private final dev.lego.core.Module lego;
	private final List<Link> links = new ArrayList<>();
	private boolean mirroring;

	private LegoModule(dev.lego.core.Module lego, Category category) {
		super(lego.id, lego.displayName(), lego.displayDesc(), "squares-four", category);
		this.lego = lego;
		List<dev.visor.setting.Setting<?>> settings = new ArrayList<>();
		for (Setting s : lego.settings) {
			Link link = this.link(s);
			if (link != null) {
				this.links.add(link);
				settings.add(link.visor());
			}
		}
		if (!settings.isEmpty()) {
			this.group("Einstellungen", settings.toArray(new dev.visor.setting.Setting<?>[0]));
		}
		this.mirror();
	}

	/** The visor category of a listed LEGO category (same name), or null. */
	static @Nullable Category categoryOf(dev.lego.core.Category c) {
		try {
			return Category.valueOf(c.name());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/** Every module of the LEGO click GUI, for the headset menu. */
	public static List<Module> all() {
		List<Module> list = new ArrayList<>();
		for (dev.lego.core.Module m : dev.lego.core.Modules.ALL) {
			Category c = m.category.listed && m.category != dev.lego.core.Category.CUSTOM_TXT ? categoryOf(m.category) : null;
			if (c != null && !m.hidden) {
				list.add(new LegoModule(m, c));
			}
		}
		return list;
	}

	@Override
	protected void onEnable() {
		if (!this.mirroring) {
			if (this.lego.isToggleable()) {
				this.lego.setEnabled(true);
				dev.lego.core.Modules.scheduleSave();
			} else {
				this.mirror();
			}
		}
	}

	@Override
	protected void onDisable() {
		if (!this.mirroring && this.lego.isToggleable()) {
			this.lego.setEnabled(false);
			dev.lego.core.Modules.scheduleSave();
		}
	}

	/** Shows the LEGO module's current state without writing it back. */
	public void mirror() {
		this.mirroring = true;
		try {
			this.setEnabled(this.lego.isToggleable() && this.lego.enabled);
			for (Link link : this.links) {
				link.pullValue();
			}
		} finally {
			this.mirroring = false;
		}
	}

	private @Nullable Link link(Setting s) {
		String id = s.name.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
		String name = s.displayName();
		if (s instanceof Setting.Bool b) {
			BoolSetting v = new BoolSetting(id, name, "", b.value);
			v.onChange(() -> {
				if (!this.mirroring) {
					b.value = v.get();
					this.changed();
				}
			});
			return new Link(v, () -> v.set(b.value));
		}
		if (s instanceof Setting.Num n) {
			SliderSetting v = new SliderSetting(id, name, "", n.value, n.min, n.max, n.step, n.unit == null ? "" : n.unit);
			v.onChange(() -> {
				if (!this.mirroring) {
					n.value = v.get();
					this.changed();
				}
			});
			return new Link(v, () -> v.set(n.value));
		}
		if (s instanceof Setting.Mode m && m.options.length > 0) {
			ModeSetting v = new ModeSetting(id, name, "", m.get(), m.options);
			v.onChange(() -> {
				if (!this.mirroring) {
					m.index = v.index();
					this.changed();
				}
			});
			return new Link(v, () -> v.set(m.get()));
		}
		if (s instanceof Setting.Color c) {
			int[] palette = Setting.Color.PALETTE;
			ColorSetting v = new ColorSetting(id, name, "", palette[Math.max(0, Math.min(palette.length - 1, c.index))], palette);
			v.onChange(() -> {
				if (!this.mirroring) {
					for (int i = 0; i < palette.length; i++) {
						if (palette[i] == v.get()) {
							c.index = i;
						}
					}
					this.changed();
				}
			});
			return new Link(v, () -> {
				if (c.index >= 0 && c.index < palette.length) {
					v.set(palette[c.index]);
				}
			});
		}
		if (s instanceof Setting.Action a) {
			// A button: switching it on runs the action, and it springs back.
			BoolSetting v = new BoolSetting(id, a.label, "", false);
			v.onChange(() -> {
				if (!this.mirroring && v.get()) {
					try {
						a.action.run();
					} catch (Throwable e) {
						dev.lego.LegoClient.LOG("Visor-Aktion " + a.name + ": " + e);
					}
					this.mirroring = true;
					try {
						v.set(false);
					} finally {
						this.mirroring = false;
					}
				}
			});
			return new Link(v, () -> {
			});
		}
		return null;
	}

	private void changed() {
		dev.lego.core.Modules.scheduleSave();
	}

	private record Link(dev.visor.setting.Setting<?> visor, Runnable pull) {
		void pullValue() {
			this.pull.run();
		}
	}
}
