package dev.lego.cosmetic;

final class HdFace {
   private static final String[] PIXEL = new String[]{
      "XXXXXXXXXXXXXXXXXXX", "XXXXXXXXXXXXXXXXXXX", "..XWWXXXX...XWWXXXX", "..XWXXXXX...XWXXXXX", "...XXXXX.....XXXXX.", "....XXX.......XXX.."
   };

   private HdFace() {
   }

   private static float[] p(float x, float y, float z) {
      return new float[]{x, y, z};
   }

   static void temples(G g, String tex, int col, float y, float x, float zFront) {
      int oc = g.color;
      g.color(col);

      for (int s = -1; s <= 1; s += 2) {
         float[][] pts = new float[][]{
            {s * x, y, zFront}, {s * 4.25F, y + 0.05F, zFront - 0.8F}, {s * 4.3F, y + 0.1F, 0.0F}, {s * 4.3F, y - 0.1F, -2.8F}, {s * 4.22F, y - 0.9F, -3.6F}
         };
         float[] rr = new float[]{0.2F, 0.18F, 0.17F, 0.17F, 0.15F};
         Cos3Geo.limb(g, tex, pts, rr, 5);
         Cos3Geo.cbox(g, tex, s * (x - 0.05F), y, zFront - 0.1F, 0.55F, 0.6F, 0.5F, 0.12F);
      }

      g.color(oc);
   }

   static void lensFrame(
      G g, float[][] outline, float cx, float cy, float z, String frameTex, int frameCol, float fr, String lensTex, int lensCol, boolean lensGlow
   ) {
      g.push();
      g.translate(cx, cy, z);
      int oc = g.color;
      g.color(frameCol);
      HdGeo.loop(g, frameTex, outline, fr, fr * 1.15F, 2.6F, 6);
      g.color(lensCol).glow(lensGlow);
      HdGeo.puff(g, lensTex, HdGeo.offset(HdGeo.ccw(outline), -fr * 0.2F), 0.2F, 0.08F, 1);
      g.glow(false).color(oc);
      g.pop();
   }

   static void sunglasses(G g, Cos.A a) {
      float[][] lens = new float[22][];

      for (int i = 0; i < 22; i++) {
         double an = (Math.PI * 2) * i / 22.0;
         float x = HdGeo.cos(an) * 1.85F;
         float y = HdGeo.sin(an) * 1.25F;
         if (y < 0.0F) {
            y *= 1.25F;
            x *= 1.0F - 0.18F * (float)Math.pow(-HdGeo.sin(an), 2.0) * (HdGeo.cos(an) > 0.0F ? 0.4F : 1.0F);
         }

         lens[i] = new float[]{x, y};
      }

      for (int s = -1; s <= 1; s += 2) {
         float[][] l = new float[lens.length][];

         for (int i = 0; i < lens.length; i++) {
            l[i] = new float[]{lens[i][0] * s, lens[i][1]};
         }

         lensFrame(g, l, s * 2.1F, 3.55F, 4.45F, "hd_gold", -1523094, 0.17F, "hd_lens", -1, false);
      }

      g.color(-1523094);
      Geo.chain(g, "hd_gold", new float[][]{{-0.45F, 4.55F, 4.45F}, {0.0F, 4.7F, 4.5F}, {0.45F, 4.55F, 4.45F}}, new float[]{0.13F, 0.13F, 0.13F}, 5);
      Geo.chain(g, "hd_gold", new float[][]{{-0.35F, 3.95F, 4.5F}, {0.0F, 3.75F, 4.55F}, {0.35F, 3.95F, 4.5F}}, new float[]{0.1F, 0.1F, 0.1F}, 5);
      g.color(-571411206);

      for (int s = -1; s <= 1; s += 2) {
         Geo.ellipsoid(g, "hd_glass", s * 0.55F, 2.75F, 4.2F, 0.18F, 0.32F, 0.14F, 5, 3);
      }

      temples(g, "hd_gold", -1523094, 4.2F, 3.9F, 4.4F);
      g.color(-1);
   }

