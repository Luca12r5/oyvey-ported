package de.bauloo.styles;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.minecraft.class_310;

public final class PlayerOutline {
   public static volatile boolean previewSelf;
   private static final float EDGE = 0.08F;
   private static final Map<Integer, long[]> SIGHT = new HashMap<>();

   private PlayerOutline() {
   }

   public static boolean outlines(class_1297 entity) {
      if (previewSelf && entity == class_310.method_1551().field_1724) {
         return true;
      }

      StylesUiConfig.load();
      if (StylesUiConfig.outline && entity instanceof class_1657 && !entity.method_5851()) {
         class_310 client = class_310.method_1551();
         if (client.field_1724 == null) {
            return false;
         } else if (entity == client.field_1724) {
            return StylesUiConfig.outlineSelf;
         } else {
            return entity.method_5767() && !wearsArmor((class_1657)entity) ? false : StylesUiConfig.outlineOthers && canSee(client, entity);
         }
      } else {
         return false;
      }
   }

   private static boolean canSee(class_310 client, class_1297 entity) {
      long tick = client.field_1687 == null ? 0L : client.field_1687.method_75260();
      long[] cached = SIGHT.get(entity.method_5628());
      if (cached != null && cached[0] == tick) {
         return cached[1] == 1L;
      }

      if (SIGHT.size() > 256) {
         SIGHT.clear();
      }

      boolean visible = client.field_1724.method_6057(entity);
      SIGHT.put(entity.method_5628(), new long[]{tick, visible ? 1L : 0L});
      return visible;
   }

   private static boolean wearsArmor(class_1657 player) {
      for (class_1304 slot : new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166}) {
         if (!player.method_6118(slot).method_7960()) {
            return true;
         }
      }

      return false;
   }

   public static int sliderColor(float t) {
      if (t < 0.08F) {
         return 16777215;
      } else {
         return t > 0.92F ? 0 : StylesTheme.hsv((t - 0.08F) / 0.84000003F, 0.65F, 1.0F);
      }
   }

   public static float sliderPos(int rgb) {
      rgb &= 16777215;
      float[] hsv = StylesTheme.toHsv(rgb);
      if (hsv[2] < 0.15F) {
         return 0.96F;
      } else {
         return hsv[1] < 0.15F ? 0.04F : 0.08F + hsv[0] * 0.84000003F;
      }
   }

   public static class_2960 postEffect(class_2960 vanilla) {
      StylesUiConfig.load();
      if (!StylesUiConfig.outline && !previewSelf) {
         return vanilla;
      }

      int width = Math.max(1, Math.min(6, StylesUiConfig.outlineWidth));
      return class_2960.method_60655("bauloo_styles", "neon_" + width);
   }

   public static int color() {
      if (StylesUiConfig.outlineRainbow) {
         float hue = (float)(System.currentTimeMillis() % 4000L) / 4000.0F;
         return Color.HSBtoRGB(hue, 0.7F, 1.0F) & 16777215;
      } else {
         return StylesUiConfig.outlineColor & 16777215;
      }
   }
}
