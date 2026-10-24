package dev.lego.visual.sky;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public final class SkyGen {
   public static final String[] IDS = new String[]{
      "milkyway",
      "nebula",
      "planets",
      "aurora",
      "sunset",
      "storm",
      "summer",
      "goldenhour",
      "cottoncandy",
      "sunrise",
      "overcast",
      "thunder",
      "anime",
      "alien",
      "synthwave",
      "oceandusk",
      "bloodmoon",
      "eclipse",
      "auroraviolet",
      "winter",
      "twilight",
      "rednebula",
      "spiral",
      "blackhole",
      "void"
   };
   public static final String[] NAMES = new String[]{
      "Sternennacht",
      "Galaxie-Nebel",
      "Planeten",
      "Polarlicht",
      "Sonnenuntergang",
      "Gewitternacht",
      "Sommertag",
      "Goldene Stunde",
      "Zuckerwatte-Abend",
      "Nebliger Sonnenaufgang",
      "Regentag",
      "Gewitterfront",
      "Anime-Himmel",
      "Fremder Planet",
      "Synthwave",
      "Meeresdämmerung",
      "Blutmond",
      "Sonnenfinsternis",
      "Violettes Polarlicht",
      "Winternacht",
      "Feen-Dämmerung",
      "Roter Nebel",
      "Spiralgalaxie",
      "Schwarzes Loch",
      "Endleere"
   };
   private static final ConcurrentHashMap<String, Integer> FOG = new ConcurrentHashMap<>();
   private static volatile ForkJoinPool POOL;
   private static final int LUT_N = 4096;
   private static final float[] GAMMA = new float[4098];
   private static final float KNEE = 0.62F;
   private static final ThreadLocal<SkyGen.C> TL;
   static final int SPACE = 0;
   static final int GROUND = 1;
   static final int OCEAN = 2;
   static final int CLOUDSEA = 3;
   private static final int[] P;

   private SkyGen() {
   }

   public static void dir(int face, double u, double v, double[] out) {
      double x;
      double y;
      double z;
      switch (face) {
         case 0:
            x = -1.0 + 2.0 * u;
            y = -1.0;
            z = -1.0 + 2.0 * v;
            break;
         case 1:
            x = -1.0 + 2.0 * u;
            y = 1.0;
            z = 1.0 - 2.0 * v;
            break;
         case 2:
            x = 1.0 - 2.0 * u;
            y = 1.0 - 2.0 * v;
            z = 1.0;
            break;
         case 3:
            x = -1.0;
            y = 1.0 - 2.0 * v;
            z = 1.0 - 2.0 * u;
            break;
         case 4:
            x = -1.0 + 2.0 * u;
            y = 1.0 - 2.0 * v;
            z = -1.0;
            break;
         default:
            x = 1.0;
            y = 1.0 - 2.0 * v;
            z = -1.0 + 2.0 * u;
      }

      double l = Math.sqrt(x * x + y * y + z * z);
      out[0] = x / l;
      out[1] = y / l;
      out[2] = z / l;
   }

   static boolean faceUV(int face, double x, double y, double z, double[] uv) {
      switch (face) {
         case 0:
            double a = -y;
            if (a <= 1.0E-6) {
               return false;
            }

            uv[0] = (x / a + 1.0) * 0.5;
            uv[1] = (z / a + 1.0) * 0.5;
            return true;
         case 1:
            if (y <= 1.0E-6) {
               return false;
            }

            uv[0] = (x / y + 1.0) * 0.5;
            uv[1] = (1.0 - z / y) * 0.5;
            return true;
         case 2:
            if (z <= 1.0E-6) {
               return false;
            }

            uv[0] = (1.0 - x / z) * 0.5;
            uv[1] = (1.0 - y / z) * 0.5;
            return true;
         case 3:
            double a3 = -x;
            if (a3 <= 1.0E-6) {
               return false;
            }

            uv[0] = (1.0 - z / a3) * 0.5;
            uv[1] = (1.0 - y / a3) * 0.5;
            return true;
         case 4:
            double a4 = -z;
            if (a4 <= 1.0E-6) {
               return false;
            }

            uv[0] = (x / a4 + 1.0) * 0.5;
            uv[1] = (1.0 - y / a4) * 0.5;
            return true;
         default:
            if (x <= 1.0E-6) {
               return false;
            } else {
               uv[0] = (z / x + 1.0) * 0.5;
               uv[1] = (1.0 - y / x) * 0.5;
               return true;
            }
      }
   }

   public static String kind(String id) {
      return sky(id).kind;
   }

   public static int fogColor(String id) {
      return FOG.computeIfAbsent(id == null ? "" : id, SkyGen::computeFog);
   }

   private static int computeFog(String id) {
      SkyGen.Sky s = sky(id);
      SkyGen.C c = new SkyGen.C();
      double r = 0.0;
      double g = 0.0;
      double b = 0.0;
      int cnt = 0;

      for (int i = 0; i < 64; i++) {
         double az = i * Math.PI * 2.0 / 64.0;

         for (int j = 0; j < 4; j++) {
            double el = 0.01 + j * 0.03;
            double ce = Math.cos(el);
            s.evalFull(Math.cos(az) * ce, Math.sin(el), Math.sin(az) * ce, c);
            r += tone8(c.r * s.exposure);
            g += tone8(c.g * s.exposure);
            b += tone8(c.b * s.exposure);
            cnt++;
         }
      }

      int ri = (int)Math.round(r / cnt);
      int gi = (int)Math.round(g / cnt);
      int bi = (int)Math.round(b / cnt);
      return clamp8(ri) << 16 | clamp8(gi) << 8 | clamp8(bi);
   }

   private static ForkJoinPool pool() {
      ForkJoinPool p = POOL;
      if (p == null) {
         synchronized (SkyGen.class) {
            if (POOL == null) {
               int threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
               POOL = new ForkJoinPool(threads, fp -> {
                  ForkJoinWorkerThread t = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(fp);
                  t.setName("LegoClient-SkyGen-" + t.getPoolIndex());
                  t.setDaemon(true);
                  t.setPriority(2);
                  return t;
               }, null, false);
            }

            p = POOL;
         }
      }

      return p;
   }

   static void parallel(int count, IntConsumer body) {
      try {
         pool().submit(() -> IntStream.range(0, count).parallel().forEach(body)).get();
      } catch (InterruptedException var4) {
         Thread.currentThread().interrupt();
         throw new RuntimeException(var4);
      } catch (ExecutionException var5) {
         Throwable c = var5.getCause();
         if (c instanceof RuntimeException) {
            throw (RuntimeException)c;
         } else {
            throw new RuntimeException(c);
         }
      }
   }

   public static int[] generate(String id, int n) {
      SkyGen.Sky s = sky(id);
      int W = n * 3;
      int H = n * 2;
      int G = Math.max(16, Math.min(n, (int)Math.round(512.0 * s.detail)));
      int GP = G + 1;
      float ex = (float)s.exposure;
      float[][] grid = new float[6][GP * GP * 4];
      parallel(6 * GP, idx -> {
         int fx = idx / GP;
         int jx = idx % GP;
         float[] gr = grid[fx];
         SkyGen.C c = new SkyGen.C();
         double[] d = new double[3];
         double v = (double)jx / G;
         int o = jx * GP * 4;

         for (int ix = 0; ix < GP; o += 4) {
            dir(fx, (double)ix / G, v, d);
            s.evalFull(d[0], d[1], d[2], c);
            gr[o] = (float)(c.r * ex);
            gr[o + 1] = (float)(c.g * ex);
            gr[o + 2] = (float)(c.b * ex);
            gr[o + 3] = (float)Math.max(0.0, Math.min(1.0, c.vis));
            ix++;
         }
      });
      boolean[][] fineCell = new boolean[6][];
      if (s.fine != null && s.fine.length > 0) {
         double[] d = new double[3];
         double margin = 3.5 / G;

         for (int f = 0; f < 6; f++) {
            boolean[] fc = null;

            for (int j = 0; j < G; j++) {
               for (int i = 0; i < G; i++) {
                  dir(f, (i + 0.5) / G, (j + 0.5) / G, d);

                  for (double[] q : s.fine) {
                     double cosA = d[0] * q[0] + d[1] * q[1] + d[2] * q[2];
                     if (cosA > Math.cos(q[3] + margin)) {
                        if (fc == null) {
                           fc = new boolean[G * G];
                        }

                        fc[j * G + i] = true;
                        break;
                     }
                  }
               }
            }

            fineCell[f] = fc;
         }
      }

      SkyGen.Stars st = SkyGen.Stars.build(s, n);
      int[] px = new int[W * H];
      double scale = (double)G / n;
      parallel(H, row -> {
         SkyGen.C fcx = null;
         double[] fd = null;
         int faceRow = row / n * 3;
         int y = row % n;
         double gy = (y + 0.5) * scale;
         int j0 = Math.min(G - 1, (int)gy);
         float fy = (float)(gy - j0);
         int cy = y / 16;
         int base = row * W;

         for (int col = 0; col < W; col++) {
            int fx = faceRow + col / n;
            int x = col % n;
            double gx = (x + 0.5) * scale;
            int i0 = Math.min(G - 1, (int)gx);
            float fxx = (float)(gx - i0);
            float[] gr = grid[fx];
            int o00 = (j0 * GP + i0) * 4;
            int o10 = o00 + 4;
            int o01 = o00 + GP * 4;
            int o11 = o01 + 4;
            float w00 = (1.0F - fxx) * (1.0F - fy);
            float w10 = fxx * (1.0F - fy);
            float w01 = (1.0F - fxx) * fy;
            float w11 = fxx * fy;
            float r = gr[o00] * w00 + gr[o10] * w10 + gr[o01] * w01 + gr[o11] * w11;
            float g = gr[o00 + 1] * w00 + gr[o10 + 1] * w10 + gr[o01 + 1] * w01 + gr[o11 + 1] * w11;
            float b = gr[o00 + 2] * w00 + gr[o10 + 2] * w10 + gr[o01 + 2] * w01 + gr[o11 + 2] * w11;
            float fineVis = -1.0F;
            boolean[] fcell = fineCell[fx];
            if (fcell != null && fcell[j0 * G + i0]) {
               if (fcx == null) {
                  fcx = new SkyGen.C();
                  fd = new double[3];
               }

               dir(fx, (x + 0.5) / n, (y + 0.5) / n, fd);
               s.evalFull(fd[0], fd[1], fd[2], fcx);
               r = (float)(fcx.r * ex);
               g = (float)(fcx.g * ex);
               b = (float)(fcx.b * ex);
               fineVis = (float)Math.max(0.0, Math.min(1.0, fcx.vis));
            }

            if (st != null) {
               int cell = (fx * st.nc + cy) * st.nc + x / 16;
               int a0 = st.start[cell];
               int a1 = st.start[cell + 1];
               if (a0 != a1) {
                  float vis = fineVis >= 0.0F ? fineVis : gr[o00 + 3] * w00 + gr[o10 + 3] * w10 + gr[o01 + 3] * w01 + gr[o11 + 3] * w11;
                  float sr = 0.0F;
                  float sg = 0.0F;
                  float sb = 0.0F;
                  float mr = 0.0F;
                  float mg = 0.0F;
                  float mb = 0.0F;
                  float[] qx = st.data;

                  for (int k = a0; k < a1; k++) {
                     int e = st.item[k] * 9;
                     float dx = x - qx[e];
                     float dy = y - qx[e + 1];
                     float d2 = dx * dx + dy * dy;
                     float i2 = qx[e + 5];
                     float arg = d2 * i2;
                     float amp = (arg < 12.0F ? qx[e + 6] * (float)Math.exp(-arg) : 0.0F) + qx[e + 7] * (float)Math.exp(-arg * 0.06F);
                     if (qx[e + 8] > 0.0F) {
                        sr += qx[e + 2] * amp;
                        sg += qx[e + 3] * amp;
                        sb += qx[e + 4] * amp;
                     } else {
                        mr += qx[e + 2] * amp;
                        mg += qx[e + 3] * amp;
                        mb += qx[e + 4] * amp;
                     }
                  }

                  r += sr * vis + mr;
                  g += sg * vis + mg;
                  b += sb * vis + mb;
               }
            }

            int h = col * 73856093 ^ row * 19349663;
            h ^= h >>> 13;
            h *= 1540483477;
            h ^= h >>> 15;
            float d1 = ((h & 1023) - (h >>> 10 & 1023)) * 9.765625E-4F;
            float d2 = ((h >>> 20 & 1023) - (h & 1023)) * 9.765625E-4F;
            float d3 = ((h >>> 5 & 1023) - (h >>> 20 & 1023)) * 9.765625E-4F;
            int ri = clamp8((int)(tone(r) + 0.5F + d1));
            int gi = clamp8((int)(tone(g) + 0.5F + d2));
            int bi = clamp8((int)(tone(b) + 0.5F + d3));
            px[base + col] = 0xFF000000 | ri << 16 | gi << 8 | bi;
         }
      });
      return px;
   }

   static float tone(float v) {
      if (v <= 0.0F) {
         return 0.0F;
      } else {
         if (v > 0.62F) {
            v = 0.62F + 0.38F * (1.0F - (float)Math.exp(-(v - 0.62F) / 0.38F));
         }

         float p = v * 4096.0F;
         int i = (int)p;
         if (i >= 4096) {
            return GAMMA[4096];
         } else {
            float t = p - i;
            return GAMMA[i] + (GAMMA[i + 1] - GAMMA[i]) * t;
         }
      }
   }

   static int tone8(double v) {
      return clamp8(Math.round(tone((float)v)));
   }

   private static int clamp8(int v) {
      return v < 0 ? 0 : (v > 255 ? 255 : v);
   }

   public static SkyGen.Preset preset(String id) {
      SkyGen.Sky s = sky(id);
      return (d, out) -> {
         SkyGen.C c = TL.get();
         s.evalFull(d[0], d[1], d[2], c);
         out[0] = c.r * s.exposure;
         out[1] = c.g * s.exposure;
         out[2] = c.b * s.exposure;
      };
   }

   static SkyGen.Sky sky(String id) {
      SkyGen.Sky s = null;
      if (s == null) {
         s = SkyGen2.make(id);
      }

      if (s == null) {
         s = SkyGen3.make(id);
      }

      if (s == null) {
         s = SkyGen3.make("milkyway");
      }

      return s;
   }

   static void clouds(SkyGen.C c, double x, double y, double z, SkyGen.Clouds P, double[] s, double[] sunCol) {
      if (!(y < 0.003)) {
         double fade = smooth(0.003, 0.09, y);
         double k = P.height / (y + 0.03);
         double px = x * k * P.scale / P.stretch;
         double pz = z * k * P.scale;
         double dist = k * P.scale;
         double oct = Math.max(2.0, P.oct - log2(1.0 + dist * 0.35));
         double wx = fbmL(px * 0.35, P.seed, pz * 0.35, 3.0);
         double wz = fbmL(px * 0.35 + 5.2, P.seed + 3.0, pz * 0.35 + 1.3, 3.0);
         double qx = px + wx * P.warp;
         double qz = pz + wz * P.warp;
         double n = fbmL(qx, P.seed + 10.0, qz, oct);
         double dens = smooth(P.cov, P.cov + P.soft, n);
         if (!(dens <= 0.002)) {
            double shx = s[0];
            double shz = s[2];
            double hl = Math.sqrt(shx * shx + shz * shz) + 1.0E-6;
            double off = 0.22 * Math.min(1.0, hl * 1.5);
            double n2 = fbmL(qx + shx / hl * off, P.seed + 10.0, qz + shz / hl * off, Math.max(2.0, oct - 2.0));
            double edge = clamp01((n - n2) * 3.0 + 0.5);
            double thick = smooth(P.cov, P.cov + P.soft * 2.8, n);
            double light = (0.62 + (edge - 0.5) * 0.9 * P.lightSide) * (1.0 - P.thickDark * thick);
            light = clamp01(light);
            double[] L = P.lit;
            double[] S = P.shade;
            double cr = S[0] + (L[0] - S[0]) * light;
            double cg = S[1] + (L[1] - S[1]) * light;
            double cb = S[2] + (L[2] - S[2]) * light;
            double sd = x * s[0] + y * s[1] + z * s[2];
            if (sd > 0.0 && P.silver > 0.0) {
               double sd2 = sd * sd;
               double sd4 = sd2 * sd2;
               double sv = sd4 * sd4 * (1.0 - thick) * P.silver;
               cr += sunCol[0] * sv;
               cg += sunCol[1] * sv;
               cb += sunCol[2] * sv;
            }

            double air = 1.0 - Math.exp(-dist / P.fog);
            cr += (c.r - cr) * air;
            cg += (c.g - cg) * air;
            cb += (c.b - cb) * air;
            double a = dens * fade * P.opacity;
            c.r = c.r + (cr - c.r) * a;
            c.g = c.g + (cg - c.g) * a;
            c.b = c.b + (cb - c.b) * a;
            c.vis = c.vis * (1.0 - Math.min(1.0, a * 1.4));
         }
      }
   }

   static void towers(SkyGen.C c, double x, double y, double z, SkyGen.Towers T, double[] s) {
      double top = T.h0 + T.h1;
      if (!(y > top + 0.08) && !(y < -0.02)) {
         double hl = Math.sqrt(x * x + z * z) + 1.0E-9;
         double ux = x / hl;
         double uz = z / hl;
         double prof = fbmL(ux * T.freq, T.seed, uz * T.freq, 4.0);
         double hgt = T.h0 + T.h1 * smooth(T.cut, T.cut + 0.35, prof);
         if (!(y > hgt + 0.07)) {
            double b = fbmL(ux * T.bill, y * T.bill * 1.25 + T.seed * 3.1, uz * T.bill, 5.0);
            double v = hgt - y + b * 0.06 * T.puff;
            double a = smooth(0.0, T.soft, v);
            if (!(a <= 0.002)) {
               double b2 = fbmL(ux * T.bill, (y - 0.012) * T.bill * 1.25 + T.seed * 3.1, uz * T.bill, 3.0);
               double edge = clamp01((b - b2) * 4.0 + 0.5);
               double rel = clamp01(y / Math.max(0.01, hgt + 0.03));
               double shx = s[0];
               double shz = s[2];
               double sl = Math.sqrt(shx * shx + shz * shz) + 1.0E-6;
               double side = (ux * shx + uz * shz) / sl;
               double light = smooth(-0.1, 1.0, rel * 0.9 + (edge - 0.5) * 0.8 + b * 0.5) * (1.0 - T.sunSide + T.sunSide * (0.5 + 0.5 * side));
               light = clamp01(light);
               double[] L = T.lit;
               double[] S = T.shade;
               double cr = S[0] + (L[0] - S[0]) * light;
               double cg = S[1] + (L[1] - S[1]) * light;
               double cb = S[2] + (L[2] - S[2]) * light;
               double hz = Math.exp(-Math.max(0.0, y) / T.haze);
               cr += (c.r - cr) * hz;
               cg += (c.g - cg) * hz;
               cb += (c.b - cb) * hz;
               c.r = c.r + (cr - c.r) * a;
               c.g = c.g + (cg - c.g) * a;
               c.b = c.b + (cb - c.b) * a;
               c.vis *= 1.0 - a;
            }
         }
      }
   }

   static void sunDisk(SkyGen.C c, double cosA, double radius, double[] col, double disk, double bloom1, double w1, double bloom2, double w2) {
      double a2 = 2.0 * (1.0 - cosA);
      double ang = Math.sqrt(Math.max(0.0, a2));
      double rr = ang / radius;
      double k = 0.0;
      if (rr < 1.15) {
         double e = smooth(1.12, 0.92, rr);
         double mu = Math.sqrt(Math.max(0.0, 1.0 - Math.min(1.0, rr * rr)));
         k += disk * e * (0.55 + 0.45 * mu);
      }

      k += bloom1 * Math.exp(-ang / w1) + bloom2 * Math.exp(-a2 / (w2 * w2));
      c.r = c.r + col[0] * k;
      c.g = c.g + col[1] * k;
      c.b = c.b + col[2] * k;
      if (rr < 1.1) {
         c.vis = c.vis * smooth(0.8, 1.1, rr);
      }
   }

   private static int fl(double v) {
      int i = (int)v;
      return v < i ? i - 1 : i;
   }

   private static double grad(int h, double x, double y, double z) {
      switch (h & 15) {
         case 0:
            return x + y;
         case 1:
            return -x + y;
         case 2:
            return x - y;
         case 3:
            return -x - y;
         case 4:
            return x + z;
         case 5:
            return -x + z;
         case 6:
            return x - z;
         case 7:
            return -x - z;
         case 8:
            return y + z;
         case 9:
            return -y + z;
         case 10:
            return y - z;
         case 11:
            return -y - z;
         case 12:
            return y + x;
         case 13:
            return -y + z;
         case 14:
            return y - x;
         default:
            return -y - z;
      }
   }

   public static double noise(double x, double y, double z) {
      int xi = fl(x);
      int yi = fl(y);
      int zi = fl(z);
      x -= xi;
      y -= yi;
      z -= zi;
      int X = xi & 0xFF;
      int Y = yi & 0xFF;
      int Z = zi & 0xFF;
      double u = x * x * x * (x * (x * 6.0 - 15.0) + 10.0);
      double v = y * y * y * (y * (y * 6.0 - 15.0) + 10.0);
      double w = z * z * z * (z * (z * 6.0 - 15.0) + 10.0);
      int A = P[X] + Y;
      int AA = P[A] + Z;
      int AB = P[A + 1] + Z;
      int B = P[X + 1] + Y;
      int BA = P[B] + Z;
      int BB = P[B + 1] + Z;
      double x1 = x - 1.0;
      double y1 = y - 1.0;
      double z1 = z - 1.0;
      double a0 = grad(P[AA], x, y, z);
      double a1 = grad(P[BA], x1, y, z);
      double b0 = grad(P[AB], x, y1, z);
      double b1 = grad(P[BB], x1, y1, z);
      double c0 = grad(P[AA + 1], x, y, z1);
      double c1 = grad(P[BA + 1], x1, y, z1);
      double d0 = grad(P[AB + 1], x, y1, z1);
      double d1 = grad(P[BB + 1], x1, y1, z1);
      double l0 = a0 + u * (a1 - a0);
      double l1 = b0 + u * (b1 - b0);
      double l2 = c0 + u * (c1 - c0);
      double l3 = d0 + u * (d1 - d0);
      double m0 = l0 + v * (l1 - l0);
      double m1 = l2 + v * (l3 - l2);
      return m0 + w * (m1 - m0);
   }

   public static double fbm(double x, double y, double z, int oct) {
      return fbmL(x, y, z, oct);
   }

   static double fbmL(double x, double y, double z, double oct) {
      double s = 0.0;
      double a = 0.5;
      int n = (int)oct;
      double frac = oct - n;

      for (int i = 0; i < n; i++) {
         s += a * noise(x, y, z);
         double nx = x * 1.62 + z * 1.18;
         double nz = z * 1.62 - x * 1.18;
         x = nx + 1.7;
         z = nz - 3.1;
         y = y * 2.01 + 0.73;
         a *= 0.5;
      }

      if (frac > 0.001) {
         s += a * frac * noise(x, y, z);
      }

      return s;
   }

   public static double ridged(double x, double y, double z, int oct) {
      double s = 0.0;
      double a = 0.5;

      for (int i = 0; i < oct; i++) {
         double n = 1.0 - Math.abs(noise(x, y, z));
         s += a * n * n;
         double nx = x * 1.62 + z * 1.18;
         double nz = z * 1.62 - x * 1.18;
         x = nx + 1.7;
         z = nz - 3.1;
         y = y * 2.01 + 0.73;
         a *= 0.5;
      }

      return s;
   }

   static double smooth(double e0, double e1, double x) {
      double t = (x - e0) / (e1 - e0);
      t = t < 0.0 ? 0.0 : (t > 1.0 ? 1.0 : t);
      return t * t * (3.0 - 2.0 * t);
   }

   static double clamp01(double v) {
      return v < 0.0 ? 0.0 : (v > 1.0 ? 1.0 : v);
   }

   static double log2(double v) {
      return Math.log(v) * 1.4426950408889634;
   }

   static double mix(double a, double b, double t) {
      return a + (b - a) * t;
   }

   static double[] rgb(int hex) {
      return new double[]{Math.pow((hex >> 16 & 0xFF) / 255.0, 2.2), Math.pow((hex >> 8 & 0xFF) / 255.0, 2.2), Math.pow((hex & 0xFF) / 255.0, 2.2)};
   }

   static double[] norm(double x, double y, double z) {
      double l = Math.sqrt(x * x + y * y + z * z);
      return new double[]{x / l, y / l, z / l};
   }

   static double[] azel(double azDeg, double elDeg) {
      double a = Math.toRadians(azDeg);
      double e = Math.toRadians(elDeg);
      return new double[]{Math.sin(a) * Math.cos(e), Math.sin(e), -Math.cos(a) * Math.cos(e)};
   }

   static void add(SkyGen.C c, double[] col, double k) {
      c.r = c.r + col[0] * k;
      c.g = c.g + col[1] * k;
      c.b = c.b + col[2] * k;
   }

   static void set(SkyGen.C c, double[] col) {
      c.r = col[0];
      c.g = col[1];
      c.b = col[2];
   }

   static void mixTo(SkyGen.C c, double[] col, double t) {
      c.r = c.r + (col[0] - c.r) * t;
      c.g = c.g + (col[1] - c.g) * t;
      c.b = c.b + (col[2] - c.b) * t;
   }

   static void mul(SkyGen.C c, double k) {
      c.r *= k;
      c.g *= k;
      c.b *= k;
   }

   static double[] cross(double[] a, double[] b) {
      double[] r = new double[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
      double l = Math.sqrt(r[0] * r[0] + r[1] * r[1] + r[2] * r[2]);
      return new double[]{r[0] / l, r[1] / l, r[2] / l};
   }

   static double[][] frame(double[] d, double rollDeg) {
      double[] up = Math.abs(d[1]) < 0.95 ? new double[]{0.0, 1.0, 0.0} : new double[]{1.0, 0.0, 0.0};
      double[] ex = cross(up, d);
      double[] ey = cross(d, ex);
      double a = Math.toRadians(rollDeg);
      double ca = Math.cos(a);
      double sa = Math.sin(a);
      double[] rx = new double[]{ex[0] * ca + ey[0] * sa, ex[1] * ca + ey[1] * sa, ex[2] * ca + ey[2] * sa};
      double[] ry = new double[]{ey[0] * ca - ex[0] * sa, ey[1] * ca - ex[1] * sa, ey[2] * ca - ex[2] * sa};
      return new double[][]{rx, ry};
   }

   static double hash(int x, int y, int z, int k) {
      long h = x * 374761393L + y * 668265263L + z * 2147483647L + k * 144665L;
      h = (h ^ h >>> 13) * 1274126177L;
      h ^= h >>> 16;
      return (h & 16777215L) / 1.6777215E7;
   }

   static {
      for (int i = 0; i <= 4097; i++) {
         GAMMA[i] = (float)(255.0 * Math.pow(Math.min(1.0, i / 4096.0), 0.45454545454545453));
      }

      TL = ThreadLocal.withInitial(SkyGen.C::new);
      P = new int[512];
      int[] p = new int[256];
      int i = 0;

      while (i < 256) {
         p[i] = i++;
      }

      Random r = new Random(1337L);

      for (int ix = 255; ix > 0; ix--) {
         int j = r.nextInt(ix + 1);
         int t = p[ix];
         p[ix] = p[j];
         p[j] = t;
      }

      for (int ix = 0; ix < 512; ix++) {
         P[ix] = p[ix & 0xFF];
      }
   }

   static final class C {
      double r;
      double g;
      double b;
      double vis;
      double t0;
      double t1;
      double t2;
      double t3;
   }

   static final class Clouds {
      double height = 1.0;
      double scale = 1.0;
      double cov = 0.05;
      double soft = 0.3;
      double warp = 0.8;
      double oct = 6.0;
      double seed = 0.0;
      double fog = 12.0;
      double opacity = 1.0;
      double stretch = 1.0;
      double lightSide = 1.0;
      double silver = 1.0;
      double thickDark = 0.55;
      double[] lit = SkyGen.rgb(16777215);
      double[] shade = SkyGen.rgb(9345712);
   }

   interface F3 {
      double f(double var1, double var3, double var5);
   }

   static final class FloatList {
      float[] a = new float[4096];
      int n;

      void add(float v) {
         if (this.n == this.a.length) {
            this.a = Arrays.copyOf(this.a, this.n * 2);
         }

         this.a[this.n++] = v;
      }

      float[] toArray() {
         return Arrays.copyOf(this.a, this.n);
      }
   }

   static final class IntList {
      int[] a = new int[1024];
      int n;

      void add(int v) {
         if (this.n == this.a.length) {
            this.a = Arrays.copyOf(this.a, this.n * 2);
         }

         this.a[this.n++] = v;
      }

      int get(int i) {
         return this.a[i];
      }
   }

   public interface Preset {
      void color(double[] var1, double[] var2);
   }

   abstract static class Sky {
      String kind = "day";
      double exposure = 1.0;
      double detail = 1.0;
      int below = 1;
      double[] ground = SkyGen.rgb(1711654);
      double[] groundFar = null;
      double groundFall = 0.22;
      double groundNoise = 0.0;
      boolean starsBelow = false;
      double[] water = SkyGen.rgb(464162);
      double waves = 1.0;
      double rough = 0.1;
      double glitter = 0.0;
      double mirror = 1.0;
      SkyGen.Clouds sea;
      double[] seaGap = SkyGen.rgb(6058900);
      double[] sun = SkyGen.norm(0.0, 1.0, 0.0);
      double[] sunCol = SkyGen.rgb(16774364);
      int stars = 0;
      double starMin = 0.05;
      double starMax = 4.0;
      double starSize = 1.0;
      double starHalo = 1.0;
      long starSeed = 7L;
      double[][] fine;
      int motes = 0;
      double moteBright = 1.0;
      double moteSize = 1.6;
      SkyGen.F3 starDensityFn;

      double moteDensity(double x, double y, double z) {
         return 1.0;
      }

      void moteColor(Random r, double[] o) {
         o[0] = 1.3;
         o[1] = 1.0;
         o[2] = 0.5;
      }

      abstract void sky(double var1, double var3, double var5, SkyGen.C var7);

      double starDensity(double x, double y, double z) {
         return this.starDensityFn != null ? this.starDensityFn.f(x, y, z) : SkyGen.smooth(-0.02, 0.28, y);
      }

      void starColor(Random r, double[] o) {
         double t = r.nextDouble();
         if (t < 0.18) {
            o[0] = 0.7;
            o[1] = 0.82;
            o[2] = 1.25;
         } else if (t < 0.45) {
            o[0] = 0.92;
            o[1] = 0.96;
            o[2] = 1.08;
         } else if (t < 0.75) {
            o[0] = 1.06;
            o[1] = 1.0;
            o[2] = 0.9;
         } else if (t < 0.93) {
            o[0] = 1.18;
            o[1] = 0.95;
            o[2] = 0.72;
         } else {
            o[0] = 1.25;
            o[1] = 0.78;
            o[2] = 0.58;
         }
      }

      final void evalFull(double x, double y, double z, SkyGen.C c) {
         c.r = c.g = c.b = 0.0;
         c.vis = 1.0;
         if (this.below != 0 && !(y >= 0.0)) {
            switch (this.below) {
               case 2:
                  this.ocean(x, y, z, c);
                  break;
               case 3:
                  this.cloudSea(x, y, z, c);
                  break;
               default:
                  this.groundBelow(x, y, z, c);
            }
         } else {
            this.sky(x, y, z, c);
         }
      }

      final void horizon(double x, double z, SkyGen.C c) {
         double hl = Math.sqrt(x * x + z * z);
         if (hl < 1.0E-9) {
            x = 1.0;
            z = 0.0;
            hl = 1.0;
         }

         c.r = c.g = c.b = 0.0;
         c.vis = 1.0;
         this.sky(x / hl, 0.0, z / hl, c);
      }

      void groundBelow(double x, double y, double z, SkyGen.C c) {
         this.horizon(x, z, c);
         double t = 1.0 - Math.exp(y / this.groundFall);
         double[] g = this.ground;
         double gr = g[0];
         double gg = g[1];
         double gb = g[2];
         if (this.groundFar != null) {
            double k = Math.exp(y / (this.groundFall * 2.5));
            gr += (this.groundFar[0] - gr) * k;
            gg += (this.groundFar[1] - gg) * k;
            gb += (this.groundFar[2] - gb) * k;
         }

         if (this.groundNoise > 0.0) {
            double k = 1.0 / (-y + 0.02);
            double px = x * k;
            double pz = z * k;
            double tex = 1.0
               + this.groundNoise * SkyGen.smooth(0.0, 0.3, -y) * SkyGen.fbmL(px * 0.6, 2.7, pz * 0.6, Math.max(2.0, 5.0 - SkyGen.log2(1.0 + k * 0.3)));
            gr *= tex;
            gg *= tex;
            gb *= tex;
         }

         c.r = c.r + (gr - c.r) * t;
         c.g = c.g + (gg - c.g) * t;
         c.b = c.b + (gb - c.b) * t;
         c.vis = this.starsBelow ? 1.0 : Math.max(0.0, 1.0 - t * 8.0) * c.vis;
      }

      void ocean(double x, double y, double z, SkyGen.C c) {
         double ny = -y;
         double k = 1.0 / (ny + 0.015);
         double px = x * k;
         double pz = z * k;
         double near = SkyGen.smooth(0.0, 0.25, ny);
         double amp = this.waves * 0.06 * near;
         double rx = x;
         double ry = ny;
         double rz = z;
         if (amp > 0.0) {
            double s = 0.9;
            double nx = SkyGen.noise(px * s, 0.37, pz * s) + 0.5 * SkyGen.noise(px * s * 2.7, 1.7, pz * s * 2.7);
            double nz = SkyGen.noise(px * s + 11.3, 4.1, pz * s) + 0.5 * SkyGen.noise(px * s * 2.7 + 5.1, 2.9, pz * s * 2.7);
            rx = x + nx * amp;
            rz = z + nz * amp;
            ry = ny + ny * 0.3 * SkyGen.noise(px * 0.7, 8.3, pz * 0.7) * near;
         }

         ry = Math.max(0.0, ry) * this.mirror;
         double l = Math.sqrt(rx * rx + ry * ry + rz * rz);
         c.r = c.g = c.b = 0.0;
         c.vis = 1.0;
         this.sky(rx / l, ry / l, rz / l, c);
         double m = 1.0 - ny;
         double m2 = m * m;
         double F = 0.03 + 0.97 * m2 * m2 * m;
         double far = Math.exp(-ny / 0.012);
         F += (1.0 - F) * far;
         c.r = this.water[0] + (c.r - this.water[0]) * F;
         c.g = this.water[1] + (c.g - this.water[1]) * F;
         c.b = this.water[2] + (c.b - this.water[2]) * F;
         if (this.glitter > 0.0) {
            double hx = this.sun[0] - x;
            double hy = ny + this.sun[1];
            double hz = this.sun[2] - z;
            if (hy > 1.0E-4 && this.sun[1] > -0.05) {
               double t2 = (hx * hx + hz * hz) / (hy * hy);
               double gl = Math.exp(-t2 / (this.rough * this.rough)) * this.glitter;
               if (gl > 1.0E-4) {
                  double sp = 0.55 + 0.9 * Math.max(0.0, SkyGen.noise(px * 6.0, 3.3, pz * 6.0 * 0.3));
                  gl *= sp * (1.0 - far);
                  c.r = c.r + this.sunCol[0] * gl;
                  c.g = c.g + this.sunCol[1] * gl;
                  c.b = c.b + this.sunCol[2] * gl;
               }
            }
         }

         c.vis = 0.0;
      }

      void cloudSea(double x, double y, double z, SkyGen.C c) {
         this.horizon(x, z, c);
         double hr = c.r;
         double hg = c.g;
         double hb = c.b;
         double ny = -y;
         SkyGen.Clouds P = this.sea;
         double k = P.height / (ny + 0.02);
         double px = x * k * P.scale;
         double pz = z * k * P.scale;
         double oct = Math.max(2.0, P.oct - SkyGen.log2(1.0 + k * P.scale * 0.35));
         double wx = SkyGen.fbmL(px * 0.35, P.seed, pz * 0.35, 3.0);
         double wz = SkyGen.fbmL(px * 0.35 + 5.2, P.seed + 3.0, pz * 0.35 + 1.3, 3.0);
         double qx = px + wx * P.warp;
         double qz = pz + wz * P.warp;
         double n = SkyGen.fbmL(qx, P.seed + 10.0, qz, oct);
         double dens = SkyGen.smooth(P.cov, P.cov + P.soft, n);
         double n2 = SkyGen.fbmL(qx + this.sun[0] * 0.3, P.seed + 10.0, qz + this.sun[2] * 0.3, Math.max(2.0, oct - 2.0));
         double slope = SkyGen.clamp01((n - n2) * 2.5 + 0.55);
         double[] L = P.lit;
         double[] S = P.shade;
         double lr = S[0] + (L[0] - S[0]) * slope;
         double lg = S[1] + (L[1] - S[1]) * slope;
         double lb = S[2] + (L[2] - S[2]) * slope;
         double cr = this.seaGap[0] + (lr - this.seaGap[0]) * dens;
         double cg = this.seaGap[1] + (lg - this.seaGap[1]) * dens;
         double cb = this.seaGap[2] + (lb - this.seaGap[2]) * dens;
         double haze = Math.exp(-ny / P.fog);
         c.r = cr + (hr - cr) * haze;
         c.g = cg + (hg - cg) * haze;
         c.b = cb + (hb - cb) * haze;
         c.vis = 0.0;
      }
   }

   static final class Stars {
      static final int CS = 16;
      static final int STRIDE = 9;
      int nc;
      int[] start;
      int[] item;
      float[] data;

      static SkyGen.Stars build(SkyGen.Sky s, int n) {
         if (s.stars <= 0 && s.motes <= 0) {
            return null;
         } else {
            Random r = new Random(s.starSeed * 7919L + 17L);
            double res = Math.sqrt(n / 1024.0);
            float ex = (float)s.exposure;
            int nc = (n + 16 - 1) / 16;
            SkyGen.FloatList ents = new SkyGen.FloatList();
            SkyGen.IntList faceOf = new SkyGen.IntList();
            double[] col = new double[3];
            double[] uv = new double[2];
            int total = s.stars + s.motes;

            for (int i = 0; i < total; i++) {
               boolean mote = i >= s.stars;
               double zz = r.nextDouble() * 2.0 - 1.0;
               double ph = r.nextDouble() * Math.PI * 2.0;
               double rq = Math.sqrt(1.0 - zz * zz);
               double x = rq * Math.cos(ph);
               double y = zz;
               double z = rq * Math.sin(ph);
               double u0 = r.nextDouble();
               double u1 = r.nextDouble();
               double sig;
               double amp;
               double halo;
               if (!mote) {
                  if (u0 > s.starDensity(x, zz, z)) {
                     continue;
                  }

                  double b = Math.min(s.starMax, s.starMin * Math.pow(Math.max(1.0E-6, u1), -0.72));
                  s.starColor(r, col);
                  sig = (0.36 + 0.16 * Math.log(b / s.starMin + 1.0)) * s.starSize * res;
                  amp = b;
                  halo = b > 1.0 ? 0.01 * b * s.starHalo : 0.0;
               } else {
                  if (u0 > s.moteDensity(x, zz, z)) {
                     continue;
                  }

                  s.moteColor(r, col);
                  double b = s.moteBright * (0.25 + 1.2 * Math.pow(u1, 3.0));
                  sig = (0.8 + s.moteSize * Math.pow(r.nextDouble(), 3.0)) * res;
                  amp = b;
                  halo = b * 0.1;
               }

               double rad = sig * (halo > 0.0 ? 12.0 : 3.6);
               rad = Math.min(rad, 48.0);
               float i2 = (float)(0.5 / (sig * sig));

               for (int f = 0; f < 6; f++) {
                  if (SkyGen.faceUV(f, x, y, z, uv)) {
                     double fx = uv[0] * n - 0.5;
                     double fy = uv[1] * n - 0.5;
                     if (!(fx < -rad) && !(fy < -rad) && !(fx > n - 1 + rad) && !(fy > n - 1 + rad)) {
                        ents.add((float)fx);
                        ents.add((float)fy);
                        ents.add((float)col[0] * ex);
                        ents.add((float)col[1] * ex);
                        ents.add((float)col[2] * ex);
                        ents.add(i2);
                        ents.add((float)amp);
                        ents.add((float)halo);
                        ents.add(mote ? 0.0F : 1.0F);
                        faceOf.add(f);
                        faceOf.add((int)Math.ceil(rad));
                     }
                  }
               }
            }

            SkyGen.Stars st = new SkyGen.Stars();
            st.nc = nc;
            st.data = ents.toArray();
            int cells = 6 * nc * nc;
            int[] cnt = new int[cells + 1];
            int m = st.data.length / 9;

            for (int pass = 0; pass < 2; pass++) {
               if (pass == 1) {
                  int acc = 0;

                  for (int c = 0; c < cells; c++) {
                     int t = cnt[c];
                     cnt[c] = acc;
                     acc += t;
                  }

                  cnt[cells] = acc;
                  st.start = (int[])cnt.clone();
                  st.item = new int[acc];
               }

               for (int e = 0; e < m; e++) {
                  int fx = faceOf.get(e * 2);
                  int rad = faceOf.get(e * 2 + 1);
                  float fxx = st.data[e * 9];
                  float fy = st.data[e * 9 + 1];
                  int x0 = Math.max(0, (int)Math.floor((fxx - rad) / 16.0F));
                  int x1 = Math.min(nc - 1, (int)Math.floor((fxx + rad) / 16.0F));
                  int y0 = Math.max(0, (int)Math.floor((fy - rad) / 16.0F));
                  int y1 = Math.min(nc - 1, (int)Math.floor((fy + rad) / 16.0F));

                  for (int cy = y0; cy <= y1; cy++) {
                     for (int cx = x0; cx <= x1; cx++) {
                        int c = (fx * nc + cy) * nc + cx;
                        if (pass == 0) {
                           cnt[c]++;
                        } else {
                           st.item[cnt[c]++] = e;
                        }
                     }
                  }
               }
            }

            return st;
         }
      }
   }

   static final class Towers {
      double h0 = 0.02;
      double h1 = 0.16;
      double freq = 2.2;
      double cut = 0.0;
      double bill = 7.0;
      double puff = 1.0;
      double soft = 0.015;
      double seed = 0.0;
      double haze = 0.035;
      double sunSide = 0.35;
      double[] lit = SkyGen.rgb(16777215);
      double[] shade = SkyGen.rgb(9214642);
   }
}
