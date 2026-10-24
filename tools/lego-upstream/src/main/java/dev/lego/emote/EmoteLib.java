package dev.lego.emote;

import java.util.ArrayList;
import java.util.List;

/**
 * All emotes as smooth procedural poses (no Minecraft classes).
 * Pose array (radians, NaN = keep the vanilla value): head p/y/r, body p/y/r, right arm p/y/r, left arm p/y/r, right leg p/y/r, left leg p/y/r.
 * Arm pitch -3 = straight up, right-arm roll +1.57 = out to the side (left arm: negative).
 */
public final class EmoteLib {
    private EmoteLib() { }

    public static final int HEAD = 0, BODY = 3, RARM = 6, LARM = 9, RLEG = 12, LLEG = 15, N = 18;

    public interface PoseFn { void pose(float t, float[] p); }

    public static final class Emote {
        public final String id, name, icon;
        public final float duration;       // seconds (loop emotes repeat until you move or stop)
        public final boolean loop;
        public final PoseFn fn;
        /** Bubble above the head (null = none). */
        public final String bubble;
        /** Degrees per second the body spins (0 = none). */
        public final float spin;
        Emote(String id, String name, String icon, float duration, boolean loop, String bubble, float spin, PoseFn fn) {
            this.id = id; this.name = name; this.icon = icon; this.duration = duration; this.loop = loop; this.bubble = bubble; this.spin = spin; this.fn = fn;
        }

        public float[] pose(float t) {
            float[] p = new float[N];
            java.util.Arrays.fill(p, Float.NaN);
            fn.pose(t, p);
            return p;
        }
    }

    public static final List<Emote> ALL = new ArrayList<>();

    public static void addGenerated(String id, String name, String icon, float dur, boolean loop, String bubble, float spin, PoseFn fn) {
        ALL.add(new Emote(id, name, icon, dur, loop, bubble, spin, fn));
    }

    private static void add(String id, String name, String icon, float dur, boolean loop, String bubble, float spin, PoseFn fn) { addGenerated(id,name,icon,dur,loop,bubble,spin,fn); }

    public static Emote get(String id) {
        for (Emote e : ALL) if (e.id.equals(id)) return e;
        return null;
    }

    public static int index(String id) {
        for (int i = 0; i < ALL.size(); i++) if (ALL.get(i).id.equals(id)) return i;
        return -1;
    }

    static float s(double v) { return (float) Math.sin(v); }
    static float c(double v) { return (float) Math.cos(v); }
    static void set(float[] p, int part, double pitch, double yaw, double roll) { p[part] = (float) pitch; p[part + 1] = (float) yaw; p[part + 2] = (float) roll; }
    static void pitch(float[] p, int part, double v) { p[part] = (float) v; }

    static {
        add("wave", "Winken", "emote", 3f, false, null, 0, (t, p) -> set(p, RARM, -2.75, 0, 0.25 + s(t * 9) * 0.45));
        add("dance", "Tanzen", "music", 6f, true, null, 220, (t, p) -> {
            float a = s(t * 8);
            set(p, RARM, -1.6 - a * 1.2, 0, 0.3);
            set(p, LARM, -1.6 + a * 1.2, 0, -0.3);
            set(p, RLEG, a * 0.4, 0, 0.05);
            set(p, LLEG, -a * 0.4, 0, -0.05);
            set(p, HEAD, a * 0.2, s(t * 4) * 0.3, 0);
        });
        add("tpose", "T-Pose", "user", 3f, false, null, 0, (t, p) -> { set(p, RARM, 0, 0, 1.5708); set(p, LARM, 0, 0, -1.5708); set(p, HEAD, 0, 0, 0); });
        add("clap", "Klatschen", "sparkle", 3f, false, null, 0, (t, p) -> {
            float k = Math.abs(s(t * 7)) * 0.45f;
            set(p, RARM, -1.35, 0, -0.05 - k);
            set(p, LARM, -1.35, 0, 0.05 + k);
        });
        add("dab", "Dab", "star", 2.2f, false, null, 0, (t, p) -> { set(p, HEAD, 0.55, -0.4, 0); set(p, RARM, -2.3, 0, -0.9); set(p, LARM, -1.25, 0, -0.6); });
        add("salute", "Salutieren", "shield", 2.5f, false, null, 0, (t, p) -> set(p, RARM, -2.2, 0, -0.75));
        add("cheer", "Jubeln", "trophy", 3f, false, null, 0, (t, p) -> {
            float b = Math.abs(s(t * 6)) * 0.3f;
            set(p, RARM, -2.9 + b, 0, 0.35);
            set(p, LARM, -2.9 + b, 0, -0.35);
            set(p, HEAD, -0.25, 0, 0);
        });
        add("facepalm", "Facepalm", "user", 2.5f, false, null, 0, (t, p) -> { set(p, HEAD, 0.45, 0, 0); set(p, RARM, -2.05, 0, -0.45); });
        add("heart", "Herz", "heart", 2.8f, false, "heart", 0, (t, p) -> { set(p, RARM, -2.4, 0, -0.35); set(p, LARM, -2.4, 0, 0.35); });
        add("gg", "GG", "gg", 2.6f, false, "gg", 0, (t, p) -> set(p, RARM, -2.2, 0, 0.3 + s(t * 6) * 0.2));
        add("laugh", "Lachen", "gg", 2.6f, false, "laugh", 0, (t, p) -> {
            float b = s(t * 14) * 0.12f;
            set(p, HEAD, -0.35 + b, 0, 0);
            set(p, RARM, -0.4, 0, 0.25);
            set(p, LARM, -0.4, 0, -0.25);
        });
        add("fire", "Feuer", "flame", 2.6f, false, "fire", 0, (t, p) -> { set(p, RARM, -1.5, 0.3, 0); set(p, LARM, -1.5, -0.3, 0); });
        add("floss", "Floss", "music", 6f, true, null, 0, (t, p) -> {
            float a = s(t * 9), h = s(t * 18);
            set(p, RARM, 0.25 * a, 0, 0.35 + a * 0.55);
            set(p, LARM, -0.25 * a, 0, -0.35 + a * 0.55);
            set(p, BODY, 0, 0, h * 0.08);
            set(p, RLEG, 0, 0, -h * 0.05);
            set(p, LLEG, 0, 0, -h * 0.05);
        });
        add("zombie", "Zombie", "skull", 5f, true, null, 0, (t, p) -> {
            float w = s(t * 3) * 0.12f;
            set(p, RARM, -1.57 + w, 0.08, 0);
            set(p, LARM, -1.57 - w, -0.08, 0);
            set(p, HEAD, 0.25, s(t * 1.5) * 0.3, s(t * 1.3) * 0.2);
        });
        add("bow", "Verbeugen", "user", 2.6f, false, null, 0, (t, p) -> {
            float k = (float) Math.sin(Math.min(1, t / 2.6) * Math.PI);
            set(p, BODY, 0.55 * k, 0, 0);
            set(p, HEAD, 0.6 * k, 0, 0);
            set(p, RARM, -0.2 * k, 0, 0.1);
            set(p, LARM, 0.6 * k, 0, -0.1);
        });
        add("shrug", "Achselzucken", "info", 2.4f, false, null, 0, (t, p) -> {
            float k = Math.abs(s(t * 2.6)) ;
            set(p, RARM, -0.5, 0.9, 1.1 + k * 0.15);
            set(p, LARM, -0.5, -0.9, -1.1 - k * 0.15);
            set(p, HEAD, 0.1, 0, 0.25);
        });
        add("headbang", "Headbang", "music", 5f, true, null, 0, (t, p) -> {
            float k = s(t * 11);
            set(p, HEAD, 0.35 + k * 0.45, 0, 0);
            set(p, RARM, -2.8, 0, 0.3 + k * 0.1);
            set(p, LARM, -0.3, 0, -0.1);
        });
        add("guitar", "Luftgitarre", "music", 5f, true, null, 0, (t, p) -> {
            float k = s(t * 14);
            set(p, RARM, -0.7 + k * 0.25, -0.4, -0.3);
            set(p, LARM, -1.2, 0.6, 0.2);
            set(p, HEAD, 0.2 + Math.abs(k) * 0.15, -0.3, 0);
        });
        add("robot", "Roboter", "chip", 6f, true, null, 0, (t, p) -> {
            int step = (int) Math.floor(t * 2.5) % 4;
            double[][] frames = {{-1.57, 0, 0, 0}, {0, 0, -1.57, 0}, {-1.57, 0.8, -1.57, -0.8}, {0, 1.2, 0, -1.2}};
            double[] f = frames[step];
            set(p, RARM, f[0], 0, f[1]);
            set(p, LARM, f[2], 0, f[3]);
            set(p, HEAD, 0, (step % 2 == 0 ? 0.5 : -0.5), 0);
        });
        add("propeller", "Propeller", "reset", 4f, true, null, 0, (t, p) -> {
            float a = t * 12;
            set(p, RARM, -1.57 + s(a) * 1.4, 0, c(a) * 0.6 + 0.6);
            set(p, LARM, -1.57 - s(a) * 1.4, 0, -c(a) * 0.6 - 0.6);
        });
        add("think", "Denken", "info", 3f, false, "think", 0, (t, p) -> {
            set(p, RARM, -2.1, -0.3, -0.5);
            set(p, LARM, -0.8, 0.5, 0.2);
            set(p, HEAD, -0.2, 0.25, 0.15);
        });
        add("cry", "Weinen", "rain", 3f, false, "cry", 0, (t, p) -> {
            float b = s(t * 12) * 0.06f;
            set(p, HEAD, 0.35 + b, 0, 0);
            set(p, RARM, -2.0, 0, -0.35);
            set(p, LARM, -2.0, 0, 0.35);
        });
        add("meditate", "Meditieren", "sun", 8f, true, "zen", 0, (t, p) -> {
            float br = s(t * 1.6) * 0.06f;
            set(p, RARM, -0.9 + br, -0.4, 0.5);
            set(p, LARM, -0.9 + br, 0.4, -0.5);
            set(p, HEAD, 0.1 + br, 0, 0);
        });
        add("box", "Boxen", "target", 4f, true, null, 0, (t, p) -> {
            float a = s(t * 10), b = s(t * 10 + Math.PI);
            set(p, RARM, -1.3 - Math.max(0, a) * 0.3, 0.3 - Math.max(0, a) * 0.3, -0.2);
            set(p, LARM, -1.3 - Math.max(0, b) * 0.3, -0.3 + Math.max(0, b) * 0.3, 0.2);
            set(p, HEAD, 0.15, s(t * 5) * 0.15, 0);
            set(p, BODY, 0, s(t * 5) * 0.12, 0);
        });
        add("point", "Zeigen", "arrow", 2.5f, false, null, 0, (t, p) -> { set(p, RARM, -1.6, 0.1, 0); set(p, HEAD, 0, 0.1, 0); });
        add("swim", "Schwimmen", "rain", 5f, true, null, 0, (t, p) -> {
            float a = t * 5;
            set(p, RARM, -1.57 + s(a) * 1.6, 0, 0.3);
            set(p, LARM, -1.57 + s(a + Math.PI) * 1.6, 0, -0.3);
            set(p, RLEG, s(a * 2) * 0.35, 0, 0);
            set(p, LLEG, -s(a * 2) * 0.35, 0, 0);
        });
        add("chicken", "Hühnertanz", "music", 5f, true, null, 0, (t, p) -> {
            float a = Math.abs(s(t * 8));
            set(p, RARM, 0, -0.3, 0.2 + a * 0.9);
            set(p, LARM, 0, 0.3, -0.2 - a * 0.9);
            set(p, HEAD, -0.1 + s(t * 16) * 0.12, 0, 0);
            set(p, RLEG, -a * 0.5, 0, 0);
        });
        add("sleep", "Schlafen", "moon", 6f, true, "sleep", 0, (t, p) -> {
            float br = s(t * 1.4) * 0.05f;
            set(p, HEAD, 0.55 + br, 0, 0.35);
            set(p, RARM, 0.1, 0, 0.05);
            set(p, LARM, 0.1, 0, -0.05);
        });
        add("hype", "Hype", "bolt", 4f, true, null, 0, (t, p) -> {
            float a = Math.abs(s(t * 9));
            set(p, RARM, -3 + a * 0.6, 0, 0.5);
            set(p, LARM, -3 + a * 0.6, 0, -0.5);
            set(p, HEAD, -0.3, s(t * 9) * 0.2, 0);
            set(p, RLEG, -a * 0.3, 0, 0.1);
            set(p, LLEG, a * 0.3, 0, -0.1);
        });
    }

