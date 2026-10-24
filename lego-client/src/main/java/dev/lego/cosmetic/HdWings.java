package dev.lego.cosmetic;

import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Path2D.Double;
import java.util.ArrayList;
import java.util.List;

final class HdWings {
   private static float[][] BUP;
   private static float[][] BLO;
   private static float[][] FAIRY;

   private HdWings() {
   }

   private static float[] p(float x, float y, float z) {
      return new float[]{x, y, z};
   }

   static float[][] outline(Path2D path, double flat, int max) {
      List<float[]> pts = new ArrayList<>();
      double[] c = new double[6];

      for (PathIterator it = new FlatteningPathIterator(path.getPathIterator(null), flat); !it.isDone(); it.next()) {
         int k = it.currentSegment(c);
         if (k == 0 || k == 1) {
            float[] q = new float[]{(float)c[0], (float)c[1]};
            if (pts.isEmpty() || Math.hypot(q[0] - pts.get(pts.size() - 1)[0], q[1] - pts.get(pts.size() - 1)[1]) > 0.5) {
               pts.add(q);
            }
         }
      }

      if (pts.size() > 2 && Math.hypot(pts.get(0)[0] - pts.get(pts.size() - 1)[0], pts.get(0)[1] - pts.get(pts.size() - 1)[1]) < 1.0) {
         pts.remove(pts.size() - 1);
      }

      int step = Math.max(1, (int)Math.ceil((double)pts.size() / max));
      List<float[]> out = new ArrayList<>();

      for (int i = 0; i < pts.size(); i += step) {
         out.add(pts.get(i));
      }

      return out.toArray(new float[0][]);
   }

   static void card(G g, String tex, float[][] poly, float tw, float th, float w, float h, float curve, float d, String rimTex, int rimCol) {
      float[][] o = HdGeo.ccw(flipY(poly));
      int n = o.length;
      float[][] P = new float[n][];

      for (int i = 0; i < n; i++) {
         P[i] = wp(o[i], tw, th, w, h, curve);
      }

      List<int[]> tris = Cos3Geo.triangulate(o);
      int oc = g.color;

      for (int[] tr : tris) {
         float[] a = P[tr[0]];
         float[] b = P[tr[1]];
         float[] c = P[tr[2]];
         float[] uv = new float[]{uvx(o[tr[0]], tw), uvy(o[tr[0]], th), uvx(o[tr[1]], tw), uvy(o[tr[1]], th), uvx(o[tr[2]], tw), uvy(o[tr[2]], th)};
         g.quad(tex, off(a, d), off(b, d), off(c, d), off(c, d), new float[]{uv[0], uv[1], uv[2], uv[3], uv[4], uv[5], uv[4], uv[5]});
         g.quad(tex, off(c, -d), off(b, -d), off(a, -d), off(a, -d), new float[]{uv[4], uv[5], uv[2], uv[3], uv[0], uv[1], uv[0], uv[1]});
      }

      g.color(rimCol);

      for (int i = 0; i < n; i++) {
         float[] a = P[i];
         float[] b = P[(i + 1) % n];
         g.quad(rimTex, off(a, -d * 1.6F), off(b, -d * 1.6F), off(b, d * 1.6F), off(a, d * 1.6F), new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});
      }

