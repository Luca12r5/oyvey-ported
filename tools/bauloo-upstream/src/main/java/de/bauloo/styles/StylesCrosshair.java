package de.bauloo.styles;

import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;

public final class StylesCrosshair {
   private StylesCrosshair() {
   }

   public static void draw(class_332 ctx, class_2960 texture, int x, int y, int w, int h) {
      int from = 0xFF000000 | StylesUiConfig.crosshairColor;
      if (!StylesUiConfig.crosshairGradient) {
         ctx.method_52707(class_10799.field_56883, texture, x, y, w, h, from);
      } else {
         int to = 0xFF000000 | StylesUiConfig.crosshairColor2;

         for (int i = 0; i < w; i++) {
            float t = w <= 1 ? 0.0F : (float)i / (w - 1);
            ctx.method_52708(class_10799.field_56883, texture, w, h, i, 0, x + i, y, 1, h, mix(from, to, shaped(t)));
         }
      }
   }

   public static float shaped(float t) {
      float strength = StylesUiConfig.gradientStrength / 100.0F;
      return Math.max(0.0F, Math.min(1.0F, 0.5F + (t - 0.5F) * strength));
   }

   public static int mix(int a, int b, float t) {
      int r = Math.round((a >> 16 & 0xFF) * (1.0F - t) + (b >> 16 & 0xFF) * t);
      int g = Math.round((a >> 8 & 0xFF) * (1.0F - t) + (b >> 8 & 0xFF) * t);
      int bl = Math.round((a & 0xFF) * (1.0F - t) + (b & 0xFF) * t);
      return 0xFF000000 | r << 16 | g << 8 | bl;
   }
}
