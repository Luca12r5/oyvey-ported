package dev.lego.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;

public final class Art {
   private Art() {
   }

   public static void brick(Graphics2D g, double x, double y, double size, Color base) {
      double s = size;
      double cx = x + size / 2.0;
      double top = y + size * 0.26;
      double mid = y + size * 0.47;
      double bot = y + size * 0.86;
      double lx = x + size * 0.06;
      double rx = x + size * 0.94;
      double h = size * 0.26;
      Color light = mix(base, Color.WHITE, 0.28);
      Color dark = mix(base, Color.BLACK, 0.32);
      Color darker = mix(base, Color.BLACK, 0.5);
      Path2D left = new Double();
      left.moveTo(lx, mid - h * 0.0);
      left.lineTo(cx, mid + (mid - top));
      left.lineTo(cx, bot);
      left.lineTo(lx, bot - (mid - top));
      left.closePath();
      g.setPaint(new GradientPaint((float)lx, (float)mid, mix(base, Color.BLACK, 0.12), (float)cx, (float)bot, dark));
      g.fill(left);
      Path2D right = new Double();
      right.moveTo(rx, mid);
      right.lineTo(cx, mid + (mid - top));
      right.lineTo(cx, bot);
      right.lineTo(rx, bot - (mid - top));
      right.closePath();
      g.setPaint(new GradientPaint((float)cx, (float)mid, dark, (float)rx, (float)bot, darker));
      g.fill(right);
      Path2D topF = new Double();
      topF.moveTo(cx, top - (mid - top));
      topF.lineTo(rx, mid);
      topF.lineTo(cx, mid + (mid - top));
      topF.lineTo(lx, mid);
      topF.closePath();
      g.setPaint(new GradientPaint((float)lx, (float)top, light, (float)rx, (float)mid, base));
      g.fill(topF);
      double sw = size * 0.17;
      double sh = size * 0.085;
      double stH = size * 0.07;
      double[][] studs = new double[][]{{0.0, -1.0}, {-1.0, 0.0}, {1.0, 0.0}, {0.0, 1.0}};

      for (double[] st : studs) {
         double px = cx + st[0] * s * 0.2;
         double py = mid + st[1] * (mid - top) * 0.52 - (mid - top) * 0.02;
         g.setColor(dark);
         g.fill(new java.awt.geom.Ellipse2D.Double(px - sw / 2.0, py - sh / 2.0 - 0.0, sw, sh + stH));
         g.setColor(mix(base, Color.BLACK, 0.18));
         g.fill(new java.awt.geom.Rectangle2D.Double(px - sw / 2.0, py - stH, sw, stH));
         g.setPaint(new GradientPaint((float)(px - sw / 2.0), (float)py, light, (float)(px + sw / 2.0), (float)py, base));
         g.fill(new java.awt.geom.Ellipse2D.Double(px - sw / 2.0, py - stH - sh / 2.0, sw, sh));
      }

      g.setStroke(new BasicStroke((float)Math.max(0.6, s * 0.012)));
      g.setColor(new Color(255, 255, 255, 50));
      g.draw(topF);
   }

   public static Color mix(Color a, Color b, double t) {
      t = Math.max(0.0, Math.min(1.0, t));
      return new Color(
         (int)(a.getRed() + (b.getRed() - a.getRed()) * t),
         (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
         (int)(a.getBlue() + (b.getBlue() - a.getBlue()) * t),
         (int)(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t)
      );
   }

   public static void glow(Graphics2D g, double cx, double cy, double r, Color c) {
      g.setPaint(
         new RadialGradientPaint((float)cx, (float)cy, (float)r, new float[]{0.0F, 1.0F}, new Color[]{c, new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)})
      );
      g.fill(new java.awt.geom.Ellipse2D.Double(cx - r, cy - r, 2.0 * r, 2.0 * r));
   }

   public static Gx.Img brickImg(int px, int argb) {
      return Gx.painted("brick" + Integer.toHexString(argb), px, px, g -> brick(g, 0.0, 0.0, px, new Color(argb, true)));
   }
}
