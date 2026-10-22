package dev.visor.setting;

import java.util.ArrayList;
import java.util.List;

/** Settings of the headset itself (interface, hands, effects, sound, browser). They drive the renderer. */
public final class VisorSettings {
	public static final int[] ACCENTS = {0xFF2F7BFF, 0xFF8B5CF6, 0xFFEC4899, 0xFF14B8A6, 0xFFF59E0B, 0xFFEF4444};

	// Interface
	public static final SliderSetting PANEL_DISTANCE = new SliderSetting(
		"panel_distance", "Panel distance", "How far the panels float in front of you", 1.62, 1.2, 2.4, 0.02, " m"
	);
	public static final SliderSetting PANEL_SCALE = new SliderSetting(
		"panel_scale", "Panel size", "Overall size of the panels", 1.0, 0.8, 1.25, 0.01, "%", 100.0
	);
	public static final ModeSetting PANEL_SHAPE = new ModeSetting(
		"panel_shape", "Panel shape", "Curve the main panel around you, or keep it flat", "Curved", "Curved", "Flat"
	);
	public static final String FOLLOW_ME = "Follow me";
	public static final String STAY = "Stay in place";
	public static final ModeSetting PANEL_ANCHOR = new ModeSetting(
		"panel_anchor", "Panel position", "Drift along when you walk, or stay put so you can walk around them", STAY, FOLLOW_ME, STAY
	).replacesDefault(FOLLOW_ME);
	public static final ColorSetting ACCENT = new ColorSetting(
		"accent", "Accent colour", "Highlights, cursor and selections", ACCENTS[0], ACCENTS
	);

	// Hands
	public static final BoolSetting SHOW_HANDS = new BoolSetting(
		"show_hands", "Show hands", "Hand-tracking style hands while the headset is on", true
	);
	public static final BoolSetting POINTER_RAY = new BoolSetting(
		"pointer_ray", "Pointer ray", "Beam from your hand to the cursor", true
	).visibleWhen(SHOW_HANDS::on);
	public static final SliderSetting HAND_OPACITY = new SliderSetting(
		"hand_opacity", "Hand opacity", "How solid the hands look", 0.62, 0.2, 1.0, 0.01, "%", 100.0
	).visibleWhen(SHOW_HANDS::on);

	// Effects
	public static final SliderSetting VIGNETTE = new SliderSetting(
		"vignette", "Lens vignette", "Soft dark edge of the lenses", 0.55, 0.0, 1.0, 0.01, "%", 100.0
	);
	public static final BoolSetting HEADSET_ANIMATION = new BoolSetting(
		"headset_animation", "Headset animation", "Arms put the headset on and take it off", true
	);
	public static final BoolSetting BOOT_ANIMATION = new BoolSetting(
		"boot_animation", "Start-up screen", "Logo and chime when the headset turns on", true
	).visibleWhen(HEADSET_ANIMATION::on);

	// Sound
	public static final SliderSetting VOLUME = new SliderSetting(
		"volume", "Interface volume", "Loudness of all headset sounds", 0.85, 0.0, 1.0, 0.01, "%", 100.0
	);
	public static final BoolSetting HOVER_SOUNDS = new BoolSetting(
		"hover_sounds", "Hover sounds", "Soft tick when the cursor moves onto something", true
	);

	// Browser
	public static final SliderSetting BROWSER_ZOOM = new SliderSetting(
		"browser_zoom", "Page zoom", "Size of text and pictures on web pages", 1.0, 0.75, 1.5, 0.05, "%", 100.0
	);
	public static final SliderSetting BROWSER_VOLUME = new SliderSetting(
		"browser_volume", "Media volume", "Loudness of videos and music in the browser", 1.0, 0.0, 1.0, 0.01, "%", 100.0
	);
	public static final ModeSetting BROWSER_FPS = new ModeSetting(
		"browser_fps", "Page frame rate", "Smoother pages, or less work for your PC", "60 fps", "30 fps", "60 fps"
	);
	public static final BoolSetting BROWSER_PAUSE = new BoolSetting(
		"browser_pause", "Pause when removed", "Pause videos when you take the headset off", true
	);
	public static final ModeSetting SEARCH_ENGINE = new ModeSetting(
		"search_engine", "Search engine", "Used by the search bars", "Google", "Google", "Bing", "DuckDuckGo"
	);

	public static final SettingGroup BROWSER = SettingGroup.of("Browser", BROWSER_ZOOM, BROWSER_VOLUME, BROWSER_FPS, BROWSER_PAUSE, SEARCH_ENGINE);

	// Video
	public static final SliderSetting FULLSCREEN_SIZE = new SliderSetting(
		"fullscreen_size", "Fullscreen size", "How much of your view a fullscreen video fills", 0.92, 0.6, 1.0, 0.01, "%", 100.0
	);
	public static final SliderSetting CINEMA_DIM = new SliderSetting(
		"cinema_dim", "Dim the world", "How dark it gets around a fullscreen video", 0.9, 0.0, 1.0, 0.01, "%", 100.0
	);
	public static final BoolSetting AMBIENT_GLOW = new BoolSetting(
		"ambient_glow", "Ambient glow", "The colours of a fullscreen video softly light up around it", true
	);
	public static final String VR_AUTOMATIC = "Automatic";
	public static final String VR_ASK = "Ask first";
	public static final ModeSetting VR_VIDEOS = new ModeSetting(
		"vr_videos", "VR videos", "Open 360° and 180° videos in VR by themselves, or ask first", VR_AUTOMATIC, VR_AUTOMATIC, VR_ASK
	);
	public static final ModeSetting VR_QUALITY = new ModeSetting(
		"vr_quality", "VR quality", "Sharper VR videos, or less work for your PC", "Standard", "Standard", "High"
	);

	public static final SettingGroup VIDEO = SettingGroup.of("Video", FULLSCREEN_SIZE, CINEMA_DIM, AMBIENT_GLOW, VR_VIDEOS, VR_QUALITY);

	public static final List<SettingGroup> GROUPS = List.of(
		SettingGroup.of("Interface", PANEL_ANCHOR, PANEL_DISTANCE, PANEL_SCALE, PANEL_SHAPE, ACCENT),
		SettingGroup.of("Hands", SHOW_HANDS, POINTER_RAY, HAND_OPACITY),
		SettingGroup.of("Effects", VIGNETTE, HEADSET_ANIMATION, BOOT_ANIMATION),
		SettingGroup.of("Sound", VOLUME, HOVER_SOUNDS),
		BROWSER,
		VIDEO
	);

	private VisorSettings() {
	}

	public static List<Setting<?>> all() {
		List<Setting<?>> all = new ArrayList<>();
		for (SettingGroup group : GROUPS) {
			all.addAll(group.settings());
		}
		return all;
	}

	/** True while the panels drift along with the player (instead of staying where they are). */
	public static boolean follows() {
		return PANEL_ANCHOR.is(FOLLOW_ME);
	}

	public static void init() {
		SettingsStore.register("settings.json", () -> SettingsStore.stamp(SettingsStore.write(all())), json -> SettingsStore.read(json, all(), SettingsStore.version(json)));
	}
}
