package dev.lego.cosmetic;

import java.util.Arrays;
import java.util.Random;

final class HdHats {
   private HdHats() {
   }

   private static float[] p(float x, float y, float z) {
      return new float[]{x, y, z};
   }

   static void brim(G g, String t, float rIn, float nIn, float rOut, float zs, float th, int seg, int rings, HdHats.Lift lift) {
      float[][][] top = new float[seg + 1][rings + 1][];
      float[][][] bot = new float[seg + 1][rings + 1][];

      for (int i = 0; i <= seg; i++) {
         double a = (Math.PI * 2) * i / seg;
         float[] in = Geo.sq(a, rIn, nIn);
         float ox = Cos3Geo.cos(a) * rOut;
         float oz = Cos3Geo.sin(a) * rOut * zs;

         for (int j = 0; j <= rings; j++) {
            float f = (float)j / rings;
            float x = in[0] + (ox - in[0]) * f;
            float z = in[1] + (oz - in[1]) * f;
            float y = lift.at(a, f);
            float thj = th * (1.0F - 0.35F * f);
            top[i][j] = p(x, y + thj / 2.0F, z);
            bot[i][j] = p(x, y - thj / 2.0F, z);
         }
      }

      for (int i = 0; i < seg; i++) {
         float ua = (float)i / seg;
         float ub = (float)(i + 1) / seg;

         for (int j = 0; j < rings; j++) {
            float va = (float)j / rings;
            float vb = (float)(j + 1) / rings;
            g.quad(t, top[i][j + 1], top[i + 1][j + 1], top[i + 1][j], top[i][j], new float[]{ua, vb, ub, vb, ub, va, ua, va});
            g.quad(t, bot[i][j], bot[i + 1][j], bot[i + 1][j + 1], bot[i][j + 1], new float[]{ua, va, ub, va, ub, vb, ua, vb});
         }

         float[] T0 = top[i][rings];
         float[] T1 = top[i + 1][rings];
         float[] B0 = bot[i][rings];
         float[] B1 = bot[i + 1][rings];
         double a0 = (Math.PI * 2) * i / seg;
         double a1 = (Math.PI * 2) * (i + 1) / seg;
         float e = th * 0.42F;
         float[] M0 = p((T0[0] + B0[0]) / 2.0F + Cos3Geo.cos(a0) * e, (T0[1] + B0[1]) / 2.0F, (T0[2] + B0[2]) / 2.0F + Cos3Geo.sin(a0) * e * zs);
         float[] M1 = p((T1[0] + B1[0]) / 2.0F + Cos3Geo.cos(a1) * e, (T1[1] + B1[1]) / 2.0F, (T1[2] + B1[2]) / 2.0F + Cos3Geo.sin(a1) * e * zs);
         g.quad(t, T0, T1, M1, M0, new float[]{ua, 0.96F, ub, 0.96F, ub, 1.0F, ua, 1.0F});
         g.quad(t, M0, M1, B1, B0, new float[]{ua, 0.96F, ub, 0.96F, ub, 1.0F, ua, 1.0F});
         g.quad(t, bot[i][0], bot[i + 1][0], top[i + 1][0], top[i][0], new float[]{ua, 0.0F, ub, 0.0F, ub, 0.04F, ua, 0.04F});
      }
   }

   static float[][] bentSpine(float yBase, float h, int n, double bendDeg, double sideDeg, float bendStart) {
      float[][] sp = new float[n + 1][];
      float x = 0.0F;
      float y = yBase;
      float z = 0.0F;
      double step = h / n;

      for (int i = 0; i <= n; i++) {
         float f = (float)i / n;
         sp[i] = p(x, y, z);
         double b = f < bendStart ? 0.0 : Math.toRadians(bendDeg) * Math.pow((f - bendStart) / (1.0F - bendStart), 1.6);
         double sd = Math.toRadians(sideDeg);
         x += (float)(Math.sin(b) * Math.sin(sd) * step);
         z += (float)(-Math.sin(b) * Math.cos(sd) * step);
         y += (float)(Math.cos(b) * step);
      }

      return sp;
   }

   static void ear(G g, float[][] outer, float[][] inner, String tex, int outerCol, int innerCol, float depth) {
      int oc = g.color;
      g.color(outerCol);
      HdGeo.puff(g, tex, outer, depth, depth * 0.45F, 2);
      g.color(innerCol);
      g.push();
      g.translate(0.0F, 0.0F, depth * 0.38F);
      HdGeo.puff(g, "hd_velvet", inner, depth * 0.4F, depth * 0.18F, 1);
      g.pop();
      g.color(oc);
   }

   static float[][] earShape(float w, float h, float bulge, int n) {
      float[][] o = new float[n * 2 + 2][];
      int k = 0;
      o[k++] = p2(w / 2.0F, 0.0F);

      for (int i = 1; i <= n; i++) {
         float f = (float)i / (n + 1);
         float x = w / 2.0F * (1.0F - f);
         float y = h * f;
         float b = bulge * (float)Math.sin(Math.PI * f);
         o[k++] = p2(x + b * 0.9F, y + b * 0.25F);
      }

      o[k++] = p2(0.0F, h);

      for (int i = n; i >= 1; i--) {
         float f = (float)i / (n + 1);
         float x = -w / 2.0F * (1.0F - f);
         float y = h * f;
         float b = bulge * (float)Math.sin(Math.PI * f);
         o[k++] = p2(x - b * 0.9F, y + b * 0.25F);
      }

      return Arrays.copyOf(o, k);
   }

   private static float[] p2(float x, float y) {
      return new float[]{x, y};
   }

