package dev.lego.ui;

import java.util.HashMap;
import java.util.Map;

public final class Ease {
   private static final Map<String, float[]> VALUES = new HashMap<>();

   private Ease() {
   }

   public static float clamp(float var0) {
      return var0 < 0.0F ? 0.0F : (var0 > 1.0F ? 1.0F : var0);
   }

   public static float outCubic(float var0) {
      var0 = clamp(var0);
      float var1 = 1.0F - var0;
      return 1.0F - var1 * var1 * var1;
   }

   public static float outQuint(float var0) {
      var0 = clamp(var0);
      float var1 = 1.0F - var0;
      return 1.0F - var1 * var1 * var1 * var1 * var1;
   }

   public static float inOutCubic(float var0) {
      var0 = clamp(var0);
      return var0 < 0.5F ? 4.0F * var0 * var0 * var0 : 1.0F - (float)Math.pow(-2.0F * var0 + 2.0F, 3.0) / 2.0F;
   }

   public static float outBack(float var0) {
      var0 = clamp(var0);
      float var1 = 1.70158F;
      float var2 = var1 + 1.0F;
      return 1.0F + var2 * (float)Math.pow(var0 - 1.0F, 3.0) + var1 * (float)Math.pow(var0 - 1.0F, 2.0);
   }

   public static float outElastic(float var0) {
      var0 = clamp(var0);
      return var0 != 0.0F && var0 != 1.0F ? (float)(Math.pow(2.0, -10.0F * var0) * Math.sin((var0 * 10.0F - 0.75) * (Math.PI * 2.0 / 3.0)) + 1.0) : var0;
   }

   public static float lerp(float var0, float var1, float var2) {
      return var0 + (var1 - var0) * var2;
   }

   public static float stagger(float var0, int var1, float var2, float var3) {
      return clamp((var0 - var1 * var2) / var3);
   }

   public static float to(String var0, float var1, float var2) {
      float[] var3 = VALUES.get(var0);
      if (var3 == null) {
         var3 = new float[]{var1};
         VALUES.put(var0, var3);
         return var1;
      } else {
         float var4 = 1.0F - (float)Math.exp(-Gx.dt * var2 * UiSettings.anim());
         var3[0] += (var1 - var3[0]) * var4;
         if (Math.abs(var1 - var3[0]) < 5.0E-4F) {
            var3[0] = var1;
         }

         return var3[0];
      }
   }

   public static float to(String var0, float var1, float var2, float var3) {
      if (!VALUES.containsKey(var0)) {
         VALUES.put(var0, new float[]{var3});
      }

      return to(var0, var1, var2);
   }

   public static void set(String var0, float var1) {
      VALUES.put(var0, new float[]{var1});
   }

   public static void reset(String var0) {
      VALUES.keySet().removeIf(var1 -> var1.startsWith(var0));
   }

   public static float get(String var0, float var1) {
      float[] var2 = VALUES.get(var0);
      return var2 == null ? var1 : var2[0];
   }
}
