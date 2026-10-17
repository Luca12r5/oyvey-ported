package dev.lego.visual;

import dev.lego.SpotifyModule;
import dev.lego.ui.Tx;
import dev.spotifyhud.HudPainter;
import dev.spotifyhud.Lyrics;
import dev.spotifyhud.PlayerState;
import dev.spotifyhud.SpotifyHudMod;
import dev.spotifyhud.Theme;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/**
 * Sky HUD: writes the current synced lyric line of the playing song across the sky, karaoke style (same words,
 * timing and accent colour as the Spotify HUD's lyrics strip). The text floats high in front of you and follows
 * your view slowly, like a banner in the sky. Toggle: Spotify HUD module, setting "Sky HUD".
 */
public final class SkyLyrics {
   private static final int TW = 1024;
   private static final int TH = 256;
   private static final Identifier TEX = Identifier.fromNamespaceAndPath("legoclient", "sky/lyrics");
   private static DynamicTexture tex;
   private static boolean broken;
   private static String drawnKey = "";
   private static final BufferedImage IMG = new BufferedImage(TW, TH, BufferedImage.TYPE_INT_ARGB);
   private static double yaw = Double.NaN;
   private static long lastNs;
   private static String lastLineKey = "";
   private static long lineChangedAt;
   private static float alpha;

   private SkyLyrics() {
   }

   public static void render(float partial) {
      SpotifyModule m = SpotifyModule.INSTANCE;
      if (broken || m == null || !m.skyHud.get() || !SpotifyHudMod.initialized()) {
         alpha = 0.0F;
         yaw = Double.NaN;
         return;
      }
      long now = System.nanoTime();
      float dt = lastNs == 0L ? 0.016F : Math.min(0.1F, (now - lastNs) / 1.0E9F);
      lastNs = now;

      PlayerState st = SpotifyHudMod.media().state();
      Lyrics lyr = SpotifyHudMod.lyrics();
      if (st == null || !st.hasTrack || lyr == null) {
         alpha = Math.max(0.0F, alpha - dt * 2.0F);
         return;
      }
      lyr.update(st);
      Lyrics.Result res = lyr.current();
      String cur = "";
      String next = "";
      String[] words = new String[0];
      int wordIdx = -1;
      double grow = 1.0;
      int idx = -1;
      if (res != null && res.status == Lyrics.Status.SYNCED && !res.lines.isEmpty()) {
         List<Lyrics.Line> lines = res.lines;
         long ms = st.currentProgress() + SpotifyHudMod.config().lyricsOffsetMs;
         idx = Lyrics.indexAt(lines, ms);
         if (idx >= 0) {
            Lyrics.Line l = lines.get(idx);
            cur = l.text.isBlank() ? "♪" : l.text;
            words = l.words;
            wordIdx = l.wordAt(ms);
            grow = wordIdx >= 0 ? Math.min(1.0, (ms - l.wordTimes[wordIdx]) / 150.0) : 1.0;
         }
         if (idx + 1 < lines.size()) next = lines.get(idx + 1).text;
      } else if (res != null && res.status == Lyrics.Status.INSTRUMENTAL) {
         cur = "♪ Instrumental ♪";
      }
      boolean show = !cur.isEmpty();
      alpha = Math.max(0.0F, Math.min(1.0F, alpha + (show ? dt * 2.5F : -dt * 2.0F)));
      if (alpha <= 0.01F) return;

      String lineKey = (res == null ? "" : res.key) + "#" + idx;
      long wall = System.currentTimeMillis();
      if (!lineKey.equals(lastLineKey)) {
         lastLineKey = lineKey;
         lineChangedAt = wall;
      }
      double anim = Math.min(1.0, (wall - lineChangedAt) / 320.0);
      int accent = Theme.accent(SpotifyHudMod.config().theme, SpotifyHudMod.media().art() instanceof BufferedImage b ? b : null).getRGB();
      String key = cur + "|" + next + "|" + wordIdx + "|" + Math.round(grow * 6) + "|" + Math.round(anim * 10) + "|" + accent + "|" + m.skyNext.get();
      if (show && !key.equals(drawnKey)) {
         drawnKey = key;
         if (!upload(paint(cur, next, words, wordIdx, grow, anim, accent, m.skyNext.get()))) return;
      }
      if (tex == null) return;

      // Position: high in front of the camera; the banner follows the view slowly.
      double camYaw = R3.camYaw();
      if (Double.isNaN(yaw)) yaw = camYaw;
      double d = camYaw - yaw;
      while (d > 180.0) d -= 360.0;
      while (d < -180.0) d += 360.0;
      yaw += d * Math.min(1.0, dt * 1.6);
      double el = Math.toRadians(m.skyHeight.value);
      double dist = 24.0;
      double r = Math.toRadians(yaw);
      double fx = -Math.sin(r) * Math.cos(el), fy = Math.sin(el), fz = Math.cos(r) * Math.cos(el);
      double x = R3.camX() + fx * dist, y = R3.camY() + fy * dist, z = R3.camZ() + fz * dist;
      double w = 22.0 * m.skySize.value;
      double h = w * TH / TW;
      int a = Math.round(alpha * 255.0F);
      R3.billboard(TEX, x, y, z, w, h, 0.0F, 0.0F, 1.0F, 1.0F, a << 24 | 0xFFFFFF);
   }

