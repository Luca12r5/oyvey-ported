package dev.lego.cosmetic;

final class Cos3Aura {
   private Cos3Aura() {
   }

   static void register() {
      Cos.add("aura_c3_splash", "Wasser-Splash", Cos.Slot.AURA, Cos.Rarity.EPIC, Cos3Aura::splash);
      Cos.add("aura_c3_book", "Zauberbuch-Orbit", Cos.Slot.AURA, Cos.Rarity.LEGENDARY, Cos3Aura::bookOrbit);
      Cos.add("aura_c3_hexshield", "Hex-Schild", Cos.Slot.AURA, Cos.Rarity.EPIC, Cos3Aura::hexShield);
      Cos.add("aura_c3_tesla", "Tesla-Feld", Cos.Slot.AURA, Cos.Rarity.LEGENDARY, Cos3Aura::tesla);
      Cos.add("aura_c3_planets", "Mini-Sonnensystem", Cos.Slot.AURA, Cos.Rarity.LEGENDARY, Cos3Aura::planets);
      Cos.add("wings_c3_mech", "Mecha-Schwingen", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, Cos3Aura::mechWings);
      Cos.add("wings_c3_prism", "Prismenflügel", Cos.Slot.WINGS, Cos.Rarity.EPIC, Cos3Aura::prismWings);
   }

   static float mv(Cos.A a) {
      return a.preview ? 0.0F : Cos3Geo.clamp(a.move, 0.0F, 1.0F);
   }

