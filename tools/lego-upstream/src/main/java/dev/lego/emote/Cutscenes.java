package dev.lego.emote;

import dev.lego.core.Mc;
import dev.lego.ui.Ease;
import dev.lego.visual.R3;
import net.minecraft.class_2960;
import net.minecraft.class_5498;
import net.minecraft.class_746;

/**
 * Cinematic emotes: the camera flies around you (no HUD, small title) while world effects and timed sound cues play.
 * Only visible for you. Camera is applied in CameraMixin via {@link #camera(float, double[])}.
 */
public final class Cutscenes {
    private Cutscenes() { }

    private static String id;
    private static long start;
    private static float dur;
    private static double ox, oy, oz;   // player position at the start
    private static float baseYaw;
    private static class_5498 savedPerspective;

    public static boolean active() { return id != null; }
    public static String current() { return id; }

    /** Seconds since start. */
    public static float time() { return id == null ? 0 : (System.currentTimeMillis() - start) / 1000f; }

    /** Fade 0..1 of the cutscene (in / out, used for the title). */
    public static float bars() {
        if (id == null) return 0;
        float t = time();
        return Ease.outCubic(Math.min(1, t / 0.5f)) * Math.min(1, Math.max(0, (dur - t) / 0.5f));
    }

    public static String title() {
        EmoteLib.Emote e = id == null ? null : EmoteLib.get(id);
        return e == null ? "" : e.name;
    }

    static void begin(EmoteLib.Emote e) {
        class_746 p = Mc.player();
        if (p == null) return;
        end();
        id = e.id;
        dur = e.duration;
        start = System.currentTimeMillis();
        ox = Mc.x(p); oy = Mc.y(p); oz = Mc.z(p);
        baseYaw = Mc.bodyYaw(p);
        savedPerspective = Mc.perspective();
        Mc.perspective(class_5498.field_26665);   // third person: your player is rendered
        cue = 0;
        sounds();
    }

    // ------------------------------------------------------------------ sound cues
    /** One timed sound: fired once when the cutscene clock passes {@code t}. */
    private record Cue(float t, String id, float pitch, float vol) { }

    private static Cue c(double t, String id, double pitch, double vol) { return new Cue((float) t, "minecraft:" + id, (float) pitch, (float) vol); }

    private static final String BELL = "block.note_block.bell", CHIME = "block.amethyst_block.chime", TWINKLE = "entity.firework_rocket.twinkle",
            BEAT = "entity.warden.heartbeat", THUNDER = "entity.lightning_bolt.thunder", LAUGH = "entity.witch.celebrate";
    /** Jingle-bells pitches (note block bell, relative to F#). */
    private static final double NE = 0.891, NG = 1.059, NC = 0.707, ND = 0.794;

    private static final java.util.Map<String, Cue[]> CUES = new java.util.HashMap<>();
    static {
        CUES.put("cs_darkaura", new Cue[]{
                c(0.0, "ambient.cave", 0.8, 0.6), c(0.1, "entity.bat.takeoff", 0.8, 0.5), c(0.9, "entity.bat.takeoff", 1.0, 0.35),
                c(1.7, BEAT, 1.0, 0.9), c(2.5, BEAT, 1.0, 0.9), c(3.3, BEAT, 1.05, 0.95), c(3.9, "block.bell.resonate", 0.6, 0.55), c(3.95, BEAT, 1.1, 1.0), c(4.6, BEAT, 1.15, 1.0),
                c(5.2, "entity.player.attack.sweep", 0.7, 1.0), c(5.22, THUNDER, 1.4, 0.45), c(5.29, "entity.player.attack.sweep", 0.85, 1.0),
                c(5.3, "entity.bat.takeoff", 0.7, 0.6), c(5.38, "entity.player.attack.sweep", 1.0, 1.0), c(6.1, "entity.bat.takeoff", 0.9, 0.35)});
        CUES.put("cs_xmas", new Cue[]{
                c(0.5, TWINKLE, 1.2, 0.35),
                c(0.6, BELL, NE, 0.7), c(0.85, BELL, NE, 0.7), c(1.1, BELL, NE, 0.7), c(1.6, BELL, NE, 0.7), c(1.85, BELL, NE, 0.7), c(2.1, BELL, NE, 0.7),
                c(2.2, CHIME, 1.2, 0.5), c(2.5, CHIME, 1.4, 0.5),
                c(2.6, BELL, NE, 0.7), c(2.85, BELL, NG, 0.7), c(3.1, BELL, NC, 0.7), c(3.1, CHIME, 1.5, 0.8), c(3.12, TWINKLE, 1.0, 0.7), c(3.35, BELL, ND, 0.7), c(3.6, BELL, NE, 0.8),
                c(3.5, CHIME, 1.0, 0.45), c(3.68, CHIME, 1.12, 0.45), c(3.86, CHIME, 1.26, 0.45), c(4.04, CHIME, 1.41, 0.45), c(4.22, CHIME, 1.5, 0.45),
                c(4.4, TWINKLE, 1.3, 0.5), c(4.4, BELL, NE * 2, 0.35), c(4.6, BELL, ND * 2, 0.35), c(4.8, BELL, NE * 2, 0.35), c(5.3, TWINKLE, 1.1, 0.45), c(6.2, TWINKLE, 1.4, 0.4)});
        CUES.put("cs_landing", new Cue[]{c(0.05, "item.elytra.flying", 1.3, 0.35), c(1.0, "entity.generic.explode", 1.3, 0.55), c(1.02, "block.anvil.land", 0.5, 0.45), c(3.7, "entity.player.levelup", 1.2, 0.4)});
        CUES.put("cs_victory", new Cue[]{c(0.1, "ui.toast.challenge_complete", 1.0, 0.5),
                c(0.4, "entity.firework_rocket.launch", 1.0, 0.6), c(0.9, "entity.firework_rocket.large_blast", 1.0, 0.7), c(1.3, TWINKLE, 1.0, 0.5),
                c(1.25, "entity.firework_rocket.launch", 1.1, 0.6), c(1.75, "entity.firework_rocket.blast", 1.0, 0.7), c(2.15, TWINKLE, 1.2, 0.5),
                c(2.1, "entity.firework_rocket.launch", 0.95, 0.6), c(2.6, "entity.firework_rocket.large_blast", 0.9, 0.7), c(3.0, TWINKLE, 0.9, 0.5),
                c(2.95, "entity.firework_rocket.launch", 1.05, 0.6), c(3.45, "entity.firework_rocket.blast", 1.1, 0.7), c(3.85, TWINKLE, 1.3, 0.5),
                c(3.8, "entity.firework_rocket.launch", 1.0, 0.6), c(4.3, "entity.firework_rocket.large_blast", 1.0, 0.7), c(4.7, TWINKLE, 1.1, 0.5),
                c(4.65, "entity.firework_rocket.launch", 1.1, 0.6), c(5.15, "entity.firework_rocket.blast", 1.0, 0.7), c(5.55, TWINKLE, 1.2, 0.5)});
        CUES.put("cs_portal", new Cue[]{c(0.2, "block.portal.trigger", 1.4, 0.3), c(2.5, "block.end_portal.spawn", 1.0, 0.45), c(2.5, "block.beacon.activate", 0.8, 0.6)});
        CUES.put("cs_power", new Cue[]{c(0.2, "block.beacon.power_select", 0.7, 0.6), c(2.0, "block.beacon.ambient", 1.2, 0.7), c(3.2, "entity.creeper.primed", 0.6, 0.5),
                c(3.8, "entity.generic.explode", 0.9, 0.7), c(3.8, "entity.lightning_bolt.impact", 1.0, 0.6)});
        CUES.put("cs_selfie", new Cue[]{c(0.3, "block.note_block.chime", 1.6, 0.35), c(3.75, "ui.button.click", 2.0, 0.6), c(3.8, "block.dispenser.dispense", 1.8, 0.35)});
        CUES.put("cs_starfall", new Cue[]{c(0.5, "block.beacon.activate", 1.4, 0.35), c(1.0, CHIME, 0.9, 0.5), c(2.1, CHIME, 1.2, 0.5), c(3.2, CHIME, 1.0, 0.5),
                c(4.3, CHIME, 1.35, 0.5), c(5.2, TWINKLE, 1.2, 0.4), c(5.6, CHIME, 1.5, 0.5)});
        CUES.put("cs_evillaugh", new Cue[]{
                c(0.0, "ambient.cave", 0.7, 0.7), c(0.05, "entity.wither.ambient", 0.5, 0.15),
                c(0.4, BEAT, 0.9, 1.0), c(1.2, BEAT, 0.9, 1.0), c(1.3, "entity.witch.ambient", 0.55, 0.6), c(2.0, BEAT, 0.95, 1.0),
                c(2.6, THUNDER, 1.2, 0.35), c(2.65, LAUGH, 0.6, 0.9), c(3.4, BEAT, 1.0, 1.0),
                c(4.4, "block.bell.resonate", 0.5, 0.8), c(4.45, BEAT, 1.0, 1.0), c(4.7, "entity.wither.ambient", 0.45, 0.22), c(5.1, BEAT, 1.05, 1.0), c(5.7, BEAT, 1.1, 1.0),
                c(6.0, THUNDER, 0.9, 0.8), c(6.0, "entity.lightning_bolt.impact", 0.8, 0.6), c(6.05, LAUGH, 0.7, 1.0), c(6.1, "item.book.page_turn", 0.7, 0.8),
                c(6.45, "item.book.page_turn", 0.9, 0.7), c(6.9, LAUGH, 0.8, 1.0), c(7.1, THUNDER, 1.3, 0.3), c(7.75, LAUGH, 0.92, 1.0),
                c(8.3, THUNDER, 1.1, 0.4), c(8.3, "entity.wither.ambient", 0.55, 0.2), c(9.3, "block.bell.resonate", 0.4, 0.5)});
        for (Cue[] cs : CUES.values()) java.util.Arrays.sort(cs, (a, b) -> Float.compare(a.t, b.t));
    }

