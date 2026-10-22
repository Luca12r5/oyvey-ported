package dev.visor.module;

import dev.visor.setting.KeySetting;
import dev.visor.setting.Setting;
import dev.visor.setting.SettingGroup;
import dev.visor.setting.SettingsStore;
import java.util.ArrayList;
import java.util.List;

/**
 * A client feature that can be switched on and off and configured. Subclasses put their
 * behaviour in {@link #onEnable()} / {@link #onDisable()}.
 */
public class Module {
	private final String id;
	private final String name;
	private final String description;
	private final String icon;
	private final Category category;
	private final KeySetting keybind;
	private final List<SettingGroup> groups = new ArrayList<>();
	private boolean enabled;

	public Module(String id, String name, String description, String icon, Category category) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.icon = icon;
		this.category = category;
		this.keybind = new KeySetting("key", "Keybind", "Toggles the module", KeySetting.NONE);
	}

	/** Adds a titled group of settings shown in the module's settings sheet. */
	public Module group(String title, Setting<?>... settings) {
		this.groups.add(SettingGroup.of(title, settings));
		return this;
	}

	public String id() {
		return this.id;
	}

	public String name() {
		return this.name;
	}

	public String description() {
		return this.description;
	}

	public String icon() {
		return this.icon;
	}

	public Category category() {
		return this.category;
	}

	public KeySetting keybind() {
		return this.keybind;
	}

	public List<SettingGroup> groups() {
		return this.groups;
	}

	public List<Setting<?>> settings() {
		List<Setting<?>> all = new ArrayList<>();
		for (SettingGroup group : this.groups) {
			all.addAll(group.settings());
		}
		return all;
	}

	public boolean enabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			if (enabled) {
				this.onEnable();
			} else {
				this.onDisable();
			}
			SettingsStore.markDirty();
		}
	}

	public void toggle() {
		this.setEnabled(!this.enabled);
	}

	/** Restores every setting (not the enabled state or keybind) to its default. */
	public void resetSettings() {
		for (Setting<?> setting : this.settings()) {
			setting.reset();
		}
	}

	protected void onEnable() {
	}

	protected void onDisable() {
	}

	void loadEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
