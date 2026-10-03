package dev.lego.games;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.RoundRectangle2D.Double;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

final class SolarTex {
   static final ForkJoinPool POOL = new ForkJoinPool(Math.max(1, Math.min(3, Runtime.getRuntime().availableProcessors() - 1)), var0x -> {
      ForkJoinWorkerThread var1x = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(var0x);
      var1x.setDaemon(true);
      var1x.setName("solar-gen-" + var1x.getPoolIndex());
      var1x.setPriority(2);
      return var1x;
   }, null, false);
   private static final int[] PERM = new int[512];
   static final byte[] HASH = new byte[65536];

   private SolarTex() {
   }

   static void rows(int var0, IntConsumer var1) {
      try {
         POOL.submit(() -> IntStream.range(0, var0).parallel().forEach(var1)).get();
      } catch (Exception var4) {
         for (int var3 = 0; var3 < var0; var3++) {
            var1.accept(var3);
         }
      }
   }

   private static double fade(double var0) {
      return var0 * var0 * var0 * (var0 * (var0 * 6.0 - 15.0) + 10.0);
   }

   private static double lerp(double var0, double var2, double var4) {
      return var2 + var0 * (var4 - var2);
   }

   private static double grad(int var0, double var1, double var3, double var5) {
      int var7 = var0 & 15;
      double var8 = var7 < 8 ? var1 : var3;
      double var10 = var7 < 4 ? var3 : (var7 != 12 && var7 != 14 ? var5 : var1);
      return ((var7 & 1) == 0 ? var8 : -var8) + ((var7 & 2) == 0 ? var10 : -var10);
   }

   static double noise(double var0, double var2, double var4) {
      int var6 = (int)Math.floor(var0);
      int var7 = (int)Math.floor(var2);
      int var8 = (int)Math.floor(var4);
      var0 -= var6;
      var2 -= var7;
      var4 -= var8;
      var6 &= 255;
      var7 &= 255;
      var8 &= 255;
      double var9 = fade(var0);
      double var11 = fade(var2);
      double var13 = fade(var4);
      int var15 = PERM[var6] + var7;
      int var16 = PERM[var15] + var8;
      int var17 = PERM[var15 + 1] + var8;
      int var18 = PERM[var6 + 1] + var7;
      int var19 = PERM[var18] + var8;
      int var20 = PERM[var18 + 1] + var8;
      return lerp(
         var13,
         lerp(
            var11,
            lerp(var9, grad(PERM[var16], var0, var2, var4), grad(PERM[var19], var0 - 1.0, var2, var4)),
            lerp(var9, grad(PERM[var17], var0, var2 - 1.0, var4), grad(PERM[var20], var0 - 1.0, var2 - 1.0, var4))
         ),
         lerp(
            var11,
            lerp(var9, grad(PERM[var16 + 1], var0, var2, var4 - 1.0), grad(PERM[var19 + 1], var0 - 1.0, var2, var4 - 1.0)),
            lerp(var9, grad(PERM[var17 + 1], var0, var2 - 1.0, var4 - 1.0), grad(PERM[var20 + 1], var0 - 1.0, var2 - 1.0, var4 - 1.0))
         )
      );
   }

   static double fbm(double var0, double var2, double var4, int var6) {
      double var7 = 0.0;
      double var9 = 0.5;
      double var11 = 1.0;

      for (int var13 = 0; var13 < var6; var13++) {
         var7 += var9 * noise(var0 * var11, var2 * var11, var4 * var11);
         var9 *= 0.5;
         var11 *= 2.03;
      }

      return var7;
   }

   static double ridge(double var0, double var2, double var4, int var6) {
      double var7 = 0.0;
      double var9 = 0.5;
      double var11 = 1.0;
      double var13 = 1.0;

      for (int var15 = 0; var15 < var6; var15++) {
         double var16 = 1.0 - Math.abs(noise(var0 * var11, var2 * var11, var4 * var11));
         var16 *= var16;
         var16 *= var13;
         var13 = Math.min(1.0, var16 * 2.0);
         var7 += var9 * var16;
         var9 *= 0.5;
         var11 *= 2.07;
      }

      return var7;
   }

   static double sstep(double var0, double var2, double var4) {
      double var6 = Math.max(0.0, Math.min(1.0, (var4 - var0) / (var2 - var0)));
      return var6 * var6 * (3.0 - 2.0 * var6);
   }

   static double clamp01(double var0) {
      return var0 < 0.0 ? 0.0 : (var0 > 1.0 ? 1.0 : var0);
   }

   static int mix(int var0, int var1, double var2) {
      if (var2 <= 0.0) {
         return var0;
      } else if (var2 >= 1.0) {
         return var0 & 0xFF000000 | var1 & 16777215;
      } else {
         int var4 = var0 >> 16 & 0xFF;
         int var5 = var0 >> 8 & 0xFF;
         int var6 = var0 & 0xFF;
         int var7 = var1 >> 16 & 0xFF;
         int var8 = var1 >> 8 & 0xFF;
         int var9 = var1 & 0xFF;
         return var0 & 0xFF000000 | (int)(var4 + (var7 - var4) * var2) << 16 | (int)(var5 + (var8 - var5) * var2) << 8 | (int)(var6 + (var9 - var6) * var2);
      }
   }

   static int shade(int var0, double var1) {
      int var3 = (int)Math.min(255.0, (var0 >> 16 & 0xFF) * var1);
      int var4 = (int)Math.min(255.0, (var0 >> 8 & 0xFF) * var1);
      int var5 = (int)Math.min(255.0, (var0 & 0xFF) * var1);
      return var0 & 0xFF000000 | Math.max(0, var3) << 16 | Math.max(0, var4) << 8 | Math.max(0, var5);
   }

   static int ramp(double var0, double[] var2, int[] var3) {
      if (var0 <= var2[0]) {
         return var3[0];
      } else {
         for (int var4 = 1; var4 < var2.length; var4++) {
            if (var0 <= var2[var4]) {
               return mix(var3[var4 - 1], var3[var4], (var0 - var2[var4 - 1]) / (var2[var4] - var2[var4 - 1]));
            }
         }

         return var3[var3.length - 1];
      }
   }

