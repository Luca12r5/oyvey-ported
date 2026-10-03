package dev.lego.cosmetic;

import java.util.Arrays;
import java.util.TreeSet;

final class VehGeo {
   static final float[] UV = new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F};
   static final float[] UP = new float[]{0.0F, 1.0F, 0.0F};

   private VehGeo() {
   }

   static float[] p(float var0, float var1, float var2) {
      return new float[]{var0, var1, var2};
   }

   static int argb(int var0, int var1) {
      return Math.max(0, Math.min(255, var0)) << 24 | var1 & 16777215;
   }

   static float[] lerp(float[] var0, float[] var1, float var2) {
      return p(lerp(var0[0], var1[0], var2), lerp(var0[1], var1[1], var2), lerp(var0[2], var1[2], var2));
   }

   static void steer(G var0, float[] var1, float var2, float var3) {
      var0.translate(var1[0], var1[1], var1[2]);
      var0.rotX(-var2);
      var0.rotY(var3);
      var0.rotX(var2);
      var0.translate(-var1[0], -var1[1], -var1[2]);
   }

   static float sin(double var0) {
      return (float)Math.sin(var0);
   }

   static float cos(double var0) {
      return (float)Math.cos(var0);
   }

   static float clamp(float var0, float var1, float var2) {
      return var0 < var1 ? var1 : (var0 > var2 ? var2 : var0);
   }

   static float lerp(float var0, float var1, float var2) {
      return var0 + (var1 - var0) * var2;
   }

   static float smooth(float var0, float var1, float var2) {
      float var3 = clamp((var2 - var0) / (var1 - var0), 0.0F, 1.0F);
      return var3 * var3 * (3.0F - 2.0F * var3);
   }

   private static boolean same(float[] var0, float[] var1) {
      return Math.abs(var0[0] - var1[0]) < 1.0E-5F && Math.abs(var0[1] - var1[1]) < 1.0E-5F && Math.abs(var0[2] - var1[2]) < 1.0E-5F;
   }

   private static float[] sub(float[] var0, float[] var1) {
      return new float[]{var0[0] - var1[0], var0[1] - var1[1], var0[2] - var1[2]};
   }

   private static float[] cross(float[] var0, float[] var1) {
      return new float[]{var0[1] * var1[2] - var0[2] * var1[1], var0[2] * var1[0] - var0[0] * var1[2], var0[0] * var1[1] - var0[1] * var1[0]};
   }

   private static float dot(float[] var0, float[] var1) {
      return var0[0] * var1[0] + var0[1] * var1[1] + var0[2] * var1[2];
   }

   private static float[] norm(float[] var0) {
      float var1 = (float)Math.sqrt(dot(var0, var0));
      return var1 < 1.0E-9F ? new float[]{0.0F, 1.0F, 0.0F} : new float[]{var0[0] / var1, var0[1] / var1, var0[2] / var1};
   }

   static void q(G var0, String var1, float[] var2, float[] var3, float[] var4, float[] var5, float[] var6) {
      float[][] var7 = new float[][]{var2, var3, var4, var5};

      for (int var8 = 0; var8 < 4; var8++) {
         float[] var9 = var7[var8];
         float[] var10 = var7[var8 + 1 & 3];
         float[] var11 = var7[var8 + 3 & 3];
         if (!same(var9, var10) && !same(var9, var11) && !same(var10, var11)) {
            float[] var12 = cross(sub(var10, var9), sub(var11, var9));
            if (!(dot(var12, var12) < 1.0E-12F)) {
               int var13 = var8 + 1 & 3;
               int var14 = var8 + 2 & 3;
               int var15 = var8 + 3 & 3;
               var0.quad(
                  var1,
                  var7[var8],
                  var7[var13],
                  var7[var14],
                  var7[var15],
                  new float[]{
                     var6[var8 * 2],
                     var6[var8 * 2 + 1],
                     var6[var13 * 2],
                     var6[var13 * 2 + 1],
                     var6[var14 * 2],
                     var6[var14 * 2 + 1],
                     var6[var15 * 2],
                     var6[var15 * 2 + 1]
                  }
               );
               return;
            }
         }
      }
   }

   static void q(G var0, String var1, float[] var2, float[] var3, float[] var4, float[] var5) {
      q(var0, var1, var2, var3, var4, var5, UV);
   }

   static void qo(G var0, String var1, float[] var2, float[] var3, float[] var4, float[] var5, float[] var6, float[] var7) {
      float[] var8 = cross(sub(var4, var2), sub(var5, var3));
      float[] var9 = new float[]{
         (var2[0] + var3[0] + var4[0] + var5[0]) / 4.0F, (var2[1] + var3[1] + var4[1] + var5[1]) / 4.0F, (var2[2] + var3[2] + var4[2] + var5[2]) / 4.0F
      };
      if (dot(var8, sub(var9, var6)) >= 0.0F) {
         q(var0, var1, var2, var3, var4, var5, var7);
      } else {
         q(var0, var1, var2, var5, var4, var3, new float[]{var7[0], var7[1], var7[6], var7[7], var7[4], var7[5], var7[2], var7[3]});
      }
   }

   static void tri(G var0, String var1, float[] var2, float[] var3, float[] var4) {
      q(var0, var1, var2, var3, var4, var4, new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F});
   }

   static void box(G var0, String var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      float[] var8 = new float[]{(var2 + var5) / 2.0F, (var3 + var6) / 2.0F, (var4 + var7) / 2.0F};
      float[][] var9 = new float[8][];

      for (int var10 = 0; var10 < 8; var10++) {
         var9[var10] = p((var10 & 1) == 0 ? var2 : var5, (var10 & 2) == 0 ? var3 : var6, (var10 & 4) == 0 ? var4 : var7);
      }

      int[][] var15 = new int[][]{{0, 1, 3, 2}, {4, 5, 7, 6}, {0, 2, 6, 4}, {1, 3, 7, 5}, {0, 1, 5, 4}, {2, 3, 7, 6}};

      for (int[] var14 : var15) {
         qo(var0, var1, var9[var14[0]], var9[var14[1]], var9[var14[2]], var9[var14[3]], var8, UV);
      }
   }

   static void box6(G var0, String var1, String[] var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      float[] var10000 = new float[]{(var3 + var6) / 2.0F, (var4 + var7) / 2.0F, (var5 + var8) / 2.0F};
      float[][] var10 = new float[8][];

      for (int var11 = 0; var11 < 8; var11++) {
         var10[var11] = p((var11 & 1) == 0 ? var3 : var6, (var11 & 2) == 0 ? var4 : var7, (var11 & 4) == 0 ? var5 : var8);
      }

      int[][] var15 = new int[][]{{4, 5, 7, 6}, {1, 0, 2, 3}, {0, 4, 6, 2}, {5, 1, 3, 7}, {6, 7, 3, 2}, {0, 1, 5, 4}};

      for (int var12 = 0; var12 < 6; var12++) {
         String var13 = var2 != null && var2[var12] != null ? var2[var12] : var1;
         int[] var14 = var15[var12];
         q(
            var0,
            var13,
            var10[var14[0]],
            var10[var14[1]],
            var10[var14[2]],
            var10[var14[3]],
            var12 == 4 ? new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F} : new float[]{1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F}
         );
      }
   }

   static void rbox(G var0, String var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      var8 = Math.min(var8, Math.min(var5 - var2, Math.min(var6 - var3, var7 - var4)) * 0.49F);
      float[] var9 = new float[]{var2 + var8, var3 + var8, var4 + var8};
      float[] var10 = new float[]{var5 - var8, var6 - var8, var7 - var8};
      float[] var11 = new float[]{(var2 + var5) / 2.0F, (var3 + var6) / 2.0F, (var4 + var7) / 2.0F};
      int[][] var12 = new int[][]{{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
      float[] var13 = new float[]{0.3F, 0.3F, 0.7F, 0.3F, 0.7F, 0.7F, 0.3F, 0.7F};

      for (int var14 = 0; var14 < 3; var14++) {
         for (byte var15 = -1; var15 <= 1; var15 += 2) {
            int var16 = (var14 + 1) % 3;
            int var17 = (var14 + 2) % 3;
            float[][] var18 = new float[4][];

            for (int var19 = 0; var19 < 4; var19++) {
               float[] var20 = new float[3];
               var20[var14] = (var15 < 0 ? var9[var14] : var10[var14]) + var15 * var8;
               var20[var16] = var12[var19][0] < 0 ? var9[var16] : var10[var16];
               var20[var17] = var12[var19][1] < 0 ? var9[var17] : var10[var17];
               var18[var19] = var20;
            }

            qo(var0, var1, var18[0], var18[1], var18[2], var18[3], var11, UV);
         }
      }

      if (!(var8 <= 1.0E-4F)) {
         for (int var24 = 0; var24 < 3; var24++) {
            for (int var26 = var24 + 1; var26 < 3; var26++) {
               int var28 = 3 - var24 - var26;

               for (byte var30 = -1; var30 <= 1; var30 += 2) {
                  for (byte var32 = -1; var32 <= 1; var32 += 2) {
                     float[][] var34 = new float[4][];
                     int[] var36 = new int[]{-1, 1, 1, -1};

                     for (int var21 = 0; var21 < 4; var21++) {
                        float[] var22 = new float[3];
                        var22[var24] = var30 < 0 ? var9[var24] : var10[var24];
                        var22[var26] = var32 < 0 ? var9[var26] : var10[var26];
                        var22[var28] = var36[var21] < 0 ? var9[var28] : var10[var28];
                        if (var21 < 2) {
                           var22[var24] += var30 * var8;
                        } else {
                           var22[var26] += var32 * var8;
                        }

                        var34[var21] = var22;
                     }

                     qo(var0, var1, var34[0], var34[1], var34[2], var34[3], var11, var13);
                  }
               }
            }
         }

         for (byte var25 = -1; var25 <= 1; var25 += 2) {
            for (byte var27 = -1; var27 <= 1; var27 += 2) {
               for (byte var29 = -1; var29 <= 1; var29 += 2) {
                  float var31 = var25 < 0 ? var9[0] : var10[0];
                  float var33 = var27 < 0 ? var9[1] : var10[1];
                  float var35 = var29 < 0 ? var9[2] : var10[2];
                  float[] var37 = p(var31 + var25 * var8, var33, var35);
                  float[] var38 = p(var31, var33 + var27 * var8, var35);
                  float[] var39 = p(var31, var33, var35 + var29 * var8);
                  qo(var0, var1, var37, var38, var39, var39, var11, var13);
               }
            }
         }
      }
   }

   static void beam(G var0, String var1, float[] var2, float[] var3, float var4, float var5, float var6, float var7, float[] var8) {
      float[] var9 = norm(sub(var3, var2));
      float[] var10 = cross(var9, var8);
      if (dot(var10, var10) < 1.0E-8F) {
         var10 = cross(var9, new float[]{1.0F, 0.0F, 0.0F});
      }

      if (dot(var10, var10) < 1.0E-8F) {
         var10 = cross(var9, new float[]{0.0F, 0.0F, 1.0F});
      }

      var10 = norm(var10);
      float[] var11 = norm(cross(var10, var9));
      float[][] var12 = new float[8][];

      for (int var13 = 0; var13 < 8; var13++) {
         float[] var14 = (var13 & 4) == 0 ? var2 : var3;
         float var15 = ((var13 & 4) == 0 ? var4 : var6) / 2.0F;
         float var16 = ((var13 & 4) == 0 ? var5 : var7) / 2.0F;
         float var17 = (var13 & 1) == 0 ? -var15 : var15;
         float var18 = (var13 & 2) == 0 ? -var16 : var16;
         var12[var13] = p(
            var14[0] + var10[0] * var17 + var11[0] * var18, var14[1] + var10[1] * var17 + var11[1] * var18, var14[2] + var10[2] * var17 + var11[2] * var18
         );
      }

      float[] var20 = new float[]{(var2[0] + var3[0]) / 2.0F, (var2[1] + var3[1]) / 2.0F, (var2[2] + var3[2]) / 2.0F};
      int[][] var21 = new int[][]{{0, 1, 3, 2}, {4, 5, 7, 6}, {0, 2, 6, 4}, {1, 3, 7, 5}, {0, 1, 5, 4}, {2, 3, 7, 6}};

      for (int[] var25 : var21) {
         qo(var0, var1, var12[var25[0]], var12[var25[1]], var12[var25[2]], var12[var25[3]], var20, UV);
      }
   }

   static void beam(G var0, String var1, float[] var2, float[] var3, float var4, float var5) {
      beam(var0, var1, var2, var3, var4, var5, var4, var5, UP);
   }

   static void tube(G var0, String var1, float[] var2, float[] var3, float var4, float var5, int var6, boolean var7) {
      float[] var8 = norm(sub(var3, var2));
      float[] var9 = Math.abs(var8[1]) < 0.9F ? new float[]{0.0F, 1.0F, 0.0F} : new float[]{1.0F, 0.0F, 0.0F};
      float[] var10 = norm(cross(var8, var9));
      float[] var11 = cross(var8, var10);
      float[][] var12 = new float[var6][];
      float[][] var13 = new float[var6][];

      for (int var14 = 0; var14 < var6; var14++) {
         double var15 = (Math.PI * 2) * var14 / var6;
         float var17 = (float)Math.cos(var15);
         float var18 = (float)Math.sin(var15);
         float var19 = var10[0] * var17 + var11[0] * var18;
         float var20 = var10[1] * var17 + var11[1] * var18;
         float var21 = var10[2] * var17 + var11[2] * var18;
         var12[var14] = p(var2[0] + var19 * var4, var2[1] + var20 * var4, var2[2] + var21 * var4);
         var13[var14] = p(var3[0] + var19 * var5, var3[1] + var20 * var5, var3[2] + var21 * var5);
      }

      for (int var22 = 0; var22 < var6; var22++) {
         int var24 = (var22 + 1) % var6;
         float[] var10000 = new float[]{(var2[0] + var3[0]) / 2.0F, (var2[1] + var3[1]) / 2.0F, (var2[2] + var3[2]) / 2.0F};
         float[] var26 = new float[]{
            (var12[var22][0] + var12[var24][0] + var13[var22][0] + var13[var24][0]) / 4.0F,
            (var12[var22][1] + var12[var24][1] + var13[var22][1] + var13[var24][1]) / 4.0F,
            (var12[var22][2] + var12[var24][2] + var13[var22][2] + var13[var24][2]) / 4.0F
         };
         float var28 = dot(sub(var26, var2), var8);
         float[] var29 = new float[]{var2[0] + var8[0] * var28, var2[1] + var8[1] * var28, var2[2] + var8[2] * var28};
         qo(
            var0,
            var1,
            var12[var22],
            var12[var24],
            var13[var24],
            var13[var22],
            var29,
            new float[]{(float)var22 / var6, 0.0F, (float)(var22 + 1) / var6, 0.0F, (float)(var22 + 1) / var6, 1.0F, (float)var22 / var6, 1.0F}
         );
      }

      if (var7) {
         for (int var23 = 0; var23 < var6; var23++) {
            int var25 = (var23 + 1) % var6;
            float[] var16 = new float[]{var2[0] + var8[0], var2[1] + var8[1], var2[2] + var8[2]};
            float[] var27 = new float[]{var3[0] - var8[0], var3[1] - var8[1], var3[2] - var8[2]};
            if (var4 > 0.001F) {
               qo(var0, var1, var2, var12[var23], var12[var25], var12[var25], var16, UV);
            }

            if (var5 > 0.001F) {
               qo(var0, var1, var3, var13[var23], var13[var25], var13[var25], var27, UV);
            }
         }
      }
   }

   static void pipe(G var0, String var1, float[][] var2, float var3, int var4, boolean var5) {
      float[][] var6 = new float[var4][];

      for (int var7 = 0; var7 < var4; var7++) {
         var6[var7] = new float[]{cos((Math.PI * 2) * var7 / var4) * var3, sin((Math.PI * 2) * var7 / var4) * var3};
      }

      sweep(var0, var1, var2, var6, null, var5);
   }

   static void strip(G var0, String var1, float[][] var2, float var3, float var4, float[] var5, boolean var6) {
      float[][] var7 = new float[][]{{-var3 / 2.0F, -var4 / 2.0F}, {var3 / 2.0F, -var4 / 2.0F}, {var3 / 2.0F, var4 / 2.0F}, {-var3 / 2.0F, var4 / 2.0F}};
      sweep(var0, var1, var2, var7, var5, var6);
   }

   static void sweep(G var0, String var1, float[][] var2, float[][] var3, float[] var4, boolean var5) {
      int var6 = var2.length;
      int var7 = var3.length;
      float[][][] var8 = new float[var6][var7][];
      float[][] var9 = new float[var6][];
      float[] var10 = null;

      for (int var11 = 0; var11 < var6; var11++) {
         float[] var12 = var2[Math.max(0, var11 - 1)];
         float[] var13 = var2[Math.min(var6 - 1, var11 + 1)];
         float[] var14 = norm(sub(var13, var12));
         float[] var15;
         if (var4 == null && var10 != null) {
            float var30 = dot(var10, var14);
            var15 = p(var10[0] - var14[0] * var30, var10[1] - var14[1] * var30, var10[2] - var14[2] * var30);
            if (dot(var15, var15) < 1.0E-6F) {
               var15 = cross(var14, new float[]{0.0F, 0.0F, 1.0F});
            }
         } else {
            float[] var16 = var4;
            if (var4 == null) {
               var16 = Math.abs(var14[1]) < 0.8F ? new float[]{0.0F, 1.0F, 0.0F} : new float[]{1.0F, 0.0F, 0.0F};
            }

            var15 = cross(var14, var16);
            if (dot(var15, var15) < 1.0E-6F) {
               var15 = cross(var14, new float[]{0.0F, 0.0F, 1.0F});
            }
         }

         var15 = norm(var15);
         var10 = var15;
         float[] var31 = norm(cross(var15, var14));
         float var17 = 1.0F;
         if (var11 > 0 && var11 < var6 - 1) {
            float[] var18 = norm(sub(var2[var11], var2[var11 - 1]));
            float[] var19 = norm(sub(var2[var11 + 1], var2[var11]));
            float var20 = dot(var18, var19);
            var17 = 1.0F / (float)Math.max(0.5, Math.sqrt((1.0F + var20) / 2.0F));
         }

         var9[var11] = var2[var11];

         for (int var32 = 0; var32 < var7; var32++) {
            float var33 = var3[var32][0];
            float var34 = var3[var32][1] * 1.0F;
            var8[var11][var32] = p(
               var2[var11][0] + (var15[0] * var33 + var31[0] * var34) * var17,
               var2[var11][1] + (var15[1] * var33 + var31[1] * var34) * var17,
               var2[var11][2] + (var15[2] * var33 + var31[2] * var34) * var17
            );
         }
      }

      for (int var21 = 0; var21 + 1 < var6; var21++) {
         float[] var23 = new float[]{
            (var9[var21][0] + var9[var21 + 1][0]) / 2.0F, (var9[var21][1] + var9[var21 + 1][1]) / 2.0F, (var9[var21][2] + var9[var21 + 1][2]) / 2.0F
         };

         for (int var25 = 0; var25 < var7; var25++) {
            int var27 = (var25 + 1) % var7;
            qo(
               var0,
               var1,
               var8[var21][var25],
               var8[var21][var27],
               var8[var21 + 1][var27],
               var8[var21 + 1][var25],
               var23,
               new float[]{
                  (float)var25 / var7,
                  (float)var21 / (var6 - 1),
                  (float)(var25 + 1) / var7,
                  (float)var21 / (var6 - 1),
                  (float)(var25 + 1) / var7,
                  (float)(var21 + 1) / (var6 - 1),
                  (float)var25 / var7,
                  (float)(var21 + 1) / (var6 - 1)
               }
            );
         }
      }

      if (var5) {
         float[] var22 = var2[1];
         float[] var24 = var2[var6 - 2];

         for (int var26 = 0; var26 < var7; var26++) {
            int var28 = (var26 + 1) % var7;
            qo(var0, var1, var2[0], var8[0][var26], var8[0][var28], var8[0][var28], var22, UV);
            qo(var0, var1, var2[var6 - 1], var8[var6 - 1][var26], var8[var6 - 1][var28], var8[var6 - 1][var28], var24, UV);
         }
      }
   }

   static float[][] arc(float var0, float var1, float var2, float var3, float var4, float var5, int var6) {
      float[][] var7 = new float[var6 + 1][];

      for (int var8 = 0; var8 <= var6; var8++) {
         double var9 = Math.toRadians(var4 + (var5 - var4) * var8 / var6);
         var7[var8] = p(var0, var1 + (float)Math.sin(var9) * var3, var2 + (float)Math.cos(var9) * var3);
      }

      return var7;
   }

   static void torus(G var0, String var1, float var2, float var3, int var4, int var5) {
      float[][] var6 = new float[var5 + 1][];

      for (int var7 = 0; var7 <= var5; var7++) {
         double var8 = (-Math.PI / 2) + (Math.PI * 2) * var7 / var5;
         var6[var7] = new float[]{var2 + (float)Math.cos(var8) * var3, (float)Math.sin(var8) * var3};
      }

      lathe(var0, var1, var6, var4);
   }

   static void ball(G var0, String var1, float var2, float var3, float var4, float var5, int var6, int var7) {
      var0.push();
      var0.translate(var2, var3, var4);
      var0.sphere(var1, var5, var6, var7, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
   }

   static void ellipsoid(G var0, String var1, float var2, float var3, float var4, float var5, float var6, float var7, int var8, int var9) {
      var0.push();
      var0.translate(var2, var3, var4);
      var0.scale(var5, var6, var7);
      var0.sphere(var1, 1.0F, var8, var9, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
   }

   static void lathe(G var0, String[] var1, float[][] var2, int var3) {
      int var4 = var2.length;
      float var5 = 0.0F;
      float[] var6 = new float[var4];

      for (int var7 = 1; var7 < var4; var7++) {
         float var8 = var2[var7][0] - var2[var7 - 1][0];
         float var9 = var2[var7][1] - var2[var7 - 1][1];
         var5 += (float)Math.sqrt(var8 * var8 + var9 * var9);
         var6[var7] = var5;
      }

      for (int var26 = 0; var26 + 1 < var4; var26++) {
         String var27 = var1[Math.min(var26, var1.length - 1)];
         float var28 = var5 > 0.0F ? var6[var26] / var5 : 0.0F;
         float var10 = var5 > 0.0F ? var6[var26 + 1] / var5 : 1.0F;

         for (int var11 = 0; var11 < var3; var11++) {
            double var12 = (Math.PI * 2) * var11 / var3;
            double var14 = (Math.PI * 2) * (var11 + 1) / var3;
            float var16 = (float)Math.cos(var12);
            float var17 = (float)Math.sin(var12);
            float var18 = (float)Math.cos(var14);
            float var19 = (float)Math.sin(var14);
            float var20 = var2[var26][0];
            float var21 = var2[var26][1];
            float var22 = var2[var26 + 1][0];
            float var23 = var2[var26 + 1][1];
            float var24 = (float)var11 / var3;
            float var25 = (float)(var11 + 1) / var3;
            q(
               var0,
               var27,
               p(var16 * var22, var23, var17 * var22),
               p(var18 * var22, var23, var19 * var22),
               p(var18 * var20, var21, var19 * var20),
               p(var16 * var20, var21, var17 * var20),
               new float[]{var24, var10, var25, var10, var25, var28, var24, var28}
            );
         }
      }
   }

   static void lathe(G var0, String var1, float[][] var2, int var3) {
      lathe(var0, new String[]{var1}, var2, var3);
   }

   static void loft(G var0, float[] var1, VehGeo.Sec var2, VehGeo.TexFn var3, boolean var4, boolean var5, float var6, float var7) {
      loft(var0, var1, var2, var3, var4, var5, var6, var7, false);
   }

   static void loft(G var0, float[] var1, VehGeo.Sec var2, VehGeo.TexFn var3, boolean var4, boolean var5, float var6, float var7, boolean var8) {
      loft(var0, var1, var2, var3, var4, var5, var6, var7, var8, false);
   }

   static void loft(G var0, float[] var1, VehGeo.Sec var2, VehGeo.TexFn var3, boolean var4, boolean var5, float var6, float var7, boolean var8, boolean var9) {
      int var10 = var8 ? 0 : 1;
      float[][][] var11 = new float[var1.length][][];

      for (int var12 = 0; var12 < var1.length; var12++) {
         var11[var12] = var2.at(var1[var12]);
      }

      float var25 = var1[0];
      float var13 = var1[var1.length - 1];

      for (int var14 = 0; var14 + 1 < var1.length; var14++) {
         float[][] var15 = var11[var14];
         float[][] var16 = var11[var14 + 1];
         int var17 = var15.length;

         for (int var18 = 0; var18 < var17; var18++) {
            int var19 = (var18 + 1) % var17;
            String var20 = var3.at(var14, var18, var1[var14], var15[var18], var15[var19]);
            if (var20 != null) {
               float[] var21 = p(var15[var18][0], var15[var18][1], var1[var14]);
               float[] var22 = p(var15[var19][0], var15[var19][1], var1[var14]);
               float[] var23 = p(var16[var19][0], var16[var19][1], var1[var14 + 1]);
               float[] var24 = p(var16[var18][0], var16[var18][1], var1[var14 + 1]);
               q(
                  var0,
                  var20,
                  var21,
                  var22,
                  var23,
                  var24,
                  new float[]{
                     uvU(var1[var14], var25, var13),
                     uvV(var21[var10], var6, var7),
                     uvU(var1[var14], var25, var13),
                     uvV(var22[var10], var6, var7),
                     uvU(var1[var14 + 1], var25, var13),
                     uvV(var23[var10], var6, var7),
                     uvU(var1[var14 + 1], var25, var13),
                     uvV(var24[var10], var6, var7)
                  }
               );
            }
         }
      }

      if (var9) {
         if (var4) {
            pairCap(var0, var11[0], var1[0], var3.at(-1, 0, var1[0], var11[0][0], var11[0][0]), 1);
         }

         if (var5) {
            pairCap(var0, var11[var1.length - 1], var1[var1.length - 1], var3.at(var1.length - 1, 0, var1[var1.length - 1], var11[0][0], var11[0][0]), -1);
         }
      } else {
         if (var4) {
            cap(var0, var11[0], var1[0], var3.at(-1, 0, var1[0], var11[0][0], var11[0][0]), false, var6, var7);
         }

         if (var5) {
            cap(
               var0,
               var11[var1.length - 1],
               var1[var1.length - 1],
               var3.at(var1.length - 1, 0, var1[var1.length - 1], var11[0][0], var11[0][0]),
               true,
               var6,
               var7
            );
         }
      }
   }

   private static void pairCap(G var0, float[][] var1, float var2, String var3, int var4) {
      if (var3 != null) {
         int var5 = var1.length;

         for (int var6 = 0; var6 < var5 / 2 - 1; var6++) {
            float[] var7 = p(var1[var6][0], var1[var6][1], var2);
            float[] var8 = p(var1[var6 + 1][0], var1[var6 + 1][1], var2);
            float[] var9 = p(var1[var5 - 2 - var6][0], var1[var5 - 2 - var6][1], var2);
            float[] var10 = p(var1[var5 - 1 - var6][0], var1[var5 - 1 - var6][1], var2);
            float[] var11 = new float[]{(var7[0] + var8[0] + var9[0] + var10[0]) / 4.0F, (var7[1] + var8[1] + var9[1] + var10[1]) / 4.0F, var2 + var4};
            qo(var0, var3, var7, var8, var9, var10, var11, UV);
         }
      }
   }

   private static float uvU(float var0, float var1, float var2) {
      return var2 == var1 ? 0.0F : (var0 - var1) / (var2 - var1);
   }

   private static float uvV(float var0, float var1, float var2) {
      return clamp((var1 - var0) / (var1 - var2), 0.0F, 1.0F);
   }

   private static void cap(G var0, float[][] var1, float var2, String var3, boolean var4, float var5, float var6) {
      if (var3 != null) {
         float var7 = 0.0F;
         float var8 = 0.0F;

         for (float[] var12 : var1) {
            var7 += var12[0];
            var8 += var12[1];
         }

         var7 /= var1.length;
         var8 /= var1.length;
         float[] var16 = p(var7, var8, var2);

         for (int var17 = 0; var17 < var1.length; var17++) {
            float[] var18 = p(var1[var17][0], var1[var17][1], var2);
            float[] var19 = p(var1[(var17 + 1) % var1.length][0], var1[(var17 + 1) % var1.length][1], var2);
            float[] var13 = new float[]{
               0.5F, uvV(var8, var5, var6), 0.5F, uvV(var18[1], var5, var6), 0.5F, uvV(var19[1], var5, var6), 0.5F, uvV(var19[1], var5, var6)
            };
            if (var4) {
               q(var0, var3, var16, var18, var19, var19, var13);
            } else {
               q(var0, var3, var16, var19, var18, var18, new float[]{var13[0], var13[1], var13[4], var13[5], var13[2], var13[3], var13[2], var13[3]});
            }
         }
      }
   }

   static float[][] mirrorRing(float[][] var0) {
      int var1 = var0.length;
      float[][] var2 = new float[var1 * 2 - 2][];

      for (int var3 = 0; var3 < var1; var3++) {
         var2[var3] = new float[]{var0[var3][0], var0[var3][1]};
      }

      for (int var4 = var1 - 2; var4 >= 1; var4--) {
         var2[var1 + (var1 - 2 - var4)] = new float[]{-var0[var4][0], var0[var4][1]};
      }

      return var2;
   }

   static float curve(float var0, float[] var1, float[] var2) {
      int var3 = var1.length;
      if (var0 <= var1[0]) {
         return var2[0];
      } else if (var0 >= var1[var3 - 1]) {
         return var2[var3 - 1];
      } else {
         int var4 = 0;

         while (var4 < var3 - 2 && var0 > var1[var4 + 1]) {
            var4++;
         }

         float var5 = (var0 - var1[var4]) / (var1[var4 + 1] - var1[var4]);
         float var6 = var2[Math.max(0, var4 - 1)];
         float var7 = var2[var4];
         float var8 = var2[var4 + 1];
         float var9 = var2[Math.min(var3 - 1, var4 + 2)];
         float var10 = (var8 - var6)
            * 0.5F
            * (var1[var4 + 1] - var1[var4])
            / Math.max(1.0E-4F, var1[Math.min(var3 - 1, var4 + 1)] - var1[Math.max(0, var4 - 1)])
            * 2.0F;
         float var11 = (var9 - var7) * 0.5F * (var1[var4 + 1] - var1[var4]) / Math.max(1.0E-4F, var1[Math.min(var3 - 1, var4 + 2)] - var1[var4]) * 2.0F;
         float var12 = var5 * var5;
         float var13 = var12 * var5;
         return (2.0F * var13 - 3.0F * var12 + 1.0F) * var7
            + (var13 - 2.0F * var12 + var5) * var10
            + (-2.0F * var13 + 3.0F * var12) * var8
            + (var13 - var12) * var11;
      }
   }

   static float[] range(float var0, float var1, float var2, float... var3) {
      TreeSet var4 = new TreeSet();
      int var5 = Math.max(1, (int)Math.ceil((var1 - var0) / var2));

      for (int var6 = 0; var6 <= var5; var6++) {
         var4.add(var0 + (var1 - var0) * var6 / var5);
      }

      for (float var9 : var3) {
         if (var9 > var0 && var9 < var1) {
            var4.add(var9);
         }
      }

      float[] var12 = new float[var4.size()];
      int var13 = 0;
      float var14 = Float.NEGATIVE_INFINITY;

      for (float var10 : (Iterable<Float>) (Iterable<?>) (var4)) {
         if (var10 - var14 > 0.05F || var13 == 0) {
            var12[var13++] = var10;
            var14 = var10;
         }
      }

      return Arrays.copyOf(var12, var13);
   }

   static void wheel(G var0, float var1, float var2, float var3, int var4, float var5, String var6, String var7, String var8, String var9, int var10) {
      var0.push();
      var0.rotX(var5);
      var0.rotZ(-90.0F);
      float var11 = var2 / 2.0F;
      float var12 = Math.min(0.9F, (var1 - var3) * 0.45F);
      lathe(
         var0,
         new String[]{var7, var6, var6, var6, var7},
         new float[][]{
            {var3 + 0.05F, -var11 + 0.2F},
            {var1 - var12, -var11},
            {var1, -var11 + var12 * 0.7F},
            {var1, var11 - var12 * 0.7F},
            {var1 - var12, var11},
            {var3 + 0.05F, var11 - 0.2F}
         },
         var10
      );
      float var13 = var11 - 0.12F;
      lathe(var0, var8, new float[][]{{var3 + 0.05F, var11 - 0.2F}, {var3 - 0.1F, var13 + 0.02F}, {var3 - 0.35F, var11 - 0.45F}}, var10);
      lathe(var0, var8, new float[][]{{var3 - 0.35F, var11 - 0.45F}, {var3 - 0.35F, -var11 + 0.3F}}, var10);
      lathe(var0, var7, new float[][]{{var3 - 0.35F, -var11 + 0.45F}, {0.01F, -var11 + 0.45F}}, var10);
      lathe(var0, var7, new float[][]{{0.01F, -var11 + 0.3F}, {var3 - 0.35F, -var11 + 0.3F}, {var3 + 0.05F, -var11 + 0.2F}}, var10);
      if (var9 != null) {
         float var14 = var3 - 0.75F;
         float var15 = (-var11 + 0.45F + (var11 - 0.35F)) / 2.0F;
         float var16 = Math.min(0.25F, (var11 - 0.35F - (-var11 + 0.45F)) / 2.0F - 0.1F);
         lathe(
            var0,
            var9,
            new float[][]{{0.01F, var15 - var16}, {var14, var15 - var16}, {var14, var15 + var16}, {0.01F, var15 + var16}},
            Math.max(8, var10 * 2 / 3)
         );
      }

      float var24 = var11 - 0.35F;
      if (var4 > 0) {
         for (int var25 = 0; var25 < var4; var25++) {
            double var27 = (Math.PI * 2) * var25 / var4;
            float var18 = (float)Math.cos(var27);
            float var19 = (float)Math.sin(var27);
            float var20 = Math.max(0.7F, var3 * 0.22F);
            float var21 = var3 - 0.3F;
            float[] var22 = p(var18 * var20, var24 + 0.1F, var19 * var20);
            float[] var23 = p(var18 * var21, var24 - 0.25F, var19 * var21);
            beam(var0, var8, var22, var23, Math.max(0.35F, var3 * 0.2F), 0.35F, Math.max(0.25F, var3 * 0.1F), 0.3F, p(0.0F, 1.0F, 0.0F));
         }

         float var26 = Math.max(0.8F, var3 * 0.3F);
         lathe(
            var0,
            new String[]{var8, var8, "veh_flat:2A2C33"},
            new float[][]{{var26, var24 - 0.4F}, {var26, var24 + 0.15F}, {var26 * 0.55F, var24 + 0.35F}, {0.01F, var24 + 0.4F}},
            Math.max(8, var10 / 2)
         );
      } else {
         lathe(
            var0,
            new String[]{var8, var8, "veh_flat:2A2C33"},
            new float[][]{{var3 - 0.35F, var11 - 0.45F}, {var3 * 0.55F, var24}, {var3 * 0.3F, var24 + 0.1F}, {0.01F, var24 + 0.3F}},
            var10
         );
      }

      var0.pop();
   }

   interface Sec {
      float[][] at(float var1);
   }

   interface TexFn {
      String at(int var1, int var2, float var3, float[] var4, float[] var5);
   }
}
