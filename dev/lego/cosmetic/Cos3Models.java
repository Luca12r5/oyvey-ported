package dev.lego.cosmetic;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

final class Cos3Models {
   private Cos3Models() {
   }

   static void registerAll() {
      Cos.cape("c3void", "Leerenmantel", Cos.Rarity.LEGENDARY);
      Cos.cape("c3nether", "Netherglut", Cos.Rarity.EPIC);
      Cos.cape("c3heaven", "Himmelsgold", Cos.Rarity.LEGENDARY);
      Cos.cape("c3sunflower", "Sonnenblume", Cos.Rarity.RARE);
      Cos.cape("c3pumpkin", "Kürbislaterne", Cos.Rarity.EPIC);
      Cos.cape("c3ghost", "Geisterchen", Cos.Rarity.EPIC);
      Cos.cape("c3skull", "Leuchtschädel", Cos.Rarity.EPIC);
      Cos.cape("c3bolt_purple", "Neonblitz Lila", Cos.Rarity.EPIC);
      Cos.cape("c3bolt_blue", "Neonblitz Blau", Cos.Rarity.EPIC);
      Cos.cape("c3bolt_green", "Neonblitz Grün", Cos.Rarity.EPIC);
      Cos.cape("c3bolt_orange", "Neonblitz Orange", Cos.Rarity.EPIC);
      Cos.cape("c3embertree", "Glutbaum", Cos.Rarity.LEGENDARY);
      Cos.add("hat_c3_crystalcrown", "Schwebende Kristallkrone", Cos.Slot.HAT, Cos.Rarity.LEGENDARY, Cos3Models::crystalCrown);
      Cos.add("hat_c3_planetring", "Planetenring", Cos.Slot.HAT, Cos.Rarity.LEGENDARY, Cos3Models::planetRing);
      Cos.add("hat_c3_rayhalo", "Strahlenkranz", Cos.Slot.HAT, Cos.Rarity.EPIC, Cos3Models::rayHalo);
      Cos.add("hat_c3_flamecrown", "Flammenkrone", Cos.Slot.HAT, Cos.Rarity.LEGENDARY, Cos3Models::flameCrown);
      Cos.add("hat_c3_bubble", "Blasenhelm", Cos.Slot.HAT, Cos.Rarity.EPIC, Cos3Models::bubbleHelmet);
      Cos.add("hat_c3_catphones", "Katzen-Kopfhörer", Cos.Slot.HAT, Cos.Rarity.EPIC, Cos3Models::catPhones);
      Cos.add("hat_c3_snowglobe", "Schneekugel", Cos.Slot.HAT, Cos.Rarity.EPIC, Cos3Models::snowGlobe);
      Cos.add("hat_c3_pumpkin", "Kürbiskopf", Cos.Slot.HAT, Cos.Rarity.EPIC, Cos3Models::pumpkinHat);
      Cos.add("hat_c3_band_bolt", "Blitz-Bandana", Cos.Slot.HAT, Cos.Rarity.RARE, (var0, var1) -> bandana(var0, var1, 0));
      Cos.add("hat_c3_band_fire", "Flammen-Bandana", Cos.Slot.HAT, Cos.Rarity.RARE, (var0, var1) -> bandana(var0, var1, 1));
      Cos.add("hat_c3_band_ice", "Frost-Stirnband", Cos.Slot.HAT, Cos.Rarity.RARE, (var0, var1) -> bandana(var0, var1, 2));
      Cos.add("face_c3_holovisor", "Holo-Visier", Cos.Slot.FACE, Cos.Rarity.LEGENDARY, Cos3Models::holoVisor);
      Cos.add("face_c3_neonshades", "Neon-Shades", Cos.Slot.FACE, Cos.Rarity.EPIC, Cos3Models::neonShades);
      Cos3Back.register();
      Cos3Aura.register();
   }

   static void flame(G var0, float var1, float var2, int var3, int var4) {
      boolean var5 = var0.glow;
      int var6 = var0.color;
      var0.glow(true);
      var0.color(var3);
      Cos3Geo.lathe(
         var0,
         "c3_flamegrad",
         new float[]{0.0F, var2 * 0.85F, var2, var2 * 0.72F, var2 * 0.36F, 0.0F},
         new float[]{0.0F, var1 * 0.09F, var1 * 0.24F, var1 * 0.48F, var1 * 0.74F, var1},
         10
      );
      var0.color(var4);
      Cos3Geo.lathe(
         var0,
         "white",
         new float[]{0.0F, var2 * 0.5F, var2 * 0.58F, var2 * 0.4F, 0.0F},
         new float[]{var1 * 0.04F, var1 * 0.12F, var1 * 0.26F, var1 * 0.46F, var1 * 0.68F},
         7
      );
      var0.glow(var5);
      var0.color(var6);
   }

   static float[] loopPt(float var0, float var1, double var2) {
      float var4 = (float)Math.cos(var2);
      float var5 = (float)Math.sin(var2);
      float var6 = Math.abs(var4) < 1.0E-4F ? 0.0F : Math.signum(var4);
      float var7 = Math.abs(var5) < 1.0E-4F ? 0.0F : Math.signum(var5);
      return new float[]{var6 * var0 + var1 * var4, var7 * var0 + var1 * var5};
   }

   static void flatRing(G var0, String var1, float var2, float var3, float var4, float var5, int var6) {
      Cos3Geo.lathe(var0, var1, new float[]{var2, var3, var3, var2, var2}, new float[]{var4, var4, var5, var5, var4}, var6);
   }

