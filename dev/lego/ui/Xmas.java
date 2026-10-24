package dev.lego.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Path2D.Double;
import java.awt.geom.Point2D.Float;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public final class Xmas {
   public static final int RED = -1890757;
   public static final int RED_DARK = -6418396;
   public static final int GOLD = -736942;
   public static final int GREEN = -14703780;
   public static final int GREEN_DARK = -15835596;
   public static final int ICE = -4201473;
   public static final int SNOW = -722689;
   public static final int NAVY = -16051162;
   public static final int[] BULBS = new int[]{-50354, -14006, -12853110, -11884289, -33835};
   private static final int N = 260;
   private static final float[] fx = new float[260];
   private static final float[] fy = new float[260];
   private static final float[] fz = new float[260];
   private static final float[] fs = new float[260];
   private static boolean seeded;

   private Xmas() {
   }

   public static boolean on() {
      return UiSettings.xmas;
   }

   public static String countdown() {
      LocalDate d = LocalDate.now();
      int y = d.getYear();
      if (d.getMonthValue() == 12 && d.getDayOfMonth() >= 24 && d.getDayOfMonth() <= 26) {
         return "Frohe Weihnachten!";
      } else {
         LocalDate eve = LocalDate.of(d.getMonthValue() == 12 && d.getDayOfMonth() > 26 ? y + 1 : y, 12, 24);
         long n = ChronoUnit.DAYS.between(d, eve);
         return n == 1L ? "Morgen ist Heiligabend!" : "Noch " + n + " Tage bis Heiligabend";
      }
   }

   private static void seed() {
      Random r = new Random(7L);

      for (int i = 0; i < 260; i++) {
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
         Gx.gradientV(0, 0, W, H, -1710878686, 2047547952);
         float t = (float)(System.currentTimeMillis() % 1000000L) / 1000.0F;
         Gx.Img au = Gx.painted("xm_aurora", 512, 256, Xmas::paintAurora);
         if (au != null) {
            int ah = H / 2;
            float sway = (float)Math.sin(t * 0.15) * W * 0.03F;
            Gx.image(au, Math.round(-W * 0.1F + sway), -ah / 8, Math.round(W * 1.2F), ah, Gx.withAlpha(-1, 0.55F + 0.15F * (float)Math.sin(t * 0.4)));
         }

         drift(W, H);
         snow(W, H, 1.0F);
         Gx.popAlpha();
      }
   }

   public static void snow(int W, int H, float a) {
      if (on()) {
         if (!seeded) {
            seed();
         }

         float dt = Math.min(0.05F, Gx.dt);
         float t = (float)(System.currentTimeMillis() % 1000000L) / 1000.0F;
         Gx.Img dot = Gx.painted("xm_flake", 32, 32, Xmas::paintFlake);
         Gx.Img cr = Gx.painted("xm_crystal", 64, 64, Xmas::paintCrystal);
         if (dot != null) {
            int S = Math.max(1, Math.min(W, H) / 540);

            for (int i = 0; i < 260; i++) {
               float z = fz[i];
               fy[i] = fy[i] + dt * (0.035F + 0.07F * z);
               fx[i] = fx[i] + dt * (0.01F * (float)Math.sin(t * 0.7 + fs[i]) * z + 0.004F);
               if (fy[i] > 1.03F) {
                  fy[i] = -0.03F;
                  fx[i] = (fx[i] * 7.31F + 0.37F) % 1.0F;
               }

               if (fx[i] > 1.02F) {
                  fx[i]--;
               }

               int d = Math.max(2, Math.round((1.2F + 3.6F * z * z) * S));
               int x = Math.round(fx[i] * W);
               int y = Math.round(fy[i] * H);
               float al = (0.35F + 0.6F * z) * a;
               if (z > 0.93F && cr != null) {
                  int c = d * 3;
                  Gx.image(cr, x - c / 2, y - c / 2, c, c, Gx.withAlpha(-1, al));
               } else {
                  Gx.image(dot, x - d, y - d, d * 2, d * 2, Gx.withAlpha(-1, al));
               }
            }
         }
      }
   }

   private static void drift(int W, int H) {
      Gx.Img d = Gx.painted("xm_drift", 1024, 160, Xmas::paintDrift);
      if (d != null) {
         int h = Math.round(H * 0.14F);
         Gx.image(d, 0, H - h, W, h, -1);
      }
   }

   public static void snowCap(int x, int y, int w, float P) {
      if (on()) {
         Gx.Img cap = Gx.painted("xm_cap", 512, 64, Xmas::paintCap);
         if (cap != null) {
            int h = Math.round(P * 17.0F);
            int tile = h * 8;
            int r = Math.round(P * 14.0F);

            for (int cx = x + r / 2; cx < x + w - r / 2; cx += tile) {
               int tw = Math.min(tile, x + w - r / 2 - cx);
               Gx.push();
               Gx.clip(cx, y - h / 2, tw, h * 2);
               Gx.image(cap, cx, y - Math.round(h * 0.42F), tile, h, -1);
               Gx.unclip();
               Gx.pop();
            }
         }
      }
   }

   public static void lights(int x0, int y0, int x1, float P, float sag, int seed) {
      if (on()) {
         float t = (float)(System.currentTimeMillis() % 1000000L) / 1000.0F;
         int n = Math.max(3, Math.round((x1 - x0) / (P * 34.0F)));
         Gx.Img bulb = Gx.painted("xm_bulb", 32, 48, Xmas::paintBulb);
         int wire = Math.max(1, Math.round(P * 0.8F));
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
               Gx.fill(Math.round(sx0 + s), Math.round(yy0 + (yy1 - yy0) * Math.min(1.0F, f)), Math.min(st, steps - s), wire, -14861788);
            }
         }

         for (int i = 0; i < n; i++) {
            float u = (i + 0.5F) / n;
            int bx = Math.round(x0 + (x1 - x0) * u);
            int by = Math.round(y0 + sag);
            int col = BULBS[(i + seed) % BULBS.length];
            float tw = 0.55F + 0.45F * (float)Math.sin(t * (2.1 + i % 3 * 0.7) + i * 1.9 + seed);
            int gr = Math.round(P * (9.0F + 4.0F * tw));
            Gx.glow(bx, by + Math.round(P * 4.0F), gr, Gx.withAlpha(col, 0.45F * tw));
            if (bulb != null) {
               int bw = Math.round(P * 5.2F);
               int bh = Math.round(P * 7.8F);
               Gx.image(bulb, bx - bw / 2, by, bw, bh, Gx.mix(col, -1, 0.25F * tw));
            }
         }
      }
   }

   public static void hat(float cx, float top, float w, float tilt) {
      if (on()) {
         Gx.Img h = Gx.painted("xm_hat", 128, 128, Xmas::paintHat);
         if (h != null) {
            int s = Math.round(w);
            Gx.image(h, Math.round(cx - s * 0.46F + tilt), Math.round(top - s * 0.72F), s, s, -1);
         }
      }
   }

   public static void bauble(float cx, float cy, float r, int color) {
      Gx.Img b = Gx.painted("xm_bauble", 64, 64, Xmas::paintBauble);
      if (b != null) {
         int d = Math.round(r * 2.0F);
         Gx.image(b, Math.round(cx - r), Math.round(cy - r), d, d, color);
      }
   }

   public static void gift(int x, int y, int w, int h, int box, int ribbon, float lift, float P) {
      int r = Math.round(P * 7.0F);
      int yy = Math.round(y - lift);
      Gx.shadow(x, yy, w, h, r, Math.round(P * (5.0F + lift * 0.6F)), 0.45F);
      Gx.rectV(x, yy, w, h, r, Gx.mix(box, -1, 0.12F), Gx.mix(box, -16777216, 0.25F));
      int rb = Math.max(2, Math.round(w * 0.14F));
      Gx.fill(x + w / 2 - rb / 2, yy, rb, h, ribbon);
      Gx.fill(x, yy + Math.round(h * 0.3F) - rb / 2, w, rb, ribbon);
      Gx.fill(x + w / 2 - rb / 2, yy, Math.max(1, rb / 4), h, Gx.withAlpha(-1, 0.25F));
      Gx.Img bow = Gx.painted("xm_bow", 128, 72, Xmas::paintBow);
      if (bow != null) {
         int bw = Math.round(w * 0.62F);
         int bh = bw * 72 / 128;
         Gx.image(bow, x + w / 2 - bw / 2, yy + Math.round(h * 0.3F) - bh + Math.round(bh * 0.35F), bw, bh, ribbon);
      }

      Gx.outline(x, yy, w, h, r, Math.max(1, Math.round(P * 0.6F)), 587202559);
   }

   public static void candy(int x, int y, int w, int h, float shift) {
      int W = Math.max(8, w);
      int H = Math.max(4, h);
      Gx.Img c = Gx.painted("xm_candy" + W + "x" + H, W * 2, H * 2, g -> paintCandy(g, W * 2, H * 2));
      if (c != null) {
         Gx.image(c, x, y, w, h, -1);
      }
   }

   private static void paintFlake(Graphics2D g) {
      g.setPaint(
         new RadialGradientPaint(
            new Float(16.0F, 16.0F),
            16.0F,
            new float[]{0.0F, 0.35F, 1.0F},
            new Color[]{new Color(255, 255, 255, 255), new Color(235, 245, 255, 190), new Color(220, 235, 255, 0)}
         )
      );
      g.fill(new java.awt.geom.Ellipse2D.Float(0.0F, 0.0F, 32.0F, 32.0F));
   }

   private static void paintCrystal(Graphics2D g) {
      g.translate(32, 32);
      g.setColor(new Color(255, 255, 255, 235));
      g.setStroke(new BasicStroke(2.6F, 1, 1));

      for (int i = 0; i < 6; i++) {
         g.drawLine(0, 0, 0, -27);
         g.drawLine(0, -12, -7, -19);
         g.drawLine(0, -12, 7, -19);
         g.drawLine(0, -20, -5, -25);
         g.drawLine(0, -20, 5, -25);
         g.rotate(Math.PI / 3);
      }

      g.setPaint(
         new RadialGradientPaint(new Float(0.0F, 0.0F), 10.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(255, 255, 255, 255), new Color(255, 255, 255, 0)})
      );
      g.fill(new java.awt.geom.Ellipse2D.Float(-10.0F, -10.0F, 20.0F, 20.0F));
   }

   private static void paintAurora(Graphics2D g) {
      Random r = new Random(3L);
      int[][] cols = new int[][]{{60, 255, 170}, {80, 200, 255}, {170, 110, 255}};

      for (int band = 0; band < 3; band++) {
         int[] c = cols[band];
         double ph = r.nextDouble() * 6.0;
         double amp = 20 + band * 8;
         double base = 70 + band * 30;

         for (int x = 0; x < 512; x += 2) {
            double y = base + Math.sin(x / 80.0 + ph) * amp + Math.sin(x / 31.0 + ph * 2.0) * 8.0;
            double fade = Math.sin(Math.PI * x / 512.0);
            int a = (int)(70.0 * fade);
            g.setPaint(new GradientPaint(x, (float)(y - 60.0), new Color(c[0], c[1], c[2], 0), x, (float)y, new Color(c[0], c[1], c[2], a)));
            g.fillRect(x, (int)(y - 60.0), 2, 60);
            g.setPaint(new GradientPaint(x, (float)y, new Color(c[0], c[1], c[2], a), x, (float)(y + 14.0), new Color(c[0], c[1], c[2], 0)));
            g.fillRect(x, (int)y, 2, 14);
         }
      }
   }

   private static void paintDrift(Graphics2D g) {
      for (int layer = 0; layer < 2; layer++) {
         Path2D p = new Double();
         p.moveTo(0.0, 160.0);
         Random r = new Random(11 + layer);
         double base = layer == 0 ? 70.0 : 100.0;

         for (int x = 0; x <= 1024; x += 8) {
            double y = base + Math.sin(x / (140.0 + layer * 60) + layer * 2) * 22.0 + Math.sin(x / 47.0 + layer) * 6.0;
            p.lineTo(x, y);
         }

         p.lineTo(1024.0, 160.0);
         p.closePath();
         g.setPaint(
            new GradientPaint(
               0.0F,
               (float)base - 20.0F,
               layer == 0 ? new Color(200, 222, 255, 140) : new Color(244, 248, 255, 235),
               0.0F,
               160.0F,
               layer == 0 ? new Color(120, 150, 210, 180) : new Color(190, 212, 245, 255)
            )
         );
         g.fill(p);
         if (layer == 1) {
            g.setColor(new Color(255, 255, 255, 180));

            for (int i = 0; i < 90; i++) {
               float x = r.nextFloat() * 1024.0F;
               float y = 110.0F + r.nextFloat() * 50.0F;
               g.fill(new java.awt.geom.Ellipse2D.Float(x, y, 2.0F, 2.0F));
            }
         }
      }
   }

   private static void paintCap(Graphics2D g) {
      Path2D p = new Double();
      p.moveTo(0.0, 34.0);
      Random r = new Random(5L);
      double prev = 10.0;

      for (int x = 0; x <= 512; x += 32) {
         double y = 6.0 + Math.sin(x / 61.0) * 4.0 + r.nextDouble() * 4.0;
         p.quadTo(x - 16, Math.min(prev, y) - 3.0, x, y);
         prev = y;
      }

      p.lineTo(512.0, 34.0);

      for (int x = 512; x >= 0; x -= 8) {
         boolean drip = x % 56 == 0 || x % 88 == 24;
         if (drip) {
            double d = 14 + x * 7 % 16;
            p.lineTo(x + 3, 35.0);
            p.lineTo(x, 34.0 + d);
            p.lineTo(x - 3, 35.0);
         } else {
            p.lineTo(x, 33.0 + Math.sin(x / 23.0) * 2.5);
         }
      }

      p.closePath();
      g.setColor(new Color(40, 60, 110, 90));
      g.translate(0, 3);
      g.fill(p);
      g.translate(0, -3);
      g.setPaint(new GradientPaint(0.0F, 4.0F, new Color(255, 255, 255), 0.0F, 50.0F, new Color(186, 212, 248)));
      g.fill(p);
      g.setColor(new Color(255, 255, 255, 200));
      g.setStroke(new BasicStroke(2.0F));

      for (int xx = 10; xx < 512; xx += 37) {
         g.drawLine(xx, 12, xx + 12, 11);
      }

      g.setColor(new Color(150, 190, 240, 120));

      for (int xx = 0; xx < 512; xx += 3) {
         g.fillRect(xx, 30 + (int)(Math.sin(xx / 23.0) * 2.5), 3, 2);
      }
   }

   private static void paintBulb(Graphics2D g) {
      g.setColor(new Color(60, 70, 60));
      g.fillRoundRect(10, 0, 12, 10, 3, 3);
      GeneralPath b = new GeneralPath();
      b.moveTo(16.0F, 8.0F);
      b.curveTo(30.0F, 12.0F, 30.0F, 40.0F, 16.0F, 47.0F);
      b.curveTo(2.0F, 40.0F, 2.0F, 12.0F, 16.0F, 8.0F);
      g.setPaint(
         new RadialGradientPaint(
            new Float(12.0F, 22.0F),
            26.0F,
            new float[]{0.0F, 0.3F, 1.0F},
            new Color[]{new Color(255, 255, 255), new Color(255, 255, 255, 240), new Color(200, 200, 200, 230)}
         )
      );
      g.fill(b);
      g.setColor(new Color(255, 255, 255, 230));
      g.fill(new java.awt.geom.Ellipse2D.Float(10.0F, 16.0F, 5.0F, 11.0F));
   }

   private static void paintHat(Graphics2D g) {
      GeneralPath c = new GeneralPath();
      c.moveTo(18.0F, 92.0F);
      c.curveTo(30.0F, 40.0F, 60.0F, 14.0F, 96.0F, 22.0F);
      c.curveTo(112.0F, 26.0F, 118.0F, 44.0F, 114.0F, 60.0F);
      c.curveTo(104.0F, 50.0F, 92.0F, 48.0F, 88.0F, 58.0F);
      c.curveTo(96.0F, 70.0F, 104.0F, 80.0F, 110.0F, 92.0F);
      c.closePath();
      g.setPaint(new GradientPaint(20.0F, 30.0F, new Color(16730714), 100.0F, 90.0F, new Color(10358820)));
      g.fill(c);
      g.setColor(new Color(0, 0, 0, 40));
      g.setStroke(new BasicStroke(3.0F));
      g.draw(new java.awt.geom.QuadCurve2D.Float(40.0F, 80.0F, 60.0F, 50.0F, 92.0F, 30.0F));
      g.setPaint(new RadialGradientPaint(new Float(110.0F, 60.0F), 14.0F, new float[]{0.0F, 1.0F}, new Color[]{Color.WHITE, new Color(205, 220, 240)}));
      g.fill(new java.awt.geom.Ellipse2D.Float(98.0F, 50.0F, 26.0F, 26.0F));
      RoundRectangle2D brim = new java.awt.geom.RoundRectangle2D.Float(8.0F, 86.0F, 112.0F, 26.0F, 26.0F, 26.0F);
      g.setPaint(new GradientPaint(0.0F, 86.0F, Color.WHITE, 0.0F, 112.0F, new Color(200, 216, 240)));
      g.fill(brim);
      g.setColor(new Color(255, 255, 255, 200));
      Random r = new Random(2L);

      for (int i = 0; i < 40; i++) {
         g.fill(new java.awt.geom.Ellipse2D.Float(10.0F + r.nextFloat() * 106.0F, 88.0F + r.nextFloat() * 20.0F, 5.0F, 5.0F));
      }
   }

   private static void paintBauble(Graphics2D g) {
      g.setColor(new Color(200, 170, 80));
      g.fillRoundRect(26, 2, 12, 9, 3, 3);
      g.setPaint(
         new RadialGradientPaint(
            new Float(24.0F, 26.0F),
            36.0F,
            new float[]{0.0F, 0.25F, 0.7F, 1.0F},
            new Color[]{new Color(255, 255, 255), new Color(250, 250, 250), new Color(170, 170, 170), new Color(90, 90, 90)},
            CycleMethod.NO_CYCLE
         )
      );
      g.fill(new java.awt.geom.Ellipse2D.Float(6.0F, 9.0F, 52.0F, 52.0F));
      g.setColor(new Color(255, 255, 255, 170));
      g.fill(new java.awt.geom.Ellipse2D.Float(16.0F, 18.0F, 10.0F, 14.0F));
      g.setColor(new Color(255, 255, 255, 90));
      g.setStroke(new BasicStroke(2.2F));
      g.drawArc(10, 26, 44, 14, 180, 180);
   }

   private static void paintBow(Graphics2D g) {
      g.setColor(Color.WHITE);
      GeneralPath l = new GeneralPath();
      l.moveTo(64.0F, 50.0F);
      l.curveTo(40.0F, 10.0F, 6.0F, 14.0F, 14.0F, 44.0F);
      l.curveTo(20.0F, 60.0F, 44.0F, 60.0F, 64.0F, 50.0F);
      GeneralPath r = new GeneralPath();
      r.moveTo(64.0F, 50.0F);
      r.curveTo(88.0F, 10.0F, 122.0F, 14.0F, 114.0F, 44.0F);
      r.curveTo(108.0F, 60.0F, 84.0F, 60.0F, 64.0F, 50.0F);
      g.fill(l);
      g.fill(r);
      g.setColor(new Color(0, 0, 0, 60));
      g.setStroke(new BasicStroke(3.0F));
      g.draw(new java.awt.geom.QuadCurve2D.Float(24.0F, 40.0F, 40.0F, 28.0F, 58.0F, 46.0F));
      g.draw(new java.awt.geom.QuadCurve2D.Float(104.0F, 40.0F, 88.0F, 28.0F, 70.0F, 46.0F));
      g.setColor(Color.WHITE);
      g.fill(new java.awt.geom.Ellipse2D.Float(54.0F, 40.0F, 20.0F, 18.0F));
      g.fill(new Polygon(new int[]{58, 48, 58}, new int[]{54, 72, 70}, 3));
      g.fill(new Polygon(new int[]{70, 80, 70}, new int[]{54, 72, 70}, 3));
   }

   private static void paintCandy(Graphics2D g, int w, int h) {
      RoundRectangle2D pill = new java.awt.geom.RoundRectangle2D.Float(0.0F, 0.0F, w, h, h, h);
      g.setClip(pill);
      g.setColor(new Color(14886459));
      g.fillRect(0, 0, w, h);
      g.setColor(new Color(255, 255, 255));
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

      g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(255, 255, 255, 90), 0.0F, h, new Color(0, 0, 0, 70)));
      g.fillRect(0, 0, w, h);
   }
}
