package dev.lego.cosmetic;

import java.util.Arrays;

final class HdBack {
   private HdBack() {
   }

   private static float[] p(float var0, float var1, float var2) {
      return new float[]{var0, var1, var2};
   }

   static void straps(G var0, int var1, float var2, float var3, float var4) {
      int var5 = var0.color;
      var0.color(var1);

      for (byte var6 = -1; var6 <= 1; var6 += 2) {
         float[][] var7 = new float[][]{
            {var6 * var2, var3, var4},
            {var6 * var2, -1.2F, -2.35F},
            {var6 * var2, 0.25F, -1.7F},
            {var6 * var2, 0.4F, 0.0F},
            {var6 * var2, 0.25F, 1.7F},
            {var6 * var2, -1.2F, 2.3F},
            {var6 * (var2 + 0.2F), -6.2F, 2.25F}
         };
         float[] var8 = new float[var7.length];
         float[] var9 = new float[var7.length];
         Arrays.fill(var8, 0.12F);
         Arrays.fill(var9, 0.55F);
         XmasGeo.sweep(var0, "hd_leather", var7, var8, var9, null, 6, 3.0F);
         var0.color(-2564376);
         var0.push();
         var0.translate(var6 * (var2 + 0.1F), -3.2F, 2.42F);
         HdGeo.loop(var0, "hd_chrome", HdGeo.rrect(0.62F, 0.5F, 0.15F, 2), 0.08F, 0.08F, 2.0F, 4);
         var0.pop();
         var0.color(var1);
      }

      var0.color(var5);
   }

   static void sash(G var0, int var1, float var2) {
      int var3 = var0.color;
      var0.color(var1);
      var0.push();
      var0.rotZ(var2);
      float[][] var4 = new float[][]{
         {0.0F, -12.5F, 2.32F},
         {0.0F, -1.5F, 2.32F},
         {0.0F, 0.9F, 1.4F},
         {0.0F, 1.3F, 0.0F},
         {0.0F, 0.9F, -1.4F},
         {0.0F, -1.5F, -2.32F},
         {0.0F, -12.5F, -2.32F}
      };
      float[] var5 = new float[var4.length];
      float[] var6 = new float[var4.length];
      Arrays.fill(var5, 0.12F);
      Arrays.fill(var6, 0.62F);
      XmasGeo.sweep(var0, "hd_leather", var4, var6, var5, null, 6, 4.0F);
      var0.pop();
      var0.color(var3);
   }

   static void legoBackpack(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(0.0F, -1.3F + (var1.preview ? 0.0F : var1.move * 0.15F * Math.abs(Cos3Geo.sin(var2 * 8.0F))), -2.15F);
      var0.color(-1900533);
      Cos3Geo.bbox(var0, "hd_brick", -3.3F, -8.4F, -3.6F, 3.3F, -0.2F, 0.0F, 0.3F);

      for (int var3 = 0; var3 < 2; var3++) {
         for (int var4 = 0; var4 < 2; var4++) {
            var0.push();
            var0.translate(-1.6F + var3 * 3.2F, -0.2F, -0.9F - var4 * 1.8F);
            Cos3Geo.lathe(var0, "hd_brick", new float[]{1.0F, 1.0F, 0.92F, 0.0F}, new float[]{-0.02F, 0.72F, 0.84F, 0.86F}, 16);
            var0.pop();
         }
      }

      var0.color(-13053);
      Cos3Geo.bbox(var0, "hd_brick", -2.6F, -7.6F, -4.3F, 2.6F, -3.9F, -3.55F, 0.22F);

      for (int var5 = 0; var5 < 2; var5++) {
         var0.push();
         var0.translate(-1.25F + var5 * 2.5F, -3.9F, -3.95F);
         Cos3Geo.lathe(var0, "hd_brick", new float[]{0.55F, 0.55F, 0.5F, 0.0F}, new float[]{-0.02F, 0.4F, 0.48F, 0.5F}, 12);
         var0.pop();
      }

      var0.color(-15066594);
      Cos3Geo.cbox(var0, "hd_plastic", 0.0F, -4.3F, -4.38F, 1.2F, 0.5F, 0.2F, 0.06F);
      var0.color(-16749385);
      Cos3Geo.bbox(var0, "hd_brick", 3.25F, -8.0F, -3.0F, 4.05F, -4.6F, -0.7F, 0.18F);
      var0.color(-6684664);
      var0.push();
      var0.translate(0.0F, -4.3F, -3.62F);
      HdGeo.loop(var0, "hd_rubber", HdGeo.rrect(3.1F, 3.9F, 0.4F, 3), 0.07F, 0.07F, 2.0F, 4);
      var0.pop();
      var0.pop();
      straps(var0, -14013904, 2.2F, -7.6F, -2.2F);
      var0.color(-1);
   }

