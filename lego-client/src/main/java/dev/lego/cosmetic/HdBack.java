package dev.lego.cosmetic;

import java.util.Arrays;

final class HdBack {
   private HdBack() {
   }

   private static float[] p(float x, float y, float z) {
      return new float[]{x, y, z};
   }

   static void straps(G g, int col, float x, float yBot, float zBack) {
      int oc = g.color;
      g.color(col);

      for (int s = -1; s <= 1; s += 2) {
         float[][] sp = new float[][]{
            {s * x, yBot, zBack},
            {s * x, -1.2F, -2.35F},
            {s * x, 0.25F, -1.7F},
            {s * x, 0.4F, 0.0F},
            {s * x, 0.25F, 1.7F},
            {s * x, -1.2F, 2.3F},
            {s * (x + 0.2F), -6.2F, 2.25F}
         };
         float[] rx = new float[sp.length];
         float[] rz = new float[sp.length];
         Arrays.fill(rx, 0.12F);
         Arrays.fill(rz, 0.55F);
         XmasGeo.sweep(g, "hd_leather", sp, rx, rz, null, 6, 3.0F);
         g.color(-2564376);
         g.push();
         g.translate(s * (x + 0.1F), -3.2F, 2.42F);
         HdGeo.loop(g, "hd_chrome", HdGeo.rrect(0.62F, 0.5F, 0.15F, 2), 0.08F, 0.08F, 2.0F, 4);
         g.pop();
         g.color(col);
      }

      g.color(oc);
   }

   static void sash(G g, int col, float angle) {
      int oc = g.color;
      g.color(col);
      g.push();
      g.rotZ(angle);
      float[][] sp = new float[][]{
         {0.0F, -12.5F, 2.32F},
         {0.0F, -1.5F, 2.32F},
         {0.0F, 0.9F, 1.4F},
         {0.0F, 1.3F, 0.0F},
         {0.0F, 0.9F, -1.4F},
         {0.0F, -1.5F, -2.32F},
         {0.0F, -12.5F, -2.32F}
      };
      float[] rx = new float[sp.length];
      float[] rz = new float[sp.length];
      Arrays.fill(rx, 0.12F);
      Arrays.fill(rz, 0.62F);
      XmasGeo.sweep(g, "hd_leather", sp, rz, rx, null, 6, 4.0F);
      g.pop();
      g.color(oc);
   }

   static void legoBackpack(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(0.0F, -1.3F + (a.preview ? 0.0F : a.move * 0.15F * Math.abs(Cos3Geo.sin(t * 8.0F))), -2.15F);
      g.color(-1900533);
      Cos3Geo.bbox(g, "hd_brick", -3.3F, -8.4F, -3.6F, 3.3F, -0.2F, 0.0F, 0.3F);

      for (int ix = 0; ix < 2; ix++) {
         for (int iz = 0; iz < 2; iz++) {
            g.push();
            g.translate(-1.6F + ix * 3.2F, -0.2F, -0.9F - iz * 1.8F);
            Cos3Geo.lathe(g, "hd_brick", new float[]{1.0F, 1.0F, 0.92F, 0.0F}, new float[]{-0.02F, 0.72F, 0.84F, 0.86F}, 16);
            g.pop();
         }
      }

      g.color(-13053);
      Cos3Geo.bbox(g, "hd_brick", -2.6F, -7.6F, -4.3F, 2.6F, -3.9F, -3.55F, 0.22F);

      for (int ix = 0; ix < 2; ix++) {
         g.push();
         g.translate(-1.25F + ix * 2.5F, -3.9F, -3.95F);
         Cos3Geo.lathe(g, "hd_brick", new float[]{0.55F, 0.55F, 0.5F, 0.0F}, new float[]{-0.02F, 0.4F, 0.48F, 0.5F}, 12);
         g.pop();
      }

      g.color(-15066594);
      Cos3Geo.cbox(g, "hd_plastic", 0.0F, -4.3F, -4.38F, 1.2F, 0.5F, 0.2F, 0.06F);
      g.color(-16749385);
      Cos3Geo.bbox(g, "hd_brick", 3.25F, -8.0F, -3.0F, 4.05F, -4.6F, -0.7F, 0.18F);
      g.color(-6684664);
      g.push();
      g.translate(0.0F, -4.3F, -3.62F);
      HdGeo.loop(g, "hd_rubber", HdGeo.rrect(3.1F, 3.9F, 0.4F, 3), 0.07F, 0.07F, 2.0F, 4);
      g.pop();
      g.pop();
      straps(g, -14013904, 2.2F, -7.6F, -2.2F);
      g.color(-1);
   }

