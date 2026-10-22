package dev.visor.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.List;

/** One choice out of a fixed list of options. */
public final class ModeSetting extends Setting<String> {
	private final List<String> modes;

	public ModeSetting(String id, String name, String description, String defaultValue, String... modes) {
		super(id, name, description, defaultValue);
		this.modes = List.of(modes);
		if (!this.modes.contains(defaultValue)) {
			throw new IllegalArgumentException("Default mode " + defaultValue + " is not one of " + this.modes);
		}
	}

	public List<String> modes() {
		return this.modes;
	}

	public int index() {
		return Math.max(0, this.modes.indexOf(this.value));
	}

	public boolean is(String mode) {
		return this.value.equals(mode);
	}

	public void cycle(int direction) {
		int size = this.modes.size();
		this.set(this.modes.get(Math.floorMod(this.index() + direction, size)));
	}

	@Override
	protected String sanitize(String value) {
		return this.modes.contains(value) ? value : this.defaultValue();
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(this.value);
	}

	@Override
	public void fromJson(JsonElement json) {
		if (json.isJsonPrimitive()) {
			this.value = this.sanitize(json.getAsString());
		}
	}
}
