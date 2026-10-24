package dev.lego.cosmetic;

import dev.lego.ui.Art;
import dev.lego.ui.Gx;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

final class CapeArtHd {
   private CapeArtHd() {
   }

   static void relief(BufferedImage img, double[] h, double str, double spec, double ao, double shadow) {
      int w = img.getWidth();
      int hh = img.getHeight();
      int[] p = Capes.px(img);
      double lx = -0.5;
      double ly = -0.62;
      double lz = 0.6;
      double ll = Math.sqrt(lx * lx + ly * ly + lz * lz);
      lx /= ll;
      ly /= ll;
      lz /= ll;
      double hz = lz + 1.0;
      double hl = Math.sqrt(lx * lx + ly * ly + hz * hz);
      double hx = lx / hl;
      double hy = ly / hl;
      hz /= hl;
      double[] blur = boxBlur(h, w, hh, 5);

      for (int y = 0; y < hh; y++) {
         for (int x = 0; x < w; x++) {
            int i = y * w + x;
            double gx = h[y * w + Math.min(w - 1, x + 1)] - h[y * w + Math.max(0, x - 1)];
            double gy = h[Math.min(hh - 1, y + 1) * w + x] - h[Math.max(0, y - 1) * w + x];
            double nx = -gx * str;
            double ny = -gy * str;
            double nz = 1.0;
            double nl = Math.sqrt(nx * nx + ny * ny + nz * nz);
            nx /= nl;
            ny /= nl;
            nz /= nl;
            double d = nx * lx + ny * ly + nz * lz;
            double m = 1.0 + (d - lz) * 1.35;
            double cav = Math.min(0.0, h[i] - blur[i]) * ao;
            m *= 1.0 + cav;
            if (shadow > 0.0) {
               double occ = 0.0;

               for (int s = 1; s <= 6; s++) {
                  int sx = x - (int)Math.round(s * 0.8);
                  int sy = y - s;
                  if (sx < 0 || sy < 0) {
                     break;
                  }

                  double dh = h[sy * w + sx] - h[i] - s * 0.06;
                  if (dh > 0.0) {
                     occ = Math.max(occ, Math.min(1.0, dh * 3.0));
                  }
               }

               m *= 1.0 - occ * shadow;
            }

            int c = Capes.shade(p[i], m);
            double sp = Math.pow(Math.max(0.0, nx * hx + ny * hy + nz * hz), 40.0) * spec;
            p[i] = p[i] & 0xFF000000 | Capes.add(c, 16777215, sp) & 16777215;
         }
      }
   }

   static double[] boxBlur(double[] h, int w, int hh, int r) {
      double[] a = new double[h.length];
      double[] b = new double[h.length];

      for (int y = 0; y < hh; y++) {
         double s = 0.0;
         int n = 0;

         for (int x = -r; x < w + r; x++) {
            int xa = x + r;
            int xr = x - r - 1;
            if (xa < w && xa >= 0) {
               s += h[y * w + xa];
               n++;
            }

            if (xr >= 0 && xr < w) {
               s -= h[y * w + xr];
               n--;
            }

            if (x >= 0 && x < w) {
               a[y * w + x] = s / Math.max(1, n);
            }
         }
      }

      for (int x = 0; x < w; x++) {
         double s = 0.0;
         int n = 0;

         for (int y = -r; y < hh + r; y++) {
            int ya = y + r;
            int yr = y - r - 1;
            if (ya < hh && ya >= 0) {
               s += a[ya * w + x];
               n++;
            }

            if (yr >= 0 && yr < hh) {
               s -= a[yr * w + x];
               n--;
            }

            if (y >= 0 && y < hh) {
               b[y * w + x] = s / Math.max(1, n);
            }
         }
      }

      return b;
   }

   static double[] heights(BufferedImage gray) {
      int[] q = Capes.px(gray);
      double[] h = new double[q.length];

      for (int i = 0; i < q.length; i++) {
         h[i] = (q[i] >> 16 & 0xFF) / 255.0;
      }

      return h;
   }

   static BufferedImage canvas() {
      return new BufferedImage(160, 256, 2);
   }