   static void backSword(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(0.4F, -5.5F, -2.85F);
      var0.rotZ(-40.0F);
      byte var3 = 10;
      float[][] var4 = new float[var3 + 2][];
      float[] var5 = new float[var3 + 2];
      float[] var6 = new float[var3 + 2];
      float[] var7 = new float[var3 + 2];

      for (int var8 = 0; var8 <= var3; var8++) {
         float var9 = (float)var8 / var3;
         var4[var8] = p(0.0F, 2.2F + var9 * 13.5F, 0.0F);
         var5[var8] = 1.25F - var9 * 0.25F;
         var6[var8] = 0.34F - var9 * 0.08F;
         var7[var8] = 1.05F;
      }

      var4[var3 + 1] = p(0.0F, 18.6F, 0.0F);
      var5[var3 + 1] = 0.02F;
      var6[var3 + 1] = 0.02F;
      var7[var3 + 1] = 1.05F;
      var0.glow(false).color(-4330497);
      XmasGeo.sweep(var0, "c3_crystal", var4, var5, var6, var7, 4, 3.0F);
      float var11 = Cos3Geo.fract(var2 * 0.5F);
      var0.glow(true).color(-8725249);

      for (int var12 = 0; var12 <= var3; var12++) {
         var5[var12] *= 0.28F;
         var6[var12] *= 1.08F;
      }

      XmasGeo.sweep(var0, "white", var4, var5, var6, var7, 4, 1.0F);
      Cos3Geo.halo(var0, 0.0F, 2.2F + var11 * 14.0F, 0.0F, 3.2F, Cos3Geo.alpha(0.55 * Math.sin(var11 * Math.PI), 10155775));
      var0.glow(false);
      var0.color(-10863968);
      float[][] var13 = new float[][]{
         {-3.4F, 0.1F}, {-2.2F, -0.35F}, {0.0F, -0.55F}, {2.2F, -0.35F}, {3.4F, 0.1F}, {3.5F, 0.75F}, {2.2F, 0.5F}, {0.0F, 0.7F}, {-2.2F, 0.5F}, {-3.5F, 0.75F}
      };
      var0.push();
      var0.translate(0.0F, 1.7F, 0.0F);
      HdGeo.puff(var0, "hd_silver", var13, 0.9F, 0.3F, 2);
      var0.glow(true).color(-6621441);
      var0.push();
      var0.translate(0.0F, 0.1F, 0.4F);
      var0.rotX(90.0F);
      Cos3Geo.gem(var0, "hd_gem", 0.45F, 0.35F, 0.1F, 6);
      var0.pop();

      for (byte var10 = -1; var10 <= 1; var10 += 2) {
         Cos3Geo.ball(var0, "hd_gem", var10 * 3.5F, 0.45F, 0.0F, 0.35F, 6);
      }

      var0.glow(false);
      var0.pop();
      var0.color(-12966888);
      Cos3Geo.lathe(
         var0,
         "hd_leather",
         new float[]{0.42F, 0.5F, 0.44F, 0.52F, 0.44F, 0.52F, 0.44F, 0.52F, 0.44F, 0.42F},
         new float[]{-2.9F, -2.5F, -2.1F, -1.7F, -1.3F, -0.9F, -0.5F, -0.1F, 0.3F, 1.2F},
         10
      );
      var0.color(-1);
      Cos3Geo.ball(var0, "hd_gold", 0.0F, -3.45F, 0.0F, 0.75F, 10);
      var0.glow(true).color(-8725249);
      Cos3Geo.ball(var0, "hd_gem", 0.0F, -3.45F, 0.62F, 0.32F, 6);
      var0.glow(false);
      var0.pop();
      sash(var0, -12966888, -40.0F);
      var0.color(-1);
   }

