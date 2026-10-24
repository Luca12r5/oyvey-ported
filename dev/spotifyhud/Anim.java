package dev.spotifyhud;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Ellipse2D.Double;

public final class Anim {
   public static final String[] IDS = new String[]{"none", "stars", "snow", "lights", "sparkle", "aurora", "waves", "xmas"};
   public static final String[] NAMES = new String[]{"Aus", "Sternschnuppen", "Schnee", "Lichterkette", "Glitzer", "Aurora", "Wellen", "Weihnachten"};
   private static final Color[] BULBS = new Color[]{new Color(255, 70, 70), new Color(80, 220, 110), new Color(255, 205, 80), new Color(90, 160, 255)};

   private Anim() {
   }

   public static int index(String id) {
      for (int i = 0; i < IDS.length; i++) {
         if (IDS[i].equals(id)) {
            return i;
         }
      }

      return 0;
   }

   public static boolean active(String id) {
      return id != null && !"none".equals(id);
   }

   public static double rnd(int i, int salt) {
      long x = i * -7046029254386353131L + salt * -4417276706812531889L;
      x ^= x >>> 33;
      x *= -49064778989728563L;
      x ^= x >>> 33;
      x *= -4265267296055464877L;
      x ^= x >>> 33;
      return (x >>> 11) * 1.110223E-16F;
   }

   public static void draw(Graphics2D g, String id, double t, double w, double h, double intensity, Color accent) {
      intensity = Math.max(0.2, Math.min(1.0, intensity));
      switch (id) {
         case "stars":
            stars(g, t, w, h, intensity);
            break;
         case "snow":
            snow(g, t, w, h, intensity);
            break;
         case "lights":
            lights(g, t, w, h, intensity);
            break;
         case "sparkle":
            sparkle(g, t, w, h, intensity, accent);
            break;
         case "aurora":
            aurora(g, t, w, h, intensity);
            break;
         case "waves":
            waves(g, t, w, h, intensity, accent);
            break;
         case "xmas":
            snow(g, t, w, h, intensity * 0.8);
            lights(g, t, w, h, intensity);
      }
   }

   private static Color c(int r, int gr, int b, double a) {
      return new Color(r, gr, b, (int)Math.max(0L, Math.min(255L, Math.round(a * 255.0))));
   }

   private static void stars(Graphics2D g, double t, double w, double h, double k) {
      int n = (int)(26.0 * k);

      for (int i = 0; i < n; i++) {
         double x = rnd(i, 1) * w;
         double y = rnd(i, 2) * h;
         double tw = 0.5 + 0.5 * Math.sin(t * (1.2 + rnd(i, 3) * 2.5) + rnd(i, 4) * 6.28);
         double r = 0.25 + rnd(i, 5) * 0.45;
         g.setColor(c(255, 255, 255, (0.15 + 0.55 * tw) * k));
         g.fill(new Double(x - r, y - r, 2.0 * r, 2.0 * r));
      }

      for (int s = 0; s < 2; s++) {
         double period = s == 0 ? 2.6 : 3.7;
         double dur = 0.9;
         double tt = t + s * 1.3;
         int slot = (int)Math.floor(tt / period);
         double local = tt - slot * period;
         if (!(local > dur) && !(rnd(slot, 10 + s) > 0.35 + 0.6 * k)) {
            double p = local / dur;
            double sx = w * (0.15 + rnd(slot, 20 + s) * 0.85);
            double sy = -4.0 + rnd(slot, 30 + s) * h * 0.35;
            double len = 28.0 + rnd(slot, 40 + s) * 20.0;
            double dx = -Math.cos(0.45);
            double dy = Math.sin(0.45);
            double travel = 70.0 + rnd(slot, 50 + s) * 60.0;
            double hx = sx + dx * travel * p;
            double hy = sy + dy * travel * p;
            double fade = p < 0.15 ? p / 0.15 : (p > 0.75 ? (1.0 - p) / 0.25 : 1.0);
            double tx = hx - dx * len;
            double ty = hy - dy * len;
            g.setStroke(new BasicStroke(0.9F, 1, 1));
            g.setPaint(new GradientPaint((float)hx, (float)hy, c(255, 255, 255, 0.95 * fade), (float)tx, (float)ty, c(255, 255, 255, 0.0)));
            g.draw(new java.awt.geom.Line2D.Double(hx, hy, tx, ty));
            glowDot(g, hx, hy, 2.4, new Color(200, 220, 255), 0.9 * fade);
         }
      }
   }

