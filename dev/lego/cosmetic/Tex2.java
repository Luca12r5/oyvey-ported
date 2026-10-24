package dev.lego.cosmetic;

import dev.lego.ui.Gx;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.function.Consumer;

final class Tex2 {
   private static final Object[][] FEATHERS = new Object[][]{
      {"angel", 16777215, 15659770, 13226982, 15259816, "none"},
      {"gold", 16774848, 15909424, 11565586, 16774064, "metal"},
      {"phoenix", 16774048, 16747038, 13112346, 16769658, "fire"},
      {"shadow", 4869730, 1974322, 526351, 10185727, "rim"},
      {"owl", 15785400, 10910798, 5124634, 15258280, "bars"},
      {"rainbow", 16777215, 16053492, 14342874, 16777215, "none"},
      {"galaxy", 3807866, 1706554, 459802, 13150463, "stars"},
      {"crystal", 15794175, 9429247, 3832575, 16777215, "crystal"},
      {"fire", 16774848, 16756784, 16726538, 16777215, "flame"}
   };

   private Tex2() {
   }

   static CTex.T img(int w, int h, Consumer<Graphics2D> p) {
      BufferedImage im = new BufferedImage(w, h, 2);
      Graphics2D g = im.createGraphics();
      Gx.hints(g);
      p.accept(g);
      g.dispose();
      return CTex.of(im);
   }

