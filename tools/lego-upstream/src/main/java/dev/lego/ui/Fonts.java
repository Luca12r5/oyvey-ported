package dev.lego.ui;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Bundled Poppins font (SIL OFL 1.1) in four weights; thread-safe caches. */
public final class Fonts {
    private Fonts() { }

    public static final int LIGHT = 0, REGULAR = 1, MEDIUM = 2, BOLD = 3;
    private static final String[] FILES = {"Poppins-Light.ttf", "Poppins-Regular.ttf", "Poppins-Medium.ttf", "Poppins-Bold.ttf"};
    private static final Font[] BASE = new Font[4];
    private static volatile boolean loaded;
    private static final Map<Long, Font> SIZED = new ConcurrentHashMap<>();
    private static final Map<String, Float> WIDTHS = new ConcurrentHashMap<>();
    public static final FontRenderContext FRC = new FontRenderContext(null, true, true);

    private static synchronized void load() {
        if (loaded) return;
        for (int i = 0; i < 4; i++) {
            try (InputStream in = Fonts.class.getResourceAsStream("/assets/legoclient/fonts/" + FILES[i])) {
                if (in != null) BASE[i] = Font.createFont(Font.TRUETYPE_FONT, in);
            } catch (Throwable t) {
                BASE[i] = null;
            }
            if (BASE[i] == null) BASE[i] = new Font(Font.SANS_SERIF, i == BOLD ? Font.BOLD : Font.PLAIN, 12);
        }
        loaded = true;
    }

    /** Font of the given weight and pixel size (size is rounded to 1/4 px). */
    public static Font get(int weight, float size) {
        if (!loaded) load();
        int w = Math.max(0, Math.min(3, weight));
        int q = Math.max(1, Math.round(size * 4));
        long key = ((long) w << 32) | q;
        return SIZED.computeIfAbsent(key, k -> BASE[w].deriveFont(q / 4f));
    }

    public static float width(String s, int weight, float size) {
        if (s == null || s.isEmpty()) return 0;
        String key = weight + "|" + Math.round(size * 4) + "|" + s;
        Float cached = WIDTHS.get(key);
        if (cached != null) return cached;
        Rectangle2D r = get(weight, size).getStringBounds(s, FRC);
        float w = (float) r.getWidth();
        if (WIDTHS.size() > 20000) WIDTHS.clear();
        WIDTHS.put(key, w);
        return w;
    }

    public static float ascent(int weight, float size) { return get(weight, size).getLineMetrics("Hg", FRC).getAscent(); }
    public static float descent(int weight, float size) { return get(weight, size).getLineMetrics("Hg", FRC).getDescent(); }

    /** Cuts the text with "…" so it fits into maxW. */
    public static String ellipsize(String s, int weight, float size, float maxW) {
        if (s == null) return "";
        if (width(s, weight, size) <= maxW) return s;
        String e = "…";
        int lo = 0, hi = s.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (width(s.substring(0, mid).trim() + e, weight, size) <= maxW) lo = mid; else hi = mid - 1;
        }
        return lo <= 0 ? e : s.substring(0, lo).trim() + e;
    }

    /** Word wrap into at most maxLines lines (last one ellipsized). */
    public static java.util.List<String> wrap(String s, int weight, float size, float maxW, int maxLines) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (s == null || s.isEmpty()) return out;
        String[] words = s.split(" ");
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String test = line.length() == 0 ? words[i] : line + " " + words[i];
            if (width(test, weight, size) <= maxW || line.length() == 0) {
                line.setLength(0);
                line.append(test);
            } else {
                out.add(line.toString());
                line.setLength(0);
                line.append(words[i]);
                if (out.size() == maxLines - 1) {
                    StringBuilder rest = new StringBuilder(line);
                    for (int j = i + 1; j < words.length; j++) rest.append(' ').append(words[j]);
                    out.add(ellipsize(rest.toString(), weight, size, maxW));
                    return out;
                }
            }
        }
        if (line.length() > 0) out.add(line.toString());
        return out;
    }
}
