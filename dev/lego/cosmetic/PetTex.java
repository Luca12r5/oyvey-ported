package dev.lego.cosmetic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Ellipse2D.Double;
import java.awt.geom.Ellipse2D.Float;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

final class PetTex {
   static final int FW = 256;
   static final int FH = 192;
   private static final Map<String, int[]> FUR = new HashMap<>();
   private static final double EY = 84.0;
   private static final double EW = 48.0;
   private static final double EH = 60.0;
   private static final double EX = 50.0;

   private PetTex() {
   }

   static CTex.T paint(String var0) {
      try {
         String[] var1 = var0.split(":");
         String var2 = var1[0];
         switch (var2) {
            case "pet_eyes":
               return CTex.of(eyes(style(var1[1]), var1[2], "1".equals(var1[3])));
            case "pet_face":
               return CTex.of(face(style(var1[1]), var1[2]));
            case "pet_fur":
               return CTex.of(fur(var1[1], false));
            case "pet_head":
               return CTex.of(fur(var1[1], true));
            case "pet_c":
               return CTex.of(solid(hex(var1[1])));
            case "pet_gl":
               return CTex.of(gloss(hex(var1[1])));
            case "pet_metal":
               return CTex.of(metal(hex(var1[1])));
            case "pet_star":
               return CTex.of(star());
            case "pet_heart":
               return CTex.of(heartTex());
            case "pet_shell":
               return CTex.of(shell());
            case "pet_screen":
               return CTex.of(screen());
            case "pet_wing":
               return CTex.of(beeWing());
            case "pet_bat_wing":
               return CTex.of(batWing());
            case "pet_dragon_wing":
               return CTex.of(dragonWing());
            case "pet_feather":
               return CTex.of(featherWing(hex(var1[1]), hex(var1[2])));
            case "pet_dwing":
               return CTex.of(drakeWing(hex(var1[1]), hex(var1[2])));
            case "pet_tailfan":
               return CTex.of(tailFan(hex(var1[1]), hex(var1[2])));
            case "pet_bubble":
               return CTex.of(bubble());
            case "pet_mane":
               return CTex.of(mane());
            case "pet_horn":
               return CTex.of(horn());
            case "pet_fin":
               return CTex.of(fin(var1.length > 1 ? hex(var1[1]) : 16747069));
            case "pet_cap":
               return CTex.of(mushCap());
            case "pet_jelly":
               return CTex.of(jelly());
            case "pet_berry":
               return CTex.of(berry());
            case "pet_brick":
               return CTex.of(brick(var1.length > 1 ? hex(var1[1]) : 14876683));
            default:
               return null;
         }
      } catch (RuntimeException var4) {
         return null;
      }
   }

   private static int hex(String var0) {
      return Integer.parseInt(var0, 16) & 16777215;
   }

   private static BufferedImage img(int var0, int var1) {
      return new BufferedImage(var0, var1, 2);
   }

   private static Graphics2D gfx(BufferedImage var0) {
      Graphics2D var1 = var0.createGraphics();
      var1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var1.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      var1.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var1.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      return var1;
   }

   static Color c(int var0) {
      return new Color(var0 & 16777215);
   }

   static Color ca(int var0, int var1) {
      return new Color(var0 >> 16 & 0xFF, var0 >> 8 & 0xFF, var0 & 0xFF, Math.max(0, Math.min(255, var1)));
   }

   static int mix(int var0, int var1, float var2) {
      int var3 = Math.round((var0 >> 16 & 0xFF) * (1.0F - var2) + (var1 >> 16 & 0xFF) * var2);
      int var4 = Math.round((var0 >> 8 & 0xFF) * (1.0F - var2) + (var1 >> 8 & 0xFF) * var2);
      int var5 = Math.round((var0 & 0xFF) * (1.0F - var2) + (var1 & 0xFF) * var2);
      return var3 << 16 | var4 << 8 | var5;
   }

   static int light(int var0, float var1) {
      return mix(var0, 16777215, var1);
   }

   static int dark(int var0, float var1) {
      return mix(var0, 0, var1);
   }

   static int shade(int var0, float var1) {
      return mix(var0, mix(dark(var0, 0.55F), 3809098, 0.25F), var1);
   }

   private static Ellipse2D el(double var0, double var2, double var4, double var6) {
      return new Double(var0 - var4 / 2.0, var2 - var6 / 2.0, var4, var6);
   }

   private static void soft(Graphics2D var0, double var1, double var3, double var5, double var7, int var9, int var10) {
      float var11 = (float)Math.max(var5, var7) / 2.0F;
      if (!(var11 < 0.5F)) {
         AffineTransform var12 = var0.getTransform();
         var0.translate(var1, var3);
         var0.scale(var5 / (2.0F * var11), var7 / (2.0F * var11));
         var0.setPaint(
            new RadialGradientPaint(0.0F, 0.0F, var11, new float[]{0.0F, 0.55F, 1.0F}, new Color[]{ca(var9, var10), ca(var9, var10 * 3 / 4), ca(var9, 0)})
         );
         var0.fill(new Float(-var11, -var11, 2.0F * var11, 2.0F * var11));
         var0.setTransform(var12);
      }
   }

   private static void baseGrad(Graphics2D var0, int var1, int var2, int var3) {
      var0.setPaint(
         new LinearGradientPaint(0.0F, 0.0F, 0.0F, var2, new float[]{0.0F, 0.45F, 1.0F}, new Color[]{c(light(var3, 0.16F)), c(var3), c(shade(var3, 0.28F))})
      );
      var0.fillRect(0, 0, var1, var2);
   }

   private static void furStrokes(Graphics2D var0, int var1, int var2, int var3, long var4, int var6, int var7) {
      Random var8 = new Random(var4);
      var0.setStroke(new BasicStroke(1.3F, 1, 1));

      for (int var9 = 0; var9 < var6; var9++) {
         float var10 = var8.nextFloat() * var1;
         float var11 = var8.nextFloat() * var2;
         float var12 = 2.0F + var8.nextFloat() * 4.0F;
         double var13 = (Math.PI / 2) + (var8.nextFloat() - 0.5) * 0.9;
         var0.setColor(var8.nextBoolean() ? ca(light(var3, 0.35F), var7) : ca(shade(var3, 0.4F), var7));
         var0.draw(new java.awt.geom.Line2D.Float(var10, var11, var10 + (float)Math.cos(var13) * var12, var11 + (float)Math.sin(var13) * var12));
      }
   }

   private static double sx(int var0, double var1) {
      return var0 * (0.25 - var1 / (Math.PI * 2));
   }

   private static double sy(int var0, double var1) {
      return var0 * (0.5 - var1 / Math.PI);
   }

   private static void patch(Graphics2D var0, int var1, int var2, double var3, double var5, double var7, double var9, int var11, int var12) {
      double var13 = sx(var1, var3);
      double var15 = sy(var2, var5);
      double var17 = var7 / Math.PI * var1;
      double var19 = var9 / Math.PI * var2 * 2.0;
      soft(var0, var13, var15, var17 * 2.2, var19 * 1.15, var11, var12);
      var0.setColor(ca(var11, var12));
      var0.fill(el(var13, var15, var17 * 1.7, var19 * 0.9));
   }

   static BufferedImage solid(int var0) {
      BufferedImage var1 = img(32, 32);
      Graphics2D var2 = gfx(var1);
      baseGrad(var2, 32, 32, var0);
      furStrokes(var2, 32, 32, var0, var0, 40, 22);
      var2.dispose();
      return var1;
   }

   static BufferedImage gloss(int var0) {
      short var1 = 128;
      byte var2 = 64;
      BufferedImage var3 = img(var1, var2);
      Graphics2D var4 = gfx(var3);
      baseGrad(var4, var1, var2, var0);
      soft(var4, sx(var1, 0.35), sy(var2, 0.55), 30.0, 22.0, 16777215, 190);
      var4.setColor(ca(16777215, 235));
      var4.fill(el(sx(var1, 0.35), sy(var2, 0.58), 10.0, 6.0));
      soft(var4, sx(var1, -0.9), sy(var2, -0.6), 40.0, 14.0, light(var0, 0.5F), 90);
      var4.dispose();
      return var3;
   }

   static BufferedImage metal(int var0) {
      byte var1 = 64;
      byte var2 = 64;
      BufferedImage var3 = img(var1, var2);
      Graphics2D var4 = gfx(var3);
      baseGrad(var4, var1, var2, var0);
      Random var5 = new Random(7L);

      for (int var6 = 0; var6 < var2; var6++) {
         var4.setColor(ca(var5.nextBoolean() ? 16777215 : 0, 10 + var5.nextInt(14)));
         var4.fillRect(0, var6, var1, 1);
      }

      var4.setColor(ca(16777215, 60));
      var4.fillRect(0, 2, var1, 3);
      var4.dispose();
      return var3;
   }

   private static Shape starShape(double var0, double var2, double var4, double var6, int var8, double var9) {
      java.awt.geom.Path2D.Double var11 = new java.awt.geom.Path2D.Double();

      for (int var12 = 0; var12 < var8 * 2; var12++) {
         double var13 = var9 - (Math.PI / 2) + Math.PI * var12 / var8;
         double var15 = var12 % 2 == 0 ? var4 : var6;
         double var17 = var0 + Math.cos(var13) * var15;
         double var19 = var2 + Math.sin(var13) * var15;
         if (var12 == 0) {
            var11.moveTo(var17, var19);
         } else {
            var11.lineTo(var17, var19);
         }
      }

      var11.closePath();
      return var11;
   }

   private static void roundStar(Graphics2D var0, double var1, double var3, double var5, Color var7, Color var8, float var9) {
      Shape var10 = starShape(var1, var3, var5, var5 * 0.5, 5, 0.0);
      var0.setPaint(var7);
      var0.fill(var10);
      var0.setStroke(new BasicStroke(var9, 1, 1));
      var0.setColor(var8);
      var0.draw(var10);
   }

