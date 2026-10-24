package dev.lego.cosmetic;

import dev.lego.ui.Fonts;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Line2D.Double;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

final class CapeArt2 {
   private CapeArt2() {
   }

   static void defineAll() {
      Capes.def("storm", 16, 10.0F, 13688063, 2765904, 922140, CapeArtHd::storm);
      Capes.def("rainbow", 16, 8.0F, 16777215, 10128072, 15128831, CapeArt2::rainbow);
      Capes.def("snow", 16, 8.0F, 16777215, 8166594, 1320522, CapeArt2::snow);
      Capes.def("fireflies", 16, 6.0F, 15265952, 3820058, 662034, CapeArtHd::fireflies);
      Capes.def("heart", 16, 12.0F, 16767176, 9054778, 3802132, CapeArt2::heart);
      Capes.def("enchant", 16, 10.0F, 15784191, 5909130, 1706554, CapeArt2::enchant);
      Capes.def("void", 16, 8.0F, 16762986, 3811856, 328456, CapeArt2::blackHole);
      Capes.def("royal", 1, 0.0F, 16771488, 10119698, 4851216, CapeArt2::royal);
      Capes.def("dragon", 1, 0.0F, 16769658, 8018450, 666138, CapeArtHd::scales);
      Capes.def("camo", 1, 0.0F, 11053176, 3815970, 2763290, CapeArtHd::camo);
      Capes.def("bricks", 1, 0.0F, 16769610, 10123776, 1710622, CapeArtHd::bricks);
      Capes.def("racing", 1, 0.0F, 16738922, 7997962, 1184274, CapeArtHd::racing);
      Capes.def("moon", 1, 0.0F, 15265023, 5925530, 659504, CapeArt2::moon);
   }

   private static double flash(int k) {
      switch (k) {
         case 3:
            return 1.0;
         case 4:
            return 0.45;
         case 5:
            return 0.8;
         case 6:
            return 0.25;
         case 7:
         case 8:
         case 9:
         case 10:
         default:
            return 0.0;
         case 11:
            return 0.7;
         case 12:
            return 0.3;
      }
   }

   static void storm(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      double fl = flash(k);
      boolean boltA = k >= 3 && k <= 6;
      double bx = boltA ? 64.0 : 110.0;
      Capes.field(img, (u, v, xx, yx) -> {
         double w = Capes.fbm(u * 3.0 + t * 3.0, v * 4.0, 3, 0, 3, 40);
         double c1 = Capes.fbm(u * 3.0 + t * 3.0 + (w - 0.5) * 0.8, v * 4.0 + (w - 0.5), 3, 0, 5, 41);
         double dens = Capes.smooth(0.2, 0.75, c1 + (0.45 - v) * 1.1);
         int sky = Capes.ramp(v, 1712692, 1054239, 461071);
         int cloud = Capes.ramp(c1, 658708, 1975864, 4082278, 6977686);
         int c = Capes.mix(sky, cloud, dens);
         double lit = fl * (0.25 + 0.75 * c1) * Math.exp(-Math.hypot(xx - bx, yx - 40) / 90.0);
         c = Capes.add(c, 12572927, lit * 0.9 + fl * 0.08);
         double hill = 0.9 + 0.03 * Math.sin(u * 8.0 + 1.0) + 0.015 * Math.sin(u * 23.0);
         if (v > hill) {
            c = Capes.add(Capes.mix(329483, 131844, (v - hill) * 8.0), 9087231, fl * 0.12);
         }

         return c;
      });
      Random rn = new Random(71L);
      g.setStroke(new BasicStroke(1.0F, 1, 1));

      for (int i = 0; i < 150; i++) {
         double x0 = rn.nextDouble();
         double y0 = rn.nextDouble();
         int j = 2 + rn.nextInt(2);
         double y = (y0 + j * t) % 1.0 * 276.0 - 10.0;
         double x = x0 * 190.0 - 15.0 + y * 0.12;
         double len = 7.0 + rn.nextDouble() * 7.0;
         g.setColor(Capes.col(11059432, 0.18 + 0.2 * rn.nextDouble() + fl * 0.2));
         g.draw(new Double(x, y, x + len * 0.12, y + len));
      }

      if (fl > 0.0) {
         Random br = new Random(boltA ? 5L : 9L);
         Path2D p = new java.awt.geom.Path2D.Double();
         double x = bx;
         double y = 30.0;
         p.moveTo(bx, y);
         List<double[]> forks = new ArrayList<>();

         while (y < 230.4) {
            x += br.nextGaussian() * 7.0;
            y += 8.0 + br.nextDouble() * 10.0;
            p.lineTo(x, y);
            if (br.nextDouble() < 0.18) {
               forks.add(new double[]{x, y});
            }
         }

         for (double[] f : forks) {
            double fx = f[0];
            double fy = f[1];
            p.moveTo(fx, fy);
            double dir = br.nextBoolean() ? 1.0 : -1.0;

            for (int s = 0; s < 4; s++) {
               fx += dir * (4.0 + br.nextDouble() * 8.0);
               fy += 6.0 + br.nextDouble() * 8.0;
               p.lineTo(fx, fy);
            }
         }

         Capes.addDot(img, bx, 40.0, 50.0, 10471679, 0.5 * fl);
         Capes.glowStroke(g, p, 11064575, 2.6F, 3.2F, fl);
      }
   }

