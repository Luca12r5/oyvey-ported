package dev.lego.cosmetic;

import dev.lego.ui.Art;
import dev.lego.ui.Fonts;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;
import java.util.Random;

final class CapeArt {
   private CapeArt() {
   }

   static void defineAll() {
      Capes.def("lego", 16, 8.0F, 16769658, 10119698, 6161932, CapeArt::lego);
      Capes.def("galaxy", 16, 6.0F, 15591167, 5917322, 1181732, CapeArt::galaxy);
      Capes.def("flames", 16, 12.0F, 16761466, 5906954, 2361349, CapeArt::flames);
      Capes.def("ice", 12, 6.0F, 16777215, 7247550, 1456740, CapeArt::ice);
      Capes.def("aurora", 16, 6.0F, 12124136, 1986130, 397854, CapeArt::aurora);
      Capes.def("sunset", 16, 8.0F, 16767370, 10496618, 1967672, CapeArt::sunset);
      Capes.def("carbon", 1, 0.0F, 16734810, 6949386, 921105, CapeArtHd::carbon);
      Capes.def("neon", 16, 8.0F, 16747253, 4852322, 786968, CapeArt::neon);
      Capes.def("spotify", 16, 10.0F, 8191912, 809514, 724236, CapeArt::spotify);
      Capes.def("sakura", 16, 6.0F, 16774102, 11896922, 15313344, CapeArt::sakura);
      Capes.def("lava", 16, 6.0F, 10123882, 1970702, 1837318, CapeArt::lava);
      Capes.def("ocean", 16, 8.0F, 15138815, 3042962, 469578, CapeArt::ocean);
      Capes.def("matrix", 16, 12.0F, 9240488, 670230, 133636, CapeArt::matrix);
   }

   private static Font bold(float size) {
      return Fonts.get(3, size);
   }

   static void lego(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      Capes.field(img, (u, v, x, yx) -> {
         int c = Capes.mix(16003642, 9045776, v * 0.85 + u * 0.15);
         double d = Math.hypot(u - 0.5, (v - 0.42) * 0.62);
         return Capes.add(c, 16743002, Math.exp(-d * d / 0.03) * 0.25);
      });

      for (int row = 0; row < 14; row++) {
         for (int col = 0; col < 9; col++) {
            double x = 16 + col * 16;
            double y = 16.0 + row * 18.5;
            double dc = Math.hypot((x - 80.0) / 60.0, (y - 110.0) / 70.0);
            double a = 0.35 + 0.65 * Capes.clamp01(dc - 0.55);
            g.setColor(Capes.col(3801092, 0.28 * a));
            g.fill(new Double(x - 5.0 + 1.4, y - 5.0 + 2.0, 10.0, 10.0));
            g.setPaint(
               new GradientPaint((float)x - 5.0F, (float)y - 5.0F, Capes.col(16777215, 0.3 * a), (float)x + 5.0F, (float)y + 5.0F, Capes.col(5898246, 0.25 * a))
            );
            g.fill(new Double(x - 5.0, y - 5.0, 10.0, 10.0));
            g.setColor(Capes.col(16777215, 0.14 * a));
            g.setStroke(new BasicStroke(0.8F));
            g.draw(new Double(x - 3.2, y - 3.2, 6.4, 6.4));
         }
      }

      Capes.dot(g, 80.0, 104.0, 62.0, 3801092, 0.35);
      Art.brick(g, 34.0, 54.0, 92.0, new Color(16764163));
      String s = "LEGO";
      g.setFont(bold(31.0F));
      float w = g.getFontMetrics().stringWidth(s);
      g.setColor(Capes.col(3801092, 0.55));
      g.drawString(s, 80.0F - w / 2.0F + 1.5F, 198.5F);
      g.setPaint(new GradientPaint(0.0F, 170.0F, Color.WHITE, 0.0F, 198.0F, new Color(16770992)));
      g.drawString(s, 80.0F - w / 2.0F, 196.0F);
      g.setFont(bold(11.0F));
      String c2 = "C L I E N T";
      float w2 = g.getFontMetrics().stringWidth(c2);
      g.setColor(Capes.col(16769658, 0.9));
      g.drawString(c2, 80.0F - w2 / 2.0F, 216.0F);
      g.setColor(Capes.col(16769658, 0.5));
      g.setStroke(new BasicStroke(1.2F));
      g.drawLine(30, 211, (int)(80.0F - w2 / 2.0F - 6.0F), 211);
      g.drawLine((int)(80.0F + w2 / 2.0F + 6.0F), 211, 130, 211);
      Capes.sweep(img, (double)k / n, 0.09, 0.42);
   }

   static void galaxy(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double rot = ph / 2.0;
      double cx = 80.0;
      double cy = 110.08;
      Capes.field(img, (u, v, xx, yx) -> {
         double nx = u * 2.6;
         double ny = v * 4.2;
         double w1 = Capes.fbm(nx + 3.0, ny, 0, 0, 3, 11);
         double neb = Capes.fbm(nx + w1 * 1.6, ny + w1 * 1.2, 0, 0, 5, 12);
         double hueN = Capes.fbm(nx * 0.6 + 9.0, ny * 0.6, 0, 0, 3, 13);
         int c = Capes.mix(131337, 721952, v);
         double ni = Math.pow(Capes.smooth(0.38, 0.9, neb), 1.5);
         c = Capes.add(c, Capes.ramp(hueN, 1981183, 6958048, 14696607, 16743002), ni * 0.7);
         double dx = xx + 0.5 - 80.0;
         double dy = (yx + 0.5 - 110.08) / 0.62;
         double r = Math.sqrt(dx * dx + dy * dy) + 0.001;
         double th = Math.atan2(dy, dx) - rot - 2.9 * Math.log(r / 16.0 + 0.2);
         double tt = (th % Math.PI + Math.PI) % Math.PI;
         double arms = Math.pow(0.5 + 0.5 * Math.cos(2.0 * th), 2.4);
         double dust = Capes.fbm(r * 0.11, tt * 8.0 / Math.PI, 0, 8, 3, 21);
         double fall = Math.exp(-r / 34.0);
         double inten = arms * fall * (0.35 + 0.95 * dust) + Math.exp(-(r / 8.0) * (r / 8.0)) * 1.4 + Math.exp(-r / 18.0) * 0.3;
         int gc = Capes.mix(16773328, 10273023, Capes.smooth(4.0, 30.0, r));
         c = Capes.add(c, gc, inten * 0.95);
         if (dust > 0.62) {
            c = Capes.add(c, 16734888, (dust - 0.62) * 2.2 * arms * fall);
         }

         double lane = Capes.smooth(0.5, 0.75, Capes.fbm(r * 0.2 + 5.0, tt * 8.0 / Math.PI, 0, 8, 2, 23)) * arms * Math.exp(-r / 45.0) * 0.55;
         return Capes.shade(c, 1.0 - lane);
      });
      Random rn = new Random(5L);

      for (int i = 0; i < 150; i++) {
         double x = rn.nextDouble() * 160.0;
         double y = rn.nextDouble() * 256.0;
         double big = Math.pow(rn.nextDouble(), 5.0);
         int j = 1 + rn.nextInt(2);
         double p0 = rn.nextDouble() * Math.PI * 2.0;
         double a = (0.35 + 0.65 * (0.5 + 0.5 * Math.sin(ph * j + p0))) * (0.5 + 0.5 * rn.nextDouble());
         int tint = rn.nextInt(3) == 0 ? 16766640 : (rn.nextBoolean() ? 12112127 : 16777215);
         Capes.star(g, x, y, 0.7 + big * 2.6, tint, a);
      }
   }

