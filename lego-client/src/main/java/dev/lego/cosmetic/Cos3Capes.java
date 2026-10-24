package dev.lego.cosmetic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Path2D.Double;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

final class Cos3Capes {
   private Cos3Capes() {
   }

   static void defineAll() {
      Capes.def("c3void", 16, 8.0F, 14203135, 2756168, 459534, Cos3Capes::voidCloak);
      Capes.def("c3nether", 16, 8.0F, 16751178, 4853256, 1705220, Cos3Capes::nether);
      Capes.def("c3heaven", 16, 8.0F, 16777215, 13146666, 16774360, Cos3Capes::heaven);
      Capes.def("c3sunflower", 12, 6.0F, 16769658, 6982186, 2775706, Cos3Capes::sunflower);
      Capes.def("c3pumpkin", 16, 10.0F, 16756810, 3807752, 1313310, Cos3Capes::pumpkin);
      Capes.def("c3ghost", 16, 8.0F, 13172724, 1722962, 529438, Cos3Capes::ghost);
      Capes.def("c3skull", 16, 8.0F, 13172656, 1714712, 395270, Cos3Capes::skull);
      Capes.def("c3bolt_purple", 16, 10.0F, 3811914, 920084, 460044, (g, i, ph, k, n) -> neonBolt(g, i, ph, k, n, 12606207));
      Capes.def("c3bolt_blue", 16, 10.0F, 2767438, 658966, 329484, (g, i, ph, k, n) -> neonBolt(g, i, ph, k, n, 3844351));
      Capes.def("c3bolt_green", 16, 10.0F, 2771506, 660494, 330759, (g, i, ph, k, n) -> neonBolt(g, i, ph, k, n, 3866490));
      Capes.def("c3bolt_orange", 16, 10.0F, 5126698, 1445898, 788485, (g, i, ph, k, n) -> neonBolt(g, i, ph, k, n, 16747050));
      Capes.def("c3embertree", 16, 8.0F, 16758890, 3807754, 1181702, Cos3Capes::emberTree);
   }

   static void voidCloak(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      double cx = 80.0;
      double cy = 118.0;
      Capes.field(img, (u, v, xx, yx) -> {
         double dx = xx - 80.0;
         double dy = (yx - 118.0) * 0.82;
         double r = Math.hypot(dx, dy) + 0.001;
         double th = Math.atan2(dy, dx) / (Math.PI * 2) + 0.5;
         double sp = th * 6.0 + Math.log(r) * 2.6 - t * 6.0;
         double w = Capes.fbm(sp, Math.log(r) * 2.2, 6, 0, 4, 71);
         double arms = 0.5 + 0.5 * Math.sin((th * 3.0 + Math.log(r) * 0.9 - t) * Math.PI * 2.0 + (w - 0.5) * 3.0);
         double core = Math.exp(-r / 20.0);
         double dens = Math.pow(arms, 2.2) * (0.35 + 0.9 * w) * Math.exp(-r / 110.0);
         int c = Capes.ramp(v, 656404, 459534, 262664);
         c = Capes.add(c, 6953672, dens * 0.9);
         c = Capes.add(c, 13794047, Math.pow(dens, 2.4) * 1.2);
         double rim = Math.exp(-Math.pow((r - 15.0) / 3.2, 2.0));
         if (r < 15.0) {
            c = Capes.mix(c, 0, Capes.smooth(15.0, 9.0, r));
         }

         c = Capes.add(c, 15780095, rim * (0.55 + 0.25 * Math.sin(ph * 2.0 + th * 12.0)));
         c = Capes.add(c, 3803754, core * 0.2);
         if (Capes.hash(xx, yx, 72) > 0.993) {
            c = Capes.add(c, 16777215, 0.3 + 0.5 * Capes.hash(xx, yx, 73));
         }

         return c;
      });
      Random rn = new Random(77L);

      for (int i = 0; i < 40; i++) {
         double r0 = 20.0 + rn.nextDouble() * 70.0;
         double a0 = rn.nextDouble() * Math.PI * 2.0;
         double a = a0 + ph * (rn.nextBoolean() ? 1 : 2) * (40.0 / r0 > 1.0 ? 1 : 1);
         double x = 80.0 + Math.cos(a) * r0;
         double y = 118.0 + Math.sin(a) * r0 * 1.2;
         Capes.dot(g, x, y, 1.6 + rn.nextDouble() * 1.8, 14723327, 0.55 + 0.35 * Math.sin(ph + i));
      }

      g.setStroke(new BasicStroke(1.6F, 1, 1));

      for (int side = 0; side < 2; side++) {
         for (int i = 0; i < 7; i++) {
            double x = side == 0 ? 22.0 : 138.0;
            double y = 30 + i * 32;
            double lit = Math.pow(0.5 + 0.5 * Math.cos(ph - (i + side * 3.5) * 0.9), 3.0);
            rune(g, x, y, 7.0, (i * 3 + side * 5) % 8, 13138687, 0.35 + 0.65 * lit);
            if (lit > 0.05) {
               Capes.addDot(img, x, y, 12.0, 10502399, 0.35 * lit);
            }
         }
      }

      for (int ix = 0; ix < 5; ix++) {
         double a = Math.PI * (1.15 + ix * 0.175);
         double x = 80.0 + Math.cos(a) * 54.0;
         double y = 118.0 + Math.sin(a) * 60.0;
         double lit = Math.pow(0.5 + 0.5 * Math.cos(ph * 2.0 - ix * 1.2), 2.0);
         rune(g, x, y, 6.0, (ix + 2) % 8, 15782143, 0.4 + 0.6 * lit);
      }
   }

