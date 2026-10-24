package dev.lego.visual.sky;

final class SkyGen2 {
   private SkyGen2() {
   }

   static SkyGen.Sky make(String id) {
      switch (id) {
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
      SkyGen2.Day s = new SkyGen2.Day();
      s.kind = "day";
      s.sun = SkyGen.azel(150.0, 58.0);
      s.sunCol = SkyGen.rgb(16774884);
      s.zen = SkyGen.rgb(2449604);
      s.hor = SkyGen.rgb(13229810);
      s.hW = 0.22;
      s.hW2 = 0.55;
      s.disk = 45.0;
      s.b1 = 0.25;
      s.w1 = 0.16;
      s.b2 = 1.3;
      s.w2 = 0.03;
      SkyGen.Clouds c = new SkyGen.Clouds();
      c.height = 1.0;
      c.scale = 0.75;
      c.cov = 0.1;
      c.soft = 0.2;
      c.warp = 0.9;
      c.seed = 2.3;
      c.fog = 14.0;
      c.lit = mulc(SkyGen.rgb(16777215), 1.15);
      c.shade = SkyGen.rgb(8228006);
      c.thickDark = 0.5;
      s.c1 = c;
      SkyGen.Clouds ci = new SkyGen.Clouds();
      ci.height = 3.0;
      ci.scale = 0.35;
      ci.stretch = 3.5;
      ci.cov = 0.18;
      ci.soft = 0.35;
      ci.warp = 1.4;
      ci.seed = 8.1;
      ci.oct = 5.0;
      ci.lit = SkyGen.rgb(16054527);
      ci.shade = SkyGen.rgb(12964840);
      ci.opacity = 0.55;
      ci.thickDark = 0.1;
      ci.fog = 30.0;
      s.c2 = ci;
      SkyGen.Towers t = new SkyGen.Towers();
      t.h0 = 0.01;
      t.h1 = 0.1;
      t.freq = 2.6;
      t.cut = 0.05;
      t.bill = 9.0;
      t.seed = 4.2;
      t.lit = mulc(SkyGen.rgb(16777215), 1.1);
      t.shade = SkyGen.rgb(8623280);
      s.tw = t;
      s.mist = SkyGen.rgb(14083314);
      s.mistK = 0.35;
      s.mistW = 0.03;
      s.ground = SkyGen.rgb(4149600);
      s.groundFar = SkyGen.rgb(11124176);
      s.groundFall = 0.25;
      return s;
   }

   static SkyGen.Sky sunset() {
      SkyGen2.Day s = new SkyGen2.Day();
      s.kind = "sunset";
      s.sun = SkyGen.azel(0.0, 2.2);
      s.sunCol = SkyGen.rgb(16766880);
      s.zen = SkyGen.rgb(2502766);
      s.mid = SkyGen.rgb(9067162);
      s.midW = 0.35;
      s.hor = SkyGen.rgb(14841962);
      s.horSun = SkyGen.rgb(16751164);
      s.horAnti = SkyGen.rgb(11562122);
      s.hW = 0.1;
      s.glow = SkyGen.rgb(16756832);
      s.glowK = 1.4;
      s.glowPow = 5.0;
      s.glowH = 0.09;
      s.belt = SkyGen.rgb(14912160);
      s.beltK = 0.25;
      s.shadow = SkyGen.rgb(3817322);
      s.shadowK = 0.5;
      s.sunR = 0.014;
      s.disk = 22.0;
      s.b1 = 0.9;
      s.w1 = 0.1;
      s.b2 = 2.5;
      s.w2 = 0.035;
      SkyGen.Clouds c = new SkyGen.Clouds();
      c.height = 1.3;
      c.scale = 0.9;
      c.cov = 0.05;
      c.soft = 0.28;
      c.warp = 1.1;
      c.seed = 5.7;
      c.fog = 10.0;
      c.stretch = 1.8;
      c.lit = SkyGen.rgb(16753770);
      c.shade = SkyGen.rgb(5978728);
      c.thickDark = 0.6;
      c.silver = 2.5;
      c.lightSide = 1.3;
      s.c1 = c;
      SkyGen.Clouds ci = new SkyGen.Clouds();
      ci.height = 3.0;
      ci.scale = 0.3;
      ci.stretch = 4.0;
      ci.cov = 0.15;
      ci.soft = 0.35;
      ci.seed = 1.9;
      ci.oct = 5.0;
      ci.warp = 1.5;
      ci.lit = SkyGen.rgb(16758416);
      ci.shade = SkyGen.rgb(11561610);
      ci.opacity = 0.7;
      ci.thickDark = 0.1;
      ci.fog = 25.0;
      s.c2 = ci;
      s.below = 2;
      s.water = SkyGen.rgb(1182756);
      s.waves = 1.0;
      s.glitter = 2.2;
      s.rough = 0.09;
      return s;
   }

   static SkyGen.Sky goldenHour() {
      SkyGen2.Day s = new SkyGen2.Day();
      s.kind = "sunset";
      s.sun = SkyGen.azel(250.0, 7.0);
      s.sunCol = SkyGen.rgb(16769704);
      s.zen = SkyGen.rgb(3498408);
      s.hor = SkyGen.rgb(15324862);
      s.horSun = SkyGen.rgb(16763774);
      s.horAnti = SkyGen.rgb(12175066);
      s.hW = 0.2;
      s.hW2 = 0.5;
      s.glow = SkyGen.rgb(16760944);
      s.glowK = 0.9;
      s.glowPow = 4.0;
      s.glowH = 0.18;
      s.sunR = 0.011;
      s.disk = 30.0;
      s.b1 = 0.55;
      s.w1 = 0.14;
      s.b2 = 2.0;
      s.w2 = 0.035;
      SkyGen.Clouds c = new SkyGen.Clouds();
      c.height = 1.0;
      c.scale = 0.8;
      c.cov = 0.14;
      c.soft = 0.22;
      c.warp = 1.0;
      c.seed = 3.3;
      c.fog = 12.0;
      c.lit = mulc(SkyGen.rgb(16766106), 1.25);
      c.shade = SkyGen.rgb(7236234);
      c.thickDark = 0.55;
      c.silver = 2.0;
      c.lightSide = 1.4;
      s.c1 = c;
      SkyGen.Clouds ci = new SkyGen.Clouds();
      ci.height = 3.5;
      ci.scale = 0.28;
      ci.stretch = 5.0;
      ci.cov = 0.12;
      ci.soft = 0.4;
      ci.seed = 6.1;
      ci.oct = 5.0;
      ci.warp = 1.6;
      ci.lit = SkyGen.rgb(16769200);
      ci.shade = SkyGen.rgb(13218996);
      ci.opacity = 0.6;
      ci.thickDark = 0.1;
      ci.fog = 28.0;
      s.c2 = ci;
      s.below = 3;
      SkyGen.Clouds sea = new SkyGen.Clouds();
      sea.height = 1.0;
      sea.scale = 0.9;
      sea.cov = -0.3;
      sea.soft = 0.3;
      sea.warp = 1.2;
      sea.seed = 9.4;
      sea.fog = 0.06;
      sea.lit = mulc(SkyGen.rgb(16769976), 1.05);
      sea.shade = SkyGen.rgb(9340576);
      s.sea = sea;
      s.seaGap = SkyGen.rgb(5923458);
      return s;
   }

   static SkyGen.Sky cottonCandy() {
      SkyGen2.Day s = new SkyGen2.Day();
      s.kind = "sunset";
      s.sun = SkyGen.azel(290.0, -1.5);
      s.sunCol = SkyGen.rgb(16763056);
      s.zen = SkyGen.rgb(5136304);
      s.mid = SkyGen.rgb(10980048);
      s.midW = 0.45;
      s.hor = SkyGen.rgb(16168648);
      s.horSun = SkyGen.rgb(16762016);
      s.horAnti = SkyGen.rgb(13805272);
      s.hW = 0.16;
      s.glow = SkyGen.rgb(16756896);
      s.glowK = 0.8;
      s.glowPow = 4.0;
      s.glowH = 0.12;
      s.belt = SkyGen.rgb(16752320);
      s.beltK = 0.35;
      s.disk = 0.0;
      s.b1 = 0.5;
      s.w1 = 0.2;
      s.b2 = 0.6;
      s.w2 = 0.06;
      SkyGen.Clouds c = new SkyGen.Clouds();
      c.height = 1.1;
      c.scale = 0.7;
      c.cov = 0.08;
      c.soft = 0.26;
      c.warp = 1.0;
      c.seed = 7.2;
      c.fog = 12.0;
      c.lit = mulc(SkyGen.rgb(16758226), 1.2);
      c.shade = SkyGen.rgb(9206978);
      c.thickDark = 0.45;
      c.silver = 1.5;
      c.lightSide = 1.2;
      s.c1 = c;
      SkyGen.Clouds ci = new SkyGen.Clouds();
      ci.height = 3.0;
      ci.scale = 0.3;
      ci.stretch = 3.0;
      ci.cov = 0.1;
      ci.soft = 0.4;
      ci.seed = 2.6;
      ci.oct = 5.0;
      ci.warp = 1.5;
      ci.lit = SkyGen.rgb(16765152);
      ci.shade = SkyGen.rgb(13018844);
      ci.opacity = 0.65;
      ci.thickDark = 0.1;
      ci.fog = 25.0;
      s.c2 = ci;
      s.below = 2;
      s.water = SkyGen.rgb(1972280);
      s.waves = 0.7;
      s.glitter = 0.6;
      s.rough = 0.12;
      return s;
   }

   static SkyGen.Sky sunrise() {
      SkyGen2.Day s = new SkyGen2.Day();
      s.kind = "sunset";
      s.sun = SkyGen.azel(90.0, 3.5);
      s.sunCol = SkyGen.rgb(16770750);
      s.zen = SkyGen.rgb(5931712);
      s.hor = SkyGen.rgb(15128780);
      s.horSun = SkyGen.rgb(16767138);
      s.horAnti = SkyGen.rgb(11517138);
      s.hW = 0.2;
      s.hW2 = 0.45;
      s.glow = SkyGen.rgb(16766106);
      s.glowK = 1.0;
      s.glowPow = 4.0;
      s.glowH = 0.15;
      s.sunR = 0.011;
      s.disk = 26.0;
      s.b1 = 0.8;
      s.w1 = 0.16;
      s.b2 = 2.2;
      s.w2 = 0.045;
      SkyGen.Clouds ci = new SkyGen.Clouds();
      ci.height = 3.0;
      ci.scale = 0.3;
      ci.stretch = 4.0;
      ci.cov = 0.14;
      ci.soft = 0.4;
      ci.seed = 4.4;
      ci.oct = 5.0;
      ci.warp = 1.6;
      ci.lit = SkyGen.rgb(16769732);
      ci.shade = SkyGen.rgb(12630216);
      ci.opacity = 0.6;
      ci.thickDark = 0.1;
      ci.fog = 28.0;
      s.c2 = ci;
      s.mist = SkyGen.rgb(15918296);
      s.mistK = 0.5;
      s.mistW = 0.035;
      s.below = 3;
      SkyGen.Clouds sea = new SkyGen.Clouds();
      sea.height = 1.0;
      sea.scale = 0.45;
      sea.cov = -0.3;
      sea.soft = 0.55;
      sea.warp = 1.4;
      sea.seed = 2.2;
      sea.fog = 0.09;
      sea.oct = 5.0;
      sea.lit = SkyGen.rgb(16770764);
      sea.shade = SkyGen.rgb(11117750);
      s.sea = sea;
      s.seaGap = SkyGen.rgb(7238792);
      return s;
   }

   static SkyGen.Sky overcast() {
      SkyGen2.Day s = new SkyGen2.Day();
      s.kind = "day";
      s.sun = SkyGen.azel(200.0, 35.0);
      s.zen = SkyGen.rgb(8292241);
      s.hor = SkyGen.rgb(11120566);
      s.hW = 0.3;
      s.disk = 0.0;
      s.b1 = 0.0;
      s.b2 = 0.0;
      SkyGen.Clouds c = new SkyGen.Clouds();
      c.height = 1.0;
      c.scale = 0.55;
      c.cov = -0.45;
      c.soft = 0.55;
      c.warp = 1.2;
      c.seed = 1.1;
      c.fog = 9.0;
      c.oct = 6.0;
      c.lit = SkyGen.rgb(12173255);
      c.shade = SkyGen.rgb(5726058);
      c.thickDark = 0.6;
      c.silver = 0.2;
      c.lightSide = 0.8;
      s.c2 = c;
      SkyGen.Clouds scud = new SkyGen.Clouds();
      scud.height = 0.5;
      scud.scale = 0.9;
      scud.cov = 0.12;
      scud.soft = 0.3;
      scud.warp = 1.4;
      scud.seed = 6.6;
      scud.fog = 7.0;
      scud.lit = SkyGen.rgb(9147035);
      scud.shade = SkyGen.rgb(4607061);
      scud.thickDark = 0.5;
      scud.opacity = 0.8;
      scud.silver = 0.0;
      s.c1 = scud;
      s.post = SkyGen.rgb(16775404);
      s.postK = 0.12;
      s.postW = 0.35;
      s.mist = SkyGen.rgb(10265515);
      s.mistK = 0.6;
      s.mistW = 0.05;
      s.ground = SkyGen.rgb(3291186);
      s.groundFar = SkyGen.rgb(9344412);
      s.groundFall = 0.22;
      return s;
   }

   static SkyGen.Sky thunder() {
      SkyGen2.Day s = new SkyGen2.Day() {
         final double[] aLit = SkyGen2.mulc(SkyGen.rgb(16773852), 1.15);
         final double[] aShade = SkyGen.rgb(5922930);
         final double[] rain = SkyGen.rgb(4870494);
         final double[] under = SkyGen.rgb(3817552);

         @Override
         void extra(double x, double y, double z, SkyGen.C c) {
            this.anvil(x, y, z, c);
         }

         void anvil(double x, double y, double z, SkyGen.C c) {
            if (!(y > 0.8) && !(y < -0.02)) {
               double az = Math.atan2(x, -z);
               double d = az - Math.toRadians(55.0);
               d = Math.atan2(Math.sin(d), Math.cos(d));
               if (!(Math.abs(d) > 1.5)) {
                  double el = Math.asin(Math.max(-1.0, Math.min(1.0, y)));
                  double ca = Math.cos(d);
                  double sa = Math.sin(d);
                  double eTop = 0.55;
                  double fl = SkyGen.smooth(eTop - 0.34, eTop - 0.03, el);
                  double shift = 0.32 * fl * fl;
                  double w = 0.4 - 0.1 * SkyGen.smooth(0.0, 0.3, el) + 0.55 * fl * fl;
                  double dd = d - shift;
                  double qq = dd / w;
                  double top = eTop + 0.035 * (1.0 - qq * qq) - 0.03 * Math.abs(qq) * Math.abs(qq);
                  double bil = SkyGen.fbmL(ca * 7.0, el * 8.0 + 1.3, sa * 7.0, 5.0);
                  double fib = SkyGen.fbmL(ca * 3.0, el * 30.0 + 4.1, sa * 3.0, 4.0);
                  double b = bil * (1.0 - fl) + fib * fl * 0.6;
                  double thin = fl * SkyGen.smooth(0.55, 1.0, Math.abs(qq));
                  double v = Math.min(w - Math.abs(dd), (top - el) * 1.6) + b * 0.09 - thin * 0.03;
                  double a = SkyGen.smooth(0.0, 0.025, v) * (1.0 - 0.6 * thin);
                  if (el < 0.12 && Math.abs(d) < 0.6) {
                     double r = SkyGen.smooth(0.12, 0.02, el)
                        * SkyGen.smooth(0.0, 0.015, el)
                        * SkyGen.smooth(0.6, 0.25, Math.abs(d))
                        * (0.55 + 0.45 * SkyGen.noise(d * 30.0, 0.5, el * 2.0));
                     SkyGen.mixTo(c, this.rain, r * 0.75);
                  }

                  if (!(a <= 0.002)) {
                     double b2 = SkyGen.fbmL(ca * 7.0 - 0.03, (el - 0.02) * 8.0 + 1.3, sa * 7.0, 3.0);
                     double edge = SkyGen.clamp01((bil - b2) * 3.5 + 0.5);
                     double rel = SkyGen.clamp01(el / eTop);
                     double light = 0.2 + 0.8 * SkyGen.smooth(0.0, 0.85, rel) + (edge - 0.5) * 0.6 * (1.0 - fl) + bil * 0.35;
                     light = SkyGen.clamp01(light - 0.25 * SkyGen.smooth(0.25, 0.9, Math.abs(qq)) * fl);
                     double cr = this.aShade[0] + (this.aLit[0] - this.aShade[0]) * light;
                     double cg = this.aShade[1] + (this.aLit[1] - this.aShade[1]) * light;
                     double cb = this.aShade[2] + (this.aLit[2] - this.aShade[2]) * light;
                     double dark = SkyGen.smooth(0.14, 0.0, el) * 0.65;
                     cr += (this.under[0] - cr) * dark;
                     cg += (this.under[1] - cg) * dark;
                     cb += (this.under[2] - cb) * dark;
                     double hz = Math.exp(-Math.max(0.0, el) / 0.03);
                     cr += (c.r - cr) * hz;
                     cg += (c.g - cg) * hz;
                     cb += (c.b - cb) * hz;
                     c.r = c.r + (cr - c.r) * a;
                     c.g = c.g + (cg - c.g) * a;
                     c.b = c.b + (cb - c.b) * a;
                     c.vis *= 1.0 - a;
                  }
               }
            }
         }
      };
      s.kind = "day";
      s.sun = SkyGen.azel(235.0, 24.0);
      s.sunCol = SkyGen.rgb(16772040);
      s.zen = SkyGen.rgb(4087694);
      s.hor = SkyGen.rgb(11845318);
      s.horSun = SkyGen.rgb(15128252);
      s.horAnti = SkyGen.rgb(9345704);
      s.hW = 0.22;
      s.disk = 30.0;
      s.b1 = 0.3;
      s.w1 = 0.15;
      s.b2 = 1.4;
      s.w2 = 0.035;
      SkyGen.Clouds c = new SkyGen.Clouds();
      c.height = 0.8;
      c.scale = 0.8;
      c.cov = 0.14;
      c.soft = 0.25;
      c.warp = 1.2;
      c.seed = 3.9;
      c.fog = 10.0;
      c.lit = SkyGen.rgb(13159636);
      c.shade = SkyGen.rgb(4870236);
      c.thickDark = 0.6;
      s.c1 = c;
      s.mist = SkyGen.rgb(10134702);
      s.mistK = 0.4;
      s.mistW = 0.03;
      s.ground = SkyGen.rgb(2501674);
      s.groundFar = SkyGen.rgb(7239808);
      s.groundFall = 0.2;
      return s;
   }

   static SkyGen.Sky anime() {
      SkyGen2.Day s = new SkyGen2.Day();
      s.kind = "day";
      s.sun = SkyGen.azel(200.0, 42.0);
      s.sunCol = SkyGen.rgb(16776170);
      s.zen = SkyGen.rgb(1201624);
      s.hor = SkyGen.rgb(10277631);
      s.hW = 0.3;
      s.hW2 = 0.6;
      s.sunR = 0.012;
      s.disk = 45.0;
      s.b1 = 0.35;
      s.w1 = 0.2;
      s.b2 = 1.6;
      s.w2 = 0.05;
      SkyGen.Clouds c = new SkyGen.Clouds();
      c.height = 1.0;
      c.scale = 0.55;
      c.cov = 0.12;
      c.soft = 0.09;
      c.warp = 1.0;
      c.seed = 12.5;
      c.fog = 18.0;
      c.oct = 5.0;
      c.lit = mulc(SkyGen.rgb(16777215), 1.3);
      c.shade = SkyGen.rgb(9348826);
      c.thickDark = 0.35;
      c.lightSide = 1.5;
      c.silver = 1.2;
      s.c1 = c;
      SkyGen.Towers t = new SkyGen.Towers();
      t.h0 = 0.02;
      t.h1 = 0.36;
      t.freq = 1.7;
      t.cut = 0.12;
      t.bill = 5.0;
      t.puff = 1.4;
      t.seed = 7.7;
      t.soft = 0.008;
      t.haze = 0.05;
      t.lit = mulc(SkyGen.rgb(16777215), 1.25);
      t.shade = SkyGen.rgb(8362708);
      t.sunSide = 0.4;
      s.tw = t;
      s.below = 3;
      SkyGen.Clouds sea = new SkyGen.Clouds();
      sea.height = 1.0;
      sea.scale = 0.45;
      sea.cov = -0.05;
      sea.soft = 0.12;
      sea.warp = 1.1;
      sea.seed = 3.2;
      sea.fog = 0.07;
      sea.lit = mulc(SkyGen.rgb(16777215), 1.15);
      sea.shade = SkyGen.rgb(10795236);
      s.sea = sea;
      s.seaGap = SkyGen.rgb(4094152);
      return s;
   }

   static SkyGen.Sky alien() {
      final double[] sun2 = SkyGen.azel(145.0, 38.0);
      final double[] sun2c = SkyGen.rgb(13625087);
      SkyGen2.Day s = new SkyGen2.Day() {
         @Override
         void extra(double x, double y, double z, SkyGen.C c) {
            double ca = x * sun2[0] + y * sun2[1] + z * sun2[2];
            SkyGen.sunDisk(c, ca, 0.006, sun2c, 60.0, 0.12, 0.08, 1.2, 0.02);
         }
      };
      s.kind = "day";
      s.sun = SkyGen.azel(215.0, 22.0);
      s.sunCol = SkyGen.rgb(16763030);
      s.zen = SkyGen.rgb(741214);
      s.hor = SkyGen.rgb(9100232);
      s.horSun = SkyGen.rgb(15261864);
      s.horAnti = SkyGen.rgb(7127232);
      s.hW = 0.25;
      s.glow = SkyGen.rgb(16756848);
      s.glowK = 0.35;
      s.glowPow = 5.0;
      s.glowH = 0.2;
      s.sunR = 0.032;
      s.disk = 14.0;
      s.b1 = 0.5;
      s.w1 = 0.2;
      s.b2 = 1.0;
      s.w2 = 0.08;
      s.moon = new SkyGen3.Body(SkyGen.azel(20.0, 32.0), 0.13, 0.0);
      s.moonTint = SkyGen.rgb(15257832);
      SkyGen.Clouds c = new SkyGen.Clouds();
      c.height = 1.4;
      c.scale = 0.6;
      c.stretch = 4.0;
      c.cov = 0.1;
      c.soft = 0.3;
      c.warp = 1.8;
      c.seed = 21.1;
      c.fog = 14.0;
      c.lit = mulc(SkyGen.rgb(16175848), 1.1);
      c.shade = SkyGen.rgb(4160646);
      c.thickDark = 0.4;
      c.silver = 1.5;
      s.c1 = c;
      s.mist = SkyGen.rgb(11069656);
      s.mistK = 0.35;
      s.mistW = 0.04;
      s.ground = SkyGen.rgb(1520182);
      s.groundFar = SkyGen.rgb(6993064);
      s.groundFall = 0.22;
      return s;
   }

   static SkyGen.Sky synthwave() {
      SkyGen2.Day s = new SkyGen2.Day() {
         final double[] top = SkyGen.rgb(16770138);
         final double[] midc = SkyGen.rgb(16747068);
         final double[] bot = SkyGen.rgb(16723590);

         @Override
         void extra(double x, double y, double z, SkyGen.C c) {
            double cz = -z;
            double cosA = x * this.sun[0] + y * this.sun[1] + z * this.sun[2];
            double ang = Math.acos(Math.max(-1.0, Math.min(1.0, cosA)));
            double R = 0.21;
            double gl = Math.exp(-Math.max(0.0, ang - R) / 0.1);
            SkyGen.add(c, this.bot, gl * 0.5);
            if (!(ang > R * 1.02) && !(cz <= 0.0)) {
               double v = (y - (this.sun[1] - R)) / (2.0 * R);
               double r1;
               double g1;
               double b1;
               if (v > 0.5) {
                  double t = (v - 0.5) * 2.0;
                  r1 = SkyGen.mix(this.midc[0], this.top[0], t);
                  g1 = SkyGen.mix(this.midc[1], this.top[1], t);
                  b1 = SkyGen.mix(this.midc[2], this.top[2], t);
               } else {
                  double t = v * 2.0;
                  r1 = SkyGen.mix(this.bot[0], this.midc[0], t);
                  g1 = SkyGen.mix(this.bot[1], this.midc[1], t);
                  b1 = SkyGen.mix(this.bot[2], this.midc[2], t);
               }

               double k = 1.0;
               if (v < 0.55) {
                  double freq = 9.0;
                  double ph = v * freq % 1.0;
                  double gap = (0.55 - v) / 0.55 * 0.55;
                  double ew = 0.06;
                  k = SkyGen.smooth(gap - ew, gap + ew, ph);
               }

               double e = SkyGen.smooth(R * 1.02, R * 0.985, ang) * k;
               double I = 2.2;
               c.r = c.r + (r1 * I - c.r) * e;
               c.g = c.g + (g1 * I - c.g) * e;
               c.b = c.b + (b1 * I - c.b) * e;
               c.vis *= 1.0 - e;
            }
         }
      };
      s.kind = "sunset";
      s.sun = SkyGen.azel(0.0, 8.0);
      s.sunCol = SkyGen.rgb(16738984);
      s.zen = SkyGen.rgb(459548);
      s.mid = SkyGen.rgb(3804770);
      s.midW = 0.35;
      s.hor = SkyGen.rgb(16727694);
      s.horSun = SkyGen.rgb(16734842);
      s.horAnti = SkyGen.rgb(11543678);
      s.hW = 0.07;
      s.glow = SkyGen.rgb(16732058);
      s.glowK = 0.6;
      s.glowPow = 3.0;
      s.glowH = 0.2;
      s.disk = 0.0;
      s.b1 = 0.0;
      s.b2 = 0.0;
      SkyGen.Clouds c = new SkyGen.Clouds();
      c.height = 2.0;
      c.scale = 0.35;
      c.stretch = 6.0;
      c.cov = 0.22;
      c.soft = 0.25;
      c.warp = 0.6;
      c.seed = 4.8;
      c.fog = 20.0;
      c.oct = 4.0;
      c.lit = SkyGen.rgb(16742080);
      c.shade = SkyGen.rgb(5906046);
      c.thickDark = 0.3;
      c.silver = 0.0;
      c.opacity = 0.85;
      s.c1 = c;
      s.stars = 26000;
      s.starMin = 0.05;
      s.starMax = 2.5;
      s.starSeed = 31L;
      s.starDensityFn = (x, y, z) -> SkyGen.smooth(0.12, 0.55, y);
      s.below = 2;
      s.water = SkyGen.rgb(787488);
      s.waves = 0.25;
      s.glitter = 0.0;
      s.mirror = 1.0;
      return s;
   }

   static SkyGen.Sky oceanDusk() {
      SkyGen2.Day s = new SkyGen2.Day() {
         @Override
         double starDensity(double x, double y, double z) {
            return SkyGen.smooth(0.12, 0.6, y) * 0.8;
         }
      };
      s.kind = "sunset";
      s.sun = SkyGen.azel(265.0, -5.0);
      s.sunCol = SkyGen.rgb(16751216);
      s.zen = SkyGen.rgb(727104);
      s.mid = SkyGen.rgb(1849968);
      s.midW = 0.4;
      s.hor = SkyGen.rgb(4025496);
      s.horSun = SkyGen.rgb(15764066);
      s.horAnti = SkyGen.rgb(3035256);
      s.hW = 0.09;
      s.glow = SkyGen.rgb(16747098);
      s.glowK = 0.7;
      s.glowPow = 6.0;
      s.glowH = 0.07;
      s.belt = SkyGen.rgb(9071258);
      s.beltK = 0.18;
      s.disk = 0.0;
      s.b1 = 0.3;
      s.w1 = 0.12;
      s.b2 = 0.0;
      s.w2 = 0.05;
      SkyGen.Clouds ci = new SkyGen.Clouds();
      ci.height = 3.0;
      ci.scale = 0.3;
      ci.stretch = 4.0;
      ci.cov = 0.14;
      ci.soft = 0.35;
      ci.seed = 9.9;
      ci.oct = 5.0;
      ci.warp = 1.6;
      ci.lit = SkyGen.rgb(16032416);
      ci.shade = SkyGen.rgb(3816554);
      ci.opacity = 0.7;
      ci.thickDark = 0.2;
      ci.fog = 25.0;
      ci.silver = 3.0;
      ci.lightSide = 1.6;
      s.c2 = ci;
      s.stars = 20000;
      s.starMin = 0.04;
      s.starMax = 2.0;
      s.starSeed = 5L;
      s.below = 2;
      s.water = SkyGen.rgb(200732);
      s.waves = 1.1;
      s.glitter = 0.4;
      s.rough = 0.1;
      return s;
   }

   static double[] mulc(double[] c, double k) {
      return new double[]{c[0] * k, c[1] * k, c[2] * k};
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
      void sky(double x, double y, double z, SkyGen.C c) {
         double yy = y > 0.0 ? y : 0.0;
         double hl = Math.sqrt(x * x + z * z) + 1.0E-9;
         double shl = Math.sqrt(this.sun[0] * this.sun[0] + this.sun[2] * this.sun[2]) + 1.0E-9;
         double sh = (x * this.sun[0] + z * this.sun[2]) / (hl * shl);
         double s01 = 0.5 + 0.5 * sh;
         double hr = this.hor[0];
         double hg = this.hor[1];
         double hb = this.hor[2];
         if (this.horSun != null) {
            double a = s01 * s01;
            double[] lo = this.horAnti != null ? this.horAnti : this.hor;
            hr = lo[0] + (this.horSun[0] - lo[0]) * a;
            hg = lo[1] + (this.horSun[1] - lo[1]) * a;
            hb = lo[2] + (this.horSun[2] - lo[2]) * a;
         }

         double t = Math.exp(-yy / this.hW);
         if (this.hW2 > 0.0) {
            t = 0.6 * t + 0.4 * Math.exp(-yy / this.hW2);
         }

         double zr = this.zen[0];
         double zg = this.zen[1];
         double zb = this.zen[2];
         if (this.mid != null) {
            double m = Math.exp(-yy / this.midW);
            zr += (this.mid[0] - zr) * m;
            zg += (this.mid[1] - zg) * m;
            zb += (this.mid[2] - zb) * m;
         }

         c.r = zr + (hr - zr) * t;
         c.g = zg + (hg - zg) * t;
         c.b = zb + (hb - zb) * t;
         double anti = (1.0 - s01) * (1.0 - s01);
         if (this.shadow != null) {
            SkyGen.mixTo(c, this.shadow, this.shadowK * anti * Math.exp(-yy / 0.035));
         }

         if (this.belt != null) {
            double q = (yy - 0.09) / 0.07;
            SkyGen.add(c, this.belt, this.beltK * anti * Math.exp(-q * q));
         }

         if (this.glow != null) {
            double sp = Math.pow(s01, this.glowPow);
            SkyGen.add(c, this.glow, this.glowK * sp * Math.exp(-yy / this.glowH));
         }

         double cosA = x * this.sun[0] + y * this.sun[1] + z * this.sun[2];
         if (this.disk > 0.0 || this.b1 > 0.0 || this.b2 > 0.0) {
            SkyGen.sunDisk(c, cosA, this.sunR, this.sunCol, this.disk, this.b1, this.w1, this.b2, this.w2);
         }

         if (this.moon != null) {
            SkyGen3.moon(c, x, y, z, this.moon, this.moonTint, 1.0, this.sun);
         }

         this.extra(x, y, z, c);
         if (this.tw != null) {
            SkyGen.towers(c, x, y, z, this.tw, this.sun);
         }

         if (this.c2 != null) {
            SkyGen.clouds(c, x, y, z, this.c2, this.sun, this.sunCol);
         }

         if (this.c1 != null) {
            SkyGen.clouds(c, x, y, z, this.c1, this.sun, this.sunCol);
         }

         if (this.post != null && cosA > 0.0) {
            double a2 = 2.0 * (1.0 - cosA);
            SkyGen.add(c, this.post, this.postK * Math.exp(-a2 / (this.postW * this.postW)));
         }

         if (this.mist != null) {
            SkyGen.mixTo(c, this.mist, this.mistK * Math.exp(-yy / this.mistW));
         }
      }

      void extra(double x, double y, double z, SkyGen.C c) {
      }
   }
}
