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
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Ellipse2D.Double;
import java.awt.geom.Point2D.Float;
import java.awt.image.BufferedImage;

public final class AmbientTex {
   public static final int S = 128;

   private AmbientTex() {
   }

   public static int[] paint(String id) {
      BufferedImage img = new BufferedImage(128, 128, 2);
      Graphics2D g = img.createGraphics();
      Gx.hints(g);
      switch (id) {
         case "snow":
            snow(g);
            break;
         case "petal":
            petal(g);
            break;
         case "leaf":
            leaf(g);
            break;
         case "ash":
            soft(g, new Color(90, 90, 96, 230), 0.55F);
            break;
         case "glitter":
            star(g, new Color(255, 255, 255), 0.95F);
            break;
         case "stardust":
            star(g, new Color(190, 220, 255), 0.7F);
            break;
         case "glow":
            soft(g, Color.WHITE, 1.0F);
            break;
         case "core":
            core(g);
            break;
         case "lantern":
            lantern(g);
            break;
         case "bubble":
            bubble(g);
            break;
         case "butterfly0":
            butterfly(g, 1.0F);
            break;
         case "butterfly1":
            butterfly(g, 0.35F);
            break;
         case "bird0":
            bird(g, 1.0F);
            break;
         case "bird1":
            bird(g, -0.6F);
            break;
         case "bat0":
            bat(g, 1.0F);
            break;
         case "bat1":
            bat(g, 0.3F);
            break;
         case "dragonfly":
            dragonfly(g);
            break;
         case "seed":
            seed(g);
            break;
         case "note":
            note(g);
            break;
         case "heart":
            heart(g);
            break;
         case "shard":
            shard(g);
            break;
         case "confetti":
            g.setColor(Color.WHITE);
            g.fillRoundRect(40, 24, 48, 80, 10, 10);
            break;
         case "leafswirl":
            leaf(g);
            break;
         default:
            soft(g, Color.WHITE, 1.0F);
      }

      g.dispose();
      int[] px = img.getRGB(0, 0, 128, 128, null, 0, 128);

      for (int i = 0; i < px.length; i++) {
         if (px[i] >>> 24 == 0) {
            px[i] = 16777215 & avgNeighbour(px, i);
         }
      }

      return px;
   }

   private static int avgNeighbour(int[] px, int i) {
      int x = i % 128;
      int y = i / 128;

      for (int r = 1; r < 4; r++) {
         for (int dy = -r; dy <= r; dy++) {
            for (int dx = -r; dx <= r; dx++) {
               int xx = x + dx;
               int yy = y + dy;
               if (xx >= 0 && yy >= 0 && xx < 128 && yy < 128) {
                  int c = px[yy * 128 + xx];
                  if (c >>> 24 > 0) {
                     return c;
                  }
               }
            }
         }
      }

      return 16777215;
   }

