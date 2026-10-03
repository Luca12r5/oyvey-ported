package dev.lego.cosmetic;

final class HdGeo {
   private HdGeo() {
   }

   static float sin(double var0) {
      return (float)Math.sin(var0);
   }

   static float cos(double var0) {
      return (float)Math.cos(var0);
   }

   private static float[] p(float var0, float var1, float var2) {
      return new float[]{var0, var1, var2};
   }

   static float[][] circle(float var0, float var1, int var2) {
      float[][] var3 = new float[var2][];

      for (int var4 = 0; var4 < var2; var4++) {
         double var5 = (Math.PI * 2) * var4 / var2;
         var3[var4] = new float[]{cos(var5) * var0, sin(var5) * var1};
      }

      return var3;
   }

   static float[][] heart(float var0, int var1) {
      float[][] var2 = new float[var1][];

      for (int var3 = 0; var3 < var1; var3++) {
         double var4 = (Math.PI * 2) * var3 / var1;
         double var6 = 16.0 * Math.pow(Math.sin(var4), 3.0);
         double var8 = 13.0 * Math.cos(var4) - 5.0 * Math.cos(2.0 * var4) - 2.0 * Math.cos(3.0 * var4) - Math.cos(4.0 * var4);
         var2[var3] = new float[]{(float)(-var6 / 16.0 * var0), (float)((var8 + 2.5) / 16.0 * var0)};
      }

      return ccw(var2);
   }

   static float[][] star(float var0, float var1, int var2) {
      float[][] var3 = new float[var2 * 2][];

      for (int var4 = 0; var4 < var2 * 2; var4++) {
         double var5 = (Math.PI / 2) + Math.PI * var4 / var2;
         float var7 = var4 % 2 == 0 ? var0 : var1;
         var3[var4] = new float[]{cos(var5) * var7, sin(var5) * var7};
      }

      return var3;
   }

   static float[][] rrect(float var0, float var1, float var2, int var3) {
      float[][] var4 = new float[4 * (var3 + 1)][];
      float[][] var5 = new float[][]{{var0 - var2, var1 - var2}, {-var0 + var2, var1 - var2}, {-var0 + var2, -var1 + var2}, {var0 - var2, -var1 + var2}};
      int var6 = 0;

      for (int var7 = 0; var7 < 4; var7++) {
         for (int var8 = 0; var8 <= var3; var8++) {
            double var9 = (Math.PI / 2) * var7 + (Math.PI / 2) * var8 / var3;
            var4[var6++] = new float[]{var5[var7][0] + cos(var9) * var2, var5[var7][1] + sin(var9) * var2};
         }
      }

      return var4;
   }

   static float[][] ccw(float[][] var0) {
      double var1 = 0.0;

      for (int var3 = 0; var3 < var0.length; var3++) {
         float[] var4 = var0[var3];
         float[] var5 = var0[(var3 + 1) % var0.length];
         var1 += var4[0] * var5[1] - var5[0] * var4[1];
      }

      if (var1 >= 0.0) {
         return var0;
      } else {
         float[][] var6 = new float[var0.length][];

         for (int var7 = 0; var7 < var0.length; var7++) {
            var6[var7] = var0[var0.length - 1 - var7];
         }

         return var6;
      }
   }

   static float[][] offset(float[][] var0, float var1) {
      int var2 = var0.length;
      float[][] var3 = new float[var2][];

      for (int var4 = 0; var4 < var2; var4++) {
         float[] var5 = var0[(var4 + var2 - 1) % var2];
         float[] var6 = var0[var4];
         float[] var7 = var0[(var4 + 1) % var2];
         float var8 = var6[0] - var5[0];
         float var9 = var6[1] - var5[1];
         float var10 = var7[0] - var6[0];
         float var11 = var7[1] - var6[1];
         float var12 = (float)Math.max(1.0E-6, Math.hypot(var8, var9));
         float var13 = (float)Math.max(1.0E-6, Math.hypot(var10, var11));
         float var14 = var9 / var12;
         float var15 = -var8 / var12;
         float var16 = var11 / var13;
         float var17 = -var10 / var13;
         float var18 = var14 + var16;
         float var19 = var15 + var17;
         float var20 = (float)Math.hypot(var18, var19);
         if (var20 < 1.0E-4F) {
            var18 = var14;
            var19 = var15;
            var20 = 1.0F;
         }

         var18 /= var20;
         var19 /= var20;
         float var21 = var18 * var14 + var19 * var15;
         float var22 = var1 / Math.max(0.45F, var21);
         var3[var4] = new float[]{var6[0] + var18 * var22, var6[1] + var19 * var22};
      }

      return var3;
   }

