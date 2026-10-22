package dev.visor.ui;

import java.util.HashMap;
import java.util.Map;

/** Per-widget springs, created on demand and kept alive while the panel exists. */
public final class UiAnimations {
	private final Map<String, Spring> springs = new HashMap<>();
	private float dt;

	public void beginFrame(float dt) {
		this.dt = dt;
	}

	/** Animates towards {@code target} and returns the current value. */
	public float get(String id, float target, float stiffness) {
		Spring spring = this.springs.computeIfAbsent(id, key -> new Spring(target, stiffness));
		return spring.target(target).update(this.dt);
	}

	public float get(String id, boolean on, float stiffness) {
		return this.get(id, on ? 1.0F : 0.0F, stiffness);
	}

	public void snap(String id, float value) {
		Spring spring = this.springs.get(id);
		if (spring != null) {
			spring.snap(value);
		}
	}
}
