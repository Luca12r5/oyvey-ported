package dev.lego.cosmetic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.geom.Path2D;
import java.awt.geom.Ellipse2D.Double;
import java.util.Random;

final class HdTex {
   private HdTex() {
   }

   static double frac(double v) {
      return v - Math.floor(v);
   }

   static CTex.T paint(String name) {
      switch (name) {
         case "hd_felt":
            return Tex2.pix(128, 128, (u, v, x, y) -> {
               double n = Capes.fbm(u * 12.0, v * 12.0, 12, 12, 4, 1001);
               double fib = Capes.fbm(u * 64.0, v * 64.0, 64, 64, 2, 1002);
               return 0xFF000000 | Capes.shade(16777215, 0.8 + 0.12 * n + 0.08 * fib + 0.03 * (Capes.hash(x, y, 1003) - 0.5));
            });
         case "hd_velvet":
            return Tex2.pix(128, 128, (u, v, x, y) -> {
               double n = Capes.fbm(u * 8.0, v * 8.0, 8, 8, 3, 1011);
               double pile = Capes.fbm(u * 48.0, v * 48.0, 48, 48, 2, 1012);
               return 0xFF000000 | Capes.shade(16777215, 0.74 + 0.16 * n + 0.1 * pile);
            });
         case "hd_cotton":
            return Tex2.pix(128, 128, (u, v, x, y) -> {
               boolean wx = (x / 2 + y / 2) % 2 == 0;
               double n = Capes.fbm(u * 10.0, v * 10.0, 10, 10, 3, 1021);
               return 0xFF000000 | Capes.shade(16777215, (wx ? 0.97 : 0.9) * (0.9 + 0.1 * n));
            });
         case "hd_knit":
            return Tex2.pix(128, 128, (u, v, x, y) -> {
               double cu = u * 24.0;
               double cv = v * 16.0;
               double fx = frac(cu) - 0.5;
               double fy = frac(cv + Math.abs(fx) * 0.9);
               double st = 0.72 + 0.28 * Math.sin(Math.PI * fy) * (1.0 - Math.abs(fx) * 1.2);
               double gap = Math.abs(fx) > 0.44 ? 0.8 : 1.0;
               return 0xFF000000 | Capes.shade(16777215, st * gap * (0.94 + 0.08 * Capes.fbm(u * 6.0, v * 6.0, 6, 6, 2, 1031)));
            });
         case "hd_fur":
            return Tex2.pix(128, 128, (u, v, x, y) -> {
               double s = Capes.fbm(u * 40.0, v * 5.0, 40, 5, 3, 1041);
               double c = Capes.fbm(u * 10.0, v * 10.0, 10, 10, 3, 1042);
               return 0xFF000000 | Capes.shade(16777215, 0.76 + 0.2 * s + 0.08 * c);
            });
         case "hd_fluff":
            return Tex2.pix(128, 128, (u, v, x, y) -> {
               double n = Capes.fbm(u * 16.0, v * 16.0, 16, 16, 4, 1051);
               double curl = 0.5 + 0.5 * Math.sin(u * 50.0 + n * 8.0 + Math.cos(v * 50.0 + n * 7.0));
               return 0xFF000000 | Capes.shade(16777215, 0.8 + 0.12 * n + 0.08 * curl);
            });
         case "hd_satin":
            return Tex2.pix(64, 128, (u, v, x, y) -> {
               double s = 0.72 + 0.22 * Math.pow(Math.max(0.0, Math.sin(u * Math.PI * 2.0 + v * 3.0)), 4.0) + 0.06 * Math.sin(v * 50.0);
               return 0xFF000000 | Capes.shade(16777215, Math.min(1.0, s));
            });
         case "hd_leather":
            return Tex2.pix(128, 128, (u, v, x, y) -> {
               double n = Capes.fbm(u * 20.0, v * 20.0, 20, 20, 4, 1061);
               double cell = Math.abs(Capes.fbm(u * 32.0, v * 32.0, 32, 32, 2, 1062) - 0.5);
               double crease = cell < 0.03 ? 0.86 : 1.0;
               double scr = Capes.fbm(u * 3.0, v * 3.0, 3, 3, 3, 1063);
               return 0xFF000000 | Capes.shade(16777215, (0.8 + 0.14 * n + 0.08 * scr) * crease);
            });
         case "hd_stitch":
            return Tex2.pix(128, 16, (u, v, x, y) -> {
               boolean dash = x % 8 < 5 && Math.abs(v - 0.5) < 0.22;
               double base = 0.72 + 0.1 * Capes.fbm(u * 16.0, v, 16, 1, 2, 1071);
               return 0xFF000000 | (dash ? 16049864 : Capes.shade(16777215, base));
            });
         case "hd_gold":
            return metal(16774856, 15778378, 11565082, 5912072, 1081);
         case "hd_silver":
            return metal(16777215, 13949668, 9082022, 3818064, 1082);
         case "hd_steel":
            return Tex2.pix(128, 128, (u, v, x, y) -> {
               double br = Capes.fbm(u * 2.0, v * 90.0, 2, 90, 2, 1091);
               double env = 0.5 + 0.5 * Math.sin(v * Math.PI * 2.0 * 1.5 + 0.8);
               return 0xFF000000 | Capes.shade(16777215, 0.62 + 0.26 * env + 0.12 * br);
            });
         case "hd_chrome":
            return Tex2.pix(64, 128, (u, v, x, y) -> {
               double h = 1.0 - v;
               int c = h > 0.52 ? Capes.mix(12572927, 16777215, (h - 0.52) / 0.48) : Capes.ramp(h / 0.52, 2764342, 6975612, 15265012);
               return 0xFF000000 | Capes.add(c, 16777215, Math.exp(-Math.pow((h - 0.55) / 0.02, 2.0)) * 0.6);
            });
         case "hd_plastic":
            return Tex2.pix(
               64,
               64,
               (u, v, x, y) -> 0xFF000000
                  | Capes.shade(16777215, 0.9 + 0.06 * Capes.fbm(u * 4.0, v * 4.0, 4, 4, 2, 1101) + 0.02 * (Capes.hash(x, y, 1102) - 0.5))
            );
         case "hd_rubber":
            return Tex2.pix(64, 64, (u, v, x, y) -> 0xFF000000 | Capes.shade(16777215, 0.78 + 0.1 * Capes.fbm(u * 24.0, v * 24.0, 24, 24, 2, 1111)));
         case "hd_gem":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double a = Math.atan2(v - 0.5, u - 0.5);
               double d = Math.hypot(u - 0.5, v - 0.5);
               int sec = (int)Math.floor((a + Math.PI) / (Math.PI / 4)) + (d > 0.3 ? 8 : 0);
               double b = 0.62 + 0.38 * Capes.hash(sec, 3, 1121);
               double sp = Math.exp(-(Math.pow(u - 0.34, 2.0) + Math.pow(v - 0.3, 2.0)) / 0.003);
               return 0xFF000000 | Capes.add(Capes.shade(16777215, b), 16777215, sp);
            });
         case "hd_glass":
            return Tex2.pix(
               64,
               64,
               (u, v, x, y) -> {
                  double streak = Math.exp(-Math.pow((u * 1.2 + v * 0.8 - 0.55) / 0.06, 2.0)) * 0.6
                     + Math.exp(-Math.pow((u * 1.2 + v * 0.8 - 0.78) / 0.025, 2.0)) * 0.5;
                  return Tex2.argb(Capes.add(12577023, 16777215, streak), 0.2 + streak * 0.7);
               }
            );
         case "hd_lens":
            return Tex2.pix(
               128,
               64,
               (u, v, x, y) -> {
                  int c = Capes.ramp(v, 3820154, 1448490, 657938, 2759226);
                  double streak = Math.exp(-Math.pow((u * 0.9 - v * 0.55 - 0.08) / 0.05, 2.0)) * 0.55
                     + Math.exp(-Math.pow((u * 0.9 - v * 0.55 - 0.22) / 0.018, 2.0)) * 0.4;
                  double top = Math.exp(-Math.pow(v / 0.18, 2.0)) * 0.25;
                  return Tex2.argb(Capes.add(c, 16777215, streak + top), 0.93);
               }
            );
         case "hd_bone":
            return Tex2.pix(64, 128, (u, v, x, y) -> {
               double n = Capes.fbm(u * 6.0, v * 14.0, 6, 14, 3, 1131);
               int c = Capes.mix(16051416, 13154458, v * 0.6 + n * 0.3);
               return 0xFF000000 | Capes.shade(c, 0.9 + 0.12 * Math.sin(v * Math.PI * 2.0 * 9.0 + n * 3.0));
            });
         case "hd_horn":
            return Tex2.pix(64, 256, (u, v, x, y) -> {
               double n = Capes.fbm(u * 6.0, v * 30.0, 6, 30, 3, 1141);
               double ring = 0.82 + 0.18 * Math.pow(Math.abs(Math.sin(v * Math.PI * 14.0 + n * 1.5)), 0.6);
               int c = Capes.ramp(v, 2753542, 6950418, 11804702, 15750190);
               return 0xFF000000 | Capes.add(Capes.shade(c, ring * (0.85 + 0.25 * n)), 16760970, Math.pow(v, 6.0) * 0.25);
            });
         case "hd_ivory":
            return Tex2.pix(64, 256, (u, v, x, y) -> {
               double n = Capes.fbm(u * 6.0, v * 24.0, 6, 24, 3, 1151);
               double ring = 0.86 + 0.14 * Math.pow(Math.abs(Math.sin(v * Math.PI * 10.0 + n * 1.2)), 0.5);
               int c = Capes.ramp(v, 9073232, 14207138, 16774880);
               return 0xFF000000 | Capes.shade(c, ring);
            });
         case "hd_wood":
            return Tex2.pix(128, 128, (u, v, x, y) -> {
               double n = Capes.fbm(u * 3.0, v * 14.0, 3, 14, 4, 1161);
               double gr = 0.5 + 0.5 * Math.sin((u * 26.0 + n * 7.0) * Math.PI);
               return 0xFF000000 | Capes.shade(Capes.ramp(gr, 6961690, 10641966, 13142602), 0.9 + 0.12 * n);
            });
         case "hd_maple":
            return Tex2.pix(128, 256, (u, v, x, y) -> {
               double n = Capes.fbm(u * 4.0, v * 30.0, 4, 30, 3, 1171);
               double flame = 0.5 + 0.5 * Math.sin(v * 120.0 + n * 9.0);
               double d = Math.hypot((u - 0.5) * 1.6, (v - 0.55) * 1.1);
               int base = Capes.mix(16756778, 14700570, Capes.smooth(0.2, 0.55, d));
               base = Capes.mix(base, 2755078, Capes.smooth(0.45, 0.72, d));
               return 0xFF000000 | Capes.shade(base, 0.86 + 0.18 * flame);
            });
         case "hd_brick":
            return Tex2.pix(
               64,
               64,
               (u, v, x, y) -> 0xFF000000
                  | Capes.shade(16777215, 0.93 + 0.04 * Capes.fbm(u * 3.0, v * 3.0, 3, 3, 2, 1181) + (Capes.hash(x, y, 1182) > 0.995 ? -0.06 : 0.0))
            );
         case "hd_lego_logo":
            return Tex2.img(64, 64, g -> {
               g.setColor(Color.WHITE);
               g.fillRect(0, 0, 64, 64);
               g.setColor(new Color(13158600));
               g.setFont(new Font("SansSerif", 3, 15));
               g.drawString("LEGO", 12, 38);
            });
         case "hd_petal":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double vein = Math.exp(-Math.pow((u - 0.5) / 0.03, 2.0)) * 0.12 + 0.05 * Math.sin(u * 40.0) * (1.0 - v);
               double base = 0.72 + 0.28 * v;
               return 0xFF000000 | Capes.shade(16777215, Math.min(1.0, base - vein + 0.06 * Capes.fbm(u * 8.0, v * 8.0, 8, 8, 2, 1191)));
            });
         case "hd_leaf":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double mid = Math.exp(-Math.pow((v - 0.5) / 0.03, 2.0));
               double side = Math.exp(-Math.pow(frac(u * 5.0 + Math.abs(v - 0.5) * 3.0) - 0.5, 2.0) / 0.004) * 0.15;
               int c = Capes.mix(3050036, 8047444, 0.3 + 0.5 * Capes.fbm(u * 5.0, v * 5.0, 5, 5, 3, 1201));
               return 0xFF000000 | Capes.add(Capes.shade(c, 0.95 - side), 13168800, mid * 0.5);
            });
         case "hd_leafw":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double mid = Math.exp(-Math.pow((v - 0.5) / 0.03, 2.0));
               double side = Math.exp(-Math.pow(frac(u * 5.0 + Math.abs(v - 0.5) * 3.0) - 0.5, 2.0) / 0.004) * 0.15;
               double n = Capes.fbm(u * 5.0, v * 5.0, 5, 5, 3, 1202);
               return 0xFF000000 | Capes.add(Capes.shade(16777215, 0.78 + 0.18 * n - side), 16777215, mid * 0.25);
            });
         case "hd_vine":
            return Tex2.pix(
               64,
               32,
               (u, v, x, y) -> 0xFF000000
                  | Capes.shade(Capes.mix(3828258, 6986298, Capes.fbm(u * 10.0, v * 4.0, 10, 4, 3, 1211)), 0.85 + 0.15 * Math.sin(u * 60.0))
            );
         case "hd_mush":
            return Tex2.pix(256, 128, (u, v, x, y) -> {
               double n = Capes.fbm(u * 10.0, v * 6.0, 10, 6, 3, 1221);
               return 0xFF000000 | Capes.shade(Capes.mix(15741994, 10097168, Math.pow(v, 1.5) * 0.8), 0.9 + 0.12 * n);
            });
         case "hd_gill":
            return Tex2.pix(256, 32, (u, v, x, y) -> 0xFF000000 | Capes.shade(15787730, 0.78 + 0.22 * Math.abs(Math.sin(u * Math.PI * 64.0)) - 0.2 * v));
         case "hd_party":
            return Tex2.img(256, 128, g -> {
               g.setColor(new Color(2799856));
               g.fillRect(0, 0, 256, 128);
               int[] cs = new int[]{16726666, 16769610, 8014591, 3858554};
               g.setStroke(new BasicStroke(12.0F));

               for (int i = -4; i < 12; i++) {
                  g.setColor(new Color(cs[(i + 8) % 4]));
                  g.drawLine(i * 32, 128, i * 32 + 96, 0);
               }

               Random r = new Random(1231L);

               for (int i = 0; i < 70; i++) {
                  g.setColor(i % 2 == 0 ? Color.WHITE : new Color(16773258));
                  double s = 3.0 + r.nextDouble() * 4.0;
                  g.fill(new Double(r.nextDouble() * 256.0, r.nextDouble() * 128.0, s, s));
               }
            });
         case "hd_wizard":
            return Tex2.img(256, 256, g -> {
               g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(3812016), 0.0F, 256.0F, new Color(1708128)));
               g.fillRect(0, 0, 256, 256);
               Random r = new Random(1241L);

               for (int i = 0; i < 26; i++) {
                  double x = r.nextDouble() * 256.0;
                  double y = 20.0 + r.nextDouble() * 220.0;
                  double s = 7.0 + r.nextDouble() * 10.0;
                  g.setColor(new Color(16767050));
                  if (i % 3 == 0) {
                     Path2D m = new java.awt.geom.Path2D.Double();
                     m.append(new Double(x - s, y - s, s * 2.0, s * 2.0), false);
                     g.fill(m);
                     g.setColor(new Color(2760330));
                     g.fill(new Double(x - s * 0.55, y - s * 1.1, s * 2.0, s * 2.0));
                  } else {
                     g.fill(XmasTex.starShape(x, y, s, s * 0.42, 5));
                  }
               }

               for (int ix = 0; ix < 200; ix++) {
                  g.setColor(Capes.col(16777215, 0.3 + r.nextDouble() * 0.5));
                  g.fill(new Double(r.nextDouble() * 256.0, r.nextDouble() * 256.0, 1.4, 1.4));
               }
            });
         case "hd_witchband":
            return Tex2.img(256, 32, g -> {
               g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(5955706), 0.0F, 32.0F, new Color(1735226)));
               g.fillRect(0, 0, 256, 32);
               g.setColor(Capes.col(0, 0.25));

               for (int i = 0; i < 256; i += 4) {
                  g.drawLine(i, 0, i, 32);
               }

               g.setColor(Capes.col(16777215, 0.35));
               g.fillRect(0, 3, 256, 2);
            });
         case "hd_hatband":
            return Tex2.img(256, 32, g -> {
               g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(14692410), 0.0F, 32.0F, new Color(7997970)));
               g.fillRect(0, 0, 256, 32);
               g.setColor(Capes.col(0, 0.22));

               for (int i = 0; i < 32; i += 3) {
                  g.drawLine(0, i, 256, i);
               }

               g.setColor(Capes.col(16777215, 0.3));
               g.fillRect(0, 4, 256, 2);
            });
         case "hd_teddyface":
            return Tex2.img(128, 128, g -> {
               g.setColor(new Color(11565640));
               g.fillRect(0, 0, 128, 128);
               g.setColor(new Color(1707528));
               g.fill(new Double(32.0, 40.0, 16.0, 18.0));
               g.fill(new Double(80.0, 40.0, 16.0, 18.0));
               g.setColor(Color.WHITE);
               g.fill(new Double(36.0, 43.0, 6.0, 6.0));
               g.fill(new Double(84.0, 43.0, 6.0, 6.0));
               g.setColor(Capes.col(16743066, 0.5));
               g.fill(new Double(20.0, 64.0, 20.0, 12.0));
               g.fill(new Double(88.0, 64.0, 20.0, 12.0));
            });
         case "hd_target":
            return Tex2.img(256, 256, g -> {
               g.setColor(new Color(1985208));
               g.fillRect(0, 0, 256, 256);
               g.setColor(new Color(15921910));
               g.fillRect(0, 0, 128, 128);
               g.fillRect(128, 128, 128, 128);
               g.setColor(Capes.col(0, 0.15));
               Random r = new Random(1251L);

               for (int i = 0; i < 400; i++) {
                  g.fill(new Double(r.nextDouble() * 256.0, r.nextDouble() * 256.0, 2.0, 2.0));
               }
            });
         case "hd_rocket":
            return Tex2.img(128, 256, g -> {
               g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(16777215), 128.0F, 0.0F, new Color(15001838)));
               g.fillRect(0, 0, 128, 256);
               g.setColor(Capes.col(0, 0.18));
               g.setStroke(new BasicStroke(1.4F));

               for (int y = 30; y < 256; y += 46) {
                  g.drawLine(0, y, 128, y);
               }

               for (int y = 12; y < 256; y += 23) {
                  for (int x = 4; x < 128; x += 16) {
                     g.fill(new Double(x, y, 2.4, 2.4));
                  }
               }

               g.setColor(new Color(14163496));
               g.fillRect(0, 200, 128, 14);
            });
         case "hd_note":
            return Tex2.img(64, 64, g -> {
               g.setColor(Color.WHITE);
               g.fillRect(0, 0, 64, 64);
            });
         case "hd_rune":
            return Tex2.img(512, 64, g -> {
               g.setColor(new Color(0, 0, 0, 0));
               g.fillRect(0, 0, 512, 64);
               g.setStroke(new BasicStroke(3.2F, 1, 1));
               Random r = new Random(1261L);

               for (int i = 0; i < 16; i++) {
                  double cx = i * 32 + 16;
                  Path2D p = new java.awt.geom.Path2D.Double();
                  int strokes = 2 + r.nextInt(3);

                  for (int k = 0; k < strokes; k++) {
                     p.moveTo(cx + (r.nextDouble() - 0.5) * 18.0, 12.0 + r.nextDouble() * 10.0);
                     p.lineTo(cx + (r.nextDouble() - 0.5) * 18.0, 36.0 + r.nextDouble() * 16.0);
                  }

                  p.moveTo(cx - 9.0, 32.0);
                  p.lineTo(cx + 9.0, 26.0 + r.nextDouble() * 12.0);
                  g.setColor(Capes.col(13148415, 0.35));
                  g.setStroke(new BasicStroke(8.0F, 1, 1));
                  g.draw(p);
                  g.setColor(Color.WHITE);
                  g.setStroke(new BasicStroke(3.0F, 1, 1));
                  g.draw(p);
               }
            });
         case "hd_bubble":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double h = frac(u * 2.0 + v * 1.3 + 0.2 * Math.sin(v * 9.0));
               int c = Capes.hsv(h, 0.45, 1.0);
               double spec = Math.exp(-(Math.pow(u - 0.3, 2.0) + Math.pow(v - 0.3, 2.0)) / 0.004);
               return Tex2.argb(Capes.add(c, 16777215, spec), 0.28 + spec * 0.7);
            });
         case "hd_softglow":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double d = Math.hypot(u - 0.5, v - 0.5) * 2.0;
               return Tex2.argb(16777215, Math.pow(Math.max(0.0, 1.0 - d), 2.2));
            });
         case "hd_trail":
            return Tex2.pix(128, 16, (u, v, x, y) -> Tex2.argb(16777215, Math.pow(u, 1.6) * Math.pow(Math.sin(Math.PI * v), 1.5)));
         case "hd_gloss":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double d = Math.hypot(u - 0.32, v - 0.3);
               return 0xFF000000 | Capes.add(Capes.shade(16777215, 0.8 + 0.2 * (1.0 - v)), 16777215, Math.exp(-d * d / 0.004) * 0.8);
            });
         case "hd_foxear":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double n = Capes.fbm(u * 24.0, v * 4.0, 24, 4, 3, 1311);
               double tip = Capes.smooth(0.42, 0.18, v + (n - 0.5) * 0.12);
               int c = Capes.mix(Capes.mix(15759912, 14177818, v), 2759188, tip);
               return 0xFF000000 | Capes.shade(c, 0.82 + 0.25 * n);
            });
         case "hd_mustache":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double hair = Capes.fbm(u * 60.0, v * 4.0, 60, 4, 2, 1271);
               return 0xFF000000 | Capes.shade(16777215, 0.7 + 0.3 * hair);
            });
         case "hd_mask":
            return Tex2.img(128, 64, g -> {
               g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(16777215), 0.0F, 64.0F, new Color(14211296)));
               g.fillRect(0, 0, 128, 64);
               g.setColor(Capes.col(0, 0.1));
               Random r = new Random(1281L);

               for (int i = 0; i < 90; i++) {
                  double x = r.nextDouble() * 128.0;
                  double y = r.nextDouble() * 64.0;
                  g.draw(new Double(x, y, 5.0, 3.0));
               }

               g.setColor(Capes.col(16777215, 0.8));

               for (int i = 0; i < 40; i++) {
                  g.fill(new Double(r.nextDouble() * 128.0, r.nextDouble() * 64.0, 1.5, 1.5));
               }
            });
         case "hd_screen":
            return Tex2.pix(256, 64, (u, v, x, y) -> {
               double scan = y % 3 == 0 ? 0.75 : 1.0;
               boolean grid = x % 16 == 0 || y % 16 == 0;
               int c = Capes.mix(656918, 2755130, v);
               if (grid) {
                  c = Capes.add(c, 16726666, 0.18);
               }

               double edge = Math.exp(-Math.pow((v - 0.08) / 0.05, 2.0)) * 0.35;
               return 0xFF000000 | Capes.add(Capes.shade(c, scan), 16743088, edge);
            });
         case "hd_ninja":
            return Tex2.pix(128, 64, (u, v, x, y) -> {
               double n = Capes.fbm(u * 16.0, v * 8.0, 16, 8, 3, 1291);
               double fold = 0.9 + 0.1 * Math.sin(v * 30.0 + n * 4.0);
               return 0xFF000000 | Capes.shade(16777215, (0.78 + 0.16 * n) * fold);
            });
         case "hd_quiver":
            return Tex2.pix(128, 128, (u, v, x, y) -> {
               double n = Capes.fbm(u * 16.0, v * 16.0, 16, 16, 3, 1301);
               boolean band = Math.abs(v - 0.15) < 0.04 || Math.abs(v - 0.85) < 0.04;
               double tool = 0.5 + 0.5 * Math.sin(u * 60.0 + v * 30.0);
               int c = band ? 15251786 : Capes.mix(6961690, 9064488, n);
               if (!band && Math.abs(v - 0.5) < 0.2) {
                  c = Capes.shade(c, 0.85 + 0.15 * tool);
               }

               return 0xFF000000 | c;
            });
         default:
            return null;
      }
   }

   static CTex.T metal(int hi, int mid, int lo, int deep, int seed) {
      return Tex2.pix(128, 128, (u, v, x, y) -> {
         double env = 0.5 + 0.5 * Math.sin(v * Math.PI * 2.0 * 1.2 + Math.sin(u * Math.PI * 2.0) * 0.6);
         double scr = Capes.fbm(u * 3.0, v * 80.0, 3, 80, 2, seed);
         int c = Capes.ramp(Math.min(1.0, env * 0.85 + scr * 0.2), deep, lo, mid, hi);
         double spark = Capes.hash(x, y, seed + 1) > 0.997 ? 0.6 : 0.0;
         return 0xFF000000 | Capes.add(c, 16777215, spark + Math.exp(-Math.pow((env - 0.95) / 0.05, 2.0)) * 0.35);
      });
   }
}
