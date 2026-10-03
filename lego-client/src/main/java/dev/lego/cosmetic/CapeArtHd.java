package dev.lego.cosmetic;

import dev.lego.ui.Art;
import dev.lego.ui.Gx;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Path2D.Double;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

final class CapeArtHd {
   private CapeArtHd() {
   }

   static void relief(BufferedImage var0, double[] var1, double var2, double var4, double var6, double var8) {
      int var10 = var0.getWidth();
      int var11 = var0.getHeight();
      int[] var12 = Capes.px(var0);
      double var13 = -0.5;
      double var15 = -0.62;
      double var17 = 0.6;
      double var19 = Math.sqrt(var13 * var13 + var15 * var15 + var17 * var17);
      var13 /= var19;
      var15 /= var19;
      var17 /= var19;
      double var25 = var17 + 1.0;
      double var27 = Math.sqrt(var13 * var13 + var15 * var15 + var25 * var25);
      double var21 = var13 / var27;
      double var23 = var15 / var27;
      var25 /= var27;
      double[] var29 = boxBlur(var1, var10, var11, 5);

      for (int var30 = 0; var30 < var11; var30++) {
         for (int var31 = 0; var31 < var10; var31++) {
            int var32 = var30 * var10 + var31;
            double var33 = var1[var30 * var10 + Math.min(var10 - 1, var31 + 1)] - var1[var30 * var10 + Math.max(0, var31 - 1)];
            double var35 = var1[Math.min(var11 - 1, var30 + 1) * var10 + var31] - var1[Math.max(0, var30 - 1) * var10 + var31];
            double var37 = -var33 * var2;
            double var39 = -var35 * var2;
            double var41 = 1.0;
            double var43 = Math.sqrt(var37 * var37 + var39 * var39 + var41 * var41);
            var37 /= var43;
            var39 /= var43;
            var41 /= var43;
            double var45 = var37 * var13 + var39 * var15 + var41 * var17;
            double var47 = 1.0 + (var45 - var17) * 1.35;
            double var49 = Math.min(0.0, var1[var32] - var29[var32]) * var6;
            var47 *= 1.0 + var49;
            if (var8 > 0.0) {
               double var51 = 0.0;

               for (int var53 = 1; var53 <= 6; var53++) {
                  int var54 = var31 - (int)Math.round(var53 * 0.8);
                  int var55 = var30 - var53;
                  if (var54 < 0 || var55 < 0) {
                     break;
                  }

                  double var56 = var1[var55 * var10 + var54] - var1[var32] - var53 * 0.06;
                  if (var56 > 0.0) {
                     var51 = Math.max(var51, Math.min(1.0, var56 * 3.0));
                  }
               }

               var47 *= 1.0 - var51 * var8;
            }

            int var66 = Capes.shade(var12[var32], var47);
            double var52 = Math.pow(Math.max(0.0, var37 * var21 + var39 * var23 + var41 * var25), 40.0) * var4;
            var12[var32] = var12[var32] & 0xFF000000 | Capes.add(var66, 16777215, var52) & 16777215;
         }
      }
   }

   static double[] boxBlur(double[] var0, int var1, int var2, int var3) {
      double[] var4 = new double[var0.length];
      double[] var5 = new double[var0.length];

      for (int var6 = 0; var6 < var2; var6++) {
         double var7 = 0.0;
         int var9 = 0;

         for (int var10 = -var3; var10 < var1 + var3; var10++) {
            int var11 = var10 + var3;
            int var12 = var10 - var3 - 1;
            if (var11 < var1 && var11 >= 0) {
               var7 += var0[var6 * var1 + var11];
               var9++;
            }

            if (var12 >= 0 && var12 < var1) {
               var7 -= var0[var6 * var1 + var12];
               var9--;
            }

            if (var10 >= 0 && var10 < var1) {
               var4[var6 * var1 + var10] = var7 / Math.max(1, var9);
            }
         }
      }

      for (int var13 = 0; var13 < var1; var13++) {
         double var14 = 0.0;
         int var15 = 0;

         for (int var16 = -var3; var16 < var2 + var3; var16++) {
            int var17 = var16 + var3;
            int var18 = var16 - var3 - 1;
            if (var17 < var2 && var17 >= 0) {
               var14 += var4[var17 * var1 + var13];
               var15++;
            }

            if (var18 >= 0 && var18 < var2) {
               var14 -= var4[var18 * var1 + var13];
               var15--;
            }

            if (var16 >= 0 && var16 < var2) {
               var5[var16 * var1 + var13] = var14 / Math.max(1, var15);
            }
         }
      }

      return var5;
   }

