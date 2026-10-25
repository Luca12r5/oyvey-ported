package de.bauloo.styles;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import java.awt.image.BufferedImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

final class AtlasWriter {
   private AtlasWriter() {
   }

   static TextureAtlas atlas(Identifier textureId) {
      TextureAtlas[] found = new TextureAtlas[1];
      Minecraft.getInstance().getAtlasManager().forEach((id, atlas) -> {
         if (atlas.location().equals(textureId) || id.equals(textureId)) {
            found[0] = atlas;
         }
      });
      if (found[0] == null) {
         BaulooStylesClient.LOGGER.warn("No atlas found for {}", textureId);
      }

      return found[0];
   }

   static void write(TextureAtlas atlas, Identifier spriteId, BufferedImage image) {
      TextureAtlasSprite sprite = atlas.getSprite(spriteId);
      if (sprite != null && sprite != atlas.missingSprite()) {
         int w = sprite.contents().width();
         int h = sprite.contents().height();
         if (image.getWidth() != w || image.getHeight() != h) {
            image = scaled(image, w, h);
         }

         GpuTexture texture = atlas.getTexture();
         if (texture != null && !texture.isClosed()) {
            int innerX = Math.round(sprite.getU0() * texture.getWidth(0));
            int pad = Math.max(0, innerX - sprite.getX());
            int pw = w + 2 * pad;
            int ph = h + 2 * pad;
            NativeImage level = new NativeImage(pw, ph, false);

            for (int y = 0; y < ph; y++) {
               for (int x = 0; x < pw; x++) {
                  int sx = Math.min(w - 1, Math.max(0, x - pad));
                  int sy = Math.min(h - 1, Math.max(0, y - pad));
                  level.setPixel(x, y, image.getRGB(sx, sy));
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
                  NativeImage smaller = half(level);
                  level.close();
                  level = smaller;
               }

               encoder.writeToTexture(texture, level, mip, 0, sprite.getX() >> mip, sprite.getY() >> mip);
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

   static NativeImage toNative(BufferedImage image) {
      NativeImage out = new NativeImage(image.getWidth(), image.getHeight(), false);

      for (int y = 0; y < image.getHeight(); y++) {
         for (int x = 0; x < image.getWidth(); x++) {
            out.setPixel(x, y, image.getRGB(x, y));
         }
      }

      return out;
   }

   static NativeImage half(NativeImage src) {
      int w = Math.max(1, src.getWidth() / 2);
      int h = Math.max(1, src.getHeight() / 2);
      NativeImage out = new NativeImage(w, h, false);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int a = 0;
            int r = 0;
            int g = 0;
            int b = 0;

            for (int dy = 0; dy < 2; dy++) {
               for (int dx = 0; dx < 2; dx++) {
                  int c = src.getPixel(Math.min(src.getWidth() - 1, x * 2 + dx), Math.min(src.getHeight() - 1, y * 2 + dy));
                  a += c >>> 24;
                  r += c >> 16 & 0xFF;
                  g += c >> 8 & 0xFF;
                  b += c & 0xFF;
               }
            }

            out.setPixel(x, y, a / 4 << 24 | r / 4 << 16 | g / 4 << 8 | b / 4);
         }
      }

      return out;
   }
}