   static SolarPlanet.Maps generate(int var0, long var1, int var3, int var4, boolean var5, boolean var6) {
      SolarPlanet.Maps var7 = new SolarPlanet.Maps(var3, var4, var0 != 3 && var0 != 0 && var0 != 8, var0 == 5);
      double var8 = var1 % 1000L * 0.137;
      double var10 = var1 / 1000L % 1000L * 0.071;
      double var12 = var1 / 7L % 997L * 0.113;
      float[] var14 = new float[var3 * var4];
      float[] var15 = new float[var3 * var4];
      float[] var16 = new float[var3 * var4];
      double[] var17 = new double[var3];
      double[] var18 = new double[var3];

      for (int var19 = 0; var19 < var3; var19++) {
         double var20 = (var19 + 0.5) / var3 * Math.PI * 2.0;
         var17[var19] = Math.cos(var20);
         var18[var19] = Math.sin(var20);
      }

      int var29 = var3 >= 1024 ? 8 : (var3 >= 512 ? 6 : 5);
      rows(
         var4,
         var15x -> {
            double var16x = (0.5 - (var15x + 0.5) / var4) * Math.PI;
            double var18x = Math.cos(var16x);
            double var20x = Math.sin(var16x);

            for (int var22x = 0; var22x < var3; var22x++) {
               double var23x = var18x * var17[var22x];
               double var25x = var18x * var18[var22x];
               int var27x = var15x * var3 + var22x;
               double var30x = 0.0;
               double var28;
               double var32x;
               switch (var0) {
                  case 1:
                  case 6:
                     double var45 = fbm(var23x * 1.1 + 5.3 + var8, var20x * 1.1 + 1.7, var25x * 1.1 + 9.2 + var12, 3) * 0.6;
                     var28 = fbm(var23x * 1.35 + var45 + var8, var20x * 1.35 - var45 * 0.5 + var10, var25x * 1.35 + var45 * 0.8 + var12, var29);
                     double var47 = ridge(var23x * 3.1 + var10, var20x * 3.1, var25x * 3.1 + var8, 5);
                     var28 += Math.max(0.0, var28) * var47 * 0.45;
                     var30x = fbm(var23x * 2.3 + 11.0 + var12, var20x * 2.3, var25x * 2.3 - 4.0, 4);
                     var32x = fbm(var23x * 11.0 + 3.0, var20x * 11.0, var25x * 11.0 + 8.0 + var8, 3);
                     break;
                  case 2:
                     double var44 = fbm(var23x * 1.4 + var8, var20x * 1.4 + 3.0, var25x * 1.4, 3) * 0.5;
                     var28 = fbm(var23x * 1.6 + var44 + var10, var20x * 1.6, var25x * 1.6 - var44, var29);
                     double var46 = ridge(var23x * 2.4 + 7.0, var20x * 2.4, var25x * 2.4 + 1.0, 5);
                     var28 -= Math.pow(var46, 3.0) * 0.35;
                     var30x = fbm(var23x * 2.8 + 20.0, var20x * 2.8, var25x * 2.8 + var8, 4);
                     var32x = fbm(var23x * 14.0, var20x * 14.0 + 5.0, var25x * 14.0, 3);
                     break;
                  case 3:
                     double var43 = fbm(var23x * 2.2 + var8, var20x * 2.2, var25x * 2.2, 4);
                     var28 = var20x * 5.5 + var43 * 1.1 + fbm(var23x * 3.0, var20x * 18.0, var25x * 3.0, 4) * 0.5;
                     var30x = fbm(var23x * 5.0 + var43, var20x * 30.0, var25x * 5.0 - var43, 4);
                     var32x = fbm(var23x * 9.0, var20x * 9.0 + 4.0, var25x * 9.0, 3);
                     break;
                  case 4:
                     var28 = fbm(var23x * 1.5 + var8, var20x * 1.5, var25x * 1.5 + var12, var29) * 0.7;
                     var30x = ridge(var23x * 3.5 + 2.0, var20x * 3.5 + var10, var25x * 3.5, 6);
                     var32x = fbm(var23x * 12.0, var20x * 12.0, var25x * 12.0, 3);
                     break;
                  case 5:
                     var28 = fbm(var23x * 1.7 + var8, var20x * 1.7, var25x * 1.7 + var12, var29);
                     var30x = ridge(var23x * 2.6 + 3.0 + var10, var20x * 2.6, var25x * 2.6 - 2.0, 6);
                     var32x = fbm(var23x * 13.0, var20x * 13.0 + 2.0, var25x * 13.0, 3);
                     break;
                  case 7:
                     double var42 = fbm(var23x * 1.2 + var8, var20x * 1.2, var25x * 1.2 + 2.0, 3) * 0.8;
                     var28 = fbm(var23x * 1.5 + var42, var20x * 1.5 + var10, var25x * 1.5 - var42, var29) * 0.8;
                     double var36 = ridge(var23x * 7.0 + var42 * 3.0, var20x * 16.0 + var42 * 2.0, var25x * 7.0 - var42 * 3.0, 3);
                     var30x = var36;
                     var28 -= Math.pow(ridge(var23x * 2.1 + 9.0, var20x * 2.1, var25x * 2.1, 5), 4.0) * 0.4;
                     var32x = fbm(var23x * 15.0, var20x * 15.0, var25x * 15.0 + 1.0, 3);
                     break;
                  case 8:
                     var28 = fbm(var23x * 1.8 + var8, var20x * 1.8 + var10, var25x * 1.8, var29) * 0.6;
                     var30x = fbm(var23x * 1.2 + 40.0 + var12, var20x * 1.2, var25x * 1.2, 4);
                     var32x = fbm(var23x * 16.0, var20x * 16.0, var25x * 16.0 + 9.0, 3);
                     break;
                  default:
                     double var34 = Math.abs(noise(var23x * 38.0 + var8, var20x * 38.0, var25x * 38.0))
                        + 0.5 * Math.abs(noise(var23x * 80.0, var20x * 80.0, var25x * 80.0 + 3.0));
                     var28 = var34;
                     var30x = fbm(var23x * 3.0 + var8, var20x * 3.0, var25x * 3.0, 4);
                     var32x = fbm(var23x * 9.0, var20x * 9.0, var25x * 9.0 + 5.0, 3);
               }

               var14[var27x] = (float)var28;
               var15[var27x] = (float)var30x;
               var16[var27x] = (float)var32x;
            }
         }
      );
      if (var0 == 8 || var0 == 2 || var0 == 4) {
         Random var30 = new Random(var1 * 31L + 7L);
         int var21 = var0 == 8 ? 900 : (var0 == 2 ? 260 : 90);

         for (int var22 = 0; var22 < var21; var22++) {
            double var23 = Math.asin(var30.nextDouble() * 2.0 - 1.0);
            double var25 = var30.nextDouble() * Math.PI * 2.0;
            double var27 = var3 / 2048.0 * (3.0 + Math.pow(var30.nextDouble(), 5.0) * (var0 == 8 ? 90 : 55));
            stampCrater(var14, var3, var4, var25, var23, var27, 0.25 + var30.nextDouble() * 0.2);
         }
      }

      double var31 = var3 / 2048.0;
      rows(
         var4,
         var12x -> {
            double var13 = (0.5 - (var12x + 0.5) / var4) * Math.PI;
            double var15x = Math.abs(var13);
            double var17x = Math.cos(var13);
            double var19x = Math.sin(var13);
            int var21x = Math.max(0, var12x - 1);
            int var22x = Math.min(var4 - 1, var12x + 1);

            for (int var23x = 0; var23x < var3; var23x++) {
               int var24x = var12x * var3 + var23x;
               double var25x = var17x * var17[var23x];
               double var27x = var17x * var18[var23x];
               double var29x = var14[var24x];
               double var31x = var15[var24x];
               double var33x = var16[var24x];
               double var35 = var14[var12x * var3 + (var23x + 1 & var3 - 1)] - var14[var12x * var3 + (var23x - 1 & var3 - 1)];
               double var37 = var14[var22x * var3 + var23x] - var14[var21x * var3 + var23x];
               double var39 = 1.0 + (-var35 * 0.8 + var37 * 0.6) * 9.0 / Math.max(0.25, var31 * 2.2);
               short var42 = 0;
               int var43 = 0;
               int var44 = 0;
               double var45 = var29x;
               int var63;
               switch (var0) {
                  case 1:
                     double var85 = 0.02;
                     boolean var90 = var15x > 1.22 - var33x * 0.25 + Math.max(0.0, var29x) * 0.3;
                     if (var29x < var85) {
                        double var50 = var85 - var29x;
                        var63 = ramp(var50, new double[]{0.0, 0.025, 0.12, 0.35}, new int[]{-12933192, -14912346, -15777922, -16245173});
                        var63 = mix(var63, -16111014, clamp01(var33x * 0.4 + 0.1));
                        var42 = 230;
                        var39 = 1.0;
                        if (var90) {
                           var63 = mix(-2234128, -4600612, clamp01(var33x + 0.5));
                           var42 = 60;
                        }
                     } else {
                        double var91 = var29x - var85;
                        double var52 = 1.0 - var15x / 1.45 - var91 * 1.4;
                        double var54 = var31x + 0.25 - Math.abs(Math.abs(var13) - 0.42) * 0.9 + (var15x < 0.2 ? 0.25 : 0.0);
                        int var56 = mix(-11630028, -14726630, clamp01(var54 * 1.6 + 0.35 + var33x * 0.4));
                        int var94 = mix(-3561622, -5799348, clamp01(var33x + 0.5));
                        var63 = mix(var94, var56, sstep(-0.05, 0.2, var54));
                        if (var52 < 0.35) {
                           var63 = mix(var63, -8619426, sstep(0.35, 0.18, var52));
                        }

                        if (var91 < 0.012) {
                           var63 = mix(var63, -2504806, 0.6);
                        }

                        var63 = mix(var63, mix(-9740978, -7042692, clamp01(var33x + 0.5)), sstep(0.2, 0.34, var91));
                        var63 = mix(var63, -723208, sstep(0.36, 0.45, var91 + (1.0 - var52) * 0.1));
                        if (var90) {
                           var63 = mix(-1445643, -3483420, clamp01(var33x + 0.5));
                        }

                        if (var5 && !var90 && var52 > 0.3 && var91 < 0.26) {
                           double var58 = fbm(var25x * 7.0 + 3.0, var19x * 7.0, var27x * 7.0 + 1.7, 3)
                              + 0.5 * noise(var25x * 30.0, var19x * 30.0, var27x * 30.0 + 4.2);
                           double var60 = var58 + (var91 < 0.04 ? 0.22 : 0.0) + var54 * 0.15 - 0.18;
                           if (var60 > 0.0) {
                              var43 = (int)Math.min(255.0, Math.pow(var60 * 3.2, 1.6) * 255.0);
                           }
                        }
                     }

                     var45 = var29x;
                     break;
                  case 2:
                     var63 = ramp(clamp01(var29x * 1.2 + 0.5), new double[]{0.0, 0.35, 0.6, 1.0}, new int[]{-10869226, -6666970, -3905478, -2321312});
                     var63 = mix(var63, -12834268, sstep(0.1, 0.35, var31x) * 0.7);
                     var63 = mix(var63, -2056080, clamp01(var33x * 0.6) * 0.3);
                     if (var15x > 1.32 - var33x * 0.2) {
                        var63 = mix(-856342, -2568250, clamp01(var33x + 0.5));
                     }

                     if (var5 && var15x < 0.6) {
                        double var84 = noise(var25x * 10.0 + 5.0, var19x * 10.0, var27x * 10.0 + 0.3);
                        double var89 = noise(var25x * 46.0, var19x * 46.0, var27x * 46.0 + 3.3);
                        if (var84 > 0.5 && var89 > 0.05) {
                           var43 = (int)Math.min(255.0, (var84 - 0.5) * 1400.0 * (var89 + 0.3));
                        }
                     }
                     break;
                  case 3:
                     double var88 = Math.sin(var29x * 2.2) * 0.5 + 0.5;
                     double var93 = Math.sin(var29x * 5.3 + 1.0) * 0.5 + 0.5;
                     var63 = mix(-1451848, -4618922, var88);
                     var63 = mix(var63, -7710150, var93 * 0.35);
                     var63 = mix(var63, -659747, clamp01(var31x * 1.2) * 0.4);
                     var63 = shade(var63, 0.94 + var33x * 0.12);
                     double var53 = (var23x + 0.5) / var3 * Math.PI * 2.0;
                     double var55 = Math.atan2(Math.sin(var53 - 1.9), Math.cos(var53 - 1.9)) / 0.33;
                     double var57 = (var13 + 0.38) / 0.12;
                     double var59 = Math.sqrt(var55 * var55 + var57 * var57);
                     if (var59 < 1.6) {
                        double var61 = Math.sin(var59 * 9.0 + Math.atan2(var57, var55) * 2.0) * 0.5 + 0.5;
                        var63 = mix(var63, mix(-4041684, -1531792, var61 * 0.5 + var59 * 0.3), sstep(1.6, 0.6, var59));
                     }

                     var39 = 1.0;
                     break;
                  case 4:
                     var63 = mix(-1379334, -5650208, clamp01(var29x * 1.3 + 0.5));
                     var63 = mix(var63, -13671017, sstep(0.72, 0.95, var31x) * 0.85);
                     var63 = mix(var63, -1, clamp01(var33x * 0.5) * 0.4);
                     var42 = 70;
                     if (var5) {
                        double var83 = noise(var25x * 16.0 + 9.0, var19x * 16.0, var27x * 16.0 + 1.1);
                        if (var83 > 0.6 && var15x < 1.1) {
                           var43 = (int)Math.min(255.0, (var83 - 0.6) * 1300.0);
                        }
                     }
                     break;
                  case 5:
                     var63 = mix(-14477291, -11912653, clamp01(var29x + 0.5));
                     var63 = shade(var63, 0.85 + var33x * 0.4);
                     double var82 = sstep(0.8, 0.98, var31x);
                     double var87 = sstep(-0.12, -0.3, var29x);
                     double var92 = Math.max(var82, var87);
                     if (var92 > 0.02) {
                        var63 = mix(var63, -8774136, var92);
                        var44 = (int)(var92 * (170.0 + var33x * 80.0));
                     }
                     break;
                  case 6:
                     double var81 = 0.33;
                     if (var29x < var81) {
                        double var49 = var81 - var29x;
                        var63 = ramp(var49, new double[]{0.0, 0.03, 0.18, 0.6}, new int[]{-12203568, -14905927, -15839594, -16375202});
                        var63 = mix(var63, -16040070, clamp01(var31x * 0.8 + 0.3));
                        var42 = 240;
                        var39 = 1.0;
                        if (var5) {
                           double var51 = noise(var25x * 16.0, var19x * 16.0, var27x * 16.0 + 8.1)
                              + noise(var25x * 56.0, var19x * 56.0, var27x * 56.0 + 2.2) * 0.5;
                           if (var51 > 0.42 && var15x < 1.0) {
                              var43 = (int)Math.min(255.0, (var51 - 0.42) * 900.0);
                           }
                        }
                     } else {
                        double var86 = var29x - var81;
                        var63 = var86 < 0.015 ? -1648730 : mix(-12678086, -14000602, clamp01(var33x + 0.5));
                        if (var5) {
                           var43 = (int)Math.min(255.0, 120.0 + var33x * 200.0);
                        }
                     }

                     if (var15x > 1.3 - var33x * 0.2) {
                        var63 = mix(-1510922, -3877149, clamp01(var33x + 0.5));
                        var42 = 40;
                     }
                     break;
                  case 7:
                     var63 = ramp(clamp01(var29x + 0.55), new double[]{0.0, 0.35, 0.65, 1.0}, new int[]{-8764122, -4818366, -2247826, -1060455});
                     var63 = shade(var63, 0.86 + var31x * 0.28);
                     boolean var80 = var29x < -0.22 && var33x > 0.0;
                     if (var80) {
                        var63 = mix(var63, -11567560, 0.7);
                     }

                     if (var15x > 1.38) {
                        var63 = mix(var63, -923169, 0.8);
                     }

                     if (var5 && var15x < 1.0) {
                        double var48 = noise(var25x * 8.0 + 2.0, var19x * 8.0, var27x * 8.0 + 6.6) + (var29x < -0.1 ? 0.3 : 0.0);
                        if (var48 > 0.35) {
                           var43 = (int)Math.min(255.0, (var48 - 0.35) * 700.0);
                        }
                     }
                     break;
                  case 8:
                     var63 = mix(-6514027, -4343117, clamp01(var29x + 0.5));
                     var63 = mix(var63, -11645614, sstep(0.05, 0.3, var31x) * 0.85);
                     var63 = shade(var63, 0.92 + var33x * 0.25);
                     if (var5) {
                        double var79 = noise(var25x * 13.0 + 1.0, var19x * 13.0, var27x * 13.0 + 7.3);
                        if (var79 > 0.62 && var15x < 1.0) {
                           var43 = (int)Math.min(255.0, (var79 - 0.62) * 1500.0);
                        }
                     }
                     break;
                  default:
                     var63 = ramp(clamp01(var29x * 0.9), new double[]{0.0, 0.3, 0.6, 1.0}, new int[]{-4703732, -1015778, -16310, -3912});
                     var63 = mix(var63, -2352, clamp01(var33x * 0.8) * 0.4);
                     double var47 = sstep(0.36, 0.46, var31x);
                     if (var47 > 0.0) {
                        var63 = mix(var63, -12971514, var47 * 0.85);
                     }

                     var39 = 1.0;
               }

               if (var0 != 3 && var0 != 0) {
                  var63 = shade(var63, Math.max(0.55, Math.min(1.4, var39)));
               }

               var7.alb[var24x] = var42 << 24 | var63 & 16777215;
               if (var7.alb[var24x] >>> 24 == 1) {
                  var7.alb[var24x] = 33554432 | var63 & 16777215;
               }

               var7.hgt[var24x] = (byte)Math.max(0, Math.min(255, (int)((var45 + 1.0) * 127.5)));
               var7.city[var24x] = (byte)var43;
               if (var7.heat0 != null) {
                  var7.heat0[var24x] = (byte)var44;
                  var7.heat[var24x] = (byte)var44;
               }
            }
         }
      );
      if (var0 == 1) {
         var7.sea = -126;
      } else if (var0 == 6) {
         var7.sea = -87;
      } else {
         var7.sea = 0;
      }

      if (var7.cloud != null) {
         int var32 = var7.cW;
         int var33 = var7.cH;
         double var24 = var0 == 6 ? 0.1 : (var0 == 1 ? 0.02 : (var0 == 7 ? -0.28 : (var0 == 2 ? -0.2 : (var0 == 5 ? -0.05 : -0.12))));
         rows(var33, var8x -> {
            double var9 = (0.5 - (var8x + 0.5) / var33) * Math.PI;
            double var11 = Math.cos(var9);
            double var13 = Math.sin(var9);

            for (int var15x = 0; var15x < var32; var15x++) {
               double var16x = (var15x + 0.5) / var32 * Math.PI * 2.0;
               double var18x = var11 * Math.cos(var16x);
               double var20x = var11 * Math.sin(var16x);
               double var22x = fbm(var18x * 1.7 + 2.0 + var12, var13 * 2.4 + 7.0, var20x * 1.7 + 1.0, 3) * 1.1;
               double var24x = fbm(var18x * 2.6 + var22x, var13 * 4.2 - var22x * 0.4, var20x * 2.6 - var22x, var3 >= 1024 ? 7 : 5);
               var24x += Math.cos(var9 * 6.0) * 0.06 + var24;
               double var26 = sstep(0.0, 0.34, var24x);
               var7.cloud[var8x * var32 + var15x] = (byte)(var26 * 235.0);
            }
         });
      }

      var7.finish();
      return var7;
   }