   static double[] heights(BufferedImage var0) {
      int[] var1 = Capes.px(var0);
      double[] var2 = new double[var1.length];

      for (int var3 = 0; var3 < var1.length; var3++) {
         var2[var3] = (var1[var3] >> 16 & 0xFF) / 255.0;
      }

      return var2;
   }

   static BufferedImage canvas() {
      return new BufferedImage(160, 256, 2);
   }

   static void scales(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double[] var6 = new double[40960];
      Capes.field(var1, (var1x, var3, var5x, var6x) -> {
         int var7x = (int)Math.floor((var6x - 14.0) / 12.0) - 1;

         for (int var8x = var7x; var8x <= var7x + 3; var8x++) {
            double var9 = var8x * 12.0;
            double var11 = Math.floorMod(var8x, 2) * 22.0 / 2.0;
            int var13 = (int)Math.floor((var5x - var11) / 22.0 + 0.5);

            for (int var14 = var13 - 1; var14 <= var13 + 1; var14++) {
               double var15 = var14 * 22.0 + var11;
               double var17 = var5x + 0.5 - var15;
               double var19 = var6x + 0.5 - var9;
               if (!(var19 < -2.0)) {
                  double var21 = 14.0 * (1.0 - 0.35 * Capes.clamp01(var19 / 14.0));
                  double var23 = Math.hypot(var17 / var21 * 14.0, var19) / 14.0;
                  if (!(var23 > 1.0)) {
                     double var25 = Capes.hash(var14, var8x, 4);
                     double var27 = Math.exp(-Math.pow(var17 / 1.8, 2.0)) * 0.18 * Capes.clamp01(var19 / 14.0 + 0.3);
                     double var29 = Math.sqrt(Math.max(0.0, 1.0 - var23 * var23));
                     var6[var6x * 160 + var5x] = 0.25 + 0.55 * var29 + var27 + var8x % 2 * 0.02;
                     double var31 = Capes.clamp01(0.5 + var17 / 33.6 + (var25 - 0.5) * 0.35 + var3 * 0.25 - var29 * 0.2);
                     int var33 = Capes.ramp(var31, 674362, 1280610, 2080912, 8052896, 15255642);
                     int var34 = Capes.shade(var33, 0.85 + 0.25 * var29 - var3 * 0.25);
                     double var35 = Math.atan2(var19, var17);
                     double var37 = Math.pow(Math.abs(Math.sin(var35 * 5.0 + var25 * 3.0)), 12.0) * 0.12 * var23;
                     var34 = Capes.shade(var34, 1.0 - var37);
                     if (var23 > 0.9) {
                        var34 = Capes.mix(var34, 13168800, (var23 - 0.9) / 0.1 * 0.35);
                     }

                     return var34;
                  }
               }
            }
         }

         var6[var6x * 160 + var5x] = 0.0;
         return 201742;
      });
      relief(var1, var6, 9.0, 0.45, 1.1, 0.5);

      for (byte var7 = 0; var7 < 256; var7 += 12) {
         Double var8 = new Double();
         var8.moveTo(75.0, var7 + 10);
         var8.lineTo(80.0, var7 - 2);
         var8.lineTo(85.0, var7 + 10);
         var8.closePath();
         var0.setPaint(new GradientPaint(75.0F, 0.0F, new Color(16773280), 85.0F, 0.0F, new Color(9067024)));
         var0.fill(var8);
         var0.setColor(Capes.col(0, 0.4));
         var0.setStroke(new BasicStroke(0.8F));
         var0.draw(var8);
      }
   }

