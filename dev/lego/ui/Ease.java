package dev.lego.ui;

import java.util.HashMap;
import java.util.Map;

public final class Ease {
   private static final Map<String, float[]> VALUES = new HashMap<>();

   private Ease() {
   }

   public static float clamp(float t) {
      return t < 0.0F ? 0.0F : (t > 1.0F ? 1.0F : t);
   }

   public static float outCubic(float t) {
      t = clamp(t);
      float f = 1.0F - t;
      return 1.0F - f * f * f;
   }

   public static float outQuint(float t) {
      t = clamp(t);
      float f = 1.0F - t;
      return 1.0F - f * f * f * f * f;
   }

   public static float inOutCubic(float t) {
      t = clamp(t);
      return t < 0.5F ? 4.0F * t * t * t : 1.0F - (float)Math.pow(-2.0F * t + 2.0F, 3.0) / 2.0F;
   }

   public static float outBack(float t) {
      t = clamp(t);
      float c1 = 1.70158F;
      float c3 = c1 + 1.0F;
      return 1.0F + c3 * (float)Math.pow(t - 1.0F, 3.0) + c1 * (float)Math.pow(t - 1.0F, 2.0);
   }

   public static float outElastic(float t) {
      t = clamp(t);
      return t != 0.0F && t != 1.0F ? (float)(Math.pow(2.0, -10.0F * t) * Math.sin((t * 10.0F - 0.75) * (Math.PI * 2.0 / 3.0)) + 1.0) : t;
   }

   public static float lerp(float a, float b, float t) {
      return a + (b - a) * t;
   }

   public static float stagger(float t, int i, float delay, float dur) {
      return clamp((t - i * delay) / dur);
   }

   public static float to(String key, float target, float speed) {
      float[] v = VALUES.get(key);
      if (v == null) {
         v = new float[]{target};
         VALUES.put(key, v);
         return target;
      } else {
         float k = 1.0F - (float)Math.exp(-Gx.dt * speed * UiSettings.anim());
         v[0] += (target - v[0]) * k;
         if (Math.abs(target - v[0]) < 5.0E-4F) {
            v[0] = target;
         }

         return v[0];
      }
   }

   public static float to(String key, float target, float speed, float from) {
      if (!VALUES.containsKey(key)) {
         VALUES.put(key, new float[]{from});
      }

      return to(key, target, speed);
   }

   public static void set(String key, float v) {
      VALUES.put(key, new float[]{v});
   }

   public static void reset(String prefix) {
      VALUES.keySet().removeIf(k -> k.startsWith(prefix));
   }

   public static float get(String key, float def) {
      float[] v = VALUES.get(key);
      return v == null ? def : v[0];
   }
}
