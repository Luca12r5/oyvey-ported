package dev.lego.ui;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Native GUI drawing in physical pixels (render thread only).
 * Shapes, text and icons are anti-aliased Java2D textures that are cached and tinted on the GPU,
 * so animations run at full frame rate without any repainting.
 */
public final class Gx {
    private Gx() { }

    /** Drawing backend (Minecraft DrawContext in game, Java2D in the offline preview). */
    public interface Backend {
        int guiScale();
        void push();
        void pop();
        void translate(float x, float y);
        void scale(float x, float y);
        void fill(int x, int y, int w, int h, int argb);
        void gradV(int x, int y, int w, int h, int top, int bottom);
        void tex(Img t, int x, int y, int w, int h, int u, int v, int rw, int rh, int argb);
        void clip(int x, int y, int w, int h);
        void unclip();
        Object register(int[] argb, int w, int h);
        /** Replaces all pixels of a registered image (same size). */
        void update(Img t, int[] argb);
        void destroy(Img t);
        int[] screenPx();
    }

    public static Backend B;
    /** Physical pixels per GUI unit. */
    public static int S = 1;
    private static float alpha = 1f;
    private static final ArrayDeque<Float> ALPHA = new ArrayDeque<>();
    private static long lastNs = 0;
    /** Seconds since the last frame (clamped). */
    public static float dt = 0.016f;

    // ------------------------------------------------------------------ frame
    public static void begin(Backend b) {
        B = b;
        S = Math.max(1, b.guiScale());
        B.push();
        B.scale(1f / S, 1f / S);
        long now = System.nanoTime();
        // dt is measured once per real frame (newFrame() from MinecraftClient.render). begin() runs several
        // times per frame (HUD, overlays, screen) - measuring here made every later call see ~1 ms,
        // which slowed games and menu animations to a crawl in the world.
        if (!frameHooked || now - frameNs > 250_000_000L) {
            if (lastNs == 0) dt = 0.016f;
            else if (now - lastNs > 2_000_000L) dt = Math.max(0.001f, Math.min(0.1f, (now - lastNs) / 1e9f));
            if (lastNs == 0 || now - lastNs > 2_000_000L) lastNs = now;
        }
        alpha = 1f;
        ALPHA.clear();
        cleanup();
    }

    private static long frameNs;
    private static boolean frameHooked;

    /** Called once at the start of every rendered frame. */
    public static void newFrame() {
        long now = System.nanoTime();
        dt = frameNs == 0 ? 0.016f : Math.max(0.0005f, Math.min(0.1f, (now - frameNs) / 1e9f));
        frameNs = now;
        frameHooked = true;
    }

    public static void end() {
        try { B.pop(); } catch (Throwable ignored) { }
    }

    // ------------------------------------------------------------------ transform / alpha
    public static void push() { B.push(); }
    public static void pop() { B.pop(); }
    /** Translation rounded to whole pixels (keeps text crisp). */
    public static void translate(float x, float y) { B.translate(Math.round(x), Math.round(y)); }
    public static void translateExact(float x, float y) { B.translate(x, y); }
    public static void scale(float s) { B.scale(s, s); }
    /** Scale around a pivot point. */
    public static void scaleAt(float cx, float cy, float s) {
        translateExact(cx, cy);
        scale(s);
        translateExact(-cx, -cy);
    }

    public static void pushAlpha(float a) { ALPHA.push(alpha); alpha *= Math.max(0, Math.min(1, a)); }
    public static void popAlpha() { alpha = ALPHA.isEmpty() ? 1f : ALPHA.pop(); }
    public static float alpha() { return alpha; }

    public static int col(int argb) {
        if (alpha >= 0.999f) return argb;
        int a = (int) (((argb >>> 24) & 255) * alpha);
        return (a << 24) | (argb & 0xFFFFFF);
    }

    public static int withAlpha(int argb, float a) {
        int al = (int) Math.max(0, Math.min(255, ((argb >>> 24) & 255) * a));
        return (al << 24) | (argb & 0xFFFFFF);
    }

    public static int rgba(int rgb, float a) {
        return ((int) Math.max(0, Math.min(255, a * 255)) << 24) | (rgb & 0xFFFFFF);
    }

