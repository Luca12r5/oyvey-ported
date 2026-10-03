package dev.lego.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D.Double;
import java.awt.geom.Point2D.Float;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public final class Xmas {
   public static final int RED = -1890757;
   public static final int RED_DARK = -6418396;
   public static final int GOLD = -736942;
   public static final int GREEN = -14703780;
   public static final int GREEN_DARK = -15835596;
   public static final int ICE = -4201473;
   public static final int SNOW = -722689;
   public static final int NAVY = -16051162;
   public static final int[] BULBS = new int[]{-50354, -14006, -12853110, -11884289, -33835};
   private static final int N = 260;
   private static final float[] fx = new float[260];
   private static final float[] fy = new float[260];
   private static final float[] fz = new float[260];
   private static final float[] fs = new float[260];
   private static boolean seeded;

   private Xmas() {
   }

   public static boolean on() {
      return UiSettings.xmas;
   }

   public static String countdown() {
      LocalDate var0 = LocalDate.now();
      int var1 = var0.getYear();
      if (var0.getMonthValue() == 12 && var0.getDayOfMonth() >= 24 && var0.getDayOfMonth() <= 26) {
         return "Frohe Weihnachten!";
      } else {
         LocalDate var2 = LocalDate.of(var0.getMonthValue() == 12 && var0.getDayOfMonth() > 26 ? var1 + 1 : var1, 12, 24);
         long var3 = ChronoUnit.DAYS.between(var0, var2);
         return var3 == 1L ? "Morgen ist Heiligabend!" : "Noch " + var3 + " Tage bis Heiligabend";
      }
   }

   private static void seed() {
      Random var0 = new Random(7L);

      for (int var1 = 0; var1 < 260; var1++) {
         fx[var1] = var0.nextFloat();
         fy[var1] = var0.nextFloat();
         fz[var1] = 0.25F + var0.nextFloat() * 0.75F;
         fs[var1] = var0.nextFloat() * 100.0F;
      }

      seeded = true;
   }

   public static void backdrop(int var0, int var1, float var2) {
      if (on() && !(var2 <= 0.01F)) {
         Gx.pushAlpha(var2);
         Gx.gradientV(0, 0, var0, var1, -1710878686, 2047547952);
         float var3 = (float)(System.currentTimeMillis() % 1000000L) / 1000.0F;
         Gx.Img var4 = Gx.painted("xm_aurora", 512, 256, Xmas::paintAurora);
         if (var4 != null) {
            int var5 = var1 / 2;
            float var6 = (float)Math.sin(var3 * 0.15) * var0 * 0.03F;
            Gx.image(
               var4, Math.round(-var0 * 0.1F + var6), -var5 / 8, Math.round(var0 * 1.2F), var5, Gx.withAlpha(-1, 0.55F + 0.15F * (float)Math.sin(var3 * 0.4))
            );
         }

         drift(var0, var1);
         snow(var0, var1, 1.0F);
         Gx.popAlpha();
      }
   }

   public static void snow(int var0, int var1, float var2) {
      if (on()) {
         if (!seeded) {
            seed();
         }

         float var3 = Math.min(0.05F, Gx.dt);
         float var4 = (float)(System.currentTimeMillis() % 1000000L) / 1000.0F;
         Gx.Img var5 = Gx.painted("xm_flake", 32, 32, Xmas::paintFlake);
         Gx.Img var6 = Gx.painted("xm_crystal", 64, 64, Xmas::paintCrystal);
         if (var5 != null) {
            int var7 = Math.max(1, Math.min(var0, var1) / 540);

            for (int var8 = 0; var8 < 260; var8++) {
               float var9 = fz[var8];
               fy[var8] = fy[var8] + var3 * (0.035F + 0.07F * var9);
               fx[var8] = fx[var8] + var3 * (0.01F * (float)Math.sin(var4 * 0.7 + fs[var8]) * var9 + 0.004F);
               if (fy[var8] > 1.03F) {
                  fy[var8] = -0.03F;
                  fx[var8] = (fx[var8] * 7.31F + 0.37F) % 1.0F;
               }

               if (fx[var8] > 1.02F) {
                  fx[var8]--;
               }

               int var10 = Math.max(2, Math.round((1.2F + 3.6F * var9 * var9) * var7));
               int var11 = Math.round(fx[var8] * var0);
               int var12 = Math.round(fy[var8] * var1);
               float var13 = (0.35F + 0.6F * var9) * var2;
               if (var9 > 0.93F && var6 != null) {
                  int var14 = var10 * 3;
                  Gx.image(var6, var11 - var14 / 2, var12 - var14 / 2, var14, var14, Gx.withAlpha(-1, var13));
               } else {
                  Gx.image(var5, var11 - var10, var12 - var10, var10 * 2, var10 * 2, Gx.withAlpha(-1, var13));
               }
            }
         }
      }
   }

   private static void drift(int var0, int var1) {
      Gx.Img var2 = Gx.painted("xm_drift", 1024, 160, Xmas::paintDrift);
      if (var2 != null) {
         int var3 = Math.round(var1 * 0.14F);
         Gx.image(var2, 0, var1 - var3, var0, var3, -1);
      }
   }

   public static void snowCap(int var0, int var1, int var2, float var3) {
      if (on()) {
         Gx.Img var4 = Gx.painted("xm_cap", 512, 64, Xmas::paintCap);
         if (var4 != null) {
            int var5 = Math.round(var3 * 17.0F);
            int var6 = var5 * 8;
            int var7 = Math.round(var3 * 14.0F);

            for (int var8 = var0 + var7 / 2; var8 < var0 + var2 - var7 / 2; var8 += var6) {
               int var9 = Math.min(var6, var0 + var2 - var7 / 2 - var8);
               Gx.push();
               Gx.clip(var8, var1 - var5 / 2, var9, var5 * 2);
               Gx.image(var4, var8, var1 - Math.round(var5 * 0.42F), var6, var5, -1);
               Gx.unclip();
               Gx.pop();
            }
         }
      }
   }

   public static void lights(int var0, int var1, int var2, float var3, float var4, int var5) {
      if (on()) {
         float var6 = (float)(System.currentTimeMillis() % 1000000L) / 1000.0F;
         int var7 = Math.max(3, Math.round((var2 - var0) / (var3 * 34.0F)));
         Gx.Img var8 = Gx.painted("xm_bulb", 32, 48, Xmas::paintBulb);
         int var9 = Math.max(1, Math.round(var3 * 0.8F));
         int var10 = var7 * 4;

         for (int var11 = 0; var11 < var10; var11++) {
            float var12 = (float)var11 / var10;
            float var13 = (float)(var11 + 1) / var10;
            float var14 = var0 + (var2 - var0) * var12;
            float var15 = var0 + (var2 - var0) * var13;
            float var16 = (float)Math.sin(var12 * var7 * Math.PI);
            float var17 = (float)Math.sin(var13 * var7 * Math.PI);
            float var18 = var1 + Math.abs(var16) * var4;
            float var19 = var1 + Math.abs(var17) * var4;
            int var20 = Math.max(1, Math.round(var15 - var14));
            int var21 = Math.max(2, var9 * 5);

            for (int var22 = 0; var22 < var20; var22 += var21) {
               float var23 = (var22 + var21 * 0.5F) / var20;
               Gx.fill(Math.round(var14 + var22), Math.round(var18 + (var19 - var18) * Math.min(1.0F, var23)), Math.min(var21, var20 - var22), var9, -14861788);
            }
         }

         for (int var24 = 0; var24 < var7; var24++) {
            float var25 = (var24 + 0.5F) / var7;
            int var26 = Math.round(var0 + (var2 - var0) * var25);
            int var27 = Math.round(var1 + var4);
            int var28 = BULBS[(var24 + var5) % BULBS.length];
            float var29 = 0.55F + 0.45F * (float)Math.sin(var6 * (2.1 + var24 % 3 * 0.7) + var24 * 1.9 + var5);
            int var30 = Math.round(var3 * (9.0F + 4.0F * var29));
            Gx.glow(var26, var27 + Math.round(var3 * 4.0F), var30, Gx.withAlpha(var28, 0.45F * var29));
            if (var8 != null) {
               int var31 = Math.round(var3 * 5.2F);
               int var32 = Math.round(var3 * 7.8F);
               Gx.image(var8, var26 - var31 / 2, var27, var31, var32, Gx.mix(var28, -1, 0.25F * var29));
            }
         }
      }
   }

   public static void hat(float var0, float var1, float var2, float var3) {
      if (on()) {
         Gx.Img var4 = Gx.painted("xm_hat", 128, 128, Xmas::paintHat);
         if (var4 != null) {
            int var5 = Math.round(var2);
            Gx.image(var4, Math.round(var0 - var5 * 0.46F + var3), Math.round(var1 - var5 * 0.72F), var5, var5, -1);
         }
      }
   }

   public static void bauble(float var0, float var1, float var2, int var3) {
      Gx.Img var4 = Gx.painted("xm_bauble", 64, 64, Xmas::paintBauble);
      if (var4 != null) {
         int var5 = Math.round(var2 * 2.0F);
         Gx.image(var4, Math.round(var0 - var2), Math.round(var1 - var2), var5, var5, var3);
      }
   }

   public static void gift(int var0, int var1, int var2, int var3, int var4, int var5, float var6, float var7) {
      int var8 = Math.round(var7 * 7.0F);
      int var9 = Math.round(var1 - var6);
      Gx.shadow(var0, var9, var2, var3, var8, Math.round(var7 * (5.0F + var6 * 0.6F)), 0.45F);
      Gx.rectV(var0, var9, var2, var3, var8, Gx.mix(var4, -1, 0.12F), Gx.mix(var4, -16777216, 0.25F));
      int var10 = Math.max(2, Math.round(var2 * 0.14F));
      Gx.fill(var0 + var2 / 2 - var10 / 2, var9, var10, var3, var5);
      Gx.fill(var0, var9 + Math.round(var3 * 0.3F) - var10 / 2, var2, var10, var5);
      Gx.fill(var0 + var2 / 2 - var10 / 2, var9, Math.max(1, var10 / 4), var3, Gx.withAlpha(-1, 0.25F));
      Gx.Img var11 = Gx.painted("xm_bow", 128, 72, Xmas::paintBow);
      if (var11 != null) {
         int var12 = Math.round(var2 * 0.62F);
         int var13 = var12 * 72 / 128;
         Gx.image(var11, var0 + var2 / 2 - var12 / 2, var9 + Math.round(var3 * 0.3F) - var13 + Math.round(var13 * 0.35F), var12, var13, var5);
      }

      Gx.outline(var0, var9, var2, var3, var8, Math.max(1, Math.round(var7 * 0.6F)), 587202559);
   }

   public static void candy(int var0, int var1, int var2, int var3, float var4) {
      int var5 = Math.max(8, var2);
      int var6 = Math.max(4, var3);
      Gx.Img var7 = Gx.painted("xm_candy" + var5 + "x" + var6, var5 * 2, var6 * 2, var2x -> paintCandy(var2x, var5 * 2, var6 * 2));
      if (var7 != null) {
         Gx.image(var7, var0, var1, var2, var3, -1);
      }
   }

   private static void paintFlake(Graphics2D var0) {
      var0.setPaint(
         new RadialGradientPaint(
            new Float(16.0F, 16.0F),
            16.0F,
            new float[]{0.0F, 0.35F, 1.0F},
            new Color[]{new Color(255, 255, 255, 255), new Color(235, 245, 255, 190), new Color(220, 235, 255, 0)}
         )
      );
      var0.fill(new java.awt.geom.Ellipse2D.Float(0.0F, 0.0F, 32.0F, 32.0F));
   }

   private static void paintCrystal(Graphics2D var0) {
      var0.translate(32, 32);
      var0.setColor(new Color(255, 255, 255, 235));
      var0.setStroke(new BasicStroke(2.6F, 1, 1));

      for (int var1 = 0; var1 < 6; var1++) {
         var0.drawLine(0, 0, 0, -27);
         var0.drawLine(0, -12, -7, -19);
         var0.drawLine(0, -12, 7, -19);
         var0.drawLine(0, -20, -5, -25);
         var0.drawLine(0, -20, 5, -25);
         var0.rotate(Math.PI / 3);
      }

      var0.setPaint(
         new RadialGradientPaint(new Float(0.0F, 0.0F), 10.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(255, 255, 255, 255), new Color(255, 255, 255, 0)})
      );
      var0.fill(new java.awt.geom.Ellipse2D.Float(-10.0F, -10.0F, 20.0F, 20.0F));
   }

   private static void paintAurora(Graphics2D var0) {
      Random var1 = new Random(3L);
      int[][] var2 = new int[][]{{60, 255, 170}, {80, 200, 255}, {170, 110, 255}};

      for (int var3 = 0; var3 < 3; var3++) {
         int[] var4 = var2[var3];
         double var5 = var1.nextDouble() * 6.0;
         double var7 = 20 + var3 * 8;
         double var9 = 70 + var3 * 30;

         for (byte var11 = 0; var11 < 512; var11 += 2) {
            double var12 = var9 + Math.sin(var11 / 80.0 + var5) * var7 + Math.sin(var11 / 31.0 + var5 * 2.0) * 8.0;
            double var14 = Math.sin(Math.PI * var11 / 512.0);
            int var16 = (int)(70.0 * var14);
            var0.setPaint(
               new GradientPaint(
                  var11, (float)(var12 - 60.0), new Color(var4[0], var4[1], var4[2], 0), var11, (float)var12, new Color(var4[0], var4[1], var4[2], var16)
               )
            );
            var0.fillRect(var11, (int)(var12 - 60.0), 2, 60);
            var0.setPaint(
               new GradientPaint(
                  var11, (float)var12, new Color(var4[0], var4[1], var4[2], var16), var11, (float)(var12 + 14.0), new Color(var4[0], var4[1], var4[2], 0)
               )
            );
            var0.fillRect(var11, (int)var12, 2, 14);
         }
      }
   }

   private static void paintDrift(Graphics2D var0) {
      for (int var1 = 0; var1 < 2; var1++) {
         Double var2 = new Double();
         var2.moveTo(0.0, 160.0);
         Random var3 = new Random(11 + var1);
         double var4 = var1 == 0 ? 70.0 : 100.0;

         for (byte var6 = 0; var6 <= 1024; var6 += 8) {
            double var7 = var4 + Math.sin(var6 / (140.0 + var1 * 60) + var1 * 2) * 22.0 + Math.sin(var6 / 47.0 + var1) * 6.0;
            var2.lineTo(var6, var7);
         }

         var2.lineTo(1024.0, 160.0);
         var2.closePath();
         var0.setPaint(
            new GradientPaint(
               0.0F,
               (float)var4 - 20.0F,
               var1 == 0 ? new Color(200, 222, 255, 140) : new Color(244, 248, 255, 235),
               0.0F,
               160.0F,
               var1 == 0 ? new Color(120, 150, 210, 180) : new Color(190, 212, 245, 255)
            )
         );
         var0.fill(var2);
         if (var1 == 1) {
            var0.setColor(new Color(255, 255, 255, 180));

            for (int var9 = 0; var9 < 90; var9++) {
               float var10 = var3.nextFloat() * 1024.0F;
               float var8 = 110.0F + var3.nextFloat() * 50.0F;
               var0.fill(new java.awt.geom.Ellipse2D.Float(var10, var8, 2.0F, 2.0F));
            }
         }
      }
   }

   private static void paintCap(Graphics2D var0) {
      Double var1 = new Double();
      var1.moveTo(0.0, 34.0);
      Random var2 = new Random(5L);
      double var3 = 10.0;

      for (byte var5 = 0; var5 <= 512; var5 += 32) {
         double var6 = 6.0 + Math.sin(var5 / 61.0) * 4.0 + var2.nextDouble() * 4.0;
         var1.quadTo(var5 - 16, Math.min(var3, var6) - 3.0, var5, var6);
         var3 = var6;
      }

      var1.lineTo(512.0, 34.0);

      for (short var9 = 512; var9 >= 0; var9 -= 8) {
         boolean var12 = var9 % 56 == 0 || var9 % 88 == 24;
         if (var12) {
            double var7 = 14 + var9 * 7 % 16;
            var1.lineTo(var9 + 3, 35.0);
            var1.lineTo(var9, 34.0 + var7);
            var1.lineTo(var9 - 3, 35.0);
         } else {
            var1.lineTo(var9, 33.0 + Math.sin(var9 / 23.0) * 2.5);
         }
      }

      var1.closePath();
      var0.setColor(new Color(40, 60, 110, 90));
      var0.translate(0, 3);
      var0.fill(var1);
      var0.translate(0, -3);
      var0.setPaint(new GradientPaint(0.0F, 4.0F, new Color(255, 255, 255), 0.0F, 50.0F, new Color(186, 212, 248)));
      var0.fill(var1);
      var0.setColor(new Color(255, 255, 255, 200));
      var0.setStroke(new BasicStroke(2.0F));

      for (byte var10 = 10; var10 < 512; var10 += 37) {
         var0.drawLine(var10, 12, var10 + 12, 11);
      }

      var0.setColor(new Color(150, 190, 240, 120));

      for (byte var11 = 0; var11 < 512; var11 += 3) {
         var0.fillRect(var11, 30 + (int)(Math.sin(var11 / 23.0) * 2.5), 3, 2);
      }
   }

   private static void paintBulb(Graphics2D var0) {
      var0.setColor(new Color(60, 70, 60));
      var0.fillRoundRect(10, 0, 12, 10, 3, 3);
      GeneralPath var1 = new GeneralPath();
      var1.moveTo(16.0F, 8.0F);
      var1.curveTo(30.0F, 12.0F, 30.0F, 40.0F, 16.0F, 47.0F);
      var1.curveTo(2.0F, 40.0F, 2.0F, 12.0F, 16.0F, 8.0F);
      var0.setPaint(
         new RadialGradientPaint(
            new Float(12.0F, 22.0F),
            26.0F,
            new float[]{0.0F, 0.3F, 1.0F},
            new Color[]{new Color(255, 255, 255), new Color(255, 255, 255, 240), new Color(200, 200, 200, 230)}
         )
      );
      var0.fill(var1);
      var0.setColor(new Color(255, 255, 255, 230));
      var0.fill(new java.awt.geom.Ellipse2D.Float(10.0F, 16.0F, 5.0F, 11.0F));
   }

   private static void paintHat(Graphics2D var0) {
      GeneralPath var1 = new GeneralPath();
      var1.moveTo(18.0F, 92.0F);
      var1.curveTo(30.0F, 40.0F, 60.0F, 14.0F, 96.0F, 22.0F);
      var1.curveTo(112.0F, 26.0F, 118.0F, 44.0F, 114.0F, 60.0F);
      var1.curveTo(104.0F, 50.0F, 92.0F, 48.0F, 88.0F, 58.0F);
      var1.curveTo(96.0F, 70.0F, 104.0F, 80.0F, 110.0F, 92.0F);
      var1.closePath();
      var0.setPaint(new GradientPaint(20.0F, 30.0F, new Color(16730714), 100.0F, 90.0F, new Color(10358820)));
      var0.fill(var1);
      var0.setColor(new Color(0, 0, 0, 40));
      var0.setStroke(new BasicStroke(3.0F));
      var0.draw(new java.awt.geom.QuadCurve2D.Float(40.0F, 80.0F, 60.0F, 50.0F, 92.0F, 30.0F));
      var0.setPaint(new RadialGradientPaint(new Float(110.0F, 60.0F), 14.0F, new float[]{0.0F, 1.0F}, new Color[]{Color.WHITE, new Color(205, 220, 240)}));
      var0.fill(new java.awt.geom.Ellipse2D.Float(98.0F, 50.0F, 26.0F, 26.0F));
      java.awt.geom.RoundRectangle2D.Float var2 = new java.awt.geom.RoundRectangle2D.Float(8.0F, 86.0F, 112.0F, 26.0F, 26.0F, 26.0F);
      var0.setPaint(new GradientPaint(0.0F, 86.0F, Color.WHITE, 0.0F, 112.0F, new Color(200, 216, 240)));
      var0.fill(var2);
      var0.setColor(new Color(255, 255, 255, 200));
      Random var3 = new Random(2L);

      for (int var4 = 0; var4 < 40; var4++) {
         var0.fill(new java.awt.geom.Ellipse2D.Float(10.0F + var3.nextFloat() * 106.0F, 88.0F + var3.nextFloat() * 20.0F, 5.0F, 5.0F));
      }
   }

   private static void paintBauble(Graphics2D var0) {
      var0.setColor(new Color(200, 170, 80));
      var0.fillRoundRect(26, 2, 12, 9, 3, 3);
      var0.setPaint(
         new RadialGradientPaint(
            new Float(24.0F, 26.0F),
            36.0F,
            new float[]{0.0F, 0.25F, 0.7F, 1.0F},
            new Color[]{new Color(255, 255, 255), new Color(250, 250, 250), new Color(170, 170, 170), new Color(90, 90, 90)},
            CycleMethod.NO_CYCLE
         )
      );
      var0.fill(new java.awt.geom.Ellipse2D.Float(6.0F, 9.0F, 52.0F, 52.0F));
      var0.setColor(new Color(255, 255, 255, 170));
      var0.fill(new java.awt.geom.Ellipse2D.Float(16.0F, 18.0F, 10.0F, 14.0F));
      var0.setColor(new Color(255, 255, 255, 90));
      var0.setStroke(new BasicStroke(2.2F));
      var0.drawArc(10, 26, 44, 14, 180, 180);
   }

   private static void paintBow(Graphics2D var0) {
      var0.setColor(Color.WHITE);
      GeneralPath var1 = new GeneralPath();
      var1.moveTo(64.0F, 50.0F);
      var1.curveTo(40.0F, 10.0F, 6.0F, 14.0F, 14.0F, 44.0F);
      var1.curveTo(20.0F, 60.0F, 44.0F, 60.0F, 64.0F, 50.0F);
      GeneralPath var2 = new GeneralPath();
      var2.moveTo(64.0F, 50.0F);
      var2.curveTo(88.0F, 10.0F, 122.0F, 14.0F, 114.0F, 44.0F);
      var2.curveTo(108.0F, 60.0F, 84.0F, 60.0F, 64.0F, 50.0F);
      var0.fill(var1);
      var0.fill(var2);
      var0.setColor(new Color(0, 0, 0, 60));
      var0.setStroke(new BasicStroke(3.0F));
      var0.draw(new java.awt.geom.QuadCurve2D.Float(24.0F, 40.0F, 40.0F, 28.0F, 58.0F, 46.0F));
      var0.draw(new java.awt.geom.QuadCurve2D.Float(104.0F, 40.0F, 88.0F, 28.0F, 70.0F, 46.0F));
      var0.setColor(Color.WHITE);
      var0.fill(new java.awt.geom.Ellipse2D.Float(54.0F, 40.0F, 20.0F, 18.0F));
      var0.fill(new Polygon(new int[]{58, 48, 58}, new int[]{54, 72, 70}, 3));
      var0.fill(new Polygon(new int[]{70, 80, 70}, new int[]{54, 72, 70}, 3));
   }

   private static void paintCandy(Graphics2D var0, int var1, int var2) {
      java.awt.geom.RoundRectangle2D.Float var3 = new java.awt.geom.RoundRectangle2D.Float(0.0F, 0.0F, var1, var2, var2, var2);
      var0.setClip(var3);
      var0.setColor(new Color(14886459));
      var0.fillRect(0, 0, var1, var2);
      var0.setColor(new Color(255, 255, 255));
      int var4 = Math.max(6, var2 / 2);

      for (int var5 = -var2; var5 < var1 + var2; var5 += var4 * 2) {
         GeneralPath var6 = new GeneralPath();
         var6.moveTo(var5, var2);
         var6.lineTo(var5 + var4, var2);
         var6.lineTo(var5 + var4 + var2, 0.0F);
         var6.lineTo(var5 + var2, 0.0F);
         var6.closePath();
         var0.fill(var6);
      }

      var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(255, 255, 255, 90), 0.0F, var2, new Color(0, 0, 0, 70)));
      var0.fillRect(0, 0, var1, var2);
   }
}