   private static Shape heart(double var0, double var2, double var4) {
      java.awt.geom.Path2D.Double var6 = new java.awt.geom.Path2D.Double();
      var6.moveTo(var0, var2 + var4 * 0.42);
      var6.curveTo(var0 - var4 * 0.62, var2 - var4 * 0.02, var0 - var4 * 0.48, var2 - var4 * 0.58, var0 - var4 * 0.02, var2 - var4 * 0.3);
      var6.lineTo(var0, var2 - var4 * 0.26);
      var6.lineTo(var0 + var4 * 0.02, var2 - var4 * 0.3);
      var6.curveTo(var0 + var4 * 0.48, var2 - var4 * 0.58, var0 + var4 * 0.62, var2 - var4 * 0.02, var0, var2 + var4 * 0.42);
      var6.closePath();
      return var6;
   }

   static BufferedImage star() {
      BufferedImage var0 = img(64, 64);
      Graphics2D var1 = gfx(var0);
      soft(var1, 32.0, 32.0, 64.0, 64.0, 16769658, 120);
      roundStar(var1, 32.0, 33.0, 22.0, c(16767050), c(16774336), 3.0F);
      var1.setPaint(new GradientPaint(0.0F, 12.0F, c(16775368), 0.0F, 54.0F, c(16756782)));
      var1.fill(starShape(32.0, 33.0, 19.0, 9.5, 5, 0.0));
      var1.setColor(ca(16777215, 220));
      var1.fill(el(27.0, 26.0, 7.0, 5.0));
      var1.dispose();
      return var0;
   }

   static BufferedImage heartTex() {
      BufferedImage var0 = img(64, 64);
      Graphics2D var1 = gfx(var0);
      soft(var1, 32.0, 32.0, 64.0, 64.0, 16740264, 110);
      var1.setPaint(new GradientPaint(0.0F, 10.0F, c(16747960), 0.0F, 54.0F, c(15741034)));
      var1.fill(heart(32.0, 34.0, 52.0));
      var1.setColor(ca(16777215, 210));
      var1.fill(el(22.0, 24.0, 9.0, 6.0));
      var1.dispose();
      return var0;
   }

   static BufferedImage shell() {
      short var0 = 256;
      short var1 = 128;
      BufferedImage var2 = img(var0, var1);
      Graphics2D var3 = gfx(var2);
      baseGrad(var3, var0, var1, 6138446);
      Random var4 = new Random(3L);

      for (int var5 = 0; var5 < 5; var5++) {
         for (int var6 = 0; var6 < 9; var6++) {
            double var7 = var6 * 29 + var5 % 2 * 14.5 + 6.0;
            double var9 = var5 * 22 + 10;
            java.awt.geom.Path2D.Double var11 = new java.awt.geom.Path2D.Double();

            for (int var12 = 0; var12 < 6; var12++) {
               double var13 = (Math.PI / 3) * var12 + (Math.PI / 6);
               double var15 = var7 + Math.cos(var13) * 12.0;
               double var17 = var9 + Math.sin(var13) * 10.0;
               if (var12 == 0) {
                  var11.moveTo(var15, var17);
               } else {
                  var11.lineTo(var15, var17);
               }
            }

            var11.closePath();
            var3.setPaint(new GradientPaint(0.0F, (float)var9 - 10.0F, c(10148714), 0.0F, (float)var9 + 10.0F, c(6271053)));
            var3.fill(var11);
            var3.setColor(ca(16777215, 40 + var4.nextInt(30)));
            var3.fill(el(var7 - 3.0, var9 - 3.0, 8.0, 5.0));
            var3.setStroke(new BasicStroke(2.4F, 1, 1));
            var3.setColor(c(4028980));
            var3.draw(var11);
         }
      }

      var3.setPaint(new GradientPaint(0.0F, 108.0F, c(15323770), 0.0F, 128.0F, c(12096574)));
      var3.fillRect(0, 106, var0, 22);
      var3.dispose();
      return var2;
   }

   static BufferedImage screen() {
      BufferedImage var0 = img(64, 64);
      Graphics2D var1 = gfx(var0);
      var1.setPaint(new GradientPaint(0.0F, 0.0F, c(2372168), 0.0F, 64.0F, c(922658)));
      var1.fillRect(0, 0, 64, 64);
      var1.setColor(ca(7336959, 16));

      for (byte var2 = 0; var2 < 64; var2 += 3) {
         var1.fillRect(0, var2, 64, 1);
      }

      var1.setColor(ca(16777215, 36));
      var1.fill(new java.awt.geom.RoundRectangle2D.Double(6.0, 4.0, 30.0, 10.0, 8.0, 8.0));
      var1.dispose();
      return var0;
   }

   static BufferedImage beeWing() {
      BufferedImage var0 = img(64, 64);
      Graphics2D var1 = gfx(var0);
      Ellipse2D var2 = el(32.0, 32.0, 60.0, 60.0);
      var1.setPaint(new RadialGradientPaint(24.0F, 22.0F, 40.0F, new float[]{0.0F, 1.0F}, new Color[]{ca(16777215, 200), ca(12577023, 120)}));
      var1.fill(var2);
      var1.setStroke(new BasicStroke(2.0F));
      var1.setColor(ca(10475263, 200));
      var1.draw(var2);
      var1.setColor(ca(16777215, 140));
      var1.drawLine(32, 4, 32, 60);
      var1.drawLine(10, 20, 54, 44);
      var1.dispose();
      return var0;
   }

   static BufferedImage batWing() {
      BufferedImage var0 = img(128, 64);
      Graphics2D var1 = gfx(var0);
      java.awt.geom.Path2D.Double var2 = new java.awt.geom.Path2D.Double();
      var2.moveTo(0.0, 6.0);
      var2.curveTo(40.0, 0.0, 90.0, 0.0, 126.0, 8.0);
      var2.quadTo(112.0, 26.0, 116.0, 50.0);
      var2.quadTo(96.0, 38.0, 82.0, 54.0);
      var2.quadTo(66.0, 38.0, 48.0, 58.0);
      var2.quadTo(30.0, 40.0, 0.0, 44.0);
      var2.closePath();
      var1.setPaint(new GradientPaint(0.0F, 0.0F, c(9072568), 0.0F, 64.0F, c(4863344)));
      var1.fill(var2);
      var1.setStroke(new BasicStroke(2.5F, 1, 1));
      var1.setColor(c(3811928));
      var1.drawLine(0, 8, 116, 48);
      var1.drawLine(0, 8, 82, 52);
      var1.drawLine(0, 8, 48, 56);
      var1.setColor(ca(14731519, 90));
      var1.drawLine(2, 6, 124, 9);
      var1.dispose();
      return var0;
   }

   static BufferedImage dragonWing() {
      BufferedImage var0 = img(128, 96);
      Graphics2D var1 = gfx(var0);
      java.awt.geom.Path2D.Double var2 = new java.awt.geom.Path2D.Double();
      var2.moveTo(0.0, 4.0);
      var2.curveTo(50.0, -4.0, 100.0, 6.0, 126.0, 20.0);
      var2.quadTo(108.0, 44.0, 118.0, 80.0);
      var2.quadTo(92.0, 64.0, 76.0, 86.0);
      var2.quadTo(58.0, 62.0, 34.0, 90.0);
      var2.quadTo(18.0, 58.0, 0.0, 56.0);
      var2.closePath();
      var1.setPaint(new GradientPaint(0.0F, 0.0F, c(16763368), 0.0F, 96.0F, c(12159733)));
      var1.fill(var2);
      var1.setStroke(new BasicStroke(3.5F, 1, 1));
      var1.setColor(c(8281040));
      var1.drawLine(0, 5, 118, 78);
      var1.drawLine(0, 5, 76, 84);
      var1.drawLine(0, 5, 34, 88);
      var1.setColor(c(9268192));
      var1.draw(new java.awt.geom.QuadCurve2D.Double(0.0, 4.0, 60.0, -2.0, 126.0, 20.0));
      var1.dispose();
      return var0;
   }

   static BufferedImage bubble() {
      short var0 = 128;
      byte var1 = 64;
      BufferedImage var2 = img(var0, var1);
      Graphics2D var3 = gfx(var2);
      var3.setPaint(
         new LinearGradientPaint(
            0.0F, 0.0F, 0.0F, var1, new float[]{0.0F, 0.3F, 0.7F, 1.0F}, new Color[]{ca(15268607, 90), ca(13628159, 26), ca(12577023, 26), ca(10476799, 80)}
         )
      );
      var3.fillRect(0, 0, var0, var1);
      var3.setStroke(new BasicStroke(2.2F, 1, 1));
      var3.setColor(ca(16777215, 210));
      var3.draw(new java.awt.geom.QuadCurve2D.Double(sx(var0, 1.25), sy(var1, 0.35), sx(var0, 1.05), sy(var1, 0.85), sx(var0, 0.6), sy(var1, 0.95)));
      var3.fill(el(sx(var0, 1.3), sy(var1, 0.22), 3.0, 3.0));
      soft(var3, sx(var0, -0.6), sy(var1, -0.5), 30.0, 12.0, 16767477, 110);
      var3.dispose();
      return var2;
   }

   static BufferedImage mane() {
      BufferedImage var0 = img(64, 64);
      Graphics2D var1 = gfx(var0);
      int[] var2 = new int[]{16751560, 16763274, 16773280, 11071680, 10277631, 13150463};

      for (int var3 = 0; var3 < 6; var3++) {
         var1.setColor(c(var2[var3]));
         var1.fillRect(0, var3 * 64 / 6, 64, 11);
      }

      var1.setColor(ca(16777215, 60));

      for (byte var4 = 0; var4 < 64; var4 += 5) {
         var1.drawLine(var4, 0, var4 + 8, 64);
      }

      var1.dispose();
      return var0;
   }

   static BufferedImage horn() {
      BufferedImage var0 = img(32, 64);
      Graphics2D var1 = gfx(var0);
      var1.setPaint(new GradientPaint(0.0F, 0.0F, c(16775112), 0.0F, 64.0F, c(15907146)));
      var1.fillRect(0, 0, 32, 64);
      var1.setStroke(new BasicStroke(4.0F));
      var1.setColor(ca(13208090, 150));

      for (byte var2 = -64; var2 < 64; var2 += 12) {
         var1.drawLine(0, var2 + 32, 32, var2 + 20);
      }

      var1.dispose();
      return var0;
   }

