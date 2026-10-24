package dev.lego.cosmetic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Path2D.Double;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

final class Cos3Capes {
   private Cos3Capes() {
   }

   static void defineAll() {
      Capes.def("c3void", 16, 8.0F, 14203135, 2756168, 459534, Cos3Capes::voidCloak);
      Capes.def("c3nether", 16, 8.0F, 16751178, 4853256, 1705220, Cos3Capes::nether);
      Capes.def("c3heaven", 16, 8.0F, 16777215, 13146666, 16774360, Cos3Capes::heaven);
      Capes.def("c3sunflower", 12, 6.0F, 16769658, 6982186, 2775706, Cos3Capes::sunflower);
      Capes.def("c3pumpkin", 16, 10.0F, 16756810, 3807752, 1313310, Cos3Capes::pumpkin);
      Capes.def("c3ghost", 16, 8.0F, 13172724, 1722962, 529438, Cos3Capes::ghost);
      Capes.def("c3skull", 16, 8.0F, 13172656, 1714712, 395270, Cos3Capes::skull);
      Capes.def("c3bolt_purple", 16, 10.0F, 3811914, 920084, 460044, (var0, var1, var2, var4, var5) -> neonBolt(var0, var1, var2, var4, var5, 12606207));
      Capes.def("c3bolt_blue", 16, 10.0F, 2767438, 658966, 329484, (var0, var1, var2, var4, var5) -> neonBolt(var0, var1, var2, var4, var5, 3844351));
      Capes.def("c3bolt_green", 16, 10.0F, 2771506, 660494, 330759, (var0, var1, var2, var4, var5) -> neonBolt(var0, var1, var2, var4, var5, 3866490));
      Capes.def("c3bolt_orange", 16, 10.0F, 5126698, 1445898, 788485, (var0, var1, var2, var4, var5) -> neonBolt(var0, var1, var2, var4, var5, 16747050));
      Capes.def("c3embertree", 16, 8.0F, 16758890, 3807754, 1181702, Cos3Capes::emberTree);
   }

