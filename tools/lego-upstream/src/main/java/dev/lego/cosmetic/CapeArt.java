package dev.lego.cosmetic;

import static dev.lego.cosmetic.Capes.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/** Cape designs part 1 (canvas FW x FH = 160 x 256, supersampled 2x). */
final class CapeArt {
    private CapeArt() { }

    static void defineAll() {
        def("lego", 16, 8, 0xFFE27A, 0x9A6A12, 0x5E060C, CapeArt::lego);
        def("galaxy", 16, 6, 0xEDE6FF, 0x5A4A8A, 0x120824, CapeArt::galaxy);
        def("flames", 16, 12, 0xFFC27A, 0x5A220A, 0x240805, CapeArt::flames);
        def("ice", 12, 6, 0xFFFFFF, 0x6E96BE, 0x163A64, CapeArt::ice);
        def("aurora", 16, 6, 0xB8FFE8, 0x1E4E52, 0x06121E, CapeArt::aurora);
        def("sunset", 16, 8, 0xFFD98A, 0xA02A6A, 0x1E0638, CapeArt::sunset);
        def("carbon", 1, 0, 0xFF5A5A, 0x6A0A0A, 0x0E0E11, CapeArtHd::carbon);
        def("neon", 16, 8, 0xFF8AF5, 0x4A0A62, 0x0C0218, CapeArt::neon);
        def("spotify", 16, 10, 0x7CFFA8, 0x0C5A2A, 0x0B0D0C, CapeArt::spotify);
        def("sakura", 16, 6, 0xFFF3D6, 0xB5885A, 0xE9A9C0, CapeArt::sakura);
        def("lava", 16, 6, 0x9A7A6A, 0x1E120E, 0x1C0906, CapeArt::lava);
        def("ocean", 16, 8, 0xE6FFFF, 0x2E6E92, 0x072A4A, CapeArt::ocean);
        def("matrix", 16, 12, 0x8CFFA8, 0x0A3A16, 0x020A04, CapeArt::matrix);
    }

    private static Font bold(float size) { return dev.lego.ui.Fonts.get(dev.lego.ui.Fonts.BOLD, size); }

    // ================================================================== lego
    static void lego(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        field(img, (u, v, x, y) -> {
            int c = mix(0xF4323A, 0x8A0710, v * 0.85 + u * 0.15);
            double d = Math.hypot(u - 0.5, (v - 0.42) * 0.62);
            return add(c, 0xFF7A5A, Math.exp(-d * d / 0.03) * 0.25);
        });
        // plate studs
        for (int row = 0; row < 14; row++)
            for (int col = 0; col < 9; col++) {
                double x = 16 + col * 16, y = 16 + row * 18.5;
                double dc = Math.hypot((x - 80) / 60, (y - 110) / 70);
                double a = 0.35 + 0.65 * clamp01(dc - 0.55);
                g.setColor(col(0x3A0004, 0.28 * a));
                g.fill(new Ellipse2D.Double(x - 5 + 1.4, y - 5 + 2, 10, 10));
                g.setPaint(new GradientPaint((float) x - 5, (float) y - 5, col(0xFFFFFF, 0.30 * a), (float) x + 5, (float) y + 5, col(0x5A0006, 0.25 * a)));
                g.fill(new Ellipse2D.Double(x - 5, y - 5, 10, 10));
                g.setColor(col(0xFFFFFF, 0.14 * a));
                g.setStroke(new BasicStroke(0.8f));
                g.draw(new Ellipse2D.Double(x - 3.2, y - 3.2, 6.4, 6.4));
            }
        // emblem
        dot(g, 80, 104, 62, 0x3A0004, 0.35);
        dev.lego.ui.Art.brick(g, 80 - 46, 104 - 50, 92, new Color(0xFFCD03));
        String s = "LEGO";
        g.setFont(bold(31));
        float w = g.getFontMetrics().stringWidth(s);
        g.setColor(col(0x3A0004, 0.55));
        g.drawString(s, 80 - w / 2 + 1.5f, 196 + 2.5f);
        g.setPaint(new GradientPaint(0, 170, Color.WHITE, 0, 198, new Color(0xFFE7B0)));
        g.drawString(s, 80 - w / 2, 196);
        g.setFont(bold(11));
        String c2 = "C L I E N T";
        float w2 = g.getFontMetrics().stringWidth(c2);
        g.setColor(col(0xFFE27A, 0.9));
        g.drawString(c2, 80 - w2 / 2, 216);
        g.setColor(col(0xFFE27A, 0.5));
        g.setStroke(new BasicStroke(1.2f));
        g.drawLine(30, 211, (int) (80 - w2 / 2 - 6), 211);
        g.drawLine((int) (80 + w2 / 2 + 6), 211, 130, 211);
        sweep(img, (double) k / n, 0.09, 0.42);
    }