   static void flames(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      Capes.field(img, (u, v, xx, yx) -> {
         double X = u * 4.0;
         double Yn = v * 6.0 + t * 12.0;
         double w = Capes.fbm(X * 1.2 + 7.0, Yn, 0, 6, 3, 5);
         double n1 = Capes.fbm(X + (w - 0.5) * 1.6, Yn + (w - 0.5) * 0.8, 0, 6, 5, 7);
         double tongues = 0.13 * Math.cos(u * Math.PI * 2.0 * 3.0 + (w - 0.5) * 4.0);
         double heat = v * 1.32 - 0.42 + (n1 - 0.5) * 1.25 + tongues;
         int bg = Capes.mix(655874, 3016709, v);
         bg = Capes.add(bg, 16730640, Math.pow(v, 3.0) * 0.35);
         int fire = Capes.ramp(heat / 1.2, 5900805, 11540490, 15747598, 16748062, 16763210, 16774092);
         double a = Capes.smooth(-0.02, 0.14, heat);
         int c = Capes.mix(bg, fire, a);
         return Capes.add(c, 16771232, Capes.smooth(0.85, 1.3, heat) * 0.25);
      });
      Random rn = new Random(8L);

      for (int i = 0; i < 46; i++) {
         double x0 = rn.nextDouble();
         double y0 = rn.nextDouble();
         int j = 1 + rn.nextInt(2);
         double s = rn.nextDouble() * Math.PI * 2.0;
         double y = ((y0 - j * t) % 1.0 + 1.0) % 1.0;
         double x = (x0 + 0.035 * Math.sin(ph * j + s)) * 160.0;
         double life = Math.pow(y, 0.7);
         double r = 1.2 + rn.nextDouble() * 1.8;
         Capes.addDot(img, x, y * 256.0, r * 3.0, 16738832, 0.5 * life);
         Capes.addDot(img, x, y * 256.0, r, 16769184, 0.9 * life);
      }
   }

   static void ice(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      int CW = 26;
      int CH = 30;
      Capes.field(img, (u, v, xx, yx) -> {
         int base = Capes.ramp(v, 15399935, 10935542, 5612762, 1921686);
         int gx = xx / 26;
         int gy = yx / 30;
         double d1 = 1.0E9;
         double d2 = 1.0E9;
         int best = 0;

         for (int oy = -1; oy <= 1; oy++) {
            for (int ox = -1; ox <= 1; ox++) {
               int cxx = gx + ox;
               int cyx = gy + oy;
               double px = (cxx + 0.15 + 0.7 * Capes.hash(cxx, cyx, 3)) * 26.0;
               double py = (cyx + 0.15 + 0.7 * Capes.hash(cxx, cyx, 4)) * 30.0;
               double d = Math.hypot(xx - px, yx - py);
               if (d < d1) {
                  d2 = d1;
                  d1 = d;
                  best = cxx * 131 + cyx;
               } else if (d < d2) {
                  d2 = d;
               }
            }
         }

         double facet = 0.86 + 0.26 * Capes.hash(best, 1, 9);
         int c = Capes.shade(base, facet);
         double edge = 1.0 - Capes.smooth(0.0, 3.2, d2 - d1);
         c = Capes.add(c, 16777215, edge * 0.38);
         double ex = Math.min(u, 1.0 - u) * 1.6;
         double ey = Math.min(v, 1.0 - v);
         double frost = Capes.smooth(0.45, 0.85, Capes.fbm(u * 9.0, v * 14.0, 0, 0, 4, 44) + (0.3 - Math.min(ex, ey)) * 1.4);
         return Capes.mix(c, 16055551, frost * 0.75);
      });
      double cx = 80.0;
      double cy = 112.0;
      double R = 44.0;
      Path2D sf = new java.awt.geom.Path2D.Double();

      for (int i = 0; i < 6; i++) {
         double a = (Math.PI / 3) * i - (Math.PI / 2);
         double ca = Math.cos(a);
         double sa = Math.sin(a);
         sf.moveTo(cx, cy);
         sf.lineTo(cx + ca * R, cy + sa * R);

         for (double t : new double[]{0.38, 0.62, 0.82}) {
            double bl = R * (0.34 - (t - 0.38) * 0.35);

            for (int sgn = -1; sgn <= 1; sgn += 2) {
               double b = a + sgn * Math.PI / 4.0;
               sf.moveTo(cx + ca * R * t, cy + sa * R * t);
               sf.lineTo(cx + ca * R * t + Math.cos(b) * bl, cy + sa * R * t + Math.sin(b) * bl);
            }
         }
      }

      Capes.dot(g, cx, cy, 64.0, 670330, 0.35);
      g.setColor(Capes.col(932984, 0.55));
      g.setStroke(new BasicStroke(7.0F, 1, 1));
      g.draw(sf);
      Capes.glowStroke(g, sf, 14546687, 3.4F, 1.8F, 0.95);
      Path2D hex = new java.awt.geom.Path2D.Double();

      for (int i = 0; i < 6; i++) {
         double a = (Math.PI / 3) * i - (Math.PI / 2);
         if (i == 0) {
            hex.moveTo(cx + Math.cos(a) * 9.0, cy + Math.sin(a) * 9.0);
         } else {
            hex.lineTo(cx + Math.cos(a) * 9.0, cy + Math.sin(a) * 9.0);
         }
      }

      hex.closePath();
      g.setPaint(new GradientPaint((float)cx - 9.0F, (float)cy - 9.0F, Color.WHITE, (float)cx + 9.0F, (float)cy + 9.0F, new Color(8374512)));
      g.fill(hex);
      Capes.sweep(img, (double)k / n, 0.07, 0.35);
      Random rn = new Random(21L);

      for (int ix = 0; ix < 26; ix++) {
         double x = 14.0 + rn.nextDouble() * 132.0;
         double y = 14.0 + rn.nextDouble() * 228.0;
         int j = 1 + rn.nextInt(2);
         double a = Math.pow(Math.max(0.0, Math.sin(ph * j + rn.nextDouble() * 6.28)), 3.0);
         Capes.star(g, x, y, 1.2 + rn.nextDouble() * 1.6, 13628415, a);
      }
   }

