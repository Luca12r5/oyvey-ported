package dev.visor.setting;

import com.google.gson.JsonElement;
import java.util.function.BooleanSupplier;
import org.jspecify.annotations.Nullable;

/**
 * A configurable value shown in a settings sheet. Every setting knows its default, how to
 * persist itself and optionally when it is relevant (e.g. only while another setting is on).
 */
public abstract class Setting<T> {
	private final String id;
	private final String name;
	private final String description;
	private final T defaultValue;
	protected T value;
	private @Nullable T replacedDefault;
	private BooleanSupplier visible = () -> true;
	private Runnable onChange = () -> {
	};

	protected Setting(String id, String name, String description, T defaultValue) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public String id() {
		return this.id;
	}

	public String name() {
		return this.name;
	}

	public String description() {
		return this.description;
	}

	public T get() {
		return this.value;
	}

	public T defaultValue() {
		return this.defaultValue;
	}

	public void set(T value) {
		T sanitized = this.sanitize(value);
		if (!sanitized.equals(this.value)) {
			this.value = sanitized;
			this.onChange.run();
			SettingsStore.markDirty();
		}
	}

	public void reset() {
		this.set(this.defaultValue);
	}

	public boolean isDefault() {
		return this.value.equals(this.defaultValue);
	}

	protected T sanitize(T value) {
		return value;
	}

	/** Only show this setting while {@code condition} holds. */
	@SuppressWarnings("unchecked")
	public <S extends Setting<T>> S visibleWhen(BooleanSupplier condition) {
		this.visible = condition;
		return (S) this;
	}

	@SuppressWarnings("unchecked")
	public <S extends Setting<T>> S onChange(Runnable listener) {
		this.onChange = listener;
		return (S) this;
	}

	public boolean isVisible() {
		return this.visible.getAsBoolean();
	}

	/**
	 * The default this setting had in older versions. A value saved back then that still equals it
	 * was most likely never touched, so it moves on to the new default (see {@link SettingsStore}).
	 */
	@SuppressWarnings("unchecked")
	public <S extends Setting<T>> S replacesDefault(T previous) {
		this.replacedDefault = previous;
		return (S) this;
	}

	/** Called after reading a file written before the defaults changed. */
	void upgrade() {
		if (this.replacedDefault != null && this.value.equals(this.replacedDefault)) {
			this.reset();
		}
	}

	public abstract JsonElement toJson();

	public abstract void fromJson(JsonElement json);
}
