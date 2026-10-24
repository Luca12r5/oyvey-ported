package dev.lego.cosmetic;

import java.util.Random;

final class VehBoard {
   static final float SKATE_Y = 2.5F;
   static final float SKATE_TH = 0.55F;
   static final float SKATE_TOP = 3.05F;
   static final float HOVER_Y = 3.8F;
   static final float HOVER_TH = 1.3F;
   static final float HOVER_TOP = 5.1F;
   static final float UFO_Y = 3.0F;
   static final float UFO_DECK = 3.4F;
   static final float UFO_TOP = 6.4F;
   static final float CLOUD_Y = 3.5F;
   static final float CLOUD_TOP = 6.0F;

   private VehBoard() {
   }

   private static float skW(float var0) {
      float var1 = Math.abs(var0);
      if (var1 <= 10.0F) {
         return 3.6F;
      } else {
         float var2 = (var1 - 10.0F) / 3.05F;
         return 3.6F * (float)Math.sqrt(Math.max(0.015F, 1.0F - var2 * var2));
      }
   }

   private static float skKick(float var0) {
      float var1 = Math.abs(var0);
      if (var1 <= 8.4F) {
         return 0.0F;
      } else {
         float var2 = (var1 - 8.4F) / 4.6F;
         return (var0 > 0.0F ? 2.3F : 2.0F) * var2 * var2;
      }
   }

   private static float concave(float var0) {
      float var1 = var0 / 3.6F;
      return 0.32F * var1 * var1;
   }

   static void skateboard(G var0, Cos.A var1) {
      var0.push();
      float var2 = -var1.lean * 6.0F;

      for (byte var3 = -1; var3 <= 1; var3 += 2) {
         truck(var0, var1, var3 * 8.3F, var3 * var1.lean * 9.0F);
      }

      var0.translate(0.0F, 1.6F, 0.0F);
      var0.rotZ(var2);
      var0.translate(0.0F, -1.6F, 0.0F);
      VehGeo.loft(
         var0,
         VehGeo.range(-12.97F, 12.97F, 0.9F),
         var0x -> {
            float var1x = skW(var0x);
            float var2x = 2.5F + skKick(var0x);
            float[][] var3x = new float[14][];

            for (int var4x = 0; var4x <= 6; var4x++) {
               float var5x = -var1x + 2.0F * var1x * var4x / 6.0F;
               var3x[var4x] = new float[]{var5x, var2x + concave(var5x)};
            }

            for (int var6x = 0; var6x <= 6; var6x++) {
               float var7x = var1x - 2.0F * var1x * var6x / 6.0F;
               var3x[7 + var6x] = new float[]{var7x, var2x + 0.55F + concave(var7x)};
            }

            return var3x;
         },
         (var0x, var1x, var2x, var3x, var4x) -> var1x < 6 ? "veh_deckart" : (var1x != 6 && var1x != 13 ? "veh_griptape" : "veh_c:D9A86A"),
         true,
         true,
         -3.6F,
         3.6F,
         true
      );

      for (byte var9 = -1; var9 <= 1; var9 += 2) {
         for (byte var4 = -1; var4 <= 1; var4 += 2) {
            for (byte var5 = -1; var5 <= 1; var5 += 2) {
               float var6 = var4 * 0.75F;
               float var7 = var9 * 8.3F + var5 * 0.8F;
               float var8 = 3.05F + concave(var6);
               VehGeo.rbox(var0, "veh_chrome", var6 - 0.2F, var8 - 0.12F, var7 - 0.2F, var6 + 0.2F, var8 + 0.12F, var7 + 0.2F, 0.07F);
            }
         }
      }

      for (byte var10 = -1; var10 <= 1; var10 += 2) {
         float var11 = var10 * 8.3F;
         VehGeo.rbox(var0, "veh_alu", -1.25F, 2.05F, var11 - 1.3F, 1.25F, 2.58F, var11 + 1.3F, 0.2F);
         var0.push();
         var0.translate(0.0F, 1.55F, var11 - var10 * 0.25F);
         VehGeo.lathe(var0, "veh_c:FFCD03", new float[][]{{0.01F, -0.3F}, {0.5F, -0.3F}, {0.55F, 0.0F}, {0.5F, 0.55F}, {0.01F, 0.55F}}, 10);
         var0.pop();
      }

      var0.pop();
   }

