package dev.lego.visual.sky;

import java.util.Random;

final class SkyGen3 {
   private static final double[] EYE = SkyGen.azel(300.0, 40.0);

   private SkyGen3() {
   }

   static SkyGen.Sky make(String var0) {
      switch (var0) {
         case "milkyway":
            return milkyway();
         case "nebula":
            return nebula();
         case "planets":
            return planets();
         case "aurora":
            return aurora();
         case "storm":
            return storm();
         case "bloodmoon":
            return bloodMoon();
         case "eclipse":
            return eclipse();
         case "auroraviolet":
            return auroraViolet();
         case "winter":
            return winter();
         case "twilight":
            return twilight();
         case "rednebula":
            return redNebula();
         case "spiral":
            return spiral();
         case "blackhole":
            return blackHole();
         case "void":
            return voidSky();
         default:
            return null;
      }
   }

   static void moon(SkyGen.C var0, double var1, double var3, double var5, SkyGen3.Body var7, double[] var8, double var9, double[] var11) {
      double var12 = var1 * var7.d[0] + var3 * var7.d[1] + var5 * var7.d[2];
      if (!(var12 < var7.cosOut)) {
         double var14 = Math.sqrt(Math.max(0.0, 2.0 * (1.0 - var12)));
         double var16 = var14 / var7.rad;
         if (var7.glow > 0.0) {
            double var18 = var7.glow * var9 * Math.exp(-Math.max(0.0, var16 - 1.0) / var7.glowW);
            SkyGen.add(var0, var8, var18);
         }

         double var50 = Math.max(0.02, 0.004 / var7.rad);
         if (!(var16 > 1.0 + var50)) {
            double var20 = (var1 * var7.ex[0] + var3 * var7.ex[1] + var5 * var7.ex[2]) / var7.sinR;
            double var22 = (var1 * var7.ey[0] + var3 * var7.ey[1] + var5 * var7.ey[2]) / var7.sinR;
            double var24 = var20 * var20 + var22 * var22;
            double var26 = Math.sqrt(Math.max(0.0, 1.0 - Math.min(1.0, var24)));
            double var28 = var7.ex[0] * var20 + var7.ey[0] * var22 - var7.d[0] * var26;
            double var30 = var7.ex[1] * var20 + var7.ey[1] * var22 - var7.d[1] * var26;
            double var32 = var7.ex[2] * var20 + var7.ey[2] * var22 - var7.d[2] * var26;
            double var34 = var7.seed;
            double var36 = SkyGen.smooth(-0.05, 0.3, SkyGen.fbmL(var28 * 1.8 + var34, var30 * 1.8, var32 * 1.8, 4.0));
            double var38 = SkyGen.smooth(0.55, 0.85, SkyGen.ridged(var28 * 6.0 + var34, var30 * 6.0, var32 * 6.0, 3));
            double var40 = SkyGen.fbmL(var28 * 14.0, var30 * 14.0 + var34, var32 * 14.0, 3.0);
            double var42 = var7.albedo * (1.0 - 0.42 * var36) * (0.9 + 0.25 * var40) * (1.0 + 0.25 * var38);
            double var44;
            if (var11 != null) {
               double var46 = var28 * var11[0] + var30 * var11[1] + var32 * var11[2];
               var44 = SkyGen.smooth(-0.04, 0.25, var46) * (0.35 + 0.65 * Math.max(0.0, var46));
            } else {
               var44 = 0.55 + 0.45 * var26;
            }

            if (var7.grad != null) {
               double var51 = var28 * var7.grad[0] + var30 * var7.grad[1] + var32 * var7.grad[2];
               var44 *= 0.3 + 0.7 * SkyGen.smooth(-0.9, 1.0, var51);
            }

            double var52 = var42 * (var44 * var9 + var7.earthshine);
            double var48 = SkyGen.smooth(1.0 + var50, 1.0 - var50, var16);
            var0.r = var0.r + (var0.r * 0.35 + var8[0] * var52 - var0.r) * var48;
            var0.g = var0.g + (var0.g * 0.35 + var8[1] * var52 - var0.g) * var48;
            var0.b = var0.b + (var0.b * 0.35 + var8[2] * var52 - var0.b) * var48;
            var0.vis *= 1.0 - var48;
         }
      }
   }

   static double milky(SkyGen.C var0, double var1, double var3, double var5, double[] var7, double[] var8, double var9) {
      double var11 = var1 * var7[0] + var3 * var7[1] + var5 * var7[2];
      if (Math.abs(var11) > 0.7) {
         return 0.0;
      } else {
         double var13 = SkyGen.fbmL(var1 * 3.0 + 1.0, var3 * 3.0, var5 * 3.0, 4.0);
         double var15 = (var11 + var13 * 0.07) / 0.17;
         double var17 = Math.exp(-var15 * var15);
         double var19 = var1 * var8[0] + var3 * var8[1] + var5 * var8[2];
         double var21 = Math.exp(-(1.0 - var19) / 0.09) * Math.exp(-var11 * var11 / 0.03);
         double var23 = SkyGen.fbmL(var1 * 7.0 + 3.0, var3 * 7.0, var5 * 7.0, 6.0);
         double var25 = SkyGen.clamp01(0.45 + var23 * 1.1);
         double var27 = SkyGen.ridged(var1 * 5.0 + 2.0, var3 * 5.0, var5 * 5.0, 5);
         double var29 = var11 / 0.06;
         double var31 = SkyGen.smooth(0.35, 0.8, var27) * Math.exp(-var29 * var29)
            + 0.5 * SkyGen.smooth(0.1, 0.5, SkyGen.fbmL(var1 * 9.0, var3 * 9.0 + 5.0, var5 * 9.0, 4.0)) * var17;
         var31 = SkyGen.clamp01(var31);
         double var33 = (var17 * (0.25 + var25) + var21 * 1.4) * (1.0 - 0.8 * var31);
         double var35 = SkyGen.clamp01(var21 * 1.5);
         var0.r += var9 * var33 * (0.55 + 0.5 * var35);
         var0.g += var9 * var33 * (0.62 + 0.22 * var35);
         var0.b += var9 * var33 * (0.88 - 0.25 * var35);
         return var17;
      }
   }

   static double milkyDensity(double var0, double var2, double var4, double[] var6) {
      double var7 = var0 * var6[0] + var2 * var6[1] + var4 * var6[2];
      double var9 = SkyGen.fbmL(var0 * 3.0 + 1.0, var2 * 3.0, var4 * 3.0, 4.0);
      double var11 = (var7 + var9 * 0.07) / 0.17;
      return Math.exp(-var11 * var11);
   }