    // ================================================================== galaxy
    static void galaxy(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double rot = ph / 2, cx = FW * 0.5, cy = FH * 0.43;
        field(img, (u, v, x, y) -> {
            double nx = u * 2.6, ny = v * 4.2;
            double w1 = fbm(nx + 3, ny, 0, 0, 3, 11);
            double neb = fbm(nx + w1 * 1.6, ny + w1 * 1.2, 0, 0, 5, 12);
            double hueN = fbm(nx * 0.6 + 9, ny * 0.6, 0, 0, 3, 13);
            int c = mix(0x020109, 0x0B0420, v);
            double ni = Math.pow(smooth(0.38, 0.9, neb), 1.5);
            c = add(c, ramp(hueN, 0x1E3AFF, 0x6A2BE0, 0xE0409F, 0xFF7A5A), ni * 0.7);
            // spiral galaxy (tilted disk)
            double dx = x + 0.5 - cx, dy = (y + 0.5 - cy) / 0.62;
            double r = Math.sqrt(dx * dx + dy * dy) + 0.001;
            double th = Math.atan2(dy, dx) - rot - 2.9 * Math.log(r / 16 + 0.2);
            double tt = ((th % Math.PI) + Math.PI) % Math.PI;
            double arms = Math.pow(0.5 + 0.5 * Math.cos(2 * th), 2.4);
            double dust = fbm(r * 0.11, tt * 8 / Math.PI, 0, 8, 3, 21);
            double fall = Math.exp(-r / 34);
            double inten = arms * fall * (0.35 + 0.95 * dust) + Math.exp(-(r / 8) * (r / 8)) * 1.4 + Math.exp(-r / 18) * 0.3;
            int gc = mix(0xFFF0D0, 0x9CC0FF, smooth(4, 30, r));
            c = add(c, gc, inten * 0.95);
            if (dust > 0.62) c = add(c, 0xFF5AA8, (dust - 0.62) * 2.2 * arms * fall);
            double lane = smooth(0.5, 0.75, fbm(r * 0.2 + 5, tt * 8 / Math.PI, 0, 8, 2, 23)) * arms * Math.exp(-r / 45) * 0.55;
            return shade(c, 1 - lane);
        });
        Random rn = new Random(5);
        for (int i = 0; i < 150; i++) {
            double x = rn.nextDouble() * FW, y = rn.nextDouble() * FH;
            double big = Math.pow(rn.nextDouble(), 5);
            int j = 1 + rn.nextInt(2);
            double p0 = rn.nextDouble() * Math.PI * 2;
            double a = (0.35 + 0.65 * (0.5 + 0.5 * Math.sin(ph * j + p0))) * (0.5 + 0.5 * rn.nextDouble());
            int tint = rn.nextInt(3) == 0 ? 0xFFD6B0 : (rn.nextBoolean() ? 0xB8D0FF : 0xFFFFFF);
            star(g, x, y, 0.7 + big * 2.6, tint, a);
        }
    }

    // ================================================================== flames
    static void flames(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        field(img, (u, v, x, y) -> {
            double X = u * 4, Yn = v * 6 + t * 12;
            double w = fbm(X * 1.2 + 7, Yn, 0, 6, 3, 5);
            double n1 = fbm(X + (w - 0.5) * 1.6, Yn + (w - 0.5) * 0.8, 0, 6, 5, 7);
            double tongues = 0.13 * Math.cos(u * Math.PI * 2 * 3 + (w - 0.5) * 4);
            double heat = v * 1.32 - 0.42 + (n1 - 0.5) * 1.25 + tongues;
            int bg = mix(0x0A0202, 0x2E0805, v);
            bg = add(bg, 0xFF4A10, Math.pow(v, 3) * 0.35);
            int fire = ramp(heat / 1.2, 0x5A0A05, 0xB0180A, 0xF04A0E, 0xFF8E1E, 0xFFC94A, 0xFFF3CC);
            double a = smooth(-0.02, 0.14, heat);
            int c = mix(bg, fire, a);
            // hot inner glow
            return add(c, 0xFFE8A0, smooth(0.85, 1.3, heat) * 0.25);
        });
        Random rn = new Random(8);
        for (int i = 0; i < 46; i++) {
            double x0 = rn.nextDouble(), y0 = rn.nextDouble();
            int j = 1 + rn.nextInt(2);
            double s = rn.nextDouble() * Math.PI * 2;
            double y = ((y0 - j * t) % 1 + 1) % 1;
            double x = (x0 + 0.035 * Math.sin(ph * j + s)) * FW;
            double life = Math.pow(y, 0.7);
            double r = 1.2 + rn.nextDouble() * 1.8;
            addDot(img, x, y * FH, r * 3, 0xFF6A10, 0.5 * life);
            addDot(img, x, y * FH, r, 0xFFE0A0, 0.9 * life);
        }
    }

    // ================================================================== ice
    static void ice(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final int CW = 26, CH = 30;
        field(img, (u, v, x, y) -> {
            int base = ramp(v, 0xEAFBFF, 0xA6DCF6, 0x55A4DA, 0x1D5296);
            int gx = x / CW, gy = y / CH;
            double d1 = 1e9, d2 = 1e9;
            int best = 0;
            for (int oy = -1; oy <= 1; oy++)
                for (int ox = -1; ox <= 1; ox++) {
                    int cx = gx + ox, cy = gy + oy;
                    double px = (cx + 0.15 + 0.7 * hash(cx, cy, 3)) * CW, py = (cy + 0.15 + 0.7 * hash(cx, cy, 4)) * CH;
                    double d = Math.hypot(x - px, y - py);
                    if (d < d1) { d2 = d1; d1 = d; best = cx * 131 + cy; } else if (d < d2) d2 = d;
                }
            double facet = 0.86 + 0.26 * hash(best, 1, 9);
            int c = shade(base, facet);
            double edge = 1 - smooth(0, 3.2, d2 - d1);
            c = add(c, 0xFFFFFF, edge * 0.38);
            double ex = Math.min(u, 1 - u) * 1.6, ey = Math.min(v, 1 - v);
            double frost = smooth(0.45, 0.85, fbm(u * 9, v * 14, 0, 0, 4, 44) + (0.3 - Math.min(ex, ey)) * 1.4);
            return mix(c, 0xF4FCFF, frost * 0.75);
        });
        // snowflake emblem
        double cx = 80, cy = 112, R = 44;
        Path2D sf = new Path2D.Double();
        for (int i = 0; i < 6; i++) {
            double a = Math.PI / 3 * i - Math.PI / 2;
            double ca = Math.cos(a), sa = Math.sin(a);
            sf.moveTo(cx, cy); sf.lineTo(cx + ca * R, cy + sa * R);
            for (double t : new double[]{0.38, 0.62, 0.82}) {
                double bl = R * (0.34 - (t - 0.38) * 0.35);
                for (int sgn = -1; sgn <= 1; sgn += 2) {
                    double b = a + sgn * Math.PI / 4;
                    sf.moveTo(cx + ca * R * t, cy + sa * R * t);
                    sf.lineTo(cx + ca * R * t + Math.cos(b) * bl, cy + sa * R * t + Math.sin(b) * bl);
                }
            }
        }
        dot(g, cx, cy, 64, 0x0A3A7A, 0.35);
        g.setColor(col(0x0E3C78, 0.55));
        g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(sf);
        glowStroke(g, sf, 0xDDF6FF, 3.4f, 1.8f, 0.95);
        Path2D hex = new Path2D.Double();
        for (int i = 0; i < 6; i++) {
            double a = Math.PI / 3 * i - Math.PI / 2;
            if (i == 0) hex.moveTo(cx + Math.cos(a) * 9, cy + Math.sin(a) * 9); else hex.lineTo(cx + Math.cos(a) * 9, cy + Math.sin(a) * 9);
        }
        hex.closePath();
        g.setPaint(new GradientPaint((float) cx - 9, (float) cy - 9, Color.WHITE, (float) cx + 9, (float) cy + 9, new Color(0x7FC8F0)));
        g.fill(hex);
        sweep(img, (double) k / n, 0.07, 0.35);
        Random rn = new Random(21);
        for (int i = 0; i < 26; i++) {
            double x = 14 + rn.nextDouble() * 132, y = 14 + rn.nextDouble() * 228;
            int j = 1 + rn.nextInt(2);
            double a = Math.pow(Math.max(0, Math.sin(ph * j + rn.nextDouble() * 6.28)), 3);
            star(g, x, y, 1.2 + rn.nextDouble() * 1.6, 0xCFF3FF, a);
        }
    }

