package dev.lego.cosmetic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

final class XmasCapes {
   private static final String[] DEER = new String[]{
      "..W.W...W.W", "...W.....W.", "...WW...WW.", ".....WWW...", ".....WWWK..", "WWWWWWWW...", "WWWWWWWW...", "W.W...W.W..", "W.W...W.W.."
   };
   private static final String[] TREE = new String[]{
      "....Y....", "...GGG...", "..GLGGG..", "...GGG...", "..GGGLG..", ".GLGGGGG.", "..GGGGG..", ".GGGGLGG.", "GLGGGGGLG", "....B...."
   };
   private static final String[] FLAKE = new String[]{"..W.W..", "W..W..W", ".W.W.W.", "WWWWWWW", ".W.W.W.", "W..W..W", "..W.W.."};

   private XmasCapes() {
   }

   static void defineAll() {
      Capes.def("xm_winterwald", 16, 8.0F, 15267071, 2771578, 924208, XmasCapes::winterForest);
      Capes.def("xm_lebkuchenhaus", 16, 8.0F, 16179392, 8010260, 2759216, XmasCapes::gingerHouse);
      Capes.def("xm_zuckerstange", 16, 10.0F, 16777215, 13112350, 16052460, XmasCapes::candyCane);
      Capes.def("xm_schlittenmond", 16, 8.0F, 16771496, 1714778, 659492, XmasCapes::sleighMoon);
      Capes.def("xm_pullover", 8, 4.0F, 16777215, 1993268, 9048088, XmasCapes::sweater);
      Capes.def("xm_nordlicht", 16, 8.0F, 12124128, 1718874, 397342, XmasCapes::auroraXmas);
   }

   static void snow(Graphics2D g, double t, double ph, int count, int seed, double alpha) {
      Random rn = new Random(seed);

      for (int i = 0; i < count; i++) {
         double x0 = rn.nextDouble() * 160.0;
         double y0 = rn.nextDouble();
         double sp = 1 + rn.nextInt(2);
         double r = 0.8 + rn.nextDouble() * 1.8 * (sp == 2.0 ? 1.3 : 0.8);
         double y = (y0 + sp * t) % 1.0 * 266.0 - 5.0;
         double x = x0 + Math.sin(ph * sp + i * 1.7) * 5.0;
         Capes.dot(g, x, y, r * 1.8, 16777215, alpha * (0.55 + 0.3 * rn.nextDouble()));
         g.setColor(Capes.col(16777215, alpha * 0.9));
         g.fill(new Double(x - r * 0.5, y - r * 0.5, r, r));
      }
   }

   static void stars(Graphics2D g, double ph, int count, int seed, double maxY) {
      Random rn = new Random(seed);

      for (int i = 0; i < count; i++) {
         double x = rn.nextDouble() * 160.0;
         double y = rn.nextDouble() * maxY;
         double r = 0.7 + rn.nextDouble() * 1.6;
         double tw = 0.55 + 0.45 * Math.sin(ph * (1 + rn.nextInt(2)) + i * 2.3);
         Capes.star(g, x, y, r, rn.nextBoolean() ? 16774360 : 14215423, tw);
      }
   }

   static void pine(Graphics2D g, double x, double y, double h, int body, int snowC, int tiers) {
      g.setColor(new Color(Capes.mix(body, 0, 0.3)));
      g.fill(new java.awt.geom.Rectangle2D.Double(x - h * 0.04, y - h * 0.12, h * 0.08, h * 0.14));

      for (int i = 0; i < tiers; i++) {
         double f = (double)i / tiers;
         double ty = y - h * 0.1 - f * h * 0.78;
         double w = h * (0.42 - f * 0.3);
         double th = h * 0.4;
         Path2D p = new java.awt.geom.Path2D.Double();
         p.moveTo(x - w, ty);
         p.quadTo(x - w * 0.5, ty + h * 0.03, x, ty + h * 0.012);
         p.quadTo(x + w * 0.5, ty + h * 0.03, x + w, ty);
         p.lineTo(x, ty - th);
         p.closePath();
         g.setPaint(
            new GradientPaint((float)(x - w), 0.0F, new Color(Capes.mix(body, 16777215, 0.12)), (float)(x + w), 0.0F, new Color(Capes.mix(body, 0, 0.25)))
         );
         g.fill(p);
         Path2D s = new java.awt.geom.Path2D.Double();
         s.moveTo(x - w * 0.82, ty - th * 0.12);
         s.quadTo(x - w * 0.45, ty - th * 0.02, x - w * 0.2, ty - th * 0.2);
         s.quadTo(x + w * 0.1, ty - th * 0.05, x + w * 0.55, ty - th * 0.16);
         s.lineTo(x + w * 0.3, ty - th * 0.4);
         s.lineTo(x, ty - th * 0.62);
         s.lineTo(x - w * 0.35, ty - th * 0.36);
         s.closePath();
         g.setColor(Capes.col(snowC, 0.92));
         g.fill(s);
      }

      g.setColor(Capes.col(snowC, 0.95));
      g.fill(new Double(x - h * 0.03, y - h * 0.92, h * 0.06, h * 0.05));
   }

