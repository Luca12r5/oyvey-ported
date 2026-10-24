package dev.lego.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/** Painted artwork (Java2D) shared by the GUI and the HUD. */
public final class Art {
    private Art() { }

    /** Isometric 2x2 Lego brick filling a size x size box. */
    public static void brick(Graphics2D g, double x, double y, double size, Color base) {
        double s = size;
        double cx = x + s / 2;
        double top = y + s * 0.26, mid = y + s * 0.47, bot = y + s * 0.86;
        double lx = x + s * 0.06, rx = x + s * 0.94;
        double h = s * 0.26;
        Color light = mix(base, Color.WHITE, 0.28), dark = mix(base, Color.BLACK, 0.32), darker = mix(base, Color.BLACK, 0.5);
        // left face
        Path2D left = new Path2D.Double();
        left.moveTo(lx, mid - h * 0.0); left.lineTo(cx, mid + (mid - top)); left.lineTo(cx, bot); left.lineTo(lx, bot - (mid - top)); left.closePath();
        g.setPaint(new GradientPaint((float) lx, (float) mid, mix(base, Color.BLACK, 0.12), (float) cx, (float) bot, dark));
        g.fill(left);
        // right face
        Path2D right = new Path2D.Double();
        right.moveTo(rx, mid); right.lineTo(cx, mid + (mid - top)); right.lineTo(cx, bot); right.lineTo(rx, bot - (mid - top)); right.closePath();
        g.setPaint(new GradientPaint((float) cx, (float) mid, dark, (float) rx, (float) bot, darker));
        g.fill(right);
        // top face
        Path2D topF = new Path2D.Double();
        topF.moveTo(cx, top - (mid - top)); topF.lineTo(rx, mid); topF.lineTo(cx, mid + (mid - top)); topF.lineTo(lx, mid); topF.closePath();
        g.setPaint(new GradientPaint((float) lx, (float) top, light, (float) rx, (float) mid, base));
        g.fill(topF);
        // studs (2x2)
        double sw = s * 0.17, sh = s * 0.085, stH = s * 0.07;
        double[][] studs = {{0, -1}, {-1, 0}, {1, 0}, {0, 1}};
        for (double[] st : studs) {
            double px = cx + st[0] * s * 0.2, py = mid + st[1] * (mid - top) * 0.52 - (mid - top) * 0.02;
            g.setColor(dark);
            g.fill(new Ellipse2D.Double(px - sw / 2, py - sh / 2 - 0.0, sw, sh + stH));
            g.setColor(mix(base, Color.BLACK, 0.18));
            g.fill(new java.awt.geom.Rectangle2D.Double(px - sw / 2, py - stH, sw, stH));
            g.setPaint(new GradientPaint((float) (px - sw / 2), (float) py, light, (float) (px + sw / 2), (float) py, base));
            g.fill(new Ellipse2D.Double(px - sw / 2, py - stH - sh / 2, sw, sh));
        }
        g.setStroke(new BasicStroke((float) Math.max(0.6, s * 0.012)));
        g.setColor(new Color(255, 255, 255, 50));
        g.draw(topF);
    }

    public static Color mix(Color a, Color b, double t) {
        t = Math.max(0, Math.min(1, t));
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t), (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t), (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    /** Soft radial glow. */
    public static void glow(Graphics2D g, double cx, double cy, double r, Color c) {
        g.setPaint(new RadialGradientPaint((float) cx, (float) cy, (float) r, new float[]{0f, 1f},
                new Color[]{c, new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)}));
        g.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
    }

    /** Cached brick texture (render thread). */
    public static Gx.Img brickImg(int px, int argb) {
        return Gx.painted("brick" + Integer.toHexString(argb), px, px, g -> brick(g, 0, 0, px, new Color(argb, true)));
    }
}