    // ================================================================== aurora
    private static int auroraSky(double u, double v, double ph) {
        int c = ramp(v / 0.78, 0x010309, 0x03102A, 0x0A2844, 0x13404C);
        double[] base = {0.34, 0.46, 0.24};
        double[] str = {0.95, 0.7, 0.5};
        for (int i = 0; i < 3; i++) {
            double yl = base[i] + 0.055 * Math.sin(Math.PI * 2 * u + ph + i * 2.1) + 0.03 * Math.sin(Math.PI * 4 * u - ph * 2 + i) + 0.012 * Math.sin(Math.PI * 10 * u + ph * 3 + i);
            double d = v - yl;
            double e = d > 0 ? Math.exp(-d / 0.016) : Math.exp(d / (0.15 + 0.05 * i));
            if (e < 0.004) continue;
            double rays = 0.55 + 0.3 * Math.sin(u * Math.PI * 2 * 9 + 2.4 * Math.sin(u * Math.PI * 2 * 2 + ph + i) + i) + 0.15 * Math.sin(u * Math.PI * 2 * 23 - ph * 2 + i * 3);
            double tt = clamp01(-d / 0.24);
            int col = ramp(tt, 0x7CFFB8, 0x2EE8B8, 0x3A8BFF, 0xB05BFF);
            c = add(c, col, e * clamp01(rays) * str[i]);
        }
        return c;
    }

    private static double mountain(double u) {
        return 0.735 + 0.05 * Math.sin(u * 7 + 1) + 0.03 * Math.sin(u * 17 + 2) + 0.018 * (vn(u * 26, 0.5, 0, 0, 3) - 0.5) - 0.07 * Math.exp(-Math.pow((u - 0.3) / 0.12, 2));
    }

    static void aurora(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double lake = 0.86;
        Random rn = new Random(2);
        final double[][] stars = new double[70][];
        for (int i = 0; i < stars.length; i++) stars[i] = new double[]{rn.nextDouble(), rn.nextDouble() * 0.7, rn.nextDouble(), 1 + rn.nextInt(2), rn.nextDouble() * 6.28};
        field(img, (u, v, x, y) -> {
            double vv = v;
            boolean refl = v > lake;
            if (refl) vv = 2 * lake - v + 0.006 * Math.sin(v * 160 + ph * 2);
            double m = mountain(u);
            int c;
            if (vv > m) {
                c = mix(0x070E1A, 0x02050A, (vv - m) * 6);
                double rim = Math.exp(-(vv - m) / 0.006);
                c = add(c, 0xDDF2FF, rim * 0.35);
                double snow = smooth(0.02, 0, vv - m) * smooth(0.72, 0.68, m);
                c = mix(c, 0xB8D2E8, snow * 0.6 * (0.5 + 0.5 * vn(u * 40, vv * 40, 0, 0, 5)));
            } else c = auroraSky(u, vv, ph);
            if (refl) {
                c = shade(mix(c, 0x0A2438, 0.25), 0.55);
                c = add(c, 0xFFFFFF, Math.exp(-(v - lake) / 0.004) * 0.12);
            }
            return c;
        });
        for (double[] s : stars) {
            double y = s[1] * FH;
            if (s[1] > mountain(s[0]) - 0.02) continue;
            double a = (0.4 + 0.6 * (0.5 + 0.5 * Math.sin(ph * s[3] + s[4]))) * (0.4 + 0.6 * s[2]);
            star(g, s[0] * FW, y, 0.6 + s[2] * s[2] * 1.8, 0xDDE8FF, a);
        }
    }

