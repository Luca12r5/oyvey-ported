package dev.lego.cosmetic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;
import java.util.Random;

final class Cos3Tex {
   private Cos3Tex() {
   }

   static CTex.T paint(String name) {
      switch (name) {
         case "@skin":
            return skin();
         case "c3_beam":
            return Tex2.pix(8, 8, (u, v, x, y) -> Tex2.argb(16777215, 0.34));
         case "c3_metal":
            return Tex2.pix(
               64,
               64,
               (u, v, x, y) -> 0xFF000000
                  | Capes.shade(Capes.mix(16777215, 11844294, v * 0.8), 0.93 + 0.07 * Capes.hash(x / 7, y, 11) + 0.04 * Math.sin(u * 9.0))
            );
         case "c3_crystal":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double f = 0.5 + 0.5 * Math.sin(u * Math.PI * 2.0 * 3.0 + v * 2.0);
               int c = Capes.mix(16777215, 10134728, 0.25 + 0.55 * v * (0.6 + 0.4 * f));
               double streak = Math.exp(-Math.pow((u * 1.3 - v * 0.7 - 0.2) / 0.05, 2.0)) * 0.7;
               return 0xFF000000 | Capes.add(c, 16777215, streak);
            });
         case "c3_scale":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double sx = u * 8.0;
               double sy = v * 8.0;
               double row = Math.floor(sy);
               double cx = sx + (row % 2.0 == 0.0 ? 0.0 : 0.5);
               double fx = cx - Math.floor(cx) - 0.5;
               double fy = sy - row;
               double d = Math.hypot(fx, (fy - 0.1) * 1.1);
               double m = d < 0.5 ? 1.0 - d * 0.6 : 0.62;
               double rim = d > 0.42 && d < 0.5 ? 0.75 : 1.0;
               return 0xFF000000 | Capes.shade(15921906, m * rim * (0.95 + 0.08 * Capes.hash(x, y, 5)));
            });
         case "c3_fur":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double s = Capes.fbm(u * 24.0, v * 3.0, 24, 3, 3, 8);
               return 0xFF000000 | Capes.shade(16185078, 0.78 + 0.3 * s);
            });
         case "c3_plume":
            return Tex2.pix(32, 64, (u, v, x, y) -> {
               double barb = 0.85 + 0.15 * Math.sin(v * 30.0 + Math.abs(u - 0.5) * 12.0);
               double shaft = Math.abs(u - 0.5) < 0.05 ? 0.7 : 1.0;
               return 0xFF000000 | Capes.shade(16185078, barb * shaft);
            });
         case "c3_lacquer":
            return Tex2.pix(32, 128, (u, v, x, y) -> {
               double sh = Math.exp(-Math.pow((u - 0.3) / 0.12, 2.0)) * 0.5 + 0.55 + 0.1 * Math.sin(v * 3.0);
               return 0xFF000000 | Capes.shade(16777215, Math.min(1.0, sh));
            });
         case "c3_jet":
            return Tex2.img(64, 64, g -> {
               g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(16777215), 64.0F, 0.0F, new Color(12896464)));
               g.fillRect(0, 0, 64, 64);
               g.setColor(Capes.col(0, 0.25));
               g.setStroke(new BasicStroke(1.5F));
               g.drawLine(0, 20, 64, 20);
               g.drawLine(0, 44, 64, 44);

               for (int i = 0; i < 4; i++) {
                  g.fill(new Double(6 + i * 16, 24.0, 3.0, 3.0));
               }
            });
         case "c3_exhaust":
            return Tex2.pix(
               16, 64, (u, v, x, y) -> Tex2.argb(Capes.mix(16777215, 16777215, 0.0), Math.pow(1.0 - v, 1.6) * (0.75 + 0.25 * Math.sin(u * Math.PI * 6.0)))
            );
         case "c3_eye":
            return Tex2.img(32, 32, g -> {
               g.setColor(new Color(1315866));
               g.fillRect(0, 0, 32, 32);
               g.setColor(Color.WHITE);
               g.fill(new Double(5.0, 5.0, 11.0, 11.0));
               g.fill(new Double(19.0, 19.0, 5.0, 5.0));
            });
         case "c3_grille":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double dx = x % 6 - 2.5;
               double dy = y % 6 - 2.5;
               return 0xFF000000 | (dx * dx + dy * dy < 3.5 ? 657934 : Capes.shade(3027000, 0.9 + 0.2 * v));
            });
         case "c3_eq":
            return Tex2.pix(256, 32, (u, v, x, y) -> {
               int bar = x / 8;
               double hgt = 0.3 + 0.7 * Capes.hash(bar, 1, 9);
               boolean on = 1.0 - v < hgt && x % 8 < 6 && y % 4 < 3;
               int c = Capes.hsv(u, 0.85, 1.0);
               return on ? 0xFF000000 | c : Tex2.argb(c, 0.18);
            });
         case "c3_band_bolt":
            return band(1222730, 678442, 12124010, 0);
         case "c3_band_fire":
            return band(13115418, 6949384, 16756778, 1);
         case "c3_band_ice":
            return band(2783960, 929386, 14219007, 2);
         case "c3_planet_gas":
            return Tex2.pix(128, 64, (u, v, x, y) -> {
               double w = Capes.fbm(u * 6.0, v * 3.0, 6, 0, 3, 21) - 0.5;
               double b = Math.sin((v + w * 0.12) * Math.PI * 9.0);
               int c = Capes.ramp(0.5 + 0.5 * b, 12083242, 15249520, 16773328);
               double spot = Math.exp(-(Math.pow((u - 0.3) / 0.06, 2.0) + Math.pow((v - 0.62) / 0.05, 2.0)));
               return 0xFF000000 | Capes.mix(c, 13123610, spot);
            });
         case "c3_planet_ice":
            return Tex2.pix(128, 64, (u, v, x, y) -> {
               double n = Capes.fbm(u * 8.0, v * 4.0, 8, 0, 4, 33);
               int c = Capes.ramp(n, 1723034, 3840744, 10152191, 16777215);
               if (v < 0.12 || v > 0.88) {
                  c = Capes.mix(c, 16777215, 0.8);
               }

               return 0xFF000000 | c;
            });
         case "c3_planet_lava":
            return Tex2.pix(128, 64, (u, v, x, y) -> {
               double n = Capes.fbm(u * 10.0, v * 5.0, 10, 0, 4, 44);
               double crack = Math.exp(-Math.pow((n - 0.5) / 0.035, 2.0));
               int c = Capes.mix(2757648, 4858904, n);
               return 0xFF000000 | Capes.add(c, 16742938, crack * 1.4);
            });
         case "c3_ring":
            return Tex2.pix(256, 16, (u, v, x, y) -> {
               double b = 0.55 + 0.45 * Math.sin(v * 40.0 + Math.sin(v * 11.0) * 2.0);
               double edge = Math.min(v, 1.0 - v) * 6.0;
               return Tex2.argb(Capes.ramp(v, 15257760, 16774362, 13148272), Math.min(1.0, edge) * (0.45 + 0.5 * b));
            });
         case "c3_glass":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double s1 = Math.exp(-Math.pow((u - 0.18) / 0.03, 2.0)) * Math.exp(-Math.pow((v - 0.35) / 0.12, 2.0));
               double s2 = Math.exp(-Math.pow((u - 0.24) / 0.012, 2.0)) * Math.exp(-Math.pow((v - 0.42) / 0.18, 2.0));
               double rim = Math.pow(Math.abs(v - 0.5) * 2.0, 6.0) * 0.3;
               return Tex2.argb(15268095, 0.1 + rim + s1 * 0.7 + s2 * 0.6);
            });
         case "c3_lglass":
            return Tex2.pix(32, 32, (u, v, x, y) -> {
               double d = Math.hypot(u - 0.5, v - 0.6);
               return Tex2.argb(Capes.mix(16774336, 16751146, d * 1.6), 0.55 + 0.4 * Math.exp(-d * 4.0));
            });
         case "c3_wrap":
            return Tex2.pix(32, 128, (u, v, x, y) -> {
               double a = ((u * 2.0 + v * 8.0) % 1.0 + 1.0) % 1.0;
               double b = ((u * 2.0 - v * 8.0) % 1.0 + 1.0) % 1.0;
               double d = Math.min(Math.abs(a - 0.5), Math.abs(b - 0.5));
               boolean diamond = Math.abs(a - 0.5) < 0.22 && Math.abs(b - 0.5) < 0.22;
               return 0xFF000000 | (diamond ? Capes.shade(15788248, 0.9 + 0.1 * v) : Capes.shade(1709088, 0.8 + d));
            });
         case "c3_blade":
            return Tex2.pix(16, 128, (u, v, x, y) -> {
               double ham = 0.55 + 0.08 * Math.sin(v * 50.0) + 0.04 * Math.sin(v * 131.0);
               int c = u > ham ? Capes.mix(16054527, 16777215, (u - ham) * 3.0) : Capes.mix(9082022, 13160668, u / ham);
               return 0xFF000000 | c;
            });
         case "c3_cover":
            return Tex2.img(64, 64, g -> {
               g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(5905018), 64.0F, 64.0F, new Color(2755136)));
               g.fillRect(0, 0, 64, 64);
               g.setColor(new Color(15910986));
               g.setStroke(new BasicStroke(2.2F));
               g.draw(new java.awt.geom.RoundRectangle2D.Double(5.0, 5.0, 54.0, 54.0, 8.0, 8.0));
               g.draw(new Double(20.0, 20.0, 24.0, 24.0));
               g.fill(Tex3.starPath(32.0, 32.0, 9.0, 4.0));

               for (int i = 0; i < 4; i++) {
                  double cx = i % 2 == 0 ? 8.0 : 56.0;
                  double cy = i < 2 ? 8.0 : 56.0;
                  g.fill(new Double(cx - 3.0, cy - 3.0, 6.0, 6.0));
               }
            });
         case "c3_pages":
            return Tex2.pix(16, 64, (u, v, x, y) -> 0xFF000000 | Capes.shade(16050376, y % 2 == 0 ? 0.86 : 1.0));
         case "c3_page":
            return Tex2.img(64, 64, g -> {
               g.setColor(new Color(16314068));
               g.fillRect(0, 0, 64, 64);
               Random r = new Random(4L);
               g.setStroke(new BasicStroke(1.6F, 1, 1));

               for (int l = 0; l < 8; l++) {
                  double x = 6.0;

                  while (x < 56.0) {
                     double w = 3 + r.nextInt(8);
                     g.setColor(Capes.col(l == 0 ? 10103496 : 4864602, 0.8));
                     g.draw(new java.awt.geom.Line2D.Double(x, 8.0 + l * 6.5, Math.min(58.0, x + w), 8.0 + l * 6.5));
                     x += w + 3.0;
                  }
               }

               g.setColor(Capes.col(10103496, 0.9));
               g.draw(new Double(38.0, 42.0, 16.0, 16.0));
            });
         case "c3_glyphs":
            return Tex2.img(
               256,
               64,
               g -> {
                  g.setStroke(new BasicStroke(4.5F, 1, 1));

                  for (int i = 0; i < 4; i++) {
                     AffineTransform o = g.getTransform();
                     g.translate(i * 64 + 32, 32);
                     g.setPaint(
                        new RadialGradientPaint(0.0F, 0.0F, 30.0F, new float[]{0.0F, 1.0F}, new Color[]{Capes.col(16777215, 0.35), Capes.col(16777215, 0.0)})
                     );
                     g.fill(new Double(-30.0, -30.0, 60.0, 60.0));
                     g.setColor(Color.WHITE);
                     glyph(g, i);
                     g.setTransform(o);
                  }
               }
            );
         case "c3_splash":
            return Tex2.pix(128, 32, (u, v, x, y) -> {
               double top = 0.25 + 0.2 * Math.sin(u * Math.PI * 2.0 * 9.0) + 0.12 * Math.sin(u * Math.PI * 2.0 * 23.0 + 1.0);
               if (v < top) {
                  return Tex2.argb(16777215, 0.0);
               } else {
                  double a = 0.85 - (v - top) * 0.7;
                  int c = Capes.mix(16777215, 4897023, (v - top) * 1.6);
                  return Tex2.argb(c, a);
               }
            });
         case "c3_ripple":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double d = Math.hypot(u - 0.5, v - 0.5) * 2.0;
               double a = Math.exp(-Math.pow((d - 0.86) / 0.06, 2.0)) + 0.5 * Math.exp(-Math.pow((d - 0.62) / 0.05, 2.0));
               return Tex2.argb(Capes.mix(16777215, 8048895, d), Math.min(1.0, a));
            });
         case "c3_drop":
            return Tex2.img(
               32,
               48,
               g -> {
                  Path2D p = new java.awt.geom.Path2D.Double();
                  p.moveTo(16.0, 2.0);
                  p.curveTo(22.0, 16.0, 30.0, 24.0, 30.0, 32.0);
                  p.curveTo(30.0, 42.0, 22.0, 46.0, 16.0, 46.0);
                  p.curveTo(10.0, 46.0, 2.0, 42.0, 2.0, 32.0);
                  p.curveTo(2.0, 24.0, 10.0, 16.0, 16.0, 2.0);
                  p.closePath();
                  g.setPaint(
                     new RadialGradientPaint(16.0F, 32.0F, 16.0F, new float[]{0.0F, 1.0F}, new Color[]{Capes.col(14218495, 0.9), Capes.col(2792191, 0.85)})
                  );
                  g.fill(p);
                  g.setColor(Capes.col(16777215, 0.95));
                  g.fill(new Double(9.0, 26.0, 6.0, 9.0));
               }
            );
         case "c3_hex":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double px = (u - 0.5) * 2.0;
               double py = (v - 0.5) * 2.0;
               double d = Math.max(Math.abs(px) * 0.866 + Math.abs(py) * 0.5, Math.abs(py));
               double edge = Math.exp(-Math.pow((d - 0.86) / 0.07, 2.0));
               double fill = d < 0.9 ? 0.28 + 0.2 * (1.0 - d) : 0.0;
               return Tex2.argb(16777215, Math.min(1.0, fill + edge));
            });
         case "c3_ray":
            return Tex2.pix(8, 64, (u, v, x, y) -> Tex2.argb(16777215, Math.pow(v, 1.4) * (0.55 + 0.45 * Math.sin(u * Math.PI))));
         case "c3_visor":
            return Tex2.pix(128, 32, (u, v, x, y) -> {
               double scan = y % 3 == 0 ? 0.14 : 0.0;
               double edge = Math.pow(Math.abs(v - 0.5) * 2.0, 4.0);
               double streak = Math.exp(-Math.pow((u * 1.0 - v * 0.25 - 0.62) / 0.03, 2.0)) * 0.5;
               int c = Capes.mix(2808063, 12086015, u);
               return Tex2.argb(Capes.add(c, 16777215, streak + edge * 0.6), 0.42 + scan + edge * 0.4 + streak);
            });
         case "c3_lens":
            return Tex2.pix(32, 32, (u, v, x, y) -> {
               int c = Capes.ramp(v, 3807834, 1444388, 525326);
               double streak = Math.exp(-Math.pow((u * 0.8 + v * 0.6 - 0.55) / 0.05, 2.0)) * 0.45;
               return Tex2.argb(Capes.add(c, 16777215, streak), 0.9);
            });
         case "c3_membrane":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double vein = Math.exp(-Math.pow(Math.sin(u * Math.PI * 5.0 + v * 2.0) / 0.12, 2.0)) * 0.5;
               return Tex2.argb(Capes.add(15263976, 16777215, vein) & 16777215, 0.88 + vein * 0.2);
            });
         case "c3_parrotwing":
            return Tex2.img(
               64,
               32,
               g -> {
                  g.setPaint(
                     new LinearGradientPaint(
                        0.0F,
                        0.0F,
                        64.0F,
                        0.0F,
                        new float[]{0.0F, 0.45F, 0.75F, 1.0F},
                        new Color[]{new Color(2279592), new Color(2788072), new Color(6961880), new Color(15216714)}
                     )
                  );
                  g.fillRect(0, 0, 64, 32);
                  g.setStroke(new BasicStroke(1.0F));

                  for (int i = 0; i < 12; i++) {
                     g.setColor(Capes.col(0, 0.22));
                     g.drawLine(8 + i * 5, 0, 14 + i * 5, 32);
                     g.setColor(Capes.col(16777215, 0.12));
                     g.drawLine(9 + i * 5, 0, 15 + i * 5, 32);
                  }

                  g.setColor(Capes.col(16769098, 0.9));
                  g.fillRect(0, 13, 18, 5);
               }
            );
         case "c3_flamegrad":
            return Tex2.pix(32, 64, (u, v, x, y) -> {
               double lick = 0.08 * Math.sin(u * Math.PI * 2.0 * 3.0 + v * 9.0);
               double f = Capes.clamp01(v + lick);
               int c = Capes.ramp(f, 16777200, 16769146, 16777215, 16777215);
               return Tex2.argb(c, Math.max(0.15, 1.0 - Math.pow(f, 1.6) * 0.8));
            });
         case "c3_pumpkin":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double rib = Math.abs(Math.sin(u * Math.PI * 8.0));
               return 0xFF000000 | Capes.shade(16777215, 0.72 + 0.28 * Math.pow(rib, 0.5));
            });
         default:
            return null;
      }
   }

   static void glyph(Graphics2D g, int i) {
      Path2D p = new java.awt.geom.Path2D.Double();
      switch (i % 4) {
         case 0:
            p.moveTo(-14.0, 16.0);
            p.lineTo(0.0, -16.0);
            p.lineTo(14.0, 16.0);
            p.moveTo(-8.0, 4.0);
            p.lineTo(8.0, 4.0);
            p.moveTo(0.0, -16.0);
            p.lineTo(0.0, -22.0);
            break;
         case 1:
            p.append(new Double(-12.0, -12.0, 24.0, 24.0), false);
            p.moveTo(-18.0, 0.0);
            p.lineTo(18.0, 0.0);
            p.moveTo(0.0, 12.0);
            p.lineTo(0.0, 22.0);
            break;
         case 2:
            p.moveTo(-12.0, -16.0);
            p.lineTo(12.0, -16.0);
            p.lineTo(-12.0, 16.0);
            p.lineTo(12.0, 16.0);
            p.moveTo(-6.0, 0.0);
            p.lineTo(6.0, 0.0);
            break;
         default:
            p.moveTo(0.0, -18.0);
            p.lineTo(14.0, 0.0);
            p.lineTo(0.0, 18.0);
            p.lineTo(-14.0, 0.0);
            p.closePath();
            p.moveTo(0.0, -8.0);
            p.lineTo(0.0, 8.0);
      }

      g.draw(p);
   }

   private static CTex.T band(int base, int dark, int accent, int pat) {
      return Tex2.img(256, 32, g -> {
         g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(Capes.mix(base, 16777215, 0.12)), 0.0F, 32.0F, new Color(Capes.mix(base, dark, 0.5))));
         g.fillRect(0, 0, 256, 32);

         for (int y = 0; y < 32; y += 2) {
            g.setColor(Capes.col(0, 0.06));
            g.drawLine(0, y, 256, y);
         }

         g.setColor(Capes.col(dark, 0.9));
         g.fillRect(0, 0, 256, 3);
         g.fillRect(0, 29, 256, 3);
         g.setColor(Capes.col(16777215, 0.35));
         g.setStroke(new BasicStroke(1.0F, 0, 0, 1.0F, new float[]{3.0F, 2.0F}, 0.0F));
         g.drawLine(0, 5, 256, 5);
         g.drawLine(0, 27, 256, 27);
         g.setStroke(new BasicStroke(1.0F));

         for (int i = 0; i < 8; i++) {
            double cx = 16 + i * 32;
            AffineTransform o = g.getTransform();
            g.translate(cx, 16.0);
            if (pat == 0) {
               Path2D b = new java.awt.geom.Path2D.Double();
               b.moveTo(2.0, -10.0);
               b.lineTo(-5.0, 1.0);
               b.lineTo(0.0, 1.0);
               b.lineTo(-3.0, 10.0);
               b.lineTo(5.0, -2.0);
               b.lineTo(0.0, -2.0);
               b.lineTo(4.0, -10.0);
               b.closePath();
               g.setColor(Capes.col(accent, 0.95));
               g.fill(b);
               g.setColor(Capes.col(16777215, 0.9));
               g.fill(new Double(10.0, -6.0, 3.0, 3.0));
               g.fill(new Double(-14.0, 4.0, 2.5, 2.5));
            } else if (pat == 1) {
               Path2D f = new java.awt.geom.Path2D.Double();
               f.moveTo(-9.0, 13.0);
               f.curveTo(-10.0, 2.0, -2.0, 0.0, -3.0, -12.0);
               f.curveTo(4.0, -4.0, 9.0, 2.0, 8.0, 13.0);
               f.closePath();
               g.setPaint(new GradientPaint(0.0F, 13.0F, new Color(16769146), 0.0F, -12.0F, new Color(accent)));
               g.fill(f);
               g.setColor(Capes.col(16774864, 0.9));
               g.fill(new Double(-3.0, 4.0, 5.0, 7.0));
            } else {
               g.setColor(Capes.col(accent, 0.95));
               g.setStroke(new BasicStroke(1.6F, 1, 1));

               for (int k = 0; k < 3; k++) {
                  g.rotate(Math.PI / 3);
                  g.drawLine(0, -9, 0, 9);
                  g.drawLine(-3, -6, 0, -8);
                  g.drawLine(3, -6, 0, -8);
               }

               g.setStroke(new BasicStroke(1.0F));
            }

            g.setTransform(o);
         }
      });
   }

   static CTex.T skin() {
      BufferedImage im = new BufferedImage(64, 64, 2);
      int[] p = Capes.px(im);
      int skinC = 15250570;
      int hair = 14708766;
      int hairD = 11029008;
      int hood = 2074784;
      int hoodD = 1276532;
      int pants = 2764360;
      int pantsD = 1842994;
      int shoe = 15921906;
      fillBox(p, 0, 0, 8, 8, 8, skinC);
      rect(p, 0, 8, 32, 3, hair);
      rect(p, 8, 0, 8, 8, hair);
      rect(p, 24, 8, 8, 7, hair);
      rect(p, 0, 11, 2, 3, hair);
      rect(p, 22, 11, 2, 3, hair);
      rect(p, 8, 11, 8, 1, hairD);
      rect(p, 10, 8, 2, 4, hair);
      rect(p, 13, 8, 2, 4, hair);
      rect(p, 9, 12, 2, 2, 16777215);
      rect(p, 13, 12, 2, 2, 16777215);
      rect(p, 10, 12, 1, 2, 2775610);
      rect(p, 13, 12, 1, 2, 2775610);
      rect(p, 11, 14, 2, 1, 13140058);
      rect(p, 10, 15, 4, 1, 10111546);
      rect(p, 9, 14, 1, 1, 15769738);
      rect(p, 14, 14, 1, 1, 15769738);
      rect(p, 32, 8, 32, 1, hoodD);
      rect(p, 40, 0, 8, 2, hoodD);
      rect(p, 56, 8, 8, 8, argb2(hood));
      rect(p, 32, 8, 2, 8, argb2(hood));
      rect(p, 54, 8, 2, 8, argb2(hood));
      fillBox(p, 16, 16, 8, 12, 4, hood);
      rect(p, 20, 30, 8, 2, hoodD);
      rect(p, 32, 30, 8, 2, hoodD);
      rect(p, 23, 20, 2, 4, 15921906);
      rect(p, 23, 20, 1, 5, 15921906);
      rect(p, 24, 20, 1, 5, 15921906);
      rect(p, 21, 25, 6, 3, hoodD);
      rect(p, 25, 21, 2, 2, 16764163);
      rect(p, 25, 21, 1, 1, 16771194);
      fillBox(p, 40, 16, 4, 12, 4, hood);
      rect(p, 40, 29, 16, 1, hoodD);
      rect(p, 40, 30, 16, 2, skinC);
      fillBox(p, 32, 48, 4, 12, 4, hood);
      rect(p, 32, 61, 16, 1, hoodD);
      rect(p, 32, 62, 16, 2, skinC);
      fillBox(p, 0, 16, 4, 12, 4, pants);
      rect(p, 0, 29, 16, 3, shoe);
      rect(p, 0, 29, 16, 1, 13158608);
      rect(p, 4, 22, 1, 6, pantsD);
      fillBox(p, 16, 48, 4, 12, 4, pants);
      rect(p, 16, 61, 16, 3, shoe);
      rect(p, 16, 61, 16, 1, 13158608);
      rect(p, 23, 54, 1, 6, pantsD);

      for (int y = 0; y < 64; y++) {
         for (int x = 0; x < 64; x++) {
            int i = y * 64 + x;
            if (p[i] >>> 24 != 0) {
               p[i] = p[i] & 0xFF000000 | Capes.shade(p[i], 0.94 + 0.1 * Capes.hash(x, y, 91)) & 16777215;
            }
         }
      }

      return CTex.of(im);
   }

   private static int argb2(int rgb) {
      return 0xFF000000 | rgb;
   }

   private static void rect(int[] p, int x, int y, int w, int h, int c) {
      if (c >>> 24 == 0) {
         c |= -16777216;
      }

      for (int j = y; j < y + h; j++) {
         for (int i = x; i < x + w; i++) {
            if (i >= 0 && j >= 0 && i < 64 && j < 64) {
               p[j * 64 + i] = c;
            }
         }
      }
   }

   private static void fillBox(int[] p, int U, int V, int w, int h, int d, int c) {
      rect(p, U + d, V, w, d, Capes.shade(c, 1.08));
      rect(p, U + d + w, V, w, d, Capes.shade(c, 0.8));
      rect(p, U, V + d, d, h, Capes.shade(c, 0.9));
      rect(p, U + d, V + d, w, h, c);
      rect(p, U + d + w, V + d, d, h, Capes.shade(c, 0.9));
      rect(p, U + 2 * d + w, V + d, w, h, Capes.shade(c, 0.86));
   }

   private static Rectangle2D r(double x, double y, double w, double h) {
      return new java.awt.geom.Rectangle2D.Double(x, y, w, h);
   }
}
