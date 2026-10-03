package dev.lego.cosmetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

final class VehCar {
   static final float CAR_HIP = 5.5F;
   static final float KART_HIP = 3.6F;
   static final float[] CAR_HAND = new float[]{2.9F, 10.4F, 7.8F};
   static final float[] KART_HAND = new float[]{2.6F, 7.5F, 8.6F};
   private static final float[] KZ = new float[]{-30.0F, -28.5F, -25.0F, -18.5F, -12.0F, -6.0F, 0.0F, 6.0F, 12.0F, 19.5F, 25.0F, 29.0F, 31.0F, 32.0F};
   private static final float[] KHC = new float[]{7.4F, 9.4F, 10.5F, 10.9F, 11.1F, 11.3F, 11.3F, 11.4F, 11.5F, 10.3F, 9.3F, 8.2F, 7.5F, 6.2F};
   private static final float[] KCR = new float[]{7.4F, 9.8F, 11.4F, 12.5F, 11.9F, 11.7F, 11.7F, 11.7F, 11.9F, 12.2F, 11.1F, 9.0F, 7.5F, 6.2F};
   private static final float[] KW = new float[]{10.0F, 11.8F, 12.7F, 13.2F, 12.6F, 12.2F, 12.2F, 12.3F, 12.7F, 13.1F, 12.6F, 11.3F, 9.8F, 8.4F};
   private static final float[] KYB = new float[]{4.0F, 2.7F, 2.2F, 2.2F, 2.2F, 2.2F, 2.2F, 2.2F, 2.2F, 2.2F, 2.2F, 2.6F, 3.3F, 4.0F};
   static final float WR = 4.6F;
   static final float WW = 3.4F;
   static final float WX = 10.6F;
   static final float WZF = 19.5F;
   static final float WZR = -18.5F;
   static final float ARCH = 5.5F;
   private static final float FLOOR = 3.0F;

   private VehCar() {
   }

   static String hex(int var0) {
      return String.format("%06X", var0 & 16777215);
   }

   private static float cockpit(float var0) {
      return VehGeo.smooth(-6.9F, -5.7F, var0) * (1.0F - VehGeo.smooth(8.4F, 10.2F, var0));
   }

   private static float arch(float var0) {
      float var1 = -1.0F;

      for (float var5 : new float[]{19.5F, -18.5F}) {
         float var6 = var0 - var5;
         if (Math.abs(var6) < 5.5F) {
            var1 = Math.max(var1, 4.6F + (float)Math.sqrt(30.25F - var6 * var6));
         }
      }

      return var1;
   }

   private static float[][] carHalf(float var0) {
      float var1 = VehGeo.curve(var0, KZ, KW);
      float var2 = VehGeo.curve(var0, KZ, KHC);
      float var3 = VehGeo.curve(var0, KZ, KCR);
      float var4 = VehGeo.curve(var0, KZ, KYB);
      float var5 = var4 + 0.8F;
      float var6 = Math.max(var5, arch(var0));
      float var7 = Math.min(8.0F, var1 - 2.6F);
      float var8 = cockpit(var0);
      float var9 = Math.min(7.3F, var1 - 4.4F);
      float var10 = var3 - var6;
      float var12 = VehGeo.lerp(var3, var2, 0.45F);
      return new float[][]{
         {0.0F, var4},
         {var7 - 0.4F, var4},
         {var7, Math.max(var6, var4 + 0.7F)},
         {var1 - 1.5F, var6},
         {var1, var6 + var10 * 0.36F},
         {var1 - 0.3F, var6 + var10 * 0.74F},
         {var1 - 2.2F, var3},
         {var9 + 1.2F, var12},
         {var9, var12 - 0.6F * var8},
         {var9 - 0.35F, VehGeo.lerp(var12, 3.0F, var8)},
         {Math.min(3.0F, var9 - 0.7F), VehGeo.lerp(var2, 3.0F, var8)},
         {1.2F, VehGeo.lerp(var2, 3.0F, var8)},
         {0.0F, VehGeo.lerp(var2, 3.0F, var8)}
      };
   }