   static void aurora(SkyGen.C var0, double var1, double var3, double var5, SkyGen3.Aurora var7) {
      if (!(var3 < 0.004)) {
         double var8 = SkyGen.smooth(0.004, 0.15, var3);
         double var10 = Math.sqrt(var1 * var1 + var5 * var5) + 1.0E-9;
         double var12 = SkyGen.smooth(
            -0.35, 0.25, SkyGen.fbmL(var1 / var10 * 1.3 + var7.seed, 0.5, var5 / var10 * 1.3, 2.0) + var7.cover + 0.4 * (-var5 / var10) * var7.north
         );
         if (!(var12 <= 0.003)) {
            double var14 = var7.scale / (var3 + 0.25);
            double var16 = var10 * (var7.h1 - var7.h0) * var14 * 0.9;
            int var18 = (int)Math.max(6.0, Math.min((double)var7.steps, Math.ceil(var16 / 0.06)));
            double var19 = (var7.h1 - var7.h0) / var18;
            double var21 = auroraF(var1 * var7.h0 * var14, var5 * var7.h0 * var14, var7);
            double var23 = var7.h0;
            double var25 = Math.exp(-var21 * var21 / 0.008);
            double var27 = 0.0;
            double var29 = 0.0;
            double var31 = 0.0;

            for (int var33 = 1; var33 <= var18; var33++) {
               double var34 = var7.h0 + var19 * var33;
               double var36 = auroraF(var1 * var34 * var14, var5 * var34 * var14, var7);
               double var38 = 0.0;
               double var40 = 0.0;
               if (var21 <= 0.0 != var36 <= 0.0) {
                  var38 = var21 / (var21 - var36);
                  var40 = 1.0;
               }

               if (var40 > 0.002) {
                  double var42 = var23 + var19 * var38;
                  double var44 = (var42 - var7.h0) / (var7.h1 - var7.h0);
                  double var46 = Math.abs(var36 - var21) / var19;
                  double var48 = var7.width / Math.sqrt(var46 * var46 + var7.width * var7.width);
                  double var50 = var1 * var42 * var14;
                  double var52 = var5 * var42 * var14;
                  double var54 = 0.25 + 0.75 * Math.abs(SkyGen.noise(var50 * 9.0 + var7.seed, 3.7, var52 * 9.0)) * var7.rays + (1.0 - var7.rays) * 0.75;
                  double var56 = (Math.exp(-var44 * 2.6) * SkyGen.smooth(0.0, 0.06, var44) + 0.12 * Math.exp(-(var44 - 0.6) * (var44 - 0.6) / 0.04))
                     * SkyGen.smooth(1.0, 0.75, var44);
                  double var58 = Math.pow(var44, 0.8);
                  double var60 = var48 * var54 * var56 * var40;
                  var27 += (var7.low[0] + (var7.high[0] - var7.low[0]) * var58) * var60;
                  var29 += (var7.low[1] + (var7.high[1] - var7.low[1]) * var58) * var60;
                  var31 += (var7.low[2] + (var7.high[2] - var7.low[2]) * var58) * var60;
               }

               var21 = var36;
               var23 = var34;
            }

            double var62 = var7.bright * var8 * var12;
            double var35 = 0.03 * var25 * var62;
            var0.r = var0.r + (var27 * var62 + var7.low[0] * var35);
            var0.g = var0.g + (var29 * var62 + var7.low[1] * var35);
            var0.b = var0.b + (var31 * var62 + var7.low[2] * var35);
         }
      }
   }

   private static double auroraF(double var0, double var2, SkyGen3.Aurora var4) {
      return SkyGen.fbmL(var0 * 0.9 + var4.seed, var4.seed * 0.7, var2 * 0.9, 2.5);
   }

   static void nebulaLayer(
      SkyGen.C var0,
      double var1,
      double var3,
      double var5,
      double var7,
      double var9,
      double[] var11,
      double[] var12,
      double[] var13,
      double[] var14,
      double var15,
      double var17
   ) {
      double var19 = SkyGen.fbmL(var1 * 1.3 + 3.1 + var7, var3 * 1.3, var5 * 1.3, 3.0);
      double var21 = SkyGen.fbmL(var1 * 1.3, var3 * 1.3 + 7.7, var5 * 1.3 + var7, 3.0);
      double var23 = SkyGen.fbmL(var1 * 1.3 + var7, var3 * 1.3, var5 * 1.3 + 1.3, 3.0);
      double var25 = (var1 + var19 * 0.4) * var9;
      double var27 = (var3 + var21 * 0.4) * var9;
      double var29 = (var5 + var23 * 0.4) * var9;
      double var31 = SkyGen.smooth(-0.05, 0.6, SkyGen.fbmL(var25 * 1.5, var27 * 1.5, var29 * 1.5 + var7, 6.0) + 0.12);
      if (!(var31 <= 0.001)) {
         double var33 = SkyGen.ridged(var25 * 4.0 + var7, var27 * 4.0, var29 * 4.0, 5);
         double var35 = SkyGen.smooth(0.5, 0.95, var33) * var31;
         double var37 = SkyGen.clamp01(0.5 + SkyGen.fbmL(var25 * 0.8 + 7.0, var27 * 0.8, var29 * 0.8 + var7, 3.0) * 1.6);
         double var39 = var31 * var31 * 0.55 + var31 * 0.12;
         double var41 = var11[0] + (var12[0] - var11[0]) * var37;
         double var43 = var11[1] + (var12[1] - var11[1]) * var37;
         double var45 = var11[2] + (var12[2] - var11[2]) * var37;
         var0.r = var0.r + var15 * (var41 * var39 + (var13[0] * 0.6 + var41 * 0.6) * var35);
         var0.g = var0.g + var15 * (var43 * var39 + (var13[1] * 0.6 + var43 * 0.6) * var35);
         var0.b = var0.b + var15 * (var45 * var39 + (var13[2] * 0.6 + var45 * 0.6) * var35);
         double var49 = Math.max(0.0, var31 - 0.75) / 0.25;
         SkyGen.add(var0, var14, var15 * var49 * var49 * 0.8);
         double var51 = SkyGen.smooth(0.45, 0.85, SkyGen.ridged(var25 * 2.3 + 3.0, var27 * 2.3 + var7, var29 * 2.3, 4)) * SkyGen.smooth(0.1, 0.6, var31);
         double var53 = 1.0 - var51 * var17;
         SkyGen.mul(var0, var53);
         var0.vis *= 0.4 + 0.6 * var53;
      }
   }

   static SkyGen.Sky milkyway() {
      SkyGen3.Night var0 = new SkyGen3.Night();
      var0.zen = SkyGen.rgb(132365);
      var0.hor = SkyGen.rgb(924206);
      var0.hW = 0.28;
      var0.air = SkyGen.rgb(928284);
      var0.airK = 0.6;
      var0.airW = 0.07;
      var0.lp = SkyGen.rgb(3810320);
      var0.lpDir = SkyGen.norm(-0.7, 0.0, 0.7);
      var0.lpK = 0.5;
      var0.mwN = SkyGen.norm(0.35, 0.62, 0.7);
      var0.mwCore = coreOn(var0.mwN, SkyGen.norm(0.75, 0.2, -0.6));
      var0.mwK = 0.075;
      var0.stars = 90000;
      var0.ground = SkyGen.rgb(329483);
      var0.groundFar = SkyGen.rgb(923170);
      var0.groundFall = 0.2;
      return var0;
   }

   static SkyGen.Sky aurora() {
      SkyGen3.Night var0 = new SkyGen3.Night();
      var0.zen = SkyGen.rgb(132880);
      var0.hor = SkyGen.rgb(729648);
      var0.hW = 0.3;
      var0.mwN = SkyGen.norm(-0.5, 0.55, 0.65);
      var0.mwCore = coreOn(var0.mwN, SkyGen.norm(0.3, 0.3, 0.9));
      var0.mwK = 0.025;
      SkyGen3.Aurora var1 = new SkyGen3.Aurora();
      var1.low = SkyGen.rgb(3080070);
      var1.high = SkyGen.rgb(11553992);
      var1.bright = 0.55;
      var1.seed = 2.7;
      var1.width = 0.14;
      var0.au = var1;
      var0.mist = SkyGen.rgb(929840);
      var0.mistK = 0.35;
      var0.mistW = 0.03;
      var0.detail = 0.85;
      var0.ground = SkyGen.rgb(1186854);
      var0.groundFar = SkyGen.rgb(1980988);
      var0.groundFall = 0.25;
      return var0;
   }

   static SkyGen.Sky auroraViolet() {
      SkyGen3.Night var0 = new SkyGen3.Night();
      var0.zen = SkyGen.rgb(328463);
      var0.hor = SkyGen.rgb(1839152);
      var0.hW = 0.3;
      SkyGen3.Aurora var1 = new SkyGen3.Aurora();
      var1.low = SkyGen.rgb(16727968);
      var1.high = SkyGen.rgb(5914879);
      var1.bright = 0.55;
      var1.seed = 7.3;
      var1.width = 0.14;
      var1.cover = 0.0;
      var0.au = var1;
      var0.stars = 70000;
      var0.below = 2;
      var0.water = SkyGen.rgb(131849);
      var0.waves = 0.3;
      var0.glitter = 0.0;
      var0.detail = 0.85;
      return var0;
   }