   private static boolean upload(int[] px) {
      try {
         if (tex == null) tex = Tx.register(TEX, px, TW, TH, true);
         else Tx.update(tex, px, TW, TH);
         return true;
      } catch (Throwable e) {
         broken = true;
         return false;
      }
   }

   /** Draws the banner: current line large (sung words white, the current word in the accent colour), next line below. */
   private static int[] paint(String cur, String next, String[] words, int wordIdx, double grow, double anim, int accent, boolean showNext) {
      Graphics2D g = IMG.createGraphics();
      g.setComposite(AlphaComposite.Clear);
      g.fillRect(0, 0, TW, TH);
      g.setComposite(AlphaComposite.SrcOver);
      HudPainter.hints(g);
      g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
      double e = 1.0 - Math.pow(1.0 - anim, 3.0);
      double rise = (1.0 - e) * 26.0;
      String[] ws = words.length > 0 ? words : new String[]{cur};
      String joined = String.join(" ", ws);
      float size = 92.0F;
      Font f = HudPainter.font(joined, true, size);
      g.setFont(f);
      while (size > 40.0F && width(g, ws) > TW - 60) {
         size -= 4.0F;
         f = HudPainter.font(joined, true, size);
         g.setFont(f);
      }
      FontMetrics fm = g.getFontMetrics();
      double sp = fm.stringWidth(" ");
      double total = width(g, ws);
      double xx = (TW - total) / 2.0;
      double base = (showNext ? 128.0 : 156.0) + rise;
      Color acc = new Color(accent, false);
      for (int i = 0; i < ws.length; i++) {
         String wd = ws[i];
         double ww = fm.stringWidth(wd);
         Color c = wordIdx < 0 && words.length == 0 ? Color.WHITE : (i < wordIdx ? Color.WHITE : (i == wordIdx ? acc : new Color(255, 255, 255, 150)));
         double sc = i == wordIdx ? 1.0 + 0.14 * (1.0 - Math.pow(1.0 - grow, 3.0)) : 1.0;
         AffineTransform old = g.getTransform();
         double cx = xx + ww / 2.0, cy = base - size * 0.35;
         g.translate(cx, cy);
         g.scale(sc, sc);
         g.translate(-cx, -cy);
         GlyphVector gv = f.createGlyphVector(g.getFontRenderContext(), wd);
         java.awt.Shape shape = gv.getOutline((float)xx, (float)base);
         // Soft glow and a dark outline so the text reads against any sky.
         for (int k = 3; k >= 1; k--) {
            g.setStroke(new BasicStroke(k * 7.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(i == wordIdx ? acc.getRed() : 0, i == wordIdx ? acc.getGreen() : 0, i == wordIdx ? acc.getBlue() : 0, (int)((i == wordIdx ? 40 : 34) * e)));
            g.draw(shape);
         }
         g.setStroke(new BasicStroke(5.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
         g.setColor(new Color(0, 0, 0, (int)(150 * e)));
         g.draw(shape);
         g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(c.getAlpha() * (0.4 + 0.6 * e))));
         g.fill(shape);
         g.setTransform(old);
         xx += ww + sp;
      }
      if (showNext && next != null && !next.isBlank()) {
         float ns = 44.0F;
         Font nf = HudPainter.font(next, false, ns);
         g.setFont(nf);
         while (ns > 24.0F && g.getFontMetrics().stringWidth(next) > TW - 80) {
            ns -= 2.0F;
            nf = HudPainter.font(next, false, ns);
            g.setFont(nf);
         }
         double nw = g.getFontMetrics().stringWidth(next);
         java.awt.Shape ns2 = nf.createGlyphVector(g.getFontRenderContext(), next).getOutline((float)((TW - nw) / 2.0), (float)(214.0 + rise));
         g.setStroke(new BasicStroke(4.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
         g.setColor(new Color(0, 0, 0, (int)(110 * e)));
         g.draw(ns2);
         g.setColor(new Color(220, 220, 220, (int)(170 * e)));
         g.fill(ns2);
      }
      g.dispose();
      return IMG.getRGB(0, 0, TW, TH, null, 0, TW);
   }

   private static double width(Graphics2D g, String[] ws) {
      FontMetrics fm = g.getFontMetrics();
      double sp = fm.stringWidth(" ");
      double t = 0.0;
      for (int i = 0; i < ws.length; i++) t += fm.stringWidth(ws[i]) + (i > 0 ? sp : 0.0);
      return t;
   }
}
