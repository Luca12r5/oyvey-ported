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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static dev.spotifyhud.HudLayout.*;

/**
 * Draws HUD + lyrics with Java2D (anti-aliased, pixel exact at the current GUI scale).
 * Layers: theme background (cached) -> animation (per frame) -> foreground (cached by key).
 */
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
    static final double R = 15;   // panel corner arc

    private static String family;           // primary UI font family
    private static String semiboldFamily;   // e.g. "Segoe UI Semibold"
    private static final Map<String, Font> FONT_CACHE = new HashMap<>();

    private HudPainter() { }

    /** Look settings shared by HUD and lyrics. */
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
        String title, artist;
        boolean showTime, showControls;
        HudLayout.Region hover;
        boolean interactive;
        double progressPreview = -1;  // 0..1
        int volumePreview = -1;       // 0..100
    }

    static final class LyricsView {
        String prev = "", next = "";
        String[] words = new String[0];  // current line
        String curText = "";
        int wordIdx = -1;               // word being sung
        double grow = 1.0;              // 0..1 growth animation of the active word
        double anim = 1.0;              // 0 = line just changed, 1 = settled
        String message;                 // status text instead of lines
        boolean interactive, hover;
        Object art;
    }

    // ------------------------------------------------------------------ fonts
    private static void initFonts() {
        if (family != null) return;
        Set<String> names = new HashSet<>();
        try {
            names.addAll(Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        } catch (Throwable ignored) { }
        String[] prefs = {"Segoe UI", "Helvetica Neue", "Arial", "Liberation Sans", "DejaVu Sans"};
        family = Font.SANS_SERIF;
        for (String p : prefs) if (names.contains(p)) { family = p; break; }
        semiboldFamily = names.contains("Segoe UI Semibold") ? "Segoe UI Semibold" : null;
    }

    public static Font font(String text, boolean title, float size) {
        initFonts();
        String fam; int style;
        if (title && semiboldFamily != null) { fam = semiboldFamily; style = Font.PLAIN; }
        else { fam = family; style = title ? Font.BOLD : Font.PLAIN; }
        String key = fam + style + size;
        Font f = FONT_CACHE.computeIfAbsent(key, k -> {
            Map<TextAttribute, Object> attrs = new HashMap<>();
            attrs.put(TextAttribute.FAMILY, fam);
            attrs.put(TextAttribute.SIZE, size);
            if (style == Font.BOLD) attrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
            if (title) attrs.put(TextAttribute.TRACKING, 0.02f);
            return new Font(attrs);
        });
        if (text != null && f.canDisplayUpTo(text) != -1) {
            Map<TextAttribute, Object> attrs = new HashMap<>();
            attrs.put(TextAttribute.FAMILY, Font.DIALOG);
            attrs.put(TextAttribute.SIZE, size);
            if (title) attrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
            return new Font(attrs);
        }
        return f;
    }

    /** Plain UI font (for the settings screen). */
    public static Font uiFont(boolean bold, float size) {
        initFonts();
        String key = "ui" + family + bold + size;
        return FONT_CACHE.computeIfAbsent(key, k -> {
            Map<TextAttribute, Object> attrs = new HashMap<>();
            attrs.put(TextAttribute.FAMILY, bold && semiboldFamily != null ? semiboldFamily : family);
            attrs.put(TextAttribute.SIZE, size);
            if (bold && semiboldFamily == null) attrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
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
        if (img == null || img.getWidth() != w || img.getHeight() != h) return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        return img;
    }

    // ------------------------------------------------------------------ layered composition
    private static final class Layers {
        BufferedImage canvas, bg, fg;
        String bgKey = "", fgKey = "";
    }

    private static final Layers HUD = new Layers();
    private static final Layers LYR = new Layers();

    private static int[] compose(Layers L, int pw, int ph, double dh, Style s, Object art, boolean anim, double t,
                                 String fgKey, java.util.function.Consumer<Graphics2D> fgPainter) {
        BufferedImage artImg = art instanceof BufferedImage ? (BufferedImage) art : null;
        double px = pw / (double) W;
        String bgKey = s.theme + "|" + pw + "x" + ph + "|" + ("cover".equals(s.theme) ? System.identityHashCode(artImg) : 0);
        if (L.bg == null || !bgKey.equals(L.bgKey)) {
            L.bg = ensure(L.bg, pw, ph);
            Graphics2D g = clear(L.bg);
            g.scale(px, ph / dh);
            Theme.paint(g, s.theme, new RoundRectangle2D.Double(0, 0, W, dh, R, R), W, dh, artImg, px);
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
            Graphics2D a = (Graphics2D) g.create();
            a.scale(px, ph / dh);
            a.clip(new RoundRectangle2D.Double(0, 0, W, dh, R, R));
            try {
                Anim.draw(a, s.anim, t, W, dh, s.intensity, Theme.accent(s.theme, artImg));
            } finally {
                a.dispose();
            }
        }
        g.drawImage(L.fg, 0, 0, null);
        g.dispose();
        return L.canvas.getRGB(0, 0, pw, ph, null, 0, pw);
    }

    // ------------------------------------------------------------------ HUD
    static int[] render(View v, int pw, int ph, Style s, double t, String fgKey) {
        Color accent = Theme.accent(s.theme, v.art instanceof BufferedImage ? (BufferedImage) v.art : null);
        return compose(HUD, pw, ph, H, s, v.art, true, t, fgKey + "|" + s.theme + accent.getRGB(),
                g -> drawHud(g, v, pw / (double) W, accent));
    }

    private static void drawHud(Graphics2D g, View v, double px, Color accent) {
        PlayerState st = v.st;
        // ---- edge
        g.setColor(v.interactive && (v.hover == Region.PANEL || v.hover == Region.SETTINGS) ? PANEL_EDGE_HOVER : PANEL_EDGE);
        g.setStroke(new BasicStroke((float) (1.0 / px)));
        double e = 0.5 / px;
        g.draw(new RoundRectangle2D.Double(e, e, W - 2 * e, H - 2 * e, R, R));

        // ---- album art
        RoundRectangle2D artClip = new RoundRectangle2D.Double(ART_X, ART_Y, ART_S, ART_S, 8, 8);
        if (v.art instanceof BufferedImage) {
            BufferedImage a = (BufferedImage) v.art;
            int target = Math.max(1, (int) Math.round(ART_S * px));
            BufferedImage rounded = roundedArt(a, target, 4.0 * px);
            AffineTransform at = new AffineTransform();
            at.translate(ART_X, ART_Y);
            at.scale(ART_S / rounded.getWidth(), ART_S / rounded.getHeight());
            g.drawImage(rounded, at, null);
        } else {
            g.setColor(ART_EMPTY);
            g.fill(artClip);
            drawNote(g, ART_X + ART_S / 2, ART_Y + ART_S / 2);
        }

        // ---- texts
        double timeW = 0;
        if (v.showTime) {
            long dur = st.durationMs;
            long prog = v.progressPreview >= 0 ? (long) (v.progressPreview * dur) : st.currentProgress();
            String time = PlayerState.fmt(prog) + " / " + PlayerState.fmt(dur);
            g.setFont(font(time, false, 7.8f));
            timeW = g.getFontMetrics().getStringBounds(time, g).getWidth();
            g.setColor(TIME);
            g.drawString(time, (float) (TEXT_RIGHT - timeW), (float) ARTIST_BASE);
        }
        String title = v.title == null ? "" : v.title;
        g.setFont(font(title, true, 9.9f));
        g.setColor(TITLE);
        double titleMax = TEXT_RIGHT - TEXT_X + 1 - (v.interactive ? 9 : 0);
        g.drawString(ellipsize(g, title, titleMax), (float) TEXT_X, (float) TITLE_BASE);

        String artist = v.artist == null ? "" : v.artist;
        g.setFont(font(artist, false, 8.1f));
        g.setColor(ARTIST);
        double artistMax = TEXT_RIGHT - TEXT_X - (timeW > 0 ? timeW + 6 : 0);
        g.drawString(ellipsize(g, artist, artistMax), (float) TEXT_X, (float) ARTIST_BASE);

        // ---- settings gear (only when the mouse is free: chat / settings)
        if (v.interactive) {
            if (v.hover == Region.SETTINGS) {
                g.setColor(HOVER_BG);
                g.fill(new Ellipse2D.Double(GEAR_X - 5, GEAR_Y - 5, 10, 10));
            }
            g.setColor(v.hover == Region.SETTINGS ? ICON : new Color(255, 255, 255, 150));
            drawGear(g, GEAR_X, GEAR_Y, 3.1);
        }

        // ---- progress bar
        double frac = v.progressPreview >= 0 ? v.progressPreview
                : (st.durationMs > 0 ? st.currentProgress() / (double) st.durationMs : 0);
        boolean progHover = v.interactive && (v.hover == Region.PROGRESS || v.progressPreview >= 0);
        bar(g, PROG_X, PROG_Y, PROG_W, PROG_H, frac, progHover, accent);

        // ---- controls row
        if (v.showControls) {
            int vol = v.volumePreview >= 0 ? v.volumePreview : st.volume;
            boolean volHover = v.interactive && (v.hover == Region.VOLUME || v.volumePreview >= 0);
            hoverBg(g, v, Region.SPEAKER, SPEAKER_X);
            drawSpeaker(g, SPEAKER_X, ROW_Y, vol);
            bar(g, VOL_X, VOL_Y, VOL_W, VOL_H, vol < 0 ? 0 : vol / 100.0, volHover, accent);

            hoverBg(g, v, Region.PREV, PREV_X);
            hoverBg(g, v, Region.PLAY, PLAY_X);
            hoverBg(g, v, Region.NEXT, NEXT_X);
            hoverBg(g, v, Region.REPEAT, REPEAT_X);
            hoverBg(g, v, Region.SHUFFLE, SHUFFLE_X);
            hoverBg(g, v, Region.LIKE, LIKE_X);

            g.setColor(ICON);
            drawPrev(g, PREV_X, ROW_Y);
            if (st.playing) drawPause(g, PLAY_X, ROW_Y); else drawPlay(g, PLAY_X, ROW_Y);
            drawNext(g, NEXT_X, ROW_Y);

            boolean rep = !"off".equals(st.repeat);
            g.setColor(rep ? accent : ICON);
            drawRepeat(g, REPEAT_X, ROW_Y, "track".equals(st.repeat));
            if (rep) dot(g, REPEAT_X);

            g.setColor(st.shuffle ? accent : ICON);
            drawShuffle(g, SHUFFLE_X, ROW_Y);
            if (st.shuffle) dot(g, SHUFFLE_X);

            g.setColor(st.liked ? accent : ICON);
            drawHeart(g, LIKE_X, ROW_Y);
        }
    }

    public static void drawGear(Graphics2D g, double cx, double cy, double r) {
        Path2D p = new Path2D.Double();
        int teeth = 8;
        for (int i = 0; i < teeth * 2; i++) {
            double a0 = Math.PI * 2 * i / (teeth * 2), a1 = Math.PI * 2 * (i + 1) / (teeth * 2);
            double rr = (i % 2 == 0) ? r : r * 0.76;
            double x0 = cx + Math.cos(a0) * rr, y0 = cy + Math.sin(a0) * rr;
            double x1 = cx + Math.cos(a1) * rr, y1 = cy + Math.sin(a1) * rr;
            if (i == 0) p.moveTo(x0, y0); else p.lineTo(x0, y0);
            p.lineTo(x1, y1);
        }
        p.closePath();
        java.awt.geom.Area area = new java.awt.geom.Area(p);
        area.subtract(new java.awt.geom.Area(new Ellipse2D.Double(cx - r * 0.36, cy - r * 0.36, r * 0.72, r * 0.72)));
        g.fill(area);
    }

    // ------------------------------------------------------------------ lyrics (karaoke)
    static int[] renderLyrics(LyricsView v, int pw, int ph, Style s, double t, String fgKey) {
        Color accent = Theme.accent(s.theme, v.art instanceof BufferedImage ? (BufferedImage) v.art : null);
        return compose(LYR, pw, ph, LYRICS_H, s, v.art, s.animOnLyrics, t, fgKey + "|" + s.theme + accent.getRGB(),
                g -> drawLyrics(g, v, pw / (double) W, s, accent));
    }

    private static void drawLyrics(Graphics2D g, LyricsView v, double px, Style s, Color accent) {
        g.setColor(v.interactive && v.hover ? PANEL_EDGE_HOVER : PANEL_EDGE);
        g.setStroke(new BasicStroke((float) (1.0 / px)));
        double e = 0.5 / px;
        g.draw(new RoundRectangle2D.Double(e, e, W - 2 * e, LYRICS_H - 2 * e, R, R));
        Shape oldClip = g.getClip();
        g.clip(new java.awt.geom.Rectangle2D.Double(3, 1.5, W - 6, LYRICS_H - 3));
        double maxW = W - 20;
        if (v.message != null) {
            g.setFont(font(v.message, false, 8.2f));
            g.setColor(ARTIST);
            centered(g, ellipsize(g, v.message, maxW), 23.5);
            g.setClip(oldClip);
            return;
        }
        double a = Math.max(0, Math.min(1, v.anim));
        double ease = 1 - Math.pow(1 - a, 3);
        double shift = (1 - ease) * 12.0;

        // layout of the current line (1 or 2 rows)
        String[] words = v.words.length > 0 ? v.words : new String[]{v.curText.isEmpty() ? "♪" : v.curText};
        String joined = String.join(" ", words);
        float size = 9.6f;
        g.setFont(font(joined, true, size));
        while (size > 7.4f && width(g, words) > maxW) { size -= 0.3f; g.setFont(font(joined, true, size)); }
        int split = words.length;
        if (width(g, words) > maxW && words.length > 1) split = splitPoint(g, words);
        boolean twoRows = split < words.length;

        if (!twoRows) drawLine(g, v.prev, false, 11.0 + shift, 0.42 * ease, maxW);
        double[] bases = twoRows ? new double[]{19.2 + shift, 29.0 + shift} : new double[]{24.5 + shift};
        int[][] rows = twoRows ? new int[][]{{0, split}, {split, words.length}} : new int[][]{{0, words.length}};
        float alphaCur = (float) (0.55 + 0.45 * ease);
        for (int r = 0; r < rows.length; r++) {
            drawKaraokeRow(g, words, rows[r][0], rows[r][1], bases[r], v, s, accent, alphaCur, size);
        }
        drawLine(g, v.next, false, (twoRows ? 37.6 : 35.5) + shift, 0.55 * ease, maxW);
        g.setClip(oldClip);
    }

    private static double width(Graphics2D g, String[] words) {
        FontMetrics fm = g.getFontMetrics();
        double w = 0, sp = fm.getStringBounds(" ", g).getWidth();
        for (int i = 0; i < words.length; i++) w += fm.getStringBounds(words[i], g).getWidth() + (i > 0 ? sp : 0);
        return w;
    }

    private static int splitPoint(Graphics2D g, String[] words) {
        double total = width(g, words), acc = 0, sp = g.getFontMetrics().getStringBounds(" ", g).getWidth();
        for (int i = 0; i < words.length; i++) {
            acc += g.getFontMetrics().getStringBounds(words[i], g).getWidth() + sp;
            if (acc >= total / 2) return Math.max(1, Math.min(words.length - 1, i + 1));
        }
        return words.length / 2;
    }

    private static void drawKaraokeRow(Graphics2D g, String[] words, int from, int to, double baseline, LyricsView v,
                                       Style s, Color accent, float alpha, float size) {
        FontMetrics fm = g.getFontMetrics();
        double sp = fm.getStringBounds(" ", g).getWidth();
        double growK = s.grow ? 1 + 0.18 * easeOut(v.grow) : 1.0;
        double[] ww = new double[to - from];
        double[] slot = new double[to - from];   // width reserved for each word (active word is bigger)
        double total = 0;
        for (int i = from; i < to; i++) {
            ww[i - from] = fm.getStringBounds(words[i], g).getWidth();
            slot[i - from] = ww[i - from] * ((s.karaoke && i == v.wordIdx) ? growK : 1.0);
            total += slot[i - from] + (i > from ? sp : 0);
        }
        double maxW = W - 20;
        double x = (W - Math.min(total, maxW)) / 2.0;

        // highlight the whole line (soft pill behind the row)
        double padX = 4.5, hgt = size * 1.45;
        Color pill = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int) (38 * alpha));
        g.setColor(pill);
        g.fill(new RoundRectangle2D.Double(x - padX, baseline - size * 1.02, Math.min(total, maxW) + 2 * padX, hgt, hgt, hgt));

        for (int i = from; i < to; i++) {
            String w = words[i];
            double wid = ww[i - from];
            double wx = x + (slot[i - from] - wid) / 2;
            Color c;
            boolean active = s.karaoke && i == v.wordIdx;
            if (!s.karaoke) c = TITLE;
            else if (i < v.wordIdx) c = TITLE;
            else if (active) c = accent;
            else c = new Color(255, 255, 255, 120);
            if (active) {
                double k = growK;
                AffineTransform old = g.getTransform();
                double cx = wx + wid / 2, cy = baseline - size * 0.35;
                g.translate(cx, cy);
                g.scale(k, k);
                g.translate(-cx, -cy);
                // soft glow
                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (70 * alpha)));
                for (double[] o : new double[][]{{-0.35, 0}, {0.35, 0}, {0, -0.35}, {0, 0.35}})
                    g.drawString(w, (float) (wx + o[0]), (float) (baseline + o[1]));
                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255 * alpha)));
                g.drawString(w, (float) wx, (float) baseline);
                g.setTransform(old);
            } else {
                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (c.getAlpha() * alpha)));
                g.drawString(w, (float) wx, (float) baseline);
            }
            x += slot[i - from] + sp;
        }
    }

    private static double easeOut(double a) {
        a = Math.max(0, Math.min(1, a));
        return 1 - Math.pow(1 - a, 3);
    }

    private static void drawLine(Graphics2D g, String text, boolean current, double baseline, double alpha, double maxW) {
        if (text == null || text.isEmpty() || alpha <= 0.01) return;
        float size = current ? 9.6f : 7.6f;
        g.setFont(font(text, current, size));
        Color c = current ? TITLE : ARTIST;
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) Math.round(255 * Math.min(1, alpha))));
        centered(g, ellipsize(g, text, maxW), baseline);
    }

    private static void centered(Graphics2D g, String text, double baseline) {
        double w = g.getFontMetrics().getStringBounds(text, g).getWidth();
        g.drawString(text, (float) ((W - w) / 2.0), (float) baseline);
    }

    private static BufferedImage lastArtSrc, lastArtScaled;
    private static int lastArtSize;

    private static BufferedImage roundedArt(BufferedImage src, int size, double radius) {
        if (src == lastArtSrc && size == lastArtSize && lastArtScaled != null) return lastArtScaled;
        BufferedImage scaled = scaledArt(src, size);
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(0, 0, size, size, radius * 2, radius * 2));
        g.setComposite(AlphaComposite.SrcIn);
        g.drawImage(scaled, 0, 0, null);
        g.dispose();
        lastArtSrc = src; lastArtSize = size; lastArtScaled = out;
        return out;
    }

    private static BufferedImage scaledArt(BufferedImage src, int size) {
        BufferedImage cur = src;
        int w = src.getWidth(), h = src.getHeight();
        // progressive halving for smooth downscaling
        while (w / 2 >= size && h / 2 >= size) {
            w /= 2; h /= 2;
            cur = resize(cur, w, h);
        }
        if (w != size || h != size) cur = resize(cur, size, size);
        return cur;
    }

    private static BufferedImage resize(BufferedImage src, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    private static String ellipsize(java.awt.Graphics2D g, String s, double max) {
        FontMetrics fm = g.getFontMetrics();
        if (fm.getStringBounds(s, g).getWidth() <= max) return s;
        String ell = "…";
        int lo = 0, hi = s.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (fm.getStringBounds(s.substring(0, mid).trim() + ell, g).getWidth() <= max) lo = mid; else hi = mid - 1;
        }
        return s.substring(0, lo).trim() + ell;
    }

    private static void bar(java.awt.Graphics2D g, double x, double y, double w, double h, double frac, boolean hover, Color accent) {
        frac = Math.max(0, Math.min(1, frac));
        g.setColor(TRACK);
        g.fill(new RoundRectangle2D.Double(x, y, w, h, h, h));
        double fw = w * frac;
        if (fw >= 0.35) {
            g.setColor(hover ? accent : FILL);
            double arc = Math.min(h, fw);
            g.fill(new RoundRectangle2D.Double(x, y, fw, h, arc, h));
        }
        if (hover) {
            double cx = x + w * frac, cy = y + h / 2, r = 3.0;
            g.setColor(new Color(0, 0, 0, 70));
            g.fill(new Ellipse2D.Double(cx - r - 0.2, cy - r + 0.4, 2 * r + 0.4, 2 * r + 0.4));
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        }
    }

    private static void hoverBg(java.awt.Graphics2D g, View v, Region r, double cx) {
        if (!v.interactive || v.hover != r) return;
        g.setColor(HOVER_BG);
        g.fill(new Ellipse2D.Double(cx - 5.6, ROW_Y - 5.6, 11.2, 11.2));
    }

    private static void dot(java.awt.Graphics2D g, double cx) {
        g.fill(new Ellipse2D.Double(cx - 0.65, ROW_Y + 4.1, 1.3, 1.3));
    }

    // ------------------------------------------------------------------ icons (centered at cx, cy)
    private static void drawPause(java.awt.Graphics2D g, double cx, double cy) {
        double h = 4.8, w = 1.35, gap = 1.35;
        g.fill(new RoundRectangle2D.Double(cx - gap / 2 - w, cy - h / 2, w, h, 0.5, 0.5));
        g.fill(new RoundRectangle2D.Double(cx + gap / 2, cy - h / 2, w, h, 0.5, 0.5));
    }

    private static void drawPlay(java.awt.Graphics2D g, double cx, double cy) {
        Path2D p = new Path2D.Double();
        double h = 5.2, w = 4.6;
        p.moveTo(cx - w / 2 + 0.4, cy - h / 2);
        p.lineTo(cx + w / 2 + 0.4, cy);
        p.lineTo(cx - w / 2 + 0.4, cy + h / 2);
        p.closePath();
        g.fill(p);
    }

    private static void drawPrev(java.awt.Graphics2D g, double cx, double cy) {
        double h = 4.6;
        g.fill(new RoundRectangle2D.Double(cx - 2.9, cy - h / 2, 1.0, h, 0.4, 0.4));
        Path2D p = new Path2D.Double();
        p.moveTo(cx + 2.7, cy - h / 2);
        p.lineTo(cx - 1.7, cy);
        p.lineTo(cx + 2.7, cy + h / 2);
        p.closePath();
        g.fill(p);
    }

    private static void drawNext(java.awt.Graphics2D g, double cx, double cy) {
        double h = 4.6;
        g.fill(new RoundRectangle2D.Double(cx + 1.9, cy - h / 2, 1.0, h, 0.4, 0.4));
        Path2D p = new Path2D.Double();
        p.moveTo(cx - 2.7, cy - h / 2);
        p.lineTo(cx + 1.7, cy);
        p.lineTo(cx - 2.7, cy + h / 2);
        p.closePath();
        g.fill(p);
    }

    private static void drawRepeat(java.awt.Graphics2D g, double cx, double cy, boolean one) {
        double w = 5.4, h = 4.0;
        g.setStroke(new BasicStroke(0.95f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D p = new Path2D.Double();
        double l = cx - w / 2, r = cx + w / 2, t = cy - h / 2, b = cy + h / 2, rad = 1.0;
        // loop, with a gap on the top-right where the arrow sits
        p.moveTo(r - 1.9, t);
        p.lineTo(l + rad, t);
        p.quadTo(l, t, l, t + rad);
        p.lineTo(l, b - rad);
        p.quadTo(l, b, l + rad, b);
        p.lineTo(r - rad, b);
        p.quadTo(r, b, r, b - rad);
        p.lineTo(r, t + rad + 0.3);
        g.draw(p);
        // arrow head on the top edge pointing right
        Path2D a = new Path2D.Double();
        a.moveTo(r - 1.9, t - 1.35);
        a.lineTo(r - 0.2, t);
        a.lineTo(r - 1.9, t + 1.35);
        a.closePath();
        g.fill(a);
        if (one) {
            Font f = new Font(Font.SANS_SERIF, Font.BOLD, 1).deriveFont(3.2f);
            g.setFont(f);
            FontMetrics fm = g.getFontMetrics();
            String s = "1";
            double sw = fm.getStringBounds(s, g).getWidth();
            g.drawString(s, (float) (cx - sw / 2), (float) (cy + 1.15));
        }
    }

    private static void drawShuffle(java.awt.Graphics2D g, double cx, double cy) {
        g.setStroke(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double l = cx - 2.9, r = cx + 2.1, t = cy - 1.7, b = cy + 1.7;
        Path2D p1 = new Path2D.Double();
        p1.moveTo(l, t);
        p1.curveTo(cx - 0.8, t, cx - 0.4, b, r, b);
        g.draw(p1);
        Path2D p2 = new Path2D.Double();
        p2.moveTo(l, b);
        p2.curveTo(cx - 0.8, b, cx - 0.4, t, r, t);
        g.draw(p2);
        arrowHead(g, r + 0.9, t);
        arrowHead(g, r + 0.9, b);
    }

    private static void arrowHead(java.awt.Graphics2D g, double tipX, double y) {
        Path2D a = new Path2D.Double();
        a.moveTo(tipX - 1.6, y - 1.25);
        a.lineTo(tipX, y);
        a.lineTo(tipX - 1.6, y + 1.25);
        a.closePath();
        g.fill(a);
    }

    private static void drawHeart(java.awt.Graphics2D g, double cx, double cy) {
        double w = 5.4, h = 4.7;
        double x = cx - w / 2, y = cy - h / 2 + 0.1;
        Path2D p = new Path2D.Double();
        p.moveTo(cx, y + h);
        p.curveTo(x - 0.2, y + h * 0.55, x - 0.1, y + 0.1, x + w * 0.27, y);
        p.curveTo(x + w * 0.42, y - 0.05, cx - 0.05, y + 0.6, cx, y + 1.1);
        p.curveTo(cx + 0.05, y + 0.6, x + w * 0.58, y - 0.05, x + w * 0.73, y);
        p.curveTo(x + w + 0.1, y + 0.1, x + w + 0.2, y + h * 0.55, cx, y + h);
        p.closePath();
        g.fill(p);
    }

    private static void drawSpeaker(java.awt.Graphics2D g, double cx, double cy, int vol) {
        g.setColor(SPEAKER);
        Path2D p = new Path2D.Double();
        double x0 = cx - 3.0;
        p.moveTo(x0, cy - 1.1);
        p.lineTo(x0 + 1.3, cy - 1.1);
        p.lineTo(x0 + 3.0, cy - 2.6);
        p.lineTo(x0 + 3.0, cy + 2.6);
        p.lineTo(x0 + 1.3, cy + 1.1);
        p.lineTo(x0, cy + 1.1);
        p.closePath();
        g.fill(p);
        g.setStroke(new BasicStroke(0.75f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (vol == 0) {
            double mx = cx + 1.7;
            g.draw(new java.awt.geom.Line2D.Double(mx - 0.1, cy - 1.2, mx + 2.2, cy + 1.2));
            g.draw(new java.awt.geom.Line2D.Double(mx - 0.1, cy + 1.2, mx + 2.2, cy - 1.2));
        } else {
            g.draw(new java.awt.geom.Arc2D.Double(cx - 1.4, cy - 1.6, 3.2, 3.2, -55, 110, java.awt.geom.Arc2D.OPEN));
            if (vol > 50 || vol < 0)
                g.draw(new java.awt.geom.Arc2D.Double(cx - 2.1, cy - 2.8, 5.6, 5.6, -55, 110, java.awt.geom.Arc2D.OPEN));
        }
    }

    private static void drawNote(java.awt.Graphics2D g, double cx, double cy) {
        g.setColor(NOTE);
        g.fill(new Ellipse2D.Double(cx - 5.5, cy + 2.2, 4.4, 3.4));
        g.fill(new Ellipse2D.Double(cx + 1.6, cy + 0.8, 4.4, 3.4));
        g.setStroke(new BasicStroke(1.1f));
        g.draw(new java.awt.geom.Line2D.Double(cx - 1.6, cy + 3.8, cx - 1.6, cy - 5.0));
        g.draw(new java.awt.geom.Line2D.Double(cx + 5.5, cy + 2.4, cx + 5.5, cy - 6.4));
        Path2D beam = new Path2D.Double();
        beam.moveTo(cx - 1.6 - 0.55, cy - 5.0);
        beam.lineTo(cx + 5.5 + 0.55, cy - 6.4);
        beam.lineTo(cx + 5.5 + 0.55, cy - 4.6);
        beam.lineTo(cx - 1.6 - 0.55, cy - 3.2);
        beam.closePath();
        g.fill(beam);
    }
}