   static SkyGen.Sky storm() {
      final double[][] var0 = new double[][]{SkyGen.azel(40.0, 25.0), SkyGen.azel(160.0, 14.0), SkyGen.azel(290.0, 35.0), SkyGen.azel(250.0, 60.0)};
      final double[] var1 = SkyGen.rgb(12104959);
      SkyGen3.Night var2 = new SkyGen3.Night() {
         @Override
         void extra(double var1x, double var3, double var5, SkyGen.C var7) {
            double var8 = Math.sqrt(var1x * var1x + var5 * var5) + 1.0E-9;
            double var10 = Math.max(0.0, (var1x * 0.8 - var5 * 0.6) / var8);
            SkyGen.add(var7, var1, 0.05 * var10 * var10 * var10 * var10 * Math.exp(-Math.max(0.0, var3) / 0.08));
         }

         @Override
         void sky(double var1x, double var3, double var5, SkyGen.C var7) {
            super.sky(var1x, var3, var5, var7);
            if (!(var3 <= 0.0)) {
               double var8 = 0.0;

               for (double[] var13 : var0) {
                  double var14 = var1x * var13[0] + var3 * var13[1] + var5 * var13[2];
                  double var16 = 2.0 * (1.0 - var14);
                  var8 += Math.exp(-var16 / 0.06);
               }

               if (var8 > 0.003) {
                  double var18 = 1.0 / (var3 + 0.05);
                  double var19 = SkyGen.fbmL(var1x * var18 * 1.2, 5.5, var5 * var18 * 1.2, 5.0);
                  double var20 = var8 * SkyGen.smooth(-0.3, 0.5, var19) * SkyGen.smooth(0.0, 0.1, var3);
                  SkyGen.add(var7, var1, var20 * 0.45);
               }
            }
         }
      };
      var2.stars = 0;
      var2.zen = SkyGen.rgb(790294);
      var2.hor = SkyGen.rgb(2501688);
      var2.hW = 0.25;
      SkyGen.Clouds var3 = new SkyGen.Clouds();
      var3.height = 0.8;
      var3.scale = 0.7;
      var3.cov = -0.15;
      var3.soft = 0.45;
      var3.warp = 1.3;
      var3.seed = 4.1;
      var3.fog = 6.0;
      var3.lit = SkyGen.rgb(5266030);
      var3.shade = SkyGen.rgb(592656);
      var3.thickDark = 0.75;
      var3.silver = 0.0;
      var3.lightSide = 1.6;
      var2.c1 = var3;
      var2.sun = SkyGen.azel(40.0, 25.0);
      var2.mist = SkyGen.rgb(1975084);
      var2.mistK = 0.55;
      var2.mistW = 0.05;
      var2.ground = SkyGen.rgb(461068);
      var2.groundFar = SkyGen.rgb(1711654);
      var2.groundFall = 0.2;
      return var2;
   }

   static SkyGen.Sky bloodMoon() {
      SkyGen3.Night var0 = new SkyGen3.Night();
      var0.zen = SkyGen.rgb(328458);
      var0.hor = SkyGen.rgb(2756626);
      var0.hW = 0.22;
      var0.air = SkyGen.rgb(3804680);
      var0.airK = 0.3;
      var0.airW = 0.05;
      var0.mwN = SkyGen.norm(0.2, 0.7, -0.68);
      var0.mwCore = coreOn(var0.mwN, SkyGen.norm(-0.8, 0.3, 0.1));
      var0.mwK = 0.02;
      SkyGen3.Body var1 = new SkyGen3.Body(SkyGen.azel(160.0, 24.0), 0.09, 25.0);
      var1.glow = 0.035;
      var1.glowW = 2.5;
      var1.albedo = 0.6;
      var1.seed = 3.0;
      var1.grad = SkyGen.norm(0.3, 1.0, 0.2);
      var1.earthshine = 0.0;
      var0.moon = var1;
      var0.moonTint = SkyGen.rgb(16734764);
      var0.moonBright = 1.5;
      var0.mist = SkyGen.rgb(2755596);
      var0.mistK = 0.5;
      var0.mistW = 0.04;
      var0.stars = 50000;
      var0.ground = SkyGen.rgb(787718);
      var0.groundFar = SkyGen.rgb(2363406);
      var0.groundFall = 0.2;
      return var0;
   }

   static SkyGen.Sky eclipse() {
      final double[] var0 = SkyGen.azel(165.0, 36.0);
      final double[] var1 = SkyGen.rgb(15723263);
      final double[] var2 = SkyGen.rgb(16726634);
      final double[] var3 = SkyGen.rgb(16751178);
      final double[] var4 = SkyGen.rgb(15255696);
      final double[][] var5 = SkyGen.frame(var0, 0.0);
      SkyGen3.Night var6 = new SkyGen3.Night() {
         @Override
         void extra(double var1x, double var3x, double var5x, SkyGen.C var7) {
            double var8 = Math.max(0.0, var3x);
            double var10 = Math.sqrt(var1x * var1x + var5x * var5x) + 1.0E-9;
            double var12 = 0.6 + 0.4 * SkyGen.fbmL(var1x / var10 * 2.0, 1.1, var5x / var10 * 2.0, 3.0) + 0.3 * ((var1x * var0[0] + var5x * var0[2]) / var10);
            SkyGen.add(var7, var3, 0.55 * var12 * Math.exp(-var8 / 0.035));
            SkyGen.add(var7, var4, 0.12 * Math.exp(-var8 / 0.12));
            double var14 = var1x * var0[0] + var3x * var0[1] + var5x * var0[2];
            if (!(var14 < 0.6)) {
               double var16 = Math.sqrt(Math.max(0.0, 2.0 * (1.0 - var14)));
               double var18 = var16 / 0.034;
               double var20 = var1x * var5[0][0] + var3x * var5[0][1] + var5x * var5[0][2];
               double var22 = var1x * var5[1][0] + var3x * var5[1][1] + var5x * var5[1][2];
               double var24 = Math.sqrt(var20 * var20 + var22 * var22) + 1.0E-12;
               double var26 = var20 / var24;
               double var28 = var22 / var24;
               if (var18 > 0.99) {
                  double var30 = 0.45 + 0.9 * Math.max(0.0, SkyGen.fbmL(var26 * 2.5, var28 * 2.5, var18 * 0.35, 4.0) + 0.2);
                  double var32 = Math.pow(Math.abs(SkyGen.noise(var26 * 9.0, var28 * 9.0, 1.3)), 0.5);
                  double var34 = 1.4 * Math.pow(var18, -2.6) * (0.3 + var30 * 0.9 + 0.3 * var32) + 0.05 * Math.pow(var18, -1.0);
                  SkyGen.add(var7, var1, var34 * SkyGen.smooth(0.99, 1.02, var18));
                  if (var18 < 1.12) {
                     double var36 = SkyGen.smooth(0.25, 0.55, SkyGen.fbmL(var26 * 6.0 + 4.0, var28 * 6.0, 2.2, 3.0)) * SkyGen.smooth(1.12, 1.0, var18);
                     SkyGen.add(var7, var2, var36 * 2.2);
                  }

                  double var43 = var26 - Math.cos(2.3);
                  double var38 = var28 - Math.sin(2.3);
                  double var40 = (var43 * var43 + var38 * var38) * 60.0 + (var18 - 1.0) * (var18 - 1.0) * 900.0;
                  SkyGen.add(var7, var1, 12.0 * Math.exp(-var40) + 0.25 * Math.exp(-(var43 * var43 + var38 * var38) * 3.0 - (var18 - 1.0) * 1.2));
               }

               double var42 = SkyGen.smooth(1.0, 0.985, var18);
               var7.r *= 1.0 - var42 * 0.97;
               var7.g *= 1.0 - var42 * 0.97;
               var7.b *= 1.0 - var42 * 0.97;
               var7.vis *= 1.0 - var42;
            }
         }
      };
      var6.zen = SkyGen.rgb(726322);
      var6.hor = SkyGen.rgb(2372174);
      var6.hW = 0.3;
      var6.stars = 30000;
      var6.starDens = 0.6;
      var6.ground = SkyGen.rgb(460812);
      var6.groundFar = SkyGen.rgb(2761254);
      var6.groundFall = 0.07;
      return var6;
   }

