package dev.lego.perf;

import java.util.LinkedHashMap;
import java.util.Map;

/** Tiny profiler: how many milliseconds per frame each part of the client costs (shown by the "Leistungs-Monitor"). */
public final class Perf {
    private Perf() { }

    private static final Map<String, double[]> SECTIONS = new LinkedHashMap<>();   // avg ms, sum ns, count
    private static final Map<String, Long> START = new java.util.HashMap<>();
    private static long lastWarn = 0;

    public static void begin(String s) { START.put(s, System.nanoTime()); }

    public static void end(String s) {
        Long t = START.remove(s);
        if (t == null) return;
        double ms = (System.nanoTime() - t) / 1e6;
        double[] v = SECTIONS.computeIfAbsent(s, k -> new double[1]);
        v[0] = v[0] * 0.95 + ms * 0.05;
        if (v[0] > 8 && System.currentTimeMillis() - lastWarn > 60000) {
            lastWarn = System.currentTimeMillis();
            dev.lego.ui.Toasts.show("warning", "Leistung: " + s, String.format(java.util.Locale.ROOT, "kostet %.1f ms pro Bild - Leistungs-Monitor zeigt Details", v[0]), 0xFFFF8A00, 6000);
        }
    }

    public static Map<String, double[]> sections() { return SECTIONS; }
}
