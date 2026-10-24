package dev.lego.visual;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Tx;
import net.minecraft.class_2338;
import net.minecraft.class_2902;
import net.minecraft.class_2960;
import net.minecraft.class_638;
import net.minecraft.class_746;

/**
 * Ambient world visuals drawn with our own HD sprites (no vanilla particles -> always visible, smooth, cheap):
 * weather particles, fireflies, sky lanterns, light dust, soap bubbles, butterflies, rainbow, aurora.
 */
public final class Ambient {
    private Ambient() { }

    public static Weather weather;
    public static Fireflies fireflies;
    public static Lanterns lanterns;
    public static Motes motes;
    public static Bubbles bubbles;
    public static Butterflies butterflies;
    public static Rainbow rainbow;
    public static Aurora aurora;

    public static void registerAll() {
        weather = Modules.register(new Weather());
        fireflies = Modules.register(new Fireflies());
        lanterns = Modules.register(new Lanterns());
        motes = Modules.register(new Motes());
        bubbles = Modules.register(new Bubbles());
        butterflies = Modules.register(new Butterflies());
        rainbow = Modules.register(new Rainbow());
        aurora = Modules.register(new Aurora());
    }

    private static long last = 0;

    /** Inside R3 begin/end. */
    static void render() {
        long now = System.nanoTime();
        float dt = last == 0 ? 0.016f : Math.min(0.1f, (now - last) / 1e9f);
        last = now;
        if (aurora.enabled) aurora.render();
        if (rainbow.enabled) rainbow.render();
        if (lanterns.enabled) lanterns.render(dt);
        if (weather.enabled) weather.render(dt);
        if (motes.enabled) motes.render(dt);
        if (bubbles.enabled) bubbles.render(dt);
        if (butterflies.enabled) butterflies.render(dt);
        if (fireflies.enabled) fireflies.render(dt);
    }

    // ------------------------------------------------------------------ helpers
    private static final Map<String, class_2960> TEX = new HashMap<>();
    static final Random RND = new Random();

    static class_2960 tex(String id) {
        class_2960 t = TEX.get(id);
        if (t == null) {
            t = class_2960.method_60655("legoclient", "ambient/" + id);
            try { Tx.register(t, AmbientTex.paint(id), AmbientTex.S, AmbientTex.S, true); } catch (Throwable e) { dev.lego.LegoClient.LOG("Ambient: " + e); }
            TEX.put(id, t);
        }
        return t;
    }

    /** Highest motion-blocking block + 1 at x,z. */
    static int top(double x, double z) {
        class_638 w = Mc.world();
        if (w == null) return -64;
        try { return w.method_8624(class_2902.class_2903.field_13197, (int) Math.floor(x), (int) Math.floor(z)); } catch (Throwable t) { return -64; }
    }

    static boolean air(double x, double y, double z) {
        class_638 w = Mc.world();
        if (w == null) return true;
        try { return w.method_8320(class_2338.method_49637(x, y, z)).method_26220(w, class_2338.method_49637(x, y, z)).method_1110(); } catch (Throwable t) { return true; }
    }

    static double night() {
        long t = Math.floorMod(Mc.timeOfDay(), 24000L);
        if (t >= 13200 && t <= 22800) return 1;
        if (t > 12200 && t < 13200) return (t - 12200) / 1000.0;
        if (t > 22800 && t < 23800) return 1 - (t - 22800) / 1000.0;
        return 0;
    }

    /** A pool of simple particles: x,y,z, vx,vy,vz, age, life, seed, size. */
    static final class Pool {
        final double[][] p;
        int n;
        Pool(int max) { p = new double[max][10]; }
        double[] add() { if (n >= p.length) return null; return p[n++]; }
        void remove(int i) { double[] t = p[i]; p[i] = p[n - 1]; p[n - 1] = t; n--; }
    }

