package de.bauloo.styles;

import net.minecraft.client.resources.model.ModelBakery;

public final class ShieldColor {
   private static boolean applied;

   private ShieldColor() {
   }

   public static void applyLive() {
      StylesUiConfig.load();
      if (StylesUiConfig.shieldCustom || applied) {
         int color = StylesUiConfig.shieldCustom ? -2 : -1;

         try {
            LiveRecolor.apply(ModelBakery.SHIELD_BASE, color);
            LiveRecolor.apply(ModelBakery.NO_PATTERN_SHIELD, color);
            applied = StylesUiConfig.shieldCustom;
         } catch (RuntimeException var2) {
            BaulooStylesClient.LOGGER.warn("Shield colour could not be applied: {}", var2.toString());
         }
      }
   }

   public static void atlasRebuilt() {
      applied = false;
      applyLive();
   }
}
