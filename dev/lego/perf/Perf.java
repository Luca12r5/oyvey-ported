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

   public static void begin(String var0) {
      START.put(var0, System.nanoTime());
   }

   public static void end(String var0) {
      Long var1 = START.remove(var0);
      if (var1 != null) {
         double var2 = (System.nanoTime() - var1) / 1000000.0;
         double[] var4 = SECTIONS.computeIfAbsent(var0, var0x -> new double[1]);
         var4[0] = var4[0] * 0.95 + var2 * 0.05;
         if (var4[0] > 8.0 && System.currentTimeMillis() - lastWarn > 60000L) {
            lastWarn = System.currentTimeMillis();
            Toasts.show(
               "warning", "Leistung: " + var0, String.format(Locale.ROOT, "kostet %.1f ms pro Bild - Leistungs-Monitor zeigt Details", var4[0]), -30208, 6000L
            );
         }
      }
   }

   public static Map<String, double[]> sections() {
      return SECTIONS;
   }
}