   static SkyGen.Sky winter() {
      SkyGen3.Night var0 = new SkyGen3.Night();
      var0.zen = SkyGen.rgb(198936);
      var0.hor = SkyGen.rgb(1848402);
      var0.hW = 0.28;
      SkyGen3.Body var1 = new SkyGen3.Body(SkyGen.azel(205.0, 34.0), 0.018, 0.0);
      var1.glow = 0.07;
      var1.glowW = 2.2;
      var1.albedo = 0.9;
      var1.seed = 1.5;
      var0.moon = var1;
      var0.moonTint = SkyGen.rgb(15397119);
      var0.moonBright = 2.2;
      var0.halo22 = 0.035;
      SkyGen.Clouds var2 = new SkyGen.Clouds();
      var2.height = 3.0;
      var2.scale = 0.3;
      var2.stretch = 4.0;
      var2.cov = 0.22;
      var2.soft = 0.35;
      var2.warp = 1.5;
      var2.seed = 3.7;
      var2.oct = 5.0;
      var2.lit = SkyGen.rgb(4873342);
      var2.shade = SkyGen.rgb(1186864);
      var2.thickDark = 0.2;
      var2.silver = 1.0;
      var2.opacity = 0.6;
      var2.fog = 25.0;
      var0.c1 = var2;
      var0.cloudLight = SkyGen.rgb(8427720);
      var0.mist = SkyGen.rgb(3033192);
      var0.mistK = 0.55;
      var0.mistW = 0.04;
      var0.stars = 70000;
      var0.starDens = 0.9;
      var0.ground = SkyGen.rgb(2898514);
      var0.groundFar = SkyGen.rgb(4217468);
      var0.groundFall = 0.3;
      var0.groundNoise = 0.35;
      return var0;
   }

   static SkyGen.Sky twilight() {
      SkyGen3.Night var0 = new SkyGen3.Night() {
         @Override
         void moteColor(Random var1, double[] var2) {
            double var3 = var1.nextDouble();
            if (var3 < 0.55) {
               var2[0] = 1.3;
               var2[1] = 0.95;
               var2[2] = 0.45;
            } else if (var3 < 0.8) {
               var2[0] = 0.45;
               var2[1] = 1.0;
               var2[2] = 1.3;
            } else {
               var2[0] = 1.25;
               var2[1] = 0.55;
               var2[2] = 1.2;
            }
         }

         @Override
         double moteDensity(double var1, double var3, double var5) {
            return 0.08 + 0.92 * Math.exp(-Math.abs(var3 + 0.04) / 0.14);
         }
      };
      var0.zen = SkyGen.rgb(525856);
      var0.hor = SkyGen.rgb(9326222);
      var0.hW = 0.12;
      var0.air = SkyGen.rgb(3808880);
      var0.airK = 0.25;
      var0.airW = 0.35;
      var0.lp = SkyGen.rgb(16751226);
      var0.lpDir = SkyGen.norm(-1.0, 0.0, -0.3);
      var0.lpK = 0.6;
      SkyGen3.Body var1 = new SkyGen3.Body(SkyGen.azel(250.0, 30.0), 0.03, 0.0);
      var1.glow = 0.03;
      var1.glowW = 1.5;
      var1.albedo = 0.9;
      var1.seed = 5.0;
      var0.moon = var1;
      var0.moonTint = SkyGen.rgb(16773338);
      var0.moonBright = 2.0;
      var0.moonLight = SkyGen.azel(290.0, -10.0);
      SkyGen.Clouds var2 = new SkyGen.Clouds();
      var2.height = 1.4;
      var2.scale = 0.5;
      var2.stretch = 2.5;
      var2.cov = 0.16;
      var2.soft = 0.3;
      var2.warp = 1.4;
      var2.seed = 11.2;
      var2.fog = 12.0;
      var2.lit = SkyGen.rgb(14715584);
      var2.shade = SkyGen.rgb(3022944);
      var2.thickDark = 0.35;
      var2.silver = 0.5;
      var2.opacity = 0.85;
      var0.c1 = var2;
      var0.cloudLight = SkyGen.rgb(16756944);
      var0.sun = SkyGen.azel(290.0, -10.0);
      var0.stars = 50000;
      var0.starDens = 0.8;
      var0.motes = 1400;
      var0.moteBright = 0.7;
      var0.moteSize = 1.4;
      var0.ground = SkyGen.rgb(657432);
      var0.groundFar = SkyGen.rgb(3810386);
      var0.groundFall = 0.2;
      return var0;
   }

   static SkyGen.Sky nebula() {
      final double[] var0 = SkyGen.rgb(3816136);
      final double[] var1 = SkyGen.rgb(11549368);
      final double[] var2 = SkyGen.rgb(8048895);
      final double[] var3 = SkyGen.rgb(16770768);
      final double[] var4 = SkyGen.rgb(131850);
      final double[] var5 = SkyGen.norm(0.2, 0.9, 0.3);
      SkyGen3.Space var6 = new SkyGen3.Space() {
         @Override
         void sky(double var1x, double var3x, double var5x, SkyGen.C var7) {
            SkyGen.set(var7, var4);
            SkyGen3.milky(var7, var1x, var3x, var5x, var5, SkyGen3.coreOn(var5, SkyGen.norm(1.0, 0.0, 0.0)), 0.02);
            SkyGen3.nebulaLayer(var7, var1x, var3x, var5x, 0.0, 1.0, var0, var1, var2, var3, 0.5, 0.85);
         }

         @Override
         double starDensity(double var1x, double var3x, double var5x) {
            return 0.3 + 0.5 * SkyGen3.milkyDensity(var1x, var3x, var5x, var5);
         }
      };
      var6.stars = 90000;
      var6.detail = 0.8;
      return var6;
   }

   static SkyGen.Sky redNebula() {
      final double[] var0 = SkyGen.rgb(12591146);
      final double[] var1 = SkyGen.rgb(16742960);
      final double[] var2 = SkyGen.rgb(16756848);
      final double[] var3 = SkyGen.rgb(16773320);
      final double[] var4 = SkyGen.rgb(262660);
      final double[] var5 = SkyGen.azel(30.0, 15.0);
      final double[] var6 = SkyGen.rgb(10141951);
      SkyGen3.Space var7 = new SkyGen3.Space() {
         @Override
         void sky(double var1x, double var3x, double var5x, SkyGen.C var7x) {
            SkyGen.set(var7x, var4);
            SkyGen3.nebulaLayer(var7x, var1x, var3x, var5x, 4.2, 1.1, var0, var1, var2, var3, 0.55, 0.9);
            double var8 = var1x * var5[0] + var3x * var5[1] + var5x * var5[2];
            double var10 = 2.0 * (1.0 - var8);
            SkyGen.add(var7x, var6, 0.35 * Math.exp(-var10 / 0.004) + 0.08 * Math.exp(-var10 / 0.05));
            double var12 = SkyGen.fbmL(var1x * 2.2 + 2.0, var3x * 2.2, var5x * 2.2 + 9.0, 5.0);
            double var14 = SkyGen.smooth(0.28, 0.45, var12);
            SkyGen.mul(var7x, 1.0 - 0.9 * var14);
            var7x.vis *= 1.0 - 0.95 * var14;
         }

         @Override
         double starDensity(double var1x, double var3x, double var5x) {
            double var7x = var1x * var5[0] + var3x * var5[1] + var5x * var5[2];
            return Math.min(1.0, 0.3 + 0.9 * Math.exp(-2.0 * (1.0 - var7x) / 0.02));
         }
      };
      var7.stars = 80000;
      var7.detail = 0.9;
      return var7;
   }

