package dev.lego.cosmetic;

import java.util.Arrays;

final class Cos3Back {
   private Cos3Back() {
   }

   static void register() {
      Cos.add("back_c3_minime", "MiniMe-Anhänger", Cos.Slot.BACK, Cos.Rarity.LEGENDARY, (g, a) -> miniMe(g, a, false));
      Cos.add("back_c3_minime_chain", "MiniMe-Kette", Cos.Slot.BACK, Cos.Rarity.LEGENDARY, (g, a) -> miniMe(g, a, true));
      Cos.add("back_c3_foxtail", "Fuchsschwanz", Cos.Slot.BACK, Cos.Rarity.EPIC, Cos3Back::foxTail);
      Cos.add("back_c3_dragontail", "Drachenschwanz", Cos.Slot.BACK, Cos.Rarity.LEGENDARY, Cos3Back::dragonTail);
      Cos.add("back_c3_parrot", "Schulterpapagei", Cos.Slot.BACK, Cos.Rarity.EPIC, Cos3Back::parrot);
      Cos.add("back_c3_shoulderdragon", "Schulterdrache", Cos.Slot.BACK, Cos.Rarity.LEGENDARY, Cos3Back::shoulderDragon);
      Cos.add("back_c3_katana", "Katana", Cos.Slot.BACK, Cos.Rarity.EPIC, Cos3Back::katana);
      Cos.add("back_c3_jetpack2", "Jetpack V2", Cos.Slot.BACK, Cos.Rarity.LEGENDARY, Cos3Back::jetpack2);
      Cos.add("back_c3_lantern", "Gürtellaterne", Cos.Slot.BACK, Cos.Rarity.RARE, Cos3Back::lantern);
   }

   static float mv(Cos.A a) {
      return a.preview ? 0.0F : Cos3Geo.clamp(a.move, 0.0F, 1.0F);
   }

