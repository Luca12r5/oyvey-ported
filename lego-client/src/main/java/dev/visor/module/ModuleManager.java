package dev.visor.module;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import dev.visor.donut.DrDonutt;
import dev.visor.setting.KeySetting;
import dev.visor.setting.SettingsStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

/**
 * All modules shown in the Visor menu, per category in menu order: the LEGO client's modules
 * (bridged, see {@link LegoBridge}) and Visor's own (DrDonutt).
 */
public final class ModuleManager {
	private static final Map<String, Module> MODULES = new LinkedHashMap<>();
	private static final Map<Module, Boolean> KEY_STATE = new HashMap<>();

	private ModuleManager() {
	}

	public static void init() {
		// The menu shows the LEGO client's real modules (see LegoBridge) plus DrDonutt. The
		// placeholder modules this class used to register are gone: they had no behaviour.
		add(new DrDonutt());
		SettingsStore.register("modules.json", ModuleManager::write, ModuleManager::read);
	}

	private static boolean bridged;

	/** Adds the LEGO modules once they exist (LegoClient registers them first) and keeps them in sync. */
	public static void tickBridge() {
		if (!bridged && !dev.lego.core.Modules.ALL.isEmpty()) {
			bridged = true;
			for (Module m : LegoBridge.modules()) {
				MODULES.putIfAbsent(m.id(), m);
			}
		}
		LegoBridge.pull(List.copyOf(MODULES.values()));
	}

	public static List<Module> all() {
		return List.copyOf(MODULES.values());
	}

	public static List<Module> of(Category category) {
		List<Module> list = new ArrayList<>();
		for (Module module : MODULES.values()) {
			if (module.category() == category) {
				list.add(module);
			}
		}
		return Collections.unmodifiableList(list);
	}

	public static @Nullable Module get(String id) {
		return MODULES.get(id);
	}

	public static int enabledCount() {
		int count = 0;
		for (Module module : MODULES.values()) {
			if (module.enabled()) {
				count++;
			}
		}
		return count;
	}

	public static int enabledCount(Category category) {
		int count = 0;
		for (Module module : of(category)) {
			if (module.enabled()) {
				count++;
			}
		}
		return count;
	}

	/** Toggles modules whose keybind was just pressed (only while no screen is open). */
	public static void tickKeybinds(Minecraft minecraft, boolean keyboardCaptured) {
		boolean allowed = minecraft.screen == null && minecraft.player != null && !keyboardCaptured;
		for (Module module : MODULES.values()) {
			if (module instanceof LegoBridge.Bridged) {
				continue; // the LEGO client handles these keys itself
			}
			KeySetting key = module.keybind();
			boolean down = allowed && key.bound() && InputConstants.isKeyDown(minecraft.getWindow(), key.get());
			boolean wasDown = KEY_STATE.getOrDefault(module, false);
			if (down && !wasDown) {
				module.toggle();
			}
			KEY_STATE.put(module, down);
		}
	}

	private static Module add(Module module) {
		MODULES.put(module.id(), module);
		return module;
	}

	// ------------------------------------------------------------------------------------------
	// Persistence

	private static JsonObject write() {
		JsonObject root = SettingsStore.stamp(new JsonObject());
		for (Module module : MODULES.values()) {
			if (module instanceof LegoBridge.Bridged) {
				continue; // saved by the LEGO client
			}
			JsonObject json = new JsonObject();
			json.addProperty("enabled", module.enabled());
			json.add("key", module.keybind().toJson());
			json.add("settings", SettingsStore.write(module.settings()));
			root.add(module.id(), json);
		}
		return root;
	}

	private static void read(JsonObject root) {
		int version = SettingsStore.version(root);
		for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
			Module module = MODULES.get(entry.getKey());
			if (module == null || !entry.getValue().isJsonObject()) {
				continue;
			}
			JsonObject json = entry.getValue().getAsJsonObject();
			if (json.has("enabled")) {
				module.loadEnabled(json.get("enabled").getAsBoolean());
			}
			if (json.has("key")) {
				module.keybind().fromJson(json.get("key"));
			}
			if (json.has("settings") && json.get("settings").isJsonObject()) {
				SettingsStore.read(json.getAsJsonObject("settings"), module.settings(), version);
			}
		}
	}
}