   private static void stampCrater(float[] var0, int var1, int var2, double var3, double var5, double var7, double var9) {
      int var11 = (int)((0.5 - var5 / Math.PI) * var2);
      double var12 = var3 / (Math.PI * 2) * var1;
      int var14 = (int)Math.ceil(var7 * 1.6);
      double var15 = var9 * Math.min(1.0, var7 / (var1 / 2048.0 * 30.0) + 0.25) * 0.5;

      for (int var17 = Math.max(0, var11 - var14); var17 <= Math.min(var2 - 1, var11 + var14); var17++) {
         double var18 = (0.5 - (var17 + 0.5) / var2) * Math.PI;
         double var20 = Math.max(0.05, Math.cos(var18));
         double var22 = var17 - var11;
         int var24 = (int)Math.min((double)(var1 / 2), Math.ceil(var14 / var20));

         for (int var25 = (int)var12 - var24; var25 <= (int)var12 + var24; var25++) {
            double var26 = (var25 - var12) * var20;
            double var28 = Math.sqrt(var26 * var26 + var22 * var22) / var7;
            if (!(var28 > 1.6)) {
               int var30 = var17 * var1 + (var25 & var1 - 1);
               double var31 = var28 < 1.0 ? -(1.0 - var28 * var28) * var15 + 0.35 * var15 : 0.35 * var15 * Math.exp(-(var28 - 1.0) * (var28 - 1.0) / 0.04);
               if (var28 < 1.0) {
                  var31 = Math.min(var31, 0.35 * var15 * Math.exp(-(var28 - 1.0) * (var28 - 1.0) / 0.04) - (1.0 - var28 * var28) * var15);
               }

               var0[var30] += (float)var31;
            }
         }
      }
   }

