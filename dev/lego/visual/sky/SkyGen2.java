package dev.lego.visual.sky;

final class SkyGen2 {
   private SkyGen2() {
   }

   static SkyGen.Sky make(String var0) {
      switch (var0) {
         case "summer":
            return summer();
         case "sunset":
            return sunset();
         case "goldenhour":
            return goldenHour();
         case "cottoncandy":
            return cottonCandy();
         case "sunrise":
            return sunrise();
         case "overcast":
            return overcast();
         case "thunder":
            return thunder();
         case "anime":
            return anime();
         case "alien":
            return alien();
         case "synthwave":
            return synthwave();
         case "oceandusk":
            return oceanDusk();
         default:
            return null;
      }
   }

   static SkyGen.Sky summer() {
      SkyGen2.Day var0 = new SkyGen2.Day();
      var0.kind = "day";
      var0.sun = SkyGen.azel(150.0, 58.0);
      var0.sunCol = SkyGen.rgb(16774884);
      var0.zen = SkyGen.rgb(2449604);
      var0.hor = SkyGen.rgb(13229810);
      var0.hW = 0.22;
      var0.hW2 = 0.55;
      var0.disk = 45.0;
      var0.b1 = 0.25;
      var0.w1 = 0.16;
      var0.b2 = 1.3;
      var0.w2 = 0.03;
      SkyGen.Clouds var1 = new SkyGen.Clouds();
      var1.height = 1.0;
      var1.scale = 0.75;
      var1.cov = 0.1;
      var1.soft = 0.2;
      var1.warp = 0.9;
      var1.seed = 2.3;
      var1.fog = 14.0;
      var1.lit = mulc(SkyGen.rgb(16777215), 1.15);
      var1.shade = SkyGen.rgb(8228006);
      var1.thickDark = 0.5;
      var0.c1 = var1;
      SkyGen.Clouds var2 = new SkyGen.Clouds();
      var2.height = 3.0;
      var2.scale = 0.35;
      var2.stretch = 3.5;
      var2.cov = 0.18;
      var2.soft = 0.35;
      var2.warp = 1.4;
      var2.seed = 8.1;
      var2.oct = 5.0;
      var2.lit = SkyGen.rgb(16054527);
      var2.shade = SkyGen.rgb(12964840);
      var2.opacity = 0.55;
      var2.thickDark = 0.1;
      var2.fog = 30.0;
      var0.c2 = var2;
      SkyGen.Towers var3 = new SkyGen.Towers();
      var3.h0 = 0.01;
      var3.h1 = 0.1;
      var3.freq = 2.6;
      var3.cut = 0.05;
      var3.bill = 9.0;
      var3.seed = 4.2;
      var3.lit = mulc(SkyGen.rgb(16777215), 1.1);
      var3.shade = SkyGen.rgb(8623280);
      var0.tw = var3;
      var0.mist = SkyGen.rgb(14083314);
      var0.mistK = 0.35;
      var0.mistW = 0.03;
      var0.ground = SkyGen.rgb(4149600);
      var0.groundFar = SkyGen.rgb(11124176);
      var0.groundFall = 0.25;
      return var0;
   }

   static SkyGen.Sky sunset() {
      SkyGen2.Day var0 = new SkyGen2.Day();
      var0.kind = "sunset";
      var0.sun = SkyGen.azel(0.0, 2.2);
      var0.sunCol = SkyGen.rgb(16766880);
      var0.zen = SkyGen.rgb(2502766);
      var0.mid = SkyGen.rgb(9067162);
      var0.midW = 0.35;
      var0.hor = SkyGen.rgb(14841962);
      var0.horSun = SkyGen.rgb(16751164);
      var0.horAnti = SkyGen.rgb(11562122);
      var0.hW = 0.1;
      var0.glow = SkyGen.rgb(16756832);
      var0.glowK = 1.4;
      var0.glowPow = 5.0;
      var0.glowH = 0.09;
      var0.belt = SkyGen.rgb(14912160);
      var0.beltK = 0.25;
      var0.shadow = SkyGen.rgb(3817322);
      var0.shadowK = 0.5;
      var0.sunR = 0.014;
      var0.disk = 22.0;
      var0.b1 = 0.9;
      var0.w1 = 0.1;
      var0.b2 = 2.5;
      var0.w2 = 0.035;
      SkyGen.Clouds var1 = new SkyGen.Clouds();
      var1.height = 1.3;
      var1.scale = 0.9;
      var1.cov = 0.05;
      var1.soft = 0.28;
      var1.warp = 1.1;
      var1.seed = 5.7;
      var1.fog = 10.0;
      var1.stretch = 1.8;
      var1.lit = SkyGen.rgb(16753770);
      var1.shade = SkyGen.rgb(5978728);
      var1.thickDark = 0.6;
      var1.silver = 2.5;
      var1.lightSide = 1.3;
      var0.c1 = var1;
      SkyGen.Clouds var2 = new SkyGen.Clouds();
      var2.height = 3.0;
      var2.scale = 0.3;
      var2.stretch = 4.0;
      var2.cov = 0.15;
      var2.soft = 0.35;
      var2.seed = 1.9;
      var2.oct = 5.0;
      var2.warp = 1.5;
      var2.lit = SkyGen.rgb(16758416);
      var2.shade = SkyGen.rgb(11561610);
      var2.opacity = 0.7;
      var2.thickDark = 0.1;
      var2.fog = 25.0;
      var0.c2 = var2;
      var0.below = 2;
      var0.water = SkyGen.rgb(1182756);
      var0.waves = 1.0;
      var0.glitter = 2.2;
      var0.rough = 0.09;
      return var0;
   }