    // ------------------------------------------------------------------ keyframe helpers
    /** Smooth keyframes: pairs of (time, value); clamps before the first / after the last key. */
    static float kf(float t, double... k) {
        if (t <= k[0]) return (float) k[1];
        for (int i = 2; i < k.length; i += 2) {
            if (t <= k[i]) {
                double u = (t - k[i - 2]) / Math.max(1e-4, k[i] - k[i - 2]);
                u = u * u * (3 - 2 * u);
                return (float) (k[i - 1] + (k[i + 1] - k[i - 1]) * u);
            }
        }
        return (float) k[k.length - 1];
    }

    /** Time inside a loop period. */
    static float loop(float t, float period) { return t % period; }

    /** Quick hit-and-decay 0..1 (for recoils / impacts) at time t0. */
    static float hit(float t, float t0, float up, float down) {
        if (t < t0) return 0;
        if (t < t0 + up) return (t - t0) / up;
        return (float) Math.exp(-(t - t0 - up) / down);
    }

    // ------------------------------------------------------------------ emote pack 2
    static {
        add("karate", "Karate-Kombo", "hit", 4.6f, false, null, 0, (t, p) -> {
            // ready -> punch R/L/R -> high kick -> chop with kiai -> bow
            set(p, RARM,
                    kf(t, 0, 0, .35, .35, .45, .35, .55, -1.62, .85, -1.62, .95, .35, 1.25, .35, 1.35, -1.62, 1.6, -1.62, 1.8, -1.25, 2.3, -1.25, 2.55, -1.3, 2.75, -3.0, 2.9, -1.35, 3.3, -1.35, 3.6, 0),
                    kf(t, .45, 0, .55, -.14, .85, -.14, .95, 0, 1.25, 0, 1.35, -.14, 1.6, -.14, 1.8, 0),
                    kf(t, 0, .05, .35, .12, 1.6, .12, 1.8, -.1, 2.55, -.1, 2.75, .25, 2.9, -.12, 3.3, -.12, 3.6, .05));
            set(p, LARM,
                    kf(t, 0, 0, .35, -1.35, .55, .35, .85, .35, .95, -1.62, 1.25, -1.62, 1.35, .35, 1.6, .35, 1.8, -1.25, 1.95, .35, 2.4, .35, 2.6, -1.1, 3.3, -1.1, 3.6, 0),
                    kf(t, .85, 0, .95, .14, 1.25, .14, 1.35, 0),
                    kf(t, 0, -.05, .35, -.1, 1.8, .1, 1.95, -.7, 2.4, -.7, 2.6, -.05, 3.6, -.05));
            set(p, RLEG, kf(t, 0, 0, .35, .32, 1.85, .32, 2.05, -1.65, 2.3, -1.6, 2.55, .32, 3.3, .32, 3.6, 0), 0, kf(t, 0, 0, .35, .06, 3.6, .02));
            set(p, LLEG, kf(t, 0, 0, .35, -.28, 1.85, -.28, 2.05, .12, 2.55, -.28, 3.3, -.28, 3.6, 0), 0, kf(t, 0, 0, .35, -.06, 3.6, -.02));
            set(p, BODY, kf(t, 0, 0, .35, .06, 1.85, .06, 2.05, -.14, 2.4, -.14, 2.6, .06, 2.9, .1, 3.3, .06, 3.6, 0, 3.8, .45, 4.25, .45, 4.6, 0), 0, 0);
            set(p, HEAD, kf(t, 0, 0, .35, .12, 2.75, .12, 2.9, -.18, 3.3, 0, 3.8, .15, 4.6, 0), kf(t, .55, 0, .6, .05, 1.0, -.05, 1.4, .05, 1.8, 0), 0);
        });
        add("yoga_tree", "Yoga-Baum", "leaf", 8f, true, "zen", 0, (t, p) -> {
            // prayer hands at the chest -> leg up to the side -> arms grow up into a tree, gentle balance sway
            float up = ease(t / 1.2f), grow = ease((t - 1.2f) / 1.4f), sway = s(t * 1.3) * 0.035f * grow, br = s(t * 1.7) * 0.03f;
            set(p, RARM, -1.3 * (1 - grow) - 3.0 * grow + br, -0.42 * (1 - grow), -0.3 * (1 - grow) + 0.2 * grow + sway);
            set(p, LARM, -1.3 * (1 - grow) - 3.0 * grow + br, 0.42 * (1 - grow), 0.3 * (1 - grow) - 0.2 * grow + sway);
            set(p, RLEG, -0.35 * up, 0.35 * up, 0.95 * up + sway * 0.5);
            set(p, LLEG, 0, 0, -0.03 + sway * 0.4);
            set(p, BODY, 0, 0, sway * 0.4);
            set(p, HEAD, -0.12 * grow + br, 0, sway * 0.8);
        });
        add("bunny", "Hasensprung", "jump", 4.8f, true, null, 0, (t, p) -> {
            float u = loop(t, 0.6f) / 0.6f, air = s(u * Math.PI), land = (float) Math.max(0, Math.cos(u * Math.PI * 2)) * 0.5f;
            int hop = (int) (t / 0.6f);
            float tilt = (hop % 2 == 0 ? 1 : -1) * 0.14f;
            set(p, RARM, -1.35 - 0.22 * air, -0.28, -0.1);          // little paws in front of the chest
            set(p, LARM, -1.35 - 0.22 * air, 0.28, 0.1);
            set(p, RLEG, 0.55 * air - 0.12 * land, 0, 0.02);
            set(p, LLEG, 0.55 * air - 0.12 * land, 0, -0.02);
            set(p, BODY, 0.12 * land - 0.04 * air, 0, 0);
            set(p, HEAD, -0.18 * air + 0.1 * land, 0, tilt * air);
        });
        add("zombie_walk", "Zombie-Gang", "skull", 6.4f, true, null, 0, (t, p) -> {
            float w = s(t * Math.PI * 2 / 1.6), lurch = s(t * Math.PI * 2 / 1.6 + 0.6);
            float twitch = loop(t, 3.2f) < 0.18f ? s(t * 70) * 0.25f : 0;
            set(p, RARM, -1.45 + s(t * 2.1) * 0.1, 0.06, -0.05 + lurch * 0.06);
            set(p, LARM, -1.72 + s(t * 2.1 + 1.5) * 0.1, -0.06, 0.05 + lurch * 0.06);
            set(p, RLEG, 0.38 * w, 0, 0.04);
            set(p, LLEG, -0.38 * w, 0, -0.04);
            set(p, BODY, 0.1, 0, lurch * 0.05);
            set(p, HEAD, 0.28 + s(t * 1.7) * 0.1, twitch, s(t * 1.9) * 0.38);
        });
        add("ballet", "Ballett-Drehung", "sparkle", 8f, true, null, 240, (t, p) -> {
            // alternates between a pirouette (arms rounded above the head, knee out) and an arabesque
            float u = loop(t, 4f), ara = kf(u, 0, 0, 1.6, 0, 2.1, 1, 3.5, 1, 4.0, 0);
            float br = s(t * 3) * 0.04f;
            set(p, RARM, -2.85 * (1 - ara) - 1.75 * ara + br, 0, 0.36 * (1 - ara) + 0.05 * ara);
            set(p, LARM, -2.85 * (1 - ara) - 0.25 * ara + br, 0, -0.36 * (1 - ara) - 1.35 * ara);
            set(p, RLEG, -0.55 * (1 - ara), 0.25 * (1 - ara), 0.7 * (1 - ara) + 0.02 * ara);
            set(p, LLEG, 0.95 * ara, 0, -0.03);
            set(p, BODY, 0.18 * ara, 0, 0);
            set(p, HEAD, -0.22 * (1 - ara) - 0.3 * ara, 0, 0.12 * (1 - ara));
        });
        add("star_jump", "Jubel-Sprung", "trophy", 4.8f, true, null, 0, (t, p) -> {
            // crouch & swing the arms back -> star jump -> land
            float u = loop(t, 1.2f);
            float crouch = kf(u, 0, 0, .3, 1, .42, 0, 1.0, 0, 1.2, 0), star = kf(u, .3, 0, .48, 1, .72, 1, .95, 0);
            float land = kf(u, .9, 0, .98, 1, 1.2, 0);
            set(p, RARM, 0.55 * crouch - 2.9 * star, 0, 0.1 * (1 - star) - 0.5 * star);
            set(p, LARM, 0.55 * crouch - 2.9 * star, 0, -0.1 * (1 - star) + 0.5 * star);
            set(p, RLEG, -0.1 * star, 0, 0.45 * star);
            set(p, LLEG, -0.1 * star, 0, -0.45 * star);
            set(p, BODY, 0.28 * crouch + 0.12 * land - 0.08 * star, 0, 0);
            set(p, HEAD, -0.15 * crouch - 0.35 * star, 0, 0);
        });
        add("deep_bow", "Tiefe Verbeugung", "crown", 3.8f, false, null, 0, (t, p) -> {
            // theatrical: right arm sweeps out -> hand to the stomach, deep bow with the left arm out behind -> rise
            float sweep = kf(t, 0, 0, .55, 1, .9, 0), bow = kf(t, .6, 0, 1.3, 1, 2.8, 1, 3.5, 0);
            set(p, RARM, -0.7 * sweep - 1.1 * bow, -0.65 * bow, 1.35 * sweep - 0.1 * bow);
            set(p, LARM, 0.55 * bow, 0, -0.1 - 0.9 * bow);
            set(p, RLEG, 0.1 * bow, 0, 0.03);
            set(p, LLEG, -0.1 * bow, 0, -0.03);
            set(p, BODY, 0.75 * bow, 0, 0);
            set(p, HEAD, -0.15 * sweep + 0.1 * bow, 0, 0);
        });
        add("snowball", "Schneeball werfen", "snow", 3.4f, false, null, 0, (t, p) -> {
            // scoop -> pack -> wind up -> throw -> follow through
            float pack = t > 0.6f && t < 1.25f ? s(t * 22) * 0.12f : 0;
            set(p, RARM,
                    kf(t, 0, 0, .35, -.75, .6, -.75, .85, -1.25, 1.25, -1.25, 1.6, -3.25, 1.72, -3.25, 1.86, -1.2, 2.1, -.4, 2.6, -.4, 3.2, 0),
                    kf(t, .6, 0, .85, -.3, 1.25, -.3, 1.6, 0),
                    kf(t, .35, .05, .85, -.2, 1.25, -.2, 1.6, -.25, 1.86, -.1, 2.1, -.5, 2.6, -.5, 3.2, .05) + pack);
            set(p, LARM,
                    kf(t, 0, 0, .35, -.75, .6, -.75, .85, -1.25, 1.25, -1.25, 1.6, -1.65, 1.8, -1.6, 1.95, .3, 2.6, .3, 3.2, 0),
                    kf(t, .6, 0, .85, .3, 1.25, .3, 1.6, 0),
                    kf(t, .35, -.05, .85, .2, 1.25, .2, 1.6, -.1, 1.95, -.2, 3.2, -.05) - pack);
            set(p, BODY, kf(t, 0, 0, .35, .55, .6, .55, .85, 0, 1.6, -.14, 1.8, .22, 2.3, .12, 3.2, 0), 0, 0);
            set(p, RLEG, kf(t, .6, 0, 1.2, .35, 1.8, .35, 2.0, -.05, 3.2, 0), 0, .04);
            set(p, LLEG, kf(t, .6, 0, 1.2, -.3, 1.8, -.3, 2.0, .15, 3.2, 0), 0, -.04);
            set(p, HEAD, kf(t, 0, 0, .35, 0, .6, .2, .85, .45, 1.25, .45, 1.5, -.05, 2.2, -.05, 2.9, -.1, 3.4, 0), 0, kf(t, 2.4, 0, 2.7, .2, 3.4, 0));
        });
        add("xmas_dance", "Weihnachts-Tanz", "gift", 8f, true, "heart", 0, (t, p) -> {
            // jingle bells: shake right up, shake left up, both up swaying, clap clap
            float u = loop(t, 4f), jing = s(t * 34) * 0.12f, step = s(t * Math.PI * 2);
            float rUp = kf(u, 0, 0, .2, 1, .9, 1, 1.1, 0, 1.9, 0, 2.1, 1, 2.9, 1, 3.1, 0);
            float lUp = kf(u, .9, 0, 1.1, 1, 2.9, 1, 3.1, 0);
            float clap = kf(u, 3.0, 0, 3.15, 1, 3.85, 1, 4.0, 0), cl = Math.abs(s(t * 12)) * 0.3f * clap;
            set(p, RARM, -2.65 * rUp + jing * rUp - 1.35 * clap, -0.3 * clap + cl, 0.2 - 0.5 * rUp + 0.2 * rUp * lUp - 0.2 * clap);
            set(p, LARM, -2.65 * lUp - jing * lUp - 1.35 * clap, 0.3 * clap - cl, -0.2 + 0.5 * lUp - 0.2 * rUp * lUp + 0.2 * clap);
            set(p, RLEG, 0, 0, 0.05 + Math.max(0, step) * 0.28);
            set(p, LLEG, 0, 0, -0.05 - Math.max(0, -step) * 0.28);
            set(p, BODY, 0, 0, step * 0.05);
            set(p, HEAD, -0.1, s(t * Math.PI) * 0.18, step * 0.16);
        });
        add("unwrap", "Geschenk auspacken", "gift", 4.8f, false, "heart", 0, (t, p) -> {
            // hold the box -> shake it -> rip the paper twice -> gasp -> lift it high
            float hold = kf(t, 0, 0, .35, 1, 2.3, 1, 2.5, 0);
            float shake = t > 0.5f && t < 1.4f ? s(t * 26) * 0.1f : 0;
            float rip = kf(t, 1.55, 0, 1.68, 1, 1.85, 0, 1.98, 1, 2.2, .2);
            float gasp = kf(t, 2.3, 0, 2.5, 1, 2.9, 1, 3.1, 0), lift = kf(t, 2.95, 0, 3.3, 1);
            float bob = Math.abs(s(t * 7)) * 0.18f * lift;
            set(p, RARM, -1.1 * hold + shake - 0.2 * rip - 2.3 * gasp - 2.95 * lift + bob, -0.22 * hold + 0.75 * rip, -0.2 * hold + 0.45 * rip + 0.45 * gasp + 0.12 * lift);
            set(p, LARM, -1.1 * hold + shake - 0.2 * rip - 2.3 * gasp - 2.95 * lift + bob, 0.22 * hold - 0.75 * rip, 0.2 * hold - 0.45 * rip - 0.45 * gasp - 0.12 * lift);
            set(p, BODY, 0.06 * hold - 0.06 * gasp - 0.08 * lift, 0, 0);
            set(p, HEAD, 0.42 * hold - 0.3 * gasp - 0.35 * lift, 0, kf(t, .4, 0, .7, .22, 1.4, .22, 1.6, 0));
            set(p, RLEG, -0.2 * lift * Math.abs(s(t * 7)), 0, 0.04);
        });
        add("shiver", "Zittern vor Kälte", "snow", 6f, true, null, 0, (t, p) -> {
            // arms hugging the body, knees together, trembling with a big shudder every few seconds
            float big = (float) Math.exp(-Math.pow((loop(t, 2.6f) - 1.3f) / 0.18f, 2));
            float tr = s(t * 58) * (0.025f + 0.07f * big), tr2 = s(t * 47 + 1) * (0.02f + 0.05f * big);
            set(p, RARM, -1.08 + tr2, -0.55, -0.35 + tr);
            set(p, LARM, -0.92 - tr2, 0.55, 0.35 - tr);
            set(p, RLEG, 0, 0, -0.06 + tr * 0.4);
            set(p, LLEG, 0, 0, 0.06 - tr * 0.4);
            set(p, BODY, 0.12 + big * 0.05, 0, tr * 0.3);
            set(p, HEAD, 0.28 + tr2 * 0.6, tr * 0.5, tr);
        });
        add("chill", "Chillen", "sun", 8f, true, null, 0, (t, p) -> {
            // hands behind the head, leaning back, lazy nod to the beat
            float in = ease(t / 0.8f), nod = s(t * 3.4) * 0.07f;
            set(p, RARM, -3.2 * in, 0, 0.62 * in + 0.05 * (1 - in));
            set(p, LARM, -3.2 * in, 0, -0.62 * in - 0.05 * (1 - in));
            set(p, BODY, -0.1 * in, 0, 0);
            set(p, HEAD, (-0.14 + nod) * in, s(t * 0.55) * 0.18, 0.08 * in);
            set(p, RLEG, -0.05 * in, 0, -0.08 * in);
            set(p, LLEG, 0.05 * in, 0, 0.12 * in + Math.max(0, s(t * 3.4)) * 0.05);
        });
        add("fingerguns", "Fingerpistolen", "target", 3.6f, false, "fire", 0, (t, p) -> {
            // aim -> pew (R) pew (L) pew (R) -> blow the smoke off
            float aim = kf(t, 0, 0, .35, 1, 2.35, 1, 2.6, 0), blow = kf(t, 2.4, 0, 2.7, 1, 3.3, 1, 3.6, 0);
            float r1 = hit(t, .7f, .05f, .12f) + hit(t, 1.5f, .05f, .12f), l1 = hit(t, 1.1f, .05f, .12f);
            float shrug = kf(t, 1.8, 0, 2.0, 1, 2.3, 0);
            set(p, RARM, -1.52 * aim - 0.4 * r1 - 2.1 * blow, (-0.1 + 0.08 * shrug) * aim - 0.35 * blow, 0.35 * blow);
            set(p, LARM, -1.52 * aim - 0.4 * l1, 0.1 * aim, 0);
            set(p, BODY, -0.07 * aim, 0, 0);
            set(p, HEAD, -0.05 * aim + 0.12 * blow, -0.12 * aim, 0.2 * aim - 0.1 * blow);
            set(p, RLEG, 0, 0, 0.1);
            set(p, LLEG, -0.12 * aim, 0, -0.06);
        });
        add("thinker", "Denker-Pose", "info", 8f, true, "think", 0, (t, p) -> {
            // leaning forward, chin on the fist, other arm resting across the lap, foot forward; slow "hmm" nod
            float in = ease(t / 0.9f), hmm = s(t * 1.1) * 0.05f;
            set(p, RARM, -2.05 * in, -0.35 * in, 0.2 * in);
            set(p, LARM, -0.85 * in, 0.45 * in, 0.2 * in);
            set(p, BODY, 0.32 * in, 0, 0);
            set(p, HEAD, (-0.12 + hmm) * in, 0.1 * in, 0.12 * in);
            set(p, RLEG, -0.45 * in, 0, 0.05);
            set(p, LLEG, 0.08 * in, 0, -0.05);
        });
        add("drums", "Schlagzeug", "music", 6f, true, null, 0, (t, p) -> {
            // alternating sticks, bass drum kick, a crash every bar
            float bar = loop(t, 2f), crash = kf(bar, 1.55, 0, 1.65, 1, 1.85, 1, 2.0, 0);
            float r = Math.max(0, s(t * 15)), l = Math.max(0, s(t * 15 + Math.PI));
            set(p, RARM, -0.95 - 0.32 * r * (1 - crash) - 1.1 * crash, 0, -0.18 * (1 - crash) - 0.5 * crash);
            set(p, LARM, -0.95 - 0.32 * l, 0, 0.18);
            set(p, RLEG, -0.28 * Math.max(0, s(t * 7.5)), 0, 0.05);
            set(p, BODY, 0.08, 0, 0);
            set(p, HEAD, 0.15 + Math.abs(s(t * 7.5)) * 0.2 - 0.25 * crash, 0, 0);
        });
        add("macarena", "Macarena", "music", 9.6f, true, null, 0, (t, p) -> {
            // 12 beats: arms out, cross to the shoulders, behind the head, to the hips, hands back + hip wiggle
            double[][] R = {{-1.57, 0, 0}, {-1.57, 0, 0}, {-1.57, 0, 0}, {-1.57, 0, 0}, {-1.5, -0.95, 0}, {-1.5, -0.95, 0}, {-3.05, 0, 0.55}, {-3.05, 0, 0.55}, {-0.45, 0, -0.45}, {-0.45, 0, -0.45}, {0.45, 0, 0.12}, {0.45, 0, 0.12}};
            double[][] L = {{0, 0, -0.05}, {-1.57, 0, 0}, {-1.57, 0, 0}, {-1.57, 0, 0}, {-1.57, 0, 0}, {-1.62, 0.95, 0}, {-1.62, 0.95, 0}, {-3.05, 0, -0.55}, {-3.05, 0, -0.55}, {-0.45, 0, 0.45}, {-0.45, 0, 0.45}, {0.45, 0, -0.12}};
            double[] rest = {0, 0, 0.05}, restL = {0, 0, -0.05};
            float beat = 0.4f, u = loop(t, beat * 12);
            int i = (int) (u / beat) % 12, pi = (i + 11) % 12;
            float k = ease((u - i * beat) / (beat * 0.45f));
            double[] r0 = t < beat ? rest : R[pi], l0 = t < beat ? restL : L[pi];
            double pop = (i == 2 || i == 3) ? s(k * Math.PI) * 0.15 : 0;      // flip the palms: little bounce
            set(p, RARM, r0[0] + (R[i][0] - r0[0]) * k - pop, r0[1] + (R[i][1] - r0[1]) * k, r0[2] + (R[i][2] - r0[2]) * k);
            set(p, LARM, l0[0] + (L[i][0] - l0[0]) * k - pop, l0[1] + (L[i][1] - l0[1]) * k, l0[2] + (L[i][2] - l0[2]) * k);
            float hip = s(t * Math.PI / beat);
            set(p, BODY, 0, 0, hip * 0.05);
            set(p, RLEG, 0, 0, 0.05 + Math.max(0, hip) * 0.12);
            set(p, LLEG, 0, 0, -0.05 - Math.max(0, -hip) * 0.12);
            set(p, HEAD, 0, 0, hip * 0.12);
        });
        add("penguin", "Pinguin-Watscheln", "snow", 6f, true, null, 0, (t, p) -> {
            float w = s(t * 6.5), flap = Math.max(0, s(t * 13)) * 0.22f;
            set(p, RARM, 0.08, 0, 0.28 + flap);
            set(p, LARM, 0.08, 0, -0.28 - flap);
            set(p, RLEG, 0, 0, 0.04 + Math.max(0, w) * 0.2);
            set(p, LLEG, 0, 0, -0.04 - Math.max(0, -w) * 0.2);
            set(p, BODY, 0, 0, w * 0.09);
            set(p, HEAD, -0.12 + Math.abs(w) * 0.06, s(t * 1.2) * 0.25, -w * 0.14);
        });
        add("superhero", "Superheld", "bolt", 4.4f, false, null, 0, (t, p) -> {
            // fists on the hips, chest out -> one fist to the sky, ready for take-off
            float in = kf(t, 0, 0, .4, 1), fly = kf(t, 2.0, 0, 2.35, 1, 3.9, 1, 4.4, 0);
            float wind = s(t * 5) * 0.03f;
            set(p, RARM, 0.18 * in * (1 - fly) - 3.05 * fly, -0.35 * in * (1 - fly), 0.55 * in * (1 - fly) - 0.08 * fly + wind);
            set(p, LARM, 0.18 * in, 0.35 * in, -0.55 * in);
            set(p, BODY, -0.07 * in, 0, 0);
            set(p, HEAD, -0.2 * in - 0.25 * fly, -0.25 * in * (1 - fly), 0);
            set(p, RLEG, -0.3 * fly, 0, 0.14 * in * (1 - fly));
            set(p, LLEG, 0, 0, -0.14 * in);
        });
    }