   static CTex.T pix(int w, int h, Tex2.PF f) {
      BufferedImage im = new BufferedImage(w, h, 2);
      int[] p = Capes.px(im);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            p[y * w + x] = f.at((x + 0.5) / w, (y + 0.5) / h, x, y);
         }
      }

      return CTex.of(im);
   }

   static int argb(int rgb, double a) {
      return (int)Math.round(Math.max(0.0, Math.min(1.0, a)) * 255.0) << 24 | rgb & 16777215;
   }

   static CTex.T paint(String name) {
      if (name.startsWith("fea_") || name.startsWith("cov_")) {
         String st = name.substring(4);
         if (st.startsWith("x")) {
            String[] p = st.substring(1).split("_");
            return feather(
               Integer.parseInt(p[0], 16), Integer.parseInt(p[1], 16), Integer.parseInt(p[2], 16), Integer.parseInt(p[3], 16), p[4], name.startsWith("cov_")
            );
         } else {
            for (Object[] f : FEATHERS) {
               if (f[0].equals(st)) {
                  return feather((Integer)f[1], (Integer)f[2], (Integer)f[3], (Integer)f[4], (String)f[5], name.startsWith("cov_"));
               }
            }

            return null;
         }
      } else if (name.startsWith("mem_x")) {
         String[] p = name.substring(5).split("_");
         return membrane(
            Integer.parseInt(p[0], 16), Integer.parseInt(p[1], 16), Integer.parseInt(p[2], 16), Integer.parseInt(p[3], 16), Double.parseDouble(p[4])
         );
      } else {
         switch (name) {
            case "mem_demon":
               return membrane(6950936, 12593712, 1704966, 16738890, 0.93);
            case "mem_icedragon":
               return membrane(1727130, 8050943, 663098, 14744319, 0.82);
            case "mem_neon":
               return membrane(2755146, 8006344, 655896, 16739061, 0.8);
            case "mem_bat":
               return membrane(4862504, 9071192, 1708558, 13148296, 0.9);
            case "mem_dragon":
               return membrane(11550730, 16752704, 4853764, 16769162, 0.9);
            case "bone":
               return pix(16, 64, (u, v, x, y) -> 0xFF000000 | Capes.mix(16777215, 10526880, Math.abs(u - 0.35) * 1.6));
            case "wing_fairy":
               return fairyWing();
            case "wing_butterfly2":
               return butterfly();
            case "mech_blade":
               return mechBlade();
            case "mech_frame":
               return pix(32, 32, (u, v, x, y) -> 0xFF000000 | Capes.shade(Capes.mix(5923440, 2764342, v), 0.9 + 0.2 * Capes.hash(x / 4, y / 4, 3)));
            case "glow_line":
               return pix(16, 16, (u, v, x, y) -> argb(Capes.mix(16777215, 16777215, 0.0), 1.0 - Math.pow(Math.abs(v - 0.5) * 2.0, 2.0)));
            default:
               return Tex3.paint(name);
         }
      }
   }

   static CTex.T feather(int base, int mid, int tip, int shaft, String pat, boolean covert) {
      int W = 64;
      int H = covert ? 128 : 256;
      return img(
         W,
         H,
         g -> {
            double w = W;
            double h = H;
            Path2D f = new java.awt.geom.Path2D.Double();
            if (pat.equals("flame")) {
               f.moveTo(w * 0.1, 0.0);
               f.lineTo(w * 0.9, 0.0);
               f.curveTo(w * 1.02, h * 0.35, w * 0.8, h * 0.62, w * 0.6, h * 0.8);
               f.curveTo(w * 0.62, h * 0.9, w * 0.5, h * 0.96, w * 0.5, h);
               f.curveTo(w * 0.4, h * 0.9, w * 0.3, h * 0.84, w * 0.34, h * 0.74);
               f.curveTo(w * 0.1, h * 0.6, w * -0.02, h * 0.3, w * 0.1, 0.0);
            } else if (covert) {
               f.moveTo(w * 0.04, 0.0);
               f.lineTo(w * 0.96, 0.0);
               f.curveTo(w * 1.0, h * 0.5, w * 0.8, h * 0.9, w * 0.5, h * 0.99);
               f.curveTo(w * 0.2, h * 0.9, w * 0.0, h * 0.5, w * 0.04, 0.0);
            } else {
               f.moveTo(w * 0.1, 0.0);
               f.lineTo(w * 0.9, 0.0);
               f.curveTo(w * 0.98, h * 0.4, w * 0.92, h * 0.8, w * 0.62, h * 0.97);
               f.quadTo(w * 0.5, h * 1.0, w * 0.4, h * 0.97);
               f.curveTo(w * 0.1, h * 0.84, w * 0.02, h * 0.4, w * 0.1, 0.0);
            }

            g.setPaint(
               new LinearGradientPaint(0.0F, 0.0F, 0.0F, (float)h, new float[]{0.0F, 0.45F, 1.0F}, new Color[]{new Color(base), new Color(mid), new Color(tip)})
            );
            if (pat.equals("crystal")) {
               g.setPaint(
                  new LinearGradientPaint(
                     0.0F, 0.0F, 0.0F, (float)h, new float[]{0.0F, 0.45F, 1.0F}, new Color[]{Capes.col(base, 0.85), Capes.col(mid, 0.7), Capes.col(tip, 0.6)}
                  )
               );
            }

            if (pat.equals("flame")) {
               g.setPaint(
                  new LinearGradientPaint(
                     0.0F,
                     0.0F,
                     0.0F,
                     (float)h,
                     new float[]{0.0F, 0.4F, 0.8F, 1.0F},
                     new Color[]{new Color(base), new Color(mid), new Color(tip), Capes.col(tip, 0.1)}
                  )
               );
            }

            g.fill(f);
            g.setClip(f);
            Random r = new Random(covert ? 3L : 4L);
            if (!pat.equals("flame") && !pat.equals("crystal")) {
               g.setStroke(new BasicStroke(1.0F));

               for (double y = 2.0; y < h; y += 3.2) {
                  double wv = 0.5 + r.nextDouble() * 0.2;
                  g.setColor(Capes.col(r.nextBoolean() ? 16777215 : 0, 0.08 + r.nextDouble() * 0.06));
                  g.draw(new java.awt.geom.QuadCurve2D.Double(w / 2.0, y, w * 0.75, y + 6.0, w, y + 14.0 * wv));
                  g.draw(new java.awt.geom.QuadCurve2D.Double(w / 2.0, y, w * 0.25, y + 6.0, 0.0, y + 14.0 * wv));
               }

               g.setColor(Capes.col(0, 0.22));

               for (int i = 0; i < (covert ? 1 : 3); i++) {
                  double y = h * (0.35 + r.nextDouble() * 0.5);
                  double side = r.nextBoolean() ? 1.0 : -1.0;
                  g.draw(new java.awt.geom.QuadCurve2D.Double(w / 2.0 + side * 4.0, y, w / 2.0 + side * 14.0, y + 8.0, w / 2.0 + side * 30.0, y + 20.0));
               }
            }

            if (pat.equals("bars")) {
               for (double y = h * 0.18; y < h * 0.95; y += h * (covert ? 0.3 : 0.17)) {
                  Path2D b = new java.awt.geom.Path2D.Double();
                  b.moveTo(0.0, y);
                  b.quadTo(w / 2.0, y - 7.0, w, y);
                  b.lineTo(w, y + 6.0);
                  b.quadTo(w / 2.0, y - 1.0, 0.0, y + 6.0);
                  b.closePath();
                  g.setColor(Capes.col(2758664, 0.32));
                  g.fill(b);
               }

               g.setColor(Capes.col(16774880, 0.25));
               g.fill(new java.awt.geom.Ellipse2D.Double(w * 0.12, h * 0.05, w * 0.3, h * 0.08));
            }

            if (pat.equals("metal")) {
               g.setPaint(new GradientPaint(0.0F, 0.0F, Capes.col(16777215, 0.55), (float)(w * 0.45), 0.0F, Capes.col(16777215, 0.0)));
               g.fillRect(0, 0, (int)(w * 0.45), (int)h);
               g.setPaint(new GradientPaint((float)(w * 0.6), 0.0F, Capes.col(5913088, 0.0), (float)w, 0.0F, Capes.col(5913088, 0.45)));
               g.fillRect((int)(w * 0.6), 0, (int)w, (int)h);
            }

            if (pat.equals("fire")) {
               g.setPaint(
                  new LinearGradientPaint(
                     0.0F, (float)(h * 0.55), 0.0F, (float)h, new float[]{0.0F, 1.0F}, new Color[]{Capes.col(16769658, 0.0), Capes.col(16769658, 0.7)}
                  )
               );
               g.fillRect(0, (int)(h * 0.55), (int)w, (int)h);
            }

            if (pat.equals("rim")) {
               g.setClip(null);

               for (int i = 4; i >= 1; i--) {
                  g.setColor(Capes.col(10185727, 0.12 * (5 - i)));
                  g.setStroke(new BasicStroke(i * 2.2F));
                  g.draw(f);
               }

               g.setClip(f);
            }

            if (pat.equals("stars")) {
               for (int i = 0; i < (covert ? 10 : 22); i++) {
                  double x = r.nextDouble() * w;
                  double y = r.nextDouble() * h;
                  double s = 1.0 + Math.pow(r.nextDouble(), 3.0) * 4.0;
                  g.setPaint(
                     new RadialGradientPaint(
                        (float)x,
                        (float)y,
                        (float)(s * 2.5),
                        new float[]{0.0F, 0.3F, 1.0F},
                        new Color[]{Color.WHITE, Capes.col(13150463, 0.7), Capes.col(13150463, 0.0)}
                     )
                  );
                  g.fill(new java.awt.geom.Ellipse2D.Double(x - s * 2.5, y - s * 2.5, s * 5.0, s * 5.0));
               }

               g.setPaint(
                  new RadialGradientPaint(
                     (float)(w * 0.4),
                     (float)(h * 0.55),
                     (float)(h * 0.35),
                     new float[]{0.0F, 1.0F},
                     new Color[]{Capes.col(16734896, 0.35), Capes.col(16734896, 0.0)}
                  )
               );
               g.fillRect(0, 0, (int)w, (int)h);
            }

            if (pat.equals("crystal")) {
               g.setStroke(new BasicStroke(1.2F));

               for (int i = 0; i < 7; i++) {
                  double y = r.nextDouble() * h;
                  g.setColor(Capes.col(16777215, 0.5));
                  g.draw(new java.awt.geom.Line2D.Double(0.0, y, w, y + (r.nextDouble() - 0.5) * 60.0));
               }

               g.setPaint(new GradientPaint(0.0F, 0.0F, Capes.col(16777215, 0.6), (float)(w * 0.5), 0.0F, Capes.col(16777215, 0.0)));
               g.fillRect(0, 0, (int)(w * 0.5), (int)h);
            }

            if (pat.equals("flame")) {
               g.setPaint(
                  new RadialGradientPaint(
                     (float)(w / 2.0),
                     (float)(h * 0.15),
                     (float)(h * 0.4),
                     new float[]{0.0F, 1.0F},
                     new Color[]{Capes.col(16777215, 0.7), Capes.col(16777215, 0.0)}
                  )
               );
               g.fillRect(0, 0, (int)w, (int)h);
            }

            if (!pat.equals("flame")) {
               g.setPaint(new GradientPaint((float)(w * 0.5), 0.0F, Capes.col(0, 0.0), (float)w, 0.0F, Capes.col(0, pat.equals("crystal") ? 0.1 : 0.28)));
               g.fillRect((int)(w * 0.5), 0, (int)w, (int)h);
            }

            g.setClip(null);
            if (!pat.equals("flame")) {
               g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(shaft), 0.0F, (float)h, Capes.col(shaft, 0.4)));
               g.setStroke(new BasicStroke(covert ? 2.2F : 2.8F, 1, 1));
               g.draw(new java.awt.geom.QuadCurve2D.Double(w / 2.0, 0.0, w * 0.53, h * 0.5, w * 0.5, h * 0.93));
               g.setColor(Capes.col(Capes.mix(tip, 0, 0.5), pat.equals("crystal") ? 0.3 : 0.55));
               g.setStroke(new BasicStroke(1.4F));
               g.draw(f);
            }
         }
      );
   }

   static CTex.T membrane(int base, int light, int dark, int vein, double alpha) {
      return pix(256, 256, (u, v, x, y) -> {
         double n = Capes.fbm(u * 7.0, v * 7.0, 0, 0, 4, 5);
         double dx = u - 0.6;
         double dy = v - 0.27;
         double r = Math.hypot(dx, dy);
         double th = Math.atan2(dy, dx);
         int c = Capes.ramp(Math.min(1.0, u * 0.8 + v * 0.35), dark, base, light);
         c = Capes.shade(c, 0.82 + 0.3 * n);
         double warp = Capes.fbm(u * 4.0 + 7.0, v * 4.0, 0, 0, 3, 8) * 2.5;
         double rad = Math.abs(Math.sin(th * 11.0 + warp + r * 3.0));
         double veinA = (1.0 - Capes.smooth(0.0, 0.07 + r * 0.05, rad)) * Capes.smooth(0.02, 0.1, r) * 0.55;
         double ring = Math.abs(Math.sin(r * 38.0 + warp * 1.3));
         veinA += (1.0 - Capes.smooth(0.0, 0.05, ring)) * 0.18 * Capes.smooth(0.05, 0.25, r);
         c = Capes.mix(c, vein, Math.min(0.7, veinA));
         c = Capes.add(c, light, Math.pow(n, 3.0) * 0.25);
         return argb(c, alpha * (0.92 + 0.08 * n) - veinA * 0.0);
      });
   }

   static CTex.T fairyWing() {
      return img(
         256,
         128,
         g -> {
            Path2D p = new java.awt.geom.Path2D.Double();
            p.moveTo(2.0, 64.0);
            p.curveTo(60.0, 0.0, 200.0, -10.0, 250.0, 40.0);
            p.curveTo(262.0, 70.0, 200.0, 112.0, 120.0, 110.0);
            p.curveTo(60.0, 108.0, 20.0, 90.0, 2.0, 64.0);
            g.setPaint(
               new LinearGradientPaint(
                  0.0F,
                  0.0F,
                  256.0F,
                  128.0F,
                  new float[]{0.0F, 0.35F, 0.7F, 1.0F},
                  new Color[]{Capes.col(16777215, 0.55), Capes.col(10154239, 0.45), Capes.col(14721279, 0.45), Capes.col(16756960, 0.55)}
               )
            );
            g.fill(p);
            g.setClip(p);
            g.setColor(Capes.col(16777215, 0.55));
            g.setStroke(new BasicStroke(1.6F));

            for (int i = 0; i < 6; i++) {
               g.draw(new java.awt.geom.QuadCurve2D.Double(2.0, 64.0, 110.0, 30 + i * 14, 256.0, i * 26 - 10));
            }

            Random r = new Random(4L);

            for (int i = 0; i < 60; i++) {
               double x = r.nextDouble() * 256.0;
               double y = r.nextDouble() * 128.0;
               double s = 0.8 + r.nextDouble() * 2.0;
               g.setColor(Capes.col(16777215, 0.5 + r.nextDouble() * 0.5));
               g.fill(new java.awt.geom.Ellipse2D.Double(x, y, s, s));
            }

            g.setClip(null);
            g.setColor(Capes.col(16777215, 0.85));
            g.setStroke(new BasicStroke(2.2F));
            g.draw(p);
         }
      );
   }

   static CTex.T butterfly() {
      return img(
         256,
         320,
         g -> {
            Path2D up = new java.awt.geom.Path2D.Double();
            up.moveTo(6.0, 30.0);
            up.curveTo(90.0, -30.0, 250.0, -10.0, 246.0, 80.0);
            up.curveTo(242.0, 140.0, 150.0, 170.0, 8.0, 150.0);
            up.closePath();
            Path2D lo = new java.awt.geom.Path2D.Double();
            lo.moveTo(8.0, 150.0);
            lo.curveTo(120.0, 150.0, 210.0, 200.0, 190.0, 270.0);
            lo.curveTo(170.0, 330.0, 60.0, 320.0, 6.0, 230.0);
            lo.closePath();

            for (Path2D p : new Path2D[]{lo, up}) {
               g.setPaint(
                  new RadialGradientPaint(
                     20.0F,
                     120.0F,
                     250.0F,
                     new float[]{0.0F, 0.35F, 0.7F, 1.0F},
                     new Color[]{new Color(662154), new Color(1997823), new Color(5953791), new Color(2763424)}
                  )
               );
               g.fill(p);
               g.setClip(p);
               g.setColor(Capes.col(329752, 0.8));
               g.setStroke(new BasicStroke(2.4F));

               for (int i = 0; i < 7; i++) {
                  g.draw(
                     new java.awt.geom.QuadCurve2D.Double(
                        8.0, p == up ? 40.0 : 160.0, 120.0, (p == up ? 20 : 170) + i * 22, 260.0, (p == up ? -20 : 180) + i * 34
                     )
                  );
               }

               g.setClip(null);
               g.setColor(new Color(460303));
               g.setStroke(new BasicStroke(12.0F));
               g.draw(p);
            }

            g.setColor(Color.WHITE);
            double[][] dots = new double[][]{
               {230.0, 40.0}, {236.0, 66.0}, {222.0, 96.0}, {196.0, 128.0}, {176.0, 258.0}, {156.0, 284.0}, {126.0, 298.0}, {206.0, 30.0}
            };

            for (double[] d : dots) {
               g.fill(new java.awt.geom.Ellipse2D.Double(d[0] - 5.0, d[1] - 5.0, 10.0, 10.0));
            }

            g.setPaint(
               new RadialGradientPaint(
                  168.0F, 72.0F, 20.0F, new float[]{0.0F, 0.5F, 1.0F}, new Color[]{new Color(16773792), new Color(16756784), new Color(5908992)}
               )
            );
            g.fill(new java.awt.geom.Ellipse2D.Double(148.0, 52.0, 40.0, 40.0));
            g.setColor(new Color(460303));
            g.fill(new java.awt.geom.Ellipse2D.Double(160.0, 64.0, 16.0, 16.0));
            g.setColor(Color.WHITE);
            g.fill(new java.awt.geom.Ellipse2D.Double(163.0, 66.0, 5.0, 5.0));
         }
      );
   }

   static CTex.T mechBlade() {
      return img(
         64,
         256,
         g -> {
            Path2D b = new java.awt.geom.Path2D.Double();
            b.moveTo(4.0, 0.0);
            b.lineTo(60.0, 0.0);
            b.lineTo(58.0, 200.0);
            b.lineTo(32.0, 254.0);
            b.lineTo(6.0, 200.0);
            b.closePath();
            g.setPaint(
               new LinearGradientPaint(
                  0.0F,
                  0.0F,
                  64.0F,
                  0.0F,
                  new float[]{0.0F, 0.45F, 0.55F, 1.0F},
                  new Color[]{new Color(15265012), new Color(10134196), new Color(5923444), new Color(12107984)}
               )
            );
            g.fill(b);
            g.setClip(b);
            g.setColor(Capes.col(1711654, 0.8));
            g.setStroke(new BasicStroke(1.6F));

            for (int y = 40; y < 256; y += 46) {
               g.drawLine(0, y, 64, y + 6);
            }

            g.setColor(new Color(2811647));
            g.setStroke(new BasicStroke(3.0F));
            g.drawLine(32, 10, 32, 236);
            g.setColor(Capes.col(16777215, 0.9));
            g.setStroke(new BasicStroke(1.0F));
            g.drawLine(32, 10, 32, 236);
            g.setClip(null);
            g.setColor(new Color(1711654));
            g.setStroke(new BasicStroke(2.0F));
            g.draw(b);
         }
      );
   }

   static int hueRgb(double h) {
      return Capes.hsv(h, 0.55, 1.0);
   }

   static int addc(int c, int rgb, double a) {
      return Capes.add(c, rgb, a);
   }

   interface PF {
      int at(double var1, double var3, int var5, int var6);
   }
}
