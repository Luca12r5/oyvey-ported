package de.bauloo.styles;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class StylesCrosshair {
   private StylesCrosshair() {
   }

   public static void draw(GuiGraphics ctx, Identifier texture, int x, int y, int w, int h) {
      int from = 0xFF000000 | StylesUiConfig.crosshairColor;
      if (!StylesUiConfig.crosshairGradient) {
         ctx.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, w, h, from);
      } else {
         int to = 0xFF000000 | StylesUiConfig.crosshairColor2;

         for (int i = 0; i < w; i++) {
            float t = w <= 1 ? 0.0F : (float)i / (w - 1);
            ctx.blitSprite(RenderPipelines.GUI_TEXTURED, texture, w, h, i, 0, x + i, y, 1, h, mix(from, to, shaped(t)));
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