    private static int cue;

    /** Fires every sound cue whose time has passed (each once). Called every tick and every frame. */
    public static void sounds() {
        if (id == null) return;
        Cue[] cs = CUES.get(id);
        if (cs == null) return;
        float t = time();
        while (cue < cs.length && cs[cue].t <= t) {
            Cue q = cs[cue++];
            if (t - q.t < 0.6f) dev.lego.ui.Sound.play(q.id, q.pitch, q.vol);   // skip stale cues after a lag spike
        }
    }

    static void end() {
        if (id == null) return;
        id = null;
        try {
            if (savedPerspective != null) Mc.perspective(savedPerspective);
        } catch (Throwable ignored) { }
        savedPerspective = null;
    }

    // ------------------------------------------------------------------ camera
    /**
     * Camera for this frame.
     * @param out x, y, z, yaw, pitch (degrees, Minecraft convention)
     * @return false if no cutscene is running
     */
    public static boolean camera(float td, double[] out) {
        if (id == null) return false;
        class_746 p = Mc.player();
        if (p == null) return false;
        double[] pos = Mc.lerpPos(p, td);
        sounds();
        if ("cs_darkaura".equals(id)) return darkAuraCam(pos, time(), out);
        if ("cs_evillaugh".equals(id)) return evilCam(pos, time(), out);
        double tx = pos[0], ty = pos[1] + 1.1, tz = pos[2];
        float t = time();
        double th = Math.toRadians(baseYaw);
        double fx = -Math.sin(th), fz = Math.cos(th);       // player's forward
        double rx = -Math.cos(th), rz = -Math.sin(th);      // player's right
        double dist, ang, h;                                 // orbit: distance, angle (0 = in front), height
        switch (id) {
            case "cs_landing": {
                // from high above behind -> sweep down to a low front view -> slow push
                float a = Ease.inOutCubic(Math.min(1, t / 1.6f));
                dist = 9 - 5.5 * a;
                ang = Math.toRadians(160 - 150 * a);
                h = 7 * (1 - a) - 0.6 * a + 0.3 * Math.max(0, t - 3.5);
                if (t > 0.9 && t < 1.3) h += Math.sin((t - 0.9) * 60) * 0.06 * (1.3 - t) / 0.4;    // impact shake
                break;
            }
            case "cs_victory":
                dist = 3.6; ang = t / dur * Math.PI * 2 + 0.3; h = 0.6 + Math.sin(t * 0.9) * 0.4;
                break;
            case "cs_portal": {
                float a = Ease.inOutCubic(Math.min(1, t / dur));
                dist = 8 - 4.5 * a; ang = Math.toRadians(20 - 40 * a); h = -0.7 + 0.4 * a;
                break;
            }
            case "cs_power": {
                float a = Math.min(1, t / 3.8f);
                dist = 5 - 2 * a; ang = t * 0.9; h = 0.2;
                if (t > 3.8 && t < 4.4) { dist += 2.5 * Ease.outCubic((t - 3.8f) / 0.6f); }
                else if (t >= 4.4) dist += 2.5;
                double shake = t < 3.8 ? a * 0.05 : Math.max(0, 0.2 - (t - 3.8) * 0.2);
                h += Math.sin(t * 47) * shake;
                ang += Math.cos(t * 53) * shake * 0.3;
                break;
            }
            case "cs_xmas": {
                // close on the face -> pull back and rise while the tree grows behind you -> orbit wide
                float a = Ease.inOutCubic(Math.min(1, t / 3.0f)), b = Ease.inOutCubic(Math.max(0, Math.min(1, (t - 3.2f) / 3.5f)));
                dist = 2.2 + 3.8 * a + 1.5 * b; ang = Math.toRadians(10 + 25 * a + 70 * b); h = 0.1 + 1.2 * a + 1.4 * b;
                ty += 0.8 * a;
                if (t > 3.2 && t < 3.6) h += Math.sin((t - 3.2) * 70) * 0.04;
                break;
            }
            case "cs_selfie":
                dist = 1.6; ang = Math.toRadians(-18); h = 0.35 + Math.sin(t * 1.5) * 0.03;
                break;
            case "cs_starfall": {
                float up = Ease.inOutCubic(Math.min(1, t / 2.5f)), back = Ease.inOutCubic(Math.max(0, (t - 4.8f) / 1.6f));
                dist = 3.2 + 3 * up - 2 * back; ang = Math.toRadians(30 + t * 8); h = -0.4 - 0.4 * up;
                ty += 6 * up * (1 - back);                    // look up at the sky
                break;
            }
            default:
                if (id.startsWith("cs_gen_")) {
                    float cycle = t % 6.8f;
                    float cut = (float)Math.floor(cycle / 1.7f);
                    double[] ds = {3.0, 5.5, 2.2, 7.0};
                    double[] hs = {0.3, 2.2, -0.5, 1.1};
                    dist = ds[(int)cut];
                    ang = Math.toRadians(-35 + cycle * 70 + cut * 85);
                    h = hs[(int)cut] + Math.sin(t * 3.0) * 0.08;
                    break;
                }
                return false;
        }
        double cx = tx + (fx * Math.cos(ang) + rx * Math.sin(ang)) * dist;
        double cz = tz + (fz * Math.cos(ang) + rz * Math.sin(ang)) * dist;
        double cy = pos[1] + 1.3 + h;
        double dx = tx - cx, dy = ty - cy, dz = tz - cz;
        out[0] = cx; out[1] = cy; out[2] = cz;
        out[3] = Math.toDegrees(Math.atan2(-dx, dz));
        out[4] = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        return true;
    }

