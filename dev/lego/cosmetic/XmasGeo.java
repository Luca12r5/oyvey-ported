package dev.lego.cosmetic;

import java.util.Arrays;

final class XmasGeo {
   static final int[] LIGHTS = new int[]{-50630, -11718, -12916630, -12932865, -34080, -26070};

   private XmasGeo() {
   }

   private static float[] p(float var0, float var1, float var2) {
      return new float[]{var0, var1, var2};
   }

   static void sweep(G var0, String var1, float[][] var2, float[] var3, float[] var4, float[] var5, int var6, float var7) {
      int var8 = var2.length;
      float[][][] var9 = new float[var8][var6 + 1][];
      float[] var10 = new float[]{1.0F, 0.0F, 0.0F};

      for (int var11 = 0; var11 < var8; var11++) {
         float[] var12 = var2[Math.max(0, var11 - 1)];
         float[] var13 = var2[Math.min(var8 - 1, var11 + 1)];
         float var14 = var13[0] - var12[0];
         float var15 = var13[1] - var12[1];
         float var16 = var13[2] - var12[2];
         float var17 = (float)Math.sqrt(var14 * var14 + var15 * var15 + var16 * var16);
         if (var17 < 1.0E-6F) {
            var14 = 0.0F;
            var15 = 1.0F;
            var16 = 0.0F;
            var17 = 1.0F;
         }

         var14 /= var17;
         var15 /= var17;
         var16 /= var17;
         float var18 = var10[0] * var14 + var10[1] * var15 + var10[2] * var16;
         float var19 = var10[0] - var18 * var14;
         float var20 = var10[1] - var18 * var15;
         float var21 = var10[2] - var18 * var16;
         float var22 = (float)Math.sqrt(var19 * var19 + var20 * var20 + var21 * var21);
         if (var22 < 1.0E-4F) {
            var19 = 0.0F;
            var20 = 0.0F;
            var21 = 1.0F;
            var22 = 1.0F;
         }

         var19 /= var22;
         var20 /= var22;
         var21 /= var22;
         var10 = new float[]{var19, var20, var21};
         float var23 = var15 * var21 - var16 * var20;
         float var24 = var16 * var19 - var14 * var21;
         float var25 = var14 * var20 - var15 * var19;

         for (int var26 = 0; var26 <= var6; var26++) {
            float[] var27 = Geo.sq((Math.PI * 2) * var26 / var6, 1.0F, var5 == null ? 2.0F : var5[var11]);
            float var28 = var27[0] * var3[var11];
            float var29 = var27[1] * var4[var11];
            var9[var11][var26] = p(
               var2[var11][0] + var19 * var28 + var23 * var29, var2[var11][1] + var20 * var28 + var24 * var29, var2[var11][2] + var21 * var28 + var25 * var29
            );
         }
      }

      for (int var30 = 0; var30 + 1 < var8; var30++) {
         float var31 = var7 * var30 / (var8 - 1);
         float var32 = var7 * (var30 + 1) / (var8 - 1);

         for (int var34 = 0; var34 < var6; var34++) {
            float var36 = (float)var34 / var6;
            float var38 = (float)(var34 + 1) / var6;
            var0.quad(
               var1,
               var9[var30 + 1][var34],
               var9[var30 + 1][var34 + 1],
               var9[var30][var34 + 1],
               var9[var30][var34],
               new float[]{var36, var32, var38, var32, var38, var31, var36, var31}
            );
         }
      }
   }

   static void bulb(G var0, float var1, int var2, float var3) {
      boolean var4 = var0.glow;
      int var5 = var0.color;
      var0.glow(false).color(-13739468);
      var0.cylinder("c3_metal", 0.26F * var1, 0.3F * var1, 0.0F, 0.42F * var1, 6, false, 0.0F, 0.0F, 1.0F, 1.0F);
      int var6 = var3 > 0.5F ? var2 : 0xFF000000 | Capes.mix(var2 & 16777215, 2105376, 0.55 - var3 * 0.5);
      var0.glow(var3 > 0.2F).color(0xFF000000 | Capes.mix(var6 & 16777215, 16777215, 0.15 * var3));
      Geo.ellipsoid(var0, "xm_bulb", 0.0F, 0.95F * var1, 0.0F, 0.38F * var1, 0.62F * var1, 0.38F * var1, 8, 5);
      if (var3 > 0.2F) {
         Cos3Geo.halo(var0, 0.0F, 0.95F * var1, 0.0F, 2.3F * var1, Cos3Geo.alpha(0.45 * var3, var2));
      }

      var0.glow(var4).color(var5);
   }

