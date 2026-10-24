package dev.lego.visual;

import dev.lego.ui.Gx;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D.Double;
import java.awt.geom.Point2D.Float;
import java.awt.image.BufferedImage;

public final class AmbientTex {
   public static final int S = 128;

   private AmbientTex() {
   }

   public static int[] paint(String var0) {
      BufferedImage var1 = new BufferedImage(128, 128, 2);
      Graphics2D var2 = var1.createGraphics();
      Gx.hints(var2);
      switch (var0) {
         case "snow":
            snow(var2);
            break;
         case "petal":
            petal(var2);
            break;
         case "leaf":
            leaf(var2);
            break;
         case "ash":
            soft(var2, new Color(90, 90, 96, 230), 0.55F);
            break;
         case "glitter":
            star(var2, new Color(255, 255, 255), 0.95F);
            break;
         case "stardust":
            star(var2, new Color(190, 220, 255), 0.7F);
            break;
         case "glow":
            soft(var2, Color.WHITE, 1.0F);
            break;
         case "core":
            core(var2);
            break;
         case "lantern":
            lantern(var2);
            break;
         case "bubble":
            bubble(var2);
            break;
         case "butterfly0":
            butterfly(var2, 1.0F);
            break;
         case "butterfly1":
            butterfly(var2, 0.35F);
            break;
         case "bird0":
            bird(var2, 1.0F);
            break;
         case "bird1":
            bird(var2, -0.6F);
            break;
         case "bat0":
            bat(var2, 1.0F);
            break;
         case "bat1":
            bat(var2, 0.3F);
            break;
         case "dragonfly":
            dragonfly(var2);
            break;
         case "seed":
            seed(var2);
            break;
         case "note":
            note(var2);
            break;
         case "heart":
            heart(var2);
            break;
         case "shard":
            shard(var2);
            break;
         case "confetti":
            var2.setColor(Color.WHITE);
            var2.fillRoundRect(40, 24, 48, 80, 10, 10);
            break;
         case "leafswirl":
            leaf(var2);
            break;
         default:
            soft(var2, Color.WHITE, 1.0F);
      }

      var2.dispose();
      int[] var3 = var1.getRGB(0, 0, 128, 128, null, 0, 128);

      for (int var5 = 0; var5 < var3.length; var5++) {
         if (var3[var5] >>> 24 == 0) {
            var3[var5] = 16777215 & avgNeighbour(var3, var5);
         }
      }

      return var3;
   }

   private static int avgNeighbour(int[] var0, int var1) {
      int var2 = var1 % 128;
      int var3 = var1 / 128;

      for (int var4 = 1; var4 < 4; var4++) {
         for (int var5 = -var4; var5 <= var4; var5++) {
            for (int var6 = -var4; var6 <= var4; var6++) {
               int var7 = var2 + var6;
               int var8 = var3 + var5;
               if (var7 >= 0 && var8 >= 0 && var7 < 128 && var8 < 128) {
                  int var9 = var0[var8 * 128 + var7];
                  if (var9 >>> 24 > 0) {
                     return var9;
                  }
               }
            }
         }
      }

      return 16777215;
   }

   private static void soft(Graphics2D var0, Color var1, float var2) {
      var0.setPaint(
         new RadialGradientPaint(
            new Float(64.0F, 64.0F),
            62.0F,
            new float[]{0.0F, 0.35F, 1.0F},
            new Color[]{
               new Color(var1.getRed(), var1.getGreen(), var1.getBlue(), (int)(var1.getAlpha() * var2)),
               new Color(var1.getRed(), var1.getGreen(), var1.getBlue(), (int)(var1.getAlpha() * var2 * 0.45F)),
               new Color(var1.getRed(), var1.getGreen(), var1.getBlue(), 0)
            },
            CycleMethod.NO_CYCLE
         )
      );
      var0.fillRect(0, 0, 128, 128);
   }

   private static void core(Graphics2D var0) {
      var0.setPaint(
         new RadialGradientPaint(
            new Float(64.0F, 64.0F),
            62.0F,
            new float[]{0.0F, 0.12F, 0.3F, 1.0F},
            new Color[]{new Color(255, 255, 255, 255), new Color(255, 255, 255, 230), new Color(255, 255, 255, 70), new Color(255, 255, 255, 0)},
            CycleMethod.NO_CYCLE
         )
      );
      var0.fillRect(0, 0, 128, 128);
   }

