package dev.lego.cosmetic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;
import java.util.Random;

final class XmasTex {
   static final float[][] GMAN = new float[][]{
      {0.0F, 1.0F},
      {0.24F, 0.96F},
      {0.36F, 0.8F},
      {0.34F, 0.58F},
      {0.22F, 0.46F},
      {0.62F, 0.44F},
      {0.86F, 0.36F},
      {0.9F, 0.2F},
      {0.76F, 0.12F},
      {0.36F, 0.12F},
      {0.36F, -0.2F},
      {0.62F, -0.72F},
      {0.62F, -0.92F},
      {0.44F, -1.0F},
      {0.26F, -0.92F},
      {0.0F, -0.46F},
      {-0.26F, -0.92F},
      {-0.44F, -1.0F},
      {-0.62F, -0.92F},
      {-0.62F, -0.72F},
      {-0.36F, -0.2F},
      {-0.36F, 0.12F},
      {-0.76F, 0.12F},
      {-0.9F, 0.2F},
      {-0.86F, 0.36F},
      {-0.62F, 0.44F},
      {-0.22F, 0.46F},
      {-0.34F, 0.58F},
      {-0.36F, 0.8F},
      {-0.24F, 0.96F}
   };

   private XmasTex() {
   }

   static CTex.T paint(String name) {
      switch (name) {
         case "xm_felt":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double n = Capes.fbm(u * 10.0, v * 10.0, 10, 10, 4, 301);
               return 0xFF000000 | Capes.shade(16777215, 0.82 + 0.16 * n + 0.05 * (Capes.hash(x, y, 302) - 0.5));
            });
         case "xm_velvet":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double n = Capes.fbm(u * 6.0, v * 6.0, 6, 6, 3, 311);
               double sheen = 0.5 + 0.5 * Math.sin(u * Math.PI * 2.0 * 2.0 + n * 2.0);
               return 0xFF000000 | Capes.shade(16777215, 0.72 + 0.2 * n + 0.1 * sheen);
            });
         case "xm_fluff":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double n = Capes.fbm(u * 16.0, v * 16.0, 16, 16, 4, 321);
               double curl = 0.5 + 0.5 * Math.sin(u * 30.0 + n * 6.0 + Math.cos(v * 30.0 + n * 5.0));
               return 0xFF000000 | Capes.shade(16777215, 0.8 + 0.14 * n + 0.08 * curl);
            });
         case "xm_knit":
            return knit(-1);
         case "xm_scarf":
            return knit(0);
         case "xm_sweater":
            return knit(1);
         case "xm_ribbon":
            return Tex2.pix(32, 32, (u, v, x, y) -> {
               double sh = 0.78 + 0.22 * Math.exp(-Math.pow((u - 0.42) / 0.16, 2.0)) + 0.05 * Math.sin(v * 40.0);
               double edge = !(u < 0.08) && !(u > 0.92) ? 1.0 : 0.8;
               return 0xFF000000 | Capes.shade(16777215, Math.min(1.0, sh * edge));
            });
         case "xm_silk":
            return Tex2.pix(
               64,
               32,
               (u, v, x, y) -> {
                  double s = 0.35
                     + 0.5 * Math.pow(Math.max(0.0, Math.sin(u * Math.PI * 2.0 * 3.0)), 6.0)
                     + 0.25 * Math.pow(Math.max(0.0, Math.sin(u * Math.PI * 2.0 * 3.0 + 1.2)), 16.0);
                  return 0xFF000000 | Capes.shade(16777215, Math.min(1.0, s + 0.05 * v));
               }
            );
         case "xm_elfcuff":
            return Tex2.img(256, 32, g -> {
               g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(15217210), 0.0F, 32.0F, new Color(10097176)));
               g.fillRect(0, 0, 256, 32);
               g.setColor(new Color(16767082));
               g.fillRect(0, 0, 256, 3);
               g.fillRect(0, 29, 256, 3);
               Path2D z = new Double();
               z.moveTo(0.0, 22.0);

               for (int i = 0; i <= 16; i++) {
                  z.lineTo(i * 16, i % 2 == 0 ? 22.0 : 9.0);
               }

               g.setColor(Color.WHITE);
               g.setStroke(new BasicStroke(4.0F, 1, 1));
               g.draw(z);
               g.setColor(new Color(3054152));

               for (int i = 0; i < 16; i++) {
                  g.fill(new java.awt.geom.Ellipse2D.Double(i * 16 + 5.5, i % 2 == 0 ? 7.0 : 17.0, 5.0, 5.0));
               }
            });
         case "xm_sack":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               boolean wx = (x / 2 + y / 2) % 2 == 0;
               double n = Capes.fbm(u * 8.0, v * 8.0, 8, 8, 3, 331);
               double m = (wx ? 0.92 : 0.8) * (0.85 + 0.25 * n) * (x % 2 == 0 ? 1.0 : 0.95);
               return 0xFF000000 | Capes.shade(16777215, m);
            });
         case "xm_candy":
            return Tex2.pix(64, 128, (u, v, x, y) -> {
               double s = frac(u * 2.0 + v * 4.0);
               int c = s < 0.32 ? 14688298 : (s > 0.42 && s < 0.47 ? 15751770 : (s > 0.72 && s < 0.78 ? 3848298 : 16775924));
               double ed = Math.min(Math.abs(s - 0.32), Math.abs(s));
               if (ed < 0.015) {
                  c = Capes.mix(c, 15764106, 0.5);
               }

               return 0xFF000000 | Capes.shade(c, 0.94 + 0.06 * Math.sin(v * 30.0));
            });
         case "xm_antler":
            return Tex2.pix(32, 128, (u, v, x, y) -> {
               double groove = 0.9 + 0.1 * Math.sin(u * Math.PI * 2.0 * 7.0 + Capes.fbm(u * 4.0, v * 8.0, 4, 8, 2, 341) * 4.0);
               double knob = Capes.fbm(u * 6.0, v * 20.0, 6, 20, 3, 342);
               int c = Capes.mix(9067060, 15786176, Math.pow(v, 1.4) * 0.9);
               return 0xFF000000 | Capes.shade(c, groove * (0.82 + 0.3 * knob));
            });
         case "xm_pine":
            return Tex2.img(64, 64, g -> {
               g.setColor(new Color(1989168));
               g.fillRect(0, 0, 64, 64);
               Random r = new Random(351L);
               g.setStroke(new BasicStroke(1.3F, 1, 1));

               for (int i = 0; i < 420; i++) {
                  double x = r.nextDouble() * 64.0;
                  double y = r.nextDouble() * 64.0;
                  double l = 4.0 + r.nextDouble() * 5.0;
                  double a = (Math.PI / 2) + (r.nextDouble() - 0.5) * 1.1;
                  int c = Capes.ramp(r.nextDouble(), 1460266, 3045956, 4892768, 8047492);
                  g.setColor(new Color(c));

                  for (int w = -1; w <= 1; w++) {
                     double xx = x + w * 64;
                     g.drawLine((int)xx, (int)y, (int)(xx + Math.cos(a) * l), (int)(y + Math.sin(a) * l));
                  }
               }
            });
         case "xm_bark":
            return Tex2.pix(32, 64, (u, v, x, y) -> 0xFF000000 | Capes.shade(6963746, 0.7 + 0.35 * Capes.fbm(u * 8.0, v * 2.0, 8, 2, 3, 361)));
         case "xm_wood":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double n = Capes.fbm(u * 2.0, v * 10.0, 2, 10, 4, 371);
               double gr = 0.5 + 0.5 * Math.sin((v * 22.0 + n * 6.0) * Math.PI);
               return 0xFF000000 | Capes.shade(16777215, 0.72 + 0.18 * gr + 0.1 * n);
            });
         case "xm_coal":
            return Tex2.pix(
               32,
               32,
               (u, v, x, y) -> 0xFF000000
                  | Capes.shade(2763312, 0.6 + 0.8 * Capes.fbm(u * 6.0, v * 6.0, 6, 6, 3, 381) + (Capes.hash(x, y, 382) > 0.93 ? 0.8 : 0.0))
            );
         case "xm_carrot":
            return Tex2.pix(32, 64, (u, v, x, y) -> {
               double ring = 0.88 + 0.12 * Math.sin(v * 60.0 + Capes.fbm(u * 4.0, v * 4.0, 4, 4, 2, 391) * 3.0);
               return 0xFF000000 | Capes.shade(Capes.mix(16747034, 15755786, v), ring);
            });
         case "xm_snow":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double n = Capes.fbm(u * 8.0, v * 8.0, 8, 8, 4, 401);
               int c = Capes.mix(14477560, 16777215, 0.4 + 0.6 * n);
               if (Capes.hash(x, y, 402) > 0.975) {
                  c = 16777215;
               }

               if (Capes.hash(x, y, 403) > 0.992) {
                  c = 12575999;
               }

               return 0xFF000000 | c;
            });
         case "xm_ginger":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double n = Capes.fbm(u * 8.0, v * 8.0, 8, 8, 4, 411);
               int c = Capes.ramp(n, 9062942, 11559978, 13138488);
               if (Capes.hash(x, y, 412) > 0.97) {
                  c = Capes.shade(c, 0.75);
               }

               if (Capes.hash(x, y, 413) > 0.985) {
                  c = Capes.mix(c, 15249520, 0.5);
               }

               return 0xFF000000 | c;
            });
         case "xm_gingerman":
            return gingerMan();
         case "xm_holly":
            return Tex2.img(64, 32, g -> {
               g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(3842128), 0.0F, 32.0F, new Color(1727016)));
               g.fillRect(0, 0, 64, 32);
               g.setColor(new Color(10147978));
               g.setStroke(new BasicStroke(1.4F));
               g.drawLine(2, 16, 62, 16);
               g.setStroke(new BasicStroke(0.8F));

               for (int i = 0; i < 6; i++) {
                  g.drawLine(8 + i * 9, 16, 13 + i * 9, 6);
                  g.drawLine(8 + i * 9, 16, 13 + i * 9, 26);
               }

               g.setColor(Capes.col(16777215, 0.25));
               g.fill(new java.awt.geom.Ellipse2D.Double(10.0, 5.0, 30.0, 7.0));
            });
         case "xm_gloss":
            return Tex2.pix(32, 32, (u, v, x, y) -> {
               double d = Math.hypot(u - 0.32, v - 0.3);
               return 0xFF000000 | Capes.add(Capes.shade(16777215, 0.7 + 0.3 * (1.0 - v)), 16777215, Math.exp(-d * d / 0.004) * 0.9);
            });
         case "xm_bulb":
            return Tex2.pix(32, 32, (u, v, x, y) -> {
               double d = Math.hypot(u - 0.5, v - 0.5);
               double spec = Math.exp(-(Math.pow(u - 0.3, 2.0) + Math.pow(v - 0.35, 2.0)) / 0.006);
               return 0xFF000000 | Capes.add(Capes.shade(16777215, 0.78 + 0.22 * (1.0 - d * 1.4)), 16777215, spec);
            });
         case "xm_flake":
            return Tex2.img(64, 64, g -> {
               g.translate(32, 32);

               for (int pass = 0; pass < 2; pass++) {
                  g.setColor(pass == 0 ? Capes.col(10149119, 0.45) : Color.WHITE);
                  g.setStroke(new BasicStroke(pass == 0 ? 5.5F : 2.4F, 1, 1));

                  for (int i = 0; i < 6; i++) {
                     g.drawLine(0, 0, 0, -28);
                     g.drawLine(0, -10, -6, -16);
                     g.drawLine(0, -10, 6, -16);
                     g.drawLine(0, -19, -5, -24);
                     g.drawLine(0, -19, 5, -24);
                     g.rotate(Math.PI / 3);
                  }
               }

               g.setColor(Color.WHITE);
               g.fill(new java.awt.geom.Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0));
            });
         case "xm_aurora":
            return Tex2.pix(128, 64, (u, v, x, y) -> {
               double st = Capes.fbm(u * 24.0, 0.5, 24, 0, 3, 421);
               double fold = 0.5 + 0.5 * Math.sin(u * Math.PI * 2.0 * 9.0 + st * 3.0);
               double bottom = Math.exp(-Math.pow((1.0 - v) / 0.1, 2.0));
               double body = Math.pow(v, 1.8) * (0.45 + 0.55 * st) * (0.6 + 0.4 * fold);
               int c = Capes.ramp(v, 11819775, 5933823, 3860696, 5963658, 13172696);
               double a = Math.min(1.0, body * 1.1 + bottom * 0.7) * (v > 0.97 ? (1.0 - v) / 0.03 : 1.0);
               return Tex2.argb(Capes.add(c, 16777215, bottom * 0.5), a);
            });
         case "xm_gold":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double s = 0.5 + 0.5 * Math.sin(u * 9.0 + v * 4.0);
               return 0xFF000000 | Capes.ramp(0.25 + 0.6 * s * (0.8 + 0.2 * Capes.hash(x / 4, y / 4, 431)), 10119698, 15251756, 16773544, 16777215);
            });
         case "xm_red_lacquer":
            return Tex2.pix(32, 128, (u, v, x, y) -> {
               double sh = Math.exp(-Math.pow((u - 0.3) / 0.12, 2.0)) * 0.45 + 0.6 + 0.08 * Math.sin(v * 3.0);
               return 0xFF000000 | Capes.shade(13112350, Math.min(1.35, sh));
            });
         case "xm_sleighside":
            return sleighSide();
         case "xm_wind":
            return Tex2.pix(128, 16, (u, v, x, y) -> {
               double w = Math.pow(Math.sin(Math.PI * v), 2.0) * (0.35 + 0.65 * Capes.fbm(u * 12.0, v * 2.0, 12, 2, 3, 461));
               return Tex2.argb(16055039, w * Math.pow(Math.sin(Math.PI * u), 0.6) * 0.75);
            });
         default:
            return name.startsWith("xm_gift:") ? gift(name.substring(8)) : null;
      }
   }

   static double frac(double v) {
      return v - Math.floor(v);
   }

   private static CTex.T knit(int kind) {
      int w = 64;
      int h = kind == 0 ? 256 : 128;
      return Tex2.pix(w, h, (u, v, x, y) -> {
         double cu = u * 8.0;
         double cv = v * (kind == 0 ? 40 : 20);
         double fx = frac(cu) - 0.5;
         double fy = frac(cv);
         double lobe = Math.abs(Math.abs(fx) * 2.0 - (1.0 - fy) * 0.9 - 0.05);
         double m = 1.0 - Math.min(1.0, lobe * 1.6);
         double groove = Math.abs(fx) < 0.05 ? 0.72 : 1.0;
         double sh = (0.72 + 0.3 * m) * groove * (0.95 + 0.06 * Capes.hash(x, y, 441));
         int c = 16777215;
         if (kind == 0) {
            int row = (int)Math.floor(cv);
            int r = row % 20;
            c = r == 3 || r == 4 || r == 15 || r == 16 ? 16777215 : (r != 9 && r != 10 ? 14164780 : 3054152);
            if (row < 1 || row > 38) {
               c = 16053492;
            }
         } else if (kind == 1) {
            int row = (int)Math.floor(cv);
            c = row % 10 != 4 && row % 10 != 5 ? 13115434 : 16777215;
         }

         return 0xFF000000 | Capes.shade(c, sh);
      });
   }

   private static CTex.T gift(String spec) {
      String[] p = spec.split(":");
      int base = Integer.parseInt(p[0], 16);
      int pat = Integer.parseInt(p[1], 16);
      int kind = p.length > 2 ? Integer.parseInt(p[2]) : 0;
      return Tex2.img(64, 64, g -> {
         g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(Capes.mix(base, 16777215, 0.12)), 64.0F, 64.0F, new Color(Capes.mix(base, 0, 0.18))));
         g.fillRect(0, 0, 64, 64);
         g.setColor(new Color(pat));
         if (kind == 2) {
            g.setStroke(new BasicStroke(5.0F));

            for (int i = -64; i < 128; i += 16) {
               g.drawLine(i, 0, i + 64, 64);
            }
         } else {
            for (int yy = 0; yy < 4; yy++) {
               for (int xx = 0; xx < 4; xx++) {
                  double cx = 8 + xx * 16 + yy % 2 * 8;
                  double cy = 8 + yy * 16;
                  if (kind == 0) {
                     g.fill(new java.awt.geom.Ellipse2D.Double(cx - 3.0, cy - 3.0, 6.0, 6.0));
                  } else if (kind == 1) {
                     g.fill(starShape(cx, cy, 5.0, 2.2, 5));
                  } else {
                     g.setStroke(new BasicStroke(1.4F, 1, 1));

                     for (int k = 0; k < 3; k++) {
                        double a = k * Math.PI / 3.0;
                        g.drawLine((int)(cx - Math.cos(a) * 5.0), (int)(cy - Math.sin(a) * 5.0), (int)(cx + Math.cos(a) * 5.0), (int)(cy + Math.sin(a) * 5.0));
                     }
                  }
               }
            }
         }

         g.setColor(Capes.col(16777215, 0.08));

         for (int i = 0; i < 64; i += 3) {
            g.drawLine(0, i, 64, i);
         }
      });
   }

   static Path2D starShape(double cx, double cy, double r0, double r1, int pts) {
      Path2D s = new Double();

      for (int i = 0; i < pts * 2; i++) {
         double a = (-Math.PI / 2) + Math.PI * i / pts;
         double r = i % 2 == 0 ? r0 : r1;
         if (i == 0) {
            s.moveTo(cx + Math.cos(a) * r, cy + Math.sin(a) * r);
         } else {
            s.lineTo(cx + Math.cos(a) * r, cy + Math.sin(a) * r);
         }
      }

      s.closePath();
      return s;
   }

   private static CTex.T gingerMan() {
      return Tex2.img(
         128,
         128,
         g -> {
            g.setColor(new Color(8011800));
            g.fillRect(0, 0, 128, 128);
            Path2D s = new Double();

            for (int i = 0; i < GMAN.length; i++) {
               double x = (GMAN[i][0] + 0.9) / 1.8 * 128.0;
               double y = 64.0F - GMAN[i][1] * 64.0F;
               if (i == 0) {
                  s.moveTo(x, y);
               } else {
                  s.lineTo(x, y);
               }
            }

            s.closePath();
            g.setPaint(
               new RadialGradientPaint(
                  56.0F, 50.0F, 80.0F, new float[]{0.0F, 0.7F, 1.0F}, new Color[]{new Color(13666884), new Color(12085806), new Color(9062942)}
               )
            );
            g.fill(s);
            Random r = new Random(451L);

            for (int ix = 0; ix < 90; ix++) {
               g.setColor(Capes.col(5909002, 0.35));
               g.fill(new java.awt.geom.Ellipse2D.Double(r.nextDouble() * 128.0, r.nextDouble() * 128.0, 1.6, 1.6));
            }

            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(3.0F, 1, 1));
            g.fill(new java.awt.geom.Ellipse2D.Double(51.0, 13.0, 8.0, 8.0));
            g.fill(new java.awt.geom.Ellipse2D.Double(69.0, 13.0, 8.0, 8.0));
            g.drawArc(52, 16, 24, 16, 200, 140);
            zig(g, 14.0, 44.0, 14.0, 62.0, 4);
            zig(g, 114.0, 44.0, 114.0, 62.0, 4);
            zig(g, 30.0, 108.0, 42.0, 120.0, 3);
            zig(g, 98.0, 108.0, 86.0, 120.0, 3);
            int[] bc = new int[]{15217210, 3061834, 15217210};

            for (int ix = 0; ix < 3; ix++) {
               g.setColor(new Color(bc[ix]));
               g.fill(new java.awt.geom.Ellipse2D.Double(58.0, 44 + ix * 14, 12.0, 12.0));
               g.setColor(Capes.col(16777215, 0.7));
               g.fill(new java.awt.geom.Ellipse2D.Double(60.0, 46 + ix * 14, 4.0, 4.0));
            }
         }
      );
   }

   private static void zig(Graphics2D g, double x0, double y0, double x1, double y1, int n) {
      Path2D p = new Double();
      double dx = x1 - x0;
      double dy = y1 - y0;
      double l = Math.hypot(dx, dy);
      double nx = -dy / l * 4.0;
      double ny = dx / l * 4.0;

      for (int i = 0; i <= n * 2; i++) {
         double f = (double)i / (n * 2);
         double s = i % 2 == 0 ? -1.0 : 1.0;
         double x = x0 + dx * f + nx * s;
         double y = y0 + dy * f + ny * s;
         if (i == 0) {
            p.moveTo(x, y);
         } else {
            p.lineTo(x, y);
         }
      }

      g.setColor(Color.WHITE);
      g.draw(p);
   }

   private static CTex.T sleighSide() {
      return Tex2.img(256, 64, g -> {
         g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(15217210), 0.0F, 64.0F, new Color(7997970)));
         g.fillRect(0, 0, 256, 64);
         g.setColor(Capes.col(16777215, 0.18));
         g.fillRect(0, 8, 256, 6);
         g.setColor(new Color(16767082));
         g.setStroke(new BasicStroke(2.2F, 1, 1));
         g.drawLine(0, 4, 256, 4);
         g.drawLine(0, 58, 256, 58);

         for (int i = 0; i < 4; i++) {
            double cx = 32 + i * 64;
            Path2D sc = new Double();
            sc.moveTo(cx - 24.0, 32.0);
            sc.curveTo(cx - 20.0, 14.0, cx - 4.0, 14.0, cx - 4.0, 28.0);
            sc.curveTo(cx - 4.0, 36.0, cx - 14.0, 36.0, cx - 12.0, 28.0);
            g.draw(sc);
            Path2D sc2 = new Double();
            sc2.moveTo(cx + 24.0, 32.0);
            sc2.curveTo(cx + 20.0, 50.0, cx + 4.0, 50.0, cx + 4.0, 36.0);
            sc2.curveTo(cx + 4.0, 28.0, cx + 14.0, 28.0, cx + 12.0, 36.0);
            g.draw(sc2);
            g.fill(new java.awt.geom.Ellipse2D.Double(cx - 3.0, 29.0, 6.0, 6.0));
         }
      });
   }
}