   static void crown(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(0.0F, 0.0F, 0.0F);
      float c = 2.6F;
      float r = 2.12F;
      float th = 0.42F;
      float y0 = 7.1F;
      float y1 = 9.2F;
      g.color(-5237730);
      Geo.sqDome(g, "hd_velvet", 4.55F, 2.7F, 8.4F, 5.0F, 16, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1);
      Cos3Geo.band(g, "hd_gold", c, r, th, y0, y1, 0.0F, 4.0F);
      g.color(-2840);

      for (int i = 0; i < 22; i++) {
         float[] q = Cos3Models.loopPt(c, r + th + 0.12F, (Math.PI * 2) * i / 22.0);
         Cos3Geo.ball(g, "hd_gloss", q[0], y0 + 0.25F, q[1], 0.24F, 4);
      }

      g.color(-5720);
      int n = 36;
      float[][] rim = new float[n + 1][];

      for (int i = 0; i <= n; i++) {
         float[] q = Cos3Models.loopPt(c, r + th * 0.5F, (Math.PI * 2) * i / n);
         rim[i] = p(q[0], y1, q[1]);
      }

      float[] rr = new float[n + 1];
      Arrays.fill(rr, th * 0.62F);
      XmasGeo.sweep(g, "hd_gold", rim, rr, rr, null, 4, 4.0F);

      for (int i = 0; i < 8; i++) {
         double an = (Math.PI * 2) * i / 8.0 + (Math.PI / 8);
         float[] q = Cos3Models.loopPt(c, r + th * 0.5F, an);
         boolean big = i % 2 == 0;
         float h = big ? 3.1F : 2.2F;
         g.push();
         g.translate(q[0], y1 - 0.25F, q[1]);
         g.rotY((float)(-Math.toDegrees(an)) + 90.0F);
         g.rotX(-6.0F);
         float[][] spike = new float[][]{
            {-1.35F, 0.0F}, {1.35F, 0.0F}, {0.9F, h * 0.3F}, {0.42F, h * 0.62F}, {0.12F, h}, {-0.12F, h}, {-0.42F, h * 0.62F}, {-0.9F, h * 0.3F}
         };
         HdGeo.puff(g, "hd_gold", spike, 0.42F, 0.16F, 1);
         g.color(-2322);
         Cos3Geo.ball(g, "hd_gloss", 0.0F, h + 0.35F, 0.0F, big ? 0.45F : 0.36F, 6);
         g.glow(true).color(big ? -1564614 : -13993217);
         g.push();
         g.translate(0.0F, h * 0.34F, 0.18F);
         g.rotX(90.0F);
         Cos3Geo.gem(g, "hd_gem", 0.36F, 0.26F, 0.1F, 6);
         g.pop();
         g.glow(false).color(-1);
         g.pop();
      }

      int[] gc = new int[]{-1564614, -14763942, -13993217, -5223681};

      for (int i = 0; i < 8; i++) {
         double an = (Math.PI * 2) * i / 8.0;
         float[] q = Cos3Models.loopPt(c, r + th + 0.02F, an);
         g.push();
         g.translate(q[0], (y0 + y1) / 2.0F + 0.05F, q[1]);
         g.rotY((float)(-Math.toDegrees(an)) + 90.0F);
         g.color(-5720);
         g.push();
         g.rotX(90.0F);
         g.torus("hd_gold", 0.55F, 0.13F, 8, 3, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
         g.glow(true).color(gc[i % 4]);
         g.rotX(90.0F);
         Cos3Geo.gem(g, "hd_gem", i % 2 == 0 ? 0.52F : 0.4F, 0.4F, 0.12F, 6);
         g.glow(false);
         g.pop();
      }

      g.color(-1);
      Cos3Geo.ball(g, "hd_gold", 0.0F, 11.35F, 0.0F, 0.62F, 10);
      g.color(-5720);
      Cos3Geo.cbox(g, "hd_gold", 0.0F, 12.55F, 0.0F, 0.36F, 1.7F, 0.36F, 0.08F);
      Cos3Geo.cbox(g, "hd_gold", 0.0F, 12.75F, 0.0F, 1.2F, 0.36F, 0.36F, 0.08F);
      g.color(-1);

      for (int i = 0; i < 3; i++) {
         float tw = Math.max(0.0F, Cos3Geo.sin(t * 2.3F + i * 2.1F));
         double an = i * 2.1 + 0.4;
         float[] q = Cos3Models.loopPt(c, r + th + 0.4F, an);
         if (tw > 0.05F) {
            Cos3Models.sparkle(g, q[0], 10.2F + i * 0.4F, q[1], 0.45F * tw, Cos3Geo.alpha(tw, 16777215));
         }
      }

      g.pop();
   }

   static void halo(G g, Cos.A a) {
      float t = a.time;
      float bob = Cos3Geo.sin(t * 2.2F) * 0.4F;
      g.push();
      g.translate(0.0F, 11.6F + bob, 0.0F);
      g.rotX(16.0F + Cos3Geo.sin(t * 1.1F) * 3.0F);
      g.push();
      g.rotY(t * 30.0F);
      g.glow(true).color(-5728);
      g.torus("hd_gold", 4.55F, 0.46F, 40, 10, 0.0F, 0.0F, 4.0F, 1.0F);
      g.color(-1);
      g.torus("white", 4.55F, 0.16F, 40, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      g.glow(false);
      g.pop();
      float pulse = 0.7F + 0.3F * Cos3Geo.sin(t * 3.0F);
      g.glow(true).color(Cos3Geo.alpha(0.35 * pulse, 16769146));
      g.torus("hd_softglow", 4.55F, 1.35F, 32, 6, 0.1F, 0.1F, 0.9F, 0.9F);
      g.glow(false);

      for (int i = 0; i < 6; i++) {
         double an = (Math.PI * 2) * i / 6.0 + t * 1.2;
         float tw = 0.5F + 0.5F * Cos3Geo.sin(t * 4.0F + i * 1.9F);
         Cos3Models.sparkle(g, Cos3Geo.cos(an) * 4.55F, 0.1F, Cos3Geo.sin(an) * 4.55F, 0.35F + tw * 0.35F, -1);
         Cos3Geo.halo(g, Cos3Geo.cos(an) * 4.55F, 0.1F, Cos3Geo.sin(an) * 4.55F, 1.6F + tw, Cos3Geo.alpha(0.4 * tw, 16773296));
      }

      g.pop();
   }

   static void tophat(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 8.05F, 0.0F);
      g.rotZ(-4.0F);
      g.color(-14935006);
      brim(g, "hd_felt", 4.3F, 2.0F, 7.0F, 0.92F, 0.36F, 32, 3, (an, f) -> 0.18F + f * f * 0.9F * (float)Math.pow(Math.abs(Math.cos(an)), 2.0));
      Cos3Geo.lathe(
         g,
         "hd_felt",
         new float[]{4.35F, 4.28F, 4.18F, 4.24F, 4.42F, 4.42F, 4.2F, 3.4F, 0.0F},
         new float[]{0.2F, 2.0F, 4.2F, 6.4F, 8.2F, 8.45F, 8.62F, 8.7F, 8.7F},
         28
      );
      g.color(-4713444);
      Cos3Geo.lathe(g, "hd_satin", new float[]{4.37F, 4.36F, 4.32F, 4.28F}, new float[]{0.3F, 0.9F, 1.6F, 2.1F}, 28, 0.0F, 0.0F, 1.0F, 1.0F);
      g.push();
      g.translate(4.35F, 1.2F, 0.8F);
      g.rotY(90.0F);
      XmasGeo.bow(g, -4713444, 0.65F);
      g.pop();
      g.pop();
   }

   static void santa(G g, Cos.A a) {
      float t = a.time;
      float mv = a.preview ? 0.0F : Cos3Geo.clamp(a.move, 0.0F, 1.0F);
      float sway = Cos3Geo.sin(t * 1.6F) * 0.2F + mv * 0.25F;
      float[] ys = new float[]{5.4F, 7.0F, 8.6F, 9.7F};
      float[] r0 = new float[]{4.72F, 4.8F, 4.55F, 3.7F};
      float[] n0 = new float[]{5.5F, 5.5F, 4.2F, 2.8F};
      int floppy = 10;
      int m = ys.length + floppy;
      float[][] sp = new float[m][];
      float[] rx = new float[m];
      float[] nn = new float[m];

      for (int i = 0; i < ys.length; i++) {
         sp[i] = p(0.0F, ys[i], 0.0F);
         rx[i] = r0[i];
         nn[i] = n0[i];
      }

      float[] cur = (float[])sp[ys.length - 1].clone();

      for (int i = 0; i < floppy; i++) {
         float f = (float)(i + 1) / floppy;
         double ang = f * f * 2.5 + 0.25;
         double side = 1.35 + sway * f;
         float seg = 1.2F - 0.35F * f;
         cur = new float[]{
            cur[0] + (float)(Math.sin(side) * Math.sin(ang)) * seg, cur[1] + (float)Math.cos(ang) * seg, cur[2] - (float)(Math.cos(side) * Math.sin(ang)) * seg
         };
         int j = ys.length + i;
         sp[j] = cur;
         rx[j] = Math.max(0.1F, 3.4F * (1.0F - f) + 0.1F);
         nn[j] = 2.0F;
      }

      g.color(-3664866);
      XmasGeo.sweep(g, "hd_velvet", sp, rx, rx, nn, 22, 3.0F);
      g.color(-460550);
      int n = 40;
      float[][] roll = new float[n + 1][];
      float[] rr = new float[n + 1];
      float[] rz = new float[n + 1];

      for (int i = 0; i <= n; i++) {
         double an = (Math.PI * 2) * i / n;
         float[] q = Cos3Models.loopPt(2.6F, 2.45F, an);
         roll[i] = p(q[0], 5.7F + 0.08F * Cos3Geo.sin(an * 7.0), q[1]);
         rr[i] = 0.95F + 0.07F * Cos3Geo.sin(an * 11.0);
         rz[i] = 1.25F + 0.07F * Cos3Geo.cos(an * 9.0);
      }

      XmasGeo.sweep(g, "hd_fluff", roll, rr, rz, null, 10, 8.0F);
      float[] e = sp[m - 1];
      g.push();
      g.translate(e[0], e[1] - 0.3F, e[2]);
      g.rotZ(Cos3Geo.sin(t * 3.0F) * 10.0F);
      Cos3Geo.ball(g, "hd_fluff", 0.0F, -0.9F, 0.0F, 1.45F, 12);

      for (int i = 0; i < 8; i++) {
         double an = i * 2.4;
         Cos3Geo.ball(g, "hd_fluff", Cos3Geo.cos(an) * 1.0F, -0.9F + Cos3Geo.sin(i * 1.3) * 0.9F, Cos3Geo.sin(an) * 1.0F, 0.7F, 6);
      }

      g.pop();
      g.color(-1);
   }

   static void witch(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(0.0F, 7.35F, 0.0F);
      g.rotZ(-5.0F);
      g.color(-12969382);
      brim(
         g,
         "hd_felt",
         4.75F,
         5.0F,
         8.6F,
         1.0F,
         0.34F,
         40,
         4,
         (an, fx) -> 0.2F + fx * 0.25F + fx * fx * 0.55F * Cos3Geo.sin(an * 3.0 + 1.0) - fx * fx * 0.4F * Math.max(0.0F, Cos3Geo.sin(an))
      );
      float[][] sp = bentSpine(0.3F, 10.5F, 14, 70.0, 200.0, 0.45F);
      int m = sp.length;
      float[] rx = new float[m];
      float[] nn = new float[m];

      for (int i = 0; i < m; i++) {
         float f = (float)i / (m - 1);
         rx[i] = Math.max(0.06F, 4.72F * (float)Math.pow(1.0F - f, 1.25) + 0.05F);
         nn[i] = f < 0.1F ? 5.0F : 2.0F;
         if (i > 0 && i < m - 1) {
            sp[i][0] = sp[i][0] + Cos3Geo.sin(f * 9.0F) * 0.12F;
         }
      }

      XmasGeo.sweep(g, "hd_felt", sp, rx, rx, nn, 22, 3.0F);
      g.color(-1);
      Geo.sqTube(g, "hd_witchband", 4.82F, 4.62F, 0.35F, 1.95F, 5.0F, 24, 0.0F, 0.0F, 3.0F, 1.0F);
      g.push();
      g.translate(0.0F, 1.15F, 4.86F);
      g.color(-10134);
      HdGeo.loop(g, "hd_gold", HdGeo.rrect(1.15F, 0.9F, 0.3F, 3), 0.22F, 0.2F, 3.0F, 6);
      Cos3Geo.cbox(g, "hd_gold", 0.0F, 0.0F, 0.05F, 0.25F, 1.5F, 0.25F, 0.05F);
      g.pop();
      float[] e = sp[m - 1];
      g.push();
      g.translate(e[0], e[1], e[2]);
      g.rotZ(Cos3Geo.sin(t * 2.4F) * 14.0F);
      g.color(-10134);
      Geo.tube(g, "hd_gold", p(0.0F, 0.0F, 0.0F), p(0.0F, -1.6F, 0.0F), 0.05F, 0.05F, 4);
      g.translate(0.0F, -2.2F, 0.0F);
      g.rotY(t * 60.0F);
      g.glow(true);
      XmasGeo.star(g, "hd_gold", 0.7F, 0.25F, 5);
      g.glow(false);
      g.pop();
      g.pop();
      g.color(-1);
   }

   static void catEars(G g, Cos.A a) {
      float t = a.time;
      float twitch = (float)Math.max(0.0, Math.sin(t * 3.1F) - 0.85F) * 55.0F;
      float[][] out = earShape(4.6F, 4.9F, 0.4F, 5);
      float[][] in = earShape(2.9F, 3.3F, 0.22F, 4);

      for (int s = 1; s >= -1; s -= 2) {
         g.push();
         g.scale(s, 1.0F, 1.0F);
         g.translate(2.25F, 7.75F, 0.2F);
         g.rotZ(-12.0F - (s > 0 ? twitch : 0.0F));
         g.rotY(-12.0F);
         ear(g, out, shift(in, 0.0F, 0.4F), "hd_fur", -10857886, -25930, 1.05F);
         g.color(-725260);

         for (int i = 0; i < 3; i++) {
            Geo.ellipsoid(g, "hd_fur", -0.35F + i * 0.35F, 0.75F + i * 0.25F, 0.62F, 0.22F, 0.8F, 0.15F, 5, 3);
         }

         g.pop();
      }

      g.color(-1);
   }

   static float[][] shift(float[][] o, float dx, float dy) {
      float[][] r = new float[o.length][];

      for (int i = 0; i < o.length; i++) {
         r[i] = new float[]{o[i][0] + dx, o[i][1] + dy};
      }

      return r;
   }

   static void bunnyEars(G g, Cos.A a) {
      float t = a.time;
      float mv = a.preview ? 0.0F : a.move;

      for (int s = 1; s >= -1; s -= 2) {
         g.push();
         g.scale(s, 1.0F, 1.0F);
         g.translate(1.85F, 7.6F, 0.2F);
         g.rotZ(-9.0F);
         float flop = (s > 0 ? 55 : 8) + Cos3Geo.sin(t * 2.2F + s) * 6.0F + mv * 14.0F;
         float[][] sp = bentSpine(0.0F, 9.4F, 10, flop, s > 0 ? 180.0 : 170.0, 0.55F);
         int m = sp.length;
         float[] rx = new float[m];
         float[] rz = new float[m];

         for (int i = 0; i < m; i++) {
            float f = (float)i / (m - 1);
            float w = (float)Math.sin(Math.PI * Math.min(1.0F, f * 0.92F + 0.08F));
            rx[i] = Math.max(0.08F, 0.55F + 1.05F * w);
            rz[i] = Math.max(0.06F, 0.5F * w + 0.18F);
         }

         g.color(-593164);
         XmasGeo.sweep(g, "hd_fur", sp, rx, rz, null, 14, 2.0F);
         float[][] sp2 = new float[m][];
         float[] rx2 = new float[m];
         float[] rz2 = new float[m];

         for (int i = 0; i < m; i++) {
            float f = (float)i / (m - 1);
            sp2[i] = new float[]{sp[i][0], sp[i][1], sp[i][2] + rz[i] * 0.72F};
            rx2[i] = Math.max(0.05F, rx[i] * 0.55F * (float)Math.sin(Math.PI * Math.min(1.0F, 0.1F + f * 0.9F)));
            rz2[i] = 0.12F;
         }

         g.color(-24900);
         XmasGeo.sweep(g, "hd_velvet", frontOf(sp, rz, 0.72F), rx2, rz2, null, 10, 1.0F);
         g.pop();
      }

      g.color(-1);
   }

   static float[][] frontOf(float[][] sp, float[] rz, float k) {
      int m = sp.length;
      float[][] out = new float[m][];

      for (int i = 0; i < m; i++) {
         float[] a0 = sp[Math.max(0, i - 1)];
         float[] b0 = sp[Math.min(m - 1, i + 1)];
         float tx = b0[0] - a0[0];
         float ty = b0[1] - a0[1];
         float tz = b0[2] - a0[2];
         float l = (float)Math.sqrt(tx * tx + ty * ty + tz * tz);
         if (l < 1.0E-5F) {
            out[i] = sp[i];
         } else {
            tx /= l;
            ty /= l;
            tz /= l;
            float fx = -tz * tx;
            float fy = -tz * ty;
            float fz = 1.0F - tz * tz;
            float fl = (float)Math.sqrt(fx * fx + fy * fy + fz * fz);
            if (fl < 1.0E-4F) {
               fx = 0.0F;
               fy = 1.0F;
               fz = 0.0F;
               fl = 1.0F;
            }

            out[i] = new float[]{sp[i][0] + fx / fl * rz[i] * k, sp[i][1] + fy / fl * rz[i] * k, sp[i][2] + fz / fl * rz[i] * k};
         }
      }

      return out;
   }

   static void horns(G g, Cos.A a) {
      float t = a.time;

      for (int s = 1; s >= -1; s -= 2) {
         g.push();
         g.scale(s, 1.0F, 1.0F);
         g.translate(2.5F, 7.75F, 1.0F);
         g.rotZ(-26.0F);
         g.rotY(-8.0F);
         g.color(-1);
         HdGeo.horn(g, "hd_horn", 1.15F, 6.4F, -95.0F, 30.0F, 16, 12, 7.0F);
         g.pop();
      }
   }

   static void headphones(G g, Cos.A a) {
      float t = a.time;
      float beat = (float)Math.pow(Math.max(0.0, Math.sin(t * 7.5)), 6.0);
      int n = 18;
      float[][] band = new float[n + 1][];
      float[][] pad = new float[n - 5][];

      for (int i = 0; i <= n; i++) {
         double an = Math.PI * i / n;
         float[] q = Geo.sq(an, 1.0F, 3.2F);
         band[i] = p(q[0] * 5.4F, 3.6F + q[1] * 5.75F, 0.0F);
      }

      float[] rx = new float[n + 1];
      float[] rz = new float[n + 1];
      Arrays.fill(rx, 0.38F);
      Arrays.fill(rz, 0.95F);
      g.color(-14539732);
      XmasGeo.sweep(g, "hd_rubber", band, rx, rz, null, 10, 1.0F);

      for (int i = 3; i <= n - 3; i++) {
         double an = Math.PI * i / n;
         float[] q = Geo.sq(an, 1.0F, 3.2F);
         pad[i - 3] = p(q[0] * 4.95F, 3.6F + q[1] * 5.3F, 0.0F);
      }

      float[] pr = new float[pad.length];
      float[] pz = new float[pad.length];
      Arrays.fill(pr, 0.34F);
      Arrays.fill(pz, 0.75F);
      g.color(-12960698);
      XmasGeo.sweep(g, "hd_leather", pad, pr, pz, null, 10, 2.0F);

      for (int s = 1; s >= -1; s -= 2) {
         g.push();
         g.translate(s * 4.05F, 3.3F, 0.0F);
         g.rotZ(s * -90);
         g.color(-4669752);
         Cos3Geo.cbox(g, "hd_chrome", 0.0F, 1.2F, 0.0F, 0.5F, 0.3F, 0.3F, 0.08F);
         g.color(-13750216);
         g.push();
         g.translate(0.0F, 0.35F, 0.0F);
         g.torus("hd_leather", 2.05F, 0.5F, 22, 8, 0.0F, 0.0F, 3.0F, 1.0F);
         g.pop();
         g.color(-15066335);
         Cos3Geo.lathe(g, "hd_plastic", new float[]{2.6F, 2.75F, 2.72F, 2.5F, 2.0F, 0.0F}, new float[]{0.6F, 0.9F, 1.6F, 2.05F, 2.25F, 2.3F}, 24);
         g.color(-2564376);
         g.push();
         g.translate(0.0F, 1.95F, 0.0F);
         g.torus("hd_chrome", 2.35F, 0.14F, 24, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
         g.glow(true).color(0xFF000000 | Capes.mix(1353800, 8191912, beat));
         g.push();
         g.translate(0.0F, 2.28F, 0.0F);
         g.torus("white", 1.35F, 0.13F, 20, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
         g.glow(false);
         Cos3Geo.halo(g, 0.0F, 2.5F, 0.0F, 3.2F + beat * 1.2F, Cos3Geo.alpha(0.2 + 0.25 * beat, 3866490));
         g.pop();
      }

      g.color(-1);
   }

   static void foxEars(G g, Cos.A a) {
      float twitch = (float)Math.max(0.0, Math.sin(a.time * 2.7F) - 0.8F) * 60.0F;
      float[][] out = earShape(4.3F, 5.3F, 0.3F, 6);
      float[][] in = earShape(2.6F, 3.4F, 0.15F, 4);

      for (int s = 1; s >= -1; s -= 2) {
         g.push();
         g.scale(s, 1.0F, 1.0F);
         g.translate(2.35F, 7.7F, 0.1F);
         g.rotZ(-15.0F - (s > 0 ? twitch : 0.0F));
         g.rotY(-14.0F);
         g.rotX(-6.0F);
         g.color(-1);
         HdGeo.puff(g, "hd_foxear", out, 1.05F, 0.45F, 2, new float[]{0.0F, 0.0F, 1.0F, 1.0F}, new float[]{0.4F, 0.6F, 0.6F, 0.7F});
         g.color(-594202);
         g.push();
         g.translate(0.0F, 0.4F, 0.45F);
         HdGeo.puff(g, "hd_fur", in, 0.45F, 0.2F, 1);

         for (int i = 0; i < 4; i++) {
            Geo.ellipsoid(g, "hd_fur", -0.5F + i * 0.33F, 0.9F + i % 2 * 0.3F, 0.28F, 0.2F, 0.95F, 0.14F, 5, 3);
         }

         g.pop();
         g.pop();
      }

      g.color(-1);
   }

   static void cowboy(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 6.9F, 0.0F);
      g.rotZ(-4.0F);
      g.color(-6659530);
      brim(g, "hd_leather", 4.75F, 5.0F, 8.7F, 1.08F, 0.38F, 36, 4, (anx, f) -> {
         float side = (float)Math.pow(Math.abs(Math.cos(anx)), 1.6);
         float front = (float)Math.max(0.0, Math.sin(anx));
         return 0.45F + f * f * (2.6F * side - 0.7F * front * (1.0F - side) - 0.3F * (float)Math.max(0.0, -Math.sin(anx)) * (1.0F - side));
      });
      int seg = 26;
      int rows = 7;
      float[][][] ring = new float[rows + 1][seg + 1][];

      for (int j = 0; j <= rows; j++) {
         float f = (float)j / rows;

         for (int i = 0; i <= seg; i++) {
            double an = (Math.PI * 2) * i / seg;
            float r = 4.72F - 0.55F * f - (f > 0.55F ? 1.6F * (float)Math.pow((f - 0.55F) / 0.45F, 2.0) : 0.0F);
            float pinch = f > 0.5F ? 0.55F * (float)Math.pow(Math.max(0.0, Math.sin(an)), 6.0) * (f - 0.5F) * 2.0F : 0.0F;
            float[] q = Geo.sq(an, r - pinch, 5.0F - f * 2.0F);
            float y = 0.3F + 5.4F * (float)Math.sin(f * Math.PI / 2.0 * 0.95);
            float dent = f > 0.85F ? -0.9F * (float)Math.pow(Math.max(0.0, Math.cos(an) * Math.cos(an)), 0.5) * (f - 0.85F) / 0.15F : 0.0F;
            ring[j][i] = p(q[0], y + dent, q[1]);
         }
      }

      for (int j = 0; j < rows; j++) {
         for (int i = 0; i < seg; i++) {
            g.quad(
               "hd_leather",
               ring[j + 1][i],
               ring[j + 1][i + 1],
               ring[j][i + 1],
               ring[j][i],
               new float[]{
                  (float)i / seg,
                  1.0F - (j + 1.0F) / rows,
                  (i + 1.0F) / seg,
                  1.0F - (j + 1.0F) / rows,
                  (i + 1.0F) / seg,
                  1.0F - (float)j / rows,
                  (float)i / seg,
                  1.0F - (float)j / rows
               }
            );
         }
      }

      float[] cTop = p(0.0F, ring[rows][0][1] - 0.6F, 0.0F);

      for (int i = 0; i < seg; i++) {
         g.quad("hd_leather", cTop, ring[rows][i + 1], ring[rows][i], ring[rows][i], new float[]{0.5F, 0.5F, 0.5F, 0.0F, 0.5F, 0.0F, 0.5F, 0.0F});
      }

      g.color(-12966892);
      Geo.sqTube(g, "hd_leather", 4.79F, 4.64F, 0.55F, 1.55F, 5.0F, 26, 0.0F, 0.0F, 2.0F, 1.0F);
      g.color(-1);
      g.push();
      g.translate(0.0F, 1.05F, 4.82F);
      g.color(-2564376);
      g.rotX(90.0F);
      Cos3Geo.lathe(g, "hd_silver", new float[]{0.0F, 0.55F, 0.72F, 0.7F, 0.0F}, new float[]{0.36F, 0.3F, 0.12F, 0.0F, 0.0F}, 14);
      g.pop();
      g.pop();
      g.color(-1);
   }

   static void beanie(G g, Cos.A a) {
      float t = a.time;
      float mv = a.preview ? 0.0F : a.move;
      g.push();
      g.translate(0.0F, 5.0F, 0.0F);
      g.color(-13737256);
      Geo.sqDome(g, "hd_knit", 4.72F, 4.3F, 1.9F, 5.0F, 26, 6, 0.0F, 0.0F, 3.0F, 1.0F);
      Geo.sqTube(g, "hd_knit", 4.72F, 4.72F, 0.3F, 1.9F, 5.0F, 26, 0.0F, 0.0F, 3.0F, 0.3F);
      g.color(-855306);
      Geo.sqTube(g, "hd_knit", 4.76F, 4.62F, 3.0F, 3.75F, 4.6F, 26, 0.0F, 0.0F, 3.0F, 0.2F);
      g.color(-14396744);

      for (int k = 0; k < 3; k++) {
         float rr = 5.02F - Math.abs(k - 1) * 0.12F;
         Geo.sqTube(g, "hd_knit", rr, k == 2 ? 4.8F : 5.02F - Math.abs(k) * 0.12F, -0.25F + k * 0.8F, 0.55F + k * 0.8F, 5.0F, 26, 0.0F, 0.0F, 4.0F, 0.3F);
      }

      Geo.sqTube(g, "hd_knit", 4.8F, 4.8F, -0.25F, -0.249F, 5.0F, 26, 0.0F, 0.0F, 1.0F, 1.0F);
      float wob = Cos3Geo.sin(t * 3.0F) * 6.0F + mv * 10.0F;
      g.translate(0.0F, 6.0F, 0.0F);
      g.rotZ(wob);
      g.rotX(Cos3Geo.sin(t * 2.2) * 5.0F);
      g.translate(0.0F, 1.15F, 0.0F);
      g.color(-592134);
      Cos3Geo.ball(g, "hd_fluff", 0.0F, 0.0F, 0.0F, 1.75F, 14);

      for (int i = 0; i < 10; i++) {
         double an = i * 2.4;
         double el = (i % 3 - 1) * 0.7;
         Cos3Geo.ball(
            g, "hd_fluff", Cos3Geo.cos(an) * Cos3Geo.cos(el) * 1.25F, Cos3Geo.sin(el) * 1.25F + 0.2F, Cos3Geo.sin(an) * Cos3Geo.cos(el) * 1.25F, 0.75F, 6
         );
      }

      g.pop();
      g.color(-1);
   }

   static void flower(G g, int petals, float r, int col, int center, float cup) {
      int oc = g.color;
      float[][] petal = new float[][]{{0.0F, 0.0F}, {r * 0.4F, r * 0.45F}, {r * 0.28F, r * 0.9F}, {0.0F, r}, {-r * 0.28F, r * 0.9F}, {-r * 0.4F, r * 0.45F}};
      g.color(col);

      for (int i = 0; i < petals; i++) {
         g.push();
         g.rotY(360.0F / petals * i);
         g.rotX(-90.0F + cup);
         HdGeo.puff(g, "hd_petal", petal, 0.16F, 0.07F, 1, new float[]{0.0F, 0.0F, 1.0F, 1.0F}, new float[]{0.4F, 0.4F, 0.6F, 0.6F});
         g.pop();
      }

      g.color(center);
      Cos3Geo.ball(g, "hd_gem", 0.0F, 0.1F, 0.0F, r * 0.28F, 6);
      g.color(oc);
   }

   static void leaf(G g, float len, int col) {
      float w = len * 0.42F;
      float[][] o = new float[][]{
         {0.0F, 0.0F},
         {len * 0.25F, w * 0.8F},
         {len * 0.55F, w},
         {len * 0.82F, w * 0.6F},
         {len, 0.0F},
         {len * 0.82F, -w * 0.6F},
         {len * 0.55F, -w},
         {len * 0.25F, -w * 0.8F}
      };
      int oc = g.color;
      g.color(col);
      HdGeo.puff(g, "hd_leafw", o, 0.14F, 0.06F, 1, new float[]{0.0F, 0.0F, 1.0F, 1.0F}, new float[]{0.4F, 0.4F, 0.6F, 0.6F});
      g.color(oc);
   }

   static void flowerCrown(G g, Cos.A a) {
      float t = a.time;
      int n = 32;
      float[][] vine = new float[n + 1][];

      for (int i = 0; i <= n; i++) {
         double an = (Math.PI * 2) * i / n;
         float[] q = Cos3Models.loopPt(2.0F, 3.25F, an);
         vine[i] = p(q[0], 8.0F + 0.18F * Cos3Geo.sin(an * 5.0), q[1]);
      }

      float[] vr = new float[n + 1];
      Arrays.fill(vr, 0.34F);
      g.color(-1);
      XmasGeo.sweep(g, "hd_vine", vine, vr, vr, null, 6, 6.0F);
      int[] cols = new int[]{-30024, -1, -10166, -7685889, -1531649};
      int flowers = 7;

      for (int i = 0; i < flowers; i++) {
         double an = (Math.PI * 2) * i / flowers + 0.3;
         float[] q = Cos3Models.loopPt(2.0F, 3.45F, an);
         g.push();
         g.translate(q[0], 8.35F, q[1]);
         g.rotY((float)(-Math.toDegrees(an)) + 90.0F);
         g.rotX(55.0F + Cos3Geo.sin(t * 1.8F + i) * 4.0F);
         float s = 1.5F + 0.35F * (i * 5 % 3) / 2.0F;
         flower(g, i % 3 == 1 ? 6 : 5, s, cols[i % cols.length], i % 5 == 2 ? -30166 : -12246, 22.0F);
         g.pop();

         for (int k = -1; k <= 1; k += 2) {
            if (k <= 0 && i % 2 != 1) {
               double an2 = an + k * 0.24;
               float[] q2 = Cos3Models.loopPt(2.0F, 3.3F, an2);
               g.push();
               g.translate(q2[0], 8.1F, q2[1]);
               g.rotY((float)(-Math.toDegrees(an2)) + 90.0F + (k > 0 ? 180 : 0) + 90.0F);
               g.rotZ(20.0F);
               g.rotX(40.0F);
               leaf(g, 1.5F, -11884478);
               g.pop();
            }
         }
      }

      float ph = Cos3Geo.fract(t * 0.12F);
      if (ph < 0.6F) {
         double an = ph * 4.0F;
         g.push();
         g.translate(Cos3Geo.cos(an) * 6.5F, 11.0F + Cos3Geo.sin(t * 3.0F) * 0.6F, Cos3Geo.sin(an) * 6.5F);
         g.rotY((float)(-Math.toDegrees(an)));
         float flap = 30.0F + 45.0F * Math.abs(Cos3Geo.sin(t * 14.0F));

         for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.rotZ(s * flap);
            g.color(-20438);
            g.push();
            g.translate(s * 0.7F, 0.0F, 0.0F);
            g.rotX(90.0F);
            HdGeo.puff(g, "hd_petal", HdGeo.circle(0.7F, 0.5F, 10), 0.06F, 0.02F, 1);
            g.pop();
            g.pop();
         }

         g.color(-14018032);
         Geo.ellipsoid(g, "hd_plastic", 0.0F, 0.0F, 0.0F, 0.12F, 0.12F, 0.55F, 5, 3);
         g.pop();
      }

      g.color(-1);
   }

   static void viking(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 6.1F, 0.0F);
      g.color(-4669752);
      Geo.sqDome(g, "hd_steel", 4.82F, 4.3F, 0.8F, 5.0F, 26, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      Geo.sqTube(g, "hd_steel", 4.82F, 4.82F, -0.2F, 0.8F, 5.0F, 26, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-2581942);
      Geo.sqTube(g, "hd_gold", 5.0F, 5.0F, -0.55F, 0.95F, 5.0F, 26, 0.0F, 0.0F, 1.0F, 1.0F);
      Geo.sqTube(g, "hd_gold", 4.86F, 5.0F, -0.55F, -0.54F, 5.0F, 26, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int k = 0; k < 2; k++) {
         g.push();
         g.rotY(k * 90);
         int n = 12;
         float[][] sp = new float[n + 1][];

         for (int i = 0; i <= n; i++) {
            double an = Math.PI * i / n;
            float[] q = Geo.sq(an, 1.0F, 2.6F);
            sp[i] = p(q[0] * 4.95F, 0.8F + q[1] * 4.42F, 0.0F);
         }

         float[] r = new float[n + 1];
         float[] rz = new float[n + 1];
         Arrays.fill(r, 0.18F);
         Arrays.fill(rz, 0.55F);
         XmasGeo.sweep(g, "hd_gold", sp, r, rz, null, 6, 1.0F);
         g.pop();
      }

      g.color(-1523584);

      for (int i = 0; i < 16; i++) {
         float[] q = Geo.sq((Math.PI * 2) * i / 16.0 + 0.1, 5.08F, 5.0F);
         Cos3Geo.ball(g, "hd_gold", q[0], 0.2F, q[1], 0.2F, 6);
      }

      g.color(-2581942);
      float[][] nose = new float[][]{{-0.55F, 0.4F}, {0.55F, 0.4F}, {0.42F, -2.2F}, {0.0F, -2.6F}, {-0.42F, -2.2F}};
      g.push();
      g.translate(0.0F, 0.0F, 5.05F);
      HdGeo.puff(g, "hd_gold", nose, 0.4F, 0.15F, 2);
      g.pop();
      g.color(-1);

      for (int s = 1; s >= -1; s -= 2) {
         g.push();
         g.scale(s, 1.0F, 1.0F);
         g.translate(4.8F, 1.9F, 0.2F);
         g.rotZ(-78.0F);
         g.rotY(-22.0F);
         HdGeo.horn(g, "hd_ivory", 1.2F, 7.0F, -62.0F, -20.0F, 14, 12, 5.0F);
         g.color(-2581942);
         g.push();
         g.translate(0.0F, 0.35F, 0.0F);
         g.torus("hd_gold", 1.18F, 0.22F, 14, 5, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
         g.color(-1);
         g.pop();
      }

      g.pop();
   }

   static void propeller(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(0.0F, 6.2F, 0.0F);
      int[] segCol = new int[]{-1900533, -13053, -16749385, -16736198, -1900533, -13053};

      for (int k = 0; k < 6; k++) {
         g.color(segCol[k]);
         domeSector(g, "hd_plastic", 4.86F, 3.5F, 5.0F, k * Math.PI / 3.0, (k + 1) * Math.PI / 3.0, 6, 6);
      }

      g.color(-855310);
      Geo.sqTube(g, "hd_plastic", 4.9F, 4.9F, -0.1F, 0.5F, 5.0F, 24, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-14787896);
      g.push();
      g.translate(0.0F, 0.25F, 0.0F);
      int vs = 12;

      for (int i = 0; i < vs; i++) {
         double a0 = Math.PI * (0.18 + 0.64 * i / vs);
         double a1 = Math.PI * (0.18 + 0.64 * (i + 1) / vs);
         float[] in0 = Geo.sq(a0, 4.9F, 5.0F);
         float[] in1 = Geo.sq(a1, 4.9F, 5.0F);
         float r0 = 8.2F - 2.2F * (float)Math.pow(Math.abs(Math.cos(a0)), 2.0);
         float r1 = 8.2F - 2.2F * (float)Math.pow(Math.abs(Math.cos(a1)), 2.0);
         float[] A = p(in0[0], 0.0F, in0[1]);
         float[] B = p(in1[0], 0.0F, in1[1]);
         float[] C = p(Cos3Geo.cos(a1) * r1, -0.7F, Cos3Geo.sin(a1) * r1);
         float[] D = p(Cos3Geo.cos(a0) * r0, -0.7F, Cos3Geo.sin(a0) * r0);
         float d = 0.18F;
         g.quad("hd_plastic", up(D, d), up(C, d), up(B, d), up(A, d), new float[]{0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F});
         g.quad("hd_plastic", A, B, C, D, new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});
         g.quad("hd_plastic", D, C, up(C, d), up(D, d), new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});
      }

      g.pop();
      g.color(-13053);
      g.translate(0.0F, 3.45F, 0.0F);
      Cos3Geo.lathe(g, "hd_plastic", new float[]{0.7F, 0.42F, 0.35F, 0.35F}, new float[]{0.0F, 0.3F, 0.5F, 1.4F}, 10);
      g.translate(0.0F, 1.4F, 0.0F);
      g.color(-1900533);
      Cos3Geo.ball(g, "hd_gloss", 0.0F, 0.1F, 0.0F, 0.62F, 10);
      g.rotY(t * (a.preview ? 260.0F : 720.0F + a.move * 900.0F));
      int[] cols = new int[]{-1900533, -16749385, -16736198};
      float[][] blade = new float[][]{
         {0.45F, -0.35F}, {2.6F, -0.9F}, {4.7F, -0.85F}, {5.4F, -0.3F}, {5.4F, 0.35F}, {4.7F, 0.85F}, {2.6F, 0.75F}, {0.45F, 0.35F}
      };

      for (int b = 0; b < 3; b++) {
         g.push();
         g.rotY(b * 120);
         g.rotX(74.0F);
         g.color(cols[b]);
         HdGeo.puff(g, "hd_plastic", blade, 0.2F, 0.08F, 1);
         g.pop();
      }

      g.pop();
      g.color(-1);
   }

