package dev.lego.cosmetic;

final class HdFace {
   private static final String[] PIXEL = new String[]{
      "XXXXXXXXXXXXXXXXXXX", "XXXXXXXXXXXXXXXXXXX", "..XWWXXXX...XWWXXXX", "..XWXXXXX...XWXXXXX", "...XXXXX.....XXXXX.", "....XXX.......XXX.."
   };

   private HdFace() {
   }

   private static float[] p(float var0, float var1, float var2) {
      return new float[]{var0, var1, var2};
   }

   static void temples(G var0, String var1, int var2, float var3, float var4, float var5) {
      int var6 = var0.color;
      var0.color(var2);

      for (byte var7 = -1; var7 <= 1; var7 += 2) {
         float[][] var8 = new float[][]{
            {var7 * var4, var3, var5},
            {var7 * 4.25F, var3 + 0.05F, var5 - 0.8F},
            {var7 * 4.3F, var3 + 0.1F, 0.0F},
            {var7 * 4.3F, var3 - 0.1F, -2.8F},
            {var7 * 4.22F, var3 - 0.9F, -3.6F}
         };
         float[] var9 = new float[]{0.2F, 0.18F, 0.17F, 0.17F, 0.15F};
         Cos3Geo.limb(var0, var1, var8, var9, 5);
         Cos3Geo.cbox(var0, var1, var7 * (var4 - 0.05F), var3, var5 - 0.1F, 0.55F, 0.6F, 0.5F, 0.12F);
      }

      var0.color(var6);
   }

   static void lensFrame(G var0, float[][] var1, float var2, float var3, float var4, String var5, int var6, float var7, String var8, int var9, boolean var10) {
      var0.push();
      var0.translate(var2, var3, var4);
      int var11 = var0.color;
      var0.color(var6);
      HdGeo.loop(var0, var5, var1, var7, var7 * 1.15F, 2.6F, 6);
      var0.color(var9).glow(var10);
      HdGeo.puff(var0, var8, HdGeo.offset(HdGeo.ccw(var1), -var7 * 0.2F), 0.2F, 0.08F, 1);
      var0.glow(false).color(var11);
      var0.pop();
   }

   static void sunglasses(G var0, Cos.A var1) {
      float[][] var2 = new float[22][];

      for (int var3 = 0; var3 < 22; var3++) {
         double var4 = (Math.PI * 2) * var3 / 22.0;
         float var6 = HdGeo.cos(var4) * 1.85F;
         float var7 = HdGeo.sin(var4) * 1.25F;
         if (var7 < 0.0F) {
            var7 *= 1.25F;
            var6 *= 1.0F - 0.18F * (float)Math.pow(-HdGeo.sin(var4), 2.0) * (HdGeo.cos(var4) > 0.0F ? 0.4F : 1.0F);
         }

         var2[var3] = new float[]{var6, var7};
      }

      for (byte var8 = -1; var8 <= 1; var8 += 2) {
         float[][] var10 = new float[var2.length][];

         for (int var5 = 0; var5 < var2.length; var5++) {
            var10[var5] = new float[]{var2[var5][0] * var8, var2[var5][1]};
         }

         lensFrame(var0, var10, var8 * 2.1F, 3.55F, 4.45F, "hd_gold", -1523094, 0.17F, "hd_lens", -1, false);
      }

      var0.color(-1523094);
      Geo.chain(var0, "hd_gold", new float[][]{{-0.45F, 4.55F, 4.45F}, {0.0F, 4.7F, 4.5F}, {0.45F, 4.55F, 4.45F}}, new float[]{0.13F, 0.13F, 0.13F}, 5);
      Geo.chain(var0, "hd_gold", new float[][]{{-0.35F, 3.95F, 4.5F}, {0.0F, 3.75F, 4.55F}, {0.35F, 3.95F, 4.5F}}, new float[]{0.1F, 0.1F, 0.1F}, 5);
      var0.color(-571411206);

      for (byte var9 = -1; var9 <= 1; var9 += 2) {
         Geo.ellipsoid(var0, "hd_glass", var9 * 0.55F, 2.75F, 4.2F, 0.18F, 0.32F, 0.14F, 5, 3);
      }

      temples(var0, "hd_gold", -1523094, 4.2F, 3.9F, 4.4F);
      var0.color(-1);
   }

