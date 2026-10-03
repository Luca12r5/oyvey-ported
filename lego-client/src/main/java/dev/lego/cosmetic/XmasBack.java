package dev.lego.cosmetic;

import java.util.ArrayList;
import java.util.Arrays;

final class XmasBack {
   private XmasBack() {
   }

   static void register() {
      Cos.add("xm_back_scarf", "Weihnachtsschal", Cos.Slot.BACK, Cos.Rarity.RARE, XmasBack::scarf);
      Cos.add("xm_back_tree", "Mini-Weihnachtsbaum", Cos.Slot.BACK, Cos.Rarity.LEGENDARY, XmasBack::treePack);
      Cos.add("xm_back_candycanes", "Zuckerstangen", Cos.Slot.BACK, Cos.Rarity.EPIC, XmasBack::candyCanes);
      Cos.add("xm_back_gifts", "Geschenke-Stapel", Cos.Slot.BACK, Cos.Rarity.EPIC, XmasBack::giftStack);
      Cos.add("xm_aura_snow", "Schneegestöber", Cos.Slot.AURA, Cos.Rarity.EPIC, XmasBack::snowStorm);
      Cos.add("xm_aura_lights", "Lichterketten-Spirale", Cos.Slot.AURA, Cos.Rarity.LEGENDARY, XmasBack::lightSpiral);
      Cos.add("xm_aura_aurora", "Nordlicht-Band", Cos.Slot.AURA, Cos.Rarity.LEGENDARY, XmasBack::auroraBand);
      Cos.add("xm_wings_ice", "Eiskristall-Flügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, XmasBack::iceWings);
      Cos.add("xm_wings_angel", "Goldene Sternenflügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, XmasBack::starWings);
   }

   static float mv(Cos.A var0) {
      return var0.preview ? 0.0F : Cos3Geo.clamp(var0.move, 0.0F, 1.0F);
   }

   static void scarf(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = mv(var1);
      float[][] var4 = densLoop(Cos3Geo.roundLoop(3.35F, 1.35F, 1.35F, 6), 0.6F);
      int var5 = var4.length;
      byte var6 = 10;
      float var7 = 0.66F;
      float var8 = 1.05F;
      float var9 = -1.2F;
      float[][][] var10 = new float[var5 + 1][var6 + 1][];

      for (int var11 = 0; var11 <= var5; var11++) {
         float[] var12 = var4[var11 % var5];
         float var13 = (float)Math.cos(var12[2]);
         float var14 = (float)Math.sin(var12[2]);
         float var15 = 1.0F + 0.06F * Cos3Geo.sin(var11 * 2.1F);

         for (int var16 = 0; var16 <= var6; var16++) {
            double var17 = (Math.PI * 2) * var16 / var6;
            float var19 = (float)Math.cos(var17) * var7 * var15;
            float var20 = (float)Math.sin(var17) * var8 * var15;
            var10[var11][var16] = new float[]{var12[0] + var13 * var19, var9 + var20, var12[1] + var14 * var19};
         }
      }

      var0.color(-1);

      for (int var21 = 0; var21 < var5; var21++) {
         float var23 = var4[var21][3];
         float var25 = var21 + 1 < var5 ? var4[var21 + 1][3] : 1.0F;

         for (int var27 = 0; var27 < var6; var27++) {
            float var29 = (float)var27 / var6;
            float var31 = (float)(var27 + 1) / var6;
            var0.quad(
               "xm_scarf",
               var10[var21][var27],
               var10[var21][var27 + 1],
               var10[var21 + 1][var27 + 1],
               var10[var21 + 1][var27],
               new float[]{var29, var23, var31, var23, var31, var25, var29, var25}
            );
         }
      }

      var0.color(-1);
      Geo.ellipsoid(var0, "xm_scarf", 2.2F, -1.5F, 2.95F, 1.15F, 1.05F, 0.75F, 10, 6);

      for (int var22 = 0; var22 < 2; var22++) {
         float var24 = var22 == 0 ? 8.2F : 6.8F;
         float var26 = var22 == 0 ? 2.85F : 3.3F;
         float var28 = Cos3Geo.sin(var2 * 1.6F + var22 * 1.3F) * 0.18F + var3 * (0.6F + 0.2F * Cos3Geo.sin(var2 * 9.0F + var22));
         var0.push();
         var0.translate(2.2F, -1.6F, 0.0F);
         var0.rotZ(var22 == 0 ? 7.0F : -13.0F);
         byte var30 = 8;
         float[][] var32 = new float[var30][];
         float[] var33 = new float[var30];

         for (int var18 = 0; var18 < var30; var18++) {
            float var35 = (float)var18 / (var30 - 1);
            var32[var18] = new float[]{
               -var35 * var24, var26 + 0.25F * var35 + var28 * var35 * var35 * 2.2F + 0.15F * Cos3Geo.sin(var35 * 5.0F + var2 * 1.2F + var22)
            };
            var33[var18] = 2.0F + 0.15F * var35;
         }

         Cos3Geo.ribbon(var0, "xm_scarf", 0.0F, var32, var33, 0.36F);
         float[] var34 = var32[var30 - 1];
         var0.color(-723724);

         for (int var36 = 0; var36 < 5; var36++) {
            float var37 = -0.85F + var36 * 0.425F;
            Geo.tube(
               var0,
               "xm_knit",
               new float[]{var37, var34[0] + 0.1F, var34[1]},
               new float[]{var37 + Cos3Geo.sin(var2 * 2.0F + var36) * 0.08F, var34[0] - 0.9F, var34[1] + var28 * 0.4F},
               0.11F,
               0.08F,
               4
            );
         }

         var0.color(-1);
         var0.pop();
      }
   }

   static float[][] densLoop(float[][] var0, float var1) {
      ArrayList var2 = new ArrayList();
      int var3 = var0.length;

      for (int var4 = 0; var4 < var3; var4++) {
         float[] var5 = var0[var4];
         float[] var6 = var0[(var4 + 1) % var3];
         float var7 = (float)Math.hypot(var6[0] - var5[0], var6[1] - var5[1]);
         int var8 = Math.max(1, (int)Math.ceil(var7 / var1));
         float var9 = var6[2] < var5[2] - 1.0F ? var6[2] + (float) (Math.PI * 2) : var6[2];

         for (int var10 = 0; var10 < var8; var10++) {
            float var11 = (float)var10 / var8;
            var2.add(new float[]{var5[0] + (var6[0] - var5[0]) * var11, var5[1] + (var6[1] - var5[1]) * var11, var5[2] + (var9 - var5[2]) * var11, 0.0F});
         }
      }

      float var12 = 0.0F;
      float[] var13 = new float[var2.size()];

      for (int var14 = 0; var14 < var2.size(); var14++) {
         var13[var14] = var12;
         float[] var16 = (float[])var2.get(var14);
         float[] var17 = (float[])var2.get((var14 + 1) % var2.size());
         var12 += (float)Math.hypot(var17[0] - var16[0], var17[1] - var16[1]);
      }

      for (int var15 = 0; var15 < var2.size(); var15++) {
         ((float[])var2.get(var15))[3] = var13[var15] / var12;
      }

      return (float[][])var2.toArray(new float[0][]);
   }

   static void treePack(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = mv(var1);
      float var4 = -6.0F;
      var0.color(-4159408);
      Cos3Geo.bbox(var0, "xm_wood", -2.9F, -11.0F, var4 - 2.9F, 2.9F, -7.3F, -2.3F, 0.3F);
      var0.color(-8762840);

      for (int var5 = 0; var5 < 2; var5++) {
         float var6 = -10.2F + var5 * 1.9F;
         Cos3Geo.bbox(var0, "xm_wood", -3.0F, var6 - 0.25F, var4 - 3.0F, 3.0F, var6 + 0.25F, -2.2F, 0.1F);
      }

      var0.color(-1);
      Geo.ellipsoid(var0, "xm_snow", 0.0F, -7.3F, var4 + 0.35F, 2.6F, 0.4F, 2.9F, 12, 4);
      var0.color(-9813470);
      var0.push();
      var0.translate(0.0F, 0.0F, var4);
      var0.cylinder("xm_bark", 0.62F, 0.55F, -7.4F, -6.2F, 8, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      float var21 = Cos3Geo.sin(var2 * 1.4F) * 1.5F + var3 * Cos3Geo.sin(var2 * 8.0F) * 3.0F;
      var0.push();
      var0.translate(0.0F, -7.3F, var4);
      var0.rotX(var21 * 0.4F);
      var0.rotZ(var21);
      var0.translate(0.0F, 7.3F, 0.0F);
      float[][] var22 = new float[][]{{-6.9F, -2.1F, 3.25F}, {-3.9F, 0.9F, 2.65F}, {-1.2F, 3.3F, 2.05F}, {1.4F, 5.3F, 1.4F}};
      int[] var7 = new int[]{-13727158, -13330342, -12934046, -12275606};

      for (int var8 = 0; var8 < var22.length; var8++) {
         float var9 = var22[var8][0];
         float var10 = var22[var8][1];
         float var11 = var22[var8][2];
         float var12 = var10 - var9;
         var0.push();
         var0.rotY(var8 * 23);
         var0.color(var7[var8]);
         Cos3Geo.lathe(
            var0,
            "xm_pine",
            new float[]{0.0F, var11 * 0.82F, var11, var11 * 0.9F, var11 * 0.55F, var11 * 0.22F, 0.0F},
            new float[]{var9 + 0.25F, var9 - 0.05F, var9 + 0.2F, var9 + var12 * 0.18F, var9 + var12 * 0.5F, var9 + var12 * 0.82F, var10},
            14
         );
         var0.pop();
      }

      int[] var23 = new int[]{-2088918, -14278, -12940545, -2088918, -3618600, -14278, -5227800};

      for (int var24 = 0; var24 < 9; var24++) {
         int var26 = var24 % 3;
         float var28 = var22[var26][0];
         float var30 = var22[var26][1];
         float var13 = var22[var26][2];
         float var14 = 0.22F + 0.1F * (var24 / 3);
         float var15 = var28 + (var30 - var28) * var14;
         float var16 = var13 * (0.95F - var14 * 1.05F) + 0.3F;
         double var17 = var24 * 2.4 + 0.6;
         float var19 = Cos3Geo.cos(var17) * var16;
         float var20 = Cos3Geo.sin(var17) * var16;
         var0.color(-3618616);
         Geo.tube(var0, "c3_metal", new float[]{var19, var15 + 0.55F, var20}, new float[]{var19, var15 + 0.3F, var20}, 0.12F, 0.12F, 4);
         var0.color(var23[var24 % var23.length]);
         Cos3Geo.ball(var0, "xm_gloss", var19, var15, var20, 0.42F, 8);
      }

      for (int var25 = 0; var25 < 16; var25++) {
         float var27 = var25 / 16.0F;
         float var29 = -6.6F + var27 * 11.2F;
         float var31 = 0.0F;

         for (float[] var36 : var22) {
            if (var29 >= var36[0] - 0.1F && var29 <= var36[1]) {
               float var37 = (var29 - var36[0]) / (var36[1] - var36[0]);
               var31 = Math.max(var31, var36[2] * (1.0F - var37) * 0.95F + 0.2F);
            }
         }

         if (!(var31 < 0.35F)) {
            double var33 = var27 * Math.PI * 2.0 * 3.2 + 1.1;
            XmasGeo.fairy(
               var0,
               Cos3Geo.cos(var33) * var31,
               var29,
               Cos3Geo.sin(var33) * var31,
               0.24F,
               XmasGeo.LIGHTS[var25 % XmasGeo.LIGHTS.length],
               XmasGeo.twinkle(var2, var25)
            );
         }
      }

      var0.push();
      var0.translate(0.0F, 6.05F, 0.0F);
      var0.rotY(Cos3Geo.sin(var2 * 0.8F) * 35.0F);
      var0.glow(true).color(-10166);
      XmasGeo.star(var0, "xm_gold", 1.15F, 0.4F, 5);
      var0.glow(false);
      Cos3Geo.halo(var0, 0.0F, 0.0F, 0.0F, 3.6F, Cos3Geo.alpha(0.45 + 0.2 * Cos3Geo.sin(var2 * 3.0F), 16769146));
      var0.pop();
      var0.pop();
      var0.color(-1);
   }

   static void candyCanes(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = mv(var1);
      var0.color(-1521496);
      Cos3Geo.bbox(var0, "strap", -1.7F, -9.6F, -2.52F, 1.7F, -6.2F, -2.06F, 0.18F);
      var0.color(-11414);
      Cos3Geo.bbox(var0, "c3_metal", -0.55F, -8.3F, -2.64F, 0.55F, -7.5F, -2.45F, 0.08F);

      for (byte var4 = -1; var4 <= 1; var4 += 2) {
         float var5 = var4 > 0 ? -2.95F : -4.2F;
         float[] var6 = new float[]{-var4 * 2.3F, -11.8F, var5};
         float[] var7 = new float[]{var4 * 6.2F, 2.3F, var5};
         float var8 = var7[0] - var6[0];
         float var9 = var7[1] - var6[1];
         float var10 = (float)Math.hypot(var8, var9);
         var8 /= var10;
         var9 /= var10;
         float var11 = var9 * var4;
         float var12 = -var8 * var4;
         float var13 = 1.55F;
         float[] var14 = new float[]{var7[0] + var11 * var13, var7[1] + var12 * var13};
         byte var15 = 10;
         float[][] var16 = new float[2 + var15][];
         var16[0] = var6;
         var16[1] = new float[]{var6[0] + var8 * var10 * 0.5F, var6[1] + var9 * var10 * 0.5F, var5};

         for (int var17 = 0; var17 < var15; var17++) {
            double var18 = Math.PI * (var17 + 1) / var15 * 1.05;
            float var20 = (float)(-var11 * Math.cos(var18) + var8 * Math.sin(var18));
            float var21 = (float)(-var12 * Math.cos(var18) + var9 * Math.sin(var18));
            var16[2 + var17] = new float[]{var14[0] + var20 * var13, var14[1] + var21 * var13, var5};
         }

         var16[1] = var7;
         float[] var24 = new float[var16.length];
         Arrays.fill(var24, 0.62F);
         var0.push();
         var0.translate(0.0F, -7.9F, 0.0F);
         var0.rotZ(Cos3Geo.sin(var2 * 1.2F + var4) * 1.2F + var3 * Cos3Geo.sin(var2 * 9.0F) * 2.0F);
         var0.translate(0.0F, 7.9F, 0.0F);
         var0.color(-1);
         Cos3Geo.limb(var0, "xm_candy", var16, var24, 10);
         var0.pop();
      }

      var0.push();
      var0.translate(0.0F, -7.2F, -5.0F);
      var0.rotY(180.0F);
      XmasGeo.bow(var0, -13719478, 1.1F);
      var0.pop();
      var0.push();
      var0.translate(0.0F, -6.3F, -5.1F);
      var0.rotX(-80.0F);
      XmasGeo.holly(var0, 0.7F);
      var0.pop();
   }

   static void gift(G var0, String var1, int var2, float var3, float var4, float var5) {
      float var6 = -var3 / 2.0F;
      float var7 = var3 / 2.0F;
      float var8 = 0.0F;
      float var10 = -var5 / 2.0F;
      float var11 = var5 / 2.0F;
      var0.color(-1);
      Cos3Geo.bbox(var0, var1, var6, var8, var10, var7, var4, var11, 0.14F);
      float var12 = Math.min(var3, var5) * 0.1F + 0.2F;
      var0.color(var2);
      var0.box("xm_ribbon", -var12, var8 - 0.06F, var10 - 0.06F, var12, var4 + 0.06F, var11 + 0.06F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.box("xm_ribbon", var6 - 0.12F, var8 - 0.12F, -var12, var7 + 0.12F, var4 + 0.12F, var12, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   static void giftStack(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = mv(var1);
      float var4 = Cos3Geo.sin(var2 * 1.3F) * 1.2F + var3 * Cos3Geo.sin(var2 * 9.0F) * 2.5F;
      var0.push();
      var0.translate(0.0F, -11.6F, -4.3F);
      gift(var0, "xm_gift:D8202A:FFD24A:1", -11414, 7.2F, 4.6F, 4.3F);
      var0.translate(0.0F, 4.6F, -0.25F);
      var0.rotZ(var4 * 0.5F);
      var0.rotY(8.0F);
      gift(var0, "xm_gift:2E9A48:FFFFFF:0", -2088918, 5.4F, 3.6F, 3.8F);
      var0.translate(0.2F, 3.6F, -0.35F);
      var0.rotZ(var4 * 0.8F);
      var0.rotY(-22.0F);
      gift(var0, "xm_gift:3A6AE8:E8F4FF:3", -986888, 3.4F, 2.5F, 2.8F);
      var0.translate(0.0F, 2.62F, 0.0F);
      XmasGeo.bow(var0, -986888, 0.95F);
      var0.pop();
      var0.push();
      var0.translate(2.6F, -8.4F, -6.52F);
      var0.rotZ(Cos3Geo.sin(var2 * 2.0F) * 6.0F - 10.0F);
      var0.color(-726832);
      Cos3Geo.bbox(var0, "xm_felt", -0.6F, -1.3F, -0.08F, 0.6F, 0.0F, 0.0F, 0.05F);
      var0.pop();

      for (int var5 = 0; var5 < 4; var5++) {
         float var6 = Cos3Geo.fract(var2 * 0.4F + var5 * 0.25F);
         Cos3Models.sparkle(
            var0,
            Cos3Geo.sin(var5 * 2.3F) * 3.8F,
            -7.0F + var6 * 8.0F,
            -4.5F - Cos3Geo.cos(var5 * 1.7F) * 2.8F,
            0.28F,
            Cos3Geo.alpha((1.0F - var6) * 0.9, 16773296)
         );
      }

      var0.color(-1);
   }

   static void snowStorm(G var0, Cos.A var1) {
      float var2 = var1.time;

      for (int var3 = 0; var3 < 7; var3++) {
         double var4 = Capes.hash1(var3, 501);
         float var6 = Cos3Geo.fract(var2 * (0.05F + 0.03F * (float)var4) + var3 / 7.0F);
         float var7 = 9.0F - var6 * 34.0F;
         float var8 = (float)(var3 * 51.4 + var2 * (22.0 + var4 * 14.0));
         float var9 = 10.0F + 2.2F * (float)Capes.hash1(var3, 502);
         float var10 = Cos3Geo.smooth(var6 * 8.0F) * Cos3Geo.smooth((1.0F - var6) * 8.0F);
         var0.push();
         var0.rotY(var8);
         var0.translate(var9, var7, 0.0F);
         var0.rotY(var2 * 70.0F + var3 * 40);
         var0.rotX(Cos3Geo.sin(var2 + var3) * 30.0F);
         var0.glow(true).color(Cos3Geo.alpha(0.92 * var10, 15267583));
         XmasGeo.flake(var0, "white", 1.0F + 0.45F * (float)var4, 0.07F);
         var0.glow(false);
         var0.pop();
      }

      var0.color(-1);

      for (int var14 = 0; var14 < 34; var14++) {
         double var16 = Capes.hash1(var14, 511);
         float var18 = Cos3Geo.fract(var2 * (0.09F + 0.06F * (float)var16) + (float)Capes.hash1(var14, 512));
         float var20 = 10.0F - var18 * 35.0F;
         float var23 = (float)(var16 * 360.0 + var2 * (35.0 + 25.0 * Capes.hash1(var14, 513)) + var18 * 120.0F);
         float var26 = 9.0F + 4.5F * (float)Capes.hash1(var14, 514);
         float var29 = Cos3Geo.smooth(var18 * 6.0F) * Cos3Geo.smooth((1.0F - var18) * 6.0F);
         double var11 = Math.toRadians(var23);
         float var13 = 0.6F + 0.7F * (float)Capes.hash1(var14, 515);
         var0.glow(true).color(Cos3Geo.alpha(0.9 * var29, 16777215));
         var0.push();
         var0.translate(Cos3Geo.cos(var11) * var26, var20, Cos3Geo.sin(var11) * var26);
         var0.rotY(-var23 + var2 * 50.0F);
         var0.rotZ(var2 * 80.0F + var14 * 20);
         var0.cross("xm_flake", 0.0F, 0.0F, 0.0F, var13, var13, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      var0.glow(false);

      for (int var15 = 0; var15 < 2; var15++) {
         byte var17 = 40;
         float[][] var5 = new float[var17 + 1][];
         float[][] var19 = new float[var17 + 1][];

         for (int var21 = 0; var21 <= var17; var21++) {
            float var24 = (float)var21 / var17;
            double var27 = var24 * Math.PI * 2.6 + var2 * (0.9 + var15 * 0.4) + var15 * Math.PI;
            float var30 = 10.5F + var15 * 1.3F + Cos3Geo.sin(var24 * 9.0F + var2) * 0.5F;
            float var12 = -20.0F + var24 * 26.0F + var15 * 3;
            float var31 = 1.2F;
            var5[var21] = new float[]{Cos3Geo.cos(var27) * var30, var12 + var31, Cos3Geo.sin(var27) * var30};
            var19[var21] = new float[]{Cos3Geo.cos(var27) * var30, var12 - var31, Cos3Geo.sin(var27) * var30};
         }

         var0.glow(true).color(-1593835521);

         for (int var22 = 0; var22 < var17; var22++) {
            float var25 = (float)var22 / var17;
            float var28 = (float)(var22 + 1) / var17;
            var0.quad("xm_wind", var5[var22], var5[var22 + 1], var19[var22 + 1], var19[var22], new float[]{var25, 0.0F, var28, 0.0F, var28, 1.0F, var25, 1.0F});
         }

         var0.glow(false);
      }

      var0.push();
      var0.translate(0.0F, -23.9F, 0.0F);
      var0.rotX(-90.0F);
      var0.color(-1862270977);
      var0.plane("orb", 0.0F, 0.0F, 24.0F, 24.0F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.color(-1);
   }

   static void lightSpiral(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = 3.1F;
      float var4 = -23.0F;
      float var5 = 10.0F;
      byte var6 = 90;
      float[][] var7 = new float[var6 + 1][];
      double var8 = Math.toRadians(var2 * 18.0F);

      for (int var10 = 0; var10 <= var6; var10++) {
         float var11 = (float)var10 / var6;
         double var12 = var11 * Math.PI * 2.0 * var3 + var8;
         float var14 = 10.4F - 1.6F * var11 + 0.15F * Cos3Geo.sin(var11 * 60.0F);
         var7[var10] = new float[]{
            Cos3Geo.cos(var12) * var14,
            var4 + (var5 - var4) * var11 - 0.25F * Math.abs(Cos3Geo.sin(var11 * Math.PI * 2.0 * var3 * 3.5)),
            Cos3Geo.sin(var12) * var14
         };
      }

      var0.color(-14796254);
      float[] var20 = new float[var6 + 1];
      Arrays.fill(var20, 0.11F);
      Geo.chain(var0, "c3_metal", var7, var20, 3);
      byte var21 = 22;

      for (int var22 = 0; var22 < var21; var22++) {
         float var13 = (var22 + 0.5F) / var21;
         double var23 = var13 * Math.PI * 2.0 * var3 + var8;
         float var16 = 10.4F - 1.6F * var13;
         float var17 = var4 + (var5 - var4) * var13 - 0.3F;
         float var18 = 0.5F + 0.5F * Cos3Geo.sin(var22 * 0.9F - var2 * 4.5F);
         float var19 = 0.3F + 0.7F * Cos3Geo.smooth(var18 * 1.5F - 0.25F);
         var0.push();
         var0.translate(Cos3Geo.cos(var23) * var16, var17, Cos3Geo.sin(var23) * var16);
         var0.rotY((float)(-Math.toDegrees(var23)));
         var0.rotZ(-150.0F + Cos3Geo.sin(var2 * 2.0F + var22) * 8.0F);
         XmasGeo.bulbLite(var0, 1.05F, XmasGeo.LIGHTS[var22 % XmasGeo.LIGHTS.length], var19);
         var0.pop();
      }

      var0.color(-1);
   }

   static void auroraBand(G var0, Cos.A var1) {
      float var2 = var1.time;

      for (int var3 = 0; var3 < 2; var3++) {
         byte var4 = 72;
         float[][] var5 = new float[var4 + 1][];
         float[][] var6 = new float[var4 + 1][];
         double var7 = var2 * (var3 == 0 ? 0.25 : -0.18);

         for (int var9 = 0; var9 <= var4; var9++) {
            double var10 = (Math.PI * 2) * var9 / var4;
            float var12 = (var3 == 0 ? 10.8F : 12.0F) + 0.9F * Cos3Geo.sin(var10 * 3.0 + var2 * 0.7F + var3) + 0.35F * Cos3Geo.sin(var10 * 17.0 + var2 * 1.3F);
            float var13 = var3 == 0 ? 1.8F + 1.5F * Cos3Geo.sin(var10 * 2.0 - var2 * 0.6F) : -15.0F + 1.8F * Cos3Geo.sin(var10 * 2.0 + var2 * 0.5F + 1.0);
            float var14 = (var3 == 0 ? 6.5F : 5.0F) + 1.8F * Cos3Geo.sin(var10 * 4.0 + var2 * 0.9F + var3 * 2);
            double var15 = var10 + var7;
            float var17 = Cos3Geo.cos(var15);
            float var18 = Cos3Geo.sin(var15);
            var6[var9] = new float[]{var17 * var12, var13, var18 * var12};
            var5[var9] = new float[]{var17 * (var12 + 0.9F), var13 + var14, var18 * (var12 + 0.9F)};
         }

         var0.glow(true).color(var3 == 0 ? -251658241 : -922746881);

         for (int var23 = 0; var23 < var4; var23++) {
            float var24 = (float)var23 / var4 * 2.0F;
            float var11 = (float)(var23 + 1) / var4 * 2.0F;
            var0.quad("xm_aurora", var5[var23], var5[var23 + 1], var6[var23 + 1], var6[var23], new float[]{var24, 0.0F, var11, 0.0F, var11, 1.0F, var24, 1.0F});
         }
      }

      var0.glow(false);

      for (int var19 = 0; var19 < 10; var19++) {
         float var20 = Cos3Geo.fract(var2 * 0.3F + var19 * 0.1F);
         double var21 = var19 * 0.63 + var2 * 0.2;
         float var22 = Cos3Geo.sin(Math.PI * var20);
         Cos3Models.sparkle(
            var0,
            Cos3Geo.cos(var21) * 11.5F,
            5.0F + (float)Capes.hash1(var19, 531) * 7.0F - 20 * (var19 % 2),
            Cos3Geo.sin(var21) * 11.5F,
            0.3F,
            Cos3Geo.alpha(var22, var19 % 3 == 0 ? 12124120 : 15266047)
         );
      }

      var0.color(-1);
   }

   static void iceWings(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = mv(var1);
      float var4 = Cos3Geo.sin(var2 * 1.5F) * 6.0F * (1.0F - var3) + Cos3Geo.sin(var2 * 6.0F) * 14.0F * var3;

      for (byte var5 = -1; var5 <= 1; var5 += 2) {
         var0.push();
         var0.translate(var5 * 1.0F, -2.4F, -3.4F);
         var0.scale(var5, 1.0F, 1.0F);
         var0.rotY(var1.sneak ? 50.0F : 22.0F);
         var0.rotZ(var4);
         var0.scale(1.3F);
         byte var6 = 8;
         float[][] var7 = new float[var6][];

         for (int var8 = 0; var8 < var6; var8++) {
            float var9 = 80 - var8 * 17;
            float var10 = 4.5F + 7.5F * Cos3Geo.sin(Math.PI * (var8 + 0.6) / (var6 + 0.2));
            double var11 = Math.toRadians(var9);
            float var13 = -40.0F * Math.max(0.0F, (float)Math.sin(var11));
            var0.push();
            var0.rotX(var13);
            var0.translate((float)Math.cos(var11) * 0.9F, (float)Math.sin(var11) * 0.9F, 0.0F);
            var0.rotZ(var9 - 90.0F);
            var0.scale(1.0F, 1.0F, 0.5F);
            var0.glow(true).color(Cos3Geo.argb(215, Capes.mix(15268095, 9097471, (float)var8 / var6)));
            Cos3Geo.shard(var0, "c3_crystal", 0.62F + 0.14F * Cos3Geo.sin(var8 * 1.3F), var10);
            if (var8 % 2 == 1) {
               var0.push();
               var0.translate(0.0F, var10 * 0.45F, 0.0F);
               var0.rotZ(28.0F);
               var0.color(Cos3Geo.argb(200, 13167871));
               Cos3Geo.shard(var0, "c3_crystal", 0.28F, var10 * 0.32F);
               var0.pop();
            }

            var0.pop();
            float var14 = (float)Math.cos(var11);
            float var15 = (float)Math.sin(var11);
            float var16 = 0.9F + var10 + 0.6F;
            double var17 = Math.toRadians(var13);
            var7[var8] = new float[]{var14 * var16, var15 * var16 * (float)Math.cos(var17), var15 * var16 * (float)Math.sin(var17)};
         }

         var0.glow(false);

         for (byte var21 = 0; var21 < var6; var21 += 3) {
            float[] var23 = var7[var21];
            var0.push();
            var0.translate(var23[0], var23[1] + Cos3Geo.sin(var2 * 2.0F + var21) * 0.3F, var23[2]);
            var0.rotY(var2 * 60.0F + var21 * 30);
            var0.glow(true).color(-251658241);
            XmasGeo.flake(var0, "white", 0.85F, 0.06F);
            var0.glow(false);
            var0.pop();
         }

         byte var22 = 5;
         float[][] var24 = new float[var22 * 6 + 1][];
         int var25 = 0;

         for (int var26 = 0; var26 < var22; var26++) {
            float[] var12 = var7[var26];
            float[] var29 = var7[var26 + 1];
            float[] var30 = lerp3(new float[]{0.0F, 0.0F, 0.0F}, var12, 0.72F);
            float[] var31 = lerp3(new float[]{0.0F, 0.0F, 0.0F}, var29, 0.72F);

            for (int var32 = 0; var32 < 6; var32++) {
               float var33 = var32 / 6.0F;
               float var18 = 4.0F * var33 * (1.0F - var33) * 0.9F;
               var24[var25++] = new float[]{
                  var30[0] + (var31[0] - var30[0]) * var33, var30[1] + (var31[1] - var30[1]) * var33 - var18, var30[2] + (var31[2] - var30[2]) * var33 - 0.25F
               };
            }

            if (var26 == var22 - 1) {
               var24[var25++] = new float[]{var31[0], var31[1], var31[2] - 0.25F};
            }
         }

         XmasGeo.wire(var0, Arrays.copyOf(var24, var25), 0.07F);

         for (byte var27 = 1; var27 < var25; var27 += 4) {
            float[] var28 = var24[var27];
            XmasGeo.fairy(
               var0, var28[0], var28[1] - 0.25F, var28[2], 0.22F, XmasGeo.LIGHTS[var27 / 3 % XmasGeo.LIGHTS.length], XmasGeo.twinkle(var2, var27 + var5 * 7)
            );
         }

         Cos3Geo.halo(var0, 0.0F, 0.0F, 0.0F, 2.0F, 1891166463);
         var0.pop();
      }

      for (int var19 = 0; var19 < 6; var19++) {
         float var20 = Cos3Geo.fract(var2 * 0.35F + var19 / 6.0F);
         Cos3Models.sparkle(
            var0,
            (var19 % 2 == 0 ? 1 : -1) * (4.0F + 6.0F * (float)Capes.hash1(var19, 541)),
            2.0F - var20 * 10.0F,
            -5.0F - 2.0F * (float)Capes.hash1(var19, 542),
            0.28F,
            Cos3Geo.alpha(1.0F - var20, 15268095)
         );
      }

      var0.color(-1);
   }

   static float[] lerp3(float[] var0, float[] var1, float var2) {
      return new float[]{var0[0] + (var1[0] - var0[0]) * var2, var0[1] + (var1[1] - var0[1]) * var2, var0[2] + (var1[2] - var0[2]) * var2};
   }

   static void starWings(G var0, Cos.A var1) {
      Wings.feathered(var0, var1, "gold", 16.5F, false, 5);
      Wings.Pose var2 = Wings.pose(var1, 1.0F);
      float var3 = 16.5F;
      float var4 = 1.0F - 0.18F * var2.fold;
      float var5 = var1.time;

      for (byte var6 = 1; var6 >= -1; var6 -= 2) {
         var0.push();
         var0.scale(var6, 1.0F, 1.0F);
         var0.translate(0.9F, -2.2F, -2.35F);
         var0.rotY(var2.sweep);
         var0.rotZ(var2.raise);
         var0.rotX(-8.0F);
         float[] var7 = new float[]{0.3F, 0.52F, 0.72F, 0.9F};

         for (int var8 = 0; var8 < var7.length; var8++) {
            float var9 = var7[var8];
            float var10 = var3 * var9 * var4;
            float var11 = var3 * (0.26F * Cos3Geo.sin(Math.PI * var9 * 0.92) + 0.1F * var9);
            float var12 = -var3 * 0.09F * Cos3Geo.sin(Math.PI * var9) - 1.3F;
            float var13 = 2.2F + var8 % 2 * 1.8F + var9 * 1.5F;
            float var14 = Cos3Geo.sin(var5 * 2.1F + var8 * 1.3F) * 10.0F;
            var0.push();
            var0.translate(var10, var11 - 0.2F, var12);
            var0.rotX(-var2.raise * 0.0F);
            var0.rotZ(-var2.raise + var14);
            var0.color(-5720);
            Geo.tube(var0, "white", new float[]{0.0F, 0.0F, 0.0F}, new float[]{0.0F, -var13, 0.0F}, 0.05F, 0.05F, 3);
            var0.translate(0.0F, -var13 - 0.85F, 0.0F);
            var0.rotY(var5 * 50.0F + var8 * 60);
            var0.glow(true).color(var8 % 2 == 0 ? -10166 : -3920);
            XmasGeo.star(var0, "xm_gold", 0.95F, 0.32F, 5);
            var0.glow(false);
            Cos3Geo.halo(var0, 0.0F, 0.0F, 0.0F, 2.6F, Cos3Geo.alpha(0.35 + 0.2 * Cos3Geo.sin(var5 * 3.0F + var8), 16769146));
            var0.pop();
         }

         var0.pop();
      }

      var0.color(-1);
   }
}