   static void miniMe(G g, Cos.A a, boolean chainVariant) {
      float t = a.time;
      float mv = mv(a);
      float[] pivot;
      if (!chainVariant) {
         g.color(-1);
         Cos3Geo.bbox(g, "strap", 0.5F, -5.3F, -2.7F, 2.7F, -3.3F, -2.25F, 0.18F);
         g.color(-11414);
         Cos3Geo.ball(g, "c3_metal", 1.6F, -4.3F, -2.72F, 0.3F, 8);
         Geo.tube(g, "c3_metal", new float[]{1.6F, -4.3F, -2.7F}, new float[]{1.6F, -4.7F, -3.7F}, 0.14F, 0.14F, 6);
         Cos3Geo.ball(g, "c3_metal", 1.6F, -4.7F, -3.7F, 0.18F, 6);
         pivot = new float[]{1.6F, -4.75F, -3.7F};
      } else {
         float[] l = new float[]{-3.1F, -1.3F, -2.85F};
         float[] r = new float[]{3.1F, -1.3F, -2.85F};

         for (float[] s : new float[][]{l, r}) {
            g.color(-14013900);
            Cos3Geo.bbox(g, "c3_metal", s[0] - 0.55F, s[1] - 0.55F, -2.95F, s[0] + 0.55F, s[1] + 0.55F, -2.25F, 0.15F);
            g.glow(true).color(-9770753);
            g.push();
            g.translate(s[0], s[1], -3.0F);
            g.rotX(90.0F);
            Cos3Geo.gem(g, "c3_crystal", 0.38F, 0.3F, 0.1F, 6);
            g.pop();
            g.glow(false);
         }

         g.color(-11414);
         float sagD = 2.2F + Cos3Geo.sin(t * 1.3F) * 0.08F;
         Cos3Geo.chainPath(g, "c3_metal", Cos3Geo.sag(l, r, sagD, 8), 0.55F);
         float[] c = new float[]{0.0F, -1.3F - sagD, -2.85F};
         Geo.tube(g, "c3_metal", c, new float[]{0.0F, c[1] - 0.5F, -3.7F}, 0.13F, 0.13F, 6);
         g.glow(true).color(-9770753);
         g.push();
         g.translate(0.0F, c[1] - 0.5F, -3.7F);
         Cos3Geo.gem(g, "c3_crystal", 0.35F, 0.35F, 0.35F, 6);
         g.pop();
         g.glow(false).color(-11414);
         pivot = new float[]{0.0F, c[1] - 0.85F, -3.7F};
      }

      float sw = 5.0F + 5.0F * Cos3Geo.sin(t * 2.2F) + mv * 28.0F;
      float side = Cos3Geo.sin(t * 1.7F) * 6.0F;
      g.push();
      g.translate(pivot[0], pivot[1], pivot[2]);
      g.rotX(sw);
      g.rotZ(side);
      g.color(-11414);
      float drop = chainVariant ? 1.6F : 1.2F;
      Cos3Geo.chainLinks(g, "c3_metal", new float[]{0.0F, 0.0F, 0.0F}, new float[]{0.0F, -drop, 0.0F}, 0.45F);
      g.push();
      g.translate(0.0F, -drop - 0.15F, 0.0F);
      g.rotX(90.0F);
      g.torus("c3_metal", 0.28F, 0.08F, 10, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.translate(0.0F, -drop - 0.35F - 2.13F, 0.0F);
      g.scale(0.25F);
      g.rotY(180.0F);
      float wave = Cos3Geo.sin(t * 2.6F);
      Cos3Geo.miniPlayer(
         g,
         "@skin",
         Cos3Geo.sin(t * 0.9F) * 18.0F,
         Cos3Geo.sin(t * 1.3F) * 6.0F,
         14.0F * wave + mv * 20.0F,
         -14.0F * wave - mv * 20.0F,
         8.0F,
         8.0F,
         12.0F * wave
      );
      g.pop();
      g.color(-1);
   }

   static float[][] tailPath(float[] start, int n, float segLen, float p0, float p1, float pw, float yawAmp, float yawPh, float pitchWave, float t) {
      float[][] pts = new float[n][];
      pts[0] = (float[])start.clone();

      for (int i = 1; i < n; i++) {
         float s = (float)i / (n - 1);
         double p = Math.toRadians(p0 + (p1 - p0) * Math.pow(s, pw) + pitchWave * Cos3Geo.sin(t * 1.8F - s * 4.0F) * s);
         double w = Math.toRadians(yawAmp * s * Cos3Geo.sin(yawPh - s * 2.2F));
         float dx = (float)(Math.sin(w) * Math.cos(p));
         float dy = (float)Math.sin(p);
         float dz = (float)(-Math.cos(w) * Math.cos(p));
         float[] q = pts[i - 1];
         pts[i] = new float[]{q[0] + dx * segLen, q[1] + dy * segLen, q[2] + dz * segLen};
      }

      return pts;
   }

   static void foxTail(G g, Cos.A a) {
      float t = a.time;
      float mv = mv(a);
      float yaw = 16.0F * (1.0F - 0.5F * mv) + 10.0F * mv;
      float ph = t * (2.2F + mv * 5.0F);
      int n = 13;
      float[][] pts = tailPath(new float[]{0.0F, -10.2F, -3.3F}, n, 1.1F, -45.0F, 100.0F, 1.6F, yaw * 2.0F, ph, 4.0F, t);
      float[] rad = new float[n];

      for (int i = 0; i < n; i++) {
         float s = (float)i / (n - 1);
         rad[i] = (0.75F + 1.75F * (float)Math.pow(Math.sin(Math.PI * Math.pow(s, 0.8) * 0.92), 0.8)) * (1.0F - (float)Math.pow(s, 5.0) * 0.88F);
      }

      int split = 9;
      float[][] a1 = Arrays.copyOfRange(pts, 0, split + 1);
      float[][] a2 = Arrays.copyOfRange(pts, split, n);
      float[] r1 = Arrays.copyOfRange(rad, 0, split + 1);
      float[] r2 = Arrays.copyOfRange(rad, split, n);
      g.color(-1542626);
      Geo.chain(g, "c3_fur", a1, r1, 12);
      Cos3Geo.ball(g, "c3_fur", pts[0][0], pts[0][1], pts[0][2], rad[0], 10);
      g.color(-264466);
      Geo.chain(g, "c3_fur", a2, r2, 12);

      for (int i = 2; i < n - 1; i += 2) {
         float s = (float)i / (n - 1);
         g.color(s > 0.7F ? -264466 : -1014230);
         float[] p = pts[i];
         float ang = i * 1.9F;
         Cos3Geo.ball(g, "c3_fur", p[0] + Cos3Geo.cos(ang) * rad[i] * 0.55F, p[1] + Cos3Geo.sin(ang) * rad[i] * 0.4F, p[2], rad[i] * 0.62F, 8);
      }

      g.color(-1);
   }

   static void dragonTail(G g, Cos.A a) {
      float t = a.time;
      float mv = mv(a);
      int n = 17;
      float[][] pts = tailPath(new float[]{0.0F, -10.0F, -3.8F}, n, 1.05F, -55.0F, -15.0F, 1.0F, 30.0F * (1.0F + mv), t * (1.5F + mv * 3.0F), 10.0F, t);
      float[] rad = new float[n];

      for (int i = 0; i < n; i++) {
         float s = (float)i / (n - 1);
         rad[i] = 1.25F * (float)Math.pow(1.0F - s, 0.9) + 0.12F;
      }

      g.color(-11915654);
      Geo.chain(g, "c3_scale", pts, rad, 10);
      Cos3Geo.ball(g, "c3_scale", pts[0][0], pts[0][1], pts[0][2], rad[0], 10);
      g.color(-1521526);

      for (int i = 1; i < n - 2; i++) {
         float[] p = pts[i];
         float[] q = pts[i + 1];
         float dx = q[0] - p[0];
         float dy = q[1] - p[1];
         float dz = q[2] - p[2];
         float ux = 0.0F;
         float uy = -dz;
         float ul = (float)Math.sqrt(uy * uy + dy * dy);
         uy /= ul;
         float uz = dy / ul;
         g.push();
         g.translate((p[0] + q[0]) / 2.0F + ux, (p[1] + q[1]) / 2.0F - uy * rad[i] * 0.72F, (p[2] + q[2]) / 2.0F - uz * rad[i] * 0.72F);
         Cos3Geo.alignY(g, dx, dy, dz);
         float w = rad[i] * 1.1F;
         Cos3Geo.bbox(g, "c3_metal", -w / 2.0F, -0.45F, -rad[i] * 0.42F, w / 2.0F, 0.45F, rad[i] * 0.42F, 0.12F);
         g.pop();
      }

      for (int i = 1; i < n - 1; i += 2) {
         float s = (float)i / (n - 1);
         float[] p = pts[i];
         float[] q = pts[i + 1];
         float dx = q[0] - p[0];
         float dy = q[1] - p[1];
         float dz = q[2] - p[2];
         float uy = -dz;
         float ul = (float)Math.sqrt(uy * uy + dy * dy);
         uy /= ul;
         float var32 = dy / ul;
         g.push();
         g.translate(p[0], p[1] + uy * rad[i] * 0.8F, p[2] + var32 * rad[i] * 0.8F);
         Cos3Geo.alignY(g, 0.0F, uy - dy * 0.5F, var32 - dz * 0.5F);
         float pul = 0.7F + 0.3F * Cos3Geo.sin(t * 3.0F - i * 0.5F);
         g.glow(true).color(Cos3Geo.alpha(pul, 12610303));
         Cos3Geo.shard(g, "c3_crystal", 0.22F + 0.28F * (1.0F - s), 0.6F + 0.9F * (1.0F - s));
         g.glow(false);
         g.pop();
      }

      float[] e = pts[n - 1];
      float[] d = pts[n - 2];
      g.push();
      g.translate(e[0], e[1], e[2]);
      Cos3Geo.alignY(g, e[0] - d[0], e[1] - d[1], e[2] - d[2]);
      g.translate(0.0F, -0.3F, 0.0F);
      g.glow(true).color(-4166913);
      Cos3Geo.extrude(g, "c3_crystal", new float[][]{{0.0F, -0.2F}, {1.0F, 0.55F}, {0.35F, 0.9F}, {0.0F, 2.2F}, {-0.35F, 0.9F}, {-1.0F, 0.55F}}, -0.14F, 0.14F);
      g.glow(false);
      g.pop();
      g.color(-1);
   }

   static void parrot(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(6.2F, 1.0F, 0.2F);
      g.rotY(-12.0F);
      float hop = Math.max(0.0F, Cos3Geo.sin(t * 0.8F)) > 0.97F ? (Cos3Geo.sin(t * 0.8F) - 0.97F) * 12.0F : 0.0F;
      g.translate(0.0F, hop, 0.0F);
      g.color(-11908526);

      for (int s = -1; s <= 1; s += 2) {
         Cos3Geo.bbox(g, "white", s * 0.45F - 0.17F, 0.0F, -0.35F, s * 0.45F + 0.17F, 0.28F, 0.75F, 0.1F);
         Geo.tube(g, "white", new float[]{s * 0.45F, 0.25F, 0.15F}, new float[]{s * 0.4F, 1.0F, 0.0F}, 0.15F, 0.13F, 5);
      }

      g.push();
      g.translate(0.0F, 1.4F, -1.1F);
      g.rotY(90.0F);

      for (int k = -1; k <= 1; k += 2) {
         g.push();
         g.translate(0.0F, 0.0F, k * 0.12F);
         g.rotZ(-42 + k * 4 + Cos3Geo.sin(t * 1.3F) * 3.0F);
         g.color(-1);
         Cos3Geo.extrude(
            g, "c3_parrotwing", new float[][]{{0.0F, 0.25F}, {2.4F, 0.35F}, {4.4F, 0.1F}, {4.6F, -0.1F}, {2.4F, -0.35F}, {0.0F, -0.3F}}, -0.06F, 0.06F
         );
         g.pop();
      }

      g.pop();
      g.color(-15026022);
      Geo.ellipsoid(g, "c3_plume", 0.0F, 2.0F, -0.1F, 1.05F, 1.55F, 1.15F, 14, 8);
      g.color(-11718);
      Geo.ellipsoid(g, "c3_plume", 0.0F, 1.75F, 0.35F, 0.8F, 1.1F, 0.9F, 12, 7);
      float fl = (float)Math.pow(Math.max(0.0F, Cos3Geo.sin(t * 1.1F)), 12.0) * 38.0F * (0.6F + 0.4F * Cos3Geo.sin(t * 18.0F));

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 0.95F, 2.9F, 0.2F);
         g.rotZ(s * (6.0F + (s < 0 ? fl * 0.3F : fl)));
         g.rotY(90.0F);
         g.rotZ(-12.0F);
         g.color(-1);
         Cos3Geo.extrude(
            g,
            "c3_parrotwing",
            new float[][]{{-0.6F, 0.3F}, {0.4F, 0.6F}, {1.6F, 0.2F}, {2.9F, -1.3F}, {2.5F, -1.7F}, {1.0F, -1.5F}, {-0.4F, -0.7F}},
            -0.12F,
            0.12F
         );
         g.pop();
      }