    // ------------------------------------------------------------------ effects (inside R3 begin/end)
    private static class_2960 core, glow, star;

    public static void renderFx(float td) {
        if (id == null) return;
        class_746 p = Mc.player();
        if (p == null) return;
        if (core == null) {
            core = tex("core"); glow = tex("glow"); star = tex("glitter");
        }
        double[] pos = Mc.lerpPos(p, td);
        double x = pos[0], y = pos[1], z = pos[2];
        float t = time();
        switch (id) {
            case "cs_landing": {
                // falling streak + shockwave + dust
                if (t < 1.0) {
                    double fy = y + 10 * (1 - t / 1.0);
                    R3.line(x, fy + 3, z, x, fy, z, 0.35, R3.argb(0x9FD8FF, 0), R3.argb(0xFFFFFF, 0.8));
                }
                double s = t - 1.0;
                if (s > 0 && s < 1.6) {
                    double r = 0.4 + s * 5, a = 1 - s / 1.6;
                    R3.ring(x, y + 0.05, z, r * 0.85, r, 48, R3.argb(0xBFE6FF, 0), R3.argb(0xFFFFFF, 0.8 * a), 0);
                    R3.ring(x, y + 0.04, z, 0, r * 0.85, 48, R3.argb(0x7FB8FF, 0.25 * a), R3.argb(0x7FB8FF, 0), 0);
                    for (int i = 0; i < 40; i++) {
                        double an = i * 2.399, sp = 1.5 + (i % 7) * 0.4;
                        double dx = Math.cos(an) * sp * s, dz = Math.sin(an) * sp * s, dy = 1.8 * s - 2.2 * s * s;
                        if (dy < 0) dy = 0;
                        R3.billboard(glow, x + dx, y + 0.1 + dy, z + dz, 0.35, R3.argb(0xC8B89A, 0.55 * a));
                    }
                }
                break;
            }
            case "cs_victory":
                for (int k = 0; k < 6; k++) fireworks(x, y, z, t - 0.4 - k * 0.85, k);
                break;
            case "cs_portal": {
                double a = Math.min(1, t / 1.5) * Math.min(1, (dur - t) / 0.6);
                double rise = Math.min(1, t / 3.0);
                for (int ring = 0; ring < 3; ring++) {
                    double ry = y + 0.05 + ring * 0.8 * rise;
                    R3.ring(x, ry, z, 1.2 + ring * 0.2, 1.4 + ring * 0.2, 48, R3.argb(0xC86BFF, 0.7 * a), R3.argb(0x5B2BFF, 0.2 * a), t * (1.5 + ring));
                }
                for (int i = 0; i < 60; i++) {
                    double an = i * 0.61 + t * 2.2, r = 1.3 + Math.sin(i * 1.7 + t) * 0.2, hy = ((i * 0.37 + t * 0.8) % 3.0);
                    R3.glowBillboard(core, x + Math.cos(an) * r, y + hy, z + Math.sin(an) * r, 0.12, R3.argb(0xE0A0FF, 0.9 * a));
                }
                if (t > 2.5) {
                    double b = Math.min(1, (t - 2.5) / 0.6) * a;
                    R3.line(x, y, z, x, y + 60, z, 1.6 * b, R3.argb(0xD9B3FF, 0.6 * b), R3.argb(0x7B3BFF, 0));
                    R3.line(x, y, z, x, y + 60, z, 0.5 * b, R3.argb(0xFFFFFF, 0.9 * b), R3.argb(0xFFFFFF, 0));
                }
                break;
            }
            case "cs_power": {
                double charge = Math.min(1, t / 3.8);
                boolean burst = t > 3.8;
                for (int i = 0; i < 70; i++) {
                    double an = i * 2.399 + t * 3, life = ((i * 0.173 + t * 1.6) % 1.0);
                    double r = 0.55 + 0.25 * Math.sin(i + t * 4);
                    double hy = life * 2.4;
                    double a = (1 - life) * charge * (burst ? Math.max(0, 1 - (t - 3.8) / 1.5) : 1);
                    int c = i % 3 == 0 ? 0xFFF3A0 : 0xFFB02E;
                    R3.glowBillboard(glow, x + Math.cos(an) * r, y + hy, z + Math.sin(an) * r, 0.5 * (1 - life * 0.5), R3.argb(c, 0.55 * a));
                }
                if (burst) {
                    double s = t - 3.8, a = Math.max(0, 1 - s / 1.2);
                    R3.ring(x, y + 1, z, s * 8, s * 8 + 0.6, 64, R3.argb(0xFFE08A, 0), R3.argb(0xFFFFFF, a), 0);
                    R3.glowBillboard(glow, x, y + 1, z, 2 + s * 12, R3.argb(0xFFD070, 0.6 * a));
                }
                // little lightning arcs
                if (!burst && ((int) (t * 7)) % 3 == 0) {
                    double an = Math.floor(t * 7) * 1.3;
                    double x1 = x + Math.cos(an) * 0.9, z1 = z + Math.sin(an) * 0.9;
                    R3.line(x1, y + 2.2, z1, x + Math.cos(an + 0.4) * 0.6, y + 0.8, z + Math.sin(an + 0.4) * 0.6, 0.06, R3.argb(0xFFFFFF, 0.9), R3.argb(0xFFE070, 0.9));
                }
                break;
            }
            case "cs_selfie":
                if (t > dur - 0.9 && t < dur - 0.5) {
                    double a = 1 - Math.abs((t - (dur - 0.7)) / 0.2);
                    R3.glowBillboard(glow, x, y + 1.4, z, 6, R3.argb(0xFFFFFF, Math.max(0, a)));
                }
                for (int i = 0; i < 6; i++) {
                    double an = t * 1.5 + i, hy = 1.6 + ((t * 0.5 + i * 0.3) % 1.2);
                    R3.billboardRot(star, x + Math.cos(an) * 0.7, y + hy, z + Math.sin(an) * 0.7, 0.18, t * 3 + i, R3.argb(0xFFE4F2, 0.9));
                }
                break;
            case "cs_xmas":
                xmasFx(x, y, z, t);
                break;
            case "cs_darkaura":
                darkAuraFx(x, y, z, t);
                break;
            case "cs_evillaugh":
                evilFx(x, y, z, t);
                break;
            case "cs_starfall":
                for (int i = 0; i < 14; i++) {
                    double period = 2.2, local = ((t + i * 0.37) % period) / period;
                    double an = i * 2.1, r = 6 + (i % 5) * 3;
                    double sx = x + Math.cos(an) * r, sz = z + Math.sin(an) * r;
                    double sy = y + 30 - local * 26;
                    double hx = sx - local * 4, hz = sz;
                    R3.line(hx + 3, sy + 3, hz, hx, sy, hz, 0.18, R3.argb(0xA8C8FF, 0), R3.argb(0xFFFFFF, 0.9 * (1 - local)));
                    R3.glowBillboard(core, hx, sy, hz, 0.8, R3.argb(0xD8E6FF, 0.9 * (1 - local)));
                }
                for (int i = 0; i < 30; i++) {
                    double an = i * 2.399 + t * 0.5, r = 0.6 + (i % 5) * 0.35, hy = ((i * 0.21 + t * 0.4) % 2.5);
                    R3.billboardRot(star, x + Math.cos(an) * r, y + hy, z + Math.sin(an) * r, 0.1, t + i, R3.argb(0xFFFFFF, 0.8));
                }
                break;
            default:
                if (id.startsWith("cs_gen_")) {
                    double a = Math.min(1, t / 0.6) * Math.min(1, Math.max(0, (dur - t) / 0.6));
                    for (int i=0;i<48;i++) {
                        double an=i*2.399+t*1.8, r=0.8+(i%8)*0.35, yy=((i*0.17+t*0.9)%2.8);
                        R3.glowBillboard(glow,x+Math.cos(an)*r,y+yy,z+Math.sin(an)*r,0.18+(i%3)*0.05,R3.argb(0xA8D8FF,0.65*a));
                    }
                    double pulse=Math.max(0,1-Math.min(1,t/1.0));
                    R3.ring(x,y+0.15,z,1.0+Math.sin(t*4)*0.2,2.0+Math.sin(t*4)*0.3,48,R3.argb(0xFFFFFF,0.0),R3.argb(0x9B7CFF,0.45*a*pulse),t*1.2);
                }
        }
    }

