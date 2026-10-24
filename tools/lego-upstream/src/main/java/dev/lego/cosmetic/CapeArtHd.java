package dev.lego.cosmetic;

import static dev.lego.cosmetic.Capes.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * HD remaster of the flattest classic capes (canvas FW x FH = 160 x 256, supersampled 2x): every design gets a
 * height field that is lit ({@link #relief}) so scales, studs, weave and folds have real depth, highlights and
 * ambient occlusion. Frame counts are unchanged.
 */
final class CapeArtHd {
    private CapeArtHd() { }

    // ================================================================== lighting of a height field
    /**
     * Lights the image with a height field (0..1 per pixel, same size): diffuse from the upper left, specular,
     * cavity occlusion and soft cast shadows.
     * @param str    bump strength (pixels of height per unit)
     * @param spec   specular strength
     * @param ao     cavity occlusion strength
     * @param shadow cast shadow strength (0 = none)
     */
    static void relief(BufferedImage img, double[] h, double str, double spec, double ao, double shadow) {
        int w = img.getWidth(), hh = img.getHeight();
        int[] p = px(img);
        double lx = -0.5, ly = -0.62, lz = 0.6, ll = Math.sqrt(lx * lx + ly * ly + lz * lz);
        lx /= ll; ly /= ll; lz /= ll;
        double hx = lx, hy = ly, hz = lz + 1, hl = Math.sqrt(hx * hx + hy * hy + hz * hz);
        hx /= hl; hy /= hl; hz /= hl;
        double[] blur = boxBlur(h, w, hh, 5);
        for (int y = 0; y < hh; y++)
            for (int x = 0; x < w; x++) {
                int i = y * w + x;
                double gx = h[y * w + Math.min(w - 1, x + 1)] - h[y * w + Math.max(0, x - 1)];
                double gy = h[Math.min(hh - 1, y + 1) * w + x] - h[Math.max(0, y - 1) * w + x];
                double nx = -gx * str, ny = -gy * str, nz = 1, nl = Math.sqrt(nx * nx + ny * ny + nz * nz);
                nx /= nl; ny /= nl; nz /= nl;
                double d = nx * lx + ny * ly + nz * lz;
                double m = 1 + (d - lz) * 1.35;
                double cav = Math.min(0, h[i] - blur[i]) * ao;
                m *= 1 + cav;
                if (shadow > 0) {
                    // march towards the light a few pixels
                    double occ = 0;
                    for (int s = 1; s <= 6; s++) {
                        int sx = x - (int) Math.round(s * 0.8), sy = y - s;
                        if (sx < 0 || sy < 0) break;
                        double dh = h[sy * w + sx] - h[i] - s * 0.06;
                        if (dh > 0) occ = Math.max(occ, Math.min(1, dh * 3));
                    }
                    m *= 1 - occ * shadow;
                }
                int c = shade(p[i], m);
                double sp = Math.pow(Math.max(0, nx * hx + ny * hy + nz * hz), 40) * spec;
                p[i] = (p[i] & 0xFF000000) | (add(c, 0xFFFFFF, sp) & 0xFFFFFF);
            }
    }

    static double[] boxBlur(double[] h, int w, int hh, int r) {
        double[] a = new double[h.length], b = new double[h.length];
        for (int y = 0; y < hh; y++) {
            double s = 0;
            int n = 0;
            for (int x = -r; x < w + r; x++) {
                int xa = x + r, xr = x - r - 1;
                if (xa < w && xa >= 0) { s += h[y * w + xa]; n++; }
                if (xr >= 0 && xr < w) { s -= h[y * w + xr]; n--; }
                if (x >= 0 && x < w) a[y * w + x] = s / Math.max(1, n);
            }
        }
        for (int x = 0; x < w; x++) {
            double s = 0;
            int n = 0;
            for (int y = -r; y < hh + r; y++) {
                int ya = y + r, yr = y - r - 1;
                if (ya < hh && ya >= 0) { s += a[ya * w + x]; n++; }
                if (yr >= 0 && yr < hh) { s -= a[yr * w + x]; n--; }
                if (y >= 0 && y < hh) b[y * w + x] = s / Math.max(1, n);
            }
        }
        return b;
    }

    /** Height field from a grayscale painting (red channel / 255). */
    static double[] heights(BufferedImage gray) {
        int[] q = px(gray);
        double[] h = new double[q.length];
        for (int i = 0; i < q.length; i++) h[i] = ((q[i] >> 16) & 255) / 255.0;
        return h;
    }

    static BufferedImage canvas() { return new BufferedImage(FW, FH, BufferedImage.TYPE_INT_ARGB); }

    // ================================================================== Drachenschuppen
    static void scales(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double SW = 22, SH = 12, R = 14;
        final double[] hgt = new double[FW * FH];
        field(img, (u, v, x, y) -> {
            int r0 = (int) Math.floor((y - R) / SH) - 1;
            for (int r = r0; r <= r0 + 3; r++) {
                double cyr = r * SH, off = (Math.floorMod(r, 2)) * SW / 2;
                int ci = (int) Math.floor((x - off) / SW + 0.5);
                for (int cc = ci - 1; cc <= ci + 1; cc++) {
                    double cxs = cc * SW + off;
                    double dx = x + 0.5 - cxs, dy = y + 0.5 - cyr;
                    if (dy < -2) continue;
                    // pointed shield shape: narrower towards the tip (down)
                    double wdt = R * (1 - 0.35 * clamp01(dy / R));
                    double d = Math.hypot(dx / wdt * R, dy) / R;
                    if (d > 1) continue;
                    double hs = hash(cc, r, 4);
                    double keel = Math.exp(-Math.pow(dx / 1.8, 2)) * 0.18 * clamp01(dy / R + 0.3);
                    double dome = Math.sqrt(Math.max(0, 1 - d * d));
                    hgt[y * FW + x] = 0.25 + 0.55 * dome + keel + (r % 2) * 0.02;
                    double irid = clamp01(0.5 + dx / (R * 2.4) + (hs - 0.5) * 0.35 + v * 0.25 - dome * 0.2);
                    int base = ramp(irid, 0x0A4A3A, 0x138A62, 0x1FC090, 0x7AE0A0, 0xE8C85A);
                    int c = shade(base, 0.85 + 0.25 * dome - v * 0.25);
                    // darker veins radiating from the scale root
                    double ang = Math.atan2(dy, dx);
                    double vein = Math.pow(Math.abs(Math.sin(ang * 5 + hs * 3)), 12) * 0.12 * d;
                    c = shade(c, 1 - vein);
                    if (d > 0.9) c = mix(c, 0xC8F0A0, (d - 0.9) / 0.1 * 0.35);
                    return c;
                }
            }
            hgt[y * FW + x] = 0;
            return 0x03140E;
        });
        relief(img, hgt, 9, 0.45, 1.1, 0.5);
        // a golden spine ridge down the middle
        for (int y = 0; y < FH; y += 12) {
            Path2D sp = new Path2D.Double();
            sp.moveTo(FW / 2.0 - 5, y + 10); sp.lineTo(FW / 2.0, y - 2); sp.lineTo(FW / 2.0 + 5, y + 10); sp.closePath();
            g.setPaint(new GradientPaint(FW / 2f - 5, 0, new Color(0xFFF0A0), FW / 2f + 5, 0, new Color(0x8A5A10)));
            g.fill(sp);
            g.setColor(col(0x000000, 0.4));
            g.setStroke(new BasicStroke(0.8f));
            g.draw(sp);
        }
    }

    // ================================================================== Tarnmuster
    static void camo(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        field(img, (u, v, x, y) -> {
            double w = fbm(u * 3, v * 5, 0, 0, 3, 70);
            double a = fbm(u * 3.2 + w, v * 5 + w, 0, 0, 4, 71), b = fbm(u * 4 + 5 + w * 0.6, v * 6, 0, 0, 4, 72);
            int c = 0x5A6A36;
            // soft printed edges
            c = mix(c, 0x2E3B1F, smooth(0.47, 0.41, a));
            c = mix(c, 0x8A7A4E, smooth(0.55, 0.61, b));
            c = mix(c, 0x1C1C14, smooth(0.57, 0.63, a) * smooth(0.53, 0.47, b));
            // fading / wear
            c = mix(c, 0x9A9A7A, fbm(u * 12, v * 18, 0, 0, 2, 73) * 0.12);
            return c;
        });
        // height: ripstop weave (fine twill + reinforcement grid) + raised patch
        BufferedImage hi = canvas();
        int[] q = px(hi);
        for (int y = 0; y < FH; y++)
            for (int x = 0; x < FW; x++) {
                double tw = ((x + y) % 4 < 2) ? 0.35 : 0.25;
                boolean grid = x % 12 == 0 || y % 12 == 0;
                int v = (int) ((tw + (grid ? 0.12 : 0)) * 255);
                q[y * FW + x] = 0xFF000000 | (v << 16) | (v << 8) | v;
            }
        Graphics2D hg = hi.createGraphics();
        dev.lego.ui.Gx.hints(hg);
        double cx = 80, cy = 70;
        hg.setColor(new Color(0x9A9A9A));
        hg.fill(new RoundRectangle2D.Double(cx - 22, cy - 16, 44, 32, 8, 8));
        hg.setColor(new Color(0xD8D8D8));
        hg.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[]{2f, 1.5f}, 0));
        hg.draw(new RoundRectangle2D.Double(cx - 19, cy - 13, 38, 26, 6, 6));
        hg.setColor(new Color(0xFFFFFF));
        hg.fill(XmasTex.starShape(cx, cy, 10, 4, 5));
        hg.dispose();
        // patch colors
        g.setColor(new Color(0x3A4424));
        g.fill(new RoundRectangle2D.Double(cx - 22, cy - 16, 44, 32, 8, 8));
        g.setColor(new Color(0xC8C090));
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[]{2f, 1.5f}, 0));
        g.draw(new RoundRectangle2D.Double(cx - 19, cy - 13, 38, 26, 6, 6));
        g.setColor(new Color(0xD8D0A0));
        g.fill(XmasTex.starShape(cx, cy, 10, 4, 5));
        double[] h = boxBlur(heights(hi), FW, FH, 1);
        relief(img, h, 6, 0.12, 0.8, 0.6);
    }

    // ================================================================== Bausteine (top view with real studs)
    static void bricks(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        int[] cols = {0xE3000B, 0xFFCD03, 0x006CB7, 0x00852B, 0xF57D20, 0xF4F4F4, 0xA0BCE8, 0xE870A0};
        final int[] cid = new int[FW * FH];
        java.util.Arrays.fill(cid, -1);
        final double[] hgt = new double[FW * FH];
        Random rn = new Random(4);
        double pitch = 16;
        java.util.List<double[]> studs = new java.util.ArrayList<>();
        for (int row = 0; row < 9; row++) {
            double y = row * pitch * 2 - 4;
            double x = -(row % 2) * pitch * 2;
            while (x < FW) {
                int len = 2 + rn.nextInt(3);
                int c = cols[rn.nextInt(cols.length)];
                double w = len * pitch, hh = pitch * 2;
                for (int yy = (int) Math.max(0, y); yy < Math.min(FH, y + hh); yy++)
                    for (int xx = (int) Math.max(0, x); xx < Math.min(FW, x + w); xx++) {
                        double ex = Math.min(xx + 0.5 - x, x + w - xx - 0.5), ey = Math.min(yy + 0.5 - y, y + hh - yy - 0.5);
                        double e = Math.min(ex, ey);
                        if (e < 0.6) continue;           // gap between bricks
                        cid[yy * FW + xx] = c;
                        hgt[yy * FW + xx] = 0.5 * smooth(0.6, 2.4, e);
                    }
                for (double sx = x + pitch / 2; sx < x + w; sx += pitch)
                    for (double sy = y + pitch / 2; sy < y + hh; sy += pitch) studs.add(new double[]{sx, sy, c});
                x += w;
            }
        }
        double r = pitch * 0.31;
        for (double[] s : studs)
            for (int yy = (int) (s[1] - r - 1); yy <= s[1] + r + 1; yy++)
                for (int xx = (int) (s[0] - r - 1); xx <= s[0] + r + 1; xx++) {
                    if (xx < 0 || yy < 0 || xx >= FW || yy >= FH) continue;
                    double d = Math.hypot(xx + 0.5 - s[0], yy + 0.5 - s[1]);
                    if (d > r) continue;
                    double top = 0.9 - 0.25 * smooth(r - 1.3, r, d);
                    hgt[yy * FW + xx] = Math.max(hgt[yy * FW + xx], top);
                }
        field(img, (u, v, x, y) -> {
            int c = cid[y * FW + x];
            if (c < 0) return 0x0C0C0F;
            return shade(c, 0.95 + 0.05 * hash(x / 3, y / 3, 11));
        });
        relief(img, hgt, 14, 0.65, 1.3, 0.75);
        // tiny embossed logo on the studs (light rim)
        g.setStroke(new BasicStroke(0.8f));
        for (double[] s : studs) {
            g.setColor(col(0xFFFFFF, 0.22));
            g.draw(new java.awt.geom.Arc2D.Double(s[0] - r * 0.55, s[1] - r * 0.55, r * 1.1, r * 1.1, 90, 110, java.awt.geom.Arc2D.OPEN));
        }
    }

    // ================================================================== Zielflagge (waving cloth)
    static void racing(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double[] hgt = new double[FW * FH];
        field(img, (u, v, x, y) -> {
            double wv = Math.sin(v * Math.PI * 2 * 1.2 + u * 5) + 0.35 * Math.sin(u * Math.PI * 2 * 1.6 - v * 4 + 1);
            hgt[y * FW + x] = 0.5 + 0.28 * wv + 0.02 * (((x + y) % 3 == 0) ? 1 : 0);
            double uu = u + 0.035 * wv, vv = v + 0.03 * Math.sin(u * Math.PI * 2 * 1.4 + v * 3);
            int cu = (int) Math.floor(uu * 6), cv = (int) Math.floor(vv * 9.6);
            // anti-aliased checker edges
            double fu = uu * 6 - cu, fv = vv * 9.6 - cv;
            double edge = Math.min(Math.min(fu, 1 - fu), Math.min(fv, 1 - fv));
            int c = ((cu + cv) & 1) == 0 ? 0xF2F2F2 : 0x161616;
            if (edge < 0.04) c = mix(c, 0x848484, (0.04 - edge) / 0.04 * 0.5);
            // red diagonal band (warped with the cloth)
            double band = vv * FH - (200 - uu * 50);
            if (band > -9 && band < 9) c = mix(0xE8202A, 0xA00A12, (band + 9) / 18);
            return c;
        });
        relief(img, hgt, 24, 0.25, 0.4, 0);
    }

    // ================================================================== Gewitter (lit clouds)
    private static double flash(int k) {
        switch (k) {
            case 3: return 1.0;
            case 4: return 0.45;
            case 5: return 0.8;
            case 6: return 0.25;
            case 11: return 0.7;
            case 12: return 0.3;
            default: return 0;
        }
    }

    static void storm(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n, fl = flash(k);
        final boolean boltA = k >= 3 && k <= 6;
        final double bx = boltA ? 64 : 110;
        final double[] hgt = new double[FW * FH];
        field(img, (u, v, x, y) -> {
            double w = fbm(u * 3 + t * 3, v * 4, 3, 0, 3, 40);
            double c1 = fbm(u * 3 + t * 3 + (w - 0.5) * 0.8, v * 4 + (w - 0.5), 3, 0, 5, 41);
            double dens = smooth(0.2, 0.75, c1 + (0.45 - v) * 1.1);
            hgt[y * FW + x] = dens * (0.6 + 0.4 * c1);
            int sky = ramp(v, 0x1E283C, 0x121A26, 0x080B12);
            int cloud = ramp(c1, 0x10141E, 0x2A344A, 0x55627E, 0x8A98B4);
            int c = mix(sky, cloud, dens);
            double lit = fl * (0.25 + 0.75 * c1) * Math.exp(-Math.hypot(x - bx, y - 40) / 90);
            c = add(c, 0xBFD8FF, lit * 0.9 + fl * 0.08);
            double hill = 0.9 + 0.03 * Math.sin(u * 8 + 1) + 0.015 * Math.sin(u * 23);
            if (v > hill) { c = add(mix(0x05070B, 0x020304, (v - hill) * 8), 0x8AA8FF, fl * 0.12); hgt[y * FW + x] = 0; }
            return c;
        });
        relief(img, hgt, 7, 0.08 + fl * 0.3, 0.5, 0.2);
        Random rn = new Random(71);
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 150; i++) {
            double x0 = rn.nextDouble(), y0 = rn.nextDouble();
            int j = 2 + rn.nextInt(2);
            double y = ((y0 + j * t) % 1) * (FH + 20) - 10, x = x0 * (FW + 30) - 15 + y * 0.12;
            double len = 7 + rn.nextDouble() * 7;
            g.setColor(col(0xA8C0E8, 0.18 + 0.2 * rn.nextDouble() + fl * 0.2));
            g.draw(new java.awt.geom.Line2D.Double(x, y, x + len * 0.12, y + len));
        }
        if (fl > 0) {
            Random br = new Random(boltA ? 5 : 9);
            Path2D p = new Path2D.Double();
            double x = bx, y = 30;
            p.moveTo(x, y);
            java.util.List<double[]> forks = new java.util.ArrayList<>();
            while (y < FH * 0.9) {
                x += br.nextGaussian() * 7;
                y += 8 + br.nextDouble() * 10;
                p.lineTo(x, y);
                if (br.nextDouble() < 0.18) forks.add(new double[]{x, y});
            }
            for (double[] f : forks) {
                double fx = f[0], fy = f[1];
                p.moveTo(fx, fy);
                double dir = br.nextBoolean() ? 1 : -1;
                for (int s = 0; s < 4; s++) { fx += dir * (4 + br.nextDouble() * 8); fy += 6 + br.nextDouble() * 8; p.lineTo(fx, fy); }
            }
            addDot(img, bx, 40, 50, 0x9FC8FF, 0.5 * fl);
            glowStroke(g, p, 0xA8D4FF, 2.6f, 3.2f, fl);
            // ground splash where it hits
            addDot(img, x, FH * 0.9, 26, 0xA8D4FF, 0.45 * fl);
        }
    }

    // ================================================================== Carbon (real twill relief + clear coat)
    static void carbon(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final int C = 8;
        final double[] hgt = new double[FW * FH];
        field(img, (u, v, x, y) -> {
            int cx = x / C, cy = y / C;
            double fx = (x % C + 0.5) / C, fy = (y % C + 0.5) / C;
            boolean horiz = ((cx + cy) & 3) < 2;
            double across = horiz ? fy : fx;
            double along = horiz ? fx : fy;
            double tow = Math.pow(Math.sin(Math.PI * across), 0.6);
            hgt[y * FW + x] = tow * 0.6 + 0.08 * Math.sin(Math.PI * along);
            double fib = 0.93 + 0.07 * Math.sin(across * Math.PI * 7 + along * 0.6);
            double lightDir = horiz ? 0.55 + 0.45 * Math.sin(u * 5 - v * 3 + 1) : 0.55 + 0.45 * Math.sin(u * 4 + v * 2 + 3);
            return mix(0x0C0D10, 0x4A4E58, tow * fib * (0.35 + 0.5 * lightDir));
        });
        relief(img, hgt, 6, 0.3, 0.8, 0);
        // clear coat reflections (after the relief: smooth lacquer on top)
        int[] p = px(img);
        for (int y = 0; y < FH; y++)
            for (int x = 0; x < FW; x++) {
                double u = (x + 0.5) / FW, v = (y + 0.5) / FH;
                double coat = Math.exp(-Math.pow((u * 0.9 - v * 0.55 - 0.12) / 0.1, 2)) * 0.16 + Math.exp(-Math.pow((u * 0.9 - v * 0.55 + 0.35) / 0.2, 2)) * 0.08;
                p[y * FW + x] = add(p[y * FW + x], 0xFFFFFF, coat);
            }
        for (int s = 0; s < 2; s++) {
            double y = s == 0 ? 40 : 212;
            g.setPaint(new GradientPaint(0, (float) y - 5, new Color(0xFF4A4A), 0, (float) y + 5, new Color(0x7A0A0A)));
            g.fill(new java.awt.geom.Rectangle2D.Double(0, y - 5, FW, 10));
            g.setColor(col(0xFFFFFF, 0.45));
            g.fill(new java.awt.geom.Rectangle2D.Double(0, y - 5, FW, 1.2));
            g.setColor(col(0x000000, 0.45));
            g.fill(new java.awt.geom.Rectangle2D.Double(0, y + 5, FW, 1.5));
            g.setColor(col(0xFF3A3A, 0.8));
            g.fill(new java.awt.geom.Rectangle2D.Double(0, y + 8, FW, 2));
        }
        // badge: chrome ring, dark glossy face, red brick
        double cx = 80, cy = 124;
        dot(g, cx + 3, cy + 5, 60, 0x000000, 0.6);
        g.setPaint(new java.awt.LinearGradientPaint((float) cx - 34, (float) cy - 34, (float) cx + 34, (float) cy + 34, new float[]{0f, 0.45f, 0.55f, 1f},
                new Color[]{new Color(0xFFFFFF), new Color(0xA2AABA), new Color(0x4A5260), new Color(0xD8DEE8)}));
        g.fill(new Ellipse2D.Double(cx - 34, cy - 34, 68, 68));
        g.setPaint(new java.awt.RadialGradientPaint((float) cx - 6, (float) cy - 10, 40, new float[]{0f, 1f}, new Color[]{new Color(0x32343C), new Color(0x0A0B0E)}));
        g.fill(new Ellipse2D.Double(cx - 29, cy - 29, 58, 58));
        dev.lego.ui.Art.brick(g, cx - 24, cy - 27, 48, new Color(0xE3000B));
        g.setColor(col(0xFFFFFF, 0.55));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new java.awt.geom.Arc2D.Double(cx - 32, cy - 32, 64, 64, 60, 90, java.awt.geom.Arc2D.OPEN));
        g.setColor(col(0xFFFFFF, 0.18));
        g.fill(new java.awt.geom.Arc2D.Double(cx - 27, cy - 27, 54, 54, 30, 120, java.awt.geom.Arc2D.CHORD));
    }

    // ================================================================== Glühwürmchen (deeper forest)
    static void fireflies(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        field(img, (u, v, x, y) -> {
            int c = ramp(v, 0x03100E, 0x082A24, 0x124A3A, 0x0C3424);
            c = add(c, 0xB8F0D8, Math.exp(-Math.hypot(u - 0.5, (v - 0.1) * 0.8) / 0.16) * 0.38);
            double fog = fbm(u * 3 + t * 3, v * 5, 3, 0, 4, 55) * Math.exp(-Math.pow((v - 0.72) / 0.14, 2));
            return add(c, 0x7AB8A8, fog * 0.4);
        });
        // light shafts from the moon glow
        int[] pp = px(img);
        for (int y = 0; y < FH; y++)
            for (int x = 0; x < FW; x++) {
                double u = (x + 0.5) / FW, v = (y + 0.5) / FH;
                double a = Math.atan2(u - 0.5, v - 0.02);
                double shaft = Math.pow(0.5 + 0.5 * Math.sin(a * 38 + 1.3), 8) * Math.exp(-v * 2.6) * 0.1 * (0.7 + 0.3 * Math.sin(ph + a * 5));
                pp[y * FW + x] = add(pp[y * FW + x], 0xC8F0D8, shaft);
            }
        Random rn = new Random(15);
        int[] lc = {0x1A5A4A, 0x0C3830, 0x041612};
        // trunks with bark relief (height field painted per layer)
        final double[] hgt = new double[FW * FH];
        for (int L = 0; L < 3; L++) {
            int cnt = L == 0 ? 8 : 5 - L;
            for (int i = 0; i < cnt; i++) {
                double x = L == 0 ? (i + 0.3 + rn.nextDouble() * 0.4) * FW / cnt : rn.nextDouble() * FW, w = 5 + L * 5 + rn.nextDouble() * 4;
                for (int y = 0; y < FH; y++) {
                    double half = (w / 2) + (w * 0.2) * y / FH;
                    for (int xx = (int) Math.max(0, x - half); xx < Math.min(FW, x + half); xx++) {
                        double f = (xx + 0.5 - x) / half;
                        double round = Math.sqrt(Math.max(0, 1 - f * f));
                        double bark = fbm(xx * 0.35, y * 0.06, 0, 0, 3, 90 + L);
                        int c = shade(lc[L], 0.7 + 0.45 * round * (0.6 + 0.4 * bark));
                        pp[y * FW + xx] = 0xFF000000 | c;
                        hgt[y * FW + xx] = round * 0.5 + bark * 0.25 + L * 0.1;
                    }
                }
            }
            g.setColor(new Color(lc[L]));
            for (int i = 0; i < 16; i++) {
                double x = rn.nextDouble() * FW, y = -10 + rn.nextDouble() * (30 + L * 10), r = 14 + rn.nextDouble() * 14;
                g.fill(new Ellipse2D.Double(x - r, y - r * 0.7, r * 2, r * 1.4));
            }
        }
        relief(img, hgt, 6, 0.1, 0.5, 0);
        // grass
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 160; i++) {
            double x = rn.nextDouble() * FW, h = 10 + rn.nextDouble() * 24, bend = rn.nextGaussian() * 5 + Math.sin(ph + x * 0.05) * 1.5;
            g.setColor(new Color(mix(0x031008, 0x2A6A3A, rn.nextDouble() * 0.7)));
            g.draw(new java.awt.geom.QuadCurve2D.Double(x, FH, x + bend * 0.3, FH - h * 0.6, x + bend, FH - h));
        }
        // fireflies: bokeh halo + hot core, lighting the grass below
        for (int i = 0; i < 28; i++) {
            double x0 = 12 + rn.nextDouble() * 136, y0 = 50 + rn.nextDouble() * 190;
            int j = 1 + rn.nextInt(2);
            double s1 = rn.nextDouble() * 6.28, s2 = rn.nextDouble() * 6.28, s3 = rn.nextDouble() * 6.28;
            double x = x0 + 8 * Math.cos(ph * j + s1) + 3 * Math.sin(2 * ph + s2), y = y0 + 6 * Math.sin(ph * j + s3);
            double blink = Math.pow(0.5 + 0.5 * Math.sin(ph * (1 + rn.nextInt(2)) + s2), 2.2);
            addDot(img, x, y, 16, 0x9AFF3A, 0.22 * blink + 0.02);
            addDot(img, x, y, 6, 0xD8FF6A, 0.5 * blink + 0.04);
            addDot(img, x, y, 2.0, 0xFFFFE0, 1.0 * blink + 0.1);
        }
    }
}
