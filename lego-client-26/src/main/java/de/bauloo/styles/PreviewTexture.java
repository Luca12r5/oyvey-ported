package de.bauloo.styles;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class PreviewTexture extends DynamicTexture {
   public static final int SIZE = 512;

   public PreviewTexture(Identifier id, NativeImage image) {
      super(id::toString, image);
      this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST, false);
   }

   public static NativeImage shrink(NativeImage source) {
      int w = source.getWidth();
      int h = source.getHeight();
      if (w <= 512 && h <= 512) {
         return source;
      } else {
         NativeImage out = new NativeImage(512, 512, false);

         for (int y = 0; y < 512; y++) {
            int y0 = y * h / 512;
            int y1 = Math.max(y0 + 1, (y + 1) * h / 512);

            for (int x = 0; x < 512; x++) {
               int x0 = x * w / 512;
               int x1 = Math.max(x0 + 1, (x + 1) * w / 512);
               long a = 0L;
               long r = 0L;
               long g = 0L;
               long b = 0L;
               int n = 0;

               for (int sy = y0; sy < y1; sy++) {
                  for (int sx = x0; sx < x1; sx++) {
                     int c = source.getPixel(sx, sy);
                     a += c >>> 24;
                     r += c >> 16 & 0xFF;
                     g += c >> 8 & 0xFF;
                     b += c & 0xFF;
                     n++;
                  }
               }

               out.setPixel(x, y, (int)(a / n << 24 | r / n << 16 | g / n << 8 | b / n));
            }
         }

         source.close();
         return out;
      }
   }
}