   static void pixelGlasses(G var0, Cos.A var1) {
      float var2 = 0.46F;
      int var3 = PIXEL[0].length();
      float var4 = -var3 * var2 / 2.0F;
      float var5 = 4.95F;
      float var6 = var1.preview ? 0.0F : Math.max(0.0F, 1.0F - var1.time * 0.0F);
      var0.push();
      var0.translate(0.0F, var6 * 0.0F, 4.3F);

      for (int var7 = 0; var7 < PIXEL.length; var7++) {
         for (int var8 = 0; var8 < var3; var8++) {
            char var9 = PIXEL[var7].charAt(var8);
            if (var9 != '.') {
               float var10 = -(var4 + var8 * var2 + var2 / 2.0F);
               float var11 = var5 - var7 * var2 - var2 / 2.0F;
               boolean var12 = var9 == 'W';
               var0.color(var12 ? -723724 : -15856110);
               var0.box(
                  var12 ? "hd_plastic" : "hd_rubber",
                  var10 - var2 / 2.0F + 0.01F,
                  var11 - var2 / 2.0F + 0.01F,
                  -0.2F,
                  var10 + var2 / 2.0F - 0.01F,
                  var11 + var2 / 2.0F - 0.01F,
                  var12 ? 0.26F : 0.2F,
                  0.0F,
                  0.0F,
                  1.0F,
                  1.0F
               );
            }
         }
      }

      var0.pop();
      temples(var0, "hd_rubber", -15856110, 4.5F, 4.15F, 4.3F);
      var0.color(-1);
   }

   static void heartGlasses(G var0, Cos.A var1) {
      float var2 = (float)Math.pow(Math.max(0.0, Math.sin(var1.time * 6.0F)), 4.0);
      float[][] var3 = HdGeo.heart(2.0F, 28);

      for (byte var4 = -1; var4 <= 1; var4 += 2) {
         var0.push();
         var0.translate(var4 * 2.15F, 3.55F, 4.5F);
         var0.scale(1.0F + 0.08F * var2, 1.0F + 0.08F * var2, 1.0F);
         var0.rotZ(var4 * -6);
         var0.color(-54678);
         HdGeo.loop(var0, "hd_plastic", var3, 0.3F, 0.32F, 2.4F, 6);
         var0.color(-855676262);
         HdGeo.puff(var0, "hd_glass", HdGeo.offset(HdGeo.ccw(var3), -0.1F), 0.2F, 0.08F, 1);
         var0.pop();
      }

      var0.color(-54678);
      Geo.chain(var0, "hd_plastic", new float[][]{{-0.55F, 4.35F, 4.5F}, {0.0F, 4.55F, 4.55F}, {0.55F, 4.35F, 4.5F}}, new float[]{0.16F, 0.16F, 0.16F}, 5);
      temples(var0, "hd_plastic", -54678, 4.3F, 4.05F, 4.45F);
      float var6 = Cos3Geo.fract(var1.time * 1.0F);
      if (var6 < 0.8F) {
         float var5 = HdGeo.sin(var6 / 0.8F * Math.PI);
         var0.push();
         var0.translate(4.6F + var6 * 1.5F, 5.4F + var6 * 3.0F, 4.6F);
         var0.scale(0.4F * var5);
         var0.glow(true).color(-46454);
         HdGeo.puff(var0, "hd_plastic", HdGeo.heart(1.4F, 16), 0.9F, 0.4F, 1);
         var0.glow(false);
         var0.pop();
      }

      var0.color(-1);
   }

