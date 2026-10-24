package dev.lego.cosmetic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Ellipse2D.Double;
import java.awt.geom.Ellipse2D.Float;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

final class PetTex {
   static final int FW = 256;
   static final int FH = 192;
   private static final Map<String, int[]> FUR = new HashMap<>();
   private static final double EY = 84.0;
   private static final double EW = 48.0;
   private static final double EH = 60.0;
   private static final double EX = 50.0;

   private PetTex() {
   }

   static CTex.T paint(String name) {
      try {
         String[] p = name.split(":");
         String var2 = p[0];
         switch (var2) {
            case "pet_eyes":
               return CTex.of(eyes(style(p[1]), p[2], "1".equals(p[3])));
            case "pet_face":
               return CTex.of(face(style(p[1]), p[2]));
            case "pet_fur":
               return CTex.of(fur(p[1], false));
            case "pet_head":
               return CTex.of(fur(p[1], true));
            case "pet_c":
               return CTex.of(solid(hex(p[1])));
            case "pet_gl":
               return CTex.of(gloss(hex(p[1])));
            case "pet_metal":
               return CTex.of(metal(hex(p[1])));
            case "pet_star":
               return CTex.of(star());
            case "pet_heart":
               return CTex.of(heartTex());
            case "pet_shell":
               return CTex.of(shell());
            case "pet_screen":
               return CTex.of(screen());
            case "pet_wing":
               return CTex.of(beeWing());
            case "pet_bat_wing":
               return CTex.of(batWing());
            case "pet_dragon_wing":
               return CTex.of(dragonWing());
            case "pet_feather":
               return CTex.of(featherWing(hex(p[1]), hex(p[2])));
            case "pet_dwing":
               return CTex.of(drakeWing(hex(p[1]), hex(p[2])));
            case "pet_tailfan":
               return CTex.of(tailFan(hex(p[1]), hex(p[2])));
            case "pet_bubble":
               return CTex.of(bubble());
            case "pet_mane":
               return CTex.of(mane());
            case "pet_horn":
               return CTex.of(horn());
            case "pet_fin":
               return CTex.of(fin(p.length > 1 ? hex(p[1]) : 16747069));
            case "pet_cap":
               return CTex.of(mushCap());
            case "pet_jelly":
               return CTex.of(jelly());
            case "pet_berry":
               return CTex.of(berry());
            case "pet_brick":
               return CTex.of(brick(p.length > 1 ? hex(p[1]) : 14876683));
            default:
               return null;
         }
      } catch (RuntimeException var4) {
         return null;
      }
   }

   private static int hex(String s) {
      return Integer.parseInt(s, 16) & 16777215;
   }

   private static BufferedImage img(int w, int h) {
      return new BufferedImage(w, h, 2);
   }

   private static Graphics2D gfx(BufferedImage im) {
      Graphics2D g = im.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      return g;
   }

   static Color c(int rgb) {
      return new Color(rgb & 16777215);
   }

   static Color ca(int rgb, int a) {
      return new Color(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, Math.max(0, Math.min(255, a)));
   }

   static int mix(int a, int b, float t) {
      int r = Math.round((a >> 16 & 0xFF) * (1.0F - t) + (b >> 16 & 0xFF) * t);
      int g = Math.round((a >> 8 & 0xFF) * (1.0F - t) + (b >> 8 & 0xFF) * t);
      int bl = Math.round((a & 0xFF) * (1.0F - t) + (b & 0xFF) * t);
      return r << 16 | g << 8 | bl;
   }

   static int light(int c, float t) {
      return mix(c, 16777215, t);
   }

   static int dark(int c, float t) {
      return mix(c, 0, t);
   }

   static int shade(int c, float t) {
      return mix(c, mix(dark(c, 0.55F), 3809098, 0.25F), t);
   }

   private static Ellipse2D el(double cx, double cy, double w, double h) {
      return new Double(cx - w / 2.0, cy - h / 2.0, w, h);
   }

   private static void soft(Graphics2D g, double cx, double cy, double w, double h, int rgb, int alpha) {
      float r = (float)Math.max(w, h) / 2.0F;
      if (!(r < 0.5F)) {
         AffineTransform old = g.getTransform();
         g.translate(cx, cy);
         g.scale(w / (2.0F * r), h / (2.0F * r));
         g.setPaint(new RadialGradientPaint(0.0F, 0.0F, r, new float[]{0.0F, 0.55F, 1.0F}, new Color[]{ca(rgb, alpha), ca(rgb, alpha * 3 / 4), ca(rgb, 0)}));
         g.fill(new Float(-r, -r, 2.0F * r, 2.0F * r));
         g.setTransform(old);
      }
   }

   private static void baseGrad(Graphics2D g, int w, int h, int base) {
      g.setPaint(
         new LinearGradientPaint(0.0F, 0.0F, 0.0F, h, new float[]{0.0F, 0.45F, 1.0F}, new Color[]{c(light(base, 0.16F)), c(base), c(shade(base, 0.28F))})
      );
      g.fillRect(0, 0, w, h);
   }

   private static void furStrokes(Graphics2D g, int w, int h, int base, long seed, int n, int alpha) {
      Random r = new Random(seed);
      g.setStroke(new BasicStroke(1.3F, 1, 1));

      for (int i = 0; i < n; i++) {
         float x = r.nextFloat() * w;
         float y = r.nextFloat() * h;
         float len = 2.0F + r.nextFloat() * 4.0F;
         double ang = (Math.PI / 2) + (r.nextFloat() - 0.5) * 0.9;
         g.setColor(r.nextBoolean() ? ca(light(base, 0.35F), alpha) : ca(shade(base, 0.4F), alpha));
         g.draw(new java.awt.geom.Line2D.Float(x, y, x + (float)Math.cos(ang) * len, y + (float)Math.sin(ang) * len));
      }
   }

   private static double sx(int w, double az) {
      return w * (0.25 - az / (Math.PI * 2));
   }

   private static double sy(int h, double elv) {
      return h * (0.5 - elv / Math.PI);
   }

   private static void patch(Graphics2D g, int w, int h, double az, double elv, double haz, double hel, int rgb, int alpha) {
      double px = sx(w, az);
      double py = sy(h, elv);
      double pw = haz / Math.PI * w;
      double ph = hel / Math.PI * h * 2.0;
      soft(g, px, py, pw * 2.2, ph * 1.15, rgb, alpha);
      g.setColor(ca(rgb, alpha));
      g.fill(el(px, py, pw * 1.7, ph * 0.9));
   }

   static BufferedImage solid(int base) {
      BufferedImage im = img(32, 32);
      Graphics2D g = gfx(im);
      baseGrad(g, 32, 32, base);
      furStrokes(g, 32, 32, base, base, 40, 22);
      g.dispose();
      return im;
   }

   static BufferedImage gloss(int base) {
      int w = 128;
      int h = 64;
      BufferedImage im = img(w, h);
      Graphics2D g = gfx(im);
      baseGrad(g, w, h, base);
      soft(g, sx(w, 0.35), sy(h, 0.55), 30.0, 22.0, 16777215, 190);
      g.setColor(ca(16777215, 235));
      g.fill(el(sx(w, 0.35), sy(h, 0.58), 10.0, 6.0));
      soft(g, sx(w, -0.9), sy(h, -0.6), 40.0, 14.0, light(base, 0.5F), 90);
      g.dispose();
      return im;
   }

   static BufferedImage metal(int base) {
      int w = 64;
      int h = 64;
      BufferedImage im = img(w, h);
      Graphics2D g = gfx(im);
      baseGrad(g, w, h, base);
      Random r = new Random(7L);

      for (int y = 0; y < h; y++) {
         g.setColor(ca(r.nextBoolean() ? 16777215 : 0, 10 + r.nextInt(14)));
         g.fillRect(0, y, w, 1);
      }

      g.setColor(ca(16777215, 60));
      g.fillRect(0, 2, w, 3);
      g.dispose();
      return im;
   }

   private static Shape starShape(double cx, double cy, double ro, double ri, int n, double rot) {
      java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();

      for (int i = 0; i < n * 2; i++) {
         double a = rot - (Math.PI / 2) + Math.PI * i / n;
         double rr = i % 2 == 0 ? ro : ri;
         double x = cx + Math.cos(a) * rr;
         double y = cy + Math.sin(a) * rr;
         if (i == 0) {
            p.moveTo(x, y);
         } else {
            p.lineTo(x, y);
         }
      }

      p.closePath();
      return p;
   }

   private static void roundStar(Graphics2D g, double cx, double cy, double r, Color fill, Color line, float lw) {
      Shape s = starShape(cx, cy, r, r * 0.5, 5, 0.0);
      g.setPaint(fill);
      g.fill(s);
      g.setStroke(new BasicStroke(lw, 1, 1));
      g.setColor(line);
      g.draw(s);
   }