   static void bulbLite(G var0, float var1, int var2, float var3) {
      boolean var4 = var0.glow;
      int var5 = var0.color;
      var0.glow(false).color(-13739468);
      var0.cylinder("c3_metal", 0.26F * var1, 0.3F * var1, 0.0F, 0.42F * var1, 6, false, 0.0F, 0.0F, 1.0F, 1.0F);
      int var6 = 0xFF000000 | Capes.mix(2631720, var2 & 16777215, 0.3 + 0.7 * var3);
      var0.glow(var3 > 0.3F).color(var6);
      Geo.ellipsoid(var0, "xm_bulb", 0.0F, 0.95F * var1, 0.0F, 0.38F * var1, 0.62F * var1, 0.38F * var1, 6, 4);
      if (var3 > 0.3F) {
         Cos3Geo.halo(var0, 0.0F, 0.95F * var1, 0.0F, 2.4F * var1, Cos3Geo.alpha(0.5 * var3, var2));
      }

      var0.glow(var4).color(var5);
   }

   static void fairy(G var0, float var1, float var2, float var3, float var4, int var5, float var6) {
      boolean var7 = var0.glow;
      int var8 = var0.color;
      int var9 = 0xFF000000 | Capes.mix(4210752, var5 & 16777215, 0.55 + 0.45 * var6);
      var0.glow(true).color(var9);
      Cos3Geo.ball(var0, "xm_bulb", var1, var2, var3, var4, 6);
      if (var6 > 0.3F) {
         Cos3Geo.halo(var0, var1, var2, var3, var4 * 5.5F, Cos3Geo.alpha(0.5 * var6, var5));
      }

      var0.glow(var7).color(var8);
   }

   static float twinkle(float var0, int var1) {
      double var2 = Capes.hash1(var1, 91);
      float var4 = 0.5F + 0.5F * Cos3Geo.sin(var0 * (2.2 + var2 * 2.5) + var2 * 30.0);
      return 0.25F + 0.75F * Cos3Geo.smooth(var4 * 1.4F - 0.2F);
   }