   static void car(G var0, Cos.A var1, int var2, int var3) {
      String var4 = "veh_paint:" + hex(var2);
      String var5 = "veh_paint:" + hex(var3);
      var0.push();
      var0.translate(0.0F, VehGeo.sin(var1.time * 38.0F) * 0.04F * (1.0F - var1.move * 0.5F), 0.0F);
      float var6 = var1.lean * 16.0F;

      for (byte var7 = -1; var7 <= 1; var7 += 2) {
         for (byte var8 = -1; var8 <= 1; var8 += 2) {
            var0.push();
            var0.translate(var7 * 10.6F, 4.6F, var8 > 0 ? 19.5F : -18.5F);
            if (var8 > 0) {
               var0.rotY(var6);
            }

            carWheel(var0, var1.wheel, var7 < 0, 4.6F, 3.4F, 3.2F, 8, "veh_c:D8141B");
            var0.pop();
         }
      }

      var0.translate(0.0F, 4.6F, 0.0F);
      var0.rotZ(-var1.lean * 2.2F);
      var0.translate(0.0F, -4.6F, 0.0F);
      ArrayList var16 = new ArrayList();

      for (float var11 : new float[]{19.5F, -18.5F}) {
         for (float var15 : VehGeo.range(var11 - 5.5F + 0.02F, var11 + 5.5F - 0.02F, 1.8F)) {
            var16.add(var15);
         }

         var16.add(var11 - 5.5F - 0.3F);
         var16.add(var11 + 5.5F + 0.3F);
      }

      for (float var29 : new float[]{-30.0F, -28.5F, -6.9F, -5.7F, 8.4F, 10.2F, 29.0F, 31.0F, 32.0F}) {
         var16.add(var29);
      }

      for (float var30 : VehGeo.range(-30.0F, 32.0F, 4.4F)) {
         if (Math.abs(var30 - 19.5F) > 6.7F && Math.abs(var30 - -18.5F) > 6.7F) {
            var16.add(var30);
         }
      }

      Collections.sort(var16);
      float[] var20 = new float[var16.size()];
      int var24 = 0;

      for (float var31 : (Iterable<Float>) (Iterable<?>) (var16)) {
         if (var24 == 0 || var31 - var20[var24 - 1] > 0.3F) {
            var20[var24++] = var31;
         }
      }

      var20 = Arrays.copyOf(var20, var24);
      int var28 = var20.length;
      VehGeo.loft(var0, var20, var0x -> VehGeo.mirrorRing(carHalf(var0x)), (var3x, var4x, var5x, var6x, var7x) -> {
         if (var3x >= 0 && var3x < var28 - 1) {
            int var8x = var4x < 12 ? var4x : 23 - var4x;
            float var9 = cockpit(var5x);
            boolean var10 = var9 > 0.3F || cockpit(var5x + 1.2F) > 0.3F;
            boolean var11x = arch(var5x) > 0.0F || arch(var5x + 1.0F) > 0.0F;
            if (var8x == 0) {
               return null;
            } else if (var8x <= 2) {
               return "veh_under";
            } else if (var8x == 3) {
               return var11x ? var4 : "veh_carbon";
            } else if (var8x >= 8 && var10) {
               return "veh_interior";
            } else {
               return var8x == 10 && !var10 ? var5 : var4;
            }
         } else {
            return var4;
         }
      }, true, true, 13.2F, 2.0F);
      VehGeo.loft(var0, VehGeo.range(-22.0F, -5.4F, 2.4F), var0x -> {
         float var1x = (var0x + 22.0F) / 16.6F;
         float var2x = VehGeo.lerp(0.9F, 3.7F, (float)Math.sqrt(var1x));
         float var3x = VehGeo.lerp(10.6F, 16.3F, (float)Math.pow(var1x, 0.7));
         float var4x = 9.6F;
         float[][] var5x = new float[10][];
         var5x[0] = new float[]{0.0F, var4x};
         var5x[1] = new float[]{var2x, var4x};

         for (int var6x = 1; var6x <= 7; var6x++) {
            double var7x = Math.PI * var6x / 8.0;
            var5x[1 + var6x] = new float[]{(float)Math.cos(var7x) * var2x, var4x + (float)Math.sin(var7x) * (var3x - var4x)};
         }

         var5x[9] = new float[]{-var2x, var4x};
         return var5x;
      }, (var1x, var2x, var3x, var4x, var5x) -> var4, false, true, 17.0F, 9.0F);
      VehGeo.rbox(var0, "veh_leather:2A2224", -4.3F, 2.6F, -4.2F, 4.3F, 3.6F, 3.6F, 0.45F);
      var0.push();
      var0.translate(0.0F, 3.2F, -4.1F);
      var0.rotX(-11.0F);
      VehGeo.rbox(var0, "veh_leather:2A2224", -4.3F, 0.0F, -1.5F, 4.3F, 10.6F, 0.2F, 0.5F);
      VehGeo.rbox(var0, "veh_leather:C8102E", -4.8F, 0.4F, -1.2F, -3.6F, 8.5F, 1.3F, 0.4F);
      VehGeo.rbox(var0, "veh_leather:C8102E", 3.6F, 0.4F, -1.2F, 4.8F, 8.5F, 1.3F, 0.4F);
      var0.pop();
      VehGeo.rbox(var0, "veh_interior", -7.2F, 7.6F, 9.0F, 7.2F, 11.2F, 11.4F, 0.5F);
      VehGeo.box6(var0, "veh_flat:15161A", new String[]{null, "veh_gauges", null, null, null, null}, -3.4F, 8.5F, 8.7F, 3.4F, 10.7F, 9.2F);
      VehGeo.tube(var0, "veh_gun", VehGeo.p(0.0F, 9.2F, 10.0F), VehGeo.p(0.0F, 10.2F, 8.1F), 0.35F, 0.35F, 6, false);
      var0.push();
      var0.translate(0.0F, 10.4F, 7.8F);
      var0.rotX(-58.0F);
      var0.rotY(-var1.lean * 70.0F);
      VehGeo.torus(var0, "veh_leather:1A1A1E", 2.9F, 0.34F, 12, 4);

      for (int var32 = 0; var32 < 3; var32++) {
         double var40 = Math.toRadians(90 + var32 * 120);
         VehGeo.beam(
            var0,
            "veh_gun",
            VehGeo.p((float)Math.cos(var40) * 0.6F, 0.0F, (float)Math.sin(var40) * 0.6F),
            VehGeo.p((float)Math.cos(var40) * 2.7F, 0.0F, (float)Math.sin(var40) * 2.7F),
            0.55F,
            0.25F,
            0.4F,
            0.2F,
            VehGeo.UP
         );
      }

      VehGeo.lathe(var0, new String[]{"veh_gun", "veh_c:D8141B"}, new float[][]{{0.9F, -0.35F}, {0.9F, 0.2F}, {0.01F, 0.32F}}, 10);
      var0.pop();

      for (int var33 = -1; var33 <= 1; var33++) {
         var0.push();
         var0.translate(var33 * 4.6F, 10.9F, 11.2F - Math.abs(var33) * 0.9F);
         var0.rotY(-var33 * 28);
         var0.rotX(-38.0F);
         VehGeo.box(var0, "veh_glass", -2.4F, 0.0F, -0.08F, 2.4F, 3.6F, 0.08F);
         var0.pop();
      }

      for (byte var34 = -1; var34 <= 1; var34 += 2) {
         VehGeo.beam(var0, "veh_carbon", VehGeo.p(var34 * 11.4F, 11.5F, 9.4F), VehGeo.p(var34 * 13.4F, 12.6F, 9.9F), 0.45F, 0.35F);
         VehGeo.ellipsoid(var0, var4, var34 * 13.9F, 12.8F, 10.1F, 1.45F, 0.85F, 0.8F, 8, 5);
         VehGeo.ellipsoid(var0, "veh_mirror", var34 * 13.9F, 12.8F, 9.45F, 1.2F, 0.66F, 0.2F, 8, 4);
      }

      for (byte var35 = -1; var35 <= 1; var35 += 2) {
         VehGeo.ellipsoid(var0, "veh_flat:111216", var35 * 12.1F, 7.6F, -10.8F, 0.75F, 1.5F, 3.4F, 8, 4);
      }

      for (byte var36 = -1; var36 <= 1; var36 += 2) {
         VehGeo.beam(
            var0, "veh_carbon", VehGeo.p(var36 * 6, 9.6F, -25.0F), VehGeo.p(var36 * 6, 14.3F, -27.2F), 0.5F, 1.6F, 0.5F, 1.2F, VehGeo.p(0.0F, 0.0F, 1.0F)
         );
         VehGeo.box(var0, "veh_carbon", var36 < 0 ? -11.35F : 11.0F, 12.9F, -30.0F, var36 < 0 ? -11.0F : 11.35F, 16.2F, -25.0F);
      }

      var0.push();
      var0.translate(0.0F, 14.6F, -27.4F);
      var0.rotY(90.0F);
      var0.rotZ(-6.0F);
      VehGeo.loft(
         var0,
         new float[]{-11.1F, 11.1F},
         var0x -> new float[][]{{2.3F, 0.0F}, {1.1F, 0.4F}, {-0.6F, 0.65F}, {-1.9F, 0.5F}, {-2.4F, 0.05F}, {-1.9F, -0.28F}, {0.0F, -0.25F}, {1.2F, -0.1F}},
         (var0x, var1x, var2x, var3x, var4x) -> "veh_carbon",
         true,
         true,
         1.0F,
         -1.0F
      );
      var0.pop();
      var0.glow(true);

      for (byte var37 = -1; var37 <= 1; var37 += 2) {
         VehGeo.ellipsoid(var0, "veh_glow:FFF6D8", var37 * 7.9F, 7.6F, 28.3F, 2.3F, 0.95F, 2.2F, 8, 4);
         VehGeo.tube(var0, "veh_glow:FF2A2A", VehGeo.p(var37 * 3.5F, 8.2F, -29.6F), VehGeo.p(var37 * 8.9F, 8.1F, -29.1F), 0.5F, 0.45F, 8, true);
      }

      VehGeo.tube(var0, "veh_glow:FF6A4A", VehGeo.p(-3.5F, 8.2F, -29.6F), VehGeo.p(3.5F, 8.2F, -29.6F), 0.22F, 0.22F, 6, true);
      var0.glow(false);
      VehGeo.box6(var0, "veh_under", new String[]{"veh_grille", null, null, null, null, null}, -6.2F, 3.4F, 29.6F, 6.2F, 6.0F, 32.4F);
      VehGeo.box(var0, "veh_carbon", -9.2F, 2.3F, 29.0F, 9.2F, 2.8F, 32.9F);
      VehGeo.box6(var0, "veh_under", new String[]{null, "veh_plate", null, null, null, null}, -3.2F, 4.9F, -30.45F, 3.2F, 6.9F, -29.6F);
      VehGeo.box(var0, "veh_carbon", -9.0F, 2.25F, -31.0F, 9.0F, 3.3F, -27.0F);

      for (int var38 = -2; var38 <= 2; var38++) {
         VehGeo.box(var0, "veh_carbon", var38 * 3.2F - 0.12F, 3.3F, -31.0F, var38 * 3.2F + 0.12F, 4.4F, -28.0F);
      }

      for (byte var39 = -1; var39 <= 1; var39 += 2) {
         var0.push();
         var0.translate(var39 * 6.6F, 3.9F, -30.2F);
         var0.rotX(-90.0F);
         VehGeo.lathe(var0, new String[]{"veh_chrome", "veh_chrome", "veh_under"}, new float[][]{{0.8F, -1.6F}, {0.8F, 1.1F}, {0.55F, 1.1F}, {0.55F, 0.6F}}, 10);
         VehGeo.lathe(var0, "veh_under", new float[][]{{0.55F, 0.6F}, {0.01F, 0.6F}}, 10);
         var0.pop();
      }

      var0.pop();
   }

