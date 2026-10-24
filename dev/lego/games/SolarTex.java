package dev.lego.games;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.RoundRectangle2D.Double;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

final class SolarTex {
   static final ForkJoinPool POOL = new ForkJoinPool(Math.max(1, Math.min(3, Runtime.getRuntime().availableProcessors() - 1)), px -> {
      ForkJoinWorkerThread tx = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(px);
      tx.setDaemon(true);
      tx.setName("solar-gen-" + tx.getPoolIndex());
      tx.setPriority(2);
      return tx;
   }, null, false);
   private static final int[] PERM = new int[512];
   static final byte[] HASH = new byte[65536];

   private SolarTex() {
   }

   static void rows(int h, IntConsumer row) {
      try {
         POOL.submit(() -> IntStream.range(0, h).parallel().forEach(row)).get();
      } catch (Exception var4) {
         for (int y = 0; y < h; y++) {
            row.accept(y);
         }
      }
   }

   private static double fade(double t) {
      return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
   }

   private static double lerp(double t, double a, double b) {
      return a + t * (b - a);
   }

   private static double grad(int h, double x, double y, double z) {
      int hh = h & 15;
      double u = hh < 8 ? x : y;
      double v = hh < 4 ? y : (hh != 12 && hh != 14 ? z : x);
      return ((hh & 1) == 0 ? u : -u) + ((hh & 2) == 0 ? v : -v);
   }

   static double noise(double x, double y, double z) {
      int X = (int)Math.floor(x);
      int Y = (int)Math.floor(y);
      int Z = (int)Math.floor(z);
      x -= X;
      y -= Y;
      z -= Z;
      X &= 255;
      Y &= 255;
      Z &= 255;
      double u = fade(x);
      double v = fade(y);
      double w = fade(z);
      int A = PERM[X] + Y;
      int AA = PERM[A] + Z;
      int AB = PERM[A + 1] + Z;
      int B = PERM[X + 1] + Y;
      int BA = PERM[B] + Z;
      int BB = PERM[B + 1] + Z;
      return lerp(
         w,
         lerp(v, lerp(u, grad(PERM[AA], x, y, z), grad(PERM[BA], x - 1.0, y, z)), lerp(u, grad(PERM[AB], x, y - 1.0, z), grad(PERM[BB], x - 1.0, y - 1.0, z))),
         lerp(
            v,
            lerp(u, grad(PERM[AA + 1], x, y, z - 1.0), grad(PERM[BA + 1], x - 1.0, y, z - 1.0)),
            lerp(u, grad(PERM[AB + 1], x, y - 1.0, z - 1.0), grad(PERM[BB + 1], x - 1.0, y - 1.0, z - 1.0))
         )
      );
   }

   static double fbm(double x, double y, double z, int oct) {
      double s = 0.0;
      double a = 0.5;
      double f = 1.0;

      for (int i = 0; i < oct; i++) {
         s += a * noise(x * f, y * f, z * f);
         a *= 0.5;
         f *= 2.03;
      }

      return s;
   }

   static double ridge(double x, double y, double z, int oct) {
      double s = 0.0;
      double a = 0.5;
      double f = 1.0;
      double w = 1.0;

      for (int i = 0; i < oct; i++) {
         double n = 1.0 - Math.abs(noise(x * f, y * f, z * f));
         n *= n;
         n *= w;
         w = Math.min(1.0, n * 2.0);
         s += a * n;
         a *= 0.5;
         f *= 2.07;
      }

      return s;
   }

   static double sstep(double a, double b, double x) {
      double t = Math.max(0.0, Math.min(1.0, (x - a) / (b - a)));
      return t * t * (3.0 - 2.0 * t);
   }

   static double clamp01(double x) {
      return x < 0.0 ? 0.0 : (x > 1.0 ? 1.0 : x);
   }

   static int mix(int a, int b, double t) {
      if (t <= 0.0) {
         return a;
      } else if (t >= 1.0) {
         return a & 0xFF000000 | b & 16777215;
      } else {
         int ar = a >> 16 & 0xFF;
         int ag = a >> 8 & 0xFF;
         int ab = a & 0xFF;
         int br = b >> 16 & 0xFF;
         int bg = b >> 8 & 0xFF;
         int bb = b & 0xFF;
         return a & 0xFF000000 | (int)(ar + (br - ar) * t) << 16 | (int)(ag + (bg - ag) * t) << 8 | (int)(ab + (bb - ab) * t);
      }
   }

   static int shade(int c, double k) {
      int r = (int)Math.min(255.0, (c >> 16 & 0xFF) * k);
      int g = (int)Math.min(255.0, (c >> 8 & 0xFF) * k);
      int b = (int)Math.min(255.0, (c & 0xFF) * k);
      return c & 0xFF000000 | Math.max(0, r) << 16 | Math.max(0, g) << 8 | Math.max(0, b);
   }

   static int ramp(double t, double[] pos, int[] col) {
      if (t <= pos[0]) {
         return col[0];
      } else {
         for (int i = 1; i < pos.length; i++) {
            if (t <= pos[i]) {
               return mix(col[i - 1], col[i], (t - pos[i - 1]) / (pos[i] - pos[i - 1]));
            }
         }

         return col[col.length - 1];
      }
   }

