package dev.spotifyhud;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D.Double;

public final class Anim {
   public static final String[] IDS = new String[]{"none", "stars", "snow", "lights", "sparkle", "aurora", "waves", "xmas"};
   public static final String[] NAMES = new String[]{"Aus", "Sternschnuppen", "Schnee", "Lichterkette", "Glitzer", "Aurora", "Wellen", "Weihnachten"};
   private static final Color[] BULBS = new Color[]{new Color(255, 70, 70), new Color(80, 220, 110), new Color(255, 205, 80), new Color(90, 160, 255)};

   private Anim() {
   }

   public static int index(String var0) {
      for (int var1 = 0; var1 < IDS.length; var1++) {
         if (IDS[var1].equals(var0)) {
            return var1;
         }
      }

      return 0;
   }

   public static boolean active(String var0) {
      return var0 != null && !"none".equals(var0);
   }

   public static double rnd(int var0, int var1) {
      long var2 = var0 * -7046029254386353131L + var1 * -4417276706812531889L;
      var2 ^= var2 >>> 33;
      var2 *= -49064778989728563L;
      var2 ^= var2 >>> 33;
      var2 *= -4265267296055464877L;
      var2 ^= var2 >>> 33;
      return (var2 >>> 11) * 1.110223E-16F;
   }

   public static void draw(Graphics2D var0, String var1, double var2, double var4, double var6, double var8, Color var10) {
      var8 = Math.max(0.2, Math.min(1.0, var8));
      switch (var1) {
         case "stars":
            stars(var0, var2, var4, var6, var8);
            break;
         case "snow":
            snow(var0, var2, var4, var6, var8);
            break;
         case "lights":
            lights(var0, var2, var4, var6, var8);
            break;
         case "sparkle":
            sparkle(var0, var2, var4, var6, var8, var10);
            break;
         case "aurora":
            aurora(var0, var2, var4, var6, var8);
            break;
         case "waves":
            waves(var0, var2, var4, var6, var8, var10);
            break;
         case "xmas":
            snow(var0, var2, var4, var6, var8 * 0.8);
            lights(var0, var2, var4, var6, var8);
      }
   }

   private static Color c(int var0, int var1, int var2, double var3) {
      return new Color(var0, var1, var2, (int)Math.max(0L, Math.min(255L, Math.round(var3 * 255.0))));
   }

   private static void stars(Graphics2D var0, double var1, double var3, double var5, double var7) {
      int var9 = (int)(26.0 * var7);

      for (int var10 = 0; var10 < var9; var10++) {
         double var11 = rnd(var10, 1) * var3;
         double var13 = rnd(var10, 2) * var5;
         double var15 = 0.5 + 0.5 * Math.sin(var1 * (1.2 + rnd(var10, 3) * 2.5) + rnd(var10, 4) * 6.28);
         double var17 = 0.25 + rnd(var10, 5) * 0.45;
         var0.setColor(c(255, 255, 255, (0.15 + 0.55 * var15) * var7));
         var0.fill(new Double(var11 - var17, var13 - var17, 2.0 * var17, 2.0 * var17));
      }

      for (int var44 = 0; var44 < 2; var44++) {
         double var45 = var44 == 0 ? 2.6 : 3.7;
         double var46 = 0.9;
         double var47 = var1 + var44 * 1.3;
         int var48 = (int)Math.floor(var47 / var45);
         double var18 = var47 - var48 * var45;
         if (!(var18 > var46) && !(rnd(var48, 10 + var44) > 0.35 + 0.6 * var7)) {
            double var20 = var18 / var46;
            double var22 = var3 * (0.15 + rnd(var48, 20 + var44) * 0.85);
            double var24 = -4.0 + rnd(var48, 30 + var44) * var5 * 0.35;
            double var26 = 28.0 + rnd(var48, 40 + var44) * 20.0;
            double var28 = -Math.cos(0.45);
            double var30 = Math.sin(0.45);
            double var32 = 70.0 + rnd(var48, 50 + var44) * 60.0;
            double var34 = var22 + var28 * var32 * var20;
            double var36 = var24 + var30 * var32 * var20;
            double var38 = var20 < 0.15 ? var20 / 0.15 : (var20 > 0.75 ? (1.0 - var20) / 0.25 : 1.0);
            double var40 = var34 - var28 * var26;
            double var42 = var36 - var30 * var26;
            var0.setStroke(new BasicStroke(0.9F, 1, 1));
            var0.setPaint(new GradientPaint((float)var34, (float)var36, c(255, 255, 255, 0.95 * var38), (float)var40, (float)var42, c(255, 255, 255, 0.0)));
            var0.draw(new java.awt.geom.Line2D.Double(var34, var36, var40, var42));
            glowDot(var0, var34, var36, 2.4, new Color(200, 220, 255), 0.9 * var38);
         }
      }
   }

