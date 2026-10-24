package dev.lego.ui;

import java.util.HashMap;
import java.util.Map;

/** Easing curves + smooth per-widget animation values. */
public final class Ease {
    private Ease() { }

    public static float clamp(float t) { return t < 0 ? 0 : (t > 1 ? 1 : t); }
    public static float outCubic(float t) { t = clamp(t); float f = 1 - t; return 1 - f * f * f; }
    public static float outQuint(float t) { t = clamp(t); float f = 1 - t; return 1 - f * f * f * f * f; }
    public static float inOutCubic(float t) { t = clamp(t); return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2; }
    public static float outBack(float t) {
        t = clamp(t);
        float c1 = 1.70158f, c3 = c1 + 1;
        return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }
    public static float outElastic(float t) {
        t = clamp(t);
        if (t == 0 || t == 1) return t;
        return (float) (Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * (2 * Math.PI / 3)) + 1);
    }
    public static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    /** Staggered progress: element i starts after i*delay (all in 0..1 units of the timeline). */
    public static float stagger(float t, int i, float delay, float dur) { return clamp((t - i * delay) / dur); }

    // ------------------------------------------------------------------ smoothed values
    private static final Map<String, float[]> VALUES = new HashMap<>();

    /** Moves the value for key towards target (exponential smoothing, speed ~ 1/seconds). */
    public static float to(String key, float target, float speed) {
        float[] v = VALUES.get(key);
        if (v == null) { v = new float[]{target}; VALUES.put(key, v); return target; }
        float k = 1f - (float) Math.exp(-Gx.dt * speed * UiSettings.anim());
        v[0] += (target - v[0]) * k;
        if (Math.abs(target - v[0]) < 0.0005f) v[0] = target;
        return v[0];
    }

    /** Same, but the first value starts at 'from'. */
    public static float to(String key, float target, float speed, float from) {
        if (!VALUES.containsKey(key)) VALUES.put(key, new float[]{from});
        return to(key, target, speed);
    }

    public static void set(String key, float v) { VALUES.put(key, new float[]{v}); }
    public static void reset(String prefix) { VALUES.keySet().removeIf(k -> k.startsWith(prefix)); }
    public static float get(String key, float def) { float[] v = VALUES.get(key); return v == null ? def : v[0]; }
}
