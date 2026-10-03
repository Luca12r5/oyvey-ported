package dev.lego.cosmetic;

final class HdAuras {
   private HdAuras() {
   }

   private static float[] p(float var0, float var1, float var2) {
      return new float[]{var0, var1, var2};
   }

   static void groundGlow(G var0, int var1, float var2) {
      boolean var3 = var0.glow;
      int var4 = var0.color;
      var0.glow(true).color(var1);
      var0.push();
      var0.translate(0.0F, -23.9F, 0.0F);
      var0.rotX(-90.0F);
      var0.plane("hd_softglow", 0.0F, 0.0F, var2 * 2.0F, var2 * 2.0F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.glow(var3).color(var4);
   }

   static float life(float var0) {
      return Cos3Geo.smooth(var0 * 5.0F) * Cos3Geo.smooth((1.0F - var0) * 2.2F);
   }

   static void fire(G var0, Cos.A var1) {
      float var2 = var1.time;
      groundGlow(var0, -1426101734, 11.0F);
      byte var3 = 12;

      for (int var4 = 0; var4 < var3; var4++) {
         float var5 = Cos3Geo.fract(var2 * 1.1F + var4 * 0.37F);
         double var6 = (Math.PI * 2) * var4 / var3 + var2 * 0.4F;
         float var8 = 7.4F + Cos3Geo.sin(var4 * 1.7) * 1.0F - var5 * 1.2F;
         float var9 = life(var5);
         if (!(var9 < 0.03F)) {
            var0.push();
            var0.translate(Cos3Geo.cos(var6) * var8, -24.0F + var5 * 10.0F, Cos3Geo.sin(var6) * var8);
            var0.rotZ(Cos3Geo.sin(var2 * 5.0F + var4) * 10.0F);
            var0.rotX(Cos3Geo.cos(var2 * 4.0F + var4) * 8.0F);
            Cos3Models.flame(var0, 5.2F * var9, 1.25F * var9, -38374, -5990);
            var0.pop();
         }
      }

      for (int var12 = 0; var12 < 14; var12++) {
         float var13 = Cos3Geo.fract(var2 * 0.6F + var12 * 0.29F);
         double var14 = var12 * 2.1 + var2 * 0.8F;
         float var15 = Cos3Geo.cos(var14) * (6.0F + var13 * 3.0F);
         float var16 = -22.0F + var13 * 26.0F;
         float var10 = Cos3Geo.sin(var14) * (6.0F + var13 * 3.0F);
         float var11 = 1.0F - var13;
         Cos3Models.sparkle(var0, var15, var16, var10, 0.35F * var11 + 0.1F, Cos3Geo.alpha(var11, 16756810));
         if (var12 % 2 == 0) {
            Cos3Geo.halo(var0, var15, var16, var10, 1.8F * var11, Cos3Geo.alpha(0.5 * var11, 16742954));
         }
      }
   }

   static void frost(G var0, Cos.A var1) {
      float var2 = var1.time;
      groundGlow(var0, -2005075969, 11.0F);

      for (int var3 = 0; var3 < 9; var3++) {
         double var4 = (Math.PI * 2) * var3 / 9.0 + 0.3;
         float var6 = 7.8F + var3 % 3 * 0.9F;
         float var7 = 0.75F + 0.25F * Cos3Geo.sin(var2 * 1.3F + var3);
         var0.push();
         var0.translate(Cos3Geo.cos(var4) * var6, -24.2F, Cos3Geo.sin(var4) * var6);
         var0.rotY((float)Math.toDegrees(-var4));
         var0.rotZ(-14 + var3 % 2 * 28);
         var0.glow(true).color(-576131841);
         Cos3Geo.shard(var0, "c3_crystal", 0.6F + var3 % 3 * 0.15F, (2.6F + var3 % 4 * 0.8F) * var7);
         var0.rotZ(30.0F);
         Cos3Geo.shard(var0, "c3_crystal", 0.35F, 1.6F * var7);
         var0.glow(false);
         var0.pop();
      }

      for (int var9 = 0; var9 < 10; var9++) {
         double var11 = (Math.PI * 2) * var9 / 10.0 + var2 * 0.7F;
         float var13 = Cos3Geo.fract(var2 * 0.18F + var9 * 0.41F);
         float var15 = life(var13);
         if (!(var15 < 0.03F)) {
            float var8 = 8.5F + Cos3Geo.sin(var9 * 2.3) * 0.8F;
            var0.push();
            var0.translate(Cos3Geo.cos(var11) * var8, -23.0F + var13 * 26.0F, Cos3Geo.sin(var11) * var8);
            var0.rotY((float)Math.toDegrees(-var11) + 90.0F + var2 * 40.0F);
            var0.rotZ(var2 * 60.0F + var9 * 30);
            var0.glow(true).color(-1509121);
            XmasGeo.flake(var0, "white", (1.1F + var9 % 3 * 0.35F) * var15, 0.08F);
            var0.glow(false);
            var0.pop();
         }
      }

      for (int var10 = 0; var10 < 10; var10++) {
         double var12 = var10 * 2.4 - var2 * 0.4;
         float var14 = 0.5F + 0.5F * Cos3Geo.sin(var2 * 4.0F + var10 * 1.3F);
         Cos3Models.sparkle(
            var0,
            Cos3Geo.cos(var12) * (6 + var10 % 4),
            -22.0F + var10 * 2.3F % 24.0F,
            Cos3Geo.sin(var12) * (6 + var10 % 4),
            0.3F + 0.3F * var14,
            Cos3Geo.alpha(var14, 16777215)
         );
      }
   }

   static void hearts(G var0, Cos.A var1) {
      float var2 = var1.time;
      float[][] var3 = HdGeo.heart(1.4F, 20);
      byte var4 = 8;
      int[] var5 = new int[]{-54678, -38246, -1568710};

      for (int var6 = 0; var6 < var4; var6++) {
         float var7 = Cos3Geo.fract(var2 * 0.3F + (float)var6 / var4);
         float var8 = life(var7);
         if (!(var8 < 0.03F)) {
            double var9 = (Math.PI * 2) * var6 / var4 + var2 * 0.6F;
            float var11 = 8.5F + Cos3Geo.sin(var2 * 1.5F + var6) * 0.8F;
            float var12 = 1.0F + 0.14F * (float)Math.pow(Math.max(0.0, Math.sin(var2 * 7.0F + var6)), 4.0);
            var0.push();
            var0.translate(Cos3Geo.cos(var9) * var11, -22.0F + var7 * 26.0F, Cos3Geo.sin(var9) * var11);
            var0.rotY((float)Math.toDegrees(-var9) + 90.0F + Cos3Geo.sin(var2 * 2.0F + var6) * 25.0F);
            var0.rotZ(Cos3Geo.sin(var2 * 3.0F + var6) * 12.0F);
            var0.scale(var8 * var12 * (1.0F + var6 % 3 * 0.2F));
            var0.color(var5[var6 % 3]);
            HdGeo.puff(var0, "hd_gloss", var3, 1.1F, 0.5F, 2);
            var0.pop();
            Cos3Geo.halo(var0, Cos3Geo.cos(var9) * var11, -22.0F + var7 * 26.0F, Cos3Geo.sin(var9) * var11, 4.5F * var8, Cos3Geo.alpha(0.3 * var8, 16730762));
         }
      }

      var0.color(-1);
   }

   static void orbit(G var0, Cos.A var1) {
      float var2 = var1.time;
      int[] var3 = new int[]{-3703297, -8587265, -3920};

      for (int var4 = 0; var4 < 3; var4++) {
         var0.push();
         var0.translate(0.0F, -9.0F, 0.0F);
         var0.rotX(25 + var4 * 55);
         var0.rotY(var2 * (70 + var4 * 25) + var4 * 120);
         var0.push();
         var0.translate(11.0F, 0.0F, 0.0F);
         var0.rotY(var2 * 120.0F);
         var0.rotX(90.0F);
         var0.glow(true).color(var3[var4]);
         XmasGeo.star(var0, "white", 1.3F, 0.45F, 5);
         var0.glow(false);
         var0.pop();
         Cos3Geo.halo(var0, 11.0F, 0.0F, 0.0F, 5.5F, Cos3Geo.alpha(0.55, var3[var4] & 16777215));

         for (int var5 = 1; var5 < 9; var5++) {
            var0.push();
            var0.rotY(-var5 * 6.5F);
            float var6 = 1.0F - var5 / 9.0F;
            Cos3Models.sparkle(var0, 11.0F, 0.0F, 0.0F, 0.55F * var6, Cos3Geo.alpha(var6, var3[var4] & 16777215));
            if (var5 % 2 == 0) {
               Cos3Geo.halo(var0, 11.0F, 0.0F, 0.0F, 3.2F * var6, Cos3Geo.alpha(0.35 * var6, var3[var4] & 16777215));
            }

            var0.pop();
         }

         var0.pop();
      }

      var0.color(-1);
   }

   static void storm(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(0.0F, 5.5F, 0.0F);
      var0.rotY(var2 * 18.0F);

      for (int var3 = 0; var3 < 12; var3++) {
         double var4 = (Math.PI * 2) * var3 / 12.0;
         float var6 = 8.0F + var3 % 2 * 1.2F;
         float var7 = Cos3Geo.sin(var2 * 1.4F + var3) * 0.4F;
         var0.color(-11906456);
         Geo.ellipsoid(var0, "hd_fluff", Cos3Geo.cos(var4) * var6, var7 + var3 % 3 * 0.5F, Cos3Geo.sin(var4) * var6, 2.3F, 1.7F, 2.1F, 10, 6);
         var0.color(-9800560);
         Geo.ellipsoid(
            var0, "hd_fluff", Cos3Geo.cos(var4 + 0.25) * (var6 - 0.6F), var7 + 1.3F, Cos3Geo.sin(var4 + 0.25) * (var6 - 0.6F), 1.6F, 1.3F, 1.6F, 8, 5
         );
      }

      float var12 = (float)Math.pow(Math.max(0.0F, Cos3Geo.sin(var2 * 11.0F) * Cos3Geo.sin(var2 * 3.7F)), 3.0);
      var0.pop();
      Cos3Geo.halo(var0, 0.0F, 5.5F, 0.0F, 18.0F, Cos3Geo.alpha(0.35 * var12, 10152191));

      for (int var13 = 0; var13 < 5; var13++) {
         float var5 = Cos3Geo.fract(var2 * 1.3F + var13 * 0.29F);
         if (!(var5 > 0.36F)) {
            double var16 = (Math.PI * 2) * ((var13 * 0.61F + Math.floor(var2 * 1.3F + var13 * 0.37F) * 0.29F) % 1.0);
            float var8 = 7.5F + var13 % 2 * 1.2F;
            float[] var9 = p(Cos3Geo.cos(var16) * (var8 + 1.0F), 4.5F, Cos3Geo.sin(var16) * (var8 + 1.0F));
            float[] var10 = p(Cos3Geo.cos(var16 + 0.3) * (var8 + 1.5F), -24.0F, Cos3Geo.sin(var16 + 0.3) * (var8 + 1.5F));
            float var11 = 1.0F - var5 / 0.36F;
            Cos3Geo.arc(var0, var9, var10, 3.4F, 0.22F * (0.5F + var11), var13 * 13 + 5, var2, Cos3Geo.alpha(0.5 + 0.5 * var11, 10152191));
            Cos3Geo.halo(var0, var10[0], -23.5F, var10[2], 6.0F * var11, Cos3Geo.alpha(0.6 * var11, 10152191));
         }
      }

      for (int var14 = 0; var14 < 18; var14++) {
         float var15 = Cos3Geo.fract(var2 * 1.8F + var14 * 0.137F);
         double var17 = var14 * 2.39;
         float var18 = 5.0F + var14 % 4 * 1.3F;
         var0.glow(true).color(-2003117825);
         var0.push();
         var0.translate(Cos3Geo.cos(var17) * (var18 + 2.0F), 4.0F - var15 * 28.0F, Cos3Geo.sin(var17) * (var18 + 2.0F));
         Geo.ellipsoid(var0, "white", 0.0F, 0.0F, 0.0F, 0.07F, 0.6F, 0.07F, 4, 2);
         var0.pop();
         var0.glow(false);
      }

      var0.color(-1);
   }

   static void petal(G var0, int var1, float var2) {
      float[][] var3 = new float[][]{
         {0.0F, -1.0F * var2},
         {0.55F * var2, -0.55F * var2},
         {0.62F * var2, 0.2F * var2},
         {0.3F * var2, 0.9F * var2},
         {0.08F * var2, 0.75F * var2},
         {0.0F, 1.0F * var2},
         {-0.08F * var2, 0.75F * var2},
         {-0.3F * var2, 0.9F * var2},
         {-0.62F * var2, 0.2F * var2},
         {-0.55F * var2, -0.55F * var2}
      };
      int var4 = var0.color;
      var0.color(var1);

      for (byte var5 = -1; var5 <= 1; var5 += 2) {
         var0.push();
         var0.rotY(var5 * 12);
         float[][] var6 = new float[6][];
         int var7 = 0;
         var6[var7++] = new float[]{0.0F, -1.0F * var2};

         for (int var8 = 1; var8 <= 4; var8++) {
            var6[var7++] = new float[]{var5 * var3[var8][0], var3[var8][1]};
         }

         var6[var7++] = new float[]{0.0F, 1.0F * var2};
         HdGeo.puff(var0, "hd_petal", var6, 0.08F, 0.03F, 1, new float[]{0.0F, 0.0F, 1.0F, 1.0F}, new float[]{0.4F, 0.4F, 0.6F, 0.6F});
         var0.pop();
      }

      var0.color(var4);
   }

   static void sakura(G var0, Cos.A var1) {
      float var2 = var1.time;
      byte var3 = 16;
      int[] var4 = new int[]{-18224, -11036, -25922};

      for (int var5 = 0; var5 < var3; var5++) {
         float var6 = Cos3Geo.fract(var2 * 0.22F + var5 * 0.137F);
         float var7 = life(var6);
         if (!(var7 < 0.03F)) {
            double var8 = var5 * 2.39 + var2 * 0.9 + var6 * 3.0F;
            float var10 = 6.0F + var5 % 4 * 1.4F;
            var0.push();
            var0.translate(Cos3Geo.cos(var8) * var10, 6.0F - var6 * 30.0F, Cos3Geo.sin(var8) * var10);
            var0.rotY(var2 * 90.0F + var5 * 40);
            var0.rotX(var2 * 70.0F + var5 * 25);
            petal(var0, var4[var5 % 3], 1.7F * var7);
            var0.pop();
         }
      }

      for (int var11 = 0; var11 < 3; var11++) {
         float var12 = Cos3Geo.fract(var2 * 0.12F + var11 / 3.0F);
         float var13 = life(var12);
         if (!(var13 < 0.05F)) {
            double var14 = var11 * 2.1 + var2 * 0.5;
            var0.push();
            var0.translate(Cos3Geo.cos(var14) * 8.5F, 2.0F - var12 * 26.0F, Cos3Geo.sin(var14) * 8.5F);
            var0.rotX(40.0F + var2 * 30.0F);
            var0.rotZ(var2 * 50.0F + var11 * 90);
            var0.scale(var13);
            HdHats.flower(var0, 5, 1.3F, -16168, -8086, 30.0F);
            var0.pop();
         }
      }

      var0.color(-1);
   }

   static void note(G var0, boolean var1) {
      Geo.ellipsoid(var0, "hd_gloss", 0.0F, 0.0F, 0.0F, 0.75F, 0.52F, 0.42F, 10, 6);
      Geo.tube(var0, "hd_plastic", p(0.62F, 0.1F, 0.0F), p(0.62F, 3.0F, 0.0F), 0.12F, 0.12F, 5);
      if (var1) {
         Geo.ellipsoid(var0, "hd_gloss", 2.0F, 0.4F, 0.0F, 0.75F, 0.52F, 0.42F, 10, 6);
         Geo.tube(var0, "hd_plastic", p(2.62F, 0.5F, 0.0F), p(2.62F, 3.4F, 0.0F), 0.12F, 0.12F, 5);
         Cos3Geo.bbox(var0, "hd_plastic", 0.5F, 2.6F, -0.14F, 2.74F, 3.2F, 0.14F, 0.06F);
      } else {
         float[][] var2 = new float[][]{{0.62F, 3.0F}, {0.62F, 2.2F}, {1.5F, 1.4F}, {1.7F, 1.8F}};
         HdGeo.puff(var0, "hd_plastic", var2, 0.2F, 0.08F, 1);
      }
   }

   static void music(G var0, Cos.A var1) {
      float var2 = var1.time;
      int[] var3 = new int[]{-9772801, -38192, -7606, -7667862};

      for (int var4 = 0; var4 < 9; var4++) {
         float var5 = Cos3Geo.fract(var2 * 0.35F + var4 / 9.0F);
         float var6 = life(var5);
         if (!(var6 < 0.03F)) {
            double var7 = (Math.PI * 2) * var4 / 9.0 + var2 * 0.5;
            float var9 = 8.0F + Cos3Geo.sin(var5 * 6.0F + var4) * 1.0F;
            float var10 = (float)Math.pow(Math.abs(Cos3Geo.sin(var2 * 4.0F + var4)), 3.0) * 0.12F;
            var0.push();
            var0.translate(Cos3Geo.cos(var7) * var9, -18.0F + var5 * 24.0F, Cos3Geo.sin(var7) * var9);
            var0.rotY((float)Math.toDegrees(-var7) + 90.0F);
            var0.rotZ(Cos3Geo.sin(var2 * 5.0F + var4) * 15.0F);
            var0.scale(var6 * (1.0F + var10));
            var0.glow(true).color(var3[var4 % 4]);
            note(var0, var4 % 3 == 0);
            var0.glow(false);
            var0.pop();
            Cos3Geo.halo(
               var0,
               Cos3Geo.cos(var7) * var9,
               -17.0F + var5 * 24.0F,
               Cos3Geo.sin(var7) * var9,
               4.0F * var6,
               Cos3Geo.alpha(0.3 * var6, var3[var4 % 4] & 16777215)
            );
         }
      }

      var0.color(-1);
   }

   static void bubbles(G var0, Cos.A var1) {
      float var2 = var1.time;

      for (int var3 = 0; var3 < 12; var3++) {
         float var4 = Cos3Geo.fract(var2 * 0.2F + var3 * 0.173F);
         float var5 = Cos3Geo.smooth(var4 * 6.0F);
         boolean var6 = var4 > 0.94F;
         double var7 = var3 * 2.1 + var4 * 2.0F;
         float var9 = 6 + var3 % 3 * 2;
         float var10 = (1.2F + var3 % 4 * 0.45F) * var5;
         float var11 = 1.0F + 0.06F * Cos3Geo.sin(var2 * 6.0F + var3);
         float var12 = Cos3Geo.cos(var7) * var9 + Cos3Geo.sin(var2 * 2.0F + var3) * 0.8F;
         float var13 = -23.0F + var4 * 30.0F;
         float var14 = Cos3Geo.sin(var7) * var9;
         if (var6) {
            float var15 = (var4 - 0.94F) / 0.06F;

            for (int var16 = 0; var16 < 6; var16++) {
               double var17 = (Math.PI * 2) * var16 / 6.0;
               Cos3Models.sparkle(
                  var0,
                  var12 + Cos3Geo.cos(var17) * var10 * (1.0F + var15),
                  var13 + Cos3Geo.sin(var17) * var10 * (1.0F + var15),
                  var14,
                  0.25F * (1.0F - var15),
                  Cos3Geo.alpha(1.0F - var15, 14546175)
               );
            }
         } else {
            var0.push();
            var0.translate(var12, var13, var14);
            var0.scale(var11, 1.0F / var11, var11);
            var0.color(-1);
            var0.sphere("hd_bubble", var10, 14, 8, 0.0F, 0.0F, 1.0F, 1.0F);
            var0.pop();
         }
      }

      var0.color(-1);
   }

   static void rainbowRing(G var0, Cos.A var1) {
      float var2 = var1.time;

      for (int var3 = 0; var3 < 2; var3++) {
         var0.push();
         var0.translate(0.0F, var3 == 0 ? -12.0F + Cos3Geo.sin(var2 * 1.5F) * 2.0F : -20.0F + Cos3Geo.sin(var2 * 1.5F + 2.0F) * 1.5F, 0.0F);
         var0.rotX(var3 == 0 ? 8.0F * Cos3Geo.sin(var2) : -6.0F);
         float var4 = var3 == 0 ? 10.0F : 8.5F;
         byte var5 = 40;

         for (int var6 = 0; var6 < var5; var6++) {
            double var7 = (Math.PI * 2) * var6 / var5;
            double var9 = (Math.PI * 2) * (var6 + 1) / var5;
            var0.glow(true).color(Geo.hue((float)var6 / var5 + var2 * 0.2F * (var3 == 0 ? 1 : -1), 0.62, 1.0));
            Geo.tube(
               var0,
               "white",
               p(Cos3Geo.cos(var7) * var4, 0.0F, Cos3Geo.sin(var7) * var4),
               p(Cos3Geo.cos(var9) * var4, 0.0F, Cos3Geo.sin(var9) * var4),
               0.32F,
               0.32F,
               6
            );
         }

         var0.color(Cos3Geo.alpha(0.3, 16777215));
         var0.torus("hd_softglow", var4, 1.1F, 32, 4, 0.1F, 0.1F, 0.9F, 0.9F);
         var0.glow(false);
         var0.pop();
      }

      for (int var11 = 0; var11 < 8; var11++) {
         double var12 = (Math.PI * 2) * var11 / 8.0 + var2 * 1.2;
         float var13 = 0.5F + 0.5F * Cos3Geo.sin(var2 * 4.0F + var11);
         Cos3Models.sparkle(
            var0,
            Cos3Geo.cos(var12) * 10.0F,
            -12.0F + Cos3Geo.sin(var2 * 1.5F) * 2.0F + 0.6F,
            Cos3Geo.sin(var12) * 10.0F,
            0.3F + 0.4F * var13,
            Geo.hue(var11 / 8.0 + var2 * 0.2, 0.3, 1.0)
         );
      }

      var0.color(-1);
   }

   static void voidAura(G var0, Cos.A var1) {
      float var2 = var1.time;
      groundGlow(var0, -1433785601, 10.0F);
      var0.push();
      var0.translate(0.0F, -23.7F, 0.0F);
      var0.rotY(-var2 * 50.0F);
      var0.glow(true).color(-860861697);

      for (int var3 = 0; var3 < 3; var3++) {
         var0.push();
         var0.rotY(var3 * 120);
         byte var4 = 16;
         float[][] var5 = new float[var4 + 1][];
         float[] var6 = new float[var4 + 1];
         float[] var7 = new float[var4 + 1];

         for (int var8 = 0; var8 <= var4; var8++) {
            float var9 = (float)var8 / var4;
            double var10 = var9 * Math.PI * 1.3;
            float var12 = 9.5F - var9 * 5.5F;
            var5[var8] = p(Cos3Geo.cos(var10) * var12, 0.1F + var9 * 0.2F, Cos3Geo.sin(var10) * var12);
            var6[var8] = 0.9F * (1.0F - var9) + 0.1F;
            var7[var8] = 0.06F;
         }

         XmasGeo.sweep(var0, "c3_beam", var5, var6, var7, null, 4, 1.0F);
         var0.pop();
      }

      var0.glow(false);
      var0.pop();

      for (int var13 = 0; var13 < 12; var13++) {
         float var14 = Cos3Geo.fract(var2 * 0.35F + var13 / 12.0F);
         double var15 = var13 * 2.39 - var14 * 5.0F - var2 * 0.4;
         float var16 = 11.0F * (1.0F - var14) + 2.0F;
         float var17 = Cos3Geo.smooth(var14 * 3.0F) * (1.0F - var14 * 0.6F);
         float var18 = Cos3Geo.cos(var15) * var16;
         float var19 = -22.0F + var14 * 20.0F + Cos3Geo.sin(var13) * 2.0F;
         float var11 = Cos3Geo.sin(var15) * var16;
         var0.color(-16120302);
         Cos3Geo.ball(var0, "hd_gloss", var18, var19, var11, 0.55F * var17 + 0.05F, 8);
         Cos3Geo.halo(var0, var18, var19, var11, 2.8F * var17, Cos3Geo.alpha(0.6 * var17, 11553535));
      }

      var0.color(-1);
   }

   static void sparkle(G var0, Cos.A var1) {
      float var2 = var1.time;
      int[] var3 = new int[]{-1, -5984, -4658945, -18200};

      for (int var4 = 0; var4 < 20; var4++) {
         float var5 = Cos3Geo.fract(var2 * 0.25F + var4 * 0.0913F);
         double var6 = var4 * 2.39 + var2 * 0.3;
         float var8 = 5 + var4 * 7 % 5;
         float var9 = 0.4F + 0.6F * Math.abs(Cos3Geo.sin(var2 * 3.0F + var4 * 1.7F));
         float var10 = life(var5) * var9;
         if (!(var10 < 0.03F)) {
            int var11 = var3[var4 % 4];
            float var12 = Cos3Geo.cos(var6) * var8;
            float var13 = 4.0F - var5 * 28.0F;
            float var14 = Cos3Geo.sin(var6) * var8;
            var0.push();
            var0.translate(var12, var13, var14);
            var0.rotY(var2 * 90.0F + var4 * 30);
            var0.glow(true).color(var11);
            Cos3Geo.gem(var0, "white", 0.32F * var10, 1.4F * var10, 1.4F * var10, 4);
            var0.rotX(90.0F);
            Cos3Geo.gem(var0, "white", 0.32F * var10, 0.95F * var10, 0.95F * var10, 4);
            var0.glow(false);
            var0.pop();
            if (var4 % 2 == 0) {
               Cos3Geo.halo(var0, var12, var13, var14, 2.4F * var10, Cos3Geo.alpha(0.4 * var10, var11 & 16777215));
            }
         }
      }

      var0.color(-1);
   }

   static void leaves(G var0, Cos.A var1) {
      float var2 = var1.time;
      int[] var3 = new int[]{-1531856, -1548246, -3657698, -4667334};

      for (int var4 = 0; var4 < 14; var4++) {
         float var5 = Cos3Geo.fract(var2 * 0.18F + var4 * 0.157F);
         float var6 = life(var5);
         if (!(var6 < 0.03F)) {
            double var7 = var4 * 2.2 + var2 * 0.7 + var5 * 2.5;
            float var9 = 7.0F + var4 % 3 * 1.5F;
            var0.push();
            var0.translate(Cos3Geo.cos(var7) * var9, 4.0F - var5 * 28.0F, Cos3Geo.sin(var7) * var9);
            var0.rotY(var2 * 80.0F + var4 * 50);
            var0.rotZ(Cos3Geo.sin(var2 * 2.0F + var4) * 40.0F);
            var0.rotX(Cos3Geo.sin(var2 * 1.3F + var4) * 30.0F);
            var0.scale(var6 * 1.6F);
            var0.push();
            var0.translate(-1.1F, 0.0F, 0.0F);
            HdHats.leaf(var0, 2.4F, var3[var4 % 4]);
            var0.pop();
            var0.color(-9813984);
            Geo.tube(var0, "hd_wood", p(-1.1F, 0.0F, 0.0F), p(-1.8F, -0.2F, 0.0F), 0.07F, 0.05F, 3);
            var0.pop();
         }
      }

      var0.color(-1);
   }

   static void runes(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = 0.8F + 0.2F * Cos3Geo.sin(var2 * 2.5F);
      var0.push();
      var0.translate(0.0F, -23.8F, 0.0F);
      var0.rotY(var2 * 25.0F);
      var0.glow(true).color(Cos3Geo.alpha(var3, 13148415));
      var0.torus("white", 10.5F, 0.12F, 48, 3, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.torus("white", 8.2F, 0.09F, 40, 3, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(Cos3Geo.alpha(0.95 * var3, 16777215));
      Cos3Geo.lathe(var0, "hd_rune", new float[]{9.4F, 9.4F}, new float[]{0.05F, 1.2F}, 40, 0.0F, 1.0F, 3.0F, 0.0F);
      var0.color(Cos3Geo.alpha(0.25 * var3, 10119935));
      var0.ring("hd_softglow", 7.5F, 11.5F, 0.02F, 40, 0.1F, 0.5F, 0.9F, 0.5F);
      var0.color(Cos3Geo.alpha(0.8 * var3, 13148415));

      for (int var4 = 0; var4 < 6; var4++) {
         double var5 = (Math.PI * 2) * var4 / 6.0;
         double var7 = (Math.PI * 2) * (var4 + 2) / 6.0;
         Geo.tube(
            var0,
            "white",
            p(Cos3Geo.cos(var5) * 8.2F, 0.05F, Cos3Geo.sin(var5) * 8.2F),
            p(Cos3Geo.cos(var7) * 8.2F, 0.05F, Cos3Geo.sin(var7) * 8.2F),
            0.07F,
            0.07F,
            3
         );
      }

      var0.glow(false);
      var0.pop();

      for (int var9 = 0; var9 < 6; var9++) {
         float var10 = Cos3Geo.fract(var2 * 0.3F + var9 / 6.0F);
         float var6 = life(var10);
         if (!(var6 < 0.03F)) {
            double var11 = (Math.PI * 2) * var9 / 6.0 + var2 * 0.4;
            var0.push();
            var0.translate(Cos3Geo.cos(var11) * 9.4F, -22.5F + var10 * 16.0F, Cos3Geo.sin(var11) * 9.4F);
            var0.rotY((float)Math.toDegrees(-var11) + 90.0F + var2 * 40.0F);
            var0.scale(var6 * 1.4F);
            var0.color(-12962744);
            Cos3Geo.bbox(var0, "hd_steel", -0.75F, -1.0F, -0.25F, 0.75F, 1.0F, 0.25F, 0.18F);
            var0.glow(true).color(-2576129);
            var0.push();
            var0.translate(0.0F, 0.0F, 0.27F);
            Cos3Geo.cbox(var0, "white", 0.0F, 0.1F, 0.0F, 0.14F, 1.3F, 0.04F, 0.02F);
            var0.rotZ(40.0F);
            Cos3Geo.cbox(var0, "white", 0.25F, 0.25F, 0.0F, 0.12F, 0.8F, 0.04F, 0.02F);
            var0.pop();
            var0.glow(false);
            var0.pop();
         }
      }

      var0.color(-1);
   }

   static void brick(G var0, int var1, int var2) {
      int var3 = var0.color;
      var0.color(var1);
      float var4 = var2 * 0.8F;
      Cos3Geo.bbox(var0, "hd_brick", -var4, -0.7F, -0.8F, var4, 0.7F, 0.8F, 0.1F);

      for (int var5 = 0; var5 < var2; var5++) {
         for (int var6 = 0; var6 < 2; var6++) {
            var0.push();
            var0.translate(-var4 + 0.8F + var5 * 1.6F, 0.7F, var6 == 0 ? -0.4F : 0.4F);
            Cos3Geo.lathe(var0, "hd_brick", new float[]{0.3F, 0.3F, 0.26F, 0.0F}, new float[]{-0.02F, 0.3F, 0.36F, 0.37F}, 8);
            var0.pop();
         }
      }

      var0.color(var3);
   }

   static void bricks(G var0, Cos.A var1) {
      float var2 = var1.time;
      int[] var3 = new int[]{-1900533, -13053, -16749385, -16743125};

      for (int var4 = 0; var4 < 8; var4++) {
         double var5 = (Math.PI * 2) * var4 / 8.0 + var2 * 0.8;
         float var7 = -10.0F + Cos3Geo.sin(var2 * 1.6F + var4 * 0.8F) * 3.0F;
         var0.push();
         var0.translate(Cos3Geo.cos(var5) * 10.0F, var7, Cos3Geo.sin(var5) * 10.0F);
         var0.rotY(var2 * 60.0F + var4 * 45);
         var0.rotX(Cos3Geo.sin(var2 + var4) * 25.0F);
         var0.scale(1.45F);
         brick(var0, var3[var4 % 4], var4 % 3 == 0 ? 1 : 2);
         var0.pop();
      }

      var0.color(-1);
   }
}