   private static float[] up(float[] q, float d) {
      return p(q[0], q[1] + d, q[2]);
   }

   static void domeSector(G g, String t, float r, float h, float n, double a0, double a1, int seg, int rings) {
      for (int j = 0; j < rings; j++) {
         double p0 = (Math.PI / 2) * j / rings;
         double p1 = (Math.PI / 2) * (j + 1) / rings;
         float ra = r * Cos3Geo.cos(p0);
         float rb = r * Cos3Geo.cos(p1);
         float ya = h * Cos3Geo.sin(p0);
         float yb = h * Cos3Geo.sin(p1);
         float na = Geo.lerp(n, 2.0F, (float)j / rings);
         float nb = Geo.lerp(n, 2.0F, (float)(j + 1) / rings);

         for (int i = 0; i < seg; i++) {
            double b0 = a0 + (a1 - a0) * i / seg;
            double b1 = a0 + (a1 - a0) * (i + 1) / seg;
            float[] q00 = Geo.sq(b0, ra, na);
            float[] q01 = Geo.sq(b1, ra, na);
            float[] q10 = Geo.sq(b0, rb, nb);
            float[] q11 = Geo.sq(b1, rb, nb);
            g.quad(
               t,
               p(q10[0], yb, q10[1]),
               p(q11[0], yb, q11[1]),
               p(q01[0], ya, q01[1]),
               p(q00[0], ya, q00[1]),
               new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
            );
         }
      }
   }

