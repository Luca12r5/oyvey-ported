package dev.lego.cosmetic;

import dev.lego.ui.Gx;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.image.BufferedImage;
import java.util.Random;

final class VehTex {
   private VehTex() {
   }

   static CTex.T tex(String var0) {
      short var1 = 64;
      if (var0.equals("veh_plate") || var0.equals("veh_gauges") || var0.equals("veh_deckart") || var0.equals("veh_display") || var0.startsWith("veh_num")) {
         var1 = 128;
      }

      BufferedImage var2 = new BufferedImage(var1, var1, 2);
      Graphics2D var3 = var2.createGraphics();
      Gx.hints(var3);

      try {
         if (!paint(var3, var0, var1)) {
            return null;
         }
      } finally {
         var3.dispose();
      }

      return CTex.of(var2);
   }

   private static int hex(String var0, int var1) {
      try {
         return Integer.parseInt(var0.substring(var1, Math.min(var0.length(), var1 + 6)), 16);
      } catch (RuntimeException var3) {
         return 16711935;
      }
   }

   private static boolean paint(Graphics2D var0, String var1, int var2) {
      if (var1.startsWith("veh_c:")) {
         Color var14 = new Color(hex(var1, 6));
         var0.setPaint(new GradientPaint(0.0F, 0.0F, bright(var14, 1.2F), 0.0F, var2, bright(var14, 0.78F)));
         var0.fillRect(0, 0, var2, var2);
         return true;
      } else if (var1.startsWith("veh_flat:")) {
         var0.setColor(new Color(hex(var1, 9)));
         var0.fillRect(0, 0, var2, var2);
         noise(var0, var2, 12, 300);
         return true;
      } else if (var1.startsWith("veh_glow:")) {
         Color var13 = new Color(hex(var1, 9));
         var0.setColor(var13);
         var0.fillRect(0, 0, var2, var2);
         var0.setPaint(
            new RadialGradientPaint(
               var2 / 2.0F, var2 / 2.0F, var2 * 0.6F, new float[]{0.0F, 1.0F}, new Color[]{new Color(255, 255, 255, 150), new Color(255, 255, 255, 0)}
            )
         );
         var0.fillRect(0, 0, var2, var2);
         return true;
      } else if (var1.startsWith("veh_paint:")) {
         Color var12 = new Color(hex(var1, 10));
         var0.setPaint(
            new LinearGradientPaint(
               0.0F,
               0.0F,
               0.0F,
               var2,
               new float[]{0.0F, 0.3F, 0.44F, 0.46F, 0.7F, 1.0F},
               new Color[]{bright(var12, 1.45F), bright(var12, 1.18F), bright(var12, 1.05F), bright(var12, 0.62F), bright(var12, 0.82F), bright(var12, 0.5F)}
            )
         );
         var0.fillRect(0, 0, var2, var2);
         return true;
      } else if (var1.startsWith("veh_leather:")) {
         Color var3 = new Color(hex(var1, 12));
         var0.setColor(var3);
         var0.fillRect(0, 0, var2, var2);
         var0.setPaint(new GradientPaint(0.0F, 0.0F, bright(var3, 1.18F), 0.0F, var2, bright(var3, 0.85F)));
         var0.fillRect(0, 0, var2, var2);
         noise(var0, var2, 16, 700);
         var0.setColor(bright(var3, 1.5F));
         var0.setStroke(new BasicStroke(1.0F, 0, 0, 1.0F, new float[]{2.0F, 2.0F}, 0.0F));
         var0.drawLine(0, 4, var2, 4);
         var0.drawLine(0, var2 - 5, var2, var2 - 5);
         return true;
      } else {
         switch (var1) {
            case "veh_tread":
               var0.setColor(new Color(30, 30, 34));
               var0.fillRect(0, 0, var2, var2);
               var0.setColor(new Color(14, 14, 16));

               for (byte var29 = 0; var29 < var2; var29 += 8) {
                  var0.fillRect(var29, 0, 2, var2);
                  var0.fillRect(var29 + 4, var2 / 3, 2, var2 / 3);
               }

               var0.fillRect(0, var2 / 2 - 2, var2, 3);
               return true;
            case "veh_side":
               var0.setColor(new Color(36, 36, 40));
               var0.fillRect(0, 0, var2, var2);
               var0.setColor(new Color(58, 58, 64));
               var0.fillRect(0, var2 / 2 - 3, var2, 2);
               return true;
            case "veh_metal":
            case "veh_rim":
               var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(236, 240, 246), var2, var2, new Color(126, 134, 146)));
               var0.fillRect(0, 0, var2, var2);
               var0.setColor(new Color(255, 255, 255, 40));

               for (byte var28 = 0; var28 < var2; var28 += 3) {
                  var0.drawLine(0, var28, var2, var28);
               }

               return true;
            case "veh_chrome":
               var0.setPaint(
                  new LinearGradientPaint(
                     0.0F,
                     0.0F,
                     0.0F,
                     var2,
                     new float[]{0.0F, 0.4F, 0.5F, 0.62F, 1.0F},
                     new Color[]{new Color(255, 255, 255), new Color(190, 204, 222), new Color(70, 76, 88), new Color(220, 226, 234), new Color(150, 156, 166)}
                  )
               );
               var0.fillRect(0, 0, var2, var2);
               return true;
            case "veh_alu":
               var0.setColor(new Color(186, 192, 202));
               var0.fillRect(0, 0, var2, var2);
               Random var27 = new Random(5L);

               for (int var34 = 0; var34 < 220; var34++) {
                  var0.setColor(var27.nextBoolean() ? new Color(255, 255, 255, 50) : new Color(90, 96, 110, 40));
                  int var36 = var27.nextInt(var2);
                  var0.drawLine(0, var36, var2, var36);
               }

               return true;
            case "veh_gun":
               var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(96, 100, 112), var2, var2, new Color(38, 40, 48)));
               var0.fillRect(0, 0, var2, var2);
               return true;
            case "veh_disc":
               var0.setColor(new Color(150, 154, 160));
               var0.fillRect(0, 0, var2, var2);
               var0.setColor(new Color(90, 92, 98));

               for (byte var26 = 0; var26 < var2; var26 += 6) {
                  var0.fillOval(var26 + 1, var26 * 7 % (var2 - 6) + 2, 3, 3);
               }

               return true;
            case "veh_carbon":
               for (byte var25 = 0; var25 < var2; var25 += 4) {
                  for (byte var33 = 0; var33 < var2; var33 += 4) {
                     boolean var35 = ((var33 + var25) / 4 & 1) == 0;
                     var0.setPaint(
                        new GradientPaint(
                           var33,
                           var25,
                           var35 ? new Color(58, 60, 68) : new Color(24, 25, 30),
                           var33 + 4,
                           var25 + 4,
                           var35 ? new Color(30, 31, 36) : new Color(46, 48, 54)
                        )
                     );
                     var0.fillRect(var33, var25, 4, 4);
                  }
               }

               return true;
            case "veh_under":
               var0.setColor(new Color(22, 23, 27));
               var0.fillRect(0, 0, var2, var2);
               noise(var0, var2, 10, 200);
               return true;
            case "veh_interior":
               var0.setColor(new Color(34, 32, 34));
               var0.fillRect(0, 0, var2, var2);
               noise(var0, var2, 16, 900);
               return true;
            case "veh_grille":
               var0.setColor(new Color(16, 16, 20));
               var0.fillRect(0, 0, var2, var2);
               var0.setColor(new Color(62, 64, 72));
               var0.setStroke(new BasicStroke(1.4F));

               for (byte var24 = 0; var24 < var2; var24 += 6) {
                  for (int var32 = var24 / 6 % 2 * 4; var32 < var2; var32 += 8) {
                     var0.drawOval(var32, var24, 6, 5);
                  }
               }

               return true;
            case "veh_gauges":
               var0.setColor(new Color(18, 18, 22));
               var0.fillRect(0, 0, var2, var2);

               for (int var23 = 0; var23 < 2; var23++) {
                  int var31 = var2 / 4 + var23 * var2 / 2;
                  int var7 = var2 / 2;
                  int var8 = var2 / 5;
                  var0.setColor(new Color(40, 42, 50));
                  var0.fillOval(var31 - var8 - 3, var7 - var8 - 3, 2 * var8 + 6, 2 * var8 + 6);
                  var0.setColor(new Color(12, 12, 16));
                  var0.fillOval(var31 - var8, var7 - var8, 2 * var8, 2 * var8);
                  var0.setColor(new Color(255, 255, 255, 200));

                  for (int var9 = 0; var9 <= 8; var9++) {
                     double var10 = Math.toRadians(225.0 - var9 * 33.75);
                     var0.drawLine(
                        var31 + (int)(Math.cos(var10) * var8 * 0.7),
                        var7 - (int)(Math.sin(var10) * var8 * 0.7),
                        var31 + (int)(Math.cos(var10) * var8 * 0.92),
                        var7 - (int)(Math.sin(var10) * var8 * 0.92)
                     );
                  }

                  var0.setColor(new Color(255, 70, 40));
                  var0.setStroke(new BasicStroke(2.0F));
                  double var37 = Math.toRadians(var23 == 0 ? 120.0 : 60.0);
                  var0.drawLine(var31, var7, var31 + (int)(Math.cos(var37) * var8 * 0.85), var7 - (int)(Math.sin(var37) * var8 * 0.85));
               }

               return true;
            case "veh_display":
               var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(10, 30, 44), 0.0F, var2, new Color(4, 12, 20)));
               var0.fillRect(0, 0, var2, var2);
               var0.setColor(new Color(90, 230, 255));
               var0.setFont(new Font("SansSerif", 1, var2 / 3));
               var0.drawString("25", var2 / 5, var2 * 2 / 3);
               var0.setFont(new Font("SansSerif", 0, var2 / 8));
               var0.drawString("km/h", var2 * 3 / 5, var2 * 2 / 3);

               for (int var22 = 0; var22 < 5; var22++) {
                  var0.setColor(var22 < 4 ? new Color(80, 255, 120) : new Color(40, 70, 50));
                  var0.fillRect(var2 / 5 + var22 * var2 / 9, var2 * 4 / 5, var2 / 12, var2 / 14);
               }

               return true;
            case "veh_plate":
               var0.setColor(Color.WHITE);
               var0.fillRect(0, 0, var2, var2);
               var0.setColor(new Color(20, 60, 170));
               var0.fillRect(0, 0, var2 / 8, var2);
               var0.setColor(new Color(20, 20, 24));
               var0.setStroke(new BasicStroke(3.0F));
               var0.drawRect(2, var2 / 4 + 2, var2 - 4, var2 / 2 - 4);
               var0.setFont(new Font("SansSerif", 1, var2 / 4));
               var0.drawString("LEGO 1", var2 / 6, var2 * 5 / 8);
               return true;
            case "veh_rubber":
               var0.setColor(new Color(30, 31, 36));
               var0.fillRect(0, 0, var2, var2);
               var0.setColor(new Color(52, 54, 60));

               for (byte var21 = 0; var21 < var2; var21 += 5) {
                  var0.fillRect(0, var21, var2, 2);
               }

               return true;
            case "veh_gripr":
               var0.setColor(new Color(26, 26, 30));
               var0.fillRect(0, 0, var2, var2);
               var0.setColor(new Color(60, 62, 70));

               for (byte var20 = 0; var20 < var2; var20 += 6) {
                  var0.fillRect(0, var20, var2, 2);
               }

               return true;
            case "veh_foam":
               var0.setColor(new Color(34, 34, 38));
               var0.fillRect(0, 0, var2, var2);
               noise(var0, var2, 26, 1600);
               return true;
            case "veh_grip":
            case "veh_griptape":
               var0.setColor(new Color(34, 35, 40));
               var0.fillRect(0, 0, var2, var2);
               noise(var0, var2, 14, 1400);
               return true;
            case "veh_deck":
            case "veh_ply":
               Color[] var19 = new Color[]{
                  new Color(222, 176, 118), new Color(200, 60, 50), new Color(230, 190, 130), new Color(60, 120, 200), new Color(214, 168, 110)
               };

               for (int var30 = 0; var30 < var2; var30++) {
                  var0.setColor(var19[var30 * var19.length / var2]);
                  var0.drawLine(0, var30, var2, var30);
               }

               return true;
            case "veh_deckart":
               var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(255, 205, 3), var2, 0.0F, new Color(255, 120, 20)));
               var0.fillRect(0, 0, var2, var2);
               var0.setColor(new Color(227, 0, 11));

               for (int var18 = -var2; var18 < var2 * 2; var18 += 22) {
                  var0.fillPolygon(new int[]{var18, var18 + 10, var18 + 10 + var2, var18 + var2}, new int[]{0, 0, var2, var2}, 4);
               }

               var0.setColor(Color.WHITE);
               var0.fillRoundRect(var2 / 2 - 22, var2 / 2 - 16, 44, 32, 10, 10);
               var0.setColor(new Color(227, 0, 11));
               var0.setFont(new Font("SansSerif", 1, 20));
               var0.drawString("LEGO", var2 / 2 - 22, var2 / 2 + 7);
               return true;
            case "veh_urethane":
               var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(255, 248, 226), 0.0F, var2, new Color(226, 212, 178)));
               var0.fillRect(0, 0, var2, var2);
               return true;
            case "veh_pu":
               var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(120, 255, 214), 0.0F, var2, new Color(20, 170, 140)));
               var0.fillRect(0, 0, var2, var2);
               return true;
            case "veh_glass":
               var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(210, 238, 255, 110), var2, var2, new Color(90, 150, 210, 80)));
               var0.fillRect(0, 0, var2, var2);
               var0.setColor(new Color(255, 255, 255, 120));
               var0.fillRect(var2 / 6, 0, var2 / 10, var2);
               var0.fillRect(var2 / 6 + var2 / 7, 0, var2 / 30, var2);
               return true;
            case "veh_tint":
               var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(60, 70, 90, 180), 0.0F, var2, new Color(20, 24, 34, 200)));
               var0.fillRect(0, 0, var2, var2);
               var0.setColor(new Color(255, 255, 255, 70));
               var0.fillRect(var2 / 5, 0, var2 / 12, var2);
               return true;
            case "veh_mirror":
               var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(200, 225, 255), var2, var2, new Color(80, 100, 130)));
               var0.fillRect(0, 0, var2, var2);
               return true;
            case "veh_cloud":
               var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(255, 255, 255), 0.0F, var2, new Color(206, 220, 240)));
               var0.fillRect(0, 0, var2, var2);
               return true;
            case "veh_checker":
               for (int var17 = 0; var17 < 8; var17++) {
                  for (int var6 = 0; var6 < 8; var6++) {
                     var0.setColor((var6 + var17) % 2 == 0 ? Color.WHITE : new Color(20, 20, 24));
                     var0.fillRect(var6 * 8, var17 * 8, 8, 8);
                  }
               }

               return true;
            case "veh_ufo":
               var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(214, 222, 234), 0.0F, var2, new Color(120, 130, 146)));
               var0.fillRect(0, 0, var2, var2);
               var0.setColor(new Color(80, 88, 104, 150));

               for (byte var16 = 0; var16 < var2; var16 += 8) {
                  var0.drawLine(var16, 0, var16, var2);
               }

               var0.drawLine(0, var2 / 2, var2, var2 / 2);
               return true;
            case "veh_chain":
               var0.setColor(new Color(60, 62, 68));
               var0.fillRect(0, 0, var2, var2);
               var0.setColor(new Color(150, 154, 162));

               for (byte var15 = 0; var15 < var2; var15 += 8) {
                  var0.fillRoundRect(var15, var2 / 4, 6, var2 / 2, 3, 3);
               }

               return true;
            case "veh_num":
               var0.setColor(Color.WHITE);
               var0.fillRect(0, 0, var2, var2);
               var0.setColor(new Color(20, 20, 24));
               var0.setFont(new Font("SansSerif", 1, var2 * 3 / 4));
               var0.drawString("7", var2 / 3, var2 * 4 / 5);
               return true;
            case "veh_basket":
               var0.setColor(new Color(150, 104, 58));
               var0.fillRect(0, 0, var2, var2);
               var0.setColor(new Color(110, 72, 36));

               for (byte var5 = 0; var5 < var2; var5 += 6) {
                  var0.drawLine(var5, 0, var5, var2);
                  var0.drawLine(0, var5 + 3, var2, var5 + 3);
               }

               return true;
            default:
               return false;
         }
      }
   }

   static Color bright(Color var0, float var1) {
      return new Color(Math.min(255, (int)(var0.getRed() * var1)), Math.min(255, (int)(var0.getGreen() * var1)), Math.min(255, (int)(var0.getBlue() * var1)));
   }

   private static void noise(Graphics2D var0, int var1, int var2, int var3) {
      Random var4 = new Random(3L);

      for (int var5 = 0; var5 < var3; var5++) {
         var0.setColor(var4.nextBoolean() ? new Color(255, 255, 255, var2) : new Color(0, 0, 0, var2));
         var0.fillRect(var4.nextInt(var1), var4.nextInt(var1), 1, 1);
      }
   }
}
