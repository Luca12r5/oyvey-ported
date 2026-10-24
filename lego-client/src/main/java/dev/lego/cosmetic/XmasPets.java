package dev.lego.cosmetic;

import java.util.Arrays;

final class XmasPets {
   static final float SLEIGH_HIP = 8.0F;
   private static final float[][] SIDE = new float[][]{
      {-8.4F, 2.5F},
      {9.4F, 2.5F},
      {10.9F, 3.3F},
      {11.9F, 5.0F},
      {12.3F, 7.0F},
      {12.0F, 8.8F},
      {11.1F, 9.7F},
      {10.1F, 9.6F},
      {9.6F, 8.9F},
      {9.9F, 8.2F},
      {10.5F, 8.2F},
      {10.6F, 7.4F},
      {9.8F, 6.6F},
      {7.5F, 6.4F},
      {3.0F, 6.9F},
      {-1.6F, 7.5F},
      {-3.4F, 8.6F},
      {-4.8F, 11.2F},
      {-6.2F, 12.9F},
      {-7.8F, 13.4F},
      {-9.2F, 12.6F},
      {-9.4F, 11.4F},
      {-8.6F, 10.8F},
      {-8.0F, 11.5F},
      {-8.9F, 8.0F},
      {-8.9F, 4.0F}
   };

   private XmasPets() {
   }

   static void register() {
      Cos.add("xm_pet_snowman", "Schneemann", Cos.Slot.PET, Cos.Rarity.EPIC, Pets.grow(XmasPets::snowman, 1.5F, 3.8F));
      Cos.add("xm_pet_reindeer", "Rentier", Cos.Slot.PET, Cos.Rarity.LEGENDARY, XmasPets::reindeer);
      Cos.add("xm_pet_gingerbread", "Lebkuchenmann", Cos.Slot.PET, Cos.Rarity.EPIC, Pets.grow(XmasPets::gingerbread, 1.5F, 3.6F));
      Cos.add("xm_pet_giftmonster", "Geschenk-Monster", Cos.Slot.PET, Cos.Rarity.LEGENDARY, Pets.grow(XmasPets::giftMonster, 1.45F, 4.2F));
      Cos.add("xm_pet_penguin", "Pinguin mit Mütze", Cos.Slot.PET, Cos.Rarity.EPIC, Pets.grow(XmasPets::santaPenguin, 1.55F, 3.4F));
      Cos.add("xm_veh_sleigh", "Weihnachtsschlitten", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, XmasPets::sleigh);
   }

   private static float[] p3(float x, float y, float z) {
      return new float[]{x, y, z};
   }

