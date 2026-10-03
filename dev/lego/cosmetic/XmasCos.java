package dev.lego.cosmetic;

import java.util.Arrays;

final class XmasCos {
   private XmasCos() {
   }

   static void registerAll() {
      Cos.add("xm_hat_antlers", "Rentier-Geweih", Cos.Slot.HAT, Cos.Rarity.EPIC, XmasCos::antlers);
      Cos.add("xm_hat_elf", "Elfenmütze", Cos.Slot.HAT, Cos.Rarity.EPIC, XmasCos::elfHat);
      Cos.add("xm_hat_lightcrown", "Lichterketten-Krone", Cos.Slot.HAT, Cos.Rarity.LEGENDARY, XmasCos::lightCrown);
      Cos.add("xm_hat_tophat", "Schneemann-Zylinder", Cos.Slot.HAT, Cos.Rarity.EPIC, XmasCos::topHat);
      Cos.add("xm_hat_earmuffs", "Lebkuchen-Ohrenschützer", Cos.Slot.HAT, Cos.Rarity.RARE, XmasCos::earmuffs);
      Cos.add("xm_face_rednose", "Rentier-Nase", Cos.Slot.FACE, Cos.Rarity.EPIC, XmasCos::redNose);
      XmasBack.register();
      cape("winterwald", "Winterwald", Cos.Rarity.EPIC);
      cape("lebkuchenhaus", "Lebkuchenhaus", Cos.Rarity.EPIC);
      cape("zuckerstange", "Zuckerstange", Cos.Rarity.RARE);
      cape("schlittenmond", "Rentierschlitten am Mond", Cos.Rarity.LEGENDARY);
      cape("pullover", "Weihnachtspullover", Cos.Rarity.RARE);
      cape("nordlicht", "Nordlicht-Weihnacht", Cos.Rarity.LEGENDARY);
      XmasPets.register();
   }

   private static void cape(String var0, String var1, Cos.Rarity var2) {
      Cos.ALL.add(new Cos.Item("xm_cape_" + var0, var1, Cos.Slot.CAPE, var2, Models::capePreview, "cape_xm_" + var0));
   }

   static float[] bandPt(double var0, float var2, float var3, float var4) {
      float[] var5 = Geo.sq(var0, 1.0F, 5.0F);
      return new float[]{var5[0] * var2, var4 + var5[1] * var3, 0.3F};
   }