   private static void truck(G var0, Cos.A var1, float var2, float var3) {
      var0.push();
      var0.translate(0.0F, 1.2F, var2);
      var0.rotY(var3);
      VehGeo.beam(var0, "veh_alu", VehGeo.p(-2.7F, 0.0F, 0.0F), VehGeo.p(2.7F, 0.0F, 0.0F), 0.62F, 0.7F);
      VehGeo.rbox(var0, "veh_alu", -1.05F, -0.45F, -0.5F, 1.05F, 0.62F, 0.5F, 0.22F);
      VehGeo.tube(var0, "veh_chrome", VehGeo.p(-4.0F, 0.0F, 0.0F), VehGeo.p(4.0F, 0.0F, 0.0F), 0.16F, 0.16F, 6, true);

      for (byte var4 = -1; var4 <= 1; var4 += 2) {
         var0.push();
         var0.translate(var4 * 3.35F, 0.0F, 0.0F);
         if (var4 < 0) {
            var0.rotY(180.0F);
         }

         var0.rotX(var4 < 0 ? -var1.wheel * 2.3F : var1.wheel * 2.3F);
         var0.rotZ(-90.0F);
         VehGeo.lathe(
            var0,
            new String[]{
               "veh_gun", "veh_gun", "veh_urethane", "veh_urethane", "veh_urethane", "veh_urethane", "veh_urethane", "veh_urethane", "veh_gun", "veh_gun"
            },
            new float[][]{
               {0.01F, -0.5F},
               {0.45F, -0.5F},
               {0.5F, -0.56F},
               {1.0F, -0.56F},
               {1.17F, -0.38F},
               {1.2F, 0.0F},
               {1.17F, 0.38F},
               {1.0F, 0.56F},
               {0.5F, 0.56F},
               {0.45F, 0.5F},
               {0.01F, 0.5F}
            },
            14
         );
         var0.pop();
      }

      var0.pop();
   }

   private static float hvW(float var0) {
      float var1 = Math.abs(var0);
      if (var1 <= 8.5F) {
         return 4.0F;
      } else {
         float var2 = (var1 - 8.5F) / 4.55F;
         return 4.0F * (float)Math.sqrt(Math.max(0.02F, 1.0F - var2 * var2));
      }
   }

   private static float hvKick(float var0) {
      if (var0 > 7.5F) {
         float var2 = (var0 - 7.5F) / 5.5F;
         return 1.5F * var2 * var2;
      } else if (var0 < -8.5F) {
         float var1 = (-8.5F - var0) / 4.5F;
         return 0.7F * var1 * var1;
      } else {
         return 0.0F;
      }
   }

   static void hoverboard(G var0, Cos.A var1) {
      float var2 = Vehicles.hoverBob(var1.time);
      var0.push();
      var0.translate(0.0F, 3.8F + var2, 0.0F);
      var0.rotZ(-var1.lean * 10.0F);
      var0.rotX(-var1.move * 4.0F);
      VehGeo.loft(
         var0,
         VehGeo.range(-13.0F, 13.0F, 0.8F),
         var0x -> {
            float var1x = hvW(var0x);
            float var2x = hvKick(var0x);
            float var3x = Math.min(0.6F, var1x * 0.4F);
            return new float[][]{
               {-var1x + var3x, var2x},
               {0.0F, var2x},
               {var1x - var3x, var2x},
               {var1x, var2x + 0.45F},
               {var1x, var2x + 0.85F},
               {var1x - var3x, var2x + 1.3F},
               {0.0F, var2x + 1.3F},
               {-var1x + var3x, var2x + 1.3F},
               {-var1x, var2x + 0.85F},
               {-var1x, var2x + 0.45F}
            };
         },
         (var0x, var1x, var2x, var3x, var4x) -> var1x < 2 ? "veh_c:2A2D3A" : (var1x >= 5 && var1x <= 6 ? "veh_paint:FF3EA5" : "veh_c:1B1D26"),
         true,
         true,
         1.8F,
         -0.2F
      );
      float var3 = 0.75F + 0.25F * VehGeo.sin(var1.time * 5.0F);
      var0.glow(true).color(VehGeo.argb((int)(255.0F * var3), 16777215));
      VehGeo.loft(var0, VehGeo.range(-13.2F, 13.2F, 0.8F), var0x -> {
         float var1x = hvW(var0x * 13.0F / 13.25F) + 0.22F;
         float var2x = hvKick(var0x * 13.0F / 13.25F);
         return new float[][]{{-var1x, var2x + 0.5F}, {var1x, var2x + 0.5F}, {var1x, var2x + 0.78F}, {-var1x, var2x + 0.78F}};
      }, (var0x, var1x, var2x, var3x, var4x) -> "veh_glow:24E0FF", true, true, 1.0F, 0.0F);
      var0.glow(false).color(-1);

      for (byte var4 = -1; var4 <= 1; var4 += 2) {
         VehGeo.rbox(var0, "veh_rubber", -3.0F, 1.1999999F, var4 < 0 ? -7.6F : 2.0F, 3.0F, 1.5799999F, var4 < 0 ? -2.0F : 7.6F, 0.12F);
      }

      for (byte var6 = -1; var6 <= 1; var6 += 2) {
         var0.push();
         var0.translate(0.0F, 0.0F, var6 * 7);
         VehGeo.lathe(
            var0,
            new String[]{"veh_gun", "veh_c:1B1D26", "veh_chrome", "veh_c:1B1D26"},
            new float[][]{{0.01F, -1.4F}, {1.8F, -1.25F}, {2.5F, -0.75F}, {2.7F, -0.2F}, {2.6F, 0.3F}},
            16
         );
         float var5 = 0.7F + 0.3F * VehGeo.sin(var1.time * 11.0F + var6);
         var0.glow(true).color(VehGeo.argb((int)(255.0F * var5), 16777215));
         VehGeo.lathe(var0, "veh_glow:7CF7FF", new float[][]{{0.01F, -1.52F}, {1.5F, -1.52F}}, 16);
         var0.color(VehGeo.argb((int)(150.0F * var5), 16777215));
         var0.translate(0.0F, -1.2F, 0.0F);
         VehGeo.torus(var0, "veh_glow:24E0FF", 2.15F, 0.22F, 20, 6);
         var0.glow(false).color(-1);
         var0.pop();
      }

      var0.pop();
   }