   static void starGlasses(G var0, Cos.A var1) {
      float var2 = var1.time;
      float[][] var3 = HdGeo.star(2.35F, 1.1F, 5);

      for (byte var4 = -1; var4 <= 1; var4 += 2) {
         var0.push();
         var0.translate(var4 * 2.25F, 3.6F, 4.5F);
         var0.rotZ(var4 * 8);
         var0.color(-14278);
         HdGeo.loop(var0, "hd_gold", var3, 0.26F, 0.3F, 2.4F, 6);
         var0.color(-14017942);
         HdGeo.puff(var0, "hd_lens", HdGeo.offset(HdGeo.ccw(var3), -0.1F), 0.2F, 0.08F, 1);
         var0.pop();
      }

      var0.color(-14278);
      Geo.chain(var0, "hd_gold", new float[][]{{-0.55F, 4.2F, 4.5F}, {0.0F, 4.4F, 4.55F}, {0.55F, 4.2F, 4.5F}}, new float[]{0.15F, 0.15F, 0.15F}, 5);
      temples(var0, "hd_gold", -14278, 4.1F, 4.1F, 4.45F);

      for (int var7 = 0; var7 < 3; var7++) {
         float var5 = Math.max(0.0F, HdGeo.sin(var2 * 3.0F + var7 * 2.1F));
         if (!(var5 < 0.05F)) {
            float[] var6 = new float[]{var7 == 0 ? 2.25F : (var7 == 1 ? -2.25F : 4.2F), var7 == 2 ? 3.2F : 5.95F};
            Cos3Models.sparkle(var0, var6[0], var6[1], 4.85F, 0.5F * var5, Cos3Geo.alpha(var5, 16777215));
            Cos3Geo.halo(var0, var6[0], var6[1], 4.85F, 2.0F * var5, Cos3Geo.alpha(0.4 * var5, 16769146));
         }
      }

      var0.color(-1);
   }

   static void monocle(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(-1.95F, 3.7F, 4.4F);
      var0.color(-1);
      HdGeo.loop(var0, "hd_gold", HdGeo.circle(1.5F, 1.5F, 24), 0.2F, 0.24F, 2.0F, 6);
      var0.color(-1523094);
      var0.push();
      var0.translate(0.0F, 0.0F, -0.05F);
      HdGeo.loop(var0, "hd_gold", HdGeo.circle(1.78F, 1.78F, 24), 0.09F, 0.14F, 2.0F, 4);
      var0.pop();
      var0.color(-1191182337);
      HdGeo.puff(var0, "hd_glass", HdGeo.circle(1.46F, 1.46F, 20), 0.16F, 0.06F, 1);
      var0.pop();
      float var3 = Cos3Geo.sin(var2 * 1.8F) * 0.25F;
      float[] var4 = new float[]{-3.35F, 2.95F, 4.4F};
      float[] var5 = new float[]{-4.1F, -1.2F + var3 * 0.2F, 3.6F + var3};
      float[][] var6 = Cos3Geo.sag(var4, var5, 1.4F, 8);
      var0.color(-1);
      Cos3Geo.chainPath(var0, "hd_gold", var6, 0.34F);
      var0.push();
      var0.translate(var5[0], var5[1], var5[2]);
      Cos3Geo.cbox(var0, "hd_gold", 0.0F, -0.3F, 0.0F, 0.3F, 0.8F, 0.5F, 0.1F);
      var0.pop();
      var0.color(-1);
   }

