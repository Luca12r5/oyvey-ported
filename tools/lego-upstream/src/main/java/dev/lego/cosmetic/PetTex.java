package dev.lego.cosmetic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Textures of the pets (Java2D, stateless painters - thread-safe). Names start with "pet":
 * <ul>
 * <li>{@code pet_eyes:<style>:<emotion>:<0|1>} - eye layer (1 = eyes closed / blinking), 256x192 face decal</li>
 * <li>{@code pet_face:<style>:<emotion>} - mouth, brows, blush, tears ... (same layout as the eyes)</li>
 * <li>{@code pet_fur:<key>} / {@code pet_head:<key>} - sphere mapped fur of body / head (front at u = 0.25)</li>
 * <li>{@code pet_c:RRGGBB} soft solid, {@code pet_gl:RRGGBB} glossy, {@code pet_metal:RRGGBB} brushed metal</li>
 * <li>misc: pet_star, pet_heart, pet_shell, pet_screen, pet_wing, pet_bat_wing, pet_bubble, pet_mane, pet_horn, pet_fin, pet_zzz</li>
 * </ul>
 */
final class PetTex {
    private PetTex() { }

    /** Face texture size (decal aspect is derived from it). */
    static final int FW = 256, FH = 192;

    /** @return the texture or null if the name is unknown */
    static CTex.T paint(String name) {
        try {
            String[] p = name.split(":");
            switch (p[0]) {
                case "pet_eyes": return CTex.of(eyes(style(p[1]), p[2], "1".equals(p[3])));
                case "pet_face": return CTex.of(face(style(p[1]), p[2]));
                case "pet_fur": return CTex.of(fur(p[1], false));
                case "pet_head": return CTex.of(fur(p[1], true));
                case "pet_c": return CTex.of(solid(hex(p[1])));
                case "pet_gl": return CTex.of(gloss(hex(p[1])));
                case "pet_metal": return CTex.of(metal(hex(p[1])));
                case "pet_star": return CTex.of(star());
                case "pet_heart": return CTex.of(heartTex());
                case "pet_shell": return CTex.of(shell());
                case "pet_screen": return CTex.of(screen());
                case "pet_wing": return CTex.of(beeWing());
                case "pet_bat_wing": return CTex.of(batWing());
                case "pet_dragon_wing": return CTex.of(dragonWing());
                case "pet_feather": return CTex.of(featherWing(hex(p[1]), hex(p[2])));
                case "pet_dwing": return CTex.of(drakeWing(hex(p[1]), hex(p[2])));
                case "pet_tailfan": return CTex.of(tailFan(hex(p[1]), hex(p[2])));
                case "pet_bubble": return CTex.of(bubble());
                case "pet_mane": return CTex.of(mane());
                case "pet_horn": return CTex.of(horn());
                case "pet_fin": return CTex.of(fin(p.length > 1 ? hex(p[1]) : 0xFF8A3D));
                case "pet_cap": return CTex.of(mushCap());
                case "pet_jelly": return CTex.of(jelly());
                case "pet_berry": return CTex.of(berry());
                case "pet_brick": return CTex.of(brick(p.length > 1 ? hex(p[1]) : 0xE3000B));
                default: return null;
            }
        } catch (RuntimeException e) {
            return null;
        }
    }

    // ================================================================== helpers
    private static int hex(String s) { return Integer.parseInt(s, 16) & 0xFFFFFF; }