   static SkyGen.Sky goldenHour() {
      SkyGen2.Day var0 = new SkyGen2.Day();
      var0.kind = "sunset";
      var0.sun = SkyGen.azel(250.0, 7.0);
      var0.sunCol = SkyGen.rgb(16769704);
      var0.zen = SkyGen.rgb(3498408);
      var0.hor = SkyGen.rgb(15324862);
      var0.horSun = SkyGen.rgb(16763774);
      var0.horAnti = SkyGen.rgb(12175066);
      var0.hW = 0.2;
      var0.hW2 = 0.5;
      var0.glow = SkyGen.rgb(16760944);
      var0.glowK = 0.9;
      var0.glowPow = 4.0;
      var0.glowH = 0.18;
      var0.sunR = 0.011;
      var0.disk = 30.0;
      var0.b1 = 0.55;
      var0.w1 = 0.14;
      var0.b2 = 2.0;
      var0.w2 = 0.035;
      SkyGen.Clouds var1 = new SkyGen.Clouds();
      var1.height = 1.0;
      var1.scale = 0.8;
      var1.cov = 0.14;
      var1.soft = 0.22;
      var1.warp = 1.0;
      var1.seed = 3.3;
      var1.fog = 12.0;
      var1.lit = mulc(SkyGen.rgb(16766106), 1.25);
      var1.shade = SkyGen.rgb(7236234);
      var1.thickDark = 0.55;
      var1.silver = 2.0;
      var1.lightSide = 1.4;
      var0.c1 = var1;
      SkyGen.Clouds var2 = new SkyGen.Clouds();
      var2.height = 3.5;
      var2.scale = 0.28;
      var2.stretch = 5.0;
      var2.cov = 0.12;
      var2.soft = 0.4;
      var2.seed = 6.1;
      var2.oct = 5.0;
      var2.warp = 1.6;
      var2.lit = SkyGen.rgb(16769200);
      var2.shade = SkyGen.rgb(13218996);
      var2.opacity = 0.6;
      var2.thickDark = 0.1;
      var2.fog = 28.0;
      var0.c2 = var2;
      var0.below = 3;
      SkyGen.Clouds var3 = new SkyGen.Clouds();
      var3.height = 1.0;
      var3.scale = 0.9;
      var3.cov = -0.3;
      var3.soft = 0.3;
      var3.warp = 1.2;
      var3.seed = 9.4;
      var3.fog = 0.06;
      var3.lit = mulc(SkyGen.rgb(16769976), 1.05);
      var3.shade = SkyGen.rgb(9340576);
      var0.sea = var3;
      var0.seaGap = SkyGen.rgb(5923458);
      return var0;
   }

