package dev.lego.cosmetic;

import static dev.lego.cosmetic.Capes.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/** Cape designs part 2 (canvas FW x FH = 160 x 256, supersampled 2x). */
final class CapeArt2 {
    private CapeArt2() { }

    static void defineAll() {
        def("storm", 16, 10, 0xD0DCFF, 0x2A3450, 0x0E121C, CapeArtHd::storm);
        def("rainbow", 16, 8, 0xFFFFFF, 0x9A8AC8, 0xE6D8FF, CapeArt2::rainbow);
        def("snow", 16, 8, 0xFFFFFF, 0x7C9CC2, 0x14264A, CapeArt2::snow);
        def("fireflies", 16, 6, 0xE8F0A0, 0x3A4A1A, 0x0A1A12, CapeArtHd::fireflies);
        def("heart", 16, 12, 0xFFD8C8, 0x8A2A3A, 0x3A0414, CapeArt2::heart);
        def("enchant", 16, 10, 0xF0D8FF, 0x5A2A8A, 0x1A0A3A, CapeArt2::enchant);
        def("void", 16, 8, 0xFFC86A, 0x3A2A10, 0x050308, CapeArt2::blackHole);
        def("royal", 1, 0, 0xFFE9A0, 0x9A6A12, 0x4A0610, CapeArt2::royal);
        def("dragon", 1, 0, 0xFFE27A, 0x7A5A12, 0x0A2A1A, CapeArtHd::scales);
        def("camo", 1, 0, 0xA8A878, 0x3A3A22, 0x2A2A1A, CapeArtHd::camo);
        def("bricks", 1, 0, 0xFFE24A, 0x9A7A00, 0x1A1A1E, CapeArtHd::bricks);
        def("racing", 1, 0, 0xFF6A6A, 0x7A0A0A, 0x121212, CapeArtHd::racing);
        def("moon", 1, 0, 0xE8ECFF, 0x5A6A9A, 0x0A1030, CapeArt2::moon);
    }