   static void mustache(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = Math.max(0.0F, Cos3Geo.sin(var2 * 1.3F)) * 0.08F;
      var0.push();
      var0.translate(0.0F, 1.55F, 4.25F);
      var0.rotZ(Cos3Geo.sin(var2 * 2.2F) * 1.2F);

      for (byte var4 = -1; var4 <= 1; var4 += 2) {
         byte var5 = 12;
         float[][] var6 = new float[var5 + 1][];
         float[] var7 = new float[var5 + 1];
         float[] var8 = new float[var5 + 1];

         for (int var9 = 0; var9 <= var5; var9++) {
            float var10 = (float)var9 / var5;
            double var11 = var10 > 0.6F ? (var10 - 0.6F) / 0.4F * Math.PI * 1.25 : 0.0;
            float var13 = var4 * (0.1F + var10 * 3.4F * (1.0F + var3));
            float var14 = -0.05F - (float)Math.sin(var10 * Math.PI * 0.9) * 0.55F + var10 * var10 * 0.55F;
            if (var11 > 0.0) {
               float var15 = var4 * 2.14F;
               var13 = var15 + var4 * (float)Math.sin(var11) * 0.75F * (1.0F + var3);
               var14 = -0.05F - (float)Math.sin(1.6964600329384882) * 0.55F + 0.19800001F + (1.0F - (float)Math.cos(var11)) * 0.6F;
            }

            var6[var9] = p(var13, var14, 0.35F * (float)Math.sin(var10 * Math.PI) - var10 * 0.3F);
            float var17 = (float)Math.pow(Math.sin(Math.PI * Math.min(1.0, 0.15 + var10 * 0.95)), 0.7);
            var7[var9] = Math.max(0.06F, 0.75F * var17 * (1.0F - 0.6F * var10));
            var8[var9] = Math.max(0.05F, 0.42F * var17 * (1.0F - 0.5F * var10));
         }

         var0.color(-11916778);
         XmasGeo.sweep(var0, "hd_mustache", var6, var8, var7, null, 10, 2.0F);
      }

      var0.pop();
      var0.color(-1);
   }