   private static void snow(Graphics2D var0, double var1, double var3, double var5, double var7) {
      int var9 = (int)(34.0 * var7);

      for (int var10 = 0; var10 < var9; var10++) {
         double var11 = 5.0 + rnd(var10, 1) * 9.0;
         double var13 = 0.5 + rnd(var10, 2) * 1.1;
         double var15 = (rnd(var10, 3) * (var5 + 10.0) + var1 * var11) % (var5 + 10.0) - 5.0;
         double var17 = Math.sin(var1 * (0.6 + rnd(var10, 4)) + rnd(var10, 5) * 6.28) * (2.0 + rnd(var10, 6) * 3.0);
         double var19 = (rnd(var10, 7) * (var3 + 10.0) + var17 + var1 * 1.5) % (var3 + 10.0) - 5.0;
         var0.setColor(c(255, 255, 255, (0.35 + 0.5 * rnd(var10, 8)) * (0.6 + 0.4 * var7)));
         var0.fill(new Double(var19 - var13, var15 - var13, 2.0 * var13, 2.0 * var13));
      }
   }

   private static void lights(Graphics2D var0, double var1, double var3, double var5, double var7) {
      byte var9 = 11;
      double var10 = 4.2;
      var0.setStroke(new BasicStroke(0.55F));
      var0.setColor(c(20, 20, 20, 0.85));
      java.awt.geom.Path2D.Double var12 = new java.awt.geom.Path2D.Double();
      var12.moveTo(-2.0, 1.2);

      for (int var13 = 0; var13 < var9; var13++) {
         double var14 = -2.0 + (var3 + 4.0) * var13 / var9;
         double var16 = -2.0 + (var3 + 4.0) * (var13 + 1) / var9;
         var12.quadTo((var14 + var16) / 2.0, 1.2 + var10, var16, 1.2);
      }

      var0.draw(var12);

      for (int var23 = 0; var23 < var9; var23++) {
         double var24 = -2.0 + (var3 + 4.0) * (var23 + 0.5) / var9;
         double var25 = 1.2 + var10 * 0.5 + 1.6;
         Color var18 = BULBS[var23 % BULBS.length];
         double var19 = var1 * 1.6 - var23 * 0.35;
         double var21 = 0.35 + 0.65 * (0.5 + 0.5 * Math.sin(var19 * Math.PI));
         glowDot(var0, var24, var25, 5.5, var18, 0.35 * var21 * var7);
         var0.setColor(new Color(var18.getRed(), var18.getGreen(), var18.getBlue(), (int)(255.0 * (0.55 + 0.45 * var21))));
         var0.fill(new Double(var24 - 1.1, var25 - 1.5, 2.2, 3.0));
         var0.setColor(c(30, 30, 30, 0.9));
         var0.fill(new java.awt.geom.Rectangle2D.Double(var24 - 0.6, var25 - 2.4, 1.2, 1.0));
      }
   }

   private static void sparkle(Graphics2D var0, double var1, double var3, double var5, double var7, Color var9) {
      int var10 = (int)(14.0 * var7) + 2;

      for (int var11 = 0; var11 < var10; var11++) {
         double var12 = 1.8 + rnd(var11, 1) * 1.6;
         double var14 = var1 + rnd(var11, 2) * var12;
         int var16 = (int)Math.floor(var14 / var12);
         double var17 = (var14 - var16 * var12) / var12;
         double var19 = Math.sin(var17 * Math.PI);
         double var21 = rnd(var16 * 31 + var11, 3) * var3;
         double var23 = rnd(var16 * 31 + var11, 4) * var5;
         double var25 = (1.2 + rnd(var11, 5) * 1.8) * var19;
         Color var27 = var11 % 3 == 0 ? var9 : Color.WHITE;
         glowDot(var0, var21, var23, var25 * 1.6, var27, 0.25 * var19);
         var0.setColor(new Color(var27.getRed(), var27.getGreen(), var27.getBlue(), (int)(230.0 * var19)));
         java.awt.geom.Path2D.Double var28 = new java.awt.geom.Path2D.Double();
         var28.moveTo(var21, var23 - var25);
         var28.quadTo(var21, var23, var21 + var25, var23);
         var28.quadTo(var21, var23, var21, var23 + var25);
         var28.quadTo(var21, var23, var21 - var25, var23);
         var28.quadTo(var21, var23, var21, var23 - var25);
         var0.fill(var28);
      }
   }