   static BufferedImage fin(int var0) {
      BufferedImage var1 = img(64, 64);
      Graphics2D var2 = gfx(var1);
      java.awt.geom.Path2D.Double var3 = new java.awt.geom.Path2D.Double();
      var3.moveTo(2.0, 26.0);
      var3.curveTo(20.0, 10.0, 40.0, 0.0, 62.0, 4.0);
      var3.quadTo(50.0, 20.0, 54.0, 32.0);
      var3.quadTo(50.0, 44.0, 62.0, 60.0);
      var3.curveTo(40.0, 64.0, 20.0, 54.0, 2.0, 38.0);
      var3.closePath();
      var2.setPaint(new GradientPaint(0.0F, 0.0F, ca(light(var0, 0.15F), 245), 64.0F, 0.0F, ca(light(var0, 0.55F), 200)));
      var2.fill(var3);
      var2.setClip(var3);
      var2.setStroke(new BasicStroke(2.0F));
      var2.setColor(ca(dark(var0, 0.12F), 150));

      for (byte var4 = 4; var4 < 64; var4 += 9) {
         var2.drawLine(0, 32, 64, var4);
      }

      var2.setClip(null);
      var2.dispose();
      return var1;
   }

   static BufferedImage berry() {
      byte var0 = 64;
      byte var1 = 32;
      BufferedImage var2 = img(var0, var1);
      Graphics2D var3 = gfx(var2);
      var3.setPaint(new GradientPaint(0.0F, 0.0F, c(16738938), 0.0F, var1, c(14163514)));
      var3.fillRect(0, 0, var0, var1);
      var3.setColor(c(16769674));

      for (byte var4 = 4; var4 < var1; var4 += 6) {
         for (int var5 = var4 / 6 % 2 * 4; var5 < var0; var5 += 8) {
            var3.fill(el(var5, var4, 2.2, 3.0));
         }
      }

      var3.dispose();
      return var2;
   }

   static BufferedImage mushCap() {
      short var0 = 256;
      short var1 = 128;
      BufferedImage var2 = img(var0, var1);
      Graphics2D var3 = gfx(var2);
      var3.setPaint(
         new LinearGradientPaint(0.0F, 0.0F, 0.0F, var1, new float[]{0.0F, 0.5F, 0.52F, 1.0F}, new Color[]{c(16739166), c(14692410), c(16773596), c(15257512)})
      );
      var3.fillRect(0, 0, var0, var1);
      Random var4 = new Random(11L);

      for (int var5 = 0; var5 < 18; var5++) {
         double var6 = var4.nextDouble() * var0;
         double var8 = 6.0 + var4.nextDouble() * 46.0;
         double var10 = 10.0 + var4.nextDouble() * 12.0;

         for (int var12 = -1; var12 <= 1; var12++) {
            var3.setColor(c(16775408));
            var3.fill(el(var6 + var12 * var0, var8, var10 * 1.3, var10 * (0.6 + var8 / 90.0)));
            var3.setColor(ca(15259856, 200));
            var3.fill(el(var6 + var12 * var0 + 2.0, var8 + 2.0, var10 * 0.6, var10 * 0.3));
         }
      }

      var3.setColor(ca(13215354, 120));

      for (byte var13 = 0; var13 < var0; var13 += 6) {
         var3.drawLine(var13, 70, var13, 128);
      }

      var3.dispose();
      return var2;
   }

   static BufferedImage jelly() {
      short var0 = 128;
      byte var1 = 64;
      BufferedImage var2 = img(var0, var1);
      Graphics2D var3 = gfx(var2);
      var3.setPaint(new LinearGradientPaint(0.0F, 0.0F, 0.0F, var1, new float[]{0.0F, 0.5F, 1.0F}, new Color[]{c(16766708), c(16096480), c(12094719)}));
      var3.fillRect(0, 0, var0, var1);
      var3.setColor(ca(16777215, 90));

      for (byte var4 = 0; var4 < var0; var4 += 16) {
         var3.fill(el(var4 + 8, 26.0, 6.0, 22.0));
      }

      soft(var3, sx(var0, 0.4), sy(var1, 0.8), 30.0, 10.0, 16777215, 200);
      var3.dispose();
      return var2;
   }

   static BufferedImage brick(int var0) {
      BufferedImage var1 = img(64, 64);
      Graphics2D var2 = gfx(var1);
      var2.setPaint(new GradientPaint(0.0F, 0.0F, c(light(var0, 0.18F)), 0.0F, 64.0F, c(dark(var0, 0.12F))));
      var2.fillRect(0, 0, 64, 64);
      var2.setColor(ca(16777215, 70));
      var2.fillRect(0, 0, 64, 4);
      var2.setColor(ca(0, 40));
      var2.fillRect(0, 60, 64, 4);
      var2.dispose();
      return var1;
   }

