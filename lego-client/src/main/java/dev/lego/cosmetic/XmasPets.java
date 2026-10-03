package dev.lego.cosmetic;

import java.util.Arrays;

final class XmasPets {
   static final float SLEIGH_HIP = 8.0F;
   private static final float[][] SIDE = new float[][]{
      {-8.4F, 2.5F},
      {9.4F, 2.5F},
      {10.9F, 3.3F},
      {11.9F, 5.0F},
      {12.3F, 7.0F},
      {12.0F, 8.8F},
      {11.1F, 9.7F},
      {10.1F, 9.6F},
      {9.6F, 8.9F},
      {9.9F, 8.2F},
      {10.5F, 8.2F},
      {10.6F, 7.4F},
      {9.8F, 6.6F},
      {7.5F, 6.4F},
      {3.0F, 6.9F},
      {-1.6F, 7.5F},
      {-3.4F, 8.6F},
      {-4.8F, 11.2F},
      {-6.2F, 12.9F},
      {-7.8F, 13.4F},
      {-9.2F, 12.6F},
      {-9.4F, 11.4F},
      {-8.6F, 10.8F},
      {-8.0F, 11.5F},
      {-8.9F, 8.0F},
      {-8.9F, 4.0F}
   };

   private XmasPets() {
   }

   static void register() {
      Cos.add("xm_pet_snowman", "Schneemann", Cos.Slot.PET, Cos.Rarity.EPIC, Pets.grow(XmasPets::snowman, 1.5F, 3.8F));
      Cos.add("xm_pet_reindeer", "Rentier", Cos.Slot.PET, Cos.Rarity.LEGENDARY, XmasPets::reindeer);
      Cos.add("xm_pet_gingerbread", "Lebkuchenmann", Cos.Slot.PET, Cos.Rarity.EPIC, Pets.grow(XmasPets::gingerbread, 1.5F, 3.6F));
      Cos.add("xm_pet_giftmonster", "Geschenk-Monster", Cos.Slot.PET, Cos.Rarity.LEGENDARY, Pets.grow(XmasPets::giftMonster, 1.45F, 4.2F));
      Cos.add("xm_pet_penguin", "Pinguin mit Mütze", Cos.Slot.PET, Cos.Rarity.EPIC, Pets.grow(XmasPets::santaPenguin, 1.55F, 3.4F));
      Cos.add("xm_veh_sleigh", "Weihnachtsschlitten", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, XmasPets::sleigh);
   }

   private static float[] p3(float var0, float var1, float var2) {
      return new float[]{var0, var1, var2};
   }

