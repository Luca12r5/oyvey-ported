package dev.spotifyhud;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;

public final class Theme {
   public static final String[] IDS = new String[]{"standard", "cover", "glass", "midnight", "purple", "christmas", "sunset", "ocean"};
   public static final String[] NAMES = new String[]{"Standard", "Cover", "Glas", "Mitternacht", "Lila", "Weihnachten", "Sunset", "Ozean"};
   public static final Color GREEN = new Color(30, 215, 96);
   private static BufferedImage blurSrc;
   private static BufferedImage blurOut;
   private static int blurW;
   private static int blurH;
   private static BufferedImage accentSrc;
   private static Color accentVal;

   private Theme() {
   }

   public static int index(String id) {
      for (int i = 0; i < IDS.length; i++) {
         if (IDS[i].equals(id)) {
            return i;
         }
      }

      return 0;
   }

   public static String name(String id) {
      return NAMES[index(id)];
   }

   public static Color accent(String id, BufferedImage art) {
      switch (id) {
         case "cover":
            return art != null ? coverAccent(art) : GREEN;
         case "glass":
            return new Color(255, 255, 255);
         case "midnight":
            return new Color(138, 180, 255);
         case "purple":
            return new Color(214, 140, 255);
         case "christmas":
            return new Color(255, 209, 102);
         case "sunset":
            return new Color(255, 177, 153);
         case "ocean":
            return new Color(79, 209, 197);
         default:
            return GREEN;
      }
   }

   public static void paint(Graphics2D g, String id, Shape panel, double w, double h, BufferedImage art, double px) {
      switch (id) {
         case "cover":
            if (art != null) {
               paintCover(g, panel, w, h, art, px);
               return;
            }

            solid(g, panel, new Color(18, 18, 18, 234));
            return;
         case "glass":
            solid(g, panel, new Color(22, 22, 28, 150));
            g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(255, 255, 255, 38), 0.0F, (float)(h * 0.55), new Color(255, 255, 255, 0)));
            g.fill(panel);
            return;
         case "midnight":
            gradient(g, panel, w, h, new Color(12, 22, 58, 236), new Color(34, 12, 56, 236));
            glow(g, panel, w * 0.85, h * 0.1, w * 0.6, new Color(90, 120, 255, 55));
            return;
         case "purple":
            gradient(g, panel, w, h, new Color(38, 8, 64, 238), new Color(88, 52, 150, 238));
            glow(g, panel, w * 0.15, h * 0.9, w * 0.55, new Color(200, 90, 255, 50));
            return;
         case "christmas":
            gradient(g, panel, w, h, new Color(110, 14, 22, 238), new Color(12, 70, 38, 238));
            glow(g, panel, w * 0.5, 0.0, w * 0.5, new Color(255, 210, 120, 40));
            return;
         case "sunset":
            gradient(g, panel, w, h, new Color(140, 40, 30, 238), new Color(120, 18, 70, 238));
            g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(0, 0, 0, 30), 0.0F, (float)h, new Color(0, 0, 0, 90)));
            g.fill(panel);
            return;
         case "ocean":
            gradient(g, panel, w, h, new Color(10, 30, 40, 238), new Color(26, 70, 88, 238));
            glow(g, panel, w * 0.2, h * 0.1, w * 0.5, new Color(79, 209, 197, 40));
            return;
         default:
            solid(g, panel, new Color(18, 18, 18, 234));
      }
   }

   private static void solid(Graphics2D g, Shape s, Color c) {
      g.setColor(c);
      g.fill(s);
   }

   private static void gradient(Graphics2D g, Shape s, double w, double h, Color a, Color b) {
      g.setPaint(new GradientPaint(0.0F, 0.0F, a, (float)w, (float)h, b));
      g.fill(s);
   }

   private static void glow(Graphics2D g, Shape s, double cx, double cy, double r, Color c) {
      g.setPaint(
         new RadialGradientPaint(new Double(cx, cy), (float)r, new float[]{0.0F, 1.0F}, new Color[]{c, new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)})
      );
      g.fill(s);
   }

   private static void paintCover(Graphics2D g, Shape panel, double w, double h, BufferedImage art, double px) {
      int pw = Math.max(1, (int)Math.round(w * px));
      int ph = Math.max(1, (int)Math.round(h * px));
      if (art != blurSrc || pw != blurW || ph != blurH) {
         blurOut = blurred(art, pw, ph);
         blurSrc = art;
         blurW = pw;
         blurH = ph;
      }

      Shape old = g.getClip();
      g.clip(panel);
      AffineTransform at = new AffineTransform();
      at.scale(w / blurOut.getWidth(), h / blurOut.getHeight());
      g.drawImage(blurOut, at, null);
      g.setClip(old);
      g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(0, 0, 0, 105), (float)w, 0.0F, new Color(0, 0, 0, 150)));
      g.fill(panel);
   }

   private static BufferedImage blurred(BufferedImage src, int pw, int ph) {
      double aspect = (double)pw / ph;
      int sw = src.getWidth();
      int sh = src.getHeight();
      int cw = sw;
      int ch = (int)Math.round(sw / aspect);
      if (ch > sh) {
         ch = sh;
         cw = (int)Math.round(sh * aspect);
      }

      int cx = (sw - cw) / 2;
      int cy = (sh - ch) / 2;
      BufferedImage crop = src.getSubimage(Math.max(0, cx), Math.max(0, cy), Math.max(1, cw), Math.max(1, ch));
      BufferedImage cur = crop;
      int w = crop.getWidth();
      int h = crop.getHeight();
      int th = 5;

      int tw;
      for (tw = Math.max(2, (int)Math.round(th * aspect)); w / 2 >= tw && h / 2 >= th; cur = scale(cur, w, h)) {
         w /= 2;
         h /= 2;
      }

      cur = scale(cur, tw, th);
      int sw2 = tw;

      for (int sh2 = th; sw2 * 2 < pw && sh2 * 2 < ph; cur = scale(cur, sw2, sh2)) {
         sw2 *= 2;
         sh2 *= 2;
      }

      return scale(cur, pw, ph);
   }

   private static BufferedImage scale(BufferedImage src, int w, int h) {
      BufferedImage out = new BufferedImage(Math.max(1, w), Math.max(1, h), 2);
      Graphics2D g = out.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g.drawImage(src, 0, 0, out.getWidth(), out.getHeight(), null);
      g.dispose();
      return out;
   }

   public static Color coverAccent(BufferedImage art) {
      if (art == accentSrc && accentVal != null) {
         return accentVal;
      } else {
         BufferedImage small = scale(art, 24, 24);
         double bestScore = -1.0;
         float[] best = new float[]{0.39F, 0.8F, 0.85F};
         float[] hsb = new float[3];

         for (int y = 0; y < 24; y++) {
            for (int x = 0; x < 24; x++) {
               int c = small.getRGB(x, y);
               Color.RGBtoHSB(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF, hsb);
               double score = hsb[1] * (0.35 + hsb[2]);
               if (!(hsb[2] < 0.18) && score > bestScore) {
                  bestScore = score;
                  best = (float[])hsb.clone();
               }
            }
         }

         Color res;
         if (bestScore < 0.12) {
            res = new Color(235, 235, 235);
         } else {
            res = Color.getHSBColor(best[0], Math.min(0.85F, Math.max(0.45F, best[1])), Math.max(0.85F, best[2]));
         }

         accentSrc = art;
         accentVal = res;
         return res;
      }
   }
}