   static void carWheel(G var0, float var1, boolean var2, float var3, float var4, float var5, int var6, String var7) {
      var0.push();
      if (var2) {
         var0.rotY(180.0F);
      }

      VehGeo.wheel(var0, var3, var4, var5, var6, var2 ? -var1 : var1, "veh_tread", "veh_side", "veh_rim", var7 != null ? "veh_disc" : null, 10);
      if (var7 != null) {
         var0.rotX(var2 ? 45.0F : -45.0F);
         float var8 = var5 * 0.5F;
         float var9 = var5 - 0.45F;
         VehGeo.box(var0, var7, -0.75F, var8, -0.7F, 0.45F, var9, 0.7F);
      }

      var0.pop();
   }

   static void kart(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, VehGeo.sin(var1.time * 45.0F) * 0.06F, 0.0F);
      String var2 = "veh_c:D81E1E";
      String var3 = "veh_c:FFCD03";

      for (byte var4 = -1; var4 <= 1; var4 += 2) {
         var0.push();
         var0.translate(var4 * 9.2F, 2.2F, 10.5F);
         var0.rotY(var1.lean * 20.0F);
         carWheel(var0, var1.wheel * 1.15F, var4 < 0, 2.2F, 2.0F, 1.35F, 6, null);
         var0.pop();
         var0.push();
         var0.translate(var4 * 9.6F, 2.5F, -10.0F);
         carWheel(var0, var1.wheel, var4 < 0, 2.5F, 3.0F, 1.55F, 6, null);
         var0.pop();
      }

