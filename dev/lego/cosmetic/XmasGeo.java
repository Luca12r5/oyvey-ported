package dev.lego.cosmetic;

import java.util.Arrays;

final class XmasGeo {
   static final int[] LIGHTS = new int[]{-50630, -11718, -12916630, -12932865, -34080, -26070};

   private XmasGeo() {
   }

   private static float[] p(float x, float y, float z) {
      return new float[]{x, y, z};
   }

   static void sweep(G g, String tex, float[][] spine, float[] rx, float[] rz, float[] n, int seg, float vRep) {
      int m = spine.length;
      float[][][] rings = new float[m][seg + 1][];
      float[] ref = new float[]{1.0F, 0.0F, 0.0F};

      for (int j = 0; j < m; j++) {
         float[] a = spine[Math.max(0, j - 1)];
         float[] b = spine[Math.min(m - 1, j + 1)];
         float tx = b[0] - a[0];
         float ty = b[1] - a[1];
         float tz = b[2] - a[2];
         float tl = (float)Math.sqrt(tx * tx + ty * ty + tz * tz);
         if (tl < 1.0E-6F) {
            tx = 0.0F;
            ty = 1.0F;
            tz = 0.0F;
            tl = 1.0F;
         }

         tx /= tl;
         ty /= tl;
         tz /= tl;
         float d = ref[0] * tx + ref[1] * ty + ref[2] * tz;
         float nx = ref[0] - d * tx;
         float ny = ref[1] - d * ty;
         float nz = ref[2] - d * tz;
         float nl = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
         if (nl < 1.0E-4F) {
            nx = 0.0F;
            ny = 0.0F;
            nz = 1.0F;
            nl = 1.0F;
         }

         nx /= nl;
         ny /= nl;
         nz /= nl;
         ref = new float[]{nx, ny, nz};
         float bx = ty * nz - tz * ny;
         float by = tz * nx - tx * nz;
         float bz = tx * ny - ty * nx;

         for (int i = 0; i <= seg; i++) {
            float[] q = Geo.sq((Math.PI * 2) * i / seg, 1.0F, n == null ? 2.0F : n[j]);
            float cx = q[0] * rx[j];
            float cz = q[1] * rz[j];
            rings[j][i] = p(spine[j][0] + nx * cx + bx * cz, spine[j][1] + ny * cx + by * cz, spine[j][2] + nz * cx + bz * cz);
         }
      }

      for (int j = 0; j + 1 < m; j++) {
         float va = vRep * j / (m - 1);
         float vb = vRep * (j + 1) / (m - 1);

         for (int i = 0; i < seg; i++) {
            float ua = (float)i / seg;
            float ub = (float)(i + 1) / seg;
            g.quad(tex, rings[j + 1][i], rings[j + 1][i + 1], rings[j][i + 1], rings[j][i], new float[]{ua, vb, ub, vb, ub, va, ua, va});
         }
      }
   }

   static void bulb(G g, float s, int argb, float lit) {
      boolean og = g.glow;
      int oc = g.color;
      g.glow(false).color(-13739468);
      g.cylinder("c3_metal", 0.26F * s, 0.3F * s, 0.0F, 0.42F * s, 6, false, 0.0F, 0.0F, 1.0F, 1.0F);
      int c = lit > 0.5F ? argb : 0xFF000000 | Capes.mix(argb & 16777215, 2105376, 0.55 - lit * 0.5);
      g.glow(lit > 0.2F).color(0xFF000000 | Capes.mix(c & 16777215, 16777215, 0.15 * lit));
      Geo.ellipsoid(g, "xm_bulb", 0.0F, 0.95F * s, 0.0F, 0.38F * s, 0.62F * s, 0.38F * s, 8, 5);
      if (lit > 0.2F) {
         Cos3Geo.halo(g, 0.0F, 0.95F * s, 0.0F, 2.3F * s, Cos3Geo.alpha(0.45 * lit, argb));
      }

      g.glow(og).color(oc);
   }

   static void bulbLite(G g, float s, int argb, float lit) {
      boolean og = g.glow;
      int oc = g.color;
      g.glow(false).color(-13739468);
      g.cylinder("c3_metal", 0.26F * s, 0.3F * s, 0.0F, 0.42F * s, 6, false, 0.0F, 0.0F, 1.0F, 1.0F);
      int c = 0xFF000000 | Capes.mix(2631720, argb & 16777215, 0.3 + 0.7 * lit);
      g.glow(lit > 0.3F).color(c);
      Geo.ellipsoid(g, "xm_bulb", 0.0F, 0.95F * s, 0.0F, 0.38F * s, 0.62F * s, 0.38F * s, 6, 4);
      if (lit > 0.3F) {
         Cos3Geo.halo(g, 0.0F, 0.95F * s, 0.0F, 2.4F * s, Cos3Geo.alpha(0.5 * lit, argb));
      }

      g.glow(og).color(oc);
   }

   static void fairy(G g, float x, float y, float z, float r, int argb, float lit) {
      boolean og = g.glow;
      int oc = g.color;
      int c = 0xFF000000 | Capes.mix(4210752, argb & 16777215, 0.55 + 0.45 * lit);
      g.glow(true).color(c);
      Cos3Geo.ball(g, "xm_bulb", x, y, z, r, 6);
      if (lit > 0.3F) {
         Cos3Geo.halo(g, x, y, z, r * 5.5F, Cos3Geo.alpha(0.5 * lit, argb));
      }

      g.glow(og).color(oc);
   }

   static float twinkle(float t, int i) {
      double h = Capes.hash1(i, 91);
      float v = 0.5F + 0.5F * Cos3Geo.sin(t * (2.2 + h * 2.5) + h * 30.0);
      return 0.25F + 0.75F * Cos3Geo.smooth(v * 1.4F - 0.2F);
   }

