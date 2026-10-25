package de.bauloo.styles;

import net.minecraft.class_1088;

public final class ShieldColor {
   private static boolean applied;

   private ShieldColor() {
   }

   public static void applyLive() {
      StylesUiConfig.load();
      if (StylesUiConfig.shieldCustom || applied) {
         int color = StylesUiConfig.shieldCustom ? -2 : -1;

         try {
            LiveRecolor.apply(class_1088.field_21557, color);
            LiveRecolor.apply(class_1088.field_21558, color);
            applied = StylesUiConfig.shieldCustom;
         } catch (RuntimeException e) {
            BaulooStylesClient.LOGGER.warn("Shield colour could not be applied: {}", e.toString());
         }
      }
   }

   public static void atlasRebuilt() {
      applied = false;
      applyLive();
   }
}