   private static void soft(Graphics2D g, Color c, float strength) {
      g.setPaint(
         new RadialGradientPaint(
            new Float(64.0F, 64.0F),
            62.0F,
            new float[]{0.0F, 0.35F, 1.0F},
            new Color[]{
               new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(c.getAlpha() * strength)),
               new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(c.getAlpha() * strength * 0.45F)),
               new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)
            },
            CycleMethod.NO_CYCLE
         )
      );
      g.fillRect(0, 0, 128, 128);
   }

   private static void core(Graphics2D g) {
      g.setPaint(
         new RadialGradientPaint(
            new Float(64.0F, 64.0F),
            62.0F,
            new float[]{0.0F, 0.12F, 0.3F, 1.0F},
            new Color[]{new Color(255, 255, 255, 255), new Color(255, 255, 255, 230), new Color(255, 255, 255, 70), new Color(255, 255, 255, 0)},
            CycleMethod.NO_CYCLE
         )
      );
      g.fillRect(0, 0, 128, 128);
   }

   private static void snow(Graphics2D g) {
      soft(g, new Color(210, 230, 255, 120), 0.8F);
      g.translate(64, 64);
      g.setStroke(new BasicStroke(5.2F, 1, 1));

      for (int i = 0; i < 6; i++) {
         AffineTransform at = g.getTransform();
         g.rotate((Math.PI / 3) * i);
         g.setColor(new Color(255, 255, 255, 250));
         g.drawLine(0, 0, 0, -46);
         g.setStroke(new BasicStroke(3.6F, 1, 1));
         g.drawLine(0, -18, -11, -29);
         g.drawLine(0, -18, 11, -29);
         g.drawLine(0, -32, -8, -40);
         g.drawLine(0, -32, 8, -40);
         g.setStroke(new BasicStroke(5.2F, 1, 1));
         g.setTransform(at);
      }

      g.setColor(Color.WHITE);
      g.fill(new Double(-7.0, -7.0, 14.0, 14.0));
   }

   private static void petal(Graphics2D g) {
      Path2D p = new java.awt.geom.Path2D.Double();
      p.moveTo(64.0, 112.0);
      p.curveTo(18.0, 88.0, 22.0, 30.0, 52.0, 16.0);
      p.quadTo(64.0, 30.0, 76.0, 16.0);
      p.curveTo(106.0, 30.0, 110.0, 88.0, 64.0, 112.0);
      p.closePath();
      g.setPaint(new GradientPaint(64.0F, 110.0F, new Color(255, 150, 190), 64.0F, 16.0F, new Color(255, 225, 236)));
      g.fill(p);
      g.setColor(new Color(255, 120, 170, 120));
      g.setStroke(new BasicStroke(2.2F));
      g.draw(new java.awt.geom.QuadCurve2D.Double(64.0, 104.0, 60.0, 70.0, 64.0, 34.0));
      g.setColor(new Color(255, 255, 255, 90));
      g.fill(new Double(46.0, 36.0, 16.0, 30.0));
   }

   private static void leaf(Graphics2D g) {
      Path2D p = new java.awt.geom.Path2D.Double();
      p.moveTo(64.0, 10.0);
      p.curveTo(104.0, 34.0, 110.0, 80.0, 64.0, 116.0);
      p.curveTo(18.0, 80.0, 24.0, 34.0, 64.0, 10.0);
      p.closePath();
      g.setPaint(new GradientPaint(30.0F, 20.0F, new Color(255, 196, 64), 100.0F, 110.0F, new Color(214, 64, 28)));
      g.fill(p);
      g.setColor(new Color(120, 40, 10, 170));
      g.setStroke(new BasicStroke(3.0F, 1, 1));
      g.drawLine(64, 18, 64, 122);

      for (int i = 0; i < 4; i++) {
         int y = 36 + i * 18;
         g.drawLine(64, y + 8, 44 + i * 2, y - 4);
         g.drawLine(64, y + 8, 84 - i * 2, y - 4);
      }
   }

   private static void star(Graphics2D g, Color c, float glow) {
      soft(g, new Color(c.getRed(), c.getGreen(), c.getBlue(), 160), glow);
      Path2D p = new java.awt.geom.Path2D.Double();

      for (int i = 0; i < 8; i++) {
         double a = (Math.PI / 4) * i - (Math.PI / 2);
         double r = i % 2 == 0 ? 60.0 : 9.0;
         double x = 64.0 + Math.cos(a) * r;
         double y = 64.0 + Math.sin(a) * r;
         if (i == 0) {
            p.moveTo(x, y);
         } else {
            p.lineTo(x, y);
         }
      }

      p.closePath();
      g.setColor(new Color(255, 255, 255, 250));
      g.fill(p);
   }

   private static void lantern(Graphics2D g) {
      soft(g, new Color(255, 150, 60, 200), 0.9F);
      RoundRectangle2D body = new java.awt.geom.RoundRectangle2D.Double(38.0, 26.0, 52.0, 70.0, 18.0, 18.0);
      g.setPaint(new GradientPaint(64.0F, 26.0F, new Color(255, 214, 140), 64.0F, 96.0F, new Color(255, 120, 40)));
      g.fill(body);
      g.setColor(new Color(255, 250, 220, 230));
      g.fill(new Double(52.0, 54.0, 24.0, 34.0));
      g.setColor(new Color(170, 60, 20, 170));
      g.setStroke(new BasicStroke(2.4F));

      for (int i = 1; i < 4; i++) {
         g.drawLine(38 + i * 13, 30, 38 + i * 13, 92);
      }

      g.setColor(new Color(120, 60, 30));
      g.fillRect(44, 94, 40, 5);
   }

   private static void bubble(Graphics2D g) {
      g.setPaint(
         new RadialGradientPaint(
            new Float(64.0F, 64.0F),
            58.0F,
            new float[]{0.0F, 0.78F, 0.92F, 1.0F},
            new Color[]{new Color(255, 255, 255, 12), new Color(200, 230, 255, 40), new Color(255, 190, 255, 170), new Color(160, 255, 230, 0)},
            CycleMethod.NO_CYCLE
         )
      );
      g.fill(new Double(6.0, 6.0, 116.0, 116.0));
      g.setColor(new Color(255, 255, 255, 200));
      g.fill(new Double(34.0, 28.0, 22.0, 14.0));
      g.setColor(new Color(255, 255, 255, 120));
      g.fill(new Double(80.0, 84.0, 10.0, 7.0));
   }

   private static void bird(Graphics2D g, float up) {
      g.setColor(new Color(40, 44, 56));
      g.setStroke(new BasicStroke(9.0F, 1, 1));
      Path2D p = new java.awt.geom.Path2D.Double();
      p.moveTo(10.0, 64.0F - 30.0F * up);
      p.quadTo(38.0, 64.0F - 34.0F * up, 64.0, 66.0);
      p.quadTo(90.0, 64.0F - 34.0F * up, 118.0, 64.0F - 30.0F * up);
      g.draw(p);
      g.fill(new Double(56.0, 58.0, 16.0, 14.0));
   }

   private static void bat(Graphics2D g, float open) {
      g.translate(64, 64);
      g.setColor(new Color(28, 20, 36));

      for (int s = -1; s <= 1; s += 2) {
         AffineTransform at = g.getTransform();
         g.scale(s, open);
         Path2D w = new java.awt.geom.Path2D.Double();
         w.moveTo(4.0, -6.0);
         w.lineTo(30.0, -30.0);
         w.lineTo(58.0, -18.0);
         w.lineTo(50.0, 4.0);
         w.lineTo(40.0, -2.0);
         w.lineTo(32.0, 10.0);
         w.lineTo(22.0, 2.0);
         w.lineTo(12.0, 12.0);
         w.closePath();
         g.fill(w);
         g.setTransform(at);
      }

      g.fill(new Double(-9.0, -12.0, 18.0, 26.0));
      g.setColor(new Color(255, 70, 70));
      g.fill(new Double(-5.0, -6.0, 3.0, 3.0));
      g.fill(new Double(2.0, -6.0, 3.0, 3.0));
   }

   private static void dragonfly(Graphics2D g) {
      g.setColor(new Color(170, 230, 255, 150));
      g.fill(new Double(12.0, 40.0, 50.0, 16.0));
      g.fill(new Double(66.0, 40.0, 50.0, 16.0));
      g.fill(new Double(16.0, 60.0, 46.0, 14.0));
      g.fill(new Double(66.0, 60.0, 46.0, 14.0));
      g.setPaint(new GradientPaint(64.0F, 30.0F, new Color(40, 220, 190), 64.0F, 120.0F, new Color(30, 90, 200)));
      g.fill(new java.awt.geom.RoundRectangle2D.Double(58.0, 30.0, 12.0, 90.0, 12.0, 12.0));
      g.fill(new Double(54.0, 22.0, 20.0, 18.0));
   }

   private static void seed(Graphics2D g) {
      g.setColor(new Color(255, 255, 255, 230));
      g.setStroke(new BasicStroke(2.4F, 1, 1));

      for (int i = 0; i < 14; i++) {
         double a = (Math.PI * 2) * i / 14.0;
         g.drawLine(64, 52, (int)(64.0 + Math.cos(a) * 40.0), (int)(52.0 + Math.sin(a) * 40.0));
      }

      g.setColor(new Color(230, 230, 220));
      g.drawLine(64, 52, 64, 118);
      g.setColor(new Color(150, 120, 80));
      g.fill(new Double(59.0, 110.0, 10.0, 14.0));
   }

   private static void note(Graphics2D g) {
      g.setColor(Color.WHITE);
      g.fill(new Double(24.0, 82.0, 34.0, 26.0));
      g.fill(new Double(72.0, 70.0, 34.0, 26.0));
      g.setStroke(new BasicStroke(8.0F));
      g.drawLine(54, 94, 54, 22);
      g.drawLine(102, 82, 102, 14);
      g.fill(new Polygon(new int[]{50, 106, 106, 50}, new int[]{18, 8, 26, 36}, 4));
   }

   private static void heart(Graphics2D g) {
      Path2D h = new java.awt.geom.Path2D.Double();
      h.moveTo(64.0, 112.0);
      h.curveTo(10.0, 76.0, 12.0, 22.0, 44.0, 22.0);
      h.curveTo(56.0, 22.0, 62.0, 32.0, 64.0, 40.0);
      h.curveTo(66.0, 32.0, 72.0, 22.0, 84.0, 22.0);
      h.curveTo(116.0, 22.0, 118.0, 76.0, 64.0, 112.0);
      g.setColor(Color.WHITE);
      g.fill(h);
   }

   private static void shard(Graphics2D g) {
      soft(g, new Color(200, 220, 255, 120), 0.6F);
      Path2D p = new java.awt.geom.Path2D.Double();
      p.moveTo(64.0, 8.0);
      p.lineTo(92.0, 56.0);
      p.lineTo(70.0, 120.0);
      p.lineTo(40.0, 64.0);
      p.closePath();
      g.setPaint(new GradientPaint(40.0F, 10.0F, new Color(255, 255, 255), 90.0F, 120.0F, new Color(150, 200, 255)));
      g.fill(p);
      g.setColor(new Color(255, 255, 255, 180));
      g.drawLine(64, 8, 70, 120);
   }

   private static void butterfly(Graphics2D g, float open) {
      g.translate(64, 64);

      for (int side = -1; side <= 1; side += 2) {
         AffineTransform at = g.getTransform();
         g.scale(side * open, 1.0);
         Path2D up = new java.awt.geom.Path2D.Double();
         up.moveTo(2.0, -4.0);
         up.curveTo(20.0, -52.0, 62.0, -48.0, 54.0, -14.0);
         up.curveTo(48.0, 2.0, 20.0, 6.0, 2.0, 2.0);
         up.closePath();
         Path2D lo = new java.awt.geom.Path2D.Double();
         lo.moveTo(2.0, 2.0);
         lo.curveTo(30.0, 6.0, 48.0, 30.0, 32.0, 46.0);
         lo.curveTo(18.0, 56.0, 6.0, 30.0, 2.0, 8.0);
         lo.closePath();
         g.setPaint(new GradientPaint(0.0F, -40.0F, new Color(90, 200, 255), 50.0F, 10.0F, new Color(150, 80, 255)));
         g.fill(up);
         g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(120, 90, 255), 30.0F, 48.0F, new Color(255, 120, 220)));
         g.fill(lo);
         g.setColor(new Color(20, 16, 40, 200));
         g.setStroke(new BasicStroke(3.0F));
         g.draw(up);
         g.draw(lo);
         g.setColor(new Color(255, 255, 255, 220));
         g.fill(new Double(30.0, -34.0, 9.0, 9.0));
         g.setTransform(at);
      }

      g.setColor(new Color(30, 24, 40));
      g.fill(new java.awt.geom.RoundRectangle2D.Double(-3.0, -22.0, 6.0, 50.0, 6.0, 6.0));
   }
}