   static void camo(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      Capes.field(var1, (var0x, var2x, var4x, var5x) -> {
         double var6x = Capes.fbm(var0x * 3.0, var2x * 5.0, 0, 0, 3, 70);
         double var8x = Capes.fbm(var0x * 3.2 + var6x, var2x * 5.0 + var6x, 0, 0, 4, 71);
         double var10x = Capes.fbm(var0x * 4.0 + 5.0 + var6x * 0.6, var2x * 6.0, 0, 0, 4, 72);
         int var12x = 5925430;
         var12x = Capes.mix(var12x, 3029791, Capes.smooth(0.47, 0.41, var8x));
         var12x = Capes.mix(var12x, 9075278, Capes.smooth(0.55, 0.61, var10x));
         var12x = Capes.mix(var12x, 1842196, Capes.smooth(0.57, 0.63, var8x) * Capes.smooth(0.53, 0.47, var10x));
         return Capes.mix(var12x, 10132090, Capes.fbm(var0x * 12.0, var2x * 18.0, 0, 0, 2, 73) * 0.12);
      });
      BufferedImage var6 = canvas();
      int[] var7 = Capes.px(var6);

      for (int var8 = 0; var8 < 256; var8++) {
         for (int var9 = 0; var9 < 160; var9++) {
            double var10 = (var9 + var8) % 4 < 2 ? 0.35 : 0.25;
            boolean var12 = var9 % 12 == 0 || var8 % 12 == 0;
            int var13 = (int)((var10 + (var12 ? 0.12 : 0.0)) * 255.0);
            var7[var8 * 160 + var9] = 0xFF000000 | var13 << 16 | var13 << 8 | var13;
         }
      }

      Graphics2D var14 = var6.createGraphics();
      Gx.hints(var14);
      double var15 = 80.0;
      double var11 = 70.0;
      var14.setColor(new Color(10132122));
      var14.fill(new java.awt.geom.RoundRectangle2D.Double(var15 - 22.0, var11 - 16.0, 44.0, 32.0, 8.0, 8.0));
      var14.setColor(new Color(14211288));
      var14.setStroke(new BasicStroke(1.4F, 0, 1, 1.0F, new float[]{2.0F, 1.5F}, 0.0F));
      var14.draw(new java.awt.geom.RoundRectangle2D.Double(var15 - 19.0, var11 - 13.0, 38.0, 26.0, 6.0, 6.0));
      var14.setColor(new Color(16777215));
      var14.fill(XmasTex.starShape(var15, var11, 10.0, 4.0, 5));
      var14.dispose();
      var0.setColor(new Color(3818532));
      var0.fill(new java.awt.geom.RoundRectangle2D.Double(var15 - 22.0, var11 - 16.0, 44.0, 32.0, 8.0, 8.0));
      var0.setColor(new Color(13156496));
      var0.setStroke(new BasicStroke(1.2F, 0, 1, 1.0F, new float[]{2.0F, 1.5F}, 0.0F));
      var0.draw(new java.awt.geom.RoundRectangle2D.Double(var15 - 19.0, var11 - 13.0, 38.0, 26.0, 6.0, 6.0));
      var0.setColor(new Color(14209184));
      var0.fill(XmasTex.starShape(var15, var11, 10.0, 4.0, 5));
      double[] var16 = boxBlur(heights(var6), 160, 256, 1);
      relief(var1, var16, 6.0, 0.12, 0.8, 0.6);
   }