   static SkyGen.Sky cottonCandy() {
      SkyGen2.Day var0 = new SkyGen2.Day();
      var0.kind = "sunset";
      var0.sun = SkyGen.azel(290.0, -1.5);
      var0.sunCol = SkyGen.rgb(16763056);
      var0.zen = SkyGen.rgb(5136304);
      var0.mid = SkyGen.rgb(10980048);
      var0.midW = 0.45;
      var0.hor = SkyGen.rgb(16168648);
      var0.horSun = SkyGen.rgb(16762016);
      var0.horAnti = SkyGen.rgb(13805272);
      var0.hW = 0.16;
      var0.glow = SkyGen.rgb(16756896);
      var0.glowK = 0.8;
      var0.glowPow = 4.0;
      var0.glowH = 0.12;
      var0.belt = SkyGen.rgb(16752320);
      var0.beltK = 0.35;
      var0.disk = 0.0;
      var0.b1 = 0.5;
      var0.w1 = 0.2;
      var0.b2 = 0.6;
      var0.w2 = 0.06;
      SkyGen.Clouds var1 = new SkyGen.Clouds();
      var1.height = 1.1;
      var1.scale = 0.7;
      var1.cov = 0.08;
      var1.soft = 0.26;
      var1.warp = 1.0;
      var1.seed = 7.2;
      var1.fog = 12.0;
      var1.lit = mulc(SkyGen.rgb(16758226), 1.2);
      var1.shade = SkyGen.rgb(9206978);
      var1.thickDark = 0.45;
      var1.silver = 1.5;
      var1.lightSide = 1.2;
      var0.c1 = var1;
      SkyGen.Clouds var2 = new SkyGen.Clouds();
      var2.height = 3.0;
      var2.scale = 0.3;
      var2.stretch = 3.0;
      var2.cov = 0.1;
      var2.soft = 0.4;
      var2.seed = 2.6;
      var2.oct = 5.0;
      var2.warp = 1.5;
      var2.lit = SkyGen.rgb(16765152);
      var2.shade = SkyGen.rgb(13018844);
      var2.opacity = 0.65;
      var2.thickDark = 0.1;
      var2.fog = 25.0;
      var0.c2 = var2;
      var0.below = 2;
      var0.water = SkyGen.rgb(1972280);
      var0.waves = 0.7;
      var0.glitter = 0.6;
      var0.rough = 0.12;
      return var0;
   }

   static SkyGen.Sky sunrise() {
      SkyGen2.Day var0 = new SkyGen2.Day();
      var0.kind = "sunset";
      var0.sun = SkyGen.azel(90.0, 3.5);
      var0.sunCol = SkyGen.rgb(16770750);
      var0.zen = SkyGen.rgb(5931712);
      var0.hor = SkyGen.rgb(15128780);
      var0.horSun = SkyGen.rgb(16767138);
      var0.horAnti = SkyGen.rgb(11517138);
      var0.hW = 0.2;
      var0.hW2 = 0.45;
      var0.glow = SkyGen.rgb(16766106);
      var0.glowK = 1.0;
      var0.glowPow = 4.0;
      var0.glowH = 0.15;
      var0.sunR = 0.011;
      var0.disk = 26.0;
      var0.b1 = 0.8;
      var0.w1 = 0.16;
      var0.b2 = 2.2;
      var0.w2 = 0.045;
      SkyGen.Clouds var1 = new SkyGen.Clouds();
      var1.height = 3.0;
      var1.scale = 0.3;
      var1.stretch = 4.0;
      var1.cov = 0.14;
      var1.soft = 0.4;
      var1.seed = 4.4;
      var1.oct = 5.0;
      var1.warp = 1.6;
      var1.lit = SkyGen.rgb(16769732);
      var1.shade = SkyGen.rgb(12630216);
      var1.opacity = 0.6;
      var1.thickDark = 0.1;
      var1.fog = 28.0;
      var0.c2 = var1;
      var0.mist = SkyGen.rgb(15918296);
      var0.mistK = 0.5;
      var0.mistW = 0.035;
      var0.below = 3;
      SkyGen.Clouds var2 = new SkyGen.Clouds();
      var2.height = 1.0;
      var2.scale = 0.45;
      var2.cov = -0.3;
      var2.soft = 0.55;
      var2.warp = 1.4;
      var2.seed = 2.2;
      var2.fog = 0.09;
      var2.oct = 5.0;
      var2.lit = SkyGen.rgb(16770764);
      var2.shade = SkyGen.rgb(11117750);
      var0.sea = var2;
      var0.seaGap = SkyGen.rgb(7238792);
      return var0;
   }

   static SkyGen.Sky overcast() {
      SkyGen2.Day var0 = new SkyGen2.Day();
      var0.kind = "day";
      var0.sun = SkyGen.azel(200.0, 35.0);
      var0.zen = SkyGen.rgb(8292241);
      var0.hor = SkyGen.rgb(11120566);
      var0.hW = 0.3;
      var0.disk = 0.0;
      var0.b1 = 0.0;
      var0.b2 = 0.0;
      SkyGen.Clouds var1 = new SkyGen.Clouds();
      var1.height = 1.0;
      var1.scale = 0.55;
      var1.cov = -0.45;
      var1.soft = 0.55;
      var1.warp = 1.2;
      var1.seed = 1.1;
      var1.fog = 9.0;
      var1.oct = 6.0;
      var1.lit = SkyGen.rgb(12173255);
      var1.shade = SkyGen.rgb(5726058);
      var1.thickDark = 0.6;
      var1.silver = 0.2;
      var1.lightSide = 0.8;
      var0.c2 = var1;
      SkyGen.Clouds var2 = new SkyGen.Clouds();
      var2.height = 0.5;
      var2.scale = 0.9;
      var2.cov = 0.12;
      var2.soft = 0.3;
      var2.warp = 1.4;
      var2.seed = 6.6;
      var2.fog = 7.0;
      var2.lit = SkyGen.rgb(9147035);
      var2.shade = SkyGen.rgb(4607061);
      var2.thickDark = 0.5;
      var2.opacity = 0.8;
      var2.silver = 0.0;
      var0.c1 = var2;
      var0.post = SkyGen.rgb(16775404);
      var0.postK = 0.12;
      var0.postW = 0.35;
      var0.mist = SkyGen.rgb(10265515);
      var0.mistK = 0.6;
      var0.mistW = 0.05;
      var0.ground = SkyGen.rgb(3291186);
      var0.groundFar = SkyGen.rgb(9344412);
      var0.groundFall = 0.22;
      return var0;
   }