      g.color(oc);
   }

   private static float[][] flipY(float[][] o) {
      float[][] r = new float[o.length][];

      for (int i = 0; i < o.length; i++) {
         r[i] = new float[]{o[i][0], -o[i][1]};
      }

      return r;
   }

   private static float uvx(float[] q, float tw) {
      return q[0] / tw;
   }

   private static float uvy(float[] q, float th) {
      return -q[1] / th;
   }

   private static float[] wp(float[] q, float tw, float th, float w, float h, float curve) {
      float x = q[0] / tw * w;
      float y = q[1] / th * h;
      return p(x, y, -curve * (x * x + y * y * 0.4F));
   }

   private static float[] off(float[] q, float d) {
      return p(q[0], q[1], q[2] + d);
   }

   private static synchronized void shapes() {
      if (BUP == null) {
         Path2D up = new Double();
         up.moveTo(6.0, 30.0);
         up.curveTo(90.0, -30.0, 250.0, -10.0, 246.0, 80.0);
         up.curveTo(242.0, 140.0, 150.0, 170.0, 8.0, 150.0);
         up.closePath();
         Path2D lo = new Double();
         lo.moveTo(8.0, 150.0);
         lo.curveTo(120.0, 150.0, 210.0, 200.0, 190.0, 270.0);
         lo.curveTo(170.0, 330.0, 60.0, 320.0, 6.0, 230.0);
         lo.closePath();
         Path2D f = new Double();
         f.moveTo(2.0, 64.0);
         f.curveTo(60.0, 0.0, 200.0, -10.0, 250.0, 40.0);
         f.curveTo(262.0, 70.0, 200.0, 112.0, 120.0, 110.0);
         f.curveTo(60.0, 108.0, 20.0, 90.0, 2.0, 64.0);
         BUP = outline(up, 1.5, 30);
         BLO = outline(lo, 1.5, 26);
         FAIRY = outline(f, 1.5, 30);
      }
   }

   static void butterfly(G g, Cos.A a) {
      shapes();
      float t = a.time;
      float mv = a.preview ? 0.0F : a.move;
      float flutter = a.sneak
         ? 72.0F
         : Geo.lerp(18.0F + 22.0F * (0.5F + 0.5F * Cos3Geo.sin(t * 2.6F)), 20.0F + 45.0F * (0.5F + 0.5F * Cos3Geo.sin(t * 12.0F)), mv);

      for (int side = 1; side >= -1; side -= 2) {
         g.push();
         g.scale(side, 1.0F, 1.0F);
         g.translate(0.55F, 2.5F, -2.9F);
         g.rotY(flutter);
         g.rotZ(8.0F);
         g.color(-1);
         card(g, "wing_butterfly2", BUP, 256.0F, 320.0F, 13.5F, 17.0F, 0.014F, 0.07F, "hd_rubber", -16316913);
         g.push();
         g.rotY(6.0F);
         card(g, "wing_butterfly2", BLO, 256.0F, 320.0F, 13.5F, 17.0F, 0.012F, 0.07F, "hd_rubber", -16316913);
         g.pop();
         g.pop();
      }

      g.color(-15068124);
      Geo.ellipsoid(g, "hd_fur", 0.0F, 1.2F, -3.0F, 0.75F, 1.6F, 0.75F, 10, 6);

      for (int i = 0; i < 5; i++) {
         Geo.ellipsoid(g, "hd_fur", 0.0F, -1.2F - i * 1.25F, -3.0F + i * 0.05F, 0.62F - i * 0.07F, 0.75F, 0.62F - i * 0.07F, 8, 5);
      }

      Cos3Geo.ball(g, "hd_fur", 0.0F, 3.3F, -3.1F, 0.62F, 8);

      for (int s = -1; s <= 1; s += 2) {
         float sw = Cos3Geo.sin(t * 2.2F + s) * 0.3F;
         float[][] ant = new float[][]{{s * 0.2F, 3.7F, -3.1F}, {s * 0.9F, 5.2F, -3.3F + sw}, {s * 1.5F, 6.4F, -3.6F + sw}};
         Cos3Geo.limb(g, "hd_plastic", ant, new float[]{0.08F, 0.07F, 0.06F}, 4);
         Cos3Geo.ball(g, "hd_gloss", ant[2][0], ant[2][1], ant[2][2], 0.22F, 6);
      }

      g.color(-1);
   }

   static void fairy(G g, Cos.A a) {
      shapes();
      float t = a.time;
      float mv = a.preview ? 0.0F : a.move;
      float fl = a.sneak ? 70.0F : Geo.lerp(22.0F + 18.0F * (0.5F + 0.5F * Cos3Geo.sin(t * 3.4F)), 20.0F + 40.0F * (0.5F + 0.5F * Cos3Geo.sin(t * 16.0F)), mv);

      for (int side = 1; side >= -1; side -= 2) {
         for (int lobe = 0; lobe < 2; lobe++) {
            g.push();
            g.scale(side, 1.0F, 1.0F);
            g.translate(0.6F, lobe == 0 ? -1.5F : -4.0F, -2.5F);
            g.rotY(fl + (lobe == 0 ? 0 : 8) + Cos3Geo.sin(t * 3.4F + lobe) * 3.0F);
            g.rotZ(lobe == 0 ? 28.0F : -34.0F);
            float w = lobe == 0 ? 17.0F : 12.0F;
            float h = lobe == 0 ? 8.5F : 6.0F;
            g.translate(0.0F, h / 2.0F, 0.0F);
            g.glow(true).color(-419430401);
            card(g, "wing_fairy", FAIRY, 256.0F, 128.0F, w, h, 0.012F, 0.04F, "white", -1510145);
            g.color(-4656897);

            for (int k = 0; k < 3; k++) {
               float[][] vein = new float[6][];

               for (int i = 0; i < 6; i++) {
                  float f = i / 5.0F;
                  float x = f * w * (0.62F + 0.15F * k);
                  float y = -h * 0.5F + h * (0.1F * k + 0.25F) * (float)Math.sin(f * Math.PI * 0.8) + h * (0.1F - 0.2F * k) * f;
                  vein[i] = p(x, y, -0.012F * x * x + 0.06F);
               }

               Geo.chain(g, "white", vein, new float[]{0.08F, 0.07F, 0.06F, 0.05F, 0.04F, 0.03F}, 3);
            }

            g.glow(false);
            g.pop();
         }
      }

      for (int i = 0; i < 10; i++) {
         float ph = Cos3Geo.fract(t * 0.5F + i / 10.0F);
         float s = Cos3Geo.sin(ph * Math.PI);
         float x = Cos3Geo.sin(i * 2.7F) * 9.0F + Cos3Geo.sin(t * 2.0F + i) * 1.2F;
         float z = -4.0F - i % 3 * 1.5F;
         Cos3Models.sparkle(g, x, -2.0F - ph * 16.0F, z, 0.4F * s, Cos3Geo.alpha(s, i % 2 == 0 ? 16777215 : 10154239));
         if (i % 3 == 0) {
            Cos3Geo.halo(g, x, -2.0F - ph * 16.0F, z, 2.2F * s, Cos3Geo.alpha(0.4 * s, 12120319));
         }
      }

      g.color(-1);
   }

   static void cyber(G g, Cos.A a) {
      Wings.Pose p = Wings.pose(a, 0.8F);
      float S = 17.0F;

      for (int side = 1; side >= -1; side -= 2) {
         g.push();
         g.scale(side, 1.0F, 1.0F);
         g.translate(1.0F, -2.0F, -2.45F);
         g.rotY(p.sweep);
         g.rotZ(p.raise);
         g.rotX(-8.0F);
         float[] O = new float[]{0.0F, 0.0F, 0.0F};
         float[] E = new float[]{S * 0.4F, S * 0.22F, -0.3F};
         float[] W = new float[]{S * 0.82F * (1.0F - 0.15F * p.fold), S * 0.26F, -0.8F};
         g.color(-12959668);
         Cos3Geo.limb(g, "hd_steel", new float[][]{O, E}, new float[]{0.95F, 0.8F}, 8);
         Cos3Geo.limb(g, "hd_steel", new float[][]{E, W}, new float[]{0.8F, 0.55F}, 8);
         g.color(-2564376);

         for (float[] j : new float[][]{O, E, W}) {
            Cos3Geo.ball(g, "hd_chrome", j[0], j[1], j[2], 1.05F, 10);
         }

         g.glow(true).color(-13965569);
         Cos3Geo.limb(
            g, "white", new float[][]{{O[0], O[1] + 0.85F, O[2]}, {E[0], E[1] + 0.75F, E[2]}, {W[0], W[1] + 0.55F, W[2]}}, new float[]{0.12F, 0.12F, 0.1F}, 4
         );
         Cos3Geo.halo(g, E[0], E[1], E[2] - 0.9F, 2.8F + 0.6F * Cos3Geo.sin(p.t * 4.0F), Cos3Geo.alpha(0.6, 2811647));
         Cos3Geo.halo(g, W[0], W[1], W[2] - 0.9F, 2.4F + 0.6F * Cos3Geo.sin(p.t * 4.0F + 1.0F), Cos3Geo.alpha(0.6, 2811647));
         g.glow(false);
         int n = 8;

         for (int i = 0; i < n; i++) {
            float f = (float)i / (n - 1);
            float[] at = new float[]{Geo.lerp(1.5F, W[0], f), Geo.lerp(0.5F, W[1], f) - 0.3F, Geo.lerp(0.0F, W[2], f) - 0.2F + i * 0.06F};
            float th = Geo.lerp(-4.0F, 62.0F, f) * (1.0F - 0.6F * p.fold) + Cos3Geo.sin(p.t * 2.0F + i) * 1.5F;
            float L = S * (0.5F + 0.42F * Cos3Geo.sin(Math.PI * (0.25 + 0.6 * f)));
            float w = S * 0.07F;
            g.push();
            g.translate(at[0], at[1], at[2]);
            g.rotZ(th);
            g.rotY(-8.0F);
            float[][] bl = new float[][]{{-w, 0.2F}, {w, 0.2F}, {w * 0.95F, -L * 0.78F}, {0.0F, -L}, {-w * 0.95F, -L * 0.78F}};
            g.color(-3617064);
            HdGeo.puff(g, "hd_steel", bl, 0.36F, 0.14F, 1);
            g.glow(true).color(Cos3Geo.alpha(0.8 + 0.2 * Cos3Geo.sin(p.t * 5.0F + i), 2811647));
            g.push();
            g.translate(0.0F, 0.0F, -0.2F);
            Cos3Geo.cbox(g, "white", 0.0F, -L * 0.42F, 0.0F, 0.14F, L * 0.72F, 0.06F, 0.02F);
            g.pop();
            g.glow(false);
            g.pop();
         }

         g.pop();
      }

      g.color(-12959668);
      Cos3Geo.bbox(g, "hd_steel", -2.2F, -5.5F, -3.3F, 2.2F, -0.8F, -2.05F, 0.3F);
      g.color(-2564376);
      g.push();
      g.translate(0.0F, -3.1F, -3.3F);
      g.rotX(-90.0F);
      Cos3Geo.lathe(g, "hd_chrome", new float[]{1.2F, 1.2F, 0.9F, 0.0F}, new float[]{0.0F, 0.35F, 0.45F, 0.46F}, 14);
      g.pop();
      g.glow(true).color(-13965569);
      g.push();
      g.translate(0.0F, -3.1F, -3.78F);
      g.rotX(90.0F);
      g.torus("white", 0.62F, 0.13F, 14, 3, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.glow(false);
      Cos3Geo.halo(g, 0.0F, -3.1F, -4.2F, 3.2F, Cos3Geo.alpha(0.5, 2811647));
      g.color(-1);
   }
}