   static int[][] nebula(int var0, int var1, long var2) {
      int[] var4 = new int[var0 * var1];
      double var5 = 2.6 / var1;
      double var7 = var2 % 97L * 1.37;
      rows(var1, var7x -> {
         for (int var8 = 0; var8 < var0; var8++) {
            double var9 = var8 * var5 + var7;
            double var11 = var7x * var5;
            double var13 = fbm(var9 * 0.7, var11 * 0.7, 3.3, 3) * 1.3;
            double var15 = fbm(var9 + var13, var11 - var13 * 0.5, 0.5, 6);
            double var17 = fbm(var9 * 1.6 - 4.0 + var13, var11 * 1.6 + 2.0, 7.1, 5);
            double var19 = fbm(var9 * 3.1 + 9.0, var11 * 3.1 + var13, 1.9, 4);
            double var21 = Math.exp(-Math.pow(((double)var7x / var1 - 0.42 - ((double)var8 / var0 - 0.5) * 0.35 + var15 * 0.2) / 0.28, 2.0));
            double var23 = sstep(-0.05, 0.55, var15 + var21 * 0.35) * (0.45 + var21 * 0.55);
            double var25 = sstep(0.0, 0.5, var17) * 0.8;
            int var27 = -16579574;
            var27 = mix(var27, -15462086, 0.8);
            var27 = mix(var27, -14018987, var23 * 0.75);
            var27 = mix(var27, -15840658, var25 * var23 * 0.8);
            var27 = mix(var27, -7721365, sstep(0.25, 0.7, var17 + var15 * 0.5) * var23 * 0.55);
            var27 = mix(var27, -2056008, sstep(0.45, 0.85, var15 + var19 * 0.3) * var23 * 0.22);
            double var28 = sstep(0.1, 0.45, var19 - var15 * 0.4) * var21;
            var27 = mix(var27, -16645627, var28 * 0.65);
            double var30 = Math.pow(Math.hypot((double)var8 / var0 - 0.5, ((double)var7x / var1 - 0.5) * 1.2), 2.0) * 1.2;
            var27 = shade(var27, 1.0 - Math.min(0.6, var30));
            int var32 = HASH[var8 * 31 + var7x * 131 & 65535] & 7;
            var4[var7x * var0 + var8] = (var27 & 16777215) + (var32 <= 1 ? 65793 * var32 : 0) | 0xFF000000;
         }
      });
      return new int[][]{var4, {var0, var1}};
   }

   static int[][] stars(int var0, int var1, float var2, long var3) {
      int[] var5 = new int[var0 * var0];
      Random var6 = new Random(var3);

      for (int var7 = 0; var7 < var1; var7++) {
         float var8 = var6.nextFloat() * var0;
         float var9 = var6.nextFloat() * var0;
         float var10 = 0.45F + (float)Math.pow(var6.nextFloat(), 6.0) * 1.5F;
         float var11 = var2 * (0.25F + var6.nextFloat() * 0.75F);
         int var12 = var6.nextFloat() < 0.15F ? 16767416 : (var6.nextFloat() < 0.3F ? 12111103 : 16777215);
         int var13 = (int)Math.ceil(var10 * 2.5F);

         for (int var14 = -var13; var14 <= var13; var14++) {
            for (int var15 = -var13; var15 <= var13; var15++) {
               int var16 = (int)var8 + var15 & var0 - 1;
               int var17 = (int)var9 + var14 & var0 - 1;
               double var18 = (int)var8 + var15 + 0.5 - var8;
               double var20 = (int)var9 + var14 + 0.5 - var9;
               double var22 = var11 * Math.exp(-(var18 * var18 + var20 * var20) / (var10 * var10 * 0.9));
               if (!(var22 < 0.01)) {
                  int var24 = var17 * var0 + var16;
                  int var25 = var5[var24] >>> 24;
                  int var26 = (int)Math.min(255.0, var25 + var22 * 255.0);
                  var5[var24] = var26 << 24 | var12;
               }
            }
         }
      }

      return new int[][]{var5, {var0, var0}};
   }

   static int[][] flare(int var0) {
      int[] var1 = new int[var0 * var0];
      double var2 = var0 / 2.0;

      for (int var4 = 0; var4 < var0; var4++) {
         for (int var5 = 0; var5 < var0; var5++) {
            double var6 = (var5 + 0.5 - var2) / var2;
            double var8 = (var4 + 0.5 - var2) / var2;
            double var10 = Math.sqrt(var6 * var6 + var8 * var8);
            double var12 = Math.pow(Math.max(0.0, 1.0 - var10), 3.0) * 0.9;
            var12 += Math.exp(-Math.abs(var8) * 60.0) * Math.pow(Math.max(0.0, 1.0 - Math.abs(var6)), 2.0) * 0.8;
            var12 += Math.exp(-Math.abs(var6) * 60.0) * Math.pow(Math.max(0.0, 1.0 - Math.abs(var8)), 2.0) * 0.8;
            var12 += Math.exp(-var10 * var10 * 90.0) * 0.8;
            var1[var4 * var0 + var5] = (int)(Math.min(1.0, var12) * 255.0) << 24 | 16777215;
         }
      }

      return new int[][]{var1, {var0, var0}};
   }

   static int[][] corona(int var0, long var1) {
      int[] var3 = new int[var0 * var0];
      double var4 = var0 / 2.0;
      rows(var0, var6 -> {
         for (int var7 = 0; var7 < var0; var7++) {
            double var8 = (var7 + 0.5 - var4) / var4;
            double var10 = (var6 + 0.5 - var4) / var4;
            double var12 = Math.sqrt(var8 * var8 + var10 * var10);
            if (!(var12 >= 1.0)) {
               double var14 = Math.atan2(var10, var8);
               double var16 = fbm(Math.cos(var14) * 3.0 + var1, Math.sin(var14) * 3.0, var12 * 1.5, 4);
               double var18 = Math.abs(noise(Math.cos(var14) * 14.0, Math.sin(var14) * 14.0, var1 * 0.3));
               double var20 = Math.pow(1.0 - var12, 2.6);
               double var22 = var20 * (0.55 + 0.45 * sstep(-0.2, 0.4, var16)) + Math.pow(1.0 - var12, 5.0) * 0.4;
               var22 *= 0.7 + 0.5 * var18;
               var3[var6 * var0 + var7] = (int)(Math.min(1.0, var22) * 255.0) << 24 | 16777215;
            }
         }
      });
      return new int[][]{var3, {var0, var0}};
   }

   static int[][] orbitRing(int var0, float var1) {
      int[] var2 = new int[var0 * var0];
      double var3 = var0 / 2.0;
      double var5 = var3 - var1 * 2.0F;

      for (int var7 = 0; var7 < var0; var7++) {
         for (int var8 = 0; var8 < var0; var8++) {
            double var9 = Math.hypot(var8 + 0.5 - var3, var7 + 0.5 - var3);
            double var11 = Math.exp(-Math.pow((var9 - var5) / var1, 2.0));
            if (var11 > 0.003) {
               var2[var7 * var0 + var8] = (int)(var11 * 255.0) << 24 | 16777215;
            }
         }
      }

      return new int[][]{var2, {var0, var0}};
   }

   static int[][] softRing(int var0) {
      int[] var1 = new int[var0 * var0];
      double var2 = var0 / 2.0;

      for (int var4 = 0; var4 < var0; var4++) {
         for (int var5 = 0; var5 < var0; var5++) {
            double var6 = Math.hypot(var5 + 0.5 - var2, var4 + 0.5 - var2) / var2;
            double var8 = Math.exp(-Math.pow((var6 - 0.82) / 0.07, 2.0)) + Math.exp(-Math.pow((var6 - 0.82) / 0.02, 2.0)) * 0.6;
            if (var6 > 0.99) {
               var8 = 0.0;
            }

            var1[var4 * var0 + var5] = (int)(Math.min(1.0, var8) * 255.0) << 24 | 16777215;
         }
      }

      return new int[][]{var1, {var0, var0}};
   }

