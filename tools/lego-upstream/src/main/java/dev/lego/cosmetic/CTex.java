package dev.lego.cosmetic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/** Procedurally painted high resolution cosmetic textures (Java2D, thread-safe, cached). */
public final class CTex {
    private CTex() { }

    public static final class T {
        public final int w, h;
        public final int[] argb;
        T(int w, int h, int[] argb) { this.w = w; this.h = h; this.argb = argb; }
        public int sample(float u, float v) {
            int x = Math.max(0, Math.min(w - 1, (int) (u * w))), y = Math.max(0, Math.min(h - 1, (int) (v * h)));
            return argb[y * w + x];
        }
    }

    private static final Map<String, T> CACHE = new HashMap<>();
    /** Animated cape frames are big (512 KB each): kept in a small LRU instead of the permanent cache. */
    private static final int FRAME_CACHE = 96;
    private static final java.util.LinkedHashMap<String, T> FRAMES = new java.util.LinkedHashMap<String, T>(64, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, T> e) { return size() > FRAME_CACHE; }
    };

    private static final Object MAPS = new Object(), PAINTING = new Object();

    /** Normalised cache key: frame 0 of an animated cape is the plain name. */
    private static String key(String name) {
        if (name.startsWith("cape_") && name.indexOf('#') > 0) {
            int h = name.indexOf('#');
            String base = name.substring(0, h);
            int n = Capes.frames(base), k;
            try { k = Math.floorMod(Integer.parseInt(name.substring(h + 1)), Math.max(1, n)); } catch (NumberFormatException e) { k = 0; }
            return k == 0 || n <= 1 ? base : base + "#" + k;
        }
        return name;
    }

    private static T lookup(String key) {
        synchronized (MAPS) { return key.indexOf('#') > 0 && key.startsWith("cape_") ? FRAMES.get(key) : CACHE.get(key); }
    }

    /** Cached texture or null (never paints and never waits for a painter - safe for the render thread). */
    public static T peek(String name) { return lookup(key(name)); }

    /** Texture by name, painted on first use (call from background threads when possible). */
    public static T get(String name) {
        String key = key(name);
        T t = lookup(key);
        if (t != null) return t;
        synchronized (PAINTING) {           // painters share Java2D state -> one at a time
            t = lookup(key);
            if (t != null) return t;
            t = paint(key);
            synchronized (MAPS) {
                if (key.indexOf('#') > 0 && key.startsWith("cape_")) FRAMES.put(key, t); else CACHE.put(key, t);
            }
            return t;
        }
    }

    private static T make(int w, int h, Consumer<Graphics2D> p) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        dev.lego.ui.Gx.hints(g);
        p.accept(g);
        g.dispose();
        int[] px = img.getRGB(0, 0, w, h, null, 0, w);
        bleed(px, w, h);
        return new T(w, h, px);
    }

    /** Copies colors into fully transparent neighbours so bilinear filtering has no dark fringes. */
    private static void bleed(int[] px, int w, int h) {
        for (int pass = 0; pass < 3; pass++) {
            int[] src = px.clone();
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++) {
                    int i = y * w + x;
                    if ((src[i] >>> 24) != 0) continue;
                    int r = 0, g = 0, b = 0, n = 0;
                    for (int dy = -1; dy <= 1; dy++)
                        for (int dx = -1; dx <= 1; dx++) {
                            int xx = x + dx, yy = y + dy;
                            if (xx < 0 || yy < 0 || xx >= w || yy >= h) continue;
                            int c = src[yy * w + xx];
                            if ((c >>> 24) == 0 || (c & 0xFFFFFF) == 0 && (c >>> 24) == 0) continue;
                            if ((c >>> 24) < 8) continue;
                            r += (c >> 16) & 255; g += (c >> 8) & 255; b += c & 255; n++;
                        }
                    if (n > 0) px[i] = ((r / n) << 16) | ((g / n) << 8) | (b / n);
                }
        }
    }

    static Color c(int rgb) { return new Color(rgb); }
    static Color ca(int rgb, int a) { return new Color((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, a); }

    /** Texture from a finished image (with edge bleeding) - used by other painters (e.g. PetTex). */
    static T of(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        int[] px = img.getRGB(0, 0, w, h, null, 0, w);
        bleed(px, w, h);
        return new T(w, h, px);
    }

    private static T paint(String name) {
        if (name.startsWith("pet")) { T pt = PetTex.paint(name); if (pt != null) return pt; }
        if (name.startsWith("veh")) { T vt = Vehicles.tex(name); if (vt != null) return vt; }
        switch (name) {
            case "white": return make(16, 16, g -> { g.setColor(Color.WHITE); g.fillRect(0, 0, 16, 16); });
            // ------------------------------------------------------------ wings
            case "wing_angel": return feathers(0xFFFFFF, 0xDCE3EF, 0x9FAEC6, 0xF6D57A);
            case "wing_gold": return feathers(0xFFF1A8, 0xF2C230, 0xB9811A, 0xFFFFFF);
            case "wing_phoenix": return feathers(0xFFF3B0, 0xFF8A1E, 0xD01F1F, 0xFFE27A);
            case "wing_shadow": return feathers(0x6B6F80, 0x2E3140, 0x0E0F16, 0x9B6BFF);
            case "wing_demon": return membrane(0x3A0B12, 0x8E1B24, 0x1A0508, 0xFF4B3A, false);
            case "wing_icedragon": return membrane(0x0F3B66, 0x39C4FF, 0x0A1E3A, 0xCFF6FF, true);
            case "wing_neon": return membrane(0x12061F, 0x2C0E4A, 0x07020E, 0xD35BFF, true);
            case "wing_butterfly": return butterfly();
            // ------------------------------------------------------------ hats & head
            case "gold": return metal(0xFFF3B0, 0xE8B92C, 0x9A6A12);
            case "silver": return metal(0xFFFFFF, 0xC9D0DA, 0x7A8494);
            case "gems": return gems();
            case "halo": return make(256, 32, g -> {
                g.setPaint(new GradientPaint(0, 0, c(0xFFF7C8), 0, 32, c(0xFFC53D)));
                g.fillRect(0, 0, 256, 32);
                g.setColor(ca(0xFFFFFF, 150));
                g.fillRect(0, 6, 256, 5);
            });
            case "felt_black": return felt(0x16161B, 0x22232B, 7);
            case "felt_red": return felt(0xC4161C, 0xE0262B, 11);
            case "felt_purple": return felt(0x2A1242, 0x3E1D5E, 13);
            case "fur_white": return fur();
            case "band_red": return make(64, 16, g -> { g.setPaint(new GradientPaint(0, 0, c(0xE33A3A), 0, 16, c(0x8E1414))); g.fillRect(0, 0, 64, 16); });
            case "band_green": return make(64, 16, g -> { g.setPaint(new GradientPaint(0, 0, c(0x2BE07A), 0, 16, c(0x13843F))); g.fillRect(0, 0, 64, 16); });
            case "cat_ear": return catEar();
            case "bunny_ear": return bunnyEar();
            case "horn": return make(64, 128, g -> {
                g.setPaint(new LinearGradientPaint(0, 0, 0, 128, new float[]{0f, 0.55f, 1f}, new Color[]{c(0x1C0406), c(0x8A1414), c(0xE8403A)}));
                g.fillRect(0, 0, 64, 128);
                g.setColor(ca(0x000000, 60));
                for (int i = 0; i < 128; i += 9) g.fillRect(0, i, 64, 3);
                g.setPaint(new GradientPaint(0, 0, ca(0xFFFFFF, 70), 64, 0, ca(0xFFFFFF, 0)));
                g.fillRect(0, 0, 22, 128);
            });
            case "headphone": return make(64, 64, g -> {
                g.setPaint(new GradientPaint(0, 0, c(0x2C2E36), 0, 64, c(0x121318)));
                g.fillRect(0, 0, 64, 64);
                g.setColor(ca(0xFFFFFF, 22));
                for (int i = 0; i < 64; i += 4) g.drawLine(0, i, 64, i);
            });
            case "cushion": return make(64, 64, g -> {
                g.setPaint(new RadialGradientPaint(32, 32, 34, new float[]{0f, 1f}, new Color[]{c(0x3A3D48), c(0x16171C)}));
                g.fillRect(0, 0, 64, 64);
                g.setColor(c(0x1ED760));
                g.setStroke(new BasicStroke(5));
                g.drawOval(9, 9, 46, 46);
            });
            case "sunglasses": return sunglasses();
            case "pixelglasses": return pixelGlasses();
            // ------------------------------------------------------------ back
            case "lego_red": return legoPlastic(0xE3000B);
            case "lego_yellow": return legoPlastic(0xFFCD03);
            case "lego_blue": return legoPlastic(0x006CB7);
            case "lego_green": return legoPlastic(0x00852B);
            case "sword": return sword();
            case "strap": return make(32, 32, g -> { g.setPaint(new GradientPaint(0, 0, c(0x4A3222), 32, 0, c(0x2A1A10))); g.fillRect(0, 0, 32, 32); });
            // ------------------------------------------------------------ sprites
            case "flame": return flame();
            case "snowflake": return snowflake();
            case "heart": return heartSprite();
            case "orb": return orb(0xFFFFFF);
            case "orb_purple": return orb(0xC77DFF);
            case "orb_cyan": return orb(0x7CF7FF);
            case "spark": return spark();
            case "bolt": return boltSprite();
            case "mannequin_head": case "mannequin": return Tex2.pix(64, 64, (u, v, x, y) -> {
                // matte vinyl: very fine grain + faint soft mottling (tinted per body part)
                double n = Capes.fbm(u * 6, v * 6, 6, 6, 3, name.length() * 17 + 5);
                return 0xFF000000 | Capes.shade(0xFFFFFF, 0.93 + 0.05 * n + 0.025 * (Capes.hash(x, y, 77) - 0.5));
            });
            case "mannequin_shadow": return Tex2.pix(64, 64, (u, v, x, y) -> {
                double d = Math.hypot((u - 0.5) * 2, (v - 0.5) * 2.4);
                double a = Math.pow(Math.max(0, 1 - d), 1.6);
                return Tex2.argb(0x000000, a);
            });
            default:
                if (name.startsWith("cape_")) return Capes.paint(name.substring(5));
                if (name.startsWith("c3_") || name.equals("@skin")) { T x = Cos3Tex.paint(name); if (x != null) return x; }
                if (name.startsWith("xm_")) { T x = XmasTex.paint(name); if (x != null) return x; }
                if (name.startsWith("hd_")) { T x = HdTex.paint(name); if (x != null) return x; }
                { T x = Tex2.paint(name); if (x != null) return x; }
                return make(16, 16, g -> { g.setColor(Color.MAGENTA); g.fillRect(0, 0, 16, 16); });
        }
    }

    // ================================================================== painters
    /** Feathered wing: root at the top-left corner, primaries fan out to the right and down. 256 x 320. */
    private static T feathers(int light, int mid, int dark, int tip) {
        int W = 256, H = 320;
        return make(W, H, g -> {
            Random r = new Random(7);
            // primaries: fan out from the "hand" at the top right
            for (int i = 0; i < 9; i++) {
                double t = i / 8.0;
                double bx = 214 - t * 72, by = 30 + t * 8;
                double ang = Math.toRadians(76 + t * 26 + r.nextGaussian() * 1.2);
                double len = 280 - t * 70;
                feather(g, bx, by, ang, len, 34, light, mid, dark, tip, 0.6);
            }
            // secondaries: along the arm, pointing down
            for (int i = 0; i < 11; i++) {
                double t = i / 10.0;
                double bx = 146 - t * 132, by = 36 - t * 10;
                double ang = Math.toRadians(100 + t * 12 + r.nextGaussian() * 1.2);
                double len = 205 - t * 85;
                feather(g, bx, by, ang, len, 32, light, mid, dark, tip, 0.35);
            }
            // coverts: two short rows on top
            for (int row = 1; row >= 0; row--) {
                for (int i = 0; i < 12; i++) {
                    double t = i / 11.0;
                    double bx = 222 - t * 212, by = 24 + row * 18 - t * 6;
                    double ang = Math.toRadians(80 + t * 22);
                    double len = row == 0 ? 52 + (1 - t) * 20 : 82 + (1 - t) * 26;
                    feather(g, bx, by, ang, len, 30, light, mid, dark, tip, 0.1);
                }
            }
            // arm / leading edge
            g.setPaint(new GradientPaint(0, 0, c(light), W, 40, c(mid)));
            Path2D arm = new Path2D.Double();
            arm.moveTo(0, 2); arm.curveTo(70, -4, 170, 4, 252, 22); arm.lineTo(246, 44); arm.curveTo(170, 30, 80, 30, 0, 36); arm.closePath();
            g.fill(arm);
            g.setColor(ca(dark, 90));
            g.setStroke(new BasicStroke(2f));
            g.draw(arm);
            g.setColor(ca(0xFFFFFF, 120));
            g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new java.awt.geom.CubicCurve2D.Double(4, 8, 70, 3, 170, 10, 246, 26));
        });
    }

    private static void feather(Graphics2D g, double x, double y, double ang, double len, double wid, int light, int mid, int dark, int tip, double tipAmt) {
        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.rotate(ang - Math.PI / 2);
        GeneralPath f = new GeneralPath();
        f.moveTo(0, 0);
        f.curveTo(wid * 0.7, len * 0.15, wid * 0.62, len * 0.8, 0, len);
        f.curveTo(-wid * 0.45, len * 0.82, -wid * 0.52, len * 0.2, 0, 0);
        f.closePath();
        g.setPaint(new LinearGradientPaint(0, 0, 0, (float) len, new float[]{0f, 0.6f, 1f},
                new Color[]{c(light), c(mid), Art2.mix(c(mid), c(tip), tipAmt)}));
        g.fill(f);
        g.setPaint(new GradientPaint((float) -wid / 2, 0, ca(0x000000, 0), (float) wid / 2, 0, ca(dark, 120)));
        g.fill(f);
        g.setColor(ca(dark, 150));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(f);
        g.setColor(ca(0xFFFFFF, 150));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new java.awt.geom.QuadCurve2D.Double(0, 2, wid * 0.12, len * 0.5, 0, len * 0.94));
        // barbs
        g.setColor(ca(dark, 55));
        g.setStroke(new BasicStroke(0.8f));
        for (double s = 0.12; s < 0.9; s += 0.07) g.drawLine(0, (int) (len * s), (int) (wid * 0.42), (int) (len * s + wid * 0.35));
        g.setTransform(old);
    }

    /** Bat / dragon membrane: bones from the root, scalloped edge. 256 x 320. */
    private static T membrane(int base, int light, int dark, int edge, boolean glowEdge) {
        int W = 256, H = 320;
        return make(W, H, g -> {
            double[][] tips = {{250, 30}, {240, 150}, {190, 260}, {110, 312}, {30, 250}};
            Path2D m = new Path2D.Double();
            m.moveTo(4, 8);
            m.lineTo(tips[0][0], tips[0][1]);
            for (int i = 1; i < tips.length; i++) {
                double px = tips[i - 1][0], py = tips[i - 1][1], nx = tips[i][0], ny = tips[i][1];
                double mx = (px + nx) / 2, my = (py + ny) / 2;
                double ix = mx - (mx - 20) * 0.22, iy = my - (my - 10) * 0.22;
                m.quadTo(ix, iy, nx, ny);
            }
            m.quadTo(10, 140, 4, 40);
            m.closePath();
            g.setPaint(new RadialGradientPaint(10, 10, 330, new float[]{0f, 0.55f, 1f}, new Color[]{c(dark), c(base), c(light)}));
            g.fill(m);
            // veins
            g.setClip(m);
            Random r = new Random(3);
            g.setColor(ca(light, 70));
            for (int i = 0; i < 40; i++) {
                double[] t = tips[r.nextInt(tips.length)];
                double s = 0.3 + r.nextDouble() * 0.6;
                g.setStroke(new BasicStroke(0.8f));
                g.draw(new java.awt.geom.QuadCurve2D.Double(10 + (t[0] - 10) * s * 0.5, 10 + (t[1] - 10) * s * 0.5, 10 + (t[0] - 10) * s + r.nextGaussian() * 16, 10 + (t[1] - 10) * s + r.nextGaussian() * 16,
                        10 + (t[0] - 10) * s * 1.2, 10 + (t[1] - 10) * s * 1.2));
            }
            g.setClip(null);
            // edge glow
            if (glowEdge) {
                for (int i = 6; i >= 1; i--) {
                    g.setColor(ca(edge, 20 + (6 - i) * 25));
                    g.setStroke(new BasicStroke(i * 1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.draw(m);
                }
            } else {
                g.setColor(ca(edge, 150));
                g.setStroke(new BasicStroke(2f));
                g.draw(m);
            }
            // bones
            for (double[] t : tips) {
                g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(c(dark));
                g.draw(new java.awt.geom.QuadCurve2D.Double(6, 10, (t[0] + 6) / 2 + 10, (t[1] + 10) / 2 - 12, t[0], t[1]));
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(glowEdge ? c(edge) : Art2.mix(c(light), c(edge), 0.3));
                g.draw(new java.awt.geom.QuadCurve2D.Double(6, 10, (t[0] + 6) / 2 + 10, (t[1] + 10) / 2 - 12, t[0], t[1]));
                g.setColor(c(dark));
                g.fill(new Ellipse2D.Double(t[0] - 4, t[1] - 4, 8, 8));
            }
            // claw at the wrist
            g.setColor(c(edge));
            Path2D claw = new Path2D.Double();
            claw.moveTo(246, 26); claw.lineTo(256, 8); claw.lineTo(238, 22); claw.closePath();
            g.fill(claw);
        });
    }

    private static T butterfly() {
        int W = 256, H = 320;
        return make(W, H, g -> {
            // upper lobe
            Path2D up = new Path2D.Double();
            up.moveTo(6, 30); up.curveTo(90, -30, 250, -10, 246, 80); up.curveTo(242, 140, 150, 170, 8, 150); up.closePath();
            Path2D lo = new Path2D.Double();
            lo.moveTo(8, 150); lo.curveTo(120, 150, 210, 200, 190, 270); lo.curveTo(170, 330, 60, 320, 6, 230); lo.closePath();
            for (Path2D p : new Path2D[]{lo, up}) {
                g.setPaint(new RadialGradientPaint(20, 120, 260, new float[]{0f, 0.45f, 0.8f, 1f},
                        new Color[]{c(0x1B2BFF), c(0x3FA9FF), c(0x9B4DFF), c(0x14081F)}));
                g.fill(p);
                g.setColor(c(0x0B0612));
                g.setStroke(new BasicStroke(10f));
                g.draw(p);
            }
            g.setClip(up);
            g.setColor(ca(0x000000, 120));
            g.setStroke(new BasicStroke(2.2f));
            for (int i = 0; i < 6; i++) g.drawLine(8, 40 + i * 4, 250, -10 + i * 36);
            g.setClip(null);
            g.setColor(Color.WHITE);
            double[][] dots = {{225, 40}, {232, 70}, {214, 100}, {190, 130}, {170, 262}, {150, 285}, {120, 298}};
            for (double[] d : dots) g.fill(new Ellipse2D.Double(d[0] - 5, d[1] - 5, 10, 10));
            g.setColor(ca(0xFFE14D, 230));
            g.fill(new Ellipse2D.Double(150, 55, 34, 34));
            g.setColor(c(0x0B0612));
            g.fill(new Ellipse2D.Double(158, 63, 18, 18));
        });
    }

    private static T metal(int hi, int mid, int lo) {
        return make(128, 128, g -> {
            g.setPaint(new LinearGradientPaint(0, 0, 0, 128, new float[]{0f, 0.35f, 0.55f, 1f}, new Color[]{c(hi), c(mid), c(lo), c(mid)}));
            g.fillRect(0, 0, 128, 128);
            Random r = new Random(5);
            for (int i = 0; i < 400; i++) {
                g.setColor(ca(r.nextBoolean() ? 0xFFFFFF : 0x000000, 14));
                g.fillRect(r.nextInt(128), r.nextInt(128), 1 + r.nextInt(8), 1);
            }
        });
    }

    private static T gems() {
        return make(128, 32, g -> {
            int[] cols = {0xE0115F, 0x1E90FF, 0x2ECC71, 0x9B59B6};
            for (int i = 0; i < 4; i++) {
                g.setPaint(new RadialGradientPaint(i * 32 + 12, 10, 22, new float[]{0f, 0.4f, 1f}, new Color[]{Color.WHITE, c(cols[i]), Art2.mix(c(cols[i]), Color.BLACK, 0.5)}));
                g.fillRect(i * 32, 0, 32, 32);
            }
        });
    }

    private static T felt(int a, int b, long seed) {
        return make(64, 64, g -> {
            g.setPaint(new GradientPaint(0, 0, c(b), 0, 64, c(a)));
            g.fillRect(0, 0, 64, 64);
            Random r = new Random(seed);
            for (int i = 0; i < 700; i++) {
                g.setColor(ca(r.nextBoolean() ? 0xFFFFFF : 0x000000, 10 + r.nextInt(14)));
                g.fillRect(r.nextInt(64), r.nextInt(64), 1, 1);
            }
        });
    }

    private static T fur() {
        return make(64, 64, g -> {
            g.setColor(c(0xF4F4F6));
            g.fillRect(0, 0, 64, 64);
            Random r = new Random(9);
            for (int i = 0; i < 300; i++) {
                int x = r.nextInt(64), y = r.nextInt(64);
                g.setColor(ca(r.nextBoolean() ? 0xFFFFFF : 0xC9CCD6, 120));
                g.fill(new Ellipse2D.Double(x - 3, y - 3, 6, 6));
            }
        });
    }

    private static T catEar() {
        return make(64, 64, g -> {
            Path2D o = new Path2D.Double();
            o.moveTo(2, 62); o.quadTo(8, 20, 30, 2); o.quadTo(56, 22, 62, 62); o.closePath();
            g.setColor(c(0x2B2D35));
            g.fill(o);
            Path2D in = new Path2D.Double();
            in.moveTo(14, 60); in.quadTo(18, 30, 31, 16); in.quadTo(46, 32, 50, 60); in.closePath();
            g.setPaint(new GradientPaint(0, 16, c(0xFFB3C7), 0, 60, c(0xFF7AA2)));
            g.fill(in);
        });
    }

    private static T bunnyEar() {
        return make(64, 160, g -> {
            RoundRectangle2D o = new RoundRectangle2D.Double(4, 2, 56, 156, 56, 90);
            g.setColor(c(0xF4F4F6));
            g.fill(o);
            RoundRectangle2D in = new RoundRectangle2D.Double(18, 18, 28, 128, 28, 60);
            g.setPaint(new GradientPaint(0, 18, c(0xFFC2D4), 0, 146, c(0xFF8FB1)));
            g.fill(in);
        });
    }

    private static T sunglasses() {
        return make(256, 64, g -> {
            g.setColor(c(0x0B0B0F));
            RoundRectangle2D l = new RoundRectangle2D.Double(10, 10, 104, 46, 22, 30), r = new RoundRectangle2D.Double(142, 10, 104, 46, 22, 30);
            g.fill(l);
            g.fill(r);
            g.fillRect(100, 14, 56, 8);
            for (RoundRectangle2D lens : new RoundRectangle2D[]{l, r}) {
                g.setPaint(new LinearGradientPaint((float) lens.getX(), 12, (float) lens.getX() + 100, 56, new float[]{0f, 0.5f, 1f},
                        new Color[]{c(0x3A1C5E), c(0x14142A), c(0x0A3A4A)}));
                g.fill(new RoundRectangle2D.Double(lens.getX() + 5, lens.getY() + 5, lens.getWidth() - 10, lens.getHeight() - 10, 16, 24));
                g.setColor(ca(0xFFFFFF, 110));
                g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine((int) lens.getX() + 18, 22, (int) lens.getX() + 40, 18);
            }
        });
    }

    private static T pixelGlasses() {
        return make(64, 16, g -> {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 64, 4);
            g.fillRect(4, 4, 24, 8);
            g.fillRect(36, 4, 24, 8);
            g.fillRect(8, 12, 16, 4);
            g.fillRect(40, 12, 16, 4);
            g.setColor(Color.WHITE);
            g.fillRect(8, 4, 4, 4);
            g.fillRect(12, 8, 4, 4);
            g.fillRect(40, 4, 4, 4);
            g.fillRect(44, 8, 4, 4);
        });
    }

    private static T legoPlastic(int rgb) {
        return make(64, 64, g -> {
            Color base = c(rgb);
            g.setPaint(new GradientPaint(0, 0, Art2.mix(base, Color.WHITE, 0.18), 64, 64, Art2.mix(base, Color.BLACK, 0.12)));
            g.fillRect(0, 0, 64, 64);
            g.setColor(Art2.mix(base, Color.BLACK, 0.35));
            g.setStroke(new BasicStroke(2f));
            g.drawRect(1, 1, 62, 62);
            g.setColor(ca(0xFFFFFF, 60));
            g.drawLine(3, 3, 60, 3);
        });
    }

    private static T sword() {
        return make(128, 128, g -> {
            AffineTransform o = g.getTransform();
            g.translate(64, 64);
            g.rotate(Math.toRadians(-45));
            // blade
            Path2D b = new Path2D.Double();
            b.moveTo(-7, -18); b.lineTo(-7, -76); b.lineTo(0, -88); b.lineTo(7, -76); b.lineTo(7, -18); b.closePath();
            g.setPaint(new GradientPaint(-7, 0, c(0x7FF3FF), 7, 0, c(0x1F9BC4)));
            g.fill(b);
            g.setColor(ca(0xFFFFFF, 190));
            g.fillRect(-2, -80, 3, 60);
            // guard + grip
            g.setColor(c(0x4B2E83));
            g.fill(new RoundRectangle2D.Double(-22, -20, 44, 9, 6, 6));
            g.setColor(c(0x3A2A1A));
            g.fillRect(-4, -12, 8, 30);
            g.setColor(c(0xE0B34A));
            g.fill(new Ellipse2D.Double(-6, 16, 12, 12));
            g.setTransform(o);
        });
    }

    private static T flame() {
        return make(64, 96, g -> {
            Path2D f = new Path2D.Double();
            f.moveTo(32, 2); f.curveTo(46, 30, 62, 50, 56, 72); f.curveTo(50, 92, 14, 92, 8, 72); f.curveTo(2, 52, 20, 36, 32, 2); f.closePath();
            g.setPaint(new LinearGradientPaint(0, 0, 0, 96, new float[]{0f, 0.45f, 1f}, new Color[]{ca(0xFF3A1A, 0), c(0xFF7A1A), c(0xFFE36B)}));
            g.fill(f);
            g.setPaint(new RadialGradientPaint(32, 72, 22, new float[]{0f, 1f}, new Color[]{ca(0xFFFFFF, 230), ca(0xFFE36B, 0)}));
            g.fill(new Ellipse2D.Double(10, 50, 44, 44));
        });
    }

    private static T snowflake() {
        return make(64, 64, g -> {
            g.translate(32, 32);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 6; i++) {
                g.drawLine(0, 0, 0, -28);
                g.drawLine(0, -16, -8, -24);
                g.drawLine(0, -16, 8, -24);
                g.rotate(Math.PI / 3);
            }
        });
    }

    private static T heartSprite() {
        return make(64, 64, g -> {
            GeneralPath h = new GeneralPath();
            h.moveTo(32, 58); h.curveTo(32, 58, 4, 40, 4, 20); h.curveTo(4, 8, 14, 3, 22, 3);
            h.curveTo(28, 3, 31, 7, 32, 11); h.curveTo(33, 7, 36, 3, 42, 3); h.curveTo(50, 3, 60, 8, 60, 20); h.curveTo(60, 40, 32, 58, 32, 58); h.closePath();
            g.setPaint(new GradientPaint(0, 0, c(0xFF7AA8), 0, 64, c(0xE0115F)));
            g.fill(h);
            g.setColor(ca(0xFFFFFF, 150));
            g.fill(new Ellipse2D.Double(14, 10, 12, 9));
        });
    }

    private static T orb(int rgb) {
        return make(64, 64, g -> {
            g.setPaint(new RadialGradientPaint(32, 32, 32, new float[]{0f, 0.25f, 0.6f, 1f},
                    new Color[]{ca(0xFFFFFF, 255), ca(rgb, 230), ca(rgb, 70), ca(rgb, 0)}));
            g.fillRect(0, 0, 64, 64);
        });
    }

    private static T spark() {
        return make(64, 64, g -> {
            g.setPaint(new RadialGradientPaint(32, 32, 30, new float[]{0f, 1f}, new Color[]{ca(0xFFFFFF, 200), ca(0xFFFFFF, 0)}));
            g.fillRect(0, 0, 64, 64);
            g.setColor(Color.WHITE);
            Path2D s = new Path2D.Double();
            s.moveTo(32, 2); s.lineTo(36, 28); s.lineTo(62, 32); s.lineTo(36, 36); s.lineTo(32, 62); s.lineTo(28, 36); s.lineTo(2, 32); s.lineTo(28, 28); s.closePath();
            g.fill(s);
        });
    }

    private static T boltSprite() {
        return make(64, 96, g -> {
            Path2D b = new Path2D.Double();
            b.moveTo(38, 2); b.lineTo(8, 54); b.lineTo(30, 54); b.lineTo(22, 94); b.lineTo(58, 36); b.lineTo(36, 36); b.closePath();
            for (int i = 6; i >= 1; i--) { g.setColor(ca(0x7CF7FF, 25)); g.setStroke(new BasicStroke(i * 2.5f)); g.draw(b); }
            g.setColor(c(0xE6FDFF));
            g.fill(b);
        });
    }

    /** Small color helpers (no dependency on the GUI package). */
    static final class Art2 {
        static Color mix(Color a, Color b, double t) {
            t = Math.max(0, Math.min(1, t));
            return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t), (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                    (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t), (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
        }
    }

    static Rectangle2D rect(double x, double y, double w, double h) { return new Rectangle2D.Double(x, y, w, h); }
}