   static void bricks(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      int[] var6 = new int[]{14876683, 16764163, 27831, 34091, 16088352, 16053492, 10534120, 15233184};
      int[] var7 = new int[40960];
      Arrays.fill(var7, -1);
      double[] var8 = new double[40960];
      Random var9 = new Random(4L);
      double var10 = 16.0;
      ArrayList var12 = new ArrayList();

      for (int var13 = 0; var13 < 9; var13++) {
         double var14 = var13 * var10 * 2.0 - 4.0;
         double var16 = -(var13 % 2) * var10 * 2.0;

         while (var16 < 160.0) {
            int var18 = 2 + var9.nextInt(3);
            int var19 = var6[var9.nextInt(var6.length)];
            double var20 = var18 * var10;
            double var22 = var10 * 2.0;

            for (int var24 = (int)Math.max(0.0, var14); var24 < Math.min(256.0, var14 + var22); var24++) {
               for (int var25 = (int)Math.max(0.0, var16); var25 < Math.min(160.0, var16 + var20); var25++) {
                  double var26 = Math.min(var25 + 0.5 - var16, var16 + var20 - var25 - 0.5);
                  double var28 = Math.min(var24 + 0.5 - var14, var14 + var22 - var24 - 0.5);
                  double var30 = Math.min(var26, var28);
                  if (!(var30 < 0.6)) {
                     var7[var24 * 160 + var25] = var19;
                     var8[var24 * 160 + var25] = 0.5 * Capes.smooth(0.6, 2.4, var30);
                  }
               }
            }

            for (double var38 = var16 + var10 / 2.0; var38 < var16 + var20; var38 += var10) {
               for (double var39 = var14 + var10 / 2.0; var39 < var14 + var22; var39 += var10) {
                  var12.add(new double[]{var38, var39, var19});
               }
            }

            var16 += var20;
         }
      }

      double var32 = var10 * 0.31;

      for (double[] var34 : (Iterable<double[]>) (Iterable<?>) (var12)) {
         for (int var17 = (int)(var34[1] - var32 - 1.0); var17 <= var34[1] + var32 + 1.0; var17++) {
            for (int var36 = (int)(var34[0] - var32 - 1.0); var36 <= var34[0] + var32 + 1.0; var36++) {
               if (var36 >= 0 && var17 >= 0 && var36 < 160 && var17 < 256) {
                  double var37 = Math.hypot(var36 + 0.5 - var34[0], var17 + 0.5 - var34[1]);
                  if (!(var37 > var32)) {
                     double var21 = 0.9 - 0.25 * Capes.smooth(var32 - 1.3, var32, var37);
                     var8[var17 * 160 + var36] = Math.max(var8[var17 * 160 + var36], var21);
                  }
               }
            }
         }
      }

      Capes.field(var1, (var1x, var3, var5x, var6x) -> {
         int var7x = var7[var6x * 160 + var5x];
         return var7x < 0 ? 789519 : Capes.shade(var7x, 0.95 + 0.05 * Capes.hash(var5x / 3, var6x / 3, 11));
      });
      relief(var1, var8, 14.0, 0.65, 1.3, 0.75);
      var0.setStroke(new BasicStroke(0.8F));

      for (double[] var35 : (Iterable<double[]>) (Iterable<?>) (var12)) {
         var0.setColor(Capes.col(16777215, 0.22));
         var0.draw(new java.awt.geom.Arc2D.Double(var35[0] - var32 * 0.55, var35[1] - var32 * 0.55, var32 * 1.1, var32 * 1.1, 90.0, 110.0, 0));
      }
   }

