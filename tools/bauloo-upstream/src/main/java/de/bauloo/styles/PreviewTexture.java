package de.bauloo.styles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;

public final class PreviewTexture extends class_1043 {
   public static final int SIZE = 512;

   public PreviewTexture(class_2960 id, class_1011 image) {
      super(id::toString, image);
      this.field_63613 = RenderSystem.getSamplerCache().method_76520(FilterMode.NEAREST, false);
   }

   public static class_1011 shrink(class_1011 source) {
      int w = source.method_4307();
      int h = source.method_4323();
      if (w <= 512 && h <= 512) {
         return source;
      }

      class_1011 out = new class_1011(512, 512, false);

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
                  int c = source.method_61940(sx, sy);
                  a += c >>> 24;
                  r += c >> 16 & 0xFF;
                  g += c >> 8 & 0xFF;
                  b += c & 0xFF;
                  n++;
               }
            }

            out.method_61941(x, y, (int)(a / n << 24 | r / n << 16 | g / n << 8 | b / n));
         }
      }

      source.close();
      return out;
   }
}