    // ================================================================== weather particles
    public static final class Weather extends Module {
        static final String[] TYPES = {"Schnee", "Kirschblüten", "Herbstlaub", "Asche", "Glitzer", "Sternenstaub"};
        static final String[] TEXS = {"snow", "petal", "leaf", "ash", "glitter", "stardust"};
        final Setting.Mode type = add(new Setting.Mode("Art", 0, TYPES));
        final Setting.Num amount = add(new Setting.Num("Menge", 50, 1500, 50, 500, ""));
        final Setting.Num size = add(new Setting.Num("Größe", 0.5, 2, 0.1, 1, "x"));
        final Setting.Num wind = add(new Setting.Num("Wind", 0, 3, 0.1, 0.6, ""));
        final Setting.Bool roof = add(new Setting.Bool("Nicht unter Dächern", true));
        private final Pool pool = new Pool(1500);
        private float spawnAcc;

        Weather() { super("snowfall", "Schnee & Blüten", "HD-Schnee, Kirschblüten, Herbstlaub, Glitzer ... fallen um dich herum", Category.WORLD_FX); icon("snow"); fresh(); }

        void render(float dt) {
            class_746 me = Mc.player();
            if (me == null) return;
            double cx = R3.camX(), cy = R3.camY(), cz = R3.camZ();
            int max = amount.getI();
            spawnAcc += dt * max / 5f;     // ~5 s life
            while (spawnAcc >= 1 && pool.n < max) {
                spawnAcc -= 1;
                double x = cx + (RND.nextDouble() - 0.5) * 36, z = cz + (RND.nextDouble() - 0.5) * 36, y = cy + 4 + RND.nextDouble() * 14;
                if (roof.get() && top(x, z) > y) continue;
                double[] q = pool.add();
                if (q == null) break;
                q[0] = x; q[1] = y; q[2] = z;
                q[3] = 0; q[4] = -(0.8 + RND.nextDouble() * 0.9) * (type.index == 3 ? 0.6 : 1); q[5] = 0;
                q[6] = 0; q[7] = 8 + RND.nextDouble() * 6; q[8] = RND.nextDouble() * 1000; q[9] = 0.6 + RND.nextDouble() * 0.8;
            }
            if (spawnAcc > 5) spawnAcc = 5;
            double t = System.currentTimeMillis() / 1000.0;
            class_2960 tx = tex(TEXS[type.index]);
            boolean spin = type.index == 1 || type.index == 2;
            double w = wind.get();
            for (int i = pool.n - 1; i >= 0; i--) {
                double[] q = pool.p[i];
                q[6] += dt;
                double sway = Math.sin(t * 1.3 + q[8]) * 0.6 * (spin ? 1.6 : 1);
                q[0] += (w * 0.8 + sway) * dt;
                q[2] += (Math.cos(t * 0.9 + q[8] * 1.7) * 0.4 + w * 0.3) * dt;
                q[1] += q[4] * dt;
                double dx = q[0] - cx, dz = q[2] - cz;
                if (q[6] > q[7] || q[1] < cy - 12 || dx * dx + dz * dz > 22 * 22 || (roof.get() && q[1] < top(q[0], q[2]))) { pool.remove(i); continue; }
                double fade = Math.min(1, Math.min(q[6] / 0.6, (q[7] - q[6]) / 0.8));
                double s = 0.12 * q[9] * size.get() * (type.index == 4 || type.index == 5 ? 0.8 : 1);
                int col = type.index == 4 || type.index == 5 ? R3.argb(0xFFFFFF, fade * (0.5 + 0.5 * Math.sin(t * 6 + q[8]))) : R3.argb(0xFFFFFF, fade * 0.95);
                if (spin) R3.billboardRot(tx, q[0], q[1], q[2], s * 1.3, t * 1.6 + q[8], col);
                else R3.billboardRot(tx, q[0], q[1], q[2], s, q[8], col);
            }
        }

        @Override public void onDisable() { pool.n = 0; }
    }