   static void pixelGlasses(G g, Cos.A a) {
      float px = 0.46F;
      int w = PIXEL[0].length();
      float x0 = -w * px / 2.0F;
      float y0 = 4.95F;
      float slide = a.preview ? 0.0F : Math.max(0.0F, 1.0F - a.time * 0.0F);
      g.push();
      g.translate(0.0F, slide * 0.0F, 4.3F);

      for (int r = 0; r < PIXEL.length; r++) {
         for (int c = 0; c < w; c++) {
            char ch = PIXEL[r].charAt(c);
            if (ch != '.') {
               float x = -(x0 + c * px + px / 2.0F);
               float y = y0 - r * px - px / 2.0F;
               boolean white = ch == 'W';
               g.color(white ? -723724 : -15856110);
               g.box(
                  white ? "hd_plastic" : "hd_rubber",
                  x - px / 2.0F + 0.01F,
                  y - px / 2.0F + 0.01F,
                  -0.2F,
                  x + px / 2.0F - 0.01F,
                  y + px / 2.0F - 0.01F,
                  white ? 0.26F : 0.2F,
                  0.0F,
                  0.0F,
                  1.0F,
                  1.0F
               );
            }
         }
      }

      g.pop();
      temples(g, "hd_rubber", -15856110, 4.5F, 4.15F, 4.3F);
      g.color(-1);
   }

