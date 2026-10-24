package dev.lego.cosmetic;

import dev.lego.ui.Gx;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D.Double;
import java.awt.geom.Rectangle2D.Float;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Capes {
   static final int W = 512;
   static final int H = 256;
   static final int FW1 = 80;
   static final int FH1 = 128;
   static final int S = 2;
   static final int FW = 160;
   static final int FH = 256;
   static final Map<String, Capes.Spec> SPECS = new LinkedHashMap<>();

   private Capes() {
   }

   static void def(String id, int frames, float fps, int trimHi, int trimLo, int lining, Capes.Painter p) {
      SPECS.put(id, new Capes.Spec(id, Math.max(1, Math.min(16, frames)), frames > 1 ? fps : 0.0F, trimHi, trimLo, lining, p));
   }

   private static Capes.Spec spec(String capeTex) {
      if (capeTex == null) {
         return null;
      } else {
         String s = capeTex.startsWith("cape_") ? capeTex.substring(5) : capeTex;
         int h = s.indexOf(35);
         if (h >= 0) {
            s = s.substring(0, h);
         }

         return SPECS.get(s);
      }
   }

   public static int frames(String capeTex) {
      Capes.Spec s = spec(capeTex);
      return s == null ? 1 : s.frames;
   }

   public static float fps(String capeTex) {
      Capes.Spec s = spec(capeTex);
      return s != null && s.frames > 1 ? s.fps : 0.0F;
   }

   public static String frameName(String capeTex, float time) {
      int n = frames(capeTex);
      if (n <= 1) {
         return capeTex;
      } else {
         int k = Math.floorMod((int)Math.floor(time * fps(capeTex)), n);
         return k == 0 ? capeTex : capeTex + "#" + k;
      }
   }

   static CTex.T paint(String d) {
      int k = 0;
      int h = d.indexOf(35);
      if (h >= 0) {
         try {
            k = Integer.parseInt(d.substring(h + 1));
         } catch (NumberFormatException var15) {
         }

         d = d.substring(0, h);
      }

      Capes.Spec s = SPECS.get(d);
      if (s == null) {
         s = SPECS.get("lego");
      }

      int n = s.frames;
      k = Math.floorMod(k, n);
      double ph = (Math.PI * 2) * k / n;
      BufferedImage f = new BufferedImage(160, 256, 2);
      Graphics2D fg = f.createGraphics();
      Gx.hints(fg);
      fg.setColor(new Color(s.lining));
      fg.fillRect(0, 0, 160, 256);

      try {
         s.p.paint(fg, f, ph, k, n);
      } catch (RuntimeException var14) {
      }

      fg.dispose();
      cloth(f, 0.9);
      trim(f, s);
      BufferedImage front = down(f);
      grain(front, 31);
      BufferedImage li = new BufferedImage(160, 256, 2);
      lining(li, s);
      cloth(li, 0.7);
      trim(li, s);
      BufferedImage inner = down(li);
      grain(inner, 57);
      BufferedImage out = new BufferedImage(512, 256, 2);
      Graphics2D g = out.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g.setColor(new Color(s.lining));
      g.fillRect(0, 0, 512, 256);
      g.drawImage(front, 176, 0, 192, 176, null);
      g.drawImage(front, 192, 16, 80, 160, null);
      g.drawImage(front, 288, 16, 80, 160, null);
      g.drawImage(front, 8, 8, null);
      g.drawImage(inner, 96, 8, null);
      edge(g, 0, 8, 8, 128, s, true);
      edge(g, 88, 8, 8, 128, s, true);
      edge(g, 8, 0, 80, 8, s, false);
      edge(g, 88, 0, 80, 8, s, false);
      g.dispose();
      return new CTex.T(512, 256, out.getRGB(0, 0, 512, 256, null, 0, 512));
   }

   private static void edge(Graphics2D g, int x, int y, int w, int h, Capes.Spec s, boolean vertical) {
      if (vertical) {
         g.setPaint(new GradientPaint(x, 0.0F, new Color(s.trimHi), x + w, 0.0F, new Color(s.trimLo)));
      } else {
         g.setPaint(new GradientPaint(0.0F, y, new Color(s.trimHi), 0.0F, y + h, new Color(s.trimLo)));
      }

      g.fillRect(x, y, w, h);
      g.setColor(new Color(255, 255, 255, 60));
      g.setStroke(new BasicStroke(1.0F, 0, 0, 1.0F, new float[]{3.0F, 2.0F}, 0.0F));
      if (vertical) {
         g.drawLine(x + w / 2, y + 2, x + w / 2, y + h - 2);
      } else {
         g.drawLine(x + 2, y + h / 2, x + w - 2, y + h / 2);
      }

      g.setStroke(new BasicStroke(1.0F));
   }

   private static void lining(BufferedImage img, Capes.Spec s) {
      int[] p = px(img);
      int base = s.lining;
      int hi = mix(base, 16777215, 0.28);
      int lo = mix(base, 0, 0.4);

      for (int y = 0; y < 256; y++) {
         for (int x = 0; x < 160; x++) {
            double u = (x + 0.5) / 160.0;
            double v = (y + 0.5) / 256.0;
            int c = mix(hi, lo, v * 0.9);
            double sheen = Math.exp(-Math.pow((u * 0.8 + v * 0.6 - 0.55) / 0.12, 2.0)) * 0.2;
            c = add(c, 16777215, sheen);
            double qa = Math.abs(((u * 5.0 + v * 3.1) % 1.0 + 1.0) % 1.0 - 0.5);
            double qb = Math.abs(((u * 5.0 - v * 3.1) % 1.0 + 1.0) % 1.0 - 0.5);
            double q = Math.min(qa, qb);
            if (q < 0.045) {
               c = shade(c, 0.78 + q * 4.5);
            } else {
               c = shade(c, 1.04 - q * 0.2);
            }

            p[y * 160 + x] = 0xFF000000 | c;
         }
      }
   }

   static void cloth(BufferedImage img, double amount) {
      int[] p = px(img);
      int w = img.getWidth();
      int h = img.getHeight();

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            double u = (x + 0.5) / w;
            double v = (y + 0.5) / h;
            double fold = 0.055 * Math.sin(u * Math.PI * 2.0 * 2.5 + 0.6 + v * 1.2) * (0.35 + 0.65 * v) + 0.025 * Math.sin(u * Math.PI * 2.0 * 5.5 + 1.3);
            double ex = Math.min(u, 1.0 - u);
            double ey = Math.min(v, 1.0 - v);
            double vig = 1.0 - 0.28 * Math.pow(1.0 - Math.min(1.0, ex / 0.22), 2.0) - 0.22 * Math.pow(1.0 - Math.min(1.0, (1.0 - v) / 0.25), 2.0);
            double top = 1.0 - 0.25 * Math.pow(1.0 - Math.min(1.0, v / 0.09), 2.0);
            double m = 1.0 + (fold + (vig * top - 1.0)) * amount;
            int i = y * w + x;
            p[i] = p[i] & 0xFF000000 | shade(p[i], m) & 16777215;
         }
      }
   }

   static void trim(BufferedImage img, Capes.Spec s) {
      Graphics2D g = img.createGraphics();
      Gx.hints(g);
      int w = img.getWidth();
      int h = img.getHeight();
      float t = 7.0F;

      for (int i = 0; i < 6; i++) {
         g.setColor(new Color(0, 0, 0, 22 - i * 3));
         g.setStroke(new BasicStroke(2.0F));
         g.drawRect((int)t + i, (int)t + i, w - 1 - 2 * ((int)t + i), h - 1 - 2 * ((int)t + i));
      }

      Shape outer = new Float(0.0F, 0.0F, w, h);
      Area band = new Area(outer);
      band.subtract(new Area(new Float(t, t, w - 2.0F * t, h - 2.0F * t)));
      g.setPaint(
         new LinearGradientPaint(
            0.0F,
            0.0F,
            w * 0.6F,
            h,
            new float[]{0.0F, 0.3F, 0.55F, 0.8F, 1.0F},
            new Color[]{
               new Color(s.trimHi), new Color(mix(s.trimHi, s.trimLo, 0.55)), new Color(s.trimHi), new Color(s.trimLo), new Color(mix(s.trimHi, s.trimLo, 0.4))
            }
         )
      );
      g.fill(band);
      g.setColor(new Color(255, 255, 255, 110));
      g.setStroke(new BasicStroke(1.2F));
      g.draw(new Float(1.5F, 1.5F, w - 3, h - 3));
      g.setColor(new Color(0, 0, 0, 120));
      g.draw(new Float(t - 0.6F, t - 0.6F, w - 2.0F * t + 1.2F, h - 2.0F * t + 1.2F));
      g.setColor(new Color(s.trimHi & 16777215 | 1593835520, true));
      g.setStroke(new BasicStroke(1.3F));
      g.draw(new Float(t + 4.0F, t + 4.0F, w - 2.0F * t - 8.0F, h - 2.0F * t - 8.0F));
      g.setPaint(new GradientPaint(0.0F, t, new Color(0, 0, 0, 70), 0.0F, t + 16.0F, new Color(0, 0, 0, 0)));
      g.fillRect((int)t, (int)t, w - 2 * (int)t, 16);
      g.dispose();
   }

   static BufferedImage down(BufferedImage src) {
      int w = src.getWidth() / 2;
      int h = src.getHeight() / 2;
      int sw = src.getWidth();
      int[] a = px(src);
      BufferedImage out = new BufferedImage(w, h, 2);
      int[] o = px(out);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int i = y * 2 * sw + x * 2;
            int c0 = a[i];
            int c1 = a[i + 1];
            int c2 = a[i + sw];
            int c3 = a[i + sw + 1];
            int r = 0;
            int gg = 0;
            int b = 0;
            int al = 0;

            for (int c : new int[]{c0, c1, c2, c3}) {
               al += c >>> 24;
               r += c >> 16 & 0xFF;
               gg += c >> 8 & 0xFF;
               b += c & 0xFF;
            }

            o[y * w + x] = al / 4 << 24 | r / 4 << 16 | gg / 4 << 8 | b / 4;
         }
      }

      return out;
   }

   static void grain(BufferedImage img, int seed) {
      int[] p = px(img);
      int w = img.getWidth();
      int h = img.getHeight();

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            double n = (hash(x, y, seed) - 0.5) * 0.05 + ((x + y) % 3 == 0 ? -0.015 : 0.006);
            int i = y * w + x;
            p[i] = p[i] & 0xFF000000 | shade(p[i], 1.0 + n) & 16777215;
         }
      }
   }

   static int[] px(BufferedImage im) {
      return ((DataBufferInt)im.getRaster().getDataBuffer()).getData();
   }

   static void field(BufferedImage im, Capes.PF f) {
      int w = im.getWidth();
      int h = im.getHeight();
      int[] p = px(im);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            p[y * w + x] = 0xFF000000 | f.at((x + 0.5) / w, (y + 0.5) / h, x, y);
         }
      }
   }

   static double hash(int x, int y, int s) {
      int h = x * 374761393 + y * 668265263 + s * 1442695041;
      h = (h ^ h >>> 13) * 1274126177;
      h ^= h >>> 16;
      return (h & 16777215) / 1.6777216E7;
   }

   static double hash1(int i, int s) {
      return hash(i, i * 31 + 7, s);
   }

   static double vn(double x, double y, int px, int py, int s) {
      int xi = (int)Math.floor(x);
      int yi = (int)Math.floor(y);
      double fx = x - xi;
      double fy = y - yi;
      fx = fx * fx * fx * (fx * (fx * 6.0 - 15.0) + 10.0);
      fy = fy * fy * fy * (fy * (fy * 6.0 - 15.0) + 10.0);
      int x0 = px > 0 ? Math.floorMod(xi, px) : xi;
      int x1 = px > 0 ? Math.floorMod(xi + 1, px) : xi + 1;
      int y0 = py > 0 ? Math.floorMod(yi, py) : yi;
      int y1 = py > 0 ? Math.floorMod(yi + 1, py) : yi + 1;
      double a = hash(x0, y0, s);
      double b = hash(x1, y0, s);
      double c = hash(x0, y1, s);
      double d = hash(x1, y1, s);
      return a + (b - a) * fx + (c - a) * fy + (a - b - c + d) * fx * fy;
   }

   static double fbm(double x, double y, int px, int py, int oct, int s) {
      double sum = 0.0;
      double amp = 0.5;
      double norm = 0.0;
      double f = 1.0;

      for (int o = 0; o < oct; o++) {
         sum += amp * vn(x * f, y * f, px * (int)f, py * (int)f, s + o * 17);
         norm += amp;
         amp *= 0.5;
         f *= 2.0;
      }

      return sum / norm;
   }

   static double clamp01(double v) {
      return v < 0.0 ? 0.0 : (v > 1.0 ? 1.0 : v);
   }

   static double smooth(double a, double b, double v) {
      double t = clamp01((v - a) / (b - a));
      return t * t * (3.0 - 2.0 * t);
   }

   static int mix(int a, int b, double t) {
      t = clamp01(t);
      int ar = a >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int ab = a & 0xFF;
      int br = b >> 16 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int bb = b & 0xFF;
      return (int)(ar + (br - ar) * t) << 16 | (int)(ag + (bg - ag) * t) << 8 | (int)(ab + (bb - ab) * t);
   }

   static int ramp(double t, int... c) {
      t = clamp01(t) * (c.length - 1);
      int i = Math.min(c.length - 2, (int)t);
      return mix(c[i], c[i + 1], t - i);
   }

   static int shade(int c, double m) {
      int r = (int)Math.min(255.0, Math.max(0.0, (c >> 16 & 0xFF) * m));
      int g = (int)Math.min(255.0, Math.max(0.0, (c >> 8 & 0xFF) * m));
      int b = (int)Math.min(255.0, Math.max(0.0, (c & 0xFF) * m));
      return c & 0xFF000000 | r << 16 | g << 8 | b;
   }

   static int add(int c, int rgb, double a) {
      if (a <= 0.0) {
         return c;
      } else {
         int r = (int)Math.min(255.0, (c >> 16 & 0xFF) + (rgb >> 16 & 0xFF) * a);
         int g = (int)Math.min(255.0, (c >> 8 & 0xFF) + (rgb >> 8 & 0xFF) * a);
         int b = (int)Math.min(255.0, (c & 0xFF) + (rgb & 0xFF) * a);
         return c & 0xFF000000 | r << 16 | g << 8 | b;
      }
   }

   static int over(int c, int rgb, double a) {
      return c & 0xFF000000 | mix(c, rgb, a);
   }

   static int hsv(double h, double s, double v) {
      return Color.HSBtoRGB((float)(h - Math.floor(h)), (float)clamp01(s), (float)clamp01(v)) & 16777215;
   }

   static Color col(int rgb, double a) {
      return new Color(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, (int)Math.round(clamp01(a) * 255.0));
   }

   static void dot(Graphics2D g, double x, double y, double r, int rgb, double a) {
      if (!(r <= 0.05) && !(a <= 0.004)) {
         g.setPaint(
            new RadialGradientPaint((float)x, (float)y, (float)r, new float[]{0.0F, 0.25F, 1.0F}, new Color[]{col(rgb, a), col(rgb, a * 0.45), col(rgb, 0.0)})
         );
         g.fill(new Double(x - r, y - r, r * 2.0, r * 2.0));
      }
   }

   static void star(Graphics2D g, double x, double y, double r, int rgb, double a) {
      if (!(a <= 0.01)) {
         dot(g, x, y, r * 2.2, rgb, a * 0.55);
         dot(g, x, y, r * 0.9, 16777215, a);
         if (r > 1.6) {
            g.setStroke(new BasicStroke((float)Math.max(0.7, r * 0.22), 1, 1));
            g.setPaint(new GradientPaint((float)(x - r * 3.2), (float)y, col(rgb, 0.0), (float)x, (float)y, col(16777215, a * 0.9), true));
            g.draw(new java.awt.geom.Line2D.Double(x - r * 3.2, y, x + r * 3.2, y));
            g.setPaint(new GradientPaint((float)x, (float)(y - r * 3.2), col(rgb, 0.0), (float)x, (float)y, col(16777215, a * 0.9), true));
            g.draw(new java.awt.geom.Line2D.Double(x, y - r * 3.2, x, y + r * 3.2));
         }
      }
   }

   static void glowStroke(Graphics2D g, Shape s, int rgb, float core, float spread, double a) {
      for (int i = 5; i >= 1; i--) {
         g.setColor(col(rgb, a * 0.12 * (6 - i) / 5.0 + 0.02));
         g.setStroke(new BasicStroke(core + spread * i, 1, 1));
         g.draw(s);
      }

      g.setColor(col(rgb, a));
      g.setStroke(new BasicStroke(core, 1, 1));
      g.draw(s);
      g.setColor(col(mix(rgb, 16777215, 0.7), a));
      g.setStroke(new BasicStroke(Math.max(0.6F, core * 0.4F), 1, 1));
      g.draw(s);
   }

   static void sweep(BufferedImage img, double t, double width, double strength) {
      int[] p = px(img);
      int w = img.getWidth();
      int h = img.getHeight();
      double pos = -0.5 + t * 2.2;

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            double u = (x + 0.5) / w;
            double v = (y + 0.5) / h;
            double d = u * 0.7 + v * 0.45 - pos;
            double a = Math.exp(-(d * d) / (width * width)) * strength;
            if (a > 0.004) {
               p[y * w + x] = add(p[y * w + x], 16777215, a);
            }
         }
      }
   }

   static void addDot(BufferedImage img, double cx, double cy, double r, int rgb, double a) {
      if (!(r <= 0.1) && !(a <= 0.003)) {
         int[] p = px(img);
         int w = img.getWidth();
         int h = img.getHeight();
         int x0 = Math.max(0, (int)(cx - r * 2.0));
         int x1 = Math.min(w - 1, (int)(cx + r * 2.0));
         int y0 = Math.max(0, (int)(cy - r * 2.0));
         int y1 = Math.min(h - 1, (int)(cy + r * 2.0));
         double k = 1.0 / (r * r * 0.5);

         for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
               double dx = x + 0.5 - cx;
               double dy = y + 0.5 - cy;
               double f = Math.exp(-(dx * dx + dy * dy) * k) * a;
               if (f > 0.003) {
                  p[y * w + x] = add(p[y * w + x], rgb, f);
               }
            }
         }
      }
   }

   static {
      CapeArt.defineAll();
      CapeArt2.defineAll();
      Cos3Capes.defineAll();
      XmasCapes.defineAll();
      CapePack.defineAll();
   }

   interface PF {
      int at(double var1, double var3, int var5, int var6);
   }

   interface Painter {
      void paint(Graphics2D var1, BufferedImage var2, double var3, int var5, int var6);
   }

   static final class Spec {
      final String id;
      final int frames;
      final float fps;
      final int trimHi;
      final int trimLo;
      final int lining;
      final Capes.Painter p;

      Spec(String id, int frames, float fps, int trimHi, int trimLo, int lining, Capes.Painter p) {
         this.id = id;
         this.frames = frames;
         this.fps = fps;
         this.trimHi = trimHi;
         this.trimLo = trimLo;
         this.lining = lining;
         this.p = p;
      }
   }
}