   static int[][] halo(int var0) {
      int[] var1 = new int[var0 * var0];
      double var2 = var0 / 2.0;
      double var4 = 0.72;

      for (int var6 = 0; var6 < var0; var6++) {
         for (int var7 = 0; var7 < var0; var7++) {
            double var8 = Math.hypot(var7 + 0.5 - var2, var6 + 0.5 - var2) / var2;
            double var10;
            if (var8 < var4) {
               var10 = Math.pow(var8 / var4, 14.0) * 0.9;
            } else {
               var10 = Math.pow(Math.max(0.0, 1.0 - (var8 - var4) / (1.0 - var4)), 2.4);
            }

            var1[var6 * var0 + var7] = (int)(Math.min(1.0, var10) * 255.0) << 24 | 16777215;
         }
      }

      return new int[][]{var1, {var0, var0}};
   }

   static int[][] rock(int var0, long var1, int var3) {
      int[] var4 = new int[var0 * var0];
      double var5 = var0 / 2.0;
      double var7 = var1 * 1.7;

      for (int var9 = 0; var9 < var0; var9++) {
         for (int var10 = 0; var10 < var0; var10++) {
            double var11 = (var10 + 0.5 - var5) / (var5 * 0.9);
            double var13 = (var9 + 0.5 - var5) / (var5 * 0.9);
            double var15 = Math.atan2(var13, var11);
            double var17 = 0.78 + 0.16 * fbm(Math.cos(var15) * 1.5 + var7, Math.sin(var15) * 1.5, var7, 3) * 2.0;
            double var19 = Math.sqrt(var11 * var11 + var13 * var13) / var17;
            if (!(var19 >= 1.0)) {
               double var21 = Math.sqrt(Math.max(0.0, 1.0 - var19 * var19));
               double var23 = var11 / var17;
               double var25 = var13 / var17;
               double var27 = fbm(var23 * 4.0 + var7, var25 * 4.0, var21 * 4.0, 4);
               double var29 = Math.max(0.0, -var23 * 0.6 - var25 * 0.5 + var21 * 0.62 + var27 * 0.5);
               double var31 = noise(var23 * 7.0 + var7, var25 * 7.0, var21 * 7.0);
               int var33 = shade(var3, 0.12 + var29 * 1.05);
               if (var31 > 0.35) {
                  var33 = shade(var33, 0.75);
               }

               double var34 = Math.min(1.0, (1.0 - var19) * var0 * 0.5);
               var4[var9 * var0 + var10] = (int)(var34 * 255.0) << 24 | var33 & 16777215;
            }
         }
      }

      return new int[][]{var4, {var0, var0}};
   }

   static int[] ringPixels(int var0, int var1, float var2, float var3, int[] var4, float[] var5, float var6, double var7) {
      int[] var9 = new int[var0 * var1];
      double var10 = var0 / 2.0;
      double var12 = var1 / 2.0;
      double var14 = var0 / 2.0;
      double var16 = var1 / 2.0;
      double var18 = Math.cos(var6);
      double var20 = Math.sin(var6);

      for (int var22 = 0; var22 < var1; var22++) {
         for (int var23 = 0; var23 < var0; var23++) {
            double var24 = var23 + 0.5 - var10;
            double var26 = var22 + 0.5 - var12;
            double var28 = var24 * var18 + var26 * var20;
            double var30 = -var24 * var20 + var26 * var18;
            double var32 = Math.sqrt(Math.pow(var28 / var14, 2.0) + Math.pow(var30 / var16, 2.0));
            if (!(var32 < var2) && !(var32 > var3)) {
               double var34 = (var32 - var2) / (var3 - var2);
               double var36 = 0.55 + 0.45 * Math.sin(var34 * 38.0) * Math.sin(var34 * 11.0 + 1.3);
               if (var34 > 0.58 && var34 < 0.63) {
                  var36 *= 0.15;
               }

               if (var34 > 0.86 && var34 < 0.88) {
                  var36 *= 0.3;
               }

               double var38 = var36 * Math.min(1.0, var34 * 8.0) * Math.min(1.0, (1.0 - var34) * 10.0) * 0.85;
               int var40 = mix(var4[0], var4[1], var34);
               var40 = mix(var40, var4[2], clamp01(Math.sin(var34 * 23.0) * 0.5 + 0.2));
               if (var5 != null) {
                  double var41 = Math.atan2(var30 / var16, var28 / var14);
                  int var43 = (int)((var41 + Math.PI) / (Math.PI * 2) * var5.length) % var5.length;
                  double var44 = var5[var43];
                  int var46 = (int)(var34 * 6.0);
                  double var47 = (HASH[var43 * 7 + var46 * 131 & 65535] & 255) / 255.0;
                  var38 *= clamp01(var44 * 1.6 - var47 * 0.6);
                  if (var44 < 0.99) {
                     var40 = mix(var40, -12965344, (1.0 - var44) * 0.4);
                  }
               }

               if (var30 > 0.0 && Math.abs(var28 / var14) < 0.43 && var28 * var7 < 0.0) {
                  var38 *= 0.35;
               }

               if (!(var38 < 0.01)) {
                  var9[var22 * var0 + var23] = (int)(var38 * 255.0) << 24 | var40 & 16777215;
               }
            }
         }
      }

      return var9;
   }

   static int[][] accretion(int var0, int var1) {
      int[] var2 = new int[var0 * var1];
      double var3 = var0 / 2.0;
      double var5 = var1 / 2.0;

      for (int var7 = 0; var7 < var1; var7++) {
         for (int var8 = 0; var8 < var0; var8++) {
            double var9 = (var8 + 0.5 - var3) / var3;
            double var11 = (var7 + 0.5 - var5) / var5;
            double var13 = Math.sqrt(var9 * var9 + var11 * var11);
            if (!(var13 > 1.0) && !(var13 < 0.26)) {
               double var15 = (var13 - 0.26) / 0.74;
               double var17 = Math.atan2(var11, var9);
               double var19 = 0.6 + 0.4 * noise(Math.cos(var17) * 3.0 + var15 * 6.0, Math.sin(var17) * 3.0, var15 * 4.0);
               double var21 = Math.pow(1.0 - var15, 1.6) * var19 * Math.min(1.0, var15 * 12.0);
               int var23 = ramp(var15, new double[]{0.0, 0.12, 0.4, 1.0}, new int[]{-1, -7520, -30166, -7725560});
               if (var9 > 0.0) {
                  var23 = shade(var23, 0.8);
               } else {
                  var21 = Math.min(1.0, var21 * 1.25);
               }

               var2[var7 * var0 + var8] = (int)(clamp01(var21) * 255.0) << 24 | var23 & 16777215;
            }
         }
      }

      return new int[][]{var2, {var0, var1}};
   }

   static int[][] swirl(int var0, int var1) {
      int[] var2 = new int[var0 * var0];
      double var3 = var0 / 2.0;

      for (int var5 = 0; var5 < var0; var5++) {
         for (int var6 = 0; var6 < var0; var6++) {
            double var7 = (var6 + 0.5 - var3) / var3;
            double var9 = (var5 + 0.5 - var3) / var3;
            double var11 = Math.sqrt(var7 * var7 + var9 * var9);
            if (!(var11 >= 1.0)) {
               double var13 = Math.atan2(var9, var7);
               double var15 = Math.sin(var13 * var1 + Math.log(var11 + 0.02) * 7.0) * 0.5 + 0.5;
               double var17 = Math.pow(1.0 - var11, 1.2) * (0.35 + 0.65 * var15) * Math.min(1.0, var11 * 5.0);
               var2[var5 * var0 + var6] = (int)(clamp01(var17) * 255.0) << 24 | 16777215;
            }
         }
      }

      return new int[][]{var2, {var0, var0}};
   }

   static int[][] cracks(int var0, long var1) {
      int[] var3 = new int[var0 * var0];
      double var4 = var0 / 2.0;

      for (int var6 = 0; var6 < var0; var6++) {
         for (int var7 = 0; var7 < var0; var7++) {
            double var8 = (var7 + 0.5 - var4) / var4;
            double var10 = (var6 + 0.5 - var4) / var4;
            double var12 = Math.sqrt(var8 * var8 + var10 * var10);
            if (!(var12 >= 1.0)) {
               double var14 = ridge(var8 * 2.6 + var1, var10 * 2.6, 0.5 + var1 * 0.1, 4);
               double var16 = sstep(0.72, 0.95, var14);
               double var18 = Math.min(1.0, (1.0 - var12) * var0 * 0.5);
               var3[var6 * var0 + var7] = (int)(var16 * var18 * 255.0) << 24 | 16777215;
            }
         }
      }

      return new int[][]{var3, {var0, var0}};
   }

   static Color C(int var0) {
      return new Color(var0, true);
   }