   static float[][] guitarOutline() {
      float[][] var0 = new float[][]{
         {0.0F, -4.6F},
         {1.6F, -4.45F},
         {2.9F, -3.8F},
         {3.55F, -2.6F},
         {3.5F, -1.2F},
         {2.9F, 0.1F},
         {2.7F, 1.0F},
         {3.1F, 2.1F},
         {3.3F, 3.4F},
         {2.6F, 4.2F},
         {1.9F, 3.1F},
         {1.1F, 2.4F},
         {0.0F, 2.3F}
      };
      float[][] var1 = new float[var0.length * 2 - 2][];
      int var2 = 0;

      for (float[] var6 : var0) {
         var1[var2++] = new float[]{var6[0], var6[1]};
      }

      for (int var7 = var0.length - 2; var7 >= 1; var7--) {
         float[] var8 = var0[var7];
         float var9 = var8[1] > 1.5F ? var8[1] - 0.8F * (var8[1] - 1.5F) / 2.7F : var8[1];
         var1[var2++] = new float[]{-var8[0], var9};
      }

      return var1;
   }

   static void guitar(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(0.0F, -7.2F, -3.15F);
      var0.rotZ(-32.0F + Cos3Geo.sin(var2 * 1.5F) * 1.2F);
      var0.rotY(180.0F);
      var0.color(-1);
      HdGeo.puff(var0, "hd_maple", guitarOutline(), 1.25F, 0.45F, 2, new float[]{0.0F, 0.0F, 1.0F, 1.0F}, new float[]{0.05F, 0.9F, 0.95F, 0.95F});
      var0.color(-724760);
      var0.push();
      var0.translate(0.0F, 0.0F, 0.66F);
      float[][] var3 = new float[][]{{-2.2F, -3.2F}, {-0.6F, -3.6F}, {1.6F, 2.0F}, {0.6F, 2.35F}, {-1.2F, 2.3F}, {-2.6F, 0.4F}};
      HdGeo.puff(var0, "hd_plastic", var3, 0.1F, 0.04F, 1);
      var0.pop();

      for (int var4 = 0; var4 < 3; var4++) {
         var0.color(-15461352);
         Cos3Geo.bbox(var0, "hd_plastic", -0.95F, -1.9F + var4 * 1.45F, 0.7F, 0.95F, -1.35F + var4 * 1.45F, 0.95F, 0.1F);
      }

      var0.color(-2564376);
      Cos3Geo.bbox(var0, "hd_chrome", -1.05F, -3.35F, 0.62F, 1.05F, -2.75F, 0.95F, 0.1F);

      for (int var7 = 0; var7 < 3; var7++) {
         var0.push();
         var0.translate(1.9F + var7 * 0.2F, -2.9F + var7 * 0.9F, 0.62F);
         var0.rotX(90.0F);
         var0.color(-1514278);
         Cos3Geo.lathe(var0, "hd_plastic", new float[]{0.42F, 0.42F, 0.34F, 0.0F}, new float[]{0.0F, -0.35F, -0.45F, -0.46F}, 10);
         var0.pop();
      }

      var0.color(-2051984);
      Cos3Geo.bbox(var0, "hd_wood", -0.55F, 2.2F, -0.35F, 0.55F, 12.6F, 0.3F, 0.15F);
      var0.color(-12967920);
      var0.box("hd_wood", -0.6F, 2.8F, 0.3F, 0.6F, 12.4F, 0.5F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-2564376);

      for (int var8 = 0; var8 < 12; var8++) {
         float var5 = 12.2F - 9.2F * (1.0F - (float)Math.pow(0.94, var8 * 1.6)) / (1.0F - (float)Math.pow(0.94, 19.2));
         var0.box("hd_chrome", -0.6F, var5, 0.5F, 0.6F, var5 + 0.08F, 0.56F, 0.0F, 0.0F, 1.0F, 1.0F);
      }

      var0.color(-15461352);
      float[][] var9 = new float[][]{{-0.6F, 12.4F}, {0.6F, 12.4F}, {1.2F, 13.6F}, {1.35F, 15.6F}, {0.2F, 16.1F}, {-0.9F, 15.3F}, {-1.0F, 13.4F}};
      HdGeo.puff(var0, "hd_plastic", var9, 0.5F, 0.18F, 1);
      var0.color(-2564376);

      for (int var10 = 0; var10 < 3; var10++) {
         for (byte var6 = -1; var6 <= 1; var6 += 2) {
            var0.push();
            var0.translate(var6 * 1.2F, 13.5F + var10 * 0.8F, 0.0F);
            var0.rotZ(var6 * -90);
            Cos3Geo.lathe(var0, "hd_chrome", new float[]{0.22F, 0.22F, 0.4F, 0.4F, 0.0F}, new float[]{0.0F, 0.5F, 0.55F, 0.85F, 0.9F}, 6);
            var0.pop();
         }
      }

      var0.color(-986892);

      for (int var11 = 0; var11 < 6; var11++) {
         float var12 = -0.4F + var11 * 0.16F;
         Geo.tube(var0, "hd_chrome", p(var12 * 1.5F, -3.05F, 1.0F), p(var12, 12.4F, 0.62F), 0.03F, 0.03F, 3);
      }

      var0.pop();
      sash(var0, -14018030, -40.0F);
      var0.color(-1);
   }