    // ================================================================== fireflies
    public static final class Fireflies extends Module {
        final Setting.Num count = add(new Setting.Num("Anzahl", 10, 200, 10, 60, ""));
        final Setting.Color color = add(new Setting.Color("Farbe", 10));
        final Setting.Bool nightOnly = add(new Setting.Bool("Nur nachts", false));
        final Setting.Num size = add(new Setting.Num("Größe", 0.5, 2, 0.1, 1, "x"));
        private final Pool pool = new Pool(200);

        Fireflies() { super("fireflies", "Glühwürmchen", "Leuchtende Glühwürmchen schwirren um dich herum", Category.WORLD_FX); icon("firefly"); fresh(); }

        void render(float dt) {
            class_746 me = Mc.player();
            if (me == null) return;
            double vis = nightOnly.get() ? night() : 1;
            if (vis <= 0.01) return;
            double px = Mc.x(me), py = Mc.y(me), pz = Mc.z(me);
            int tries = 0;
            while (pool.n < count.getI() && tries++ < 8) {
                double x = px + (RND.nextDouble() - 0.5) * 28, z = pz + (RND.nextDouble() - 0.5) * 28;
                int ground = top(x, z);
                double y = (ground > py + 6 || ground < py - 8 ? py : ground) + 0.4 + RND.nextDouble() * 2.6;
                if (!air(x, y, z)) continue;
                double[] q = pool.add();
                q[0] = x; q[1] = y; q[2] = z; q[6] = 0; q[7] = 12 + RND.nextDouble() * 20; q[8] = RND.nextDouble() * 1000; q[9] = 0.7 + RND.nextDouble() * 0.6;
                q[3] = q[4] = q[5] = 0;
            }
            double t = System.currentTimeMillis() / 1000.0;
            class_2960 glow = tex("glow"), core = tex("core");
            for (int i = pool.n - 1; i >= 0; i--) {
                double[] q = pool.p[i];
                q[6] += dt;
                double dx = q[0] - px, dz = q[2] - pz;
                if (q[6] > q[7] || dx * dx + dz * dz > 20 * 20 || i >= count.getI()) { pool.remove(i); continue; }
                // smooth wandering (sum of sines = soft curl-like motion)
                double s = q[8];
                q[3] += (Math.sin(t * 0.7 + s) * 0.9 - q[3]) * dt;
                q[4] += (Math.sin(t * 0.9 + s * 1.3) * 0.35 - q[4]) * dt;
                q[5] += (Math.cos(t * 0.6 + s * 0.7) * 0.9 - q[5]) * dt;
                double nx = q[0] + q[3] * dt, ny = q[1] + q[4] * dt, nz = q[2] + q[5] * dt;
                if (air(nx, ny, nz)) { q[0] = nx; q[1] = ny; q[2] = nz; } else { q[3] = -q[3]; q[4] = Math.abs(q[4]); q[5] = -q[5]; }
                double blink = 0.35 + 0.65 * Math.pow(Math.max(0, Math.sin(t * 1.7 + s * 3)), 2);
                double fade = Math.min(1, Math.min(q[6] / 1.2, (q[7] - q[6]) / 1.5)) * vis;
                int rgb = color.argb(s) & 0xFFFFFF;
                double sz = size.get() * q[9];
                R3.glowBillboard(glow, q[0], q[1], q[2], 0.75 * sz, R3.argb(rgb, 0.45 * blink * fade));
                R3.glowBillboard(core, q[0], q[1], q[2], 0.16 * sz, R3.argb(0xFFFFF0, 0.95 * blink * fade));
            }
        }

        @Override public void onDisable() { pool.n = 0; }
    }

