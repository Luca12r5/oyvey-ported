package dev.lego.cosmetic;

import java.util.ArrayList;
import java.util.List;

final class VehRide {
   static final float KICK_TOP = 1.95F;
   static final float ESC_TOP = 2.85F;
   static final float[] KICK_HAND = new float[]{5.25F, 17.3F, 7.8F};
   static final float[] ESC_HAND = new float[]{6.1F, 18.4F, 8.0F};
   static final float BIKE_SEAT = 13.0F;
   static final float MOPED_SEAT = 12.6F;
   static final float MOTO_SEAT = 13.2F;
   static final float[] BIKE_HAND = new float[]{5.45F, 14.6F, 7.8F};
   static final float[] MOPED_HAND = new float[]{5.6F, 15.6F, 10.6F};
   static final float[] MOTO_HAND = new float[]{4.45F, 13.95F, 10.05F};
   static final float GEAR = 0.45F;
   static final float CRANK = 2.6F;
   static final float PEDAL_X = 2.7F;
   static final float[] BB = new float[]{0.0F, 4.4F, 2.6F};
   private static final String TREAD = "veh_tread";
   private static final String SIDE = "veh_side";
   static final float BR = 5.4F;
   static final float BRIM = 4.75F;
   static final float[] RA = new float[]{0.0F, 5.4F, -4.2F};
   static final float[] FA = new float[]{0.0F, 5.4F, 13.0F};

   private VehRide() {
   }

   static void wheelAt(G g, float x, float y, float z, float R, float w, float rimR, int spokes, float spin, String rim, String disc) {
      g.push();
      g.translate(x, y, z);
      VehGeo.wheel(g, R, w, rimR, spokes, spin, "veh_tread", "veh_side", rim, disc, 16);
      g.pop();
   }

   static void kickScooter(G g, Cos.A a) {
      String alu = "veh_alu";
      String red = "veh_c:E3242B";
      String blk = "veh_flat:1C1D22";
      float wr = 1.7F;
      float zf = 9.9F;
      float zr = -8.4F;
      float spin = a.wheel * 1.8F;
      g.push();
      g.rotZ(-a.lean * 5.0F);
      VehGeo.rbox(g, alu, -2.1F, 1.05F, -6.6F, 2.1F, 1.75F, 6.1F, 0.3F);
      VehGeo.box(g, "veh_griptape", -1.8F, 1.6F, -6.2F, 1.8F, 1.95F, 5.5F);
      VehGeo.box(g, red, -2.2F, 1.2F, -6.0F, 2.2F, 1.45F, 5.4F);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.beam(g, alu, VehGeo.p(s * 0.95F, 1.35F, -6.3F), VehGeo.p(s * 0.95F, wr, zr), 0.35F, 0.9F);
      }

