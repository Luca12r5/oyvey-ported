package de.bauloo.styles;

import net.minecraft.client.resources.model.ModelBakery;

public final class FireColor {
   private static boolean applied;

   private FireColor() {
   }

   public static void applyLive() {
      StylesUiConfig.load();
      if (StylesUiConfig.fireCustom || applied) {
         int color = StylesUiConfig.fireCustom ? StylesUiConfig.fireColor : -1;

         try {
            LiveRecolor.apply(ModelBakery.FIRE_0, color);
            LiveRecolor.apply(ModelBakery.FIRE_1, color);
            applied = StylesUiConfig.fireCustom;
         } catch (RuntimeException var2) {
            BaulooStylesClient.LOGGER.warn("Fire colour could not be applied: {}", var2.toString());
         }
      }
   }

   public static void atlasRebuilt() {
      applied = false;
      applyLive();
   }
}
