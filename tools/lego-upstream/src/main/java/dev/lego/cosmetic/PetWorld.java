package dev.lego.cosmetic;

import java.util.HashMap;
import java.util.Map;

import dev.lego.core.Mc;
import dev.lego.emote.Bubbles;
import dev.lego.ui.Tx;
import dev.lego.visual.R3;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_265;
import net.minecraft.class_2960;
import net.minecraft.class_638;
import net.minecraft.class_746;

/**
 * Your equipped pet as a little companion in the world (client side only): follows you, walks / hops / flies,
 * blinks, looks at you and shows emotions depending on what you do.
 */
public final class PetWorld {
    private PetWorld() { }

    private static double x, y, z, px, py, pz;
    private static float yaw, lastYaw;
    private static boolean placed;
    private static float speed;
    private static String emotion = "happy", shownEmotion = "happy";
    private static long emotionUntil, bubbleAt, lastActive = System.currentTimeMillis(), nextBlink, blinkAt, curiousAt;
    private static float lastHealth = -1;
    private static long nextCurious = System.currentTimeMillis() + 20000;
    private static float spinFrom;

    /** Growth from your XP level: 0 (baby) .. 1 (level 40+). */
    static float growth() {
        class_746 p = Mc.player();
        int lvl = p == null ? 0 : p.field_7520;
        return Math.max(0, Math.min(1, lvl / 40f));
    }

    static int level() {
        class_746 p = Mc.player();
        int lvl = p == null ? 0 : p.field_7520;
        return lvl >= 35 ? 5 : lvl >= 25 ? 4 : lvl >= 15 ? 3 : lvl >= 5 ? 2 : 1;
    }

    private static int lastLevel = -1;

    public static void onHit() { set("excited", 1800); }
    public static void onKill() { set("excited", 3500); }

    private static void set(String e, long ms) {
        emotion = e;
        emotionUntil = System.currentTimeMillis() + ms;
    }

    /** Game tick: movement + emotion logic. */
    public static void tick() {
        class_746 p = Mc.player();
        Cos.Item it = Cos.equipped(Cos.Slot.PET);
        if (p == null || it == null) { placed = false; return; }
        long now = System.currentTimeMillis();
        px = x; py = y; pz = z;
        lastYaw = yaw;

        // target: behind-right of the player
        double by = Math.toRadians(Mc.bodyYaw(p));
        double fx = -Math.sin(by), fz = Math.cos(by), rx = -Math.cos(by), rz = -Math.sin(by);
        double off = 0.85 + growth() * 0.45;
        double tx = Mc.x(p) + rx * off - fx * (0.55 + growth() * 0.4), tz = Mc.z(p) + rz * off - fz * (0.55 + growth() * 0.4);
        int lv = level();
        if (lastLevel > 0 && lv > lastLevel) { set("excited", 4000); dev.lego.ui.Toasts.show("paw", "Dein Haustier ist gewachsen!", "Stufe " + lv + " von 5", 0xFFFFC53D, 4000); }
        lastLevel = lv;
        double ty = ground(tx, Mc.y(p) + 0.6, tz, Mc.y(p));
        if (!placed || (x - tx) * (x - tx) + (z - tz) * (z - tz) > 144 || Math.abs(y - ty) > 8) {
            x = px = tx; y = py = ty; z = pz = tz;
            yaw = lastYaw = Mc.yaw(p);
            placed = true;
        }
        double dx = tx - x, dz = tz - z, d = Math.sqrt(dx * dx + dz * dz);
        double k = d > 0.25 ? Math.min(0.35, 0.08 + d * 0.06) : 0;
        x += dx * k;
        z += dz * k;
        y += (ty - y) * 0.35;
        double moved = Math.sqrt((x - px) * (x - px) + (z - pz) * (z - pz));
        speed += ((float) Math.min(1, moved * 5) - speed) * 0.3f;
        // face walking direction, otherwise look towards the player
        float want;
        if (moved > 0.02) want = (float) Math.toDegrees(Math.atan2(-(x - px), z - pz));
        else want = (float) Math.toDegrees(Math.atan2(-(Mc.x(p) - x), Mc.z(p) - z));
        float diff = wrap(want - yaw);
        yaw += diff * 0.25f;

        // ---- emotion
        float hp = Mc.health(p);
        if (lastHealth >= 0 && hp < lastHealth - 0.01f) set(p.method_6065() != null ? "angry" : "sad", 3000);
        lastHealth = hp;
        double vel = Math.sqrt(Math.pow(Mc.x(p) - Mc.lastX(p), 2) + Math.pow(Mc.z(p) - Mc.lastZ(p), 2));
        if (vel > 0.01 || Mc.isSneaking(p) || Mc.mouseDown(0)) lastActive = now;
        if (now > emotionUntil) {
            String e;
            if (hp <= Mc.maxHealth(p) * 0.3f) e = "sad";
            else if (p.field_6017 > 4) e = "dizzy";                                    // falling far
            else if (Mc.isSneaking(p) && moved < 0.05) e = "love";
            else if (now - lastActive > 25000) e = "sleepy";
            else if (p.method_5624() && vel > 0.2) e = "excited";
            else if (now > nextCurious) { e = "curious"; emotionUntil = now + 3500; nextCurious = now + 25000 + (long) (Math.random() * 30000); }
            else e = "happy";
            emotion = e;
        }
        if (!emotion.equals(shownEmotion)) { shownEmotion = emotion; bubbleAt = now; }
        if (now > nextBlink) { blinkAt = now; nextBlink = now + 2200 + (long) (Math.random() * 3200); }
    }

