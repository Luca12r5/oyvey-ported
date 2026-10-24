package dev.lego.visual.sky;

import java.util.Random;

final class SkyGen3 {
   private static final double[] EYE = SkyGen.azel(300.0, 40.0);

   private SkyGen3() {
   }

   static SkyGen.Sky make(String id) {
      switch (id) {
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

   static void moon(SkyGen.C c, double x, double y, double z, SkyGen3.Body m, double[] tint, double bright, double[] light) {
      double cosA = x * m.d[0] + y * m.d[1] + z * m.d[2];
      if (!(cosA < m.cosOut)) {
         double ang = Math.sqrt(Math.max(0.0, 2.0 * (1.0 - cosA)));
         double rr = ang / m.rad;
         if (m.glow > 0.0) {
            double g = m.glow * bright * Math.exp(-Math.max(0.0, rr - 1.0) / m.glowW);
            SkyGen.add(c, tint, g);
         }

         double aa = Math.max(0.02, 0.004 / m.rad);
         if (!(rr > 1.0 + aa)) {
            double px = (x * m.ex[0] + y * m.ex[1] + z * m.ex[2]) / m.sinR;
            double py = (x * m.ey[0] + y * m.ey[1] + z * m.ey[2]) / m.sinR;
            double q = px * px + py * py;
            double pz = Math.sqrt(Math.max(0.0, 1.0 - Math.min(1.0, q)));
            double nx = m.ex[0] * px + m.ey[0] * py - m.d[0] * pz;
            double ny = m.ex[1] * px + m.ey[1] * py - m.d[1] * pz;
            double nz = m.ex[2] * px + m.ey[2] * py - m.d[2] * pz;
            double s = m.seed;
            double maria = SkyGen.smooth(-0.05, 0.3, SkyGen.fbmL(nx * 1.8 + s, ny * 1.8, nz * 1.8, 4.0));
            double crater = SkyGen.smooth(0.55, 0.85, SkyGen.ridged(nx * 6.0 + s, ny * 6.0, nz * 6.0, 3));
            double fine = SkyGen.fbmL(nx * 14.0, ny * 14.0 + s, nz * 14.0, 3.0);
            double alb = m.albedo * (1.0 - 0.42 * maria) * (0.9 + 0.25 * fine) * (1.0 + 0.25 * crater);
            double lam;
            if (light != null) {
               double ld = nx * light[0] + ny * light[1] + nz * light[2];
               lam = SkyGen.smooth(-0.04, 0.25, ld) * (0.35 + 0.65 * Math.max(0.0, ld));
            } else {
               lam = 0.55 + 0.45 * pz;
            }

            if (m.grad != null) {
               double gd = nx * m.grad[0] + ny * m.grad[1] + nz * m.grad[2];
               lam *= 0.3 + 0.7 * SkyGen.smooth(-0.9, 1.0, gd);
            }

            double k = alb * (lam * bright + m.earthshine);
            double e = SkyGen.smooth(1.0 + aa, 1.0 - aa, rr);
            c.r = c.r + (c.r * 0.35 + tint[0] * k - c.r) * e;
            c.g = c.g + (c.g * 0.35 + tint[1] * k - c.g) * e;
            c.b = c.b + (c.b * 0.35 + tint[2] * k - c.b) * e;
            c.vis *= 1.0 - e;
         }
      }
   }

   static double milky(SkyGen.C c, double x, double y, double z, double[] N, double[] core, double K) {
      double dist = x * N[0] + y * N[1] + z * N[2];
      if (Math.abs(dist) > 0.7) {
         return 0.0;
      } else {
         double w = SkyGen.fbmL(x * 3.0 + 1.0, y * 3.0, z * 3.0, 4.0);
         double q = (dist + w * 0.07) / 0.17;
         double band = Math.exp(-q * q);
         double cc = x * core[0] + y * core[1] + z * core[2];
         double bul = Math.exp(-(1.0 - cc) / 0.09) * Math.exp(-dist * dist / 0.03);
         double s = SkyGen.fbmL(x * 7.0 + 3.0, y * 7.0, z * 7.0, 6.0);
         double cl = SkyGen.clamp01(0.45 + s * 1.1);
         double r = SkyGen.ridged(x * 5.0 + 2.0, y * 5.0, z * 5.0, 5);
         double q2 = dist / 0.06;
         double dust = SkyGen.smooth(0.35, 0.8, r) * Math.exp(-q2 * q2)
            + 0.5 * SkyGen.smooth(0.1, 0.5, SkyGen.fbmL(x * 9.0, y * 9.0 + 5.0, z * 9.0, 4.0)) * band;
         dust = SkyGen.clamp01(dust);
         double light = (band * (0.25 + cl) + bul * 1.4) * (1.0 - 0.8 * dust);
         double warm = SkyGen.clamp01(bul * 1.5);
         c.r += K * light * (0.55 + 0.5 * warm);
         c.g += K * light * (0.62 + 0.22 * warm);
         c.b += K * light * (0.88 - 0.25 * warm);
         return band;
      }
   }

   static double milkyDensity(double x, double y, double z, double[] N) {
      double dist = x * N[0] + y * N[1] + z * N[2];
      double w = SkyGen.fbmL(x * 3.0 + 1.0, y * 3.0, z * 3.0, 4.0);
      double q = (dist + w * 0.07) / 0.17;
      return Math.exp(-q * q);
   }

   static void aurora(SkyGen.C c, double x, double y, double z, SkyGen3.Aurora A) {
      if (!(y < 0.004)) {
         double fade = SkyGen.smooth(0.004, 0.15, y);
         double hl = Math.sqrt(x * x + z * z) + 1.0E-9;
         double mask = SkyGen.smooth(-0.35, 0.25, SkyGen.fbmL(x / hl * 1.3 + A.seed, 0.5, z / hl * 1.3, 2.0) + A.cover + 0.4 * (-z / hl) * A.north);
         if (!(mask <= 0.003)) {
            double inv = A.scale / (y + 0.25);
            double segLen = hl * (A.h1 - A.h0) * inv * 0.9;
            int S = (int)Math.max(6.0, Math.min((double)A.steps, Math.ceil(segLen / 0.06)));
            double dh = (A.h1 - A.h0) / S;
            double pf = auroraF(x * A.h0 * inv, z * A.h0 * inv, A);
            double ph = A.h0;
            double glow = Math.exp(-pf * pf / 0.008);
            double sr = 0.0;
            double sg = 0.0;
            double sb = 0.0;

            for (int i = 1; i <= S; i++) {
               double h = A.h0 + dh * i;
               double f = auroraF(x * h * inv, z * h * inv, A);
               double u = 0.0;
               double wt = 0.0;
               if (pf <= 0.0 != f <= 0.0) {
                  u = pf / (pf - f);
                  wt = 1.0;
               }

               if (wt > 0.002) {
                  double hs = ph + dh * u;
                  double t = (hs - A.h0) / (A.h1 - A.h0);
                  double slope = Math.abs(f - pf) / dh;
                  double b = A.width / Math.sqrt(slope * slope + A.width * A.width);
                  double px = x * hs * inv;
                  double pz = z * hs * inv;
                  double r = 0.25 + 0.75 * Math.abs(SkyGen.noise(px * 9.0 + A.seed, 3.7, pz * 9.0)) * A.rays + (1.0 - A.rays) * 0.75;
                  double prof = (Math.exp(-t * 2.6) * SkyGen.smooth(0.0, 0.06, t) + 0.12 * Math.exp(-(t - 0.6) * (t - 0.6) / 0.04))
                     * SkyGen.smooth(1.0, 0.75, t);
                  double tt = Math.pow(t, 0.8);
                  double w = b * r * prof * wt;
                  sr += (A.low[0] + (A.high[0] - A.low[0]) * tt) * w;
                  sg += (A.low[1] + (A.high[1] - A.low[1]) * tt) * w;
                  sb += (A.low[2] + (A.high[2] - A.low[2]) * tt) * w;
               }

               pf = f;
               ph = h;
            }

            double k = A.bright * fade * mask;
            double gk = 0.03 * glow * k;
            c.r = c.r + (sr * k + A.low[0] * gk);
            c.g = c.g + (sg * k + A.low[1] * gk);
            c.b = c.b + (sb * k + A.low[2] * gk);
         }
      }
   }

   private static double auroraF(double px, double pz, SkyGen3.Aurora A) {
      return SkyGen.fbmL(px * 0.9 + A.seed, A.seed * 0.7, pz * 0.9, 2.5);
   }

   static void nebulaLayer(
      SkyGen.C c, double x, double y, double z, double seed, double scale, double[] a, double[] b, double[] cc, double[] core, double K, double dustK
   ) {
      double wx = SkyGen.fbmL(x * 1.3 + 3.1 + seed, y * 1.3, z * 1.3, 3.0);
      double wy = SkyGen.fbmL(x * 1.3, y * 1.3 + 7.7, z * 1.3 + seed, 3.0);
      double wz = SkyGen.fbmL(x * 1.3 + seed, y * 1.3, z * 1.3 + 1.3, 3.0);
      double X = (x + wx * 0.4) * scale;
      double Y = (y + wy * 0.4) * scale;
      double Z = (z + wz * 0.4) * scale;
      double D = SkyGen.smooth(-0.05, 0.6, SkyGen.fbmL(X * 1.5, Y * 1.5, Z * 1.5 + seed, 6.0) + 0.12);
      if (!(D <= 0.001)) {
         double fil = SkyGen.ridged(X * 4.0 + seed, Y * 4.0, Z * 4.0, 5);
         double f = SkyGen.smooth(0.5, 0.95, fil) * D;
         double hue = SkyGen.clamp01(0.5 + SkyGen.fbmL(X * 0.8 + 7.0, Y * 0.8, Z * 0.8 + seed, 3.0) * 1.6);
         double gl = D * D * 0.55 + D * 0.12;
         double ra = a[0] + (b[0] - a[0]) * hue;
         double ga = a[1] + (b[1] - a[1]) * hue;
         double ba = a[2] + (b[2] - a[2]) * hue;
         c.r = c.r + K * (ra * gl + (cc[0] * 0.6 + ra * 0.6) * f);
         c.g = c.g + K * (ga * gl + (cc[1] * 0.6 + ga * 0.6) * f);
         c.b = c.b + K * (ba * gl + (cc[2] * 0.6 + ba * 0.6) * f);
         double hot = Math.max(0.0, D - 0.75) / 0.25;
         SkyGen.add(c, core, K * hot * hot * 0.8);
         double dust = SkyGen.smooth(0.45, 0.85, SkyGen.ridged(X * 2.3 + 3.0, Y * 2.3 + seed, Z * 2.3, 4)) * SkyGen.smooth(0.1, 0.6, D);
         double dk = 1.0 - dust * dustK;
         SkyGen.mul(c, dk);
         c.vis *= 0.4 + 0.6 * dk;
      }
   }

   static SkyGen.Sky milkyway() {
      SkyGen3.Night s = new SkyGen3.Night();
      s.zen = SkyGen.rgb(132365);
      s.hor = SkyGen.rgb(924206);
      s.hW = 0.28;
      s.air = SkyGen.rgb(928284);
      s.airK = 0.6;
      s.airW = 0.07;
      s.lp = SkyGen.rgb(3810320);
      s.lpDir = SkyGen.norm(-0.7, 0.0, 0.7);
      s.lpK = 0.5;
      s.mwN = SkyGen.norm(0.35, 0.62, 0.7);
      s.mwCore = coreOn(s.mwN, SkyGen.norm(0.75, 0.2, -0.6));
      s.mwK = 0.075;
      s.stars = 90000;
      s.ground = SkyGen.rgb(329483);
      s.groundFar = SkyGen.rgb(923170);
      s.groundFall = 0.2;
      return s;
   }

   static SkyGen.Sky aurora() {
      SkyGen3.Night s = new SkyGen3.Night();
      s.zen = SkyGen.rgb(132880);
      s.hor = SkyGen.rgb(729648);
      s.hW = 0.3;
      s.mwN = SkyGen.norm(-0.5, 0.55, 0.65);
      s.mwCore = coreOn(s.mwN, SkyGen.norm(0.3, 0.3, 0.9));
      s.mwK = 0.025;
      SkyGen3.Aurora a = new SkyGen3.Aurora();
      a.low = SkyGen.rgb(3080070);
      a.high = SkyGen.rgb(11553992);
      a.bright = 0.55;
      a.seed = 2.7;
      a.width = 0.14;
      s.au = a;
      s.mist = SkyGen.rgb(929840);
      s.mistK = 0.35;
      s.mistW = 0.03;
      s.detail = 0.85;
      s.ground = SkyGen.rgb(1186854);
      s.groundFar = SkyGen.rgb(1980988);
      s.groundFall = 0.25;
      return s;
   }

   static SkyGen.Sky auroraViolet() {
      SkyGen3.Night s = new SkyGen3.Night();
      s.zen = SkyGen.rgb(328463);
      s.hor = SkyGen.rgb(1839152);
      s.hW = 0.3;
      SkyGen3.Aurora a = new SkyGen3.Aurora();
      a.low = SkyGen.rgb(16727968);
      a.high = SkyGen.rgb(5914879);
      a.bright = 0.55;
      a.seed = 7.3;
      a.width = 0.14;
      a.cover = 0.0;
      s.au = a;
      s.stars = 70000;
      s.below = 2;
      s.water = SkyGen.rgb(131849);
      s.waves = 0.3;
      s.glitter = 0.0;
      s.detail = 0.85;
      return s;
   }

   static SkyGen.Sky storm() {
      final double[][] cells = new double[][]{SkyGen.azel(40.0, 25.0), SkyGen.azel(160.0, 14.0), SkyGen.azel(290.0, 35.0), SkyGen.azel(250.0, 60.0)};
      final double[] flash = SkyGen.rgb(12104959);
      SkyGen3.Night s = new SkyGen3.Night() {
         @Override
         void extra(double x, double y, double z, SkyGen.C c) {
            double hl = Math.sqrt(x * x + z * z) + 1.0E-9;
            double a = Math.max(0.0, (x * 0.8 - z * 0.6) / hl);
            SkyGen.add(c, flash, 0.05 * a * a * a * a * Math.exp(-Math.max(0.0, y) / 0.08));
         }

         @Override
         void sky(double x, double y, double z, SkyGen.C c) {
            super.sky(x, y, z, c);
            if (!(y <= 0.0)) {
               double g = 0.0;

               for (double[] q : cells) {
                  double cosA = x * q[0] + y * q[1] + z * q[2];
                  double a2 = 2.0 * (1.0 - cosA);
                  g += Math.exp(-a2 / 0.06);
               }

               if (g > 0.003) {
                  double hl = 1.0 / (y + 0.05);
                  double n = SkyGen.fbmL(x * hl * 1.2, 5.5, z * hl * 1.2, 5.0);
                  double k = g * SkyGen.smooth(-0.3, 0.5, n) * SkyGen.smooth(0.0, 0.1, y);
                  SkyGen.add(c, flash, k * 0.45);
               }
            }
         }
      };
      s.stars = 0;
      s.zen = SkyGen.rgb(790294);
      s.hor = SkyGen.rgb(2501688);
      s.hW = 0.25;
      SkyGen.Clouds c = new SkyGen.Clouds();
      c.height = 0.8;
      c.scale = 0.7;
      c.cov = -0.15;
      c.soft = 0.45;
      c.warp = 1.3;
      c.seed = 4.1;
      c.fog = 6.0;
      c.lit = SkyGen.rgb(5266030);
      c.shade = SkyGen.rgb(592656);
      c.thickDark = 0.75;
      c.silver = 0.0;
      c.lightSide = 1.6;
      s.c1 = c;
      s.sun = SkyGen.azel(40.0, 25.0);
      s.mist = SkyGen.rgb(1975084);
      s.mistK = 0.55;
      s.mistW = 0.05;
      s.ground = SkyGen.rgb(461068);
      s.groundFar = SkyGen.rgb(1711654);
      s.groundFall = 0.2;
      return s;
   }

   static SkyGen.Sky bloodMoon() {
      SkyGen3.Night s = new SkyGen3.Night();
      s.zen = SkyGen.rgb(328458);
      s.hor = SkyGen.rgb(2756626);
      s.hW = 0.22;
      s.air = SkyGen.rgb(3804680);
      s.airK = 0.3;
      s.airW = 0.05;
      s.mwN = SkyGen.norm(0.2, 0.7, -0.68);
      s.mwCore = coreOn(s.mwN, SkyGen.norm(-0.8, 0.3, 0.1));
      s.mwK = 0.02;
      SkyGen3.Body m = new SkyGen3.Body(SkyGen.azel(160.0, 24.0), 0.09, 25.0);
      m.glow = 0.035;
      m.glowW = 2.5;
      m.albedo = 0.6;
      m.seed = 3.0;
      m.grad = SkyGen.norm(0.3, 1.0, 0.2);
      m.earthshine = 0.0;
      s.moon = m;
      s.moonTint = SkyGen.rgb(16734764);
      s.moonBright = 1.5;
      s.mist = SkyGen.rgb(2755596);
      s.mistK = 0.5;
      s.mistW = 0.04;
      s.stars = 50000;
      s.ground = SkyGen.rgb(787718);
      s.groundFar = SkyGen.rgb(2363406);
      s.groundFall = 0.2;
      return s;
   }

   static SkyGen.Sky eclipse() {
      final double[] sunD = SkyGen.azel(165.0, 36.0);
      double R = 0.034;
      final double[] cor = SkyGen.rgb(15723263);
      final double[] prom = SkyGen.rgb(16726634);
      final double[] band = SkyGen.rgb(16751178);
      final double[] band2 = SkyGen.rgb(15255696);
      final double[][] fr = SkyGen.frame(sunD, 0.0);
      SkyGen3.Night s = new SkyGen3.Night() {
         @Override
         void extra(double x, double y, double z, SkyGen.C c) {
            double yy = Math.max(0.0, y);
            double hl = Math.sqrt(x * x + z * z) + 1.0E-9;
            double var = 0.6 + 0.4 * SkyGen.fbmL(x / hl * 2.0, 1.1, z / hl * 2.0, 3.0) + 0.3 * ((x * sunD[0] + z * sunD[2]) / hl);
            SkyGen.add(c, band, 0.55 * var * Math.exp(-yy / 0.035));
            SkyGen.add(c, band2, 0.12 * Math.exp(-yy / 0.12));
            double cosA = x * sunD[0] + y * sunD[1] + z * sunD[2];
            if (!(cosA < 0.6)) {
               double ang = Math.sqrt(Math.max(0.0, 2.0 * (1.0 - cosA)));
               double r = ang / 0.034;
               double px = x * fr[0][0] + y * fr[0][1] + z * fr[0][2];
               double py = x * fr[1][0] + y * fr[1][1] + z * fr[1][2];
               double pl = Math.sqrt(px * px + py * py) + 1.0E-12;
               double ca = px / pl;
               double sa = py / pl;
               if (r > 0.99) {
                  double st = 0.45 + 0.9 * Math.max(0.0, SkyGen.fbmL(ca * 2.5, sa * 2.5, r * 0.35, 4.0) + 0.2);
                  double st2 = Math.pow(Math.abs(SkyGen.noise(ca * 9.0, sa * 9.0, 1.3)), 0.5);
                  double corona = 1.4 * Math.pow(r, -2.6) * (0.3 + st * 0.9 + 0.3 * st2) + 0.05 * Math.pow(r, -1.0);
                  SkyGen.add(c, cor, corona * SkyGen.smooth(0.99, 1.02, r));
                  if (r < 1.12) {
                     double p = SkyGen.smooth(0.25, 0.55, SkyGen.fbmL(ca * 6.0 + 4.0, sa * 6.0, 2.2, 3.0)) * SkyGen.smooth(1.12, 1.0, r);
                     SkyGen.add(c, prom, p * 2.2);
                  }

                  double bx = ca - Math.cos(2.3);
                  double by = sa - Math.sin(2.3);
                  double bd = (bx * bx + by * by) * 60.0 + (r - 1.0) * (r - 1.0) * 900.0;
                  SkyGen.add(c, cor, 12.0 * Math.exp(-bd) + 0.25 * Math.exp(-(bx * bx + by * by) * 3.0 - (r - 1.0) * 1.2));
               }

               double e = SkyGen.smooth(1.0, 0.985, r);
               c.r *= 1.0 - e * 0.97;
               c.g *= 1.0 - e * 0.97;
               c.b *= 1.0 - e * 0.97;
               c.vis *= 1.0 - e;
            }
         }
      };
      s.zen = SkyGen.rgb(726322);
      s.hor = SkyGen.rgb(2372174);
      s.hW = 0.3;
      s.stars = 30000;
      s.starDens = 0.6;
      s.ground = SkyGen.rgb(460812);
      s.groundFar = SkyGen.rgb(2761254);
      s.groundFall = 0.07;
      return s;
   }

   static SkyGen.Sky winter() {
      SkyGen3.Night s = new SkyGen3.Night();
      s.zen = SkyGen.rgb(198936);
      s.hor = SkyGen.rgb(1848402);
      s.hW = 0.28;
      SkyGen3.Body m = new SkyGen3.Body(SkyGen.azel(205.0, 34.0), 0.018, 0.0);
      m.glow = 0.07;
      m.glowW = 2.2;
      m.albedo = 0.9;
      m.seed = 1.5;
      s.moon = m;
      s.moonTint = SkyGen.rgb(15397119);
      s.moonBright = 2.2;
      s.halo22 = 0.035;
      SkyGen.Clouds c = new SkyGen.Clouds();
      c.height = 3.0;
      c.scale = 0.3;
      c.stretch = 4.0;
      c.cov = 0.22;
      c.soft = 0.35;
      c.warp = 1.5;
      c.seed = 3.7;
      c.oct = 5.0;
      c.lit = SkyGen.rgb(4873342);
      c.shade = SkyGen.rgb(1186864);
      c.thickDark = 0.2;
      c.silver = 1.0;
      c.opacity = 0.6;
      c.fog = 25.0;
      s.c1 = c;
      s.cloudLight = SkyGen.rgb(8427720);
      s.mist = SkyGen.rgb(3033192);
      s.mistK = 0.55;
      s.mistW = 0.04;
      s.stars = 70000;
      s.starDens = 0.9;
      s.ground = SkyGen.rgb(2898514);
      s.groundFar = SkyGen.rgb(4217468);
      s.groundFall = 0.3;
      s.groundNoise = 0.35;
      return s;
   }

   static SkyGen.Sky twilight() {
      SkyGen3.Night s = new SkyGen3.Night() {
         @Override
         void moteColor(Random r, double[] o) {
            double t = r.nextDouble();
            if (t < 0.55) {
               o[0] = 1.3;
               o[1] = 0.95;
               o[2] = 0.45;
            } else if (t < 0.8) {
               o[0] = 0.45;
               o[1] = 1.0;
               o[2] = 1.3;
            } else {
               o[0] = 1.25;
               o[1] = 0.55;
               o[2] = 1.2;
            }
         }

         @Override
         double moteDensity(double x, double y, double z) {
            return 0.08 + 0.92 * Math.exp(-Math.abs(y + 0.04) / 0.14);
         }
      };
      s.zen = SkyGen.rgb(525856);
      s.hor = SkyGen.rgb(9326222);
      s.hW = 0.12;
      s.air = SkyGen.rgb(3808880);
      s.airK = 0.25;
      s.airW = 0.35;
      s.lp = SkyGen.rgb(16751226);
      s.lpDir = SkyGen.norm(-1.0, 0.0, -0.3);
      s.lpK = 0.6;
      SkyGen3.Body m = new SkyGen3.Body(SkyGen.azel(250.0, 30.0), 0.03, 0.0);
      m.glow = 0.03;
      m.glowW = 1.5;
      m.albedo = 0.9;
      m.seed = 5.0;
      s.moon = m;
      s.moonTint = SkyGen.rgb(16773338);
      s.moonBright = 2.0;
      s.moonLight = SkyGen.azel(290.0, -10.0);
      SkyGen.Clouds c = new SkyGen.Clouds();
      c.height = 1.4;
      c.scale = 0.5;
      c.stretch = 2.5;
      c.cov = 0.16;
      c.soft = 0.3;
      c.warp = 1.4;
      c.seed = 11.2;
      c.fog = 12.0;
      c.lit = SkyGen.rgb(14715584);
      c.shade = SkyGen.rgb(3022944);
      c.thickDark = 0.35;
      c.silver = 0.5;
      c.opacity = 0.85;
      s.c1 = c;
      s.cloudLight = SkyGen.rgb(16756944);
      s.sun = SkyGen.azel(290.0, -10.0);
      s.stars = 50000;
      s.starDens = 0.8;
      s.motes = 1400;
      s.moteBright = 0.7;
      s.moteSize = 1.4;
      s.ground = SkyGen.rgb(657432);
      s.groundFar = SkyGen.rgb(3810386);
      s.groundFall = 0.2;
      return s;
   }

   static SkyGen.Sky nebula() {
      final double[] a = SkyGen.rgb(3816136);
      final double[] b = SkyGen.rgb(11549368);
      final double[] cc = SkyGen.rgb(8048895);
      final double[] core = SkyGen.rgb(16770768);
      final double[] bg = SkyGen.rgb(131850);
      final double[] mwN = SkyGen.norm(0.2, 0.9, 0.3);
      SkyGen3.Space s = new SkyGen3.Space() {
         @Override
         void sky(double x, double y, double z, SkyGen.C c) {
            SkyGen.set(c, bg);
            SkyGen3.milky(c, x, y, z, mwN, SkyGen3.coreOn(mwN, SkyGen.norm(1.0, 0.0, 0.0)), 0.02);
            SkyGen3.nebulaLayer(c, x, y, z, 0.0, 1.0, a, b, cc, core, 0.5, 0.85);
         }

         @Override
         double starDensity(double x, double y, double z) {
            return 0.3 + 0.5 * SkyGen3.milkyDensity(x, y, z, mwN);
         }
      };
      s.stars = 90000;
      s.detail = 0.8;
      return s;
   }

   static SkyGen.Sky redNebula() {
      final double[] a = SkyGen.rgb(12591146);
      final double[] b = SkyGen.rgb(16742960);
      final double[] cc = SkyGen.rgb(16756848);
      final double[] core = SkyGen.rgb(16773320);
      final double[] bg = SkyGen.rgb(262660);
      final double[] cl = SkyGen.azel(30.0, 15.0);
      final double[] blue = SkyGen.rgb(10141951);
      SkyGen3.Space s = new SkyGen3.Space() {
         @Override
         void sky(double x, double y, double z, SkyGen.C c) {
            SkyGen.set(c, bg);
            SkyGen3.nebulaLayer(c, x, y, z, 4.2, 1.1, a, b, cc, core, 0.55, 0.9);
            double cosA = x * cl[0] + y * cl[1] + z * cl[2];
            double a2 = 2.0 * (1.0 - cosA);
            SkyGen.add(c, blue, 0.35 * Math.exp(-a2 / 0.004) + 0.08 * Math.exp(-a2 / 0.05));
            double g = SkyGen.fbmL(x * 2.2 + 2.0, y * 2.2, z * 2.2 + 9.0, 5.0);
            double k = SkyGen.smooth(0.28, 0.45, g);
            SkyGen.mul(c, 1.0 - 0.9 * k);
            c.vis *= 1.0 - 0.95 * k;
         }

         @Override
         double starDensity(double x, double y, double z) {
            double cosA = x * cl[0] + y * cl[1] + z * cl[2];
            return Math.min(1.0, 0.3 + 0.9 * Math.exp(-2.0 * (1.0 - cosA) / 0.02));
         }
      };
      s.stars = 80000;
      s.detail = 0.9;
      return s;
   }

   static SkyGen.Sky planets() {
      final double[] bg = SkyGen.rgb(66056);
      final double[] neb1 = SkyGen.rgb(1719434);
      final double[] neb2 = SkyGen.rgb(5906042);
      final double[] P = SkyGen.azel(35.0, 22.0);
      double pr = 0.26;
      final double Rp = Math.sin(0.26);
      final double[] Nr = SkyGen.norm(0.25, 0.95, 0.35);
      final double[] light = SkyGen.azel(250.0, 25.0);
      final double[] c1 = SkyGen.rgb(15258280);
      final double[] c2 = SkyGen.rgb(12618330);
      final double[] c3 = SkyGen.rgb(9067066);
      final double[] c4 = SkyGen.rgb(15919312);
      final double[] ringC = SkyGen.rgb(14207144);
      final double[] atm = SkyGen.rgb(16769712);
      final SkyGen3.Body m1 = new SkyGen3.Body(SkyGen.azel(300.0, 34.0), 0.045, 0.0);
      final SkyGen3.Body m2 = new SkyGen3.Body(SkyGen.azel(95.0, 55.0), 0.02, 0.0);
      final SkyGen3.Body m3 = new SkyGen3.Body(SkyGen.azel(200.0, 12.0), 0.012, 0.0);
      m1.albedo = 0.7;
      m1.seed = 2.0;
      m2.albedo = 0.9;
      m2.seed = 7.0;
      m3.albedo = 0.5;
      m3.seed = 11.0;
      final double[] m1t = SkyGen.rgb(14209224);
      final double[] m2t = SkyGen.rgb(13624575);
      final double[] m3t = SkyGen.rgb(16751216);
      SkyGen3.Space s = new SkyGen3.Space() {
         @Override
         void sky(double x, double y, double z, SkyGen.C c) {
            SkyGen.set(c, bg);
            double n = SkyGen.smooth(0.1, 0.8, SkyGen.fbmL(x * 2.0 + 4.0, y * 2.0, z * 2.0, 5.0));
            SkyGen.add(c, neb1, n * 0.12);
            SkyGen.add(c, neb2, SkyGen.smooth(0.3, 0.9, SkyGen.fbmL(x * 3.0, y * 3.0 + 2.0, z * 3.0, 4.0)) * 0.1);
            SkyGen3.moon(c, x, y, z, m1, m1t, 1.3, light);
            SkyGen3.moon(c, x, y, z, m2, m2t, 1.4, light);
            SkyGen3.moon(c, x, y, z, m3, m3t, 1.2, light);
            this.ringed(c, x, y, z);
         }

         void ringed(SkyGen.C c, double x, double y, double z) {
            double b = x * P[0] + y * P[1] + z * P[2];
            if (!(b < 0.3)) {
               double disc = b * b - (1.0 - Rp * Rp);
               double tP = Double.MAX_VALUE;
               double edge = 0.0;
               double hx = 0.0;
               double hy = 0.0;
               double hz = 0.0;
               double rim = Math.sqrt(Math.max(0.0, disc)) / Rp;
               if (disc > -4.0E-4 * Rp) {
                  edge = SkyGen.smooth(-0.004, 0.012, disc / Rp);
                  tP = b - Math.sqrt(Math.max(0.0, disc));
                  hx = x * tP;
                  hy = y * tP;
                  hz = z * tP;
               }

               double dn = x * Nr[0] + y * Nr[1] + z * Nr[2];
               double tR = Math.abs(dn) > 1.0E-5 ? (P[0] * Nr[0] + P[1] * Nr[1] + P[2] * Nr[2]) / dn : -1.0;
               double ringA = 0.0;
               double ringL = 0.0;
               if (tR > 0.0) {
                  double rx = x * tR - P[0];
                  double ry = y * tR - P[1];
                  double rz = z * tR - P[2];
                  double rr = Math.sqrt(rx * rx + ry * ry + rz * rz) / Rp;
                  if (rr > 1.25 && rr < 2.35) {
                     ringA = SkyGen3.ringDensity(rr);
                     double ox = x * tR;
                     double oy = y * tR;
                     double oz = z * tR;
                     double sh = SkyGen3.sphereHit(ox, oy, oz, light, P, Rp);
                     double lam = 0.35 + 0.65 * Math.abs(light[0] * Nr[0] + light[1] * Nr[1] + light[2] * Nr[2]);
                     ringL = lam * (1.0 - 0.92 * sh);
                  }
               }

               boolean ringFront = ringA > 0.0 && tR < tP;
               if (edge > 0.0) {
                  double nx = (hx - P[0]) / Rp;
                  double ny = (hy - P[1]) / Rp;
                  double nz = (hz - P[2]) / Rp;
                  double lat = nx * Nr[0] + ny * Nr[1] + nz * Nr[2];
                  double w = SkyGen.fbmL(nx * 3.0, ny * 3.0, nz * 3.0, 4.0) * 0.12;
                  double bands = Math.sin((lat + w) * 22.0) * 0.5 + 0.5;
                  double bands2 = Math.sin((lat + w * 1.5) * 57.0 + 1.3) * 0.5 + 0.5;
                  double storm = SkyGen.fbmL(nx * 8.0, ny * 8.0 + 3.0, nz * 8.0, 4.0);
                  double[] ca = bands > 0.5 ? c1 : c2;
                  double t = Math.abs(bands - 0.5) * 2.0;
                  double r0 = SkyGen.mix(c2[0], ca[0], t);
                  double g0 = SkyGen.mix(c2[1], ca[1], t);
                  double b0 = SkyGen.mix(c2[2], ca[2], t);
                  r0 = SkyGen.mix(r0, c3[0], bands2 * 0.35);
                  g0 = SkyGen.mix(g0, c3[1], bands2 * 0.35);
                  b0 = SkyGen.mix(b0, c3[2], bands2 * 0.35);
                  double st = SkyGen.smooth(0.3, 0.5, storm) * 0.5;
                  r0 = SkyGen.mix(r0, c4[0], st);
                  g0 = SkyGen.mix(g0, c4[1], st);
                  b0 = SkyGen.mix(b0, c4[2], st);
                  double ld = nx * light[0] + ny * light[1] + nz * light[2];
                  double lam = SkyGen.smooth(-0.1, 0.3, ld) * (0.25 + 0.75 * Math.max(0.0, ld));
                  double dnl = light[0] * Nr[0] + light[1] * Nr[1] + light[2] * Nr[2];
                  if (Math.abs(dnl) > 1.0E-4) {
                     double st2 = ((P[0] - hx) * Nr[0] + (P[1] - hy) * Nr[1] + (P[2] - hz) * Nr[2]) / dnl;
                     if (st2 > 0.0) {
                        double qx = hx + light[0] * st2 - P[0];
                        double qy = hy + light[1] * st2 - P[1];
                        double qz = hz + light[2] * st2 - P[2];
                        double qr = Math.sqrt(qx * qx + qy * qy + qz * qz) / Rp;
                        if (qr > 1.25 && qr < 2.35) {
                           lam *= 1.0 - 0.8 * SkyGen3.ringDensity(qr);
                        }
                     }
                  }

                  double fres = Math.pow(1.0 - Math.min(1.0, rim), 4.0);
                  double k = 1.3 * lam;
                  double pr0 = r0 * k + atm[0] * fres * lam * 0.6;
                  double pg0 = g0 * k + atm[1] * fres * lam * 0.6;
                  double pb0 = b0 * k + atm[2] * fres * lam * 0.6;
                  pr0 += 0.004;
                  pg0 += 0.004;
                  pb0 += 0.006;
                  c.r = c.r + (pr0 - c.r) * edge;
                  c.g = c.g + (pg0 - c.g) * edge;
                  c.b = c.b + (pb0 - c.b) * edge;
                  c.vis *= 1.0 - edge;
               }

               if (ringA > 0.0 && (ringFront || edge < 1.0)) {
                  double a = ringA * (ringFront ? 1.0 : 1.0 - edge);
                  double k = 1.2 * ringL;
                  c.r = c.r + (ringC[0] * k - c.r) * a;
                  c.g = c.g + (ringC[1] * k - c.g) * a;
                  c.b = c.b + (ringC[2] * k - c.b) * a;
                  c.vis *= 1.0 - a;
               }
            }
         }
      };
      s.stars = 70000;
      return s;
   }

   static double ringDensity(double rr) {
      double d = 0.35 + 0.35 * SkyGen.noise(rr * 18.0, 1.1, 0.5) + 0.2 * SkyGen.noise(rr * 55.0, 2.3, 0.1);
      d *= SkyGen.smooth(1.25, 1.32, rr) * SkyGen.smooth(2.35, 2.25, rr);
      d *= 1.0 - 0.9 * SkyGen.smooth(0.035, 0.0, Math.abs(rr - 1.95));
      d *= 1.0 - 0.5 * SkyGen.smooth(1.55, 1.45, rr);
      return SkyGen.clamp01(d * 1.4);
   }

   static double sphereHit(double ox, double oy, double oz, double[] l, double[] p, double R) {
      double vx = p[0] - ox;
      double vy = p[1] - oy;
      double vz = p[2] - oz;
      double t = vx * l[0] + vy * l[1] + vz * l[2];
      if (t < 0.0) {
         return 0.0;
      } else {
         double d2 = vx * vx + vy * vy + vz * vz - t * t;
         return SkyGen.smooth(R * R * 1.03, R * R * 0.97, d2);
      }
   }

   static SkyGen.Sky spiral() {
      final double[] G = SkyGen.azel(20.0, 28.0);
      final double[][] fr = SkyGen.frame(G, 35.0);
      double Rg = 0.95;
      final double inc = Math.cos(Math.toRadians(58.0));
      final double[] bulgeC = SkyGen.rgb(16770748);
      final double[] armC = SkyGen.rgb(11060479);
      final double[] hii = SkyGen.rgb(16738992);
      final double[] dustC = SkyGen.rgb(1707528);
      final double[] bg = SkyGen.rgb(131848);
      final double[] G2 = SkyGen.azel(210.0, -30.0);
      final double[][] fr2 = SkyGen.frame(G2, -20.0);
      final double[] neb = SkyGen.rgb(3808874);
      SkyGen3.Space s = new SkyGen3.Space() {
         @Override
         void sky(double x, double y, double z, SkyGen.C c) {
            SkyGen.set(c, bg);
            SkyGen.add(c, neb, 0.12 * SkyGen.smooth(0.1, 0.8, SkyGen.fbmL(x * 2.0 + 1.0, y * 2.0, z * 2.0 + 5.0, 5.0)));
            this.galaxy(c, x, y, z, G, fr, 0.95, inc, 1.0, 0.0);
            this.galaxy(c, x, y, z, G2, fr2, 0.16, Math.cos(Math.toRadians(70.0)), 0.6, 3.0);
         }

         void galaxy(SkyGen.C c, double x, double y, double z, double[] g, double[][] f, double R, double inc, double K, double seed) {
            double cosA = x * g[0] + y * g[1] + z * g[2];
            if (!(cosA < Math.cos(R * 1.6))) {
               double px = x * f[0][0] + y * f[0][1] + z * f[0][2];
               double py = x * f[1][0] + y * f[1][1] + z * f[1][2];
               double a = px / R;
               double bq = py / (R * inc);
               double r = Math.sqrt(a * a + bq * bq) + 1.0E-9;
               if (!(r > 1.6)) {
                  double th = Math.atan2(bq, a);
                  double warp = SkyGen.fbmL(a * 3.0 + seed, bq * 3.0, 1.7 + seed, 4.0);
                  double phase = th - Math.log(r) / 0.32 + warp * 0.9;
                  double arm = Math.pow(0.5 + 0.5 * Math.cos(2.0 * phase), 2.5);
                  double armD = Math.pow(0.5 + 0.5 * Math.cos(2.0 * (phase + 0.45)), 6.0);
                  double disk = Math.exp(-r / 0.3) * SkyGen.smooth(1.4, 0.6, r);
                  double bulge = Math.exp(-(r * r) / 0.006) * 3.0 + Math.exp(-r / 0.06) * 1.0;
                  double grain = 0.75 + 0.5 * SkyGen.fbmL(a * 25.0 + seed, bq * 25.0, 3.3, 3.0);
                  double lumA = disk * (0.25 + 1.4 * arm * grain);
                  double knots = SkyGen.smooth(0.25, 0.55, SkyGen.fbmL(a * 14.0, bq * 14.0 + seed, 7.1, 3.0)) * arm * disk * 2.0;
                  double dust = armD * SkyGen.smooth(0.05, 0.25, r) * SkyGen.smooth(1.1, 0.4, r) * 0.75;
                  double k = 0.45 * K;
                  c.r = c.r + k * (armC[0] * lumA + bulgeC[0] * bulge + hii[0] * knots * 0.5);
                  c.g = c.g + k * (armC[1] * lumA + bulgeC[1] * bulge + hii[1] * knots * 0.5);
                  c.b = c.b + k * (armC[2] * lumA + bulgeC[2] * bulge + hii[2] * knots * 0.5);
                  double dd = 1.0 - dust;
                  c.r = c.r * dd + dustC[0] * dust * 0.2;
                  c.g = c.g * dd + dustC[1] * dust * 0.2;
                  c.b = c.b * dd + dustC[2] * dust * 0.2;
                  c.vis = c.vis * (1.0 - 0.6 * SkyGen.smooth(0.0, 0.6, disk + bulge * 0.3));
               }
            }
         }
      };
      s.stars = 70000;
      return s;
   }

   static SkyGen.Sky blackHole() {
      final double[] B = SkyGen.azel(0.0, 18.0);
      final double[][] fr = SkyGen.frame(B, -8.0);
      double rs = 0.1;
      double E = 0.19;
      final double inc = Math.cos(Math.toRadians(76.0));
      final double[] mwN = SkyGen.norm(0.1, 0.5, 0.86);
      final double[] neb = SkyGen.rgb(2763386);
      final double[] neb2 = SkyGen.rgb(6957658);
      final double[] bg = SkyGen.rgb(65798);
      final double[] hot = SkyGen.rgb(16774368);
      final double[] warm = SkyGen.rgb(16752704);
      final double[] cool = SkyGen.rgb(12638463);
      SkyGen3.Space s = new SkyGen3.Space() {
         void background(double x, double y, double z, SkyGen.C c) {
            SkyGen.set(c, bg);
            SkyGen3.milky(c, x, y, z, mwN, SkyGen3.coreOn(mwN, B), 0.04);
            SkyGen.add(c, neb, 0.15 * SkyGen.smooth(0.0, 0.8, SkyGen.fbmL(x * 2.2 + 5.0, y * 2.2, z * 2.2, 5.0)));
            SkyGen.add(c, neb2, 0.1 * SkyGen.smooth(0.2, 0.9, SkyGen.fbmL(x * 3.1, y * 3.1 + 5.0, z * 3.1, 5.0)));
         }

         @Override
         void sky(double x, double y, double z, SkyGen.C c) {
            double cosA = x * B[0] + y * B[1] + z * B[2];
            double px = x * fr[0][0] + y * fr[0][1] + z * fr[0][2];
            double py = x * fr[1][0] + y * fr[1][1] + z * fr[1][2];
            double th = Math.acos(Math.max(-1.0, Math.min(1.0, cosA)));
            double pl = Math.sqrt(px * px + py * py) + 1.0E-12;
            double ux = px / pl;
            double uy = py / pl;
            double q = th / 0.1;
            if (q > 1.0) {
               double th2 = th - 0.0361 / th;
               double st = Math.sin(th2);
               double ct = Math.cos(th2);
               double dx = B[0] * ct + (fr[0][0] * ux + fr[1][0] * uy) * st;
               double dy = B[1] * ct + (fr[0][1] * ux + fr[1][1] * uy) * st;
               double dz = B[2] * ct + (fr[0][2] * ux + fr[1][2] * uy) * st;
               this.background(dx, dy, dz, c);
            } else {
               c.r = c.g = c.b = 0.0;
            }

            c.vis = c.vis * SkyGen.smooth(1.6, 3.0, q);
            double shadow = SkyGen.smooth(1.03, 0.99, q);
            SkyGen.mul(c, 1.0 - shadow);
            double pr = (q - 1.06) / 0.035;
            double ring = Math.exp(-pr * pr);
            double lq = (q - 1.3) / (0.16 + 0.22 * Math.abs(uy));
            double arc = Math.exp(-lq * lq) * (0.35 + 0.65 * uy * uy) * SkyGen.smooth(1.02, 1.12, q);
            double beamL = 1.0 + 0.4 * ux;
            double texA = 0.6 + 0.4 * SkyGen.fbmL(ux * 4.0, uy * 4.0, q * 6.0, 3.0);
            double hk = 1.5 * arc * texA * beamL * beamL + 2.2 * ring * beamL;
            double tr = hot[0] * 0.6 + warm[0] * 0.4;
            double tg = hot[1] * 0.6 + warm[1] * 0.4;
            double tb = hot[2] * 0.6 + warm[2] * 0.4;
            c.r += tr * hk;
            c.g += tg * hk;
            c.b += tb * hk;
            double a = px / 0.1;
            double bq = py / (0.1 * inc);
            double rd = Math.sqrt(a * a + bq * bq);
            boolean front = py < 0.0;
            if (rd > 2.2 && rd < 7.5 && (front || q > 1.02)) {
               double phi = Math.atan2(bq, a);
               double swirl = phi + 3.0 / Math.sqrt(rd);
               double tex = 0.45 + 0.55 * SkyGen.smooth(-0.4, 0.5, SkyGen.fbmL(Math.cos(swirl) * rd * 0.9, Math.sin(swirl) * rd * 0.9, rd * 0.4, 5.0));
               double streak = 0.75 + 0.25 * SkyGen.noise(rd * 3.5, 0.3, 0.7);
               double prof = Math.pow(2.6 / rd, 2.0) * SkyGen.smooth(2.2, 2.9, rd) * SkyGen.smooth(7.5, 4.5, rd);
               double dop = 1.0 + 0.4 * (a / rd);
               double I = prof * tex * streak * dop * dop * dop * 1.6;
               double tt = SkyGen.smooth(2.5, 7.5, rd);
               double cr = SkyGen.mix(hot[0], warm[0], tt);
               double cg = SkyGen.mix(hot[1], warm[1], tt);
               double cb = SkyGen.mix(hot[2], warm[2], tt);
               double bl = SkyGen.clamp01((dop - 1.0) * 0.8);
               cr = SkyGen.mix(cr, cool[0], bl * 0.5);
               cg = SkyGen.mix(cg, cool[1], bl * 0.5);
               cb = SkyGen.mix(cb, cool[2], bl * 0.5);
               double thick = SkyGen.smooth(1.0, 0.0, Math.abs(bq) / Math.max(1.0E-6, rd) * 0.0);
               double al = SkyGen.clamp01(I * 1.4) * thick;
               c.r = c.r * (1.0 - al * 0.5) + cr * I;
               c.g = c.g * (1.0 - al * 0.5) + cg * I;
               c.b = c.b * (1.0 - al * 0.5) + cb * I;
               c.vis *= 1.0 - al;
            }

            SkyGen.add(c, warm, 0.025 * Math.exp(-Math.max(0.0, q - 1.0) / 1.2) * SkyGen.smooth(0.97, 1.05, q));
         }

         @Override
         double starDensity(double x, double y, double z) {
            double cosA = x * B[0] + y * B[1] + z * B[2];
            double th = Math.acos(Math.max(-1.0, Math.min(1.0, cosA)));
            return (0.3 + 0.5 * SkyGen3.milkyDensity(x, y, z, mwN)) * SkyGen.smooth(0.15000000000000002, 0.30000000000000004, th);
         }
      };
      s.stars = 80000;
      s.detail = 0.85;
      return s;
   }

   static SkyGen.Sky voidSky() {
      final double[] bg = SkyGen.rgb(393740);
      final double[] v1 = SkyGen.rgb(5906058);
      final double[] v2 = SkyGen.rgb(12603647);
      final double[] v3 = SkyGen.rgb(1706554);
      final double[] eye = SkyGen.rgb(14725375);
      SkyGen3.Space s = new SkyGen3.Space() {
         @Override
         void sky(double x, double y, double z, SkyGen.C c) {
            SkyGen.set(c, bg);
            double wx = SkyGen.fbmL(x * 1.5 + 2.0, y * 1.5, z * 1.5, 3.0);
            double wy = SkyGen.fbmL(x * 1.5, y * 1.5 + 6.0, z * 1.5, 3.0);
            double wz = SkyGen.fbmL(x * 1.5, y * 1.5, z * 1.5 + 4.0, 3.0);
            double X = x + wx * 0.9;
            double Y = y + wy * 0.9;
            double Z = z + wz * 0.9;
            double n = SkyGen.fbmL(X * 2.5, Y * 2.5, Z * 2.5, 6.0);
            double r = SkyGen.ridged(X * 3.5 + 1.0, Y * 3.5, Z * 3.5, 5);
            double swirl = SkyGen.smooth(0.0, 0.6, n);
            double fil = SkyGen.smooth(0.6, 0.95, r) * swirl;
            SkyGen.add(c, v3, 0.5 * swirl);
            SkyGen.add(c, v1, 0.35 * swirl * swirl + 0.25 * fil);
            SkyGen.add(c, v2, 0.5 * fil * fil);
            double below = SkyGen.smooth(0.2, -0.9, y);
            SkyGen.add(c, v1, 0.07 * below);
            double[] E = SkyGen3.EYE;
            double cosA = x * E[0] + y * E[1] + z * E[2];
            double a2 = 2.0 * (1.0 - cosA);
            SkyGen.add(c, eye, 0.9 * Math.exp(-a2 / 0.0015) + 0.1 * Math.exp(-a2 / 0.03));
         }

         @Override
         void moteColor(Random r, double[] o) {
            double t = r.nextDouble();
            if (t < 0.7) {
               o[0] = 0.9;
               o[1] = 0.5;
               o[2] = 1.3;
            } else {
               o[0] = 0.6;
               o[1] = 0.9;
               o[2] = 1.2;
            }
         }

         @Override
         double moteDensity(double x, double y, double z) {
            return 1.0;
         }
      };
      s.stars = 30000;
      s.starMax = 1.6;
      s.motes = 700;
      s.moteBright = 0.35;
      s.moteSize = 1.2;
      return s;
   }

   static double[] coreOn(double[] N, double[] hint) {
      double d = hint[0] * N[0] + hint[1] * N[1] + hint[2] * N[2];
      return SkyGen.norm(hint[0] - N[0] * d, hint[1] - N[1] * d, hint[2] - N[2] * d);
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

      Body(double[] dir, double rad, double roll) {
         this.d = dir;
         this.rad = rad;
         this.sinR = Math.sin(rad);
         double[][] f = SkyGen.frame(dir, roll);
         this.ex = f[0];
         this.ey = f[1];
         this.cosOut = Math.cos(Math.min(Math.PI, rad * 14.0));
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
      void sky(double x, double y, double z, SkyGen.C c) {
         double yy = y > 0.0 ? y : 0.0;
         double t = Math.exp(-yy / this.hW);
         c.r = this.zen[0] + (this.hor[0] - this.zen[0]) * t;
         c.g = this.zen[1] + (this.hor[1] - this.zen[1]) * t;
         c.b = this.zen[2] + (this.hor[2] - this.zen[2]) * t;
         if (this.air != null) {
            SkyGen.add(c, this.air, this.airK * Math.exp(-yy / this.airW));
         }

         if (this.lp != null) {
            double hl = Math.sqrt(x * x + z * z) + 1.0E-9;
            double a = (x * this.lpDir[0] + z * this.lpDir[2]) / hl;
            a = Math.max(0.0, a);
            SkyGen.add(c, this.lp, this.lpK * a * a * a * Math.exp(-yy / 0.1));
         }

         if (this.mwN != null) {
            SkyGen3.milky(c, x, y, z, this.mwN, this.mwCore, this.mwK);
         }

         if (this.au != null) {
            SkyGen3.aurora(c, x, y, z, this.au);
         }

         this.extra(x, y, z, c);
         if (this.moon != null) {
            if (this.halo22 > 0.0) {
               double cosA = x * this.moon.d[0] + y * this.moon.d[1] + z * this.moon.d[2];
               double ang = Math.acos(Math.max(-1.0, Math.min(1.0, cosA)));
               double q = (ang - 0.384) / 0.016;
               double hh = Math.exp(-q * q) * this.halo22 + Math.exp(-Math.max(0.0, ang - 0.384) / 0.12) * this.halo22 * 0.25 * SkyGen.smooth(0.36, 0.4, ang);
               c.r = c.r + hh * (1.1 - 0.25 * SkyGen.smooth(-1.0, 1.0, q));
               c.g += hh;
               c.b = c.b + hh * (0.85 + 0.35 * SkyGen.smooth(-1.0, 1.0, q));
            }

            SkyGen3.moon(c, x, y, z, this.moon, this.moonTint, this.moonBright, this.moonLight);
         }

         if (this.c1 != null) {
            SkyGen.clouds(c, x, y, z, this.c1, this.moon != null ? this.moon.d : this.sun, this.cloudLight);
         }

         if (this.mist != null) {
            SkyGen.mixTo(c, this.mist, this.mistK * Math.exp(-yy / this.mistW));
         }

         c.vis = c.vis * SkyGen.smooth(-0.01, 0.2, y);
      }

      void extra(double x, double y, double z, SkyGen.C c) {
      }

      @Override
      double starDensity(double x, double y, double z) {
         double h = SkyGen.smooth(-0.02, 0.3, y);
         return this.mwN == null ? h * 0.35 * this.starDens : h * Math.min(1.0, (0.25 + 0.75 * SkyGen3.milkyDensity(x, y, z, this.mwN)) * this.starDens);
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
      double starDensity(double x, double y, double z) {
         return 0.4;
      }
   }
}