   static void missile(Graphics2D var0, int var1, double var2, int var4, int var5, int var6, float var7) {
      var0.translate(var1 / 2.0, var1 / 2.0);
      var0.rotate(var2);
      double var8 = var1 * 0.42 * var7;
      double var10 = var1 * 0.075;
      var0.setColor(C(var6));
      var0.fill(
         poly(
            -var8 * 0.9,
            -var10 * 0.9,
            -var8 * 1.05,
            -var10 * 2.6,
            -var8 * 0.55,
            -var10,
            -var8 * 0.55,
            var10,
            -var8 * 1.05,
            var10 * 2.6,
            -var8 * 0.9,
            var10 * 0.9
         )
      );
      var0.setPaint(new GradientPaint(0.0F, (float)(-var10), C(var4), 0.0F, (float)var10, C(shade(var4, 0.55))));
      var0.fill(new Double(-var8 * 0.9, -var10, var8 * 1.5, var10 * 2.0, var10, var10));
      var0.setColor(C(var5));
      var0.fill(poly(var8 * 0.58, -var10, var8, 0.0, var8 * 0.58, var10));
      var0.setColor(new Color(255, 255, 255, 70));
      var0.fill(new Double(-var8 * 0.8, -var10 * 0.8, var8 * 1.3, var10 * 0.6, var10 * 0.5, var10 * 0.5));
   }

   static java.awt.geom.Path2D.Double poly(double... var0) {
      java.awt.geom.Path2D.Double var1 = new java.awt.geom.Path2D.Double();
      var1.moveTo(var0[0], var0[1]);

      for (byte var2 = 2; var2 < var0.length; var2 += 2) {
         var1.lineTo(var0[var2], var0[var2 + 1]);
      }

      var1.closePath();
      return var1;
   }

   static void ellipse(Graphics2D var0, double var1, double var3, double var5, double var7) {
      var0.fill(new java.awt.geom.Ellipse2D.Double(var1 - var5, var3 - var7, var5 * 2.0, var7 * 2.0));
   }

   static void ufo(Graphics2D var0, int var1, int var2, int var3) {
      double var4 = var1 / 2.0;
      if (var2 == 1) {
         var0.setPaint(new RadialGradientPaint((float)var4, (float)(var4 * 0.8), var1 * 0.5F, new float[]{0.0F, 1.0F}, new Color[]{C(-10853260), C(-14933976)}));
         ellipse(var0, var4, var4, var1 * 0.48, var1 * 0.3);
         var0.setColor(C(-15855338));
         ellipse(var0, var4, var4 + var1 * 0.02, var1 * 0.3, var1 * 0.17);
         var0.setColor(C(var3));

         for (int var10 = 0; var10 < 16; var10++) {
            double var7 = var10 / 16.0 * Math.PI * 2.0;
            ellipse(var0, var4 + Math.cos(var7) * var1 * 0.4, var4 + Math.sin(var7) * var1 * 0.23, var1 * 0.014, var1 * 0.014);
         }

         var0.setPaint(new RadialGradientPaint((float)var4, (float)var4, var1 * 0.16F, new float[]{0.0F, 1.0F}, new Color[]{C(-1), C(var3 & 16777215)}));
         ellipse(var0, var4, var4, var1 * 0.16, var1 * 0.1);
         var0.setColor(C(1442840575));
         var0.setStroke(new BasicStroke(var1 * 0.006F));

         for (int var11 = 1; var11 <= 3; var11++) {
            var0.draw(
               new java.awt.geom.Ellipse2D.Double(
                  var4 - var1 * 0.12 * var11 - var1 * 0.04,
                  var4 - var1 * 0.07 * var11 - var1 * 0.02,
                  (var1 * 0.12 * var11 + var1 * 0.04) * 2.0,
                  (var1 * 0.07 * var11 + var1 * 0.02) * 2.0
               )
            );
         }
      } else if (var2 == 2) {
         var0.setColor(C(-14012614));
         var0.fill(poly(var4, var4 - var1 * 0.3, var4 + var1 * 0.42, var4 + var1 * 0.18, var4, var4 + var1 * 0.06, var4 - var1 * 0.42, var4 + var1 * 0.18));
         var0.setColor(C(var3));
         ellipse(var0, var4, var4 - var1 * 0.05, var1 * 0.06, var1 * 0.06);
         ellipse(var0, var4 - var1 * 0.3, var4 + var1 * 0.12, var1 * 0.03, var1 * 0.03);
         ellipse(var0, var4 + var1 * 0.3, var4 + var1 * 0.12, var1 * 0.03, var1 * 0.03);
      } else if (var2 == 3) {
         var0.setPaint(new GradientPaint(0.0F, (float)(var4 - var1 * 0.1), C(-8749172), 0.0F, (float)(var4 + var1 * 0.12), C(-14013131)));
         var0.fill(
            poly(
               var4 - var1 * 0.46,
               var4,
               var4 - var1 * 0.2,
               var4 - var1 * 0.12,
               var4 + var1 * 0.36,
               var4 - var1 * 0.08,
               var4 + var1 * 0.47,
               var4,
               var4 + var1 * 0.36,
               var4 + var1 * 0.1,
               var4 - var1 * 0.2,
               var4 + var1 * 0.12
            )
         );
         var0.setColor(C(var3));

         for (int var9 = 0; var9 < 5; var9++) {
            ellipse(var0, var4 - var1 * 0.2 + var9 * var1 * 0.12, var4, var1 * 0.022, var1 * 0.022);
         }

         var0.setColor(C(-1));
         ellipse(var0, var4 - var1 * 0.44, var4, var1 * 0.03, var1 * 0.05);
      } else {
         double var6 = var2 == 4 ? 0.85 : 1.0;
         var0.setPaint(new GradientPaint(0.0F, (float)(var4 - var1 * 0.1), C(-4668720), 0.0F, (float)(var4 + var1 * 0.15), C(-12959408)));
         ellipse(var0, var4, var4 + var1 * 0.05, var1 * 0.46 * var6, var1 * 0.15 * var6);
         var0.setPaint(
            new RadialGradientPaint(
               (float)(var4 - var1 * 0.05), (float)(var4 - var1 * 0.12), (float)(var1 * 0.25), new float[]{0.0F, 1.0F}, new Color[]{C(-1507340), C(var3)}
            )
         );
         var0.fill(new java.awt.geom.Arc2D.Double(var4 - var1 * 0.2 * var6, var4 - var1 * 0.2 * var6, var1 * 0.4 * var6, var1 * 0.4 * var6, 0.0, 180.0, 1));
         var0.setColor(C(var3));

         for (int var8 = 0; var8 < 5; var8++) {
            ellipse(var0, var4 - var1 * 0.3 * var6 + var8 * var1 * 0.15 * var6, var4 + var1 * 0.08, var1 * 0.03, var1 * 0.03);
         }

         if (var2 == 4) {
            var0.setColor(C(-7695453));
            var0.setStroke(new BasicStroke(var1 * 0.03F));
            var0.draw(new java.awt.geom.Line2D.Double(var4 - var1 * 0.25, var4 + var1 * 0.12, var4 - var1 * 0.34, var4 + var1 * 0.3));
            var0.draw(new java.awt.geom.Line2D.Double(var4 + var1 * 0.25, var4 + var1 * 0.12, var4 + var1 * 0.34, var4 + var1 * 0.3));
         }
      }
   }

   static void walker(Graphics2D var0, int var1, int var2) {
      double var3 = var1 / 2.0;
      var0.setStroke(new BasicStroke(var1 * 0.045F, 1, 1));
      var0.setColor(C(-6643024));
      var0.draw(new java.awt.geom.Line2D.Double(var3, var3 - var1 * 0.12, var3 - var1 * 0.32, var3 + var1 * 0.42));
      var0.draw(new java.awt.geom.Line2D.Double(var3, var3 - var1 * 0.12, var3 + var1 * 0.32, var3 + var1 * 0.42));
      var0.draw(new java.awt.geom.Line2D.Double(var3, var3 - var1 * 0.12, var3 + var1 * 0.04, var3 + var1 * 0.46));
      var0.setPaint(new GradientPaint(0.0F, (float)(var3 - var1 * 0.3), C(-3616548), 0.0F, (float)(var3 - var1 * 0.02), C(-11906464)));
      ellipse(var0, var3, var3 - var1 * 0.18, var1 * 0.2, var1 * 0.12);
      var0.setColor(C(var2));
      ellipse(var0, var3, var3 - var1 * 0.16, var1 * 0.05, var1 * 0.04);
   }