    // ================================================================== synthwave sunset (grid scrolls)
    static void sunset(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n, hz = 0.6;
        final double scx = 0.5 * FW, scy = 0.4 * FH, sr = 0.31 * FW;
        field(img, (u, v, x, y) -> {
            if (v < hz) {
                int c = ramp(v / hz, 0x0C0322, 0x2E0852, 0x8A1C78, 0xE83A7C, 0xFF9A5A);
                double dx = x + 0.5 - scx, dy = y + 0.5 - scy, d = Math.hypot(dx, dy);
                c = add(c, 0xFF5A9A, Math.exp(-Math.max(0, d - sr) / 16) * 0.45);
                if (d < sr) {
                    double s = (dy + sr) / (2 * sr);
                    int sc = ramp(s, 0xFFF7A0, 0xFFC04A, 0xFF6A5A, 0xFF2E8A);
                    boolean gap = false;
                    if (s > 0.48) {
                        double f = ((s * 9 - t) % 1 + 1) % 1;
                        gap = f < (s - 0.48) * 1.25;
                    }
                    double edgeA = smooth(sr, sr - 1.5, d);
                    if (!gap) c = mix(c, sc, edgeA);
                }
                // mountains (valley in the middle)
                double mh = hz - (0.035 + 0.11 * Math.pow(Math.abs(u - 0.5) * 2, 1.6)) * (0.75 + 0.5 * vn(u * 9, 1, 0, 0, 77));
                if (v > mh) {
                    int mc = mix(0x3A0E5E, 0x14042A, (v - mh) / 0.1);
                    mc = add(mc, 0xFF6AC8, Math.exp(-(v - mh) / 0.005) * 0.6);
                    c = mc;
                }
                return c;
            }
            double dv = v - hz + 0.0015;
            double z = 0.07 / dv;
            int c = mix(0x22063E, 0x080112, smooth(0, 0.4, dv));
            double zl = ((z + t) % 1 + 1) % 1;
            double dz = Math.min(zl, 1 - zl);
            double thick = 0.07 / (dv * dv) * (1.3 / FH);
            double hl = smooth(thick * 1.6, thick * 0.3, dz);
            double xw = (u - 0.5) * z * 7;
            double xl = ((xw % 1) + 1) % 1, dx = Math.min(xl, 1 - xl);
            double thx = z * 7 * (1.3 / FW);
            double vl = smooth(thx * 1.6, thx * 0.3, dx);
            double fade = smooth(0.005, 0.12, dv);
            double line = Math.max(hl, vl) * fade;
            c = add(c, 0xFF3AD0, line * 0.95);
            c = add(c, 0xFFFFFF, line * line * 0.35);
            c = add(c, 0xFF4AA0, Math.exp(-dv / 0.03) * 0.5);
            return c;
        });
        Random rn = new Random(33);
        for (int i = 0; i < 40; i++) {
            double x = rn.nextDouble() * FW, y = rn.nextDouble() * FH * 0.3;
            double a = (0.5 + 0.5 * Math.sin(ph * (1 + rn.nextInt(2)) + rn.nextDouble() * 6.28)) * (1 - y / (FH * 0.3));
            star(g, x, y, 0.6 + rn.nextDouble() * 1.4, 0xFFD8F0, a);
        }
    }

