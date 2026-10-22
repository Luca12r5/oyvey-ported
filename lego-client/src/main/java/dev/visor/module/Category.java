package dev.visor.module;

/** Menu categories (the LEGO client's listed categories plus Visor), in sidebar order. */
public enum Category {
	HUD("HUD", "squares-four", "Clean on-screen displays", 0xFF2F7BFF),
	VISUALS("Visuals", "sparkle", "Sky, effects, filters and particles", 0xFF8B5CF6),
	UTILITY("Utility", "wrench", "Fair helpers: sprint, waypoints, chat", 0xFF22C55E),
	PERFORMANCE("Performance", "rocket-launch", "More FPS, fewer stutters", 0xFFF59E0B),
	SPOTIFY("Spotify", "music-notes", "Spotify HUD and karaoke lyrics", 0xFF1ED760),
	VISOR("Visor", "virtual-reality", "Headset, browser and DrDonutt", 0xFFEC4899);

	/** The menu page for a LEGO client category (null: it has its own screen). */
	public static @org.jspecify.annotations.Nullable Category of(dev.lego.core.Category lego) {
		return switch (lego) {
			case HUD -> HUD;
			case VISUALS -> VISUALS;
			case UTILITY -> UTILITY;
			case PERFORMANCE -> PERFORMANCE;
			case SPOTIFY -> SPOTIFY;
			default -> null;
		};
	}

	private final String displayName;
	private final String icon;
	private final String description;
	private final int color;

	Category(String displayName, String icon, String description, int color) {
		this.displayName = displayName;
		this.icon = icon;
		this.description = description;
		this.color = color;
	}

	public String displayName() {
		return this.displayName;
	}

	public String icon() {
		return this.icon;
	}

	public String description() {
		return this.description;
	}

	/** Identity colour of the category (icon tiles, subtle tints). */
	public int color() {
		return this.color;
	}
}