      g.push();
      g.translate(0.0F, 3.75F, 0.35F);
      g.rotY(Cos3Geo.sin(t * 0.9F) * 28.0F);
      g.rotZ(Cos3Geo.sin(t * 0.6F + 1.0F) * 10.0F);
      g.color(-14497624);
      g.sphere("c3_plume", 0.95F, 14, 8, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int s = -1; s <= 1; s += 2) {
         g.color(-461586);
         Geo.ellipsoid(g, "white", s * 0.52F, 0.02F, 0.48F, 0.42F, 0.48F, 0.4F, 10, 6);
         boolean blink = Cos3Geo.fract(t * 0.3F) > 0.96F;
         g.color(-15461352);
         Geo.ellipsoid(g, "white", s * 0.78F, 0.22F, 0.5F, 0.2F, blink ? 0.04F : 0.22F, 0.2F, 8, 5);
         if (!blink) {
            g.color(-1);
            Cos3Geo.ball(g, "white", s * 0.9F, 0.3F, 0.58F, 0.07F, 4);
         }
      }

      g.color(-20422);
      Cos3Geo.limb(g, "c3_metal", new float[][]{{0.0F, 0.05F, 0.8F}, {0.0F, -0.1F, 1.25F}, {0.0F, -0.45F, 1.38F}}, new float[]{0.36F, 0.24F, 0.07F}, 8);
      g.color(-3637222);
      Cos3Geo.limb(g, "c3_metal", new float[][]{{0.0F, -0.35F, 0.75F}, {0.0F, -0.5F, 1.02F}}, new float[]{0.26F, 0.1F}, 7);
      float cr = 10.0F + 12.0F * (float)Math.pow(Math.max(0.0F, Cos3Geo.sin(t * 0.7F)), 6.0);

