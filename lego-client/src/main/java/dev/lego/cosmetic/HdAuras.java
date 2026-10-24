package dev.lego.cosmetic;

final class HdAuras {
   private HdAuras() {
   }

   private static float[] p(float x, float y, float z) {
      return new float[]{x, y, z};
   }

   static void groundGlow(G g, int argb, float r) {
      boolean gl = g.glow;
      int c = g.color;
      g.glow(true).color(argb);
      g.push();
      g.translate(0.0F, -23.9F, 0.0F);
      g.rotX(-90.0F);
      g.plane("hd_softglow", 0.0F, 0.0F, r * 2.0F, r * 2.0F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.glow(gl).color(c);
   }

   static float life(float ph) {
      return Cos3Geo.smooth(ph * 5.0F) * Cos3Geo.smooth((1.0F - ph) * 2.2F);
   }

   static void fire(G g, Cos.A a) {
      float t = a.time;
      groundGlow(g, -1426101734, 11.0F);
      int n = 12;

      for (int i = 0; i < n; i++) {
         float ph = Cos3Geo.fract(t * 1.1F + i * 0.37F);
         double ang = (Math.PI * 2) * i / n + t * 0.4F;
         float r = 7.4F + Cos3Geo.sin(i * 1.7) * 1.0F - ph * 1.2F;
         float s = life(ph);
         if (!(s < 0.03F)) {
            g.push();
            g.translate(Cos3Geo.cos(ang) * r, -24.0F + ph * 10.0F, Cos3Geo.sin(ang) * r);
            g.rotZ(Cos3Geo.sin(t * 5.0F + i) * 10.0F);
            g.rotX(Cos3Geo.cos(t * 4.0F + i) * 8.0F);
            Cos3Models.flame(g, 5.2F * s, 1.25F * s, -38374, -5990);
            g.pop();
         }
      }

      for (int ix = 0; ix < 14; ix++) {
         float ph = Cos3Geo.fract(t * 0.6F + ix * 0.29F);
         double ang = ix * 2.1 + t * 0.8F;
         float x = Cos3Geo.cos(ang) * (6.0F + ph * 3.0F);
         float y = -22.0F + ph * 26.0F;
         float z = Cos3Geo.sin(ang) * (6.0F + ph * 3.0F);
         float s = 1.0F - ph;
         Cos3Models.sparkle(g, x, y, z, 0.35F * s + 0.1F, Cos3Geo.alpha(s, 16756810));
         if (ix % 2 == 0) {
            Cos3Geo.halo(g, x, y, z, 1.8F * s, Cos3Geo.alpha(0.5 * s, 16742954));
         }
      }
   }

   static void frost(G g, Cos.A a) {
      float t = a.time;
      groundGlow(g, -2005075969, 11.0F);

      for (int i = 0; i < 9; i++) {
         double ang = (Math.PI * 2) * i / 9.0 + 0.3;
         float r = 7.8F + i % 3 * 0.9F;
         float grow = 0.75F + 0.25F * Cos3Geo.sin(t * 1.3F + i);
         g.push();
         g.translate(Cos3Geo.cos(ang) * r, -24.2F, Cos3Geo.sin(ang) * r);
         g.rotY((float)Math.toDegrees(-ang));
         g.rotZ(-14 + i % 2 * 28);
         g.glow(true).color(-576131841);
         Cos3Geo.shard(g, "c3_crystal", 0.6F + i % 3 * 0.15F, (2.6F + i % 4 * 0.8F) * grow);
         g.rotZ(30.0F);
         Cos3Geo.shard(g, "c3_crystal", 0.35F, 1.6F * grow);
         g.glow(false);
         g.pop();
      }

      for (int i = 0; i < 10; i++) {
         double ang = (Math.PI * 2) * i / 10.0 + t * 0.7F;
         float ph = Cos3Geo.fract(t * 0.18F + i * 0.41F);
         float s = life(ph);
         if (!(s < 0.03F)) {
            float r = 8.5F + Cos3Geo.sin(i * 2.3) * 0.8F;
            g.push();
            g.translate(Cos3Geo.cos(ang) * r, -23.0F + ph * 26.0F, Cos3Geo.sin(ang) * r);
            g.rotY((float)Math.toDegrees(-ang) + 90.0F + t * 40.0F);
            g.rotZ(t * 60.0F + i * 30);
            g.glow(true).color(-1509121);
            XmasGeo.flake(g, "white", (1.1F + i % 3 * 0.35F) * s, 0.08F);
            g.glow(false);
            g.pop();
         }
      }

      for (int ix = 0; ix < 10; ix++) {
         double ang = ix * 2.4 - t * 0.4;
         float tw = 0.5F + 0.5F * Cos3Geo.sin(t * 4.0F + ix * 1.3F);
         Cos3Models.sparkle(
            g, Cos3Geo.cos(ang) * (6 + ix % 4), -22.0F + ix * 2.3F % 24.0F, Cos3Geo.sin(ang) * (6 + ix % 4), 0.3F + 0.3F * tw, Cos3Geo.alpha(tw, 16777215)
         );
      }
   }

   static void hearts(G g, Cos.A a) {
      float t = a.time;
      float[][] heart = HdGeo.heart(1.4F, 20);
      int n = 8;
      int[] cols = new int[]{-54678, -38246, -1568710};

      for (int i = 0; i < n; i++) {
         float ph = Cos3Geo.fract(t * 0.3F + (float)i / n);
         float s = life(ph);
         if (!(s < 0.03F)) {
            double ang = (Math.PI * 2) * i / n + t * 0.6F;
            float r = 8.5F + Cos3Geo.sin(t * 1.5F + i) * 0.8F;
            float beat = 1.0F + 0.14F * (float)Math.pow(Math.max(0.0, Math.sin(t * 7.0F + i)), 4.0);
            g.push();
            g.translate(Cos3Geo.cos(ang) * r, -22.0F + ph * 26.0F, Cos3Geo.sin(ang) * r);
            g.rotY((float)Math.toDegrees(-ang) + 90.0F + Cos3Geo.sin(t * 2.0F + i) * 25.0F);
            g.rotZ(Cos3Geo.sin(t * 3.0F + i) * 12.0F);
            g.scale(s * beat * (1.0F + i % 3 * 0.2F));
            g.color(cols[i % 3]);
            HdGeo.puff(g, "hd_gloss", heart, 1.1F, 0.5F, 2);
            g.pop();
            Cos3Geo.halo(g, Cos3Geo.cos(ang) * r, -22.0F + ph * 26.0F, Cos3Geo.sin(ang) * r, 4.5F * s, Cos3Geo.alpha(0.3 * s, 16730762));
         }
      }

      g.color(-1);
   }

   static void orbit(G g, Cos.A a) {
      float t = a.time;
      int[] cols = new int[]{-3703297, -8587265, -3920};

      for (int i = 0; i < 3; i++) {
         g.push();
         g.translate(0.0F, -9.0F, 0.0F);
         g.rotX(25 + i * 55);
         g.rotY(t * (70 + i * 25) + i * 120);
         g.push();
         g.translate(11.0F, 0.0F, 0.0F);
         g.rotY(t * 120.0F);
         g.rotX(90.0F);
         g.glow(true).color(cols[i]);
         XmasGeo.star(g, "white", 1.3F, 0.45F, 5);
         g.glow(false);
         g.pop();
         Cos3Geo.halo(g, 11.0F, 0.0F, 0.0F, 5.5F, Cos3Geo.alpha(0.55, cols[i] & 16777215));

         for (int k = 1; k < 9; k++) {
            g.push();
            g.rotY(-k * 6.5F);
            float f = 1.0F - k / 9.0F;
            Cos3Models.sparkle(g, 11.0F, 0.0F, 0.0F, 0.55F * f, Cos3Geo.alpha(f, cols[i] & 16777215));
            if (k % 2 == 0) {
               Cos3Geo.halo(g, 11.0F, 0.0F, 0.0F, 3.2F * f, Cos3Geo.alpha(0.35 * f, cols[i] & 16777215));
            }

            g.pop();
         }

         g.pop();
      }

      g.color(-1);
   }

   static void storm(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(0.0F, 5.5F, 0.0F);
      g.rotY(t * 18.0F);

      for (int i = 0; i < 12; i++) {
         double ang = (Math.PI * 2) * i / 12.0;
         float r = 8.0F + i % 2 * 1.2F;
         float bob = Cos3Geo.sin(t * 1.4F + i) * 0.4F;
         g.color(-11906456);
         Geo.ellipsoid(g, "hd_fluff", Cos3Geo.cos(ang) * r, bob + i % 3 * 0.5F, Cos3Geo.sin(ang) * r, 2.3F, 1.7F, 2.1F, 10, 6);
         g.color(-9800560);
         Geo.ellipsoid(g, "hd_fluff", Cos3Geo.cos(ang + 0.25) * (r - 0.6F), bob + 1.3F, Cos3Geo.sin(ang + 0.25) * (r - 0.6F), 1.6F, 1.3F, 1.6F, 8, 5);
      }

      float fl = (float)Math.pow(Math.max(0.0F, Cos3Geo.sin(t * 11.0F) * Cos3Geo.sin(t * 3.7F)), 3.0);
      g.pop();
      Cos3Geo.halo(g, 0.0F, 5.5F, 0.0F, 18.0F, Cos3Geo.alpha(0.35 * fl, 10152191));

      for (int i = 0; i < 5; i++) {
         float cycle = Cos3Geo.fract(t * 1.3F + i * 0.29F);
         if (!(cycle > 0.36F)) {
            double ang = (Math.PI * 2) * ((i * 0.61F + Math.floor(t * 1.3F + i * 0.37F) * 0.29F) % 1.0);
            float r = 7.5F + i % 2 * 1.2F;
            float[] top = p(Cos3Geo.cos(ang) * (r + 1.0F), 4.5F, Cos3Geo.sin(ang) * (r + 1.0F));
            float[] bot = p(Cos3Geo.cos(ang + 0.3) * (r + 1.5F), -24.0F, Cos3Geo.sin(ang + 0.3) * (r + 1.5F));
            float k = 1.0F - cycle / 0.36F;
            Cos3Geo.arc(g, top, bot, 3.4F, 0.22F * (0.5F + k), i * 13 + 5, t, Cos3Geo.alpha(0.5 + 0.5 * k, 10152191));
            Cos3Geo.halo(g, bot[0], -23.5F, bot[2], 6.0F * k, Cos3Geo.alpha(0.6 * k, 10152191));
         }
      }

      for (int ix = 0; ix < 18; ix++) {
         float ph = Cos3Geo.fract(t * 1.8F + ix * 0.137F);
         double ang = ix * 2.39;
         float r = 5.0F + ix % 4 * 1.3F;
         g.glow(true).color(-2003117825);
         g.push();
         g.translate(Cos3Geo.cos(ang) * (r + 2.0F), 4.0F - ph * 28.0F, Cos3Geo.sin(ang) * (r + 2.0F));
         Geo.ellipsoid(g, "white", 0.0F, 0.0F, 0.0F, 0.07F, 0.6F, 0.07F, 4, 2);
         g.pop();
         g.glow(false);
      }

      g.color(-1);
   }

   static void petal(G g, int col, float s) {
      float[][] o = new float[][]{
         {0.0F, -1.0F * s},
         {0.55F * s, -0.55F * s},
         {0.62F * s, 0.2F * s},
         {0.3F * s, 0.9F * s},
         {0.08F * s, 0.75F * s},
         {0.0F, 1.0F * s},
         {-0.08F * s, 0.75F * s},
         {-0.3F * s, 0.9F * s},
         {-0.62F * s, 0.2F * s},
         {-0.55F * s, -0.55F * s}
      };
      int oc = g.color;
      g.color(col);

      for (int k = -1; k <= 1; k += 2) {
         g.push();
         g.rotY(k * 12);
         float[][] half = new float[6][];
         int j = 0;
         half[j++] = new float[]{0.0F, -1.0F * s};

         for (int i = 1; i <= 4; i++) {
            half[j++] = new float[]{k * o[i][0], o[i][1]};
         }

         half[j++] = new float[]{0.0F, 1.0F * s};
         HdGeo.puff(g, "hd_petal", half, 0.08F, 0.03F, 1, new float[]{0.0F, 0.0F, 1.0F, 1.0F}, new float[]{0.4F, 0.4F, 0.6F, 0.6F});
         g.pop();
      }

      g.color(oc);
   }

   static void sakura(G g, Cos.A a) {
      float t = a.time;
      int n = 16;
      int[] cols = new int[]{-18224, -11036, -25922};

      for (int i = 0; i < n; i++) {
         float ph = Cos3Geo.fract(t * 0.22F + i * 0.137F);
         float s = life(ph);
         if (!(s < 0.03F)) {
            double ang = i * 2.39 + t * 0.9 + ph * 3.0F;
            float r = 6.0F + i % 4 * 1.4F;
            g.push();
            g.translate(Cos3Geo.cos(ang) * r, 6.0F - ph * 30.0F, Cos3Geo.sin(ang) * r);
            g.rotY(t * 90.0F + i * 40);
            g.rotX(t * 70.0F + i * 25);
            petal(g, cols[i % 3], 1.7F * s);
            g.pop();
         }
      }

      for (int ix = 0; ix < 3; ix++) {
         float ph = Cos3Geo.fract(t * 0.12F + ix / 3.0F);
         float s = life(ph);
         if (!(s < 0.05F)) {
            double ang = ix * 2.1 + t * 0.5;
            g.push();
            g.translate(Cos3Geo.cos(ang) * 8.5F, 2.0F - ph * 26.0F, Cos3Geo.sin(ang) * 8.5F);
            g.rotX(40.0F + t * 30.0F);
            g.rotZ(t * 50.0F + ix * 90);
            g.scale(s);
            HdHats.flower(g, 5, 1.3F, -16168, -8086, 30.0F);
            g.pop();
         }
      }

      g.color(-1);
   }

   static void note(G g, boolean dbl) {
      Geo.ellipsoid(g, "hd_gloss", 0.0F, 0.0F, 0.0F, 0.75F, 0.52F, 0.42F, 10, 6);
      Geo.tube(g, "hd_plastic", p(0.62F, 0.1F, 0.0F), p(0.62F, 3.0F, 0.0F), 0.12F, 0.12F, 5);
      if (dbl) {
         Geo.ellipsoid(g, "hd_gloss", 2.0F, 0.4F, 0.0F, 0.75F, 0.52F, 0.42F, 10, 6);
         Geo.tube(g, "hd_plastic", p(2.62F, 0.5F, 0.0F), p(2.62F, 3.4F, 0.0F), 0.12F, 0.12F, 5);
         Cos3Geo.bbox(g, "hd_plastic", 0.5F, 2.6F, -0.14F, 2.74F, 3.2F, 0.14F, 0.06F);
      } else {
         float[][] fl = new float[][]{{0.62F, 3.0F}, {0.62F, 2.2F}, {1.5F, 1.4F}, {1.7F, 1.8F}};
         HdGeo.puff(g, "hd_plastic", fl, 0.2F, 0.08F, 1);
      }
   }

   static void music(G g, Cos.A a) {
      float t = a.time;
      int[] cols = new int[]{-9772801, -38192, -7606, -7667862};

      for (int i = 0; i < 9; i++) {
         float ph = Cos3Geo.fract(t * 0.35F + i / 9.0F);
         float s = life(ph);
         if (!(s < 0.03F)) {
            double ang = (Math.PI * 2) * i / 9.0 + t * 0.5;
            float r = 8.0F + Cos3Geo.sin(ph * 6.0F + i) * 1.0F;
            float bounce = (float)Math.pow(Math.abs(Cos3Geo.sin(t * 4.0F + i)), 3.0) * 0.12F;
            g.push();
            g.translate(Cos3Geo.cos(ang) * r, -18.0F + ph * 24.0F, Cos3Geo.sin(ang) * r);
            g.rotY((float)Math.toDegrees(-ang) + 90.0F);
            g.rotZ(Cos3Geo.sin(t * 5.0F + i) * 15.0F);
            g.scale(s * (1.0F + bounce));
            g.glow(true).color(cols[i % 4]);
            note(g, i % 3 == 0);
            g.glow(false);
            g.pop();
            Cos3Geo.halo(g, Cos3Geo.cos(ang) * r, -17.0F + ph * 24.0F, Cos3Geo.sin(ang) * r, 4.0F * s, Cos3Geo.alpha(0.3 * s, cols[i % 4] & 16777215));
         }
      }

      g.color(-1);
   }

   static void bubbles(G g, Cos.A a) {
      float t = a.time;

      for (int i = 0; i < 12; i++) {
         float ph = Cos3Geo.fract(t * 0.2F + i * 0.173F);
         float grow = Cos3Geo.smooth(ph * 6.0F);
         boolean pop = ph > 0.94F;
         double ang = i * 2.1 + ph * 2.0F;
         float r = 6 + i % 3 * 2;
         float s = (1.2F + i % 4 * 0.45F) * grow;
         float wob = 1.0F + 0.06F * Cos3Geo.sin(t * 6.0F + i);
         float x = Cos3Geo.cos(ang) * r + Cos3Geo.sin(t * 2.0F + i) * 0.8F;
         float y = -23.0F + ph * 30.0F;
         float z = Cos3Geo.sin(ang) * r;
         if (pop) {
            float k = (ph - 0.94F) / 0.06F;

            for (int d = 0; d < 6; d++) {
               double da = (Math.PI * 2) * d / 6.0;
               Cos3Models.sparkle(
                  g, x + Cos3Geo.cos(da) * s * (1.0F + k), y + Cos3Geo.sin(da) * s * (1.0F + k), z, 0.25F * (1.0F - k), Cos3Geo.alpha(1.0F - k, 14546175)
               );
            }
         } else {
            g.push();
            g.translate(x, y, z);
            g.scale(wob, 1.0F / wob, wob);
            g.color(-1);
            g.sphere("hd_bubble", s, 14, 8, 0.0F, 0.0F, 1.0F, 1.0F);
            g.pop();
         }
      }

      g.color(-1);
   }

   static void rainbowRing(G g, Cos.A a) {
      float t = a.time;

      for (int ring = 0; ring < 2; ring++) {
         g.push();
         g.translate(0.0F, ring == 0 ? -12.0F + Cos3Geo.sin(t * 1.5F) * 2.0F : -20.0F + Cos3Geo.sin(t * 1.5F + 2.0F) * 1.5F, 0.0F);
         g.rotX(ring == 0 ? 8.0F * Cos3Geo.sin(t) : -6.0F);
         float R = ring == 0 ? 10.0F : 8.5F;
         int seg = 40;

         for (int i = 0; i < seg; i++) {
            double a0 = (Math.PI * 2) * i / seg;
            double a1 = (Math.PI * 2) * (i + 1) / seg;
            g.glow(true).color(Geo.hue((float)i / seg + t * 0.2F * (ring == 0 ? 1 : -1), 0.62, 1.0));
            Geo.tube(g, "white", p(Cos3Geo.cos(a0) * R, 0.0F, Cos3Geo.sin(a0) * R), p(Cos3Geo.cos(a1) * R, 0.0F, Cos3Geo.sin(a1) * R), 0.32F, 0.32F, 6);
         }

         g.color(Cos3Geo.alpha(0.3, 16777215));
         g.torus("hd_softglow", R, 1.1F, 32, 4, 0.1F, 0.1F, 0.9F, 0.9F);
         g.glow(false);
         g.pop();
      }

      for (int i = 0; i < 8; i++) {
         double an = (Math.PI * 2) * i / 8.0 + t * 1.2;
         float tw = 0.5F + 0.5F * Cos3Geo.sin(t * 4.0F + i);
         Cos3Models.sparkle(
            g,
            Cos3Geo.cos(an) * 10.0F,
            -12.0F + Cos3Geo.sin(t * 1.5F) * 2.0F + 0.6F,
            Cos3Geo.sin(an) * 10.0F,
            0.3F + 0.4F * tw,
            Geo.hue(i / 8.0 + t * 0.2, 0.3, 1.0)
         );
      }

      g.color(-1);
   }

   static void voidAura(G g, Cos.A a) {
      float t = a.time;
      groundGlow(g, -1433785601, 10.0F);
      g.push();
      g.translate(0.0F, -23.7F, 0.0F);
      g.rotY(-t * 50.0F);
      g.glow(true).color(-860861697);

      for (int k = 0; k < 3; k++) {
         g.push();
         g.rotY(k * 120);
         int n = 16;
         float[][] sp = new float[n + 1][];
         float[] rr = new float[n + 1];
         float[] rz = new float[n + 1];

         for (int i = 0; i <= n; i++) {
            float f = (float)i / n;
            double an = f * Math.PI * 1.3;
            float r = 9.5F - f * 5.5F;
            sp[i] = p(Cos3Geo.cos(an) * r, 0.1F + f * 0.2F, Cos3Geo.sin(an) * r);
            rr[i] = 0.9F * (1.0F - f) + 0.1F;
            rz[i] = 0.06F;
         }

         XmasGeo.sweep(g, "c3_beam", sp, rr, rz, null, 4, 1.0F);
         g.pop();
      }

      g.glow(false);
      g.pop();

      for (int i = 0; i < 12; i++) {
         float ph = Cos3Geo.fract(t * 0.35F + i / 12.0F);
         double ang = i * 2.39 - ph * 5.0F - t * 0.4;
         float r = 11.0F * (1.0F - ph) + 2.0F;
         float s = Cos3Geo.smooth(ph * 3.0F) * (1.0F - ph * 0.6F);
         float x = Cos3Geo.cos(ang) * r;
         float y = -22.0F + ph * 20.0F + Cos3Geo.sin(i) * 2.0F;
         float z = Cos3Geo.sin(ang) * r;
         g.color(-16120302);
         Cos3Geo.ball(g, "hd_gloss", x, y, z, 0.55F * s + 0.05F, 8);
         Cos3Geo.halo(g, x, y, z, 2.8F * s, Cos3Geo.alpha(0.6 * s, 11553535));
      }

      g.color(-1);
   }

   static void sparkle(G g, Cos.A a) {
      float t = a.time;
      int[] cols = new int[]{-1, -5984, -4658945, -18200};

      for (int i = 0; i < 20; i++) {
         float ph = Cos3Geo.fract(t * 0.25F + i * 0.0913F);
         double ang = i * 2.39 + t * 0.3;
         float r = 5 + i * 7 % 5;
         float tw = 0.4F + 0.6F * Math.abs(Cos3Geo.sin(t * 3.0F + i * 1.7F));
         float s = life(ph) * tw;
         if (!(s < 0.03F)) {
            int c = cols[i % 4];
            float x = Cos3Geo.cos(ang) * r;
            float y = 4.0F - ph * 28.0F;
            float z = Cos3Geo.sin(ang) * r;
            g.push();
            g.translate(x, y, z);
            g.rotY(t * 90.0F + i * 30);
            g.glow(true).color(c);
            Cos3Geo.gem(g, "white", 0.32F * s, 1.4F * s, 1.4F * s, 4);
            g.rotX(90.0F);
            Cos3Geo.gem(g, "white", 0.32F * s, 0.95F * s, 0.95F * s, 4);
            g.glow(false);
            g.pop();
            if (i % 2 == 0) {
               Cos3Geo.halo(g, x, y, z, 2.4F * s, Cos3Geo.alpha(0.4 * s, c & 16777215));
            }
         }
      }

      g.color(-1);
   }

   static void leaves(G g, Cos.A a) {
      float t = a.time;
      int[] cols = new int[]{-1531856, -1548246, -3657698, -4667334};

      for (int i = 0; i < 14; i++) {
         float ph = Cos3Geo.fract(t * 0.18F + i * 0.157F);
         float s = life(ph);
         if (!(s < 0.03F)) {
            double ang = i * 2.2 + t * 0.7 + ph * 2.5;
            float r = 7.0F + i % 3 * 1.5F;
            g.push();
            g.translate(Cos3Geo.cos(ang) * r, 4.0F - ph * 28.0F, Cos3Geo.sin(ang) * r);
            g.rotY(t * 80.0F + i * 50);
            g.rotZ(Cos3Geo.sin(t * 2.0F + i) * 40.0F);
            g.rotX(Cos3Geo.sin(t * 1.3F + i) * 30.0F);
            g.scale(s * 1.6F);
            g.push();
            g.translate(-1.1F, 0.0F, 0.0F);
            HdHats.leaf(g, 2.4F, cols[i % 4]);
            g.pop();
            g.color(-9813984);
            Geo.tube(g, "hd_wood", p(-1.1F, 0.0F, 0.0F), p(-1.8F, -0.2F, 0.0F), 0.07F, 0.05F, 3);
            g.pop();
         }
      }

      g.color(-1);
   }

   static void runes(G g, Cos.A a) {
      float t = a.time;
      float pulse = 0.8F + 0.2F * Cos3Geo.sin(t * 2.5F);
      g.push();
      g.translate(0.0F, -23.8F, 0.0F);
      g.rotY(t * 25.0F);
      g.glow(true).color(Cos3Geo.alpha(pulse, 13148415));
      g.torus("white", 10.5F, 0.12F, 48, 3, 0.0F, 0.0F, 1.0F, 1.0F);
      g.torus("white", 8.2F, 0.09F, 40, 3, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(Cos3Geo.alpha(0.95 * pulse, 16777215));
      Cos3Geo.lathe(g, "hd_rune", new float[]{9.4F, 9.4F}, new float[]{0.05F, 1.2F}, 40, 0.0F, 1.0F, 3.0F, 0.0F);
      g.color(Cos3Geo.alpha(0.25 * pulse, 10119935));
      g.ring("hd_softglow", 7.5F, 11.5F, 0.02F, 40, 0.1F, 0.5F, 0.9F, 0.5F);
      g.color(Cos3Geo.alpha(0.8 * pulse, 13148415));

      for (int k = 0; k < 6; k++) {
         double a0 = (Math.PI * 2) * k / 6.0;
         double a1 = (Math.PI * 2) * (k + 2) / 6.0;
         Geo.tube(
            g, "white", p(Cos3Geo.cos(a0) * 8.2F, 0.05F, Cos3Geo.sin(a0) * 8.2F), p(Cos3Geo.cos(a1) * 8.2F, 0.05F, Cos3Geo.sin(a1) * 8.2F), 0.07F, 0.07F, 3
         );
      }

      g.glow(false);
      g.pop();

      for (int i = 0; i < 6; i++) {
         float ph = Cos3Geo.fract(t * 0.3F + i / 6.0F);
         float s = life(ph);
         if (!(s < 0.03F)) {
            double an = (Math.PI * 2) * i / 6.0 + t * 0.4;
            g.push();
            g.translate(Cos3Geo.cos(an) * 9.4F, -22.5F + ph * 16.0F, Cos3Geo.sin(an) * 9.4F);
            g.rotY((float)Math.toDegrees(-an) + 90.0F + t * 40.0F);
            g.scale(s * 1.4F);
            g.color(-12962744);
            Cos3Geo.bbox(g, "hd_steel", -0.75F, -1.0F, -0.25F, 0.75F, 1.0F, 0.25F, 0.18F);
            g.glow(true).color(-2576129);
            g.push();
            g.translate(0.0F, 0.0F, 0.27F);
            Cos3Geo.cbox(g, "white", 0.0F, 0.1F, 0.0F, 0.14F, 1.3F, 0.04F, 0.02F);
            g.rotZ(40.0F);
            Cos3Geo.cbox(g, "white", 0.25F, 0.25F, 0.0F, 0.12F, 0.8F, 0.04F, 0.02F);
            g.pop();
            g.glow(false);
            g.pop();
         }
      }

      g.color(-1);
   }

   static void brick(G g, int col, int w) {
      int oc = g.color;
      g.color(col);
      float hw = w * 0.8F;
      Cos3Geo.bbox(g, "hd_brick", -hw, -0.7F, -0.8F, hw, 0.7F, 0.8F, 0.1F);

      for (int i = 0; i < w; i++) {
         for (int k = 0; k < 2; k++) {
            g.push();
            g.translate(-hw + 0.8F + i * 1.6F, 0.7F, k == 0 ? -0.4F : 0.4F);
            Cos3Geo.lathe(g, "hd_brick", new float[]{0.3F, 0.3F, 0.26F, 0.0F}, new float[]{-0.02F, 0.3F, 0.36F, 0.37F}, 8);
            g.pop();
         }
      }

      g.color(oc);
   }

   static void bricks(G g, Cos.A a) {
      float t = a.time;
      int[] cols = new int[]{-1900533, -13053, -16749385, -16743125};

      for (int i = 0; i < 8; i++) {
         double an = (Math.PI * 2) * i / 8.0 + t * 0.8;
         float y = -10.0F + Cos3Geo.sin(t * 1.6F + i * 0.8F) * 3.0F;
         g.push();
         g.translate(Cos3Geo.cos(an) * 10.0F, y, Cos3Geo.sin(an) * 10.0F);
         g.rotY(t * 60.0F + i * 45);
         g.rotX(Cos3Geo.sin(t + i) * 25.0F);
         g.scale(1.45F);
         brick(g, cols[i % 4], i % 3 == 0 ? 1 : 2);
         g.pop();
      }

      g.color(-1);
   }
}