   static SkyGen.Sky thunder() {
      SkyGen2.Day var0 = new SkyGen2.Day() {
         final double[] aLit = SkyGen2.mulc(SkyGen.rgb(16773852), 1.15);
         final double[] aShade = SkyGen.rgb(5922930);
         final double[] rain = SkyGen.rgb(4870494);
         final double[] under = SkyGen.rgb(3817552);

         @Override
         void extra(double var1, double var3, double var5, SkyGen.C var7) {
            this.anvil(var1, var3, var5, var7);
         }

         void anvil(double var1, double var3, double var5, SkyGen.C var7) {
            if (!(var3 > 0.8) && !(var3 < -0.02)) {
               double var8 = Math.atan2(var1, -var5);
               double var10 = var8 - Math.toRadians(55.0);
               var10 = Math.atan2(Math.sin(var10), Math.cos(var10));
               if (!(Math.abs(var10) > 1.5)) {
                  double var12 = Math.asin(Math.max(-1.0, Math.min(1.0, var3)));
                  double var14 = Math.cos(var10);
                  double var16 = Math.sin(var10);
                  double var18 = 0.55;
                  double var20 = SkyGen.smooth(var18 - 0.34, var18 - 0.03, var12);
                  double var22 = 0.32 * var20 * var20;
                  double var24 = 0.4 - 0.1 * SkyGen.smooth(0.0, 0.3, var12) + 0.55 * var20 * var20;
                  double var26 = var10 - var22;
                  double var28 = var26 / var24;
                  double var30 = var18 + 0.035 * (1.0 - var28 * var28) - 0.03 * Math.abs(var28) * Math.abs(var28);
                  double var32 = SkyGen.fbmL(var14 * 7.0, var12 * 8.0 + 1.3, var16 * 7.0, 5.0);
                  double var34 = SkyGen.fbmL(var14 * 3.0, var12 * 30.0 + 4.1, var16 * 3.0, 4.0);
                  double var36 = var32 * (1.0 - var20) + var34 * var20 * 0.6;
                  double var38 = var20 * SkyGen.smooth(0.55, 1.0, Math.abs(var28));
                  double var40 = Math.min(var24 - Math.abs(var26), (var30 - var12) * 1.6) + var36 * 0.09 - var38 * 0.03;
                  double var42 = SkyGen.smooth(0.0, 0.025, var40) * (1.0 - 0.6 * var38);
                  if (var12 < 0.12 && Math.abs(var10) < 0.6) {
                     double var44 = SkyGen.smooth(0.12, 0.02, var12)
                        * SkyGen.smooth(0.0, 0.015, var12)
                        * SkyGen.smooth(0.6, 0.25, Math.abs(var10))
                        * (0.55 + 0.45 * SkyGen.noise(var10 * 30.0, 0.5, var12 * 2.0));
                     SkyGen.mixTo(var7, this.rain, var44 * 0.75);
                  }

                  if (!(var42 <= 0.002)) {
                     double var63 = SkyGen.fbmL(var14 * 7.0 - 0.03, (var12 - 0.02) * 8.0 + 1.3, var16 * 7.0, 3.0);
                     double var46 = SkyGen.clamp01((var32 - var63) * 3.5 + 0.5);
                     double var48 = SkyGen.clamp01(var12 / var18);
                     double var50 = 0.2 + 0.8 * SkyGen.smooth(0.0, 0.85, var48) + (var46 - 0.5) * 0.6 * (1.0 - var20) + var32 * 0.35;
                     var50 = SkyGen.clamp01(var50 - 0.25 * SkyGen.smooth(0.25, 0.9, Math.abs(var28)) * var20);
                     double var52 = this.aShade[0] + (this.aLit[0] - this.aShade[0]) * var50;
                     double var54 = this.aShade[1] + (this.aLit[1] - this.aShade[1]) * var50;
                     double var56 = this.aShade[2] + (this.aLit[2] - this.aShade[2]) * var50;
                     double var58 = SkyGen.smooth(0.14, 0.0, var12) * 0.65;
                     var52 += (this.under[0] - var52) * var58;
                     var54 += (this.under[1] - var54) * var58;
                     var56 += (this.under[2] - var56) * var58;
                     double var60 = Math.exp(-Math.max(0.0, var12) / 0.03);
                     var52 += (var7.r - var52) * var60;
                     var54 += (var7.g - var54) * var60;
                     var56 += (var7.b - var56) * var60;
                     var7.r = var7.r + (var52 - var7.r) * var42;
                     var7.g = var7.g + (var54 - var7.g) * var42;
                     var7.b = var7.b + (var56 - var7.b) * var42;
                     var7.vis *= 1.0 - var42;
                  }
               }
            }
         }
      };
      var0.kind = "day";
      var0.sun = SkyGen.azel(235.0, 24.0);
      var0.sunCol = SkyGen.rgb(16772040);
      var0.zen = SkyGen.rgb(4087694);
      var0.hor = SkyGen.rgb(11845318);
      var0.horSun = SkyGen.rgb(15128252);
      var0.horAnti = SkyGen.rgb(9345704);
      var0.hW = 0.22;
      var0.disk = 30.0;
      var0.b1 = 0.3;
      var0.w1 = 0.15;
      var0.b2 = 1.4;
      var0.w2 = 0.035;
      SkyGen.Clouds var1 = new SkyGen.Clouds();
      var1.height = 0.8;
      var1.scale = 0.8;
      var1.cov = 0.14;
      var1.soft = 0.25;
      var1.warp = 1.2;
      var1.seed = 3.9;
      var1.fog = 10.0;
      var1.lit = SkyGen.rgb(13159636);
      var1.shade = SkyGen.rgb(4870236);
      var1.thickDark = 0.6;
      var0.c1 = var1;
      var0.mist = SkyGen.rgb(10134702);
      var0.mistK = 0.4;
      var0.mistW = 0.03;
      var0.ground = SkyGen.rgb(2501674);
      var0.groundFar = SkyGen.rgb(7239808);
      var0.groundFall = 0.2;
      return var0;
   }

