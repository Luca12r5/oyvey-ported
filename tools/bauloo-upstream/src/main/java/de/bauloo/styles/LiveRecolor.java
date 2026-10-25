package de.bauloo.styles;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import de.bauloo.styles.mixin.SpriteAnimatorAccessor;
import de.bauloo.styles.mixin.SpriteContentsAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.class_1011;
import net.minecraft.class_1058;
import net.minecraft.class_1059;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4730;
import net.minecraft.class_7764;
import net.minecraft.class_7764.class_12298;

public final class LiveRecolor {
   public static final int GRAY = -2;
   public static final int MONO = -3;
   public static final Map<class_7764, class_12298> ANIMATORS = new WeakHashMap<>();

   private LiveRecolor() {
   }

   public static void apply(class_4730 id, int color) {
      class_310 mc = class_310.method_1551();
      class_1059 atlas = AtlasWriter.atlas(id.method_24144());
      if (atlas != null) {
         class_1058 sprite = atlas.method_4608(id.method_24147());
         if (sprite != null && sprite != atlas.method_73023()) {
            class_7764 contents = sprite.method_45851();
            class_1011 source = ((SpriteContentsAccessor)contents).baulooStyles$image();
            int w = contents.method_45807();
            int h = contents.method_45815();
            if (source != null && source.method_4307() >= w && source.method_4323() >= h) {
               class_12298 animator = ANIMATORS.get(contents);
               if (animator == null) {
                  AtlasWriter.write(atlas, id.method_24147(), frame(source, 0, 0, w, h, color));
               } else {
                  int columns = Math.max(1, source.method_4307() / w);
                  Int2ObjectMap<GpuTextureView> frames = ((SpriteAnimatorAccessor)animator).baulooStyles$frames();
                  CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
                  ObjectIterator var13 = frames.int2ObjectEntrySet().iterator();

                  while (var13.hasNext()) {
                     Entry<GpuTextureView> entry = (Entry<GpuTextureView>)var13.next();
                     int index = entry.getIntKey();
                     int fx = index % columns * w;
                     int fy = index / columns * h;
                     if (fy + h <= source.method_4323()) {
                        GpuTexture texture = ((GpuTextureView)entry.getValue()).texture();
                        if (!texture.isClosed()) {
                           class_1011 level = AtlasWriter.toNative(frame(source, fx, fy, w, h, color));

                           for (int mip = 0; mip < texture.getMipLevels() && w >> mip >= 1 && h >> mip >= 1; mip++) {
                              if (mip > 0) {
                                 class_1011 smaller = AtlasWriter.half(level);
                                 level.close();
                                 level = smaller;
                              }

                              encoder.writeToTexture(texture, level, mip, 0, 0, 0, w >> mip, h >> mip, 0, 0);
                           }

                           level.close();
                        }
                     }
                  }
               }
            } else {
               BaulooStylesClient.LOGGER.info("[LiveRecolor] {} has no pixels", id.method_24147());
            }
         } else {
            BaulooStylesClient.LOGGER.info("[LiveRecolor] {} missing in {}", id.method_24147(), id.method_24144());
         }
      }
   }

   public static void applyImage(class_1059 atlas, class_2960 spriteId, BufferedImage image) {
      class_1058 sprite = atlas.method_4608(spriteId);
      if (sprite != null && sprite != atlas.method_73023()) {
         class_7764 contents = sprite.method_45851();
         int w = contents.method_45807();
         int h = contents.method_45815();
         class_12298 animator = ANIMATORS.get(contents);
         if (animator == null) {
            AtlasWriter.write(atlas, spriteId, image);
         } else {
            int columns = Math.max(1, image.getWidth() / Math.max(1, w));
            Int2ObjectMap<GpuTextureView> frames = ((SpriteAnimatorAccessor)animator).baulooStyles$frames();
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            ObjectIterator var11 = frames.int2ObjectEntrySet().iterator();

            while (var11.hasNext()) {
               Entry<GpuTextureView> entry = (Entry<GpuTextureView>)var11.next();
               int index = entry.getIntKey();
               int fx = index % columns * w;
               int fy = index / columns * h;
               if (fx + w <= image.getWidth() && fy + h <= image.getHeight()) {
                  GpuTexture texture = ((GpuTextureView)entry.getValue()).texture();
                  if (!texture.isClosed()) {
                     class_1011 level = AtlasWriter.toNative(image.getSubimage(fx, fy, w, h));

                     for (int mip = 0; mip < texture.getMipLevels() && w >> mip >= 1 && h >> mip >= 1; mip++) {
                        if (mip > 0) {
                           class_1011 smaller = AtlasWriter.half(level);
                           level.close();
                           level = smaller;
                        }

                        encoder.writeToTexture(texture, level, mip, 0, 0, 0, w >> mip, h >> mip, 0, 0);
                     }

                     level.close();
                  }
               }
            }
         }
      }
   }

   private static BufferedImage frame(class_1011 source, int fx, int fy, int w, int h, int color) {
      BufferedImage out = new BufferedImage(w, h, 2);
      float[] target = color < 0 ? null : Color.RGBtoHSB(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, null);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int c = source.method_61940(fx + x, fy + y);
            if (color == -3 && c >>> 24 > 0) {
               float lum = 0.299F * (c >> 16 & 0xFF) + 0.587F * (c >> 8 & 0xFF) + 0.114F * (c & 0xFF);
               int v = Math.round(lum * 0.87F);
               c = c & 0xFF000000 | v << 16 | v << 8 | v;
            } else if (color == -2 && c >>> 24 > 0) {
               float lum = (0.3F * (c >> 16 & 0xFF) + 0.59F * (c >> 8 & 0xFF) + 0.11F * (c & 0xFF)) / 255.0F;
               int v = Math.round(Math.min(1.0F, 0.35F + lum * 1.3F) * 255.0F);
               c = c & 0xFF000000 | v << 16 | v << 8 | v;
            } else if (target != null && c >>> 24 > 0) {
               float[] hsb = Color.RGBtoHSB(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF, null);
               int rgb = Color.HSBtoRGB(target[0], target[1] * (0.35F + 0.65F * hsb[1]), Math.max(hsb[2], target[2] * 0.3F));
               c = c & 0xFF000000 | rgb & 16777215;
            }

            out.setRGB(x, y, c);
         }
      }

      return out;
   }
}
