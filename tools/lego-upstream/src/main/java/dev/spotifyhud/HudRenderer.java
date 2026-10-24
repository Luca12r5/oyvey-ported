package dev.spotifyhud;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1060;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

/**
 * Renders HUD + lyrics. Panels are painted with Java2D at the exact physical pixel size and uploaded to dynamic
 * textures that are drawn 1 texel = 1 screen pixel (crisp + anti-aliased).
 *
 * Intermediary names (Minecraft 1.21.11): class_310 MinecraftClient, class_332 DrawContext,
 * class_1043 NativeImageBackedTexture, class_1011 NativeImage, class_1060 TextureManager, class_2960 Identifier,
 * class_10799 RenderPipelines, class_327 TextRenderer
 */
final class HudRenderer {
    private final TexSlot hudSlot = new TexSlot(class_2960.method_60655("spotifyhud", "hud_panel"));
    private final TexSlot lyricsSlot = new TexSlot(class_2960.method_60655("spotifyhud", "lyrics_panel"));
    private boolean awtFailed;

    // lyrics animation state
    private int lastLyricIndex = Integer.MIN_VALUE;
    private String lastLyricKey = "";
    private long lyricChangedAt = 0;

    // ------------------------------------------------------------------ layout
    /** x, y, w, h in GUI units. */
    int[] bounds(int sw, int sh, Config cfg) {
        double scale = effectiveScale(sw, cfg);
        int w = Math.max(20, (int) Math.round(HudLayout.W * scale));
        int h = Math.max(7, (int) Math.round(HudLayout.H * scale));
        int x = (int) Math.round(Config.clamp01(cfg.posX) * Math.max(0, sw - w));
        int y = (int) Math.round(Config.clamp01(cfg.posY) * Math.max(0, sh - h));
        return new int[]{x, y, w, h};
    }

    private static double effectiveScale(int sw, Config cfg) {
        double scale = cfg.scale;
        if (HudLayout.W * scale > sw - 2) scale = (sw - 2) / (double) HudLayout.W;
        return scale;
    }

    /** Lyrics panel bounds: below the HUD, or above it if there is no room below. */
    int[] lyricsBounds(int[] hb, int sw, int sh, Config cfg) {
        double scale = hb[2] / (double) HudLayout.W;
        int lh = Math.max(6, (int) Math.round(HudPainter.LYRICS_H * scale));
        int gap = Math.max(1, (int) Math.round(3 * scale));
        int y = hb[1] + hb[3] + gap;
        if (y + lh > sh) y = hb[1] - gap - lh;
        return new int[]{hb[0], y, hb[2], lh};
    }

