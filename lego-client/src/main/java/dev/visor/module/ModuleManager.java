package dev.visor.module;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import dev.visor.donut.DrDonutt;
import dev.visor.setting.BoolSetting;
import dev.visor.setting.ColorSetting;
import dev.visor.setting.KeySetting;
import dev.visor.setting.ModeSetting;
import dev.visor.setting.SettingsStore;
import dev.visor.setting.SliderSetting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

/**
 * All modules of the client, per category in menu order. Most of them are still empty shells:
 * name, settings, saving and keybinds work, and their behaviour goes in onEnable/onDisable.
 */
public final class ModuleManager {
	private static final Map<String, Module> MODULES = new LinkedHashMap<>();
	private static final Map<Module, Boolean> KEY_STATE = new HashMap<>();
	private static final int[] REDS = {0xFFF43F5E, 0xFFEF4444, 0xFFF97316, 0xFFFFFFFF, 0xFF2F7BFF, 0xFFA855F7};
	private static final int[] OUTLINES = {0xFFFFFFFF, 0xFF2F7BFF, 0xFF22C55E, 0xFFF59E0B, 0xFFEC4899, 0xFF000000};

	private ModuleManager() {
	}

	public static void init() {
		registerCombat();
		registerMovement();
		registerRender();
		registerPlayer();
		registerWorld();
		registerMisc();
		SettingsStore.register("modules.json", ModuleManager::write, ModuleManager::read);
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

	// ------------------------------------------------------------------------------------------
	// Modules

	private static void registerCombat() {
		Category c = Category.COMBAT;
		add(new Module("hit_color", "Hit Color", "Tints entities when they take damage", "drop", c)
			.group("Appearance",
				new ColorSetting("color", "Color", "Tint applied on hit", REDS[0], REDS),
				new SliderSetting("opacity", "Opacity", "Strength of the tint", 0.45, 0.0, 1.0, 0.01, "%", 100.0),
				new ModeSetting("mode", "Style", "How the tint appears", "Fade", "Flash", "Fade", "Pulse")));
		add(new Module("combo_counter", "Combo Counter", "Counts consecutive hits on a target", "lightning", c)
			.group("Display",
				new ModeSetting("position", "Position", "Where the counter is shown", "Crosshair", "Top", "Crosshair", "Corner"),
				new SliderSetting("reset", "Reset after", "Time without hits before the combo resets", 2.0, 0.5, 5.0, 0.5, " s"),
				new BoolSetting("best", "Show best combo", "Also show your highest combo", true)));
		add(new Module("reach_display", "Reach Display", "Shows the distance of your last hit", "ruler", c)
			.group("Display",
				new SliderSetting("decimals", "Decimals", "Precision of the distance", 2, 0, 3, 1, ""),
				new BoolSetting("chat", "Show in chat", "Also print each hit in chat", false)));
		add(new Module("target_hud", "Target HUD", "Details about the entity you are fighting", "target", c)
			.group("Layout",
				new ModeSetting("style", "Style", "Amount of information", "Compact", "Compact", "Detailed"),
				new BoolSetting("armor", "Show armor", "Armor pieces of the target", true),
				new BoolSetting("health", "Health bar", "Animated health bar", true)));
		add(new Module("crosshair", "Crosshair", "Custom crosshair shapes and colors", "crosshair", c)
			.group("Shape",
				new ModeSetting("style", "Style", "Crosshair shape", "Cross", "Dot", "Cross", "Circle"),
				new SliderSetting("size", "Size", "Length of the lines", 6, 2, 16, 1, " px"),
				new SliderSetting("gap", "Gap", "Space in the middle", 2, 0, 8, 1, " px"),
				new ColorSetting("color", "Color", "Crosshair color", 0xFFFFFFFF, OUTLINES)));
		add(new Module("cps", "CPS Counter", "Clicks per second for both mouse buttons", "cursor-click", c)
			.group("Display",
				new BoolSetting("right", "Right clicks", "Count right clicks too", true),
				new SliderSetting("window", "Average over", "Time window for the average", 1.0, 1.0, 3.0, 0.5, " s")));
		add(new Module("armor_status", "Armor Status", "Durability of your armor and held item", "shield", c)
			.group("Display",
				new ModeSetting("layout", "Layout", "Direction of the list", "Vertical", "Horizontal", "Vertical"),
				new BoolSetting("percent", "Percentages", "Show durability in percent", true),
				new SliderSetting("warn", "Warn below", "Highlight items under this durability", 0.2, 0.05, 0.5, 0.05, "%", 100.0)));
	}

	private static void registerMovement() {
		Category c = Category.MOVEMENT;
		add(new Module("toggle_sprint", "Toggle Sprint", "Keeps sprinting without holding the key", "person-simple-run", c)
			.group("Behaviour",
				new ModeSetting("mode", "Mode", "When sprinting is active", "Toggle", "Toggle", "Always"),
				new BoolSetting("status", "Show status", "Small label while sprint is locked", true)));
		add(new Module("toggle_sneak", "Toggle Sneak", "Sneak with a tap instead of holding", "sneaker-move", c)
			.group("Behaviour",
				new BoolSetting("status", "Show status", "Small label while sneaking is locked", true)));
		add(new Module("speed_display", "Speed Display", "Your current movement speed", "gauge", c)
			.group("Display",
				new ModeSetting("unit", "Unit", "Speed unit", "b/s", "b/s", "km/h"),
				new SliderSetting("decimals", "Decimals", "Precision", 1, 0, 2, 1, "")));
		add(new Module("elytra_info", "Elytra Info", "Altitude, speed and pitch while gliding", "airplane-tilt", c)
			.group("Display",
				new BoolSetting("warnings", "Low altitude warning", "Warn when gliding too close to the ground", true)));
		add(new Module("auto_walk", "Auto Walk", "Walks forward until you press a movement key", "footprints", c)
			.group("Behaviour",
				new BoolSetting("jump", "Auto jump", "Jump up single blocks while walking", false)));
		add(new Module("freelook", "Freelook", "Look around without turning your character", "eye", c)
			.group("Camera",
				new SliderSetting("sensitivity", "Sensitivity", "Camera speed while looking around", 1.0, 0.25, 2.0, 0.05, "x"),
				new BoolSetting("invert", "Invert pitch", "Invert up and down", false)));
	}

	private static void registerRender() {
		Category c = Category.RENDER;
		add(new DrDonutt());
		// Fullbright left out on purpose: seeing in the dark is an advantage over vanilla.
		add(new Module("zoom", "Zoom", "Smooth zoom with the scroll wheel", "binoculars", c)
			.group("Zoom",
				new SliderSetting("level", "Zoom level", "Default magnification", 4.0, 2.0, 10.0, 0.5, "x"),
				new BoolSetting("smooth", "Smooth camera", "Cinematic smoothing while zoomed", true),
				new BoolSetting("scroll", "Scroll to adjust", "Change the zoom with the scroll wheel", true)));
		add(new Module("motion_blur", "Motion Blur", "Soft trails when turning quickly", "sparkle", c)
			.group("Effect",
				new SliderSetting("strength", "Strength", "Amount of blur", 0.5, 0.1, 1.0, 0.05, "%", 100.0)));
		add(new Module("block_outline", "Block Outline", "Custom outline for the block you look at", "cube", c)
			.group("Outline",
				new ColorSetting("color", "Color", "Outline color", 0xFFFFFFFF, OUTLINES),
				new SliderSetting("width", "Thickness", "Line thickness", 2, 1, 5, 1, " px"),
				new BoolSetting("fill", "Fill faces", "Tint the faces of the block", false)));
		add(new Module("particles", "Particles", "Control how many particles are shown", "snowflake", c)
			.group("Amount",
				new ModeSetting("amount", "Amount", "Particle density", "All", "All", "Decreased", "Minimal"),
				new SliderSetting("hits", "Hit particles", "Multiplier for hit particles", 1.0, 0.0, 5.0, 0.5, "x")));
		add(new Module("time_changer", "Time Changer", "Pick your own time of day", "moon", c)
			.group("Time",
				new SliderSetting("time", "Time", "Client-side time of day", 6000, 0, 24000, 250, "")));
		add(new Module("weather", "Weather", "Override rain and thunder", "cloud", c)
			.group("Weather",
				new ModeSetting("mode", "Mode", "Visible weather", "Clear", "Clear", "Rain", "Thunder")));
	}

	private static void registerPlayer() {
		Category c = Category.PLAYER;
		add(new Module("cosmetics", "Cosmetics", "Capes, wings and hats", "crown", c)
			.group("Visibility",
				new BoolSetting("others", "Show on others", "Render cosmetics of other players", true)));
		add(new Module("nick_hider", "Nick Hider", "Replace your name on screen", "ghost", c)
			.group("Name",
				new ModeSetting("name", "Shown name", "What others see on your screen", "You", "You", "Player", "Hidden")));
		add(new Module("skin_layers", "Skin Layers", "Three-dimensional skin overlays", "user", c)
			.group("Detail",
				new SliderSetting("distance", "Detail distance", "Up to which distance layers are 3D", 12, 4, 32, 1, " m")));
		add(new Module("nametags", "Nametags", "Customise player name tags", "tag", c)
			.group("Style",
				new SliderSetting("background", "Background", "Opacity of the tag background", 0.25, 0.0, 1.0, 0.05, "%", 100.0),
				new BoolSetting("own", "Show own tag", "Show your own name in third person", false)));
		add(new Module("emotes", "Emotes", "Play emotes from a quick wheel", "hand-palm", c)
			.group("Wheel",
				new BoolSetting("sounds", "Emote sounds", "Play sounds with emotes", true)));
		add(new Module("hitboxes", "Hitboxes", "Show the hitboxes of entities", "selection-all", c)
			.group("Display",
				new ColorSetting("color", "Color", "Box color", 0xFFFFFFFF, OUTLINES),
				new BoolSetting("eyes", "Eye line", "Show the eye height line", false)));
	}

	private static void registerWorld() {
		Category c = Category.WORLD;
		add(new Module("coordinates", "Coordinates", "Position, biome and facing", "map-pin", c)
			.group("Display",
				new BoolSetting("biome", "Show biome", "Name of the current biome", true),
				new BoolSetting("direction", "Show direction", "Compass direction you face", true)));
		add(new Module("minimap", "Minimap", "Map of your surroundings with waypoints", "compass", c)
			.group("Map",
				new ModeSetting("shape", "Shape", "Outline of the map", "Round", "Round", "Square"),
				new SliderSetting("size", "Size", "Size of the map", 120, 80, 220, 10, " px"),
				new SliderSetting("zoom", "Zoom", "Map zoom level", 1.0, 0.5, 3.0, 0.25, "x")));
		add(new Module("waypoints", "Waypoints", "Mark places and find them again", "flag", c)
			.group("Display",
				new BoolSetting("beams", "Beacon beams", "Show beams at waypoints", true),
				new SliderSetting("max", "Max distance", "Hide waypoints further away", 2000, 100, 10000, 100, " m")));
		add(new Module("chunk_borders", "Chunk Borders", "Outline the edges of chunks", "squares-four", c)
			.group("Display",
				new ColorSetting("color", "Color", "Line color", 0xFFF59E0B, OUTLINES)));
		add(new Module("light_levels", "Light Levels", "Show where mobs can spawn", "lightbulb", c)
			.group("Overlay",
				new SliderSetting("radius", "Radius", "Overlay range", 16, 8, 32, 1, " m")));
		add(new Module("biome_info", "Biome Info", "Details about the biome you are in", "tree", c)
			.group("Display",
				new BoolSetting("temperature", "Temperature", "Show biome temperature", false)));
	}

	private static void registerMisc() {
		Category c = Category.MISC;
		add(new Module("auto_gg", "Auto GG", "Sends a message at the end of a match", "trophy", c)
			.group("Message",
				new ModeSetting("message", "Message", "What gets sent", "gg", "gg", "Good game", "gf"),
				new SliderSetting("delay", "Delay", "Wait before sending", 1.0, 0.0, 5.0, 0.5, " s")));
		add(new Module("chat_tweaks", "Chat Tweaks", "Timestamps and a cleaner chat", "chat-circle-dots", c)
			.group("Chat",
				new BoolSetting("timestamps", "Timestamps", "Time before every message", true),
				new BoolSetting("compact", "Compact spam", "Merge repeated messages", true)));
		add(new Module("screenshots", "Screenshots", "Copy screenshots to the clipboard", "camera", c)
			.group("Capture",
				new BoolSetting("clipboard", "Copy to clipboard", "Copy every new screenshot", true)));
		add(new Module("performance", "Performance", "FPS, ping and memory overlay", "pulse", c)
			.group("Display",
				new BoolSetting("memory", "Show memory", "Java memory usage", false),
				new ModeSetting("position", "Position", "Corner of the overlay", "Top left", "Top left", "Top right")));
		add(new Module("discord", "Discord Presence", "Show what you are playing on Discord", "game-controller", c)
			.group("Presence",
				new BoolSetting("server", "Show server", "Include the server address", false)));
		add(new Module("notifications", "Notifications", "Small toasts when modules change", "bell", c)
			.group("Toasts",
				new SliderSetting("duration", "Duration", "How long a toast stays", 2.0, 1.0, 5.0, 0.5, " s")));
	}
}
