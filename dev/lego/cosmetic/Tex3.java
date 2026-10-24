package dev.lego.cosmetic;

import dev.lego.ui.Art;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;
import java.util.Random;

final class Tex3 {
   private Tex3() {
   }

   static CTex.T paint(String var0) {
      switch (var0) {
         case "plastic":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> 0xFF000000 | Capes.mix(16777215, 13159636, var2 * 0.8 + Math.abs(var0x - 0.3) * 0.3));
         case "satin":
            return Tex2.pix(
               64, 64, (var0x, var2, var4, var5) -> 0xFF000000 | Capes.shade(15921906, 0.86 + 0.14 * Math.sin((var0x * 3.0 + var2) * Math.PI * 2.0))
            );
         case "cloth":
            return Tex2.pix(
               64,
               64,
               (var0x, var2, var4, var5) -> 0xFF000000 | Capes.shade(16053492, 0.93 + 0.07 * Capes.hash(var4, var5, 3) - ((var4 + var5) % 4 == 0 ? 0.04 : 0.0))
            );
         case "knit":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = var4 % 8 / 8.0;
               double var8 = var5 % 8 / 8.0;
               double var10 = Math.abs(var6 - 0.5) * 2.0;
               double var12 = 1.0 - Math.abs(var8 - var10 * 0.5 - 0.25) * 2.2;
               return 0xFF000000 | Capes.shade(16053492, 0.72 + 0.3 * Math.max(0.0, var12) + 0.05 * Capes.hash(var4, var5, 2));
            });
         case "leather":
            return Tex2.pix(
               64,
               64,
               (var0x, var2, var4, var5) -> 0xFF000000
                  | Capes.shade(Capes.mix(9067058, 5911576, var2), 0.85 + 0.25 * Capes.fbm(var0x * 10.0, var2 * 10.0, 0, 0, 4, 3))
            );
         case "leather_dark":
            return Tex2.pix(
               64,
               64,
               (var0x, var2, var4, var5) -> 0xFF000000
                  | Capes.shade(Capes.mix(3810840, 1970698, var2), 0.85 + 0.25 * Capes.fbm(var0x * 10.0, var2 * 10.0, 0, 0, 4, 3))
            );
         case "steel":
            return Tex2.pix(
               64,
               64,
               (var0x, var2, var4, var5) -> 0xFF000000
                  | Capes.shade(Capes.mix(15265010, 8029332, var2 * 0.7 + 0.3 * Math.abs(Math.sin(var0x * 6.0))), 0.95 + 0.08 * Capes.hash(var4, var5 / 3, 5))
            );
         case "iron":
            return Tex2.pix(
               64,
               64,
               (var0x, var2, var4, var5) -> 0xFF000000
                  | Capes.shade(Capes.mix(11581120, 4870234, var2), 0.85 + 0.25 * Capes.fbm(var0x * 8.0, var2 * 8.0, 0, 0, 3, 9))
            );
         case "wood":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Math.sin((var0x * 3.0 + Capes.fbm(var0x * 2.0, var2 * 8.0, 0, 0, 3, 4) * 2.0) * Math.PI * 4.0);
               return 0xFF000000 | Capes.mix(11563066, 6961688, 0.5 + 0.5 * var6);
            });
         case "ivory":
            return Tex2.pix(
               32, 64, (var0x, var2, var4, var5) -> 0xFF000000 | Capes.shade(Capes.mix(16774880, 13153418, var2), 0.92 + 0.1 * Math.sin(var2 * 40.0))
            );
         case "gloss":
            return Tex2.img(
               64,
               64,
               var0x -> {
                  var0x.setPaint(
                     new RadialGradientPaint(
                        22.0F, 18.0F, 50.0F, new float[]{0.0F, 0.2F, 1.0F}, new Color[]{Color.WHITE, new Color(16053492), new Color(11053224)}
                     )
                  );
                  var0x.fillRect(0, 0, 64, 64);
               }
            );
         case "fox_ear":
            return Tex2.img(64, 64, var0x -> {
               Double var1 = new Double();
               var1.moveTo(2.0, 62.0);
               var1.quadTo(10.0, 22.0, 32.0, 2.0);
               var1.quadTo(54.0, 22.0, 62.0, 62.0);
               var1.closePath();
               var0x.setPaint(new GradientPaint(0.0F, 0.0F, new Color(16747050), 0.0F, 64.0F, new Color(14178832)));
               var0x.fill(var1);
               var0x.setClip(var1);
               var0x.setColor(new Color(2759186));
               var0x.fill(new java.awt.geom.Ellipse2D.Double(10.0, -8.0, 44.0, 28.0));
               var0x.setClip(null);
               Double var2 = new Double();
               var2.moveTo(14.0, 62.0);
               var2.quadTo(20.0, 32.0, 32.0, 18.0);
               var2.quadTo(44.0, 32.0, 50.0, 62.0);
               var2.closePath();
               var0x.setPaint(new GradientPaint(0.0F, 18.0F, new Color(16774894), 0.0F, 62.0F, new Color(16767168)));
               var0x.fill(var2);
            });
         case "cap_segments":
            return Tex2.pix(128, 64, (var0x, var2, var4, var5) -> {
               int[] var6 = new int[]{14876683, 16764163, 27831, 41018};
               int var7 = (int)(var0x * 8.0) % 4;
               return 0xFF000000 | Capes.shade(var6[var7], 0.85 + 0.2 * (1.0 - var2) - (Math.abs(var0x * 8.0 % 1.0 - 0.5) > 0.47 ? 0.25 : 0.0));
            });
         case "party_cone":
            return Tex2.pix(64, 128, (var0x, var2, var4, var5) -> {
               boolean var6 = ((var0x * 4.0 + var2 * 3.0) % 1.0 + 1.0) % 1.0 < 0.5;
               int var7 = var6 ? 16726666 : 3852543;
               if (Capes.hash(var4 / 6, var5 / 6, 7) > 0.9) {
                  var7 = 16769610;
               }

               return 0xFF000000 | Capes.shade(var7, 0.85 + 0.15 * var2);
            });
         case "wizard_cone":
            return Tex2.img(64, 128, var0x -> {
               var0x.setPaint(new GradientPaint(0.0F, 0.0F, new Color(3812010), 0.0F, 128.0F, new Color(1444954)));
               var0x.fillRect(0, 0, 64, 128);
               Random var1 = new Random(3L);

               for (int var2 = 0; var2 < 14; var2++) {
                  star(var0x, var1.nextDouble() * 64.0, var1.nextDouble() * 128.0, 3.0 + var1.nextDouble() * 4.0, 16769658);
               }

               var0x.setColor(Capes.col(16769658, 0.8));

               for (int var3 = 0; var3 < 30; var3++) {
                  var0x.fill(new java.awt.geom.Ellipse2D.Double(var1.nextDouble() * 64.0, var1.nextDouble() * 128.0, 1.5, 1.5));
               }
            });
         case "mushroom_cap":
            return Tex2.img(128, 64, var0x -> {
               var0x.setPaint(new GradientPaint(0.0F, 0.0F, new Color(16726570), 0.0F, 64.0F, new Color(11536906)));
               var0x.fillRect(0, 0, 128, 64);
               Random var1 = new Random(5L);

               for (int var2 = 0; var2 < 14; var2++) {
                  double var3 = var2 % 7 * 18.3 + var1.nextDouble() * 6.0;
                  double var5 = 8 + var2 / 7 * 26 + var1.nextDouble() * 8.0;
                  double var7 = 7.0 + var1.nextDouble() * 6.0;
                  var0x.setColor(new Color(16775408));
                  var0x.fill(new java.awt.geom.Ellipse2D.Double(var3, var5, var7, var7 * 0.8));
               }
            });
         case "gill":
            return Tex2.pix(64, 16, (var0x, var2, var4, var5) -> 0xFF000000 | Capes.shade(16049872, 0.8 + 0.2 * Math.abs(Math.sin(var0x * 60.0))));
         case "vine":
            return Tex2.img(128, 32, var0x -> {
               var0x.setColor(new Color(3832362));
               var0x.fillRect(0, 12, 128, 8);
               Random var1 = new Random(2L);

               for (int var2 = 0; var2 < 18; var2++) {
                  double var3 = var2 * 7.3;
                  double var5 = 16.0;
                  AffineTransform var7 = var0x.getTransform();
                  var0x.translate(var3, var5);
                  var0x.rotate((var2 % 2 == 0 ? -1 : 1) * (0.6 + var1.nextDouble() * 0.4));
                  var0x.setPaint(new GradientPaint(0.0F, 0.0F, new Color(8050762), 14.0F, 0.0F, new Color(2787882)));
                  var0x.fill(new java.awt.geom.Ellipse2D.Double(0.0, -4.0, 15.0, 8.0));
                  var0x.setTransform(var7);
               }
            });
         case "flower_pink":
            return flower(16747192, 16769262, 16765498);
         case "flower_white":
            return flower(15790335, 16777215, 16762938);
         case "flower_yellow":
            return flower(16765498, 16774304, 14704682);
         case "flower_blue":
            return flower(6994175, 14217471, 16774304);
         case "heart_glasses":
            return Tex2.img(256, 96, var0x -> {
               for (int var1 = 0; var1 < 2; var1++) {
                  double var2 = var1 == 0 ? 64.0 : 192.0;
                  Path2D var4 = CapeArt2.heartPath(var2, 50.0, 104.0);
                  var0x.setColor(new Color(14684511));
                  var0x.fill(var4);
                  Path2D var5 = CapeArt2.heartPath(var2, 50.0, 84.0);
                  var0x.setPaint(new GradientPaint(0.0F, 10.0F, new Color(16743088), 0.0F, 90.0F, new Color(12587082)));
                  var0x.fill(var5);
                  var0x.setColor(Capes.col(16777215, 0.7));
                  var0x.fill(new java.awt.geom.Ellipse2D.Double(var2 - 30.0, 24.0, 18.0, 10.0));
               }

               var0x.setColor(new Color(14684511));
               var0x.fillRect(110, 30, 36, 8);
            });
         case "star_glasses":
            return Tex2.img(256, 96, var0x -> {
               for (int var1 = 0; var1 < 2; var1++) {
                  double var2 = var1 == 0 ? 64.0 : 192.0;
                  Path2D var4 = starPath(var2, 50.0, 48.0, 22.0);
                  var0x.setColor(new Color(15245328));
                  var0x.fill(var4);
                  var0x.setPaint(new GradientPaint(0.0F, 10.0F, new Color(16774304), 0.0F, 90.0F, new Color(16756768)));
                  var0x.fill(starPath(var2, 51.0, 40.0, 18.0));
                  var0x.setColor(Capes.col(8010240, 0.5));
                  var0x.fill(new java.awt.geom.Ellipse2D.Double(var2 - 16.0, 36.0, 32.0, 28.0));
               }

               var0x.setColor(new Color(15245328));
               var0x.fillRect(104, 40, 48, 7);
            });
         case "monocle_glass":
            return Tex2.img(
               64,
               64,
               var0x -> {
                  var0x.setPaint(
                     new RadialGradientPaint(24.0F, 22.0F, 40.0F, new float[]{0.0F, 1.0F}, new Color[]{Capes.col(16777215, 0.55), Capes.col(12116223, 0.25)})
                  );
                  var0x.fill(new java.awt.geom.Ellipse2D.Double(2.0, 2.0, 60.0, 60.0));
                  var0x.setColor(Capes.col(16777215, 0.8));
                  var0x.setStroke(new BasicStroke(3.0F, 1, 1));
                  var0x.draw(new java.awt.geom.Arc2D.Double(12.0, 12.0, 40.0, 40.0, 100.0, 70.0, 0));
               }
            );
         case "mustache":
            return Tex2.img(128, 48, var0x -> {
               Double var1 = new Double();
               var1.moveTo(64.0, 14.0);
               var1.curveTo(80.0, 2.0, 96.0, 8.0, 104.0, 22.0);
               var1.curveTo(110.0, 30.0, 122.0, 30.0, 126.0, 18.0);
               var1.curveTo(126.0, 38.0, 106.0, 46.0, 90.0, 38.0);
               var1.curveTo(80.0, 34.0, 70.0, 30.0, 64.0, 26.0);
               var1.curveTo(58.0, 30.0, 48.0, 34.0, 38.0, 38.0);
               var1.curveTo(22.0, 46.0, 2.0, 38.0, 2.0, 18.0);
               var1.curveTo(6.0, 30.0, 18.0, 30.0, 24.0, 22.0);
               var1.curveTo(32.0, 8.0, 48.0, 2.0, 64.0, 14.0);
               var0x.setPaint(new GradientPaint(0.0F, 4.0F, new Color(6963752), 0.0F, 44.0F, new Color(2758154)));
               var0x.fill(var1);
               var0x.setClip(var1);
               var0x.setColor(Capes.col(11567192, 0.35));
               var0x.setStroke(new BasicStroke(1.2F));

               for (int var2 = 0; var2 < 26; var2++) {
                  double var3 = 6.0 + var2 * 4.6;
                  var0x.draw(new java.awt.geom.QuadCurve2D.Double(64.0 + (var3 - 64.0) * 0.3, 14.0, var3, 20.0, var3 + (var3 < 64.0 ? -6 : 6), 40.0));
               }
            });
         case "visor":
            return Tex2.img(
               256,
               64,
               var0x -> {
                  var0x.setPaint(
                     new LinearGradientPaint(
                        0.0F,
                        0.0F,
                        0.0F,
                        64.0F,
                        new float[]{0.0F, 0.5F, 1.0F},
                        new Color[]{Capes.col(1706554, 0.92), Capes.col(2759274, 0.85), Capes.col(656928, 0.95)}
                     )
                  );
                  var0x.fill(new java.awt.geom.RoundRectangle2D.Double(0.0, 4.0, 256.0, 56.0, 30.0, 30.0));
                  var0x.setColor(Capes.col(2811647, 0.25));

                  for (byte var1 = 8; var1 < 60; var1 += 4) {
                     var0x.fillRect(10, var1, 236, 1);
                  }

                  var0x.setPaint(new GradientPaint(0.0F, 4.0F, Capes.col(16777215, 0.35), 0.0F, 26.0F, Capes.col(16777215, 0.0)));
                  var0x.fill(new java.awt.geom.RoundRectangle2D.Double(8.0, 6.0, 240.0, 20.0, 16.0, 16.0));
               }
            );
         case "mask_masq":
            return Tex2.img(
               256,
               128,
               var0x -> {
                  Double var1 = new Double();
                  var1.moveTo(128.0, 40.0);
                  var1.curveTo(150.0, 20.0, 220.0, 10.0, 252.0, 30.0);
                  var1.curveTo(250.0, 70.0, 220.0, 110.0, 170.0, 104.0);
                  var1.curveTo(150.0, 100.0, 138.0, 86.0, 128.0, 80.0);
                  var1.curveTo(118.0, 86.0, 106.0, 100.0, 86.0, 104.0);
                  var1.curveTo(36.0, 110.0, 6.0, 70.0, 4.0, 30.0);
                  var1.curveTo(36.0, 10.0, 106.0, 20.0, 128.0, 40.0);
                  var1.closePath();
                  var0x.setPaint(
                     new LinearGradientPaint(
                        0.0F, 0.0F, 256.0F, 128.0F, new float[]{0.0F, 0.5F, 1.0F}, new Color[]{new Color(5905034), new Color(9054920), new Color(3803754)}
                     )
                  );
                  var0x.fill(var1);
                  var0x.setClip(var1);
                  var0x.setColor(Capes.col(16765562, 0.5));
                  var0x.setStroke(new BasicStroke(2.0F));

                  for (int var2 = 0; var2 < 6; var2++) {
                     var0x.draw(new java.awt.geom.QuadCurve2D.Double(0.0, 20 + var2 * 18, 128.0, 60 + var2 * 10, 256.0, 20 + var2 * 18));
                  }

                  var0x.setClip(null);
                  var0x.setColor(new Color(16765562));
                  var0x.setStroke(new BasicStroke(4.0F));
                  var0x.draw(var1);
                  var0x.setComposite(AlphaComposite.Clear);
                  var0x.fill(new java.awt.geom.Ellipse2D.Double(56.0, 44.0, 50.0, 30.0));
                  var0x.fill(new java.awt.geom.Ellipse2D.Double(150.0, 44.0, 50.0, 30.0));
                  var0x.setComposite(AlphaComposite.SrcOver);
                  var0x.setColor(new Color(16765562));
                  var0x.setStroke(new BasicStroke(3.0F));
                  var0x.draw(new java.awt.geom.Ellipse2D.Double(56.0, 44.0, 50.0, 30.0));
                  var0x.draw(new java.awt.geom.Ellipse2D.Double(150.0, 44.0, 50.0, 30.0));
                  Random var4 = new Random(4L);

                  for (int var3 = 0; var3 < 14; var3++) {
                     var0x.setColor(Capes.col(16777215, 0.8));
                     var0x.fill(new java.awt.geom.Ellipse2D.Double(20.0 + var4.nextDouble() * 216.0, 20.0 + var4.nextDouble() * 80.0, 3.0, 3.0));
                  }
               }
            );
         case "feather_plume":
            return Tex2.feather(16777215, 14721279, 9054920, 16777215, "none", false);
         case "shield_face":
            return Tex2.img(128, 128, var0x -> {
               var0x.setPaint(new RadialGradientPaint(50.0F, 44.0F, 80.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(3828448), new Color(928378)}));
               var0x.fillRect(0, 0, 128, 128);
               var0x.setColor(new Color(15263984));
               Double var1 = new Double();
               var1.moveTo(64.0, 0.0);
               var1.lineTo(76.0, 0.0);
               var1.lineTo(76.0, 128.0);
               var1.lineTo(52.0, 128.0);
               var1.lineTo(52.0, 0.0);
               var1.closePath();
               var0x.fill(new java.awt.geom.Rectangle2D.Double(54.0, 0.0, 20.0, 128.0));
               var0x.fill(new java.awt.geom.Rectangle2D.Double(0.0, 54.0, 128.0, 20.0));
               var0x.setPaint(new RadialGradientPaint(64.0F, 64.0F, 22.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(16774328), new Color(13145626)}));
               var0x.fill(new java.awt.geom.Ellipse2D.Double(42.0, 42.0, 44.0, 44.0));
               var0x.setColor(new Color(9067018));
               var0x.setStroke(new BasicStroke(2.0F));
               var0x.draw(new java.awt.geom.Ellipse2D.Double(42.0, 42.0, 44.0, 44.0));
               Art.brick(var0x, 48.0, 44.0, 32.0, new Color(14876683));
            });
         case "guitar_body":
            return Tex2.img(
               128,
               128,
               var0x -> {
                  var0x.setPaint(
                     new RadialGradientPaint(
                        64.0F, 60.0F, 70.0F, new float[]{0.0F, 0.5F, 1.0F}, new Color[]{new Color(16765562), new Color(14704682), new Color(8002058)}
                     )
                  );
                  var0x.fillRect(0, 0, 128, 128);
                  var0x.setColor(new Color(1706500));
                  var0x.fill(new java.awt.geom.Ellipse2D.Double(48.0, 40.0, 32.0, 32.0));
                  var0x.setColor(new Color(15259824));
                  var0x.setStroke(new BasicStroke(2.5F));
                  var0x.draw(new java.awt.geom.Ellipse2D.Double(44.0, 36.0, 40.0, 40.0));
                  var0x.setColor(new Color(2759178));
                  var0x.fillRect(44, 92, 40, 8);
               }
            );
         case "strings":
            return Tex2.pix(16, 64, (var0x, var2, var4, var5) -> var4 % 4 == 1 ? -1513232 : 16777215);
         case "teddy_face":
            return Tex2.img(64, 64, var0x -> {
               var0x.setColor(new Color(11565640));
               var0x.fillRect(0, 0, 64, 64);
               var0x.setColor(new Color(15255704));
               var0x.fill(new java.awt.geom.Ellipse2D.Double(18.0, 30.0, 28.0, 22.0));
               var0x.setColor(new Color(1707528));
               var0x.fill(new java.awt.geom.Ellipse2D.Double(16.0, 18.0, 9.0, 10.0));
               var0x.fill(new java.awt.geom.Ellipse2D.Double(39.0, 18.0, 9.0, 10.0));
               var0x.fill(new java.awt.geom.Ellipse2D.Double(27.0, 32.0, 10.0, 7.0));
               var0x.setStroke(new BasicStroke(2.0F, 1, 1));
               var0x.draw(new java.awt.geom.QuadCurve2D.Double(26.0, 44.0, 32.0, 49.0, 38.0, 44.0));
               var0x.setColor(Color.WHITE);
               var0x.fill(new java.awt.geom.Ellipse2D.Double(18.0, 19.0, 3.0, 3.0));
               var0x.fill(new java.awt.geom.Ellipse2D.Double(41.0, 19.0, 3.0, 3.0));
               var0x.setColor(Capes.col(16743066, 0.5));
               var0x.fill(new java.awt.geom.Ellipse2D.Double(8.0, 32.0, 9.0, 6.0));
               var0x.fill(new java.awt.geom.Ellipse2D.Double(47.0, 32.0, 9.0, 6.0));
            });
         case "fur_brown":
            return Tex2.pix(
               64, 64, (var0x, var2, var4, var5) -> 0xFF000000 | Capes.shade(11565640, 0.82 + 0.3 * Capes.fbm(var0x * 16.0, var2 * 16.0, 0, 0, 3, 6))
            );
         case "rocket_body":
            return Tex2.pix(64, 128, (var0x, var2, var4, var5) -> {
               int var6 = 16054010;
               if (var2 > 0.3 && var2 < 0.36) {
                  var6 = 14876683;
               }

               if (var2 > 0.75 && var2 < 0.8) {
                  var6 = 14876683;
               }

               double var7 = 0.75 + 0.3 * Math.sin(var0x * Math.PI);
               if (Math.abs(var0x * 8.0 % 1.0 - 0.5) > 0.48) {
                  var7 *= 0.9;
               }

               return 0xFF000000 | Capes.shade(var6, var7);
            });
         case "window":
            return Tex2.img(32, 32, var0x -> {
               var0x.setColor(new Color(10134192));
               var0x.fill(new java.awt.geom.Ellipse2D.Double(0.0, 0.0, 32.0, 32.0));
               var0x.setPaint(new RadialGradientPaint(12.0F, 12.0F, 16.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(12120319), new Color(1727144)}));
               var0x.fill(new java.awt.geom.Ellipse2D.Double(4.0, 4.0, 24.0, 24.0));
            });
         case "sword_blade":
            return Tex2.img(
               32,
               128,
               var0x -> {
                  var0x.setPaint(
                     new LinearGradientPaint(
                        0.0F,
                        0.0F,
                        32.0F,
                        0.0F,
                        new float[]{0.0F, 0.5F, 0.52F, 1.0F},
                        new Color[]{new Color(14679039), new Color(8382719), new Color(2792152), new Color(1727130)}
                     )
                  );
                  var0x.fillRect(0, 0, 32, 128);
                  var0x.setColor(Capes.col(16777215, 0.8));
                  var0x.fillRect(14, 0, 3, 128);
                  Random var1 = new Random(3L);
                  var0x.setColor(Capes.col(14745599, 0.9));

                  for (int var2 = 0; var2 < 6; var2++) {
                     double var3 = 20 + var2 * 16;
                     var0x.draw(new java.awt.geom.Line2D.Double(8.0, var3, 12.0 + var1.nextDouble() * 8.0, var3 + 6.0));
                  }
               }
            );
         case "arrow_fletch":
            return Tex2.img(32, 64, var0x -> {
               Double var1 = new Double();
               var1.moveTo(16.0, 0.0);
               var1.lineTo(30.0, 20.0);
               var1.lineTo(30.0, 62.0);
               var1.lineTo(16.0, 48.0);
               var1.lineTo(2.0, 62.0);
               var1.lineTo(2.0, 20.0);
               var1.closePath();
               var0x.setPaint(new GradientPaint(0.0F, 0.0F, new Color(16777215), 0.0F, 64.0F, new Color(14696538)));
               var0x.fill(var1);
            });
         case "petal":
            return Tex2.img(32, 32, var0x -> {
               Double var1 = new Double();
               var1.moveTo(16.0, 30.0);
               var1.curveTo(30.0, 22.0, 28.0, 6.0, 19.0, 2.0);
               var1.lineTo(16.0, 7.0);
               var1.lineTo(13.0, 2.0);
               var1.curveTo(4.0, 6.0, 2.0, 22.0, 16.0, 30.0);
               var0x.setPaint(new GradientPaint(0.0F, 30.0F, new Color(16748468), 0.0F, 2.0F, new Color(16773366)));
               var0x.fill(var1);
            });
         case "note":
            return Tex2.img(64, 64, var0x -> {
               var0x.setColor(Color.WHITE);
               var0x.fill(new java.awt.geom.Ellipse2D.Double(6.0, 40.0, 20.0, 15.0));
               var0x.fill(new java.awt.geom.Ellipse2D.Double(36.0, 34.0, 20.0, 15.0));
               var0x.setStroke(new BasicStroke(4.0F));
               var0x.drawLine(24, 47, 24, 10);
               var0x.drawLine(54, 41, 54, 4);
               var0x.setStroke(new BasicStroke(8.0F));
               var0x.drawLine(24, 12, 54, 6);
            });
         case "bubble":
            return Tex2.img(
               64,
               64,
               var0x -> {
                  var0x.setPaint(
                     new RadialGradientPaint(
                        32.0F,
                        32.0F,
                        30.0F,
                        new float[]{0.0F, 0.75F, 0.95F, 1.0F},
                        new Color[]{Capes.col(12120319, 0.08), Capes.col(12118271, 0.25), Capes.col(16777215, 0.8), Capes.col(16777215, 0.0)}
                     )
                  );
                  var0x.fill(new java.awt.geom.Ellipse2D.Double(1.0, 1.0, 62.0, 62.0));
                  var0x.setColor(Capes.col(16777215, 0.9));
                  var0x.fill(new java.awt.geom.Ellipse2D.Double(16.0, 13.0, 12.0, 8.0));
                  var0x.setColor(Capes.col(16747232, 0.25));
                  var0x.setStroke(new BasicStroke(2.0F));
                  var0x.draw(new java.awt.geom.Arc2D.Double(6.0, 6.0, 52.0, 52.0, 200.0, 80.0, 0));
               }
            );
         case "leaf":
            return Tex2.img(48, 48, var0x -> {
               Double var1 = new Double();
               var1.moveTo(4.0, 44.0);
               var1.curveTo(4.0, 14.0, 24.0, 4.0, 44.0, 4.0);
               var1.curveTo(44.0, 26.0, 30.0, 44.0, 4.0, 44.0);
               var0x.setPaint(new GradientPaint(4.0F, 44.0F, new Color(14700570), 44.0F, 4.0F, new Color(16760890)));
               var0x.fill(var1);
               var0x.setColor(Capes.col(8006154, 0.7));
               var0x.setStroke(new BasicStroke(1.5F));
               var0x.draw(new java.awt.geom.QuadCurve2D.Double(4.0, 44.0, 22.0, 24.0, 40.0, 8.0));
            });
         case "rune_circle":
            return Tex2.img(256, 256, var0x -> {
               var0x.translate(128, 128);
               int var1 = 13142783;

               for (int var2 = 4; var2 >= 1; var2--) {
                  var0x.setColor(Capes.col(var1, 0.1 * (5 - var2)));
                  var0x.setStroke(new BasicStroke(var2 * 3.0F));
                  var0x.draw(new java.awt.geom.Ellipse2D.Double(-120.0, -120.0, 240.0, 240.0));
                  var0x.draw(new java.awt.geom.Ellipse2D.Double(-96.0, -96.0, 192.0, 192.0));
               }

               var0x.setColor(Capes.col(15785215, 0.95));
               var0x.setStroke(new BasicStroke(2.5F));
               var0x.draw(new java.awt.geom.Ellipse2D.Double(-120.0, -120.0, 240.0, 240.0));
               var0x.draw(new java.awt.geom.Ellipse2D.Double(-96.0, -96.0, 192.0, 192.0));
               var0x.draw(new java.awt.geom.Ellipse2D.Double(-40.0, -40.0, 80.0, 80.0));
               Random var8 = new Random(7L);

               for (int var3 = 0; var3 < 20; var3++) {
                  AffineTransform var4 = var0x.getTransform();
                  var0x.rotate((Math.PI * 2) * var3 / 20.0);
                  var0x.translate(0, -108);

                  for (int var5 = 0; var5 < 3; var5++) {
                     var0x.drawLine(var8.nextInt(3) * 5 - 5, var8.nextInt(3) * 5 - 5, var8.nextInt(3) * 5 - 5, var8.nextInt(3) * 5 - 5);
                  }

                  var0x.setTransform(var4);
               }

               for (int var9 = 0; var9 < 2; var9++) {
                  Double var10 = new Double();

                  for (int var11 = 0; var11 < 3; var11++) {
                     double var6 = var9 * Math.PI / 3.0 + var11 * Math.PI * 2.0 / 3.0;
                     if (var11 == 0) {
                        var10.moveTo(Math.cos(var6) * 94.0, Math.sin(var6) * 94.0);
                     } else {
                        var10.lineTo(Math.cos(var6) * 94.0, Math.sin(var6) * 94.0);
                     }
                  }

                  var10.closePath();
                  var0x.draw(var10);
               }
            });
         case "glyph":
            return Tex2.img(32, 32, var0x -> {
               var0x.setColor(Capes.col(15255807, 1.0));
               var0x.setStroke(new BasicStroke(3.0F, 1, 1));
               var0x.drawLine(8, 6, 8, 26);
               var0x.drawLine(8, 16, 24, 8);
               var0x.drawLine(16, 12, 24, 26);
            });
         case "ring_glow":
            return Tex2.pix(256, 16, (var0x, var2, var4, var5) -> Tex2.argb(16777215, Math.pow(1.0 - Math.abs(var2 - 0.5) * 2.0, 1.6)));
         case "void_orb":
            return Tex2.img(
               64,
               64,
               var0x -> {
                  var0x.setPaint(
                     new RadialGradientPaint(
                        32.0F,
                        32.0F,
                        32.0F,
                        new float[]{0.0F, 0.35F, 0.55F, 1.0F},
                        new Color[]{new Color(655378), new Color(1703984), Capes.col(11553535, 0.9), Capes.col(6953672, 0.0)}
                     )
                  );
                  var0x.fillRect(0, 0, 64, 64);
               }
            );
         default:
            return null;
      }
   }

   static void star(Graphics2D var0, double var1, double var3, double var5, int var7) {
      var0.setColor(new Color(var7));
      var0.fill(starPath(var1, var3, var5, var5 * 0.45));
   }

   static Path2D starPath(double var0, double var2, double var4, double var6) {
      Double var8 = new Double();

      for (int var9 = 0; var9 < 10; var9++) {
         double var10 = (-Math.PI / 2) + var9 * Math.PI / 5.0;
         double var12 = var9 % 2 == 0 ? var4 : var6;
         if (var9 == 0) {
            var8.moveTo(var0 + Math.cos(var10) * var12, var2 + Math.sin(var10) * var12);
         } else {
            var8.lineTo(var0 + Math.cos(var10) * var12, var2 + Math.sin(var10) * var12);
         }
      }

      var8.closePath();
      return var8;
   }

   static CTex.T flower(int var0, int var1, int var2) {
      return Tex2.img(64, 64, var3 -> {
         var3.translate(32, 32);

         for (int var4 = 0; var4 < 6; var4++) {
            AffineTransform var5 = var3.getTransform();
            var3.rotate((Math.PI * 2) * var4 / 6.0);
            var3.setPaint(new GradientPaint(0.0F, 0.0F, new Color(var0), 0.0F, -28.0F, new Color(var1)));
            var3.fill(new java.awt.geom.Ellipse2D.Double(-8.0, -30.0, 16.0, 28.0));
            var3.setTransform(var5);
         }

         var3.setPaint(new RadialGradientPaint(-2.0F, -2.0F, 10.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(16774848), new Color(var2)}));
         var3.fill(new java.awt.geom.Ellipse2D.Double(-8.0, -8.0, 16.0, 16.0));
      });
   }
}