   static void snowman(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      float[] h = PetRig.hop(p);
      p.y = p.y + h[0] * 1.8F;
      p.stepBob = 0.0F;
      float sq = -h[1] * 0.12F + Cos3Geo.sin(p.t * 2.2F) * 0.015F;
      g.push();
      Pets.root(g, p);
      g.color(-1);
      g.push();
      g.scale(1.0F + sq, 1.0F - sq, 1.0F + sq);
      Geo.ellipsoid(g, "xm_snow", 0.0F, 2.25F, 0.0F, 2.45F, 2.3F, 2.4F, 16, 10);
      g.pop();
      float lag = h[1] * 0.25F;
      g.push();
      g.translate(0.0F, 2.25F * (1.0F - sq) + 0.1F, 0.0F);
      g.rotZ(p.waddle * 0.6F + Cos3Geo.sin(p.t * 1.5F) * 2.0F);
      Geo.ellipsoid(g, "xm_snow", 0.0F, 2.75F - lag * 0.4F, 0.0F, 1.85F, 1.8F, 1.8F, 14, 9);
      g.color(-14013904);

      for (int i = 0; i < 3; i++) {
         double el = 0.55 - i * 0.45;
         Cos3Geo.ball(g, "xm_coal", 0.0F, 2.75F - lag * 0.4F + (float)Math.sin(el) * 1.8F, (float)Math.cos(el) * 1.8F + 0.02F, 0.24F, 6);
      }

      for (int s = -1; s <= 1; s += 2) {
         boolean wave = s > 0 && (p.is("happy") || p.is("excited") || p.is("love"));
         float ang = wave ? 55.0F + Cos3Geo.sin(p.t * 7.0F) * 22.0F : 20.0F + Cos3Geo.sin(p.t * 2.0F + s) * 6.0F + h[2] * 30.0F;
         if (p.is("sad")) {
            ang = -25.0F;
         }

         g.push();
         g.translate(s * 1.6F, 3.25F, 0.0F);
         g.rotZ(s * ang);
         g.scale(s, 1.0F, 1.0F);
         g.color(-9813470);
         Cos3Geo.limb(g, "xm_bark", new float[][]{{0.0F, 0.0F, 0.0F}, {1.4F, 0.1F, 0.0F}, {2.9F, 0.35F, 0.1F}}, new float[]{0.2F, 0.17F, 0.12F}, 5);
         Cos3Geo.limb(g, "xm_bark", new float[][]{{2.2F, 0.25F, 0.05F}, {2.8F, 0.95F, 0.1F}}, new float[]{0.1F, 0.07F}, 4);
         Cos3Geo.limb(g, "xm_bark", new float[][]{{2.5F, 0.3F, 0.05F}, {3.1F, -0.2F, 0.2F}}, new float[]{0.09F, 0.06F}, 4);
         g.pop();
      }

      g.color(-1);
      g.push();
      g.translate(0.0F, 5.55F - lag, 0.0F);
      Pets.headTurn(g, p);
      float hr = 1.5F;
      Geo.ellipsoid(g, "xm_snow", 0.0F, 0.0F, 0.0F, hr, hr * 0.97F, hr, 14, 9);
      float bl = p.closed ? 0.15F : 1.0F;
      g.color(-14803420);

      for (int s = -1; s <= 1; s += 2) {
         double az = s * 0.36 + p.lookX * 0.12;
         double el = 0.28 + p.lookY * 0.08;
         g.push();
         g.translate((float)(Math.sin(az) * Math.cos(el)) * hr, (float)Math.sin(el) * hr, (float)(Math.cos(az) * Math.cos(el)) * hr);
         g.scale(1.0F, bl, 1.0F);
         g.sphere("xm_coal", 0.24F, 6, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      float curve = p.is("sad") ? -0.18F : (p.is("angry") ? -0.06F : 0.2F);

      for (int i = 0; i < 5; i++) {
         double az = (i - 2) * 0.24;
         double el = -0.28 - curve * (1.0 - Math.pow((i - 2) / 2.0, 2.0)) + curve;
         Cos3Geo.ball(
            g,
            "xm_coal",
            (float)(Math.sin(az) * Math.cos(el)) * hr * 0.99F,
            (float)Math.sin(el) * hr * 0.99F,
            (float)(Math.cos(az) * Math.cos(el)) * hr * 0.99F,
            0.13F,
            4
         );
      }

      g.push();
      g.translate(0.0F, 0.02F, hr * 0.92F);
      g.rotX(90.0F);
      g.color(-1);
      Cos3Geo.lathe(g, "xm_carrot", new float[]{0.3F, 0.28F, 0.2F, 0.1F, 0.0F}, new float[]{0.0F, 0.4F, 0.9F, 1.4F, 1.8F}, 8);
      g.pop();
      g.push();
      g.translate(0.0F, hr * 0.84F, -0.1F);
      g.rotZ(-12.0F);
      g.color(-14277074);
      Cos3Geo.lathe(
         g, "xm_silk", new float[]{0.0F, 1.55F, 1.62F, 1.5F, 1.0F, 0.95F, 1.02F, 0.0F}, new float[]{0.0F, 0.0F, 0.12F, 0.22F, 0.25F, 1.8F, 1.9F, 1.9F}, 16
      );
      g.color(-3139030);
      Cos3Models.flatRing(g, "xm_ribbon", 0.9F, 1.03F, 0.35F, 0.75F, 16);
      g.pop();
      g.pop();
      g.push();
      g.translate(0.0F, 4.35F - lag * 0.7F, 0.0F);
      g.color(-1);
      g.torus("xm_scarf", 1.2F, 0.42F, 16, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      g.translate(0.8F, -0.1F, 0.9F);
      Cos3Geo.ribbon(g, "xm_scarf", 0.0F, new float[][]{{0.0F, 0.1F}, {-0.9F, 0.45F}, {-1.8F, 0.55F + h[2] * 0.4F}}, new float[]{0.7F, 0.75F, 0.8F}, 0.18F);
      g.pop();
      g.pop();
      Pets.fx(g, p, 9.6F);
      g.pop();
   }

   static void reindeer(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      PetReal.R4 r = new PetReal.R4();
      r.body = Pets.fur("deer");
      r.head = Pets.head("deer");
      r.muzzle = Pets.col(12092504);
      r.legUp = Pets.col(10118208);
      r.legLo = Pets.col(11565648);
      r.paw = Pets.gl(2760736);
      r.style = "deer";
      r.nose = 14688298;
      r.grazer = true;
      r.L = 6.2F;
      r.legX = 1.05F;
      r.zs = 3.0F;
      r.zh = -3.1F;
      r.rx = 1.8F;
      r.ry = 2.15F;
      r.hipF = 0.95F;
      r.neckLen = 3.0F;
      r.neckAng = 28.0F;
      r.neckR = 0.98F;
      r.skRx = 1.32F;
      r.skRy = 1.3F;
      r.skRz = 1.45F;
      r.snLen = 1.8F;
      r.snR = 0.7F;
      r.snY = -0.32F;
      r.headDown = 14.0F;
      r.r1f = 0.58F;
      r.r2f = 0.3F;
      r.r1h = 0.82F;
      r.r2h = 0.32F;
      r.pawR = 0.4F;
      r.hoof = true;
      r.span = 1.05F;
      r.eyeEl = 0.26F;
      r.babyS = 0.56F;
      r.babyLeg = 0.75F;
      r.babyHead = 1.6F;
      float k = p.gr;
      r.ears = (gg, pp) -> {
         for (int s = -1; s <= 1; s += 2) {
            Pets.earPointy(gg, pp, Pets.col(10118208), Pets.col(16181466), s * 1.0F, 0.8F, -0.45F, s, 0.7F, 1.6F, 62.0F);
         }
      };
      r.headExtra = (gg, pp) -> {
         reindeerAntlers(gg, pp);
         float sf = PetRig.lerp(r.babySnout, 1.0F, pp.gr);
         float sl = r.snLen * sf;
         float sc = r.skRz * 0.42F + sl * 0.5F;
         float srx = r.snR * PetRig.lerp(1.1F, 1.0F, pp.gr);
         float sry = r.snR * 0.8F;
         float srz = sl * 0.55F + r.snR * 0.3F;
         float pul = 0.5F + 0.5F * Cos3Geo.sin(pp.t * 3.4F);
         float nr = srx * 0.52F * (1.0F + 0.08F * pul);
         float ny = r.snY + sry * 0.42F;
         float nz = sc + srz * 0.9F;
         boolean og = gg.glow;
         int oc = gg.color;
         gg.glow(true).color(0xFF000000 | Capes.mix(13111324, 16730698, pul));
         Geo.ellipsoid(gg, "xm_gloss", 0.0F, ny, nz, nr * 1.15F, nr, nr, 10, 6);
         Cos3Geo.halo(gg, 0.0F, ny, nz + 0.2F, 2.4F + pul, Cos3Geo.alpha(0.35 + 0.3 * pul, 16726586));
         gg.glow(og).color(oc);
      };
      r.neckExtra = (gg, pp) -> {
         float nr = r.neckR * PetRig.lerp(1.1F, 1.0F, pp.gr);
         gg.push();
         gg.translate(0.0F, r.neckLen * PetRig.lerp(r.babyNeck, 1.0F, pp.gr) * 0.2F, 0.0F);
         gg.rotX(-10.0F);
         gg.color(-3139030);
         gg.torus("xm_velvet", nr * 1.02F, 0.24F, 14, 5, 0.0F, 0.0F, 1.0F, 1.0F);
         gg.translate(0.0F, -0.15F, nr * 1.02F + 0.2F);
         XmasGeo.bell(gg, 0.55F, Cos3Geo.sin(pp.t * 5.0F) * 18.0F * (0.3F + pp.walkW));
         gg.color(-1);
         gg.pop();
      };
      r.bodyExtra = (gg, pp) -> {
         float bf = PetRig.lerp(r.babyLen, 1.0F, pp.gr);
         float fat = PetRig.lerp(1.18F, 1.0F, pp.gr);
         float cz = (r.zs * bf * 0.45F + r.zh * bf * 0.5F) / 2.0F;
         float rx = r.rx * fat;
         float ry = r.ry * fat;
         gg.color(-3664866);
         Geo.ellipsoid(gg, "xm_velvet", 0.0F, ry * 0.38F, cz, rx * 1.07F, ry * 0.72F, 1.55F * bf, 12, 6);
         gg.color(-11414);
         Geo.ellipsoid(gg, "xm_gold", 0.0F, ry * 0.38F - 0.05F, cz, rx * 1.1F, ry * 0.5F, 1.62F * bf, 12, 3);
         gg.color(-1);
      };
      r.tail = (gg, pp) -> {
         gg.rotX(35.0F + pp.tailLift * 0.5F + (!pp.is("excited") && !pp.is("angry") ? 0 : 40));
         PetGeo.blob(gg, Pets.col(16777215), 0.0F, 0.0F, -0.45F, 0.45F, 0.38F, 0.62F, 6, 3);
      };
      PetReal.real4(g, p, r);
      if (k > 1.0F) {
         g.color(-1);
      }
   }

   static void reindeerAntlers(G g, Pets.P p) {
      float k = p.gr;
      float S = 0.55F + 0.55F * k;
      int oc = g.color;
      g.color(-1);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 0.62F, 1.0F, -0.15F);
         g.scale(s * S, S, S);
         g.rotZ(-18.0F);
         float[][] beam = new float[][]{
            {0.0F, 0.0F, 0.0F}, {0.3F, 1.1F, -0.3F}, {0.9F, 2.2F, -0.8F}, {1.4F, 3.2F, -0.9F}, {1.6F, 4.2F, -0.6F}, {1.4F, 5.0F, -0.2F}
         };
         Cos3Geo.limb(g, "xm_antler", beam, new float[]{0.24F, 0.22F, 0.19F, 0.16F, 0.12F, 0.06F}, 6);
         Cos3Geo.limb(g, "xm_antler", new float[][]{beam[1], {0.2F, 1.8F, 0.6F}, {0.1F, 2.2F, 1.0F}}, new float[]{0.15F, 0.11F, 0.05F}, 5);
         Cos3Geo.limb(g, "xm_antler", new float[][]{beam[2], {0.6F, 3.3F, 0.1F}, {0.5F, 3.9F, 0.4F}}, new float[]{0.14F, 0.1F, 0.05F}, 5);
         Cos3Geo.limb(g, "xm_antler", new float[][]{beam[3], {2.3F, 3.9F, -1.1F}, {2.6F, 4.4F, -1.1F}}, new float[]{0.13F, 0.09F, 0.04F}, 5);
         g.pop();
      }

      g.color(oc);
   }

   static void gingerbread(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      float ww = p.walkW;
      double cyc = (Math.PI * 2) * PetRig.frac(p.t * 1.7F);
      float swing = Cos3Geo.sin(cyc) * 38.0F * ww;
      float bob = Math.abs(Cos3Geo.sin(cyc)) * 0.35F * ww + (p.is("excited") ? Math.abs(Cos3Geo.sin(p.t * 6.8F)) * 0.8F : 0.0F);
      g.push();
      Pets.root(g, p);
      g.translate(0.0F, bob, 0.0F);
      g.rotZ(Cos3Geo.sin(cyc) * 4.0F * ww);
      g.color(-1);
      String tx = "xm_ginger";
      float th = 0.62F;

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 0.95F, 3.3F, 0.0F);
         g.rotX(s * swing - p.sit * 80.0F);
         g.scale(1.0F, 1.0F, th);
         Cos3Geo.limb(g, tx, new float[][]{{0.0F, 0.0F, 0.0F}, {s * 0.12F, -1.5F, 0.0F}, {s * 0.25F, -2.75F, 0.0F}}, new float[]{0.82F, 0.78F, 0.74F}, 8);
         icing(g, s * 0.25F, -2.3F, 0.78F);
         g.pop();
      }