   static void scales(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double SW = 22.0;
      double SH = 12.0;
      double R = 14.0;
      double[] hgt = new double[40960];
      Capes.field(img, (u, v, x, yx) -> {
         int r0 = (int)Math.floor((yx - 14.0) / 12.0) - 1;

         for (int r = r0; r <= r0 + 3; r++) {
            double cyr = r * 12.0;
            double off = Math.floorMod(r, 2) * 22.0 / 2.0;
            int ci = (int)Math.floor((x - off) / 22.0 + 0.5);

            for (int cc = ci - 1; cc <= ci + 1; cc++) {
               double cxs = cc * 22.0 + off;
               double dx = x + 0.5 - cxs;
               double dy = yx + 0.5 - cyr;
               if (!(dy < -2.0)) {
                  double wdt = 14.0 * (1.0 - 0.35 * Capes.clamp01(dy / 14.0));
                  double d = Math.hypot(dx / wdt * 14.0, dy) / 14.0;
                  if (!(d > 1.0)) {
                     double hs = Capes.hash(cc, r, 4);
                     double keel = Math.exp(-Math.pow(dx / 1.8, 2.0)) * 0.18 * Capes.clamp01(dy / 14.0 + 0.3);
                     double dome = Math.sqrt(Math.max(0.0, 1.0 - d * d));
                     hgt[yx * 160 + x] = 0.25 + 0.55 * dome + keel + r % 2 * 0.02;
                     double irid = Capes.clamp01(0.5 + dx / 33.6 + (hs - 0.5) * 0.35 + v * 0.25 - dome * 0.2);
                     int base = Capes.ramp(irid, 674362, 1280610, 2080912, 8052896, 15255642);
                     int c = Capes.shade(base, 0.85 + 0.25 * dome - v * 0.25);
                     double ang = Math.atan2(dy, dx);
                     double vein = Math.pow(Math.abs(Math.sin(ang * 5.0 + hs * 3.0)), 12.0) * 0.12 * d;
                     c = Capes.shade(c, 1.0 - vein);
                     if (d > 0.9) {
                        c = Capes.mix(c, 13168800, (d - 0.9) / 0.1 * 0.35);
                     }

                     return c;
                  }
               }
            }
         }

         hgt[yx * 160 + x] = 0.0;
         return 201742;
      });
      relief(img, hgt, 9.0, 0.45, 1.1, 0.5);

      for (int y = 0; y < 256; y += 12) {
         Path2D sp = new Double();
         sp.moveTo(75.0, y + 10);
         sp.lineTo(80.0, y - 2);
         sp.lineTo(85.0, y + 10);
         sp.closePath();
         g.setPaint(new GradientPaint(75.0F, 0.0F, new Color(16773280), 85.0F, 0.0F, new Color(9067024)));
         g.fill(sp);
         g.setColor(Capes.col(0, 0.4));
         g.setStroke(new BasicStroke(0.8F));
         g.draw(sp);
      }
   }

