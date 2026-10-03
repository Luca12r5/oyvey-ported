package dev.lego.cosmetic;

import java.util.Arrays;
import java.util.HashMap;

final class Smooth {
   static final float CREASE = 0.42F;
   static final int FLAT = Integer.MIN_VALUE;

   private Smooth() {
   }

   static int group(String var0, boolean var1) {
      return !var0.contains("crystal") && !var0.contains("gem") ? var0.hashCode() * 7 + (var1 ? 1 : 0) : Integer.MIN_VALUE;
   }

   static float[] normals(float[][] var0, float[][] var1, int[] var2, int var3) {
      HashMap var4 = new HashMap(var3 * 3);
      long[] var5 = new long[var3 * 4];

      for (int var6 = 0; var6 < var3; var6++) {
         if (var2[var6] != Integer.MIN_VALUE) {
            float[] var7 = var0[var6];

            for (int var8 = 0; var8 < 4; var8++) {
               long var9 = key(var7[var8 * 3], var7[var8 * 3 + 1], var7[var8 * 3 + 2], var2[var6]);
               var5[var6 * 4 + var8] = var9;
               int[] var11 = (int[])var4.get(var9);
               if (var11 == null) {
                  var11 = new int[5];
                  var4.put(var9, var11);
               } else if (var11[0] + 1 >= var11.length) {
                  var11 = Arrays.copyOf(var11, var11.length * 2);
                  var4.put(var9, var11);
               }

               if (var11[0] == 0 || var11[var11[0]] != var6) {
                  var11[++var11[0]] = var6;
               }
            }
         }
      }

      float[] var16 = new float[var3 * 12];

      for (int var17 = 0; var17 < var3; var17++) {
         float[] var18 = var1[var17];
         if (var2[var17] == Integer.MIN_VALUE) {
            for (int var20 = 0; var20 < 4; var20++) {
               var16[var17 * 12 + var20 * 3] = var18[0];
               var16[var17 * 12 + var20 * 3 + 1] = var18[1];
               var16[var17 * 12 + var20 * 3 + 2] = var18[2];
            }
         } else {
            for (int var19 = 0; var19 < 4; var19++) {
               int[] var10 = (int[])var4.get(var5[var17 * 4 + var19]);
               float var21 = 0.0F;
               float var12 = 0.0F;
               float var13 = 0.0F;

               for (int var14 = 1; var14 <= var10[0]; var14++) {
                  float[] var15 = var1[var10[var14]];
                  if (!(var15[0] * var18[0] + var15[1] * var18[1] + var15[2] * var18[2] < 0.42F)) {
                     var21 += var15[0];
                     var12 += var15[1];
                     var13 += var15[2];
                  }
               }

               float var22 = (float)Math.sqrt(var21 * var21 + var12 * var12 + var13 * var13);
               if (var22 < 1.0E-5F) {
                  var21 = var18[0];
                  var12 = var18[1];
                  var13 = var18[2];
                  var22 = 1.0F;
               }

               var16[var17 * 12 + var19 * 3] = var21 / var22;
               var16[var17 * 12 + var19 * 3 + 1] = var12 / var22;
               var16[var17 * 12 + var19 * 3 + 2] = var13 / var22;
            }
         }
      }

      return var16;
   }

   private static long key(float var0, float var1, float var2, int var3) {
      long var4 = Math.round(var0 * 40.0F) & 1048575;
      long var6 = Math.round(var1 * 40.0F) & 1048575;
      long var8 = Math.round(var2 * 40.0F) & 1048575;
      return (var4 << 40 | var6 << 20 | var8) * 31L + var3;
   }
}
