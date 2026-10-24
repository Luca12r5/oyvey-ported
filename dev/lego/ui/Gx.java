package dev.lego.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Gx {
   public static Gx.Backend B;
   public static int S = 1;
   private static float alpha = 1.0F;
   private static final ArrayDeque<Float> ALPHA = new ArrayDeque<>();
   private static long lastNs = 0L;
   public static float dt = 0.016F;
   private static long frameNs;
   private static boolean frameHooked;
   private static final Map<String, Gx.Img> CACHE = new HashMap<>();
   private static long lastClean = 0L;
   private static final Map<Integer, Gx.Atlas> ATLAS = new HashMap<>();

   private Gx() {
   }

   public static void begin(Gx.Backend var0) {
      B = var0;
      S = Math.max(1, var0.guiScale());
      B.push();
      B.scale(1.0F / S, 1.0F / S);
      long var1 = System.nanoTime();
      if (!frameHooked || var1 - frameNs > 250000000L) {
         if (lastNs == 0L) {
            dt = 0.016F;
         } else if (var1 - lastNs > 2000000L) {
            dt = Math.max(0.001F, Math.min(0.1F, (float)(var1 - lastNs) / 1.0E9F));
         }

         if (lastNs == 0L || var1 - lastNs > 2000000L) {
            lastNs = var1;
         }
      }

      alpha = 1.0F;
      ALPHA.clear();
      cleanup();
   }

   public static void newFrame() {
      long var0 = System.nanoTime();
      dt = frameNs == 0L ? 0.016F : Math.max(5.0E-4F, Math.min(0.1F, (float)(var0 - frameNs) / 1.0E9F));
      frameNs = var0;
      frameHooked = true;
   }

   public static void end() {
      try {
         B.pop();
      } catch (Throwable var1) {
      }
   }

   public static void push() {
      B.push();
   }

   public static void pop() {
      B.pop();
   }

   public static void translate(float var0, float var1) {
      B.translate(Math.round(var0), Math.round(var1));
   }

   public static void translateExact(float var0, float var1) {
      B.translate(var0, var1);
   }

   public static void scale(float var0) {
      B.scale(var0, var0);
   }

   public static void scaleAt(float var0, float var1, float var2) {
      translateExact(var0, var1);
      scale(var2);
      translateExact(-var0, -var1);
   }

   public static void pushAlpha(float var0) {
      ALPHA.push(alpha);
      alpha = alpha * Math.max(0.0F, Math.min(1.0F, var0));
   }

   public static void popAlpha() {
      alpha = ALPHA.isEmpty() ? 1.0F : ALPHA.pop();
   }

   public static float alpha() {
      return alpha;
   }

   public static int col(int var0) {
      if (alpha >= 0.999F) {
         return var0;
      } else {
         int var1 = (int)((var0 >>> 24 & 0xFF) * alpha);
         return var1 << 24 | var0 & 16777215;
      }
   }

   public static int withAlpha(int var0, float var1) {
      int var2 = (int)Math.max(0.0F, Math.min(255.0F, (var0 >>> 24 & 0xFF) * var1));
      return var2 << 24 | var0 & 16777215;
   }

   public static int rgba(int var0, float var1) {
      return (int)Math.max(0.0F, Math.min(255.0F, var1 * 255.0F)) << 24 | var0 & 16777215;
   }

   public static int mix(int var0, int var1, float var2) {
      var2 = Math.max(0.0F, Math.min(1.0F, var2));
      int var3 = var0 >>> 24;
      int var4 = var0 >> 16 & 0xFF;
      int var5 = var0 >> 8 & 0xFF;
      int var6 = var0 & 0xFF;
      int var7 = var1 >>> 24;
      int var8 = var1 >> 16 & 0xFF;
      int var9 = var1 >> 8 & 0xFF;
      int var10 = var1 & 0xFF;
      return (int)(var3 + (var7 - var3) * var2) << 24
         | (int)(var4 + (var8 - var4) * var2) << 16
         | (int)(var5 + (var9 - var5) * var2) << 8
         | (int)(var6 + (var10 - var6) * var2);
   }

   public static void clip(int var0, int var1, int var2, int var3) {
      B.clip(var0, var1, var2, var3);
   }

   public static void unclip() {
      B.unclip();
   }

   public static void fill(int var0, int var1, int var2, int var3, int var4) {
      if (var2 > 0 && var3 > 0) {
         B.fill(var0, var1, var2, var3, col(var4));
      }
   }

   public static void gradientV(int var0, int var1, int var2, int var3, int var4, int var5) {
      if (var2 > 0 && var3 > 0) {
         B.gradV(var0, var1, var2, var3, col(var4), col(var5));
      }
   }

   private static void tex(Gx.Img var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9) {
      if (var3 > 0 && var4 > 0) {
         B.tex(var0, var1, var2, var3, var4, var5, var6, var7, var8, col(var9));
      }
   }

   public static void image(Gx.Img var0, int var1, int var2, int var3, int var4, int var5) {
      if (var0 != null) {
         tex(var0, var1, var2, var3, var4, 0, 0, var0.w, var0.h, var5);
      }
   }

   static int bucket(int var0) {
      return var0 <= 24 ? var0 : (var0 + 2) / 4 * 4;
   }

   public static void rect(int var0, int var1, int var2, int var3, int var4, int var5) {
      if (var2 > 0 && var3 > 0) {
         var4 = Math.max(0, Math.min(var4, Math.min(var2, var3) / 2));
         if (var4 <= 0) {
            fill(var0, var1, var2, var3, var5);
         } else {
            int var6 = bucket(var4);
            Gx.Img var7 = circle(var6);
            tex(var7, var0, var1, var4, var4, 0, 0, var6, var6, var5);
            tex(var7, var0 + var2 - var4, var1, var4, var4, var6, 0, var6, var6, var5);
            tex(var7, var0, var1 + var3 - var4, var4, var4, 0, var6, var6, var6, var5);
            tex(var7, var0 + var2 - var4, var1 + var3 - var4, var4, var4, var6, var6, var6, var6, var5);
            fill(var0 + var4, var1, var2 - 2 * var4, var4, var5);
            fill(var0, var1 + var4, var2, var3 - 2 * var4, var5);
            fill(var0 + var4, var1 + var3 - var4, var2 - 2 * var4, var4, var5);
         }
      }
   }

   public static void rectV(int var0, int var1, int var2, int var3, int var4, int var5, int var6) {
      if (var2 > 0 && var3 > 0) {
         var4 = Math.max(0, Math.min(var4, Math.min(var2, var3) / 2));
         if (var4 <= 0) {
            gradientV(var0, var1, var2, var3, var5, var6);
         } else {
            float var7 = (float)var4 / var3;
            int var8 = mix(var5, var6, var7);
            int var9 = mix(var5, var6, 1.0F - var7);
            int var10 = bucket(var4);
            Gx.Img var11 = circle(var10);
            tex(var11, var0, var1, var4, var4, 0, 0, var10, var10, var5);
            tex(var11, var0 + var2 - var4, var1, var4, var4, var10, 0, var10, var10, var5);
            tex(var11, var0, var1 + var3 - var4, var4, var4, 0, var10, var10, var10, var6);
            tex(var11, var0 + var2 - var4, var1 + var3 - var4, var4, var4, var10, var10, var10, var10, var6);
            B.gradV(var0 + var4, var1, var2 - 2 * var4, var4, col(var5), col(var8));
            B.gradV(var0, var1 + var4, var2, var3 - 2 * var4, col(var8), col(var9));
            B.gradV(var0 + var4, var1 + var3 - var4, var2 - 2 * var4, var4, col(var9), col(var6));
         }
      }
   }

   public static void outline(int var0, int var1, int var2, int var3, int var4, int var5, int var6) {
      if (var2 > 0 && var3 > 0 && var5 > 0) {
         var4 = Math.max(var5, Math.min(var4, Math.min(var2, var3) / 2));
         Gx.Img var7 = ring(var4, var5);
         tex(var7, var0, var1, var4, var4, 0, 0, var4, var4, var6);
         tex(var7, var0 + var2 - var4, var1, var4, var4, var4, 0, var4, var4, var6);
         tex(var7, var0, var1 + var3 - var4, var4, var4, 0, var4, var4, var4, var6);
         tex(var7, var0 + var2 - var4, var1 + var3 - var4, var4, var4, var4, var4, var4, var4, var6);
         fill(var0 + var4, var1, var2 - 2 * var4, var5, var6);
         fill(var0 + var4, var1 + var3 - var5, var2 - 2 * var4, var5, var6);
         fill(var0, var1 + var4, var5, var3 - 2 * var4, var6);
         fill(var0 + var2 - var5, var1 + var4, var5, var3 - 2 * var4, var6);
      }
   }

   public static void shadow(int var0, int var1, int var2, int var3, int var4, int var5, float var6) {
      if (var2 > 0 && var3 > 0 && var5 > 0) {
         Gx.Img var7 = shadowTex(var4, var5);
         int var8 = var7.w / 2;
         int var9 = rgba(0, var6);
         int var10 = var0 - var5 * 2;
         int var11 = var1 - var5 * 2 + var5 / 2;
         int var12 = var2 + var5 * 4;
         int var13 = var3 + var5 * 4;
         if (var12 >= 2 * var8 && var13 >= 2 * var8) {
            tex(var7, var10, var11, var8, var8, 0, 0, var8, var8, var9);
            tex(var7, var10 + var12 - var8, var11, var8, var8, var8 + 1, 0, var8, var8, var9);
            tex(var7, var10, var11 + var13 - var8, var8, var8, 0, var8 + 1, var8, var8, var9);
            tex(var7, var10 + var12 - var8, var11 + var13 - var8, var8, var8, var8 + 1, var8 + 1, var8, var8, var9);
            tex(var7, var10 + var8, var11, var12 - 2 * var8, var8, var8, 0, 1, var8, var9);
            tex(var7, var10 + var8, var11 + var13 - var8, var12 - 2 * var8, var8, var8, var8 + 1, 1, var8, var9);
            tex(var7, var10, var11 + var8, var8, var13 - 2 * var8, 0, var8, var8, 1, var9);
            tex(var7, var10 + var12 - var8, var11 + var8, var8, var13 - 2 * var8, var8 + 1, var8, var8, 1, var9);
            tex(var7, var10 + var8, var11 + var8, var12 - 2 * var8, var13 - 2 * var8, var8, var8, 1, 1, var9);
         } else {
            image(var7, var10, var11, var12, var13, var9);
         }
      }
   }

   public static void glow(int var0, int var1, int var2, int var3) {
      if (var2 > 0) {
         Gx.Img var4 = get("glow", 128, () -> {
            short var0x = 128;
            int[] var1x = new int[var0x * var0x];

            for (int var2x = 0; var2x < var0x; var2x++) {
               for (int var3x = 0; var3x < var0x; var3x++) {
                  double var4x = (var3x + 0.5 - var0x / 2.0) / (var0x / 2.0);
                  double var6 = (var2x + 0.5 - var0x / 2.0) / (var0x / 2.0);
                  double var8 = Math.sqrt(var4x * var4x + var6 * var6);
                  double var10 = var8 >= 1.0 ? 0.0 : Math.pow(1.0 - var8, 2.2);
                  var1x[var2x * var0x + var3x] = (int)(var10 * 255.0) << 24 | 16777215;
               }
            }

            return new int[][]{var1x, {var0x, var0x}};
         });
         image(var4, var0 - var2, var1 - var2, var2 * 2, var2 * 2, var3);
      }
   }

   public static void circle(int var0, int var1, int var2, int var3) {
      if (var2 > 0) {
         Gx.Img var4 = circle(bucket(var2));
         image(var4, var0 - var2, var1 - var2, 2 * var2, 2 * var2, var3);
      }
   }

   public static void ringAt(int var0, int var1, int var2, int var3, int var4) {
      if (var2 > 0) {
         int var5 = bucket(var2);
         Gx.Img var6 = ring(var5, Math.max(1, Math.round((float)(Math.min(var3, var2) * var5) / var2)));
         image(var6, var0 - var2, var1 - var2, 2 * var2, 2 * var2, var4);
      }
   }

   public static float text(String var0, float var1, float var2, float var3, int var4, int var5) {
      if (var0 != null && !var0.isEmpty() && !(var3 <= 1.0F)) {
         Gx.Atlas var6 = atlas(var4, var3);
         int var7 = var0.length();
         Gx.Glyph[] var8 = new Gx.Glyph[var7];

         for (int var9 = 0; var9 < var7; var9++) {
            var8[var9] = var6.glyph(var0.charAt(var9));
         }

         var6.flush();
         int var14 = col(var5);
         int var10 = Math.round(var2 + var6.asc);
         float var11 = var1;

         for (int var12 = 0; var12 < var7; var12++) {
            Gx.Glyph var13 = var8[var12];
            if (var13.w > 0) {
               B.tex(var13.page.img, Math.round(var11) + var13.ox, var10 + var13.oy, var13.w, var13.h, var13.u, var13.v, var13.w, var13.h, var14);
            }

            var11 += var13.adv;
         }

         return var11 - var1;
      } else {
         return 0.0F;
      }
   }

   public static float textMid(String var0, float var1, float var2, float var3, int var4, int var5) {
      return text(var0, var1, topFor(var2, var3, var4), var3, var4, var5);
   }

   public static float textCenter(String var0, float var1, float var2, float var3, int var4, int var5) {
      float var6 = width(var0, var3, var4);
      return text(var0, var1 - var6 / 2.0F, topFor(var2, var3, var4), var3, var4, var5);
   }

   public static float textRight(String var0, float var1, float var2, float var3, int var4, int var5) {
      float var6 = width(var0, var3, var4);
      return text(var0, var1 - var6, topFor(var2, var3, var4), var3, var4, var5);
   }

   public static float width(String var0, float var1, int var2) {
      if (var0 != null && !var0.isEmpty()) {
         Gx.Atlas var3 = atlas(var2, var1);
         float var4 = 0.0F;

         for (int var5 = 0; var5 < var0.length(); var5++) {
            var4 += var3.advance(var0.charAt(var5));
         }

         return var4;
      } else {
         return 0.0F;
      }
   }

   public static void textSpaced(String var0, float var1, float var2, float var3, int var4, int var5, float var6) {
      float var7 = 0.0F;

      for (int var8 = 0; var8 < var0.length(); var8++) {
         var7 += width(String.valueOf(var0.charAt(var8)), var3, var4) + (var8 < var0.length() - 1 ? var6 : 0.0F);
      }

      float var12 = var1 - var7 / 2.0F;
      float var9 = topFor(var2, var3, var4);

      for (int var10 = 0; var10 < var0.length(); var10++) {
         String var11 = String.valueOf(var0.charAt(var10));
         if (!var11.equals(" ")) {
            text(var11, var12, var9, var3, var4, var5);
         }

         var12 += width(var11, var3, var4) + var6;
      }
   }

   public static float spacedWidth(String var0, float var1, int var2, float var3) {
      float var4 = 0.0F;

      for (int var5 = 0; var5 < var0.length(); var5++) {
         var4 += width(String.valueOf(var0.charAt(var5)), var1, var2) + (var5 < var0.length() - 1 ? var3 : 0.0F);
      }

      return var4;
   }

   public static float topFor(float var0, float var1, int var2) {
      float var3 = Fonts.ascent(var2, var1);
      float var4 = var1 * 0.7F;
      return var0 + var4 / 2.0F - var3;
   }

   public static void icon(String var0, float var1, float var2, float var3, int var4) {
      int var5 = Math.max(4, Math.round(var3));
      int var6 = var5 <= 20 ? var5 : (var5 + 2) / 4 * 4;
      Gx.Img var7 = get("icon:" + var0 + ":" + var6, var6 + 4, () -> {
         int var2x = var6 + 4;
         BufferedImage var3x = new BufferedImage(var2x, var2x, 2);
         Graphics2D var4x = var3x.createGraphics();
         hints(var4x);
         var4x.translate(2, 2);
         Icons.paint(var4x, var0, var6, Color.WHITE);
         var4x.dispose();
         return new int[][]{whiten(var3x.getRGB(0, 0, var2x, var2x, null, 0, var2x)), {var2x, var2x}};
      });
      int var8 = Math.round((float)(var7.w * var5) / var6);
      tex(var7, Math.round(var1 - var8 / 2.0F), Math.round(var2 - var8 / 2.0F), var8, var8, 0, 0, var7.w, var7.h, var4);
   }

   public static Gx.Img get(String var0, int var1, Supplier<int[][]> var2) {
      Gx.Img var3 = CACHE.get(var0);
      if (var3 == null) {
         int[][] var4 = (int[][])var2.get();
         var3 = new Gx.Img(B.register(var4[0], var4[1][0], var4[1][1]), var4[1][0], var4[1][1], var4.length > 2 ? var4[2][0] : 0);
         CACHE.put(var0, var3);
      }

      var3.used = System.currentTimeMillis();
      return var3;
   }

   public static Gx.Img peek(String var0) {
      Gx.Img var1 = CACHE.get(var0);
      if (var1 != null) {
         var1.used = System.currentTimeMillis();
      }

      return var1;
   }

   public static Gx.Img painted(String var0, int var1, int var2, Consumer<Graphics2D> var3) {
      return get("p:" + var0 + ":" + var1 + "x" + var2, 0, () -> {
         BufferedImage var3x = new BufferedImage(Math.max(1, var1), Math.max(1, var2), 2);
         Graphics2D var4 = var3x.createGraphics();
         hints(var4);

         try {
            var3.accept(var4);
         } finally {
            var4.dispose();
         }

         return new int[][]{var3x.getRGB(0, 0, var3x.getWidth(), var3x.getHeight(), null, 0, var3x.getWidth()), {var3x.getWidth(), var3x.getHeight()}};
      });
   }

   private static void cleanup() {
      long var0 = System.currentTimeMillis();
      if (var0 - lastClean >= 5000L) {
         lastClean = var0;
         Iterator var2 = CACHE.entrySet().iterator();

         while (var2.hasNext()) {
            Entry var3 = (Entry)var2.next();
            long var4 = var0 - ((Gx.Img)var3.getValue()).used;
            boolean var6 = ((String)var3.getKey()).startsWith("t:");
            if (var4 > (var6 ? 20000 : '\uea60')) {
               B.destroy((Gx.Img)var3.getValue());
               var2.remove();
            }
         }
      }
   }

   public static void clearCache() {
      for (Gx.Img var1 : CACHE.values()) {
         B.destroy(var1);
      }

      CACHE.clear();
   }

   private static Gx.Img circle(int var0) {
      return get("circle:" + var0, 0, () -> {
         int var1 = 2 * var0;
         int[] var2 = new int[var1 * var1];

         for (int var3 = 0; var3 < var1; var3++) {
            for (int var4 = 0; var4 < var1; var4++) {
               int var5 = 0;

               for (int var6 = 0; var6 < 4; var6++) {
                  for (int var7 = 0; var7 < 4; var7++) {
                     double var8 = var4 + (var7 + 0.5) / 4.0 - var0;
                     double var10 = var3 + (var6 + 0.5) / 4.0 - var0;
                     if (var8 * var8 + var10 * var10 <= (double)var0 * var0) {
                        var5++;
                     }
                  }
               }

               var2[var3 * var1 + var4] = var5 * 255 / 16 << 24 | 16777215;
            }
         }

         return new int[][]{var2, {var1, var1}};
      });
   }

   private static Gx.Img ring(int var0, int var1) {
      return get("ring:" + var0 + ":" + var1, 0, () -> {
         int var2 = 2 * var0;
         int[] var3 = new int[var2 * var2];
         double var4 = var0 - var1;

         for (int var6 = 0; var6 < var2; var6++) {
            for (int var7 = 0; var7 < var2; var7++) {
               int var8 = 0;

               for (int var9 = 0; var9 < 4; var9++) {
                  for (int var10 = 0; var10 < 4; var10++) {
                     double var11 = var7 + (var10 + 0.5) / 4.0 - var0;
                     double var13 = var6 + (var9 + 0.5) / 4.0 - var0;
                     double var15 = var11 * var11 + var13 * var13;
                     if (var15 <= (double)var0 * var0 && var15 >= var4 * var4) {
                        var8++;
                     }
                  }
               }

               var3[var6 * var2 + var7] = var8 * 255 / 16 << 24 | 16777215;
            }
         }

         return new int[][]{var3, {var2, var2}};
      });
   }

   private static Gx.Img shadowTex(int var0, int var1) {
      return get("shadow:" + var0 + ":" + var1, 0, () -> {
         int var2 = var0 + var1 * 2;
         int var3 = 2 * var2 + 1;
         float[] var4 = new float[var3 * var3];
         int var5 = var1 * 2;

         for (int var6 = 0; var6 < var3; var6++) {
            for (int var7 = 0; var7 < var3; var7++) {
               double var8 = Math.max(Math.abs(var7 + 0.5 - var3 / 2.0) - (var3 / 2.0 - var5 - var0), 0.0);
               double var10 = Math.max(Math.abs(var6 + 0.5 - var3 / 2.0) - (var3 / 2.0 - var5 - var0), 0.0);
               var4[var6 * var3 + var7] = Math.sqrt(var8 * var8 + var10 * var10) <= var0 ? 1.0F : 0.0F;
            }
         }

         for (int var12 = 0; var12 < 3; var12++) {
            var4 = boxBlur(var4, var3, Math.max(1, var1 / 2));
         }

         int[] var13 = new int[var3 * var3];

         for (int var14 = 0; var14 < var13.length; var14++) {
            var13[var14] = (int)(Math.min(1.0F, var4[var14]) * 255.0F) << 24;
         }

         return new int[][]{var13, {var3, var3}};
      });
   }

   private static float[] boxBlur(float[] var0, int var1, int var2) {
      float[] var3 = new float[var1 * var1];
      float[] var4 = new float[var1 * var1];

      for (int var5 = 0; var5 < var1; var5++) {
         for (int var6 = 0; var6 < var1; var6++) {
            float var7 = 0.0F;
            int var8 = 0;

            for (int var9 = -var2; var9 <= var2; var9++) {
               int var10 = var6 + var9;
               if (var10 >= 0 && var10 < var1) {
                  var7 += var0[var5 * var1 + var10];
               }

               var8++;
            }

            var3[var5 * var1 + var6] = var7 / var8;
         }
      }

      for (int var11 = 0; var11 < var1; var11++) {
         for (int var12 = 0; var12 < var1; var12++) {
            float var13 = 0.0F;
            int var14 = 0;

            for (int var15 = -var2; var15 <= var2; var15++) {
               int var16 = var11 + var15;
               if (var16 >= 0 && var16 < var1) {
                  var13 += var3[var16 * var1 + var12];
               }

               var14++;
            }

            var4[var11 * var1 + var12] = var13 / var14;
         }
      }

      return var4;
   }

   static Gx.Atlas atlas(int var0, float var1) {
      int var2 = Math.max(4, Math.min(400, Math.round(var1)));
      int var3 = var0 * 1000 + var2;
      Gx.Atlas var4 = ATLAS.get(var3);
      if (var4 == null) {
         var4 = new Gx.Atlas(var0, var2);
         ATLAS.put(var3, var4);
      }

      return var4;
   }

   private static int[] whiten(int[] var0) {
      for (int var1 = 0; var1 < var0.length; var1++) {
         var0[var1] = var0[var1] & 0xFF000000 | 16777215;
      }

      return var0;
   }

   public static void hints(Graphics2D var0) {
      var0.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var0.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      var0.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      var0.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      var0.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var0.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      var0.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
   }

   static final class Atlas {
      final Font font;
      final int px;
      final int weight;
      final float asc;
      final List<Gx.Page> pages = new ArrayList<>();
      final Map<Character, Gx.Glyph> glyphs = new HashMap<>();
      final Map<Character, Float> adv = new HashMap<>();

      Atlas(int var1, int var2) {
         this.weight = var1;
         this.px = var2;
         this.font = Fonts.get(var1, var2);
         this.asc = Fonts.ascent(var1, var2);
      }

      private Font fontFor(char var1) {
         return this.font.canDisplay(var1) ? this.font : new Font("Dialog", this.weight >= 3 ? 1 : 0, this.px);
      }

      float advance(char var1) {
         Gx.Glyph var2 = this.glyphs.get(var1);
         if (var2 != null) {
            return var2.adv;
         } else {
            Float var3 = this.adv.get(var1);
            if (var3 == null) {
               GlyphVector var4 = this.fontFor(var1).createGlyphVector(Fonts.FRC, String.valueOf(var1));
               var3 = var4.getGlyphMetrics(0).getAdvance();
               this.adv.put(var1, var3);
            }

            return var3;
         }
      }

      Gx.Glyph glyph(char var1) {
         Gx.Glyph var2 = this.glyphs.get(var1);
         if (var2 != null) {
            return var2;
         } else {
            var2 = new Gx.Glyph();
            Font var3 = this.fontFor(var1);
            GlyphVector var4 = var3.createGlyphVector(Fonts.FRC, String.valueOf(var1));
            var2.adv = var4.getGlyphMetrics(0).getAdvance();
            Rectangle var5 = var4.getPixelBounds(Fonts.FRC, 0.0F, 0.0F);
            if (var5.width > 0 && var5.height > 0 && !Character.isWhitespace(var1)) {
               int var6 = var5.width + 4;
               int var7 = var5.height + 4;
               BufferedImage var8 = new BufferedImage(var6, var7, 2);
               Graphics2D var9 = var8.createGraphics();
               Gx.hints(var9);
               var9.setColor(Color.WHITE);
               var9.drawGlyphVector(var4, 2 - var5.x, 2 - var5.y);
               var9.dispose();
               Gx.Page var10 = this.place(var6, var7);
               var2.page = var10;
               var2.u = var10.cx;
               var2.v = var10.cy;
               var2.w = var6;
               var2.h = var7;
               var2.ox = var5.x - 2;
               var2.oy = var5.y - 2;
               int[] var11 = var8.getRGB(0, 0, var6, var7, null, 0, var6);

               for (int var12 = 0; var12 < var7; var12++) {
                  for (int var13 = 0; var13 < var6; var13++) {
                     var10.px[(var10.cy + var12) * var10.size + var10.cx + var13] = var11[var12 * var6 + var13] & 0xFF000000 | 16777215;
                  }
               }

               var10.cx += var6 + 1;
               var10.rowH = Math.max(var10.rowH, var7);
               var10.dirty = true;
            }

            this.glyphs.put(var1, var2);
            return var2;
         }
      }

      private Gx.Page place(int var1, int var2) {
         Gx.Page var3 = this.pages.isEmpty() ? null : this.pages.get(this.pages.size() - 1);
         if (var3 != null && var3.cx + var1 + 1 > var3.size) {
            var3.cx = 1;
            var3.cy = var3.cy + var3.rowH + 1;
            var3.rowH = 0;
         }

         if (var3 == null || var3.cy + var2 + 1 > var3.size) {
            int var4 = this.px <= 18 ? 256 : (this.px <= 36 ? 512 : (this.px <= 72 ? 1024 : 2048));

            while (var4 < Math.max(var1, var2) + 2) {
               var4 *= 2;
            }

            var3 = new Gx.Page(var4);
            this.pages.add(var3);
         }

         return var3;
      }

      void flush() {
         for (Gx.Page var2 : this.pages) {
            if (var2.img == null) {
               var2.img = new Gx.Img(Gx.B.register(var2.px, var2.size, var2.size), var2.size, var2.size, 0);
               var2.dirty = false;
            } else if (var2.dirty) {
               Gx.B.update(var2.img, var2.px);
               var2.dirty = false;
            }
         }
      }
   }

   public interface Backend {
      int guiScale();

      void push();

      void pop();

      void translate(float var1, float var2);

      void scale(float var1, float var2);

      void fill(int var1, int var2, int var3, int var4, int var5);

      void gradV(int var1, int var2, int var3, int var4, int var5, int var6);

      void tex(Gx.Img var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10);

      void clip(int var1, int var2, int var3, int var4);

      void unclip();

      Object register(int[] var1, int var2, int var3);

      void update(Gx.Img var1, int[] var2);

      void destroy(Gx.Img var1);

      int[] screenPx();
   }

   static final class Glyph {
      Gx.Page page;
      int u;
      int v;
      int w;
      int h;
      int ox;
      int oy;
      float adv;
   }

   public static final class Img {
      public final Object handle;
      public final int w;
      public final int h;
      final int pad;
      long used;

      Img(Object var1, int var2, int var3, int var4) {
         this.handle = var1;
         this.w = var2;
         this.h = var3;
         this.pad = var4;
      }
   }

   static final class Page {
      final int size;
      final int[] px;
      Gx.Img img;
      int cx = 1;
      int cy = 1;
      int rowH = 0;
      boolean dirty;

      Page(int var1) {
         this.size = var1;
         this.px = new int[var1 * var1];
         Arrays.fill(this.px, 16777215);
      }
   }
}