   static void jetpack(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(0.0F, -1.5F, -2.15F);
      var0.color(-12960184);
      Cos3Geo.bbox(var0, "hd_steel", -3.4F, -8.4F, -1.4F, 3.4F, -0.8F, 0.0F, 0.3F);

      for (byte var3 = -1; var3 <= 1; var3 += 2) {
         var0.push();
         var0.translate(var3 * 2.15F, -1.4F, -2.95F);
         var0.color(-1512206);
         Cos3Geo.lathe(
            var0, "hd_chrome", new float[]{0.0F, 1.1F, 1.62F, 1.85F, 1.85F, 1.7F, 1.3F}, new float[]{1.35F, 1.15F, 0.7F, 0.1F, -6.6F, -7.2F, -7.5F}, 18
         );
         var0.color(-1900533);
         Cos3Geo.lathe(var0, "hd_plastic", new float[]{1.88F, 1.95F, 1.95F, 1.88F}, new float[]{-1.4F, -1.5F, -2.2F, -2.3F}, 18);
         Cos3Geo.lathe(var0, "hd_plastic", new float[]{1.88F, 1.95F, 1.95F, 1.88F}, new float[]{-5.3F, -5.4F, -5.9F, -6.0F}, 18);
         var0.push();
         var0.translate(var3 * 1.3F, -3.6F, -1.25F);
         var0.rotY(var3 * 45 + 180);
         var0.rotX(90.0F);
         var0.color(-14012874);
         Cos3Geo.lathe(var0, "hd_steel", new float[]{0.55F, 0.55F, 0.45F, 0.0F}, new float[]{0.0F, 0.3F, 0.34F, 0.34F}, 12);
         var0.pop();
         var0.color(-12960184);
         Cos3Geo.lathe(var0, "hd_steel", new float[]{1.3F, 1.0F, 0.95F, 1.35F, 1.1F}, new float[]{-7.4F, -7.9F, -8.4F, -9.6F, -9.7F}, 14);
         float var4 = 1.0F + 0.18F * Cos3Geo.sin(var2 * 22.0F + var3) + (var1.preview ? 0.0F : var1.move * 0.6F);
         var0.push();
         var0.translate(0.0F, -9.6F, 0.0F);
         var0.rotZ(180.0F);
         Cos3Models.flame(var0, 4.8F * var4, 1.0F, -30166, -2880);
         var0.pop();
         Cos3Geo.halo(var0, 0.0F, -10.4F, 0.0F, 4.2F, Cos3Geo.alpha(0.5, 16751162));

         for (int var5 = 0; var5 < 3; var5++) {
            float var6 = Cos3Geo.fract(var2 * 1.6F + var5 / 3.0F + (var3 > 0 ? 0.5F : 0.0F));
            Cos3Geo.halo(
               var0,
               Cos3Geo.sin(var5 * 2.1 + var2) * var6 * 1.4F,
               -12.0F - var6 * 7.0F * var4,
               Cos3Geo.cos(var5 * 2.1) * var6,
               1.5F + var6 * 3.5F,
               Cos3Geo.alpha(0.3 * (1.0F - var6), 13154559)
            );
         }

         var0.pop();
      }

      var0.pop();
      straps(var0, -14012874, 2.2F, -7.6F, -2.2F);
      var0.color(-1);
   }