   private static Shape heart(double cx, double cy, double s) {
      java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
      p.moveTo(cx, cy + s * 0.42);
      p.curveTo(cx - s * 0.62, cy - s * 0.02, cx - s * 0.48, cy - s * 0.58, cx - s * 0.02, cy - s * 0.3);
      p.lineTo(cx, cy - s * 0.26);
      p.lineTo(cx + s * 0.02, cy - s * 0.3);
      p.curveTo(cx + s * 0.48, cy - s * 0.58, cx + s * 0.62, cy - s * 0.02, cx, cy + s * 0.42);
      p.closePath();
      return p;
   }

   static BufferedImage star() {
      BufferedImage im = img(64, 64);
      Graphics2D g = gfx(im);
      soft(g, 32.0, 32.0, 64.0, 64.0, 16769658, 120);
      roundStar(g, 32.0, 33.0, 22.0, c(16767050), c(16774336), 3.0F);
      g.setPaint(new GradientPaint(0.0F, 12.0F, c(16775368), 0.0F, 54.0F, c(16756782)));
      g.fill(starShape(32.0, 33.0, 19.0, 9.5, 5, 0.0));
      g.setColor(ca(16777215, 220));
      g.fill(el(27.0, 26.0, 7.0, 5.0));
      g.dispose();
      return im;
   }

   static BufferedImage heartTex() {
      BufferedImage im = img(64, 64);
      Graphics2D g = gfx(im);
      soft(g, 32.0, 32.0, 64.0, 64.0, 16740264, 110);
      g.setPaint(new GradientPaint(0.0F, 10.0F, c(16747960), 0.0F, 54.0F, c(15741034)));
      g.fill(heart(32.0, 34.0, 52.0));
      g.setColor(ca(16777215, 210));
      g.fill(el(22.0, 24.0, 9.0, 6.0));
      g.dispose();
      return im;
   }

   static BufferedImage shell() {
      int w = 256;
      int h = 128;
      BufferedImage im = img(w, h);
      Graphics2D g = gfx(im);
      baseGrad(g, w, h, 6138446);
      Random r = new Random(3L);

      for (int row = 0; row < 5; row++) {
         for (int col = 0; col < 9; col++) {
            double cx = col * 29 + row % 2 * 14.5 + 6.0;
            double cy = row * 22 + 10;
            java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();

            for (int k = 0; k < 6; k++) {
               double a = (Math.PI / 3) * k + (Math.PI / 6);
               double x = cx + Math.cos(a) * 12.0;
               double y = cy + Math.sin(a) * 10.0;
               if (k == 0) {
                  p.moveTo(x, y);
               } else {
                  p.lineTo(x, y);
               }
            }

            p.closePath();
            g.setPaint(new GradientPaint(0.0F, (float)cy - 10.0F, c(10148714), 0.0F, (float)cy + 10.0F, c(6271053)));
            g.fill(p);
            g.setColor(ca(16777215, 40 + r.nextInt(30)));
            g.fill(el(cx - 3.0, cy - 3.0, 8.0, 5.0));
            g.setStroke(new BasicStroke(2.4F, 1, 1));
            g.setColor(c(4028980));
            g.draw(p);
         }
      }

      g.setPaint(new GradientPaint(0.0F, 108.0F, c(15323770), 0.0F, 128.0F, c(12096574)));
      g.fillRect(0, 106, w, 22);
      g.dispose();
      return im;
   }

   static BufferedImage screen() {
      BufferedImage im = img(64, 64);
      Graphics2D g = gfx(im);
      g.setPaint(new GradientPaint(0.0F, 0.0F, c(2372168), 0.0F, 64.0F, c(922658)));
      g.fillRect(0, 0, 64, 64);
      g.setColor(ca(7336959, 16));

      for (int y = 0; y < 64; y += 3) {
         g.fillRect(0, y, 64, 1);
      }

      g.setColor(ca(16777215, 36));
      g.fill(new java.awt.geom.RoundRectangle2D.Double(6.0, 4.0, 30.0, 10.0, 8.0, 8.0));
      g.dispose();
      return im;
   }

   static BufferedImage beeWing() {
      BufferedImage im = img(64, 64);
      Graphics2D g = gfx(im);
      Shape s = el(32.0, 32.0, 60.0, 60.0);
      g.setPaint(new RadialGradientPaint(24.0F, 22.0F, 40.0F, new float[]{0.0F, 1.0F}, new Color[]{ca(16777215, 200), ca(12577023, 120)}));
      g.fill(s);
      g.setStroke(new BasicStroke(2.0F));
      g.setColor(ca(10475263, 200));
      g.draw(s);
      g.setColor(ca(16777215, 140));
      g.drawLine(32, 4, 32, 60);
      g.drawLine(10, 20, 54, 44);
      g.dispose();
      return im;
   }

   static BufferedImage batWing() {
      BufferedImage im = img(128, 64);
      Graphics2D g = gfx(im);
      java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
      p.moveTo(0.0, 6.0);
      p.curveTo(40.0, 0.0, 90.0, 0.0, 126.0, 8.0);
      p.quadTo(112.0, 26.0, 116.0, 50.0);
      p.quadTo(96.0, 38.0, 82.0, 54.0);
      p.quadTo(66.0, 38.0, 48.0, 58.0);
      p.quadTo(30.0, 40.0, 0.0, 44.0);
      p.closePath();
      g.setPaint(new GradientPaint(0.0F, 0.0F, c(9072568), 0.0F, 64.0F, c(4863344)));
      g.fill(p);
      g.setStroke(new BasicStroke(2.5F, 1, 1));
      g.setColor(c(3811928));
      g.drawLine(0, 8, 116, 48);
      g.drawLine(0, 8, 82, 52);
      g.drawLine(0, 8, 48, 56);
      g.setColor(ca(14731519, 90));
      g.drawLine(2, 6, 124, 9);
      g.dispose();
      return im;
   }

   static BufferedImage dragonWing() {
      BufferedImage im = img(128, 96);
      Graphics2D g = gfx(im);
      java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
      p.moveTo(0.0, 4.0);
      p.curveTo(50.0, -4.0, 100.0, 6.0, 126.0, 20.0);
      p.quadTo(108.0, 44.0, 118.0, 80.0);
      p.quadTo(92.0, 64.0, 76.0, 86.0);
      p.quadTo(58.0, 62.0, 34.0, 90.0);
      p.quadTo(18.0, 58.0, 0.0, 56.0);
      p.closePath();
      g.setPaint(new GradientPaint(0.0F, 0.0F, c(16763368), 0.0F, 96.0F, c(12159733)));
      g.fill(p);
      g.setStroke(new BasicStroke(3.5F, 1, 1));
      g.setColor(c(8281040));
      g.drawLine(0, 5, 118, 78);
      g.drawLine(0, 5, 76, 84);
      g.drawLine(0, 5, 34, 88);
      g.setColor(c(9268192));
      g.draw(new java.awt.geom.QuadCurve2D.Double(0.0, 4.0, 60.0, -2.0, 126.0, 20.0));
      g.dispose();
      return im;
   }

   static BufferedImage bubble() {
      int w = 128;
      int h = 64;
      BufferedImage im = img(w, h);
      Graphics2D g = gfx(im);
      g.setPaint(
         new LinearGradientPaint(
            0.0F, 0.0F, 0.0F, h, new float[]{0.0F, 0.3F, 0.7F, 1.0F}, new Color[]{ca(15268607, 90), ca(13628159, 26), ca(12577023, 26), ca(10476799, 80)}
         )
      );
      g.fillRect(0, 0, w, h);
      g.setStroke(new BasicStroke(2.2F, 1, 1));
      g.setColor(ca(16777215, 210));
      g.draw(new java.awt.geom.QuadCurve2D.Double(sx(w, 1.25), sy(h, 0.35), sx(w, 1.05), sy(h, 0.85), sx(w, 0.6), sy(h, 0.95)));
      g.fill(el(sx(w, 1.3), sy(h, 0.22), 3.0, 3.0));
      soft(g, sx(w, -0.6), sy(h, -0.5), 30.0, 12.0, 16767477, 110);
      g.dispose();
      return im;
   }

   static BufferedImage mane() {
      BufferedImage im = img(64, 64);
      Graphics2D g = gfx(im);
      int[] cols = new int[]{16751560, 16763274, 16773280, 11071680, 10277631, 13150463};

      for (int i = 0; i < 6; i++) {
         g.setColor(c(cols[i]));
         g.fillRect(0, i * 64 / 6, 64, 11);
      }

      g.setColor(ca(16777215, 60));

      for (int i = 0; i < 64; i += 5) {
         g.drawLine(i, 0, i + 8, 64);
      }

      g.dispose();
      return im;
   }

   static BufferedImage horn() {
      BufferedImage im = img(32, 64);
      Graphics2D g = gfx(im);
      g.setPaint(new GradientPaint(0.0F, 0.0F, c(16775112), 0.0F, 64.0F, c(15907146)));
      g.fillRect(0, 0, 32, 64);
      g.setStroke(new BasicStroke(4.0F));
      g.setColor(ca(13208090, 150));

      for (int i = -64; i < 64; i += 12) {
         g.drawLine(0, i + 32, 32, i + 20);
      }

      g.dispose();
      return im;
   }