    // ------------------------------------------------------------------ cutscene emotes (camera flight + effects, see Cutscenes)
    public static boolean isCutscene(String id) { return id.startsWith("cs_"); }

    static float ease(float x) { x = Math.max(0, Math.min(1, x)); return x * x * (3 - 2 * x); }

    static {
        add("cs_landing", "Heldenlandung", "bolt", 5.5f, false, null, 0, (t, p) -> {
            // jump pose -> hard landing on one knee with a fist on the ground -> stand up proud
            float land = ease((t - 0.9f) / 0.25f), up = ease((t - 3.6f) / 0.8f);
            float crouch = land * (1 - up);
            set(p, BODY, 0.55 * crouch, 0, 0);
            set(p, HEAD, -0.35 * crouch - 0.2 * up, 0, 0);
            set(p, RARM, -0.3 * crouch + (-2.9 * up), 0, 0.15);
            set(p, LARM, -1.1 * crouch + 0.1, -0.3 * crouch, -0.4 * crouch - 0.1);
            set(p, RLEG, -1.35 * crouch, 0.25 * crouch, 0);
            set(p, LLEG, 0.9 * crouch, -0.1 * crouch, 0);
        });
        add("cs_victory", "Siegesfeier", "trophy", 6.5f, false, "gg", 0, (t, p) -> {
            float b = Math.abs(s(t * 6)) * 0.25f;
            set(p, RARM, -2.95 + b, 0, 0.35);
            set(p, LARM, -2.95 + b, 0, -0.35);
            set(p, HEAD, -0.3, s(t * 2) * 0.25, 0);
            set(p, RLEG, -Math.abs(s(t * 6)) * 0.25, 0, 0.05);
        });
        add("cs_portal", "Portal-Beschwörung", "sparkle", 6.5f, false, null, 0, (t, p) -> {
            float r = ease(t / 1.2f), w = s(t * 3) * 0.08f;
            set(p, RARM, -1.6 - 1.2 * r + w, 0.2, 0.5 * r);
            set(p, LARM, -1.6 - 1.2 * r - w, -0.2, -0.5 * r);
            set(p, HEAD, -0.45 * r, 0, 0);
            set(p, BODY, -0.08 * r, 0, 0);
        });
        add("cs_power", "Energie-Aufladung", "flame", 6.5f, false, "fire", 0, (t, p) -> {
            float charge = ease(t / 3.5f), burst = ease((t - 3.8f) / 0.3f);
            float shake = s(t * 60) * 0.03f * charge * (1 - burst);
            set(p, RARM, -0.4 * (1 - burst) - 2.9 * burst, 0.3 * (1 - burst), 0.5 + shake + 0.3 * burst);
            set(p, LARM, -0.4 * (1 - burst) - 2.9 * burst, -0.3 * (1 - burst), -0.5 - shake - 0.3 * burst);
            set(p, BODY, 0.25 * charge * (1 - burst) - 0.1 * burst, 0, shake);
            set(p, HEAD, 0.3 * charge * (1 - burst) - 0.45 * burst, 0, 0);
            set(p, RLEG, -0.25 * charge, 0, 0.2 * charge);
            set(p, LLEG, 0.1 * charge, 0, -0.2 * charge);
        });
        add("cs_selfie", "Selfie", "camera", 4.5f, false, "heart", 0, (t, p) -> {
            float r = ease(t / 0.6f);
            set(p, RARM, -1.9 * r, -0.4 * r, -0.1);
            set(p, LARM, -2.3 * r, 0.1, 0.65 * r);           // peace sign next to the head
            set(p, HEAD, -0.15 * r, -0.25 * r, 0.22 * r);
        });
        add("cs_xmas", "Weihnachtszauber", "gift", 8f, false, null, 0, (t, p) -> {
            // looks up at the growing tree, then cheers with both arms, little hop at the star burst
            float look = ease((t - 0.6f) / 0.8f), cheer = ease((t - 3.0f) / 0.4f), hop = Math.max(0, s((t - 3.2f) * 9)) * (t > 3.2f && t < 3.55f ? 1 : 0);
            set(p, HEAD, -0.45 * look * (1 - cheer * 0.5), 0, 0);
            double wave = s(t * 8) * 0.25 * cheer;
            set(p, RARM, -0.2 - 2.7 * cheer + wave, 0, 0.35 * cheer);
            set(p, LARM, -0.2 - 2.7 * cheer - wave, 0, -0.35 * cheer);
            set(p, BODY, -0.1 * cheer, 0, 0);
            set(p, RLEG, -0.3 * hop, 0, 0.05);
            set(p, LLEG, -0.3 * hop, 0, -0.05);
        });
        add("cs_darkaura", "Dunkle Aura", "moon", 7.6f, false, null, 0, (t, p) -> {
            // hunched, head bowed -> slowly raises the head (eye shot) -> fast slash -> menacing stand
            float raise = ease((t - 2.7f) / 1.1f), slash = ease((t - 5.2f) / 0.18f), after = ease((t - 5.6f) / 0.8f);
            float breathe = s(t * 2.2) * 0.03f;
            set(p, HEAD, 0.4 * (1 - raise) + breathe * 0.5 - 0.12 * after, 0, 0);
            set(p, BODY, 0.12 * (1 - raise) + 0.05 * slash * (1 - after), 0, 0);
            double arm = t < 5.2f ? -0.25 - 0.3 * raise : -2.4 + 2.9 * slash - 0.2 * after;
            if (t >= 5.0f && t < 5.2f) arm = -0.55 - 1.85 * ease((t - 5.0f) / 0.2f);   // wind up
            set(p, RARM, arm, -0.35 * slash * (1 - after), 0.12 + 0.5 * slash * (1 - after));
            set(p, LARM, -0.2 + breathe, 0.1, -0.12);
            set(p, RLEG, -0.08 * slash, 0, 0.06);
            set(p, LLEG, 0.12 * slash, 0, -0.06);
        });
        add("cs_evillaugh", "Böses Lachen", "eye", 10f, false, null, 0, (t, p) -> {
            // quiet chuckle behind a hand -> head thrown back laughing -> dead still for the eye close-up -> arms wide, maniacal laughter
            float amp = kf(t, 0, .015, 1.25, .02, 1.4, .05, 2.5, .04, 2.62, .085, 3.9, .07, 4.3, 0, 5.95, 0, 6.05, .1, 9.1, .1, 9.6, .02, 10, 0);
            float sh = s(t * 24) * amp, sh2 = s(t * 24 + 1.3) * amp;
            set(p, HEAD, kf(t, 0, .2, .8, .38, 2.58, .38, 2.62, -.45, 3.9, -.5, 4.36, 0, 5.95, 0, 6.05, -.35, 6.4, -.72, 9.1, -.72, 9.8, -.1) + sh,
                    0, kf(t, 0, 0, .8, .12, 2.58, .12, 2.62, -.08, 3.9, -.08, 4.36, 0, 5.95, 0, 6.3, .1, 9.1, .1, 9.8, 0) + sh2 * 0.5);
            set(p, BODY, kf(t, 0, .05, .8, .15, 2.58, .15, 2.62, -.08, 3.9, -.1, 4.36, 0, 5.95, 0, 6.1, -.18, 9.1, -.18, 9.8, 0) + sh2 * 0.5, 0, 0);
            // left hand over the face, slides down before the eye shot; then both arms thrown wide
            set(p, LARM, kf(t, 0, 0, .8, -2.35, 2.58, -2.35, 2.62, -2.75, 3.9, -2.75, 4.3, -.15, 5.95, -.15, 6.15, -.5, 9.1, -.5, 9.8, -.1) + sh,
                    kf(t, 0, 0, .8, .35, 3.9, .35, 4.3, 0),
                    kf(t, 0, -.05, .8, -.3, 3.9, -.3, 4.3, -.08, 5.95, -.08, 6.15, -1.95, 9.1, -1.95, 9.8, -.3) - sh);
            // right hand clutching the stomach
            set(p, RARM, kf(t, 0, 0, .8, -.8, 3.9, -.8, 4.3, -.1, 5.95, -.1, 6.15, -.5, 9.1, -.5, 9.8, -.1) + sh2,
                    kf(t, 0, 0, .8, -.55, 3.9, -.55, 4.3, 0),
                    kf(t, 0, .05, .8, 0, 3.9, 0, 4.3, .08, 5.95, .08, 6.15, 1.95, 9.1, 1.95, 9.8, .3) + sh);
            set(p, RLEG, kf(t, 5.95, 0, 6.15, -.12, 9.8, 0), 0, kf(t, 0, .03, 5.95, .03, 6.15, .16, 9.8, .04));
            set(p, LLEG, kf(t, 5.95, 0, 6.15, .1, 9.8, 0), 0, kf(t, 0, -.03, 5.95, -.03, 6.15, -.16, 9.8, -.04));
        });
        add("cs_starfall", "Sternenregen", "star", 7f, false, null, 0, (t, p) -> {
            float r = ease((t - 0.5f) / 1.5f);
            set(p, RARM, -0.2 - 2.3 * r, 0, 0.9 * r);
            set(p, LARM, -0.2 - 2.3 * r, 0, -0.9 * r);
            set(p, HEAD, -0.75 * r, s(t * 0.8) * 0.2 * r, 0);
            set(p, BODY, -0.12 * r, 0, 0);
        });
    }

