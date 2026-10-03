package dev.lego.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Path2D.Double;

public final class Art {
   private Art() {
   }

   public static void brick(Graphics2D var0, double var1, double var3, double var5, Color var7) {
      double var8 = var5;
      double var10 = var1 + var5 / 2.0;
      double var12 = var3 + var5 * 0.26;
      double var14 = var3 + var5 * 0.47;
      double var16 = var3 + var5 * 0.86;
      double var18 = var1 + var5 * 0.06;
      double var20 = var1 + var5 * 0.94;
      double var22 = var5 * 0.26;
      Color var24 = mix(var7, Color.WHITE, 0.28);
      Color var25 = mix(var7, Color.BLACK, 0.32);
      Color var26 = mix(var7, Color.BLACK, 0.5);
      Double var27 = new Double();
      var27.moveTo(var18, var14 - var22 * 0.0);
      var27.lineTo(var10, var14 + (var14 - var12));
      var27.lineTo(var10, var16);
      var27.lineTo(var18, var16 - (var14 - var12));
      var27.closePath();
      var0.setPaint(new GradientPaint((float)var18, (float)var14, mix(var7, Color.BLACK, 0.12), (float)var10, (float)var16, var25));
      var0.fill(var27);
      Double var28 = new Double();
      var28.moveTo(var20, var14);
      var28.lineTo(var10, var14 + (var14 - var12));
      var28.lineTo(var10, var16);
      var28.lineTo(var20, var16 - (var14 - var12));
      var28.closePath();
      var0.setPaint(new GradientPaint((float)var10, (float)var14, var25, (float)var20, (float)var16, var26));
      var0.fill(var28);
      Double var29 = new Double();
      var29.moveTo(var10, var12 - (var14 - var12));
      var29.lineTo(var20, var14);
      var29.lineTo(var10, var14 + (var14 - var12));
      var29.lineTo(var18, var14);
      var29.closePath();
      var0.setPaint(new GradientPaint((float)var18, (float)var12, var24, (float)var20, (float)var14, var7));
      var0.fill(var29);
      double var30 = var5 * 0.17;
      double var32 = var5 * 0.085;
      double var34 = var5 * 0.07;
      double[][] var36 = new double[][]{{0.0, -1.0}, {-1.0, 0.0}, {1.0, 0.0}, {0.0, 1.0}};

      for (double[] var40 : var36) {
         double var41 = var10 + var40[0] * var8 * 0.2;
         double var43 = var14 + var40[1] * (var14 - var12) * 0.52 - (var14 - var12) * 0.02;
         var0.setColor(var25);
         var0.fill(new java.awt.geom.Ellipse2D.Double(var41 - var30 / 2.0, var43 - var32 / 2.0 - 0.0, var30, var32 + var34));
         var0.setColor(mix(var7, Color.BLACK, 0.18));
         var0.fill(new java.awt.geom.Rectangle2D.Double(var41 - var30 / 2.0, var43 - var34, var30, var34));
         var0.setPaint(new GradientPaint((float)(var41 - var30 / 2.0), (float)var43, var24, (float)(var41 + var30 / 2.0), (float)var43, var7));
         var0.fill(new java.awt.geom.Ellipse2D.Double(var41 - var30 / 2.0, var43 - var34 - var32 / 2.0, var30, var32));
      }

      var0.setStroke(new BasicStroke((float)Math.max(0.6, var8 * 0.012)));
      var0.setColor(new Color(255, 255, 255, 50));
      var0.draw(var29);
   }

   public static Color mix(Color var0, Color var1, double var2) {
      var2 = Math.max(0.0, Math.min(1.0, var2));
      return new Color(
         (int)(var0.getRed() + (var1.getRed() - var0.getRed()) * var2),
         (int)(var0.getGreen() + (var1.getGreen() - var0.getGreen()) * var2),
         (int)(var0.getBlue() + (var1.getBlue() - var0.getBlue()) * var2),
         (int)(var0.getAlpha() + (var1.getAlpha() - var0.getAlpha()) * var2)
      );
   }

   public static void glow(Graphics2D var0, double var1, double var3, double var5, Color var7) {
      var0.setPaint(
         new RadialGradientPaint(
            (float)var1, (float)var3, (float)var5, new float[]{0.0F, 1.0F}, new Color[]{var7, new Color(var7.getRed(), var7.getGreen(), var7.getBlue(), 0)}
         )
      );
      var0.fill(new java.awt.geom.Ellipse2D.Double(var1 - var5, var3 - var5, 2.0 * var5, 2.0 * var5));
   }

   public static Gx.Img brickImg(int var0, int var1) {
      return Gx.painted("brick" + Integer.toHexString(var1), var0, var0, var2 -> brick(var2, 0.0, 0.0, var0, new Color(var1, true)));
   }
}
