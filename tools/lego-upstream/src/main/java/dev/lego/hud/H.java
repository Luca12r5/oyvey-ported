package dev.lego.hud;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import dev.lego.ui.Fonts;
import dev.lego.ui.Icons;
import dev.lego.ui.UiSettings;

/** Java2D helpers for the HUD painters (design units, Poppins font). Runs on the painter thread. */
public final class H {
    private H() { }

    public static final Color TXT = new Color(245, 246, 248), SUB = new Color(166, 171, 181), DIM = new Color(110, 115, 125);
    public static final int REG = Fonts.REGULAR, MED = Fonts.MEDIUM, BOLD = Fonts.BOLD;

    public static double w(String s, int weight, float size) { return Fonts.width(s, weight, size); }

    /** Text with its baseline at y. Minimal style adds a soft shadow. */
    public static void text(Graphics2D g, String s, double x, double y, int weight, float size, Color c) {
        if (s == null || s.isEmpty()) return;
        g.setFont(Fonts.get(weight, size));
        if (UiSettings.hudStyle == 1 && UiSettings.hudShadow) {
            g.setColor(new Color(0, 0, 0, Math.min(170, c.getAlpha())));
            g.drawString(s, (float) (x + size * 0.07), (float) (y + size * 0.08));
        }
        g.setColor(c);
        g.drawString(s, (float) x, (float) y);
    }

    public static void right(Graphics2D g, String s, double rx, double y, int weight, float size, Color c) { text(g, s, rx - w(s, weight, size), y, weight, size, c); }
    public static void center(Graphics2D g, String s, double cx, double y, int weight, float size, Color c) { text(g, s, cx - w(s, weight, size) / 2, y, weight, size, c); }

    /** Baseline so that text of this size is vertically centered in a box of height h. */
    public static double base(double top, double h, float size) { return top + h / 2 + size * 0.35; }

    public static void icon(Graphics2D g, String name, double cx, double cy, double size, Color c) {
        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(cx - size / 2, cy - size / 2);
        gg.scale(size / 24.0, size / 24.0);
        Icons.paint(gg, name, 24, c);
        gg.dispose();
    }

    public static void round(Graphics2D g, double x, double y, double w, double h, double r, Color c) {
        g.setColor(c);
        g.fill(new RoundRectangle2D.Double(x, y, w, h, r * 2, r * 2));
    }

    public static void bar(Graphics2D g, double x, double y, double w, double h, double frac, Color fill) {
        round(g, x, y, w, h, h / 2, new Color(255, 255, 255, 30));
        double f = Math.max(0, Math.min(1, frac));
        if (f > 0) round(g, x, y, Math.max(h, w * f), h, h / 2, fill);
    }

    public static Color alpha(Color c, int a) { return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a))); }

    public static Color mix(Color a, Color b, double t) {
        t = Math.max(0, Math.min(1, t));
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t), (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t), (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    public static String ellipsize(String s, double max, int weight, float size) { return Fonts.ellipsize(s, weight, size, (float) max); }
}
