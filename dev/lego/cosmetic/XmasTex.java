package dev.lego.cosmetic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;
import java.util.Random;

final class XmasTex {
   static final float[][] GMAN = new float[][]{
      {0.0F, 1.0F},
      {0.24F, 0.96F},
      {0.36F, 0.8F},
      {0.34F, 0.58F},
      {0.22F, 0.46F},
      {0.62F, 0.44F},
      {0.86F, 0.36F},
      {0.9F, 0.2F},
      {0.76F, 0.12F},
      {0.36F, 0.12F},
      {0.36F, -0.2F},
      {0.62F, -0.72F},
      {0.62F, -0.92F},
      {0.44F, -1.0F},
      {0.26F, -0.92F},
      {0.0F, -0.46F},
      {-0.26F, -0.92F},
      {-0.44F, -1.0F},
      {-0.62F, -0.92F},
      {-0.62F, -0.72F},
      {-0.36F, -0.2F},
      {-0.36F, 0.12F},
      {-0.76F, 0.12F},
      {-0.9F, 0.2F},
      {-0.86F, 0.36F},
      {-0.62F, 0.44F},
      {-0.22F, 0.46F},
      {-0.34F, 0.58F},
      {-0.36F, 0.8F},
      {-0.24F, 0.96F}
   };

   private XmasTex() {
   }

