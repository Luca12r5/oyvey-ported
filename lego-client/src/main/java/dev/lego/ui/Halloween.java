package dev.lego.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Path2D.Double;
import java.awt.geom.Point2D.Float;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public final class Halloween {
   public static final int ORANGE = -38118;
   public static final int PUMPKIN = -1552882;
   public static final int PURPLE = -7655482;
   public static final int DARK_PURPLE = -11920789;
   public static final int TOXIC = -12916643;
   public static final int BLOOD = -3924450;
   public static final int BONE = -1516344;
   public static final int[] LANTERN_COLS = new int[]{-38118, -7655482, -12916643, -50354, -14006};
   private static final int N = 200;
   private static final float[] fx = new float[200];
   private static final float[] fy = new float[200];
   private static final float[] fz = new float[200];
   private static final float[] fs = new float[200];
   private static boolean seeded;

   private Halloween() {
   }

   public static boolean on() {
      return UiSettings.halloween;
   }

   public static String countdown() {
      LocalDate d = LocalDate.now();
      if (d.getMonthValue() == 10 && d.getDayOfMonth() == 31) {
         return "Happy Halloween!";
      } else {
         int y = d.getYear();
         LocalDate hw = LocalDate.of(d.getMonthValue() > 10 ? y + 1 : y, 10, 31);
         long n = ChronoUnit.DAYS.between(d, hw);
         return n == 1L ? "Morgen ist Halloween!" : "Noch " + n + " Tage bis Halloween";
      }
   }

   private static void seed() {
      Random r = new Random(13L);

      for (int i = 0; i < 200; i++) {
         fx[i] = r.nextFloat();
         fy[i] = r.nextFloat();
         fz[i] = 0.25F + r.nextFloat() * 0.75F;
         fs[i] = r.nextFloat() * 100.0F;
      }

      seeded = true;
   }

   public static void backdrop(int W, int H, float a) {
      if (on() && !(a <= 0.01F)) {
         Gx.pushAlpha(a);
         Gx.gradientV(0, 0, W, H, -1709701080, 2047477272);
         float t = (float)(System.currentTimeMillis() % 1000000L) / 1000.0F;
         int mx = Math.round(W * 0.82F);
         int my = Math.round(H * 0.12F);
         int mr = Math.round(Math.min(W, H) * 0.06F);
         Gx.glow(mx, my, mr * 4, Gx.withAlpha(-991056, 0.15F));
         Gx.glow(mx, my, mr * 2, Gx.withAlpha(-462640, 0.3F));
         Gx.Img moon = Gx.painted("hw_moon", 64, 64, Halloween::paintMoon);
         if (moon != null) {
            Gx.image(moon, mx - mr, my - mr, mr * 2, mr * 2, -1);
         }

         fog(W, H);
         embers(W, H, 1.0F);
         bats(W, H, t);
         Gx.popAlpha();
      }
   }

   public static void embers(int W, int H, float a) {
      if (on()) {
         if (!seeded) {
            seed();
         }

         float dt = Math.min(0.05F, Gx.dt);
         float t = (float)(System.currentTimeMillis() % 1000000L) / 1000.0F;
         Gx.Img dot = Gx.painted("hw_ember", 32, 32, Halloween::paintEmber);
         if (dot != null) {
            int S = Math.max(1, Math.min(W, H) / 540);

            for (int i = 0; i < 200; i++) {
               float z = fz[i];
               fy[i] = fy[i] - dt * (0.02F + 0.04F * z);
               fx[i] = fx[i] + dt * (0.015F * (float)Math.sin(t * 0.5 + fs[i]) * z);
               if (fy[i] < -0.03F) {
                  fy[i] = 1.03F;
                  fx[i] = (fx[i] * 7.31F + 0.37F) % 1.0F;
               }

               if (fx[i] > 1.02F) {
                  fx[i]--;
               }

               int d = Math.max(2, Math.round((1.0F + 2.8F * z * z) * S));
               int x = Math.round(fx[i] * W);
               int y = Math.round(fy[i] * H);
               float al = (0.25F + 0.5F * z) * a;
               int col = z > 0.7F ? -38118 : (z > 0.4F ? -1531840 : -7655482);
               Gx.image(dot, x - d, y - d, d * 2, d * 2, Gx.withAlpha(col, al));
            }
         }
      }
   }

   private static void fog(int W, int H) {
      Gx.Img d = Gx.painted("hw_fog", 1024, 160, Halloween::paintFog);
      if (d != null) {
         int h = Math.round(H * 0.18F);
         float t = (float)(System.currentTimeMillis() % 1000000L) / 1000.0F;
         float sway = (float)Math.sin(t * 0.1) * W * 0.02F;
         Gx.image(d, Math.round(-W * 0.05F + sway), H - h, Math.round(W * 1.1F), h, Gx.withAlpha(-1, 0.7F));
      }
   }

   private static void bats(int W, int H, float t) {
      Gx.Img bat = Gx.painted("hw_bat", 64, 32, Halloween::paintBat);
      if (bat != null) {
         for (int i = 0; i < 6; i++) {
            float phase = i * 1.7F;
            float bx = W * (0.1F + 0.8F * ((t * 0.03F + i * 0.17F) % 1.0F));
            float by = H * (0.08F + 0.15F * (float)Math.sin(t * 0.8 + phase));
            float sz = Math.min(W, H) * (0.02F + 0.01F * (float)Math.sin(phase));
            float wing = 0.7F + 0.3F * (float)Math.sin(t * 6.0F + phase);
            int bw = Math.round(sz * 2.0F * wing);
            int bh = Math.round(sz);
            Gx.image(bat, Math.round(bx - bw / 2.0F), Math.round(by - bh / 2.0F), bw, bh, Gx.withAlpha(-15070678, 0.6F));
         }
      }
   }

   public static void cobweb(int x, int y, int w, float P) {
      if (on()) {
         Gx.Img web = Gx.painted("hw_web", 128, 128, Halloween::paintCobweb);
         if (web != null) {
            int s = Math.round(P * 22.0F);
            Gx.image(web, x - Math.round(s * 0.1F), y - Math.round(s * 0.1F), s, s, Gx.withAlpha(-1, 0.6F));
         }
      }
   }

   public static void lanterns(int x0, int y0, int x1, float P, float sag, int seed) {
      if (on()) {
         float t = (float)(System.currentTimeMillis() % 1000000L) / 1000.0F;
         int n = Math.max(3, Math.round((x1 - x0) / (P * 40.0F)));
         Gx.Img lantern = Gx.painted("hw_lantern", 32, 48, Halloween::paintLantern);
         int wire = Math.max(1, Math.round(P * 0.6F));
         int seg = n * 4;

         for (int i = 0; i < seg; i++) {
            float u0 = (float)i / seg;
            float u1 = (float)(i + 1) / seg;
            float sx0 = x0 + (x1 - x0) * u0;
            float sx1 = x0 + (x1 - x0) * u1;
            float k0 = (float)Math.sin(u0 * n * Math.PI);
            float k1 = (float)Math.sin(u1 * n * Math.PI);
            float yy0 = y0 + Math.abs(k0) * sag;
            float yy1 = y0 + Math.abs(k1) * sag;
            int steps = Math.max(1, Math.round(sx1 - sx0));
            int st = Math.max(2, wire * 5);

            for (int s = 0; s < steps; s += st) {
               float f = (s + st * 0.5F) / steps;
               Gx.fill(Math.round(sx0 + s), Math.round(yy0 + (yy1 - yy0) * Math.min(1.0F, f)), Math.min(st, steps - s), wire, -14019264);
            }
         }

         for (int i = 0; i < n; i++) {
            float u = (i + 0.5F) / n;
            int bx = Math.round(x0 + (x1 - x0) * u);
            int by = Math.round(y0 + sag);
            int col = LANTERN_COLS[(i + seed) % LANTERN_COLS.length];
            float tw = 0.5F + 0.5F * (float)Math.sin(t * (1.8 + i % 3 * 0.5) + i * 2.1 + seed);
            int gr = Math.round(P * (8.0F + 5.0F * tw));
            Gx.glow(bx, by + Math.round(P * 3.0F), gr, Gx.withAlpha(col, 0.4F * tw));
            if (lantern != null) {
               int bw = Math.round(P * 5.0F);
               int bh = Math.round(P * 7.5F);
               Gx.image(lantern, bx - bw / 2, by, bw, bh, Gx.mix(col, -1, 0.2F * tw));
            }
         }
      }
   }

   public static void witchHat(float cx, float top, float w, float tilt) {
      if (on()) {
         Gx.Img h = Gx.painted("hw_witch", 128, 128, Halloween::paintWitchHat);
         if (h != null) {
            int s = Math.round(w);
            Gx.image(h, Math.round(cx - s * 0.5F + tilt), Math.round(top - s * 0.78F), s, s, -1);
         }
      }
   }

   public static void pumpkin(float cx, float cy, float r) {
      Gx.Img p = Gx.painted("hw_pumpkin", 64, 64, Halloween::paintPumpkin);
      if (p != null) {
         int d = Math.round(r * 2.0F);
         Gx.image(p, Math.round(cx - r), Math.round(cy - r), d, d, -1);
      }
   }

   public static void tombstone(int x, int y, int w, int h, int body, int detail, float lift, float P) {
      int r = Math.round(P * 7.0F);
      int yy = Math.round(y - lift);
      Gx.shadow(x, yy, w, h, r, Math.round(P * (5.0F + lift * 0.6F)), 0.55F);
      int tr = Math.min(r, Math.round(w * 0.45F));
      Gx.rectV(x, yy, w, h, tr, Gx.mix(body, -1, 0.08F), Gx.mix(body, -16777216, 0.3F));
      int rb = Math.max(2, Math.round(w * 0.06F));
      Gx.fill(x + w / 2 - rb / 2, yy + Math.round(h * 0.15F), rb, Math.round(h * 0.55F), detail);
      Gx.fill(x + Math.round(w * 0.25F), yy + Math.round(h * 0.3F), Math.round(w * 0.5F), rb, detail);
      Gx.outline(x, yy, w, h, tr, Math.max(1, Math.round(P * 0.6F)), 587202559);
   }

   public static void spookyStripes(int x, int y, int w, int h, float shift) {
      int W = Math.max(8, w);
      int H = Math.max(4, h);
      Gx.Img c = Gx.painted("hw_stripe" + W + "x" + H, W * 2, H * 2, g -> paintSpookyStripes(g, W * 2, H * 2));
      if (c != null) {
         Gx.image(c, x, y, w, h, -1);
      }
   }

   private static void paintEmber(Graphics2D g) {
      g.setPaint(
         new RadialGradientPaint(
            new Float(16.0F, 16.0F),
            16.0F,
            new float[]{0.0F, 0.3F, 1.0F},
            new Color[]{new Color(255, 200, 80, 255), new Color(255, 120, 40, 180), new Color(200, 60, 20, 0)}
         )
      );
      g.fill(new java.awt.geom.Ellipse2D.Float(0.0F, 0.0F, 32.0F, 32.0F));
   }

   private static void paintMoon(Graphics2D g) {
      g.setPaint(
         new RadialGradientPaint(
            new Float(32.0F, 32.0F),
            30.0F,
            new float[]{0.0F, 0.5F, 0.85F, 1.0F},
            new Color[]{new Color(255, 248, 220), new Color(244, 232, 192), new Color(200, 180, 140, 180), new Color(160, 140, 100, 0)}
         )
      );
      g.fill(new java.awt.geom.Ellipse2D.Float(2.0F, 2.0F, 60.0F, 60.0F));
      g.setColor(new Color(200, 185, 145, 50));
      g.fill(new java.awt.geom.Ellipse2D.Float(18.0F, 14.0F, 8.0F, 8.0F));
      g.fill(new java.awt.geom.Ellipse2D.Float(35.0F, 24.0F, 12.0F, 10.0F));
      g.fill(new java.awt.geom.Ellipse2D.Float(22.0F, 36.0F, 6.0F, 6.0F));
   }

   private static void paintFog(Graphics2D g) {
      for (int layer = 0; layer < 3; layer++) {
         Path2D p = new Double();
         p.moveTo(0.0, 160.0);
         double base = layer == 0 ? 50.0 : (layer == 1 ? 80 : 110);

         for (int x = 0; x <= 1024; x += 8) {
            double y = base + Math.sin(x / (120.0 + layer * 40) + layer * 1.5) * 18.0 + Math.sin(x / 50.0 + layer * 3) * 8.0;
            p.lineTo(x, y);
         }

         p.lineTo(1024.0, 160.0);
         p.closePath();
         int a = layer == 0 ? 40 : (layer == 1 ? 70 : 100);
         g.setPaint(new GradientPaint(0.0F, (float)base - 10.0F, new Color(60, 20, 80, a / 2), 0.0F, 160.0F, new Color(30, 10, 50, a)));
         g.fill(p);
      }
   }

   private static void paintBat(Graphics2D g) {
      g.setColor(new Color(20, 8, 30));
      g.fill(new java.awt.geom.Ellipse2D.Float(26.0F, 10.0F, 12.0F, 14.0F));
      GeneralPath lw = new GeneralPath();
      lw.moveTo(28.0F, 14.0F);
      lw.curveTo(16.0F, 6.0F, 4.0F, 8.0F, 0.0F, 4.0F);
      lw.curveTo(6.0F, 16.0F, 12.0F, 20.0F, 28.0F, 18.0F);
      lw.closePath();
      g.fill(lw);
      GeneralPath rw = new GeneralPath();
      rw.moveTo(36.0F, 14.0F);
      rw.curveTo(48.0F, 6.0F, 60.0F, 8.0F, 64.0F, 4.0F);
      rw.curveTo(58.0F, 16.0F, 52.0F, 20.0F, 36.0F, 18.0F);
      rw.closePath();
      g.fill(rw);
      g.setColor(new Color(255, 100, 30));
      g.fill(new java.awt.geom.Ellipse2D.Float(28.0F, 13.0F, 4.0F, 4.0F));
      g.fill(new java.awt.geom.Ellipse2D.Float(34.0F, 13.0F, 4.0F, 4.0F));
      g.setColor(new Color(20, 8, 30));
      g.fill(new Polygon(new int[]{28, 26, 30}, new int[]{11, 4, 8}, 3));
      g.fill(new Polygon(new int[]{36, 34, 38}, new int[]{11, 4, 8}, 3));
   }

   private static void paintCobweb(Graphics2D g) {
      g.setColor(new Color(220, 220, 230, 160));
      g.setStroke(new BasicStroke(1.2F, 1, 1));
      int rays = 8;
      double[] angles = new double[rays];

      for (int i = 0; i < rays; i++) {
         angles[i] = (Math.PI / 2) * i / (rays - 1);
         int ex = (int)(Math.cos(angles[i]) * 120.0);
         int ey = (int)(Math.sin(angles[i]) * 120.0);
         g.drawLine(0, 0, ex, ey);
      }

      for (int ring = 1; ring <= 5; ring++) {
         float r = ring * 22.0F;
         GeneralPath path = new GeneralPath();
         boolean first = true;

         for (int i = 0; i < rays; i++) {
            float dr = r + (float)Math.sin(i * 2.3 + ring) * 4.0F;
            int px = (int)(Math.cos(angles[i]) * dr);
            int py = (int)(Math.sin(angles[i]) * dr);
            if (first) {
               path.moveTo(px, py);
               first = false;
            } else {
               path.lineTo(px, py);
            }
         }

         g.setColor(new Color(220, 220, 230, 100 + ring * 10));
         g.draw(path);
      }
   }

   private static void paintWitchHat(Graphics2D g) {
      g.setPaint(new GradientPaint(20.0F, 100.0F, new Color(30, 10, 50), 110.0F, 100.0F, new Color(50, 20, 70)));
      g.fill(new java.awt.geom.Ellipse2D.Float(6.0F, 88.0F, 116.0F, 30.0F));
      GeneralPath cone = new GeneralPath();
      cone.moveTo(20.0F, 98.0F);
      cone.curveTo(30.0F, 60.0F, 50.0F, 20.0F, 72.0F, 8.0F);
      cone.curveTo(90.0F, 20.0F, 98.0F, 50.0F, 108.0F, 98.0F);
      cone.closePath();
      g.setPaint(new GradientPaint(40.0F, 10.0F, new Color(60, 20, 90), 90.0F, 98.0F, new Color(30, 8, 50)));
      g.fill(cone);
      g.setColor(new Color(200, 160, 50));
      g.fillRect(48, 80, 32, 12);
      g.setColor(new Color(240, 200, 80));
      g.fillRect(56, 78, 16, 16);
      g.setColor(new Color(60, 20, 90));
      g.fillRect(60, 82, 8, 8);
      GeneralPath tip = new GeneralPath();
      tip.moveTo(72.0F, 8.0F);
      tip.curveTo(78.0F, 2.0F, 95.0F, 10.0F, 98.0F, 28.0F);
      tip.curveTo(88.0F, 16.0F, 80.0F, 12.0F, 72.0F, 8.0F);
      g.setPaint(new GradientPaint(72.0F, 8.0F, new Color(60, 20, 90), 95.0F, 30.0F, new Color(40, 12, 65)));
      g.fill(tip);
   }

   private static void paintPumpkin(Graphics2D g) {
      g.setPaint(
         new RadialGradientPaint(
            new Float(32.0F, 34.0F), 28.0F, new float[]{0.0F, 0.5F, 1.0F}, new Color[]{new Color(255, 140, 20), new Color(230, 100, 10), new Color(180, 60, 0)}
         )
      );
      g.fill(new java.awt.geom.Ellipse2D.Float(6.0F, 14.0F, 24.0F, 36.0F));
      g.fill(new java.awt.geom.Ellipse2D.Float(16.0F, 10.0F, 30.0F, 44.0F));
      g.fill(new java.awt.geom.Ellipse2D.Float(34.0F, 14.0F, 24.0F, 36.0F));
      g.setPaint(
         new RadialGradientPaint(
            new Float(32.0F, 32.0F), 24.0F, new float[]{0.0F, 0.7F, 1.0F}, new Color[]{new Color(255, 160, 30), new Color(240, 110, 15), new Color(200, 70, 5)}
         )
      );
      g.fill(new java.awt.geom.Ellipse2D.Float(12.0F, 12.0F, 40.0F, 40.0F));
      g.setColor(new Color(20, 10, 0));
      g.fill(new Polygon(new int[]{22, 20, 28}, new int[]{24, 32, 30}, 3));
      g.fill(new Polygon(new int[]{42, 36, 44}, new int[]{24, 32, 30}, 3));
      GeneralPath mouth = new GeneralPath();
      mouth.moveTo(20.0F, 38.0F);
      mouth.lineTo(24.0F, 36.0F);
      mouth.lineTo(28.0F, 40.0F);
      mouth.lineTo(32.0F, 36.0F);
      mouth.lineTo(36.0F, 40.0F);
      mouth.lineTo(40.0F, 36.0F);
      mouth.lineTo(44.0F, 38.0F);
      mouth.lineTo(42.0F, 44.0F);
      mouth.lineTo(22.0F, 44.0F);
      mouth.closePath();
      g.fill(mouth);
      g.setPaint(
         new RadialGradientPaint(
            new Float(32.0F, 34.0F),
            18.0F,
            new float[]{0.0F, 0.5F, 1.0F},
            new Color[]{new Color(255, 200, 50, 60), new Color(255, 150, 20, 30), new Color(255, 100, 0, 0)}
         )
      );
      g.fill(new java.awt.geom.Ellipse2D.Float(14.0F, 20.0F, 36.0F, 30.0F));
      g.setColor(new Color(80, 120, 40));
      g.fillRoundRect(30, 6, 6, 10, 3, 3);
   }

   private static void paintLantern(Graphics2D g) {
      g.setColor(new Color(80, 80, 80));
      g.setStroke(new BasicStroke(2.0F));
      g.drawArc(8, 0, 16, 12, 0, 180);
      g.setPaint(
         new RadialGradientPaint(
            new Float(16.0F, 28.0F),
            18.0F,
            new float[]{0.0F, 0.4F, 1.0F},
            new Color[]{new Color(255, 255, 255), new Color(240, 240, 240, 230), new Color(180, 180, 180, 200)}
         )
      );
      g.fill(new java.awt.geom.Ellipse2D.Float(4.0F, 12.0F, 24.0F, 34.0F));
      g.setPaint(
         new RadialGradientPaint(
            new Float(16.0F, 26.0F), 10.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(255, 255, 255, 180), new Color(255, 255, 255, 0)}
         )
      );
      g.fill(new java.awt.geom.Ellipse2D.Float(6.0F, 14.0F, 20.0F, 30.0F));
   }

   private static void paintSpookyStripes(Graphics2D g, int w, int h) {
      RoundRectangle2D pill = new java.awt.geom.RoundRectangle2D.Float(0.0F, 0.0F, w, h, h, h);
      g.setClip(pill);
      g.setColor(new Color(139, 47, 198));
      g.fillRect(0, 0, w, h);
      g.setColor(new Color(255, 107, 26));
      int s = Math.max(6, h / 2);

      for (int x = -h; x < w + h; x += s * 2) {
         GeneralPath p = new GeneralPath();
         p.moveTo(x, h);
         p.lineTo(x + s, h);
         p.lineTo(x + s + h, 0.0F);
         p.lineTo(x + h, 0.0F);
         p.closePath();
         g.fill(p);
      }

      g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(255, 255, 255, 70), 0.0F, h, new Color(0, 0, 0, 80)));
      g.fillRect(0, 0, w, h);
   }
}