   static BufferedImage fin(int base) {
      BufferedImage im = img(64, 64);
      Graphics2D g = gfx(im);
      java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
      p.moveTo(2.0, 26.0);
      p.curveTo(20.0, 10.0, 40.0, 0.0, 62.0, 4.0);
      p.quadTo(50.0, 20.0, 54.0, 32.0);
      p.quadTo(50.0, 44.0, 62.0, 60.0);
      p.curveTo(40.0, 64.0, 20.0, 54.0, 2.0, 38.0);
      p.closePath();
      g.setPaint(new GradientPaint(0.0F, 0.0F, ca(light(base, 0.15F), 245), 64.0F, 0.0F, ca(light(base, 0.55F), 200)));
      g.fill(p);
      g.setClip(p);
      g.setStroke(new BasicStroke(2.0F));
      g.setColor(ca(dark(base, 0.12F), 150));

      for (int i = 4; i < 64; i += 9) {
         g.drawLine(0, 32, 64, i);
      }

      g.setClip(null);
      g.dispose();
      return im;
   }

   static BufferedImage berry() {
      int w = 64;
      int h = 32;
      BufferedImage im = img(w, h);
      Graphics2D g = gfx(im);
      g.setPaint(new GradientPaint(0.0F, 0.0F, c(16738938), 0.0F, h, c(14163514)));
      g.fillRect(0, 0, w, h);
      g.setColor(c(16769674));

      for (int y = 4; y < h; y += 6) {
         for (int x = y / 6 % 2 * 4; x < w; x += 8) {
            g.fill(el(x, y, 2.2, 3.0));
         }
      }

      g.dispose();
      return im;
   }

   static BufferedImage mushCap() {
      int w = 256;
      int h = 128;
      BufferedImage im = img(w, h);
      Graphics2D g = gfx(im);
      g.setPaint(
         new LinearGradientPaint(0.0F, 0.0F, 0.0F, h, new float[]{0.0F, 0.5F, 0.52F, 1.0F}, new Color[]{c(16739166), c(14692410), c(16773596), c(15257512)})
      );
      g.fillRect(0, 0, w, h);
      Random r = new Random(11L);

      for (int i = 0; i < 18; i++) {
         double x = r.nextDouble() * w;
         double y = 6.0 + r.nextDouble() * 46.0;
         double s = 10.0 + r.nextDouble() * 12.0;

         for (int k = -1; k <= 1; k++) {
            g.setColor(c(16775408));
            g.fill(el(x + k * w, y, s * 1.3, s * (0.6 + y / 90.0)));
            g.setColor(ca(15259856, 200));
            g.fill(el(x + k * w + 2.0, y + 2.0, s * 0.6, s * 0.3));
         }
      }

      g.setColor(ca(13215354, 120));

      for (int x = 0; x < w; x += 6) {
         g.drawLine(x, 70, x, 128);
      }

      g.dispose();
      return im;
   }

   static BufferedImage jelly() {
      int w = 128;
      int h = 64;
      BufferedImage im = img(w, h);
      Graphics2D g = gfx(im);
      g.setPaint(new LinearGradientPaint(0.0F, 0.0F, 0.0F, h, new float[]{0.0F, 0.5F, 1.0F}, new Color[]{c(16766708), c(16096480), c(12094719)}));
      g.fillRect(0, 0, w, h);
      g.setColor(ca(16777215, 90));

      for (int i = 0; i < w; i += 16) {
         g.fill(el(i + 8, 26.0, 6.0, 22.0));
      }

      soft(g, sx(w, 0.4), sy(h, 0.8), 30.0, 10.0, 16777215, 200);
      g.dispose();
      return im;
   }

   static BufferedImage brick(int base) {
      BufferedImage im = img(64, 64);
      Graphics2D g = gfx(im);
      g.setPaint(new GradientPaint(0.0F, 0.0F, c(light(base, 0.18F)), 0.0F, 64.0F, c(dark(base, 0.12F))));
      g.fillRect(0, 0, 64, 64);
      g.setColor(ca(16777215, 70));
      g.fillRect(0, 0, 64, 4);
      g.setColor(ca(0, 40));
      g.fillRect(0, 60, 64, 4);
      g.dispose();
      return im;
   }