      for (byte var8 = -1; var8 <= 1; var8 += 2) {
         VehGeo.pipe(
            var0,
            var2,
            new float[][]{{var8 * 3.8F, 1.5F, -12.5F}, {var8 * 3.8F, 1.5F, 6.0F}, {var8 * 2.9F, 1.8F, 12.5F}, {var8 * 2.0F, 2.5F, 15.6F}},
            0.38F,
            6,
            true
         );
         VehGeo.pipe(
            var0,
            var2,
            new float[][]{{var8 * 3.8F, 1.6F, -5.5F}, {var8 * 8.8F, 1.9F, -5.2F}, {var8 * 9.0F, 1.9F, 5.2F}, {var8 * 3.8F, 1.6F, 6.5F}},
            0.32F,
            6,
            false
         );
         VehGeo.pipe(var0, "veh_gun", new float[][]{{var8 * 3.2F, 1.7F, 10.5F}, {var8 * 8.0F, 2.2F, 10.5F}}, 0.3F, 6, false);
         VehGeo.tube(var0, "veh_gun", VehGeo.p(var8 * 8.1F, 1.2F, 10.7F), VehGeo.p(var8 * 8.1F, 3.3F, 10.3F), 0.28F, 0.28F, 6, true);
      }

      for (float var7 : new float[]{-12.0F, -6.0F, 3.0F, 12.0F}) {
         VehGeo.tube(var0, var2, VehGeo.p(-3.8F, var7 == 12.0F ? 1.8F : 1.5F, var7), VehGeo.p(3.8F, var7 == 12.0F ? 1.8F : 1.5F, var7), 0.3F, 0.3F, 6, false);
      }

