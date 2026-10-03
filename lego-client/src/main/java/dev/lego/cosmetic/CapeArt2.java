package dev.lego.cosmetic;

import dev.lego.ui.Fonts;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Line2D.Double;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

final class CapeArt2 {
   private CapeArt2() {
   }

   static void defineAll() {
      Capes.def("storm", 16, 10.0F, 13688063, 2765904, 922140, CapeArtHd::storm);
      Capes.def("rainbow", 16, 8.0F, 16777215, 10128072, 15128831, CapeArt2::rainbow);
      Capes.def("snow", 16, 8.0F, 16777215, 8166594, 1320522, CapeArt2::snow);
      Capes.def("fireflies", 16, 6.0F, 15265952, 3820058, 662034, CapeArtHd::fireflies);
      Capes.def("heart", 16, 12.0F, 16767176, 9054778, 3802132, CapeArt2::heart);
      Capes.def("enchant", 16, 10.0F, 15784191, 5909130, 1706554, CapeArt2::enchant);
      Capes.def("void", 16, 8.0F, 16762986, 3811856, 328456, CapeArt2::blackHole);
      Capes.def("royal", 1, 0.0F, 16771488, 10119698, 4851216, CapeArt2::royal);
      Capes.def("dragon", 1, 0.0F, 16769658, 8018450, 666138, CapeArtHd::scales);
      Capes.def("camo", 1, 0.0F, 11053176, 3815970, 2763290, CapeArtHd::camo);
      Capes.def("bricks", 1, 0.0F, 16769610, 10123776, 1710622, CapeArtHd::bricks);
      Capes.def("racing", 1, 0.0F, 16738922, 7997962, 1184274, CapeArtHd::racing);
      Capes.def("moon", 1, 0.0F, 15265023, 5925530, 659504, CapeArt2::moon);
   }

   private static double flash(int var0) {
      switch (var0) {
         case 3:
            return 1.0;
         case 4:
            return 0.45;
         case 5:
            return 0.8;
         case 6:
            return 0.25;
         case 7:
         case 8:
         case 9:
         case 10:
         default:
            return 0.0;
         case 11:
            return 0.7;
         case 12:
            return 0.3;
      }
   }

   static void storm(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      double var8 = flash(var4);
      boolean var10 = var4 >= 3 && var4 <= 6;
      double var11 = var10 ? 64.0 : 110.0;
      Capes.field(var1, (var6x, var8x, var10x, var11x) -> {
         double var12 = Capes.fbm(var6x * 3.0 + var6 * 3.0, var8x * 4.0, 3, 0, 3, 40);
         double var14x = Capes.fbm(var6x * 3.0 + var6 * 3.0 + (var12 - 0.5) * 0.8, var8x * 4.0 + (var12 - 0.5), 3, 0, 5, 41);
         double var16x = Capes.smooth(0.2, 0.75, var14x + (0.45 - var8x) * 1.1);
         int var18x = Capes.ramp(var8x, 1712692, 1054239, 461071);
         int var19x = Capes.ramp(var14x, 658708, 1975864, 4082278, 6977686);
         int var20x = Capes.mix(var18x, var19x, var16x);
         double var21 = var8 * (0.25 + 0.75 * var14x) * Math.exp(-Math.hypot(var10x - var11, var11x - 40) / 90.0);
         var20x = Capes.add(var20x, 12572927, var21 * 0.9 + var8 * 0.08);
         double var23x = 0.9 + 0.03 * Math.sin(var6x * 8.0 + 1.0) + 0.015 * Math.sin(var6x * 23.0);
         if (var8x > var23x) {
            var20x = Capes.add(Capes.mix(329483, 131844, (var8x - var23x) * 8.0), 9087231, var8 * 0.12);
         }

         return var20x;
      });
      Random var13 = new Random(71L);
      var0.setStroke(new BasicStroke(1.0F, 1, 1));

      for (int var14 = 0; var14 < 150; var14++) {
         double var15 = var13.nextDouble();
         double var17 = var13.nextDouble();
         int var19 = 2 + var13.nextInt(2);
         double var20 = (var17 + var19 * var6) % 1.0 * 276.0 - 10.0;
         double var22 = var15 * 190.0 - 15.0 + var20 * 0.12;
         double var24 = 7.0 + var13.nextDouble() * 7.0;
         var0.setColor(Capes.col(11059432, 0.18 + 0.2 * var13.nextDouble() + var8 * 0.2));
         var0.draw(new Double(var22, var20, var22 + var24 * 0.12, var20 + var24));
      }

      if (var8 > 0.0) {
         Random var30 = new Random(var10 ? 5L : 9L);
         java.awt.geom.Path2D.Double var31 = new java.awt.geom.Path2D.Double();
         double var16 = var11;
         double var18 = 30.0;
         var31.moveTo(var11, var18);
         ArrayList var32 = new ArrayList();

         while (var18 < 230.4) {
            var16 += var30.nextGaussian() * 7.0;
            var18 += 8.0 + var30.nextDouble() * 10.0;
            var31.lineTo(var16, var18);
            if (var30.nextDouble() < 0.18) {
               var32.add(new double[]{var16, var18});
            }
         }

         for (double[] var33 : var32) {
            double var23 = var33[0];
            double var25 = var33[1];
            var31.moveTo(var23, var25);
            double var27 = var30.nextBoolean() ? 1.0 : -1.0;

            for (int var29 = 0; var29 < 4; var29++) {
               var23 += var27 * (4.0 + var30.nextDouble() * 8.0);
               var25 += 6.0 + var30.nextDouble() * 8.0;
               var31.lineTo(var23, var25);
            }
         }

         Capes.addDot(var1, var11, 40.0, 50.0, 10471679, 0.5 * var8);
         Capes.glowStroke(var0, var31, 11064575, 2.6F, 3.2F, var8);
      }
   }