   static BufferedImage fur(String key, boolean head) {
      int[] f = FUR.getOrDefault(key, new int[]{13421772, 16777215, 8947848});
      int base = f[0];
      int belly = f[1];
      int acc = f[2];
      int w = 256;
      int h = 128;
      BufferedImage im = img(w, h);
      Graphics2D g = gfx(im);
      baseGrad(g, w, h, base);
      long seed = key.hashCode() * 31L + (head ? 7 : 0);
      boolean smooth = key.equals("drake")
         || key.equals("slime")
         || key.equals("cloud")
         || key.equals("ghost")
         || key.equals("frog")
         || key.equals("axolotl")
         || key.equals("mush")
         || key.equals("fish")
         || key.equals("turtle");
      if (!smooth) {
         furStrokes(g, w, h, base, seed, 1600, 26);
      }

      switch (key) {
         case "cat":
            if (head) {
               patch(g, w, h, 0.0, -0.36, 0.44, 0.24, belly, 255);
               g.setStroke(new BasicStroke(5.0F, 1, 1));
               g.setColor(ca(acc, 230));

               for (int i = -1; i <= 1; i++) {
                  double x = sx(w, i * 0.16);
                  double y0 = sy(h, 0.95);
                  double y1 = sy(h, 0.62 - Math.abs(i) * 0.06);
                  g.draw(new java.awt.geom.Line2D.Double(x, y0, x, y1));
               }

               for (int s = -1; s <= 1; s += 2) {
                  for (int i = 0; i < 2; i++) {
                     double az = s * (1.3 + i * 0.25);
                     g.draw(new java.awt.geom.Line2D.Double(sx(w, az), sy(h, 0.1 + i * 0.25), sx(w, az + s * 0.35), sy(h, 0.15 + i * 0.25)));
                  }
               }
            } else {
               patch(g, w, h, 0.0, -0.35, 0.55, 0.55, belly, 255);
               tabby(g, w, h, acc);
            }
            break;
         case "shiba":
            if (head) {
               patch(g, w, h, 0.62, -0.3, 0.42, 0.34, belly, 255);
               patch(g, w, h, -0.62, -0.3, 0.42, 0.34, belly, 255);
               patch(g, w, h, 0.0, -0.5, 0.45, 0.3, belly, 255);
               patch(g, w, h, 0.34, 0.55, 0.1, 0.07, belly, 240);
               patch(g, w, h, -0.34, 0.55, 0.1, 0.07, belly, 240);
            } else {
               patch(g, w, h, 0.0, -0.3, 0.6, 0.6, belly, 255);
            }
            break;
         case "fox":
            if (head) {
               patch(g, w, h, 0.5, -0.4, 0.55, 0.32, belly, 255);
               patch(g, w, h, -0.5, -0.4, 0.55, 0.32, belly, 255);
               patch(g, w, h, 0.0, -0.55, 0.35, 0.3, belly, 255);
            } else {
               patch(g, w, h, 0.0, -0.1, 0.5, 0.65, belly, 255);
            }
            break;
         case "redpanda":
            if (head) {
               patch(g, w, h, 0.0, -0.45, 0.6, 0.3, belly, 255);
               patch(g, w, h, 0.45, 0.5, 0.16, 0.1, belly, 250);
               patch(g, w, h, -0.45, 0.5, 0.16, 0.1, belly, 250);
               g.setStroke(new BasicStroke(5.0F, 1, 1));
               g.setColor(ca(10107416, 110));

               for (int s = -1; s <= 1; s += 2) {
                  g.draw(new java.awt.geom.QuadCurve2D.Double(sx(w, s * 0.45), sy(h, -0.05), sx(w, s * 0.56), sy(h, -0.35), sx(w, s * 0.75), sy(h, -0.55)));
               }
            } else {
               patch(g, w, h, 0.0, -0.5, 0.7, 0.55, acc, 240);
            }
            break;
         case "panda":
            if (head) {
               for (int s = -1; s <= 1; s += 2) {
                  AffineTransform o = g.getTransform();
                  double px = sx(w, s * 0.38);
                  double py = sy(h, 0.03);
                  g.translate(px, py);
                  g.rotate(s * 0.45);
                  soft(g, 0.0, 0.0, 44.0, 44.0, acc, 255);
                  g.setColor(c(acc));
                  g.fill(el(0.0, 0.0, 32.0, 34.0));
                  g.setTransform(o);
               }

               patch(g, w, h, 0.0, -0.45, 0.4, 0.2, 16777215, 200);
            } else {
               g.setPaint(new GradientPaint(0.0F, 0.0F, c(acc), 0.0F, h, c(light(acc, 0.05F))));
               g.fill(new java.awt.geom.Rectangle2D.Double(0.0, sy(h, 0.75), w, sy(h, 0.1) - sy(h, 0.75)));
               furStrokes(g, w, h, acc, seed + 1L, 300, 30);
            }
            break;
         case "penguin":
            if (head) {
               g.setColor(c(belly));
               Area a = new Area(el(sx(w, 0.3), sy(h, 0.0), 60.0, 58.0));
               a.add(new Area(el(sx(w, -0.3), sy(h, 0.0), 60.0, 58.0)));
               a.add(new Area(el(sx(w, 0.0), sy(h, -0.35), 70.0, 50.0)));
               g.fill(a);
            } else {
               patch(g, w, h, 0.0, -0.1, 0.75, 0.85, belly, 255);
            }
            break;
         case "axolotl":
            patch(g, w, h, 0.0, head ? -0.5 : -0.4, 0.6, 0.4, belly, 220);
            spots(g, w, h, acc, seed, 26, 3, 6, 160);
            break;
         case "frog":
            patch(g, w, h, 0.0, head ? -0.6 : -0.4, 0.75, 0.45, belly, 250);
            spots(g, w, h, acc, seed, head ? 8 : 16, 4, 10, 170);
            break;
         case "chick":
            patch(g, w, h, 0.0, -0.4, 0.6, 0.55, belly, 200);
            break;
         case "hamster":
            if (head) {
               patch(g, w, h, 0.62, -0.35, 0.45, 0.35, belly, 255);
               patch(g, w, h, -0.62, -0.35, 0.45, 0.35, belly, 255);
               patch(g, w, h, 0.0, -0.55, 0.4, 0.3, belly, 255);
               patch(g, w, h, 0.0, 0.55, 0.12, 0.35, acc, 120);
            } else {
               patch(g, w, h, 0.0, -0.3, 0.7, 0.65, belly, 255);
            }
            break;
         case "owl":
            if (head) {
               for (int s = -1; s <= 1; s += 2) {
                  patch(g, w, h, s * 0.36, 0.02, 0.36, 0.34, belly, 255);
               }

               g.setStroke(new BasicStroke(2.0F));
               g.setColor(ca(acc, 120));

               for (int s = -1; s <= 1; s += 2) {
                  g.draw(el(sx(w, s * 0.36), sy(h, 0.02), 50.0, 50.0));
               }
            } else {
               patch(g, w, h, 0.0, -0.3, 0.6, 0.65, belly, 255);
               chevrons(g, sx(w, 0.0), sy(h, -0.3), acc);
            }
            break;
         case "bee":
            if (!head) {
               g.setColor(c(acc));

               for (int i = 0; i < 3; i++) {
                  double y = h * (0.3 + i * 0.22);
                  g.fill(new java.awt.geom.Rectangle2D.Double(0.0, y, w, h * 0.1));
               }

               furStrokes(g, w, h, acc, seed + 2L, 400, 30);
            } else {
               patch(g, w, h, 0.0, -0.45, 0.5, 0.3, belly, 180);
            }
            break;
         case "dragon":
            if (head) {
               patch(g, w, h, 0.0, -0.5, 0.45, 0.3, belly, 230);
            } else {
               patch(g, w, h, 0.0, -0.3, 0.55, 0.65, belly, 255);
               g.setStroke(new BasicStroke(2.0F));
               g.setColor(ca(14727280, 170));

               for (int i = 0; i < 6; i++) {
                  double y = sy(h, -0.8 + i * 0.22);
                  g.draw(new java.awt.geom.QuadCurve2D.Double(sx(w, 0.45), y, sx(w, 0.0), y + 5.0, sx(w, -0.45), y));
               }

               spots(g, w, h, acc, seed, 20, 4, 8, 120);
            }
            break;
         case "unicorn":
            spots(g, w, h, 16766706, seed, 20, 3, 5, 160);
            if (head) {
               patch(g, w, h, 0.0, -0.55, 0.4, 0.28, 16769262, 200);
            }
            break;
         case "bear":
            if (head) {
               patch(g, w, h, 0.0, -0.45, 0.45, 0.3, belly, 255);
            } else {
               patch(g, w, h, 0.0, -0.25, 0.5, 0.55, belly, 255);
               g.setColor(ca(16743068, 190));
               g.fill(heart(sx(w, 0.0), sy(h, -0.15), 22.0));
            }
            break;
         case "turtle":
            patch(g, w, h, 0.0, -0.5, 0.6, 0.35, belly, 230);
            spots(g, w, h, acc, seed, 14, 4, 8, 140);
            break;
         case "bat":
            if (head) {
               patch(g, w, h, 0.0, -0.1, 0.85, 0.55, belly, 255);
            }
            break;
         case "fish":
            patch(g, w, h, 0.0, -0.6, 1.2, 0.5, belly, 230);
            scales(g, w, h, 16756848);
            break;
         case "cloud":
         case "ghost":
            g.setPaint(new LinearGradientPaint(0.0F, 0.0F, 0.0F, h, new float[]{0.0F, 0.55F, 1.0F}, new Color[]{c(16777215), c(base), c(acc)}));
            g.fillRect(0, 0, w, h);
            break;
         case "slime":
            spots(g, w, h, light(base, 0.4F), seed, 10, 3, 6, 120);
            break;
         case "mush":
            patch(g, w, h, 0.0, -0.3, 0.6, 0.5, belly, 150);
            break;
         case "bunny":
            if (head) {
               patch(g, w, h, 0.0, -0.45, 0.5, 0.32, belly, 255);
               patch(g, w, h, 0.0, 0.7, 0.14, 0.35, 16777215, 150);
            } else {
               patch(g, w, h, 0.0, -0.35, 0.6, 0.55, belly, 255);
            }
            break;
         case "wolf":
            if (head) {
               patch(g, w, h, 0.5, -0.35, 0.5, 0.35, belly, 255);
               patch(g, w, h, -0.5, -0.35, 0.5, 0.35, belly, 255);
               patch(g, w, h, 0.0, 0.6, 0.35, 0.3, acc, 150);

               for (int sd = -1; sd <= 1; sd += 2) {
                  patch(g, w, h, sd * 0.36, 0.34, 0.1, 0.06, belly, 230);
               }
            } else {
               patch(g, w, h, 0.0, -0.45, 0.8, 0.55, belly, 255);
               patch(g, w, h, Math.PI, 0.75, 1.6, 0.55, acc, 170);
            }
            break;
         case "horse":
            if (head) {
               patch(g, w, h, 0.0, 0.1, 0.1, 0.6, acc, 235);
            } else {
               patch(g, w, h, 0.0, -0.6, 0.6, 0.35, belly, 140);
            }
            break;
         case "fawn":
            patch(g, w, h, 0.0, -0.45, 0.6, 0.5, belly, 255);
            if (!head) {
               Random r = new Random(seed);

               for (int i = 0; i < 46; i++) {
                  double az = r.nextDouble() * Math.PI * 2.0;
                  double el = 0.1 + r.nextDouble() * 0.75;
                  g.setColor(ca(acc, 235));
                  double ss = 5.0 + r.nextDouble() * 4.0;
                  g.fill(el(az / (Math.PI * 2) * w, sy(h, el), ss * 1.3, ss));
               }
            }
            break;
         case "deer":
            patch(g, w, h, 0.0, -0.45, 0.6, 0.5, belly, 255);
            if (!head) {
               patch(g, w, h, Math.PI, -0.1, 0.45, 0.35, 16777215, 220);
            }
            break;
         case "lion":
            patch(g, w, h, 0.0, -0.45, 0.6, 0.5, belly, 255);
            break;
         case "tiger":
            if (!head) {
               patch(g, w, h, 0.0, -0.6, 0.7, 0.4, belly, 255);
            } else {
               for (int sd = -1; sd <= 1; sd += 2) {
                  patch(g, w, h, sd * 0.5, -0.35, 0.45, 0.35, belly, 255);
               }

               for (int sd = -1; sd <= 1; sd += 2) {
                  patch(g, w, h, sd * 0.33, 0.36, 0.1, 0.06, belly, 240);
               }
            }

            stripes(g, w, h, acc, seed, head ? 14 : 22, head);
            break;
         case "retr":
            patch(g, w, h, 0.0, -0.4, 0.6, 0.5, belly, 220);
            furStrokes(g, w, h, light(base, 0.25F), seed + 5L, 500, 60);
            break;
         case "afox":
            patch(g, w, h, 0.0, -0.4, 0.6, 0.5, 16777215, 255);
            furStrokes(g, w, h, acc, seed + 5L, 400, 45);
            break;
         case "eagle":
            g.setStroke(new BasicStroke(2.0F, 1, 1));

            for (int row = 0; row < 7; row++) {
               for (int x = 0; x < w + 16; x += 16) {
                  double xx = x + row % 2 * 8;
                  double y = 10 + row * 16;
                  g.setColor(ca(light(base, 0.3F), 140));
                  g.draw(new java.awt.geom.Arc2D.Double(xx - 8.0, y - 8.0, 16.0, 16.0, 180.0, 180.0, 0));
               }
            }
            break;
         case "eagleh":
            patch(g, w, h, 0.0, -0.5, 0.5, 0.3, 16777215, 200);
            break;
         case "drake":
            if (!head) {
               patch(g, w, h, 0.0, -0.55, 0.7, 0.45, belly, 255);
               g.setStroke(new BasicStroke(2.0F));
               g.setColor(ca(dark(belly, 0.25F), 170));

               for (int i = 0; i < 6; i++) {
                  double y = sy(h, -0.95 + i * 0.13);
                  g.draw(new java.awt.geom.Line2D.Double(sx(w, 0.6), y, sx(w, -0.6), y));
               }
            } else {
               patch(g, w, h, 0.0, -0.55, 0.45, 0.3, belly, 230);
            }

            scales(g, w, h, light(base, 0.25F));
            break;
         default:
            if (!head) {
               patch(g, w, h, 0.0, -0.35, 0.55, 0.55, belly, 255);
            }
      }

      g.dispose();
      return im;
   }