    // ================================================================== sky lanterns
    public static final class Lanterns extends Module {
        final Setting.Num count = add(new Setting.Num("Anzahl", 5, 120, 5, 40, ""));
        final Setting.Bool nightOnly = add(new Setting.Bool("Nur nachts", true));
        private final Pool pool = new Pool(120);
        Lanterns() { super("lanterns", "Himmelslaternen", "Leuchtende Laternen steigen langsam in den Himmel", Category.WORLD_FX); icon("flame"); fresh(); }

        void render(float dt) {
            class_746 me = Mc.player();
            if (me == null) return;
            double vis = nightOnly.get() ? night() : 1;
            if (vis <= 0.01) return;
            double px = Mc.x(me), py = Mc.y(me), pz = Mc.z(me);
            while (pool.n < count.getI()) {
                double a = RND.nextDouble() * Math.PI * 2, r = 20 + RND.nextDouble() * 70;
                double[] q = pool.add();
                q[0] = px + Math.cos(a) * r; q[2] = pz + Math.sin(a) * r; q[1] = py - 4 + RND.nextDouble() * 50;
                q[6] = 0; q[7] = 40 + RND.nextDouble() * 40; q[8] = RND.nextDouble() * 1000; q[9] = 0.8 + RND.nextDouble() * 0.5;
            }
            double t = System.currentTimeMillis() / 1000.0;
            class_2960 lt = tex("lantern"), glow = tex("glow");
            for (int i = pool.n - 1; i >= 0; i--) {
                double[] q = pool.p[i];
                q[6] += dt;
                q[1] += (0.45 + 0.1 * Math.sin(q[8])) * dt;
                q[0] += Math.sin(t * 0.2 + q[8]) * 0.25 * dt;
                q[2] += Math.cos(t * 0.17 + q[8]) * 0.25 * dt;
                double dx = q[0] - px, dz = q[2] - pz;
                if (q[6] > q[7] || q[1] > py + 120 || dx * dx + dz * dz > 110 * 110 || i >= count.getI()) { pool.remove(i); continue; }
                double fade = Math.min(1, Math.min(q[6] / 3, (q[7] - q[6]) / 6)) * vis;
                double flick = 0.85 + 0.15 * Math.sin(t * 9 + q[8]);
                R3.glowBillboard(glow, q[0], q[1], q[2], 3.2 * q[9], R3.argb(0xFF9A40, 0.3 * fade * flick));
                R3.billboard(lt, q[0], q[1], q[2], 1.3 * q[9], R3.argb(0xFFFFFF, fade));
            }
        }
    }

    // ================================================================== light dust
    public static final class Motes extends Module {
        final Setting.Num count = add(new Setting.Num("Anzahl", 20, 400, 20, 140, ""));
        final Setting.Color color = add(new Setting.Color("Farbe", 2));
        private final Pool pool = new Pool(400);
        Motes() { super("motes", "Lichtstaub", "Feiner leuchtender Staub schwebt in der Luft (sehr cinematic)", Category.WORLD_FX); icon("sparkle"); fresh(); }

        void render(float dt) {
            double cx = R3.camX(), cy = R3.camY(), cz = R3.camZ();
            while (pool.n < count.getI()) {
                double[] q = pool.add();
                q[0] = cx + (RND.nextDouble() - 0.5) * 16; q[1] = cy + (RND.nextDouble() - 0.5) * 8; q[2] = cz + (RND.nextDouble() - 0.5) * 16;
                q[6] = 0; q[7] = 6 + RND.nextDouble() * 8; q[8] = RND.nextDouble() * 1000; q[9] = 0.5 + RND.nextDouble();
            }
            double t = System.currentTimeMillis() / 1000.0;
            class_2960 core = tex("core");
            for (int i = pool.n - 1; i >= 0; i--) {
                double[] q = pool.p[i];
                q[6] += dt;
                q[0] += Math.sin(t * 0.3 + q[8]) * 0.08 * dt;
                q[1] += (0.05 + Math.sin(t * 0.5 + q[8]) * 0.05) * dt;
                q[2] += Math.cos(t * 0.35 + q[8]) * 0.08 * dt;
                double dx = q[0] - cx, dz = q[2] - cz;
                if (q[6] > q[7] || dx * dx + dz * dz > 12 * 12 || i >= count.getI()) { pool.remove(i); continue; }
                double fade = Math.min(1, Math.min(q[6] / 1.5, (q[7] - q[6]) / 1.5));
                double tw = 0.5 + 0.5 * Math.sin(t * 2.3 + q[8] * 5);
                R3.glowBillboard(core, q[0], q[1], q[2], 0.07 * q[9], R3.argb(color.argb(q[8]) & 0xFFFFFF, 0.8 * fade * tw));
            }
        }
    }