   static void rune(Graphics2D g, double x, double y, double s, int kind, int rgb, double a) {
      Path2D p = new Double();
      switch (kind % 8) {
         case 0:
            p.moveTo(-s, s);
            p.lineTo(0.0, -s);
            p.lineTo(s, s);
            p.closePath();
            p.append(new java.awt.geom.Ellipse2D.Double(-s * 0.22, s * 0.1, s * 0.44, s * 0.44), false);
            break;
         case 1:
            p.moveTo(0.0, -s);
            p.lineTo(0.0, s);
            p.moveTo(-s, -s * 0.4);
            p.lineTo(0.0, 0.0);
            p.lineTo(s, -s * 0.4);
            break;
         case 2:
            p.append(new java.awt.geom.Ellipse2D.Double(-s * 0.6, -s * 0.6, s * 1.2, s * 1.2), false);
            p.moveTo(0.0, -s);
            p.lineTo(0.0, s);
            break;
         case 3:
            p.moveTo(-s, -s);
            p.lineTo(s, -s);
            p.lineTo(-s, s);
            p.lineTo(s, s);
            p.closePath();
            p.moveTo(-s * 1.2, 0.0);
            p.lineTo(s * 1.2, 0.0);
            break;
         case 4:
            p.moveTo(0.0, -s);
            p.lineTo(s, 0.0);
            p.lineTo(0.0, s);
            p.lineTo(-s, 0.0);
            p.closePath();
            p.moveTo(0.0, -s * 0.3);
            p.lineTo(0.0, s * 0.3);
            break;
         case 5:
            p.moveTo(-s, s);
            p.lineTo(-s, -s);
            p.lineTo(s * 0.6, -s * 0.2);
            p.moveTo(-s, 0.0);
            p.lineTo(s, s);
            break;
         case 6:
            p.moveTo(-s, -s * 0.2);
            p.quadTo(0.0, -s * 1.6, s, -s * 0.2);
            p.moveTo(0.0, -s * 0.8);
            p.lineTo(0.0, s);
            p.moveTo(-s * 0.6, s);
            p.lineTo(s * 0.6, s);
            break;
         default:
            p.moveTo(-s, 0.0);
            p.lineTo(s, 0.0);
            p.moveTo(-s * 0.5, -s);
            p.lineTo(s * 0.5, s);
            p.moveTo(s * 0.5, -s);
            p.lineTo(-s * 0.5, s);
      }

      Shape sh = AffineTransform.getTranslateInstance(x, y).createTransformedShape(p);
      Capes.glowStroke(g, sh, rgb, 1.5F, 1.2F, a);
   }

   static void nether(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      Capes.field(img, (u, v, xx, yx) -> {
         double wv = Capes.fbm(u * 3.0, v * 5.0 - t * 5.0, 3, 5, 3, 81);
         double f = Capes.fbm(u * 4.0 + (wv - 0.5) * 1.2, v * 6.0 - t * 6.0, 4, 6, 5, 82);
         double ridge = 1.0 - Math.abs(f - 0.5) * 2.0;
         double vein = Math.pow(Capes.smooth(0.9, 0.99, ridge), 1.5);
         double rock = Capes.fbm(u * 10.0, v * 16.0, 10, 16, 3, 83);
         int c = Capes.ramp(rock, 1312262, 2755594, 4068368, 4856854);
         c = Capes.shade(c, 0.7 + 0.5 * Capes.smooth(0.3, 0.9, ridge));
         double pulse = 0.8 + 0.2 * Math.sin(ph + v * 6.0);
         c = Capes.add(c, 16726538, vein * 1.1 * pulse);
         c = Capes.add(c, 16760906, Math.pow(vein, 3.0) * 0.9 * pulse);
         return Capes.add(c, 16722442, Capes.smooth(0.55, 1.0, v) * 0.25);
      });
      Random rn = new Random(84L);

      for (int i = 0; i < 7; i++) {
         double x = 18 + i * 21 + rn.nextDouble() * 8.0;
         double h = 26.0 + rn.nextDouble() * 34.0;
         double w = 7.0 + rn.nextDouble() * 5.0;
         double lean = (rn.nextDouble() - 0.5) * 0.5;
         Path2D p = new Double();
         p.moveTo(x - w / 2.0, 250.0);
         p.lineTo(x - w / 2.0 + lean * h * 0.6, 250.0 - h * 0.75);
         p.lineTo(x + lean * h, 250.0 - h);
         p.lineTo(x + w / 2.0 + lean * h * 0.6, 250.0 - h * 0.75);
         p.lineTo(x + w / 2.0, 250.0);
         p.closePath();
         Capes.addDot(img, x + lean * h * 0.5, 250.0 - h * 0.5, h * 0.7, 16722506, 0.35 + 0.15 * Math.sin(ph + i));
         g.setPaint(new GradientPaint((float)x, (float)(256.0 - h), new Color(16738938), (float)(x + w), 256.0F, new Color(9046558)));
         g.fill(p);
         g.setColor(Capes.col(16765144, 0.7));
         g.setStroke(new BasicStroke(1.2F));
         g.draw(new java.awt.geom.Line2D.Double(x, 250.0, x + lean * h, 250.0 - h));
      }

      for (int i = 0; i < 60; i++) {
         double x0 = rn.nextDouble() * 160.0;
         double y0 = rn.nextDouble();
         double sp = 1 + rn.nextInt(2);
         double y = 256.0 - (y0 + sp * t) % 1.0 * 276.0 + 10.0;
         double x = x0 + Math.sin(ph * sp + i) * 6.0;
         boolean ember = i % 3 != 0;
         double life = (y0 + sp * t) % 1.0;
         if (ember) {
            Capes.dot(g, x, y, 2.2 + rn.nextDouble() * 1.5, 16747050, 0.9 * (1.0 - life * 0.7));
         } else {
            Capes.dot(g, x, y, 1.6, 9079434, 0.5);
         }
      }
   }