      wheelAt(g, 0.0F, wr, zr, wr, 1.1F, 1.05F, 5, spin, "veh_pu", null);
      VehGeo.tube(g, "veh_chrome", VehGeo.p(-1.15F, wr, zr), VehGeo.p(1.15F, wr, zr), 0.22F, 0.22F, 6, true);
      g.push();
      VehGeo.strip(g, blk, VehGeo.arc(0.0F, wr, zr, wr + 0.35F, 70.0F, 175.0F, 8), 0.3F, 1.6F, VehGeo.p(1.0F, 0.0F, 0.0F), true);
      VehGeo.box(g, "veh_rubber", -0.65F, wr + 2.05F, zr - 1.2F, 0.65F, wr + 2.35F, zr + 0.4F);
      g.pop();
      VehGeo.beam(g, alu, VehGeo.p(0.0F, 1.4F, 5.6F), VehGeo.p(0.0F, 3.6F, 8.0F), 1.6F, 1.0F);
      float[] piv = new float[]{0.0F, 5.0F, 8.3F};
      g.push();
      VehGeo.steer(g, piv, 4.0F, a.lean * 22.0F);
      VehGeo.tube(g, red, VehGeo.p(0.0F, 2.9F, 8.45F), VehGeo.p(0.0F, 5.9F, 8.25F), 0.62F, 0.62F, 10, true);
      VehGeo.rbox(g, alu, -1.15F, 2.55F, 8.0F, 1.15F, 3.2F, 9.0F, 0.2F);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.beam(g, alu, VehGeo.p(s * 0.9F, 2.8F, 8.6F), VehGeo.p(s * 0.9F, wr, zf), 0.35F, 0.7F);
      }

      wheelAt(g, 0.0F, wr, zf, wr, 1.1F, 1.05F, 5, spin, "veh_pu", null);
      VehGeo.tube(g, "veh_chrome", VehGeo.p(-1.15F, wr, zf), VehGeo.p(1.15F, wr, zf), 0.22F, 0.22F, 6, true);
      VehGeo.tube(g, alu, VehGeo.p(0.0F, 5.9F, 8.25F), VehGeo.p(0.0F, 11.0F, 8.05F), 0.48F, 0.48F, 8, false);
      VehGeo.tube(g, alu, VehGeo.p(0.0F, 10.4F, 8.07F), VehGeo.p(0.0F, 17.3F, 7.8F), 0.38F, 0.38F, 8, false);
      g.push();
      g.translate(0.0F, 10.6F, 8.06F);
      VehGeo.lathe(g, red, new float[][]{{0.01F, -0.5F}, {0.66F, -0.5F}, {0.7F, 0.5F}, {0.01F, 0.5F}}, 10);
      g.pop();
      VehGeo.beam(g, red, VehGeo.p(0.6F, 10.9F, 8.1F), VehGeo.p(0.9F, 9.2F, 8.7F), 0.25F, 0.2F);
      VehGeo.tube(g, alu, VehGeo.p(-5.6F, 17.3F, 7.8F), VehGeo.p(5.6F, 17.3F, 7.8F), 0.36F, 0.36F, 8, false);
      VehGeo.rbox(g, alu, -0.6F, 16.8F, 7.3F, 0.6F, 17.8F, 8.3F, 0.2F);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.tube(g, "veh_foam", VehGeo.p(s * 3.9F, 17.3F, 7.8F), VehGeo.p(s * 6.6F, 17.3F, 7.8F), 0.62F, 0.62F, 10, true);
      }

      g.pop();
      float fold = VehGeo.smooth(0.05F, 0.3F, a.move);
      VehGeo.beam(g, "veh_gun", VehGeo.p(-2.0F, 1.2F, -1.5F), VehGeo.lerp(VehGeo.p(-3.4F, 0.12F, -3.2F), VehGeo.p(-2.25F, 1.1F, -5.2F), fold), 0.3F, 0.3F);
      g.pop();
   }

   static void eScooter(G g, Cos.A a) {
      String body = "veh_c:2F323A";
      String gun = "veh_gun";
      String acc = "veh_c:1ED760";
      float wr = 2.3F;
      float zf = 10.6F;
      float zr = -9.6F;
      float spin = a.wheel * 1.3F;
      g.push();
      g.rotZ(-a.lean * 5.0F);
      VehGeo.rbox(g, body, -2.5F, 1.45F, -7.2F, 2.5F, 2.6F, 6.6F, 0.45F);
      VehGeo.box(g, "veh_rubber", -2.05F, 2.45F, -6.6F, 2.05F, 2.85F, 6.0F);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.box(g, acc, s < 0 ? -2.58F : 2.42F, 1.85F, -6.4F, s < 0 ? -2.42F : 2.58F, 2.05F, 5.6F);
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.beam(g, body, VehGeo.p(s * 1.05F, 2.0F, -6.9F), VehGeo.p(s * 1.05F, wr, zr), 0.4F, 1.0F);
      }

      g.push();
      g.translate(0.0F, wr, zr);
      g.rotY(180.0F);
      VehGeo.wheel(g, wr, 1.3F, 1.45F, 5, -spin, "veh_tread", "veh_side", "veh_gun", "veh_disc", 16);
      g.pop();
      VehGeo.tube(g, "veh_chrome", VehGeo.p(-1.35F, wr, zr), VehGeo.p(1.35F, wr, zr), 0.25F, 0.25F, 6, true);
      VehGeo.strip(g, body, VehGeo.arc(0.0F, wr, zr, wr + 0.45F, 55.0F, 192.0F, 10), 0.28F, 1.7F, VehGeo.p(1.0F, 0.0F, 0.0F), true);
      g.glow(true).color(VehGeo.argb((int)(200.0F + 55.0F * VehGeo.sin(a.time * 4.0F)), 16777215));
      float[] bl = VehGeo.arc(0.0F, wr, zr, wr + 0.6F, 180.0F, 180.0F, 1)[0];
      VehGeo.rbox(g, "veh_glow:FF2A2A", -0.6F, bl[1] - 0.15F, bl[2] - 0.2F, 0.6F, bl[1] + 0.45F, bl[2] + 0.3F, 0.1F);
      g.glow(false).color(-1);
      VehGeo.beam(g, body, VehGeo.p(0.0F, 2.0F, 5.9F), VehGeo.p(0.0F, 4.5F, 8.5F), 1.8F, 1.2F);
      VehGeo.rbox(g, gun, -0.95F, 4.2F, 8.1F, 0.95F, 5.6F, 9.3F, 0.3F);
      VehGeo.tube(g, "veh_chrome", VehGeo.p(-1.1F, 4.9F, 8.4F), VehGeo.p(1.1F, 4.9F, 8.4F), 0.28F, 0.28F, 8, true);
      VehGeo.beam(g, acc, VehGeo.p(1.05F, 5.3F, 8.6F), VehGeo.p(1.05F, 4.0F, 7.5F), 0.22F, 0.3F);
      g.push();
      VehGeo.steer(g, VehGeo.p(0.0F, 5.0F, 9.2F), 5.0F, a.lean * 20.0F);
      VehGeo.tube(g, body, VehGeo.p(0.0F, 5.3F, 9.2F), VehGeo.p(0.0F, 18.0F, 8.1F), 0.6F, 0.55F, 10, false);
      VehGeo.rbox(g, gun, -1.35F, 4.9F, 8.6F, 1.35F, 5.9F, 9.8F, 0.3F);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.beam(g, gun, VehGeo.p(s * 1.05F, 5.2F, 9.4F), VehGeo.p(s * 1.05F, wr, zf), 0.45F, 0.7F);
      }

      VehGeo.strip(g, body, VehGeo.arc(0.0F, wr, zf, wr + 0.4F, 25.0F, 150.0F, 8), 0.26F, 1.6F, VehGeo.p(1.0F, 0.0F, 0.0F), true);
      g.push();
      g.translate(0.0F, wr, zf);
      VehGeo.wheel(g, wr, 1.3F, 1.5F, 0, spin, "veh_tread", "veh_side", "veh_gun", null, 16);
      g.rotZ(-90.0F);
      VehGeo.lathe(
         g,
         new String[]{gun, gun, acc, gun, gun},
         new float[][]{{0.01F, -0.85F}, {1.1F, -0.85F}, {1.2F, -0.6F}, {1.2F, 0.6F}, {1.1F, 0.85F}, {0.01F, 0.85F}},
         14
      );
      g.pop();
      VehGeo.rbox(g, body, -0.75F, 14.9F, 8.3F, 0.75F, 16.4F, 9.2F, 0.25F);
      g.glow(true);
      VehGeo.ellipsoid(g, "veh_glow:FFFBE8", 0.0F, 15.65F, 9.2F, 0.6F, 0.55F, 0.22F, 10, 5);
      g.glow(false);
      VehGeo.tube(g, gun, VehGeo.p(-7.0F, 18.4F, 8.0F), VehGeo.p(7.0F, 18.4F, 8.0F), 0.42F, 0.42F, 8, false);
      VehGeo.rbox(g, body, -1.4F, 17.6F, 7.2F, 1.4F, 18.95F, 8.9F, 0.35F);
      g.glow(true);
      VehGeo.box6(g, "veh_flat:0B1016", new String[]{null, null, null, null, VehicleWorld.displayTex(), null}, -1.0F, 18.85F, 7.45F, 1.0F, 19.05F, 8.65F);
      g.glow(false);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.tube(g, "veh_gripr", VehGeo.p(s * 4.9F, 18.4F, 8.0F), VehGeo.p(s * 7.3F, 18.4F, 8.0F), 0.62F, 0.62F, 10, true);
      }

      VehGeo.rbox(g, gun, -4.6F, 18.1F, 7.6F, -3.8F, 18.9F, 8.5F, 0.2F);
      VehGeo.beam(g, gun, VehGeo.p(-4.3F, 18.6F, 8.5F), VehGeo.p(-6.8F, 18.3F, 9.2F), 0.3F, 0.25F);
      VehGeo.rbox(g, gun, 3.9F, 17.9F, 8.3F, 4.5F, 18.9F, 8.75F, 0.12F);
      g.push();
      g.translate(-3.0F, 18.75F, 8.0F);
      VehGeo.lathe(g, "veh_chrome", new float[][]{{0.01F, 0.0F}, {0.6F, 0.0F}, {0.5F, 0.35F}, {0.01F, 0.45F}}, 10);
      g.pop();
      g.pop();
      float fold = VehGeo.smooth(0.05F, 0.3F, a.move);
      VehGeo.beam(g, gun, VehGeo.p(-2.5F, 1.75F, -0.8F), VehGeo.lerp(VehGeo.p(-3.8F, 0.12F, -2.8F), VehGeo.p(-2.65F, 1.4F, -5.2F), fold), 0.35F, 0.35F);
      g.pop();
   }

   static void bikeWheel(G g, float spin) {
      g.push();
      g.rotX(spin);
      g.rotZ(-90.0F);
      VehGeo.lathe(
         g,
         new String[]{"veh_side", "veh_tread", "veh_tread", "veh_side"},
         new float[][]{{4.75F, -0.3F}, {5.15F, -0.4F}, {5.4F, 0.0F}, {5.15F, 0.4F}, {4.75F, 0.3F}},
         22
      );
      VehGeo.lathe(g, "veh_alu", new float[][]{{4.75F, 0.3F}, {4.25F, 0.22F}, {4.25F, -0.22F}, {4.75F, -0.3F}}, 22);
      VehGeo.lathe(
         g,
         "veh_gun",
         new float[][]{
            {0.01F, -0.75F},
            {0.3F, -0.75F},
            {0.7F, -0.65F},
            {0.7F, -0.5F},
            {0.32F, -0.45F},
            {0.32F, 0.45F},
            {0.7F, 0.5F},
            {0.7F, 0.65F},
            {0.3F, 0.75F},
            {0.01F, 0.75F}
         },
         10
      );
      int n = 20;

      for (int i = 0; i < n; i++) {
         double an = (Math.PI * 2) * i / n;
         double ar = an + (i % 2 == 0 ? 0.28 : -0.28);
         float hy = i % 4 < 2 ? -0.58F : 0.58F;
         VehGeo.tube(
            g,
            "veh_chrome",
            VehGeo.p((float)Math.cos(an) * 0.6F, hy, (float)Math.sin(an) * 0.6F),
            VehGeo.p((float)Math.cos(ar) * 4.3F, 0.0F, (float)Math.sin(ar) * 4.3F),
            0.07F,
            0.07F,
            3,
            false
         );
      }

      g.pop();
   }

   static void bike(G g, Cos.A a) {
      String fr = "veh_c:2FA58C";
      String cr = "veh_chrome";
      String gun = "veh_gun";
      String brown = "veh_leather:6A4424";
      float spin = a.wheel * 0.55F;
      float crank = Vehicles.pedalAngle(a.wheel);
      g.push();
      g.rotZ(-a.lean * 6.0F);
      g.push();
      g.translate(RA[0], RA[1], RA[2]);
      bikeWheel(g, spin);
      g.pop();
      g.push();
      g.translate(1.25F, RA[1], RA[2]);
      g.rotX(spin);
      g.rotZ(-90.0F);
      VehGeo.lathe(g, gun, new float[][]{{0.01F, -0.12F}, {0.95F, -0.12F}, {0.95F, 0.12F}, {0.01F, 0.12F}}, 12);
      g.pop();
      VehGeo.pipe(g, fr, new float[][]{BB, {0.0F, 11.8F, -0.25F}}, 0.36F, 8, false);
      VehGeo.pipe(g, fr, new float[][]{{0.0F, 10.4F, 10.95F}, {0.0F, 7.4F, 8.8F}, {0.0F, 5.3F, 5.4F}, {0.0F, 4.5F, 2.9F}}, 0.4F, 8, false);
      VehGeo.pipe(g, fr, new float[][]{{0.0F, 11.8F, 10.6F}, {0.0F, 9.2F, 8.1F}, {0.0F, 7.8F, 4.4F}, {0.0F, 7.6F, 1.4F}}, 0.32F, 8, false);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.pipe(g, fr, new float[][]{{s * 0.3F, 4.4F, 2.3F}, {s * 0.78F, 5.4F, -4.2F}}, 0.24F, 6, false);
         VehGeo.pipe(g, fr, new float[][]{{s * 0.22F, 11.1F, -0.1F}, {s * 0.78F, 5.4F, -4.2F}}, 0.22F, 6, false);
      }

      VehGeo.tube(g, fr, VehGeo.p(0.0F, 9.6F, 11.28F), VehGeo.p(0.0F, 12.75F, 10.3F), 0.52F, 0.52F, 10, true);
      VehGeo.tube(g, fr, VehGeo.p(-0.9F, BB[1], BB[2]), VehGeo.p(0.9F, BB[1], BB[2]), 0.55F, 0.55F, 10, true);
      VehGeo.tube(g, cr, VehGeo.p(0.0F, 11.5F, -0.2F), VehGeo.p(0.0F, 12.3F, -0.43F), 0.27F, 0.27F, 6, false);
      VehGeo.loft(g, VehGeo.range(-2.5F, 2.9F, 0.6F), z -> {
         float t = (z + 2.5F) / 5.4F;
         float w = t < 0.12F ? VehGeo.lerp(1.6F, 2.3F, t / 0.12F) : VehGeo.lerp(2.3F, 0.55F, VehGeo.smooth(0.2F, 0.85F, t));
         float top = 12.95F - 0.25F * VehGeo.sin(t * Math.PI) + (t < 0.1F ? (0.1F - t) * 1.5F : 0.0F);
         float bot = 11.95F;
         int n = 12;
         float[][] r = new float[n][];

         for (int i = 0; i < n; i++) {
            double an = (-Math.PI / 2) + (Math.PI * 2) * i / n;
            float c = (float)Math.cos(an);
            float sn = (float)Math.sin(an);
            r[i] = new float[]{c * w, sn < 0.0F ? bot + (1.0F + sn) * (top - bot) * 0.5F : bot + (top - bot) * (0.5F + 0.5F * (float)Math.sqrt(sn))};
         }

         return r;
      }, (k, j, z, pa, pb) -> brown, true, true, 13.0F, 11.9F);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.tube(g, cr, VehGeo.p(s * 1.2F, 11.7F, -1.9F), VehGeo.p(s * 1.2F, 12.25F, -1.9F), 0.25F, 0.25F, 6, true);
      }

      VehGeo.tube(g, cr, VehGeo.p(-1.2F, 11.75F, -1.9F), VehGeo.p(1.2F, 11.75F, -1.9F), 0.12F, 0.12F, 4, false);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.pipe(g, cr, new float[][]{{s * 1.9F, 10.9F, -1.6F}, {s * 1.9F, 10.9F, -9.4F}, {s * 0.9F, 5.4F, -4.4F}}, 0.16F, 6, true);
      }

      for (int i = 0; i < 3; i++) {
         VehGeo.tube(g, cr, VehGeo.p(-1.9F, 10.9F, -3.6F - i * 2.4F), VehGeo.p(1.9F, 10.9F, -3.6F - i * 2.4F), 0.14F, 0.14F, 4, false);
      }

      VehGeo.strip(g, fr, VehGeo.arc(0.0F, RA[1], RA[2], 5.9500003F, 20.0F, 200.0F, 12), 0.2F, 1.4F, VehGeo.p(1.0F, 0.0F, 0.0F), true);
      g.glow(true);
      VehGeo.rbox(g, "veh_glow:FF2A2A", -0.6F, 10.2F, -9.75F, 0.6F, 10.75F, -9.35F, 0.12F);
      g.glow(false);
      g.push();
      g.translate(1.25F, BB[1], BB[2]);
      g.rotX(crank);
      g.rotZ(-90.0F);
      VehGeo.lathe(g, new String[]{gun, "veh_alu", gun}, new float[][]{{0.01F, -0.12F}, {2.0F, -0.12F}, {2.0F, 0.12F}, {0.01F, 0.12F}}, 18);
      g.pop();
      g.push();
      g.translate(0.0F, BB[1], BB[2]);
      g.rotX(crank);
      VehGeo.tube(g, "veh_alu", VehGeo.p(-1.75F, 0.0F, 0.0F), VehGeo.p(1.75F, 0.0F, 0.0F), 0.3F, 0.3F, 6, true);
      VehGeo.beam(g, "veh_alu", VehGeo.p(1.65F, 0.0F, 0.0F), VehGeo.p(1.65F, -2.6F, 0.0F), 0.4F, 0.55F, 0.35F, 0.45F, VehGeo.p(0.0F, 0.0F, 1.0F));
      VehGeo.beam(g, "veh_alu", VehGeo.p(-1.65F, 0.0F, 0.0F), VehGeo.p(-1.65F, 2.6F, 0.0F), 0.4F, 0.55F, 0.35F, 0.45F, VehGeo.p(0.0F, 0.0F, 1.0F));
      g.pop();
      float[][] ped = Vehicles.pedals("veh_bike", a.wheel);

      for (float[] pp : ped) {
         float sx = Math.signum(pp[0]);
         VehGeo.tube(g, "veh_alu", VehGeo.p(sx * 1.7F, pp[1], pp[2]), VehGeo.p(sx * 1.95F, pp[1], pp[2]), 0.14F, 0.14F, 4, false);
         VehGeo.rbox(g, "veh_flat:1C1D22", pp[0] - 0.8F, pp[1] - 0.2F, pp[2] - 0.6F, pp[0] + 0.8F, pp[1] + 0.2F, pp[2] + 0.6F, 0.1F);
      }

      chain(g, crank);
      float fold = VehGeo.smooth(0.05F, 0.3F, a.move);
      VehGeo.beam(g, gun, VehGeo.p(-0.7F, 4.6F, -0.8F), VehGeo.lerp(VehGeo.p(-2.6F, 0.12F, -1.8F), VehGeo.p(-0.9F, 4.4F, -4.6F), fold), 0.3F, 0.3F);
      g.push();
      VehGeo.steer(g, VehGeo.p(0.0F, 11.2F, 10.75F), 17.8F, a.lean * 22.0F);
      VehGeo.rbox(g, fr, -1.1F, 9.1F, 10.8F, 1.1F, 9.8F, 11.8F, 0.2F);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.pipe(g, fr, new float[][]{{s * 0.75F, 9.5F, 11.3F}, {s * 0.75F, 7.3F, 12.25F}, {s * 0.8F, FA[1], FA[2]}}, 0.24F, 6, false);
      }

      g.push();
      g.translate(FA[0], FA[1], FA[2]);
      bikeWheel(g, spin);
      g.pop();
      VehGeo.strip(g, fr, VehGeo.arc(0.0F, FA[1], FA[2], 5.9500003F, 5.0F, 160.0F, 11), 0.2F, 1.4F, VehGeo.p(1.0F, 0.0F, 0.0F), true);
      VehGeo.tube(g, cr, VehGeo.p(0.0F, 12.6F, 10.35F), VehGeo.p(0.0F, 14.2F, 10.1F), 0.3F, 0.3F, 6, true);
      VehGeo.pipe(
         g,
         cr,
         new float[][]{
            {-5.9F, 14.6F, 7.2F},
            {-4.6F, 14.6F, 8.8F},
            {-2.5F, 14.35F, 10.1F},
            {0.0F, 14.2F, 10.25F},
            {2.5F, 14.35F, 10.1F},
            {4.6F, 14.6F, 8.8F},
            {5.9F, 14.6F, 7.2F}
         },
         0.22F,
         6,
         false
      );

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.tube(g, brown, VehGeo.p(s * 4.85F, 14.6F, 8.65F), VehGeo.p(s * 6.05F, 14.6F, 7.0F), 0.45F, 0.45F, 8, true);
      }

      g.push();
      g.translate(-2.6F, 14.55F, 10.1F);
      VehGeo.lathe(g, cr, new float[][]{{0.01F, 0.0F}, {0.55F, 0.0F}, {0.45F, 0.35F}, {0.01F, 0.42F}}, 8);
      g.pop();
      g.push();
      g.translate(0.0F, 9.4F, 12.3F);
      g.rotX(90.0F);
      VehGeo.lathe(g, cr, new float[][]{{0.01F, -0.9F}, {0.45F, -0.9F}, {0.8F, -0.1F}, {0.8F, 0.0F}}, 10);
      g.glow(true);
      VehGeo.lathe(g, "veh_glow:FFF6D8", new float[][]{{0.8F, 0.0F}, {0.01F, 0.15F}}, 10);
      g.glow(false);
      g.pop();
      float bz0 = 12.2F;
      float bz1 = 16.4F;
      float by0 = 11.6F;
      float by1 = 14.4F;
      VehGeo.box(g, "veh_basket", -3.0F, by0, bz0, 3.0F, by0 + 0.3F, bz1);
      VehGeo.box(g, "veh_basket", -3.0F, by0 + 0.3F, bz0, 3.0F, by1, bz0 + 0.3F);
      VehGeo.box(g, "veh_basket", -3.0F, by0 + 0.3F, bz1 - 0.3F, 3.0F, by1, bz1);
      VehGeo.box(g, "veh_basket", -3.0F, by0 + 0.3F, bz0 + 0.3F, -2.7F, by1 - 0.02F, bz1 - 0.3F);
      VehGeo.box(g, "veh_basket", 2.7F, by0 + 0.3F, bz0 + 0.3F, 3.0F, by1 - 0.02F, bz1 - 0.3F);
      VehGeo.pipe(g, cr, new float[][]{{-1.5F, by0 - 0.1F, bz0 + 0.5F}, {-0.8F, 9.8F, 12.0F}}, 0.12F, 4, false);
      VehGeo.pipe(g, cr, new float[][]{{1.5F, by0 - 0.1F, bz0 + 0.5F}, {0.8F, 9.8F, 12.0F}}, 0.12F, 4, false);
      g.pop();
      g.pop();
   }

   private static void chain(G g, float crank) {
      float rr = 2.0F;
      float rc = 0.95F;
      float x = 1.25F;
      List<float[]> path = new ArrayList<>();

      for (float[] q : VehGeo.arc(x, BB[1], BB[2], rr, -90.0F, 90.0F, 10)) {
         path.add(q);
      }

      for (float[] q : VehGeo.arc(x, RA[1], RA[2], rc, 90.0F, 270.0F, 6)) {
         path.add(q);
      }

      path.add(path.get(0));
      int m = path.size();
      float[] acc = new float[m];

      for (int i = 1; i < m; i++) {
         float[] a = path.get(i - 1);
         float[] b = path.get(i);
         acc[i] = acc[i - 1] + (float)Math.sqrt((b[1] - a[1]) * (b[1] - a[1]) + (b[2] - a[2]) * (b[2] - a[2]));
      }

      float L = acc[m - 1];
      int n = 34;
      float step = L / n;
      float phase = (float)Math.toRadians(crank) * rr;

      for (int i = 0; i < n; i++) {
         float s0 = ((i * step - phase) % L + L) % L;
         float[] a = at(path, acc, s0);
         float[] b = at(path, acc, Math.min(L, s0 + step * 0.78F));
         VehGeo.beam(g, "veh_gun", a, b, 0.44F, 0.28F);
      }
   }

   private static float[] at(List<float[]> path, float[] acc, float s) {
      int i = 1;

      while (i < acc.length - 1 && acc[i] < s) {
         i++;
      }

      float t = acc[i] == acc[i - 1] ? 0.0F : (s - acc[i - 1]) / (acc[i] - acc[i - 1]);
      return VehGeo.lerp(path.get(i - 1), path.get(i), VehGeo.clamp(t, 0.0F, 1.0F));
   }

   static void moped(G g, Cos.A a) {
      String P = "veh_paint:8FD9C8";
      String cr = "veh_chrome";
      String gun = "veh_gun";
      float wr = 3.4F;
      float zr = -10.5F;
      float zf = 13.6F;
      float spin = a.wheel * 0.9F;
      g.push();
      g.rotZ(-a.lean * 8.0F);
      wheelAt(g, 0.0F, wr, zr, wr, 1.9F, 2.1F, 0, spin, "veh_rim", null);
      float[] cz = new float[]{-15.6F, -13.5F, -9.0F, -4.0F, 0.0F, 1.6F};
      float[] cw = new float[]{1.8F, 4.4F, 5.6F, 5.4F, 4.3F, 3.0F};
      float[] ct = new float[]{9.0F, 10.8F, 11.3F, 11.3F, 11.2F, 10.2F};
      float[] cb = new float[]{7.4F, 5.4F, 4.4F, 4.2F, 4.2F, 4.8F};
      VehGeo.loft(g, VehGeo.range(-15.6F, 1.6F, 1.1F), z -> {
         float w = VehGeo.curve(z, cz, cw);
         float t = VehGeo.curve(z, cz, ct);
         float b = VehGeo.curve(z, cz, cb);
         int n = 18;
         float[][] r = new float[n][];

         for (int i = 0; i < n; i++) {
            double an = (-Math.PI / 2) + (Math.PI * 2) * i / n;
            float[] q = Geo.sq(an, 1.0F, 2.6F);
            float mid = b + (t - b) * 0.42F;
            float yy = q[1] < 0.0F ? b + (1.0F + q[1]) * (mid - b) : mid + q[1] * (t - mid);
            float fx = q[1] > 0.0F ? 1.0F - 0.38F * q[1] * q[1] : 1.0F - 0.12F * q[1] * q[1];
            r[i] = new float[]{q[0] * w * fx, yy};
         }

         return r;
      }, (k, j, z, pa, pb) -> P, true, true, 11.5F, 4.0F);
      VehGeo.rbox(g, "veh_leather:3A2A22", -3.3F, 10.9F, -11.0F, 3.3F, 12.6F, 2.2F, 0.8F);
      VehGeo.pipe(g, cr, new float[][]{{-2.6F, 11.4F, -11.0F}, {-2.6F, 12.4F, -12.6F}, {2.6F, 12.4F, -12.6F}, {2.6F, 11.4F, -11.0F}}, 0.2F, 6, true);
      g.glow(true);
      VehGeo.rbox(g, "veh_glow:FF2A2A", -1.2F, 8.0F, -16.0F, 1.2F, 9.3F, -15.0F, 0.3F);
      g.glow(false);
      g.push();
      g.translate(3.3F, 3.8F, -9.5F);
      g.rotX(-90.0F);
      VehGeo.lathe(
         g,
         new String[]{cr, cr, cr, gun, gun, gun},
         new float[][]{{0.01F, -3.6F}, {0.8F, -3.6F}, {1.05F, -2.8F}, {1.05F, 2.4F}, {0.35F, 2.8F}, {0.35F, 2.5F}, {0.01F, 2.5F}},
         12
      );
      g.pop();
      VehGeo.pipe(g, cr, new float[][]{{3.3F, 3.8F, -12.1F}, {3.3F, 3.8F, -13.6F}}, 0.3F, 6, true);
      VehGeo.rbox(g, P, -3.9F, 2.6F, -1.2F, 3.9F, 3.6F, 9.4F, 0.4F);

      for (int i = 0; i < 5; i++) {
         VehGeo.box(g, "veh_rubber", -3.2F, 3.5F, -0.2F + i * 1.8F, 3.2F, 3.75F, 0.8F + i * 1.8F);
      }

      VehGeo.box(g, gun, -2.4F, 1.3F, -2.0F, 2.4F, 2.7F, 6.0F);
      float fold = VehGeo.smooth(0.05F, 0.3F, a.move);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.beam(g, gun, VehGeo.p(s * 2.2F, 2.0F, -3.0F), VehGeo.lerp(VehGeo.p(s * 2.6F, 0.1F, -3.8F), VehGeo.p(s * 2.4F, 1.4F, -6.2F), fold), 0.35F, 0.35F);
      }

      g.push();
      g.rotX(-90.0F);
      VehGeo.loft(g, VehGeo.range(3.3F, 16.2F, 1.0F), y -> {
         float W = y < 9.0F ? VehGeo.lerp(3.9F, 5.4F, VehGeo.smooth(3.3F, 9.0F, y)) : VehGeo.lerp(5.4F, 3.0F, VehGeo.smooth(9.0F, 16.2F, y));
         float z0 = 8.9F - (y - 3.3F) * 0.12F;
         float bulge = 1.1F;
         int n = 8;
         float[][] r = new float[2 * n + 2][];

         for (int i = 0; i <= n; i++) {
            float x = -W + 2.0F * W * i / n;
            float t = x / W;
            r[i] = new float[]{x, -(z0 + bulge * (1.0F - t * t))};
         }

         for (int i = 0; i <= n; i++) {
            float x = W - 2.0F * W * i / n;
            float t = x / W;
            r[n + 1 + i] = new float[]{x, -(z0 + bulge * (1.0F - t * t) - 0.45F)};
         }

         return r;
      }, (k, j, z, pa, pb) -> P, true, true, -12.0F, -4.0F, false, true);
      g.pop();
      VehGeo.beam(g, cr, VehGeo.p(0.0F, 12.6F, 10.2F), VehGeo.p(0.0F, 7.0F, 10.8F), 0.9F, 0.35F, 0.5F, 0.3F, VehGeo.p(0.0F, 0.0F, 1.0F));
      g.push();
      VehGeo.steer(g, VehGeo.p(0.0F, 10.0F, 11.9F), 14.4F, a.lean * 20.0F);
      VehGeo.pipe(g, gun, new float[][]{{0.0F, 7.9F, 12.8F}, {0.0F, 16.0F, 10.5F}}, 0.45F, 8, false);
      VehGeo.beam(g, gun, VehGeo.p(-1.4F, 8.2F, 12.7F), VehGeo.p(-1.4F, wr, zf), 0.45F, 0.8F);
      VehGeo.rbox(g, gun, -1.75F, 7.6F, 12.1F, 0.4F, 8.6F, 13.4F, 0.25F);
      VehGeo.strip(g, P, VehGeo.arc(0.0F, wr, zf, wr + 0.5F, 10.0F, 150.0F, 10), 0.3F, 2.5F, VehGeo.p(1.0F, 0.0F, 0.0F), true);
      wheelAt(g, 0.0F, wr, zf, wr, 1.9F, 2.1F, 0, spin, "veh_rim", null);
      VehGeo.ellipsoid(g, P, 0.0F, 16.4F, 10.4F, 2.6F, 1.0F, 1.9F, 14, 7);
      g.push();
      g.translate(0.0F, 16.3F, 11.7F);
      g.rotX(90.0F);
      VehGeo.lathe(g, cr, new float[][]{{0.01F, -0.6F}, {1.05F, -0.6F}, {1.15F, 0.1F}, {1.0F, 0.2F}}, 14);
      g.glow(true);
      VehGeo.lathe(g, "veh_glow:FFF6D8", new float[][]{{1.0F, 0.2F}, {0.01F, 0.45F}}, 14);
      g.glow(false);
      g.pop();
      VehGeo.ellipsoid(g, "veh_flat:15161A", 0.0F, 17.5F, 9.6F, 0.95F, 0.3F, 0.75F, 10, 5);
      VehGeo.tube(g, cr, VehGeo.p(-5.7F, 15.6F, 10.6F), VehGeo.p(5.7F, 15.6F, 10.6F), 0.35F, 0.35F, 8, false);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.tube(g, "veh_gripr", VehGeo.p(s * 4.6F, 15.6F, 10.6F), VehGeo.p(s * 6.6F, 15.6F, 10.6F), 0.62F, 0.62F, 10, true);
         VehGeo.pipe(g, cr, new float[][]{{s * 3.8F, 15.9F, 10.4F}, {s * 5.0F, 18.4F, 10.1F}}, 0.12F, 4, false);
         g.push();
         g.translate(s * 5.05F, 18.8F, 10.0F);
         g.rotX(-90.0F);
         VehGeo.lathe(g, new String[]{cr, cr, "veh_mirror"}, new float[][]{{0.01F, -0.25F}, {0.95F, -0.25F}, {0.95F, 0.05F}, {0.01F, 0.1F}}, 12);
         g.pop();
      }

      g.pop();
      g.pop();
   }

   static void motorbike(G g, Cos.A a) {
      String P = "veh_paint:FF7A00";
      String blk = "veh_flat:1A1B20";
      String alu = "veh_c:A9B0BC";
      String gun = "veh_gun";
      String cr = "veh_chrome";
      String gold = "veh_c:E0A21A";
      float wr = 4.4F;
      float zr = -10.0F;
      float zf = 15.2F;
      float spin = a.wheel * 0.7F;
      g.push();
      g.rotZ(-a.lean * 14.0F);
      g.push();
      g.translate(0.0F, wr, zr);
      g.rotY(180.0F);
      VehGeo.wheel(g, wr, 2.8F, 3.1F, 5, -spin, "veh_tread", "veh_side", "veh_c:1A1B20", "veh_disc", 14);
      g.pop();

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.strip(
            g, alu, new float[][]{{s * 2.0F, 7.4F, -1.4F}, {s * 1.85F, 5.8F, -6.0F}, {s * 1.75F, wr, zr}}, 1.3F, 0.55F, VehGeo.p(1.0F, 0.0F, 0.0F), true
         );
         VehGeo.strip(
            g,
            alu,
            new float[][]{{s * 1.5F, 13.3F, 10.2F}, {s * 3.0F, 11.3F, 4.0F}, {s * 2.9F, 9.2F, -0.2F}, {s * 2.2F, 7.4F, -1.6F}},
            2.0F,
            0.8F,
            VehGeo.p(1.0F, 0.0F, 0.0F),
            true
         );
      }

      VehGeo.tube(g, cr, VehGeo.p(-2.3F, 7.4F, -1.5F), VehGeo.p(2.3F, 7.4F, -1.5F), 0.35F, 0.35F, 8, true);
      VehGeo.tube(g, gold, VehGeo.p(0.0F, 7.3F, -4.3F), VehGeo.p(0.0F, 11.3F, -1.8F), 0.55F, 0.55F, 8, true);
      VehGeo.rbox(g, gun, -2.7F, 3.4F, -1.8F, 2.7F, 9.0F, 7.0F, 0.8F);
      g.push();
      g.translate(0.0F, 8.4F, 5.4F);
      g.rotX(28.0F);
      VehGeo.rbox(g, alu, -2.5F, 0.0F, -1.9F, 2.5F, 3.2F, 1.9F, 0.4F);

      for (int i = 0; i < 4; i++) {
         VehGeo.box(g, alu, -2.8F, 0.3F + i * 0.7F, -2.1F, 2.8F, 0.6F + i * 0.7F, 2.1F);
      }

      g.pop();
      VehGeo.box6(g, blk, new String[]{"veh_grille", null, null, null, null, null}, -3.0F, 6.2F, 6.9F, 3.0F, 11.2F, 7.8F);
      g.push();
      g.translate(2.7F, 5.8F, 2.0F);
      g.rotZ(-90.0F);
      VehGeo.lathe(g, new String[]{alu, alu, cr}, new float[][]{{2.0F, -0.3F}, {2.0F, 0.2F}, {1.2F, 0.45F}, {0.01F, 0.5F}}, 14);
      g.pop();

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.pipe(
            g,
            cr,
            new float[][]{
               {s * 1.1F, 7.0F, 7.6F}, {s * 1.1F, 3.6F, 8.1F}, {s * 0.9F, 2.4F, 5.0F}, {1.3F, 2.3F, 0.5F}, {2.3F, 2.6F, -2.2F}, {3.3F + s * 0.25F, 4.5F, -4.8F}
            },
            0.4F,
            6,
            true
         );
      }

      float[] s0 = VehGeo.p(3.3F, 4.6F, -4.8F);
      float[] s1 = VehGeo.p(3.9F, 7.3F, -10.6F);
      VehGeo.tube(g, "veh_carbon", s0, s1, 1.15F, 0.95F, 12, true);
      VehGeo.tube(g, cr, VehGeo.lerp(s0, s1, -0.04F), VehGeo.lerp(s0, s1, 0.05F), 1.22F, 1.22F, 12, true);
      VehGeo.tube(g, cr, VehGeo.lerp(s0, s1, 0.97F), VehGeo.lerp(s0, s1, 1.06F), 1.0F, 0.75F, 12, true);
      VehGeo.tube(g, blk, VehGeo.lerp(s0, s1, 1.05F), VehGeo.lerp(s0, s1, 1.07F), 0.5F, 0.5F, 8, true);
      g.push();
      g.translate(-1.6F, wr, zr);
      g.rotX(spin);
      g.rotZ(-90.0F);
      VehGeo.lathe(g, gun, new float[][]{{0.01F, -0.15F}, {1.8F, -0.15F}, {1.8F, 0.15F}, {0.01F, 0.15F}}, 16);
      g.pop();
      VehGeo.strip(g, "veh_chain", new float[][]{{-1.6F, wr + 1.85F, zr}, {-1.6F, 7.9F, -0.2F}}, 0.3F, 0.46F, VehGeo.p(1.0F, 0.0F, 0.0F), true);
      VehGeo.strip(g, "veh_chain", new float[][]{{-1.6F, wr - 1.85F, zr}, {-1.6F, 5.6F, -0.2F}}, 0.3F, 0.46F, VehGeo.p(1.0F, 0.0F, 0.0F), true);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.beam(g, alu, VehGeo.p(s * 2.4F, 7.2F, -1.9F), VehGeo.p(s * 2.9F, 6.2F, -3.4F), 0.5F, 0.35F);
         VehGeo.tube(g, "veh_rubber", VehGeo.p(s * 2.8F, 6.2F, -3.2F), VehGeo.p(s * 4.3F, 6.2F, -3.2F), 0.3F, 0.3F, 6, true);
      }

      VehGeo.loft(g, VehGeo.range(0.4F, 8.8F, 1.2F), z -> {
         float t = (z - 0.4F) / 8.4F;
         float w = VehGeo.lerp(3.0F, 4.3F, VehGeo.smooth(0.0F, 0.35F, t)) - VehGeo.smooth(0.7F, 1.0F, t) * 1.3F;
         float top = VehGeo.lerp(13.2F, 14.9F, VehGeo.smooth(0.0F, 0.45F, t)) - VehGeo.smooth(0.75F, 1.0F, t) * 0.9F;
         float bot = 10.4F;
         int n = 16;
         float[][] r = new float[n][];

         for (int i = 0; i < n; i++) {
            double an = (-Math.PI / 2) + (Math.PI * 2) * i / n;
            float[] q = Geo.sq(an, 1.0F, 2.5F);
            r[i] = new float[]{q[0] * w, q[1] < 0.0F ? bot + (1.0F + q[1]) * 1.2F : bot + 1.2F + q[1] * (top - bot - 1.2F)};
         }

         return r;
      }, (k, j, z, pa, pb) -> P, true, true, 15.0F, 10.0F);
      g.push();
      g.translate(0.0F, 14.65F, 5.4F);
      VehGeo.lathe(g, alu, new float[][]{{0.01F, 0.0F}, {0.9F, 0.0F}, {0.9F, 0.45F}, {0.01F, 0.5F}}, 10);
      g.pop();
      VehGeo.rbox(g, "veh_leather:1A1A1E", -2.9F, 11.9F, -5.6F, 2.9F, 13.2F, 1.3F, 0.6F);
      VehGeo.loft(
         g,
         VehGeo.range(-15.2F, -4.6F, 1.5F),
         z -> {
            float t = (z + 15.2F) / 10.6F;
            float w = VehGeo.lerp(1.2F, 3.3F, t);
            float bot = VehGeo.lerp(13.0F, 10.4F, t);
            float top = VehGeo.lerp(14.5F, 13.0F, t * t) + (t > 0.2F && t < 0.75F ? 0.6F * VehGeo.sin((t - 0.2F) / 0.55F * Math.PI) : 0.0F);
            return new float[][]{
               {0.0F, bot},
               {w * 0.8F, bot},
               {w, bot + (top - bot) * 0.4F},
               {w * 0.85F, top - 0.2F},
               {w * 0.4F, top},
               {0.0F, top},
               {-w * 0.4F, top},
               {-w * 0.85F, top - 0.2F},
               {-w, bot + (top - bot) * 0.4F},
               {-w * 0.8F, bot}
            };
         },
         (k, j, z, pa, pb) -> P,
         true,
         true,
         15.0F,
         10.0F
      );
      g.glow(true);
      VehGeo.ellipsoid(g, "veh_glow:FF2A2A", 0.0F, 13.7F, -15.15F, 1.0F, 0.35F, 0.3F, 10, 5);
      g.glow(false);
      VehGeo.beam(g, blk, VehGeo.p(0.0F, 12.2F, -13.0F), VehGeo.p(0.0F, 9.4F, -15.6F), 1.6F, 0.3F, 1.6F, 0.3F, VehGeo.p(0.0F, 0.0F, 1.0F));
      VehGeo.box6(g, blk, new String[]{null, "veh_plate", null, null, null, null}, -2.0F, 8.2F, -16.0F, 2.0F, 10.2F, -15.6F);
      g.push();
      VehGeo.steer(g, VehGeo.p(0.0F, 9.1F, 13.1F), 24.0F, a.lean * 14.0F);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.tube(g, gold, VehGeo.p(s * 1.75F, 14.0F, 10.9F), VehGeo.p(s * 1.75F, 8.4F, 13.4F), 0.62F, 0.62F, 10, true);
         VehGeo.tube(g, blk, VehGeo.p(s * 1.75F, 8.8F, 13.2F), VehGeo.p(s * 1.75F, wr, zf), 0.45F, 0.45F, 8, true);
         VehGeo.rbox(g, "veh_c:D8141B", s < 0 ? -1.9F : 0.9F, wr + 1.1F, zf - 2.4F, s < 0 ? -0.9F : 1.9F, wr + 2.6F, zf - 0.9F, 0.2F);
      }

      VehGeo.rbox(g, alu, -2.6F, 13.4F, 10.2F, 2.6F, 14.1F, 11.7F, 0.25F);
      g.push();
      g.translate(0.0F, wr, zf);
      VehGeo.wheel(g, wr, 2.2F, 3.1F, 5, spin, "veh_tread", "veh_side", "veh_c:1A1B20", "veh_disc", 14);
      g.pop();
      VehGeo.strip(g, P, VehGeo.arc(0.0F, wr, zf, wr + 0.45F, 25.0F, 150.0F, 10), 0.25F, 2.0F, VehGeo.p(1.0F, 0.0F, 0.0F), true);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.tube(g, alu, VehGeo.p(s * 1.6F, 13.95F, 10.6F), VehGeo.p(s * 4.2F, 13.95F, 10.1F), 0.3F, 0.3F, 6, false);
         VehGeo.tube(g, "veh_gripr", VehGeo.p(s * 3.6F, 14.0F, 10.2F), VehGeo.p(s * 5.3F, 14.0F, 9.9F), 0.55F, 0.55F, 10, true);
      }

      g.pop();
      VehGeo.loft(
         g,
         VehGeo.range(8.0F, 18.4F, 1.3F),
         z -> {
            float t = (z - 8.0F) / 10.4F;
            float w = VehGeo.lerp(4.4F, 1.4F, t * t);
            float top = VehGeo.lerp(16.0F, 13.2F, t * t);
            float bot = VehGeo.curve(z, new float[]{8.0F, 10.5F, 12.0F, 18.4F}, new float[]{8.4F, 9.2F, 10.0F, 10.8F});
            float mid = (top + bot) / 2.0F;
            float h = top - bot;
            float y2 = bot + h * 0.25F;
            float y3 = bot + h * 0.6F;
            float y4 = bot + h * 0.88F;
            return new float[][]{
               {0.0F, bot},
               {w * 0.7F, bot},
               {w, y2},
               {w, y3},
               {w * 0.8F, y4},
               {w * 0.35F, top},
               {0.0F, top},
               {-w * 0.35F, top},
               {-w * 0.8F, y4},
               {-w, y3},
               {-w, y2},
               {-w * 0.7F, bot}
            };
         },
         (k, j, z, pa, pb) -> P,
         true,
         true,
         16.5F,
         5.5F
      );
      g.push();
      g.translate(0.0F, 15.1F, 12.6F);
      g.rotX(-40.0F);
      VehGeo.rbox(g, "veh_tint", -2.6F, 0.0F, -0.1F, 2.6F, 4.2F, 0.1F, 0.05F);
      g.pop();
      g.glow(true);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.ellipsoid(g, "veh_glow:FFF6D8", s * 1.25F, 12.6F, 17.4F, 1.05F, 0.5F, 0.85F, 10, 5);
      }

      g.glow(false);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.beam(g, blk, VehGeo.p(s * 3.6F, 15.0F, 11.0F), VehGeo.p(s * 5.0F, 15.6F, 11.4F), 0.3F, 0.3F);
         VehGeo.ellipsoid(g, blk, s * 5.4F, 15.7F, 11.4F, 1.0F, 0.55F, 0.5F, 10, 5);
         VehGeo.ellipsoid(g, "veh_mirror", s * 5.4F, 15.7F, 10.95F, 0.85F, 0.45F, 0.12F, 10, 5);
      }

      g.pop();
   }
}