   static BufferedImage fur(String var0, boolean var1) {
      int[] var2 = FUR.getOrDefault(var0, new int[]{13421772, 16777215, 8947848});
      int var3 = var2[0];
      int var4 = var2[1];
      int var5 = var2[2];
      short var6 = 256;
      short var7 = 128;
      BufferedImage var8 = img(var6, var7);
      Graphics2D var9 = gfx(var8);
      baseGrad(var9, var6, var7, var3);
      long var10 = var0.hashCode() * 31L + (var1 ? 7 : 0);
      boolean var12 = var0.equals("drake")
         || var0.equals("slime")
         || var0.equals("cloud")
         || var0.equals("ghost")
         || var0.equals("frog")
         || var0.equals("axolotl")
         || var0.equals("mush")
         || var0.equals("fish")
         || var0.equals("turtle");
      if (!var12) {
         furStrokes(var9, var6, var7, var3, var10, 1600, 26);
      }

      switch (var0) {
         case "cat":
            if (var1) {
               patch(var9, var6, var7, 0.0, -0.36, 0.44, 0.24, var4, 255);
               var9.setStroke(new BasicStroke(5.0F, 1, 1));
               var9.setColor(ca(var5, 230));

               for (int var35 = -1; var35 <= 1; var35++) {
                  double var42 = sx(var6, var35 * 0.16);
                  double var18 = sy(var7, 0.95);
                  double var20 = sy(var7, 0.62 - Math.abs(var35) * 0.06);
                  var9.draw(new java.awt.geom.Line2D.Double(var42, var18, var42, var20));
               }

               for (byte var36 = -1; var36 <= 1; var36 += 2) {
                  for (int var43 = 0; var43 < 2; var43++) {
                     double var46 = var36 * (1.3 + var43 * 0.25);
                     var9.draw(
                        new java.awt.geom.Line2D.Double(
                           sx(var6, var46), sy(var7, 0.1 + var43 * 0.25), sx(var6, var46 + var36 * 0.35), sy(var7, 0.15 + var43 * 0.25)
                        )
                     );
                  }
               }
            } else {
               patch(var9, var6, var7, 0.0, -0.35, 0.55, 0.55, var4, 255);
               tabby(var9, var6, var7, var5);
            }
            break;
         case "shiba":
            if (var1) {
               patch(var9, var6, var7, 0.62, -0.3, 0.42, 0.34, var4, 255);
               patch(var9, var6, var7, -0.62, -0.3, 0.42, 0.34, var4, 255);
               patch(var9, var6, var7, 0.0, -0.5, 0.45, 0.3, var4, 255);
               patch(var9, var6, var7, 0.34, 0.55, 0.1, 0.07, var4, 240);
               patch(var9, var6, var7, -0.34, 0.55, 0.1, 0.07, var4, 240);
            } else {
               patch(var9, var6, var7, 0.0, -0.3, 0.6, 0.6, var4, 255);
            }
            break;
         case "fox":
            if (var1) {
               patch(var9, var6, var7, 0.5, -0.4, 0.55, 0.32, var4, 255);
               patch(var9, var6, var7, -0.5, -0.4, 0.55, 0.32, var4, 255);
               patch(var9, var6, var7, 0.0, -0.55, 0.35, 0.3, var4, 255);
            } else {
               patch(var9, var6, var7, 0.0, -0.1, 0.5, 0.65, var4, 255);
            }
            break;
         case "redpanda":
            if (var1) {
               patch(var9, var6, var7, 0.0, -0.45, 0.6, 0.3, var4, 255);
               patch(var9, var6, var7, 0.45, 0.5, 0.16, 0.1, var4, 250);
               patch(var9, var6, var7, -0.45, 0.5, 0.16, 0.1, var4, 250);
               var9.setStroke(new BasicStroke(5.0F, 1, 1));
               var9.setColor(ca(10107416, 110));

               for (byte var34 = -1; var34 <= 1; var34 += 2) {
                  var9.draw(
                     new java.awt.geom.QuadCurve2D.Double(
                        sx(var6, var34 * 0.45), sy(var7, -0.05), sx(var6, var34 * 0.56), sy(var7, -0.35), sx(var6, var34 * 0.75), sy(var7, -0.55)
                     )
                  );
               }
            } else {
               patch(var9, var6, var7, 0.0, -0.5, 0.7, 0.55, var5, 240);
            }
            break;
         case "panda":
            if (var1) {
               for (byte var33 = -1; var33 <= 1; var33 += 2) {
                  AffineTransform var41 = var9.getTransform();
                  double var45 = sx(var6, var33 * 0.38);
                  double var48 = sy(var7, 0.03);
                  var9.translate(var45, var48);
                  var9.rotate(var33 * 0.45);
                  soft(var9, 0.0, 0.0, 44.0, 44.0, var5, 255);
                  var9.setColor(c(var5));
                  var9.fill(el(0.0, 0.0, 32.0, 34.0));
                  var9.setTransform(var41);
               }

               patch(var9, var6, var7, 0.0, -0.45, 0.4, 0.2, 16777215, 200);
            } else {
               var9.setPaint(new GradientPaint(0.0F, 0.0F, c(var5), 0.0F, var7, c(light(var5, 0.05F))));
               var9.fill(new java.awt.geom.Rectangle2D.Double(0.0, sy(var7, 0.75), var6, sy(var7, 0.1) - sy(var7, 0.75)));
               furStrokes(var9, var6, var7, var5, var10 + 1L, 300, 30);
            }
            break;
         case "penguin":
            if (var1) {
               var9.setColor(c(var4));
               Area var32 = new Area(el(sx(var6, 0.3), sy(var7, 0.0), 60.0, 58.0));
               var32.add(new Area(el(sx(var6, -0.3), sy(var7, 0.0), 60.0, 58.0)));
               var32.add(new Area(el(sx(var6, 0.0), sy(var7, -0.35), 70.0, 50.0)));
               var9.fill(var32);
            } else {
               patch(var9, var6, var7, 0.0, -0.1, 0.75, 0.85, var4, 255);
            }
            break;
         case "axolotl":
            patch(var9, var6, var7, 0.0, var1 ? -0.5 : -0.4, 0.6, 0.4, var4, 220);
            spots(var9, var6, var7, var5, var10, 26, 3, 6, 160);
            break;
         case "frog":
            patch(var9, var6, var7, 0.0, var1 ? -0.6 : -0.4, 0.75, 0.45, var4, 250);
            spots(var9, var6, var7, var5, var10, var1 ? 8 : 16, 4, 10, 170);
            break;
         case "chick":
            patch(var9, var6, var7, 0.0, -0.4, 0.6, 0.55, var4, 200);
            break;
         case "hamster":
            if (var1) {
               patch(var9, var6, var7, 0.62, -0.35, 0.45, 0.35, var4, 255);
               patch(var9, var6, var7, -0.62, -0.35, 0.45, 0.35, var4, 255);
               patch(var9, var6, var7, 0.0, -0.55, 0.4, 0.3, var4, 255);
               patch(var9, var6, var7, 0.0, 0.55, 0.12, 0.35, var5, 120);
            } else {
               patch(var9, var6, var7, 0.0, -0.3, 0.7, 0.65, var4, 255);
            }
            break;
         case "owl":
            if (var1) {
               for (byte var30 = -1; var30 <= 1; var30 += 2) {
                  patch(var9, var6, var7, var30 * 0.36, 0.02, 0.36, 0.34, var4, 255);
               }

               var9.setStroke(new BasicStroke(2.0F));
               var9.setColor(ca(var5, 120));

               for (byte var31 = -1; var31 <= 1; var31 += 2) {
                  var9.draw(el(sx(var6, var31 * 0.36), sy(var7, 0.02), 50.0, 50.0));
               }
            } else {
               patch(var9, var6, var7, 0.0, -0.3, 0.6, 0.65, var4, 255);
               chevrons(var9, sx(var6, 0.0), sy(var7, -0.3), var5);
            }
            break;
         case "bee":
            if (!var1) {
               var9.setColor(c(var5));

               for (int var29 = 0; var29 < 3; var29++) {
                  double var40 = var7 * (0.3 + var29 * 0.22);
                  var9.fill(new java.awt.geom.Rectangle2D.Double(0.0, var40, var6, var7 * 0.1));
               }

               furStrokes(var9, var6, var7, var5, var10 + 2L, 400, 30);
            } else {
               patch(var9, var6, var7, 0.0, -0.45, 0.5, 0.3, var4, 180);
            }
            break;
         case "dragon":
            if (var1) {
               patch(var9, var6, var7, 0.0, -0.5, 0.45, 0.3, var4, 230);
            } else {
               patch(var9, var6, var7, 0.0, -0.3, 0.55, 0.65, var4, 255);
               var9.setStroke(new BasicStroke(2.0F));
               var9.setColor(ca(14727280, 170));

               for (int var28 = 0; var28 < 6; var28++) {
                  double var39 = sy(var7, -0.8 + var28 * 0.22);
                  var9.draw(new java.awt.geom.QuadCurve2D.Double(sx(var6, 0.45), var39, sx(var6, 0.0), var39 + 5.0, sx(var6, -0.45), var39));
               }

               spots(var9, var6, var7, var5, var10, 20, 4, 8, 120);
            }
            break;
         case "unicorn":
            spots(var9, var6, var7, 16766706, var10, 20, 3, 5, 160);
            if (var1) {
               patch(var9, var6, var7, 0.0, -0.55, 0.4, 0.28, 16769262, 200);
            }
            break;
         case "bear":
            if (var1) {
               patch(var9, var6, var7, 0.0, -0.45, 0.45, 0.3, var4, 255);
            } else {
               patch(var9, var6, var7, 0.0, -0.25, 0.5, 0.55, var4, 255);
               var9.setColor(ca(16743068, 190));
               var9.fill(heart(sx(var6, 0.0), sy(var7, -0.15), 22.0));
            }
            break;
         case "turtle":
            patch(var9, var6, var7, 0.0, -0.5, 0.6, 0.35, var4, 230);
            spots(var9, var6, var7, var5, var10, 14, 4, 8, 140);
            break;
         case "bat":
            if (var1) {
               patch(var9, var6, var7, 0.0, -0.1, 0.85, 0.55, var4, 255);
            }
            break;
         case "fish":
            patch(var9, var6, var7, 0.0, -0.6, 1.2, 0.5, var4, 230);
            scales(var9, var6, var7, 16756848);
            break;
         case "cloud":
         case "ghost":
            var9.setPaint(new LinearGradientPaint(0.0F, 0.0F, 0.0F, var7, new float[]{0.0F, 0.55F, 1.0F}, new Color[]{c(16777215), c(var3), c(var5)}));
            var9.fillRect(0, 0, var6, var7);
            break;
         case "slime":
            spots(var9, var6, var7, light(var3, 0.4F), var10, 10, 3, 6, 120);
            break;
         case "mush":
            patch(var9, var6, var7, 0.0, -0.3, 0.6, 0.5, var4, 150);
            break;
         case "bunny":
            if (var1) {
               patch(var9, var6, var7, 0.0, -0.45, 0.5, 0.32, var4, 255);
               patch(var9, var6, var7, 0.0, 0.7, 0.14, 0.35, 16777215, 150);
            } else {
               patch(var9, var6, var7, 0.0, -0.35, 0.6, 0.55, var4, 255);
            }
            break;
         case "wolf":
            if (var1) {
               patch(var9, var6, var7, 0.5, -0.35, 0.5, 0.35, var4, 255);
               patch(var9, var6, var7, -0.5, -0.35, 0.5, 0.35, var4, 255);
               patch(var9, var6, var7, 0.0, 0.6, 0.35, 0.3, var5, 150);

               for (byte var27 = -1; var27 <= 1; var27 += 2) {
                  patch(var9, var6, var7, var27 * 0.36, 0.34, 0.1, 0.06, var4, 230);
               }
            } else {
               patch(var9, var6, var7, 0.0, -0.45, 0.8, 0.55, var4, 255);
               patch(var9, var6, var7, Math.PI, 0.75, 1.6, 0.55, var5, 170);
            }
            break;
         case "horse":
            if (var1) {
               patch(var9, var6, var7, 0.0, 0.1, 0.1, 0.6, var5, 235);
            } else {
               patch(var9, var6, var7, 0.0, -0.6, 0.6, 0.35, var4, 140);
            }
            break;
         case "fawn":
            patch(var9, var6, var7, 0.0, -0.45, 0.6, 0.5, var4, 255);
            if (!var1) {
               Random var26 = new Random(var10);

               for (int var38 = 0; var38 < 46; var38++) {
                  double var44 = var26.nextDouble() * Math.PI * 2.0;
                  double var47 = 0.1 + var26.nextDouble() * 0.75;
                  var9.setColor(ca(var5, 235));
                  double var21 = 5.0 + var26.nextDouble() * 4.0;
                  var9.fill(el(var44 / (Math.PI * 2) * var6, sy(var7, var47), var21 * 1.3, var21));
               }
            }
            break;
         case "deer":
            patch(var9, var6, var7, 0.0, -0.45, 0.6, 0.5, var4, 255);
            if (!var1) {
               patch(var9, var6, var7, Math.PI, -0.1, 0.45, 0.35, 16777215, 220);
            }
            break;
         case "lion":
            patch(var9, var6, var7, 0.0, -0.45, 0.6, 0.5, var4, 255);
            break;
         case "tiger":
            if (!var1) {
               patch(var9, var6, var7, 0.0, -0.6, 0.7, 0.4, var4, 255);
            } else {
               for (byte var24 = -1; var24 <= 1; var24 += 2) {
                  patch(var9, var6, var7, var24 * 0.5, -0.35, 0.45, 0.35, var4, 255);
               }

               for (byte var25 = -1; var25 <= 1; var25 += 2) {
                  patch(var9, var6, var7, var25 * 0.33, 0.36, 0.1, 0.06, var4, 240);
               }
            }

            stripes(var9, var6, var7, var5, var10, var1 ? 14 : 22, var1);
            break;
         case "retr":
            patch(var9, var6, var7, 0.0, -0.4, 0.6, 0.5, var4, 220);
            furStrokes(var9, var6, var7, light(var3, 0.25F), var10 + 5L, 500, 60);
            break;
         case "afox":
            patch(var9, var6, var7, 0.0, -0.4, 0.6, 0.5, 16777215, 255);
            furStrokes(var9, var6, var7, var5, var10 + 5L, 400, 45);
            break;
         case "eagle":
            var9.setStroke(new BasicStroke(2.0F, 1, 1));

            for (int var23 = 0; var23 < 7; var23++) {
               for (byte var37 = 0; var37 < var6 + 16; var37 += 16) {
                  double var17 = var37 + var23 % 2 * 8;
                  double var19 = 10 + var23 * 16;
                  var9.setColor(ca(light(var3, 0.3F), 140));
                  var9.draw(new java.awt.geom.Arc2D.Double(var17 - 8.0, var19 - 8.0, 16.0, 16.0, 180.0, 180.0, 0));
               }
            }
            break;
         case "eagleh":
            patch(var9, var6, var7, 0.0, -0.5, 0.5, 0.3, 16777215, 200);
            break;
         case "drake":
            if (!var1) {
               patch(var9, var6, var7, 0.0, -0.55, 0.7, 0.45, var4, 255);
               var9.setStroke(new BasicStroke(2.0F));
               var9.setColor(ca(dark(var4, 0.25F), 170));

               for (int var15 = 0; var15 < 6; var15++) {
                  double var16 = sy(var7, -0.95 + var15 * 0.13);
                  var9.draw(new java.awt.geom.Line2D.Double(sx(var6, 0.6), var16, sx(var6, -0.6), var16));
               }
            } else {
               patch(var9, var6, var7, 0.0, -0.55, 0.45, 0.3, var4, 230);
            }

            scales(var9, var6, var7, light(var3, 0.25F));
            break;
         default:
            if (!var1) {
               patch(var9, var6, var7, 0.0, -0.35, 0.55, 0.55, var4, 255);
            }
      }

      var9.dispose();
      return var8;
   }