   static void bell(G var0, float var1, float var2) {
      int var3 = var0.color;
      var0.push();
      var0.rotZ(var2);
      var0.color(-1525460);
      var0.push();
      var0.translate(0.0F, -0.1F * var1, 0.0F);
      var0.rotX(90.0F);
      var0.torus("xm_gold", 0.18F * var1, 0.06F * var1, 8, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      Cos3Geo.lathe(
         var0,
         "xm_gold",
         new float[]{0.0F, 0.28F * var1, 0.42F * var1, 0.5F * var1, 0.62F * var1, 0.8F * var1, 0.84F * var1, 0.7F * var1, 0.0F},
         new float[]{-0.2F * var1, -0.24F * var1, -0.4F * var1, -0.7F * var1, -1.05F * var1, -1.3F * var1, -1.4F * var1, -1.44F * var1, -1.44F * var1},
         10
      );
      var0.color(-7706086);
      Cos3Geo.ball(var0, "c3_metal", 0.0F, -1.48F * var1, 0.0F, 0.2F * var1, 6);
      var0.pop();
      var0.color(var3);
   }

   static void hollyLeaf(G var0, float var1) {
      float var2 = var1 * 0.36F;
      float[][] var3 = new float[][]{
         {0.0F, 0.0F},
         {var1 * 0.18F, var2 * 0.75F},
         {var1 * 0.3F, var2 * 0.55F},
         {var1 * 0.45F, var2},
         {var1 * 0.58F, var2 * 0.62F},
         {var1 * 0.74F, var2 * 0.9F},
         {var1 * 0.84F, var2 * 0.4F},
         {var1, 0.0F}
      };
      int var4 = var0.color;
      var0.color(-1);

      for (byte var5 = -1; var5 <= 1; var5 += 2) {
         var0.push();
         var0.rotX(var5 * 14);
         float[][] var6 = new float[var3.length][];

         for (int var7 = 0; var7 < var3.length; var7++) {
            var6[var5 > 0 ? var7 : var3.length - 1 - var7] = new float[]{var3[var7][0], var5 * var3[var7][1]};
         }

         Cos3Geo.extrude(
            var0, "xm_holly", var6, -0.05F, 0.05F, new float[]{0.0F, var5 > 0 ? 0.0F : 0.5F, 1.0F, var5 > 0 ? 0.5F : 1.0F}, new float[]{0.1F, 0.4F, 0.2F, 0.6F}
         );
         var0.pop();
      }

      var0.color(var4);
   }

   static void holly(G var0, float var1) {
      float[] var2 = new float[]{20.0F, 150.0F, 270.0F};

      for (int var3 = 0; var3 < 3; var3++) {
         var0.push();
         var0.rotY(var2[var3]);
         var0.rotZ(12.0F);
         hollyLeaf(var0, 2.4F * var1);
         var0.pop();
      }

      int var4 = var0.color;
      var0.color(-2090966);
      Cos3Geo.ball(var0, "xm_gloss", 0.25F * var1, 0.32F * var1, 0.12F * var1, 0.34F * var1, 8);
      Cos3Geo.ball(var0, "xm_gloss", -0.28F * var1, 0.3F * var1, 0.2F * var1, 0.32F * var1, 8);
      Cos3Geo.ball(var0, "xm_gloss", 0.0F, 0.36F * var1, -0.3F * var1, 0.33F * var1, 8);
      var0.color(var4);
   }

   static void bow(G var0, int var1, float var2) {
      int var3 = var0.color;
      var0.color(var1);

      for (byte var4 = -1; var4 <= 1; var4 += 2) {
         var0.push();
         var0.translate(var4 * 0.75F * var2, 0.12F * var2, 0.0F);
         var0.rotZ(var4 * 18);
         var0.scale(1.0F, 0.62F, 0.5F);
         var0.rotX(90.0F);
         var0.torus("xm_ribbon", 0.62F * var2, 0.2F * var2, 10, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
         float[][] var5 = new float[][]{{-0.1F * var2, 0.05F * var2}, {-0.6F * var2, 0.12F * var2}, {-1.2F * var2, 0.05F * var2}};
         var0.push();
         var0.rotZ(var4 * 24);
         Cos3Geo.ribbon(var0, "xm_ribbon", 0.0F, var5, new float[]{0.42F * var2, 0.46F * var2, 0.5F * var2}, 0.08F * var2);
         var0.pop();
      }

      Cos3Geo.ball(var0, "xm_ribbon", 0.0F, 0.05F * var2, 0.08F * var2, 0.3F * var2, 8);
      var0.color(var3);
   }

   static void star(G var0, String var1, float var2, float var3, int var4) {
      int var5 = var4 * 2;
      float[][] var6 = new float[var5][];

      for (int var7 = 0; var7 < var5; var7++) {
         double var8 = (Math.PI / 2) + Math.PI * var7 / var4;
         float var10 = var7 % 2 == 0 ? var2 : var2 * 0.45F;
         var6[var7] = p((float)Math.cos(var8) * var10, (float)Math.sin(var8) * var10, 0.0F);
      }

      float[] var12 = p(0.0F, 0.0F, var3);
      float[] var13 = p(0.0F, 0.0F, -var3);

      for (int var9 = 0; var9 < var5; var9++) {
         float[] var14 = var6[var9];
         float[] var11 = var6[(var9 + 1) % var5];
         var0.quad(var1, var12, var11, var14, var14, new float[]{0.5F, 0.5F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F});
         var0.quad(var1, var13, var14, var11, var11, new float[]{0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F});
      }
   }

   static void flake(G var0, String var1, float var2, float var3) {
      float var4 = var2 * 0.09F;

      for (int var5 = 0; var5 < 3; var5++) {
         var0.push();
         var0.rotZ(var5 * 60);
         var0.box(var1, -var2, -var4, -var3, var2, var4, var3, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      for (int var6 = 0; var6 < 6; var6++) {
         var0.push();
         var0.rotZ(var6 * 60);
         var0.translate(0.0F, var2 * 0.62F, 0.0F);
         var0.box(var1, -var2 * 0.24F, -var4 * 0.8F, -var3 * 0.8F, var2 * 0.24F, var4 * 0.8F, var3 * 0.8F, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }
   }

   static void wire(G var0, float[][] var1, float var2) {
      int var3 = var0.color;
      var0.color(-14796254);
      float[] var4 = new float[var1.length];
      Arrays.fill(var4, var2);
      Geo.chain(var0, "c3_metal", var1, var4, 3);
      var0.color(var3);
   }
}
