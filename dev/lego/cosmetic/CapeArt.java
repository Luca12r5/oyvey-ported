package dev.lego.cosmetic;

import dev.lego.ui.Art;
import dev.lego.ui.Fonts;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;
import java.util.Random;

final class CapeArt {
   private CapeArt() {
   }

   static void defineAll() {
      Capes.def("lego", 16, 8.0F, 16769658, 10119698, 6161932, CapeArt::lego);
      Capes.def("galaxy", 16, 6.0F, 15591167, 5917322, 1181732, CapeArt::galaxy);
      Capes.def("flames", 16, 12.0F, 16761466, 5906954, 2361349, CapeArt::flames);
      Capes.def("ice", 12, 6.0F, 16777215, 7247550, 1456740, CapeArt::ice);
      Capes.def("aurora", 16, 6.0F, 12124136, 1986130, 397854, CapeArt::aurora);
      Capes.def("sunset", 16, 8.0F, 16767370, 10496618, 1967672, CapeArt::sunset);
      Capes.def("carbon", 1, 0.0F, 16734810, 6949386, 921105, CapeArtHd::carbon);
      Capes.def("neon", 16, 8.0F, 16747253, 4852322, 786968, CapeArt::neon);
      Capes.def("spotify", 16, 10.0F, 8191912, 809514, 724236, CapeArt::spotify);
      Capes.def("sakura", 16, 6.0F, 16774102, 11896922, 15313344, CapeArt::sakura);
      Capes.def("lava", 16, 6.0F, 10123882, 1970702, 1837318, CapeArt::lava);
      Capes.def("ocean", 16, 8.0F, 15138815, 3042962, 469578, CapeArt::ocean);
      Capes.def("matrix", 16, 12.0F, 9240488, 670230, 133636, CapeArt::matrix);
   }

   private static Font bold(float var0) {
      return Fonts.get(3, var0);
   }

   static void lego(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      Capes.field(var1, (var0x, var2x, var4x, var5x) -> {
         int var6x = Capes.mix(16003642, 9045776, var2x * 0.85 + var0x * 0.15);
         double var7x = Math.hypot(var0x - 0.5, (var2x - 0.42) * 0.62);
         return Capes.add(var6x, 16743002, Math.exp(-var7x * var7x / 0.03) * 0.25);
      });

      for (int var6 = 0; var6 < 14; var6++) {
         for (int var7 = 0; var7 < 9; var7++) {
            double var8 = 16 + var7 * 16;
            double var10 = 16.0 + var6 * 18.5;
            double var12 = Math.hypot((var8 - 80.0) / 60.0, (var10 - 110.0) / 70.0);
            double var14 = 0.35 + 0.65 * Capes.clamp01(var12 - 0.55);
            var0.setColor(Capes.col(3801092, 0.28 * var14));
            var0.fill(new Double(var8 - 5.0 + 1.4, var10 - 5.0 + 2.0, 10.0, 10.0));
            var0.setPaint(
               new GradientPaint(
                  (float)var8 - 5.0F,
                  (float)var10 - 5.0F,
                  Capes.col(16777215, 0.3 * var14),
                  (float)var8 + 5.0F,
                  (float)var10 + 5.0F,
                  Capes.col(5898246, 0.25 * var14)
               )
            );
            var0.fill(new Double(var8 - 5.0, var10 - 5.0, 10.0, 10.0));
            var0.setColor(Capes.col(16777215, 0.14 * var14));
            var0.setStroke(new BasicStroke(0.8F));
            var0.draw(new Double(var8 - 3.2, var10 - 3.2, 6.4, 6.4));
         }
      }

      Capes.dot(var0, 80.0, 104.0, 62.0, 3801092, 0.35);
      Art.brick(var0, 34.0, 54.0, 92.0, new Color(16764163));
      String var16 = "LEGO";
      var0.setFont(bold(31.0F));
      float var17 = var0.getFontMetrics().stringWidth(var16);
      var0.setColor(Capes.col(3801092, 0.55));
      var0.drawString(var16, 80.0F - var17 / 2.0F + 1.5F, 198.5F);
      var0.setPaint(new GradientPaint(0.0F, 170.0F, Color.WHITE, 0.0F, 198.0F, new Color(16770992)));
      var0.drawString(var16, 80.0F - var17 / 2.0F, 196.0F);
      var0.setFont(bold(11.0F));
      String var18 = "C L I E N T";
      float var9 = var0.getFontMetrics().stringWidth(var18);
      var0.setColor(Capes.col(16769658, 0.9));
      var0.drawString(var18, 80.0F - var9 / 2.0F, 216.0F);
      var0.setColor(Capes.col(16769658, 0.5));
      var0.setStroke(new BasicStroke(1.2F));
      var0.drawLine(30, 211, (int)(80.0F - var9 / 2.0F - 6.0F), 211);
      var0.drawLine((int)(80.0F + var9 / 2.0F + 6.0F), 211, 130, 211);
      Capes.sweep(var1, (double)var4 / var5, 0.09, 0.42);
   }

   static void galaxy(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = var2 / 2.0;
      Capes.field(var1, (var2x, var4x, var6x, var7) -> {
         double var8x = var2x * 2.6;
         double var10x = var4x * 4.2;
         double var12x = Capes.fbm(var8x + 3.0, var10x, 0, 0, 3, 11);
         double var14x = Capes.fbm(var8x + var12x * 1.6, var10x + var12x * 1.2, 0, 0, 5, 12);
         double var16x = Capes.fbm(var8x * 0.6 + 9.0, var10x * 0.6, 0, 0, 3, 13);
         int var18 = Capes.mix(131337, 721952, var4x);
         double var19x = Math.pow(Capes.smooth(0.38, 0.9, var14x), 1.5);
         var18 = Capes.add(var18, Capes.ramp(var16x, 1981183, 6958048, 14696607, 16743002), var19x * 0.7);
         double var21x = var6x + 0.5 - 80.0;
         double var23 = (var7 + 0.5 - 110.08) / 0.62;
         double var25 = Math.sqrt(var21x * var21x + var23 * var23) + 0.001;
         double var27 = Math.atan2(var23, var21x) - var6 - 2.9 * Math.log(var25 / 16.0 + 0.2);
         double var29 = (var27 % Math.PI + Math.PI) % Math.PI;
         double var31 = Math.pow(0.5 + 0.5 * Math.cos(2.0 * var27), 2.4);
         double var33 = Capes.fbm(var25 * 0.11, var29 * 8.0 / Math.PI, 0, 8, 3, 21);
         double var35 = Math.exp(-var25 / 34.0);
         double var37 = var31 * var35 * (0.35 + 0.95 * var33) + Math.exp(-(var25 / 8.0) * (var25 / 8.0)) * 1.4 + Math.exp(-var25 / 18.0) * 0.3;
         int var39 = Capes.mix(16773328, 10273023, Capes.smooth(4.0, 30.0, var25));
         var18 = Capes.add(var18, var39, var37 * 0.95);
         if (var33 > 0.62) {
            var18 = Capes.add(var18, 16734888, (var33 - 0.62) * 2.2 * var31 * var35);
         }

         double var40 = Capes.smooth(0.5, 0.75, Capes.fbm(var25 * 0.2 + 5.0, var29 * 8.0 / Math.PI, 0, 8, 2, 23)) * var31 * Math.exp(-var25 / 45.0) * 0.55;
         return Capes.shade(var18, 1.0 - var40);
      });
      Random var8 = new Random(5L);

      for (int var9 = 0; var9 < 150; var9++) {
         double var10 = var8.nextDouble() * 160.0;
         double var12 = var8.nextDouble() * 256.0;
         double var14 = Math.pow(var8.nextDouble(), 5.0);
         int var16 = 1 + var8.nextInt(2);
         double var17 = var8.nextDouble() * Math.PI * 2.0;
         double var19 = (0.35 + 0.65 * (0.5 + 0.5 * Math.sin(var2 * var16 + var17))) * (0.5 + 0.5 * var8.nextDouble());
         int var21 = var8.nextInt(3) == 0 ? 16766640 : (var8.nextBoolean() ? 12112127 : 16777215);
         Capes.star(var0, var10, var12, 0.7 + var14 * 2.6, var21, var19);
      }
   }

