package de.bauloo.styles;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

public final class AnimatedCape {
   private static final int UNIT = 32;
   private static final int TEX_W = 2048;
   private static final int TEX_H = 1024;
   private static final int FACE_W = 320;
   private static final int FACE_H = 512;
   private static final int MIPS = 6;
   private final Identifier id;
   private final AnimatedCape.FrameSource source;
   private final long frameMs;
   private final boolean tintable;
   private int tint = -1;
   private volatile NativeImage[] frames;
   private boolean loading;
   private AnimatedCape.Texture texture;
   private int shown = -1;

   public AnimatedCape(Identifier id, AnimatedCape.FrameSource source, int fps) {
      this(id, source, fps, false);
   }

   public AnimatedCape(Identifier id, AnimatedCape.FrameSource source, int fps, boolean tintable) {
      this.id = id;
      this.source = source;
      this.frameMs = 1000L / Math.max(1, fps);
      this.tintable = tintable;
   }

   private static NativeImage recolored(NativeImage src, int rgb) {
      float[] target = Color.RGBtoHSB(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, null);
      float[] hsb = new float[3];
      NativeImage out = new NativeImage(src.getWidth(), src.getHeight(), false);

      for (int y = 0; y < src.getHeight(); y++) {
         for (int x = 0; x < src.getWidth(); x++) {
            int c = src.getPixel(x, y);
            Color.RGBtoHSB(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF, hsb);
            float s = hsb[1] < 0.08F ? hsb[1] : Math.min(1.0F, hsb[1] * 0.4F + target[1] * 0.6F);
            out.setPixel(x, y, c & 0xFF000000 | Color.HSBtoRGB(hsb[1] < 0.08F ? hsb[0] : target[0], s, hsb[2]) & 16777215);
         }
      }

      return out;
   }

   public Identifier current() {
      NativeImage[] ready = this.frames;
      if (ready == null) {
         this.startLoading();
         return null;
      } else {
         if (this.texture == null) {
            this.texture = new AnimatedCape.Texture(this.id);
            Minecraft.getInstance().getTextureManager().register(this.id, this.texture);
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
               NativeImage tinted = recolored(ready[frame], this.tint);
               this.texture.writeRegion(tinted, 32, 32);
               tinted.close();
            }
         }

         return this.id;
      }
   }

   public synchronized void release() {
      NativeImage[] old = this.frames;
      this.frames = null;
      this.loading = false;
      this.shown = -1;
      if (this.texture != null) {
         Minecraft.getInstance().getTextureManager().release(this.id);
         this.texture = null;
      }

      if (old != null) {
         for (NativeImage n : old) {
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

               NativeImage[] out = new NativeImage[count];

               for (int i = 0; i < count; i++) {
                  BufferedImage img = this.source.read(i);
                  if (img == null) {
                     BaulooStylesClient.LOGGER.warn("Animated cape {}: frame {} missing", this.id, i);
                     return;
                  }

                  NativeImage n = new NativeImage(320, 512, false);

                  for (int y = 0; y < 512; y++) {
                     for (int x = 0; x < 320; x++) {
                        n.setPixel(x, y, 0xFF000000 | img.getRGB(x * img.getWidth() / 320, y * img.getHeight() / 512));
                     }
                  }

                  out[i] = n;
               }

               this.frames = out;
            } catch (Exception var8) {
               BaulooStylesClient.LOGGER.warn("Animated cape {} could not load: {}", this.id, var8.toString());
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

   private static final class Texture extends AbstractTexture {
      Texture(Identifier id) {
         GpuDevice device = RenderSystem.getDevice();
         this.texture = device.createTexture(id::toString, 5, GpuFormat.RGBA8_UNORM, 2048, 1024, 1, 6);
         this.textureView = device.createTextureView(this.texture);
         this.sampler = RenderSystem.getSamplerCache()
            .getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, true);
      }

      void writeStatic(NativeImage face) {
         NativeImage all = new NativeImage(2048, 1024, true);
         int edge = -15724524;

         for (int y = 0; y < 1024; y++) {
            for (int x = 0; x < 2048; x++) {
               all.setPixel(x, y, edge);
            }
         }

         for (int y = 0; y < 512; y++) {
            for (int x = 0; x < 320; x++) {
               int c = face.getPixel(x, y);
               all.setPixel(384 + x, 32 + y, 0xFF000000 | (c >> 16 & 0xFF) * 5 / 10 << 16 | (c >> 8 & 0xFF) * 5 / 10 << 8 | (c & 0xFF) * 5 / 10);
            }
         }

         int ew = 768;
         int eh = 704;

         for (int y = 0; y < eh; y++) {
            for (int x = 0; x < ew; x++) {
               all.setPixel(704 + x, y, face.getPixel(x * 320 / ew, y * 512 / eh));
            }
         }

         this.writeRegion(all, 0, 0);
         all.close();
      }

      void writeRegion(NativeImage image, int x, int y) {
         CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
         NativeImage level = image;

         for (int mip = 0; mip < 6; mip++) {
            if (mip > 0) {
               NativeImage smaller = AtlasWriter.half(level);
               if (level != image) {
                  level.close();
               }

               level = smaller;
            }

            encoder.writeToTexture(this.texture, level, mip, 0, x >> mip, y >> mip);
         }

         if (level != image) {
            level.close();
         }
      }
   }
}
