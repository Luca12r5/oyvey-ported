package dev.lego.cosmetic;

import dev.lego.ui.Gx;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Path2D.Double;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Consumer;

public final class CTex {
   private static final Map<String, CTex.T> CACHE = new HashMap<>();
   private static final int FRAME_CACHE = 96;
   private static final LinkedHashMap<String, CTex.T> FRAMES = new LinkedHashMap<String, CTex.T>(64, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, CTex.T> var1) {
         return this.size() > 96;
      }
   };
   private static final Object MAPS = new Object();
   private static final Object PAINTING = new Object();

   private CTex() {
   }

   private static String key(String var0) {
      if (var0.startsWith("cape_") && var0.indexOf(35) > 0) {
         int var1 = var0.indexOf(35);
         String var2 = var0.substring(0, var1);
         int var3 = Capes.frames(var2);

         int var4;
         try {
            var4 = Math.floorMod(Integer.parseInt(var0.substring(var1 + 1)), Math.max(1, var3));
         } catch (NumberFormatException var6) {
            var4 = 0;
         }

         return var4 != 0 && var3 > 1 ? var2 + "#" + var4 : var2;
      } else {
         return var0;
      }
   }

   private static CTex.T lookup(String var0) {
      synchronized (MAPS) {
         return var0.indexOf(35) > 0 && var0.startsWith("cape_") ? FRAMES.get(var0) : CACHE.get(var0);
      }
   }

   public static CTex.T peek(String var0) {
      return lookup(key(var0));
   }

   public static CTex.T get(String var0) {
      String var1 = key(var0);
      CTex.T var2 = lookup(var1);
      if (var2 != null) {
         return var2;
      } else {
         synchronized (PAINTING) {
            var2 = lookup(var1);
            if (var2 != null) {
               return var2;
            } else {
               var2 = paint(var1);
               synchronized (MAPS) {
                  if (var1.indexOf(35) > 0 && var1.startsWith("cape_")) {
                     FRAMES.put(var1, var2);
                  } else {
                     CACHE.put(var1, var2);
                  }
               }

               return var2;
            }
         }
      }
   }

   private static CTex.T make(int var0, int var1, Consumer<Graphics2D> var2) {
      BufferedImage var3 = new BufferedImage(var0, var1, 2);
      Graphics2D var4 = var3.createGraphics();
      Gx.hints(var4);
      var2.accept(var4);
      var4.dispose();
      int[] var5 = var3.getRGB(0, 0, var0, var1, null, 0, var0);
      bleed(var5, var0, var1);
      return new CTex.T(var0, var1, var5);
   }

   private static void bleed(int[] var0, int var1, int var2) {
      for (int var3 = 0; var3 < 3; var3++) {
         int[] var4 = (int[])var0.clone();

         for (int var5 = 0; var5 < var2; var5++) {
            for (int var6 = 0; var6 < var1; var6++) {
               int var7 = var5 * var1 + var6;
               if (var4[var7] >>> 24 == 0) {
                  int var8 = 0;
                  int var9 = 0;
                  int var10 = 0;
                  int var11 = 0;

                  for (int var12 = -1; var12 <= 1; var12++) {
                     for (int var13 = -1; var13 <= 1; var13++) {
                        int var14 = var6 + var13;
                        int var15 = var5 + var12;
                        if (var14 >= 0 && var15 >= 0 && var14 < var1 && var15 < var2) {
                           int var16 = var4[var15 * var1 + var14];
                           if (var16 >>> 24 != 0 && ((var16 & 16777215) != 0 || var16 >>> 24 != 0) && var16 >>> 24 >= 8) {
                              var8 += var16 >> 16 & 0xFF;
                              var9 += var16 >> 8 & 0xFF;
                              var10 += var16 & 0xFF;
                              var11++;
                           }
                        }
                     }
                  }

                  if (var11 > 0) {
                     var0[var7] = var8 / var11 << 16 | var9 / var11 << 8 | var10 / var11;
                  }
               }
            }
         }
      }
   }

   static Color c(int var0) {
      return new Color(var0);
   }

   static Color ca(int var0, int var1) {
      return new Color(var0 >> 16 & 0xFF, var0 >> 8 & 0xFF, var0 & 0xFF, var1);
   }

   static CTex.T of(BufferedImage var0) {
      int var1 = var0.getWidth();
      int var2 = var0.getHeight();
      int[] var3 = var0.getRGB(0, 0, var1, var2, null, 0, var1);
      bleed(var3, var1, var2);
      return new CTex.T(var1, var2, var3);
   }

   private static CTex.T paint(String var0) {
      if (var0.startsWith("pet")) {
         CTex.T var1 = PetTex.paint(var0);
         if (var1 != null) {
            return var1;
         }
      }

      if (var0.startsWith("veh")) {
         CTex.T var4 = Vehicles.tex(var0);
         if (var4 != null) {
            return var4;
         }
      }

      switch (var0) {
         case "white":
            return make(16, 16, var0x -> {
               var0x.setColor(Color.WHITE);
               var0x.fillRect(0, 0, 16, 16);
            });
         case "wing_angel":
            return feathers(16777215, 14476271, 10464966, 16176506);
         case "wing_gold":
            return feathers(16773544, 15909424, 12157210, 16777215);
         case "wing_phoenix":
            return feathers(16774064, 16747038, 13639455, 16769658);
         case "wing_shadow":
            return feathers(7040896, 3027264, 921366, 10185727);
         case "wing_demon":
            return membrane(3803922, 9313060, 1705224, 16730938, false);
         case "wing_icedragon":
            return membrane(998246, 3785983, 663098, 13629183, true);
         case "wing_neon":
            return membrane(1181215, 2887242, 459278, 13851647, true);
         case "wing_butterfly":
            return butterfly();
         case "gold":
            return metal(16774064, 15251756, 10119698);
         case "silver":
            return metal(16777215, 13226202, 8029332);
         case "gems":
            return gems();
         case "halo":
            return make(256, 32, var0x -> {
               var0x.setPaint(new GradientPaint(0.0F, 0.0F, c(16775112), 0.0F, 32.0F, c(16762173)));
               var0x.fillRect(0, 0, 256, 32);
               var0x.setColor(ca(16777215, 150));
               var0x.fillRect(0, 6, 256, 5);
            });
         case "felt_black":
            return felt(1447451, 2237227, 7L);
         case "felt_red":
            return felt(12850716, 14689835, 11L);
         case "felt_purple":
            return felt(2757186, 4070750, 13L);
         case "fur_white":
            return fur();
         case "band_red":
            return make(64, 16, var0x -> {
               var0x.setPaint(new GradientPaint(0.0F, 0.0F, c(14891578), 0.0F, 16.0F, c(9311252)));
               var0x.fillRect(0, 0, 64, 16);
            });
         case "band_green":
            return make(64, 16, var0x -> {
               var0x.setPaint(new GradientPaint(0.0F, 0.0F, c(2875514), 0.0F, 16.0F, c(1279039)));
               var0x.fillRect(0, 0, 64, 16);
            });
         case "cat_ear":
            return catEar();
         case "bunny_ear":
            return bunnyEar();
         case "horn":
            return make(
               64,
               128,
               var0x -> {
                  var0x.setPaint(
                     new LinearGradientPaint(0.0F, 0.0F, 0.0F, 128.0F, new float[]{0.0F, 0.55F, 1.0F}, new Color[]{c(1836038), c(9049108), c(15220794)})
                  );
                  var0x.fillRect(0, 0, 64, 128);
                  var0x.setColor(ca(0, 60));

                  for (byte var1x = 0; var1x < 128; var1x += 9) {
                     var0x.fillRect(0, var1x, 64, 3);
                  }

                  var0x.setPaint(new GradientPaint(0.0F, 0.0F, ca(16777215, 70), 64.0F, 0.0F, ca(16777215, 0)));
                  var0x.fillRect(0, 0, 22, 128);
               }
            );
         case "headphone":
            return make(64, 64, var0x -> {
               var0x.setPaint(new GradientPaint(0.0F, 0.0F, c(2895414), 0.0F, 64.0F, c(1184536)));
               var0x.fillRect(0, 0, 64, 64);
               var0x.setColor(ca(16777215, 22));

               for (byte var1x = 0; var1x < 64; var1x += 4) {
                  var0x.drawLine(0, var1x, 64, var1x);
               }
            });
         case "cushion":
            return make(64, 64, var0x -> {
               var0x.setPaint(new RadialGradientPaint(32.0F, 32.0F, 34.0F, new float[]{0.0F, 1.0F}, new Color[]{c(3816776), c(1447708)}));
               var0x.fillRect(0, 0, 64, 64);
               var0x.setColor(c(2021216));
               var0x.setStroke(new BasicStroke(5.0F));
               var0x.drawOval(9, 9, 46, 46);
            });
         case "sunglasses":
            return sunglasses();
         case "pixelglasses":
            return pixelGlasses();
         case "lego_red":
            return legoPlastic(14876683);
         case "lego_yellow":
            return legoPlastic(16764163);
         case "lego_blue":
            return legoPlastic(27831);
         case "lego_green":
            return legoPlastic(34091);
         case "sword":
            return sword();
         case "strap":
            return make(32, 32, var0x -> {
               var0x.setPaint(new GradientPaint(0.0F, 0.0F, c(4862498), 32.0F, 0.0F, c(2759184)));
               var0x.fillRect(0, 0, 32, 32);
            });
         case "flame":
            return flame();
         case "snowflake":
            return snowflake();
         case "heart":
            return heartSprite();
         case "orb":
            return orb(16777215);
         case "orb_purple":
            return orb(13073919);
         case "orb_cyan":
            return orb(8189951);
         case "spark":
            return spark();
         case "bolt":
            return boltSprite();
         case "mannequin_head":
         case "mannequin":
            return Tex2.pix(64, 64, (var1x, var3x, var5x, var6x) -> {
               double var7x = Capes.fbm(var1x * 6.0, var3x * 6.0, 6, 6, 3, var0.length() * 17 + 5);
               return 0xFF000000 | Capes.shade(16777215, 0.93 + 0.05 * var7x + 0.025 * (Capes.hash(var5x, var6x, 77) - 0.5));
            });
         case "mannequin_shadow":
            return Tex2.pix(64, 64, (var0x, var2, var4x, var5x) -> {
               double var6x = Math.hypot((var0x - 0.5) * 2.0, (var2 - 0.5) * 2.4);
               double var8 = Math.pow(Math.max(0.0, 1.0 - var6x), 1.6);
               return Tex2.argb(0, var8);
            });
         default:
            if (var0.startsWith("cape_")) {
               return Capes.paint(var0.substring(5));
            } else {
               if (var0.startsWith("c3_") || var0.equals("@skin")) {
                  CTex.T var3 = Cos3Tex.paint(var0);
                  if (var3 != null) {
                     return var3;
                  }
               }

               if (var0.startsWith("xm_")) {
                  CTex.T var5 = XmasTex.paint(var0);
                  if (var5 != null) {
                     return var5;
                  }
               }

               if (var0.startsWith("hd_")) {
                  CTex.T var6 = HdTex.paint(var0);
                  if (var6 != null) {
                     return var6;
                  }
               }

               CTex.T var7 = Tex2.paint(var0);
               return var7 != null ? var7 : make(16, 16, var0x -> {
                  var0x.setColor(Color.MAGENTA);
                  var0x.fillRect(0, 0, 16, 16);
               });
            }
      }
   }

   private static CTex.T feathers(int var0, int var1, int var2, int var3) {
      short var4 = 256;
      short var5 = 320;
      return make(var4, var5, var5x -> {
         Random var6 = new Random(7L);

         for (int var7 = 0; var7 < 9; var7++) {
            double var8 = var7 / 8.0;
            double var10 = 214.0 - var8 * 72.0;
            double var12 = 30.0 + var8 * 8.0;
            double var14 = Math.toRadians(76.0 + var8 * 26.0 + var6.nextGaussian() * 1.2);
            double var16 = 280.0 - var8 * 70.0;
            feather(var5x, var10, var12, var14, var16, 34.0, var0, var1, var2, var3, 0.6);
         }

         for (int var19 = 0; var19 < 11; var19++) {
            double var22 = var19 / 10.0;
            double var24 = 146.0 - var22 * 132.0;
            double var25 = 36.0 - var22 * 10.0;
            double var26 = Math.toRadians(100.0 + var22 * 12.0 + var6.nextGaussian() * 1.2);
            double var27 = 205.0 - var22 * 85.0;
            feather(var5x, var24, var25, var26, var27, 32.0, var0, var1, var2, var3, 0.35);
         }

         for (int var20 = 1; var20 >= 0; var20--) {
            for (int var23 = 0; var23 < 12; var23++) {
               double var9 = var23 / 11.0;
               double var11 = 222.0 - var9 * 212.0;
               double var13 = 24 + var20 * 18 - var9 * 6.0;
               double var15 = Math.toRadians(80.0 + var9 * 22.0);
               double var17 = var20 == 0 ? 52.0 + (1.0 - var9) * 20.0 : 82.0 + (1.0 - var9) * 26.0;
               feather(var5x, var11, var13, var15, var17, 30.0, var0, var1, var2, var3, 0.1);
            }
         }

         var5x.setPaint(new GradientPaint(0.0F, 0.0F, c(var0), var4, 40.0F, c(var1)));
         Double var21 = new Double();
         var21.moveTo(0.0, 2.0);
         var21.curveTo(70.0, -4.0, 170.0, 4.0, 252.0, 22.0);
         var21.lineTo(246.0, 44.0);
         var21.curveTo(170.0, 30.0, 80.0, 30.0, 0.0, 36.0);
         var21.closePath();
         var5x.fill(var21);
         var5x.setColor(ca(var2, 90));
         var5x.setStroke(new BasicStroke(2.0F));
         var5x.draw(var21);
         var5x.setColor(ca(16777215, 120));
         var5x.setStroke(new BasicStroke(2.5F, 1, 1));
         var5x.draw(new java.awt.geom.CubicCurve2D.Double(4.0, 8.0, 70.0, 3.0, 170.0, 10.0, 246.0, 26.0));
      });
   }

   private static void feather(
      Graphics2D var0, double var1, double var3, double var5, double var7, double var9, int var11, int var12, int var13, int var14, double var15
   ) {
      AffineTransform var17 = var0.getTransform();
      var0.translate(var1, var3);
      var0.rotate(var5 - (Math.PI / 2));
      GeneralPath var18 = new GeneralPath();
      var18.moveTo(0.0F, 0.0F);
      var18.curveTo(var9 * 0.7, var7 * 0.15, var9 * 0.62, var7 * 0.8, 0.0, var7);
      var18.curveTo(-var9 * 0.45, var7 * 0.82, -var9 * 0.52, var7 * 0.2, 0.0, 0.0);
      var18.closePath();
      var0.setPaint(
         new LinearGradientPaint(
            0.0F, 0.0F, 0.0F, (float)var7, new float[]{0.0F, 0.6F, 1.0F}, new Color[]{c(var11), c(var12), CTex.Art2.mix(c(var12), c(var14), var15)}
         )
      );
      var0.fill(var18);
      var0.setPaint(new GradientPaint((float)(-var9) / 2.0F, 0.0F, ca(0, 0), (float)var9 / 2.0F, 0.0F, ca(var13, 120)));
      var0.fill(var18);
      var0.setColor(ca(var13, 150));
      var0.setStroke(new BasicStroke(1.4F));
      var0.draw(var18);
      var0.setColor(ca(16777215, 150));
      var0.setStroke(new BasicStroke(1.2F));
      var0.draw(new java.awt.geom.QuadCurve2D.Double(0.0, 2.0, var9 * 0.12, var7 * 0.5, 0.0, var7 * 0.94));
      var0.setColor(ca(var13, 55));
      var0.setStroke(new BasicStroke(0.8F));

      for (double var19 = 0.12; var19 < 0.9; var19 += 0.07) {
         var0.drawLine(0, (int)(var7 * var19), (int)(var9 * 0.42), (int)(var7 * var19 + var9 * 0.35));
      }

      var0.setTransform(var17);
   }

   private static CTex.T membrane(int var0, int var1, int var2, int var3, boolean var4) {
      short var5 = 256;
      short var6 = 320;
      return make(
         var5,
         var6,
         var5x -> {
            double[][] var6x = new double[][]{{250.0, 30.0}, {240.0, 150.0}, {190.0, 260.0}, {110.0, 312.0}, {30.0, 250.0}};
            Double var7 = new Double();
            var7.moveTo(4.0, 8.0);
            var7.lineTo(var6x[0][0], var6x[0][1]);

            for (int var8 = 1; var8 < var6x.length; var8++) {
               double var9 = var6x[var8 - 1][0];
               double var11 = var6x[var8 - 1][1];
               double var13 = var6x[var8][0];
               double var15 = var6x[var8][1];
               double var17 = (var9 + var13) / 2.0;
               double var19 = (var11 + var15) / 2.0;
               double var21 = var17 - (var17 - 20.0) * 0.22;
               double var23 = var19 - (var19 - 10.0) * 0.22;
               var7.quadTo(var21, var23, var13, var15);
            }

            var7.quadTo(10.0, 140.0, 4.0, 40.0);
            var7.closePath();
            var5x.setPaint(new RadialGradientPaint(10.0F, 10.0F, 330.0F, new float[]{0.0F, 0.55F, 1.0F}, new Color[]{c(var2), c(var0), c(var1)}));
            var5x.fill(var7);
            var5x.setClip(var7);
            Random var25 = new Random(3L);
            var5x.setColor(ca(var1, 70));

            for (int var26 = 0; var26 < 40; var26++) {
               double[] var10 = var6x[var25.nextInt(var6x.length)];
               double var31 = 0.3 + var25.nextDouble() * 0.6;
               var5x.setStroke(new BasicStroke(0.8F));
               var5x.draw(
                  new java.awt.geom.QuadCurve2D.Double(
                     10.0 + (var10[0] - 10.0) * var31 * 0.5,
                     10.0 + (var10[1] - 10.0) * var31 * 0.5,
                     10.0 + (var10[0] - 10.0) * var31 + var25.nextGaussian() * 16.0,
                     10.0 + (var10[1] - 10.0) * var31 + var25.nextGaussian() * 16.0,
                     10.0 + (var10[0] - 10.0) * var31 * 1.2,
                     10.0 + (var10[1] - 10.0) * var31 * 1.2
                  )
               );
            }

            var5x.setClip(null);
            if (var4) {
               for (int var27 = 6; var27 >= 1; var27--) {
                  var5x.setColor(ca(var3, 20 + (6 - var27) * 25));
                  var5x.setStroke(new BasicStroke(var27 * 1.6F, 1, 1));
                  var5x.draw(var7);
               }
            } else {
               var5x.setColor(ca(var3, 150));
               var5x.setStroke(new BasicStroke(2.0F));
               var5x.draw(var7);
            }

            for (double[] var12 : var6x) {
               var5x.setStroke(new BasicStroke(7.0F, 1, 1));
               var5x.setColor(c(var2));
               var5x.draw(new java.awt.geom.QuadCurve2D.Double(6.0, 10.0, (var12[0] + 6.0) / 2.0 + 10.0, (var12[1] + 10.0) / 2.0 - 12.0, var12[0], var12[1]));
               var5x.setStroke(new BasicStroke(3.0F, 1, 1));
               var5x.setColor(var4 ? c(var3) : CTex.Art2.mix(c(var1), c(var3), 0.3));
               var5x.draw(new java.awt.geom.QuadCurve2D.Double(6.0, 10.0, (var12[0] + 6.0) / 2.0 + 10.0, (var12[1] + 10.0) / 2.0 - 12.0, var12[0], var12[1]));
               var5x.setColor(c(var2));
               var5x.fill(new java.awt.geom.Ellipse2D.Double(var12[0] - 4.0, var12[1] - 4.0, 8.0, 8.0));
            }

            var5x.setColor(c(var3));
            Double var29 = new Double();
            var29.moveTo(246.0, 26.0);
            var29.lineTo(256.0, 8.0);
            var29.lineTo(238.0, 22.0);
            var29.closePath();
            var5x.fill(var29);
         }
      );
   }

   private static CTex.T butterfly() {
      short var0 = 256;
      short var1 = 320;
      return make(
         var0,
         var1,
         var0x -> {
            Double var1x = new Double();
            var1x.moveTo(6.0, 30.0);
            var1x.curveTo(90.0, -30.0, 250.0, -10.0, 246.0, 80.0);
            var1x.curveTo(242.0, 140.0, 150.0, 170.0, 8.0, 150.0);
            var1x.closePath();
            Double var2 = new Double();
            var2.moveTo(8.0, 150.0);
            var2.curveTo(120.0, 150.0, 210.0, 200.0, 190.0, 270.0);
            var2.curveTo(170.0, 330.0, 60.0, 320.0, 6.0, 230.0);
            var2.closePath();

            for (Path2D var6 : new Path2D[]{var2, var1x}) {
               var0x.setPaint(
                  new RadialGradientPaint(
                     20.0F, 120.0F, 260.0F, new float[]{0.0F, 0.45F, 0.8F, 1.0F}, new Color[]{c(1780735), c(4172287), c(10178047), c(1312799)}
                  )
               );
               var0x.fill(var6);
               var0x.setColor(c(722450));
               var0x.setStroke(new BasicStroke(10.0F));
               var0x.draw(var6);
            }

            var0x.setClip(var1x);
            var0x.setColor(ca(0, 120));
            var0x.setStroke(new BasicStroke(2.2F));

            for (int var8 = 0; var8 < 6; var8++) {
               var0x.drawLine(8, 40 + var8 * 4, 250, -10 + var8 * 36);
            }

            var0x.setClip(null);
            var0x.setColor(Color.WHITE);
            double[][] var9 = new double[][]{{225.0, 40.0}, {232.0, 70.0}, {214.0, 100.0}, {190.0, 130.0}, {170.0, 262.0}, {150.0, 285.0}, {120.0, 298.0}};

            for (double[] var7 : var9) {
               var0x.fill(new java.awt.geom.Ellipse2D.Double(var7[0] - 5.0, var7[1] - 5.0, 10.0, 10.0));
            }

            var0x.setColor(ca(16769357, 230));
            var0x.fill(new java.awt.geom.Ellipse2D.Double(150.0, 55.0, 34.0, 34.0));
            var0x.setColor(c(722450));
            var0x.fill(new java.awt.geom.Ellipse2D.Double(158.0, 63.0, 18.0, 18.0));
         }
      );
   }

   private static CTex.T metal(int var0, int var1, int var2) {
      return make(
         128,
         128,
         var3 -> {
            var3.setPaint(
               new LinearGradientPaint(0.0F, 0.0F, 0.0F, 128.0F, new float[]{0.0F, 0.35F, 0.55F, 1.0F}, new Color[]{c(var0), c(var1), c(var2), c(var1)})
            );
            var3.fillRect(0, 0, 128, 128);
            Random var4 = new Random(5L);

            for (int var5 = 0; var5 < 400; var5++) {
               var3.setColor(ca(var4.nextBoolean() ? 16777215 : 0, 14));
               var3.fillRect(var4.nextInt(128), var4.nextInt(128), 1 + var4.nextInt(8), 1);
            }
         }
      );
   }

   private static CTex.T gems() {
      return make(
         128,
         32,
         var0 -> {
            int[] var1 = new int[]{14684511, 2003199, 3066993, 10181046};

            for (int var2 = 0; var2 < 4; var2++) {
               var0.setPaint(
                  new RadialGradientPaint(
                     var2 * 32 + 12,
                     10.0F,
                     22.0F,
                     new float[]{0.0F, 0.4F, 1.0F},
                     new Color[]{Color.WHITE, c(var1[var2]), CTex.Art2.mix(c(var1[var2]), Color.BLACK, 0.5)}
                  )
               );
               var0.fillRect(var2 * 32, 0, 32, 32);
            }
         }
      );
   }

   private static CTex.T felt(int var0, int var1, long var2) {
      return make(64, 64, var4 -> {
         var4.setPaint(new GradientPaint(0.0F, 0.0F, c(var1), 0.0F, 64.0F, c(var0)));
         var4.fillRect(0, 0, 64, 64);
         Random var5 = new Random(var2);

         for (int var6 = 0; var6 < 700; var6++) {
            var4.setColor(ca(var5.nextBoolean() ? 16777215 : 0, 10 + var5.nextInt(14)));
            var4.fillRect(var5.nextInt(64), var5.nextInt(64), 1, 1);
         }
      });
   }

   private static CTex.T fur() {
      return make(64, 64, var0 -> {
         var0.setColor(c(16053494));
         var0.fillRect(0, 0, 64, 64);
         Random var1 = new Random(9L);

         for (int var2 = 0; var2 < 300; var2++) {
            int var3 = var1.nextInt(64);
            int var4 = var1.nextInt(64);
            var0.setColor(ca(var1.nextBoolean() ? 16777215 : 13225174, 120));
            var0.fill(new java.awt.geom.Ellipse2D.Double(var3 - 3, var4 - 3, 6.0, 6.0));
         }
      });
   }

   private static CTex.T catEar() {
      return make(64, 64, var0 -> {
         Double var1 = new Double();
         var1.moveTo(2.0, 62.0);
         var1.quadTo(8.0, 20.0, 30.0, 2.0);
         var1.quadTo(56.0, 22.0, 62.0, 62.0);
         var1.closePath();
         var0.setColor(c(2829621));
         var0.fill(var1);
         Double var2 = new Double();
         var2.moveTo(14.0, 60.0);
         var2.quadTo(18.0, 30.0, 31.0, 16.0);
         var2.quadTo(46.0, 32.0, 50.0, 60.0);
         var2.closePath();
         var0.setPaint(new GradientPaint(0.0F, 16.0F, c(16757703), 0.0F, 60.0F, c(16743074)));
         var0.fill(var2);
      });
   }

   private static CTex.T bunnyEar() {
      return make(64, 160, var0 -> {
         java.awt.geom.RoundRectangle2D.Double var1 = new java.awt.geom.RoundRectangle2D.Double(4.0, 2.0, 56.0, 156.0, 56.0, 90.0);
         var0.setColor(c(16053494));
         var0.fill(var1);
         java.awt.geom.RoundRectangle2D.Double var2 = new java.awt.geom.RoundRectangle2D.Double(18.0, 18.0, 28.0, 128.0, 28.0, 60.0);
         var0.setPaint(new GradientPaint(0.0F, 18.0F, c(16761556), 0.0F, 146.0F, c(16748465)));
         var0.fill(var2);
      });
   }

   private static CTex.T sunglasses() {
      return make(
         256,
         64,
         var0 -> {
            var0.setColor(c(723727));
            java.awt.geom.RoundRectangle2D.Double var1 = new java.awt.geom.RoundRectangle2D.Double(10.0, 10.0, 104.0, 46.0, 22.0, 30.0);
            java.awt.geom.RoundRectangle2D.Double var2 = new java.awt.geom.RoundRectangle2D.Double(142.0, 10.0, 104.0, 46.0, 22.0, 30.0);
            var0.fill(var1);
            var0.fill(var2);
            var0.fillRect(100, 14, 56, 8);

            for (RoundRectangle2D var6 : new RoundRectangle2D[]{var1, var2}) {
               var0.setPaint(
                  new LinearGradientPaint(
                     (float)var6.getX(),
                     12.0F,
                     (float)var6.getX() + 100.0F,
                     56.0F,
                     new float[]{0.0F, 0.5F, 1.0F},
                     new Color[]{c(3808350), c(1315882), c(670282)}
                  )
               );
               var0.fill(
                  new java.awt.geom.RoundRectangle2D.Double(var6.getX() + 5.0, var6.getY() + 5.0, var6.getWidth() - 10.0, var6.getHeight() - 10.0, 16.0, 24.0)
               );
               var0.setColor(ca(16777215, 110));
               var0.setStroke(new BasicStroke(4.0F, 1, 1));
               var0.drawLine((int)var6.getX() + 18, 22, (int)var6.getX() + 40, 18);
            }
         }
      );
   }

   private static CTex.T pixelGlasses() {
      return make(64, 16, var0 -> {
         var0.setColor(Color.BLACK);
         var0.fillRect(0, 0, 64, 4);
         var0.fillRect(4, 4, 24, 8);
         var0.fillRect(36, 4, 24, 8);
         var0.fillRect(8, 12, 16, 4);
         var0.fillRect(40, 12, 16, 4);
         var0.setColor(Color.WHITE);
         var0.fillRect(8, 4, 4, 4);
         var0.fillRect(12, 8, 4, 4);
         var0.fillRect(40, 4, 4, 4);
         var0.fillRect(44, 8, 4, 4);
      });
   }

   private static CTex.T legoPlastic(int var0) {
      return make(64, 64, var1 -> {
         Color var2 = c(var0);
         var1.setPaint(new GradientPaint(0.0F, 0.0F, CTex.Art2.mix(var2, Color.WHITE, 0.18), 64.0F, 64.0F, CTex.Art2.mix(var2, Color.BLACK, 0.12)));
         var1.fillRect(0, 0, 64, 64);
         var1.setColor(CTex.Art2.mix(var2, Color.BLACK, 0.35));
         var1.setStroke(new BasicStroke(2.0F));
         var1.drawRect(1, 1, 62, 62);
         var1.setColor(ca(16777215, 60));
         var1.drawLine(3, 3, 60, 3);
      });
   }

   private static CTex.T sword() {
      return make(128, 128, var0 -> {
         AffineTransform var1 = var0.getTransform();
         var0.translate(64, 64);
         var0.rotate(Math.toRadians(-45.0));
         Double var2 = new Double();
         var2.moveTo(-7.0, -18.0);
         var2.lineTo(-7.0, -76.0);
         var2.lineTo(0.0, -88.0);
         var2.lineTo(7.0, -76.0);
         var2.lineTo(7.0, -18.0);
         var2.closePath();
         var0.setPaint(new GradientPaint(-7.0F, 0.0F, c(8385535), 7.0F, 0.0F, c(2071492)));
         var0.fill(var2);
         var0.setColor(ca(16777215, 190));
         var0.fillRect(-2, -80, 3, 60);
         var0.setColor(c(4927107));
         var0.fill(new java.awt.geom.RoundRectangle2D.Double(-22.0, -20.0, 44.0, 9.0, 6.0, 6.0));
         var0.setColor(c(3811866));
         var0.fillRect(-4, -12, 8, 30);
         var0.setColor(c(14725962));
         var0.fill(new java.awt.geom.Ellipse2D.Double(-6.0, 16.0, 12.0, 12.0));
         var0.setTransform(var1);
      });
   }

   private static CTex.T flame() {
      return make(
         64,
         96,
         var0 -> {
            Double var1 = new Double();
            var1.moveTo(32.0, 2.0);
            var1.curveTo(46.0, 30.0, 62.0, 50.0, 56.0, 72.0);
            var1.curveTo(50.0, 92.0, 14.0, 92.0, 8.0, 72.0);
            var1.curveTo(2.0, 52.0, 20.0, 36.0, 32.0, 2.0);
            var1.closePath();
            var0.setPaint(
               new LinearGradientPaint(0.0F, 0.0F, 0.0F, 96.0F, new float[]{0.0F, 0.45F, 1.0F}, new Color[]{ca(16726554, 0), c(16742938), c(16769899)})
            );
            var0.fill(var1);
            var0.setPaint(new RadialGradientPaint(32.0F, 72.0F, 22.0F, new float[]{0.0F, 1.0F}, new Color[]{ca(16777215, 230), ca(16769899, 0)}));
            var0.fill(new java.awt.geom.Ellipse2D.Double(10.0, 50.0, 44.0, 44.0));
         }
      );
   }

   private static CTex.T snowflake() {
      return make(64, 64, var0 -> {
         var0.translate(32, 32);
         var0.setColor(Color.WHITE);
         var0.setStroke(new BasicStroke(3.2F, 1, 1));

         for (int var1 = 0; var1 < 6; var1++) {
            var0.drawLine(0, 0, 0, -28);
            var0.drawLine(0, -16, -8, -24);
            var0.drawLine(0, -16, 8, -24);
            var0.rotate(Math.PI / 3);
         }
      });
   }

   private static CTex.T heartSprite() {
      return make(64, 64, var0 -> {
         GeneralPath var1 = new GeneralPath();
         var1.moveTo(32.0F, 58.0F);
         var1.curveTo(32.0F, 58.0F, 4.0F, 40.0F, 4.0F, 20.0F);
         var1.curveTo(4.0F, 8.0F, 14.0F, 3.0F, 22.0F, 3.0F);
         var1.curveTo(28.0F, 3.0F, 31.0F, 7.0F, 32.0F, 11.0F);
         var1.curveTo(33.0F, 7.0F, 36.0F, 3.0F, 42.0F, 3.0F);
         var1.curveTo(50.0F, 3.0F, 60.0F, 8.0F, 60.0F, 20.0F);
         var1.curveTo(60.0F, 40.0F, 32.0F, 58.0F, 32.0F, 58.0F);
         var1.closePath();
         var0.setPaint(new GradientPaint(0.0F, 0.0F, c(16743080), 0.0F, 64.0F, c(14684511)));
         var0.fill(var1);
         var0.setColor(ca(16777215, 150));
         var0.fill(new java.awt.geom.Ellipse2D.Double(14.0, 10.0, 12.0, 9.0));
      });
   }

   private static CTex.T orb(int var0) {
      return make(
         64,
         64,
         var1 -> {
            var1.setPaint(
               new RadialGradientPaint(
                  32.0F, 32.0F, 32.0F, new float[]{0.0F, 0.25F, 0.6F, 1.0F}, new Color[]{ca(16777215, 255), ca(var0, 230), ca(var0, 70), ca(var0, 0)}
               )
            );
            var1.fillRect(0, 0, 64, 64);
         }
      );
   }

   private static CTex.T spark() {
      return make(64, 64, var0 -> {
         var0.setPaint(new RadialGradientPaint(32.0F, 32.0F, 30.0F, new float[]{0.0F, 1.0F}, new Color[]{ca(16777215, 200), ca(16777215, 0)}));
         var0.fillRect(0, 0, 64, 64);
         var0.setColor(Color.WHITE);
         Double var1 = new Double();
         var1.moveTo(32.0, 2.0);
         var1.lineTo(36.0, 28.0);
         var1.lineTo(62.0, 32.0);
         var1.lineTo(36.0, 36.0);
         var1.lineTo(32.0, 62.0);
         var1.lineTo(28.0, 36.0);
         var1.lineTo(2.0, 32.0);
         var1.lineTo(28.0, 28.0);
         var1.closePath();
         var0.fill(var1);
      });
   }

   private static CTex.T boltSprite() {
      return make(64, 96, var0 -> {
         Double var1 = new Double();
         var1.moveTo(38.0, 2.0);
         var1.lineTo(8.0, 54.0);
         var1.lineTo(30.0, 54.0);
         var1.lineTo(22.0, 94.0);
         var1.lineTo(58.0, 36.0);
         var1.lineTo(36.0, 36.0);
         var1.closePath();

         for (int var2 = 6; var2 >= 1; var2--) {
            var0.setColor(ca(8189951, 25));
            var0.setStroke(new BasicStroke(var2 * 2.5F));
            var0.draw(var1);
         }

         var0.setColor(c(15138303));
         var0.fill(var1);
      });
   }

   static Rectangle2D rect(double var0, double var2, double var4, double var6) {
      return new java.awt.geom.Rectangle2D.Double(var0, var2, var4, var6);
   }

   static final class Art2 {
      static Color mix(Color var0, Color var1, double var2) {
         var2 = Math.max(0.0, Math.min(1.0, var2));
         return new Color(
            (int)(var0.getRed() + (var1.getRed() - var0.getRed()) * var2),
            (int)(var0.getGreen() + (var1.getGreen() - var0.getGreen()) * var2),
            (int)(var0.getBlue() + (var1.getBlue() - var0.getBlue()) * var2),
            (int)(var0.getAlpha() + (var1.getAlpha() - var0.getAlpha()) * var2)
         );
      }
   }

   public static final class T {
      public final int w;
      public final int h;
      public final int[] argb;

      T(int var1, int var2, int[] var3) {
         this.w = var1;
         this.h = var2;
         this.argb = var3;
      }

      public int sample(float var1, float var2) {
         int var3 = Math.max(0, Math.min(this.w - 1, (int)(var1 * this.w)));
         int var4 = Math.max(0, Math.min(this.h - 1, (int)(var2 * this.h)));
         return this.argb[var4 * this.w + var3];
      }
   }
}