   static void heaven(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      Capes.field(img, (u, v, xx, yx) -> {
         int cxx = Capes.ramp(v, 16773836, 16044154, 14459450, 11038746);
         double cl = Capes.fbm(u * 4.0 + t * 4.0, v * 3.0, 4, 0, 4, 91);
         cxx = Capes.mix(cxx, 16775392, Capes.smooth(0.5, 0.8, cl) * 0.45 * (1.0 - v));
         double ang = Math.atan2(yx + 40, xx - 80);
         double ray = Math.pow(0.5 + 0.5 * Math.sin(ang * 22.0 + ph), 6.0) * Math.exp(-v * 2.2);
         return Capes.add(cxx, 16774864, ray * 0.35);
      });
      Random rn = new Random(92L);

      for (int b = 0; b < 5; b++) {
         double x = 20 + b * 30 + rn.nextDouble() * 10.0;
         double y = 0.0;
         Path2D p = new Double();
         p.moveTo(x, y);
         double dir = rn.nextBoolean() ? 1.0 : -1.0;

         while (y < 256.0) {
            x += (rn.nextDouble() - 0.5) * 16.0 + dir * 2.0;
            if (x < 14.0 || x > 146.0) {
               dir = -dir;
            }

            y += 10.0 + rn.nextDouble() * 12.0;
            p.lineTo(x, y);
            if (rn.nextDouble() < 0.3) {
               Path2D q = new Double();
               q.moveTo(x, y);
               double qx = x;
               double qy = y;

               for (int s = 0; s < 3; s++) {
                  qx += (rn.nextBoolean() ? 1 : -1) * (5.0 + rn.nextDouble() * 8.0);
                  qy += 7.0 + rn.nextDouble() * 8.0;
                  q.lineTo(qx, qy);
               }

               Capes.glowStroke(g, q, 16762954, 0.9F, 1.0F, 0.6);
            }
         }

         Capes.glowStroke(g, p, 16758826, 1.4F, 1.3F, 0.75);

         for (int s = 0; s < 2; s++) {
            double pos = (t + s * 0.5 + b * 0.21) % 1.0;
            PathIterator it = p.getPathIterator(null, 1.0);
            double[] c = new double[6];
            double py = pos * 256.0;
            double lx = 0.0;
            double ly = 0.0;

            while (!it.isDone()) {
               it.currentSegment(c);
               if (c[1] >= py) {
                  double f = ly == c[1] ? 0.0 : (py - ly) / (c[1] - ly);
                  double px = lx + (c[0] - lx) * f;
                  Capes.addDot(img, px, py, 10.0, 16777215, 0.8);
                  Capes.addDot(img, px, py, 22.0, 16765024, 0.35);
                  break;
               }

               lx = c[0];
               ly = c[1];
               it.next();
            }
         }
      }

      double cx = 80.0;
      double cy = 96.0;
      Capes.addDot(img, cx, cy, 40.0, 16769162, 0.5 + 0.15 * Math.sin(ph));
      g.setColor(Capes.col(16777215, 0.95));
      g.setStroke(new BasicStroke(2.2F));
      g.draw(new java.awt.geom.Ellipse2D.Double(cx - 18.0, cy - 18.0, 36.0, 36.0));

      for (int i = 0; i < 12; i++) {
         double a = i * Math.PI / 6.0 + ph / 6.0;
         double r0 = 22.0;
         double r1 = i % 2 == 0 ? 36.0 : 29.0;
         Capes.glowStroke(
            g,
            new java.awt.geom.Line2D.Double(cx + Math.cos(a) * r0, cy + Math.sin(a) * r0, cx + Math.cos(a) * r1, cy + Math.sin(a) * r1),
            16762954,
            1.8F,
            1.2F,
            0.9
         );
      }

      g.setPaint(new RadialGradientPaint((float)cx, (float)cy, 13.0F, new float[]{0.0F, 1.0F}, new Color[]{Color.WHITE, new Color(16767082)}));
      g.fill(new java.awt.geom.Ellipse2D.Double(cx - 12.0, cy - 12.0, 24.0, 24.0));

      for (int i = 0; i < 30; i++) {
         double x = 12.0 + rn.nextDouble() * 136.0;
         double y = 12.0 + rn.nextDouble() * 232.0;
         Capes.star(g, x, y, 1.0 + rn.nextDouble() * 1.6, 16771232, Math.pow(Math.max(0.0, Math.sin(ph + rn.nextDouble() * 6.28)), 2.0));
      }
   }