   static void racing(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double[] var6 = new double[40960];
      Capes.field(var1, (var1x, var3, var5x, var6x) -> {
         double var7 = Math.sin(var3 * Math.PI * 2.0 * 1.2 + var1x * 5.0) + 0.35 * Math.sin(var1x * Math.PI * 2.0 * 1.6 - var3 * 4.0 + 1.0);
         var6[var6x * 160 + var5x] = 0.5 + 0.28 * var7 + 0.02 * ((var5x + var6x) % 3 == 0 ? 1 : 0);
         double var9 = var1x + 0.035 * var7;
         double var11 = var3 + 0.03 * Math.sin(var1x * Math.PI * 2.0 * 1.4 + var3 * 3.0);
         int var13 = (int)Math.floor(var9 * 6.0);
         int var14 = (int)Math.floor(var11 * 9.6);
         double var15 = var9 * 6.0 - var13;
         double var17 = var11 * 9.6 - var14;
         double var19 = Math.min(Math.min(var15, 1.0 - var15), Math.min(var17, 1.0 - var17));
         int var21 = (var13 + var14 & 1) == 0 ? 15921906 : 1447446;
         if (var19 < 0.04) {
            var21 = Capes.mix(var21, 8684676, (0.04 - var19) / 0.04 * 0.5);
         }

         double var22 = var11 * 256.0 - (200.0 - var9 * 50.0);
         if (var22 > -9.0 && var22 < 9.0) {
            var21 = Capes.mix(15212586, 10488338, (var22 + 9.0) / 18.0);
         }

         return var21;
      });
      relief(var1, var6, 24.0, 0.25, 0.4, 0.0);
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
      double[] var13 = new double[40960];
      Capes.field(var1, (var7, var9, var11x, var12) -> {
         double var13x = Capes.fbm(var7 * 3.0 + var6 * 3.0, var9 * 4.0, 3, 0, 3, 40);
         double var15x = Capes.fbm(var7 * 3.0 + var6 * 3.0 + (var13x - 0.5) * 0.8, var9 * 4.0 + (var13x - 0.5), 3, 0, 5, 41);
         double var17x = Capes.smooth(0.2, 0.75, var15x + (0.45 - var9) * 1.1);
         var13[var12 * 160 + var11x] = var17x * (0.6 + 0.4 * var15x);
         int var19x = Capes.ramp(var9, 1976380, 1186342, 527122);
         int var20x = Capes.ramp(var15x, 1053726, 2765898, 5595774, 9083060);
         int var21x = Capes.mix(var19x, var20x, var17x);
         double var22 = var8 * (0.25 + 0.75 * var15x) * Math.exp(-Math.hypot(var11x - var11, var12 - 40) / 90.0);
         var21x = Capes.add(var21x, 12572927, var22 * 0.9 + var8 * 0.08);
         double var24x = 0.9 + 0.03 * Math.sin(var7 * 8.0 + 1.0) + 0.015 * Math.sin(var7 * 23.0);
         if (var9 > var24x) {
            var21x = Capes.add(Capes.mix(329483, 131844, (var9 - var24x) * 8.0), 9087231, var8 * 0.12);
            var13[var12 * 160 + var11x] = 0.0;
         }

         return var21x;
      });
      relief(var1, var13, 7.0, 0.08 + var8 * 0.3, 0.5, 0.2);
      Random var14 = new Random(71L);
      var0.setStroke(new BasicStroke(1.0F, 1, 1));

      for (int var15 = 0; var15 < 150; var15++) {
         double var16 = var14.nextDouble();
         double var18 = var14.nextDouble();
         int var20 = 2 + var14.nextInt(2);
         double var21 = (var18 + var20 * var6) % 1.0 * 276.0 - 10.0;
         double var23 = var16 * 190.0 - 15.0 + var21 * 0.12;
         double var25 = 7.0 + var14.nextDouble() * 7.0;
         var0.setColor(Capes.col(11059432, 0.18 + 0.2 * var14.nextDouble() + var8 * 0.2));
         var0.draw(new java.awt.geom.Line2D.Double(var23, var21, var23 + var25 * 0.12, var21 + var25));
      }

      if (var8 > 0.0) {
         Random var31 = new Random(var10 ? 5L : 9L);
         Double var32 = new Double();
         double var17 = var11;
         double var19 = 30.0;
         var32.moveTo(var11, var19);
         ArrayList var33 = new ArrayList();

         while (var19 < 230.4) {
            var17 += var31.nextGaussian() * 7.0;
            var19 += 8.0 + var31.nextDouble() * 10.0;
            var32.lineTo(var17, var19);
            if (var31.nextDouble() < 0.18) {
               var33.add(new double[]{var17, var19});
            }
         }

         for (double[] var34 : (Iterable<double[]>) (Iterable<?>) (var33)) {
            double var24 = var34[0];
            double var26 = var34[1];
            var32.moveTo(var24, var26);
            double var28 = var31.nextBoolean() ? 1.0 : -1.0;

            for (int var30 = 0; var30 < 4; var30++) {
               var24 += var28 * (4.0 + var31.nextDouble() * 8.0);
               var26 += 6.0 + var31.nextDouble() * 8.0;
               var32.lineTo(var24, var26);
            }
         }

         Capes.addDot(var1, var11, 40.0, 50.0, 10471679, 0.5 * var8);
         Capes.glowStroke(var0, var32, 11064575, 2.6F, 3.2F, var8);
         Capes.addDot(var1, var17, 230.4, 26.0, 11064575, 0.45 * var8);
      }
   }