    private static void fireworks(double x, double y, double z, double s, int k) {
        if (s < 0 || s > 2.2) return;
        int[] cols = {0xFF4D6D, 0xFFD23F, 0x3FD2FF, 0x9B5BFF, 0x5BFF9B, 0xFF8A3D};
        int c = cols[k % cols.length];
        double an0 = k * 1.9, cx = x + Math.cos(an0) * 4, cz = z + Math.sin(an0) * 4, cy = y + 7 + (k % 3);
        if (s < 0.5) {    // rocket
            double f = s / 0.5;
            R3.line(cx, y + f * (cy - y) - 1.2, cz, cx, y + f * (cy - y), cz, 0.12, R3.argb(0xFFD9A0, 0), R3.argb(0xFFFFFF, 0.9));
            return;
        }
        double e = s - 0.5, a = Math.max(0, 1 - e / 1.7);
        for (int i = 0; i < 48; i++) {
            double u = (i + 0.5) / 48, phi = Math.acos(1 - 2 * u), th = i * 2.399;
            double sp = 4.5 * (1 - Math.exp(-e * 3));
            double px = cx + Math.sin(phi) * Math.cos(th) * sp, py = cy + Math.cos(phi) * sp - e * e * 1.2, pz = cz + Math.sin(phi) * Math.sin(th) * sp;
            R3.glowBillboard(core, px, py, pz, 0.35, R3.argb(c, a));
        }
        R3.glowBillboard(glow, cx, cy, cz, 6 * a, R3.argb(c, 0.3 * a));
    }


    // ------------------------------------------------------------------ "Dunkle Aura": anime edit with hard cuts
    /** Shot boundaries (seconds): wide, feet, profile, eye, slash, outro. */
    public static final float[] DA_CUTS = {0f, 1.7f, 2.7f, 3.9f, 5.2f, 6.1f};

    /** Shot boundaries of "Böses Lachen": slow push-in, low-angle face, eye close-up, spinning wide shot. */
    public static final float[] EL_CUTS = {0f, 2.6f, 4.4f, 6.0f};

    private static float[] cuts() {
        if ("cs_darkaura".equals(id)) return DA_CUTS;
        if ("cs_evillaugh".equals(id)) return EL_CUTS;
        return null;
    }

    /** Current shot index of an edited cutscene (dark aura / evil laugh; -1 = other / none). */
    public static int shot() {
        float[] cs = cuts();
        if (cs == null) return -1;
        float t = time();
        int s = 0;
        for (int i = 0; i < cs.length; i++) if (t >= cs[i]) s = i;
        return s;
    }

    /** Seconds since the current shot started. */
    public static float shotTime() {
        int s = shot();
        return s < 0 ? 0 : time() - cuts()[s];
    }

    public static float lockYaw() { return baseYaw; }

    /** Cutscenes that frame the face / eye keep your body turned to the start direction. */
    public static boolean locksYaw() { return "cs_darkaura".equals(id) || "cs_evillaugh".equals(id); }

    /** World position of the glowing eye. */
    static double[] eye(double x, double y, double z) {
        double th = Math.toRadians(baseYaw);
        double fx = -Math.sin(th), fz = Math.cos(th), rx = -Math.cos(th), rz = -Math.sin(th);
        return new double[]{x + fx * 0.245 + rx * 0.117, y + 1.611, z + fz * 0.245 + rz * 0.117};
    }

    private static boolean darkAuraCam(double[] pos, float t, double[] out) {
        double th = Math.toRadians(baseYaw);
        double fx = -Math.sin(th), fz = Math.cos(th), rx = -Math.cos(th), rz = -Math.sin(th);
        double x = pos[0], y = pos[1], z = pos[2];
        double cx, cy, cz, lx, ly, lz;
        int s = shot();
        float l = shotTime();
        switch (s) {
            case 0: {   // wide low-angle, slow push in, slight orbit
                double d = 5.6 - l * 0.55, a = Math.toRadians(28 - l * 8);
                cx = x + (fx * Math.cos(a) + rx * Math.sin(a)) * d; cz = z + (fz * Math.cos(a) + rz * Math.sin(a)) * d;
                cy = y + 0.35; lx = x; ly = y + 1.25; lz = z;
                break;
            }
            case 1: {   // feet in the smoke, tracking sideways
                double side = 0.9 - l * 0.5;
                cx = x + fx * 1.5 + rx * side; cz = z + fz * 1.5 + rz * side; cy = y + 0.22;
                lx = x + rx * 0.1; ly = y + 0.2; lz = z + rz * 0.1;
                break;
            }
            case 2: {   // side profile, head rises into frame
                double d = 1.35 - l * 0.12;
                cx = x + rx * d + fx * 0.15; cz = z + rz * d + fz * 0.15; cy = y + 1.55 + l * 0.04;
                lx = x + fx * 0.1; ly = y + 1.55; lz = z + fz * 0.1;
                break;
            }
            case 3: {   // extreme eye close-up, tiny push + tremble
                double[] e = eye(x, y, z);
                double d = 0.55 - Math.min(1, l / 1.3) * 0.14;
                double tr = Math.max(0, l - 0.35) * 0.0025;
                cx = e[0] + fx * d + Math.sin(t * 61) * tr; cz = e[2] + fz * d + Math.cos(t * 57) * tr; cy = e[1] + Math.sin(t * 53) * tr;
                lx = e[0]; ly = e[1]; lz = e[2];
                break;
            }
            case 4: {   // slash: medium front, violent shake
                double sh = Math.max(0, 0.18 - l * 0.2);
                double a = Math.toRadians(-20);
                cx = x + (fx * Math.cos(a) + rx * Math.sin(a)) * 2.6 + Math.sin(t * 71) * sh; cz = z + (fz * Math.cos(a) + rz * Math.sin(a)) * 2.6;
                cy = y + 1.2 + Math.cos(t * 67) * sh;
                lx = x; ly = y + 1.15; lz = z;
                break;
            }
            default: {  // outro: pull back and up, orbit
                float k = Ease.inOutCubic(Math.min(1, l / 1.5f));
                double d = 2.8 + 3.6 * k, a = Math.toRadians(-20 + 50 * k);
                cx = x + (fx * Math.cos(a) + rx * Math.sin(a)) * d; cz = z + (fz * Math.cos(a) + rz * Math.sin(a)) * d;
                cy = y + 1.2 + 1.6 * k; lx = x; ly = y + 1.1; lz = z;
            }
        }
        double dx = lx - cx, dy = ly - cy, dz = lz - cz;
        out[0] = cx; out[1] = cy; out[2] = cz;
        out[3] = Math.toDegrees(Math.atan2(-dx, dz));
        out[4] = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        return true;
    }