   static SolarPlanet.Maps generate(int type, long seed, int W, int H, boolean inhabited, boolean full) {
      SolarPlanet.Maps m = new SolarPlanet.Maps(W, H, type != 3 && type != 0 && type != 8, type == 5);
      double ox = seed % 1000L * 0.137;
      double oy = seed / 1000L % 1000L * 0.071;
      double oz = seed / 7L % 997L * 0.113;
      float[] hs = new float[W * H];
      float[] ms = new float[W * H];
      float[] ds = new float[W * H];
      double[] cl = new double[W];
      double[] sl = new double[W];

      for (int x = 0; x < W; x++) {
         double lon = (x + 0.5) / W * Math.PI * 2.0;
         cl[x] = Math.cos(lon);
         sl[x] = Math.sin(lon);
      }

      int oct = W >= 1024 ? 8 : (W >= 512 ? 6 : 5);
      rows(H, y -> {
         double lat = (0.5 - (y + 0.5) / H) * Math.PI;
         double cy = Math.cos(lat);
         double py = Math.sin(lat);

         for (int x = 0; x < W; x++) {
            double px = cy * cl[x];
            double pz = cy * sl[x];
            int i = y * W + x;
            double mo = 0.0;
            double h;
            double de;
            switch (type) {
               case 1:
               case 6: {
                  double w = fbm(px * 1.1 + 5.3 + ox, py * 1.1 + 1.7, pz * 1.1 + 9.2 + oz, 3) * 0.6;
                  h = fbm(px * 1.35 + w + ox, py * 1.35 - w * 0.5 + oy, pz * 1.35 + w * 0.8 + oz, oct);
                  double rr = ridge(px * 3.1 + oy, py * 3.1, pz * 3.1 + ox, 5);
                  h += Math.max(0.0, h) * rr * 0.45;
                  mo = fbm(px * 2.3 + 11.0 + oz, py * 2.3, pz * 2.3 - 4.0, 4);
                  de = fbm(px * 11.0 + 3.0, py * 11.0, pz * 11.0 + 8.0 + ox, 3);
                  break;
               }
               case 2: {
                  double w = fbm(px * 1.4 + ox, py * 1.4 + 3.0, pz * 1.4, 3) * 0.5;
                  h = fbm(px * 1.6 + w + oy, py * 1.6, pz * 1.6 - w, oct);
                  double can = ridge(px * 2.4 + 7.0, py * 2.4, pz * 2.4 + 1.0, 5);
                  h -= Math.pow(can, 3.0) * 0.35;
                  mo = fbm(px * 2.8 + 20.0, py * 2.8, pz * 2.8 + ox, 4);
                  de = fbm(px * 14.0, py * 14.0 + 5.0, pz * 14.0, 3);
                  break;
               }
               case 3: {
                  double w = fbm(px * 2.2 + ox, py * 2.2, pz * 2.2, 4);
                  h = py * 5.5 + w * 1.1 + fbm(px * 3.0, py * 18.0, pz * 3.0, 4) * 0.5;
                  mo = fbm(px * 5.0 + w, py * 30.0, pz * 5.0 - w, 4);
                  de = fbm(px * 9.0, py * 9.0 + 4.0, pz * 9.0, 3);
                  break;
               }
               case 4:
                  h = fbm(px * 1.5 + ox, py * 1.5, pz * 1.5 + oz, oct) * 0.7;
                  mo = ridge(px * 3.5 + 2.0, py * 3.5 + oy, pz * 3.5, 6);
                  de = fbm(px * 12.0, py * 12.0, pz * 12.0, 3);
                  break;
               case 5:
                  h = fbm(px * 1.7 + ox, py * 1.7, pz * 1.7 + oz, oct);
                  mo = ridge(px * 2.6 + 3.0 + oy, py * 2.6, pz * 2.6 - 2.0, 6);
                  de = fbm(px * 13.0, py * 13.0 + 2.0, pz * 13.0, 3);
                  break;
               case 7: {
                  double w = fbm(px * 1.2 + ox, py * 1.2, pz * 1.2 + 2.0, 3) * 0.8;
                  h = fbm(px * 1.5 + w, py * 1.5 + oy, pz * 1.5 - w, oct) * 0.8;
                  double dune = ridge(px * 7.0 + w * 3.0, py * 16.0 + w * 2.0, pz * 7.0 - w * 3.0, 3);
                  mo = dune;
                  h -= Math.pow(ridge(px * 2.1 + 9.0, py * 2.1, pz * 2.1, 5), 4.0) * 0.4;
                  de = fbm(px * 15.0, py * 15.0, pz * 15.0 + 1.0, 3);
                  break;
               }
               case 8:
                  h = fbm(px * 1.8 + ox, py * 1.8 + oy, pz * 1.8, oct) * 0.6;
                  mo = fbm(px * 1.2 + 40.0 + oz, py * 1.2, pz * 1.2, 4);
                  de = fbm(px * 16.0, py * 16.0, pz * 16.0 + 9.0, 3);
                  break;
               default:
                  double g = Math.abs(noise(px * 38.0 + ox, py * 38.0, pz * 38.0)) + 0.5 * Math.abs(noise(px * 80.0, py * 80.0, pz * 80.0 + 3.0));
                  h = g;
                  mo = fbm(px * 3.0 + ox, py * 3.0, pz * 3.0, 4);
                  de = fbm(px * 9.0, py * 9.0, pz * 9.0 + 5.0, 3);
            }

            hs[i] = (float)h;
            ms[i] = (float)mo;
            ds[i] = (float)de;
         }
      });
      if (type == 8 || type == 2 || type == 4) {
         Random r = new Random(seed * 31L + 7L);
         int n = type == 8 ? 900 : (type == 2 ? 260 : 90);

         for (int k = 0; k < n; k++) {
            double lat = Math.asin(r.nextDouble() * 2.0 - 1.0);
            double lon = r.nextDouble() * Math.PI * 2.0;
            double rad = W / 2048.0 * (3.0 + Math.pow(r.nextDouble(), 5.0) * (type == 8 ? 90 : 55));
            stampCrater(hs, W, H, lon, lat, rad, 0.25 + r.nextDouble() * 0.2);
         }
      }

      double kShade = W / 2048.0;
      rows(H, y -> {
         double lat = (0.5 - (y + 0.5) / H) * Math.PI;
         double alat = Math.abs(lat);
         double cyl = Math.cos(lat);
         double py = Math.sin(lat);
         int ym = Math.max(0, y - 1);
         int yp = Math.min(H - 1, y + 1);

         for (int x = 0; x < W; x++) {
            int i = y * W + x;
            double px = cyl * cl[x];
            double pz = cyl * sl[x];
            double h = hs[i];
            double mo = ms[i];
            double de = ds[i];
            double gx = hs[y * W + (x + 1 & W - 1)] - hs[y * W + (x - 1 & W - 1)];
            double gy = hs[yp * W + x] - hs[ym * W + x];
            double sh = 1.0 + (-gx * 0.8 + gy * 0.6) * 9.0 / Math.max(0.25, kShade * 2.2);
            int spec = 0;
            int city = 0;
            int heat0 = 0;
            double hb = h;
            int c;
            switch (type) {
               case 1:
                  double seax = 0.02;
                  boolean ice = alat > 1.22 - de * 0.25 + Math.max(0.0, h) * 0.3;
                  if (h < seax) {
                     double d = seax - h;
                     c = ramp(d, new double[]{0.0, 0.025, 0.12, 0.35}, new int[]{-12933192, -14912346, -15777922, -16245173});
                     c = mix(c, -16111014, clamp01(de * 0.4 + 0.1));
                     spec = 230;
                     sh = 1.0;
                     if (ice) {
                        c = mix(-2234128, -4600612, clamp01(de + 0.5));
                        spec = 60;
                     }
                  } else {
                     double e = h - seax;
                     double temp = 1.0 - alat / 1.45 - e * 1.4;
                     double wet = mo + 0.25 - Math.abs(Math.abs(lat) - 0.42) * 0.9 + (alat < 0.2 ? 0.25 : 0.0);
                     int green = mix(-11630028, -14726630, clamp01(wet * 1.6 + 0.35 + de * 0.4));
                     int dry = mix(-3561622, -5799348, clamp01(de + 0.5));
                     c = mix(dry, green, sstep(-0.05, 0.2, wet));
                     if (temp < 0.35) {
                        c = mix(c, -8619426, sstep(0.35, 0.18, temp));
                     }

                     if (e < 0.012) {
                        c = mix(c, -2504806, 0.6);
                     }

                     c = mix(c, mix(-9740978, -7042692, clamp01(de + 0.5)), sstep(0.2, 0.34, e));
                     c = mix(c, -723208, sstep(0.36, 0.45, e + (1.0 - temp) * 0.1));
                     if (ice) {
                        c = mix(-1445643, -3483420, clamp01(de + 0.5));
                     }

                     if (inhabited && !ice && temp > 0.3 && e < 0.26) {
                        double cn = fbm(px * 7.0 + 3.0, py * 7.0, pz * 7.0 + 1.7, 3) + 0.5 * noise(px * 30.0, py * 30.0, pz * 30.0 + 4.2);
                        double v = cn + (e < 0.04 ? 0.22 : 0.0) + wet * 0.15 - 0.18;
                        if (v > 0.0) {
                           city = (int)Math.min(255.0, Math.pow(v * 3.2, 1.6) * 255.0);
                        }
                     }
                  }

                  hb = h;
                  break;
               case 2:
                  c = ramp(clamp01(h * 1.2 + 0.5), new double[]{0.0, 0.35, 0.6, 1.0}, new int[]{-10869226, -6666970, -3905478, -2321312});
                  c = mix(c, -12834268, sstep(0.1, 0.35, mo) * 0.7);
                  c = mix(c, -2056080, clamp01(de * 0.6) * 0.3);
                  if (alat > 1.32 - de * 0.2) {
                     c = mix(-856342, -2568250, clamp01(de + 0.5));
                  }

                  if (inhabited && alat < 0.6) {
                     double cn = noise(px * 10.0 + 5.0, py * 10.0, pz * 10.0 + 0.3);
                     double d2 = noise(px * 46.0, py * 46.0, pz * 46.0 + 3.3);
                     if (cn > 0.5 && d2 > 0.05) {
                        city = (int)Math.min(255.0, (cn - 0.5) * 1400.0 * (d2 + 0.3));
                     }
                  }
                  break;
               case 3:
                  double b = Math.sin(h * 2.2) * 0.5 + 0.5;
                  double b2 = Math.sin(h * 5.3 + 1.0) * 0.5 + 0.5;
                  c = mix(-1451848, -4618922, b);
                  c = mix(c, -7710150, b2 * 0.35);
                  c = mix(c, -659747, clamp01(mo * 1.2) * 0.4);
                  c = shade(c, 0.94 + de * 0.12);
                  double lonx = (x + 0.5) / W * Math.PI * 2.0;
                  double dx = Math.atan2(Math.sin(lonx - 1.9), Math.cos(lonx - 1.9)) / 0.33;
                  double dy = (lat + 0.38) / 0.12;
                  double dd = Math.sqrt(dx * dx + dy * dy);
                  if (dd < 1.6) {
                     double sw = Math.sin(dd * 9.0 + Math.atan2(dy, dx) * 2.0) * 0.5 + 0.5;
                     c = mix(c, mix(-4041684, -1531792, sw * 0.5 + dd * 0.3), sstep(1.6, 0.6, dd));
                  }

                  sh = 1.0;
                  break;
               case 4:
                  c = mix(-1379334, -5650208, clamp01(h * 1.3 + 0.5));
                  c = mix(c, -13671017, sstep(0.72, 0.95, mo) * 0.85);
                  c = mix(c, -1, clamp01(de * 0.5) * 0.4);
                  spec = 70;
                  if (inhabited) {
                     double cn = noise(px * 16.0 + 9.0, py * 16.0, pz * 16.0 + 1.1);
                     if (cn > 0.6 && alat < 1.1) {
                        city = (int)Math.min(255.0, (cn - 0.6) * 1300.0);
                     }
                  }
                  break;
               case 5:
                  c = mix(-14477291, -11912653, clamp01(h + 0.5));
                  c = shade(c, 0.85 + de * 0.4);
                  double crack = sstep(0.8, 0.98, mo);
                  double lake = sstep(-0.12, -0.3, h);
                  double hot = Math.max(crack, lake);
                  if (hot > 0.02) {
                     c = mix(c, -8774136, hot);
                     heat0 = (int)(hot * (170.0 + de * 80.0));
                  }
                  break;
               case 6:
                  double sea = 0.33;
                  if (h < sea) {
                     double d = sea - h;
                     c = ramp(d, new double[]{0.0, 0.03, 0.18, 0.6}, new int[]{-12203568, -14905927, -15839594, -16375202});
                     c = mix(c, -16040070, clamp01(mo * 0.8 + 0.3));
                     spec = 240;
                     sh = 1.0;
                     if (inhabited) {
                        double cn = noise(px * 16.0, py * 16.0, pz * 16.0 + 8.1) + noise(px * 56.0, py * 56.0, pz * 56.0 + 2.2) * 0.5;
                        if (cn > 0.42 && alat < 1.0) {
                           city = (int)Math.min(255.0, (cn - 0.42) * 900.0);
                        }
                     }
                  } else {
                     double e = h - sea;
                     c = e < 0.015 ? -1648730 : mix(-12678086, -14000602, clamp01(de + 0.5));
                     if (inhabited) {
                        city = (int)Math.min(255.0, 120.0 + de * 200.0);
                     }
                  }

                  if (alat > 1.3 - de * 0.2) {
                     c = mix(-1510922, -3877149, clamp01(de + 0.5));
                     spec = 40;
                  }
                  break;
               case 7:
                  c = ramp(clamp01(h + 0.55), new double[]{0.0, 0.35, 0.65, 1.0}, new int[]{-8764122, -4818366, -2247826, -1060455});
                  c = shade(c, 0.86 + mo * 0.28);
                  boolean oasis = h < -0.22 && de > 0.0;
                  if (oasis) {
                     c = mix(c, -11567560, 0.7);
                  }

                  if (alat > 1.38) {
                     c = mix(c, -923169, 0.8);
                  }

                  if (inhabited && alat < 1.0) {
                     double cn = noise(px * 8.0 + 2.0, py * 8.0, pz * 8.0 + 6.6) + (h < -0.1 ? 0.3 : 0.0);
                     if (cn > 0.35) {
                        city = (int)Math.min(255.0, (cn - 0.35) * 700.0);
                     }
                  }
                  break;
               case 8:
                  c = mix(-6514027, -4343117, clamp01(h + 0.5));
                  c = mix(c, -11645614, sstep(0.05, 0.3, mo) * 0.85);
                  c = shade(c, 0.92 + de * 0.25);
                  if (inhabited) {
                     double cn = noise(px * 13.0 + 1.0, py * 13.0, pz * 13.0 + 7.3);
                     if (cn > 0.62 && alat < 1.0) {
                        city = (int)Math.min(255.0, (cn - 0.62) * 1500.0);
                     }
                  }
                  break;
               default:
                  c = ramp(clamp01(h * 0.9), new double[]{0.0, 0.3, 0.6, 1.0}, new int[]{-4703732, -1015778, -16310, -3912});
                  c = mix(c, -2352, clamp01(de * 0.8) * 0.4);
                  double spot = sstep(0.36, 0.46, mo);
                  if (spot > 0.0) {
                     c = mix(c, -12971514, spot * 0.85);
                  }

                  sh = 1.0;
            }

            if (type != 3 && type != 0) {
               c = shade(c, Math.max(0.55, Math.min(1.4, sh)));
            }

            m.alb[i] = spec << 24 | c & 16777215;
            if (m.alb[i] >>> 24 == 1) {
               m.alb[i] = 33554432 | c & 16777215;
            }

            m.hgt[i] = (byte)Math.max(0, Math.min(255, (int)((hb + 1.0) * 127.5)));
            m.city[i] = (byte)city;
            if (m.heat0 != null) {
               m.heat0[i] = (byte)heat0;
               m.heat[i] = (byte)heat0;
            }
         }
      });
      if (type == 1) {
         m.sea = -126;
      } else if (type == 6) {
         m.sea = -87;
      } else {
         m.sea = 0;
      }

      if (m.cloud != null) {
         int cW = m.cW;
         int cH = m.cH;
         double cover = type == 6 ? 0.1 : (type == 1 ? 0.02 : (type == 7 ? -0.28 : (type == 2 ? -0.2 : (type == 5 ? -0.05 : -0.12))));
         rows(cH, y -> {
            double lat = (0.5 - (y + 0.5) / cH) * Math.PI;
            double cy = Math.cos(lat);
            double py = Math.sin(lat);

            for (int x = 0; x < cW; x++) {
               double lonx = (x + 0.5) / cW * Math.PI * 2.0;
               double px = cy * Math.cos(lonx);
               double pz = cy * Math.sin(lonx);
               double w = fbm(px * 1.7 + 2.0 + oz, py * 2.4 + 7.0, pz * 1.7 + 1.0, 3) * 1.1;
               double v = fbm(px * 2.6 + w, py * 4.2 - w * 0.4, pz * 2.6 - w, W >= 1024 ? 7 : 5);
               v += Math.cos(lat * 6.0) * 0.06 + cover;
               double a = sstep(0.0, 0.34, v);
               m.cloud[y * cW + x] = (byte)(a * 235.0);
            }
         });
      }

      m.finish();
      return m;
   }

