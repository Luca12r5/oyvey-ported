package de.bauloo.styles;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import java.awt.image.BufferedImage;
import net.minecraft.class_1011;
import net.minecraft.class_1058;
import net.minecraft.class_1059;
import net.minecraft.class_2960;
import net.minecraft.class_310;

final class AtlasWriter {
   private AtlasWriter() {
   }

   static class_1059 atlas(class_2960 textureId) {
      class_1059[] found = new class_1059[1];
      class_310.method_1551().method_72703().method_73156((id, atlas) -> {
         if (atlas.method_24106().equals(textureId) || id.equals(textureId)) {
            found[0] = atlas;
         }
      });
      if (found[0] == null) {
         BaulooStylesClient.LOGGER.warn("No atlas found for {}", textureId);
      }

      return found[0];
   }

   static void write(class_1059 atlas, class_2960 spriteId, BufferedImage image) {
      class_1058 sprite = atlas.method_4608(spriteId);
      if (sprite != null && sprite != atlas.method_73023()) {
         int w = sprite.method_45851().method_45807();
         int h = sprite.method_45851().method_45815();
         if (image.getWidth() != w || image.getHeight() != h) {
            image = scaled(image, w, h);
         }

         GpuTexture texture = atlas.method_68004();
         if (texture != null && !texture.isClosed()) {
            int innerX = Math.round(sprite.method_4594() * texture.getWidth(0));
            int pad = Math.max(0, innerX - sprite.method_35806());
            int pw = w + 2 * pad;
            int ph = h + 2 * pad;
            class_1011 level = new class_1011(pw, ph, false);

            for (int y = 0; y < ph; y++) {
               for (int x = 0; x < pw; x++) {
                  int sx = Math.min(w - 1, Math.max(0, x - pad));
                  int sy = Math.min(h - 1, Math.max(0, y - pad));
                  level.method_61941(x, y, image.getRGB(sx, sy));
               }
            }

            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

            for (int mip = 0; mip < texture.getMipLevels(); mip++) {
               int lw = pw >> mip;
               int lh = ph >> mip;
               if (lw < 1 || lh < 1) {
                  break;
               }

               if (mip > 0) {
                  class_1011 smaller = half(level);
                  level.close();
                  level = smaller;
               }

               encoder.writeToTexture(texture, level, mip, 0, sprite.method_35806() >> mip, sprite.method_35807() >> mip, lw, lh, 0, 0);
            }

            level.close();
         }
      }
   }

   static BufferedImage scaled(BufferedImage src, int w, int h) {
      BufferedImage out = new BufferedImage(w, h, 2);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            out.setRGB(x, y, src.getRGB(x * src.getWidth() / w, y * src.getHeight() / h));
         }
      }

      return out;
   }

   static class_1011 toNative(BufferedImage image) {
      class_1011 out = new class_1011(image.getWidth(), image.getHeight(), false);

      for (int y = 0; y < image.getHeight(); y++) {
         for (int x = 0; x < image.getWidth(); x++) {
            out.method_61941(x, y, image.getRGB(x, y));
         }
      }

      return out;
   }

   static class_1011 half(class_1011 src) {
      int w = Math.max(1, src.method_4307() / 2);
      int h = Math.max(1, src.method_4323() / 2);
      class_1011 out = new class_1011(w, h, false);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int a = 0;
            int r = 0;
            int g = 0;
            int b = 0;

            for (int dy = 0; dy < 2; dy++) {
               for (int dx = 0; dx < 2; dx++) {
                  int c = src.method_61940(Math.min(src.method_4307() - 1, x * 2 + dx), Math.min(src.method_4323() - 1, y * 2 + dy));
                  a += c >>> 24;
                  r += c >> 16 & 0xFF;
                  g += c >> 8 & 0xFF;
                  b += c & 0xFF;
               }
            }

            out.method_61941(x, y, a / 4 << 24 | r / 4 << 16 | g / 4 << 8 | b / 4);
         }
      }

      return out;
   }
}