   private static void stripes(Graphics2D g, int w, int h, int col, long seed, int n, boolean head) {
      Random r = new Random(seed);
      g.setColor(ca(col, 235));

      for (int i = 0; i < n; i++) {
         double az = -Math.PI + (i + r.nextDouble() * 0.5) * Math.PI * 2.0 / n;
         if (!head || !(Math.abs(az) < 0.55)) {
            double x = sx(w, az);
            double top = head ? sy(h, 1.3) : sy(h, 1.5);
            double bot = head ? sy(h, 0.35) : sy(h, -0.35 + r.nextDouble() * 0.25);
            double wd = 3.0 + r.nextDouble() * 4.0;
            java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
            p.moveTo(x - wd, top);
            p.quadTo(x + 6.0, (top + bot) / 2.0, x, bot);
            p.quadTo(x + 6.0 + wd, (top + bot) / 2.0, x + wd, top);
            p.closePath();

            for (int k = -1; k <= 1; k++) {
               AffineTransform o = g.getTransform();
               g.translate(k * w, 0);
               g.fill(p);
               g.setTransform(o);
            }
         }
      }
   }

   static BufferedImage featherWing(int base, int tip) {
      BufferedImage im = img(128, 64);
      Graphics2D g = gfx(im);
      java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
      p.moveTo(0.0, 2.0);
      p.curveTo(40.0, -2.0, 90.0, 0.0, 126.0, 10.0);
      p.lineTo(120.0, 26.0);

      for (int i = 0; i < 6; i++) {
         double x = 118 - i * 14;
         double y = 34 + i * 4;
         p.lineTo(x, y + 12.0);
         p.lineTo(x - 7.0, y + 4.0);
      }

      p.quadTo(30.0, 50.0, 0.0, 44.0);
      p.closePath();
      g.setPaint(new GradientPaint(0.0F, 0.0F, c(light(base, 0.15F)), 128.0F, 0.0F, c(tip)));
      g.fill(p);
      g.setClip(p);
      g.setStroke(new BasicStroke(1.6F));

      for (int i = 0; i < 16; i++) {
         double x = 10 + i * 8;
         g.setColor(ca(dark(base, 0.35F), 150));
         g.draw(new java.awt.geom.Line2D.Double(x, 14.0, x - 6.0, 62.0));
      }

      g.setColor(ca(light(base, 0.2F), 200));
      g.fill(new Double(-10.0, -6.0, 90.0, 26.0));
      g.setClip(null);
      g.setStroke(new BasicStroke(2.0F));
      g.setColor(c(dark(base, 0.4F)));
      g.draw(new java.awt.geom.QuadCurve2D.Double(0.0, 2.0, 60.0, -2.0, 126.0, 10.0));
      g.dispose();
      return im;
   }

   static BufferedImage drakeWing(int bone, int membrane) {
      BufferedImage im = img(128, 96);
      Graphics2D g = gfx(im);
      java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
      p.moveTo(0.0, 4.0);
      p.curveTo(50.0, -4.0, 100.0, 6.0, 126.0, 20.0);
      p.quadTo(108.0, 44.0, 118.0, 80.0);
      p.quadTo(92.0, 64.0, 76.0, 86.0);
      p.quadTo(58.0, 62.0, 34.0, 90.0);
      p.quadTo(18.0, 58.0, 0.0, 56.0);
      p.closePath();
      g.setPaint(new GradientPaint(0.0F, 0.0F, ca(light(membrane, 0.25F), 240), 0.0F, 96.0F, ca(membrane, 230)));
      g.fill(p);
      g.setStroke(new BasicStroke(4.0F, 1, 1));
      g.setColor(c(bone));
      g.drawLine(0, 5, 118, 78);
      g.drawLine(0, 5, 76, 84);
      g.drawLine(0, 5, 34, 88);
      g.draw(new java.awt.geom.QuadCurve2D.Double(0.0, 4.0, 60.0, -2.0, 126.0, 20.0));
      g.fill(new Double(121.0, 14.0, 8.0, 8.0));
      g.dispose();
      return im;
   }

   static BufferedImage tailFan(int base, int tip) {
      BufferedImage im = img(64, 64);
      Graphics2D g = gfx(im);

      for (int i = -3; i <= 3; i++) {
         AffineTransform o = g.getTransform();
         g.translate(32, 2);
         g.rotate(i * 0.16);
         g.setPaint(new GradientPaint(0.0F, 0.0F, c(base), 0.0F, 60.0F, c(tip)));
         g.fill(new java.awt.geom.RoundRectangle2D.Double(-6.0, 0.0, 12.0, 60.0, 10.0, 10.0));
         g.setColor(ca(dark(base, 0.3F), 160));
         g.drawLine(0, 4, 0, 56);
         g.setTransform(o);
      }

      g.dispose();
      return im;
   }

   private static void tabby(Graphics2D g, int w, int h, int acc) {
      g.setStroke(new BasicStroke(9.0F, 1, 1));
      g.setColor(ca(acc, 200));

      for (int i = 0; i < 5; i++) {
         double az = Math.PI + (i - 2) * 0.42;
         double x = sx(w, az);
         g.draw(new java.awt.geom.QuadCurve2D.Double(x, sy(h, 1.4), x + 8.0, sy(h, 0.8), x - 2.0, sy(h, 0.15)));
         g.draw(new java.awt.geom.QuadCurve2D.Double(x + w, sy(h, 1.4), x + w + 8.0, sy(h, 0.8), x + w - 2.0, sy(h, 0.15)));
         g.draw(new java.awt.geom.QuadCurve2D.Double(x - w, sy(h, 1.4), x - w + 8.0, sy(h, 0.8), x - w - 2.0, sy(h, 0.15)));
      }
   }

   private static void spots(Graphics2D g, int w, int h, int col, long seed, int n, int min, int max, int alpha) {
      Random r = new Random(seed);

      for (int i = 0; i < n; i++) {
         double x = r.nextDouble() * w;
         double y = 12.0 + r.nextDouble() * (h * 0.6);
         double s = min + r.nextDouble() * (max - min);
         if (!(Math.abs(x - w * 0.25) < w * 0.12) || !(y > h * 0.3)) {
            g.setColor(ca(col, alpha));
            g.fill(el(x, y, s * 1.4, s));
         }
      }
   }

   private static void chevrons(Graphics2D g, double cx, double cy, int acc) {
      g.setStroke(new BasicStroke(2.6F, 1, 1));
      g.setColor(ca(acc, 150));

      for (int row = 0; row < 4; row++) {
         for (int col = -2; col <= 2; col++) {
            double x = cx + col * 12 + row % 2 * 6;
            double y = cy - 18.0 + row * 11;
            g.draw(new java.awt.geom.QuadCurve2D.Double(x - 4.0, y, x, y + 4.0, x + 4.0, y));
         }
      }
   }

   private static void scales(Graphics2D g, int w, int h, int col) {
      g.setStroke(new BasicStroke(2.0F, 1, 1));
      g.setColor(ca(col, 150));

      for (int row = 0; row < 6; row++) {
         for (int x = 0; x < w + 12; x += 12) {
            double xx = x + row % 2 * 6;
            double y = 20 + row * 12;
            g.draw(new java.awt.geom.Arc2D.Double(xx - 6.0, y - 6.0, 12.0, 12.0, 180.0, 180.0, 0));
         }
      }
   }

   static PetTex.St style(String s) {
      int stage = 0;
      int dot = s.lastIndexOf(46);
      if (dot > 0) {
         try {
            stage = Integer.parseInt(s.substring(dot + 1));
         } catch (NumberFormatException var4) {
         }

         s = s.substring(0, dot);
      }

      PetTex.St t = style0(s);
      if (stage == 1) {
         t.eye *= 0.86F;
         t.gap *= 1.03F;
      } else if (stage >= 2) {
         t.eye *= 0.72F;
         t.gap *= 1.08F;
      }

      return t;
   }

