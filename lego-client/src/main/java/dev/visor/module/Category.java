package dev.visor.module;

/** Module categories, in the order they appear in the sidebar. */
public enum Category {
	// The LEGO click GUI categories (their modules are the real LEGO modules, see LegoModule).
	HUD("HUD", "monitor", "LEGO: clean displays on screen", 0xFF3B82F6),
	VISUALS("Visuals", "sparkle", "LEGO: sky, effects, filters, particles", 0xFFA855F7),
	UTILITY("Utility", "wrench", "LEGO: legit helpers", 0xFF22C55E),
	PERFORMANCE("Leistung", "gauge", "LEGO: more FPS, less stutter", 0xFFF59E0B),
	SPOTIFY("Spotify", "music-notes", "LEGO: Spotify HUD and lyrics", 0xFF1DB954),
		COMBAT("Combat", "sword", "Fighting feedback, timing and target information", 0xFFF43F5E),
	MOVEMENT("Movement", "sneaker-move", "Sprinting, sneaking and getting around", 0xFF22C55E),
	RENDER("Render", "eye", "How the world and its entities are drawn", 0xFF8B5CF6),
	PLAYER("Player", "user", "Your character, cosmetics and nameplates", 0xFF0EA5E9),
	WORLD("World", "globe-hemisphere-west", "Maps, coordinates and the environment", 0xFF14B8A6),
	MISC("Misc", "dots-three-outline", "Everything else that makes life easier", 0xFFF59E0B),
	CUSTOM_TXT("Custom TxT", "palette", "Texture styles from Bauloo Styles", 0xFFE879F9);

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