   static void puff(G var0, String var1, float[][] var2, float var3, float var4, int var5, float[] var6, float[] var7) {
      float[][] var8 = ccw(var2);
      var4 = Math.min(var4, var3 / 2.0F * 0.98F);
      int var9 = var8.length;
      float var10 = 1.0E9F;
      float var11 = 1.0E9F;
      float var12 = -1.0E9F;
      float var13 = -1.0E9F;

      for (float[] var17 : var8) {
         var10 = Math.min(var10, var17[0]);
         var12 = Math.max(var12, var17[0]);
         var11 = Math.min(var11, var17[1]);
         var13 = Math.max(var13, var17[1]);
      }

      float var33 = Math.max(1.0E-4F, var12 - var10);
      float var34 = Math.max(1.0E-4F, var13 - var11);
      int var35 = var5 * 2 + 2;
      float[][][] var36 = new float[var35][][];
      float[] var18 = new float[var35];
      float var19 = var3 / 2.0F;
      int var20 = 0;

      for (int var21 = 0; var21 <= var5; var21++) {
         double var22 = (Math.PI / 2) * var21 / var5;
         var36[var20] = offset(var8, -var4 * (1.0F - (float)Math.sin(var22)));
         var18[var20++] = -var19 + var4 * (1.0F - (float)Math.cos(var22));
      }

      for (int var37 = var5; var37 >= 0; var37--) {
         double var40 = (Math.PI / 2) * var37 / var5;
         var36[var20] = offset(var8, -var4 * (1.0F - (float)Math.sin(var40)));
         var18[var20++] = var19 - var4 * (1.0F - (float)Math.cos(var40));
      }

      for (int var38 = 0; var38 + 1 < var35; var38++) {
         float var41 = var7[1] + (var7[3] - var7[1]) * var38 / (var35 - 1);
         float var23 = var7[1] + (var7[3] - var7[1]) * (var38 + 1) / (var35 - 1);

         for (int var24 = 0; var24 < var9; var24++) {
            int var25 = (var24 + 1) % var9;
            float var26 = var7[0] + (var7[2] - var7[0]) * var24 / var9;
            float var27 = var7[0] + (var7[2] - var7[0]) * (var24 + 1) / var9;
            float[] var28 = var36[var38][var24];
            float[] var29 = var36[var38][var25];
            float[] var30 = var36[var38 + 1][var25];
            float[] var31 = var36[var38 + 1][var24];
            var0.quad(
               var1,
               p(var28[0], var28[1], var18[var38]),
               p(var29[0], var29[1], var18[var38]),
               p(var30[0], var30[1], var18[var38 + 1]),
               p(var31[0], var31[1], var18[var38 + 1]),
               new float[]{var26, var41, var27, var41, var27, var23, var26, var23}
            );
         }
      }

      float[][] var39 = var36[0];
      float[][] var42 = var36[var35 - 1];

      for (int[] var45 : Cos3Geo.triangulate(var39)) {
         float[] var46 = new float[8];
         float[] var47 = new float[8];

         for (int var48 = 0; var48 < 3; var48++) {
            float[] var51 = var42[var45[var48]];
            var46[var48 * 2] = var6[0] + (var6[2] - var6[0]) * (var12 - var51[0]) / var33;
            var46[var48 * 2 + 1] = var6[1] + (var6[3] - var6[1]) * (var13 - var51[1]) / var34;
            float[] var54 = var39[var45[var48]];
            var47[var48 * 2] = var6[0] + (var6[2] - var6[0]) * (var54[0] - var10) / var33;
            var47[var48 * 2 + 1] = var6[1] + (var6[3] - var6[1]) * (var13 - var54[1]) / var34;
         }

         float[] var49 = var42[var45[0]];
         float[] var52 = var42[var45[1]];
         float[] var55 = var42[var45[2]];
         var0.quad(
            var1,
            p(var49[0], var49[1], var19),
            p(var52[0], var52[1], var19),
            p(var55[0], var55[1], var19),
            p(var55[0], var55[1], var19),
            new float[]{var46[0], var46[1], var46[2], var46[3], var46[4], var46[5], var46[4], var46[5]}
         );
         var49 = var39[var45[0]];
         var52 = var39[var45[1]];
         var55 = var39[var45[2]];
         var0.quad(
            var1,
            p(var55[0], var55[1], -var19),
            p(var52[0], var52[1], -var19),
            p(var49[0], var49[1], -var19),
            p(var49[0], var49[1], -var19),
            new float[]{var47[4], var47[5], var47[2], var47[3], var47[0], var47[1], var47[0], var47[1]}
         );
      }
   }

