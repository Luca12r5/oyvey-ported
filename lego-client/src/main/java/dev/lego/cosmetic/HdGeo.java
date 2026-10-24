package dev.lego.cosmetic;

final class HdGeo {
   private HdGeo() {
   }

   static float sin(double v) {
      return (float)Math.sin(v);
   }

   static float cos(double v) {
      return (float)Math.cos(v);
   }

   private static float[] p(float x, float y, float z) {
      return new float[]{x, y, z};
   }

   static float[][] circle(float rx, float ry, int n) {
      float[][] o = new float[n][];

      for (int i = 0; i < n; i++) {
         double a = (Math.PI * 2) * i / n;
         o[i] = new float[]{cos(a) * rx, sin(a) * ry};
      }

      return o;
   }

   static float[][] heart(float s, int n) {
      float[][] o = new float[n][];

      for (int i = 0; i < n; i++) {
         double t = (Math.PI * 2) * i / n;
         double x = 16.0 * Math.pow(Math.sin(t), 3.0);
         double y = 13.0 * Math.cos(t) - 5.0 * Math.cos(2.0 * t) - 2.0 * Math.cos(3.0 * t) - Math.cos(4.0 * t);
         o[i] = new float[]{(float)(-x / 16.0 * s), (float)((y + 2.5) / 16.0 * s)};
      }

      return ccw(o);
   }

   static float[][] star(float r, float ri, int pts) {
      float[][] o = new float[pts * 2][];

      for (int i = 0; i < pts * 2; i++) {
         double a = (Math.PI / 2) + Math.PI * i / pts;
         float rr = i % 2 == 0 ? r : ri;
         o[i] = new float[]{cos(a) * rr, sin(a) * rr};
      }

      return o;
   }

   static float[][] rrect(float hw, float hh, float rc, int per) {
      float[][] o = new float[4 * (per + 1)][];
      float[][] cs = new float[][]{{hw - rc, hh - rc}, {-hw + rc, hh - rc}, {-hw + rc, -hh + rc}, {hw - rc, -hh + rc}};
      int k = 0;

      for (int c = 0; c < 4; c++) {
         for (int i = 0; i <= per; i++) {
            double a = (Math.PI / 2) * c + (Math.PI / 2) * i / per;
            o[k++] = new float[]{cs[c][0] + cos(a) * rc, cs[c][1] + sin(a) * rc};
         }
      }

      return o;
   }

   static float[][] ccw(float[][] o) {
      double area = 0.0;

      for (int i = 0; i < o.length; i++) {
         float[] a = o[i];
         float[] b = o[(i + 1) % o.length];
         area += a[0] * b[1] - b[0] * a[1];
      }

      if (area >= 0.0) {
         return o;
      } else {
         float[][] r = new float[o.length][];

         for (int i = 0; i < o.length; i++) {
            r[i] = o[o.length - 1 - i];
         }

         return r;
      }
   }

   static float[][] offset(float[][] o, float d) {
      int n = o.length;
      float[][] r = new float[n][];

      for (int i = 0; i < n; i++) {
         float[] a = o[(i + n - 1) % n];
         float[] b = o[i];
         float[] c = o[(i + 1) % n];
         float e1x = b[0] - a[0];
         float e1y = b[1] - a[1];
         float e2x = c[0] - b[0];
         float e2y = c[1] - b[1];
         float l1 = (float)Math.max(1.0E-6, Math.hypot(e1x, e1y));
         float l2 = (float)Math.max(1.0E-6, Math.hypot(e2x, e2y));
         float n1x = e1y / l1;
         float n1y = -e1x / l1;
         float n2x = e2y / l2;
         float n2y = -e2x / l2;
         float mx = n1x + n2x;
         float my = n1y + n2y;
         float ml = (float)Math.hypot(mx, my);
         if (ml < 1.0E-4F) {
            mx = n1x;
            my = n1y;
            ml = 1.0F;
         }

         mx /= ml;
         my /= ml;
         float cosh = mx * n1x + my * n1y;
         float s = d / Math.max(0.45F, cosh);
         r[i] = new float[]{b[0] + mx * s, b[1] + my * s};
      }

      return r;
   }