      for (int k = 0; k < 3; k++) {
         g.push();
         g.translate(0.0F, 0.8F, -0.05F - k * 0.2F);
         g.rotY(90.0F);
         g.rotZ(95 + k * 22 - cr);
         g.color(k == 1 ? -34262 : -46534);
         Cos3Geo.extrude(g, "c3_plume", new float[][]{{0.0F, 0.18F}, {0.9F, 0.22F}, {1.5F, 0.0F}, {0.9F, -0.2F}, {0.0F, -0.15F}}, -0.07F, 0.07F);
         g.pop();
      }

      g.pop();
      g.pop();
      g.color(-1);
   }

   static void shoulderDragon(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(-6.1F, 0.95F, 0.0F);
      g.rotY(10.0F);
      int body = -9815384;
      int belly = -862048;
      g.color(body);

      for (int s = -1; s <= 1; s += 2) {
         for (int f = 0; f < 2; f++) {
            float z = f == 0 ? 0.8F : -1.0F;
            Cos3Geo.limb(
               g, "c3_scale", new float[][]{{s * 0.55F, 1.2F, z}, {s * 0.7F, 0.5F, z + 0.1F}, {s * 0.62F, 0.15F, z + 0.3F}}, new float[]{0.3F, 0.2F, 0.17F}, 6
            );
         }
      }

      Geo.ellipsoid(g, "c3_scale", 0.0F, 1.5F, -0.2F, 0.95F, 1.0F, 1.55F, 14, 8);
      g.color(belly);
      Geo.ellipsoid(g, "c3_metal", 0.0F, 1.22F, 0.05F, 0.72F, 0.72F, 1.25F, 12, 6);
      g.color(body);
      float sw = Cos3Geo.sin(t * 1.4F) * 0.4F;
      Cos3Geo.limb(
         g,
         "c3_scale",
         new float[][]{{0.0F, 1.4F, -1.5F}, {0.1F + sw * 0.3F, 1.0F, -2.4F}, {0.4F + sw, 0.3F, -2.9F}, {0.9F + sw, -0.5F, -2.85F}, {1.2F + sw, -1.0F, -2.5F}},
         new float[]{0.55F, 0.42F, 0.3F, 0.18F, 0.08F},
         7
      );
      float bob = Cos3Geo.sin(t * 1.6F) * 0.12F;
      Cos3Geo.limb(g, "c3_scale", new float[][]{{0.0F, 1.9F, 0.9F}, {0.0F, 2.7F, 1.35F}, {0.0F, 3.3F + bob, 1.65F}}, new float[]{0.58F, 0.46F, 0.42F}, 8);

      for (int i = 0; i < 4; i++) {
         g.push();
         g.translate(0.0F, 2.35F - i * 0.12F, 0.5F - i * 0.6F);
         g.rotX(-20.0F);
         g.glow(true).color(-20422);
         Cos3Geo.shard(g, "c3_crystal", 0.2F, 0.55F);
         g.glow(false);
         g.pop();
      }

      float flap = Cos3Geo.sin(t * 2.2F) * 12.0F + (float)Math.pow(Math.max(0.0F, Cos3Geo.sin(t * 0.7F)), 10.0) * 35.0F;

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 0.65F, 2.2F, -0.1F);
         g.scale(s, 1.0F, 1.0F);
         if (s > 0) {
            g.rotZ(14.0F + flap * 0.3F);
            g.rotY(62.0F);
         } else {
            g.rotZ(38.0F + flap);
            g.rotY(22.0F);
         }

         g.rotX(-90.0F);
         g.color(-660972824);
         float[][] mem = new float[][]{
            {0.0F, 0.0F}, {1.5F, -0.25F}, {3.3F, 0.3F}, {2.6F, 1.05F}, {2.3F, 2.0F}, {1.65F, 1.5F}, {1.0F, 2.2F}, {0.6F, 1.4F}, {0.0F, 1.1F}
         };
         Cos3Geo.extrude(g, "c3_membrane", mem, -0.05F, 0.05F);
         g.color(body);
         Geo.chain(g, "c3_scale", new float[][]{{0.0F, 0.0F, 0.0F}, {1.5F, -0.25F, 0.0F}, {3.3F, 0.3F, 0.0F}}, new float[]{0.2F, 0.15F, 0.07F}, 5);
         Geo.tube(g, "c3_scale", new float[]{1.5F, -0.25F, 0.0F}, new float[]{2.3F, 2.0F, 0.0F}, 0.1F, 0.05F, 4);
         Geo.tube(g, "c3_scale", new float[]{1.5F, -0.25F, 0.0F}, new float[]{1.0F, 2.2F, 0.0F}, 0.1F, 0.05F, 4);
         g.pop();
      }

      g.push();
      g.translate(0.0F, 3.55F + bob, 1.85F);
      g.rotY(Cos3Geo.sin(t * 0.8F) * 25.0F);
      g.rotX(-8.0F + Cos3Geo.sin(t * 1.1F) * 5.0F);
      g.color(body);
      Cos3Geo.bbox(g, "c3_scale", -0.72F, -0.55F, -0.7F, 0.72F, 0.62F, 0.72F, 0.32F);
      Cos3Geo.bbox(g, "c3_scale", -0.48F, -0.5F, 0.5F, 0.48F, 0.18F, 1.65F, 0.2F);
      g.color(belly);
      Cos3Geo.bbox(g, "c3_metal", -0.4F, -0.62F, 0.3F, 0.4F, -0.3F, 1.55F, 0.12F);

      for (int s = -1; s <= 1; s += 2) {
         g.glow(true).color(-8118);
         Geo.ellipsoid(g, "white", s * 0.7F, 0.18F, 0.35F, 0.12F, 0.16F, 0.2F, 6, 4);
         g.glow(false).color(-857904);
         g.push();
         g.translate(s * 0.38F, 0.5F, -0.35F);
         g.rotX(-118.0F);
         g.rotZ(s * 12);
         Cos3Geo.shard(g, "c3_metal", 0.18F, 1.25F);
         g.pop();
      }

      float br = Cos3Geo.fract(t / 5.0F);
      if (br < 0.3F) {
         for (int i = 0; i < 4; i++) {
            float f = Cos3Geo.fract(br / 0.3F + i * 0.25F);
            Cos3Geo.halo(
               g,
               Cos3Geo.sin(i * 2.1F) * 0.2F * f,
               -0.2F + f * 0.3F,
               1.9F + f * 2.4F,
               0.6F + f * 1.2F,
               Cos3Geo.alpha((1.0F - f) * 0.9, f < 0.3F ? 16769146 : 16738842)
            );
         }
      }

      g.pop();
      g.pop();
      g.color(-1);
   }

   static float curve(float y) {
      return 0.012F * (y + 1.5F) * (y + 1.5F) - 0.2F;
   }

   static void katana(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(0.0F, -5.6F, -2.42F);
      g.rotZ(52.0F);
      g.color(-1);
      Cos3Geo.bbox(g, "strap", -0.6F, -5.3F, -0.16F, 0.6F, 5.3F, 0.16F, 0.1F);
      g.pop();
      g.push();
      g.translate(1.0F, -6.0F, -3.35F);
      g.rotZ(-38.0F);
      int n = 14;
      float y0 = -7.5F;
      float y1 = 3.9F;
      float[][] poly = new float[n * 2 + 2][];

      for (int i = 0; i <= n; i++) {
         float y = y0 + (y1 - y0) * i / n;
         float w = 0.48F * (i == 0 ? 0.55F : 1.0F);
         poly[i] = new float[]{curve(y) + w, y};
         poly[2 * n + 1 - i] = new float[]{curve(y) - w, y};
      }

      g.color(-9826792);
      Cos3Geo.extrude(g, "c3_lacquer", poly, -0.3F, 0.3F, new float[]{0.0F, 0.0F, 1.0F, 1.0F}, new float[]{0.3F, 0.0F, 0.4F, 1.0F});
      g.color(-14246);
      float[] fy = new float[]{3.35F, -1.2F, -2.1F};

      for (float y : fy) {
         Cos3Geo.bbox(g, "c3_metal", curve(y) - 0.6F, y - 0.25F, -0.4F, curve(y) + 0.6F, y + 0.25F, 0.4F, 0.12F);
      }

      Cos3Geo.bbox(g, "c3_metal", curve(y0) - 0.55F, y0 - 0.2F, -0.38F, curve(y0) + 0.55F, y0 + 0.65F, 0.38F, 0.2F);
      g.color(-12965344);
      Cos3Geo.bbox(g, "strap", curve(-1.65F) - 0.35F, -1.95F, 0.2F, curve(-1.65F) + 0.35F, -1.35F, 0.75F, 0.1F);
      float gy = 4.1F;
      g.push();
      g.translate(curve(gy), gy, 0.0F);
      g.scale(1.0F, 1.0F, 0.55F);
      g.color(-14013902);
      Cos3Geo.lathe(g, "c3_metal", new float[]{0.0F, 1.35F, 1.35F, 0.0F}, new float[]{-0.18F, -0.18F, 0.18F, 0.18F}, 16);
      g.color(-14246);
      g.torus("c3_metal", 1.35F, 0.1F, 16, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.color(-14246);
      float hx = curve(gy);
      Cos3Geo.bbox(g, "c3_metal", hx - 0.45F, 4.25F, -0.33F, hx + 0.45F, 4.65F, 0.33F, 0.1F);
      g.color(-1);
      Cos3Geo.bbox(g, "c3_wrap", hx - 0.38F, 4.6F, -0.28F, hx + 0.38F, 8.5F, 0.28F, 0.14F);
      g.color(-14246);
      Cos3Geo.bbox(g, "c3_metal", hx - 0.43F, 8.45F, -0.33F, hx + 0.43F, 8.95F, 0.33F, 0.16F);
      g.push();
      g.translate(hx, 8.95F, 0.0F);
      g.rotZ(38.0F);
      g.rotX(Cos3Geo.sin(t * 1.8F) * 8.0F + mv(a) * 25.0F);
      g.color(-6673688);
      Geo.tube(g, "white", new float[]{0.0F, 0.0F, 0.0F}, new float[]{0.0F, -1.4F, 0.0F}, 0.06F, 0.06F, 4);
      g.glow(true).color(-9770753);
      Cos3Geo.ball(g, "white", 0.0F, -1.55F, 0.0F, 0.28F, 8);
      Cos3Geo.halo(g, 0.0F, -1.55F, 0.0F, 1.6F, -2140477185);
      g.glow(false).color(-6673688);
      Cos3Geo.lathe(g, "c3_fur", new float[]{0.08F, 0.22F, 0.3F, 0.18F}, new float[]{-1.75F, -2.0F, -2.9F, -3.2F}, 8);
      g.pop();
      g.pop();
      g.color(-1);
   }

   static void jetpack2(G g, Cos.A a) {
      float t = a.time;
      float mv = mv(a);
      g.color(-1512206);
      Cos3Geo.bbox(g, "c3_metal", -2.2F, -8.6F, -4.6F, 2.2F, -1.2F, -2.3F, 0.5F);
      g.color(-14012872);
      Cos3Geo.bbox(g, "c3_metal", -1.7F, -1.5F, -4.1F, 1.7F, -0.8F, -2.5F, 0.2F);
      float pul = 0.75F + 0.25F * Cos3Geo.sin(t * 3.0F);
      g.push();
      g.translate(0.0F, -4.6F, -4.62F);
      g.rotX(90.0F);
      g.color(-14012872);
      g.torus("c3_metal", 1.0F, 0.2F, 18, 5, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.glow(true).color(Cos3Geo.alpha(pul, 4905215));
      Geo.ellipsoid(g, "c3_crystal", 0.0F, -4.6F, -4.6F, 0.9F, 0.9F, 0.25F, 14, 6);
      g.color(-11872001);
      Cos3Geo.bbox(g, "white", -0.18F, -8.0F, -4.72F, 0.18F, -5.8F, -4.45F, 0.05F);
      Cos3Geo.bbox(g, "white", -0.18F, -3.4F, -4.72F, 0.18F, -1.8F, -4.45F, 0.05F);
      g.glow(false);

      for (int s = -1; s <= 1; s += 2) {
         float x = s * 2.75F;
         float z = -3.95F;
         g.push();
         g.translate(x, 0.0F, z);
         g.color(-12960182);
         Cos3Geo.lathe(
            g,
            "c3_metal",
            new float[]{0.0F, 1.0F, 1.42F, 1.5F, 1.5F, 1.42F, 1.0F, 0.0F},
            new float[]{-8.8F, -8.62F, -8.1F, -7.6F, -2.4F, -1.9F, -1.35F, -1.15F},
            18
         );
         g.color(-30166);
         g.push();
         g.translate(0.0F, -3.0F, 0.0F);
         g.torus("c3_metal", 1.5F, 0.14F, 18, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
         g.push();
         g.translate(0.0F, -7.0F, 0.0F);
         g.torus("c3_metal", 1.5F, 0.14F, 18, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
         g.color(-14539732);
         Cos3Geo.lathe(g, "c3_metal", new float[]{0.95F, 0.72F, 0.86F, 1.08F, 0.95F}, new float[]{-8.7F, -9.1F, -9.8F, -10.45F, -10.5F}, 14);
         g.glow(true).color(-38374);
         g.push();
         g.translate(0.0F, -10.35F, 0.0F);
         g.torus("white", 0.85F, 0.1F, 14, 3, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
         float len = 1.0F + 0.18F * Cos3Geo.sin(t * 23.0F + s * 2) + 0.1F * Cos3Geo.sin(t * 37.0F + s) + mv * 0.5F;
         g.push();
         g.translate(0.0F, -10.4F, 0.0F);
         g.rotX(180.0F);
         g.scale(1.0F, len, 1.0F);
         Cos3Models.flame(g, 3.0F, 0.78F, -1338336513, -253165569);
         g.pop();
         g.glow(false);
         g.color(-1512206);
         g.push();
         g.translate(s * 1.35F, -8.0F, 0.0F);
         g.scale(s, 1.0F, 1.0F);
         Cos3Geo.extrude(g, "c3_metal", new float[][]{{0.0F, 3.2F}, {0.9F, 0.8F}, {0.9F, -1.2F}, {0.0F, -0.4F}}, -0.14F, 0.14F);
         g.pop();
         g.pop();

         for (int i = 0; i < 4; i++) {
            float ph = Cos3Geo.fract(t * 1.6F + i * 0.25F + (s > 0 ? 0.0F : 0.12F));
            Cos3Geo.halo(
               g,
               x + Cos3Geo.sin(i * 2.3F + t) * 0.3F * ph,
               -12.0F - ph * 5.0F,
               z - ph * 0.8F,
               0.8F + ph * 1.6F,
               Cos3Geo.alpha((1.0F - ph) * 0.55, ph < 0.25F ? 10149887 : 12107984)
            );
         }
      }

      for (int i = 0; i < 3; i++) {
         boolean on = ((int)(t * 3.0F) + i) % 3 != 0;
         g.glow(true).color(on ? (i == 1 ? -46486 : -11862134) : -14670806);
         Cos3Geo.ball(g, "white", -0.9F + i * 0.9F, -1.25F, -3.6F, 0.2F, 5);
      }

      g.glow(false).color(-1);
   }

   static void lantern(G g, Cos.A a) {
      float t = a.time;
      float mv = mv(a);
      g.color(-1);
      Cos3Geo.bbox(g, "strap", -3.3F, -10.4F, -2.62F, -1.5F, -8.9F, -2.25F, 0.15F);
      g.color(-12961214);
      Geo.tube(g, "c3_metal", new float[]{-2.4F, -9.6F, -2.6F}, new float[]{-2.4F, -9.9F, -3.7F}, 0.15F, 0.15F, 6);
      Cos3Geo.ball(g, "c3_metal", -2.4F, -9.9F, -3.7F, 0.2F, 6);
      g.push();
      g.translate(-2.4F, -10.0F, -3.7F);
      g.rotX(7.0F + 5.0F * Cos3Geo.sin(t * 2.0F) + mv * 35.0F);
      g.rotZ(Cos3Geo.sin(t * 1.6F) * 5.0F);
      g.color(-12961214);
      g.push();
      g.translate(0.0F, -0.55F, 0.0F);
      g.rotX(90.0F);
      g.torus("c3_metal", 0.5F, 0.1F, 12, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.push();
      g.translate(0.0F, -1.2F, 0.0F);
      g.rotY(45.0F);
      Cos3Geo.lathe(g, "c3_metal", new float[]{0.0F, 0.3F, 1.62F, 1.62F, 0.0F}, new float[]{0.0F, 0.0F, -0.75F, -0.95F, -0.95F}, 4);
      g.pop();
      Cos3Geo.bbox(g, "c3_metal", -1.1F, -2.35F, -1.1F, 1.1F, -2.05F, 1.1F, 0.08F);
      Cos3Geo.bbox(g, "c3_metal", -1.15F, -5.1F, -1.15F, 1.15F, -4.75F, 1.15F, 0.1F);

      for (int sx = -1; sx <= 1; sx += 2) {
         for (int sz = -1; sz <= 1; sz += 2) {
            Cos3Geo.bbox(g, "c3_metal", sx * 0.95F - 0.14F, -4.8F, sz * 0.95F - 0.14F, sx * 0.95F + 0.14F, -2.3F, sz * 0.95F + 0.14F, 0.05F);
         }
      }

      float fl = 0.85F + 0.1F * Cos3Geo.sin(t * 13.0F) + 0.05F * Cos3Geo.sin(t * 29.0F);
      g.push();
      g.translate(0.0F, -4.75F, 0.0F);
      g.scale(1.0F, fl, 1.0F);
      Cos3Models.flame(g, 1.5F, 0.42F, -788559334, -117444448);
      g.pop();
      g.color(-1);
      g.glow(true);
      g.box("c3_lglass", -0.86F, -4.9F, -0.86F, 0.86F, -2.2F, 0.86F, 0.0F, 0.0F, 1.0F, 1.0F);
      Cos3Geo.halo(g, 0.0F, -3.6F, 0.0F, 2.8F, Cos3Geo.alpha(0.45 * fl, 16756810));
      g.glow(false);
      g.pop();

      for (int i = 0; i < 3; i++) {
         float an = t * (1.2F + i * 0.3F) + i * 2.1F;
         Cos3Models.sparkle(g, -2.4F + Cos3Geo.cos(an) * 2.4F, -13.4F + Cos3Geo.sin(an * 1.7F) * 0.8F, -3.9F + Cos3Geo.sin(an) * 1.2F - 0.6F, 0.2F, -520099718);
      }

      g.color(-1);
   }
}