   private static PetTex.St style0(String s) {
      PetTex.St t = new PetTex.St();
      switch (s) {
         case "cat":
            t.iris = 4173418;
            t.mouth = 1;
            t.whiskers = true;
            break;
         case "dog":
            t.iris = 6961694;
            t.mouth = 1;
            break;
         case "beak":
            t.iris = 3811904;
            t.mouth = 2;
            break;
         case "owl":
            t.iris = 14719514;
            t.mouth = 2;
            t.eye = 1.15F;
            t.gap = 1.05F;
            break;
         case "gem":
            t.iris = 10509552;
            t.lashes = true;
            break;
         case "amber":
            t.iris = 14716958;
            break;
         case "blue":
            t.iris = 4097766;
            break;
         case "rim":
            t.iris = 4864624;
            t.rim = true;
            break;
         case "led":
            t.iris = 7336959;
            t.led = true;
            t.line = 7336959;
            t.blush = 16740296;
            break;
         case "blob":
            t.iris = 3814224;
            t.eye = 0.92F;
            t.gap = 0.92F;
            break;
         case "pink":
            t.iris = 14700682;
            t.lashes = true;
            break;
         case "wolf":
            t.iris = 14262814;
            t.mouth = 1;
            t.eye = 0.92F;
            t.gap = 1.05F;
            break;
         case "horse":
            t.iris = 4860956;
            t.lashes = true;
            t.eye = 0.9F;
            t.gap = 1.3F;
            break;
         case "deer":
            t.iris = 3021844;
            t.lashes = true;
            t.eye = 1.0F;
            t.gap = 1.22F;
            break;
         case "lion":
            t.iris = 13666842;
            t.mouth = 1;
            t.eye = 0.9F;
            break;
         case "tiger":
            t.iris = 15249950;
            t.mouth = 1;
            t.eye = 0.9F;
            break;
         case "retr":
            t.iris = 5911578;
            t.mouth = 1;
            t.eye = 0.95F;
            break;
         case "afox":
            t.iris = 13144106;
            t.mouth = 1;
            t.eye = 0.95F;
            break;
         case "eagle":
            t.iris = 15777824;
            t.mouth = 2;
            t.eye = 0.85F;
            t.gap = 1.1F;
            break;
         case "drake":
            t.iris = 15769632;
            t.eye = 0.9F;
      }

      return t;
   }

   static BufferedImage eyes(PetTex.St s, String emo, boolean closed) {
      BufferedImage im = img(256, 192);
      Graphics2D g = gfx(im);
      double ew = 48.0 * s.eye;
      double eh = 60.0 * s.eye;
      double ex = 50.0 * s.gap;

      for (int side = -1; side <= 1; side += 2) {
         double cx = 128.0 + side * ex;
         double cy = 84.0;
         boolean inner = true;
         if (closed) {
            closedEye(g, s, emo, cx, cy, ew, eh, side);
         } else {
            switch (emo) {
               case "love":
                  heartEye(g, s, cx, cy + 2.0, ew * 1.2);
                  break;
               case "excited":
                  starEye(g, s, cx, cy + 2.0, ew * 0.78);
                  break;
               case "dizzy":
                  spiralEye(g, s, cx, cy + 2.0, ew * 0.55);
                  break;
               case "sleepy":
                  Shape clip = new java.awt.geom.Rectangle2D.Double(cx - ew, cy - eh * 0.02, ew * 2.0, eh);
                  g.setClip(clip);
                  openEye(g, s, cx, cy + 4.0, ew, eh * 0.95, false, 0);
                  g.setClip(null);
                  lid(g, s, cx - ew * 0.56, cy - eh * 0.02, cx + ew * 0.56, cy - eh * 0.02);
                  break;
               case "angry":
                  double ix = cx - side * ew * 0.6;
                  double ox = cx + side * ew * 0.6;
                  java.awt.geom.Path2D.Double cut = new java.awt.geom.Path2D.Double();
                  cut.moveTo(ix, cy + eh * 0.02);
                  cut.lineTo(ox, cy - eh * 0.34);
                  cut.lineTo(ox, cy + eh);
                  cut.lineTo(ix, cy + eh);
                  cut.closePath();
                  g.setClip(cut);
                  openEye(g, s, cx, cy + 4.0, ew * 0.95, eh * 0.9, false, 0);
                  g.setClip(null);
                  lid(g, s, ix, cy + eh * 0.02, ox, cy - eh * 0.34);
                  break;
               case "sad":
                  openEye(g, s, cx, cy + 3.0, ew * 1.04, eh * 1.02, true, 1);
                  break;
               case "curious":
                  float k = side < 0 ? 1.1F : 0.9F;
                  openEye(g, s, cx, cy + (side < 0 ? -2 : 3), ew * k, eh * k, false, 0);
                  break;
               default:
                  openEye(g, s, cx, cy, ew, eh, true, 2);
            }
         }
      }

      g.dispose();
      return im;
   }

   private static void lid(Graphics2D g, PetTex.St s, double x0, double y0, double x1, double y1) {
      g.setStroke(new BasicStroke(6.5F, 1, 1));
      g.setColor(s.led ? c(s.iris) : c(2364440));
      g.draw(new java.awt.geom.Line2D.Double(x0, y0, x1, y1));
   }

   private static void openEye(Graphics2D g, PetTex.St s, double cx, double cy, double w, double h, boolean big, int mode) {
      if (s.led) {
         soft(g, cx, cy, w * 1.6, h * 1.5, s.iris, 110);
         g.setColor(c(light(s.iris, 0.3F)));
         g.fill(new java.awt.geom.RoundRectangle2D.Double(cx - w * 0.4, cy - h * 0.45, w * 0.8, h * 0.9, w * 0.7, w * 0.7));
         g.setColor(ca(16777215, 200));
         g.fill(el(cx - w * 0.12, cy - h * 0.2, w * 0.2, h * 0.2));
      } else {
         if (s.rim) {
            g.setColor(c(16777215));
            g.fill(el(cx, cy, w + 9.0, h + 9.0));
         }

         Ellipse2D e = el(cx, cy, w, h);
         g.setPaint(
            new LinearGradientPaint(
               (float)cx,
               (float)(cy - h / 2.0),
               (float)cx,
               (float)(cy + h / 2.0),
               new float[]{0.0F, 0.45F, 1.0F},
               new Color[]{c(1773600), c(mix(1773600, s.iris, 0.35F)), c(light(s.iris, 0.25F))}
            )
         );
         g.fill(e);
         g.setColor(ca(787984, 170));
         g.fill(el(cx, cy - h * 0.05, w * 0.5, h * 0.55));
         g.setClip(e);
         soft(g, cx, cy + h * 0.42, w * 0.9, h * 0.45, light(s.iris, 0.55F), 160);
         g.setClip(null);
         g.setStroke(new BasicStroke(3.0F));
         g.setColor(c(1773600));
         g.draw(e);
         g.setColor(c(16777215));
         g.fill(el(cx - w * 0.17, cy - h * 0.2, w * 0.46, h * 0.38));
         g.fill(el(cx + w * 0.2, cy + h * 0.2, w * 0.18, w * 0.18));
         if (mode == 1) {
            g.setClip(e);
            g.setColor(ca(10477823, 150));
            g.fill(el(cx, cy + h * 0.5, w * 1.1, h * 0.45));
            g.setClip(null);
            g.setColor(ca(16777215, 230));
            g.fill(el(cx + w * 0.02, cy + h * 0.02, w * 0.14, w * 0.14));
            g.setStroke(new BasicStroke(2.5F, 1, 1));
            g.setColor(ca(16777215, 200));
            g.draw(new java.awt.geom.Arc2D.Double(cx - w * 0.36, cy - h * 0.2, w * 0.72, h * 0.7, 200.0, 60.0, 0));
         } else if (mode == 2) {
            g.setColor(c(16777215));
            g.fill(starShape(cx + w * 0.18, cy - h * 0.28, w * 0.13, w * 0.04, 4, 0.0));
         }

         if (s.lashes) {
            g.setStroke(new BasicStroke(3.5F, 1, 1));
            g.setColor(c(1773600));
            double sd = cx < 128.0 ? -1.0 : 1.0;

            for (int i = 0; i < 2; i++) {
               double a = Math.toRadians(sd < 0.0 ? 150 + i * 18 : 30 - i * 18);
               double x0 = cx + Math.cos(a) * w * 0.5;
               double y0 = cy - Math.sin(a) * h * 0.5;
               g.draw(new java.awt.geom.Line2D.Double(x0, y0, x0 + Math.cos(a) * 9.0, y0 - Math.sin(a) * 9.0));
            }
         }
      }
   }

   private static void closedEye(Graphics2D g, PetTex.St s, String emo, double cx, double cy, double w, double h, int side) {
      Color col = s.led ? c(light(s.iris, 0.3F)) : c(2364440);
      if (s.led) {
         soft(g, cx, cy + 6.0, w * 1.4, h * 0.8, s.iris, 90);
      }

      if (s.rim) {
         g.setColor(ca(16777215, 230));
         g.fill(el(cx, cy + 8.0, w * 0.9, h * 0.45));
      }

      g.setStroke(new BasicStroke(7.0F, 1, 1));
      g.setColor(col);
      switch (emo) {
         case "happy":
         case "love":
            g.draw(new java.awt.geom.Arc2D.Double(cx - w * 0.42, cy - h * 0.05, w * 0.84, h * 0.6, 20.0, 140.0, 0));
            break;
         case "excited":
            java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
            double d = -side;
            p.moveTo(cx - d * w * 0.3, cy - h * 0.18);
            p.lineTo(cx + d * w * 0.3, cy + h * 0.04);
            p.lineTo(cx - d * w * 0.3, cy + h * 0.26);
            g.draw(p);
            break;
         case "dizzy":
            g.setStroke(new BasicStroke(6.0F, 1, 1));
            double r = w * 0.28;
            g.draw(new java.awt.geom.Line2D.Double(cx - r, cy - r + 6.0, cx + r, cy + r + 6.0));
            g.draw(new java.awt.geom.Line2D.Double(cx - r, cy + r + 6.0, cx + r, cy - r + 6.0));
            break;
         case "angry":
            g.draw(new java.awt.geom.Line2D.Double(cx - side * w * 0.4, cy + h * 0.06, cx + side * w * 0.4, cy - h * 0.08));
            break;
         default:
            g.draw(new java.awt.geom.Arc2D.Double(cx - w * 0.42, cy - h * 0.2, w * 0.84, h * 0.5, 200.0, 140.0, 0));
            g.setStroke(new BasicStroke(3.5F, 1, 1));
            double ox = cx + side * w * 0.4;
            g.draw(new java.awt.geom.Line2D.Double(ox, cy + h * 0.02, ox + side * 7, cy + h * 0.08));
      }
   }