    // ------------------------------------------------------------------ favourites for the wheel
    public static final String[] DEFAULT_WHEEL = {"wave", "dance", "dab", "heart", "gg", "floss", "tpose", "laugh"};
    public static final int WHEEL_MAX = 8;
    public static final List<String> WHEEL = new ArrayList<>(java.util.Arrays.asList(DEFAULT_WHEEL));

    public static boolean inWheel(String id) { return WHEEL.contains(id); }

    /** Adds / removes an emote from the wheel; returns false if the wheel is full. */
    public static boolean toggleWheel(String id) {
        if (WHEEL.remove(id)) { dev.lego.core.Modules.scheduleSave(); return true; }
        if (WHEEL.size() >= WHEEL_MAX) return false;
        WHEEL.add(id);
        dev.lego.core.Modules.scheduleSave();
        return true;
    }

    public static void registerPersist() {
        dev.lego.core.Modules.persist("emoteWheel", new dev.lego.core.Modules.Persist() {
            @Override public Object save() { return new ArrayList<Object>(WHEEL); }
            @SuppressWarnings("unchecked")
            @Override public void load(Object o) {
                if (!(o instanceof List)) return;
                WHEEL.clear();
                for (Object x : (List<Object>) o) if (x instanceof String && get((String) x) != null && WHEEL.size() < WHEEL_MAX) WHEEL.add((String) x);
            }
        });
    }