   static void sunflower(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      Capes.field(img, (u, v, x, y) -> {
         int c = Capes.ramp(v, 3832536, 6990576, 12116223, 15267583);
         double cl = Capes.fbm(u * 3.0 + ph / (Math.PI * 2) * 3.0, v * 5.0, 3, 0, 4, 101);
         c = Capes.mix(c, 16777215, Capes.smooth(0.55, 0.75, cl) * (v < 0.5 ? 0.8 : 0.3));
         double hill = 0.78 + 0.03 * Math.sin(u * 7.0 + 1.0);
         if (v > hill) {
            c = Capes.mix(5941306, 2779678, (v - hill) * 4.0 + 0.15 * Capes.hash(x / 3, y / 3, 102));
         }

         return c;
      });
      double sway = Math.sin(ph) * 0.06;
      Path2D stem = new Double();
      stem.moveTo(78.0, 256.0);
      stem.curveTo(74.0, 200.0, 90.0 + sway * 60.0, 160.0, 80.0 + sway * 90.0, 108.0);
      g.setColor(new Color(2783778));
      g.setStroke(new BasicStroke(7.0F, 1, 1));
      g.draw(stem);
      g.setColor(new Color(5945402));
      g.setStroke(new BasicStroke(2.0F, 1, 1));
      g.draw(stem);

      for (int s = -1; s <= 1; s += 2) {
         AffineTransform o = g.getTransform();
         g.translate(80 + s * 2, 190 - s * 14);
         g.rotate(s * (0.7 + sway * 2.0));
         Path2D leaf = new Double();
         leaf.moveTo(0.0, 0.0);
         leaf.curveTo(s * 14, -16.0, s * 34, -10.0, s * 42, 0.0);
         leaf.curveTo(s * 30, 10.0, s * 12, 12.0, 0.0, 0.0);
         g.setPaint(new GradientPaint(0.0F, -10.0F, new Color(6998090), 0.0F, 12.0F, new Color(2783778)));
         g.fill(leaf);
         g.setColor(Capes.col(1726994, 0.8));
         g.setStroke(new BasicStroke(1.2F));
         g.draw(new java.awt.geom.Line2D.Double(0.0, 0.0, s * 38, 0.0));
         g.setTransform(o);
      }

      double cx = 80.0 + sway * 90.0;
      double cy = 96.0;
      Capes.addDot(img, cx, cy, 70.0, 16769658, 0.35);
      AffineTransform o = g.getTransform();
      g.translate(cx, cy);
      g.rotate(sway * 1.5);

      for (int ring = 0; ring < 2; ring++) {
         int np = 18;

         for (int i = 0; i < np; i++) {
            double a = (i + ring * 0.5) * Math.PI * 2.0 / np;
            AffineTransform o2 = g.getTransform();
            g.rotate(a);
            double len = ring == 0 ? 50.0 : 42.0;
            double wid = ring == 0 ? 11.0 : 10.0;
            Path2D p = new Double();
            p.moveTo(18.0, 0.0);
            p.curveTo(26.0, -wid, len - 6.0, -wid * 0.7, len, 0.0);
            p.curveTo(len - 6.0, wid * 0.7, 26.0, wid, 18.0, 0.0);
            g.setPaint(new GradientPaint(18.0F, 0.0F, new Color(ring == 0 ? 15243786 : 16036378), (float)len, 0.0F, new Color(ring == 0 ? 16765498 : 16770154)));
            g.fill(p);
            g.setColor(Capes.col(12085760, 0.5));
            g.setStroke(new BasicStroke(0.9F));
            g.draw(new java.awt.geom.Line2D.Double(22.0, 0.0, len - 6.0, 0.0));
            g.setTransform(o2);
         }
      }

      g.setPaint(
         new RadialGradientPaint(0.0F, 0.0F, 22.0F, new float[]{0.0F, 0.7F, 1.0F}, new Color[]{new Color(3808778), new Color(5910542), new Color(8014354)})
      );
      g.fill(new java.awt.geom.Ellipse2D.Double(-22.0, -22.0, 44.0, 44.0));

      for (int i = 0; i < 150; i++) {
         double r = Math.sqrt(i) * 1.75;
         double a = i * 2.39996;
         if (r > 20.0) {
            break;
         }

         g.setColor(new Color(Capes.mix(13144106, 2757638, r / 22.0)));
         g.fill(new java.awt.geom.Ellipse2D.Double(Math.cos(a) * r - 0.9, Math.sin(a) * r - 0.9, 1.8, 1.8));
      }

      g.setTransform(o);
      Random rn = new Random(103L);

      for (int i = 0; i < 18; i++) {
         double a = rn.nextDouble() * 6.28 + ph;
         double r = 60.0 + rn.nextDouble() * 20.0;
         Capes.dot(g, cx + Math.cos(a) * r * 0.8, cy + Math.sin(a) * r, 1.8, 16774304, 0.8);
      }

      double bx = cx + Math.cos(ph) * 52.0;
      double by = cy + 40.0 + Math.sin(ph * 2.0) * 14.0;
      g.setColor(new Color(16765498));
      g.fill(new java.awt.geom.Ellipse2D.Double(bx - 6.0, by - 4.0, 12.0, 8.0));
      g.setColor(new Color(2759178));
      g.fill(new java.awt.geom.Rectangle2D.Double(bx - 2.0, by - 4.0, 2.0, 8.0));
      g.fill(new java.awt.geom.Rectangle2D.Double(bx + 2.0, by - 4.0, 2.0, 8.0));
      g.setColor(Capes.col(16777215, 0.8));
      g.fill(new java.awt.geom.Ellipse2D.Double(bx - 4.0, by - 10.0, 6.0, 6.0));
      g.fill(new java.awt.geom.Ellipse2D.Double(bx, by - 10.0, 6.0, 6.0));
   }