   static float[][] heater(float var0, float var1, int var2) {
      float var3 = var1 * 0.42F;
      float var4 = var1;
      float[][] var5 = new float[var2 * 2 + 1][];
      int var6 = 0;

      for (int var7 = 0; var7 <= var2; var7++) {
         float var8 = (float)var7 / var2;
         var5[var6++] = new float[]{var0 * (float)Math.pow(Math.cos(var8 * Math.PI / 2.0), 0.55), var3 - var4 * var8};
      }

      for (int var9 = var2 - 1; var9 >= 0; var9--) {
         float var10 = (float)var9 / var2;
         var5[var6++] = new float[]{-var0 * (float)Math.pow(Math.cos(var10 * Math.PI / 2.0), 0.55), var3 - var4 * var10};
      }

      return HdGeo.ccw(Arrays.copyOf(var5, var6));
   }

   static void shield(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, -5.4F, -3.0F);
      var0.rotZ(8.0F);
      var0.rotY(180.0F);
      float[][] var2 = heater(5.2F, 6.2F, 10);
      var0.color(-1);
      HdGeo.puff(var0, "hd_target", var2, 0.9F, 0.3F, 2, new float[]{0.0F, 0.0F, 1.0F, 1.0F}, new float[]{0.3F, 0.3F, 0.32F, 0.32F});
      var0.color(-2564376);
      HdGeo.loop(var0, "hd_steel", var2, 0.42F, 0.62F, 2.6F, 6);
      var0.color(-10134);
      var0.push();
      var0.translate(0.0F, 0.0F, 0.5F);
      Cos3Geo.bbox(var0, "hd_gold", -0.55F, -5.6F, -0.1F, 0.55F, 2.55F, 0.22F, 0.12F);
      Cos3Geo.bbox(var0, "hd_gold", -4.9F, -0.2F, -0.1F, 4.9F, 0.9F, 0.22F, 0.12F);
      var0.translate(0.0F, 0.35F, 0.15F);
      var0.rotX(90.0F);
      Cos3Geo.lathe(var0, "hd_gold", new float[]{1.35F, 1.3F, 1.05F, 0.5F, 0.0F}, new float[]{0.0F, -0.3F, -0.7F, -0.95F, -1.0F}, 16);
      var0.pop();
      var0.color(-1512206);

      for (byte var3 = 0; var3 < var2.length; var3 += 2) {
         Cos3Geo.ball(var0, "hd_chrome", var2[var3][0] * 0.86F, var2[var3][1] * 0.86F + 0.1F, 0.5F, 0.18F, 5);
      }

