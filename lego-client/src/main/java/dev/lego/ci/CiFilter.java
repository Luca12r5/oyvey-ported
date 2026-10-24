package dev.lego.ci;

import dev.lego.LegoClient;
import dev.lego.visual.Visuals;
import net.minecraft.client.Minecraft;

/**
 * CI only (-Dlegoclient.ci.filter=1): switches the color filter on a few seconds after start and
 * logs which post effect the game then has requested, so a broken shader or API shows in the log.
 */
public final class CiFilter {
   private static final boolean ON = System.getProperty("legoclient.ci.filter") != null;
   private static long start;
   private static int step;

   private CiFilter() {
   }

   public static void tick(Minecraft mc) {
      if (!ON || step >= 2 || Visuals.filter == null) return;
      long now = System.currentTimeMillis();
      if (start == 0L) start = now;
      if (step == 0 && now - start > 5000L) {
         step = 1;
         Visuals.filter.setEnabled(true);
         LegoClient.LOG("CI filter: on");
      } else if (step == 1 && now - start > 15000L) {
         step = 2;
         LegoClient.LOG("CI filter: enabled=" + Visuals.filter.enabled + " effect=" + dev.lego.compat.McCompat.currentPostEffect(mc.gameRenderer));
      }
   }
}