   static SkyGen.Sky planets() {
      final double[] var0 = SkyGen.rgb(66056);
      final double[] var1 = SkyGen.rgb(1719434);
      final double[] var2 = SkyGen.rgb(5906042);
      final double[] var3 = SkyGen.azel(35.0, 22.0);
      final double var4 = Math.sin(0.26);
      final double[] var6 = SkyGen.norm(0.25, 0.95, 0.35);
      final double[] var7 = SkyGen.azel(250.0, 25.0);
      final double[] var8 = SkyGen.rgb(15258280);
      final double[] var9 = SkyGen.rgb(12618330);
      final double[] var10 = SkyGen.rgb(9067066);
      final double[] var11 = SkyGen.rgb(15919312);
      final double[] var12 = SkyGen.rgb(14207144);
      final double[] var13 = SkyGen.rgb(16769712);
      final SkyGen3.Body var14 = new SkyGen3.Body(SkyGen.azel(300.0, 34.0), 0.045, 0.0);
      final SkyGen3.Body var15 = new SkyGen3.Body(SkyGen.azel(95.0, 55.0), 0.02, 0.0);
      final SkyGen3.Body var16 = new SkyGen3.Body(SkyGen.azel(200.0, 12.0), 0.012, 0.0);
      var14.albedo = 0.7;
      var14.seed = 2.0;
      var15.albedo = 0.9;
      var15.seed = 7.0;
      var16.albedo = 0.5;
      var16.seed = 11.0;
      final double[] var17 = SkyGen.rgb(14209224);
      final double[] var18 = SkyGen.rgb(13624575);
      final double[] var19 = SkyGen.rgb(16751216);
      SkyGen3.Space var20 = new SkyGen3.Space() {
         @Override
         void sky(double var1x, double var3x, double var5, SkyGen.C var7x) {
            SkyGen.set(var7x, var0);
            double var8x = SkyGen.smooth(0.1, 0.8, SkyGen.fbmL(var1x * 2.0 + 4.0, var3x * 2.0, var5 * 2.0, 5.0));
            SkyGen.add(var7x, var1, var8x * 0.12);
            SkyGen.add(var7x, var2, SkyGen.smooth(0.3, 0.9, SkyGen.fbmL(var1x * 3.0, var3x * 3.0 + 2.0, var5 * 3.0, 4.0)) * 0.1);
            SkyGen3.moon(var7x, var1x, var3x, var5, var14, var17, 1.3, var7);
            SkyGen3.moon(var7x, var1x, var3x, var5, var15, var18, 1.4, var7);
            SkyGen3.moon(var7x, var1x, var3x, var5, var16, var19, 1.2, var7);
            this.ringed(var7x, var1x, var3x, var5);
         }

         void ringed(SkyGen.C var1x, double var2x, double var4x, double var6x) {
            double var8x = var2x * var3[0] + var4x * var3[1] + var6x * var3[2];
            if (!(var8x < 0.3)) {
               double var10x = var8x * var8x - (1.0 - var4 * var4);
               double var12x = Double.MAX_VALUE;
               double var14x = 0.0;
               double var16x = 0.0;
               double var18x = 0.0;
               double var20x = 0.0;
               double var22 = Math.sqrt(Math.max(0.0, var10x)) / var4;
               if (var10x > -4.0E-4 * var4) {
                  var14x = SkyGen.smooth(-0.004, 0.012, var10x / var4);
                  var12x = var8x - Math.sqrt(Math.max(0.0, var10x));
                  var16x = var2x * var12x;
                  var18x = var4x * var12x;
                  var20x = var6x * var12x;
               }

               double var24 = var2x * var6[0] + var4x * var6[1] + var6x * var6[2];
               double var26 = Math.abs(var24) > 1.0E-5 ? (var3[0] * var6[0] + var3[1] * var6[1] + var3[2] * var6[2]) / var24 : -1.0;
               double var28 = 0.0;
               double var30 = 0.0;
               if (var26 > 0.0) {
                  double var32 = var2x * var26 - var3[0];
                  double var34 = var4x * var26 - var3[1];
                  double var36 = var6x * var26 - var3[2];
                  double var38 = Math.sqrt(var32 * var32 + var34 * var34 + var36 * var36) / var4;
                  if (var38 > 1.25 && var38 < 2.35) {
                     var28 = SkyGen3.ringDensity(var38);
                     double var40 = var2x * var26;
                     double var42 = var4x * var26;
                     double var44 = var6x * var26;
                     double var46 = SkyGen3.sphereHit(var40, var42, var44, var7, var3, var4);
                     double var48 = 0.35 + 0.65 * Math.abs(var7[0] * var6[0] + var7[1] * var6[1] + var7[2] * var6[2]);
                     var30 = var48 * (1.0 - 0.92 * var46);
                  }
               }

               boolean var76 = var28 > 0.0 && var26 < var12x;
               if (var14x > 0.0) {
                  double var33 = (var16x - var3[0]) / var4;
                  double var35 = (var18x - var3[1]) / var4;
                  double var37 = (var20x - var3[2]) / var4;
                  double var39 = var33 * var6[0] + var35 * var6[1] + var37 * var6[2];
                  double var41 = SkyGen.fbmL(var33 * 3.0, var35 * 3.0, var37 * 3.0, 4.0) * 0.12;
                  double var43 = Math.sin((var39 + var41) * 22.0) * 0.5 + 0.5;
                  double var45 = Math.sin((var39 + var41 * 1.5) * 57.0 + 1.3) * 0.5 + 0.5;
                  double var47 = SkyGen.fbmL(var33 * 8.0, var35 * 8.0 + 3.0, var37 * 8.0, 4.0);
                  double[] var49 = var43 > 0.5 ? var8 : var9;
                  double var50 = Math.abs(var43 - 0.5) * 2.0;
                  double var52 = SkyGen.mix(var9[0], var49[0], var50);
                  double var54 = SkyGen.mix(var9[1], var49[1], var50);
                  double var56 = SkyGen.mix(var9[2], var49[2], var50);
                  var52 = SkyGen.mix(var52, var10[0], var45 * 0.35);
                  var54 = SkyGen.mix(var54, var10[1], var45 * 0.35);
                  var56 = SkyGen.mix(var56, var10[2], var45 * 0.35);
                  double var58 = SkyGen.smooth(0.3, 0.5, var47) * 0.5;
                  var52 = SkyGen.mix(var52, var11[0], var58);
                  var54 = SkyGen.mix(var54, var11[1], var58);
                  var56 = SkyGen.mix(var56, var11[2], var58);
                  double var60 = var33 * var7[0] + var35 * var7[1] + var37 * var7[2];
                  double var62 = SkyGen.smooth(-0.1, 0.3, var60) * (0.25 + 0.75 * Math.max(0.0, var60));
                  double var64 = var7[0] * var6[0] + var7[1] * var6[1] + var7[2] * var6[2];
                  if (Math.abs(var64) > 1.0E-4) {
                     double var66 = ((var3[0] - var16x) * var6[0] + (var3[1] - var18x) * var6[1] + (var3[2] - var20x) * var6[2]) / var64;
                     if (var66 > 0.0) {
                        double var68 = var16x + var7[0] * var66 - var3[0];
                        double var70 = var18x + var7[1] * var66 - var3[1];
                        double var72 = var20x + var7[2] * var66 - var3[2];
                        double var74 = Math.sqrt(var68 * var68 + var70 * var70 + var72 * var72) / var4;
                        if (var74 > 1.25 && var74 < 2.35) {
                           var62 *= 1.0 - 0.8 * SkyGen3.ringDensity(var74);
                        }
                     }
                  }

                  double var85 = Math.pow(1.0 - Math.min(1.0, var22), 4.0);
                  double var86 = 1.3 * var62;
                  double var87 = var52 * var86 + var13[0] * var85 * var62 * 0.6;
                  double var89 = var54 * var86 + var13[1] * var85 * var62 * 0.6;
                  double var91 = var56 * var86 + var13[2] * var85 * var62 * 0.6;
                  var87 += 0.004;
                  var89 += 0.004;
                  var91 += 0.006;
                  var1x.r = var1x.r + (var87 - var1x.r) * var14x;
                  var1x.g = var1x.g + (var89 - var1x.g) * var14x;
                  var1x.b = var1x.b + (var91 - var1x.b) * var14x;
                  var1x.vis *= 1.0 - var14x;
               }

               if (var28 > 0.0 && (var76 || var14x < 1.0)) {
                  double var77 = var28 * (var76 ? 1.0 : 1.0 - var14x);
                  double var78 = 1.2 * var30;
                  var1x.r = var1x.r + (var12[0] * var78 - var1x.r) * var77;
                  var1x.g = var1x.g + (var12[1] * var78 - var1x.g) * var77;
                  var1x.b = var1x.b + (var12[2] * var78 - var1x.b) * var77;
                  var1x.vis *= 1.0 - var77;
               }
            }
         }
      };
      var20.stars = 70000;
      return var20;
   }