   private static void snow(Graphics2D g, double t, double w, double h, double k) {
      int n = (int)(34.0 * k);

      for (int i = 0; i < n; i++) {
         double speed = 5.0 + rnd(i, 1) * 9.0;
         double r = 0.5 + rnd(i, 2) * 1.1;
         double y = (rnd(i, 3) * (h + 10.0) + t * speed) % (h + 10.0) - 5.0;
         double sway = Math.sin(t * (0.6 + rnd(i, 4)) + rnd(i, 5) * 6.28) * (2.0 + rnd(i, 6) * 3.0);
         double x = (rnd(i, 7) * (w + 10.0) + sway + t * 1.5) % (w + 10.0) - 5.0;
         g.setColor(c(255, 255, 255, (0.35 + 0.5 * rnd(i, 8)) * (0.6 + 0.4 * k)));
         g.fill(new Double(x - r, y - r, 2.0 * r, 2.0 * r));
      }
   }

   private static void lights(Graphics2D g, double t, double w, double h, double k) {
      int n = 11;
      double sag = 4.2;
      g.setStroke(new BasicStroke(0.55F));
      g.setColor(c(20, 20, 20, 0.85));
      Path2D wire = new java.awt.geom.Path2D.Double();
      wire.moveTo(-2.0, 1.2);

      for (int i = 0; i < n; i++) {
         double x0 = -2.0 + (w + 4.0) * i / n;
         double x1 = -2.0 + (w + 4.0) * (i + 1) / n;
         wire.quadTo((x0 + x1) / 2.0, 1.2 + sag, x1, 1.2);
      }

      g.draw(wire);

      for (int i = 0; i < n; i++) {
         double x = -2.0 + (w + 4.0) * (i + 0.5) / n;
         double y = 1.2 + sag * 0.5 + 1.6;
         Color col = BULBS[i % BULBS.length];
         double phase = t * 1.6 - i * 0.35;
         double on = 0.35 + 0.65 * (0.5 + 0.5 * Math.sin(phase * Math.PI));
         glowDot(g, x, y, 5.5, col, 0.35 * on * k);
         g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), (int)(255.0 * (0.55 + 0.45 * on))));
         g.fill(new Double(x - 1.1, y - 1.5, 2.2, 3.0));
         g.setColor(c(30, 30, 30, 0.9));
         g.fill(new java.awt.geom.Rectangle2D.Double(x - 0.6, y - 2.4, 1.2, 1.0));
      }
   }

   private static void sparkle(Graphics2D g, double t, double w, double h, double k, Color accent) {
      int n = (int)(14.0 * k) + 2;

      for (int i = 0; i < n; i++) {
         double period = 1.8 + rnd(i, 1) * 1.6;
         double tt = t + rnd(i, 2) * period;
         int cycle = (int)Math.floor(tt / period);
         double p = (tt - cycle * period) / period;
         double a = Math.sin(p * Math.PI);
         double x = rnd(cycle * 31 + i, 3) * w;
         double y = rnd(cycle * 31 + i, 4) * h;
         double s = (1.2 + rnd(i, 5) * 1.8) * a;
         Color col = i % 3 == 0 ? accent : Color.WHITE;
         glowDot(g, x, y, s * 1.6, col, 0.25 * a);
         g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), (int)(230.0 * a)));
         Path2D star = new java.awt.geom.Path2D.Double();
         star.moveTo(x, y - s);
         star.quadTo(x, y, x + s, y);
         star.quadTo(x, y, x, y + s);
         star.quadTo(x, y, x - s, y);
         star.quadTo(x, y, x, y - s);
         g.fill(star);
      }
   }

   private static void aurora(Graphics2D g, double t, double w, double h, double k) {
      Color[] cols = new Color[]{new Color(60, 255, 170), new Color(90, 140, 255), new Color(200, 90, 255)};

      for (int band = 0; band < 3; band++) {
         Path2D p = new java.awt.geom.Path2D.Double();
         double base = h * (0.25 + band * 0.18);
         p.moveTo(0.0, h);

         for (int i = 0; i <= 24; i++) {
            double x = w * i / 24.0;
            double y = base + Math.sin(x * 0.045 + t * (0.5 + band * 0.2) + band) * 6.0 + Math.sin(x * 0.11 - t * 0.7 + band * 2) * 2.5;
            p.lineTo(x, y);
         }

         p.lineTo(w, h);
         p.closePath();
         Color col = cols[band];
         g.setPaint(
            new GradientPaint(
               0.0F,
               (float)(base - 8.0),
               c(col.getRed(), col.getGreen(), col.getBlue(), 0.28 * k),
               0.0F,
               (float)(base + 22.0),
               c(col.getRed(), col.getGreen(), col.getBlue(), 0.0)
            )
         );
         g.fill(p);
      }
   }

   private static void waves(Graphics2D g, double t, double w, double h, double k, Color accent) {
      for (int band = 0; band < 3; band++) {
         Path2D p = new java.awt.geom.Path2D.Double();
         double base = h - 7.0 - band * 4;
         p.moveTo(0.0, h);

         for (int i = 0; i <= 40; i++) {
            double x = w * i / 40.0;
            double y = base + Math.sin(x * 0.06 + t * (1.4 - band * 0.3) + band * 1.7) * (2.4 - band * 0.5);
            p.lineTo(x, y);
         }

         p.lineTo(w, h);
         p.closePath();
         g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(255.0 * (0.1 + 0.05 * band) * k)));
         g.fill(p);
      }
   }

   private static void glowDot(Graphics2D g, double x, double y, double r, Color col, double a) {
      if (!(a <= 0.01) && !(r <= 0.05)) {
         g.setPaint(
            new RadialGradientPaint(
               new java.awt.geom.Point2D.Double(x, y),
               (float)r,
               new float[]{0.0F, 1.0F},
               new Color[]{
                  new Color(col.getRed(), col.getGreen(), col.getBlue(), (int)(255.0 * Math.min(1.0, a))),
                  new Color(col.getRed(), col.getGreen(), col.getBlue(), 0)
               }
            )
         );
         g.fill(new Double(x - r, y - r, 2.0 * r, 2.0 * r));
      }
   }

   public static void icon(Graphics2D g, String id, double x, double y, double s, double t, Color accent) {
      Shape old = g.getClip();
      g.clip(new java.awt.geom.RoundRectangle2D.Double(x, y, s * 1.6, s, 6.0, 6.0));
      g.setColor(new Color(12, 12, 16));
      g.fill(new java.awt.geom.Rectangle2D.Double(x, y, s * 1.6, s));
      AffineTransform at = g.getTransform();
      g.translate(x, y);
      g.scale(s * 1.6 / 80.0, s / 50.0);
      if ("none".equals(id)) {
         g.setColor(new Color(255, 255, 255, 90));
         g.setStroke(new BasicStroke(2.2F, 1, 1));
         g.draw(new Double(28.0, 13.0, 24.0, 24.0));
         g.draw(new java.awt.geom.Line2D.Double(31.5, 33.5, 48.5, 16.5));
      } else {
         draw(g, id, t, 80.0, 50.0, 1.0, accent);
      }

      g.setTransform(at);
      g.setClip(old);
   }
}