    // ================================================================== soap bubbles
    public static final class Bubbles extends Module {
        final Setting.Num count = add(new Setting.Num("Anzahl", 5, 100, 5, 30, ""));
        private final Pool pool = new Pool(100);
        Bubbles() { super("bubbles", "Seifenblasen", "Schillernde Seifenblasen schweben um dich", Category.WORLD_FX); icon("aura"); fresh(); }

        void render(float dt) {
            class_746 me = Mc.player();
            if (me == null) return;
            double px = Mc.x(me), py = Mc.y(me), pz = Mc.z(me);
            while (pool.n < count.getI()) {
                double[] q = pool.add();
                q[0] = px + (RND.nextDouble() - 0.5) * 14; q[1] = py + RND.nextDouble() * 2; q[2] = pz + (RND.nextDouble() - 0.5) * 14;
                q[6] = 0; q[7] = 6 + RND.nextDouble() * 8; q[8] = RND.nextDouble() * 1000; q[9] = 0.5 + RND.nextDouble();
            }
            double t = System.currentTimeMillis() / 1000.0;
            class_2960 b = tex("bubble");
            for (int i = pool.n - 1; i >= 0; i--) {
                double[] q = pool.p[i];
                q[6] += dt;
                q[1] += 0.35 * dt;
                q[0] += Math.sin(t * 0.8 + q[8]) * 0.3 * dt;
                q[2] += Math.cos(t * 0.7 + q[8]) * 0.3 * dt;
                if (q[6] > q[7] || i >= count.getI()) { pool.remove(i); continue; }
                double fade = Math.min(1, Math.min(q[6] / 0.8, (q[7] - q[6]) / 0.3));
                double wob = 1 + Math.sin(t * 5 + q[8]) * 0.04;
                R3.billboard(b, q[0], q[1], q[2], 0.45 * q[9] * wob, R3.argb(0xFFFFFF, fade));
            }
        }
    }

    // ================================================================== butterflies
    public static final class Butterflies extends Module {
        final Setting.Num count = add(new Setting.Num("Anzahl", 2, 40, 1, 10, ""));
        private final Pool pool = new Pool(40);
        Butterflies() { super("butterflies", "Schmetterlinge", "Bunte Schmetterlinge flattern um dich herum (tagsüber)", Category.WORLD_FX); icon("wing"); fresh(); }

        void render(float dt) {
            class_746 me = Mc.player();
            if (me == null) return;
            double day = 1 - night();
            if (day < 0.01) return;
            double px = Mc.x(me), py = Mc.y(me), pz = Mc.z(me);
            while (pool.n < count.getI()) {
                double[] q = pool.add();
                q[0] = px + (RND.nextDouble() - 0.5) * 16; q[1] = py + 0.5 + RND.nextDouble() * 2; q[2] = pz + (RND.nextDouble() - 0.5) * 16;
                q[6] = 0; q[7] = 15 + RND.nextDouble() * 20; q[8] = RND.nextDouble() * 1000; q[9] = 0.7 + RND.nextDouble() * 0.5;
            }
            double t = System.currentTimeMillis() / 1000.0;
            class_2960 f0 = tex("butterfly0"), f1 = tex("butterfly1");
            for (int i = pool.n - 1; i >= 0; i--) {
                double[] q = pool.p[i];
                q[6] += dt;
                double s = q[8];
                q[0] += Math.sin(t * 0.6 + s) * 0.9 * dt;
                q[1] += Math.sin(t * 2.2 + s) * 0.5 * dt;
                q[2] += Math.cos(t * 0.5 + s * 1.3) * 0.9 * dt;
                double dx = q[0] - px, dz = q[2] - pz;
                if (q[6] > q[7] || dx * dx + dz * dz > 18 * 18 || i >= count.getI()) { pool.remove(i); continue; }
                double fade = Math.min(1, Math.min(q[6], (q[7] - q[6]))) * day;
                boolean open = Math.sin(t * 16 + s) > 0;
                R3.billboardRot(open ? f0 : f1, q[0], q[1], q[2], 0.32 * q[9], Math.sin(t * 0.8 + s) * 0.3, R3.argb(0xFFFFFF, fade));
            }
        }
    }