    // ================================================================== Paket 2: 60 neue Emotes
    static double tri(double v) { double x = v - Math.floor(v); return x < 0.5 ? x * 2 : 2 - x * 2; }

    static {
        add("x_victory", "Sieg", "trophy", 2.6f, false, null, 0, (t, p) -> { float b = s(t * 5) * 0.1f; set(p, RARM, -2.7 + b, 0, 0.6); set(p, LARM, -2.7 + b, 0, -0.6); set(p, HEAD, -0.3, 0, 0); });
        add("x_think", "Nachdenken", "info", 3.5f, false, null, 0, (t, p) -> { set(p, RARM, -1.9, 0, -0.75); set(p, LARM, -0.7, 0, 0.5); set(p, HEAD, 0.2, 0.2 + s(t * 1.5) * 0.1, 0.2); });
        add("x_yawn", "Gähnen", "moon", 3.2f, false, null, 0, (t, p) -> { double k = Math.sin(Math.min(1, t / 3.2) * Math.PI); set(p, RARM, -3.0 * k, 0, 0.3 * k); set(p, LARM, -3.0 * k, 0, -0.3 * k); set(p, HEAD, -0.4 * k, 0, 0); });
        add("x_jacks", "Hampelmann", "jump", 4f, true, null, 0, (t, p) -> { double k = tri(t * 1.6); set(p, RARM, 0, 0, 0.2 + k * 2.6); set(p, LARM, 0, 0, -0.2 - k * 2.6); set(p, RLEG, 0, 0, k * 0.4); set(p, LLEG, 0, 0, -k * 0.4); });
        add("x_chicken", "Huhn", "paw", 5f, true, null, 0, (t, p) -> { double k = Math.abs(s(t * 10)); set(p, RARM, 0.2, 0, 0.3 + k * 0.6); set(p, LARM, 0.2, 0, -0.3 - k * 0.6); set(p, HEAD, s(t * 5) * 0.3, 0, 0); });
        add("x_swim", "Kraulen", "wing", 5f, true, null, 0, (t, p) -> { set(p, RARM, -1.57 + c(t * 5) * 1.6, 0, 0.1); set(p, LARM, -1.57 - c(t * 5) * 1.6, 0, -0.1); set(p, HEAD, 0, s(t * 2.5) * 0.4, 0); });
        add("x_karate", "Karate", "bolt", 3f, false, null, 0, (t, p) -> { double k = tri(t * 1.3); set(p, RARM, -2.6 + k * 2.2, 0, 0.2); set(p, LARM, -0.9, 0, -0.4); set(p, RLEG, -0.5 * k, 0, 0); });
        add("x_sword", "Schwerthieb", "sword", 2.6f, false, null, 0, (t, p) -> { double k = Math.min(1, t / 0.6); double sw = t < 0.6 ? -2.9 * k : -2.9 + Math.min(1, (t - 0.6) / 0.25) * 2.6; set(p, RARM, sw, 0, -0.3); set(p, BODY, 0, s(t * 2) * 0.2, 0); });
        add("x_archer", "Bogenschütze", "target", 3f, false, null, 0, (t, p) -> { set(p, LARM, -1.57, 0.1, 0); set(p, RARM, -1.57, -0.9, 0); set(p, HEAD, 0, 0.6, 0); });
        add("x_conductor", "Dirigent", "music", 6f, true, null, 0, (t, p) -> { set(p, RARM, -2.0 + s(t * 5) * 0.6, 0, -0.3 + c(t * 5) * 0.4); set(p, LARM, -1.3, 0, 0.2); set(p, HEAD, -0.1, s(t * 2.5) * 0.2, 0); });
        add("x_piano", "Klavier", "music", 6f, true, null, 0, (t, p) -> { set(p, RARM, -1.0 + s(t * 11) * 0.08, -0.25 + s(t * 2) * 0.15, 0); set(p, LARM, -1.0 + s(t * 13) * 0.08, 0.25 + s(t * 2.3) * 0.15, 0); set(p, HEAD, 0.35, s(t * 1.5) * 0.15, 0); });
        add("x_violin", "Geige", "music", 6f, true, null, 0, (t, p) -> { set(p, LARM, -1.8, 0.7, 0); set(p, RARM, -1.3, -0.4 + s(t * 6) * 0.45, 0); set(p, HEAD, 0.2, 0.4, 0.3); });
        add("x_dj", "DJ", "music", 6f, true, null, 0, (t, p) -> { set(p, RARM, -2.4, 0, -0.9); set(p, LARM, -1.1, 0.4 + s(t * 10) * 0.3, 0); set(p, HEAD, 0.25 + Math.abs(s(t * 8)) * 0.2, 0, 0.2); });
        add("x_phone", "Telefonieren", "chat", 4f, true, null, 0, (t, p) -> { set(p, RARM, -2.3, 0, -1.0); set(p, HEAD, 0.1, s(t * 1.2) * 0.3, 0.2); set(p, LARM, s(t * 2) * 0.3, 0, -0.1); });
        add("x_look", "Umschauen", "eye", 4f, false, null, 0, (t, p) -> { set(p, HEAD, -0.1, s(t * 1.6) * 0.9, 0); set(p, RARM, -2.6, 0, -0.9); });
        add("x_nod", "Nicken", "check", 2f, false, null, 0, (t, p) -> set(p, HEAD, 0.25 + s(t * 10) * 0.3, 0, 0));
        add("x_no", "Kopfschütteln", "x", 2f, false, null, 0, (t, p) -> set(p, HEAD, 0.05, s(t * 12) * 0.5, 0));
        add("x_thumbs", "Daumen hoch", "star", 2.4f, false, null, 0, (t, p) -> { set(p, RARM, -1.3, 0, -0.3); set(p, HEAD, 0, 0, -0.15); });
        add("x_flex", "Muskeln", "boost", 3f, false, null, 0, (t, p) -> { double k = 0.15 * Math.abs(s(t * 4)); set(p, RARM, -1.0 - k, 0, 1.57); set(p, LARM, -1.0 - k, 0, -1.57); set(p, HEAD, -0.15, 0, 0); });
        add("x_plane", "Flugzeug", "wing", 5f, true, null, 0, (t, p) -> { set(p, RARM, 0, 0, 1.57); set(p, LARM, 0, 0, -1.57); set(p, BODY, 0, 0, s(t * 2) * 0.35); set(p, HEAD, 0, 0, s(t * 2) * 0.3); });
        add("x_heli", "Helikopter", "sky", 4f, true, null, 520, (t, p) -> { set(p, RARM, 0, 0, 1.57); set(p, LARM, 0, 0, -1.57); set(p, HEAD, -0.2, 0, 0); });
        add("x_ballet", "Ballett", "sparkle", 4f, true, null, 300, (t, p) -> { set(p, RARM, -3.0, 0, -0.35); set(p, LARM, -3.0, 0, 0.35); set(p, LLEG, -0.6, 0, -0.3); set(p, HEAD, -0.2, 0, 0); });
        add("x_hula", "Hula", "sun", 6f, true, null, 0, (t, p) -> { float k = s(t * 6); set(p, BODY, 0, 0, k * 0.15); set(p, RARM, -0.5, 0, 1.2 + k * 0.3); set(p, LARM, -0.5, 0, -1.2 + k * 0.3); set(p, RLEG, 0, 0, -k * 0.1); set(p, LLEG, 0, 0, -k * 0.1); });
        add("x_moonwalk", "Moonwalk", "moon", 5f, true, null, 0, (t, p) -> { float k = s(t * 7); set(p, RLEG, k * 0.5, 0, 0); set(p, LLEG, -k * 0.5, 0, 0); set(p, RARM, -k * 0.3, 0, 0.15); set(p, LARM, k * 0.3, 0, -0.15); set(p, HEAD, 0.1, 0, 0); });
        add("x_runningman", "Running Man", "run", 5f, true, null, 0, (t, p) -> { double k = tri(t * 2.4) * 2 - 1; set(p, RLEG, -0.9 * Math.max(0, k), 0, 0); set(p, LLEG, -0.9 * Math.max(0, -k), 0, 0); set(p, RARM, -1.0 - 0.4 * k, 0, 0.2); set(p, LARM, -1.0 + 0.4 * k, 0, -0.2); });
        add("x_disco", "Disco", "star", 6f, true, null, 0, (t, p) -> { boolean up = ((int) (t * 2.5)) % 2 == 0; set(p, RARM, up ? -2.9 : -0.2, 0, up ? 0.5 : -0.5); set(p, LARM, 0.1, 0, -0.2); set(p, BODY, 0, 0, up ? -0.08 : 0.08); set(p, HEAD, up ? -0.3 : 0.2, 0, 0); });
        add("x_wave2", "Beidhändig winken", "emote", 3f, false, null, 0, (t, p) -> { set(p, RARM, -2.75, 0, 0.3 + s(t * 9) * 0.45); set(p, LARM, -2.75, 0, -0.3 - s(t * 9) * 0.45); });
        add("x_ovation", "Standing Ovation", "trophy", 4f, true, null, 0, (t, p) -> { double k = Math.abs(s(t * 8)) * 0.4; set(p, RARM, -2.6, 0, -0.05 - k); set(p, LARM, -2.6, 0, 0.05 + k); set(p, HEAD, -0.3, 0, 0); });
        add("x_angry", "Wütend", "flame", 3f, false, "fire", 0, (t, p) -> { float sh = s(t * 25) * 0.06f; set(p, RARM, 0.2, 0, 0.25 + sh); set(p, LARM, 0.2, 0, -0.25 - sh); set(p, HEAD, 0.2, sh, 0); set(p, BODY, 0.05, 0, sh); });
        add("x_scared", "Erschrocken", "alert", 3f, false, null, 0, (t, p) -> { float sh = s(t * 30) * 0.05f; set(p, RARM, -1.9 + sh, 0, -0.4); set(p, LARM, -1.9 - sh, 0, 0.4); set(p, HEAD, -0.3, 0, 0); set(p, BODY, -0.1, 0, sh); });
        add("x_shy", "Schüchtern", "heart", 4f, true, null, 0, (t, p) -> { float k = s(t * 2); set(p, RARM, 0.4, 0, 0.25); set(p, LARM, 0.4, 0, -0.25); set(p, HEAD, 0.45, k * 0.25, k * 0.1); set(p, BODY, 0, 0, k * 0.05); });
        add("x_kiss", "Kusshand", "heart", 2.6f, false, "heart", 0, (t, p) -> { double k = Math.min(1, t / 1.2); set(p, RARM, -2.3 + k * 0.9, 0, -0.6 + k * 0.6); set(p, HEAD, -0.1, 0, 0.15); });
        add("x_pray", "Beten", "star", 4f, true, null, 0, (t, p) -> { set(p, RARM, -1.25, 0, -0.45); set(p, LARM, -1.25, 0, 0.45); set(p, HEAD, 0.45, 0, 0); });
        add("x_stretch", "Dehnen", "boost", 5f, true, null, 0, (t, p) -> { float k = s(t * 2); set(p, RARM, -2.9, 0, 0.15); set(p, LARM, -2.9, 0, -0.15); set(p, BODY, 0, 0, k * 0.3); set(p, HEAD, 0, 0, k * 0.3); });
        add("x_lookup", "Ausschau halten", "sun", 3.5f, false, null, 0, (t, p) -> { set(p, RARM, -2.5, 0, -0.8); set(p, HEAD, -0.5, s(t * 1.2) * 0.4, 0); });
        add("x_hello", "Hallo!", "emote", 3f, false, null, 0, (t, p) -> { set(p, RARM, -2.9, 0, 0.6 + s(t * 11) * 0.35); set(p, LARM, -2.9, 0, -0.6 - s(t * 11 + 1) * 0.35); set(p, HEAD, -0.2, 0, 0); });
        add("x_come", "Herkommen", "user", 3f, false, null, 0, (t, p) -> set(p, RARM, -1.4 + Math.abs(s(t * 6)) * -0.6, 0, -0.1));
        add("x_stop", "Stopp", "x", 2.4f, false, null, 0, (t, p) -> { set(p, RARM, -1.57, 0, 0); set(p, HEAD, 0.05, 0, 0); });
        add("x_mummy", "Mumie", "skull", 6f, true, null, 0, (t, p) -> { set(p, RARM, -1.57, 0.05, 0); set(p, LARM, -1.57, -0.05, 0); set(p, BODY, 0, 0, s(t * 2) * 0.1); set(p, HEAD, 0.1, 0, s(t * 2) * 0.15); });
        add("x_penguin", "Pinguin", "paw", 5f, true, null, 0, (t, p) -> { float k = s(t * 7); set(p, RARM, 0, 0, 0.35 + Math.abs(k) * 0.3); set(p, LARM, 0, 0, -0.35 - Math.abs(k) * 0.3); set(p, BODY, 0, 0, k * 0.12); set(p, HEAD, 0, 0, k * 0.15); });
        add("x_crab", "Krabbe", "paw", 5f, true, null, 0, (t, p) -> { float k = s(t * 8); set(p, RARM, -2.2, 0, 1.0 + k * 0.2); set(p, LARM, -2.2, 0, -1.0 + k * 0.2); set(p, RLEG, 0, 0, 0.3 + k * 0.1); set(p, LLEG, 0, 0, -0.3 + k * 0.1); });
        add("x_monkey", "Affe", "paw", 5f, true, null, 0, (t, p) -> { set(p, RARM, -2.9, 0, -0.3 + s(t * 9) * 0.2); set(p, LARM, -0.6, 0, -0.9 + s(t * 9) * 0.2); set(p, HEAD, 0.1, s(t * 4) * 0.3, 0); });
        add("x_fish", "Angeln", "flag", 5f, false, null, 0, (t, p) -> { double k = t < 1 ? -2.8 * t : t < 1.3 ? -2.8 + (t - 1) / 0.3 * 2.0 : -0.8 + s(t * 2) * 0.05; set(p, RARM, k, -0.1, 0); set(p, LARM, -1.0, 0.3, 0); });
        add("x_axe", "Holz hacken", "wand", 5f, true, null, 0, (t, p) -> { double k = tri(t * 1.4); set(p, RARM, -2.9 + k * 2.4, 0, 0); set(p, LARM, -2.9 + k * 2.4, 0, 0); set(p, BODY, 0.2 * k, 0, 0); });
        add("x_mine", "Abbauen", "brick", 5f, true, null, 0, (t, p) -> { double k = tri(t * 2.2); set(p, RARM, -2.5 + k * 1.8, 0.2, 0); set(p, HEAD, 0.3, 0, 0); });
        add("x_drink", "Trinken", "potion", 3f, false, null, 0, (t, p) -> { set(p, RARM, -2.0, 0, -0.7); set(p, HEAD, -0.5 + s(t * 6) * 0.05, 0, 0); });
        add("x_eat", "Essen", "food", 4f, true, null, 0, (t, p) -> { set(p, RARM, -1.6 - Math.abs(s(t * 6)) * 0.5, 0, -0.6); set(p, HEAD, 0.15 + Math.abs(s(t * 6)) * 0.1, 0, 0); });
        add("x_sneeze", "Niesen", "alert", 2.4f, false, null, 0, (t, p) -> { double k = t < 1.2 ? -0.4 * t / 1.2 : t < 1.4 ? -0.4 + (t - 1.2) / 0.2 * 1.0 : 0.6 - (t - 1.4) * 0.5; set(p, HEAD, k, 0, 0); set(p, BODY, k * 0.25, 0, 0); set(p, RARM, -1.9, 0, -0.6); });
        add("x_lap", "Ehrenrunde", "trophy", 4f, true, null, 180, (t, p) -> { set(p, RARM, -2.9, 0, 0.2 + s(t * 6) * 0.2); set(p, HEAD, -0.2, 0, 0); });
        add("x_royal", "Königlich verbeugen", "crown", 3f, false, null, 0, (t, p) -> { double k = Math.sin(Math.min(1, t / 3) * Math.PI); set(p, BODY, 0.7 * k, 0, 0); set(p, HEAD, 0.6 * k, 0, 0); set(p, RARM, -1.2 * k, 0, -0.6 * k); set(p, LARM, 0.5 * k, 0, -0.4 * k); set(p, RLEG, 0.3 * k, 0, 0); });
        add("x_horse", "Pferdetanz", "music", 6f, true, null, 0, (t, p) -> { float b = Math.abs(s(t * 8)); set(p, RARM, -1.3 - b * 0.2, 0, -0.4); set(p, LARM, -1.3 - b * 0.2, 0, 0.4); set(p, RLEG, -0.5 * b, 0, 0.1); set(p, LLEG, -0.5 * (1 - b), 0, -0.1); set(p, HEAD, 0.1 + b * 0.15, 0, 0); });
        add("x_snowball", "Weitwurf", "snow", 2.4f, false, null, 0, (t, p) -> { double k = t < 0.8 ? t / 0.8 : Math.max(0, 1 - (t - 0.8) / 0.3); set(p, RARM, -2.6 * k + (t > 0.8 ? -0.9 : 0), 0, -0.2); set(p, BODY, 0, -0.3 * k, 0); });
        add("x_peek", "Gucken", "eye", 3f, false, null, 0, (t, p) -> { set(p, BODY, 0, 0, s(Math.min(1, t / 0.6) * Math.PI / 2) * 0.35); set(p, HEAD, 0, 0, 0.35); set(p, RARM, -2.4, 0, -0.9); });
        add("x_wipe", "Schweiß abwischen", "sun", 2.6f, false, null, 0, (t, p) -> { set(p, RARM, -2.6, 0.3 + s(t * 4) * 0.5, -0.8); set(p, HEAD, -0.15, 0, 0); });
        add("x_tired", "Erschöpft", "hourglass", 4f, true, null, 0, (t, p) -> { float br = s(t * 4) * 0.06f; set(p, BODY, 0.45 + br, 0, 0); set(p, HEAD, 0.3, 0, 0); set(p, RARM, -0.6, 0, 0.1); set(p, LARM, -0.6, 0, -0.1); });
        add("x_shuffle", "Shuffle", "music", 6f, true, null, 0, (t, p) -> { float k = s(t * 10); set(p, RLEG, -Math.max(0, k) * 0.7, 0, 0.15); set(p, LLEG, -Math.max(0, -k) * 0.7, 0, -0.15); set(p, RARM, -0.4, 0, 0.3 + Math.abs(k) * 0.2); set(p, LARM, -0.4, 0, -0.3 - Math.abs(k) * 0.2); set(p, HEAD, 0.1 + Math.abs(k) * 0.1, 0, 0); });
        add("x_noodle", "Wackelarme", "wand", 5f, true, null, 0, (t, p) -> { set(p, RARM, -1.2 + s(t * 7) * 0.9, 0, 1.0 + c(t * 9) * 0.5); set(p, LARM, -1.2 + c(t * 7) * 0.9, 0, -1.0 - s(t * 9) * 0.5); set(p, HEAD, 0, s(t * 3) * 0.3, s(t * 5) * 0.2); });
        add("x_rowing", "Rudern", "flag", 5f, true, null, 0, (t, p) -> { double k = tri(t * 0.9); set(p, RARM, -1.57 + k * 1.0, 0, -0.15); set(p, LARM, -1.57 + k * 1.0, 0, 0.15); set(p, BODY, 0.35 * (1 - k), 0, 0); });
        add("x_juggle", "Jonglieren", "sparkle", 5f, true, null, 0, (t, p) -> { set(p, RARM, -1.3 - Math.max(0, s(t * 8)) * 0.6, 0, -0.1); set(p, LARM, -1.3 - Math.max(0, -s(t * 8)) * 0.6, 0, 0.1); set(p, HEAD, -0.4, s(t * 4) * 0.2, 0); });
        add("x_magic", "Zaubern", "wand", 3f, false, null, 0, (t, p) -> { double k = Math.min(1, t / 1.2); set(p, RARM, -1.5 - k * 1.4, 0, -0.2 + s(t * 14) * 0.15); set(p, LARM, -1.0, 0, 0.4); set(p, HEAD, -0.2 * k, 0, 0); });        add("x_spin", "Drehen", "refresh", 3f, false, null, 600, (t, p) -> { set(p, RARM, -0.6, 0, 1.2); set(p, LARM, -0.6, 0, -1.2); set(p, HEAD, -0.25, 0, 0); });
    }
    // ================================================================== Paket 3: 40 weitere Emotes
    static {
        add("y_robo2", "Roboterarme", "chip", 5f, true, null, 0, (t, p) -> { int st = (int) (t * 4) % 4; set(p, RARM, st < 2 ? -1.57 : 0, 0, st % 2 == 0 ? 0 : 1.57); set(p, LARM, st >= 2 ? -1.57 : 0, 0, st % 2 == 1 ? 0 : -1.57); set(p, HEAD, 0, st * 0.25 - 0.4, 0); });
        add("y_wave_low", "Leise winken", "emote", 2.4f, false, null, 0, (t, p) -> set(p, RARM, -1.3, 0, 0.2 + s(t * 10) * 0.25));
        add("y_bye", "Tschüss", "emote", 3f, false, null, 0, (t, p) -> { set(p, RARM, -2.8, 0, 0.4 + s(t * 7) * 0.5); set(p, HEAD, 0, 0.3, 0); });
        add("y_clapslow", "Langsamer Applaus", "sparkle", 4f, true, null, 0, (t, p) -> { double k = Math.max(0, s(t * 2.6)) * 0.45; set(p, RARM, -1.35, 0, -0.05 - k); set(p, LARM, -1.35, 0, 0.05 + k); set(p, HEAD, 0.1, 0, 0.15); });
        add("y_hands_hips", "Hände in die Hüften", "user", 3f, false, null, 0, (t, p) -> { set(p, RARM, -0.2, 0, 0.75); set(p, LARM, -0.2, 0, -0.75); set(p, HEAD, -0.1, 0, 0); });
        add("y_arms_cross", "Arme verschränken", "user", 3f, false, null, 0, (t, p) -> { set(p, RARM, -1.25, 0, -0.75); set(p, LARM, -1.15, 0, 0.75); set(p, HEAD, 0.05, -0.2, 0); });
        add("y_tap", "Ungeduldig tippen", "clock", 4f, true, null, 0, (t, p) -> { set(p, RARM, -0.2, 0, 0.75); set(p, LARM, -0.2, 0, -0.75); set(p, RLEG, -Math.max(0, s(t * 12)) * 0.35, 0, 0); });
        add("y_scratch", "Kopf kratzen", "info", 3f, false, null, 0, (t, p) -> { set(p, RARM, -2.6, 0, -0.9 + s(t * 14) * 0.1); set(p, HEAD, 0.1, 0, 0.25); });
        add("y_peace", "Peace", "star", 2.6f, false, null, 0, (t, p) -> { set(p, RARM, -2.4, 0, 0.15); set(p, HEAD, 0, 0, -0.2); });
        add("y_ok", "Alles okay", "check", 2.4f, false, null, 0, (t, p) -> set(p, RARM, -1.5, -0.3, -0.2));
        add("y_shush", "Psst", "mute", 2.6f, false, null, 0, (t, p) -> { set(p, RARM, -2.1, 0, -0.55); set(p, HEAD, 0.15, 0, 0); });
        add("y_listen", "Lauschen", "sound", 3f, false, null, 0, (t, p) -> { set(p, RARM, -2.6, 0.3, -1.1); set(p, HEAD, 0.05, 0, 0.35); });
        add("y_celebrate", "Feiern", "trophy", 5f, true, null, 0, (t, p) -> { double b = Math.abs(s(t * 8)); set(p, RARM, -2.6 - b * 0.3, 0, 0.6); set(p, LARM, -2.6 - b * 0.3, 0, -0.6); set(p, RLEG, -0.3 * b, 0, 0); set(p, HEAD, -0.3, 0, 0); });
        add("y_fistpump", "Faust hoch", "trophy", 2.6f, false, null, 0, (t, p) -> { double k = Math.abs(s(t * 7)); set(p, RARM, -1.6 - k * 1.2, 0, 0.1); set(p, HEAD, -0.2, 0, 0); });
        add("y_sad", "Traurig", "rain", 4f, true, null, 0, (t, p) -> { set(p, HEAD, 0.7, 0, s(t * 1.2) * 0.1); set(p, RARM, 0.15, 0, 0.05); set(p, LARM, 0.15, 0, -0.05); set(p, BODY, 0.15, 0, 0); });
        add("y_sigh", "Seufzen", "cloud", 2.6f, false, null, 0, (t, p) -> { double k = Math.sin(Math.min(1, t / 2.6) * Math.PI); set(p, HEAD, -0.3 * k + 0.4 * (1 - k), 0, 0); set(p, RARM, 0.2 * k, 0, 0.3 * k); set(p, LARM, 0.2 * k, 0, -0.3 * k); });
        add("y_excited", "Aufgeregt", "bolt", 4f, true, null, 0, (t, p) -> { float k = s(t * 16); set(p, RARM, -1.8 + k * 0.3, 0, 0.4); set(p, LARM, -1.8 - k * 0.3, 0, -0.4); set(p, HEAD, -0.15, k * 0.15, 0); });
        add("y_bored", "Gelangweilt", "hourglass", 6f, true, null, 0, (t, p) -> { set(p, HEAD, 0.35, s(t * 0.8) * 0.5, 0.2); set(p, RARM, 0.1, 0, 0.1); set(p, LARM, 0.1, 0, -0.1); });
        add("y_confused", "Verwirrt", "info", 3f, false, null, 0, (t, p) -> { set(p, HEAD, 0.05, s(t * 3) * 0.4, 0.35); set(p, RARM, -0.6, 0.8, 1.0); set(p, LARM, -0.6, -0.8, -1.0); });
        add("y_surprise", "Überrascht", "alert", 2.4f, false, null, 0, (t, p) -> { double k = Math.min(1, t * 4); set(p, RARM, -2.3 * k, 0, 0.6 * k); set(p, LARM, -2.3 * k, 0, -0.6 * k); set(p, HEAD, -0.35 * k, 0, 0); });
        add("y_love", "Verliebt", "heart", 4f, true, "heart", 0, (t, p) -> { float k = s(t * 3); set(p, RARM, -1.7, 0, -0.6); set(p, LARM, -1.7, 0, 0.6); set(p, HEAD, -0.1, 0, k * 0.25); set(p, BODY, 0, 0, k * 0.08); });
        add("y_wink", "Zwinkern", "eye", 2.2f, false, null, 0, (t, p) -> { set(p, HEAD, 0, 0.2, 0.3); set(p, RARM, -1.6, -0.3, -0.2); });
        add("y_hug", "Umarmen", "heart", 3f, false, "heart", 0, (t, p) -> { double k = Math.min(1, t * 2); set(p, RARM, -1.5, -0.7 * k, -0.2); set(p, LARM, -1.5, 0.7 * k, 0.2); set(p, HEAD, 0.1, 0, 0.15); });
        add("y_highfive", "High Five", "emote", 2.4f, false, null, 0, (t, p) -> { double k = t < 1 ? t : 1; set(p, RARM, -2.6 * k, -0.2, 0.2); });
        add("y_fistbump", "Fist Bump", "hit", 2.4f, false, null, 0, (t, p) -> { double k = t < 0.6 ? t / 0.6 : 1; set(p, RARM, -1.57 * k, -0.1, 0); });
        add("y_signal", "Winken (Notruf)", "alert", 4f, true, null, 0, (t, p) -> { set(p, RARM, -2.9, 0, 0.4 + s(t * 6) * 0.6); set(p, LARM, -2.9, 0, -0.4 - s(t * 6) * 0.6); });
        add("y_salute2", "Militärgruß", "shield", 3f, false, null, 0, (t, p) -> { set(p, RARM, -2.2, 0.3, -0.85); set(p, HEAD, -0.05, 0, 0); set(p, LARM, 0, 0, -0.05); });
        add("y_kungfu", "Kung-Fu", "bolt", 4f, true, null, 0, (t, p) -> { boolean a = ((int) (t * 2)) % 2 == 0; set(p, RARM, a ? -1.57 : -0.5, 0, a ? 0 : 0.9); set(p, LARM, a ? -0.5 : -1.57, 0, a ? -0.9 : 0); set(p, RLEG, a ? -0.9 : 0, 0, 0.1); });
        add("y_ninja", "Ninja-Pose", "sword", 3f, false, null, 0, (t, p) -> { set(p, RARM, -2.0, 0.5, -0.6); set(p, LARM, -1.0, -0.3, 0.4); set(p, BODY, 0.2, 0.4, 0); set(p, HEAD, 0, -0.4, 0); set(p, LLEG, -0.5, 0, -0.2); });
        add("y_wizard", "Zauberspruch", "wand", 4f, true, null, 0, (t, p) -> { set(p, RARM, -2.6 + s(t * 5) * 0.3, 0, -0.3 + c(t * 5) * 0.3); set(p, LARM, -2.6 - s(t * 5) * 0.3, 0, 0.3 - c(t * 5) * 0.3); set(p, HEAD, -0.3, 0, 0); });
        add("y_lasso", "Lasso", "ring", 4f, true, null, 0, (t, p) -> { set(p, RARM, -2.6, s(t * 9) * 0.6, c(t * 9) * 0.5); set(p, LARM, -0.4, 0, -0.3); set(p, HEAD, -0.2, 0, 0); });
        add("y_skate", "Skaten", "speed", 5f, true, null, 0, (t, p) -> { float k = s(t * 2); set(p, BODY, 0.1, 0.3, k * 0.1); set(p, RARM, -0.4, 0, 1.0 + k * 0.2); set(p, LARM, -0.4, 0, -1.0 + k * 0.2); set(p, RLEG, -0.3, 0.3, 0); set(p, LLEG, 0.2, 0.3, 0); });
        add("y_surf", "Surfen", "rain", 5f, true, null, 0, (t, p) -> { float k = s(t * 2.4); set(p, BODY, 0.15, -0.5, k * 0.12); set(p, RARM, 0, 0, 1.3 + k * 0.2); set(p, LARM, 0, 0, -1.3 + k * 0.2); set(p, RLEG, -0.4, -0.5, 0); set(p, LLEG, 0.3, -0.5, 0); });
        add("y_ski", "Skifahren", "snow", 5f, true, null, 0, (t, p) -> { float k = s(t * 3); set(p, BODY, 0.35, k * 0.3, 0); set(p, RARM, -0.6, 0, 0.3); set(p, LARM, -0.6, 0, -0.3); set(p, RLEG, -0.5, k * 0.3, 0); set(p, LLEG, -0.5, k * 0.3, 0); set(p, HEAD, -0.3, 0, 0); });
        add("y_bike", "Radfahren", "speed", 5f, true, null, 0, (t, p) -> { set(p, RLEG, -1.0 + s(t * 8) * 0.6, 0, 0); set(p, LLEG, -1.0 - s(t * 8) * 0.6, 0, 0); set(p, RARM, -1.3, 0, 0); set(p, LARM, -1.3, 0, 0); set(p, BODY, 0.3, 0, 0); });
        add("y_climb", "Klettern", "jump", 5f, true, null, 0, (t, p) -> { float k = s(t * 5); set(p, RARM, -2.7 + k * 0.5, 0, 0.1); set(p, LARM, -2.7 - k * 0.5, 0, -0.1); set(p, RLEG, -0.6 * Math.max(0, k), 0, 0); set(p, LLEG, -0.6 * Math.max(0, -k), 0, 0); });
        add("y_stomp", "Stampfen", "hit", 4f, true, null, 0, (t, p) -> { double k = Math.max(0, s(t * 6)); set(p, RLEG, -1.0 * k, 0, 0); set(p, RARM, -0.5, 0, 0.6); set(p, LARM, -0.5, 0, -0.6); set(p, HEAD, 0.2 * k, 0, 0); });
        add("y_kick", "Kicken", "hit", 2.4f, false, null, 0, (t, p) -> { double k = t < 0.5 ? t / 0.5 * 0.6 : t < 0.8 ? 0.6 - (t - 0.5) / 0.3 * 2.0 : -1.4 + (t - 0.8) * 0.8; set(p, RLEG, Math.min(0.6, k), 0, 0); set(p, LARM, -0.8, 0, -0.4); set(p, RARM, 0.4, 0, 0.3); });
        add("y_bow3", "Knicks", "crown", 2.6f, false, null, 0, (t, p) -> { double k = Math.sin(Math.min(1, t / 2.6) * Math.PI); set(p, RLEG, 0.5 * k, 0, 0); set(p, LLEG, -0.3 * k, 0, 0); set(p, RARM, 0, 0, 0.45 * k); set(p, LARM, 0, 0, -0.45 * k); set(p, HEAD, 0.35 * k, 0, 0); });
        add("y_67", "67", "star", 5f, true, null, 0, (t, p) -> { float k = s(t * 7); set(p, RARM, -1.25 + k * 0.45, 0.15, -0.15); set(p, LARM, -1.25 - k * 0.45, -0.15, 0.15); set(p, HEAD, 0.05, s(t * 3.5) * 0.15, k * 0.05); set(p, BODY, 0, 0, k * 0.04); });
        add("y_freeze", "Einfrieren", "snow", 3f, false, null, 0, (t, p) -> { float sh = s(t * 40) * 0.03f; set(p, RARM, -1.0 + sh, 0, -0.7); set(p, LARM, -1.0 - sh, 0, 0.7); set(p, HEAD, 0.25, sh, 0); set(p, BODY, 0.05, 0, sh); });
    }
    // ================================================================== Paket 4: Keyframe-Emotes
    static { EmotePack.addAll(); }
}