   private static int auroraSky(double u, double v, double ph) {
      int c = Capes.ramp(v / 0.78, 66313, 200746, 665668, 1261644);
      double[] base = new double[]{0.34, 0.46, 0.24};
      double[] str = new double[]{0.95, 0.7, 0.5};

      for (int i = 0; i < 3; i++) {
         double yl = base[i]
            + 0.055 * Math.sin((Math.PI * 2) * u + ph + i * 2.1)
            + 0.03 * Math.sin((Math.PI * 4) * u - ph * 2.0 + i)
            + 0.012 * Math.sin((Math.PI * 10) * u + ph * 3.0 + i);
         double d = v - yl;
         double e = d > 0.0 ? Math.exp(-d / 0.016) : Math.exp(d / (0.15 + 0.05 * i));
         if (!(e < 0.004)) {
            double rays = 0.55
               + 0.3 * Math.sin(u * Math.PI * 2.0 * 9.0 + 2.4 * Math.sin(u * Math.PI * 2.0 * 2.0 + ph + i) + i)
               + 0.15 * Math.sin(u * Math.PI * 2.0 * 23.0 - ph * 2.0 + i * 3);
            double tt = Capes.clamp01(-d / 0.24);
            int col = Capes.ramp(tt, 8191928, 3074232, 3836927, 11557887);
            c = Capes.add(c, col, e * Capes.clamp01(rays) * str[i]);
         }
      }

      return c;
   }

   private static double mountain(double u) {
      return 0.735
         + 0.05 * Math.sin(u * 7.0 + 1.0)
         + 0.03 * Math.sin(u * 17.0 + 2.0)
         + 0.018 * (Capes.vn(u * 26.0, 0.5, 0, 0, 3) - 0.5)
         - 0.07 * Math.exp(-Math.pow((u - 0.3) / 0.12, 2.0));
   }

   static void aurora(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double lake = 0.86;
      Random rn = new Random(2L);
      double[][] stars = new double[70][];

      for (int i = 0; i < stars.length; i++) {
         stars[i] = new double[]{rn.nextDouble(), rn.nextDouble() * 0.7, rn.nextDouble(), 1 + rn.nextInt(2), rn.nextDouble() * 6.28};
      }

      Capes.field(img, (u, v, x, yx) -> {
         double vv = v;
         boolean refl = v > 0.86;
         if (refl) {
            vv = 1.72 - v + 0.006 * Math.sin(v * 160.0 + ph * 2.0);
         }

         double m = mountain(u);
         int c;
         if (vv > m) {
            c = Capes.mix(462362, 132362, (vv - m) * 6.0);
            double rim = Math.exp(-(vv - m) / 0.006);
            c = Capes.add(c, 14545663, rim * 0.35);
            double snow = Capes.smooth(0.02, 0.0, vv - m) * Capes.smooth(0.72, 0.68, m);
            c = Capes.mix(c, 12112616, snow * 0.6 * (0.5 + 0.5 * Capes.vn(u * 40.0, vv * 40.0, 0, 0, 5)));
         } else {
            c = auroraSky(u, vv, ph);
         }

         if (refl) {
            c = Capes.shade(Capes.mix(c, 664632, 0.25), 0.55);
            c = Capes.add(c, 16777215, Math.exp(-(v - 0.86) / 0.004) * 0.12);
         }

         return c;
      });

      for (double[] s : stars) {
         double y = s[1] * 256.0;
         if (!(s[1] > mountain(s[0]) - 0.02)) {
            double a = (0.4 + 0.6 * (0.5 + 0.5 * Math.sin(ph * s[3] + s[4]))) * (0.4 + 0.6 * s[2]);
            Capes.star(g, s[0] * 160.0, y, 0.6 + s[2] * s[2] * 1.8, 14543103, a);
         }
      }
   }