    // ================================================================== rainbow
    public static final class Rainbow extends Module {
        final Setting.Mode when = add(new Setting.Mode("Zeigen", 0, "Nach Regen", "Immer (am Tag)"));
        final Setting.Num strength = add(new Setting.Num("Stärke", 10, 100, 5, 55, "%"));
        private long rainEnded = 0;
        private boolean wasRaining;
        Rainbow() { super("rainbow", "Regenbogen", "Ein echter Regenbogen gegenüber der Sonne - nach Regen oder immer", Category.WORLD_FX); icon("rain"); fresh(); }

        @Override public void tick() {
            boolean r = false;
            try { r = Mc.world().method_8430(1f) > 0.2f; } catch (Throwable ignored) { }
            if (wasRaining && !r) rainEnded = System.currentTimeMillis();
            wasRaining = r;
        }

        void render() {
            double day = 1 - night();
            if (day < 0.05) return;
            double vis = day * strength.get() / 100.0;
            if (when.index == 0) {
                long since = System.currentTimeMillis() - rainEnded;
                double rain = wasRaining ? 0.5 : (since < 240_000 ? 1 - since / 240_000.0 : 0);
                vis *= rain;
            }
            if (vis < 0.01) return;
            long tod = Math.floorMod(Mc.timeOfDay(), 24000L);
            // the sun moves east -> west; the rainbow sits opposite the sun
            double sunAng = (tod / 24000.0) * Math.PI * 2;          // 0 = sunrise-ish
            double east = Math.cos(sunAng) >= 0 ? 1 : -1;
            double far = 180;
            try { far = Math.min(far, Mc.mc().field_1773.method_32796() * 0.45); } catch (Throwable ignored) { }
            double cx = R3.camX() - east * far * 0.9, cy = R3.camY() - far * 0.25, cz = R3.camZ();
            double[] u = {0, 0, 1}, w = {0, 1, 0};
            int[] cols = {0xFF3B3B, 0xFF9A2E, 0xFFE83A, 0x4DDA5A, 0x3BA5FF, 0x5B5BFF, 0xA24BFF};
            double r0 = far * 0.62, band = far * 0.018;
            for (int i = 0; i < cols.length; i++) {
                double ra = r0 + (cols.length - 1 - i) * band;
                arc(cx, cy, cz, u, w, ra, ra + band, R3.argb(cols[i], 0.35 * vis));
            }
        }