   static void chef(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(0.0F, 6.4F, 0.0F);
      int seg = 64;
      int rows = 3;

      for (int j = 0; j < rows; j++) {
         float y0 = 0.8F + 4.6F * j / rows;
         float y1 = 0.8F + 4.6F * (j + 1) / rows;

         for (int i = 0; i < seg; i++) {
            double a0 = (Math.PI * 2) * i / seg;
            double a1 = (Math.PI * 2) * (i + 1) / seg;
            float w0 = 0.32F * (float)Math.abs(Math.sin(a0 * 8.0));
            float w1 = 0.32F * (float)Math.abs(Math.sin(a1 * 8.0));
            float g0 = 0.1F * j / rows;
            float g1 = 0.1F * (j + 1) / rows;
            float[] b0 = Geo.sq(a0, 4.8F + w0 + g0, 5.0F);
            float[] b1 = Geo.sq(a1, 4.8F + w1 + g0, 5.0F);
            float[] t0 = Geo.sq(a0, 4.8F + w0 + g1, 5.0F);
            float[] t1 = Geo.sq(a1, 4.8F + w1 + g1, 5.0F);
            g.color(-460550);
            g.quad(
               "hd_cotton",
               p(t0[0], y1, t0[1]),
               p(t1[0], y1, t1[1]),
               p(b1[0], y0, b1[1]),
               p(b0[0], y0, b0[1]),
               new float[]{0.0F, 0.0F, 0.1F, 0.0F, 0.1F, 1.0F, 0.0F, 1.0F}
            );
         }
      }

      g.color(-1513234);
      Geo.sqTube(g, "hd_cotton", 5.0F, 5.0F, -0.2F, 1.3F, 5.0F, 26, 0.0F, 0.0F, 3.0F, 0.3F);
      float puff = 1.0F + Cos3Geo.sin(t * 1.5F) * 0.03F;
      g.color(-1);

      for (int i = 0; i < 7; i++) {
         double an = (Math.PI * 2) * i / 7.0 + 0.3;
         Geo.ellipsoid(g, "hd_cotton", Cos3Geo.cos(an) * 3.1F, 6.4F, Cos3Geo.sin(an) * 3.1F, 2.6F * puff, 2.2F * puff, 2.6F * puff, 14, 9);
      }

      Geo.ellipsoid(g, "hd_cotton", 0.0F, 7.35F, 0.0F, 3.4F * puff, 2.5F * puff, 3.4F * puff, 16, 10);
      g.pop();
   }