   static void winterForest(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      Capes.field(img, (u, v, xx, yx) -> {
         int c = Capes.ramp(v * 1.4, 461858, 1319498, 3033728, 6982336);
         double neb = Capes.fbm(u * 3.0, v * 3.0, 3, 3, 4, 701);
         return Capes.add(c, 3824288, neb * 0.18 * (1.0 - v));
      });
      stars(g, ph, 45, 702, 150.0);
      Capes.addDot(img, 116.0, 52.0, 34.0, 13162751, 0.35);
      g.setPaint(new RadialGradientPaint(112.0F, 48.0F, 16.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(16776428), new Color(14736584)}));
      g.fill(new Double(102.0, 38.0, 28.0, 28.0));
      g.setColor(Capes.col(12104864, 0.5));
      g.fill(new Double(110.0, 44.0, 6.0, 5.0));
      g.fill(new Double(119.0, 53.0, 4.0, 4.0));
      Path2D hills = new java.awt.geom.Path2D.Double();
      hills.moveTo(0.0, 170.0);

      for (int x = 0; x <= 160; x += 8) {
         hills.lineTo(x, 160.0 - 12.0 * Math.sin(x * 0.03 + 1.0) - 6.0 * Math.sin(x * 0.08));
      }

      hills.lineTo(160.0, 256.0);
      hills.lineTo(0.0, 256.0);
      hills.closePath();
      g.setPaint(new GradientPaint(0.0F, 140.0F, new Color(10138842), 0.0F, 200.0F, new Color(5929128)));
      g.fill(hills);
      Random rn = new Random(703L);

      for (int i = 0; i < 11; i++) {
         pine(g, 6 + i * 15 + rn.nextDouble() * 6.0, 168.0 + rn.nextDouble() * 6.0, 30.0 + rn.nextDouble() * 14.0, 2771562, 12111082, 3);
      }

      double cx = 58.0;
      double cy = 196.0;
      g.setColor(new Color(4860442));
      g.fill(new java.awt.geom.Rectangle2D.Double(cx - 16.0, cy - 16.0, 32.0, 18.0));
      Path2D roof = new java.awt.geom.Path2D.Double();
      roof.moveTo(cx - 21.0, cy - 14.0);
      roof.lineTo(cx, cy - 31.0);
      roof.lineTo(cx + 21.0, cy - 14.0);
      roof.closePath();
      g.setColor(new Color(2759188));
      g.fill(roof);
      g.setColor(new Color(16054527));
      g.fill(new java.awt.geom.RoundRectangle2D.Double(cx - 22.0, cy - 19.0, 44.0, 6.0, 5.0, 5.0));
      Path2D rs = new java.awt.geom.Path2D.Double();
      rs.moveTo(cx - 19.0, cy - 16.0);
      rs.lineTo(cx, cy - 32.0);
      rs.lineTo(cx + 19.0, cy - 16.0);
      rs.lineTo(cx + 12.0, cy - 18.0);
      rs.lineTo(cx, cy - 27.0);
      rs.lineTo(cx - 12.0, cy - 18.0);
      rs.closePath();
      g.fill(rs);
      double fl = 0.8 + 0.1 * Math.sin(ph * 3.0) + 0.06 * Math.sin(ph * 7.0 + 1.0);

      for (int w = -1; w <= 1; w += 2) {
         g.setColor(new Color(Capes.mix(16751146, 16769162, fl)));
         g.fill(new java.awt.geom.Rectangle2D.Double(cx + w * 8 - 4.0, cy - 11.0, 8.0, 7.0));
         Capes.addDot(img, cx + w * 8, cy - 7.0, 12.0, 16752704, 0.35 * fl);
         g.setColor(new Color(2759188));
         g.fill(new java.awt.geom.Rectangle2D.Double(cx + w * 8 - 0.6, cy - 11.0, 1.2, 7.0));
      }

      g.setColor(new Color(2759188));
      g.fill(new java.awt.geom.Rectangle2D.Double(cx + 8.0, cy - 32.0, 5.0, 10.0));

      for (int i = 0; i < 6; i++) {
         double f = (t * 2.0 + i / 6.0) % 1.0;
         Capes.dot(g, cx + 10.5 + Math.sin(f * 6.0 + i) * 3.0 + f * 10.0, cy - 34.0 - f * 40.0, 4.0 + f * 7.0, 13160672, 0.35 * (1.0 - f));
      }

      Path2D gr = new java.awt.geom.Path2D.Double();
      gr.moveTo(0.0, 204.0);

      for (int x = 0; x <= 160; x += 8) {
         gr.lineTo(x, 200.0 + 4.0 * Math.sin(x * 0.05 + 2.0));
      }

      gr.lineTo(160.0, 256.0);
      gr.lineTo(0.0, 256.0);
      gr.closePath();
      g.setPaint(new GradientPaint(0.0F, 196.0F, new Color(16054527), 0.0F, 256.0F, new Color(12111082)));
      g.fill(gr);

      for (int i = 0; i < 30; i++) {
         double x = rn.nextDouble() * 160.0;
         double y = 206.0 + rn.nextDouble() * 44.0;
         Capes.star(g, x, y, 0.8 + rn.nextDouble(), 16777215, 0.3 + 0.6 * Math.max(0.0, Math.sin(ph * 2.0 + i * 1.9)));
      }

