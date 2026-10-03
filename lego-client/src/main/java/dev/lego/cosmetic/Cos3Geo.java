package dev.lego.cosmetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

final class Cos3Geo {
   static final float[] FULL = new float[]{0.0F, 0.0F, 1.0F, 1.0F};

   private Cos3Geo() {
   }

   static float sin(double var0) {
      return (float)Math.sin(var0);
   }

   static float cos(double var0) {
      return (float)Math.cos(var0);
   }

   static float fract(float var0) {
      return var0 - (float)Math.floor(var0);
   }

   static float clamp(float var0, float var1, float var2) {
      return var0 < var1 ? var1 : (var0 > var2 ? var2 : var0);
   }

   static float smooth(float var0) {
      var0 = clamp(var0, 0.0F, 1.0F);
      return var0 * var0 * (3.0F - 2.0F * var0);
   }

   static int argb(int var0, int var1) {
      return Math.max(0, Math.min(255, var0)) << 24 | var1 & 16777215;
   }

   static int alpha(double var0, int var2) {
      return argb((int)Math.round(var0 * 255.0), var2);
   }

   private static void tri(G var0, String var1, float[] var2, float[] var3, float[] var4, float[] var5) {
      var0.quad(var1, var2, var3, var4, var4, new float[]{var5[0], var5[1], var5[2], var5[3], var5[4], var5[5], var5[4], var5[5]});
   }

   private static float[] p(float var0, float var1, float var2) {
      return new float[]{var0, var1, var2};
   }

   static void bbox(G var0, String var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float[] var9) {
      float var10 = Math.min(var5 - var2, Math.min(var6 - var3, var7 - var4)) * 0.45F;
      if (var8 > var10) {
         var8 = var10;
      }

      if (var8 < 0.02F) {
         var0.box(var1, var2, var3, var4, var5, var6, var7, var9[0], var9[1], var9[2], var9[3]);
      } else {
         float var11 = var2 + var8;
         float var12 = var5 - var8;
         float var13 = var3 + var8;
         float var14 = var6 - var8;
         float var15 = var4 + var8;
         float var16 = var7 - var8;
         float[] var17 = new float[]{var9[0], var9[1], var9[2], var9[1], var9[2], var9[3], var9[0], var9[3]};
         float var18 = (var9[0] + var9[2]) / 2.0F;
         float var19 = (var9[1] + var9[3]) / 2.0F;
         float[] var20 = new float[]{var9[0], var9[1], var9[2], var9[1], var18, var9[3], var18, var9[3]};
         var0.quad(var1, p(var11, var14, var7), p(var12, var14, var7), p(var12, var13, var7), p(var11, var13, var7), var17);
         var0.quad(var1, p(var12, var14, var4), p(var11, var14, var4), p(var11, var13, var4), p(var12, var13, var4), var17);
         var0.quad(var1, p(var2, var14, var15), p(var2, var14, var16), p(var2, var13, var16), p(var2, var13, var15), var17);
         var0.quad(var1, p(var5, var14, var16), p(var5, var14, var15), p(var5, var13, var15), p(var5, var13, var16), var17);
         var0.quad(var1, p(var11, var6, var15), p(var12, var6, var15), p(var12, var6, var16), p(var11, var6, var16), var17);
         var0.quad(var1, p(var11, var3, var16), p(var12, var3, var16), p(var12, var3, var15), p(var11, var3, var15), var17);
         var0.quad(var1, p(var11, var6, var16), p(var12, var6, var16), p(var12, var14, var7), p(var11, var14, var7), var17);
         var0.quad(var1, p(var12, var6, var15), p(var11, var6, var15), p(var11, var14, var4), p(var12, var14, var4), var17);
         var0.quad(var1, p(var2, var14, var15), p(var2, var14, var16), p(var11, var6, var16), p(var11, var6, var15), var17);
         var0.quad(var1, p(var12, var6, var15), p(var12, var6, var16), p(var5, var14, var16), p(var5, var14, var15), var17);
         var0.quad(var1, p(var11, var13, var7), p(var12, var13, var7), p(var12, var3, var16), p(var11, var3, var16), var17);
         var0.quad(var1, p(var12, var13, var4), p(var11, var13, var4), p(var11, var3, var15), p(var12, var3, var15), var17);
         var0.quad(var1, p(var11, var3, var15), p(var11, var3, var16), p(var2, var13, var16), p(var2, var13, var15), var17);
         var0.quad(var1, p(var5, var13, var15), p(var5, var13, var16), p(var12, var3, var16), p(var12, var3, var15), var17);
         var0.quad(var1, p(var2, var14, var16), p(var11, var14, var7), p(var11, var13, var7), p(var2, var13, var16), var17);
         var0.quad(var1, p(var12, var14, var7), p(var5, var14, var16), p(var5, var13, var16), p(var12, var13, var7), var17);
         var0.quad(var1, p(var11, var14, var4), p(var2, var14, var15), p(var2, var13, var15), p(var11, var13, var4), var17);
         var0.quad(var1, p(var5, var14, var15), p(var12, var14, var4), p(var12, var13, var4), p(var5, var13, var15), var17);

         for (int var21 = 0; var21 < 2; var21++) {
            for (int var22 = 0; var22 < 2; var22++) {
               for (int var23 = 0; var23 < 2; var23++) {
                  float var24 = var21 == 0 ? var2 : var5;
                  float var25 = var22 == 0 ? var3 : var6;
                  float var26 = var23 == 0 ? var4 : var7;
                  float var27 = var21 == 0 ? var11 : var12;
                  float var28 = var22 == 0 ? var13 : var14;
                  float var29 = var23 == 0 ? var15 : var16;
                  tri(var0, var1, p(var24, var28, var29), p(var27, var25, var29), p(var27, var28, var26), var20);
               }
            }
         }
      }
   }

