package dev.lego.cosmetic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HD capes (512 x 256 = vanilla 64 x 32 cape layout scaled 8x), optionally animated.
 * <p>Layout (8x): outer face (8,8)-(88,136), inner face (96,8)-(176,136), side edges (0,8)-(8,136) and (88,8)-(96,136),
 * top (8,0)-(88,8), bottom (88,0)-(168,8); the elytra area (176,0)-(368,176) repeats the outer design.
 * <p>Animation: texture name "cape_&lt;id&gt;" is frame 0, "cape_&lt;id&gt;#k" frame k (0 &lt;= k &lt; {@link #frames}).
 * Every animation loops seamlessly (phase = 2*PI*k/frames).
 */
public final class Capes {
    private Capes() { }

    static final int W = 512, H = 256;
    /** Outer face size at 1x and the supersampled painting canvas (2x). */
    static final int FW1 = 80, FH1 = 128, S = 2, FW = FW1 * S, FH = FH1 * S;

    /** Painter of a face: canvas FW x FH, phase 0..2PI, frame k of n. */
    interface Painter { void paint(Graphics2D g, BufferedImage img, double ph, int k, int n); }

    static final class Spec {
        final String id;
        final int frames;
        final float fps;
        final int trimHi, trimLo, lining;
        final Painter p;
        Spec(String id, int frames, float fps, int trimHi, int trimLo, int lining, Painter p) {
            this.id = id; this.frames = frames; this.fps = fps; this.trimHi = trimHi; this.trimLo = trimLo; this.lining = lining; this.p = p;
        }
    }

    static final Map<String, Spec> SPECS = new LinkedHashMap<>();

    static void def(String id, int frames, float fps, int trimHi, int trimLo, int lining, Painter p) {
        SPECS.put(id, new Spec(id, Math.max(1, Math.min(16, frames)), frames > 1 ? fps : 0, trimHi, trimLo, lining, p));
    }

    static {
        CapeArt.defineAll();
        CapeArt2.defineAll();
        Cos3Capes.defineAll();
        XmasCapes.defineAll();
        CapePack.defineAll();
    }

    private static Spec spec(String capeTex) {
        if (capeTex == null) return null;
        String s = capeTex.startsWith("cape_") ? capeTex.substring(5) : capeTex;
        int h = s.indexOf('#');
        if (h >= 0) s = s.substring(0, h);
        return SPECS.get(s);
    }

    /** Number of animation frames of a cape texture (e.g. "cape_galaxy"); 1 = static. */
    public static int frames(String capeTex) {
        Spec s = spec(capeTex);
        return s == null ? 1 : s.frames;
    }

    /** Playback speed in frames per second (0 for static capes). */
    public static float fps(String capeTex) {
        Spec s = spec(capeTex);
        return s == null || s.frames <= 1 ? 0 : s.fps;
    }

    /** Texture name of the frame to show at the given time (seconds). */
    public static String frameName(String capeTex, float time) {
        int n = frames(capeTex);
        if (n <= 1) return capeTex;
        int k = Math.floorMod((int) Math.floor(time * fps(capeTex)), n);
        return k == 0 ? capeTex : capeTex + "#" + k;
    }

    // ================================================================== assembly
    /** @param d cape id with optional "#frame" suffix (without "cape_") */
    static CTex.T paint(String d) {
        int k = 0;
        int h = d.indexOf('#');
        if (h >= 0) {
            try { k = Integer.parseInt(d.substring(h + 1)); } catch (NumberFormatException ignored) { }
            d = d.substring(0, h);
        }
        Spec s = SPECS.get(d);
        if (s == null) s = SPECS.get("lego");
        int n = s.frames;
        k = Math.floorMod(k, n);
        double ph = Math.PI * 2 * k / n;

        // ---- outer face, painted supersampled
        BufferedImage f = new BufferedImage(FW, FH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D fg = f.createGraphics();
        dev.lego.ui.Gx.hints(fg);
        fg.setColor(new Color(s.lining));
        fg.fillRect(0, 0, FW, FH);
        try { s.p.paint(fg, f, ph, k, n); } catch (RuntimeException e) { /* keep what was painted */ }
        fg.dispose();
        cloth(f, 0.9);
        trim(f, s);
        BufferedImage front = down(f);
        grain(front, 31);

        // ---- inner lining
        BufferedImage li = new BufferedImage(FW, FH, BufferedImage.TYPE_INT_ARGB);
        lining(li, s);
        cloth(li, 0.7);
        trim(li, s);
        BufferedImage inner = down(li);
        grain(inner, 57);

        // ---- assemble
        BufferedImage out = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(new Color(s.lining));
        g.fillRect(0, 0, W, H);
        // elytra (uses the cape texture): outer design on both wing faces
        g.drawImage(front, 176, 0, 192, 176, null);
        g.drawImage(front, 192, 16, 80, 160, null);
        g.drawImage(front, 288, 16, 80, 160, null);
        g.drawImage(front, 8, 8, null);
        g.drawImage(inner, 96, 8, null);
        edge(g, 0, 8, 8, 128, s, true);
        edge(g, 88, 8, 8, 128, s, true);
        edge(g, 8, 0, 80, 8, s, false);
        edge(g, 88, 0, 80, 8, s, false);
        g.dispose();
        return new CTex.T(W, H, out.getRGB(0, 0, W, H, null, 0, W));
    }

    /** Side / top strips: the trim fabric (metallic gradient with a stitch line). */
    private static void edge(Graphics2D g, int x, int y, int w, int h, Spec s, boolean vertical) {
        if (vertical) g.setPaint(new GradientPaint(x, 0, new Color(s.trimHi), x + w, 0, new Color(s.trimLo)));
        else g.setPaint(new GradientPaint(0, y, new Color(s.trimHi), 0, y + h, new Color(s.trimLo)));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(255, 255, 255, 60));
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{3f, 2f}, 0));
        if (vertical) g.drawLine(x + w / 2, y + 2, x + w / 2, y + h - 2);
        else g.drawLine(x + 2, y + h / 2, x + w - 2, y + h / 2);
        g.setStroke(new BasicStroke(1f));
    }

    /** Default satin lining: soft vertical gradient, diagonal sheen, fine quilting. */
    private static void lining(BufferedImage img, Spec s) {
        int[] p = px(img);
        int base = s.lining;
        int hi = mix(base, 0xFFFFFF, 0.28), lo = mix(base, 0x000000, 0.4);
        for (int y = 0; y < FH; y++)
            for (int x = 0; x < FW; x++) {
                double u = (x + 0.5) / FW, v = (y + 0.5) / FH;
                int c = mix(hi, lo, v * 0.9);
                double sheen = Math.exp(-Math.pow((u * 0.8 + v * 0.6 - 0.55) / 0.12, 2)) * 0.2;
                c = add(c, 0xFFFFFF, sheen);
                // diamond quilting
                double qa = Math.abs(((u * 5 + v * 3.1) % 1 + 1) % 1 - 0.5), qb = Math.abs(((u * 5 - v * 3.1) % 1 + 1) % 1 - 0.5);
                double q = Math.min(qa, qb);
                if (q < 0.045) c = shade(c, 0.78 + q * 4.5);
                else c = shade(c, 1.04 - q * 0.2);
                p[y * FW + x] = 0xFF000000 | c;
            }
    }

    /** Fabric shading: soft vertical folds, top shadow (shoulders), edge vignette. */
    static void cloth(BufferedImage img, double amount) {
        int[] p = px(img);
        int w = img.getWidth(), h = img.getHeight();
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                double u = (x + 0.5) / w, v = (y + 0.5) / h;
                double fold = 0.055 * Math.sin(u * Math.PI * 2 * 2.5 + 0.6 + v * 1.2) * (0.35 + 0.65 * v) + 0.025 * Math.sin(u * Math.PI * 2 * 5.5 + 1.3);
                double ex = Math.min(u, 1 - u), ey = Math.min(v, 1 - v);
                double vig = 1 - 0.28 * Math.pow(1 - Math.min(1, ex / 0.22), 2) - 0.22 * Math.pow(1 - Math.min(1, (1 - v) / 0.25), 2);
                double top = 1 - 0.25 * Math.pow(1 - Math.min(1, v / 0.09), 2);
                double m = 1 + (fold + (vig * top - 1)) * amount;
                int i = y * w + x;
                p[i] = (p[i] & 0xFF000000) | (shade(p[i], m) & 0xFFFFFF);
            }
    }

    /** Clean metallic border trim with an inner pinstripe. */
    static void trim(BufferedImage img, Spec s) {
        Graphics2D g = img.createGraphics();
        dev.lego.ui.Gx.hints(g);
        int w = img.getWidth(), h = img.getHeight();
        float t = 7f;
        // soft inner shadow of the trim
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(0, 0, 0, 22 - i * 3));
            g.setStroke(new BasicStroke(2f));
            g.drawRect((int) t + i, (int) t + i, w - 1 - 2 * ((int) t + i), h - 1 - 2 * ((int) t + i));
        }
        Shape outer = new java.awt.geom.Rectangle2D.Float(0, 0, w, h);
        java.awt.geom.Area band = new java.awt.geom.Area(outer);
        band.subtract(new java.awt.geom.Area(new java.awt.geom.Rectangle2D.Float(t, t, w - 2 * t, h - 2 * t)));
        g.setPaint(new java.awt.LinearGradientPaint(0, 0, w * 0.6f, h, new float[]{0f, 0.3f, 0.55f, 0.8f, 1f},
                new Color[]{new Color(s.trimHi), new Color(mix(s.trimHi, s.trimLo, 0.55)), new Color(s.trimHi), new Color(s.trimLo), new Color(mix(s.trimHi, s.trimLo, 0.4))}));
        g.fill(band);
        g.setColor(new Color(255, 255, 255, 110));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new java.awt.geom.Rectangle2D.Float(1.5f, 1.5f, w - 3, h - 3));
        g.setColor(new Color(0, 0, 0, 120));
        g.draw(new java.awt.geom.Rectangle2D.Float(t - 0.6f, t - 0.6f, w - 2 * t + 1.2f, h - 2 * t + 1.2f));
        // pinstripe
        g.setColor(new Color((s.trimHi & 0xFFFFFF) | (95 << 24), true));
        g.setStroke(new BasicStroke(1.3f));
        g.draw(new java.awt.geom.Rectangle2D.Float(t + 4, t + 4, w - 2 * t - 8, h - 2 * t - 8));
        // top collar shadow
        g.setPaint(new GradientPaint(0, t, new Color(0, 0, 0, 70), 0, t + 16, new Color(0, 0, 0, 0)));
        g.fillRect((int) t, (int) t, w - 2 * (int) t, 16);
        g.dispose();
    }

    /** 2x2 box filter downsample. */
    static BufferedImage down(BufferedImage src) {
        int w = src.getWidth() / 2, h = src.getHeight() / 2, sw = src.getWidth();
        int[] a = px(src);
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] o = px(out);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                int i = y * 2 * sw + x * 2;
                int c0 = a[i], c1 = a[i + 1], c2 = a[i + sw], c3 = a[i + sw + 1];
                int r = 0, gg = 0, b = 0, al = 0;
                for (int c : new int[]{c0, c1, c2, c3}) { al += c >>> 24; r += (c >> 16) & 255; gg += (c >> 8) & 255; b += c & 255; }
                o[y * w + x] = ((al / 4) << 24) | ((r / 4) << 16) | ((gg / 4) << 8) | (b / 4);
            }
        return out;
    }

    /** Very fine woven grain at 1x. */
    static void grain(BufferedImage img, int seed) {
        int[] p = px(img);
        int w = img.getWidth(), h = img.getHeight();
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                double n = (hash(x, y, seed) - 0.5) * 0.05 + (((x + y) % 3 == 0) ? -0.015 : 0.006);
                int i = y * w + x;
                p[i] = (p[i] & 0xFF000000) | (shade(p[i], 1 + n) & 0xFFFFFF);
            }
    }

    // ================================================================== toolkit for painters
    static int[] px(BufferedImage im) { return ((DataBufferInt) im.getRaster().getDataBuffer()).getData(); }

    interface PF { int at(double u, double v, int x, int y); }

    /** Per-pixel fill of the whole canvas (opaque result). */
    static void field(BufferedImage im, PF f) {
        int w = im.getWidth(), h = im.getHeight();
        int[] p = px(im);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) p[y * w + x] = 0xFF000000 | f.at((x + 0.5) / w, (y + 0.5) / h, x, y);
    }

    static double hash(int x, int y, int s) {
        int h = x * 374761393 + y * 668265263 + s * 1442695041;
        h = (h ^ (h >>> 13)) * 1274126177;
        h ^= h >>> 16;
        return (h & 0xFFFFFF) / 16777216.0;
    }

    static double hash1(int i, int s) { return hash(i, i * 31 + 7, s); }

    /** Value noise with quintic interpolation, periodic with px / py lattice cells (0 = not periodic). */
    static double vn(double x, double y, int px, int py, int s) {
        int xi = (int) Math.floor(x), yi = (int) Math.floor(y);
        double fx = x - xi, fy = y - yi;
        fx = fx * fx * fx * (fx * (fx * 6 - 15) + 10);
        fy = fy * fy * fy * (fy * (fy * 6 - 15) + 10);
        int x0 = px > 0 ? Math.floorMod(xi, px) : xi, x1 = px > 0 ? Math.floorMod(xi + 1, px) : xi + 1;
        int y0 = py > 0 ? Math.floorMod(yi, py) : yi, y1 = py > 0 ? Math.floorMod(yi + 1, py) : yi + 1;
        double a = hash(x0, y0, s), b = hash(x1, y0, s), c = hash(x0, y1, s), d = hash(x1, y1, s);
        return a + (b - a) * fx + (c - a) * fy + (a - b - c + d) * fx * fy;
    }

    /** Fractal noise 0..1, periodic like {@link #vn} (periods double per octave). */
    static double fbm(double x, double y, int px, int py, int oct, int s) {
        double sum = 0, amp = 0.5, norm = 0, f = 1;
        for (int o = 0; o < oct; o++) {
            sum += amp * vn(x * f, y * f, px * (int) f, py * (int) f, s + o * 17);
            norm += amp;
            amp *= 0.5;
            f *= 2;
        }
        return sum / norm;
    }

    static double clamp01(double v) { return v < 0 ? 0 : (v > 1 ? 1 : v); }
    static double smooth(double a, double b, double v) { double t = clamp01((v - a) / (b - a)); return t * t * (3 - 2 * t); }

    static int mix(int a, int b, double t) {
        t = clamp01(t);
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255, br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return ((int) (ar + (br - ar) * t) << 16) | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }

    /** Evenly spaced colour ramp. */
    static int ramp(double t, int... c) {
        t = clamp01(t) * (c.length - 1);
        int i = Math.min(c.length - 2, (int) t);
        return mix(c[i], c[i + 1], t - i);
    }

    static int shade(int c, double m) {
        int r = (int) Math.min(255, Math.max(0, ((c >> 16) & 255) * m)), g = (int) Math.min(255, Math.max(0, ((c >> 8) & 255) * m)), b = (int) Math.min(255, Math.max(0, (c & 255) * m));
        return (c & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    /** Additive blend of rgb scaled by a (a may exceed 1). */
    static int add(int c, int rgb, double a) {
        if (a <= 0) return c;
        int r = (int) Math.min(255, ((c >> 16) & 255) + ((rgb >> 16) & 255) * a), g = (int) Math.min(255, ((c >> 8) & 255) + ((rgb >> 8) & 255) * a), b = (int) Math.min(255, (c & 255) + (rgb & 255) * a);
        return (c & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    /** Screen-like "over" of rgb with alpha a. */
    static int over(int c, int rgb, double a) { return (c & 0xFF000000) | mix(c, rgb, a); }

    static int hsv(double h, double s, double v) { return Color.HSBtoRGB((float) (h - Math.floor(h)), (float) clamp01(s), (float) clamp01(v)) & 0xFFFFFF; }

    static Color col(int rgb, double a) { return new Color((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, (int) Math.round(clamp01(a) * 255)); }

    /** Soft radial glow dot. */
    static void dot(Graphics2D g, double x, double y, double r, int rgb, double a) {
        if (r <= 0.05 || a <= 0.004) return;
        g.setPaint(new RadialGradientPaint((float) x, (float) y, (float) r, new float[]{0f, 0.25f, 1f}, new Color[]{col(rgb, a), col(rgb, a * 0.45), col(rgb, 0)}));
        g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
    }

    /** Twinkling star: soft core + 4-point glint. */
    static void star(Graphics2D g, double x, double y, double r, int rgb, double a) {
        if (a <= 0.01) return;
        dot(g, x, y, r * 2.2, rgb, a * 0.55);
        dot(g, x, y, r * 0.9, 0xFFFFFF, a);
        if (r > 1.6) {
            g.setStroke(new BasicStroke((float) Math.max(0.7, r * 0.22), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setPaint(new GradientPaint((float) (x - r * 3.2), (float) y, col(rgb, 0), (float) x, (float) y, col(0xFFFFFF, a * 0.9), true));
            g.draw(new Line2D.Double(x - r * 3.2, y, x + r * 3.2, y));
            g.setPaint(new GradientPaint((float) x, (float) (y - r * 3.2), col(rgb, 0), (float) x, (float) y, col(0xFFFFFF, a * 0.9), true));
            g.draw(new Line2D.Double(x, y - r * 3.2, x, y + r * 3.2));
        }
    }

    /** Glowing stroke of a shape (several widening translucent passes, then a bright core). */
    static void glowStroke(Graphics2D g, Shape s, int rgb, float core, float spread, double a) {
        for (int i = 5; i >= 1; i--) {
            g.setColor(col(rgb, a * 0.12 * (6 - i) / 5.0 + 0.02));
            g.setStroke(new BasicStroke(core + spread * i, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(s);
        }
        g.setColor(col(rgb, a));
        g.setStroke(new BasicStroke(core, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(s);
        g.setColor(col(mix(rgb, 0xFFFFFF, 0.7), a));
        g.setStroke(new BasicStroke(Math.max(0.6f, core * 0.4f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(s);
    }

    /** Diagonal glossy shine band crossing the face once per loop (off-screen at k = 0: seamless). */
    static void sweep(BufferedImage img, double t, double width, double strength) {
        int[] p = px(img);
        int w = img.getWidth(), h = img.getHeight();
        double pos = -0.5 + t * 2.2;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                double u = (x + 0.5) / w, v = (y + 0.5) / h;
                double d = (u * 0.7 + v * 0.45) - pos;
                double a = Math.exp(-(d * d) / (width * width)) * strength;
                if (a > 0.004) p[y * w + x] = add(p[y * w + x], 0xFFFFFF, a);
            }
    }

    /** Additive soft glow dot straight into the pixel array (gaussian falloff). */
    static void addDot(BufferedImage img, double cx, double cy, double r, int rgb, double a) {
        if (r <= 0.1 || a <= 0.003) return;
        int[] p = px(img);
        int w = img.getWidth(), h = img.getHeight();
        int x0 = Math.max(0, (int) (cx - r * 2)), x1 = Math.min(w - 1, (int) (cx + r * 2)), y0 = Math.max(0, (int) (cy - r * 2)), y1 = Math.min(h - 1, (int) (cy + r * 2));
        double k = 1.0 / (r * r * 0.5);
        for (int y = y0; y <= y1; y++)
            for (int x = x0; x <= x1; x++) {
                double dx = x + 0.5 - cx, dy = y + 0.5 - cy;
                double f = Math.exp(-(dx * dx + dy * dy) * k) * a;
                if (f > 0.003) p[y * w + x] = add(p[y * w + x], rgb, f);
            }
    }
}