   static void camo(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      Capes.field(img, (u, v, x, y) -> {
         double w = Capes.fbm(u * 3.0, v * 5.0, 0, 0, 3, 70);
         double a = Capes.fbm(u * 3.2 + w, v * 5.0 + w, 0, 0, 4, 71);
         double b = Capes.fbm(u * 4.0 + 5.0 + w * 0.6, v * 6.0, 0, 0, 4, 72);
         int c = 5925430;
         c = Capes.mix(c, 3029791, Capes.smooth(0.47, 0.41, a));
         c = Capes.mix(c, 9075278, Capes.smooth(0.55, 0.61, b));
         c = Capes.mix(c, 1842196, Capes.smooth(0.57, 0.63, a) * Capes.smooth(0.53, 0.47, b));
         return Capes.mix(c, 10132090, Capes.fbm(u * 12.0, v * 18.0, 0, 0, 2, 73) * 0.12);
      });
      BufferedImage hi = canvas();
      int[] q = Capes.px(hi);

      for (int y = 0; y < 256; y++) {
         for (int x = 0; x < 160; x++) {
            double tw = (x + y) % 4 < 2 ? 0.35 : 0.25;
            boolean grid = x % 12 == 0 || y % 12 == 0;
            int v = (int)((tw + (grid ? 0.12 : 0.0)) * 255.0);
            q[y * 160 + x] = 0xFF000000 | v << 16 | v << 8 | v;
         }
      }

      Graphics2D hg = hi.createGraphics();
      Gx.hints(hg);
      double cx = 80.0;
      double cy = 70.0;
      hg.setColor(new Color(10132122));
      hg.fill(new java.awt.geom.RoundRectangle2D.Double(cx - 22.0, cy - 16.0, 44.0, 32.0, 8.0, 8.0));
      hg.setColor(new Color(14211288));
      hg.setStroke(new BasicStroke(1.4F, 0, 1, 1.0F, new float[]{2.0F, 1.5F}, 0.0F));
      hg.draw(new java.awt.geom.RoundRectangle2D.Double(cx - 19.0, cy - 13.0, 38.0, 26.0, 6.0, 6.0));
      hg.setColor(new Color(16777215));
      hg.fill(XmasTex.starShape(cx, cy, 10.0, 4.0, 5));
      hg.dispose();
      g.setColor(new Color(3818532));
      g.fill(new java.awt.geom.RoundRectangle2D.Double(cx - 22.0, cy - 16.0, 44.0, 32.0, 8.0, 8.0));
      g.setColor(new Color(13156496));
      g.setStroke(new BasicStroke(1.2F, 0, 1, 1.0F, new float[]{2.0F, 1.5F}, 0.0F));
      g.draw(new java.awt.geom.RoundRectangle2D.Double(cx - 19.0, cy - 13.0, 38.0, 26.0, 6.0, 6.0));
      g.setColor(new Color(14209184));
      g.fill(XmasTex.starShape(cx, cy, 10.0, 4.0, 5));
      double[] h = boxBlur(heights(hi), 160, 256, 1);
      relief(img, h, 6.0, 0.12, 0.8, 0.6);
   }

   static void bricks(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      int[] cols = new int[]{14876683, 16764163, 27831, 34091, 16088352, 16053492, 10534120, 15233184};
      int[] cid = new int[40960];
      Arrays.fill(cid, -1);
      double[] hgt = new double[40960];
      Random rn = new Random(4L);
      double pitch = 16.0;
      List<double[]> studs = new ArrayList<>();

      for (int row = 0; row < 9; row++) {
         double y = row * pitch * 2.0 - 4.0;
         double x = -(row % 2) * pitch * 2.0;

         while (x < 160.0) {
            int len = 2 + rn.nextInt(3);
            int c = cols[rn.nextInt(cols.length)];
            double w = len * pitch;
            double hh = pitch * 2.0;

            for (int yy = (int)Math.max(0.0, y); yy < Math.min(256.0, y + hh); yy++) {
               for (int xx = (int)Math.max(0.0, x); xx < Math.min(160.0, x + w); xx++) {
                  double ex = Math.min(xx + 0.5 - x, x + w - xx - 0.5);
                  double ey = Math.min(yy + 0.5 - y, y + hh - yy - 0.5);
                  double e = Math.min(ex, ey);
                  if (!(e < 0.6)) {
                     cid[yy * 160 + xx] = c;
                     hgt[yy * 160 + xx] = 0.5 * Capes.smooth(0.6, 2.4, e);
                  }
               }
            }

            for (double sx = x + pitch / 2.0; sx < x + w; sx += pitch) {
               for (double sy = y + pitch / 2.0; sy < y + hh; sy += pitch) {
                  studs.add(new double[]{sx, sy, c});
               }
            }

            x += w;
         }
      }

      double r = pitch * 0.31;

      for (double[] s : studs) {
         for (int yy = (int)(s[1] - r - 1.0); yy <= s[1] + r + 1.0; yy++) {
            for (int xxx = (int)(s[0] - r - 1.0); xxx <= s[0] + r + 1.0; xxx++) {
               if (xxx >= 0 && yy >= 0 && xxx < 160 && yy < 256) {
                  double d = Math.hypot(xxx + 0.5 - s[0], yy + 0.5 - s[1]);
                  if (!(d > r)) {
                     double top = 0.9 - 0.25 * Capes.smooth(r - 1.3, r, d);
                     hgt[yy * 160 + xxx] = Math.max(hgt[yy * 160 + xxx], top);
                  }
               }
            }
         }
      }

      Capes.field(img, (u, v, x, yx) -> {
         int c = cid[yx * 160 + x];
         return c < 0 ? 789519 : Capes.shade(c, 0.95 + 0.05 * Capes.hash(x / 3, yx / 3, 11));
      });
      relief(img, hgt, 14.0, 0.65, 1.3, 0.75);
      g.setStroke(new BasicStroke(0.8F));

      for (double[] s : studs) {
         g.setColor(Capes.col(16777215, 0.22));
         g.draw(new java.awt.geom.Arc2D.Double(s[0] - r * 0.55, s[1] - r * 0.55, r * 1.1, r * 1.1, 90.0, 110.0, 0));
      }
   }

