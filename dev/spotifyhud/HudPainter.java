package dev.spotifyhud;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Double;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class HudPainter {
   private static final Color PANEL_EDGE = new Color(255, 255, 255, 13);
   private static final Color PANEL_EDGE_HOVER = new Color(255, 255, 255, 60);
   static final Color TITLE = new Color(234, 236, 235);
   static final Color ARTIST = new Color(179, 179, 179);
   private static final Color TIME = new Color(167, 167, 167);
   private static final Color TRACK = new Color(62, 63, 63, 235);
   private static final Color FILL = new Color(247, 249, 247);
   private static final Color GREEN = Theme.GREEN;
   private static final Color ICON = new Color(248, 250, 250);
   private static final Color SPEAKER = new Color(255, 255, 255, 170);
   private static final Color HOVER_BG = new Color(255, 255, 255, 26);
   private static final Color ART_EMPTY = new Color(40, 40, 40);
   private static final Color NOTE = new Color(127, 127, 127);
   static final int LYRICS_H = 40;
   static final double R = 15.0;
   private static String family;
   private static String semiboldFamily;
   private static final Map<String, Font> FONT_CACHE = new HashMap<>();
   private static final HudPainter.Layers HUD = new HudPainter.Layers();
   private static final HudPainter.Layers LYR = new HudPainter.Layers();
   private static BufferedImage lastArtSrc;
   private static BufferedImage lastArtScaled;
   private static int lastArtSize;

   private HudPainter() {
   }

   private static void initFonts() {
      if (family == null) {
         Set<String> names = new HashSet<>();

         try {
            names.addAll(Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
         } catch (Throwable var6) {
         }

         String[] prefs = new String[]{"Segoe UI", "Helvetica Neue", "Arial", "Liberation Sans", "DejaVu Sans"};
         family = "SansSerif";

         for (String p : prefs) {
            if (names.contains(p)) {
               family = p;
               break;
            }
         }

         semiboldFamily = names.contains("Segoe UI Semibold") ? "Segoe UI Semibold" : null;
      }
   }

   public static Font font(String text, boolean title, float size) {
      initFonts();
      String fam;
      int style;
      if (title && semiboldFamily != null) {
         fam = semiboldFamily;
         style = 0;
      } else {
         fam = family;
         style = title ? 1 : 0;
      }

      String key = fam + style + size;
      Font f = FONT_CACHE.computeIfAbsent(key, k -> {
         Map<TextAttribute, Object> attrsx = new HashMap<>();
         attrsx.put(TextAttribute.FAMILY, fam);
         attrsx.put(TextAttribute.SIZE, size);
         if (style == 1) {
            attrsx.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
         }

         if (title) {
            attrsx.put(TextAttribute.TRACKING, 0.02F);
         }

         return new Font(attrsx);
      });
      if (text != null && f.canDisplayUpTo(text) != -1) {
         Map<TextAttribute, Object> attrs = new HashMap<>();
         attrs.put(TextAttribute.FAMILY, "Dialog");
         attrs.put(TextAttribute.SIZE, size);
         if (title) {
            attrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
         }

         return new Font(attrs);
      } else {
         return f;
      }
   }

   public static Font uiFont(boolean bold, float size) {
      initFonts();
      String key = "ui" + family + bold + size;
      return FONT_CACHE.computeIfAbsent(key, k -> {
         Map<TextAttribute, Object> attrs = new HashMap<>();
         attrs.put(TextAttribute.FAMILY, bold && semiboldFamily != null ? semiboldFamily : family);
         attrs.put(TextAttribute.SIZE, size);
         if (bold && semiboldFamily == null) {
            attrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
         }

         return new Font(attrs);
      });
   }

   public static void hints(Graphics2D g) {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
   }

   private static Graphics2D clear(BufferedImage img) {
      Graphics2D g = img.createGraphics();
      g.setComposite(AlphaComposite.Clear);
      g.fillRect(0, 0, img.getWidth(), img.getHeight());
      g.setComposite(AlphaComposite.SrcOver);
      hints(g);
      return g;
   }

   private static BufferedImage ensure(BufferedImage img, int w, int h) {
      return img != null && img.getWidth() == w && img.getHeight() == h ? img : new BufferedImage(w, h, 2);
   }

   private static int[] compose(
      HudPainter.Layers L, int pw, int ph, double dh, HudPainter.Style s, Object art, boolean anim, double t, String fgKey, Consumer<Graphics2D> fgPainter
   ) {
      BufferedImage artImg = art instanceof BufferedImage ? (BufferedImage)art : null;
      double px = pw / 207.0;
      String bgKey = s.theme + "|" + pw + "x" + ph + "|" + ("cover".equals(s.theme) ? System.identityHashCode(artImg) : 0);
      if (L.bg == null || !bgKey.equals(L.bgKey)) {
         L.bg = ensure(L.bg, pw, ph);
         Graphics2D g = clear(L.bg);
         g.scale(px, ph / dh);
         Theme.paint(g, s.theme, new Double(0.0, 0.0, 207.0, dh, 15.0, 15.0), 207.0, dh, artImg, px);
         g.dispose();
         L.bgKey = bgKey;
      }

      if (L.fg == null || !fgKey.equals(L.fgKey) || L.fg.getWidth() != pw || L.fg.getHeight() != ph) {
         L.fg = ensure(L.fg, pw, ph);
         Graphics2D g = clear(L.fg);
         g.scale(px, ph / dh);
         fgPainter.accept(g);
         g.dispose();
         L.fgKey = fgKey;
      }

      L.canvas = ensure(L.canvas, pw, ph);
      Graphics2D g = clear(L.canvas);
      g.drawImage(L.bg, 0, 0, null);
      if (anim && Anim.active(s.anim)) {
         Graphics2D a = (Graphics2D)g.create();
         a.scale(px, ph / dh);
         a.clip(new Double(0.0, 0.0, 207.0, dh, 15.0, 15.0));

         try {
            Anim.draw(a, s.anim, t, 207.0, dh, s.intensity, Theme.accent(s.theme, artImg));
         } finally {
            a.dispose();
         }
      }

      g.drawImage(L.fg, 0, 0, null);
      g.dispose();
      return L.canvas.getRGB(0, 0, pw, ph, null, 0, pw);
   }

   static int[] render(HudPainter.View v, int pw, int ph, HudPainter.Style s, double t, String fgKey) {
      Color accent = Theme.accent(s.theme, v.art instanceof BufferedImage ? (BufferedImage)v.art : null);
      return compose(HUD, pw, ph, 70.0, s, v.art, true, t, fgKey + "|" + s.theme + accent.getRGB(), g -> drawHud(g, v, pw / 207.0, accent));
   }

   private static void drawHud(Graphics2D g, HudPainter.View v, double px, Color accent) {
      PlayerState st = v.st;
      g.setColor(!v.interactive || v.hover != HudLayout.Region.PANEL && v.hover != HudLayout.Region.SETTINGS ? PANEL_EDGE : PANEL_EDGE_HOVER);
      g.setStroke(new BasicStroke((float)(1.0 / px)));
      double e = 0.5 / px;
      g.draw(new Double(e, e, 207.0 - 2.0 * e, 70.0 - 2.0 * e, 15.0, 15.0));
      RoundRectangle2D artClip = new Double(9.0, 9.0, 37.0, 37.0, 8.0, 8.0);
      if (v.art instanceof BufferedImage a) {
         int target = Math.max(1, (int)Math.round(37.0 * px));
         BufferedImage rounded = roundedArt(a, target, 4.0 * px);
         AffineTransform at = new AffineTransform();
         at.translate(9.0, 9.0);
         at.scale(37.0 / rounded.getWidth(), 37.0 / rounded.getHeight());
         g.drawImage(rounded, at, null);
      } else {
         g.setColor(ART_EMPTY);
         g.fill(artClip);
         drawNote(g, 27.5, 27.5);
      }

      double timeW = 0.0;
      if (v.showTime) {
         long dur = st.durationMs;
         long prog = v.progressPreview >= 0.0 ? (long)(v.progressPreview * dur) : st.currentProgress();
         String time = PlayerState.fmt(prog) + " / " + PlayerState.fmt(dur);
         g.setFont(font(time, false, 7.8F));
         timeW = g.getFontMetrics().getStringBounds(time, g).getWidth();
         g.setColor(TIME);
         g.drawString(time, (float)(197.0 - timeW), 29.0F);
      }

      String title = v.title == null ? "" : v.title;
      g.setFont(font(title, true, 9.9F));
      g.setColor(TITLE);
      double titleMax = 145.0 - (v.interactive ? 9 : 0);
      g.drawString(ellipsize(g, title, titleMax), 53.0F, 18.2F);
      String artist = v.artist == null ? "" : v.artist;
      g.setFont(font(artist, false, 8.1F));
      g.setColor(ARTIST);
      double artistMax = 144.0 - (timeW > 0.0 ? timeW + 6.0 : 0.0);
      g.drawString(ellipsize(g, artist, artistMax), 53.0F, 29.0F);
      if (v.interactive) {
         if (v.hover == HudLayout.Region.SETTINGS) {
            g.setColor(HOVER_BG);
            g.fill(new java.awt.geom.Ellipse2D.Double(194.2, 3.1999999999999993, 10.0, 10.0));
         }

         g.setColor(v.hover == HudLayout.Region.SETTINGS ? ICON : new Color(255, 255, 255, 150));
         drawGear(g, 199.2, 8.2, 3.1);
      }

      double frac = v.progressPreview >= 0.0 ? v.progressPreview : (st.durationMs > 0L ? (double)st.currentProgress() / st.durationMs : 0.0);
      boolean progHover = v.interactive && (v.hover == HudLayout.Region.PROGRESS || v.progressPreview >= 0.0);
      bar(g, 52.5, 36.6, 146.5, 3.4, frac, progHover, accent);
      if (v.showControls) {
         int vol = v.volumePreview >= 0 ? v.volumePreview : st.volume;
         boolean volHover = v.interactive && (v.hover == HudLayout.Region.VOLUME || v.volumePreview >= 0);
         hoverBg(g, v, HudLayout.Region.SPEAKER, 14.0);
         drawSpeaker(g, 14.0, 57.0, vol);
         bar(g, 21.0, 55.4, 37.5, 3.2, vol < 0 ? 0.0 : vol / 100.0, volHover, accent);
         hoverBg(g, v, HudLayout.Region.PREV, 88.8);
         hoverBg(g, v, HudLayout.Region.PLAY, 103.3);
         hoverBg(g, v, HudLayout.Region.NEXT, 118.0);
         hoverBg(g, v, HudLayout.Region.REPEAT, 167.2);
         hoverBg(g, v, HudLayout.Region.SHUFFLE, 181.3);
         hoverBg(g, v, HudLayout.Region.LIKE, 195.8);
         g.setColor(ICON);
         drawPrev(g, 88.8, 57.0);
         if (st.playing) {
            drawPause(g, 103.3, 57.0);
         } else {
            drawPlay(g, 103.3, 57.0);
         }

         drawNext(g, 118.0, 57.0);
         boolean rep = !"off".equals(st.repeat);
         g.setColor(rep ? accent : ICON);
         drawRepeat(g, 167.2, 57.0, "track".equals(st.repeat));
         if (rep) {
            dot(g, 167.2);
         }

         g.setColor(st.shuffle ? accent : ICON);
         drawShuffle(g, 181.3, 57.0);
         if (st.shuffle) {
            dot(g, 181.3);
         }

         g.setColor(st.liked ? accent : ICON);
         drawHeart(g, 195.8, 57.0);
      }
   }

   public static void drawGear(Graphics2D g, double cx, double cy, double r) {
      Path2D p = new java.awt.geom.Path2D.Double();
      int teeth = 8;

      for (int i = 0; i < teeth * 2; i++) {
         double a0 = (Math.PI * 2) * i / (teeth * 2);
         double a1 = (Math.PI * 2) * (i + 1) / (teeth * 2);
         double rr = i % 2 == 0 ? r : r * 0.76;
         double x0 = cx + Math.cos(a0) * rr;
         double y0 = cy + Math.sin(a0) * rr;
         double x1 = cx + Math.cos(a1) * rr;
         double y1 = cy + Math.sin(a1) * rr;
         if (i == 0) {
            p.moveTo(x0, y0);
         } else {
            p.lineTo(x0, y0);
         }

         p.lineTo(x1, y1);
      }

      p.closePath();
      Area area = new Area(p);
      area.subtract(new Area(new java.awt.geom.Ellipse2D.Double(cx - r * 0.36, cy - r * 0.36, r * 0.72, r * 0.72)));
      g.fill(area);
   }

   static int[] renderLyrics(HudPainter.LyricsView v, int pw, int ph, HudPainter.Style s, double t, String fgKey) {
      Color accent = Theme.accent(s.theme, v.art instanceof BufferedImage ? (BufferedImage)v.art : null);
      return compose(LYR, pw, ph, 40.0, s, v.art, s.animOnLyrics, t, fgKey + "|" + s.theme + accent.getRGB(), g -> drawLyrics(g, v, pw / 207.0, s, accent));
   }

   private static void drawLyrics(Graphics2D g, HudPainter.LyricsView v, double px, HudPainter.Style s, Color accent) {
      g.setColor(v.interactive && v.hover ? PANEL_EDGE_HOVER : PANEL_EDGE);
      g.setStroke(new BasicStroke((float)(1.0 / px)));
      double e = 0.5 / px;
      g.draw(new Double(e, e, 207.0 - 2.0 * e, 40.0 - 2.0 * e, 15.0, 15.0));
      Shape oldClip = g.getClip();
      g.clip(new java.awt.geom.Rectangle2D.Double(3.0, 1.5, 201.0, 37.0));
      double maxW = 187.0;
      if (v.message != null) {
         g.setFont(font(v.message, false, 8.2F));
         g.setColor(ARTIST);
         centered(g, ellipsize(g, v.message, maxW), 23.5);
         g.setClip(oldClip);
      } else {
         double a = Math.max(0.0, Math.min(1.0, v.anim));
         double ease = 1.0 - Math.pow(1.0 - a, 3.0);
         double shift = (1.0 - ease) * 12.0;
         String[] words = v.words.length > 0 ? v.words : new String[]{v.curText.isEmpty() ? "♪" : v.curText};
         String joined = String.join(" ", words);
         float size = 9.6F;
         g.setFont(font(joined, true, size));

         while (size > 7.4F && width(g, words) > maxW) {
            size -= 0.3F;
            g.setFont(font(joined, true, size));
         }

         int split = words.length;
         if (width(g, words) > maxW && words.length > 1) {
            split = splitPoint(g, words);
         }

         boolean twoRows = split < words.length;
         if (!twoRows) {
            drawLine(g, v.prev, false, 11.0 + shift, 0.42 * ease, maxW);
         }

         double[] bases = twoRows ? new double[]{19.2 + shift, 29.0 + shift} : new double[]{24.5 + shift};
         int[][] rows = twoRows ? new int[][]{{0, split}, {split, words.length}} : new int[][]{{0, words.length}};
         float alphaCur = (float)(0.55 + 0.45 * ease);

         for (int r = 0; r < rows.length; r++) {
            drawKaraokeRow(g, words, rows[r][0], rows[r][1], bases[r], v, s, accent, alphaCur, size);
         }

         drawLine(g, v.next, false, (twoRows ? 37.6 : 35.5) + shift, 0.55 * ease, maxW);
         g.setClip(oldClip);
      }
   }

   private static double width(Graphics2D g, String[] words) {
      FontMetrics fm = g.getFontMetrics();
      double w = 0.0;
      double sp = fm.getStringBounds(" ", g).getWidth();

      for (int i = 0; i < words.length; i++) {
         w += fm.getStringBounds(words[i], g).getWidth() + (i > 0 ? sp : 0.0);
      }

      return w;
   }

   private static int splitPoint(Graphics2D g, String[] words) {
      double total = width(g, words);
      double acc = 0.0;
      double sp = g.getFontMetrics().getStringBounds(" ", g).getWidth();

      for (int i = 0; i < words.length; i++) {
         acc += g.getFontMetrics().getStringBounds(words[i], g).getWidth() + sp;
         if (acc >= total / 2.0) {
            return Math.max(1, Math.min(words.length - 1, i + 1));
         }
      }

      return words.length / 2;
   }

   private static void drawKaraokeRow(
      Graphics2D g, String[] words, int from, int to, double baseline, HudPainter.LyricsView v, HudPainter.Style s, Color accent, float alpha, float size
   ) {
      FontMetrics fm = g.getFontMetrics();
      double sp = fm.getStringBounds(" ", g).getWidth();
      double growK = s.grow ? 1.0 + 0.18 * easeOut(v.grow) : 1.0;
      double[] ww = new double[to - from];
      double[] slot = new double[to - from];
      double total = 0.0;

      for (int i = from; i < to; i++) {
         ww[i - from] = fm.getStringBounds(words[i], g).getWidth();
         slot[i - from] = ww[i - from] * (s.karaoke && i == v.wordIdx ? growK : 1.0);
         total += slot[i - from] + (i > from ? sp : 0.0);
      }

      double maxW = 187.0;
      double x = (207.0 - Math.min(total, maxW)) / 2.0;
      double padX = 4.5;
      double hgt = size * 1.45;
      Color pill = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(38.0F * alpha));
      g.setColor(pill);
      g.fill(new Double(x - padX, baseline - size * 1.02, Math.min(total, maxW) + 2.0 * padX, hgt, hgt, hgt));

      for (int i = from; i < to; i++) {
         String w = words[i];
         double wid = ww[i - from];
         double wx = x + (slot[i - from] - wid) / 2.0;
         boolean active = s.karaoke && i == v.wordIdx;
         Color c;
         if (!s.karaoke) {
            c = TITLE;
         } else if (i < v.wordIdx) {
            c = TITLE;
         } else if (active) {
            c = accent;
         } else {
            c = new Color(255, 255, 255, 120);
         }

         if (!active) {
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(c.getAlpha() * alpha)));
            g.drawString(w, (float)wx, (float)baseline);
         } else {
            AffineTransform old = g.getTransform();
            double cx = wx + wid / 2.0;
            double cy = baseline - size * 0.35;
            g.translate(cx, cy);
            g.scale(growK, growK);
            g.translate(-cx, -cy);
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(70.0F * alpha)));

            for (double[] o : new double[][]{{-0.35, 0.0}, {0.35, 0.0}, {0.0, -0.35}, {0.0, 0.35}}) {
               g.drawString(w, (float)(wx + o[0]), (float)(baseline + o[1]));
            }

            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(255.0F * alpha)));
            g.drawString(w, (float)wx, (float)baseline);
            g.setTransform(old);
         }

         x += slot[i - from] + sp;
      }
   }

   private static double easeOut(double a) {
      a = Math.max(0.0, Math.min(1.0, a));
      return 1.0 - Math.pow(1.0 - a, 3.0);
   }

   private static void drawLine(Graphics2D g, String text, boolean current, double baseline, double alpha, double maxW) {
      if (text != null && !text.isEmpty() && !(alpha <= 0.01)) {
         float size = current ? 9.6F : 7.6F;
         g.setFont(font(text, current, size));
         Color c = current ? TITLE : ARTIST;
         g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)Math.round(255.0 * Math.min(1.0, alpha))));
         centered(g, ellipsize(g, text, maxW), baseline);
      }
   }

   private static void centered(Graphics2D g, String text, double baseline) {
      double w = g.getFontMetrics().getStringBounds(text, g).getWidth();
      g.drawString(text, (float)((207.0 - w) / 2.0), (float)baseline);
   }

   private static BufferedImage roundedArt(BufferedImage src, int size, double radius) {
      if (src == lastArtSrc && size == lastArtSize && lastArtScaled != null) {
         return lastArtScaled;
      } else {
         BufferedImage scaled = scaledArt(src, size);
         BufferedImage out = new BufferedImage(size, size, 2);
         Graphics2D g = out.createGraphics();
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.setColor(Color.WHITE);
         g.fill(new Double(0.0, 0.0, size, size, radius * 2.0, radius * 2.0));
         g.setComposite(AlphaComposite.SrcIn);
         g.drawImage(scaled, 0, 0, null);
         g.dispose();
         lastArtSrc = src;
         lastArtSize = size;
         lastArtScaled = out;
         return out;
      }
   }

   private static BufferedImage scaledArt(BufferedImage src, int size) {
      BufferedImage cur = src;
      int w = src.getWidth();

      int h;
      for (h = src.getHeight(); w / 2 >= size && h / 2 >= size; cur = resize(cur, w, h)) {
         w /= 2;
         h /= 2;
      }

      if (w != size || h != size) {
         cur = resize(cur, size, size);
      }

      return cur;
   }

   private static BufferedImage resize(BufferedImage src, int w, int h) {
      BufferedImage out = new BufferedImage(w, h, 2);
      Graphics2D g = out.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g.drawImage(src, 0, 0, w, h, null);
      g.dispose();
      return out;
   }

   private static String ellipsize(Graphics2D g, String s, double max) {
      FontMetrics fm = g.getFontMetrics();
      if (fm.getStringBounds(s, g).getWidth() <= max) {
         return s;
      } else {
         String ell = "…";
         int lo = 0;
         int hi = s.length();

         while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (fm.getStringBounds(s.substring(0, mid).trim() + ell, g).getWidth() <= max) {
               lo = mid;
            } else {
               hi = mid - 1;
            }
         }

         return s.substring(0, lo).trim() + ell;
      }
   }

   private static void bar(Graphics2D g, double x, double y, double w, double h, double frac, boolean hover, Color accent) {
      frac = Math.max(0.0, Math.min(1.0, frac));
      g.setColor(TRACK);
      g.fill(new Double(x, y, w, h, h, h));
      double fw = w * frac;
      if (fw >= 0.35) {
         g.setColor(hover ? accent : FILL);
         double arc = Math.min(h, fw);
         g.fill(new Double(x, y, fw, h, arc, h));
      }

      if (hover) {
         double cx = x + w * frac;
         double cy = y + h / 2.0;
         double r = 3.0;
         g.setColor(new Color(0, 0, 0, 70));
         g.fill(new java.awt.geom.Ellipse2D.Double(cx - r - 0.2, cy - r + 0.4, 2.0 * r + 0.4, 2.0 * r + 0.4));
         g.setColor(Color.WHITE);
         g.fill(new java.awt.geom.Ellipse2D.Double(cx - r, cy - r, 2.0 * r, 2.0 * r));
      }
   }

   private static void hoverBg(Graphics2D g, HudPainter.View v, HudLayout.Region r, double cx) {
      if (v.interactive && v.hover == r) {
         g.setColor(HOVER_BG);
         g.fill(new java.awt.geom.Ellipse2D.Double(cx - 5.6, 51.4, 11.2, 11.2));
      }
   }

   private static void dot(Graphics2D g, double cx) {
      g.fill(new java.awt.geom.Ellipse2D.Double(cx - 0.65, 61.1, 1.3, 1.3));
   }

   private static void drawPause(Graphics2D g, double cx, double cy) {
      double h = 4.8;
      double w = 1.35;
      double gap = 1.35;
      g.fill(new Double(cx - gap / 2.0 - w, cy - h / 2.0, w, h, 0.5, 0.5));
      g.fill(new Double(cx + gap / 2.0, cy - h / 2.0, w, h, 0.5, 0.5));
   }

   private static void drawPlay(Graphics2D g, double cx, double cy) {
      Path2D p = new java.awt.geom.Path2D.Double();
      double h = 5.2;
      double w = 4.6;
      p.moveTo(cx - w / 2.0 + 0.4, cy - h / 2.0);
      p.lineTo(cx + w / 2.0 + 0.4, cy);
      p.lineTo(cx - w / 2.0 + 0.4, cy + h / 2.0);
      p.closePath();
      g.fill(p);
   }

   private static void drawPrev(Graphics2D g, double cx, double cy) {
      double h = 4.6;
      g.fill(new Double(cx - 2.9, cy - h / 2.0, 1.0, h, 0.4, 0.4));
      Path2D p = new java.awt.geom.Path2D.Double();
      p.moveTo(cx + 2.7, cy - h / 2.0);
      p.lineTo(cx - 1.7, cy);
      p.lineTo(cx + 2.7, cy + h / 2.0);
      p.closePath();
      g.fill(p);
   }

   private static void drawNext(Graphics2D g, double cx, double cy) {
      double h = 4.6;
      g.fill(new Double(cx + 1.9, cy - h / 2.0, 1.0, h, 0.4, 0.4));
      Path2D p = new java.awt.geom.Path2D.Double();
      p.moveTo(cx - 2.7, cy - h / 2.0);
      p.lineTo(cx + 1.7, cy);
      p.lineTo(cx - 2.7, cy + h / 2.0);
      p.closePath();
      g.fill(p);
   }

   private static void drawRepeat(Graphics2D g, double cx, double cy, boolean one) {
      double w = 5.4;
      double h = 4.0;
      g.setStroke(new BasicStroke(0.95F, 1, 1));
      Path2D p = new java.awt.geom.Path2D.Double();
      double l = cx - w / 2.0;
      double r = cx + w / 2.0;
      double t = cy - h / 2.0;
      double b = cy + h / 2.0;
      double rad = 1.0;
      p.moveTo(r - 1.9, t);
      p.lineTo(l + rad, t);
      p.quadTo(l, t, l, t + rad);
      p.lineTo(l, b - rad);
      p.quadTo(l, b, l + rad, b);
      p.lineTo(r - rad, b);
      p.quadTo(r, b, r, b - rad);
      p.lineTo(r, t + rad + 0.3);
      g.draw(p);
      Path2D a = new java.awt.geom.Path2D.Double();
      a.moveTo(r - 1.9, t - 1.35);
      a.lineTo(r - 0.2, t);
      a.lineTo(r - 1.9, t + 1.35);
      a.closePath();
      g.fill(a);
      if (one) {
         Font f = new Font("SansSerif", 1, 1).deriveFont(3.2F);
         g.setFont(f);
         FontMetrics fm = g.getFontMetrics();
         String s = "1";
         double sw = fm.getStringBounds(s, g).getWidth();
         g.drawString(s, (float)(cx - sw / 2.0), (float)(cy + 1.15));
      }
   }

   private static void drawShuffle(Graphics2D g, double cx, double cy) {
      g.setStroke(new BasicStroke(0.9F, 1, 1));
      double l = cx - 2.9;
      double r = cx + 2.1;
      double t = cy - 1.7;
      double b = cy + 1.7;
      Path2D p1 = new java.awt.geom.Path2D.Double();
      p1.moveTo(l, t);
      p1.curveTo(cx - 0.8, t, cx - 0.4, b, r, b);
      g.draw(p1);
      Path2D p2 = new java.awt.geom.Path2D.Double();
      p2.moveTo(l, b);
      p2.curveTo(cx - 0.8, b, cx - 0.4, t, r, t);
      g.draw(p2);
      arrowHead(g, r + 0.9, t);
      arrowHead(g, r + 0.9, b);
   }

   private static void arrowHead(Graphics2D g, double tipX, double y) {
      Path2D a = new java.awt.geom.Path2D.Double();
      a.moveTo(tipX - 1.6, y - 1.25);
      a.lineTo(tipX, y);
      a.lineTo(tipX - 1.6, y + 1.25);
      a.closePath();
      g.fill(a);
   }

   private static void drawHeart(Graphics2D g, double cx, double cy) {
      double w = 5.4;
      double h = 4.7;
      double x = cx - w / 2.0;
      double y = cy - h / 2.0 + 0.1;
      Path2D p = new java.awt.geom.Path2D.Double();
      p.moveTo(cx, y + h);
      p.curveTo(x - 0.2, y + h * 0.55, x - 0.1, y + 0.1, x + w * 0.27, y);
      p.curveTo(x + w * 0.42, y - 0.05, cx - 0.05, y + 0.6, cx, y + 1.1);
      p.curveTo(cx + 0.05, y + 0.6, x + w * 0.58, y - 0.05, x + w * 0.73, y);
      p.curveTo(x + w + 0.1, y + 0.1, x + w + 0.2, y + h * 0.55, cx, y + h);
      p.closePath();
      g.fill(p);
   }

   private static void drawSpeaker(Graphics2D g, double cx, double cy, int vol) {
      g.setColor(SPEAKER);
      Path2D p = new java.awt.geom.Path2D.Double();
      double x0 = cx - 3.0;
      p.moveTo(x0, cy - 1.1);
      p.lineTo(x0 + 1.3, cy - 1.1);
      p.lineTo(x0 + 3.0, cy - 2.6);
      p.lineTo(x0 + 3.0, cy + 2.6);
      p.lineTo(x0 + 1.3, cy + 1.1);
      p.lineTo(x0, cy + 1.1);
      p.closePath();
      g.fill(p);
      g.setStroke(new BasicStroke(0.75F, 1, 1));
      if (vol == 0) {
         double mx = cx + 1.7;
         g.draw(new java.awt.geom.Line2D.Double(mx - 0.1, cy - 1.2, mx + 2.2, cy + 1.2));
         g.draw(new java.awt.geom.Line2D.Double(mx - 0.1, cy + 1.2, mx + 2.2, cy - 1.2));
      } else {
         g.draw(new java.awt.geom.Arc2D.Double(cx - 1.4, cy - 1.6, 3.2, 3.2, -55.0, 110.0, 0));
         if (vol > 50 || vol < 0) {
            g.draw(new java.awt.geom.Arc2D.Double(cx - 2.1, cy - 2.8, 5.6, 5.6, -55.0, 110.0, 0));
         }
      }
   }

   private static void drawNote(Graphics2D g, double cx, double cy) {
      g.setColor(NOTE);
      g.fill(new java.awt.geom.Ellipse2D.Double(cx - 5.5, cy + 2.2, 4.4, 3.4));
      g.fill(new java.awt.geom.Ellipse2D.Double(cx + 1.6, cy + 0.8, 4.4, 3.4));
      g.setStroke(new BasicStroke(1.1F));
      g.draw(new java.awt.geom.Line2D.Double(cx - 1.6, cy + 3.8, cx - 1.6, cy - 5.0));
      g.draw(new java.awt.geom.Line2D.Double(cx + 5.5, cy + 2.4, cx + 5.5, cy - 6.4));
      Path2D beam = new java.awt.geom.Path2D.Double();
      beam.moveTo(cx - 1.6 - 0.55, cy - 5.0);
      beam.lineTo(cx + 5.5 + 0.55, cy - 6.4);
      beam.lineTo(cx + 5.5 + 0.55, cy - 4.6);
      beam.lineTo(cx - 1.6 - 0.55, cy - 3.2);
      beam.closePath();
      g.fill(beam);
   }

   private static final class Layers {
      BufferedImage canvas;
      BufferedImage bg;
      BufferedImage fg;
      String bgKey = "";
      String fgKey = "";
   }

   static final class LyricsView {
      String prev = "";
      String next = "";
      String[] words = new String[0];
      String curText = "";
      int wordIdx = -1;
      double grow = 1.0;
      double anim = 1.0;
      String message;
      boolean interactive;
      boolean hover;
      Object art;
   }

   static final class Style {
      String theme = "standard";
      String anim = "none";
      double intensity = 0.7;
      boolean animOnLyrics = true;
      boolean karaoke = true;
      boolean grow = true;
   }

   static final class View {
      PlayerState st;
      Object art;
      String title;
      String artist;
      boolean showTime;
      boolean showControls;
      HudLayout.Region hover;
      boolean interactive;
      double progressPreview = -1.0;
      int volumePreview = -1;
   }
}