   static void snowman(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      float[] var3 = PetRig.hop(var2);
      var2.y = var2.y + var3[0] * 1.8F;
      var2.stepBob = 0.0F;
      float var4 = -var3[1] * 0.12F + Cos3Geo.sin(var2.t * 2.2F) * 0.015F;
      var0.push();
      Pets.root(var0, var2);
      var0.color(-1);
      var0.push();
      var0.scale(1.0F + var4, 1.0F - var4, 1.0F + var4);
      Geo.ellipsoid(var0, "xm_snow", 0.0F, 2.25F, 0.0F, 2.45F, 2.3F, 2.4F, 16, 10);
      var0.pop();
      float var5 = var3[1] * 0.25F;
      var0.push();
      var0.translate(0.0F, 2.25F * (1.0F - var4) + 0.1F, 0.0F);
      var0.rotZ(var2.waddle * 0.6F + Cos3Geo.sin(var2.t * 1.5F) * 2.0F);
      Geo.ellipsoid(var0, "xm_snow", 0.0F, 2.75F - var5 * 0.4F, 0.0F, 1.85F, 1.8F, 1.8F, 14, 9);
      var0.color(-14013904);

      for (int var6 = 0; var6 < 3; var6++) {
         double var7 = 0.55 - var6 * 0.45;
         Cos3Geo.ball(var0, "xm_coal", 0.0F, 2.75F - var5 * 0.4F + (float)Math.sin(var7) * 1.8F, (float)Math.cos(var7) * 1.8F + 0.02F, 0.24F, 6);
      }

      for (byte var14 = -1; var14 <= 1; var14 += 2) {
         boolean var16 = var14 > 0 && (var2.is("happy") || var2.is("excited") || var2.is("love"));
         float var8 = var16 ? 55.0F + Cos3Geo.sin(var2.t * 7.0F) * 22.0F : 20.0F + Cos3Geo.sin(var2.t * 2.0F + var14) * 6.0F + var3[2] * 30.0F;
         if (var2.is("sad")) {
            var8 = -25.0F;
         }

         var0.push();
         var0.translate(var14 * 1.6F, 3.25F, 0.0F);
         var0.rotZ(var14 * var8);
         var0.scale(var14, 1.0F, 1.0F);
         var0.color(-9813470);
         Cos3Geo.limb(var0, "xm_bark", new float[][]{{0.0F, 0.0F, 0.0F}, {1.4F, 0.1F, 0.0F}, {2.9F, 0.35F, 0.1F}}, new float[]{0.2F, 0.17F, 0.12F}, 5);
         Cos3Geo.limb(var0, "xm_bark", new float[][]{{2.2F, 0.25F, 0.05F}, {2.8F, 0.95F, 0.1F}}, new float[]{0.1F, 0.07F}, 4);
         Cos3Geo.limb(var0, "xm_bark", new float[][]{{2.5F, 0.3F, 0.05F}, {3.1F, -0.2F, 0.2F}}, new float[]{0.09F, 0.06F}, 4);
         var0.pop();
      }

      var0.color(-1);
      var0.push();
      var0.translate(0.0F, 5.55F - var5, 0.0F);
      Pets.headTurn(var0, var2);
      float var15 = 1.5F;
      Geo.ellipsoid(var0, "xm_snow", 0.0F, 0.0F, 0.0F, var15, var15 * 0.97F, var15, 14, 9);
      float var17 = var2.closed ? 0.15F : 1.0F;
      var0.color(-14803420);

      for (byte var18 = -1; var18 <= 1; var18 += 2) {
         double var9 = var18 * 0.36 + var2.lookX * 0.12;
         double var11 = 0.28 + var2.lookY * 0.08;
         var0.push();
         var0.translate((float)(Math.sin(var9) * Math.cos(var11)) * var15, (float)Math.sin(var11) * var15, (float)(Math.cos(var9) * Math.cos(var11)) * var15);
         var0.scale(1.0F, var17, 1.0F);
         var0.sphere("xm_coal", 0.24F, 6, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      float var19 = var2.is("sad") ? -0.18F : (var2.is("angry") ? -0.06F : 0.2F);

      for (int var20 = 0; var20 < 5; var20++) {
         double var10 = (var20 - 2) * 0.24;
         double var12 = -0.28 - var19 * (1.0 - Math.pow((var20 - 2) / 2.0, 2.0)) + var19;
         Cos3Geo.ball(
            var0,
            "xm_coal",
            (float)(Math.sin(var10) * Math.cos(var12)) * var15 * 0.99F,
            (float)Math.sin(var12) * var15 * 0.99F,
            (float)(Math.cos(var10) * Math.cos(var12)) * var15 * 0.99F,
            0.13F,
            4
         );
      }

      var0.push();
      var0.translate(0.0F, 0.02F, var15 * 0.92F);
      var0.rotX(90.0F);
      var0.color(-1);
      Cos3Geo.lathe(var0, "xm_carrot", new float[]{0.3F, 0.28F, 0.2F, 0.1F, 0.0F}, new float[]{0.0F, 0.4F, 0.9F, 1.4F, 1.8F}, 8);
      var0.pop();
      var0.push();
      var0.translate(0.0F, var15 * 0.84F, -0.1F);
      var0.rotZ(-12.0F);
      var0.color(-14277074);
      Cos3Geo.lathe(
         var0, "xm_silk", new float[]{0.0F, 1.55F, 1.62F, 1.5F, 1.0F, 0.95F, 1.02F, 0.0F}, new float[]{0.0F, 0.0F, 0.12F, 0.22F, 0.25F, 1.8F, 1.9F, 1.9F}, 16
      );
      var0.color(-3139030);
      Cos3Models.flatRing(var0, "xm_ribbon", 0.9F, 1.03F, 0.35F, 0.75F, 16);
      var0.pop();
      var0.pop();
      var0.push();
      var0.translate(0.0F, 4.35F - var5 * 0.7F, 0.0F);
      var0.color(-1);
      var0.torus("xm_scarf", 1.2F, 0.42F, 16, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.translate(0.8F, -0.1F, 0.9F);
      Cos3Geo.ribbon(
         var0, "xm_scarf", 0.0F, new float[][]{{0.0F, 0.1F}, {-0.9F, 0.45F}, {-1.8F, 0.55F + var3[2] * 0.4F}}, new float[]{0.7F, 0.75F, 0.8F}, 0.18F
      );
      var0.pop();
      var0.pop();
      Pets.fx(var0, var2, 9.6F);
      var0.pop();
   }

   static void reindeer(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      PetReal.R4 var3 = new PetReal.R4();
      var3.body = Pets.fur("deer");
      var3.head = Pets.head("deer");
      var3.muzzle = Pets.col(12092504);
      var3.legUp = Pets.col(10118208);
      var3.legLo = Pets.col(11565648);
      var3.paw = Pets.gl(2760736);
      var3.style = "deer";
      var3.nose = 14688298;
      var3.grazer = true;
      var3.L = 6.2F;
      var3.legX = 1.05F;
      var3.zs = 3.0F;
      var3.zh = -3.1F;
      var3.rx = 1.8F;
      var3.ry = 2.15F;
      var3.hipF = 0.95F;
      var3.neckLen = 3.0F;
      var3.neckAng = 28.0F;
      var3.neckR = 0.98F;
      var3.skRx = 1.32F;
      var3.skRy = 1.3F;
      var3.skRz = 1.45F;
      var3.snLen = 1.8F;
      var3.snR = 0.7F;
      var3.snY = -0.32F;
      var3.headDown = 14.0F;
      var3.r1f = 0.58F;
      var3.r2f = 0.3F;
      var3.r1h = 0.82F;
      var3.r2h = 0.32F;
      var3.pawR = 0.4F;
      var3.hoof = true;
      var3.span = 1.05F;
      var3.eyeEl = 0.26F;
      var3.babyS = 0.56F;
      var3.babyLeg = 0.75F;
      var3.babyHead = 1.6F;
      float var4 = var2.gr;
      var3.ears = (var0x, var1x) -> {
         for (byte var2x = -1; var2x <= 1; var2x += 2) {
            Pets.earPointy(var0x, var1x, Pets.col(10118208), Pets.col(16181466), var2x * 1.0F, 0.8F, -0.45F, var2x, 0.7F, 1.6F, 62.0F);
         }
      };
      var3.headExtra = (var1x, var2x) -> {
         reindeerAntlers(var1x, var2x);
         float var3x = PetRig.lerp(var3.babySnout, 1.0F, var2x.gr);
         float var4x = var3.snLen * var3x;
         float var5 = var3.skRz * 0.42F + var4x * 0.5F;
         float var6 = var3.snR * PetRig.lerp(1.1F, 1.0F, var2x.gr);
         float var7 = var3.snR * 0.8F;
         float var8 = var4x * 0.55F + var3.snR * 0.3F;
         float var9 = 0.5F + 0.5F * Cos3Geo.sin(var2x.t * 3.4F);
         float var10 = var6 * 0.52F * (1.0F + 0.08F * var9);
         float var11 = var3.snY + var7 * 0.42F;
         float var12 = var5 + var8 * 0.9F;
         boolean var13 = var1x.glow;
         int var14 = var1x.color;
         var1x.glow(true).color(0xFF000000 | Capes.mix(13111324, 16730698, var9));
         Geo.ellipsoid(var1x, "xm_gloss", 0.0F, var11, var12, var10 * 1.15F, var10, var10, 10, 6);
         Cos3Geo.halo(var1x, 0.0F, var11, var12 + 0.2F, 2.4F + var9, Cos3Geo.alpha(0.35 + 0.3 * var9, 16726586));
         var1x.glow(var13).color(var14);
      };
      var3.neckExtra = (var1x, var2x) -> {
         float var3x = var3.neckR * PetRig.lerp(1.1F, 1.0F, var2x.gr);
         var1x.push();
         var1x.translate(0.0F, var3.neckLen * PetRig.lerp(var3.babyNeck, 1.0F, var2x.gr) * 0.2F, 0.0F);
         var1x.rotX(-10.0F);
         var1x.color(-3139030);
         var1x.torus("xm_velvet", var3x * 1.02F, 0.24F, 14, 5, 0.0F, 0.0F, 1.0F, 1.0F);
         var1x.translate(0.0F, -0.15F, var3x * 1.02F + 0.2F);
         XmasGeo.bell(var1x, 0.55F, Cos3Geo.sin(var2x.t * 5.0F) * 18.0F * (0.3F + var2x.walkW));
         var1x.color(-1);
         var1x.pop();
      };
      var3.bodyExtra = (var1x, var2x) -> {
         float var3x = PetRig.lerp(var3.babyLen, 1.0F, var2x.gr);
         float var4x = PetRig.lerp(1.18F, 1.0F, var2x.gr);
         float var5 = (var3.zs * var3x * 0.45F + var3.zh * var3x * 0.5F) / 2.0F;
         float var6 = var3.rx * var4x;
         float var7 = var3.ry * var4x;
         var1x.color(-3664866);
         Geo.ellipsoid(var1x, "xm_velvet", 0.0F, var7 * 0.38F, var5, var6 * 1.07F, var7 * 0.72F, 1.55F * var3x, 12, 6);
         var1x.color(-11414);
         Geo.ellipsoid(var1x, "xm_gold", 0.0F, var7 * 0.38F - 0.05F, var5, var6 * 1.1F, var7 * 0.5F, 1.62F * var3x, 12, 3);
         var1x.color(-1);
      };
      var3.tail = (var0x, var1x) -> {
         var0x.rotX(35.0F + var1x.tailLift * 0.5F + (!var1x.is("excited") && !var1x.is("angry") ? 0 : 40));
         PetGeo.blob(var0x, Pets.col(16777215), 0.0F, 0.0F, -0.45F, 0.45F, 0.38F, 0.62F, 6, 3);
      };
      PetReal.real4(var0, var2, var3);
      if (var4 > 1.0F) {
         var0.color(-1);
      }
   }

   static void reindeerAntlers(G var0, Pets.P var1) {
      float var2 = var1.gr;
      float var3 = 0.55F + 0.55F * var2;
      int var4 = var0.color;
      var0.color(-1);

      for (byte var5 = -1; var5 <= 1; var5 += 2) {
         var0.push();
         var0.translate(var5 * 0.62F, 1.0F, -0.15F);
         var0.scale(var5 * var3, var3, var3);
         var0.rotZ(-18.0F);
         float[][] var6 = new float[][]{
            {0.0F, 0.0F, 0.0F}, {0.3F, 1.1F, -0.3F}, {0.9F, 2.2F, -0.8F}, {1.4F, 3.2F, -0.9F}, {1.6F, 4.2F, -0.6F}, {1.4F, 5.0F, -0.2F}
         };
         Cos3Geo.limb(var0, "xm_antler", var6, new float[]{0.24F, 0.22F, 0.19F, 0.16F, 0.12F, 0.06F}, 6);
         Cos3Geo.limb(var0, "xm_antler", new float[][]{var6[1], {0.2F, 1.8F, 0.6F}, {0.1F, 2.2F, 1.0F}}, new float[]{0.15F, 0.11F, 0.05F}, 5);
         Cos3Geo.limb(var0, "xm_antler", new float[][]{var6[2], {0.6F, 3.3F, 0.1F}, {0.5F, 3.9F, 0.4F}}, new float[]{0.14F, 0.1F, 0.05F}, 5);
         Cos3Geo.limb(var0, "xm_antler", new float[][]{var6[3], {2.3F, 3.9F, -1.1F}, {2.6F, 4.4F, -1.1F}}, new float[]{0.13F, 0.09F, 0.04F}, 5);
         var0.pop();
      }

      var0.color(var4);
   }

   static void gingerbread(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      float var3 = var2.walkW;
      double var4 = (Math.PI * 2) * PetRig.frac(var2.t * 1.7F);
      float var6 = Cos3Geo.sin(var4) * 38.0F * var3;
      float var7 = Math.abs(Cos3Geo.sin(var4)) * 0.35F * var3 + (var2.is("excited") ? Math.abs(Cos3Geo.sin(var2.t * 6.8F)) * 0.8F : 0.0F);
      var0.push();
      Pets.root(var0, var2);
      var0.translate(0.0F, var7, 0.0F);
      var0.rotZ(Cos3Geo.sin(var4) * 4.0F * var3);
      var0.color(-1);
      String var8 = "xm_ginger";
      float var9 = 0.62F;

      for (byte var10 = -1; var10 <= 1; var10 += 2) {
         var0.push();
         var0.translate(var10 * 0.95F, 3.3F, 0.0F);
         var0.rotX(var10 * var6 - var2.sit * 80.0F);
         var0.scale(1.0F, 1.0F, var9);
         Cos3Geo.limb(
            var0, var8, new float[][]{{0.0F, 0.0F, 0.0F}, {var10 * 0.12F, -1.5F, 0.0F}, {var10 * 0.25F, -2.75F, 0.0F}}, new float[]{0.82F, 0.78F, 0.74F}, 8
         );
         icing(var0, var10 * 0.25F, -2.3F, 0.78F);
         var0.pop();
      }

      Geo.ellipsoid(var0, var8, 0.0F, 4.9F, 0.0F, 1.95F, 2.15F, 0.78F, 14, 8);
      int[] var18 = new int[]{-2088918, -13715382, -2088918};

      for (int var11 = 0; var11 < 3; var11++) {
         var0.color(var18[var11]);
         float var12 = 5.9F - var11 * 0.95F;
         float var13 = 0.78F * (float)Math.sqrt(Math.max(0.05, 1.0 - Math.pow((var12 - 4.9F) / 2.15F, 2.0)));
         Geo.ellipsoid(var0, "xm_gloss", 0.0F, var12, var13 + 0.05F, 0.34F, 0.3F, 0.2F, 8, 5);
      }

      var0.color(-1);

      for (byte var19 = -1; var19 <= 1; var19 += 2) {
         boolean var21 = var19 > 0 && (var2.is("happy") || var2.is("excited")) && var3 < 0.3F;
         float var24 = var21 ? 75.0F + Cos3Geo.sin(var2.t * 8.0F) * 25.0F : Cos3Geo.sin(var2.t * 2.0F + var19) * 5.0F - 5.0F;
         var0.push();
         var0.translate(var19 * 1.55F, 6.0F, 0.0F);
         var0.rotX(-var19 * var6 * 0.8F);
         var0.rotZ(var19 * var24);
         var0.scale(1.0F, 1.0F, var9);
         float[][] var14 = new float[][]{{0.0F, 0.0F, 0.0F}, {var19 * 1.2F, -0.55F, 0.0F}, {var19 * 2.3F, -0.95F, 0.0F}};
         Cos3Geo.limb(var0, var8, var14, new float[]{0.72F, 0.68F, 0.64F}, 8);
         icing2(var0, var19 * 1.95F, -0.83F, 0.66F);
         var0.pop();
      }

      var0.push();
      var0.translate(0.0F, 6.85F, 0.62F);
      XmasGeo.bow(var0, -2088918, 0.45F);
      var0.pop();
      var0.push();
      var0.translate(0.0F, 8.4F, 0.0F);
      Pets.headTurn(var0, var2);
      var0.color(-1);
      Geo.ellipsoid(var0, var8, 0.0F, 0.0F, 0.0F, 1.85F, 1.75F, 0.8F, 14, 8);
      float var20 = var2.closed ? 0.2F : 1.0F;
      var0.color(-1);

      for (byte var22 = -1; var22 <= 1; var22 += 2) {
         var0.push();
         var0.translate(var22 * 0.62F + var2.lookX * 0.12F, 0.35F + var2.lookY * 0.1F, 0.7F);
         var0.scale(1.0F, var20, 0.5F);
         var0.sphere("white", 0.27F, 8, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      float var23 = var2.is("sad") ? -0.3F : 0.35F;
      byte var25 = 7;
      float[][] var26 = new float[var25][];

      for (int var15 = 0; var15 < var25; var15++) {
         float var16 = (var15 - (var25 - 1) / 2.0F) / ((var25 - 1) / 2.0F);
         float var17 = -0.45F - var23 * (1.0F - var16 * var16);
         var26[var15] = p3(
            var16 * 0.8F, var17, 0.8F * (float)Math.sqrt(Math.max(0.05, 1.0 - Math.pow(var16 * 0.8 / 1.85, 2.0) - Math.pow(var17 / 1.75, 2.0))) + 0.04F
         );
      }

      float[] var27 = new float[var25];
      Arrays.fill(var27, 0.1F);
      Cos3Geo.limb(var0, "white", var26, var27, 4);
      var0.color(-1013110);

      for (byte var28 = -1; var28 <= 1; var28 += 2) {
         Geo.ellipsoid(var0, "white", var28 * 1.1F, -0.25F, 0.62F, 0.28F, 0.2F, 0.12F, 6, 3);
      }

      var0.pop();
      Pets.fx(var0, var2, 10.6F);
      var0.pop();
      var0.color(-1);
   }

   private static void icing(G var0, float var1, float var2, float var3) {
      byte var4 = 12;
      float[][] var5 = new float[var4 + 1][];

      for (int var6 = 0; var6 <= var4; var6++) {
         double var7 = (Math.PI * 2) * var6 / var4;
         var5[var6] = p3(var1 + (float)Math.cos(var7) * (var3 + 0.04F), var2 + (var6 % 2 == 0 ? 0.22F : -0.22F), (float)Math.sin(var7) * (var3 + 0.04F));
      }

      float[] var9 = new float[var4 + 1];
      Arrays.fill(var9, 0.09F);
      int var10 = var0.color;
      var0.color(-1);
      Geo.chain(var0, "white", var5, var9, 4);
      var0.color(var10);
   }

   private static void icing2(G var0, float var1, float var2, float var3) {
      byte var4 = 12;
      float[][] var5 = new float[var4 + 1][];

      for (int var6 = 0; var6 <= var4; var6++) {
         double var7 = (Math.PI * 2) * var6 / var4;
         var5[var6] = p3(var1 + (var6 % 2 == 0 ? 0.2F : -0.2F), var2 + (float)Math.cos(var7) * (var3 + 0.04F), (float)Math.sin(var7) * (var3 + 0.04F));
      }

      float[] var9 = new float[var4 + 1];
      Arrays.fill(var9, 0.09F);
      int var10 = var0.color;
      var0.color(-1);
      Geo.chain(var0, "white", var5, var9, 4);
      var0.color(var10);
   }

   static void giftMonster(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      float[] var3 = PetRig.hop(var2);
      var2.y = var2.y + var3[0] * 1.6F;
      var2.stepBob = 0.0F;
      float var4 = -var3[1] * 0.1F;
      float var5 = 5.0F;
      float var6 = 4.0F;
      float var7 = 4.6F;
      var0.push();
      Pets.root(var0, var2);
      var0.color(-1);

      for (byte var8 = -1; var8 <= 1; var8 += 2) {
         float var9 = PetRig.frac(var2.t * 2.1F + (var8 > 0 ? 0.0F : 0.5F));
         float var10 = var2.walkW * Math.max(0.0F, Cos3Geo.sin((Math.PI * 2) * var9)) * 0.5F;
         PetGeo.blob(var0, Pets.gl(3054152), var8 * 1.4F, 0.45F + var10, 0.4F, 0.75F, 0.5F, 1.0F, 8, 4);
      }

      var0.push();
      var0.translate(0.0F, 0.75F, 0.0F);
      var0.scale(1.0F + var4, 1.0F - var4, 1.0F + var4);
      var0.rotZ(var2.waddle * 0.5F);
      var0.color(-1);
      Cos3Geo.bbox(var0, "xm_gift:D8202A:FFD24A:1", -var5 / 2.0F, 0.0F, -var7 / 2.0F, var5 / 2.0F, var6, var7 / 2.0F, 0.22F);
      float var15 = 0.55F;
      var0.color(-11414);
      var0.box("xm_ribbon", -var5 / 2.0F - 0.08F, -0.08F, -var15, var5 / 2.0F + 0.08F, var6 + 0.08F, var15, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-12973552);
      var0.box("xm_felt", -var5 / 2.0F + 0.3F, var6 - 0.35F, -var7 / 2.0F + 0.3F, var5 / 2.0F - 0.3F, var6 + 0.14F, var7 / 2.0F - 0.3F, 0.0F, 0.0F, 1.0F, 1.0F);
      float var16 = 12.0F
         + 26.0F * var3[2]
         + (var2.is("excited") ? 18.0F * Math.abs(Cos3Geo.sin(var2.t * 7.0F)) : 0.0F)
         + (var2.is("angry") ? 14.0F * Math.abs(Cos3Geo.sin(var2.t * 12.0F)) : 0.0F)
         + (var2.is("sleepy") ? -12 : 0)
         + 6.0F * Cos3Geo.sin(var2.t * 1.7F);
      var16 = Math.max(0.0F, var16);
      float var18 = Math.min(1.0F, var16 / 25.0F);
      if (var18 > 0.05F) {
         var0.glow(true).color(Cos3Geo.alpha(0.9 * var18, 16769146));
         var0.box(
            "white", -var5 / 2.0F + 0.45F, var6 + 0.2F, -var7 / 2.0F + 0.45F, var5 / 2.0F - 0.45F, var6 + 0.25F, var7 / 2.0F - 0.45F, 0.0F, 0.0F, 1.0F, 1.0F
         );
         var0.glow(false);
         Cos3Geo.halo(var0, 0.0F, var6 + 0.8F, 0.0F, 5.5F * var18, Cos3Geo.alpha(0.45 * var18, 16767082));

         for (int var11 = 0; var11 < 4; var11++) {
            float var12 = Cos3Geo.fract(var2.t * 0.7F + var11 * 0.25F);
            Cos3Models.sparkle(
               var0,
               Cos3Geo.sin(var11 * 2.4F + var2.t) * 1.4F,
               var6 + 0.5F + var12 * 4.0F,
               Cos3Geo.cos(var11 * 1.9F) * 1.2F,
               0.28F,
               Cos3Geo.alpha((1.0F - var12) * var18, 16773296)
            );
         }
      }

      var0.color(-1);

      for (int var19 = 0; var19 < 5; var19++) {
         var0.push();
         var0.translate(-1.6F + var19 * 0.8F, var6 + 0.12F, var7 / 2.0F - 0.45F);
         PetGeo.cone(var0, "white", 0.26F, 0.55F, 5, false);
         var0.pop();
      }

      Pets.faceFlat(var0, var2, "std", 0.0F, var6 * 0.46F, var7 / 2.0F + 0.08F, var5 * 0.86F, var6 * 0.78F);
      var0.push();
      var0.translate(0.0F, var6 + 0.02F, -var7 / 2.0F - 0.15F);
      var0.rotX(-var16);
      var0.translate(0.0F, 0.0F, var7 / 2.0F + 0.15F);
      float var20 = var5 + 0.5F;
      float var21 = var7 + 0.5F;
      float var13 = 1.1F;
      Cos3Geo.bbox(var0, "xm_gift:D8202A:FFD24A:1", -var20 / 2.0F, 0.0F, -var21 / 2.0F, var20 / 2.0F, var13, var21 / 2.0F, 0.2F);
      var0.color(-11414);
      var0.box("xm_ribbon", -var15, -0.06F, -var21 / 2.0F - 0.06F, var15, var13 + 0.06F, var21 / 2.0F + 0.06F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.box("xm_ribbon", -var20 / 2.0F - 0.12F, -0.12F, -var15, var20 / 2.0F + 0.12F, var13 + 0.12F, var15, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1);

      for (int var14 = 0; var14 < 4; var14++) {
         var0.push();
         var0.translate(-1.2F + var14 * 0.8F, 0.02F, var21 / 2.0F - 0.55F);
         var0.rotX(180.0F);
         PetGeo.cone(var0, "white", 0.26F, 0.5F, 5, false);
         var0.pop();
      }

      var0.translate(0.0F, var13 + 0.12F, 0.0F);
      XmasGeo.bow(var0, -11414, 1.25F);
      var0.pop();
      var0.pop();
      Pets.fx(var0, var2, var6 + 3.5F);
      var0.pop();
      var0.color(-1);
   }

   static void santaPenguin(G var0, Cos.A var1) {
      PetModels.penguin(var0, var1);
      Pets.P var2 = Pets.pose(var1);
      float var3 = var2.gr;
      var0.push();
      Pets.root(var0, var2);
      var0.translate(0.0F, var2.waddleBob * 0.7F, 0.0F);
      float var4 = 3.55F + var3 * 1.1F - var2.lie * 0.6F;
      float var5 = 3.3F + var3 * 1.3F;
      var0.translate(0.0F, var4, 0.0F);
      var0.rotZ(var2.waddle * 1.2F);
      var0.rotY(var2.waddle * 0.5F);
      var0.rotY(var2.headYaw * 0.6F);
      var0.rotX(var2.headPitch * 0.4F);
      var0.rotZ(var2.headRoll * 0.8F);
      var0.translate(0.0F, var5 * 0.78F, -0.25F);
      var0.rotX(-14.0F);
      var0.rotZ(8.0F);
      float var6 = Cos3Geo.sin(var2.t * 2.1F) * 0.25F + var2.walkW * Cos3Geo.sin(var2.t * 9.0F) * 0.15F;
      byte var7 = 9;
      float[][] var8 = new float[var7][];
      float[] var9 = new float[var7];
      float[] var10 = new float[]{0.0F, 0.0F, 0.0F};

      for (int var11 = 0; var11 < var7; var11++) {
         float var12 = (float)var11 / (var7 - 1);
         double var13 = var12 * var12 * 1.9 + 0.1;
         if (var11 > 0) {
            float var15 = 0.55F;
            var10 = new float[]{
               var10[0] + (float)(Math.sin(var6 * var12) * Math.sin(var13)) * var15,
               var10[1] + (float)Math.cos(var13) * var15,
               var10[2] - (float)(Math.cos(var6 * var12) * Math.sin(var13)) * var15
            };
         }

         var8[var11] = var10;
         var9[var11] = 1.75F * (1.0F - var12) + 0.06F;
      }

      var0.color(-2613206);
      XmasGeo.sweep(var0, "xm_felt", var8, var9, var9, null, 14, 2.0F);
      var0.color(-1);
      var0.push();
      var0.translate(0.0F, 0.08F, 0.0F);
      var0.torus("xm_fluff", 1.72F, 0.42F, 18, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      float[] var16 = var8[var7 - 1];
      Cos3Geo.ball(var0, "xm_fluff", var16[0], var16[1] - 0.1F, var16[2], 0.5F, 8);
      var0.pop();
   }

   static void sleigh(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = Cos3Geo.clamp(var1.move, 0.0F, 1.0F);
      var0.push();
      var0.rotZ(-var1.lean * 4.0F);
      var0.translate(0.0F, var3 * 0.15F * Math.abs(Cos3Geo.sin(var2 * 7.0F)), 0.0F);
      float[][] var4 = new float[][]{
         {0.0F, 2.6F, -10.3F},
         {0.0F, 1.6F, -10.2F},
         {0.0F, 0.7F, -9.2F},
         {0.0F, 0.5F, -7.5F},
         {0.0F, 0.5F, 5.0F},
         {0.0F, 0.6F, 8.6F},
         {0.0F, 1.1F, 10.6F},
         {0.0F, 2.4F, 12.3F},
         {0.0F, 4.4F, 13.4F},
         {0.0F, 6.6F, 13.8F},
         {0.0F, 8.3F, 13.2F},
         {0.0F, 9.0F, 12.2F},
         {0.0F, 8.7F, 11.2F},
         {0.0F, 7.9F, 10.9F},
         {0.0F, 7.4F, 11.4F}
      };

      for (byte var5 = -1; var5 <= 1; var5 += 2) {
         float[][] var6 = new float[var4.length][];

         for (int var7 = 0; var7 < var4.length; var7++) {
            var6[var7] = new float[]{var5 * 4.7F, var4[var7][1], var4[var7][2]};
         }

         float[] var19 = new float[var6.length];
         Arrays.fill(var19, 0.42F);
         var19[var19.length - 1] = 0.3F;
         var0.color(-11414);
         Cos3Geo.limb(var0, "xm_gold", var6, var19, 6);

         for (float var11 : new float[]{-6.2F, -0.5F, 5.2F}) {
            Geo.tube(var0, "xm_gold", new float[]{var5 * 4.7F, 0.6F, var11}, new float[]{var5 * 4.7F, 2.6F, var11 + 0.4F}, 0.26F, 0.26F, 6);
            Cos3Geo.ball(var0, "xm_gold", var5 * 4.7F, 2.55F, var11 + 0.4F, 0.36F, 6);
         }
      }

      var0.color(-8779246);
      Cos3Geo.bbox(var0, "xm_red_lacquer", -4.95F, 2.5F, -8.6F, 4.95F, 3.3F, 9.8F, 0.2F);

      for (byte var15 = -1; var15 <= 1; var15 += 2) {
         var0.push();
         var0.rotY(-90.0F);
         var0.color(-1);
         float var17 = var15 > 0 ? 4.95F : -5.55F;
         float var20 = var17 + 0.6F;
         Cos3Geo.extrude(var0, "xm_sleighside", SIDE, -var20, -var17, Cos3Geo.FULL, new float[]{0.0F, 0.05F, 0.02F, 0.07F});
         var0.pop();
         int[] var22 = new int[]{13, 14, 15, 16, 17, 18, 19, 20};
         float[][] var24 = new float[var22.length][];

         for (int var27 = 0; var27 < var22.length; var27++) {
            var24[var27] = new float[]{var15 * 5.25F, SIDE[var22[var27]][1] + 0.05F, SIDE[var22[var27]][0]};
         }

         float[] var28 = new float[var24.length];
         Arrays.fill(var28, 0.2F);
         var0.color(-11414);
         Cos3Geo.limb(var0, "xm_gold", var24, var28, 5);
      }

      var0.color(-6287336);
      Cos3Geo.bbox(var0, "xm_velvet", -4.5F, 3.2F, -3.2F, 4.5F, 6.0F, 3.6F, 0.45F);
      Cos3Geo.bbox(var0, "xm_velvet", -4.4F, 5.5F, -4.0F, 4.4F, 14.2F, -2.45F, 0.5F);
      var0.color(-11414);
      Cos3Geo.bbox(var0, "xm_gold", -4.55F, 13.9F, -4.1F, 4.55F, 14.5F, -2.35F, 0.15F);
      var0.color(-6681066);
      Cos3Geo.bbox(var0, "xm_red_lacquer", -4.9F, 3.2F, 9.3F, 4.9F, 5.5F, 10.1F, 0.2F);
      var0.color(-11414);
      Cos3Geo.bbox(var0, "xm_gold", -4.95F, 5.4F, 9.2F, 4.95F, 5.75F, 10.2F, 0.1F);
      var0.color(-5207976);
      Geo.ellipsoid(var0, "xm_sack", 0.0F, 7.9F, -6.3F, 3.5F, 3.4F, 2.3F, 12, 8);
      Geo.ellipsoid(var0, "xm_sack", 1.4F, 9.6F, -6.6F, 2.0F, 2.0F, 1.6F, 8, 5);
      Geo.ellipsoid(var0, "xm_sack", -1.3F, 9.4F, -6.1F, 1.9F, 2.1F, 1.5F, 8, 5);
      var0.push();
      var0.translate(0.0F, 11.3F, -6.4F);
      Cos3Geo.lathe(var0, "xm_sack", new float[]{1.4F, 0.8F, 0.7F, 1.0F, 1.25F}, new float[]{0.0F, 0.6F, 1.0F, 1.5F, 1.9F}, 12);
      var0.color(-11414);
      var0.translate(0.0F, 0.8F, 0.0F);
      var0.torus("xm_gold", 0.82F, 0.18F, 12, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.push();
      var0.translate(0.4F, 12.3F, -6.4F);
      var0.rotZ(14.0F);
      var0.rotY(20.0F);
      XmasBack.gift(var0, "xm_gift:2E9A48:FFFFFF:0", -2088918, 1.8F, 1.6F, 1.6F);
      var0.pop();
      var0.push();
      var0.translate(-1.4F, 12.0F, -6.0F);
      var0.rotZ(-22.0F);
      var0.rotY(-15.0F);
      XmasBack.gift(var0, "xm_gift:3A6AE8:E8F4FF:3", -11414, 1.4F, 1.4F, 1.4F);
      var0.pop();
      var0.push();
      var0.translate(1.6F, 11.8F, -6.9F);
      var0.color(-1);
      float[][] var16 = new float[][]{
         {0.0F, 0.0F, 0.0F}, {0.3F, 2.4F, 0.0F}, {0.55F, 3.1F, 0.0F}, {1.05F, 3.35F, 0.0F}, {1.5F, 3.0F, 0.0F}, {1.55F, 2.6F, 0.0F}
      };
      Cos3Geo.limb(var0, "xm_candy", var16, new float[]{0.26F, 0.26F, 0.26F, 0.26F, 0.26F, 0.26F}, 7);
      var0.pop();
      float[][] var18 = new float[][]{{5.25F, 9.2F, 11.0F}, {5.4F, 11.4F, 11.5F}, {5.5F, 13.0F, 12.4F}, {5.5F, 13.4F, 13.5F}};
      var0.color(-14013904);
      Cos3Geo.limb(var0, "c3_metal", var18, new float[]{0.18F, 0.16F, 0.15F, 0.13F}, 6);
      var0.push();
      var0.translate(5.5F, 13.3F, 13.5F);
      var0.rotX(Cos3Geo.sin(var2 * 2.2F) * 8.0F - var3 * 10.0F);
      var0.rotZ(Cos3Geo.sin(var2 * 1.7F) * 6.0F);
      var0.color(-14013904);
      var0.push();
      var0.translate(0.0F, -0.35F, 0.0F);
      var0.rotX(90.0F);
      var0.torus("c3_metal", 0.3F, 0.07F, 8, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      Cos3Geo.lathe(var0, "c3_metal", new float[]{0.0F, 0.55F, 0.9F, 0.95F, 0.8F}, new float[]{-0.6F, -0.75F, -1.1F, -1.35F, -1.35F}, 8);
      float var21 = 0.85F + 0.1F * Cos3Geo.sin(var2 * 11.0F) + 0.05F * Cos3Geo.sin(var2 * 17.3F);
      var0.glow(true).color(-1);
      var0.cylinder("c3_lglass", 0.68F, 0.72F, -3.2F, -1.35F, 4, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.push();
      var0.translate(0.0F, -2.9F, 0.0F);
      var0.scale(1.0F, var21, 1.0F);
      Cos3Models.flame(var0, 1.1F, 0.3F, -385902038, -251661136);
      var0.pop();
      var0.glow(false).color(-14013904);
      Cos3Geo.lathe(var0, "c3_metal", new float[]{0.8F, 0.95F, 0.8F, 0.0F}, new float[]{-3.2F, -3.35F, -3.6F, -3.62F}, 8);
      Cos3Geo.halo(var0, 0.0F, -2.3F, 0.0F, 5.5F, Cos3Geo.alpha(0.5 * var21, 16762986));
      var0.pop();
      byte var23 = 5;

      for (int var25 = 0; var25 < var23; var25++) {
         float var29 = Cos3Geo.fract(var2 * 0.9F + (float)var25 / var23);
         float var31 = Cos3Geo.sin(var25 * 2.7F + var2) * 3.5F;
         float var12 = 2.0F + (float)Capes.hash1(var25, 601) * 6.0F + var29 * 2.0F;
         float var13 = -10.0F - var29 * 9.0F;
         float var14 = (0.25F + 0.75F * var3) * (1.0F - var29);
         if (var1.preview) {
            var14 = 0.7F * (1.0F - var29);
         }

         Cos3Models.sparkle(var0, var31, var12, var13, 0.35F, Cos3Geo.alpha(var14, var25 % 2 == 0 ? 16769146 : 15267071));
      }

      if (var3 > 0.05F) {
         for (byte var26 = -1; var26 <= 1; var26 += 2) {
            for (int var30 = 0; var30 < 3; var30++) {
               float var32 = Cos3Geo.fract(var2 * 2.2F + var30 / 3.0F + (var26 > 0 ? 0.0F : 0.17F));
               Cos3Geo.halo(
                  var0,
                  var26 * (4.7F + var32 * 1.2F),
                  0.5F + var32 * 1.2F,
                  -8.5F - var32 * 3.0F,
                  1.2F + var32 * 2.0F,
                  Cos3Geo.alpha(var3 * (1.0F - var32) * 0.7, 16777215)
               );
            }
         }
      }

      var0.pop();
      var0.color(-1);
   }
}
