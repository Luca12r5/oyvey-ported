package dev.lego.perf;

import dev.lego.ui.Toasts;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class Perf {
   private static final Map<String, double[]> SECTIONS = new LinkedHashMap<>();
   private static final Map<String, Long> START = new HashMap<>();
   private static long lastWarn = 0L;

   private Perf() {
   }

   public static void begin(String s) {
      START.put(s, System.nanoTime());
   }

   public static void end(String s) {
      Long t = START.remove(s);
      if (t != null) {
         double ms = (System.nanoTime() - t) / 1000000.0;
         double[] v = SECTIONS.computeIfAbsent(s, k -> new double[1]);
         v[0] = v[0] * 0.95 + ms * 0.05;
         if (v[0] > 8.0 && System.currentTimeMillis() - lastWarn > 60000L) {
            lastWarn = System.currentTimeMillis();
            Toasts.show(
               "warning", "Leistung: " + s, String.format(Locale.ROOT, "kostet %.1f ms pro Bild - Leistungs-Monitor zeigt Details", v[0]), -30208, 6000L
            );
         }
      }
   }

   public static Map<String, double[]> sections() {
      return SECTIONS;
   }
}
