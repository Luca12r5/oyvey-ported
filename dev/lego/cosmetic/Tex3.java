package dev.lego.cosmetic;

import dev.lego.ui.Art;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;
import java.util.Random;

final class Tex3 {
   private Tex3() {
   }

   static CTex.T paint(String name) {
      switch (name) {
         case "plastic":
            return Tex2.pix(64, 64, (u, v, x, y) -> 0xFF000000 | Capes.mix(16777215, 13159636, v * 0.8 + Math.abs(u - 0.3) * 0.3));
         case "satin":
            return Tex2.pix(64, 64, (u, v, x, y) -> 0xFF000000 | Capes.shade(15921906, 0.86 + 0.14 * Math.sin((u * 3.0 + v) * Math.PI * 2.0)));
         case "cloth":
            return Tex2.pix(64, 64, (u, v, x, y) -> 0xFF000000 | Capes.shade(16053492, 0.93 + 0.07 * Capes.hash(x, y, 3) - ((x + y) % 4 == 0 ? 0.04 : 0.0)));
         case "knit":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double fx = x % 8 / 8.0;
               double fy = y % 8 / 8.0;
               double vv = Math.abs(fx - 0.5) * 2.0;
               double stitch = 1.0 - Math.abs(fy - vv * 0.5 - 0.25) * 2.2;
               return 0xFF000000 | Capes.shade(16053492, 0.72 + 0.3 * Math.max(0.0, stitch) + 0.05 * Capes.hash(x, y, 2));
            });
         case "leather":
            return Tex2.pix(
               64, 64, (u, v, x, y) -> 0xFF000000 | Capes.shade(Capes.mix(9067058, 5911576, v), 0.85 + 0.25 * Capes.fbm(u * 10.0, v * 10.0, 0, 0, 4, 3))
            );
         case "leather_dark":
            return Tex2.pix(
               64, 64, (u, v, x, y) -> 0xFF000000 | Capes.shade(Capes.mix(3810840, 1970698, v), 0.85 + 0.25 * Capes.fbm(u * 10.0, v * 10.0, 0, 0, 4, 3))
            );
         case "steel":
            return Tex2.pix(
               64,
               64,
               (u, v, x, y) -> 0xFF000000
                  | Capes.shade(Capes.mix(15265010, 8029332, v * 0.7 + 0.3 * Math.abs(Math.sin(u * 6.0))), 0.95 + 0.08 * Capes.hash(x, y / 3, 5))
            );
         case "iron":
            return Tex2.pix(
               64, 64, (u, v, x, y) -> 0xFF000000 | Capes.shade(Capes.mix(11581120, 4870234, v), 0.85 + 0.25 * Capes.fbm(u * 8.0, v * 8.0, 0, 0, 3, 9))
            );
         case "wood":
            return Tex2.pix(64, 64, (u, v, x, y) -> {
               double r = Math.sin((u * 3.0 + Capes.fbm(u * 2.0, v * 8.0, 0, 0, 3, 4) * 2.0) * Math.PI * 4.0);
               return 0xFF000000 | Capes.mix(11563066, 6961688, 0.5 + 0.5 * r);
            });
         case "ivory":
            return Tex2.pix(32, 64, (u, v, x, y) -> 0xFF000000 | Capes.shade(Capes.mix(16774880, 13153418, v), 0.92 + 0.1 * Math.sin(v * 40.0)));
         case "gloss":
            return Tex2.img(
               64,
               64,
               g -> {
                  g.setPaint(
                     new RadialGradientPaint(
                        22.0F, 18.0F, 50.0F, new float[]{0.0F, 0.2F, 1.0F}, new Color[]{Color.WHITE, new Color(16053492), new Color(11053224)}
                     )
                  );
                  g.fillRect(0, 0, 64, 64);
               }
            );
         case "fox_ear":
            return Tex2.img(64, 64, g -> {
               Path2D o = new Double();
               o.moveTo(2.0, 62.0);
               o.quadTo(10.0, 22.0, 32.0, 2.0);
               o.quadTo(54.0, 22.0, 62.0, 62.0);
               o.closePath();
               g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(16747050), 0.0F, 64.0F, new Color(14178832)));
               g.fill(o);
               g.setClip(o);
               g.setColor(new Color(2759186));
               g.fill(new java.awt.geom.Ellipse2D.Double(10.0, -8.0, 44.0, 28.0));
               g.setClip(null);
               Path2D in = new Double();
               in.moveTo(14.0, 62.0);
               in.quadTo(20.0, 32.0, 32.0, 18.0);
               in.quadTo(44.0, 32.0, 50.0, 62.0);
               in.closePath();
               g.setPaint(new GradientPaint(0.0F, 18.0F, new Color(16774894), 0.0F, 62.0F, new Color(16767168)));
               g.fill(in);
            });
         case "cap_segments":
            return Tex2.pix(128, 64, (u, v, x, y) -> {
               int[] c = new int[]{14876683, 16764163, 27831, 41018};
               int seg = (int)(u * 8.0) % 4;
               return 0xFF000000 | Capes.shade(c[seg], 0.85 + 0.2 * (1.0 - v) - (Math.abs(u * 8.0 % 1.0 - 0.5) > 0.47 ? 0.25 : 0.0));
            });
         case "party_cone":
            return Tex2.pix(64, 128, (u, v, x, y) -> {
               boolean st = ((u * 4.0 + v * 3.0) % 1.0 + 1.0) % 1.0 < 0.5;
               int c = st ? 16726666 : 3852543;
               if (Capes.hash(x / 6, y / 6, 7) > 0.9) {
                  c = 16769610;
               }

               return 0xFF000000 | Capes.shade(c, 0.85 + 0.15 * v);
            });
         case "wizard_cone":
            return Tex2.img(64, 128, g -> {
               g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(3812010), 0.0F, 128.0F, new Color(1444954)));
               g.fillRect(0, 0, 64, 128);
               Random r = new Random(3L);

               for (int i = 0; i < 14; i++) {
                  star(g, r.nextDouble() * 64.0, r.nextDouble() * 128.0, 3.0 + r.nextDouble() * 4.0, 16769658);
               }

               g.setColor(Capes.col(16769658, 0.8));

               for (int i = 0; i < 30; i++) {
                  g.fill(new java.awt.geom.Ellipse2D.Double(r.nextDouble() * 64.0, r.nextDouble() * 128.0, 1.5, 1.5));
               }
            });
         case "mushroom_cap":
            return Tex2.img(128, 64, g -> {
               g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(16726570), 0.0F, 64.0F, new Color(11536906)));
               g.fillRect(0, 0, 128, 64);
               Random r = new Random(5L);

               for (int i = 0; i < 14; i++) {
                  double x = i % 7 * 18.3 + r.nextDouble() * 6.0;
                  double y = 8 + i / 7 * 26 + r.nextDouble() * 8.0;
                  double s = 7.0 + r.nextDouble() * 6.0;
                  g.setColor(new Color(16775408));
                  g.fill(new java.awt.geom.Ellipse2D.Double(x, y, s, s * 0.8));
               }
            });
         case "gill":
            return Tex2.pix(64, 16, (u, v, x, y) -> 0xFF000000 | Capes.shade(16049872, 0.8 + 0.2 * Math.abs(Math.sin(u * 60.0))));
         case "vine":
            return Tex2.img(128, 32, g -> {
               g.setColor(new Color(3832362));
               g.fillRect(0, 12, 128, 8);
               Random r = new Random(2L);

               for (int i = 0; i < 18; i++) {
                  double x = i * 7.3;
                  double y = 16.0;
                  AffineTransform o = g.getTransform();
                  g.translate(x, y);
                  g.rotate((i % 2 == 0 ? -1 : 1) * (0.6 + r.nextDouble() * 0.4));
                  g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(8050762), 14.0F, 0.0F, new Color(2787882)));
                  g.fill(new java.awt.geom.Ellipse2D.Double(0.0, -4.0, 15.0, 8.0));
                  g.setTransform(o);
               }
            });
         case "flower_pink":
            return flower(16747192, 16769262, 16765498);
         case "flower_white":
            return flower(15790335, 16777215, 16762938);
         case "flower_yellow":
            return flower(16765498, 16774304, 14704682);
         case "flower_blue":
            return flower(6994175, 14217471, 16774304);
         case "heart_glasses":
            return Tex2.img(256, 96, g -> {
               for (int s = 0; s < 2; s++) {
                  double cx = s == 0 ? 64.0 : 192.0;
                  Path2D h = CapeArt2.heartPath(cx, 50.0, 104.0);
                  g.setColor(new Color(14684511));
                  g.fill(h);
                  Path2D in = CapeArt2.heartPath(cx, 50.0, 84.0);
                  g.setPaint(new GradientPaint(0.0F, 10.0F, new Color(16743088), 0.0F, 90.0F, new Color(12587082)));
                  g.fill(in);
                  g.setColor(Capes.col(16777215, 0.7));
                  g.fill(new java.awt.geom.Ellipse2D.Double(cx - 30.0, 24.0, 18.0, 10.0));
               }

               g.setColor(new Color(14684511));
               g.fillRect(110, 30, 36, 8);
            });
         case "star_glasses":
            return Tex2.img(256, 96, g -> {
               for (int s = 0; s < 2; s++) {
                  double cx = s == 0 ? 64.0 : 192.0;
                  Path2D st = starPath(cx, 50.0, 48.0, 22.0);
                  g.setColor(new Color(15245328));
                  g.fill(st);
                  g.setPaint(new GradientPaint(0.0F, 10.0F, new Color(16774304), 0.0F, 90.0F, new Color(16756768)));
                  g.fill(starPath(cx, 51.0, 40.0, 18.0));
                  g.setColor(Capes.col(8010240, 0.5));
                  g.fill(new java.awt.geom.Ellipse2D.Double(cx - 16.0, 36.0, 32.0, 28.0));
               }

               g.setColor(new Color(15245328));
               g.fillRect(104, 40, 48, 7);
            });
         case "monocle_glass":
            return Tex2.img(
               64,
               64,
               g -> {
                  g.setPaint(
                     new RadialGradientPaint(24.0F, 22.0F, 40.0F, new float[]{0.0F, 1.0F}, new Color[]{Capes.col(16777215, 0.55), Capes.col(12116223, 0.25)})
                  );
                  g.fill(new java.awt.geom.Ellipse2D.Double(2.0, 2.0, 60.0, 60.0));
                  g.setColor(Capes.col(16777215, 0.8));
                  g.setStroke(new BasicStroke(3.0F, 1, 1));
                  g.draw(new java.awt.geom.Arc2D.Double(12.0, 12.0, 40.0, 40.0, 100.0, 70.0, 0));
               }
            );
         case "mustache":
            return Tex2.img(128, 48, g -> {
               Path2D m = new Double();
               m.moveTo(64.0, 14.0);
               m.curveTo(80.0, 2.0, 96.0, 8.0, 104.0, 22.0);
               m.curveTo(110.0, 30.0, 122.0, 30.0, 126.0, 18.0);
               m.curveTo(126.0, 38.0, 106.0, 46.0, 90.0, 38.0);
               m.curveTo(80.0, 34.0, 70.0, 30.0, 64.0, 26.0);
               m.curveTo(58.0, 30.0, 48.0, 34.0, 38.0, 38.0);
               m.curveTo(22.0, 46.0, 2.0, 38.0, 2.0, 18.0);
               m.curveTo(6.0, 30.0, 18.0, 30.0, 24.0, 22.0);
               m.curveTo(32.0, 8.0, 48.0, 2.0, 64.0, 14.0);
               g.setPaint(new GradientPaint(0.0F, 4.0F, new Color(6963752), 0.0F, 44.0F, new Color(2758154)));
               g.fill(m);
               g.setClip(m);
               g.setColor(Capes.col(11567192, 0.35));
               g.setStroke(new BasicStroke(1.2F));

               for (int i = 0; i < 26; i++) {
                  double x = 6.0 + i * 4.6;
                  g.draw(new java.awt.geom.QuadCurve2D.Double(64.0 + (x - 64.0) * 0.3, 14.0, x, 20.0, x + (x < 64.0 ? -6 : 6), 40.0));
               }
            });
         case "visor":
            return Tex2.img(
               256,
               64,
               g -> {
                  g.setPaint(
                     new LinearGradientPaint(
                        0.0F,
                        0.0F,
                        0.0F,
                        64.0F,
                        new float[]{0.0F, 0.5F, 1.0F},
                        new Color[]{Capes.col(1706554, 0.92), Capes.col(2759274, 0.85), Capes.col(656928, 0.95)}
                     )
                  );
                  g.fill(new java.awt.geom.RoundRectangle2D.Double(0.0, 4.0, 256.0, 56.0, 30.0, 30.0));
                  g.setColor(Capes.col(2811647, 0.25));

                  for (int y = 8; y < 60; y += 4) {
                     g.fillRect(10, y, 236, 1);
                  }

                  g.setPaint(new GradientPaint(0.0F, 4.0F, Capes.col(16777215, 0.35), 0.0F, 26.0F, Capes.col(16777215, 0.0)));
                  g.fill(new java.awt.geom.RoundRectangle2D.Double(8.0, 6.0, 240.0, 20.0, 16.0, 16.0));
               }
            );
         case "mask_masq":
            return Tex2.img(
               256,
               128,
               g -> {
                  Path2D m = new Double();
                  m.moveTo(128.0, 40.0);
                  m.curveTo(150.0, 20.0, 220.0, 10.0, 252.0, 30.0);
                  m.curveTo(250.0, 70.0, 220.0, 110.0, 170.0, 104.0);
                  m.curveTo(150.0, 100.0, 138.0, 86.0, 128.0, 80.0);
                  m.curveTo(118.0, 86.0, 106.0, 100.0, 86.0, 104.0);
                  m.curveTo(36.0, 110.0, 6.0, 70.0, 4.0, 30.0);
                  m.curveTo(36.0, 10.0, 106.0, 20.0, 128.0, 40.0);
                  m.closePath();
                  g.setPaint(
                     new LinearGradientPaint(
                        0.0F, 0.0F, 256.0F, 128.0F, new float[]{0.0F, 0.5F, 1.0F}, new Color[]{new Color(5905034), new Color(9054920), new Color(3803754)}
                     )
                  );
                  g.fill(m);
                  g.setClip(m);
                  g.setColor(Capes.col(16765562, 0.5));
                  g.setStroke(new BasicStroke(2.0F));

                  for (int i = 0; i < 6; i++) {
                     g.draw(new java.awt.geom.QuadCurve2D.Double(0.0, 20 + i * 18, 128.0, 60 + i * 10, 256.0, 20 + i * 18));
                  }

                  g.setClip(null);
                  g.setColor(new Color(16765562));
                  g.setStroke(new BasicStroke(4.0F));
                  g.draw(m);
                  g.setComposite(AlphaComposite.Clear);
                  g.fill(new java.awt.geom.Ellipse2D.Double(56.0, 44.0, 50.0, 30.0));
                  g.fill(new java.awt.geom.Ellipse2D.Double(150.0, 44.0, 50.0, 30.0));
                  g.setComposite(AlphaComposite.SrcOver);
                  g.setColor(new Color(16765562));
                  g.setStroke(new BasicStroke(3.0F));
                  g.draw(new java.awt.geom.Ellipse2D.Double(56.0, 44.0, 50.0, 30.0));
                  g.draw(new java.awt.geom.Ellipse2D.Double(150.0, 44.0, 50.0, 30.0));
                  Random r = new Random(4L);

                  for (int i = 0; i < 14; i++) {
                     g.setColor(Capes.col(16777215, 0.8));
                     g.fill(new java.awt.geom.Ellipse2D.Double(20.0 + r.nextDouble() * 216.0, 20.0 + r.nextDouble() * 80.0, 3.0, 3.0));
                  }
               }
            );
         case "feather_plume":
            return Tex2.feather(16777215, 14721279, 9054920, 16777215, "none", false);
         case "shield_face":
            return Tex2.img(128, 128, g -> {
               g.setPaint(new RadialGradientPaint(50.0F, 44.0F, 80.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(3828448), new Color(928378)}));
               g.fillRect(0, 0, 128, 128);
               g.setColor(new Color(15263984));
               Path2D c = new Double();
               c.moveTo(64.0, 0.0);
               c.lineTo(76.0, 0.0);
               c.lineTo(76.0, 128.0);
               c.lineTo(52.0, 128.0);
               c.lineTo(52.0, 0.0);
               c.closePath();
               g.fill(new java.awt.geom.Rectangle2D.Double(54.0, 0.0, 20.0, 128.0));
               g.fill(new java.awt.geom.Rectangle2D.Double(0.0, 54.0, 128.0, 20.0));
               g.setPaint(new RadialGradientPaint(64.0F, 64.0F, 22.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(16774328), new Color(13145626)}));
               g.fill(new java.awt.geom.Ellipse2D.Double(42.0, 42.0, 44.0, 44.0));
               g.setColor(new Color(9067018));
               g.setStroke(new BasicStroke(2.0F));
               g.draw(new java.awt.geom.Ellipse2D.Double(42.0, 42.0, 44.0, 44.0));
               Art.brick(g, 48.0, 44.0, 32.0, new Color(14876683));
            });
         case "guitar_body":
            return Tex2.img(
               128,
               128,
               g -> {
                  g.setPaint(
                     new RadialGradientPaint(
                        64.0F, 60.0F, 70.0F, new float[]{0.0F, 0.5F, 1.0F}, new Color[]{new Color(16765562), new Color(14704682), new Color(8002058)}
                     )
                  );
                  g.fillRect(0, 0, 128, 128);
                  g.setColor(new Color(1706500));
                  g.fill(new java.awt.geom.Ellipse2D.Double(48.0, 40.0, 32.0, 32.0));
                  g.setColor(new Color(15259824));
                  g.setStroke(new BasicStroke(2.5F));
                  g.draw(new java.awt.geom.Ellipse2D.Double(44.0, 36.0, 40.0, 40.0));
                  g.setColor(new Color(2759178));
                  g.fillRect(44, 92, 40, 8);
               }
            );
         case "strings":
            return Tex2.pix(16, 64, (u, v, x, y) -> x % 4 == 1 ? -1513232 : 16777215);
         case "teddy_face":
            return Tex2.img(64, 64, g -> {
               g.setColor(new Color(11565640));
               g.fillRect(0, 0, 64, 64);
               g.setColor(new Color(15255704));
               g.fill(new java.awt.geom.Ellipse2D.Double(18.0, 30.0, 28.0, 22.0));
               g.setColor(new Color(1707528));
               g.fill(new java.awt.geom.Ellipse2D.Double(16.0, 18.0, 9.0, 10.0));
               g.fill(new java.awt.geom.Ellipse2D.Double(39.0, 18.0, 9.0, 10.0));
               g.fill(new java.awt.geom.Ellipse2D.Double(27.0, 32.0, 10.0, 7.0));
               g.setStroke(new BasicStroke(2.0F, 1, 1));
               g.draw(new java.awt.geom.QuadCurve2D.Double(26.0, 44.0, 32.0, 49.0, 38.0, 44.0));
               g.setColor(Color.WHITE);
               g.fill(new java.awt.geom.Ellipse2D.Double(18.0, 19.0, 3.0, 3.0));
               g.fill(new java.awt.geom.Ellipse2D.Double(41.0, 19.0, 3.0, 3.0));
               g.setColor(Capes.col(16743066, 0.5));
               g.fill(new java.awt.geom.Ellipse2D.Double(8.0, 32.0, 9.0, 6.0));
               g.fill(new java.awt.geom.Ellipse2D.Double(47.0, 32.0, 9.0, 6.0));
            });
         case "fur_brown":
            return Tex2.pix(64, 64, (u, v, x, y) -> 0xFF000000 | Capes.shade(11565640, 0.82 + 0.3 * Capes.fbm(u * 16.0, v * 16.0, 0, 0, 3, 6)));
         case "rocket_body":
            return Tex2.pix(64, 128, (u, v, x, y) -> {
               int c = 16054010;
               if (v > 0.3 && v < 0.36) {
                  c = 14876683;
               }

               if (v > 0.75 && v < 0.8) {
                  c = 14876683;
               }

               double sh = 0.75 + 0.3 * Math.sin(u * Math.PI);
               if (Math.abs(u * 8.0 % 1.0 - 0.5) > 0.48) {
                  sh *= 0.9;
               }

               return 0xFF000000 | Capes.shade(c, sh);
            });
         case "window":
            return Tex2.img(32, 32, g -> {
               g.setColor(new Color(10134192));
               g.fill(new java.awt.geom.Ellipse2D.Double(0.0, 0.0, 32.0, 32.0));
               g.setPaint(new RadialGradientPaint(12.0F, 12.0F, 16.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(12120319), new Color(1727144)}));
               g.fill(new java.awt.geom.Ellipse2D.Double(4.0, 4.0, 24.0, 24.0));
            });
         case "sword_blade":
            return Tex2.img(
               32,
               128,
               g -> {
                  g.setPaint(
                     new LinearGradientPaint(
                        0.0F,
                        0.0F,
                        32.0F,
                        0.0F,
                        new float[]{0.0F, 0.5F, 0.52F, 1.0F},
                        new Color[]{new Color(14679039), new Color(8382719), new Color(2792152), new Color(1727130)}
                     )
                  );
                  g.fillRect(0, 0, 32, 128);
                  g.setColor(Capes.col(16777215, 0.8));
                  g.fillRect(14, 0, 3, 128);
                  Random r = new Random(3L);
                  g.setColor(Capes.col(14745599, 0.9));

                  for (int i = 0; i < 6; i++) {
                     double y = 20 + i * 16;
                     g.draw(new java.awt.geom.Line2D.Double(8.0, y, 12.0 + r.nextDouble() * 8.0, y + 6.0));
                  }
               }
            );
         case "arrow_fletch":
            return Tex2.img(32, 64, g -> {
               Path2D f = new Double();
               f.moveTo(16.0, 0.0);
               f.lineTo(30.0, 20.0);
               f.lineTo(30.0, 62.0);
               f.lineTo(16.0, 48.0);
               f.lineTo(2.0, 62.0);
               f.lineTo(2.0, 20.0);
               f.closePath();
               g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(16777215), 0.0F, 64.0F, new Color(14696538)));
               g.fill(f);
            });
         case "petal":
            return Tex2.img(32, 32, g -> {
               Path2D p = new Double();
               p.moveTo(16.0, 30.0);
               p.curveTo(30.0, 22.0, 28.0, 6.0, 19.0, 2.0);
               p.lineTo(16.0, 7.0);
               p.lineTo(13.0, 2.0);
               p.curveTo(4.0, 6.0, 2.0, 22.0, 16.0, 30.0);
               g.setPaint(new GradientPaint(0.0F, 30.0F, new Color(16748468), 0.0F, 2.0F, new Color(16773366)));
               g.fill(p);
            });
         case "note":
            return Tex2.img(64, 64, g -> {
               g.setColor(Color.WHITE);
               g.fill(new java.awt.geom.Ellipse2D.Double(6.0, 40.0, 20.0, 15.0));
               g.fill(new java.awt.geom.Ellipse2D.Double(36.0, 34.0, 20.0, 15.0));
               g.setStroke(new BasicStroke(4.0F));
               g.drawLine(24, 47, 24, 10);
               g.drawLine(54, 41, 54, 4);
               g.setStroke(new BasicStroke(8.0F));
               g.drawLine(24, 12, 54, 6);
            });
         case "bubble":
            return Tex2.img(
               64,
               64,
               g -> {
                  g.setPaint(
                     new RadialGradientPaint(
                        32.0F,
                        32.0F,
                        30.0F,
                        new float[]{0.0F, 0.75F, 0.95F, 1.0F},
                        new Color[]{Capes.col(12120319, 0.08), Capes.col(12118271, 0.25), Capes.col(16777215, 0.8), Capes.col(16777215, 0.0)}
                     )
                  );
                  g.fill(new java.awt.geom.Ellipse2D.Double(1.0, 1.0, 62.0, 62.0));
                  g.setColor(Capes.col(16777215, 0.9));
                  g.fill(new java.awt.geom.Ellipse2D.Double(16.0, 13.0, 12.0, 8.0));
                  g.setColor(Capes.col(16747232, 0.25));
                  g.setStroke(new BasicStroke(2.0F));
                  g.draw(new java.awt.geom.Arc2D.Double(6.0, 6.0, 52.0, 52.0, 200.0, 80.0, 0));
               }
            );
         case "leaf":
            return Tex2.img(48, 48, g -> {
               Path2D l = new Double();
               l.moveTo(4.0, 44.0);
               l.curveTo(4.0, 14.0, 24.0, 4.0, 44.0, 4.0);
               l.curveTo(44.0, 26.0, 30.0, 44.0, 4.0, 44.0);
               g.setPaint(new GradientPaint(4.0F, 44.0F, new Color(14700570), 44.0F, 4.0F, new Color(16760890)));
               g.fill(l);
               g.setColor(Capes.col(8006154, 0.7));
               g.setStroke(new BasicStroke(1.5F));
               g.draw(new java.awt.geom.QuadCurve2D.Double(4.0, 44.0, 22.0, 24.0, 40.0, 8.0));
            });
         case "rune_circle":
            return Tex2.img(256, 256, g -> {
               g.translate(128, 128);
               int c = 13142783;

               for (int i = 4; i >= 1; i--) {
                  g.setColor(Capes.col(c, 0.1 * (5 - i)));
                  g.setStroke(new BasicStroke(i * 3.0F));
                  g.draw(new java.awt.geom.Ellipse2D.Double(-120.0, -120.0, 240.0, 240.0));
                  g.draw(new java.awt.geom.Ellipse2D.Double(-96.0, -96.0, 192.0, 192.0));
               }

               g.setColor(Capes.col(15785215, 0.95));
               g.setStroke(new BasicStroke(2.5F));
               g.draw(new java.awt.geom.Ellipse2D.Double(-120.0, -120.0, 240.0, 240.0));
               g.draw(new java.awt.geom.Ellipse2D.Double(-96.0, -96.0, 192.0, 192.0));
               g.draw(new java.awt.geom.Ellipse2D.Double(-40.0, -40.0, 80.0, 80.0));
               Random r = new Random(7L);

               for (int i = 0; i < 20; i++) {
                  AffineTransform o = g.getTransform();
                  g.rotate((Math.PI * 2) * i / 20.0);
                  g.translate(0, -108);

                  for (int s = 0; s < 3; s++) {
                     g.drawLine(r.nextInt(3) * 5 - 5, r.nextInt(3) * 5 - 5, r.nextInt(3) * 5 - 5, r.nextInt(3) * 5 - 5);
                  }

                  g.setTransform(o);
               }

               for (int t = 0; t < 2; t++) {
                  Path2D p = new Double();

                  for (int i = 0; i < 3; i++) {
                     double a = t * Math.PI / 3.0 + i * Math.PI * 2.0 / 3.0;
                     if (i == 0) {
                        p.moveTo(Math.cos(a) * 94.0, Math.sin(a) * 94.0);
                     } else {
                        p.lineTo(Math.cos(a) * 94.0, Math.sin(a) * 94.0);
                     }
                  }

                  p.closePath();
                  g.draw(p);
               }
            });
         case "glyph":
            return Tex2.img(32, 32, g -> {
               g.setColor(Capes.col(15255807, 1.0));
               g.setStroke(new BasicStroke(3.0F, 1, 1));
               g.drawLine(8, 6, 8, 26);
               g.drawLine(8, 16, 24, 8);
               g.drawLine(16, 12, 24, 26);
            });
         case "ring_glow":
            return Tex2.pix(256, 16, (u, v, x, y) -> Tex2.argb(16777215, Math.pow(1.0 - Math.abs(v - 0.5) * 2.0, 1.6)));
         case "void_orb":
            return Tex2.img(
               64,
               64,
               g -> {
                  g.setPaint(
                     new RadialGradientPaint(
                        32.0F,
                        32.0F,
                        32.0F,
                        new float[]{0.0F, 0.35F, 0.55F, 1.0F},
                        new Color[]{new Color(655378), new Color(1703984), Capes.col(11553535, 0.9), Capes.col(6953672, 0.0)}
                     )
                  );
                  g.fillRect(0, 0, 64, 64);
               }
            );
         default:
            return null;
      }
   }

   static void star(Graphics2D g, double x, double y, double r, int rgb) {
      g.setColor(new Color(rgb));
      g.fill(starPath(x, y, r, r * 0.45));
   }

   static Path2D starPath(double cx, double cy, double R, double r) {
      Path2D p = new Double();

      for (int i = 0; i < 10; i++) {
         double a = (-Math.PI / 2) + i * Math.PI / 5.0;
         double rr = i % 2 == 0 ? R : r;
         if (i == 0) {
            p.moveTo(cx + Math.cos(a) * rr, cy + Math.sin(a) * rr);
         } else {
            p.lineTo(cx + Math.cos(a) * rr, cy + Math.sin(a) * rr);
         }
      }

      p.closePath();
      return p;
   }

   static CTex.T flower(int petal, int light, int center) {
      return Tex2.img(64, 64, g -> {
         g.translate(32, 32);

         for (int i = 0; i < 6; i++) {
            AffineTransform o = g.getTransform();
            g.rotate((Math.PI * 2) * i / 6.0);
            g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(petal), 0.0F, -28.0F, new Color(light)));
            g.fill(new java.awt.geom.Ellipse2D.Double(-8.0, -30.0, 16.0, 28.0));
            g.setTransform(o);
         }

         g.setPaint(new RadialGradientPaint(-2.0F, -2.0F, 10.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(16774848), new Color(center)}));
         g.fill(new java.awt.geom.Ellipse2D.Double(-8.0, -8.0, 16.0, 16.0));
      });
   }
}
