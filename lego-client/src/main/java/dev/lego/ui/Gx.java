package dev.lego.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Gx {
   public static Gx.Backend B;
   public static int S = 1;
   private static float alpha = 1.0F;
   private static final ArrayDeque<Float> ALPHA = new ArrayDeque<>();
   private static long lastNs = 0L;
   public static float dt = 0.016F;
   private static long frameNs;
   private static boolean frameHooked;
   private static final Map<String, Gx.Img> CACHE = new HashMap<>();
   private static long lastClean = 0L;
   private static final Map<Integer, Gx.Atlas> ATLAS = new HashMap<>();

   private Gx() {
   }

   public static void begin(Gx.Backend b) {
      B = b;
      S = Math.max(1, b.guiScale());
      B.push();
      B.scale(1.0F / S, 1.0F / S);
      long now = System.nanoTime();
      if (!frameHooked || now - frameNs > 250000000L) {
         if (lastNs == 0L) {
            dt = 0.016F;
         } else if (now - lastNs > 2000000L) {
            dt = Math.max(0.001F, Math.min(0.1F, (float)(now - lastNs) / 1.0E9F));
         }

         if (lastNs == 0L || now - lastNs > 2000000L) {
            lastNs = now;
         }
      }

      alpha = 1.0F;
      ALPHA.clear();
      cleanup();
   }

   public static void newFrame() {
      long now = System.nanoTime();
      dt = frameNs == 0L ? 0.016F : Math.max(5.0E-4F, Math.min(0.1F, (float)(now - frameNs) / 1.0E9F));
      frameNs = now;
      frameHooked = true;
   }

   public static void end() {
      try {
         B.pop();
      } catch (Throwable var1) {
      }
   }

   public static void push() {
      B.push();
   }

   public static void pop() {
      B.pop();
   }

   public static void translate(float x, float y) {
      B.translate(Math.round(x), Math.round(y));
   }

   public static void translateExact(float x, float y) {
      B.translate(x, y);
   }

   public static void scale(float s) {
      B.scale(s, s);
   }

   public static void scaleAt(float cx, float cy, float s) {
      translateExact(cx, cy);
      scale(s);
      translateExact(-cx, -cy);
   }

   public static void pushAlpha(float a) {
      ALPHA.push(alpha);
      alpha = alpha * Math.max(0.0F, Math.min(1.0F, a));
   }

   public static void popAlpha() {
      alpha = ALPHA.isEmpty() ? 1.0F : ALPHA.pop();
   }

   public static float alpha() {
      return alpha;
   }

   public static int col(int argb) {
      if (alpha >= 0.999F) {
         return argb;
      } else {
         int a = (int)((argb >>> 24 & 0xFF) * alpha);
         return a << 24 | argb & 16777215;
      }
   }

   public static int withAlpha(int argb, float a) {
      int al = (int)Math.max(0.0F, Math.min(255.0F, (argb >>> 24 & 0xFF) * a));
      return al << 24 | argb & 16777215;
   }

   public static int rgba(int rgb, float a) {
      return (int)Math.max(0.0F, Math.min(255.0F, a * 255.0F)) << 24 | rgb & 16777215;
   }

   public static int mix(int a, int b, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int aa = a >>> 24;
      int ar = a >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int ab = a & 0xFF;
      int ba = b >>> 24;
      int br = b >> 16 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int bb = b & 0xFF;
      return (int)(aa + (ba - aa) * t) << 24 | (int)(ar + (br - ar) * t) << 16 | (int)(ag + (bg - ag) * t) << 8 | (int)(ab + (bb - ab) * t);
   }

   public static void clip(int x, int y, int w, int h) {
      B.clip(x, y, w, h);
   }

   public static void unclip() {
      B.unclip();
   }

   public static void fill(int x, int y, int w, int h, int argb) {
      if (w > 0 && h > 0) {
         B.fill(x, y, w, h, col(argb));
      }
   }

   public static void gradientV(int x, int y, int w, int h, int top, int bottom) {
      if (w > 0 && h > 0) {
         B.gradV(x, y, w, h, col(top), col(bottom));
      }
   }

   private static void tex(Gx.Img t, int x, int y, int w, int h, int u, int v, int rw, int rh, int argb) {
      if (w > 0 && h > 0) {
         B.tex(t, x, y, w, h, u, v, rw, rh, col(argb));
      }
   }

   public static void image(Gx.Img t, int x, int y, int w, int h, int argb) {
      if (t != null) {
         tex(t, x, y, w, h, 0, 0, t.w, t.h, argb);
      }
   }

   /** Corner rounding of the whole UI (UiSettings "Rundung"; 1 = as designed). */
   public static float round = 1.4F;

   public static int rounder(int r) {
      return r <= 0 ? r : Math.round(r * round);
   }

   static int bucket(int r) {
      return r <= 24 ? r : (r + 2) / 4 * 4;
   }

   public static void rect(int x, int y, int w, int h, int r, int argb) {
      if (w > 0 && h > 0) {
         r = rounder(r);
         r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
         if (r <= 0) {
            fill(x, y, w, h, argb);
         } else {
            int b = bucket(r);
            Gx.Img c = circle(b);
            tex(c, x, y, r, r, 0, 0, b, b, argb);
            tex(c, x + w - r, y, r, r, b, 0, b, b, argb);
            tex(c, x, y + h - r, r, r, 0, b, b, b, argb);
            tex(c, x + w - r, y + h - r, r, r, b, b, b, b, argb);
            fill(x + r, y, w - 2 * r, r, argb);
            fill(x, y + r, w, h - 2 * r, argb);
            fill(x + r, y + h - r, w - 2 * r, r, argb);
         }
      }
   }

   public static void rectV(int x, int y, int w, int h, int r, int top, int bottom) {
      if (w > 0 && h > 0) {
         r = rounder(r);
         r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
         if (r <= 0) {
            gradientV(x, y, w, h, top, bottom);
         } else {
            float k = (float)r / h;
            int midTop = mix(top, bottom, k);
            int midBot = mix(top, bottom, 1.0F - k);
            int b = bucket(r);
            Gx.Img c = circle(b);
            tex(c, x, y, r, r, 0, 0, b, b, top);
            tex(c, x + w - r, y, r, r, b, 0, b, b, top);
            tex(c, x, y + h - r, r, r, 0, b, b, b, bottom);
            tex(c, x + w - r, y + h - r, r, r, b, b, b, b, bottom);
            B.gradV(x + r, y, w - 2 * r, r, col(top), col(midTop));
            B.gradV(x, y + r, w, h - 2 * r, col(midTop), col(midBot));
            B.gradV(x + r, y + h - r, w - 2 * r, r, col(midBot), col(bottom));
         }
      }
   }

   public static void outline(int x, int y, int w, int h, int r, int t, int argb) {
      if (w > 0 && h > 0 && t > 0) {
         r = rounder(r);
         r = Math.max(t, Math.min(r, Math.min(w, h) / 2));
         Gx.Img c = ring(r, t);
         tex(c, x, y, r, r, 0, 0, r, r, argb);
         tex(c, x + w - r, y, r, r, r, 0, r, r, argb);
         tex(c, x, y + h - r, r, r, 0, r, r, r, argb);
         tex(c, x + w - r, y + h - r, r, r, r, r, r, r, argb);
         fill(x + r, y, w - 2 * r, t, argb);
         fill(x + r, y + h - t, w - 2 * r, t, argb);
         fill(x, y + r, t, h - 2 * r, argb);
         fill(x + w - t, y + r, t, h - 2 * r, argb);
      }
   }

   public static void shadow(int x, int y, int w, int h, int r, int blur, float strength) {
      if (w > 0 && h > 0 && blur > 0) {
         Gx.Img s = shadowTex(r, blur);
         int c = s.w / 2;
         int argb = rgba(0, strength);
         int X = x - blur * 2;
         int Y = y - blur * 2 + blur / 2;
         int W = w + blur * 4;
         int H = h + blur * 4;
         if (W >= 2 * c && H >= 2 * c) {
            tex(s, X, Y, c, c, 0, 0, c, c, argb);
            tex(s, X + W - c, Y, c, c, c + 1, 0, c, c, argb);
            tex(s, X, Y + H - c, c, c, 0, c + 1, c, c, argb);
            tex(s, X + W - c, Y + H - c, c, c, c + 1, c + 1, c, c, argb);
            tex(s, X + c, Y, W - 2 * c, c, c, 0, 1, c, argb);
            tex(s, X + c, Y + H - c, W - 2 * c, c, c, c + 1, 1, c, argb);
            tex(s, X, Y + c, c, H - 2 * c, 0, c, c, 1, argb);
            tex(s, X + W - c, Y + c, c, H - 2 * c, c + 1, c, c, 1, argb);
            tex(s, X + c, Y + c, W - 2 * c, H - 2 * c, c, c, 1, 1, argb);
         } else {
            image(s, X, Y, W, H, argb);
         }
      }
   }

   public static void glow(int cx, int cy, int radius, int argb) {
      if (radius > 0) {
         Gx.Img g = get("glow", 128, () -> {
            int d = 128;
            int[] px = new int[d * d];

            for (int y = 0; y < d; y++) {
               for (int x = 0; x < d; x++) {
                  double dx = (x + 0.5 - d / 2.0) / (d / 2.0);
                  double dy = (y + 0.5 - d / 2.0) / (d / 2.0);
                  double r = Math.sqrt(dx * dx + dy * dy);
                  double a = r >= 1.0 ? 0.0 : Math.pow(1.0 - r, 2.2);
                  px[y * d + x] = (int)(a * 255.0) << 24 | 16777215;
               }
            }

            return new int[][]{px, {d, d}};
         });
         image(g, cx - radius, cy - radius, radius * 2, radius * 2, argb);
      }
   }

   public static void circle(int cx, int cy, int r, int argb) {
      if (r > 0) {
         Gx.Img c = circle(bucket(r));
         image(c, cx - r, cy - r, 2 * r, 2 * r, argb);
      }
   }

   public static void ringAt(int cx, int cy, int r, int t, int argb) {
      if (r > 0) {
         int b = bucket(r);
         Gx.Img c = ring(b, Math.max(1, Math.round((float)(Math.min(t, r) * b) / r)));
         image(c, cx - r, cy - r, 2 * r, 2 * r, argb);
      }
   }

   public static float text(String s, float x, float y, float size, int weight, int argb) {
      if (s != null && !s.isEmpty() && !(size <= 1.0F)) {
         Gx.Atlas at = atlas(weight, size);
         int n = s.length();
         Gx.Glyph[] gl = new Gx.Glyph[n];

         for (int i = 0; i < n; i++) {
            gl[i] = at.glyph(s.charAt(i));
         }

         at.flush();
         int c = col(argb);
         int base = Math.round(y + at.asc);
         float pen = x;

         for (int i = 0; i < n; i++) {
            Gx.Glyph g = gl[i];
            if (g.w > 0) {
               B.tex(g.page.img, Math.round(pen) + g.ox, base + g.oy, g.w, g.h, g.u, g.v, g.w, g.h, c);
            }

            pen += g.adv;
         }

         return pen - x;
      } else {
         return 0.0F;
      }
   }

   public static float textMid(String s, float x, float cy, float size, int weight, int argb) {
      return text(s, x, topFor(cy, size, weight), size, weight, argb);
   }

   public static float textCenter(String s, float cx, float cy, float size, int weight, int argb) {
      float w = width(s, size, weight);
      return text(s, cx - w / 2.0F, topFor(cy, size, weight), size, weight, argb);
   }

   public static float textRight(String s, float rx, float cy, float size, int weight, int argb) {
      float w = width(s, size, weight);
      return text(s, rx - w, topFor(cy, size, weight), size, weight, argb);
   }

   public static float width(String s, float size, int weight) {
      if (s != null && !s.isEmpty()) {
         Gx.Atlas at = atlas(weight, size);
         float w = 0.0F;

         for (int i = 0; i < s.length(); i++) {
            w += at.advance(s.charAt(i));
         }

         return w;
      } else {
         return 0.0F;
      }
   }

   public static void textSpaced(String s, float cx, float cy, float size, int weight, int argb, float spacing) {
      float total = 0.0F;

      for (int i = 0; i < s.length(); i++) {
         total += width(String.valueOf(s.charAt(i)), size, weight) + (i < s.length() - 1 ? spacing : 0.0F);
      }

      float x = cx - total / 2.0F;
      float top = topFor(cy, size, weight);

      for (int i = 0; i < s.length(); i++) {
         String c = String.valueOf(s.charAt(i));
         if (!c.equals(" ")) {
            text(c, x, top, size, weight, argb);
         }

         x += width(c, size, weight) + spacing;
      }
   }

   public static float spacedWidth(String s, float size, int weight, float spacing) {
      float total = 0.0F;

      for (int i = 0; i < s.length(); i++) {
         total += width(String.valueOf(s.charAt(i)), size, weight) + (i < s.length() - 1 ? spacing : 0.0F);
      }

      return total;
   }

   public static float topFor(float cy, float size, int weight) {
      float asc = Fonts.ascent(weight, size);
      float cap = size * 0.7F;
      return cy + cap / 2.0F - asc;
   }

   public static void icon(String name, float cx, float cy, float size, int argb) {
      int want = Math.max(4, Math.round(size));
      int s = want <= 20 ? want : (want + 2) / 4 * 4;
      Gx.Img t = get("icon:" + name + ":" + s, s + 4, () -> {
         int d = s + 4;
         BufferedImage img = new BufferedImage(d, d, 2);
         Graphics2D g = img.createGraphics();
         hints(g);
         g.translate(2, 2);
         Icons.paint(g, name, s, Color.WHITE);
         g.dispose();
         return new int[][]{whiten(img.getRGB(0, 0, d, d, null, 0, d)), {d, d}};
      });
      int dw = Math.round((float)(t.w * want) / s);
      tex(t, Math.round(cx - dw / 2.0F), Math.round(cy - dw / 2.0F), dw, dw, 0, 0, t.w, t.h, argb);
   }

   public static Gx.Img get(String key, int hint, Supplier<int[][]> painter) {
      Gx.Img t = CACHE.get(key);
      if (t == null) {
         int[][] r = painter.get();
         t = new Gx.Img(B.register(r[0], r[1][0], r[1][1]), r[1][0], r[1][1], r.length > 2 ? r[2][0] : 0);
         CACHE.put(key, t);
      }

      t.used = System.currentTimeMillis();
      return t;
   }

   public static Gx.Img peek(String key) {
      Gx.Img t = CACHE.get(key);
      if (t != null) {
         t.used = System.currentTimeMillis();
      }

      return t;
   }

   public static Gx.Img painted(String key, int w, int h, Consumer<Graphics2D> paint) {
      return get("p:" + key + ":" + w + "x" + h, 0, () -> {
         BufferedImage img = new BufferedImage(Math.max(1, w), Math.max(1, h), 2);
         Graphics2D g = img.createGraphics();
         hints(g);

         try {
            paint.accept(g);
         } finally {
            g.dispose();
         }

         return new int[][]{img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth()), {img.getWidth(), img.getHeight()}};
      });
   }

   private static void cleanup() {
      long now = System.currentTimeMillis();
      if (now - lastClean >= 5000L) {
         lastClean = now;
         Iterator<Entry<String, Gx.Img>> it = CACHE.entrySet().iterator();

         while (it.hasNext()) {
            Entry<String, Gx.Img> e = it.next();
            long age = now - e.getValue().used;
            boolean small = e.getKey().startsWith("t:");
            if (age > (small ? 20000 : '\uea60')) {
               B.destroy(e.getValue());
               it.remove();
            }
         }
      }
   }

   public static void clearCache() {
      for (Gx.Img i : CACHE.values()) {
         B.destroy(i);
      }

      CACHE.clear();
   }

   private static Gx.Img circle(int r) {
      return get("circle:" + r, 0, () -> {
         int d = 2 * r;
         int[] px = new int[d * d];

         for (int y = 0; y < d; y++) {
            for (int x = 0; x < d; x++) {
               int cov = 0;

               for (int sy = 0; sy < 4; sy++) {
                  for (int sx = 0; sx < 4; sx++) {
                     double dx = x + (sx + 0.5) / 4.0 - r;
                     double dy = y + (sy + 0.5) / 4.0 - r;
                     if (dx * dx + dy * dy <= (double)r * r) {
                        cov++;
                     }
                  }
               }

               px[y * d + x] = cov * 255 / 16 << 24 | 16777215;
            }
         }

         return new int[][]{px, {d, d}};
      });
   }

   private static Gx.Img ring(int r, int t) {
      return get("ring:" + r + ":" + t, 0, () -> {
         int d = 2 * r;
         int[] px = new int[d * d];
         double ri = r - t;

         for (int y = 0; y < d; y++) {
            for (int x = 0; x < d; x++) {
               int cov = 0;

               for (int sy = 0; sy < 4; sy++) {
                  for (int sx = 0; sx < 4; sx++) {
                     double dx = x + (sx + 0.5) / 4.0 - r;
                     double dy = y + (sy + 0.5) / 4.0 - r;
                     double dd = dx * dx + dy * dy;
                     if (dd <= (double)r * r && dd >= ri * ri) {
                        cov++;
                     }
                  }
               }

               px[y * d + x] = cov * 255 / 16 << 24 | 16777215;
            }
         }

         return new int[][]{px, {d, d}};
      });
   }

   private static Gx.Img shadowTex(int r, int blur) {
      return get("shadow:" + r + ":" + blur, 0, () -> {
         int c = r + blur * 2;
         int d = 2 * c + 1;
         float[] a = new float[d * d];
         int inset = blur * 2;

         for (int y = 0; y < d; y++) {
            for (int x = 0; x < d; x++) {
               double qx = Math.max(Math.abs(x + 0.5 - d / 2.0) - (d / 2.0 - inset - r), 0.0);
               double qy = Math.max(Math.abs(y + 0.5 - d / 2.0) - (d / 2.0 - inset - r), 0.0);
               a[y * d + x] = Math.sqrt(qx * qx + qy * qy) <= r ? 1.0F : 0.0F;
            }
         }

         for (int pass = 0; pass < 3; pass++) {
            a = boxBlur(a, d, Math.max(1, blur / 2));
         }

         int[] px = new int[d * d];

         for (int i = 0; i < px.length; i++) {
            px[i] = (int)(Math.min(1.0F, a[i]) * 255.0F) << 24;
         }

         return new int[][]{px, {d, d}};
      });
   }

   private static float[] boxBlur(float[] src, int d, int rad) {
      float[] tmp = new float[d * d];
      float[] out = new float[d * d];

      for (int y = 0; y < d; y++) {
         for (int x = 0; x < d; x++) {
            float s = 0.0F;
            int n = 0;

            for (int k = -rad; k <= rad; k++) {
               int xx = x + k;
               if (xx >= 0 && xx < d) {
                  s += src[y * d + xx];
               }

               n++;
            }

            tmp[y * d + x] = s / n;
         }
      }

      for (int y = 0; y < d; y++) {
         for (int x = 0; x < d; x++) {
            float s = 0.0F;
            int n = 0;

            for (int k = -rad; k <= rad; k++) {
               int yy = y + k;
               if (yy >= 0 && yy < d) {
                  s += tmp[yy * d + x];
               }

               n++;
            }

            out[y * d + x] = s / n;
         }
      }

      return out;
   }

   static Gx.Atlas atlas(int weight, float size) {
      int px = Math.max(4, Math.min(400, Math.round(size)));
      int key = weight * 1000 + px;
      Gx.Atlas a = ATLAS.get(key);
      if (a == null) {
         a = new Gx.Atlas(weight, px);
         ATLAS.put(key, a);
      }

      return a;
   }

   private static int[] whiten(int[] px) {
      for (int i = 0; i < px.length; i++) {
         px[i] = px[i] & 0xFF000000 | 16777215;
      }

      return px;
   }

   public static void hints(Graphics2D g) {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
   }

   static final class Atlas {
      final Font font;
      final int px;
      final int weight;
      final float asc;
      final List<Gx.Page> pages = new ArrayList<>();
      final Map<Character, Gx.Glyph> glyphs = new HashMap<>();
      final Map<Character, Float> adv = new HashMap<>();

      Atlas(int weight, int px) {
         this.weight = weight;
         this.px = px;
         this.font = Fonts.get(weight, px);
         this.asc = Fonts.ascent(weight, px);
      }

      private Font fontFor(char c) {
         return this.font.canDisplay(c) ? this.font : new Font("Dialog", this.weight >= 3 ? 1 : 0, this.px);
      }

      float advance(char c) {
         Gx.Glyph g = this.glyphs.get(c);
         if (g != null) {
            return g.adv;
         } else {
            Float a = this.adv.get(c);
            if (a == null) {
               GlyphVector gv = this.fontFor(c).createGlyphVector(Fonts.FRC, String.valueOf(c));
               a = gv.getGlyphMetrics(0).getAdvance();
               this.adv.put(c, a);
            }

            return a;
         }
      }

      Gx.Glyph glyph(char c) {
         Gx.Glyph g = this.glyphs.get(c);
         if (g != null) {
            return g;
         } else {
            g = new Gx.Glyph();
            Font f = this.fontFor(c);
            GlyphVector gv = f.createGlyphVector(Fonts.FRC, String.valueOf(c));
            g.adv = gv.getGlyphMetrics(0).getAdvance();
            Rectangle pb = gv.getPixelBounds(Fonts.FRC, 0.0F, 0.0F);
            if (pb.width > 0 && pb.height > 0 && !Character.isWhitespace(c)) {
               int w = pb.width + 4;
               int h = pb.height + 4;
               BufferedImage img = new BufferedImage(w, h, 2);
               Graphics2D gr = img.createGraphics();
               Gx.hints(gr);
               gr.setColor(Color.WHITE);
               gr.drawGlyphVector(gv, 2 - pb.x, 2 - pb.y);
               gr.dispose();
               Gx.Page p = this.place(w, h);
               g.page = p;
               g.u = p.cx;
               g.v = p.cy;
               g.w = w;
               g.h = h;
               g.ox = pb.x - 2;
               g.oy = pb.y - 2;
               int[] src = img.getRGB(0, 0, w, h, null, 0, w);

               for (int yy = 0; yy < h; yy++) {
                  for (int xx = 0; xx < w; xx++) {
                     p.px[(p.cy + yy) * p.size + p.cx + xx] = src[yy * w + xx] & 0xFF000000 | 16777215;
                  }
               }

               p.cx += w + 1;
               p.rowH = Math.max(p.rowH, h);
               p.dirty = true;
            }

            this.glyphs.put(c, g);
            return g;
         }
      }

      private Gx.Page place(int w, int h) {
         Gx.Page p = this.pages.isEmpty() ? null : this.pages.get(this.pages.size() - 1);
         if (p != null && p.cx + w + 1 > p.size) {
            p.cx = 1;
            p.cy = p.cy + p.rowH + 1;
            p.rowH = 0;
         }

         if (p == null || p.cy + h + 1 > p.size) {
            int size = this.px <= 18 ? 256 : (this.px <= 36 ? 512 : (this.px <= 72 ? 1024 : 2048));

            while (size < Math.max(w, h) + 2) {
               size *= 2;
            }

            p = new Gx.Page(size);
            this.pages.add(p);
         }

         return p;
      }

      void flush() {
         for (Gx.Page p : this.pages) {
            if (p.img == null) {
               p.img = new Gx.Img(Gx.B.register(p.px, p.size, p.size), p.size, p.size, 0);
               p.dirty = false;
            } else if (p.dirty) {
               Gx.B.update(p.img, p.px);
               p.dirty = false;
            }
         }
      }
   }

   public interface Backend {
      int guiScale();

      void push();

      void pop();

      void translate(float var1, float var2);

      void scale(float var1, float var2);

      void fill(int var1, int var2, int var3, int var4, int var5);

      void gradV(int var1, int var2, int var3, int var4, int var5, int var6);

      void tex(Gx.Img var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10);

      void clip(int var1, int var2, int var3, int var4);

      void unclip();

      Object register(int[] var1, int var2, int var3);

      void update(Gx.Img var1, int[] var2);

      void destroy(Gx.Img var1);

      int[] screenPx();
   }

   static final class Glyph {
      Gx.Page page;
      int u;
      int v;
      int w;
      int h;
      int ox;
      int oy;
      float adv;
   }

   public static final class Img {
      public final Object handle;
      public final int w;
      public final int h;
      final int pad;
      public long used;

      Img(Object handle, int w, int h, int pad) {
         this.handle = handle;
         this.w = w;
         this.h = h;
         this.pad = pad;
      }
   }

   static final class Page {
      final int size;
      final int[] px;
      Gx.Img img;
      int cx = 1;
      int cy = 1;
      int rowH = 0;
      boolean dirty;

      Page(int size) {
         this.size = size;
         this.px = new int[size * size];
         Arrays.fill(this.px, 16777215);
      }
   }
}