   static void voidCloak(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var4x, var6x, var8x, var9x) -> {
         double var10x = var8x - 80.0;
         double var12x = (var9x - 118.0) * 0.82;
         double var14x = Math.hypot(var10x, var12x) + 0.001;
         double var16x = Math.atan2(var12x, var10x) / (Math.PI * 2) + 0.5;
         double var18x = var16x * 6.0 + Math.log(var14x) * 2.6 - var6 * 6.0;
         double var20x = Capes.fbm(var18x, Math.log(var14x) * 2.2, 6, 0, 4, 71);
         double var22x = 0.5 + 0.5 * Math.sin((var16x * 3.0 + Math.log(var14x) * 0.9 - var6) * Math.PI * 2.0 + (var20x - 0.5) * 3.0);
         double var24x = Math.exp(-var14x / 20.0);
         double var26x = Math.pow(var22x, 2.2) * (0.35 + 0.9 * var20x) * Math.exp(-var14x / 110.0);
         int var28 = Capes.ramp(var6x, 656404, 459534, 262664);
         var28 = Capes.add(var28, 6953672, var26x * 0.9);
         var28 = Capes.add(var28, 13794047, Math.pow(var26x, 2.4) * 1.2);
         double var29 = Math.exp(-Math.pow((var14x - 15.0) / 3.2, 2.0));
         if (var14x < 15.0) {
            var28 = Capes.mix(var28, 0, Capes.smooth(15.0, 9.0, var14x));
         }

         var28 = Capes.add(var28, 15780095, var29 * (0.55 + 0.25 * Math.sin(var2 * 2.0 + var16x * 12.0)));
         var28 = Capes.add(var28, 3803754, var24x * 0.2);
         if (Capes.hash(var8x, var9x, 72) > 0.993) {
            var28 = Capes.add(var28, 16777215, 0.3 + 0.5 * Capes.hash(var8x, var9x, 73));
         }

         return var28;
      });
      Random var8 = new Random(77L);

      for (int var9 = 0; var9 < 40; var9++) {
         double var10 = 20.0 + var8.nextDouble() * 70.0;
         double var12 = var8.nextDouble() * Math.PI * 2.0;
         double var14 = var12 + var2 * (var8.nextBoolean() ? 1 : 2) * (40.0 / var10 > 1.0 ? 1 : 1);
         double var16 = 80.0 + Math.cos(var14) * var10;
         double var18 = 118.0 + Math.sin(var14) * var10 * 1.2;
         Capes.dot(var0, var16, var18, 1.6 + var8.nextDouble() * 1.8, 14723327, 0.55 + 0.35 * Math.sin(var2 + var9));
      }

      var0.setStroke(new BasicStroke(1.6F, 1, 1));

      for (int var20 = 0; var20 < 2; var20++) {
         for (int var22 = 0; var22 < 7; var22++) {
            double var11 = var20 == 0 ? 22.0 : 138.0;
            double var13 = 30 + var22 * 32;
            double var15 = Math.pow(0.5 + 0.5 * Math.cos(var2 - (var22 + var20 * 3.5) * 0.9), 3.0);
            rune(var0, var11, var13, 7.0, (var22 * 3 + var20 * 5) % 8, 13138687, 0.35 + 0.65 * var15);
            if (var15 > 0.05) {
               Capes.addDot(var1, var11, var13, 12.0, 10502399, 0.35 * var15);
            }
         }
      }

      for (int var21 = 0; var21 < 5; var21++) {
         double var23 = Math.PI * (1.15 + var21 * 0.175);
         double var24 = 80.0 + Math.cos(var23) * 54.0;
         double var25 = 118.0 + Math.sin(var23) * 60.0;
         double var26 = Math.pow(0.5 + 0.5 * Math.cos(var2 * 2.0 - var21 * 1.2), 2.0);
         rune(var0, var24, var25, 6.0, (var21 + 2) % 8, 15782143, 0.4 + 0.6 * var26);
      }
   }

   static void rune(Graphics2D var0, double var1, double var3, double var5, int var7, int var8, double var9) {
      Double var11 = new Double();
      switch (var7 % 8) {
         case 0:
            var11.moveTo(-var5, var5);
            var11.lineTo(0.0, -var5);
            var11.lineTo(var5, var5);
            var11.closePath();
            var11.append(new java.awt.geom.Ellipse2D.Double(-var5 * 0.22, var5 * 0.1, var5 * 0.44, var5 * 0.44), false);
            break;
         case 1:
            var11.moveTo(0.0, -var5);
            var11.lineTo(0.0, var5);
            var11.moveTo(-var5, -var5 * 0.4);
            var11.lineTo(0.0, 0.0);
            var11.lineTo(var5, -var5 * 0.4);
            break;
         case 2:
            var11.append(new java.awt.geom.Ellipse2D.Double(-var5 * 0.6, -var5 * 0.6, var5 * 1.2, var5 * 1.2), false);
            var11.moveTo(0.0, -var5);
            var11.lineTo(0.0, var5);
            break;
         case 3:
            var11.moveTo(-var5, -var5);
            var11.lineTo(var5, -var5);
            var11.lineTo(-var5, var5);
            var11.lineTo(var5, var5);
            var11.closePath();
            var11.moveTo(-var5 * 1.2, 0.0);
            var11.lineTo(var5 * 1.2, 0.0);
            break;
         case 4:
            var11.moveTo(0.0, -var5);
            var11.lineTo(var5, 0.0);
            var11.lineTo(0.0, var5);
            var11.lineTo(-var5, 0.0);
            var11.closePath();
            var11.moveTo(0.0, -var5 * 0.3);
            var11.lineTo(0.0, var5 * 0.3);
            break;
         case 5:
            var11.moveTo(-var5, var5);
            var11.lineTo(-var5, -var5);
            var11.lineTo(var5 * 0.6, -var5 * 0.2);
            var11.moveTo(-var5, 0.0);
            var11.lineTo(var5, var5);
            break;
         case 6:
            var11.moveTo(-var5, -var5 * 0.2);
            var11.quadTo(0.0, -var5 * 1.6, var5, -var5 * 0.2);
            var11.moveTo(0.0, -var5 * 0.8);
            var11.lineTo(0.0, var5);
            var11.moveTo(-var5 * 0.6, var5);
            var11.lineTo(var5 * 0.6, var5);
            break;
         default:
            var11.moveTo(-var5, 0.0);
            var11.lineTo(var5, 0.0);
            var11.moveTo(-var5 * 0.5, -var5);
            var11.lineTo(var5 * 0.5, var5);
            var11.moveTo(var5 * 0.5, -var5);
            var11.lineTo(-var5 * 0.5, var5);
      }

      Shape var12 = AffineTransform.getTranslateInstance(var1, var3).createTransformedShape(var11);
      Capes.glowStroke(var0, var12, var8, 1.5F, 1.2F, var9);
   }

   static void nether(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var4x, var6x, var8x, var9x) -> {
         double var10x = Capes.fbm(var4x * 3.0, var6x * 5.0 - var6 * 5.0, 3, 5, 3, 81);
         double var12x = Capes.fbm(var4x * 4.0 + (var10x - 0.5) * 1.2, var6x * 6.0 - var6 * 6.0, 4, 6, 5, 82);
         double var14x = 1.0 - Math.abs(var12x - 0.5) * 2.0;
         double var16x = Math.pow(Capes.smooth(0.9, 0.99, var14x), 1.5);
         double var18x = Capes.fbm(var4x * 10.0, var6x * 16.0, 10, 16, 3, 83);
         int var20x = Capes.ramp(var18x, 1312262, 2755594, 4068368, 4856854);
         var20x = Capes.shade(var20x, 0.7 + 0.5 * Capes.smooth(0.3, 0.9, var14x));
         double var21x = 0.8 + 0.2 * Math.sin(var2 + var6x * 6.0);
         var20x = Capes.add(var20x, 16726538, var16x * 1.1 * var21x);
         var20x = Capes.add(var20x, 16760906, Math.pow(var16x, 3.0) * 0.9 * var21x);
         return Capes.add(var20x, 16722442, Capes.smooth(0.55, 1.0, var6x) * 0.25);
      });
      Random var8 = new Random(84L);

      for (int var9 = 0; var9 < 7; var9++) {
         double var10 = 18 + var9 * 21 + var8.nextDouble() * 8.0;
         double var12 = 26.0 + var8.nextDouble() * 34.0;
         double var14 = 7.0 + var8.nextDouble() * 5.0;
         double var16 = (var8.nextDouble() - 0.5) * 0.5;
         Double var18 = new Double();
         var18.moveTo(var10 - var14 / 2.0, 250.0);
         var18.lineTo(var10 - var14 / 2.0 + var16 * var12 * 0.6, 250.0 - var12 * 0.75);
         var18.lineTo(var10 + var16 * var12, 250.0 - var12);
         var18.lineTo(var10 + var14 / 2.0 + var16 * var12 * 0.6, 250.0 - var12 * 0.75);
         var18.lineTo(var10 + var14 / 2.0, 250.0);
         var18.closePath();
         Capes.addDot(var1, var10 + var16 * var12 * 0.5, 250.0 - var12 * 0.5, var12 * 0.7, 16722506, 0.35 + 0.15 * Math.sin(var2 + var9));
         var0.setPaint(new GradientPaint((float)var10, (float)(256.0 - var12), new Color(16738938), (float)(var10 + var14), 256.0F, new Color(9046558)));
         var0.fill(var18);
         var0.setColor(Capes.col(16765144, 0.7));
         var0.setStroke(new BasicStroke(1.2F));
         var0.draw(new java.awt.geom.Line2D.Double(var10, 250.0, var10 + var16 * var12, 250.0 - var12));
      }

      for (int var23 = 0; var23 < 60; var23++) {
         double var24 = var8.nextDouble() * 160.0;
         double var25 = var8.nextDouble();
         double var26 = 1 + var8.nextInt(2);
         double var27 = 256.0 - (var25 + var26 * var6) % 1.0 * 276.0 + 10.0;
         double var28 = var24 + Math.sin(var2 * var26 + var23) * 6.0;
         boolean var20 = var23 % 3 != 0;
         double var21 = (var25 + var26 * var6) % 1.0;
         if (var20) {
            Capes.dot(var0, var28, var27, 2.2 + var8.nextDouble() * 1.5, 16747050, 0.9 * (1.0 - var21 * 0.7));
         } else {
            Capes.dot(var0, var28, var27, 1.6, 9079434, 0.5);
         }
      }
   }

   static void heaven(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var4x, var6x, var8x, var9x) -> {
         int var10x = Capes.ramp(var6x, 16773836, 16044154, 14459450, 11038746);
         double var11x = Capes.fbm(var4x * 4.0 + var6 * 4.0, var6x * 3.0, 4, 0, 4, 91);
         var10x = Capes.mix(var10x, 16775392, Capes.smooth(0.5, 0.8, var11x) * 0.45 * (1.0 - var6x));
         double var13x = Math.atan2(var9x + 40, var8x - 80);
         double var15x = Math.pow(0.5 + 0.5 * Math.sin(var13x * 22.0 + var2), 6.0) * Math.exp(-var6x * 2.2);
         return Capes.add(var10x, 16774864, var15x * 0.35);
      });
      Random var8 = new Random(92L);

      for (int var9 = 0; var9 < 5; var9++) {
         double var10 = 20 + var9 * 30 + var8.nextDouble() * 10.0;
         double var12 = 0.0;
         Double var14 = new Double();
         var14.moveTo(var10, var12);
         double var15 = var8.nextBoolean() ? 1.0 : -1.0;

         while (var12 < 256.0) {
            var10 += (var8.nextDouble() - 0.5) * 16.0 + var15 * 2.0;
            if (var10 < 14.0 || var10 > 146.0) {
               var15 = -var15;
            }

            var12 += 10.0 + var8.nextDouble() * 12.0;
            var14.lineTo(var10, var12);
            if (var8.nextDouble() < 0.3) {
               Double var17 = new Double();
               var17.moveTo(var10, var12);
               double var18 = var10;
               double var20 = var12;

               for (int var22 = 0; var22 < 3; var22++) {
                  var18 += (var8.nextBoolean() ? 1 : -1) * (5.0 + var8.nextDouble() * 8.0);
                  var20 += 7.0 + var8.nextDouble() * 8.0;
                  var17.lineTo(var18, var20);
               }

               Capes.glowStroke(var0, var17, 16762954, 0.9F, 1.0F, 0.6);
            }
         }

         Capes.glowStroke(var0, var14, 16758826, 1.4F, 1.3F, 0.75);

         for (int var37 = 0; var37 < 2; var37++) {
            double var38 = (var6 + var37 * 0.5 + var9 * 0.21) % 1.0;
            PathIterator var40 = var14.getPathIterator(null, 1.0);
            double[] var21 = new double[6];
            double var41 = var38 * 256.0;
            double var24 = 0.0;
            double var26 = 0.0;

            while (!var40.isDone()) {
               var40.currentSegment(var21);
               if (var21[1] >= var41) {
                  double var28 = var26 == var21[1] ? 0.0 : (var41 - var26) / (var21[1] - var26);
                  double var30 = var24 + (var21[0] - var24) * var28;
                  Capes.addDot(var1, var30, var41, 10.0, 16777215, 0.8);
                  Capes.addDot(var1, var30, var41, 22.0, 16765024, 0.35);
                  break;
               }

               var24 = var21[0];
               var26 = var21[1];
               var40.next();
            }
         }
      }

      double var32 = 80.0;
      double var11 = 96.0;
      Capes.addDot(var1, var32, var11, 40.0, 16769162, 0.5 + 0.15 * Math.sin(var2));
      var0.setColor(Capes.col(16777215, 0.95));
      var0.setStroke(new BasicStroke(2.2F));
      var0.draw(new java.awt.geom.Ellipse2D.Double(var32 - 18.0, var11 - 18.0, 36.0, 36.0));

      for (int var13 = 0; var13 < 12; var13++) {
         double var34 = var13 * Math.PI / 6.0 + var2 / 6.0;
         double var16 = 22.0;
         double var39 = var13 % 2 == 0 ? 36.0 : 29.0;
         Capes.glowStroke(
            var0,
            new java.awt.geom.Line2D.Double(
               var32 + Math.cos(var34) * var16, var11 + Math.sin(var34) * var16, var32 + Math.cos(var34) * var39, var11 + Math.sin(var34) * var39
            ),
            16762954,
            1.8F,
            1.2F,
            0.9
         );
      }

      var0.setPaint(new RadialGradientPaint((float)var32, (float)var11, 13.0F, new float[]{0.0F, 1.0F}, new Color[]{Color.WHITE, new Color(16767082)}));
      var0.fill(new java.awt.geom.Ellipse2D.Double(var32 - 12.0, var11 - 12.0, 24.0, 24.0));

      for (int var33 = 0; var33 < 30; var33++) {
         double var35 = 12.0 + var8.nextDouble() * 136.0;
         double var36 = 12.0 + var8.nextDouble() * 232.0;
         Capes.star(var0, var35, var36, 1.0 + var8.nextDouble() * 1.6, 16771232, Math.pow(Math.max(0.0, Math.sin(var2 + var8.nextDouble() * 6.28)), 2.0));
      }
   }

   static void sunflower(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      Capes.field(var1, (var2x, var4x, var6x, var7) -> {
         int var8x = Capes.ramp(var4x, 3832536, 6990576, 12116223, 15267583);
         double var9x = Capes.fbm(var2x * 3.0 + var2 / (Math.PI * 2) * 3.0, var4x * 5.0, 3, 0, 4, 101);
         var8x = Capes.mix(var8x, 16777215, Capes.smooth(0.55, 0.75, var9x) * (var4x < 0.5 ? 0.8 : 0.3));
         double var11x = 0.78 + 0.03 * Math.sin(var2x * 7.0 + 1.0);
         if (var4x > var11x) {
            var8x = Capes.mix(5941306, 2779678, (var4x - var11x) * 4.0 + 0.15 * Capes.hash(var6x / 3, var7 / 3, 102));
         }

         return var8x;
      });
      double var6 = Math.sin(var2) * 0.06;
      Double var8 = new Double();
      var8.moveTo(78.0, 256.0);
      var8.curveTo(74.0, 200.0, 90.0 + var6 * 60.0, 160.0, 80.0 + var6 * 90.0, 108.0);
      var0.setColor(new Color(2783778));
      var0.setStroke(new BasicStroke(7.0F, 1, 1));
      var0.draw(var8);
      var0.setColor(new Color(5945402));
      var0.setStroke(new BasicStroke(2.0F, 1, 1));
      var0.draw(var8);

      for (byte var9 = -1; var9 <= 1; var9 += 2) {
         AffineTransform var10 = var0.getTransform();
         var0.translate(80 + var9 * 2, 190 - var9 * 14);
         var0.rotate(var9 * (0.7 + var6 * 2.0));
         Double var11 = new Double();
         var11.moveTo(0.0, 0.0);
         var11.curveTo(var9 * 14, -16.0, var9 * 34, -10.0, var9 * 42, 0.0);
         var11.curveTo(var9 * 30, 10.0, var9 * 12, 12.0, 0.0, 0.0);
         var0.setPaint(new GradientPaint(0.0F, -10.0F, new Color(6998090), 0.0F, 12.0F, new Color(2783778)));
         var0.fill(var11);
         var0.setColor(Capes.col(1726994, 0.8));
         var0.setStroke(new BasicStroke(1.2F));
         var0.draw(new java.awt.geom.Line2D.Double(0.0, 0.0, var9 * 38, 0.0));
         var0.setTransform(var10);
      }

      double var25 = 80.0 + var6 * 90.0;
      double var26 = 96.0;
      Capes.addDot(var1, var25, var26, 70.0, 16769658, 0.35);
      AffineTransform var13 = var0.getTransform();
      var0.translate(var25, var26);
      var0.rotate(var6 * 1.5);

      for (int var14 = 0; var14 < 2; var14++) {
         byte var15 = 18;

         for (int var16 = 0; var16 < var15; var16++) {
            double var17 = (var16 + var14 * 0.5) * Math.PI * 2.0 / var15;
            AffineTransform var19 = var0.getTransform();
            var0.rotate(var17);
            double var20 = var14 == 0 ? 50.0 : 42.0;
            double var22 = var14 == 0 ? 11.0 : 10.0;
            Double var24 = new Double();
            var24.moveTo(18.0, 0.0);
            var24.curveTo(26.0, -var22, var20 - 6.0, -var22 * 0.7, var20, 0.0);
            var24.curveTo(var20 - 6.0, var22 * 0.7, 26.0, var22, 18.0, 0.0);
            var0.setPaint(
               new GradientPaint(18.0F, 0.0F, new Color(var14 == 0 ? 15243786 : 16036378), (float)var20, 0.0F, new Color(var14 == 0 ? 16765498 : 16770154))
            );
            var0.fill(var24);
            var0.setColor(Capes.col(12085760, 0.5));
            var0.setStroke(new BasicStroke(0.9F));
            var0.draw(new java.awt.geom.Line2D.Double(22.0, 0.0, var20 - 6.0, 0.0));
            var0.setTransform(var19);
         }
      }

      var0.setPaint(
         new RadialGradientPaint(0.0F, 0.0F, 22.0F, new float[]{0.0F, 0.7F, 1.0F}, new Color[]{new Color(3808778), new Color(5910542), new Color(8014354)})
      );
      var0.fill(new java.awt.geom.Ellipse2D.Double(-22.0, -22.0, 44.0, 44.0));

      for (int var27 = 0; var27 < 150; var27++) {
         double var29 = Math.sqrt(var27) * 1.75;
         double var33 = var27 * 2.39996;
         if (var29 > 20.0) {
            break;
         }

         var0.setColor(new Color(Capes.mix(13144106, 2757638, var29 / 22.0)));
         var0.fill(new java.awt.geom.Ellipse2D.Double(Math.cos(var33) * var29 - 0.9, Math.sin(var33) * var29 - 0.9, 1.8, 1.8));
      }

      var0.setTransform(var13);
      Random var28 = new Random(103L);

      for (int var30 = 0; var30 < 18; var30++) {
         double var32 = var28.nextDouble() * 6.28 + var2;
         double var18 = 60.0 + var28.nextDouble() * 20.0;
         Capes.dot(var0, var25 + Math.cos(var32) * var18 * 0.8, var26 + Math.sin(var32) * var18, 1.8, 16774304, 0.8);
      }

      double var31 = var25 + Math.cos(var2) * 52.0;
      double var34 = var26 + 40.0 + Math.sin(var2 * 2.0) * 14.0;
      var0.setColor(new Color(16765498));
      var0.fill(new java.awt.geom.Ellipse2D.Double(var31 - 6.0, var34 - 4.0, 12.0, 8.0));
      var0.setColor(new Color(2759178));
      var0.fill(new java.awt.geom.Rectangle2D.Double(var31 - 2.0, var34 - 4.0, 2.0, 8.0));
      var0.fill(new java.awt.geom.Rectangle2D.Double(var31 + 2.0, var34 - 4.0, 2.0, 8.0));
      var0.setColor(Capes.col(16777215, 0.8));
      var0.fill(new java.awt.geom.Ellipse2D.Double(var31 - 4.0, var34 - 10.0, 6.0, 6.0));
      var0.fill(new java.awt.geom.Ellipse2D.Double(var31, var34 - 10.0, 6.0, 6.0));
   }

   static void pumpkin(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      double var8 = 0.8 + 0.12 * Math.sin(var2 * 3.0) + 0.08 * Math.sin(var2 * 7.0 + 1.0);
      Capes.field(var1, (var4x, var6x, var8x, var9) -> {
         int var10x = Capes.ramp(var6x, 1706542, 2757184, 3807792, 1706510);
         double var11x = Capes.fbm(var4x * 3.0 + var6 * 3.0, var6x * 2.0, 3, 0, 4, 111);
         var10x = Capes.add(var10x, 6961802, Capes.smooth(0.5, 0.8, var11x) * 0.25 * var6x);
         var10x = Capes.add(var10x, 16742938, Math.exp(-Math.hypot(var8x - 80, var9 - 150) / 50.0) * 0.35 * var8);
         if (Capes.hash(var8x, var9, 112) > 0.994 && var6x < 0.5) {
            var10x = Capes.add(var10x, 16777215, 0.5);
         }

         return var10x;
      });
      var0.setPaint(new RadialGradientPaint(124.0F, 38.0F, 16.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(16774872), new Color(15784080)}));
      var0.fill(new java.awt.geom.Ellipse2D.Double(110.0, 24.0, 28.0, 28.0));

      for (int var10 = 0; var10 < 3; var10++) {
         double var11 = (var6 + var10 / 3.0) % 1.0;
         double var13 = -20.0 + var11 * 200.0;
         double var15 = 40 + var10 * 22 + Math.sin(var11 * Math.PI * 4.0) * 6.0;
         bat(var0, var13, var15, 6 + var10, Math.sin(var2 * 4.0 + var10) > 0.0);
      }

      double var24 = 80.0;
      double var12 = 150.0;

      for (int var14 = -2; var14 <= 2; var14++) {
         double var26 = 34 - Math.abs(var14) * 6;
         var0.setPaint(
            new GradientPaint(
               (float)(var24 + var14 * 16 - var26),
               (float)(var12 - 40.0),
               new Color(Math.abs(var14) == 2 ? 13127690 : 16747034),
               (float)(var24 + var14 * 16 + var26),
               (float)(var12 + 40.0),
               new Color(11026442)
            )
         );
         var0.fill(new java.awt.geom.Ellipse2D.Double(var24 + var14 * 15 - var26, var12 - 42.0, var26 * 2.0, 84.0));
      }

      var0.setColor(new Color(4876826));
      var0.fill(new java.awt.geom.RoundRectangle2D.Double(var24 - 5.0, var12 - 56.0, 10.0, 18.0, 4.0, 4.0));
      Double var25 = new Double();
      var25.append(new Double(tri(var24 - 30.0, var12 - 6.0, var24 - 10.0, var12 - 6.0, var24 - 18.0, var12 - 26.0)), false);
      var25.append(new Double(tri(var24 + 30.0, var12 - 6.0, var24 + 10.0, var12 - 6.0, var24 + 18.0, var12 - 26.0)), false);
      var25.append(new Double(tri(var24 - 5.0, var12 + 6.0, var24 + 5.0, var12 + 6.0, var24, var12 - 3.0)), false);
      Double var27 = new Double();
      var27.moveTo(var24 - 34.0, var12 + 12.0);

      for (int var16 = 0; var16 <= 8; var16++) {
         var27.lineTo(var24 - 34.0 + var16 * 8.5, var12 + 14.0 + (var16 % 2 == 0 ? 0 : 7) + Math.sin(var16 * 0.4) * 2.0);
      }

      var27.quadTo(var24, var12 + 44.0, var24 - 34.0, var12 + 12.0);
      var25.append(var27, false);
      Capes.addDot(var1, var24, var12 + 6.0, 60.0, 16751146, 0.35 * var8);
      var0.setColor(new Color(Capes.mix(5904896, 16769146, var8)));
      var0.fill(var25);
      var0.setPaint(
         new RadialGradientPaint(
            (float)var24, (float)(var12 + 4.0), 30.0F, new float[]{0.0F, 1.0F}, new Color[]{Capes.col(16777184, var8), Capes.col(16756778, 0.0)}
         )
      );
      var0.fill(var25);
      Capes.glowStroke(var0, var25, 16756794, 0.8F, 1.4F, 0.8 * var8);
      Random var28 = new Random(113L);

      for (int var17 = 0; var17 < 20; var17++) {
         double var18 = var28.nextDouble() * 160.0;
         double var20 = var28.nextDouble();
         double var22 = 256.0 - (var20 + var6) % 1.0 * 256.0;
         Capes.dot(var0, var18 + Math.sin(var2 + var17) * 5.0, var22, 2.0, 16756810, 0.7);
      }
   }

   private static Path2D tri(double var0, double var2, double var4, double var6, double var8, double var10) {
      Double var12 = new Double();
      var12.moveTo(var0, var2);
      var12.lineTo(var4, var6);
      var12.lineTo(var8, var10);
      var12.closePath();
      return var12;
   }

   private static void bat(Graphics2D var0, double var1, double var3, double var5, boolean var7) {
      Double var8 = new Double();
      double var9 = var7 ? -var5 * 0.9 : var5 * 0.4;
      var8.moveTo(var1, var3 - var5 * 0.3);
      var8.quadTo(var1 - var5, var3 + var9 - var5 * 0.2, var1 - var5 * 2.0, var3 + var9);
      var8.quadTo(var1 - var5 * 1.4, var3 + var5 * 0.2, var1 - var5 * 0.9, var3 + var5 * 0.5);
      var8.quadTo(var1 - var5 * 0.5, var3 + var5 * 0.2, var1, var3 + var5 * 0.6);
      var8.quadTo(var1 + var5 * 0.5, var3 + var5 * 0.2, var1 + var5 * 0.9, var3 + var5 * 0.5);
      var8.quadTo(var1 + var5 * 1.4, var3 + var5 * 0.2, var1 + var5 * 2.0, var3 + var9);
      var8.quadTo(var1 + var5, var3 + var9 - var5 * 0.2, var1, var3 - var5 * 0.3);
      var0.setColor(new Color(656400));
      var0.fill(var8);
   }

   static void ghost(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var2x, var4x, var6x, var7) -> {
         int var8x = Capes.ramp(var4x, 662058, 1058874, 926256, 529440);
         double var9 = Capes.fbm(var2x * 3.0 - var6 * 3.0, var4x * 4.0, 3, 0, 4, 121);
         var8x = Capes.add(var8x, 2787978, Capes.smooth(0.55, 0.85, var9) * 0.22);
         if (Capes.hash(var6x, var7, 122) > 0.993) {
            var8x = Capes.add(var8x, 13172735, 0.5);
         }

         return var8x;
      });
      double var8 = 80.0;
      double var10 = 112.0 + Math.sin(var2) * 8.0;
      Capes.addDot(var1, var8, var10, 70.0, 5963744, 0.3);
      java.awt.geom.Ellipse2D.Double var12 = new java.awt.geom.Ellipse2D.Double(var8 - 56.0, var10 - 56.0, 112.0, 112.0);
      Capes.glowStroke(var0, var12, 7012328, 2.4F, 2.2F, 0.9);

      for (int var13 = 0; var13 < 10; var13++) {
         double var14 = var2 + var13 * Math.PI / 5.0;
         double var16 = 56.0;
         Capes.dot(var0, var8 + Math.cos(var14) * var16, var10 + Math.sin(var14) * var16, 4.0, 14745599, 0.9);
      }

      Double var30 = new Double();
      double var31 = 34.0;
      double var32 = var10 - 44.0;
      double var18 = var10 + 36.0;
      var30.moveTo(var8 - var31, var18);
      var30.lineTo(var8 - var31, var10 - 10.0);
      var30.curveTo(var8 - var31, var32 - 4.0, var8 + var31, var32 - 4.0, var8 + var31, var10 - 10.0);
      var30.lineTo(var8 + var31, var18);
      byte var20 = 4;

      for (int var21 = 0; var21 < var20; var21++) {
         double var22 = var8 + var31 - var21 * (2.0 * var31 / var20);
         double var24 = var22 - var31 / var20;
         double var26 = var22 - 2.0 * var31 / var20;
         double var28 = Math.sin(var2 * 2.0 + var21 * 1.6) * 4.0;
         var30.quadTo(var24, var18 + 12.0 + var28, var26, var18);
      }

      var30.closePath();
      var0.setColor(Capes.col(3866592, 0.25));
      var0.setStroke(new BasicStroke(8.0F));
      var0.draw(var30);
      var0.setPaint(new GradientPaint((float)var8, (float)var32, new Color(16777215), (float)var8, (float)var18, new Color(13166832)));
      var0.fill(var30);
      double var33 = var4 % 8 == 5 ? 0.15 : 1.0;
      var0.setColor(new Color(1712176));
      var0.fill(new java.awt.geom.Ellipse2D.Double(var8 - 17.0, var10 - 16.0, 11.0, 14.0 * var33));
      var0.fill(new java.awt.geom.Ellipse2D.Double(var8 + 6.0, var10 - 16.0, 11.0, 14.0 * var33));
      var0.setColor(Color.WHITE);
      if (var33 > 0.5) {
         var0.fill(new java.awt.geom.Ellipse2D.Double(var8 - 14.0, var10 - 14.0, 4.0, 4.0));
         var0.fill(new java.awt.geom.Ellipse2D.Double(var8 + 9.0, var10 - 14.0, 4.0, 4.0));
      }

      var0.setColor(Capes.col(16747176, 0.7));
      var0.fill(new java.awt.geom.Ellipse2D.Double(var8 - 25.0, var10 + 1.0, 10.0, 6.0));
      var0.fill(new java.awt.geom.Ellipse2D.Double(var8 + 15.0, var10 + 1.0, 10.0, 6.0));
      var0.setColor(new Color(1712176));
      var0.fill(new java.awt.geom.Ellipse2D.Double(var8 - 5.0, var10 + 2.0, 10.0, 8.0 + Math.sin(var2 * 2.0) * 2.0));
      var0.setColor(new Color(15398650));
      var0.fill(new java.awt.geom.Ellipse2D.Double(var8 - var31 - 8.0, var10 + 4.0 + Math.sin(var2 * 2.0) * 3.0, 14.0, 9.0));
      var0.fill(new java.awt.geom.Ellipse2D.Double(var8 + var31 - 6.0, var10 + 4.0 - Math.sin(var2 * 2.0) * 3.0, 14.0, 9.0));
   }

   static void skull(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = 0.75 + 0.25 * Math.sin(var2);
      Capes.field(var1, (var2x, var4x, var6x, var7) -> {
         int var8x = Capes.ramp(var4x, 461319, 658954, 395270);
         double var9 = Math.hypot(var6x - 80, (var7 - 108) * 0.9);
         var8x = Capes.add(var8x, 3866442, Math.exp(-var9 / 46.0) * 0.3 * var6);
         double var11 = var6x % 16 != 0 && var7 % 16 != 0 ? 0.0 : 0.05;
         return Capes.add(var8x, 3866442, var11);
      });
      double var8 = 80.0;
      double var10 = 104.0;
      Double var12 = new Double();
      var12.moveTo(var8 - 34.0, var10 + 6.0);
      var12.curveTo(var8 - 40.0, var10 - 50.0, var8 + 40.0, var10 - 50.0, var8 + 34.0, var10 + 6.0);
      var12.curveTo(var8 + 34.0, var10 + 20.0, var8 + 22.0, var10 + 22.0, var8 + 20.0, var10 + 34.0);
      var12.lineTo(var8 - 20.0, var10 + 34.0);
      var12.curveTo(var8 - 22.0, var10 + 22.0, var8 - 34.0, var10 + 20.0, var8 - 34.0, var10 + 6.0);
      var12.closePath();
      var0.setPaint(new GradientPaint((float)var8, (float)(var10 - 40.0), new Color(16056304), (float)var8, (float)(var10 + 34.0), new Color(12110000)));
      var0.fill(var12);
      Capes.glowStroke(var0, var12, 7012202, 1.6F, 2.0F, 0.9 * var6);
      var0.setColor(new Color(14214352));
      var0.fill(new java.awt.geom.RoundRectangle2D.Double(var8 - 18.0, var10 + 32.0, 36.0, 14.0, 8.0, 8.0));
      var0.setColor(new Color(1054222));

      for (int var13 = 0; var13 < 5; var13++) {
         var0.fill(new java.awt.geom.Rectangle2D.Double(var8 - 15.0 + var13 * 7, var10 + 33.0, 1.4, 11.0));
      }

      var0.fill(new java.awt.geom.Rectangle2D.Double(var8 - 17.0, var10 + 38.0, 34.0, 1.2));

      for (byte var25 = -1; var25 <= 1; var25 += 2) {
         double var14 = var8 + var25 * 14;
         double var16 = var10 + 2.0;
         var0.setColor(new Color(659466));
         var0.fill(new java.awt.geom.Ellipse2D.Double(var14 - 10.0, var16 - 9.0, 20.0, 18.0));
         double var18 = 0.7 + 0.3 * Math.sin(var2 * 3.0 + var25);
         Capes.addDot(var1, var14, var16, 16.0, 4915034, 0.9 * var18);
         Capes.dot(var0, var14, var16, 5.0, 15400938, var18);
      }

      Path2D var26 = tri(var8 - 5.0, var10 + 22.0, var8 + 5.0, var10 + 22.0, var8, var10 + 12.0);
      var0.setColor(new Color(659466));
      var0.fill(var26);
      var0.setColor(Capes.col(5925462, 0.9));
      var0.setStroke(new BasicStroke(1.2F));
      Double var27 = new Double();
      var27.moveTo(var8 + 8.0, var10 - 38.0);
      var27.lineTo(var8 + 12.0, var10 - 28.0);
      var27.lineTo(var8 + 6.0, var10 - 22.0);
      var27.lineTo(var8 + 10.0, var10 - 14.0);
      var0.draw(var27);

      for (byte var15 = -1; var15 <= 1; var15 += 2) {
         AffineTransform var29 = var0.getTransform();
         var0.translate(var8, var10 + 82.0);
         var0.rotate(var15 * 0.6);
         var0.setColor(new Color(14214352));
         var0.fill(new java.awt.geom.RoundRectangle2D.Double(-40.0, -4.0, 80.0, 8.0, 6.0, 6.0));

         for (byte var17 = -1; var17 <= 1; var17 += 2) {
            var0.fill(new java.awt.geom.Ellipse2D.Double(var17 * 40 - 7, -9.0, 10.0, 10.0));
            var0.fill(new java.awt.geom.Ellipse2D.Double(var17 * 40 - 7, -1.0, 10.0, 10.0));
         }

         var0.setTransform(var29);
      }

      Random var28 = new Random(131L);
      double var30 = (double)var4 / var5;

      for (int var31 = 0; var31 < 26; var31++) {
         double var19 = var28.nextDouble() * 160.0;
         double var21 = var28.nextDouble();
         double var23 = 256.0 - (var21 + var30) % 1.0 * 256.0;
         Capes.dot(var0, var19 + Math.sin(var2 + var31) * 4.0, var23, 2.2, 7012202, 0.6);
      }
   }

   static void neonBolt(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5, int var6) {
      double var7 = 0.7 + 0.3 * Math.sin(var2);
      boolean var9 = var4 == 5 || var4 == 11;
      Capes.field(var1, (var3, var5x, var7x, var8) -> {
         int var9x = Capes.ramp(var5x, 789522, 592142, 394761);
         double var10x = (var7x / 4 + var8 / 4) % 2 == 0 ? 1.06 : 0.94;
         var9x = Capes.shade(var9x, var10x);
         double var12 = Math.hypot(var7x - 80, var8 - 118);
         return Capes.add(var9x, var6, Math.exp(-var12 / 60.0) * 0.18 * var7);
      });
      java.awt.geom.RoundRectangle2D.Double var10 = new java.awt.geom.RoundRectangle2D.Double(13.0, 13.0, 134.0, 230.0, 10.0, 10.0);
      Capes.glowStroke(var0, var10, var6, 1.6F, 1.6F, 0.9 * var7);
      double var11 = 728.0;
      double var13 = var2 / (Math.PI * 2) * var11;

      for (int var15 = 0; var15 < 2; var15++) {
         double var16 = (var13 + var15 * var11 / 2.0) % var11;
         double var22 = 134.0;
         double var24 = 230.0;
         double var18;
         double var20;
         if (var16 < var22) {
            var18 = 13.0 + var16;
            var20 = 13.0;
         } else if (var16 < var22 + var24) {
            var18 = 13.0 + var22;
            var20 = 13.0 + var16 - var22;
         } else if (var16 < 2.0 * var22 + var24) {
            var18 = 13.0 + var22 - (var16 - var22 - var24);
            var20 = 13.0 + var24;
         } else {
            var18 = 13.0;
            var20 = 13.0 + var24 - (var16 - 2.0 * var22 - var24);
         }

         Capes.addDot(var1, var18, var20, 12.0, var6, 0.9);
         Capes.addDot(var1, var18, var20, 4.0, 16777215, 0.9);
      }

      Double var26 = new Double();
      var26.moveTo(96.0, 34.0);
      var26.lineTo(46.0, 132.0);
      var26.lineTo(76.0, 132.0);
      var26.lineTo(58.0, 222.0);
      var26.lineTo(116.0, 108.0);
      var26.lineTo(84.0, 108.0);
      var26.lineTo(108.0, 34.0);
      var26.closePath();
      double var27 = var9 ? 0.45 : var7;
      Capes.addDot(var1, 80.0, 128.0, 70.0, var6, 0.35 * var27);
      var0.setColor(new Color(Capes.mix(657936, var6, 0.25)));
      var0.fill(var26);
      Capes.glowStroke(var0, var26, var6, 2.4F, 2.6F, var27);
      var0.setPaint(new GradientPaint(60.0F, 40.0F, Capes.col(16777215, 0.25 * var27), 100.0F, 220.0F, Capes.col(var6, 0.05)));
      var0.fill(var26);
      Random var28 = new Random(141L);

      for (int var19 = 0; var19 < 18; var19++) {
         double var29 = 30.0 + var28.nextDouble() * 100.0;
         double var30 = 30.0 + var28.nextDouble() * 200.0;
         double var31 = Math.pow(Math.max(0.0, Math.sin(var2 * 2.0 + var28.nextDouble() * 6.28)), 4.0);
         Capes.star(var0, var29, var30, 1.2 + var28.nextDouble(), var6, var31);
      }
   }

   static void emberTree(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var0x, var2x, var4x, var5x) -> {
         int var6x = Capes.ramp(var2x, 1182228, 1969682, 2757136, 1444360);
         var6x = Capes.add(var6x, 16734746, Math.exp(-Math.hypot(var4x - 80, var5x - 80) / 60.0) * 0.3);
         double var7 = 0.86 + 0.02 * Math.sin(var0x * 6.0);
         if (var2x > var7) {
            var6x = Capes.mix(1706502, 656387, (var2x - var7) * 5.0);
         }

         return var6x;
      });
      Random var8 = new Random(151L);
      var0.setColor(new Color(656646));
      ArrayList var9 = new ArrayList();
      branch(var0, var8, 80.0, 225.28, -Math.PI / 2, 40.0, 11.0, 0, var9);

      for (int var10 = 0; var10 < var9.size(); var10++) {
         double[] var11 = (double[])var9.get(var10);
         double var12 = 0.6 + 0.4 * Math.sin(var2 + var10 * 0.7);
         Capes.addDot(var1, var11[0], var11[1], 9.0, 16738842, 0.5 * var12);
         Capes.dot(var0, var11[0], var11[1], 3.2, var10 % 3 == 0 ? 16769146 : 16747050, 0.9 * var12);
      }

      for (int var24 = 0; var24 < 46; var24++) {
         double[] var25 = (double[])var9.get(var8.nextInt(var9.size()));
         double var26 = var8.nextDouble();
         double var14 = 1 + var8.nextInt(2);
         double var16 = (var26 + var6 * var14) % 1.0;
         double var18 = var25[0] + Math.sin(var16 * 6.0 + var24) * 8.0 + var16 * 10.0 * (var8.nextBoolean() ? 1 : -1);
         double var20 = var25[1] + var16 * (230.4 - var25[1]);
         double var22 = Math.sin(Math.PI * var16);
         Capes.dot(var0, var18, var20, 2.2, 16752704, 0.9 * var22);
         Capes.dot(var0, var18, var20, 0.9, 16777184, var22);
      }

      Capes.addDot(var1, 80.0, 225.28, 40.0, 16734746, 0.3 + 0.1 * Math.sin(var2));
   }

   private static void branch(Graphics2D var0, Random var1, double var2, double var4, double var6, double var8, double var10, int var12, List<double[]> var13) {
      double var14 = var2 + Math.cos(var6) * var8;
      double var16 = var4 + Math.sin(var6) * var8;
      var0.setStroke(new BasicStroke((float)var10, 1, 1));
      Double var18 = new Double();
      var18.moveTo(var2, var4);
      var18.quadTo((var2 + var14) / 2.0 + (var1.nextDouble() - 0.5) * var8 * 0.3, (var4 + var16) / 2.0, var14, var16);
      var0.setColor(new Color(656646));
      var0.draw(var18);
      if (var12 < 3) {
         var0.setColor(Capes.col(16738842, 0.25));
         var0.setStroke(new BasicStroke((float)Math.max(0.6, var10 * 0.2)));
         var0.draw(AffineTransform.getTranslateInstance(var10 * 0.25, 0.0).createTransformedShape(var18));
      }

      if (var12 < 5 && !(var8 < 6.0)) {
         int var19 = var12 == 0 ? 3 : 2;

         for (int var20 = 0; var20 < var19; var20++) {
            double var21 = (var20 - (var19 - 1) / 2.0) * (0.55 + var1.nextDouble() * 0.3) + (var1.nextDouble() - 0.5) * 0.3;
            branch(var0, var1, var14, var16, var6 + var21, var8 * (0.68 + var1.nextDouble() * 0.12), var10 * 0.66, var12 + 1, var13);
         }

         if (var12 >= 3) {
            var13.add(new double[]{var14, var16});
         }
      } else {
         var13.add(new double[]{var14, var16});
      }
   }
}