      VehGeo.pipe(var0, var2, new float[][]{{-2.0F, 2.5F, 15.6F}, {2.0F, 2.5F, 15.6F}}, 0.38F, 6, false);
      VehGeo.tube(var0, "veh_chrome", VehGeo.p(-10.9F, 2.5F, -10.0F), VehGeo.p(10.9F, 2.5F, -10.0F), 0.42F, 0.42F, 8, true);
      VehGeo.pipe(var0, "veh_chrome", new float[][]{{-7.0F, 2.2F, -12.5F}, {-7.0F, 2.8F, -15.0F}, {7.0F, 2.8F, -15.0F}, {7.0F, 2.2F, -12.5F}}, 0.36F, 6, true);
      VehGeo.box(var0, "veh_alu", -3.8F, 1.32F, -6.2F, 3.8F, 1.6F, 13.0F);

      for (byte var10 = -1; var10 <= 1; var10 += 2) {
         VehGeo.rbox(var0, var3, var10 < 0 ? -8.9F : 5.0F, 1.6F, -6.6F, var10 < 0 ? -5.0F : 8.9F, 4.3F, 5.6F, 1.1F);
      }

      VehGeo.rbox(var0, var3, -5.8F, 1.7F, 11.6F, 5.8F, 4.4F, 17.2F, 1.5F);
      VehGeo.rbox(var0, var3, -3.2F, 1.7F, 11.2F, 3.2F, 7.0F, 12.4F, 0.4F);
      VehGeo.box6(var0, "veh_flat:F4F4F4", new String[]{"veh_num", null, null, null, null, null}, -2.3F, 2.3F, 12.3F, 2.3F, 6.4F, 12.65F);
      VehGeo.rbox(var0, "veh_flat:1A1B20", -3.6F, 1.45F, -5.6F, 3.6F, 2.6F, 1.6F, 0.6F);
      var0.push();
      var0.translate(0.0F, 2.0F, -5.2F);
      var0.rotX(-14.0F);
      VehGeo.rbox(var0, "veh_flat:1A1B20", -3.8F, 0.0F, -1.3F, 3.8F, 9.6F, 0.1F, 0.6F);
      var0.pop();

      for (byte var11 = -1; var11 <= 1; var11 += 2) {
         VehGeo.rbox(var0, "veh_flat:1A1B20", var11 < 0 ? -4.3F : 3.4F, 1.6F, -5.6F, var11 < 0 ? -3.4F : 4.3F, 5.4F, 0.8F, 0.35F);
      }