   private static void stampCrater(float[] hs, int W, int H, double lon, double lat, double rad, double depth) {
      int cy = (int)((0.5 - lat / Math.PI) * H);
      double cx = lon / (Math.PI * 2) * W;
      int ry = (int)Math.ceil(rad * 1.6);
      double dh = depth * Math.min(1.0, rad / (W / 2048.0 * 30.0) + 0.25) * 0.5;

      for (int y = Math.max(0, cy - ry); y <= Math.min(H - 1, cy + ry); y++) {
         double la = (0.5 - (y + 0.5) / H) * Math.PI;
         double cl = Math.max(0.05, Math.cos(la));
         double dy = y - cy;
         int rx = (int)Math.min((double)(W / 2), Math.ceil(ry / cl));

         for (int xx = (int)cx - rx; xx <= (int)cx + rx; xx++) {
            double dx = (xx - cx) * cl;
            double d = Math.sqrt(dx * dx + dy * dy) / rad;
            if (!(d > 1.6)) {
               int i = y * W + (xx & W - 1);
               double v = d < 1.0 ? -(1.0 - d * d) * dh + 0.35 * dh : 0.35 * dh * Math.exp(-(d - 1.0) * (d - 1.0) / 0.04);
               if (d < 1.0) {
                  v = Math.min(v, 0.35 * dh * Math.exp(-(d - 1.0) * (d - 1.0) / 0.04) - (1.0 - d * d) * dh);
               }

               hs[i] += (float)v;
            }
         }
      }
   }