   private static void snow(Graphics2D var0) {
      soft(var0, new Color(210, 230, 255, 120), 0.8F);
      var0.translate(64, 64);
      var0.setStroke(new BasicStroke(5.2F, 1, 1));

      for (int var1 = 0; var1 < 6; var1++) {
         AffineTransform var2 = var0.getTransform();
         var0.rotate((Math.PI / 3) * var1);
         var0.setColor(new Color(255, 255, 255, 250));
         var0.drawLine(0, 0, 0, -46);
         var0.setStroke(new BasicStroke(3.6F, 1, 1));
         var0.drawLine(0, -18, -11, -29);
         var0.drawLine(0, -18, 11, -29);
         var0.drawLine(0, -32, -8, -40);
         var0.drawLine(0, -32, 8, -40);
         var0.setStroke(new BasicStroke(5.2F, 1, 1));
         var0.setTransform(var2);
      }

      var0.setColor(Color.WHITE);
      var0.fill(new Double(-7.0, -7.0, 14.0, 14.0));
   }

   private static void petal(Graphics2D var0) {
      java.awt.geom.Path2D.Double var1 = new java.awt.geom.Path2D.Double();
      var1.moveTo(64.0, 112.0);
      var1.curveTo(18.0, 88.0, 22.0, 30.0, 52.0, 16.0);
      var1.quadTo(64.0, 30.0, 76.0, 16.0);
      var1.curveTo(106.0, 30.0, 110.0, 88.0, 64.0, 112.0);
      var1.closePath();
      var0.setPaint(new GradientPaint(64.0F, 110.0F, new Color(255, 150, 190), 64.0F, 16.0F, new Color(255, 225, 236)));
      var0.fill(var1);
      var0.setColor(new Color(255, 120, 170, 120));
      var0.setStroke(new BasicStroke(2.2F));
      var0.draw(new java.awt.geom.QuadCurve2D.Double(64.0, 104.0, 60.0, 70.0, 64.0, 34.0));
      var0.setColor(new Color(255, 255, 255, 90));
      var0.fill(new Double(46.0, 36.0, 16.0, 30.0));
   }

   private static void leaf(Graphics2D var0) {
      java.awt.geom.Path2D.Double var1 = new java.awt.geom.Path2D.Double();
      var1.moveTo(64.0, 10.0);
      var1.curveTo(104.0, 34.0, 110.0, 80.0, 64.0, 116.0);
      var1.curveTo(18.0, 80.0, 24.0, 34.0, 64.0, 10.0);
      var1.closePath();
      var0.setPaint(new GradientPaint(30.0F, 20.0F, new Color(255, 196, 64), 100.0F, 110.0F, new Color(214, 64, 28)));
      var0.fill(var1);
      var0.setColor(new Color(120, 40, 10, 170));
      var0.setStroke(new BasicStroke(3.0F, 1, 1));
      var0.drawLine(64, 18, 64, 122);

      for (int var2 = 0; var2 < 4; var2++) {
         int var3 = 36 + var2 * 18;
         var0.drawLine(64, var3 + 8, 44 + var2 * 2, var3 - 4);
         var0.drawLine(64, var3 + 8, 84 - var2 * 2, var3 - 4);
      }
   }

   private static void star(Graphics2D var0, Color var1, float var2) {
      soft(var0, new Color(var1.getRed(), var1.getGreen(), var1.getBlue(), 160), var2);
      java.awt.geom.Path2D.Double var3 = new java.awt.geom.Path2D.Double();

      for (int var4 = 0; var4 < 8; var4++) {
         double var5 = (Math.PI / 4) * var4 - (Math.PI / 2);
         double var7 = var4 % 2 == 0 ? 60.0 : 9.0;
         double var9 = 64.0 + Math.cos(var5) * var7;
         double var11 = 64.0 + Math.sin(var5) * var7;
         if (var4 == 0) {
            var3.moveTo(var9, var11);
         } else {
            var3.lineTo(var9, var11);
         }
      }

      var3.closePath();
      var0.setColor(new Color(255, 255, 255, 250));
      var0.fill(var3);
   }

