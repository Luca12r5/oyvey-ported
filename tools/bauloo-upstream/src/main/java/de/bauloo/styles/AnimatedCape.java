package de.bauloo.styles;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import net.minecraft.class_1011;
import net.minecraft.class_1044;
import net.minecraft.class_2960;
import net.minecraft.class_310;

public final class AnimatedCape {
   private static final int UNIT = 32;
   private static final int TEX_W = 2048;
   private static final int TEX_H = 1024;
   private static final int FACE_W = 320;
   private static final int FACE_H = 512;
   private static final int MIPS = 6;
   private final class_2960 id;
   private final AnimatedCape.FrameSource source;
   private final long frameMs;
   private final boolean tintable;
   private int tint = -1;
   private volatile class_1011[] frames;
   private boolean loading;
   private AnimatedCape.Texture texture;
   private int shown = -1;

   public AnimatedCape(class_2960 id, AnimatedCape.FrameSource source, int fps) {
      this(id, source, fps, false);
   }

   public AnimatedCape(class_2960 id, AnimatedCape.FrameSource source, int fps, boolean tintable) {
      this.id = id;
      this.source = source;
      this.frameMs = 1000L / Math.max(1, fps);
      this.tintable = tintable;
   }

   private static class_1011 recolored(class_1011 src, int rgb) {
      float[] target = Color.RGBtoHSB(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, null);
      float[] hsb = new float[3];
      class_1011 out = new class_1011(src.method_4307(), src.method_4323(), false);

      for (int y = 0; y < src.method_4323(); y++) {
         for (int x = 0; x < src.method_4307(); x++) {
            int c = src.method_61940(x, y);
            Color.RGBtoHSB(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF, hsb);
            float s = hsb[1] < 0.08F ? hsb[1] : Math.min(1.0F, hsb[1] * 0.4F + target[1] * 0.6F);
            out.method_61941(x, y, c & 0xFF000000 | Color.HSBtoRGB(hsb[1] < 0.08F ? hsb[0] : target[0], s, hsb[2]) & 16777215);
         }
      }

      return out;
   }

   public class_2960 current() {
      class_1011[] ready = this.frames;
      if (ready == null) {
         this.startLoading();
         return null;
      }

      if (this.texture == null) {
         this.texture = new AnimatedCape.Texture(this.id);
         class_310.method_1551().method_1531().method_4616(this.id, this.texture);
         this.texture.writeStatic(ready[0]);
      }

      int want = this.tintable && StylesUiConfig.cosCapeTint ? StylesUiConfig.cosColor & 16777215 : -1;
      if (want != this.tint) {
         this.tint = want;
         this.shown = -1;
      }

      int frame = (int)(System.currentTimeMillis() / this.frameMs % ready.length);
      if (frame != this.shown) {
         this.shown = frame;
         if (this.tint < 0) {
            this.texture.writeRegion(ready[frame], 32, 32);
         } else {
            class_1011 tinted = recolored(ready[frame], this.tint);
            this.texture.writeRegion(tinted, 32, 32);
            tinted.close();
         }
      }

      return this.id;
   }

   public synchronized void release() {
      class_1011[] old = this.frames;
      this.frames = null;
      this.loading = false;
      this.shown = -1;
      if (this.texture != null) {
         class_310.method_1551().method_1531().method_4615(this.id);
         this.texture = null;
      }

      if (old != null) {
         for (class_1011 n : old) {
            if (n != null) {
               n.close();
            }
         }
      }
   }

   private synchronized void startLoading() {
      if (!this.loading) {
         this.loading = true;
         Thread t = new Thread(() -> {
            try {
               int count = this.source.count();
               if (count <= 0) {
                  return;
               }

               class_1011[] out = new class_1011[count];

               for (int i = 0; i < count; i++) {
                  BufferedImage img = this.source.read(i);
                  if (img == null) {
                     BaulooStylesClient.LOGGER.warn("Animated cape {}: frame {} missing", this.id, i);
                     return;
                  }

                  class_1011 n = new class_1011(320, 512, false);

                  for (int y = 0; y < 512; y++) {
                     for (int x = 0; x < 320; x++) {
                        n.method_61941(x, y, 0xFF000000 | img.getRGB(x * img.getWidth() / 320, y * img.getHeight() / 512));
                     }
                  }

                  out[i] = n;
               }

               this.frames = out;
            } catch (Exception e) {
               BaulooStylesClient.LOGGER.warn("Animated cape {} could not load: {}", this.id, e.toString());
            }
         }, "Bauloo Animated Cape");
         t.setDaemon(true);
         t.start();
      }
   }

   public interface FrameSource {
      int count();

      BufferedImage read(int var1) throws IOException;
   }

   private static final class Texture extends class_1044 {
      Texture(class_2960 id) {
         GpuDevice device = RenderSystem.getDevice();
         this.field_56974 = device.createTexture(id::toString, 5, TextureFormat.RGBA8, 2048, 1024, 1, 6);
         this.field_60597 = device.createTextureView(this.field_56974);
         this.field_63613 = RenderSystem.getSamplerCache()
            .method_75293(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, true);
      }

      void writeStatic(class_1011 face) {
         class_1011 all = new class_1011(2048, 1024, true);
         int edge = -15724524;

         for (int y = 0; y < 1024; y++) {
            for (int x = 0; x < 2048; x++) {
               all.method_61941(x, y, edge);
            }
         }

         for (int y = 0; y < 512; y++) {
            for (int x = 0; x < 320; x++) {
               int c = face.method_61940(x, y);
               all.method_61941(384 + x, 32 + y, 0xFF000000 | (c >> 16 & 0xFF) * 5 / 10 << 16 | (c >> 8 & 0xFF) * 5 / 10 << 8 | (c & 0xFF) * 5 / 10);
            }
         }

         int ew = 768;
         int eh = 704;

         for (int y = 0; y < eh; y++) {
            for (int x = 0; x < ew; x++) {
               all.method_61941(704 + x, y, face.method_61940(x * 320 / ew, y * 512 / eh));
            }
         }

         this.writeRegion(all, 0, 0);
         all.close();
      }

      void writeRegion(class_1011 image, int x, int y) {
         CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
         class_1011 level = image;

         for (int mip = 0; mip < 6; mip++) {
            if (mip > 0) {
               class_1011 smaller = AtlasWriter.half(level);
               if (level != image) {
                  level.close();
               }

               level = smaller;
            }

            encoder.writeToTexture(this.field_56974, level, mip, 0, x >> mip, y >> mip, level.method_4307(), level.method_4323(), 0, 0);
         }

         if (level != image) {
            level.close();
         }
      }
   }
}