   static void racing(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double[] hgt = new double[40960];
      Capes.field(img, (u, v, x, y) -> {
         double wv = Math.sin(v * Math.PI * 2.0 * 1.2 + u * 5.0) + 0.35 * Math.sin(u * Math.PI * 2.0 * 1.6 - v * 4.0 + 1.0);
         hgt[y * 160 + x] = 0.5 + 0.28 * wv + 0.02 * ((x + y) % 3 == 0 ? 1 : 0);
         double uu = u + 0.035 * wv;
         double vv = v + 0.03 * Math.sin(u * Math.PI * 2.0 * 1.4 + v * 3.0);
         int cu = (int)Math.floor(uu * 6.0);
         int cv = (int)Math.floor(vv * 9.6);
         double fu = uu * 6.0 - cu;
         double fv = vv * 9.6 - cv;
         double edge = Math.min(Math.min(fu, 1.0 - fu), Math.min(fv, 1.0 - fv));
         int c = (cu + cv & 1) == 0 ? 15921906 : 1447446;
         if (edge < 0.04) {
            c = Capes.mix(c, 8684676, (0.04 - edge) / 0.04 * 0.5);
         }

         double band = vv * 256.0 - (200.0 - uu * 50.0);
         if (band > -9.0 && band < 9.0) {
            c = Capes.mix(15212586, 10488338, (band + 9.0) / 18.0);
         }

         return c;
      });
      relief(img, hgt, 24.0, 0.25, 0.4, 0.0);
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
      double[] hgt = new double[40960];
      Capes.field(img, (u, v, xx, yx) -> {
         double w = Capes.fbm(u * 3.0 + t * 3.0, v * 4.0, 3, 0, 3, 40);
         double c1 = Capes.fbm(u * 3.0 + t * 3.0 + (w - 0.5) * 0.8, v * 4.0 + (w - 0.5), 3, 0, 5, 41);
         double dens = Capes.smooth(0.2, 0.75, c1 + (0.45 - v) * 1.1);
         hgt[yx * 160 + xx] = dens * (0.6 + 0.4 * c1);
         int sky = Capes.ramp(v, 1976380, 1186342, 527122);
         int cloud = Capes.ramp(c1, 1053726, 2765898, 5595774, 9083060);
         int c = Capes.mix(sky, cloud, dens);
         double lit = fl * (0.25 + 0.75 * c1) * Math.exp(-Math.hypot(xx - bx, yx - 40) / 90.0);
         c = Capes.add(c, 12572927, lit * 0.9 + fl * 0.08);
         double hill = 0.9 + 0.03 * Math.sin(u * 8.0 + 1.0) + 0.015 * Math.sin(u * 23.0);
         if (v > hill) {
            c = Capes.add(Capes.mix(329483, 131844, (v - hill) * 8.0), 9087231, fl * 0.12);
            hgt[yx * 160 + xx] = 0.0;
         }

         return c;
      });
      relief(img, hgt, 7.0, 0.08 + fl * 0.3, 0.5, 0.2);
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
         g.draw(new java.awt.geom.Line2D.Double(x, y, x + len * 0.12, y + len));
      }

