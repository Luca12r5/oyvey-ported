package dev.lego.hud;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.function.Supplier;

import dev.lego.core.Mc;
import dev.lego.core.Modules;
import net.minecraft.class_638;
import net.minecraft.class_746;

/** Paket 4: 70 more HUD panels - every one shows real game / system data (value + unit). */
public final class HudPack {
    private HudPack() { }

    private static int pos = 0;
    /** Every panel gets its own default spot (5 columns x 16 rows), so two of them never start on top of each other. */
    private static double[] spot() { int i = pos++; return new double[]{0.02 + (i / 16) * 0.19, 0.1 + (i % 16) * 0.052}; }

    /** A panel from a supplier of {value, unit}. */
    static final class Fn extends HudModules.Simple {
        private final Supplier<String[]> f;
        Runnable off;
        @Override public void onDisable() { if (off != null) off.run(); }
        Fn(String id, String name, String desc, String icon, Supplier<String[]> f, double[] at) { super("h4_" + id, name, desc, icon, at[0], at[1]); this.f = f; fresh(); }
        @Override void compute() {
            String[] r;
            try { r = f.get(); } catch (Throwable t) { r = null; }
            if (r == null) r = new String[]{"-", ""};
            value = r[0];
            unit = r.length > 1 ? r[1] : "";
        }
    }

    private static Fn add(String id, String name, String desc, String icon, Supplier<String[]> f) { return Modules.register(new Fn(id, name, desc, icon, f, spot())); }

    private static String[] v(Object value, String unit) { return new String[]{String.valueOf(value), unit}; }
    private static String f1(double d) { return String.format(Locale.ROOT, "%.1f", d); }
    private static String f2(double d) { return String.format(Locale.ROOT, "%.2f", d); }

    // ------------------------------------------------------------------ session statistics (updated every tick by tick())
    static double traveled, maxY = Double.NaN, minY = Double.NaN, lastX = Double.NaN, lastZ, damageTaken, healed, lastHp = -1, topSpeed;
    static int jumps, minFps = Integer.MAX_VALUE, maxFps;
    static long fpsSum, fpsN, lastWorldTime = -1, lastWorldReal;
    static double tps = 20;
    static boolean wasOnGround = true;
    static final long START = System.currentTimeMillis();

    /** Called every client tick (LegoClient). */
    public static void tick() {
        class_746 p = Mc.player();
        int fps = Mc.fps();
        if (fps > 0 && p != null) { minFps = Math.min(minFps, fps); maxFps = Math.max(maxFps, fps); fpsSum += fps; fpsN++; }
        if (p == null) { lastX = Double.NaN; lastHp = -1; return; }
        double x = Mc.x(p), y = Mc.y(p), z = Mc.z(p);
        if (!Double.isNaN(lastX)) {
            double d = Math.hypot(x - lastX, z - lastZ);
            if (d < 20) { traveled += d; topSpeed = Math.max(topSpeed, d * 20 * 3.6); }
        }
        lastX = x; lastZ = z;
        maxY = Double.isNaN(maxY) ? y : Math.max(maxY, y);
        minY = Double.isNaN(minY) ? y : Math.min(minY, y);
        boolean ground = Mc.onGround(p);
        if (wasOnGround && !ground && Mc.velocity(p)[1] > 0.3) jumps++;
        wasOnGround = ground;
        float hp = Mc.health(p);
        if (lastHp >= 0) { if (hp < lastHp) damageTaken += lastHp - hp; else if (hp > lastHp) healed += hp - lastHp; }
        lastHp = hp;
        class_638 w = Mc.world();
        if (w != null) {
            long wt = w.method_8532(), now = System.currentTimeMillis();
            if (lastWorldTime < 0 || wt < lastWorldTime) { lastWorldTime = wt; lastWorldReal = now; }
            else if (now - lastWorldReal >= 3000) {
                double measured = (wt - lastWorldTime) / ((now - lastWorldReal) / 1000.0);
                if (wt != lastWorldTime) tps = tps * 0.6 + Math.min(20, measured) * 0.4;
                lastWorldTime = wt; lastWorldReal = now;
            }
        }
    }

    // ------------------------------------------------------------------ helpers
    private static String dur(long ms) {
        long s = ms / 1000;
        return s >= 3600 ? String.format(Locale.ROOT, "%d:%02d:%02d", s / 3600, s / 60 % 60, s % 60) : String.format(Locale.ROOT, "%d:%02d", s / 60, s % 60);
    }

    // ------------------------------------------------------------------ stopwatch state
    static long swStart = 0, swAcc = 0;

    public static void registerAll() {
        // ---- Zeit
        add("date", "Datum", "Heutiges Datum", "clock", () -> v(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")), ""));
        add("stopwatch", "Stoppuhr", "Läuft, solange das Modul an ist - aus/an setzt sie zurück", "hourglass", () -> {
            if (swStart == 0) swStart = System.currentTimeMillis();
            return v(dur(System.currentTimeMillis() - swStart), "");
        }).off = () -> swStart = 0;
        // ---- Server
        add("tps", "TPS (geschätzt)", "Geschätzte Server-TPS aus der Weltzeit (20 = flüssig)", "pulse", () -> v(f1(tps), "TPS"));
        add("online", "Spieler online", "Wie viele Spieler auf dem Server sind (Tab-Liste)", "users", () -> v(Mc.mc().method_1562().method_2880().size(), "online"));
    }
}
