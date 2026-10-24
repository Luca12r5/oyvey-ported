package dev.lego.cosmetic;

final class Geo {
   private Geo() {
   }

   static float lerp(float var0, float var1, float var2) {
      return var0 + (var1 - var0) * var2;
   }

   static float sin(double var0) {
      return (float)Math.sin(var0);
   }

   static float cos(double var0) {
      return (float)Math.cos(var0);
   }

   static void tube(G var0, String var1, float[] var2, float[] var3, float var4, float var5, int var6) {
      tube(var0, var1, var2, var3, var4, var5, var6, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   static void tube(G var0, String var1, float[] var2, float[] var3, float var4, float var5, int var6, float var7, float var8, float var9, float var10) {
      float var11 = var3[0] - var2[0];
      float var12 = var3[1] - var2[1];
      float var13 = var3[2] - var2[2];
      float var14 = (float)Math.sqrt(var11 * var11 + var12 * var12 + var13 * var13);
      if (!(var14 < 1.0E-4F)) {
         var11 /= var14;
         var12 /= var14;
         var13 /= var14;
         float var15;
         float var16;
         float var17;
         if (Math.abs(var12) < 0.9F) {
            var15 = var13;
            var16 = 0.0F;
            var17 = -var11;
         } else {
            var15 = 0.0F;
            var16 = -var13;
            var17 = var12;
         }

         float var18 = (float)Math.sqrt(var15 * var15 + var16 * var16 + var17 * var17);
         var15 /= var18;
         var16 /= var18;
         var17 /= var18;
         float var19 = var12 * var17 - var13 * var16;
         float var20 = var13 * var15 - var11 * var17;
         float var21 = var11 * var16 - var12 * var15;

         for (int var22 = 0; var22 < var6; var22++) {
            double var23 = (Math.PI * 2) * var22 / var6;
            double var25 = (Math.PI * 2) * (var22 + 1) / var6;
            float var27 = (float)Math.cos(var23);
            float var28 = (float)Math.sin(var23);
            float var29 = (float)Math.cos(var25);
            float var30 = (float)Math.sin(var25);
            float[] var31 = new float[]{
               var2[0] + (var15 * var27 + var19 * var28) * var4,
               var2[1] + (var16 * var27 + var20 * var28) * var4,
               var2[2] + (var17 * var27 + var21 * var28) * var4
            };
            float[] var32 = new float[]{
               var2[0] + (var15 * var29 + var19 * var30) * var4,
               var2[1] + (var16 * var29 + var20 * var30) * var4,
               var2[2] + (var17 * var29 + var21 * var30) * var4
            };
            float[] var33 = new float[]{
               var3[0] + (var15 * var27 + var19 * var28) * var5,
               var3[1] + (var16 * var27 + var20 * var28) * var5,
               var3[2] + (var17 * var27 + var21 * var28) * var5
            };
            float[] var34 = new float[]{
               var3[0] + (var15 * var29 + var19 * var30) * var5,
               var3[1] + (var16 * var29 + var20 * var30) * var5,
               var3[2] + (var17 * var29 + var21 * var30) * var5
            };
            float var35 = var7 + (var9 - var7) * var22 / var6;
            float var36 = var7 + (var9 - var7) * (var22 + 1) / var6;
            var0.quad(var1, var33, var34, var32, var31, new float[]{var35, var8, var36, var8, var36, var10, var35, var10});
         }
      }
   }

   static void chain(G var0, String var1, float[][] var2, float[] var3, int var4) {
      for (int var5 = 0; var5 + 1 < var2.length; var5++) {
         float var6 = (float)var5 / (var2.length - 1);
         float var7 = (float)(var5 + 1) / (var2.length - 1);
         tube(var0, var1, var2[var5], var2[var5 + 1], var3[var5], var3[var5 + 1], var4, 0.0F, var6, 1.0F, var7);
      }
   }

   static void tri(G var0, String var1, float[] var2, float[] var3, float[] var4, float[] var5, float[] var6, float[] var7) {
      var0.quad(var1, var2, var3, var4, var4, new float[]{var5[0], var5[1], var6[0], var6[1], var7[0], var7[1], var7[0], var7[1]});
   }

   static void sprite(G var0, String var1, float var2, float var3, float var4, float var5) {
      var0.cross(var1, var2, var3, var4, var5, var5, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   static void sprite3(G var0, String var1, float var2, float var3, float var4, float var5) {
      var0.push();
      var0.translate(var2, var3, var4);
      var0.plane(var1, 0.0F, 0.0F, var5, var5, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.rotY(60.0F);
      var0.plane(var1, 0.0F, 0.0F, var5, var5, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.rotY(60.0F);
      var0.plane(var1, 0.0F, 0.0F, var5, var5, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
   }

   static void ellipsoid(G var0, String var1, float var2, float var3, float var4, float var5, float var6, float var7, int var8, int var9) {
      var0.push();
      var0.translate(var2, var3, var4);
      var0.scale(var5, var6, var7);
      var0.sphere(var1, 1.0F, var8, var9, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
   }

   static void box(G var0, String var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      var0.box(var1, var2, var3, var4, var5, var6, var7, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   static int argb(int var0, int var1) {
      return Math.max(0, Math.min(255, var0)) << 24 | var1 & 16777215;
   }

   static int hue(double var0, double var2, double var4) {
      return 0xFF000000 | Capes.hsv(var0, var2, var4);
   }

   static float[] sq(double var0, float var2, float var3) {
      double var4 = Math.cos(var0);
      double var6 = Math.sin(var0);
      double var8 = 2.0 / var3;
      return new float[]{
         (float)(Math.signum(var4) * Math.pow(Math.abs(var4), var8) * var2), (float)(Math.signum(var6) * Math.pow(Math.abs(var6), var8) * var2)
      };
   }

   static void sqTube(
      G var0, String var1, float var2, float var3, float var4, float var5, float var6, int var7, float var8, float var9, float var10, float var11
   ) {
      for (int var12 = 0; var12 < var7; var12++) {
         double var13 = (Math.PI * 2) * var12 / var7;
         double var15 = (Math.PI * 2) * (var12 + 1) / var7;
         float[] var17 = sq(var13, var2, var6);
         float[] var18 = sq(var15, var2, var6);
         float[] var19 = sq(var13, var3, var6);
         float[] var20 = sq(var15, var3, var6);
         float var21 = var8 + (var10 - var8) * var12 / var7;
         float var22 = var8 + (var10 - var8) * (var12 + 1) / var7;
         var0.quad(
            var1,
            new float[]{var19[0], var5, var19[1]},
            new float[]{var20[0], var5, var20[1]},
            new float[]{var18[0], var4, var18[1]},
            new float[]{var17[0], var4, var17[1]},
            new float[]{var21, var9, var22, var9, var22, var11, var21, var11}
         );
      }
   }

   static void sqDome(G var0, String var1, float var2, float var3, float var4, float var5, int var6, int var7, float var8, float var9, float var10, float var11) {
      for (int var12 = 0; var12 < var7; var12++) {
         double var13 = (Math.PI / 2) * var12 / var7;
         double var15 = (Math.PI / 2) * (var12 + 1) / var7;
         float var17 = var2 * (float)Math.cos(var13);
         float var18 = var2 * (float)Math.cos(var15);
         float var19 = var4 + var3 * (float)Math.sin(var13);
         float var20 = var4 + var3 * (float)Math.sin(var15);
         float var21 = lerp(var5, 2.0F, (float)var12 / var7);
         float var22 = lerp(var5, 2.0F, (float)(var12 + 1) / var7);
         float var23 = var11 - (var11 - var9) * var12 / var7;
         float var24 = var11 - (var11 - var9) * (var12 + 1) / var7;

         for (int var25 = 0; var25 < var6; var25++) {
            double var26 = (Math.PI * 2) * var25 / var6;
            double var28 = (Math.PI * 2) * (var25 + 1) / var6;
            float[] var30 = sq(var26, var17, var21);
            float[] var31 = sq(var28, var17, var21);
            float[] var32 = sq(var26, var18, var22);
            float[] var33 = sq(var28, var18, var22);
            float var34 = var8 + (var10 - var8) * var25 / var6;
            float var35 = var8 + (var10 - var8) * (var25 + 1) / var6;
            var0.quad(
               var1,
               new float[]{var32[0], var20, var32[1]},
               new float[]{var33[0], var20, var33[1]},
               new float[]{var31[0], var19, var31[1]},
               new float[]{var30[0], var19, var30[1]},
               new float[]{var34, var24, var35, var24, var35, var23, var34, var23}
            );
         }
      }
   }
}