   static void party(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(1.1F, 7.95F, 0.2F);
      g.rotZ(-13.0F + Cos3Geo.sin(t * 2.2F) * 4.0F);
      g.color(-1);
      Cos3Geo.lathe(
         g, "hd_party", new float[]{3.35F, 3.1F, 2.4F, 1.5F, 0.7F, 0.12F}, new float[]{0.1F, 0.9F, 3.0F, 5.4F, 7.6F, 9.1F}, 24, 0.0F, 1.0F, 2.0F, 0.0F
      );
      g.color(-7606);
      int n = 40;
      float[][] fr = new float[n + 1][];

      for (int i = 0; i <= n; i++) {
         double an = (Math.PI * 2) * i / n;
         fr[i] = p(Cos3Geo.cos(an) * 3.4F, 0.25F + 0.22F * Cos3Geo.sin(an * 10.0), Cos3Geo.sin(an) * 3.4F);
      }

      float[] rr = new float[n + 1];
      Arrays.fill(rr, 0.42F);
      XmasGeo.sweep(g, "hd_satin", fr, rr, rr, null, 8, 8.0F);
      g.translate(0.0F, 9.3F, 0.0F);
      g.color(-46438);
      Cos3Geo.ball(g, "hd_fluff", 0.0F, 0.0F, 0.0F, 0.95F, 10);
      int[] cs = new int[]{-46438, -12924673, -7606, -9764998, -5215489};

      for (int i = 0; i < 12; i++) {
         g.push();
         g.rotY(i * 97);
         g.rotZ(20 + i % 4 * 22 + Cos3Geo.sin(t * 3.0F + i) * 5.0F);
         g.color(cs[i % 5]);
         Cos3Geo.cbox(g, "hd_satin", 0.0F, 1.2F, 0.0F, 0.35F, 1.8F, 0.06F, 0.02F);
         g.pop();
      }

      g.pop();
      int[] cols = new int[]{-50550, -12924673, -7606, -9764998, -5215489};

      for (int i = 0; i < 14; i++) {
         float ph = Cos3Geo.fract(t * 0.4F + i / 14.0F);
         double an = i * 2.4 + t * 0.5;
         float sc = Math.min(1.0F, Cos3Geo.sin(ph * Math.PI) * 2.2F);
         if (!(sc <= 0.05F)) {
            g.push();
            g.translate(Cos3Geo.cos(an) * (4.5F + i % 3), 20.0F - ph * 15.0F, Cos3Geo.sin(an) * (4.5F + i % 3));
            g.rotY(t * 200.0F + i * 40);
            g.rotX(t * 150.0F + i * 70);
            g.scale(sc);
            g.color(cols[i % 5]);
            if (i % 3 == 0) {
               Geo.ellipsoid(g, "hd_plastic", 0.0F, 0.0F, 0.0F, 0.3F, 0.3F, 0.06F, 6, 3);
            } else {
               Cos3Geo.cbox(g, "hd_plastic", 0.0F, 0.0F, 0.0F, 0.8F, 0.45F, 0.06F, 0.02F);
            }

            g.pop();
         }
      }

      g.color(-1);
   }