   static void bbox(G var0, String var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      bbox(var0, var1, var2, var3, var4, var5, var6, var7, var8, FULL);
   }

   static void cbox(G var0, String var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      bbox(var0, var1, var2 - var5 / 2.0F, var3 - var6 / 2.0F, var4 - var7 / 2.0F, var2 + var5 / 2.0F, var3 + var6 / 2.0F, var4 + var7 / 2.0F, var8, FULL);
   }

   static void lathe(G var0, String var1, float[] var2, float[] var3, int var4, float var5, float var6, float var7, float var8) {
      int var9 = var2.length;

      for (int var10 = 0; var10 + 1 < var9; var10++) {
         float var11 = var6 + (var8 - var6) * var10 / (var9 - 1);
         float var12 = var6 + (var8 - var6) * (var10 + 1) / (var9 - 1);

         for (int var13 = 0; var13 < var4; var13++) {
            double var14 = (Math.PI * 2) * var13 / var4;
            double var16 = (Math.PI * 2) * (var13 + 1) / var4;
            float var18 = (float)Math.cos(var14);
            float var19 = (float)Math.sin(var14);
            float var20 = (float)Math.cos(var16);
            float var21 = (float)Math.sin(var16);
            float var22 = var5 + (var7 - var5) * var13 / var4;
            float var23 = var5 + (var7 - var5) * (var13 + 1) / var4;
            var0.quad(
               var1,
               p(var18 * var2[var10 + 1], var3[var10 + 1], var19 * var2[var10 + 1]),
               p(var20 * var2[var10 + 1], var3[var10 + 1], var21 * var2[var10 + 1]),
               p(var20 * var2[var10], var3[var10], var21 * var2[var10]),
               p(var18 * var2[var10], var3[var10], var19 * var2[var10]),
               new float[]{var22, var12, var23, var12, var23, var11, var22, var11}
            );
         }
      }
   }

