package dev.lego.cosmetic;

final class Cos3Aura {
   private Cos3Aura() {
   }

   static void register() {
      Cos.add("aura_c3_splash", "Wasser-Splash", Cos.Slot.AURA, Cos.Rarity.EPIC, Cos3Aura::splash);
      Cos.add("aura_c3_book", "Zauberbuch-Orbit", Cos.Slot.AURA, Cos.Rarity.LEGENDARY, Cos3Aura::bookOrbit);
      Cos.add("aura_c3_hexshield", "Hex-Schild", Cos.Slot.AURA, Cos.Rarity.EPIC, Cos3Aura::hexShield);
      Cos.add("aura_c3_tesla", "Tesla-Feld", Cos.Slot.AURA, Cos.Rarity.LEGENDARY, Cos3Aura::tesla);
      Cos.add("aura_c3_planets", "Mini-Sonnensystem", Cos.Slot.AURA, Cos.Rarity.LEGENDARY, Cos3Aura::planets);
      Cos.add("wings_c3_mech", "Mecha-Schwingen", Cos.Slot.WINGS, Cos.Rarity.LEGENDARY, Cos3Aura::mechWings);
      Cos.add("wings_c3_prism", "Prismenflügel", Cos.Slot.WINGS, Cos.Rarity.EPIC, Cos3Aura::prismWings);
   }

   static float mv(Cos.A var0) {
      return var0.preview ? 0.0F : Cos3Geo.clamp(var0.move, 0.0F, 1.0F);
   }