        private static void arc(double x, double y, double z, double[] u, double[] w, double r0, double r1, int c) {
            int seg = 48;
            for (int i = 0; i < seg; i++) {
                double a0 = Math.PI * i / seg, a1 = Math.PI * (i + 1) / seg;
                double c0 = Math.cos(a0), s0 = Math.sin(a0), c1 = Math.cos(a1), s1 = Math.sin(a1);
                R3.quad(x + (u[0] * c0 + w[0] * s0) * r0, y + (u[1] * c0 + w[1] * s0) * r0, z + (u[2] * c0 + w[2] * s0) * r0,
                        x + (u[0] * c0 + w[0] * s0) * r1, y + (u[1] * c0 + w[1] * s0) * r1, z + (u[2] * c0 + w[2] * s0) * r1,
                        x + (u[0] * c1 + w[0] * s1) * r1, y + (u[1] * c1 + w[1] * s1) * r1, z + (u[2] * c1 + w[2] * s1) * r1,
                        x + (u[0] * c1 + w[0] * s1) * r0, y + (u[1] * c1 + w[1] * s1) * r0, z + (u[2] * c1 + w[2] * s1) * r0,
                        R3.argb(c, 0) | 0, c, c, R3.argb(c, 0));
            }
        }
    }

    // ================================================================== aurora curtains
    public static final class Aurora extends Module {
        final Setting.Mode palette = add(new Setting.Mode("Farben", 0, "Grün", "Lila & Pink", "Regenbogen"));
        final Setting.Num strength = add(new Setting.Num("Stärke", 10, 100, 5, 60, "%"));
        Aurora() { super("aurora", "Nordlicht", "Wehende Polarlicht-Vorhänge am Nachthimmel (echt 3D)", Category.WORLD_FX); icon("sky"); fresh(); }

        void render() {
            double vis = night() * strength.get() / 100.0;
            if (vis < 0.01) return;
            double t = System.currentTimeMillis() / 1000.0;
            double cx = R3.camX(), cy = R3.camY(), cz = R3.camZ();
            double far = 160;
            try { far = Math.min(far, Mc.mc().field_1773.method_32796() * 0.45); } catch (Throwable ignored) { }
            for (int band = 0; band < 3; band++) {
                double z0 = -far * (0.55 + band * 0.12), base = cy + far * (0.32 + band * 0.05), h = far * (0.22 + band * 0.04);
                int seg = 64;
                for (int i = 0; i < seg; i++) {
                    double f0 = i / (double) seg, f1 = (i + 1) / (double) seg;
                    double x0 = (f0 - 0.5) * far * 2.4, x1 = (f1 - 0.5) * far * 2.4;
                    double w0 = Math.sin(f0 * 9 + t * 0.35 + band) * far * 0.08 + Math.sin(f0 * 23 - t * 0.6) * far * 0.02;
                    double w1 = Math.sin(f1 * 9 + t * 0.35 + band) * far * 0.08 + Math.sin(f1 * 23 - t * 0.6) * far * 0.02;
                    double i0 = 0.55 + 0.45 * Math.sin(f0 * 14 + t * 0.9 + band * 2), i1 = 0.55 + 0.45 * Math.sin(f1 * 14 + t * 0.9 + band * 2);
                    double edge0 = Math.sin(f0 * Math.PI), edge1 = Math.sin(f1 * Math.PI);
                    int top0 = col(f0, band, true), bot0 = col(f0, band, false), top1 = col(f1, band, true), bot1 = col(f1, band, false);
                    double a0 = vis * i0 * edge0 * 0.55, a1 = vis * i1 * edge1 * 0.55;
                    R3.quad(cx + x0, base, cz + z0 + w0, cx + x1, base, cz + z0 + w1,
                            cx + x1, base + h, cz + z0 + w1 * 1.3, cx + x0, base + h, cz + z0 + w0 * 1.3,
                            R3.argb(bot0, a0), R3.argb(bot1, a1), R3.argb(top1, 0), R3.argb(top0, 0));
                }
            }
        }

        private int col(double f, int band, boolean top) {
            switch (palette.index) {
                case 1: return top ? 0xFF4FD8 : 0x9B5BFF;
                case 2: return java.awt.Color.HSBtoRGB((float) (f * 1.5 + band * 0.2 + System.currentTimeMillis() / 20000.0), 0.7f, 1f) & 0xFFFFFF;
                default: return top ? 0x8A5BFF : 0x3DFFA0;
            }
        }
    }
}