    private static class_2960 bat0, bat1;

    private static void darkAuraFx(double x, double y, double z, float t) {
        if (bat0 == null) { bat0 = tex("bat0"); bat1 = tex("bat1"); }
        double fadeIn = Math.min(1, t / 0.6), fadeOut = Math.min(1, Math.max(0, (dur - t) / 0.8));
        double a0 = fadeIn * fadeOut;
        boolean burst = t > DA_CUTS[4];
        double bt = burst ? t - DA_CUTS[4] : 0;
        // black smoke column rising from the ground, dark red core
        for (int i = 0; i < 110; i++) {
            double seed = i * 2.399, life = ((i * 0.137 + t * (0.28 + (i % 5) * 0.04)) % 1.0);
            double r = 0.35 + (i % 7) * 0.26 + life * 0.8 + bt * 1.5;
            double an = seed + t * 0.4 * ((i & 1) == 0 ? 1 : -1) + life * 1.3;
            double hy = life * (2.4 + (i % 4) * 0.4);
            double sz = 0.5 + life * 1.3 + (i % 3) * 0.2;
            double a = Math.sin(life * Math.PI) * a0 * (burst ? Math.max(0.35, 1 - bt * 0.5) : 1);
            int c = i % 6 == 0 ? 0x3A0008 : (i % 4 == 0 ? 0x160406 : 0x050204);
            R3.billboard(glow, x + Math.cos(an) * r, y + 0.05 + hy, z + Math.sin(an) * r, sz, R3.argb(c, 0.62 * a));
        }
        // ground: dark pool + red rune ring
        R3.ring(x, y + 0.03, z, 0, 2.4 + bt * 3, 48, R3.argb(0x000000, 0.7 * a0), R3.argb(0x000000, 0), 0);
        R3.ring(x, y + 0.04, z, 1.5, 1.62, 64, R3.argb(0xFF1030, 0.55 * a0), R3.argb(0x800010, 0.25 * a0), t * 0.6);
        // embers
        for (int i = 0; i < 26; i++) {
            double life = ((i * 0.31 + t * 0.5) % 1.0), an = i * 1.7 + t * 0.8, r = 0.5 + (i % 5) * 0.35;
            R3.glowBillboard(core, x + Math.cos(an) * r, y + life * 3, z + Math.sin(an) * r, 0.06, R3.argb(0xFF2A3A, 0.9 * (1 - life) * a0));
        }
        // bats swirling, exploding outward on the slash
        for (int i = 0; i < 30; i++) {
            double sp = 1.1 + (i % 5) * 0.25, an = i * 2.1 + t * sp * ((i % 3 == 0) ? -1 : 1);
            double r = 1.3 + (i % 6) * 0.45 + Math.sin(t * 2 + i) * 0.25 + bt * bt * 4;
            double hy = 0.4 + (i % 7) * 0.45 + Math.sin(t * 3.1 + i * 1.3) * 0.25 + bt * 1.2;
            class_2960 f = ((int) (t * 13 + i)) % 2 == 0 ? bat0 : bat1;
            R3.billboard(f, x + Math.cos(an) * r, y + hy, z + Math.sin(an) * r, 0.34 + (i % 3) * 0.08, R3.argb(0x1A0A12, 0.95 * a0));
        }
        // glowing red eye (from the profile shot on)
        if (t > DA_CUTS[2] + 0.6) {
            double e = Math.min(1, (t - DA_CUTS[2] - 0.6) / 0.5) * fadeOut;
            double flick = 0.85 + 0.15 * Math.sin(t * 31);
            double[] ep = eye(x, y, z);
            R3.glowBillboard(glow, ep[0], ep[1], ep[2], 0.32 * e * flick, R3.argb(0xFF0A1E, 0.8 * e));
            R3.glowBillboard(core, ep[0], ep[1], ep[2], 0.07 * e, R3.argb(0xFFD0D0, e));
            if (shot() >= 4) R3.line(ep[0], ep[1], ep[2], ep[0] - Math.cos(Math.toRadians(baseYaw)) * 0.6, ep[1] + 0.05, ep[2] - Math.sin(Math.toRadians(baseYaw)) * 0.6,
                    0.04, R3.argb(0xFF1030, 0.8 * e), R3.argb(0xFF1030, 0));    // light trail from the eye
        }
        // slash arcs in the world
        if (burst && bt < 0.7) {
            double k = bt / 0.7, th = Math.toRadians(baseYaw);
            double fx = -Math.sin(th), fz = Math.cos(th), rx = -Math.cos(th), rz = -Math.sin(th);
            for (int s = 0; s < 3; s++) {
                double off = (s - 1) * 0.35, px = 0, py = 0, pz = 0;
                for (int j = 0; j <= 16; j++) {
                    double u = j / 16.0 * Math.min(1, k * 3), an = -1.2 + u * 2.4;
                    double qx = x + fx * (1.0 + off * 0.3) + rx * Math.sin(an) * 1.6, qy = y + 1.2 + Math.cos(an) * 0.9 + off, qz = z + fz * (1.0 + off * 0.3) + rz * Math.sin(an) * 1.6;
                    if (j > 0) R3.line(px, py, pz, qx, qy, qz, 0.09 * (1 - k), R3.argb(0xFF1030, 0.9 * (1 - k)), R3.argb(0xFFD0D0, 0.9 * (1 - k)));
                    px = qx; py = qy; pz = qz;
                }
            }
        }
    }


    // ------------------------------------------------------------------ "Böses Lachen": evil-genius laugh with hard cuts
    /** Pose of the running emote at the cutscene clock (for pose-aware effect positions). */
    private static float[] pose() {
        EmoteLib.Emote e = EmoteLib.get(id);
        return e == null ? null : e.pose(time());
    }

    /** Glowing right eye, following head pitch and the body bend (pivots as in Emotes.pose). */
    static double[] eyeAt(double x, double y, double z) {
        float[] p = pose();
        double hp = p == null || Float.isNaN(p[EmoteLib.HEAD]) ? 0 : p[EmoteLib.HEAD];
        double b = p == null || Float.isNaN(p[EmoteLib.BODY]) ? 0 : p[EmoteLib.BODY];
        double th = Math.toRadians(baseYaw);
        double fx = -Math.sin(th), fz = Math.cos(th), rx = -Math.cos(th), rz = -Math.sin(th);
        // neck = hip (0.75) + 0.75 up, bent forward by b; eye = 0.111 up / 0.245 forward from the neck, rotated by head pitch + bend
        double neckUp = 0.75 + 0.75 * Math.cos(b), neckFwd = 0.75 * Math.sin(b);
        double a = hp + b;
        double up = neckUp + 0.111 * Math.cos(a) - 0.245 * Math.sin(a), fwd = neckFwd + 0.245 * Math.cos(a) + 0.111 * Math.sin(a);
        return new double[]{x + fx * fwd + rx * 0.117, y + up, z + fz * fwd + rz * 0.117};
    }

