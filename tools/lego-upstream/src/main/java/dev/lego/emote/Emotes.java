package dev.lego.emote;

import java.util.HashMap;
import java.util.Map;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Tx;
import dev.lego.visual.R3;
import net.minecraft.class_2960;
import net.minecraft.class_5498;
import net.minecraft.class_630;
import net.minecraft.class_746;

/**
 * Plays {@link EmoteLib} emotes on your own player (client side only). Poses are applied in PlayerModelMixin,
 * bubbles are billboards above your head.
 */
public final class Emotes {
    private Emotes() { }

    public static EmoteModule module;

    private static EmoteLib.Emote current;
    private static long startMs, stopMs;
    private static class_5498 savedPerspective;
    private static float spin;
    private static double startX, startZ;
    /** Pose that is blended out after an emote ended. */
    private static float[] fading;

    public static void register() { module = Modules.register(new EmoteModule()); }

    public static final class EmoteModule extends Module {
        public final Setting.Bool camera = add(new Setting.Bool("Kamera nach vorne drehen", true));
        public final Setting.Bool cancelOnMove = add(new Setting.Bool("Bei Bewegung abbrechen", true));
        public final Setting.Bool bubbles = add(new Setting.Bool("Sprechblasen", true));
        public final Setting.Num speed = add(new Setting.Num("Tempo", 0.5, 2, 0.1, 1, "x"));
        EmoteModule() {
            super("emotes", "Emotes", "Taste B halten: Emote-Rad · " + EmoteLib.ALL.size() + " Emotes, nur für dich sichtbar", Category.EMOTES);
            enabled = true;
            icon("emote");
        }
    }

    public static boolean active() { return current != null || fading != null; }
    public static EmoteLib.Emote current() { return current; }

    public static void play(int idx) {
        if (idx < 0 || idx >= EmoteLib.ALL.size() || module == null || !module.enabled) return;
        class_746 p = Mc.player();
        if (p == null) return;
        EmoteLib.Emote e = EmoteLib.ALL.get(idx);
        boolean restoreCam = savedPerspective != null;
        class_5498 saved = savedPerspective;
        current = null;
        fading = null;
        current = e;
        startMs = System.currentTimeMillis();
        spin = 0;
        startX = Mc.x(p);
        startZ = Mc.z(p);
        if (EmoteLib.isCutscene(e.id)) {
            if (restoreCam) { try { Mc.perspective(saved); } catch (Throwable ignored) { } }
            savedPerspective = null;
            Cutscenes.begin(e);
        } else if (restoreCam) savedPerspective = saved;
        else if (module.camera.get() && Mc.firstPerson()) {
            savedPerspective = Mc.perspective();
            try { Mc.perspective(class_5498.field_26666); } catch (Throwable ignored) { }
        }
    }

    public static void stop() {
        if (current == null) return;
        Cutscenes.end();
        fading = current.pose(time());
        stopMs = System.currentTimeMillis();
        current = null;
        if (savedPerspective != null) {
            try { Mc.perspective(savedPerspective); } catch (Throwable ignored) { }
            savedPerspective = null;
        }
    }

    private static float time() {
        return (System.currentTimeMillis() - startMs) / 1000f * (module == null ? 1 : module.speed.getF());
    }

    public static void tick() {
        if (fading != null && System.currentTimeMillis() - stopMs > 260) fading = null;
        if (current == null) return;
        class_746 p = Mc.player();
        if (p == null || Mc.isDead(p)) { stop(); return; }
        float t = time();
        if (!current.loop && t > current.duration) { stop(); return; }
        double dx = Mc.x(p) - startX, dz = Mc.z(p) - startZ;
        if (module.cancelOnMove.get() && (dx * dx + dz * dz > 0.09 || Mc.isSneaking(p))) { stop(); return; }
        Cutscenes.sounds();
        if (Cutscenes.active() && Cutscenes.locksYaw()) Mc.setBodyYaw(p, Cutscenes.lockYaw());
        if (current.spin != 0) {
            spin += current.spin / 20f * module.speed.getF();
            Mc.setBodyYaw(p, Mc.yaw(p) + spin);
        }
    }

    /** Pose of ANOTHER Lego user (Lego-Sync): emote id + unix second it started. */
    public static void poseOther(String id, double startSec, class_630 head, class_630 body, class_630 rightArm, class_630 leftArm, class_630 rightLeg, class_630 leftLeg) {
        EmoteLib.Emote e = EmoteLib.get(id);
        if (e == null || startSec <= 0) return;
        float t = (float) (System.currentTimeMillis() / 1000.0 - startSec);
        if (t < 0) t = 0;
        if (!e.loop && t > e.duration) return;
        if (e.loop) t = t % Math.max(0.1f, e.duration);
        float[] p = e.pose(t);
        float blend = smooth(Math.min(1, Math.min(t / 0.25f, e.loop ? 1 : (e.duration - t) / 0.25f)));
        if (blend <= 0) return;
        apply(head, p, EmoteLib.HEAD, blend);
        apply(body, p, EmoteLib.BODY, blend);
        apply(rightArm, p, EmoteLib.RARM, blend);
        apply(leftArm, p, EmoteLib.LARM, blend);
        apply(rightLeg, p, EmoteLib.RLEG, blend);
        apply(leftLeg, p, EmoteLib.LLEG, blend);
    }