   static int[][] nebula(int w, int h, long seed) {
      int[] px = new int[w * h];
      double s = 2.6 / h;
      double o = seed % 97L * 1.37;
      rows(h, y -> {
         for (int x = 0; x < w; x++) {
            double fx = x * s + o;
            double fy = y * s;
            double wx = fbm(fx * 0.7, fy * 0.7, 3.3, 3) * 1.3;
            double n1 = fbm(fx + wx, fy - wx * 0.5, 0.5, 6);
            double n2 = fbm(fx * 1.6 - 4.0 + wx, fy * 1.6 + 2.0, 7.1, 5);
            double n3 = fbm(fx * 3.1 + 9.0, fy * 3.1 + wx, 1.9, 4);
            double band = Math.exp(-Math.pow(((double)y / h - 0.42 - ((double)x / w - 0.5) * 0.35 + n1 * 0.2) / 0.28, 2.0));
            double a = sstep(-0.05, 0.55, n1 + band * 0.35) * (0.45 + band * 0.55);
            double b = sstep(0.0, 0.5, n2) * 0.8;
            int c = -16579574;
            c = mix(c, -15462086, 0.8);
            c = mix(c, -14018987, a * 0.75);
            c = mix(c, -15840658, b * a * 0.8);
            c = mix(c, -7721365, sstep(0.25, 0.7, n2 + n1 * 0.5) * a * 0.55);
            c = mix(c, -2056008, sstep(0.45, 0.85, n1 + n3 * 0.3) * a * 0.22);
            double dust = sstep(0.1, 0.45, n3 - n1 * 0.4) * band;
            c = mix(c, -16645627, dust * 0.65);
            double vig = Math.pow(Math.hypot((double)x / w - 0.5, ((double)y / h - 0.5) * 1.2), 2.0) * 1.2;
            c = shade(c, 1.0 - Math.min(0.6, vig));
            int hsh = HASH[x * 31 + y * 131 & 65535] & 7;
            px[y * w + x] = (c & 16777215) + (hsh <= 1 ? 65793 * hsh : 0) | 0xFF000000;
         }
      });
      return new int[][]{px, {w, h}};
   }

   static int[][] stars(int size, int count, float bright, long seed) {
      int[] px = new int[size * size];
      Random r = new Random(seed);

      for (int k = 0; k < count; k++) {
         float x = r.nextFloat() * size;
         float y = r.nextFloat() * size;
         float rad = 0.45F + (float)Math.pow(r.nextFloat(), 6.0) * 1.5F;
         float a = bright * (0.25F + r.nextFloat() * 0.75F);
         int col = r.nextFloat() < 0.15F ? 16767416 : (r.nextFloat() < 0.3F ? 12111103 : 16777215);
         int R = (int)Math.ceil(rad * 2.5F);

         for (int yy = -R; yy <= R; yy++) {
            for (int xx = -R; xx <= R; xx++) {
               int X = (int)x + xx & size - 1;
               int Y = (int)y + yy & size - 1;
               double dx = (int)x + xx + 0.5 - x;
               double dy = (int)y + yy + 0.5 - y;
               double v = a * Math.exp(-(dx * dx + dy * dy) / (rad * rad * 0.9));
               if (!(v < 0.01)) {
                  int i = Y * size + X;
                  int old = px[i] >>> 24;
                  int na = (int)Math.min(255.0, old + v * 255.0);
                  px[i] = na << 24 | col;
               }
            }
         }
      }

      return new int[][]{px, {size, size}};
   }

   static int[][] flare(int d) {
      int[] px = new int[d * d];
      double c = d / 2.0;

      for (int y = 0; y < d; y++) {
         for (int x = 0; x < d; x++) {
            double dx = (x + 0.5 - c) / c;
            double dy = (y + 0.5 - c) / c;
            double r = Math.sqrt(dx * dx + dy * dy);
            double v = Math.pow(Math.max(0.0, 1.0 - r), 3.0) * 0.9;
            v += Math.exp(-Math.abs(dy) * 60.0) * Math.pow(Math.max(0.0, 1.0 - Math.abs(dx)), 2.0) * 0.8;
            v += Math.exp(-Math.abs(dx) * 60.0) * Math.pow(Math.max(0.0, 1.0 - Math.abs(dy)), 2.0) * 0.8;
            v += Math.exp(-r * r * 90.0) * 0.8;
            px[y * d + x] = (int)(Math.min(1.0, v) * 255.0) << 24 | 16777215;
         }
      }

      return new int[][]{px, {d, d}};
   }

   static int[][] corona(int d, long seed) {
      int[] px = new int[d * d];
      double c = d / 2.0;
      rows(d, y -> {
         for (int x = 0; x < d; x++) {
            double dx = (x + 0.5 - c) / c;
            double dy = (y + 0.5 - c) / c;
            double r = Math.sqrt(dx * dx + dy * dy);
            if (!(r >= 1.0)) {
               double ang = Math.atan2(dy, dx);
               double st = fbm(Math.cos(ang) * 3.0 + seed, Math.sin(ang) * 3.0, r * 1.5, 4);
               double st2 = Math.abs(noise(Math.cos(ang) * 14.0, Math.sin(ang) * 14.0, seed * 0.3));
               double base = Math.pow(1.0 - r, 2.6);
               double v = base * (0.55 + 0.45 * sstep(-0.2, 0.4, st)) + Math.pow(1.0 - r, 5.0) * 0.4;
               v *= 0.7 + 0.5 * st2;
               px[y * d + x] = (int)(Math.min(1.0, v) * 255.0) << 24 | 16777215;
            }
         }
      });
      return new int[][]{px, {d, d}};
   }

   static int[][] orbitRing(int d, float thick) {
      int[] px = new int[d * d];
      double c = d / 2.0;
      double R = c - thick * 2.0F;

      for (int y = 0; y < d; y++) {
         for (int x = 0; x < d; x++) {
            double r = Math.hypot(x + 0.5 - c, y + 0.5 - c);
            double v = Math.exp(-Math.pow((r - R) / thick, 2.0));
            if (v > 0.003) {
               px[y * d + x] = (int)(v * 255.0) << 24 | 16777215;
            }
         }
      }

      return new int[][]{px, {d, d}};
   }