   static void satellite(Graphics2D var0, int var1, int var2) {
      double var3 = var1 / 2.0;
      var0.setColor(C(-13940086));
      var0.fill(new java.awt.geom.Rectangle2D.Double(var3 - var1 * 0.46, var3 - var1 * 0.08, var1 * 0.28, var1 * 0.16));
      var0.fill(new java.awt.geom.Rectangle2D.Double(var3 + var1 * 0.18, var3 - var1 * 0.08, var1 * 0.28, var1 * 0.16));
      var0.setColor(C(-9794872));

      for (int var5 = 0; var5 < 3; var5++) {
         var0.fill(new java.awt.geom.Rectangle2D.Double(var3 - var1 * 0.44 + var5 * var1 * 0.09, var3 - var1 * 0.06, var1 * 0.07, var1 * 0.12));
         var0.fill(new java.awt.geom.Rectangle2D.Double(var3 + var1 * 0.2 + var5 * var1 * 0.09, var3 - var1 * 0.06, var1 * 0.07, var1 * 0.12));
      }

      var0.setPaint(new GradientPaint(0.0F, (float)(var3 - var1 * 0.12), C(-2038550), 0.0F, (float)(var3 + var1 * 0.12), C(-9801606)));
      var0.fill(new Double(var3 - var1 * 0.13, var3 - var1 * 0.13, var1 * 0.26, var1 * 0.26, var1 * 0.06, var1 * 0.06));
      var0.setColor(C(var2));
      ellipse(var0, var3, var3, var1 * 0.06, var1 * 0.06);
   }

   static int[][] station(int var0) {
      int[] var1 = new int[var0 * var0];
      double var2 = var0 / 2.0;

      for (int var4 = 0; var4 < var0; var4++) {
         for (int var5 = 0; var5 < var0; var5++) {
            double var6 = (var5 + 0.5 - var2) / (var2 * 0.96);
            double var8 = (var4 + 0.5 - var2) / (var2 * 0.96);
            double var10 = var6 * var6 + var8 * var8;
            if (!(var10 >= 1.0)) {
               double var12 = Math.sqrt(1.0 - var10);
               double var14 = Math.max(0.0, -var6 * 0.55 - var8 * 0.45 + var12 * 0.7);
               int var16 = -7564644;
               double var17 = Math.asin(var8);
               double var19 = Math.atan2(var6, var12);
               boolean var21 = Math.abs(Math.sin(var17 * 14.0)) < 0.07 || Math.abs(Math.sin(var19 * 18.0)) < 0.05;
               if (var21) {
                  var16 = -10854294;
               }

               int var22 = HASH[(int)(var17 * 40.0) * 97 + (int)(var19 * 50.0) * 13 & 65535] & 255;
               if (var22 < 40) {
                  var16 = mix(var16, -9670020, 0.8);
               }

               if (Math.abs(var8) < 0.035) {
                  var16 = -13881288;
               }

               double var23 = var6 + 0.38;
               double var25 = var8 + 0.36;
               double var27 = Math.sqrt(var23 * var23 + var25 * var25);
               if (var27 < 0.24) {
                  var16 = mix(-12959672, -9801608, var27 / 0.24);
                  if (Math.abs(Math.sin(var27 * 60.0)) < 0.3) {
                     var16 = shade(var16, 0.8);
                  }

                  if (var27 < 0.04) {
                     var16 = -6422678;
                  }
               }

               var16 = shade(var16, 0.15 + var14 * 1.0);
               if (var22 > 250 && var14 < 0.3) {
                  var16 = -5728;
               }

               double var29 = Math.min(1.0, (1.0 - Math.sqrt(var10)) * var0 * 0.48);
               var1[var4 * var0 + var5] = (int)(var29 * 255.0) << 24 | var16 & 16777215;
            }
         }
      }

      return new int[][]{var1, {var0, var0}};
   }