    // ================================================================== carbon
    static void carbon(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final int C = 8;
        field(img, (u, v, x, y) -> {
            int cx = x / C, cy = y / C;
            double fx = (x % C + 0.5) / C, fy = (y % C + 0.5) / C;
            boolean horiz = ((cx + cy) & 3) < 2;
            double across = horiz ? fy : fx;
            double along = horiz ? fx : fy;
            double tow = Math.pow(Math.sin(Math.PI * across), 0.6);
            double fib = 0.93 + 0.07 * Math.sin(across * Math.PI * 7 + along * 0.6);
            double lightDir = horiz ? 0.55 + 0.45 * Math.sin(u * 5 - v * 3 + 1) : 0.55 + 0.45 * Math.sin(u * 4 + v * 2 + 3);
            int c = mix(0x0C0D10, 0x4A4E58, tow * fib * (0.35 + 0.5 * lightDir));
            double coat = Math.exp(-Math.pow((u * 0.9 - v * 0.55 - 0.12) / 0.1, 2)) * 0.14 + Math.exp(-Math.pow((u * 0.9 - v * 0.55 + 0.35) / 0.2, 2)) * 0.08;
            return add(c, 0xFFFFFF, coat);
        });
        for (int s = 0; s < 2; s++) {
            double y = s == 0 ? 40 : 212;
            g.setPaint(new GradientPaint(0, (float) y - 5, new Color(0xFF3A3A), 0, (float) y + 5, new Color(0x8A0A0A)));
            g.fill(new java.awt.geom.Rectangle2D.Double(0, y - 5, FW, 10));
            g.setColor(col(0xFFFFFF, 0.35));
            g.fill(new java.awt.geom.Rectangle2D.Double(0, y - 5, FW, 1.5));
            g.setColor(col(0xFF3A3A, 0.8));
            g.fill(new java.awt.geom.Rectangle2D.Double(0, y + 8, FW, 2));
        }
        // badge
        double cx = 80, cy = 124;
        dot(g, cx, cy, 58, 0x000000, 0.55);
        g.setPaint(new LinearGradientPaint((float) cx - 34, (float) cy - 34, (float) cx + 34, (float) cy + 34, new float[]{0f, 0.45f, 0.55f, 1f},
                new Color[]{new Color(0xF4F6FA), new Color(0x9AA2B0), new Color(0x5A6270), new Color(0xC8CED8)}));
        g.fill(new Ellipse2D.Double(cx - 34, cy - 34, 68, 68));
        g.setPaint(new RadialGradientPaint((float) cx, (float) cy - 8, 36, new float[]{0f, 1f}, new Color[]{new Color(0x2A2C32), new Color(0x0E0F12)}));
        g.fill(new Ellipse2D.Double(cx - 29, cy - 29, 58, 58));
        dev.lego.ui.Art.brick(g, cx - 24, cy - 27, 48, new Color(0xE3000B));
        g.setColor(col(0xFFFFFF, 0.45));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new java.awt.geom.Arc2D.Double(cx - 32, cy - 32, 64, 64, 60, 90, java.awt.geom.Arc2D.OPEN));
    }

    // ================================================================== neon grid
    static void neon(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n, hz = 0.64;
        final double pulse = 0.78 + 0.22 * Math.sin(ph);
        final int hue = mix(0xFF3AD0, 0x2AE6FF, 0.5 + 0.5 * Math.sin(ph));
        field(img, (u, v, x, y) -> {
            if (v < hz) {
                int c = ramp(v / hz, 0x05010C, 0x10032A, 0x2A0848);
                double vx = ((u * 10) % 1), vy = ((v * 16) % 1);
                double gl = Math.max(smooth(0.06, 0, Math.min(vx, 1 - vx)), smooth(0.06, 0, Math.min(vy, 1 - vy)));
                c = add(c, 0x6A2BE0, gl * 0.12);
                return add(c, 0xFF3AD0, Math.exp(-(hz - v) / 0.04) * 0.45);
            }
            double dv = v - hz + 0.0015, z = 0.06 / dv;
            int c = mix(0x14022A, 0x040008, smooth(0, 0.3, dv));
            double zl = ((z + t) % 1 + 1) % 1, dz = Math.min(zl, 1 - zl);
            double thick = 0.06 / (dv * dv) * (1.4 / FH);
            double hl = smooth(thick * 1.8, thick * 0.3, dz);
            double xw = (u - 0.5) * z * 8, xl = ((xw % 1) + 1) % 1, dx = Math.min(xl, 1 - xl);
            double thx = z * 8 * (1.4 / FW);
            double vl = smooth(thx * 1.8, thx * 0.3, dx);
            double line = Math.max(hl, vl) * smooth(0.005, 0.1, dv) * pulse;
            c = add(c, 0x2AE6FF, line);
            c = add(c, 0xFFFFFF, line * line * 0.3);
            return add(c, 0xFF3AD0, Math.exp(-dv / 0.025) * 0.55);
        });
        // neon triangle
        double cx = 80, cy = 98, R = 46;
        Path2D tri = new Path2D.Double();
        for (int i = 0; i < 3; i++) {
            double a = -Math.PI / 2 + i * Math.PI * 2 / 3;
            if (i == 0) tri.moveTo(cx + Math.cos(a) * R, cy + Math.sin(a) * R); else tri.lineTo(cx + Math.cos(a) * R, cy + Math.sin(a) * R);
        }
        tri.closePath();
        addDot(img, cx, cy + 4, 46, hue, 0.35 * pulse);
        g.setPaint(new GradientPaint((float) cx, (float) (cy - R), col(0x2A0848, 0.2), (float) cx, (float) (cy + R / 2), col(0xFF3AD0, 0.28)));
        g.fill(tri);
        glowStroke(g, tri, hue, 3.2f, 2.4f, pulse);
        Path2D tri2 = new Path2D.Double();
        for (int i = 0; i < 3; i++) {
            double a = Math.PI / 2 + i * Math.PI * 2 / 3;
            double r2 = R * 0.34;
            if (i == 0) tri2.moveTo(cx + Math.cos(a) * r2, cy + 2 + Math.sin(a) * r2); else tri2.lineTo(cx + Math.cos(a) * r2, cy + 2 + Math.sin(a) * r2);
        }
        tri2.closePath();
        glowStroke(g, tri2, mix(0x2AE6FF, 0xFF3AD0, 0.5 + 0.5 * Math.sin(ph)), 1.6f, 1.4f, 0.8);
        // scan line
        double sy = t * FH;
        int[] p = px(img);
        for (int y = Math.max(0, (int) sy - 6); y < Math.min(FH, (int) sy + 6); y++) {
            double a = Math.exp(-Math.pow((y - sy) / 2.2, 2)) * 0.35;
            for (int x = 0; x < FW; x++) p[y * FW + x] = add(p[y * FW + x], 0xB8F6FF, a);
        }
    }

    // ================================================================== spotify
    static void spotify(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double beat = SpotifyCape.LIVE.get() != null ? 0.7 + 0.3 * SpotifyCape.LIVE.get().beat : 0.7 + 0.3 * Math.pow(Math.max(0, Math.cos(ph * 2)), 4);
        field(img, (u, v, x, y) -> {
            int c = ramp(v, 0x0E1F15, 0x121413, 0x0A0B0A);
            double d = Math.hypot(x - 80, (y - 92) * 0.9);
            c = add(c, 0x1ED760, Math.exp(-(d / 62) * (d / 62)) * 0.32 * beat);
            double stripes = 0.5 + 0.5 * Math.sin((u * 0.8 - v * 1.1) * 40);
            return shade(c, 0.96 + 0.04 * stripes);
        });
        double cx = 80, cy = 92, R = 36 * (0.97 + 0.03 * beat);
        dot(g, cx + 2, cy + 5, R * 1.3, 0x000000, 0.55);
        g.setPaint(new RadialGradientPaint((float) (cx - R * 0.4), (float) (cy - R * 0.5), (float) (R * 1.6), new float[]{0f, 0.6f, 1f},
                new Color[]{new Color(0x5CF08E), new Color(0x1ED760), new Color(0x0F9A42)}));
        g.fill(new Ellipse2D.Double(cx - R, cy - R, R * 2, R * 2));
        g.setColor(new Color(0x0C0C0C));
        float[] wd = {7.4f, 6.2f, 5.0f};
        double[] wy = {-13, 1, 14}, ww = {26, 22, 17};
        for (int i = 0; i < 3; i++) {
            g.setStroke(new BasicStroke(wd[i] * (float) (R / 36), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            double y = cy + wy[i] * R / 36, hw = ww[i] * R / 36;
            g.draw(new QuadCurve2D.Double(cx - hw, y, cx + hw * 0.1, y - 10 * R / 36, cx + hw, y + 4 * R / 36));
        }
        g.setPaint(new GradientPaint((float) cx, (float) (cy - R), col(0xFFFFFF, 0.3), (float) cx, (float) cy, col(0xFFFFFF, 0)));
        g.fill(new Ellipse2D.Double(cx - R * 0.8, cy - R * 0.95, R * 1.6, R * 1.0));
        // equalizer (live: driven by the current Spotify song, see SpotifyCape)
        SpotifyCape.Live live = SpotifyCape.LIVE.get();
        int bars = 11;
        double bw = 7.2, gap = 3.6, x0 = 80 - (bars * bw + (bars - 1) * gap) / 2, by = 214;
        Random rn = new Random(4);
        for (int i = 0; i < bars; i++) {
            int a1 = 1 + rn.nextInt(3), a2 = 1 + rn.nextInt(3);
            double b1 = rn.nextDouble() * 6.28, b2 = rn.nextDouble() * 6.28;
            double env = 0.55 + 0.45 * Math.sin(Math.PI * (i + 0.5) / bars);
            double h = live != null ? 5 + 44 * live.bars[i]
                    : 6 + 46 * env * (0.25 + 0.75 * (0.5 + 0.5 * Math.sin(ph * a1 + b1)) * (0.55 + 0.45 * (0.5 + 0.5 * Math.sin(ph * a2 + b2))));
            double x = x0 + i * (bw + gap);
            g.setPaint(new GradientPaint(0, (float) (by - h), new Color(0xB8FFD0), 0, (float) by, new Color(0x14B04C)));
            g.fill(new RoundRectangle2D.Double(x, by - h, bw, h, 4, 4));
            if (live != null && live.peaks[i] > 0) {
                double py = by - 5 - 44 * live.peaks[i] - 4;
                g.setColor(col(0xE8FFF0, 0.9));
                g.fill(new RoundRectangle2D.Double(x, py, bw, 2.2, 2, 2));
            }
            g.setPaint(new GradientPaint(0, (float) by + 2, col(0x1ED760, 0.3), 0, (float) (by + 2 + h * 0.4), col(0x1ED760, 0)));
            g.fill(new RoundRectangle2D.Double(x, by + 2, bw, h * 0.4, 4, 4));
        }
        if (live != null) {
            // song title + artist + progress bar
            g.setFont(bold(10.5f));
            java.awt.FontMetrics fm = g.getFontMetrics();
            String tt = fit(live.title, fm, 140);
            g.setColor(col(0xFFFFFF, 0.95));
            g.drawString(tt, (float) (80 - fm.stringWidth(tt) / 2.0), 140);
            g.setFont(bold(8f));
            fm = g.getFontMetrics();
            String ar = fit(live.artist, fm, 140);
            g.setColor(col(0x9AF5BC, 0.85));
            g.drawString(ar, (float) (80 - fm.stringWidth(ar) / 2.0), 151);
            double px = 22, pw = 116, py = 232;
            g.setColor(col(0xFFFFFF, 0.18));
            g.fill(new RoundRectangle2D.Double(px, py, pw, 3, 3, 3));
            g.setColor(new Color(0x1ED760));
            g.fill(new RoundRectangle2D.Double(px, py, pw * live.progress, 3, 3, 3));
            dot(g, px + pw * live.progress, py + 1.5, 3.2, 0xFFFFFF, 0.95);
            if (!live.playing) {   // pause symbol
                g.setColor(col(0xFFFFFF, 0.8));
                g.fill(new RoundRectangle2D.Double(71, 180, 6, 18, 2, 2));
                g.fill(new RoundRectangle2D.Double(83, 180, 6, 18, 2, 2));
            }
        }
    }

    private static String fit(String s, java.awt.FontMetrics fm, int w) {
        if (s == null) return "";
        if (fm.stringWidth(s) <= w) return s;
        while (s.length() > 1 && fm.stringWidth(s + "…") > w) s = s.substring(0, s.length() - 1);
        return s + "…";
    }

    // ================================================================== sakura
    private static Path2D petal(double len, double wid) {
        Path2D p = new Path2D.Double();
        p.moveTo(0, len * 0.5);
        p.curveTo(wid * 0.9, len * 0.2, wid * 0.7, -len * 0.45, wid * 0.18, -len * 0.5);
        p.lineTo(0, -len * 0.36);
        p.lineTo(-wid * 0.18, -len * 0.5);
        p.curveTo(-wid * 0.7, -len * 0.45, -wid * 0.9, len * 0.2, 0, len * 0.5);
        p.closePath();
        return p;
    }

    static void blossom(Graphics2D g, double x, double y, double r, double rot, int pink) {
        AffineTransform o = g.getTransform();
        g.translate(x, y);
        g.rotate(rot);
        for (int i = 0; i < 5; i++) {
            AffineTransform o2 = g.getTransform();
            g.rotate(Math.PI * 2 * i / 5);
            g.translate(0, -r * 0.55);
            Path2D p = petal(r * 1.05, r * 0.95);
            g.setPaint(new GradientPaint(0, (float) (r * 0.5), new Color(pink), 0, (float) (-r * 0.5), new Color(0xFFF6FA)));
            g.fill(p);
            g.setColor(col(0xC0406A, 0.35));
            g.setStroke(new BasicStroke(0.6f));
            g.draw(p);
            g.setTransform(o2);
        }
        g.setColor(new Color(0xE0406E));
        g.fill(new Ellipse2D.Double(-r * 0.25, -r * 0.25, r * 0.5, r * 0.5));
        g.setColor(new Color(0xFFE27A));
        for (int i = 0; i < 6; i++) {
            double a = Math.PI * 2 * i / 6;
            g.fill(new Ellipse2D.Double(Math.cos(a) * r * 0.36 - 0.8, Math.sin(a) * r * 0.36 - 0.8, 1.6, 1.6));
        }
        g.setTransform(o);
    }

    static void sakura(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        field(img, (u, v, x, y) -> {
            int c = ramp(v, 0xFFF1F6, 0xFFD9E7, 0xF9BCD3, 0xEBA2C4);
            c = add(c, 0xFFFFFF, Math.exp(-Math.hypot(u - 0.78, v - 0.18) / 0.12) * 0.35);
            double m = 0.64 + 0.42 * Math.pow(Math.abs(u - 0.56), 1.25) + 0.012 * (vn(u * 18, 3, 0, 0, 51) - 0.5);
            if (v > m) {
                int mc = mix(0xC6A2D8, 0xE9C4DC, (v - m) * 2.5);
                double capLine = 0.69 + 0.02 * Math.sin(u * 60) * (vn(u * 30, 1, 0, 0, 5));
                if (v < capLine) mc = mix(0xFFFFFF, 0xDCCBEF, (v - m) / 0.06);
                c = mix(c, mc, smooth(0, 0.004, v - m) * 0.85);
            }
            c = mix(c, 0xFFF4F8, smooth(0.78, 1.0, v) * 0.55);
            return c;
        });
        // branch
        Path2D br = new Path2D.Double();
        br.moveTo(-10, 22); br.curveTo(40, 30, 70, 58, 128, 64);
        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(0x3A1E26));
        g.draw(br);
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(col(0x8A5A60, 0.8));
        g.draw(new QuadCurve2D.Double(-10, 19, 40, 24, 124, 61));
        double[][] twigs = {{30, 30, 44, 8}, {64, 48, 58, 86}, {94, 58, 120, 36}, {108, 62, 132, 90}, {48, 38, 20, 62}};
        g.setColor(new Color(0x3A1E26));
        g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (double[] tw : twigs) g.draw(new QuadCurve2D.Double(tw[0], tw[1], (tw[0] + tw[2]) / 2 + 4, (tw[1] + tw[3]) / 2 - 4, tw[2], tw[3]));
        Random rn = new Random(12);
        double[][] cl = {{44, 8}, {58, 86}, {120, 36}, {132, 90}, {20, 62}, {84, 58}, {12, 26}, {100, 70}, {70, 44}};
        for (double[] c : cl)
            for (int i = 0; i < 3; i++) {
                double r = 6 + rn.nextDouble() * 4;
                blossom(g, c[0] + rn.nextGaussian() * 7, c[1] + rn.nextGaussian() * 6, r, rn.nextDouble() * 6.28, rn.nextBoolean() ? 0xFF9EC0 : 0xFFB8D2);
            }
        // falling petals
        for (int i = 0; i < 30; i++) {
            double x0 = rn.nextDouble(), y0 = rn.nextDouble();
            double s = rn.nextDouble() * 6.28;
            int dir = rn.nextBoolean() ? 1 : -1;
            double y = ((y0 + t) % 1) * (FH + 30) - 15;
            double x = (((x0 + t) % 1) * (FW + 30) - 15) + Math.sin(ph + s) * 8;
            double sz = 4 + rn.nextDouble() * 3.5;
            AffineTransform o = g.getTransform();
            g.translate(x, y);
            g.rotate(s + ph * dir);
            g.scale(0.35 + 0.65 * Math.abs(Math.cos(ph + s)), 1);
            Path2D p = petal(sz * 1.3, sz);
            g.setPaint(new GradientPaint(0, (float) sz, new Color(0xFF8FB4), 0, (float) -sz, new Color(0xFFF0F6)));
            g.fill(p);
            g.setTransform(o);
        }
    }

    // ================================================================== lava
    static void lava(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        final int CX = 5, CY = 8, CW = FW / CX, CH = FH / CY;
        field(img, (u, v, x, y) -> {
            double yy = y - t * CH;
            int gx = Math.floorDiv(x, CW), gy = (int) Math.floor(yy / CH);
            double d1 = 1e9, d2 = 1e9;
            int best = 0;
            for (int oy = -1; oy <= 1; oy++)
                for (int ox = -1; ox <= 1; ox++) {
                    int cx = gx + ox, cy = gy + oy;
                    int hx = Math.floorMod(cx, CX), hy = Math.floorMod(cy, CY);
                    double px = (cx + 0.15 + 0.7 * hash(hx, hy, 61)) * CW, py = (cy + 0.15 + 0.7 * hash(hx, hy, 62)) * CH;
                    double dx = x - px, dy = yy - py;
                    double d = Math.sqrt(dx * dx + dy * dy * 1.2);
                    if (d < d1) { d2 = d1; d1 = d; best = hx * 17 + hy; } else if (d < d2) d2 = d;
                }
            double crack = d2 - d1;
            double flow = fbm(u * 5, v * 8 - t * 8, 0, 8, 3, 63);
            double pulse = 0.72 + 0.28 * Math.sin(ph + hash(best, 2, 64) * Math.PI * 2);
            double rockN = fbm(u * 14, (yy / FH) * 22, 0, 0, 4, 65);
            int rock = mix(0x3E2A24, 0x100908, rockN * 0.8 + hash(best, 5, 66) * 0.3);
            rock = shade(rock, 0.75 + 0.35 * smooth(0, 16, crack));
            double glow = Math.exp(-crack / 3.2) * (0.6 + 0.6 * flow) * pulse;
            double warm = Math.exp(-crack / 12) * 0.35 * pulse;
            int c = add(rock, 0xFF3A0A, warm);
            int lava = ramp(glow, 0x000000, 0x9A1200, 0xFF4A00, 0xFFA020, 0xFFF0A0);
            c = mix(c, lava, smooth(0.15, 0.55, glow));
            return c;
        });
        Random rn = new Random(18);
        for (int i = 0; i < 22; i++) {
            double x0 = rn.nextDouble(), y0 = rn.nextDouble();
            double y = ((y0 - t) % 1 + 1) % 1;
            double x = (x0 + 0.03 * Math.sin(ph + i)) * FW;
            addDot(img, x, y * FH, 3.5, 0xFF7A1A, 0.5 * y);
            addDot(img, x, y * FH, 1.2, 0xFFE8A0, 0.8 * y);
        }
    }

    // ================================================================== ocean (underwater)
    static void ocean(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        final double cph = Math.cos(ph), sph = Math.sin(ph);
        field(img, (u, v, x, y) -> {
            int c = ramp(v, 0x5ADCF6, 0x1592CF, 0x0A5A9E, 0x05285E, 0x021236);
            double s = u + v * 0.28;
            double rays = 0;
            double[] rc = {0.12, 0.3, 0.52, 0.7, 0.9};
            for (int i = 0; i < rc.length; i++) {
                double d = (s - rc[i] - 0.02 * Math.sin(ph + i)) / (0.035 + 0.02 * (i % 2));
                rays += Math.exp(-d * d) * (0.6 + 0.4 * Math.sin(ph * (1 + i % 2) + i * 1.7));
            }
            c = add(c, 0xCFFBFF, rays * 0.28 * Math.exp(-v * 2.4));
            double X = u * 16, Y = v * 22;
            double cs = Math.sin(X + 1.4 * Math.sin(Y * 0.7 + ph)) + Math.sin(Y + 1.4 * Math.sin(X * 0.8 - ph)) + Math.sin((X + Y) * 0.55 + ph);
            double caus = Math.pow(clamp01(cs / 3), 3) * 2.2;
            c = add(c, 0xE0FFFF, caus * 0.32 * Math.pow(1 - v, 2.2));
            double sl = 0.055 + 0.01 * Math.sin(u * Math.PI * 6 + ph) + 0.005 * Math.sin(u * Math.PI * 14 - ph * 2);
            if (v < sl) c = mix(c, 0xD8FAFF, 0.55 + 0.45 * smooth(sl, sl - 0.03, v));
            c = add(c, 0xFFFFFF, Math.exp(-Math.abs(v - sl) / 0.005) * 0.5);
            // sand
            double sand = 0.93 + 0.02 * Math.sin(u * 9 + 1);
            if (v > sand) c = mix(c, mix(0x8A7A52, 0x3A3422, (v - sand) * 10), smooth(sand, sand + 0.01, v) * 0.8);
            return c;
        });
        Random rn = new Random(44);
        // kelp
        for (int i = 0; i < 6; i++) {
            double bx = 10 + i * 28 + rn.nextDouble() * 10, hgt = 60 + rn.nextDouble() * 70;
            double s = rn.nextDouble() * 6.28;
            Path2D kp = new Path2D.Double();
            kp.moveTo(bx, FH);
            for (int j = 1; j <= 10; j++) {
                double yy = FH - hgt * j / 10.0;
                kp.lineTo(bx + Math.sin(ph + s + j * 0.5) * j * 0.9, yy);
            }
            g.setColor(col(mix(0x0E4A3A, 0x2A8A5A, rn.nextDouble()), 0.85));
            g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(kp);
        }
        // fish school
        for (int i = 0; i < 8; i++) {
            double x0 = rn.nextDouble(), y = 90 + rn.nextDouble() * 70;
            double x = ((x0 + t) % 1) * (FW + 40) - 20;
            double yy = y + Math.sin(ph * 2 + i) * 2;
            AffineTransform o = g.getTransform();
            g.translate(x, yy);
            g.setColor(col(0x06305A, 0.6));
            g.fill(new Ellipse2D.Double(-7, -2.6, 12, 5.2));
            Path2D tail = new Path2D.Double();
            tail.moveTo(-6, 0); tail.lineTo(-11, -3.5 + Math.sin(ph * 4 + i)); tail.lineTo(-11, 3.5 + Math.sin(ph * 4 + i)); tail.closePath();
            g.fill(tail);
            g.setTransform(o);
        }
        // bubbles
        for (int i = 0; i < 26; i++) {
            double x0 = rn.nextDouble() * FW, y0 = rn.nextDouble();
            int j = 1 + rn.nextInt(2);
            double r = 1.4 + Math.pow(rn.nextDouble(), 2) * 4;
            double y = ((y0 - j * t) % 1 + 1) % 1 * FH;
            double x = x0 + Math.sin(ph * j * 2 + i) * 3;
            g.setColor(col(0xE8FFFF, 0.55));
            g.setStroke(new BasicStroke(0.9f));
            g.draw(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
            g.setColor(col(0xFFFFFF, 0.14));
            g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
            g.setColor(col(0xFFFFFF, 0.85));
            g.fill(new Ellipse2D.Double(x - r * 0.55, y - r * 0.6, r * 0.5, r * 0.45));
        }
    }

    // ================================================================== matrix code rain
    static void matrix(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        field(img, (u, v, x, y) -> {
            int c = ramp(v, 0x010803, 0x020D05, 0x010502);
            double d = Math.hypot(u - 0.5, v - 0.45);
            return add(c, 0x0A5A1A, Math.exp(-d * d / 0.08) * 0.25);
        });
        int cols = 10, rows = 16, cs = 16;
        int[] p = px(img);
        for (int c = 0; c < cols; c++) {
            for (int drop = 0; drop < 2; drop++) {
                int sp = hash(c, drop, 91) < 0.4 ? 2 : 1;
                int head = (int) (hash(c, drop, 92) * rows) + drop * rows / 2;
                int L = 5 + (int) (hash(c, drop, 93) * 9);
                int hr = Math.floorMod(head + sp * k, rows);
                for (int r = 0; r < rows; r++) {
                    int dist = Math.floorMod(hr - r, rows);
                    if (dist >= L) continue;
                    double b = dist == 0 ? 1 : Math.pow(1 - dist / (double) L, 1.4) * 0.85;
                    int flick = hash(c, r, 94) < 0.2 ? k / 4 : 0;
                    int gly = (int) (hash(c * 31 + flick, r, 95) * 1e6);
                    int color = dist == 0 ? 0xEAFFEA : mix(0x06401A, 0x3CFF78, b);
                    for (int gy = 0; gy < 7; gy++)
                        for (int gx = 0; gx < 5; gx++) {
                            int bit = gx < 3 ? (gly >> (gy * 3 + gx)) & 1 : (gly >> (gy * 3 + (4 - gx))) & 1;
                            if (gx == 2 && gy % 3 == 0) bit = 1;
                            if (((gly >> 21) & 1) == 1 && gx >= 3) bit = (gly >> (gy + 22 + gx)) & 1;
                            if (bit == 0) continue;
                            for (int yy = 0; yy < 2; yy++)
                                for (int xx = 0; xx < 2; xx++) {
                                    int px = c * cs + 3 + gx * 2 + xx, py = r * cs + 1 + gy * 2 + yy;
                                    if (px < FW && py < FH) p[py * FW + px] = over(p[py * FW + px], color, 0.25 + 0.75 * b);
                                }
                        }
                    if (dist == 0) addDot(img, c * cs + 8, r * cs + 8, 9, 0x3CFF78, 0.35);
                }
            }
        }
    }
}