   static void heartGlasses(G g, Cos.A a) {
      float beat = (float)Math.pow(Math.max(0.0, Math.sin(a.time * 6.0F)), 4.0);
      float[][] heart = HdGeo.heart(2.0F, 28);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 2.15F, 3.55F, 4.5F);
         g.scale(1.0F + 0.08F * beat, 1.0F + 0.08F * beat, 1.0F);
         g.rotZ(s * -6);
         g.color(-54678);
         HdGeo.loop(g, "hd_plastic", heart, 0.3F, 0.32F, 2.4F, 6);
         g.color(-855676262);
         HdGeo.puff(g, "hd_glass", HdGeo.offset(HdGeo.ccw(heart), -0.1F), 0.2F, 0.08F, 1);
         g.pop();
      }

      g.color(-54678);
      Geo.chain(g, "hd_plastic", new float[][]{{-0.55F, 4.35F, 4.5F}, {0.0F, 4.55F, 4.55F}, {0.55F, 4.35F, 4.5F}}, new float[]{0.16F, 0.16F, 0.16F}, 5);
      temples(g, "hd_plastic", -54678, 4.3F, 4.05F, 4.45F);
      float ph = Cos3Geo.fract(a.time * 1.0F);
      if (ph < 0.8F) {
         float s = HdGeo.sin(ph / 0.8F * Math.PI);
         g.push();
         g.translate(4.6F + ph * 1.5F, 5.4F + ph * 3.0F, 4.6F);
         g.scale(0.4F * s);
         g.glow(true).color(-46454);
         HdGeo.puff(g, "hd_plastic", HdGeo.heart(1.4F, 16), 0.9F, 0.4F, 1);
         g.glow(false);
         g.pop();
      }

      g.color(-1);
   }

   static void starGlasses(G g, Cos.A a) {
      float t = a.time;
      float[][] star = HdGeo.star(2.35F, 1.1F, 5);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 2.25F, 3.6F, 4.5F);
         g.rotZ(s * 8);
         g.color(-14278);
         HdGeo.loop(g, "hd_gold", star, 0.26F, 0.3F, 2.4F, 6);
         g.color(-14017942);
         HdGeo.puff(g, "hd_lens", HdGeo.offset(HdGeo.ccw(star), -0.1F), 0.2F, 0.08F, 1);
         g.pop();
      }

      g.color(-14278);
      Geo.chain(g, "hd_gold", new float[][]{{-0.55F, 4.2F, 4.5F}, {0.0F, 4.4F, 4.55F}, {0.55F, 4.2F, 4.5F}}, new float[]{0.15F, 0.15F, 0.15F}, 5);
      temples(g, "hd_gold", -14278, 4.1F, 4.1F, 4.45F);

      for (int i = 0; i < 3; i++) {
         float tw = Math.max(0.0F, HdGeo.sin(t * 3.0F + i * 2.1F));
         if (!(tw < 0.05F)) {
            float[] q = new float[]{i == 0 ? 2.25F : (i == 1 ? -2.25F : 4.2F), i == 2 ? 3.2F : 5.95F};
            Cos3Models.sparkle(g, q[0], q[1], 4.85F, 0.5F * tw, Cos3Geo.alpha(tw, 16777215));
            Cos3Geo.halo(g, q[0], q[1], 4.85F, 2.0F * tw, Cos3Geo.alpha(0.4 * tw, 16769146));
         }
      }

      g.color(-1);
   }

   static void monocle(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(-1.95F, 3.7F, 4.4F);
      g.color(-1);
      HdGeo.loop(g, "hd_gold", HdGeo.circle(1.5F, 1.5F, 24), 0.2F, 0.24F, 2.0F, 6);
      g.color(-1523094);
      g.push();
      g.translate(0.0F, 0.0F, -0.05F);
      HdGeo.loop(g, "hd_gold", HdGeo.circle(1.78F, 1.78F, 24), 0.09F, 0.14F, 2.0F, 4);
      g.pop();
      g.color(-1191182337);
      HdGeo.puff(g, "hd_glass", HdGeo.circle(1.46F, 1.46F, 20), 0.16F, 0.06F, 1);
      g.pop();
      float sw = Cos3Geo.sin(t * 1.8F) * 0.25F;
      float[] start = new float[]{-3.35F, 2.95F, 4.4F};
      float[] end = new float[]{-4.1F, -1.2F + sw * 0.2F, 3.6F + sw};
      float[][] pts = Cos3Geo.sag(start, end, 1.4F, 8);
      g.color(-1);
      Cos3Geo.chainPath(g, "hd_gold", pts, 0.34F);
      g.push();
      g.translate(end[0], end[1], end[2]);
      Cos3Geo.cbox(g, "hd_gold", 0.0F, -0.3F, 0.0F, 0.3F, 0.8F, 0.5F, 0.1F);
      g.pop();
      g.color(-1);
   }

   static void mustache(G g, Cos.A a) {
      float t = a.time;
      float tw = Math.max(0.0F, Cos3Geo.sin(t * 1.3F)) * 0.08F;
      g.push();
      g.translate(0.0F, 1.55F, 4.25F);
      g.rotZ(Cos3Geo.sin(t * 2.2F) * 1.2F);

      for (int s = -1; s <= 1; s += 2) {
         int n = 12;
         float[][] sp = new float[n + 1][];
         float[] rx = new float[n + 1];
         float[] rz = new float[n + 1];

         for (int i = 0; i <= n; i++) {
            float f = (float)i / n;
            double curl = f > 0.6F ? (f - 0.6F) / 0.4F * Math.PI * 1.25 : 0.0;
            float x = s * (0.1F + f * 3.4F * (1.0F + tw));
            float y = -0.05F - (float)Math.sin(f * Math.PI * 0.9) * 0.55F + f * f * 0.55F;
            if (curl > 0.0) {
               float cx = s * 2.14F;
               x = cx + s * (float)Math.sin(curl) * 0.75F * (1.0F + tw);
               y = -0.05F - (float)Math.sin(1.6964600329384882) * 0.55F + 0.19800001F + (1.0F - (float)Math.cos(curl)) * 0.6F;
            }

            sp[i] = p(x, y, 0.35F * (float)Math.sin(f * Math.PI) - f * 0.3F);
            float w = (float)Math.pow(Math.sin(Math.PI * Math.min(1.0, 0.15 + f * 0.95)), 0.7);
            rx[i] = Math.max(0.06F, 0.75F * w * (1.0F - 0.6F * f));
            rz[i] = Math.max(0.05F, 0.42F * w * (1.0F - 0.5F * f));
         }

         g.color(-11916778);
         XmasGeo.sweep(g, "hd_mustache", sp, rz, rx, null, 10, 2.0F);
      }

      g.pop();
      g.color(-1);
   }

   static void clownNose(G g, Cos.A a) {
      float t = a.time;
      float honk = (float)Math.pow(Math.max(0.0, Math.sin(t * 1.7)), 12.0);
      float sq = 1.0F + 0.06F * Cos3Geo.sin(t * 3.0F) - 0.12F * honk;
      g.push();
      g.translate(0.0F, 2.6F, 4.95F);
      g.scale(sq + honk * 0.2F, 1.0F / sq, sq);
      g.color(-1041890);
      g.sphere("hd_gloss", 1.3F, 20, 12, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      if (honk > 0.1F) {
         Cos3Models.sparkle(g, -0.55F, 3.4F, 6.1F, 0.5F * honk, Cos3Geo.alpha(honk, 16777215));
      }

      g.color(-1);
   }

   static void visor(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(0.0F, 3.7F, 0.0F);
      int n = 16;
      float[][] sp = new float[n + 1][];

      for (int i = 0; i <= n; i++) {
         double an = Math.PI * (0.1 + 0.8 * i / n);
         float[] q = Geo.sq(an, 4.85F, 5.0F);
         sp[i] = p(q[0], 0.0F, q[1]);
      }

      float[] rx = new float[n + 1];
      float[] rz = new float[n + 1];

      for (int i = 0; i <= n; i++) {
         rx[i] = 0.32F;
         rz[i] = 1.55F;
      }

      g.color(-14012872);
      XmasGeo.sweep(g, "hd_steel", sp, rx, rz, null, 8, 1.0F);

      for (int i = 0; i <= n; i++) {
         double an = Math.PI * (0.13 + 0.74 * i / n);
         float[] q = Geo.sq(an, 5.12F, 5.0F);
         sp[i] = p(q[0], 0.0F, q[1]);
         rx[i] = 0.08F;
         rz[i] = 1.12F;
      }

      g.glow(true).color(-1);
      XmasGeo.sweep(g, "hd_screen", sp, rx, rz, null, 4, 1.0F);
      float sweep = 0.5F + 0.5F * Cos3Geo.sin(t * 2.4F);
      double an = Math.PI * (0.2 + 0.6 * sweep);
      float[] q = Geo.sq(an, 5.25F, 5.0F);
      g.color(-50598);
      g.push();
      g.translate(q[0], 0.0F, q[1]);
      g.rotY((float)(-Math.toDegrees(an)) + 90.0F);
      Cos3Geo.bbox(g, "white", -0.35F, -0.85F, -0.05F, 0.35F, 0.85F, 0.12F, 0.08F);
      g.pop();
      Cos3Geo.halo(g, q[0], 0.0F, q[1] + 0.2F, 3.6F, Cos3Geo.alpha(0.55, 16726618));

      for (int s = -1; s <= 1; s += 2) {
         g.glow(false).color(-12960182);
         g.push();
         g.translate(s * 4.4F, 0.0F, -0.6F);
         g.rotZ(90.0F);
         Cos3Geo.lathe(g, "hd_chrome", new float[]{1.1F, 1.25F, 1.1F, 0.6F, 0.0F}, new float[]{-0.4F, -0.1F, 0.35F, 0.55F, 0.56F}, 14);
         g.pop();
         g.glow(true).color(-13965569);
         g.push();
         g.translate(s * 4.97F, 0.0F, -0.6F);
         g.rotZ(90.0F);
         g.torus("white", 0.62F, 0.1F, 14, 3, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
         g.glow(false);
      }

      g.glow(false).color(-1);
      g.pop();
   }

   static void masquerade(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(0.0F, 3.75F, 4.3F);

      for (int s = -1; s <= 1; s += 2) {
         float[][] eye = new float[20][];

         for (int i = 0; i < 20; i++) {
            double an = (Math.PI * 2) * i / 20.0;
            float x = HdGeo.cos(an) * 1.45F;
            float y = HdGeo.sin(an) * 0.85F;
            float out = x * s > 0.0F ? x * s / 1.45F : 0.0F;
            eye[i] = new float[]{s * 2.0F + x, y + out * out * 0.6F};
         }

         g.color(-9819464);
         HdGeo.loop(g, "hd_mask", eye, 0.55F, 0.18F, 3.0F, 6);
         g.color(-10134);
         g.push();
         g.translate(0.0F, 0.0F, 0.14F);
         HdGeo.loop(g, "hd_gold", HdGeo.offset(HdGeo.ccw(eye), 0.55F), 0.08F, 0.08F, 2.0F, 4);
         g.pop();
         int n = 10;
         float[][] sp = new float[n + 1][];
         float[] rr = new float[n + 1];
         float[] rz = new float[n + 1];

         for (int i = 0; i <= n; i++) {
            float f = (float)i / n;
            double an = f * Math.PI * 1.4;
            sp[i] = p(s * (3.6F + 1.1F * (float)Math.sin(an)), 0.9F + 1.1F * (1.0F - (float)Math.cos(an)) * 0.6F - f * 0.6F, -0.2F - f * 0.6F);
            rr[i] = Math.max(0.05F, 0.45F * (1.0F - f));
            rz[i] = 0.12F;
         }

         g.color(-9819464);
         XmasGeo.sweep(g, "hd_mask", sp, rr, rz, null, 6, 1.0F);
      }

      g.color(-9819464);
      Cos3Geo.cbox(g, "hd_mask", 0.0F, 0.25F, -0.05F, 1.2F, 0.8F, 0.3F, 0.12F);
      g.glow(true).color(-50550);
      g.push();
      g.translate(0.0F, 0.35F, 0.2F);
      g.rotX(90.0F);
      Cos3Geo.gem(g, "hd_gem", 0.35F, 0.25F, 0.08F, 6);
      g.pop();
      g.glow(false);
      g.push();
      g.translate(3.8F, 1.5F, -0.2F);
      g.rotZ(-22.0F + Cos3Geo.sin(t * 2.0F) * 5.0F);
      int[] fc = new int[]{-4691201, -7718182, -1531649};

      for (int k = 0; k < 3; k++) {
         g.push();
         g.rotZ(-18 + k * 18);
         int n = 10;
         float[][] sp = new float[n + 1][];
         float[] rr = new float[n + 1];
         float[] rz = new float[n + 1];

         for (int i = 0; i <= n; i++) {
            float f = (float)i / n;
            sp[i] = p(0.6F * f * f, f * 6.2F, -0.3F * f);
            rr[i] = Math.max(0.05F, 0.95F * (float)Math.sin(Math.PI * Math.min(1.0, f * 0.95 + 0.05)));
            rz[i] = 0.07F;
         }

         g.color(fc[k]);
         XmasGeo.sweep(g, "c3_plume", sp, rr, rz, null, 6, 1.0F);
         g.pop();
      }

      g.pop();
      g.pop();
      g.color(-1);
   }

   static void ninja(G g, Cos.A a) {
      float t = a.time;
      float mv = a.preview ? 0.0F : a.move;
      g.color(-13749694);
      Cos3Geo.band(g, "hd_ninja", 2.6F, 2.07F, 0.3F, -0.15F, 2.95F, 0.0F, 4.0F);
      g.push();
      g.translate(0.0F, 2.2F, 4.65F);
      g.color(-13749694);
      float[][] fold = new float[][]{{-1.4F, 0.8F}, {1.4F, 0.8F}, {0.9F, -0.7F}, {-0.9F, -0.7F}};
      HdGeo.puff(g, "hd_ninja", fold, 0.3F, 0.12F, 1);
      g.pop();
      g.color(-4710870);
      Cos3Geo.band(g, "hd_ninja", 2.6F, 2.07F, 0.3F, 5.2F, 6.7F, 0.0F, 4.0F);
      g.color(-4143408);
      g.push();
      g.translate(0.0F, 5.95F, 4.72F);
      Cos3Geo.bbox(g, "hd_steel", -1.8F, -0.65F, -0.1F, 1.8F, 0.65F, 0.2F, 0.12F);
      g.color(-12960184);
      Cos3Geo.cbox(g, "hd_steel", 0.0F, 0.0F, 0.22F, 1.6F, 0.12F, 0.05F, 0.02F);
      g.pop();
      g.color(-4710870);
      Geo.ellipsoid(g, "hd_ninja", 0.0F, 5.95F, -4.75F, 0.9F, 0.6F, 0.5F, 8, 5);
      float fl = Cos3Geo.sin(t * 4.0F) * 10.0F + mv * 25.0F;

      for (int s = -1; s <= 1; s += 2) {
         int n = 9;
         float[][] sp = new float[n + 1][];
         float[] w = new float[n + 1];
         float[] th = new float[n + 1];

         for (int i = 0; i <= n; i++) {
            float f = (float)i / n;
            double ang = Math.toRadians(62.0 - fl * 0.6 * f - s * 6);
            float wave = 0.25F * Cos3Geo.sin(t * 6.0F + f * 6.0F + s) * f;
            sp[i] = p(s * (0.35F + f * 1.2F) + wave, 5.95F - f * 5.2F * (float)Math.sin(ang), -4.9F - f * 5.2F * (float)Math.cos(ang));
            w[i] = 0.1F;
            th[i] = 0.55F - f * 0.15F;
         }

         XmasGeo.sweep(g, "hd_ninja", sp, w, th, null, 6, 2.0F);
      }

      g.color(-1);
   }
}