    // ------------------------------------------------------------------ HUD
    void render(class_332 ctx, int x, int y, int w, int h, HudPainter.View v) {
        class_310 mc = class_310.method_1551();
        int s = Math.max(1, mc.method_22683().method_4495());
        int pw = w * s, ph = h * s;
        if (!awtFailed && pw > 0 && ph > 0 && pw <= 8192 && ph <= 8192) {
            try {
                HudPainter.Style st = style();
                double t = animTime();
                String fk = key(v, pw, ph);
                String k = fk + "|" + st.theme + "|" + (Anim.active(st.anim) ? st.anim + frame(t) + st.intensity : "");
                hudSlot.draw(ctx, mc, x, y, w, h, pw, ph, k, () -> HudPainter.render(v, pw, ph, st, t, fk));
                return;
            } catch (Throwable t) {
                awtFailed = true;
                SpotifyHudMod.LOG("Java2D-Rendering nicht verfügbar, nutze Fallback: " + t);
            }
        }
        renderFallback(ctx, mc, x, y, w, h, v);
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

    private static final long T0 = System.currentTimeMillis();

    static double animTime() { return (System.currentTimeMillis() - T0) / 1000.0; }

    /** Animations run at 30 fps (re-uploading the texture every frame would waste CPU). */
    static long frame(double t) { return (long) (t * 30); }

    private static String key(HudPainter.View v, int pw, int ph) {
        PlayerState st = v.st;
        double pxPerUnit = pw / (double) HudLayout.W;
        long dur = st.durationMs;
        long prog = v.progressPreview >= 0 ? (long) (v.progressPreview * dur) : st.currentProgress();
        double frac = v.progressPreview >= 0 ? v.progressPreview : (dur > 0 ? prog / (double) dur : 0);
        long barPx = Math.round(frac * HudLayout.PROG_W * pxPerUnit * 2);
        StringBuilder b = new StringBuilder(160);
        b.append(pw).append('x').append(ph).append('|')
                .append(v.title).append('|').append(v.artist).append('|')
                .append(v.showTime).append(v.showControls).append('|')
                .append(v.showTime ? prog / 1000 : -1).append('|').append(dur / 1000).append('|')
                .append(barPx).append('|')
                .append(v.volumePreview >= 0 ? v.volumePreview : st.volume).append('|')
                .append(st.playing).append(st.shuffle).append(st.repeat).append(st.liked).append('|')
                .append(v.hover).append(v.interactive).append(v.progressPreview >= 0).append(v.volumePreview >= 0).append('|')
                .append(SpotifyHudMod.media().artVersion()).append('|').append(System.identityHashCode(v.art));
        return b.toString();
    }

    // ------------------------------------------------------------------ lyrics
    void renderLyrics(class_332 ctx, int[] lb, Lyrics.Result res, PlayerState st, Config cfg, boolean interactive, boolean hover) {
        class_310 mc = class_310.method_1551();
        HudPainter.LyricsView v = new HudPainter.LyricsView();
        v.interactive = interactive;
        v.hover = hover;
        long now = System.currentTimeMillis();
        switch (res.status) {
            case LOADING: v.message = "Lyrics werden geladen..."; break;
            case NOT_FOUND: v.message = "Keine Lyrics gefunden"; break;
            case INSTRUMENTAL: v.message = "♪ Instrumental ♪"; break;
            case ERROR: v.message = "Lyrics-Server nicht erreichbar"; break;
            case PLAIN: v.message = "Nur ungesyncte Lyrics verfügbar"; break;
            case NONE: v.message = st.hasTrack ? "Lyrics werden geladen..." : "Keine Wiedergabe"; break;
            case SYNCED: {
                List<Lyrics.Line> lines = res.lines;
                long t = st.currentProgress() + cfg.lyricsOffsetMs;
                int i = Lyrics.indexAt(lines, t);
                if (!res.key.equals(lastLyricKey)) { lastLyricKey = res.key; lastLyricIndex = i; lyricChangedAt = 0; }
                if (i != lastLyricIndex) {
                    // animate only for forward steps of one line; jumps (seek) switch instantly
                    lyricChangedAt = (i == lastLyricIndex + 1) ? now : 0;
                    lastLyricIndex = i;
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
                    // after the line is fully sung everything stays "sung"
                } else {
                    v.curText = "\u266A";
                    v.words = new String[0];
                    v.wordIdx = -1;
                }
                double a = lyricChangedAt == 0 ? 1.0 : (now - lyricChangedAt) / 260.0;
                v.anim = Math.max(0, Math.min(1, a));
                break;
            }
            default: break;
        }
        v.art = SpotifyHudMod.media().art();
        int s = Math.max(1, mc.method_22683().method_4495());
        int pw = lb[2] * s, ph = lb[3] * s;
        if (awtFailed || pw <= 0 || ph <= 0 || pw > 8192 || ph > 8192) {
            lyricsFallback(ctx, mc, lb, v);
            return;
        }
        try {
            HudPainter.Style sty = style();
            double at = animTime();
            String fk = pw + "x" + ph + "|" + v.message + "|" + v.prev + "|" + v.curText + "|" + v.next + "|"
                    + Math.round(v.anim * 24) + "|" + v.wordIdx + "|" + Math.round(v.grow * 10) + "|" + interactive + hover
                    + "|" + sty.karaoke + sty.grow + "|" + System.identityHashCode(v.art);
            String k = fk + "|" + sty.theme + (Anim.active(sty.anim) && sty.animOnLyrics ? sty.anim + frame(at) + sty.intensity : "");
            final HudPainter.LyricsView fv = v;
            lyricsSlot.draw(ctx, mc, lb[0], lb[1], lb[2], lb[3], pw, ph, k, () -> HudPainter.renderLyrics(fv, pw, ph, sty, at, fk));
        } catch (Throwable t) {
            awtFailed = true;
            SpotifyHudMod.LOG("Java2D-Rendering nicht verfügbar, nutze Fallback: " + t);
            lyricsFallback(ctx, mc, lb, v);
        }
    }

    private static String text(Lyrics.Line l) {
        return l.text.isEmpty() ? "♪" : l.text;
    }

    private static void lyricsFallback(class_332 ctx, class_310 mc, int[] lb, HudPainter.LyricsView v) {
        class_327 tr = mc.field_1772;
        ctx.method_25294(lb[0], lb[1], lb[0] + lb[2], lb[1] + lb[3], 0xEA121212);
        int cx = lb[0] + lb[2] / 2;
        if (v.message != null) {
            ctx.method_25300(tr, trim(tr, v.message, lb[2] - 8), cx, lb[1] + lb[3] / 2 - 4, 0xFFB3B3B3);
            return;
        }
        ctx.method_25300(tr, trim(tr, v.curText, lb[2] - 8), cx, lb[1] + lb[3] / 2 - 4, 0xFFFFFFFF);
        ctx.method_25300(tr, trim(tr, v.next, lb[2] - 8), cx, lb[1] + lb[3] / 2 + 7, 0xFF8A8A8A);
    }

    // ------------------------------------------------------------------ toast
    void renderToast(class_332 ctx, int[] hb, int[] lb, int sh, String msg, long at) {
        long age = System.currentTimeMillis() - at;
        if (msg == null || msg.isEmpty() || age > 4000) return;
        class_310 mc = class_310.method_1551();
        class_327 tr = mc.field_1772;
        int alpha = age > 3400 ? (int) (255 * (4000 - age) / 600.0) : 255;
        if (alpha < 8) return;
        int bottom = Math.max(hb[1] + hb[3], lb != null ? lb[1] + lb[3] : 0);
        int y = bottom + 3;
        if (y + 12 > sh) y = Math.min(hb[1], lb != null ? lb[1] : hb[1]) - 13;
        String t = trim(tr, msg, Math.max(120, hb[2] + 60));
        int tw = tr.method_1727(t);
        int x = hb[0] + hb[2] / 2 - tw / 2;
        ctx.method_25294(x - 4, y - 2, x + tw + 4, y + 10, ((alpha * 0xD0 / 255) << 24) | 0x121212);
        ctx.method_25303(tr, t, x, y, (alpha << 24) | 0xFFFFFF);
    }

    // ------------------------------------------------------------------ fallback (only if AWT is unavailable)
    private void renderFallback(class_332 ctx, class_310 mc, int x, int y, int w, int h, HudPainter.View v) {
        class_327 tr = mc.field_1772;
        double k = w / (double) HudLayout.W;
        ctx.method_25294(x, y, x + w, y + h, 0xEA121212);
        int ax = x + (int) (HudLayout.ART_X * k), ay = y + (int) (HudLayout.ART_Y * k), as = (int) (HudLayout.ART_S * k);
        ctx.method_25294(ax, ay, ax + as, ay + as, 0xFF282828);
        int tx = x + (int) (HudLayout.TEXT_X * k);
        int maxW = (int) ((HudLayout.TEXT_RIGHT - HudLayout.TEXT_X) * k);
        ctx.method_51433(tr, trim(tr, v.title, maxW), tx, y + (int) (10 * k), 0xFFEAECEB, false);
        if (v.showTime) {
            long dur = v.st.durationMs;
            long prog = v.progressPreview >= 0 ? (long) (v.progressPreview * dur) : v.st.currentProgress();
            String time = PlayerState.fmt(prog) + " / " + PlayerState.fmt(dur);
            int tw = tr.method_1727(time);
            ctx.method_51433(tr, time, x + (int) (HudLayout.TEXT_RIGHT * k) - tw, y + (int) (22 * k), 0xFFA7A7A7, false);
            maxW -= tw + 6;
        }
        ctx.method_51433(tr, trim(tr, v.artist, maxW), tx, y + (int) (22 * k), 0xFFB3B3B3, false);
        double frac = v.progressPreview >= 0 ? v.progressPreview
                : (v.st.durationMs > 0 ? v.st.currentProgress() / (double) v.st.durationMs : 0);
        int px0 = x + (int) (HudLayout.PROG_X * k), py = y + (int) (HudLayout.PROG_Y * k), pwid = (int) (HudLayout.PROG_W * k);
        int ph = Math.max(2, (int) Math.round(HudLayout.PROG_H * k));
        ctx.method_25294(px0, py, px0 + pwid, py + ph, 0xFF3E3F3F);
        ctx.method_25294(px0, py, px0 + (int) (pwid * Math.max(0, Math.min(1, frac))), py + ph, 0xFFF7F9F7);
        if (!v.showControls) return;
        int vol = v.volumePreview >= 0 ? v.volumePreview : v.st.volume;
        int vx = x + (int) (HudLayout.VOL_X * k), vy = y + (int) (HudLayout.VOL_Y * k), vw = (int) (HudLayout.VOL_W * k);
        int vh = Math.max(2, (int) Math.round(HudLayout.VOL_H * k));
        ctx.method_25294(vx, vy, vx + vw, vy + vh, 0xFF3E3F3F);
        ctx.method_25294(vx, vy, vx + (int) (vw * Math.max(0, vol) / 100.0), vy + vh, 0xFFF7F9F7);
        int cy = y + (int) (HudLayout.ROW_Y * k) - 4;
        glyph(ctx, tr, "⏮", HudLayout.PREV_X, x, k, cy, 0xFFFFFFFF);
        glyph(ctx, tr, v.st.playing ? "⏸" : "▶", HudLayout.PLAY_X, x, k, cy, 0xFFFFFFFF);
        glyph(ctx, tr, "⏭", HudLayout.NEXT_X, x, k, cy, 0xFFFFFFFF);
        glyph(ctx, tr, "↻", HudLayout.REPEAT_X, x, k, cy, "off".equals(v.st.repeat) ? 0xFFFFFFFF : 0xFF1ED760);
        glyph(ctx, tr, "⇄", HudLayout.SHUFFLE_X, x, k, cy, v.st.shuffle ? 0xFF1ED760 : 0xFFFFFFFF);
        glyph(ctx, tr, "♥", HudLayout.LIKE_X, x, k, cy, v.st.liked ? 0xFF1ED760 : 0xFFFFFFFF);
    }

    private static void glyph(class_332 ctx, class_327 tr, String g, double cxDesign, int x, double k, int y, int color) {
        int gw = tr.method_1727(g);
        ctx.method_51433(tr, g, x + (int) (cxDesign * k) - gw / 2, y, color, false);
    }

    static String trim(class_327 tr, String s, int max) {
        if (s == null) return "";
        if (tr.method_1727(s) <= max) return s;
        String e = "...";
        int n = s.length();
        while (n > 0 && tr.method_1727(s.substring(0, n) + e) > max) n--;
        return s.substring(0, n) + e;
    }
}