   private static void stripes(Graphics2D var0, int var1, int var2, int var3, long var4, int var6, boolean var7) {
      Random var8 = new Random(var4);
      var0.setColor(ca(var3, 235));

      for (int var9 = 0; var9 < var6; var9++) {
         double var10 = -Math.PI + (var9 + var8.nextDouble() * 0.5) * Math.PI * 2.0 / var6;
         if (!var7 || !(Math.abs(var10) < 0.55)) {
            double var12 = sx(var1, var10);
            double var14 = var7 ? sy(var2, 1.3) : sy(var2, 1.5);
            double var16 = var7 ? sy(var2, 0.35) : sy(var2, -0.35 + var8.nextDouble() * 0.25);
            double var18 = 3.0 + var8.nextDouble() * 4.0;
            java.awt.geom.Path2D.Double var20 = new java.awt.geom.Path2D.Double();
            var20.moveTo(var12 - var18, var14);
            var20.quadTo(var12 + 6.0, (var14 + var16) / 2.0, var12, var16);
            var20.quadTo(var12 + 6.0 + var18, (var14 + var16) / 2.0, var12 + var18, var14);
            var20.closePath();

            for (int var21 = -1; var21 <= 1; var21++) {
               AffineTransform var22 = var0.getTransform();
               var0.translate(var21 * var1, 0);
               var0.fill(var20);
               var0.setTransform(var22);
            }
         }
      }
   }

   static BufferedImage featherWing(int var0, int var1) {
      BufferedImage var2 = img(128, 64);
      Graphics2D var3 = gfx(var2);
      java.awt.geom.Path2D.Double var4 = new java.awt.geom.Path2D.Double();
      var4.moveTo(0.0, 2.0);
      var4.curveTo(40.0, -2.0, 90.0, 0.0, 126.0, 10.0);
      var4.lineTo(120.0, 26.0);

      for (int var5 = 0; var5 < 6; var5++) {
         double var6 = 118 - var5 * 14;
         double var8 = 34 + var5 * 4;
         var4.lineTo(var6, var8 + 12.0);
         var4.lineTo(var6 - 7.0, var8 + 4.0);
      }

      var4.quadTo(30.0, 50.0, 0.0, 44.0);
      var4.closePath();
      var3.setPaint(new GradientPaint(0.0F, 0.0F, c(light(var0, 0.15F)), 128.0F, 0.0F, c(var1)));
      var3.fill(var4);
      var3.setClip(var4);
      var3.setStroke(new BasicStroke(1.6F));

      for (int var10 = 0; var10 < 16; var10++) {
         double var11 = 10 + var10 * 8;
         var3.setColor(ca(dark(var0, 0.35F), 150));
         var3.draw(new java.awt.geom.Line2D.Double(var11, 14.0, var11 - 6.0, 62.0));
      }

      var3.setColor(ca(light(var0, 0.2F), 200));
      var3.fill(new Double(-10.0, -6.0, 90.0, 26.0));
      var3.setClip(null);
      var3.setStroke(new BasicStroke(2.0F));
      var3.setColor(c(dark(var0, 0.4F)));
      var3.draw(new java.awt.geom.QuadCurve2D.Double(0.0, 2.0, 60.0, -2.0, 126.0, 10.0));
      var3.dispose();
      return var2;
   }

   static BufferedImage drakeWing(int var0, int var1) {
      BufferedImage var2 = img(128, 96);
      Graphics2D var3 = gfx(var2);
      java.awt.geom.Path2D.Double var4 = new java.awt.geom.Path2D.Double();
      var4.moveTo(0.0, 4.0);
      var4.curveTo(50.0, -4.0, 100.0, 6.0, 126.0, 20.0);
      var4.quadTo(108.0, 44.0, 118.0, 80.0);
      var4.quadTo(92.0, 64.0, 76.0, 86.0);
      var4.quadTo(58.0, 62.0, 34.0, 90.0);
      var4.quadTo(18.0, 58.0, 0.0, 56.0);
      var4.closePath();
      var3.setPaint(new GradientPaint(0.0F, 0.0F, ca(light(var1, 0.25F), 240), 0.0F, 96.0F, ca(var1, 230)));
      var3.fill(var4);
      var3.setStroke(new BasicStroke(4.0F, 1, 1));
      var3.setColor(c(var0));
      var3.drawLine(0, 5, 118, 78);
      var3.drawLine(0, 5, 76, 84);
      var3.drawLine(0, 5, 34, 88);
      var3.draw(new java.awt.geom.QuadCurve2D.Double(0.0, 4.0, 60.0, -2.0, 126.0, 20.0));
      var3.fill(new Double(121.0, 14.0, 8.0, 8.0));
      var3.dispose();
      return var2;
   }

   static BufferedImage tailFan(int var0, int var1) {
      BufferedImage var2 = img(64, 64);
      Graphics2D var3 = gfx(var2);

      for (int var4 = -3; var4 <= 3; var4++) {
         AffineTransform var5 = var3.getTransform();
         var3.translate(32, 2);
         var3.rotate(var4 * 0.16);
         var3.setPaint(new GradientPaint(0.0F, 0.0F, c(var0), 0.0F, 60.0F, c(var1)));
         var3.fill(new java.awt.geom.RoundRectangle2D.Double(-6.0, 0.0, 12.0, 60.0, 10.0, 10.0));
         var3.setColor(ca(dark(var0, 0.3F), 160));
         var3.drawLine(0, 4, 0, 56);
         var3.setTransform(var5);
      }

      var3.dispose();
      return var2;
   }

   private static void tabby(Graphics2D var0, int var1, int var2, int var3) {
      var0.setStroke(new BasicStroke(9.0F, 1, 1));
      var0.setColor(ca(var3, 200));

      for (int var4 = 0; var4 < 5; var4++) {
         double var5 = Math.PI + (var4 - 2) * 0.42;
         double var7 = sx(var1, var5);
         var0.draw(new java.awt.geom.QuadCurve2D.Double(var7, sy(var2, 1.4), var7 + 8.0, sy(var2, 0.8), var7 - 2.0, sy(var2, 0.15)));
         var0.draw(new java.awt.geom.QuadCurve2D.Double(var7 + var1, sy(var2, 1.4), var7 + var1 + 8.0, sy(var2, 0.8), var7 + var1 - 2.0, sy(var2, 0.15)));
         var0.draw(new java.awt.geom.QuadCurve2D.Double(var7 - var1, sy(var2, 1.4), var7 - var1 + 8.0, sy(var2, 0.8), var7 - var1 - 2.0, sy(var2, 0.15)));
      }
   }

   private static void spots(Graphics2D var0, int var1, int var2, int var3, long var4, int var6, int var7, int var8, int var9) {
      Random var10 = new Random(var4);

      for (int var11 = 0; var11 < var6; var11++) {
         double var12 = var10.nextDouble() * var1;
         double var14 = 12.0 + var10.nextDouble() * (var2 * 0.6);
         double var16 = var7 + var10.nextDouble() * (var8 - var7);
         if (!(Math.abs(var12 - var1 * 0.25) < var1 * 0.12) || !(var14 > var2 * 0.3)) {
            var0.setColor(ca(var3, var9));
            var0.fill(el(var12, var14, var16 * 1.4, var16));
         }
      }
   }

   private static void chevrons(Graphics2D var0, double var1, double var3, int var5) {
      var0.setStroke(new BasicStroke(2.6F, 1, 1));
      var0.setColor(ca(var5, 150));

      for (int var6 = 0; var6 < 4; var6++) {
         for (int var7 = -2; var7 <= 2; var7++) {
            double var8 = var1 + var7 * 12 + var6 % 2 * 6;
            double var10 = var3 - 18.0 + var6 * 11;
            var0.draw(new java.awt.geom.QuadCurve2D.Double(var8 - 4.0, var10, var8, var10 + 4.0, var8 + 4.0, var10));
         }
      }
   }

   private static void scales(Graphics2D var0, int var1, int var2, int var3) {
      var0.setStroke(new BasicStroke(2.0F, 1, 1));
      var0.setColor(ca(var3, 150));

      for (int var4 = 0; var4 < 6; var4++) {
         for (byte var5 = 0; var5 < var1 + 12; var5 += 12) {
            double var6 = var5 + var4 % 2 * 6;
            double var8 = 20 + var4 * 12;
            var0.draw(new java.awt.geom.Arc2D.Double(var6 - 6.0, var8 - 6.0, 12.0, 12.0, 180.0, 180.0, 0));
         }
      }
   }

   static PetTex.St style(String var0) {
      int var1 = 0;
      int var2 = var0.lastIndexOf(46);
      if (var2 > 0) {
         try {
            var1 = Integer.parseInt(var0.substring(var2 + 1));
         } catch (NumberFormatException var4) {
         }

         var0 = var0.substring(0, var2);
      }

      PetTex.St var3 = style0(var0);
      if (var1 == 1) {
         var3.eye *= 0.86F;
         var3.gap *= 1.03F;
      } else if (var1 >= 2) {
         var3.eye *= 0.72F;
         var3.gap *= 1.08F;
      }

      return var3;
   }