      if (fl > 0.0) {
         Random br = new Random(boltA ? 5L : 9L);
         Path2D p = new Double();
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
         Capes.addDot(img, x, 230.4, 26.0, 11064575, 0.45 * fl);
      }
   }

   static void carbon(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      int C = 8;
      double[] hgt = new double[40960];
      Capes.field(img, (u, vx, x, yx) -> {
         int cxx = x / 8;
         int cyx = yx / 8;
         double fx = (x % 8 + 0.5) / 8.0;
         double fy = (yx % 8 + 0.5) / 8.0;
         boolean horiz = (cxx + cyx & 3) < 2;
         double across = horiz ? fy : fx;
         double along = horiz ? fx : fy;
         double tow = Math.pow(Math.sin(Math.PI * across), 0.6);
         hgt[yx * 160 + x] = tow * 0.6 + 0.08 * Math.sin(Math.PI * along);
         double fib = 0.93 + 0.07 * Math.sin(across * Math.PI * 7.0 + along * 0.6);
         double lightDir = horiz ? 0.55 + 0.45 * Math.sin(u * 5.0 - vx * 3.0 + 1.0) : 0.55 + 0.45 * Math.sin(u * 4.0 + vx * 2.0 + 3.0);
         return Capes.mix(789776, 4869720, tow * fib * (0.35 + 0.5 * lightDir));
      });
      relief(img, hgt, 6.0, 0.3, 0.8, 0.0);
      int[] p = Capes.px(img);

      for (int y = 0; y < 256; y++) {
         for (int x = 0; x < 160; x++) {
            double u = (x + 0.5) / 160.0;
            double v = (y + 0.5) / 256.0;
            double coat = Math.exp(-Math.pow((u * 0.9 - v * 0.55 - 0.12) / 0.1, 2.0)) * 0.16
               + Math.exp(-Math.pow((u * 0.9 - v * 0.55 + 0.35) / 0.2, 2.0)) * 0.08;
            p[y * 160 + x] = Capes.add(p[y * 160 + x], 16777215, coat);
         }
      }

      for (int s = 0; s < 2; s++) {
         double y = s == 0 ? 40.0 : 212.0;
         g.setPaint(new GradientPaint(0.0F, (float)y - 5.0F, new Color(16730698), 0.0F, (float)y + 5.0F, new Color(7997962)));
         g.fill(new java.awt.geom.Rectangle2D.Double(0.0, y - 5.0, 160.0, 10.0));
         g.setColor(Capes.col(16777215, 0.45));
         g.fill(new java.awt.geom.Rectangle2D.Double(0.0, y - 5.0, 160.0, 1.2));
         g.setColor(Capes.col(0, 0.45));
         g.fill(new java.awt.geom.Rectangle2D.Double(0.0, y + 5.0, 160.0, 1.5));
         g.setColor(Capes.col(16726586, 0.8));
         g.fill(new java.awt.geom.Rectangle2D.Double(0.0, y + 8.0, 160.0, 2.0));
      }

      double cx = 80.0;
      double cy = 124.0;
      Capes.dot(g, cx + 3.0, cy + 5.0, 60.0, 0, 0.6);
      g.setPaint(
         new LinearGradientPaint(
            (float)cx - 34.0F,
            (float)cy - 34.0F,
            (float)cx + 34.0F,
            (float)cy + 34.0F,
            new float[]{0.0F, 0.45F, 0.55F, 1.0F},
            new Color[]{new Color(16777215), new Color(10660538), new Color(4870752), new Color(14212840)}
         )
      );
      g.fill(new java.awt.geom.Ellipse2D.Double(cx - 34.0, cy - 34.0, 68.0, 68.0));
      g.setPaint(
         new RadialGradientPaint((float)cx - 6.0F, (float)cy - 10.0F, 40.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(3290172), new Color(658190)})
      );
      g.fill(new java.awt.geom.Ellipse2D.Double(cx - 29.0, cy - 29.0, 58.0, 58.0));
      Art.brick(g, cx - 24.0, cy - 27.0, 48.0, new Color(14876683));
      g.setColor(Capes.col(16777215, 0.55));
      g.setStroke(new BasicStroke(1.4F));
      g.draw(new java.awt.geom.Arc2D.Double(cx - 32.0, cy - 32.0, 64.0, 64.0, 60.0, 90.0, 0));
      g.setColor(Capes.col(16777215, 0.18));
      g.fill(new java.awt.geom.Arc2D.Double(cx - 27.0, cy - 27.0, 54.0, 54.0, 30.0, 120.0, 1));
   }

   static void fireflies(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      Capes.field(img, (u, v, xx, yx) -> {
         int cx = Capes.ramp(v, 200718, 535076, 1198650, 799780);
         cx = Capes.add(cx, 12120280, Math.exp(-Math.hypot(u - 0.5, (v - 0.1) * 0.8) / 0.16) * 0.38);
         double fog = Capes.fbm(u * 3.0 + t * 3.0, v * 5.0, 3, 0, 4, 55) * Math.exp(-Math.pow((v - 0.72) / 0.14, 2.0));
         return Capes.add(cx, 8042664, fog * 0.4);
      });
      int[] pp = Capes.px(img);

      for (int y = 0; y < 256; y++) {
         for (int x = 0; x < 160; x++) {
            double u = (x + 0.5) / 160.0;
            double v = (y + 0.5) / 256.0;
            double a = Math.atan2(u - 0.5, v - 0.02);
            double shaft = Math.pow(0.5 + 0.5 * Math.sin(a * 38.0 + 1.3), 8.0) * Math.exp(-v * 2.6) * 0.1 * (0.7 + 0.3 * Math.sin(ph + a * 5.0));
            pp[y * 160 + x] = Capes.add(pp[y * 160 + x], 13168856, shaft);
         }
      }

      Random rn = new Random(15L);
      int[] lc = new int[]{1727050, 800816, 267794};
      double[] hgt = new double[40960];

      for (int L = 0; L < 3; L++) {
         int cnt = L == 0 ? 8 : 5 - L;

         for (int i = 0; i < cnt; i++) {
            double x = L == 0 ? (i + 0.3 + rn.nextDouble() * 0.4) * 160.0 / cnt : rn.nextDouble() * 160.0;
            double w = 5 + L * 5 + rn.nextDouble() * 4.0;

            for (int y = 0; y < 256; y++) {
               double half = w / 2.0 + w * 0.2 * y / 256.0;

               for (int xx = (int)Math.max(0.0, x - half); xx < Math.min(160.0, x + half); xx++) {
                  double f = (xx + 0.5 - x) / half;
                  double round = Math.sqrt(Math.max(0.0, 1.0 - f * f));
                  double bark = Capes.fbm(xx * 0.35, y * 0.06, 0, 0, 3, 90 + L);
                  int c = Capes.shade(lc[L], 0.7 + 0.45 * round * (0.6 + 0.4 * bark));
                  pp[y * 160 + xx] = 0xFF000000 | c;
                  hgt[y * 160 + xx] = round * 0.5 + bark * 0.25 + L * 0.1;
               }
            }
         }

         g.setColor(new Color(lc[L]));

         for (int i = 0; i < 16; i++) {
            double x = rn.nextDouble() * 160.0;
            double y = -10.0 + rn.nextDouble() * (30 + L * 10);
            double r = 14.0 + rn.nextDouble() * 14.0;
            g.fill(new java.awt.geom.Ellipse2D.Double(x - r, y - r * 0.7, r * 2.0, r * 1.4));
         }
      }

      relief(img, hgt, 6.0, 0.1, 0.5, 0.0);
      g.setStroke(new BasicStroke(1.4F, 1, 1));

      for (int i = 0; i < 160; i++) {
         double x = rn.nextDouble() * 160.0;
         double h = 10.0 + rn.nextDouble() * 24.0;
         double bend = rn.nextGaussian() * 5.0 + Math.sin(ph + x * 0.05) * 1.5;
         g.setColor(new Color(Capes.mix(200712, 2779706, rn.nextDouble() * 0.7)));
         g.draw(new java.awt.geom.QuadCurve2D.Double(x, 256.0, x + bend * 0.3, 256.0 - h * 0.6, x + bend, 256.0 - h));
      }

      for (int i = 0; i < 28; i++) {
         double x0 = 12.0 + rn.nextDouble() * 136.0;
         double y0 = 50.0 + rn.nextDouble() * 190.0;
         int j = 1 + rn.nextInt(2);
         double s1 = rn.nextDouble() * 6.28;
         double s2 = rn.nextDouble() * 6.28;
         double s3 = rn.nextDouble() * 6.28;
         double x = x0 + 8.0 * Math.cos(ph * j + s1) + 3.0 * Math.sin(2.0 * ph + s2);
         double y = y0 + 6.0 * Math.sin(ph * j + s3);
         double blink = Math.pow(0.5 + 0.5 * Math.sin(ph * (1 + rn.nextInt(2)) + s2), 2.2);
         Capes.addDot(img, x, y, 16.0, 10157882, 0.22 * blink + 0.02);
         Capes.addDot(img, x, y, 6.0, 14221162, 0.5 * blink + 0.04);
         Capes.addDot(img, x, y, 2.0, 16777184, 1.0 * blink + 0.1);
      }
   }
}