   static void backSword(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(0.4F, -5.5F, -2.85F);
      g.rotZ(-40.0F);
      int n = 10;
      float[][] sp = new float[n + 2][];
      float[] rx = new float[n + 2];
      float[] rz = new float[n + 2];
      float[] ex = new float[n + 2];

      for (int i = 0; i <= n; i++) {
         float f = (float)i / n;
         sp[i] = p(0.0F, 2.2F + f * 13.5F, 0.0F);
         rx[i] = 1.25F - f * 0.25F;
         rz[i] = 0.34F - f * 0.08F;
         ex[i] = 1.05F;
      }

      sp[n + 1] = p(0.0F, 18.6F, 0.0F);
      rx[n + 1] = 0.02F;
      rz[n + 1] = 0.02F;
      ex[n + 1] = 1.05F;
      g.glow(false).color(-4330497);
      XmasGeo.sweep(g, "c3_crystal", sp, rx, rz, ex, 4, 3.0F);
      float sh = Cos3Geo.fract(t * 0.5F);
      g.glow(true).color(-8725249);

      for (int i = 0; i <= n; i++) {
         rx[i] *= 0.28F;
         rz[i] *= 1.08F;
      }

      XmasGeo.sweep(g, "white", sp, rx, rz, ex, 4, 1.0F);
      Cos3Geo.halo(g, 0.0F, 2.2F + sh * 14.0F, 0.0F, 3.2F, Cos3Geo.alpha(0.55 * Math.sin(sh * Math.PI), 10155775));
      g.glow(false);
      g.color(-10863968);
      float[][] guard = new float[][]{
         {-3.4F, 0.1F}, {-2.2F, -0.35F}, {0.0F, -0.55F}, {2.2F, -0.35F}, {3.4F, 0.1F}, {3.5F, 0.75F}, {2.2F, 0.5F}, {0.0F, 0.7F}, {-2.2F, 0.5F}, {-3.5F, 0.75F}
      };
      g.push();
      g.translate(0.0F, 1.7F, 0.0F);
      HdGeo.puff(g, "hd_silver", guard, 0.9F, 0.3F, 2);
      g.glow(true).color(-6621441);
      g.push();
      g.translate(0.0F, 0.1F, 0.4F);
      g.rotX(90.0F);
      Cos3Geo.gem(g, "hd_gem", 0.45F, 0.35F, 0.1F, 6);
      g.pop();

      for (int s = -1; s <= 1; s += 2) {
         Cos3Geo.ball(g, "hd_gem", s * 3.5F, 0.45F, 0.0F, 0.35F, 6);
      }

      g.glow(false);
      g.pop();
      g.color(-12966888);
      Cos3Geo.lathe(
         g,
         "hd_leather",
         new float[]{0.42F, 0.5F, 0.44F, 0.52F, 0.44F, 0.52F, 0.44F, 0.52F, 0.44F, 0.42F},
         new float[]{-2.9F, -2.5F, -2.1F, -1.7F, -1.3F, -0.9F, -0.5F, -0.1F, 0.3F, 1.2F},
         10
      );
      g.color(-1);
      Cos3Geo.ball(g, "hd_gold", 0.0F, -3.45F, 0.0F, 0.75F, 10);
      g.glow(true).color(-8725249);
      Cos3Geo.ball(g, "hd_gem", 0.0F, -3.45F, 0.62F, 0.32F, 6);
      g.glow(false);
      g.pop();
      sash(g, -12966888, -40.0F);
      g.color(-1);
   }