      VehGeo.tube(var0, "veh_gun", VehGeo.p(0.0F, 3.0F, 12.0F), VehGeo.p(0.0F, 7.3F, 8.8F), 0.3F, 0.3F, 6, false);
      var0.push();
      var0.translate(0.0F, 7.5F, 8.6F);
      var0.rotX(-52.0F);
      var0.rotY(-var1.lean * 80.0F);
      VehGeo.torus(var0, "veh_flat:15151A", 2.6F, 0.36F, 16, 6);
      VehGeo.beam(var0, "veh_gun", VehGeo.p(-2.5F, 0.0F, 0.0F), VehGeo.p(2.5F, 0.0F, 0.0F), 0.5F, 0.3F);
      VehGeo.lathe(var0, new String[]{"veh_gun", var3}, new float[][]{{0.8F, -0.3F}, {0.8F, 0.2F}, {0.01F, 0.35F}}, 8);
      var0.pop();
      var0.push();
      var0.translate(0.0F, 2.4F, 13.4F);
      VehGeo.lathe(
         var0, "veh_flat:E8E8E0", new float[][]{{0.01F, -0.9F}, {1.3F, -0.8F}, {1.4F, 1.2F}, {0.9F, 1.6F}, {0.5F, 1.7F}, {0.5F, 2.2F}, {0.01F, 2.2F}}, 10
      );
      var0.pop();
      VehGeo.rbox(var0, "veh_gun", 4.3F, 1.8F, -10.2F, 7.6F, 5.4F, -6.2F, 0.5F);

      for (int var12 = 0; var12 < 5; var12++) {
         VehGeo.box(var0, "veh_alu", 4.0F, 5.4F + var12 * 0.55F, -9.4F, 7.9F, 5.65F + var12 * 0.55F, -6.9F);
      }

      VehGeo.tube(var0, "veh_alu", VehGeo.p(5.9F, 5.2F, -8.2F), VehGeo.p(5.9F, 8.3F, -8.2F), 0.9F, 0.9F, 8, true);
      var0.push();
      var0.translate(8.0F, 4.6F, -7.4F);
      var0.rotZ(-90.0F);
      VehGeo.lathe(var0, new String[]{var2, "veh_flat:222228", var2}, new float[][]{{0.01F, -0.1F}, {1.3F, -0.1F}, {1.3F, 2.2F}, {0.01F, 2.2F}}, 10);
      var0.pop();
      VehGeo.pipe(var0, "veh_chrome", new float[][]{{5.9F, 6.6F, -10.4F}, {5.9F, 6.6F, -12.2F}, {4.6F, 5.4F, -13.6F}, {2.6F, 4.6F, -13.8F}}, 0.35F, 6, false);
      VehGeo.tube(var0, "veh_chrome", VehGeo.p(3.0F, 4.6F, -13.8F), VehGeo.p(-2.4F, 4.6F, -13.8F), 0.8F, 0.8F, 8, true);
      var0.push();
      var0.translate(4.9F, 2.5F, -10.0F);
      var0.rotZ(-90.0F);
      VehGeo.lathe(var0, "veh_gun", new float[][]{{0.01F, -0.15F}, {1.6F, -0.15F}, {1.6F, 0.15F}, {0.01F, 0.15F}}, 12);
      var0.pop();
      VehGeo.strip(var0, "veh_chain", new float[][]{{4.9F, 4.1F, -10.0F}, {4.9F, 3.4F, -8.2F}}, 0.25F, 0.5F, VehGeo.p(1.0F, 0.0F, 0.0F), true);
      VehGeo.tube(var0, "veh_alu", VehGeo.p(-7.0F, 2.8F, -14.6F), VehGeo.p(-7.0F, 17.5F, -14.6F), 0.18F, 0.18F, 6, true);

      for (int var13 = 0; var13 < 6; var13++) {
         float var14 = -6.8F + var13 * 0.9F;
         float var15 = var14 + 0.9F;
         float var16 = VehGeo.sin(var1.time * 7.0F - var13 * 0.9) * 0.35F * var13 / 5.0F;
         var0.push();
         var0.translate(0.0F, 0.0F, var16);
         VehGeo.box6(var0, "veh_checker", null, var14, 13.8F - var13 * 0.05F, -14.65F, var15, 17.2F - var13 * 0.05F, -14.55F);
         var0.pop();
      }

      var0.pop();
   }
}