    private static boolean evilCam(double[] pos, float t, double[] out) {
        double th = Math.toRadians(baseYaw);
        double fx = -Math.sin(th), fz = Math.cos(th), rx = -Math.cos(th), rz = -Math.sin(th);
        double x = pos[0], y = pos[1], z = pos[2];
        double cx, cy, cz, lx, ly, lz;
        int s = shot();
        float l = shotTime();
        switch (s) {
            case 0: {   // slow push-in, slightly low, drifting to the side
                float k = Ease.inOutCubic(Math.min(1, l / 2.6f));
                double d = 5.4 - 2.3 * k, a = Math.toRadians(24 - 16 * k);
                cx = x + (fx * Math.cos(a) + rx * Math.sin(a)) * d; cz = z + (fz * Math.cos(a) + rz * Math.sin(a)) * d;
                cy = y + 0.9 + 0.15 * k; lx = x; ly = y + 1.35 + 0.1 * k; lz = z;
                break;
            }
            case 1: {   // HARD CUT: low angle below the chin looking up at the laughing face, trembling
                double d = 1.05 - Math.min(1, l / 1.8) * 0.15, tr = 0.012;
                cx = x + fx * d + rx * 0.18 + Math.sin(t * 43) * tr; cz = z + fz * d + rz * 0.18 + Math.cos(t * 39) * tr;
                cy = y + 0.95 + Math.sin(t * 47) * tr;
                lx = x + fx * 0.05; ly = y + 1.72; lz = z + fz * 0.05;
                break;
            }
            case 2: {   // extreme close-up on the glowing eye: tiny push + growing tremble
                double[] e = eyeAt(x, y, z);
                double d = 0.52 - Math.min(1, l / 1.5) * 0.14;
                double tr = Math.max(0, l - 0.5) * 0.0028;
                cx = e[0] + fx * d + Math.sin(t * 61) * tr; cz = e[2] + fz * d + Math.cos(t * 57) * tr; cy = e[1] + Math.sin(t * 53) * tr;
                lx = e[0]; ly = e[1]; lz = e[2];
                break;
            }
            default: {  // wide shot: the camera whirls around the laughing figure, lifting and pulling back
                float k = Ease.inOutCubic(Math.min(1, l / 1.4f)), spin = Ease.inOutCubic(Math.min(1, l / 4.0f));
                double d = 2.6 + 3.0 * k, a = Math.PI * 2 * 1.3 * spin + Math.toRadians(10);
                double sh = Math.max(0, 0.12 - l * 0.12) + bolt(t) * 0.06;
                cx = x + (fx * Math.cos(a) + rx * Math.sin(a)) * d + Math.sin(t * 71) * sh; cz = z + (fz * Math.cos(a) + rz * Math.sin(a)) * d;
                cy = y + 0.45 + 1.3 * k + Math.cos(t * 67) * sh; lx = x; ly = y + 1.25; lz = z;
            }
        }
        double dx = lx - cx, dy = ly - cy, dz = lz - cz;
        out[0] = cx; out[1] = cy; out[2] = cz;
        out[3] = Math.toDegrees(Math.atan2(-dx, dz));
        out[4] = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        return true;
    }

    /** Lightning strike times of the evil laugh (seconds). */
    public static final float[] EL_BOLTS = {2.6f, 6.0f, 7.1f, 8.3f};

    /** 0..1 flash of the most recent lightning strike. */
    public static float bolt(float t) {
        float f = 0;
        for (float b : EL_BOLTS) if (t >= b && t < b + 0.45f) f = Math.max(f, (float) Math.exp(-(t - b) * 9) * (0.6f + 0.4f * (float) Math.abs(Math.sin((t - b) * 60))));
        return f;
    }

    /** Heartbeat pulse 0..1 of the evil laugh (double thump, synced with the heartbeat cues). */
    public static double heartbeat(float t) {
        double v = 0;
        for (Cue q : CUES.get("cs_evillaugh")) {
            if (!q.id().endsWith("heartbeat")) continue;
            double d = t - q.t();
            if (d >= 0 && d < 0.7) v = Math.max(v, Math.exp(-d * 9) + 0.6 * (d > 0.22 ? Math.exp(-(d - 0.22) * 9) : 0));
        }
        return Math.min(1, v);
    }

    /** Jagged lightning between two points (seeded, flickers with the seed). */
    private static void arc(double x1, double y1, double z1, double x2, double y2, double z2, long seed, int segs, double jit, double w, double a) {
        java.util.Random r = new java.util.Random(seed);
        double px = x1, py = y1, pz = z1;
        for (int i = 1; i <= segs; i++) {
            double u = i / (double) segs, j = i == segs ? 0 : jit * Math.sin(u * Math.PI);
            double qx = x1 + (x2 - x1) * u + (r.nextDouble() - 0.5) * 2 * j, qy = y1 + (y2 - y1) * u + (r.nextDouble() - 0.5) * j, qz = z1 + (z2 - z1) * u + (r.nextDouble() - 0.5) * 2 * j;
            R3.line(px, py, pz, qx, qy, qz, w * 3, R3.argb(0xFF0A1E, 0.35 * a), R3.argb(0xFF0A1E, 0.35 * a));
            R3.line(px, py, pz, qx, qy, qz, w, R3.argb(0xFFE0E4, a), R3.argb(0xFF6070, a));
            if (i == segs / 2 && r.nextBoolean()) {   // side branch
                double bx = qx + (r.nextDouble() - 0.5) * jit * 3, by = qy - jit * 1.5, bz = qz + (r.nextDouble() - 0.5) * jit * 3;
                R3.line(qx, qy, qz, bx, by, bz, w * 0.6, R3.argb(0xFF8090, a), R3.argb(0xFF1030, 0));
            }
            px = qx; py = qy; pz = qz;
        }
    }

    private static class_2960 page, ash;

