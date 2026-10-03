package dev.lego.cosmetic;

import java.util.Arrays;

final class Cos3Back {
   private Cos3Back() {
   }

   static void register() {
      Cos.add("back_c3_minime", "MiniMe-Anhänger", Cos.Slot.BACK, Cos.Rarity.LEGENDARY, (var0, var1) -> miniMe(var0, var1, false));
      Cos.add("back_c3_minime_chain", "MiniMe-Kette", Cos.Slot.BACK, Cos.Rarity.LEGENDARY, (var0, var1) -> miniMe(var0, var1, true));
      Cos.add("back_c3_foxtail", "Fuchsschwanz", Cos.Slot.BACK, Cos.Rarity.EPIC, Cos3Back::foxTail);
      Cos.add("back_c3_dragontail", "Drachenschwanz", Cos.Slot.BACK, Cos.Rarity.LEGENDARY, Cos3Back::dragonTail);
      Cos.add("back_c3_parrot", "Schulterpapagei", Cos.Slot.BACK, Cos.Rarity.EPIC, Cos3Back::parrot);
      Cos.add("back_c3_shoulderdragon", "Schulterdrache", Cos.Slot.BACK, Cos.Rarity.LEGENDARY, Cos3Back::shoulderDragon);
      Cos.add("back_c3_katana", "Katana", Cos.Slot.BACK, Cos.Rarity.EPIC, Cos3Back::katana);
      Cos.add("back_c3_jetpack2", "Jetpack V2", Cos.Slot.BACK, Cos.Rarity.LEGENDARY, Cos3Back::jetpack2);
      Cos.add("back_c3_lantern", "Gürtellaterne", Cos.Slot.BACK, Cos.Rarity.RARE, Cos3Back::lantern);
   }

   static float mv(Cos.A var0) {
      return var0.preview ? 0.0F : Cos3Geo.clamp(var0.move, 0.0F, 1.0F);
   }

