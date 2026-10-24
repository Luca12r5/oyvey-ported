package dev.lego.cosmetic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;
import java.util.Random;

final class Cos3Tex {
   private Cos3Tex() {
   }

   static CTex.T paint(String var0) {
      switch (var0) {
         case "@skin":
            return skin();
         case "c3_beam":
            return Tex2.pix(8, 8, (var0x, var2, var4, var5) -> Tex2.argb(16777215, 0.34));
         case "c3_metal":
            return Tex2.pix(
               64,
               64,
               (var0x, var2, var4, var5) -> 0xFF000000
                  | Capes.shade(Capes.mix(16777215, 11844294, var2 * 0.8), 0.93 + 0.07 * Capes.hash(var4 / 7, var5, 11) + 0.04 * Math.sin(var0x * 9.0))
            );
         case "c3_crystal":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = 0.5 + 0.5 * Math.sin(var0x * Math.PI * 2.0 * 3.0 + var2 * 2.0);
               int var8 = Capes.mix(16777215, 10134728, 0.25 + 0.55 * var2 * (0.6 + 0.4 * var6));
               double var9 = Math.exp(-Math.pow((var0x * 1.3 - var2 * 0.7 - 0.2) / 0.05, 2.0)) * 0.7;
               return 0xFF000000 | Capes.add(var8, 16777215, var9);
            });
         case "c3_scale":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = var0x * 8.0;
               double var8 = var2 * 8.0;
               double var10 = Math.floor(var8);
               double var12 = var6 + (var10 % 2.0 == 0.0 ? 0.0 : 0.5);
               double var14 = var12 - Math.floor(var12) - 0.5;
               double var16 = var8 - var10;
               double var18 = Math.hypot(var14, (var16 - 0.1) * 1.1);
               double var20 = var18 < 0.5 ? 1.0 - var18 * 0.6 : 0.62;
               double var22 = var18 > 0.42 && var18 < 0.5 ? 0.75 : 1.0;
               return 0xFF000000 | Capes.shade(15921906, var20 * var22 * (0.95 + 0.08 * Capes.hash(var4, var5, 5)));
            });
         case "c3_fur":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 24.0, var2 * 3.0, 24, 3, 3, 8);
               return 0xFF000000 | Capes.shade(16185078, 0.78 + 0.3 * var6);
            });
         case "c3_plume":
            return Tex2.pix(32, 64, (var0x, var2, var4, var5) -> {
               double var6 = 0.85 + 0.15 * Math.sin(var2 * 30.0 + Math.abs(var0x - 0.5) * 12.0);
               double var8 = Math.abs(var0x - 0.5) < 0.05 ? 0.7 : 1.0;
               return 0xFF000000 | Capes.shade(16185078, var6 * var8);
            });
         case "c3_lacquer":
            return Tex2.pix(32, 128, (var0x, var2, var4, var5) -> {
               double var6 = Math.exp(-Math.pow((var0x - 0.3) / 0.12, 2.0)) * 0.5 + 0.55 + 0.1 * Math.sin(var2 * 3.0);
               return 0xFF000000 | Capes.shade(16777215, Math.min(1.0, var6));
            });
         case "c3_jet":
            return Tex2.img(64, 64, var0x -> {
               var0x.setPaint(new GradientPaint(0.0F, 0.0F, new Color(16777215), 64.0F, 0.0F, new Color(12896464)));
               var0x.fillRect(0, 0, 64, 64);
               var0x.setColor(Capes.col(0, 0.25));
               var0x.setStroke(new BasicStroke(1.5F));
               var0x.drawLine(0, 20, 64, 20);
               var0x.drawLine(0, 44, 64, 44);

               for (int var1 = 0; var1 < 4; var1++) {
                  var0x.fill(new Double(6 + var1 * 16, 24.0, 3.0, 3.0));
               }
            });
         case "c3_exhaust":
            return Tex2.pix(
               16,
               64,
               (var0x, var2, var4, var5) -> Tex2.argb(
                  Capes.mix(16777215, 16777215, 0.0), Math.pow(1.0 - var2, 1.6) * (0.75 + 0.25 * Math.sin(var0x * Math.PI * 6.0))
               )
            );
         case "c3_eye":
            return Tex2.img(32, 32, var0x -> {
               var0x.setColor(new Color(1315866));
               var0x.fillRect(0, 0, 32, 32);
               var0x.setColor(Color.WHITE);
               var0x.fill(new Double(5.0, 5.0, 11.0, 11.0));
               var0x.fill(new Double(19.0, 19.0, 5.0, 5.0));
            });
         case "c3_grille":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = var4 % 6 - 2.5;
               double var8 = var5 % 6 - 2.5;
               return 0xFF000000 | (var6 * var6 + var8 * var8 < 3.5 ? 657934 : Capes.shade(3027000, 0.9 + 0.2 * var2));
            });
         case "c3_eq":
            return Tex2.pix(256, 32, (var0x, var2, var4, var5) -> {
               int var6 = var4 / 8;
               double var7 = 0.3 + 0.7 * Capes.hash(var6, 1, 9);
               boolean var9 = 1.0 - var2 < var7 && var4 % 8 < 6 && var5 % 4 < 3;
               int var10 = Capes.hsv(var0x, 0.85, 1.0);
               return var9 ? 0xFF000000 | var10 : Tex2.argb(var10, 0.18);
            });
         case "c3_band_bolt":
            return band(1222730, 678442, 12124010, 0);
         case "c3_band_fire":
            return band(13115418, 6949384, 16756778, 1);
         case "c3_band_ice":
            return band(2783960, 929386, 14219007, 2);
         case "c3_planet_gas":
            return Tex2.pix(128, 64, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 6.0, var2 * 3.0, 6, 0, 3, 21) - 0.5;
               double var8 = Math.sin((var2 + var6 * 0.12) * Math.PI * 9.0);
               int var10 = Capes.ramp(0.5 + 0.5 * var8, 12083242, 15249520, 16773328);
               double var11 = Math.exp(-(Math.pow((var0x - 0.3) / 0.06, 2.0) + Math.pow((var2 - 0.62) / 0.05, 2.0)));
               return 0xFF000000 | Capes.mix(var10, 13123610, var11);
            });
         case "c3_planet_ice":
            return Tex2.pix(128, 64, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 8.0, var2 * 4.0, 8, 0, 4, 33);
               int var8 = Capes.ramp(var6, 1723034, 3840744, 10152191, 16777215);
               if (var2 < 0.12 || var2 > 0.88) {
                  var8 = Capes.mix(var8, 16777215, 0.8);
               }

               return 0xFF000000 | var8;
            });
         case "c3_planet_lava":
            return Tex2.pix(128, 64, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 10.0, var2 * 5.0, 10, 0, 4, 44);
               double var8 = Math.exp(-Math.pow((var6 - 0.5) / 0.035, 2.0));
               int var10 = Capes.mix(2757648, 4858904, var6);
               return 0xFF000000 | Capes.add(var10, 16742938, var8 * 1.4);
            });
         case "c3_ring":
            return Tex2.pix(256, 16, (var0x, var2, var4, var5) -> {
               double var6 = 0.55 + 0.45 * Math.sin(var2 * 40.0 + Math.sin(var2 * 11.0) * 2.0);
               double var8 = Math.min(var2, 1.0 - var2) * 6.0;
               return Tex2.argb(Capes.ramp(var2, 15257760, 16774362, 13148272), Math.min(1.0, var8) * (0.45 + 0.5 * var6));
            });
         case "c3_glass":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Math.exp(-Math.pow((var0x - 0.18) / 0.03, 2.0)) * Math.exp(-Math.pow((var2 - 0.35) / 0.12, 2.0));
               double var8 = Math.exp(-Math.pow((var0x - 0.24) / 0.012, 2.0)) * Math.exp(-Math.pow((var2 - 0.42) / 0.18, 2.0));
               double var10 = Math.pow(Math.abs(var2 - 0.5) * 2.0, 6.0) * 0.3;
               return Tex2.argb(15268095, 0.1 + var10 + var6 * 0.7 + var8 * 0.6);
            });
         case "c3_lglass":
            return Tex2.pix(32, 32, (var0x, var2, var4, var5) -> {
               double var6 = Math.hypot(var0x - 0.5, var2 - 0.6);
               return Tex2.argb(Capes.mix(16774336, 16751146, var6 * 1.6), 0.55 + 0.4 * Math.exp(-var6 * 4.0));
            });
         case "c3_wrap":
            return Tex2.pix(32, 128, (var0x, var2, var4, var5) -> {
               double var6 = ((var0x * 2.0 + var2 * 8.0) % 1.0 + 1.0) % 1.0;
               double var8 = ((var0x * 2.0 - var2 * 8.0) % 1.0 + 1.0) % 1.0;
               double var10 = Math.min(Math.abs(var6 - 0.5), Math.abs(var8 - 0.5));
               boolean var12 = Math.abs(var6 - 0.5) < 0.22 && Math.abs(var8 - 0.5) < 0.22;
               return 0xFF000000 | (var12 ? Capes.shade(15788248, 0.9 + 0.1 * var2) : Capes.shade(1709088, 0.8 + var10));
            });
         case "c3_blade":
            return Tex2.pix(16, 128, (var0x, var2, var4, var5) -> {
               double var6 = 0.55 + 0.08 * Math.sin(var2 * 50.0) + 0.04 * Math.sin(var2 * 131.0);
               int var8 = var0x > var6 ? Capes.mix(16054527, 16777215, (var0x - var6) * 3.0) : Capes.mix(9082022, 13160668, var0x / var6);
               return 0xFF000000 | var8;
            });
         case "c3_cover":
            return Tex2.img(64, 64, var0x -> {
               var0x.setPaint(new GradientPaint(0.0F, 0.0F, new Color(5905018), 64.0F, 64.0F, new Color(2755136)));
               var0x.fillRect(0, 0, 64, 64);
               var0x.setColor(new Color(15910986));
               var0x.setStroke(new BasicStroke(2.2F));
               var0x.draw(new java.awt.geom.RoundRectangle2D.Double(5.0, 5.0, 54.0, 54.0, 8.0, 8.0));
               var0x.draw(new Double(20.0, 20.0, 24.0, 24.0));
               var0x.fill(Tex3.starPath(32.0, 32.0, 9.0, 4.0));

               for (int var1 = 0; var1 < 4; var1++) {
                  double var2 = var1 % 2 == 0 ? 8.0 : 56.0;
                  double var4 = var1 < 2 ? 8.0 : 56.0;
                  var0x.fill(new Double(var2 - 3.0, var4 - 3.0, 6.0, 6.0));
               }
            });
         case "c3_pages":
            return Tex2.pix(16, 64, (var0x, var2, var4, var5) -> 0xFF000000 | Capes.shade(16050376, var5 % 2 == 0 ? 0.86 : 1.0));
         case "c3_page":
            return Tex2.img(64, 64, var0x -> {
               var0x.setColor(new Color(16314068));
               var0x.fillRect(0, 0, 64, 64);
               Random var1 = new Random(4L);
               var0x.setStroke(new BasicStroke(1.6F, 1, 1));

               for (int var2 = 0; var2 < 8; var2++) {
                  double var3 = 6.0;

                  while (var3 < 56.0) {
                     double var5 = 3 + var1.nextInt(8);
                     var0x.setColor(Capes.col(var2 == 0 ? 10103496 : 4864602, 0.8));
                     var0x.draw(new java.awt.geom.Line2D.Double(var3, 8.0 + var2 * 6.5, Math.min(58.0, var3 + var5), 8.0 + var2 * 6.5));
                     var3 += var5 + 3.0;
                  }
               }

               var0x.setColor(Capes.col(10103496, 0.9));
               var0x.draw(new Double(38.0, 42.0, 16.0, 16.0));
            });
         case "c3_glyphs":
            return Tex2.img(
               256,
               64,
               var0x -> {
                  var0x.setStroke(new BasicStroke(4.5F, 1, 1));

                  for (int var1 = 0; var1 < 4; var1++) {
                     AffineTransform var2 = var0x.getTransform();
                     var0x.translate(var1 * 64 + 32, 32);
                     var0x.setPaint(
                        new RadialGradientPaint(0.0F, 0.0F, 30.0F, new float[]{0.0F, 1.0F}, new Color[]{Capes.col(16777215, 0.35), Capes.col(16777215, 0.0)})
                     );
                     var0x.fill(new Double(-30.0, -30.0, 60.0, 60.0));
                     var0x.setColor(Color.WHITE);
                     glyph(var0x, var1);
                     var0x.setTransform(var2);
                  }
               }
            );
         case "c3_splash":
            return Tex2.pix(128, 32, (var0x, var2, var4, var5) -> {
               double var6 = 0.25 + 0.2 * Math.sin(var0x * Math.PI * 2.0 * 9.0) + 0.12 * Math.sin(var0x * Math.PI * 2.0 * 23.0 + 1.0);
               if (var2 < var6) {
                  return Tex2.argb(16777215, 0.0);
               } else {
                  double var8 = 0.85 - (var2 - var6) * 0.7;
                  int var10 = Capes.mix(16777215, 4897023, (var2 - var6) * 1.6);
                  return Tex2.argb(var10, var8);
               }
            });
         case "c3_ripple":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Math.hypot(var0x - 0.5, var2 - 0.5) * 2.0;
               double var8 = Math.exp(-Math.pow((var6 - 0.86) / 0.06, 2.0)) + 0.5 * Math.exp(-Math.pow((var6 - 0.62) / 0.05, 2.0));
               return Tex2.argb(Capes.mix(16777215, 8048895, var6), Math.min(1.0, var8));
            });
         case "c3_drop":
            return Tex2.img(
               32,
               48,
               var0x -> {
                  java.awt.geom.Path2D.Double var1 = new java.awt.geom.Path2D.Double();
                  var1.moveTo(16.0, 2.0);
                  var1.curveTo(22.0, 16.0, 30.0, 24.0, 30.0, 32.0);
                  var1.curveTo(30.0, 42.0, 22.0, 46.0, 16.0, 46.0);
                  var1.curveTo(10.0, 46.0, 2.0, 42.0, 2.0, 32.0);
                  var1.curveTo(2.0, 24.0, 10.0, 16.0, 16.0, 2.0);
                  var1.closePath();
                  var0x.setPaint(
                     new RadialGradientPaint(16.0F, 32.0F, 16.0F, new float[]{0.0F, 1.0F}, new Color[]{Capes.col(14218495, 0.9), Capes.col(2792191, 0.85)})
                  );
                  var0x.fill(var1);
                  var0x.setColor(Capes.col(16777215, 0.95));
                  var0x.fill(new Double(9.0, 26.0, 6.0, 9.0));
               }
            );
         case "c3_hex":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = (var0x - 0.5) * 2.0;
               double var8 = (var2 - 0.5) * 2.0;
               double var10 = Math.max(Math.abs(var6) * 0.866 + Math.abs(var8) * 0.5, Math.abs(var8));
               double var12 = Math.exp(-Math.pow((var10 - 0.86) / 0.07, 2.0));
               double var14 = var10 < 0.9 ? 0.28 + 0.2 * (1.0 - var10) : 0.0;
               return Tex2.argb(16777215, Math.min(1.0, var14 + var12));
            });
         case "c3_ray":
            return Tex2.pix(8, 64, (var0x, var2, var4, var5) -> Tex2.argb(16777215, Math.pow(var2, 1.4) * (0.55 + 0.45 * Math.sin(var0x * Math.PI))));
         case "c3_visor":
            return Tex2.pix(128, 32, (var0x, var2, var4, var5) -> {
               double var6 = var5 % 3 == 0 ? 0.14 : 0.0;
               double var8 = Math.pow(Math.abs(var2 - 0.5) * 2.0, 4.0);
               double var10 = Math.exp(-Math.pow((var0x * 1.0 - var2 * 0.25 - 0.62) / 0.03, 2.0)) * 0.5;
               int var12 = Capes.mix(2808063, 12086015, var0x);
               return Tex2.argb(Capes.add(var12, 16777215, var10 + var8 * 0.6), 0.42 + var6 + var8 * 0.4 + var10);
            });
         case "c3_lens":
            return Tex2.pix(32, 32, (var0x, var2, var4, var5) -> {
               int var6 = Capes.ramp(var2, 3807834, 1444388, 525326);
               double var7 = Math.exp(-Math.pow((var0x * 0.8 + var2 * 0.6 - 0.55) / 0.05, 2.0)) * 0.45;
               return Tex2.argb(Capes.add(var6, 16777215, var7), 0.9);
            });
         case "c3_membrane":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Math.exp(-Math.pow(Math.sin(var0x * Math.PI * 5.0 + var2 * 2.0) / 0.12, 2.0)) * 0.5;
               return Tex2.argb(Capes.add(15263976, 16777215, var6) & 16777215, 0.88 + var6 * 0.2);
            });
         case "c3_parrotwing":
            return Tex2.img(
               64,
               32,
               var0x -> {
                  var0x.setPaint(
                     new LinearGradientPaint(
                        0.0F,
                        0.0F,
                        64.0F,
                        0.0F,
                        new float[]{0.0F, 0.45F, 0.75F, 1.0F},
                        new Color[]{new Color(2279592), new Color(2788072), new Color(6961880), new Color(15216714)}
                     )
                  );
                  var0x.fillRect(0, 0, 64, 32);
                  var0x.setStroke(new BasicStroke(1.0F));

                  for (int var1 = 0; var1 < 12; var1++) {
                     var0x.setColor(Capes.col(0, 0.22));
                     var0x.drawLine(8 + var1 * 5, 0, 14 + var1 * 5, 32);
                     var0x.setColor(Capes.col(16777215, 0.12));
                     var0x.drawLine(9 + var1 * 5, 0, 15 + var1 * 5, 32);
                  }

                  var0x.setColor(Capes.col(16769098, 0.9));
                  var0x.fillRect(0, 13, 18, 5);
               }
            );
         case "c3_flamegrad":
            return Tex2.pix(32, 64, (var0x, var2, var4, var5) -> {
               double var6 = 0.08 * Math.sin(var0x * Math.PI * 2.0 * 3.0 + var2 * 9.0);
               double var8 = Capes.clamp01(var2 + var6);
               int var10 = Capes.ramp(var8, 16777200, 16769146, 16777215, 16777215);
               return Tex2.argb(var10, Math.max(0.15, 1.0 - Math.pow(var8, 1.6) * 0.8));
            });
         case "c3_pumpkin":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Math.abs(Math.sin(var0x * Math.PI * 8.0));
               return 0xFF000000 | Capes.shade(16777215, 0.72 + 0.28 * Math.pow(var6, 0.5));
            });
         default:
            return null;
      }
   }

   static void glyph(Graphics2D var0, int var1) {
      java.awt.geom.Path2D.Double var2 = new java.awt.geom.Path2D.Double();
      switch (var1 % 4) {
         case 0:
            var2.moveTo(-14.0, 16.0);
            var2.lineTo(0.0, -16.0);
            var2.lineTo(14.0, 16.0);
            var2.moveTo(-8.0, 4.0);
            var2.lineTo(8.0, 4.0);
            var2.moveTo(0.0, -16.0);
            var2.lineTo(0.0, -22.0);
            break;
         case 1:
            var2.append(new Double(-12.0, -12.0, 24.0, 24.0), false);
            var2.moveTo(-18.0, 0.0);
            var2.lineTo(18.0, 0.0);
            var2.moveTo(0.0, 12.0);
            var2.lineTo(0.0, 22.0);
            break;
         case 2:
            var2.moveTo(-12.0, -16.0);
            var2.lineTo(12.0, -16.0);
            var2.lineTo(-12.0, 16.0);
            var2.lineTo(12.0, 16.0);
            var2.moveTo(-6.0, 0.0);
            var2.lineTo(6.0, 0.0);
            break;
         default:
            var2.moveTo(0.0, -18.0);
            var2.lineTo(14.0, 0.0);
            var2.lineTo(0.0, 18.0);
            var2.lineTo(-14.0, 0.0);
            var2.closePath();
            var2.moveTo(0.0, -8.0);
            var2.lineTo(0.0, 8.0);
      }

      var0.draw(var2);
   }

   private static CTex.T band(int var0, int var1, int var2, int var3) {
      return Tex2.img(256, 32, var4 -> {
         var4.setPaint(new GradientPaint(0.0F, 0.0F, new Color(Capes.mix(var0, 16777215, 0.12)), 0.0F, 32.0F, new Color(Capes.mix(var0, var1, 0.5))));
         var4.fillRect(0, 0, 256, 32);

         for (byte var5 = 0; var5 < 32; var5 += 2) {
            var4.setColor(Capes.col(0, 0.06));
            var4.drawLine(0, var5, 256, var5);
         }

         var4.setColor(Capes.col(var1, 0.9));
         var4.fillRect(0, 0, 256, 3);
         var4.fillRect(0, 29, 256, 3);
         var4.setColor(Capes.col(16777215, 0.35));
         var4.setStroke(new BasicStroke(1.0F, 0, 0, 1.0F, new float[]{3.0F, 2.0F}, 0.0F));
         var4.drawLine(0, 5, 256, 5);
         var4.drawLine(0, 27, 256, 27);
         var4.setStroke(new BasicStroke(1.0F));

         for (int var10 = 0; var10 < 8; var10++) {
            double var6 = 16 + var10 * 32;
            AffineTransform var8 = var4.getTransform();
            var4.translate(var6, 16.0);
            if (var3 == 0) {
               java.awt.geom.Path2D.Double var12 = new java.awt.geom.Path2D.Double();
               var12.moveTo(2.0, -10.0);
               var12.lineTo(-5.0, 1.0);
               var12.lineTo(0.0, 1.0);
               var12.lineTo(-3.0, 10.0);
               var12.lineTo(5.0, -2.0);
               var12.lineTo(0.0, -2.0);
               var12.lineTo(4.0, -10.0);
               var12.closePath();
               var4.setColor(Capes.col(var2, 0.95));
               var4.fill(var12);
               var4.setColor(Capes.col(16777215, 0.9));
               var4.fill(new Double(10.0, -6.0, 3.0, 3.0));
               var4.fill(new Double(-14.0, 4.0, 2.5, 2.5));
            } else if (var3 == 1) {
               java.awt.geom.Path2D.Double var11 = new java.awt.geom.Path2D.Double();
               var11.moveTo(-9.0, 13.0);
               var11.curveTo(-10.0, 2.0, -2.0, 0.0, -3.0, -12.0);
               var11.curveTo(4.0, -4.0, 9.0, 2.0, 8.0, 13.0);
               var11.closePath();
               var4.setPaint(new GradientPaint(0.0F, 13.0F, new Color(16769146), 0.0F, -12.0F, new Color(var2)));
               var4.fill(var11);
               var4.setColor(Capes.col(16774864, 0.9));
               var4.fill(new Double(-3.0, 4.0, 5.0, 7.0));
            } else {
               var4.setColor(Capes.col(var2, 0.95));
               var4.setStroke(new BasicStroke(1.6F, 1, 1));

               for (int var9 = 0; var9 < 3; var9++) {
                  var4.rotate(Math.PI / 3);
                  var4.drawLine(0, -9, 0, 9);
                  var4.drawLine(-3, -6, 0, -8);
                  var4.drawLine(3, -6, 0, -8);
               }

               var4.setStroke(new BasicStroke(1.0F));
            }

            var4.setTransform(var8);
         }
      });
   }

   static CTex.T skin() {
      BufferedImage var0 = new BufferedImage(64, 64, 2);
      int[] var1 = Capes.px(var0);
      int var2 = 15250570;
      int var3 = 14708766;
      int var4 = 11029008;
      int var5 = 2074784;
      int var6 = 1276532;
      int var7 = 2764360;
      int var8 = 1842994;
      int var9 = 15921906;
      fillBox(var1, 0, 0, 8, 8, 8, var2);
      rect(var1, 0, 8, 32, 3, var3);
      rect(var1, 8, 0, 8, 8, var3);
      rect(var1, 24, 8, 8, 7, var3);
      rect(var1, 0, 11, 2, 3, var3);
      rect(var1, 22, 11, 2, 3, var3);
      rect(var1, 8, 11, 8, 1, var4);
      rect(var1, 10, 8, 2, 4, var3);
      rect(var1, 13, 8, 2, 4, var3);
      rect(var1, 9, 12, 2, 2, 16777215);
      rect(var1, 13, 12, 2, 2, 16777215);
      rect(var1, 10, 12, 1, 2, 2775610);
      rect(var1, 13, 12, 1, 2, 2775610);
      rect(var1, 11, 14, 2, 1, 13140058);
      rect(var1, 10, 15, 4, 1, 10111546);
      rect(var1, 9, 14, 1, 1, 15769738);
      rect(var1, 14, 14, 1, 1, 15769738);
      rect(var1, 32, 8, 32, 1, var6);
      rect(var1, 40, 0, 8, 2, var6);
      rect(var1, 56, 8, 8, 8, argb2(var5));
      rect(var1, 32, 8, 2, 8, argb2(var5));
      rect(var1, 54, 8, 2, 8, argb2(var5));
      fillBox(var1, 16, 16, 8, 12, 4, var5);
      rect(var1, 20, 30, 8, 2, var6);
      rect(var1, 32, 30, 8, 2, var6);
      rect(var1, 23, 20, 2, 4, 15921906);
      rect(var1, 23, 20, 1, 5, 15921906);
      rect(var1, 24, 20, 1, 5, 15921906);
      rect(var1, 21, 25, 6, 3, var6);
      rect(var1, 25, 21, 2, 2, 16764163);
      rect(var1, 25, 21, 1, 1, 16771194);
      fillBox(var1, 40, 16, 4, 12, 4, var5);
      rect(var1, 40, 29, 16, 1, var6);
      rect(var1, 40, 30, 16, 2, var2);
      fillBox(var1, 32, 48, 4, 12, 4, var5);
      rect(var1, 32, 61, 16, 1, var6);
      rect(var1, 32, 62, 16, 2, var2);
      fillBox(var1, 0, 16, 4, 12, 4, var7);
      rect(var1, 0, 29, 16, 3, var9);
      rect(var1, 0, 29, 16, 1, 13158608);
      rect(var1, 4, 22, 1, 6, var8);
      fillBox(var1, 16, 48, 4, 12, 4, var7);
      rect(var1, 16, 61, 16, 3, var9);
      rect(var1, 16, 61, 16, 1, 13158608);
      rect(var1, 23, 54, 1, 6, var8);

      for (int var10 = 0; var10 < 64; var10++) {
         for (int var11 = 0; var11 < 64; var11++) {
            int var12 = var10 * 64 + var11;
            if (var1[var12] >>> 24 != 0) {
               var1[var12] = var1[var12] & 0xFF000000 | Capes.shade(var1[var12], 0.94 + 0.1 * Capes.hash(var11, var10, 91)) & 16777215;
            }
         }
      }

      return CTex.of(var0);
   }

   private static int argb2(int var0) {
      return 0xFF000000 | var0;
   }

   private static void rect(int[] var0, int var1, int var2, int var3, int var4, int var5) {
      if (var5 >>> 24 == 0) {
         var5 |= -16777216;
      }

      for (int var6 = var2; var6 < var2 + var4; var6++) {
         for (int var7 = var1; var7 < var1 + var3; var7++) {
            if (var7 >= 0 && var6 >= 0 && var7 < 64 && var6 < 64) {
               var0[var6 * 64 + var7] = var5;
            }
         }
      }
   }

   private static void fillBox(int[] var0, int var1, int var2, int var3, int var4, int var5, int var6) {
      rect(var0, var1 + var5, var2, var3, var5, Capes.shade(var6, 1.08));
      rect(var0, var1 + var5 + var3, var2, var3, var5, Capes.shade(var6, 0.8));
      rect(var0, var1, var2 + var5, var5, var4, Capes.shade(var6, 0.9));
      rect(var0, var1 + var5, var2 + var5, var3, var4, var6);
      rect(var0, var1 + var5 + var3, var2 + var5, var5, var4, Capes.shade(var6, 0.9));
      rect(var0, var1 + 2 * var5 + var3, var2 + var5, var3, var4, Capes.shade(var6, 0.86));
   }

   private static Rectangle2D r(double var0, double var2, double var4, double var6) {
      return new java.awt.geom.Rectangle2D.Double(var0, var2, var4, var6);
   }
}
