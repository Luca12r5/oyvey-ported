package dev.lego.cosmetic;

import java.util.Arrays;
import java.util.Random;

final class HdHats {
   private HdHats() {
   }

   private static float[] p(float var0, float var1, float var2) {
      return new float[]{var0, var1, var2};
   }

   static void brim(G var0, String var1, float var2, float var3, float var4, float var5, float var6, int var7, int var8, HdHats.Lift var9) {
      float[][][] var10 = new float[var7 + 1][var8 + 1][];
      float[][][] var11 = new float[var7 + 1][var8 + 1][];

      for (int var12 = 0; var12 <= var7; var12++) {
         double var13 = (Math.PI * 2) * var12 / var7;
         float[] var15 = Geo.sq(var13, var2, var3);
         float var16 = Cos3Geo.cos(var13) * var4;
         float var17 = Cos3Geo.sin(var13) * var4 * var5;

         for (int var18 = 0; var18 <= var8; var18++) {
            float var19 = (float)var18 / var8;
            float var20 = var15[0] + (var16 - var15[0]) * var19;
            float var21 = var15[1] + (var17 - var15[1]) * var19;
            float var22 = var9.at(var13, var19);
            float var23 = var6 * (1.0F - 0.35F * var19);
            var10[var12][var18] = p(var20, var22 + var23 / 2.0F, var21);
            var11[var12][var18] = p(var20, var22 - var23 / 2.0F, var21);
         }
      }

      for (int var26 = 0; var26 < var7; var26++) {
         float var27 = (float)var26 / var7;
         float var14 = (float)(var26 + 1) / var7;

         for (int var28 = 0; var28 < var8; var28++) {
            float var30 = (float)var28 / var8;
            float var32 = (float)(var28 + 1) / var8;
            var0.quad(
               var1,
               var10[var26][var28 + 1],
               var10[var26 + 1][var28 + 1],
               var10[var26 + 1][var28],
               var10[var26][var28],
               new float[]{var27, var32, var14, var32, var14, var30, var27, var30}
            );
            var0.quad(
               var1,
               var11[var26][var28],
               var11[var26 + 1][var28],
               var11[var26 + 1][var28 + 1],
               var11[var26][var28 + 1],
               new float[]{var27, var30, var14, var30, var14, var32, var27, var32}
            );
         }

         float[] var29 = var10[var26][var8];
         float[] var31 = var10[var26 + 1][var8];
         float[] var33 = var11[var26][var8];
         float[] var34 = var11[var26 + 1][var8];
         double var35 = (Math.PI * 2) * var26 / var7;
         double var36 = (Math.PI * 2) * (var26 + 1) / var7;
         float var37 = var6 * 0.42F;
         float[] var24 = p(
            (var29[0] + var33[0]) / 2.0F + Cos3Geo.cos(var35) * var37,
            (var29[1] + var33[1]) / 2.0F,
            (var29[2] + var33[2]) / 2.0F + Cos3Geo.sin(var35) * var37 * var5
         );
         float[] var25 = p(
            (var31[0] + var34[0]) / 2.0F + Cos3Geo.cos(var36) * var37,
            (var31[1] + var34[1]) / 2.0F,
            (var31[2] + var34[2]) / 2.0F + Cos3Geo.sin(var36) * var37 * var5
         );
         var0.quad(var1, var29, var31, var25, var24, new float[]{var27, 0.96F, var14, 0.96F, var14, 1.0F, var27, 1.0F});
         var0.quad(var1, var24, var25, var34, var33, new float[]{var27, 0.96F, var14, 0.96F, var14, 1.0F, var27, 1.0F});
         var0.quad(
            var1, var11[var26][0], var11[var26 + 1][0], var10[var26 + 1][0], var10[var26][0], new float[]{var27, 0.0F, var14, 0.0F, var14, 0.04F, var27, 0.04F}
         );
      }
   }

   static float[][] bentSpine(float var0, float var1, int var2, double var3, double var5, float var7) {
      float[][] var8 = new float[var2 + 1][];
      float var9 = 0.0F;
      float var10 = var0;
      float var11 = 0.0F;
      double var12 = var1 / var2;

      for (int var14 = 0; var14 <= var2; var14++) {
         float var15 = (float)var14 / var2;
         var8[var14] = p(var9, var10, var11);
         double var16 = var15 < var7 ? 0.0 : Math.toRadians(var3) * Math.pow((var15 - var7) / (1.0F - var7), 1.6);
         double var18 = Math.toRadians(var5);
         var9 += (float)(Math.sin(var16) * Math.sin(var18) * var12);
         var11 += (float)(-Math.sin(var16) * Math.cos(var18) * var12);
         var10 += (float)(Math.cos(var16) * var12);
      }

      return var8;
   }

   static void ear(G var0, float[][] var1, float[][] var2, String var3, int var4, int var5, float var6) {
      int var7 = var0.color;
      var0.color(var4);
      HdGeo.puff(var0, var3, var1, var6, var6 * 0.45F, 2);
      var0.color(var5);
      var0.push();
      var0.translate(0.0F, 0.0F, var6 * 0.38F);
      HdGeo.puff(var0, "hd_velvet", var2, var6 * 0.4F, var6 * 0.18F, 1);
      var0.pop();
      var0.color(var7);
   }

   static float[][] earShape(float var0, float var1, float var2, int var3) {
      float[][] var4 = new float[var3 * 2 + 2][];
      int var5 = 0;
      var4[var5++] = p2(var0 / 2.0F, 0.0F);

      for (int var6 = 1; var6 <= var3; var6++) {
         float var7 = (float)var6 / (var3 + 1);
         float var8 = var0 / 2.0F * (1.0F - var7);
         float var9 = var1 * var7;
         float var10 = var2 * (float)Math.sin(Math.PI * var7);
         var4[var5++] = p2(var8 + var10 * 0.9F, var9 + var10 * 0.25F);
      }

      var4[var5++] = p2(0.0F, var1);

      for (int var13 = var3; var13 >= 1; var13--) {
         float var14 = (float)var13 / (var3 + 1);
         float var15 = -var0 / 2.0F * (1.0F - var14);
         float var16 = var1 * var14;
         float var17 = var2 * (float)Math.sin(Math.PI * var14);
         var4[var5++] = p2(var15 - var17 * 0.9F, var16 + var17 * 0.25F);
      }

      return Arrays.copyOf(var4, var5);
   }

   private static float[] p2(float var0, float var1) {
      return new float[]{var0, var1};
   }

