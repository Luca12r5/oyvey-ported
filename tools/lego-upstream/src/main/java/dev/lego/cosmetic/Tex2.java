package dev.lego.cosmetic;

import static dev.lego.cosmetic.Capes.add;
import static dev.lego.cosmetic.Capes.col;
import static dev.lego.cosmetic.Capes.fbm;
import static dev.lego.cosmetic.Capes.hash;
import static dev.lego.cosmetic.Capes.mix;
import static dev.lego.cosmetic.Capes.ramp;
import static dev.lego.cosmetic.Capes.shade;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.function.Consumer;

/** Additional cosmetic textures (wings, hats, face, back, auras). Tex3 holds more. */
final class Tex2 {
    private Tex2() { }

    static CTex.T img(int w, int h, Consumer<Graphics2D> p) {
        BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        dev.lego.ui.Gx.hints(g);
        p.accept(g);
        g.dispose();
        return CTex.of(im);
    }

    /** Texture from a per-pixel function returning ARGB (u, v in 0..1). */
    interface PF { int at(double u, double v, int x, int y); }

    static CTex.T pix(int w, int h, PF f) {
        BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] p = Capes.px(im);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) p[y * w + x] = f.at((x + 0.5) / w, (y + 0.5) / h, x, y);
        return CTex.of(im);
    }

    static int argb(int rgb, double a) { return ((int) Math.round(Math.max(0, Math.min(1, a)) * 255) << 24) | (rgb & 0xFFFFFF); }

    /** Feather styles: base (root), mid, tip, shaft, pattern. */
    private static final Object[][] FEATHERS = {
            {"angel", 0xFFFFFF, 0xEEF2FA, 0xC9D3E6, 0xE8D8A8, "none"},
            {"gold", 0xFFF6C0, 0xF2C230, 0xB07A12, 0xFFF3B0, "metal"},
            {"phoenix", 0xFFF3A0, 0xFF8A1E, 0xC8141A, 0xFFE27A, "fire"},
            {"shadow", 0x4A4E62, 0x1E2032, 0x08080F, 0x9B6BFF, "rim"},
            {"owl", 0xF0DDB8, 0xA67C4E, 0x4E321A, 0xE8D2A8, "bars"},
            {"rainbow", 0xFFFFFF, 0xF4F4F4, 0xDADADA, 0xFFFFFF, "none"},
            {"galaxy", 0x3A1A7A, 0x1A0A3A, 0x07041A, 0xC8A8FF, "stars"},
            {"crystal", 0xF0FFFF, 0x8FE0FF, 0x3A7AFF, 0xFFFFFF, "crystal"},
            {"fire", 0xFFF6C0, 0xFFB030, 0xFF3A0A, 0xFFFFFF, "flame"},
    };

    static CTex.T paint(String name) {
        if (name.startsWith("fea_") || name.startsWith("cov_")) {
            String st = name.substring(4);
            if (st.startsWith("x")) {   // custom colors: x<base>_<mid>_<tip>_<shaft>_<pattern>
                String[] p = st.substring(1).split("_");
                return feather(Integer.parseInt(p[0], 16), Integer.parseInt(p[1], 16), Integer.parseInt(p[2], 16), Integer.parseInt(p[3], 16), p[4], name.startsWith("cov_"));
            }
            for (Object[] f : FEATHERS)
                if (f[0].equals(st)) return feather((int) f[1], (int) f[2], (int) f[3], (int) f[4], (String) f[5], name.startsWith("cov_"));
            return null;
        }
        if (name.startsWith("mem_x")) {   // custom membrane: mem_x<base>_<light>_<dark>_<vein>_<alpha>
            String[] p = name.substring(5).split("_");
            return membrane(Integer.parseInt(p[0], 16), Integer.parseInt(p[1], 16), Integer.parseInt(p[2], 16), Integer.parseInt(p[3], 16), Double.parseDouble(p[4]));
        }
        switch (name) {
            // membranes: base, light, dark, vein, edge alpha
            case "mem_demon": return membrane(0x6A1018, 0xC02A30, 0x1A0406, 0xFF6A4A, 0.93);
            case "mem_icedragon": return membrane(0x1A5A9A, 0x7AD8FF, 0x0A1E3A, 0xE0FAFF, 0.82);
            case "mem_neon": return membrane(0x2A0A4A, 0x7A2AC8, 0x0A0218, 0xFF6AF5, 0.8);
            case "mem_bat": return membrane(0x4A3228, 0x8A6A58, 0x1A120E, 0xC8A088, 0.9);
            case "mem_dragon": return membrane(0xB0400A, 0xFFA040, 0x4A1004, 0xFFE08A, 0.9);
            case "bone": return pix(16, 64, (u, v, x, y) -> 0xFF000000 | mix(0xFFFFFF, 0xA0A0A0, Math.abs(u - 0.35) * 1.6));
            case "wing_fairy": return fairyWing();
            case "wing_butterfly2": return butterfly();
            case "mech_blade": return mechBlade();
            case "mech_frame": return pix(32, 32, (u, v, x, y) -> 0xFF000000 | shade(mix(0x5A6270, 0x2A2E36, v), 0.9 + 0.2 * hash(x / 4, y / 4, 3)));
            case "glow_line": return pix(16, 16, (u, v, x, y) -> argb(mix(0xFFFFFF, 0xFFFFFF, 0), 1 - Math.pow(Math.abs(v - 0.5) * 2, 2)));
            default: return Tex3.paint(name);
        }
    }

    // ================================================================== feathers (root at the top, tip at the bottom)
    static CTex.T feather(int base, int mid, int tip, int shaft, String pat, boolean covert) {
        int W = 64, H = covert ? 128 : 256;
        return img(W, H, g -> {
            double w = W, h = H;
            Path2D f = new Path2D.Double();
            if (pat.equals("flame")) {
                f.moveTo(w * 0.1, 0); f.lineTo(w * 0.9, 0);
                f.curveTo(w * 1.02, h * 0.35, w * 0.8, h * 0.62, w * 0.6, h * 0.8);
                f.curveTo(w * 0.62, h * 0.9, w * 0.5, h * 0.96, w * 0.5, h);
                f.curveTo(w * 0.4, h * 0.9, w * 0.3, h * 0.84, w * 0.34, h * 0.74);
                f.curveTo(w * 0.1, h * 0.6, w * -0.02, h * 0.3, w * 0.1, 0);
            } else if (covert) {
                f.moveTo(w * 0.04, 0); f.lineTo(w * 0.96, 0);
                f.curveTo(w * 1.0, h * 0.5, w * 0.8, h * 0.9, w * 0.5, h * 0.99);
                f.curveTo(w * 0.2, h * 0.9, w * 0.0, h * 0.5, w * 0.04, 0);
            } else {
                f.moveTo(w * 0.1, 0); f.lineTo(w * 0.9, 0);
                f.curveTo(w * 0.98, h * 0.4, w * 0.92, h * 0.8, w * 0.62, h * 0.97);
                f.quadTo(w * 0.5, h * 1.0, w * 0.4, h * 0.97);
                f.curveTo(w * 0.1, h * 0.84, w * 0.02, h * 0.4, w * 0.1, 0);
            }
            g.setPaint(new LinearGradientPaint(0, 0, 0, (float) h, new float[]{0f, 0.45f, 1f}, new Color[]{new Color(base), new Color(mid), new Color(tip)}));
            if (pat.equals("crystal")) g.setPaint(new LinearGradientPaint(0, 0, 0, (float) h, new float[]{0f, 0.45f, 1f}, new Color[]{col(base, 0.85), col(mid, 0.7), col(tip, 0.6)}));
            if (pat.equals("flame")) g.setPaint(new LinearGradientPaint(0, 0, 0, (float) h, new float[]{0f, 0.4f, 0.8f, 1f}, new Color[]{new Color(base), new Color(mid), new Color(tip), col(tip, 0.1)}));
            g.fill(f);
            g.setClip(f);
            // barbs: fine diagonal lines on both vanes
            Random r = new Random(covert ? 3 : 4);
            if (!pat.equals("flame") && !pat.equals("crystal")) {
                g.setStroke(new BasicStroke(1f));
                for (double y = 2; y < h; y += 3.2) {
                    double wv = 0.5 + r.nextDouble() * 0.2;
                    g.setColor(col(r.nextBoolean() ? 0xFFFFFF : 0x000000, 0.08 + r.nextDouble() * 0.06));
                    g.draw(new QuadCurve2D.Double(w / 2, y, w * 0.75, y + 6, w, y + 14 * wv));
                    g.draw(new QuadCurve2D.Double(w / 2, y, w * 0.25, y + 6, 0, y + 14 * wv));
                }
                // occasional splits in the vane
                g.setColor(col(0x000000, 0.22));
                for (int i = 0; i < (covert ? 1 : 3); i++) {
                    double y = h * (0.35 + r.nextDouble() * 0.5), side = r.nextBoolean() ? 1 : -1;
                    g.draw(new QuadCurve2D.Double(w / 2 + side * 4, y, w / 2 + side * 14, y + 8, w / 2 + side * 30, y + 20));
                }
            }
            if (pat.equals("bars")) {
                for (double y = h * 0.18; y < h * 0.95; y += h * (covert ? 0.3 : 0.17)) {
                    Path2D b = new Path2D.Double();
                    b.moveTo(0, y); b.quadTo(w / 2, y - 7, w, y); b.lineTo(w, y + 6); b.quadTo(w / 2, y - 1, 0, y + 6); b.closePath();
                    g.setColor(col(0x2A1808, 0.32));
                    g.fill(b);
                }
                g.setColor(col(0xFFF6E0, 0.25));
                g.fill(new Ellipse2D.Double(w * 0.12, h * 0.05, w * 0.3, h * 0.08));
            }
            if (pat.equals("metal")) {
                g.setPaint(new GradientPaint(0, 0, col(0xFFFFFF, 0.55), (float) (w * 0.45), 0, col(0xFFFFFF, 0)));
                g.fillRect(0, 0, (int) (w * 0.45), (int) h);
                g.setPaint(new GradientPaint((float) (w * 0.6), 0, col(0x5A3A00, 0), (float) w, 0, col(0x5A3A00, 0.45)));
                g.fillRect((int) (w * 0.6), 0, (int) w, (int) h);
            }
            if (pat.equals("fire")) {
                g.setPaint(new LinearGradientPaint(0, (float) (h * 0.55), 0, (float) h, new float[]{0f, 1f}, new Color[]{col(0xFFE27A, 0), col(0xFFE27A, 0.7)}));
                g.fillRect(0, (int) (h * 0.55), (int) w, (int) h);
            }
            if (pat.equals("rim")) {
                g.setClip(null);
                for (int i = 4; i >= 1; i--) {
                    g.setColor(col(0x9B6BFF, 0.12 * (5 - i)));
                    g.setStroke(new BasicStroke(i * 2.2f));
                    g.draw(f);
                }
                g.setClip(f);
            }
            if (pat.equals("stars")) {
                for (int i = 0; i < (covert ? 10 : 22); i++) {
                    double x = r.nextDouble() * w, y = r.nextDouble() * h, s = 1 + Math.pow(r.nextDouble(), 3) * 4;
                    g.setPaint(new RadialGradientPaint((float) x, (float) y, (float) (s * 2.5), new float[]{0f, 0.3f, 1f}, new Color[]{Color.WHITE, col(0xC8A8FF, 0.7), col(0xC8A8FF, 0)}));
                    g.fill(new Ellipse2D.Double(x - s * 2.5, y - s * 2.5, s * 5, s * 5));
                }
                g.setPaint(new RadialGradientPaint((float) (w * 0.4), (float) (h * 0.55), (float) (h * 0.35), new float[]{0f, 1f}, new Color[]{col(0xFF5AB0, 0.35), col(0xFF5AB0, 0)}));
                g.fillRect(0, 0, (int) w, (int) h);
            }
            if (pat.equals("crystal")) {
                g.setStroke(new BasicStroke(1.2f));
                for (int i = 0; i < 7; i++) {
                    double y = r.nextDouble() * h;
                    g.setColor(col(0xFFFFFF, 0.5));
                    g.draw(new java.awt.geom.Line2D.Double(0, y, w, y + (r.nextDouble() - 0.5) * 60));
                }
                g.setPaint(new GradientPaint(0, 0, col(0xFFFFFF, 0.6), (float) (w * 0.5), 0, col(0xFFFFFF, 0)));
                g.fillRect(0, 0, (int) (w * 0.5), (int) h);
            }
            if (pat.equals("flame")) {
                g.setPaint(new RadialGradientPaint((float) (w / 2), (float) (h * 0.15), (float) (h * 0.4), new float[]{0f, 1f}, new Color[]{col(0xFFFFFF, 0.7), col(0xFFFFFF, 0)}));
                g.fillRect(0, 0, (int) w, (int) h);
            }
            // shading: darker vane on one side for depth
            if (!pat.equals("flame")) {
                g.setPaint(new GradientPaint((float) (w * 0.5), 0, col(0x000000, 0), (float) w, 0, col(0x000000, pat.equals("crystal") ? 0.1 : 0.28)));
                g.fillRect((int) (w * 0.5), 0, (int) w, (int) h);
            }
            g.setClip(null);
            // shaft
            if (!pat.equals("flame")) {
                g.setPaint(new GradientPaint(0, 0, new Color(shaft), 0, (float) h, col(shaft, 0.4)));
                g.setStroke(new BasicStroke(covert ? 2.2f : 2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new QuadCurve2D.Double(w / 2, 0, w * 0.53, h * 0.5, w * 0.5, h * 0.93));
                g.setColor(col(mix(tip, 0x000000, 0.5), pat.equals("crystal") ? 0.3 : 0.55));
                g.setStroke(new BasicStroke(1.4f));
                g.draw(f);
            }
        });
    }

    // ================================================================== membrane (planar mapped over the whole wing)
    static CTex.T membrane(int base, int light, int dark, int vein, double alpha) {
        return pix(256, 256, (u, v, x, y) -> {
            double n = fbm(u * 7, v * 7, 0, 0, 4, 5);
            double dx = u - 0.6, dy = v - 0.27;
            double r = Math.hypot(dx, dy), th = Math.atan2(dy, dx);
            int c = ramp(Math.min(1, u * 0.8 + v * 0.35), dark, base, light);
            c = shade(c, 0.82 + 0.3 * n);
            double warp = fbm(u * 4 + 7, v * 4, 0, 0, 3, 8) * 2.5;
            double rad = Math.abs(Math.sin(th * 11 + warp + r * 3));
            double veinA = (1 - Capes.smooth(0.0, 0.07 + r * 0.05, rad)) * Capes.smooth(0.02, 0.1, r) * 0.55;
            double ring = Math.abs(Math.sin(r * 38 + warp * 1.3));
            veinA += (1 - Capes.smooth(0.0, 0.05, ring)) * 0.18 * Capes.smooth(0.05, 0.25, r);
            c = mix(c, vein, Math.min(0.7, veinA));
            // thinner (brighter, more translucent) far from the bones
            c = add(c, light, Math.pow(n, 3) * 0.25);
            return argb(c, alpha * (0.92 + 0.08 * n) - veinA * 0.0);
        });
    }

    // ================================================================== fairy wing lobe (root at the left center)
    static CTex.T fairyWing() {
        return img(256, 128, g -> {
            Path2D p = new Path2D.Double();
            p.moveTo(2, 64); p.curveTo(60, 0, 200, -10, 250, 40); p.curveTo(262, 70, 200, 112, 120, 110); p.curveTo(60, 108, 20, 90, 2, 64);
            g.setPaint(new LinearGradientPaint(0, 0, 256, 128, new float[]{0f, 0.35f, 0.7f, 1f},
                    new Color[]{col(0xFFFFFF, 0.55), col(0x9AF0FF, 0.45), col(0xE0A0FF, 0.45), col(0xFFB0E0, 0.55)}));
            g.fill(p);
            g.setClip(p);
            g.setColor(col(0xFFFFFF, 0.55));
            g.setStroke(new BasicStroke(1.6f));
            for (int i = 0; i < 6; i++) g.draw(new QuadCurve2D.Double(2, 64, 110, 30 + i * 14, 256, i * 26 - 10));
            Random r = new Random(4);
            for (int i = 0; i < 60; i++) {
                double x = r.nextDouble() * 256, y = r.nextDouble() * 128, s = 0.8 + r.nextDouble() * 2;
                g.setColor(col(0xFFFFFF, 0.5 + r.nextDouble() * 0.5));
                g.fill(new Ellipse2D.Double(x, y, s, s));
            }
            g.setClip(null);
            g.setColor(col(0xFFFFFF, 0.85));
            g.setStroke(new BasicStroke(2.2f));
            g.draw(p);
        });
    }

    // ================================================================== butterfly (root top-left; upper + lower lobe)
    static CTex.T butterfly() {
        return img(256, 320, g -> {
            Path2D up = new Path2D.Double();
            up.moveTo(6, 30); up.curveTo(90, -30, 250, -10, 246, 80); up.curveTo(242, 140, 150, 170, 8, 150); up.closePath();
            Path2D lo = new Path2D.Double();
            lo.moveTo(8, 150); lo.curveTo(120, 150, 210, 200, 190, 270); lo.curveTo(170, 330, 60, 320, 6, 230); lo.closePath();
            for (Path2D p : new Path2D[]{lo, up}) {
                g.setPaint(new RadialGradientPaint(20, 120, 250, new float[]{0f, 0.35f, 0.7f, 1f},
                        new Color[]{new Color(0x0A1A8A), new Color(0x1E7BFF), new Color(0x5AD8FF), new Color(0x2A2AA0)}));
                g.fill(p);
                g.setClip(p);
                g.setColor(col(0x050818, 0.8));
                g.setStroke(new BasicStroke(2.4f));
                for (int i = 0; i < 7; i++) g.draw(new QuadCurve2D.Double(8, p == up ? 40 : 160, 120, (p == up ? 20 : 170) + i * 22, 260, (p == up ? -20 : 180) + i * 34));
                g.setClip(null);
                g.setColor(new Color(0x07060F));
                g.setStroke(new BasicStroke(12f));
                g.draw(p);
            }
            g.setColor(Color.WHITE);
            double[][] dots = {{230, 40}, {236, 66}, {222, 96}, {196, 128}, {176, 258}, {156, 284}, {126, 298}, {206, 30}};
            for (double[] d : dots) g.fill(new Ellipse2D.Double(d[0] - 5, d[1] - 5, 10, 10));
            g.setPaint(new RadialGradientPaint(168, 72, 20, new float[]{0f, 0.5f, 1f}, new Color[]{new Color(0xFFF2A0), new Color(0xFFB030), new Color(0x5A2A00)}));
            g.fill(new Ellipse2D.Double(148, 52, 40, 40));
            g.setColor(new Color(0x07060F));
            g.fill(new Ellipse2D.Double(160, 64, 16, 16));
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(163, 66, 5, 5));
        });
    }

    static CTex.T mechBlade() {
        return img(64, 256, g -> {
            Path2D b = new Path2D.Double();
            b.moveTo(4, 0); b.lineTo(60, 0); b.lineTo(58, 200); b.lineTo(32, 254); b.lineTo(6, 200); b.closePath();
            g.setPaint(new LinearGradientPaint(0, 0, 64, 0, new float[]{0f, 0.45f, 0.55f, 1f}, new Color[]{new Color(0xE8ECF4), new Color(0x9AA2B4), new Color(0x5A6274), new Color(0xB8C0D0)}));
            g.fill(b);
            g.setClip(b);
            g.setColor(col(0x1A1E26, 0.8));
            g.setStroke(new BasicStroke(1.6f));
            for (int y = 40; y < 256; y += 46) g.drawLine(0, y, 64, y + 6);
            g.setColor(new Color(0x2AE6FF));
            g.setStroke(new BasicStroke(3f));
            g.drawLine(32, 10, 32, 236);
            g.setColor(col(0xFFFFFF, 0.9));
            g.setStroke(new BasicStroke(1f));
            g.drawLine(32, 10, 32, 236);
            g.setClip(null);
            g.setColor(new Color(0x1A1E26));
            g.setStroke(new BasicStroke(2f));
            g.draw(b);
        });
    }

    static int hueRgb(double h) { return Capes.hsv(h, 0.55, 1); }

    static int addc(int c, int rgb, double a) { return add(c, rgb, a); }
}