    private static BufferedImage img(int w, int h) { return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB); }

    private static Graphics2D gfx(BufferedImage im) {
        Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        return g;
    }

    static Color c(int rgb) { return new Color(rgb & 0xFFFFFF); }
    static Color ca(int rgb, int a) { return new Color((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, Math.max(0, Math.min(255, a))); }

    static int mix(int a, int b, float t) {
        int r = Math.round(((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = Math.round(((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = Math.round((a & 255) * (1 - t) + (b & 255) * t);
        return (r << 16) | (g << 8) | bl;
    }

    static int light(int c, float t) { return mix(c, 0xFFFFFF, t); }
    static int dark(int c, float t) { return mix(c, 0x000000, t); }
    /** Warm shadow (slightly purple instead of grey - looks softer). */
    static int shade(int c, float t) { return mix(c, mix(dark(c, 0.55f), 0x3A1F4A, 0.25f), t); }

    private static Ellipse2D el(double cx, double cy, double w, double h) { return new Ellipse2D.Double(cx - w / 2, cy - h / 2, w, h); }

    private static void soft(Graphics2D g, double cx, double cy, double w, double h, int rgb, int alpha) {
        float r = (float) Math.max(w, h) / 2f;
        if (r < 0.5f) return;
        AffineTransform old = g.getTransform();
        g.translate(cx, cy);
        g.scale(w / (2 * r), h / (2 * r));
        g.setPaint(new RadialGradientPaint(0, 0, r, new float[]{0f, 0.55f, 1f}, new Color[]{ca(rgb, alpha), ca(rgb, alpha * 3 / 4), ca(rgb, 0)}));
        g.fill(new Ellipse2D.Float(-r, -r, 2 * r, 2 * r));
        g.setTransform(old);
    }

    /** Vertical base gradient: light top, rich middle, warm dark bottom. */
    private static void baseGrad(Graphics2D g, int w, int h, int base) {
        g.setPaint(new LinearGradientPaint(0, 0, 0, h, new float[]{0f, 0.45f, 1f}, new Color[]{c(light(base, 0.16f)), c(base), c(shade(base, 0.28f))}));
        g.fillRect(0, 0, w, h);
    }

    private static void furStrokes(Graphics2D g, int w, int h, int base, long seed, int n, int alpha) {
        Random r = new Random(seed);
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < n; i++) {
            float x = r.nextFloat() * w, y = r.nextFloat() * h, len = 2 + r.nextFloat() * 4;
            double ang = Math.PI / 2 + (r.nextFloat() - 0.5) * 0.9;
            g.setColor(r.nextBoolean() ? ca(light(base, 0.35f), alpha) : ca(shade(base, 0.4f), alpha));
            g.draw(new java.awt.geom.Line2D.Float(x, y, x + (float) Math.cos(ang) * len, y + (float) Math.sin(ang) * len));
        }
    }

    // sphere mapping: azimuth (rad, + = pet's right) / elevation (rad, + = up) -> texture px
    private static double sx(int w, double az) { return w * (0.25 - az / (2 * Math.PI)); }
    private static double sy(int h, double elv) { return h * (0.5 - elv / Math.PI); }

    /** Soft patch in sphere angles (az, el center; half sizes in rad). */
    private static void patch(Graphics2D g, int w, int h, double az, double elv, double haz, double hel, int rgb, int alpha) {
        double px = sx(w, az), py = sy(h, elv), pw = haz / Math.PI * w, ph = hel / Math.PI * h * 2;
        soft(g, px, py, pw * 2.2, ph * 1.15, rgb, alpha);
        g.setColor(ca(rgb, alpha));
        g.fill(el(px, py, pw * 1.7, ph * 0.9));
    }

    // ================================================================== small textures
    static BufferedImage solid(int base) {
        BufferedImage im = img(32, 32);
        Graphics2D g = gfx(im);
        baseGrad(g, 32, 32, base);
        furStrokes(g, 32, 32, base, base, 40, 22);
        g.dispose();
        return im;
    }

    static BufferedImage gloss(int base) {
        int w = 128, h = 64;
        BufferedImage im = img(w, h);
        Graphics2D g = gfx(im);
        baseGrad(g, w, h, base);
        soft(g, sx(w, 0.35), sy(h, 0.55), 30, 22, 0xFFFFFF, 190);
        g.setColor(ca(0xFFFFFF, 235));
        g.fill(el(sx(w, 0.35), sy(h, 0.58), 10, 6));
        soft(g, sx(w, -0.9), sy(h, -0.6), 40, 14, light(base, 0.5f), 90);
        g.dispose();
        return im;
    }

    static BufferedImage metal(int base) {
        int w = 64, h = 64;
        BufferedImage im = img(w, h);
        Graphics2D g = gfx(im);
        baseGrad(g, w, h, base);
        Random r = new Random(7);
        for (int y = 0; y < h; y++) {
            g.setColor(ca(r.nextBoolean() ? 0xFFFFFF : 0x000000, 10 + r.nextInt(14)));
            g.fillRect(0, y, w, 1);
        }
        g.setColor(ca(0xFFFFFF, 60));
        g.fillRect(0, 2, w, 3);
        g.dispose();
        return im;
    }

    private static Shape starShape(double cx, double cy, double ro, double ri, int n, double rot) {
        Path2D.Double p = new Path2D.Double();
        for (int i = 0; i < n * 2; i++) {
            double a = rot - Math.PI / 2 + Math.PI * i / n, rr = i % 2 == 0 ? ro : ri;
            double x = cx + Math.cos(a) * rr, y = cy + Math.sin(a) * rr;
            if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
        }
        p.closePath();
        return p;
    }

    /** Rounded 5-point star (stroked with round joins). */
    private static void roundStar(Graphics2D g, double cx, double cy, double r, Color fill, Color line, float lw) {
        Shape s = starShape(cx, cy, r, r * 0.5, 5, 0);
        g.setPaint(fill);
        g.fill(s);
        g.setStroke(new BasicStroke(lw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(line);
        g.draw(s);
    }

    private static Shape heart(double cx, double cy, double s) {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(cx, cy + s * 0.42);
        p.curveTo(cx - s * 0.62, cy - s * 0.02, cx - s * 0.48, cy - s * 0.58, cx - s * 0.02, cy - s * 0.3);
        p.lineTo(cx, cy - s * 0.26);
        p.lineTo(cx + s * 0.02, cy - s * 0.3);
        p.curveTo(cx + s * 0.48, cy - s * 0.58, cx + s * 0.62, cy - s * 0.02, cx, cy + s * 0.42);
        p.closePath();
        return p;
    }

    static BufferedImage star() {
        BufferedImage im = img(64, 64);
        Graphics2D g = gfx(im);
        soft(g, 32, 32, 64, 64, 0xFFE27A, 120);
        roundStar(g, 32, 33, 22, c(0xFFD84A), c(0xFFF4C0), 3f);
        g.setPaint(new GradientPaint(0, 12, c(0xFFF8C8), 0, 54, c(0xFFB02E)));
        g.fill(starShape(32, 33, 19, 9.5, 5, 0));
        g.setColor(ca(0xFFFFFF, 220));
        g.fill(el(27, 26, 7, 5));
        g.dispose();
        return im;
    }

    static BufferedImage heartTex() {
        BufferedImage im = img(64, 64);
        Graphics2D g = gfx(im);
        soft(g, 32, 32, 64, 64, 0xFF6FA8, 110);
        g.setPaint(new GradientPaint(0, 10, c(0xFF8DB8), 0, 54, c(0xF0306A)));
        g.fill(heart(32, 34, 52));
        g.setColor(ca(0xFFFFFF, 210));
        g.fill(el(22, 24, 9, 6));
        g.dispose();
        return im;
    }

    static BufferedImage shell() {
        int w = 256, h = 128;
        BufferedImage im = img(w, h);
        Graphics2D g = gfx(im);
        baseGrad(g, w, h, 0x5DAA4E);
        // hexagon plates (in u/v space - good enough on a dome)
        Random r = new Random(3);
        for (int row = 0; row < 5; row++)
            for (int col = 0; col < 9; col++) {
                double cx = col * 29 + (row % 2) * 14.5 + 6, cy = row * 22 + 10;
                Path2D.Double p = new Path2D.Double();
                for (int k = 0; k < 6; k++) {
                    double a = Math.PI / 3 * k + Math.PI / 6;
                    double x = cx + Math.cos(a) * 12, y = cy + Math.sin(a) * 10;
                    if (k == 0) p.moveTo(x, y); else p.lineTo(x, y);
                }
                p.closePath();
                g.setPaint(new GradientPaint(0, (float) cy - 10, c(0x9ADB6A), 0, (float) cy + 10, c(0x5FB04D)));
                g.fill(p);
                g.setColor(ca(0xFFFFFF, 40 + r.nextInt(30)));
                g.fill(el(cx - 3, cy - 3, 8, 5));
                g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(c(0x3D7A34));
                g.draw(p);
            }
        // rim
        g.setPaint(new GradientPaint(0, 108, c(0xE9D27A), 0, 128, c(0xB8943E)));
        g.fillRect(0, 106, w, 22);
        g.dispose();
        return im;
    }

    static BufferedImage screen() {
        BufferedImage im = img(64, 64);
        Graphics2D g = gfx(im);
        g.setPaint(new GradientPaint(0, 0, c(0x243248), 0, 64, c(0x0E1422)));
        g.fillRect(0, 0, 64, 64);
        g.setColor(ca(0x6FF3FF, 16));
        for (int y = 0; y < 64; y += 3) g.fillRect(0, y, 64, 1);
        g.setColor(ca(0xFFFFFF, 36));
        g.fill(new RoundRectangle2D.Double(6, 4, 30, 10, 8, 8));
        g.dispose();
        return im;
    }

    static BufferedImage beeWing() {
        BufferedImage im = img(64, 64);
        Graphics2D g = gfx(im);
        Shape s = el(32, 32, 60, 60);
        g.setPaint(new RadialGradientPaint(24, 22, 40, new float[]{0f, 1f}, new Color[]{ca(0xFFFFFF, 200), ca(0xBFE8FF, 120)}));
        g.fill(s);
        g.setStroke(new BasicStroke(2f));
        g.setColor(ca(0x9FD6FF, 200));
        g.draw(s);
        g.setColor(ca(0xFFFFFF, 140));
        g.drawLine(32, 4, 32, 60);
        g.drawLine(10, 20, 54, 44);
        g.dispose();
        return im;
    }

    static BufferedImage batWing() {
        BufferedImage im = img(128, 64);
        Graphics2D g = gfx(im);
        Path2D.Double p = new Path2D.Double();
        p.moveTo(0, 6);
        p.curveTo(40, 0, 90, 0, 126, 8);
        p.quadTo(112, 26, 116, 50);
        p.quadTo(96, 38, 82, 54);
        p.quadTo(66, 38, 48, 58);
        p.quadTo(30, 40, 0, 44);
        p.closePath();
        g.setPaint(new GradientPaint(0, 0, c(0x8A6FB8), 0, 64, c(0x4A3570)));
        g.fill(p);
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c(0x3A2A58));
        g.drawLine(0, 8, 116, 48);
        g.drawLine(0, 8, 82, 52);
        g.drawLine(0, 8, 48, 56);
        g.setColor(ca(0xE0C8FF, 90));
        g.drawLine(2, 6, 124, 9);
        g.dispose();
        return im;
    }

    static BufferedImage dragonWing() {
        BufferedImage im = img(128, 96);
        Graphics2D g = gfx(im);
        Path2D.Double p = new Path2D.Double();
        p.moveTo(0, 4);
        p.curveTo(50, -4, 100, 6, 126, 20);
        p.quadTo(108, 44, 118, 80);
        p.quadTo(92, 64, 76, 86);
        p.quadTo(58, 62, 34, 90);
        p.quadTo(18, 58, 0, 56);
        p.closePath();
        g.setPaint(new GradientPaint(0, 0, c(0xFFC9E8), 0, 96, c(0xB98AF5)));
        g.fill(p);
        g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c(0x7E5BD0));
        g.drawLine(0, 5, 118, 78);
        g.drawLine(0, 5, 76, 84);
        g.drawLine(0, 5, 34, 88);
        g.setColor(c(0x8D6BE0));
        g.draw(new java.awt.geom.QuadCurve2D.Double(0, 4, 60, -2, 126, 20));
        g.dispose();
        return im;
    }

    static BufferedImage bubble() {
        int w = 128, h = 64;
        BufferedImage im = img(w, h);
        Graphics2D g = gfx(im);
        // mostly clear glass, a bit denser towards the top and bottom (fresnel-ish)
        g.setPaint(new LinearGradientPaint(0, 0, 0, h, new float[]{0f, 0.3f, 0.7f, 1f},
                new Color[]{ca(0xE8FAFF, 90), ca(0xCFF2FF, 26), ca(0xBFE8FF, 26), ca(0x9FDCFF, 80)}));
        g.fillRect(0, 0, w, h);
        // thin curved window reflection
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(ca(0xFFFFFF, 210));
        g.draw(new java.awt.geom.QuadCurve2D.Double(sx(w, 1.25), sy(h, 0.35), sx(w, 1.05), sy(h, 0.85), sx(w, 0.6), sy(h, 0.95)));
        g.fill(el(sx(w, 1.3), sy(h, 0.22), 3, 3));
        soft(g, sx(w, -0.6), sy(h, -0.5), 30, 12, 0xFFD9F5, 110);
        g.dispose();
        return im;
    }

    static BufferedImage mane() {
        BufferedImage im = img(64, 64);
        Graphics2D g = gfx(im);
        int[] cols = {0xFF9BC8, 0xFFC98A, 0xFFF0A0, 0xA8F0C0, 0x9CD2FF, 0xC8A8FF};
        for (int i = 0; i < 6; i++) {
            g.setColor(c(cols[i]));
            g.fillRect(0, i * 64 / 6, 64, 64 / 6 + 1);
        }
        g.setColor(ca(0xFFFFFF, 60));
        for (int i = 0; i < 64; i += 5) g.drawLine(i, 0, i + 8, 64);
        g.dispose();
        return im;
    }

    static BufferedImage horn() {
        BufferedImage im = img(32, 64);
        Graphics2D g = gfx(im);
        g.setPaint(new GradientPaint(0, 0, c(0xFFF7C8), 0, 64, c(0xF2B94A)));
        g.fillRect(0, 0, 32, 64);
        g.setStroke(new BasicStroke(4f));
        g.setColor(ca(0xC98A1A, 150));
        for (int i = -64; i < 64; i += 12) g.drawLine(0, i + 32, 32, i + 20);
        g.dispose();
        return im;
    }

    static BufferedImage fin(int base) {
        BufferedImage im = img(64, 64);
        Graphics2D g = gfx(im);
        // fan shaped tail fin, root at the left middle
        Path2D.Double p = new Path2D.Double();
        p.moveTo(2, 26);
        p.curveTo(20, 10, 40, 0, 62, 4);
        p.quadTo(50, 20, 54, 32);
        p.quadTo(50, 44, 62, 60);
        p.curveTo(40, 64, 20, 54, 2, 38);
        p.closePath();
        g.setPaint(new GradientPaint(0, 0, ca(light(base, 0.15f), 245), 64, 0, ca(light(base, 0.55f), 200)));
        g.fill(p);
        g.setClip(p);
        g.setStroke(new BasicStroke(2f));
        g.setColor(ca(dark(base, 0.12f), 150));
        for (int i = 4; i < 64; i += 9) g.drawLine(0, 32, 64, i);
        g.setClip(null);
        g.dispose();
        return im;
    }

    static BufferedImage berry() {
        int w = 64, h = 32;
        BufferedImage im = img(w, h);
        Graphics2D g = gfx(im);
        g.setPaint(new GradientPaint(0, 0, c(0xFF6A7A), 0, h, c(0xD81E3A)));
        g.fillRect(0, 0, w, h);
        g.setColor(c(0xFFE28A));
        for (int y = 4; y < h; y += 6)
            for (int x = (y / 6) % 2 * 4; x < w; x += 8) g.fill(el(x, y, 2.2, 3));
        g.dispose();
        return im;
    }

    static BufferedImage mushCap() {
        int w = 256, h = 128;
        BufferedImage im = img(w, h);
        Graphics2D g = gfx(im);
        g.setPaint(new LinearGradientPaint(0, 0, 0, h, new float[]{0f, 0.5f, 0.52f, 1f}, new Color[]{c(0xFF6B5E), c(0xE0303A), c(0xFFF1DC), c(0xE8CFA8)}));
        g.fillRect(0, 0, w, h);
        Random r = new Random(11);
        for (int i = 0; i < 18; i++) {
            double x = r.nextDouble() * w, y = 6 + r.nextDouble() * 46, s = 10 + r.nextDouble() * 12;
            for (int k = -1; k <= 1; k++) {
                g.setColor(c(0xFFF8F0));
                g.fill(el(x + k * w, y, s * 1.3, s * (0.6 + y / 90)));
                g.setColor(ca(0xE8D8D0, 200));
                g.fill(el(x + k * w + 2, y + 2, s * 0.6, s * 0.3));
            }
        }
        // gills under the cap
        g.setColor(ca(0xC9A67A, 120));
        for (int x = 0; x < w; x += 6) g.drawLine(x, 70, x, 128);
        g.dispose();
        return im;
    }

    static BufferedImage jelly() {
        int w = 128, h = 64;
        BufferedImage im = img(w, h);
        Graphics2D g = gfx(im);
        g.setPaint(new LinearGradientPaint(0, 0, 0, h, new float[]{0f, 0.5f, 1f}, new Color[]{c(0xFFD6F4), c(0xF59CE0), c(0xB88CFF)}));
        g.fillRect(0, 0, w, h);
        g.setColor(ca(0xFFFFFF, 90));
        for (int i = 0; i < w; i += 16) g.fill(el(i + 8, 26, 6, 22));
        soft(g, sx(w, 0.4), sy(h, 0.8), 30, 10, 0xFFFFFF, 200);
        g.dispose();
        return im;
    }

    static BufferedImage brick(int base) {
        BufferedImage im = img(64, 64);
        Graphics2D g = gfx(im);
        g.setPaint(new GradientPaint(0, 0, c(light(base, 0.18f)), 0, 64, c(dark(base, 0.12f))));
        g.fillRect(0, 0, 64, 64);
        g.setColor(ca(0xFFFFFF, 70));
        g.fillRect(0, 0, 64, 4);
        g.setColor(ca(0x000000, 40));
        g.fillRect(0, 60, 64, 4);
        g.dispose();
        return im;
    }

    // ================================================================== fur
    /** base, belly/muzzle, accent (e.g. dark markings). */
    private static final Map<String, int[]> FUR = new HashMap<>();
    static {
        FUR.put("cat", new int[]{0xF7A451, 0xFFF4E6, 0xD9742B});
        FUR.put("shiba", new int[]{0xEE9748, 0xFFF7EC, 0xB8632A});
        FUR.put("bunny", new int[]{0xF1DCC6, 0xFFFBF5, 0xFFB7C8});
        FUR.put("fox", new int[]{0xF7863A, 0xFFF6EC, 0x4A2A22});
        FUR.put("redpanda", new int[]{0xD3602E, 0xFFF6EA, 0x5A2A1A});
        FUR.put("panda", new int[]{0xFBFBF7, 0xFFFFFF, 0x2A2A33});
        FUR.put("penguin", new int[]{0x34426A, 0xFFFFFF, 0xFFB04A});
        FUR.put("axolotl", new int[]{0xFFB8CE, 0xFFE3EC, 0xF2779F});
        FUR.put("frog", new int[]{0x86D160, 0xF1FAC8, 0x5AA544});
        FUR.put("chick", new int[]{0xFFE46A, 0xFFF6B8, 0xFFB43C});
        FUR.put("hamster", new int[]{0xF0B064, 0xFFF8EE, 0xC9803E});
        FUR.put("owl", new int[]{0xA7784F, 0xF1DDBA, 0x6E4A30});
        FUR.put("bee", new int[]{0xFFD447, 0xFFE98A, 0x3A2A2A});
        FUR.put("dragon", new int[]{0xAE92F7, 0xFFE9B0, 0x8367E0});
        FUR.put("unicorn", new int[]{0xFFFBFF, 0xFFEAF6, 0xF2D2FF});
        FUR.put("bear", new int[]{0xB9804E, 0xF0D2A8, 0x8A5A34});
        FUR.put("turtle", new int[]{0x9FDB82, 0xE8F6C0, 0x6FB55A});
        FUR.put("bat", new int[]{0x6E5C94, 0xCBB8E6, 0x4A3A6A});
        FUR.put("fish", new int[]{0xFF8C3A, 0xFFE0B8, 0xFFFFFF});
        FUR.put("mush", new int[]{0xFFF1DC, 0xFFFFFF, 0xE8CFA8});
        FUR.put("cloud", new int[]{0xFFFFFF, 0xFFFFFF, 0xD6E2FF});
        FUR.put("ghost", new int[]{0xFBFCFF, 0xFFFFFF, 0xC9D6FF});
        FUR.put("slime", new int[]{0x8CF0A8, 0xC8FFD4, 0x3FAE63});
        FUR.put("pdark", new int[]{0x2E2E38, 0x2E2E38, 0x2E2E38});
        // realistic pets
        FUR.put("wolf", new int[]{0x8E939E, 0xE9E7E2, 0x4E535E});
        FUR.put("horse", new int[]{0xA3603A, 0xC98A5E, 0xFFFFFF});
        FUR.put("fawn", new int[]{0xB5774A, 0xF4E6D0, 0xFFF8EE});
        FUR.put("deer", new int[]{0xA9714A, 0xF1E3CF, 0x6E4A30});
        FUR.put("lion", new int[]{0xE0A965, 0xF7E6C6, 0x9A6230});
        FUR.put("tiger", new int[]{0xF0892C, 0xFFF6EA, 0x241A1C});
        FUR.put("retr", new int[]{0xE3AC5E, 0xF6DBA6, 0xC98A3E});
        FUR.put("afox", new int[]{0xF3F5FA, 0xFFFFFF, 0xC5D0E2});
        FUR.put("eagle", new int[]{0x5B3B25, 0x7A5236, 0x3A2416});
        FUR.put("eagleh", new int[]{0xF6F3EC, 0xFFFFFF, 0xDCD6CC});
        FUR.put("drake", new int[]{0x7C5CD6, 0xF2D9A0, 0x4E3A9E});
    }

    static BufferedImage fur(String key, boolean head) {
        int[] f = FUR.getOrDefault(key, new int[]{0xCCCCCC, 0xFFFFFF, 0x888888});
        int base = f[0], belly = f[1], acc = f[2];
        int w = 256, h = 128;
        BufferedImage im = img(w, h);
        Graphics2D g = gfx(im);
        baseGrad(g, w, h, base);
        long seed = key.hashCode() * 31L + (head ? 7 : 0);
        boolean smooth = key.equals("drake") || key.equals("slime") || key.equals("cloud") || key.equals("ghost") || key.equals("frog") || key.equals("axolotl") || key.equals("mush") || key.equals("fish") || key.equals("turtle");
        if (!smooth) furStrokes(g, w, h, base, seed, 1600, 26);
        switch (key) {
            case "cat":
                if (head) {
                    patch(g, w, h, 0, -0.36, 0.44, 0.24, belly, 255);
                    // forehead tabby "M"
                    g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.setColor(ca(acc, 230));
                    for (int i = -1; i <= 1; i++) {
                        double x = sx(w, i * 0.16), y0 = sy(h, 0.95), y1 = sy(h, 0.62 - Math.abs(i) * 0.06);
                        g.draw(new java.awt.geom.Line2D.Double(x, y0, x, y1));
                    }
                    for (int s = -1; s <= 1; s += 2)
                        for (int i = 0; i < 2; i++) {
                            double az = s * (1.3 + i * 0.25);
                            g.draw(new java.awt.geom.Line2D.Double(sx(w, az), sy(h, 0.1 + i * 0.25), sx(w, az + s * 0.35), sy(h, 0.15 + i * 0.25)));
                        }
                } else {
                    patch(g, w, h, 0, -0.35, 0.55, 0.55, belly, 255);
                    tabby(g, w, h, acc);
                }
                break;
            case "shiba":
                if (head) {
                    patch(g, w, h, 0.62, -0.3, 0.42, 0.34, belly, 255);
                    patch(g, w, h, -0.62, -0.3, 0.42, 0.34, belly, 255);
                    patch(g, w, h, 0, -0.5, 0.45, 0.3, belly, 255);
                    patch(g, w, h, 0.34, 0.55, 0.1, 0.07, belly, 240);
                    patch(g, w, h, -0.34, 0.55, 0.1, 0.07, belly, 240);
                } else patch(g, w, h, 0, -0.3, 0.6, 0.6, belly, 255);
                break;
            case "fox":
                if (head) {
                    patch(g, w, h, 0.5, -0.4, 0.55, 0.32, belly, 255);
                    patch(g, w, h, -0.5, -0.4, 0.55, 0.32, belly, 255);
                    patch(g, w, h, 0, -0.55, 0.35, 0.3, belly, 255);
                } else patch(g, w, h, 0, -0.1, 0.5, 0.65, belly, 255);
                break;
            case "redpanda":
                if (head) {
                    patch(g, w, h, 0, -0.45, 0.6, 0.3, belly, 255);
                    patch(g, w, h, 0.45, 0.5, 0.16, 0.1, belly, 250);
                    patch(g, w, h, -0.45, 0.5, 0.16, 0.1, belly, 250);
                    // tear marks
                    g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.setColor(ca(0x9A3A18, 110));
                    for (int s = -1; s <= 1; s += 2)
                        g.draw(new java.awt.geom.QuadCurve2D.Double(sx(w, s * 0.45), sy(h, -0.05), sx(w, s * 0.56), sy(h, -0.35), sx(w, s * 0.75), sy(h, -0.55)));
                } else {
                    // dark belly / chest
                    patch(g, w, h, 0, -0.5, 0.7, 0.55, acc, 240);
                }
                break;
            case "panda":
                if (head) {
                    for (int s = -1; s <= 1; s += 2) {
                        AffineTransform o = g.getTransform();
                        double px = sx(w, s * 0.38), py = sy(h, 0.03);
                        g.translate(px, py);
                        g.rotate(s * 0.45);
                        soft(g, 0, 0, 44, 44, acc, 255);
                        g.setColor(c(acc));
                        g.fill(el(0, 0, 32, 34));
                        g.setTransform(o);
                    }
                    patch(g, w, h, 0, -0.45, 0.4, 0.2, 0xFFFFFF, 200);
                } else {
                    // black shoulder band
                    g.setPaint(new GradientPaint(0, 0, c(acc), 0, h, c(light(acc, 0.05f))));
                    g.fill(new Rectangle2D.Double(0, sy(h, 0.75), w, sy(h, 0.1) - sy(h, 0.75)));
                    furStrokes(g, w, h, acc, seed + 1, 300, 30);
                }
                break;
            case "penguin":
                if (head) {
                    // white heart shaped face
                    g.setColor(c(belly));
                    Area a = new Area(el(sx(w, 0.3), sy(h, 0.0), 60, 58));
                    a.add(new Area(el(sx(w, -0.3), sy(h, 0.0), 60, 58)));
                    a.add(new Area(el(sx(w, 0), sy(h, -0.35), 70, 50)));
                    g.fill(a);
                } else patch(g, w, h, 0, -0.1, 0.75, 0.85, belly, 255);
                break;
            case "axolotl":
                patch(g, w, h, 0, head ? -0.5 : -0.4, 0.6, 0.4, belly, 220);
                spots(g, w, h, acc, seed, 26, 3, 6, 160);
                break;
            case "frog":
                patch(g, w, h, 0, head ? -0.6 : -0.4, 0.75, 0.45, belly, 250);
                spots(g, w, h, acc, seed, head ? 8 : 16, 4, 10, 170);
                break;
            case "chick":
                patch(g, w, h, 0, -0.4, 0.6, 0.55, belly, 200);
                break;
            case "hamster":
                if (head) {
                    patch(g, w, h, 0.62, -0.35, 0.45, 0.35, belly, 255);
                    patch(g, w, h, -0.62, -0.35, 0.45, 0.35, belly, 255);
                    patch(g, w, h, 0, -0.55, 0.4, 0.3, belly, 255);
                    patch(g, w, h, 0, 0.55, 0.12, 0.35, acc, 120);
                } else patch(g, w, h, 0, -0.3, 0.7, 0.65, belly, 255);
                break;
            case "owl":
                if (head) {
                    for (int s = -1; s <= 1; s += 2) patch(g, w, h, s * 0.36, 0.02, 0.36, 0.34, belly, 255);
                    g.setStroke(new BasicStroke(2f));
                    g.setColor(ca(acc, 120));
                    for (int s = -1; s <= 1; s += 2) g.draw(el(sx(w, s * 0.36), sy(h, 0.02), 50, 50));
                } else {
                    patch(g, w, h, 0, -0.3, 0.6, 0.65, belly, 255);
                    chevrons(g, sx(w, 0), sy(h, -0.3), acc);
                }
                break;
            case "bee":
                if (!head) {
                    g.setColor(c(acc));
                    for (int i = 0; i < 3; i++) {
                        double y = h * (0.3 + i * 0.22);
                        g.fill(new Rectangle2D.Double(0, y, w, h * 0.1));
                    }
                    furStrokes(g, w, h, acc, seed + 2, 400, 30);
                } else patch(g, w, h, 0, -0.45, 0.5, 0.3, belly, 180);
                break;
            case "dragon":
                if (head) patch(g, w, h, 0, -0.5, 0.45, 0.3, belly, 230);
                else {
                    patch(g, w, h, 0, -0.3, 0.55, 0.65, belly, 255);
                    g.setStroke(new BasicStroke(2f));
                    g.setColor(ca(0xE0B870, 170));
                    for (int i = 0; i < 6; i++) {
                        double y = sy(h, -0.8 + i * 0.22);
                        g.draw(new java.awt.geom.QuadCurve2D.Double(sx(w, 0.45), y, sx(w, 0), y + 5, sx(w, -0.45), y));
                    }
                    spots(g, w, h, acc, seed, 20, 4, 8, 120);
                }
                break;
            case "unicorn":
                spots(g, w, h, 0xFFD6F2, seed, 20, 3, 5, 160);
                if (head) patch(g, w, h, 0, -0.55, 0.4, 0.28, 0xFFE0EE, 200);
                break;
            case "bear":
                if (head) patch(g, w, h, 0, -0.45, 0.45, 0.3, belly, 255);
                else {
                    patch(g, w, h, 0, -0.25, 0.5, 0.55, belly, 255);
                    g.setColor(ca(0xFF7A9C, 190));
                    g.fill(heart(sx(w, 0), sy(h, -0.15), 22));
                }
                break;
            case "turtle":
                patch(g, w, h, 0, -0.5, 0.6, 0.35, belly, 230);
                spots(g, w, h, acc, seed, 14, 4, 8, 140);
                break;
            case "bat":
                if (head) patch(g, w, h, 0, -0.1, 0.85, 0.55, belly, 255);
                break;
            case "fish":
                patch(g, w, h, 0, -0.6, 1.2, 0.5, belly, 230);
                scales(g, w, h, 0xFFB070);
                break;
            case "cloud": case "ghost":
                g.setPaint(new LinearGradientPaint(0, 0, 0, h, new float[]{0f, 0.55f, 1f}, new Color[]{c(0xFFFFFF), c(base), c(acc)}));
                g.fillRect(0, 0, w, h);
                break;
            case "slime":
                spots(g, w, h, light(base, 0.4f), seed, 10, 3, 6, 120);
                break;
            case "mush":
                patch(g, w, h, 0, -0.3, 0.6, 0.5, belly, 150);
                break;
            case "bunny":
                if (head) {
                    patch(g, w, h, 0, -0.45, 0.5, 0.32, belly, 255);
                    patch(g, w, h, 0, 0.7, 0.14, 0.35, 0xFFFFFF, 150);
                } else patch(g, w, h, 0, -0.35, 0.6, 0.55, belly, 255);
                break;
            case "wolf":
                if (head) {
                    patch(g, w, h, 0.5, -0.35, 0.5, 0.35, belly, 255);
                    patch(g, w, h, -0.5, -0.35, 0.5, 0.35, belly, 255);
                    patch(g, w, h, 0, 0.6, 0.35, 0.3, acc, 150);
                    for (int sd = -1; sd <= 1; sd += 2) patch(g, w, h, sd * 0.36, 0.34, 0.1, 0.06, belly, 230);
                } else {
                    patch(g, w, h, 0, -0.45, 0.8, 0.55, belly, 255);
                    patch(g, w, h, Math.PI, 0.75, 1.6, 0.55, acc, 170);   // dark saddle
                }
                break;
            case "horse":
                if (head) patch(g, w, h, 0, 0.1, 0.1, 0.6, acc, 235);   // blaze
                else patch(g, w, h, 0, -0.6, 0.6, 0.35, belly, 140);
                break;
            case "fawn":
                patch(g, w, h, 0, -0.45, 0.6, 0.5, belly, 255);
                if (!head) {
                    Random r = new Random(seed);
                    for (int i = 0; i < 46; i++) {
                        double az = r.nextDouble() * Math.PI * 2, el = 0.1 + r.nextDouble() * 0.75;
                        g.setColor(ca(acc, 235));
                        double ss = 5 + r.nextDouble() * 4;
                        g.fill(el(az / (Math.PI * 2) * w, sy(h, el), ss * 1.3, ss));
                    }
                }
                break;
            case "deer":
                patch(g, w, h, 0, -0.45, 0.6, 0.5, belly, 255);
                if (!head) patch(g, w, h, Math.PI, -0.1, 0.45, 0.35, 0xFFFFFF, 220);   // white rump
                break;
            case "lion":
                patch(g, w, h, 0, -0.45, 0.6, 0.5, belly, 255);
                break;
            case "tiger":
                if (head) {
                    for (int sd = -1; sd <= 1; sd += 2) patch(g, w, h, sd * 0.5, -0.35, 0.45, 0.35, belly, 255);
                    for (int sd = -1; sd <= 1; sd += 2) patch(g, w, h, sd * 0.33, 0.36, 0.1, 0.06, belly, 240);
                } else patch(g, w, h, 0, -0.6, 0.7, 0.4, belly, 255);
                stripes(g, w, h, acc, seed, head ? 14 : 22, head);
                break;
            case "retr":
                patch(g, w, h, 0, -0.4, 0.6, 0.5, belly, 220);
                furStrokes(g, w, h, light(base, 0.25f), seed + 5, 500, 60);
                break;
            case "afox":
                patch(g, w, h, 0, -0.4, 0.6, 0.5, 0xFFFFFF, 255);
                furStrokes(g, w, h, acc, seed + 5, 400, 45);
                break;
            case "eagle":
                // feather rows
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int row = 0; row < 7; row++)
                    for (int x = 0; x < w + 16; x += 16) {
                        double xx = x + (row % 2) * 8, y = 10 + row * 16;
                        g.setColor(ca(light(base, 0.3f), 140));
                        g.draw(new Arc2D.Double(xx - 8, y - 8, 16, 16, 180, 180, Arc2D.OPEN));
                    }
                break;
            case "eagleh":
                patch(g, w, h, 0, -0.5, 0.5, 0.3, 0xFFFFFF, 200);
                break;
            case "drake":
                if (!head) {
                    patch(g, w, h, 0, -0.55, 0.7, 0.45, belly, 255);
                    g.setStroke(new BasicStroke(2f));
                    g.setColor(ca(dark(belly, 0.25f), 170));
                    for (int i = 0; i < 6; i++) {
                        double y = sy(h, -0.95 + i * 0.13);
                        g.draw(new java.awt.geom.Line2D.Double(sx(w, 0.6), y, sx(w, -0.6), y));
                    }
                } else patch(g, w, h, 0, -0.55, 0.45, 0.3, belly, 230);
                scales(g, w, h, light(base, 0.25f));
                break;
            default:
                if (!head) patch(g, w, h, 0, -0.35, 0.55, 0.55, belly, 255);
        }
        g.dispose();
        return im;
    }

    /** Vertical, slightly wavy tiger stripes (not on the face / belly). */
    private static void stripes(Graphics2D g, int w, int h, int col, long seed, int n, boolean head) {
        Random r = new Random(seed);
        g.setColor(ca(col, 235));
        for (int i = 0; i < n; i++) {
            double az = -Math.PI + (i + r.nextDouble() * 0.5) * Math.PI * 2 / n;
            if (head && Math.abs(az) < 0.55) continue;   // keep the face free
            double x = sx(w, az), top = head ? sy(h, 1.3) : sy(h, 1.5), bot = head ? sy(h, 0.35) : sy(h, -0.35 + r.nextDouble() * 0.25);
            double wd = 3 + r.nextDouble() * 4;
            Path2D.Double p = new Path2D.Double();
            p.moveTo(x - wd, top);
            p.quadTo(x + 6, (top + bot) / 2, x, bot);
            p.quadTo(x + 6 + wd, (top + bot) / 2, x + wd, top);
            p.closePath();
            for (int k = -1; k <= 1; k++) {
                AffineTransform o = g.getTransform();
                g.translate(k * w, 0);
                g.fill(p);
                g.setTransform(o);
            }
        }
    }

    /** Bird wing (top edge = leading edge at y 0, root at x 0), primaries at the tip. */
    static BufferedImage featherWing(int base, int tip) {
        BufferedImage im = img(128, 64);
        Graphics2D g = gfx(im);
        Path2D.Double p = new Path2D.Double();
        p.moveTo(0, 2);
        p.curveTo(40, -2, 90, 0, 126, 10);
        p.lineTo(120, 26);
        for (int i = 0; i < 6; i++) {
            double x = 118 - i * 14, y = 34 + i * 4;
            p.lineTo(x, y + 12);
            p.lineTo(x - 7, y + 4);
        }
        p.quadTo(30, 50, 0, 44);
        p.closePath();
        g.setPaint(new GradientPaint(0, 0, c(light(base, 0.15f)), 128, 0, c(tip)));
        g.fill(p);
        g.setClip(p);
        g.setStroke(new BasicStroke(1.6f));
        for (int i = 0; i < 16; i++) {
            double x = 10 + i * 8;
            g.setColor(ca(dark(base, 0.35f), 150));
            g.draw(new java.awt.geom.Line2D.Double(x, 14, x - 6, 62));
        }
        // coverts
        g.setColor(ca(light(base, 0.2f), 200));
        g.fill(new java.awt.geom.Ellipse2D.Double(-10, -6, 90, 26));
        g.setClip(null);
        g.setStroke(new BasicStroke(2f));
        g.setColor(c(dark(base, 0.4f)));
        g.draw(new java.awt.geom.QuadCurve2D.Double(0, 2, 60, -2, 126, 10));
        g.dispose();
        return im;
    }

    /** Dragon wing membrane with bones in the given colors. */
    static BufferedImage drakeWing(int bone, int membrane) {
        BufferedImage im = img(128, 96);
        Graphics2D g = gfx(im);
        Path2D.Double p = new Path2D.Double();
        p.moveTo(0, 4);
        p.curveTo(50, -4, 100, 6, 126, 20);
        p.quadTo(108, 44, 118, 80);
        p.quadTo(92, 64, 76, 86);
        p.quadTo(58, 62, 34, 90);
        p.quadTo(18, 58, 0, 56);
        p.closePath();
        g.setPaint(new GradientPaint(0, 0, ca(light(membrane, 0.25f), 240), 0, 96, ca(membrane, 230)));
        g.fill(p);
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c(bone));
        g.drawLine(0, 5, 118, 78);
        g.drawLine(0, 5, 76, 84);
        g.drawLine(0, 5, 34, 88);
        g.draw(new java.awt.geom.QuadCurve2D.Double(0, 4, 60, -2, 126, 20));
        g.fill(new java.awt.geom.Ellipse2D.Double(121, 14, 8, 8));
        g.dispose();
        return im;
    }

    /** Fanned bird tail (root at the top center). */
    static BufferedImage tailFan(int base, int tip) {
        BufferedImage im = img(64, 64);
        Graphics2D g = gfx(im);
        for (int i = -3; i <= 3; i++) {
            AffineTransform o = g.getTransform();
            g.translate(32, 2);
            g.rotate(i * 0.16);
            g.setPaint(new GradientPaint(0, 0, c(base), 0, 60, c(tip)));
            g.fill(new RoundRectangle2D.Double(-6, 0, 12, 60, 10, 10));
            g.setColor(ca(dark(base, 0.3f), 160));
            g.drawLine(0, 4, 0, 56);
            g.setTransform(o);
        }
        g.dispose();
        return im;
    }

    private static void tabby(Graphics2D g, int w, int h, int acc) {
        g.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(ca(acc, 200));
        for (int i = 0; i < 5; i++) {
            double az = Math.PI + (i - 2) * 0.42;  // around the back
            double x = sx(w, az);
            g.draw(new java.awt.geom.QuadCurve2D.Double(x, sy(h, 1.4), x + 8, sy(h, 0.8), x - 2, sy(h, 0.15)));
            g.draw(new java.awt.geom.QuadCurve2D.Double(x + w, sy(h, 1.4), x + w + 8, sy(h, 0.8), x + w - 2, sy(h, 0.15)));
            g.draw(new java.awt.geom.QuadCurve2D.Double(x - w, sy(h, 1.4), x - w + 8, sy(h, 0.8), x - w - 2, sy(h, 0.15)));
        }
    }

    private static void spots(Graphics2D g, int w, int h, int col, long seed, int n, int min, int max, int alpha) {
        Random r = new Random(seed);
        for (int i = 0; i < n; i++) {
            double x = r.nextDouble() * w, y = 12 + r.nextDouble() * (h * 0.6), s = min + r.nextDouble() * (max - min);
            // keep the face free
            if (Math.abs(x - w * 0.25) < w * 0.12 && y > h * 0.3) continue;
            g.setColor(ca(col, alpha));
            g.fill(el(x, y, s * 1.4, s));
        }
    }

    private static void chevrons(Graphics2D g, double cx, double cy, int acc) {
        g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(ca(acc, 150));
        for (int row = 0; row < 4; row++)
            for (int col = -2; col <= 2; col++) {
                double x = cx + col * 12 + (row % 2) * 6, y = cy - 18 + row * 11;
                g.draw(new java.awt.geom.QuadCurve2D.Double(x - 4, y, x, y + 4, x + 4, y));
            }
    }

    private static void scales(Graphics2D g, int w, int h, int col) {
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(ca(col, 150));
        for (int row = 0; row < 6; row++)
            for (int x = 0; x < w + 12; x += 12) {
                double xx = x + (row % 2) * 6, y = 20 + row * 12;
                g.draw(new Arc2D.Double(xx - 6, y - 6, 12, 12, 180, 180, Arc2D.OPEN));
            }
    }

    // ================================================================== faces
    /** Face style. */
    static final class St {
        int iris = 0x4A2C1E;     // lower eye color
        int mouth;               // 0 = normal, 1 = cat "w", 2 = none (beak)
        boolean lashes, rim, led, whiskers;
        int line = 0x3B2320;     // mouth / brow line color
        float eye = 1f;          // eye size scale
        float gap = 1f;          // eye distance scale
        int blush = 0xFF7BA6;
    }

    static St style(String s) {
        int stage = 0;
        int dot = s.lastIndexOf('.');
        if (dot > 0) {
            try { stage = Integer.parseInt(s.substring(dot + 1)); } catch (NumberFormatException ignored) { }
            s = s.substring(0, dot);
        }
        St t = style0(s);
        // older pets: smaller, more realistic eyes
        if (stage == 1) { t.eye *= 0.86f; t.gap *= 1.03f; }
        else if (stage >= 2) { t.eye *= 0.72f; t.gap *= 1.08f; }
        return t;
    }

    private static St style0(String s) {
        St t = new St();
        switch (s) {
            case "cat": t.iris = 0x3FAE6A; t.mouth = 1; t.whiskers = true; break;
            case "dog": t.iris = 0x6A3A1E; t.mouth = 1; break;
            case "beak": t.iris = 0x3A2A40; t.mouth = 2; break;
            case "owl": t.iris = 0xE09A1A; t.mouth = 2; t.eye = 1.15f; t.gap = 1.05f; break;
            case "gem": t.iris = 0xA05CF0; t.lashes = true; break;
            case "amber": t.iris = 0xE0901E; break;
            case "blue": t.iris = 0x3E86E6; break;
            case "rim": t.iris = 0x4A3A70; t.rim = true; break;
            case "led": t.iris = 0x6FF3FF; t.led = true; t.line = 0x6FF3FF; t.blush = 0xFF6FC8; break;
            case "blob": t.iris = 0x3A3350; t.eye = 0.92f; t.gap = 0.92f; break;
            case "pink": t.iris = 0xE0508A; t.lashes = true; break;
            // realistic pets (the growth stage shrinks the eyes further)
            case "wolf": t.iris = 0xD9A21E; t.mouth = 1; t.eye = 0.92f; t.gap = 1.05f; break;
            case "horse": t.iris = 0x4A2C1C; t.lashes = true; t.eye = 0.9f; t.gap = 1.3f; break;
            case "deer": t.iris = 0x2E1C14; t.lashes = true; t.eye = 1.0f; t.gap = 1.22f; break;
            case "lion": t.iris = 0xD08A1A; t.mouth = 1; t.eye = 0.9f; break;
            case "tiger": t.iris = 0xE8B21E; t.mouth = 1; t.eye = 0.9f; break;
            case "retr": t.iris = 0x5A341A; t.mouth = 1; t.eye = 0.95f; break;
            case "afox": t.iris = 0xC8902A; t.mouth = 1; t.eye = 0.95f; break;
            case "eagle": t.iris = 0xF0C020; t.mouth = 2; t.eye = 0.85f; t.gap = 1.1f; break;
            case "drake": t.iris = 0xF0A020; t.eye = 0.9f; break;
            default: break;
        }
        return t;
    }

    // layout (256 x 192)
    private static final double EY = 84, EW = 48, EH = 60, EX = 50;

    static BufferedImage eyes(St s, String emo, boolean closed) {
        BufferedImage im = img(FW, FH);
        Graphics2D g = gfx(im);
        double ew = EW * s.eye, eh = EH * s.eye, ex = EX * s.gap;
        for (int side = -1; side <= 1; side += 2) {
            double cx = 128 + side * ex, cy = EY;
            boolean inner = true;
            if (closed) { closedEye(g, s, emo, cx, cy, ew, eh, side); continue; }
            switch (emo) {
                case "love": heartEye(g, s, cx, cy + 2, ew * 1.2); break;
                case "excited": starEye(g, s, cx, cy + 2, ew * 0.78); break;
                case "dizzy": spiralEye(g, s, cx, cy + 2, ew * 0.55); break;
                case "sleepy": {
                    Shape clip = new Rectangle2D.Double(cx - ew, cy - eh * 0.02, ew * 2, eh);
                    g.setClip(clip);
                    openEye(g, s, cx, cy + 4, ew, eh * 0.95, false, 0);
                    g.setClip(null);
                    lid(g, s, cx - ew * 0.56, cy - eh * 0.02, cx + ew * 0.56, cy - eh * 0.02);
                    break;
                }
                case "angry": {
                    // cut the inner top corner diagonally
                    double ix = cx - side * ew * 0.6, ox = cx + side * ew * 0.6;
                    Path2D.Double cut = new Path2D.Double();
                    cut.moveTo(ix, cy + eh * 0.02);
                    cut.lineTo(ox, cy - eh * 0.34);
                    cut.lineTo(ox, cy + eh);
                    cut.lineTo(ix, cy + eh);
                    cut.closePath();
                    g.setClip(cut);
                    openEye(g, s, cx, cy + 4, ew * 0.95, eh * 0.9, false, 0);
                    g.setClip(null);
                    lid(g, s, ix, cy + eh * 0.02, ox, cy - eh * 0.34);
                    break;
                }
                case "sad": openEye(g, s, cx, cy + 3, ew * 1.04, eh * 1.02, true, 1); break;
                case "curious": {
                    float k = side < 0 ? 1.1f : 0.9f;
                    openEye(g, s, cx, cy + (side < 0 ? -2 : 3), ew * k, eh * k, false, 0);
                    break;
                }
                default: openEye(g, s, cx, cy, ew, eh, true, 2); break;
            }
        }
        g.dispose();
        return im;
    }

    private static void lid(Graphics2D g, St s, double x0, double y0, double x1, double y1) {
        g.setStroke(new BasicStroke(6.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(s.led ? c(s.iris) : c(0x241418));
        g.draw(new java.awt.geom.Line2D.Double(x0, y0, x1, y1));
    }

    /**
     * Big shiny eye.
     * @param mode 0 = normal, 1 = watery (sad), 2 = sparkly (happy)
     */
    private static void openEye(Graphics2D g, St s, double cx, double cy, double w, double h, boolean big, int mode) {
        if (s.led) {
            soft(g, cx, cy, w * 1.6, h * 1.5, s.iris, 110);
            g.setColor(c(light(s.iris, 0.3f)));
            g.fill(new RoundRectangle2D.Double(cx - w * 0.4, cy - h * 0.45, w * 0.8, h * 0.9, w * 0.7, w * 0.7));
            g.setColor(ca(0xFFFFFF, 200));
            g.fill(el(cx - w * 0.12, cy - h * 0.2, w * 0.2, h * 0.2));
            return;
        }
        if (s.rim) {
            g.setColor(c(0xFFFFFF));
            g.fill(el(cx, cy, w + 9, h + 9));
        }
        Ellipse2D e = el(cx, cy, w, h);
        g.setPaint(new LinearGradientPaint((float) cx, (float) (cy - h / 2), (float) cx, (float) (cy + h / 2), new float[]{0f, 0.45f, 1f},
                new Color[]{c(0x1B1020), c(mix(0x1B1020, s.iris, 0.35f)), c(light(s.iris, 0.25f))}));
        g.fill(e);
        // pupil
        g.setColor(ca(0x0C0610, 170));
        g.fill(el(cx, cy - h * 0.05, w * 0.5, h * 0.55));
        // iris ring glow at the bottom
        g.setClip(e);
        soft(g, cx, cy + h * 0.42, w * 0.9, h * 0.45, light(s.iris, 0.55f), 160);
        g.setClip(null);
        g.setStroke(new BasicStroke(3f));
        g.setColor(c(0x1B1020));
        g.draw(e);
        // highlights
        g.setColor(c(0xFFFFFF));
        g.fill(el(cx - w * 0.17, cy - h * 0.2, w * 0.46, h * 0.38));
        g.fill(el(cx + w * 0.2, cy + h * 0.2, w * 0.18, w * 0.18));
        if (mode == 1) {
            // watery: extra shine + tear film
            g.setClip(e);
            g.setColor(ca(0x9FE0FF, 150));
            g.fill(el(cx, cy + h * 0.5, w * 1.1, h * 0.45));
            g.setClip(null);
            g.setColor(ca(0xFFFFFF, 230));
            g.fill(el(cx + w * 0.02, cy + h * 0.02, w * 0.14, w * 0.14));
            g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(ca(0xFFFFFF, 200));
            g.draw(new Arc2D.Double(cx - w * 0.36, cy - h * 0.2, w * 0.72, h * 0.7, 200, 60, Arc2D.OPEN));
        } else if (mode == 2) {
            g.setColor(c(0xFFFFFF));
            g.fill(starShape(cx + w * 0.18, cy - h * 0.28, w * 0.13, w * 0.04, 4, 0));
        }
        if (s.lashes) {
            g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(c(0x1B1020));
            double sd = cx < 128 ? -1 : 1;
            for (int i = 0; i < 2; i++) {
                double a = Math.toRadians(sd < 0 ? 150 + i * 18 : 30 - i * 18);
                double x0 = cx + Math.cos(a) * w * 0.5, y0 = cy - Math.sin(a) * h * 0.5;
                g.draw(new java.awt.geom.Line2D.Double(x0, y0, x0 + Math.cos(a) * 9, y0 - Math.sin(a) * 9));
            }
        }
    }

    private static void closedEye(Graphics2D g, St s, String emo, double cx, double cy, double w, double h, int side) {
        Color col = s.led ? c(light(s.iris, 0.3f)) : c(0x241418);
        if (s.led) soft(g, cx, cy + 6, w * 1.4, h * 0.8, s.iris, 90);
        if (s.rim) {
            g.setColor(ca(0xFFFFFF, 230));
            g.fill(el(cx, cy + 8, w * 0.9, h * 0.45));
        }
        g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(col);
        switch (emo) {
            case "happy": case "love":
                // ^ ^
                g.draw(new Arc2D.Double(cx - w * 0.42, cy - h * 0.05, w * 0.84, h * 0.6, 20, 140, Arc2D.OPEN));
                break;
            case "excited": {
                // > <
                Path2D.Double p = new Path2D.Double();
                double d = -side;
                p.moveTo(cx - d * w * 0.3, cy - h * 0.18);
                p.lineTo(cx + d * w * 0.3, cy + h * 0.04);
                p.lineTo(cx - d * w * 0.3, cy + h * 0.26);
                g.draw(p);
                break;
            }
            case "dizzy": {
                g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                double r = w * 0.28;
                g.draw(new java.awt.geom.Line2D.Double(cx - r, cy - r + 6, cx + r, cy + r + 6));
                g.draw(new java.awt.geom.Line2D.Double(cx - r, cy + r + 6, cx + r, cy - r + 6));
                break;
            }
            case "angry":
                g.draw(new java.awt.geom.Line2D.Double(cx - side * w * 0.4, cy + h * 0.06, cx + side * w * 0.4, cy - h * 0.08));
                break;
            default: {
                // sleepy/sad/curious: soft U with lashes
                g.draw(new Arc2D.Double(cx - w * 0.42, cy - h * 0.2, w * 0.84, h * 0.5, 200, 140, Arc2D.OPEN));
                g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                double ox = cx + side * w * 0.4;
                g.draw(new java.awt.geom.Line2D.Double(ox, cy + h * 0.02, ox + side * 7, cy + h * 0.08));
            }
        }
    }

    private static void heartEye(Graphics2D g, St s, double cx, double cy, double sz) {
        if (s.led) soft(g, cx, cy, sz * 1.3, sz * 1.2, 0xFF6FC8, 110);
        if (s.rim) { g.setColor(c(0xFFFFFF)); g.fill(el(cx, cy, sz * 0.95, sz * 0.9)); }
        Shape h = heart(cx, cy, sz);
        g.setPaint(new GradientPaint(0, (float) (cy - sz * 0.4), c(0xFF7FAE), 0, (float) (cy + sz * 0.4), c(0xE8205C)));
        g.fill(h);
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c(0xA8103E));
        if (!s.led) g.draw(h);
        g.setColor(c(0xFFFFFF));
        g.fill(el(cx - sz * 0.2, cy - sz * 0.17, sz * 0.2, sz * 0.14));
        g.fill(el(cx + sz * 0.12, cy + sz * 0.08, sz * 0.08, sz * 0.08));
    }

    private static void starEye(Graphics2D g, St s, double cx, double cy, double r) {
        if (s.led) soft(g, cx, cy, r * 2.6, r * 2.6, 0xFFE27A, 110);
        if (s.rim) { g.setColor(c(0xFFFFFF)); g.fill(el(cx, cy, r * 2.1, r * 2.1)); }
        Shape st = starShape(cx, cy + 2, r, r * 0.5, 5, 0);
        g.setPaint(new GradientPaint(0, (float) (cy - r), c(0xFFF3A0), 0, (float) (cy + r), c(0xFFAA1C)));
        g.fill(st);
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c(0xD9731A));
        g.draw(st);
        g.setColor(c(0xFFFFFF));
        g.fill(el(cx - r * 0.25, cy - r * 0.2, r * 0.32, r * 0.24));
    }

    private static void spiralEye(Graphics2D g, St s, double cx, double cy, double r) {
        if (!s.led) {
            g.setColor(ca(0xFFFFFF, 240));
            g.fill(el(cx, cy, r * 2.2, r * 2.2));
            g.setStroke(new BasicStroke(2.5f));
            g.setColor(c(0x241418));
            g.draw(el(cx, cy, r * 2.2, r * 2.2));
        } else soft(g, cx, cy, r * 2.8, r * 2.8, s.iris, 100);
        Path2D.Double p = new Path2D.Double();
        for (int i = 0; i <= 80; i++) {
            double t = i / 80.0, a = t * Math.PI * 5.2, rr = r * 0.95 * t;
            double x = cx + Math.cos(a) * rr, y = cy + Math.sin(a) * rr;
            if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
        }
        g.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(s.led ? c(light(s.iris, 0.3f)) : c(0x3A2450));
        g.draw(p);
    }

    static BufferedImage face(St s, String emo) {
        BufferedImage im = img(FW, FH);
        Graphics2D g = gfx(im);
        double ex = EX * s.gap, ew = EW * s.eye, eh = EH * s.eye;
        Color line = c(s.line);
        // ---------------- blush
        int bl = s.blush;
        int ba = "love".equals(emo) ? 210 : "angry".equals(emo) ? 150 : "sad".equals(emo) ? 90 : "excited".equals(emo) ? 180 : 140;
        if ("angry".equals(emo)) bl = 0xFF5A6A;
        for (int side = -1; side <= 1; side += 2) {
            double bx = 128 + side * (ex + ew * 0.62), by = EY + eh * 0.62;
            soft(g, bx, by, 50, 30, bl, ba);
            if ("love".equals(emo) || "excited".equals(emo) || "happy".equals(emo)) {
                g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(ca(0xFFFFFF, 150));
                for (int i = -1; i <= 1; i++) g.draw(new java.awt.geom.Line2D.Double(bx + i * 8 - 3, by + 5, bx + i * 8 + 3, by - 5));
            }
        }
        // ---------------- whiskers
        if (s.whiskers) {
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(ca(0x5A3A2A, 150));
            for (int side = -1; side <= 1; side += 2)
                for (int i = 0; i < 2; i++) {
                    double x0 = 128 + side * (ex + ew * 0.95), y0 = EY + eh * 0.66 + i * 9;
                    g.draw(new java.awt.geom.QuadCurve2D.Double(x0, y0, x0 + side * 12, y0 - 2 + i * 3, x0 + side * 24, y0 + i * 6 - 2));
                }
        }
        // ---------------- brows
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(s.led ? c(s.iris) : ca(s.line, 235));
        double byy = EY - eh * 0.62;
        switch (emo) {
            case "angry":
                for (int side = -1; side <= 1; side += 2) {
                    double cx = 128 + side * ex;
                    g.draw(new java.awt.geom.Line2D.Double(cx - side * ew * 0.55, byy + 12, cx + side * ew * 0.4, byy - 6));
                }
                break;
            case "sad":
                for (int side = -1; side <= 1; side += 2) {
                    double cx = 128 + side * ex;
                    g.draw(new java.awt.geom.QuadCurve2D.Double(cx - side * ew * 0.45, byy - 6, cx, byy - 2, cx + side * ew * 0.4, byy + 8));
                }
                break;
            case "curious": {
                double cx = 128 - ex;   // viewer's left eye (bigger) gets the raised brow
                g.draw(new java.awt.geom.QuadCurve2D.Double(cx - ew * 0.45, byy - 4, cx, byy - 20, cx + ew * 0.4, byy - 6));
                double cx2 = 128 + ex;
                g.draw(new java.awt.geom.Line2D.Double(cx2 - ew * 0.35, byy + 6, cx2 + ew * 0.35, byy + 4));
                break;
            }
            case "sleepy":
                break;
            default:
                break;
        }
        // ---------------- mouth
        double my = EY + eh * 0.66, mx = 128;
        g.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(line);
        if (s.mouth != 2) {
            switch (emo) {
                case "happy":
                    if (s.mouth == 1) { omega(g, mx, my, 12, line); openMouth(g, s, mx, my + 7, 14, 11); }
                    else openMouth(g, s, mx, my, 24, 16);
                    break;
                case "love":
                    if (s.mouth == 1) omega(g, mx, my, 12, line);
                    else g.draw(new Arc2D.Double(mx - 11, my - 10, 22, 16, 200, 140, Arc2D.OPEN));
                    g.setColor(ca(0xFF4F8B, 230));
                    g.fill(heart(mx + 30, my - 2, 16));
                    break;
                case "sleepy":
                    g.setColor(line);
                    g.setStroke(new BasicStroke(3.5f));
                    g.draw(el(mx, my + 2, 9, 8));
                    break;
                case "sad":
                    g.draw(new java.awt.geom.QuadCurve2D.Double(mx - 13, my + 7, mx, my - 6, mx + 13, my + 7));
                    break;
                case "excited":
                    if (s.mouth == 1) omega(g, mx, my - 2, 12, line);
                    openMouth(g, s, mx, my + (s.mouth == 1 ? 5 : 0), s.mouth == 1 ? 20 : 30, s.mouth == 1 ? 16 : 22);
                    break;
                case "angry": {
                    Path2D.Double p = new Path2D.Double();
                    p.moveTo(mx - 12, my + 6);
                    p.quadTo(mx - 6, my - 1, mx, my + 2);
                    p.quadTo(mx + 6, my - 1, mx + 12, my + 6);
                    g.draw(p);
                    g.setColor(c(0xFFFFFF));
                    Path2D.Double f = new Path2D.Double();
                    f.moveTo(mx + 4, my + 1.5);
                    f.lineTo(mx + 9, my + 3);
                    f.lineTo(mx + 7, my + 9);
                    f.closePath();
                    if (!s.led) g.fill(f);
                    break;
                }
                case "curious":
                    g.setColor(s.led ? line : c(0x6A2030));
                    g.fill(el(mx + 8, my + 3, 11, 12));
                    g.setColor(line);
                    g.setStroke(new BasicStroke(3.5f));
                    g.draw(el(mx + 8, my + 3, 11, 12));
                    break;
                case "dizzy": {
                    Path2D.Double p = new Path2D.Double();
                    for (int i = 0; i <= 30; i++) {
                        double x = mx - 20 + 40 * i / 30.0, y = my + 3 + Math.sin(i / 30.0 * Math.PI * 3) * 4.5;
                        if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
                    }
                    g.draw(p);
                    break;
                }
                default:
                    g.draw(new Arc2D.Double(mx - 11, my - 10, 22, 16, 200, 140, Arc2D.OPEN));
            }
        } else if ("love".equals(emo)) {
            g.setColor(ca(0xFF4F8B, 230));
            g.fill(heart(mx + 40, my - 6, 16));
        }
        // ---------------- extras
        switch (emo) {
            case "sad":
                for (int side = -1; side <= 1; side += 2) {
                    double tx = 128 + side * ex + side * ew * 0.18, ty = EY + eh * 0.38;
                    g.setPaint(new GradientPaint(0, (float) ty, ca(0x9FE3FF, 220), 0, (float) (ty + 70), ca(0x6FC8FF, 60)));
                    g.fill(new RoundRectangle2D.Double(tx - 5, ty, 10, 70, 10, 10));
                }
                // drop
                {
                    double dx = 128 + ex + ew * 0.55, dy = EY + eh * 0.8;
                    Path2D.Double d = new Path2D.Double();
                    d.moveTo(dx, dy - 12);
                    d.curveTo(dx + 9, dy, dx + 7, dy + 9, dx, dy + 9);
                    d.curveTo(dx - 7, dy + 9, dx - 9, dy, dx, dy - 12);
                    g.setColor(ca(0x8FDBFF, 235));
                    g.fill(d);
                    g.setColor(ca(0xFFFFFF, 230));
                    g.fill(el(dx - 2.5, dy + 1, 3.5, 5));
                }
                break;
            case "sleepy": {
                // snot bubble
                double bx = 128 + ex * 0.62, by = EY + eh * 0.72, r = 15;
                g.setColor(ca(0xBFEFFF, 150));
                g.fill(el(bx, by, r * 2, r * 2));
                g.setStroke(new BasicStroke(2.2f));
                g.setColor(ca(0x7FCFF5, 220));
                g.draw(el(bx, by, r * 2, r * 2));
                g.setColor(ca(0xFFFFFF, 240));
                g.fill(el(bx - 5, by - 6, 8, 6));
                break;
            }
            case "angry": {
                // anger mark
                double vx = 128 + ex + ew * 0.62, vy = 26;
                g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(c(0xFF3B4A));
                for (int k = 0; k < 4; k++) {
                    AffineTransform o = g.getTransform();
                    g.translate(vx, vy);
                    g.rotate(Math.PI / 2 * k);
                    g.draw(new java.awt.geom.QuadCurve2D.Double(4, -12, 4, -4, 12, -4));
                    g.setTransform(o);
                }
                break;
            }
            case "dizzy": {
                g.setStroke(new BasicStroke(2.4f));
                g.setColor(ca(0x9FD9FF, 200));
                double dx = 128 - ex - ew * 0.7, dy = EY - eh * 0.55;
                Path2D.Double d = new Path2D.Double();
                d.moveTo(dx, dy - 8);
                d.curveTo(dx + 6, dy, dx + 5, dy + 6, dx, dy + 6);
                d.curveTo(dx - 5, dy + 6, dx - 6, dy, dx, dy - 8);
                g.setColor(ca(0x9FDFFF, 220));
                g.fill(d);
                break;
            }
            default: break;
        }
        g.dispose();
        return im;
    }

    private static void omega(Graphics2D g, double mx, double my, double r, Color line) {
        g.setColor(line);
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Double(mx - r, my - r * 0.9, r, r, 200, 160, Arc2D.OPEN));
        g.draw(new Arc2D.Double(mx, my - r * 0.9, r, r, 180, 160, Arc2D.OPEN));
    }

    /** Open "D" smile with tongue. */
    private static void openMouth(Graphics2D g, St s, double mx, double my, double w, double h) {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(mx - w / 2, my - h * 0.3);
        p.quadTo(mx, my - h * 0.1, mx + w / 2, my - h * 0.3);
        p.curveTo(mx + w * 0.45, my + h * 0.75, mx - w * 0.45, my + h * 0.75, mx - w / 2, my - h * 0.3);
        p.closePath();
        if (s.led) {
            g.setColor(c(light(s.iris, 0.3f)));
            g.fill(p);
            return;
        }
        g.setColor(c(0x7A2034));
        g.fill(p);
        Shape old = g.getClip();
        g.setClip(p);
        g.setColor(c(0xFF7F96));
        g.fill(el(mx, my + h * 0.55, w * 0.62, h * 0.7));
        g.setClip(old);
        g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c(s.line));
        g.draw(p);
    }
}
