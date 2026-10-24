package dev.lego.cosmetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class XmasBack {
   private XmasBack() {
   }

   static void register() {
      Cos.add("xm_back_scarf", "Weihnachtsschal", Cos.Slot.BACK, Cos.Rarity.RARE, XmasBack::scarf);
      Cos.add("xm_back_tree", "Mini-Weihnachtsbaum", Cos.Slot.BACK, Cos.Rarity.LEGENDARY, XmasBack::treePack);
      Cos.add("xm_back_candycanes", "Zuckerstangen", Cos.Slot.BACK, Cos.Rarity.EPIC, XmasBack::candyCanes);
      Cos.add("xm_back_gifts", "Geschenke-Stapel", Cos.Slot.BACK, Cos.Rarity.EPIC, XmasBack::giftStack);
      Cos.add("xm_aura_snow", "Schneegestöber", Cos.Slot.AURA, Cos.Rarity.EPIC, XmasBack::snowStorm);
      Cos.add("xm_aura_lights", "Lichterketten-Spirale", Cos.Slot.AURA, Cos.Rarity.LEGENDARY, XmasBack::lightSpiral);
      Cos.add("xm_aura_aurora", "Nordlicht-Band", Cos.Slot.AURA, Cos.Rarity.LEGENDARY, XmasBack::auroraBand);
      Cos.add("xm_wings_ice", "Eiskristall-Flügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, XmasBack::iceWings);
      Cos.add("xm_wings_angel", "Goldene Sternenflügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, XmasBack::starWings);
   }

   static float mv(Cos.A a) {
      return a.preview ? 0.0F : Cos3Geo.clamp(a.move, 0.0F, 1.0F);
   }

   static void scarf(G g, Cos.A a) {
      float t = a.time;
      float mv = mv(a);
      float[][] loop = densLoop(Cos3Geo.roundLoop(3.35F, 1.35F, 1.35F, 6), 0.6F);
      int n = loop.length;
      int seg = 10;
      float rh = 0.66F;
      float rv = 1.05F;
      float yc = -1.2F;
      float[][][] ring = new float[n + 1][seg + 1][];

      for (int j = 0; j <= n; j++) {
         float[] L = loop[j % n];
         float ca = (float)Math.cos(L[2]);
         float sa = (float)Math.sin(L[2]);
         float puff = 1.0F + 0.06F * Cos3Geo.sin(j * 2.1F);

         for (int i = 0; i <= seg; i++) {
            double ph = (Math.PI * 2) * i / seg;
            float h = (float)Math.cos(ph) * rh * puff;
            float v = (float)Math.sin(ph) * rv * puff;
            ring[j][i] = new float[]{L[0] + ca * h, yc + v, L[1] + sa * h};
         }
      }

      g.color(-1);

      for (int j = 0; j < n; j++) {
         float va = loop[j][3];
         float vb = j + 1 < n ? loop[j + 1][3] : 1.0F;

         for (int i = 0; i < seg; i++) {
            float ua = (float)i / seg;
            float ub = (float)(i + 1) / seg;
            g.quad("xm_scarf", ring[j][i], ring[j][i + 1], ring[j + 1][i + 1], ring[j + 1][i], new float[]{ua, va, ub, va, ub, vb, ua, vb});
         }
      }

      g.color(-1);
      Geo.ellipsoid(g, "xm_scarf", 2.2F, -1.5F, 2.95F, 1.15F, 1.05F, 0.75F, 10, 6);

      for (int k = 0; k < 2; k++) {
         float len = k == 0 ? 8.2F : 6.8F;
         float z0 = k == 0 ? 2.85F : 3.3F;
         float sw = Cos3Geo.sin(t * 1.6F + k * 1.3F) * 0.18F + mv * (0.6F + 0.2F * Cos3Geo.sin(t * 9.0F + k));
         g.push();
         g.translate(2.2F, -1.6F, 0.0F);
         g.rotZ(k == 0 ? 7.0F : -13.0F);
         int m = 8;
         float[][] yz = new float[m][];
         float[] w = new float[m];

         for (int i = 0; i < m; i++) {
            float f = (float)i / (m - 1);
            yz[i] = new float[]{-f * len, z0 + 0.25F * f + sw * f * f * 2.2F + 0.15F * Cos3Geo.sin(f * 5.0F + t * 1.2F + k)};
            w[i] = 2.0F + 0.15F * f;
         }

         Cos3Geo.ribbon(g, "xm_scarf", 0.0F, yz, w, 0.36F);
         float[] e = yz[m - 1];
         g.color(-723724);

         for (int f = 0; f < 5; f++) {
            float x = -0.85F + f * 0.425F;
            Geo.tube(
               g,
               "xm_knit",
               new float[]{x, e[0] + 0.1F, e[1]},
               new float[]{x + Cos3Geo.sin(t * 2.0F + f) * 0.08F, e[0] - 0.9F, e[1] + sw * 0.4F},
               0.11F,
               0.08F,
               4
            );
         }

         g.color(-1);
         g.pop();
      }
   }

   static float[][] densLoop(float[][] in, float maxLen) {
      List<float[]> out = new ArrayList<>();
      int n = in.length;

      for (int i = 0; i < n; i++) {
         float[] a = in[i];
         float[] b = in[(i + 1) % n];
         float l = (float)Math.hypot(b[0] - a[0], b[1] - a[1]);
         int k = Math.max(1, (int)Math.ceil(l / maxLen));
         float ab = b[2] < a[2] - 1.0F ? b[2] + (float) (Math.PI * 2) : b[2];

         for (int j = 0; j < k; j++) {
            float f = (float)j / k;
            out.add(new float[]{a[0] + (b[0] - a[0]) * f, a[1] + (b[1] - a[1]) * f, a[2] + (ab - a[2]) * f, 0.0F});
         }
      }

      float per = 0.0F;
      float[] acc = new float[out.size()];

      for (int i = 0; i < out.size(); i++) {
         acc[i] = per;
         float[] a = out.get(i);
         float[] b = out.get((i + 1) % out.size());
         per += (float)Math.hypot(b[0] - a[0], b[1] - a[1]);
      }

      for (int i = 0; i < out.size(); i++) {
         out.get(i)[3] = acc[i] / per;
      }

      return out.toArray(new float[0][]);
   }

   static void treePack(G g, Cos.A a) {
      float t = a.time;
      float mv = mv(a);
      float Z = -6.0F;
      g.color(-4159408);
      Cos3Geo.bbox(g, "xm_wood", -2.9F, -11.0F, Z - 2.9F, 2.9F, -7.3F, -2.3F, 0.3F);
      g.color(-8762840);

      for (int i = 0; i < 2; i++) {
         float y = -10.2F + i * 1.9F;
         Cos3Geo.bbox(g, "xm_wood", -3.0F, y - 0.25F, Z - 3.0F, 3.0F, y + 0.25F, -2.2F, 0.1F);
      }

      g.color(-1);
      Geo.ellipsoid(g, "xm_snow", 0.0F, -7.3F, Z + 0.35F, 2.6F, 0.4F, 2.9F, 12, 4);
      g.color(-9813470);
      g.push();
      g.translate(0.0F, 0.0F, Z);
      g.cylinder("xm_bark", 0.62F, 0.55F, -7.4F, -6.2F, 8, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      float sway = Cos3Geo.sin(t * 1.4F) * 1.5F + mv * Cos3Geo.sin(t * 8.0F) * 3.0F;
      g.push();
      g.translate(0.0F, -7.3F, Z);
      g.rotX(sway * 0.4F);
      g.rotZ(sway);
      g.translate(0.0F, 7.3F, 0.0F);
      float[][] tiers = new float[][]{{-6.9F, -2.1F, 3.25F}, {-3.9F, 0.9F, 2.65F}, {-1.2F, 3.3F, 2.05F}, {1.4F, 5.3F, 1.4F}};
      int[] shades = new int[]{-13727158, -13330342, -12934046, -12275606};

      for (int i = 0; i < tiers.length; i++) {
         float y0 = tiers[i][0];
         float y1 = tiers[i][1];
         float R = tiers[i][2];
         float h = y1 - y0;
         g.push();
         g.rotY(i * 23);
         g.color(shades[i]);
         Cos3Geo.lathe(
            g,
            "xm_pine",
            new float[]{0.0F, R * 0.82F, R, R * 0.9F, R * 0.55F, R * 0.22F, 0.0F},
            new float[]{y0 + 0.25F, y0 - 0.05F, y0 + 0.2F, y0 + h * 0.18F, y0 + h * 0.5F, y0 + h * 0.82F, y1},
            14
         );
         g.pop();
      }

      int[] bc = new int[]{-2088918, -14278, -12940545, -2088918, -3618600, -14278, -5227800};

      for (int i = 0; i < 9; i++) {
         int ti = i % 3;
         float y0 = tiers[ti][0];
         float y1 = tiers[ti][1];
         float R = tiers[ti][2];
         float f = 0.22F + 0.1F * (i / 3);
         float y = y0 + (y1 - y0) * f;
         float rr = R * (0.95F - f * 1.05F) + 0.3F;
         double an = i * 2.4 + 0.6;
         float x = Cos3Geo.cos(an) * rr;
         float z = Cos3Geo.sin(an) * rr;
         g.color(-3618616);
         Geo.tube(g, "c3_metal", new float[]{x, y + 0.55F, z}, new float[]{x, y + 0.3F, z}, 0.12F, 0.12F, 4);
         g.color(bc[i % bc.length]);
         Cos3Geo.ball(g, "xm_gloss", x, y, z, 0.42F, 8);
      }

      for (int i = 0; i < 16; i++) {
         float f = i / 16.0F;
         float y = -6.6F + f * 11.2F;
         float rr = 0.0F;

         for (float[] tr : tiers) {
            if (y >= tr[0] - 0.1F && y <= tr[1]) {
               float u = (y - tr[0]) / (tr[1] - tr[0]);
               rr = Math.max(rr, tr[2] * (1.0F - u) * 0.95F + 0.2F);
            }
         }

         if (!(rr < 0.35F)) {
            double an = f * Math.PI * 2.0 * 3.2 + 1.1;
            XmasGeo.fairy(g, Cos3Geo.cos(an) * rr, y, Cos3Geo.sin(an) * rr, 0.24F, XmasGeo.LIGHTS[i % XmasGeo.LIGHTS.length], XmasGeo.twinkle(t, i));
         }
      }

      g.push();
      g.translate(0.0F, 6.05F, 0.0F);
      g.rotY(Cos3Geo.sin(t * 0.8F) * 35.0F);
      g.glow(true).color(-10166);
      XmasGeo.star(g, "xm_gold", 1.15F, 0.4F, 5);
      g.glow(false);
      Cos3Geo.halo(g, 0.0F, 0.0F, 0.0F, 3.6F, Cos3Geo.alpha(0.45 + 0.2 * Cos3Geo.sin(t * 3.0F), 16769146));
      g.pop();
      g.pop();
      g.color(-1);
   }

   static void candyCanes(G g, Cos.A a) {
      float t = a.time;
      float mv = mv(a);
      g.color(-1521496);
      Cos3Geo.bbox(g, "strap", -1.7F, -9.6F, -2.52F, 1.7F, -6.2F, -2.06F, 0.18F);
      g.color(-11414);
      Cos3Geo.bbox(g, "c3_metal", -0.55F, -8.3F, -2.64F, 0.55F, -7.5F, -2.45F, 0.08F);

      for (int s = -1; s <= 1; s += 2) {
         float z = s > 0 ? -2.95F : -4.2F;
         float[] bot = new float[]{-s * 2.3F, -11.8F, z};
         float[] top = new float[]{s * 6.2F, 2.3F, z};
         float dx = top[0] - bot[0];
         float dy = top[1] - bot[1];
         float l = (float)Math.hypot(dx, dy);
         dx /= l;
         dy /= l;
         float px = dy * s;
         float py = -dx * s;
         float R = 1.55F;
         float[] C = new float[]{top[0] + px * R, top[1] + py * R};
         int nh = 10;
         float[][] pts = new float[2 + nh][];
         pts[0] = bot;
         pts[1] = new float[]{bot[0] + dx * l * 0.5F, bot[1] + dy * l * 0.5F, z};

         for (int i = 0; i < nh; i++) {
            double th = Math.PI * (i + 1) / nh * 1.05;
            float cx = (float)(-px * Math.cos(th) + dx * Math.sin(th));
            float cy = (float)(-py * Math.cos(th) + dy * Math.sin(th));
            pts[2 + i] = new float[]{C[0] + cx * R, C[1] + cy * R, z};
         }

         pts[1] = top;
         float[] rr = new float[pts.length];
         Arrays.fill(rr, 0.62F);
         g.push();
         g.translate(0.0F, -7.9F, 0.0F);
         g.rotZ(Cos3Geo.sin(t * 1.2F + s) * 1.2F + mv * Cos3Geo.sin(t * 9.0F) * 2.0F);
         g.translate(0.0F, 7.9F, 0.0F);
         g.color(-1);
         Cos3Geo.limb(g, "xm_candy", pts, rr, 10);
         g.pop();
      }

      g.push();
      g.translate(0.0F, -7.2F, -5.0F);
      g.rotY(180.0F);
      XmasGeo.bow(g, -13719478, 1.1F);
      g.pop();
      g.push();
      g.translate(0.0F, -6.3F, -5.1F);
      g.rotX(-80.0F);
      XmasGeo.holly(g, 0.7F);
      g.pop();
   }

   static void gift(G g, String paper, int ribbonC, float w, float h, float d) {
      float x0 = -w / 2.0F;
      float x1 = w / 2.0F;
      float y0 = 0.0F;
      float z0 = -d / 2.0F;
      float z1 = d / 2.0F;
      g.color(-1);
      Cos3Geo.bbox(g, paper, x0, y0, z0, x1, h, z1, 0.14F);
      float wr = Math.min(w, d) * 0.1F + 0.2F;
      g.color(ribbonC);
      g.box("xm_ribbon", -wr, y0 - 0.06F, z0 - 0.06F, wr, h + 0.06F, z1 + 0.06F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.box("xm_ribbon", x0 - 0.12F, y0 - 0.12F, -wr, x1 + 0.12F, h + 0.12F, wr, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   static void giftStack(G g, Cos.A a) {
      float t = a.time;
      float mv = mv(a);
      float wob = Cos3Geo.sin(t * 1.3F) * 1.2F + mv * Cos3Geo.sin(t * 9.0F) * 2.5F;
      g.push();
      g.translate(0.0F, -11.6F, -4.3F);
      gift(g, "xm_gift:D8202A:FFD24A:1", -11414, 7.2F, 4.6F, 4.3F);
      g.translate(0.0F, 4.6F, -0.25F);
      g.rotZ(wob * 0.5F);
      g.rotY(8.0F);
      gift(g, "xm_gift:2E9A48:FFFFFF:0", -2088918, 5.4F, 3.6F, 3.8F);
      g.translate(0.2F, 3.6F, -0.35F);
      g.rotZ(wob * 0.8F);
      g.rotY(-22.0F);
      gift(g, "xm_gift:3A6AE8:E8F4FF:3", -986888, 3.4F, 2.5F, 2.8F);
      g.translate(0.0F, 2.62F, 0.0F);
      XmasGeo.bow(g, -986888, 0.95F);
      g.pop();
      g.push();
      g.translate(2.6F, -8.4F, -6.52F);
      g.rotZ(Cos3Geo.sin(t * 2.0F) * 6.0F - 10.0F);
      g.color(-726832);
      Cos3Geo.bbox(g, "xm_felt", -0.6F, -1.3F, -0.08F, 0.6F, 0.0F, 0.0F, 0.05F);
      g.pop();

      for (int i = 0; i < 4; i++) {
         float ph = Cos3Geo.fract(t * 0.4F + i * 0.25F);
         Cos3Models.sparkle(
            g, Cos3Geo.sin(i * 2.3F) * 3.8F, -7.0F + ph * 8.0F, -4.5F - Cos3Geo.cos(i * 1.7F) * 2.8F, 0.28F, Cos3Geo.alpha((1.0F - ph) * 0.9, 16773296)
         );
      }

      g.color(-1);
   }

   static void snowStorm(G g, Cos.A a) {
      float t = a.time;

      for (int i = 0; i < 7; i++) {
         double h = Capes.hash1(i, 501);
         float ph = Cos3Geo.fract(t * (0.05F + 0.03F * (float)h) + i / 7.0F);
         float y = 9.0F - ph * 34.0F;
         float an = (float)(i * 51.4 + t * (22.0 + h * 14.0));
         float R = 10.0F + 2.2F * (float)Capes.hash1(i, 502);
         float fade = Cos3Geo.smooth(ph * 8.0F) * Cos3Geo.smooth((1.0F - ph) * 8.0F);
         g.push();
         g.rotY(an);
         g.translate(R, y, 0.0F);
         g.rotY(t * 70.0F + i * 40);
         g.rotX(Cos3Geo.sin(t + i) * 30.0F);
         g.glow(true).color(Cos3Geo.alpha(0.92 * fade, 15267583));
         XmasGeo.flake(g, "white", 1.0F + 0.45F * (float)h, 0.07F);
         g.glow(false);
         g.pop();
      }

      g.color(-1);

      for (int i = 0; i < 34; i++) {
         double h = Capes.hash1(i, 511);
         float ph = Cos3Geo.fract(t * (0.09F + 0.06F * (float)h) + (float)Capes.hash1(i, 512));
         float y = 10.0F - ph * 35.0F;
         float an = (float)(h * 360.0 + t * (35.0 + 25.0 * Capes.hash1(i, 513)) + ph * 120.0F);
         float R = 9.0F + 4.5F * (float)Capes.hash1(i, 514);
         float fade = Cos3Geo.smooth(ph * 6.0F) * Cos3Geo.smooth((1.0F - ph) * 6.0F);
         double ar = Math.toRadians(an);
         float s = 0.6F + 0.7F * (float)Capes.hash1(i, 515);
         g.glow(true).color(Cos3Geo.alpha(0.9 * fade, 16777215));
         g.push();
         g.translate(Cos3Geo.cos(ar) * R, y, Cos3Geo.sin(ar) * R);
         g.rotY(-an + t * 50.0F);
         g.rotZ(t * 80.0F + i * 20);
         g.cross("xm_flake", 0.0F, 0.0F, 0.0F, s, s, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      g.glow(false);

      for (int k = 0; k < 2; k++) {
         int n = 40;
         float[][] top = new float[n + 1][];
         float[][] bot = new float[n + 1][];

         for (int i = 0; i <= n; i++) {
            float f = (float)i / n;
            double an = f * Math.PI * 2.6 + t * (0.9 + k * 0.4) + k * Math.PI;
            float R = 10.5F + k * 1.3F + Cos3Geo.sin(f * 9.0F + t) * 0.5F;
            float y = -20.0F + f * 26.0F + k * 3;
            float wdt = 1.2F;
            top[i] = new float[]{Cos3Geo.cos(an) * R, y + wdt, Cos3Geo.sin(an) * R};
            bot[i] = new float[]{Cos3Geo.cos(an) * R, y - wdt, Cos3Geo.sin(an) * R};
         }

         g.glow(true).color(-1593835521);

         for (int i = 0; i < n; i++) {
            float ua = (float)i / n;
            float ub = (float)(i + 1) / n;
            g.quad("xm_wind", top[i], top[i + 1], bot[i + 1], bot[i], new float[]{ua, 0.0F, ub, 0.0F, ub, 1.0F, ua, 1.0F});
         }

         g.glow(false);
      }

      g.push();
      g.translate(0.0F, -23.9F, 0.0F);
      g.rotX(-90.0F);
      g.color(-1862270977);
      g.plane("orb", 0.0F, 0.0F, 24.0F, 24.0F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.color(-1);
   }

   static void lightSpiral(G g, Cos.A a) {
      float t = a.time;
      float turns = 3.1F;
      float y0 = -23.0F;
      float y1 = 10.0F;
      int n = 90;
      float[][] w = new float[n + 1][];
      double rot = Math.toRadians(t * 18.0F);

      for (int i = 0; i <= n; i++) {
         float f = (float)i / n;
         double an = f * Math.PI * 2.0 * turns + rot;
         float R = 10.4F - 1.6F * f + 0.15F * Cos3Geo.sin(f * 60.0F);
         w[i] = new float[]{Cos3Geo.cos(an) * R, y0 + (y1 - y0) * f - 0.25F * Math.abs(Cos3Geo.sin(f * Math.PI * 2.0 * turns * 3.5)), Cos3Geo.sin(an) * R};
      }

      g.color(-14796254);
      float[] rr = new float[n + 1];
      Arrays.fill(rr, 0.11F);
      Geo.chain(g, "c3_metal", w, rr, 3);
      int nb = 22;

      for (int i = 0; i < nb; i++) {
         float f = (i + 0.5F) / nb;
         double an = f * Math.PI * 2.0 * turns + rot;
         float R = 10.4F - 1.6F * f;
         float y = y0 + (y1 - y0) * f - 0.3F;
         float wave = 0.5F + 0.5F * Cos3Geo.sin(i * 0.9F - t * 4.5F);
         float lit = 0.3F + 0.7F * Cos3Geo.smooth(wave * 1.5F - 0.25F);
         g.push();
         g.translate(Cos3Geo.cos(an) * R, y, Cos3Geo.sin(an) * R);
         g.rotY((float)(-Math.toDegrees(an)));
         g.rotZ(-150.0F + Cos3Geo.sin(t * 2.0F + i) * 8.0F);
         XmasGeo.bulbLite(g, 1.05F, XmasGeo.LIGHTS[i % XmasGeo.LIGHTS.length], lit);
         g.pop();
      }

      g.color(-1);
   }

   static void auroraBand(G g, Cos.A a) {
      float t = a.time;

      for (int k = 0; k < 2; k++) {
         int n = 72;
         float[][] top = new float[n + 1][];
         float[][] bot = new float[n + 1][];
         double spin = t * (k == 0 ? 0.25 : -0.18);

         for (int i = 0; i <= n; i++) {
            double th = (Math.PI * 2) * i / n;
            float R = (k == 0 ? 10.8F : 12.0F) + 0.9F * Cos3Geo.sin(th * 3.0 + t * 0.7F + k) + 0.35F * Cos3Geo.sin(th * 17.0 + t * 1.3F);
            float yb = k == 0 ? 1.8F + 1.5F * Cos3Geo.sin(th * 2.0 - t * 0.6F) : -15.0F + 1.8F * Cos3Geo.sin(th * 2.0 + t * 0.5F + 1.0);
            float hh = (k == 0 ? 6.5F : 5.0F) + 1.8F * Cos3Geo.sin(th * 4.0 + t * 0.9F + k * 2);
            double a0 = th + spin;
            float cx = Cos3Geo.cos(a0);
            float cz = Cos3Geo.sin(a0);
            bot[i] = new float[]{cx * R, yb, cz * R};
            top[i] = new float[]{cx * (R + 0.9F), yb + hh, cz * (R + 0.9F)};
         }

         g.glow(true).color(k == 0 ? -251658241 : -922746881);

         for (int i = 0; i < n; i++) {
            float ua = (float)i / n * 2.0F;
            float ub = (float)(i + 1) / n * 2.0F;
            g.quad("xm_aurora", top[i], top[i + 1], bot[i + 1], bot[i], new float[]{ua, 0.0F, ub, 0.0F, ub, 1.0F, ua, 1.0F});
         }
      }

      g.glow(false);

      for (int i = 0; i < 10; i++) {
         float ph = Cos3Geo.fract(t * 0.3F + i * 0.1F);
         double an = i * 0.63 + t * 0.2;
         float tw = Cos3Geo.sin(Math.PI * ph);
         Cos3Models.sparkle(
            g,
            Cos3Geo.cos(an) * 11.5F,
            5.0F + (float)Capes.hash1(i, 531) * 7.0F - 20 * (i % 2),
            Cos3Geo.sin(an) * 11.5F,
            0.3F,
            Cos3Geo.alpha(tw, i % 3 == 0 ? 12124120 : 15266047)
         );
      }

      g.color(-1);
   }

   static void iceWings(G g, Cos.A a) {
      float t = a.time;
      float mv = mv(a);
      float flap = Cos3Geo.sin(t * 1.5F) * 6.0F * (1.0F - mv) + Cos3Geo.sin(t * 6.0F) * 14.0F * mv;

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 1.0F, -2.4F, -3.4F);
         g.scale(s, 1.0F, 1.0F);
         g.rotY(a.sneak ? 50.0F : 22.0F);
         g.rotZ(flap);
         g.scale(1.3F);
         int n = 8;
         float[][] tips = new float[n][];

         for (int i = 0; i < n; i++) {
            float an = 80 - i * 17;
            float L = 4.5F + 7.5F * Cos3Geo.sin(Math.PI * (i + 0.6) / (n + 0.2));
            double ar = Math.toRadians(an);
            float lift = -40.0F * Math.max(0.0F, (float)Math.sin(ar));
            g.push();
            g.rotX(lift);
            g.translate((float)Math.cos(ar) * 0.9F, (float)Math.sin(ar) * 0.9F, 0.0F);
            g.rotZ(an - 90.0F);
            g.scale(1.0F, 1.0F, 0.5F);
            g.glow(true).color(Cos3Geo.argb(215, Capes.mix(15268095, 9097471, (float)i / n)));
            Cos3Geo.shard(g, "c3_crystal", 0.62F + 0.14F * Cos3Geo.sin(i * 1.3F), L);
            if (i % 2 == 1) {
               g.push();
               g.translate(0.0F, L * 0.45F, 0.0F);
               g.rotZ(28.0F);
               g.color(Cos3Geo.argb(200, 13167871));
               Cos3Geo.shard(g, "c3_crystal", 0.28F, L * 0.32F);
               g.pop();
            }

            g.pop();
            float ca = (float)Math.cos(ar);
            float sa = (float)Math.sin(ar);
            float rr = 0.9F + L + 0.6F;
            double lr = Math.toRadians(lift);
            tips[i] = new float[]{ca * rr, sa * rr * (float)Math.cos(lr), sa * rr * (float)Math.sin(lr)};
         }

         g.glow(false);

         for (int i = 0; i < n; i += 3) {
            float[] p = tips[i];
            g.push();
            g.translate(p[0], p[1] + Cos3Geo.sin(t * 2.0F + i) * 0.3F, p[2]);
            g.rotY(t * 60.0F + i * 30);
            g.glow(true).color(-251658241);
            XmasGeo.flake(g, "white", 0.85F, 0.06F);
            g.glow(false);
            g.pop();
         }

         int m = 5;
         float[][] str = new float[m * 6 + 1][];
         int k = 0;

         for (int i = 0; i < m; i++) {
            float[] p0 = tips[i];
            float[] p1 = tips[i + 1];
            float[] q0 = lerp3(new float[]{0.0F, 0.0F, 0.0F}, p0, 0.72F);
            float[] q1 = lerp3(new float[]{0.0F, 0.0F, 0.0F}, p1, 0.72F);

            for (int j = 0; j < 6; j++) {
               float f = j / 6.0F;
               float sag = 4.0F * f * (1.0F - f) * 0.9F;
               str[k++] = new float[]{q0[0] + (q1[0] - q0[0]) * f, q0[1] + (q1[1] - q0[1]) * f - sag, q0[2] + (q1[2] - q0[2]) * f - 0.25F};
            }

            if (i == m - 1) {
               str[k++] = new float[]{q1[0], q1[1], q1[2] - 0.25F};
            }
         }

         XmasGeo.wire(g, Arrays.copyOf(str, k), 0.07F);

         for (int i = 1; i < k; i += 4) {
            float[] q = str[i];
            XmasGeo.fairy(g, q[0], q[1] - 0.25F, q[2], 0.22F, XmasGeo.LIGHTS[i / 3 % XmasGeo.LIGHTS.length], XmasGeo.twinkle(t, i + s * 7));
         }

         Cos3Geo.halo(g, 0.0F, 0.0F, 0.0F, 2.0F, 1891166463);
         g.pop();
      }

      for (int i = 0; i < 6; i++) {
         float ph = Cos3Geo.fract(t * 0.35F + i / 6.0F);
         Cos3Models.sparkle(
            g,
            (i % 2 == 0 ? 1 : -1) * (4.0F + 6.0F * (float)Capes.hash1(i, 541)),
            2.0F - ph * 10.0F,
            -5.0F - 2.0F * (float)Capes.hash1(i, 542),
            0.28F,
            Cos3Geo.alpha(1.0F - ph, 15268095)
         );
      }

      g.color(-1);
   }

   static float[] lerp3(float[] a, float[] b, float f) {
      return new float[]{a[0] + (b[0] - a[0]) * f, a[1] + (b[1] - a[1]) * f, a[2] + (b[2] - a[2]) * f};
   }

   static void starWings(G g, Cos.A a) {
      Wings.feathered(g, a, "gold", 16.5F, false, 5);
      Wings.Pose p = Wings.pose(a, 1.0F);
      float S = 16.5F;
      float spanK = 1.0F - 0.18F * p.fold;
      float t = a.time;

      for (int side = 1; side >= -1; side -= 2) {
         g.push();
         g.scale(side, 1.0F, 1.0F);
         g.translate(0.9F, -2.2F, -2.35F);
         g.rotY(p.sweep);
         g.rotZ(p.raise);
         g.rotX(-8.0F);
         float[] sp = new float[]{0.3F, 0.52F, 0.72F, 0.9F};

         for (int i = 0; i < sp.length; i++) {
            float s = sp[i];
            float x = S * s * spanK;
            float y = S * (0.26F * Cos3Geo.sin(Math.PI * s * 0.92) + 0.1F * s);
            float z = -S * 0.09F * Cos3Geo.sin(Math.PI * s) - 1.3F;
            float len = 2.2F + i % 2 * 1.8F + s * 1.5F;
            float sw = Cos3Geo.sin(t * 2.1F + i * 1.3F) * 10.0F;
            g.push();
            g.translate(x, y - 0.2F, z);
            g.rotX(-p.raise * 0.0F);
            g.rotZ(-p.raise + sw);
            g.color(-5720);
            Geo.tube(g, "white", new float[]{0.0F, 0.0F, 0.0F}, new float[]{0.0F, -len, 0.0F}, 0.05F, 0.05F, 3);
            g.translate(0.0F, -len - 0.85F, 0.0F);
            g.rotY(t * 50.0F + i * 60);
            g.glow(true).color(i % 2 == 0 ? -10166 : -3920);
            XmasGeo.star(g, "xm_gold", 0.95F, 0.32F, 5);
            g.glow(false);
            Cos3Geo.halo(g, 0.0F, 0.0F, 0.0F, 2.6F, Cos3Geo.alpha(0.35 + 0.2 * Cos3Geo.sin(t * 3.0F + i), 16769146));
            g.pop();
         }

         g.pop();
      }

      g.color(-1);
   }
}