   static void pumpkin(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      double fl = 0.8 + 0.12 * Math.sin(ph * 3.0) + 0.08 * Math.sin(ph * 7.0 + 1.0);
      Capes.field(img, (u, v, xx, yx) -> {
         int c = Capes.ramp(v, 1706542, 2757184, 3807792, 1706510);
         double fog = Capes.fbm(u * 3.0 + t * 3.0, v * 2.0, 3, 0, 4, 111);
         c = Capes.add(c, 6961802, Capes.smooth(0.5, 0.8, fog) * 0.25 * v);
         c = Capes.add(c, 16742938, Math.exp(-Math.hypot(xx - 80, yx - 150) / 50.0) * 0.35 * fl);
         if (Capes.hash(xx, yx, 112) > 0.994 && v < 0.5) {
            c = Capes.add(c, 16777215, 0.5);
         }

         return c;
      });
      g.setPaint(new RadialGradientPaint(124.0F, 38.0F, 16.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(16774872), new Color(15784080)}));
      g.fill(new java.awt.geom.Ellipse2D.Double(110.0, 24.0, 28.0, 28.0));

      for (int i = 0; i < 3; i++) {
         double bt = (t + i / 3.0) % 1.0;
         double bx = -20.0 + bt * 200.0;
         double by = 40 + i * 22 + Math.sin(bt * Math.PI * 4.0) * 6.0;
         bat(g, bx, by, 6 + i, Math.sin(ph * 4.0 + i) > 0.0);
      }

      double cx = 80.0;
      double cy = 150.0;

      for (int i = -2; i <= 2; i++) {
         double w = 34 - Math.abs(i) * 6;
         g.setPaint(
            new GradientPaint(
               (float)(cx + i * 16 - w),
               (float)(cy - 40.0),
               new Color(Math.abs(i) == 2 ? 13127690 : 16747034),
               (float)(cx + i * 16 + w),
               (float)(cy + 40.0),
               new Color(11026442)
            )
         );
         g.fill(new java.awt.geom.Ellipse2D.Double(cx + i * 15 - w, cy - 42.0, w * 2.0, 84.0));
      }

      g.setColor(new Color(4876826));
      g.fill(new java.awt.geom.RoundRectangle2D.Double(cx - 5.0, cy - 56.0, 10.0, 18.0, 4.0, 4.0));
      Path2D face = new Double();
      face.append(new Double(tri(cx - 30.0, cy - 6.0, cx - 10.0, cy - 6.0, cx - 18.0, cy - 26.0)), false);
      face.append(new Double(tri(cx + 30.0, cy - 6.0, cx + 10.0, cy - 6.0, cx + 18.0, cy - 26.0)), false);
      face.append(new Double(tri(cx - 5.0, cy + 6.0, cx + 5.0, cy + 6.0, cx, cy - 3.0)), false);
      Path2D m = new Double();
      m.moveTo(cx - 34.0, cy + 12.0);

      for (int i = 0; i <= 8; i++) {
         m.lineTo(cx - 34.0 + i * 8.5, cy + 14.0 + (i % 2 == 0 ? 0 : 7) + Math.sin(i * 0.4) * 2.0);
      }

      m.quadTo(cx, cy + 44.0, cx - 34.0, cy + 12.0);
      face.append(m, false);
      Capes.addDot(img, cx, cy + 6.0, 60.0, 16751146, 0.35 * fl);
      g.setColor(new Color(Capes.mix(5904896, 16769146, fl)));
      g.fill(face);
      g.setPaint(
         new RadialGradientPaint((float)cx, (float)(cy + 4.0), 30.0F, new float[]{0.0F, 1.0F}, new Color[]{Capes.col(16777184, fl), Capes.col(16756778, 0.0)})
      );
      g.fill(face);
      Capes.glowStroke(g, face, 16756794, 0.8F, 1.4F, 0.8 * fl);
      Random rn = new Random(113L);

