package de.bauloo.styles;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.class_1059;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;

public final class AnchorPixelGlow {
   private static boolean applied;

   private AnchorPixelGlow() {
   }

   public static void applyLive() {
      StylesUiConfig.load();
      if (StylesUiConfig.anchorPixelGlow || applied) {
         class_310 mc = class_310.method_1551();

         try {
            class_1059 atlas = AtlasWriter.atlas(class_1059.field_5275);
            if (atlas == null) {
               return;
            }

            boolean on = StylesUiConfig.anchorPixelGlow;
            BufferedImage empty = AnchorGlow.read(mc, 0);
            BufferedImage full = AnchorGlow.read(mc, 4);

            for (int level = 0; level <= 4; level++) {
               BufferedImage side = level > 0 && StylesUiConfig.anchorShowFull && full != null ? full : AnchorGlow.read(mc, level);
               if (side != null && level > 0 && StylesUiConfig.anchorGlowCustom && empty != null) {
                  side = AnchorGlow.recolor(empty, side, StylesUiConfig.anchorGlowColor);
               }

               write(atlas, "anchor_glow_side" + level, side, on);
            }

            write(atlas, "anchor_glow_bottom", read(mc, "respawn_anchor_bottom"), on);
            write(atlas, "anchor_glow_top_off", read(mc, "respawn_anchor_top_off"), on);
            applied = on;
         } catch (IOException | RuntimeException e) {
            BaulooStylesClient.LOGGER.warn("Anchor pixel glow could not be applied: {}", e.toString());
         }
      }
   }

   private static void write(class_1059 atlas, String sprite, BufferedImage source, boolean on) {
      BufferedImage out = new BufferedImage(source == null ? 16 : source.getWidth(), source == null ? 16 : source.getHeight(), 2);
      if (on && source != null) {
         for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
               int c = source.getRGB(x, y);
               if (glows(c)) {
                  out.setRGB(x, y, c | 0xFF000000);
               }
            }
         }
      }

      LiveRecolor.applyImage(atlas, class_2960.method_60655("bauloo_styles", "block/" + sprite), out);
   }

   static boolean glows(int argb) {
      if (argb >>> 24 < 128) {
         return false;
      }

      float[] hsb = Color.RGBtoHSB(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, null);
      return hsb[1] > 0.35F && hsb[2] > 0.38F || hsb[2] > 0.8F;
   }

   private static BufferedImage read(class_310 mc, String name) throws IOException {
      List<class_3298> all = mc.method_1478().method_14489(class_2960.method_60656("textures/block/" + name + ".png"));

      for (int i = all.size() - 1; i >= 0; i--) {
         class_3298 resource = all.get(i);
         if (!resource.method_14480().contains("bauloo_anchor_glow")) {
            try (InputStream in = resource.method_14482()) {
               return ImageIO.read(in);
            }
         }
      }

      return null;
   }
}