   static void antlers(G var0, Cos.A var1) {
      float var2 = var1.time;
      byte var3 = 22;
      float[][] var4 = new float[var3 + 1][];

      for (int var5 = 0; var5 <= var3; var5++) {
         var4[var5] = bandPt(Math.PI * var5 / var3, 5.0F, 5.6F, 3.6F);
      }

      float[] var9 = new float[var3 + 1];
      Arrays.fill(var9, 0.42F);
      var0.color(-4712930);
      Cos3Geo.limb(var0, "xm_velvet", var4, var9, 7);

      for (byte var6 = -1; var6 <= 1; var6 += 2) {
         float[] var7 = bandPt(var6 > 0 ? Math.PI / 3 : Math.PI * 2.0 / 3.0, 5.0F, 5.6F, 3.6F);
         var0.push();
         var0.translate(var7[0], var7[1] - 0.1F, var7[2]);
         var0.scale(var6, 1.0F, 1.0F);
         var0.color(-9813464);
         Geo.ellipsoid(var0, "c3_fur", 0.0F, 0.1F, 0.0F, 0.75F, 0.6F, 0.75F, 8, 5);
         var0.color(-1);
         float[][] var8 = new float[][]{
            {0.0F, 0.2F, 0.0F},
            {0.45F, 1.4F, -0.15F},
            {1.35F, 2.8F, -0.45F},
            {2.15F, 4.0F, -0.55F},
            {2.6F, 5.3F, -0.35F},
            {2.55F, 6.5F, 0.05F},
            {2.25F, 7.3F, 0.35F}
         };
         Cos3Geo.limb(var0, "xm_antler", var8, new float[]{0.55F, 0.5F, 0.45F, 0.4F, 0.33F, 0.25F, 0.14F}, 7);
         Cos3Geo.limb(var0, "xm_antler", new float[][]{var8[1], {0.35F, 2.1F, 0.9F}, {0.2F, 2.6F, 1.5F}}, new float[]{0.36F, 0.26F, 0.12F}, 6);
         Cos3Geo.limb(var0, "xm_antler", new float[][]{var8[2], {1.2F, 4.0F, 0.6F}, {1.05F, 4.9F, 1.05F}}, new float[]{0.34F, 0.25F, 0.12F}, 6);
         Cos3Geo.limb(var0, "xm_antler", new float[][]{var8[3], {1.5F, 5.3F, -0.9F}, {1.25F, 6.2F, -1.0F}}, new float[]{0.3F, 0.22F, 0.1F}, 6);
         Cos3Geo.limb(var0, "xm_antler", new float[][]{var8[4], {3.5F, 6.1F, -0.7F}, {3.9F, 6.7F, -0.7F}}, new float[]{0.26F, 0.18F, 0.09F}, 6);
         var0.color(-2088918);
         Geo.ellipsoid(var0, "xm_velvet", 1.62F, 2.95F, 0.05F, 0.34F, 0.28F, 0.34F, 8, 4);
         var0.push();
         var0.translate(1.62F, 2.75F, 0.1F);
         XmasGeo.bell(var0, 0.9F, Cos3Geo.sin(var2 * 3.2F + var6) * 16.0F + (var1.preview ? 0.0F : var1.move * Cos3Geo.sin(var2 * 12.0F) * 20.0F));
         var0.pop();
         var0.pop();
      }

      var0.push();
      var0.translate(0.0F, 9.25F, 0.3F);
      var0.rotX(18.0F);
      XmasGeo.holly(var0, 0.72F);
      var0.pop();
   }

   static void elfHat(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = var1.preview ? 0.0F : Cos3Geo.clamp(var1.move, 0.0F, 1.0F);
      float var4 = Cos3Geo.sin(var2 * 1.7F) * 0.22F + Cos3Geo.sin(var2 * 3.1F) * 0.06F;
      float var5 = 0.35F + var3 * 0.35F + Cos3Geo.sin(var2 * 2.3F) * 0.05F;
      float[] var6 = new float[]{5.3F, 7.0F, 8.6F, 9.6F, 10.5F};
      float[] var7 = new float[]{4.7F, 4.8F, 4.6F, 3.75F, 3.05F};
      float[] var8 = new float[]{6.0F, 6.0F, 4.5F, 3.0F, 2.3F};
      byte var9 = 10;
      int var10 = var6.length + var9;
      float[][] var11 = new float[var10][];
      float[] var12 = new float[var10];
      float[] var13 = new float[var10];
      float[] var14 = new float[var10];

      for (int var15 = 0; var15 < var6.length; var15++) {
         var11[var15] = new float[]{0.0F, var6[var15], 0.0F};
         var12[var15] = var13[var15] = var7[var15];
         var14[var15] = var8[var15];
      }

      float[] var27 = (float[])var11[var6.length - 1].clone();

      for (int var16 = 0; var16 < var9; var16++) {
         float var17 = (float)(var16 + 1) / var9;
         double var18 = var17 * var17 * 2.3 * (0.8 + var5 * 0.6) + 0.15;
         double var20 = var4 * var17 * 1.8 + 0.35 * var17;
         float var22 = 1.45F - 0.3F * var17;
         float var23 = (float)(Math.sin(var20) * Math.sin(var18));
         float var24 = (float)Math.cos(var18);
         float var25 = (float)(-Math.cos(var20) * Math.sin(var18));
         var27 = new float[]{var27[0] + var23 * var22, var27[1] + var24 * var22, var27[2] + var25 * var22};
         int var26 = var6.length + var16;
         var11[var26] = var27;
         var12[var26] = var13[var26] = Math.max(0.08F, 2.75F * (1.0F - var17) + 0.08F);
         var14[var26] = 2.0F;
      }

      var0.color(-13723064);
      XmasGeo.sweep(var0, "xm_felt", var11, var12, var13, var14, 20, 3.0F);
      float[] var28 = var11[var10 - 1];
      var0.push();
      var0.translate(var28[0], var28[1] - 0.1F, var28[2]);
      XmasGeo.bell(var0, 1.05F, Cos3Geo.sin(var2 * 4.1F) * 20.0F + var4 * 30.0F);
      var0.pop();
      var0.color(-1);
      Cos3Geo.band(var0, "xm_elfcuff", 2.6F, 2.1F, 0.62F, 4.9F, 6.7F, 0.0F, 3.0F);

      for (byte var29 = -1; var29 <= 1; var29 += 2) {
         var0.push();
         var0.translate(var29 * 5.45F, 5.2F, 0.4F);
         XmasGeo.bell(var0, 0.55F, Cos3Geo.sin(var2 * 3.0F + var29) * 12.0F);
         var0.pop();
      }
   }