    public static int mix(int a, int b, float t) {
        t = Math.max(0, Math.min(1, t));
        int aa = a >>> 24, ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int ba = b >>> 24, br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return ((int) (aa + (ba - aa) * t) << 24) | ((int) (ar + (br - ar) * t) << 16) | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }

    // ------------------------------------------------------------------ scissor
    public static void clip(int x, int y, int w, int h) { B.clip(x, y, w, h); }
    public static void unclip() { B.unclip(); }

    // ------------------------------------------------------------------ primitives
    public static void fill(int x, int y, int w, int h, int argb) {
        if (w <= 0 || h <= 0) return;
        B.fill(x, y, w, h, col(argb));
    }

    public static void gradientV(int x, int y, int w, int h, int top, int bottom) {
        if (w <= 0 || h <= 0) return;
        B.gradV(x, y, w, h, col(top), col(bottom));
    }

    /** Draws a region of a texture. */
    private static void tex(Img t, int x, int y, int w, int h, int u, int v, int rw, int rh, int argb) {
        if (w <= 0 || h <= 0) return;
        B.tex(t, x, y, w, h, u, v, rw, rh, col(argb));
    }

    /** Whole texture stretched to w x h. */
    public static void image(Img t, int x, int y, int w, int h, int argb) {
        if (t == null) return;
        tex(t, x, y, w, h, 0, 0, t.w, t.h, argb);
    }


    /** Texture size bucket for radii (animated sizes reuse textures instead of creating new ones every frame). */
    static int bucket(int r) { return r <= 24 ? r : (r + 2) / 4 * 4; }

