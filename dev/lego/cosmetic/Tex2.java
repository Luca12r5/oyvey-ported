package dev.lego.cosmetic;

import dev.lego.ui.Gx;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.function.Consumer;

final class Tex2 {
   private static final Object[][] FEATHERS = new Object[][]{
      {"angel", 16777215, 15659770, 13226982, 15259816, "none"},
      {"gold", 16774848, 15909424, 11565586, 16774064, "metal"},
      {"phoenix", 16774048, 16747038, 13112346, 16769658, "fire"},
      {"shadow", 4869730, 1974322, 526351, 10185727, "rim"},
      {"owl", 15785400, 10910798, 5124634, 15258280, "bars"},
      {"rainbow", 16777215, 16053492, 14342874, 16777215, "none"},
      {"galaxy", 3807866, 1706554, 459802, 13150463, "stars"},
      {"crystal", 15794175, 9429247, 3832575, 16777215, "crystal"},
      {"fire", 16774848, 16756784, 16726538, 16777215, "flame"}
   };

   private Tex2() {
   }

   static CTex.T img(int var0, int var1, Consumer<Graphics2D> var2) {
      BufferedImage var3 = new BufferedImage(var0, var1, 2);
      Graphics2D var4 = var3.createGraphics();
      Gx.hints(var4);
      var2.accept(var4);
      var4.dispose();
      return CTex.of(var3);
   }

   static CTex.T pix(int var0, int var1, Tex2.PF var2) {
      BufferedImage var3 = new BufferedImage(var0, var1, 2);
      int[] var4 = Capes.px(var3);

      for (int var5 = 0; var5 < var1; var5++) {
         for (int var6 = 0; var6 < var0; var6++) {
            var4[var5 * var0 + var6] = var2.at((var6 + 0.5) / var0, (var5 + 0.5) / var1, var6, var5);
         }
      }

      return CTex.of(var3);
   }

   static int argb(int var0, double var1) {
      return (int)Math.round(Math.max(0.0, Math.min(1.0, var1)) * 255.0) << 24 | var0 & 16777215;
   }

   static CTex.T paint(String var0) {
      if (!var0.startsWith("fea_") && !var0.startsWith("cov_")) {
         switch (var0) {
            case "mem_demon":
               return membrane(6950936, 12593712, 1704966, 16738890, 0.93);
            case "mem_icedragon":
               return membrane(1727130, 8050943, 663098, 14744319, 0.82);
            case "mem_neon":
               return membrane(2755146, 8006344, 655896, 16739061, 0.8);
            case "mem_bat":
               return membrane(4862504, 9071192, 1708558, 13148296, 0.9);
            case "mem_dragon":
               return membrane(11550730, 16752704, 4853764, 16769162, 0.9);
            case "bone":
               return pix(16, 64, (var0x, var2, var4, var5x) -> 0xFF000000 | Capes.mix(16777215, 10526880, Math.abs(var0x - 0.35) * 1.6));
            case "wing_fairy":
               return fairyWing();
            case "wing_butterfly2":
               return butterfly();
            case "mech_blade":
               return mechBlade();
            case "mech_frame":
               return pix(
                  32,
                  32,
                  (var0x, var2, var4, var5x) -> 0xFF000000 | Capes.shade(Capes.mix(5923440, 2764342, var2), 0.9 + 0.2 * Capes.hash(var4 / 4, var5x / 4, 3))
               );
            case "glow_line":
               return pix(16, 16, (var0x, var2, var4, var5x) -> argb(Capes.mix(16777215, 16777215, 0.0), 1.0 - Math.pow(Math.abs(var2 - 0.5) * 2.0, 2.0)));
            default:
               return Tex3.paint(var0);
         }
      } else {
         String var1 = var0.substring(4);

         for (Object[] var5 : FEATHERS) {
            if (var5[0].equals(var1)) {
               return feather((Integer)var5[1], (Integer)var5[2], (Integer)var5[3], (Integer)var5[4], (String)var5[5], var0.startsWith("cov_"));
            }
         }

         return null;
      }
   }

