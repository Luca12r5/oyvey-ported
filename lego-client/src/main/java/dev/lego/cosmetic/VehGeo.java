package dev.lego.cosmetic;

import java.util.Arrays;
import java.util.TreeSet;

final class VehGeo {
   static final float[] UV = new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F};
   static final float[] UP = new float[]{0.0F, 1.0F, 0.0F};

   private VehGeo() {
   }

   static float[] p(float x, float y, float z) {
      return new float[]{x, y, z};
   }

   static int argb(int a, int rgb) {
      return Math.max(0, Math.min(255, a)) << 24 | rgb & 16777215;
   }

   static float[] lerp(float[] a, float[] b, float t) {
      return p(lerp(a[0], b[0], t), lerp(a[1], b[1], t), lerp(a[2], b[2], t));
   }

   static void steer(G g, float[] pivot, float tilt, float deg) {
      g.translate(pivot[0], pivot[1], pivot[2]);
      g.rotX(-tilt);
      g.rotY(deg);
      g.rotX(tilt);
      g.translate(-pivot[0], -pivot[1], -pivot[2]);
   }

   static float sin(double v) {
      return (float)Math.sin(v);
   }

   static float cos(double v) {
      return (float)Math.cos(v);
   }

   static float clamp(float v, float a, float b) {
      return v < a ? a : (v > b ? b : v);
   }

   static float lerp(float a, float b, float t) {
      return a + (b - a) * t;
   }

   static float smooth(float e0, float e1, float x) {
      float t = clamp((x - e0) / (e1 - e0), 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   private static boolean same(float[] a, float[] b) {
      return Math.abs(a[0] - b[0]) < 1.0E-5F && Math.abs(a[1] - b[1]) < 1.0E-5F && Math.abs(a[2] - b[2]) < 1.0E-5F;
   }

   private static float[] sub(float[] a, float[] b) {
      return new float[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
   }

   private static float[] cross(float[] a, float[] b) {
      return new float[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
   }

   private static float dot(float[] a, float[] b) {
      return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
   }

   private static float[] norm(float[] a) {
      float l = (float)Math.sqrt(dot(a, a));
      return l < 1.0E-9F ? new float[]{0.0F, 1.0F, 0.0F} : new float[]{a[0] / l, a[1] / l, a[2] / l};
   }

   static void q(G g, String t, float[] a, float[] b, float[] c, float[] d, float[] uv) {
      float[][] v = new float[][]{a, b, c, d};

      for (int k = 0; k < 4; k++) {
         float[] A = v[k];
         float[] B = v[k + 1 & 3];
         float[] D = v[k + 3 & 3];
         if (!same(A, B) && !same(A, D) && !same(B, D)) {
            float[] n = cross(sub(B, A), sub(D, A));
            if (!(dot(n, n) < 1.0E-12F)) {
               int k1 = k + 1 & 3;
               int k2 = k + 2 & 3;
               int k3 = k + 3 & 3;
               g.quad(
                  t,
                  v[k],
                  v[k1],
                  v[k2],
                  v[k3],
                  new float[]{uv[k * 2], uv[k * 2 + 1], uv[k1 * 2], uv[k1 * 2 + 1], uv[k2 * 2], uv[k2 * 2 + 1], uv[k3 * 2], uv[k3 * 2 + 1]}
               );
               return;
            }
         }
      }
   }

   static void q(G g, String t, float[] a, float[] b, float[] c, float[] d) {
      q(g, t, a, b, c, d, UV);
   }

   static void qo(G g, String t, float[] a, float[] b, float[] c, float[] d, float[] inside, float[] uv) {
      float[] n = cross(sub(c, a), sub(d, b));
      float[] ctr = new float[]{(a[0] + b[0] + c[0] + d[0]) / 4.0F, (a[1] + b[1] + c[1] + d[1]) / 4.0F, (a[2] + b[2] + c[2] + d[2]) / 4.0F};
      if (dot(n, sub(ctr, inside)) >= 0.0F) {
         q(g, t, a, b, c, d, uv);
      } else {
         q(g, t, a, d, c, b, new float[]{uv[0], uv[1], uv[6], uv[7], uv[4], uv[5], uv[2], uv[3]});
      }
   }

   static void tri(G g, String t, float[] a, float[] b, float[] c) {
      q(g, t, a, b, c, c, new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F});
   }

   static void box(G g, String t, float x0, float y0, float z0, float x1, float y1, float z1) {
      float[] c = new float[]{(x0 + x1) / 2.0F, (y0 + y1) / 2.0F, (z0 + z1) / 2.0F};
      float[][] P = new float[8][];

      for (int i = 0; i < 8; i++) {
         P[i] = p((i & 1) == 0 ? x0 : x1, (i & 2) == 0 ? y0 : y1, (i & 4) == 0 ? z0 : z1);
      }

      int[][] F = new int[][]{{0, 1, 3, 2}, {4, 5, 7, 6}, {0, 2, 6, 4}, {1, 3, 7, 5}, {0, 1, 5, 4}, {2, 3, 7, 6}};

      for (int[] f : F) {
         qo(g, t, P[f[0]], P[f[1]], P[f[2]], P[f[3]], c, UV);
      }
   }

   static void box6(G g, String t, String[] faces, float x0, float y0, float z0, float x1, float y1, float z1) {
      float[] var10000 = new float[]{(x0 + x1) / 2.0F, (y0 + y1) / 2.0F, (z0 + z1) / 2.0F};
      float[][] P = new float[8][];

      for (int i = 0; i < 8; i++) {
         P[i] = p((i & 1) == 0 ? x0 : x1, (i & 2) == 0 ? y0 : y1, (i & 4) == 0 ? z0 : z1);
      }

      int[][] F = new int[][]{{4, 5, 7, 6}, {1, 0, 2, 3}, {0, 4, 6, 2}, {5, 1, 3, 7}, {6, 7, 3, 2}, {0, 1, 5, 4}};

      for (int i = 0; i < 6; i++) {
         String tx = faces != null && faces[i] != null ? faces[i] : t;
         int[] f = F[i];
         q(
            g,
            tx,
            P[f[0]],
            P[f[1]],
            P[f[2]],
            P[f[3]],
            i == 4 ? new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F} : new float[]{1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F}
         );
      }
   }

   static void rbox(G g, String t, float x0, float y0, float z0, float x1, float y1, float z1, float b) {
      b = Math.min(b, Math.min(x1 - x0, Math.min(y1 - y0, z1 - z0)) * 0.49F);
      float[] lo = new float[]{x0 + b, y0 + b, z0 + b};
      float[] hi = new float[]{x1 - b, y1 - b, z1 - b};
      float[] c = new float[]{(x0 + x1) / 2.0F, (y0 + y1) / 2.0F, (z0 + z1) / 2.0F};
      int[][] sq = new int[][]{{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
      float[] mid = new float[]{0.3F, 0.3F, 0.7F, 0.3F, 0.7F, 0.7F, 0.3F, 0.7F};

      for (int ax = 0; ax < 3; ax++) {
         for (int s = -1; s <= 1; s += 2) {
            int u = (ax + 1) % 3;
            int v = (ax + 2) % 3;
            float[][] pts = new float[4][];

            for (int k = 0; k < 4; k++) {
               float[] pp = new float[3];
               pp[ax] = (s < 0 ? lo[ax] : hi[ax]) + s * b;
               pp[u] = sq[k][0] < 0 ? lo[u] : hi[u];
               pp[v] = sq[k][1] < 0 ? lo[v] : hi[v];
               pts[k] = pp;
            }

            qo(g, t, pts[0], pts[1], pts[2], pts[3], c, UV);
         }
      }

      if (!(b <= 1.0E-4F)) {
         for (int a = 0; a < 3; a++) {
            for (int cc = a + 1; cc < 3; cc++) {
               int tt = 3 - a - cc;

               for (int sa = -1; sa <= 1; sa += 2) {
                  for (int sc = -1; sc <= 1; sc += 2) {
                     float[][] pts = new float[4][];
                     int[] ts = new int[]{-1, 1, 1, -1};

                     for (int k = 0; k < 4; k++) {
                        float[] pp = new float[3];
                        pp[a] = sa < 0 ? lo[a] : hi[a];
                        pp[cc] = sc < 0 ? lo[cc] : hi[cc];
                        pp[tt] = ts[k] < 0 ? lo[tt] : hi[tt];
                        if (k < 2) {
                           pp[a] += sa * b;
                        } else {
                           pp[cc] += sc * b;
                        }

                        pts[k] = pp;
                     }

                     qo(g, t, pts[0], pts[1], pts[2], pts[3], c, mid);
                  }
               }
            }
         }

         for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
               for (int sz = -1; sz <= 1; sz += 2) {
                  float px = sx < 0 ? lo[0] : hi[0];
                  float py = sy < 0 ? lo[1] : hi[1];
                  float pz = sz < 0 ? lo[2] : hi[2];
                  float[] A = p(px + sx * b, py, pz);
                  float[] B = p(px, py + sy * b, pz);
                  float[] C = p(px, py, pz + sz * b);
                  qo(g, t, A, B, C, C, c, mid);
               }
            }
         }
      }
   }

   static void beam(G g, String t, float[] a, float[] b, float w0, float h0, float w1, float h1, float[] up) {
      float[] d = norm(sub(b, a));
      float[] s = cross(d, up);
      if (dot(s, s) < 1.0E-8F) {
         s = cross(d, new float[]{1.0F, 0.0F, 0.0F});
      }

      if (dot(s, s) < 1.0E-8F) {
         s = cross(d, new float[]{0.0F, 0.0F, 1.0F});
      }

      s = norm(s);
      float[] u = norm(cross(s, d));
      float[][] P = new float[8][];

      for (int i = 0; i < 8; i++) {
         float[] base = (i & 4) == 0 ? a : b;
         float w = ((i & 4) == 0 ? w0 : w1) / 2.0F;
         float h = ((i & 4) == 0 ? h0 : h1) / 2.0F;
         float sw = (i & 1) == 0 ? -w : w;
         float sh = (i & 2) == 0 ? -h : h;
         P[i] = p(base[0] + s[0] * sw + u[0] * sh, base[1] + s[1] * sw + u[1] * sh, base[2] + s[2] * sw + u[2] * sh);
      }

      float[] c = new float[]{(a[0] + b[0]) / 2.0F, (a[1] + b[1]) / 2.0F, (a[2] + b[2]) / 2.0F};
      int[][] F = new int[][]{{0, 1, 3, 2}, {4, 5, 7, 6}, {0, 2, 6, 4}, {1, 3, 7, 5}, {0, 1, 5, 4}, {2, 3, 7, 6}};

      for (int[] f : F) {
         qo(g, t, P[f[0]], P[f[1]], P[f[2]], P[f[3]], c, UV);
      }
   }

   static void beam(G g, String t, float[] a, float[] b, float w, float h) {
      beam(g, t, a, b, w, h, w, h, UP);
   }

   static void tube(G g, String t, float[] a, float[] b, float r0, float r1, int n, boolean caps) {
      float[] d = norm(sub(b, a));
      float[] ref = Math.abs(d[1]) < 0.9F ? new float[]{0.0F, 1.0F, 0.0F} : new float[]{1.0F, 0.0F, 0.0F};
      float[] u = norm(cross(d, ref));
      float[] v = cross(d, u);
      float[][] A = new float[n][];
      float[][] B = new float[n][];

      for (int i = 0; i < n; i++) {
         double an = (Math.PI * 2) * i / n;
         float c = (float)Math.cos(an);
         float s = (float)Math.sin(an);
         float ox = u[0] * c + v[0] * s;
         float oy = u[1] * c + v[1] * s;
         float oz = u[2] * c + v[2] * s;
         A[i] = p(a[0] + ox * r0, a[1] + oy * r0, a[2] + oz * r0);
         B[i] = p(b[0] + ox * r1, b[1] + oy * r1, b[2] + oz * r1);
      }

      for (int i = 0; i < n; i++) {
         int j = (i + 1) % n;
         float[] var10000 = new float[]{(a[0] + b[0]) / 2.0F, (a[1] + b[1]) / 2.0F, (a[2] + b[2]) / 2.0F};
         float[] mid = new float[]{
            (A[i][0] + A[j][0] + B[i][0] + B[j][0]) / 4.0F, (A[i][1] + A[j][1] + B[i][1] + B[j][1]) / 4.0F, (A[i][2] + A[j][2] + B[i][2] + B[j][2]) / 4.0F
         };
         float tt = dot(sub(mid, a), d);
         float[] ap = new float[]{a[0] + d[0] * tt, a[1] + d[1] * tt, a[2] + d[2] * tt};
         qo(g, t, A[i], A[j], B[j], B[i], ap, new float[]{(float)i / n, 0.0F, (float)(i + 1) / n, 0.0F, (float)(i + 1) / n, 1.0F, (float)i / n, 1.0F});
      }

      if (caps) {
         for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            float[] inA = new float[]{a[0] + d[0], a[1] + d[1], a[2] + d[2]};
            float[] inB = new float[]{b[0] - d[0], b[1] - d[1], b[2] - d[2]};
            if (r0 > 0.001F) {
               qo(g, t, a, A[i], A[j], A[j], inA, UV);
            }

            if (r1 > 0.001F) {
               qo(g, t, b, B[i], B[j], B[j], inB, UV);
            }
         }
      }
   }

   static void pipe(G g, String t, float[][] pts, float r, int n, boolean caps) {
      float[][] sec = new float[n][];

      for (int i = 0; i < n; i++) {
         sec[i] = new float[]{cos((Math.PI * 2) * i / n) * r, sin((Math.PI * 2) * i / n) * r};
      }

      sweep(g, t, pts, sec, null, caps);
   }

   static void strip(G g, String t, float[][] pts, float w, float h, float[] ref, boolean caps) {
      float[][] sec = new float[][]{{-w / 2.0F, -h / 2.0F}, {w / 2.0F, -h / 2.0F}, {w / 2.0F, h / 2.0F}, {-w / 2.0F, h / 2.0F}};
      sweep(g, t, pts, sec, ref, caps);
   }

   static void sweep(G g, String t, float[][] pts, float[][] sec, float[] ref, boolean caps) {
      int m = pts.length;
      int n = sec.length;
      float[][][] R = new float[m][n][];
      float[][] ctr = new float[m][];
      float[] prevSd = null;

      for (int k = 0; k < m; k++) {
         float[] a = pts[Math.max(0, k - 1)];
         float[] b = pts[Math.min(m - 1, k + 1)];
         float[] d = norm(sub(b, a));
         float[] sd;
         if (ref == null && prevSd != null) {
            float k2 = dot(prevSd, d);
            sd = p(prevSd[0] - d[0] * k2, prevSd[1] - d[1] * k2, prevSd[2] - d[2] * k2);
            if (dot(sd, sd) < 1.0E-6F) {
               sd = cross(d, new float[]{0.0F, 0.0F, 1.0F});
            }
         } else {
            float[] rf = ref;
            if (ref == null) {
               rf = Math.abs(d[1]) < 0.8F ? new float[]{0.0F, 1.0F, 0.0F} : new float[]{1.0F, 0.0F, 0.0F};
            }

            sd = cross(d, rf);
            if (dot(sd, sd) < 1.0E-6F) {
               sd = cross(d, new float[]{0.0F, 0.0F, 1.0F});
            }
         }

         sd = norm(sd);
         prevSd = sd;
         float[] up = norm(cross(sd, d));
         float sc = 1.0F;
         if (k > 0 && k < m - 1) {
            float[] d0 = norm(sub(pts[k], pts[k - 1]));
            float[] d1 = norm(sub(pts[k + 1], pts[k]));
            float c = dot(d0, d1);
            sc = 1.0F / (float)Math.max(0.5, Math.sqrt((1.0F + c) / 2.0F));
         }

         ctr[k] = pts[k];

         for (int i = 0; i < n; i++) {
            float s = sec[i][0];
            float u = sec[i][1] * 1.0F;
            R[k][i] = p(pts[k][0] + (sd[0] * s + up[0] * u) * sc, pts[k][1] + (sd[1] * s + up[1] * u) * sc, pts[k][2] + (sd[2] * s + up[2] * u) * sc);
         }
      }

      for (int k = 0; k + 1 < m; k++) {
         float[] hint = new float[]{(ctr[k][0] + ctr[k + 1][0]) / 2.0F, (ctr[k][1] + ctr[k + 1][1]) / 2.0F, (ctr[k][2] + ctr[k + 1][2]) / 2.0F};

         for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            qo(
               g,
               t,
               R[k][i],
               R[k][j],
               R[k + 1][j],
               R[k + 1][i],
               hint,
               new float[]{
                  (float)i / n,
                  (float)k / (m - 1),
                  (float)(i + 1) / n,
                  (float)k / (m - 1),
                  (float)(i + 1) / n,
                  (float)(k + 1) / (m - 1),
                  (float)i / n,
                  (float)(k + 1) / (m - 1)
               }
            );
         }
      }

      if (caps) {
         float[] in0 = pts[1];
         float[] in1 = pts[m - 2];

         for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            qo(g, t, pts[0], R[0][i], R[0][j], R[0][j], in0, UV);
            qo(g, t, pts[m - 1], R[m - 1][i], R[m - 1][j], R[m - 1][j], in1, UV);
         }
      }
   }

   static float[][] arc(float x, float cy, float cz, float r, float a0, float a1, int n) {
      float[][] o = new float[n + 1][];

      for (int i = 0; i <= n; i++) {
         double a = Math.toRadians(a0 + (a1 - a0) * i / n);
         o[i] = p(x, cy + (float)Math.sin(a) * r, cz + (float)Math.cos(a) * r);
      }

      return o;
   }

   static void torus(G g, String t, float R, float r, int seg, int side) {
      float[][] prof = new float[side + 1][];

      for (int i = 0; i <= side; i++) {
         double a = (-Math.PI / 2) + (Math.PI * 2) * i / side;
         prof[i] = new float[]{R + (float)Math.cos(a) * r, (float)Math.sin(a) * r};
      }

      lathe(g, t, prof, seg);
   }

   static void ball(G g, String t, float x, float y, float z, float r, int seg, int rings) {
      g.push();
      g.translate(x, y, z);
      g.sphere(t, r, seg, rings, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
   }

   static void ellipsoid(G g, String t, float x, float y, float z, float rx, float ry, float rz, int seg, int rings) {
      g.push();
      g.translate(x, y, z);
      g.scale(rx, ry, rz);
      g.sphere(t, 1.0F, seg, rings, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
   }

   static void lathe(G g, String[] tex, float[][] prof, int seg) {
      int n = prof.length;
      float total = 0.0F;
      float[] acc = new float[n];

      for (int i = 1; i < n; i++) {
         float dr = prof[i][0] - prof[i - 1][0];
         float dy = prof[i][1] - prof[i - 1][1];
         total += (float)Math.sqrt(dr * dr + dy * dy);
         acc[i] = total;
      }

      for (int i = 0; i + 1 < n; i++) {
         String t = tex[Math.min(i, tex.length - 1)];
         float v0 = total > 0.0F ? acc[i] / total : 0.0F;
         float v1 = total > 0.0F ? acc[i + 1] / total : 1.0F;

         for (int j = 0; j < seg; j++) {
            double a0 = (Math.PI * 2) * j / seg;
            double a1 = (Math.PI * 2) * (j + 1) / seg;
            float c0 = (float)Math.cos(a0);
            float s0 = (float)Math.sin(a0);
            float c1 = (float)Math.cos(a1);
            float s1 = (float)Math.sin(a1);
            float r0 = prof[i][0];
            float y0 = prof[i][1];
            float r1 = prof[i + 1][0];
            float y1 = prof[i + 1][1];
            float u0 = (float)j / seg;
            float u1 = (float)(j + 1) / seg;
            q(
               g,
               t,
               p(c0 * r1, y1, s0 * r1),
               p(c1 * r1, y1, s1 * r1),
               p(c1 * r0, y0, s1 * r0),
               p(c0 * r0, y0, s0 * r0),
               new float[]{u0, v1, u1, v1, u1, v0, u0, v0}
            );
         }
      }
   }

   static void lathe(G g, String t, float[][] prof, int seg) {
      lathe(g, new String[]{t}, prof, seg);
   }

   static void loft(G g, float[] zs, VehGeo.Sec sec, VehGeo.TexFn tex, boolean capStart, boolean capEnd, float vTop, float vBot) {
      loft(g, zs, sec, tex, capStart, capEnd, vTop, vBot, false);
   }

   static void loft(G g, float[] zs, VehGeo.Sec sec, VehGeo.TexFn tex, boolean capStart, boolean capEnd, float vTop, float vBot, boolean vByX) {
      loft(g, zs, sec, tex, capStart, capEnd, vTop, vBot, vByX, false);
   }

   static void loft(
      G g, float[] zs, VehGeo.Sec sec, VehGeo.TexFn tex, boolean capStart, boolean capEnd, float vTop, float vBot, boolean vByX, boolean pairedCaps
   ) {
      int vc = vByX ? 0 : 1;
      float[][][] S = new float[zs.length][][];

      for (int k = 0; k < zs.length; k++) {
         S[k] = sec.at(zs[k]);
      }

      float zl = zs[0];
      float zr = zs[zs.length - 1];

      for (int k = 0; k + 1 < zs.length; k++) {
         float[][] a = S[k];
         float[][] b = S[k + 1];
         int n = a.length;

         for (int j = 0; j < n; j++) {
            int j1 = (j + 1) % n;
            String t = tex.at(k, j, zs[k], a[j], a[j1]);
            if (t != null) {
               float[] A = p(a[j][0], a[j][1], zs[k]);
               float[] B = p(a[j1][0], a[j1][1], zs[k]);
               float[] C = p(b[j1][0], b[j1][1], zs[k + 1]);
               float[] D = p(b[j][0], b[j][1], zs[k + 1]);
               q(
                  g,
                  t,
                  A,
                  B,
                  C,
                  D,
                  new float[]{
                     uvU(zs[k], zl, zr),
                     uvV(A[vc], vTop, vBot),
                     uvU(zs[k], zl, zr),
                     uvV(B[vc], vTop, vBot),
                     uvU(zs[k + 1], zl, zr),
                     uvV(C[vc], vTop, vBot),
                     uvU(zs[k + 1], zl, zr),
                     uvV(D[vc], vTop, vBot)
                  }
               );
            }
         }
      }

      if (pairedCaps) {
         if (capStart) {
            pairCap(g, S[0], zs[0], tex.at(-1, 0, zs[0], S[0][0], S[0][0]), 1);
         }

         if (capEnd) {
            pairCap(g, S[zs.length - 1], zs[zs.length - 1], tex.at(zs.length - 1, 0, zs[zs.length - 1], S[0][0], S[0][0]), -1);
         }
      } else {
         if (capStart) {
            cap(g, S[0], zs[0], tex.at(-1, 0, zs[0], S[0][0], S[0][0]), false, vTop, vBot);
         }

         if (capEnd) {
            cap(g, S[zs.length - 1], zs[zs.length - 1], tex.at(zs.length - 1, 0, zs[zs.length - 1], S[0][0], S[0][0]), true, vTop, vBot);
         }
      }
   }

   private static void pairCap(G g, float[][] s, float z, String t, int inward) {
      if (t != null) {
         int n = s.length;

         for (int i = 0; i < n / 2 - 1; i++) {
            float[] a = p(s[i][0], s[i][1], z);
            float[] b = p(s[i + 1][0], s[i + 1][1], z);
            float[] c = p(s[n - 2 - i][0], s[n - 2 - i][1], z);
            float[] d = p(s[n - 1 - i][0], s[n - 1 - i][1], z);
            float[] in = new float[]{(a[0] + b[0] + c[0] + d[0]) / 4.0F, (a[1] + b[1] + c[1] + d[1]) / 4.0F, z + inward};
            qo(g, t, a, b, c, d, in, UV);
         }
      }
   }

   private static float uvU(float z, float z0, float z1) {
      return z1 == z0 ? 0.0F : (z - z0) / (z1 - z0);
   }

   private static float uvV(float y, float t, float b) {
      return clamp((t - y) / (t - b), 0.0F, 1.0F);
   }

   private static void cap(G g, float[][] s, float z, String t, boolean front, float vTop, float vBot) {
      if (t != null) {
         float cx = 0.0F;
         float cy = 0.0F;

         for (float[] pt : s) {
            cx += pt[0];
            cy += pt[1];
         }

         cx /= s.length;
         cy /= s.length;
         float[] c = p(cx, cy, z);

         for (int j = 0; j < s.length; j++) {
            float[] a = p(s[j][0], s[j][1], z);
            float[] b = p(s[(j + 1) % s.length][0], s[(j + 1) % s.length][1], z);
            float[] uv = new float[]{0.5F, uvV(cy, vTop, vBot), 0.5F, uvV(a[1], vTop, vBot), 0.5F, uvV(b[1], vTop, vBot), 0.5F, uvV(b[1], vTop, vBot)};
            if (front) {
               q(g, t, c, a, b, b, uv);
            } else {
               q(g, t, c, b, a, a, new float[]{uv[0], uv[1], uv[4], uv[5], uv[2], uv[3], uv[2], uv[3]});
            }
         }
      }
   }

   static float[][] mirrorRing(float[][] half) {
      int n = half.length;
      float[][] r = new float[n * 2 - 2][];

      for (int i = 0; i < n; i++) {
         r[i] = new float[]{half[i][0], half[i][1]};
      }

      for (int i = n - 2; i >= 1; i--) {
         r[n + (n - 2 - i)] = new float[]{-half[i][0], half[i][1]};
      }

      return r;
   }

   static float curve(float x, float[] xs, float[] vs) {
      int n = xs.length;
      if (x <= xs[0]) {
         return vs[0];
      } else if (x >= xs[n - 1]) {
         return vs[n - 1];
      } else {
         int i = 0;

         while (i < n - 2 && x > xs[i + 1]) {
            i++;
         }

         float t = (x - xs[i]) / (xs[i + 1] - xs[i]);
         float p0 = vs[Math.max(0, i - 1)];
         float p1 = vs[i];
         float p2 = vs[i + 1];
         float p3 = vs[Math.min(n - 1, i + 2)];
         float m1 = (p2 - p0) * 0.5F * (xs[i + 1] - xs[i]) / Math.max(1.0E-4F, xs[Math.min(n - 1, i + 1)] - xs[Math.max(0, i - 1)]) * 2.0F;
         float m2 = (p3 - p1) * 0.5F * (xs[i + 1] - xs[i]) / Math.max(1.0E-4F, xs[Math.min(n - 1, i + 2)] - xs[i]) * 2.0F;
         float t2 = t * t;
         float t3 = t2 * t;
         return (2.0F * t3 - 3.0F * t2 + 1.0F) * p1 + (t3 - 2.0F * t2 + t) * m1 + (-2.0F * t3 + 3.0F * t2) * p2 + (t3 - t2) * m2;
      }
   }

   static float[] range(float a, float b, float step, float... extra) {
      TreeSet<Float> s = new TreeSet<>();
      int n = Math.max(1, (int)Math.ceil((b - a) / step));

      for (int i = 0; i <= n; i++) {
         s.add(a + (b - a) * i / n);
      }

      for (float e : extra) {
         if (e > a && e < b) {
            s.add(e);
         }
      }

      float[] out = new float[s.size()];
      int k = 0;
      float last = Float.NEGATIVE_INFINITY;

      for (float f : s) {
         if (f - last > 0.05F || k == 0) {
            out[k++] = f;
            last = f;
         }
      }

      return Arrays.copyOf(out, k);
   }

   static void wheel(G g, float R, float w, float rimR, int spokes, float spin, String tyre, String side, String rim, String disc, int seg) {
      g.push();
      g.rotX(spin);
      g.rotZ(-90.0F);
      float h = w / 2.0F;
      float sh = Math.min(0.9F, (R - rimR) * 0.45F);
      lathe(
         g,
         new String[]{side, tyre, tyre, tyre, side},
         new float[][]{{rimR + 0.05F, -h + 0.2F}, {R - sh, -h}, {R, -h + sh * 0.7F}, {R, h - sh * 0.7F}, {R - sh, h}, {rimR + 0.05F, h - 0.2F}},
         seg
      );
      float lipY = h - 0.12F;
      lathe(g, rim, new float[][]{{rimR + 0.05F, h - 0.2F}, {rimR - 0.1F, lipY + 0.02F}, {rimR - 0.35F, h - 0.45F}}, seg);
      lathe(g, rim, new float[][]{{rimR - 0.35F, h - 0.45F}, {rimR - 0.35F, -h + 0.3F}}, seg);
      lathe(g, side, new float[][]{{rimR - 0.35F, -h + 0.45F}, {0.01F, -h + 0.45F}}, seg);
      lathe(g, side, new float[][]{{0.01F, -h + 0.3F}, {rimR - 0.35F, -h + 0.3F}, {rimR + 0.05F, -h + 0.2F}}, seg);
      if (disc != null) {
         float dr = rimR - 0.75F;
         float dy = (-h + 0.45F + (h - 0.35F)) / 2.0F;
         float dt = Math.min(0.25F, (h - 0.35F - (-h + 0.45F)) / 2.0F - 0.1F);
         lathe(g, disc, new float[][]{{0.01F, dy - dt}, {dr, dy - dt}, {dr, dy + dt}, {0.01F, dy + dt}}, Math.max(8, seg * 2 / 3));
      }

      float fy = h - 0.35F;
      if (spokes > 0) {
         for (int i = 0; i < spokes; i++) {
            double an = (Math.PI * 2) * i / spokes;
            float c = (float)Math.cos(an);
            float s = (float)Math.sin(an);
            float r0 = Math.max(0.7F, rimR * 0.22F);
            float r1 = rimR - 0.3F;
            float[] a = p(c * r0, fy + 0.1F, s * r0);
            float[] b = p(c * r1, fy - 0.25F, s * r1);
            beam(g, rim, a, b, Math.max(0.35F, rimR * 0.2F), 0.35F, Math.max(0.25F, rimR * 0.1F), 0.3F, p(0.0F, 1.0F, 0.0F));
         }

         float hr = Math.max(0.8F, rimR * 0.3F);
         lathe(
            g,
            new String[]{rim, rim, "veh_flat:2A2C33"},
            new float[][]{{hr, fy - 0.4F}, {hr, fy + 0.15F}, {hr * 0.55F, fy + 0.35F}, {0.01F, fy + 0.4F}},
            Math.max(8, seg / 2)
         );
      } else {
         lathe(
            g,
            new String[]{rim, rim, "veh_flat:2A2C33"},
            new float[][]{{rimR - 0.35F, h - 0.45F}, {rimR * 0.55F, fy}, {rimR * 0.3F, fy + 0.1F}, {0.01F, fy + 0.3F}},
            seg
         );
      }

      g.pop();
   }

   interface Sec {
      float[][] at(float var1);
   }

   interface TexFn {
      String at(int var1, int var2, float var3, float[] var4, float[] var5);
   }
}