   private static void aurora(Graphics2D var0, double var1, double var3, double var5, double var7) {
      Color[] var9 = new Color[]{new Color(60, 255, 170), new Color(90, 140, 255), new Color(200, 90, 255)};

      for (int var10 = 0; var10 < 3; var10++) {
         java.awt.geom.Path2D.Double var11 = new java.awt.geom.Path2D.Double();
         double var12 = var5 * (0.25 + var10 * 0.18);
         var11.moveTo(0.0, var5);

         for (int var14 = 0; var14 <= 24; var14++) {
            double var15 = var3 * var14 / 24.0;
            double var17 = var12 + Math.sin(var15 * 0.045 + var1 * (0.5 + var10 * 0.2) + var10) * 6.0 + Math.sin(var15 * 0.11 - var1 * 0.7 + var10 * 2) * 2.5;
            var11.lineTo(var15, var17);
         }

         var11.lineTo(var3, var5);
         var11.closePath();
         Color var19 = var9[var10];
         var0.setPaint(
            new GradientPaint(
               0.0F,
               (float)(var12 - 8.0),
               c(var19.getRed(), var19.getGreen(), var19.getBlue(), 0.28 * var7),
               0.0F,
               (float)(var12 + 22.0),
               c(var19.getRed(), var19.getGreen(), var19.getBlue(), 0.0)
            )
         );
         var0.fill(var11);
      }
   }

   private static void waves(Graphics2D var0, double var1, double var3, double var5, double var7, Color var9) {
      for (int var10 = 0; var10 < 3; var10++) {
         java.awt.geom.Path2D.Double var11 = new java.awt.geom.Path2D.Double();
         double var12 = var5 - 7.0 - var10 * 4;
         var11.moveTo(0.0, var5);

         for (int var14 = 0; var14 <= 40; var14++) {
            double var15 = var3 * var14 / 40.0;
            double var17 = var12 + Math.sin(var15 * 0.06 + var1 * (1.4 - var10 * 0.3) + var10 * 1.7) * (2.4 - var10 * 0.5);
            var11.lineTo(var15, var17);
         }

         var11.lineTo(var3, var5);
         var11.closePath();
         var0.setColor(new Color(var9.getRed(), var9.getGreen(), var9.getBlue(), (int)(255.0 * (0.1 + 0.05 * var10) * var7)));
         var0.fill(var11);
      }
   }

   private static void glowDot(Graphics2D var0, double var1, double var3, double var5, Color var7, double var8) {
      if (!(var8 <= 0.01) && !(var5 <= 0.05)) {
         var0.setPaint(
            new RadialGradientPaint(
               new java.awt.geom.Point2D.Double(var1, var3),
               (float)var5,
               new float[]{0.0F, 1.0F},
               new Color[]{
                  new Color(var7.getRed(), var7.getGreen(), var7.getBlue(), (int)(255.0 * Math.min(1.0, var8))),
                  new Color(var7.getRed(), var7.getGreen(), var7.getBlue(), 0)
               }
            )
         );
         var0.fill(new Double(var1 - var5, var3 - var5, 2.0 * var5, 2.0 * var5));
      }
   }

   public static void icon(Graphics2D var0, String var1, double var2, double var4, double var6, double var8, Color var10) {
      Shape var11 = var0.getClip();
      var0.clip(new java.awt.geom.RoundRectangle2D.Double(var2, var4, var6 * 1.6, var6, 6.0, 6.0));
      var0.setColor(new Color(12, 12, 16));
      var0.fill(new java.awt.geom.Rectangle2D.Double(var2, var4, var6 * 1.6, var6));
      AffineTransform var12 = var0.getTransform();
      var0.translate(var2, var4);
      var0.scale(var6 * 1.6 / 80.0, var6 / 50.0);
      if ("none".equals(var1)) {
         var0.setColor(new Color(255, 255, 255, 90));
         var0.setStroke(new BasicStroke(2.2F, 1, 1));
         var0.draw(new Double(28.0, 13.0, 24.0, 24.0));
         var0.draw(new java.awt.geom.Line2D.Double(31.5, 33.5, 48.5, 16.5));
      } else {
         draw(var0, var1, var8, 80.0, 50.0, 1.0, var10);
      }

      var0.setTransform(var12);
      var0.setClip(var11);
   }
}
