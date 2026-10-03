package dev.lego.visual.sky;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public final class SkyGen {
   public static final String[] IDS = new String[]{
      "milkyway",
      "nebula",
      "planets",
      "aurora",
      "sunset",
      "storm",
      "summer",
      "goldenhour",
      "cottoncandy",
      "sunrise",
      "overcast",
      "thunder",
      "anime",
      "alien",
      "synthwave",
      "oceandusk",
      "bloodmoon",
      "eclipse",
      "auroraviolet",
      "winter",
      "twilight",
      "rednebula",
      "spiral",
      "blackhole",
      "void"
   };
   public static final String[] NAMES = new String[]{
      "Sternennacht",
      "Galaxie-Nebel",
      "Planeten",
      "Polarlicht",
      "Sonnenuntergang",
      "Gewitternacht",
      "Sommertag",
      "Goldene Stunde",
      "Zuckerwatte-Abend",
      "Nebliger Sonnenaufgang",
      "Regentag",
      "Gewitterfront",
      "Anime-Himmel",
      "Fremder Planet",
      "Synthwave",
      "Meeresdämmerung",
      "Blutmond",
      "Sonnenfinsternis",
      "Violettes Polarlicht",
      "Winternacht",
      "Feen-Dämmerung",
      "Roter Nebel",
      "Spiralgalaxie",
      "Schwarzes Loch",
      "Endleere"
   };
   private static final ConcurrentHashMap<String, Integer> FOG = new ConcurrentHashMap<>();
   private static volatile ForkJoinPool POOL;
   private static final int LUT_N = 4096;
   private static final float[] GAMMA = new float[4098];
   private static final float KNEE = 0.62F;
   private static final ThreadLocal<SkyGen.C> TL;
   static final int SPACE = 0;
   static final int GROUND = 1;
   static final int OCEAN = 2;
   static final int CLOUDSEA = 3;
   private static final int[] P;

   private SkyGen() {
   }

   public static void dir(int var0, double var1, double var3, double[] var5) {
      double var6;
      double var8;
      double var10;
      switch (var0) {
         case 0:
            var6 = -1.0 + 2.0 * var1;
            var8 = -1.0;
            var10 = -1.0 + 2.0 * var3;
            break;
         case 1:
            var6 = -1.0 + 2.0 * var1;
            var8 = 1.0;
            var10 = 1.0 - 2.0 * var3;
            break;
         case 2:
            var6 = 1.0 - 2.0 * var1;
            var8 = 1.0 - 2.0 * var3;
            var10 = 1.0;
            break;
         case 3:
            var6 = -1.0;
            var8 = 1.0 - 2.0 * var3;
            var10 = 1.0 - 2.0 * var1;
            break;
         case 4:
            var6 = -1.0 + 2.0 * var1;
            var8 = 1.0 - 2.0 * var3;
            var10 = -1.0;
            break;
         default:
            var6 = 1.0;
            var8 = 1.0 - 2.0 * var3;
            var10 = -1.0 + 2.0 * var1;
      }

      double var12 = Math.sqrt(var6 * var6 + var8 * var8 + var10 * var10);
      var5[0] = var6 / var12;
      var5[1] = var8 / var12;
      var5[2] = var10 / var12;
   }

   static boolean faceUV(int var0, double var1, double var3, double var5, double[] var7) {
      switch (var0) {
         case 0:
            double var11 = -var3;
            if (var11 <= 1.0E-6) {
               return false;
            }

            var7[0] = (var1 / var11 + 1.0) * 0.5;
            var7[1] = (var5 / var11 + 1.0) * 0.5;
            return true;
         case 1:
            if (var3 <= 1.0E-6) {
               return false;
            }

            var7[0] = (var1 / var3 + 1.0) * 0.5;
            var7[1] = (1.0 - var5 / var3) * 0.5;
            return true;
         case 2:
            if (var5 <= 1.0E-6) {
               return false;
            }

            var7[0] = (1.0 - var1 / var5) * 0.5;
            var7[1] = (1.0 - var3 / var5) * 0.5;
            return true;
         case 3:
            double var10 = -var1;
            if (var10 <= 1.0E-6) {
               return false;
            }

            var7[0] = (1.0 - var5 / var10) * 0.5;
            var7[1] = (1.0 - var3 / var10) * 0.5;
            return true;
         case 4:
            double var8 = -var5;
            if (var8 <= 1.0E-6) {
               return false;
            }

            var7[0] = (var1 / var8 + 1.0) * 0.5;
            var7[1] = (1.0 - var3 / var8) * 0.5;
            return true;
         default:
            if (var1 <= 1.0E-6) {
               return false;
            } else {
               var7[0] = (var5 / var1 + 1.0) * 0.5;
               var7[1] = (1.0 - var3 / var1) * 0.5;
               return true;
            }
      }
   }

   public static String kind(String var0) {
      return sky(var0).kind;
   }

   public static int fogColor(String var0) {
      return FOG.computeIfAbsent(var0 == null ? "" : var0, SkyGen::computeFog);
   }

   private static int computeFog(String var0) {
      SkyGen.Sky var1 = sky(var0);
      SkyGen.C var2 = new SkyGen.C();
      double var3 = 0.0;
      double var5 = 0.0;
      double var7 = 0.0;
      int var9 = 0;

      for (int var10 = 0; var10 < 64; var10++) {
         double var11 = var10 * Math.PI * 2.0 / 64.0;

         for (int var13 = 0; var13 < 4; var13++) {
            double var14 = 0.01 + var13 * 0.03;
            double var16 = Math.cos(var14);
            var1.evalFull(Math.cos(var11) * var16, Math.sin(var14), Math.sin(var11) * var16, var2);
            var3 += tone8(var2.r * var1.exposure);
            var5 += tone8(var2.g * var1.exposure);
            var7 += tone8(var2.b * var1.exposure);
            var9++;
         }
      }

      int var18 = (int)Math.round(var3 / var9);
      int var19 = (int)Math.round(var5 / var9);
      int var12 = (int)Math.round(var7 / var9);
      return clamp8(var18) << 16 | clamp8(var19) << 8 | clamp8(var12);
   }

   private static ForkJoinPool pool() {
      ForkJoinPool var0 = POOL;
      if (var0 == null) {
         synchronized (SkyGen.class) {
            if (POOL == null) {
               int var2 = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
               POOL = new ForkJoinPool(var2, var0x -> {
                  ForkJoinWorkerThread var1 = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(var0x);
                  var1.setName("LegoClient-SkyGen-" + var1.getPoolIndex());
                  var1.setDaemon(true);
                  var1.setPriority(2);
                  return var1;
               }, null, false);
            }

            var0 = POOL;
         }
      }

      return var0;
   }

   static void parallel(int var0, IntConsumer var1) {
      try {
         pool().submit(() -> IntStream.range(0, var0).parallel().forEach(var1)).get();
      } catch (InterruptedException var4) {
         Thread.currentThread().interrupt();
         throw new RuntimeException(var4);
      } catch (ExecutionException var5) {
         Throwable var3 = var5.getCause();
         if (var3 instanceof RuntimeException) {
            throw (RuntimeException)var3;
         } else {
            throw new RuntimeException(var3);
         }
      }
   }

   public static int[] generate(String var0, int var1) {
      SkyGen.Sky var2 = sky(var0);
      int var3 = var1 * 3;
      int var4 = var1 * 2;
      int var5 = Math.max(16, Math.min(var1, (int)Math.round(512.0 * var2.detail)));
      int var6 = var5 + 1;
      float var7 = (float)var2.exposure;
      float[][] var8 = new float[6][var6 * var6 * 4];
      parallel(6 * var6, var5x -> {
         int var6x = var5x / var6;
         int var7x = var5x % var6;
         float[] var8x = var8[var6x];
         SkyGen.C var9x = new SkyGen.C();
         double[] var10x = new double[3];
         double var11x = (double)var7x / var5;
         int var13x = var7x * var6 * 4;

         for (int var14x = 0; var14x < var6; var13x += 4) {
            dir(var6x, (double)var14x / var5, var11x, var10x);
            var2.evalFull(var10x[0], var10x[1], var10x[2], var9x);
            var8x[var13x] = (float)(var9x.r * var7);
            var8x[var13x + 1] = (float)(var9x.g * var7);
            var8x[var13x + 2] = (float)(var9x.b * var7);
            var8x[var13x + 3] = (float)Math.max(0.0, Math.min(1.0, var9x.vis));
            var14x++;
         }
      });
      boolean[][] var9 = new boolean[6][];
      if (var2.fine != null && var2.fine.length > 0) {
         double[] var10 = new double[3];
         double var11 = 3.5 / var5;

         for (int var13 = 0; var13 < 6; var13++) {
            boolean[] var14 = null;

            for (int var15 = 0; var15 < var5; var15++) {
               for (int var16 = 0; var16 < var5; var16++) {
                  dir(var13, (var16 + 0.5) / var5, (var15 + 0.5) / var5, var10);

                  for (double[] var20 : var2.fine) {
                     double var21 = var10[0] * var20[0] + var10[1] * var20[1] + var10[2] * var20[2];
                     if (var21 > Math.cos(var20[3] + var11)) {
                        if (var14 == null) {
                           var14 = new boolean[var5 * var5];
                        }

                        var14[var15 * var5 + var16] = true;
                        break;
                     }
                  }
               }
            }

            var9[var13] = var14;
         }
      }

      SkyGen.Stars var23 = SkyGen.Stars.build(var2, var1);
      int[] var24 = new int[var3 * var4];
      double var12 = (double)var5 / var1;
      parallel(
         var4,
         var12x -> {
            SkyGen.C var13x = null;
            double[] var14x = null;
            int var15x = var12x / var1 * 3;
            int var16x = var12x % var1;
            double var17 = (var16x + 0.5) * var12;
            int var19 = Math.min(var5 - 1, (int)var17);
            float var20x = (float)(var17 - var19);
            int var21x = var16x / 16;
            int var22 = var12x * var3;

            for (int var23x = 0; var23x < var3; var23x++) {
               int var24x = var15x + var23x / var1;
               int var25 = var23x % var1;
               double var26 = (var25 + 0.5) * var12;
               int var28 = Math.min(var5 - 1, (int)var26);
               float var29 = (float)(var26 - var28);
               float[] var30 = var8[var24x];
               int var31 = (var19 * var6 + var28) * 4;
               int var32 = var31 + 4;
               int var33 = var31 + var6 * 4;
               int var34 = var33 + 4;
               float var35 = (1.0F - var29) * (1.0F - var20x);
               float var36 = var29 * (1.0F - var20x);
               float var37 = (1.0F - var29) * var20x;
               float var38 = var29 * var20x;
               float var39 = var30[var31] * var35 + var30[var32] * var36 + var30[var33] * var37 + var30[var34] * var38;
               float var40 = var30[var31 + 1] * var35 + var30[var32 + 1] * var36 + var30[var33 + 1] * var37 + var30[var34 + 1] * var38;
               float var41 = var30[var31 + 2] * var35 + var30[var32 + 2] * var36 + var30[var33 + 2] * var37 + var30[var34 + 2] * var38;
               float var42 = -1.0F;
               boolean[] var43 = var9[var24x];
               if (var43 != null && var43[var19 * var5 + var28]) {
                  if (var13x == null) {
                     var13x = new SkyGen.C();
                     var14x = new double[3];
                  }

                  dir(var24x, (var25 + 0.5) / var1, (var16x + 0.5) / var1, var14x);
                  var2.evalFull(var14x[0], var14x[1], var14x[2], var13x);
                  var39 = (float)(var13x.r * var7);
                  var40 = (float)(var13x.g * var7);
                  var41 = (float)(var13x.b * var7);
                  var42 = (float)Math.max(0.0, Math.min(1.0, var13x.vis));
               }

               if (var23 != null) {
                  int var44 = (var24x * var23.nc + var21x) * var23.nc + var25 / 16;
                  int var45 = var23.start[var44];
                  int var46 = var23.start[var44 + 1];
                  if (var45 != var46) {
                     float var47 = var42 >= 0.0F
                        ? var42
                        : var30[var31 + 3] * var35 + var30[var32 + 3] * var36 + var30[var33 + 3] * var37 + var30[var34 + 3] * var38;
                     float var48 = 0.0F;
                     float var49 = 0.0F;
                     float var50 = 0.0F;
                     float var51 = 0.0F;
                     float var52 = 0.0F;
                     float var53 = 0.0F;
                     float[] var54 = var23.data;

                     for (int var55 = var45; var55 < var46; var55++) {
                        int var56 = var23.item[var55] * 9;
                        float var57 = var25 - var54[var56];
                        float var58 = var16x - var54[var56 + 1];
                        float var59 = var57 * var57 + var58 * var58;
                        float var60 = var54[var56 + 5];
                        float var61 = var59 * var60;
                        float var62 = (var61 < 12.0F ? var54[var56 + 6] * (float)Math.exp(-var61) : 0.0F) + var54[var56 + 7] * (float)Math.exp(-var61 * 0.06F);
                        if (var54[var56 + 8] > 0.0F) {
                           var48 += var54[var56 + 2] * var62;
                           var49 += var54[var56 + 3] * var62;
                           var50 += var54[var56 + 4] * var62;
                        } else {
                           var51 += var54[var56 + 2] * var62;
                           var52 += var54[var56 + 3] * var62;
                           var53 += var54[var56 + 4] * var62;
                        }
                     }

                     var39 += var48 * var47 + var51;
                     var40 += var49 * var47 + var52;
                     var41 += var50 * var47 + var53;
                  }
               }

               int var63 = var23x * 73856093 ^ var12x * 19349663;
               var63 ^= var63 >>> 13;
               var63 *= 1540483477;
               var63 ^= var63 >>> 15;
               float var67 = ((var63 & 1023) - (var63 >>> 10 & 1023)) * 9.765625E-4F;
               float var68 = ((var63 >>> 20 & 1023) - (var63 & 1023)) * 9.765625E-4F;
               float var69 = ((var63 >>> 5 & 1023) - (var63 >>> 20 & 1023)) * 9.765625E-4F;
               int var70 = clamp8((int)(tone(var39) + 0.5F + var67));
               int var71 = clamp8((int)(tone(var40) + 0.5F + var68));
               int var72 = clamp8((int)(tone(var41) + 0.5F + var69));
               var24[var22 + var23x] = 0xFF000000 | var70 << 16 | var71 << 8 | var72;
            }
         }
      );
      return var24;
   }

   static float tone(float var0) {
      if (var0 <= 0.0F) {
         return 0.0F;
      } else {
         if (var0 > 0.62F) {
            var0 = 0.62F + 0.38F * (1.0F - (float)Math.exp(-(var0 - 0.62F) / 0.38F));
         }

         float var1 = var0 * 4096.0F;
         int var2 = (int)var1;
         if (var2 >= 4096) {
            return GAMMA[4096];
         } else {
            float var3 = var1 - var2;
            return GAMMA[var2] + (GAMMA[var2 + 1] - GAMMA[var2]) * var3;
         }
      }
   }

   static int tone8(double var0) {
      return clamp8(Math.round(tone((float)var0)));
   }

   private static int clamp8(int var0) {
      return var0 < 0 ? 0 : (var0 > 255 ? 255 : var0);
   }

   public static SkyGen.Preset preset(String var0) {
      SkyGen.Sky var1 = sky(var0);
      return (var1x, var2) -> {
         SkyGen.C var3 = TL.get();
         var1.evalFull(var1x[0], var1x[1], var1x[2], var3);
         var2[0] = var3.r * var1.exposure;
         var2[1] = var3.g * var1.exposure;
         var2[2] = var3.b * var1.exposure;
      };
   }

   static SkyGen.Sky sky(String var0) {
      SkyGen.Sky var1 = null;
      if (var1 == null) {
         var1 = SkyGen2.make(var0);
      }

      if (var1 == null) {
         var1 = SkyGen3.make(var0);
      }

      if (var1 == null) {
         var1 = SkyGen3.make("milkyway");
      }

      return var1;
   }

   static void clouds(SkyGen.C var0, double var1, double var3, double var5, SkyGen.Clouds var7, double[] var8, double[] var9) {
      if (!(var3 < 0.003)) {
         double var10 = smooth(0.003, 0.09, var3);
         double var12 = var7.height / (var3 + 0.03);
         double var14 = var1 * var12 * var7.scale / var7.stretch;
         double var16 = var5 * var12 * var7.scale;
         double var18 = var12 * var7.scale;
         double var20 = Math.max(2.0, var7.oct - log2(1.0 + var18 * 0.35));
         double var22 = fbmL(var14 * 0.35, var7.seed, var16 * 0.35, 3.0);
         double var24 = fbmL(var14 * 0.35 + 5.2, var7.seed + 3.0, var16 * 0.35 + 1.3, 3.0);
         double var26 = var14 + var22 * var7.warp;
         double var28 = var16 + var24 * var7.warp;
         double var30 = fbmL(var26, var7.seed + 10.0, var28, var20);
         double var32 = smooth(var7.cov, var7.cov + var7.soft, var30);
         if (!(var32 <= 0.002)) {
            double var34 = var8[0];
            double var36 = var8[2];
            double var38 = Math.sqrt(var34 * var34 + var36 * var36) + 1.0E-6;
            double var40 = 0.22 * Math.min(1.0, var38 * 1.5);
            double var42 = fbmL(var26 + var34 / var38 * var40, var7.seed + 10.0, var28 + var36 / var38 * var40, Math.max(2.0, var20 - 2.0));
            double var44 = clamp01((var30 - var42) * 3.0 + 0.5);
            double var46 = smooth(var7.cov, var7.cov + var7.soft * 2.8, var30);
            double var48 = (0.62 + (var44 - 0.5) * 0.9 * var7.lightSide) * (1.0 - var7.thickDark * var46);
            var48 = clamp01(var48);
            double[] var50 = var7.lit;
            double[] var51 = var7.shade;
            double var52 = var51[0] + (var50[0] - var51[0]) * var48;
            double var54 = var51[1] + (var50[1] - var51[1]) * var48;
            double var56 = var51[2] + (var50[2] - var51[2]) * var48;
            double var58 = var1 * var8[0] + var3 * var8[1] + var5 * var8[2];
            if (var58 > 0.0 && var7.silver > 0.0) {
               double var60 = var58 * var58;
               double var62 = var60 * var60;
               double var64 = var62 * var62 * (1.0 - var46) * var7.silver;
               var52 += var9[0] * var64;
               var54 += var9[1] * var64;
               var56 += var9[2] * var64;
            }

            double var70 = 1.0 - Math.exp(-var18 / var7.fog);
            var52 += (var0.r - var52) * var70;
            var54 += (var0.g - var54) * var70;
            var56 += (var0.b - var56) * var70;
            double var71 = var32 * var10 * var7.opacity;
            var0.r = var0.r + (var52 - var0.r) * var71;
            var0.g = var0.g + (var54 - var0.g) * var71;
            var0.b = var0.b + (var56 - var0.b) * var71;
            var0.vis = var0.vis * (1.0 - Math.min(1.0, var71 * 1.4));
         }
      }
   }

   static void towers(SkyGen.C var0, double var1, double var3, double var5, SkyGen.Towers var7, double[] var8) {
      double var9 = var7.h0 + var7.h1;
      if (!(var3 > var9 + 0.08) && !(var3 < -0.02)) {
         double var11 = Math.sqrt(var1 * var1 + var5 * var5) + 1.0E-9;
         double var13 = var1 / var11;
         double var15 = var5 / var11;
         double var17 = fbmL(var13 * var7.freq, var7.seed, var15 * var7.freq, 4.0);
         double var19 = var7.h0 + var7.h1 * smooth(var7.cut, var7.cut + 0.35, var17);
         if (!(var3 > var19 + 0.07)) {
            double var21 = fbmL(var13 * var7.bill, var3 * var7.bill * 1.25 + var7.seed * 3.1, var15 * var7.bill, 5.0);
            double var23 = var19 - var3 + var21 * 0.06 * var7.puff;
            double var25 = smooth(0.0, var7.soft, var23);
            if (!(var25 <= 0.002)) {
               double var27 = fbmL(var13 * var7.bill, (var3 - 0.012) * var7.bill * 1.25 + var7.seed * 3.1, var15 * var7.bill, 3.0);
               double var29 = clamp01((var21 - var27) * 4.0 + 0.5);
               double var31 = clamp01(var3 / Math.max(0.01, var19 + 0.03));
               double var33 = var8[0];
               double var35 = var8[2];
               double var37 = Math.sqrt(var33 * var33 + var35 * var35) + 1.0E-6;
               double var39 = (var13 * var33 + var15 * var35) / var37;
               double var41 = smooth(-0.1, 1.0, var31 * 0.9 + (var29 - 0.5) * 0.8 + var21 * 0.5) * (1.0 - var7.sunSide + var7.sunSide * (0.5 + 0.5 * var39));
               var41 = clamp01(var41);
               double[] var43 = var7.lit;
               double[] var44 = var7.shade;
               double var45 = var44[0] + (var43[0] - var44[0]) * var41;
               double var47 = var44[1] + (var43[1] - var44[1]) * var41;
               double var49 = var44[2] + (var43[2] - var44[2]) * var41;
               double var51 = Math.exp(-Math.max(0.0, var3) / var7.haze);
               var45 += (var0.r - var45) * var51;
               var47 += (var0.g - var47) * var51;
               var49 += (var0.b - var49) * var51;
               var0.r = var0.r + (var45 - var0.r) * var25;
               var0.g = var0.g + (var47 - var0.g) * var25;
               var0.b = var0.b + (var49 - var0.b) * var25;
               var0.vis *= 1.0 - var25;
            }
         }
      }
   }

   static void sunDisk(SkyGen.C var0, double var1, double var3, double[] var5, double var6, double var8, double var10, double var12, double var14) {
      double var16 = 2.0 * (1.0 - var1);
      double var18 = Math.sqrt(Math.max(0.0, var16));
      double var20 = var18 / var3;
      double var22 = 0.0;
      if (var20 < 1.15) {
         double var24 = smooth(1.12, 0.92, var20);
         double var26 = Math.sqrt(Math.max(0.0, 1.0 - Math.min(1.0, var20 * var20)));
         var22 += var6 * var24 * (0.55 + 0.45 * var26);
      }

      var22 += var8 * Math.exp(-var18 / var10) + var12 * Math.exp(-var16 / (var14 * var14));
      var0.r = var0.r + var5[0] * var22;
      var0.g = var0.g + var5[1] * var22;
      var0.b = var0.b + var5[2] * var22;
      if (var20 < 1.1) {
         var0.vis = var0.vis * smooth(0.8, 1.1, var20);
      }
   }

   private static int fl(double var0) {
      int var2 = (int)var0;
      return var0 < var2 ? var2 - 1 : var2;
   }

   private static double grad(int var0, double var1, double var3, double var5) {
      switch (var0 & 15) {
         case 0:
            return var1 + var3;
         case 1:
            return -var1 + var3;
         case 2:
            return var1 - var3;
         case 3:
            return -var1 - var3;
         case 4:
            return var1 + var5;
         case 5:
            return -var1 + var5;
         case 6:
            return var1 - var5;
         case 7:
            return -var1 - var5;
         case 8:
            return var3 + var5;
         case 9:
            return -var3 + var5;
         case 10:
            return var3 - var5;
         case 11:
            return -var3 - var5;
         case 12:
            return var3 + var1;
         case 13:
            return -var3 + var5;
         case 14:
            return var3 - var1;
         default:
            return -var3 - var5;
      }
   }

   public static double noise(double var0, double var2, double var4) {
      int var6 = fl(var0);
      int var7 = fl(var2);
      int var8 = fl(var4);
      var0 -= var6;
      var2 -= var7;
      var4 -= var8;
      int var9 = var6 & 0xFF;
      int var10 = var7 & 0xFF;
      int var11 = var8 & 0xFF;
      double var12 = var0 * var0 * var0 * (var0 * (var0 * 6.0 - 15.0) + 10.0);
      double var14 = var2 * var2 * var2 * (var2 * (var2 * 6.0 - 15.0) + 10.0);
      double var16 = var4 * var4 * var4 * (var4 * (var4 * 6.0 - 15.0) + 10.0);
      int var18 = P[var9] + var10;
      int var19 = P[var18] + var11;
      int var20 = P[var18 + 1] + var11;
      int var21 = P[var9 + 1] + var10;
      int var22 = P[var21] + var11;
      int var23 = P[var21 + 1] + var11;
      double var24 = var0 - 1.0;
      double var26 = var2 - 1.0;
      double var28 = var4 - 1.0;
      double var30 = grad(P[var19], var0, var2, var4);
      double var32 = grad(P[var22], var24, var2, var4);
      double var34 = grad(P[var20], var0, var26, var4);
      double var36 = grad(P[var23], var24, var26, var4);
      double var38 = grad(P[var19 + 1], var0, var2, var28);
      double var40 = grad(P[var22 + 1], var24, var2, var28);
      double var42 = grad(P[var20 + 1], var0, var26, var28);
      double var44 = grad(P[var23 + 1], var24, var26, var28);
      double var46 = var30 + var12 * (var32 - var30);
      double var48 = var34 + var12 * (var36 - var34);
      double var50 = var38 + var12 * (var40 - var38);
      double var52 = var42 + var12 * (var44 - var42);
      double var54 = var46 + var14 * (var48 - var46);
      double var56 = var50 + var14 * (var52 - var50);
      return var54 + var16 * (var56 - var54);
   }

   public static double fbm(double var0, double var2, double var4, int var6) {
      return fbmL(var0, var2, var4, var6);
   }

   static double fbmL(double var0, double var2, double var4, double var6) {
      double var8 = 0.0;
      double var10 = 0.5;
      int var12 = (int)var6;
      double var13 = var6 - var12;

      for (int var15 = 0; var15 < var12; var15++) {
         var8 += var10 * noise(var0, var2, var4);
         double var16 = var0 * 1.62 + var4 * 1.18;
         double var18 = var4 * 1.62 - var0 * 1.18;
         var0 = var16 + 1.7;
         var4 = var18 - 3.1;
         var2 = var2 * 2.01 + 0.73;
         var10 *= 0.5;
      }

      if (var13 > 0.001) {
         var8 += var10 * var13 * noise(var0, var2, var4);
      }

      return var8;
   }

   public static double ridged(double var0, double var2, double var4, int var6) {
      double var7 = 0.0;
      double var9 = 0.5;

      for (int var11 = 0; var11 < var6; var11++) {
         double var12 = 1.0 - Math.abs(noise(var0, var2, var4));
         var7 += var9 * var12 * var12;
         double var14 = var0 * 1.62 + var4 * 1.18;
         double var16 = var4 * 1.62 - var0 * 1.18;
         var0 = var14 + 1.7;
         var4 = var16 - 3.1;
         var2 = var2 * 2.01 + 0.73;
         var9 *= 0.5;
      }

      return var7;
   }

   static double smooth(double var0, double var2, double var4) {
      double var6 = (var4 - var0) / (var2 - var0);
      var6 = var6 < 0.0 ? 0.0 : (var6 > 1.0 ? 1.0 : var6);
      return var6 * var6 * (3.0 - 2.0 * var6);
   }

   static double clamp01(double var0) {
      return var0 < 0.0 ? 0.0 : (var0 > 1.0 ? 1.0 : var0);
   }

   static double log2(double var0) {
      return Math.log(var0) * 1.4426950408889634;
   }

   static double mix(double var0, double var2, double var4) {
      return var0 + (var2 - var0) * var4;
   }

   static double[] rgb(int var0) {
      return new double[]{Math.pow((var0 >> 16 & 0xFF) / 255.0, 2.2), Math.pow((var0 >> 8 & 0xFF) / 255.0, 2.2), Math.pow((var0 & 0xFF) / 255.0, 2.2)};
   }

   static double[] norm(double var0, double var2, double var4) {
      double var6 = Math.sqrt(var0 * var0 + var2 * var2 + var4 * var4);
      return new double[]{var0 / var6, var2 / var6, var4 / var6};
   }

   static double[] azel(double var0, double var2) {
      double var4 = Math.toRadians(var0);
      double var6 = Math.toRadians(var2);
      return new double[]{Math.sin(var4) * Math.cos(var6), Math.sin(var6), -Math.cos(var4) * Math.cos(var6)};
   }

   static void add(SkyGen.C var0, double[] var1, double var2) {
      var0.r = var0.r + var1[0] * var2;
      var0.g = var0.g + var1[1] * var2;
      var0.b = var0.b + var1[2] * var2;
   }

   static void set(SkyGen.C var0, double[] var1) {
      var0.r = var1[0];
      var0.g = var1[1];
      var0.b = var1[2];
   }

   static void mixTo(SkyGen.C var0, double[] var1, double var2) {
      var0.r = var0.r + (var1[0] - var0.r) * var2;
      var0.g = var0.g + (var1[1] - var0.g) * var2;
      var0.b = var0.b + (var1[2] - var0.b) * var2;
   }

   static void mul(SkyGen.C var0, double var1) {
      var0.r *= var1;
      var0.g *= var1;
      var0.b *= var1;
   }

   static double[] cross(double[] var0, double[] var1) {
      double[] var2 = new double[]{var0[1] * var1[2] - var0[2] * var1[1], var0[2] * var1[0] - var0[0] * var1[2], var0[0] * var1[1] - var0[1] * var1[0]};
      double var3 = Math.sqrt(var2[0] * var2[0] + var2[1] * var2[1] + var2[2] * var2[2]);
      return new double[]{var2[0] / var3, var2[1] / var3, var2[2] / var3};
   }

   static double[][] frame(double[] var0, double var1) {
      double[] var3 = Math.abs(var0[1]) < 0.95 ? new double[]{0.0, 1.0, 0.0} : new double[]{1.0, 0.0, 0.0};
      double[] var4 = cross(var3, var0);
      double[] var5 = cross(var0, var4);
      double var6 = Math.toRadians(var1);
      double var8 = Math.cos(var6);
      double var10 = Math.sin(var6);
      double[] var12 = new double[]{var4[0] * var8 + var5[0] * var10, var4[1] * var8 + var5[1] * var10, var4[2] * var8 + var5[2] * var10};
      double[] var13 = new double[]{var5[0] * var8 - var4[0] * var10, var5[1] * var8 - var4[1] * var10, var5[2] * var8 - var4[2] * var10};
      return new double[][]{var12, var13};
   }

   static double hash(int var0, int var1, int var2, int var3) {
      long var4 = var0 * 374761393L + var1 * 668265263L + var2 * 2147483647L + var3 * 144665L;
      var4 = (var4 ^ var4 >>> 13) * 1274126177L;
      var4 ^= var4 >>> 16;
      return (var4 & 16777215L) / 1.6777215E7;
   }

   static {
      for (int var0 = 0; var0 <= 4097; var0++) {
         GAMMA[var0] = (float)(255.0 * Math.pow(Math.min(1.0, var0 / 4096.0), 0.45454545454545453));
      }

      TL = ThreadLocal.withInitial(SkyGen.C::new);
      P = new int[512];
      int[] var5 = new int[256];
      int var1 = 0;

      while (var1 < 256) {
         var5[var1] = var1++;
      }

      Random var6 = new Random(1337L);

      for (int var2 = 255; var2 > 0; var2--) {
         int var3 = var6.nextInt(var2 + 1);
         int var4 = var5[var2];
         var5[var2] = var5[var3];
         var5[var3] = var4;
      }

      for (int var7 = 0; var7 < 512; var7++) {
         P[var7] = var5[var7 & 0xFF];
      }
   }

   static final class C {
      double r;
      double g;
      double b;
      double vis;
      double t0;
      double t1;
      double t2;
      double t3;
   }

   static final class Clouds {
      double height = 1.0;
      double scale = 1.0;
      double cov = 0.05;
      double soft = 0.3;
      double warp = 0.8;
      double oct = 6.0;
      double seed = 0.0;
      double fog = 12.0;
      double opacity = 1.0;
      double stretch = 1.0;
      double lightSide = 1.0;
      double silver = 1.0;
      double thickDark = 0.55;
      double[] lit = SkyGen.rgb(16777215);
      double[] shade = SkyGen.rgb(9345712);
   }

   interface F3 {
      double f(double var1, double var3, double var5);
   }

   static final class FloatList {
      float[] a = new float[4096];
      int n;

      void add(float var1) {
         if (this.n == this.a.length) {
            this.a = Arrays.copyOf(this.a, this.n * 2);
         }

         this.a[this.n++] = var1;
      }

      float[] toArray() {
         return Arrays.copyOf(this.a, this.n);
      }
   }

   static final class IntList {
      int[] a = new int[1024];
      int n;

      void add(int var1) {
         if (this.n == this.a.length) {
            this.a = Arrays.copyOf(this.a, this.n * 2);
         }

         this.a[this.n++] = var1;
      }

      int get(int var1) {
         return this.a[var1];
      }
   }

   public interface Preset {
      void color(double[] var1, double[] var2);
   }

   abstract static class Sky {
      String kind = "day";
      double exposure = 1.0;
      double detail = 1.0;
      int below = 1;
      double[] ground = SkyGen.rgb(1711654);
      double[] groundFar = null;
      double groundFall = 0.22;
      double groundNoise = 0.0;
      boolean starsBelow = false;
      double[] water = SkyGen.rgb(464162);
      double waves = 1.0;
      double rough = 0.1;
      double glitter = 0.0;
      double mirror = 1.0;
      SkyGen.Clouds sea;
      double[] seaGap = SkyGen.rgb(6058900);
      double[] sun = SkyGen.norm(0.0, 1.0, 0.0);
      double[] sunCol = SkyGen.rgb(16774364);
      int stars = 0;
      double starMin = 0.05;
      double starMax = 4.0;
      double starSize = 1.0;
      double starHalo = 1.0;
      long starSeed = 7L;
      double[][] fine;
      int motes = 0;
      double moteBright = 1.0;
      double moteSize = 1.6;
      SkyGen.F3 starDensityFn;

      double moteDensity(double var1, double var3, double var5) {
         return 1.0;
      }

      void moteColor(Random var1, double[] var2) {
         var2[0] = 1.3;
         var2[1] = 1.0;
         var2[2] = 0.5;
      }

      abstract void sky(double var1, double var3, double var5, SkyGen.C var7);

      double starDensity(double var1, double var3, double var5) {
         return this.starDensityFn != null ? this.starDensityFn.f(var1, var3, var5) : SkyGen.smooth(-0.02, 0.28, var3);
      }

      void starColor(Random var1, double[] var2) {
         double var3 = var1.nextDouble();
         if (var3 < 0.18) {
            var2[0] = 0.7;
            var2[1] = 0.82;
            var2[2] = 1.25;
         } else if (var3 < 0.45) {
            var2[0] = 0.92;
            var2[1] = 0.96;
            var2[2] = 1.08;
         } else if (var3 < 0.75) {
            var2[0] = 1.06;
            var2[1] = 1.0;
            var2[2] = 0.9;
         } else if (var3 < 0.93) {
            var2[0] = 1.18;
            var2[1] = 0.95;
            var2[2] = 0.72;
         } else {
            var2[0] = 1.25;
            var2[1] = 0.78;
            var2[2] = 0.58;
         }
      }

      final void evalFull(double var1, double var3, double var5, SkyGen.C var7) {
         var7.r = var7.g = var7.b = 0.0;
         var7.vis = 1.0;
         if (this.below != 0 && !(var3 >= 0.0)) {
            switch (this.below) {
               case 2:
                  this.ocean(var1, var3, var5, var7);
                  break;
               case 3:
                  this.cloudSea(var1, var3, var5, var7);
                  break;
               default:
                  this.groundBelow(var1, var3, var5, var7);
            }
         } else {
            this.sky(var1, var3, var5, var7);
         }
      }

      final void horizon(double var1, double var3, SkyGen.C var5) {
         double var6 = Math.sqrt(var1 * var1 + var3 * var3);
         if (var6 < 1.0E-9) {
            var1 = 1.0;
            var3 = 0.0;
            var6 = 1.0;
         }

         var5.r = var5.g = var5.b = 0.0;
         var5.vis = 1.0;
         this.sky(var1 / var6, 0.0, var3 / var6, var5);
      }

      void groundBelow(double var1, double var3, double var5, SkyGen.C var7) {
         this.horizon(var1, var5, var7);
         double var8 = 1.0 - Math.exp(var3 / this.groundFall);
         double[] var10 = this.ground;
         double var11 = var10[0];
         double var13 = var10[1];
         double var15 = var10[2];
         if (this.groundFar != null) {
            double var17 = Math.exp(var3 / (this.groundFall * 2.5));
            var11 += (this.groundFar[0] - var11) * var17;
            var13 += (this.groundFar[1] - var13) * var17;
            var15 += (this.groundFar[2] - var15) * var17;
         }

         if (this.groundNoise > 0.0) {
            double var25 = 1.0 / (-var3 + 0.02);
            double var19 = var1 * var25;
            double var21 = var5 * var25;
            double var23 = 1.0
               + this.groundNoise
                  * SkyGen.smooth(0.0, 0.3, -var3)
                  * SkyGen.fbmL(var19 * 0.6, 2.7, var21 * 0.6, Math.max(2.0, 5.0 - SkyGen.log2(1.0 + var25 * 0.3)));
            var11 *= var23;
            var13 *= var23;
            var15 *= var23;
         }

         var7.r = var7.r + (var11 - var7.r) * var8;
         var7.g = var7.g + (var13 - var7.g) * var8;
         var7.b = var7.b + (var15 - var7.b) * var8;
         var7.vis = this.starsBelow ? 1.0 : Math.max(0.0, 1.0 - var8 * 8.0) * var7.vis;
      }

      void ocean(double var1, double var3, double var5, SkyGen.C var7) {
         double var8 = -var3;
         double var10 = 1.0 / (var8 + 0.015);
         double var12 = var1 * var10;
         double var14 = var5 * var10;
         double var16 = SkyGen.smooth(0.0, 0.25, var8);
         double var18 = this.waves * 0.06 * var16;
         double var20 = var1;
         double var22 = var8;
         double var24 = var5;
         if (var18 > 0.0) {
            double var26 = 0.9;
            double var28 = SkyGen.noise(var12 * var26, 0.37, var14 * var26) + 0.5 * SkyGen.noise(var12 * var26 * 2.7, 1.7, var14 * var26 * 2.7);
            double var30 = SkyGen.noise(var12 * var26 + 11.3, 4.1, var14 * var26) + 0.5 * SkyGen.noise(var12 * var26 * 2.7 + 5.1, 2.9, var14 * var26 * 2.7);
            var20 = var1 + var28 * var18;
            var24 = var5 + var30 * var18;
            var22 = var8 + var8 * 0.3 * SkyGen.noise(var12 * 0.7, 8.3, var14 * 0.7) * var16;
         }

         var22 = Math.max(0.0, var22) * this.mirror;
         double var49 = Math.sqrt(var20 * var20 + var22 * var22 + var24 * var24);
         var7.r = var7.g = var7.b = 0.0;
         var7.vis = 1.0;
         this.sky(var20 / var49, var22 / var49, var24 / var49, var7);
         double var50 = 1.0 - var8;
         double var51 = var50 * var50;
         double var32 = 0.03 + 0.97 * var51 * var51 * var50;
         double var34 = Math.exp(-var8 / 0.012);
         var32 += (1.0 - var32) * var34;
         var7.r = this.water[0] + (var7.r - this.water[0]) * var32;
         var7.g = this.water[1] + (var7.g - this.water[1]) * var32;
         var7.b = this.water[2] + (var7.b - this.water[2]) * var32;
         if (this.glitter > 0.0) {
            double var36 = this.sun[0] - var1;
            double var38 = var8 + this.sun[1];
            double var40 = this.sun[2] - var5;
            if (var38 > 1.0E-4 && this.sun[1] > -0.05) {
               double var42 = (var36 * var36 + var40 * var40) / (var38 * var38);
               double var44 = Math.exp(-var42 / (this.rough * this.rough)) * this.glitter;
               if (var44 > 1.0E-4) {
                  double var46 = 0.55 + 0.9 * Math.max(0.0, SkyGen.noise(var12 * 6.0, 3.3, var14 * 6.0 * 0.3));
                  var44 *= var46 * (1.0 - var34);
                  var7.r = var7.r + this.sunCol[0] * var44;
                  var7.g = var7.g + this.sunCol[1] * var44;
                  var7.b = var7.b + this.sunCol[2] * var44;
               }
            }
         }

         var7.vis = 0.0;
      }

      void cloudSea(double var1, double var3, double var5, SkyGen.C var7) {
         this.horizon(var1, var5, var7);
         double var8 = var7.r;
         double var10 = var7.g;
         double var12 = var7.b;
         double var14 = -var3;
         SkyGen.Clouds var16 = this.sea;
         double var17 = var16.height / (var14 + 0.02);
         double var19 = var1 * var17 * var16.scale;
         double var21 = var5 * var17 * var16.scale;
         double var23 = Math.max(2.0, var16.oct - SkyGen.log2(1.0 + var17 * var16.scale * 0.35));
         double var25 = SkyGen.fbmL(var19 * 0.35, var16.seed, var21 * 0.35, 3.0);
         double var27 = SkyGen.fbmL(var19 * 0.35 + 5.2, var16.seed + 3.0, var21 * 0.35 + 1.3, 3.0);
         double var29 = var19 + var25 * var16.warp;
         double var31 = var21 + var27 * var16.warp;
         double var33 = SkyGen.fbmL(var29, var16.seed + 10.0, var31, var23);
         double var35 = SkyGen.smooth(var16.cov, var16.cov + var16.soft, var33);
         double var37 = SkyGen.fbmL(var29 + this.sun[0] * 0.3, var16.seed + 10.0, var31 + this.sun[2] * 0.3, Math.max(2.0, var23 - 2.0));
         double var39 = SkyGen.clamp01((var33 - var37) * 2.5 + 0.55);
         double[] var41 = var16.lit;
         double[] var42 = var16.shade;
         double var43 = var42[0] + (var41[0] - var42[0]) * var39;
         double var45 = var42[1] + (var41[1] - var42[1]) * var39;
         double var47 = var42[2] + (var41[2] - var42[2]) * var39;
         double var49 = this.seaGap[0] + (var43 - this.seaGap[0]) * var35;
         double var51 = this.seaGap[1] + (var45 - this.seaGap[1]) * var35;
         double var53 = this.seaGap[2] + (var47 - this.seaGap[2]) * var35;
         double var55 = Math.exp(-var14 / var16.fog);
         var7.r = var49 + (var8 - var49) * var55;
         var7.g = var51 + (var10 - var51) * var55;
         var7.b = var53 + (var12 - var53) * var55;
         var7.vis = 0.0;
      }
   }

   static final class Stars {
      static final int CS = 16;
      static final int STRIDE = 9;
      int nc;
      int[] start;
      int[] item;
      float[] data;

      static SkyGen.Stars build(SkyGen.Sky var0, int var1) {
         if (var0.stars <= 0 && var0.motes <= 0) {
            return null;
         } else {
            Random var2 = new Random(var0.starSeed * 7919L + 17L);
            double var3 = Math.sqrt(var1 / 1024.0);
            float var5 = (float)var0.exposure;
            int var6 = (var1 + 16 - 1) / 16;
            SkyGen.FloatList var7 = new SkyGen.FloatList();
            SkyGen.IntList var8 = new SkyGen.IntList();
            double[] var9 = new double[3];
            double[] var10 = new double[2];
            int var11 = var0.stars + var0.motes;

            for (int var12 = 0; var12 < var11; var12++) {
               boolean var13 = var12 >= var0.stars;
               double var14 = var2.nextDouble() * 2.0 - 1.0;
               double var16 = var2.nextDouble() * Math.PI * 2.0;
               double var18 = Math.sqrt(1.0 - var14 * var14);
               double var20 = var18 * Math.cos(var16);
               double var22 = var14;
               double var24 = var18 * Math.sin(var16);
               double var26 = var2.nextDouble();
               double var28 = var2.nextDouble();
               double var30;
               double var32;
               double var34;
               if (!var13) {
                  if (var26 > var0.starDensity(var20, var14, var24)) {
                     continue;
                  }

                  double var36 = Math.min(var0.starMax, var0.starMin * Math.pow(Math.max(1.0E-6, var28), -0.72));
                  var0.starColor(var2, var9);
                  var30 = (0.36 + 0.16 * Math.log(var36 / var0.starMin + 1.0)) * var0.starSize * var3;
                  var32 = var36;
                  var34 = var36 > 1.0 ? 0.01 * var36 * var0.starHalo : 0.0;
               } else {
                  if (var26 > var0.moteDensity(var20, var14, var24)) {
                     continue;
                  }

                  var0.moteColor(var2, var9);
                  double var57 = var0.moteBright * (0.25 + 1.2 * Math.pow(var28, 3.0));
                  var30 = (0.8 + var0.moteSize * Math.pow(var2.nextDouble(), 3.0)) * var3;
                  var32 = var57;
                  var34 = var57 * 0.1;
               }

               double var58 = var30 * (var34 > 0.0 ? 12.0 : 3.6);
               var58 = Math.min(var58, 48.0);
               float var38 = (float)(0.5 / (var30 * var30));

               for (int var39 = 0; var39 < 6; var39++) {
                  if (SkyGen.faceUV(var39, var20, var22, var24, var10)) {
                     double var40 = var10[0] * var1 - 0.5;
                     double var42 = var10[1] * var1 - 0.5;
                     if (!(var40 < -var58) && !(var42 < -var58) && !(var40 > var1 - 1 + var58) && !(var42 > var1 - 1 + var58)) {
                        var7.add((float)var40);
                        var7.add((float)var42);
                        var7.add((float)var9[0] * var5);
                        var7.add((float)var9[1] * var5);
                        var7.add((float)var9[2] * var5);
                        var7.add(var38);
                        var7.add((float)var32);
                        var7.add((float)var34);
                        var7.add(var13 ? 0.0F : 1.0F);
                        var8.add(var39);
                        var8.add((int)Math.ceil(var58));
                     }
                  }
               }
            }

            SkyGen.Stars var44 = new SkyGen.Stars();
            var44.nc = var6;
            var44.data = var7.toArray();
            int var45 = 6 * var6 * var6;
            int[] var46 = new int[var45 + 1];
            int var15 = var44.data.length / 9;

            for (int var47 = 0; var47 < 2; var47++) {
               if (var47 == 1) {
                  int var17 = 0;

                  for (int var49 = 0; var49 < var45; var49++) {
                     int var19 = var46[var49];
                     var46[var49] = var17;
                     var17 += var19;
                  }

                  var46[var45] = var17;
                  var44.start = (int[])var46.clone();
                  var44.item = new int[var17];
               }

               for (int var48 = 0; var48 < var15; var48++) {
                  int var50 = var8.get(var48 * 2);
                  int var51 = var8.get(var48 * 2 + 1);
                  float var52 = var44.data[var48 * 9];
                  float var21 = var44.data[var48 * 9 + 1];
                  int var53 = Math.max(0, (int)Math.floor((var52 - var51) / 16.0F));
                  int var23 = Math.min(var6 - 1, (int)Math.floor((var52 + var51) / 16.0F));
                  int var54 = Math.max(0, (int)Math.floor((var21 - var51) / 16.0F));
                  int var25 = Math.min(var6 - 1, (int)Math.floor((var21 + var51) / 16.0F));

                  for (int var55 = var54; var55 <= var25; var55++) {
                     for (int var27 = var53; var27 <= var23; var27++) {
                        int var56 = (var50 * var6 + var55) * var6 + var27;
                        if (var47 == 0) {
                           var46[var56]++;
                        } else {
                           var44.item[var46[var56]++] = var48;
                        }
                     }
                  }
               }
            }

            return var44;
         }
      }
   }

   static final class Towers {
      double h0 = 0.02;
      double h1 = 0.16;
      double freq = 2.2;
      double cut = 0.0;
      double bill = 7.0;
      double puff = 1.0;
      double soft = 0.015;
      double seed = 0.0;
      double haze = 0.035;
      double sunSide = 0.35;
      double[] lit = SkyGen.rgb(16777215);
      double[] shade = SkyGen.rgb(9214642);
   }
}