      pine(g, 14.0, 238.0, 110.0, 1194532, 16054527, 5);
      pine(g, 142.0, 232.0, 96.0, 1458730, 16054527, 5);
      pine(g, 112.0, 214.0, 58.0, 1722928, 15266047, 4);
      snow(g, t, ph, 110, 704, 0.9);
   }

   static void gingerHouse(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      Capes.field(img, (u, v, xx, yx) -> Capes.ramp(v, 1313316, 2759242, 4860506));
      stars(g, ph, 30, 711, 100.0);
      double cx = 80.0;
      double base = 206.0;
      double w = 96.0;
      double wh = 70.0;
      g.setColor(new Color(10115626));
      g.fill(new java.awt.geom.Rectangle2D.Double(cx + 18.0, base - wh - 58.0, 14.0, 34.0));
      g.setColor(Color.WHITE);
      g.fill(new java.awt.geom.RoundRectangle2D.Double(cx + 16.0, base - wh - 62.0, 18.0, 7.0, 6.0, 6.0));

      for (int i = 0; i < 6; i++) {
         double f = (t * 2.0 + i / 6.0) % 1.0;
         Capes.dot(g, cx + 25.0 + Math.sin(f * 5.0 + i) * 4.0 + f * 8.0, base - wh - 66.0 - f * 50.0, 5.0 + f * 8.0, 14735592, 0.4 * (1.0 - f));
      }

      Rectangle2D wall = new java.awt.geom.Rectangle2D.Double(cx - w / 2.0, base - wh, w, wh);
      g.setPaint(new GradientPaint(0.0F, (float)(base - wh), new Color(12876346), 0.0F, (float)base, new Color(9062942)));
      g.fill(wall);
      Random rn = new Random(712L);

      for (int i = 0; i < 160; i++) {
         g.setColor(Capes.col(5909002, 0.35));
         g.fill(new Double(cx - w / 2.0 + rn.nextDouble() * w, base - wh + rn.nextDouble() * wh, 1.6, 1.6));
      }

      g.setColor(Color.WHITE);
      g.setStroke(new BasicStroke(3.0F, 1, 1));
      g.draw(new java.awt.geom.Line2D.Double(cx - w / 2.0 + 2.0, base - wh + 4.0, cx - w / 2.0 + 2.0, base - 2.0));
      g.draw(new java.awt.geom.Line2D.Double(cx + w / 2.0 - 2.0, base - wh + 4.0, cx + w / 2.0 - 2.0, base - 2.0));
      Path2D roof = new java.awt.geom.Path2D.Double();
      roof.moveTo(cx - w / 2.0 - 12.0, base - wh + 6.0);
      roof.lineTo(cx, base - wh - 56.0);
      roof.lineTo(cx + w / 2.0 + 12.0, base - wh + 6.0);
      roof.closePath();
      g.setPaint(new GradientPaint(0.0F, (float)(base - wh - 56.0), new Color(11033124), 0.0F, (float)(base - wh), new Color(8010260)));
      g.fill(roof);
      g.setStroke(new BasicStroke(1.6F, 1, 1));

      for (int row = 0; row < 5; row++) {
         double y = base - wh - 44.0 + row * 11;
         double half = (y - (base - wh - 56.0)) / 62.0 * (w / 2.0 + 12.0);

         for (double x = cx - half + 6.0; x < cx + half - 4.0; x += 10.0) {
            g.setColor(Capes.col(16774376, 0.85));
            g.draw(new java.awt.geom.Arc2D.Double(x - 5.0, y - 5.0, 10.0, 10.0, 180.0, 180.0, 0));
         }
      }

      g.setColor(Color.WHITE);
      g.setStroke(new BasicStroke(6.0F, 1, 1));
      Path2D edge = new java.awt.geom.Path2D.Double();
      edge.moveTo(cx - w / 2.0 - 12.0, base - wh + 6.0);
      edge.lineTo(cx, base - wh - 56.0);
      edge.lineTo(cx + w / 2.0 + 12.0, base - wh + 6.0);
      g.draw(edge);

      for (int i = 0; i < 12; i++) {
         double f = (i + 0.5) / 12.0;
         double side = f < 0.5 ? -1.0 : 1.0;
         double ff = f < 0.5 ? f * 2.0 : (1.0 - f) * 2.0;
         double x = cx + side * (1.0 - ff) * (w / 2.0 + 12.0);
         double y = base - wh + 6.0 - ff * 62.0;
         double len = 4.0 + 5.0 * Capes.hash1(i, 713);
         g.fill(new java.awt.geom.RoundRectangle2D.Double(x - 2.0, y, 4.0, len, 4.0, 4.0));
         g.fill(new Double(x - 2.5, y + len - 2.5, 5.0, 5.0));
      }

      int[] gd = new int[]{15217210, 3061834, 16765498, 3836671, 15227608};

      for (int i = 0; i < 7; i++) {
         double f = (i + 1) / 8.0;
         double x = cx - (w / 2.0 + 6.0) + f * (w + 12.0);
         double y = base - wh + 2.0 - (1.0 - Math.abs(f - 0.5) * 2.0) * 58.0 - 4.0;
         gumdrop(g, x, y, 4.5, gd[i % gd.length]);
      }

      double fl = 0.8 + 0.12 * Math.sin(ph * 3.0) + 0.06 * Math.sin(ph * 5.0 + 1.0);

      for (int s = -1; s <= 1; s += 2) {
         double wx = cx + s * 28;
         double wy = base - wh + 14.0;
         Capes.addDot(img, wx, wy + 9.0, 22.0, 16752704, 0.4 * fl);
         g.setColor(new Color(Capes.mix(16751146, 16771488, fl)));
         g.fill(new java.awt.geom.RoundRectangle2D.Double(wx - 10.0, wy, 20.0, 18.0, 4.0, 4.0));
         g.setColor(Color.WHITE);
         g.setStroke(new BasicStroke(2.4F));
         g.draw(new java.awt.geom.RoundRectangle2D.Double(wx - 10.0, wy, 20.0, 18.0, 4.0, 4.0));
         g.draw(new java.awt.geom.Line2D.Double(wx, wy, wx, wy + 18.0));
         g.draw(new java.awt.geom.Line2D.Double(wx - 10.0, wy + 9.0, wx + 10.0, wy + 9.0));
      }

      g.setColor(new Color(5909008));
      g.fill(new java.awt.geom.RoundRectangle2D.Double(cx - 11.0, base - 38.0, 22.0, 38.0, 20.0, 20.0));
      g.setColor(new Color(16765498));
      g.fill(new Double(cx + 5.0, base - 20.0, 4.0, 4.0));

      for (int s = -1; s <= 1; s += 2) {
         candyStick(g, cx + s * 15, base, base - 44.0, s);
      }

      g.setColor(new Color(3050052));
      g.setStroke(new BasicStroke(3.5F));
      g.draw(new Double(cx - 6.0, base - 34.0, 12.0, 12.0));
      g.setColor(new Color(15217210));
      g.fill(new Double(cx - 2.0, base - 25.0, 4.0, 4.0));
      g.setPaint(new GradientPaint(0.0F, (float)base, new Color(16053503), 0.0F, 256.0F, new Color(13159656)));
      g.fill(new java.awt.geom.Rectangle2D.Double(0.0, base, 160.0, 256.0 - base));

      for (int i = 0; i < 5; i++) {
         double y = base + 6.0 + i * 9;
         double x = cx + (i % 2 == 0 ? -3 : 3);
         g.setColor(new Color(i % 2 == 0 ? 15217210 : 3061834));
         g.fill(new Double(x - 5.0 - i, y - 2.0, 10 + i * 2, 5.0));
      }

      for (int s = -1; s <= 1; s += 2) {
         double x = cx + s * 62;
         double y = base + 16.0;
         g.setColor(new Color(16052460));
         g.fill(new java.awt.geom.Rectangle2D.Double(x - 1.2, y - 28.0, 2.4, 30.0));
         g.setColor(new Color(s < 0 ? 15217210 : 3836671));
         g.fill(new Double(x - 9.0, y - 44.0, 18.0, 18.0));
         g.setColor(Color.WHITE);
         g.setStroke(new BasicStroke(2.0F));
         g.draw(new java.awt.geom.Arc2D.Double(x - 6.0, y - 41.0, 12.0, 12.0, 90.0 + ph * 57.3 * s, 250.0, 0));
      }

      snow(g, t, ph, 70, 714, 0.85);
   }

   static void gumdrop(Graphics2D g, double x, double y, double r, int c) {
      g.setPaint(
         new RadialGradientPaint(
            (float)(x - r * 0.3),
            (float)(y - r * 0.4),
            (float)(r * 1.3),
            new float[]{0.0F, 1.0F},
            new Color[]{new Color(Capes.mix(c, 16777215, 0.45)), new Color(Capes.mix(c, 0, 0.2))}
         )
      );
      Path2D p = new java.awt.geom.Path2D.Double();
      p.moveTo(x - r, y + r * 0.6);
      p.quadTo(x - r, y - r, x, y - r);
      p.quadTo(x + r, y - r, x + r, y + r * 0.6);
      p.closePath();
      g.fill(p);
      g.setColor(Capes.col(16777215, 0.8));

      for (int i = 0; i < 4; i++) {
         g.fill(new Double(x - r * 0.6 + i * r * 0.4, y - r * 0.2 + i % 2 * r * 0.4, 1.1, 1.1));
      }
   }

   static void candyStick(Graphics2D g, double x, double y0, double y1, int dir) {
      Path2D p = new java.awt.geom.Path2D.Double();
      p.moveTo(x, y0);
      p.lineTo(x, y1 + 6.0);
      p.quadTo(x, y1 - 2.0, x + dir * 5, y1 - 2.0);
      p.quadTo(x + dir * 10, y1 - 2.0, x + dir * 10, y1 + 5.0);
      g.setColor(Color.WHITE);
      g.setStroke(new BasicStroke(5.0F, 1, 1));
      g.draw(p);
      g.setColor(new Color(14688298));
      g.setStroke(new BasicStroke(5.0F, 0, 1, 1.0F, new float[]{3.5F, 3.5F}, 0.0F));
      g.draw(p);
      g.setStroke(new BasicStroke(1.0F));
   }

   static void candyCane(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      double P = 40.0;
      Capes.field(img, (u, v, xx, yx) -> {
         double d = (xx + yx * 0.8) / 40.0 - t;
         double s = d - Math.floor(d);
         int c = s < 0.38 ? 14160926 : (s > 0.46 && s < 0.5 ? 15227482 : (s > 0.66 && s < 0.7 ? 3057738 : 16512752));
         double edge = Math.min(Math.min(Math.abs(s), Math.abs(s - 0.38)), Math.abs(1.0 - s));
         if (edge < 0.012) {
            c = Capes.mix(c, 15769760, 0.5);
         }

         double sh = 0.86 + 0.18 * Math.sin(u * Math.PI) + 0.25 * Math.exp(-Math.pow((u - 0.32) / 0.06, 2.0));
         return Capes.shade(c, sh);
      });
      Capes.sweep(img, t, 0.07, 0.35);
      double cx = 80.0;
      double cy = 108.0;

      for (int i = 0; i < 26; i++) {
         double a = (Math.PI * 2) * i / 26.0;
         double r = 30 + i % 2 * 3;
         double x = cx + Math.cos(a) * r;
         double y = cy + Math.sin(a) * r;
         g.setPaint(
            new RadialGradientPaint((float)x - 2.0F, (float)y - 2.0F, 11.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(4896864), new Color(1727018)})
         );
         g.fill(new Double(x - 9.0, y - 7.0, 18.0, 14.0));
      }

      for (int i = 0; i < 10; i++) {
         double a = (Math.PI * 2) * i / 10.0 + 0.3;
         double x = cx + Math.cos(a) * 31.0;
         double y = cy + Math.sin(a) * 31.0;
         double tw = 0.6 + 0.4 * Math.sin(ph * 2.0 + i * 1.3);
         Capes.addDot(img, x, y, 7.0, i % 2 == 0 ? 16726586 : 16765498, 0.5 * tw);
         g.setColor(new Color(i % 2 == 0 ? 15212586 : 16765498));
         g.fill(new Double(x - 3.0, y - 3.0, 6.0, 6.0));
         g.setColor(Capes.col(16777215, 0.8));
         g.fill(new Double(x - 1.8, y - 2.0, 2.0, 2.0));
      }

      g.setColor(new Color(14160926));
      Path2D bl = new java.awt.geom.Path2D.Double();
      bl.moveTo(cx, cy + 34.0);
      bl.curveTo(cx - 22.0, cy + 18.0, cx - 26.0, cy + 44.0, cx - 4.0, cy + 38.0);
      bl.closePath();
      Path2D br = new java.awt.geom.Path2D.Double();
      br.moveTo(cx, cy + 34.0);
      br.curveTo(cx + 22.0, cy + 18.0, cx + 26.0, cy + 44.0, cx + 4.0, cy + 38.0);
      br.closePath();
      g.fill(bl);
      g.fill(br);
      g.fill(new Polygon(new int[]{(int)cx - 3, (int)cx - 12, (int)cx - 6}, new int[]{(int)cy + 36, (int)cy + 60, (int)cy + 58}, 3));
      g.fill(new Polygon(new int[]{(int)cx + 3, (int)cx + 12, (int)cx + 6}, new int[]{(int)cy + 36, (int)cy + 60, (int)cy + 58}, 3));
      g.setColor(new Color(16730706));
      g.fill(new Double(cx - 5.0, cy + 30.0, 10.0, 9.0));
   }

   static void sleighMoon(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      Capes.field(img, (u, v, xx, y) -> {
         int c = Capes.ramp(v, 395806, 1055812, 1977952, 2767472);
         double d = Math.hypot(xx - 80, (y - 92) * 1.0);
         return Capes.add(c, 6978240, Math.exp(-Math.pow(Math.max(0.0, d - 50.0) / 40.0, 2.0)) * 0.5);
      });
      stars(g, ph, 50, 721, 200.0);
      double mx = 80.0;
      double my = 92.0;
      double mr = 50.0;
      g.setPaint(
         new RadialGradientPaint(
            (float)(mx - 14.0),
            (float)(my - 16.0),
            (float)(mr * 1.3),
            new float[]{0.0F, 0.7F, 1.0F},
            new Color[]{new Color(16776424), new Color(15787712), new Color(14207128)}
         )
      );
      g.fill(new Double(mx - mr, my - mr, mr * 2.0, mr * 2.0));
      Random rn = new Random(722L);

      for (int i = 0; i < 14; i++) {
         double a = rn.nextDouble() * Math.PI * 2.0;
         double rr = Math.sqrt(rn.nextDouble()) * mr * 0.8;
         double cr = 2.0 + rn.nextDouble() * 7.0;
         double x = mx + Math.cos(a) * rr;
         double y = my + Math.sin(a) * rr;
         g.setColor(Capes.col(12101752, 0.45));
         g.fill(new Double(x - cr, y - cr, cr * 2.0, cr * 2.0));
         g.setColor(Capes.col(16777200, 0.35));
         g.fill(new Double(x - cr * 0.8, y - cr * 0.9, cr * 1.2, cr * 0.8));
      }

      double bob = Math.sin(ph) * 3.0;
      int sil = 790564;
      g.setColor(new Color(sil));

      for (int i = 0; i < 26; i++) {
         double f = i / 26.0;
         double x = 34.0 - f * 40.0;
         double y = 108.0 + bob + Math.sin(f * 6.0 - ph) * 4.0 + f * 18.0;
         Capes.star(g, x, y, 1.2 + (1.0 - f) * 1.4, 16769162, (1.0 - f) * (0.6 + 0.4 * Math.sin(ph * 2.0 + i)));
      }

      g.setColor(new Color(sil));
      double sx = 46.0;
      double sy = 104.0 + bob;
      Path2D sl = new java.awt.geom.Path2D.Double();
      sl.moveTo(sx - 16.0, sy - 12.0);
      sl.lineTo(sx - 14.0, sy + 2.0);
      sl.lineTo(sx + 10.0, sy + 2.0);
      sl.quadTo(sx + 16.0, sy - 2.0, sx + 14.0, sy - 8.0);
      sl.lineTo(sx + 8.0, sy - 6.0);
      sl.lineTo(sx - 8.0, sy - 6.0);
      sl.lineTo(sx - 10.0, sy - 14.0);
      sl.closePath();
      g.fill(sl);
      g.setStroke(new BasicStroke(1.8F, 1, 1));
      Path2D runner = new java.awt.geom.Path2D.Double();
      runner.moveTo(sx - 18.0, sy + 6.0);
      runner.lineTo(sx + 14.0, sy + 6.0);
      runner.quadTo(sx + 21.0, sy + 5.0, sx + 19.0, sy - 1.0);
      g.draw(runner);
      g.draw(new java.awt.geom.Line2D.Double(sx - 10.0, sy + 2.0, sx - 10.0, sy + 6.0));
      g.draw(new java.awt.geom.Line2D.Double(sx + 6.0, sy + 2.0, sx + 6.0, sy + 6.0));
      g.fill(new Double(sx - 4.0, sy - 16.0, 9.0, 11.0));
      g.fill(new Double(sx - 1.0, sy - 22.0, 6.0, 6.0));
      g.fill(new Double(sx - 15.0, sy - 20.0, 11.0, 12.0));
      g.setStroke(new BasicStroke(0.9F));
      g.draw(new java.awt.geom.Line2D.Double(sx + 4.0, sy - 12.0, sx + 30.0, sy - 12.0 + Math.sin(ph) * 1.0));

      for (int d = 0; d < 4; d++) {
         double rx = sx + 30.0 + d * 20;
         double ry = sy - 14.0 - d * 4 + Math.sin(ph + d * 0.9) * 2.5;
         reindeer(g, rx, ry, ph * 2.0 + d * 1.1, d == 3);
         if (d < 3) {
            g.setColor(new Color(sil));
            g.setStroke(new BasicStroke(0.9F));
            g.draw(new java.awt.geom.Line2D.Double(rx + 7.0, ry - 3.0, rx + 18.0, ry - 7.0 + Math.sin(ph + (d + 1) * 0.9) * 2.5 - Math.sin(ph + d * 0.9) * 2.5));
         }
      }

      Random tr = new Random(723L);
      double x = -4.0;
      int hi = 0;

      while (x < 160.0) {
         double w = 16.0 + tr.nextDouble() * 14.0;
         double h = 26.0 + tr.nextDouble() * 30.0;
         double bx = x;
         double by = 242.0 - h;
         g.setColor(new Color(Capes.mix(922672, 1712712, tr.nextDouble())));
         g.fill(new java.awt.geom.Rectangle2D.Double(x, by, w, h + 14.0));
         Path2D rf = new java.awt.geom.Path2D.Double();
         rf.moveTo(x - 2.0, by);
         rf.lineTo(x + w / 2.0, by - 10.0);
         rf.lineTo(x + w + 2.0, by);
         rf.closePath();
         g.fill(rf);
         g.setColor(new Color(15266047));
         Path2D sn = new java.awt.geom.Path2D.Double();
         sn.moveTo(x - 2.0, by);
         sn.lineTo(x + w / 2.0, by - 10.0);
         sn.lineTo(x + w + 2.0, by);
         sn.lineTo(x + w - 2.0, by + 1.5);
         sn.lineTo(x + w / 2.0, by - 7.0);
         sn.lineTo(x + 2.0, by + 1.5);
         sn.closePath();
         g.fill(sn);

         for (int wy = 0; wy < 3; wy++) {
            for (int wx = 0; wx < 2; wx++) {
               if (!(tr.nextDouble() < 0.35)) {
                  double on = 0.6 + 0.4 * Math.sin(ph * (1 + hi % 2) + hi * 1.7);
                  double lx = bx + 3.0 + wx * (w - 10.0) / 1.0 * 0.6;
                  double ly = by + 6.0 + wy * 10;
                  if (!(ly > 238.0)) {
                     g.setColor(new Color(Capes.mix(5913114, 16765562, on)));
                     g.fill(new java.awt.geom.Rectangle2D.Double(lx, ly, 4.0, 5.0));
                     hi++;
                  }
               }
            }
         }

         x += w + 1.0;
      }

      g.setPaint(new GradientPaint(0.0F, 240.0F, new Color(15266047), 0.0F, 256.0F, new Color(11583712)));
      g.fill(new java.awt.geom.Rectangle2D.Double(0.0, 240.0, 160.0, 16.0));
      snow(g, t, ph, 50, 724, 0.7);
   }

   static void reindeer(Graphics2D g, double x, double y, double a, boolean rudolph) {
      g.setColor(new Color(790564));
      g.fill(new Double(x - 7.0, y - 3.5, 14.0, 7.0));
      Path2D neck = new java.awt.geom.Path2D.Double();
      neck.moveTo(x + 4.0, y - 2.0);
      neck.lineTo(x + 8.0, y - 8.0);
      neck.lineTo(x + 10.0, y - 7.0);
      neck.lineTo(x + 7.0, y);
      neck.closePath();
      g.fill(neck);
      g.fill(new Double(x + 7.0, y - 10.0, 6.0, 4.0));
      g.setStroke(new BasicStroke(1.2F, 1, 1));
      g.draw(new java.awt.geom.Line2D.Double(x + 8.0, y - 10.0, x + 6.0, y - 15.0));
      g.draw(new java.awt.geom.Line2D.Double(x + 7.0, y - 13.0, x + 4.0, y - 14.0));
      g.draw(new java.awt.geom.Line2D.Double(x + 9.0, y - 10.0, x + 10.0, y - 15.0));
      g.draw(new java.awt.geom.Line2D.Double(x + 10.0, y - 13.0, x + 12.0, y - 15.0));
      double s = Math.sin(a);
      g.setStroke(new BasicStroke(1.4F, 1, 1));
      g.draw(new java.awt.geom.Line2D.Double(x + 4.0, y + 2.0, x + 8.0 + s * 2.0, y + 7.0));
      g.draw(new java.awt.geom.Line2D.Double(x + 3.0, y + 2.0, x + 6.0 - s * 2.0, y + 7.0));
      g.draw(new java.awt.geom.Line2D.Double(x - 5.0, y + 2.0, x - 9.0 - s * 2.0, y + 6.0));
      g.draw(new java.awt.geom.Line2D.Double(x - 4.0, y + 2.0, x - 7.0 + s * 2.0, y + 7.0));
      g.fill(new Double(x - 9.0, y - 4.0, 3.0, 3.0));
      if (rudolph) {
         g.setColor(new Color(16726586));
         g.fill(new Double(x + 11.5, y - 9.5, 2.6, 2.6));
         Capes.dot(g, x + 12.8, y - 8.2, 5.0, 16726586, 0.7);
      }
   }

   static void sweater(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      int cs = 5;
      int cols = 160 / cs;
      int rows = 256 / cs + 1;
      int[][] cell = new int[rows][cols];
      int R = 12063774;
      int W = 16184042;
      int G = 1997368;
      int Y = 16765498;
      int B = 6961690;

      for (int[] r : cell) {
         Arrays.fill(r, 12063774);
      }

      int y = 2;

      for (int x = 0; x < cols; x++) {
         cell[y][x] = 16184042;
         cell[y + 1][x] = x / 2 % 2 == 0 ? 1997368 : 16184042;
         cell[y + 2][x] = 16184042;
      }

      int var20 = 7;

      for (int x = 0; x < cols; x++) {
         int zz = Math.abs(x % 6 - 3);

         for (int d = 0; d < 4; d++) {
            if (d == zz) {
               cell[var20 + d][x] = 16184042;
            }
         }
      }

      var20 = 13;
      stamp(cell, DEER, 2, var20, 16184042, 1997368, 16765498, 6961690, k);
      stampMirror(cell, DEER, cols - 13, var20, 16184042, 1997368, 16765498, 6961690, k);
      var20 = 25;

      for (int x = 0; x < cols; x++) {
         cell[var20][x] = 1997368;
         cell[var20 + 1][x] = x % 4 == 1 ? 16184042 : 1997368;
         cell[var20 + 2][x] = 1997368;
      }

      var20 = 29;

      for (int i = 0; i < 3; i++) {
         stamp(cell, TREE, 1 + i * 11, var20, 16184042, 1997368, 16765498, 6961690, k + i);
      }

      var20 = 41;

      for (int x = 0; x < cols; x++) {
         cell[var20][x] = 16184042;
         cell[var20 + 1][x] = x / 2 % 2 == 0 ? 16184042 : 12063774;
      }

      var20 = 44;

      for (int i = 0; i < 4; i++) {
         stamp(cell, FLAKE, 1 + i * 8, var20, 16184042, 1997368, 16765498, 6961690, k);
      }

      for (int r = 0; r < rows; r++) {
         for (int c = 0; c < cols; c++) {
            knitStitch(g, c * cs, r * cs, cs, cell[r][c]);
         }
      }

      for (int r = 0; r < rows; r++) {
         for (int c = 0; c < cols; c++) {
            if (cell[r][c] == 16726586 || cell[r][c] == 16765498 && r > 28 && r < 40) {
               Capes.addDot(img, c * cs + 2.5, r * cs + 2.5, 5.0, cell[r][c], 0.25);
            }
         }
      }
   }

   private static void stamp(int[][] cell, String[] m, int x0, int y0, int W, int G, int Y, int B, int k) {
      for (int r = 0; r < m.length; r++) {
         for (int c = 0; c < m[r].length(); c++) {
            put(cell, x0 + c, y0 + r, m[r].charAt(c), W, G, Y, B, k + r * 3 + c);
         }
      }
   }

   private static void stampMirror(int[][] cell, String[] m, int x0, int y0, int W, int G, int Y, int B, int k) {
      for (int r = 0; r < m.length; r++) {
         for (int c = 0; c < m[r].length(); c++) {
            put(cell, x0 + m[r].length() - 1 - c, y0 + r, m[r].charAt(c), W, G, Y, B, k + r * 3 + c);
         }
      }
   }

   private static void put(int[][] cell, int x, int y, char ch, int W, int G, int Y, int B, int seed) {
      if (y >= 0 && y < cell.length && x >= 0 && x < cell[0].length) {
         switch (ch) {
            case 'B':
               cell[y][x] = B;
               break;
            case 'G':
               cell[y][x] = G;
               break;
            case 'K':
               cell[y][x] = 16726586;
               break;
            case 'L':
               int[] lc = new int[]{16765498, 16726586, 3844351, 16184042};
               cell[y][x] = lc[Math.floorMod(seed, 4)];
               break;
            case 'W':
               cell[y][x] = W;
               break;
            case 'Y':
               cell[y][x] = Y;
         }
      }
   }

   private static void knitStitch(Graphics2D g, double x, double y, double cs, int c) {
      g.setColor(new Color(Capes.shade(c, 0.62)));
      g.fill(new java.awt.geom.Rectangle2D.Double(x, y, cs, cs));

      for (int s = -1; s <= 1; s += 2) {
         AffineTransform o = g.getTransform();
         g.translate(x + cs / 2.0 + s * cs * 0.22, y + cs * 0.5);
         g.rotate(-s * 0.55);
         g.setPaint(
            new GradientPaint(0.0F, (float)(-cs * 0.6), new Color(Capes.mix(c, 16777215, 0.18)), 0.0F, (float)(cs * 0.6), new Color(Capes.shade(c, 0.85)))
         );
         g.fill(new Double(-cs * 0.2, -cs * 0.62, cs * 0.4, cs * 1.24));
         g.setTransform(o);
      }
   }

   static void auroraXmas(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      Capes.field(img, (u, v, x, y) -> {
         int cx = Capes.ramp(v, 198159, 463402, 926272, 1586264);

         for (int b = 0; b < 2; b++) {
            double edgeY = (b == 0 ? 0.42 : 0.3) + 0.07 * Math.sin(u * 7.0 + ph + b * 2) + 0.03 * Math.sin(u * 17.0 - ph * 2.0 + b);
            double dv = edgeY - v;
            if (!(dv < -0.02)) {
               double ray = 0.5 + 0.5 * Math.sin(u * 60.0 + Math.sin(u * 9.0 + ph) * 3.0 + b * 5);
               double fb = Capes.fbm(u * 8.0 + b * 3, t * 2.0, 8, 2, 3, 731 + b);
               double inten = Math.exp(-Math.max(0.0, dv) / (0.16 + 0.06 * fb)) * (0.35 + 0.65 * ray * fb) * Capes.smooth(-0.02, 0.01, dv);
               int ac = b == 0 ? Capes.mix(3866506, 11557631, Math.min(1.0, dv * 3.5)) : Capes.mix(3860696, 16734936, Math.min(1.0, dv * 4.0));
               cx = Capes.add(cx, ac, inten * (b == 0 ? 0.95 : 0.6));
            }
         }

         return cx;
      });
      stars(g, ph, 40, 732, 150.0);
      Path2D m1 = new java.awt.geom.Path2D.Double();
      m1.moveTo(0.0, 190.0);
      m1.lineTo(26.0, 150.0);
      m1.lineTo(48.0, 172.0);
      m1.lineTo(82.0, 132.0);
      m1.lineTo(116.0, 176.0);
      m1.lineTo(138.0, 156.0);
      m1.lineTo(160.0, 176.0);
      m1.lineTo(160.0, 256.0);
      m1.lineTo(0.0, 256.0);
      m1.closePath();
      g.setPaint(new GradientPaint(0.0F, 130.0F, new Color(2767450), 0.0F, 200.0F, new Color(1186350)));
      g.fill(m1);
      Path2D caps = new java.awt.geom.Path2D.Double();
      caps.moveTo(70.0, 147.0);
      caps.lineTo(82.0, 132.0);
      caps.lineTo(96.0, 150.0);
      caps.lineTo(88.0, 147.0);
      caps.lineTo(82.0, 152.0);
      caps.lineTo(76.0, 146.0);
      caps.closePath();
      caps.moveTo(18.0, 162.0);
      caps.lineTo(26.0, 150.0);
      caps.lineTo(34.0, 160.0);
      caps.lineTo(26.0, 158.0);
      caps.closePath();
      caps.moveTo(131.0, 164.0);
      caps.lineTo(138.0, 156.0);
      caps.lineTo(146.0, 162.0);
      caps.lineTo(139.0, 161.0);
      caps.closePath();
      g.setColor(new Color(14214392));
      g.fill(caps);
      Path2D hill = new java.awt.geom.Path2D.Double();
      hill.moveTo(0.0, 214.0);
      hill.quadTo(80.0, 188.0, 160.0, 214.0);
      hill.lineTo(160.0, 256.0);
      hill.lineTo(0.0, 256.0);
      hill.closePath();
      g.setPaint(new GradientPaint(0.0F, 190.0F, new Color(15266047), 0.0F, 256.0F, new Color(9087184)));
      g.fill(hill);
      Capes.addDot(img, 80.0, 206.0, 60.0, 3866506, 0.12);
      double tx = 80.0;
      double ty = 206.0;
      pine(g, tx, ty, 70.0, 1329194, 15791871, 5);
      Random rn = new Random(733L);
      int[] lc = new int[]{16726586, 16765498, 3844351, 16743136, 3860586};

      for (int i = 0; i < 18; i++) {
         double f = rn.nextDouble();
         double yy = ty - 10.0 - f * 50.0;
         double half = (1.0 - f) * 22.0 + 3.0;
         double xx = tx + (rn.nextDouble() * 2.0 - 1.0) * half * 0.85;
         double tw = 0.5 + 0.5 * Math.sin(ph * (1 + i % 2) + i * 2.1);
         int c = lc[i % lc.length];
         Capes.addDot(img, xx, yy, 5.0, c, 0.45 * tw);
         g.setColor(new Color(Capes.mix(4210752, c, 0.4 + 0.6 * tw)));
         g.fill(new Double(xx - 1.6, yy - 1.6, 3.2, 3.2));
      }

      Capes.addDot(img, tx, ty - 66.0, 14.0, 16769146, 0.6 + 0.2 * Math.sin(ph));
      g.setColor(new Color(16769146));
      g.fill(XmasTex.starShape(tx, ty - 66.0, 6.0, 2.6, 5));
      int[][] gifts = new int[][]{{-18, 15217210}, {12, 3836671}, {-6, 3061834}};

      for (int[] gf : gifts) {
         double gx = tx + gf[0];
         double gy = ty - 2.0;
         g.setColor(new Color(gf[1]));
         g.fill(new java.awt.geom.Rectangle2D.Double(gx, gy - 7.0, 9.0, 8.0));
         g.setColor(new Color(16769146));
         g.fill(new java.awt.geom.Rectangle2D.Double(gx + 3.8, gy - 7.0, 1.4, 8.0));
      }

      snow(g, t, ph, 45, 734, 0.6);
   }
}
