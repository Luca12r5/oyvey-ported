package dev.visor.setting;

import java.util.List;

/** A titled section of settings in a settings sheet. */
public record SettingGroup(String title, List<Setting<?>> settings) {
	public static SettingGroup of(String title, Setting<?>... settings) {
		return new SettingGroup(title, List.of(settings));
	}
}
