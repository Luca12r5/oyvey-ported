package de.bauloo.styles;

import java.awt.image.BufferedImage;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;

public final class AnimatedIcon {
   private static final int W = 80;
   private static final int H = 128;
   private final class_2960 id;
   private final AnimatedCape.FrameSource source;
   private final long frameMs;
   private volatile class_1011[] frames;
   private boolean loading;
   private class_1043 texture;
   private int shown = -1;

   public AnimatedIcon(class_2960 id, AnimatedCape.FrameSource source, int fps) {
      this.id = id;
      this.source = source;
      this.frameMs = 1000L / Math.max(1, fps);
   }

   public class_2960 current() {
      class_1011[] ready = this.frames;
      if (ready == null) {
         this.startLoading();
         return null;
      }

      if (this.texture == null) {
         this.texture = new class_1043(this.id::toString, 80, 128, false);
         class_310.method_1551().method_1531().method_4616(this.id, this.texture);
      }

      int frame = (int)(System.currentTimeMillis() / this.frameMs % ready.length);
      if (frame != this.shown && this.texture.method_4525() != null) {
         this.shown = frame;
         this.texture.method_4525().method_4317(ready[frame]);
         this.texture.method_4524();
      }

      return this.id;
   }

   private synchronized void startLoading() {
      if (!this.loading) {
         this.loading = true;
         Thread t = new Thread(() -> {
            try {
               int count = this.source.count();
               class_1011[] out = new class_1011[count];

               for (int i = 0; i < count; i++) {
                  BufferedImage img = this.source.read(i);
                  if (img == null) {
                     return;
                  }

                  class_1011 n = new class_1011(80, 128, false);

                  for (int y = 0; y < 128; y++) {
                     for (int x = 0; x < 80; x++) {
                        int x0 = x * img.getWidth() / 80;
                        int x1 = Math.max(x0 + 1, (x + 1) * img.getWidth() / 80);
                        int y0 = y * img.getHeight() / 128;
                        int y1 = Math.max(y0 + 1, (y + 1) * img.getHeight() / 128);
                        int r = 0;
                        int g = 0;
                        int b = 0;
                        int c = 0;

                        for (int yy = y0; yy < y1; yy += 2) {
                           for (int xx = x0; xx < x1; xx += 2) {
                              int p = img.getRGB(xx, yy);
                              r += p >> 16 & 0xFF;
                              g += p >> 8 & 0xFF;
                              b += p & 0xFF;
                              c++;
                           }
                        }

                        n.method_61941(x, y, 0xFF000000 | r / c << 16 | g / c << 8 | b / c);
                     }
                  }

                  out[i] = n;
               }

               this.frames = out;
            } catch (Exception e) {
               BaulooStylesClient.LOGGER.warn("Animated icon {} could not load: {}", this.id, e.toString());
            }
         }, "Bauloo Cape Icon");
         t.setDaemon(true);
         t.start();
      }
   }
}
