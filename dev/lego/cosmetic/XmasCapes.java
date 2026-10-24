package dev.lego.cosmetic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

final class XmasCapes {
   private static final String[] DEER = new String[]{
      "..W.W...W.W", "...W.....W.", "...WW...WW.", ".....WWW...", ".....WWWK..", "WWWWWWWW...", "WWWWWWWW...", "W.W...W.W..", "W.W...W.W.."
   };
   private static final String[] TREE = new String[]{
      "....Y....", "...GGG...", "..GLGGG..", "...GGG...", "..GGGLG..", ".GLGGGGG.", "..GGGGG..", ".GGGGLGG.", "GLGGGGGLG", "....B...."
   };
   private static final String[] FLAKE = new String[]{"..W.W..", "W..W..W", ".W.W.W.", "WWWWWWW", ".W.W.W.", "W..W..W", "..W.W.."};

   private XmasCapes() {
   }

   static void defineAll() {
      Capes.def("xm_winterwald", 16, 8.0F, 15267071, 2771578, 924208, XmasCapes::winterForest);
      Capes.def("xm_lebkuchenhaus", 16, 8.0F, 16179392, 8010260, 2759216, XmasCapes::gingerHouse);
      Capes.def("xm_zuckerstange", 16, 10.0F, 16777215, 13112350, 16052460, XmasCapes::candyCane);
      Capes.def("xm_schlittenmond", 16, 8.0F, 16771496, 1714778, 659492, XmasCapes::sleighMoon);
      Capes.def("xm_pullover", 8, 4.0F, 16777215, 1993268, 9048088, XmasCapes::sweater);
      Capes.def("xm_nordlicht", 16, 8.0F, 12124128, 1718874, 397342, XmasCapes::auroraXmas);
   }

   static void snow(Graphics2D var0, double var1, double var3, int var5, int var6, double var7) {
      Random var9 = new Random(var6);

      for (int var10 = 0; var10 < var5; var10++) {
         double var11 = var9.nextDouble() * 160.0;
         double var13 = var9.nextDouble();
         double var15 = 1 + var9.nextInt(2);
         double var17 = 0.8 + var9.nextDouble() * 1.8 * (var15 == 2.0 ? 1.3 : 0.8);
         double var19 = (var13 + var15 * var1) % 1.0 * 266.0 - 5.0;
         double var21 = var11 + Math.sin(var3 * var15 + var10 * 1.7) * 5.0;
         Capes.dot(var0, var21, var19, var17 * 1.8, 16777215, var7 * (0.55 + 0.3 * var9.nextDouble()));
         var0.setColor(Capes.col(16777215, var7 * 0.9));
         var0.fill(new Double(var21 - var17 * 0.5, var19 - var17 * 0.5, var17, var17));
      }
   }

   static void stars(Graphics2D var0, double var1, int var3, int var4, double var5) {
      Random var7 = new Random(var4);

      for (int var8 = 0; var8 < var3; var8++) {
         double var9 = var7.nextDouble() * 160.0;
         double var11 = var7.nextDouble() * var5;
         double var13 = 0.7 + var7.nextDouble() * 1.6;
         double var15 = 0.55 + 0.45 * Math.sin(var1 * (1 + var7.nextInt(2)) + var8 * 2.3);
         Capes.star(var0, var9, var11, var13, var7.nextBoolean() ? 16774360 : 14215423, var15);
      }
   }

   static void pine(Graphics2D var0, double var1, double var3, double var5, int var7, int var8, int var9) {
      var0.setColor(new Color(Capes.mix(var7, 0, 0.3)));
      var0.fill(new java.awt.geom.Rectangle2D.Double(var1 - var5 * 0.04, var3 - var5 * 0.12, var5 * 0.08, var5 * 0.14));

      for (int var10 = 0; var10 < var9; var10++) {
         double var11 = (double)var10 / var9;
         double var13 = var3 - var5 * 0.1 - var11 * var5 * 0.78;
         double var15 = var5 * (0.42 - var11 * 0.3);
         double var17 = var5 * 0.4;
         java.awt.geom.Path2D.Double var19 = new java.awt.geom.Path2D.Double();
         var19.moveTo(var1 - var15, var13);
         var19.quadTo(var1 - var15 * 0.5, var13 + var5 * 0.03, var1, var13 + var5 * 0.012);
         var19.quadTo(var1 + var15 * 0.5, var13 + var5 * 0.03, var1 + var15, var13);
         var19.lineTo(var1, var13 - var17);
         var19.closePath();
         var0.setPaint(
            new GradientPaint(
               (float)(var1 - var15), 0.0F, new Color(Capes.mix(var7, 16777215, 0.12)), (float)(var1 + var15), 0.0F, new Color(Capes.mix(var7, 0, 0.25))
            )
         );
         var0.fill(var19);
         java.awt.geom.Path2D.Double var20 = new java.awt.geom.Path2D.Double();
         var20.moveTo(var1 - var15 * 0.82, var13 - var17 * 0.12);
         var20.quadTo(var1 - var15 * 0.45, var13 - var17 * 0.02, var1 - var15 * 0.2, var13 - var17 * 0.2);
         var20.quadTo(var1 + var15 * 0.1, var13 - var17 * 0.05, var1 + var15 * 0.55, var13 - var17 * 0.16);
         var20.lineTo(var1 + var15 * 0.3, var13 - var17 * 0.4);
         var20.lineTo(var1, var13 - var17 * 0.62);
         var20.lineTo(var1 - var15 * 0.35, var13 - var17 * 0.36);
         var20.closePath();
         var0.setColor(Capes.col(var8, 0.92));
         var0.fill(var20);
      }

      var0.setColor(Capes.col(var8, 0.95));
      var0.fill(new Double(var1 - var5 * 0.03, var3 - var5 * 0.92, var5 * 0.06, var5 * 0.05));
   }