   static CTex.T feather(int var0, int var1, int var2, int var3, String var4, boolean var5) {
      byte var6 = 64;
      int var7 = var5 ? 128 : 256;
      return img(
         var6,
         var7,
         var8 -> {
            double var9 = var6;
            double var11 = var7;
            Double var13 = new Double();
            if (var4.equals("flame")) {
               var13.moveTo(var9 * 0.1, 0.0);
               var13.lineTo(var9 * 0.9, 0.0);
               var13.curveTo(var9 * 1.02, var11 * 0.35, var9 * 0.8, var11 * 0.62, var9 * 0.6, var11 * 0.8);
               var13.curveTo(var9 * 0.62, var11 * 0.9, var9 * 0.5, var11 * 0.96, var9 * 0.5, var11);
               var13.curveTo(var9 * 0.4, var11 * 0.9, var9 * 0.3, var11 * 0.84, var9 * 0.34, var11 * 0.74);
               var13.curveTo(var9 * 0.1, var11 * 0.6, var9 * -0.02, var11 * 0.3, var9 * 0.1, 0.0);
            } else if (var5) {
               var13.moveTo(var9 * 0.04, 0.0);
               var13.lineTo(var9 * 0.96, 0.0);
               var13.curveTo(var9 * 1.0, var11 * 0.5, var9 * 0.8, var11 * 0.9, var9 * 0.5, var11 * 0.99);
               var13.curveTo(var9 * 0.2, var11 * 0.9, var9 * 0.0, var11 * 0.5, var9 * 0.04, 0.0);
            } else {
               var13.moveTo(var9 * 0.1, 0.0);
               var13.lineTo(var9 * 0.9, 0.0);
               var13.curveTo(var9 * 0.98, var11 * 0.4, var9 * 0.92, var11 * 0.8, var9 * 0.62, var11 * 0.97);
               var13.quadTo(var9 * 0.5, var11 * 1.0, var9 * 0.4, var11 * 0.97);
               var13.curveTo(var9 * 0.1, var11 * 0.84, var9 * 0.02, var11 * 0.4, var9 * 0.1, 0.0);
            }

            var8.setPaint(
               new LinearGradientPaint(
                  0.0F, 0.0F, 0.0F, (float)var11, new float[]{0.0F, 0.45F, 1.0F}, new Color[]{new Color(var0), new Color(var1), new Color(var2)}
               )
            );
            if (var4.equals("crystal")) {
               var8.setPaint(
                  new LinearGradientPaint(
                     0.0F,
                     0.0F,
                     0.0F,
                     (float)var11,
                     new float[]{0.0F, 0.45F, 1.0F},
                     new Color[]{Capes.col(var0, 0.85), Capes.col(var1, 0.7), Capes.col(var2, 0.6)}
                  )
               );
            }

            if (var4.equals("flame")) {
               var8.setPaint(
                  new LinearGradientPaint(
                     0.0F,
                     0.0F,
                     0.0F,
                     (float)var11,
                     new float[]{0.0F, 0.4F, 0.8F, 1.0F},
                     new Color[]{new Color(var0), new Color(var1), new Color(var2), Capes.col(var2, 0.1)}
                  )
               );
            }

            var8.fill(var13);
            var8.setClip(var13);
            Random var14 = new Random(var5 ? 3L : 4L);
            if (!var4.equals("flame") && !var4.equals("crystal")) {
               var8.setStroke(new BasicStroke(1.0F));

               for (double var15 = 2.0; var15 < var11; var15 += 3.2) {
                  double var17 = 0.5 + var14.nextDouble() * 0.2;
                  var8.setColor(Capes.col(var14.nextBoolean() ? 16777215 : 0, 0.08 + var14.nextDouble() * 0.06));
                  var8.draw(new java.awt.geom.QuadCurve2D.Double(var9 / 2.0, var15, var9 * 0.75, var15 + 6.0, var9, var15 + 14.0 * var17));
                  var8.draw(new java.awt.geom.QuadCurve2D.Double(var9 / 2.0, var15, var9 * 0.25, var15 + 6.0, 0.0, var15 + 14.0 * var17));
               }

               var8.setColor(Capes.col(0, 0.22));

               for (int var22 = 0; var22 < (var5 ? 1 : 3); var22++) {
                  double var16 = var11 * (0.35 + var14.nextDouble() * 0.5);
                  double var18 = var14.nextBoolean() ? 1.0 : -1.0;
                  var8.draw(
                     new java.awt.geom.QuadCurve2D.Double(
                        var9 / 2.0 + var18 * 4.0, var16, var9 / 2.0 + var18 * 14.0, var16 + 8.0, var9 / 2.0 + var18 * 30.0, var16 + 20.0
                     )
                  );
               }
            }

            if (var4.equals("bars")) {
               for (double var23 = var11 * 0.18; var23 < var11 * 0.95; var23 += var11 * (var5 ? 0.3 : 0.17)) {
                  Double var29 = new Double();
                  var29.moveTo(0.0, var23);
                  var29.quadTo(var9 / 2.0, var23 - 7.0, var9, var23);
                  var29.lineTo(var9, var23 + 6.0);
                  var29.quadTo(var9 / 2.0, var23 - 1.0, 0.0, var23 + 6.0);
                  var29.closePath();
                  var8.setColor(Capes.col(2758664, 0.32));
                  var8.fill(var29);
               }

               var8.setColor(Capes.col(16774880, 0.25));
               var8.fill(new java.awt.geom.Ellipse2D.Double(var9 * 0.12, var11 * 0.05, var9 * 0.3, var11 * 0.08));
            }

            if (var4.equals("metal")) {
               var8.setPaint(new GradientPaint(0.0F, 0.0F, Capes.col(16777215, 0.55), (float)(var9 * 0.45), 0.0F, Capes.col(16777215, 0.0)));
               var8.fillRect(0, 0, (int)(var9 * 0.45), (int)var11);
               var8.setPaint(new GradientPaint((float)(var9 * 0.6), 0.0F, Capes.col(5913088, 0.0), (float)var9, 0.0F, Capes.col(5913088, 0.45)));
               var8.fillRect((int)(var9 * 0.6), 0, (int)var9, (int)var11);
            }

            if (var4.equals("fire")) {
               var8.setPaint(
                  new LinearGradientPaint(
                     0.0F, (float)(var11 * 0.55), 0.0F, (float)var11, new float[]{0.0F, 1.0F}, new Color[]{Capes.col(16769658, 0.0), Capes.col(16769658, 0.7)}
                  )
               );
               var8.fillRect(0, (int)(var11 * 0.55), (int)var9, (int)var11);
            }

            if (var4.equals("rim")) {
               var8.setClip(null);

               for (int var24 = 4; var24 >= 1; var24--) {
                  var8.setColor(Capes.col(10185727, 0.12 * (5 - var24)));
                  var8.setStroke(new BasicStroke(var24 * 2.2F));
                  var8.draw(var13);
               }

               var8.setClip(var13);
            }

            if (var4.equals("stars")) {
               for (int var25 = 0; var25 < (var5 ? 10 : 22); var25++) {
                  double var27 = var14.nextDouble() * var9;
                  double var30 = var14.nextDouble() * var11;
                  double var20 = 1.0 + Math.pow(var14.nextDouble(), 3.0) * 4.0;
                  var8.setPaint(
                     new RadialGradientPaint(
                        (float)var27,
                        (float)var30,
                        (float)(var20 * 2.5),
                        new float[]{0.0F, 0.3F, 1.0F},
                        new Color[]{Color.WHITE, Capes.col(13150463, 0.7), Capes.col(13150463, 0.0)}
                     )
                  );
                  var8.fill(new java.awt.geom.Ellipse2D.Double(var27 - var20 * 2.5, var30 - var20 * 2.5, var20 * 5.0, var20 * 5.0));
               }

               var8.setPaint(
                  new RadialGradientPaint(
                     (float)(var9 * 0.4),
                     (float)(var11 * 0.55),
                     (float)(var11 * 0.35),
                     new float[]{0.0F, 1.0F},
                     new Color[]{Capes.col(16734896, 0.35), Capes.col(16734896, 0.0)}
                  )
               );
               var8.fillRect(0, 0, (int)var9, (int)var11);
            }

            if (var4.equals("crystal")) {
               var8.setStroke(new BasicStroke(1.2F));

               for (int var26 = 0; var26 < 7; var26++) {
                  double var28 = var14.nextDouble() * var11;
                  var8.setColor(Capes.col(16777215, 0.5));
                  var8.draw(new java.awt.geom.Line2D.Double(0.0, var28, var9, var28 + (var14.nextDouble() - 0.5) * 60.0));
               }

               var8.setPaint(new GradientPaint(0.0F, 0.0F, Capes.col(16777215, 0.6), (float)(var9 * 0.5), 0.0F, Capes.col(16777215, 0.0)));
               var8.fillRect(0, 0, (int)(var9 * 0.5), (int)var11);
            }

            if (var4.equals("flame")) {
               var8.setPaint(
                  new RadialGradientPaint(
                     (float)(var9 / 2.0),
                     (float)(var11 * 0.15),
                     (float)(var11 * 0.4),
                     new float[]{0.0F, 1.0F},
                     new Color[]{Capes.col(16777215, 0.7), Capes.col(16777215, 0.0)}
                  )
               );
               var8.fillRect(0, 0, (int)var9, (int)var11);
            }

            if (!var4.equals("flame")) {
               var8.setPaint(
                  new GradientPaint((float)(var9 * 0.5), 0.0F, Capes.col(0, 0.0), (float)var9, 0.0F, Capes.col(0, var4.equals("crystal") ? 0.1 : 0.28))
               );
               var8.fillRect((int)(var9 * 0.5), 0, (int)var9, (int)var11);
            }

            var8.setClip(null);
            if (!var4.equals("flame")) {
               var8.setPaint(new GradientPaint(0.0F, 0.0F, new Color(var3), 0.0F, (float)var11, Capes.col(var3, 0.4)));
               var8.setStroke(new BasicStroke(var5 ? 2.2F : 2.8F, 1, 1));
               var8.draw(new java.awt.geom.QuadCurve2D.Double(var9 / 2.0, 0.0, var9 * 0.53, var11 * 0.5, var9 * 0.5, var11 * 0.93));
               var8.setColor(Capes.col(Capes.mix(var2, 0, 0.5), var4.equals("crystal") ? 0.3 : 0.55));
               var8.setStroke(new BasicStroke(1.4F));
               var8.draw(var13);
            }
         }
      );
   }