   static int[][] softRing(int d) {
      int[] px = new int[d * d];
      double c = d / 2.0;

      for (int y = 0; y < d; y++) {
         for (int x = 0; x < d; x++) {
            double r = Math.hypot(x + 0.5 - c, y + 0.5 - c) / c;
            double v = Math.exp(-Math.pow((r - 0.82) / 0.07, 2.0)) + Math.exp(-Math.pow((r - 0.82) / 0.02, 2.0)) * 0.6;
            if (r > 0.99) {
               v = 0.0;
            }

            px[y * d + x] = (int)(Math.min(1.0, v) * 255.0) << 24 | 16777215;
         }
      }

      return new int[][]{px, {d, d}};
   }

   static int[][] halo(int d) {
      int[] px = new int[d * d];
      double c = d / 2.0;
      double R = 0.72;

      for (int y = 0; y < d; y++) {
         for (int x = 0; x < d; x++) {
            double r = Math.hypot(x + 0.5 - c, y + 0.5 - c) / c;
            double v;
            if (r < R) {
               v = Math.pow(r / R, 14.0) * 0.9;
            } else {
               v = Math.pow(Math.max(0.0, 1.0 - (r - R) / (1.0 - R)), 2.4);
            }

            px[y * d + x] = (int)(Math.min(1.0, v) * 255.0) << 24 | 16777215;
         }
      }

      return new int[][]{px, {d, d}};
   }

   static int[][] rock(int d, long seed, int base) {
      int[] px = new int[d * d];
      double c = d / 2.0;
      double so = seed * 1.7;

      for (int y = 0; y < d; y++) {
         for (int x = 0; x < d; x++) {
            double dx = (x + 0.5 - c) / (c * 0.9);
            double dy = (y + 0.5 - c) / (c * 0.9);
            double ang = Math.atan2(dy, dx);
            double rr = 0.78 + 0.16 * fbm(Math.cos(ang) * 1.5 + so, Math.sin(ang) * 1.5, so, 3) * 2.0;
            double r = Math.sqrt(dx * dx + dy * dy) / rr;
            if (!(r >= 1.0)) {
               double nz = Math.sqrt(Math.max(0.0, 1.0 - r * r));
               double nx = dx / rr;
               double ny = dy / rr;
               double bump = fbm(nx * 4.0 + so, ny * 4.0, nz * 4.0, 4);
               double l = Math.max(0.0, -nx * 0.6 - ny * 0.5 + nz * 0.62 + bump * 0.5);
               double cr = noise(nx * 7.0 + so, ny * 7.0, nz * 7.0);
               int col = shade(base, 0.12 + l * 1.05);
               if (cr > 0.35) {
                  col = shade(col, 0.75);
               }

               double edge = Math.min(1.0, (1.0 - r) * d * 0.5);
               px[y * d + x] = (int)(edge * 255.0) << 24 | col & 16777215;
            }
         }
      }

      return new int[][]{px, {d, d}};
   }