   static void sunset(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      double hz = 0.6;
      double scx = 80.0;
      double scy = 102.4;
      double sr = 49.6;
      Capes.field(img, (u, v, xx, yx) -> {
         if (v < 0.6) {
            int c = Capes.ramp(v / 0.6, 787234, 3016786, 9051256, 15219324, 16751194);
            double dx = xx + 0.5 - 80.0;
            double dy = yx + 0.5 - 102.4;
            double d = Math.hypot(dx, dy);
            c = Capes.add(c, 16734874, Math.exp(-Math.max(0.0, d - 49.6) / 16.0) * 0.45);
            if (d < 49.6) {
               double s = (dy + 49.6) / 99.2;
               int sc = Capes.ramp(s, 16775072, 16760906, 16738906, 16723594);
               boolean gap = false;
               if (s > 0.48) {
                  double f = ((s * 9.0 - t) % 1.0 + 1.0) % 1.0;
                  gap = f < (s - 0.48) * 1.25;
               }

               double edgeA = Capes.smooth(49.6, 48.1, d);
               if (!gap) {
                  c = Capes.mix(c, sc, edgeA);
               }
            }

            double mh = 0.6 - (0.035 + 0.11 * Math.pow(Math.abs(u - 0.5) * 2.0, 1.6)) * (0.75 + 0.5 * Capes.vn(u * 9.0, 1.0, 0, 0, 77));
            if (v > mh) {
               int mc = Capes.mix(3804766, 1311786, (v - mh) / 0.1);
               mc = Capes.add(mc, 16739016, Math.exp(-(v - mh) / 0.005) * 0.6);
               c = mc;
            }

            return c;
         } else {
            double dv = v - 0.6 + 0.0015;
            double z = 0.07 / dv;
            int cx = Capes.mix(2229822, 524562, Capes.smooth(0.0, 0.4, dv));
            double zl = ((z + t) % 1.0 + 1.0) % 1.0;
            double dz = Math.min(zl, 1.0 - zl);
            double thick = 0.07 / (dv * dv) * 0.005078125;
            double hl = Capes.smooth(thick * 1.6, thick * 0.3, dz);
            double xw = (u - 0.5) * z * 7.0;
            double xl = (xw % 1.0 + 1.0) % 1.0;
            double dxx = Math.min(xl, 1.0 - xl);
            double thx = z * 7.0 * 0.008125;
            double vl = Capes.smooth(thx * 1.6, thx * 0.3, dxx);
            double fade = Capes.smooth(0.005, 0.12, dv);
            double line = Math.max(hl, vl) * fade;
            cx = Capes.add(cx, 16726736, line * 0.95);
            cx = Capes.add(cx, 16777215, line * line * 0.35);
            return Capes.add(cx, 16730784, Math.exp(-dv / 0.03) * 0.5);
         }
      });
      Random rn = new Random(33L);

      for (int i = 0; i < 40; i++) {
         double x = rn.nextDouble() * 160.0;
         double y = rn.nextDouble() * 256.0 * 0.3;
         double a = (0.5 + 0.5 * Math.sin(ph * (1 + rn.nextInt(2)) + rn.nextDouble() * 6.28)) * (1.0 - y / 76.8);
         Capes.star(g, x, y, 0.6 + rn.nextDouble() * 1.4, 16767216, a);
      }
   }

