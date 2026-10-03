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

   private static void wheelAt(G var0, float var1, float var2, float var3, float var4, float var5, float var6, int var7, float var8, String var9, String var10) {
      var0.push();
      var0.translate(var1, var2, var3);
      VehGeo.wheel(var0, var4, var5, var6, var7, var8, "veh_tread", "veh_side", var9, var10, 16);
      var0.pop();
   }

   static void kickScooter(G var0, Cos.A var1) {
      String var2 = "veh_alu";
      String var3 = "veh_c:E3242B";
      String var4 = "veh_flat:1C1D22";
      float var5 = 1.7F;
      float var6 = 9.9F;
      float var7 = -8.4F;
      float var8 = var1.wheel * 1.8F;
      var0.push();
      var0.rotZ(-var1.lean * 5.0F);
      VehGeo.rbox(var0, var2, -2.1F, 1.05F, -6.6F, 2.1F, 1.75F, 6.1F, 0.3F);
      VehGeo.box(var0, "veh_griptape", -1.8F, 1.6F, -6.2F, 1.8F, 1.95F, 5.5F);
      VehGeo.box(var0, var3, -2.2F, 1.2F, -6.0F, 2.2F, 1.45F, 5.4F);

      for (byte var9 = -1; var9 <= 1; var9 += 2) {
         VehGeo.beam(var0, var2, VehGeo.p(var9 * 0.95F, 1.35F, -6.3F), VehGeo.p(var9 * 0.95F, var5, var7), 0.35F, 0.9F);
      }

      wheelAt(var0, 0.0F, var5, var7, var5, 1.1F, 1.05F, 5, var8, "veh_pu", null);
      VehGeo.tube(var0, "veh_chrome", VehGeo.p(-1.15F, var5, var7), VehGeo.p(1.15F, var5, var7), 0.22F, 0.22F, 6, true);
      var0.push();
      VehGeo.strip(var0, var4, VehGeo.arc(0.0F, var5, var7, var5 + 0.35F, 70.0F, 175.0F, 8), 0.3F, 1.6F, VehGeo.p(1.0F, 0.0F, 0.0F), true);
      VehGeo.box(var0, "veh_rubber", -0.65F, var5 + 2.05F, var7 - 1.2F, 0.65F, var5 + 2.35F, var7 + 0.4F);
      var0.pop();
      VehGeo.beam(var0, var2, VehGeo.p(0.0F, 1.4F, 5.6F), VehGeo.p(0.0F, 3.6F, 8.0F), 1.6F, 1.0F);
      float[] var11 = new float[]{0.0F, 5.0F, 8.3F};
      var0.push();
      VehGeo.steer(var0, var11, 4.0F, var1.lean * 22.0F);
      VehGeo.tube(var0, var3, VehGeo.p(0.0F, 2.9F, 8.45F), VehGeo.p(0.0F, 5.9F, 8.25F), 0.62F, 0.62F, 10, true);
      VehGeo.rbox(var0, var2, -1.15F, 2.55F, 8.0F, 1.15F, 3.2F, 9.0F, 0.2F);

      for (byte var10 = -1; var10 <= 1; var10 += 2) {
         VehGeo.beam(var0, var2, VehGeo.p(var10 * 0.9F, 2.8F, 8.6F), VehGeo.p(var10 * 0.9F, var5, var6), 0.35F, 0.7F);
      }

      wheelAt(var0, 0.0F, var5, var6, var5, 1.1F, 1.05F, 5, var8, "veh_pu", null);
      VehGeo.tube(var0, "veh_chrome", VehGeo.p(-1.15F, var5, var6), VehGeo.p(1.15F, var5, var6), 0.22F, 0.22F, 6, true);
      VehGeo.tube(var0, var2, VehGeo.p(0.0F, 5.9F, 8.25F), VehGeo.p(0.0F, 11.0F, 8.05F), 0.48F, 0.48F, 8, false);
      VehGeo.tube(var0, var2, VehGeo.p(0.0F, 10.4F, 8.07F), VehGeo.p(0.0F, 17.3F, 7.8F), 0.38F, 0.38F, 8, false);
      var0.push();
      var0.translate(0.0F, 10.6F, 8.06F);
      VehGeo.lathe(var0, var3, new float[][]{{0.01F, -0.5F}, {0.66F, -0.5F}, {0.7F, 0.5F}, {0.01F, 0.5F}}, 10);
      var0.pop();
      VehGeo.beam(var0, var3, VehGeo.p(0.6F, 10.9F, 8.1F), VehGeo.p(0.9F, 9.2F, 8.7F), 0.25F, 0.2F);
      VehGeo.tube(var0, var2, VehGeo.p(-5.6F, 17.3F, 7.8F), VehGeo.p(5.6F, 17.3F, 7.8F), 0.36F, 0.36F, 8, false);
      VehGeo.rbox(var0, var2, -0.6F, 16.8F, 7.3F, 0.6F, 17.8F, 8.3F, 0.2F);

      for (byte var12 = -1; var12 <= 1; var12 += 2) {
         VehGeo.tube(var0, "veh_foam", VehGeo.p(var12 * 3.9F, 17.3F, 7.8F), VehGeo.p(var12 * 6.6F, 17.3F, 7.8F), 0.62F, 0.62F, 10, true);
      }

      var0.pop();
      float var13 = VehGeo.smooth(0.05F, 0.3F, var1.move);
      VehGeo.beam(var0, "veh_gun", VehGeo.p(-2.0F, 1.2F, -1.5F), VehGeo.lerp(VehGeo.p(-3.4F, 0.12F, -3.2F), VehGeo.p(-2.25F, 1.1F, -5.2F), var13), 0.3F, 0.3F);
      var0.pop();
   }

   static void eScooter(G var0, Cos.A var1) {
      String var2 = "veh_c:2F323A";
      String var3 = "veh_gun";
      String var4 = "veh_c:1ED760";
      float var5 = 2.3F;
      float var6 = 10.6F;
      float var7 = -9.6F;
      float var8 = var1.wheel * 1.3F;
      var0.push();
      var0.rotZ(-var1.lean * 5.0F);
      VehGeo.rbox(var0, var2, -2.5F, 1.45F, -7.2F, 2.5F, 2.6F, 6.6F, 0.45F);
      VehGeo.box(var0, "veh_rubber", -2.05F, 2.45F, -6.6F, 2.05F, 2.85F, 6.0F);

      for (byte var9 = -1; var9 <= 1; var9 += 2) {
         VehGeo.box(var0, var4, var9 < 0 ? -2.58F : 2.42F, 1.85F, -6.4F, var9 < 0 ? -2.42F : 2.58F, 2.05F, 5.6F);
      }

      for (byte var11 = -1; var11 <= 1; var11 += 2) {
         VehGeo.beam(var0, var2, VehGeo.p(var11 * 1.05F, 2.0F, -6.9F), VehGeo.p(var11 * 1.05F, var5, var7), 0.4F, 1.0F);
      }

      var0.push();
      var0.translate(0.0F, var5, var7);
      var0.rotY(180.0F);
      VehGeo.wheel(var0, var5, 1.3F, 1.45F, 5, -var8, "veh_tread", "veh_side", "veh_gun", "veh_disc", 16);
      var0.pop();
      VehGeo.tube(var0, "veh_chrome", VehGeo.p(-1.35F, var5, var7), VehGeo.p(1.35F, var5, var7), 0.25F, 0.25F, 6, true);
      VehGeo.strip(var0, var2, VehGeo.arc(0.0F, var5, var7, var5 + 0.45F, 55.0F, 192.0F, 10), 0.28F, 1.7F, VehGeo.p(1.0F, 0.0F, 0.0F), true);
      var0.glow(true).color(VehGeo.argb((int)(200.0F + 55.0F * VehGeo.sin(var1.time * 4.0F)), 16777215));
      float[] var12 = VehGeo.arc(0.0F, var5, var7, var5 + 0.6F, 180.0F, 180.0F, 1)[0];
      VehGeo.rbox(var0, "veh_glow:FF2A2A", -0.6F, var12[1] - 0.15F, var12[2] - 0.2F, 0.6F, var12[1] + 0.45F, var12[2] + 0.3F, 0.1F);
      var0.glow(false).color(-1);
      VehGeo.beam(var0, var2, VehGeo.p(0.0F, 2.0F, 5.9F), VehGeo.p(0.0F, 4.5F, 8.5F), 1.8F, 1.2F);
      VehGeo.rbox(var0, var3, -0.95F, 4.2F, 8.1F, 0.95F, 5.6F, 9.3F, 0.3F);
      VehGeo.tube(var0, "veh_chrome", VehGeo.p(-1.1F, 4.9F, 8.4F), VehGeo.p(1.1F, 4.9F, 8.4F), 0.28F, 0.28F, 8, true);
      VehGeo.beam(var0, var4, VehGeo.p(1.05F, 5.3F, 8.6F), VehGeo.p(1.05F, 4.0F, 7.5F), 0.22F, 0.3F);
      var0.push();
      VehGeo.steer(var0, VehGeo.p(0.0F, 5.0F, 9.2F), 5.0F, var1.lean * 20.0F);
      VehGeo.tube(var0, var2, VehGeo.p(0.0F, 5.3F, 9.2F), VehGeo.p(0.0F, 18.0F, 8.1F), 0.6F, 0.55F, 10, false);
      VehGeo.rbox(var0, var3, -1.35F, 4.9F, 8.6F, 1.35F, 5.9F, 9.8F, 0.3F);

      for (byte var10 = -1; var10 <= 1; var10 += 2) {
         VehGeo.beam(var0, var3, VehGeo.p(var10 * 1.05F, 5.2F, 9.4F), VehGeo.p(var10 * 1.05F, var5, var6), 0.45F, 0.7F);
      }

      VehGeo.strip(var0, var2, VehGeo.arc(0.0F, var5, var6, var5 + 0.4F, 25.0F, 150.0F, 8), 0.26F, 1.6F, VehGeo.p(1.0F, 0.0F, 0.0F), true);
      var0.push();
      var0.translate(0.0F, var5, var6);
      VehGeo.wheel(var0, var5, 1.3F, 1.5F, 0, var8, "veh_tread", "veh_side", "veh_gun", null, 16);
      var0.rotZ(-90.0F);
      VehGeo.lathe(
         var0,
         new String[]{var3, var3, var4, var3, var3},
         new float[][]{{0.01F, -0.85F}, {1.1F, -0.85F}, {1.2F, -0.6F}, {1.2F, 0.6F}, {1.1F, 0.85F}, {0.01F, 0.85F}},
         14
      );
      var0.pop();
      VehGeo.rbox(var0, var2, -0.75F, 14.9F, 8.3F, 0.75F, 16.4F, 9.2F, 0.25F);
      var0.glow(true);
      VehGeo.ellipsoid(var0, "veh_glow:FFFBE8", 0.0F, 15.65F, 9.2F, 0.6F, 0.55F, 0.22F, 10, 5);
      var0.glow(false);
      VehGeo.tube(var0, var3, VehGeo.p(-7.0F, 18.4F, 8.0F), VehGeo.p(7.0F, 18.4F, 8.0F), 0.42F, 0.42F, 8, false);
      VehGeo.rbox(var0, var2, -1.4F, 17.6F, 7.2F, 1.4F, 18.95F, 8.9F, 0.35F);
      var0.glow(true);
      VehGeo.box6(var0, "veh_flat:0B1016", new String[]{null, null, null, null, "veh_display", null}, -1.0F, 18.85F, 7.45F, 1.0F, 19.05F, 8.65F);
      var0.glow(false);

      for (byte var13 = -1; var13 <= 1; var13 += 2) {
         VehGeo.tube(var0, "veh_gripr", VehGeo.p(var13 * 4.9F, 18.4F, 8.0F), VehGeo.p(var13 * 7.3F, 18.4F, 8.0F), 0.62F, 0.62F, 10, true);
      }

      VehGeo.rbox(var0, var3, -4.6F, 18.1F, 7.6F, -3.8F, 18.9F, 8.5F, 0.2F);
      VehGeo.beam(var0, var3, VehGeo.p(-4.3F, 18.6F, 8.5F), VehGeo.p(-6.8F, 18.3F, 9.2F), 0.3F, 0.25F);
      VehGeo.rbox(var0, var3, 3.9F, 17.9F, 8.3F, 4.5F, 18.9F, 8.75F, 0.12F);
      var0.push();
      var0.translate(-3.0F, 18.75F, 8.0F);
      VehGeo.lathe(var0, "veh_chrome", new float[][]{{0.01F, 0.0F}, {0.6F, 0.0F}, {0.5F, 0.35F}, {0.01F, 0.45F}}, 10);
      var0.pop();
      var0.pop();
      float var14 = VehGeo.smooth(0.05F, 0.3F, var1.move);
      VehGeo.beam(var0, var3, VehGeo.p(-2.5F, 1.75F, -0.8F), VehGeo.lerp(VehGeo.p(-3.8F, 0.12F, -2.8F), VehGeo.p(-2.65F, 1.4F, -5.2F), var14), 0.35F, 0.35F);
      var0.pop();
   }

   private static void bikeWheel(G var0, float var1) {
      var0.push();
      var0.rotX(var1);
      var0.rotZ(-90.0F);
      VehGeo.lathe(
         var0,
         new String[]{"veh_side", "veh_tread", "veh_tread", "veh_side"},
         new float[][]{{4.75F, -0.3F}, {5.15F, -0.4F}, {5.4F, 0.0F}, {5.15F, 0.4F}, {4.75F, 0.3F}},
         22
      );
      VehGeo.lathe(var0, "veh_alu", new float[][]{{4.75F, 0.3F}, {4.25F, 0.22F}, {4.25F, -0.22F}, {4.75F, -0.3F}}, 22);
      VehGeo.lathe(
         var0,
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
      byte var2 = 20;

      for (int var3 = 0; var3 < var2; var3++) {
         double var4 = (Math.PI * 2) * var3 / var2;
         double var6 = var4 + (var3 % 2 == 0 ? 0.28 : -0.28);
         float var8 = var3 % 4 < 2 ? -0.58F : 0.58F;
         VehGeo.tube(
            var0,
            "veh_chrome",
            VehGeo.p((float)Math.cos(var4) * 0.6F, var8, (float)Math.sin(var4) * 0.6F),
            VehGeo.p((float)Math.cos(var6) * 4.3F, 0.0F, (float)Math.sin(var6) * 4.3F),
            0.07F,
            0.07F,
            3,
            false
         );
      }

      var0.pop();
   }

   static void bike(G var0, Cos.A var1) {
      String var2 = "veh_c:2FA58C";
      String var3 = "veh_chrome";
      String var4 = "veh_gun";
      String var5 = "veh_leather:6A4424";
      float var6 = var1.wheel * 0.55F;
      float var7 = Vehicles.pedalAngle(var1.wheel);
      var0.push();
      var0.rotZ(-var1.lean * 6.0F);
      var0.push();
      var0.translate(RA[0], RA[1], RA[2]);
      bikeWheel(var0, var6);
      var0.pop();
      var0.push();
      var0.translate(1.25F, RA[1], RA[2]);
      var0.rotX(var6);
      var0.rotZ(-90.0F);
      VehGeo.lathe(var0, var4, new float[][]{{0.01F, -0.12F}, {0.95F, -0.12F}, {0.95F, 0.12F}, {0.01F, 0.12F}}, 12);
      var0.pop();
      VehGeo.pipe(var0, var2, new float[][]{BB, {0.0F, 11.8F, -0.25F}}, 0.36F, 8, false);
      VehGeo.pipe(var0, var2, new float[][]{{0.0F, 10.4F, 10.95F}, {0.0F, 7.4F, 8.8F}, {0.0F, 5.3F, 5.4F}, {0.0F, 4.5F, 2.9F}}, 0.4F, 8, false);
      VehGeo.pipe(var0, var2, new float[][]{{0.0F, 11.8F, 10.6F}, {0.0F, 9.2F, 8.1F}, {0.0F, 7.8F, 4.4F}, {0.0F, 7.6F, 1.4F}}, 0.32F, 8, false);

      for (byte var8 = -1; var8 <= 1; var8 += 2) {
         VehGeo.pipe(var0, var2, new float[][]{{var8 * 0.3F, 4.4F, 2.3F}, {var8 * 0.78F, 5.4F, -4.2F}}, 0.24F, 6, false);
         VehGeo.pipe(var0, var2, new float[][]{{var8 * 0.22F, 11.1F, -0.1F}, {var8 * 0.78F, 5.4F, -4.2F}}, 0.22F, 6, false);
      }

      VehGeo.tube(var0, var2, VehGeo.p(0.0F, 9.6F, 11.28F), VehGeo.p(0.0F, 12.75F, 10.3F), 0.52F, 0.52F, 10, true);
      VehGeo.tube(var0, var2, VehGeo.p(-0.9F, BB[1], BB[2]), VehGeo.p(0.9F, BB[1], BB[2]), 0.55F, 0.55F, 10, true);
      VehGeo.tube(var0, var3, VehGeo.p(0.0F, 11.5F, -0.2F), VehGeo.p(0.0F, 12.3F, -0.43F), 0.27F, 0.27F, 6, false);
      VehGeo.loft(
         var0,
         VehGeo.range(-2.5F, 2.9F, 0.6F),
         var0x -> {
            float var1x = (var0x + 2.5F) / 5.4F;
            float var2x = var1x < 0.12F ? VehGeo.lerp(1.6F, 2.3F, var1x / 0.12F) : VehGeo.lerp(2.3F, 0.55F, VehGeo.smooth(0.2F, 0.85F, var1x));
            float var3x = 12.95F - 0.25F * VehGeo.sin(var1x * Math.PI) + (var1x < 0.1F ? (0.1F - var1x) * 1.5F : 0.0F);
            float var4x = 11.95F;
            byte var5x = 12;
            float[][] var6x = new float[var5x][];

            for (int var7x = 0; var7x < var5x; var7x++) {
               double var8x = (-Math.PI / 2) + (Math.PI * 2) * var7x / var5x;
               float var10 = (float)Math.cos(var8x);
               float var11 = (float)Math.sin(var8x);
               var6x[var7x] = new float[]{
                  var10 * var2x,
                  var11 < 0.0F ? var4x + (1.0F + var11) * (var3x - var4x) * 0.5F : var4x + (var3x - var4x) * (0.5F + 0.5F * (float)Math.sqrt(var11))
               };
            }

            return var6x;
         },
         (var1x, var2x, var3x, var4x, var5x) -> var5,
         true,
         true,
         13.0F,
         11.9F
      );

      for (byte var14 = -1; var14 <= 1; var14 += 2) {
         VehGeo.tube(var0, var3, VehGeo.p(var14 * 1.2F, 11.7F, -1.9F), VehGeo.p(var14 * 1.2F, 12.25F, -1.9F), 0.25F, 0.25F, 6, true);
      }

      VehGeo.tube(var0, var3, VehGeo.p(-1.2F, 11.75F, -1.9F), VehGeo.p(1.2F, 11.75F, -1.9F), 0.12F, 0.12F, 4, false);

      for (byte var15 = -1; var15 <= 1; var15 += 2) {
         VehGeo.pipe(var0, var3, new float[][]{{var15 * 1.9F, 10.9F, -1.6F}, {var15 * 1.9F, 10.9F, -9.4F}, {var15 * 0.9F, 5.4F, -4.4F}}, 0.16F, 6, true);
      }

      for (int var16 = 0; var16 < 3; var16++) {
         VehGeo.tube(var0, var3, VehGeo.p(-1.9F, 10.9F, -3.6F - var16 * 2.4F), VehGeo.p(1.9F, 10.9F, -3.6F - var16 * 2.4F), 0.14F, 0.14F, 4, false);
      }

      VehGeo.strip(var0, var2, VehGeo.arc(0.0F, RA[1], RA[2], 5.9500003F, 20.0F, 200.0F, 12), 0.2F, 1.4F, VehGeo.p(1.0F, 0.0F, 0.0F), true);
      var0.glow(true);
      VehGeo.rbox(var0, "veh_glow:FF2A2A", -0.6F, 10.2F, -9.75F, 0.6F, 10.75F, -9.35F, 0.12F);
      var0.glow(false);
      var0.push();
      var0.translate(1.25F, BB[1], BB[2]);
      var0.rotX(var7);
      var0.rotZ(-90.0F);
      VehGeo.lathe(var0, new String[]{var4, "veh_alu", var4}, new float[][]{{0.01F, -0.12F}, {2.0F, -0.12F}, {2.0F, 0.12F}, {0.01F, 0.12F}}, 18);
      var0.pop();
      var0.push();
      var0.translate(0.0F, BB[1], BB[2]);
      var0.rotX(var7);
      VehGeo.tube(var0, "veh_alu", VehGeo.p(-1.75F, 0.0F, 0.0F), VehGeo.p(1.75F, 0.0F, 0.0F), 0.3F, 0.3F, 6, true);
      VehGeo.beam(var0, "veh_alu", VehGeo.p(1.65F, 0.0F, 0.0F), VehGeo.p(1.65F, -2.6F, 0.0F), 0.4F, 0.55F, 0.35F, 0.45F, VehGeo.p(0.0F, 0.0F, 1.0F));
      VehGeo.beam(var0, "veh_alu", VehGeo.p(-1.65F, 0.0F, 0.0F), VehGeo.p(-1.65F, 2.6F, 0.0F), 0.4F, 0.55F, 0.35F, 0.45F, VehGeo.p(0.0F, 0.0F, 1.0F));
      var0.pop();
      float[][] var17 = Vehicles.pedals("veh_bike", var1.wheel);

      for (float[] var12 : var17) {
         float var13 = Math.signum(var12[0]);
         VehGeo.tube(var0, "veh_alu", VehGeo.p(var13 * 1.7F, var12[1], var12[2]), VehGeo.p(var13 * 1.95F, var12[1], var12[2]), 0.14F, 0.14F, 4, false);
         VehGeo.rbox(var0, "veh_flat:1C1D22", var12[0] - 0.8F, var12[1] - 0.2F, var12[2] - 0.6F, var12[0] + 0.8F, var12[1] + 0.2F, var12[2] + 0.6F, 0.1F);
      }

      chain(var0, var7);
      float var18 = VehGeo.smooth(0.05F, 0.3F, var1.move);
      VehGeo.beam(var0, var4, VehGeo.p(-0.7F, 4.6F, -0.8F), VehGeo.lerp(VehGeo.p(-2.6F, 0.12F, -1.8F), VehGeo.p(-0.9F, 4.4F, -4.6F), var18), 0.3F, 0.3F);
      var0.push();
      VehGeo.steer(var0, VehGeo.p(0.0F, 11.2F, 10.75F), 17.8F, var1.lean * 22.0F);
      VehGeo.rbox(var0, var2, -1.1F, 9.1F, 10.8F, 1.1F, 9.8F, 11.8F, 0.2F);

      for (byte var19 = -1; var19 <= 1; var19 += 2) {
         VehGeo.pipe(var0, var2, new float[][]{{var19 * 0.75F, 9.5F, 11.3F}, {var19 * 0.75F, 7.3F, 12.25F}, {var19 * 0.8F, FA[1], FA[2]}}, 0.24F, 6, false);
      }

      var0.push();
      var0.translate(FA[0], FA[1], FA[2]);
      bikeWheel(var0, var6);
      var0.pop();
      VehGeo.strip(var0, var2, VehGeo.arc(0.0F, FA[1], FA[2], 5.9500003F, 5.0F, 160.0F, 11), 0.2F, 1.4F, VehGeo.p(1.0F, 0.0F, 0.0F), true);
      VehGeo.tube(var0, var3, VehGeo.p(0.0F, 12.6F, 10.35F), VehGeo.p(0.0F, 14.2F, 10.1F), 0.3F, 0.3F, 6, true);
      VehGeo.pipe(
         var0,
         var3,
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

      for (byte var20 = -1; var20 <= 1; var20 += 2) {
         VehGeo.tube(var0, var5, VehGeo.p(var20 * 4.85F, 14.6F, 8.65F), VehGeo.p(var20 * 6.05F, 14.6F, 7.0F), 0.45F, 0.45F, 8, true);
      }

      var0.push();
      var0.translate(-2.6F, 14.55F, 10.1F);
      VehGeo.lathe(var0, var3, new float[][]{{0.01F, 0.0F}, {0.55F, 0.0F}, {0.45F, 0.35F}, {0.01F, 0.42F}}, 8);
      var0.pop();
      var0.push();
      var0.translate(0.0F, 9.4F, 12.3F);
      var0.rotX(90.0F);
      VehGeo.lathe(var0, var3, new float[][]{{0.01F, -0.9F}, {0.45F, -0.9F}, {0.8F, -0.1F}, {0.8F, 0.0F}}, 10);
      var0.glow(true);
      VehGeo.lathe(var0, "veh_glow:FFF6D8", new float[][]{{0.8F, 0.0F}, {0.01F, 0.15F}}, 10);
      var0.glow(false);
      var0.pop();
      float var21 = 12.2F;
      float var22 = 16.4F;
      float var23 = 11.6F;
      float var24 = 14.4F;
      VehGeo.box(var0, "veh_basket", -3.0F, var23, var21, 3.0F, var23 + 0.3F, var22);
      VehGeo.box(var0, "veh_basket", -3.0F, var23 + 0.3F, var21, 3.0F, var24, var21 + 0.3F);
      VehGeo.box(var0, "veh_basket", -3.0F, var23 + 0.3F, var22 - 0.3F, 3.0F, var24, var22);
      VehGeo.box(var0, "veh_basket", -3.0F, var23 + 0.3F, var21 + 0.3F, -2.7F, var24 - 0.02F, var22 - 0.3F);
      VehGeo.box(var0, "veh_basket", 2.7F, var23 + 0.3F, var21 + 0.3F, 3.0F, var24 - 0.02F, var22 - 0.3F);
      VehGeo.pipe(var0, var3, new float[][]{{-1.5F, var23 - 0.1F, var21 + 0.5F}, {-0.8F, 9.8F, 12.0F}}, 0.12F, 4, false);
      VehGeo.pipe(var0, var3, new float[][]{{1.5F, var23 - 0.1F, var21 + 0.5F}, {0.8F, 9.8F, 12.0F}}, 0.12F, 4, false);
      var0.pop();
      var0.pop();
   }

   private static void chain(G var0, float var1) {
      float var2 = 2.0F;
      float var3 = 0.95F;
      float var4 = 1.25F;
      ArrayList var5 = new ArrayList();

      for (float[] var9 : VehGeo.arc(var4, BB[1], BB[2], var2, -90.0F, 90.0F, 10)) {
         var5.add(var9);
      }

      for (float[] var23 : VehGeo.arc(var4, RA[1], RA[2], var3, 90.0F, 270.0F, 6)) {
         var5.add(var23);
      }

      var5.add((float[])var5.get(0));
      int var17 = var5.size();
      float[] var19 = new float[var17];

      for (int var21 = 1; var21 < var17; var21++) {
         float[] var24 = (float[])var5.get(var21 - 1);
         float[] var10 = (float[])var5.get(var21);
         var19[var21] = var19[var21 - 1] + (float)Math.sqrt((var10[1] - var24[1]) * (var10[1] - var24[1]) + (var10[2] - var24[2]) * (var10[2] - var24[2]));
      }

      float var22 = var19[var17 - 1];
      byte var25 = 34;
      float var26 = var22 / var25;
      float var11 = (float)Math.toRadians(var1) * var2;

      for (int var12 = 0; var12 < var25; var12++) {
         float var13 = ((var12 * var26 - var11) % var22 + var22) % var22;
         float[] var14 = at(var5, var19, var13);
         float[] var15 = at(var5, var19, Math.min(var22, var13 + var26 * 0.78F));
         VehGeo.beam(var0, "veh_gun", var14, var15, 0.44F, 0.28F);
      }
   }

   private static float[] at(List<float[]> var0, float[] var1, float var2) {
      int var3 = 1;

      while (var3 < var1.length - 1 && var1[var3] < var2) {
         var3++;
      }

      float var4 = var1[var3] == var1[var3 - 1] ? 0.0F : (var2 - var1[var3 - 1]) / (var1[var3] - var1[var3 - 1]);
      return VehGeo.lerp((float[])var0.get(var3 - 1), (float[])var0.get(var3), VehGeo.clamp(var4, 0.0F, 1.0F));
   }

   static void moped(G var0, Cos.A var1) {
      String var2 = "veh_paint:8FD9C8";
      String var3 = "veh_chrome";
      String var4 = "veh_gun";
      float var5 = 3.4F;
      float var6 = -10.5F;
      float var7 = 13.6F;
      float var8 = var1.wheel * 0.9F;
      var0.push();
      var0.rotZ(-var1.lean * 8.0F);
      wheelAt(var0, 0.0F, var5, var6, var5, 1.9F, 2.1F, 0, var8, "veh_rim", null);
      float[] var9 = new float[]{-15.6F, -13.5F, -9.0F, -4.0F, 0.0F, 1.6F};
      float[] var10 = new float[]{1.8F, 4.4F, 5.6F, 5.4F, 4.3F, 3.0F};
      float[] var11 = new float[]{9.0F, 10.8F, 11.3F, 11.3F, 11.2F, 10.2F};
      float[] var12 = new float[]{7.4F, 5.4F, 4.4F, 4.2F, 4.2F, 4.8F};
      VehGeo.loft(var0, VehGeo.range(-15.6F, 1.6F, 1.1F), var4x -> {
         float var5x = VehGeo.curve(var4x, var9, var10);
         float var6x = VehGeo.curve(var4x, var9, var11);
         float var7x = VehGeo.curve(var4x, var9, var12);
         byte var8x = 18;
         float[][] var9x = new float[var8x][];

         for (int var10x = 0; var10x < var8x; var10x++) {
            double var11x = (-Math.PI / 2) + (Math.PI * 2) * var10x / var8x;
            float[] var13x = Geo.sq(var11x, 1.0F, 2.6F);
            float var14x = var7x + (var6x - var7x) * 0.42F;
            float var15x = var13x[1] < 0.0F ? var7x + (1.0F + var13x[1]) * (var14x - var7x) : var14x + var13x[1] * (var6x - var14x);
            float var16x = var13x[1] > 0.0F ? 1.0F - 0.38F * var13x[1] * var13x[1] : 1.0F - 0.12F * var13x[1] * var13x[1];
            var9x[var10x] = new float[]{var13x[0] * var5x * var16x, var15x};
         }

         return var9x;
      }, (var1x, var2x, var3x, var4x, var5x) -> var2, true, true, 11.5F, 4.0F);
      VehGeo.rbox(var0, "veh_leather:3A2A22", -3.3F, 10.9F, -11.0F, 3.3F, 12.6F, 2.2F, 0.8F);
      VehGeo.pipe(var0, var3, new float[][]{{-2.6F, 11.4F, -11.0F}, {-2.6F, 12.4F, -12.6F}, {2.6F, 12.4F, -12.6F}, {2.6F, 11.4F, -11.0F}}, 0.2F, 6, true);
      var0.glow(true);
      VehGeo.rbox(var0, "veh_glow:FF2A2A", -1.2F, 8.0F, -16.0F, 1.2F, 9.3F, -15.0F, 0.3F);
      var0.glow(false);
      var0.push();
      var0.translate(3.3F, 3.8F, -9.5F);
      var0.rotX(-90.0F);
      VehGeo.lathe(
         var0,
         new String[]{var3, var3, var3, var4, var4, var4},
         new float[][]{{0.01F, -3.6F}, {0.8F, -3.6F}, {1.05F, -2.8F}, {1.05F, 2.4F}, {0.35F, 2.8F}, {0.35F, 2.5F}, {0.01F, 2.5F}},
         12
      );
      var0.pop();
      VehGeo.pipe(var0, var3, new float[][]{{3.3F, 3.8F, -12.1F}, {3.3F, 3.8F, -13.6F}}, 0.3F, 6, true);
      VehGeo.rbox(var0, var2, -3.9F, 2.6F, -1.2F, 3.9F, 3.6F, 9.4F, 0.4F);

      for (int var13 = 0; var13 < 5; var13++) {
         VehGeo.box(var0, "veh_rubber", -3.2F, 3.5F, -0.2F + var13 * 1.8F, 3.2F, 3.75F, 0.8F + var13 * 1.8F);
      }

      VehGeo.box(var0, var4, -2.4F, 1.3F, -2.0F, 2.4F, 2.7F, 6.0F);
      float var15 = VehGeo.smooth(0.05F, 0.3F, var1.move);

      for (byte var14 = -1; var14 <= 1; var14 += 2) {
         VehGeo.beam(
            var0,
            var4,
            VehGeo.p(var14 * 2.2F, 2.0F, -3.0F),
            VehGeo.lerp(VehGeo.p(var14 * 2.6F, 0.1F, -3.8F), VehGeo.p(var14 * 2.4F, 1.4F, -6.2F), var15),
            0.35F,
            0.35F
         );
      }

      var0.push();
      var0.rotX(-90.0F);
      VehGeo.loft(var0, VehGeo.range(3.3F, 16.2F, 1.0F), var0x -> {
         float var1x = var0x < 9.0F ? VehGeo.lerp(3.9F, 5.4F, VehGeo.smooth(3.3F, 9.0F, var0x)) : VehGeo.lerp(5.4F, 3.0F, VehGeo.smooth(9.0F, 16.2F, var0x));
         float var2x = 8.9F - (var0x - 3.3F) * 0.12F;
         float var3x = 1.1F;
         byte var4x = 8;
         float[][] var5x = new float[2 * var4x + 2][];

         for (int var6x = 0; var6x <= var4x; var6x++) {
            float var7x = -var1x + 2.0F * var1x * var6x / var4x;
            float var8x = var7x / var1x;
            var5x[var6x] = new float[]{var7x, -(var2x + var3x * (1.0F - var8x * var8x))};
         }

         for (int var9x = 0; var9x <= var4x; var9x++) {
            float var10x = var1x - 2.0F * var1x * var9x / var4x;
            float var11x = var10x / var1x;
            var5x[var4x + 1 + var9x] = new float[]{var10x, -(var2x + var3x * (1.0F - var11x * var11x) - 0.45F)};
         }

         return var5x;
      }, (var1x, var2x, var3x, var4x, var5x) -> var2, true, true, -12.0F, -4.0F, false, true);
      var0.pop();
      VehGeo.beam(var0, var3, VehGeo.p(0.0F, 12.6F, 10.2F), VehGeo.p(0.0F, 7.0F, 10.8F), 0.9F, 0.35F, 0.5F, 0.3F, VehGeo.p(0.0F, 0.0F, 1.0F));
      var0.push();
      VehGeo.steer(var0, VehGeo.p(0.0F, 10.0F, 11.9F), 14.4F, var1.lean * 20.0F);
      VehGeo.pipe(var0, var4, new float[][]{{0.0F, 7.9F, 12.8F}, {0.0F, 16.0F, 10.5F}}, 0.45F, 8, false);
      VehGeo.beam(var0, var4, VehGeo.p(-1.4F, 8.2F, 12.7F), VehGeo.p(-1.4F, var5, var7), 0.45F, 0.8F);
      VehGeo.rbox(var0, var4, -1.75F, 7.6F, 12.1F, 0.4F, 8.6F, 13.4F, 0.25F);
      VehGeo.strip(var0, var2, VehGeo.arc(0.0F, var5, var7, var5 + 0.5F, 10.0F, 150.0F, 10), 0.3F, 2.5F, VehGeo.p(1.0F, 0.0F, 0.0F), true);
      wheelAt(var0, 0.0F, var5, var7, var5, 1.9F, 2.1F, 0, var8, "veh_rim", null);
      VehGeo.ellipsoid(var0, var2, 0.0F, 16.4F, 10.4F, 2.6F, 1.0F, 1.9F, 14, 7);
      var0.push();
      var0.translate(0.0F, 16.3F, 11.7F);
      var0.rotX(90.0F);
      VehGeo.lathe(var0, var3, new float[][]{{0.01F, -0.6F}, {1.05F, -0.6F}, {1.15F, 0.1F}, {1.0F, 0.2F}}, 14);
      var0.glow(true);
      VehGeo.lathe(var0, "veh_glow:FFF6D8", new float[][]{{1.0F, 0.2F}, {0.01F, 0.45F}}, 14);
      var0.glow(false);
      var0.pop();
      VehGeo.ellipsoid(var0, "veh_flat:15161A", 0.0F, 17.5F, 9.6F, 0.95F, 0.3F, 0.75F, 10, 5);
      VehGeo.tube(var0, var3, VehGeo.p(-5.7F, 15.6F, 10.6F), VehGeo.p(5.7F, 15.6F, 10.6F), 0.35F, 0.35F, 8, false);

      for (byte var16 = -1; var16 <= 1; var16 += 2) {
         VehGeo.tube(var0, "veh_gripr", VehGeo.p(var16 * 4.6F, 15.6F, 10.6F), VehGeo.p(var16 * 6.6F, 15.6F, 10.6F), 0.62F, 0.62F, 10, true);
         VehGeo.pipe(var0, var3, new float[][]{{var16 * 3.8F, 15.9F, 10.4F}, {var16 * 5.0F, 18.4F, 10.1F}}, 0.12F, 4, false);
         var0.push();
         var0.translate(var16 * 5.05F, 18.8F, 10.0F);
         var0.rotX(-90.0F);
         VehGeo.lathe(var0, new String[]{var3, var3, "veh_mirror"}, new float[][]{{0.01F, -0.25F}, {0.95F, -0.25F}, {0.95F, 0.05F}, {0.01F, 0.1F}}, 12);
         var0.pop();
      }

      var0.pop();
      var0.pop();
   }

   static void motorbike(G var0, Cos.A var1) {
      String var2 = "veh_paint:FF7A00";
      String var3 = "veh_flat:1A1B20";
      String var4 = "veh_c:A9B0BC";
      String var5 = "veh_gun";
      String var6 = "veh_chrome";
      String var7 = "veh_c:E0A21A";
      float var8 = 4.4F;
      float var9 = -10.0F;
      float var10 = 15.2F;
      float var11 = var1.wheel * 0.7F;
      var0.push();
      var0.rotZ(-var1.lean * 14.0F);
      var0.push();
      var0.translate(0.0F, var8, var9);
      var0.rotY(180.0F);
      VehGeo.wheel(var0, var8, 2.8F, 3.1F, 5, -var11, "veh_tread", "veh_side", "veh_c:1A1B20", "veh_disc", 14);
      var0.pop();

      for (byte var12 = -1; var12 <= 1; var12 += 2) {
         VehGeo.strip(
            var0,
            var4,
            new float[][]{{var12 * 2.0F, 7.4F, -1.4F}, {var12 * 1.85F, 5.8F, -6.0F}, {var12 * 1.75F, var8, var9}},
            1.3F,
            0.55F,
            VehGeo.p(1.0F, 0.0F, 0.0F),
            true
         );
         VehGeo.strip(
            var0,
            var4,
            new float[][]{{var12 * 1.5F, 13.3F, 10.2F}, {var12 * 3.0F, 11.3F, 4.0F}, {var12 * 2.9F, 9.2F, -0.2F}, {var12 * 2.2F, 7.4F, -1.6F}},
            2.0F,
            0.8F,
            VehGeo.p(1.0F, 0.0F, 0.0F),
            true
         );
      }

      VehGeo.tube(var0, var6, VehGeo.p(-2.3F, 7.4F, -1.5F), VehGeo.p(2.3F, 7.4F, -1.5F), 0.35F, 0.35F, 8, true);
      VehGeo.tube(var0, var7, VehGeo.p(0.0F, 7.3F, -4.3F), VehGeo.p(0.0F, 11.3F, -1.8F), 0.55F, 0.55F, 8, true);
      VehGeo.rbox(var0, var5, -2.7F, 3.4F, -1.8F, 2.7F, 9.0F, 7.0F, 0.8F);
      var0.push();
      var0.translate(0.0F, 8.4F, 5.4F);
      var0.rotX(28.0F);
      VehGeo.rbox(var0, var4, -2.5F, 0.0F, -1.9F, 2.5F, 3.2F, 1.9F, 0.4F);

      for (int var15 = 0; var15 < 4; var15++) {
         VehGeo.box(var0, var4, -2.8F, 0.3F + var15 * 0.7F, -2.1F, 2.8F, 0.6F + var15 * 0.7F, 2.1F);
      }

      var0.pop();
      VehGeo.box6(var0, var3, new String[]{"veh_grille", null, null, null, null, null}, -3.0F, 6.2F, 6.9F, 3.0F, 11.2F, 7.8F);
      var0.push();
      var0.translate(2.7F, 5.8F, 2.0F);
      var0.rotZ(-90.0F);
      VehGeo.lathe(var0, new String[]{var4, var4, var6}, new float[][]{{2.0F, -0.3F}, {2.0F, 0.2F}, {1.2F, 0.45F}, {0.01F, 0.5F}}, 14);
      var0.pop();

      for (byte var16 = -1; var16 <= 1; var16 += 2) {
         VehGeo.pipe(
            var0,
            var6,
            new float[][]{
               {var16 * 1.1F, 7.0F, 7.6F},
               {var16 * 1.1F, 3.6F, 8.1F},
               {var16 * 0.9F, 2.4F, 5.0F},
               {1.3F, 2.3F, 0.5F},
               {2.3F, 2.6F, -2.2F},
               {3.3F + var16 * 0.25F, 4.5F, -4.8F}
            },
            0.4F,
            6,
            true
         );
      }

      float[] var17 = VehGeo.p(3.3F, 4.6F, -4.8F);
      float[] var13 = VehGeo.p(3.9F, 7.3F, -10.6F);
      VehGeo.tube(var0, "veh_carbon", var17, var13, 1.15F, 0.95F, 12, true);
      VehGeo.tube(var0, var6, VehGeo.lerp(var17, var13, -0.04F), VehGeo.lerp(var17, var13, 0.05F), 1.22F, 1.22F, 12, true);
      VehGeo.tube(var0, var6, VehGeo.lerp(var17, var13, 0.97F), VehGeo.lerp(var17, var13, 1.06F), 1.0F, 0.75F, 12, true);
      VehGeo.tube(var0, var3, VehGeo.lerp(var17, var13, 1.05F), VehGeo.lerp(var17, var13, 1.07F), 0.5F, 0.5F, 8, true);
      var0.push();
      var0.translate(-1.6F, var8, var9);
      var0.rotX(var11);
      var0.rotZ(-90.0F);
      VehGeo.lathe(var0, var5, new float[][]{{0.01F, -0.15F}, {1.8F, -0.15F}, {1.8F, 0.15F}, {0.01F, 0.15F}}, 16);
      var0.pop();
      VehGeo.strip(var0, "veh_chain", new float[][]{{-1.6F, var8 + 1.85F, var9}, {-1.6F, 7.9F, -0.2F}}, 0.3F, 0.46F, VehGeo.p(1.0F, 0.0F, 0.0F), true);
      VehGeo.strip(var0, "veh_chain", new float[][]{{-1.6F, var8 - 1.85F, var9}, {-1.6F, 5.6F, -0.2F}}, 0.3F, 0.46F, VehGeo.p(1.0F, 0.0F, 0.0F), true);

      for (byte var14 = -1; var14 <= 1; var14 += 2) {
         VehGeo.beam(var0, var4, VehGeo.p(var14 * 2.4F, 7.2F, -1.9F), VehGeo.p(var14 * 2.9F, 6.2F, -3.4F), 0.5F, 0.35F);
         VehGeo.tube(var0, "veh_rubber", VehGeo.p(var14 * 2.8F, 6.2F, -3.2F), VehGeo.p(var14 * 4.3F, 6.2F, -3.2F), 0.3F, 0.3F, 6, true);
      }

      VehGeo.loft(
         var0,
         VehGeo.range(0.4F, 8.8F, 1.2F),
         var0x -> {
            float var1x = (var0x - 0.4F) / 8.4F;
            float var2x = VehGeo.lerp(3.0F, 4.3F, VehGeo.smooth(0.0F, 0.35F, var1x)) - VehGeo.smooth(0.7F, 1.0F, var1x) * 1.3F;
            float var3x = VehGeo.lerp(13.2F, 14.9F, VehGeo.smooth(0.0F, 0.45F, var1x)) - VehGeo.smooth(0.75F, 1.0F, var1x) * 0.9F;
            float var4x = 10.4F;
            byte var5x = 16;
            float[][] var6x = new float[var5x][];

            for (int var7x = 0; var7x < var5x; var7x++) {
               double var8x = (-Math.PI / 2) + (Math.PI * 2) * var7x / var5x;
               float[] var10x = Geo.sq(var8x, 1.0F, 2.5F);
               var6x[var7x] = new float[]{
                  var10x[0] * var2x, var10x[1] < 0.0F ? var4x + (1.0F + var10x[1]) * 1.2F : var4x + 1.2F + var10x[1] * (var3x - var4x - 1.2F)
               };
            }

            return var6x;
         },
         (var1x, var2x, var3x, var4x, var5x) -> var2,
         true,
         true,
         15.0F,
         10.0F
      );
      var0.push();
      var0.translate(0.0F, 14.65F, 5.4F);
      VehGeo.lathe(var0, var4, new float[][]{{0.01F, 0.0F}, {0.9F, 0.0F}, {0.9F, 0.45F}, {0.01F, 0.5F}}, 10);
      var0.pop();
      VehGeo.rbox(var0, "veh_leather:1A1A1E", -2.9F, 11.9F, -5.6F, 2.9F, 13.2F, 1.3F, 0.6F);
      VehGeo.loft(
         var0,
         VehGeo.range(-15.2F, -4.6F, 1.5F),
         var0x -> {
            float var1x = (var0x + 15.2F) / 10.6F;
            float var2x = VehGeo.lerp(1.2F, 3.3F, var1x);
            float var3x = VehGeo.lerp(13.0F, 10.4F, var1x);
            float var4x = VehGeo.lerp(14.5F, 13.0F, var1x * var1x)
               + (var1x > 0.2F && var1x < 0.75F ? 0.6F * VehGeo.sin((var1x - 0.2F) / 0.55F * Math.PI) : 0.0F);
            return new float[][]{
               {0.0F, var3x},
               {var2x * 0.8F, var3x},
               {var2x, var3x + (var4x - var3x) * 0.4F},
               {var2x * 0.85F, var4x - 0.2F},
               {var2x * 0.4F, var4x},
               {0.0F, var4x},
               {-var2x * 0.4F, var4x},
               {-var2x * 0.85F, var4x - 0.2F},
               {-var2x, var3x + (var4x - var3x) * 0.4F},
               {-var2x * 0.8F, var3x}
            };
         },
         (var1x, var2x, var3x, var4x, var5x) -> var2,
         true,
         true,
         15.0F,
         10.0F
      );
      var0.glow(true);
      VehGeo.ellipsoid(var0, "veh_glow:FF2A2A", 0.0F, 13.7F, -15.15F, 1.0F, 0.35F, 0.3F, 10, 5);
      var0.glow(false);
      VehGeo.beam(var0, var3, VehGeo.p(0.0F, 12.2F, -13.0F), VehGeo.p(0.0F, 9.4F, -15.6F), 1.6F, 0.3F, 1.6F, 0.3F, VehGeo.p(0.0F, 0.0F, 1.0F));
      VehGeo.box6(var0, var3, new String[]{null, "veh_plate", null, null, null, null}, -2.0F, 8.2F, -16.0F, 2.0F, 10.2F, -15.6F);
      var0.push();
      VehGeo.steer(var0, VehGeo.p(0.0F, 9.1F, 13.1F), 24.0F, var1.lean * 14.0F);

      for (byte var18 = -1; var18 <= 1; var18 += 2) {
         VehGeo.tube(var0, var7, VehGeo.p(var18 * 1.75F, 14.0F, 10.9F), VehGeo.p(var18 * 1.75F, 8.4F, 13.4F), 0.62F, 0.62F, 10, true);
         VehGeo.tube(var0, var3, VehGeo.p(var18 * 1.75F, 8.8F, 13.2F), VehGeo.p(var18 * 1.75F, var8, var10), 0.45F, 0.45F, 8, true);
         VehGeo.rbox(var0, "veh_c:D8141B", var18 < 0 ? -1.9F : 0.9F, var8 + 1.1F, var10 - 2.4F, var18 < 0 ? -0.9F : 1.9F, var8 + 2.6F, var10 - 0.9F, 0.2F);
      }

      VehGeo.rbox(var0, var4, -2.6F, 13.4F, 10.2F, 2.6F, 14.1F, 11.7F, 0.25F);
      var0.push();
      var0.translate(0.0F, var8, var10);
      VehGeo.wheel(var0, var8, 2.2F, 3.1F, 5, var11, "veh_tread", "veh_side", "veh_c:1A1B20", "veh_disc", 14);
      var0.pop();
      VehGeo.strip(var0, var2, VehGeo.arc(0.0F, var8, var10, var8 + 0.45F, 25.0F, 150.0F, 10), 0.25F, 2.0F, VehGeo.p(1.0F, 0.0F, 0.0F), true);

      for (byte var19 = -1; var19 <= 1; var19 += 2) {
         VehGeo.tube(var0, var4, VehGeo.p(var19 * 1.6F, 13.95F, 10.6F), VehGeo.p(var19 * 4.2F, 13.95F, 10.1F), 0.3F, 0.3F, 6, false);
         VehGeo.tube(var0, "veh_gripr", VehGeo.p(var19 * 3.6F, 14.0F, 10.2F), VehGeo.p(var19 * 5.3F, 14.0F, 9.9F), 0.55F, 0.55F, 10, true);
      }

      var0.pop();
      VehGeo.loft(
         var0,
         VehGeo.range(8.0F, 18.4F, 1.3F),
         var0x -> {
            float var1x = (var0x - 8.0F) / 10.4F;
            float var2x = VehGeo.lerp(4.4F, 1.4F, var1x * var1x);
            float var3x = VehGeo.lerp(16.0F, 13.2F, var1x * var1x);
            float var4x = VehGeo.curve(var0x, new float[]{8.0F, 10.5F, 12.0F, 18.4F}, new float[]{8.4F, 9.2F, 10.0F, 10.8F});
            float var5x = (var3x + var4x) / 2.0F;
            float var6x = var3x - var4x;
            float var7x = var4x + var6x * 0.25F;
            float var8x = var4x + var6x * 0.6F;
            float var9x = var4x + var6x * 0.88F;
            return new float[][]{
               {0.0F, var4x},
               {var2x * 0.7F, var4x},
               {var2x, var7x},
               {var2x, var8x},
               {var2x * 0.8F, var9x},
               {var2x * 0.35F, var3x},
               {0.0F, var3x},
               {-var2x * 0.35F, var3x},
               {-var2x * 0.8F, var9x},
               {-var2x, var8x},
               {-var2x, var7x},
               {-var2x * 0.7F, var4x}
            };
         },
         (var1x, var2x, var3x, var4x, var5x) -> var2,
         true,
         true,
         16.5F,
         5.5F
      );
      var0.push();
      var0.translate(0.0F, 15.1F, 12.6F);
      var0.rotX(-40.0F);
      VehGeo.rbox(var0, "veh_tint", -2.6F, 0.0F, -0.1F, 2.6F, 4.2F, 0.1F, 0.05F);
      var0.pop();
      var0.glow(true);

      for (byte var20 = -1; var20 <= 1; var20 += 2) {
         VehGeo.ellipsoid(var0, "veh_glow:FFF6D8", var20 * 1.25F, 12.6F, 17.4F, 1.05F, 0.5F, 0.85F, 10, 5);
      }

      var0.glow(false);

      for (byte var21 = -1; var21 <= 1; var21 += 2) {
         VehGeo.beam(var0, var3, VehGeo.p(var21 * 3.6F, 15.0F, 11.0F), VehGeo.p(var21 * 5.0F, 15.6F, 11.4F), 0.3F, 0.3F);
         VehGeo.ellipsoid(var0, var3, var21 * 5.4F, 15.7F, 11.4F, 1.0F, 0.55F, 0.5F, 10, 5);
         VehGeo.ellipsoid(var0, "veh_mirror", var21 * 5.4F, 15.7F, 10.95F, 0.85F, 0.45F, 0.12F, 10, 5);
      }

      var0.pop();
   }
}
