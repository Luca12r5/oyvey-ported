package dev.lego.ui;

import com.mojang.blaze3d.platform.NativeImage;
import dev.lego.LegoClient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.IntBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryUtil;

public final class Tx {
   private static boolean fast = true;
   private static boolean linearBroken = false;
   private static Object linearSampler;
   private static Field samplerField;

   private Tx() {
   }

   public static TextureManager tm() {
      return Minecraft.getInstance().getTextureManager();
   }

   public static DynamicTexture register(Identifier id, int[] argb, int w, int h, boolean linear) {
      NativeImage img = new NativeImage(w, h, true);
      fill(img, argb, w, h);
      DynamicTexture tex = new DynamicTexture(() -> "legoclient/" + id.getPath(), img);
      tm().register(id, tex);
      tex.upload();
      if (linear) {
         linear(tex);
      }

      return tex;
   }

   public static void update(DynamicTexture tex, int[] argb, int w, int h) {
      fill(tex.getPixels(), argb, w, h);
      tex.upload();
   }

   public static void destroy(Identifier id) {
      try {
         tm().release(id);
      } catch (Throwable var2) {
      }
   }

   private static void fill(NativeImage img, int[] argb, int w, int h) {
      int n = w * h;
      if (fast) {
         try {
            long ptr = img.getPointer();
            if (ptr != 0L) {
               IntBuffer buf = MemoryUtil.memIntBuffer(ptr, n);
               int[] row = new int[w];

               for (int y = 0; y < h; y++) {
                  int o = y * w;

                  for (int x = 0; x < w; x++) {
                     int c = argb[o + x];
                     row[x] = c & -16711936 | (c & 0xFF) << 16 | c >> 16 & 0xFF;
                  }

                  buf.put(row, 0, w);
               }

               return;
            }
         } catch (Throwable var13) {
            fast = false;
         }
      }

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            img.setPixel(x, y, argb[y * w + x]);
         }
      }
   }

   public static void linear(AbstractTexture tex) {
      if (!linearBroken && tex != null) {
         try {
            if (linearSampler == null) {
               Class<?> rs = Class.forName("com.mojang.blaze3d.systems.RenderSystem");
               Object cache = null;

               for (Method m : rs.getMethods()) {
                  if (m.getParameterCount() == 0 && Modifier.isStatic(m.getModifiers()) && m.getReturnType().getName().equals("net.minecraft.class_12136")) {
                     cache = m.invoke(null);
                     break;
                  }
               }

               if (cache == null) {
                  throw new IllegalStateException("no sampler cache");
               }

               Class<?> fm = Class.forName("com.mojang.blaze3d.textures.FilterMode");
               Object lin = null;

               for (Object c : fm.getEnumConstants()) {
                  if (((Enum)c).name().equals("LINEAR")) {
                     lin = c;
                  }
               }

               Method get = cache.getClass().getMethod("method_75294", fm);
               linearSampler = get.invoke(cache, lin);
               samplerField = AbstractTexture.class.getDeclaredField("field_63613");
               samplerField.setAccessible(true);
            }

            samplerField.set(tex, linearSampler);
         } catch (Throwable var9) {
            linearBroken = true;
            LegoClient.LOG("Linearer Filter nicht verfügbar: " + var9);
         }
      }
   }

   public static void linear(Identifier id) {
      try {
         linear(tm().getTexture(id));
      } catch (Throwable var2) {
      }
   }
}