   static void wizard(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(0.0F, 7.3F, 0.0F);
      g.rotZ(-5.0F);
      g.color(-12965200);
      brim(g, "hd_felt", 4.75F, 5.0F, 9.2F, 1.0F, 0.34F, 40, 4, (anx, f) -> 0.2F + f * f * (0.5F * Cos3Geo.sin(anx * 3.0 + 1.0) - 0.35F));
      g.color(-1);
      float sway = Cos3Geo.sin(t * 1.4F) * 10.0F;
      float[][] sp = bentSpine(0.2F, 13.5F, 16, 95.0F + sway, 200.0F + sway, 0.4F);
      int m = sp.length;
      float[] rx = new float[m];
      float[] nn = new float[m];

      for (int i = 0; i < m; i++) {
         float f = (float)i / (m - 1);
         rx[i] = Math.max(0.06F, 4.74F * (float)Math.pow(1.0F - f, 1.3) + 0.05F);
         nn[i] = f < 0.1F ? 5.0F : 2.0F;
      }

      XmasGeo.sweep(g, "hd_wizard", sp, rx, rx, nn, 24, 2.0F);
      g.color(-10134);
      Geo.sqTube(g, "hd_gold", 4.83F, 4.6F, 0.35F, 1.7F, 5.0F, 24, 0.0F, 0.0F, 3.0F, 1.0F);
      g.push();
      g.translate(0.0F, 1.05F, 4.9F);
      float[][] moon = new float[20][];

      for (int i = 0; i < 10; i++) {
         double an = (Math.PI / 5) + (Math.PI * 8.0 / 5.0) * i / 9.0;
         moon[i] = new float[]{Cos3Geo.cos(an + (Math.PI / 2)) * 1.2F, Cos3Geo.sin(an + (Math.PI / 2)) * 1.2F};
         moon[19 - i] = new float[]{Cos3Geo.cos(an + (Math.PI / 2)) * 0.9F + 0.45F * (float)Math.sin(an) * 0.0F, Cos3Geo.sin(an + (Math.PI / 2)) * 0.9F + 0.1F};
      }

      for (int i = 0; i < 10; i++) {
         moon[19 - i][0] = moon[19 - i][0] + 0.45F;
      }

      g.glow(true);
      HdGeo.puff(g, "hd_gold", moon, 0.3F, 0.1F, 1);
      g.glow(false);
      g.pop();
      float[] e = sp[m - 1];
      g.push();
      g.translate(e[0], e[1] + 0.2F, e[2]);
      g.rotY(t * 70.0F);
      g.glow(true).color(-7558);
      XmasGeo.star(g, "hd_gold", 1.0F, 0.3F, 5);
      g.glow(false);
      Cos3Geo.halo(g, 0.0F, 0.0F, 0.0F, 3.6F, Cos3Geo.alpha(0.5 + 0.2 * Cos3Geo.sin(t * 4.0F), 16769146));
      g.pop();
      g.pop();

      for (int i = 0; i < 6; i++) {
         float ph = Cos3Geo.fract(t * 0.35F + i / 6.0F);
         double an = i * 1.05 + t * 0.9;
         float s = Cos3Geo.sin(ph * Math.PI);
         Cos3Models.sparkle(
            g, Cos3Geo.cos(an) * (6.5F + ph * 2.0F), 9.0F + ph * 9.0F, Cos3Geo.sin(an) * (6.5F + ph * 2.0F), 0.5F * s, Cos3Geo.alpha(s, 16773296)
         );
      }

      g.color(-1);
   }

