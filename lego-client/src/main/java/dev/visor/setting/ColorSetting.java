package dev.visor.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** A colour picked from a small palette of swatches (ARGB). */
public final class ColorSetting extends Setting<Integer> {
	private final int[] palette;

	public ColorSetting(String id, String name, String description, int defaultValue, int... palette) {
		super(id, name, description, defaultValue);
		this.palette = palette;
	}

	public int[] palette() {
		return this.palette;
	}

	public int argb() {
		return this.value;
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(String.format("#%08X", this.value));
	}

	@Override
	public void fromJson(JsonElement json) {
		if (json.isJsonPrimitive()) {
			try {
				this.value = (int) Long.parseLong(json.getAsString().replace("#", ""), 16);
			} catch (NumberFormatException ignored) {
				// keep the current colour
			}
		}
	}
}