   static void fun(Graphics2D var0, int var1, int var2, int var3) {
      double var4 = var1 / 2.0;
      double var6 = var1 / 100.0;
      switch (var2) {
         case 0:
            var0.setColor(C(shade(var3, 0.6)));
            var0.fill(new Double(var4 - 44.0 * var6, var4 - 22.0 * var6, 88.0 * var6, 50.0 * var6, 8.0 * var6, 8.0 * var6));
            var0.setColor(C(var3));
            var0.fill(new Double(var4 - 44.0 * var6, var4 - 26.0 * var6, 88.0 * var6, 46.0 * var6, 8.0 * var6, 8.0 * var6));

            for (int var20 = 0; var20 < 4; var20++) {
               for (int var23 = 0; var23 < 2; var23++) {
                  double var26 = var4 - 33.0 * var6 + var20 * 22 * var6;
                  double var29 = var4 - 14.0 * var6 + var23 * 22 * var6;
                  var0.setColor(C(shade(var3, 0.7)));
                  ellipse(var0, var26, var29 + 2.0 * var6, 8.0 * var6, 8.0 * var6);
                  var0.setColor(C(shade(var3, 1.22)));
                  ellipse(var0, var26, var29, 8.0 * var6, 8.0 * var6);
               }
            }
            break;
         case 1:
            var0.setColor(C(-724502));
            ellipse(var0, var4, var4 + 4.0 * var6, 36.0 * var6, 24.0 * var6);
            var0.setColor(C(-14935526));
            ellipse(var0, var4 - 12.0 * var6, var4, 10.0 * var6, 8.0 * var6);
            ellipse(var0, var4 + 14.0 * var6, var4 + 12.0 * var6, 9.0 * var6, 7.0 * var6);
            ellipse(var0, var4 + 4.0 * var6, var4 - 10.0 * var6, 6.0 * var6, 5.0 * var6);
            var0.setColor(C(-724502));
            ellipse(var0, var4 + 38.0 * var6, var4 - 8.0 * var6, 14.0 * var6, 12.0 * var6);
            var0.setColor(C(-875088));
            ellipse(var0, var4 + 46.0 * var6, var4 - 4.0 * var6, 8.0 * var6, 6.0 * var6);
            var0.setColor(C(-14935526));
            ellipse(var0, var4 + 36.0 * var6, var4 - 12.0 * var6, 2.5 * var6, 2.5 * var6);
            var0.setColor(C(-2502720));
            var0.fill(poly(var4 + 30.0 * var6, var4 - 18.0 * var6, var4 + 26.0 * var6, var4 - 30.0 * var6, var4 + 34.0 * var6, var4 - 20.0 * var6));
            var0.setColor(C(-12962764));

            for (int var19 = 0; var19 < 4; var19++) {
               var0.fill(new java.awt.geom.Rectangle2D.Double(var4 - 26.0 * var6 + var19 * 15 * var6, var4 + 22.0 * var6, 5.0 * var6, 14.0 * var6));
            }
            break;
         case 2:
            var0.setColor(C(-7714262));
            ellipse(var0, var4, var4 + 14.0 * var6, 40.0 * var6, 16.0 * var6);
            var0.fill(new java.awt.geom.Rectangle2D.Double(var4 - 40.0 * var6, var4 - 12.0 * var6, 80.0 * var6, 26.0 * var6));
            var0.setColor(C(-10268));
            ellipse(var0, var4, var4 - 12.0 * var6, 40.0 * var6, 16.0 * var6);
            var0.fill(new java.awt.geom.Rectangle2D.Double(var4 - 40.0 * var6, var4 - 12.0 * var6, 80.0 * var6, 8.0 * var6));
            var0.setColor(C(-1));

            for (int var18 = 0; var18 < 8; var18++) {
               ellipse(var0, var4 - 35.0 * var6 + var18 * 10 * var6, var4 - 4.0 * var6, 5.0 * var6, 5.0 * var6);
            }

            var0.setColor(C(-1900533));
            ellipse(var0, var4 - 14.0 * var6, var4 - 16.0 * var6, 5.0 * var6, 5.0 * var6);
            ellipse(var0, var4 + 12.0 * var6, var4 - 10.0 * var6, 5.0 * var6, 5.0 * var6);
            var0.setColor(C(-10503937));
            var0.fill(new java.awt.geom.Rectangle2D.Double(var4 - 2.0 * var6, var4 - 36.0 * var6, 4.0 * var6, 20.0 * var6));
            var0.setColor(C(-11446));
            ellipse(var0, var4, var4 - 40.0 * var6, 4.0 * var6, 6.0 * var6);
            break;
         case 3:
            var0.setColor(C(-11745));
            ellipse(var0, var4 - 4.0 * var6, var4 + 12.0 * var6, 34.0 * var6, 22.0 * var6);
            ellipse(var0, var4 + 16.0 * var6, var4 - 14.0 * var6, 18.0 * var6, 18.0 * var6);
            var0.setColor(C(-30208));
            var0.fill(poly(var4 + 30.0 * var6, var4 - 16.0 * var6, var4 + 46.0 * var6, var4 - 10.0 * var6, var4 + 30.0 * var6, var4 - 6.0 * var6));
            var0.setColor(C(-15066598));
            ellipse(var0, var4 + 20.0 * var6, var4 - 18.0 * var6, 3.5 * var6, 3.5 * var6);
            var0.setColor(C(-6528));
            ellipse(var0, var4 - 12.0 * var6, var4 + 6.0 * var6, 14.0 * var6, 8.0 * var6);
            break;
         case 4:
            var0.setPaint(
               new RadialGradientPaint(
                  (float)(var4 - 14.0 * var6),
                  (float)(var4 - 14.0 * var6),
                  (float)(50.0 * var6),
                  new float[]{0.0F, 1.0F},
                  new Color[]{C(-9811253), C(-15463888)}
               )
            );
            ellipse(var0, var4, var4, 44.0 * var6, 44.0 * var6);
            var0.setColor(C(-16120300));
            ellipse(var0, var4 + 8.0 * var6, var4 - 16.0 * var6, 5.0 * var6, 5.0 * var6);
            ellipse(var0, var4 + 20.0 * var6, var4 - 8.0 * var6, 5.0 * var6, 5.0 * var6);
            ellipse(var0, var4 + 14.0 * var6, var4 + 6.0 * var6, 6.0 * var6, 6.0 * var6);
            break;
         case 5:
            var0.setColor(C(-2778550));
            ellipse(var0, var4, var4, 44.0 * var6, 44.0 * var6);
            var0.setColor(C(-1553884));
            ellipse(var0, var4, var4, 38.0 * var6, 38.0 * var6);
            var0.setColor(C(-9110));
            ellipse(var0, var4, var4, 35.0 * var6, 35.0 * var6);
            var0.setColor(C(-5233382));
            double[][] var17 = new double[][]{{-14.0, -12.0}, {12.0, -18.0}, {18.0, 8.0}, {-6.0, 16.0}, {-20.0, 6.0}, {2.0, -2.0}};

            for (double[] var12 : var17) {
               ellipse(var0, var4 + var12[0] * var6, var4 + var12[1] * var6, 7.0 * var6, 7.0 * var6);
            }

            var0.setColor(C(-12678610));
            ellipse(var0, var4 + 8.0 * var6, var4 + 18.0 * var6, 3.0 * var6, 2.0 * var6);
            ellipse(var0, var4 - 16.0 * var6, var4 - 22.0 * var6, 3.0 * var6, 2.0 * var6);
            break;
         case 6:
            var0.setColor(C(-3569074));
            ellipse(var0, var4, var4, 44.0 * var6, 44.0 * var6);
            var0.setColor(C(-32832));
            ellipse(var0, var4, var4 - 2.0 * var6, 38.0 * var6, 36.0 * var6);
            int[] var16 = new int[]{-1, -10496769, -7859, -8587396};
            Random var21 = new Random(4L);

            for (int var24 = 0; var24 < 26; var24++) {
               double var27 = var21.nextDouble() * Math.PI * 2.0;
               double var13 = 20.0 + var21.nextDouble() * 14.0;
               var0.setColor(C(var16[var24 % 4]));
               var0.fill(
                  new java.awt.geom.Rectangle2D.Double(
                     var4 + Math.cos(var27) * var13 * var6, var4 + Math.sin(var27) * var13 * var6 - 2.0 * var6, 5.0 * var6, 2.0 * var6
                  )
               );
            }

            var0.setComposite(AlphaComposite.Clear);
            ellipse(var0, var4, var4, 13.0 * var6, 13.0 * var6);
            var0.setComposite(AlphaComposite.SrcOver);
            break;
         case 7:
            var0.setColor(C(-15329766));
            var0.fill(new Double(var4 - 40.0 * var6, var4 - 28.0 * var6, 80.0 * var6, 56.0 * var6, 10.0 * var6, 10.0 * var6));
            var0.setColor(C(-723728));
            var0.fill(new java.awt.geom.Rectangle2D.Double(var4 - 38.0 * var6, var4 + 10.0 * var6, 76.0 * var6, 14.0 * var6));
            var0.setColor(C(-15329766));

            for (int var15 = 0; var15 < 14; var15++) {
               if (var15 % 7 != 2 && var15 % 7 != 6) {
                  var0.fill(new java.awt.geom.Rectangle2D.Double(var4 - 36.0 * var6 + var15 * 5.4 * var6, var4 + 10.0 * var6, 3.0 * var6, 8.0 * var6));
               }
            }

            var0.setColor(C(-12961212));
            var0.fill(new java.awt.geom.Rectangle2D.Double(var4 - 34.0 * var6, var4 - 22.0 * var6, 68.0 * var6, 26.0 * var6));
            break;
         case 8:
            var0.setColor(C(-11745));
            ellipse(var0, var4, var4 - 26.0 * var6, 11.0 * var6, 11.0 * var6);
            var0.fill(new java.awt.geom.Rectangle2D.Double(var4 - 4.0 * var6, var4 - 38.0 * var6, 8.0 * var6, 4.0 * var6));
            var0.setColor(C(-15066598));
            ellipse(var0, var4 - 4.0 * var6, var4 - 28.0 * var6, 1.8 * var6, 1.8 * var6);
            ellipse(var0, var4 + 4.0 * var6, var4 - 28.0 * var6, 1.8 * var6, 1.8 * var6);
            var0.setStroke(new BasicStroke((float)(1.6 * var6)));
            var0.draw(new java.awt.geom.Arc2D.Double(var4 - 5.0 * var6, var4 - 27.0 * var6, 10.0 * var6, 7.0 * var6, 200.0, 140.0, 0));
            var0.setColor(C(var3));
            var0.fill(
               poly(
                  var4 - 14.0 * var6,
                  var4 - 14.0 * var6,
                  var4 + 14.0 * var6,
                  var4 - 14.0 * var6,
                  var4 + 17.0 * var6,
                  var4 + 12.0 * var6,
                  var4 - 17.0 * var6,
                  var4 + 12.0 * var6
               )
            );
            var0.setColor(C(shade(var3, 0.8)));
            var0.fill(new Double(var4 - 24.0 * var6, var4 - 12.0 * var6, 8.0 * var6, 22.0 * var6, 6.0 * var6, 6.0 * var6));
            var0.fill(new Double(var4 + 16.0 * var6, var4 - 12.0 * var6, 8.0 * var6, 22.0 * var6, 6.0 * var6, 6.0 * var6));
            var0.setColor(C(-16755265));
            var0.fill(new java.awt.geom.Rectangle2D.Double(var4 - 16.0 * var6, var4 + 12.0 * var6, 32.0 * var6, 26.0 * var6));
            var0.setColor(C(-16761204));
            var0.fill(new java.awt.geom.Rectangle2D.Double(var4 - 1.0 * var6, var4 + 18.0 * var6, 2.0 * var6, 20.0 * var6));
            break;
         case 9:
            var0.setColor(C(var3));
            ellipse(var0, var4, var4 + 8.0 * var6, 20.0 * var6, 24.0 * var6);
            ellipse(var0, var4, var4 - 20.0 * var6, 15.0 * var6, 13.0 * var6);
            ellipse(var0, var4 - 12.0 * var6, var4 - 32.0 * var6, 6.0 * var6, 6.0 * var6);
            ellipse(var0, var4 + 12.0 * var6, var4 - 32.0 * var6, 6.0 * var6, 6.0 * var6);
            ellipse(var0, var4 - 20.0 * var6, var4 - 2.0 * var6, 8.0 * var6, 6.0 * var6);
            ellipse(var0, var4 + 20.0 * var6, var4 - 2.0 * var6, 8.0 * var6, 6.0 * var6);
            ellipse(var0, var4 - 12.0 * var6, var4 + 30.0 * var6, 9.0 * var6, 7.0 * var6);
            ellipse(var0, var4 + 12.0 * var6, var4 + 30.0 * var6, 9.0 * var6, 7.0 * var6);
            var0.setColor(new Color(255, 255, 255, 110));
            ellipse(var0, var4 - 6.0 * var6, var4 - 24.0 * var6, 4.0 * var6, 3.0 * var6);
            ellipse(var0, var4 - 7.0 * var6, var4 + 2.0 * var6, 5.0 * var6, 8.0 * var6);
            break;
         default:
            var0.setColor(C(-6643542));
            ellipse(var0, var4, var4, 42.0 * var6, 42.0 * var6);
            Random var8 = new Random(7L);

            for (byte var9 = -40; var9 < 40; var9 += 7) {
               for (byte var10 = -40; var10 < 40; var10 += 7) {
                  if (var10 * var10 + var9 * var9 <= 1600) {
                     int var11 = 120 + var8.nextInt(135);
                     var0.setColor(new Color(var11, var11, Math.min(255, var11 + 20)));
                     var0.fill(new java.awt.geom.Rectangle2D.Double(var4 + var10 * var6, var4 + var9 * var6, 6.0 * var6, 6.0 * var6));
                  }
               }
            }
      }
   }

   static {
      Random var0 = new Random(1337L);
      int[] var1 = new int[256];
      int var2 = 0;

      while (var2 < 256) {
         var1[var2] = var2++;
      }

      for (int var5 = 255; var5 > 0; var5--) {
         int var3 = var0.nextInt(var5 + 1);
         int var4 = var1[var5];
         var1[var5] = var1[var3];
         var1[var3] = var4;
      }

      for (int var6 = 0; var6 < 512; var6++) {
         PERM[var6] = var1[var6 & 0xFF];
      }

      var0.nextBytes(HASH);
   }
}