   static void rainbow(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double hueOff = (double)k / n;
      Capes.field(img, (u, v, xx, yx) -> {
         double w = 0.07 * Math.sin(v * Math.PI * 3.0 + ph) + 0.05 * Math.sin(u * Math.PI * 2.0 + v * Math.PI - ph);
         double hue = u * 0.22 + v * 0.62 + w + hueOff;
         double f = Math.sin((v * 2.6 + u * 1.1 + w * 2.2) * Math.PI * 2.0);
         double shadeV = 0.86 + 0.14 * f;
         int c = Capes.hsv(hue, 0.6, 0.97 * shadeV);
         c = Capes.add(c, 16777215, Math.pow(Math.max(0.0, f), 14.0) * 0.4);
         return Capes.mix(c, 16777215, 0.08);
      });
      double cx = 80.0;
      double cy = 112.0;
      Capes.addDot(img, cx, cy, 48.0, 16777215, 0.45);
      Path2D st = new java.awt.geom.Path2D.Double();
      double R = 34.0;
      double r = 7.0;

      for (int i = 0; i < 8; i++) {
         double a = (-Math.PI / 2) + i * Math.PI / 4.0;
         double rr = i % 2 == 0 ? R : r;
         if (i == 0) {
            st.moveTo(cx + Math.cos(a) * rr, cy + Math.sin(a) * rr);
         } else {
            st.quadTo(cx + Math.cos(a - (Math.PI / 8)) * r * 0.6, cy + Math.sin(a - (Math.PI / 8)) * r * 0.6, cx + Math.cos(a) * rr, cy + Math.sin(a) * rr);
         }
      }

      st.quadTo(cx + Math.cos(-Math.PI * 5.0 / 8.0) * r * 0.6, cy + Math.sin(-Math.PI * 5.0 / 8.0) * r * 0.6, cx, cy - R);
      g.setColor(Capes.col(6965928, 0.25));
      g.fill(AffineTransform.getTranslateInstance(2.0, 3.0).createTransformedShape(st));
      g.setPaint(
         new RadialGradientPaint(
            (float)cx, (float)cy, (float)R, new float[]{0.0F, 0.5F, 1.0F}, new Color[]{Color.WHITE, new Color(16774911), new Color(15259903)}
         )
      );
      g.fill(st);
      Random rn = new Random(3L);

      for (int ix = 0; ix < 34; ix++) {
         double x = 12.0 + rn.nextDouble() * 136.0;
         double y = 12.0 + rn.nextDouble() * 232.0;
         double a = Math.pow(Math.max(0.0, Math.sin(ph * (1 + rn.nextInt(2)) + rn.nextDouble() * 6.28)), 2.0);
         Capes.star(g, x, y, 1.0 + rn.nextDouble() * 1.8, 16777215, a);
      }
   }

   private static void pine(Graphics2D g, double x, double base, double h, int col, double snow) {
      double w = h * 0.42;
      g.setColor(new Color(Capes.mix(col, 0, 0.3)));
      g.fill(new java.awt.geom.Rectangle2D.Double(x - h * 0.03, base - h * 0.12, h * 0.06, h * 0.14));

      for (int i = 0; i < 4; i++) {
         double ty = base - h * 0.1 - i * h * 0.22;
         double tw = w * (1.0 - i * 0.22);
         Path2D p = new java.awt.geom.Path2D.Double();
         p.moveTo(x, ty - h * 0.34);
         p.lineTo(x + tw / 2.0, ty);
         p.quadTo(x, ty - h * 0.05, x - tw / 2.0, ty);
         p.closePath();
         g.setColor(new Color(col));
         g.fill(p);
         Path2D s = new java.awt.geom.Path2D.Double();
         s.moveTo(x, ty - h * 0.34);
         s.lineTo(x + tw * 0.28, ty - h * 0.12);
         s.quadTo(x, ty - h * 0.2, x - tw * 0.28, ty - h * 0.12);
         s.closePath();
         g.setColor(Capes.col(15398143, snow));
         g.fill(s);
      }
   }

   static void snow(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      Capes.field(img, (u, v, xx, yx) -> {
         int c = Capes.ramp(v / 0.75, 397354, 926542, 2508668, 6192818);
         c = Capes.add(c, 13623551, Math.exp(-Math.hypot(xx - 116, yx - 50) / 22.0) * 0.45);
         double h1 = 0.62 + 0.04 * Math.sin(u * 5.0 + 1.0) + 0.02 * Math.sin(u * 13.0);
         if (v > h1) {
            c = Capes.mix(10270428, 6191784, (v - h1) * 5.0);
         }

         double h2 = 0.82 + 0.025 * Math.sin(u * 4.0 + 2.0);
         if (v > h2) {
            c = Capes.mix(15791871, 11058912, (v - h2) * 4.0);
            if (Capes.hash(xx, yx, 7) > 0.985) {
               c = Capes.add(c, 16777215, 0.5);
            }
         }

         return c;
      });
      g.setPaint(new RadialGradientPaint(112.0F, 46.0F, 16.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(16776688), new Color(15262928)}));
      g.fill(new java.awt.geom.Ellipse2D.Double(104.0, 38.0, 24.0, 24.0));
      g.setColor(Capes.col(12104864, 0.35));
      g.fill(new java.awt.geom.Ellipse2D.Double(110.0, 44.0, 6.0, 5.0));
      g.fill(new java.awt.geom.Ellipse2D.Double(118.0, 52.0, 4.0, 4.0));
      Random rn = new Random(6L);

      for (int i = 0; i < 40; i++) {
         Capes.star(g, rn.nextDouble() * 160.0, rn.nextDouble() * 256.0 * 0.5, 0.5 + rn.nextDouble(), 14543103, 0.3 + 0.5 * (0.5 + 0.5 * Math.sin(ph + i)));
      }

      for (int i = 0; i < 9; i++) {
         double x = 8 + i * 18 + rn.nextDouble() * 6.0;
         double base = 256.0 * (0.64 + 0.04 * Math.sin(x / 160.0 * 5.0 + 1.0)) + 2.0;
         pine(g, x, base, 18.0 + rn.nextDouble() * 10.0, 1716818, 0.55);
      }

      double[] big = new double[]{18.0, 52.0, 132.0, 150.0};

      for (double x : big) {
         pine(g, x, 220.16, 52.0 + rn.nextDouble() * 20.0, 793648, 0.9);
      }

      int[][] layers = new int[][]{{50, 1}, {34, 1}, {14, 2}};
      double[][] rr = new double[][]{{0.8, 1.4}, {1.6, 2.4}, {3.0, 4.2}};

