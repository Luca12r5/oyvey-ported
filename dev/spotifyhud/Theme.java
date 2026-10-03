package dev.spotifyhud;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;

public final class Theme {
   public static final String[] IDS = new String[]{"standard", "cover", "glass", "midnight", "purple", "christmas", "sunset", "ocean"};
   public static final String[] NAMES = new String[]{"Standard", "Cover", "Glas", "Mitternacht", "Lila", "Weihnachten", "Sunset", "Ozean"};
   public static final Color GREEN = new Color(30, 215, 96);
   private static BufferedImage blurSrc;
   private static BufferedImage blurOut;
   private static int blurW;
   private static int blurH;
   private static BufferedImage accentSrc;
   private static Color accentVal;

   private Theme() {
   }

   public static int index(String var0) {
      for (int var1 = 0; var1 < IDS.length; var1++) {
         if (IDS[var1].equals(var0)) {
            return var1;
         }
      }

      return 0;
   }

   public static String name(String var0) {
      return NAMES[index(var0)];
   }

   public static Color accent(String var0, BufferedImage var1) {
      switch (var0) {
         case "cover":
            return var1 != null ? coverAccent(var1) : GREEN;
         case "glass":
            return new Color(255, 255, 255);
         case "midnight":
            return new Color(138, 180, 255);
         case "purple":
            return new Color(214, 140, 255);
         case "christmas":
            return new Color(255, 209, 102);
         case "sunset":
            return new Color(255, 177, 153);
         case "ocean":
            return new Color(79, 209, 197);
         default:
            return GREEN;
      }
   }