   static void lightCrown(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = 2.6F;
      float var4 = 2.1F;
      float var5 = 0.45F;
      var0.color(-11414);
      Cos3Geo.band(var0, "xm_gold", var3, var4, var5, 6.4F, 8.1F, 0.0F, 4.0F);
      var0.color(-5720);
      Cos3Geo.band(var0, "xm_gold", var3, var4 + var5 - 0.08F, 0.26F, 7.95F, 8.4F, 0.0F, 4.0F);
      Cos3Geo.band(var0, "xm_gold", var3, var4 + var5 - 0.08F, 0.26F, 6.2F, 6.6F, 0.0F, 4.0F);
      int var6 = (int)Math.floor(var2 * 1.4F);

      for (int var7 = 0; var7 < 8; var7++) {
         double var8 = var7 * Math.PI / 4.0 + (Math.PI / 8);
         float[] var10 = Cos3Models.loopPt(var3, var4 + var5 * 0.5F, var8);
         var0.push();
         var0.translate(var10[0], 8.35F, var10[1]);
         var0.rotY((float)(-Math.toDegrees(var8)) + 90.0F);
         var0.rotX(10.0F);
         var0.color(-11414);
         Cos3Geo.shard(var0, "xm_gold", 0.28F, 1.1F);
         var0.translate(0.0F, 1.0F, 0.0F);
         int var11 = XmasGeo.LIGHTS[(var7 + var6) % XmasGeo.LIGHTS.length];
         XmasGeo.bulb(var0, 1.25F, var11, XmasGeo.twinkle(var2, var7));
         var0.pop();
         float[] var12 = Cos3Models.loopPt(var3, var4 + var5 + 0.02F, var8 + (Math.PI / 8));
         var0.push();
         var0.translate(var12[0], 7.2F, var12[1]);
         var0.rotY((float)(-Math.toDegrees(var8 + (Math.PI / 8))) + 90.0F);
         var0.rotX(90.0F);
         var0.glow(true).color(var7 % 2 == 0 ? -2088918 : -13711270);
         Cos3Geo.gem(var0, "c3_crystal", 0.36F, 0.3F, 0.1F, 6);
         var0.glow(false);
         var0.pop();
      }

      byte var14 = 48;
      float[][] var15 = new float[var14 + 1][];

      for (int var9 = 0; var9 <= var14; var9++) {
         double var18 = (Math.PI * 2) * var9 / var14 + (Math.PI / 8);
         float[] var22 = Cos3Models.loopPt(var3, var4 + var5 + 0.28F, var18);
         float var13 = 8.25F - 1.25F * Math.abs(Cos3Geo.sin((var18 - (Math.PI / 8)) * 4.0));
         var15[var9] = new float[]{var22[0], var13, var22[1]};
      }

      XmasGeo.wire(var0, var15, 0.09F);

      for (int var16 = 0; var16 < 8; var16++) {
         double var19 = var16 * Math.PI / 4.0 + (Math.PI / 4);
         float[] var23 = Cos3Models.loopPt(var3, var4 + var5 + 0.42F, var19);
         int var24 = XmasGeo.LIGHTS[(var16 + 3 + var6) % XmasGeo.LIGHTS.length];
         XmasGeo.fairy(var0, var23[0], 6.85F, var23[1], 0.3F, var24, XmasGeo.twinkle(var2, var16 + 20));
      }

      var0.push();
      var0.translate(0.0F, 11.4F + Cos3Geo.sin(var2 * 2.0F) * 0.3F, 0.0F);
      var0.rotY(var2 * 40.0F);
      var0.glow(true).color(-10166);
      XmasGeo.star(var0, "xm_gold", 1.35F, 0.45F, 5);
      var0.glow(false);
      Cos3Geo.halo(var0, 0.0F, 0.0F, 0.0F, 4.2F, Cos3Geo.alpha(0.5 + 0.2 * Cos3Geo.sin(var2 * 3.0F), 16769146));
      var0.pop();

      for (int var17 = 0; var17 < 4; var17++) {
         float var20 = Cos3Geo.fract(var2 * 0.5F + var17 * 0.25F);
         float var21 = var17 * 90 + var2 * 40.0F;
         Cos3Models.sparkle(
            var0,
            Cos3Geo.cos(Math.toRadians(var21)) * 1.8F * (1.0F + var20),
            11.4F - var20 * 2.5F,
            Cos3Geo.sin(Math.toRadians(var21)) * 1.8F * (1.0F + var20),
            0.25F,
            Cos3Geo.alpha(1.0F - var20, 16773296)
         );
      }
   }