   static void alien(G g, Cos.A a) {
      float t = a.time;
      float mv = a.preview ? 0.0F : a.move;
      int n = 20;
      float[][] bandPts = new float[n + 1][];

      for (int i = 0; i <= n; i++) {
         bandPts[i] = XmasCos.bandPt(Math.PI * i / n, 5.0F, 5.55F, 3.5F);
      }

      float[] br = new float[n + 1];
      float[] bz = new float[n + 1];
      Arrays.fill(br, 0.3F);
      Arrays.fill(bz, 0.62F);
      g.color(-13989318);
      XmasGeo.sweep(g, "hd_plastic", bandPts, br, bz, null, 8, 1.0F);

      for (int s = 1; s >= -1; s -= 2) {
         float[] b = XmasCos.bandPt(s > 0 ? 1.0053096491487339 : 2.1362830044410597, 5.0F, 5.55F, 3.5F);
         int k = 10;
         float[][] pts = new float[k + 1][];
         float[] rad = new float[k + 1];

         for (int i = 0; i <= k; i++) {
            float f = (float)i / k;
            float sw = Cos3Geo.sin(t * 3.2F + s + f * 1.5F) * 1.3F * f * f + mv * f * f * 1.5F;
            pts[i] = p(b[0] + s * f * 2.2F + sw * 0.5F, b[1] + f * 6.8F, b[2] + sw * 0.8F - f * f * 0.9F);
            rad[i] = 0.3F - f * 0.12F;
         }

         g.color(-11870630);
         Cos3Geo.ball(g, "hd_plastic", b[0], b[1] + 0.1F, b[2], 0.55F, 8);
         Cos3Geo.limb(g, "hd_plastic", pts, rad, 7);
         g.color(-13989318);

         for (int i = 1; i < k; i += 2) {
            g.push();
            g.translate(pts[i][0], pts[i][1], pts[i][2]);
            float[] d = pts[i + 1];
            Cos3Geo.alignY(g, d[0] - pts[i][0], d[1] - pts[i][1], d[2] - pts[i][2]);
            g.torus("hd_plastic", rad[i] + 0.06F, 0.07F, 10, 3, 0.0F, 0.0F, 1.0F, 1.0F);
            g.pop();
         }

         float[] tip = pts[k];
         float pulse = 0.75F + 0.25F * Cos3Geo.sin(t * 4.0F + s);
         g.push();
         g.translate(tip[0], tip[1] + 0.85F, tip[2]);
         g.color(-8585366);
         Cos3Geo.ball(g, "hd_bubble", 0.0F, 0.0F, 0.0F, 1.15F, 12);
         g.glow(true).color(0xFF000000 | Capes.mix(3860554, 15269856, pulse));
         Cos3Geo.ball(g, "white", 0.0F, 0.0F, 0.0F, 0.6F * pulse, 8);
         g.glow(false);
         Cos3Geo.halo(g, 0.0F, 0.0F, 0.0F, 4.4F * pulse, Cos3Geo.alpha(0.45 * pulse, 8191850));
         g.pop();
      }

      g.color(-1);
   }