   private static void heartEye(Graphics2D g, PetTex.St s, double cx, double cy, double sz) {
      if (s.led) {
         soft(g, cx, cy, sz * 1.3, sz * 1.2, 16740296, 110);
      }

      if (s.rim) {
         g.setColor(c(16777215));
         g.fill(el(cx, cy, sz * 0.95, sz * 0.9));
      }

      Shape h = heart(cx, cy, sz);
      g.setPaint(new GradientPaint(0.0F, (float)(cy - sz * 0.4), c(16744366), 0.0F, (float)(cy + sz * 0.4), c(15212636)));
      g.fill(h);
      g.setStroke(new BasicStroke(3.0F, 1, 1));
      g.setColor(c(11014206));
      if (!s.led) {
         g.draw(h);
      }

      g.setColor(c(16777215));
      g.fill(el(cx - sz * 0.2, cy - sz * 0.17, sz * 0.2, sz * 0.14));
      g.fill(el(cx + sz * 0.12, cy + sz * 0.08, sz * 0.08, sz * 0.08));
   }

   private static void starEye(Graphics2D g, PetTex.St s, double cx, double cy, double r) {
      if (s.led) {
         soft(g, cx, cy, r * 2.6, r * 2.6, 16769658, 110);
      }

      if (s.rim) {
         g.setColor(c(16777215));
         g.fill(el(cx, cy, r * 2.1, r * 2.1));
      }

      Shape st = starShape(cx, cy + 2.0, r, r * 0.5, 5, 0.0);
      g.setPaint(new GradientPaint(0.0F, (float)(cy - r), c(16774048), 0.0F, (float)(cy + r), c(16755228)));
      g.fill(st);
      g.setStroke(new BasicStroke(4.0F, 1, 1));
      g.setColor(c(14250778));
      g.draw(st);
      g.setColor(c(16777215));
      g.fill(el(cx - r * 0.25, cy - r * 0.2, r * 0.32, r * 0.24));
   }

   private static void spiralEye(Graphics2D g, PetTex.St s, double cx, double cy, double r) {
      if (!s.led) {
         g.setColor(ca(16777215, 240));
         g.fill(el(cx, cy, r * 2.2, r * 2.2));
         g.setStroke(new BasicStroke(2.5F));
         g.setColor(c(2364440));
         g.draw(el(cx, cy, r * 2.2, r * 2.2));
      } else {
         soft(g, cx, cy, r * 2.8, r * 2.8, s.iris, 100);
      }

      java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();

      for (int i = 0; i <= 80; i++) {
         double t = i / 80.0;
         double a = t * Math.PI * 5.2;
         double rr = r * 0.95 * t;
         double x = cx + Math.cos(a) * rr;
         double y = cy + Math.sin(a) * rr;
         if (i == 0) {
            p.moveTo(x, y);
         } else {
            p.lineTo(x, y);
         }
      }

      g.setStroke(new BasicStroke(4.5F, 1, 1));
      g.setColor(s.led ? c(light(s.iris, 0.3F)) : c(3810384));
      g.draw(p);
   }

   static BufferedImage face(PetTex.St s, String emo) {
      BufferedImage im = img(256, 192);
      Graphics2D g = gfx(im);
      double ex = 50.0 * s.gap;
      double ew = 48.0 * s.eye;
      double eh = 60.0 * s.eye;
      Color line = c(s.line);
      int bl = s.blush;
      int ba = "love".equals(emo) ? 210 : ("angry".equals(emo) ? 150 : ("sad".equals(emo) ? 90 : ("excited".equals(emo) ? 180 : 140)));
      if ("angry".equals(emo)) {
         bl = 16734826;
      }

      for (int side = -1; side <= 1; side += 2) {
         double bx = 128.0 + side * (ex + ew * 0.62);
         double by = 84.0 + eh * 0.62;
         soft(g, bx, by, 50.0, 30.0, bl, ba);
         if ("love".equals(emo) || "excited".equals(emo) || "happy".equals(emo)) {
            g.setStroke(new BasicStroke(2.6F, 1, 1));
            g.setColor(ca(16777215, 150));

            for (int i = -1; i <= 1; i++) {
               g.draw(new java.awt.geom.Line2D.Double(bx + i * 8 - 3.0, by + 5.0, bx + i * 8 + 3.0, by - 5.0));
            }
         }
      }

      if (s.whiskers) {
         g.setStroke(new BasicStroke(2.2F, 1, 1));
         g.setColor(ca(5913130, 150));

         for (int sidex = -1; sidex <= 1; sidex += 2) {
            for (int i = 0; i < 2; i++) {
               double x0 = 128.0 + sidex * (ex + ew * 0.95);
               double y0 = 84.0 + eh * 0.66 + i * 9;
               g.draw(new java.awt.geom.QuadCurve2D.Double(x0, y0, x0 + sidex * 12, y0 - 2.0 + i * 3, x0 + sidex * 24, y0 + i * 6 - 2.0));
            }
         }
      }

      g.setStroke(new BasicStroke(6.0F, 1, 1));
      g.setColor(s.led ? c(s.iris) : ca(s.line, 235));
      double byy = 84.0 - eh * 0.62;
      switch (emo) {
         case "angry":
            for (int sidex = -1; sidex <= 1; sidex += 2) {
               double cxx = 128.0 + sidex * ex;
               g.draw(new java.awt.geom.Line2D.Double(cxx - sidex * ew * 0.55, byy + 12.0, cxx + sidex * ew * 0.4, byy - 6.0));
            }
            break;
         case "sad":
            for (int sidex = -1; sidex <= 1; sidex += 2) {
               double cxx = 128.0 + sidex * ex;
               g.draw(new java.awt.geom.QuadCurve2D.Double(cxx - sidex * ew * 0.45, byy - 6.0, cxx, byy - 2.0, cxx + sidex * ew * 0.4, byy + 8.0));
            }
            break;
         case "curious":
            double cx = 128.0 - ex;
            g.draw(new java.awt.geom.QuadCurve2D.Double(cx - ew * 0.45, byy - 4.0, cx, byy - 20.0, cx + ew * 0.4, byy - 6.0));
            double cx2 = 128.0 + ex;
            g.draw(new java.awt.geom.Line2D.Double(cx2 - ew * 0.35, byy + 6.0, cx2 + ew * 0.35, byy + 4.0));
         case "sleepy":
      }

      double my = 84.0 + eh * 0.66;
      double mx = 128.0;
      g.setStroke(new BasicStroke(4.5F, 1, 1));
      g.setColor(line);
      if (s.mouth != 2) {
         switch (emo) {
            case "happy":
               if (s.mouth == 1) {
                  omega(g, mx, my, 12.0, line);
                  openMouth(g, s, mx, my + 7.0, 14.0, 11.0);
               } else {
                  openMouth(g, s, mx, my, 24.0, 16.0);
               }
               break;
            case "love":
               if (s.mouth == 1) {
                  omega(g, mx, my, 12.0, line);
               } else {
                  g.draw(new java.awt.geom.Arc2D.Double(mx - 11.0, my - 10.0, 22.0, 16.0, 200.0, 140.0, 0));
               }

               g.setColor(ca(16732043, 230));
               g.fill(heart(mx + 30.0, my - 2.0, 16.0));
               break;
            case "sleepy":
               g.setColor(line);
               g.setStroke(new BasicStroke(3.5F));
               g.draw(el(mx, my + 2.0, 9.0, 8.0));
               break;
            case "sad":
               g.draw(new java.awt.geom.QuadCurve2D.Double(mx - 13.0, my + 7.0, mx, my - 6.0, mx + 13.0, my + 7.0));
               break;
            case "excited":
               if (s.mouth == 1) {
                  omega(g, mx, my - 2.0, 12.0, line);
               }

               openMouth(g, s, mx, my + (s.mouth == 1 ? 5 : 0), s.mouth == 1 ? 20.0 : 30.0, s.mouth == 1 ? 16.0 : 22.0);
               break;
            case "angry":
               java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
               p.moveTo(mx - 12.0, my + 6.0);
               p.quadTo(mx - 6.0, my - 1.0, mx, my + 2.0);
               p.quadTo(mx + 6.0, my - 1.0, mx + 12.0, my + 6.0);
               g.draw(p);
               g.setColor(c(16777215));
               java.awt.geom.Path2D.Double f = new java.awt.geom.Path2D.Double();
               f.moveTo(mx + 4.0, my + 1.5);
               f.lineTo(mx + 9.0, my + 3.0);
               f.lineTo(mx + 7.0, my + 9.0);
               f.closePath();
               if (!s.led) {
                  g.fill(f);
               }
               break;
            case "curious":
               g.setColor(s.led ? line : c(6955056));
               g.fill(el(mx + 8.0, my + 3.0, 11.0, 12.0));
               g.setColor(line);
               g.setStroke(new BasicStroke(3.5F));
               g.draw(el(mx + 8.0, my + 3.0, 11.0, 12.0));
               break;
            case "dizzy":
               java.awt.geom.Path2D.Double px = new java.awt.geom.Path2D.Double();

               for (int i = 0; i <= 30; i++) {
                  double x = mx - 20.0 + 40 * i / 30.0;
                  double y = my + 3.0 + Math.sin(i / 30.0 * Math.PI * 3.0) * 4.5;
                  if (i == 0) {
                     px.moveTo(x, y);
                  } else {
                     px.lineTo(x, y);
                  }
               }

               g.draw(px);
               break;
            default:
               g.draw(new java.awt.geom.Arc2D.Double(mx - 11.0, my - 10.0, 22.0, 16.0, 200.0, 140.0, 0));
         }
      } else if ("love".equals(emo)) {
         g.setColor(ca(16732043, 230));
         g.fill(heart(mx + 40.0, my - 6.0, 16.0));
      }

      switch (emo) {
         case "sad": {
            for (int sidex = -1; sidex <= 1; sidex += 2) {
               double tx = 128.0 + sidex * ex + sidex * ew * 0.18;
               double ty = 84.0 + eh * 0.38;
               g.setPaint(new GradientPaint(0.0F, (float)ty, ca(10478591, 220), 0.0F, (float)(ty + 70.0), ca(7325951, 60)));
               g.fill(new java.awt.geom.RoundRectangle2D.Double(tx - 5.0, ty, 10.0, 70.0, 10.0, 10.0));
            }

            double dx = 128.0 + ex + ew * 0.55;
            double dy = 84.0 + eh * 0.8;
            java.awt.geom.Path2D.Double d = new java.awt.geom.Path2D.Double();
            d.moveTo(dx, dy - 12.0);
            d.curveTo(dx + 9.0, dy, dx + 7.0, dy + 9.0, dx, dy + 9.0);
            d.curveTo(dx - 7.0, dy + 9.0, dx - 9.0, dy, dx, dy - 12.0);
            g.setColor(ca(9427967, 235));
            g.fill(d);
            g.setColor(ca(16777215, 230));
            g.fill(el(dx - 2.5, dy + 1.0, 3.5, 5.0));
            break;
         }
         case "sleepy":
            double bx = 128.0 + ex * 0.62;
            double by = 84.0 + eh * 0.72;
            double r = 15.0;
            g.setColor(ca(12578815, 150));
            g.fill(el(bx, by, r * 2.0, r * 2.0));
            g.setStroke(new BasicStroke(2.2F));
            g.setColor(ca(8376309, 220));
            g.draw(el(bx, by, r * 2.0, r * 2.0));
            g.setColor(ca(16777215, 240));
            g.fill(el(bx - 5.0, by - 6.0, 8.0, 6.0));
            break;
         case "angry":
            double vx = 128.0 + ex + ew * 0.62;
            double vy = 26.0;
            g.setStroke(new BasicStroke(5.0F, 1, 1));
            g.setColor(c(16726858));

            for (int k = 0; k < 4; k++) {
               AffineTransform o = g.getTransform();
               g.translate(vx, vy);
               g.rotate((Math.PI / 2) * k);
               g.draw(new java.awt.geom.QuadCurve2D.Double(4.0, -12.0, 4.0, -4.0, 12.0, -4.0));
               g.setTransform(o);
            }
            break;
         case "dizzy": {
            g.setStroke(new BasicStroke(2.4F));
            g.setColor(ca(10476031, 200));
            double dx = 128.0 - ex - ew * 0.7;
            double dy = 84.0 - eh * 0.55;
            java.awt.geom.Path2D.Double d = new java.awt.geom.Path2D.Double();
            d.moveTo(dx, dy - 8.0);
            d.curveTo(dx + 6.0, dy, dx + 5.0, dy + 6.0, dx, dy + 6.0);
            d.curveTo(dx - 5.0, dy + 6.0, dx - 6.0, dy, dx, dy - 8.0);
            g.setColor(ca(10477567, 220));
            g.fill(d);
         }
      }

      g.dispose();
      return im;
   }