   private static PetTex.St style0(String var0) {
      PetTex.St var1 = new PetTex.St();
      switch (var0) {
         case "cat":
            var1.iris = 4173418;
            var1.mouth = 1;
            var1.whiskers = true;
            break;
         case "dog":
            var1.iris = 6961694;
            var1.mouth = 1;
            break;
         case "beak":
            var1.iris = 3811904;
            var1.mouth = 2;
            break;
         case "owl":
            var1.iris = 14719514;
            var1.mouth = 2;
            var1.eye = 1.15F;
            var1.gap = 1.05F;
            break;
         case "gem":
            var1.iris = 10509552;
            var1.lashes = true;
            break;
         case "amber":
            var1.iris = 14716958;
            break;
         case "blue":
            var1.iris = 4097766;
            break;
         case "rim":
            var1.iris = 4864624;
            var1.rim = true;
            break;
         case "led":
            var1.iris = 7336959;
            var1.led = true;
            var1.line = 7336959;
            var1.blush = 16740296;
            break;
         case "blob":
            var1.iris = 3814224;
            var1.eye = 0.92F;
            var1.gap = 0.92F;
            break;
         case "pink":
            var1.iris = 14700682;
            var1.lashes = true;
            break;
         case "wolf":
            var1.iris = 14262814;
            var1.mouth = 1;
            var1.eye = 0.92F;
            var1.gap = 1.05F;
            break;
         case "horse":
            var1.iris = 4860956;
            var1.lashes = true;
            var1.eye = 0.9F;
            var1.gap = 1.3F;
            break;
         case "deer":
            var1.iris = 3021844;
            var1.lashes = true;
            var1.eye = 1.0F;
            var1.gap = 1.22F;
            break;
         case "lion":
            var1.iris = 13666842;
            var1.mouth = 1;
            var1.eye = 0.9F;
            break;
         case "tiger":
            var1.iris = 15249950;
            var1.mouth = 1;
            var1.eye = 0.9F;
            break;
         case "retr":
            var1.iris = 5911578;
            var1.mouth = 1;
            var1.eye = 0.95F;
            break;
         case "afox":
            var1.iris = 13144106;
            var1.mouth = 1;
            var1.eye = 0.95F;
            break;
         case "eagle":
            var1.iris = 15777824;
            var1.mouth = 2;
            var1.eye = 0.85F;
            var1.gap = 1.1F;
            break;
         case "drake":
            var1.iris = 15769632;
            var1.eye = 0.9F;
      }

      return var1;
   }

   static BufferedImage eyes(PetTex.St var0, String var1, boolean var2) {
      BufferedImage var3 = img(256, 192);
      Graphics2D var4 = gfx(var3);
      double var5 = 48.0 * var0.eye;
      double var7 = 60.0 * var0.eye;
      double var9 = 50.0 * var0.gap;

      for (byte var11 = -1; var11 <= 1; var11 += 2) {
         double var12 = 128.0 + var11 * var9;
         double var14 = 84.0;
         boolean var16 = true;
         if (var2) {
            closedEye(var4, var0, var1, var12, var14, var5, var7, var11);
         } else {
            switch (var1) {
               case "love":
                  heartEye(var4, var0, var12, var14 + 2.0, var5 * 1.2);
                  break;
               case "excited":
                  starEye(var4, var0, var12, var14 + 2.0, var5 * 0.78);
                  break;
               case "dizzy":
                  spiralEye(var4, var0, var12, var14 + 2.0, var5 * 0.55);
                  break;
               case "sleepy":
                  java.awt.geom.Rectangle2D.Double var25 = new java.awt.geom.Rectangle2D.Double(var12 - var5, var14 - var7 * 0.02, var5 * 2.0, var7);
                  var4.setClip(var25);
                  openEye(var4, var0, var12, var14 + 4.0, var5, var7 * 0.95, false, 0);
                  var4.setClip(null);
                  lid(var4, var0, var12 - var5 * 0.56, var14 - var7 * 0.02, var12 + var5 * 0.56, var14 - var7 * 0.02);
                  break;
               case "angry":
                  double var24 = var12 - var11 * var5 * 0.6;
                  double var21 = var12 + var11 * var5 * 0.6;
                  java.awt.geom.Path2D.Double var23 = new java.awt.geom.Path2D.Double();
                  var23.moveTo(var24, var14 + var7 * 0.02);
                  var23.lineTo(var21, var14 - var7 * 0.34);
                  var23.lineTo(var21, var14 + var7);
                  var23.lineTo(var24, var14 + var7);
                  var23.closePath();
                  var4.setClip(var23);
                  openEye(var4, var0, var12, var14 + 4.0, var5 * 0.95, var7 * 0.9, false, 0);
                  var4.setClip(null);
                  lid(var4, var0, var24, var14 + var7 * 0.02, var21, var14 - var7 * 0.34);
                  break;
               case "sad":
                  openEye(var4, var0, var12, var14 + 3.0, var5 * 1.04, var7 * 1.02, true, 1);
                  break;
               case "curious":
                  float var19 = var11 < 0 ? 1.1F : 0.9F;
                  openEye(var4, var0, var12, var14 + (var11 < 0 ? -2 : 3), var5 * var19, var7 * var19, false, 0);
                  break;
               default:
                  openEye(var4, var0, var12, var14, var5, var7, true, 2);
            }
         }
      }

      var4.dispose();
      return var3;
   }

   private static void lid(Graphics2D var0, PetTex.St var1, double var2, double var4, double var6, double var8) {
      var0.setStroke(new BasicStroke(6.5F, 1, 1));
      var0.setColor(var1.led ? c(var1.iris) : c(2364440));
      var0.draw(new java.awt.geom.Line2D.Double(var2, var4, var6, var8));
   }

   private static void openEye(Graphics2D var0, PetTex.St var1, double var2, double var4, double var6, double var8, boolean var10, int var11) {
      if (var1.led) {
         soft(var0, var2, var4, var6 * 1.6, var8 * 1.5, var1.iris, 110);
         var0.setColor(c(light(var1.iris, 0.3F)));
         var0.fill(new java.awt.geom.RoundRectangle2D.Double(var2 - var6 * 0.4, var4 - var8 * 0.45, var6 * 0.8, var8 * 0.9, var6 * 0.7, var6 * 0.7));
         var0.setColor(ca(16777215, 200));
         var0.fill(el(var2 - var6 * 0.12, var4 - var8 * 0.2, var6 * 0.2, var8 * 0.2));
      } else {
         if (var1.rim) {
            var0.setColor(c(16777215));
            var0.fill(el(var2, var4, var6 + 9.0, var8 + 9.0));
         }

         Ellipse2D var12 = el(var2, var4, var6, var8);
         var0.setPaint(
            new LinearGradientPaint(
               (float)var2,
               (float)(var4 - var8 / 2.0),
               (float)var2,
               (float)(var4 + var8 / 2.0),
               new float[]{0.0F, 0.45F, 1.0F},
               new Color[]{c(1773600), c(mix(1773600, var1.iris, 0.35F)), c(light(var1.iris, 0.25F))}
            )
         );
         var0.fill(var12);
         var0.setColor(ca(787984, 170));
         var0.fill(el(var2, var4 - var8 * 0.05, var6 * 0.5, var8 * 0.55));
         var0.setClip(var12);
         soft(var0, var2, var4 + var8 * 0.42, var6 * 0.9, var8 * 0.45, light(var1.iris, 0.55F), 160);
         var0.setClip(null);
         var0.setStroke(new BasicStroke(3.0F));
         var0.setColor(c(1773600));
         var0.draw(var12);
         var0.setColor(c(16777215));
         var0.fill(el(var2 - var6 * 0.17, var4 - var8 * 0.2, var6 * 0.46, var8 * 0.38));
         var0.fill(el(var2 + var6 * 0.2, var4 + var8 * 0.2, var6 * 0.18, var6 * 0.18));
         if (var11 == 1) {
            var0.setClip(var12);
            var0.setColor(ca(10477823, 150));
            var0.fill(el(var2, var4 + var8 * 0.5, var6 * 1.1, var8 * 0.45));
            var0.setClip(null);
            var0.setColor(ca(16777215, 230));
            var0.fill(el(var2 + var6 * 0.02, var4 + var8 * 0.02, var6 * 0.14, var6 * 0.14));
            var0.setStroke(new BasicStroke(2.5F, 1, 1));
            var0.setColor(ca(16777215, 200));
            var0.draw(new java.awt.geom.Arc2D.Double(var2 - var6 * 0.36, var4 - var8 * 0.2, var6 * 0.72, var8 * 0.7, 200.0, 60.0, 0));
         } else if (var11 == 2) {
            var0.setColor(c(16777215));
            var0.fill(starShape(var2 + var6 * 0.18, var4 - var8 * 0.28, var6 * 0.13, var6 * 0.04, 4, 0.0));
         }

         if (var1.lashes) {
            var0.setStroke(new BasicStroke(3.5F, 1, 1));
            var0.setColor(c(1773600));
            double var13 = var2 < 128.0 ? -1.0 : 1.0;

            for (int var15 = 0; var15 < 2; var15++) {
               double var16 = Math.toRadians(var13 < 0.0 ? 150 + var15 * 18 : 30 - var15 * 18);
               double var18 = var2 + Math.cos(var16) * var6 * 0.5;
               double var20 = var4 - Math.sin(var16) * var8 * 0.5;
               var0.draw(new java.awt.geom.Line2D.Double(var18, var20, var18 + Math.cos(var16) * 9.0, var20 - Math.sin(var16) * 9.0));
            }
         }
      }
   }

