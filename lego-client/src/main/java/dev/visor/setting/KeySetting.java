package dev.visor.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.InputConstants;

/** A keyboard key (GLFW key code), or {@link #NONE} when unbound. */
public final class KeySetting extends Setting<Integer> {
	public static final int NONE = -1;

	public KeySetting(String id, String name, String description, int defaultValue) {
		super(id, name, description, defaultValue);
	}

	public boolean bound() {
		return this.value != NONE;
	}

	public String keyName() {
		if (!this.bound()) {
			return "None";
		}
		return InputConstants.Type.KEYSYM.getOrCreate(this.value).getDisplayName().getString();
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(this.value);
	}

	@Override
	public void fromJson(JsonElement json) {
		if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
			this.value = json.getAsInt();
		}
	}
}