   static void ufo(G var0, Cos.A var1) {
      float var2 = Vehicles.hoverBob(var1.time) * 1.4F;
      var0.push();
      var0.translate(0.0F, 3.0F + var2, 0.0F);
      var0.rotZ(-var1.lean * 8.0F);
      String[] var3 = new String[]{"veh_gun", "veh_gun", "veh_ufo", "veh_ufo", "veh_chrome", "veh_chrome", "veh_ufo", "veh_ufo", "veh_ufo", "veh_flat:2A2D36"};
      VehGeo.lathe(
         var0,
         var3,
         new float[][]{
            {0.01F, -3.0F},
            {4.0F, -2.9F},
            {10.0F, -2.2F},
            {15.0F, -1.1F},
            {19.3F, -0.3F},
            {20.0F, 0.25F},
            {19.4F, 0.9F},
            {15.0F, 2.2F},
            {11.6F, 3.1F},
            {10.9F, 3.4F},
            {0.01F, 3.4F}
         },
         30
      );
      var0.glow(true);
      byte var4 = 14;

      for (int var5 = 0; var5 < var4; var5++) {
         double var6 = (Math.PI * 2) * var5 / var4 + var1.time * 1.5;
         float var8 = 0.5F + 0.5F * VehGeo.sin(var1.time * 6.0F + var5);
         var0.color(VehGeo.argb((int)(150.0F + 105.0F * var8), 16777215));
         VehGeo.ellipsoid(
            var0,
            var5 % 2 == 0 ? "veh_glow:7CFF6B" : "veh_glow:FFE14D",
            (float)Math.cos(var6) * 19.1F,
            0.4F,
            (float)Math.sin(var6) * 19.1F,
            0.75F,
            0.55F,
            0.75F,
            8,
            5
         );
      }

      var0.color(-1);
      var0.push();
      var0.translate(0.0F, -2.0F, 0.0F);
      VehGeo.torus(var0, "veh_glow:7CFF6B", 10.3F, 0.32F, 28, 5);
      var0.pop();
      var0.color(-1711276033);
      VehGeo.lathe(var0, "veh_glow:7CFF6B", new float[][]{{0.01F, -3.1F}, {3.6F, -3.1F}}, 20);
      var0.glow(false).color(-1);

      for (int var9 = 0; var9 < 3; var9++) {
         double var12 = (Math.PI * 2) * var9 / 3.0 + 0.5;
         VehGeo.ellipsoid(var0, "veh_chrome", (float)Math.cos(var12) * 13.5F, -1.5F, (float)Math.sin(var12) * 13.5F, 1.3F, 0.7F, 1.3F, 10, 5);
      }

      var0.push();
      var0.translate(0.0F, 3.4F, 5.6F);
      VehGeo.rbox(var0, "veh_gun", -2.6F, -0.1F, -0.8F, 2.6F, 9.0F, 0.8F, 0.4F);
      var0.translate(0.0F, 9.0F, 0.0F);
      var0.rotX(-30.0F);
      VehGeo.rbox(var0, "veh_gun", -3.2F, -0.4F, -1.4F, 3.2F, 0.3F, 1.4F, 0.25F);
      var0.glow(true);

      for (int var10 = 0; var10 < 5; var10++) {
         float var13 = Math.floorMod((int)(var1.time * 4.0F) + var10 * 3, 5) < 3 ? 1.0F : 0.35F;
         var0.color(VehGeo.argb((int)(255.0F * var13), 16777215));
         VehGeo.box(var0, var10 % 2 == 0 ? "veh_glow:FF4B6E" : "veh_glow:7CF7FF", -2.4F + var10 * 1.0F, 0.25F, -0.4F, -1.8F + var10 * 1.0F, 0.45F, 0.3F);
      }

      var0.glow(false).color(-1);
      var0.pop();
      var0.push();
      var0.translate(0.0F, 3.5F, 0.0F);
      VehGeo.torus(var0, "veh_chrome", 10.6F, 0.45F, 28, 6);
      var0.pop();
      float var11 = 3.4F;
      VehGeo.lathe(
         var0,
         "veh_glass",
         new float[][]{
            {10.4F, var11 - 0.2F},
            {11.0F, var11 + 2.0F},
            {10.7F, var11 + 4.5F},
            {9.9F, var11 + 6.8F},
            {9.6F, var11 + 7.0F},
            {9.4F, var11 + 6.6F},
            {10.1F, var11 + 4.4F},
            {10.3F, var11 + 2.0F},
            {9.8F, var11 - 0.15F}
         },
         30
      );
      var0.push();
      var0.translate(0.0F, var11 + 6.95F, 0.0F);
      VehGeo.torus(var0, "veh_chrome", 9.75F, 0.28F, 30, 5);
      var0.pop();
      VehGeo.tube(var0, "veh_chrome", VehGeo.p(0.0F, var11 + 6.9F, -9.75F), VehGeo.p(0.0F, var11 + 13.5F, -11.0F), 0.16F, 0.1F, 6, false);
      var0.glow(true).color(VehGeo.argb((int)(160.0F + 95.0F * VehGeo.sin(var1.time * 8.0F)), 16777215));
      VehGeo.ball(var0, "veh_glow:FF4B6E", 0.0F, var11 + 13.7F, -11.05F, 0.5F, 8, 5);
      var0.glow(false).color(-1);
      var0.pop();
   }