   static void bell(G g, float s, float swing) {
      int oc = g.color;
      g.push();
      g.rotZ(swing);
      g.color(-1525460);
      g.push();
      g.translate(0.0F, -0.1F * s, 0.0F);
      g.rotX(90.0F);
      g.torus("xm_gold", 0.18F * s, 0.06F * s, 8, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      Cos3Geo.lathe(
         g,
         "xm_gold",
         new float[]{0.0F, 0.28F * s, 0.42F * s, 0.5F * s, 0.62F * s, 0.8F * s, 0.84F * s, 0.7F * s, 0.0F},
         new float[]{-0.2F * s, -0.24F * s, -0.4F * s, -0.7F * s, -1.05F * s, -1.3F * s, -1.4F * s, -1.44F * s, -1.44F * s},
         10
      );
      g.color(-7706086);
      Cos3Geo.ball(g, "c3_metal", 0.0F, -1.48F * s, 0.0F, 0.2F * s, 6);
      g.pop();
      g.color(oc);
   }

   static void hollyLeaf(G g, float len) {
      float w = len * 0.36F;
      float[][] top = new float[][]{
         {0.0F, 0.0F},
         {len * 0.18F, w * 0.75F},
         {len * 0.3F, w * 0.55F},
         {len * 0.45F, w},
         {len * 0.58F, w * 0.62F},
         {len * 0.74F, w * 0.9F},
         {len * 0.84F, w * 0.4F},
         {len, 0.0F}
      };
      int oc = g.color;
      g.color(-1);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.rotX(s * 14);
         float[][] poly = new float[top.length][];

         for (int i = 0; i < top.length; i++) {
            poly[s > 0 ? i : top.length - 1 - i] = new float[]{top[i][0], s * top[i][1]};
         }

         Cos3Geo.extrude(
            g, "xm_holly", poly, -0.05F, 0.05F, new float[]{0.0F, s > 0 ? 0.0F : 0.5F, 1.0F, s > 0 ? 0.5F : 1.0F}, new float[]{0.1F, 0.4F, 0.2F, 0.6F}
         );
         g.pop();
      }

      g.color(oc);
   }

   static void holly(G g, float s) {
      float[] angs = new float[]{20.0F, 150.0F, 270.0F};

      for (int i = 0; i < 3; i++) {
         g.push();
         g.rotY(angs[i]);
         g.rotZ(12.0F);
         hollyLeaf(g, 2.4F * s);
         g.pop();
      }

      int oc = g.color;
      g.color(-2090966);
      Cos3Geo.ball(g, "xm_gloss", 0.25F * s, 0.32F * s, 0.12F * s, 0.34F * s, 8);
      Cos3Geo.ball(g, "xm_gloss", -0.28F * s, 0.3F * s, 0.2F * s, 0.32F * s, 8);
      Cos3Geo.ball(g, "xm_gloss", 0.0F, 0.36F * s, -0.3F * s, 0.33F * s, 8);
      g.color(oc);
   }

   static void bow(G g, int argb, float s) {
      int oc = g.color;
      g.color(argb);

      for (int k = -1; k <= 1; k += 2) {
         g.push();
         g.translate(k * 0.75F * s, 0.12F * s, 0.0F);
         g.rotZ(k * 18);
         g.scale(1.0F, 0.62F, 0.5F);
         g.rotX(90.0F);
         g.torus("xm_ribbon", 0.62F * s, 0.2F * s, 10, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
         float[][] yz = new float[][]{{-0.1F * s, 0.05F * s}, {-0.6F * s, 0.12F * s}, {-1.2F * s, 0.05F * s}};
         g.push();
         g.rotZ(k * 24);
         Cos3Geo.ribbon(g, "xm_ribbon", 0.0F, yz, new float[]{0.42F * s, 0.46F * s, 0.5F * s}, 0.08F * s);
         g.pop();
      }

      Cos3Geo.ball(g, "xm_ribbon", 0.0F, 0.05F * s, 0.08F * s, 0.3F * s, 8);
      g.color(oc);
   }

   static void star(G g, String tex, float r, float d, int pts) {
      int n = pts * 2;
      float[][] o = new float[n][];

      for (int i = 0; i < n; i++) {
         double a = (Math.PI / 2) + Math.PI * i / pts;
         float rr = i % 2 == 0 ? r : r * 0.45F;
         o[i] = p((float)Math.cos(a) * rr, (float)Math.sin(a) * rr, 0.0F);
      }

      float[] f = p(0.0F, 0.0F, d);
      float[] b = p(0.0F, 0.0F, -d);

      for (int i = 0; i < n; i++) {
         float[] A = o[i];
         float[] B = o[(i + 1) % n];
         g.quad(tex, f, B, A, A, new float[]{0.5F, 0.5F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F});
         g.quad(tex, b, A, B, B, new float[]{0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F});
      }
   }

   static void flake(G g, String tex, float r, float th) {
      float w = r * 0.09F;

      for (int i = 0; i < 3; i++) {
         g.push();
         g.rotZ(i * 60);
         g.box(tex, -r, -w, -th, r, w, th, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      for (int i = 0; i < 6; i++) {
         g.push();
         g.rotZ(i * 60);
         g.translate(0.0F, r * 0.62F, 0.0F);
         g.box(tex, -r * 0.24F, -w * 0.8F, -th * 0.8F, r * 0.24F, w * 0.8F, th * 0.8F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }
   }

   static void wire(G g, float[][] pts, float r) {
      int oc = g.color;
      g.color(-14796254);
      float[] rr = new float[pts.length];
      Arrays.fill(rr, r);
      Geo.chain(g, "c3_metal", pts, rr, 3);
      g.color(oc);
   }
}