   private static void lantern(Graphics2D var0) {
      soft(var0, new Color(255, 150, 60, 200), 0.9F);
      java.awt.geom.RoundRectangle2D.Double var1 = new java.awt.geom.RoundRectangle2D.Double(38.0, 26.0, 52.0, 70.0, 18.0, 18.0);
      var0.setPaint(new GradientPaint(64.0F, 26.0F, new Color(255, 214, 140), 64.0F, 96.0F, new Color(255, 120, 40)));
      var0.fill(var1);
      var0.setColor(new Color(255, 250, 220, 230));
      var0.fill(new Double(52.0, 54.0, 24.0, 34.0));
      var0.setColor(new Color(170, 60, 20, 170));
      var0.setStroke(new BasicStroke(2.4F));

      for (int var2 = 1; var2 < 4; var2++) {
         var0.drawLine(38 + var2 * 13, 30, 38 + var2 * 13, 92);
      }

      var0.setColor(new Color(120, 60, 30));
      var0.fillRect(44, 94, 40, 5);
   }

   private static void bubble(Graphics2D var0) {
      var0.setPaint(
         new RadialGradientPaint(
            new Float(64.0F, 64.0F),
            58.0F,
            new float[]{0.0F, 0.78F, 0.92F, 1.0F},
            new Color[]{new Color(255, 255, 255, 12), new Color(200, 230, 255, 40), new Color(255, 190, 255, 170), new Color(160, 255, 230, 0)},
            CycleMethod.NO_CYCLE
         )
      );
      var0.fill(new Double(6.0, 6.0, 116.0, 116.0));
      var0.setColor(new Color(255, 255, 255, 200));
      var0.fill(new Double(34.0, 28.0, 22.0, 14.0));
      var0.setColor(new Color(255, 255, 255, 120));
      var0.fill(new Double(80.0, 84.0, 10.0, 7.0));
   }

   private static void bird(Graphics2D var0, float var1) {
      var0.setColor(new Color(40, 44, 56));
      var0.setStroke(new BasicStroke(9.0F, 1, 1));
      java.awt.geom.Path2D.Double var2 = new java.awt.geom.Path2D.Double();
      var2.moveTo(10.0, 64.0F - 30.0F * var1);
      var2.quadTo(38.0, 64.0F - 34.0F * var1, 64.0, 66.0);
      var2.quadTo(90.0, 64.0F - 34.0F * var1, 118.0, 64.0F - 30.0F * var1);
      var0.draw(var2);
      var0.fill(new Double(56.0, 58.0, 16.0, 14.0));
   }

   private static void bat(Graphics2D var0, float var1) {
      var0.translate(64, 64);
      var0.setColor(new Color(28, 20, 36));

      for (byte var2 = -1; var2 <= 1; var2 += 2) {
         AffineTransform var3 = var0.getTransform();
         var0.scale(var2, var1);
         java.awt.geom.Path2D.Double var4 = new java.awt.geom.Path2D.Double();
         var4.moveTo(4.0, -6.0);
         var4.lineTo(30.0, -30.0);
         var4.lineTo(58.0, -18.0);
         var4.lineTo(50.0, 4.0);
         var4.lineTo(40.0, -2.0);
         var4.lineTo(32.0, 10.0);
         var4.lineTo(22.0, 2.0);
         var4.lineTo(12.0, 12.0);
         var4.closePath();
         var0.fill(var4);
         var0.setTransform(var3);
      }

      var0.fill(new Double(-9.0, -12.0, 18.0, 26.0));
      var0.setColor(new Color(255, 70, 70));
      var0.fill(new Double(-5.0, -6.0, 3.0, 3.0));
      var0.fill(new Double(2.0, -6.0, 3.0, 3.0));
   }

   private static void dragonfly(Graphics2D var0) {
      var0.setColor(new Color(170, 230, 255, 150));
      var0.fill(new Double(12.0, 40.0, 50.0, 16.0));
      var0.fill(new Double(66.0, 40.0, 50.0, 16.0));
      var0.fill(new Double(16.0, 60.0, 46.0, 14.0));
      var0.fill(new Double(66.0, 60.0, 46.0, 14.0));
      var0.setPaint(new GradientPaint(64.0F, 30.0F, new Color(40, 220, 190), 64.0F, 120.0F, new Color(30, 90, 200)));
      var0.fill(new java.awt.geom.RoundRectangle2D.Double(58.0, 30.0, 12.0, 90.0, 12.0, 12.0));
      var0.fill(new Double(54.0, 22.0, 20.0, 18.0));
   }

