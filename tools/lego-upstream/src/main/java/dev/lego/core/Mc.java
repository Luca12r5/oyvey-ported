package dev.lego.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import net.minecraft.class_1041;
import net.minecraft.class_1293;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1702;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2394;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_5321;
import net.minecraft.class_5498;
import net.minecraft.class_634;
import net.minecraft.class_638;
import net.minecraft.class_640;
import net.minecraft.class_642;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.minecraft.class_7923;

/**
 * All Minecraft access in one place (Minecraft 1.21.11, intermediary names).
 * class_310 MinecraftClient, class_746 ClientPlayerEntity, class_638 ClientWorld, class_1297 Entity,
 * class_1309 LivingEntity, class_1657 PlayerEntity, class_1799 ItemStack, class_243 Vec3d
 */
public final class Mc {
    private Mc() { }

    public static class_310 mc() { return class_310.method_1551(); }
    public static class_746 player() { return mc().field_1724; }
    public static class_638 world() { return mc().field_1687; }
    public static boolean inGame() { return player() != null && world() != null; }

    public static int fps() { return mc().method_47599(); }

    public static float tickDelta() {
        try { return mc().method_61966().method_60637(false); } catch (Throwable t) { return 1f; }
    }

    public static int ping() {
        class_746 p = player();
        class_634 nh = mc().method_1562();
        if (p == null || nh == null) return -1;
        class_640 e = nh.method_2871(p.method_5667());
        return e == null ? -1 : e.method_2959();
    }

    public static String serverAddress() {
        if (mc().method_1542()) return "Einzelspieler";
        class_642 s = mc().method_1558();
        return s == null ? "-" : s.field_3761;
    }

    public static void command(String cmd) {
        class_634 nh = mc().method_1562();
        if (nh != null) nh.method_45730(cmd);
    }

    public static void actionBar(String msg) {
        class_746 p = player();
        if (p != null) p.method_7353(class_2561.method_43470(msg), true);
    }

    public static void chat(String msg) {
        class_746 p = player();
        if (p != null) p.method_7353(class_2561.method_43470(msg), false);
    }

    // ------------------------------------------------------------------ entity helpers
    public static double x(class_1297 e) { return e.method_23317(); }
    public static double y(class_1297 e) { return e.method_23318(); }
    public static double z(class_1297 e) { return e.method_23321(); }
    public static double lastX(class_1297 e) { return e.field_6014; }
    public static double lastY(class_1297 e) { return e.field_6036; }
    public static double lastZ(class_1297 e) { return e.field_5969; }
    /** Interpolated render position. */
    public static double[] lerpPos(class_1297 e, float td) {
        return new double[]{lastX(e) + (x(e) - lastX(e)) * td, lastY(e) + (y(e) - lastY(e)) * td, lastZ(e) + (z(e) - lastZ(e)) * td};
    }
    public static float yaw(class_1297 e) { return e.method_36454(); }
    public static float pitch(class_1297 e) { return e.method_36455(); }
    public static float height(class_1297 e) { return e.method_17682(); }
    public static float width(class_1297 e) { return e.method_17681(); }
    public static boolean onGround(class_1297 e) { return e.method_24828(); }
    public static int id(class_1297 e) { return e.method_5628(); }
    public static String name(class_1297 e) { return e.method_5477().getString(); }
    public static double[] velocity(class_1297 e) { class_243 v = e.method_18798(); return new double[]{v.field_1352, v.field_1351, v.field_1350}; }
    public static boolean isPlayer(class_1297 e) { return e instanceof class_1657; }
    public static boolean isLiving(class_1297 e) { return e instanceof class_1309; }
    public static boolean isEndCrystal(class_1297 e) { return e instanceof net.minecraft.class_1511; }
    public static boolean isSpectator(class_1297 e) { return e.method_7325(); }
    public static boolean isSneaking(class_1297 e) { return e.method_5715(); }

    public static float health(class_1309 e) { return e.method_6032(); }
    public static float maxHealth(class_1309 e) { return e.method_6063(); }
    public static float absorption(class_1309 e) { return e.method_6067(); }
    public static float bodyYaw(class_1309 e) { return e.field_6283; }
    public static float lastBodyYaw(class_1309 e) { return e.field_6220; }
    public static void setBodyYaw(class_1309 e, float v) { e.field_6283 = v; }

    public static List<class_1297> entities() {
        List<class_1297> out = new ArrayList<>();
        class_638 w = world();
        if (w == null) return out;
        for (class_1297 e : w.method_18112()) out.add(e);
        return out;
    }

    @SuppressWarnings("unchecked")
    public static List<class_1657> players() {
        class_638 w = world();
        if (w == null) return new ArrayList<>();
        return new ArrayList<>((List<class_1657>) (List<?>) w.method_18456());
    }

    // ------------------------------------------------------------------ inventory
    public static int invSize() { class_746 p = player(); return p == null ? 0 : p.method_31548().method_5439(); }
    public static class_1799 invStack(int i) { class_746 p = player(); return p == null ? null : p.method_31548().method_5438(i); }
    /** 36 feet, 37 legs, 38 chest, 39 head, 40 offhand. */
    public static class_1799 armor(int slot) { return invStack(slot); }
    public static boolean empty(class_1799 s) { return s == null || s.method_7960(); }
    public static int count(class_1799 s) { return empty(s) ? 0 : s.method_7947(); }
    public static String itemKey(class_1799 s) { return empty(s) ? "" : s.method_7909().method_7876(); }
    public static int damage(class_1799 s) { return s.method_7919(); }
    public static int maxDamage(class_1799 s) { return s.method_7936(); }
    public static boolean damageable(class_1799 s) { return s.method_7963(); }