      Geo.ellipsoid(g, tx, 0.0F, 4.9F, 0.0F, 1.95F, 2.15F, 0.78F, 14, 8);
      int[] bc = new int[]{-2088918, -13715382, -2088918};

      for (int i = 0; i < 3; i++) {
         g.color(bc[i]);
         float y = 5.9F - i * 0.95F;
         float zz = 0.78F * (float)Math.sqrt(Math.max(0.05, 1.0 - Math.pow((y - 4.9F) / 2.15F, 2.0)));
         Geo.ellipsoid(g, "xm_gloss", 0.0F, y, zz + 0.05F, 0.34F, 0.3F, 0.2F, 8, 5);
      }

      g.color(-1);

      for (int s = -1; s <= 1; s += 2) {
         boolean wave = s > 0 && (p.is("happy") || p.is("excited")) && ww < 0.3F;
         float raise = wave ? 75.0F + Cos3Geo.sin(p.t * 8.0F) * 25.0F : Cos3Geo.sin(p.t * 2.0F + s) * 5.0F - 5.0F;
         g.push();
         g.translate(s * 1.55F, 6.0F, 0.0F);
         g.rotX(-s * swing * 0.8F);
         g.rotZ(s * raise);
         g.scale(1.0F, 1.0F, th);
         float[][] arm = new float[][]{{0.0F, 0.0F, 0.0F}, {s * 1.2F, -0.55F, 0.0F}, {s * 2.3F, -0.95F, 0.0F}};
         Cos3Geo.limb(g, tx, arm, new float[]{0.72F, 0.68F, 0.64F}, 8);
         icing2(g, s * 1.95F, -0.83F, 0.66F);
         g.pop();
      }