   static void splash(G var0, Cos.A var1) {
      float var2 = var1.time;

      for (int var3 = 0; var3 < 2; var3++) {
         byte var4 = 40;
         float var5 = var3 == 0 ? 6.1F : 7.0F;
         float[][] var6 = new float[var4 + 1][];

         for (int var7 = 0; var7 <= var4; var7++) {
            double var8 = (Math.PI * 2) * var7 / var4;
            float var10 = var3 == 0 ? -13.4F + 0.45F * Cos3Geo.sin(var8 * 3.0 + var2 * 3.0F) : -14.2F + 0.35F * Cos3Geo.sin(var8 * 4.0 - var2 * 2.5F);
            var6[var7] = new float[]{(float)Math.cos(var8) * var5, var10, (float)Math.sin(var8) * var5};
         }

         float[] var25 = new float[var4 + 1];

         for (int var27 = 0; var27 <= var4; var27++) {
            var25[var27] = (var3 == 0 ? 0.3F : 0.18F) * (0.75F + 0.25F * Cos3Geo.sin(var27 * Math.PI * 2.0 / var4 * 5.0 + var2 * 4.0F));
         }

         var0.color(var3 == 0 ? -1203062529 : -927405825);
         Geo.chain(var0, "white", var6, var25, 6);
      }

      for (int var11 = 0; var11 < 2; var11++) {
         float var15 = Cos3Geo.fract(var2 * 0.55F + var11 * 0.5F);
         float var19 = 5.2F + var15 * 4.2F;
         float var23 = 2.1F * (1.0F - var15) + 0.3F;
         var0.color(Cos3Geo.alpha((1.0F - var15) * 0.95, 16777215));
         var0.cylinder("c3_splash", var19, var19 * 1.12F, -15.0F, -15.0F + var23, 44, false, 0.0F, 0.0F, 1.0F, 1.0F);
      }

      for (int var12 = 0; var12 < 12; var12++) {
         float var16 = Cos3Geo.fract(var2 * 0.5F + var12 / 12.0F);
         float var20 = var12 * 30 + var2 * 20.0F;
         float var24 = 9.1F + 2.2F * var16;
         float var26 = -14.0F + 34.0F * var16 * (1.0F - var16);
         float var28 = Cos3Geo.smooth(var16 / 0.08F) * Cos3Geo.smooth((1.0F - var16) / 0.08F);
         if (!(var28 < 0.02F)) {
            var0.push();
            var0.rotY(var20);
            var0.translate(var24, var26, 0.0F);
            Cos3Geo.alignY(var0, 2.5F, 34.0F * (1.0F - 2.0F * var16), 0.0F);
            var0.scale(var28 * (0.8F + 0.3F * (var12 % 3) / 2.0F));
            var0.color(-931474177);
            Cos3Geo.lathe(var0, "white", new float[]{0.0F, 0.42F, 0.5F, 0.34F, 0.14F, 0.0F}, new float[]{-0.5F, -0.35F, -0.05F, 0.35F, 0.7F, 0.95F}, 10);
            var0.color(-251658241);
            Cos3Geo.ball(var0, "white", 0.18F, 0.0F, 0.44F, 0.13F, 5);
            var0.pop();
         }
      }

      for (int var13 = 0; var13 < 3; var13++) {
         float var17 = Cos3Geo.fract(var2 * 0.4F + var13 / 3.0F);
         var0.push();
         var0.translate(0.0F, -23.85F + var13 * 0.02F, 0.0F);
         var0.rotX(-90.0F);
         var0.color(Cos3Geo.alpha((1.0F - var17) * 0.8, 16777215));
         float var21 = 9.0F + var17 * 18.0F;
         var0.plane("c3_ripple", 0.0F, 0.0F, var21, var21, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      for (int var14 = 0; var14 < 8; var14++) {
         float var18 = Cos3Geo.fract(var2 * 0.9F + var14 * 0.125F);
         float var22 = var14 * 45 + var2 * 40.0F;
         Cos3Models.sparkle(
            var0,
            Cos3Geo.cos(Math.toRadians(var22)) * (6.5F + var18 * 3.0F),
            -14.5F + var18 * 2.0F,
            Cos3Geo.sin(Math.toRadians(var22)) * (6.5F + var18 * 3.0F),
            0.22F,
            Cos3Geo.alpha(1.0F - var18, 15268607)
         );
      }

      var0.color(-1);
   }

   static void bookOrbit(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = var2 * 32.0F;

      for (int var4 = 1; var4 <= 7; var4++) {
         double var5 = Math.toRadians(var3 - var4 * 7);
         Cos3Models.sparkle(
            var0,
            (float)Math.cos(var5) * 11.2F,
            -3.0F + Cos3Geo.sin(var2 * 1.7F - var4 * 0.2F) * 0.6F + Cos3Geo.sin(var4 * 1.7F) * 0.5F,
            (float)(-Math.sin(var5)) * 11.2F,
            0.28F - var4 * 0.025F,
            Cos3Geo.alpha(0.9 - var4 * 0.11, 14199039)
         );
      }

      var0.push();
      var0.rotY(var3);
      var0.translate(11.2F, -3.0F + Cos3Geo.sin(var2 * 1.7F) * 0.6F, 0.0F);
      var0.rotY(90.0F);
      var0.rotX(-28.0F);
      var0.rotZ(Cos3Geo.sin(var2 * 1.3F) * 5.0F);
      var0.scale(1.35F);
      float[] var10 = new float[]{0.01F, 0.01F, 0.04F, 0.04F};

      for (byte var11 = -1; var11 <= 1; var11 += 2) {
         var0.push();
         var0.rotY(-18 * var11);
         var0.color(-1);
         float var6 = var11 > 0 ? 0.0F : -3.25F;
         float var7 = var11 > 0 ? 3.25F : 0.0F;
         Cos3Geo.bbox(var0, "c3_cover", var6, -2.25F, -0.4F, var7, 2.25F, -0.16F, 0.08F);
         float var8 = var11 > 0 ? 0.1F : -3.0F;
         float var9 = var11 > 0 ? 3.0F : -0.1F;
         var0.box6(
            "c3_page",
            var8,
            -2.05F,
            -0.2F,
            var9,
            2.05F,
            0.3F,
            new float[][]{var11 > 0 ? new float[]{0.0F, 0.0F, 1.0F, 1.0F} : new float[]{1.0F, 0.0F, 0.0F, 1.0F}, var10, var10, var10, var10, var10}
         );
         var0.pop();
      }

      var0.color(-10872198);
      Geo.tube(var0, "c3_cover", new float[]{0.0F, -2.25F, -0.3F}, new float[]{0.0F, 2.25F, -0.3F}, 0.3F, 0.3F, 8);
      float var12 = Cos3Geo.fract(var2 * 0.45F);
      if (var12 < 0.6F) {
         float var13 = 18.0F + 144.0F * Cos3Geo.smooth(var12 / 0.6F);
         var0.push();
         var0.translate(0.0F, 0.0F, 0.32F);
         var0.rotY(-var13);
         var0.color(-1);
         float var15 = Cos3Geo.sin(Math.PI * var12 / 0.6F) * 0.5F;
         var0.quad(
            "c3_page",
            new float[]{0.0F, 2.0F, 0.0F},
            new float[]{1.45F, 2.0F, var15},
            new float[]{1.45F, -2.0F, var15},
            new float[]{0.0F, -2.0F, 0.0F},
            new float[]{0.0F, 0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.0F, 1.0F}
         );
         var0.quad(
            "c3_page",
            new float[]{1.45F, 2.0F, var15},
            new float[]{2.9F, 2.0F, var15 * 0.6F},
            new float[]{2.9F, -2.0F, var15 * 0.6F},
            new float[]{1.45F, -2.0F, var15},
            new float[]{0.5F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.5F, 1.0F}
         );
         var0.pop();
      }

      var0.glow(true);

      for (int var14 = 0; var14 < 3; var14++) {
         float var16 = Cos3Geo.fract(var2 * 0.6F + var14 / 3.0F);
         var0.color(Cos3Geo.alpha(Math.sin(Math.PI * var16) * 0.95, var14 == 1 ? 16767082 : 13142783));
         var0.push();
         var0.translate(Cos3Geo.sin(var14 * 2.1F + var16 * 2.0F) * 1.3F, 0.6F + var16 * 4.5F, 0.9F + var16 * 0.9F);
         var0.rotY(Cos3Geo.sin(var2 * 2.0F + var14) * 25.0F);
         float var17 = 0.9F + var16 * 0.7F;
         var0.plane("c3_glyphs", 0.0F, 0.0F, var17, var17, var14 / 4.0F, 0.0F, (var14 + 1) / 4.0F, 1.0F);
         var0.pop();
      }

      var0.glow(false);
      Cos3Geo.halo(var0, 0.0F, 0.4F, 1.0F, 3.2F, 1354263295);
      var0.pop();
      var0.color(-1);
   }

   static void hexShield(G var0, Cos.A var1) {
      float var2 = var1.time;
      float[][] var3 = new float[6][];

      for (int var4 = 0; var4 < 6; var4++) {
         double var5 = Math.toRadians(30 + 60 * var4);
         var3[var4] = new float[]{(float)Math.cos(var5) * 1.55F, (float)Math.sin(var5) * 1.55F};
      }

      var0.glow(true);

      for (int var12 = 0; var12 < 3; var12++) {
         float var13 = -1.5F - var12 * 5.2F;
         float var6 = 10.5F - Math.abs(var12 - 1) * 0.8F;
         float var7 = var12 % 2 == 0 ? 1.0F : -1.0F;
         float var8 = var7 * var2 * 18.0F + var12 * 18;

         for (int var9 = 0; var9 < 10; var9++) {
            float var10 = 0.5F + 0.5F * Cos3Geo.sin(var2 * 2.5F + var9 * 0.9F + var12 * 1.7F);
            boolean var11 = Capes.hash((int)Math.floor(var2 * 2.0F), var9 + var12 * 10, 3) > 0.93;
            var0.push();
            var0.rotY(var8 + var9 * 36);
            var0.translate(var6, var13, 0.0F);
            var0.rotY(90.0F);
            var0.rotX((var12 - 1) * 18);
            var0.color(var11 ? -251658241 : Cos3Geo.alpha(0.35 + 0.45 * var10, var12 == 1 ? 4909311 : 6990079));
            Cos3Geo.extrude(var0, "c3_hex", var3, -0.1F, 0.1F, Cos3Geo.FULL, new float[]{0.48F, 0.04F, 0.52F, 0.08F});
            var0.pop();
         }
      }

      var0.glow(false);
      var0.push();
      var0.translate(0.0F, -23.8F, 0.0F);
      var0.rotY(var2 * 20.0F);
      var0.glow(true).color(1615522047);
      var0.torus("white", 9.8F, 0.12F, 40, 3, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.glow(false);
      var0.pop();
      var0.color(-1);
   }

   static void tesla(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = 10.0F;
      float[][] var4 = new float[3][];
      double[] var5 = new double[3];

      for (int var6 = 0; var6 < 3; var6++) {
         var5[var6] = Math.toRadians(var2 * 40.0F + var6 * 120);
         var4[var6] = new float[]{
            (float)Math.cos(var5[var6]) * var3, -4.0F + Cos3Geo.sin(var2 * 1.3F + var6 * 2) * 2.5F - var6 * 1.2F, (float)(-Math.sin(var5[var6])) * var3
         };
      }

      for (int var17 = 0; var17 < 3; var17++) {
         float[] var7 = var4[var17];
         var0.glow(true).color(-1);
         Cos3Geo.ball(var0, "white", var7[0], var7[1], var7[2], 0.45F, 8);
         var0.color(1883953407);
         Cos3Geo.ball(var0, "c3_crystal", var7[0], var7[1], var7[2], 1.0F, 12);
         var0.push();
         var0.translate(var7[0], var7[1], var7[2]);
         var0.rotX(60 + var17 * 20);
         var0.rotY(var2 * 200.0F);
         var0.color(-7673601);
         var0.torus("white", 1.35F, 0.07F, 16, 3, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
         Cos3Geo.halo(var0, var7[0], var7[1], var7[2], 2.8F, 1883953407);
         var0.glow(false);
         int var8 = (var17 + 1) % 3;
         int var9 = (int)Math.floor(var2 * 8.0F);
         if (!(Capes.hash(var9, var17, 9) < 0.35)) {
            float[] var10 = var4[var8];
            float[] var11 = var7;

            for (int var12 = 1; var12 <= 3; var12++) {
               double var13 = var5[var17] + Math.toRadians(120.0) * var12 / 3.0;
               float var15 = var7[1] + (var10[1] - var7[1]) * var12 / 3.0F;
               float[] var16 = var12 == 3 ? var10 : new float[]{(float)Math.cos(var13) * var3, var15, (float)(-Math.sin(var13)) * var3};
               Cos3Geo.arc(var0, var11, var16, 1.1F, 0.07F, var17 * 7 + var12, var2, -1068836609);
               var11 = var16;
            }
         }
      }

      int var18 = (int)Math.floor(var2 * 6.0F);

      for (int var19 = 0; var19 < 3; var19++) {
         if (!(Capes.hash(var18, var19, 21) < 0.4)) {
            double var20 = Capes.hash(var18, var19, 22) * Math.PI * 2.0;
            Cos3Geo.arc(
               var0,
               new float[]{(float)Math.cos(var20) * 5.6F, -23.6F, (float)Math.sin(var20) * 5.6F},
               new float[]{(float)Math.cos(var20 + 0.7) * 5.6F, -23.6F, (float)Math.sin(var20 + 0.7) * 5.6F},
               0.8F,
               0.05F,
               var18 + var19,
               var2,
               -1337272065
            );
         }
      }

      var0.color(-1);
   }

   static void planets(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(0.0F, -6.0F, 0.0F);
      var0.rotX(10.0F);
      var0.rotZ(-6.0F);
      float[] var3 = new float[]{9.5F, 12.0F, 14.5F};
      float[] var4 = new float[]{40.0F, 28.0F, 19.0F};
      float[] var5 = new float[]{0.8F, 1.15F, 1.5F};
      String[] var6 = new String[]{"c3_planet_ice", "c3_planet_lava", "c3_planet_gas"};

      for (int var7 = 0; var7 < 3; var7++) {
         var0.glow(true).color(1625878783);
         var0.torus("white", var3[var7], 0.05F, 56, 3, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.glow(false);
         var0.push();
         var0.rotY(var2 * var4[var7] + var7 * 130);
         var0.translate(var3[var7], 0.0F, 0.0F);
         var0.push();
         var0.rotY(-var2 * 70.0F);
         var0.color(-1);
         var0.sphere(var6[var7], var5[var7], 16, 8, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
         if (var7 == 1) {
            Cos3Geo.halo(var0, 0.0F, 0.0F, 0.0F, var5[var7] * 4.0F, 1627351594);
         }

         if (var7 == 2) {
            var0.push();
            var0.rotZ(22.0F);
            var0.color(-3880);
            var0.ring("c3_ring", var5[var7] * 1.35F, var5[var7] * 2.1F, 0.0F, 24, 0.0F, 0.0F, 1.0F, 1.0F);
            var0.pop();
         }

         if (var7 == 0) {
            var0.push();
            var0.rotY(var2 * 150.0F);
            var0.translate(1.5F, 0.2F, 0.0F);
            var0.color(-2565920);
            var0.sphere("c3_metal", 0.3F, 8, 4, 0.0F, 0.0F, 1.0F, 1.0F);
            var0.pop();
         }

         var0.pop();
      }

      for (int var11 = 0; var11 < 10; var11++) {
         double var8 = Math.toRadians(var11 * 36 + var2 * 8.0F);
         float var10 = var3[var11 % 3] + (var11 % 2 == 0 ? 0.5F : -0.5F);
         Cos3Models.sparkle(
            var0,
            (float)Math.cos(var8) * var10,
            Cos3Geo.sin(var11 * 2.7F) * 0.8F,
            (float)Math.sin(var8) * var10,
            0.25F,
            Cos3Geo.alpha(0.5 + 0.5 * Cos3Geo.sin(var2 * 3.0F + var11), 16777215)
         );
      }

      var0.pop();
      var0.color(-1);
   }

   static void mechWings(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = mv(var1);
      float var4 = Cos3Geo.sin(var2 * 1.6F) * 7.0F * (1.0F - var3) + Cos3Geo.sin(var2 * 6.0F) * 16.0F * var3;
      float var5 = Math.max(0.0F, (var1.sneak ? 10 : 35) + var4);
      float var6 = var1.sneak ? 55.0F : 26.0F - var3 * 8.0F;
      float var7 = 0.5F + 0.5F * Cos3Geo.sin(var2 * 0.8F);

      for (byte var8 = -1; var8 <= 1; var8 += 2) {
         var0.push();
         var0.translate(var8 * 1.2F, -2.2F, -3.6F);
         var0.scale(var8 * 1.3F, 1.3F, 1.3F);
         var0.rotY(var6);
         var0.rotZ(var5);
         var0.color(-13749700);
         Cos3Geo.ball(var0, "c3_metal", 0.0F, 0.0F, 0.0F, 0.75F, 10);
         var0.glow(true).color(-11867905);
         var0.push();
         var0.rotX(90.0F);
         var0.torus("white", 0.78F, 0.1F, 14, 3, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
         var0.glow(false);
         var0.color(-12960182);
         Cos3Geo.bbox(var0, "c3_metal", 0.3F, -0.45F, -0.35F, 4.6F, 0.45F, 0.35F, 0.2F);
         blades(var0, var2, 0, new float[]{1.3F, 2.6F, 3.9F}, new float[]{6.4F, 7.2F, 8.0F}, -12.0F, 6.0F, var7);
         var0.translate(4.5F, 0.0F, 0.0F);
         var0.color(-13749700);
         Cos3Geo.ball(var0, "c3_metal", 0.0F, 0.0F, 0.0F, 0.55F, 8);
         var0.glow(true).color(-11867905);
         Cos3Geo.ball(var0, "white", 0.0F, 0.0F, 0.5F, 0.2F, 5);
         var0.glow(false);
         var0.rotZ(-30.0F + var4 * 0.4F);
         var0.color(-12960182);
         Cos3Geo.bbox(var0, "c3_metal", 0.2F, -0.38F, -0.3F, 5.2F, 0.38F, 0.3F, 0.18F);
         blades(var0, var2, 3, new float[]{1.0F, 2.3F, 3.6F, 4.9F}, new float[]{8.6F, 8.8F, 8.2F, 7.0F}, 8.0F, 11.0F, var7);
         var0.push();
         var0.translate(5.2F, 0.0F, 0.0F);
         var0.rotZ(-90.0F);
         var0.glow(true).color(-11867905);
         Cos3Geo.shard(var0, "c3_crystal", 0.3F, 1.3F);
         var0.glow(false);
         var0.pop();
         var0.pop();
      }

      var0.color(-1);
   }

   private static void blades(G var0, float var1, int var2, float[] var3, float[] var4, float var5, float var6, float var7) {
      for (int var8 = 0; var8 < var3.length; var8++) {
         float var9 = var4[var8];
         var0.push();
         var0.translate(var3[var8], -0.2F, 0.0F);
         var0.rotZ(var5 + var8 * var6 * (0.8F + 0.4F * var7));
         float[][] var10 = new float[][]{{-0.55F, 0.0F}, {0.55F, 0.0F}, {0.5F, -var9 * 0.78F}, {0.05F, -var9}, {-0.35F, -var9 * 0.84F}};
         var0.color(-1775376);
         Cos3Geo.extrude(var0, "c3_metal", var10, -0.16F, 0.16F);
         float[][] var11 = new float[][]{{0.28F, -0.4F}, {0.62F, -0.4F}, {0.57F, -var9 * 0.78F}, {0.08F, -var9 * 1.02F}, {0.3F, -var9 * 0.78F}};
         float var12 = 0.7F + 0.3F * Cos3Geo.sin(var1 * 3.0F - (var2 + var8) * 0.6F);
         var0.glow(true).color(Cos3Geo.alpha(var12, 4909311));
         Cos3Geo.extrude(var0, "white", var11, -0.22F, 0.22F);
         var0.glow(false);
         var0.pop();
      }
   }

   static void prismWings(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = mv(var1);
      float var4 = Cos3Geo.sin(var2 * 1.5F) * 6.0F * (1.0F - var3) + Cos3Geo.sin(var2 * 6.0F) * 14.0F * var3;

      for (byte var5 = -1; var5 <= 1; var5 += 2) {
         var0.push();
         var0.translate(var5 * 1.0F, -2.4F, -3.4F);
         var0.scale(var5, 1.0F, 1.0F);
         var0.rotY(var1.sneak ? 50.0F : 24.0F);
         var0.rotZ(var4);
         byte var6 = 9;

         for (int var7 = 0; var7 < var6; var7++) {
            float var8 = 78 - var7 * 17;
            float var9 = 4.0F + 6.5F * Cos3Geo.sin(Math.PI * (var7 + 0.6) / (var6 + 0.2));
            float var10 = 0.9F + 0.3F * Cos3Geo.sin(var2 * 2.0F + var7 * 0.8F);
            double var11 = Math.toRadians(var8);
            var0.push();
            var0.rotX(-42.0F * Math.max(0.0F, (float)Math.sin(var11)));
            var0.translate((float)Math.cos(var11) * var10, (float)Math.sin(var11) * var10, 0.0F);
            var0.rotZ(var8 - 90.0F);
            var0.scale(1.0F, 1.0F, 0.55F);
            int var13 = Capes.hsv(0.52 + var7 * 0.045 + 0.03 * Math.sin(var2), 0.5, 1.0);
            var0.glow(true).color(Cos3Geo.argb(200, var13));
            Cos3Geo.shard(var0, "c3_crystal", 0.55F + 0.15F * Cos3Geo.sin(var7 * 1.3F), var9);
            var0.pop();
            if (var7 % 2 == 0) {
               float var14 = (float)Math.cos(var11) * (var10 + var9 + 0.8F);
               float var15 = (float)Math.sin(var11) * (var10 + var9 + 0.8F);
               var0.push();
               var0.rotX(-42.0F * Math.max(0.0F, (float)Math.sin(var11)));
               var0.translate(var14, var15 + Cos3Geo.sin(var2 * 2.4F + var7) * 0.35F, 0.0F);
               var0.rotY(var2 * 90.0F + var7 * 40);
               var0.color(Cos3Geo.argb(230, var13));
               Cos3Geo.gem(var0, "c3_crystal", 0.35F, 0.5F, 0.5F, 4);
               var0.pop();
            }
         }

         var0.glow(false);
         Cos3Geo.halo(var0, 0.0F, 0.0F, 0.0F, 1.8F, 1891166463);
         var0.pop();
      }

      var0.color(-1);
   }
}
