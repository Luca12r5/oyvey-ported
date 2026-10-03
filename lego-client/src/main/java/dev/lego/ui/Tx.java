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

   public static DynamicTexture register(Identifier var0, int[] var1, int var2, int var3, boolean var4) {
      NativeImage var5 = new NativeImage(var2, var3, true);
      fill(var5, var1, var2, var3);
      DynamicTexture var6 = new DynamicTexture(() -> "legoclient/" + var0.getPath(), var5);
      tm().register(var0, var6);
      var6.upload();
      if (var4) {
         linear(var6);
      }

      return var6;
   }

   public static void update(DynamicTexture var0, int[] var1, int var2, int var3) {
      fill(var0.getPixels(), var1, var2, var3);
      var0.upload();
   }

   public static void destroy(Identifier var0) {
      try {
         tm().release(var0);
      } catch (Throwable var2) {
      }
   }

   private static void fill(NativeImage var0, int[] var1, int var2, int var3) {
      int var4 = var2 * var3;
      if (fast) {
         try {
            long var5 = var0.getPointer();
            if (var5 != 0L) {
               IntBuffer var7 = MemoryUtil.memIntBuffer(var5, var4);
               int[] var8 = new int[var2];

               for (int var9 = 0; var9 < var3; var9++) {
                  int var10 = var9 * var2;

                  for (int var11 = 0; var11 < var2; var11++) {
                     int var12 = var1[var10 + var11];
                     var8[var11] = var12 & -16711936 | (var12 & 0xFF) << 16 | var12 >> 16 & 0xFF;
                  }

                  var7.put(var8, 0, var2);
               }

               return;
            }
         } catch (Throwable var13) {
            fast = false;
         }
      }

      for (int var14 = 0; var14 < var3; var14++) {
         for (int var6 = 0; var6 < var2; var6++) {
            var0.setPixel(var6, var14, var1[var14 * var2 + var6]);
         }
      }
   }

   public static void linear(AbstractTexture var0) {
      if (!linearBroken && var0 != null) {
         try {
            if (linearSampler == null) {
               Class var1 = Class.forName("com.mojang.blaze3d.systems.RenderSystem");
               Object var2 = null;

               for (Method var6 : var1.getMethods()) {
                  if (var6.getParameterCount() == 0
                     && Modifier.isStatic(var6.getModifiers())
                     && var6.getReturnType().getName().equals(dev.lego.util.Remap.clazz("net.minecraft.class_12136"))) {
                     var2 = var6.invoke(null);
                     break;
                  }
               }

               if (var2 == null) {
                  throw new IllegalStateException("no sampler cache");
               }

               Class var10 = Class.forName("com.mojang.blaze3d.textures.FilterMode");
               Object var11 = null;

               for (Object var8 : var10.getEnumConstants()) {
                  if (((Enum)var8).name().equals("LINEAR")) {
                     var11 = var8;
                  }
               }

               Method var13 = var2.getClass().getMethod(dev.lego.util.Remap.method("net.minecraft.class_12136", "method_75294", "(Lcom/mojang/blaze3d/textures/FilterMode;)Lnet/minecraft/class_12137;"), var10);
               linearSampler = var13.invoke(var2, var11);
               samplerField = AbstractTexture.class.getDeclaredField(dev.lego.util.Remap.field("net.minecraft.class_1044", "field_63613", "Lnet/minecraft/class_12137;"));
               samplerField.setAccessible(true);
            }

            samplerField.set(var0, linearSampler);
         } catch (Throwable var9) {
            linearBroken = true;
            LegoClient.LOG("Linearer Filter nicht verfügbar: " + var9);
         }
      }
   }

   public static void linear(Identifier var0) {
      try {
         linear(tm().getTexture(var0));
      } catch (Throwable var2) {
      }
   }
}
