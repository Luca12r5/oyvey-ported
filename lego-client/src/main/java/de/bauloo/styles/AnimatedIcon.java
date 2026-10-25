package de.bauloo.styles;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class AnimatedIcon {
   private static final int W = 80;
   private static final int H = 128;
   private final Identifier id;
   private final AnimatedCape.FrameSource source;
   private final long frameMs;
   private volatile NativeImage[] frames;
   private boolean loading;
   private DynamicTexture texture;
   private int shown = -1;

   public AnimatedIcon(Identifier id, AnimatedCape.FrameSource source, int fps) {
      this.id = id;
      this.source = source;
      this.frameMs = 1000L / Math.max(1, fps);
   }

   public Identifier current() {
      NativeImage[] ready = this.frames;
      if (ready == null) {
         this.startLoading();
         return null;
      } else {
         if (this.texture == null) {
            this.texture = new DynamicTexture(this.id::toString, 80, 128, false);
            Minecraft.getInstance().getTextureManager().register(this.id, this.texture);
         }

         int frame = (int)(System.currentTimeMillis() / this.frameMs % ready.length);
         if (frame != this.shown && this.texture.getPixels() != null) {
            this.shown = frame;
            this.texture.getPixels().copyFrom(ready[frame]);
            this.texture.upload();
         }

         return this.id;
      }
   }

   private synchronized void startLoading() {
      if (!this.loading) {
         this.loading = true;
         Thread t = new Thread(() -> {
            try {
               int count = this.source.count();
               NativeImage[] out = new NativeImage[count];

               for (int i = 0; i < count; i++) {
                  BufferedImage img = this.source.read(i);
                  if (img == null) {
                     return;
                  }

                  NativeImage n = new NativeImage(80, 128, false);

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

                        n.setPixel(x, y, 0xFF000000 | r / c << 16 | g / c << 8 | b / c);
                     }
                  }

                  out[i] = n;
               }

               this.frames = out;
            } catch (Exception var19) {
               BaulooStylesClient.LOGGER.warn("Animated icon {} could not load: {}", this.id, var19.toString());
            }
         }, "Bauloo Cape Icon");
         t.setDaemon(true);
         t.start();
      }
   }
}