   static void splash(G g, Cos.A a) {
      float t = a.time;

      for (int k = 0; k < 2; k++) {
         int n = 40;
         float R = k == 0 ? 6.1F : 7.0F;
         float[][] pts = new float[n + 1][];

         for (int i = 0; i <= n; i++) {
            double an = (Math.PI * 2) * i / n;
            float y = k == 0 ? -13.4F + 0.45F * Cos3Geo.sin(an * 3.0 + t * 3.0F) : -14.2F + 0.35F * Cos3Geo.sin(an * 4.0 - t * 2.5F);
            pts[i] = new float[]{(float)Math.cos(an) * R, y, (float)Math.sin(an) * R};
         }

         float[] rr = new float[n + 1];

         for (int i = 0; i <= n; i++) {
            rr[i] = (k == 0 ? 0.3F : 0.18F) * (0.75F + 0.25F * Cos3Geo.sin(i * Math.PI * 2.0 / n * 5.0 + t * 4.0F));
         }

         g.color(k == 0 ? -1203062529 : -927405825);
         Geo.chain(g, "white", pts, rr, 6);
      }

      for (int i = 0; i < 2; i++) {
         float f = Cos3Geo.fract(t * 0.55F + i * 0.5F);
         float r = 5.2F + f * 4.2F;
         float h = 2.1F * (1.0F - f) + 0.3F;
         g.color(Cos3Geo.alpha((1.0F - f) * 0.95, 16777215));
         g.cylinder("c3_splash", r, r * 1.12F, -15.0F, -15.0F + h, 44, false, 0.0F, 0.0F, 1.0F, 1.0F);
      }

      for (int i = 0; i < 12; i++) {
         float p = Cos3Geo.fract(t * 0.5F + i / 12.0F);
         float ang = i * 30 + t * 20.0F;
         float rad = 9.1F + 2.2F * p;
         float y = -14.0F + 34.0F * p * (1.0F - p);
         float sc = Cos3Geo.smooth(p / 0.08F) * Cos3Geo.smooth((1.0F - p) / 0.08F);
         if (!(sc < 0.02F)) {
            g.push();
            g.rotY(ang);
            g.translate(rad, y, 0.0F);
            Cos3Geo.alignY(g, 2.5F, 34.0F * (1.0F - 2.0F * p), 0.0F);
            g.scale(sc * (0.8F + 0.3F * (i % 3) / 2.0F));
            g.color(-931474177);
            Cos3Geo.lathe(g, "white", new float[]{0.0F, 0.42F, 0.5F, 0.34F, 0.14F, 0.0F}, new float[]{-0.5F, -0.35F, -0.05F, 0.35F, 0.7F, 0.95F}, 10);
            g.color(-251658241);
            Cos3Geo.ball(g, "white", 0.18F, 0.0F, 0.44F, 0.13F, 5);
            g.pop();
         }
      }

      for (int ix = 0; ix < 3; ix++) {
         float f = Cos3Geo.fract(t * 0.4F + ix / 3.0F);
         g.push();
         g.translate(0.0F, -23.85F + ix * 0.02F, 0.0F);
         g.rotX(-90.0F);
         g.color(Cos3Geo.alpha((1.0F - f) * 0.8, 16777215));
         float s = 9.0F + f * 18.0F;
         g.plane("c3_ripple", 0.0F, 0.0F, s, s, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      for (int ix = 0; ix < 8; ix++) {
         float f = Cos3Geo.fract(t * 0.9F + ix * 0.125F);
         float an = ix * 45 + t * 40.0F;
         Cos3Models.sparkle(
            g,
            Cos3Geo.cos(Math.toRadians(an)) * (6.5F + f * 3.0F),
            -14.5F + f * 2.0F,
            Cos3Geo.sin(Math.toRadians(an)) * (6.5F + f * 3.0F),
            0.22F,
            Cos3Geo.alpha(1.0F - f, 15268607)
         );
      }

      g.color(-1);
   }

   static void bookOrbit(G g, Cos.A a) {
      float t = a.time;
      float ang = t * 32.0F;

      for (int j = 1; j <= 7; j++) {
         double an = Math.toRadians(ang - j * 7);
         Cos3Models.sparkle(
            g,
            (float)Math.cos(an) * 11.2F,
            -3.0F + Cos3Geo.sin(t * 1.7F - j * 0.2F) * 0.6F + Cos3Geo.sin(j * 1.7F) * 0.5F,
            (float)(-Math.sin(an)) * 11.2F,
            0.28F - j * 0.025F,
            Cos3Geo.alpha(0.9 - j * 0.11, 14199039)
         );
      }

      g.push();
      g.rotY(ang);
      g.translate(11.2F, -3.0F + Cos3Geo.sin(t * 1.7F) * 0.6F, 0.0F);
      g.rotY(90.0F);
      g.rotX(-28.0F);
      g.rotZ(Cos3Geo.sin(t * 1.3F) * 5.0F);
      g.scale(1.35F);
      float[] plain = new float[]{0.01F, 0.01F, 0.04F, 0.04F};

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.rotY(-18 * s);
         g.color(-1);
         float xa = s > 0 ? 0.0F : -3.25F;
         float xb = s > 0 ? 3.25F : 0.0F;
         Cos3Geo.bbox(g, "c3_cover", xa, -2.25F, -0.4F, xb, 2.25F, -0.16F, 0.08F);
         float pa = s > 0 ? 0.1F : -3.0F;
         float pb = s > 0 ? 3.0F : -0.1F;
         g.box6(
            "c3_page",
            pa,
            -2.05F,
            -0.2F,
            pb,
            2.05F,
            0.3F,
            new float[][]{s > 0 ? new float[]{0.0F, 0.0F, 1.0F, 1.0F} : new float[]{1.0F, 0.0F, 0.0F, 1.0F}, plain, plain, plain, plain, plain}
         );
         g.pop();
      }

      g.color(-10872198);
      Geo.tube(g, "c3_cover", new float[]{0.0F, -2.25F, -0.3F}, new float[]{0.0F, 2.25F, -0.3F}, 0.3F, 0.3F, 8);
      float f = Cos3Geo.fract(t * 0.45F);
      if (f < 0.6F) {
         float th = 18.0F + 144.0F * Cos3Geo.smooth(f / 0.6F);
         g.push();
         g.translate(0.0F, 0.0F, 0.32F);
         g.rotY(-th);
         g.color(-1);
         float lift = Cos3Geo.sin(Math.PI * f / 0.6F) * 0.5F;
         g.quad(
            "c3_page",
            new float[]{0.0F, 2.0F, 0.0F},
            new float[]{1.45F, 2.0F, lift},
            new float[]{1.45F, -2.0F, lift},
            new float[]{0.0F, -2.0F, 0.0F},
            new float[]{0.0F, 0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.0F, 1.0F}
         );
         g.quad(
            "c3_page",
            new float[]{1.45F, 2.0F, lift},
            new float[]{2.9F, 2.0F, lift * 0.6F},
            new float[]{2.9F, -2.0F, lift * 0.6F},
            new float[]{1.45F, -2.0F, lift},
            new float[]{0.5F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.5F, 1.0F}
         );
         g.pop();
      }

      g.glow(true);

      for (int k = 0; k < 3; k++) {
         float gp = Cos3Geo.fract(t * 0.6F + k / 3.0F);
         g.color(Cos3Geo.alpha(Math.sin(Math.PI * gp) * 0.95, k == 1 ? 16767082 : 13142783));
         g.push();
         g.translate(Cos3Geo.sin(k * 2.1F + gp * 2.0F) * 1.3F, 0.6F + gp * 4.5F, 0.9F + gp * 0.9F);
         g.rotY(Cos3Geo.sin(t * 2.0F + k) * 25.0F);
         float sz = 0.9F + gp * 0.7F;
         g.plane("c3_glyphs", 0.0F, 0.0F, sz, sz, k / 4.0F, 0.0F, (k + 1) / 4.0F, 1.0F);
         g.pop();
      }

      g.glow(false);
      Cos3Geo.halo(g, 0.0F, 0.4F, 1.0F, 3.2F, 1354263295);
      g.pop();
      g.color(-1);
   }

   static void hexShield(G g, Cos.A a) {
      float t = a.time;
      float[][] hex = new float[6][];

      for (int k = 0; k < 6; k++) {
         double an = Math.toRadians(30 + 60 * k);
         hex[k] = new float[]{(float)Math.cos(an) * 1.55F, (float)Math.sin(an) * 1.55F};
      }

      g.glow(true);

      for (int r = 0; r < 3; r++) {
         float y = -1.5F - r * 5.2F;
         float R = 10.5F - Math.abs(r - 1) * 0.8F;
         float dir = r % 2 == 0 ? 1.0F : -1.0F;
         float ang0 = dir * t * 18.0F + r * 18;

         for (int i = 0; i < 10; i++) {
            float pulse = 0.5F + 0.5F * Cos3Geo.sin(t * 2.5F + i * 0.9F + r * 1.7F);
            boolean hit = Capes.hash((int)Math.floor(t * 2.0F), i + r * 10, 3) > 0.93;
            g.push();
            g.rotY(ang0 + i * 36);
            g.translate(R, y, 0.0F);
            g.rotY(90.0F);
            g.rotX((r - 1) * 18);
            g.color(hit ? -251658241 : Cos3Geo.alpha(0.35 + 0.45 * pulse, r == 1 ? 4909311 : 6990079));
            Cos3Geo.extrude(g, "c3_hex", hex, -0.1F, 0.1F, Cos3Geo.FULL, new float[]{0.48F, 0.04F, 0.52F, 0.08F});
            g.pop();
         }
      }

      g.glow(false);
      g.push();
      g.translate(0.0F, -23.8F, 0.0F);
      g.rotY(t * 20.0F);
      g.glow(true).color(1615522047);
      g.torus("white", 9.8F, 0.12F, 40, 3, 0.0F, 0.0F, 1.0F, 1.0F);
      g.glow(false);
      g.pop();
      g.color(-1);
   }

   static void tesla(G g, Cos.A a) {
      float t = a.time;
      float R = 10.0F;
      float[][] orb = new float[3][];
      double[] an = new double[3];

      for (int i = 0; i < 3; i++) {
         an[i] = Math.toRadians(t * 40.0F + i * 120);
         orb[i] = new float[]{(float)Math.cos(an[i]) * R, -4.0F + Cos3Geo.sin(t * 1.3F + i * 2) * 2.5F - i * 1.2F, (float)(-Math.sin(an[i])) * R};
      }

      for (int i = 0; i < 3; i++) {
         float[] p = orb[i];
         g.glow(true).color(-1);
         Cos3Geo.ball(g, "white", p[0], p[1], p[2], 0.45F, 8);
         g.color(1883953407);
         Cos3Geo.ball(g, "c3_crystal", p[0], p[1], p[2], 1.0F, 12);
         g.push();
         g.translate(p[0], p[1], p[2]);
         g.rotX(60 + i * 20);
         g.rotY(t * 200.0F);
         g.color(-7673601);
         g.torus("white", 1.35F, 0.07F, 16, 3, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
         Cos3Geo.halo(g, p[0], p[1], p[2], 2.8F, 1883953407);
         g.glow(false);
         int j = (i + 1) % 3;
         int step = (int)Math.floor(t * 8.0F);
         if (!(Capes.hash(step, i, 9) < 0.35)) {
            float[] q = orb[j];
            float[] prev = p;

            for (int s = 1; s <= 3; s++) {
               double m = an[i] + Math.toRadians(120.0) * s / 3.0;
               float yy = p[1] + (q[1] - p[1]) * s / 3.0F;
               float[] cur = s == 3 ? q : new float[]{(float)Math.cos(m) * R, yy, (float)(-Math.sin(m)) * R};
               Cos3Geo.arc(g, prev, cur, 1.1F, 0.07F, i * 7 + s, t, -1068836609);
               prev = cur;
            }
         }
      }

      int step = (int)Math.floor(t * 6.0F);

      for (int k = 0; k < 3; k++) {
         if (!(Capes.hash(step, k, 21) < 0.4)) {
            double a0 = Capes.hash(step, k, 22) * Math.PI * 2.0;
            Cos3Geo.arc(
               g,
               new float[]{(float)Math.cos(a0) * 5.6F, -23.6F, (float)Math.sin(a0) * 5.6F},
               new float[]{(float)Math.cos(a0 + 0.7) * 5.6F, -23.6F, (float)Math.sin(a0 + 0.7) * 5.6F},
               0.8F,
               0.05F,
               step + k,
               t,
               -1337272065
            );
         }
      }

      g.color(-1);
   }

   static void planets(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(0.0F, -6.0F, 0.0F);
      g.rotX(10.0F);
      g.rotZ(-6.0F);
      float[] R = new float[]{9.5F, 12.0F, 14.5F};
      float[] sp = new float[]{40.0F, 28.0F, 19.0F};
      float[] pr = new float[]{0.8F, 1.15F, 1.5F};
      String[] tex = new String[]{"c3_planet_ice", "c3_planet_lava", "c3_planet_gas"};

      for (int i = 0; i < 3; i++) {
         g.glow(true).color(1625878783);
         g.torus("white", R[i], 0.05F, 56, 3, 0.0F, 0.0F, 1.0F, 1.0F);
         g.glow(false);
         g.push();
         g.rotY(t * sp[i] + i * 130);
         g.translate(R[i], 0.0F, 0.0F);
         g.push();
         g.rotY(-t * 70.0F);
         g.color(-1);
         g.sphere(tex[i], pr[i], 16, 8, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
         if (i == 1) {
            Cos3Geo.halo(g, 0.0F, 0.0F, 0.0F, pr[i] * 4.0F, 1627351594);
         }

         if (i == 2) {
            g.push();
            g.rotZ(22.0F);
            g.color(-3880);
            g.ring("c3_ring", pr[i] * 1.35F, pr[i] * 2.1F, 0.0F, 24, 0.0F, 0.0F, 1.0F, 1.0F);
            g.pop();
         }

         if (i == 0) {
            g.push();
            g.rotY(t * 150.0F);
            g.translate(1.5F, 0.2F, 0.0F);
            g.color(-2565920);
            g.sphere("c3_metal", 0.3F, 8, 4, 0.0F, 0.0F, 1.0F, 1.0F);
            g.pop();
         }

         g.pop();
      }

      for (int i = 0; i < 10; i++) {
         double an = Math.toRadians(i * 36 + t * 8.0F);
         float r = R[i % 3] + (i % 2 == 0 ? 0.5F : -0.5F);
         Cos3Models.sparkle(
            g,
            (float)Math.cos(an) * r,
            Cos3Geo.sin(i * 2.7F) * 0.8F,
            (float)Math.sin(an) * r,
            0.25F,
            Cos3Geo.alpha(0.5 + 0.5 * Cos3Geo.sin(t * 3.0F + i), 16777215)
         );
      }

      g.pop();
      g.color(-1);
   }

   static void mechWings(G g, Cos.A a) {
      float t = a.time;
      float mv = mv(a);
      float flap = Cos3Geo.sin(t * 1.6F) * 7.0F * (1.0F - mv) + Cos3Geo.sin(t * 6.0F) * 16.0F * mv;
      float raise = Math.max(0.0F, (a.sneak ? 10 : 35) + flap);
      float sweep = a.sneak ? 55.0F : 26.0F - mv * 8.0F;
      float spread = 0.5F + 0.5F * Cos3Geo.sin(t * 0.8F);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 1.2F, -2.2F, -3.6F);
         g.scale(s * 1.3F, 1.3F, 1.3F);
         g.rotY(sweep);
         g.rotZ(raise);
         g.color(-13749700);
         Cos3Geo.ball(g, "c3_metal", 0.0F, 0.0F, 0.0F, 0.75F, 10);
         g.glow(true).color(-11867905);
         g.push();
         g.rotX(90.0F);
         g.torus("white", 0.78F, 0.1F, 14, 3, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
         g.glow(false);
         g.color(-12960182);
         Cos3Geo.bbox(g, "c3_metal", 0.3F, -0.45F, -0.35F, 4.6F, 0.45F, 0.35F, 0.2F);
         blades(g, t, 0, new float[]{1.3F, 2.6F, 3.9F}, new float[]{6.4F, 7.2F, 8.0F}, -12.0F, 6.0F, spread);
         g.translate(4.5F, 0.0F, 0.0F);
         g.color(-13749700);
         Cos3Geo.ball(g, "c3_metal", 0.0F, 0.0F, 0.0F, 0.55F, 8);
         g.glow(true).color(-11867905);
         Cos3Geo.ball(g, "white", 0.0F, 0.0F, 0.5F, 0.2F, 5);
         g.glow(false);
         g.rotZ(-30.0F + flap * 0.4F);
         g.color(-12960182);
         Cos3Geo.bbox(g, "c3_metal", 0.2F, -0.38F, -0.3F, 5.2F, 0.38F, 0.3F, 0.18F);
         blades(g, t, 3, new float[]{1.0F, 2.3F, 3.6F, 4.9F}, new float[]{8.6F, 8.8F, 8.2F, 7.0F}, 8.0F, 11.0F, spread);
         g.push();
         g.translate(5.2F, 0.0F, 0.0F);
         g.rotZ(-90.0F);
         g.glow(true).color(-11867905);
         Cos3Geo.shard(g, "c3_crystal", 0.3F, 1.3F);
         g.glow(false);
         g.pop();
         g.pop();
      }

      g.color(-1);
   }

   private static void blades(G g, float t, int idx0, float[] xs, float[] lens, float fan0, float fanStep, float spread) {
      for (int i = 0; i < xs.length; i++) {
         float L = lens[i];
         g.push();
         g.translate(xs[i], -0.2F, 0.0F);
         g.rotZ(fan0 + i * fanStep * (0.8F + 0.4F * spread));
         float[][] blade = new float[][]{{-0.55F, 0.0F}, {0.55F, 0.0F}, {0.5F, -L * 0.78F}, {0.05F, -L}, {-0.35F, -L * 0.84F}};
         g.color(-1775376);
         Cos3Geo.extrude(g, "c3_metal", blade, -0.16F, 0.16F);
         float[][] edge = new float[][]{{0.28F, -0.4F}, {0.62F, -0.4F}, {0.57F, -L * 0.78F}, {0.08F, -L * 1.02F}, {0.3F, -L * 0.78F}};
         float pul = 0.7F + 0.3F * Cos3Geo.sin(t * 3.0F - (idx0 + i) * 0.6F);
         g.glow(true).color(Cos3Geo.alpha(pul, 4909311));
         Cos3Geo.extrude(g, "white", edge, -0.22F, 0.22F);
         g.glow(false);
         g.pop();
      }
   }

   static void prismWings(G g, Cos.A a) {
      float t = a.time;
      float mv = mv(a);
      float flap = Cos3Geo.sin(t * 1.5F) * 6.0F * (1.0F - mv) + Cos3Geo.sin(t * 6.0F) * 14.0F * mv;

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 1.0F, -2.4F, -3.4F);
         g.scale(s, 1.0F, 1.0F);
         g.rotY(a.sneak ? 50.0F : 24.0F);
         g.rotZ(flap);
         int n = 9;

         for (int i = 0; i < n; i++) {
            float an = 78 - i * 17;
            float L = 4.0F + 6.5F * Cos3Geo.sin(Math.PI * (i + 0.6) / (n + 0.2));
            float base = 0.9F + 0.3F * Cos3Geo.sin(t * 2.0F + i * 0.8F);
            double ar = Math.toRadians(an);
            g.push();
            g.rotX(-42.0F * Math.max(0.0F, (float)Math.sin(ar)));
            g.translate((float)Math.cos(ar) * base, (float)Math.sin(ar) * base, 0.0F);
            g.rotZ(an - 90.0F);
            g.scale(1.0F, 1.0F, 0.55F);
            int c = Capes.hsv(0.52 + i * 0.045 + 0.03 * Math.sin(t), 0.5, 1.0);
            g.glow(true).color(Cos3Geo.argb(200, c));
            Cos3Geo.shard(g, "c3_crystal", 0.55F + 0.15F * Cos3Geo.sin(i * 1.3F), L);
            g.pop();
            if (i % 2 == 0) {
               float fx = (float)Math.cos(ar) * (base + L + 0.8F);
               float fy = (float)Math.sin(ar) * (base + L + 0.8F);
               g.push();
               g.rotX(-42.0F * Math.max(0.0F, (float)Math.sin(ar)));
               g.translate(fx, fy + Cos3Geo.sin(t * 2.4F + i) * 0.35F, 0.0F);
               g.rotY(t * 90.0F + i * 40);
               g.color(Cos3Geo.argb(230, c));
               Cos3Geo.gem(g, "c3_crystal", 0.35F, 0.5F, 0.5F, 4);
               g.pop();
            }
         }

         g.glow(false);
         Cos3Geo.halo(g, 0.0F, 0.0F, 0.0F, 1.8F, 1891166463);
         g.pop();
      }

      g.color(-1);
   }
}