   static float[][] guitarOutline() {
      float[][] half = new float[][]{
         {0.0F, -4.6F},
         {1.6F, -4.45F},
         {2.9F, -3.8F},
         {3.55F, -2.6F},
         {3.5F, -1.2F},
         {2.9F, 0.1F},
         {2.7F, 1.0F},
         {3.1F, 2.1F},
         {3.3F, 3.4F},
         {2.6F, 4.2F},
         {1.9F, 3.1F},
         {1.1F, 2.4F},
         {0.0F, 2.3F}
      };
      float[][] o = new float[half.length * 2 - 2][];
      int k = 0;

      for (float[] q : half) {
         o[k++] = new float[]{q[0], q[1]};
      }

      for (int i = half.length - 2; i >= 1; i--) {
         float[] q = half[i];
         float y = q[1] > 1.5F ? q[1] - 0.8F * (q[1] - 1.5F) / 2.7F : q[1];
         o[k++] = new float[]{-q[0], y};
      }

      return o;
   }

   static void guitar(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(0.0F, -7.2F, -3.15F);
      g.rotZ(-32.0F + Cos3Geo.sin(t * 1.5F) * 1.2F);
      g.rotY(180.0F);
      g.color(-1);
      HdGeo.puff(g, "hd_maple", guitarOutline(), 1.25F, 0.45F, 2, new float[]{0.0F, 0.0F, 1.0F, 1.0F}, new float[]{0.05F, 0.9F, 0.95F, 0.95F});
      g.color(-724760);
      g.push();
      g.translate(0.0F, 0.0F, 0.66F);
      float[][] pg = new float[][]{{-2.2F, -3.2F}, {-0.6F, -3.6F}, {1.6F, 2.0F}, {0.6F, 2.35F}, {-1.2F, 2.3F}, {-2.6F, 0.4F}};
      HdGeo.puff(g, "hd_plastic", pg, 0.1F, 0.04F, 1);
      g.pop();

      for (int i = 0; i < 3; i++) {
         g.color(-15461352);
         Cos3Geo.bbox(g, "hd_plastic", -0.95F, -1.9F + i * 1.45F, 0.7F, 0.95F, -1.35F + i * 1.45F, 0.95F, 0.1F);
      }

      g.color(-2564376);
      Cos3Geo.bbox(g, "hd_chrome", -1.05F, -3.35F, 0.62F, 1.05F, -2.75F, 0.95F, 0.1F);

      for (int i = 0; i < 3; i++) {
         g.push();
         g.translate(1.9F + i * 0.2F, -2.9F + i * 0.9F, 0.62F);
         g.rotX(90.0F);
         g.color(-1514278);
         Cos3Geo.lathe(g, "hd_plastic", new float[]{0.42F, 0.42F, 0.34F, 0.0F}, new float[]{0.0F, -0.35F, -0.45F, -0.46F}, 10);
         g.pop();
      }

      g.color(-2051984);
      Cos3Geo.bbox(g, "hd_wood", -0.55F, 2.2F, -0.35F, 0.55F, 12.6F, 0.3F, 0.15F);
      g.color(-12967920);
      g.box("hd_wood", -0.6F, 2.8F, 0.3F, 0.6F, 12.4F, 0.5F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-2564376);

      for (int i = 0; i < 12; i++) {
         float y = 12.2F - 9.2F * (1.0F - (float)Math.pow(0.94, i * 1.6)) / (1.0F - (float)Math.pow(0.94, 19.2));
         g.box("hd_chrome", -0.6F, y, 0.5F, 0.6F, y + 0.08F, 0.56F, 0.0F, 0.0F, 1.0F, 1.0F);
      }

      g.color(-15461352);
      float[][] hs = new float[][]{{-0.6F, 12.4F}, {0.6F, 12.4F}, {1.2F, 13.6F}, {1.35F, 15.6F}, {0.2F, 16.1F}, {-0.9F, 15.3F}, {-1.0F, 13.4F}};
      HdGeo.puff(g, "hd_plastic", hs, 0.5F, 0.18F, 1);
      g.color(-2564376);

      for (int i = 0; i < 3; i++) {
         for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 1.2F, 13.5F + i * 0.8F, 0.0F);
            g.rotZ(s * -90);
            Cos3Geo.lathe(g, "hd_chrome", new float[]{0.22F, 0.22F, 0.4F, 0.4F, 0.0F}, new float[]{0.0F, 0.5F, 0.55F, 0.85F, 0.9F}, 6);
            g.pop();
         }
      }

      g.color(-986892);

      for (int i = 0; i < 6; i++) {
         float x = -0.4F + i * 0.16F;
         Geo.tube(g, "hd_chrome", p(x * 1.5F, -3.05F, 1.0F), p(x, 12.4F, 0.62F), 0.03F, 0.03F, 3);
      }

      g.pop();
      sash(g, -14018030, -40.0F);
      g.color(-1);
   }

   static void jetpack(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(0.0F, -1.5F, -2.15F);
      g.color(-12960184);
      Cos3Geo.bbox(g, "hd_steel", -3.4F, -8.4F, -1.4F, 3.4F, -0.8F, 0.0F, 0.3F);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 2.15F, -1.4F, -2.95F);
         g.color(-1512206);
         Cos3Geo.lathe(g, "hd_chrome", new float[]{0.0F, 1.1F, 1.62F, 1.85F, 1.85F, 1.7F, 1.3F}, new float[]{1.35F, 1.15F, 0.7F, 0.1F, -6.6F, -7.2F, -7.5F}, 18);
         g.color(-1900533);
         Cos3Geo.lathe(g, "hd_plastic", new float[]{1.88F, 1.95F, 1.95F, 1.88F}, new float[]{-1.4F, -1.5F, -2.2F, -2.3F}, 18);
         Cos3Geo.lathe(g, "hd_plastic", new float[]{1.88F, 1.95F, 1.95F, 1.88F}, new float[]{-5.3F, -5.4F, -5.9F, -6.0F}, 18);
         g.push();
         g.translate(s * 1.3F, -3.6F, -1.25F);
         g.rotY(s * 45 + 180);
         g.rotX(90.0F);
         g.color(-14012874);
         Cos3Geo.lathe(g, "hd_steel", new float[]{0.55F, 0.55F, 0.45F, 0.0F}, new float[]{0.0F, 0.3F, 0.34F, 0.34F}, 12);
         g.pop();
         g.color(-12960184);
         Cos3Geo.lathe(g, "hd_steel", new float[]{1.3F, 1.0F, 0.95F, 1.35F, 1.1F}, new float[]{-7.4F, -7.9F, -8.4F, -9.6F, -9.7F}, 14);
         float fl = 1.0F + 0.18F * Cos3Geo.sin(t * 22.0F + s) + (a.preview ? 0.0F : a.move * 0.6F);
         g.push();
         g.translate(0.0F, -9.6F, 0.0F);
         g.rotZ(180.0F);
         Cos3Models.flame(g, 4.8F * fl, 1.0F, -30166, -2880);
         g.pop();
         Cos3Geo.halo(g, 0.0F, -10.4F, 0.0F, 4.2F, Cos3Geo.alpha(0.5, 16751162));

         for (int i = 0; i < 3; i++) {
            float ph = Cos3Geo.fract(t * 1.6F + i / 3.0F + (s > 0 ? 0.5F : 0.0F));
            Cos3Geo.halo(
               g,
               Cos3Geo.sin(i * 2.1 + t) * ph * 1.4F,
               -12.0F - ph * 7.0F * fl,
               Cos3Geo.cos(i * 2.1) * ph,
               1.5F + ph * 3.5F,
               Cos3Geo.alpha(0.3 * (1.0F - ph), 13154559)
            );
         }

         g.pop();
      }

      g.pop();
      straps(g, -14012874, 2.2F, -7.6F, -2.2F);
      g.color(-1);
   }

   static float[][] heater(float w, float h, int n) {
      float top = h * 0.42F;
      float H = h;
      float[][] o = new float[n * 2 + 1][];
      int k = 0;

      for (int i = 0; i <= n; i++) {
         float f = (float)i / n;
         o[k++] = new float[]{w * (float)Math.pow(Math.cos(f * Math.PI / 2.0), 0.55), top - H * f};
      }

      for (int i = n - 1; i >= 0; i--) {
         float f = (float)i / n;
         o[k++] = new float[]{-w * (float)Math.pow(Math.cos(f * Math.PI / 2.0), 0.55), top - H * f};
      }

      return HdGeo.ccw(Arrays.copyOf(o, k));
   }

   static void shield(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, -5.4F, -3.0F);
      g.rotZ(8.0F);
      g.rotY(180.0F);
      float[][] o = heater(5.2F, 6.2F, 10);
      g.color(-1);
      HdGeo.puff(g, "hd_target", o, 0.9F, 0.3F, 2, new float[]{0.0F, 0.0F, 1.0F, 1.0F}, new float[]{0.3F, 0.3F, 0.32F, 0.32F});
      g.color(-2564376);
      HdGeo.loop(g, "hd_steel", o, 0.42F, 0.62F, 2.6F, 6);
      g.color(-10134);
      g.push();
      g.translate(0.0F, 0.0F, 0.5F);
      Cos3Geo.bbox(g, "hd_gold", -0.55F, -5.6F, -0.1F, 0.55F, 2.55F, 0.22F, 0.12F);
      Cos3Geo.bbox(g, "hd_gold", -4.9F, -0.2F, -0.1F, 4.9F, 0.9F, 0.22F, 0.12F);
      g.translate(0.0F, 0.35F, 0.15F);
      g.rotX(90.0F);
      Cos3Geo.lathe(g, "hd_gold", new float[]{1.35F, 1.3F, 1.05F, 0.5F, 0.0F}, new float[]{0.0F, -0.3F, -0.7F, -0.95F, -1.0F}, 16);
      g.pop();
      g.color(-1512206);

      for (int i = 0; i < o.length; i += 2) {
         Cos3Geo.ball(g, "hd_chrome", o[i][0] * 0.86F, o[i][1] * 0.86F + 0.1F, 0.5F, 0.18F, 5);
      }

      g.pop();
      sash(g, -12966888, 30.0F);
      g.color(-1);
   }

   static void quiver(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(1.6F, -5.4F, -3.9F);
      g.rotZ(-24.0F);
      g.color(-1);
      Cos3Geo.lathe(g, "hd_quiver", new float[]{0.0F, 1.3F, 1.62F, 1.75F, 1.9F, 1.95F, 1.8F}, new float[]{-6.3F, -6.25F, -5.9F, -3.0F, 2.0F, 4.7F, 5.0F}, 18);
      g.color(-1525430);
      g.push();
      g.translate(0.0F, 4.75F, 0.0F);
      g.torus("hd_gold", 1.9F, 0.2F, 18, 5, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      int[] fc = new int[]{-1560518, -723724, -13993240, -1560518, -723724};

      for (int i = 0; i < 5; i++) {
         float ox = -0.85F + i % 3 * 0.85F;
         float oz = -0.55F + i / 3 * 1.05F;
         float h = 7.8F + i % 2 * 0.9F;
         g.push();
         g.translate(ox, 0.0F, oz);
         g.rotZ((i - 2) * 3 + Cos3Geo.sin(t * 2.0F + i) * 0.8F);
         g.color(-3630502);
         Geo.tube(g, "hd_wood", p(0.0F, 1.5F, 0.0F), p(0.0F, h, 0.0F), 0.12F, 0.12F, 5);
         g.color(fc[i]);

         for (int v = 0; v < 3; v++) {
            g.push();
            g.rotY(v * 120 + i * 17);
            float[][] vane = new float[][]{{0.1F, h - 2.4F}, {0.75F, h - 2.1F}, {0.8F, h - 0.4F}, {0.1F, h - 0.2F}};
            g.rotY(90.0F);
            HdGeo.puff(g, "hd_satin", vane, 0.05F, 0.02F, 1);
            g.pop();
         }

         g.color(-14013904);
         Cos3Geo.ball(g, "hd_plastic", 0.0F, h + 0.1F, 0.0F, 0.17F, 5);
         g.pop();
      }

      g.pop();
      sash(g, -12966376, 38.0F);
      g.color(-1);
   }

   static void rocket(G g, Cos.A a) {
      float t = a.time;
      g.push();
      float shake = (a.preview ? 0.0F : a.move) * 0.15F * Cos3Geo.sin(t * 40.0F);
      g.translate(shake, -4.0F + 0.2F * Cos3Geo.sin(t * 2.0F), -4.9F);
      g.color(-1);
      Cos3Geo.lathe(g, "hd_rocket", new float[]{1.5F, 1.95F, 2.2F, 2.25F, 2.2F}, new float[]{-6.2F, -5.6F, -4.2F, 0.0F, 3.0F}, 20, 0.0F, 1.0F, 1.0F, 0.1F);
      g.color(-1900533);
      Cos3Geo.lathe(g, "hd_gloss", new float[]{2.2F, 2.05F, 1.6F, 0.9F, 0.3F, 0.0F}, new float[]{3.0F, 4.5F, 5.9F, 7.0F, 7.6F, 7.7F}, 20);
      g.push();
      g.translate(0.0F, 0.9F, -2.25F);
      g.rotY(180.0F);
      g.color(-2564376);
      HdGeo.loop(g, "hd_chrome", HdGeo.circle(0.95F, 0.95F, 18), 0.22F, 0.2F, 2.0F, 6);
      g.color(-9778177);
      HdGeo.puff(g, "hd_glass", HdGeo.circle(0.9F, 0.9F, 16), 0.2F, 0.08F, 1);
      g.pop();
      g.color(-1900533);
      float[][] fin = new float[][]{{2.0F, -1.8F}, {2.0F, -6.0F}, {4.4F, -7.4F}, {4.3F, -4.8F}};

      for (int i = 0; i < 4; i++) {
         g.push();
         g.rotY(45 + i * 90);
         g.rotY(-90.0F);
         g.push();
         g.rotY(90.0F);
         HdGeo.puff(g, "hd_gloss", fin, 0.35F, 0.12F, 1);
         g.pop();
         g.pop();
      }

      g.color(-12960184);
      Cos3Geo.lathe(g, "hd_steel", new float[]{1.2F, 1.1F, 1.55F, 1.4F}, new float[]{-6.1F, -6.5F, -7.5F, -7.6F}, 16);
      float fl = 0.85F + 0.18F * Cos3Geo.sin(t * 25.0F) + (a.preview ? 0.0F : a.move * 0.8F);
      g.push();
      g.translate(0.0F, -7.5F, 0.0F);
      g.rotZ(180.0F);
      Cos3Models.flame(g, 5.2F * fl, 1.2F, -34278, -2880);
      g.pop();
      Cos3Geo.halo(g, 0.0F, -8.4F, 0.0F, 4.4F, Cos3Geo.alpha(0.55, 16751162));
      g.pop();
      g.color(-12960184);
      Cos3Geo.bbox(g, "hd_steel", -1.2F, -5.2F, -2.9F, 1.2F, -3.6F, -2.05F, 0.15F);
      straps(g, -14012874, 1.6F, -5.0F, -2.3F);
      g.color(-1);
   }

   static void teddy(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(0.8F, -4.2F, -4.5F);
      g.rotZ(Cos3Geo.sin(t * 1.6F) * 4.0F + (a.preview ? 0.0F : a.move * Cos3Geo.sin(t * 8.0F) * 6.0F));
      g.rotY(180.0F);
      int fur = -5211576;
      int light = -1521512;
      g.color(fur);
      Geo.ellipsoid(g, "hd_fur", 0.0F, -2.2F, 0.0F, 2.4F, 2.8F, 1.9F, 16, 10);
      g.color(light);
      Geo.ellipsoid(g, "hd_fur", 0.0F, -2.4F, 1.0F, 1.55F, 1.85F, 1.0F, 12, 8);
      g.color(fur);
      Geo.ellipsoid(g, "hd_fur", 0.0F, 1.8F, 0.0F, 2.35F, 2.15F, 2.05F, 18, 12);
      g.color(light);
      Geo.ellipsoid(g, "hd_fur", 0.0F, 1.2F, 1.75F, 1.1F, 0.8F, 0.7F, 12, 8);
      g.color(-15069688);
      Geo.ellipsoid(g, "hd_gloss", 0.0F, 1.55F, 2.4F, 0.4F, 0.28F, 0.22F, 8, 5);

      for (int s = -1; s <= 1; s += 2) {
         Cos3Geo.ball(g, "hd_gloss", s * 0.85F, 2.4F, 1.8F, 0.3F, 8);
         g.color(fur);
         Geo.ellipsoid(g, "hd_fur", s * 1.7F, 3.6F, 0.0F, 0.95F, 0.95F, 0.55F, 10, 6);
         g.color(light);
         Geo.ellipsoid(g, "hd_fur", s * 1.7F, 3.55F, 0.32F, 0.55F, 0.55F, 0.28F, 8, 5);
         g.color(fur);
         Geo.ellipsoid(g, "hd_fur", s * 2.4F, -1.4F, 0.4F, 0.8F, 1.5F, 0.8F, 10, 6);
         Geo.ellipsoid(g, "hd_fur", s * 1.3F, -4.8F, 0.6F, 0.95F, 0.85F, 1.15F, 10, 6);
         g.color(light);
         Geo.ellipsoid(g, "hd_fur", s * 1.3F, -4.85F, 1.6F, 0.62F, 0.6F, 0.25F, 8, 5);
         g.color(-15069688);
      }

      g.push();
      g.translate(0.0F, -0.25F, 1.7F);
      XmasGeo.bow(g, -2080678, 0.7F);
      g.pop();
      g.color(-1);
      g.pop();
      g.color(-1);
   }

   static void balloon(G g, Cos.A a) {
      float t = a.time;
      float bx = 6.0F + Cos3Geo.sin(t * 0.9F) * 1.0F;
      float by = 1.0F + Cos3Geo.sin(t * 1.3F) * 0.8F;
      float bz = -6.0F + Cos3Geo.cos(t * 0.7F) * 1.0F;
      float[] hand = new float[]{2.5F, -9.0F, -2.3F};
      int n = 12;
      float[][] pts = new float[n][];
      float[] rad = new float[n];

      for (int i = 0; i < n; i++) {
         float k = (float)i / (n - 1);
         pts[i] = new float[]{
            Geo.lerp(hand[0], bx, k) + Cos3Geo.sin(k * Math.PI) * 0.8F * Cos3Geo.sin(t * 2.0F),
            Geo.lerp(hand[1], by - 3.6F, k) - Cos3Geo.sin(k * Math.PI) * 0.6F,
            Geo.lerp(hand[2], bz, k)
         };
         rad[i] = 0.06F;
      }

      g.color(-723724);
      Geo.chain(g, "hd_plastic", pts, rad, 4);
      g.push();
      g.translate(bx, by, bz);
      g.rotZ(Cos3Geo.sin(t * 1.1F) * 8.0F);
      g.rotY(t * 25.0F);
      g.color(-54694);
      HdGeo.puff(g, "hd_gloss", HdGeo.heart(3.1F, 32), 2.6F, 1.25F, 4);
      g.color(-2092998);
      Cos3Geo.lathe(g, "hd_gloss", new float[]{0.05F, 0.42F, 0.28F, 0.45F, 0.0F}, new float[]{-2.75F, -3.0F, -3.25F, -3.5F, -3.6F}, 8);
      g.pop();
      g.color(-1);
   }
}
