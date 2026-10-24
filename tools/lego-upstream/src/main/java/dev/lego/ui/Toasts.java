package dev.lego.ui;

import java.util.ArrayList;
import java.util.List;

/** Small animated notifications (top right), drawn on the HUD. Thread-safe show(). */
public final class Toasts {
    private Toasts() { }

    private static final class T {
        final String icon, title, text;
        final int color;
        final long at = System.currentTimeMillis();
        final long dur;
        float y = -1;
        T(String icon, String title, String text, int color, long dur) { this.icon = icon; this.title = title; this.text = text; this.color = color; this.dur = dur; }
    }

    private static final List<T> LIST = new ArrayList<>();

    public static void show(String icon, String title, String text) { show(icon, title, text, 0, 3500); }

    /** color 0 = accent. */
    public static void show(String icon, String title, String text, int color, long durMs) {
        synchronized (LIST) {
            LIST.add(new T(icon, title, text == null ? "" : text, color, durMs));
            while (LIST.size() > 4) LIST.remove(0);
        }
    }

    public static boolean active() { synchronized (LIST) { return !LIST.isEmpty(); } }

    /** Draws the toasts (Gx active, pixel coordinates). */
    public static void render(int W, int H) {
        List<T> copy;
        synchronized (LIST) {
            LIST.removeIf(t -> System.currentTimeMillis() - t.at > t.dur + 400);
            copy = new ArrayList<>(LIST);
        }
        if (copy.isEmpty()) return;
        Style.update();
        float P = Math.max(1f, Math.min(W / 680f, H / 420f));
        int w = Math.round(210 * P), h = Math.round(40 * P), gap = Math.round(6 * P);
        float targetY = Math.round(12 * P);
        for (int i = copy.size() - 1; i >= 0; i--) {
            T t = copy.get(i);
            long age = System.currentTimeMillis() - t.at;
            float in = Ease.outBack(Math.min(1, age / 380f));
            float out = age > t.dur ? Ease.clamp((age - t.dur) / 400f) : 0;
            if (t.y < 0) t.y = targetY;
            t.y += (targetY - t.y) * Math.min(1, Gx.dt * 12);
            int x = Math.round(W - w - 12 * P + (1 - in) * (w + 20 * P) + out * (w + 20 * P));
            int y = Math.round(t.y);
            int accent = t.color == 0 ? Style.accent : t.color;
            Gx.pushAlpha(1 - out);
            Gx.shadow(x, y, w, h, Math.round(10 * P), Math.round(6 * P), 0.45f);
            Gx.rect(x, y, w, h, Math.round(10 * P), 0xF2121318);
            Gx.outline(x, y, w, h, Math.round(10 * P), Math.max(1, Math.round(0.6f * P)), 0x1FFFFFFF);
            int is = Math.round(26 * P);
            Gx.rect(x + Math.round(7 * P), y + (h - is) / 2, is, is, Math.round(8 * P), Gx.withAlpha(accent, 0.2f));
            Gx.icon(t.icon, x + Math.round(7 * P) + is / 2f, y + h / 2f, 14 * P, accent);
            float tx = x + 40 * P;
            Gx.text(Fonts.ellipsize(t.title, Fonts.BOLD, 8.6f * P, w - 48 * P), tx, y + 7 * P, 8.6f * P, Fonts.BOLD, 0xFFFFFFFF);
            Gx.text(Fonts.ellipsize(t.text, Fonts.REGULAR, 7.4f * P, w - 48 * P), tx, y + 21 * P, 7.4f * P, Fonts.REGULAR, 0xFFA2A8B3);
            // time bar
            float frac = Math.max(0, 1 - age / (float) t.dur);
            Gx.rect(x + Math.round(10 * P), y + h - Math.round(3 * P), Math.round((w - 20 * P) * frac), Math.max(1, Math.round(1.5f * P)), 1, Gx.withAlpha(accent, 0.7f));
            Gx.popAlpha();
            targetY += h + gap;
        }
    }
}