    /** Counts items whose translation key ends with the given id (e.g. "totem_of_undying"). */
    public static int countItem(String idPath) {
        int n = 0, size = invSize();
        for (int i = 0; i < size; i++) {
            class_1799 s = invStack(i);
            if (!empty(s) && itemKey(s).endsWith("." + idPath)) n += count(s);
        }
        return n;
    }

    private static final java.util.Map<String, class_1799> ICONS = new java.util.HashMap<>();

    /** An ItemStack for an item id (for HUD icons), cached. */
    public static class_1799 icon(String idPath) {
        return ICONS.computeIfAbsent(idPath, k -> {
            try {
                Object item = class_7923.field_41178.method_63535(class_2960.method_60655("minecraft", k));
                return item instanceof class_1792 ? new class_1799((class_1792) item) : null;
            } catch (Throwable t) {
                return null;
            }
        });
    }

    public static Collection<class_1293> effects() {
        class_746 p = player();
        return p == null ? new ArrayList<>() : p.method_6026();
    }

    public static int food() { class_746 p = player(); return p == null ? 0 : p.method_7344().method_7586(); }
    public static float saturation() {
        class_746 p = player();
        if (p == null) return 0;
        class_1702 h = p.method_7344();
        return h.method_7589();
    }

    public static String biome() {
        class_746 p = player();
        class_638 w = world();
        if (p == null || w == null) return "-";
        try {
            class_2338 pos = p.method_24515();
            class_6880<?> b = w.method_23753(pos);
            Optional<?> key = b.method_40230();
            if (key.isPresent()) return ((class_5321<?>) key.get()).method_29177().method_12832();
        } catch (Throwable ignored) { }
        return "-";
    }

    public static long timeOfDay() { class_638 w = world(); return w == null ? 0 : w.method_8532(); }

    public static void setTimeOfDay(long t) {
        class_638 w = world();
        if (w != null) w.method_28104().method_165(t);
    }

    /** Block light level at the player's feet (-1 = unknown). */
    public static int blockLight() {
        class_746 p = player();
        class_638 w = world();
        if (p == null || w == null) return -1;
        try { return w.method_8314(net.minecraft.class_1944.field_9282, p.method_24515()); } catch (Throwable t) { return -1; }
    }

    public static void clipboard(String s) {
        try { mc().field_1774.method_1455(s); } catch (Throwable ignored) { }
    }

    public static net.minecraft.class_315 options() { return mc().field_1690; }

    public static boolean windowFocused() { try { return mc().method_1569(); } catch (Throwable t) { return true; } }

    /** Registry path of an entity type, e.g. "arrow". */
    public static String typeId(class_1297 e) {
        try { return class_7923.field_41177.method_10221(e.method_5864()).method_12832(); } catch (Throwable t) { return ""; }
    }

    public static boolean isDead(class_1309 e) { return e.method_29504(); }

    public static void clearWeather() {
        class_638 w = world();
        if (w == null) return;
        w.method_8519(0f);
        w.method_8496(0f);
    }

    // ------------------------------------------------------------------ particles
    private static final java.util.Map<String, class_2394> PARTICLES = new java.util.HashMap<>();

    public static class_2394 particle(String id) {
        return PARTICLES.computeIfAbsent(id, k -> {
            try {
                Object o = class_7923.field_41180.method_63535(class_2960.method_60655("minecraft", k));
                return o instanceof class_2394 ? (class_2394) o : null;
            } catch (Throwable t) {
                return null;
            }
        });
    }

    public static void spawn(String id, double x, double y, double z, double vx, double vy, double vz) {
        class_2394 p = particle(id);
        if (p == null) return;
        try { mc().field_1713.method_3056(p, x, y, z, vx, vy, vz); } catch (Throwable ignored) { }
    }

    // ------------------------------------------------------------------ status effects (fullbright)
    private static class_6880<?> nightVision;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static class_6880 nightVision() {
        if (nightVision == null) {
            try {
                Optional<?> e = class_7923.field_41174.method_10223(class_2960.method_60655("minecraft", "night_vision"));
                nightVision = (class_6880<?>) e.orElse(null);
            } catch (Throwable ignored) { }
        }
        return nightVision;
    }

    // ------------------------------------------------------------------ input / camera
    public static class_1041 window() { return mc().method_22683(); }
    public static boolean keyDown(int key) {
        if (key < 0) return false;
        try {
            if (key <= 7) return org.lwjgl.glfw.GLFW.glfwGetMouseButton(window().method_4490(), key) == 1;
            return class_3675.method_15987(window(), key);
        } catch (Throwable t) {
            return false;
        }
    }
    public static boolean mouseDown(int button) {
        try { return org.lwjgl.glfw.GLFW.glfwGetMouseButton(window().method_4490(), button) == 1; } catch (Throwable t) { return false; }
    }
    public static boolean firstPerson() {
        class_5498 p = mc().field_1690.method_31044();
        return p == null || p.method_31034();
    }
    public static class_5498 perspective() { return mc().field_1690.method_31044(); }
    public static void perspective(class_5498 p) { mc().field_1690.method_31043(p); }
    public static boolean screenOpen() { return mc().field_1755 != null; }

    public static boolean pressed(net.minecraft.class_304 k) { return k != null && k.method_1434(); }
}