   static CTex.T paint(String var0) {
      switch (var0) {
         case "xm_felt":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 10.0, var2 * 10.0, 10, 10, 4, 301);
               return 0xFF000000 | Capes.shade(16777215, 0.82 + 0.16 * var6 + 0.05 * (Capes.hash(var4, var5, 302) - 0.5));
            });
         case "xm_velvet":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 6.0, var2 * 6.0, 6, 6, 3, 311);
               double var8 = 0.5 + 0.5 * Math.sin(var0x * Math.PI * 2.0 * 2.0 + var6 * 2.0);
               return 0xFF000000 | Capes.shade(16777215, 0.72 + 0.2 * var6 + 0.1 * var8);
            });
         case "xm_fluff":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 16.0, var2 * 16.0, 16, 16, 4, 321);
               double var8 = 0.5 + 0.5 * Math.sin(var0x * 30.0 + var6 * 6.0 + Math.cos(var2 * 30.0 + var6 * 5.0));
               return 0xFF000000 | Capes.shade(16777215, 0.8 + 0.14 * var6 + 0.08 * var8);
            });
         case "xm_knit":
            return knit(-1);
         case "xm_scarf":
            return knit(0);
         case "xm_sweater":
            return knit(1);
         case "xm_ribbon":
            return Tex2.pix(32, 32, (var0x, var2, var4, var5) -> {
               double var6 = 0.78 + 0.22 * Math.exp(-Math.pow((var0x - 0.42) / 0.16, 2.0)) + 0.05 * Math.sin(var2 * 40.0);
               double var8 = !(var0x < 0.08) && !(var0x > 0.92) ? 1.0 : 0.8;
               return 0xFF000000 | Capes.shade(16777215, Math.min(1.0, var6 * var8));
            });
         case "xm_silk":
            return Tex2.pix(
               64,
               32,
               (var0x, var2, var4, var5) -> {
                  double var6 = 0.35
                     + 0.5 * Math.pow(Math.max(0.0, Math.sin(var0x * Math.PI * 2.0 * 3.0)), 6.0)
                     + 0.25 * Math.pow(Math.max(0.0, Math.sin(var0x * Math.PI * 2.0 * 3.0 + 1.2)), 16.0);
                  return 0xFF000000 | Capes.shade(16777215, Math.min(1.0, var6 + 0.05 * var2));
               }
            );
         case "xm_elfcuff":
            return Tex2.img(256, 32, var0x -> {
               var0x.setPaint(new GradientPaint(0.0F, 0.0F, new Color(15217210), 0.0F, 32.0F, new Color(10097176)));
               var0x.fillRect(0, 0, 256, 32);
               var0x.setColor(new Color(16767082));
               var0x.fillRect(0, 0, 256, 3);
               var0x.fillRect(0, 29, 256, 3);
               Double var1 = new Double();
               var1.moveTo(0.0, 22.0);

               for (int var2 = 0; var2 <= 16; var2++) {
                  var1.lineTo(var2 * 16, var2 % 2 == 0 ? 22.0 : 9.0);
               }

               var0x.setColor(Color.WHITE);
               var0x.setStroke(new BasicStroke(4.0F, 1, 1));
               var0x.draw(var1);
               var0x.setColor(new Color(3054152));

               for (int var3 = 0; var3 < 16; var3++) {
                  var0x.fill(new java.awt.geom.Ellipse2D.Double(var3 * 16 + 5.5, var3 % 2 == 0 ? 7.0 : 17.0, 5.0, 5.0));
               }
            });
         case "xm_sack":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               boolean var6 = (var4 / 2 + var5 / 2) % 2 == 0;
               double var7 = Capes.fbm(var0x * 8.0, var2 * 8.0, 8, 8, 3, 331);
               double var9 = (var6 ? 0.92 : 0.8) * (0.85 + 0.25 * var7) * (var4 % 2 == 0 ? 1.0 : 0.95);
               return 0xFF000000 | Capes.shade(16777215, var9);
            });
         case "xm_candy":
            return Tex2.pix(64, 128, (var0x, var2, var4, var5) -> {
               double var6 = frac(var0x * 2.0 + var2 * 4.0);
               int var8 = var6 < 0.32 ? 14688298 : (var6 > 0.42 && var6 < 0.47 ? 15751770 : (var6 > 0.72 && var6 < 0.78 ? 3848298 : 16775924));
               double var9 = Math.min(Math.abs(var6 - 0.32), Math.abs(var6));
               if (var9 < 0.015) {
                  var8 = Capes.mix(var8, 15764106, 0.5);
               }

               return 0xFF000000 | Capes.shade(var8, 0.94 + 0.06 * Math.sin(var2 * 30.0));
            });
         case "xm_antler":
            return Tex2.pix(32, 128, (var0x, var2, var4, var5) -> {
               double var6 = 0.9 + 0.1 * Math.sin(var0x * Math.PI * 2.0 * 7.0 + Capes.fbm(var0x * 4.0, var2 * 8.0, 4, 8, 2, 341) * 4.0);
               double var8 = Capes.fbm(var0x * 6.0, var2 * 20.0, 6, 20, 3, 342);
               int var10 = Capes.mix(9067060, 15786176, Math.pow(var2, 1.4) * 0.9);
               return 0xFF000000 | Capes.shade(var10, var6 * (0.82 + 0.3 * var8));
            });
         case "xm_pine":
            return Tex2.img(64, 64, var0x -> {
               var0x.setColor(new Color(1989168));
               var0x.fillRect(0, 0, 64, 64);
               Random var1 = new Random(351L);
               var0x.setStroke(new BasicStroke(1.3F, 1, 1));

               for (int var2 = 0; var2 < 420; var2++) {
                  double var3 = var1.nextDouble() * 64.0;
                  double var5 = var1.nextDouble() * 64.0;
                  double var7 = 4.0 + var1.nextDouble() * 5.0;
                  double var9 = (Math.PI / 2) + (var1.nextDouble() - 0.5) * 1.1;
                  int var11 = Capes.ramp(var1.nextDouble(), 1460266, 3045956, 4892768, 8047492);
                  var0x.setColor(new Color(var11));

                  for (int var12 = -1; var12 <= 1; var12++) {
                     double var13 = var3 + var12 * 64;
                     var0x.drawLine((int)var13, (int)var5, (int)(var13 + Math.cos(var9) * var7), (int)(var5 + Math.sin(var9) * var7));
                  }
               }
            });
         case "xm_bark":
            return Tex2.pix(
               32, 64, (var0x, var2, var4, var5) -> 0xFF000000 | Capes.shade(6963746, 0.7 + 0.35 * Capes.fbm(var0x * 8.0, var2 * 2.0, 8, 2, 3, 361))
            );
         case "xm_wood":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 2.0, var2 * 10.0, 2, 10, 4, 371);
               double var8 = 0.5 + 0.5 * Math.sin((var2 * 22.0 + var6 * 6.0) * Math.PI);
               return 0xFF000000 | Capes.shade(16777215, 0.72 + 0.18 * var8 + 0.1 * var6);
            });
         case "xm_coal":
            return Tex2.pix(
               32,
               32,
               (var0x, var2, var4, var5) -> 0xFF000000
                  | Capes.shade(2763312, 0.6 + 0.8 * Capes.fbm(var0x * 6.0, var2 * 6.0, 6, 6, 3, 381) + (Capes.hash(var4, var5, 382) > 0.93 ? 0.8 : 0.0))
            );
         case "xm_carrot":
            return Tex2.pix(32, 64, (var0x, var2, var4, var5) -> {
               double var6 = 0.88 + 0.12 * Math.sin(var2 * 60.0 + Capes.fbm(var0x * 4.0, var2 * 4.0, 4, 4, 2, 391) * 3.0);
               return 0xFF000000 | Capes.shade(Capes.mix(16747034, 15755786, var2), var6);
            });
         case "xm_snow":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 8.0, var2 * 8.0, 8, 8, 4, 401);
               int var8 = Capes.mix(14477560, 16777215, 0.4 + 0.6 * var6);
               if (Capes.hash(var4, var5, 402) > 0.975) {
                  var8 = 16777215;
               }

               if (Capes.hash(var4, var5, 403) > 0.992) {
                  var8 = 12575999;
               }

               return 0xFF000000 | var8;
            });
         case "xm_ginger":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 8.0, var2 * 8.0, 8, 8, 4, 411);
               int var8 = Capes.ramp(var6, 9062942, 11559978, 13138488);
               if (Capes.hash(var4, var5, 412) > 0.97) {
                  var8 = Capes.shade(var8, 0.75);
               }

               if (Capes.hash(var4, var5, 413) > 0.985) {
                  var8 = Capes.mix(var8, 15249520, 0.5);
               }

               return 0xFF000000 | var8;
            });
         case "xm_gingerman":
            return gingerMan();
         case "xm_holly":
            return Tex2.img(64, 32, var0x -> {
               var0x.setPaint(new GradientPaint(0.0F, 0.0F, new Color(3842128), 0.0F, 32.0F, new Color(1727016)));
               var0x.fillRect(0, 0, 64, 32);
               var0x.setColor(new Color(10147978));
               var0x.setStroke(new BasicStroke(1.4F));
               var0x.drawLine(2, 16, 62, 16);
               var0x.setStroke(new BasicStroke(0.8F));

               for (int var1 = 0; var1 < 6; var1++) {
                  var0x.drawLine(8 + var1 * 9, 16, 13 + var1 * 9, 6);
                  var0x.drawLine(8 + var1 * 9, 16, 13 + var1 * 9, 26);
               }

               var0x.setColor(Capes.col(16777215, 0.25));
               var0x.fill(new java.awt.geom.Ellipse2D.Double(10.0, 5.0, 30.0, 7.0));
            });
         case "xm_gloss":
            return Tex2.pix(32, 32, (var0x, var2, var4, var5) -> {
               double var6 = Math.hypot(var0x - 0.32, var2 - 0.3);
               return 0xFF000000 | Capes.add(Capes.shade(16777215, 0.7 + 0.3 * (1.0 - var2)), 16777215, Math.exp(-var6 * var6 / 0.004) * 0.9);
            });
         case "xm_bulb":
            return Tex2.pix(32, 32, (var0x, var2, var4, var5) -> {
               double var6 = Math.hypot(var0x - 0.5, var2 - 0.5);
               double var8 = Math.exp(-(Math.pow(var0x - 0.3, 2.0) + Math.pow(var2 - 0.35, 2.0)) / 0.006);
               return 0xFF000000 | Capes.add(Capes.shade(16777215, 0.78 + 0.22 * (1.0 - var6 * 1.4)), 16777215, var8);
            });
         case "xm_flake":
            return Tex2.img(64, 64, var0x -> {
               var0x.translate(32, 32);

               for (int var1 = 0; var1 < 2; var1++) {
                  var0x.setColor(var1 == 0 ? Capes.col(10149119, 0.45) : Color.WHITE);
                  var0x.setStroke(new BasicStroke(var1 == 0 ? 5.5F : 2.4F, 1, 1));

                  for (int var2 = 0; var2 < 6; var2++) {
                     var0x.drawLine(0, 0, 0, -28);
                     var0x.drawLine(0, -10, -6, -16);
                     var0x.drawLine(0, -10, 6, -16);
                     var0x.drawLine(0, -19, -5, -24);
                     var0x.drawLine(0, -19, 5, -24);
                     var0x.rotate(Math.PI / 3);
                  }
               }

               var0x.setColor(Color.WHITE);
               var0x.fill(new java.awt.geom.Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0));
            });
         case "xm_aurora":
            return Tex2.pix(128, 64, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 24.0, 0.5, 24, 0, 3, 421);
               double var8 = 0.5 + 0.5 * Math.sin(var0x * Math.PI * 2.0 * 9.0 + var6 * 3.0);
               double var10 = Math.exp(-Math.pow((1.0 - var2) / 0.1, 2.0));
               double var12 = Math.pow(var2, 1.8) * (0.45 + 0.55 * var6) * (0.6 + 0.4 * var8);
               int var14 = Capes.ramp(var2, 11819775, 5933823, 3860696, 5963658, 13172696);
               double var15 = Math.min(1.0, var12 * 1.1 + var10 * 0.7) * (var2 > 0.97 ? (1.0 - var2) / 0.03 : 1.0);
               return Tex2.argb(Capes.add(var14, 16777215, var10 * 0.5), var15);
            });
         case "xm_gold":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = 0.5 + 0.5 * Math.sin(var0x * 9.0 + var2 * 4.0);
               return 0xFF000000 | Capes.ramp(0.25 + 0.6 * var6 * (0.8 + 0.2 * Capes.hash(var4 / 4, var5 / 4, 431)), 10119698, 15251756, 16773544, 16777215);
            });
         case "xm_red_lacquer":
            return Tex2.pix(32, 128, (var0x, var2, var4, var5) -> {
               double var6 = Math.exp(-Math.pow((var0x - 0.3) / 0.12, 2.0)) * 0.45 + 0.6 + 0.08 * Math.sin(var2 * 3.0);
               return 0xFF000000 | Capes.shade(13112350, Math.min(1.35, var6));
            });
         case "xm_sleighside":
            return sleighSide();
         case "xm_wind":
            return Tex2.pix(128, 16, (var0x, var2, var4, var5) -> {
               double var6 = Math.pow(Math.sin(Math.PI * var2), 2.0) * (0.35 + 0.65 * Capes.fbm(var0x * 12.0, var2 * 2.0, 12, 2, 3, 461));
               return Tex2.argb(16055039, var6 * Math.pow(Math.sin(Math.PI * var0x), 0.6) * 0.75);
            });
         default:
            return var0.startsWith("xm_gift:") ? gift(var0.substring(8)) : null;
      }
   }

   static double frac(double var0) {
      return var0 - Math.floor(var0);
   }

   private static CTex.T knit(int var0) {
      byte var1 = 64;
      int var2 = var0 == 0 ? 256 : 128;
      return Tex2.pix(var1, var2, (var1x, var3, var5, var6) -> {
         double var7 = var1x * 8.0;
         double var9 = var3 * (var0 == 0 ? 40 : 20);
         double var11 = frac(var7) - 0.5;
         double var13 = frac(var9);
         double var15 = Math.abs(Math.abs(var11) * 2.0 - (1.0 - var13) * 0.9 - 0.05);
         double var17 = 1.0 - Math.min(1.0, var15 * 1.6);
         double var19 = Math.abs(var11) < 0.05 ? 0.72 : 1.0;
         double var21 = (0.72 + 0.3 * var17) * var19 * (0.95 + 0.06 * Capes.hash(var5, var6, 441));
         int var23 = 16777215;
         if (var0 == 0) {
            int var24 = (int)Math.floor(var9);
            int var25 = var24 % 20;
            var23 = var25 == 3 || var25 == 4 || var25 == 15 || var25 == 16 ? 16777215 : (var25 != 9 && var25 != 10 ? 14164780 : 3054152);
            if (var24 < 1 || var24 > 38) {
               var23 = 16053492;
            }
         } else if (var0 == 1) {
            int var26 = (int)Math.floor(var9);
            var23 = var26 % 10 != 4 && var26 % 10 != 5 ? 13115434 : 16777215;
         }

         return 0xFF000000 | Capes.shade(var23, var21);
      });
   }

   private static CTex.T gift(String var0) {
      String[] var1 = var0.split(":");
      int var2 = Integer.parseInt(var1[0], 16);
      int var3 = Integer.parseInt(var1[1], 16);
      int var4 = var1.length > 2 ? Integer.parseInt(var1[2]) : 0;
      return Tex2.img(
         64,
         64,
         var3x -> {
            var3x.setPaint(new GradientPaint(0.0F, 0.0F, new Color(Capes.mix(var2, 16777215, 0.12)), 64.0F, 64.0F, new Color(Capes.mix(var2, 0, 0.18))));
            var3x.fillRect(0, 0, 64, 64);
            var3x.setColor(new Color(var3));
            if (var4 == 2) {
               var3x.setStroke(new BasicStroke(5.0F));

               for (byte var4x = -64; var4x < 128; var4x += 16) {
                  var3x.drawLine(var4x, 0, var4x + 64, 64);
               }
            } else {
               for (int var13 = 0; var13 < 4; var13++) {
                  for (int var5 = 0; var5 < 4; var5++) {
                     double var6 = 8 + var5 * 16 + var13 % 2 * 8;
                     double var8 = 8 + var13 * 16;
                     if (var4 == 0) {
                        var3x.fill(new java.awt.geom.Ellipse2D.Double(var6 - 3.0, var8 - 3.0, 6.0, 6.0));
                     } else if (var4 == 1) {
                        var3x.fill(starShape(var6, var8, 5.0, 2.2, 5));
                     } else {
                        var3x.setStroke(new BasicStroke(1.4F, 1, 1));

                        for (int var10 = 0; var10 < 3; var10++) {
                           double var11 = var10 * Math.PI / 3.0;
                           var3x.drawLine(
                              (int)(var6 - Math.cos(var11) * 5.0),
                              (int)(var8 - Math.sin(var11) * 5.0),
                              (int)(var6 + Math.cos(var11) * 5.0),
                              (int)(var8 + Math.sin(var11) * 5.0)
                           );
                        }
                     }
                  }
               }
            }

            var3x.setColor(Capes.col(16777215, 0.08));

            for (byte var14 = 0; var14 < 64; var14 += 3) {
               var3x.drawLine(0, var14, 64, var14);
            }
         }
      );
   }

   static Path2D starShape(double var0, double var2, double var4, double var6, int var8) {
      Double var9 = new Double();

      for (int var10 = 0; var10 < var8 * 2; var10++) {
         double var11 = (-Math.PI / 2) + Math.PI * var10 / var8;
         double var13 = var10 % 2 == 0 ? var4 : var6;
         if (var10 == 0) {
            var9.moveTo(var0 + Math.cos(var11) * var13, var2 + Math.sin(var11) * var13);
         } else {
            var9.lineTo(var0 + Math.cos(var11) * var13, var2 + Math.sin(var11) * var13);
         }
      }

      var9.closePath();
      return var9;
   }

   private static CTex.T gingerMan() {
      return Tex2.img(
         128,
         128,
         var0 -> {
            var0.setColor(new Color(8011800));
            var0.fillRect(0, 0, 128, 128);
            Double var1 = new Double();

            for (int var2 = 0; var2 < GMAN.length; var2++) {
               double var3 = (GMAN[var2][0] + 0.9) / 1.8 * 128.0;
               double var5 = 64.0F - GMAN[var2][1] * 64.0F;
               if (var2 == 0) {
                  var1.moveTo(var3, var5);
               } else {
                  var1.lineTo(var3, var5);
               }
            }

            var1.closePath();
            var0.setPaint(
               new RadialGradientPaint(
                  56.0F, 50.0F, 80.0F, new float[]{0.0F, 0.7F, 1.0F}, new Color[]{new Color(13666884), new Color(12085806), new Color(9062942)}
               )
            );
            var0.fill(var1);
            Random var7 = new Random(451L);

            for (int var8 = 0; var8 < 90; var8++) {
               var0.setColor(Capes.col(5909002, 0.35));
               var0.fill(new java.awt.geom.Ellipse2D.Double(var7.nextDouble() * 128.0, var7.nextDouble() * 128.0, 1.6, 1.6));
            }

            var0.setColor(Color.WHITE);
            var0.setStroke(new BasicStroke(3.0F, 1, 1));
            var0.fill(new java.awt.geom.Ellipse2D.Double(51.0, 13.0, 8.0, 8.0));
            var0.fill(new java.awt.geom.Ellipse2D.Double(69.0, 13.0, 8.0, 8.0));
            var0.drawArc(52, 16, 24, 16, 200, 140);
            zig(var0, 14.0, 44.0, 14.0, 62.0, 4);
            zig(var0, 114.0, 44.0, 114.0, 62.0, 4);
            zig(var0, 30.0, 108.0, 42.0, 120.0, 3);
            zig(var0, 98.0, 108.0, 86.0, 120.0, 3);
            int[] var9 = new int[]{15217210, 3061834, 15217210};

            for (int var4 = 0; var4 < 3; var4++) {
               var0.setColor(new Color(var9[var4]));
               var0.fill(new java.awt.geom.Ellipse2D.Double(58.0, 44 + var4 * 14, 12.0, 12.0));
               var0.setColor(Capes.col(16777215, 0.7));
               var0.fill(new java.awt.geom.Ellipse2D.Double(60.0, 46 + var4 * 14, 4.0, 4.0));
            }
         }
      );
   }

   private static void zig(Graphics2D var0, double var1, double var3, double var5, double var7, int var9) {
      Double var10 = new Double();
      double var11 = var5 - var1;
      double var13 = var7 - var3;
      double var15 = Math.hypot(var11, var13);
      double var17 = -var13 / var15 * 4.0;
      double var19 = var11 / var15 * 4.0;

      for (int var21 = 0; var21 <= var9 * 2; var21++) {
         double var22 = (double)var21 / (var9 * 2);
         double var24 = var21 % 2 == 0 ? -1.0 : 1.0;
         double var26 = var1 + var11 * var22 + var17 * var24;
         double var28 = var3 + var13 * var22 + var19 * var24;
         if (var21 == 0) {
            var10.moveTo(var26, var28);
         } else {
            var10.lineTo(var26, var28);
         }
      }

      var0.setColor(Color.WHITE);
      var0.draw(var10);
   }

   private static CTex.T sleighSide() {
      return Tex2.img(256, 64, var0 -> {
         var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(15217210), 0.0F, 64.0F, new Color(7997970)));
         var0.fillRect(0, 0, 256, 64);
         var0.setColor(Capes.col(16777215, 0.18));
         var0.fillRect(0, 8, 256, 6);
         var0.setColor(new Color(16767082));
         var0.setStroke(new BasicStroke(2.2F, 1, 1));
         var0.drawLine(0, 4, 256, 4);
         var0.drawLine(0, 58, 256, 58);

         for (int var1 = 0; var1 < 4; var1++) {
            double var2 = 32 + var1 * 64;
            Double var4 = new Double();
            var4.moveTo(var2 - 24.0, 32.0);
            var4.curveTo(var2 - 20.0, 14.0, var2 - 4.0, 14.0, var2 - 4.0, 28.0);
            var4.curveTo(var2 - 4.0, 36.0, var2 - 14.0, 36.0, var2 - 12.0, 28.0);
            var0.draw(var4);
            Double var5 = new Double();
            var5.moveTo(var2 + 24.0, 32.0);
            var5.curveTo(var2 + 20.0, 50.0, var2 + 4.0, 50.0, var2 + 4.0, 36.0);
            var5.curveTo(var2 + 4.0, 28.0, var2 + 14.0, 28.0, var2 + 12.0, 36.0);
            var0.draw(var5);
            var0.fill(new java.awt.geom.Ellipse2D.Double(var2 - 3.0, 29.0, 6.0, 6.0));
         }
      });
   }
}