   private static void omega(Graphics2D g, double mx, double my, double r, Color line) {
      g.setColor(line);
      g.setStroke(new BasicStroke(4.0F, 1, 1));
      g.draw(new java.awt.geom.Arc2D.Double(mx - r, my - r * 0.9, r, r, 200.0, 160.0, 0));
      g.draw(new java.awt.geom.Arc2D.Double(mx, my - r * 0.9, r, r, 180.0, 160.0, 0));
   }

   private static void openMouth(Graphics2D g, PetTex.St s, double mx, double my, double w, double h) {
      java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
      p.moveTo(mx - w / 2.0, my - h * 0.3);
      p.quadTo(mx, my - h * 0.1, mx + w / 2.0, my - h * 0.3);
      p.curveTo(mx + w * 0.45, my + h * 0.75, mx - w * 0.45, my + h * 0.75, mx - w / 2.0, my - h * 0.3);
      p.closePath();
      if (s.led) {
         g.setColor(c(light(s.iris, 0.3F)));
         g.fill(p);
      } else {
         g.setColor(c(8003636));
         g.fill(p);
         Shape old = g.getClip();
         g.setClip(p);
         g.setColor(c(16744342));
         g.fill(el(mx, my + h * 0.55, w * 0.62, h * 0.7));
         g.setClip(old);
         g.setStroke(new BasicStroke(3.2F, 1, 1));
         g.setColor(c(s.line));
         g.draw(p);
      }
   }

   static {
      FUR.put("cat", new int[]{16229457, 16774374, 14251051});
      FUR.put("shiba", new int[]{15636296, 16775148, 12084010});
      FUR.put("bunny", new int[]{15850694, 16776181, 16758728});
      FUR.put("fox", new int[]{16221754, 16774892, 4860450});
      FUR.put("redpanda", new int[]{13852718, 16774890, 5909018});
      FUR.put("panda", new int[]{16514039, 16777215, 2763315});
      FUR.put("penguin", new int[]{3424874, 16777215, 16756810});
      FUR.put("axolotl", new int[]{16758990, 16770028, 15890335});
      FUR.put("frog", new int[]{8835424, 15858376, 5940548});
      FUR.put("chick", new int[]{16770154, 16774840, 16757820});
      FUR.put("hamster", new int[]{15773796, 16775406, 13205566});
      FUR.put("owl", new int[]{10975311, 15850938, 7227952});
      FUR.put("bee", new int[]{16766023, 16771466, 3811882});
      FUR.put("dragon", new int[]{11440887, 16771504, 8611808});
      FUR.put("unicorn", new int[]{16776191, 16771830, 15913727});
      FUR.put("bear", new int[]{12157006, 15782568, 9067060});
      FUR.put("turtle", new int[]{10476418, 15267520, 7320922});
      FUR.put("bat", new int[]{7232660, 13351142, 4864618});
      FUR.put("fish", new int[]{16747578, 16769208, 16777215});
      FUR.put("mush", new int[]{16773596, 16777215, 15257512});
      FUR.put("cloud", new int[]{16777215, 16777215, 14082815});
      FUR.put("ghost", new int[]{16514303, 16777215, 13227775});
      FUR.put("slime", new int[]{9236648, 13172692, 4173411});
      FUR.put("pdark", new int[]{3026488, 3026488, 3026488});
      FUR.put("wolf", new int[]{9343902, 15329250, 5133150});
      FUR.put("horse", new int[]{10707002, 13208158, 16777215});
      FUR.put("fawn", new int[]{11892554, 16049872, 16775406});
      FUR.put("deer", new int[]{11104586, 15852495, 7227952});
      FUR.put("lion", new int[]{14723429, 16246470, 10117680});
      FUR.put("tiger", new int[]{15763756, 16774890, 2365980});
      FUR.put("retr", new int[]{14920798, 16178086, 13208126});
      FUR.put("afox", new int[]{15988218, 16777215, 12964066});
      FUR.put("eagle", new int[]{5978917, 8016438, 3810326});
      FUR.put("eagleh", new int[]{16184300, 16777215, 14472908});
      FUR.put("drake", new int[]{8150230, 15915424, 5126814});
   }

   static final class St {
      int iris = 4860958;
      int mouth;
      boolean lashes;
      boolean rim;
      boolean led;
      boolean whiskers;
      int line = 3875616;
      float eye = 1.0F;
      float gap = 1.0F;
      int blush = 16743334;
   }
}