   static void lathe(G var0, String var1, float[] var2, float[] var3, int var4) {
      lathe(var0, var1, var2, var3, var4, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   static void gem(G var0, String var1, float var2, float var3, float var4, int var5) {
      lathe(var0, var1, new float[]{0.0F, var2 * 0.95F, var2, var2 * 0.62F, 0.0F}, new float[]{-var4, -var4 * 0.12F, var4 * 0.02F, var3 * 0.55F, var3}, var5);
   }

   static void shard(G var0, String var1, float var2, float var3) {
      lathe(var0, var1, new float[]{0.0F, var2 * 0.8F, var2, var2 * 0.9F, 0.0F}, new float[]{0.0F, 0.05F * var3, 0.25F * var3, 0.72F * var3, var3}, 6);
   }

   static void ball(G var0, String var1, float var2, float var3, float var4, float var5, int var6) {
      var0.push();
      var0.translate(var2, var3, var4);
      var0.sphere(var1, var5, var6, Math.max(3, var6 / 2), 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
   }

   static void extrude(G var0, String var1, float[][] var2, float var3, float var4, float[] var5, float[] var6) {
      int var7 = var2.length;
      float var8 = 1.0E9F;
      float var9 = 1.0E9F;
      float var10 = -1.0E9F;
      float var11 = -1.0E9F;

      for (float[] var15 : var2) {
         var8 = Math.min(var8, var15[0]);
         var10 = Math.max(var10, var15[0]);
         var9 = Math.min(var9, var15[1]);
         var11 = Math.max(var11, var15[1]);
      }

      float var23 = Math.max(1.0E-4F, var10 - var8);
      float var24 = Math.max(1.0E-4F, var11 - var9);

      for (int[] var16 : triangulate(var2)) {
         float[] var17 = new float[6];

         for (int var18 = 0; var18 < 3; var18++) {
            float[] var19 = var2[var16[var18]];
            var17[var18 * 2] = var5[0] + (var5[2] - var5[0]) * (var10 - var19[0]) / var23;
            var17[var18 * 2 + 1] = var5[1] + (var5[3] - var5[1]) * (var11 - var19[1]) / var24;
         }

         float[] var30 = var2[var16[0]];
         float[] var31 = var2[var16[1]];
         float[] var20 = var2[var16[2]];
         tri(var0, var1, p(var30[0], var30[1], var4), p(var31[0], var31[1], var4), p(var20[0], var20[1], var4), var17);
         float[] var21 = (float[])var17.clone();

         for (int var22 = 0; var22 < 3; var22++) {
            var21[var22 * 2] = var5[0] + var5[2] - var17[var22 * 2];
         }

         tri(
            var0,
            var1,
            p(var20[0], var20[1], var3),
            p(var31[0], var31[1], var3),
            p(var30[0], var30[1], var3),
            new float[]{var21[4], var21[5], var21[2], var21[3], var21[0], var21[1]}
         );
      }

      for (int var27 = 0; var27 < var7; var27++) {
         float[] var28 = var2[var27];
         float[] var29 = var2[(var27 + 1) % var7];
         var0.quad(
            var1,
            p(var28[0], var28[1], var4),
            p(var29[0], var29[1], var4),
            p(var29[0], var29[1], var3),
            p(var28[0], var28[1], var3),
            new float[]{var6[0], var6[1], var6[2], var6[1], var6[2], var6[3], var6[0], var6[3]}
         );
      }
   }

   static void extrude(G var0, String var1, float[][] var2, float var3, float var4) {
      extrude(var0, var1, var2, var3, var4, FULL, new float[]{0.45F, 0.45F, 0.55F, 0.55F});
   }

   static List<int[]> triangulate(float[][] var0) {
      int var1 = var0.length;
      ArrayList var2 = new ArrayList();
      ArrayList var3 = new ArrayList();

      for (int var4 = 0; var4 < var1; var4++) {
         var3.add(var4);
      }

      double var21 = 0.0;

      for (int var6 = 0; var6 < var1; var6++) {
         float[] var7 = var0[var6];
         float[] var8 = var0[(var6 + 1) % var1];
         var21 += var7[0] * var8[1] - var8[0] * var7[1];
      }

      boolean var22 = var21 > 0.0;
      int var23 = 0;

      label86:
      while (var3.size() > 3 && var23++ < 1000) {
         boolean var24 = false;

         for (int var9 = 0; var9 < var3.size(); var9++) {
            int var10 = (Integer)var3.get((var9 + var3.size() - 1) % var3.size());
            int var11 = (Integer)var3.get(var9);
            int var12 = (Integer)var3.get((var9 + 1) % var3.size());
            float[] var13 = var0[var10];
            float[] var14 = var0[var11];
            float[] var15 = var0[var12];
            double var16 = (var14[0] - var13[0]) * (var15[1] - var13[1]) - (var14[1] - var13[1]) * (var15[0] - var13[0]);
            if (var22 ? !(var16 <= 1.0E-7) : !(var16 >= -1.0E-7)) {
               boolean var18 = false;

               for (int var20 : (Iterable<Integer>) (Iterable<?>) (var3)) {
                  if (var20 != var10 && var20 != var11 && var20 != var12 && inTri(var0[var20], var13, var14, var15)) {
                     var18 = true;
                     break;
                  }
               }

               if (!var18) {
                  var2.add(var22 ? new int[]{var10, var11, var12} : new int[]{var12, var11, var10});
                  var3.remove(var9);
                  var24 = true;
                  if (!var24) {
                     break label86;
                  }
                  continue label86;
               }
            }
         }
         break;
      }

      if (var3.size() == 3) {
         int var25 = (Integer)var3.get(0);
         int var26 = (Integer)var3.get(1);
         int var27 = (Integer)var3.get(2);
         var2.add(var22 ? new int[]{var25, var26, var27} : new int[]{var27, var26, var25});
      }

      return var2;
   }

   private static boolean inTri(float[] var0, float[] var1, float[] var2, float[] var3) {
      double var4 = (var0[0] - var2[0]) * (var1[1] - var2[1]) - (var1[0] - var2[0]) * (var0[1] - var2[1]);
      double var6 = (var0[0] - var3[0]) * (var2[1] - var3[1]) - (var2[0] - var3[0]) * (var0[1] - var3[1]);
      double var8 = (var0[0] - var1[0]) * (var3[1] - var1[1]) - (var3[0] - var1[0]) * (var0[1] - var1[1]);
      boolean var10 = var4 < 0.0 || var6 < 0.0 || var8 < 0.0;
      boolean var11 = var4 > 0.0 || var6 > 0.0 || var8 > 0.0;
      return !var10 || !var11;
   }

   static float[][] roundLoop(float var0, float var1, float var2, int var3) {
      float[][] var4 = new float[4 * (var3 + 1)][];
      int var5 = 0;
      float[][] var6 = new float[][]{{var0, var1}, {-var0, var1}, {-var0, -var1}, {var0, -var1}};

      for (int var7 = 0; var7 < 4; var7++) {
         for (int var8 = 0; var8 <= var3; var8++) {
            double var9 = (Math.PI / 2) * var7 + (Math.PI / 2) * var8 / var3;
            var4[var5++] = new float[]{var6[var7][0] + (float)Math.cos(var9) * var2, var6[var7][1] + (float)Math.sin(var9) * var2, (float)var9};
         }
      }

      return var4;
   }

   static void band(G var0, String var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      float[][] var9 = roundLoop(var2, var2, var3, 4);
      float[][] var10 = roundLoop(var2, var2, var3 + var4, 4);
      int var11 = var9.length;
      float var12 = 0.0F;
      float[] var13 = new float[var11 + 1];

      for (int var14 = 0; var14 < var11; var14++) {
         float[] var15 = var10[var14];
         float[] var16 = var10[(var14 + 1) % var11];
         var12 += (float)Math.hypot(var16[0] - var15[0], var16[1] - var15[1]);
         var13[var14 + 1] = var12;
      }

      for (int var20 = 0; var20 < var11; var20++) {
         int var21 = (var20 + 1) % var11;
         float var22 = var7 + (var8 - var7) * var13[var20] / var12;
         float var17 = var7 + (var8 - var7) * var13[var20 + 1] / var12;
         float[] var18 = new float[]{var22, 0.0F, var17, 0.0F, var17, 1.0F, var22, 1.0F};
         float[] var19 = new float[]{var22, 0.45F, var17, 0.45F, var17, 0.55F, var22, 0.55F};
         var0.quad(
            var1,
            p(var10[var20][0], var6, var10[var20][1]),
            p(var10[var21][0], var6, var10[var21][1]),
            p(var10[var21][0], var5, var10[var21][1]),
            p(var10[var20][0], var5, var10[var20][1]),
            var18
         );
         var0.quad(
            var1,
            p(var9[var21][0], var6, var9[var21][1]),
            p(var9[var20][0], var6, var9[var20][1]),
            p(var9[var20][0], var5, var9[var20][1]),
            p(var9[var21][0], var5, var9[var21][1]),
            var19
         );
         var0.quad(
            var1,
            p(var9[var20][0], var6, var9[var20][1]),
            p(var9[var21][0], var6, var9[var21][1]),
            p(var10[var21][0], var6, var10[var21][1]),
            p(var10[var20][0], var6, var10[var20][1]),
            new float[]{var22, 0.0F, var17, 0.0F, var17, 0.08F, var22, 0.08F}
         );
         var0.quad(
            var1,
            p(var10[var20][0], var5, var10[var20][1]),
            p(var10[var21][0], var5, var10[var21][1]),
            p(var9[var21][0], var5, var9[var21][1]),
            p(var9[var20][0], var5, var9[var20][1]),
            new float[]{var22, 0.92F, var17, 0.92F, var17, 1.0F, var22, 1.0F}
         );
      }
   }

   static void ribbon(G var0, String var1, float var2, float[][] var3, float[] var4, float var5) {
      int var6 = var3.length;
      float[][] var7 = new float[var6][];

      for (int var8 = 0; var8 < var6; var8++) {
         float[] var9 = var3[Math.max(0, var8 - 1)];
         float[] var10 = var3[Math.min(var6 - 1, var8 + 1)];
         float var11 = var10[0] - var9[0];
         float var12 = var10[1] - var9[1];
         float var13 = (float)Math.max(1.0E-5, Math.hypot(var11, var12));
         var7[var8] = new float[]{-var12 / var13 * var5 / 2.0F, var11 / var13 * var5 / 2.0F};
      }

      for (int var19 = 0; var19 + 1 < var6; var19++) {
         float var21 = (float)var19 / (var6 - 1);
         float var23 = (float)(var19 + 1) / (var6 - 1);
         float[] var25 = var3[var19];
         float[] var27 = var3[var19 + 1];
         float[] var29 = var7[var19];
         float[] var14 = var7[var19 + 1];
         float var15 = var4[var19] / 2.0F;
         float var16 = var4[var19 + 1] / 2.0F;
         float[] var17 = new float[]{0.0F, var21, 1.0F, var21, 1.0F, var23, 0.0F, var23};
         var0.quad(
            var1,
            p(var2 - var15, var25[0] + var29[0], var25[1] + var29[1]),
            p(var2 + var15, var25[0] + var29[0], var25[1] + var29[1]),
            p(var2 + var16, var27[0] + var14[0], var27[1] + var14[1]),
            p(var2 - var16, var27[0] + var14[0], var27[1] + var14[1]),
            var17
         );
         var0.quad(
            var1,
            p(var2 + var15, var25[0] - var29[0], var25[1] - var29[1]),
            p(var2 - var15, var25[0] - var29[0], var25[1] - var29[1]),
            p(var2 - var16, var27[0] - var14[0], var27[1] - var14[1]),
            p(var2 + var16, var27[0] - var14[0], var27[1] - var14[1]),
            var17
         );
         float[] var18 = new float[]{0.0F, var21, 0.1F, var21, 0.1F, var23, 0.0F, var23};
         var0.quad(
            var1,
            p(var2 - var15, var25[0] - var29[0], var25[1] - var29[1]),
            p(var2 - var15, var25[0] + var29[0], var25[1] + var29[1]),
            p(var2 - var16, var27[0] + var14[0], var27[1] + var14[1]),
            p(var2 - var16, var27[0] - var14[0], var27[1] - var14[1]),
            var18
         );
         var0.quad(
            var1,
            p(var2 + var15, var25[0] + var29[0], var25[1] + var29[1]),
            p(var2 + var15, var25[0] - var29[0], var25[1] - var29[1]),
            p(var2 + var16, var27[0] - var14[0], var27[1] - var14[1]),
            p(var2 + var16, var27[0] + var14[0], var27[1] + var14[1]),
            var18
         );
      }

      float[] var20 = var3[var6 - 1];
      float[] var22 = var7[var6 - 1];
      float[] var24 = var3[0];
      float[] var26 = var7[0];
      float var28 = var4[var6 - 1] / 2.0F;
      float var30 = var4[0] / 2.0F;
      float[] var31 = new float[]{0.0F, 0.95F, 1.0F, 0.95F, 1.0F, 1.0F, 0.0F, 1.0F};
      var0.quad(
         var1,
         p(var2 - var28, var20[0] + var22[0], var20[1] + var22[1]),
         p(var2 + var28, var20[0] + var22[0], var20[1] + var22[1]),
         p(var2 + var28, var20[0] - var22[0], var20[1] - var22[1]),
         p(var2 - var28, var20[0] - var22[0], var20[1] - var22[1]),
         var31
      );
      var0.quad(
         var1,
         p(var2 + var30, var24[0] + var26[0], var24[1] + var26[1]),
         p(var2 - var30, var24[0] + var26[0], var24[1] + var26[1]),
         p(var2 - var30, var24[0] - var26[0], var24[1] - var26[1]),
         p(var2 + var30, var24[0] - var26[0], var24[1] - var26[1]),
         var31
      );
   }

   static void limb(G var0, String var1, float[][] var2, float[] var3, int var4) {
      Geo.chain(var0, var1, var2, var3, var4);

      for (int var5 = 0; var5 < 2; var5++) {
         int var6 = var5 == 0 ? 0 : var2.length - 1;
         if (!(var3[var6] < 0.05F)) {
            ball(var0, var1, var2[var6][0], var2[var6][1], var2[var6][2], var3[var6], var4);
         }
      }
   }

   static void alignY(G var0, float var1, float var2, float var3) {
      float var4 = (float)Math.sqrt(var1 * var1 + var2 * var2 + var3 * var3);
      if (!(var4 < 1.0E-6F)) {
         var1 /= var4;
         var2 /= var4;
         var3 /= var4;
         float var5 = (float)Math.toDegrees(Math.acos(Math.max(-1.0F, Math.min(1.0F, var2))));
         float var6 = (float)Math.toDegrees(Math.atan2(var1, var3));
         var0.rotY(var6);
         var0.rotX(var5);
      }
   }

   static void chainLinks(G var0, String var1, float[] var2, float[] var3, float var4) {
      float var5 = var3[0] - var2[0];
      float var6 = var3[1] - var2[1];
      float var7 = var3[2] - var2[2];
      float var8 = (float)Math.sqrt(var5 * var5 + var6 * var6 + var7 * var7);
      int var9 = Math.max(1, Math.round(var8 / (var4 * 1.35F)));

      for (int var10 = 0; var10 < var9; var10++) {
         float var11 = (var10 + 0.5F) / var9;
         var0.push();
         var0.translate(var2[0] + var5 * var11, var2[1] + var6 * var11, var2[2] + var7 * var11);
         alignY(var0, var5, var6, var7);
         var0.rotY(var10 % 2 == 0 ? 0.0F : 90.0F);
         var0.rotZ(90.0F);
         var0.scale(1.0F, 1.0F, 1.6F);
         var0.torus(var1, var4 * 0.45F, var4 * 0.14F, 8, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }
   }

   static void chainPath(G var0, String var1, float[][] var2, float var3) {
      for (int var4 = 0; var4 + 1 < var2.length; var4++) {
         chainLinks(var0, var1, var2[var4], var2[var4 + 1], var3);
      }
   }

   static float[][] sag(float[] var0, float[] var1, float var2, int var3) {
      float[][] var4 = new float[var3 + 1][];

      for (int var5 = 0; var5 <= var3; var5++) {
         float var6 = (float)var5 / var3;
         float var7 = 4.0F * var6 * (1.0F - var6);
         var4[var5] = new float[]{
            var0[0] + (var1[0] - var0[0]) * var6, var0[1] + (var1[1] - var0[1]) * var6 - var2 * var7, var0[2] + (var1[2] - var0[2]) * var6
         };
      }

      return var4;
   }

   static void arc(G var0, float[] var1, float[] var2, float var3, float var4, int var5, float var6, int var7) {
      int var8 = (int)Math.floor(var6 * 14.0F);
      Random var9 = new Random(var5 * 7919L + var8 * 104729L);
      byte var10 = 7;
      float[][] var11 = new float[var10 + 1][];

      for (int var12 = 0; var12 <= var10; var12++) {
         float var13 = (float)var12 / var10;
         float var14 = var12 != 0 && var12 != var10 ? var3 : 0.0F;
         var11[var12] = new float[]{
            var1[0] + (var2[0] - var1[0]) * var13 + (var9.nextFloat() - 0.5F) * var14,
            var1[1] + (var2[1] - var1[1]) * var13 + (var9.nextFloat() - 0.5F) * var14,
            var1[2] + (var2[2] - var1[2]) * var13 + (var9.nextFloat() - 0.5F) * var14
         };
      }

      boolean var15 = var0.glow;
      int var16 = var0.color;
      var0.glow(true);
      var0.color(var7);
      float[] var17 = new float[var10 + 1];
      Arrays.fill(var17, var4 * 2.6F);
      Geo.chain(var0, "c3_beam", var11, var17, 4);
      var0.color(-1);
      Arrays.fill(var17, var4);
      Geo.chain(var0, "white", var11, var17, 4);
      var0.glow(var15);
      var0.color(var16);
   }

   static void halo(G var0, float var1, float var2, float var3, float var4, int var5) {
      boolean var6 = var0.glow;
      int var7 = var0.color;
      var0.glow(true).color(var5);
      Geo.sprite3(var0, "orb", var1, var2, var3, var4);
      var0.glow(var6).color(var7);
   }

   static float[][] skinUV(int var0, int var1, int var2, int var3, int var4) {
      float var5 = 0.015625F;
      return new float[][]{
         {(var0 + var4 + var2) * var5, (var1 + var4) * var5, (var0 + var4) * var5, (var1 + var4 + var3) * var5},
         {(var0 + 2 * var4 + 2 * var2) * var5, (var1 + var4) * var5, (var0 + 2 * var4 + var2) * var5, (var1 + var4 + var3) * var5},
         {(var0 + 2 * var4 + var2) * var5, (var1 + var4) * var5, (var0 + var4 + var2) * var5, (var1 + var4 + var3) * var5},
         {(var0 + var4) * var5, (var1 + var4) * var5, var0 * var5, (var1 + var4 + var3) * var5},
         {(var0 + var4 + var2) * var5, var1 * var5, (var0 + var4) * var5, (var1 + var4) * var5},
         {(var0 + var4 + 2 * var2) * var5, var1 * var5, (var0 + var4 + var2) * var5, (var1 + var4) * var5}
      };
   }

   static void skinBox(
      G var0,
      String var1,
      float var2,
      float var3,
      float var4,
      float var5,
      float var6,
      float var7,
      int var8,
      int var9,
      int var10,
      int var11,
      int var12,
      float var13
   ) {
      var0.box6(var1, var2 - var13, var3 - var13, var4 - var13, var5 + var13, var6 + var13, var7 + var13, skinUV(var8, var9, var10, var11, var12));
   }

   static void miniPlayer(G var0, String var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      int var9 = var0.color;
      var0.color(-1);
      var0.push();
      var0.rotY(var2);
      var0.rotX(var3);
      skinBox(var0, var1, -4.0F, 0.0F, -4.0F, 4.0F, 8.0F, 4.0F, 0, 0, 8, 8, 8, 0.0F);
      skinBox(var0, var1, -4.0F, 0.0F, -4.0F, 4.0F, 8.0F, 4.0F, 32, 0, 8, 8, 8, 0.5F);
      var0.pop();
      skinBox(var0, var1, -4.0F, -12.0F, -2.0F, 4.0F, 0.0F, 2.0F, 16, 16, 8, 12, 4, 0.0F);
      skinBox(var0, var1, -4.0F, -12.0F, -2.0F, 4.0F, 0.0F, 2.0F, 16, 32, 8, 12, 4, 0.25F);

      for (byte var10 = 1; var10 >= -1; var10 -= 2) {
         var0.push();
         var0.translate(var10 * 5, -2.0F, 0.0F);
         var0.rotZ(var10 * (var10 > 0 ? var6 : var7));
         var0.rotX(-(var10 > 0 ? var4 : var5));
         if (var10 > 0) {
            skinBox(var0, var1, -1.0F, -10.0F, -2.0F, 3.0F, 2.0F, 2.0F, 40, 16, 4, 12, 4, 0.0F);
            skinBox(var0, var1, -1.0F, -10.0F, -2.0F, 3.0F, 2.0F, 2.0F, 40, 32, 4, 12, 4, 0.25F);
         } else {
            skinBox(var0, var1, -3.0F, -10.0F, -2.0F, 1.0F, 2.0F, 2.0F, 32, 48, 4, 12, 4, 0.0F);
            skinBox(var0, var1, -3.0F, -10.0F, -2.0F, 1.0F, 2.0F, 2.0F, 48, 48, 4, 12, 4, 0.25F);
         }

         var0.pop();
      }

      for (byte var11 = 1; var11 >= -1; var11 -= 2) {
         var0.push();
         var0.translate(var11 * 2, -12.0F, 0.0F);
         var0.rotX(var11 * var8);
         if (var11 > 0) {
            skinBox(var0, var1, -2.0F, -12.0F, -2.0F, 2.0F, 0.0F, 2.0F, 0, 16, 4, 12, 4, 0.0F);
            skinBox(var0, var1, -2.0F, -12.0F, -2.0F, 2.0F, 0.0F, 2.0F, 0, 32, 4, 12, 4, 0.25F);
         } else {
            skinBox(var0, var1, -2.0F, -12.0F, -2.0F, 2.0F, 0.0F, 2.0F, 16, 48, 4, 12, 4, 0.0F);
            skinBox(var0, var1, -2.0F, -12.0F, -2.0F, 2.0F, 0.0F, 2.0F, 0, 48, 4, 12, 4, 0.25F);
         }

         var0.pop();
      }

      var0.color(var9);
   }
}