   static CTex.T membrane(int var0, int var1, int var2, int var3, double var4) {
      return pix(256, 256, (var6, var8, var10, var11) -> {
         double var12 = Capes.fbm(var6 * 7.0, var8 * 7.0, 0, 0, 4, 5);
         double var14 = var6 - 0.6;
         double var16 = var8 - 0.27;
         double var18 = Math.hypot(var14, var16);
         double var20 = Math.atan2(var16, var14);
         int var22 = Capes.ramp(Math.min(1.0, var6 * 0.8 + var8 * 0.35), var2, var0, var1);
         var22 = Capes.shade(var22, 0.82 + 0.3 * var12);
         double var23 = Capes.fbm(var6 * 4.0 + 7.0, var8 * 4.0, 0, 0, 3, 8) * 2.5;
         double var25 = Math.abs(Math.sin(var20 * 11.0 + var23 + var18 * 3.0));
         double var27 = (1.0 - Capes.smooth(0.0, 0.07 + var18 * 0.05, var25)) * Capes.smooth(0.02, 0.1, var18) * 0.55;
         double var29 = Math.abs(Math.sin(var18 * 38.0 + var23 * 1.3));
         var27 += (1.0 - Capes.smooth(0.0, 0.05, var29)) * 0.18 * Capes.smooth(0.05, 0.25, var18);
         var22 = Capes.mix(var22, var3, Math.min(0.7, var27));
         var22 = Capes.add(var22, var1, Math.pow(var12, 3.0) * 0.25);
         return argb(var22, var4 * (0.92 + 0.08 * var12) - var27 * 0.0);
      });
   }

