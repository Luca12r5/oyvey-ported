package dev.visor.ui;

/** Easing curves; all take and return values in 0..1. */
public final class Ease {
	private Ease() {
	}

	public static float clamp01(float t) {
		return t < 0.0F ? 0.0F : Math.min(t, 1.0F);
	}

	/** Progress of {@code time} through the window [start, end], clamped to 0..1. */
	public static float window(double time, double start, double end) {
		if (end <= start) {
			return time >= end ? 1.0F : 0.0F;
		}
		return clamp01((float) ((time - start) / (end - start)));
	}

	public static float smooth(float t) {
		t = clamp01(t);
		return t * t * (3.0F - 2.0F * t);
	}

	public static float smoother(float t) {
		t = clamp01(t);
		return t * t * t * (t * (t * 6.0F - 15.0F) + 10.0F);
	}

	public static float outCubic(float t) {
		t = 1.0F - clamp01(t);
		return 1.0F - t * t * t;
	}

	public static float inCubic(float t) {
		t = clamp01(t);
		return t * t * t;
	}

	public static float inOutCubic(float t) {
		t = clamp01(t);
		return t < 0.5F ? 4.0F * t * t * t : 1.0F - (float) Math.pow(-2.0F * t + 2.0F, 3.0) / 2.0F;
	}

	public static float outQuint(float t) {
		t = 1.0F - clamp01(t);
		return 1.0F - t * t * t * t * t;
	}

	public static float outExpo(float t) {
		t = clamp01(t);
		return t >= 1.0F ? 1.0F : 1.0F - (float) Math.pow(2.0, -10.0 * t);
	}

	/** Overshoots slightly before settling - nice for things popping into place. */
	public static float outBack(float t) {
		t = clamp01(t);
		float c1 = 1.70158F;
		float c3 = c1 + 1.0F;
		float u = t - 1.0F;
		return 1.0F + c3 * u * u * u + c1 * u * u;
	}

	public static float lerp(float t, float a, float b) {
		return a + (b - a) * t;
	}
}