   static void winterForest(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var0x, var2x, var4x, var5x) -> {
         int var6x = Capes.ramp(var2x * 1.4, 461858, 1319498, 3033728, 6982336);
         double var7 = Capes.fbm(var0x * 3.0, var2x * 3.0, 3, 3, 4, 701);
         return Capes.add(var6x, 3824288, var7 * 0.18 * (1.0 - var2x));
      });
      stars(var0, var2, 45, 702, 150.0);
      Capes.addDot(var1, 116.0, 52.0, 34.0, 13162751, 0.35);
      var0.setPaint(new RadialGradientPaint(112.0F, 48.0F, 16.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(16776428), new Color(14736584)}));
      var0.fill(new Double(102.0, 38.0, 28.0, 28.0));
      var0.setColor(Capes.col(12104864, 0.5));
      var0.fill(new Double(110.0, 44.0, 6.0, 5.0));
      var0.fill(new Double(119.0, 53.0, 4.0, 4.0));
      java.awt.geom.Path2D.Double var8 = new java.awt.geom.Path2D.Double();
      var8.moveTo(0.0, 170.0);

      for (byte var9 = 0; var9 <= 160; var9 += 8) {
         var8.lineTo(var9, 160.0 - 12.0 * Math.sin(var9 * 0.03 + 1.0) - 6.0 * Math.sin(var9 * 0.08));
      }

      var8.lineTo(160.0, 256.0);
      var8.lineTo(0.0, 256.0);
      var8.closePath();
      var0.setPaint(new GradientPaint(0.0F, 140.0F, new Color(10138842), 0.0F, 200.0F, new Color(5929128)));
      var0.fill(var8);
      Random var24 = new Random(703L);

      for (int var10 = 0; var10 < 11; var10++) {
         pine(var0, 6 + var10 * 15 + var24.nextDouble() * 6.0, 168.0 + var24.nextDouble() * 6.0, 30.0 + var24.nextDouble() * 14.0, 2771562, 12111082, 3);
      }

      double var25 = 58.0;
      double var12 = 196.0;
      var0.setColor(new Color(4860442));
      var0.fill(new java.awt.geom.Rectangle2D.Double(var25 - 16.0, var12 - 16.0, 32.0, 18.0));
      java.awt.geom.Path2D.Double var14 = new java.awt.geom.Path2D.Double();
      var14.moveTo(var25 - 21.0, var12 - 14.0);
      var14.lineTo(var25, var12 - 31.0);
      var14.lineTo(var25 + 21.0, var12 - 14.0);
      var14.closePath();
      var0.setColor(new Color(2759188));
      var0.fill(var14);
      var0.setColor(new Color(16054527));
      var0.fill(new java.awt.geom.RoundRectangle2D.Double(var25 - 22.0, var12 - 19.0, 44.0, 6.0, 5.0, 5.0));
      java.awt.geom.Path2D.Double var15 = new java.awt.geom.Path2D.Double();
      var15.moveTo(var25 - 19.0, var12 - 16.0);
      var15.lineTo(var25, var12 - 32.0);
      var15.lineTo(var25 + 19.0, var12 - 16.0);
      var15.lineTo(var25 + 12.0, var12 - 18.0);
      var15.lineTo(var25, var12 - 27.0);
      var15.lineTo(var25 - 12.0, var12 - 18.0);
      var15.closePath();
      var0.fill(var15);
      double var16 = 0.8 + 0.1 * Math.sin(var2 * 3.0) + 0.06 * Math.sin(var2 * 7.0 + 1.0);

      for (byte var18 = -1; var18 <= 1; var18 += 2) {
         var0.setColor(new Color(Capes.mix(16751146, 16769162, var16)));
         var0.fill(new java.awt.geom.Rectangle2D.Double(var25 + var18 * 8 - 4.0, var12 - 11.0, 8.0, 7.0));
         Capes.addDot(var1, var25 + var18 * 8, var12 - 7.0, 12.0, 16752704, 0.35 * var16);
         var0.setColor(new Color(2759188));
         var0.fill(new java.awt.geom.Rectangle2D.Double(var25 + var18 * 8 - 0.6, var12 - 11.0, 1.2, 7.0));
      }

      var0.setColor(new Color(2759188));
      var0.fill(new java.awt.geom.Rectangle2D.Double(var25 + 8.0, var12 - 32.0, 5.0, 10.0));

      for (int var26 = 0; var26 < 6; var26++) {
         double var19 = (var6 * 2.0 + var26 / 6.0) % 1.0;
         Capes.dot(
            var0,
            var25 + 10.5 + Math.sin(var19 * 6.0 + var26) * 3.0 + var19 * 10.0,
            var12 - 34.0 - var19 * 40.0,
            4.0 + var19 * 7.0,
            13160672,
            0.35 * (1.0 - var19)
         );
      }

      java.awt.geom.Path2D.Double var27 = new java.awt.geom.Path2D.Double();
      var27.moveTo(0.0, 204.0);

      for (byte var28 = 0; var28 <= 160; var28 += 8) {
         var27.lineTo(var28, 200.0 + 4.0 * Math.sin(var28 * 0.05 + 2.0));
      }

      var27.lineTo(160.0, 256.0);
      var27.lineTo(0.0, 256.0);
      var27.closePath();
      var0.setPaint(new GradientPaint(0.0F, 196.0F, new Color(16054527), 0.0F, 256.0F, new Color(12111082)));
      var0.fill(var27);

      for (int var29 = 0; var29 < 30; var29++) {
         double var20 = var24.nextDouble() * 160.0;
         double var22 = 206.0 + var24.nextDouble() * 44.0;
         Capes.star(var0, var20, var22, 0.8 + var24.nextDouble(), 16777215, 0.3 + 0.6 * Math.max(0.0, Math.sin(var2 * 2.0 + var29 * 1.9)));
      }

      pine(var0, 14.0, 238.0, 110.0, 1194532, 16054527, 5);
      pine(var0, 142.0, 232.0, 96.0, 1458730, 16054527, 5);
      pine(var0, 112.0, 214.0, 58.0, 1722928, 15266047, 4);
      snow(var0, var6, var2, 110, 704, 0.9);
   }

   static void gingerHouse(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var0x, var2x, var4x, var5x) -> Capes.ramp(var2x, 1313316, 2759242, 4860506));
      stars(var0, var2, 30, 711, 100.0);
      double var8 = 80.0;
      double var10 = 206.0;
      double var12 = 96.0;
      double var14 = 70.0;
      var0.setColor(new Color(10115626));
      var0.fill(new java.awt.geom.Rectangle2D.Double(var8 + 18.0, var10 - var14 - 58.0, 14.0, 34.0));
      var0.setColor(Color.WHITE);
      var0.fill(new java.awt.geom.RoundRectangle2D.Double(var8 + 16.0, var10 - var14 - 62.0, 18.0, 7.0, 6.0, 6.0));

      for (int var16 = 0; var16 < 6; var16++) {
         double var17 = (var6 * 2.0 + var16 / 6.0) % 1.0;
         Capes.dot(
            var0,
            var8 + 25.0 + Math.sin(var17 * 5.0 + var16) * 4.0 + var17 * 8.0,
            var10 - var14 - 66.0 - var17 * 50.0,
            5.0 + var17 * 8.0,
            14735592,
            0.4 * (1.0 - var17)
         );
      }

      java.awt.geom.Rectangle2D.Double var33 = new java.awt.geom.Rectangle2D.Double(var8 - var12 / 2.0, var10 - var14, var12, var14);
      var0.setPaint(new GradientPaint(0.0F, (float)(var10 - var14), new Color(12876346), 0.0F, (float)var10, new Color(9062942)));
      var0.fill(var33);
      Random var34 = new Random(712L);

      for (int var18 = 0; var18 < 160; var18++) {
         var0.setColor(Capes.col(5909002, 0.35));
         var0.fill(new Double(var8 - var12 / 2.0 + var34.nextDouble() * var12, var10 - var14 + var34.nextDouble() * var14, 1.6, 1.6));
      }

      var0.setColor(Color.WHITE);
      var0.setStroke(new BasicStroke(3.0F, 1, 1));
      var0.draw(new java.awt.geom.Line2D.Double(var8 - var12 / 2.0 + 2.0, var10 - var14 + 4.0, var8 - var12 / 2.0 + 2.0, var10 - 2.0));
      var0.draw(new java.awt.geom.Line2D.Double(var8 + var12 / 2.0 - 2.0, var10 - var14 + 4.0, var8 + var12 / 2.0 - 2.0, var10 - 2.0));
      java.awt.geom.Path2D.Double var35 = new java.awt.geom.Path2D.Double();
      var35.moveTo(var8 - var12 / 2.0 - 12.0, var10 - var14 + 6.0);
      var35.lineTo(var8, var10 - var14 - 56.0);
      var35.lineTo(var8 + var12 / 2.0 + 12.0, var10 - var14 + 6.0);
      var35.closePath();
      var0.setPaint(new GradientPaint(0.0F, (float)(var10 - var14 - 56.0), new Color(11033124), 0.0F, (float)(var10 - var14), new Color(8010260)));
      var0.fill(var35);
      var0.setStroke(new BasicStroke(1.6F, 1, 1));

      for (int var19 = 0; var19 < 5; var19++) {
         double var20 = var10 - var14 - 44.0 + var19 * 11;
         double var22 = (var20 - (var10 - var14 - 56.0)) / 62.0 * (var12 / 2.0 + 12.0);

         for (double var24 = var8 - var22 + 6.0; var24 < var8 + var22 - 4.0; var24 += 10.0) {
            var0.setColor(Capes.col(16774376, 0.85));
            var0.draw(new java.awt.geom.Arc2D.Double(var24 - 5.0, var20 - 5.0, 10.0, 10.0, 180.0, 180.0, 0));
         }
      }

      var0.setColor(Color.WHITE);
      var0.setStroke(new BasicStroke(6.0F, 1, 1));
      java.awt.geom.Path2D.Double var36 = new java.awt.geom.Path2D.Double();
      var36.moveTo(var8 - var12 / 2.0 - 12.0, var10 - var14 + 6.0);
      var36.lineTo(var8, var10 - var14 - 56.0);
      var36.lineTo(var8 + var12 / 2.0 + 12.0, var10 - var14 + 6.0);
      var0.draw(var36);

      for (int var37 = 0; var37 < 12; var37++) {
         double var21 = (var37 + 0.5) / 12.0;
         double var23 = var21 < 0.5 ? -1.0 : 1.0;
         double var25 = var21 < 0.5 ? var21 * 2.0 : (1.0 - var21) * 2.0;
         double var27 = var8 + var23 * (1.0 - var25) * (var12 / 2.0 + 12.0);
         double var29 = var10 - var14 + 6.0 - var25 * 62.0;
         double var31 = 4.0 + 5.0 * Capes.hash1(var37, 713);
         var0.fill(new java.awt.geom.RoundRectangle2D.Double(var27 - 2.0, var29, 4.0, var31, 4.0, 4.0));
         var0.fill(new Double(var27 - 2.5, var29 + var31 - 2.5, 5.0, 5.0));
      }

      int[] var38 = new int[]{15217210, 3061834, 16765498, 3836671, 15227608};

      for (int var39 = 0; var39 < 7; var39++) {
         double var41 = (var39 + 1) / 8.0;
         double var46 = var8 - (var12 / 2.0 + 6.0) + var41 * (var12 + 12.0);
         double var26 = var10 - var14 + 2.0 - (1.0 - Math.abs(var41 - 0.5) * 2.0) * 58.0 - 4.0;
         gumdrop(var0, var46, var26, 4.5, var38[var39 % var38.length]);
      }

      double var40 = 0.8 + 0.12 * Math.sin(var2 * 3.0) + 0.06 * Math.sin(var2 * 5.0 + 1.0);

      for (byte var42 = -1; var42 <= 1; var42 += 2) {
         double var47 = var8 + var42 * 28;
         double var50 = var10 - var14 + 14.0;
         Capes.addDot(var1, var47, var50 + 9.0, 22.0, 16752704, 0.4 * var40);
         var0.setColor(new Color(Capes.mix(16751146, 16771488, var40)));
         var0.fill(new java.awt.geom.RoundRectangle2D.Double(var47 - 10.0, var50, 20.0, 18.0, 4.0, 4.0));
         var0.setColor(Color.WHITE);
         var0.setStroke(new BasicStroke(2.4F));
         var0.draw(new java.awt.geom.RoundRectangle2D.Double(var47 - 10.0, var50, 20.0, 18.0, 4.0, 4.0));
         var0.draw(new java.awt.geom.Line2D.Double(var47, var50, var47, var50 + 18.0));
         var0.draw(new java.awt.geom.Line2D.Double(var47 - 10.0, var50 + 9.0, var47 + 10.0, var50 + 9.0));
      }

      var0.setColor(new Color(5909008));
      var0.fill(new java.awt.geom.RoundRectangle2D.Double(var8 - 11.0, var10 - 38.0, 22.0, 38.0, 20.0, 20.0));
      var0.setColor(new Color(16765498));
      var0.fill(new Double(var8 + 5.0, var10 - 20.0, 4.0, 4.0));

      for (byte var43 = -1; var43 <= 1; var43 += 2) {
         candyStick(var0, var8 + var43 * 15, var10, var10 - 44.0, var43);
      }

      var0.setColor(new Color(3050052));
      var0.setStroke(new BasicStroke(3.5F));
      var0.draw(new Double(var8 - 6.0, var10 - 34.0, 12.0, 12.0));
      var0.setColor(new Color(15217210));
      var0.fill(new Double(var8 - 2.0, var10 - 25.0, 4.0, 4.0));
      var0.setPaint(new GradientPaint(0.0F, (float)var10, new Color(16053503), 0.0F, 256.0F, new Color(13159656)));
      var0.fill(new java.awt.geom.Rectangle2D.Double(0.0, var10, 160.0, 256.0 - var10));

      for (int var44 = 0; var44 < 5; var44++) {
         double var48 = var10 + 6.0 + var44 * 9;
         double var51 = var8 + (var44 % 2 == 0 ? -3 : 3);
         var0.setColor(new Color(var44 % 2 == 0 ? 15217210 : 3061834));
         var0.fill(new Double(var51 - 5.0 - var44, var48 - 2.0, 10 + var44 * 2, 5.0));
      }

      for (byte var45 = -1; var45 <= 1; var45 += 2) {
         double var49 = var8 + var45 * 62;
         double var52 = var10 + 16.0;
         var0.setColor(new Color(16052460));
         var0.fill(new java.awt.geom.Rectangle2D.Double(var49 - 1.2, var52 - 28.0, 2.4, 30.0));
         var0.setColor(new Color(var45 < 0 ? 15217210 : 3836671));
         var0.fill(new Double(var49 - 9.0, var52 - 44.0, 18.0, 18.0));
         var0.setColor(Color.WHITE);
         var0.setStroke(new BasicStroke(2.0F));
         var0.draw(new java.awt.geom.Arc2D.Double(var49 - 6.0, var52 - 41.0, 12.0, 12.0, 90.0 + var2 * 57.3 * var45, 250.0, 0));
      }

      snow(var0, var6, var2, 70, 714, 0.85);
   }

   static void gumdrop(Graphics2D var0, double var1, double var3, double var5, int var7) {
      var0.setPaint(
         new RadialGradientPaint(
            (float)(var1 - var5 * 0.3),
            (float)(var3 - var5 * 0.4),
            (float)(var5 * 1.3),
            new float[]{0.0F, 1.0F},
            new Color[]{new Color(Capes.mix(var7, 16777215, 0.45)), new Color(Capes.mix(var7, 0, 0.2))}
         )
      );
      java.awt.geom.Path2D.Double var8 = new java.awt.geom.Path2D.Double();
      var8.moveTo(var1 - var5, var3 + var5 * 0.6);
      var8.quadTo(var1 - var5, var3 - var5, var1, var3 - var5);
      var8.quadTo(var1 + var5, var3 - var5, var1 + var5, var3 + var5 * 0.6);
      var8.closePath();
      var0.fill(var8);
      var0.setColor(Capes.col(16777215, 0.8));

      for (int var9 = 0; var9 < 4; var9++) {
         var0.fill(new Double(var1 - var5 * 0.6 + var9 * var5 * 0.4, var3 - var5 * 0.2 + var9 % 2 * var5 * 0.4, 1.1, 1.1));
      }
   }

   static void candyStick(Graphics2D var0, double var1, double var3, double var5, int var7) {
      java.awt.geom.Path2D.Double var8 = new java.awt.geom.Path2D.Double();
      var8.moveTo(var1, var3);
      var8.lineTo(var1, var5 + 6.0);
      var8.quadTo(var1, var5 - 2.0, var1 + var7 * 5, var5 - 2.0);
      var8.quadTo(var1 + var7 * 10, var5 - 2.0, var1 + var7 * 10, var5 + 5.0);
      var0.setColor(Color.WHITE);
      var0.setStroke(new BasicStroke(5.0F, 1, 1));
      var0.draw(var8);
      var0.setColor(new Color(14688298));
      var0.setStroke(new BasicStroke(5.0F, 0, 1, 1.0F, new float[]{3.5F, 3.5F}, 0.0F));
      var0.draw(var8);
      var0.setStroke(new BasicStroke(1.0F));
   }

   static void candyCane(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var2x, var4x, var6x, var7) -> {
         double var8x = (var6x + var7 * 0.8) / 40.0 - var6;
         double var10x = var8x - Math.floor(var8x);
         int var12x = var10x < 0.38 ? 14160926 : (var10x > 0.46 && var10x < 0.5 ? 15227482 : (var10x > 0.66 && var10x < 0.7 ? 3057738 : 16512752));
         double var13x = Math.min(Math.min(Math.abs(var10x), Math.abs(var10x - 0.38)), Math.abs(1.0 - var10x));
         if (var13x < 0.012) {
            var12x = Capes.mix(var12x, 15769760, 0.5);
         }

         double var15x = 0.86 + 0.18 * Math.sin(var2x * Math.PI) + 0.25 * Math.exp(-Math.pow((var2x - 0.32) / 0.06, 2.0));
         return Capes.shade(var12x, var15x);
      });
      Capes.sweep(var1, var6, 0.07, 0.35);
      double var8 = 80.0;
      double var10 = 108.0;

      for (int var12 = 0; var12 < 26; var12++) {
         double var13 = (Math.PI * 2) * var12 / 26.0;
         double var15 = 30 + var12 % 2 * 3;
         double var17 = var8 + Math.cos(var13) * var15;
         double var19 = var10 + Math.sin(var13) * var15;
         var0.setPaint(
            new RadialGradientPaint(
               (float)var17 - 2.0F, (float)var19 - 2.0F, 11.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(4896864), new Color(1727018)}
            )
         );
         var0.fill(new Double(var17 - 9.0, var19 - 7.0, 18.0, 14.0));
      }

      for (int var21 = 0; var21 < 10; var21++) {
         double var23 = (Math.PI * 2) * var21 / 10.0 + 0.3;
         double var25 = var8 + Math.cos(var23) * 31.0;
         double var26 = var10 + Math.sin(var23) * 31.0;
         double var27 = 0.6 + 0.4 * Math.sin(var2 * 2.0 + var21 * 1.3);
         Capes.addDot(var1, var25, var26, 7.0, var21 % 2 == 0 ? 16726586 : 16765498, 0.5 * var27);
         var0.setColor(new Color(var21 % 2 == 0 ? 15212586 : 16765498));
         var0.fill(new Double(var25 - 3.0, var26 - 3.0, 6.0, 6.0));
         var0.setColor(Capes.col(16777215, 0.8));
         var0.fill(new Double(var25 - 1.8, var26 - 2.0, 2.0, 2.0));
      }

      var0.setColor(new Color(14160926));
      java.awt.geom.Path2D.Double var22 = new java.awt.geom.Path2D.Double();
      var22.moveTo(var8, var10 + 34.0);
      var22.curveTo(var8 - 22.0, var10 + 18.0, var8 - 26.0, var10 + 44.0, var8 - 4.0, var10 + 38.0);
      var22.closePath();
      java.awt.geom.Path2D.Double var24 = new java.awt.geom.Path2D.Double();
      var24.moveTo(var8, var10 + 34.0);
      var24.curveTo(var8 + 22.0, var10 + 18.0, var8 + 26.0, var10 + 44.0, var8 + 4.0, var10 + 38.0);
      var24.closePath();
      var0.fill(var22);
      var0.fill(var24);
      var0.fill(new Polygon(new int[]{(int)var8 - 3, (int)var8 - 12, (int)var8 - 6}, new int[]{(int)var10 + 36, (int)var10 + 60, (int)var10 + 58}, 3));
      var0.fill(new Polygon(new int[]{(int)var8 + 3, (int)var8 + 12, (int)var8 + 6}, new int[]{(int)var10 + 36, (int)var10 + 60, (int)var10 + 58}, 3));
      var0.setColor(new Color(16730706));
      var0.fill(new Double(var8 - 5.0, var10 + 30.0, 10.0, 9.0));
   }

   static void sleighMoon(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var0x, var2x, var4x, var5x) -> {
         int var6x = Capes.ramp(var2x, 395806, 1055812, 1977952, 2767472);
         double var7 = Math.hypot(var4x - 80, (var5x - 92) * 1.0);
         return Capes.add(var6x, 6978240, Math.exp(-Math.pow(Math.max(0.0, var7 - 50.0) / 40.0, 2.0)) * 0.5);
      });
      stars(var0, var2, 50, 721, 200.0);
      double var8 = 80.0;
      double var10 = 92.0;
      double var12 = 50.0;
      var0.setPaint(
         new RadialGradientPaint(
            (float)(var8 - 14.0),
            (float)(var10 - 16.0),
            (float)(var12 * 1.3),
            new float[]{0.0F, 0.7F, 1.0F},
            new Color[]{new Color(16776424), new Color(15787712), new Color(14207128)}
         )
      );
      var0.fill(new Double(var8 - var12, var10 - var12, var12 * 2.0, var12 * 2.0));
      Random var14 = new Random(722L);

      for (int var15 = 0; var15 < 14; var15++) {
         double var16 = var14.nextDouble() * Math.PI * 2.0;
         double var18 = Math.sqrt(var14.nextDouble()) * var12 * 0.8;
         double var20 = 2.0 + var14.nextDouble() * 7.0;
         double var22 = var8 + Math.cos(var16) * var18;
         double var24 = var10 + Math.sin(var16) * var18;
         var0.setColor(Capes.col(12101752, 0.45));
         var0.fill(new Double(var22 - var20, var24 - var20, var20 * 2.0, var20 * 2.0));
         var0.setColor(Capes.col(16777200, 0.35));
         var0.fill(new Double(var22 - var20 * 0.8, var24 - var20 * 0.9, var20 * 1.2, var20 * 0.8));
      }

      double var46 = Math.sin(var2) * 3.0;
      int var17 = 790564;
      var0.setColor(new Color(var17));

      for (int var47 = 0; var47 < 26; var47++) {
         double var19 = var47 / 26.0;
         double var21 = 34.0 - var19 * 40.0;
         double var23 = 108.0 + var46 + Math.sin(var19 * 6.0 - var2) * 4.0 + var19 * 18.0;
         Capes.star(var0, var21, var23, 1.2 + (1.0 - var19) * 1.4, 16769162, (1.0 - var19) * (0.6 + 0.4 * Math.sin(var2 * 2.0 + var47)));
      }

      var0.setColor(new Color(var17));
      double var48 = 46.0;
      double var49 = 104.0 + var46;
      java.awt.geom.Path2D.Double var50 = new java.awt.geom.Path2D.Double();
      var50.moveTo(var48 - 16.0, var49 - 12.0);
      var50.lineTo(var48 - 14.0, var49 + 2.0);
      var50.lineTo(var48 + 10.0, var49 + 2.0);
      var50.quadTo(var48 + 16.0, var49 - 2.0, var48 + 14.0, var49 - 8.0);
      var50.lineTo(var48 + 8.0, var49 - 6.0);
      var50.lineTo(var48 - 8.0, var49 - 6.0);
      var50.lineTo(var48 - 10.0, var49 - 14.0);
      var50.closePath();
      var0.fill(var50);
      var0.setStroke(new BasicStroke(1.8F, 1, 1));
      java.awt.geom.Path2D.Double var51 = new java.awt.geom.Path2D.Double();
      var51.moveTo(var48 - 18.0, var49 + 6.0);
      var51.lineTo(var48 + 14.0, var49 + 6.0);
      var51.quadTo(var48 + 21.0, var49 + 5.0, var48 + 19.0, var49 - 1.0);
      var0.draw(var51);
      var0.draw(new java.awt.geom.Line2D.Double(var48 - 10.0, var49 + 2.0, var48 - 10.0, var49 + 6.0));
      var0.draw(new java.awt.geom.Line2D.Double(var48 + 6.0, var49 + 2.0, var48 + 6.0, var49 + 6.0));
      var0.fill(new Double(var48 - 4.0, var49 - 16.0, 9.0, 11.0));
      var0.fill(new Double(var48 - 1.0, var49 - 22.0, 6.0, 6.0));
      var0.fill(new Double(var48 - 15.0, var49 - 20.0, 11.0, 12.0));
      var0.setStroke(new BasicStroke(0.9F));
      var0.draw(new java.awt.geom.Line2D.Double(var48 + 4.0, var49 - 12.0, var48 + 30.0, var49 - 12.0 + Math.sin(var2) * 1.0));

      for (int var52 = 0; var52 < 4; var52++) {
         double var25 = var48 + 30.0 + var52 * 20;
         double var27 = var49 - 14.0 - var52 * 4 + Math.sin(var2 + var52 * 0.9) * 2.5;
         reindeer(var0, var25, var27, var2 * 2.0 + var52 * 1.1, var52 == 3);
         if (var52 < 3) {
            var0.setColor(new Color(var17));
            var0.setStroke(new BasicStroke(0.9F));
            var0.draw(
               new java.awt.geom.Line2D.Double(
                  var25 + 7.0, var27 - 3.0, var25 + 18.0, var27 - 7.0 + Math.sin(var2 + (var52 + 1) * 0.9) * 2.5 - Math.sin(var2 + var52 * 0.9) * 2.5
               )
            );
         }
      }

      Random var53 = new Random(723L);
      double var54 = -4.0;
      int var55 = 0;

      while (var54 < 160.0) {
         double var28 = 16.0 + var53.nextDouble() * 14.0;
         double var30 = 26.0 + var53.nextDouble() * 30.0;
         double var32 = var54;
         double var34 = 242.0 - var30;
         var0.setColor(new Color(Capes.mix(922672, 1712712, var53.nextDouble())));
         var0.fill(new java.awt.geom.Rectangle2D.Double(var54, var34, var28, var30 + 14.0));
         java.awt.geom.Path2D.Double var36 = new java.awt.geom.Path2D.Double();
         var36.moveTo(var54 - 2.0, var34);
         var36.lineTo(var54 + var28 / 2.0, var34 - 10.0);
         var36.lineTo(var54 + var28 + 2.0, var34);
         var36.closePath();
         var0.fill(var36);
         var0.setColor(new Color(15266047));
         java.awt.geom.Path2D.Double var37 = new java.awt.geom.Path2D.Double();
         var37.moveTo(var54 - 2.0, var34);
         var37.lineTo(var54 + var28 / 2.0, var34 - 10.0);
         var37.lineTo(var54 + var28 + 2.0, var34);
         var37.lineTo(var54 + var28 - 2.0, var34 + 1.5);
         var37.lineTo(var54 + var28 / 2.0, var34 - 7.0);
         var37.lineTo(var54 + 2.0, var34 + 1.5);
         var37.closePath();
         var0.fill(var37);

         for (int var38 = 0; var38 < 3; var38++) {
            for (int var39 = 0; var39 < 2; var39++) {
               if (!(var53.nextDouble() < 0.35)) {
                  double var40 = 0.6 + 0.4 * Math.sin(var2 * (1 + var55 % 2) + var55 * 1.7);
                  double var42 = var32 + 3.0 + var39 * (var28 - 10.0) / 1.0 * 0.6;
                  double var44 = var34 + 6.0 + var38 * 10;
                  if (!(var44 > 238.0)) {
                     var0.setColor(new Color(Capes.mix(5913114, 16765562, var40)));
                     var0.fill(new java.awt.geom.Rectangle2D.Double(var42, var44, 4.0, 5.0));
                     var55++;
                  }
               }
            }
         }

         var54 += var28 + 1.0;
      }

      var0.setPaint(new GradientPaint(0.0F, 240.0F, new Color(15266047), 0.0F, 256.0F, new Color(11583712)));
      var0.fill(new java.awt.geom.Rectangle2D.Double(0.0, 240.0, 160.0, 16.0));
      snow(var0, var6, var2, 50, 724, 0.7);
   }

   static void reindeer(Graphics2D var0, double var1, double var3, double var5, boolean var7) {
      var0.setColor(new Color(790564));
      var0.fill(new Double(var1 - 7.0, var3 - 3.5, 14.0, 7.0));
      java.awt.geom.Path2D.Double var8 = new java.awt.geom.Path2D.Double();
      var8.moveTo(var1 + 4.0, var3 - 2.0);
      var8.lineTo(var1 + 8.0, var3 - 8.0);
      var8.lineTo(var1 + 10.0, var3 - 7.0);
      var8.lineTo(var1 + 7.0, var3);
      var8.closePath();
      var0.fill(var8);
      var0.fill(new Double(var1 + 7.0, var3 - 10.0, 6.0, 4.0));
      var0.setStroke(new BasicStroke(1.2F, 1, 1));
      var0.draw(new java.awt.geom.Line2D.Double(var1 + 8.0, var3 - 10.0, var1 + 6.0, var3 - 15.0));
      var0.draw(new java.awt.geom.Line2D.Double(var1 + 7.0, var3 - 13.0, var1 + 4.0, var3 - 14.0));
      var0.draw(new java.awt.geom.Line2D.Double(var1 + 9.0, var3 - 10.0, var1 + 10.0, var3 - 15.0));
      var0.draw(new java.awt.geom.Line2D.Double(var1 + 10.0, var3 - 13.0, var1 + 12.0, var3 - 15.0));
      double var9 = Math.sin(var5);
      var0.setStroke(new BasicStroke(1.4F, 1, 1));
      var0.draw(new java.awt.geom.Line2D.Double(var1 + 4.0, var3 + 2.0, var1 + 8.0 + var9 * 2.0, var3 + 7.0));
      var0.draw(new java.awt.geom.Line2D.Double(var1 + 3.0, var3 + 2.0, var1 + 6.0 - var9 * 2.0, var3 + 7.0));
      var0.draw(new java.awt.geom.Line2D.Double(var1 - 5.0, var3 + 2.0, var1 - 9.0 - var9 * 2.0, var3 + 6.0));
      var0.draw(new java.awt.geom.Line2D.Double(var1 - 4.0, var3 + 2.0, var1 - 7.0 + var9 * 2.0, var3 + 7.0));
      var0.fill(new Double(var1 - 9.0, var3 - 4.0, 3.0, 3.0));
      if (var7) {
         var0.setColor(new Color(16726586));
         var0.fill(new Double(var1 + 11.5, var3 - 9.5, 2.6, 2.6));
         Capes.dot(var0, var1 + 12.8, var3 - 8.2, 5.0, 16726586, 0.7);
      }
   }

   static void sweater(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      byte var6 = 5;
      int var7 = 160 / var6;
      int var8 = 256 / var6 + 1;
      int[][] var9 = new int[var8][var7];

      for (int[] var13 : var9) {
         Arrays.fill(var13, 12063774);
      }

      byte var14 = 2;

      for (int var21 = 0; var21 < var7; var21++) {
         var9[var14][var21] = 16184042;
         var9[var14 + 1][var21] = var21 / 2 % 2 == 0 ? 1997368 : 16184042;
         var9[var14 + 2][var21] = 16184042;
      }

      var14 = 7;

      for (int var22 = 0; var22 < var7; var22++) {
         int var29 = Math.abs(var22 % 6 - 3);

         for (int var32 = 0; var32 < 4; var32++) {
            if (var32 == var29) {
               var9[var14 + var32][var22] = 16184042;
            }
         }
      }

      var14 = 13;
      stamp(var9, DEER, 2, var14, 16184042, 1997368, 16765498, 6961690, var4);
      stampMirror(var9, DEER, var7 - 13, var14, 16184042, 1997368, 16765498, 6961690, var4);
      var14 = 25;

      for (int var23 = 0; var23 < var7; var23++) {
         var9[var14][var23] = 1997368;
         var9[var14 + 1][var23] = var23 % 4 == 1 ? 16184042 : 1997368;
         var9[var14 + 2][var23] = 1997368;
      }

      var14 = 29;

      for (int var24 = 0; var24 < 3; var24++) {
         stamp(var9, TREE, 1 + var24 * 11, var14, 16184042, 1997368, 16765498, 6961690, var4 + var24);
      }

      var14 = 41;

      for (int var25 = 0; var25 < var7; var25++) {
         var9[var14][var25] = 16184042;
         var9[var14 + 1][var25] = var25 / 2 % 2 == 0 ? 16184042 : 12063774;
      }

      var14 = 44;

      for (int var26 = 0; var26 < 4; var26++) {
         stamp(var9, FLAKE, 1 + var26 * 8, var14, 16184042, 1997368, 16765498, 6961690, var4);
      }

      for (int var27 = 0; var27 < var8; var27++) {
         for (int var30 = 0; var30 < var7; var30++) {
            knitStitch(var0, var30 * var6, var27 * var6, var6, var9[var27][var30]);
         }
      }

      for (int var28 = 0; var28 < var8; var28++) {
         for (int var31 = 0; var31 < var7; var31++) {
            if (var9[var28][var31] == 16726586 || var9[var28][var31] == 16765498 && var28 > 28 && var28 < 40) {
               Capes.addDot(var1, var31 * var6 + 2.5, var28 * var6 + 2.5, 5.0, var9[var28][var31], 0.25);
            }
         }
      }
   }

   private static void stamp(int[][] var0, String[] var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      for (int var9 = 0; var9 < var1.length; var9++) {
         for (int var10 = 0; var10 < var1[var9].length(); var10++) {
            put(var0, var2 + var10, var3 + var9, var1[var9].charAt(var10), var4, var5, var6, var7, var8 + var9 * 3 + var10);
         }
      }
   }

   private static void stampMirror(int[][] var0, String[] var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      for (int var9 = 0; var9 < var1.length; var9++) {
         for (int var10 = 0; var10 < var1[var9].length(); var10++) {
            put(var0, var2 + var1[var9].length() - 1 - var10, var3 + var9, var1[var9].charAt(var10), var4, var5, var6, var7, var8 + var9 * 3 + var10);
         }
      }
   }

   private static void put(int[][] var0, int var1, int var2, char var3, int var4, int var5, int var6, int var7, int var8) {
      if (var2 >= 0 && var2 < var0.length && var1 >= 0 && var1 < var0[0].length) {
         switch (var3) {
            case 'B':
               var0[var2][var1] = var7;
               break;
            case 'G':
               var0[var2][var1] = var5;
               break;
            case 'K':
               var0[var2][var1] = 16726586;
               break;
            case 'L':
               int[] var9 = new int[]{16765498, 16726586, 3844351, 16184042};
               var0[var2][var1] = var9[Math.floorMod(var8, 4)];
               break;
            case 'W':
               var0[var2][var1] = var4;
               break;
            case 'Y':
               var0[var2][var1] = var6;
         }
      }
   }

   private static void knitStitch(Graphics2D var0, double var1, double var3, double var5, int var7) {
      var0.setColor(new Color(Capes.shade(var7, 0.62)));
      var0.fill(new java.awt.geom.Rectangle2D.Double(var1, var3, var5, var5));

      for (byte var8 = -1; var8 <= 1; var8 += 2) {
         AffineTransform var9 = var0.getTransform();
         var0.translate(var1 + var5 / 2.0 + var8 * var5 * 0.22, var3 + var5 * 0.5);
         var0.rotate(-var8 * 0.55);
         var0.setPaint(
            new GradientPaint(
               0.0F, (float)(-var5 * 0.6), new Color(Capes.mix(var7, 16777215, 0.18)), 0.0F, (float)(var5 * 0.6), new Color(Capes.shade(var7, 0.85))
            )
         );
         var0.fill(new Double(-var5 * 0.2, -var5 * 0.62, var5 * 0.4, var5 * 1.24));
         var0.setTransform(var9);
      }
   }

   static void auroraXmas(Graphics2D var0, BufferedImage var1, double var2, int var4, int var5) {
      double var6 = (double)var4 / var5;
      Capes.field(var1, (var4x, var6x, var8x, var9x) -> {
         int var10x = Capes.ramp(var6x, 198159, 463402, 926272, 1586264);

         for (int var11x = 0; var11x < 2; var11x++) {
            double var12 = (var11x == 0 ? 0.42 : 0.3) + 0.07 * Math.sin(var4x * 7.0 + var2 + var11x * 2) + 0.03 * Math.sin(var4x * 17.0 - var2 * 2.0 + var11x);
            double var14 = var12 - var6x;
            if (!(var14 < -0.02)) {
               double var16x = 0.5 + 0.5 * Math.sin(var4x * 60.0 + Math.sin(var4x * 9.0 + var2) * 3.0 + var11x * 5);
               double var18x = Capes.fbm(var4x * 8.0 + var11x * 3, var6 * 2.0, 8, 2, 3, 731 + var11x);
               double var20x = Math.exp(-Math.max(0.0, var14) / (0.16 + 0.06 * var18x)) * (0.35 + 0.65 * var16x * var18x) * Capes.smooth(-0.02, 0.01, var14);
               int var22x = var11x == 0 ? Capes.mix(3866506, 11557631, Math.min(1.0, var14 * 3.5)) : Capes.mix(3860696, 16734936, Math.min(1.0, var14 * 4.0));
               var10x = Capes.add(var10x, var22x, var20x * (var11x == 0 ? 0.95 : 0.6));
            }
         }

         return var10x;
      });
      stars(var0, var2, 40, 732, 150.0);
      java.awt.geom.Path2D.Double var8 = new java.awt.geom.Path2D.Double();
      var8.moveTo(0.0, 190.0);
      var8.lineTo(26.0, 150.0);
      var8.lineTo(48.0, 172.0);
      var8.lineTo(82.0, 132.0);
      var8.lineTo(116.0, 176.0);
      var8.lineTo(138.0, 156.0);
      var8.lineTo(160.0, 176.0);
      var8.lineTo(160.0, 256.0);
      var8.lineTo(0.0, 256.0);
      var8.closePath();
      var0.setPaint(new GradientPaint(0.0F, 130.0F, new Color(2767450), 0.0F, 200.0F, new Color(1186350)));
      var0.fill(var8);
      java.awt.geom.Path2D.Double var9 = new java.awt.geom.Path2D.Double();
      var9.moveTo(70.0, 147.0);
      var9.lineTo(82.0, 132.0);
      var9.lineTo(96.0, 150.0);
      var9.lineTo(88.0, 147.0);
      var9.lineTo(82.0, 152.0);
      var9.lineTo(76.0, 146.0);
      var9.closePath();
      var9.moveTo(18.0, 162.0);
      var9.lineTo(26.0, 150.0);
      var9.lineTo(34.0, 160.0);
      var9.lineTo(26.0, 158.0);
      var9.closePath();
      var9.moveTo(131.0, 164.0);
      var9.lineTo(138.0, 156.0);
      var9.lineTo(146.0, 162.0);
      var9.lineTo(139.0, 161.0);
      var9.closePath();
      var0.setColor(new Color(14214392));
      var0.fill(var9);
      java.awt.geom.Path2D.Double var10 = new java.awt.geom.Path2D.Double();
      var10.moveTo(0.0, 214.0);
      var10.quadTo(80.0, 188.0, 160.0, 214.0);
      var10.lineTo(160.0, 256.0);
      var10.lineTo(0.0, 256.0);
      var10.closePath();
      var0.setPaint(new GradientPaint(0.0F, 190.0F, new Color(15266047), 0.0F, 256.0F, new Color(9087184)));
      var0.fill(var10);
      Capes.addDot(var1, 80.0, 206.0, 60.0, 3866506, 0.12);
      double var11 = 80.0;
      double var13 = 206.0;
      pine(var0, var11, var13, 70.0, 1329194, 15791871, 5);
      Random var15 = new Random(733L);
      int[] var16 = new int[]{16726586, 16765498, 3844351, 16743136, 3860586};

      for (int var17 = 0; var17 < 18; var17++) {
         double var18 = var15.nextDouble();
         double var20 = var13 - 10.0 - var18 * 50.0;
         double var22 = (1.0 - var18) * 22.0 + 3.0;
         double var24 = var11 + (var15.nextDouble() * 2.0 - 1.0) * var22 * 0.85;
         double var26 = 0.5 + 0.5 * Math.sin(var2 * (1 + var17 % 2) + var17 * 2.1);
         int var28 = var16[var17 % var16.length];
         Capes.addDot(var1, var24, var20, 5.0, var28, 0.45 * var26);
         var0.setColor(new Color(Capes.mix(4210752, var28, 0.4 + 0.6 * var26)));
         var0.fill(new Double(var24 - 1.6, var20 - 1.6, 3.2, 3.2));
      }

      Capes.addDot(var1, var11, var13 - 66.0, 14.0, 16769146, 0.6 + 0.2 * Math.sin(var2));
      var0.setColor(new Color(16769146));
      var0.fill(XmasTex.starShape(var11, var13 - 66.0, 6.0, 2.6, 5));
      int[][] var29 = new int[][]{{-18, 15217210}, {12, 3836671}, {-6, 3061834}};

      for (int[] var21 : var29) {
         double var32 = var11 + var21[0];
         double var33 = var13 - 2.0;
         var0.setColor(new Color(var21[1]));
         var0.fill(new java.awt.geom.Rectangle2D.Double(var32, var33 - 7.0, 9.0, 8.0));
         var0.setColor(new Color(16769146));
         var0.fill(new java.awt.geom.Rectangle2D.Double(var32 + 3.8, var33 - 7.0, 1.4, 8.0));
      }

      snow(var0, var6, var2, 45, 734, 0.6);
   }
}
