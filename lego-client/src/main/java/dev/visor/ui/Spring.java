package dev.visor.ui;

/**
 * Critically damped spring towards a target value. Frame rate independent, used for hover
 * highlights, sliding selections and anything that should feel physical rather than linear.
 */
public final class Spring {
	private float value;
	private float velocity;
	private float target;
	private final float stiffness;

	/** @param stiffness angular frequency; ~12 is snappy, ~6 is soft */
	public Spring(float initial, float stiffness) {
		this.value = initial;
		this.target = initial;
		this.stiffness = stiffness;
	}

	public Spring target(float target) {
		this.target = target;
		return this;
	}

	public void snap(float value) {
		this.value = value;
		this.target = value;
		this.velocity = 0.0F;
	}

	public float update(float dt) {
		dt = Math.min(dt, 0.05F);
		float omega = this.stiffness;
		float x = this.value - this.target;
		float exp = (float) Math.exp(-omega * dt);
		float newX = (x + (this.velocity + omega * x) * dt) * exp;
		this.velocity = (this.velocity - omega * (this.velocity + omega * x) * dt) * exp;
		this.value = this.target + newX;
		return this.value;
	}

	public float value() {
		return this.value;
	}

	public float target() {
		return this.target;
	}
}