      for (int L = 0; L < 3; L++) {
         for (int i = 0; i < layers[L][0]; i++) {
            double x0 = rn.nextDouble() * 160.0;
            double y0 = rn.nextDouble();
            double s = rn.nextDouble() * 6.28;
            double y = (y0 + layers[L][1] * t) % 1.0 * 268.0 - 6.0;
            double x = x0 + Math.sin(ph + s) * (4 + L * 2);
            double r = rr[L][0] + rn.nextDouble() * (rr[L][1] - rr[L][0]);
            Capes.dot(g, x, y, r * 1.6, 16777215, L == 2 ? 0.7 : 0.9);
         }
      }
   }

   static void fireflies(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      Capes.field(img, (u, v, xx, yx) -> {
         int c = Capes.ramp(v, 133642, 401436, 932398, 666142);
         c = Capes.add(c, 10152144, Math.exp(-Math.hypot(u - 0.5, (v - 0.12) * 0.8) / 0.18) * 0.28);
         double fog = Capes.fbm(u * 3.0 + t * 3.0, v * 5.0, 3, 0, 4, 55) * Math.exp(-Math.pow((v - 0.72) / 0.12, 2.0));
         return Capes.add(c, 6989978, fog * 0.35);
      });
      Random rn = new Random(15L);
      int[] lc = new int[]{997938, 534048, 201232};

      for (int L = 0; L < 3; L++) {
         int cnt = 5 - L;

         for (int i = 0; i < cnt; i++) {
            double x = rn.nextDouble() * 160.0;
            double w = 5 + L * 5 + rn.nextDouble() * 4.0;
            g.setPaint(
               new GradientPaint((float)(x - w), 0.0F, new Color(Capes.mix(lc[L], 0, 0.2)), (float)(x + w), 0.0F, new Color(Capes.mix(lc[L], 3828314, 0.25)))
            );
            Path2D tr = new java.awt.geom.Path2D.Double();
            tr.moveTo(x - w / 2.0, 0.0);
            tr.lineTo(x + w / 2.0, 0.0);
            tr.lineTo(x + w * 0.7, 256.0);
            tr.lineTo(x - w * 0.7, 256.0);
            tr.closePath();
            g.fill(tr);
         }

         g.setColor(new Color(lc[L]));

         for (int i = 0; i < 16; i++) {
            double x = rn.nextDouble() * 160.0;
            double y = -10.0 + rn.nextDouble() * (30 + L * 10);
            double r = 14.0 + rn.nextDouble() * 14.0;
            g.fill(new java.awt.geom.Ellipse2D.Double(x - r, y - r * 0.7, r * 2.0, r * 1.4));
         }
      }

      g.setStroke(new BasicStroke(1.4F, 1, 1));

      for (int i = 0; i < 140; i++) {
         double x = rn.nextDouble() * 160.0;
         double h = 10.0 + rn.nextDouble() * 22.0;
         double bend = rn.nextGaussian() * 5.0 + Math.sin(ph + x * 0.05) * 1.5;
         g.setColor(new Color(Capes.mix(200712, 1985066, rn.nextDouble() * 0.6)));
         g.draw(new java.awt.geom.QuadCurve2D.Double(x, 256.0, x + bend * 0.3, 256.0 - h * 0.6, x + bend, 256.0 - h));
      }

      for (int i = 0; i < 26; i++) {
         double x0 = 12.0 + rn.nextDouble() * 136.0;
         double y0 = 50.0 + rn.nextDouble() * 190.0;
         int j = 1 + rn.nextInt(2);
         double s1 = rn.nextDouble() * 6.28;
         double s2 = rn.nextDouble() * 6.28;
         double s3 = rn.nextDouble() * 6.28;
         double x = x0 + 8.0 * Math.cos(ph * j + s1) + 3.0 * Math.sin(2.0 * ph + s2);
         double y = y0 + 6.0 * Math.sin(ph * j + s3);
         double blink = Math.pow(0.5 + 0.5 * Math.sin(ph * (1 + rn.nextInt(2)) + s2), 2.2);
         Capes.addDot(img, x, y, 10.0, 12123978, 0.45 * blink + 0.04);
         Capes.addDot(img, x, y, 2.2, 16777168, 0.95 * blink + 0.1);
      }
   }

   private static double beat(double t) {
      double b = 0.0;

      for (int w = -1; w <= 1; w++) {
         double tt = t + w;
         b += Math.exp(-Math.pow((tt - 0.08) / 0.05, 2.0)) + 0.6 * Math.exp(-Math.pow((tt - 0.3) / 0.06, 2.0));
      }

      return b;
   }

   static Path2D heartPath(double cx, double cy, double s) {
      Path2D h = new java.awt.geom.Path2D.Double();
      h.moveTo(cx, cy + s * 0.42);
      h.curveTo(cx - s * 0.08, cy + s * 0.33, cx - s * 0.5, cy + s * 0.05, cx - s * 0.5, cy - s * 0.18);
      h.curveTo(cx - s * 0.5, cy - s * 0.4, cx - s * 0.28, cy - s * 0.5, cx - s * 0.16, cy - s * 0.5);
      h.curveTo(cx - s * 0.06, cy - s * 0.5, cx, cy - s * 0.42, cx, cy - s * 0.32);
      h.curveTo(cx, cy - s * 0.42, cx + s * 0.06, cy - s * 0.5, cx + s * 0.16, cy - s * 0.5);
      h.curveTo(cx + s * 0.28, cy - s * 0.5, cx + s * 0.5, cy - s * 0.4, cx + s * 0.5, cy - s * 0.18);
      h.curveTo(cx + s * 0.5, cy + s * 0.05, cx + s * 0.08, cy + s * 0.33, cx, cy + s * 0.42);
      h.closePath();
      return h;
   }

   static void heart(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      double b = beat(t);
      Capes.field(img, (u, v, xx, yx) -> {
         double d = Math.hypot(xx - 80, (yx - 104) * 0.8);
         int c = Capes.mix(10489914, 2753038, d / 150.0);
         return Capes.add(c, 16730746, Math.exp(-(d / 55.0) * (d / 55.0)) * 0.3 * b);
      });
      Random rn = new Random(8L);

      for (int i = 0; i < 18; i++) {
         double x = rn.nextDouble() * 160.0;
         double y0 = rn.nextDouble();
         double y = ((y0 - t) % 1.0 + 1.0) % 1.0 * 276.0 - 10.0;
         double s = 7.0 + rn.nextDouble() * 8.0;
         g.setColor(Capes.col(16743066, 0.12 + 0.18 * rn.nextDouble()));
         g.fill(heartPath(x + Math.sin(ph + i) * 4.0, y, s));
      }

      double s = 84.0 * (1.0 + 0.1 * b);
      double cx = 80.0;
      double cy = 106.0;
      Capes.addDot(img, cx, cy, 58.0 * (1.0 + 0.1 * b), 16722522, 0.25 + 0.35 * b);
      Path2D h = heartPath(cx, cy, s);
      g.setColor(Capes.col(2752520, 0.45));
      g.fill(heartPath(cx + 2.0, cy + 4.0, s));
      g.setPaint(
         new RadialGradientPaint(
            (float)(cx - s * 0.18),
            (float)(cy - s * 0.2),
            (float)(s * 0.75),
            new float[]{0.0F, 0.45F, 1.0F},
            new Color[]{new Color(16751280), new Color(15736910), new Color(9046054)}
         )
      );
      g.fill(h);
      g.setColor(Capes.col(5899288, 0.6));
      g.setStroke(new BasicStroke(1.4F));
      g.draw(h);
      AffineTransform o = g.getTransform();
      g.translate(cx - s * 0.24, cy - s * 0.26);
      g.rotate(-0.6);
      g.setPaint(new GradientPaint(0.0F, (float)(-s * 0.08), Capes.col(16777215, 0.75), 0.0F, (float)(s * 0.08), Capes.col(16777215, 0.0)));
      g.fill(new java.awt.geom.Ellipse2D.Double(-s * 0.13, -s * 0.07, s * 0.26, s * 0.14));
      g.setTransform(o);
      double ey = 204.0;
      double head = t * 200.0 - 20.0;
      double prevX = 0.0;
      double prevY = ey;

      for (int x = 0; x <= 160; x += 2) {
         double ph2 = (x % 80 + 80) % 80 / 80.0;
         double yv = ey;
         if (ph2 > 0.3 && ph2 < 0.36) {
            yv = ey - Math.sin((ph2 - 0.3) / 0.06 * Math.PI) * 4.0;
         } else if (ph2 > 0.42 && ph2 < 0.46) {
            yv = ey + (ph2 - 0.42) / 0.04 * 5.0;
         } else if (ph2 >= 0.46 && ph2 < 0.5) {
            yv = ey + 5.0 - (ph2 - 0.46) / 0.04 * 32.0;
         } else if (ph2 >= 0.5 && ph2 < 0.55) {
            yv = ey - 27.0 + (ph2 - 0.5) / 0.05 * 35.0;
         } else if (ph2 >= 0.55 && ph2 < 0.58) {
            yv = ey + 8.0 - (ph2 - 0.55) / 0.03 * 8.0;
         } else if (ph2 > 0.66 && ph2 < 0.78) {
            yv = ey - Math.sin((ph2 - 0.66) / 0.12 * Math.PI) * 6.0;
         }

         double behind = head - x;
         if (behind < 0.0) {
            behind += 200.0;
         }

         double a = Math.exp(-behind / 70.0);
         if (x > 0) {
            Line2D ln = new Double(prevX, prevY, x, yv);
            g.setColor(Capes.col(16738954, a * 0.35));
            g.setStroke(new BasicStroke(4.0F, 1, 1));
            g.draw(ln);
            g.setColor(Capes.col(16769256, a));
            g.setStroke(new BasicStroke(1.5F, 1, 1));
            g.draw(ln);
         }

         if (Math.abs(x - head) < 1.5) {
            Capes.addDot(img, x, yv, 7.0, 16777215, 0.7);
         }

         prevX = x;
         prevY = yv;
      }
   }

   static void enchant(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      Capes.field(img, (ux, vx, xx, yx) -> {
         int c = Capes.ramp(vx, 3019362, 1968710, 918826);
         c = Capes.shade(c, 0.88 + 0.24 * Capes.fbm(ux * 10.0, vx * 16.0, 0, 0, 4, 12));
         return Capes.add(c, 9063167, Math.exp(-Math.hypot(ux - 0.5, (vx - 0.42) * 0.7) / 0.2) * 0.3);
      });
      double cx = 80.0;
      double cy = 108.0;
      double R = 54.0;
      int vio = 13142783;
      Capes.glowStroke(g, new java.awt.geom.Ellipse2D.Double(cx - R, cy - R, R * 2.0, R * 2.0), vio, 1.6F, 1.6F, 0.9);
      Capes.glowStroke(g, new java.awt.geom.Ellipse2D.Double(cx - R + 11.0, cy - R + 11.0, (R - 11.0) * 2.0, (R - 11.0) * 2.0), vio, 1.2F, 1.2F, 0.8);
      Random rn = new Random(77L);
      int runes = 16;

      for (int i = 0; i < runes; i++) {
         double a = (Math.PI * 2) * i / runes;
         double br = 0.45 + 0.55 * Math.pow(0.5 + 0.5 * Math.sin(ph - a * 2.0), 2.0);
         AffineTransform o = g.getTransform();
         g.translate(cx + Math.cos(a) * (R - 5.5), cy + Math.sin(a) * (R - 5.5));
         g.rotate(a + (Math.PI / 2));
         Path2D r = new java.awt.geom.Path2D.Double();

         for (int s = 0; s < 3; s++) {
            double x1 = rn.nextInt(3) * 2.2 - 2.2;
            double y1 = rn.nextInt(3) * 2.4 - 2.4;
            double x2 = rn.nextInt(3) * 2.2 - 2.2;
            double y2 = rn.nextInt(3) * 2.4 - 2.4;
            if (x1 == x2 && y1 == y2) {
               y2 += 2.4;
            }

            r.moveTo(x1, y1);
            r.lineTo(x2, y2);
         }

         Capes.glowStroke(g, r, 15255807, 0.9F, 0.8F, br);
         g.setTransform(o);
      }

      double al = t * Math.PI / 3.0;

      for (int tri = 0; tri < 2; tri++) {
         Path2D p = new java.awt.geom.Path2D.Double();

         for (int i = 0; i < 3; i++) {
            double a = al + tri * Math.PI / 3.0 + i * Math.PI * 2.0 / 3.0 - (Math.PI / 2);
            double px = cx + Math.cos(a) * (R - 12.0);
            double py = cy + Math.sin(a) * (R - 12.0);
            if (i == 0) {
               p.moveTo(px, py);
            } else {
               p.lineTo(px, py);
            }
         }

         p.closePath();
         Capes.glowStroke(g, p, 11037439, 1.1F, 1.3F, 0.75);
      }

      double r2 = R * 0.32;

      for (int ix = 0; ix < 12; ix++) {
         double a = -t * Math.PI / 6.0 + ix * Math.PI / 6.0;
         Capes.dot(g, cx + Math.cos(a) * r2, cy + Math.sin(a) * r2, 2.6, 15784191, 0.9);
      }

      Capes.addDot(img, cx, cy, 16.0, 13142783, 0.7 + 0.3 * Math.sin(ph));
      Capes.addDot(img, cx, cy, 5.0, 16777215, 0.9);

      for (int ix = 0; ix < 20; ix++) {
         double x = rn.nextDouble() * 160.0;
         double y0 = rn.nextDouble();
         double y = ((y0 - t) % 1.0 + 1.0) % 1.0 * 256.0;
         Capes.addDot(img, x + Math.sin(ph + ix) * 3.0, y, 3.0, 14199039, 0.6 * Math.sin(Math.PI * y / 256.0));
      }

      int[] p = Capes.px(img);

      for (int y = 0; y < 256; y++) {
         for (int x = 0; x < 160; x++) {
            double u = (x + 0.5) / 160.0;
            double v = (y + 0.5) / 256.0;
            double gl = Math.pow(0.5 + 0.5 * Math.sin((u * 1.1 + v * 0.55) * Math.PI * 2.0 * 2.5 - ph), 7.0) * 0.32;
            p[y * 160 + x] = Capes.add(p[y * 160 + x], 10509055, gl);
         }
      }
   }

   static void blackHole(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      double cx = 80.0;
      double cy = 112.0;
      double Rh = 21.0;
      Capes.field(img, (u, v, x, y) -> {
         double dx = x + 0.5 - 80.0;
         double dy = y + 0.5 - 112.0;
         double rs = Math.hypot(dx, dy);
         double lens = rs > 1.0 ? 617.4 / (rs * rs) : 0.0;
         int sx = (int)Math.round(x + dx * lens);
         int sy = (int)Math.round(y + dy * lens);
         int c = Capes.mix(262666, 656408, v);
         c = Capes.add(c, 5909146, Math.pow(Capes.fbm(sx / 50.0, sy / 50.0, 0, 0, 4, 3), 3.0) * 0.6);
         double sh = Capes.hash(sx >> 1, sy >> 1, 17);
         if (sh > 0.985) {
            c = Capes.add(c, 16777215, (sh - 0.985) * 60.0);
         }

         c = Capes.add(c, 16747066, Math.exp(-rs / 55.0) * 0.2);
         double ddy = dy / 0.26;
         double r = Math.hypot(dx, ddy);
         double th = Math.atan2(ddy, dx);
         double prof = Capes.smooth(24.0, 31.0, r) * (1.0 - Capes.smooth(39.0, 74.0, r));
         double nz = Capes.fbm(r * 0.09, th / (Math.PI * 2) * 16.0 + t * 32.0, 0, 16, 4, 29);
         double dop = 0.8 + 0.4 * -Math.cos(th);
         double heat = prof * (0.45 + 0.8 * nz) * dop * (1.25 - (r - 21.0) / 70.0);
         int dc = Capes.ramp(heat, 0, 6950912, 15222796, 16754760, 16774360);
         boolean front = dy > 0.0;
         if (!front && rs > 21.0) {
            c = Capes.add(c, dc, Capes.clamp01(heat * 1.4));
         }

         double arc = Math.exp(-Math.pow((rs - 26.0) / 3.5, 2.0)) * (dy < 4.0 ? 1.0 : 0.35);
         double nz2 = Capes.fbm(Math.atan2(dy, dx) / (Math.PI * 2) * 16.0 - t * 32.0, rs * 0.2, 16, 0, 3, 31);
         c = Capes.add(c, Capes.ramp(0.6 + 0.4 * nz2, 6950912, 16754760, 16774360), arc * (0.5 + 0.5 * nz2));
         if (rs < 21.0) {
            c = Capes.mix(c, 0, Capes.smooth(21.0, 19.5, rs));
         }

         if (front) {
            c = Capes.add(c, dc, Capes.clamp01(heat * 1.4));
         }

         return Capes.add(c, 16771264, Math.exp(-Math.pow((rs - 21.0 - 0.8) / 1.1, 2.0)) * 0.9);
      });
   }

   static void royal(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      Capes.field(img, (u, v, xx, yx) -> {
         double nn = Capes.fbm(u * 3.0, v * 2.5, 0, 0, 4, 90);
         int c = Capes.mix(9047074, 3801612, v * 0.7 + 0.3 * (1.0 - nn));
         double sheen = Math.pow(0.5 + 0.5 * Math.sin(u * Math.PI * 5.0 + nn * 3.0), 3.0) * 0.12;
         return Capes.add(c, 16738938, sheen);
      });
      g.setColor(Capes.col(16765562, 0.18));

      for (int row = 0; row < 7; row++) {
         for (int col = -1; col < 5; col++) {
            double x = col * 40 + row % 2 * 20 + 20;
            double y = 60 + row * 34;
            fleur(g, x, y, 9.0);
         }
      }

      for (int y = 12; y < 44; y++) {
         for (int x = 0; x < 160; x++) {
            double edge = 40.0 + 3.0 * Math.sin(x * 0.4) + 2.0 * Capes.hash(x, 3, 5);
            if (!(y > edge)) {
               double f = 0.9 + 0.1 * Capes.hash(x, y, 8) + 0.05 * Math.sin(x * 0.9 + y * 0.3);
               Capes.px(img)[y * 160 + x] = 0xFF000000 | Capes.shade(Capes.mix(16777215, 14210252, (y - 12) / 32.0), f);
            }
         }
      }

      for (int i = 0; i < 7; i++) {
         double xx = 14 + i * 22;
         double y = 24 + i % 2 * 8;
         Path2D tail = new java.awt.geom.Path2D.Double();
         tail.moveTo(xx, y - 4.0);
         tail.quadTo(xx + 3.0, y + 2.0, xx, y + 6.0);
         tail.quadTo(xx - 3.0, y + 2.0, xx, y - 4.0);
         g.setColor(new Color(1118481));
         g.fill(tail);
      }

      double cx = 80.0;
      double cy = 136.0;
      Path2D sh = new java.awt.geom.Path2D.Double();
      sh.moveTo(cx - 28.0, cy - 30.0);
      sh.lineTo(cx + 28.0, cy - 30.0);
      sh.lineTo(cx + 28.0, cy + 2.0);
      sh.curveTo(cx + 28.0, cy + 22.0, cx + 10.0, cy + 32.0, cx, cy + 40.0);
      sh.curveTo(cx - 10.0, cy + 32.0, cx - 28.0, cy + 22.0, cx - 28.0, cy + 2.0);
      sh.closePath();
      g.setColor(Capes.col(1703940, 0.5));
      g.fill(AffineTransform.getTranslateInstance(2.0, 4.0).createTransformedShape(sh));
      g.setPaint(gold(cx, cy - 30.0, cy + 40.0));
      g.fill(sh);
      Shape inner = AffineTransform.getTranslateInstance(cx, cy + 4.0)
         .createTransformedShape(
            AffineTransform.getScaleInstance(0.82, 0.82)
               .createTransformedShape(AffineTransform.getTranslateInstance(-cx, -cy - 4.0).createTransformedShape(sh))
         );
      g.setPaint(new GradientPaint(0.0F, (float)cy - 26.0F, new Color(2767514), 0.0F, (float)cy + 36.0F, new Color(922704)));
      g.fill(inner);
      g.setFont(Fonts.get(3, 34.0F));
      float w = g.getFontMetrics().stringWidth("L");
      g.setPaint(gold(cx, cy - 20.0, cy + 14.0));
      g.drawString("L", (float)(cx - w / 2.0F), (float)cy + 14.0F);
      Path2D cr = new java.awt.geom.Path2D.Double();
      double by = cy - 36.0;
      cr.moveTo(cx - 24.0, by);
      cr.lineTo(cx - 28.0, by - 22.0);
      cr.lineTo(cx - 13.0, by - 10.0);
      cr.lineTo(cx, by - 28.0);
      cr.lineTo(cx + 13.0, by - 10.0);
      cr.lineTo(cx + 28.0, by - 22.0);
      cr.lineTo(cx + 24.0, by);
      cr.closePath();
      g.setPaint(gold(cx, by - 28.0, by));
      g.fill(cr);
      g.setColor(Capes.col(5913088, 0.6));
      g.setStroke(new BasicStroke(1.0F));
      g.draw(cr);
      int[] gems = new int[]{14684511, 2003199, 14684511};
      double[][] gp = new double[][]{{cx - 28.0, by - 22.0}, {cx, by - 28.0}, {cx + 28.0, by - 22.0}};

      for (int i = 0; i < 3; i++) {
         g.setPaint(
            new RadialGradientPaint((float)gp[i][0] - 1.0F, (float)gp[i][1] - 1.0F, 4.0F, new float[]{0.0F, 1.0F}, new Color[]{Color.WHITE, new Color(gems[i])})
         );
         g.fill(new java.awt.geom.Ellipse2D.Double(gp[i][0] - 3.5, gp[i][1] - 3.5, 7.0, 7.0));
      }

      g.setColor(new Color(2003199));
      g.fill(new java.awt.geom.Ellipse2D.Double(cx - 3.0, by - 7.0, 6.0, 5.0));
   }

   static LinearGradientPaint gold(double cx, double y0, double y1) {
      return new LinearGradientPaint(
         (float)cx - 10.0F,
         (float)y0,
         (float)cx + 10.0F,
         (float)y1,
         new float[]{0.0F, 0.4F, 0.55F, 1.0F},
         new Color[]{new Color(16774328), new Color(15251756), new Color(11565586), new Color(16767082)}
      );
   }

   private static void fleur(Graphics2D g, double x, double y, double s) {
      Path2D p = new java.awt.geom.Path2D.Double();
      p.moveTo(x, y - s * 1.6);
      p.curveTo(x + s * 0.6, y - s * 0.8, x + s * 0.4, y - s * 0.2, x, y + s * 0.2);
      p.curveTo(x - s * 0.4, y - s * 0.2, x - s * 0.6, y - s * 0.8, x, y - s * 1.6);
      p.moveTo(x + s * 0.2, y);
      p.curveTo(x + s * 1.6, y - s * 1.2, x + s * 1.8, y + s * 0.2, x + s * 0.9, y + s * 0.3);
      p.curveTo(x + s * 1.1, y - s * 0.3, x + s * 0.7, y - s * 0.4, x + s * 0.2, y);
      p.moveTo(x - s * 0.2, y);
      p.curveTo(x - s * 1.6, y - s * 1.2, x - s * 1.8, y + s * 0.2, x - s * 0.9, y + s * 0.3);
      p.curveTo(x - s * 1.1, y - s * 0.3, x - s * 0.7, y - s * 0.4, x - s * 0.2, y);
      g.fill(p);
      g.fill(new java.awt.geom.RoundRectangle2D.Double(x - s * 0.8, y + s * 0.15, s * 1.6, s * 0.35, 2.0, 2.0));
      p = new java.awt.geom.Path2D.Double();
      p.moveTo(x - s * 0.25, y + s * 0.5);
      p.lineTo(x + s * 0.25, y + s * 0.5);
      p.lineTo(x, y + s * 1.3);
      p.closePath();
      g.fill(p);
   }

   static void scales(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double SW = 22.0;
      double SH = 12.0;
      double R = 14.0;
      Capes.field(img, (u, v, x, y) -> {
         int r0 = (int)Math.floor((y - 14.0) / 12.0) - 1;

         for (int r = r0; r <= r0 + 3; r++) {
            double cyr = r * 12.0;
            double off = Math.floorMod(r, 2) * 22.0 / 2.0;
            int ci = (int)Math.floor((x - off) / 22.0 + 0.5);

            for (int cc = ci - 1; cc <= ci + 1; cc++) {
               double cxs = cc * 22.0 + off;
               double dx = x + 0.5 - cxs;
               double dy = y + 0.5 - cyr;
               if (!(dy < -2.0)) {
                  double d = Math.hypot(dx, dy) / 14.0;
                  if (!(d > 1.0)) {
                     double h = Capes.hash(cc, r, 4);
                     int base = Capes.ramp(Capes.clamp01(0.5 + dx / 30.800000000000004 + (h - 0.5) * 0.3 + v * 0.3), 944712, 1482874, 1751200, 674376);
                     double dome = 1.0 - d * d * 0.3 + dy / 14.0 * 0.12;
                     int c = Capes.shade(base, dome * (1.05 - v * 0.35));
                     c = Capes.add(c, 15269856, Math.exp(-Math.pow((dx + 2.8000000000000003) / 5.0, 2.0) - Math.pow((dy - 6.3) / 3.0, 2.0)) * 0.2);
                     if (d > 0.84) {
                        c = Capes.mix(c, 10152112, (d - 0.84) / 0.16 * 0.45);
                     }

                     if (d > 0.93) {
                        c = Capes.shade(c, 0.55);
                     }

                     return c;
                  }
               }
            }
         }

         return 335896;
      });
   }

   static void camo(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      Capes.field(img, (u, v, x, y) -> {
         double w = Capes.fbm(u * 3.0, v * 5.0, 0, 0, 3, 70);
         double ax = Capes.fbm(u * 3.2 + w, v * 5.0 + w, 0, 0, 4, 71);
         double b = Capes.fbm(u * 4.0 + 5.0 + w * 0.6, v * 6.0, 0, 0, 4, 72);
         int c = 5925430;
         if (ax < 0.44) {
            c = 3029791;
         }

         if (b > 0.58) {
            c = 9075278;
         }

         if (ax > 0.6 && b < 0.5) {
            c = 1842196;
         }

         double weave = (x + y) % 4 < 2 ? 0.97 : 1.03;
         return Capes.shade(c, weave);
      });
      double cx = 80.0;
      double cy = 70.0;
      g.setColor(Capes.col(0, 0.35));
      g.fill(new java.awt.geom.RoundRectangle2D.Double(cx - 22.0, cy - 16.0 + 2.0, 44.0, 34.0, 8.0, 8.0));
      g.setColor(new Color(3818532));
      g.fill(new java.awt.geom.RoundRectangle2D.Double(cx - 22.0, cy - 16.0, 44.0, 32.0, 8.0, 8.0));
      g.setColor(new Color(13156496));
      g.setStroke(new BasicStroke(1.0F, 0, 1, 1.0F, new float[]{2.0F, 2.0F}, 0.0F));
      g.draw(new java.awt.geom.RoundRectangle2D.Double(cx - 19.0, cy - 13.0, 38.0, 26.0, 6.0, 6.0));
      Path2D st = new java.awt.geom.Path2D.Double();

      for (int i = 0; i < 10; i++) {
         double a = (-Math.PI / 2) + i * Math.PI / 5.0;
         double r = i % 2 == 0 ? 10.0 : 4.0;
         if (i == 0) {
            st.moveTo(cx + Math.cos(a) * r, cy + Math.sin(a) * r);
         } else {
            st.lineTo(cx + Math.cos(a) * r, cy + Math.sin(a) * r);
         }
      }

      st.closePath();
      g.setColor(new Color(14209184));
      g.fill(st);
   }

   static void bricks(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      Capes.field(img, (u, v, xx, yx) -> 1381656);
      int[] cols = new int[]{14876683, 16764163, 27831, 34091, 16088352, 16777215, 10534120, 15233184};
      Random rn = new Random(4L);
      double pitch = 16.0;

      for (int row = 0; row < 16; row++) {
         double y = row * pitch * 2.0 - 4.0 + 0.0;
         double x = -(row % 2) * pitch * 2.0;

         while (x < 160.0) {
            int len = 2 + rn.nextInt(3);
            int c = cols[rn.nextInt(cols.length)];
            brick(g, x, y, len * pitch, pitch * 2.0, c, pitch);
            x += len * pitch;
         }
      }
   }

   private static void brick(Graphics2D g, double x, double y, double w, double h, int c, double pitch) {
      new Color(c);
      g.setPaint(
         new GradientPaint((float)x, (float)y, new Color(Capes.mix(c, 16777215, 0.2)), (float)(x + w), (float)(y + h), new Color(Capes.mix(c, 0, 0.25)))
      );
      g.fill(new java.awt.geom.RoundRectangle2D.Double(x + 0.8, y + 0.8, w - 1.6, h - 1.6, 3.0, 3.0));
      g.setColor(Capes.col(Capes.mix(c, 0, 0.5), 0.8));
      g.setStroke(new BasicStroke(1.0F));
      g.draw(new java.awt.geom.RoundRectangle2D.Double(x + 0.8, y + 0.8, w - 1.6, h - 1.6, 3.0, 3.0));

      for (double sx = x + pitch / 2.0; sx < x + w; sx += pitch) {
         for (double sy = y + pitch / 2.0; sy < y + h; sy += pitch) {
            double r = pitch * 0.31;
            g.setColor(Capes.col(0, 0.35));
            g.fill(new java.awt.geom.Ellipse2D.Double(sx - r + 1.2, sy - r + 1.6, r * 2.0, r * 2.0));
            g.setPaint(
               new GradientPaint(
                  (float)(sx - r), (float)(sy - r), new Color(Capes.mix(c, 16777215, 0.35)), (float)(sx + r), (float)(sy + r), new Color(Capes.mix(c, 0, 0.15))
               )
            );
            g.fill(new java.awt.geom.Ellipse2D.Double(sx - r, sy - r, r * 2.0, r * 2.0));
            g.setColor(Capes.col(16777215, 0.3));
            g.draw(new java.awt.geom.Arc2D.Double(sx - r * 0.7, sy - r * 0.7, r * 1.4, r * 1.4, 100.0, 80.0, 0));
         }
      }
   }

   static void racing(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      Capes.field(img, (u, v, x, y) -> {
         double wave = Math.sin(v * Math.PI * 2.0 * 1.2 + u * 5.0);
         double uu = u + 0.035 * wave;
         double vv = v + 0.03 * Math.sin(u * Math.PI * 2.0 * 1.4 + v * 3.0);
         int cu = (int)Math.floor(uu * 6.0);
         int cv = (int)Math.floor(vv * 9.6);
         int c = (cu + cv & 1) == 0 ? 15921906 : 1315860;
         double sh = 0.84 + 0.2 * Math.cos(v * Math.PI * 2.0 * 1.2 + u * 5.0);
         return Capes.add(Capes.shade(c, sh), 16777215, Math.pow(Math.max(0.0, Math.cos(v * Math.PI * 2.0 * 1.2 + u * 5.0 - 0.6)), 10.0) * 0.12);
      });
      g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(16722474), 160.0F, 0.0F, new Color(11536906)));
      Path2D s = new java.awt.geom.Path2D.Double();
      s.moveTo(0.0, 200.0);
      s.lineTo(160.0, 150.0);
      s.lineTo(160.0, 168.0);
      s.lineTo(0.0, 218.0);
      s.closePath();
      g.setColor(Capes.col(0, 0.35));
      g.fill(AffineTransform.getTranslateInstance(0.0, 3.0).createTransformedShape(s));
      g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(16726586), 160.0F, 0.0F, new Color(11536906)));
      g.fill(s);
   }

   static void moon(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double mx = 80.0;
      double my = 84.0;
      double mr = 34.0;
      Capes.field(img, (u, v, xx, yx) -> {
         int cx = Capes.ramp(v, 330276, 924232, 2763370, 4864634);
         double d = Math.hypot(xx - 80.0, yx - 84.0);
         cx = Capes.add(cx, 13161727, Math.exp(-Math.max(0.0, d - 34.0) / 20.0) * 0.35);
         if (d < 35.0) {
            double nn = Capes.fbm(xx / 14.0, yx / 14.0, 0, 0, 5, 9);
            int mc = Capes.mix(16776170, 13157556, Capes.smooth(0.45, 0.7, nn));
            double limb = Math.sqrt(Math.max(0.0, 1.0 - d / 34.0 * (d / 34.0)));
            mc = Capes.shade(mc, 0.72 + 0.3 * limb);
            cx = Capes.mix(cx, mc, Capes.smooth(34.8, 33.2, d));
         }

         double cl = Capes.fbm(u * 2.2, v * 7.0, 0, 0, 5, 19);
         double band = Math.exp(-Math.pow((v - 0.44) / 0.07, 2.0)) + Math.exp(-Math.pow((v - 0.66) / 0.09, 2.0));
         double ca = Capes.smooth(0.45, 0.62, cl * band + band * 0.15);
         int ccol = Capes.mix(3820170, 12109040, Capes.smooth(0.5, 0.8, cl) * (0.5 + 0.5 * Math.exp(-d / 60.0)));
         return Capes.mix(cx, ccol, ca * 0.85);
      });
      Random rn = new Random(1L);

      for (int i = 0; i < 50; i++) {
         double x = rn.nextDouble() * 160.0;
         double y = rn.nextDouble() * 256.0 * 0.8;
         if (!(Math.hypot(x - 80.0, y - 84.0) < 40.0)) {
            Capes.star(g, x, y, 0.5 + Math.pow(rn.nextDouble(), 3.0) * 2.0, 14739711, 0.4 + 0.5 * rn.nextDouble());
         }
      }

      g.setColor(new Color(329242));
      Path2D c = new java.awt.geom.Path2D.Double();
      c.moveTo(0.0, 256.0);
      c.lineTo(0.0, 222.0);
      c.lineTo(30.0, 214.0);
      c.lineTo(30.0, 196.0);
      c.lineTo(34.0, 196.0);
      c.lineTo(34.0, 190.0);
      c.lineTo(38.0, 190.0);
      c.lineTo(38.0, 196.0);
      c.lineTo(42.0, 196.0);
      c.lineTo(42.0, 176.0);
      c.lineTo(40.0, 176.0);
      c.lineTo(48.0, 160.0);
      c.lineTo(56.0, 176.0);
      c.lineTo(54.0, 176.0);
      c.lineTo(54.0, 200.0);
      c.lineTo(84.0, 200.0);
      c.lineTo(84.0, 186.0);
      c.lineTo(88.0, 186.0);
      c.lineTo(88.0, 192.0);
      c.lineTo(92.0, 192.0);
      c.lineTo(92.0, 186.0);
      c.lineTo(96.0, 186.0);
      c.lineTo(96.0, 206.0);
      c.lineTo(120.0, 212.0);
      c.lineTo(160.0, 206.0);
      c.lineTo(160.0, 256.0);
      c.closePath();
      g.fill(c);
      g.setColor(new Color(16765562));
      g.fill(new java.awt.geom.Rectangle2D.Double(46.5, 180.0, 3.0, 5.0));
      g.fill(new java.awt.geom.Rectangle2D.Double(88.5, 196.0, 2.5, 4.0));
      Capes.addDot(img, 48.0, 182.0, 6.0, 16758858, 0.35);
   }
}