   static void sparkle(G var0, float var1, float var2, float var3, float var4, int var5) {
      boolean var6 = var0.glow;
      int var7 = var0.color;
      var0.glow(true).color(var5);
      var0.push();
      var0.translate(var1, var2, var3);
      Cos3Geo.gem(var0, "white", var4 * 0.5F, var4, var4, 4);
      var0.pop();
      var0.glow(var6).color(var7);
   }

   static void crystalCrown(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = Cos3Geo.sin(var2 * 2.0F) * 0.35F;
      var0.push();
      var0.translate(0.0F, 9.7F + var3, 0.0F);
      var0.rotY(var2 * 16.0F);
      var0.color(-11414);
      flatRing(var0, "c3_metal", 4.3F, 5.0F, 0.0F, 0.75F, 24);
      var0.color(-5720);
      var0.push();
      var0.translate(0.0F, 0.75F, 0.0F);
      var0.torus("c3_metal", 4.65F, 0.2F, 24, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();

      for (int var4 = 0; var4 < 8; var4++) {
         float var5 = var4 * 45;
         boolean var6 = var4 % 2 == 0;
         var0.push();
         var0.rotY(var5);
         var0.translate(4.65F, 0.55F, 0.0F);
         var0.rotZ(var6 ? 8.0F : 14.0F);
         var0.glow(true).color(var6 ? -527767297 : -523728129);
         Cos3Geo.shard(var0, "c3_crystal", var6 ? 0.62F : 0.45F, var6 ? 3.8F : 2.4F);
         var0.glow(false);
         var0.pop();
         var0.push();
         var0.rotY(var5 + 22.5F);
         var0.translate(5.0F, 0.38F, 0.0F);
         var0.rotZ(-90.0F);
         var0.glow(true).color(var4 % 2 == 0 ? -46470 : -11867905);
         Cos3Geo.gem(var0, "c3_crystal", 0.3F, 0.28F, 0.12F, 6);
         var0.glow(false);
         var0.pop();
      }

      var0.push();
      var0.translate(0.0F, 3.2F + Cos3Geo.sin(var2 * 2.6F) * 0.3F, 0.0F);
      var0.rotY(-var2 * 50.0F);
      var0.glow(true).color(-253168385);
      Cos3Geo.gem(var0, "c3_crystal", 1.0F, 1.9F, 1.5F, 6);
      var0.pop();
      Cos3Geo.halo(var0, 0.0F, 3.2F, 0.0F, 4.5F, -2139432705);
      var0.pop();

      for (int var9 = 0; var9 < 5; var9++) {
         float var10 = Cos3Geo.fract(var2 * 0.35F + var9 * 0.2F);
         float var11 = var9 * 72 + var2 * 30.0F;
         float var7 = Cos3Geo.cos(Math.toRadians(var11)) * 5.8F;
         float var8 = Cos3Geo.sin(Math.toRadians(var11)) * 5.8F;
         sparkle(var0, var7, 9.2F + var10 * 5.0F, var8, 0.35F * (1.0F - var10) + 0.1F, Cos3Geo.alpha(1.0F - var10, 14218495));
      }
   }

   static void planetRing(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(0.0F, 5.2F, 0.0F);
      var0.rotZ(16.0F);
      var0.rotX(-9.0F);
      var0.push();
      var0.rotY(var2 * 12.0F);
      var0.color(-1);
      var0.ring("c3_ring", 6.7F, 9.8F, 0.14F, 48, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.ring("c3_ring", 6.7F, 9.8F, -0.14F, 48, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1519456);
      var0.cylinder("white", 9.8F, 9.8F, -0.14F, 0.14F, 48, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.cylinder("white", 6.7F, 6.7F, -0.14F, 0.14F, 48, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.glow(true).color(-931469057);
      var0.torus("white", 6.45F, 0.09F, 40, 3, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.glow(false);
      var0.pop();
      String[] var3 = new String[]{"c3_planet_gas", "c3_planet_ice", "c3_planet_lava"};
      float[] var4 = new float[]{1.15F, 0.85F, 0.95F};
      float[] var5 = new float[]{26.0F, -34.0F, 40.0F};
      float[] var6 = new float[]{11.3F, 11.0F, 11.6F};

      for (int var7 = 0; var7 < 3; var7++) {
         float var8 = var2 * var5[var7] + var7 * 120;
         var0.push();
         var0.rotY(var8);
         var0.translate(var6[var7], Cos3Geo.sin(var2 * 1.5F + var7) * 0.4F, 0.0F);
         var0.rotY(-var2 * 60.0F);
         var0.color(-1);
         var0.sphere(var3[var7], var4[var7], 14, 8, 0.0F, 0.0F, 1.0F, 1.0F);
         if (var7 == 2) {
            Cos3Geo.halo(var0, 0.0F, 0.0F, 0.0F, var4[var7] * 4.0F, 1627351594);
         }

         if (var7 == 0) {
            var0.rotZ(20.0F);
            var0.color(-3880);
            var0.ring("c3_ring", var4[var7] * 1.35F, var4[var7] * 2.0F, 0.0F, 20, 0.0F, 0.0F, 1.0F, 1.0F);
         }

         var0.pop();
      }

      var0.pop();
   }

   static void rayHalo(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = Cos3Geo.sin(var2 * 1.8F) * 0.3F;
      var0.push();
      var0.translate(0.0F, 11.8F + var3, -0.4F);
      var0.rotX(8.0F);
      var0.glow(true).color(-7558);
      var0.torus("c3_metal", 4.3F, 0.42F, 32, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1325402416);
      var0.torus("white", 5.3F, 0.1F, 32, 3, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int var4 = 0; var4 < 16; var4++) {
         float var5 = var4 * 22.5F + var2 * 14.0F;
         float var6 = 2.2F + 1.8F * (0.5F + 0.5F * Cos3Geo.sin(var2 * 3.0F + var4 * 1.7F)) + (var4 % 2 == 0 ? 1.2F : 0.0F);
         var0.push();
         var0.rotY(var5);
         var0.color(Cos3Geo.alpha(0.75, var4 % 2 == 0 ? 16774320 : 16769162));
         limbRay(var0, 4.85F, var6, var4 % 2 == 0 ? 0.26F : 0.18F);
         var0.pop();
      }

      var0.glow(false);
      var0.pop();

      for (int var7 = 0; var7 < 4; var7++) {
         float var8 = Cos3Geo.fract(var2 * 0.4F + var7 * 0.25F);
         float var9 = var7 * 90 + var2 * 20.0F;
         sparkle(
            var0,
            Cos3Geo.cos(Math.toRadians(var9)) * 5.6F,
            12.6F - var8 * 2.5F,
            Cos3Geo.sin(Math.toRadians(var9)) * 5.6F - 0.4F,
            0.3F,
            Cos3Geo.alpha(1.0F - var8, 16774336)
         );
      }
   }

   private static void limbRay(G var0, float var1, float var2, float var3) {
      Geo.tube(var0, "c3_ray", new float[]{var1, 0.0F, 0.0F}, new float[]{var1 + var2, 0.0F, 0.0F}, var3, 0.02F, 4);
   }

   static void flameCrown(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = 2.6F;
      float var4 = 2.25F;
      float var5 = 0.55F;
      var0.color(-13355970);
      Cos3Geo.band(var0, "c3_metal", var3, var4, var5, 6.4F, 8.7F, 0.0F, 3.0F);
      var0.color(-16310);
      Cos3Geo.band(var0, "c3_metal", var3, var4 + var5 - 0.05F, 0.2F, 6.2F, 6.65F, 0.0F, 3.0F);
      Cos3Geo.band(var0, "c3_metal", var3, var4 + var5 - 0.05F, 0.2F, 8.45F, 8.9F, 0.0F, 3.0F);

      for (int var6 = 0; var6 < 8; var6++) {
         double var7 = var6 * Math.PI / 4.0;
         float[] var9 = loopPt(var3, var4 + var5 * 0.5F, var7);
         var0.push();
         var0.translate(var9[0], 8.7F, var9[1]);
         var0.color(-14013902);
         Cos3Geo.shard(var0, "c3_metal", 0.42F, 1.5F);
         var0.pop();
         float var10 = 1.0F + 0.2F * Cos3Geo.sin(var2 * 9.0F + var6 * 1.3F) + 0.1F * Cos3Geo.sin(var2 * 14.3F + var6 * 2.1F);
         var0.push();
         var0.translate(var9[0], 9.6F, var9[1]);
         var0.rotZ(Cos3Geo.sin(var2 * 3.0F + var6) * 6.0F);
         var0.scale(1.0F, var10, 1.0F);
         flame(var0, var6 % 2 == 0 ? 3.6F : 2.8F, 0.85F, -385910246, -251662176);
         var0.pop();
         float[] var11 = loopPt(var3, var4 + var5 + 0.05F, var7 + (Math.PI / 8));
         var0.push();
         var0.translate(var11[0], 7.55F, var11[1]);
         var0.rotY((float)(-Math.toDegrees(var7 + (Math.PI / 8))));
         var0.rotZ(-90.0F);
         var0.glow(true).color(-50646);
         Cos3Geo.gem(var0, "c3_crystal", 0.42F, 0.34F, 0.14F, 6);
         var0.glow(false);
         var0.pop();
      }

      for (int var12 = 0; var12 < 9; var12++) {
         float var13 = Cos3Geo.fract(var2 * 0.7F + var12 * 0.111F);
         float[] var8 = loopPt(var3, var4 + var5 * 0.5F, var12 * 0.7);
         sparkle(
            var0,
            var8[0] * (1.0F - var13 * 0.3F),
            11.0F + var13 * 5.0F,
            var8[1] * (1.0F - var13 * 0.3F),
            0.28F,
            Cos3Geo.alpha((1.0F - var13) * 0.9, var13 < 0.4 ? 16769146 : 16742954)
         );
      }
   }

   static void bubbleHelmet(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.color(-1512204);
      var0.push();
      var0.translate(0.0F, 0.75F, 0.0F);
      var0.torus("c3_metal", 6.4F, 0.45F, 28, 6, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int var3 = 0; var3 < 8; var3++) {
         double var4 = var3 * Math.PI / 4.0 + (Math.PI / 8);
         boolean var6 = ((int)Math.floor(var2 * 3.0F) + var3) % 4 != 0;
         var0.glow(true).color(var6 ? (var3 % 2 == 0 ? -11867905 : -38184) : -14009788);
         Cos3Geo.ball(var0, "white", (float)Math.cos(var4) * 6.4F, 0.4F, (float)Math.sin(var4) * 6.4F, 0.22F, 6);
      }

      var0.glow(false);
      var0.pop();
      byte var13 = 11;
      float var14 = 7.4F;
      float var5 = 4.0F;
      float[] var15 = new float[var13 + 1];
      float[] var7 = new float[var13 + 1];
      double var8 = Math.asin((0.75 - var5) / var14);

      for (int var10 = 0; var10 <= var13; var10++) {
         double var11 = var8 + ((Math.PI / 2) - var8) * var10 / var13;
         var15[var10] = (float)Math.cos(var11) * var14;
         var7[var10] = var5 + (float)Math.sin(var11) * var14;
      }

      var15[var13] = 0.0F;
      var0.color(-3617064);
      Geo.tube(var0, "c3_metal", new float[]{0.0F, 11.2F, 0.0F}, new float[]{0.0F, 13.6F, 0.0F}, 0.16F, 0.12F, 6);
      var0.glow(true).color((int)(var2 * 2.0F) % 2 == 0 ? -46486 : -8775126);
      Cos3Geo.ball(var0, "white", 0.0F, 13.8F, 0.0F, 0.42F, 8);
      Cos3Geo.halo(var0, 0.0F, 13.8F, 0.0F, 2.2F, 1895778922);
      var0.glow(false).color(-2558209);
      var0.push();
      var0.rotY(var2 * 8.0F);
      Cos3Geo.lathe(var0, "c3_glass", var15, var7, 28, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
   }

   static void catPhones(G var0, Cos.A var1) {
      float var2 = var1.time;
      int var3 = 0xFF000000 | Capes.hsv(0.88 + 0.08 * Math.sin(var2 * 1.5), 0.55, 1.0);
      float[][] var4 = new float[][]{{4.9F, 3.0F}, {4.95F, 6.0F}, {4.75F, 8.3F}, {3.4F, 9.2F}, {1.6F, 9.45F}, {0.0F, 9.5F}};
      float[][] var5 = new float[var4.length * 2 - 1][];

      for (int var6 = 0; var6 < var4.length; var6++) {
         var5[var6] = new float[]{var4[var6][0], var4[var6][1], 0.0F};
         var5[var5.length - 1 - var6] = new float[]{-var4[var6][0], var4[var6][1], 0.0F};
      }

      float[] var11 = new float[var5.length];
      Arrays.fill(var11, 0.4F);
      var0.color(-855818);
      Geo.chain(var0, "c3_metal", var5, var11, 8);
      float[][] var7 = new float[][]{{-3.0F, 9.35F, 0.0F}, {-1.5F, 9.55F, 0.0F}, {0.0F, 9.6F, 0.0F}, {1.5F, 9.55F, 0.0F}, {3.0F, 9.35F, 0.0F}};
      var0.color(-12961210);
      Cos3Geo.limb(var0, "c3_fur", var7, new float[]{0.5F, 0.55F, 0.55F, 0.55F, 0.5F}, 8);

      for (byte var8 = -1; var8 <= 1; var8 += 2) {
         var0.push();
         var0.scale(var8, 1.0F, 1.0F);
         var0.color(-13355970);
         Cos3Geo.bbox(var0, "c3_fur", 4.4F, 1.5F, -2.1F, 5.4F, 5.9F, 2.1F, 0.4F);
         var0.color(-14112);
         Cos3Geo.bbox(var0, "c3_metal", 5.3F, 1.2F, -2.4F, 6.6F, 6.2F, 2.4F, 0.55F);
         var0.push();
         var0.translate(6.62F, 3.7F, 0.0F);
         var0.rotZ(90.0F);
         var0.glow(true).color(var3);
         var0.torus("white", 1.45F, 0.18F, 20, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.color(-1);
         var0.pop();
         var0.glow(false).color(-1);
         var0.push();
         var0.translate(6.55F, 3.7F, 0.0F);
         var0.scale(0.25F, 1.0F, 1.0F);
         var0.sphere("c3_grille", 1.0F, 12, 6, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
         var0.push();
         var0.translate(2.7F, 9.1F, 0.0F);
         var0.rotZ(-14.0F);
         float[][] var9 = new float[][]{{-1.7F, 0.0F}, {1.6F, 0.0F}, {0.35F, 3.3F}};
         var0.color(-855818);
         Cos3Geo.extrude(var0, "c3_metal", var9, -0.5F, 0.5F);
         float[][] var10 = new float[][]{{-1.0F, 0.45F}, {0.95F, 0.45F}, {0.25F, 2.35F}};
         var0.glow(true).color(var3);
         Cos3Geo.extrude(var0, "white", var10, 0.2F, 0.66F);
         Cos3Geo.extrude(var0, "white", var10, -0.66F, -0.2F);
         var0.glow(false);
         var0.pop();
         var0.pop();
      }

      for (int var12 = 0; var12 < 2; var12++) {
         float var13 = Cos3Geo.fract(var2 * 0.45F + var12 * 0.5F);
         float var14 = var12 == 0 ? 1.0F : -1.0F;
         var0.push();
         var0.translate(var14 * (7.6F + var13 * 1.5F), 5.0F + var13 * 6.0F, Cos3Geo.sin(var2 * 2.0F + var12) * 0.8F);
         var0.glow(true).color(Cos3Geo.alpha(1.0F - var13, var3 & 16777215));
         Cos3Geo.ball(var0, "white", 0.0F, 0.0F, 0.0F, 0.42F, 6);
         Geo.tube(var0, "white", new float[]{0.35F, 0.0F, 0.0F}, new float[]{0.35F, 1.6F, 0.0F}, 0.1F, 0.1F, 4);
         Cos3Geo.bbox(var0, "white", 0.3F, 1.3F, -0.1F, 1.2F, 1.65F, 0.1F, 0.05F);
         var0.glow(false);
         var0.pop();
      }
   }

   static void snowGlobe(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(0.0F, 8.6F, 0.0F);
      var0.rotZ(Cos3Geo.sin(var2 * 1.4F) * 3.0F);
      var0.rotX(Cos3Geo.sin(var2 * 1.1F + 1.0F) * 2.0F);
      var0.color(-9814498);
      Cos3Geo.lathe(var0, "c3_lacquer", new float[]{0.0F, 3.5F, 3.7F, 3.4F, 3.0F, 2.8F, 0.0F}, new float[]{0.0F, 0.0F, 0.8F, 1.5F, 1.9F, 2.3F, 2.3F}, 20);
      var0.color(-11414);
      var0.push();
      var0.translate(0.0F, 1.55F, 0.0F);
      var0.torus("c3_metal", 3.42F, 0.2F, 22, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      float var3 = 5.85F;
      float var4 = 3.8F;
      var0.color(-722689);
      Geo.ellipsoid(var0, "c3_fur", 0.0F, 3.25F, 0.0F, 2.55F, 0.75F, 2.55F, 14, 6);
      var0.color(-13993398);
      Cos3Geo.lathe(var0, "c3_fur", new float[]{0.0F, 1.7F, 0.0F}, new float[]{3.6F, 4.3F, 6.2F}, 10);
      var0.color(-13397926);
      Cos3Geo.lathe(var0, "c3_fur", new float[]{0.0F, 1.35F, 0.0F}, new float[]{5.2F, 5.8F, 7.4F}, 10);
      var0.color(-12933016);
      Cos3Geo.lathe(var0, "c3_fur", new float[]{0.0F, 0.95F, 0.0F}, new float[]{6.6F, 7.1F, 8.4F}, 10);
      var0.push();
      var0.translate(0.0F, 8.55F, 0.0F);
      var0.rotY(var2 * 60.0F);
      var0.glow(true).color(-7558);
      Cos3Geo.gem(var0, "white", 0.38F, 0.4F, 0.3F, 5);
      var0.pop();
      Cos3Geo.halo(var0, 0.0F, 8.55F, 0.0F, 1.8F, -1862278534);

      for (int var5 = 0; var5 < 7; var5++) {
         float var6 = 4.6F + var5 * 0.55F;
         float var7 = 1.65F - var5 * 0.2F;
         float var8 = var5 * 2.4F;
         boolean var9 = ((int)(var2 * 3.0F) + var5) % 3 != 0;
         var0.glow(true).color(var9 ? (var5 % 3 == 0 ? -46518 : (var5 % 3 == 1 ? -11867905 : -8118)) : -12566464);
         Cos3Geo.ball(var0, "white", Cos3Geo.cos(var8) * var7, var6, Cos3Geo.sin(var8) * var7, 0.13F, 4);
      }

      var0.glow(false);
      var0.color(-1560502);
      Cos3Geo.bbox(var0, "white", 1.2F, 3.3F, 0.5F, 2.0F, 4.1F, 1.3F, 0.08F);
      var0.color(-7558);
      Cos3Geo.bbox(var0, "white", 1.51F, 3.28F, 0.48F, 1.69F, 4.14F, 1.32F, 0.03F);
      Random var13 = new Random(5L);
      var0.color(-1);

      for (int var14 = 0; var14 < 26; var14++) {
         float var15 = var13.nextFloat();
         float var16 = var13.nextFloat() * 6.28F + var2 * (0.6F + var13.nextFloat() * 0.6F);
         float var17 = 3.6F + Cos3Geo.fract(var15 - var2 * 0.12F) * 5.4F;
         float var10 = var17 - var3;
         float var11 = (float)Math.sqrt(Math.max(0.2, (double)((var4 - 0.5F) * (var4 - 0.5F) - var10 * var10)));
         float var12 = var11 * (0.3F + 0.7F * var13.nextFloat());
         var0.push();
         var0.translate(Cos3Geo.cos(var16) * var12, var17, Cos3Geo.sin(var16) * var12);
         var0.rotY(var16 * 57.0F + var2 * 90.0F);
         var0.box("white", -0.1F, -0.1F, -0.1F, 0.1F, 0.1F, 0.1F, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      var0.color(-1509121);
      var0.push();
      var0.translate(0.0F, var3, 0.0F);
      var0.rotY(20.0F);
      var0.sphere("c3_glass", var4, 22, 12, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.pop();
   }

   static void pumpkinHat(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = 0.8F + 0.12F * Cos3Geo.sin(var2 * 11.0F) + 0.08F * Cos3Geo.sin(var2 * 17.3F);
      var0.push();
      var0.translate(0.0F, 8.4F, 0.0F);
      var0.rotZ(6.0F);
      var0.translate(0.0F, 2.95F, 0.0F);
      var0.color(-30182);

      for (int var4 = 0; var4 < 8; var4++) {
         var0.push();
         var0.rotY(-var4 * 45);
         var0.color(var4 % 2 == 0 ? -30182 : -1017326);
         Geo.ellipsoid(var0, "c3_pumpkin", 1.6F, 0.0F, 0.0F, 2.1F, 2.7F, 1.8F, 12, 8);
         var0.pop();
      }

      var0.color(-10847702);
      Cos3Geo.limb(
         var0,
         "c3_fur",
         new float[][]{{0.0F, 2.3F, 0.0F}, {0.1F, 3.2F, 0.0F}, {0.5F, 3.9F, 0.1F}, {1.0F, 4.1F, 0.2F}},
         new float[]{0.55F, 0.45F, 0.35F, 0.3F},
         7
      );
      var0.color(-12940758);
      float[][] var10 = new float[10][];

      for (int var5 = 0; var5 < 10; var5++) {
         float var6 = var5 * 0.7F;
         float var7 = 1.1F - var5 * 0.08F;
         var10[var5] = new float[]{-0.3F - Cos3Geo.cos(var6) * var7, 2.8F + var5 * 0.08F, Cos3Geo.sin(var6) * var7};
      }

      float[] var11 = new float[10];
      Arrays.fill(var11, 0.12F);
      Geo.chain(var0, "white", var10, var11, 4);
      var0.push();
      var0.translate(0.5F, 2.9F, -0.6F);
      var0.rotX(-70.0F);
      var0.rotZ(30.0F);
      var0.color(-11884486);
      Cos3Geo.extrude(var0, "c3_fur", new float[][]{{0.0F, 0.0F}, {0.9F, 0.5F}, {1.8F, 0.1F}, {2.2F, -0.6F}, {1.1F, -0.7F}}, -0.08F, 0.08F);
      var0.pop();
      var0.glow(true).color(0xFF000000 | Capes.mix(16742928, 16771194, var3));
      float[][] var12 = new float[][]{{0.5F, 0.2F}, {1.9F, 0.2F}, {1.2F, 1.35F}};
      float[][] var13 = new float[][]{{-0.5F, 0.2F}, {-1.2F, 1.35F}, {-1.9F, 0.2F}};
      float[][] var8 = new float[][]{{0.3F, -0.25F}, {0.0F, 0.25F}, {-0.3F, -0.25F}};
      float[][] var9 = new float[][]{
         {2.0F, -0.55F},
         {1.2F, -1.0F},
         {0.8F, -0.75F},
         {0.4F, -1.1F},
         {0.0F, -0.85F},
         {-0.4F, -1.1F},
         {-0.8F, -0.75F},
         {-1.2F, -1.0F},
         {-2.0F, -0.55F},
         {-1.1F, -1.75F},
         {0.0F, -1.95F},
         {1.1F, -1.75F}
      };
      Cos3Geo.extrude(var0, "white", var12, 2.3F, 3.62F);
      Cos3Geo.extrude(var0, "white", var13, 2.3F, 3.62F);
      Cos3Geo.extrude(var0, "white", var8, 2.3F, 3.85F);
      Cos3Geo.extrude(var0, "white", var9, 2.3F, 3.72F);
      Cos3Geo.halo(var0, 0.0F, 0.0F, 4.2F, 5.0F, Cos3Geo.alpha(0.35 * var3, 16751146));
      var0.glow(false);
      var0.pop();
   }

   static void bandana(G var0, Cos.A var1, int var2) {
      float var3 = var1.time;
      String var4 = var2 == 0 ? "c3_band_bolt" : (var2 == 1 ? "c3_band_fire" : "c3_band_ice");
      int var5 = var2 == 0 ? -4653206 : (var2 == 1 ? -20438 : -4656897);
      float var6 = 2.5F;
      float var7 = 2.4F;
      float var8 = 0.35F;
      float var9 = 4.5F;
      float var10 = 6.9F;
      var0.color(-1);
      Cos3Geo.band(var0, var4, var6, var7, var8, var9, var10, 0.0F, 2.0F);
      float var11 = var6 + var7 + var8;
      var0.push();
      var0.translate(0.0F, 5.75F, -var11 - 0.25F);
      var0.rotZ(8.0F);
      Cos3Geo.bbox(var0, var4, -0.85F, -0.8F, -0.45F, 0.85F, 0.8F, 0.45F, 0.35F, new float[]{0.1F, 0.2F, 0.2F, 0.8F});
      var0.pop();
      float var12 = var1.preview ? 0.4F : 0.25F + var1.move * 1.2F;

      for (byte var13 = -1; var13 <= 1; var13 += 2) {
         byte var14 = 7;
         float[][] var15 = new float[var14][];
         float[] var16 = new float[var14];

         for (int var17 = 0; var17 < var14; var17++) {
            float var18 = (float)var17 / (var14 - 1);
            float var19 = Cos3Geo.sin(var3 * (4.0F + var12 * 4.0F) - var18 * 3.2F + (var13 > 0 ? 0.0F : 1.3F)) * (0.25F + var12 * 0.5F) * var18;
            float var20 = var18 * var18 * (1.2F + var12 * 3.2F);
            var15[var17] = new float[]{5.6F - var18 * (5.2F - var12 * 1.8F), -var11 - 0.5F - var18 * 1.4F - var20 + var19};
            var16[var17] = 1.25F - var18 * 0.35F;
         }

         Cos3Geo.ribbon(var0, var4, var13 * 0.72F, var15, var16, 0.2F);
      }

      for (int var23 = 0; var23 < 3; var23++) {
         double var27 = (Math.PI / 2) + (var23 - 1) * 0.9;
         float[] var34 = loopPt(var6, var7 + var8 - 0.1F, var27);
         var0.push();
         var0.translate(var34[0], 5.7F, var34[1]);
         var0.rotY((float)Math.toDegrees((Math.PI / 2) - var27));
         float var35 = 0.75F + 0.25F * Cos3Geo.sin(var3 * 4.0F + var23 * 2);
         var0.glow(true).color(Cos3Geo.alpha(var35, var5));
         if (var2 == 0) {
            float[][] var37 = new float[][]{{0.35F, 1.0F}, {-0.45F, -0.05F}, {0.0F, -0.05F}, {-0.3F, -1.0F}, {0.5F, 0.2F}, {0.05F, 0.2F}, {0.5F, 1.0F}};
            Cos3Geo.extrude(var0, "white", var37, -0.1F, 0.28F);
         } else if (var2 == 1) {
            var0.push();
            var0.translate(0.0F, -0.9F, 0.15F);
            var0.scale(1.0F, 0.9F + 0.2F * Cos3Geo.sin(var3 * 10.0F + var23), 1.0F);
            var0.glow(false);
            flame(var0, 1.9F, 0.5F, -520136166, -251666310);
            var0.pop();
         } else {
            var0.rotX(90.0F);
            Cos3Geo.shard(var0, "c3_crystal", 0.28F, 0.9F);
            var0.rotY(60.0F);
            var0.push();
            var0.rotZ(50.0F);
            Cos3Geo.shard(var0, "c3_crystal", 0.2F, 0.7F);
            var0.pop();
            var0.push();
            var0.rotZ(-50.0F);
            Cos3Geo.shard(var0, "c3_crystal", 0.2F, 0.7F);
            var0.pop();
         }

         var0.glow(false);
         var0.pop();
      }

      if (var2 == 0) {
         int var24 = (int)Math.floor(var3 * 5.0F);

         for (int var28 = 0; var28 < 2; var28++) {
            double var31 = Capes.hash(var24, var28, 17);
            if (!(var31 < 0.3)) {
               double var36 = Capes.hash(var24, var28, 18) * Math.PI * 2.0;
               double var38 = var36 + 0.5 + var31 * 0.8;
               float[] var21 = loopPt(var6, var7 + var8 + 0.45F, var36);
               float[] var22 = loopPt(var6, var7 + var8 + 0.45F, var38);
               Cos3Geo.arc(
                  var0,
                  new float[]{var21[0], 5.7F + (float)(var31 - 0.5) * 1.5F, var21[1]},
                  new float[]{var22[0], 5.7F - (float)(var31 - 0.5) * 1.5F, var22[1]},
                  0.6F,
                  0.06F,
                  var24 * 3 + var28,
                  var3,
                  -1061617814
               );
            }
         }
      } else if (var2 == 1) {
         for (int var25 = 0; var25 < 5; var25++) {
            float var29 = Cos3Geo.fract(var3 * 0.8F + var25 * 0.2F);
            float[] var32 = loopPt(var6, var7 + var8, (Math.PI / 2) + (var25 - 2) * 0.55);
            sparkle(var0, var32[0], 7.2F + var29 * 3.0F, var32[1], 0.22F, Cos3Geo.alpha(1.0F - var29, var29 < 0.3 ? 16769146 : 16738842));
         }
      } else {
         for (int var26 = 0; var26 < 6; var26++) {
            float var30 = Cos3Geo.fract(var3 * 0.3F + var26 / 6.0F);
            float var33 = var26 * 60 + var3 * 25.0F;
            sparkle(
               var0,
               Cos3Geo.cos(Math.toRadians(var33)) * 6.6F,
               5.7F - var30 * 2.5F + 1.2F,
               Cos3Geo.sin(Math.toRadians(var33)) * 6.6F,
               0.25F,
               Cos3Geo.alpha(1.0F - var30, 15268607)
            );
         }
      }
   }

   static void arcBand(G var0, String var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9) {
      float[][] var10 = new float[var9 + 1][];
      float[][] var11 = new float[var9 + 1][];

      for (int var12 = 0; var12 <= var9; var12++) {
         double var13 = Math.toRadians(var4 + (var5 - var4) * var12 / var9);
         var10[var12] = Geo.sq(var13, var2, var3);
         var11[var12] = Geo.sq(var13, var2 + var8, var3);
      }

      for (int var15 = 0; var15 < var9; var15++) {
         float var17 = (float)var15 / var9;
         float var14 = (float)(var15 + 1) / var9;
         var0.quad(
            var1,
            new float[]{var11[var15][0], var7, var11[var15][1]},
            new float[]{var11[var15 + 1][0], var7, var11[var15 + 1][1]},
            new float[]{var11[var15 + 1][0], var6, var11[var15 + 1][1]},
            new float[]{var11[var15][0], var6, var11[var15][1]},
            new float[]{1.0F - var17, 0.0F, 1.0F - var14, 0.0F, 1.0F - var14, 1.0F, 1.0F - var17, 1.0F}
         );
         var0.quad(
            var1,
            new float[]{var10[var15 + 1][0], var7, var10[var15 + 1][1]},
            new float[]{var10[var15][0], var7, var10[var15][1]},
            new float[]{var10[var15][0], var6, var10[var15][1]},
            new float[]{var10[var15 + 1][0], var6, var10[var15 + 1][1]},
            new float[]{1.0F - var14, 0.0F, 1.0F - var17, 0.0F, 1.0F - var17, 1.0F, 1.0F - var14, 1.0F}
         );
         var0.quad(
            var1,
            new float[]{var10[var15][0], var7, var10[var15][1]},
            new float[]{var10[var15 + 1][0], var7, var10[var15 + 1][1]},
            new float[]{var11[var15 + 1][0], var7, var11[var15 + 1][1]},
            new float[]{var11[var15][0], var7, var11[var15][1]},
            new float[]{1.0F - var17, 0.0F, 1.0F - var14, 0.0F, 1.0F - var14, 0.05F, 1.0F - var17, 0.05F}
         );
         var0.quad(
            var1,
            new float[]{var11[var15][0], var6, var11[var15][1]},
            new float[]{var11[var15 + 1][0], var6, var11[var15 + 1][1]},
            new float[]{var10[var15 + 1][0], var6, var10[var15 + 1][1]},
            new float[]{var10[var15][0], var6, var10[var15][1]},
            new float[]{1.0F - var17, 0.95F, 1.0F - var14, 0.95F, 1.0F - var14, 1.0F, 1.0F - var17, 1.0F}
         );
      }

      for (int var16 = 0; var16 <= var9; var16 += var9) {
         var0.quad(
            var1,
            new float[]{var10[var16][0], var7, var10[var16][1]},
            new float[]{var11[var16][0], var7, var11[var16][1]},
            new float[]{var11[var16][0], var6, var11[var16][1]},
            new float[]{var10[var16][0], var6, var10[var16][1]},
            new float[]{0.0F, 0.0F, 0.05F, 0.0F, 0.05F, 1.0F, 0.0F, 1.0F}
         );
      }
   }

   static void holoVisor(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = 4.95F;
      float var4 = 6.0F;
      var0.glow(true).color(-1);
      arcBand(var0, "c3_visor", var3, var4, -12.0F, 192.0F, 2.6F, 5.1F, 0.28F, 36);
      var0.glow(false);

      for (int var5 = 0; var5 < 2; var5++) {
         float var6 = var5 == 0 ? 5.18F : 2.52F;
         byte var7 = 28;
         float[][] var8 = new float[var7 + 1][];

         for (int var9 = 0; var9 <= var7; var9++) {
            float[] var10 = Geo.sq(Math.toRadians(-14.0 + 208.0 * var9 / var7), var3 + 0.14F, var4);
            var8[var9] = new float[]{var10[0], var6, var10[1]};
         }

         float[] var16 = new float[var7 + 1];
         Arrays.fill(var16, 0.17F);
         var0.color(-14012872);
         Geo.chain(var0, "c3_metal", var8, var16, 5);
      }

      for (byte var11 = -1; var11 <= 1; var11 += 2) {
         var0.color(-14012872);
         Cos3Geo.bbox(var0, "c3_metal", var11 > 0 ? 4.45F : -5.75F, 2.2F, -1.9F, var11 > 0 ? 5.75F : -4.45F, 5.5F, 0.6F, 0.4F);

         for (int var13 = 0; var13 < 3; var13++) {
            boolean var14 = ((int)(var2 * 4.0F) + var13 + (var11 > 0 ? 0 : 1)) % 3 != 0;
            var0.glow(true).color(var14 ? -11867905 : -15054246);
            Cos3Geo.ball(var0, "white", var11 * 5.8F, 4.6F - var13 * 0.8F, -0.6F, 0.2F, 5);
         }

         var0.glow(false);
      }

      double var12 = Math.toRadians(90.0 + 78.0 * Math.sin(var2 * 1.8));
      float[] var15 = Geo.sq(var12, var3 + 0.33F, var4);
      var0.push();
      var0.translate(var15[0], 3.85F, var15[1]);
      var0.rotY((float)Math.toDegrees((Math.PI / 2) - var12));
      var0.glow(true).color(-521601025);
      Cos3Geo.bbox(var0, "white", -0.09F, -1.2F, -0.05F, 0.09F, 1.2F, 0.07F, 0.02F);
      var0.pop();
      Cos3Geo.halo(var0, var15[0] * 1.02F, 3.85F, var15[1] * 1.02F, 1.6F, 1615522047);
      var0.push();
      var0.translate(-2.0F, 3.9F, var3 + 0.95F);
      var0.rotZ(var2 * 40.0F);
      var0.glow(true).color(-1337267969);
      var0.plane("c3_hex", 0.0F, 0.0F, 1.8F, 1.8F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.glow(false);
      var0.pop();
   }

   static void neonShades(G var0, Cos.A var1) {
      float var2 = var1.time;
      int var3 = 0xFF000000 | Capes.hsv(0.8 + 0.12 * Math.sin(var2 * 1.2), 0.75, 1.0);
      byte var4 = 18;

      for (byte var5 = -1; var5 <= 1; var5 += 2) {
         float var6 = var5 * 2.1F;
         float var7 = 3.75F;
         float[][] var8 = new float[var4][];
         float[][] var9 = new float[var4 + 1][];

         for (int var10 = 0; var10 < var4; var10++) {
            double var11 = (Math.PI * 2) * var10 / var4;
            float[] var13 = Geo.sq(var11, 1.0F, 3.2F);
            float var14 = 1.65F;
            float var15 = var13[1] < 0.0F ? 1.25F : 1.0F;
            float var16 = var13[0] * var14;
            float var17 = var13[1] * var15;
            if (var5 < 0) {
               var16 = -var16;
            }

            var8[var10] = new float[]{var6 + var16, var7 + var17};
            var9[var10] = new float[]{var6 + var16 * 1.07F, var7 + var17 * 1.08F, 4.5F};
         }

         if (var5 < 0) {
            Collections.reverse(Arrays.asList(var8));
         }

         var9[var4] = var9[0];
         var0.color(-520093697);
         Cos3Geo.extrude(var0, "c3_lens", var8, 4.36F, 4.6F);
         float[] var20 = new float[var4 + 1];
         Arrays.fill(var20, 0.2F);
         var0.glow(true).color(var3);
         Geo.chain(var0, "white", var9, var20, 5);
         float[][] var21 = new float[][]{{var5 * 3.85F, 4.35F, 4.45F}, {var5 * 4.5F, 4.35F, 3.7F}, {var5 * 4.52F, 4.3F, 0.0F}, {var5 * 4.52F, 3.6F, -2.2F}};
         Geo.chain(var0, "white", var21, new float[]{0.19F, 0.18F, 0.16F, 0.14F}, 5);
         var0.glow(false);
      }

      var0.glow(true).color(var3);
      Geo.chain(var0, "white", new float[][]{{0.45F, 4.25F, 4.5F}, {0.0F, 4.45F, 4.5F}, {-0.45F, 4.25F, 4.5F}}, new float[]{0.2F, 0.2F, 0.2F}, 5);
      var0.glow(false);
      float var18 = Cos3Geo.fract(var2 * 0.35F);
      if (var18 < 0.3F) {
         float var19 = 4.0F - var18 / 0.3F * 8.0F;
         sparkle(var0, var19, 4.3F, 4.75F, 0.35F, -520093697);
      }
   }
}
