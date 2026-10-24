package dev.spotifyhud;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

final class HudRenderer {
   private final TexSlot hudSlot = new TexSlot(Identifier.fromNamespaceAndPath("spotifyhud", "hud_panel"));
   private final TexSlot lyricsSlot = new TexSlot(Identifier.fromNamespaceAndPath("spotifyhud", "lyrics_panel"));
   private boolean awtFailed;
   private int lastLyricIndex = Integer.MIN_VALUE;
   private String lastLyricKey = "";
   private long lyricChangedAt = 0L;
   private static final long T0 = System.currentTimeMillis();

   int[] bounds(int sw, int sh, Config cfg) {
      double scale = effectiveScale(sw, cfg);
      int w = Math.max(20, (int)Math.round(207.0 * scale));
      int h = Math.max(7, (int)Math.round(70.0 * scale));
      int x = (int)Math.round(Config.clamp01(cfg.posX) * Math.max(0, sw - w));
      int y = (int)Math.round(Config.clamp01(cfg.posY) * Math.max(0, sh - h));
      return new int[]{x, y, w, h};
   }

   private static double effectiveScale(int sw, Config cfg) {
      double scale = cfg.scale;
      if (207.0 * scale > sw - 2) {
         scale = (sw - 2) / 207.0;
      }

      return scale;
   }

   int[] lyricsBounds(int[] hb, int sw, int sh, Config cfg) {
      double scale = hb[2] / 207.0;
      int lh = Math.max(6, (int)Math.round(40.0 * scale));
      int gap = Math.max(1, (int)Math.round(3.0 * scale));
      int y = hb[1] + hb[3] + gap;
      if (y + lh > sh) {
         y = hb[1] - gap - lh;
      }

      return new int[]{hb[0], y, hb[2], lh};
   }

   void render(GuiGraphics ctx, int x, int y, int w, int h, HudPainter.View v) {
      Minecraft mc = Minecraft.getInstance();
      int s = Math.max(1, mc.getWindow().getGuiScale());
      int pw = w * s;
      int ph = h * s;
      if (!this.awtFailed && pw > 0 && ph > 0 && pw <= 8192 && ph <= 8192) {
         try {
            HudPainter.Style st = style();
            double t = animTime();
            String fk = key(v, pw, ph);
            String k = fk + "|" + st.theme + "|" + (Anim.active(st.anim) ? st.anim + frame(t) + st.intensity : "");
            this.hudSlot.draw(ctx, mc, x, y, w, h, pw, ph, k, () -> HudPainter.render(v, pw, ph, st, t, fk));
            return;
         } catch (Throwable var16) {
            this.awtFailed = true;
            SpotifyHudMod.LOG("Java2D-Rendering nicht verfügbar, nutze Fallback: " + var16);
         }
      }

      this.renderFallback(ctx, mc, x, y, w, h, v);
   }

   static HudPainter.Style style() {
      Config c = SpotifyHudMod.config();
      HudPainter.Style s = new HudPainter.Style();
      s.theme = c.theme;
      s.anim = c.animation;
      s.intensity = c.animIntensity;
      s.animOnLyrics = c.animOnLyrics;
      s.karaoke = c.karaoke;
      s.grow = c.wordGrow;
      return s;
   }

   static double animTime() {
      return (System.currentTimeMillis() - T0) / 1000.0;
   }

   static long frame(double t) {
      return (long)(t * 30.0);
   }

   private static String key(HudPainter.View v, int pw, int ph) {
      PlayerState st = v.st;
      double pxPerUnit = pw / 207.0;
      long dur = st.durationMs;
      long prog = v.progressPreview >= 0.0 ? (long)(v.progressPreview * dur) : st.currentProgress();
      double frac = v.progressPreview >= 0.0 ? v.progressPreview : (dur > 0L ? (double)prog / dur : 0.0);
      long barPx = Math.round(frac * 146.5 * pxPerUnit * 2.0);
      StringBuilder b = new StringBuilder(160);
      b.append(pw)
         .append('x')
         .append(ph)
         .append('|')
         .append(v.title)
         .append('|')
         .append(v.artist)
         .append('|')
         .append(v.showTime)
         .append(v.showControls)
         .append('|')
         .append(v.showTime ? prog / 1000L : -1L)
         .append('|')
         .append(dur / 1000L)
         .append('|')
         .append(barPx)
         .append('|')
         .append(v.volumePreview >= 0 ? v.volumePreview : st.volume)
         .append('|')
         .append(st.playing)
         .append(st.shuffle)
         .append(st.repeat)
         .append(st.liked)
         .append('|')
         .append(v.hover)
         .append(v.interactive)
         .append(v.progressPreview >= 0.0)
         .append(v.volumePreview >= 0)
         .append('|')
         .append(SpotifyHudMod.media().artVersion())
         .append('|')
         .append(System.identityHashCode(v.art));
      return b.toString();
   }

