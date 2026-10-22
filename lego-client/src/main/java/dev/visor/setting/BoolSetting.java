package dev.visor.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class BoolSetting extends Setting<Boolean> {
	public BoolSetting(String id, String name, String description, boolean defaultValue) {
		super(id, name, description, defaultValue);
	}

	public boolean on() {
		return this.value;
	}

	public void toggle() {
		this.set(!this.value);
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(this.value);
	}

	@Override
	public void fromJson(JsonElement json) {
		if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isBoolean()) {
			this.value = json.getAsBoolean();
		}
	}
}