   static SkyGen.Sky anime() {
      SkyGen2.Day var0 = new SkyGen2.Day();
      var0.kind = "day";
      var0.sun = SkyGen.azel(200.0, 42.0);
      var0.sunCol = SkyGen.rgb(16776170);
      var0.zen = SkyGen.rgb(1201624);
      var0.hor = SkyGen.rgb(10277631);
      var0.hW = 0.3;
      var0.hW2 = 0.6;
      var0.sunR = 0.012;
      var0.disk = 45.0;
      var0.b1 = 0.35;
      var0.w1 = 0.2;
      var0.b2 = 1.6;
      var0.w2 = 0.05;
      SkyGen.Clouds var1 = new SkyGen.Clouds();
      var1.height = 1.0;
      var1.scale = 0.55;
      var1.cov = 0.12;
      var1.soft = 0.09;
      var1.warp = 1.0;
      var1.seed = 12.5;
      var1.fog = 18.0;
      var1.oct = 5.0;
      var1.lit = mulc(SkyGen.rgb(16777215), 1.3);
      var1.shade = SkyGen.rgb(9348826);
      var1.thickDark = 0.35;
      var1.lightSide = 1.5;
      var1.silver = 1.2;
      var0.c1 = var1;
      SkyGen.Towers var2 = new SkyGen.Towers();
      var2.h0 = 0.02;
      var2.h1 = 0.36;
      var2.freq = 1.7;
      var2.cut = 0.12;
      var2.bill = 5.0;
      var2.puff = 1.4;
      var2.seed = 7.7;
      var2.soft = 0.008;
      var2.haze = 0.05;
      var2.lit = mulc(SkyGen.rgb(16777215), 1.25);
      var2.shade = SkyGen.rgb(8362708);
      var2.sunSide = 0.4;
      var0.tw = var2;
      var0.below = 3;
      SkyGen.Clouds var3 = new SkyGen.Clouds();
      var3.height = 1.0;
      var3.scale = 0.45;
      var3.cov = -0.05;
      var3.soft = 0.12;
      var3.warp = 1.1;
      var3.seed = 3.2;
      var3.fog = 0.07;
      var3.lit = mulc(SkyGen.rgb(16777215), 1.15);
      var3.shade = SkyGen.rgb(10795236);
      var0.sea = var3;
      var0.seaGap = SkyGen.rgb(4094152);
      return var0;
   }

