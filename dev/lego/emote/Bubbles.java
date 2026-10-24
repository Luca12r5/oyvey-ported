package dev.lego.emote;

import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D.Double;
import java.awt.image.BufferedImage;

public final class Bubbles {
   private Bubbles() {
   }

   public static int[] paint(String id, int s) {
      BufferedImage img = new BufferedImage(s, s, 2);
      Graphics2D g = img.createGraphics();
      Gx.hints(g);
      g.scale(s / 64.0, s / 64.0);
      g.setColor(new Color(0, 0, 0, 45));
      g.fill(new Double(5.0, 6.0, 56.0, 46.0, 24.0, 24.0));
      g.setColor(new Color(255, 255, 255, 245));
      g.fill(new Double(4.0, 4.0, 56.0, 46.0, 24.0, 24.0));
      Path2D tail = new java.awt.geom.Path2D.Double();
      tail.moveTo(26.0, 48.0);
      tail.lineTo(32.0, 60.0);
      tail.lineTo(38.0, 48.0);
      tail.closePath();
      g.fill(tail);
      switch (id) {
         case "heart":
            g.setPaint(new GradientPaint(20.0F, 12.0F, new Color(255, 110, 140), 44.0F, 42.0F, new Color(220, 20, 60)));
            Path2D h = new java.awt.geom.Path2D.Double();
            h.moveTo(32.0, 43.0);
            h.curveTo(12.0, 30.0, 15.0, 11.0, 26.0, 12.0);
            h.curveTo(30.0, 12.5, 32.0, 16.0, 32.0, 18.0);
            h.curveTo(32.0, 16.0, 34.0, 12.5, 38.0, 12.0);
            h.curveTo(49.0, 11.0, 52.0, 30.0, 32.0, 43.0);
            g.fill(h);
            g.setColor(new Color(255, 255, 255, 150));
            g.fill(new java.awt.geom.Ellipse2D.Double(21.0, 16.0, 7.0, 5.0));
            break;
         case "gg":
            text(g, "GG", new Color(30, 215, 96), 24.0F);
            break;
         case "laugh":
            face(g, true, false);
            break;
         case "cry":
            face(g, false, true);
            break;
         case "think":
            text(g, "?", new Color(4172287), 30.0F);
            break;
         case "zen":
            g.setColor(new Color(10177994));
            g.setStroke(new BasicStroke(3.0F, 1, 1));
            g.draw(new java.awt.geom.Ellipse2D.Double(20.0, 13.0, 24.0, 24.0));
            g.fill(new java.awt.geom.Arc2D.Double(20.0, 13.0, 24.0, 24.0, 90.0, 180.0, 1));
            g.setColor(Color.WHITE);
            g.fill(new java.awt.geom.Ellipse2D.Double(29.0, 16.0, 6.0, 6.0));
            g.setColor(new Color(10177994));
            g.fill(new java.awt.geom.Ellipse2D.Double(29.0, 28.0, 6.0, 6.0));
            break;
         case "sleep":
            text(g, "Zzz", new Color(5991423), 18.0F);
            break;
         default:
            g.setPaint(new GradientPaint(32.0F, 10.0F, new Color(255, 220, 60), 32.0F, 44.0F, new Color(255, 60, 20)));
            Path2D f = new java.awt.geom.Path2D.Double();
            f.moveTo(32.0, 44.0);
            f.curveTo(18.0, 44.0, 16.0, 30.0, 24.0, 22.0);
            f.curveTo(24.0, 28.0, 28.0, 30.0, 30.0, 28.0);
            f.curveTo(26.0, 20.0, 30.0, 12.0, 36.0, 8.0);
            f.curveTo(36.0, 16.0, 48.0, 22.0, 46.0, 34.0);
            f.curveTo(45.0, 41.0, 40.0, 44.0, 32.0, 44.0);
            g.fill(f);
      }

      g.dispose();
      return img.getRGB(0, 0, s, s, null, 0, s);
   }

   private static void text(Graphics2D g, String s, Color c, float size) {
      g.setColor(c);
      g.setFont(Fonts.get(3, size));
      double w = g.getFontMetrics().getStringBounds(s, g).getWidth();
      g.drawString(s, (float)(32.0 - w / 2.0), 27.0F + size * 0.36F);
   }

   private static void face(Graphics2D g, boolean laugh, boolean cry) {
      g.setPaint(new RadialGradientPaint(28.0F, 20.0F, 22.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(255, 225, 90), new Color(245, 180, 0)}));
      g.fill(new java.awt.geom.Ellipse2D.Double(16.0, 10.0, 32.0, 32.0));
      g.setColor(new Color(60, 40, 10));
      g.setStroke(new BasicStroke(2.2F, 1, 1));
      if (laugh) {
         g.draw(new java.awt.geom.Arc2D.Double(21.0, 18.0, 8.0, 6.0, 0.0, 180.0, 0));
         g.draw(new java.awt.geom.Arc2D.Double(35.0, 18.0, 8.0, 6.0, 0.0, 180.0, 0));
         g.fill(new java.awt.geom.Arc2D.Double(21.0, 21.0, 22.0, 16.0, 180.0, 180.0, 1));
      } else {
         g.fill(new java.awt.geom.Ellipse2D.Double(23.0, 20.0, 4.0, 5.0));
         g.fill(new java.awt.geom.Ellipse2D.Double(37.0, 20.0, 4.0, 5.0));
         g.draw(new java.awt.geom.Arc2D.Double(24.0, 30.0, 16.0, 10.0, 20.0, 140.0, 0));
      }

      if (laugh || cry) {
         g.setColor(new Color(90, 180, 255));
         g.fill(new java.awt.geom.Ellipse2D.Double(cry ? 22.0 : 14.0, cry ? 26.0 : 23.0, 5.0, 9.0));
         g.fill(new java.awt.geom.Ellipse2D.Double(cry ? 37.0 : 45.0, cry ? 26.0 : 23.0, 5.0, 9.0));
      }
   }
}