   static void flames(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var2x, var4x, var6x, var7) -> {
         double var8x = var2x * 4.0;
         double var10x = var4x * 6.0 + var6 * 12.0;
         double var12x = Capes.fbm(var8x * 1.2 + 7.0, var10x, 0, 6, 3, 5);
         double var14x = Capes.fbm(var8x + (var12x - 0.5) * 1.6, var10x + (var12x - 0.5) * 0.8, 0, 6, 5, 7);
         double var16 = 0.13 * Math.cos(var2x * Math.PI * 2.0 * 3.0 + (var12x - 0.5) * 4.0);
         double var18 = var4x * 1.32 - 0.42 + (var14x - 0.5) * 1.25 + var16;
         int var20 = Capes.mix(655874, 3016709, var4x);
         var20 = Capes.add(var20, 16730640, Math.pow(var4x, 3.0) * 0.35);
         int var21x = Capes.ramp(var18 / 1.2, 5900805, 11540490, 15747598, 16748062, 16763210, 16774092);
         double var22 = Capes.smooth(-0.02, 0.14, var18);
         int var24 = Capes.mix(var20, var21x, var22);
         return Capes.add(var24, 16771232, Capes.smooth(0.85, 1.3, var18) * 0.25);
      });
      Random var8 = new Random(8L);

      for (int var9 = 0; var9 < 46; var9++) {
         double var10 = var8.nextDouble();
         double var12 = var8.nextDouble();
         int var14 = 1 + var8.nextInt(2);
         double var15 = var8.nextDouble() * Math.PI * 2.0;
         double var17 = ((var12 - var14 * var6) % 1.0 + 1.0) % 1.0;
         double var19 = (var10 + 0.035 * Math.sin(var2 * var14 + var15)) * 160.0;
         double var21 = Math.pow(var17, 0.7);
         double var23 = 1.2 + var8.nextDouble() * 1.8;
         Capes.addDot(var1, var19, var17 * 256.0, var23 * 3.0, 16738832, 0.5 * var21);
         Capes.addDot(var1, var19, var17 * 256.0, var23, 16769184, 0.9 * var21);
      }
   }

   static void ice(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      Capes.field(var1, (var0x, var2x, var4x, var5x) -> {
         int var6x = Capes.ramp(var2x, 15399935, 10935542, 5612762, 1921686);
         int var7 = var4x / 26;
         int var8x = var5x / 30;
         double var9 = 1.0E9;
         double var11 = 1.0E9;
         int var13x = 0;

         for (int var14x = -1; var14x <= 1; var14x++) {
            for (int var15x = -1; var15x <= 1; var15x++) {
               int var16x = var7 + var15x;
               int var17 = var8x + var14x;
               double var18x = (var16x + 0.15 + 0.7 * Capes.hash(var16x, var17, 3)) * 26.0;
               double var20 = (var17 + 0.15 + 0.7 * Capes.hash(var16x, var17, 4)) * 30.0;
               double var22 = Math.hypot(var4x - var18x, var5x - var20);
               if (var22 < var9) {
                  var11 = var9;
                  var9 = var22;
                  var13x = var16x * 131 + var17;
               } else if (var22 < var11) {
                  var11 = var22;
               }
            }
         }

         double var25x = 0.86 + 0.26 * Capes.hash(var13x, 1, 9);
         int var26 = Capes.shade(var6x, var25x);
         double var28x = 1.0 - Capes.smooth(0.0, 3.2, var11 - var9);
         var26 = Capes.add(var26, 16777215, var28x * 0.38);
         double var19 = Math.min(var0x, 1.0 - var0x) * 1.6;
         double var21 = Math.min(var2x, 1.0 - var2x);
         double var23x = Capes.smooth(0.45, 0.85, Capes.fbm(var0x * 9.0, var2x * 14.0, 0, 0, 4, 44) + (0.3 - Math.min(var19, var21)) * 1.4);
         return Capes.mix(var26, 16055551, var23x * 0.75);
      });
      double var6 = 80.0;
      double var8 = 112.0;
      double var10 = 44.0;
      java.awt.geom.Path2D.Double var12 = new java.awt.geom.Path2D.Double();

      for (int var13 = 0; var13 < 6; var13++) {
         double var14 = (Math.PI / 3) * var13 - (Math.PI / 2);
         double var16 = Math.cos(var14);
         double var18 = Math.sin(var14);
         var12.moveTo(var6, var8);
         var12.lineTo(var6 + var16 * var10, var8 + var18 * var10);

         for (double var23 : new double[]{0.38, 0.62, 0.82}) {
            double var25 = var10 * (0.34 - (var23 - 0.38) * 0.35);

            for (byte var27 = -1; var27 <= 1; var27 += 2) {
               double var28 = var14 + var27 * Math.PI / 4.0;
               var12.moveTo(var6 + var16 * var10 * var23, var8 + var18 * var10 * var23);
               var12.lineTo(var6 + var16 * var10 * var23 + Math.cos(var28) * var25, var8 + var18 * var10 * var23 + Math.sin(var28) * var25);
            }
         }
      }

      Capes.dot(var0, var6, var8, 64.0, 670330, 0.35);
      var0.setColor(Capes.col(932984, 0.55));
      var0.setStroke(new BasicStroke(7.0F, 1, 1));
      var0.draw(var12);
      Capes.glowStroke(var0, var12, 14546687, 3.4F, 1.8F, 0.95);
      java.awt.geom.Path2D.Double var30 = new java.awt.geom.Path2D.Double();

      for (int var31 = 0; var31 < 6; var31++) {
         double var15 = (Math.PI / 3) * var31 - (Math.PI / 2);
         if (var31 == 0) {
            var30.moveTo(var6 + Math.cos(var15) * 9.0, var8 + Math.sin(var15) * 9.0);
         } else {
            var30.lineTo(var6 + Math.cos(var15) * 9.0, var8 + Math.sin(var15) * 9.0);
         }
      }

      var30.closePath();
      var0.setPaint(new GradientPaint((float)var6 - 9.0F, (float)var8 - 9.0F, Color.WHITE, (float)var6 + 9.0F, (float)var8 + 9.0F, new Color(8374512)));
      var0.fill(var30);
      Capes.sweep(var1, (double)var4 / var5, 0.07, 0.35);
      Random var32 = new Random(21L);

      for (int var33 = 0; var33 < 26; var33++) {
         double var34 = 14.0 + var32.nextDouble() * 132.0;
         double var35 = 14.0 + var32.nextDouble() * 228.0;
         int var36 = 1 + var32.nextInt(2);
         double var37 = Math.pow(Math.max(0.0, Math.sin(var2 * var36 + var32.nextDouble() * 6.28)), 3.0);
         Capes.star(var0, var34, var35, 1.2 + var32.nextDouble() * 1.6, 13628415, var37);
      }
   }

   private static int auroraSky(double var0, double var2, double var4) {
      int var6 = Capes.ramp(var2 / 0.78, 66313, 200746, 665668, 1261644);
      double[] var7 = new double[]{0.34, 0.46, 0.24};
      double[] var8 = new double[]{0.95, 0.7, 0.5};

      for (int var9 = 0; var9 < 3; var9++) {
         double var10 = var7[var9]
            + 0.055 * Math.sin((Math.PI * 2) * var0 + var4 + var9 * 2.1)
            + 0.03 * Math.sin((Math.PI * 4) * var0 - var4 * 2.0 + var9)
            + 0.012 * Math.sin((Math.PI * 10) * var0 + var4 * 3.0 + var9);
         double var12 = var2 - var10;
         double var14 = var12 > 0.0 ? Math.exp(-var12 / 0.016) : Math.exp(var12 / (0.15 + 0.05 * var9));
         if (!(var14 < 0.004)) {
            double var16 = 0.55
               + 0.3 * Math.sin(var0 * Math.PI * 2.0 * 9.0 + 2.4 * Math.sin(var0 * Math.PI * 2.0 * 2.0 + var4 + var9) + var9)
               + 0.15 * Math.sin(var0 * Math.PI * 2.0 * 23.0 - var4 * 2.0 + var9 * 3);
            double var18 = Capes.clamp01(-var12 / 0.24);
            int var20 = Capes.ramp(var18, 8191928, 3074232, 3836927, 11557887);
            var6 = Capes.add(var6, var20, var14 * Capes.clamp01(var16) * var8[var9]);
         }
      }

      return var6;
   }

   private static double mountain(double var0) {
      return 0.735
         + 0.05 * Math.sin(var0 * 7.0 + 1.0)
         + 0.03 * Math.sin(var0 * 17.0 + 2.0)
         + 0.018 * (Capes.vn(var0 * 26.0, 0.5, 0, 0, 3) - 0.5)
         - 0.07 * Math.exp(-Math.pow((var0 - 0.3) / 0.12, 2.0));
   }

   static void aurora(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      Random var6 = new Random(2L);
      double[][] var7 = new double[70][];

      for (int var8 = 0; var8 < var7.length; var8++) {
         var7[var8] = new double[]{var6.nextDouble(), var6.nextDouble() * 0.7, var6.nextDouble(), 1 + var6.nextInt(2), var6.nextDouble() * 6.28};
      }

      Capes.field(var1, (var2x, var4x, var6x, var7x) -> {
         double var8x = var4x;
         boolean var10 = var4x > 0.86;
         if (var10) {
            var8x = 1.72 - var4x + 0.006 * Math.sin(var4x * 160.0 + var2 * 2.0);
         }

         double var11x = mountain(var2x);
         int var19;
         if (var8x > var11x) {
            var19 = Capes.mix(462362, 132362, (var8x - var11x) * 6.0);
            double var14x = Math.exp(-(var8x - var11x) / 0.006);
            var19 = Capes.add(var19, 14545663, var14x * 0.35);
            double var16 = Capes.smooth(0.02, 0.0, var8x - var11x) * Capes.smooth(0.72, 0.68, var11x);
            var19 = Capes.mix(var19, 12112616, var16 * 0.6 * (0.5 + 0.5 * Capes.vn(var2x * 40.0, var8x * 40.0, 0, 0, 5)));
         } else {
            var19 = auroraSky(var2x, var8x, var2);
         }

         if (var10) {
            var19 = Capes.shade(Capes.mix(var19, 664632, 0.25), 0.55);
            var19 = Capes.add(var19, 16777215, Math.exp(-(var4x - 0.86) / 0.004) * 0.12);
         }

         return var19;
      });

      for (double[] var11 : var7) {
         double var12 = var11[1] * 256.0;
         if (!(var11[1] > mountain(var11[0]) - 0.02)) {
            double var14 = (0.4 + 0.6 * (0.5 + 0.5 * Math.sin(var2 * var11[3] + var11[4]))) * (0.4 + 0.6 * var11[2]);
            Capes.star(var0, var11[0] * 160.0, var12, 0.6 + var11[2] * var11[2] * 1.8, 14543103, var14);
         }
      }
   }

   static void sunset(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var2x, var4x, var6x, var7) -> {
         if (var4x < 0.6) {
            int var35 = Capes.ramp(var4x / 0.6, 787234, 3016786, 9051256, 15219324, 16751194);
            double var9x = var6x + 0.5 - 80.0;
            double var11 = var7 + 0.5 - 102.4;
            double var39 = Math.hypot(var9x, var11);
            var35 = Capes.add(var35, 16734874, Math.exp(-Math.max(0.0, var39 - 49.6) / 16.0) * 0.45);
            if (var39 < 49.6) {
               double var40 = (var11 + 49.6) / 99.2;
               int var42 = Capes.ramp(var40, 16775072, 16760906, 16738906, 16723594);
               boolean var18 = false;
               if (var40 > 0.48) {
                  double var45 = ((var40 * 9.0 - var6) % 1.0 + 1.0) % 1.0;
                  var18 = var45 < (var40 - 0.48) * 1.25;
               }

               double var46 = Capes.smooth(49.6, 48.1, var39);
               if (!var18) {
                  var35 = Capes.mix(var35, var42, var46);
               }
            }

            double var41 = 0.6 - (0.035 + 0.11 * Math.pow(Math.abs(var2x - 0.5) * 2.0, 1.6)) * (0.75 + 0.5 * Capes.vn(var2x * 9.0, 1.0, 0, 0, 77));
            if (var4x > var41) {
               int var43 = Capes.mix(3804766, 1311786, (var4x - var41) / 0.1);
               var43 = Capes.add(var43, 16739016, Math.exp(-(var4x - var41) / 0.005) * 0.6);
               var35 = var43;
            }

            return var35;
         } else {
            double var8x = var4x - 0.6 + 0.0015;
            double var10x = 0.07 / var8x;
            int var12x = Capes.mix(2229822, 524562, Capes.smooth(0.0, 0.4, var8x));
            double var13 = ((var10x + var6) % 1.0 + 1.0) % 1.0;
            double var15 = Math.min(var13, 1.0 - var13);
            double var17 = 0.07 / (var8x * var8x) * 0.005078125;
            double var19 = Capes.smooth(var17 * 1.6, var17 * 0.3, var15);
            double var21 = (var2x - 0.5) * var10x * 7.0;
            double var23 = (var21 % 1.0 + 1.0) % 1.0;
            double var25 = Math.min(var23, 1.0 - var23);
            double var27 = var10x * 7.0 * 0.008125;
            double var29 = Capes.smooth(var27 * 1.6, var27 * 0.3, var25);
            double var31 = Capes.smooth(0.005, 0.12, var8x);
            double var33 = Math.max(var19, var29) * var31;
            var12x = Capes.add(var12x, 16726736, var33 * 0.95);
            var12x = Capes.add(var12x, 16777215, var33 * var33 * 0.35);
            return Capes.add(var12x, 16730784, Math.exp(-var8x / 0.03) * 0.5);
         }
      });
      Random var8 = new Random(33L);

      for (int var9 = 0; var9 < 40; var9++) {
         double var10 = var8.nextDouble() * 160.0;
         double var12 = var8.nextDouble() * 256.0 * 0.3;
         double var14 = (0.5 + 0.5 * Math.sin(var2 * (1 + var8.nextInt(2)) + var8.nextDouble() * 6.28)) * (1.0 - var12 / 76.8);
         Capes.star(var0, var10, var12, 0.6 + var8.nextDouble() * 1.4, 16767216, var14);
      }
   }

   static void carbon(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      Capes.field(
         var1,
         (var0x, var2x, var4x, var5x) -> {
            int var6x = var4x / 8;
            int var7x = var5x / 8;
            double var8x = (var4x % 8 + 0.5) / 8.0;
            double var10x = (var5x % 8 + 0.5) / 8.0;
            boolean var12 = (var6x + var7x & 3) < 2;
            double var13 = var12 ? var10x : var8x;
            double var15 = var12 ? var8x : var10x;
            double var17 = Math.pow(Math.sin(Math.PI * var13), 0.6);
            double var19 = 0.93 + 0.07 * Math.sin(var13 * Math.PI * 7.0 + var15 * 0.6);
            double var21 = var12 ? 0.55 + 0.45 * Math.sin(var0x * 5.0 - var2x * 3.0 + 1.0) : 0.55 + 0.45 * Math.sin(var0x * 4.0 + var2x * 2.0 + 3.0);
            int var23 = Capes.mix(789776, 4869720, var17 * var19 * (0.35 + 0.5 * var21));
            double var24 = Math.exp(-Math.pow((var0x * 0.9 - var2x * 0.55 - 0.12) / 0.1, 2.0)) * 0.14
               + Math.exp(-Math.pow((var0x * 0.9 - var2x * 0.55 + 0.35) / 0.2, 2.0)) * 0.08;
            return Capes.add(var23, 16777215, var24);
         }
      );

      for (int var6 = 0; var6 < 2; var6++) {
         double var7 = var6 == 0 ? 40.0 : 212.0;
         var0.setPaint(new GradientPaint(0.0F, (float)var7 - 5.0F, new Color(16726586), 0.0F, (float)var7 + 5.0F, new Color(9046538)));
         var0.fill(new java.awt.geom.Rectangle2D.Double(0.0, var7 - 5.0, 160.0, 10.0));
         var0.setColor(Capes.col(16777215, 0.35));
         var0.fill(new java.awt.geom.Rectangle2D.Double(0.0, var7 - 5.0, 160.0, 1.5));
         var0.setColor(Capes.col(16726586, 0.8));
         var0.fill(new java.awt.geom.Rectangle2D.Double(0.0, var7 + 8.0, 160.0, 2.0));
      }

      double var10 = 80.0;
      double var8 = 124.0;
      Capes.dot(var0, var10, var8, 58.0, 0, 0.55);
      var0.setPaint(
         new LinearGradientPaint(
            (float)var10 - 34.0F,
            (float)var8 - 34.0F,
            (float)var10 + 34.0F,
            (float)var8 + 34.0F,
            new float[]{0.0F, 0.45F, 0.55F, 1.0F},
            new Color[]{new Color(16054010), new Color(10134192), new Color(5923440), new Color(13160152)}
         )
      );
      var0.fill(new Double(var10 - 34.0, var8 - 34.0, 68.0, 68.0));
      var0.setPaint(
         new RadialGradientPaint((float)var10, (float)var8 - 8.0F, 36.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(2763826), new Color(921362)})
      );
      var0.fill(new Double(var10 - 29.0, var8 - 29.0, 58.0, 58.0));
      Art.brick(var0, var10 - 24.0, var8 - 27.0, 48.0, new Color(14876683));
      var0.setColor(Capes.col(16777215, 0.45));
      var0.setStroke(new BasicStroke(1.2F));
      var0.draw(new java.awt.geom.Arc2D.Double(var10 - 32.0, var8 - 32.0, 64.0, 64.0, 60.0, 90.0, 0));
   }

   static void neon(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      double var8 = 0.78 + 0.22 * Math.sin(var2);
      int var10 = Capes.mix(16726736, 2811647, 0.5 + 0.5 * Math.sin(var2));
      Capes.field(var1, (var4x, var6x, var8x, var9) -> {
         if (var6x < 0.64) {
            int var35 = Capes.ramp(var6x / 0.64, 327948, 1049386, 2754632);
            double var11x = var4x * 10.0 % 1.0;
            double var13x = var6x * 16.0 % 1.0;
            double var39 = Math.max(Capes.smooth(0.06, 0.0, Math.min(var11x, 1.0 - var11x)), Capes.smooth(0.06, 0.0, Math.min(var13x, 1.0 - var13x)));
            var35 = Capes.add(var35, 6958048, var39 * 0.12);
            return Capes.add(var35, 16726736, Math.exp(-(0.64 - var6x) / 0.04) * 0.45);
         } else {
            double var10x = var6x - 0.64 + 0.0015;
            double var12 = 0.06 / var10x;
            int var14 = Capes.mix(1311274, 262152, Capes.smooth(0.0, 0.3, var10x));
            double var15x = ((var12 + var6) % 1.0 + 1.0) % 1.0;
            double var17x = Math.min(var15x, 1.0 - var15x);
            double var19x = 0.06 / (var10x * var10x) * 0.00546875;
            double var21x = Capes.smooth(var19x * 1.8, var19x * 0.3, var17x);
            double var23x = (var4x - 0.5) * var12 * 8.0;
            double var25x = (var23x % 1.0 + 1.0) % 1.0;
            double var27x = Math.min(var25x, 1.0 - var25x);
            double var29x = var12 * 8.0 * 0.008749999999999999;
            double var31 = Capes.smooth(var29x * 1.8, var29x * 0.3, var27x);
            double var33 = Math.max(var21x, var31) * Capes.smooth(0.005, 0.1, var10x) * var8;
            var14 = Capes.add(var14, 2811647, var33);
            var14 = Capes.add(var14, 16777215, var33 * var33 * 0.3);
            return Capes.add(var14, 16726736, Math.exp(-var10x / 0.025) * 0.55);
         }
      });
      double var11 = 80.0;
      double var13 = 98.0;
      double var15 = 46.0;
      java.awt.geom.Path2D.Double var17 = new java.awt.geom.Path2D.Double();

      for (int var18 = 0; var18 < 3; var18++) {
         double var19 = (-Math.PI / 2) + var18 * Math.PI * 2.0 / 3.0;
         if (var18 == 0) {
            var17.moveTo(var11 + Math.cos(var19) * var15, var13 + Math.sin(var19) * var15);
         } else {
            var17.lineTo(var11 + Math.cos(var19) * var15, var13 + Math.sin(var19) * var15);
         }
      }

      var17.closePath();
      Capes.addDot(var1, var11, var13 + 4.0, 46.0, var10, 0.35 * var8);
      var0.setPaint(
         new GradientPaint((float)var11, (float)(var13 - var15), Capes.col(2754632, 0.2), (float)var11, (float)(var13 + var15 / 2.0), Capes.col(16726736, 0.28))
      );
      var0.fill(var17);
      Capes.glowStroke(var0, var17, var10, 3.2F, 2.4F, var8);
      java.awt.geom.Path2D.Double var26 = new java.awt.geom.Path2D.Double();

      for (int var27 = 0; var27 < 3; var27++) {
         double var20 = (Math.PI / 2) + var27 * Math.PI * 2.0 / 3.0;
         double var22 = var15 * 0.34;
         if (var27 == 0) {
            var26.moveTo(var11 + Math.cos(var20) * var22, var13 + 2.0 + Math.sin(var20) * var22);
         } else {
            var26.lineTo(var11 + Math.cos(var20) * var22, var13 + 2.0 + Math.sin(var20) * var22);
         }
      }

      var26.closePath();
      Capes.glowStroke(var0, var26, Capes.mix(2811647, 16726736, 0.5 + 0.5 * Math.sin(var2)), 1.6F, 1.4F, 0.8);
      double var28 = var6 * 256.0;
      int[] var21 = Capes.px(var1);

      for (int var29 = Math.max(0, (int)var28 - 6); var29 < Math.min(256, (int)var28 + 6); var29++) {
         double var23 = Math.exp(-Math.pow((var29 - var28) / 2.2, 2.0)) * 0.35;

         for (int var25 = 0; var25 < 160; var25++) {
            var21[var29 * 160 + var25] = Capes.add(var21[var29 * 160 + var25], 12121855, var23);
         }
      }
   }

   static void spotify(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = SpotifyCape.LIVE.get() != null ? 0.7 + 0.3 * SpotifyCape.LIVE.get().beat : 0.7 + 0.3 * Math.pow(Math.max(0.0, Math.cos(var2 * 2.0)), 4.0);
      Capes.field(var1, (var2x, var4x, var6x, var7) -> {
         int var8x = Capes.ramp(var4x, 925461, 1184787, 658186);
         double var9 = Math.hypot(var6x - 80, (var7 - 92) * 0.9);
         var8x = Capes.add(var8x, 2021216, Math.exp(-(var9 / 62.0) * (var9 / 62.0)) * 0.32 * var6);
         double var11 = 0.5 + 0.5 * Math.sin((var2x * 0.8 - var4x * 1.1) * 40.0);
         return Capes.shade(var8x, 0.96 + 0.04 * var11);
      });
      double var8 = 80.0;
      double var10 = 92.0;
      double var12 = 36.0 * (0.97 + 0.03 * var6);
      Capes.dot(var0, var8 + 2.0, var10 + 5.0, var12 * 1.3, 0, 0.55);
      var0.setPaint(
         new RadialGradientPaint(
            (float)(var8 - var12 * 0.4),
            (float)(var10 - var12 * 0.5),
            (float)(var12 * 1.6),
            new float[]{0.0F, 0.6F, 1.0F},
            new Color[]{new Color(6090894), new Color(2021216), new Color(1022530)}
         )
      );
      var0.fill(new Double(var8 - var12, var10 - var12, var12 * 2.0, var12 * 2.0));
      var0.setColor(new Color(789516));
      float[] var14 = new float[]{7.4F, 6.2F, 5.0F};
      double[] var15 = new double[]{-13.0, 1.0, 14.0};
      double[] var16 = new double[]{26.0, 22.0, 17.0};

      for (int var17 = 0; var17 < 3; var17++) {
         var0.setStroke(new BasicStroke(var14[var17] * (float)(var12 / 36.0), 1, 1));
         double var18 = var10 + var15[var17] * var12 / 36.0;
         double var20 = var16[var17] * var12 / 36.0;
         var0.draw(
            new java.awt.geom.QuadCurve2D.Double(var8 - var20, var18, var8 + var20 * 0.1, var18 - 10.0 * var12 / 36.0, var8 + var20, var18 + 4.0 * var12 / 36.0)
         );
      }

      var0.setPaint(new GradientPaint((float)var8, (float)(var10 - var12), Capes.col(16777215, 0.3), (float)var8, (float)var10, Capes.col(16777215, 0.0)));
      var0.fill(new Double(var8 - var12 * 0.8, var10 - var12 * 0.95, var12 * 1.6, var12 * 1.0));
      SpotifyCape.Live var43 = SpotifyCape.LIVE.get();
      byte var44 = 11;
      double var19 = 7.2;
      double var21 = 3.6;
      double var23 = 80.0 - (var44 * var19 + (var44 - 1) * var21) / 2.0;
      double var25 = 214.0;
      Random var27 = new Random(4L);

      for (int var28 = 0; var28 < var44; var28++) {
         int var29 = 1 + var27.nextInt(3);
         int var30 = 1 + var27.nextInt(3);
         double var31 = var27.nextDouble() * 6.28;
         double var33 = var27.nextDouble() * 6.28;
         double var35 = 0.55 + 0.45 * Math.sin(Math.PI * (var28 + 0.5) / var44);
         double var37 = var43 != null
            ? 5.0 + 44.0 * var43.bars[var28]
            : 6.0 + 46.0 * var35 * (0.25 + 0.75 * (0.5 + 0.5 * Math.sin(var2 * var29 + var31)) * (0.55 + 0.45 * (0.5 + 0.5 * Math.sin(var2 * var30 + var33))));
         double var39 = var23 + var28 * (var19 + var21);
         var0.setPaint(new GradientPaint(0.0F, (float)(var25 - var37), new Color(12124112), 0.0F, (float)var25, new Color(1355852)));
         var0.fill(new java.awt.geom.RoundRectangle2D.Double(var39, var25 - var37, var19, var37, 4.0, 4.0));
         if (var43 != null && var43.peaks[var28] > 0.0) {
            double var41 = var25 - 5.0 - 44.0 * var43.peaks[var28] - 4.0;
            var0.setColor(Capes.col(15269872, 0.9));
            var0.fill(new java.awt.geom.RoundRectangle2D.Double(var39, var41, var19, 2.2, 2.0, 2.0));
         }

         var0.setPaint(new GradientPaint(0.0F, (float)var25 + 2.0F, Capes.col(2021216, 0.3), 0.0F, (float)(var25 + 2.0 + var37 * 0.4), Capes.col(2021216, 0.0)));
         var0.fill(new java.awt.geom.RoundRectangle2D.Double(var39, var25 + 2.0, var19, var37 * 0.4, 4.0, 4.0));
      }

      if (var43 != null) {
         var0.setFont(bold(10.5F));
         FontMetrics var45 = var0.getFontMetrics();
         String var47 = fit(var43.title, var45, 140);
         var0.setColor(Capes.col(16777215, 0.95));
         var0.drawString(var47, (float)(80.0 - var45.stringWidth(var47) / 2.0), 140.0F);
         var0.setFont(bold(8.0F));
         var45 = var0.getFontMetrics();
         String var48 = fit(var43.artist, var45, 140);
         var0.setColor(Capes.col(10155452, 0.85));
         var0.drawString(var48, (float)(80.0 - var45.stringWidth(var48) / 2.0), 151.0F);
         double var49 = 22.0;
         double var50 = 116.0;
         double var51 = 232.0;
         var0.setColor(Capes.col(16777215, 0.18));
         var0.fill(new java.awt.geom.RoundRectangle2D.Double(var49, var51, var50, 3.0, 3.0, 3.0));
         var0.setColor(new Color(2021216));
         var0.fill(new java.awt.geom.RoundRectangle2D.Double(var49, var51, var50 * var43.progress, 3.0, 3.0, 3.0));
         Capes.dot(var0, var49 + var50 * var43.progress, var51 + 1.5, 3.2, 16777215, 0.95);
         if (!var43.playing) {
            var0.setColor(Capes.col(16777215, 0.8));
            var0.fill(new java.awt.geom.RoundRectangle2D.Double(71.0, 180.0, 6.0, 18.0, 2.0, 2.0));
            var0.fill(new java.awt.geom.RoundRectangle2D.Double(83.0, 180.0, 6.0, 18.0, 2.0, 2.0));
         }
      }
   }

   private static String fit(String var0, FontMetrics var1, int var2) {
      if (var0 == null) {
         return "";
      } else if (var1.stringWidth(var0) <= var2) {
         return var0;
      } else {
         while (var0.length() > 1 && var1.stringWidth(var0 + "…") > var2) {
            var0 = var0.substring(0, var0.length() - 1);
         }

         return var0 + "…";
      }
   }

   private static Path2D petal(double var0, double var2) {
      java.awt.geom.Path2D.Double var4 = new java.awt.geom.Path2D.Double();
      var4.moveTo(0.0, var0 * 0.5);
      var4.curveTo(var2 * 0.9, var0 * 0.2, var2 * 0.7, -var0 * 0.45, var2 * 0.18, -var0 * 0.5);
      var4.lineTo(0.0, -var0 * 0.36);
      var4.lineTo(-var2 * 0.18, -var0 * 0.5);
      var4.curveTo(-var2 * 0.7, -var0 * 0.45, -var2 * 0.9, var0 * 0.2, 0.0, var0 * 0.5);
      var4.closePath();
      return var4;
   }

   static void blossom(Graphics2D var0, double var1, double var3, double var5, double var7, int var9) {
      AffineTransform var10 = var0.getTransform();
      var0.translate(var1, var3);
      var0.rotate(var7);

      for (int var11 = 0; var11 < 5; var11++) {
         AffineTransform var12 = var0.getTransform();
         var0.rotate((Math.PI * 2) * var11 / 5.0);
         var0.translate(0.0, -var5 * 0.55);
         Path2D var13 = petal(var5 * 1.05, var5 * 0.95);
         var0.setPaint(new GradientPaint(0.0F, (float)(var5 * 0.5), new Color(var9), 0.0F, (float)(-var5 * 0.5), new Color(16774906)));
         var0.fill(var13);
         var0.setColor(Capes.col(12599402, 0.35));
         var0.setStroke(new BasicStroke(0.6F));
         var0.draw(var13);
         var0.setTransform(var12);
      }

      var0.setColor(new Color(14696558));
      var0.fill(new Double(-var5 * 0.25, -var5 * 0.25, var5 * 0.5, var5 * 0.5));
      var0.setColor(new Color(16769658));

      for (int var14 = 0; var14 < 6; var14++) {
         double var15 = (Math.PI * 2) * var14 / 6.0;
         var0.fill(new Double(Math.cos(var15) * var5 * 0.36 - 0.8, Math.sin(var15) * var5 * 0.36 - 0.8, 1.6, 1.6));
      }

      var0.setTransform(var10);
   }

   static void sakura(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var0x, var2x, var4x, var5x) -> {
         int var6x = Capes.ramp(var2x, 16773622, 16767463, 16366803, 15442628);
         var6x = Capes.add(var6x, 16777215, Math.exp(-Math.hypot(var0x - 0.78, var2x - 0.18) / 0.12) * 0.35);
         double var7 = 0.64 + 0.42 * Math.pow(Math.abs(var0x - 0.56), 1.25) + 0.012 * (Capes.vn(var0x * 18.0, 3.0, 0, 0, 51) - 0.5);
         if (var2x > var7) {
            int var9x = Capes.mix(13017816, 15320284, (var2x - var7) * 2.5);
            double var10 = 0.69 + 0.02 * Math.sin(var0x * 60.0) * Capes.vn(var0x * 30.0, 1.0, 0, 0, 5);
            if (var2x < var10) {
               var9x = Capes.mix(16777215, 14470127, (var2x - var7) / 0.06);
            }

            var6x = Capes.mix(var6x, var9x, Capes.smooth(0.0, 0.004, var2x - var7) * 0.85);
         }

         return Capes.mix(var6x, 16774392, Capes.smooth(0.78, 1.0, var2x) * 0.55);
      });
      java.awt.geom.Path2D.Double var8 = new java.awt.geom.Path2D.Double();
      var8.moveTo(-10.0, 22.0);
      var8.curveTo(40.0, 30.0, 70.0, 58.0, 128.0, 64.0);
      var0.setStroke(new BasicStroke(8.0F, 1, 1));
      var0.setColor(new Color(3808806));
      var0.draw(var8);
      var0.setStroke(new BasicStroke(2.2F, 1, 1));
      var0.setColor(Capes.col(9067104, 0.8));
      var0.draw(new java.awt.geom.QuadCurve2D.Double(-10.0, 19.0, 40.0, 24.0, 124.0, 61.0));
      double[][] var9 = new double[][]{
         {30.0, 30.0, 44.0, 8.0}, {64.0, 48.0, 58.0, 86.0}, {94.0, 58.0, 120.0, 36.0}, {108.0, 62.0, 132.0, 90.0}, {48.0, 38.0, 20.0, 62.0}
      };
      var0.setColor(new Color(3808806));
      var0.setStroke(new BasicStroke(3.2F, 1, 1));

      for (double[] var13 : var9) {
         var0.draw(
            new java.awt.geom.QuadCurve2D.Double(var13[0], var13[1], (var13[0] + var13[2]) / 2.0 + 4.0, (var13[1] + var13[3]) / 2.0 - 4.0, var13[2], var13[3])
         );
      }

      Random var28 = new Random(12L);
      double[][] var29 = new double[][]{
         {44.0, 8.0}, {58.0, 86.0}, {120.0, 36.0}, {132.0, 90.0}, {20.0, 62.0}, {84.0, 58.0}, {12.0, 26.0}, {100.0, 70.0}, {70.0, 44.0}
      };

      for (double[] var15 : var29) {
         for (int var16 = 0; var16 < 3; var16++) {
            double var17 = 6.0 + var28.nextDouble() * 4.0;
            blossom(
               var0,
               var15[0] + var28.nextGaussian() * 7.0,
               var15[1] + var28.nextGaussian() * 6.0,
               var17,
               var28.nextDouble() * 6.28,
               var28.nextBoolean() ? 16752320 : 16758994
            );
         }
      }

      for (int var31 = 0; var31 < 30; var31++) {
         double var33 = var28.nextDouble();
         double var34 = var28.nextDouble();
         double var35 = var28.nextDouble() * 6.28;
         int var19 = var28.nextBoolean() ? 1 : -1;
         double var20 = (var34 + var6) % 1.0 * 286.0 - 15.0;
         double var22 = (var33 + var6) % 1.0 * 190.0 - 15.0 + Math.sin(var2 + var35) * 8.0;
         double var24 = 4.0 + var28.nextDouble() * 3.5;
         AffineTransform var26 = var0.getTransform();
         var0.translate(var22, var20);
         var0.rotate(var35 + var2 * var19);
         var0.scale(0.35 + 0.65 * Math.abs(Math.cos(var2 + var35)), 1.0);
         Path2D var27 = petal(var24 * 1.3, var24);
         var0.setPaint(new GradientPaint(0.0F, (float)var24, new Color(16748468), 0.0F, (float)(-var24), new Color(16773366)));
         var0.fill(var27);
         var0.setTransform(var26);
      }
   }

   static void lava(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var4x, var6x, var8x, var9x) -> {
         double var10x = var9x - var6 * 32.0;
         int var12x = Math.floorDiv(var8x, 32);
         int var13 = (int)Math.floor(var10x / 32.0);
         double var14x = 1.0E9;
         double var16x = 1.0E9;
         int var18 = 0;

         for (int var19 = -1; var19 <= 1; var19++) {
            for (int var20 = -1; var20 <= 1; var20++) {
               int var21 = var12x + var20;
               int var22 = var13 + var19;
               int var23 = Math.floorMod(var21, 5);
               int var24 = Math.floorMod(var22, 8);
               double var25 = (var21 + 0.15 + 0.7 * Capes.hash(var23, var24, 61)) * 32.0;
               double var27 = (var22 + 0.15 + 0.7 * Capes.hash(var23, var24, 62)) * 32.0;
               double var29 = var8x - var25;
               double var31 = var10x - var27;
               double var33 = Math.sqrt(var29 * var29 + var31 * var31 * 1.2);
               if (var33 < var14x) {
                  var16x = var14x;
                  var14x = var33;
                  var18 = var23 * 17 + var24;
               } else if (var33 < var16x) {
                  var16x = var33;
               }
            }
         }

         double var35 = var16x - var14x;
         double var36 = Capes.fbm(var4x * 5.0, var6x * 8.0 - var6 * 8.0, 0, 8, 3, 63);
         double var37 = 0.72 + 0.28 * Math.sin(var2 + Capes.hash(var18, 2, 64) * Math.PI * 2.0);
         double var38 = Capes.fbm(var4x * 14.0, var10x / 256.0 * 22.0, 0, 0, 4, 65);
         int var39 = Capes.mix(4074020, 1050888, var38 * 0.8 + Capes.hash(var18, 5, 66) * 0.3);
         var39 = Capes.shade(var39, 0.75 + 0.35 * Capes.smooth(0.0, 16.0, var35));
         double var28 = Math.exp(-var35 / 3.2) * (0.6 + 0.6 * var36) * var37;
         double var30 = Math.exp(-var35 / 12.0) * 0.35 * var37;
         int var32 = Capes.add(var39, 16726538, var30);
         int var41 = Capes.ramp(var28, 0, 10097152, 16730624, 16752672, 16773280);
         return Capes.mix(var32, var41, Capes.smooth(0.15, 0.55, var28));
      });
      Random var8 = new Random(18L);

      for (int var9 = 0; var9 < 22; var9++) {
         double var10 = var8.nextDouble();
         double var12 = var8.nextDouble();
         double var14 = ((var12 - var6) % 1.0 + 1.0) % 1.0;
         double var16 = (var10 + 0.03 * Math.sin(var2 + var9)) * 160.0;
         Capes.addDot(var1, var16, var14 * 256.0, 3.5, 16742938, 0.5 * var14);
         Capes.addDot(var1, var16, var14 * 256.0, 1.2, 16771232, 0.8 * var14);
      }
   }

   static void ocean(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      double var8 = Math.cos(var2);
      double var10 = Math.sin(var2);
      Capes.field(
         var1,
         (var2x, var4x, var6x, var7) -> {
            int var8x = Capes.ramp(var4x, 5954806, 1413839, 678558, 338014, 135734);
            double var9 = var2x + var4x * 0.28;
            double var11 = 0.0;
            double[] var13x = new double[]{0.12, 0.3, 0.52, 0.7, 0.9};

            for (int var14x = 0; var14x < var13x.length; var14x++) {
               double var15 = (var9 - var13x[var14x] - 0.02 * Math.sin(var2 + var14x)) / (0.035 + 0.02 * (var14x % 2));
               var11 += Math.exp(-var15 * var15) * (0.6 + 0.4 * Math.sin(var2 * (1 + var14x % 2) + var14x * 1.7));
            }

            var8x = Capes.add(var8x, 13630463, var11 * 0.28 * Math.exp(-var4x * 2.4));
            double var29x = var2x * 16.0;
            double var16x = var4x * 22.0;
            double var18x = Math.sin(var29x + 1.4 * Math.sin(var16x * 0.7 + var2))
               + Math.sin(var16x + 1.4 * Math.sin(var29x * 0.8 - var2))
               + Math.sin((var29x + var16x) * 0.55 + var2);
            double var20x = Math.pow(Capes.clamp01(var18x / 3.0), 3.0) * 2.2;
            var8x = Capes.add(var8x, 14745599, var20x * 0.32 * Math.pow(1.0 - var4x, 2.2));
            double var22x = 0.055 + 0.01 * Math.sin(var2x * Math.PI * 6.0 + var2) + 0.005 * Math.sin(var2x * Math.PI * 14.0 - var2 * 2.0);
            if (var4x < var22x) {
               var8x = Capes.mix(var8x, 14220031, 0.55 + 0.45 * Capes.smooth(var22x, var22x - 0.03, var4x));
            }

            var8x = Capes.add(var8x, 16777215, Math.exp(-Math.abs(var4x - var22x) / 0.005) * 0.5);
            double var24 = 0.93 + 0.02 * Math.sin(var2x * 9.0 + 1.0);
            if (var4x > var24) {
               var8x = Capes.mix(var8x, Capes.mix(9075282, 3814434, (var4x - var24) * 10.0), Capes.smooth(var24, var24 + 0.01, var4x) * 0.8);
            }

            return var8x;
         }
      );
      Random var12 = new Random(44L);

      for (int var13 = 0; var13 < 6; var13++) {
         double var14 = 10 + var13 * 28 + var12.nextDouble() * 10.0;
         double var16 = 60.0 + var12.nextDouble() * 70.0;
         double var18 = var12.nextDouble() * 6.28;
         java.awt.geom.Path2D.Double var20 = new java.awt.geom.Path2D.Double();
         var20.moveTo(var14, 256.0);

         for (int var21 = 1; var21 <= 10; var21++) {
            double var22 = 256.0 - var16 * var21 / 10.0;
            var20.lineTo(var14 + Math.sin(var2 + var18 + var21 * 0.5) * var21 * 0.9, var22);
         }

         var0.setColor(Capes.col(Capes.mix(936506, 2787930, var12.nextDouble()), 0.85));
         var0.setStroke(new BasicStroke(3.2F, 1, 1));
         var0.draw(var20);
      }

      for (int var25 = 0; var25 < 8; var25++) {
         double var27 = var12.nextDouble();
         double var29 = 90.0 + var12.nextDouble() * 70.0;
         double var31 = (var27 + var6) % 1.0 * 200.0 - 20.0;
         double var33 = var29 + Math.sin(var2 * 2.0 + var25) * 2.0;
         AffineTransform var35 = var0.getTransform();
         var0.translate(var31, var33);
         var0.setColor(Capes.col(405594, 0.6));
         var0.fill(new Double(-7.0, -2.6, 12.0, 5.2));
         java.awt.geom.Path2D.Double var23 = new java.awt.geom.Path2D.Double();
         var23.moveTo(-6.0, 0.0);
         var23.lineTo(-11.0, -3.5 + Math.sin(var2 * 4.0 + var25));
         var23.lineTo(-11.0, 3.5 + Math.sin(var2 * 4.0 + var25));
         var23.closePath();
         var0.fill(var23);
         var0.setTransform(var35);
      }

      for (int var26 = 0; var26 < 26; var26++) {
         double var28 = var12.nextDouble() * 160.0;
         double var30 = var12.nextDouble();
         int var32 = 1 + var12.nextInt(2);
         double var19 = 1.4 + Math.pow(var12.nextDouble(), 2.0) * 4.0;
         double var34 = ((var30 - var32 * var6) % 1.0 + 1.0) % 1.0 * 256.0;
         double var36 = var28 + Math.sin(var2 * var32 * 2.0 + var26) * 3.0;
         var0.setColor(Capes.col(15269887, 0.55));
         var0.setStroke(new BasicStroke(0.9F));
         var0.draw(new Double(var36 - var19, var34 - var19, var19 * 2.0, var19 * 2.0));
         var0.setColor(Capes.col(16777215, 0.14));
         var0.fill(new Double(var36 - var19, var34 - var19, var19 * 2.0, var19 * 2.0));
         var0.setColor(Capes.col(16777215, 0.85));
         var0.fill(new Double(var36 - var19 * 0.55, var34 - var19 * 0.6, var19 * 0.5, var19 * 0.45));
      }
   }

   static void matrix(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      Capes.field(var1, (var0x, var2x, var4x, var5x) -> {
         int var6x = Capes.ramp(var2x, 67587, 134405, 66818);
         double var7x = Math.hypot(var0x - 0.5, var2x - 0.45);
         return Capes.add(var6x, 678426, Math.exp(-var7x * var7x / 0.08) * 0.25);
      });
      byte var6 = 10;
      byte var7 = 16;
      byte var8 = 16;
      int[] var9 = Capes.px(var1);

      for (int var10 = 0; var10 < var6; var10++) {
         for (int var11 = 0; var11 < 2; var11++) {
            int var12 = Capes.hash(var10, var11, 91) < 0.4 ? 2 : 1;
            int var13 = (int)(Capes.hash(var10, var11, 92) * var7) + var11 * var7 / 2;
            int var14 = 5 + (int)(Capes.hash(var10, var11, 93) * 9.0);
            int var15 = Math.floorMod(var13 + var12 * var4, var7);

            for (int var16 = 0; var16 < var7; var16++) {
               int var17 = Math.floorMod(var15 - var16, var7);
               if (var17 < var14) {
                  double var18 = var17 == 0 ? 1.0 : Math.pow(1.0 - (double)var17 / var14, 1.4) * 0.85;
                  int var20 = Capes.hash(var10, var16, 94) < 0.2 ? var4 / 4 : 0;
                  int var21 = (int)(Capes.hash(var10 * 31 + var20, var16, 95) * 1000000.0);
                  int var22 = var17 == 0 ? 15400938 : Capes.mix(409626, 3997560, var18);

                  for (int var23 = 0; var23 < 7; var23++) {
                     for (int var24 = 0; var24 < 5; var24++) {
                        int var25 = var24 < 3 ? var21 >> var23 * 3 + var24 & 1 : var21 >> var23 * 3 + (4 - var24) & 1;
                        if (var24 == 2 && var23 % 3 == 0) {
                           var25 = 1;
                        }

                        if ((var21 >> 21 & 1) == 1 && var24 >= 3) {
                           var25 = var21 >> var23 + 22 + var24 & 1;
                        }

                        if (var25 != 0) {
                           for (int var26 = 0; var26 < 2; var26++) {
                              for (int var27 = 0; var27 < 2; var27++) {
                                 int var28 = var10 * var8 + 3 + var24 * 2 + var27;
                                 int var29 = var16 * var8 + 1 + var23 * 2 + var26;
                                 if (var28 < 160 && var29 < 256) {
                                    var9[var29 * 160 + var28] = Capes.over(var9[var29 * 160 + var28], var22, 0.25 + 0.75 * var18);
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (var17 == 0) {
                     Capes.addDot(var1, var10 * var8 + 8, var16 * var8 + 8, 9.0, 3997560, 0.35);
                  }
               }
            }
         }
      }
   }
}