    private static void evilFx(double x, double y, double z, float t) {
        if (page == null) { page = tex("confetti"); ash = tex("ash"); }
        double fadeIn = Math.min(1, t / 0.6), fadeOut = Math.min(1, Math.max(0, (dur - t) / 0.8));
        double a0 = fadeIn * fadeOut, hb = heartbeat(t);
        int s = shot();
        boolean wide = s >= 3;
        double wt = wide ? t - EL_CUTS[3] : 0;
        // pulsing dark aura hugging the body (breathes with the heartbeat, flares in the wide shot)
        for (int i = 0; i < 80; i++) {
            double life = ((i * 0.137 + t * (0.32 + (i % 5) * 0.05)) % 1.0);
            double an = i * 2.399 + t * 0.5 * ((i & 1) == 0 ? 1 : -1) + life * 1.6;
            double r = 0.3 + (i % 6) * 0.14 + life * 0.5 + hb * 0.25 + (wide ? Math.min(1, wt) * 0.6 : 0);
            double sz = 0.45 + life * 1.1 + hb * 0.3;
            double a = Math.sin(life * Math.PI) * a0 * (0.75 + 0.25 * hb);
            int c = i % 5 == 0 ? 0x4A000C : (i % 3 == 0 ? 0x1A0306 : 0x040102);
            R3.billboard(glow, x + Math.cos(an) * r, y + 0.05 + life * (2.2 + (i % 3) * 0.3), z + Math.sin(an) * r, sz, R3.argb(c, 0.6 * a));
        }
        // blood-red rim light around the silhouette + ground sigil, both pulsing
        R3.glowBillboard(glow, x, y + 1.0, z, 2.6 + hb * 0.8, R3.argb(0xB0001A, (0.18 + 0.22 * hb) * a0));
        R3.ring(x, y + 0.03, z, 0, 2.6, 48, R3.argb(0x000000, 0.75 * a0), R3.argb(0x000000, 0), 0);
        R3.ring(x, y + 0.04, z, 1.3 + hb * 0.12, 1.4 + hb * 0.16, 64, R3.argb(0xFF1030, (0.45 + 0.4 * hb) * a0), R3.argb(0x800010, 0.2 * a0), -t * 0.4);
        R3.ring(x, y + 0.045, z, 1.85, 1.9, 64, R3.argb(0xFF1030, 0.3 * a0), R3.argb(0xFF1030, 0.1 * a0), t * 0.25);
        if (hb > 0.05) R3.ring(x, y + 0.05, z, 0.4 + (1 - hb) * 2.5, 0.5 + (1 - hb) * 2.6, 48, R3.argb(0xFF2040, 0), R3.argb(0xFF2040, 0.5 * hb * a0), 0);
        // falling ash everywhere
        for (int i = 0; i < 90; i++) {
            double life = ((i * 0.083 + t * (0.09 + (i % 4) * 0.015)) % 1.0), an = i * 2.399, r = 0.5 + (i % 11) * 0.45;
            double ax = x + Math.cos(an) * r + Math.sin(t * 0.8 + i) * 0.3, az = z + Math.sin(an) * r + Math.cos(t * 0.7 + i) * 0.3;
            R3.billboardRot(ash, ax, y + 5.5 - life * 5.6, az, 0.06 + (i % 3) * 0.03, t * 0.6 + i, R3.argb(i % 7 == 0 ? 0x5A1A1E : 0x2A2A2E, 0.85 * a0 * Math.min(1, life * 5)));
        }
        // loose black pages: a few drifting down at first, then a howling vortex in the wide shot
        int np = wide ? 44 : 12;
        for (int i = 0; i < np; i++) {
            double px, py, pz, ang, sz = 0.2 + (i % 4) * 0.04;
            if (!wide) {
                double life = ((i * 0.27 + t * 0.11) % 1.0), an = i * 2.1;
                double r = 1.4 + (i % 4) * 0.6;
                px = x + Math.cos(an) * r + Math.sin(t * 1.3 + i) * 0.35; pz = z + Math.sin(an) * r + Math.cos(t * 1.1 + i) * 0.35;
                py = y + 3.6 - life * 3.5; ang = Math.sin(t * 2 + i) * 1.2;
            } else {
                double k = Math.min(1, wt / 0.6), sp = 1.6 + (i % 5) * 0.3;
                double an = i * 2.399 + wt * sp * (1 + 0.8 * Math.max(0, 1 - wt)) ;
                double r = 0.5 + k * (1.1 + (i % 7) * 0.35) + Math.sin(wt * 2 + i) * 0.2;
                px = x + Math.cos(an) * r; pz = z + Math.sin(an) * r;
                py = y + 0.3 + ((i * 0.23 + wt * (0.35 + (i % 3) * 0.1)) % 1.0) * 3.4 * k + (1 - k) * 1.0;
                ang = wt * (4 + i % 5) + i;
            }
            double a = a0 * (wide ? 1 : Math.min(1, t / 1.0));
            R3.glowBillboard(page, px, py, pz, sz * 1.25, R3.argb(0xFF1030, 0.35 * a));      // red edge glow
            R3.billboardRot(page, px, py, pz, sz, ang, R3.argb(0x0A0808, 0.95 * a));
        }
        // red lightning: sky strikes at the bolt times, small arcs crawling over the aura while laughing
        for (float b : EL_BOLTS) {
            float d = t - b;
            if (d < 0 || d > 0.4f) continue;
            double fl = Math.exp(-d * 7) * (0.5 + 0.5 * Math.abs(Math.sin(d * 55)));
            double an = b * 2.3, r = b == EL_BOLTS[0] ? 7 : 3.2;
            double sx = x + Math.cos(an) * r, sz2 = z + Math.sin(an) * r;
            arc(sx + 1.5, y + 18, sz2 - 1, sx, y, sz2, (long) (b * 100) + (long) (d * 20), 14, 0.9, 0.12, fl * a0);
            R3.glowBillboard(glow, sx, y + 0.3, sz2, 3 * fl, R3.argb(0xFF2040, 0.6 * fl * a0));
        }
        if (s == 1 || wide) {
            int n = wide ? 3 : 1;
            for (int k = 0; k < n; k++) {
                long seed = (long) (t * 14) * 7 + k;
                java.util.Random r = new java.util.Random(seed);
                if (r.nextDouble() > (wide ? 0.75 : 0.4)) continue;
                double an = r.nextDouble() * Math.PI * 2, an2 = an + 0.6 + r.nextDouble();
                double r1 = 0.6 + r.nextDouble() * 0.5, r2 = 0.8 + r.nextDouble() * 0.9;
                arc(x + Math.cos(an) * r1, y + 1.4 + r.nextDouble() * 0.9, z + Math.sin(an) * r1,
                        x + Math.cos(an2) * r2, y + 0.1 + r.nextDouble() * 0.5, z + Math.sin(an2) * r2, seed, 7, 0.22, 0.035, 0.9 * a0);
            }
        }
        // the eye: glows from the close-up on, leaves a light trail while the camera whirls
        if (t > EL_CUTS[2] - 0.1) {
            double e = Math.min(1, (t - EL_CUTS[2] + 0.1) / 0.5) * fadeOut;
            double flick = 0.85 + 0.15 * Math.sin(t * 31);
            double[] ep = eyeAt(x, y, z);
            R3.glowBillboard(glow, ep[0], ep[1], ep[2], (0.32 + 0.1 * hb) * e * flick, R3.argb(0xFF0A1E, 0.85 * e));
            R3.glowBillboard(core, ep[0], ep[1], ep[2], 0.07 * e, R3.argb(0xFFD0D0, e));
            if (wide) {
                double th = Math.toRadians(baseYaw), rx = -Math.cos(th), rz = -Math.sin(th);
                R3.line(ep[0], ep[1], ep[2], ep[0] + rx * 0.7, ep[1] + 0.08, ep[2] + rz * 0.7, 0.045, R3.argb(0xFF1030, 0.85 * e), R3.argb(0xFF1030, 0));
            }
        }
    }

    // ------------------------------------------------------------------ "Weihnachtszauber"
    private static final int[] XB = {0xFF3B4E, 0xFFC94A, 0x3BE08A, 0x4AA8FF, 0xFF7BD5};

    private static void cone(double x, double y0, double z, double r, double h, int seg, int cBottom, int cTop) {
        for (int i = 0; i < seg; i++) {
            double a0 = i * Math.PI * 2 / seg, a1 = (i + 1) * Math.PI * 2 / seg;
            double x0 = x + Math.cos(a0) * r, z0 = z + Math.sin(a0) * r, x1 = x + Math.cos(a1) * r, z1 = z + Math.sin(a1) * r;
            int shade = (i % 2 == 0) ? cBottom : R3.argb(((cBottom >> 16 & 255) * 9 / 10) << 16 | ((cBottom >> 8 & 255) * 9 / 10) << 8 | (cBottom & 255) * 9 / 10, (cBottom >>> 24) / 255.0);
            R3.quad(x0, y0, z0, x1, y0, z1, x, y0 + h, z, x, y0 + h, z, shade, shade, cTop, cTop);
            R3.quad(x0, y0, z0, x1, y0, z1, x, y0 + 0.05, z, x, y0 + 0.05, z, cBottom, cBottom, cBottom, cBottom);   // underside
        }
    }