    private static float wrap(float a) { a %= 360; if (a > 180) a -= 360; if (a < -180) a += 360; return a; }

    /** Top of the ground below (x, from, z), at most 5 blocks down; fallback = player feet. */
    private static double ground(double x, double from, double z, double fallback) {
        class_638 w = Mc.world();
        if (w == null) return fallback;
        class_2338.class_2339 pos = new class_2338.class_2339();
        int bx = (int) Math.floor(x), bz = (int) Math.floor(z);
        for (int by = (int) Math.floor(from); by > from - 6; by--) {
            pos.method_10103(bx, by, bz);
            class_2680 st = w.method_8320(pos);
            class_265 sh = st.method_26220(w, pos);
            if (!sh.method_1110()) {
                double top = by + sh.method_1107().field_1325;
                if (top <= from + 0.01) return top;
            }
        }
        return fallback;
    }

    // ------------------------------------------------------------------ rendering (inside R3 begin/end)
    private static final Map<String, class_2960> BUBBLES = new HashMap<>();

    public static void render(float td) {
        Cos.Item it = Cos.equipped(Cos.Slot.PET);
        if (it == null || !placed || Mc.player() == null) return;
        double rx = px + (x - px) * td, ry = py + (y - py) * td, rz = pz + (z - pz) * td;
        float ry2 = lastYaw + wrap(yaw - lastYaw) * td;
        long now = System.currentTimeMillis();
        Cos.A a = new Cos.A();
        a.time = (now % 3_600_000L) / 1000f;
        a.speed = speed;
        a.growth = growth();
        a.level = level();
        a.emotion = shownEmotion;
        float bt = (now - blinkAt) / 140f;
        a.blink = bt < 1 ? 1 - Math.abs(bt * 2 - 1) : 0;
        // look at the camera (in pet space)
        double lx = R3.camX() - rx, lz = R3.camZ() - rz, ly = R3.camY() - (ry + 0.4);
        double th = Math.toRadians(ry2);
        double fwd = lx * -Math.sin(th) + lz * Math.cos(th), right = lx * -Math.cos(th) + lz * -Math.sin(th);
        double ang = Math.atan2(right, Math.max(0.1, fwd));
        a.lookX = (float) Math.max(-1, Math.min(1, ang / 1.2));
        a.lookY = (float) Math.max(-1, Math.min(1, Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) / 1.0));
        int light = CosRender.light(rx, ry + 0.3, rz);
        CosRender.drawWorld(it.model, a, rx, ry, rz, ry2, 0, 0, 0.95f, light);

        // emotion bubble for 2.5 s after a change
        long age = now - bubbleAt;
        String bubble = bubbleFor(shownEmotion);
        if (bubble != null && age < 2600) {
            class_2960 tex = BUBBLES.get(bubble);
            if (tex == null) {
                tex = class_2960.method_60655("legoclient", "pet_bubble/" + bubble);
                try { Tx.register(tex, Bubbles.paint(bubble, 128), 128, 128, true); } catch (Throwable ignored) { }
                BUBBLES.put(bubble, tex);
            }
            float in = Math.min(1, age / 250f), out = age > 2200 ? 1 - (age - 2200) / 400f : 1;
            double pop = 1 + 0.25 * Math.sin(Math.min(1, age / 300.0) * Math.PI);
            double h = Pets.height(it.id, a.growth) / 16.0 * 0.95 + 0.35 + Math.sin(now / 300.0) * 0.03;
            R3.billboard(tex, rx, ry + h, rz, 0.42 * in * pop, R3.argb(0xFFFFFF, Math.max(0, out)));
        }
    }

    private static String bubbleFor(String e) {
        switch (e) {
            case "love": return "heart";
            case "sleepy": return "sleep";
            case "sad": return "cry";
            case "curious": return "think";
            case "excited": return "laugh";
            case "angry": return "fire";
            case "dizzy": return "zen";
            default: return null;
        }
    }
}