   static int[] ringPixels(int w, int h, float inner, float outer, int[] cols, float[] sectorHp, float tilt, double sunX) {
      int[] px = new int[w * h];
      double cx = w / 2.0;
      double cy = h / 2.0;
      double rx = w / 2.0;
      double ry = h / 2.0;
      double ct = Math.cos(tilt);
      double st = Math.sin(tilt);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            double dx = x + 0.5 - cx;
            double dy = y + 0.5 - cy;
            double ux = dx * ct + dy * st;
            double uy = -dx * st + dy * ct;
            double r = Math.sqrt(Math.pow(ux / rx, 2.0) + Math.pow(uy / ry, 2.0));
            if (!(r < inner) && !(r > outer)) {
               double t = (r - inner) / (outer - inner);
               double band = 0.55 + 0.45 * Math.sin(t * 38.0) * Math.sin(t * 11.0 + 1.3);
               if (t > 0.58 && t < 0.63) {
                  band *= 0.15;
               }

               if (t > 0.86 && t < 0.88) {
                  band *= 0.3;
               }

               double a = band * Math.min(1.0, t * 8.0) * Math.min(1.0, (1.0 - t) * 10.0) * 0.85;
               int c = mix(cols[0], cols[1], t);
               c = mix(c, cols[2], clamp01(Math.sin(t * 23.0) * 0.5 + 0.2));
               if (sectorHp != null) {
                  double ang = Math.atan2(uy / ry, ux / rx);
                  int s = (int)((ang + Math.PI) / (Math.PI * 2) * sectorHp.length) % sectorHp.length;
                  double hp = sectorHp[s];
                  int tr = (int)(t * 6.0);
                  double hsh = (HASH[s * 7 + tr * 131 & 65535] & 255) / 255.0;
                  a *= clamp01(hp * 1.6 - hsh * 0.6);
                  if (hp < 0.99) {
                     c = mix(c, -12965344, (1.0 - hp) * 0.4);
                  }
               }

               if (uy > 0.0 && Math.abs(ux / rx) < 0.43 && ux * sunX < 0.0) {
                  a *= 0.35;
               }

               if (!(a < 0.01)) {
                  px[y * w + x] = (int)(a * 255.0) << 24 | c & 16777215;
               }
            }
         }
      }

      return px;
   }

   static int[][] accretion(int w, int h) {
      int[] px = new int[w * h];
      double cx = w / 2.0;
      double cy = h / 2.0;

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            double dx = (x + 0.5 - cx) / cx;
            double dy = (y + 0.5 - cy) / cy;
            double r = Math.sqrt(dx * dx + dy * dy);
            if (!(r > 1.0) && !(r < 0.26)) {
               double t = (r - 0.26) / 0.74;
               double ang = Math.atan2(dy, dx);
               double sw = 0.6 + 0.4 * noise(Math.cos(ang) * 3.0 + t * 6.0, Math.sin(ang) * 3.0, t * 4.0);
               double a = Math.pow(1.0 - t, 1.6) * sw * Math.min(1.0, t * 12.0);
               int c = ramp(t, new double[]{0.0, 0.12, 0.4, 1.0}, new int[]{-1, -7520, -30166, -7725560});
               if (dx > 0.0) {
                  c = shade(c, 0.8);
               } else {
                  a = Math.min(1.0, a * 1.25);
               }

               px[y * w + x] = (int)(clamp01(a) * 255.0) << 24 | c & 16777215;
            }
         }
      }

      return new int[][]{px, {w, h}};
   }

   static int[][] swirl(int d, int arms) {
      int[] px = new int[d * d];
      double c = d / 2.0;

      for (int y = 0; y < d; y++) {
         for (int x = 0; x < d; x++) {
            double dx = (x + 0.5 - c) / c;
            double dy = (y + 0.5 - c) / c;
            double r = Math.sqrt(dx * dx + dy * dy);
            if (!(r >= 1.0)) {
               double ang = Math.atan2(dy, dx);
               double s = Math.sin(ang * arms + Math.log(r + 0.02) * 7.0) * 0.5 + 0.5;
               double a = Math.pow(1.0 - r, 1.2) * (0.35 + 0.65 * s) * Math.min(1.0, r * 5.0);
               px[y * d + x] = (int)(clamp01(a) * 255.0) << 24 | 16777215;
            }
         }
      }

      return new int[][]{px, {d, d}};
   }

   static int[][] cracks(int d, long seed) {
      int[] px = new int[d * d];
      double c = d / 2.0;

      for (int y = 0; y < d; y++) {
         for (int x = 0; x < d; x++) {
            double dx = (x + 0.5 - c) / c;
            double dy = (y + 0.5 - c) / c;
            double r = Math.sqrt(dx * dx + dy * dy);
            if (!(r >= 1.0)) {
               double n = ridge(dx * 2.6 + seed, dy * 2.6, 0.5 + seed * 0.1, 4);
               double v = sstep(0.72, 0.95, n);
               double e = Math.min(1.0, (1.0 - r) * d * 0.5);
               px[y * d + x] = (int)(v * e * 255.0) << 24 | 16777215;
            }
         }
      }

      return new int[][]{px, {d, d}};
   }

   static Color C(int argb) {
      return new Color(argb, true);
   }

   static void missile(Graphics2D g, int s, double ang, int body, int tip, int fin, float len) {
      g.translate(s / 2.0, s / 2.0);
      g.rotate(ang);
      double L = s * 0.42 * len;
      double W = s * 0.075;
      g.setColor(C(fin));
      g.fill(poly(-L * 0.9, -W * 0.9, -L * 1.05, -W * 2.6, -L * 0.55, -W, -L * 0.55, W, -L * 1.05, W * 2.6, -L * 0.9, W * 0.9));
      g.setPaint(new GradientPaint(0.0F, (float)(-W), C(body), 0.0F, (float)W, C(shade(body, 0.55))));
      g.fill(new Double(-L * 0.9, -W, L * 1.5, W * 2.0, W, W));
      g.setColor(C(tip));
      g.fill(poly(L * 0.58, -W, L, 0.0, L * 0.58, W));
      g.setColor(new Color(255, 255, 255, 70));
      g.fill(new Double(-L * 0.8, -W * 0.8, L * 1.3, W * 0.6, W * 0.5, W * 0.5));
   }

   static java.awt.geom.Path2D.Double poly(double... xy) {
      java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
      p.moveTo(xy[0], xy[1]);

      for (int i = 2; i < xy.length; i += 2) {
         p.lineTo(xy[i], xy[i + 1]);
      }

      p.closePath();
      return p;
   }

   static void ellipse(Graphics2D g, double cx, double cy, double rx, double ry) {
      g.fill(new java.awt.geom.Ellipse2D.Double(cx - rx, cy - ry, rx * 2.0, ry * 2.0));
   }

   static void ufo(Graphics2D g, int s, int kind, int glow) {
      double c = s / 2.0;
      if (kind == 1) {
         g.setPaint(new RadialGradientPaint((float)c, (float)(c * 0.8), s * 0.5F, new float[]{0.0F, 1.0F}, new Color[]{C(-10853260), C(-14933976)}));
         ellipse(g, c, c, s * 0.48, s * 0.3);
         g.setColor(C(-15855338));
         ellipse(g, c, c + s * 0.02, s * 0.3, s * 0.17);
         g.setColor(C(glow));

         for (int i = 0; i < 16; i++) {
            double a = i / 16.0 * Math.PI * 2.0;
            ellipse(g, c + Math.cos(a) * s * 0.4, c + Math.sin(a) * s * 0.23, s * 0.014, s * 0.014);
         }

         g.setPaint(new RadialGradientPaint((float)c, (float)c, s * 0.16F, new float[]{0.0F, 1.0F}, new Color[]{C(-1), C(glow & 16777215)}));
         ellipse(g, c, c, s * 0.16, s * 0.1);
         g.setColor(C(1442840575));
         g.setStroke(new BasicStroke(s * 0.006F));

         for (int i = 1; i <= 3; i++) {
            g.draw(
               new java.awt.geom.Ellipse2D.Double(
                  c - s * 0.12 * i - s * 0.04, c - s * 0.07 * i - s * 0.02, (s * 0.12 * i + s * 0.04) * 2.0, (s * 0.07 * i + s * 0.02) * 2.0
               )
            );
         }
      } else if (kind == 2) {
         g.setColor(C(-14012614));
         g.fill(poly(c, c - s * 0.3, c + s * 0.42, c + s * 0.18, c, c + s * 0.06, c - s * 0.42, c + s * 0.18));
         g.setColor(C(glow));
         ellipse(g, c, c - s * 0.05, s * 0.06, s * 0.06);
         ellipse(g, c - s * 0.3, c + s * 0.12, s * 0.03, s * 0.03);
         ellipse(g, c + s * 0.3, c + s * 0.12, s * 0.03, s * 0.03);
      } else if (kind == 3) {
         g.setPaint(new GradientPaint(0.0F, (float)(c - s * 0.1), C(-8749172), 0.0F, (float)(c + s * 0.12), C(-14013131)));
         g.fill(
            poly(c - s * 0.46, c, c - s * 0.2, c - s * 0.12, c + s * 0.36, c - s * 0.08, c + s * 0.47, c, c + s * 0.36, c + s * 0.1, c - s * 0.2, c + s * 0.12)
         );
         g.setColor(C(glow));

         for (int i = 0; i < 5; i++) {
            ellipse(g, c - s * 0.2 + i * s * 0.12, c, s * 0.022, s * 0.022);
         }

         g.setColor(C(-1));
         ellipse(g, c - s * 0.44, c, s * 0.03, s * 0.05);
      } else {
         double sc = kind == 4 ? 0.85 : 1.0;
         g.setPaint(new GradientPaint(0.0F, (float)(c - s * 0.1), C(-4668720), 0.0F, (float)(c + s * 0.15), C(-12959408)));
         ellipse(g, c, c + s * 0.05, s * 0.46 * sc, s * 0.15 * sc);
         g.setPaint(
            new RadialGradientPaint((float)(c - s * 0.05), (float)(c - s * 0.12), (float)(s * 0.25), new float[]{0.0F, 1.0F}, new Color[]{C(-1507340), C(glow)})
         );
         g.fill(new java.awt.geom.Arc2D.Double(c - s * 0.2 * sc, c - s * 0.2 * sc, s * 0.4 * sc, s * 0.4 * sc, 0.0, 180.0, 1));
         g.setColor(C(glow));

         for (int i = 0; i < 5; i++) {
            ellipse(g, c - s * 0.3 * sc + i * s * 0.15 * sc, c + s * 0.08, s * 0.03, s * 0.03);
         }

         if (kind == 4) {
            g.setColor(C(-7695453));
            g.setStroke(new BasicStroke(s * 0.03F));
            g.draw(new java.awt.geom.Line2D.Double(c - s * 0.25, c + s * 0.12, c - s * 0.34, c + s * 0.3));
            g.draw(new java.awt.geom.Line2D.Double(c + s * 0.25, c + s * 0.12, c + s * 0.34, c + s * 0.3));
         }
      }
   }

   static void walker(Graphics2D g, int s, int glow) {
      double c = s / 2.0;
      g.setStroke(new BasicStroke(s * 0.045F, 1, 1));
      g.setColor(C(-6643024));
      g.draw(new java.awt.geom.Line2D.Double(c, c - s * 0.12, c - s * 0.32, c + s * 0.42));
      g.draw(new java.awt.geom.Line2D.Double(c, c - s * 0.12, c + s * 0.32, c + s * 0.42));
      g.draw(new java.awt.geom.Line2D.Double(c, c - s * 0.12, c + s * 0.04, c + s * 0.46));
      g.setPaint(new GradientPaint(0.0F, (float)(c - s * 0.3), C(-3616548), 0.0F, (float)(c - s * 0.02), C(-11906464)));
      ellipse(g, c, c - s * 0.18, s * 0.2, s * 0.12);
      g.setColor(C(glow));
      ellipse(g, c, c - s * 0.16, s * 0.05, s * 0.04);
   }

   static void satellite(Graphics2D g, int s, int glow) {
      double c = s / 2.0;
      g.setColor(C(-13940086));
      g.fill(new java.awt.geom.Rectangle2D.Double(c - s * 0.46, c - s * 0.08, s * 0.28, s * 0.16));
      g.fill(new java.awt.geom.Rectangle2D.Double(c + s * 0.18, c - s * 0.08, s * 0.28, s * 0.16));
      g.setColor(C(-9794872));

      for (int i = 0; i < 3; i++) {
         g.fill(new java.awt.geom.Rectangle2D.Double(c - s * 0.44 + i * s * 0.09, c - s * 0.06, s * 0.07, s * 0.12));
         g.fill(new java.awt.geom.Rectangle2D.Double(c + s * 0.2 + i * s * 0.09, c - s * 0.06, s * 0.07, s * 0.12));
      }

      g.setPaint(new GradientPaint(0.0F, (float)(c - s * 0.12), C(-2038550), 0.0F, (float)(c + s * 0.12), C(-9801606)));
      g.fill(new Double(c - s * 0.13, c - s * 0.13, s * 0.26, s * 0.26, s * 0.06, s * 0.06));
      g.setColor(C(glow));
      ellipse(g, c, c, s * 0.06, s * 0.06);
   }

   static int[][] station(int d) {
      int[] px = new int[d * d];
      double c = d / 2.0;

      for (int y = 0; y < d; y++) {
         for (int x = 0; x < d; x++) {
            double dx = (x + 0.5 - c) / (c * 0.96);
            double dy = (y + 0.5 - c) / (c * 0.96);
            double r2 = dx * dx + dy * dy;
            if (!(r2 >= 1.0)) {
               double nz = Math.sqrt(1.0 - r2);
               double l = Math.max(0.0, -dx * 0.55 - dy * 0.45 + nz * 0.7);
               int col = -7564644;
               double lat = Math.asin(dy);
               double lon = Math.atan2(dx, nz);
               boolean seam = Math.abs(Math.sin(lat * 14.0)) < 0.07 || Math.abs(Math.sin(lon * 18.0)) < 0.05;
               if (seam) {
                  col = -10854294;
               }

               int hsh = HASH[(int)(lat * 40.0) * 97 + (int)(lon * 50.0) * 13 & 65535] & 255;
               if (hsh < 40) {
                  col = mix(col, -9670020, 0.8);
               }

               if (Math.abs(dy) < 0.035) {
                  col = -13881288;
               }

               double ddx = dx + 0.38;
               double ddy = dy + 0.36;
               double dr = Math.sqrt(ddx * ddx + ddy * ddy);
               if (dr < 0.24) {
                  col = mix(-12959672, -9801608, dr / 0.24);
                  if (Math.abs(Math.sin(dr * 60.0)) < 0.3) {
                     col = shade(col, 0.8);
                  }

                  if (dr < 0.04) {
                     col = -6422678;
                  }
               }

               col = shade(col, 0.15 + l * 1.0);
               if (hsh > 250 && l < 0.3) {
                  col = -5728;
               }

               double e = Math.min(1.0, (1.0 - Math.sqrt(r2)) * d * 0.48);
               px[y * d + x] = (int)(e * 255.0) << 24 | col & 16777215;
            }
         }
      }

      return new int[][]{px, {d, d}};
   }

   static void fun(Graphics2D g, int s, int kind, int col) {
      double c = s / 2.0;
      double k = s / 100.0;
      switch (kind) {
         case 0:
            g.setColor(C(shade(col, 0.6)));
            g.fill(new Double(c - 44.0 * k, c - 22.0 * k, 88.0 * k, 50.0 * k, 8.0 * k, 8.0 * k));
            g.setColor(C(col));
            g.fill(new Double(c - 44.0 * k, c - 26.0 * k, 88.0 * k, 46.0 * k, 8.0 * k, 8.0 * k));

            for (int ix = 0; ix < 4; ix++) {
               for (int j = 0; j < 2; j++) {
                  double sx = c - 33.0 * k + ix * 22 * k;
                  double sy = c - 14.0 * k + j * 22 * k;
                  g.setColor(C(shade(col, 0.7)));
                  ellipse(g, sx, sy + 2.0 * k, 8.0 * k, 8.0 * k);
                  g.setColor(C(shade(col, 1.22)));
                  ellipse(g, sx, sy, 8.0 * k, 8.0 * k);
               }
            }
            break;
         case 1:
            g.setColor(C(-724502));
            ellipse(g, c, c + 4.0 * k, 36.0 * k, 24.0 * k);
            g.setColor(C(-14935526));
            ellipse(g, c - 12.0 * k, c, 10.0 * k, 8.0 * k);
            ellipse(g, c + 14.0 * k, c + 12.0 * k, 9.0 * k, 7.0 * k);
            ellipse(g, c + 4.0 * k, c - 10.0 * k, 6.0 * k, 5.0 * k);
            g.setColor(C(-724502));
            ellipse(g, c + 38.0 * k, c - 8.0 * k, 14.0 * k, 12.0 * k);
            g.setColor(C(-875088));
            ellipse(g, c + 46.0 * k, c - 4.0 * k, 8.0 * k, 6.0 * k);
            g.setColor(C(-14935526));
            ellipse(g, c + 36.0 * k, c - 12.0 * k, 2.5 * k, 2.5 * k);
            g.setColor(C(-2502720));
            g.fill(poly(c + 30.0 * k, c - 18.0 * k, c + 26.0 * k, c - 30.0 * k, c + 34.0 * k, c - 20.0 * k));
            g.setColor(C(-12962764));

            for (int ix = 0; ix < 4; ix++) {
               g.fill(new java.awt.geom.Rectangle2D.Double(c - 26.0 * k + ix * 15 * k, c + 22.0 * k, 5.0 * k, 14.0 * k));
            }
            break;
         case 2:
            g.setColor(C(-7714262));
            ellipse(g, c, c + 14.0 * k, 40.0 * k, 16.0 * k);
            g.fill(new java.awt.geom.Rectangle2D.Double(c - 40.0 * k, c - 12.0 * k, 80.0 * k, 26.0 * k));
            g.setColor(C(-10268));
            ellipse(g, c, c - 12.0 * k, 40.0 * k, 16.0 * k);
            g.fill(new java.awt.geom.Rectangle2D.Double(c - 40.0 * k, c - 12.0 * k, 80.0 * k, 8.0 * k));
            g.setColor(C(-1));

            for (int ix = 0; ix < 8; ix++) {
               ellipse(g, c - 35.0 * k + ix * 10 * k, c - 4.0 * k, 5.0 * k, 5.0 * k);
            }

            g.setColor(C(-1900533));
            ellipse(g, c - 14.0 * k, c - 16.0 * k, 5.0 * k, 5.0 * k);
            ellipse(g, c + 12.0 * k, c - 10.0 * k, 5.0 * k, 5.0 * k);
            g.setColor(C(-10503937));
            g.fill(new java.awt.geom.Rectangle2D.Double(c - 2.0 * k, c - 36.0 * k, 4.0 * k, 20.0 * k));
            g.setColor(C(-11446));
            ellipse(g, c, c - 40.0 * k, 4.0 * k, 6.0 * k);
            break;
         case 3:
            g.setColor(C(-11745));
            ellipse(g, c - 4.0 * k, c + 12.0 * k, 34.0 * k, 22.0 * k);
            ellipse(g, c + 16.0 * k, c - 14.0 * k, 18.0 * k, 18.0 * k);
            g.setColor(C(-30208));
            g.fill(poly(c + 30.0 * k, c - 16.0 * k, c + 46.0 * k, c - 10.0 * k, c + 30.0 * k, c - 6.0 * k));
            g.setColor(C(-15066598));
            ellipse(g, c + 20.0 * k, c - 18.0 * k, 3.5 * k, 3.5 * k);
            g.setColor(C(-6528));
            ellipse(g, c - 12.0 * k, c + 6.0 * k, 14.0 * k, 8.0 * k);
            break;
         case 4:
            g.setPaint(
               new RadialGradientPaint(
                  (float)(c - 14.0 * k), (float)(c - 14.0 * k), (float)(50.0 * k), new float[]{0.0F, 1.0F}, new Color[]{C(-9811253), C(-15463888)}
               )
            );
            ellipse(g, c, c, 44.0 * k, 44.0 * k);
            g.setColor(C(-16120300));
            ellipse(g, c + 8.0 * k, c - 16.0 * k, 5.0 * k, 5.0 * k);
            ellipse(g, c + 20.0 * k, c - 8.0 * k, 5.0 * k, 5.0 * k);
            ellipse(g, c + 14.0 * k, c + 6.0 * k, 6.0 * k, 6.0 * k);
            break;
         case 5:
            g.setColor(C(-2778550));
            ellipse(g, c, c, 44.0 * k, 44.0 * k);
            g.setColor(C(-1553884));
            ellipse(g, c, c, 38.0 * k, 38.0 * k);
            g.setColor(C(-9110));
            ellipse(g, c, c, 35.0 * k, 35.0 * k);
            g.setColor(C(-5233382));
            double[][] pp = new double[][]{{-14.0, -12.0}, {12.0, -18.0}, {18.0, 8.0}, {-6.0, 16.0}, {-20.0, 6.0}, {2.0, -2.0}};

            for (double[] q : pp) {
               ellipse(g, c + q[0] * k, c + q[1] * k, 7.0 * k, 7.0 * k);
            }

            g.setColor(C(-12678610));
            ellipse(g, c + 8.0 * k, c + 18.0 * k, 3.0 * k, 2.0 * k);
            ellipse(g, c - 16.0 * k, c - 22.0 * k, 3.0 * k, 2.0 * k);
            break;
         case 6:
            g.setColor(C(-3569074));
            ellipse(g, c, c, 44.0 * k, 44.0 * k);
            g.setColor(C(-32832));
            ellipse(g, c, c - 2.0 * k, 38.0 * k, 36.0 * k);
            int[] sp = new int[]{-1, -10496769, -7859, -8587396};
            Random r = new Random(4L);

            for (int ix = 0; ix < 26; ix++) {
               double a = r.nextDouble() * Math.PI * 2.0;
               double rr = 20.0 + r.nextDouble() * 14.0;
               g.setColor(C(sp[ix % 4]));
               g.fill(new java.awt.geom.Rectangle2D.Double(c + Math.cos(a) * rr * k, c + Math.sin(a) * rr * k - 2.0 * k, 5.0 * k, 2.0 * k));
            }

            g.setComposite(AlphaComposite.Clear);
            ellipse(g, c, c, 13.0 * k, 13.0 * k);
            g.setComposite(AlphaComposite.SrcOver);
            break;
         case 7:
            g.setColor(C(-15329766));
            g.fill(new Double(c - 40.0 * k, c - 28.0 * k, 80.0 * k, 56.0 * k, 10.0 * k, 10.0 * k));
            g.setColor(C(-723728));
            g.fill(new java.awt.geom.Rectangle2D.Double(c - 38.0 * k, c + 10.0 * k, 76.0 * k, 14.0 * k));
            g.setColor(C(-15329766));

            for (int i = 0; i < 14; i++) {
               if (i % 7 != 2 && i % 7 != 6) {
                  g.fill(new java.awt.geom.Rectangle2D.Double(c - 36.0 * k + i * 5.4 * k, c + 10.0 * k, 3.0 * k, 8.0 * k));
               }
            }

            g.setColor(C(-12961212));
            g.fill(new java.awt.geom.Rectangle2D.Double(c - 34.0 * k, c - 22.0 * k, 68.0 * k, 26.0 * k));
            break;
         case 8:
            g.setColor(C(-11745));
            ellipse(g, c, c - 26.0 * k, 11.0 * k, 11.0 * k);
            g.fill(new java.awt.geom.Rectangle2D.Double(c - 4.0 * k, c - 38.0 * k, 8.0 * k, 4.0 * k));
            g.setColor(C(-15066598));
            ellipse(g, c - 4.0 * k, c - 28.0 * k, 1.8 * k, 1.8 * k);
            ellipse(g, c + 4.0 * k, c - 28.0 * k, 1.8 * k, 1.8 * k);
            g.setStroke(new BasicStroke((float)(1.6 * k)));
            g.draw(new java.awt.geom.Arc2D.Double(c - 5.0 * k, c - 27.0 * k, 10.0 * k, 7.0 * k, 200.0, 140.0, 0));
            g.setColor(C(col));
            g.fill(poly(c - 14.0 * k, c - 14.0 * k, c + 14.0 * k, c - 14.0 * k, c + 17.0 * k, c + 12.0 * k, c - 17.0 * k, c + 12.0 * k));
            g.setColor(C(shade(col, 0.8)));
            g.fill(new Double(c - 24.0 * k, c - 12.0 * k, 8.0 * k, 22.0 * k, 6.0 * k, 6.0 * k));
            g.fill(new Double(c + 16.0 * k, c - 12.0 * k, 8.0 * k, 22.0 * k, 6.0 * k, 6.0 * k));
            g.setColor(C(-16755265));
            g.fill(new java.awt.geom.Rectangle2D.Double(c - 16.0 * k, c + 12.0 * k, 32.0 * k, 26.0 * k));
            g.setColor(C(-16761204));
            g.fill(new java.awt.geom.Rectangle2D.Double(c - 1.0 * k, c + 18.0 * k, 2.0 * k, 20.0 * k));
            break;
         case 9:
            g.setColor(C(col));
            ellipse(g, c, c + 8.0 * k, 20.0 * k, 24.0 * k);
            ellipse(g, c, c - 20.0 * k, 15.0 * k, 13.0 * k);
            ellipse(g, c - 12.0 * k, c - 32.0 * k, 6.0 * k, 6.0 * k);
            ellipse(g, c + 12.0 * k, c - 32.0 * k, 6.0 * k, 6.0 * k);
            ellipse(g, c - 20.0 * k, c - 2.0 * k, 8.0 * k, 6.0 * k);
            ellipse(g, c + 20.0 * k, c - 2.0 * k, 8.0 * k, 6.0 * k);
            ellipse(g, c - 12.0 * k, c + 30.0 * k, 9.0 * k, 7.0 * k);
            ellipse(g, c + 12.0 * k, c + 30.0 * k, 9.0 * k, 7.0 * k);
            g.setColor(new Color(255, 255, 255, 110));
            ellipse(g, c - 6.0 * k, c - 24.0 * k, 4.0 * k, 3.0 * k);
            ellipse(g, c - 7.0 * k, c + 2.0 * k, 5.0 * k, 8.0 * k);
            break;
         default:
            g.setColor(C(-6643542));
            ellipse(g, c, c, 42.0 * k, 42.0 * k);
            Random r = new Random(7L);

            for (int yy = -40; yy < 40; yy += 7) {
               for (int xx = -40; xx < 40; xx += 7) {
                  if (xx * xx + yy * yy <= 1600) {
                     int b = 120 + r.nextInt(135);
                     g.setColor(new Color(b, b, Math.min(255, b + 20)));
                     g.fill(new java.awt.geom.Rectangle2D.Double(c + xx * k, c + yy * k, 6.0 * k, 6.0 * k));
                  }
               }
            }
      }
   }

   static {
      Random r = new Random(1337L);
      int[] p = new int[256];
      int i = 0;

      while (i < 256) {
         p[i] = i++;
      }

      for (int ix = 255; ix > 0; ix--) {
         int j = r.nextInt(ix + 1);
         int t = p[ix];
         p[ix] = p[j];
         p[j] = t;
      }

      for (int ix = 0; ix < 512; ix++) {
         PERM[ix] = p[ix & 0xFF];
      }

      r.nextBytes(HASH);
   }
}