   private static void closedEye(Graphics2D var0, PetTex.St var1, String var2, double var3, double var5, double var7, double var9, int var11) {
      Color var12 = var1.led ? c(light(var1.iris, 0.3F)) : c(2364440);
      if (var1.led) {
         soft(var0, var3, var5 + 6.0, var7 * 1.4, var9 * 0.8, var1.iris, 90);
      }

      if (var1.rim) {
         var0.setColor(ca(16777215, 230));
         var0.fill(el(var3, var5 + 8.0, var7 * 0.9, var9 * 0.45));
      }

      var0.setStroke(new BasicStroke(7.0F, 1, 1));
      var0.setColor(var12);
      switch (var2) {
         case "happy":
         case "love":
            var0.draw(new java.awt.geom.Arc2D.Double(var3 - var7 * 0.42, var5 - var9 * 0.05, var7 * 0.84, var9 * 0.6, 20.0, 140.0, 0));
            break;
         case "excited":
            java.awt.geom.Path2D.Double var19 = new java.awt.geom.Path2D.Double();
            double var16 = -var11;
            var19.moveTo(var3 - var16 * var7 * 0.3, var5 - var9 * 0.18);
            var19.lineTo(var3 + var16 * var7 * 0.3, var5 + var9 * 0.04);
            var19.lineTo(var3 - var16 * var7 * 0.3, var5 + var9 * 0.26);
            var0.draw(var19);
            break;
         case "dizzy":
            var0.setStroke(new BasicStroke(6.0F, 1, 1));
            double var18 = var7 * 0.28;
            var0.draw(new java.awt.geom.Line2D.Double(var3 - var18, var5 - var18 + 6.0, var3 + var18, var5 + var18 + 6.0));
            var0.draw(new java.awt.geom.Line2D.Double(var3 - var18, var5 + var18 + 6.0, var3 + var18, var5 - var18 + 6.0));
            break;
         case "angry":
            var0.draw(new java.awt.geom.Line2D.Double(var3 - var11 * var7 * 0.4, var5 + var9 * 0.06, var3 + var11 * var7 * 0.4, var5 - var9 * 0.08));
            break;
         default:
            var0.draw(new java.awt.geom.Arc2D.Double(var3 - var7 * 0.42, var5 - var9 * 0.2, var7 * 0.84, var9 * 0.5, 200.0, 140.0, 0));
            var0.setStroke(new BasicStroke(3.5F, 1, 1));
            double var15 = var3 + var11 * var7 * 0.4;
            var0.draw(new java.awt.geom.Line2D.Double(var15, var5 + var9 * 0.02, var15 + var11 * 7, var5 + var9 * 0.08));
      }
   }

   private static void heartEye(Graphics2D var0, PetTex.St var1, double var2, double var4, double var6) {
      if (var1.led) {
         soft(var0, var2, var4, var6 * 1.3, var6 * 1.2, 16740296, 110);
      }

      if (var1.rim) {
         var0.setColor(c(16777215));
         var0.fill(el(var2, var4, var6 * 0.95, var6 * 0.9));
      }

      Shape var8 = heart(var2, var4, var6);
      var0.setPaint(new GradientPaint(0.0F, (float)(var4 - var6 * 0.4), c(16744366), 0.0F, (float)(var4 + var6 * 0.4), c(15212636)));
      var0.fill(var8);
      var0.setStroke(new BasicStroke(3.0F, 1, 1));
      var0.setColor(c(11014206));
      if (!var1.led) {
         var0.draw(var8);
      }

      var0.setColor(c(16777215));
      var0.fill(el(var2 - var6 * 0.2, var4 - var6 * 0.17, var6 * 0.2, var6 * 0.14));
      var0.fill(el(var2 + var6 * 0.12, var4 + var6 * 0.08, var6 * 0.08, var6 * 0.08));
   }

   private static void starEye(Graphics2D var0, PetTex.St var1, double var2, double var4, double var6) {
      if (var1.led) {
         soft(var0, var2, var4, var6 * 2.6, var6 * 2.6, 16769658, 110);
      }

      if (var1.rim) {
         var0.setColor(c(16777215));
         var0.fill(el(var2, var4, var6 * 2.1, var6 * 2.1));
      }

      Shape var8 = starShape(var2, var4 + 2.0, var6, var6 * 0.5, 5, 0.0);
      var0.setPaint(new GradientPaint(0.0F, (float)(var4 - var6), c(16774048), 0.0F, (float)(var4 + var6), c(16755228)));
      var0.fill(var8);
      var0.setStroke(new BasicStroke(4.0F, 1, 1));
      var0.setColor(c(14250778));
      var0.draw(var8);
      var0.setColor(c(16777215));
      var0.fill(el(var2 - var6 * 0.25, var4 - var6 * 0.2, var6 * 0.32, var6 * 0.24));
   }

   private static void spiralEye(Graphics2D var0, PetTex.St var1, double var2, double var4, double var6) {
      if (!var1.led) {
         var0.setColor(ca(16777215, 240));
         var0.fill(el(var2, var4, var6 * 2.2, var6 * 2.2));
         var0.setStroke(new BasicStroke(2.5F));
         var0.setColor(c(2364440));
         var0.draw(el(var2, var4, var6 * 2.2, var6 * 2.2));
      } else {
         soft(var0, var2, var4, var6 * 2.8, var6 * 2.8, var1.iris, 100);
      }

      java.awt.geom.Path2D.Double var8 = new java.awt.geom.Path2D.Double();

      for (int var9 = 0; var9 <= 80; var9++) {
         double var10 = var9 / 80.0;
         double var12 = var10 * Math.PI * 5.2;
         double var14 = var6 * 0.95 * var10;
         double var16 = var2 + Math.cos(var12) * var14;
         double var18 = var4 + Math.sin(var12) * var14;
         if (var9 == 0) {
            var8.moveTo(var16, var18);
         } else {
            var8.lineTo(var16, var18);
         }
      }

      var0.setStroke(new BasicStroke(4.5F, 1, 1));
      var0.setColor(var1.led ? c(light(var1.iris, 0.3F)) : c(3810384));
      var0.draw(var8);
   }

