package dev.lego.emote;

import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.RoundRectangle2D.Double;
import java.awt.image.BufferedImage;

public final class Bubbles {
   private Bubbles() {
   }

   public static int[] paint(String var0, int var1) {
      BufferedImage var2 = new BufferedImage(var1, var1, 2);
      Graphics2D var3 = var2.createGraphics();
      Gx.hints(var3);
      var3.scale(var1 / 64.0, var1 / 64.0);
      var3.setColor(new Color(0, 0, 0, 45));
      var3.fill(new Double(5.0, 6.0, 56.0, 46.0, 24.0, 24.0));
      var3.setColor(new Color(255, 255, 255, 245));
      var3.fill(new Double(4.0, 4.0, 56.0, 46.0, 24.0, 24.0));
      java.awt.geom.Path2D.Double var4 = new java.awt.geom.Path2D.Double();
      var4.moveTo(26.0, 48.0);
      var4.lineTo(32.0, 60.0);
      var4.lineTo(38.0, 48.0);
      var4.closePath();
      var3.fill(var4);
      switch (var0) {
         case "heart":
            var3.setPaint(new GradientPaint(20.0F, 12.0F, new Color(255, 110, 140), 44.0F, 42.0F, new Color(220, 20, 60)));
            java.awt.geom.Path2D.Double var8 = new java.awt.geom.Path2D.Double();
            var8.moveTo(32.0, 43.0);
            var8.curveTo(12.0, 30.0, 15.0, 11.0, 26.0, 12.0);
            var8.curveTo(30.0, 12.5, 32.0, 16.0, 32.0, 18.0);
            var8.curveTo(32.0, 16.0, 34.0, 12.5, 38.0, 12.0);
            var8.curveTo(49.0, 11.0, 52.0, 30.0, 32.0, 43.0);
            var3.fill(var8);
            var3.setColor(new Color(255, 255, 255, 150));
            var3.fill(new java.awt.geom.Ellipse2D.Double(21.0, 16.0, 7.0, 5.0));
            break;
         case "gg":
            text(var3, "GG", new Color(30, 215, 96), 24.0F);
            break;
         case "laugh":
            face(var3, true, false);
            break;
         case "cry":
            face(var3, false, true);
            break;
         case "think":
            text(var3, "?", new Color(4172287), 30.0F);
            break;
         case "zen":
            var3.setColor(new Color(10177994));
            var3.setStroke(new BasicStroke(3.0F, 1, 1));
            var3.draw(new java.awt.geom.Ellipse2D.Double(20.0, 13.0, 24.0, 24.0));
            var3.fill(new java.awt.geom.Arc2D.Double(20.0, 13.0, 24.0, 24.0, 90.0, 180.0, 1));
            var3.setColor(Color.WHITE);
            var3.fill(new java.awt.geom.Ellipse2D.Double(29.0, 16.0, 6.0, 6.0));
            var3.setColor(new Color(10177994));
            var3.fill(new java.awt.geom.Ellipse2D.Double(29.0, 28.0, 6.0, 6.0));
            break;
         case "sleep":
            text(var3, "Zzz", new Color(5991423), 18.0F);
            break;
         default:
            var3.setPaint(new GradientPaint(32.0F, 10.0F, new Color(255, 220, 60), 32.0F, 44.0F, new Color(255, 60, 20)));
            java.awt.geom.Path2D.Double var7 = new java.awt.geom.Path2D.Double();
            var7.moveTo(32.0, 44.0);
            var7.curveTo(18.0, 44.0, 16.0, 30.0, 24.0, 22.0);
            var7.curveTo(24.0, 28.0, 28.0, 30.0, 30.0, 28.0);
            var7.curveTo(26.0, 20.0, 30.0, 12.0, 36.0, 8.0);
            var7.curveTo(36.0, 16.0, 48.0, 22.0, 46.0, 34.0);
            var7.curveTo(45.0, 41.0, 40.0, 44.0, 32.0, 44.0);
            var3.fill(var7);
      }

      var3.dispose();
      return var2.getRGB(0, 0, var1, var1, null, 0, var1);
   }

   private static void text(Graphics2D var0, String var1, Color var2, float var3) {
      var0.setColor(var2);
      var0.setFont(Fonts.get(3, var3));
      double var4 = var0.getFontMetrics().getStringBounds(var1, var0).getWidth();
      var0.drawString(var1, (float)(32.0 - var4 / 2.0), 27.0F + var3 * 0.36F);
   }

   private static void face(Graphics2D var0, boolean var1, boolean var2) {
      var0.setPaint(new RadialGradientPaint(28.0F, 20.0F, 22.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(255, 225, 90), new Color(245, 180, 0)}));
      var0.fill(new java.awt.geom.Ellipse2D.Double(16.0, 10.0, 32.0, 32.0));
      var0.setColor(new Color(60, 40, 10));
      var0.setStroke(new BasicStroke(2.2F, 1, 1));
      if (var1) {
         var0.draw(new java.awt.geom.Arc2D.Double(21.0, 18.0, 8.0, 6.0, 0.0, 180.0, 0));
         var0.draw(new java.awt.geom.Arc2D.Double(35.0, 18.0, 8.0, 6.0, 0.0, 180.0, 0));
         var0.fill(new java.awt.geom.Arc2D.Double(21.0, 21.0, 22.0, 16.0, 180.0, 180.0, 1));
      } else {
         var0.fill(new java.awt.geom.Ellipse2D.Double(23.0, 20.0, 4.0, 5.0));
         var0.fill(new java.awt.geom.Ellipse2D.Double(37.0, 20.0, 4.0, 5.0));
         var0.draw(new java.awt.geom.Arc2D.Double(24.0, 30.0, 16.0, 10.0, 20.0, 140.0, 0));
      }

      if (var1 || var2) {
         var0.setColor(new Color(90, 180, 255));
         var0.fill(new java.awt.geom.Ellipse2D.Double(var2 ? 22.0 : 14.0, var2 ? 26.0 : 23.0, 5.0, 9.0));
         var0.fill(new java.awt.geom.Ellipse2D.Double(var2 ? 37.0 : 45.0, var2 ? 26.0 : 23.0, 5.0, 9.0));
      }
   }
}