   static double ringDensity(double var0) {
      double var2 = 0.35 + 0.35 * SkyGen.noise(var0 * 18.0, 1.1, 0.5) + 0.2 * SkyGen.noise(var0 * 55.0, 2.3, 0.1);
      var2 *= SkyGen.smooth(1.25, 1.32, var0) * SkyGen.smooth(2.35, 2.25, var0);
      var2 *= 1.0 - 0.9 * SkyGen.smooth(0.035, 0.0, Math.abs(var0 - 1.95));
      var2 *= 1.0 - 0.5 * SkyGen.smooth(1.55, 1.45, var0);
      return SkyGen.clamp01(var2 * 1.4);
   }

   static double sphereHit(double var0, double var2, double var4, double[] var6, double[] var7, double var8) {
      double var10 = var7[0] - var0;
      double var12 = var7[1] - var2;
      double var14 = var7[2] - var4;
      double var16 = var10 * var6[0] + var12 * var6[1] + var14 * var6[2];
      if (var16 < 0.0) {
         return 0.0;
      } else {
         double var18 = var10 * var10 + var12 * var12 + var14 * var14 - var16 * var16;
         return SkyGen.smooth(var8 * var8 * 1.03, var8 * var8 * 0.97, var18);
      }
   }

   static SkyGen.Sky spiral() {
      final double[] var0 = SkyGen.azel(20.0, 28.0);
      final double[][] var1 = SkyGen.frame(var0, 35.0);
      final double var2 = Math.cos(Math.toRadians(58.0));
      final double[] var4 = SkyGen.rgb(16770748);
      final double[] var5 = SkyGen.rgb(11060479);
      final double[] var6 = SkyGen.rgb(16738992);
      final double[] var7 = SkyGen.rgb(1707528);
      final double[] var8 = SkyGen.rgb(131848);
      final double[] var9 = SkyGen.azel(210.0, -30.0);
      final double[][] var10 = SkyGen.frame(var9, -20.0);
      final double[] var11 = SkyGen.rgb(3808874);
      SkyGen3.Space var12 = new SkyGen3.Space() {
         @Override
         void sky(double var1x, double var3, double var5x, SkyGen.C var7x) {
            SkyGen.set(var7x, var8);
            SkyGen.add(var7x, var11, 0.12 * SkyGen.smooth(0.1, 0.8, SkyGen.fbmL(var1x * 2.0 + 1.0, var3 * 2.0, var5x * 2.0 + 5.0, 5.0)));
            this.galaxy(var7x, var1x, var3, var5x, var0, var1, 0.95, var2, 1.0, 0.0);
            this.galaxy(var7x, var1x, var3, var5x, var9, var10, 0.16, Math.cos(Math.toRadians(70.0)), 0.6, 3.0);
         }

         void galaxy(
            SkyGen.C var1x,
            double var2x,
            double var4x,
            double var6x,
            double[] var8x,
            double[][] var9x,
            double var10x,
            double var12x,
            double var14,
            double var16
         ) {
            double var18 = var2x * var8x[0] + var4x * var8x[1] + var6x * var8x[2];
            if (!(var18 < Math.cos(var10x * 1.6))) {
               double var20 = var2x * var9x[0][0] + var4x * var9x[0][1] + var6x * var9x[0][2];
               double var22 = var2x * var9x[1][0] + var4x * var9x[1][1] + var6x * var9x[1][2];
               double var24 = var20 / var10x;
               double var26 = var22 / (var10x * var12x);
               double var28 = Math.sqrt(var24 * var24 + var26 * var26) + 1.0E-9;
               if (!(var28 > 1.6)) {
                  double var30 = Math.atan2(var26, var24);
                  double var32 = SkyGen.fbmL(var24 * 3.0 + var16, var26 * 3.0, 1.7 + var16, 4.0);
                  double var34 = var30 - Math.log(var28) / 0.32 + var32 * 0.9;
                  double var36 = Math.pow(0.5 + 0.5 * Math.cos(2.0 * var34), 2.5);
                  double var38 = Math.pow(0.5 + 0.5 * Math.cos(2.0 * (var34 + 0.45)), 6.0);
                  double var40 = Math.exp(-var28 / 0.3) * SkyGen.smooth(1.4, 0.6, var28);
                  double var42 = Math.exp(-(var28 * var28) / 0.006) * 3.0 + Math.exp(-var28 / 0.06) * 1.0;
                  double var44 = 0.75 + 0.5 * SkyGen.fbmL(var24 * 25.0 + var16, var26 * 25.0, 3.3, 3.0);
                  double var46 = var40 * (0.25 + 1.4 * var36 * var44);
                  double var48 = SkyGen.smooth(0.25, 0.55, SkyGen.fbmL(var24 * 14.0, var26 * 14.0 + var16, 7.1, 3.0)) * var36 * var40 * 2.0;
                  double var50 = var38 * SkyGen.smooth(0.05, 0.25, var28) * SkyGen.smooth(1.1, 0.4, var28) * 0.75;
                  double var52 = 0.45 * var14;
                  var1x.r = var1x.r + var52 * (var5[0] * var46 + var4[0] * var42 + var6[0] * var48 * 0.5);
                  var1x.g = var1x.g + var52 * (var5[1] * var46 + var4[1] * var42 + var6[1] * var48 * 0.5);
                  var1x.b = var1x.b + var52 * (var5[2] * var46 + var4[2] * var42 + var6[2] * var48 * 0.5);
                  double var54 = 1.0 - var50;
                  var1x.r = var1x.r * var54 + var7[0] * var50 * 0.2;
                  var1x.g = var1x.g * var54 + var7[1] * var50 * 0.2;
                  var1x.b = var1x.b * var54 + var7[2] * var50 * 0.2;
                  var1x.vis = var1x.vis * (1.0 - 0.6 * SkyGen.smooth(0.0, 0.6, var40 + var42 * 0.3));
               }
            }
         }
      };
      var12.stars = 70000;
      return var12;
   }