    // ------------------------------------------------------------------ model (called from PlayerModelMixin)
    /** Applies the current pose to your own model parts (after vanilla setAngles). */
    public static void pose(class_630 head, class_630 body, class_630 rightArm, class_630 leftArm, class_630 rightLeg, class_630 leftLeg) {
        float[] p;
        float blend;
        if (current != null) {
            float t = time();
            p = current.pose(t);
            float in = Math.min(1, (System.currentTimeMillis() - startMs) / 250f);
            float out = current.loop ? 1 : Math.min(1, (current.duration - t) / 0.25f);
            blend = smooth(Math.max(0, Math.min(in, out)));
        } else if (fading != null) {
            p = fading;
            blend = smooth(1 - Math.min(1, (System.currentTimeMillis() - stopMs) / 250f));
        } else return;
        if (blend <= 0) return;

        apply(head, p, EmoteLib.HEAD, blend);
        apply(body, p, EmoteLib.BODY, blend);
        apply(rightArm, p, EmoteLib.RARM, blend);
        apply(leftArm, p, EmoteLib.LARM, blend);
        apply(rightLeg, p, EmoteLib.RLEG, blend);
        apply(leftLeg, p, EmoteLib.LLEG, blend);

        // bending the body (bow ...) rotates the whole upper body around the hip
        float bend = Float.isNaN(p[EmoteLib.BODY]) ? 0 : p[EmoteLib.BODY] * blend;
        if (bend != 0) {
            // vanilla already wrote body.pitch; the pose value replaces it, so only the upper parts are moved here
            pivotBend(head, bend);
            pivotBend(rightArm, bend);
            pivotBend(leftArm, bend);
            head.field_3654 += bend;
            rightArm.field_3654 += bend;
            leftArm.field_3654 += bend;
            // keep the legs straight: body pivot at the neck -> move it so the hip stays in place
            body.field_3656 = 12 - 12 * (float) Math.cos(bend);
            body.field_3655 = -12 * (float) Math.sin(bend);
        }
    }

    private static void pivotBend(class_630 part, float a) {
        float ry = part.field_3656 - 12, rz = part.field_3655;
        float c = (float) Math.cos(a), s = (float) Math.sin(a);
        part.field_3656 = 12 + ry * c - rz * s;
        part.field_3655 = ry * s + rz * c;
    }

    private static void apply(class_630 part, float[] p, int i, float b) {
        if (part == null) return;
        if (!Float.isNaN(p[i])) part.field_3654 = lerpAngle(part.field_3654, p[i], b);
        if (!Float.isNaN(p[i + 1])) part.field_3675 = lerpAngle(part.field_3675, p[i + 1], b);
        if (!Float.isNaN(p[i + 2])) part.field_3674 = lerpAngle(part.field_3674, p[i + 2], b);
    }

    private static float lerpAngle(float a, float b, float t) {
        float d = (float) Math.IEEEremainder(b - a, Math.PI * 2);
        return a + d * t;
    }

    private static float smooth(float t) { return t * t * (3 - 2 * t); }

    // ------------------------------------------------------------------ bubbles
    private static final Map<String, class_2960> BUBBLES = new HashMap<>();

    public static void renderBubble(float td) {
        if (current == null || current.bubble == null || !module.bubbles.get()) return;
        class_746 p = Mc.player();
        if (p == null || Mc.firstPerson()) return;
        class_2960 tex = BUBBLES.get(current.bubble);
        if (tex == null) {
            tex = class_2960.method_60655("legoclient", "emote/" + current.bubble);
            try { Tx.register(tex, Bubbles.paint(current.bubble, 256), 256, 256, true); }
            catch (Throwable t) { LegoClient.LOG("Emote-Textur: " + t); }
            BUBBLES.put(current.bubble, tex);
        }
        float t = time();
        double pop = Math.min(1, t / 0.3);
        pop = 1 + 2.70158 * Math.pow(pop - 1, 3) + 1.70158 * Math.pow(pop - 1, 2);   // back-out
        double fade = current.loop ? 1 : Math.max(0, Math.min(1, (current.duration - t) / 0.3));
        double[] pos = Mc.lerpPos(p, td);
        double y = pos[1] + Mc.height(p) + 0.6 + Math.sin(t * 3) * 0.05;
        R3.billboard(tex, pos[0], y, pos[2], 0.75 * pop, R3.argb(0xFFFFFF, fade));
    }
}