   void renderLyrics(GuiGraphics ctx, int[] lb, Lyrics.Result res, PlayerState st, Config cfg, boolean interactive, boolean hover) {
      Minecraft mc = Minecraft.getInstance();
      HudPainter.LyricsView v = new HudPainter.LyricsView();
      v.interactive = interactive;
      v.hover = hover;
      long now = System.currentTimeMillis();
      switch (res.status) {
         case LOADING:
            v.message = "Lyrics werden geladen...";
            break;
         case NOT_FOUND:
            v.message = "Keine Lyrics gefunden";
            break;
         case INSTRUMENTAL:
            v.message = "♪ Instrumental ♪";
            break;
         case ERROR:
            v.message = "Lyrics-Server nicht erreichbar";
            break;
         case PLAIN:
            v.message = "Nur ungesyncte Lyrics verfügbar";
            break;
         case NONE:
            v.message = st.hasTrack ? "Lyrics werden geladen..." : "Keine Wiedergabe";
            break;
         case SYNCED:
            List<Lyrics.Line> lines = res.lines;
            long t = st.currentProgress() + cfg.lyricsOffsetMs;
            int i = Lyrics.indexAt(lines, t);
            if (!res.key.equals(this.lastLyricKey)) {
               this.lastLyricKey = res.key;
               this.lastLyricIndex = i;
               this.lyricChangedAt = 0L;
            }

            if (i != this.lastLyricIndex) {
               this.lyricChangedAt = i == this.lastLyricIndex + 1 ? now : 0L;
               this.lastLyricIndex = i;
            }

            v.prev = i - 1 >= 0 ? text(lines.get(i - 1)) : "";
            v.next = i + 1 < lines.size() ? text(lines.get(i + 1)) : "";
            if (i >= 0) {
               Lyrics.Line line = lines.get(i);
               v.curText = text(line);
               v.words = line.words;
               int wi = line.wordAt(t);
               v.wordIdx = wi;
               v.grow = wi >= 0 ? Math.min(1.0, (t - line.wordTimes[wi]) / 150.0) : 1.0;
            } else {
               v.curText = "♪";
               v.words = new String[0];
               v.wordIdx = -1;
            }

            double a = this.lyricChangedAt == 0L ? 1.0 : (now - this.lyricChangedAt) / 260.0;
            v.anim = Math.max(0.0, Math.min(1.0, a));
      }

      v.art = SpotifyHudMod.media().art();
      int s = Math.max(1, mc.getWindow().getGuiScale());
      int pw = lb[2] * s;
      int ph = lb[3] * s;
      if (!this.awtFailed && pw > 0 && ph > 0 && pw <= 8192 && ph <= 8192) {
         try {
            HudPainter.Style sty = style();
            double at = animTime();
            String fk = pw
               + "x"
               + ph
               + "|"
               + v.message
               + "|"
               + v.prev
               + "|"
               + v.curText
               + "|"
               + v.next
               + "|"
               + Math.round(v.anim * 24.0)
               + "|"
               + v.wordIdx
               + "|"
               + Math.round(v.grow * 10.0)
               + "|"
               + interactive
               + hover
               + "|"
               + sty.karaoke
               + sty.grow
               + "|"
               + System.identityHashCode(v.art);
            String k = fk + "|" + sty.theme + (Anim.active(sty.anim) && sty.animOnLyrics ? sty.anim + frame(at) + sty.intensity : "");
            this.lyricsSlot.draw(ctx, mc, lb[0], lb[1], lb[2], lb[3], pw, ph, k, () -> HudPainter.renderLyrics(v, pw, ph, sty, at, fk));
         } catch (Throwable var21) {
            this.awtFailed = true;
            SpotifyHudMod.LOG("Java2D-Rendering nicht verfügbar, nutze Fallback: " + var21);
            lyricsFallback(ctx, mc, lb, v);
         }
      } else {
         lyricsFallback(ctx, mc, lb, v);
      }
   }

   private static String text(Lyrics.Line l) {
      return l.text.isEmpty() ? "♪" : l.text;
   }

   private static void lyricsFallback(GuiGraphics ctx, Minecraft mc, int[] lb, HudPainter.LyricsView v) {
      Font tr = mc.font;
      ctx.fill(lb[0], lb[1], lb[0] + lb[2], lb[1] + lb[3], -367914478);
      int cx = lb[0] + lb[2] / 2;
      if (v.message != null) {
         ctx.drawCenteredString(tr, trim(tr, v.message, lb[2] - 8), cx, lb[1] + lb[3] / 2 - 4, -5000269);
      } else {
         ctx.drawCenteredString(tr, trim(tr, v.curText, lb[2] - 8), cx, lb[1] + lb[3] / 2 - 4, -1);
         ctx.drawCenteredString(tr, trim(tr, v.next, lb[2] - 8), cx, lb[1] + lb[3] / 2 + 7, -7697782);
      }
   }