   static SkyGen.Sky alien() {
      final double[] var0 = SkyGen.azel(145.0, 38.0);
      final double[] var1 = SkyGen.rgb(13625087);
      SkyGen2.Day var2 = new SkyGen2.Day() {
         @Override
         void extra(double var1x, double var3, double var5, SkyGen.C var7) {
            double var8 = var1x * var0[0] + var3 * var0[1] + var5 * var0[2];
            SkyGen.sunDisk(var7, var8, 0.006, var1, 60.0, 0.12, 0.08, 1.2, 0.02);
         }
      };
      var2.kind = "day";
      var2.sun = SkyGen.azel(215.0, 22.0);
      var2.sunCol = SkyGen.rgb(16763030);
      var2.zen = SkyGen.rgb(741214);
      var2.hor = SkyGen.rgb(9100232);
      var2.horSun = SkyGen.rgb(15261864);
      var2.horAnti = SkyGen.rgb(7127232);
      var2.hW = 0.25;
      var2.glow = SkyGen.rgb(16756848);
      var2.glowK = 0.35;
      var2.glowPow = 5.0;
      var2.glowH = 0.2;
      var2.sunR = 0.032;
      var2.disk = 14.0;
      var2.b1 = 0.5;
      var2.w1 = 0.2;
      var2.b2 = 1.0;
      var2.w2 = 0.08;
      var2.moon = new SkyGen3.Body(SkyGen.azel(20.0, 32.0), 0.13, 0.0);
      var2.moonTint = SkyGen.rgb(15257832);
      SkyGen.Clouds var3 = new SkyGen.Clouds();
      var3.height = 1.4;
      var3.scale = 0.6;
      var3.stretch = 4.0;
      var3.cov = 0.1;
      var3.soft = 0.3;
      var3.warp = 1.8;
      var3.seed = 21.1;
      var3.fog = 14.0;
      var3.lit = mulc(SkyGen.rgb(16175848), 1.1);
      var3.shade = SkyGen.rgb(4160646);
      var3.thickDark = 0.4;
      var3.silver = 1.5;
      var2.c1 = var3;
      var2.mist = SkyGen.rgb(11069656);
      var2.mistK = 0.35;
      var2.mistW = 0.04;
      var2.ground = SkyGen.rgb(1520182);
      var2.groundFar = SkyGen.rgb(6993064);
      var2.groundFall = 0.22;
      return var2;
   }

   static SkyGen.Sky synthwave() {
      SkyGen2.Day var0 = new SkyGen2.Day() {
         final double[] top = SkyGen.rgb(16770138);
         final double[] midc = SkyGen.rgb(16747068);
         final double[] bot = SkyGen.rgb(16723590);

         @Override
         void extra(double var1, double var3, double var5, SkyGen.C var7) {
            double var8 = -var5;
            double var12 = var1 * this.sun[0] + var3 * this.sun[1] + var5 * this.sun[2];
            double var10 = Math.acos(Math.max(-1.0, Math.min(1.0, var12)));
            double var14 = 0.21;
            double var16 = Math.exp(-Math.max(0.0, var10 - var14) / 0.1);
            SkyGen.add(var7, this.bot, var16 * 0.5);
            if (!(var10 > var14 * 1.02) && !(var8 <= 0.0)) {
               double var18 = (var3 - (this.sun[1] - var14)) / (2.0 * var14);
               double var20;
               double var22;
               double var24;
               if (var18 > 0.5) {
                  double var26 = (var18 - 0.5) * 2.0;
                  var20 = SkyGen.mix(this.midc[0], this.top[0], var26);
                  var22 = SkyGen.mix(this.midc[1], this.top[1], var26);
                  var24 = SkyGen.mix(this.midc[2], this.top[2], var26);
               } else {
                  double var36 = var18 * 2.0;
                  var20 = SkyGen.mix(this.bot[0], this.midc[0], var36);
                  var22 = SkyGen.mix(this.bot[1], this.midc[1], var36);
                  var24 = SkyGen.mix(this.bot[2], this.midc[2], var36);
               }

               double var37 = 1.0;
               if (var18 < 0.55) {
                  double var28 = 9.0;
                  double var30 = var18 * var28 % 1.0;
                  double var32 = (0.55 - var18) / 0.55 * 0.55;
                  double var34 = 0.06;
                  var37 = SkyGen.smooth(var32 - var34, var32 + var34, var30);
               }

               double var38 = SkyGen.smooth(var14 * 1.02, var14 * 0.985, var10) * var37;
               double var39 = 2.2;
               var7.r = var7.r + (var20 * var39 - var7.r) * var38;
               var7.g = var7.g + (var22 * var39 - var7.g) * var38;
               var7.b = var7.b + (var24 * var39 - var7.b) * var38;
               var7.vis *= 1.0 - var38;
            }
         }
      };
      var0.kind = "sunset";
      var0.sun = SkyGen.azel(0.0, 8.0);
      var0.sunCol = SkyGen.rgb(16738984);
      var0.zen = SkyGen.rgb(459548);
      var0.mid = SkyGen.rgb(3804770);
      var0.midW = 0.35;
      var0.hor = SkyGen.rgb(16727694);
      var0.horSun = SkyGen.rgb(16734842);
      var0.horAnti = SkyGen.rgb(11543678);
      var0.hW = 0.07;
      var0.glow = SkyGen.rgb(16732058);
      var0.glowK = 0.6;
      var0.glowPow = 3.0;
      var0.glowH = 0.2;
      var0.disk = 0.0;
      var0.b1 = 0.0;
      var0.b2 = 0.0;
      SkyGen.Clouds var1 = new SkyGen.Clouds();
      var1.height = 2.0;
      var1.scale = 0.35;
      var1.stretch = 6.0;
      var1.cov = 0.22;
      var1.soft = 0.25;
      var1.warp = 0.6;
      var1.seed = 4.8;
      var1.fog = 20.0;
      var1.oct = 4.0;
      var1.lit = SkyGen.rgb(16742080);
      var1.shade = SkyGen.rgb(5906046);
      var1.thickDark = 0.3;
      var1.silver = 0.0;
      var1.opacity = 0.85;
      var0.c1 = var1;
      var0.stars = 26000;
      var0.starMin = 0.05;
      var0.starMax = 2.5;
      var0.starSeed = 31L;
      var0.starDensityFn = (var0x, var2, var4) -> SkyGen.smooth(0.12, 0.55, var2);
      var0.below = 2;
      var0.water = SkyGen.rgb(787488);
      var0.waves = 0.25;
      var0.glitter = 0.0;
      var0.mirror = 1.0;
      return var0;
   }