   static void cloud(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 3.5F + Vehicles.cloudBob(var1.time), 0.0F);
      PetGeo.blob2(var0, "veh_cloud", 0.0F, 0.0F, 0.0F, 8.5F, 2.6F, 1.4F, 10.5F, 16, 8);
      Random var2 = new Random(7L);
      byte var3 = 9;

      for (int var4 = 0; var4 < var3; var4++) {
         double var5 = (Math.PI * 2) * var4 / var3 + var2.nextFloat() * 0.3;
         float var7 = 9.0F + var2.nextFloat() * 2.5F;
         float var8 = 3.8F + var2.nextFloat() * 1.8F;
         float var9 = VehGeo.sin(var1.time * 1.2 + var4) * 0.25F;
         PetGeo.blob2(
            var0, "veh_cloud", (float)Math.cos(var5) * var7 * 0.8F, 0.3F + var9, (float)Math.sin(var5) * var7, var8, var8 * 0.8F, var8 * 0.35F, var8, 12, 7
         );
      }

      for (int var10 = 0; var10 < 4; var10++) {
         double var12 = (Math.PI * 2) * var10 / 4.0 + 0.7;
         float var14 = VehGeo.sin(var1.time * 1.5 + var10 * 2) * 0.2F;
         PetGeo.blob2(var0, "veh_cloud", (float)Math.cos(var12) * 8.5F, 2.2F + var14, (float)Math.sin(var12) * 9.5F, 3.2F, 2.6F, 0.8F, 3.2F, 10, 6);
      }

      for (int var11 = 0; var11 < 3; var11++) {
         float var13 = -15.0F - var11 * 3.2F - var1.time * 2.0F % 3.2F * var1.move;
         float var6 = 2.2F - var11 * 0.55F;
         PetGeo.blob2(var0, "veh_cloud", VehGeo.sin(var1.time + var11) * 1.5F, 0.6F, var13, var6, var6 * 0.8F, var6 * 0.5F, var6, 10, 6);
      }

      var0.pop();
   }
}