   static SkyGen.Sky blackHole() {
      final double[] var0 = SkyGen.azel(0.0, 18.0);
      final double[][] var1 = SkyGen.frame(var0, -8.0);
      final double var2 = Math.cos(Math.toRadians(76.0));
      final double[] var4 = SkyGen.norm(0.1, 0.5, 0.86);
      final double[] var5 = SkyGen.rgb(2763386);
      final double[] var6 = SkyGen.rgb(6957658);
      final double[] var7 = SkyGen.rgb(65798);
      final double[] var8 = SkyGen.rgb(16774368);
      final double[] var9 = SkyGen.rgb(16752704);
      final double[] var10 = SkyGen.rgb(12638463);
      SkyGen3.Space var11 = new SkyGen3.Space() {
         void background(double var1x, double var3, double var5x, SkyGen.C var7x) {
            SkyGen.set(var7x, var7);
            SkyGen3.milky(var7x, var1x, var3, var5x, var4, SkyGen3.coreOn(var4, var0), 0.04);
            SkyGen.add(var7x, var5, 0.15 * SkyGen.smooth(0.0, 0.8, SkyGen.fbmL(var1x * 2.2 + 5.0, var3 * 2.2, var5x * 2.2, 5.0)));
            SkyGen.add(var7x, var6, 0.1 * SkyGen.smooth(0.2, 0.9, SkyGen.fbmL(var1x * 3.1, var3 * 3.1 + 5.0, var5x * 3.1, 5.0)));
         }

         @Override
         void sky(double var1x, double var3, double var5x, SkyGen.C var7x) {
            double var8x = var1x * var0[0] + var3 * var0[1] + var5x * var0[2];
            double var10x = var1x * var1[0][0] + var3 * var1[0][1] + var5x * var1[0][2];
            double var12 = var1x * var1[1][0] + var3 * var1[1][1] + var5x * var1[1][2];
            double var14 = Math.acos(Math.max(-1.0, Math.min(1.0, var8x)));
            double var16 = Math.sqrt(var10x * var10x + var12 * var12) + 1.0E-12;
            double var18 = var10x / var16;
            double var20 = var12 / var16;
            double var22 = var14 / 0.1;
            if (var22 > 1.0) {
               double var24 = var14 - 0.0361 / var14;
               double var26 = Math.sin(var24);
               double var28 = Math.cos(var24);
               double var30 = var0[0] * var28 + (var1[0][0] * var18 + var1[1][0] * var20) * var26;
               double var32 = var0[1] * var28 + (var1[0][1] * var18 + var1[1][1] * var20) * var26;
               double var34 = var0[2] * var28 + (var1[0][2] * var18 + var1[1][2] * var20) * var26;
               this.background(var30, var32, var34, var7x);
            } else {
               var7x.r = var7x.g = var7x.b = 0.0;
            }

            var7x.vis = var7x.vis * SkyGen.smooth(1.6, 3.0, var22);
            double var81 = SkyGen.smooth(1.03, 0.99, var22);
            SkyGen.mul(var7x, 1.0 - var81);
            double var82 = (var22 - 1.06) / 0.035;
            double var83 = Math.exp(-var82 * var82);
            double var84 = (var22 - 1.3) / (0.16 + 0.22 * Math.abs(var20));
            double var85 = Math.exp(-var84 * var84) * (0.35 + 0.65 * var20 * var20) * SkyGen.smooth(1.02, 1.12, var22);
            double var86 = 1.0 + 0.4 * var18;
            double var36 = 0.6 + 0.4 * SkyGen.fbmL(var18 * 4.0, var20 * 4.0, var22 * 6.0, 3.0);
            double var38 = 1.5 * var85 * var36 * var86 * var86 + 2.2 * var83 * var86;
            double var40 = var8[0] * 0.6 + var9[0] * 0.4;
            double var42 = var8[1] * 0.6 + var9[1] * 0.4;
            double var44 = var8[2] * 0.6 + var9[2] * 0.4;
            var7x.r += var40 * var38;
            var7x.g += var42 * var38;
            var7x.b += var44 * var38;
            double var46 = var10x / 0.1;
            double var48 = var12 / (0.1 * var2);
            double var50 = Math.sqrt(var46 * var46 + var48 * var48);
            boolean var52 = var12 < 0.0;
            if (var50 > 2.2 && var50 < 7.5 && (var52 || var22 > 1.02)) {
               double var53 = Math.atan2(var48, var46);
               double var55 = var53 + 3.0 / Math.sqrt(var50);
               double var57 = 0.45
                  + 0.55 * SkyGen.smooth(-0.4, 0.5, SkyGen.fbmL(Math.cos(var55) * var50 * 0.9, Math.sin(var55) * var50 * 0.9, var50 * 0.4, 5.0));
               double var59 = 0.75 + 0.25 * SkyGen.noise(var50 * 3.5, 0.3, 0.7);
               double var61 = Math.pow(2.6 / var50, 2.0) * SkyGen.smooth(2.2, 2.9, var50) * SkyGen.smooth(7.5, 4.5, var50);
               double var63 = 1.0 + 0.4 * (var46 / var50);
               double var65 = var61 * var57 * var59 * var63 * var63 * var63 * 1.6;
               double var67 = SkyGen.smooth(2.5, 7.5, var50);
               double var69 = SkyGen.mix(var8[0], var9[0], var67);
               double var71 = SkyGen.mix(var8[1], var9[1], var67);
               double var73 = SkyGen.mix(var8[2], var9[2], var67);
               double var75 = SkyGen.clamp01((var63 - 1.0) * 0.8);
               var69 = SkyGen.mix(var69, var10[0], var75 * 0.5);
               var71 = SkyGen.mix(var71, var10[1], var75 * 0.5);
               var73 = SkyGen.mix(var73, var10[2], var75 * 0.5);
               double var77 = SkyGen.smooth(1.0, 0.0, Math.abs(var48) / Math.max(1.0E-6, var50) * 0.0);
               double var79 = SkyGen.clamp01(var65 * 1.4) * var77;
               var7x.r = var7x.r * (1.0 - var79 * 0.5) + var69 * var65;
               var7x.g = var7x.g * (1.0 - var79 * 0.5) + var71 * var65;
               var7x.b = var7x.b * (1.0 - var79 * 0.5) + var73 * var65;
               var7x.vis *= 1.0 - var79;
            }

            SkyGen.add(var7x, var9, 0.025 * Math.exp(-Math.max(0.0, var22 - 1.0) / 1.2) * SkyGen.smooth(0.97, 1.05, var22));
         }

         @Override
         double starDensity(double var1x, double var3, double var5x) {
            double var7x = var1x * var0[0] + var3 * var0[1] + var5x * var0[2];
            double var9x = Math.acos(Math.max(-1.0, Math.min(1.0, var7x)));
            return (0.3 + 0.5 * SkyGen3.milkyDensity(var1x, var3, var5x, var4)) * SkyGen.smooth(0.15000000000000002, 0.30000000000000004, var9x);
         }
      };
      var11.stars = 80000;
      var11.detail = 0.85;
      return var11;
   }

