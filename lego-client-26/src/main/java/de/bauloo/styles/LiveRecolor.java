package de.bauloo.styles;

import com.mojang.blaze3d.platform.NativeImage;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents.AnimationState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;

public final class LiveRecolor {
   public static final int GRAY = -2;
   public static final int MONO = -3;
   public static final Map<SpriteContents, AnimationState> ANIMATORS = new WeakHashMap<>();

   private LiveRecolor() {
   }

   public static void apply(SpriteId id, int color) {
      Minecraft mc = Minecraft.getInstance();
      TextureAtlas atlas = AtlasWriter.atlas(id.atlasLocation());
      if (atlas != null) {
         TextureAtlasSprite sprite = atlas.getSprite(id.texture());
         if (sprite != null && sprite != atlas.missingSprite()) {
            SpriteContents contents = sprite.contents();
            NativeImage source = ((SpriteContentsAccessor)contents).baulooStyles$image();
            int w = contents.width();
            int h = contents.height();
            if (source != null && source.getWidth() >= w && source.getHeight() >= h) {
               AnimationState animator = ANIMATORS.get(contents);
               if (animator == null) {
                  AtlasWriter.write(atlas, id.texture(), frame(source, 0, 0, w, h, color));
               } else {
                  int columns = Math.max(1, source.getWidth() / w);
                  Int2ObjectMap<GpuTextureView> frames = ((SpriteAnimatorAccessor)animator).baulooStyles$frames();
                  CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
                  ObjectIterator var13 = frames.int2ObjectEntrySet().iterator();

                  while (var13.hasNext()) {
                     Entry<GpuTextureView> entry = (Entry<GpuTextureView>)var13.next();
                     int index = entry.getIntKey();
                     int fx = index % columns * w;
                     int fy = index / columns * h;
                     if (fy + h <= source.getHeight()) {
                        GpuTexture texture = ((GpuTextureView)entry.getValue()).texture();
                        if (!texture.isClosed()) {
                           NativeImage level = AtlasWriter.toNative(frame(source, fx, fy, w, h, color));

                           for (int mip = 0; mip < texture.getMipLevels() && w >> mip >= 1 && h >> mip >= 1; mip++) {
                              if (mip > 0) {
                                 NativeImage smaller = AtlasWriter.half(level);
                                 level.close();
                                 level = smaller;
                              }

                              encoder.writeToTexture(texture, level, mip, 0, 0, 0);
                           }

                           level.close();
                        }
                     }
                  }
               }
            } else {
               BaulooStylesClient.LOGGER.info("[LiveRecolor] {} has no pixels", id.texture());
            }
         } else {
            BaulooStylesClient.LOGGER.info("[LiveRecolor] {} missing in {}", id.texture(), id.atlasLocation());
         }
      }
   }

   public static void applyImage(TextureAtlas atlas, Identifier spriteId, BufferedImage image) {
      TextureAtlasSprite sprite = atlas.getSprite(spriteId);
      if (sprite != null && sprite != atlas.missingSprite()) {
         SpriteContents contents = sprite.contents();
         int w = contents.width();
         int h = contents.height();
         AnimationState animator = ANIMATORS.get(contents);
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
                     NativeImage level = AtlasWriter.toNative(image.getSubimage(fx, fy, w, h));

                     for (int mip = 0; mip < texture.getMipLevels() && w >> mip >= 1 && h >> mip >= 1; mip++) {
                        if (mip > 0) {
                           NativeImage smaller = AtlasWriter.half(level);
                           level.close();
                           level = smaller;
                        }

                        encoder.writeToTexture(texture, level, mip, 0, 0, 0);
                     }

                     level.close();
                  }
               }
            }
         }
      }
   }

   private static BufferedImage frame(NativeImage source, int fx, int fy, int w, int h, int color) {
      BufferedImage out = new BufferedImage(w, h, 2);
      float[] target = color < 0 ? null : Color.RGBtoHSB(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, null);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int c = source.getPixel(fx + x, fy + y);
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
