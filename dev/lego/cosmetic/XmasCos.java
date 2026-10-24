package dev.lego.cosmetic;

import java.util.Arrays;

final class XmasCos {
   private XmasCos() {
   }

   static void registerAll() {
      Cos.add("xm_hat_antlers", "Rentier-Geweih", Cos.Slot.HAT, Cos.Rarity.EPIC, XmasCos::antlers);
      Cos.add("xm_hat_elf", "Elfenmütze", Cos.Slot.HAT, Cos.Rarity.EPIC, XmasCos::elfHat);
      Cos.add("xm_hat_lightcrown", "Lichterketten-Krone", Cos.Slot.HAT, Cos.Rarity.LEGENDARY, XmasCos::lightCrown);
      Cos.add("xm_hat_tophat", "Schneemann-Zylinder", Cos.Slot.HAT, Cos.Rarity.EPIC, XmasCos::topHat);
      Cos.add("xm_hat_earmuffs", "Lebkuchen-Ohrenschützer", Cos.Slot.HAT, Cos.Rarity.RARE, XmasCos::earmuffs);
      Cos.add("xm_face_rednose", "Rentier-Nase", Cos.Slot.FACE, Cos.Rarity.EPIC, XmasCos::redNose);
      XmasBack.register();
      cape("winterwald", "Winterwald", Cos.Rarity.EPIC);
      cape("lebkuchenhaus", "Lebkuchenhaus", Cos.Rarity.EPIC);
      cape("zuckerstange", "Zuckerstange", Cos.Rarity.RARE);
      cape("schlittenmond", "Rentierschlitten am Mond", Cos.Rarity.LEGENDARY);
      cape("pullover", "Weihnachtspullover", Cos.Rarity.RARE);
      cape("nordlicht", "Nordlicht-Weihnacht", Cos.Rarity.LEGENDARY);
      XmasPets.register();
   }

   private static void cape(String id, String name, Cos.Rarity r) {
      Cos.ALL.add(new Cos.Item("xm_cape_" + id, name, Cos.Slot.CAPE, r, Models::capePreview, "cape_xm_" + id));
   }

   static float[] bandPt(double th, float rx, float ry, float y0) {
      float[] q = Geo.sq(th, 1.0F, 5.0F);
      return new float[]{q[0] * rx, y0 + q[1] * ry, 0.3F};
   }

   static void antlers(G g, Cos.A a) {
      float t = a.time;
      int n = 22;
      float[][] pts = new float[n + 1][];

      for (int i = 0; i <= n; i++) {
         pts[i] = bandPt(Math.PI * i / n, 5.0F, 5.6F, 3.6F);
      }

      float[] rr = new float[n + 1];
      Arrays.fill(rr, 0.42F);
      g.color(-4712930);
      Cos3Geo.limb(g, "xm_velvet", pts, rr, 7);

      for (int s = -1; s <= 1; s += 2) {
         float[] b = bandPt(s > 0 ? Math.PI / 3 : Math.PI * 2.0 / 3.0, 5.0F, 5.6F, 3.6F);
         g.push();
         g.translate(b[0], b[1] - 0.1F, b[2]);
         g.scale(s, 1.0F, 1.0F);
         g.color(-9813464);
         Geo.ellipsoid(g, "c3_fur", 0.0F, 0.1F, 0.0F, 0.75F, 0.6F, 0.75F, 8, 5);
         g.color(-1);
         float[][] beam = new float[][]{
            {0.0F, 0.2F, 0.0F},
            {0.45F, 1.4F, -0.15F},
            {1.35F, 2.8F, -0.45F},
            {2.15F, 4.0F, -0.55F},
            {2.6F, 5.3F, -0.35F},
            {2.55F, 6.5F, 0.05F},
            {2.25F, 7.3F, 0.35F}
         };
         Cos3Geo.limb(g, "xm_antler", beam, new float[]{0.55F, 0.5F, 0.45F, 0.4F, 0.33F, 0.25F, 0.14F}, 7);
         Cos3Geo.limb(g, "xm_antler", new float[][]{beam[1], {0.35F, 2.1F, 0.9F}, {0.2F, 2.6F, 1.5F}}, new float[]{0.36F, 0.26F, 0.12F}, 6);
         Cos3Geo.limb(g, "xm_antler", new float[][]{beam[2], {1.2F, 4.0F, 0.6F}, {1.05F, 4.9F, 1.05F}}, new float[]{0.34F, 0.25F, 0.12F}, 6);
         Cos3Geo.limb(g, "xm_antler", new float[][]{beam[3], {1.5F, 5.3F, -0.9F}, {1.25F, 6.2F, -1.0F}}, new float[]{0.3F, 0.22F, 0.1F}, 6);
         Cos3Geo.limb(g, "xm_antler", new float[][]{beam[4], {3.5F, 6.1F, -0.7F}, {3.9F, 6.7F, -0.7F}}, new float[]{0.26F, 0.18F, 0.09F}, 6);
         g.color(-2088918);
         Geo.ellipsoid(g, "xm_velvet", 1.62F, 2.95F, 0.05F, 0.34F, 0.28F, 0.34F, 8, 4);
         g.push();
         g.translate(1.62F, 2.75F, 0.1F);
         XmasGeo.bell(g, 0.9F, Cos3Geo.sin(t * 3.2F + s) * 16.0F + (a.preview ? 0.0F : a.move * Cos3Geo.sin(t * 12.0F) * 20.0F));
         g.pop();
         g.pop();
      }

      g.push();
      g.translate(0.0F, 9.25F, 0.3F);
      g.rotX(18.0F);
      XmasGeo.holly(g, 0.72F);
      g.pop();
   }