   static void carbon(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double[] var6 = new double[40960];
      Capes.field(var1, (var1x, var3, var5x, var6x) -> {
         int var7x = var5x / 8;
         int var8x = var6x / 8;
         double var9x = (var5x % 8 + 0.5) / 8.0;
         double var11 = (var6x % 8 + 0.5) / 8.0;
         boolean var13 = (var7x + var8x & 3) < 2;
         double var14x = var13 ? var11 : var9x;
         double var16x = var13 ? var9x : var11;
         double var18x = Math.pow(Math.sin(Math.PI * var14x), 0.6);
         var6[var6x * 160 + var5x] = var18x * 0.6 + 0.08 * Math.sin(Math.PI * var16x);
         double var20 = 0.93 + 0.07 * Math.sin(var14x * Math.PI * 7.0 + var16x * 0.6);
         double var22 = var13 ? 0.55 + 0.45 * Math.sin(var1x * 5.0 - var3 * 3.0 + 1.0) : 0.55 + 0.45 * Math.sin(var1x * 4.0 + var3 * 2.0 + 3.0);
         return Capes.mix(789776, 4869720, var18x * var20 * (0.35 + 0.5 * var22));
      });
      relief(var1, var6, 6.0, 0.3, 0.8, 0.0);
      int[] var7 = Capes.px(var1);

      for (int var8 = 0; var8 < 256; var8++) {
         for (int var9 = 0; var9 < 160; var9++) {
            double var10 = (var9 + 0.5) / 160.0;
            double var12 = (var8 + 0.5) / 256.0;
            double var14 = Math.exp(-Math.pow((var10 * 0.9 - var12 * 0.55 - 0.12) / 0.1, 2.0)) * 0.16
               + Math.exp(-Math.pow((var10 * 0.9 - var12 * 0.55 + 0.35) / 0.2, 2.0)) * 0.08;
            var7[var8 * 160 + var9] = Capes.add(var7[var8 * 160 + var9], 16777215, var14);
         }
      }

      for (int var16 = 0; var16 < 2; var16++) {
         double var18 = var16 == 0 ? 40.0 : 212.0;
         var0.setPaint(new GradientPaint(0.0F, (float)var18 - 5.0F, new Color(16730698), 0.0F, (float)var18 + 5.0F, new Color(7997962)));
         var0.fill(new java.awt.geom.Rectangle2D.Double(0.0, var18 - 5.0, 160.0, 10.0));
         var0.setColor(Capes.col(16777215, 0.45));
         var0.fill(new java.awt.geom.Rectangle2D.Double(0.0, var18 - 5.0, 160.0, 1.2));
         var0.setColor(Capes.col(0, 0.45));
         var0.fill(new java.awt.geom.Rectangle2D.Double(0.0, var18 + 5.0, 160.0, 1.5));
         var0.setColor(Capes.col(16726586, 0.8));
         var0.fill(new java.awt.geom.Rectangle2D.Double(0.0, var18 + 8.0, 160.0, 2.0));
      }

      double var17 = 80.0;
      double var19 = 124.0;
      Capes.dot(var0, var17 + 3.0, var19 + 5.0, 60.0, 0, 0.6);
      var0.setPaint(
         new LinearGradientPaint(
            (float)var17 - 34.0F,
            (float)var19 - 34.0F,
            (float)var17 + 34.0F,
            (float)var19 + 34.0F,
            new float[]{0.0F, 0.45F, 0.55F, 1.0F},
            new Color[]{new Color(16777215), new Color(10660538), new Color(4870752), new Color(14212840)}
         )
      );
      var0.fill(new java.awt.geom.Ellipse2D.Double(var17 - 34.0, var19 - 34.0, 68.0, 68.0));
      var0.setPaint(
         new RadialGradientPaint((float)var17 - 6.0F, (float)var19 - 10.0F, 40.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(3290172), new Color(658190)})
      );
      var0.fill(new java.awt.geom.Ellipse2D.Double(var17 - 29.0, var19 - 29.0, 58.0, 58.0));
      Art.brick(var0, var17 - 24.0, var19 - 27.0, 48.0, new Color(14876683));
      var0.setColor(Capes.col(16777215, 0.55));
      var0.setStroke(new BasicStroke(1.4F));
      var0.draw(new java.awt.geom.Arc2D.Double(var17 - 32.0, var19 - 32.0, 64.0, 64.0, 60.0, 90.0, 0));
      var0.setColor(Capes.col(16777215, 0.18));
      var0.fill(new java.awt.geom.Arc2D.Double(var17 - 27.0, var19 - 27.0, 54.0, 54.0, 30.0, 120.0, 1));
   }

   static void fireflies(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var2x, var4x, var6x, var7) -> {
         int var8x = Capes.ramp(var4x, 200718, 535076, 1198650, 799780);
         var8x = Capes.add(var8x, 12120280, Math.exp(-Math.hypot(var2x - 0.5, (var4x - 0.1) * 0.8) / 0.16) * 0.38);
         double var9x = Capes.fbm(var2x * 3.0 + var6 * 3.0, var4x * 5.0, 3, 0, 4, 55) * Math.exp(-Math.pow((var4x - 0.72) / 0.14, 2.0));
         return Capes.add(var8x, 8042664, var9x * 0.4);
      });
      int[] var8 = Capes.px(var1);

      for (int var9 = 0; var9 < 256; var9++) {
         for (int var10 = 0; var10 < 160; var10++) {
            double var11 = (var10 + 0.5) / 160.0;
            double var13 = (var9 + 0.5) / 256.0;
            double var15 = Math.atan2(var11 - 0.5, var13 - 0.02);
            double var17 = Math.pow(0.5 + 0.5 * Math.sin(var15 * 38.0 + 1.3), 8.0) * Math.exp(-var13 * 2.6) * 0.1 * (0.7 + 0.3 * Math.sin(var2 + var15 * 5.0));
            var8[var9 * 160 + var10] = Capes.add(var8[var9 * 160 + var10], 13168856, var17);
         }
      }

      Random var30 = new Random(15L);
      int[] var31 = new int[]{1727050, 800816, 267794};
      double[] var32 = new double[40960];

      for (int var12 = 0; var12 < 3; var12++) {
         int var35 = var12 == 0 ? 8 : 5 - var12;

         for (int var14 = 0; var14 < var35; var14++) {
            double var39 = var12 == 0 ? (var14 + 0.3 + var30.nextDouble() * 0.4) * 160.0 / var35 : var30.nextDouble() * 160.0;
            double var43 = 5 + var12 * 5 + var30.nextDouble() * 4.0;

            for (int var19 = 0; var19 < 256; var19++) {
               double var20 = var43 / 2.0 + var43 * 0.2 * var19 / 256.0;

               for (int var22 = (int)Math.max(0.0, var39 - var20); var22 < Math.min(160.0, var39 + var20); var22++) {
                  double var23 = (var22 + 0.5 - var39) / var20;
                  double var25 = Math.sqrt(Math.max(0.0, 1.0 - var23 * var23));
                  double var27 = Capes.fbm(var22 * 0.35, var19 * 0.06, 0, 0, 3, 90 + var12);
                  int var29 = Capes.shade(var31[var12], 0.7 + 0.45 * var25 * (0.6 + 0.4 * var27));
                  var8[var19 * 160 + var22] = 0xFF000000 | var29;
                  var32[var19 * 160 + var22] = var25 * 0.5 + var27 * 0.25 + var12 * 0.1;
               }
            }
         }

         var0.setColor(new Color(var31[var12]));

         for (int var38 = 0; var38 < 16; var38++) {
            double var40 = var30.nextDouble() * 160.0;
            double var44 = -10.0 + var30.nextDouble() * (30 + var12 * 10);
            double var47 = 14.0 + var30.nextDouble() * 14.0;
            var0.fill(new java.awt.geom.Ellipse2D.Double(var40 - var47, var44 - var47 * 0.7, var47 * 2.0, var47 * 1.4));
         }
      }

      relief(var1, var32, 6.0, 0.1, 0.5, 0.0);
      var0.setStroke(new BasicStroke(1.4F, 1, 1));

      for (int var33 = 0; var33 < 160; var33++) {
         double var36 = var30.nextDouble() * 160.0;
         double var41 = 10.0 + var30.nextDouble() * 24.0;
         double var45 = var30.nextGaussian() * 5.0 + Math.sin(var2 + var36 * 0.05) * 1.5;
         var0.setColor(new Color(Capes.mix(200712, 2779706, var30.nextDouble() * 0.7)));
         var0.draw(new java.awt.geom.QuadCurve2D.Double(var36, 256.0, var36 + var45 * 0.3, 256.0 - var41 * 0.6, var36 + var45, 256.0 - var41));
      }

      for (int var34 = 0; var34 < 28; var34++) {
         double var37 = 12.0 + var30.nextDouble() * 136.0;
         double var42 = 50.0 + var30.nextDouble() * 190.0;
         int var46 = 1 + var30.nextInt(2);
         double var18 = var30.nextDouble() * 6.28;
         double var48 = var30.nextDouble() * 6.28;
         double var49 = var30.nextDouble() * 6.28;
         double var24 = var37 + 8.0 * Math.cos(var2 * var46 + var18) + 3.0 * Math.sin(2.0 * var2 + var48);
         double var26 = var42 + 6.0 * Math.sin(var2 * var46 + var49);
         double var28 = Math.pow(0.5 + 0.5 * Math.sin(var2 * (1 + var30.nextInt(2)) + var48), 2.2);
         Capes.addDot(var1, var24, var26, 16.0, 10157882, 0.22 * var28 + 0.02);
         Capes.addDot(var1, var24, var26, 6.0, 14221162, 0.5 * var28 + 0.04);
         Capes.addDot(var1, var24, var26, 2.0, 16777184, 1.0 * var28 + 0.1);
      }
   }
}