    private static void box(double x0, double y0, double z0, double x1, double y1, double z1, int top, int side) {
        R3.quad(x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, top);
        R3.quad(x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, side);
        R3.quad(x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, side);
        R3.quad(x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, side);
        R3.quad(x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, side);
    }

    private static void xmasFx(double x, double y, double z, float t) {
        double th = Math.toRadians(baseYaw);
        double fx = -Math.sin(th), fz = Math.cos(th);
        double out = Math.min(1, Math.max(0, (dur - t) / 0.8));
        // tree 2 blocks behind you
        double tx = x - fx * 2.2, tz = z - fz * 2.2;
        double grow = Ease.outBack(Math.max(0, Math.min(1, (t - 0.5f) / 2.0f)));
        if (grow > 0.01) {
            double sc = grow;
            box(tx - 0.15 * sc, y, tz - 0.15 * sc, tx + 0.15 * sc, y + 0.5 * sc, tz + 0.15 * sc, R3.argb(0x5A3A1E, out), R3.argb(0x4A2E16, out));
            double[][] tiers = {{0.45, 1.3, 1.2}, {1.05, 1.05, 1.0}, {1.6, 0.8, 0.85}, {2.1, 0.5, 0.7}};
            for (double[] tr : tiers)
                cone(tx, y + tr[0] * sc, tz, tr[1] * sc, tr[2] * sc, 20, R3.argb(0x0E6A34, out), R3.argb(0x2FB85F, out));
            // snow on the tiers
            for (double[] tr : tiers) R3.ring(tx, y + tr[0] * sc + 0.02, tz, tr[1] * sc * 0.82, tr[1] * sc * 0.98, 28, R3.argb(0xFFFFFF, 0.55 * out), R3.argb(0xE8F2FF, 0.2 * out), 0);
            // fairy lights spiralling up
            double lit = Math.max(0, Math.min(1, (t - 2.2) / 1.0));
            int n = 46;
            for (int i = 0; i < n * lit; i++) {
                double u = i / (double) n, hy = 0.5 + u * 2.3, r = (1.3 - u * 1.15) * 0.98;
                double an = u * Math.PI * 9 + 0.4;
                double tw = 0.6 + 0.4 * Math.sin(t * 5 + i * 1.7);
                int c = XB[i % XB.length];
                double px = tx + Math.cos(an) * r * sc, py = y + hy * sc, pz = tz + Math.sin(an) * r * sc;
                R3.glowBillboard(glow, px, py, pz, 0.28 * tw, R3.argb(c, 0.55 * out));
                R3.glowBillboard(core, px, py, pz, 0.07, R3.argb(c, out));
            }
            // star on top + burst
            double st = Math.max(0, Math.min(1, (t - 3.1) / 0.3));
            if (st > 0) {
                double ty = y + 2.95 * sc;
                double pul = 0.85 + 0.15 * Math.sin(t * 6);
                R3.glowBillboard(glow, tx, ty, tz, 1.4 * st * pul, R3.argb(0xFFD66B, 0.8 * out));
                R3.billboardRot(star, tx, ty, tz, 0.55 * st, t * 0.8, R3.argb(0xFFF2B0, out));
                double b = t - 3.1;
                if (b > 0 && b < 1.4) {
                    double a = 1 - b / 1.4;
                    R3.ring(tx, ty, tz, b * 4, b * 4 + 0.25, 48, R3.argb(0xFFE08A, 0), R3.argb(0xFFF6D0, 0.8 * a), 0);
                    for (int i = 0; i < 40; i++) {
                        double u = (i + 0.5) / 40, phi = Math.acos(1 - 2 * u), an = i * 2.399, sp = 3.5 * (1 - Math.exp(-b * 3));
                        R3.glowBillboard(core, tx + Math.sin(phi) * Math.cos(an) * sp, ty + Math.cos(phi) * sp - b * b * 0.8, tz + Math.sin(phi) * Math.sin(an) * sp, 0.18, R3.argb(XB[i % XB.length], a));
                    }
                }
            }
            // gifts pop up around the tree
            int[][] gc = {{0xD62238, 0xF4C152}, {0x1A8A4E, 0xE3263B}, {0x1E3E7A, 0xF4C152}, {0xE8B23A, 0xB5152C}, {0x6B2FB0, 0xF4E9D0}};
            for (int i = 0; i < 5; i++) {
                double gt = Math.max(0, Math.min(1, (t - 3.5 - i * 0.18) / 0.35));
                if (gt <= 0) continue;
                double pop = Ease.outBack((float) gt), an = i * 1.25 + 0.3, r = 1.25;
                double gx = tx + Math.cos(an) * r, gz = tz + Math.sin(an) * r, s2 = (0.22 + (i % 2) * 0.06) * pop;
                int bc = R3.argb(gc[i][0], out), rc = R3.argb(gc[i][1], out);
                box(gx - s2, y, gz - s2, gx + s2, y + s2 * 1.6, gz + s2, bc, bc);
                box(gx - s2 * 1.02, y + s2 * 1.6, gz - s2 * 0.18, gx + s2 * 1.02, y + s2 * 1.62, gz + s2 * 0.18, rc, rc);
                box(gx - s2 * 0.18, y + s2 * 1.6, gz - s2 * 1.02, gx + s2 * 0.18, y + s2 * 1.62, gz + s2 * 1.02, rc, rc);
            }
        }
        // snow falling all around
        for (int i = 0; i < 140; i++) {
            double life = ((i * 0.071 + t * 0.12) % 1.0), an = i * 2.399, r = 0.6 + (i % 9) * 0.6;
            double sx = x + Math.cos(an) * r + Math.sin(t + i) * 0.2, sz = z + Math.sin(an) * r;
            R3.glowBillboard(core, sx, y + 6 - life * 6.2, sz, 0.05 + (i % 3) * 0.02, R3.argb(0xFFFFFF, 0.85 * out * Math.min(1, life * 4)));
        }
        // Santa's sleigh trail across the sky
        double sl = (t - 4.3) / 2.6;
        if (sl > 0 && sl < 1) {
            double rx = -Math.cos(th), rz = -Math.sin(th);
            double sx = x - rx * (14 - 28 * sl) - fx * 8, sz = z - rz * (14 - 28 * sl) - fz * 8, sy = y + 9 + Math.sin(sl * Math.PI) * 2;
            R3.glowBillboard(glow, sx, sy, sz, 1.2, R3.argb(0xFFE3A0, 0.8 * out));
            R3.glowBillboard(core, sx, sy, sz, 0.35, R3.argb(0xFF3040, out));
            for (int k = 1; k < 30; k++) {
                double q = sl - k * 0.012;
                if (q < 0) break;
                double px = x - rx * (14 - 28 * q) - fx * 8, pz = z - rz * (14 - 28 * q) - fz * 8, py = y + 9 + Math.sin(q * Math.PI) * 2;
                R3.glowBillboard(star, px, py + Math.sin(k * 1.3 + t * 9) * 0.1, pz, 0.25 * (1 - k / 30.0), R3.argb(XB[k % XB.length], (1 - k / 30.0) * out));
            }
        }
    }

    private static final java.util.Map<String, class_2960> TEX = new java.util.HashMap<>();

    private static class_2960 tex(String id) {
        return TEX.computeIfAbsent(id, k -> {
            class_2960 t = class_2960.method_60655("legoclient", "cutscene/" + k);
            try { dev.lego.ui.Tx.register(t, dev.lego.visual.AmbientTex.paint(k), dev.lego.visual.AmbientTex.S, dev.lego.visual.AmbientTex.S, true); } catch (Throwable ignored) { }
            return t;
        });
    }
}