   static void rainbow(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var4x, var6x, var8x, var9) -> {
         double var10x = 0.07 * Math.sin(var6x * Math.PI * 3.0 + var2) + 0.05 * Math.sin(var4x * Math.PI * 2.0 + var6x * Math.PI - var2);
         double var12x = var4x * 0.22 + var6x * 0.62 + var10x + var6;
         double var14 = Math.sin((var6x * 2.6 + var4x * 1.1 + var10x * 2.2) * Math.PI * 2.0);
         double var16 = 0.86 + 0.14 * var14;
         int var18x = Capes.hsv(var12x, 0.6, 0.97 * var16);
         var18x = Capes.add(var18x, 16777215, Math.pow(Math.max(0.0, var14), 14.0) * 0.4);
         return Capes.mix(var18x, 16777215, 0.08);
      });
      double var8 = 80.0;
      double var10 = 112.0;
      Capes.addDot(var1, var8, var10, 48.0, 16777215, 0.45);
      java.awt.geom.Path2D.Double var12 = new java.awt.geom.Path2D.Double();
      double var13 = 34.0;
      double var15 = 7.0;

      for (int var17 = 0; var17 < 8; var17++) {
         double var18 = (-Math.PI / 2) + var17 * Math.PI / 4.0;
         double var20 = var17 % 2 == 0 ? var13 : var15;
         if (var17 == 0) {
            var12.moveTo(var8 + Math.cos(var18) * var20, var10 + Math.sin(var18) * var20);
         } else {
            var12.quadTo(
               var8 + Math.cos(var18 - (Math.PI / 8)) * var15 * 0.6,
               var10 + Math.sin(var18 - (Math.PI / 8)) * var15 * 0.6,
               var8 + Math.cos(var18) * var20,
               var10 + Math.sin(var18) * var20
            );
         }
      }

      var12.quadTo(var8 + Math.cos(-Math.PI * 5.0 / 8.0) * var15 * 0.6, var10 + Math.sin(-Math.PI * 5.0 / 8.0) * var15 * 0.6, var8, var10 - var13);
      var0.setColor(Capes.col(6965928, 0.25));
      var0.fill(AffineTransform.getTranslateInstance(2.0, 3.0).createTransformedShape(var12));
      var0.setPaint(
         new RadialGradientPaint(
            (float)var8, (float)var10, (float)var13, new float[]{0.0F, 0.5F, 1.0F}, new Color[]{Color.WHITE, new Color(16774911), new Color(15259903)}
         )
      );
      var0.fill(var12);
      Random var25 = new Random(3L);

      for (int var26 = 0; var26 < 34; var26++) {
         double var19 = 12.0 + var25.nextDouble() * 136.0;
         double var21 = 12.0 + var25.nextDouble() * 232.0;
         double var23 = Math.pow(Math.max(0.0, Math.sin(var2 * (1 + var25.nextInt(2)) + var25.nextDouble() * 6.28)), 2.0);
         Capes.star(var0, var19, var21, 1.0 + var25.nextDouble() * 1.8, 16777215, var23);
      }
   }

   private static void pine(Graphics2D var0, double var1, double var3, double var5, int var7, double var8) {
      double var10 = var5 * 0.42;
      var0.setColor(new Color(Capes.mix(var7, 0, 0.3)));
      var0.fill(new java.awt.geom.Rectangle2D.Double(var1 - var5 * 0.03, var3 - var5 * 0.12, var5 * 0.06, var5 * 0.14));

      for (int var12 = 0; var12 < 4; var12++) {
         double var13 = var3 - var5 * 0.1 - var12 * var5 * 0.22;
         double var15 = var10 * (1.0 - var12 * 0.22);
         java.awt.geom.Path2D.Double var17 = new java.awt.geom.Path2D.Double();
         var17.moveTo(var1, var13 - var5 * 0.34);
         var17.lineTo(var1 + var15 / 2.0, var13);
         var17.quadTo(var1, var13 - var5 * 0.05, var1 - var15 / 2.0, var13);
         var17.closePath();
         var0.setColor(new Color(var7));
         var0.fill(var17);
         java.awt.geom.Path2D.Double var18 = new java.awt.geom.Path2D.Double();
         var18.moveTo(var1, var13 - var5 * 0.34);
         var18.lineTo(var1 + var15 * 0.28, var13 - var5 * 0.12);
         var18.quadTo(var1, var13 - var5 * 0.2, var1 - var15 * 0.28, var13 - var5 * 0.12);
         var18.closePath();
         var0.setColor(Capes.col(15398143, var8));
         var0.fill(var18);
      }
   }

   static void snow(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var0x, var2x, var4x, var5x) -> {
         int var6x = Capes.ramp(var2x / 0.75, 397354, 926542, 2508668, 6192818);
         var6x = Capes.add(var6x, 13623551, Math.exp(-Math.hypot(var4x - 116, var5x - 50) / 22.0) * 0.45);
         double var7 = 0.62 + 0.04 * Math.sin(var0x * 5.0 + 1.0) + 0.02 * Math.sin(var0x * 13.0);
         if (var2x > var7) {
            var6x = Capes.mix(10270428, 6191784, (var2x - var7) * 5.0);
         }

         double var9x = 0.82 + 0.025 * Math.sin(var0x * 4.0 + 2.0);
         if (var2x > var9x) {
            var6x = Capes.mix(15791871, 11058912, (var2x - var9x) * 4.0);
            if (Capes.hash(var4x, var5x, 7) > 0.985) {
               var6x = Capes.add(var6x, 16777215, 0.5);
            }
         }

         return var6x;
      });
      var0.setPaint(new RadialGradientPaint(112.0F, 46.0F, 16.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(16776688), new Color(15262928)}));
      var0.fill(new java.awt.geom.Ellipse2D.Double(104.0, 38.0, 24.0, 24.0));
      var0.setColor(Capes.col(12104864, 0.35));
      var0.fill(new java.awt.geom.Ellipse2D.Double(110.0, 44.0, 6.0, 5.0));
      var0.fill(new java.awt.geom.Ellipse2D.Double(118.0, 52.0, 4.0, 4.0));
      Random var8 = new Random(6L);

      for (int var9 = 0; var9 < 40; var9++) {
         Capes.star(
            var0,
            var8.nextDouble() * 160.0,
            var8.nextDouble() * 256.0 * 0.5,
            0.5 + var8.nextDouble(),
            14543103,
            0.3 + 0.5 * (0.5 + 0.5 * Math.sin(var2 + var9))
         );
      }

      for (int var26 = 0; var26 < 9; var26++) {
         double var10 = 8 + var26 * 18 + var8.nextDouble() * 6.0;
         double var12 = 256.0 * (0.64 + 0.04 * Math.sin(var10 / 160.0 * 5.0 + 1.0)) + 2.0;
         pine(var0, var10, var12, 18.0 + var8.nextDouble() * 10.0, 1716818, 0.55);
      }

      double[] var27 = new double[]{18.0, 52.0, 132.0, 150.0};

      for (double var13 : var27) {
         pine(var0, var13, 220.16, 52.0 + var8.nextDouble() * 20.0, 793648, 0.9);
      }

      int[][] var29 = new int[][]{{50, 1}, {34, 1}, {14, 2}};
      double[][] var30 = new double[][]{{0.8, 1.4}, {1.6, 2.4}, {3.0, 4.2}};

      for (int var32 = 0; var32 < 3; var32++) {
         for (int var33 = 0; var33 < var29[var32][0]; var33++) {
            double var14 = var8.nextDouble() * 160.0;
            double var16 = var8.nextDouble();
            double var18 = var8.nextDouble() * 6.28;
            double var20 = (var16 + var29[var32][1] * var6) % 1.0 * 268.0 - 6.0;
            double var22 = var14 + Math.sin(var2 + var18) * (4 + var32 * 2);
            double var24 = var30[var32][0] + var8.nextDouble() * (var30[var32][1] - var30[var32][0]);
            Capes.dot(var0, var22, var20, var24 * 1.6, 16777215, var32 == 2 ? 0.7 : 0.9);
         }
      }
   }

   static void fireflies(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var2x, var4x, var6x, var7) -> {
         int var8x = Capes.ramp(var4x, 133642, 401436, 932398, 666142);
         var8x = Capes.add(var8x, 10152144, Math.exp(-Math.hypot(var2x - 0.5, (var4x - 0.12) * 0.8) / 0.18) * 0.28);
         double var9x = Capes.fbm(var2x * 3.0 + var6 * 3.0, var4x * 5.0, 3, 0, 4, 55) * Math.exp(-Math.pow((var4x - 0.72) / 0.12, 2.0));
         return Capes.add(var8x, 6989978, var9x * 0.35);
      });
      Random var8 = new Random(15L);
      int[] var9 = new int[]{997938, 534048, 201232};

      for (int var10 = 0; var10 < 3; var10++) {
         int var11 = 5 - var10;

         for (int var12 = 0; var12 < var11; var12++) {
            double var13 = var8.nextDouble() * 160.0;
            double var15 = 5 + var10 * 5 + var8.nextDouble() * 4.0;
            var0.setPaint(
               new GradientPaint(
                  (float)(var13 - var15),
                  0.0F,
                  new Color(Capes.mix(var9[var10], 0, 0.2)),
                  (float)(var13 + var15),
                  0.0F,
                  new Color(Capes.mix(var9[var10], 3828314, 0.25))
               )
            );
            java.awt.geom.Path2D.Double var17 = new java.awt.geom.Path2D.Double();
            var17.moveTo(var13 - var15 / 2.0, 0.0);
            var17.lineTo(var13 + var15 / 2.0, 0.0);
            var17.lineTo(var13 + var15 * 0.7, 256.0);
            var17.lineTo(var13 - var15 * 0.7, 256.0);
            var17.closePath();
            var0.fill(var17);
         }

         var0.setColor(new Color(var9[var10]));

         for (int var32 = 0; var32 < 16; var32++) {
            double var33 = var8.nextDouble() * 160.0;
            double var36 = -10.0 + var8.nextDouble() * (30 + var10 * 10);
            double var39 = 14.0 + var8.nextDouble() * 14.0;
            var0.fill(new java.awt.geom.Ellipse2D.Double(var33 - var39, var36 - var39 * 0.7, var39 * 2.0, var39 * 1.4));
         }
      }

      var0.setStroke(new BasicStroke(1.4F, 1, 1));

      for (int var28 = 0; var28 < 140; var28++) {
         double var30 = var8.nextDouble() * 160.0;
         double var34 = 10.0 + var8.nextDouble() * 22.0;
         double var37 = var8.nextGaussian() * 5.0 + Math.sin(var2 + var30 * 0.05) * 1.5;
         var0.setColor(new Color(Capes.mix(200712, 1985066, var8.nextDouble() * 0.6)));
         var0.draw(new java.awt.geom.QuadCurve2D.Double(var30, 256.0, var30 + var37 * 0.3, 256.0 - var34 * 0.6, var30 + var37, 256.0 - var34));
      }

      for (int var29 = 0; var29 < 26; var29++) {
         double var31 = 12.0 + var8.nextDouble() * 136.0;
         double var35 = 50.0 + var8.nextDouble() * 190.0;
         int var38 = 1 + var8.nextInt(2);
         double var16 = var8.nextDouble() * 6.28;
         double var18 = var8.nextDouble() * 6.28;
         double var20 = var8.nextDouble() * 6.28;
         double var22 = var31 + 8.0 * Math.cos(var2 * var38 + var16) + 3.0 * Math.sin(2.0 * var2 + var18);
         double var24 = var35 + 6.0 * Math.sin(var2 * var38 + var20);
         double var26 = Math.pow(0.5 + 0.5 * Math.sin(var2 * (1 + var8.nextInt(2)) + var18), 2.2);
         Capes.addDot(var1, var22, var24, 10.0, 12123978, 0.45 * var26 + 0.04);
         Capes.addDot(var1, var22, var24, 2.2, 16777168, 0.95 * var26 + 0.1);
      }
   }

   private static double beat(double var0) {
      double var2 = 0.0;

      for (int var4 = -1; var4 <= 1; var4++) {
         double var5 = var0 + var4;
         var2 += Math.exp(-Math.pow((var5 - 0.08) / 0.05, 2.0)) + 0.6 * Math.exp(-Math.pow((var5 - 0.3) / 0.06, 2.0));
      }

      return var2;
   }

   static Path2D heartPath(double var0, double var2, double var4) {
      java.awt.geom.Path2D.Double var6 = new java.awt.geom.Path2D.Double();
      var6.moveTo(var0, var2 + var4 * 0.42);
      var6.curveTo(var0 - var4 * 0.08, var2 + var4 * 0.33, var0 - var4 * 0.5, var2 + var4 * 0.05, var0 - var4 * 0.5, var2 - var4 * 0.18);
      var6.curveTo(var0 - var4 * 0.5, var2 - var4 * 0.4, var0 - var4 * 0.28, var2 - var4 * 0.5, var0 - var4 * 0.16, var2 - var4 * 0.5);
      var6.curveTo(var0 - var4 * 0.06, var2 - var4 * 0.5, var0, var2 - var4 * 0.42, var0, var2 - var4 * 0.32);
      var6.curveTo(var0, var2 - var4 * 0.42, var0 + var4 * 0.06, var2 - var4 * 0.5, var0 + var4 * 0.16, var2 - var4 * 0.5);
      var6.curveTo(var0 + var4 * 0.28, var2 - var4 * 0.5, var0 + var4 * 0.5, var2 - var4 * 0.4, var0 + var4 * 0.5, var2 - var4 * 0.18);
      var6.curveTo(var0 + var4 * 0.5, var2 + var4 * 0.05, var0 + var4 * 0.08, var2 + var4 * 0.33, var0, var2 + var4 * 0.42);
      var6.closePath();
      return var6;
   }

   static void heart(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      double var8 = beat(var6);
      Capes.field(var1, (var2x, var4x, var6x, var7) -> {
         double var8x = Math.hypot(var6x - 80, (var7 - 104) * 0.8);
         int var10x = Capes.mix(10489914, 2753038, var8x / 150.0);
         return Capes.add(var10x, 16730746, Math.exp(-(var8x / 55.0) * (var8x / 55.0)) * 0.3 * var8);
      });
      Random var10 = new Random(8L);

      for (int var11 = 0; var11 < 18; var11++) {
         double var12 = var10.nextDouble() * 160.0;
         double var14 = var10.nextDouble();
         double var16 = ((var14 - var6) % 1.0 + 1.0) % 1.0 * 276.0 - 10.0;
         double var18 = 7.0 + var10.nextDouble() * 8.0;
         var0.setColor(Capes.col(16743066, 0.12 + 0.18 * var10.nextDouble()));
         var0.fill(heartPath(var12 + Math.sin(var2 + var11) * 4.0, var16, var18));
      }

      double var37 = 84.0 * (1.0 + 0.1 * var8);
      double var13 = 80.0;
      double var15 = 106.0;
      Capes.addDot(var1, var13, var15, 58.0 * (1.0 + 0.1 * var8), 16722522, 0.25 + 0.35 * var8);
      Path2D var17 = heartPath(var13, var15, var37);
      var0.setColor(Capes.col(2752520, 0.45));
      var0.fill(heartPath(var13 + 2.0, var15 + 4.0, var37));
      var0.setPaint(
         new RadialGradientPaint(
            (float)(var13 - var37 * 0.18),
            (float)(var15 - var37 * 0.2),
            (float)(var37 * 0.75),
            new float[]{0.0F, 0.45F, 1.0F},
            new Color[]{new Color(16751280), new Color(15736910), new Color(9046054)}
         )
      );
      var0.fill(var17);
      var0.setColor(Capes.col(5899288, 0.6));
      var0.setStroke(new BasicStroke(1.4F));
      var0.draw(var17);
      AffineTransform var38 = var0.getTransform();
      var0.translate(var13 - var37 * 0.24, var15 - var37 * 0.26);
      var0.rotate(-0.6);
      var0.setPaint(new GradientPaint(0.0F, (float)(-var37 * 0.08), Capes.col(16777215, 0.75), 0.0F, (float)(var37 * 0.08), Capes.col(16777215, 0.0)));
      var0.fill(new java.awt.geom.Ellipse2D.Double(-var37 * 0.13, -var37 * 0.07, var37 * 0.26, var37 * 0.14));
      var0.setTransform(var38);
      double var19 = 204.0;
      double var21 = var6 * 200.0 - 20.0;
      double var23 = 0.0;
      double var25 = var19;

      for (byte var27 = 0; var27 <= 160; var27 += 2) {
         double var28 = (var27 % 80 + 80) % 80 / 80.0;
         double var30 = var19;
         if (var28 > 0.3 && var28 < 0.36) {
            var30 = var19 - Math.sin((var28 - 0.3) / 0.06 * Math.PI) * 4.0;
         } else if (var28 > 0.42 && var28 < 0.46) {
            var30 = var19 + (var28 - 0.42) / 0.04 * 5.0;
         } else if (var28 >= 0.46 && var28 < 0.5) {
            var30 = var19 + 5.0 - (var28 - 0.46) / 0.04 * 32.0;
         } else if (var28 >= 0.5 && var28 < 0.55) {
            var30 = var19 - 27.0 + (var28 - 0.5) / 0.05 * 35.0;
         } else if (var28 >= 0.55 && var28 < 0.58) {
            var30 = var19 + 8.0 - (var28 - 0.55) / 0.03 * 8.0;
         } else if (var28 > 0.66 && var28 < 0.78) {
            var30 = var19 - Math.sin((var28 - 0.66) / 0.12 * Math.PI) * 6.0;
         }

         double var32 = var21 - var27;
         if (var32 < 0.0) {
            var32 += 200.0;
         }

         double var34 = Math.exp(-var32 / 70.0);
         if (var27 > 0) {
            Double var36 = new Double(var23, var25, var27, var30);
            var0.setColor(Capes.col(16738954, var34 * 0.35));
            var0.setStroke(new BasicStroke(4.0F, 1, 1));
            var0.draw(var36);
            var0.setColor(Capes.col(16769256, var34));
            var0.setStroke(new BasicStroke(1.5F, 1, 1));
            var0.draw(var36);
         }

         if (Math.abs(var27 - var21) < 1.5) {
            Capes.addDot(var1, var27, var30, 7.0, 16777215, 0.7);
         }

         var23 = var27;
         var25 = var30;
      }
   }

   static void enchant(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var0x, var2x, var4x, var5x) -> {
         int var6x = Capes.ramp(var2x, 3019362, 1968710, 918826);
         var6x = Capes.shade(var6x, 0.88 + 0.24 * Capes.fbm(var0x * 10.0, var2x * 16.0, 0, 0, 4, 12));
         return Capes.add(var6x, 9063167, Math.exp(-Math.hypot(var0x - 0.5, (var2x - 0.42) * 0.7) / 0.2) * 0.3);
      });
      double var8 = 80.0;
      double var10 = 108.0;
      double var12 = 54.0;
      int var14 = 13142783;
      Capes.glowStroke(var0, new java.awt.geom.Ellipse2D.Double(var8 - var12, var10 - var12, var12 * 2.0, var12 * 2.0), var14, 1.6F, 1.6F, 0.9);
      Capes.glowStroke(
         var0,
         new java.awt.geom.Ellipse2D.Double(var8 - var12 + 11.0, var10 - var12 + 11.0, (var12 - 11.0) * 2.0, (var12 - 11.0) * 2.0),
         var14,
         1.2F,
         1.2F,
         0.8
      );
      Random var15 = new Random(77L);
      byte var16 = 16;

      for (int var17 = 0; var17 < var16; var17++) {
         double var18 = (Math.PI * 2) * var17 / var16;
         double var20 = 0.45 + 0.55 * Math.pow(0.5 + 0.5 * Math.sin(var2 - var18 * 2.0), 2.0);
         AffineTransform var22 = var0.getTransform();
         var0.translate(var8 + Math.cos(var18) * (var12 - 5.5), var10 + Math.sin(var18) * (var12 - 5.5));
         var0.rotate(var18 + (Math.PI / 2));
         java.awt.geom.Path2D.Double var23 = new java.awt.geom.Path2D.Double();

         for (int var24 = 0; var24 < 3; var24++) {
            double var25 = var15.nextInt(3) * 2.2 - 2.2;
            double var27 = var15.nextInt(3) * 2.4 - 2.4;
            double var29 = var15.nextInt(3) * 2.2 - 2.2;
            double var31 = var15.nextInt(3) * 2.4 - 2.4;
            if (var25 == var29 && var27 == var31) {
               var31 += 2.4;
            }

            var23.moveTo(var25, var27);
            var23.lineTo(var29, var31);
         }

         Capes.glowStroke(var0, var23, 15255807, 0.9F, 0.8F, var20);
         var0.setTransform(var22);
      }

      double var33 = var6 * Math.PI / 3.0;

      for (int var19 = 0; var19 < 2; var19++) {
         java.awt.geom.Path2D.Double var35 = new java.awt.geom.Path2D.Double();

         for (int var21 = 0; var21 < 3; var21++) {
            double var39 = var33 + var19 * Math.PI / 3.0 + var21 * Math.PI * 2.0 / 3.0 - (Math.PI / 2);
            double var44 = var8 + Math.cos(var39) * (var12 - 12.0);
            double var26 = var10 + Math.sin(var39) * (var12 - 12.0);
            if (var21 == 0) {
               var35.moveTo(var44, var26);
            } else {
               var35.lineTo(var44, var26);
            }
         }

         var35.closePath();
         Capes.glowStroke(var0, var35, 11037439, 1.1F, 1.3F, 0.75);
      }

      double var34 = var12 * 0.32;

      for (int var36 = 0; var36 < 12; var36++) {
         double var40 = -var6 * Math.PI / 6.0 + var36 * Math.PI / 6.0;
         Capes.dot(var0, var8 + Math.cos(var40) * var34, var10 + Math.sin(var40) * var34, 2.6, 15784191, 0.9);
      }

      Capes.addDot(var1, var8, var10, 16.0, 13142783, 0.7 + 0.3 * Math.sin(var2));
      Capes.addDot(var1, var8, var10, 5.0, 16777215, 0.9);

      for (int var37 = 0; var37 < 20; var37++) {
         double var41 = var15.nextDouble() * 160.0;
         double var45 = var15.nextDouble();
         double var47 = ((var45 - var6) % 1.0 + 1.0) % 1.0 * 256.0;
         Capes.addDot(var1, var41 + Math.sin(var2 + var37) * 3.0, var47, 3.0, 14199039, 0.6 * Math.sin(Math.PI * var47 / 256.0));
      }

      int[] var38 = Capes.px(var1);

      for (int var42 = 0; var42 < 256; var42++) {
         for (int var43 = 0; var43 < 160; var43++) {
            double var46 = (var43 + 0.5) / 160.0;
            double var48 = (var42 + 0.5) / 256.0;
            double var28 = Math.pow(0.5 + 0.5 * Math.sin((var46 * 1.1 + var48 * 0.55) * Math.PI * 2.0 * 2.5 - var2), 7.0) * 0.32;
            var38[var42 * 160 + var43] = Capes.add(var38[var42 * 160 + var43], 10509055, var28);
         }
      }
   }

   static void blackHole(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var2x, var4x, var6x, var7) -> {
         double var8 = var6x + 0.5 - 80.0;
         double var10 = var7 + 0.5 - 112.0;
         double var12 = Math.hypot(var8, var10);
         double var14 = var12 > 1.0 ? 617.4 / (var12 * var12) : 0.0;
         int var16 = (int)Math.round(var6x + var8 * var14);
         int var17 = (int)Math.round(var7 + var10 * var14);
         int var18 = Capes.mix(262666, 656408, var4x);
         var18 = Capes.add(var18, 5909146, Math.pow(Capes.fbm(var16 / 50.0, var17 / 50.0, 0, 0, 4, 3), 3.0) * 0.6);
         double var19 = Capes.hash(var16 >> 1, var17 >> 1, 17);
         if (var19 > 0.985) {
            var18 = Capes.add(var18, 16777215, (var19 - 0.985) * 60.0);
         }

         var18 = Capes.add(var18, 16747066, Math.exp(-var12 / 55.0) * 0.2);
         double var21 = var10 / 0.26;
         double var23 = Math.hypot(var8, var21);
         double var25 = Math.atan2(var21, var8);
         double var27 = Capes.smooth(24.0, 31.0, var23) * (1.0 - Capes.smooth(39.0, 74.0, var23));
         double var29 = Capes.fbm(var23 * 0.09, var25 / (Math.PI * 2) * 16.0 + var6 * 32.0, 0, 16, 4, 29);
         double var31 = 0.8 + 0.4 * -Math.cos(var25);
         double var33 = var27 * (0.45 + 0.8 * var29) * var31 * (1.25 - (var23 - 21.0) / 70.0);
         int var35 = Capes.ramp(var33, 0, 6950912, 15222796, 16754760, 16774360);
         boolean var36 = var10 > 0.0;
         if (!var36 && var12 > 21.0) {
            var18 = Capes.add(var18, var35, Capes.clamp01(var33 * 1.4));
         }

         double var37 = Math.exp(-Math.pow((var12 - 26.0) / 3.5, 2.0)) * (var10 < 4.0 ? 1.0 : 0.35);
         double var39 = Capes.fbm(Math.atan2(var10, var8) / (Math.PI * 2) * 16.0 - var6 * 32.0, var12 * 0.2, 16, 0, 3, 31);
         var18 = Capes.add(var18, Capes.ramp(0.6 + 0.4 * var39, 6950912, 16754760, 16774360), var37 * (0.5 + 0.5 * var39));
         if (var12 < 21.0) {
            var18 = Capes.mix(var18, 0, Capes.smooth(21.0, 19.5, var12));
         }

         if (var36) {
            var18 = Capes.add(var18, var35, Capes.clamp01(var33 * 1.4));
         }

         return Capes.add(var18, 16771264, Math.exp(-Math.pow((var12 - 21.0 - 0.8) / 1.1, 2.0)) * 0.9);
      });
   }

   static void royal(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      Capes.field(var1, (var0x, var2x, var4x, var5x) -> {
         double var6x = Capes.fbm(var0x * 3.0, var2x * 2.5, 0, 0, 4, 90);
         int var8x = Capes.mix(9047074, 3801612, var2x * 0.7 + 0.3 * (1.0 - var6x));
         double var9x = Math.pow(0.5 + 0.5 * Math.sin(var0x * Math.PI * 5.0 + var6x * 3.0), 3.0) * 0.12;
         return Capes.add(var8x, 16738938, var9x);
      });
      var0.setColor(Capes.col(16765562, 0.18));

      for (int var6 = 0; var6 < 7; var6++) {
         for (int var7 = -1; var7 < 5; var7++) {
            double var8 = var7 * 40 + var6 % 2 * 20 + 20;
            double var10 = 60 + var6 * 34;
            fleur(var0, var8, var10, 9.0);
         }
      }

      for (int var19 = 12; var19 < 44; var19++) {
         for (int var22 = 0; var22 < 160; var22++) {
            double var24 = 40.0 + 3.0 * Math.sin(var22 * 0.4) + 2.0 * Capes.hash(var22, 3, 5);
            if (!(var19 > var24)) {
               double var26 = 0.9 + 0.1 * Capes.hash(var22, var19, 8) + 0.05 * Math.sin(var22 * 0.9 + var19 * 0.3);
               Capes.px(var1)[var19 * 160 + var22] = 0xFF000000 | Capes.shade(Capes.mix(16777215, 14210252, (var19 - 12) / 32.0), var26);
            }
         }
      }

      for (int var20 = 0; var20 < 7; var20++) {
         double var23 = 14 + var20 * 22;
         double var9 = 24 + var20 % 2 * 8;
         java.awt.geom.Path2D.Double var11 = new java.awt.geom.Path2D.Double();
         var11.moveTo(var23, var9 - 4.0);
         var11.quadTo(var23 + 3.0, var9 + 2.0, var23, var9 + 6.0);
         var11.quadTo(var23 - 3.0, var9 + 2.0, var23, var9 - 4.0);
         var0.setColor(new Color(1118481));
         var0.fill(var11);
      }

      double var21 = 80.0;
      double var25 = 136.0;
      java.awt.geom.Path2D.Double var27 = new java.awt.geom.Path2D.Double();
      var27.moveTo(var21 - 28.0, var25 - 30.0);
      var27.lineTo(var21 + 28.0, var25 - 30.0);
      var27.lineTo(var21 + 28.0, var25 + 2.0);
      var27.curveTo(var21 + 28.0, var25 + 22.0, var21 + 10.0, var25 + 32.0, var21, var25 + 40.0);
      var27.curveTo(var21 - 10.0, var25 + 32.0, var21 - 28.0, var25 + 22.0, var21 - 28.0, var25 + 2.0);
      var27.closePath();
      var0.setColor(Capes.col(1703940, 0.5));
      var0.fill(AffineTransform.getTranslateInstance(2.0, 4.0).createTransformedShape(var27));
      var0.setPaint(gold(var21, var25 - 30.0, var25 + 40.0));
      var0.fill(var27);
      Shape var28 = AffineTransform.getTranslateInstance(var21, var25 + 4.0)
         .createTransformedShape(
            AffineTransform.getScaleInstance(0.82, 0.82)
               .createTransformedShape(AffineTransform.getTranslateInstance(-var21, -var25 - 4.0).createTransformedShape(var27))
         );
      var0.setPaint(new GradientPaint(0.0F, (float)var25 - 26.0F, new Color(2767514), 0.0F, (float)var25 + 36.0F, new Color(922704)));
      var0.fill(var28);
      var0.setFont(Fonts.get(3, 34.0F));
      float var12 = var0.getFontMetrics().stringWidth("L");
      var0.setPaint(gold(var21, var25 - 20.0, var25 + 14.0));
      var0.drawString("L", (float)(var21 - var12 / 2.0F), (float)var25 + 14.0F);
      java.awt.geom.Path2D.Double var13 = new java.awt.geom.Path2D.Double();
      double var14 = var25 - 36.0;
      var13.moveTo(var21 - 24.0, var14);
      var13.lineTo(var21 - 28.0, var14 - 22.0);
      var13.lineTo(var21 - 13.0, var14 - 10.0);
      var13.lineTo(var21, var14 - 28.0);
      var13.lineTo(var21 + 13.0, var14 - 10.0);
      var13.lineTo(var21 + 28.0, var14 - 22.0);
      var13.lineTo(var21 + 24.0, var14);
      var13.closePath();
      var0.setPaint(gold(var21, var14 - 28.0, var14));
      var0.fill(var13);
      var0.setColor(Capes.col(5913088, 0.6));
      var0.setStroke(new BasicStroke(1.0F));
      var0.draw(var13);
      int[] var16 = new int[]{14684511, 2003199, 14684511};
      double[][] var17 = new double[][]{{var21 - 28.0, var14 - 22.0}, {var21, var14 - 28.0}, {var21 + 28.0, var14 - 22.0}};

      for (int var18 = 0; var18 < 3; var18++) {
         var0.setPaint(
            new RadialGradientPaint(
               (float)var17[var18][0] - 1.0F, (float)var17[var18][1] - 1.0F, 4.0F, new float[]{0.0F, 1.0F}, new Color[]{Color.WHITE, new Color(var16[var18])}
            )
         );
         var0.fill(new java.awt.geom.Ellipse2D.Double(var17[var18][0] - 3.5, var17[var18][1] - 3.5, 7.0, 7.0));
      }

      var0.setColor(new Color(2003199));
      var0.fill(new java.awt.geom.Ellipse2D.Double(var21 - 3.0, var14 - 7.0, 6.0, 5.0));
   }

   static LinearGradientPaint gold(double var0, double var2, double var4) {
      return new LinearGradientPaint(
         (float)var0 - 10.0F,
         (float)var2,
         (float)var0 + 10.0F,
         (float)var4,
         new float[]{0.0F, 0.4F, 0.55F, 1.0F},
         new Color[]{new Color(16774328), new Color(15251756), new Color(11565586), new Color(16767082)}
      );
   }

   private static void fleur(Graphics2D var0, double var1, double var3, double var5) {
      java.awt.geom.Path2D.Double var7 = new java.awt.geom.Path2D.Double();
      var7.moveTo(var1, var3 - var5 * 1.6);
      var7.curveTo(var1 + var5 * 0.6, var3 - var5 * 0.8, var1 + var5 * 0.4, var3 - var5 * 0.2, var1, var3 + var5 * 0.2);
      var7.curveTo(var1 - var5 * 0.4, var3 - var5 * 0.2, var1 - var5 * 0.6, var3 - var5 * 0.8, var1, var3 - var5 * 1.6);
      var7.moveTo(var1 + var5 * 0.2, var3);
      var7.curveTo(var1 + var5 * 1.6, var3 - var5 * 1.2, var1 + var5 * 1.8, var3 + var5 * 0.2, var1 + var5 * 0.9, var3 + var5 * 0.3);
      var7.curveTo(var1 + var5 * 1.1, var3 - var5 * 0.3, var1 + var5 * 0.7, var3 - var5 * 0.4, var1 + var5 * 0.2, var3);
      var7.moveTo(var1 - var5 * 0.2, var3);
      var7.curveTo(var1 - var5 * 1.6, var3 - var5 * 1.2, var1 - var5 * 1.8, var3 + var5 * 0.2, var1 - var5 * 0.9, var3 + var5 * 0.3);
      var7.curveTo(var1 - var5 * 1.1, var3 - var5 * 0.3, var1 - var5 * 0.7, var3 - var5 * 0.4, var1 - var5 * 0.2, var3);
      var0.fill(var7);
      var0.fill(new java.awt.geom.RoundRectangle2D.Double(var1 - var5 * 0.8, var3 + var5 * 0.15, var5 * 1.6, var5 * 0.35, 2.0, 2.0));
      var7 = new java.awt.geom.Path2D.Double();
      var7.moveTo(var1 - var5 * 0.25, var3 + var5 * 0.5);
      var7.lineTo(var1 + var5 * 0.25, var3 + var5 * 0.5);
      var7.lineTo(var1, var3 + var5 * 1.3);
      var7.closePath();
      var0.fill(var7);
   }

   static void scales(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      Capes.field(
         var1,
         (var0x, var2x, var4x, var5x) -> {
            int var6 = (int)Math.floor((var5x - 14.0) / 12.0) - 1;

            for (int var7 = var6; var7 <= var6 + 3; var7++) {
               double var8 = var7 * 12.0;
               double var10 = Math.floorMod(var7, 2) * 22.0 / 2.0;
               int var12 = (int)Math.floor((var4x - var10) / 22.0 + 0.5);

               for (int var13 = var12 - 1; var13 <= var12 + 1; var13++) {
                  double var14 = var13 * 22.0 + var10;
                  double var16 = var4x + 0.5 - var14;
                  double var18 = var5x + 0.5 - var8;
                  if (!(var18 < -2.0)) {
                     double var20 = Math.hypot(var16, var18) / 14.0;
                     if (!(var20 > 1.0)) {
                        double var22 = Capes.hash(var13, var7, 4);
                        int var24 = Capes.ramp(
                           Capes.clamp01(0.5 + var16 / 30.800000000000004 + (var22 - 0.5) * 0.3 + var2x * 0.3), 944712, 1482874, 1751200, 674376
                        );
                        double var25 = 1.0 - var20 * var20 * 0.3 + var18 / 14.0 * 0.12;
                        int var27 = Capes.shade(var24, var25 * (1.05 - var2x * 0.35));
                        var27 = Capes.add(
                           var27, 15269856, Math.exp(-Math.pow((var16 + 2.8000000000000003) / 5.0, 2.0) - Math.pow((var18 - 6.3) / 3.0, 2.0)) * 0.2
                        );
                        if (var20 > 0.84) {
                           var27 = Capes.mix(var27, 10152112, (var20 - 0.84) / 0.16 * 0.45);
                        }

                        if (var20 > 0.93) {
                           var27 = Capes.shade(var27, 0.55);
                        }

                        return var27;
                     }
                  }
               }
            }

            return 335896;
         }
      );
   }

   static void camo(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      Capes.field(var1, (var0x, var2x, var4x, var5x) -> {
         double var6x = Capes.fbm(var0x * 3.0, var2x * 5.0, 0, 0, 3, 70);
         double var8x = Capes.fbm(var0x * 3.2 + var6x, var2x * 5.0 + var6x, 0, 0, 4, 71);
         double var10x = Capes.fbm(var0x * 4.0 + 5.0 + var6x * 0.6, var2x * 6.0, 0, 0, 4, 72);
         int var12x = 5925430;
         if (var8x < 0.44) {
            var12x = 3029791;
         }

         if (var10x > 0.58) {
            var12x = 9075278;
         }

         if (var8x > 0.6 && var10x < 0.5) {
            var12x = 1842196;
         }

         double var13 = (var4x + var5x) % 4 < 2 ? 0.97 : 1.03;
         return Capes.shade(var12x, var13);
      });
      double var6 = 80.0;
      double var8 = 70.0;
      var0.setColor(Capes.col(0, 0.35));
      var0.fill(new java.awt.geom.RoundRectangle2D.Double(var6 - 22.0, var8 - 16.0 + 2.0, 44.0, 34.0, 8.0, 8.0));
      var0.setColor(new Color(3818532));
      var0.fill(new java.awt.geom.RoundRectangle2D.Double(var6 - 22.0, var8 - 16.0, 44.0, 32.0, 8.0, 8.0));
      var0.setColor(new Color(13156496));
      var0.setStroke(new BasicStroke(1.0F, 0, 1, 1.0F, new float[]{2.0F, 2.0F}, 0.0F));
      var0.draw(new java.awt.geom.RoundRectangle2D.Double(var6 - 19.0, var8 - 13.0, 38.0, 26.0, 6.0, 6.0));
      java.awt.geom.Path2D.Double var10 = new java.awt.geom.Path2D.Double();

      for (int var11 = 0; var11 < 10; var11++) {
         double var12 = (-Math.PI / 2) + var11 * Math.PI / 5.0;
         double var14 = var11 % 2 == 0 ? 10.0 : 4.0;
         if (var11 == 0) {
            var10.moveTo(var6 + Math.cos(var12) * var14, var8 + Math.sin(var12) * var14);
         } else {
            var10.lineTo(var6 + Math.cos(var12) * var14, var8 + Math.sin(var12) * var14);
         }
      }

      var10.closePath();
      var0.setColor(new Color(14209184));
      var0.fill(var10);
   }

   static void bricks(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      Capes.field(var1, (var0x, var2x, var4x, var5x) -> 1381656);
      int[] var6 = new int[]{14876683, 16764163, 27831, 34091, 16088352, 16777215, 10534120, 15233184};
      Random var7 = new Random(4L);
      double var8 = 16.0;

      for (int var10 = 0; var10 < 16; var10++) {
         double var11 = var10 * var8 * 2.0 - 4.0 + 0.0;
         double var13 = -(var10 % 2) * var8 * 2.0;

         while (var13 < 160.0) {
            int var15 = 2 + var7.nextInt(3);
            int var16 = var6[var7.nextInt(var6.length)];
            brick(var0, var13, var11, var15 * var8, var8 * 2.0, var16, var8);
            var13 += var15 * var8;
         }
      }
   }

   private static void brick(Graphics2D var0, double var1, double var3, double var5, double var7, int var9, double var10) {
      new Color(var9);
      var0.setPaint(
         new GradientPaint(
            (float)var1,
            (float)var3,
            new Color(Capes.mix(var9, 16777215, 0.2)),
            (float)(var1 + var5),
            (float)(var3 + var7),
            new Color(Capes.mix(var9, 0, 0.25))
         )
      );
      var0.fill(new java.awt.geom.RoundRectangle2D.Double(var1 + 0.8, var3 + 0.8, var5 - 1.6, var7 - 1.6, 3.0, 3.0));
      var0.setColor(Capes.col(Capes.mix(var9, 0, 0.5), 0.8));
      var0.setStroke(new BasicStroke(1.0F));
      var0.draw(new java.awt.geom.RoundRectangle2D.Double(var1 + 0.8, var3 + 0.8, var5 - 1.6, var7 - 1.6, 3.0, 3.0));

      for (double var13 = var1 + var10 / 2.0; var13 < var1 + var5; var13 += var10) {
         for (double var15 = var3 + var10 / 2.0; var15 < var3 + var7; var15 += var10) {
            double var17 = var10 * 0.31;
            var0.setColor(Capes.col(0, 0.35));
            var0.fill(new java.awt.geom.Ellipse2D.Double(var13 - var17 + 1.2, var15 - var17 + 1.6, var17 * 2.0, var17 * 2.0));
            var0.setPaint(
               new GradientPaint(
                  (float)(var13 - var17),
                  (float)(var15 - var17),
                  new Color(Capes.mix(var9, 16777215, 0.35)),
                  (float)(var13 + var17),
                  (float)(var15 + var17),
                  new Color(Capes.mix(var9, 0, 0.15))
               )
            );
            var0.fill(new java.awt.geom.Ellipse2D.Double(var13 - var17, var15 - var17, var17 * 2.0, var17 * 2.0));
            var0.setColor(Capes.col(16777215, 0.3));
            var0.draw(new java.awt.geom.Arc2D.Double(var13 - var17 * 0.7, var15 - var17 * 0.7, var17 * 1.4, var17 * 1.4, 100.0, 80.0, 0));
         }
      }
   }

   static void racing(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      Capes.field(var1, (var0x, var2x, var4x, var5x) -> {
         double var6x = Math.sin(var2x * Math.PI * 2.0 * 1.2 + var0x * 5.0);
         double var8 = var0x + 0.035 * var6x;
         double var10 = var2x + 0.03 * Math.sin(var0x * Math.PI * 2.0 * 1.4 + var2x * 3.0);
         int var12 = (int)Math.floor(var8 * 6.0);
         int var13 = (int)Math.floor(var10 * 9.6);
         int var14 = (var12 + var13 & 1) == 0 ? 15921906 : 1315860;
         double var15 = 0.84 + 0.2 * Math.cos(var2x * Math.PI * 2.0 * 1.2 + var0x * 5.0);
         return Capes.add(Capes.shade(var14, var15), 16777215, Math.pow(Math.max(0.0, Math.cos(var2x * Math.PI * 2.0 * 1.2 + var0x * 5.0 - 0.6)), 10.0) * 0.12);
      });
      var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(16722474), 160.0F, 0.0F, new Color(11536906)));
      java.awt.geom.Path2D.Double var6 = new java.awt.geom.Path2D.Double();
      var6.moveTo(0.0, 200.0);
      var6.lineTo(160.0, 150.0);
      var6.lineTo(160.0, 168.0);
      var6.lineTo(0.0, 218.0);
      var6.closePath();
      var0.setColor(Capes.col(0, 0.35));
      var0.fill(AffineTransform.getTranslateInstance(0.0, 3.0).createTransformedShape(var6));
      var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(16726586), 160.0F, 0.0F, new Color(11536906)));
      var0.fill(var6);
   }

   static void moon(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      Capes.field(var1, (var0x, var2x, var4x, var5x) -> {
         int var6x = Capes.ramp(var2x, 330276, 924232, 2763370, 4864634);
         double var7x = Math.hypot(var4x - 80.0, var5x - 84.0);
         var6x = Capes.add(var6x, 13161727, Math.exp(-Math.max(0.0, var7x - 34.0) / 20.0) * 0.35);
         if (var7x < 35.0) {
            double var9 = Capes.fbm(var4x / 14.0, var5x / 14.0, 0, 0, 5, 9);
            int var11 = Capes.mix(16776170, 13157556, Capes.smooth(0.45, 0.7, var9));
            double var12x = Math.sqrt(Math.max(0.0, 1.0 - var7x / 34.0 * (var7x / 34.0)));
            var11 = Capes.shade(var11, 0.72 + 0.3 * var12x);
            var6x = Capes.mix(var6x, var11, Capes.smooth(34.8, 33.2, var7x));
         }

         double var17 = Capes.fbm(var0x * 2.2, var2x * 7.0, 0, 0, 5, 19);
         double var19 = Math.exp(-Math.pow((var2x - 0.44) / 0.07, 2.0)) + Math.exp(-Math.pow((var2x - 0.66) / 0.09, 2.0));
         double var13 = Capes.smooth(0.45, 0.62, var17 * var19 + var19 * 0.15);
         int var15 = Capes.mix(3820170, 12109040, Capes.smooth(0.5, 0.8, var17) * (0.5 + 0.5 * Math.exp(-var7x / 60.0)));
         return Capes.mix(var6x, var15, var13 * 0.85);
      });
      Random var6 = new Random(1L);

      for (int var7 = 0; var7 < 50; var7++) {
         double var8 = var6.nextDouble() * 160.0;
         double var10 = var6.nextDouble() * 256.0 * 0.8;
         if (!(Math.hypot(var8 - 80.0, var10 - 84.0) < 40.0)) {
            Capes.star(var0, var8, var10, 0.5 + Math.pow(var6.nextDouble(), 3.0) * 2.0, 14739711, 0.4 + 0.5 * var6.nextDouble());
         }
      }

      var0.setColor(new Color(329242));
      java.awt.geom.Path2D.Double var12 = new java.awt.geom.Path2D.Double();
      var12.moveTo(0.0, 256.0);
      var12.lineTo(0.0, 222.0);
      var12.lineTo(30.0, 214.0);
      var12.lineTo(30.0, 196.0);
      var12.lineTo(34.0, 196.0);
      var12.lineTo(34.0, 190.0);
      var12.lineTo(38.0, 190.0);
      var12.lineTo(38.0, 196.0);
      var12.lineTo(42.0, 196.0);
      var12.lineTo(42.0, 176.0);
      var12.lineTo(40.0, 176.0);
      var12.lineTo(48.0, 160.0);
      var12.lineTo(56.0, 176.0);
      var12.lineTo(54.0, 176.0);
      var12.lineTo(54.0, 200.0);
      var12.lineTo(84.0, 200.0);
      var12.lineTo(84.0, 186.0);
      var12.lineTo(88.0, 186.0);
      var12.lineTo(88.0, 192.0);
      var12.lineTo(92.0, 192.0);
      var12.lineTo(92.0, 186.0);
      var12.lineTo(96.0, 186.0);
      var12.lineTo(96.0, 206.0);
      var12.lineTo(120.0, 212.0);
      var12.lineTo(160.0, 206.0);
      var12.lineTo(160.0, 256.0);
      var12.closePath();
      var0.fill(var12);
      var0.setColor(new Color(16765562));
      var0.fill(new java.awt.geom.Rectangle2D.Double(46.5, 180.0, 3.0, 5.0));
      var0.fill(new java.awt.geom.Rectangle2D.Double(88.5, 196.0, 2.5, 4.0));
      Capes.addDot(var1, 48.0, 182.0, 6.0, 16758858, 0.35);
   }
}