   static void miniMe(G var0, Cos.A var1, boolean var2) {
      float var3 = var1.time;
      float var4 = mv(var1);
      float[] var5;
      if (!var2) {
         var0.color(-1);
         Cos3Geo.bbox(var0, "strap", 0.5F, -5.3F, -2.7F, 2.7F, -3.3F, -2.25F, 0.18F);
         var0.color(-11414);
         Cos3Geo.ball(var0, "c3_metal", 1.6F, -4.3F, -2.72F, 0.3F, 8);
         Geo.tube(var0, "c3_metal", new float[]{1.6F, -4.3F, -2.7F}, new float[]{1.6F, -4.7F, -3.7F}, 0.14F, 0.14F, 6);
         Cos3Geo.ball(var0, "c3_metal", 1.6F, -4.7F, -3.7F, 0.18F, 6);
         var5 = new float[]{1.6F, -4.75F, -3.7F};
      } else {
         float[] var6 = new float[]{-3.1F, -1.3F, -2.85F};
         float[] var7 = new float[]{3.1F, -1.3F, -2.85F};

         for (float[] var11 : new float[][]{var6, var7}) {
            var0.color(-14013900);
            Cos3Geo.bbox(var0, "c3_metal", var11[0] - 0.55F, var11[1] - 0.55F, -2.95F, var11[0] + 0.55F, var11[1] + 0.55F, -2.25F, 0.15F);
            var0.glow(true).color(-9770753);
            var0.push();
            var0.translate(var11[0], var11[1], -3.0F);
            var0.rotX(90.0F);
            Cos3Geo.gem(var0, "c3_crystal", 0.38F, 0.3F, 0.1F, 6);
            var0.pop();
            var0.glow(false);
         }

         var0.color(-11414);
         float var14 = 2.2F + Cos3Geo.sin(var3 * 1.3F) * 0.08F;
         Cos3Geo.chainPath(var0, "c3_metal", Cos3Geo.sag(var6, var7, var14, 8), 0.55F);
         float[] var16 = new float[]{0.0F, -1.3F - var14, -2.85F};
         Geo.tube(var0, "c3_metal", var16, new float[]{0.0F, var16[1] - 0.5F, -3.7F}, 0.13F, 0.13F, 6);
         var0.glow(true).color(-9770753);
         var0.push();
         var0.translate(0.0F, var16[1] - 0.5F, -3.7F);
         Cos3Geo.gem(var0, "c3_crystal", 0.35F, 0.35F, 0.35F, 6);
         var0.pop();
         var0.glow(false).color(-11414);
         var5 = new float[]{0.0F, var16[1] - 0.85F, -3.7F};
      }

      float var12 = 5.0F + 5.0F * Cos3Geo.sin(var3 * 2.2F) + var4 * 28.0F;
      float var13 = Cos3Geo.sin(var3 * 1.7F) * 6.0F;
      var0.push();
      var0.translate(var5[0], var5[1], var5[2]);
      var0.rotX(var12);
      var0.rotZ(var13);
      var0.color(-11414);
      float var15 = var2 ? 1.6F : 1.2F;
      Cos3Geo.chainLinks(var0, "c3_metal", new float[]{0.0F, 0.0F, 0.0F}, new float[]{0.0F, -var15, 0.0F}, 0.45F);
      var0.push();
      var0.translate(0.0F, -var15 - 0.15F, 0.0F);
      var0.rotX(90.0F);
      var0.torus("c3_metal", 0.28F, 0.08F, 10, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.translate(0.0F, -var15 - 0.35F - 2.13F, 0.0F);
      var0.scale(0.25F);
      var0.rotY(180.0F);
      float var17 = Cos3Geo.sin(var3 * 2.6F);
      Cos3Geo.miniPlayer(
         var0,
         "@skin",
         Cos3Geo.sin(var3 * 0.9F) * 18.0F,
         Cos3Geo.sin(var3 * 1.3F) * 6.0F,
         14.0F * var17 + var4 * 20.0F,
         -14.0F * var17 - var4 * 20.0F,
         8.0F,
         8.0F,
         12.0F * var17
      );
      var0.pop();
      var0.color(-1);
   }

   static float[][] tailPath(float[] var0, int var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9) {
      float[][] var10 = new float[var1][];
      var10[0] = (float[])var0.clone();

      for (int var11 = 1; var11 < var1; var11++) {
         float var12 = (float)var11 / (var1 - 1);
         double var13 = Math.toRadians(var3 + (var4 - var3) * Math.pow(var12, var5) + var8 * Cos3Geo.sin(var9 * 1.8F - var12 * 4.0F) * var12);
         double var15 = Math.toRadians(var6 * var12 * Cos3Geo.sin(var7 - var12 * 2.2F));
         float var17 = (float)(Math.sin(var15) * Math.cos(var13));
         float var18 = (float)Math.sin(var13);
         float var19 = (float)(-Math.cos(var15) * Math.cos(var13));
         float[] var20 = var10[var11 - 1];
         var10[var11] = new float[]{var20[0] + var17 * var2, var20[1] + var18 * var2, var20[2] + var19 * var2};
      }

      return var10;
   }

   static void foxTail(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = mv(var1);
      float var4 = 16.0F * (1.0F - 0.5F * var3) + 10.0F * var3;
      float var5 = var2 * (2.2F + var3 * 5.0F);
      byte var6 = 13;
      float[][] var7 = tailPath(new float[]{0.0F, -10.2F, -3.3F}, var6, 1.1F, -45.0F, 100.0F, 1.6F, var4 * 2.0F, var5, 4.0F, var2);
      float[] var8 = new float[var6];

      for (int var9 = 0; var9 < var6; var9++) {
         float var10 = (float)var9 / (var6 - 1);
         var8[var9] = (0.75F + 1.75F * (float)Math.pow(Math.sin(Math.PI * Math.pow(var10, 0.8) * 0.92), 0.8)) * (1.0F - (float)Math.pow(var10, 5.0) * 0.88F);
      }

      byte var18 = 9;
      float[][] var19 = Arrays.copyOfRange(var7, 0, var18 + 1);
      float[][] var11 = Arrays.copyOfRange(var7, var18, var6);
      float[] var12 = Arrays.copyOfRange(var8, 0, var18 + 1);
      float[] var13 = Arrays.copyOfRange(var8, var18, var6);
      var0.color(-1542626);
      Geo.chain(var0, "c3_fur", var19, var12, 12);
      Cos3Geo.ball(var0, "c3_fur", var7[0][0], var7[0][1], var7[0][2], var8[0], 10);
      var0.color(-264466);
      Geo.chain(var0, "c3_fur", var11, var13, 12);

      for (byte var14 = 2; var14 < var6 - 1; var14 += 2) {
         float var15 = (float)var14 / (var6 - 1);
         var0.color(var15 > 0.7F ? -264466 : -1014230);
         float[] var16 = var7[var14];
         float var17 = var14 * 1.9F;
         Cos3Geo.ball(
            var0,
            "c3_fur",
            var16[0] + Cos3Geo.cos(var17) * var8[var14] * 0.55F,
            var16[1] + Cos3Geo.sin(var17) * var8[var14] * 0.4F,
            var16[2],
            var8[var14] * 0.62F,
            8
         );
      }

      var0.color(-1);
   }

   static void dragonTail(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = mv(var1);
      byte var4 = 17;
      float[][] var5 = tailPath(
         new float[]{0.0F, -10.0F, -3.8F}, var4, 1.05F, -55.0F, -15.0F, 1.0F, 30.0F * (1.0F + var3), var2 * (1.5F + var3 * 3.0F), 10.0F, var2
      );
      float[] var6 = new float[var4];

      for (int var7 = 0; var7 < var4; var7++) {
         float var8 = (float)var7 / (var4 - 1);
         var6[var7] = 1.25F * (float)Math.pow(1.0F - var8, 0.9) + 0.12F;
      }

      var0.color(-11915654);
      Geo.chain(var0, "c3_scale", var5, var6, 10);
      Cos3Geo.ball(var0, "c3_scale", var5[0][0], var5[0][1], var5[0][2], var6[0], 10);
      var0.color(-1521526);

      for (int var18 = 1; var18 < var4 - 2; var18++) {
         float[] var21 = var5[var18];
         float[] var9 = var5[var18 + 1];
         float var10 = var9[0] - var21[0];
         float var11 = var9[1] - var21[1];
         float var12 = var9[2] - var21[2];
         float var13 = 0.0F;
         float var14 = -var12;
         float var16 = (float)Math.sqrt(var14 * var14 + var11 * var11);
         var14 /= var16;
         float var15 = var11 / var16;
         var0.push();
         var0.translate(
            (var21[0] + var9[0]) / 2.0F + var13,
            (var21[1] + var9[1]) / 2.0F - var14 * var6[var18] * 0.72F,
            (var21[2] + var9[2]) / 2.0F - var15 * var6[var18] * 0.72F
         );
         Cos3Geo.alignY(var0, var10, var11, var12);
         float var17 = var6[var18] * 1.1F;
         Cos3Geo.bbox(var0, "c3_metal", -var17 / 2.0F, -0.45F, -var6[var18] * 0.42F, var17 / 2.0F, 0.45F, var6[var18] * 0.42F, 0.12F);
         var0.pop();
      }

      for (byte var19 = 1; var19 < var4 - 1; var19 += 2) {
         float var22 = (float)var19 / (var4 - 1);
         float[] var24 = var5[var19];
         float[] var25 = var5[var19 + 1];
         float var26 = var25[0] - var24[0];
         float var27 = var25[1] - var24[1];
         float var28 = var25[2] - var24[2];
         float var30 = -var28;
         float var33 = (float)Math.sqrt(var30 * var30 + var27 * var27);
         var30 /= var33;
         float var32 = var27 / var33;
         var0.push();
         var0.translate(var24[0], var24[1] + var30 * var6[var19] * 0.8F, var24[2] + var32 * var6[var19] * 0.8F);
         Cos3Geo.alignY(var0, 0.0F, var30 - var27 * 0.5F, var32 - var28 * 0.5F);
         float var34 = 0.7F + 0.3F * Cos3Geo.sin(var2 * 3.0F - var19 * 0.5F);
         var0.glow(true).color(Cos3Geo.alpha(var34, 12610303));
         Cos3Geo.shard(var0, "c3_crystal", 0.22F + 0.28F * (1.0F - var22), 0.6F + 0.9F * (1.0F - var22));
         var0.glow(false);
         var0.pop();
      }

      float[] var20 = var5[var4 - 1];
      float[] var23 = var5[var4 - 2];
      var0.push();
      var0.translate(var20[0], var20[1], var20[2]);
      Cos3Geo.alignY(var0, var20[0] - var23[0], var20[1] - var23[1], var20[2] - var23[2]);
      var0.translate(0.0F, -0.3F, 0.0F);
      var0.glow(true).color(-4166913);
      Cos3Geo.extrude(
         var0, "c3_crystal", new float[][]{{0.0F, -0.2F}, {1.0F, 0.55F}, {0.35F, 0.9F}, {0.0F, 2.2F}, {-0.35F, 0.9F}, {-1.0F, 0.55F}}, -0.14F, 0.14F
      );
      var0.glow(false);
      var0.pop();
      var0.color(-1);
   }

   static void parrot(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(6.2F, 1.0F, 0.2F);
      var0.rotY(-12.0F);
      float var3 = Math.max(0.0F, Cos3Geo.sin(var2 * 0.8F)) > 0.97F ? (Cos3Geo.sin(var2 * 0.8F) - 0.97F) * 12.0F : 0.0F;
      var0.translate(0.0F, var3, 0.0F);
      var0.color(-11908526);

      for (byte var4 = -1; var4 <= 1; var4 += 2) {
         Cos3Geo.bbox(var0, "white", var4 * 0.45F - 0.17F, 0.0F, -0.35F, var4 * 0.45F + 0.17F, 0.28F, 0.75F, 0.1F);
         Geo.tube(var0, "white", new float[]{var4 * 0.45F, 0.25F, 0.15F}, new float[]{var4 * 0.4F, 1.0F, 0.0F}, 0.15F, 0.13F, 5);
      }

      var0.push();
      var0.translate(0.0F, 1.4F, -1.1F);
      var0.rotY(90.0F);

      for (byte var7 = -1; var7 <= 1; var7 += 2) {
         var0.push();
         var0.translate(0.0F, 0.0F, var7 * 0.12F);
         var0.rotZ(-42 + var7 * 4 + Cos3Geo.sin(var2 * 1.3F) * 3.0F);
         var0.color(-1);
         Cos3Geo.extrude(
            var0, "c3_parrotwing", new float[][]{{0.0F, 0.25F}, {2.4F, 0.35F}, {4.4F, 0.1F}, {4.6F, -0.1F}, {2.4F, -0.35F}, {0.0F, -0.3F}}, -0.06F, 0.06F
         );
         var0.pop();
      }

      var0.pop();
      var0.color(-15026022);
      Geo.ellipsoid(var0, "c3_plume", 0.0F, 2.0F, -0.1F, 1.05F, 1.55F, 1.15F, 14, 8);
      var0.color(-11718);
      Geo.ellipsoid(var0, "c3_plume", 0.0F, 1.75F, 0.35F, 0.8F, 1.1F, 0.9F, 12, 7);
      float var8 = (float)Math.pow(Math.max(0.0F, Cos3Geo.sin(var2 * 1.1F)), 12.0) * 38.0F * (0.6F + 0.4F * Cos3Geo.sin(var2 * 18.0F));

      for (byte var5 = -1; var5 <= 1; var5 += 2) {
         var0.push();
         var0.translate(var5 * 0.95F, 2.9F, 0.2F);
         var0.rotZ(var5 * (6.0F + (var5 < 0 ? var8 * 0.3F : var8)));
         var0.rotY(90.0F);
         var0.rotZ(-12.0F);
         var0.color(-1);
         Cos3Geo.extrude(
            var0,
            "c3_parrotwing",
            new float[][]{{-0.6F, 0.3F}, {0.4F, 0.6F}, {1.6F, 0.2F}, {2.9F, -1.3F}, {2.5F, -1.7F}, {1.0F, -1.5F}, {-0.4F, -0.7F}},
            -0.12F,
            0.12F
         );
         var0.pop();
      }

      var0.push();
      var0.translate(0.0F, 3.75F, 0.35F);
      var0.rotY(Cos3Geo.sin(var2 * 0.9F) * 28.0F);
      var0.rotZ(Cos3Geo.sin(var2 * 0.6F + 1.0F) * 10.0F);
      var0.color(-14497624);
      var0.sphere("c3_plume", 0.95F, 14, 8, 0.0F, 0.0F, 1.0F, 1.0F);

      for (byte var9 = -1; var9 <= 1; var9 += 2) {
         var0.color(-461586);
         Geo.ellipsoid(var0, "white", var9 * 0.52F, 0.02F, 0.48F, 0.42F, 0.48F, 0.4F, 10, 6);
         boolean var6 = Cos3Geo.fract(var2 * 0.3F) > 0.96F;
         var0.color(-15461352);
         Geo.ellipsoid(var0, "white", var9 * 0.78F, 0.22F, 0.5F, 0.2F, var6 ? 0.04F : 0.22F, 0.2F, 8, 5);
         if (!var6) {
            var0.color(-1);
            Cos3Geo.ball(var0, "white", var9 * 0.9F, 0.3F, 0.58F, 0.07F, 4);
         }
      }

      var0.color(-20422);
      Cos3Geo.limb(var0, "c3_metal", new float[][]{{0.0F, 0.05F, 0.8F}, {0.0F, -0.1F, 1.25F}, {0.0F, -0.45F, 1.38F}}, new float[]{0.36F, 0.24F, 0.07F}, 8);
      var0.color(-3637222);
      Cos3Geo.limb(var0, "c3_metal", new float[][]{{0.0F, -0.35F, 0.75F}, {0.0F, -0.5F, 1.02F}}, new float[]{0.26F, 0.1F}, 7);
      float var10 = 10.0F + 12.0F * (float)Math.pow(Math.max(0.0F, Cos3Geo.sin(var2 * 0.7F)), 6.0);

      for (int var11 = 0; var11 < 3; var11++) {
         var0.push();
         var0.translate(0.0F, 0.8F, -0.05F - var11 * 0.2F);
         var0.rotY(90.0F);
         var0.rotZ(95 + var11 * 22 - var10);
         var0.color(var11 == 1 ? -34262 : -46534);
         Cos3Geo.extrude(var0, "c3_plume", new float[][]{{0.0F, 0.18F}, {0.9F, 0.22F}, {1.5F, 0.0F}, {0.9F, -0.2F}, {0.0F, -0.15F}}, -0.07F, 0.07F);
         var0.pop();
      }

      var0.pop();
      var0.pop();
      var0.color(-1);
   }

   static void shoulderDragon(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(-6.1F, 0.95F, 0.0F);
      var0.rotY(10.0F);
      int var3 = -9815384;
      int var4 = -862048;
      var0.color(var3);

      for (byte var5 = -1; var5 <= 1; var5 += 2) {
         for (int var6 = 0; var6 < 2; var6++) {
            float var7 = var6 == 0 ? 0.8F : -1.0F;
            Cos3Geo.limb(
               var0,
               "c3_scale",
               new float[][]{{var5 * 0.55F, 1.2F, var7}, {var5 * 0.7F, 0.5F, var7 + 0.1F}, {var5 * 0.62F, 0.15F, var7 + 0.3F}},
               new float[]{0.3F, 0.2F, 0.17F},
               6
            );
         }
      }

      Geo.ellipsoid(var0, "c3_scale", 0.0F, 1.5F, -0.2F, 0.95F, 1.0F, 1.55F, 14, 8);
      var0.color(var4);
      Geo.ellipsoid(var0, "c3_metal", 0.0F, 1.22F, 0.05F, 0.72F, 0.72F, 1.25F, 12, 6);
      var0.color(var3);
      float var11 = Cos3Geo.sin(var2 * 1.4F) * 0.4F;
      Cos3Geo.limb(
         var0,
         "c3_scale",
         new float[][]{
            {0.0F, 1.4F, -1.5F}, {0.1F + var11 * 0.3F, 1.0F, -2.4F}, {0.4F + var11, 0.3F, -2.9F}, {0.9F + var11, -0.5F, -2.85F}, {1.2F + var11, -1.0F, -2.5F}
         },
         new float[]{0.55F, 0.42F, 0.3F, 0.18F, 0.08F},
         7
      );
      float var12 = Cos3Geo.sin(var2 * 1.6F) * 0.12F;
      Cos3Geo.limb(var0, "c3_scale", new float[][]{{0.0F, 1.9F, 0.9F}, {0.0F, 2.7F, 1.35F}, {0.0F, 3.3F + var12, 1.65F}}, new float[]{0.58F, 0.46F, 0.42F}, 8);

      for (int var13 = 0; var13 < 4; var13++) {
         var0.push();
         var0.translate(0.0F, 2.35F - var13 * 0.12F, 0.5F - var13 * 0.6F);
         var0.rotX(-20.0F);
         var0.glow(true).color(-20422);
         Cos3Geo.shard(var0, "c3_crystal", 0.2F, 0.55F);
         var0.glow(false);
         var0.pop();
      }

      float var14 = Cos3Geo.sin(var2 * 2.2F) * 12.0F + (float)Math.pow(Math.max(0.0F, Cos3Geo.sin(var2 * 0.7F)), 10.0) * 35.0F;

      for (byte var8 = -1; var8 <= 1; var8 += 2) {
         var0.push();
         var0.translate(var8 * 0.65F, 2.2F, -0.1F);
         var0.scale(var8, 1.0F, 1.0F);
         if (var8 > 0) {
            var0.rotZ(14.0F + var14 * 0.3F);
            var0.rotY(62.0F);
         } else {
            var0.rotZ(38.0F + var14);
            var0.rotY(22.0F);
         }

         var0.rotX(-90.0F);
         var0.color(-660972824);
         float[][] var9 = new float[][]{
            {0.0F, 0.0F}, {1.5F, -0.25F}, {3.3F, 0.3F}, {2.6F, 1.05F}, {2.3F, 2.0F}, {1.65F, 1.5F}, {1.0F, 2.2F}, {0.6F, 1.4F}, {0.0F, 1.1F}
         };
         Cos3Geo.extrude(var0, "c3_membrane", var9, -0.05F, 0.05F);
         var0.color(var3);
         Geo.chain(var0, "c3_scale", new float[][]{{0.0F, 0.0F, 0.0F}, {1.5F, -0.25F, 0.0F}, {3.3F, 0.3F, 0.0F}}, new float[]{0.2F, 0.15F, 0.07F}, 5);
         Geo.tube(var0, "c3_scale", new float[]{1.5F, -0.25F, 0.0F}, new float[]{2.3F, 2.0F, 0.0F}, 0.1F, 0.05F, 4);
         Geo.tube(var0, "c3_scale", new float[]{1.5F, -0.25F, 0.0F}, new float[]{1.0F, 2.2F, 0.0F}, 0.1F, 0.05F, 4);
         var0.pop();
      }

      var0.push();
      var0.translate(0.0F, 3.55F + var12, 1.85F);
      var0.rotY(Cos3Geo.sin(var2 * 0.8F) * 25.0F);
      var0.rotX(-8.0F + Cos3Geo.sin(var2 * 1.1F) * 5.0F);
      var0.color(var3);
      Cos3Geo.bbox(var0, "c3_scale", -0.72F, -0.55F, -0.7F, 0.72F, 0.62F, 0.72F, 0.32F);
      Cos3Geo.bbox(var0, "c3_scale", -0.48F, -0.5F, 0.5F, 0.48F, 0.18F, 1.65F, 0.2F);
      var0.color(var4);
      Cos3Geo.bbox(var0, "c3_metal", -0.4F, -0.62F, 0.3F, 0.4F, -0.3F, 1.55F, 0.12F);

      for (byte var15 = -1; var15 <= 1; var15 += 2) {
         var0.glow(true).color(-8118);
         Geo.ellipsoid(var0, "white", var15 * 0.7F, 0.18F, 0.35F, 0.12F, 0.16F, 0.2F, 6, 4);
         var0.glow(false).color(-857904);
         var0.push();
         var0.translate(var15 * 0.38F, 0.5F, -0.35F);
         var0.rotX(-118.0F);
         var0.rotZ(var15 * 12);
         Cos3Geo.shard(var0, "c3_metal", 0.18F, 1.25F);
         var0.pop();
      }

      float var16 = Cos3Geo.fract(var2 / 5.0F);
      if (var16 < 0.3F) {
         for (int var17 = 0; var17 < 4; var17++) {
            float var10 = Cos3Geo.fract(var16 / 0.3F + var17 * 0.25F);
            Cos3Geo.halo(
               var0,
               Cos3Geo.sin(var17 * 2.1F) * 0.2F * var10,
               -0.2F + var10 * 0.3F,
               1.9F + var10 * 2.4F,
               0.6F + var10 * 1.2F,
               Cos3Geo.alpha((1.0F - var10) * 0.9, var10 < 0.3F ? 16769146 : 16738842)
            );
         }
      }

      var0.pop();
      var0.pop();
      var0.color(-1);
   }

   static float curve(float var0) {
      return 0.012F * (var0 + 1.5F) * (var0 + 1.5F) - 0.2F;
   }

   static void katana(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(0.0F, -5.6F, -2.42F);
      var0.rotZ(52.0F);
      var0.color(-1);
      Cos3Geo.bbox(var0, "strap", -0.6F, -5.3F, -0.16F, 0.6F, 5.3F, 0.16F, 0.1F);
      var0.pop();
      var0.push();
      var0.translate(1.0F, -6.0F, -3.35F);
      var0.rotZ(-38.0F);
      byte var3 = 14;
      float var4 = -7.5F;
      float var5 = 3.9F;
      float[][] var6 = new float[var3 * 2 + 2][];

      for (int var7 = 0; var7 <= var3; var7++) {
         float var8 = var4 + (var5 - var4) * var7 / var3;
         float var9 = 0.48F * (var7 == 0 ? 0.55F : 1.0F);
         var6[var7] = new float[]{curve(var8) + var9, var8};
         var6[2 * var3 + 1 - var7] = new float[]{curve(var8) - var9, var8};
      }

      var0.color(-9826792);
      Cos3Geo.extrude(var0, "c3_lacquer", var6, -0.3F, 0.3F, new float[]{0.0F, 0.0F, 1.0F, 1.0F}, new float[]{0.3F, 0.0F, 0.4F, 1.0F});
      var0.color(-14246);
      float[] var12 = new float[]{3.35F, -1.2F, -2.1F};

      for (float var11 : var12) {
         Cos3Geo.bbox(var0, "c3_metal", curve(var11) - 0.6F, var11 - 0.25F, -0.4F, curve(var11) + 0.6F, var11 + 0.25F, 0.4F, 0.12F);
      }

      Cos3Geo.bbox(var0, "c3_metal", curve(var4) - 0.55F, var4 - 0.2F, -0.38F, curve(var4) + 0.55F, var4 + 0.65F, 0.38F, 0.2F);
      var0.color(-12965344);
      Cos3Geo.bbox(var0, "strap", curve(-1.65F) - 0.35F, -1.95F, 0.2F, curve(-1.65F) + 0.35F, -1.35F, 0.75F, 0.1F);
      float var14 = 4.1F;
      var0.push();
      var0.translate(curve(var14), var14, 0.0F);
      var0.scale(1.0F, 1.0F, 0.55F);
      var0.color(-14013902);
      Cos3Geo.lathe(var0, "c3_metal", new float[]{0.0F, 1.35F, 1.35F, 0.0F}, new float[]{-0.18F, -0.18F, 0.18F, 0.18F}, 16);
      var0.color(-14246);
      var0.torus("c3_metal", 1.35F, 0.1F, 16, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.color(-14246);
      float var16 = curve(var14);
      Cos3Geo.bbox(var0, "c3_metal", var16 - 0.45F, 4.25F, -0.33F, var16 + 0.45F, 4.65F, 0.33F, 0.1F);
      var0.color(-1);
      Cos3Geo.bbox(var0, "c3_wrap", var16 - 0.38F, 4.6F, -0.28F, var16 + 0.38F, 8.5F, 0.28F, 0.14F);
      var0.color(-14246);
      Cos3Geo.bbox(var0, "c3_metal", var16 - 0.43F, 8.45F, -0.33F, var16 + 0.43F, 8.95F, 0.33F, 0.16F);
      var0.push();
      var0.translate(var16, 8.95F, 0.0F);
      var0.rotZ(38.0F);
      var0.rotX(Cos3Geo.sin(var2 * 1.8F) * 8.0F + mv(var1) * 25.0F);
      var0.color(-6673688);
      Geo.tube(var0, "white", new float[]{0.0F, 0.0F, 0.0F}, new float[]{0.0F, -1.4F, 0.0F}, 0.06F, 0.06F, 4);
      var0.glow(true).color(-9770753);
      Cos3Geo.ball(var0, "white", 0.0F, -1.55F, 0.0F, 0.28F, 8);
      Cos3Geo.halo(var0, 0.0F, -1.55F, 0.0F, 1.6F, -2140477185);
      var0.glow(false).color(-6673688);
      Cos3Geo.lathe(var0, "c3_fur", new float[]{0.08F, 0.22F, 0.3F, 0.18F}, new float[]{-1.75F, -2.0F, -2.9F, -3.2F}, 8);
      var0.pop();
      var0.pop();
      var0.color(-1);
   }

   static void jetpack2(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = mv(var1);
      var0.color(-1512206);
      Cos3Geo.bbox(var0, "c3_metal", -2.2F, -8.6F, -4.6F, 2.2F, -1.2F, -2.3F, 0.5F);
      var0.color(-14012872);
      Cos3Geo.bbox(var0, "c3_metal", -1.7F, -1.5F, -4.1F, 1.7F, -0.8F, -2.5F, 0.2F);
      float var4 = 0.75F + 0.25F * Cos3Geo.sin(var2 * 3.0F);
      var0.push();
      var0.translate(0.0F, -4.6F, -4.62F);
      var0.rotX(90.0F);
      var0.color(-14012872);
      var0.torus("c3_metal", 1.0F, 0.2F, 18, 5, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.glow(true).color(Cos3Geo.alpha(var4, 4905215));
      Geo.ellipsoid(var0, "c3_crystal", 0.0F, -4.6F, -4.6F, 0.9F, 0.9F, 0.25F, 14, 6);
      var0.color(-11872001);
      Cos3Geo.bbox(var0, "white", -0.18F, -8.0F, -4.72F, 0.18F, -5.8F, -4.45F, 0.05F);
      Cos3Geo.bbox(var0, "white", -0.18F, -3.4F, -4.72F, 0.18F, -1.8F, -4.45F, 0.05F);
      var0.glow(false);

      for (byte var5 = -1; var5 <= 1; var5 += 2) {
         float var6 = var5 * 2.75F;
         float var7 = -3.95F;
         var0.push();
         var0.translate(var6, 0.0F, var7);
         var0.color(-12960182);
         Cos3Geo.lathe(
            var0,
            "c3_metal",
            new float[]{0.0F, 1.0F, 1.42F, 1.5F, 1.5F, 1.42F, 1.0F, 0.0F},
            new float[]{-8.8F, -8.62F, -8.1F, -7.6F, -2.4F, -1.9F, -1.35F, -1.15F},
            18
         );
         var0.color(-30166);
         var0.push();
         var0.translate(0.0F, -3.0F, 0.0F);
         var0.torus("c3_metal", 1.5F, 0.14F, 18, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
         var0.push();
         var0.translate(0.0F, -7.0F, 0.0F);
         var0.torus("c3_metal", 1.5F, 0.14F, 18, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
         var0.color(-14539732);
         Cos3Geo.lathe(var0, "c3_metal", new float[]{0.95F, 0.72F, 0.86F, 1.08F, 0.95F}, new float[]{-8.7F, -9.1F, -9.8F, -10.45F, -10.5F}, 14);
         var0.glow(true).color(-38374);
         var0.push();
         var0.translate(0.0F, -10.35F, 0.0F);
         var0.torus("white", 0.85F, 0.1F, 14, 3, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
         float var8 = 1.0F + 0.18F * Cos3Geo.sin(var2 * 23.0F + var5 * 2) + 0.1F * Cos3Geo.sin(var2 * 37.0F + var5) + var3 * 0.5F;
         var0.push();
         var0.translate(0.0F, -10.4F, 0.0F);
         var0.rotX(180.0F);
         var0.scale(1.0F, var8, 1.0F);
         Cos3Models.flame(var0, 3.0F, 0.78F, -1338336513, -253165569);
         var0.pop();
         var0.glow(false);
         var0.color(-1512206);
         var0.push();
         var0.translate(var5 * 1.35F, -8.0F, 0.0F);
         var0.scale(var5, 1.0F, 1.0F);
         Cos3Geo.extrude(var0, "c3_metal", new float[][]{{0.0F, 3.2F}, {0.9F, 0.8F}, {0.9F, -1.2F}, {0.0F, -0.4F}}, -0.14F, 0.14F);
         var0.pop();
         var0.pop();

         for (int var9 = 0; var9 < 4; var9++) {
            float var10 = Cos3Geo.fract(var2 * 1.6F + var9 * 0.25F + (var5 > 0 ? 0.0F : 0.12F));
            Cos3Geo.halo(
               var0,
               var6 + Cos3Geo.sin(var9 * 2.3F + var2) * 0.3F * var10,
               -12.0F - var10 * 5.0F,
               var7 - var10 * 0.8F,
               0.8F + var10 * 1.6F,
               Cos3Geo.alpha((1.0F - var10) * 0.55, var10 < 0.25F ? 10149887 : 12107984)
            );
         }
      }

      for (int var11 = 0; var11 < 3; var11++) {
         boolean var12 = ((int)(var2 * 3.0F) + var11) % 3 != 0;
         var0.glow(true).color(var12 ? (var11 == 1 ? -46486 : -11862134) : -14670806);
         Cos3Geo.ball(var0, "white", -0.9F + var11 * 0.9F, -1.25F, -3.6F, 0.2F, 5);
      }

      var0.glow(false).color(-1);
   }

   static void lantern(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = mv(var1);
      var0.color(-1);
      Cos3Geo.bbox(var0, "strap", -3.3F, -10.4F, -2.62F, -1.5F, -8.9F, -2.25F, 0.15F);
      var0.color(-12961214);
      Geo.tube(var0, "c3_metal", new float[]{-2.4F, -9.6F, -2.6F}, new float[]{-2.4F, -9.9F, -3.7F}, 0.15F, 0.15F, 6);
      Cos3Geo.ball(var0, "c3_metal", -2.4F, -9.9F, -3.7F, 0.2F, 6);
      var0.push();
      var0.translate(-2.4F, -10.0F, -3.7F);
      var0.rotX(7.0F + 5.0F * Cos3Geo.sin(var2 * 2.0F) + var3 * 35.0F);
      var0.rotZ(Cos3Geo.sin(var2 * 1.6F) * 5.0F);
      var0.color(-12961214);
      var0.push();
      var0.translate(0.0F, -0.55F, 0.0F);
      var0.rotX(90.0F);
      var0.torus("c3_metal", 0.5F, 0.1F, 12, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.push();
      var0.translate(0.0F, -1.2F, 0.0F);
      var0.rotY(45.0F);
      Cos3Geo.lathe(var0, "c3_metal", new float[]{0.0F, 0.3F, 1.62F, 1.62F, 0.0F}, new float[]{0.0F, 0.0F, -0.75F, -0.95F, -0.95F}, 4);
      var0.pop();
      Cos3Geo.bbox(var0, "c3_metal", -1.1F, -2.35F, -1.1F, 1.1F, -2.05F, 1.1F, 0.08F);
      Cos3Geo.bbox(var0, "c3_metal", -1.15F, -5.1F, -1.15F, 1.15F, -4.75F, 1.15F, 0.1F);

      for (byte var4 = -1; var4 <= 1; var4 += 2) {
         for (byte var5 = -1; var5 <= 1; var5 += 2) {
            Cos3Geo.bbox(var0, "c3_metal", var4 * 0.95F - 0.14F, -4.8F, var5 * 0.95F - 0.14F, var4 * 0.95F + 0.14F, -2.3F, var5 * 0.95F + 0.14F, 0.05F);
         }
      }

      float var7 = 0.85F + 0.1F * Cos3Geo.sin(var2 * 13.0F) + 0.05F * Cos3Geo.sin(var2 * 29.0F);
      var0.push();
      var0.translate(0.0F, -4.75F, 0.0F);
      var0.scale(1.0F, var7, 1.0F);
      Cos3Models.flame(var0, 1.5F, 0.42F, -788559334, -117444448);
      var0.pop();
      var0.color(-1);
      var0.glow(true);
      var0.box("c3_lglass", -0.86F, -4.9F, -0.86F, 0.86F, -2.2F, 0.86F, 0.0F, 0.0F, 1.0F, 1.0F);
      Cos3Geo.halo(var0, 0.0F, -3.6F, 0.0F, 2.8F, Cos3Geo.alpha(0.45 * var7, 16756810));
      var0.glow(false);
      var0.pop();

      for (int var8 = 0; var8 < 3; var8++) {
         float var6 = var2 * (1.2F + var8 * 0.3F) + var8 * 2.1F;
         Cos3Models.sparkle(
            var0, -2.4F + Cos3Geo.cos(var6) * 2.4F, -13.4F + Cos3Geo.sin(var6 * 1.7F) * 0.8F, -3.9F + Cos3Geo.sin(var6) * 1.2F - 0.6F, 0.2F, -520099718
         );
      }

      var0.color(-1);
   }
}