      g.push();
      g.translate(0.0F, 6.85F, 0.62F);
      XmasGeo.bow(g, -2088918, 0.45F);
      g.pop();
      g.push();
      g.translate(0.0F, 8.4F, 0.0F);
      Pets.headTurn(g, p);
      g.color(-1);
      Geo.ellipsoid(g, tx, 0.0F, 0.0F, 0.0F, 1.85F, 1.75F, 0.8F, 14, 8);
      float bl = p.closed ? 0.2F : 1.0F;
      g.color(-1);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 0.62F + p.lookX * 0.12F, 0.35F + p.lookY * 0.1F, 0.7F);
         g.scale(1.0F, bl, 0.5F);
         g.sphere("white", 0.27F, 8, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      float curve = p.is("sad") ? -0.3F : 0.35F;
      int n = 7;
      float[][] m = new float[n][];

      for (int i = 0; i < n; i++) {
         float f = (i - (n - 1) / 2.0F) / ((n - 1) / 2.0F);
         float y = -0.45F - curve * (1.0F - f * f);
         m[i] = p3(f * 0.8F, y, 0.8F * (float)Math.sqrt(Math.max(0.05, 1.0 - Math.pow(f * 0.8 / 1.85, 2.0) - Math.pow(y / 1.75, 2.0))) + 0.04F);
      }

      float[] mr = new float[n];
      Arrays.fill(mr, 0.1F);
      Cos3Geo.limb(g, "white", m, mr, 4);
      g.color(-1013110);

      for (int s = -1; s <= 1; s += 2) {
         Geo.ellipsoid(g, "white", s * 1.1F, -0.25F, 0.62F, 0.28F, 0.2F, 0.12F, 6, 3);
      }

      g.pop();
      Pets.fx(g, p, 10.6F);
      g.pop();
      g.color(-1);
   }

   private static void icing(G g, float x, float y, float r) {
      int n = 12;
      float[][] pts = new float[n + 1][];

      for (int i = 0; i <= n; i++) {
         double an = (Math.PI * 2) * i / n;
         pts[i] = p3(x + (float)Math.cos(an) * (r + 0.04F), y + (i % 2 == 0 ? 0.22F : -0.22F), (float)Math.sin(an) * (r + 0.04F));
      }

      float[] rr = new float[n + 1];
      Arrays.fill(rr, 0.09F);
      int oc = g.color;
      g.color(-1);
      Geo.chain(g, "white", pts, rr, 4);
      g.color(oc);
   }

   private static void icing2(G g, float x, float y, float r) {
      int n = 12;
      float[][] pts = new float[n + 1][];

      for (int i = 0; i <= n; i++) {
         double an = (Math.PI * 2) * i / n;
         pts[i] = p3(x + (i % 2 == 0 ? 0.2F : -0.2F), y + (float)Math.cos(an) * (r + 0.04F), (float)Math.sin(an) * (r + 0.04F));
      }

      float[] rr = new float[n + 1];
      Arrays.fill(rr, 0.09F);
      int oc = g.color;
      g.color(-1);
      Geo.chain(g, "white", pts, rr, 4);
      g.color(oc);
   }

   static void giftMonster(G g, Cos.A a) {
      Pets.P p = Pets.pose(a);
      float[] h = PetRig.hop(p);
      p.y = p.y + h[0] * 1.6F;
      p.stepBob = 0.0F;
      float sq = -h[1] * 0.1F;
      float W = 5.0F;
      float H = 4.0F;
      float D = 4.6F;
      g.push();
      Pets.root(g, p);
      g.color(-1);

      for (int s = -1; s <= 1; s += 2) {
         float c = PetRig.frac(p.t * 2.1F + (s > 0 ? 0.0F : 0.5F));
         float lift = p.walkW * Math.max(0.0F, Cos3Geo.sin((Math.PI * 2) * c)) * 0.5F;
         PetGeo.blob(g, Pets.gl(3054152), s * 1.4F, 0.45F + lift, 0.4F, 0.75F, 0.5F, 1.0F, 8, 4);
      }

      g.push();
      g.translate(0.0F, 0.75F, 0.0F);
      g.scale(1.0F + sq, 1.0F - sq, 1.0F + sq);
      g.rotZ(p.waddle * 0.5F);
      g.color(-1);
      Cos3Geo.bbox(g, "xm_gift:D8202A:FFD24A:1", -W / 2.0F, 0.0F, -D / 2.0F, W / 2.0F, H, D / 2.0F, 0.22F);
      float wr = 0.55F;
      g.color(-11414);
      g.box("xm_ribbon", -W / 2.0F - 0.08F, -0.08F, -wr, W / 2.0F + 0.08F, H + 0.08F, wr, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-12973552);
      g.box("xm_felt", -W / 2.0F + 0.3F, H - 0.35F, -D / 2.0F + 0.3F, W / 2.0F - 0.3F, H + 0.14F, D / 2.0F - 0.3F, 0.0F, 0.0F, 1.0F, 1.0F);
      float open = 12.0F
         + 26.0F * h[2]
         + (p.is("excited") ? 18.0F * Math.abs(Cos3Geo.sin(p.t * 7.0F)) : 0.0F)
         + (p.is("angry") ? 14.0F * Math.abs(Cos3Geo.sin(p.t * 12.0F)) : 0.0F)
         + (p.is("sleepy") ? -12 : 0)
         + 6.0F * Cos3Geo.sin(p.t * 1.7F);
      open = Math.max(0.0F, open);
      float lit = Math.min(1.0F, open / 25.0F);
      if (lit > 0.05F) {
         g.glow(true).color(Cos3Geo.alpha(0.9 * lit, 16769146));
         g.box("white", -W / 2.0F + 0.45F, H + 0.2F, -D / 2.0F + 0.45F, W / 2.0F - 0.45F, H + 0.25F, D / 2.0F - 0.45F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.glow(false);
         Cos3Geo.halo(g, 0.0F, H + 0.8F, 0.0F, 5.5F * lit, Cos3Geo.alpha(0.45 * lit, 16767082));

         for (int i = 0; i < 4; i++) {
            float ph = Cos3Geo.fract(p.t * 0.7F + i * 0.25F);
            Cos3Models.sparkle(
               g, Cos3Geo.sin(i * 2.4F + p.t) * 1.4F, H + 0.5F + ph * 4.0F, Cos3Geo.cos(i * 1.9F) * 1.2F, 0.28F, Cos3Geo.alpha((1.0F - ph) * lit, 16773296)
            );
         }
      }

      g.color(-1);

      for (int i = 0; i < 5; i++) {
         g.push();
         g.translate(-1.6F + i * 0.8F, H + 0.12F, D / 2.0F - 0.45F);
         PetGeo.cone(g, "white", 0.26F, 0.55F, 5, false);
         g.pop();
      }

      Pets.faceFlat(g, p, "std", 0.0F, H * 0.46F, D / 2.0F + 0.08F, W * 0.86F, H * 0.78F);
      g.push();
      g.translate(0.0F, H + 0.02F, -D / 2.0F - 0.15F);
      g.rotX(-open);
      g.translate(0.0F, 0.0F, D / 2.0F + 0.15F);
      float LW = W + 0.5F;
      float LD = D + 0.5F;
      float LH = 1.1F;
      Cos3Geo.bbox(g, "xm_gift:D8202A:FFD24A:1", -LW / 2.0F, 0.0F, -LD / 2.0F, LW / 2.0F, LH, LD / 2.0F, 0.2F);
      g.color(-11414);
      g.box("xm_ribbon", -wr, -0.06F, -LD / 2.0F - 0.06F, wr, LH + 0.06F, LD / 2.0F + 0.06F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.box("xm_ribbon", -LW / 2.0F - 0.12F, -0.12F, -wr, LW / 2.0F + 0.12F, LH + 0.12F, wr, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1);

      for (int i = 0; i < 4; i++) {
         g.push();
         g.translate(-1.2F + i * 0.8F, 0.02F, LD / 2.0F - 0.55F);
         g.rotX(180.0F);
         PetGeo.cone(g, "white", 0.26F, 0.5F, 5, false);
         g.pop();
      }

      g.translate(0.0F, LH + 0.12F, 0.0F);
      XmasGeo.bow(g, -11414, 1.25F);
      g.pop();
      g.pop();
      Pets.fx(g, p, H + 3.5F);
      g.pop();
      g.color(-1);
   }

   static void santaPenguin(G g, Cos.A a) {
      PetModels.penguin(g, a);
      Pets.P p = Pets.pose(a);
      float k = p.gr;
      g.push();
      Pets.root(g, p);
      g.translate(0.0F, p.waddleBob * 0.7F, 0.0F);
      float by = 3.55F + k * 1.1F - p.lie * 0.6F;
      float ry = 3.3F + k * 1.3F;
      g.translate(0.0F, by, 0.0F);
      g.rotZ(p.waddle * 1.2F);
      g.rotY(p.waddle * 0.5F);
      g.rotY(p.headYaw * 0.6F);
      g.rotX(p.headPitch * 0.4F);
      g.rotZ(p.headRoll * 0.8F);
      g.translate(0.0F, ry * 0.78F, -0.25F);
      g.rotX(-14.0F);
      g.rotZ(8.0F);
      float sway = Cos3Geo.sin(p.t * 2.1F) * 0.25F + p.walkW * Cos3Geo.sin(p.t * 9.0F) * 0.15F;
      int m = 9;
      float[][] sp = new float[m][];
      float[] rr = new float[m];
      float[] cur = new float[]{0.0F, 0.0F, 0.0F};

      for (int i = 0; i < m; i++) {
         float f = (float)i / (m - 1);
         double ang = f * f * 1.9 + 0.1;
         if (i > 0) {
            float seg = 0.55F;
            cur = new float[]{
               cur[0] + (float)(Math.sin(sway * f) * Math.sin(ang)) * seg,
               cur[1] + (float)Math.cos(ang) * seg,
               cur[2] - (float)(Math.cos(sway * f) * Math.sin(ang)) * seg
            };
         }

         sp[i] = cur;
         rr[i] = 1.75F * (1.0F - f) + 0.06F;
      }

      g.color(-2613206);
      XmasGeo.sweep(g, "xm_felt", sp, rr, rr, null, 14, 2.0F);
      g.color(-1);
      g.push();
      g.translate(0.0F, 0.08F, 0.0F);
      g.torus("xm_fluff", 1.72F, 0.42F, 18, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      float[] e = sp[m - 1];
      Cos3Geo.ball(g, "xm_fluff", e[0], e[1] - 0.1F, e[2], 0.5F, 8);
      g.pop();
   }

   static void sleigh(G g, Cos.A a) {
      float t = a.time;
      float mv = Cos3Geo.clamp(a.move, 0.0F, 1.0F);
      g.push();
      g.rotZ(-a.lean * 4.0F);
      g.translate(0.0F, mv * 0.15F * Math.abs(Cos3Geo.sin(t * 7.0F)), 0.0F);
      float[][] run = new float[][]{
         {0.0F, 2.6F, -10.3F},
         {0.0F, 1.6F, -10.2F},
         {0.0F, 0.7F, -9.2F},
         {0.0F, 0.5F, -7.5F},
         {0.0F, 0.5F, 5.0F},
         {0.0F, 0.6F, 8.6F},
         {0.0F, 1.1F, 10.6F},
         {0.0F, 2.4F, 12.3F},
         {0.0F, 4.4F, 13.4F},
         {0.0F, 6.6F, 13.8F},
         {0.0F, 8.3F, 13.2F},
         {0.0F, 9.0F, 12.2F},
         {0.0F, 8.7F, 11.2F},
         {0.0F, 7.9F, 10.9F},
         {0.0F, 7.4F, 11.4F}
      };

      for (int s = -1; s <= 1; s += 2) {
         float[][] pts = new float[run.length][];

         for (int i = 0; i < run.length; i++) {
            pts[i] = new float[]{s * 4.7F, run[i][1], run[i][2]};
         }

         float[] rr = new float[pts.length];
         Arrays.fill(rr, 0.42F);
         rr[rr.length - 1] = 0.3F;
         g.color(-11414);
         Cos3Geo.limb(g, "xm_gold", pts, rr, 6);

         for (float z : new float[]{-6.2F, -0.5F, 5.2F}) {
            Geo.tube(g, "xm_gold", new float[]{s * 4.7F, 0.6F, z}, new float[]{s * 4.7F, 2.6F, z + 0.4F}, 0.26F, 0.26F, 6);
            Cos3Geo.ball(g, "xm_gold", s * 4.7F, 2.55F, z + 0.4F, 0.36F, 6);
         }
      }

      g.color(-8779246);
      Cos3Geo.bbox(g, "xm_red_lacquer", -4.95F, 2.5F, -8.6F, 4.95F, 3.3F, 9.8F, 0.2F);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.rotY(-90.0F);
         g.color(-1);
         float xa = s > 0 ? 4.95F : -5.55F;
         float xb = xa + 0.6F;
         Cos3Geo.extrude(g, "xm_sleighside", SIDE, -xb, -xa, Cos3Geo.FULL, new float[]{0.0F, 0.05F, 0.02F, 0.07F});
         g.pop();
         int[] idx = new int[]{13, 14, 15, 16, 17, 18, 19, 20};
         float[][] tp = new float[idx.length][];

         for (int i = 0; i < idx.length; i++) {
            tp[i] = new float[]{s * 5.25F, SIDE[idx[i]][1] + 0.05F, SIDE[idx[i]][0]};
         }

         float[] tr = new float[tp.length];
         Arrays.fill(tr, 0.2F);
         g.color(-11414);
         Cos3Geo.limb(g, "xm_gold", tp, tr, 5);
      }

      g.color(-6287336);
      Cos3Geo.bbox(g, "xm_velvet", -4.5F, 3.2F, -3.2F, 4.5F, 6.0F, 3.6F, 0.45F);
      Cos3Geo.bbox(g, "xm_velvet", -4.4F, 5.5F, -4.0F, 4.4F, 14.2F, -2.45F, 0.5F);
      g.color(-11414);
      Cos3Geo.bbox(g, "xm_gold", -4.55F, 13.9F, -4.1F, 4.55F, 14.5F, -2.35F, 0.15F);
      g.color(-6681066);
      Cos3Geo.bbox(g, "xm_red_lacquer", -4.9F, 3.2F, 9.3F, 4.9F, 5.5F, 10.1F, 0.2F);
      g.color(-11414);
      Cos3Geo.bbox(g, "xm_gold", -4.95F, 5.4F, 9.2F, 4.95F, 5.75F, 10.2F, 0.1F);
      g.color(-5207976);
      Geo.ellipsoid(g, "xm_sack", 0.0F, 7.9F, -6.3F, 3.5F, 3.4F, 2.3F, 12, 8);
      Geo.ellipsoid(g, "xm_sack", 1.4F, 9.6F, -6.6F, 2.0F, 2.0F, 1.6F, 8, 5);
      Geo.ellipsoid(g, "xm_sack", -1.3F, 9.4F, -6.1F, 1.9F, 2.1F, 1.5F, 8, 5);
      g.push();
      g.translate(0.0F, 11.3F, -6.4F);
      Cos3Geo.lathe(g, "xm_sack", new float[]{1.4F, 0.8F, 0.7F, 1.0F, 1.25F}, new float[]{0.0F, 0.6F, 1.0F, 1.5F, 1.9F}, 12);
      g.color(-11414);
      g.translate(0.0F, 0.8F, 0.0F);
      g.torus("xm_gold", 0.82F, 0.18F, 12, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.push();
      g.translate(0.4F, 12.3F, -6.4F);
      g.rotZ(14.0F);
      g.rotY(20.0F);
      XmasBack.gift(g, "xm_gift:2E9A48:FFFFFF:0", -2088918, 1.8F, 1.6F, 1.6F);
      g.pop();
      g.push();
      g.translate(-1.4F, 12.0F, -6.0F);
      g.rotZ(-22.0F);
      g.rotY(-15.0F);
      XmasBack.gift(g, "xm_gift:3A6AE8:E8F4FF:3", -11414, 1.4F, 1.4F, 1.4F);
      g.pop();
      g.push();
      g.translate(1.6F, 11.8F, -6.9F);
      g.color(-1);
      float[][] cane = new float[][]{
         {0.0F, 0.0F, 0.0F}, {0.3F, 2.4F, 0.0F}, {0.55F, 3.1F, 0.0F}, {1.05F, 3.35F, 0.0F}, {1.5F, 3.0F, 0.0F}, {1.55F, 2.6F, 0.0F}
      };
      Cos3Geo.limb(g, "xm_candy", cane, new float[]{0.26F, 0.26F, 0.26F, 0.26F, 0.26F, 0.26F}, 7);
      g.pop();
      float[][] pole = new float[][]{{5.25F, 9.2F, 11.0F}, {5.4F, 11.4F, 11.5F}, {5.5F, 13.0F, 12.4F}, {5.5F, 13.4F, 13.5F}};
      g.color(-14013904);
      Cos3Geo.limb(g, "c3_metal", pole, new float[]{0.18F, 0.16F, 0.15F, 0.13F}, 6);
      g.push();
      g.translate(5.5F, 13.3F, 13.5F);
      g.rotX(Cos3Geo.sin(t * 2.2F) * 8.0F - mv * 10.0F);
      g.rotZ(Cos3Geo.sin(t * 1.7F) * 6.0F);
      g.color(-14013904);
      g.push();
      g.translate(0.0F, -0.35F, 0.0F);
      g.rotX(90.0F);
      g.torus("c3_metal", 0.3F, 0.07F, 8, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      Cos3Geo.lathe(g, "c3_metal", new float[]{0.0F, 0.55F, 0.9F, 0.95F, 0.8F}, new float[]{-0.6F, -0.75F, -1.1F, -1.35F, -1.35F}, 8);
      float fl = 0.85F + 0.1F * Cos3Geo.sin(t * 11.0F) + 0.05F * Cos3Geo.sin(t * 17.3F);
      g.glow(true).color(-1);
      g.cylinder("c3_lglass", 0.68F, 0.72F, -3.2F, -1.35F, 4, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.push();
      g.translate(0.0F, -2.9F, 0.0F);
      g.scale(1.0F, fl, 1.0F);
      Cos3Models.flame(g, 1.1F, 0.3F, -385902038, -251661136);
      g.pop();
      g.glow(false).color(-14013904);
      Cos3Geo.lathe(g, "c3_metal", new float[]{0.8F, 0.95F, 0.8F, 0.0F}, new float[]{-3.2F, -3.35F, -3.6F, -3.62F}, 8);
      Cos3Geo.halo(g, 0.0F, -2.3F, 0.0F, 5.5F, Cos3Geo.alpha(0.5 * fl, 16762986));
      g.pop();
      int nt = 5;

      for (int i = 0; i < nt; i++) {
         float ph = Cos3Geo.fract(t * 0.9F + (float)i / nt);
         float x = Cos3Geo.sin(i * 2.7F + t) * 3.5F;
         float y = 2.0F + (float)Capes.hash1(i, 601) * 6.0F + ph * 2.0F;
         float z = -10.0F - ph * 9.0F;
         float al = (0.25F + 0.75F * mv) * (1.0F - ph);
         if (a.preview) {
            al = 0.7F * (1.0F - ph);
         }

         Cos3Models.sparkle(g, x, y, z, 0.35F, Cos3Geo.alpha(al, i % 2 == 0 ? 16769146 : 15267071));
      }

      if (mv > 0.05F) {
         for (int s = -1; s <= 1; s += 2) {
            for (int i = 0; i < 3; i++) {
               float ph = Cos3Geo.fract(t * 2.2F + i / 3.0F + (s > 0 ? 0.0F : 0.17F));
               Cos3Geo.halo(g, s * (4.7F + ph * 1.2F), 0.5F + ph * 1.2F, -8.5F - ph * 3.0F, 1.2F + ph * 2.0F, Cos3Geo.alpha(mv * (1.0F - ph) * 0.7, 16777215));
            }
         }
      }

      g.pop();
      g.color(-1);
   }
}
