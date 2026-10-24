package dev.lego.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Immediate-mode widget kit: widgets are drawn every frame and register click regions;
 * the screen routes mouse input to the top-most region.
 */
public final class Kit {
    private Kit() { }

    public static final class Region {
        public final int x, y, w, h;
        Runnable click, right;
        BiConsumer<Float, Float> drag;
        Consumer<Double> scroll;
        String id;
        Region(int x, int y, int w, int h) { this.x = x; this.y = y; this.w = w; this.h = h; }
        public Region click(Runnable r) { click = r; return this; }
        public Region right(Runnable r) { right = r; return this; }
        /** Drag handler receives the mouse position in pixels (also called on press). */
        public Region drag(BiConsumer<Float, Float> d) { drag = d; return this; }
        public Region scroll(Consumer<Double> s) { scroll = s; return this; }
        public Region id(String s) { id = s; return this; }
        public String idOrNull() { return id; }
        boolean in(float px, float py) { return px >= x && py >= y && px < x + w && py < y + h; }
    }

    private static final List<Region> REGIONS = new ArrayList<>();
    private static Region active;
    /** Clip rectangle for regions (so scrolled-out widgets are not clickable). */
    private static int[] clip;

    public static void frame() { REGIONS.clear(); clip = null; }
    public static void clip(int x, int y, int w, int h) { clip = new int[]{x, y, w, h}; }
    public static void unclip() { clip = null; }

    public static Region hit(int x, int y, int w, int h) {
        int X = x, Y = y, W = w, H = h;
        if (clip != null) {
            int x2 = Math.min(x + w, clip[0] + clip[2]), y2 = Math.min(y + h, clip[1] + clip[3]);
            X = Math.max(x, clip[0]);
            Y = Math.max(y, clip[1]);
            W = Math.max(0, x2 - X);
            H = Math.max(0, y2 - Y);
        }
        Region r = new Region(X, Y, W, H);
        REGIONS.add(r);
        return r;
    }

    public static Region at(float px, float py) {
        for (int i = REGIONS.size() - 1; i >= 0; i--) if (REGIONS.get(i).in(px, py)) return REGIONS.get(i);
        return null;
    }

    public static boolean down(float px, float py, int button) {
        for (int i = REGIONS.size() - 1; i >= 0; i--) {
            Region r = REGIONS.get(i);
            if (!r.in(px, py)) continue;
            if (button == 1 && r.right != null) { r.right.run(); Sound.click(); return true; }
            if (button == 0 && r.drag != null) { active = r; r.drag.accept(px, py); return true; }
            if (button == 0 && r.click != null) { r.click.run(); Sound.click(); return true; }
            if (r.click != null || r.right != null || r.drag != null) return true;
        }
        return false;
    }

    public static boolean dragging() { return active != null; }

    public static boolean drag(float px, float py) {
        if (active == null) return false;
        active.drag.accept(px, py);
        return true;
    }

    public static boolean up() {
        boolean had = active != null;
        active = null;
        return had;
    }

    public static boolean scroll(float px, float py, double amount) {
        for (int i = REGIONS.size() - 1; i >= 0; i--) {
            Region r = REGIONS.get(i);
            if (r.scroll != null && r.in(px, py)) { r.scroll.accept(amount); return true; }
        }
        return false;
    }

    // ================================================================== widgets (P = pixels per design unit)
    /** Animated switch. */
    public static void toggle(String key, int x, int y, int w, int h, boolean on, float P) {
        float t = Ease.to("tg:" + key, on ? 1f : 0f, 16f);
        int track = Gx.mix(Style.light ? 0xFFD5D8DF : 0xFF3A3D47, Style.accent, t);
        Gx.rect(x, y, w, h, h / 2, track);
        if (Halloween.on() && t > 0.01f) { Gx.pushAlpha(t); Halloween.spookyStripes(x, y, w, h, 0); Gx.popAlpha(); }
        else if (Xmas.on() && t > 0.01f) { Gx.pushAlpha(t); Xmas.candy(x, y, w, h, 0); Gx.popAlpha(); }
        int pad = Math.max(2, Math.round(h * 0.14f));
        int d = h - pad * 2;
        int kx = Math.round(x + pad + (w - pad * 2 - d) * Ease.outCubic(t));
        Gx.shadow(kx, y + pad, d, d, d / 2, Math.max(1, Math.round(P * 1.2f)), 0.25f);
        Gx.circle(kx + d / 2, y + h / 2, d / 2, 0xFFFFFFFF);
    }

    /** Slider track; returns the rectangle so the caller can register a drag region. */
    public static void slider(String key, int x, int y, int w, int h, float frac, float P) {
        float f = Ease.to("sl:" + key, frac, 22f);
        int th = Math.max(2, Math.round(P * 3.2f));
        int ty = y + h / 2 - th / 2;
        Gx.rect(x, ty, w, th, th / 2, Style.light ? 0xFFD9DCE3 : 0xFF33363F);
        int fw = Math.round(w * f);
        Gx.rect(x, ty, Math.max(th, fw), th, th / 2, Style.accent);
        int kd = Math.round(P * 11);
        int kx = x + fw - kd / 2;
        Gx.shadow(kx, y + h / 2 - kd / 2, kd, kd, kd / 2, Math.max(1, Math.round(P * 1.5f)), 0.3f);
        Gx.circle(kx + kd / 2, y + h / 2, kd / 2, 0xFFFFFFFF);
        Gx.circle(kx + kd / 2, y + h / 2, Math.max(1, kd / 5), Style.accent);
    }

    /** Filled button with optional icon; hover animation. */
    public static void button(String key, String label, String icon, int x, int y, int w, int h, float P, boolean primary, boolean hovered) {
        float hv = Ease.to("bt:" + key, hovered ? 1f : 0f, 14f);
        int base = primary ? Style.accent : Style.surface2;
        int top = primary ? Gx.mix(base, 0xFFFFFFFF, 0.12f * hv) : Gx.mix(base, Style.surfaceHover, hv);
        Gx.rect(x, y, w, h, Math.round(P * 7), top);
        if (!primary) Gx.outline(x, y, w, h, Math.round(P * 7), Math.max(1, Math.round(P * 0.6f)), Gx.mix(Style.stroke, Style.strokeHi, hv));
        int fg = primary ? Style.accentText : Style.text;
        float fs = P * 8.6f;
        float tw = Gx.width(label, fs, Fonts.MEDIUM);
        float iw = icon == null ? 0 : P * 11 + P * 5;
        float sx = x + (w - tw - iw) / 2f;
        if (icon != null) Gx.icon(icon, sx + P * 5.5f, y + h / 2f, P * 11, fg);
        Gx.textMid(label, sx + iw, y + h / 2f, fs, Fonts.MEDIUM, fg);
    }

    /** Small tooltip bubble above a point. */
    public static void tooltip(String text, float cx, float bottomY, float P) {
        float fs = P * 8f;
        int tw = Math.round(Gx.width(text, fs, Fonts.MEDIUM) + P * 14);
        int th = Math.round(P * 18);
        int x = Math.round(cx - tw / 2f), y = Math.round(bottomY - th);
        Gx.shadow(x, y, tw, th, Math.round(P * 6), Math.round(P * 4), 0.35f);
        Gx.rect(x, y, tw, th, Math.round(P * 6), Style.light ? 0xFF1B1C22 : 0xFFF3F4F6);
        Gx.textCenter(text, x + tw / 2f, y + th / 2f, fs, Fonts.MEDIUM, Style.light ? 0xFFFFFFFF : 0xFF111216);
    }
}
