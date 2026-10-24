package dev.lego.cosmetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

final class Cos3Geo {
   static final float[] FULL = new float[]{0.0F, 0.0F, 1.0F, 1.0F};

   private Cos3Geo() {
   }

   static float sin(double v) {
      return (float)Math.sin(v);
   }

   static float cos(double v) {
      return (float)Math.cos(v);
   }

   static float fract(float v) {
      return v - (float)Math.floor(v);
   }

   static float clamp(float v, float a, float b) {
      return v < a ? a : (v > b ? b : v);
   }

   static float smooth(float t) {
      t = clamp(t, 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   static int argb(int a, int rgb) {
      return Math.max(0, Math.min(255, a)) << 24 | rgb & 16777215;
   }

   static int alpha(double a, int rgb) {
      return argb((int)Math.round(a * 255.0), rgb);
   }

   private static void tri(G g, String t, float[] a, float[] b, float[] c, float[] uv) {
      g.quad(t, a, b, c, c, new float[]{uv[0], uv[1], uv[2], uv[3], uv[4], uv[5], uv[4], uv[5]});
   }

   private static float[] p(float x, float y, float z) {
      return new float[]{x, y, z};
   }

   static void bbox(G g, String t, float x0, float y0, float z0, float x1, float y1, float z1, float b, float[] r) {
      float m = Math.min(x1 - x0, Math.min(y1 - y0, z1 - z0)) * 0.45F;
      if (b > m) {
         b = m;
      }

      if (b < 0.02F) {
         g.box(t, x0, y0, z0, x1, y1, z1, r[0], r[1], r[2], r[3]);
      } else {
         float X0 = x0 + b;
         float X1 = x1 - b;
         float Y0 = y0 + b;
         float Y1 = y1 - b;
         float Z0 = z0 + b;
         float Z1 = z1 - b;
         float[] uv = new float[]{r[0], r[1], r[2], r[1], r[2], r[3], r[0], r[3]};
         float um = (r[0] + r[2]) / 2.0F;
         float vm = (r[1] + r[3]) / 2.0F;
         float[] uvc = new float[]{r[0], r[1], r[2], r[1], um, r[3], um, r[3]};
         g.quad(t, p(X0, Y1, z1), p(X1, Y1, z1), p(X1, Y0, z1), p(X0, Y0, z1), uv);
         g.quad(t, p(X1, Y1, z0), p(X0, Y1, z0), p(X0, Y0, z0), p(X1, Y0, z0), uv);
         g.quad(t, p(x0, Y1, Z0), p(x0, Y1, Z1), p(x0, Y0, Z1), p(x0, Y0, Z0), uv);
         g.quad(t, p(x1, Y1, Z1), p(x1, Y1, Z0), p(x1, Y0, Z0), p(x1, Y0, Z1), uv);
         g.quad(t, p(X0, y1, Z0), p(X1, y1, Z0), p(X1, y1, Z1), p(X0, y1, Z1), uv);
         g.quad(t, p(X0, y0, Z1), p(X1, y0, Z1), p(X1, y0, Z0), p(X0, y0, Z0), uv);
         g.quad(t, p(X0, y1, Z1), p(X1, y1, Z1), p(X1, Y1, z1), p(X0, Y1, z1), uv);
         g.quad(t, p(X1, y1, Z0), p(X0, y1, Z0), p(X0, Y1, z0), p(X1, Y1, z0), uv);
         g.quad(t, p(x0, Y1, Z0), p(x0, Y1, Z1), p(X0, y1, Z1), p(X0, y1, Z0), uv);
         g.quad(t, p(X1, y1, Z0), p(X1, y1, Z1), p(x1, Y1, Z1), p(x1, Y1, Z0), uv);
         g.quad(t, p(X0, Y0, z1), p(X1, Y0, z1), p(X1, y0, Z1), p(X0, y0, Z1), uv);
         g.quad(t, p(X1, Y0, z0), p(X0, Y0, z0), p(X0, y0, Z0), p(X1, y0, Z0), uv);
         g.quad(t, p(X0, y0, Z0), p(X0, y0, Z1), p(x0, Y0, Z1), p(x0, Y0, Z0), uv);
         g.quad(t, p(x1, Y0, Z0), p(x1, Y0, Z1), p(X1, y0, Z1), p(X1, y0, Z0), uv);
         g.quad(t, p(x0, Y1, Z1), p(X0, Y1, z1), p(X0, Y0, z1), p(x0, Y0, Z1), uv);
         g.quad(t, p(X1, Y1, z1), p(x1, Y1, Z1), p(x1, Y0, Z1), p(X1, Y0, z1), uv);
         g.quad(t, p(X0, Y1, z0), p(x0, Y1, Z0), p(x0, Y0, Z0), p(X0, Y0, z0), uv);
         g.quad(t, p(x1, Y1, Z0), p(X1, Y1, z0), p(X1, Y0, z0), p(x1, Y0, Z0), uv);

         for (int sx = 0; sx < 2; sx++) {
            for (int sy = 0; sy < 2; sy++) {
               for (int sz = 0; sz < 2; sz++) {
                  float cx = sx == 0 ? x0 : x1;
                  float cy = sy == 0 ? y0 : y1;
                  float cz = sz == 0 ? z0 : z1;
                  float ix = sx == 0 ? X0 : X1;
                  float iy = sy == 0 ? Y0 : Y1;
                  float iz = sz == 0 ? Z0 : Z1;
                  tri(g, t, p(cx, iy, iz), p(ix, cy, iz), p(ix, iy, cz), uvc);
               }
            }
         }
      }
   }

   static void bbox(G g, String t, float x0, float y0, float z0, float x1, float y1, float z1, float b) {
      bbox(g, t, x0, y0, z0, x1, y1, z1, b, FULL);
   }

   static void cbox(G g, String t, float cx, float cy, float cz, float w, float h, float d, float b) {
      bbox(g, t, cx - w / 2.0F, cy - h / 2.0F, cz - d / 2.0F, cx + w / 2.0F, cy + h / 2.0F, cz + d / 2.0F, b, FULL);
   }

   static void lathe(G g, String t, float[] r, float[] y, int seg, float u0, float v0, float u1, float v1) {
      int n = r.length;

      for (int j = 0; j + 1 < n; j++) {
         float va = v0 + (v1 - v0) * j / (n - 1);
         float vb = v0 + (v1 - v0) * (j + 1) / (n - 1);

         for (int i = 0; i < seg; i++) {
            double a0 = (Math.PI * 2) * i / seg;
            double a1 = (Math.PI * 2) * (i + 1) / seg;
            float c0 = (float)Math.cos(a0);
            float s0 = (float)Math.sin(a0);
            float c1 = (float)Math.cos(a1);
            float s1 = (float)Math.sin(a1);
            float ua = u0 + (u1 - u0) * i / seg;
            float ub = u0 + (u1 - u0) * (i + 1) / seg;
            g.quad(
               t,
               p(c0 * r[j + 1], y[j + 1], s0 * r[j + 1]),
               p(c1 * r[j + 1], y[j + 1], s1 * r[j + 1]),
               p(c1 * r[j], y[j], s1 * r[j]),
               p(c0 * r[j], y[j], s0 * r[j]),
               new float[]{ua, vb, ub, vb, ub, va, ua, va}
            );
         }
      }
   }

   static void lathe(G g, String t, float[] r, float[] y, int seg) {
      lathe(g, t, r, y, seg, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   static void gem(G g, String t, float r, float top, float bot, int n) {
      lathe(g, t, new float[]{0.0F, r * 0.95F, r, r * 0.62F, 0.0F}, new float[]{-bot, -bot * 0.12F, bot * 0.02F, top * 0.55F, top}, n);
   }

   static void shard(G g, String t, float r, float h) {
      lathe(g, t, new float[]{0.0F, r * 0.8F, r, r * 0.9F, 0.0F}, new float[]{0.0F, 0.05F * h, 0.25F * h, 0.72F * h, h}, 6);
   }

   static void ball(G g, String t, float x, float y, float z, float r, int seg) {
      g.push();
      g.translate(x, y, z);
      g.sphere(t, r, seg, Math.max(3, seg / 2), 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
   }

   static void extrude(G g, String t, float[][] poly, float z0, float z1, float[] cap, float[] side) {
      int n = poly.length;
      float minx = 1.0E9F;
      float miny = 1.0E9F;
      float maxx = -1.0E9F;
      float maxy = -1.0E9F;

      for (float[] q : poly) {
         minx = Math.min(minx, q[0]);
         maxx = Math.max(maxx, q[0]);
         miny = Math.min(miny, q[1]);
         maxy = Math.max(maxy, q[1]);
      }

      float bw = Math.max(1.0E-4F, maxx - minx);
      float bh = Math.max(1.0E-4F, maxy - miny);

      for (int[] tr : triangulate(poly)) {
         float[] uv = new float[6];

         for (int k = 0; k < 3; k++) {
            float[] q = poly[tr[k]];
            uv[k * 2] = cap[0] + (cap[2] - cap[0]) * (maxx - q[0]) / bw;
            uv[k * 2 + 1] = cap[1] + (cap[3] - cap[1]) * (maxy - q[1]) / bh;
         }

         float[] a = poly[tr[0]];
         float[] b = poly[tr[1]];
         float[] c = poly[tr[2]];
         tri(g, t, p(a[0], a[1], z1), p(b[0], b[1], z1), p(c[0], c[1], z1), uv);
         float[] uvb = (float[])uv.clone();

         for (int k = 0; k < 3; k++) {
            uvb[k * 2] = cap[0] + cap[2] - uv[k * 2];
         }

         tri(g, t, p(c[0], c[1], z0), p(b[0], b[1], z0), p(a[0], a[1], z0), new float[]{uvb[4], uvb[5], uvb[2], uvb[3], uvb[0], uvb[1]});
      }

      for (int i = 0; i < n; i++) {
         float[] a = poly[i];
         float[] b = poly[(i + 1) % n];
         g.quad(
            t,
            p(a[0], a[1], z1),
            p(b[0], b[1], z1),
            p(b[0], b[1], z0),
            p(a[0], a[1], z0),
            new float[]{side[0], side[1], side[2], side[1], side[2], side[3], side[0], side[3]}
         );
      }
   }

   static void extrude(G g, String t, float[][] poly, float z0, float z1) {
      extrude(g, t, poly, z0, z1, FULL, new float[]{0.45F, 0.45F, 0.55F, 0.55F});
   }

   static List<int[]> triangulate(float[][] poly) {
      int n = poly.length;
      List<int[]> out = new ArrayList<>();
      List<Integer> idx = new ArrayList<>();

      for (int i = 0; i < n; i++) {
         idx.add(i);
      }

      double area = 0.0;

      for (int i = 0; i < n; i++) {
         float[] a = poly[i];
         float[] b = poly[(i + 1) % n];
         area += a[0] * b[1] - b[0] * a[1];
      }

      boolean ccw = area > 0.0;
      int guard = 0;

      label86:
      while (idx.size() > 3 && guard++ < 1000) {
         boolean cut = false;

         for (int i = 0; i < idx.size(); i++) {
            int ia = idx.get((i + idx.size() - 1) % idx.size());
            int ib = idx.get(i);
            int ic = idx.get((i + 1) % idx.size());
            float[] a = poly[ia];
            float[] b = poly[ib];
            float[] c = poly[ic];
            double cr = (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0]);
            if (ccw ? !(cr <= 1.0E-7) : !(cr >= -1.0E-7)) {
               boolean inside = false;

               for (int j : idx) {
                  if (j != ia && j != ib && j != ic && inTri(poly[j], a, b, c)) {
                     inside = true;
                     break;
                  }
               }

               if (!inside) {
                  out.add(ccw ? new int[]{ia, ib, ic} : new int[]{ic, ib, ia});
                  idx.remove(i);
                  cut = true;
                  if (!cut) {
                     break label86;
                  }
                  continue label86;
               }
            }
         }
         break;
      }

      if (idx.size() == 3) {
         int ia = idx.get(0);
         int ib = idx.get(1);
         int ic = idx.get(2);
         out.add(ccw ? new int[]{ia, ib, ic} : new int[]{ic, ib, ia});
      }

      return out;
   }

   private static boolean inTri(float[] p, float[] a, float[] b, float[] c) {
      double d1 = (p[0] - b[0]) * (a[1] - b[1]) - (a[0] - b[0]) * (p[1] - b[1]);
      double d2 = (p[0] - c[0]) * (b[1] - c[1]) - (b[0] - c[0]) * (p[1] - c[1]);
      double d3 = (p[0] - a[0]) * (c[1] - a[1]) - (c[0] - a[0]) * (p[1] - a[1]);
      boolean neg = d1 < 0.0 || d2 < 0.0 || d3 < 0.0;
      boolean pos = d1 > 0.0 || d2 > 0.0 || d3 > 0.0;
      return !neg || !pos;
   }

   static float[][] roundLoop(float cx, float cz, float r, int perCorner) {
      float[][] out = new float[4 * (perCorner + 1)][];
      int k = 0;
      float[][] cs = new float[][]{{cx, cz}, {-cx, cz}, {-cx, -cz}, {cx, -cz}};

      for (int c = 0; c < 4; c++) {
         for (int i = 0; i <= perCorner; i++) {
            double a = (Math.PI / 2) * c + (Math.PI / 2) * i / perCorner;
            out[k++] = new float[]{cs[c][0] + (float)Math.cos(a) * r, cs[c][1] + (float)Math.sin(a) * r, (float)a};
         }
      }

      return out;
   }

   static void band(G g, String t, float c, float r, float th, float y0, float y1, float u0, float u1) {
      float[][] in = roundLoop(c, c, r, 4);
      float[][] out = roundLoop(c, c, r + th, 4);
      int n = in.length;
      float per = 0.0F;
      float[] acc = new float[n + 1];

      for (int i = 0; i < n; i++) {
         float[] a = out[i];
         float[] b = out[(i + 1) % n];
         per += (float)Math.hypot(b[0] - a[0], b[1] - a[1]);
         acc[i + 1] = per;
      }

      for (int i = 0; i < n; i++) {
         int j = (i + 1) % n;
         float ua = u0 + (u1 - u0) * acc[i] / per;
         float ub = u0 + (u1 - u0) * acc[i + 1] / per;
         float[] uv = new float[]{ua, 0.0F, ub, 0.0F, ub, 1.0F, ua, 1.0F};
         float[] uvIn = new float[]{ua, 0.45F, ub, 0.45F, ub, 0.55F, ua, 0.55F};
         g.quad(t, p(out[i][0], y1, out[i][1]), p(out[j][0], y1, out[j][1]), p(out[j][0], y0, out[j][1]), p(out[i][0], y0, out[i][1]), uv);
         g.quad(t, p(in[j][0], y1, in[j][1]), p(in[i][0], y1, in[i][1]), p(in[i][0], y0, in[i][1]), p(in[j][0], y0, in[j][1]), uvIn);
         g.quad(
            t,
            p(in[i][0], y1, in[i][1]),
            p(in[j][0], y1, in[j][1]),
            p(out[j][0], y1, out[j][1]),
            p(out[i][0], y1, out[i][1]),
            new float[]{ua, 0.0F, ub, 0.0F, ub, 0.08F, ua, 0.08F}
         );
         g.quad(
            t,
            p(out[i][0], y0, out[i][1]),
            p(out[j][0], y0, out[j][1]),
            p(in[j][0], y0, in[j][1]),
            p(in[i][0], y0, in[i][1]),
            new float[]{ua, 0.92F, ub, 0.92F, ub, 1.0F, ua, 1.0F}
         );
      }
   }

   static void ribbon(G g, String t, float cx, float[][] yz, float[] w, float th) {
      int n = yz.length;
      float[][] nrm = new float[n][];

      for (int i = 0; i < n; i++) {
         float[] a = yz[Math.max(0, i - 1)];
         float[] b = yz[Math.min(n - 1, i + 1)];
         float dy = b[0] - a[0];
         float dz = b[1] - a[1];
         float l = (float)Math.max(1.0E-5, Math.hypot(dy, dz));
         nrm[i] = new float[]{-dz / l * th / 2.0F, dy / l * th / 2.0F};
      }

      for (int i = 0; i + 1 < n; i++) {
         float va = (float)i / (n - 1);
         float vb = (float)(i + 1) / (n - 1);
         float[] A = yz[i];
         float[] B = yz[i + 1];
         float[] na = nrm[i];
         float[] nb = nrm[i + 1];
         float xa = w[i] / 2.0F;
         float xb = w[i + 1] / 2.0F;
         float[] uv = new float[]{0.0F, va, 1.0F, va, 1.0F, vb, 0.0F, vb};
         g.quad(
            t,
            p(cx - xa, A[0] + na[0], A[1] + na[1]),
            p(cx + xa, A[0] + na[0], A[1] + na[1]),
            p(cx + xb, B[0] + nb[0], B[1] + nb[1]),
            p(cx - xb, B[0] + nb[0], B[1] + nb[1]),
            uv
         );
         g.quad(
            t,
            p(cx + xa, A[0] - na[0], A[1] - na[1]),
            p(cx - xa, A[0] - na[0], A[1] - na[1]),
            p(cx - xb, B[0] - nb[0], B[1] - nb[1]),
            p(cx + xb, B[0] - nb[0], B[1] - nb[1]),
            uv
         );
         float[] ue = new float[]{0.0F, va, 0.1F, va, 0.1F, vb, 0.0F, vb};
         g.quad(
            t,
            p(cx - xa, A[0] - na[0], A[1] - na[1]),
            p(cx - xa, A[0] + na[0], A[1] + na[1]),
            p(cx - xb, B[0] + nb[0], B[1] + nb[1]),
            p(cx - xb, B[0] - nb[0], B[1] - nb[1]),
            ue
         );
         g.quad(
            t,
            p(cx + xa, A[0] + na[0], A[1] + na[1]),
            p(cx + xa, A[0] - na[0], A[1] - na[1]),
            p(cx + xb, B[0] - nb[0], B[1] - nb[1]),
            p(cx + xb, B[0] + nb[0], B[1] + nb[1]),
            ue
         );
      }

      float[] E = yz[n - 1];
      float[] ne = nrm[n - 1];
      float[] S = yz[0];
      float[] ns = nrm[0];
      float xe = w[n - 1] / 2.0F;
      float xs = w[0] / 2.0F;
      float[] uc = new float[]{0.0F, 0.95F, 1.0F, 0.95F, 1.0F, 1.0F, 0.0F, 1.0F};
      g.quad(
         t,
         p(cx - xe, E[0] + ne[0], E[1] + ne[1]),
         p(cx + xe, E[0] + ne[0], E[1] + ne[1]),
         p(cx + xe, E[0] - ne[0], E[1] - ne[1]),
         p(cx - xe, E[0] - ne[0], E[1] - ne[1]),
         uc
      );
      g.quad(
         t,
         p(cx + xs, S[0] + ns[0], S[1] + ns[1]),
         p(cx - xs, S[0] + ns[0], S[1] + ns[1]),
         p(cx - xs, S[0] - ns[0], S[1] - ns[1]),
         p(cx + xs, S[0] - ns[0], S[1] - ns[1]),
         uc
      );
   }

   static void limb(G g, String t, float[][] pts, float[] radii, int sides) {
      Geo.chain(g, t, pts, radii, sides);

      for (int e = 0; e < 2; e++) {
         int i = e == 0 ? 0 : pts.length - 1;
         if (!(radii[i] < 0.05F)) {
            ball(g, t, pts[i][0], pts[i][1], pts[i][2], radii[i], sides);
         }
      }
   }

   static void alignY(G g, float dx, float dy, float dz) {
      float l = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (!(l < 1.0E-6F)) {
         dx /= l;
         dy /= l;
         dz /= l;
         float th = (float)Math.toDegrees(Math.acos(Math.max(-1.0F, Math.min(1.0F, dy))));
         float ph = (float)Math.toDegrees(Math.atan2(dx, dz));
         g.rotY(ph);
         g.rotX(th);
      }
   }

   static void chainLinks(G g, String t, float[] a, float[] b, float link) {
      float dx = b[0] - a[0];
      float dy = b[1] - a[1];
      float dz = b[2] - a[2];
      float len = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
      int n = Math.max(1, Math.round(len / (link * 1.35F)));

      for (int i = 0; i < n; i++) {
         float f = (i + 0.5F) / n;
         g.push();
         g.translate(a[0] + dx * f, a[1] + dy * f, a[2] + dz * f);
         alignY(g, dx, dy, dz);
         g.rotY(i % 2 == 0 ? 0.0F : 90.0F);
         g.rotZ(90.0F);
         g.scale(1.0F, 1.0F, 1.6F);
         g.torus(t, link * 0.45F, link * 0.14F, 8, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }
   }

   static void chainPath(G g, String t, float[][] pts, float link) {
      for (int i = 0; i + 1 < pts.length; i++) {
         chainLinks(g, t, pts[i], pts[i + 1], link);
      }
   }

   static float[][] sag(float[] a, float[] b, float depth, int n) {
      float[][] out = new float[n + 1][];

      for (int i = 0; i <= n; i++) {
         float f = (float)i / n;
         float s = 4.0F * f * (1.0F - f);
         out[i] = new float[]{a[0] + (b[0] - a[0]) * f, a[1] + (b[1] - a[1]) * f - depth * s, a[2] + (b[2] - a[2]) * f};
      }

      return out;
   }

   static void arc(G g, float[] a, float[] b, float jitter, float r, int seed, float time, int argb) {
      int step = (int)Math.floor(time * 14.0F);
      Random rn = new Random(seed * 7919L + step * 104729L);
      int n = 7;
      float[][] pts = new float[n + 1][];

      for (int i = 0; i <= n; i++) {
         float f = (float)i / n;
         float e = i != 0 && i != n ? jitter : 0.0F;
         pts[i] = new float[]{
            a[0] + (b[0] - a[0]) * f + (rn.nextFloat() - 0.5F) * e,
            a[1] + (b[1] - a[1]) * f + (rn.nextFloat() - 0.5F) * e,
            a[2] + (b[2] - a[2]) * f + (rn.nextFloat() - 0.5F) * e
         };
      }

      boolean gl = g.glow;
      int c = g.color;
      g.glow(true);
      g.color(argb);
      float[] rad = new float[n + 1];
      Arrays.fill(rad, r * 2.6F);
      Geo.chain(g, "c3_beam", pts, rad, 4);
      g.color(-1);
      Arrays.fill(rad, r);
      Geo.chain(g, "white", pts, rad, 4);
      g.glow(gl);
      g.color(c);
   }

   static void halo(G g, float x, float y, float z, float s, int argb) {
      boolean gl = g.glow;
      int c = g.color;
      g.glow(true).color(argb);
      Geo.sprite3(g, "orb", x, y, z, s);
      g.glow(gl).color(c);
   }

   static float[][] skinUV(int U, int V, int w, int h, int d) {
      float s = 0.015625F;
      return new float[][]{
         {(U + d + w) * s, (V + d) * s, (U + d) * s, (V + d + h) * s},
         {(U + 2 * d + 2 * w) * s, (V + d) * s, (U + 2 * d + w) * s, (V + d + h) * s},
         {(U + 2 * d + w) * s, (V + d) * s, (U + d + w) * s, (V + d + h) * s},
         {(U + d) * s, (V + d) * s, U * s, (V + d + h) * s},
         {(U + d + w) * s, V * s, (U + d) * s, (V + d) * s},
         {(U + d + 2 * w) * s, V * s, (U + d + w) * s, (V + d) * s}
      };
   }

   static void skinBox(G g, String skin, float x0, float y0, float z0, float x1, float y1, float z1, int U, int V, int w, int h, int d, float inflate) {
      g.box6(skin, x0 - inflate, y0 - inflate, z0 - inflate, x1 + inflate, y1 + inflate, z1 + inflate, skinUV(U, V, w, h, d));
   }

   static void miniPlayer(G g, String skin, float headYaw, float headPitch, float armR, float armL, float armSideR, float armSideL, float legSwing) {
      int c = g.color;
      g.color(-1);
      g.push();
      g.rotY(headYaw);
      g.rotX(headPitch);
      skinBox(g, skin, -4.0F, 0.0F, -4.0F, 4.0F, 8.0F, 4.0F, 0, 0, 8, 8, 8, 0.0F);
      skinBox(g, skin, -4.0F, 0.0F, -4.0F, 4.0F, 8.0F, 4.0F, 32, 0, 8, 8, 8, 0.5F);
      g.pop();
      skinBox(g, skin, -4.0F, -12.0F, -2.0F, 4.0F, 0.0F, 2.0F, 16, 16, 8, 12, 4, 0.0F);
      skinBox(g, skin, -4.0F, -12.0F, -2.0F, 4.0F, 0.0F, 2.0F, 16, 32, 8, 12, 4, 0.25F);

      for (int s = 1; s >= -1; s -= 2) {
         g.push();
         g.translate(s * 5, -2.0F, 0.0F);
         g.rotZ(s * (s > 0 ? armSideR : armSideL));
         g.rotX(-(s > 0 ? armR : armL));
         if (s > 0) {
            skinBox(g, skin, -1.0F, -10.0F, -2.0F, 3.0F, 2.0F, 2.0F, 40, 16, 4, 12, 4, 0.0F);
            skinBox(g, skin, -1.0F, -10.0F, -2.0F, 3.0F, 2.0F, 2.0F, 40, 32, 4, 12, 4, 0.25F);
         } else {
            skinBox(g, skin, -3.0F, -10.0F, -2.0F, 1.0F, 2.0F, 2.0F, 32, 48, 4, 12, 4, 0.0F);
            skinBox(g, skin, -3.0F, -10.0F, -2.0F, 1.0F, 2.0F, 2.0F, 48, 48, 4, 12, 4, 0.25F);
         }

         g.pop();
      }

      for (int s = 1; s >= -1; s -= 2) {
         g.push();
         g.translate(s * 2, -12.0F, 0.0F);
         g.rotX(s * legSwing);
         if (s > 0) {
            skinBox(g, skin, -2.0F, -12.0F, -2.0F, 2.0F, 0.0F, 2.0F, 0, 16, 4, 12, 4, 0.0F);
            skinBox(g, skin, -2.0F, -12.0F, -2.0F, 2.0F, 0.0F, 2.0F, 0, 32, 4, 12, 4, 0.25F);
         } else {
            skinBox(g, skin, -2.0F, -12.0F, -2.0F, 2.0F, 0.0F, 2.0F, 16, 48, 4, 12, 4, 0.0F);
            skinBox(g, skin, -2.0F, -12.0F, -2.0F, 2.0F, 0.0F, 2.0F, 0, 48, 4, 12, 4, 0.25F);
         }

         g.pop();
      }

      g.color(c);
   }
}