   static void crown(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(0.0F, 0.0F, 0.0F);
      float var3 = 2.6F;
      float var4 = 2.12F;
      float var5 = 0.42F;
      float var6 = 7.1F;
      float var7 = 9.2F;
      var0.color(-5237730);
      Geo.sqDome(var0, "hd_velvet", 4.55F, 2.7F, 8.4F, 5.0F, 16, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1);
      Cos3Geo.band(var0, "hd_gold", var3, var4, var5, var6, var7, 0.0F, 4.0F);
      var0.color(-2840);

      for (int var8 = 0; var8 < 22; var8++) {
         float[] var9 = Cos3Models.loopPt(var3, var4 + var5 + 0.12F, (Math.PI * 2) * var8 / 22.0);
         Cos3Geo.ball(var0, "hd_gloss", var9[0], var6 + 0.25F, var9[1], 0.24F, 4);
      }

      var0.color(-5720);
      byte var15 = 36;
      float[][] var18 = new float[var15 + 1][];

      for (int var10 = 0; var10 <= var15; var10++) {
         float[] var11 = Cos3Models.loopPt(var3, var4 + var5 * 0.5F, (Math.PI * 2) * var10 / var15);
         var18[var10] = p(var11[0], var7, var11[1]);
      }

      float[] var22 = new float[var15 + 1];
      Arrays.fill(var22, var5 * 0.62F);
      XmasGeo.sweep(var0, "hd_gold", var18, var22, var22, null, 4, 4.0F);

      for (int var16 = 0; var16 < 8; var16++) {
         double var19 = (Math.PI * 2) * var16 / 8.0 + (Math.PI / 8);
         float[] var25 = Cos3Models.loopPt(var3, var4 + var5 * 0.5F, var19);
         boolean var12 = var16 % 2 == 0;
         float var13 = var12 ? 3.1F : 2.2F;
         var0.push();
         var0.translate(var25[0], var7 - 0.25F, var25[1]);
         var0.rotY((float)(-Math.toDegrees(var19)) + 90.0F);
         var0.rotX(-6.0F);
         float[][] var14 = new float[][]{
            {-1.35F, 0.0F},
            {1.35F, 0.0F},
            {0.9F, var13 * 0.3F},
            {0.42F, var13 * 0.62F},
            {0.12F, var13},
            {-0.12F, var13},
            {-0.42F, var13 * 0.62F},
            {-0.9F, var13 * 0.3F}
         };
         HdGeo.puff(var0, "hd_gold", var14, 0.42F, 0.16F, 1);
         var0.color(-2322);
         Cos3Geo.ball(var0, "hd_gloss", 0.0F, var13 + 0.35F, 0.0F, var12 ? 0.45F : 0.36F, 6);
         var0.glow(true).color(var12 ? -1564614 : -13993217);
         var0.push();
         var0.translate(0.0F, var13 * 0.34F, 0.18F);
         var0.rotX(90.0F);
         Cos3Geo.gem(var0, "hd_gem", 0.36F, 0.26F, 0.1F, 6);
         var0.pop();
         var0.glow(false).color(-1);
         var0.pop();
      }

      int[] var17 = new int[]{-1564614, -14763942, -13993217, -5223681};

      for (int var20 = 0; var20 < 8; var20++) {
         double var23 = (Math.PI * 2) * var20 / 8.0;
         float[] var27 = Cos3Models.loopPt(var3, var4 + var5 + 0.02F, var23);
         var0.push();
         var0.translate(var27[0], (var6 + var7) / 2.0F + 0.05F, var27[1]);
         var0.rotY((float)(-Math.toDegrees(var23)) + 90.0F);
         var0.color(-5720);
         var0.push();
         var0.rotX(90.0F);
         var0.torus("hd_gold", 0.55F, 0.13F, 8, 3, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
         var0.glow(true).color(var17[var20 % 4]);
         var0.rotX(90.0F);
         Cos3Geo.gem(var0, "hd_gem", var20 % 2 == 0 ? 0.52F : 0.4F, 0.4F, 0.12F, 6);
         var0.glow(false);
         var0.pop();
      }

      var0.color(-1);
      Cos3Geo.ball(var0, "hd_gold", 0.0F, 11.35F, 0.0F, 0.62F, 10);
      var0.color(-5720);
      Cos3Geo.cbox(var0, "hd_gold", 0.0F, 12.55F, 0.0F, 0.36F, 1.7F, 0.36F, 0.08F);
      Cos3Geo.cbox(var0, "hd_gold", 0.0F, 12.75F, 0.0F, 1.2F, 0.36F, 0.36F, 0.08F);
      var0.color(-1);

      for (int var21 = 0; var21 < 3; var21++) {
         float var24 = Math.max(0.0F, Cos3Geo.sin(var2 * 2.3F + var21 * 2.1F));
         double var26 = var21 * 2.1 + 0.4;
         float[] var28 = Cos3Models.loopPt(var3, var4 + var5 + 0.4F, var26);
         if (var24 > 0.05F) {
            Cos3Models.sparkle(var0, var28[0], 10.2F + var21 * 0.4F, var28[1], 0.45F * var24, Cos3Geo.alpha(var24, 16777215));
         }
      }

      var0.pop();
   }

   static void halo(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = Cos3Geo.sin(var2 * 2.2F) * 0.4F;
      var0.push();
      var0.translate(0.0F, 11.6F + var3, 0.0F);
      var0.rotX(16.0F + Cos3Geo.sin(var2 * 1.1F) * 3.0F);
      var0.push();
      var0.rotY(var2 * 30.0F);
      var0.glow(true).color(-5728);
      var0.torus("hd_gold", 4.55F, 0.46F, 40, 10, 0.0F, 0.0F, 4.0F, 1.0F);
      var0.color(-1);
      var0.torus("white", 4.55F, 0.16F, 40, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.glow(false);
      var0.pop();
      float var4 = 0.7F + 0.3F * Cos3Geo.sin(var2 * 3.0F);
      var0.glow(true).color(Cos3Geo.alpha(0.35 * var4, 16769146));
      var0.torus("hd_softglow", 4.55F, 1.35F, 32, 6, 0.1F, 0.1F, 0.9F, 0.9F);
      var0.glow(false);

      for (int var5 = 0; var5 < 6; var5++) {
         double var6 = (Math.PI * 2) * var5 / 6.0 + var2 * 1.2;
         float var8 = 0.5F + 0.5F * Cos3Geo.sin(var2 * 4.0F + var5 * 1.9F);
         Cos3Models.sparkle(var0, Cos3Geo.cos(var6) * 4.55F, 0.1F, Cos3Geo.sin(var6) * 4.55F, 0.35F + var8 * 0.35F, -1);
         Cos3Geo.halo(var0, Cos3Geo.cos(var6) * 4.55F, 0.1F, Cos3Geo.sin(var6) * 4.55F, 1.6F + var8, Cos3Geo.alpha(0.4 * var8, 16773296));
      }

      var0.pop();
   }

   static void tophat(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 8.05F, 0.0F);
      var0.rotZ(-4.0F);
      var0.color(-14935006);
      brim(
         var0, "hd_felt", 4.3F, 2.0F, 7.0F, 0.92F, 0.36F, 32, 3, (var0x, var2) -> 0.18F + var2 * var2 * 0.9F * (float)Math.pow(Math.abs(Math.cos(var0x)), 2.0)
      );
      Cos3Geo.lathe(
         var0,
         "hd_felt",
         new float[]{4.35F, 4.28F, 4.18F, 4.24F, 4.42F, 4.42F, 4.2F, 3.4F, 0.0F},
         new float[]{0.2F, 2.0F, 4.2F, 6.4F, 8.2F, 8.45F, 8.62F, 8.7F, 8.7F},
         28
      );
      var0.color(-4713444);
      Cos3Geo.lathe(var0, "hd_satin", new float[]{4.37F, 4.36F, 4.32F, 4.28F}, new float[]{0.3F, 0.9F, 1.6F, 2.1F}, 28, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.push();
      var0.translate(4.35F, 1.2F, 0.8F);
      var0.rotY(90.0F);
      XmasGeo.bow(var0, -4713444, 0.65F);
      var0.pop();
      var0.pop();
   }

   static void santa(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = var1.preview ? 0.0F : Cos3Geo.clamp(var1.move, 0.0F, 1.0F);
      float var4 = Cos3Geo.sin(var2 * 1.6F) * 0.2F + var3 * 0.25F;
      float[] var5 = new float[]{5.4F, 7.0F, 8.6F, 9.7F};
      float[] var6 = new float[]{4.72F, 4.8F, 4.55F, 3.7F};
      float[] var7 = new float[]{5.5F, 5.5F, 4.2F, 2.8F};
      byte var8 = 10;
      int var9 = var5.length + var8;
      float[][] var10 = new float[var9][];
      float[] var11 = new float[var9];
      float[] var12 = new float[var9];

      for (int var13 = 0; var13 < var5.length; var13++) {
         var10[var13] = p(0.0F, var5[var13], 0.0F);
         var11[var13] = var6[var13];
         var12[var13] = var7[var13];
      }

      float[] var22 = (float[])var10[var5.length - 1].clone();

      for (int var14 = 0; var14 < var8; var14++) {
         float var15 = (float)(var14 + 1) / var8;
         double var16 = var15 * var15 * 2.5 + 0.25;
         double var18 = 1.35 + var4 * var15;
         float var20 = 1.2F - 0.35F * var15;
         var22 = new float[]{
            var22[0] + (float)(Math.sin(var18) * Math.sin(var16)) * var20,
            var22[1] + (float)Math.cos(var16) * var20,
            var22[2] - (float)(Math.cos(var18) * Math.sin(var16)) * var20
         };
         int var21 = var5.length + var14;
         var10[var21] = var22;
         var11[var21] = Math.max(0.1F, 3.4F * (1.0F - var15) + 0.1F);
         var12[var21] = 2.0F;
      }

      var0.color(-3664866);
      XmasGeo.sweep(var0, "hd_velvet", var10, var11, var11, var12, 22, 3.0F);
      var0.color(-460550);
      byte var23 = 40;
      float[][] var25 = new float[var23 + 1][];
      float[] var27 = new float[var23 + 1];
      float[] var17 = new float[var23 + 1];

      for (int var29 = 0; var29 <= var23; var29++) {
         double var19 = (Math.PI * 2) * var29 / var23;
         float[] var30 = Cos3Models.loopPt(2.6F, 2.45F, var19);
         var25[var29] = p(var30[0], 5.7F + 0.08F * Cos3Geo.sin(var19 * 7.0), var30[1]);
         var27[var29] = 0.95F + 0.07F * Cos3Geo.sin(var19 * 11.0);
         var17[var29] = 1.25F + 0.07F * Cos3Geo.cos(var19 * 9.0);
      }

      XmasGeo.sweep(var0, "hd_fluff", var25, var27, var17, null, 10, 8.0F);
      float[] var24 = var10[var9 - 1];
      var0.push();
      var0.translate(var24[0], var24[1] - 0.3F, var24[2]);
      var0.rotZ(Cos3Geo.sin(var2 * 3.0F) * 10.0F);
      Cos3Geo.ball(var0, "hd_fluff", 0.0F, -0.9F, 0.0F, 1.45F, 12);

      for (int var26 = 0; var26 < 8; var26++) {
         double var28 = var26 * 2.4;
         Cos3Geo.ball(var0, "hd_fluff", Cos3Geo.cos(var28) * 1.0F, -0.9F + Cos3Geo.sin(var26 * 1.3) * 0.9F, Cos3Geo.sin(var28) * 1.0F, 0.7F, 6);
      }

      var0.pop();
      var0.color(-1);
   }

   static void witch(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(0.0F, 7.35F, 0.0F);
      var0.rotZ(-5.0F);
      var0.color(-12969382);
      brim(
         var0,
         "hd_felt",
         4.75F,
         5.0F,
         8.6F,
         1.0F,
         0.34F,
         40,
         4,
         (var0x, var2x) -> 0.2F
            + var2x * 0.25F
            + var2x * var2x * 0.55F * Cos3Geo.sin(var0x * 3.0 + 1.0)
            - var2x * var2x * 0.4F * Math.max(0.0F, Cos3Geo.sin(var0x))
      );
      float[][] var3 = bentSpine(0.3F, 10.5F, 14, 70.0, 200.0, 0.45F);
      int var4 = var3.length;
      float[] var5 = new float[var4];
      float[] var6 = new float[var4];

      for (int var7 = 0; var7 < var4; var7++) {
         float var8 = (float)var7 / (var4 - 1);
         var5[var7] = Math.max(0.06F, 4.72F * (float)Math.pow(1.0F - var8, 1.25) + 0.05F);
         var6[var7] = var8 < 0.1F ? 5.0F : 2.0F;
         if (var7 > 0 && var7 < var4 - 1) {
            var3[var7][0] = var3[var7][0] + Cos3Geo.sin(var8 * 9.0F) * 0.12F;
         }
      }

      XmasGeo.sweep(var0, "hd_felt", var3, var5, var5, var6, 22, 3.0F);
      var0.color(-1);
      Geo.sqTube(var0, "hd_witchband", 4.82F, 4.62F, 0.35F, 1.95F, 5.0F, 24, 0.0F, 0.0F, 3.0F, 1.0F);
      var0.push();
      var0.translate(0.0F, 1.15F, 4.86F);
      var0.color(-10134);
      HdGeo.loop(var0, "hd_gold", HdGeo.rrect(1.15F, 0.9F, 0.3F, 3), 0.22F, 0.2F, 3.0F, 6);
      Cos3Geo.cbox(var0, "hd_gold", 0.0F, 0.0F, 0.05F, 0.25F, 1.5F, 0.25F, 0.05F);
      var0.pop();
      float[] var9 = var3[var4 - 1];
      var0.push();
      var0.translate(var9[0], var9[1], var9[2]);
      var0.rotZ(Cos3Geo.sin(var2 * 2.4F) * 14.0F);
      var0.color(-10134);
      Geo.tube(var0, "hd_gold", p(0.0F, 0.0F, 0.0F), p(0.0F, -1.6F, 0.0F), 0.05F, 0.05F, 4);
      var0.translate(0.0F, -2.2F, 0.0F);
      var0.rotY(var2 * 60.0F);
      var0.glow(true);
      XmasGeo.star(var0, "hd_gold", 0.7F, 0.25F, 5);
      var0.glow(false);
      var0.pop();
      var0.pop();
      var0.color(-1);
   }

   static void catEars(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = (float)Math.max(0.0, Math.sin(var2 * 3.1F) - 0.85F) * 55.0F;
      float[][] var4 = earShape(4.6F, 4.9F, 0.4F, 5);
      float[][] var5 = earShape(2.9F, 3.3F, 0.22F, 4);

      for (byte var6 = 1; var6 >= -1; var6 -= 2) {
         var0.push();
         var0.scale(var6, 1.0F, 1.0F);
         var0.translate(2.25F, 7.75F, 0.2F);
         var0.rotZ(-12.0F - (var6 > 0 ? var3 : 0.0F));
         var0.rotY(-12.0F);
         ear(var0, var4, shift(var5, 0.0F, 0.4F), "hd_fur", -10857886, -25930, 1.05F);
         var0.color(-725260);

         for (int var7 = 0; var7 < 3; var7++) {
            Geo.ellipsoid(var0, "hd_fur", -0.35F + var7 * 0.35F, 0.75F + var7 * 0.25F, 0.62F, 0.22F, 0.8F, 0.15F, 5, 3);
         }

         var0.pop();
      }

      var0.color(-1);
   }

   static float[][] shift(float[][] var0, float var1, float var2) {
      float[][] var3 = new float[var0.length][];

      for (int var4 = 0; var4 < var0.length; var4++) {
         var3[var4] = new float[]{var0[var4][0] + var1, var0[var4][1] + var2};
      }

      return var3;
   }

   static void bunnyEars(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = var1.preview ? 0.0F : var1.move;

      for (byte var4 = 1; var4 >= -1; var4 -= 2) {
         var0.push();
         var0.scale(var4, 1.0F, 1.0F);
         var0.translate(1.85F, 7.6F, 0.2F);
         var0.rotZ(-9.0F);
         float var5 = (var4 > 0 ? 55 : 8) + Cos3Geo.sin(var2 * 2.2F + var4) * 6.0F + var3 * 14.0F;
         float[][] var6 = bentSpine(0.0F, 9.4F, 10, var5, var4 > 0 ? 180.0 : 170.0, 0.55F);
         int var7 = var6.length;
         float[] var8 = new float[var7];
         float[] var9 = new float[var7];

         for (int var10 = 0; var10 < var7; var10++) {
            float var11 = (float)var10 / (var7 - 1);
            float var12 = (float)Math.sin(Math.PI * Math.min(1.0F, var11 * 0.92F + 0.08F));
            var8[var10] = Math.max(0.08F, 0.55F + 1.05F * var12);
            var9[var10] = Math.max(0.06F, 0.5F * var12 + 0.18F);
         }

         var0.color(-593164);
         XmasGeo.sweep(var0, "hd_fur", var6, var8, var9, null, 14, 2.0F);
         float[][] var15 = new float[var7][];
         float[] var16 = new float[var7];
         float[] var17 = new float[var7];

         for (int var13 = 0; var13 < var7; var13++) {
            float var14 = (float)var13 / (var7 - 1);
            var15[var13] = new float[]{var6[var13][0], var6[var13][1], var6[var13][2] + var9[var13] * 0.72F};
            var16[var13] = Math.max(0.05F, var8[var13] * 0.55F * (float)Math.sin(Math.PI * Math.min(1.0F, 0.1F + var14 * 0.9F)));
            var17[var13] = 0.12F;
         }

         var0.color(-24900);
         XmasGeo.sweep(var0, "hd_velvet", frontOf(var6, var9, 0.72F), var16, var17, null, 10, 1.0F);
         var0.pop();
      }

      var0.color(-1);
   }

   static float[][] frontOf(float[][] var0, float[] var1, float var2) {
      int var3 = var0.length;
      float[][] var4 = new float[var3][];

      for (int var5 = 0; var5 < var3; var5++) {
         float[] var6 = var0[Math.max(0, var5 - 1)];
         float[] var7 = var0[Math.min(var3 - 1, var5 + 1)];
         float var8 = var7[0] - var6[0];
         float var9 = var7[1] - var6[1];
         float var10 = var7[2] - var6[2];
         float var11 = (float)Math.sqrt(var8 * var8 + var9 * var9 + var10 * var10);
         if (var11 < 1.0E-5F) {
            var4[var5] = var0[var5];
         } else {
            var8 /= var11;
            var9 /= var11;
            var10 /= var11;
            float var13 = -var10 * var8;
            float var14 = -var10 * var9;
            float var15 = 1.0F - var10 * var10;
            float var16 = (float)Math.sqrt(var13 * var13 + var14 * var14 + var15 * var15);
            if (var16 < 1.0E-4F) {
               var13 = 0.0F;
               var14 = 1.0F;
               var15 = 0.0F;
               var16 = 1.0F;
            }

            var4[var5] = new float[]{
               var0[var5][0] + var13 / var16 * var1[var5] * var2,
               var0[var5][1] + var14 / var16 * var1[var5] * var2,
               var0[var5][2] + var15 / var16 * var1[var5] * var2
            };
         }
      }

      return var4;
   }

   static void horns(G var0, Cos.A var1) {
      float var2 = var1.time;

      for (byte var3 = 1; var3 >= -1; var3 -= 2) {
         var0.push();
         var0.scale(var3, 1.0F, 1.0F);
         var0.translate(2.5F, 7.75F, 1.0F);
         var0.rotZ(-26.0F);
         var0.rotY(-8.0F);
         var0.color(-1);
         HdGeo.horn(var0, "hd_horn", 1.15F, 6.4F, -95.0F, 30.0F, 16, 12, 7.0F);
         var0.pop();
      }
   }

   static void headphones(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = (float)Math.pow(Math.max(0.0, Math.sin(var2 * 7.5)), 6.0);
      byte var4 = 18;
      float[][] var5 = new float[var4 + 1][];
      float[][] var6 = new float[var4 - 5][];

      for (int var7 = 0; var7 <= var4; var7++) {
         double var8 = Math.PI * var7 / var4;
         float[] var10 = Geo.sq(var8, 1.0F, 3.2F);
         var5[var7] = p(var10[0] * 5.4F, 3.6F + var10[1] * 5.75F, 0.0F);
      }

      float[] var13 = new float[var4 + 1];
      float[] var14 = new float[var4 + 1];
      Arrays.fill(var13, 0.38F);
      Arrays.fill(var14, 0.95F);
      var0.color(-14539732);
      XmasGeo.sweep(var0, "hd_rubber", var5, var13, var14, null, 10, 1.0F);

      for (int var9 = 3; var9 <= var4 - 3; var9++) {
         double var16 = Math.PI * var9 / var4;
         float[] var12 = Geo.sq(var16, 1.0F, 3.2F);
         var6[var9 - 3] = p(var12[0] * 4.95F, 3.6F + var12[1] * 5.3F, 0.0F);
      }

      float[] var15 = new float[var6.length];
      float[] var17 = new float[var6.length];
      Arrays.fill(var15, 0.34F);
      Arrays.fill(var17, 0.75F);
      var0.color(-12960698);
      XmasGeo.sweep(var0, "hd_leather", var6, var15, var17, null, 10, 2.0F);

      for (byte var11 = 1; var11 >= -1; var11 -= 2) {
         var0.push();
         var0.translate(var11 * 4.05F, 3.3F, 0.0F);
         var0.rotZ(var11 * -90);
         var0.color(-4669752);
         Cos3Geo.cbox(var0, "hd_chrome", 0.0F, 1.2F, 0.0F, 0.5F, 0.3F, 0.3F, 0.08F);
         var0.color(-13750216);
         var0.push();
         var0.translate(0.0F, 0.35F, 0.0F);
         var0.torus("hd_leather", 2.05F, 0.5F, 22, 8, 0.0F, 0.0F, 3.0F, 1.0F);
         var0.pop();
         var0.color(-15066335);
         Cos3Geo.lathe(var0, "hd_plastic", new float[]{2.6F, 2.75F, 2.72F, 2.5F, 2.0F, 0.0F}, new float[]{0.6F, 0.9F, 1.6F, 2.05F, 2.25F, 2.3F}, 24);
         var0.color(-2564376);
         var0.push();
         var0.translate(0.0F, 1.95F, 0.0F);
         var0.torus("hd_chrome", 2.35F, 0.14F, 24, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
         var0.glow(true).color(0xFF000000 | Capes.mix(1353800, 8191912, var3));
         var0.push();
         var0.translate(0.0F, 2.28F, 0.0F);
         var0.torus("white", 1.35F, 0.13F, 20, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
         var0.glow(false);
         Cos3Geo.halo(var0, 0.0F, 2.5F, 0.0F, 3.2F + var3 * 1.2F, Cos3Geo.alpha(0.2 + 0.25 * var3, 3866490));
         var0.pop();
      }

      var0.color(-1);
   }

   static void foxEars(G var0, Cos.A var1) {
      float var2 = (float)Math.max(0.0, Math.sin(var1.time * 2.7F) - 0.8F) * 60.0F;
      float[][] var3 = earShape(4.3F, 5.3F, 0.3F, 6);
      float[][] var4 = earShape(2.6F, 3.4F, 0.15F, 4);

      for (byte var5 = 1; var5 >= -1; var5 -= 2) {
         var0.push();
         var0.scale(var5, 1.0F, 1.0F);
         var0.translate(2.35F, 7.7F, 0.1F);
         var0.rotZ(-15.0F - (var5 > 0 ? var2 : 0.0F));
         var0.rotY(-14.0F);
         var0.rotX(-6.0F);
         var0.color(-1);
         HdGeo.puff(var0, "hd_foxear", var3, 1.05F, 0.45F, 2, new float[]{0.0F, 0.0F, 1.0F, 1.0F}, new float[]{0.4F, 0.6F, 0.6F, 0.7F});
         var0.color(-594202);
         var0.push();
         var0.translate(0.0F, 0.4F, 0.45F);
         HdGeo.puff(var0, "hd_fur", var4, 0.45F, 0.2F, 1);

         for (int var6 = 0; var6 < 4; var6++) {
            Geo.ellipsoid(var0, "hd_fur", -0.5F + var6 * 0.33F, 0.9F + var6 % 2 * 0.3F, 0.28F, 0.2F, 0.95F, 0.14F, 5, 3);
         }

         var0.pop();
         var0.pop();
      }

      var0.color(-1);
   }

   static void cowboy(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 6.9F, 0.0F);
      var0.rotZ(-4.0F);
      var0.color(-6659530);
      brim(var0, "hd_leather", 4.75F, 5.0F, 8.7F, 1.08F, 0.38F, 36, 4, (var0x, var2x) -> {
         float var3x = (float)Math.pow(Math.abs(Math.cos(var0x)), 1.6);
         float var4x = (float)Math.max(0.0, Math.sin(var0x));
         return 0.45F + var2x * var2x * (2.6F * var3x - 0.7F * var4x * (1.0F - var3x) - 0.3F * (float)Math.max(0.0, -Math.sin(var0x)) * (1.0F - var3x));
      });
      byte var2 = 26;
      byte var3 = 7;
      float[][][] var4 = new float[var3 + 1][var2 + 1][];

      for (int var5 = 0; var5 <= var3; var5++) {
         float var6 = (float)var5 / var3;

         for (int var7 = 0; var7 <= var2; var7++) {
            double var8 = (Math.PI * 2) * var7 / var2;
            float var10 = 4.72F - 0.55F * var6 - (var6 > 0.55F ? 1.6F * (float)Math.pow((var6 - 0.55F) / 0.45F, 2.0) : 0.0F);
            float var11 = var6 > 0.5F ? 0.55F * (float)Math.pow(Math.max(0.0, Math.sin(var8)), 6.0) * (var6 - 0.5F) * 2.0F : 0.0F;
            float[] var12 = Geo.sq(var8, var10 - var11, 5.0F - var6 * 2.0F);
            float var13 = 0.3F + 5.4F * (float)Math.sin(var6 * Math.PI / 2.0 * 0.95);
            float var14 = var6 > 0.85F ? -0.9F * (float)Math.pow(Math.max(0.0, Math.cos(var8) * Math.cos(var8)), 0.5) * (var6 - 0.85F) / 0.15F : 0.0F;
            var4[var5][var7] = p(var12[0], var13 + var14, var12[1]);
         }
      }

      for (int var15 = 0; var15 < var3; var15++) {
         for (int var17 = 0; var17 < var2; var17++) {
            var0.quad(
               "hd_leather",
               var4[var15 + 1][var17],
               var4[var15 + 1][var17 + 1],
               var4[var15][var17 + 1],
               var4[var15][var17],
               new float[]{
                  (float)var17 / var2,
                  1.0F - (var15 + 1.0F) / var3,
                  (var17 + 1.0F) / var2,
                  1.0F - (var15 + 1.0F) / var3,
                  (var17 + 1.0F) / var2,
                  1.0F - (float)var15 / var3,
                  (float)var17 / var2,
                  1.0F - (float)var15 / var3
               }
            );
         }
      }

      float[] var16 = p(0.0F, var4[var3][0][1] - 0.6F, 0.0F);

      for (int var18 = 0; var18 < var2; var18++) {
         var0.quad(
            "hd_leather", var16, var4[var3][var18 + 1], var4[var3][var18], var4[var3][var18], new float[]{0.5F, 0.5F, 0.5F, 0.0F, 0.5F, 0.0F, 0.5F, 0.0F}
         );
      }

      var0.color(-12966892);
      Geo.sqTube(var0, "hd_leather", 4.79F, 4.64F, 0.55F, 1.55F, 5.0F, 26, 0.0F, 0.0F, 2.0F, 1.0F);
      var0.color(-1);
      var0.push();
      var0.translate(0.0F, 1.05F, 4.82F);
      var0.color(-2564376);
      var0.rotX(90.0F);
      Cos3Geo.lathe(var0, "hd_silver", new float[]{0.0F, 0.55F, 0.72F, 0.7F, 0.0F}, new float[]{0.36F, 0.3F, 0.12F, 0.0F, 0.0F}, 14);
      var0.pop();
      var0.pop();
      var0.color(-1);
   }

   static void beanie(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = var1.preview ? 0.0F : var1.move;
      var0.push();
      var0.translate(0.0F, 5.0F, 0.0F);
      var0.color(-13737256);
      Geo.sqDome(var0, "hd_knit", 4.72F, 4.3F, 1.9F, 5.0F, 26, 6, 0.0F, 0.0F, 3.0F, 1.0F);
      Geo.sqTube(var0, "hd_knit", 4.72F, 4.72F, 0.3F, 1.9F, 5.0F, 26, 0.0F, 0.0F, 3.0F, 0.3F);
      var0.color(-855306);
      Geo.sqTube(var0, "hd_knit", 4.76F, 4.62F, 3.0F, 3.75F, 4.6F, 26, 0.0F, 0.0F, 3.0F, 0.2F);
      var0.color(-14396744);

      for (int var4 = 0; var4 < 3; var4++) {
         float var5 = 5.02F - Math.abs(var4 - 1) * 0.12F;
         Geo.sqTube(
            var0,
            "hd_knit",
            var5,
            var4 == 2 ? 4.8F : 5.02F - Math.abs(var4) * 0.12F,
            -0.25F + var4 * 0.8F,
            0.55F + var4 * 0.8F,
            5.0F,
            26,
            0.0F,
            0.0F,
            4.0F,
            0.3F
         );
      }

      Geo.sqTube(var0, "hd_knit", 4.8F, 4.8F, -0.25F, -0.249F, 5.0F, 26, 0.0F, 0.0F, 1.0F, 1.0F);
      float var10 = Cos3Geo.sin(var2 * 3.0F) * 6.0F + var3 * 10.0F;
      var0.translate(0.0F, 6.0F, 0.0F);
      var0.rotZ(var10);
      var0.rotX(Cos3Geo.sin(var2 * 2.2) * 5.0F);
      var0.translate(0.0F, 1.15F, 0.0F);
      var0.color(-592134);
      Cos3Geo.ball(var0, "hd_fluff", 0.0F, 0.0F, 0.0F, 1.75F, 14);

      for (int var11 = 0; var11 < 10; var11++) {
         double var6 = var11 * 2.4;
         double var8 = (var11 % 3 - 1) * 0.7;
         Cos3Geo.ball(
            var0,
            "hd_fluff",
            Cos3Geo.cos(var6) * Cos3Geo.cos(var8) * 1.25F,
            Cos3Geo.sin(var8) * 1.25F + 0.2F,
            Cos3Geo.sin(var6) * Cos3Geo.cos(var8) * 1.25F,
            0.75F,
            6
         );
      }

      var0.pop();
      var0.color(-1);
   }

   static void flower(G var0, int var1, float var2, int var3, int var4, float var5) {
      int var6 = var0.color;
      float[][] var7 = new float[][]{
         {0.0F, 0.0F}, {var2 * 0.4F, var2 * 0.45F}, {var2 * 0.28F, var2 * 0.9F}, {0.0F, var2}, {-var2 * 0.28F, var2 * 0.9F}, {-var2 * 0.4F, var2 * 0.45F}
      };
      var0.color(var3);

      for (int var8 = 0; var8 < var1; var8++) {
         var0.push();
         var0.rotY(360.0F / var1 * var8);
         var0.rotX(-90.0F + var5);
         HdGeo.puff(var0, "hd_petal", var7, 0.16F, 0.07F, 1, new float[]{0.0F, 0.0F, 1.0F, 1.0F}, new float[]{0.4F, 0.4F, 0.6F, 0.6F});
         var0.pop();
      }

      var0.color(var4);
      Cos3Geo.ball(var0, "hd_gem", 0.0F, 0.1F, 0.0F, var2 * 0.28F, 6);
      var0.color(var6);
   }

   static void leaf(G var0, float var1, int var2) {
      float var3 = var1 * 0.42F;
      float[][] var4 = new float[][]{
         {0.0F, 0.0F},
         {var1 * 0.25F, var3 * 0.8F},
         {var1 * 0.55F, var3},
         {var1 * 0.82F, var3 * 0.6F},
         {var1, 0.0F},
         {var1 * 0.82F, -var3 * 0.6F},
         {var1 * 0.55F, -var3},
         {var1 * 0.25F, -var3 * 0.8F}
      };
      int var5 = var0.color;
      var0.color(var2);
      HdGeo.puff(var0, "hd_leafw", var4, 0.14F, 0.06F, 1, new float[]{0.0F, 0.0F, 1.0F, 1.0F}, new float[]{0.4F, 0.4F, 0.6F, 0.6F});
      var0.color(var5);
   }

   static void flowerCrown(G var0, Cos.A var1) {
      float var2 = var1.time;
      byte var3 = 32;
      float[][] var4 = new float[var3 + 1][];

      for (int var5 = 0; var5 <= var3; var5++) {
         double var6 = (Math.PI * 2) * var5 / var3;
         float[] var8 = Cos3Models.loopPt(2.0F, 3.25F, var6);
         var4[var5] = p(var8[0], 8.0F + 0.18F * Cos3Geo.sin(var6 * 5.0), var8[1]);
      }

      float[] var17 = new float[var3 + 1];
      Arrays.fill(var17, 0.34F);
      var0.color(-1);
      XmasGeo.sweep(var0, "hd_vine", var4, var17, var17, null, 6, 6.0F);
      int[] var18 = new int[]{-30024, -1, -10166, -7685889, -1531649};
      byte var7 = 7;

      for (int var19 = 0; var19 < var7; var19++) {
         double var9 = (Math.PI * 2) * var19 / var7 + 0.3;
         float[] var11 = Cos3Models.loopPt(2.0F, 3.45F, var9);
         var0.push();
         var0.translate(var11[0], 8.35F, var11[1]);
         var0.rotY((float)(-Math.toDegrees(var9)) + 90.0F);
         var0.rotX(55.0F + Cos3Geo.sin(var2 * 1.8F + var19) * 4.0F);
         float var12 = 1.5F + 0.35F * (var19 * 5 % 3) / 2.0F;
         flower(var0, var19 % 3 == 1 ? 6 : 5, var12, var18[var19 % var18.length], var19 % 5 == 2 ? -30166 : -12246, 22.0F);
         var0.pop();

         for (byte var13 = -1; var13 <= 1; var13 += 2) {
            if (var13 <= 0 && var19 % 2 != 1) {
               double var14 = var9 + var13 * 0.24;
               float[] var16 = Cos3Models.loopPt(2.0F, 3.3F, var14);
               var0.push();
               var0.translate(var16[0], 8.1F, var16[1]);
               var0.rotY((float)(-Math.toDegrees(var14)) + 90.0F + (var13 > 0 ? 180 : 0) + 90.0F);
               var0.rotZ(20.0F);
               var0.rotX(40.0F);
               leaf(var0, 1.5F, -11884478);
               var0.pop();
            }
         }
      }

      float var20 = Cos3Geo.fract(var2 * 0.12F);
      if (var20 < 0.6F) {
         double var21 = var20 * 4.0F;
         var0.push();
         var0.translate(Cos3Geo.cos(var21) * 6.5F, 11.0F + Cos3Geo.sin(var2 * 3.0F) * 0.6F, Cos3Geo.sin(var21) * 6.5F);
         var0.rotY((float)(-Math.toDegrees(var21)));
         float var22 = 30.0F + 45.0F * Math.abs(Cos3Geo.sin(var2 * 14.0F));

         for (byte var23 = -1; var23 <= 1; var23 += 2) {
            var0.push();
            var0.rotZ(var23 * var22);
            var0.color(-20438);
            var0.push();
            var0.translate(var23 * 0.7F, 0.0F, 0.0F);
            var0.rotX(90.0F);
            HdGeo.puff(var0, "hd_petal", HdGeo.circle(0.7F, 0.5F, 10), 0.06F, 0.02F, 1);
            var0.pop();
            var0.pop();
         }

         var0.color(-14018032);
         Geo.ellipsoid(var0, "hd_plastic", 0.0F, 0.0F, 0.0F, 0.12F, 0.12F, 0.55F, 5, 3);
         var0.pop();
      }

      var0.color(-1);
   }

   static void viking(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 6.1F, 0.0F);
      var0.color(-4669752);
      Geo.sqDome(var0, "hd_steel", 4.82F, 4.3F, 0.8F, 5.0F, 26, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      Geo.sqTube(var0, "hd_steel", 4.82F, 4.82F, -0.2F, 0.8F, 5.0F, 26, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-2581942);
      Geo.sqTube(var0, "hd_gold", 5.0F, 5.0F, -0.55F, 0.95F, 5.0F, 26, 0.0F, 0.0F, 1.0F, 1.0F);
      Geo.sqTube(var0, "hd_gold", 4.86F, 5.0F, -0.55F, -0.54F, 5.0F, 26, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int var2 = 0; var2 < 2; var2++) {
         var0.push();
         var0.rotY(var2 * 90);
         byte var3 = 12;
         float[][] var4 = new float[var3 + 1][];

         for (int var5 = 0; var5 <= var3; var5++) {
            double var6 = Math.PI * var5 / var3;
            float[] var8 = Geo.sq(var6, 1.0F, 2.6F);
            var4[var5] = p(var8[0] * 4.95F, 0.8F + var8[1] * 4.42F, 0.0F);
         }

         float[] var13 = new float[var3 + 1];
         float[] var14 = new float[var3 + 1];
         Arrays.fill(var13, 0.18F);
         Arrays.fill(var14, 0.55F);
         XmasGeo.sweep(var0, "hd_gold", var4, var13, var14, null, 6, 1.0F);
         var0.pop();
      }

      var0.color(-1523584);

      for (int var9 = 0; var9 < 16; var9++) {
         float[] var11 = Geo.sq((Math.PI * 2) * var9 / 16.0 + 0.1, 5.08F, 5.0F);
         Cos3Geo.ball(var0, "hd_gold", var11[0], 0.2F, var11[1], 0.2F, 6);
      }

      var0.color(-2581942);
      float[][] var10 = new float[][]{{-0.55F, 0.4F}, {0.55F, 0.4F}, {0.42F, -2.2F}, {0.0F, -2.6F}, {-0.42F, -2.2F}};
      var0.push();
      var0.translate(0.0F, 0.0F, 5.05F);
      HdGeo.puff(var0, "hd_gold", var10, 0.4F, 0.15F, 2);
      var0.pop();
      var0.color(-1);

      for (byte var12 = 1; var12 >= -1; var12 -= 2) {
         var0.push();
         var0.scale(var12, 1.0F, 1.0F);
         var0.translate(4.8F, 1.9F, 0.2F);
         var0.rotZ(-78.0F);
         var0.rotY(-22.0F);
         HdGeo.horn(var0, "hd_ivory", 1.2F, 7.0F, -62.0F, -20.0F, 14, 12, 5.0F);
         var0.color(-2581942);
         var0.push();
         var0.translate(0.0F, 0.35F, 0.0F);
         var0.torus("hd_gold", 1.18F, 0.22F, 14, 5, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
         var0.color(-1);
         var0.pop();
      }

      var0.pop();
   }

   static void propeller(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(0.0F, 6.2F, 0.0F);
      int[] var3 = new int[]{-1900533, -13053, -16749385, -16736198, -1900533, -13053};

      for (int var4 = 0; var4 < 6; var4++) {
         var0.color(var3[var4]);
         domeSector(var0, "hd_plastic", 4.86F, 3.5F, 5.0F, var4 * Math.PI / 3.0, (var4 + 1) * Math.PI / 3.0, 6, 6);
      }

      var0.color(-855310);
      Geo.sqTube(var0, "hd_plastic", 4.9F, 4.9F, -0.1F, 0.5F, 5.0F, 24, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-14787896);
      var0.push();
      var0.translate(0.0F, 0.25F, 0.0F);
      byte var19 = 12;

      for (int var5 = 0; var5 < var19; var5++) {
         double var6 = Math.PI * (0.18 + 0.64 * var5 / var19);
         double var8 = Math.PI * (0.18 + 0.64 * (var5 + 1) / var19);
         float[] var10 = Geo.sq(var6, 4.9F, 5.0F);
         float[] var11 = Geo.sq(var8, 4.9F, 5.0F);
         float var12 = 8.2F - 2.2F * (float)Math.pow(Math.abs(Math.cos(var6)), 2.0);
         float var13 = 8.2F - 2.2F * (float)Math.pow(Math.abs(Math.cos(var8)), 2.0);
         float[] var14 = p(var10[0], 0.0F, var10[1]);
         float[] var15 = p(var11[0], 0.0F, var11[1]);
         float[] var16 = p(Cos3Geo.cos(var8) * var13, -0.7F, Cos3Geo.sin(var8) * var13);
         float[] var17 = p(Cos3Geo.cos(var6) * var12, -0.7F, Cos3Geo.sin(var6) * var12);
         float var18 = 0.18F;
         var0.quad(
            "hd_plastic", up(var17, var18), up(var16, var18), up(var15, var18), up(var14, var18), new float[]{0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F}
         );
         var0.quad("hd_plastic", var14, var15, var16, var17, new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});
         var0.quad("hd_plastic", var17, var16, up(var16, var18), up(var17, var18), new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});
      }

      var0.pop();
      var0.color(-13053);
      var0.translate(0.0F, 3.45F, 0.0F);
      Cos3Geo.lathe(var0, "hd_plastic", new float[]{0.7F, 0.42F, 0.35F, 0.35F}, new float[]{0.0F, 0.3F, 0.5F, 1.4F}, 10);
      var0.translate(0.0F, 1.4F, 0.0F);
      var0.color(-1900533);
      Cos3Geo.ball(var0, "hd_gloss", 0.0F, 0.1F, 0.0F, 0.62F, 10);
      var0.rotY(var2 * (var1.preview ? 260.0F : 720.0F + var1.move * 900.0F));
      int[] var20 = new int[]{-1900533, -16749385, -16736198};
      float[][] var21 = new float[][]{
         {0.45F, -0.35F}, {2.6F, -0.9F}, {4.7F, -0.85F}, {5.4F, -0.3F}, {5.4F, 0.35F}, {4.7F, 0.85F}, {2.6F, 0.75F}, {0.45F, 0.35F}
      };

      for (int var7 = 0; var7 < 3; var7++) {
         var0.push();
         var0.rotY(var7 * 120);
         var0.rotX(74.0F);
         var0.color(var20[var7]);
         HdGeo.puff(var0, "hd_plastic", var21, 0.2F, 0.08F, 1);
         var0.pop();
      }

      var0.pop();
      var0.color(-1);
   }

   private static float[] up(float[] var0, float var1) {
      return p(var0[0], var0[1] + var1, var0[2]);
   }

   static void domeSector(G var0, String var1, float var2, float var3, float var4, double var5, double var7, int var9, int var10) {
      for (int var11 = 0; var11 < var10; var11++) {
         double var12 = (Math.PI / 2) * var11 / var10;
         double var14 = (Math.PI / 2) * (var11 + 1) / var10;
         float var16 = var2 * Cos3Geo.cos(var12);
         float var17 = var2 * Cos3Geo.cos(var14);
         float var18 = var3 * Cos3Geo.sin(var12);
         float var19 = var3 * Cos3Geo.sin(var14);
         float var20 = Geo.lerp(var4, 2.0F, (float)var11 / var10);
         float var21 = Geo.lerp(var4, 2.0F, (float)(var11 + 1) / var10);

         for (int var22 = 0; var22 < var9; var22++) {
            double var23 = var5 + (var7 - var5) * var22 / var9;
            double var25 = var5 + (var7 - var5) * (var22 + 1) / var9;
            float[] var27 = Geo.sq(var23, var16, var20);
            float[] var28 = Geo.sq(var25, var16, var20);
            float[] var29 = Geo.sq(var23, var17, var21);
            float[] var30 = Geo.sq(var25, var17, var21);
            var0.quad(
               var1,
               p(var29[0], var19, var29[1]),
               p(var30[0], var19, var30[1]),
               p(var28[0], var18, var28[1]),
               p(var27[0], var18, var27[1]),
               new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
            );
         }
      }
   }

   static void chef(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(0.0F, 6.4F, 0.0F);
      byte var3 = 64;
      byte var4 = 3;

      for (int var5 = 0; var5 < var4; var5++) {
         float var6 = 0.8F + 4.6F * var5 / var4;
         float var7 = 0.8F + 4.6F * (var5 + 1) / var4;

         for (int var8 = 0; var8 < var3; var8++) {
            double var9 = (Math.PI * 2) * var8 / var3;
            double var11 = (Math.PI * 2) * (var8 + 1) / var3;
            float var13 = 0.32F * (float)Math.abs(Math.sin(var9 * 8.0));
            float var14 = 0.32F * (float)Math.abs(Math.sin(var11 * 8.0));
            float var15 = 0.1F * var5 / var4;
            float var16 = 0.1F * (var5 + 1) / var4;
            float[] var17 = Geo.sq(var9, 4.8F + var13 + var15, 5.0F);
            float[] var18 = Geo.sq(var11, 4.8F + var14 + var15, 5.0F);
            float[] var19 = Geo.sq(var9, 4.8F + var13 + var16, 5.0F);
            float[] var20 = Geo.sq(var11, 4.8F + var14 + var16, 5.0F);
            var0.color(-460550);
            var0.quad(
               "hd_cotton",
               p(var19[0], var7, var19[1]),
               p(var20[0], var7, var20[1]),
               p(var18[0], var6, var18[1]),
               p(var17[0], var6, var17[1]),
               new float[]{0.0F, 0.0F, 0.1F, 0.0F, 0.1F, 1.0F, 0.0F, 1.0F}
            );
         }
      }

      var0.color(-1513234);
      Geo.sqTube(var0, "hd_cotton", 5.0F, 5.0F, -0.2F, 1.3F, 5.0F, 26, 0.0F, 0.0F, 3.0F, 0.3F);
      float var21 = 1.0F + Cos3Geo.sin(var2 * 1.5F) * 0.03F;
      var0.color(-1);

      for (int var22 = 0; var22 < 7; var22++) {
         double var23 = (Math.PI * 2) * var22 / 7.0 + 0.3;
         Geo.ellipsoid(var0, "hd_cotton", Cos3Geo.cos(var23) * 3.1F, 6.4F, Cos3Geo.sin(var23) * 3.1F, 2.6F * var21, 2.2F * var21, 2.6F * var21, 14, 9);
      }

      Geo.ellipsoid(var0, "hd_cotton", 0.0F, 7.35F, 0.0F, 3.4F * var21, 2.5F * var21, 3.4F * var21, 16, 10);
      var0.pop();
   }

   static void party(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(1.1F, 7.95F, 0.2F);
      var0.rotZ(-13.0F + Cos3Geo.sin(var2 * 2.2F) * 4.0F);
      var0.color(-1);
      Cos3Geo.lathe(
         var0, "hd_party", new float[]{3.35F, 3.1F, 2.4F, 1.5F, 0.7F, 0.12F}, new float[]{0.1F, 0.9F, 3.0F, 5.4F, 7.6F, 9.1F}, 24, 0.0F, 1.0F, 2.0F, 0.0F
      );
      var0.color(-7606);
      byte var3 = 40;
      float[][] var4 = new float[var3 + 1][];

      for (int var5 = 0; var5 <= var3; var5++) {
         double var6 = (Math.PI * 2) * var5 / var3;
         var4[var5] = p(Cos3Geo.cos(var6) * 3.4F, 0.25F + 0.22F * Cos3Geo.sin(var6 * 10.0), Cos3Geo.sin(var6) * 3.4F);
      }

      float[] var13 = new float[var3 + 1];
      Arrays.fill(var13, 0.42F);
      XmasGeo.sweep(var0, "hd_satin", var4, var13, var13, null, 8, 8.0F);
      var0.translate(0.0F, 9.3F, 0.0F);
      var0.color(-46438);
      Cos3Geo.ball(var0, "hd_fluff", 0.0F, 0.0F, 0.0F, 0.95F, 10);
      int[] var14 = new int[]{-46438, -12924673, -7606, -9764998, -5215489};

      for (int var7 = 0; var7 < 12; var7++) {
         var0.push();
         var0.rotY(var7 * 97);
         var0.rotZ(20 + var7 % 4 * 22 + Cos3Geo.sin(var2 * 3.0F + var7) * 5.0F);
         var0.color(var14[var7 % 5]);
         Cos3Geo.cbox(var0, "hd_satin", 0.0F, 1.2F, 0.0F, 0.35F, 1.8F, 0.06F, 0.02F);
         var0.pop();
      }

      var0.pop();
      int[] var15 = new int[]{-50550, -12924673, -7606, -9764998, -5215489};

      for (int var8 = 0; var8 < 14; var8++) {
         float var9 = Cos3Geo.fract(var2 * 0.4F + var8 / 14.0F);
         double var10 = var8 * 2.4 + var2 * 0.5;
         float var12 = Math.min(1.0F, Cos3Geo.sin(var9 * Math.PI) * 2.2F);
         if (!(var12 <= 0.05F)) {
            var0.push();
            var0.translate(Cos3Geo.cos(var10) * (4.5F + var8 % 3), 20.0F - var9 * 15.0F, Cos3Geo.sin(var10) * (4.5F + var8 % 3));
            var0.rotY(var2 * 200.0F + var8 * 40);
            var0.rotX(var2 * 150.0F + var8 * 70);
            var0.scale(var12);
            var0.color(var15[var8 % 5]);
            if (var8 % 3 == 0) {
               Geo.ellipsoid(var0, "hd_plastic", 0.0F, 0.0F, 0.0F, 0.3F, 0.3F, 0.06F, 6, 3);
            } else {
               Cos3Geo.cbox(var0, "hd_plastic", 0.0F, 0.0F, 0.0F, 0.8F, 0.45F, 0.06F, 0.02F);
            }

            var0.pop();
         }
      }

      var0.color(-1);
   }

   static void wizard(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(0.0F, 7.3F, 0.0F);
      var0.rotZ(-5.0F);
      var0.color(-12965200);
      brim(var0, "hd_felt", 4.75F, 5.0F, 9.2F, 1.0F, 0.34F, 40, 4, (var0x, var2x) -> 0.2F + var2x * var2x * (0.5F * Cos3Geo.sin(var0x * 3.0 + 1.0) - 0.35F));
      var0.color(-1);
      float var3 = Cos3Geo.sin(var2 * 1.4F) * 10.0F;
      float[][] var4 = bentSpine(0.2F, 13.5F, 16, 95.0F + var3, 200.0F + var3, 0.4F);
      int var5 = var4.length;
      float[] var6 = new float[var5];
      float[] var7 = new float[var5];

      for (int var8 = 0; var8 < var5; var8++) {
         float var9 = (float)var8 / (var5 - 1);
         var6[var8] = Math.max(0.06F, 4.74F * (float)Math.pow(1.0F - var9, 1.3) + 0.05F);
         var7[var8] = var9 < 0.1F ? 5.0F : 2.0F;
      }

      XmasGeo.sweep(var0, "hd_wizard", var4, var6, var6, var7, 24, 2.0F);
      var0.color(-10134);
      Geo.sqTube(var0, "hd_gold", 4.83F, 4.6F, 0.35F, 1.7F, 5.0F, 24, 0.0F, 0.0F, 3.0F, 1.0F);
      var0.push();
      var0.translate(0.0F, 1.05F, 4.9F);
      float[][] var15 = new float[20][];

      for (int var16 = 0; var16 < 10; var16++) {
         double var10 = (Math.PI / 5) + (Math.PI * 8.0 / 5.0) * var16 / 9.0;
         var15[var16] = new float[]{Cos3Geo.cos(var10 + (Math.PI / 2)) * 1.2F, Cos3Geo.sin(var10 + (Math.PI / 2)) * 1.2F};
         var15[19 - var16] = new float[]{
            Cos3Geo.cos(var10 + (Math.PI / 2)) * 0.9F + 0.45F * (float)Math.sin(var10) * 0.0F, Cos3Geo.sin(var10 + (Math.PI / 2)) * 0.9F + 0.1F
         };
      }

      for (int var17 = 0; var17 < 10; var17++) {
         var15[19 - var17][0] = var15[19 - var17][0] + 0.45F;
      }

      var0.glow(true);
      HdGeo.puff(var0, "hd_gold", var15, 0.3F, 0.1F, 1);
      var0.glow(false);
      var0.pop();
      float[] var18 = var4[var5 - 1];
      var0.push();
      var0.translate(var18[0], var18[1] + 0.2F, var18[2]);
      var0.rotY(var2 * 70.0F);
      var0.glow(true).color(-7558);
      XmasGeo.star(var0, "hd_gold", 1.0F, 0.3F, 5);
      var0.glow(false);
      Cos3Geo.halo(var0, 0.0F, 0.0F, 0.0F, 3.6F, Cos3Geo.alpha(0.5 + 0.2 * Cos3Geo.sin(var2 * 4.0F), 16769146));
      var0.pop();
      var0.pop();

      for (int var19 = 0; var19 < 6; var19++) {
         float var11 = Cos3Geo.fract(var2 * 0.35F + var19 / 6.0F);
         double var12 = var19 * 1.05 + var2 * 0.9;
         float var14 = Cos3Geo.sin(var11 * Math.PI);
         Cos3Models.sparkle(
            var0,
            Cos3Geo.cos(var12) * (6.5F + var11 * 2.0F),
            9.0F + var11 * 9.0F,
            Cos3Geo.sin(var12) * (6.5F + var11 * 2.0F),
            0.5F * var14,
            Cos3Geo.alpha(var14, 16773296)
         );
      }

      var0.color(-1);
   }

   static void alien(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = var1.preview ? 0.0F : var1.move;
      byte var4 = 20;
      float[][] var5 = new float[var4 + 1][];

      for (int var6 = 0; var6 <= var4; var6++) {
         var5[var6] = XmasCos.bandPt(Math.PI * var6 / var4, 5.0F, 5.55F, 3.5F);
      }

      float[] var16 = new float[var4 + 1];
      float[] var7 = new float[var4 + 1];
      Arrays.fill(var16, 0.3F);
      Arrays.fill(var7, 0.62F);
      var0.color(-13989318);
      XmasGeo.sweep(var0, "hd_plastic", var5, var16, var7, null, 8, 1.0F);

      for (byte var8 = 1; var8 >= -1; var8 -= 2) {
         float[] var9 = XmasCos.bandPt(var8 > 0 ? 1.0053096491487339 : 2.1362830044410597, 5.0F, 5.55F, 3.5F);
         byte var10 = 10;
         float[][] var11 = new float[var10 + 1][];
         float[] var12 = new float[var10 + 1];

         for (int var13 = 0; var13 <= var10; var13++) {
            float var14 = (float)var13 / var10;
            float var15 = Cos3Geo.sin(var2 * 3.2F + var8 + var14 * 1.5F) * 1.3F * var14 * var14 + var3 * var14 * var14 * 1.5F;
            var11[var13] = p(var9[0] + var8 * var14 * 2.2F + var15 * 0.5F, var9[1] + var14 * 6.8F, var9[2] + var15 * 0.8F - var14 * var14 * 0.9F);
            var12[var13] = 0.3F - var14 * 0.12F;
         }

         var0.color(-11870630);
         Cos3Geo.ball(var0, "hd_plastic", var9[0], var9[1] + 0.1F, var9[2], 0.55F, 8);
         Cos3Geo.limb(var0, "hd_plastic", var11, var12, 7);
         var0.color(-13989318);

         for (byte var17 = 1; var17 < var10; var17 += 2) {
            var0.push();
            var0.translate(var11[var17][0], var11[var17][1], var11[var17][2]);
            float[] var19 = var11[var17 + 1];
            Cos3Geo.alignY(var0, var19[0] - var11[var17][0], var19[1] - var11[var17][1], var19[2] - var11[var17][2]);
            var0.torus("hd_plastic", var12[var17] + 0.06F, 0.07F, 10, 3, 0.0F, 0.0F, 1.0F, 1.0F);
            var0.pop();
         }

         float[] var18 = var11[var10];
         float var20 = 0.75F + 0.25F * Cos3Geo.sin(var2 * 4.0F + var8);
         var0.push();
         var0.translate(var18[0], var18[1] + 0.85F, var18[2]);
         var0.color(-8585366);
         Cos3Geo.ball(var0, "hd_bubble", 0.0F, 0.0F, 0.0F, 1.15F, 12);
         var0.glow(true).color(0xFF000000 | Capes.mix(3860554, 15269856, var20));
         Cos3Geo.ball(var0, "white", 0.0F, 0.0F, 0.0F, 0.6F * var20, 8);
         var0.glow(false);
         Cos3Geo.halo(var0, 0.0F, 0.0F, 0.0F, 4.4F * var20, Cos3Geo.alpha(0.45 * var20, 8191850));
         var0.pop();
      }

      var0.color(-1);
   }

   static void bow(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(1.9F, 8.9F, 1.3F);
      var0.rotZ(-14.0F + Cos3Geo.sin(var2 * 2.0F) * 2.0F);
      var0.rotY(-12.0F);
      var0.scale(1.3F);
      var0.color(-46450);
      float[][] var3 = new float[18][];
      var3[0] = new float[]{0.3F, -0.6F};

      for (int var4 = 0; var4 < 16; var4++) {
         double var5 = Math.toRadians(-150.0 + 300 * var4 / 15.0);
         var3[var4 + 1] = new float[]{2.25F + Cos3Geo.cos(var5) * 1.55F, Cos3Geo.sin(var5) * 1.75F};
      }

      var3[17] = new float[]{0.3F, 0.6F};

      for (byte var7 = 1; var7 >= -1; var7 -= 2) {
         var0.push();
         var0.scale(var7, 1.0F, 1.0F);
         var0.rotZ(10.0F);
         var0.rotY(-12.0F);
         HdGeo.puff(var0, "hd_satin", var3, 1.25F, 0.55F, 2);
         var0.pop();
         var0.push();
         var0.translate(var7 * 0.35F, -0.4F, 0.2F);
         var0.rotZ(var7 * 24 + Cos3Geo.sin(var2 * 2.5F + var7) * 5.0F);
         float[][] var8 = new float[][]{{-0.62F, 0.0F}, {0.62F, 0.0F}, {0.95F, -3.1F}, {0.2F, -2.6F}, {-0.45F, -3.2F}};
         HdGeo.puff(var0, "hd_satin", var8, 0.32F, 0.12F, 1);
         var0.pop();
      }

      var0.color(-2084742);
      Geo.ellipsoid(var0, "hd_satin", 0.0F, 0.0F, 0.05F, 0.8F, 1.0F, 0.78F, 12, 8);
      var0.color(-1);
      var0.pop();
   }

   static void mushroom(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = Cos3Geo.sin(var2 * 2.0F) * 0.15F;
      var0.push();
      var0.translate(0.0F, 7.3F + var3, 0.0F);
      var0.rotZ(Cos3Geo.sin(var2 * 1.3F) * 2.0F);
      float[] var4 = new float[]{5.4F, 6.55F, 6.85F, 6.7F, 6.1F, 5.1F, 3.6F, 1.8F, 0.0F};
      float[] var5 = new float[]{0.35F, 0.25F, 0.75F, 1.6F, 2.8F, 3.9F, 4.7F, 5.1F, 5.2F};
      var0.color(-1);
      Cos3Geo.lathe(var0, "hd_mush", reverse(var4), reverse(var5), 30, 0.0F, 0.0F, 1.0F, 1.0F);
      Cos3Geo.lathe(var0, "hd_gill", new float[]{5.4F, 4.4F}, new float[]{0.35F, 0.9F}, 30);
      Random var6 = new Random(42L);

      for (int var7 = 0; var7 < 13; var7++) {
         double var8 = var7 * 2.39996;
         float var10 = var7 == 0 ? 0.0F : 0.25F + 0.65F * (float)Math.sqrt(var7 / 13.0F);
         float var11 = 2.0F + (1.0F - var10) * 5.6F;
         int var12 = Math.min(7, (int)var11);
         float var13 = var11 - var12;
         float var14 = var4[var12] + (var4[var12 + 1] - var4[var12]) * var13;
         float var15 = var5[var12] + (var5[var12 + 1] - var5[var12]) * var13;
         float var16 = var4[var12 + 1] - var4[var12];
         float var17 = var5[var12 + 1] - var5[var12];
         float var18 = Cos3Geo.cos(var8);
         float var19 = Cos3Geo.sin(var8);
         var0.push();
         var0.translate(var18 * var14, var15, var19 * var14);
         Cos3Geo.alignY(var0, var18 * var17, -var16, var19 * var17);
         float var20 = 0.55F + var6.nextFloat() * 0.35F;
         var0.color(-1810);
         Geo.ellipsoid(var0, "hd_cotton", 0.0F, 0.0F, 0.0F, var20, 0.22F, var20 * 0.9F, 8, 3);
         var0.pop();
      }

      var0.pop();
      var0.color(-1);
   }

   static float[] reverse(float[] var0) {
      float[] var1 = new float[var0.length];

      for (int var2 = 0; var2 < var0.length; var2++) {
         var1[var2] = var0[var0.length - 1 - var2];
      }

      return var1;
   }

   interface Lift {
      float at(double var1, float var3);
   }
}