   void renderToast(GuiGraphics ctx, int[] hb, int[] lb, int sh, String msg, long at) {
      long age = System.currentTimeMillis() - at;
      if (msg != null && !msg.isEmpty() && age <= 4000L) {
         Minecraft mc = Minecraft.getInstance();
         Font tr = mc.font;
         int alpha = age > 3400L ? (int)(255L * (4000L - age) / 600.0) : 255;
         if (alpha >= 8) {
            int bottom = Math.max(hb[1] + hb[3], lb != null ? lb[1] + lb[3] : 0);
            int y = bottom + 3;
            if (y + 12 > sh) {
               y = Math.min(hb[1], lb != null ? lb[1] : hb[1]) - 13;
            }

            String t = trim(tr, msg, Math.max(120, hb[2] + 60));
            int tw = tr.width(t);
            int x = hb[0] + hb[2] / 2 - tw / 2;
            ctx.fill(x - 4, y - 2, x + tw + 4, y + 10, alpha * 208 / 255 << 24 | 1184274);
            ctx.drawString(tr, t, x, y, alpha << 24 | 16777215);
         }
      }
   }

   private void renderFallback(GuiGraphics ctx, Minecraft mc, int x, int y, int w, int h, HudPainter.View v) {
      Font tr = mc.font;
      double k = w / 207.0;
      ctx.fill(x, y, x + w, y + h, -367914478);
      int ax = x + (int)(9.0 * k);
      int ay = y + (int)(9.0 * k);
      int as = (int)(37.0 * k);
      ctx.fill(ax, ay, ax + as, ay + as, -14145496);
      int tx = x + (int)(53.0 * k);
      int maxW = (int)(144.0 * k);
      ctx.drawString(tr, trim(tr, v.title, maxW), tx, y + (int)(10.0 * k), -1381141, false);
      if (v.showTime) {
         long dur = v.st.durationMs;
         long prog = v.progressPreview >= 0.0 ? (long)(v.progressPreview * dur) : v.st.currentProgress();
         String time = PlayerState.fmt(prog) + " / " + PlayerState.fmt(dur);
         int tw = tr.width(time);
         ctx.drawString(tr, time, x + (int)(197.0 * k) - tw, y + (int)(22.0 * k), -5789785, false);
         maxW -= tw + 6;
      }

      ctx.drawString(tr, trim(tr, v.artist, maxW), tx, y + (int)(22.0 * k), -5000269, false);
      double frac = v.progressPreview >= 0.0 ? v.progressPreview : (v.st.durationMs > 0L ? (double)v.st.currentProgress() / v.st.durationMs : 0.0);
      int px0 = x + (int)(52.5 * k);
      int py = y + (int)(36.6 * k);
      int pwid = (int)(146.5 * k);
      int ph = Math.max(2, (int)Math.round(3.4 * k));
      ctx.fill(px0, py, px0 + pwid, py + ph, -12697793);
      ctx.fill(px0, py, px0 + (int)(pwid * Math.max(0.0, Math.min(1.0, frac))), py + ph, -525833);
      if (v.showControls) {
         int vol = v.volumePreview >= 0 ? v.volumePreview : v.st.volume;
         int vx = x + (int)(21.0 * k);
         int vy = y + (int)(55.4 * k);
         int vw = (int)(37.5 * k);
         int vh = Math.max(2, (int)Math.round(3.2 * k));
         ctx.fill(vx, vy, vx + vw, vy + vh, -12697793);
         ctx.fill(vx, vy, vx + (int)(vw * Math.max(0, vol) / 100.0), vy + vh, -525833);
         int cy = y + (int)(57.0 * k) - 4;
         glyph(ctx, tr, "⏮", 88.8, x, k, cy, -1);
         glyph(ctx, tr, v.st.playing ? "⏸" : "▶", 103.3, x, k, cy, -1);
         glyph(ctx, tr, "⏭", 118.0, x, k, cy, -1);
         glyph(ctx, tr, "↻", 167.2, x, k, cy, "off".equals(v.st.repeat) ? -1 : -14756000);
         glyph(ctx, tr, "⇄", 181.3, x, k, cy, v.st.shuffle ? -14756000 : -1);
         glyph(ctx, tr, "♥", 195.8, x, k, cy, v.st.liked ? -14756000 : -1);
      }
   }

   private static void glyph(GuiGraphics ctx, Font tr, String g, double cxDesign, int x, double k, int y, int color) {
      int gw = tr.width(g);
      ctx.drawString(tr, g, x + (int)(cxDesign * k) - gw / 2, y, color, false);
   }

   static String trim(Font tr, String s, int max) {
      if (s == null) {
         return "";
      } else if (tr.width(s) <= max) {
         return s;
      } else {
         String e = "...";
         int n = s.length();

         while (n > 0 && tr.width(s.substring(0, n) + e) > max) {
            n--;
         }

         return s.substring(0, n) + e;
      }
   }
}