   static BufferedImage face(PetTex.St var0, String var1) {
      BufferedImage var2 = img(256, 192);
      Graphics2D var3 = gfx(var2);
      double var4 = 50.0 * var0.gap;
      double var6 = 48.0 * var0.eye;
      double var8 = 60.0 * var0.eye;
      Color var10 = c(var0.line);
      int var11 = var0.blush;
      int var12 = "love".equals(var1) ? 210 : ("angry".equals(var1) ? 150 : ("sad".equals(var1) ? 90 : ("excited".equals(var1) ? 180 : 140)));
      if ("angry".equals(var1)) {
         var11 = 16734826;
      }

      for (byte var13 = -1; var13 <= 1; var13 += 2) {
         double var14 = 128.0 + var13 * (var4 + var6 * 0.62);
         double var16 = 84.0 + var8 * 0.62;
         soft(var3, var14, var16, 50.0, 30.0, var11, var12);
         if ("love".equals(var1) || "excited".equals(var1) || "happy".equals(var1)) {
            var3.setStroke(new BasicStroke(2.6F, 1, 1));
            var3.setColor(ca(16777215, 150));

            for (int var18 = -1; var18 <= 1; var18++) {
               var3.draw(new java.awt.geom.Line2D.Double(var14 + var18 * 8 - 3.0, var16 + 5.0, var14 + var18 * 8 + 3.0, var16 - 5.0));
            }
         }
      }

      if (var0.whiskers) {
         var3.setStroke(new BasicStroke(2.2F, 1, 1));
         var3.setColor(ca(5913130, 150));

         for (byte var27 = -1; var27 <= 1; var27 += 2) {
            for (int var29 = 0; var29 < 2; var29++) {
               double var15 = 128.0 + var27 * (var4 + var6 * 0.95);
               double var17 = 84.0 + var8 * 0.66 + var29 * 9;
               var3.draw(
                  new java.awt.geom.QuadCurve2D.Double(var15, var17, var15 + var27 * 12, var17 - 2.0 + var29 * 3, var15 + var27 * 24, var17 + var29 * 6 - 2.0)
               );
            }
         }
      }

      var3.setStroke(new BasicStroke(6.0F, 1, 1));
      var3.setColor(var0.led ? c(var0.iris) : ca(var0.line, 235));
      double var28 = 84.0 - var8 * 0.62;
      switch (var1) {
         case "angry":
            for (byte var34 = -1; var34 <= 1; var34 += 2) {
               double var37 = 128.0 + var34 * var4;
               var3.draw(new java.awt.geom.Line2D.Double(var37 - var34 * var6 * 0.55, var28 + 12.0, var37 + var34 * var6 * 0.4, var28 - 6.0));
            }
            break;
         case "sad":
            for (byte var33 = -1; var33 <= 1; var33 += 2) {
               double var36 = 128.0 + var33 * var4;
               var3.draw(
                  new java.awt.geom.QuadCurve2D.Double(var36 - var33 * var6 * 0.45, var28 - 6.0, var36, var28 - 2.0, var36 + var33 * var6 * 0.4, var28 + 8.0)
               );
            }
            break;
         case "curious":
            double var32 = 128.0 - var4;
            var3.draw(new java.awt.geom.QuadCurve2D.Double(var32 - var6 * 0.45, var28 - 4.0, var32, var28 - 20.0, var32 + var6 * 0.4, var28 - 6.0));
            double var19 = 128.0 + var4;
            var3.draw(new java.awt.geom.Line2D.Double(var19 - var6 * 0.35, var28 + 6.0, var19 + var6 * 0.35, var28 + 4.0));
         case "sleepy":
      }

      double var30 = 84.0 + var8 * 0.66;
      double var35 = 128.0;
      var3.setStroke(new BasicStroke(4.5F, 1, 1));
      var3.setColor(var10);
      if (var0.mouth != 2) {
         switch (var1) {
            case "happy":
               if (var0.mouth == 1) {
                  omega(var3, var35, var30, 12.0, var10);
                  openMouth(var3, var0, var35, var30 + 7.0, 14.0, 11.0);
               } else {
                  openMouth(var3, var0, var35, var30, 24.0, 16.0);
               }
               break;
            case "love":
               if (var0.mouth == 1) {
                  omega(var3, var35, var30, 12.0, var10);
               } else {
                  var3.draw(new java.awt.geom.Arc2D.Double(var35 - 11.0, var30 - 10.0, 22.0, 16.0, 200.0, 140.0, 0));
               }

               var3.setColor(ca(16732043, 230));
               var3.fill(heart(var35 + 30.0, var30 - 2.0, 16.0));
               break;
            case "sleepy":
               var3.setColor(var10);
               var3.setStroke(new BasicStroke(3.5F));
               var3.draw(el(var35, var30 + 2.0, 9.0, 8.0));
               break;
            case "sad":
               var3.draw(new java.awt.geom.QuadCurve2D.Double(var35 - 13.0, var30 + 7.0, var35, var30 - 6.0, var35 + 13.0, var30 + 7.0));
               break;
            case "excited":
               if (var0.mouth == 1) {
                  omega(var3, var35, var30 - 2.0, 12.0, var10);
               }

               openMouth(var3, var0, var35, var30 + (var0.mouth == 1 ? 5 : 0), var0.mouth == 1 ? 20.0 : 30.0, var0.mouth == 1 ? 16.0 : 22.0);
               break;
            case "angry":
               java.awt.geom.Path2D.Double var39 = new java.awt.geom.Path2D.Double();
               var39.moveTo(var35 - 12.0, var30 + 6.0);
               var39.quadTo(var35 - 6.0, var30 - 1.0, var35, var30 + 2.0);
               var39.quadTo(var35 + 6.0, var30 - 1.0, var35 + 12.0, var30 + 6.0);
               var3.draw(var39);
               var3.setColor(c(16777215));
               java.awt.geom.Path2D.Double var45 = new java.awt.geom.Path2D.Double();
               var45.moveTo(var35 + 4.0, var30 + 1.5);
               var45.lineTo(var35 + 9.0, var30 + 3.0);
               var45.lineTo(var35 + 7.0, var30 + 9.0);
               var45.closePath();
               if (!var0.led) {
                  var3.fill(var45);
               }
               break;
            case "curious":
               var3.setColor(var0.led ? var10 : c(6955056));
               var3.fill(el(var35 + 8.0, var30 + 3.0, 11.0, 12.0));
               var3.setColor(var10);
               var3.setStroke(new BasicStroke(3.5F));
               var3.draw(el(var35 + 8.0, var30 + 3.0, 11.0, 12.0));
               break;
            case "dizzy":
               java.awt.geom.Path2D.Double var21 = new java.awt.geom.Path2D.Double();

               for (int var22 = 0; var22 <= 30; var22++) {
                  double var23 = var35 - 20.0 + 40 * var22 / 30.0;
                  double var25 = var30 + 3.0 + Math.sin(var22 / 30.0 * Math.PI * 3.0) * 4.5;
                  if (var22 == 0) {
                     var21.moveTo(var23, var25);
                  } else {
                     var21.lineTo(var23, var25);
                  }
               }

               var3.draw(var21);
               break;
            default:
               var3.draw(new java.awt.geom.Arc2D.Double(var35 - 11.0, var30 - 10.0, 22.0, 16.0, 200.0, 140.0, 0));
         }
      } else if ("love".equals(var1)) {
         var3.setColor(ca(16732043, 230));
         var3.fill(heart(var35 + 40.0, var30 - 6.0, 16.0));
      }

      switch (var1) {
         case "sad":
            for (byte var43 = -1; var43 <= 1; var43 += 2) {
               double var46 = 128.0 + var43 * var4 + var43 * var6 * 0.18;
               double var24 = 84.0 + var8 * 0.38;
               var3.setPaint(new GradientPaint(0.0F, (float)var24, ca(10478591, 220), 0.0F, (float)(var24 + 70.0), ca(7325951, 60)));
               var3.fill(new java.awt.geom.RoundRectangle2D.Double(var46 - 5.0, var24, 10.0, 70.0, 10.0, 10.0));
            }

            double var44 = 128.0 + var4 + var6 * 0.55;
            double var50 = 84.0 + var8 * 0.8;
            java.awt.geom.Path2D.Double var54 = new java.awt.geom.Path2D.Double();
            var54.moveTo(var44, var50 - 12.0);
            var54.curveTo(var44 + 9.0, var50, var44 + 7.0, var50 + 9.0, var44, var50 + 9.0);
            var54.curveTo(var44 - 7.0, var50 + 9.0, var44 - 9.0, var50, var44, var50 - 12.0);
            var3.setColor(ca(9427967, 235));
            var3.fill(var54);
            var3.setColor(ca(16777215, 230));
            var3.fill(el(var44 - 2.5, var50 + 1.0, 3.5, 5.0));
            break;
         case "sleepy":
            double var42 = 128.0 + var4 * 0.62;
            double var49 = 84.0 + var8 * 0.72;
            double var53 = 15.0;
            var3.setColor(ca(12578815, 150));
            var3.fill(el(var42, var49, var53 * 2.0, var53 * 2.0));
            var3.setStroke(new BasicStroke(2.2F));
            var3.setColor(ca(8376309, 220));
            var3.draw(el(var42, var49, var53 * 2.0, var53 * 2.0));
            var3.setColor(ca(16777215, 240));
            var3.fill(el(var42 - 5.0, var49 - 6.0, 8.0, 6.0));
            break;
         case "angry":
            double var41 = 128.0 + var4 + var6 * 0.62;
            double var48 = 26.0;
            var3.setStroke(new BasicStroke(5.0F, 1, 1));
            var3.setColor(c(16726858));

            for (int var52 = 0; var52 < 4; var52++) {
               AffineTransform var26 = var3.getTransform();
               var3.translate(var41, var48);
               var3.rotate((Math.PI / 2) * var52);
               var3.draw(new java.awt.geom.QuadCurve2D.Double(4.0, -12.0, 4.0, -4.0, 12.0, -4.0));
               var3.setTransform(var26);
            }
            break;
         case "dizzy":
            var3.setStroke(new BasicStroke(2.4F));
            var3.setColor(ca(10476031, 200));
            double var40 = 128.0 - var4 - var6 * 0.7;
            double var47 = 84.0 - var8 * 0.55;
            java.awt.geom.Path2D.Double var51 = new java.awt.geom.Path2D.Double();
            var51.moveTo(var40, var47 - 8.0);
            var51.curveTo(var40 + 6.0, var47, var40 + 5.0, var47 + 6.0, var40, var47 + 6.0);
            var51.curveTo(var40 - 5.0, var47 + 6.0, var40 - 6.0, var47, var40, var47 - 8.0);
            var3.setColor(ca(10477567, 220));
            var3.fill(var51);
      }

      var3.dispose();
      return var2;
   }

   private static void omega(Graphics2D var0, double var1, double var3, double var5, Color var7) {
      var0.setColor(var7);
      var0.setStroke(new BasicStroke(4.0F, 1, 1));
      var0.draw(new java.awt.geom.Arc2D.Double(var1 - var5, var3 - var5 * 0.9, var5, var5, 200.0, 160.0, 0));
      var0.draw(new java.awt.geom.Arc2D.Double(var1, var3 - var5 * 0.9, var5, var5, 180.0, 160.0, 0));
   }

   private static void openMouth(Graphics2D var0, PetTex.St var1, double var2, double var4, double var6, double var8) {
      java.awt.geom.Path2D.Double var10 = new java.awt.geom.Path2D.Double();
      var10.moveTo(var2 - var6 / 2.0, var4 - var8 * 0.3);
      var10.quadTo(var2, var4 - var8 * 0.1, var2 + var6 / 2.0, var4 - var8 * 0.3);
      var10.curveTo(var2 + var6 * 0.45, var4 + var8 * 0.75, var2 - var6 * 0.45, var4 + var8 * 0.75, var2 - var6 / 2.0, var4 - var8 * 0.3);
      var10.closePath();
      if (var1.led) {
         var0.setColor(c(light(var1.iris, 0.3F)));
         var0.fill(var10);
      } else {
         var0.setColor(c(8003636));
         var0.fill(var10);
         Shape var11 = var0.getClip();
         var0.setClip(var10);
         var0.setColor(c(16744342));
         var0.fill(el(var2, var4 + var8 * 0.55, var6 * 0.62, var8 * 0.7));
         var0.setClip(var11);
         var0.setStroke(new BasicStroke(3.2F, 1, 1));
         var0.setColor(c(var1.line));
         var0.draw(var10);
      }
   }

   static {
      FUR.put("cat", new int[]{16229457, 16774374, 14251051});
      FUR.put("shiba", new int[]{15636296, 16775148, 12084010});
      FUR.put("bunny", new int[]{15850694, 16776181, 16758728});
      FUR.put("fox", new int[]{16221754, 16774892, 4860450});
      FUR.put("redpanda", new int[]{13852718, 16774890, 5909018});
      FUR.put("panda", new int[]{16514039, 16777215, 2763315});
      FUR.put("penguin", new int[]{3424874, 16777215, 16756810});
      FUR.put("axolotl", new int[]{16758990, 16770028, 15890335});
      FUR.put("frog", new int[]{8835424, 15858376, 5940548});
      FUR.put("chick", new int[]{16770154, 16774840, 16757820});
      FUR.put("hamster", new int[]{15773796, 16775406, 13205566});
      FUR.put("owl", new int[]{10975311, 15850938, 7227952});
      FUR.put("bee", new int[]{16766023, 16771466, 3811882});
      FUR.put("dragon", new int[]{11440887, 16771504, 8611808});
      FUR.put("unicorn", new int[]{16776191, 16771830, 15913727});
      FUR.put("bear", new int[]{12157006, 15782568, 9067060});
      FUR.put("turtle", new int[]{10476418, 15267520, 7320922});
      FUR.put("bat", new int[]{7232660, 13351142, 4864618});
      FUR.put("fish", new int[]{16747578, 16769208, 16777215});
      FUR.put("mush", new int[]{16773596, 16777215, 15257512});
      FUR.put("cloud", new int[]{16777215, 16777215, 14082815});
      FUR.put("ghost", new int[]{16514303, 16777215, 13227775});
      FUR.put("slime", new int[]{9236648, 13172692, 4173411});
      FUR.put("pdark", new int[]{3026488, 3026488, 3026488});
      FUR.put("wolf", new int[]{9343902, 15329250, 5133150});
      FUR.put("horse", new int[]{10707002, 13208158, 16777215});
      FUR.put("fawn", new int[]{11892554, 16049872, 16775406});
      FUR.put("deer", new int[]{11104586, 15852495, 7227952});
      FUR.put("lion", new int[]{14723429, 16246470, 10117680});
      FUR.put("tiger", new int[]{15763756, 16774890, 2365980});
      FUR.put("retr", new int[]{14920798, 16178086, 13208126});
      FUR.put("afox", new int[]{15988218, 16777215, 12964066});
      FUR.put("eagle", new int[]{5978917, 8016438, 3810326});
      FUR.put("eagleh", new int[]{16184300, 16777215, 14472908});
      FUR.put("drake", new int[]{8150230, 15915424, 5126814});
   }

   static final class St {
      int iris = 4860958;
      int mouth;
      boolean lashes;
      boolean rim;
      boolean led;
      boolean whiskers;
      int line = 3875616;
      float eye = 1.0F;
      float gap = 1.0F;
      int blush = 16743334;
   }
}