   static CTex.T fairyWing() {
      return img(
         256,
         128,
         var0 -> {
            Double var1 = new Double();
            var1.moveTo(2.0, 64.0);
            var1.curveTo(60.0, 0.0, 200.0, -10.0, 250.0, 40.0);
            var1.curveTo(262.0, 70.0, 200.0, 112.0, 120.0, 110.0);
            var1.curveTo(60.0, 108.0, 20.0, 90.0, 2.0, 64.0);
            var0.setPaint(
               new LinearGradientPaint(
                  0.0F,
                  0.0F,
                  256.0F,
                  128.0F,
                  new float[]{0.0F, 0.35F, 0.7F, 1.0F},
                  new Color[]{Capes.col(16777215, 0.55), Capes.col(10154239, 0.45), Capes.col(14721279, 0.45), Capes.col(16756960, 0.55)}
               )
            );
            var0.fill(var1);
            var0.setClip(var1);
            var0.setColor(Capes.col(16777215, 0.55));
            var0.setStroke(new BasicStroke(1.6F));

            for (int var2 = 0; var2 < 6; var2++) {
               var0.draw(new java.awt.geom.QuadCurve2D.Double(2.0, 64.0, 110.0, 30 + var2 * 14, 256.0, var2 * 26 - 10));
            }

            Random var10 = new Random(4L);

            for (int var3 = 0; var3 < 60; var3++) {
               double var4 = var10.nextDouble() * 256.0;
               double var6 = var10.nextDouble() * 128.0;
               double var8 = 0.8 + var10.nextDouble() * 2.0;
               var0.setColor(Capes.col(16777215, 0.5 + var10.nextDouble() * 0.5));
               var0.fill(new java.awt.geom.Ellipse2D.Double(var4, var6, var8, var8));
            }

            var0.setClip(null);
            var0.setColor(Capes.col(16777215, 0.85));
            var0.setStroke(new BasicStroke(2.2F));
            var0.draw(var1);
         }
      );
   }