   static void topHat(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(0.0F, 8.0F, 0.0F);
      var0.rotZ(-6.0F + Cos3Geo.sin(var2 * 1.3F) * 0.8F);
      var0.rotX(-3.0F);
      var0.color(-14013900);
      Cos3Geo.lathe(
         var0,
         "xm_silk",
         new float[]{0.0F, 5.6F, 6.15F, 6.4F, 6.25F, 5.85F, 3.9F, 3.72F, 3.6F, 3.72F, 3.95F, 3.98F, 3.72F, 0.0F},
         new float[]{0.05F, 0.05F, 0.15F, 0.4F, 0.64F, 0.66F, 0.56F, 0.9F, 4.0F, 6.4F, 7.2F, 7.42F, 7.62F, 7.62F},
         32
      );
      var0.color(-3139030);
      Cos3Models.flatRing(var0, "xm_ribbon", 3.3F, 3.84F, 1.0F, 2.3F, 28);
      var0.push();
      var0.rotY(-35.0F);
      var0.translate(3.95F, 1.75F, 0.0F);
      var0.rotZ(-78.0F);
      XmasGeo.holly(var0, 0.95F);
      var0.pop();
      var0.color(-1);
      Geo.ellipsoid(var0, "xm_snow", 0.2F, 7.62F, 0.1F, 3.55F, 0.6F, 3.5F, 16, 6);
      Geo.ellipsoid(var0, "xm_snow", -1.3F, 8.05F, 0.6F, 1.4F, 0.55F, 1.3F, 10, 5);
      float[][] var3 = new float[][]{{4.7F, 0.6F, 1.1F}, {-3.0F, -3.9F, 1.3F}};

      for (float[] var7 : var3) {
         Geo.ellipsoid(var0, "xm_snow", var7[0], 0.6F, var7[1], var7[2], 0.2F, var7[2] * 0.7F, 10, 4);
      }

      var0.pop();

      for (int var10 = 0; var10 < 5; var10++) {
         float var11 = Cos3Geo.fract(var2 * 0.22F + var10 * 0.2F);
         float var12 = var10 * 72 + var2 * 25.0F;
         float var13 = Cos3Geo.cos(Math.toRadians(var12)) * 7.2F;
         float var8 = Cos3Geo.sin(Math.toRadians(var12)) * 7.2F;
         float var9 = 17.0F - var11 * 10.0F;
         var0.push();
         var0.translate(var13, var9, var8);
         var0.rotY(var12 + var2 * 60.0F);
         var0.glow(true).color(Cos3Geo.alpha(Cos3Geo.smooth(var11 * 5.0F) * Cos3Geo.smooth((1.0F - var11) * 5.0F) * 0.95, 15792383));
         var0.cross("xm_flake", 0.0F, 0.0F, 0.0F, 1.3F, 1.3F, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.glow(false);
         var0.pop();
      }

      var0.color(-1);
   }

   static void earmuffs(G var0, Cos.A var1) {
      float var2 = var1.time;
      byte var3 = 24;
      float[][] var4 = new float[var3 + 1][];

      for (int var5 = 0; var5 <= var3; var5++) {
         var4[var5] = new float[]{bandPt(Math.PI * var5 / var3, 5.0F, 5.9F, 3.4F)[0], bandPt(Math.PI * var5 / var3, 5.0F, 5.9F, 3.4F)[1], 0.0F};
      }

      float[] var8 = new float[var3 + 1];
      Arrays.fill(var8, 0.42F);
      var0.color(-1);
      Geo.chain(var0, "xm_candy", var4, var8, 8);
      float[][] var6 = new float[XmasTex.GMAN.length][];

      for (int var7 = 0; var7 < var6.length; var7++) {
         var6[var7] = new float[]{XmasTex.GMAN[var7][0] * 2.35F, XmasTex.GMAN[var7][1] * 2.35F};
      }

      for (byte var9 = -1; var9 <= 1; var9 += 2) {
         var0.push();
         var0.translate(var9 * 5.0F, 3.4F, 0.0F);
         var0.color(-593170);
         Geo.ellipsoid(var0, "xm_fluff", 0.0F, 0.0F, 0.0F, 0.95F, 2.45F, 2.45F, 14, 9);
         var0.push();
         var0.translate(var9 * 0.72F, 0.0F, 0.0F);
         var0.rotY(var9 * 90);
         var0.rotZ(Cos3Geo.sin(var2 * 2.2F + var9) * 6.0F);
         var0.color(-1);
         Cos3Geo.extrude(var0, "xm_gingerman", var6, -0.05F, 0.62F, Cos3Geo.FULL, new float[]{0.01F, 0.5F, 0.04F, 0.53F});
         var0.pop();
         var0.pop();
      }
   }

   static void redNose(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = 0.5F + 0.5F * Cos3Geo.sin(var2 * 3.4F);
      float var4 = 1.0F + 0.07F * var3;
      var0.push();
      var0.translate(0.0F, 3.1F, 4.72F);
      var0.scale(var4);
      var0.glow(true).color(0xFF000000 | Capes.mix(12587036, 16730698, var3));
      var0.sphere("xm_gloss", 1.08F, 16, 10, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-385875969);
      Cos3Geo.ball(var0, "white", -0.38F, 0.42F, 0.86F, 0.2F, 6);
      var0.glow(false);
      var0.pop();
      Cos3Geo.halo(var0, 0.0F, 3.1F, 5.2F, 3.4F + var3 * 1.6F, Cos3Geo.alpha(0.35 + 0.35 * var3, 16726586));

      for (int var5 = 0; var5 < 3; var5++) {
         float var6 = Cos3Geo.fract(var2 * 0.6F + var5 / 3.0F);
         Cos3Models.sparkle(
            var0, Cos3Geo.sin(var5 * 2.1F + var2) * 1.5F, 3.4F + var6 * 3.0F, 5.6F + var6 * 0.6F, 0.2F, Cos3Geo.alpha((1.0F - var6) * 0.9, 16747146)
         );
      }

      var0.color(-1);
   }
}