   static void puff(G var0, String var1, float[][] var2, float var3, float var4, int var5) {
      puff(var0, var1, var2, var3, var4, var5, Cos3Geo.FULL, new float[]{0.45F, 0.45F, 0.55F, 0.55F});
   }

   static void loop(G var0, String var1, float[][] var2, float var3, float var4, float var5, int var6) {
      float[][] var7 = ccw(var2);
      int var8 = var7.length;
      float[][][] var9 = new float[var8][var6 + 1][];

      for (int var10 = 0; var10 < var8; var10++) {
         float[] var11 = var7[(var10 + var8 - 1) % var8];
         float[] var12 = var7[(var10 + 1) % var8];
         float var13 = var12[0] - var11[0];
         float var14 = var12[1] - var11[1];
         float var15 = (float)Math.max(1.0E-6, Math.hypot(var13, var14));
         float var16 = var14 / var15;
         float var17 = -var13 / var15;

         for (int var18 = 0; var18 <= var6; var18++) {
            float[] var19 = Geo.sq((Math.PI * 2) * var18 / var6, 1.0F, var5);
            var9[var10][var18] = p(var7[var10][0] + var16 * var19[0] * var3, var7[var10][1] + var17 * var19[0] * var3, var19[1] * var4);
         }
      }

      for (int var20 = 0; var20 < var8; var20++) {
         int var21 = (var20 + 1) % var8;
         float var22 = (float)var20 / var8;
         float var23 = (float)(var20 + 1) / var8;

         for (int var24 = 0; var24 < var6; var24++) {
            float var25 = (float)var24 / var6;
            float var26 = (float)(var24 + 1) / var6;
            var0.quad(
               var1,
               var9[var20][var24],
               var9[var21][var24],
               var9[var21][var24 + 1],
               var9[var20][var24 + 1],
               new float[]{var22, var25, var23, var25, var23, var26, var22, var26}
            );
         }
      }
   }

   static void horn(G var0, String var1, float var2, float var3, float var4, float var5, int var6, int var7, float var8) {
      float[][] var9 = new float[var6 + 1][];
      float[] var10 = new float[var6 + 1];
      float var11 = 0.0F;
      float var12 = 0.0F;
      float var13 = 0.0F;
      double var14 = 0.0;
      double var16 = 0.0;
      float var18 = var3 / var6;

      for (int var19 = 0; var19 <= var6; var19++) {
         float var20 = (float)var19 / var6;
         var9[var19] = new float[]{var11, var12, var13};
         float var21 = var8 > 0.0F ? 1.0F + 0.07F * (float)Math.cos(var20 * Math.PI * 2.0 * var8) : 1.0F;
         var10[var19] = Math.max(0.02F, var2 * (1.0F - var20 * 0.94F) * var21);
         var14 += Math.toRadians(var4) / var6;
         var16 += Math.toRadians(var5) / var6;
         var11 += (float)Math.sin(var14) * var18;
         var12 += (float)Math.cos(var14) * var18;
         var13 += (float)Math.sin(var16) * var18 * 0.35F;
      }

      XmasGeo.sweep(var0, var1, var9, var10, var10, null, var7, 1.0F);
      Cos3Geo.ball(var0, var1, 0.0F, 0.0F, 0.0F, var2 * 0.98F, var7);
   }

   static void glowBall(G var0, float var1, float var2, float var3, float var4, int var5) {
      Cos3Geo.halo(var0, var1, var2, var3, var4, var5);
   }
}