   static void clownNose(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = (float)Math.pow(Math.max(0.0, Math.sin(var2 * 1.7)), 12.0);
      float var4 = 1.0F + 0.06F * Cos3Geo.sin(var2 * 3.0F) - 0.12F * var3;
      var0.push();
      var0.translate(0.0F, 2.6F, 4.95F);
      var0.scale(var4 + var3 * 0.2F, 1.0F / var4, var4);
      var0.color(-1041890);
      var0.sphere("hd_gloss", 1.3F, 20, 12, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      if (var3 > 0.1F) {
         Cos3Models.sparkle(var0, -0.55F, 3.4F, 6.1F, 0.5F * var3, Cos3Geo.alpha(var3, 16777215));
      }

      var0.color(-1);
   }

   static void visor(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(0.0F, 3.7F, 0.0F);
      byte var3 = 16;
      float[][] var4 = new float[var3 + 1][];

      for (int var5 = 0; var5 <= var3; var5++) {
         double var6 = Math.PI * (0.1 + 0.8 * var5 / var3);
         float[] var8 = Geo.sq(var6, 4.85F, 5.0F);
         var4[var5] = p(var8[0], 0.0F, var8[1]);
      }

      float[] var12 = new float[var3 + 1];
      float[] var13 = new float[var3 + 1];

      for (int var7 = 0; var7 <= var3; var7++) {
         var12[var7] = 0.32F;
         var13[var7] = 1.55F;
      }

      var0.color(-14012872);
      XmasGeo.sweep(var0, "hd_steel", var4, var12, var13, null, 8, 1.0F);

      for (int var14 = 0; var14 <= var3; var14++) {
         double var16 = Math.PI * (0.13 + 0.74 * var14 / var3);
         float[] var10 = Geo.sq(var16, 5.12F, 5.0F);
         var4[var14] = p(var10[0], 0.0F, var10[1]);
         var12[var14] = 0.08F;
         var13[var14] = 1.12F;
      }

      var0.glow(true).color(-1);
      XmasGeo.sweep(var0, "hd_screen", var4, var12, var13, null, 4, 1.0F);
      float var15 = 0.5F + 0.5F * Cos3Geo.sin(var2 * 2.4F);
      double var17 = Math.PI * (0.2 + 0.6 * var15);
      float[] var18 = Geo.sq(var17, 5.25F, 5.0F);
      var0.color(-50598);
      var0.push();
      var0.translate(var18[0], 0.0F, var18[1]);
      var0.rotY((float)(-Math.toDegrees(var17)) + 90.0F);
      Cos3Geo.bbox(var0, "white", -0.35F, -0.85F, -0.05F, 0.35F, 0.85F, 0.12F, 0.08F);
      var0.pop();
      Cos3Geo.halo(var0, var18[0], 0.0F, var18[1] + 0.2F, 3.6F, Cos3Geo.alpha(0.55, 16726618));

      for (byte var11 = -1; var11 <= 1; var11 += 2) {
         var0.glow(false).color(-12960182);
         var0.push();
         var0.translate(var11 * 4.4F, 0.0F, -0.6F);
         var0.rotZ(90.0F);
         Cos3Geo.lathe(var0, "hd_chrome", new float[]{1.1F, 1.25F, 1.1F, 0.6F, 0.0F}, new float[]{-0.4F, -0.1F, 0.35F, 0.55F, 0.56F}, 14);
         var0.pop();
         var0.glow(true).color(-13965569);
         var0.push();
         var0.translate(var11 * 4.97F, 0.0F, -0.6F);
         var0.rotZ(90.0F);
         var0.torus("white", 0.62F, 0.1F, 14, 3, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
         var0.glow(false);
      }

      var0.glow(false).color(-1);
      var0.pop();
   }

   static void masquerade(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(0.0F, 3.75F, 4.3F);

      for (byte var3 = -1; var3 <= 1; var3 += 2) {
         float[][] var4 = new float[20][];

         for (int var5 = 0; var5 < 20; var5++) {
            double var6 = (Math.PI * 2) * var5 / 20.0;
            float var8 = HdGeo.cos(var6) * 1.45F;
            float var9 = HdGeo.sin(var6) * 0.85F;
            float var10 = var8 * var3 > 0.0F ? var8 * var3 / 1.45F : 0.0F;
            var4[var5] = new float[]{var3 * 2.0F + var8, var9 + var10 * var10 * 0.6F};
         }

         var0.color(-9819464);
         HdGeo.loop(var0, "hd_mask", var4, 0.55F, 0.18F, 3.0F, 6);
         var0.color(-10134);
         var0.push();
         var0.translate(0.0F, 0.0F, 0.14F);
         HdGeo.loop(var0, "hd_gold", HdGeo.offset(HdGeo.ccw(var4), 0.55F), 0.08F, 0.08F, 2.0F, 4);
         var0.pop();
         byte var15 = 10;
         float[][] var17 = new float[var15 + 1][];
         float[] var7 = new float[var15 + 1];
         float[] var20 = new float[var15 + 1];

         for (int var22 = 0; var22 <= var15; var22++) {
            float var24 = (float)var22 / var15;
            double var11 = var24 * Math.PI * 1.4;
            var17[var22] = p(
               var3 * (3.6F + 1.1F * (float)Math.sin(var11)), 0.9F + 1.1F * (1.0F - (float)Math.cos(var11)) * 0.6F - var24 * 0.6F, -0.2F - var24 * 0.6F
            );
            var7[var22] = Math.max(0.05F, 0.45F * (1.0F - var24));
            var20[var22] = 0.12F;
         }

         var0.color(-9819464);
         XmasGeo.sweep(var0, "hd_mask", var17, var7, var20, null, 6, 1.0F);
      }

      var0.color(-9819464);
      Cos3Geo.cbox(var0, "hd_mask", 0.0F, 0.25F, -0.05F, 1.2F, 0.8F, 0.3F, 0.12F);
      var0.glow(true).color(-50550);
      var0.push();
      var0.translate(0.0F, 0.35F, 0.2F);
      var0.rotX(90.0F);
      Cos3Geo.gem(var0, "hd_gem", 0.35F, 0.25F, 0.08F, 6);
      var0.pop();
      var0.glow(false);
      var0.push();
      var0.translate(3.8F, 1.5F, -0.2F);
      var0.rotZ(-22.0F + Cos3Geo.sin(var2 * 2.0F) * 5.0F);
      int[] var13 = new int[]{-4691201, -7718182, -1531649};

      for (int var14 = 0; var14 < 3; var14++) {
         var0.push();
         var0.rotZ(-18 + var14 * 18);
         byte var16 = 10;
         float[][] var18 = new float[var16 + 1][];
         float[] var19 = new float[var16 + 1];
         float[] var21 = new float[var16 + 1];

         for (int var23 = 0; var23 <= var16; var23++) {
            float var25 = (float)var23 / var16;
            var18[var23] = p(0.6F * var25 * var25, var25 * 6.2F, -0.3F * var25);
            var19[var23] = Math.max(0.05F, 0.95F * (float)Math.sin(Math.PI * Math.min(1.0, var25 * 0.95 + 0.05)));
            var21[var23] = 0.07F;
         }

         var0.color(var13[var14]);
         XmasGeo.sweep(var0, "c3_plume", var18, var19, var21, null, 6, 1.0F);
         var0.pop();
      }

      var0.pop();
      var0.pop();
      var0.color(-1);
   }

   static void ninja(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = var1.preview ? 0.0F : var1.move;
      var0.color(-13749694);
      Cos3Geo.band(var0, "hd_ninja", 2.6F, 2.07F, 0.3F, -0.15F, 2.95F, 0.0F, 4.0F);
      var0.push();
      var0.translate(0.0F, 2.2F, 4.65F);
      var0.color(-13749694);
      float[][] var4 = new float[][]{{-1.4F, 0.8F}, {1.4F, 0.8F}, {0.9F, -0.7F}, {-0.9F, -0.7F}};
      HdGeo.puff(var0, "hd_ninja", var4, 0.3F, 0.12F, 1);
      var0.pop();
      var0.color(-4710870);
      Cos3Geo.band(var0, "hd_ninja", 2.6F, 2.07F, 0.3F, 5.2F, 6.7F, 0.0F, 4.0F);
      var0.color(-4143408);
      var0.push();
      var0.translate(0.0F, 5.95F, 4.72F);
      Cos3Geo.bbox(var0, "hd_steel", -1.8F, -0.65F, -0.1F, 1.8F, 0.65F, 0.2F, 0.12F);
      var0.color(-12960184);
      Cos3Geo.cbox(var0, "hd_steel", 0.0F, 0.0F, 0.22F, 1.6F, 0.12F, 0.05F, 0.02F);
      var0.pop();
      var0.color(-4710870);
      Geo.ellipsoid(var0, "hd_ninja", 0.0F, 5.95F, -4.75F, 0.9F, 0.6F, 0.5F, 8, 5);
      float var5 = Cos3Geo.sin(var2 * 4.0F) * 10.0F + var3 * 25.0F;

      for (byte var6 = -1; var6 <= 1; var6 += 2) {
         byte var7 = 9;
         float[][] var8 = new float[var7 + 1][];
         float[] var9 = new float[var7 + 1];
         float[] var10 = new float[var7 + 1];

         for (int var11 = 0; var11 <= var7; var11++) {
            float var12 = (float)var11 / var7;
            double var13 = Math.toRadians(62.0 - var5 * 0.6 * var12 - var6 * 6);
            float var15 = 0.25F * Cos3Geo.sin(var2 * 6.0F + var12 * 6.0F + var6) * var12;
            var8[var11] = p(var6 * (0.35F + var12 * 1.2F) + var15, 5.95F - var12 * 5.2F * (float)Math.sin(var13), -4.9F - var12 * 5.2F * (float)Math.cos(var13));
            var9[var11] = 0.1F;
            var10[var11] = 0.55F - var12 * 0.15F;
         }

         XmasGeo.sweep(var0, "hd_ninja", var8, var9, var10, null, 6, 2.0F);
      }

      var0.color(-1);
   }
}
