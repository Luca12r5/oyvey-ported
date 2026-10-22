package dev.visor.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Locale;

/** A number between {@code min} and {@code max}, snapped to {@code step}. */
public final class SliderSetting extends Setting<Double> {
	private final double min;
	private final double max;
	private final double step;
	private final String unit;
	private final double displayScale;

	public SliderSetting(String id, String name, String description, double defaultValue, double min, double max, double step, String unit) {
		this(id, name, description, defaultValue, min, max, step, unit, 1.0);
	}

	/**
	 * @param displayScale factor applied when showing the value (e.g. 100 to show 0.8 as "80%")
	 */
	public SliderSetting(String id, String name, String description, double defaultValue, double min, double max, double step, String unit, double displayScale) {
		super(id, name, description, defaultValue);
		this.min = min;
		this.max = max;
		this.step = step;
		this.unit = unit;
		this.displayScale = displayScale;
	}

	public double min() {
		return this.min;
	}

	public double max() {
		return this.max;
	}

	public double step() {
		return this.step;
	}

	public float floatValue() {
		return this.value.floatValue();
	}

	/** Position of the value along the track, 0..1. */
	public float fraction() {
		return (float) ((this.value - this.min) / (this.max - this.min));
	}

	public void setFraction(double fraction) {
		this.set(this.min + Math.max(0.0, Math.min(1.0, fraction)) * (this.max - this.min));
	}

	@Override
	protected Double sanitize(Double value) {
		double clamped = Math.max(this.min, Math.min(this.max, value));
		if (this.step > 0) {
			clamped = this.min + Math.round((clamped - this.min) / this.step) * this.step;
		}
		return Math.round(clamped * 1.0E6) / 1.0E6;
	}

	public String format() {
		double shown = this.value * this.displayScale;
		double shownStep = this.step * this.displayScale;
		String number = shownStep >= 1.0 || shownStep == 0.0
			? Long.toString(Math.round(shown))
			: String.format(Locale.ROOT, shownStep >= 0.1 ? "%.1f" : "%.2f", shown);
		return number + this.unit;
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(this.value);
	}

	@Override
	public void fromJson(JsonElement json) {
		if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
			this.value = this.sanitize(json.getAsDouble());
		}
	}
}