   static void bow(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(1.9F, 8.9F, 1.3F);
      g.rotZ(-14.0F + Cos3Geo.sin(t * 2.0F) * 2.0F);
      g.rotY(-12.0F);
      g.scale(1.3F);
      g.color(-46450);
      float[][] wing = new float[18][];
      wing[0] = new float[]{0.3F, -0.6F};

      for (int i = 0; i < 16; i++) {
         double an = Math.toRadians(-150.0 + 300 * i / 15.0);
         wing[i + 1] = new float[]{2.25F + Cos3Geo.cos(an) * 1.55F, Cos3Geo.sin(an) * 1.75F};
      }

      wing[17] = new float[]{0.3F, 0.6F};

      for (int s = 1; s >= -1; s -= 2) {
         g.push();
         g.scale(s, 1.0F, 1.0F);
         g.rotZ(10.0F);
         g.rotY(-12.0F);
         HdGeo.puff(g, "hd_satin", wing, 1.25F, 0.55F, 2);
         g.pop();
         g.push();
         g.translate(s * 0.35F, -0.4F, 0.2F);
         g.rotZ(s * 24 + Cos3Geo.sin(t * 2.5F + s) * 5.0F);
         float[][] tail = new float[][]{{-0.62F, 0.0F}, {0.62F, 0.0F}, {0.95F, -3.1F}, {0.2F, -2.6F}, {-0.45F, -3.2F}};
         HdGeo.puff(g, "hd_satin", tail, 0.32F, 0.12F, 1);
         g.pop();
      }

      g.color(-2084742);
      Geo.ellipsoid(g, "hd_satin", 0.0F, 0.0F, 0.05F, 0.8F, 1.0F, 0.78F, 12, 8);
      g.color(-1);
      g.pop();
   }

   static void mushroom(G g, Cos.A a) {
      float t = a.time;
      float bob = Cos3Geo.sin(t * 2.0F) * 0.15F;
      g.push();
      g.translate(0.0F, 7.3F + bob, 0.0F);
      g.rotZ(Cos3Geo.sin(t * 1.3F) * 2.0F);
      float[] r = new float[]{5.4F, 6.55F, 6.85F, 6.7F, 6.1F, 5.1F, 3.6F, 1.8F, 0.0F};
      float[] y = new float[]{0.35F, 0.25F, 0.75F, 1.6F, 2.8F, 3.9F, 4.7F, 5.1F, 5.2F};
      g.color(-1);
      Cos3Geo.lathe(g, "hd_mush", reverse(r), reverse(y), 30, 0.0F, 0.0F, 1.0F, 1.0F);
      Cos3Geo.lathe(g, "hd_gill", new float[]{5.4F, 4.4F}, new float[]{0.35F, 0.9F}, 30);
      Random rn = new Random(42L);

      for (int i = 0; i < 13; i++) {
         double an = i * 2.39996;
         float f = i == 0 ? 0.0F : 0.25F + 0.65F * (float)Math.sqrt(i / 13.0F);
         float kf = 2.0F + (1.0F - f) * 5.6F;
         int k = Math.min(7, (int)kf);
         float fr = kf - k;
         float rr = r[k] + (r[k + 1] - r[k]) * fr;
         float yy = y[k] + (y[k + 1] - y[k]) * fr;
         float tr = r[k + 1] - r[k];
         float ty = y[k + 1] - y[k];
         float nx = Cos3Geo.cos(an);
         float nz = Cos3Geo.sin(an);
         g.push();
         g.translate(nx * rr, yy, nz * rr);
         Cos3Geo.alignY(g, nx * ty, -tr, nz * ty);
         float s = 0.55F + rn.nextFloat() * 0.35F;
         g.color(-1810);
         Geo.ellipsoid(g, "hd_cotton", 0.0F, 0.0F, 0.0F, s, 0.22F, s * 0.9F, 8, 3);
         g.pop();
      }

      g.pop();
      g.color(-1);
   }

   static float[] reverse(float[] a) {
      float[] r = new float[a.length];

      for (int i = 0; i < a.length; i++) {
         r[i] = a[a.length - 1 - i];
      }

      return r;
   }

   interface Lift {
      float at(double var1, float var3);
   }
}