   private static void seed(Graphics2D var0) {
      var0.setColor(new Color(255, 255, 255, 230));
      var0.setStroke(new BasicStroke(2.4F, 1, 1));

      for (int var1 = 0; var1 < 14; var1++) {
         double var2 = (Math.PI * 2) * var1 / 14.0;
         var0.drawLine(64, 52, (int)(64.0 + Math.cos(var2) * 40.0), (int)(52.0 + Math.sin(var2) * 40.0));
      }

      var0.setColor(new Color(230, 230, 220));
      var0.drawLine(64, 52, 64, 118);
      var0.setColor(new Color(150, 120, 80));
      var0.fill(new Double(59.0, 110.0, 10.0, 14.0));
   }

   private static void note(Graphics2D var0) {
      var0.setColor(Color.WHITE);
      var0.fill(new Double(24.0, 82.0, 34.0, 26.0));
      var0.fill(new Double(72.0, 70.0, 34.0, 26.0));
      var0.setStroke(new BasicStroke(8.0F));
      var0.drawLine(54, 94, 54, 22);
      var0.drawLine(102, 82, 102, 14);
      var0.fill(new Polygon(new int[]{50, 106, 106, 50}, new int[]{18, 8, 26, 36}, 4));
   }

   private static void heart(Graphics2D var0) {
      java.awt.geom.Path2D.Double var1 = new java.awt.geom.Path2D.Double();
      var1.moveTo(64.0, 112.0);
      var1.curveTo(10.0, 76.0, 12.0, 22.0, 44.0, 22.0);
      var1.curveTo(56.0, 22.0, 62.0, 32.0, 64.0, 40.0);
      var1.curveTo(66.0, 32.0, 72.0, 22.0, 84.0, 22.0);
      var1.curveTo(116.0, 22.0, 118.0, 76.0, 64.0, 112.0);
      var0.setColor(Color.WHITE);
      var0.fill(var1);
   }

   private static void shard(Graphics2D var0) {
      soft(var0, new Color(200, 220, 255, 120), 0.6F);
      java.awt.geom.Path2D.Double var1 = new java.awt.geom.Path2D.Double();
      var1.moveTo(64.0, 8.0);
      var1.lineTo(92.0, 56.0);
      var1.lineTo(70.0, 120.0);
      var1.lineTo(40.0, 64.0);
      var1.closePath();
      var0.setPaint(new GradientPaint(40.0F, 10.0F, new Color(255, 255, 255), 90.0F, 120.0F, new Color(150, 200, 255)));
      var0.fill(var1);
      var0.setColor(new Color(255, 255, 255, 180));
      var0.drawLine(64, 8, 70, 120);
   }

   private static void butterfly(Graphics2D var0, float var1) {
      var0.translate(64, 64);

      for (byte var2 = -1; var2 <= 1; var2 += 2) {
         AffineTransform var3 = var0.getTransform();
         var0.scale(var2 * var1, 1.0);
         java.awt.geom.Path2D.Double var4 = new java.awt.geom.Path2D.Double();
         var4.moveTo(2.0, -4.0);
         var4.curveTo(20.0, -52.0, 62.0, -48.0, 54.0, -14.0);
         var4.curveTo(48.0, 2.0, 20.0, 6.0, 2.0, 2.0);
         var4.closePath();
         java.awt.geom.Path2D.Double var5 = new java.awt.geom.Path2D.Double();
         var5.moveTo(2.0, 2.0);
         var5.curveTo(30.0, 6.0, 48.0, 30.0, 32.0, 46.0);
         var5.curveTo(18.0, 56.0, 6.0, 30.0, 2.0, 8.0);
         var5.closePath();
         var0.setPaint(new GradientPaint(0.0F, -40.0F, new Color(90, 200, 255), 50.0F, 10.0F, new Color(150, 80, 255)));
         var0.fill(var4);
         var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(120, 90, 255), 30.0F, 48.0F, new Color(255, 120, 220)));
         var0.fill(var5);
         var0.setColor(new Color(20, 16, 40, 200));
         var0.setStroke(new BasicStroke(3.0F));
         var0.draw(var4);
         var0.draw(var5);
         var0.setColor(new Color(255, 255, 255, 220));
         var0.fill(new Double(30.0, -34.0, 9.0, 9.0));
         var0.setTransform(var3);
      }

      var0.setColor(new Color(30, 24, 40));
      var0.fill(new java.awt.geom.RoundRectangle2D.Double(-3.0, -22.0, 6.0, 50.0, 6.0, 6.0));
   }
}