   public static void paint(Graphics2D var0, String var1, Shape var2, double var3, double var5, BufferedImage var7, double var8) {
      switch (var1) {
         case "cover":
            if (var7 != null) {
               paintCover(var0, var2, var3, var5, var7, var8);
               return;
            }

            solid(var0, var2, new Color(18, 18, 18, 234));
            return;
         case "glass":
            solid(var0, var2, new Color(22, 22, 28, 150));
            var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(255, 255, 255, 38), 0.0F, (float)(var5 * 0.55), new Color(255, 255, 255, 0)));
            var0.fill(var2);
            return;
         case "midnight":
            gradient(var0, var2, var3, var5, new Color(12, 22, 58, 236), new Color(34, 12, 56, 236));
            glow(var0, var2, var3 * 0.85, var5 * 0.1, var3 * 0.6, new Color(90, 120, 255, 55));
            return;
         case "purple":
            gradient(var0, var2, var3, var5, new Color(38, 8, 64, 238), new Color(88, 52, 150, 238));
            glow(var0, var2, var3 * 0.15, var5 * 0.9, var3 * 0.55, new Color(200, 90, 255, 50));
            return;
         case "christmas":
            gradient(var0, var2, var3, var5, new Color(110, 14, 22, 238), new Color(12, 70, 38, 238));
            glow(var0, var2, var3 * 0.5, 0.0, var3 * 0.5, new Color(255, 210, 120, 40));
            return;
         case "sunset":
            gradient(var0, var2, var3, var5, new Color(140, 40, 30, 238), new Color(120, 18, 70, 238));
            var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(0, 0, 0, 30), 0.0F, (float)var5, new Color(0, 0, 0, 90)));
            var0.fill(var2);
            return;
         case "ocean":
            gradient(var0, var2, var3, var5, new Color(10, 30, 40, 238), new Color(26, 70, 88, 238));
            glow(var0, var2, var3 * 0.2, var5 * 0.1, var3 * 0.5, new Color(79, 209, 197, 40));
            return;
         default:
            solid(var0, var2, new Color(18, 18, 18, 234));
      }
   }

   private static void solid(Graphics2D var0, Shape var1, Color var2) {
      var0.setColor(var2);
      var0.fill(var1);
   }

   private static void gradient(Graphics2D var0, Shape var1, double var2, double var4, Color var6, Color var7) {
      var0.setPaint(new GradientPaint(0.0F, 0.0F, var6, (float)var2, (float)var4, var7));
      var0.fill(var1);
   }

   private static void glow(Graphics2D var0, Shape var1, double var2, double var4, double var6, Color var8) {
      var0.setPaint(
         new RadialGradientPaint(
            new Double(var2, var4), (float)var6, new float[]{0.0F, 1.0F}, new Color[]{var8, new Color(var8.getRed(), var8.getGreen(), var8.getBlue(), 0)}
         )
      );
      var0.fill(var1);
   }

   private static void paintCover(Graphics2D var0, Shape var1, double var2, double var4, BufferedImage var6, double var7) {
      int var9 = Math.max(1, (int)Math.round(var2 * var7));
      int var10 = Math.max(1, (int)Math.round(var4 * var7));
      if (var6 != blurSrc || var9 != blurW || var10 != blurH) {
         blurOut = blurred(var6, var9, var10);
         blurSrc = var6;
         blurW = var9;
         blurH = var10;
      }

      Shape var11 = var0.getClip();
      var0.clip(var1);
      AffineTransform var12 = new AffineTransform();
      var12.scale(var2 / blurOut.getWidth(), var4 / blurOut.getHeight());
      var0.drawImage(blurOut, var12, null);
      var0.setClip(var11);
      var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(0, 0, 0, 105), (float)var2, 0.0F, new Color(0, 0, 0, 150)));
      var0.fill(var1);
   }

   private static BufferedImage blurred(BufferedImage var0, int var1, int var2) {
      double var3 = (double)var1 / var2;
      int var5 = var0.getWidth();
      int var6 = var0.getHeight();
      int var7 = var5;
      int var8 = (int)Math.round(var5 / var3);
      if (var8 > var6) {
         var8 = var6;
         var7 = (int)Math.round(var6 * var3);
      }

      int var9 = (var5 - var7) / 2;
      int var10 = (var6 - var8) / 2;
      BufferedImage var11 = var0.getSubimage(Math.max(0, var9), Math.max(0, var10), Math.max(1, var7), Math.max(1, var8));
      BufferedImage var12 = var11;
      int var13 = var11.getWidth();
      int var14 = var11.getHeight();
      byte var15 = 5;

      int var16;
      for (var16 = Math.max(2, (int)Math.round(var15 * var3)); var13 / 2 >= var16 && var14 / 2 >= var15; var12 = scale(var12, var13, var14)) {
         var13 /= 2;
         var14 /= 2;
      }

      var12 = scale(var12, var16, var15);
      int var17 = var16;

      for (byte var18 = var15; var17 * 2 < var1 && var18 * 2 < var2; var12 = scale(var12, var17, var18)) {
         var17 *= 2;
         var18 *= 2;
      }

      return scale(var12, var1, var2);
   }

   private static BufferedImage scale(BufferedImage var0, int var1, int var2) {
      BufferedImage var3 = new BufferedImage(Math.max(1, var1), Math.max(1, var2), 2);
      Graphics2D var4 = var3.createGraphics();
      var4.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      var4.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var4.drawImage(var0, 0, 0, var3.getWidth(), var3.getHeight(), null);
      var4.dispose();
      return var3;
   }

   public static Color coverAccent(BufferedImage var0) {
      if (var0 == accentSrc && accentVal != null) {
         return accentVal;
      } else {
         BufferedImage var1 = scale(var0, 24, 24);
         double var2 = -1.0;
         float[] var4 = new float[]{0.39F, 0.8F, 0.85F};
         float[] var5 = new float[3];

         for (int var6 = 0; var6 < 24; var6++) {
            for (int var7 = 0; var7 < 24; var7++) {
               int var8 = var1.getRGB(var7, var6);
               Color.RGBtoHSB(var8 >> 16 & 0xFF, var8 >> 8 & 0xFF, var8 & 0xFF, var5);
               double var9 = var5[1] * (0.35 + var5[2]);
               if (!(var5[2] < 0.18) && var9 > var2) {
                  var2 = var9;
                  var4 = (float[])var5.clone();
               }
            }
         }

         Color var11;
         if (var2 < 0.12) {
            var11 = new Color(235, 235, 235);
         } else {
            var11 = Color.getHSBColor(var4[0], Math.min(0.85F, Math.max(0.45F, var4[1])), Math.max(0.85F, var4[2]));
         }

         accentSrc = var0;
         accentVal = var11;
         return var11;
      }
   }
}
