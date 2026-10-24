package dev.lego.cosmetic;

import java.util.ArrayList;

final class Wings {
   private Wings() {
   }

   static void register() {
      Cos.add("wings_angel", "Engelsflügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, (var0, var1) -> feathered(var0, var1, "angel", 16.0F, false, 0));
      Cos.add("wings_gold", "Goldflügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, (var0, var1) -> feathered(var0, var1, "gold", 16.0F, false, 0));
      Cos.add("wings_phoenix", "Phönixflügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, (var0, var1) -> feathered(var0, var1, "phoenix", 17.0F, true, 1));
      Cos.add("wings_fire", "Feuerflügel", Cos.Slot.WINGS, Cos.Rarity.EPIC, (var0, var1) -> feathered(var0, var1, "fire", 16.0F, true, 2));
      Cos.add("wings_shadow", "Schattenflügel", Cos.Slot.WINGS, Cos.Rarity.EPIC, (var0, var1) -> feathered(var0, var1, "shadow", 16.0F, false, 3));
      Cos.add("wings_owl", "Eulenflügel", Cos.Slot.WINGS, Cos.Rarity.RARE, (var0, var1) -> feathered(var0, var1, "owl", 15.0F, false, 0));
      Cos.add("wings_rainbow", "Regenbogenflügel", Cos.Slot.WINGS, Cos.Rarity.EPIC, (var0, var1) -> feathered(var0, var1, "rainbow", 15.0F, true, 4));
      Cos.add("wings_galaxy", "Galaxieflügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, (var0, var1) -> feathered(var0, var1, "galaxy", 17.0F, true, 5));
      Cos.add("wings_crystal", "Kristallflügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, (var0, var1) -> feathered(var0, var1, "crystal", 16.0F, true, 5));
      Cos.add("wings_demon", "Dämonenflügel", Cos.Slot.WINGS, Cos.Rarity.EPIC, (var0, var1) -> membrane(var0, var1, "demon", 18.0F, -12969456, 0, false));
      Cos.add("wings_dragon", "Drachenflügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, (var0, var1) -> membrane(var0, var1, "dragon", 20.0F, -10868214, 0, false));
      Cos.add(
         "wings_icedragon",
         "Eisdrachenflügel",
         Cos.Slot.WINGS,
         Cos.Rarity.LEGENDARY,
         (var0, var1) -> membrane(var0, var1, "icedragon", 19.0F, -4198401, -7673601, true)
      );
      Cos.add("wings_neon", "Neonflügel", Cos.Slot.WINGS, Cos.Rarity.EPIC, (var0, var1) -> membrane(var0, var1, "neon", 17.0F, -15070678, -42256, true));
      Cos.add("wings_bat", "Fledermausflügel", Cos.Slot.WINGS, Cos.Rarity.RARE, (var0, var1) -> membrane(var0, var1, "bat", 15.0F, -14017514, 0, false));
      Cos.add("wings_butterfly", "Schmetterling", Cos.Slot.WINGS, Cos.Rarity.EPIC, HdWings::butterfly);
      Cos.add("wings_fairy", "Feenflügel", Cos.Slot.WINGS, Cos.Rarity.EPIC, HdWings::fairy);
      Cos.add("wings_cyber", "Cyberflügel", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, HdWings::cyber);
   }

   static Wings.Pose pose(Cos.A var0, float var1) {
      Wings.Pose var2 = new Wings.Pose();
      float var3 = var0.time;
      float var4 = var0.preview ? 0.0F : Math.max(0.0F, Math.min(1.0F, var0.move));
      float var5 = var0.sneak ? 1.0F : (var0.preview ? 0.0F : 0.22F * (1.0F - var4));
      float var6 = Geo.sin(var3 * 1.6) * 5.0F + Geo.sin(var3 * 0.7 + 1.0) * 3.0F;
      float var7 = Geo.sin(var3 * 7.5) * 26.0F;
      float var8 = (var6 * (1.0F - var4) + var7 * var4) * var1 * (var0.sneak ? 0.35F : 1.0F);
      var2.raise = Geo.lerp(14.0F, -62.0F, var5) + var8 + var4 * 4.0F;
      var2.sweep = Geo.lerp(26.0F, 8.0F, var5) - var4 * 10.0F + var8 * 0.3F;
      var2.fold = var5;
      var2.flap = var8;
      var2.t = var3;
      var2.mv = var4;
      return var2;
   }

   private static void root(G var0, Wings.Pose var1, int var2, float var3, float var4) {
      var0.scale(var2, 1.0F, 1.0F);
      var0.translate(var3, var4, -2.35F);
      var0.rotY(var1.sweep);
      var0.rotZ(var1.raise);
      var0.rotX(-8.0F);
   }

   static void feathered(G var0, Cos.A var1, String var2, float var3, boolean var4, int var5) {
      Wings.Pose var6 = pose(var1, 1.0F);
      String var7 = "fea_" + var2;
      String var8 = "cov_" + var2;

      for (byte var9 = 1; var9 >= -1; var9 -= 2) {
         var0.push();
         root(var0, var6, var9, 0.9F, -2.2F);
         var0.glow(var4);
         float var10 = var6.fold;
         float var11 = 1.0F - 0.18F * var10;
         float var12 = 3.5F + var6.flap * 0.18F;
         byte var13 = 9;

         for (int var14 = 0; var14 < var13; var14++) {
            float var15 = (float)var14 / (var13 - 1);
            float var16 = 0.58F + 0.42F * var15;
            float var17 = Geo.lerp(16.0F, 50.0F, var15) * (1.0F - 0.65F * var10);
            float var18 = var3
               * (0.74F + 0.26F * Geo.sin(Math.PI * (0.3 + 0.55 * var15)))
               * (var5 == 2 ? 0.9F + 0.12F * Geo.sin(var6.t * 9.0F + var14 * 1.7) : 1.0F);
            tint(var0, var5, var16, 0.86F, var6.t);
            feather(var0, var7, edge(var3, var16, var11, 0.1F + var14 * 0.012F), var17, var18, var3 * 0.17F, 4, var12);
         }

         byte var25 = 11;

         for (int var26 = 0; var26 < var25; var26++) {
            float var28 = (float)var26 / (var25 - 1);
            float var31 = 0.03F + 0.57F * var28;
            float var38 = Geo.lerp(-8.0F, 16.0F, var28) * (1.0F - 0.5F * var10);
            float var19 = var3 * (0.54F + 0.14F * var28) * (var5 == 2 ? 0.9F + 0.12F * Geo.sin(var6.t * 8.0F + var26 * 2.1) : 1.0F);
            tint(var0, var5, var31, 0.93F, var6.t);
            feather(var0, var7, edge(var3, var31, var11, 0.0F + var26 * 0.01F), var38, var19, var3 * 0.16F, 3, var12 * 0.8F);
         }

         byte var27 = 13;

         for (int var29 = 0; var29 < var27; var29++) {
            float var32 = (float)var29 / (var27 - 1);
            float var39 = 0.02F + 0.9F * var32;
            float var45 = (var32 < 0.62F ? Geo.lerp(-6.0F, 16.0F, var32 / 0.62F) : Geo.lerp(18.0F, 55.0F, (var32 - 0.62F) / 0.38F)) * (1.0F - 0.55F * var10);
            float var20 = var3 * (0.34F - 0.1F * var32);
            tint(var0, var5, var39, 1.0F, var6.t);
            feather(var0, var8, edge(var3, var39, var11, -0.22F), var45, var20, var3 * 0.15F, 2, var12 * 0.5F);
         }

         byte var30 = 14;

         for (int var33 = 0; var33 < var30; var33++) {
            float var40 = (float)var33 / (var30 - 1);
            float var46 = 0.0F + 0.96F * var40;
            float var51 = Geo.lerp(0.0F, 40.0F, var40) * (1.0F - 0.5F * var10);
            tint(var0, var5, var46, 1.0F, var6.t);
            float[] var21 = edge(var3, var46, var11, -0.42F);
            var21[1] += 0.35F;
            feather(var0, var8, var21, var51, var3 * (0.19F - 0.05F * var40), var3 * 0.13F, 1, 0.0F);
         }

         byte var34 = 10;
         float[][] var41 = new float[var34 + 1][];
         float[] var47 = new float[var34 + 1];
         float[] var52 = new float[var34 + 1];

         for (int var56 = 0; var56 <= var34; var56++) {
            float var22 = (float)var56 / var34;
            float[] var23 = edge(var3, var22 * 0.97F, var11, -0.3F);
            var41[var56] = new float[]{var23[0], var23[1] + 0.2F, var23[2]};
            var47[var56] = var3 * 0.05F * (1.2F - var22 * 0.75F);
            var52[var56] = var3 * 0.045F * (1.2F - var22 * 0.75F);
         }

         tint(var0, var5, 0.5F, 1.0F, var6.t);
         XmasGeo.sweep(var0, var8, var41, var47, var52, null, 6, 1.0F);
         var0.color(-1);
         if (var5 != 1 && var5 != 2) {
            if (var5 == 3) {
               var0.glow(true);

               for (int var37 = 0; var37 < 6; var37++) {
                  float var44 = (var6.t * 0.6F + var37 / 6.0F) % 1.0F;
                  float var50 = 0.2F + 0.75F * (var37 * 0.47F % 1.0F);
                  var52 = edge(var3, var50, var11, 0.4F);
                  float var58 = var3 * 0.7F + var44 * 6.0F;
                  var0.color(Geo.argb((int)(140.0 * Math.sin(var44 * Math.PI)), 16777215));
                  Geo.sprite(var0, "orb_purple", var52[0] + var50 * 4.0F, var52[1] - var58, var52[2] - 0.5F, 3.0F + var44 * 3.0F);
               }
            } else if (var5 == 5) {
               var0.glow(true);

               for (int var36 = 0; var36 < 8; var36++) {
                  float var43 = 0.5F + 0.5F * Geo.sin(var6.t * 3.0F + var36 * 2.3F);
                  float var49 = var36 * 0.37F % 1.0F;
                  var52 = edge(var3, 0.1F + 0.85F * var49, var11, -0.8F);
                  var0.color(Geo.argb((int)(255.0F * var43), 16777215));
                  Geo.sprite(var0, "spark", var52[0] + 2.0F, var52[1] - var3 * (0.15F + 0.4F * (var36 * 0.53F % 1.0F)), var52[2] - 0.6F, 1.2F + 1.6F * var43);
               }
            }
         } else {
            var0.glow(true);

            for (int var35 = 0; var35 < 7; var35++) {
               float var42 = (var6.t * 1.3F + var35 * 0.37F) % 1.0F;
               float var48 = 0.25F + 0.7F * (var35 * 0.61F % 1.0F);
               var52 = edge(var3, var48, var11, 0.3F);
               float var57 = (float)Math.toRadians(Geo.lerp(-8.0F, 60.0F, var48));
               float var59 = var3 * (0.55F + 0.35F * var48) + var42 * 5.0F;
               float var60 = var52[0] + (float)Math.sin(var57) * var59;
               float var24 = var52[1] - (float)Math.cos(var57) * var59 - var42 * 3.0F;
               var0.color(Geo.argb((int)(230.0 * Math.sin(var42 * Math.PI)), 16777215));
               Geo.sprite(var0, "flame", var60, var24 + 1.5F, var52[2], 3.2F * (1.0F - var42 * 0.5F));
            }
         }

         var0.color(-1).glow(false);
         var0.pop();
      }
   }

   private static void tint(G var0, int var1, float var2, float var3, float var4) {
      if (var1 == 4) {
         int var5 = Geo.hue(var2 * 0.75 + var4 * 0.12, 0.5, var3);
         var0.color(var5);
      } else {
         int var6 = (int)(255.0F * var3);
         var0.color(0xFF000000 | var6 << 16 | var6 << 8 | var6);
      }
   }

   private static float[] edge(float var0, float var1, float var2, float var3) {
      float var4 = var0 * var1 * var2;
      float var5 = var0 * (0.26F * Geo.sin(Math.PI * var1 * 0.92) + 0.1F * var1);
      float var6 = -var0 * 0.09F * Geo.sin(Math.PI * var1) + var3;
      return new float[]{var4, var5, var6};
   }

   private static void feather(G var0, String var1, float[] var2, float var3, float var4, float var5, int var6, float var7) {
      var0.push();
      var0.translate(var2[0], var2[1], var2[2]);
      var0.rotZ(var3);
      var0.rotY(-6.0F);
      int var8 = var6 + (var6 >= 3 ? 1 : 0);
      float var9 = var4 / var8;
      float var10 = var7 * var6 / var8;
      float var11 = var5 * 0.12F;

      for (int var12 = 0; var12 < var8; var12++) {
         float var13 = (float)var12 / var8;
         float var14 = (float)(var12 + 1) / var8;
         float var15 = var5 * (var12 == var8 - 1 && var8 > 1 ? 1.0F : 1.0F);
         float var16 = var5 * (var12 == var8 - 1 ? 0.55F : 1.0F);
         var0.quad(
            var1,
            new float[]{-var15 / 2.0F, 0.0F, -var11 * 0.3F},
            new float[]{0.0F, 0.0F, var11},
            new float[]{0.0F, -var9, var11},
            new float[]{-var16 / 2.0F, -var9, -var11 * 0.3F},
            new float[]{0.0F, var13, 0.5F, var13, 0.5F, var14, 0.5F - 0.5F * var16 / var5, var14}
         );
         var0.quad(
            var1,
            new float[]{0.0F, 0.0F, var11},
            new float[]{var15 / 2.0F, 0.0F, -var11 * 0.3F},
            new float[]{var16 / 2.0F, -var9, -var11 * 0.3F},
            new float[]{0.0F, -var9, var11},
            new float[]{0.5F, var13, 1.0F, var13, 0.5F + 0.5F * var16 / var5, var14, 0.5F, var14}
         );
         var0.translate(0.0F, -var9, 0.0F);
         var0.rotX(-var10);
      }

      var0.pop();
   }

   private static float[][] memPoints(float var0, float var1, float var2) {
      float[] var3 = new float[]{0.0F, 0.0F, 0.0F};
      float var4 = (float)Math.toRadians(34.0F - var1 * 10.0F);
      float[] var5 = new float[]{var3[0] + 0.42F * var0 * Geo.cos(var4), var3[1] + 0.42F * var0 * Geo.sin(var4), 0.0F};
      float var6 = (float)Math.toRadians(-6.0F - var1 * 22.0F);
      float[] var7 = new float[]{var5[0] + 0.44F * var0 * Geo.cos(var6), var5[1] + 0.44F * var0 * Geo.sin(var6), -0.6F};
      float[] var8 = new float[]{30.0F, -6.0F, -40.0F, -72.0F};
      float[] var9 = new float[]{0.64F, 0.74F, 0.62F, 0.5F};
      float[][] var10 = new float[][]{var3, var5, var7, null, null, null, null};

      for (int var11 = 0; var11 < 4; var11++) {
         float var12 = (float)Math.toRadians(Geo.lerp(var8[var11], -95.0F, var1 * 0.35F) - var2 * 0.1F * var11);
         float var13 = var9[var11] * var0 * (1.0F - 0.15F * var1);
         var10[3 + var11] = new float[]{var7[0] + var13 * Geo.cos(var12), var7[1] + var13 * Geo.sin(var12), var7[2] - 0.12F * var13};
      }

      return var10;
   }

   static void membrane(G var0, Cos.A var1, String var2, float var3, int var4, int var5, boolean var6) {
      Wings.Pose var7 = pose(var1, 1.2F);
      float[][] var8 = memPoints(var3, 0.0F, 0.0F);
      float[][] var9 = memPoints(var3, var7.fold, var7.flap);
      float[] var10 = new float[]{0.4F, -0.8F * var3, 0.2F};
      float[] var11 = var10;
      String var12 = "mem_" + var2;
      float var13 = -0.05F * var3;
      float var14 = 1.35F * var3;
      float var15 = 0.62F * var3;
      float var16 = -0.95F * var3;

      for (byte var17 = 1; var17 >= -1; var17 -= 2) {
         var0.push();
         root(var0, var7, var17, 1.0F, -1.6F);
         ArrayList var18 = new ArrayList();
         ArrayList var19 = new ArrayList();

         for (int var20 = 0; var20 < 4; var20++) {
            float[] var21 = var9[3 + var20];
            float[] var22 = var20 < 3 ? var9[4 + var20] : var10;
            float[] var23 = var8[3 + var20];
            float[] var24 = var20 < 3 ? var8[4 + var20] : var11;
            byte var25 = 5;

            for (int var26 = 0; var26 < var25; var26++) {
               float var27 = (float)var26 / var25;
               var18.add(scallop(var21, var22, var9[2], var27, var20 < 3 ? 0.26F : 0.12F));
               var19.add(scallop(var23, var24, var8[2], var27, var20 < 3 ? 0.26F : 0.12F));
            }
         }

         var18.add(var10);
         var19.add(var11);
         var18.add(var9[0]);
         var19.add(var8[0]);
         var18.add(var9[1]);
         var19.add(var8[1]);
         float[] var28 = var9[2];
         float[] var29 = var8[2];
         var0.glow(var6);
         var0.color(-1);

         for (int var30 = 0; var30 + 1 < var18.size(); var30++) {
            float[] var34 = uv(var29, var13, var14, var16, var15);
            float[] var36 = uv((float[])var19.get(var30), var13, var14, var16, var15);
            float[] var38 = uv((float[])var19.get(var30 + 1), var13, var14, var16, var15);
            Geo.tri(var0, var12, var28, (float[])var18.get(var30), (float[])var18.get(var30 + 1), var34, var36, var38);
         }

         var0.glow(false);
         var0.color(var4);
         Geo.tube(var0, "bone", var9[0], var9[1], 0.62F, 0.5F, 6);
         Geo.tube(var0, "bone", var9[1], var9[2], 0.5F, 0.38F, 6);

         for (int var31 = 0; var31 < 4; var31++) {
            float[] var35 = var9[3 + var31];
            float[] var37 = new float[]{
               Geo.lerp(var28[0], var35[0], 0.5F), Geo.lerp(var28[1], var35[1], 0.5F) + 0.4F, Geo.lerp(var28[2], var35[2], 0.5F) - 0.25F
            };
            Geo.tube(var0, "bone", var28, var37, 0.34F, 0.24F, 5);
            Geo.tube(var0, "bone", var37, var35, 0.24F, 0.06F, 5);
         }

         var0.push();
         var0.translate(var9[1][0], var9[1][1], var9[1][2]);
         var0.sphere("bone", 0.6F, 6, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
         var0.push();
         var0.translate(var28[0], var28[1], var28[2]);
         var0.sphere("bone", 0.5F, 6, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
         var0.color(-1515312);
         Geo.tube(var0, "horn", var28, new float[]{var28[0] + 0.6F, var28[1] + 1.9F, var28[2] - 0.2F}, 0.34F, 0.02F, 5);
         if (var5 != 0) {
            var0.glow(true);
            var0.color(var5);

            for (int var32 = 0; var32 + 1 < var18.size() - 3; var32++) {
               Geo.tube(var0, "white", (float[])var18.get(var32), (float[])var18.get(var32 + 1), 0.16F, 0.16F, 4);
            }

            for (int var33 = 0; var33 < 4; var33++) {
               Geo.tube(var0, "white", var28, var9[3 + var33], 0.1F, 0.05F, 4);
            }
         }

         var0.color(-1).glow(false);
         var0.pop();
      }
   }

   private static float[] scallop(float[] var0, float[] var1, float[] var2, float var3, float var4) {
      float var5 = (var0[0] + var1[0]) / 2.0F;
      float var6 = (var0[1] + var1[1]) / 2.0F;
      float var7 = (var0[2] + var1[2]) / 2.0F;
      float var8 = var5 + (var2[0] - var5) * var4;
      float var9 = var6 + (var2[1] - var6) * var4;
      float var10 = var7 + (var2[2] - var7) * var4;
      float var11 = 1.0F - var3;
      return new float[]{
         var11 * var11 * var0[0] + 2.0F * var11 * var3 * var8 + var3 * var3 * var1[0],
         var11 * var11 * var0[1] + 2.0F * var11 * var3 * var9 + var3 * var3 * var1[1],
         var11 * var11 * var0[2] + 2.0F * var11 * var3 * var10 + var3 * var3 * var1[2]
      };
   }

   private static float[] uv(float[] var0, float var1, float var2, float var3, float var4) {
      return new float[]{(var0[0] - var1) / (var2 - var1), (var4 - var0[1]) / (var4 - var3)};
   }

   static void butterfly(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = var1.preview ? 0.0F : var1.move;
      float var4 = var1.sneak
         ? 72.0F
         : Geo.lerp(18.0F + 22.0F * (0.5F + 0.5F * Geo.sin(var2 * 2.6)), 20.0F + 45.0F * (0.5F + 0.5F * Geo.sin(var2 * 12.0F)), var3);

      for (byte var5 = 1; var5 >= -1; var5 -= 2) {
         var0.push();
         var0.scale(var5, 1.0F, 1.0F);
         var0.translate(0.5F, 2.5F, -2.4F);
         var0.rotY(var4);
         var0.rotZ(8.0F);
         grid(var0, "wing_butterfly2", 13.5F, 17.0F, 5, 6, 0.014F, true);
         var0.pop();
      }

      var0.color(-15396836);
      Geo.tube(var0, "felt_black", new float[]{0.0F, 2.5F, -2.55F}, new float[]{0.0F, -9.0F, -2.55F}, 0.7F, 0.4F, 6);
      var0.color(-1);
   }

   private static void grid(G var0, String var1, float var2, float var3, int var4, int var5, float var6, boolean var7) {
      for (int var8 = 0; var8 < var5; var8++) {
         for (int var9 = 0; var9 < var4; var9++) {
            float var10 = (float)var9 / var4;
            float var11 = (float)(var9 + 1) / var4;
            float var12 = (float)var8 / var5;
            float var13 = (float)(var8 + 1) / var5;
            var0.quad(
               var1,
               gp(var2, var3, var10, var12, var6),
               gp(var2, var3, var11, var12, var6),
               gp(var2, var3, var11, var13, var6),
               gp(var2, var3, var10, var13, var6),
               new float[]{var10, var12, var11, var12, var11, var13, var10, var13}
            );
         }
      }
   }

   private static float[] gp(float var0, float var1, float var2, float var3, float var4) {
      float var5 = var2 * var0;
      float var6 = -var3 * var1;
      return new float[]{var5, var6, -var4 * (var5 * var5 + var6 * var6 * 0.4F)};
   }

   static void fairy(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = var1.preview ? 0.0F : var1.move;
      float var4 = var1.sneak
         ? 70.0F
         : Geo.lerp(22.0F + 18.0F * (0.5F + 0.5F * Geo.sin(var2 * 3.4)), 20.0F + 40.0F * (0.5F + 0.5F * Geo.sin(var2 * 16.0F)), var3);
      var0.glow(true);

      for (byte var5 = 1; var5 >= -1; var5 -= 2) {
         for (int var6 = 0; var6 < 2; var6++) {
            var0.push();
            var0.scale(var5, 1.0F, 1.0F);
            var0.translate(0.6F, var6 == 0 ? -1.5F : -4.0F, -2.4F);
            var0.rotY(var4 + (var6 == 0 ? 0 : 8) + Geo.sin(var2 * 3.4 + var6) * 3.0F);
            var0.rotZ(var6 == 0 ? 28.0F : -34.0F);
            float var7 = var6 == 0 ? 17.0F : 12.0F;
            float var8 = var6 == 0 ? 8.5F : 6.0F;
            var0.color(-419430401);

            for (int var9 = 0; var9 < 4; var9++) {
               float var10 = var9 / 4.0F;
               float var11 = (var9 + 1) / 4.0F;
               float var12 = -0.012F * (var10 * var7) * (var10 * var7);
               float var13 = -0.012F * (var11 * var7) * (var11 * var7);
               var0.quad(
                  "wing_fairy",
                  new float[]{var10 * var7, var8 / 2.0F, var12},
                  new float[]{var11 * var7, var8 / 2.0F, var13},
                  new float[]{var11 * var7, -var8 / 2.0F, var13},
                  new float[]{var10 * var7, -var8 / 2.0F, var12},
                  new float[]{var10, 0.0F, var11, 0.0F, var11, 1.0F, var10, 1.0F}
               );
            }

            var0.pop();
         }
      }

      for (int var14 = 0; var14 < 10; var14++) {
         float var15 = (var2 * 0.5F + var14 / 10.0F) % 1.0F;
         float var16 = Geo.sin(var14 * 2.7F) * 9.0F;
         float var17 = -4.0F - var14 % 3 * 1.5F;
         var0.color(Geo.argb((int)(255.0 * Math.sin(var15 * Math.PI)), 16777215));
         Geo.sprite(
            var0,
            var14 % 2 == 0 ? "spark" : "orb_cyan",
            var16 + Geo.sin(var2 * 2.0F + var14) * 1.2F,
            -2.0F - var15 * 16.0F,
            var17,
            1.6F * (1.0F - var15 * 0.5F)
         );
      }

      var0.color(-1).glow(false);
   }

   static void cyber(G var0, Cos.A var1) {
      Wings.Pose var2 = pose(var1, 0.8F);
      float var3 = 17.0F;

      for (byte var4 = 1; var4 >= -1; var4 -= 2) {
         var0.push();
         root(var0, var2, var4, 1.0F, -2.0F);
         float[] var5 = new float[]{0.0F, 0.0F, 0.0F};
         float[] var6 = new float[]{var3 * 0.4F, var3 * 0.22F, -0.3F};
         float[] var7 = new float[]{var3 * 0.82F * (1.0F - 0.15F * var2.fold), var3 * 0.26F, -0.8F};
         var0.color(-1);
         Geo.tube(var0, "mech_frame", var5, var6, 0.9F, 0.75F, 6);
         Geo.tube(var0, "mech_frame", var6, var7, 0.75F, 0.55F, 6);
         var0.glow(true);
         var0.color(-13965569);
         Geo.tube(var0, "white", new float[]{var5[0], var5[1] + 0.8F, var5[2]}, new float[]{var6[0], var6[1] + 0.7F, var6[2]}, 0.14F, 0.14F, 4);
         Geo.tube(var0, "white", new float[]{var6[0], var6[1] + 0.7F, var6[2]}, new float[]{var7[0], var7[1] + 0.5F, var7[2]}, 0.14F, 0.14F, 4);
         Geo.sprite(var0, "orb_cyan", var6[0], var6[1], var6[2] - 0.8F, 2.6F + 0.6F * Geo.sin(var2.t * 4.0F));
         Geo.sprite(var0, "orb_cyan", var7[0], var7[1], var7[2] - 0.8F, 2.2F + 0.6F * Geo.sin(var2.t * 4.0F + 1.0F));
         var0.glow(false);
         byte var8 = 8;

         for (int var9 = 0; var9 < var8; var9++) {
            float var10 = (float)var9 / (var8 - 1);
            float[] var11 = new float[]{
               Geo.lerp(1.5F, var7[0], var10), Geo.lerp(0.5F, var7[1], var10) - 0.3F, Geo.lerp(0.0F, var7[2], var10) - 0.2F + var9 * 0.05F
            };
            float var12 = Geo.lerp(-4.0F, 62.0F, var10) * (1.0F - 0.6F * var2.fold) + Geo.sin(var2.t * 2.0F + var9) * 1.5F;
            float var13 = var3 * (0.5F + 0.42F * Geo.sin(Math.PI * (0.25 + 0.6 * var10)));
            var0.push();
            var0.translate(var11[0], var11[1], var11[2]);
            var0.rotZ(var12);
            var0.rotY(-8.0F);
            float var14 = var3 * 0.14F;
            float[][] var15 = new float[][]{
               {0.0F, 0.0F, 1.0F, 1.0F},
               {0.0F, 0.0F, 1.0F, 1.0F},
               {0.0F, 0.0F, 0.1F, 1.0F},
               {0.9F, 0.0F, 1.0F, 1.0F},
               {0.0F, 0.0F, 1.0F, 0.05F},
               {0.0F, 0.95F, 1.0F, 1.0F}
            };
            var0.color(-1);
            var0.box6("mech_blade", -var14 / 2.0F, -var13, -0.18F, var14 / 2.0F, 0.0F, 0.18F, var15);
            var0.glow(true);
            var0.color(Geo.argb(200 + (int)(55.0F * Geo.sin(var2.t * 5.0F + var9)), 2811647));
            var0.quad(
               "glow_line",
               new float[]{-var14 / 2.0F, -0.3F, -0.2F},
               new float[]{var14 / 2.0F, -0.3F, -0.2F},
               new float[]{var14 / 2.0F, -0.8F, -0.2F},
               new float[]{-var14 / 2.0F, -0.8F, -0.2F},
               new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
            );
            var0.glow(false);
            var0.pop();
         }

         var0.color(-1);
         var0.pop();
      }

      var0.color(-1);
      var0.box("mech_frame", -2.2F, -5.5F, -3.3F, 2.2F, -0.8F, -2.05F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.glow(true);
      var0.color(-13965569);
      var0.push();
      var0.translate(0.0F, 0.0F, -3.36F);
      var0.plane("glow_line", 0.0F, -3.1F, 3.4F, 0.7F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.glow(false);
      var0.color(-1);
   }

   static final class Pose {
      float raise;
      float sweep;
      float fold;
      float flap;
      float t;
      float mv;
   }
}
