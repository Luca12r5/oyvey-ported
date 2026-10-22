package dev.visor.sound;

import dev.visor.VisorClient;
import dev.visor.setting.VisorSettings;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundEvent;

/**
 * Interface sounds. They are client-only and never registered in the sound event registry, so
 * the client stays compatible with every server; playback only needs the sounds.json entry.
 */
public final class VisorSounds {
	public static final SoundEvent HOVER = event("ui.hover");
	public static final SoundEvent CLICK = event("ui.click");
	public static final SoundEvent TAB = event("ui.tab");
	public static final SoundEvent TOGGLE_ON = event("ui.toggle_on");
	public static final SoundEvent TOGGLE_OFF = event("ui.toggle_off");
	public static final SoundEvent PANEL_OPEN = event("ui.panel_open");
	public static final SoundEvent PANEL_CLOSE = event("ui.panel_close");
	public static final SoundEvent MENU_OPEN = event("ui.menu_open");
	public static final SoundEvent MENU_CLOSE = event("ui.menu_close");
	public static final SoundEvent SLIDER = event("ui.slider");
	public static final SoundEvent KEYBIND = event("ui.keybind");
	public static final SoundEvent NOTIFY = event("ui.notify");
	public static final SoundEvent RECENTER = event("ui.recenter");
	public static final SoundEvent DENY = event("ui.deny");
	public static final SoundEvent EQUIP = event("headset.equip");
	public static final SoundEvent REMOVE = event("headset.remove");
	public static final SoundEvent BOOT = event("headset.boot");

	private static final Set<SoundEvent> WARNED = new HashSet<>();

	private VisorSounds() {
	}

	private static SoundEvent event(String name) {
		return SoundEvent.createVariableRangeEvent(VisorClient.id(name));
	}

	public static void play(SoundEvent sound, float volume) {
		play(sound, volume, 1.0F);
	}

	public static void play(SoundEvent sound, float volume, float pitch) {
		float scaled = volume * VisorSettings.VOLUME.floatValue();
		if (scaled <= 0.001F) {
			return;
		}
		SoundEngine.PlayResult result = Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, Math.min(1.0F, scaled)));
		if (result == SoundEngine.PlayResult.NOT_STARTED && WARNED.add(sound)) {
			VisorClient.LOGGER.warn("Sound {} was not started by the sound engine (is the Interface volume at 0?)", sound.location());
		}
	}
}