   static SkyGen.Sky oceanDusk() {
      SkyGen2.Day var0 = new SkyGen2.Day() {
         @Override
         double starDensity(double var1, double var3, double var5) {
            return SkyGen.smooth(0.12, 0.6, var3) * 0.8;
         }
      };
      var0.kind = "sunset";
      var0.sun = SkyGen.azel(265.0, -5.0);
      var0.sunCol = SkyGen.rgb(16751216);
      var0.zen = SkyGen.rgb(727104);
      var0.mid = SkyGen.rgb(1849968);
      var0.midW = 0.4;
      var0.hor = SkyGen.rgb(4025496);
      var0.horSun = SkyGen.rgb(15764066);
      var0.horAnti = SkyGen.rgb(3035256);
      var0.hW = 0.09;
      var0.glow = SkyGen.rgb(16747098);
      var0.glowK = 0.7;
      var0.glowPow = 6.0;
      var0.glowH = 0.07;
      var0.belt = SkyGen.rgb(9071258);
      var0.beltK = 0.18;
      var0.disk = 0.0;
      var0.b1 = 0.3;
      var0.w1 = 0.12;
      var0.b2 = 0.0;
      var0.w2 = 0.05;
      SkyGen.Clouds var1 = new SkyGen.Clouds();
      var1.height = 3.0;
      var1.scale = 0.3;
      var1.stretch = 4.0;
      var1.cov = 0.14;
      var1.soft = 0.35;
      var1.seed = 9.9;
      var1.oct = 5.0;
      var1.warp = 1.6;
      var1.lit = SkyGen.rgb(16032416);
      var1.shade = SkyGen.rgb(3816554);
      var1.opacity = 0.7;
      var1.thickDark = 0.2;
      var1.fog = 25.0;
      var1.silver = 3.0;
      var1.lightSide = 1.6;
      var0.c2 = var1;
      var0.stars = 20000;
      var0.starMin = 0.04;
      var0.starMax = 2.0;
      var0.starSeed = 5L;
      var0.below = 2;
      var0.water = SkyGen.rgb(200732);
      var0.waves = 1.1;
      var0.glitter = 0.4;
      var0.rough = 0.1;
      return var0;
   }

   static double[] mulc(double[] var0, double var1) {
      return new double[]{var0[0] * var1, var0[1] * var1, var0[2] * var1};
   }

