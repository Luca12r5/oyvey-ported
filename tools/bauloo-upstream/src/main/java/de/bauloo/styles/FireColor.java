package de.bauloo.styles;

import net.minecraft.class_1088;

public final class FireColor {
   private static boolean applied;

   private FireColor() {
   }

   public static void applyLive() {
      StylesUiConfig.load();
      if (StylesUiConfig.fireCustom || applied) {
         int color = StylesUiConfig.fireCustom ? StylesUiConfig.fireColor : -1;

         try {
            LiveRecolor.apply(class_1088.field_5397, color);
            LiveRecolor.apply(class_1088.field_5370, color);
            applied = StylesUiConfig.fireCustom;
         } catch (RuntimeException e) {
            BaulooStylesClient.LOGGER.warn("Fire colour could not be applied: {}", e.toString());
         }
      }
   }

   public static void atlasRebuilt() {
      applied = false;
      applyLive();
   }
}