    // ================================================================== thunderstorm
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
        field(img, (u, v, x, y) -> {
            double w = fbm(u * 3 + t * 3, v * 4, 3, 0, 3, 40);
            double c1 = fbm(u * 3 + t * 3 + (w - 0.5) * 0.8, v * 4 + (w - 0.5), 3, 0, 5, 41);
            double dens = smooth(0.2, 0.75, c1 + (0.45 - v) * 1.1);
            int sky = ramp(v, 0x1A2234, 0x10161F, 0x07090F);
            int cloud = ramp(c1, 0x0A0D14, 0x1E2638, 0x3E4A66, 0x6A7896);
            int c = mix(sky, cloud, dens);
            double lit = fl * (0.25 + 0.75 * c1) * Math.exp(-Math.hypot(x - bx, y - 40) / 90);
            c = add(c, 0xBFD8FF, lit * 0.9 + fl * 0.08);
            double hill = 0.9 + 0.03 * Math.sin(u * 8 + 1) + 0.015 * Math.sin(u * 23);
            if (v > hill) c = add(mix(0x05070B, 0x020304, (v - hill) * 8), 0x8AA8FF, fl * 0.12);
            return c;
        });
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
        }
    }

    // ================================================================== liquid rainbow
    static void rainbow(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double hueOff = (double) k / n;
        field(img, (u, v, x, y) -> {
            double w = 0.07 * Math.sin(v * Math.PI * 3 + ph) + 0.05 * Math.sin(u * Math.PI * 2 + v * Math.PI - ph);
            double hue = u * 0.22 + v * 0.62 + w + hueOff;
            double f = Math.sin((v * 2.6 + u * 1.1 + w * 2.2) * Math.PI * 2);
            double shadeV = 0.86 + 0.14 * f;
            int c = hsv(hue, 0.6, 0.97 * shadeV);
            c = add(c, 0xFFFFFF, Math.pow(Math.max(0, f), 14) * 0.4);
            return mix(c, 0xFFFFFF, 0.08);
        });
        // central sparkle emblem
        double cx = 80, cy = 112;
        addDot(img, cx, cy, 48, 0xFFFFFF, 0.45);
        Path2D st = new Path2D.Double();
        double R = 34, r = 7;
        for (int i = 0; i < 8; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 4, rr = i % 2 == 0 ? R : r;
            if (i == 0) st.moveTo(cx + Math.cos(a) * rr, cy + Math.sin(a) * rr); else st.quadTo(cx + Math.cos(a - Math.PI / 8) * r * 0.6, cy + Math.sin(a - Math.PI / 8) * r * 0.6, cx + Math.cos(a) * rr, cy + Math.sin(a) * rr);
        }
        st.quadTo(cx + Math.cos(-Math.PI / 2 - Math.PI / 8) * r * 0.6, cy + Math.sin(-Math.PI / 2 - Math.PI / 8) * r * 0.6, cx, cy - R);
        g.setColor(col(0x6A4AA8, 0.25));
        g.fill(AffineTransform.getTranslateInstance(2, 3).createTransformedShape(st));
        g.setPaint(new RadialGradientPaint((float) cx, (float) cy, (float) R, new float[]{0f, 0.5f, 1f}, new Color[]{Color.WHITE, new Color(0xFFF6FF), new Color(0xE8D8FF)}));
        g.fill(st);
        Random rn = new Random(3);
        for (int i = 0; i < 34; i++) {
            double x = 12 + rn.nextDouble() * 136, y = 12 + rn.nextDouble() * 232;
            double a = Math.pow(Math.max(0, Math.sin(ph * (1 + rn.nextInt(2)) + rn.nextDouble() * 6.28)), 2);
            star(g, x, y, 1 + rn.nextDouble() * 1.8, 0xFFFFFF, a);
        }
    }

    // ================================================================== winter night with snowfall
    private static void pine(Graphics2D g, double x, double base, double h, int col, double snow) {
        double w = h * 0.42;
        g.setColor(new Color(mix(col, 0x000000, 0.3)));
        g.fill(new Rectangle2D.Double(x - h * 0.03, base - h * 0.12, h * 0.06, h * 0.14));
        for (int i = 0; i < 4; i++) {
            double ty = base - h * 0.1 - i * h * 0.22, tw = w * (1 - i * 0.22);
            Path2D p = new Path2D.Double();
            p.moveTo(x, ty - h * 0.34); p.lineTo(x + tw / 2, ty); p.quadTo(x, ty - h * 0.05, x - tw / 2, ty); p.closePath();
            g.setColor(new Color(col));
            g.fill(p);
            Path2D s = new Path2D.Double();
            s.moveTo(x, ty - h * 0.34); s.lineTo(x + tw * 0.28, ty - h * 0.12); s.quadTo(x, ty - h * 0.2, x - tw * 0.28, ty - h * 0.12); s.closePath();
            g.setColor(col(0xEAF4FF, snow));
            g.fill(s);
        }
    }

    static void snow(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        field(img, (u, v, x, y) -> {
            int c = ramp(v / 0.75, 0x06102A, 0x0E234E, 0x26477C, 0x5E7EB2);
            c = add(c, 0xCFE0FF, Math.exp(-Math.hypot(x - 116, y - 50) / 22) * 0.45);
            double h1 = 0.62 + 0.04 * Math.sin(u * 5 + 1) + 0.02 * Math.sin(u * 13);
            if (v > h1) c = mix(0x9CB6DC, 0x5E7AA8, (v - h1) * 5);
            double h2 = 0.82 + 0.025 * Math.sin(u * 4 + 2);
            if (v > h2) {
                c = mix(0xF0F6FF, 0xA8BEE0, (v - h2) * 4);
                if (hash(x, y, 7) > 0.985) c = add(c, 0xFFFFFF, 0.5);
            }
            return c;
        });
        // moon
        g.setPaint(new RadialGradientPaint(112, 46, 16, new float[]{0f, 1f}, new Color[]{new Color(0xFFFDF0), new Color(0xE8E4D0)}));
        g.fill(new Ellipse2D.Double(104, 38, 24, 24));
        g.setColor(col(0xB8B4A0, 0.35));
        g.fill(new Ellipse2D.Double(110, 44, 6, 5));
        g.fill(new Ellipse2D.Double(118, 52, 4, 4));
        Random rn = new Random(6);
        for (int i = 0; i < 40; i++) star(g, rn.nextDouble() * FW, rn.nextDouble() * FH * 0.5, 0.5 + rn.nextDouble(), 0xDDE8FF, 0.3 + 0.5 * (0.5 + 0.5 * Math.sin(ph + i)));
        for (int i = 0; i < 9; i++) {
            double x = 8 + i * 18 + rn.nextDouble() * 6;
            double base = FH * (0.64 + 0.04 * Math.sin(x / FW * 5 + 1)) + 2;
            pine(g, x, base, 18 + rn.nextDouble() * 10, 0x1A3252, 0.55);
        }
        double[] big = {18, 52, 132, 150};
        for (double x : big) pine(g, x, FH * 0.86, 52 + rn.nextDouble() * 20, 0x0C1C30, 0.9);
        // snowfall: 3 parallax layers
        int[][] layers = {{50, 1}, {34, 1}, {14, 2}};
        double[][] rr = {{0.8, 1.4}, {1.6, 2.4}, {3.0, 4.2}};
        for (int L = 0; L < 3; L++)
            for (int i = 0; i < layers[L][0]; i++) {
                double x0 = rn.nextDouble() * FW, y0 = rn.nextDouble();
                double s = rn.nextDouble() * 6.28;
                double y = ((y0 + layers[L][1] * t) % 1) * (FH + 12) - 6;
                double x = x0 + Math.sin(ph + s) * (4 + L * 2);
                double r = rr[L][0] + rn.nextDouble() * (rr[L][1] - rr[L][0]);
                dot(g, x, y, r * 1.6, 0xFFFFFF, L == 2 ? 0.7 : 0.9);
            }
    }

    // ================================================================== forest with fireflies
    static void fireflies(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        field(img, (u, v, x, y) -> {
            int c = ramp(v, 0x020A0A, 0x06201C, 0x0E3A2E, 0x0A2A1E);
            c = add(c, 0x9AE8D0, Math.exp(-Math.hypot(u - 0.5, (v - 0.12) * 0.8) / 0.18) * 0.28);
            double fog = fbm(u * 3 + t * 3, v * 5, 3, 0, 4, 55) * Math.exp(-Math.pow((v - 0.72) / 0.12, 2));
            return add(c, 0x6AA89A, fog * 0.35);
        });
        Random rn = new Random(15);
        int[] lc = {0x0F3A32, 0x082620, 0x031210};
        for (int L = 0; L < 3; L++) {
            int cnt = 5 - L;
            for (int i = 0; i < cnt; i++) {
                double x = rn.nextDouble() * FW, w = 5 + L * 5 + rn.nextDouble() * 4;
                g.setPaint(new GradientPaint((float) (x - w), 0, new Color(mix(lc[L], 0x000000, 0.2)), (float) (x + w), 0, new Color(mix(lc[L], 0x3A6A5A, 0.25))));
                Path2D tr = new Path2D.Double();
                tr.moveTo(x - w / 2, 0); tr.lineTo(x + w / 2, 0); tr.lineTo(x + w * 0.7, FH); tr.lineTo(x - w * 0.7, FH); tr.closePath();
                g.fill(tr);
            }
            g.setColor(new Color(lc[L]));
            for (int i = 0; i < 16; i++) {
                double x = rn.nextDouble() * FW, y = -10 + rn.nextDouble() * (30 + L * 10), r = 14 + rn.nextDouble() * 14;
                g.fill(new Ellipse2D.Double(x - r, y - r * 0.7, r * 2, r * 1.4));
            }
        }
        // grass
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 140; i++) {
            double x = rn.nextDouble() * FW, h = 10 + rn.nextDouble() * 22, bend = rn.nextGaussian() * 5 + Math.sin(ph + x * 0.05) * 1.5;
            g.setColor(new Color(mix(0x031008, 0x1E4A2A, rn.nextDouble() * 0.6)));
            g.draw(new java.awt.geom.QuadCurve2D.Double(x, FH, x + bend * 0.3, FH - h * 0.6, x + bend, FH - h));
        }
        for (int i = 0; i < 26; i++) {
            double x0 = 12 + rn.nextDouble() * 136, y0 = 50 + rn.nextDouble() * 190;
            int j = 1 + rn.nextInt(2);
            double s1 = rn.nextDouble() * 6.28, s2 = rn.nextDouble() * 6.28, s3 = rn.nextDouble() * 6.28;
            double x = x0 + 8 * Math.cos(ph * j + s1) + 3 * Math.sin(2 * ph + s2), y = y0 + 6 * Math.sin(ph * j + s3);
            double blink = Math.pow(0.5 + 0.5 * Math.sin(ph * (1 + rn.nextInt(2)) + s2), 2.2);
            addDot(img, x, y, 10, 0xB8FF4A, 0.45 * blink + 0.04);
            addDot(img, x, y, 2.2, 0xFFFFD0, 0.95 * blink + 0.1);
        }
    }

    // ================================================================== heartbeat
    private static double beat(double t) {
        double b = 0;
        for (int w = -1; w <= 1; w++) {
            double tt = t + w;
            b += Math.exp(-Math.pow((tt - 0.08) / 0.05, 2)) + 0.6 * Math.exp(-Math.pow((tt - 0.3) / 0.06, 2));
        }
        return b;
    }

    static Path2D heartPath(double cx, double cy, double s) {
        Path2D h = new Path2D.Double();
        h.moveTo(cx, cy + s * 0.42);
        h.curveTo(cx - s * 0.08, cy + s * 0.33, cx - s * 0.5, cy + s * 0.05, cx - s * 0.5, cy - s * 0.18);
        h.curveTo(cx - s * 0.5, cy - s * 0.4, cx - s * 0.28, cy - s * 0.5, cx - s * 0.16, cy - s * 0.5);
        h.curveTo(cx - s * 0.06, cy - s * 0.5, cx, cy - s * 0.42, cx, cy - s * 0.32);
        h.curveTo(cx, cy - s * 0.42, cx + s * 0.06, cy - s * 0.5, cx + s * 0.16, cy - s * 0.5);
        h.curveTo(cx + s * 0.28, cy - s * 0.5, cx + s * 0.5, cy - s * 0.4, cx + s * 0.5, cy - s * 0.18);
        h.curveTo(cx + s * 0.5, cy + s * 0.05, cx + s * 0.08, cy + s * 0.33, cx, cy + s * 0.42);
        h.closePath();
        return h;
    }

    static void heart(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n, b = beat(t);
        field(img, (u, v, x, y) -> {
            double d = Math.hypot(x - 80, (y - 104) * 0.8);
            int c = mix(0xA0103A, 0x2A020E, d / 150);
            return add(c, 0xFF4A7A, Math.exp(-(d / 55) * (d / 55)) * 0.3 * b);
        });
        Random rn = new Random(8);
        for (int i = 0; i < 18; i++) {
            double x = rn.nextDouble() * FW, y0 = rn.nextDouble();
            double y = (((y0 - t) % 1 + 1) % 1) * (FH + 20) - 10;
            double s = 7 + rn.nextDouble() * 8;
            g.setColor(col(0xFF7A9A, 0.12 + 0.18 * rn.nextDouble()));
            g.fill(heartPath(x + Math.sin(ph + i) * 4, y, s));
        }
        double s = 84 * (1 + 0.1 * b), cx = 80, cy = 106;
        addDot(img, cx, cy, 58 * (1 + 0.1 * b), 0xFF2A5A, 0.25 + 0.35 * b);
        Path2D h = heartPath(cx, cy, s);
        g.setColor(col(0x2A0008, 0.45));
        g.fill(heartPath(cx + 2, cy + 4, s));
        g.setPaint(new RadialGradientPaint((float) (cx - s * 0.18), (float) (cy - s * 0.2), (float) (s * 0.75), new float[]{0f, 0.45f, 1f},
                new Color[]{new Color(0xFF9AB0), new Color(0xF0204E), new Color(0x8A0826)}));
        g.fill(h);
        g.setColor(col(0x5A0418, 0.6));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(h);
        AffineTransform o = g.getTransform();
        g.translate(cx - s * 0.24, cy - s * 0.26);
        g.rotate(-0.6);
        g.setPaint(new GradientPaint(0, (float) (-s * 0.08), col(0xFFFFFF, 0.75), 0, (float) (s * 0.08), col(0xFFFFFF, 0)));
        g.fill(new Ellipse2D.Double(-s * 0.13, -s * 0.07, s * 0.26, s * 0.14));
        g.setTransform(o);
        // ECG trace
        double ey = 204, head = t * (FW + 40) - 20;
        double prevX = 0, prevY = ey;
        for (int x = 0; x <= FW; x += 2) {
            double ph2 = ((x % 80) + 80) % 80 / 80.0;
            double yv = ey;
            if (ph2 > 0.30 && ph2 < 0.36) yv -= Math.sin((ph2 - 0.3) / 0.06 * Math.PI) * 4;
            else if (ph2 > 0.42 && ph2 < 0.46) yv += (ph2 - 0.42) / 0.04 * 5;
            else if (ph2 >= 0.46 && ph2 < 0.5) yv = ey + 5 - (ph2 - 0.46) / 0.04 * 32;
            else if (ph2 >= 0.5 && ph2 < 0.55) yv = ey - 27 + (ph2 - 0.5) / 0.05 * 35;
            else if (ph2 >= 0.55 && ph2 < 0.58) yv = ey + 8 - (ph2 - 0.55) / 0.03 * 8;
            else if (ph2 > 0.66 && ph2 < 0.78) yv -= Math.sin((ph2 - 0.66) / 0.12 * Math.PI) * 6;
            double behind = head - x;
            if (behind < 0) behind += FW + 40;
            double a = Math.exp(-behind / 70);
            if (x > 0) {
                java.awt.geom.Line2D ln = new java.awt.geom.Line2D.Double(prevX, prevY, x, yv);
                g.setColor(col(0xFF6A8A, a * 0.35));
                g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(ln);
                g.setColor(col(0xFFE0E8, a));
                g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(ln);
            }
            if (Math.abs(x - head) < 1.5) addDot(img, x, yv, 7, 0xFFFFFF, 0.7);
            prevX = x; prevY = yv;
        }
    }

    // ================================================================== enchanted
    static void enchant(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        field(img, (u, v, x, y) -> {
            int c = ramp(v, 0x2E1262, 0x1E0A46, 0x0E052A);
            c = shade(c, 0.88 + 0.24 * fbm(u * 10, v * 16, 0, 0, 4, 12));
            return add(c, 0x8A4AFF, Math.exp(-Math.hypot(u - 0.5, (v - 0.42) * 0.7) / 0.2) * 0.3);
        });
        double cx = 80, cy = 108, R = 54;
        int vio = 0xC88AFF;
        glowStroke(g, new Ellipse2D.Double(cx - R, cy - R, R * 2, R * 2), vio, 1.6f, 1.6f, 0.9);
        glowStroke(g, new Ellipse2D.Double(cx - R + 11, cy - R + 11, (R - 11) * 2, (R - 11) * 2), vio, 1.2f, 1.2f, 0.8);
        Random rn = new Random(77);
        int runes = 16;
        for (int i = 0; i < runes; i++) {
            double a = Math.PI * 2 * i / runes;
            double br = 0.45 + 0.55 * Math.pow(0.5 + 0.5 * Math.sin(ph - a * 2), 2);
            AffineTransform o = g.getTransform();
            g.translate(cx + Math.cos(a) * (R - 5.5), cy + Math.sin(a) * (R - 5.5));
            g.rotate(a + Math.PI / 2);
            Path2D r = new Path2D.Double();
            for (int s = 0; s < 3; s++) {
                double x1 = rn.nextInt(3) * 2.2 - 2.2, y1 = rn.nextInt(3) * 2.4 - 2.4, x2 = rn.nextInt(3) * 2.2 - 2.2, y2 = rn.nextInt(3) * 2.4 - 2.4;
                if (x1 == x2 && y1 == y2) y2 += 2.4;
                r.moveTo(x1, y1); r.lineTo(x2, y2);
            }
            glowStroke(g, r, 0xE8C8FF, 0.9f, 0.8f, br);
            g.setTransform(o);
        }
        // hexagram (6-fold symmetric -> rotation by 60 deg per loop is seamless)
        double al = t * Math.PI / 3;
        for (int tri = 0; tri < 2; tri++) {
            Path2D p = new Path2D.Double();
            for (int i = 0; i < 3; i++) {
                double a = al + tri * Math.PI / 3 + i * Math.PI * 2 / 3 - Math.PI / 2;
                double px = cx + Math.cos(a) * (R - 12), py = cy + Math.sin(a) * (R - 12);
                if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
            }
            p.closePath();
            glowStroke(g, p, 0xA86AFF, 1.1f, 1.3f, 0.75);
        }
        double r2 = R * 0.32;
        for (int i = 0; i < 12; i++) {
            double a = -t * Math.PI / 6 + i * Math.PI / 6;
            dot(g, cx + Math.cos(a) * r2, cy + Math.sin(a) * r2, 2.6, 0xF0D8FF, 0.9);
        }
        addDot(img, cx, cy, 16, 0xC88AFF, 0.7 + 0.3 * Math.sin(ph));
        addDot(img, cx, cy, 5, 0xFFFFFF, 0.9);
        for (int i = 0; i < 20; i++) {
            double x = rn.nextDouble() * FW, y0 = rn.nextDouble();
            double y = ((y0 - t) % 1 + 1) % 1 * FH;
            addDot(img, x + Math.sin(ph + i) * 3, y, 3, 0xD8A8FF, 0.6 * Math.sin(Math.PI * y / FH));
        }
        // enchantment glint stripes
        int[] p = px(img);
        for (int y = 0; y < FH; y++)
            for (int x = 0; x < FW; x++) {
                double u = (x + 0.5) / FW, v = (y + 0.5) / FH;
                double gl = Math.pow(0.5 + 0.5 * Math.sin((u * 1.1 + v * 0.55) * Math.PI * 2 * 2.5 - ph), 7) * 0.32;
                p[y * FW + x] = add(p[y * FW + x], 0xA05AFF, gl);
            }
    }

    // ================================================================== black hole
    static void blackHole(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n, cx = 80, cy = 112, Rh = 21;
        field(img, (u, v, x, y) -> {
            double dx = x + 0.5 - cx, dy = y + 0.5 - cy, rs = Math.hypot(dx, dy);
            // lensed star field
            double lens = rs > 1 ? (Rh * Rh * 1.4) / (rs * rs) : 0;
            int sx = (int) Math.round(x + dx * lens), sy = (int) Math.round(y + dy * lens);
            int c = mix(0x04020A, 0x0A0418, v);
            c = add(c, 0x5A2A9A, Math.pow(fbm(sx / 50.0, sy / 50.0, 0, 0, 4, 3), 3) * 0.6);
            double sh = hash(sx >> 1, sy >> 1, 17);
            if (sh > 0.985) c = add(c, 0xFFFFFF, (sh - 0.985) * 60);
            c = add(c, 0xFF8A3A, Math.exp(-rs / 55) * 0.2);
            double ddy = dy / 0.26, r = Math.hypot(dx, ddy), th = Math.atan2(ddy, dx);
            double prof = smooth(Rh + 3, Rh + 10, r) * (1 - smooth(Rh + 18, 74, r));
            double nz = fbm(r * 0.09, (th / (Math.PI * 2)) * 16 + t * 32, 0, 16, 4, 29);
            double dop = 0.8 + 0.4 * (-Math.cos(th));
            double heat = prof * (0.45 + 0.8 * nz) * dop * (1.25 - (r - Rh) / 70);
            int dc = ramp(heat, 0x000000, 0x6A1000, 0xE8480C, 0xFFA848, 0xFFF4D8);
            boolean front = dy > 0;
            if (!front && rs > Rh) c = add(c, dc, clamp01(heat * 1.4));
            // lensed arc of the far disk above the shadow
            double arc = Math.exp(-Math.pow((rs - (Rh + 5)) / 3.5, 2)) * (dy < 4 ? 1 : 0.35);
            double nz2 = fbm((Math.atan2(dy, dx) / (Math.PI * 2)) * 16 - t * 32, rs * 0.2, 16, 0, 3, 31);
            c = add(c, ramp(0.6 + 0.4 * nz2, 0x6A1000, 0xFFA848, 0xFFF4D8), arc * (0.5 + 0.5 * nz2));
            if (rs < Rh) c = mix(c, 0x000000, smooth(Rh, Rh - 1.5, rs));
            if (front) c = add(c, dc, clamp01(heat * 1.4));
            c = add(c, 0xFFE8C0, Math.exp(-Math.pow((rs - Rh - 0.8) / 1.1, 2)) * 0.9);
            return c;
        });
    }

    // ================================================================== royal velvet
    static void royal(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        field(img, (u, v, x, y) -> {
            double nn = fbm(u * 3, v * 2.5, 0, 0, 4, 90);
            int c = mix(0x8A0C22, 0x3A020C, v * 0.7 + 0.3 * (1 - nn));
            double sheen = Math.pow(0.5 + 0.5 * Math.sin(u * Math.PI * 5 + nn * 3), 3) * 0.12;
            return add(c, 0xFF6A7A, sheen);
        });
        // damask
        g.setColor(col(0xFFD27A, 0.18));
        for (int row = 0; row < 7; row++)
            for (int col = -1; col < 5; col++) {
                double x = col * 40 + (row % 2) * 20 + 20, y = 60 + row * 34;
                fleur(g, x, y, 9);
            }
        // ermine collar
        for (int y = 12; y < 44; y++)
            for (int x = 0; x < FW; x++) {
                double edge = 40 + 3 * Math.sin(x * 0.4) + 2 * hash(x, 3, 5);
                if (y > edge) continue;
                double f = 0.9 + 0.1 * hash(x, y, 8) + 0.05 * Math.sin(x * 0.9 + y * 0.3);
                px(img)[y * FW + x] = 0xFF000000 | shade(mix(0xFFFFFF, 0xD8D4CC, (y - 12) / 32.0), f);
            }
        for (int i = 0; i < 7; i++) {
            double x = 14 + i * 22, y = 24 + (i % 2) * 8;
            Path2D tail = new Path2D.Double();
            tail.moveTo(x, y - 4); tail.quadTo(x + 3, y + 2, x, y + 6); tail.quadTo(x - 3, y + 2, x, y - 4);
            g.setColor(new Color(0x111111));
            g.fill(tail);
        }
        // crest: crown + shield
        double cx = 80, cy = 136;
        Path2D sh = new Path2D.Double();
        sh.moveTo(cx - 28, cy - 30); sh.lineTo(cx + 28, cy - 30); sh.lineTo(cx + 28, cy + 2); sh.curveTo(cx + 28, cy + 22, cx + 10, cy + 32, cx, cy + 40);
        sh.curveTo(cx - 10, cy + 32, cx - 28, cy + 22, cx - 28, cy + 2); sh.closePath();
        g.setColor(col(0x1A0004, 0.5));
        g.fill(AffineTransform.getTranslateInstance(2, 4).createTransformedShape(sh));
        g.setPaint(gold(cx, cy - 30, cy + 40));
        g.fill(sh);
        java.awt.Shape inner = AffineTransform.getTranslateInstance(cx, cy + 4).createTransformedShape(AffineTransform.getScaleInstance(0.82, 0.82).createTransformedShape(AffineTransform.getTranslateInstance(-cx, -cy - 4).createTransformedShape(sh)));
        g.setPaint(new GradientPaint(0, (float) cy - 26, new Color(0x2A3A9A), 0, (float) cy + 36, new Color(0x0E1450)));
        g.fill(inner);
        g.setFont(dev.lego.ui.Fonts.get(dev.lego.ui.Fonts.BOLD, 34));
        float w = g.getFontMetrics().stringWidth("L");
        g.setPaint(gold(cx, cy - 20, cy + 14));
        g.drawString("L", (float) (cx - w / 2), (float) cy + 14);
        // crown
        Path2D cr = new Path2D.Double();
        double by = cy - 36;
        cr.moveTo(cx - 24, by); cr.lineTo(cx - 28, by - 22); cr.lineTo(cx - 13, by - 10); cr.lineTo(cx, by - 28); cr.lineTo(cx + 13, by - 10); cr.lineTo(cx + 28, by - 22); cr.lineTo(cx + 24, by); cr.closePath();
        g.setPaint(gold(cx, by - 28, by));
        g.fill(cr);
        g.setColor(col(0x5A3A00, 0.6));
        g.setStroke(new BasicStroke(1f));
        g.draw(cr);
        int[] gems = {0xE0115F, 0x1E90FF, 0xE0115F};
        double[][] gp = {{cx - 28, by - 22}, {cx, by - 28}, {cx + 28, by - 22}};
        for (int i = 0; i < 3; i++) {
            g.setPaint(new RadialGradientPaint((float) gp[i][0] - 1, (float) gp[i][1] - 1, 4, new float[]{0f, 1f}, new Color[]{Color.WHITE, new Color(gems[i])}));
            g.fill(new Ellipse2D.Double(gp[i][0] - 3.5, gp[i][1] - 3.5, 7, 7));
        }
        g.setColor(new Color(0x1E90FF));
        g.fill(new Ellipse2D.Double(cx - 3, by - 7, 6, 5));
    }

    static LinearGradientPaint gold(double cx, double y0, double y1) {
        return new LinearGradientPaint((float) cx - 10, (float) y0, (float) cx + 10, (float) y1, new float[]{0f, 0.4f, 0.55f, 1f},
                new Color[]{new Color(0xFFF4B8), new Color(0xE8B92C), new Color(0xB07A12), new Color(0xFFD86A)});
    }

    private static void fleur(Graphics2D g, double x, double y, double s) {
        Path2D p = new Path2D.Double();
        p.moveTo(x, y - s * 1.6); p.curveTo(x + s * 0.6, y - s * 0.8, x + s * 0.4, y - s * 0.2, x, y + s * 0.2); p.curveTo(x - s * 0.4, y - s * 0.2, x - s * 0.6, y - s * 0.8, x, y - s * 1.6);
        p.moveTo(x + s * 0.2, y); p.curveTo(x + s * 1.6, y - s * 1.2, x + s * 1.8, y + s * 0.2, x + s * 0.9, y + s * 0.3); p.curveTo(x + s * 1.1, y - s * 0.3, x + s * 0.7, y - s * 0.4, x + s * 0.2, y);
        p.moveTo(x - s * 0.2, y); p.curveTo(x - s * 1.6, y - s * 1.2, x - s * 1.8, y + s * 0.2, x - s * 0.9, y + s * 0.3); p.curveTo(x - s * 1.1, y - s * 0.3, x - s * 0.7, y - s * 0.4, x - s * 0.2, y);
        g.fill(p);
        g.fill(new RoundRectangle2D.Double(x - s * 0.8, y + s * 0.15, s * 1.6, s * 0.35, 2, 2));
        p = new Path2D.Double();
        p.moveTo(x - s * 0.25, y + s * 0.5); p.lineTo(x + s * 0.25, y + s * 0.5); p.lineTo(x, y + s * 1.3); p.closePath();
        g.fill(p);
    }

    // ================================================================== dragon scales
    static void scales(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double SW = 22, SH = 12, R = 14;
        field(img, (u, v, x, y) -> {
            int r0 = (int) Math.floor((y - R) / SH) - 1;
            for (int r = r0; r <= r0 + 3; r++) {
                double cyr = r * SH, off = (Math.floorMod(r, 2)) * SW / 2;
                int ci = (int) Math.floor((x - off) / SW + 0.5);
                for (int cc = ci - 1; cc <= ci + 1; cc++) {
                    double cxs = cc * SW + off;
                    double dx = x + 0.5 - cxs, dy = y + 0.5 - cyr;
                    if (dy < -2) continue;
                    double d = Math.hypot(dx, dy) / R;
                    if (d > 1) continue;
                    double h = hash(cc, r, 4);
                    int base = ramp(clamp01(0.5 + dx / (R * 2.2) + (h - 0.5) * 0.3 + v * 0.3), 0x0E6A48, 0x16A07A, 0x1AB8A0, 0x0A4A48);
                    double dome = 1 - d * d * 0.3 + (dy / R) * 0.12;
                    int c = shade(base, dome * (1.05 - v * 0.35));
                    c = add(c, 0xE8FFE0, Math.exp(-Math.pow((dx + R * 0.2) / 5, 2) - Math.pow((dy - R * 0.45) / 3, 2)) * 0.2);
                    if (d > 0.84) c = mix(c, 0x9AE8B0, (d - 0.84) / 0.16 * 0.45);
                    if (d > 0.93) c = shade(c, 0.55);
                    return c;
                }
            }
            return 0x052018;
        });
    }

    // ================================================================== woodland camo
    static void camo(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        field(img, (u, v, x, y) -> {
            double w = fbm(u * 3, v * 5, 0, 0, 3, 70);
            double a = fbm(u * 3.2 + w, v * 5 + w, 0, 0, 4, 71), b = fbm(u * 4 + 5 + w * 0.6, v * 6, 0, 0, 4, 72);
            int c = 0x5A6A36;
            if (a < 0.44) c = 0x2E3B1F;
            if (b > 0.58) c = 0x8A7A4E;
            if (a > 0.6 && b < 0.5) c = 0x1C1C14;
            double weave = ((x + y) % 4 < 2 ? 0.97 : 1.03);
            return shade(c, weave);
        });
        double cx = 80, cy = 70;
        g.setColor(col(0x000000, 0.35));
        g.fill(new RoundRectangle2D.Double(cx - 22, cy - 16 + 2, 44, 34, 8, 8));
        g.setColor(new Color(0x3A4424));
        g.fill(new RoundRectangle2D.Double(cx - 22, cy - 16, 44, 32, 8, 8));
        g.setColor(new Color(0xC8C090));
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[]{2f, 2f}, 0));
        g.draw(new RoundRectangle2D.Double(cx - 19, cy - 13, 38, 26, 6, 6));
        Path2D st = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 5, r = i % 2 == 0 ? 10 : 4;
            if (i == 0) st.moveTo(cx + Math.cos(a) * r, cy + Math.sin(a) * r); else st.lineTo(cx + Math.cos(a) * r, cy + Math.sin(a) * r);
        }
        st.closePath();
        g.setColor(new Color(0xD8D0A0));
        g.fill(st);
    }

    // ================================================================== bricks (top view)
    static void bricks(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        field(img, (u, v, x, y) -> 0x151518);
        int[] cols = {0xE3000B, 0xFFCD03, 0x006CB7, 0x00852B, 0xF57D20, 0xFFFFFF, 0xA0BCE8, 0xE870A0};
        Random rn = new Random(4);
        double pitch = 16;
        for (int row = 0; row < 16; row++) {
            double y = row * pitch * 2 - 4 + 0;
            double x = -(row % 2) * pitch * 2;
            while (x < FW) {
                int len = 2 + rn.nextInt(3);
                int c = cols[rn.nextInt(cols.length)];
                brick(g, x, y, len * pitch, pitch * 2, c, pitch);
                x += len * pitch;
            }
        }
    }

    private static void brick(Graphics2D g, double x, double y, double w, double h, int c, double pitch) {
        Color base = new Color(c);
        g.setPaint(new GradientPaint((float) x, (float) y, new Color(mix(c, 0xFFFFFF, 0.2)), (float) (x + w), (float) (y + h), new Color(mix(c, 0x000000, 0.25))));
        g.fill(new RoundRectangle2D.Double(x + 0.8, y + 0.8, w - 1.6, h - 1.6, 3, 3));
        g.setColor(col(mix(c, 0x000000, 0.5), 0.8));
        g.setStroke(new BasicStroke(1f));
        g.draw(new RoundRectangle2D.Double(x + 0.8, y + 0.8, w - 1.6, h - 1.6, 3, 3));
        for (double sx = x + pitch / 2; sx < x + w; sx += pitch)
            for (double sy = y + pitch / 2; sy < y + h; sy += pitch) {
                double r = pitch * 0.31;
                g.setColor(col(0x000000, 0.35));
                g.fill(new Ellipse2D.Double(sx - r + 1.2, sy - r + 1.6, r * 2, r * 2));
                g.setPaint(new GradientPaint((float) (sx - r), (float) (sy - r), new Color(mix(c, 0xFFFFFF, 0.35)), (float) (sx + r), (float) (sy + r), new Color(mix(c, 0x000000, 0.15))));
                g.fill(new Ellipse2D.Double(sx - r, sy - r, r * 2, r * 2));
                g.setColor(col(0xFFFFFF, 0.3));
                g.draw(new java.awt.geom.Arc2D.Double(sx - r * 0.7, sy - r * 0.7, r * 1.4, r * 1.4, 100, 80, java.awt.geom.Arc2D.OPEN));
            }
    }

    // ================================================================== checkered racing flag
    static void racing(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        field(img, (u, v, x, y) -> {
            double wave = Math.sin(v * Math.PI * 2 * 1.2 + u * 5);
            double uu = u + 0.035 * wave, vv = v + 0.03 * Math.sin(u * Math.PI * 2 * 1.4 + v * 3);
            int cu = (int) Math.floor(uu * 6), cv = (int) Math.floor(vv * 9.6);
            int c = ((cu + cv) & 1) == 0 ? 0xF2F2F2 : 0x141414;
            double sh = 0.84 + 0.2 * Math.cos(v * Math.PI * 2 * 1.2 + u * 5);
            return add(shade(c, sh), 0xFFFFFF, Math.pow(Math.max(0, Math.cos(v * Math.PI * 2 * 1.2 + u * 5 - 0.6)), 10) * 0.12);
        });
        g.setPaint(new GradientPaint(0, 0, new Color(0xFF2A2A), FW, 0, new Color(0xB00A0A)));
        Path2D s = new Path2D.Double();
        s.moveTo(0, 200); s.lineTo(FW, 150); s.lineTo(FW, 168); s.lineTo(0, 218); s.closePath();
        g.setColor(col(0x000000, 0.35));
        g.fill(AffineTransform.getTranslateInstance(0, 3).createTransformedShape(s));
        g.setPaint(new GradientPaint(0, 0, new Color(0xFF3A3A), FW, 0, new Color(0xB00A0A)));
        g.fill(s);
    }

    // ================================================================== moonlit night
    static void moon(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double mx = 80, my = 84, mr = 34;
        field(img, (u, v, x, y) -> {
            int c = ramp(v, 0x050A24, 0x0E1A48, 0x2A2A6A, 0x4A3A7A);
            double d = Math.hypot(x - mx, y - my);
            c = add(c, 0xC8D4FF, Math.exp(-Math.max(0, d - mr) / 20) * 0.35);
            if (d < mr + 1) {
                double nn = fbm(x / 14.0, y / 14.0, 0, 0, 5, 9);
                int mc = mix(0xFFFBEA, 0xC8C4B4, smooth(0.45, 0.7, nn));
                double limb = Math.sqrt(Math.max(0, 1 - (d / mr) * (d / mr)));
                mc = shade(mc, 0.72 + 0.3 * limb);
                c = mix(c, mc, smooth(mr + 0.8, mr - 0.8, d));
            }
            double cl = fbm(u * 2.2, v * 7, 0, 0, 5, 19);
            double band = Math.exp(-Math.pow((v - 0.44) / 0.07, 2)) + Math.exp(-Math.pow((v - 0.66) / 0.09, 2));
            double ca = smooth(0.45, 0.62, cl * band + band * 0.15);
            int ccol = mix(0x3A4A8A, 0xB8C4F0, smooth(0.5, 0.8, cl) * (0.5 + 0.5 * Math.exp(-d / 60)));
            c = mix(c, ccol, ca * 0.85);
            return c;
        });
        Random rn = new Random(1);
        for (int i = 0; i < 50; i++) {
            double x = rn.nextDouble() * FW, y = rn.nextDouble() * FH * 0.8;
            if (Math.hypot(x - mx, y - my) < mr + 6) continue;
            star(g, x, y, 0.5 + Math.pow(rn.nextDouble(), 3) * 2, 0xE0E8FF, 0.4 + 0.5 * rn.nextDouble());
        }
        // castle silhouette
        g.setColor(new Color(0x05061A));
        Path2D c = new Path2D.Double();
        c.moveTo(0, FH); c.lineTo(0, 222); c.lineTo(30, 214); c.lineTo(30, 196); c.lineTo(34, 196); c.lineTo(34, 190); c.lineTo(38, 190); c.lineTo(38, 196); c.lineTo(42, 196);
        c.lineTo(42, 176); c.lineTo(40, 176); c.lineTo(48, 160); c.lineTo(56, 176); c.lineTo(54, 176); c.lineTo(54, 200); c.lineTo(84, 200); c.lineTo(84, 186); c.lineTo(88, 186);
        c.lineTo(88, 192); c.lineTo(92, 192); c.lineTo(92, 186); c.lineTo(96, 186); c.lineTo(96, 206); c.lineTo(120, 212); c.lineTo(160, 206); c.lineTo(FW, FH); c.closePath();
        g.fill(c);
        g.setColor(new Color(0xFFD27A));
        g.fill(new Rectangle2D.Double(46.5, 180, 3, 5));
        g.fill(new Rectangle2D.Double(88.5, 196, 2.5, 4));
        addDot(img, 48, 182, 6, 0xFFB84A, 0.35);
    }
}
