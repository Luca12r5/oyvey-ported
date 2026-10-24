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

   private static boolean reported;

   /** Once per session, 60 s after joining a world: the ten most expensive sections to the log (CI reads it). */
   public static void report(long joinedAt) {
      if (reported || joinedAt == 0L || System.currentTimeMillis() - joinedAt < 60000L) {
         return;
      }
      reported = true;
      StringBuilder b = new StringBuilder("LEGO perf (ms/frame, smoothed):");
      SECTIONS.entrySet().stream()
         .sorted((a, c) -> Double.compare(c.getValue()[0], a.getValue()[0]))
         .limit(10)
         .forEach(e -> b.append(String.format(Locale.ROOT, " %s=%.2f", e.getKey(), e.getValue()[0])));
      dev.lego.LegoClient.LOG(b.toString());
   }

   public static Map<String, double[]> sections() {
      return SECTIONS;
   }
}