   static void puff(G g, String t, float[][] outline, float depth, float round, int steps, float[] capUV, float[] sideUV) {
      float[][] o = ccw(outline);
      round = Math.min(round, depth / 2.0F * 0.98F);
      int n = o.length;
      float minx = 1.0E9F;
      float miny = 1.0E9F;
      float maxx = -1.0E9F;
      float maxy = -1.0E9F;

      for (float[] q : o) {
         minx = Math.min(minx, q[0]);
         maxx = Math.max(maxx, q[0]);
         miny = Math.min(miny, q[1]);
         maxy = Math.max(maxy, q[1]);
      }

      float bw = Math.max(1.0E-4F, maxx - minx);
      float bh = Math.max(1.0E-4F, maxy - miny);
      int rings = steps * 2 + 2;
      float[][][] R = new float[rings][][];
      float[] Z = new float[rings];
      float hz = depth / 2.0F;
      int k = 0;

      for (int s = 0; s <= steps; s++) {
         double a = (Math.PI / 2) * s / steps;
         R[k] = offset(o, -round * (1.0F - (float)Math.sin(a)));
         Z[k++] = -hz + round * (1.0F - (float)Math.cos(a));
      }

      for (int s = steps; s >= 0; s--) {
         double a = (Math.PI / 2) * s / steps;
         R[k] = offset(o, -round * (1.0F - (float)Math.sin(a)));
         Z[k++] = hz - round * (1.0F - (float)Math.cos(a));
      }

      for (int j = 0; j + 1 < rings; j++) {
         float va = sideUV[1] + (sideUV[3] - sideUV[1]) * j / (rings - 1);
         float vb = sideUV[1] + (sideUV[3] - sideUV[1]) * (j + 1) / (rings - 1);

         for (int i = 0; i < n; i++) {
            int i2 = (i + 1) % n;
            float ua = sideUV[0] + (sideUV[2] - sideUV[0]) * i / n;
            float ub = sideUV[0] + (sideUV[2] - sideUV[0]) * (i + 1) / n;
            float[] A = R[j][i];
            float[] B = R[j][i2];
            float[] C = R[j + 1][i2];
            float[] D = R[j + 1][i];
            g.quad(t, p(A[0], A[1], Z[j]), p(B[0], B[1], Z[j]), p(C[0], C[1], Z[j + 1]), p(D[0], D[1], Z[j + 1]), new float[]{ua, va, ub, va, ub, vb, ua, vb});
         }
      }

      float[][] back = R[0];
      float[][] front = R[rings - 1];

      for (int[] tr : Cos3Geo.triangulate(back)) {
         float[] uvf = new float[8];
         float[] uvb = new float[8];

         for (int q = 0; q < 3; q++) {
            float[] f = front[tr[q]];
            uvf[q * 2] = capUV[0] + (capUV[2] - capUV[0]) * (maxx - f[0]) / bw;
            uvf[q * 2 + 1] = capUV[1] + (capUV[3] - capUV[1]) * (maxy - f[1]) / bh;
            float[] b = back[tr[q]];
            uvb[q * 2] = capUV[0] + (capUV[2] - capUV[0]) * (b[0] - minx) / bw;
            uvb[q * 2 + 1] = capUV[1] + (capUV[3] - capUV[1]) * (maxy - b[1]) / bh;
         }

         float[] a = front[tr[0]];
         float[] b = front[tr[1]];
         float[] c = front[tr[2]];
         g.quad(
            t,
            p(a[0], a[1], hz),
            p(b[0], b[1], hz),
            p(c[0], c[1], hz),
            p(c[0], c[1], hz),
            new float[]{uvf[0], uvf[1], uvf[2], uvf[3], uvf[4], uvf[5], uvf[4], uvf[5]}
         );
         a = back[tr[0]];
         b = back[tr[1]];
         c = back[tr[2]];
         g.quad(
            t,
            p(c[0], c[1], -hz),
            p(b[0], b[1], -hz),
            p(a[0], a[1], -hz),
            p(a[0], a[1], -hz),
            new float[]{uvb[4], uvb[5], uvb[2], uvb[3], uvb[0], uvb[1], uvb[0], uvb[1]}
         );
      }
   }

   static void puff(G g, String t, float[][] outline, float depth, float round, int steps) {
      puff(g, t, outline, depth, round, steps, Cos3Geo.FULL, new float[]{0.45F, 0.45F, 0.55F, 0.55F});
   }

   static void loop(G g, String t, float[][] outline, float rx, float rz, float ex, int sides) {
      float[][] o = ccw(outline);
      int n = o.length;
      float[][][] ring = new float[n][sides + 1][];

      for (int i = 0; i < n; i++) {
         float[] a = o[(i + n - 1) % n];
         float[] c = o[(i + 1) % n];
         float tx = c[0] - a[0];
         float ty = c[1] - a[1];
         float tl = (float)Math.max(1.0E-6, Math.hypot(tx, ty));
         float nx = ty / tl;
         float ny = -tx / tl;

         for (int s = 0; s <= sides; s++) {
            float[] q = Geo.sq((Math.PI * 2) * s / sides, 1.0F, ex);
            ring[i][s] = p(o[i][0] + nx * q[0] * rx, o[i][1] + ny * q[0] * rx, q[1] * rz);
         }
      }

      for (int i = 0; i < n; i++) {
         int i2 = (i + 1) % n;
         float ua = (float)i / n;
         float ub = (float)(i + 1) / n;

         for (int s = 0; s < sides; s++) {
            float va = (float)s / sides;
            float vb = (float)(s + 1) / sides;
            g.quad(t, ring[i][s], ring[i2][s], ring[i2][s + 1], ring[i][s + 1], new float[]{ua, va, ub, va, ub, vb, ua, vb});
         }
      }
   }

   static void horn(G g, String t, float r0, float len, float curl, float twist, int seg, int sides, float ridges) {
      float[][] sp = new float[seg + 1][];
      float[] rr = new float[seg + 1];
      float x = 0.0F;
      float y = 0.0F;
      float z = 0.0F;
      double ang = 0.0;
      double tw = 0.0;
      float step = len / seg;

      for (int i = 0; i <= seg; i++) {
         float f = (float)i / seg;
         sp[i] = new float[]{x, y, z};
         float wob = ridges > 0.0F ? 1.0F + 0.07F * (float)Math.cos(f * Math.PI * 2.0 * ridges) : 1.0F;
         rr[i] = Math.max(0.02F, r0 * (1.0F - f * 0.94F) * wob);
         ang += Math.toRadians(curl) / seg;
         tw += Math.toRadians(twist) / seg;
         x += (float)Math.sin(ang) * step;
         y += (float)Math.cos(ang) * step;
         z += (float)Math.sin(tw) * step * 0.35F;
      }

      XmasGeo.sweep(g, t, sp, rr, rr, null, sides, 1.0F);
      Cos3Geo.ball(g, t, 0.0F, 0.0F, 0.0F, r0 * 0.98F, sides);
   }

   static void glowBall(G g, float x, float y, float z, float s, int argb) {
      Cos3Geo.halo(g, x, y, z, s, argb);
   }
}