   static void elfHat(G g, Cos.A a) {
      float t = a.time;
      float mv = a.preview ? 0.0F : Cos3Geo.clamp(a.move, 0.0F, 1.0F);
      float sway = Cos3Geo.sin(t * 1.7F) * 0.22F + Cos3Geo.sin(t * 3.1F) * 0.06F;
      float back = 0.35F + mv * 0.35F + Cos3Geo.sin(t * 2.3F) * 0.05F;
      float[] ys = new float[]{5.3F, 7.0F, 8.6F, 9.6F, 10.5F};
      float[] r0 = new float[]{4.7F, 4.8F, 4.6F, 3.75F, 3.05F};
      float[] n0 = new float[]{6.0F, 6.0F, 4.5F, 3.0F, 2.3F};
      int floppy = 10;
      int m = ys.length + floppy;
      float[][] sp = new float[m][];
      float[] rx = new float[m];
      float[] rz = new float[m];
      float[] nn = new float[m];

      for (int i = 0; i < ys.length; i++) {
         sp[i] = new float[]{0.0F, ys[i], 0.0F};
         rx[i] = rz[i] = r0[i];
         nn[i] = n0[i];
      }

      float[] cur = (float[])sp[ys.length - 1].clone();

      for (int i = 0; i < floppy; i++) {
         float f = (float)(i + 1) / floppy;
         double ang = f * f * 2.3 * (0.8 + back * 0.6) + 0.15;
         double side = sway * f * 1.8 + 0.35 * f;
         float seg = 1.45F - 0.3F * f;
         float dx = (float)(Math.sin(side) * Math.sin(ang));
         float dy = (float)Math.cos(ang);
         float dz = (float)(-Math.cos(side) * Math.sin(ang));
         cur = new float[]{cur[0] + dx * seg, cur[1] + dy * seg, cur[2] + dz * seg};
         int j = ys.length + i;
         sp[j] = cur;
         rx[j] = rz[j] = Math.max(0.08F, 2.75F * (1.0F - f) + 0.08F);
         nn[j] = 2.0F;
      }

      g.color(-13723064);
      XmasGeo.sweep(g, "xm_felt", sp, rx, rz, nn, 20, 3.0F);
      float[] e = sp[m - 1];
      g.push();
      g.translate(e[0], e[1] - 0.1F, e[2]);
      XmasGeo.bell(g, 1.05F, Cos3Geo.sin(t * 4.1F) * 20.0F + sway * 30.0F);
      g.pop();
      g.color(-1);
      Cos3Geo.band(g, "xm_elfcuff", 2.6F, 2.1F, 0.62F, 4.9F, 6.7F, 0.0F, 3.0F);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 5.45F, 5.2F, 0.4F);
         XmasGeo.bell(g, 0.55F, Cos3Geo.sin(t * 3.0F + s) * 12.0F);
         g.pop();
      }
   }

   static void lightCrown(G g, Cos.A a) {
      float t = a.time;
      float c = 2.6F;
      float r = 2.1F;
      float th = 0.45F;
      g.color(-11414);
      Cos3Geo.band(g, "xm_gold", c, r, th, 6.4F, 8.1F, 0.0F, 4.0F);
      g.color(-5720);
      Cos3Geo.band(g, "xm_gold", c, r + th - 0.08F, 0.26F, 7.95F, 8.4F, 0.0F, 4.0F);
      Cos3Geo.band(g, "xm_gold", c, r + th - 0.08F, 0.26F, 6.2F, 6.6F, 0.0F, 4.0F);
      int chase = (int)Math.floor(t * 1.4F);

      for (int i = 0; i < 8; i++) {
         double ang = i * Math.PI / 4.0 + (Math.PI / 8);
         float[] p = Cos3Models.loopPt(c, r + th * 0.5F, ang);
         g.push();
         g.translate(p[0], 8.35F, p[1]);
         g.rotY((float)(-Math.toDegrees(ang)) + 90.0F);
         g.rotX(10.0F);
         g.color(-11414);
         Cos3Geo.shard(g, "xm_gold", 0.28F, 1.1F);
         g.translate(0.0F, 1.0F, 0.0F);
         int col = XmasGeo.LIGHTS[(i + chase) % XmasGeo.LIGHTS.length];
         XmasGeo.bulb(g, 1.25F, col, XmasGeo.twinkle(t, i));
         g.pop();
         float[] q = Cos3Models.loopPt(c, r + th + 0.02F, ang + (Math.PI / 8));
         g.push();
         g.translate(q[0], 7.2F, q[1]);
         g.rotY((float)(-Math.toDegrees(ang + (Math.PI / 8))) + 90.0F);
         g.rotX(90.0F);
         g.glow(true).color(i % 2 == 0 ? -2088918 : -13711270);
         Cos3Geo.gem(g, "c3_crystal", 0.36F, 0.3F, 0.1F, 6);
         g.glow(false);
         g.pop();
      }

      int n = 48;
      float[][] w = new float[n + 1][];

      for (int i = 0; i <= n; i++) {
         double an = (Math.PI * 2) * i / n + (Math.PI / 8);
         float[] p = Cos3Models.loopPt(c, r + th + 0.28F, an);
         float y = 8.25F - 1.25F * Math.abs(Cos3Geo.sin((an - (Math.PI / 8)) * 4.0));
         w[i] = new float[]{p[0], y, p[1]};
      }

      XmasGeo.wire(g, w, 0.09F);

      for (int i = 0; i < 8; i++) {
         double an = i * Math.PI / 4.0 + (Math.PI / 4);
         float[] p = Cos3Models.loopPt(c, r + th + 0.42F, an);
         int col = XmasGeo.LIGHTS[(i + 3 + chase) % XmasGeo.LIGHTS.length];
         XmasGeo.fairy(g, p[0], 6.85F, p[1], 0.3F, col, XmasGeo.twinkle(t, i + 20));
      }

      g.push();
      g.translate(0.0F, 11.4F + Cos3Geo.sin(t * 2.0F) * 0.3F, 0.0F);
      g.rotY(t * 40.0F);
      g.glow(true).color(-10166);
      XmasGeo.star(g, "xm_gold", 1.35F, 0.45F, 5);
      g.glow(false);
      Cos3Geo.halo(g, 0.0F, 0.0F, 0.0F, 4.2F, Cos3Geo.alpha(0.5 + 0.2 * Cos3Geo.sin(t * 3.0F), 16769146));
      g.pop();

      for (int i = 0; i < 4; i++) {
         float ph = Cos3Geo.fract(t * 0.5F + i * 0.25F);
         float an = i * 90 + t * 40.0F;
         Cos3Models.sparkle(
            g,
            Cos3Geo.cos(Math.toRadians(an)) * 1.8F * (1.0F + ph),
            11.4F - ph * 2.5F,
            Cos3Geo.sin(Math.toRadians(an)) * 1.8F * (1.0F + ph),
            0.25F,
            Cos3Geo.alpha(1.0F - ph, 16773296)
         );
      }
   }

   static void topHat(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(0.0F, 8.0F, 0.0F);
      g.rotZ(-6.0F + Cos3Geo.sin(t * 1.3F) * 0.8F);
      g.rotX(-3.0F);
      g.color(-14013900);
      Cos3Geo.lathe(
         g,
         "xm_silk",
         new float[]{0.0F, 5.6F, 6.15F, 6.4F, 6.25F, 5.85F, 3.9F, 3.72F, 3.6F, 3.72F, 3.95F, 3.98F, 3.72F, 0.0F},
         new float[]{0.05F, 0.05F, 0.15F, 0.4F, 0.64F, 0.66F, 0.56F, 0.9F, 4.0F, 6.4F, 7.2F, 7.42F, 7.62F, 7.62F},
         32
      );
      g.color(-3139030);
      Cos3Models.flatRing(g, "xm_ribbon", 3.3F, 3.84F, 1.0F, 2.3F, 28);
      g.push();
      g.rotY(-35.0F);
      g.translate(3.95F, 1.75F, 0.0F);
      g.rotZ(-78.0F);
      XmasGeo.holly(g, 0.95F);
      g.pop();
      g.color(-1);
      Geo.ellipsoid(g, "xm_snow", 0.2F, 7.62F, 0.1F, 3.55F, 0.6F, 3.5F, 16, 6);
      Geo.ellipsoid(g, "xm_snow", -1.3F, 8.05F, 0.6F, 1.4F, 0.55F, 1.3F, 10, 5);
      float[][] patches = new float[][]{{4.7F, 0.6F, 1.1F}, {-3.0F, -3.9F, 1.3F}};

      for (float[] pch : patches) {
         Geo.ellipsoid(g, "xm_snow", pch[0], 0.6F, pch[1], pch[2], 0.2F, pch[2] * 0.7F, 10, 4);
      }

      g.pop();

      for (int i = 0; i < 5; i++) {
         float ph = Cos3Geo.fract(t * 0.22F + i * 0.2F);
         float an = i * 72 + t * 25.0F;
         float x = Cos3Geo.cos(Math.toRadians(an)) * 7.2F;
         float z = Cos3Geo.sin(Math.toRadians(an)) * 7.2F;
         float y = 17.0F - ph * 10.0F;
         g.push();
         g.translate(x, y, z);
         g.rotY(an + t * 60.0F);
         g.glow(true).color(Cos3Geo.alpha(Cos3Geo.smooth(ph * 5.0F) * Cos3Geo.smooth((1.0F - ph) * 5.0F) * 0.95, 15792383));
         g.cross("xm_flake", 0.0F, 0.0F, 0.0F, 1.3F, 1.3F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.glow(false);
         g.pop();
      }

      g.color(-1);
   }

   static void earmuffs(G g, Cos.A a) {
      float t = a.time;
      int n = 24;
      float[][] pts = new float[n + 1][];

      for (int i = 0; i <= n; i++) {
         pts[i] = new float[]{bandPt(Math.PI * i / n, 5.0F, 5.9F, 3.4F)[0], bandPt(Math.PI * i / n, 5.0F, 5.9F, 3.4F)[1], 0.0F};
      }

      float[] rr = new float[n + 1];
      Arrays.fill(rr, 0.42F);
      g.color(-1);
      Geo.chain(g, "xm_candy", pts, rr, 8);
      float[][] gm = new float[XmasTex.GMAN.length][];

      for (int i = 0; i < gm.length; i++) {
         gm[i] = new float[]{XmasTex.GMAN[i][0] * 2.35F, XmasTex.GMAN[i][1] * 2.35F};
      }

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 5.0F, 3.4F, 0.0F);
         g.color(-593170);
         Geo.ellipsoid(g, "xm_fluff", 0.0F, 0.0F, 0.0F, 0.95F, 2.45F, 2.45F, 14, 9);
         g.push();
         g.translate(s * 0.72F, 0.0F, 0.0F);
         g.rotY(s * 90);
         g.rotZ(Cos3Geo.sin(t * 2.2F + s) * 6.0F);
         g.color(-1);
         Cos3Geo.extrude(g, "xm_gingerman", gm, -0.05F, 0.62F, Cos3Geo.FULL, new float[]{0.01F, 0.5F, 0.04F, 0.53F});
         g.pop();
         g.pop();
      }
   }

   static void redNose(G g, Cos.A a) {
      float t = a.time;
      float pul = 0.5F + 0.5F * Cos3Geo.sin(t * 3.4F);
      float s = 1.0F + 0.07F * pul;
      g.push();
      g.translate(0.0F, 3.1F, 4.72F);
      g.scale(s);
      g.glow(true).color(0xFF000000 | Capes.mix(12587036, 16730698, pul));
      g.sphere("xm_gloss", 1.08F, 16, 10, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-385875969);
      Cos3Geo.ball(g, "white", -0.38F, 0.42F, 0.86F, 0.2F, 6);
      g.glow(false);
      g.pop();
      Cos3Geo.halo(g, 0.0F, 3.1F, 5.2F, 3.4F + pul * 1.6F, Cos3Geo.alpha(0.35 + 0.35 * pul, 16726586));

      for (int i = 0; i < 3; i++) {
         float ph = Cos3Geo.fract(t * 0.6F + i / 3.0F);
         Cos3Models.sparkle(g, Cos3Geo.sin(i * 2.1F + t) * 1.5F, 3.4F + ph * 3.0F, 5.6F + ph * 0.6F, 0.2F, Cos3Geo.alpha((1.0F - ph) * 0.9, 16747146));
      }

      g.color(-1);
   }
}
