package dev.visor.browser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * The main video of the page as the page script reports it (see {@code browser/helper.js}).
 *
 * @param format    VR layout: "360", "360tb" (top-bottom 3D), "180", "180sbs" (side-by-side 3D),
 *                  or empty for a normal flat video
 * @param certainty how sure that layout is: {@link #SURE} (told by the site), {@link #LIKELY}
 *                  (looks like a VR player) or {@link #GUESS} (only the shape of the picture)
 * @param id        stays the same while the same video plays
 */
public record MediaState(
	int width, int height, double time, double duration, boolean paused, boolean muted, boolean ended,
	String format, int certainty, boolean ad, boolean skippable, boolean youtube, String id, String title
) {
	public static final int GUESS = 0;
	public static final int SURE = 1;
	public static final int LIKELY = 2;

	/** A VR video the headset should offer to show in VR. */
	public boolean vr() {
		return !this.format.isEmpty() && (this.certainty == SURE || this.certainty == LIKELY);
	}

	static @Nullable MediaState parse(@Nullable JsonElement element) {
		if (element == null || !element.isJsonObject()) {
			return null;
		}
		JsonObject o = element.getAsJsonObject();
		return new MediaState(
			number(o, "w"), number(o, "h"), decimal(o, "t"), decimal(o, "d"), number(o, "p") != 0, number(o, "m") != 0, number(o, "e") != 0,
			text(o, "f"), number(o, "sure"), number(o, "ad") != 0, number(o, "skip") != 0, number(o, "yt") != 0, text(o, "id"), text(o, "title")
		);
	}

	private static int number(JsonObject o, String key) {
		JsonElement e = o.get(key);
		return e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber() ? e.getAsInt() : 0;
	}

	private static double decimal(JsonObject o, String key) {
		JsonElement e = o.get(key);
		return e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber() ? e.getAsDouble() : 0.0;
	}

	private static String text(JsonObject o, String key) {
		JsonElement e = o.get(key);
		return e != null && e.isJsonPrimitive() ? e.getAsString() : "";
	}
}