      var0.pop();
      sash(var0, -12966888, 30.0F);
      var0.color(-1);
   }

   static void quiver(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(1.6F, -5.4F, -3.9F);
      var0.rotZ(-24.0F);
      var0.color(-1);
      Cos3Geo.lathe(var0, "hd_quiver", new float[]{0.0F, 1.3F, 1.62F, 1.75F, 1.9F, 1.95F, 1.8F}, new float[]{-6.3F, -6.25F, -5.9F, -3.0F, 2.0F, 4.7F, 5.0F}, 18);
      var0.color(-1525430);
      var0.push();
      var0.translate(0.0F, 4.75F, 0.0F);
      var0.torus("hd_gold", 1.9F, 0.2F, 18, 5, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      int[] var3 = new int[]{-1560518, -723724, -13993240, -1560518, -723724};

      for (int var4 = 0; var4 < 5; var4++) {
         float var5 = -0.85F + var4 % 3 * 0.85F;
         float var6 = -0.55F + var4 / 3 * 1.05F;
         float var7 = 7.8F + var4 % 2 * 0.9F;
         var0.push();
         var0.translate(var5, 0.0F, var6);
         var0.rotZ((var4 - 2) * 3 + Cos3Geo.sin(var2 * 2.0F + var4) * 0.8F);
         var0.color(-3630502);
         Geo.tube(var0, "hd_wood", p(0.0F, 1.5F, 0.0F), p(0.0F, var7, 0.0F), 0.12F, 0.12F, 5);
         var0.color(var3[var4]);

         for (int var8 = 0; var8 < 3; var8++) {
            var0.push();
            var0.rotY(var8 * 120 + var4 * 17);
            float[][] var9 = new float[][]{{0.1F, var7 - 2.4F}, {0.75F, var7 - 2.1F}, {0.8F, var7 - 0.4F}, {0.1F, var7 - 0.2F}};
            var0.rotY(90.0F);
            HdGeo.puff(var0, "hd_satin", var9, 0.05F, 0.02F, 1);
            var0.pop();
         }

         var0.color(-14013904);
         Cos3Geo.ball(var0, "hd_plastic", 0.0F, var7 + 0.1F, 0.0F, 0.17F, 5);
         var0.pop();
      }

      var0.pop();
      sash(var0, -12966376, 38.0F);
      var0.color(-1);
   }

   static void rocket(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      float var3 = (var1.preview ? 0.0F : var1.move) * 0.15F * Cos3Geo.sin(var2 * 40.0F);
      var0.translate(var3, -4.0F + 0.2F * Cos3Geo.sin(var2 * 2.0F), -4.9F);
      var0.color(-1);
      Cos3Geo.lathe(var0, "hd_rocket", new float[]{1.5F, 1.95F, 2.2F, 2.25F, 2.2F}, new float[]{-6.2F, -5.6F, -4.2F, 0.0F, 3.0F}, 20, 0.0F, 1.0F, 1.0F, 0.1F);
      var0.color(-1900533);
      Cos3Geo.lathe(var0, "hd_gloss", new float[]{2.2F, 2.05F, 1.6F, 0.9F, 0.3F, 0.0F}, new float[]{3.0F, 4.5F, 5.9F, 7.0F, 7.6F, 7.7F}, 20);
      var0.push();
      var0.translate(0.0F, 0.9F, -2.25F);
      var0.rotY(180.0F);
      var0.color(-2564376);
      HdGeo.loop(var0, "hd_chrome", HdGeo.circle(0.95F, 0.95F, 18), 0.22F, 0.2F, 2.0F, 6);
      var0.color(-9778177);
      HdGeo.puff(var0, "hd_glass", HdGeo.circle(0.9F, 0.9F, 16), 0.2F, 0.08F, 1);
      var0.pop();
      var0.color(-1900533);
      float[][] var4 = new float[][]{{2.0F, -1.8F}, {2.0F, -6.0F}, {4.4F, -7.4F}, {4.3F, -4.8F}};

      for (int var5 = 0; var5 < 4; var5++) {
         var0.push();
         var0.rotY(45 + var5 * 90);
         var0.rotY(-90.0F);
         var0.push();
         var0.rotY(90.0F);
         HdGeo.puff(var0, "hd_gloss", var4, 0.35F, 0.12F, 1);
         var0.pop();
         var0.pop();
      }

      var0.color(-12960184);
      Cos3Geo.lathe(var0, "hd_steel", new float[]{1.2F, 1.1F, 1.55F, 1.4F}, new float[]{-6.1F, -6.5F, -7.5F, -7.6F}, 16);
      float var6 = 0.85F + 0.18F * Cos3Geo.sin(var2 * 25.0F) + (var1.preview ? 0.0F : var1.move * 0.8F);
      var0.push();
      var0.translate(0.0F, -7.5F, 0.0F);
      var0.rotZ(180.0F);
      Cos3Models.flame(var0, 5.2F * var6, 1.2F, -34278, -2880);
      var0.pop();
      Cos3Geo.halo(var0, 0.0F, -8.4F, 0.0F, 4.4F, Cos3Geo.alpha(0.55, 16751162));
      var0.pop();
      var0.color(-12960184);
      Cos3Geo.bbox(var0, "hd_steel", -1.2F, -5.2F, -2.9F, 1.2F, -3.6F, -2.05F, 0.15F);
      straps(var0, -14012874, 1.6F, -5.0F, -2.3F);
      var0.color(-1);
   }

   static void teddy(G var0, Cos.A var1) {
      float var2 = var1.time;
      var0.push();
      var0.translate(0.8F, -4.2F, -4.5F);
      var0.rotZ(Cos3Geo.sin(var2 * 1.6F) * 4.0F + (var1.preview ? 0.0F : var1.move * Cos3Geo.sin(var2 * 8.0F) * 6.0F));
      var0.rotY(180.0F);
      int var3 = -5211576;
      int var4 = -1521512;
      var0.color(var3);
      Geo.ellipsoid(var0, "hd_fur", 0.0F, -2.2F, 0.0F, 2.4F, 2.8F, 1.9F, 16, 10);
      var0.color(var4);
      Geo.ellipsoid(var0, "hd_fur", 0.0F, -2.4F, 1.0F, 1.55F, 1.85F, 1.0F, 12, 8);
      var0.color(var3);
      Geo.ellipsoid(var0, "hd_fur", 0.0F, 1.8F, 0.0F, 2.35F, 2.15F, 2.05F, 18, 12);
      var0.color(var4);
      Geo.ellipsoid(var0, "hd_fur", 0.0F, 1.2F, 1.75F, 1.1F, 0.8F, 0.7F, 12, 8);
      var0.color(-15069688);
      Geo.ellipsoid(var0, "hd_gloss", 0.0F, 1.55F, 2.4F, 0.4F, 0.28F, 0.22F, 8, 5);

      for (byte var5 = -1; var5 <= 1; var5 += 2) {
         Cos3Geo.ball(var0, "hd_gloss", var5 * 0.85F, 2.4F, 1.8F, 0.3F, 8);
         var0.color(var3);
         Geo.ellipsoid(var0, "hd_fur", var5 * 1.7F, 3.6F, 0.0F, 0.95F, 0.95F, 0.55F, 10, 6);
         var0.color(var4);
         Geo.ellipsoid(var0, "hd_fur", var5 * 1.7F, 3.55F, 0.32F, 0.55F, 0.55F, 0.28F, 8, 5);
         var0.color(var3);
         Geo.ellipsoid(var0, "hd_fur", var5 * 2.4F, -1.4F, 0.4F, 0.8F, 1.5F, 0.8F, 10, 6);
         Geo.ellipsoid(var0, "hd_fur", var5 * 1.3F, -4.8F, 0.6F, 0.95F, 0.85F, 1.15F, 10, 6);
         var0.color(var4);
         Geo.ellipsoid(var0, "hd_fur", var5 * 1.3F, -4.85F, 1.6F, 0.62F, 0.6F, 0.25F, 8, 5);
         var0.color(-15069688);
      }

      var0.push();
      var0.translate(0.0F, -0.25F, 1.7F);
      XmasGeo.bow(var0, -2080678, 0.7F);
      var0.pop();
      var0.color(-1);
      var0.pop();
      var0.color(-1);
   }

   static void balloon(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = 6.0F + Cos3Geo.sin(var2 * 0.9F) * 1.0F;
      float var4 = 1.0F + Cos3Geo.sin(var2 * 1.3F) * 0.8F;
      float var5 = -6.0F + Cos3Geo.cos(var2 * 0.7F) * 1.0F;
      float[] var6 = new float[]{2.5F, -9.0F, -2.3F};
      byte var7 = 12;
      float[][] var8 = new float[var7][];
      float[] var9 = new float[var7];

      for (int var10 = 0; var10 < var7; var10++) {
         float var11 = (float)var10 / (var7 - 1);
         var8[var10] = new float[]{
            Geo.lerp(var6[0], var3, var11) + Cos3Geo.sin(var11 * Math.PI) * 0.8F * Cos3Geo.sin(var2 * 2.0F),
            Geo.lerp(var6[1], var4 - 3.6F, var11) - Cos3Geo.sin(var11 * Math.PI) * 0.6F,
            Geo.lerp(var6[2], var5, var11)
         };
         var9[var10] = 0.06F;
      }

      var0.color(-723724);
      Geo.chain(var0, "hd_plastic", var8, var9, 4);
      var0.push();
      var0.translate(var3, var4, var5);
      var0.rotZ(Cos3Geo.sin(var2 * 1.1F) * 8.0F);
      var0.rotY(var2 * 25.0F);
      var0.color(-54694);
      HdGeo.puff(var0, "hd_gloss", HdGeo.heart(3.1F, 32), 2.6F, 1.25F, 4);
      var0.color(-2092998);
      Cos3Geo.lathe(var0, "hd_gloss", new float[]{0.05F, 0.42F, 0.28F, 0.45F, 0.0F}, new float[]{-2.75F, -3.0F, -3.25F, -3.5F, -3.6F}, 8);
      var0.pop();
      var0.color(-1);
   }
}