      for (int i = 0; i < 20; i++) {
         double x = rn.nextDouble() * 160.0;
         double y0 = rn.nextDouble();
         double y = 256.0 - (y0 + t) % 1.0 * 256.0;
         Capes.dot(g, x + Math.sin(ph + i) * 5.0, y, 2.0, 16756810, 0.7);
      }
   }

   private static Path2D tri(double x0, double y0, double x1, double y1, double x2, double y2) {
      Path2D p = new Double();
      p.moveTo(x0, y0);
      p.lineTo(x1, y1);
      p.lineTo(x2, y2);
      p.closePath();
      return p;
   }

   private static void bat(Graphics2D g, double x, double y, double s, boolean up) {
      Path2D p = new Double();
      double wy = up ? -s * 0.9 : s * 0.4;
      p.moveTo(x, y - s * 0.3);
      p.quadTo(x - s, y + wy - s * 0.2, x - s * 2.0, y + wy);
      p.quadTo(x - s * 1.4, y + s * 0.2, x - s * 0.9, y + s * 0.5);
      p.quadTo(x - s * 0.5, y + s * 0.2, x, y + s * 0.6);
      p.quadTo(x + s * 0.5, y + s * 0.2, x + s * 0.9, y + s * 0.5);
      p.quadTo(x + s * 1.4, y + s * 0.2, x + s * 2.0, y + wy);
      p.quadTo(x + s, y + wy - s * 0.2, x, y - s * 0.3);
      g.setColor(new Color(656400));
      g.fill(p);
   }

   static void ghost(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      Capes.field(img, (u, v, x, y) -> {
         int c = Capes.ramp(v, 662058, 1058874, 926256, 529440);
         double f = Capes.fbm(u * 3.0 - t * 3.0, v * 4.0, 3, 0, 4, 121);
         c = Capes.add(c, 2787978, Capes.smooth(0.55, 0.85, f) * 0.22);
         if (Capes.hash(x, y, 122) > 0.993) {
            c = Capes.add(c, 13172735, 0.5);
         }

         return c;
      });
      double cx = 80.0;
      double cy = 112.0 + Math.sin(ph) * 8.0;
      Capes.addDot(img, cx, cy, 70.0, 5963744, 0.3);
      Ellipse2D ring = new java.awt.geom.Ellipse2D.Double(cx - 56.0, cy - 56.0, 112.0, 112.0);
      Capes.glowStroke(g, ring, 7012328, 2.4F, 2.2F, 0.9);

      for (int i = 0; i < 10; i++) {
         double a = ph + i * Math.PI / 5.0;
         double r = 56.0;
         Capes.dot(g, cx + Math.cos(a) * r, cy + Math.sin(a) * r, 4.0, 14745599, 0.9);
      }

      Path2D b = new Double();
      double w = 34.0;
      double top = cy - 44.0;
      double bot = cy + 36.0;
      b.moveTo(cx - w, bot);
      b.lineTo(cx - w, cy - 10.0);
      b.curveTo(cx - w, top - 4.0, cx + w, top - 4.0, cx + w, cy - 10.0);
      b.lineTo(cx + w, bot);
      int waves = 4;

      for (int i = 0; i < waves; i++) {
         double x0 = cx + w - i * (2.0 * w / waves);
         double x1 = x0 - w / waves;
         double x2 = x0 - 2.0 * w / waves;
         double wob = Math.sin(ph * 2.0 + i * 1.6) * 4.0;
         b.quadTo(x1, bot + 12.0 + wob, x2, bot);
      }

      b.closePath();
      g.setColor(Capes.col(3866592, 0.25));
      g.setStroke(new BasicStroke(8.0F));
      g.draw(b);
      g.setPaint(new GradientPaint((float)cx, (float)top, new Color(16777215), (float)cx, (float)bot, new Color(13166832)));
      g.fill(b);
      double blink = k % 8 == 5 ? 0.15 : 1.0;
      g.setColor(new Color(1712176));
      g.fill(new java.awt.geom.Ellipse2D.Double(cx - 17.0, cy - 16.0, 11.0, 14.0 * blink));
      g.fill(new java.awt.geom.Ellipse2D.Double(cx + 6.0, cy - 16.0, 11.0, 14.0 * blink));
      g.setColor(Color.WHITE);
      if (blink > 0.5) {
         g.fill(new java.awt.geom.Ellipse2D.Double(cx - 14.0, cy - 14.0, 4.0, 4.0));
         g.fill(new java.awt.geom.Ellipse2D.Double(cx + 9.0, cy - 14.0, 4.0, 4.0));
      }

      g.setColor(Capes.col(16747176, 0.7));
      g.fill(new java.awt.geom.Ellipse2D.Double(cx - 25.0, cy + 1.0, 10.0, 6.0));
      g.fill(new java.awt.geom.Ellipse2D.Double(cx + 15.0, cy + 1.0, 10.0, 6.0));
      g.setColor(new Color(1712176));
      g.fill(new java.awt.geom.Ellipse2D.Double(cx - 5.0, cy + 2.0, 10.0, 8.0 + Math.sin(ph * 2.0) * 2.0));
      g.setColor(new Color(15398650));
      g.fill(new java.awt.geom.Ellipse2D.Double(cx - w - 8.0, cy + 4.0 + Math.sin(ph * 2.0) * 3.0, 14.0, 9.0));
      g.fill(new java.awt.geom.Ellipse2D.Double(cx + w - 6.0, cy + 4.0 - Math.sin(ph * 2.0) * 3.0, 14.0, 9.0));
   }

   static void skull(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double pulse = 0.75 + 0.25 * Math.sin(ph);
      Capes.field(img, (u, v, xx, yx) -> {
         int c = Capes.ramp(v, 461319, 658954, 395270);
         double d = Math.hypot(xx - 80, (yx - 108) * 0.9);
         c = Capes.add(c, 3866442, Math.exp(-d / 46.0) * 0.3 * pulse);
         double grid = xx % 16 != 0 && yx % 16 != 0 ? 0.0 : 0.05;
         return Capes.add(c, 3866442, grid);
      });
      double cx = 80.0;
      double cy = 104.0;
      Path2D sk = new Double();
      sk.moveTo(cx - 34.0, cy + 6.0);
      sk.curveTo(cx - 40.0, cy - 50.0, cx + 40.0, cy - 50.0, cx + 34.0, cy + 6.0);
      sk.curveTo(cx + 34.0, cy + 20.0, cx + 22.0, cy + 22.0, cx + 20.0, cy + 34.0);
      sk.lineTo(cx - 20.0, cy + 34.0);
      sk.curveTo(cx - 22.0, cy + 22.0, cx - 34.0, cy + 20.0, cx - 34.0, cy + 6.0);
      sk.closePath();
      g.setPaint(new GradientPaint((float)cx, (float)(cy - 40.0), new Color(16056304), (float)cx, (float)(cy + 34.0), new Color(12110000)));
      g.fill(sk);
      Capes.glowStroke(g, sk, 7012202, 1.6F, 2.0F, 0.9 * pulse);
      g.setColor(new Color(14214352));
      g.fill(new java.awt.geom.RoundRectangle2D.Double(cx - 18.0, cy + 32.0, 36.0, 14.0, 8.0, 8.0));
      g.setColor(new Color(1054222));

      for (int i = 0; i < 5; i++) {
         g.fill(new java.awt.geom.Rectangle2D.Double(cx - 15.0 + i * 7, cy + 33.0, 1.4, 11.0));
      }

      g.fill(new java.awt.geom.Rectangle2D.Double(cx - 17.0, cy + 38.0, 34.0, 1.2));

      for (int s = -1; s <= 1; s += 2) {
         double ex = cx + s * 14;
         double ey = cy + 2.0;
         g.setColor(new Color(659466));
         g.fill(new java.awt.geom.Ellipse2D.Double(ex - 10.0, ey - 9.0, 20.0, 18.0));
         double fl = 0.7 + 0.3 * Math.sin(ph * 3.0 + s);
         Capes.addDot(img, ex, ey, 16.0, 4915034, 0.9 * fl);
         Capes.dot(g, ex, ey, 5.0, 15400938, fl);
      }

      Path2D nose = tri(cx - 5.0, cy + 22.0, cx + 5.0, cy + 22.0, cx, cy + 12.0);
      g.setColor(new Color(659466));
      g.fill(nose);
      g.setColor(Capes.col(5925462, 0.9));
      g.setStroke(new BasicStroke(1.2F));
      Path2D cr = new Double();
      cr.moveTo(cx + 8.0, cy - 38.0);
      cr.lineTo(cx + 12.0, cy - 28.0);
      cr.lineTo(cx + 6.0, cy - 22.0);
      cr.lineTo(cx + 10.0, cy - 14.0);
      g.draw(cr);

      for (int s = -1; s <= 1; s += 2) {
         AffineTransform o = g.getTransform();
         g.translate(cx, cy + 82.0);
         g.rotate(s * 0.6);
         g.setColor(new Color(14214352));
         g.fill(new java.awt.geom.RoundRectangle2D.Double(-40.0, -4.0, 80.0, 8.0, 6.0, 6.0));

         for (int e = -1; e <= 1; e += 2) {
            g.fill(new java.awt.geom.Ellipse2D.Double(e * 40 - 7, -9.0, 10.0, 10.0));
            g.fill(new java.awt.geom.Ellipse2D.Double(e * 40 - 7, -1.0, 10.0, 10.0));
         }

         g.setTransform(o);
      }

      Random rn = new Random(131L);
      double tt = (double)k / n;

      for (int i = 0; i < 26; i++) {
         double x = rn.nextDouble() * 160.0;
         double y0 = rn.nextDouble();
         double y = 256.0 - (y0 + tt) % 1.0 * 256.0;
         Capes.dot(g, x + Math.sin(ph + i) * 4.0, y, 2.2, 7012202, 0.6);
      }
   }

   static void neonBolt(Graphics2D g, BufferedImage img, double ph, int k, int n, int neon) {
      double pulse = 0.7 + 0.3 * Math.sin(ph);
      boolean flick = k == 5 || k == 11;
      Capes.field(img, (u, v, xx, yx) -> {
         int c = Capes.ramp(v, 789522, 592142, 394761);
         double wv = (xx / 4 + yx / 4) % 2 == 0 ? 1.06 : 0.94;
         c = Capes.shade(c, wv);
         double d = Math.hypot(xx - 80, yx - 118);
         return Capes.add(c, neon, Math.exp(-d / 60.0) * 0.18 * pulse);
      });
      RoundRectangle2D edge = new java.awt.geom.RoundRectangle2D.Double(13.0, 13.0, 134.0, 230.0, 10.0, 10.0);
      Capes.glowStroke(g, edge, neon, 1.6F, 1.6F, 0.9 * pulse);
      double per = 728.0;
      double pos = ph / (Math.PI * 2) * per;

      for (int s = 0; s < 2; s++) {
         double q = (pos + s * per / 2.0) % per;
         double w = 134.0;
         double h = 230.0;
         double x;
         double y;
         if (q < w) {
            x = 13.0 + q;
            y = 13.0;
         } else if (q < w + h) {
            x = 13.0 + w;
            y = 13.0 + q - w;
         } else if (q < 2.0 * w + h) {
            x = 13.0 + w - (q - w - h);
            y = 13.0 + h;
         } else {
            x = 13.0;
            y = 13.0 + h - (q - 2.0 * w - h);
         }

         Capes.addDot(img, x, y, 12.0, neon, 0.9);
         Capes.addDot(img, x, y, 4.0, 16777215, 0.9);
      }

      Path2D b = new Double();
      b.moveTo(96.0, 34.0);
      b.lineTo(46.0, 132.0);
      b.lineTo(76.0, 132.0);
      b.lineTo(58.0, 222.0);
      b.lineTo(116.0, 108.0);
      b.lineTo(84.0, 108.0);
      b.lineTo(108.0, 34.0);
      b.closePath();
      double a = flick ? 0.45 : pulse;
      Capes.addDot(img, 80.0, 128.0, 70.0, neon, 0.35 * a);
      g.setColor(new Color(Capes.mix(657936, neon, 0.25)));
      g.fill(b);
      Capes.glowStroke(g, b, neon, 2.4F, 2.6F, a);
      g.setPaint(new GradientPaint(60.0F, 40.0F, Capes.col(16777215, 0.25 * a), 100.0F, 220.0F, Capes.col(neon, 0.05)));
      g.fill(b);
      Random rn = new Random(141L);

      for (int i = 0; i < 18; i++) {
         double x = 30.0 + rn.nextDouble() * 100.0;
         double y = 30.0 + rn.nextDouble() * 200.0;
         double tw = Math.pow(Math.max(0.0, Math.sin(ph * 2.0 + rn.nextDouble() * 6.28)), 4.0);
         Capes.star(g, x, y, 1.2 + rn.nextDouble(), neon, tw);
      }
   }

   static void emberTree(Graphics2D g, BufferedImage img, double ph, int k, int n) {
      double t = (double)k / n;
      Capes.field(img, (u, v, xx, yx) -> {
         int c = Capes.ramp(v, 1182228, 1969682, 2757136, 1444360);
         c = Capes.add(c, 16734746, Math.exp(-Math.hypot(xx - 80, yx - 80) / 60.0) * 0.3);
         double ground = 0.86 + 0.02 * Math.sin(u * 6.0);
         if (v > ground) {
            c = Capes.mix(1706502, 656387, (v - ground) * 5.0);
         }

         return c;
      });
      Random rn = new Random(151L);
      g.setColor(new Color(656646));
      List<double[]> tips = new ArrayList<>();
      branch(g, rn, 80.0, 225.28, -Math.PI / 2, 40.0, 11.0, 0, tips);

      for (int i = 0; i < tips.size(); i++) {
         double[] tp = tips.get(i);
         double a = 0.6 + 0.4 * Math.sin(ph + i * 0.7);
         Capes.addDot(img, tp[0], tp[1], 9.0, 16738842, 0.5 * a);
         Capes.dot(g, tp[0], tp[1], 3.2, i % 3 == 0 ? 16769146 : 16747050, 0.9 * a);
      }

      for (int i = 0; i < 46; i++) {
         double[] tp = tips.get(rn.nextInt(tips.size()));
         double off = rn.nextDouble();
         double sp = 1 + rn.nextInt(2);
         double life = (off + t * sp) % 1.0;
         double x = tp[0] + Math.sin(life * 6.0 + i) * 8.0 + life * 10.0 * (rn.nextBoolean() ? 1 : -1);
         double y = tp[1] + life * (230.4 - tp[1]);
         double al = Math.sin(Math.PI * life);
         Capes.dot(g, x, y, 2.2, 16752704, 0.9 * al);
         Capes.dot(g, x, y, 0.9, 16777184, al);
      }

      Capes.addDot(img, 80.0, 225.28, 40.0, 16734746, 0.3 + 0.1 * Math.sin(ph));
   }

   private static void branch(Graphics2D g, Random rn, double x, double y, double ang, double len, double w, int depth, List<double[]> tips) {
      double x1 = x + Math.cos(ang) * len;
      double y1 = y + Math.sin(ang) * len;
      g.setStroke(new BasicStroke((float)w, 1, 1));
      Path2D p = new Double();
      p.moveTo(x, y);
      p.quadTo((x + x1) / 2.0 + (rn.nextDouble() - 0.5) * len * 0.3, (y + y1) / 2.0, x1, y1);
      g.setColor(new Color(656646));
      g.draw(p);
      if (depth < 3) {
         g.setColor(Capes.col(16738842, 0.25));
         g.setStroke(new BasicStroke((float)Math.max(0.6, w * 0.2)));
         g.draw(AffineTransform.getTranslateInstance(w * 0.25, 0.0).createTransformedShape(p));
      }

      if (depth < 5 && !(len < 6.0)) {
         int kids = depth == 0 ? 3 : 2;

         for (int i = 0; i < kids; i++) {
            double da = (i - (kids - 1) / 2.0) * (0.55 + rn.nextDouble() * 0.3) + (rn.nextDouble() - 0.5) * 0.3;
            branch(g, rn, x1, y1, ang + da, len * (0.68 + rn.nextDouble() * 0.12), w * 0.66, depth + 1, tips);
         }

         if (depth >= 3) {
            tips.add(new double[]{x1, y1});
         }
      } else {
         tips.add(new double[]{x1, y1});
      }
   }
}
