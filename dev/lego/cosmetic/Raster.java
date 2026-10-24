package dev.lego.cosmetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public final class Raster {
   private static final float[] KEY = norm(-0.42F, 0.72F, 0.55F);
   private static final float[] FILL = norm(0.75F, 0.1F, 0.45F);
   private static final float[] HALF = norm(-0.42F, 0.72F, 1.55F);
   static final int MQ_SKIN = -9274999;
   static final int MQ_SHIRT = -11380116;
   static final int MQ_PANTS = -12367785;

   private Raster() {
   }

   public static int[] render(Cos.Item var0, int var1, int var2, float var3, float var4, float var5, boolean var6, int var7) {
      return render(var0, null, var1, var2, var3, var4, var5, var6, var7);
   }

   public static int[] renderPose(float[] var0, int var1, int var2, float var3, float var4, int var5) {
      return render(null, var0, var1, var2, var3, var4, 0.0F, true, var5);
   }

   private static float[] norm(float var0, float var1, float var2) {
      float var3 = (float)Math.sqrt(var0 * var0 + var1 * var1 + var2 * var2);
      return new float[]{var0 / var3, var1 / var3, var2 / var3};
   }

   private static int[] render(Cos.Item var0, float[] var1, int var2, int var3, float var4, float var5, float var6, boolean var7, int var8) {
      float var9 = (float)Math.cos(Math.toRadians(var4));
      float var10 = (float)Math.sin(Math.toRadians(var4));
      float var11 = (float)Math.cos(Math.toRadians(var5));
      float var12 = (float)Math.sin(Math.toRadians(var5));
      float[] var13 = focus(var0 == null ? null : var0.slot);
      ArrayList var14 = new ArrayList();
      ArrayList var15 = new ArrayList();
      ArrayList var16 = new ArrayList();
      ArrayList var17 = new ArrayList();
      ArrayList var18 = new ArrayList();
      G.Sink var19 = (var10x, var11x, var12x, var13x, var14x, var15x, var16x, var17x) -> {
         float[] var18x = new float[12];

         for (int var19x = 0; var19x < 4; var19x++) {
            float var20x = var11x[var19x * 3] - var13[0];
            float var21x = var11x[var19x * 3 + 1] - var13[1];
            float var22x = var11x[var19x * 3 + 2] - var13[2];
            float var23x = var20x * var9 - var22x * var10;
            float var24x = var20x * var10 + var22x * var9;
            float var25x = var21x * var11 - var24x * var12;
            float var26x = var21x * var12 + var24x * var11;
            var18x[var19x * 3] = var23x;
            var18x[var19x * 3 + 1] = var25x;
            var18x[var19x * 3 + 2] = var26x;
         }

         float var27x = var15x * var9 - var17x * var10;
         float var28x = var15x * var10 + var17x * var9;
         float var29x = var16x * var11 - var28x * var12;
         float var30x = var16x * var12 + var28x * var11;
         var14.add(var18x);
         var15.add(new float[]{var27x, var29x, var30x});
         var16.add(var12x);
         var17.add(var10x);
         var18.add(new int[]{var13x, var14x ? 1 : 0});
      };
      G var20 = new G(var19);
      Cos.A var21 = new Cos.A();
      var21.time = var6;
      var21.preview = true;
      boolean var22 = var0 != null && var0.slot == Cos.Slot.PET;
      boolean var23 = var0 != null && var0.slot == Cos.Slot.VEHICLE;
      Vehicles.Stance var24 = var23 ? Vehicles.stance(var0.id) : null;
      boolean var25 = var7 && !var22 && (!var23 || var24 != Vehicles.Stance.SIT && var24 != Vehicles.Stance.PEDAL && var24 != Vehicles.Stance.RIDE);
      if (var25) {
         mannequin(var20, var1);
      }

      if (var0 != null) {
         var20.push();
         if (var0.slot == Cos.Slot.CAPE) {
            Models.capeGeometry(var20, var21, var0.cape);
         } else if (var23) {
            var20.translate(0.0F, -24.0F - Math.max(0.0F, Vehicles.lift(var0.id, var6)), 0.0F);
            var0.model.render(var20, var21);
         } else {
            var0.model.render(var20, var21);
         }

         var20.pop();
      }

      if (var25 && var0 != null && var0.slot != Cos.Slot.HAT && var0.slot != Cos.Slot.FACE || var1 != null) {
         groundShadow(var20);
      }

      int var26 = var14.size();
      float[][] var27 = var14.toArray(new float[0][]);
      float[][] var28 = var15.toArray(new float[0][]);
      int[] var29 = new int[var26];

      for (int var30 = 0; var30 < var26; var30++) {
         var29[var30] = Smooth.group((String)var17.get(var30), ((int[])var18.get(var30))[1] != 0);
      }

      float[] var61 = Smooth.normals(var27, var28, var29, var26);
      ArrayList var31 = new ArrayList(var26 * 2);
      int[][] var32 = new int[][]{{0, 1, 2}, {0, 2, 3}};
      HashMap var33 = new HashMap();

      for (int var34 = 0; var34 < var26; var34++) {
         float[] var35 = var27[var34];
         float[] var36 = var28[var34];
         float[] var37 = (float[])var16.get(var34);
         int var38 = ((int[])var18.get(var34))[0];
         boolean var39 = ((int[])var18.get(var34))[1] != 0;
         CTex.T var40 = var33.computeIfAbsent((String)var17.get(var34), CTex::get);
         float var41 = (var35[0] + var35[3] + var35[6] + var35[9]) / 4.0F;
         float var42 = (var35[1] + var35[4] + var35[7] + var35[10]) / 4.0F;
         float var43 = (var35[2] + var35[5] + var35[8] + var35[11]) / 4.0F;
         float var44 = distance(var0 == null ? null : var0.slot);
         float var45 = -var41;
         float var46 = -var42;
         float var47 = var44 - var43;
         float var48 = var36[0] * var45 + var36[1] * var46 + var36[2] * var47 < 0.0F ? -1.0F : 1.0F;
         float[] var49 = new float[4];
         float[] var50 = new float[4];

         for (int var51 = 0; var51 < 4; var51++) {
            if (var39) {
               var49[var51] = 1.0F;
            } else {
               float var52 = var61[var34 * 12 + var51 * 3] * var48;
               float var53 = var61[var34 * 12 + var51 * 3 + 1] * var48;
               float var54 = var61[var34 * 12 + var51 * 3 + 2] * var48;
               float var55 = var52 * KEY[0] + var53 * KEY[1] + var54 * KEY[2];
               float var56 = var52 * FILL[0] + var53 * FILL[1] + var54 * FILL[2];
               float var57 = Math.max(0.0F, var52 * HALF[0] + var53 * HALF[1] + var54 * HALF[2]);
               float var58 = 1.0F - Math.max(0.0F, var54);
               float var59 = 0.36F
                  + 0.62F * Math.max(0.0F, var55)
                  + 0.16F * Math.max(0.0F, var56)
                  + 0.2F * var58 * var58 * var58
                  + 0.07F * Math.max(0.0F, var53);
               float var60 = (float)Math.pow(var57, 28.0) * 0.22F;
               var49[var51] = var59;
               var50[var51] = var60;
            }
         }

         for (int[] var90 : var32) {
            Raster.Tri var93 = new Raster.Tri();

            for (int var96 = 0; var96 < 3; var96++) {
               System.arraycopy(var35, var90[var96] * 3, var93.p, var96 * 3, 3);
               var93.uv[var96 * 2] = var37[var90[var96] * 2];
               var93.uv[var96 * 2 + 1] = var37[var90[var96] * 2 + 1];
               var93.vs[var96] = var49[var90[var96]];
               var93.sp[var96] = var50[var90[var96]];
            }

            var93.tex = var40;
            var93.color = var38;
            var93.glow = var39;
            var93.depth = (var93.p[2] + var93.p[5] + var93.p[8]) / 3.0F;
            var31.add(var93);
         }
      }

      var31.sort((var0x, var1x) -> Float.compare(var0x.depth, var1x.depth));
      byte var62 = 2;
      int var63 = var2 * var62;
      int var64 = var3 * var62;
      float[] var65 = new float[var63 * var64];
      Arrays.fill(var65, -1.0E9F);
      float[] var66 = new float[var63 * var64];
      float[] var67 = new float[var63 * var64];
      float[] var68 = new float[var63 * var64];
      float[] var69 = new float[var63 * var64];
      float var70 = distance(var0 == null ? null : var0.slot);
      float var71 = (float)(var64 * 1.9);

      for (Raster.Tri var74 : var31) {
         raster(var74, var63, var64, var70, var71, var65, var66, var67, var68, var69);
      }

      int[] var73 = new int[var2 * var3];
      float var75 = (var8 >> 16 & 0xFF) / 255.0F;
      float var76 = (var8 >> 8 & 0xFF) / 255.0F;
      float var77 = (var8 & 0xFF) / 255.0F;
      float var78 = (var8 >>> 24) / 255.0F;

      for (int var79 = 0; var79 < var3; var79++) {
         for (int var80 = 0; var80 < var2; var80++) {
            float var82 = 0.0F;
            float var85 = 0.0F;
            float var88 = 0.0F;
            float var91 = 0.0F;

            for (int var94 = 0; var94 < var62; var94++) {
               for (int var97 = 0; var97 < var62; var97++) {
                  int var99 = (var79 * var62 + var94) * var63 + var80 * var62 + var97;
                  var82 += var66[var99];
                  var85 += var67[var99];
                  var88 += var68[var99];
                  var91 += var69[var99];
               }
            }

            int var95 = var62 * var62;
            var82 /= var95;
            var85 /= var95;
            var88 /= var95;
            var91 /= var95;
            float var98 = var91 + var78 * (1.0F - var91);
            float var100 = var82 + var75 * var78 * (1.0F - var91);
            float var101 = var85 + var76 * var78 * (1.0F - var91);
            float var102 = var88 + var77 * var78 * (1.0F - var91);
            if (var98 > 1.0E-4F) {
               var100 /= var98;
               var101 /= var98;
               var102 /= var98;
            }

            var73[var79 * var2 + var80] = clamp(var98) << 24 | clamp(var100) << 16 | clamp(var101) << 8 | clamp(var102);
         }
      }

      return var73;
   }

   private static void groundShadow(G var0) {
      int var1 = var0.color;
      boolean var2 = var0.glow;
      var0.color(-1711276032).glow(true);
      var0.quad(
         "mannequin_shadow",
         new float[]{-9.0F, -24.05F, -7.0F},
         new float[]{9.0F, -24.05F, -7.0F},
         new float[]{9.0F, -24.05F, 7.0F},
         new float[]{-9.0F, -24.05F, 7.0F},
         new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
      );
      var0.color(var1).glow(var2);
   }

   private static int clamp(float var0) {
      return Math.max(0, Math.min(255, Math.round(var0 * 255.0F)));
   }

   private static float[] focus(Cos.Slot var0) {
      if (var0 == null) {
         return new float[]{0.0F, -5.0F, 0.0F};
      } else {
         switch (var0) {
            case HAT:
               return new float[]{0.0F, 9.0F, 0.0F};
            case FACE:
               return new float[]{0.0F, 5.0F, 0.0F};
            case VEHICLE:
               return new float[]{0.0F, -17.0F, 0.0F};
            case PET:
               return new float[]{0.0F, 5.0F, 0.0F};
            case AURA:
               return new float[]{0.0F, -11.0F, 0.0F};
            case WINGS:
               return new float[]{0.0F, -6.0F, -4.0F};
            case CAPE:
            case BACK:
               return new float[]{0.0F, -7.0F, -2.0F};
            default:
               return new float[]{0.0F, -6.0F, 0.0F};
         }
      }
   }

   private static float distance(Cos.Slot var0) {
      if (var0 == null) {
         return 82.0F;
      } else {
         switch (var0) {
            case HAT:
               return 40.0F;
            case FACE:
               return 28.0F;
            case VEHICLE:
               return 130.0F;
            case PET:
               return 30.0F;
            case AURA:
               return 66.0F;
            case WINGS:
               return 86.0F;
            default:
               return 48.0F;
         }
      }
   }

   private static void raster(Raster.Tri var0, int var1, int var2, float var3, float var4, float[] var5, float[] var6, float[] var7, float[] var8, float[] var9) {
      float[] var10 = new float[3];
      float[] var11 = new float[3];
      float[] var12 = new float[3];

      for (int var13 = 0; var13 < 3; var13++) {
         float var14 = var3 - var0.p[var13 * 3 + 2];
         if (var14 < 1.0F) {
            return;
         }

         var12[var13] = 1.0F / var14;
         var10[var13] = var1 / 2.0F - var0.p[var13 * 3] * var4 * var12[var13];
         var11[var13] = var2 / 2.0F - var0.p[var13 * 3 + 1] * var4 * var12[var13];
      }

      float var40 = (var10[1] - var10[0]) * (var11[2] - var11[0]) - (var10[2] - var10[0]) * (var11[1] - var11[0]);
      if (!(Math.abs(var40) < 1.0E-6F)) {
         int var41 = Math.max(0, (int)Math.floor(Math.min(var10[0], Math.min(var10[1], var10[2]))));
         int var15 = Math.min(var1 - 1, (int)Math.ceil(Math.max(var10[0], Math.max(var10[1], var10[2]))));
         int var16 = Math.max(0, (int)Math.floor(Math.min(var11[0], Math.min(var11[1], var11[2]))));
         int var17 = Math.min(var2 - 1, (int)Math.ceil(Math.max(var11[0], Math.max(var11[1], var11[2]))));
         float var18 = (var0.color >> 16 & 0xFF) / 255.0F;
         float var19 = (var0.color >> 8 & 0xFF) / 255.0F;
         float var20 = (var0.color & 0xFF) / 255.0F;
         float var21 = (var0.color >>> 24) / 255.0F;

         for (int var22 = var16; var22 <= var17; var22++) {
            for (int var23 = var41; var23 <= var15; var23++) {
               float var24 = var23 + 0.5F;
               float var25 = var22 + 0.5F;
               float var26 = ((var10[1] - var24) * (var11[2] - var25) - (var10[2] - var24) * (var11[1] - var25)) / var40;
               float var27 = ((var10[2] - var24) * (var11[0] - var25) - (var10[0] - var24) * (var11[2] - var25)) / var40;
               float var28 = 1.0F - var26 - var27;
               if (!(var26 < -1.0E-4F) && !(var27 < -1.0E-4F) && !(var28 < -1.0E-4F)) {
                  float var29 = var26 * var12[0] + var27 * var12[1] + var28 * var12[2];
                  int var30 = var22 * var1 + var23;
                  if (!(var29 < var5[var30] - 1.0E-7F)) {
                     float var31 = (var26 * var0.uv[0] * var12[0] + var27 * var0.uv[2] * var12[1] + var28 * var0.uv[4] * var12[2]) / var29;
                     float var32 = (var26 * var0.uv[1] * var12[0] + var27 * var0.uv[3] * var12[1] + var28 * var0.uv[5] * var12[2]) / var29;
                     int var33 = bilinear(var0.tex, var31, var32);
                     float var34 = (var33 >>> 24) / 255.0F * var21;
                     if (!(var34 < 0.02F)) {
                        float var35 = var26 * var0.vs[0] + var27 * var0.vs[1] + var28 * var0.vs[2];
                        float var36 = var26 * var0.sp[0] + var27 * var0.sp[1] + var28 * var0.sp[2];
                        float var37 = (var33 >> 16 & 0xFF) / 255.0F * var18 * var35 + var36;
                        float var38 = (var33 >> 8 & 0xFF) / 255.0F * var19 * var35 + var36;
                        float var39 = (var33 & 0xFF) / 255.0F * var20 * var35 + var36;
                        if (var0.glow) {
                           var37 *= 1.1F;
                           var38 *= 1.1F;
                           var39 *= 1.1F;
                        }

                        var37 = Math.min(1.0F, var37);
                        var38 = Math.min(1.0F, var38);
                        var39 = Math.min(1.0F, var39);
                        var6[var30] = var37 * var34 + var6[var30] * (1.0F - var34);
                        var7[var30] = var38 * var34 + var7[var30] * (1.0F - var34);
                        var8[var30] = var39 * var34 + var8[var30] * (1.0F - var34);
                        var9[var30] = var34 + var9[var30] * (1.0F - var34);
                        if (var34 > 0.5F) {
                           var5[var30] = var29;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static int bilinear(CTex.T var0, float var1, float var2) {
      float var3 = var1 * var0.w - 0.5F;
      float var4 = var2 * var0.h - 0.5F;
      int var5 = (int)Math.floor(var3);
      int var6 = (int)Math.floor(var4);
      float var7 = var3 - var5;
      float var8 = var4 - var6;
      int var9 = px(var0, var5, var6);
      int var10 = px(var0, var5 + 1, var6);
      int var11 = px(var0, var5, var6 + 1);
      int var12 = px(var0, var5 + 1, var6 + 1);
      int var13 = 0;

      for (byte var14 = 0; var14 < 32; var14 += 8) {
         float var15 = (var9 >>> var14 & 0xFF) * (1.0F - var7) + (var10 >>> var14 & 0xFF) * var7;
         float var16 = (var11 >>> var14 & 0xFF) * (1.0F - var7) + (var12 >>> var14 & 0xFF) * var7;
         var13 |= (Math.round(var15 * (1.0F - var8) + var16 * var8) & 0xFF) << var14;
      }

      return var13;
   }

   private static int px(CTex.T var0, int var1, int var2) {
      var1 = Math.max(0, Math.min(var0.w - 1, var1));
      var2 = Math.max(0, Math.min(var0.h - 1, var2));
      return var0.argb[var2 * var0.w + var1];
   }

   static void mannequin(G var0, float[] var1) {
      int var2 = var0.color;
      float var3 = var1 != null && !Float.isNaN(var1[3]) ? var1[3] : 0.0F;
      var0.push();
      if (var3 != 0.0F) {
         var0.translate(0.0F, -12.0F, 0.0F);
         var0.rotX((float)Math.toDegrees(var3));
         var0.translate(0.0F, 12.0F, 0.0F);
      }

      var0.color(-9274999);
      part(var0, var1, 0, 0.0F, 0.0F, 0.0F, -4.0F, 0.0F, -4.0F, 4.0F, 8.0F, 4.0F, "mannequin_head");
      float[] var4 = var1 == null ? null : new float[]{Float.NaN, Float.NaN, Float.NaN, Float.NaN, var1[4], var1[5]};
      var0.color(-11380116);
      part(var0, var4, 3, 0.0F, 0.0F, 0.0F, -4.0F, -12.0F, -2.0F, 4.0F, 0.0F, 2.0F, "mannequin");
      var0.color(-9274999);
      part(var0, var1, 6, 5.0F, -2.0F, 0.0F, -1.0F, -10.0F, -2.0F, 3.0F, 2.0F, 2.0F, "mannequin");
      part(var0, var1, 9, -5.0F, -2.0F, 0.0F, -3.0F, -10.0F, -2.0F, 1.0F, 2.0F, 2.0F, "mannequin");
      var0.pop();
      var0.color(-12367785);
      part(var0, var1, 12, 1.9F, -12.0F, 0.0F, -2.0F, -12.0F, -2.0F, 2.0F, 0.0F, 2.0F, "mannequin");
      part(var0, var1, 15, -1.9F, -12.0F, 0.0F, -2.0F, -12.0F, -2.0F, 2.0F, 0.0F, 2.0F, "mannequin");
      var0.color(var2);
   }

   private static void part(
      G var0,
      float[] var1,
      int var2,
      float var3,
      float var4,
      float var5,
      float var6,
      float var7,
      float var8,
      float var9,
      float var10,
      float var11,
      String var12
   ) {
      var0.push();
      var0.translate(var3, var4, var5);
      if (var1 != null) {
         float var13 = var1[var2];
         float var14 = var1[var2 + 1];
         float var15 = var1[var2 + 2];
         if (!Float.isNaN(var15)) {
            var0.rotZ((float)Math.toDegrees(var15));
         }

         if (!Float.isNaN(var14)) {
            var0.rotY((float)Math.toDegrees(var14));
         }

         if (!Float.isNaN(var13)) {
            var0.rotX((float)Math.toDegrees(var13));
         }
      }

      Cos3Geo.bbox(var0, var12, var6, var7, var8, var9, var10, var11, 0.45F);
      var0.pop();
   }

   private static final class Tri {
      float[] p = new float[9];
      float[] uv = new float[6];
      CTex.T tex;
      int color;
      boolean glow;
      float[] vs = new float[3];
      float[] sp = new float[3];
      float depth;
   }
}