   static class Day extends SkyGen.Sky {
      double[] zen = SkyGen.rgb(2844616);
      double[] hor = SkyGen.rgb(12901104);
      double[] horSun = null;
      double[] horAnti = null;
      double hW = 0.26;
      double hW2 = 0.0;
      double[] mid = null;
      double midW = 0.5;
      double sunR = 0.0095;
      double disk = 40.0;
      double b1 = 0.22;
      double w1 = 0.14;
      double b2 = 1.4;
      double w2 = 0.03;
      double[] glow = null;
      double glowK = 0.0;
      double glowPow = 6.0;
      double glowH = 0.12;
      double[] belt = null;
      double[] shadow = null;
      double beltK = 0.0;
      double shadowK = 0.0;
      double[] mist = null;
      double mistW = 0.04;
      double mistK = 0.0;
      double[] post = null;
      double postK = 0.0;
      double postW = 0.25;
      SkyGen.Clouds c1;
      SkyGen.Clouds c2;
      SkyGen.Towers tw;
      SkyGen3.Body moon;
      double[] moonTint = SkyGen.rgb(16777215);

      @Override
      void sky(double var1, double var3, double var5, SkyGen.C var7) {
         double var8 = var3 > 0.0 ? var3 : 0.0;
         double var10 = Math.sqrt(var1 * var1 + var5 * var5) + 1.0E-9;
         double var12 = Math.sqrt(this.sun[0] * this.sun[0] + this.sun[2] * this.sun[2]) + 1.0E-9;
         double var14 = (var1 * this.sun[0] + var5 * this.sun[2]) / (var10 * var12);
         double var16 = 0.5 + 0.5 * var14;
         double var18 = this.hor[0];
         double var20 = this.hor[1];
         double var22 = this.hor[2];
         if (this.horSun != null) {
            double var24 = var16 * var16;
            double[] var26 = this.horAnti != null ? this.horAnti : this.hor;
            var18 = var26[0] + (this.horSun[0] - var26[0]) * var24;
            var20 = var26[1] + (this.horSun[1] - var26[1]) * var24;
            var22 = var26[2] + (this.horSun[2] - var26[2]) * var24;
         }

         double var38 = Math.exp(-var8 / this.hW);
         if (this.hW2 > 0.0) {
            var38 = 0.6 * var38 + 0.4 * Math.exp(-var8 / this.hW2);
         }

         double var39 = this.zen[0];
         double var28 = this.zen[1];
         double var30 = this.zen[2];
         if (this.mid != null) {
            double var32 = Math.exp(-var8 / this.midW);
            var39 += (this.mid[0] - var39) * var32;
            var28 += (this.mid[1] - var28) * var32;
            var30 += (this.mid[2] - var30) * var32;
         }

         var7.r = var39 + (var18 - var39) * var38;
         var7.g = var28 + (var20 - var28) * var38;
         var7.b = var30 + (var22 - var30) * var38;
         double var40 = (1.0 - var16) * (1.0 - var16);
         if (this.shadow != null) {
            SkyGen.mixTo(var7, this.shadow, this.shadowK * var40 * Math.exp(-var8 / 0.035));
         }

         if (this.belt != null) {
            double var34 = (var8 - 0.09) / 0.07;
            SkyGen.add(var7, this.belt, this.beltK * var40 * Math.exp(-var34 * var34));
         }

         if (this.glow != null) {
            double var41 = Math.pow(var16, this.glowPow);
            SkyGen.add(var7, this.glow, this.glowK * var41 * Math.exp(-var8 / this.glowH));
         }

         double var42 = var1 * this.sun[0] + var3 * this.sun[1] + var5 * this.sun[2];
         if (this.disk > 0.0 || this.b1 > 0.0 || this.b2 > 0.0) {
            SkyGen.sunDisk(var7, var42, this.sunR, this.sunCol, this.disk, this.b1, this.w1, this.b2, this.w2);
         }

         if (this.moon != null) {
            SkyGen3.moon(var7, var1, var3, var5, this.moon, this.moonTint, 1.0, this.sun);
         }

         this.extra(var1, var3, var5, var7);
         if (this.tw != null) {
            SkyGen.towers(var7, var1, var3, var5, this.tw, this.sun);
         }

         if (this.c2 != null) {
            SkyGen.clouds(var7, var1, var3, var5, this.c2, this.sun, this.sunCol);
         }

         if (this.c1 != null) {
            SkyGen.clouds(var7, var1, var3, var5, this.c1, this.sun, this.sunCol);
         }

         if (this.post != null && var42 > 0.0) {
            double var36 = 2.0 * (1.0 - var42);
            SkyGen.add(var7, this.post, this.postK * Math.exp(-var36 / (this.postW * this.postW)));
         }

         if (this.mist != null) {
            SkyGen.mixTo(var7, this.mist, this.mistK * Math.exp(-var8 / this.mistW));
         }
      }

      void extra(double var1, double var3, double var5, SkyGen.C var7) {
      }
   }
}