   static CTex.T butterfly() {
      return img(
         256,
         320,
         var0 -> {
            Double var1 = new Double();
            var1.moveTo(6.0, 30.0);
            var1.curveTo(90.0, -30.0, 250.0, -10.0, 246.0, 80.0);
            var1.curveTo(242.0, 140.0, 150.0, 170.0, 8.0, 150.0);
            var1.closePath();
            Double var2 = new Double();
            var2.moveTo(8.0, 150.0);
            var2.curveTo(120.0, 150.0, 210.0, 200.0, 190.0, 270.0);
            var2.curveTo(170.0, 330.0, 60.0, 320.0, 6.0, 230.0);
            var2.closePath();

            for (Path2D var6 : new Path2D[]{var2, var1}) {
               var0.setPaint(
                  new RadialGradientPaint(
                     20.0F,
                     120.0F,
                     250.0F,
                     new float[]{0.0F, 0.35F, 0.7F, 1.0F},
                     new Color[]{new Color(662154), new Color(1997823), new Color(5953791), new Color(2763424)}
                  )
               );
               var0.fill(var6);
               var0.setClip(var6);
               var0.setColor(Capes.col(329752, 0.8));
               var0.setStroke(new BasicStroke(2.4F));

               for (int var7 = 0; var7 < 7; var7++) {
                  var0.draw(
                     new java.awt.geom.QuadCurve2D.Double(
                        8.0, var6 == var1 ? 40.0 : 160.0, 120.0, (var6 == var1 ? 20 : 170) + var7 * 22, 260.0, (var6 == var1 ? -20 : 180) + var7 * 34
                     )
                  );
               }

               var0.setClip(null);
               var0.setColor(new Color(460303));
               var0.setStroke(new BasicStroke(12.0F));
               var0.draw(var6);
            }

            var0.setColor(Color.WHITE);
            double[][] var8 = new double[][]{
               {230.0, 40.0}, {236.0, 66.0}, {222.0, 96.0}, {196.0, 128.0}, {176.0, 258.0}, {156.0, 284.0}, {126.0, 298.0}, {206.0, 30.0}
            };

            for (double[] var12 : var8) {
               var0.fill(new java.awt.geom.Ellipse2D.Double(var12[0] - 5.0, var12[1] - 5.0, 10.0, 10.0));
            }

            var0.setPaint(
               new RadialGradientPaint(
                  168.0F, 72.0F, 20.0F, new float[]{0.0F, 0.5F, 1.0F}, new Color[]{new Color(16773792), new Color(16756784), new Color(5908992)}
               )
            );
            var0.fill(new java.awt.geom.Ellipse2D.Double(148.0, 52.0, 40.0, 40.0));
            var0.setColor(new Color(460303));
            var0.fill(new java.awt.geom.Ellipse2D.Double(160.0, 64.0, 16.0, 16.0));
            var0.setColor(Color.WHITE);
            var0.fill(new java.awt.geom.Ellipse2D.Double(163.0, 66.0, 5.0, 5.0));
         }
      );
   }

   static CTex.T mechBlade() {
      return img(
         64,
         256,
         var0 -> {
            Double var1 = new Double();
            var1.moveTo(4.0, 0.0);
            var1.lineTo(60.0, 0.0);
            var1.lineTo(58.0, 200.0);
            var1.lineTo(32.0, 254.0);
            var1.lineTo(6.0, 200.0);
            var1.closePath();
            var0.setPaint(
               new LinearGradientPaint(
                  0.0F,
                  0.0F,
                  64.0F,
                  0.0F,
                  new float[]{0.0F, 0.45F, 0.55F, 1.0F},
                  new Color[]{new Color(15265012), new Color(10134196), new Color(5923444), new Color(12107984)}
               )
            );
            var0.fill(var1);
            var0.setClip(var1);
            var0.setColor(Capes.col(1711654, 0.8));
            var0.setStroke(new BasicStroke(1.6F));

            for (byte var2 = 40; var2 < 256; var2 += 46) {
               var0.drawLine(0, var2, 64, var2 + 6);
            }

            var0.setColor(new Color(2811647));
            var0.setStroke(new BasicStroke(3.0F));
            var0.drawLine(32, 10, 32, 236);
            var0.setColor(Capes.col(16777215, 0.9));
            var0.setStroke(new BasicStroke(1.0F));
            var0.drawLine(32, 10, 32, 236);
            var0.setClip(null);
            var0.setColor(new Color(1711654));
            var0.setStroke(new BasicStroke(2.0F));
            var0.draw(var1);
         }
      );
   }

   static int hueRgb(double var0) {
      return Capes.hsv(var0, 0.55, 1.0);
   }

   static int addc(int var0, int var1, double var2) {
      return Capes.add(var0, var1, var2);
   }

   interface PF {
      int at(double var1, double var3, int var5, int var6);
   }
}