   static SkyGen.Sky voidSky() {
      final double[] var0 = SkyGen.rgb(393740);
      final double[] var1 = SkyGen.rgb(5906058);
      final double[] var2 = SkyGen.rgb(12603647);
      final double[] var3 = SkyGen.rgb(1706554);
      final double[] var4 = SkyGen.rgb(14725375);
      SkyGen3.Space var5 = new SkyGen3.Space() {
         @Override
         void sky(double var1x, double var3x, double var5x, SkyGen.C var7) {
            SkyGen.set(var7, var0);
            double var8 = SkyGen.fbmL(var1x * 1.5 + 2.0, var3x * 1.5, var5x * 1.5, 3.0);
            double var10 = SkyGen.fbmL(var1x * 1.5, var3x * 1.5 + 6.0, var5x * 1.5, 3.0);
            double var12 = SkyGen.fbmL(var1x * 1.5, var3x * 1.5, var5x * 1.5 + 4.0, 3.0);
            double var14 = var1x + var8 * 0.9;
            double var16 = var3x + var10 * 0.9;
            double var18 = var5x + var12 * 0.9;
            double var20 = SkyGen.fbmL(var14 * 2.5, var16 * 2.5, var18 * 2.5, 6.0);
            double var22 = SkyGen.ridged(var14 * 3.5 + 1.0, var16 * 3.5, var18 * 3.5, 5);
            double var24 = SkyGen.smooth(0.0, 0.6, var20);
            double var26 = SkyGen.smooth(0.6, 0.95, var22) * var24;
            SkyGen.add(var7, var3, 0.5 * var24);
            SkyGen.add(var7, var1, 0.35 * var24 * var24 + 0.25 * var26);
            SkyGen.add(var7, var2, 0.5 * var26 * var26);
            double var28 = SkyGen.smooth(0.2, -0.9, var3x);
            SkyGen.add(var7, var1, 0.07 * var28);
            double[] var30 = SkyGen3.EYE;
            double var31 = var1x * var30[0] + var3x * var30[1] + var5x * var30[2];
            double var33 = 2.0 * (1.0 - var31);
            SkyGen.add(var7, var4, 0.9 * Math.exp(-var33 / 0.0015) + 0.1 * Math.exp(-var33 / 0.03));
         }

         @Override
         void moteColor(Random var1x, double[] var2x) {
            double var3x = var1x.nextDouble();
            if (var3x < 0.7) {
               var2x[0] = 0.9;
               var2x[1] = 0.5;
               var2x[2] = 1.3;
            } else {
               var2x[0] = 0.6;
               var2x[1] = 0.9;
               var2x[2] = 1.2;
            }
         }

         @Override
         double moteDensity(double var1x, double var3x, double var5x) {
            return 1.0;
         }
      };
      var5.stars = 30000;
      var5.starMax = 1.6;
      var5.motes = 700;
      var5.moteBright = 0.35;
      var5.moteSize = 1.2;
      return var5;
   }

   static double[] coreOn(double[] var0, double[] var1) {
      double var2 = var1[0] * var0[0] + var1[1] * var0[1] + var1[2] * var0[2];
      return SkyGen.norm(var1[0] - var0[0] * var2, var1[1] - var0[1] * var2, var1[2] - var0[2] * var2);
   }

   static final class Aurora {
      double[] low = SkyGen.rgb(3997578);
      double[] high = SkyGen.rgb(11549850);
      double bright = 1.0;
      double scale = 0.35;
      double width = 0.04;
      double seed = 0.0;
      double h0 = 1.0;
      double h1 = 3.2;
      double rays = 1.0;
      double cover = 0.2;
      double north = 1.0;
      int steps = 36;
   }

   static final class Body {
      final double[] d;
      final double[] ex;
      final double[] ey;
      final double rad;
      final double sinR;
      final double cosOut;
      double glow = 0.0;
      double glowW = 2.5;
      double albedo = 0.6;
      double seed = 0.0;
      double earthshine = 0.015;
      double[] grad = null;

      Body(double[] var1, double var2, double var4) {
         this.d = var1;
         this.rad = var2;
         this.sinR = Math.sin(var2);
         double[][] var6 = SkyGen.frame(var1, var4);
         this.ex = var6[0];
         this.ey = var6[1];
         this.cosOut = Math.cos(Math.min(Math.PI, var2 * 14.0));
      }
   }

   static class Night extends SkyGen.Sky {
      double[] zen = SkyGen.rgb(198158);
      double[] hor = SkyGen.rgb(1055276);
      double hW = 0.25;
      double[] air = null;
      double airK = 0.0;
      double airW = 0.08;
      double[] lp = null;
      double[] lpDir = null;
      double lpK = 0.0;
      double[] mwN = null;
      double[] mwCore = null;
      double mwK = 0.05;
      SkyGen3.Body moon;
      double[] moonTint = SkyGen.rgb(16777215);
      double[] moonLight = null;
      double moonBright = 1.0;
      double halo22 = 0.0;
      SkyGen3.Aurora au;
      SkyGen.Clouds c1;
      double[] cloudLight = SkyGen.rgb(8425648);
      double[] mist = null;
      double mistW = 0.04;
      double mistK = 0.0;
      double starDens = 1.0;

      Night() {
         this.kind = "night";
         this.stars = 60000;
         this.starMin = 0.035;
         this.starMax = 3.5;
      }

      @Override
      void sky(double var1, double var3, double var5, SkyGen.C var7) {
         double var8 = var3 > 0.0 ? var3 : 0.0;
         double var10 = Math.exp(-var8 / this.hW);
         var7.r = this.zen[0] + (this.hor[0] - this.zen[0]) * var10;
         var7.g = this.zen[1] + (this.hor[1] - this.zen[1]) * var10;
         var7.b = this.zen[2] + (this.hor[2] - this.zen[2]) * var10;
         if (this.air != null) {
            SkyGen.add(var7, this.air, this.airK * Math.exp(-var8 / this.airW));
         }

         if (this.lp != null) {
            double var12 = Math.sqrt(var1 * var1 + var5 * var5) + 1.0E-9;
            double var14 = (var1 * this.lpDir[0] + var5 * this.lpDir[2]) / var12;
            var14 = Math.max(0.0, var14);
            SkyGen.add(var7, this.lp, this.lpK * var14 * var14 * var14 * Math.exp(-var8 / 0.1));
         }

         if (this.mwN != null) {
            SkyGen3.milky(var7, var1, var3, var5, this.mwN, this.mwCore, this.mwK);
         }

         if (this.au != null) {
            SkyGen3.aurora(var7, var1, var3, var5, this.au);
         }

         this.extra(var1, var3, var5, var7);
         if (this.moon != null) {
            if (this.halo22 > 0.0) {
               double var20 = var1 * this.moon.d[0] + var3 * this.moon.d[1] + var5 * this.moon.d[2];
               double var22 = Math.acos(Math.max(-1.0, Math.min(1.0, var20)));
               double var16 = (var22 - 0.384) / 0.016;
               double var18 = Math.exp(-var16 * var16) * this.halo22
                  + Math.exp(-Math.max(0.0, var22 - 0.384) / 0.12) * this.halo22 * 0.25 * SkyGen.smooth(0.36, 0.4, var22);
               var7.r = var7.r + var18 * (1.1 - 0.25 * SkyGen.smooth(-1.0, 1.0, var16));
               var7.g += var18;
               var7.b = var7.b + var18 * (0.85 + 0.35 * SkyGen.smooth(-1.0, 1.0, var16));
            }

            SkyGen3.moon(var7, var1, var3, var5, this.moon, this.moonTint, this.moonBright, this.moonLight);
         }

         if (this.c1 != null) {
            SkyGen.clouds(var7, var1, var3, var5, this.c1, this.moon != null ? this.moon.d : this.sun, this.cloudLight);
         }

         if (this.mist != null) {
            SkyGen.mixTo(var7, this.mist, this.mistK * Math.exp(-var8 / this.mistW));
         }

         var7.vis = var7.vis * SkyGen.smooth(-0.01, 0.2, var3);
      }

      void extra(double var1, double var3, double var5, SkyGen.C var7) {
      }

      @Override
      double starDensity(double var1, double var3, double var5) {
         double var7 = SkyGen.smooth(-0.02, 0.3, var3);
         return this.mwN == null
            ? var7 * 0.35 * this.starDens
            : var7 * Math.min(1.0, (0.25 + 0.75 * SkyGen3.milkyDensity(var1, var3, var5, this.mwN)) * this.starDens);
      }
   }

   abstract static class Space extends SkyGen.Sky {
      Space() {
         this.kind = "space";
         this.below = 0;
         this.stars = 70000;
         this.starMin = 0.04;
         this.starMax = 4.0;
      }

      @Override
      double starDensity(double var1, double var3, double var5) {
         return 0.4;
      }
   }
}