   static void carbon(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      int C = 8;
      Capes.field(img, (u, v, x, yx) -> {
         int cxx = x / 8;
         int cyx = yx / 8;
         double fx = (x % 8 + 0.5) / 8.0;
         double fy = (yx % 8 + 0.5) / 8.0;
         boolean horiz = (cxx + cyx & 3) < 2;
         double across = horiz ? fy : fx;
         double along = horiz ? fx : fy;
         double tow = Math.pow(Math.sin(Math.PI * across), 0.6);
         double fib = 0.93 + 0.07 * Math.sin(across * Math.PI * 7.0 + along * 0.6);
         double lightDir = horiz ? 0.55 + 0.45 * Math.sin(u * 5.0 - v * 3.0 + 1.0) : 0.55 + 0.45 * Math.sin(u * 4.0 + v * 2.0 + 3.0);
         int c = Capes.mix(789776, 4869720, tow * fib * (0.35 + 0.5 * lightDir));
         double coat = Math.exp(-Math.pow((u * 0.9 - v * 0.55 - 0.12) / 0.1, 2.0)) * 0.14 + Math.exp(-Math.pow((u * 0.9 - v * 0.55 + 0.35) / 0.2, 2.0)) * 0.08;
         return Capes.add(c, 16777215, coat);
      });

      for (int s = 0; s < 2; s++) {
         double y = s == 0 ? 40.0 : 212.0;
         g.setPaint(new GradientPaint(0.0F, (float)y - 5.0F, new Color(16726586), 0.0F, (float)y + 5.0F, new Color(9046538)));
         g.fill(new java.awt.geom.Rectangle2D.Double(0.0, y - 5.0, 160.0, 10.0));
         g.setColor(Capes.col(16777215, 0.35));
         g.fill(new java.awt.geom.Rectangle2D.Double(0.0, y - 5.0, 160.0, 1.5));
         g.setColor(Capes.col(16726586, 0.8));
         g.fill(new java.awt.geom.Rectangle2D.Double(0.0, y + 8.0, 160.0, 2.0));
      }

      double cx = 80.0;
      double cy = 124.0;
      Capes.dot(g, cx, cy, 58.0, 0, 0.55);
      g.setPaint(
         new LinearGradientPaint(
            (float)cx - 34.0F,
            (float)cy - 34.0F,
            (float)cx + 34.0F,
            (float)cy + 34.0F,
            new float[]{0.0F, 0.45F, 0.55F, 1.0F},
            new Color[]{new Color(16054010), new Color(10134192), new Color(5923440), new Color(13160152)}
         )
      );
      g.fill(new Double(cx - 34.0, cy - 34.0, 68.0, 68.0));
      g.setPaint(new RadialGradientPaint((float)cx, (float)cy - 8.0F, 36.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(2763826), new Color(921362)}));
      g.fill(new Double(cx - 29.0, cy - 29.0, 58.0, 58.0));
      Art.brick(g, cx - 24.0, cy - 27.0, 48.0, new Color(14876683));
      g.setColor(Capes.col(16777215, 0.45));
      g.setStroke(new BasicStroke(1.2F));
      g.draw(new java.awt.geom.Arc2D.Double(cx - 32.0, cy - 32.0, 64.0, 64.0, 60.0, 90.0, 0));
   }

   static void neon(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      double hz = 0.64;
      double pulse = 0.78 + 0.22 * Math.sin(ph);
      int hue = Capes.mix(16726736, 2811647, 0.5 + 0.5 * Math.sin(ph));
      Capes.field(img, (u, v, xx, yx) -> {
         if (v < 0.64) {
            int c = Capes.ramp(v / 0.64, 327948, 1049386, 2754632);
            double vx = u * 10.0 % 1.0;
            double vy = v * 16.0 % 1.0;
            double gl = Math.max(Capes.smooth(0.06, 0.0, Math.min(vx, 1.0 - vx)), Capes.smooth(0.06, 0.0, Math.min(vy, 1.0 - vy)));
            c = Capes.add(c, 6958048, gl * 0.12);
            return Capes.add(c, 16726736, Math.exp(-(0.64 - v) / 0.04) * 0.45);
         } else {
            double dv = v - 0.64 + 0.0015;
            double z = 0.06 / dv;
            int c = Capes.mix(1311274, 262152, Capes.smooth(0.0, 0.3, dv));
            double zl = ((z + t) % 1.0 + 1.0) % 1.0;
            double dz = Math.min(zl, 1.0 - zl);
            double thick = 0.06 / (dv * dv) * 0.00546875;
            double hl = Capes.smooth(thick * 1.8, thick * 0.3, dz);
            double xw = (u - 0.5) * z * 8.0;
            double xl = (xw % 1.0 + 1.0) % 1.0;
            double dx = Math.min(xl, 1.0 - xl);
            double thx = z * 8.0 * 0.008749999999999999;
            double vl = Capes.smooth(thx * 1.8, thx * 0.3, dx);
            double line = Math.max(hl, vl) * Capes.smooth(0.005, 0.1, dv) * pulse;
            c = Capes.add(c, 2811647, line);
            c = Capes.add(c, 16777215, line * line * 0.3);
            return Capes.add(c, 16726736, Math.exp(-dv / 0.025) * 0.55);
         }
      });
      double cx = 80.0;
      double cy = 98.0;
      double R = 46.0;
      Path2D tri = new java.awt.geom.Path2D.Double();

      for (int i = 0; i < 3; i++) {
         double a = (-Math.PI / 2) + i * Math.PI * 2.0 / 3.0;
         if (i == 0) {
            tri.moveTo(cx + Math.cos(a) * R, cy + Math.sin(a) * R);
         } else {
            tri.lineTo(cx + Math.cos(a) * R, cy + Math.sin(a) * R);
         }
      }

      tri.closePath();
      Capes.addDot(img, cx, cy + 4.0, 46.0, hue, 0.35 * pulse);
      g.setPaint(new GradientPaint((float)cx, (float)(cy - R), Capes.col(2754632, 0.2), (float)cx, (float)(cy + R / 2.0), Capes.col(16726736, 0.28)));
      g.fill(tri);
      Capes.glowStroke(g, tri, hue, 3.2F, 2.4F, pulse);
      Path2D tri2 = new java.awt.geom.Path2D.Double();

      for (int ix = 0; ix < 3; ix++) {
         double a = (Math.PI / 2) + ix * Math.PI * 2.0 / 3.0;
         double r2 = R * 0.34;
         if (ix == 0) {
            tri2.moveTo(cx + Math.cos(a) * r2, cy + 2.0 + Math.sin(a) * r2);
         } else {
            tri2.lineTo(cx + Math.cos(a) * r2, cy + 2.0 + Math.sin(a) * r2);
         }
      }

      tri2.closePath();
      Capes.glowStroke(g, tri2, Capes.mix(2811647, 16726736, 0.5 + 0.5 * Math.sin(ph)), 1.6F, 1.4F, 0.8);
      double sy = t * 256.0;
      int[] p = Capes.px(img);

      for (int y = Math.max(0, (int)sy - 6); y < Math.min(256, (int)sy + 6); y++) {
         double a = Math.exp(-Math.pow((y - sy) / 2.2, 2.0)) * 0.35;

         for (int x = 0; x < 160; x++) {
            p[y * 160 + x] = Capes.add(p[y * 160 + x], 12121855, a);
         }
      }
   }

   static void spotify(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double beat = SpotifyCape.LIVE.get() != null ? 0.7 + 0.3 * SpotifyCape.LIVE.get().beat : 0.7 + 0.3 * Math.pow(Math.max(0.0, Math.cos(ph * 2.0)), 4.0);
      Capes.field(img, (u, v, xx, y) -> {
         int c = Capes.ramp(v, 925461, 1184787, 658186);
         double d = Math.hypot(xx - 80, (y - 92) * 0.9);
         c = Capes.add(c, 2021216, Math.exp(-(d / 62.0) * (d / 62.0)) * 0.32 * beat);
         double stripes = 0.5 + 0.5 * Math.sin((u * 0.8 - v * 1.1) * 40.0);
         return Capes.shade(c, 0.96 + 0.04 * stripes);
      });
      double cx = 80.0;
      double cy = 92.0;
      double R = 36.0 * (0.97 + 0.03 * beat);
      Capes.dot(g, cx + 2.0, cy + 5.0, R * 1.3, 0, 0.55);
      g.setPaint(
         new RadialGradientPaint(
            (float)(cx - R * 0.4),
            (float)(cy - R * 0.5),
            (float)(R * 1.6),
            new float[]{0.0F, 0.6F, 1.0F},
            new Color[]{new Color(6090894), new Color(2021216), new Color(1022530)}
         )
      );
      g.fill(new Double(cx - R, cy - R, R * 2.0, R * 2.0));
      g.setColor(new Color(789516));
      float[] wd = new float[]{7.4F, 6.2F, 5.0F};
      double[] wy = new double[]{-13.0, 1.0, 14.0};
      double[] ww = new double[]{26.0, 22.0, 17.0};

      for (int i = 0; i < 3; i++) {
         g.setStroke(new BasicStroke(wd[i] * (float)(R / 36.0), 1, 1));
         double y = cy + wy[i] * R / 36.0;
         double hw = ww[i] * R / 36.0;
         g.draw(new java.awt.geom.QuadCurve2D.Double(cx - hw, y, cx + hw * 0.1, y - 10.0 * R / 36.0, cx + hw, y + 4.0 * R / 36.0));
      }

      g.setPaint(new GradientPaint((float)cx, (float)(cy - R), Capes.col(16777215, 0.3), (float)cx, (float)cy, Capes.col(16777215, 0.0)));
      g.fill(new Double(cx - R * 0.8, cy - R * 0.95, R * 1.6, R * 1.0));
      SpotifyCape.Live live = SpotifyCape.LIVE.get();
      int bars = 11;
      double bw = 7.2;
      double gap = 3.6;
      double x0 = 80.0 - (bars * bw + (bars - 1) * gap) / 2.0;
      double by = 214.0;
      Random rn = new Random(4L);

      for (int i = 0; i < bars; i++) {
         int a1 = 1 + rn.nextInt(3);
         int a2 = 1 + rn.nextInt(3);
         double b1 = rn.nextDouble() * 6.28;
         double b2 = rn.nextDouble() * 6.28;
         double env = 0.55 + 0.45 * Math.sin(Math.PI * (i + 0.5) / bars);
         double h = live != null
            ? 5.0 + 44.0 * live.bars[i]
            : 6.0 + 46.0 * env * (0.25 + 0.75 * (0.5 + 0.5 * Math.sin(ph * a1 + b1)) * (0.55 + 0.45 * (0.5 + 0.5 * Math.sin(ph * a2 + b2))));
         double x = x0 + i * (bw + gap);
         g.setPaint(new GradientPaint(0.0F, (float)(by - h), new Color(12124112), 0.0F, (float)by, new Color(1355852)));
         g.fill(new java.awt.geom.RoundRectangle2D.Double(x, by - h, bw, h, 4.0, 4.0));
         if (live != null && live.peaks[i] > 0.0) {
            double py = by - 5.0 - 44.0 * live.peaks[i] - 4.0;
            g.setColor(Capes.col(15269872, 0.9));
            g.fill(new java.awt.geom.RoundRectangle2D.Double(x, py, bw, 2.2, 2.0, 2.0));
         }

         g.setPaint(new GradientPaint(0.0F, (float)by + 2.0F, Capes.col(2021216, 0.3), 0.0F, (float)(by + 2.0 + h * 0.4), Capes.col(2021216, 0.0)));
         g.fill(new java.awt.geom.RoundRectangle2D.Double(x, by + 2.0, bw, h * 0.4, 4.0, 4.0));
      }

      if (live != null) {
         g.setFont(bold(10.5F));
         FontMetrics fm = g.getFontMetrics();
         String tt = fit(live.title, fm, 140);
         g.setColor(Capes.col(16777215, 0.95));
         g.drawString(tt, (float)(80.0 - fm.stringWidth(tt) / 2.0), 140.0F);
         g.setFont(bold(8.0F));
         fm = g.getFontMetrics();
         String ar = fit(live.artist, fm, 140);
         g.setColor(Capes.col(10155452, 0.85));
         g.drawString(ar, (float)(80.0 - fm.stringWidth(ar) / 2.0), 151.0F);
         double px = 22.0;
         double pw = 116.0;
         double py = 232.0;
         g.setColor(Capes.col(16777215, 0.18));
         g.fill(new java.awt.geom.RoundRectangle2D.Double(px, py, pw, 3.0, 3.0, 3.0));
         g.setColor(new Color(2021216));
         g.fill(new java.awt.geom.RoundRectangle2D.Double(px, py, pw * live.progress, 3.0, 3.0, 3.0));
         Capes.dot(g, px + pw * live.progress, py + 1.5, 3.2, 16777215, 0.95);
         if (!live.playing) {
            g.setColor(Capes.col(16777215, 0.8));
            g.fill(new java.awt.geom.RoundRectangle2D.Double(71.0, 180.0, 6.0, 18.0, 2.0, 2.0));
            g.fill(new java.awt.geom.RoundRectangle2D.Double(83.0, 180.0, 6.0, 18.0, 2.0, 2.0));
         }
      }
   }

   private static String fit(String s, FontMetrics fm, int w) {
      if (s == null) {
         return "";
      } else if (fm.stringWidth(s) <= w) {
         return s;
      } else {
         while (s.length() > 1 && fm.stringWidth(s + "…") > w) {
            s = s.substring(0, s.length() - 1);
         }

         return s + "…";
      }
   }

   private static Path2D petal(double len, double wid) {
      Path2D p = new java.awt.geom.Path2D.Double();
      p.moveTo(0.0, len * 0.5);
      p.curveTo(wid * 0.9, len * 0.2, wid * 0.7, -len * 0.45, wid * 0.18, -len * 0.5);
      p.lineTo(0.0, -len * 0.36);
      p.lineTo(-wid * 0.18, -len * 0.5);
      p.curveTo(-wid * 0.7, -len * 0.45, -wid * 0.9, len * 0.2, 0.0, len * 0.5);
      p.closePath();
      return p;
   }

   static void blossom(Graphics2D g, double x, double y, double r, double rot, int pink) {
      AffineTransform o = g.getTransform();
      g.translate(x, y);
      g.rotate(rot);

      for (int i = 0; i < 5; i++) {
         AffineTransform o2 = g.getTransform();
         g.rotate((Math.PI * 2) * i / 5.0);
         g.translate(0.0, -r * 0.55);
         Path2D p = petal(r * 1.05, r * 0.95);
         g.setPaint(new GradientPaint(0.0F, (float)(r * 0.5), new Color(pink), 0.0F, (float)(-r * 0.5), new Color(16774906)));
         g.fill(p);
         g.setColor(Capes.col(12599402, 0.35));
         g.setStroke(new BasicStroke(0.6F));
         g.draw(p);
         g.setTransform(o2);
      }

      g.setColor(new Color(14696558));
      g.fill(new Double(-r * 0.25, -r * 0.25, r * 0.5, r * 0.5));
      g.setColor(new Color(16769658));

      for (int i = 0; i < 6; i++) {
         double a = (Math.PI * 2) * i / 6.0;
         g.fill(new Double(Math.cos(a) * r * 0.36 - 0.8, Math.sin(a) * r * 0.36 - 0.8, 1.6, 1.6));
      }

      g.setTransform(o);
   }

   static void sakura(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      Capes.field(img, (u, v, xx, yx) -> {
         int c = Capes.ramp(v, 16773622, 16767463, 16366803, 15442628);
         c = Capes.add(c, 16777215, Math.exp(-Math.hypot(u - 0.78, v - 0.18) / 0.12) * 0.35);
         double m = 0.64 + 0.42 * Math.pow(Math.abs(u - 0.56), 1.25) + 0.012 * (Capes.vn(u * 18.0, 3.0, 0, 0, 51) - 0.5);
         if (v > m) {
            int mc = Capes.mix(13017816, 15320284, (v - m) * 2.5);
            double capLine = 0.69 + 0.02 * Math.sin(u * 60.0) * Capes.vn(u * 30.0, 1.0, 0, 0, 5);
            if (v < capLine) {
               mc = Capes.mix(16777215, 14470127, (v - m) / 0.06);
            }

            c = Capes.mix(c, mc, Capes.smooth(0.0, 0.004, v - m) * 0.85);
         }

         return Capes.mix(c, 16774392, Capes.smooth(0.78, 1.0, v) * 0.55);
      });
      Path2D br = new java.awt.geom.Path2D.Double();
      br.moveTo(-10.0, 22.0);
      br.curveTo(40.0, 30.0, 70.0, 58.0, 128.0, 64.0);
      g.setStroke(new BasicStroke(8.0F, 1, 1));
      g.setColor(new Color(3808806));
      g.draw(br);
      g.setStroke(new BasicStroke(2.2F, 1, 1));
      g.setColor(Capes.col(9067104, 0.8));
      g.draw(new java.awt.geom.QuadCurve2D.Double(-10.0, 19.0, 40.0, 24.0, 124.0, 61.0));
      double[][] twigs = new double[][]{
         {30.0, 30.0, 44.0, 8.0}, {64.0, 48.0, 58.0, 86.0}, {94.0, 58.0, 120.0, 36.0}, {108.0, 62.0, 132.0, 90.0}, {48.0, 38.0, 20.0, 62.0}
      };
      g.setColor(new Color(3808806));
      g.setStroke(new BasicStroke(3.2F, 1, 1));

      for (double[] tw : twigs) {
         g.draw(new java.awt.geom.QuadCurve2D.Double(tw[0], tw[1], (tw[0] + tw[2]) / 2.0 + 4.0, (tw[1] + tw[3]) / 2.0 - 4.0, tw[2], tw[3]));
      }

      Random rn = new Random(12L);
      double[][] cl = new double[][]{
         {44.0, 8.0}, {58.0, 86.0}, {120.0, 36.0}, {132.0, 90.0}, {20.0, 62.0}, {84.0, 58.0}, {12.0, 26.0}, {100.0, 70.0}, {70.0, 44.0}
      };

      for (double[] c : cl) {
         for (int i = 0; i < 3; i++) {
            double r = 6.0 + rn.nextDouble() * 4.0;
            blossom(g, c[0] + rn.nextGaussian() * 7.0, c[1] + rn.nextGaussian() * 6.0, r, rn.nextDouble() * 6.28, rn.nextBoolean() ? 16752320 : 16758994);
         }
      }

      for (int i = 0; i < 30; i++) {
         double x0 = rn.nextDouble();
         double y0 = rn.nextDouble();
         double s = rn.nextDouble() * 6.28;
         int dir = rn.nextBoolean() ? 1 : -1;
         double y = (y0 + t) % 1.0 * 286.0 - 15.0;
         double x = (x0 + t) % 1.0 * 190.0 - 15.0 + Math.sin(ph + s) * 8.0;
         double sz = 4.0 + rn.nextDouble() * 3.5;
         AffineTransform o = g.getTransform();
         g.translate(x, y);
         g.rotate(s + ph * dir);
         g.scale(0.35 + 0.65 * Math.abs(Math.cos(ph + s)), 1.0);
         Path2D p = petal(sz * 1.3, sz);
         g.setPaint(new GradientPaint(0.0F, (float)sz, new Color(16748468), 0.0F, (float)(-sz), new Color(16773366)));
         g.fill(p);
         g.setTransform(o);
      }
   }

   static void lava(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      int CX = 5;
      int CY = 8;
      int CW = 32;
      int CH = 32;
      Capes.field(img, (u, v, xx, yx) -> {
         double yy = yx - t * 32.0;
         int gx = Math.floorDiv(xx, 32);
         int gy = (int)Math.floor(yy / 32.0);
         double d1 = 1.0E9;
         double d2 = 1.0E9;
         int best = 0;

         for (int oy = -1; oy <= 1; oy++) {
            for (int ox = -1; ox <= 1; ox++) {
               int cx = gx + ox;
               int cy = gy + oy;
               int hx = Math.floorMod(cx, 5);
               int hy = Math.floorMod(cy, 8);
               double px = (cx + 0.15 + 0.7 * Capes.hash(hx, hy, 61)) * 32.0;
               double py = (cy + 0.15 + 0.7 * Capes.hash(hx, hy, 62)) * 32.0;
               double dx = xx - px;
               double dy = yy - py;
               double d = Math.sqrt(dx * dx + dy * dy * 1.2);
               if (d < d1) {
                  d2 = d1;
                  d1 = d;
                  best = hx * 17 + hy;
               } else if (d < d2) {
                  d2 = d;
               }
            }
         }

         double crack = d2 - d1;
         double flow = Capes.fbm(u * 5.0, v * 8.0 - t * 8.0, 0, 8, 3, 63);
         double pulse = 0.72 + 0.28 * Math.sin(ph + Capes.hash(best, 2, 64) * Math.PI * 2.0);
         double rockN = Capes.fbm(u * 14.0, yy / 256.0 * 22.0, 0, 0, 4, 65);
         int rock = Capes.mix(4074020, 1050888, rockN * 0.8 + Capes.hash(best, 5, 66) * 0.3);
         rock = Capes.shade(rock, 0.75 + 0.35 * Capes.smooth(0.0, 16.0, crack));
         double glow = Math.exp(-crack / 3.2) * (0.6 + 0.6 * flow) * pulse;
         double warm = Math.exp(-crack / 12.0) * 0.35 * pulse;
         int c = Capes.add(rock, 16726538, warm);
         int lava = Capes.ramp(glow, 0, 10097152, 16730624, 16752672, 16773280);
         return Capes.mix(c, lava, Capes.smooth(0.15, 0.55, glow));
      });
      Random rn = new Random(18L);

      for (int i = 0; i < 22; i++) {
         double x0 = rn.nextDouble();
         double y0 = rn.nextDouble();
         double y = ((y0 - t) % 1.0 + 1.0) % 1.0;
         double x = (x0 + 0.03 * Math.sin(ph + i)) * 160.0;
         Capes.addDot(img, x, y * 256.0, 3.5, 16742938, 0.5 * y);
         Capes.addDot(img, x, y * 256.0, 1.2, 16771232, 0.8 * y);
      }
   }

   static void ocean(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      double cph = Math.cos(ph);
      double sph = Math.sin(ph);
      Capes.field(img, (u, v, xx, yx) -> {
         int c = Capes.ramp(v, 5954806, 1413839, 678558, 338014, 135734);
         double s = u + v * 0.28;
         double rays = 0.0;
         double[] rc = new double[]{0.12, 0.3, 0.52, 0.7, 0.9};

         for (int ix = 0; ix < rc.length; ix++) {
            double d = (s - rc[ix] - 0.02 * Math.sin(ph + ix)) / (0.035 + 0.02 * (ix % 2));
            rays += Math.exp(-d * d) * (0.6 + 0.4 * Math.sin(ph * (1 + ix % 2) + ix * 1.7));
         }

         c = Capes.add(c, 13630463, rays * 0.28 * Math.exp(-v * 2.4));
         double X = u * 16.0;
         double Y = v * 22.0;
         double cs = Math.sin(X + 1.4 * Math.sin(Y * 0.7 + ph)) + Math.sin(Y + 1.4 * Math.sin(X * 0.8 - ph)) + Math.sin((X + Y) * 0.55 + ph);
         double caus = Math.pow(Capes.clamp01(cs / 3.0), 3.0) * 2.2;
         c = Capes.add(c, 14745599, caus * 0.32 * Math.pow(1.0 - v, 2.2));
         double sl = 0.055 + 0.01 * Math.sin(u * Math.PI * 6.0 + ph) + 0.005 * Math.sin(u * Math.PI * 14.0 - ph * 2.0);
         if (v < sl) {
            c = Capes.mix(c, 14220031, 0.55 + 0.45 * Capes.smooth(sl, sl - 0.03, v));
         }

         c = Capes.add(c, 16777215, Math.exp(-Math.abs(v - sl) / 0.005) * 0.5);
         double sand = 0.93 + 0.02 * Math.sin(u * 9.0 + 1.0);
         if (v > sand) {
            c = Capes.mix(c, Capes.mix(9075282, 3814434, (v - sand) * 10.0), Capes.smooth(sand, sand + 0.01, v) * 0.8);
         }

         return c;
      });
      Random rn = new Random(44L);

      for (int i = 0; i < 6; i++) {
         double bx = 10 + i * 28 + rn.nextDouble() * 10.0;
         double hgt = 60.0 + rn.nextDouble() * 70.0;
         double s = rn.nextDouble() * 6.28;
         Path2D kp = new java.awt.geom.Path2D.Double();
         kp.moveTo(bx, 256.0);

         for (int j = 1; j <= 10; j++) {
            double yy = 256.0 - hgt * j / 10.0;
            kp.lineTo(bx + Math.sin(ph + s + j * 0.5) * j * 0.9, yy);
         }

         g.setColor(Capes.col(Capes.mix(936506, 2787930, rn.nextDouble()), 0.85));
         g.setStroke(new BasicStroke(3.2F, 1, 1));
         g.draw(kp);
      }

      for (int i = 0; i < 8; i++) {
         double x0 = rn.nextDouble();
         double y = 90.0 + rn.nextDouble() * 70.0;
         double x = (x0 + t) % 1.0 * 200.0 - 20.0;
         double yy = y + Math.sin(ph * 2.0 + i) * 2.0;
         AffineTransform o = g.getTransform();
         g.translate(x, yy);
         g.setColor(Capes.col(405594, 0.6));
         g.fill(new Double(-7.0, -2.6, 12.0, 5.2));
         Path2D tail = new java.awt.geom.Path2D.Double();
         tail.moveTo(-6.0, 0.0);
         tail.lineTo(-11.0, -3.5 + Math.sin(ph * 4.0 + i));
         tail.lineTo(-11.0, 3.5 + Math.sin(ph * 4.0 + i));
         tail.closePath();
         g.fill(tail);
         g.setTransform(o);
      }

      for (int i = 0; i < 26; i++) {
         double x0 = rn.nextDouble() * 160.0;
         double y0 = rn.nextDouble();
         int j = 1 + rn.nextInt(2);
         double r = 1.4 + Math.pow(rn.nextDouble(), 2.0) * 4.0;
         double y = ((y0 - j * t) % 1.0 + 1.0) % 1.0 * 256.0;
         double x = x0 + Math.sin(ph * j * 2.0 + i) * 3.0;
         g.setColor(Capes.col(15269887, 0.55));
         g.setStroke(new BasicStroke(0.9F));
         g.draw(new Double(x - r, y - r, r * 2.0, r * 2.0));
         g.setColor(Capes.col(16777215, 0.14));
         g.fill(new Double(x - r, y - r, r * 2.0, r * 2.0));
         g.setColor(Capes.col(16777215, 0.85));
         g.fill(new Double(x - r * 0.55, y - r * 0.6, r * 0.5, r * 0.45));
      }
   }

   static void matrix(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      Capes.field(img, (u, v, x, y) -> {
         int cx = Capes.ramp(v, 67587, 134405, 66818);
         double d = Math.hypot(u - 0.5, v - 0.45);
         return Capes.add(cx, 678426, Math.exp(-d * d / 0.08) * 0.25);
      });
      int cols = 10;
      int rows = 16;
      int cs = 16;
      int[] p = Capes.px(img);

      for (int c = 0; c < cols; c++) {
         for (int drop = 0; drop < 2; drop++) {
            int sp = Capes.hash(c, drop, 91) < 0.4 ? 2 : 1;
            int head = (int)(Capes.hash(c, drop, 92) * rows) + drop * rows / 2;
            int L = 5 + (int)(Capes.hash(c, drop, 93) * 9.0);
            int hr = Math.floorMod(head + sp * k, rows);

            for (int r = 0; r < rows; r++) {
               int dist = Math.floorMod(hr - r, rows);
               if (dist < L) {
                  double b = dist == 0 ? 1.0 : Math.pow(1.0 - (double)dist / L, 1.4) * 0.85;
                  int flick = Capes.hash(c, r, 94) < 0.2 ? k / 4 : 0;
                  int gly = (int)(Capes.hash(c * 31 + flick, r, 95) * 1000000.0);
                  int color = dist == 0 ? 15400938 : Capes.mix(409626, 3997560, b);

                  for (int gy = 0; gy < 7; gy++) {
                     for (int gx = 0; gx < 5; gx++) {
                        int bit = gx < 3 ? gly >> gy * 3 + gx & 1 : gly >> gy * 3 + (4 - gx) & 1;
                        if (gx == 2 && gy % 3 == 0) {
                           bit = 1;
                        }

                        if ((gly >> 21 & 1) == 1 && gx >= 3) {
                           bit = gly >> gy + 22 + gx & 1;
                        }

                        if (bit != 0) {
                           for (int yy = 0; yy < 2; yy++) {
                              for (int xx = 0; xx < 2; xx++) {
                                 int px = c * cs + 3 + gx * 2 + xx;
                                 int py = r * cs + 1 + gy * 2 + yy;
                                 if (px < 160 && py < 256) {
                                    p[py * 160 + px] = Capes.over(p[py * 160 + px], color, 0.25 + 0.75 * b);
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (dist == 0) {
                     Capes.addDot(img, c * cs + 8, r * cs + 8, 9.0, 3997560, 0.35);
                  }
               }
            }
         }
      }
   }
}