    /** Rounded rectangle. */
    public static void rect(int x, int y, int w, int h, int r, int argb) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (r <= 0) { fill(x, y, w, h, argb); return; }
        int b = bucket(r);
        Img c = circle(b);
        tex(c, x, y, r, r, 0, 0, b, b, argb);
        tex(c, x + w - r, y, r, r, b, 0, b, b, argb);
        tex(c, x, y + h - r, r, r, 0, b, b, b, argb);
        tex(c, x + w - r, y + h - r, r, r, b, b, b, b, argb);
        fill(x + r, y, w - 2 * r, r, argb);
        fill(x, y + r, w, h - 2 * r, argb);
        fill(x + r, y + h - r, w - 2 * r, r, argb);
    }

    /** Rounded rectangle with a vertical gradient. */
    public static void rectV(int x, int y, int w, int h, int r, int top, int bottom) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (r <= 0) { gradientV(x, y, w, h, top, bottom); return; }
        float k = (float) r / h;
        int midTop = mix(top, bottom, k), midBot = mix(top, bottom, 1 - k);
        int b = bucket(r);
        Img c = circle(b);
        tex(c, x, y, r, r, 0, 0, b, b, top);
        tex(c, x + w - r, y, r, r, b, 0, b, b, top);
        tex(c, x, y + h - r, r, r, 0, b, b, b, bottom);
        tex(c, x + w - r, y + h - r, r, r, b, b, b, b, bottom);
        B.gradV(x + r, y, w - 2 * r, r, col(top), col(midTop));
        B.gradV(x, y + r, w, h - 2 * r, col(midTop), col(midBot));
        B.gradV(x + r, y + h - r, w - 2 * r, r, col(midBot), col(bottom));
    }

    /** Rounded outline of thickness t. */
    public static void outline(int x, int y, int w, int h, int r, int t, int argb) {
        if (w <= 0 || h <= 0 || t <= 0) return;
        r = Math.max(t, Math.min(r, Math.min(w, h) / 2));
        Img c = ring(r, t);
        tex(c, x, y, r, r, 0, 0, r, r, argb);
        tex(c, x + w - r, y, r, r, r, 0, r, r, argb);
        tex(c, x, y + h - r, r, r, 0, r, r, r, argb);
        tex(c, x + w - r, y + h - r, r, r, r, r, r, r, argb);
        fill(x + r, y, w - 2 * r, t, argb);
        fill(x + r, y + h - t, w - 2 * r, t, argb);
        fill(x, y + r, t, h - 2 * r, argb);
        fill(x + w - t, y + r, t, h - 2 * r, argb);
    }

    /** Soft drop shadow around a rounded rectangle. */
    public static void shadow(int x, int y, int w, int h, int r, int blur, float strength) {
        if (w <= 0 || h <= 0 || blur <= 0) return;
        Img s = shadowTex(r, blur);
        int c = s.w / 2;            // corner size = r + blur*2
        int argb = rgba(0x000000, strength);
        int X = x - blur * 2, Y = y - blur * 2 + blur / 2, W = w + blur * 4, H = h + blur * 4;
        if (W < 2 * c || H < 2 * c) { image(s, X, Y, W, H, argb); return; }
        tex(s, X, Y, c, c, 0, 0, c, c, argb);
        tex(s, X + W - c, Y, c, c, c + 1, 0, c, c, argb);
        tex(s, X, Y + H - c, c, c, 0, c + 1, c, c, argb);
        tex(s, X + W - c, Y + H - c, c, c, c + 1, c + 1, c, c, argb);
        tex(s, X + c, Y, W - 2 * c, c, c, 0, 1, c, argb);
        tex(s, X + c, Y + H - c, W - 2 * c, c, c, c + 1, 1, c, argb);
        tex(s, X, Y + c, c, H - 2 * c, 0, c, c, 1, argb);
        tex(s, X + W - c, Y + c, c, H - 2 * c, c + 1, c, c, 1, argb);
        tex(s, X + c, Y + c, W - 2 * c, H - 2 * c, c, c, 1, 1, argb);
    }

    /** Soft round glow (radial gradient). */
    public static void glow(int cx, int cy, int radius, int argb) {
        if (radius <= 0) return;
        Img g = get("glow", 128, () -> {
            int d = 128;
            int[] px = new int[d * d];
            for (int y = 0; y < d; y++)
                for (int x = 0; x < d; x++) {
                    double dx = (x + 0.5 - d / 2.0) / (d / 2.0), dy = (y + 0.5 - d / 2.0) / (d / 2.0);
                    double r = Math.sqrt(dx * dx + dy * dy);
                    double a = r >= 1 ? 0 : Math.pow(1 - r, 2.2);
                    px[y * d + x] = ((int) (a * 255) << 24) | 0xFFFFFF;
                }
            return new int[][]{px, {d, d}};
        });
        image(g, cx - radius, cy - radius, radius * 2, radius * 2, argb);
    }

    public static void circle(int cx, int cy, int r, int argb) {
        if (r <= 0) return;
        Img c = circle(bucket(r));
        image(c, cx - r, cy - r, 2 * r, 2 * r, argb);
    }

    public static void ringAt(int cx, int cy, int r, int t, int argb) {
        if (r <= 0) return;
        int b = bucket(r);
        Img c = ring(b, Math.max(1, Math.round(Math.min(t, r) * b / (float) r)));
        image(c, cx - r, cy - r, 2 * r, 2 * r, argb);
    }

    // ------------------------------------------------------------------ text
    /** Draws text with its top-left at (x, y); returns the advance width. Glyphs come from a shared atlas (1 texture per font size). */
    public static float text(String s, float x, float y, float size, int weight, int argb) {
        if (s == null || s.isEmpty() || size <= 1) return 0;
        Atlas at = atlas(weight, size);
        int n = s.length();
        Glyph[] gl = new Glyph[n];
        for (int i = 0; i < n; i++) gl[i] = at.glyph(s.charAt(i));
        at.flush();
        int c = col(argb);
        int base = Math.round(y + at.asc);
        float pen = x;
        for (int i = 0; i < n; i++) {
            Glyph g = gl[i];
            if (g.w > 0) B.tex(g.page.img, Math.round(pen) + g.ox, base + g.oy, g.w, g.h, g.u, g.v, g.w, g.h, c);
            pen += g.adv;
        }
        return pen - x;
    }

    /** Text vertically centered (on the cap height) at cy. */
    public static float textMid(String s, float x, float cy, float size, int weight, int argb) {
        return text(s, x, topFor(cy, size, weight), size, weight, argb);
    }

    public static float textCenter(String s, float cx, float cy, float size, int weight, int argb) {
        float w = width(s, size, weight);
        return text(s, cx - w / 2, topFor(cy, size, weight), size, weight, argb);
    }

    public static float textRight(String s, float rx, float cy, float size, int weight, int argb) {
        float w = width(s, size, weight);
        return text(s, rx - w, topFor(cy, size, weight), size, weight, argb);
    }

    /** Advance width exactly as text() draws it. */
    public static float width(String s, float size, int weight) {
        if (s == null || s.isEmpty()) return 0;
        Atlas at = atlas(weight, size);
        float w = 0;
        for (int i = 0; i < s.length(); i++) w += at.advance(s.charAt(i));
        return w;
    }

    /** Letter-spaced text centered at (cx, cy). */
    public static void textSpaced(String s, float cx, float cy, float size, int weight, int argb, float spacing) {
        float total = 0;
        for (int i = 0; i < s.length(); i++) total += width(String.valueOf(s.charAt(i)), size, weight) + (i < s.length() - 1 ? spacing : 0);
        float x = cx - total / 2f, top = topFor(cy, size, weight);
        for (int i = 0; i < s.length(); i++) {
            String c = String.valueOf(s.charAt(i));
            if (!c.equals(" ")) text(c, x, top, size, weight, argb);
            x += width(c, size, weight) + spacing;
        }
    }

    public static float spacedWidth(String s, float size, int weight, float spacing) {
        float total = 0;
        for (int i = 0; i < s.length(); i++) total += width(String.valueOf(s.charAt(i)), size, weight) + (i < s.length() - 1 ? spacing : 0);
        return total;
    }

    /** Top y so that the capital letters are centered at cy. */
    public static float topFor(float cy, float size, int weight) {
        float asc = Fonts.ascent(weight, size);
        float cap = size * 0.70f;
        return cy + cap / 2f - asc;
    }

    // ------------------------------------------------------------------ icons
    /** Icon centered at (cx, cy) with the given pixel size. */
    public static void icon(String name, float cx, float cy, float size, int argb) {
        int want = Math.max(4, Math.round(size));
        int s = want <= 20 ? want : (want + 2) / 4 * 4;
        Img t = get("icon:" + name + ":" + s, s + 4, () -> {
            int d = s + 4;
            BufferedImage img = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            hints(g);
            g.translate(2, 2);
            Icons.paint(g, name, s, java.awt.Color.WHITE);
            g.dispose();
            return new int[][]{whiten(img.getRGB(0, 0, d, d, null, 0, d)), {d, d}};
        });
        int dw = Math.round(t.w * want / (float) s);
        tex(t, Math.round(cx - dw / 2f), Math.round(cy - dw / 2f), dw, dw, 0, 0, t.w, t.h, argb);
    }

    // ------------------------------------------------------------------ cache
    public static final class Img {
        /** Backend handle (Identifier in game, BufferedImage offline). */
        public final Object handle;
        public final int w, h;
        final int pad;
        public long used;
        Img(Object handle, int w, int h, int pad) { this.handle = handle; this.w = w; this.h = h; this.pad = pad; }
    }

    private static final Map<String, Img> CACHE = new HashMap<>();
    private static long lastClean = 0;

    /** Custom cached image; supplier returns {pixels, {w, h}}. */
    public static Img get(String key, int hint, java.util.function.Supplier<int[][]> painter) {
        Img t = CACHE.get(key);
        if (t == null) {
            int[][] r = painter.get();
            t = new Img(B.register(r[0], r[1][0], r[1][1]), r[1][0], r[1][1], r.length > 2 ? r[2][0] : 0);
            CACHE.put(key, t);
        }
        t.used = System.currentTimeMillis();
        return t;
    }

    /** Cached image or null (does not create it). */
    public static Img peek(String key) {
        Img t = CACHE.get(key);
        if (t != null) t.used = System.currentTimeMillis();
        return t;
    }

    /** Custom cached image from a Java2D painter (w x h pixels). */
    public static Img painted(String key, int w, int h, java.util.function.Consumer<Graphics2D> paint) {
        return get("p:" + key + ":" + w + "x" + h, 0, () -> {
            BufferedImage img = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            hints(g);
            try { paint.accept(g); } finally { g.dispose(); }
            return new int[][]{img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth()), {img.getWidth(), img.getHeight()}};
        });
    }

    /** Frees textures that were not used for a while. */
    private static void cleanup() {
        long now = System.currentTimeMillis();
        if (now - lastClean < 5000) return;
        lastClean = now;
        Iterator<Map.Entry<String, Img>> it = CACHE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Img> e = it.next();
            long age = now - e.getValue().used;
            boolean small = e.getKey().startsWith("t:");
            if (age > (small ? 20000 : 60000)) {
                B.destroy(e.getValue());
                it.remove();
            }
        }
    }

    public static void clearCache() {
        for (Img i : CACHE.values()) B.destroy(i);
        CACHE.clear();
    }

    private static Img circle(int r) {
        return get("circle:" + r, 0, () -> {
            int d = 2 * r;
            int[] px = new int[d * d];
            for (int y = 0; y < d; y++)
                for (int x = 0; x < d; x++) {
                    int cov = 0;
                    for (int sy = 0; sy < 4; sy++)
                        for (int sx = 0; sx < 4; sx++) {
                            double dx = x + (sx + 0.5) / 4 - r, dy = y + (sy + 0.5) / 4 - r;
                            if (dx * dx + dy * dy <= (double) r * r) cov++;
                        }
                    px[y * d + x] = ((cov * 255 / 16) << 24) | 0xFFFFFF;
                }
            return new int[][]{px, {d, d}};
        });
    }

    private static Img ring(int r, int t) {
        return get("ring:" + r + ":" + t, 0, () -> {
            int d = 2 * r;
            int[] px = new int[d * d];
            double ri = r - t;
            for (int y = 0; y < d; y++)
                for (int x = 0; x < d; x++) {
                    int cov = 0;
                    for (int sy = 0; sy < 4; sy++)
                        for (int sx = 0; sx < 4; sx++) {
                            double dx = x + (sx + 0.5) / 4 - r, dy = y + (sy + 0.5) / 4 - r;
                            double dd = dx * dx + dy * dy;
                            if (dd <= (double) r * r && dd >= ri * ri) cov++;
                        }
                    px[y * d + x] = ((cov * 255 / 16) << 24) | 0xFFFFFF;
                }
            return new int[][]{px, {d, d}};
        });
    }

    private static Img shadowTex(int r, int blur) {
        return get("shadow:" + r + ":" + blur, 0, () -> {
            int c = r + blur * 2;
            int d = 2 * c + 1;
            float[] a = new float[d * d];
            // solid rounded square inset by blur*2 (so the blurred edge fits)
            int inset = blur * 2;
            for (int y = 0; y < d; y++)
                for (int x = 0; x < d; x++) {
                    double qx = Math.max(Math.abs(x + 0.5 - d / 2.0) - (d / 2.0 - inset - r), 0);
                    double qy = Math.max(Math.abs(y + 0.5 - d / 2.0) - (d / 2.0 - inset - r), 0);
                    a[y * d + x] = Math.sqrt(qx * qx + qy * qy) <= r ? 1f : 0f;
                }
            for (int pass = 0; pass < 3; pass++) a = boxBlur(a, d, Math.max(1, blur / 2));
            int[] px = new int[d * d];
            for (int i = 0; i < px.length; i++) px[i] = ((int) (Math.min(1, a[i]) * 255) << 24);
            return new int[][]{px, {d, d}};
        });
    }

    private static float[] boxBlur(float[] src, int d, int rad) {
        float[] tmp = new float[d * d], out = new float[d * d];
        for (int y = 0; y < d; y++)
            for (int x = 0; x < d; x++) {
                float s = 0; int n = 0;
                for (int k = -rad; k <= rad; k++) { int xx = x + k; if (xx >= 0 && xx < d) { s += src[y * d + xx]; } n++; }
                tmp[y * d + x] = s / n;
            }
        for (int y = 0; y < d; y++)
            for (int x = 0; x < d; x++) {
                float s = 0; int n = 0;
                for (int k = -rad; k <= rad; k++) { int yy = y + k; if (yy >= 0 && yy < d) { s += tmp[yy * d + x]; } n++; }
                out[y * d + x] = s / n;
            }
        return out;
    }

    // ------------------------------------------------------------------ glyph atlas
    static final class Glyph {
        Page page;
        int u, v, w, h, ox, oy;
        float adv;
    }

    static final class Page {
        final int size;
        final int[] px;
        Img img;
        int cx = 1, cy = 1, rowH = 0;
        boolean dirty;
        Page(int size) { this.size = size; px = new int[size * size]; java.util.Arrays.fill(px, 0x00FFFFFF); }
    }

    static final class Atlas {
        final java.awt.Font font;
        final int px, weight;
        final float asc;
        final java.util.List<Page> pages = new java.util.ArrayList<>();
        final Map<Character, Glyph> glyphs = new HashMap<>();
        final Map<Character, Float> adv = new HashMap<>();
        Atlas(int weight, int px) {
            this.weight = weight;
            this.px = px;
            this.font = Fonts.get(weight, px);
            this.asc = Fonts.ascent(weight, px);
        }

        private java.awt.Font fontFor(char c) {
            if (font.canDisplay(c)) return font;
            return new java.awt.Font(java.awt.Font.DIALOG, weight >= Fonts.BOLD ? java.awt.Font.BOLD : java.awt.Font.PLAIN, px);
        }

        float advance(char c) {
            Glyph g = glyphs.get(c);
            if (g != null) return g.adv;
            Float a = adv.get(c);
            if (a == null) {
                java.awt.font.GlyphVector gv = fontFor(c).createGlyphVector(Fonts.FRC, String.valueOf(c));
                a = (float) gv.getGlyphMetrics(0).getAdvance();
                adv.put(c, a);
            }
            return a;
        }

        Glyph glyph(char c) {
            Glyph g = glyphs.get(c);
            if (g != null) return g;
            g = new Glyph();
            java.awt.Font f = fontFor(c);
            java.awt.font.GlyphVector gv = f.createGlyphVector(Fonts.FRC, String.valueOf(c));
            g.adv = (float) gv.getGlyphMetrics(0).getAdvance();
            java.awt.Rectangle pb = gv.getPixelBounds(Fonts.FRC, 0, 0);
            if (pb.width > 0 && pb.height > 0 && !Character.isWhitespace(c)) {
                int w = pb.width + 4, h = pb.height + 4;
                BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gr = img.createGraphics();
                hints(gr);
                gr.setColor(java.awt.Color.WHITE);
                gr.drawGlyphVector(gv, 2 - pb.x, 2 - pb.y);
                gr.dispose();
                Page p = place(w, h);
                g.page = p;
                g.u = p.cx;
                g.v = p.cy;
                g.w = w;
                g.h = h;
                g.ox = pb.x - 2;
                g.oy = pb.y - 2;
                int[] src = img.getRGB(0, 0, w, h, null, 0, w);
                for (int yy = 0; yy < h; yy++)
                    for (int xx = 0; xx < w; xx++)
                        p.px[(p.cy + yy) * p.size + p.cx + xx] = (src[yy * w + xx] & 0xFF000000) | 0xFFFFFF;
                p.cx += w + 1;
                p.rowH = Math.max(p.rowH, h);
                p.dirty = true;
            }
            glyphs.put(c, g);
            return g;
        }

        private Page place(int w, int h) {
            Page p = pages.isEmpty() ? null : pages.get(pages.size() - 1);
            if (p != null && p.cx + w + 1 > p.size) { p.cx = 1; p.cy += p.rowH + 1; p.rowH = 0; }
            if (p == null || p.cy + h + 1 > p.size) {
                int size = px <= 18 ? 256 : px <= 36 ? 512 : px <= 72 ? 1024 : 2048;
                while (size < Math.max(w, h) + 2) size *= 2;
                p = new Page(size);
                pages.add(p);
            }
            return p;
        }

        /** Uploads pages that received new glyphs. */
        void flush() {
            for (Page p : pages) {
                if (p.img == null) { p.img = new Img(B.register(p.px, p.size, p.size), p.size, p.size, 0); p.dirty = false; }
                else if (p.dirty) { B.update(p.img, p.px); p.dirty = false; }
            }
        }
    }

    private static final Map<Integer, Atlas> ATLAS = new HashMap<>();

    static Atlas atlas(int weight, float size) {
        int px = Math.max(4, Math.min(400, Math.round(size)));
        int key = weight * 1000 + px;
        Atlas a = ATLAS.get(key);
        if (a == null) { a = new Atlas(weight, px); ATLAS.put(key, a); }
        return a;
    }

    /** Makes every pixel white (keeps alpha) so linear filtering never produces dark fringes. */
    private static int[] whiten(int[] px) {
        for (int i = 0; i < px.length; i++) px[i] = (px[i] & 0xFF000000) | 0xFFFFFF;
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
}
