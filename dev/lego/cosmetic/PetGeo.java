package dev.lego.cosmetic;

final class PetGeo {
   private PetGeo() {
   }

   static float sin(double var0) {
      return (float)Math.sin(var0);
   }

   static float cos(double var0) {
      return (float)Math.cos(var0);
   }

   static void blob(G var0, String var1, float var2, float var3, float var4, float var5, float var6, float var7, int var8, int var9) {
      var0.push();
      var0.translate(var2, var3, var4);
      var0.scale(var5, var6, var7);
      var0.sphere(var1, 1.0F, Math.max(8, var8), Math.max(4, var9), 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
   }

   static void blob2(G var0, String var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9, int var10) {
      float[][] var11 = new float[(var10 + 1) * (var9 + 1)][];

      for (int var12 = 0; var12 <= var10; var12++) {
         double var13 = Math.PI * var12 / var10 - (Math.PI / 2);
         float var15 = (float)Math.sin(var13);
         float var16 = (float)Math.cos(var13);
         var15 *= var15 > 0.0F ? var6 : var7;

         for (int var17 = 0; var17 <= var9; var17++) {
            double var18 = (Math.PI * 2) * var17 / var9;
            var11[var12 * (var9 + 1) + var17] = new float[]{
               var2 + (float)Math.cos(var18) * var16 * var5, var3 + var15, var4 + (float)Math.sin(var18) * var16 * var8
            };
         }
      }

      for (int var20 = 0; var20 < var10; var20++) {
         for (int var21 = 0; var21 < var9; var21++) {
            float var14 = (float)var21 / var9;
            float var23 = (float)(var21 + 1) / var9;
            float var24 = 1.0F - (float)var20 / var10;
            float var25 = 1.0F - (float)(var20 + 1) / var10;
            var0.quad(
               var1,
               var11[(var20 + 1) * (var9 + 1) + var21],
               var11[(var20 + 1) * (var9 + 1) + var21 + 1],
               var11[var20 * (var9 + 1) + var21 + 1],
               var11[var20 * (var9 + 1) + var21],
               new float[]{var14, var25, var23, var25, var23, var24, var14, var24}
            );
         }
      }
   }

   static void cone(G var0, String var1, float var2, float var3, int var4, boolean var5) {
      for (int var6 = 0; var6 < var4; var6++) {
         double var7 = (Math.PI * 2) * var6 / var4;
         double var9 = (Math.PI * 2) * (var6 + 1) / var4;
         float var11 = cos(var7);
         float var12 = sin(var7);
         float var13 = cos(var9);
         float var14 = sin(var9);
         float var15 = (float)var6 / var4;
         float var16 = (float)(var6 + 1) / var4;
         float[] var17 = new float[]{0.0F, var3, 0.0F};
         var0.quad(
            var1,
            var17,
            var17,
            new float[]{var13 * var2, 0.0F, var14 * var2},
            new float[]{var11 * var2, 0.0F, var12 * var2},
            new float[]{var15, 0.0F, var16, 0.0F, var16, 1.0F, var15, 1.0F}
         );
         if (var5) {
            float[] var18 = new float[]{0.0F, 0.0F, 0.0F};
            var0.quad(
               var1,
               var18,
               new float[]{var11 * var2, 0.0F, var12 * var2},
               new float[]{var13 * var2, 0.0F, var14 * var2},
               var18,
               new float[]{0.5F, 0.9F, var15, 1.0F, var16, 1.0F, 0.5F, 0.9F}
            );
         }
      }
   }

   static void tri(G var0, String var1, float[] var2, float[] var3, float[] var4) {
      var0.quad(var1, var2, var3, var4, var4, new float[]{0.5F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F});
   }

   static void rbox(G var0, String var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      float[] var9 = new float[]{var2 + var8, var3 + var8, var4 + var8};
      float[] var10 = new float[]{var5 - var8, var6 - var8, var7 - var8};
      float[] var11 = new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F};
      float[] var12 = new float[]{0.3F, 0.3F, 0.7F, 0.3F, 0.7F, 0.7F, 0.3F, 0.7F};
      int[][] var13 = new int[][]{{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};

      for (int var14 = 0; var14 < 3; var14++) {
         for (byte var15 = -1; var15 <= 1; var15 += 2) {
            int var16 = (var14 + 1) % 3;
            int var17 = (var14 + 2) % 3;
            float[][] var18 = new float[4][];

            for (int var19 = 0; var19 < 4; var19++) {
               float[] var20 = new float[3];
               var20[var14] = (var15 < 0 ? var9[var14] : var10[var14]) + var15 * var8;
               var20[var16] = var13[var19][0] < 0 ? var9[var16] : var10[var16];
               var20[var17] = var13[var19][1] < 0 ? var9[var17] : var10[var17];
               var18[var19] = var20;
            }

            var0.quad(var1, var18[0], var18[1], var18[2], var18[3], var11);
         }
      }

      for (int var23 = 0; var23 < 3; var23++) {
         for (int var25 = var23 + 1; var25 < 3; var25++) {
            int var27 = 3 - var23 - var25;

            for (byte var29 = -1; var29 <= 1; var29 += 2) {
               for (byte var31 = -1; var31 <= 1; var31 += 2) {
                  float[][] var33 = new float[4][];
                  int[] var35 = new int[]{-1, 1, 1, -1};

                  for (int var21 = 0; var21 < 4; var21++) {
                     float[] var22 = new float[3];
                     var22[var23] = var29 < 0 ? var9[var23] : var10[var23];
                     var22[var25] = var31 < 0 ? var9[var25] : var10[var25];
                     var22[var27] = var35[var21] < 0 ? var9[var27] : var10[var27];
                     if (var21 < 2) {
                        var22[var23] += var29 * var8;
                     } else {
                        var22[var25] += var31 * var8;
                     }

                     var33[var21] = var22;
                  }

                  var0.quad(var1, var33[0], var33[1], var33[2], var33[3], var12);
               }
            }
         }
      }

      for (byte var24 = -1; var24 <= 1; var24 += 2) {
         for (byte var26 = -1; var26 <= 1; var26 += 2) {
            for (byte var28 = -1; var28 <= 1; var28 += 2) {
               float var30 = var24 < 0 ? var9[0] : var10[0];
               float var32 = var26 < 0 ? var9[1] : var10[1];
               float var34 = var28 < 0 ? var9[2] : var10[2];
               tri(
                  var0,
                  var1,
                  new float[]{var30 + var24 * var8, var32, var34},
                  new float[]{var30, var32 + var26 * var8, var34},
                  new float[]{var30, var32, var34 + var28 * var8}
               );
            }
         }
      }
   }

   static void decal(
      G var0,
      String var1,
      float var2,
      float var3,
      float var4,
      float var5,
      float var6,
      float var7,
      float var8,
      float var9,
      float var10,
      float var11,
      float var12,
      float var13,
      float var14,
      float var15
   ) {
      decal(var0, var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, var13, var14, var15, 6, 5);
   }

   static void decal(
      G var0,
      String var1,
      float var2,
      float var3,
      float var4,
      float var5,
      float var6,
      float var7,
      float var8,
      float var9,
      float var10,
      float var11,
      float var12,
      float var13,
      float var14,
      float var15,
      int var16,
      int var17
   ) {
      float var18 = var10 * ((var14 - var12) * 192.0F) / ((var13 - var11) * 256.0F) * var5 / var6;
      float[][] var19 = new float[(var16 + 1) * (var17 + 1)][];

      for (int var20 = 0; var20 <= var17; var20++) {
         for (int var21 = 0; var21 <= var16; var21++) {
            double var22 = var8 + var10 * (1.0 - 2.0 * var21 / var16);
            double var24 = var9 + var18 * (1.0 - 2.0 * var20 / var17);
            var19[var20 * (var16 + 1) + var21] = new float[]{
               var2 + (float)(Math.sin(var22) * Math.cos(var24)) * var5 * var15,
               var3 + (float)Math.sin(var24) * var6 * var15,
               var4 + (float)(Math.cos(var22) * Math.cos(var24)) * var7 * var15
            };
         }
      }

      for (int var26 = 0; var26 < var17; var26++) {
         for (int var27 = 0; var27 < var16; var27++) {
            float var28 = var11 + (var13 - var11) * var27 / var16;
            float var23 = var11 + (var13 - var11) * (var27 + 1) / var16;
            float var29 = var12 + (var14 - var12) * var26 / var17;
            float var25 = var12 + (var14 - var12) * (var26 + 1) / var17;
            var0.quad(
               var1,
               var19[var26 * (var16 + 1) + var27],
               var19[var26 * (var16 + 1) + var27 + 1],
               var19[(var26 + 1) * (var16 + 1) + var27 + 1],
               var19[(var26 + 1) * (var16 + 1) + var27],
               new float[]{var28, var29, var23, var29, var23, var25, var28, var25}
            );
         }
      }
   }

   static void flat(G var0, String var1, float var2, float var3, float var4, float var5, float var6) {
      float var7 = var2 + var5 / 2.0F;
      float var8 = var2 - var5 / 2.0F;
      float var9 = var3 + var6 / 2.0F;
      float var10 = var3 - var6 / 2.0F;
      var0.quad(
         var1,
         new float[]{var7, var9, var4},
         new float[]{var8, var9, var4},
         new float[]{var8, var10, var4},
         new float[]{var7, var10, var4},
         new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
      );
   }

   static void sprite(G var0, String var1, float var2, float var3, float var4, float var5, int var6) {
      int var7 = var0.color;
      boolean var8 = var0.glow;
      var0.color(var6).glow(true);
      var0.push();
      var0.translate(var2, var3, var4);
      var0.rotY(25.0F);
      var0.plane(var1, 0.0F, 0.0F, var5, var5, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.rotY(90.0F);
      var0.plane(var1, 0.0F, 0.0F, var5, var5, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.color(var7).glow(var8);
   }
}
