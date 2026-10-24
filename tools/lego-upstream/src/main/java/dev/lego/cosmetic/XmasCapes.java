package dev.lego.cosmetic;

import static dev.lego.cosmetic.Capes.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/** Christmas capes (canvas FW x FH = 160 x 256, every animation loops seamlessly). */
final class XmasCapes {
    private XmasCapes() { }

    static void defineAll() {
        def("xm_winterwald", 16, 8, 0xE8F4FF, 0x2A4A7A, 0x0E1A30, XmasCapes::winterForest);
        def("xm_lebkuchenhaus", 16, 8, 0xF6E0C0, 0x7A3A14, 0x2A1A30, XmasCapes::gingerHouse);
        def("xm_zuckerstange", 16, 10, 0xFFFFFF, 0xC8141E, 0xF4F0EC, XmasCapes::candyCane);
        def("xm_schlittenmond", 16, 8, 0xFFE9A8, 0x1A2A5A, 0x0A1024, XmasCapes::sleighMoon);
        def("xm_pullover", 8, 4, 0xFFFFFF, 0x1E6A34, 0x8A1018, XmasCapes::sweater);
        def("xm_nordlicht", 16, 8, 0xB8FFE0, 0x1A3A5A, 0x06101E, XmasCapes::auroraXmas);
    }

    // ================================================================== shared painters
    /** Falling snow that loops seamlessly (each flake falls 1 or 2 canvas heights per loop). */
    static void snow(Graphics2D g, double t, double ph, int count, int seed, double alpha) {
        Random rn = new Random(seed);
        for (int i = 0; i < count; i++) {
            double x0 = rn.nextDouble() * FW, y0 = rn.nextDouble(), sp = 1 + rn.nextInt(2), r = 0.8 + rn.nextDouble() * 1.8 * (sp == 2 ? 1.3 : 0.8);
            double y = ((y0 + sp * t) % 1) * (FH + 10) - 5;
            double x = x0 + Math.sin(ph * sp + i * 1.7) * 5;
            dot(g, x, y, r * 1.8, 0xFFFFFF, alpha * (0.55 + 0.3 * rn.nextDouble()));
            g.setColor(col(0xFFFFFF, alpha * 0.9));
            g.fill(new Ellipse2D.Double(x - r * 0.5, y - r * 0.5, r, r));
        }
    }

    static void stars(Graphics2D g, double ph, int count, int seed, double maxY) {
        Random rn = new Random(seed);
        for (int i = 0; i < count; i++) {
            double x = rn.nextDouble() * FW, y = rn.nextDouble() * maxY, r = 0.7 + rn.nextDouble() * 1.6;
            double tw = 0.55 + 0.45 * Math.sin(ph * (1 + rn.nextInt(2)) + i * 2.3);
            star(g, x, y, r, rn.nextBoolean() ? 0xFFF4D8 : 0xD8E8FF, tw);
        }
    }

    /** Pine tree silhouette with snow on the tiers; base at (x, y), height h. */
    static void pine(Graphics2D g, double x, double y, double h, int body, int snowC, int tiers) {
        g.setColor(new Color(mix(body, 0x000000, 0.3)));
        g.fill(new Rectangle2D.Double(x - h * 0.04, y - h * 0.12, h * 0.08, h * 0.14));
        for (int i = 0; i < tiers; i++) {
            double f = i / (double) tiers;
            double ty = y - h * 0.1 - f * h * 0.78, w = h * (0.42 - f * 0.3), th = h * 0.4;
            Path2D p = new Path2D.Double();
            p.moveTo(x - w, ty);
            p.quadTo(x - w * 0.5, ty + h * 0.03, x, ty + h * 0.012);
            p.quadTo(x + w * 0.5, ty + h * 0.03, x + w, ty);
            p.lineTo(x, ty - th);
            p.closePath();
            g.setPaint(new GradientPaint((float) (x - w), 0, new Color(mix(body, 0xFFFFFF, 0.12)), (float) (x + w), 0, new Color(mix(body, 0x000000, 0.25))));
            g.fill(p);
            // snow on the tier
            Path2D s = new Path2D.Double();
            s.moveTo(x - w * 0.82, ty - th * 0.12);
            s.quadTo(x - w * 0.45, ty - th * 0.02, x - w * 0.2, ty - th * 0.2);
            s.quadTo(x + w * 0.1, ty - th * 0.05, x + w * 0.55, ty - th * 0.16);
            s.lineTo(x + w * 0.3, ty - th * 0.4);
            s.lineTo(x, ty - th * 0.62);
            s.lineTo(x - w * 0.35, ty - th * 0.36);
            s.closePath();
            g.setColor(col(snowC, 0.92));
            g.fill(s);
        }
        g.setColor(col(snowC, 0.95));
        g.fill(new Ellipse2D.Double(x - h * 0.03, y - h * 0.92, h * 0.06, h * 0.05));
    }

    // ================================================================== Winterwald
    static void winterForest(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        field(img, (u, v, x, y) -> {
            int c = ramp(v * 1.4, 0x070C22, 0x14224A, 0x2E4A80, 0x6A8AC0);
            double neb = fbm(u * 3, v * 3, 3, 3, 4, 701);
            return add(c, 0x3A5AA0, neb * 0.18 * (1 - v));
        });
        stars(g, ph, 45, 702, 150);
        // moon
        addDot(img, 116, 52, 34, 0xC8D8FF, 0.35);
        g.setPaint(new RadialGradientPaint(112, 48, 16, new float[]{0f, 1f}, new Color[]{new Color(0xFFFCEC), new Color(0xE0DCC8)}));
        g.fill(new Ellipse2D.Double(102, 38, 28, 28));
        g.setColor(col(0xB8B4A0, 0.5));
        g.fill(new Ellipse2D.Double(110, 44, 6, 5));
        g.fill(new Ellipse2D.Double(119, 53, 4, 4));
        // far hills
        Path2D hills = new Path2D.Double();
        hills.moveTo(0, 170);
        for (int x = 0; x <= FW; x += 8) hills.lineTo(x, 160 - 12 * Math.sin(x * 0.03 + 1) - 6 * Math.sin(x * 0.08));
        hills.lineTo(FW, FH); hills.lineTo(0, FH); hills.closePath();
        g.setPaint(new GradientPaint(0, 140, new Color(0x9AB4DA), 0, 200, new Color(0x5A78A8)));
        g.fill(hills);
        Random rn = new Random(703);
        for (int i = 0; i < 11; i++) pine(g, 6 + i * 15 + rn.nextDouble() * 6, 168 + rn.nextDouble() * 6, 30 + rn.nextDouble() * 14, 0x2A4A6A, 0xB8CCEA, 3);
        // cabin with warm light
        double cx = 58, cy = 196;
        g.setColor(new Color(0x4A2A1A));
        g.fill(new Rectangle2D.Double(cx - 16, cy - 16, 32, 18));
        Path2D roof = new Path2D.Double();
        roof.moveTo(cx - 21, cy - 14); roof.lineTo(cx, cy - 31); roof.lineTo(cx + 21, cy - 14); roof.closePath();
        g.setColor(new Color(0x2A1A14));
        g.fill(roof);
        g.setColor(new Color(0xF4F8FF));
        g.fill(new RoundRectangle2D.Double(cx - 22, cy - 19, 44, 6, 5, 5));
        Path2D rs = new Path2D.Double();
        rs.moveTo(cx - 19, cy - 16); rs.lineTo(cx, cy - 32); rs.lineTo(cx + 19, cy - 16); rs.lineTo(cx + 12, cy - 18); rs.lineTo(cx, cy - 27); rs.lineTo(cx - 12, cy - 18); rs.closePath();
        g.fill(rs);
        double fl = 0.8 + 0.1 * Math.sin(ph * 3) + 0.06 * Math.sin(ph * 7 + 1);
        for (int w = -1; w <= 1; w += 2) {
            g.setColor(new Color(mix(0xFF9A2A, 0xFFE08A, fl)));
            g.fill(new Rectangle2D.Double(cx + w * 8 - 4, cy - 11, 8, 7));
            addDot(img, cx + w * 8, cy - 7, 12, 0xFFA040, 0.35 * fl);
            g.setColor(new Color(0x2A1A14));
            g.fill(new Rectangle2D.Double(cx + w * 8 - 0.6, cy - 11, 1.2, 7));
        }
        // chimney smoke
        g.setColor(new Color(0x2A1A14));
        g.fill(new Rectangle2D.Double(cx + 8, cy - 32, 5, 10));
        for (int i = 0; i < 6; i++) {
            double f = (t * 2 + i / 6.0) % 1;
            dot(g, cx + 10.5 + Math.sin(f * 6 + i) * 3 + f * 10, cy - 34 - f * 40, 4 + f * 7, 0xC8D0E0, 0.35 * (1 - f));
        }
        // snowy ground
        Path2D gr = new Path2D.Double();
        gr.moveTo(0, 204);
        for (int x = 0; x <= FW; x += 8) gr.lineTo(x, 200 + 4 * Math.sin(x * 0.05 + 2));
        gr.lineTo(FW, FH); gr.lineTo(0, FH); gr.closePath();
        g.setPaint(new GradientPaint(0, 196, new Color(0xF4F8FF), 0, FH, new Color(0xB8CCEA)));
        g.fill(gr);
        for (int i = 0; i < 30; i++) {
            double x = rn.nextDouble() * FW, y = 206 + rn.nextDouble() * 44;
            star(g, x, y, 0.8 + rn.nextDouble(), 0xFFFFFF, 0.3 + 0.6 * Math.max(0, Math.sin(ph * 2 + i * 1.9)));
        }
        // near trees
        pine(g, 14, 238, 110, 0x123A24, 0xF4F8FF, 5);
        pine(g, 142, 232, 96, 0x16422A, 0xF4F8FF, 5);
        pine(g, 112, 214, 58, 0x1A4A30, 0xE8F0FF, 4);
        snow(g, t, ph, 110, 704, 0.9);
    }

    // ================================================================== Lebkuchenhaus
    static void gingerHouse(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        field(img, (u, v, x, y) -> ramp(v, 0x140A24, 0x2A1A4A, 0x4A2A5A));
        stars(g, ph, 30, 711, 100);
        double cx = 80, base = 206, w = 96, wh = 70;
        // chimney + smoke
        g.setColor(new Color(0x9A5A2A));
        g.fill(new Rectangle2D.Double(cx + 18, base - wh - 58, 14, 34));
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(cx + 16, base - wh - 62, 18, 7, 6, 6));
        for (int i = 0; i < 6; i++) {
            double f = (t * 2 + i / 6.0) % 1;
            dot(g, cx + 25 + Math.sin(f * 5 + i) * 4 + f * 8, base - wh - 66 - f * 50, 5 + f * 8, 0xE0D8E8, 0.4 * (1 - f));
        }
        // walls
        Rectangle2D wall = new Rectangle2D.Double(cx - w / 2, base - wh, w, wh);
        g.setPaint(new GradientPaint(0, (float) (base - wh), new Color(0xC47A3A), 0, (float) base, new Color(0x8A4A1E)));
        g.fill(wall);
        Random rn = new Random(712);
        for (int i = 0; i < 160; i++) { g.setColor(col(0x5A2A0A, 0.35)); g.fill(new Ellipse2D.Double(cx - w / 2 + rn.nextDouble() * w, base - wh + rn.nextDouble() * wh, 1.6, 1.6)); }
        // icing trim on the wall corners
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new java.awt.geom.Line2D.Double(cx - w / 2 + 2, base - wh + 4, cx - w / 2 + 2, base - 2));
        g.draw(new java.awt.geom.Line2D.Double(cx + w / 2 - 2, base - wh + 4, cx + w / 2 - 2, base - 2));
        // roof
        Path2D roof = new Path2D.Double();
        roof.moveTo(cx - w / 2 - 12, base - wh + 6); roof.lineTo(cx, base - wh - 56); roof.lineTo(cx + w / 2 + 12, base - wh + 6); roof.closePath();
        g.setPaint(new GradientPaint(0, (float) (base - wh - 56), new Color(0xA85A24), 0, (float) (base - wh), new Color(0x7A3A14)));
        g.fill(roof);
        // roof tiles: icing scallops
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int row = 0; row < 5; row++) {
            double y = base - wh - 44 + row * 11, half = (y - (base - wh - 56)) / 62 * (w / 2 + 12);
            for (double x = cx - half + 6; x < cx + half - 4; x += 10) {
                g.setColor(col(0xFFF4E8, 0.85));
                g.draw(new java.awt.geom.Arc2D.Double(x - 5, y - 5, 10, 10, 180, 180, java.awt.geom.Arc2D.OPEN));
            }
        }
        // thick icing along the roof edge with drips
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D edge = new Path2D.Double();
        edge.moveTo(cx - w / 2 - 12, base - wh + 6); edge.lineTo(cx, base - wh - 56); edge.lineTo(cx + w / 2 + 12, base - wh + 6);
        g.draw(edge);
        for (int i = 0; i < 12; i++) {
            double f = (i + 0.5) / 12, side = f < 0.5 ? -1 : 1, ff = f < 0.5 ? f * 2 : (1 - f) * 2;
            double x = cx + side * (1 - ff) * (w / 2 + 12), y = base - wh + 6 - ff * 62;
            double len = 4 + 5 * hash1(i, 713);
            g.fill(new RoundRectangle2D.Double(x - 2, y, 4, len, 4, 4));
            g.fill(new Ellipse2D.Double(x - 2.5, y + len - 2.5, 5, 5));
        }
        // gumdrops on the ridge and roof
        int[] gd = {0xE8323A, 0x2EB84A, 0xFFD23A, 0x3A8AFF, 0xE85AD8};
        for (int i = 0; i < 7; i++) {
            double f = (i + 1) / 8.0;
            double x = cx - (w / 2 + 6) + f * (w + 12), y = base - wh + 2 - (1 - Math.abs(f - 0.5) * 2) * 58 - 4;
            gumdrop(g, x, y, 4.5, gd[i % gd.length]);
        }
        // windows with warm flicker
        double fl = 0.8 + 0.12 * Math.sin(ph * 3) + 0.06 * Math.sin(ph * 5 + 1);
        for (int s = -1; s <= 1; s += 2) {
            double wx = cx + s * 28, wy = base - wh + 14;
            addDot(img, wx, wy + 9, 22, 0xFFA040, 0.4 * fl);
            g.setColor(new Color(mix(0xFF9A2A, 0xFFE9A0, fl)));
            g.fill(new RoundRectangle2D.Double(wx - 10, wy, 20, 18, 4, 4));
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2.4f));
            g.draw(new RoundRectangle2D.Double(wx - 10, wy, 20, 18, 4, 4));
            g.draw(new java.awt.geom.Line2D.Double(wx, wy, wx, wy + 18));
            g.draw(new java.awt.geom.Line2D.Double(wx - 10, wy + 9, wx + 10, wy + 9));
        }
        // door framed by candy canes
        g.setColor(new Color(0x5A2A10));
        g.fill(new RoundRectangle2D.Double(cx - 11, base - 38, 22, 38, 20, 20));
        g.setColor(new Color(0xFFD23A));
        g.fill(new Ellipse2D.Double(cx + 5, base - 20, 4, 4));
        for (int s = -1; s <= 1; s += 2) candyStick(g, cx + s * 15, base, base - 44, s);
        // wreath over the door
        g.setColor(new Color(0x2E8A44));
        g.setStroke(new BasicStroke(3.5f));
        g.draw(new Ellipse2D.Double(cx - 6, base - 34, 12, 12));
        g.setColor(new Color(0xE8323A));
        g.fill(new Ellipse2D.Double(cx - 2, base - 25, 4, 4));
        // snowy ground + candy path
        g.setPaint(new GradientPaint(0, (float) base, new Color(0xF4F4FF), 0, FH, new Color(0xC8CCE8)));
        g.fill(new Rectangle2D.Double(0, base, FW, FH - base));
        for (int i = 0; i < 5; i++) {
            double y = base + 6 + i * 9, x = cx + (i % 2 == 0 ? -3 : 3);
            g.setColor(new Color(i % 2 == 0 ? 0xE8323A : 0x2EB84A));
            g.fill(new Ellipse2D.Double(x - 5 - i, y - 2, 10 + i * 2, 5));
        }
        // lollipops in the snow
        for (int s = -1; s <= 1; s += 2) {
            double x = cx + s * 62, y = base + 16;
            g.setColor(new Color(0xF4F0EC));
            g.fill(new Rectangle2D.Double(x - 1.2, y - 28, 2.4, 30));
            g.setColor(new Color(s < 0 ? 0xE8323A : 0x3A8AFF));
            g.fill(new Ellipse2D.Double(x - 9, y - 44, 18, 18));
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2f));
            g.draw(new java.awt.geom.Arc2D.Double(x - 6, y - 41, 12, 12, 90 + ph * 57.3 * s, 250, java.awt.geom.Arc2D.OPEN));
        }
        snow(g, t, ph, 70, 714, 0.85);
    }

    static void gumdrop(Graphics2D g, double x, double y, double r, int c) {
        g.setPaint(new RadialGradientPaint((float) (x - r * 0.3), (float) (y - r * 0.4), (float) (r * 1.3), new float[]{0f, 1f}, new Color[]{new Color(mix(c, 0xFFFFFF, 0.45)), new Color(mix(c, 0x000000, 0.2))}));
        Path2D p = new Path2D.Double();
        p.moveTo(x - r, y + r * 0.6);
        p.quadTo(x - r, y - r, x, y - r);
        p.quadTo(x + r, y - r, x + r, y + r * 0.6);
        p.closePath();
        g.fill(p);
        g.setColor(col(0xFFFFFF, 0.8));
        for (int i = 0; i < 4; i++) g.fill(new Ellipse2D.Double(x - r * 0.6 + i * r * 0.4, y - r * 0.2 + (i % 2) * r * 0.4, 1.1, 1.1));
    }

    /** Vertical candy stick with a hook at the top (hook opens towards dir). */
    static void candyStick(Graphics2D g, double x, double y0, double y1, int dir) {
        Path2D p = new Path2D.Double();
        p.moveTo(x, y0);
        p.lineTo(x, y1 + 6);
        p.quadTo(x, y1 - 2, x + dir * 5, y1 - 2);
        p.quadTo(x + dir * 10, y1 - 2, x + dir * 10, y1 + 5);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(p);
        g.setColor(new Color(0xE0202A));
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[]{3.5f, 3.5f}, 0));
        g.draw(p);
        g.setStroke(new BasicStroke(1f));
    }

    // ================================================================== Zuckerstange
    static void candyCane(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        final double P = 40;   // stripe period (px along the diagonal)
        field(img, (u, v, x, y) -> {
            double d = (x + y * 0.8) / P - t;
            double s = d - Math.floor(d);
            int c = s < 0.38 ? 0xD8141E : (s > 0.46 && s < 0.5 ? 0xE85A5A : (s > 0.66 && s < 0.7 ? 0x2EA84A : 0xFBF6F0));
            double edge = Math.min(Math.min(Math.abs(s), Math.abs(s - 0.38)), Math.abs(1 - s));
            if (edge < 0.012) c = mix(c, 0xF0A0A0, 0.5);
            // glossy cylinder-like shading across the cape
            double sh = 0.86 + 0.18 * Math.sin(u * Math.PI) + 0.25 * Math.exp(-Math.pow((u - 0.32) / 0.06, 2));
            return shade(c, sh);
        });
        sweep(img, t, 0.07, 0.35);
        // emblem: wreath with a bow in the middle
        double cx = FW / 2.0, cy = 108;
        for (int i = 0; i < 26; i++) {
            double a = Math.PI * 2 * i / 26, r = 30 + (i % 2) * 3;
            double x = cx + Math.cos(a) * r, y = cy + Math.sin(a) * r;
            g.setPaint(new RadialGradientPaint((float) x - 2, (float) y - 2, 11, new float[]{0f, 1f}, new Color[]{new Color(0x4AB860), new Color(0x1A5A2A)}));
            g.fill(new Ellipse2D.Double(x - 9, y - 7, 18, 14));
        }
        for (int i = 0; i < 10; i++) {
            double a = Math.PI * 2 * i / 10 + 0.3, x = cx + Math.cos(a) * 31, y = cy + Math.sin(a) * 31;
            double tw = 0.6 + 0.4 * Math.sin(ph * 2 + i * 1.3);
            addDot(img, x, y, 7, i % 2 == 0 ? 0xFF3A3A : 0xFFD23A, 0.5 * tw);
            g.setColor(new Color(i % 2 == 0 ? 0xE8202A : 0xFFD23A));
            g.fill(new Ellipse2D.Double(x - 3, y - 3, 6, 6));
            g.setColor(col(0xFFFFFF, 0.8));
            g.fill(new Ellipse2D.Double(x - 1.8, y - 2, 2, 2));
        }
        // bow
        g.setColor(new Color(0xD8141E));
        Path2D bl = new Path2D.Double();
        bl.moveTo(cx, cy + 34); bl.curveTo(cx - 22, cy + 18, cx - 26, cy + 44, cx - 4, cy + 38); bl.closePath();
        Path2D br = new Path2D.Double();
        br.moveTo(cx, cy + 34); br.curveTo(cx + 22, cy + 18, cx + 26, cy + 44, cx + 4, cy + 38); br.closePath();
        g.fill(bl); g.fill(br);
        g.fill(new java.awt.Polygon(new int[]{(int) cx - 3, (int) cx - 12, (int) cx - 6}, new int[]{(int) cy + 36, (int) cy + 60, (int) cy + 58}, 3));
        g.fill(new java.awt.Polygon(new int[]{(int) cx + 3, (int) cx + 12, (int) cx + 6}, new int[]{(int) cy + 36, (int) cy + 60, (int) cy + 58}, 3));
        g.setColor(new Color(0xFF4A52));
        g.fill(new Ellipse2D.Double(cx - 5, cy + 30, 10, 9));
    }

    // ================================================================== Rentierschlitten am Mond
    static void sleighMoon(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        field(img, (u, v, x, y) -> {
            int c = ramp(v, 0x060A1E, 0x101C44, 0x1E2E60, 0x2A3A70);
            double d = Math.hypot(x - 80, (y - 92) * 1.0);
            c = add(c, 0x6A7AC0, Math.exp(-Math.pow(Math.max(0, d - 50) / 40, 2)) * 0.5);
            return c;
        });
        stars(g, ph, 50, 721, 200);
        // moon with craters
        double mx = 80, my = 92, mr = 50;
        g.setPaint(new RadialGradientPaint((float) (mx - 14), (float) (my - 16), (float) (mr * 1.3), new float[]{0f, 0.7f, 1f}, new Color[]{new Color(0xFFFCE8), new Color(0xF0E6C0), new Color(0xD8C898)}));
        g.fill(new Ellipse2D.Double(mx - mr, my - mr, mr * 2, mr * 2));
        Random rn = new Random(722);
        for (int i = 0; i < 14; i++) {
            double a = rn.nextDouble() * Math.PI * 2, rr = Math.sqrt(rn.nextDouble()) * mr * 0.8, cr = 2 + rn.nextDouble() * 7;
            double x = mx + Math.cos(a) * rr, y = my + Math.sin(a) * rr;
            g.setColor(col(0xB8A878, 0.45));
            g.fill(new Ellipse2D.Double(x - cr, y - cr, cr * 2, cr * 2));
            g.setColor(col(0xFFFFF0, 0.35));
            g.fill(new Ellipse2D.Double(x - cr * 0.8, y - cr * 0.9, cr * 1.2, cr * 0.8));
        }
        // sleigh + reindeer silhouette flying over the moon (bobbing, galloping)
        double bob = Math.sin(ph) * 3;
        int sil = 0x0C1024;
        g.setColor(new Color(sil));
        // magic trail
        for (int i = 0; i < 26; i++) {
            double f = i / 26.0;
            double x = 34 - f * 40, y = 108 + bob + Math.sin(f * 6 - ph) * 4 + f * 18;
            star(g, x, y, 1.2 + (1 - f) * 1.4, 0xFFE08A, (1 - f) * (0.6 + 0.4 * Math.sin(ph * 2 + i)));
        }
        g.setColor(new Color(sil));
        double sx = 46, sy = 104 + bob;
        Path2D sl = new Path2D.Double();
        sl.moveTo(sx - 16, sy - 12); sl.lineTo(sx - 14, sy + 2); sl.lineTo(sx + 10, sy + 2); sl.quadTo(sx + 16, sy - 2, sx + 14, sy - 8); sl.lineTo(sx + 8, sy - 6); sl.lineTo(sx - 8, sy - 6); sl.lineTo(sx - 10, sy - 14); sl.closePath();
        g.fill(sl);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D runner = new Path2D.Double();
        runner.moveTo(sx - 18, sy + 6); runner.lineTo(sx + 14, sy + 6); runner.quadTo(sx + 21, sy + 5, sx + 19, sy - 1);
        g.draw(runner);
        g.draw(new java.awt.geom.Line2D.Double(sx - 10, sy + 2, sx - 10, sy + 6));
        g.draw(new java.awt.geom.Line2D.Double(sx + 6, sy + 2, sx + 6, sy + 6));
        // santa + sack
        g.fill(new Ellipse2D.Double(sx - 4, sy - 16, 9, 11));
        g.fill(new Ellipse2D.Double(sx - 1, sy - 22, 6, 6));
        g.fill(new Ellipse2D.Double(sx - 15, sy - 20, 11, 12));
        // reins
        g.setStroke(new BasicStroke(0.9f));
        g.draw(new java.awt.geom.Line2D.Double(sx + 4, sy - 12, sx + 30, sy - 12 + Math.sin(ph) * 1));
        for (int d = 0; d < 4; d++) {
            double rx = sx + 30 + d * 20, ry = sy - 14 - d * 4 + Math.sin(ph + d * 0.9) * 2.5;
            reindeer(g, rx, ry, ph * 2 + d * 1.1, d == 3);
            if (d < 3) {
                g.setColor(new Color(sil));
                g.setStroke(new BasicStroke(0.9f));
                g.draw(new java.awt.geom.Line2D.Double(rx + 7, ry - 3, rx + 18, ry - 7 + Math.sin(ph + (d + 1) * 0.9) * 2.5 - Math.sin(ph + d * 0.9) * 2.5));
            }
        }
        // town at the bottom
        Random tr = new Random(723);
        double x = -4;
        int hi = 0;
        while (x < FW) {
            double w = 16 + tr.nextDouble() * 14, h = 26 + tr.nextDouble() * 30, bx = x, by = FH - 14 - h;
            g.setColor(new Color(mix(0x0E1430, 0x1A2248, tr.nextDouble())));
            g.fill(new Rectangle2D.Double(bx, by, w, h + 14));
            Path2D rf = new Path2D.Double();
            rf.moveTo(bx - 2, by); rf.lineTo(bx + w / 2, by - 10); rf.lineTo(bx + w + 2, by); rf.closePath();
            g.fill(rf);
            g.setColor(new Color(0xE8F0FF));
            Path2D sn = new Path2D.Double();
            sn.moveTo(bx - 2, by); sn.lineTo(bx + w / 2, by - 10); sn.lineTo(bx + w + 2, by); sn.lineTo(bx + w - 2, by + 1.5); sn.lineTo(bx + w / 2, by - 7); sn.lineTo(bx + 2, by + 1.5); sn.closePath();
            g.fill(sn);
            for (int wy = 0; wy < 3; wy++)
                for (int wx = 0; wx < 2; wx++) {
                    if (tr.nextDouble() < 0.35) continue;
                    double on = 0.6 + 0.4 * Math.sin(ph * (1 + (hi % 2)) + hi * 1.7);
                    double lx = bx + 3 + wx * (w - 10) / 1.0 * 0.6, ly = by + 6 + wy * 10;
                    if (ly > FH - 18) continue;
                    g.setColor(new Color(mix(0x5A3A1A, 0xFFD27A, on)));
                    g.fill(new Rectangle2D.Double(lx, ly, 4, 5));
                    hi++;
                }
            x += w + 1;
        }
        g.setPaint(new GradientPaint(0, FH - 16, new Color(0xE8F0FF), 0, FH, new Color(0xB0C0E0)));
        g.fill(new Rectangle2D.Double(0, FH - 16, FW, 16));
        snow(g, t, ph, 50, 724, 0.7);
    }

    /** Flying reindeer silhouette facing right; gallop phase a. */
    static void reindeer(Graphics2D g, double x, double y, double a, boolean rudolph) {
        g.setColor(new Color(0x0C1024));
        g.fill(new Ellipse2D.Double(x - 7, y - 3.5, 14, 7));
        Path2D neck = new Path2D.Double();
        neck.moveTo(x + 4, y - 2); neck.lineTo(x + 8, y - 8); neck.lineTo(x + 10, y - 7); neck.lineTo(x + 7, y); neck.closePath();
        g.fill(neck);
        g.fill(new Ellipse2D.Double(x + 7, y - 10, 6, 4));
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new java.awt.geom.Line2D.Double(x + 8, y - 10, x + 6, y - 15));
        g.draw(new java.awt.geom.Line2D.Double(x + 7, y - 13, x + 4, y - 14));
        g.draw(new java.awt.geom.Line2D.Double(x + 9, y - 10, x + 10, y - 15));
        g.draw(new java.awt.geom.Line2D.Double(x + 10, y - 13, x + 12, y - 15));
        double s = Math.sin(a);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new java.awt.geom.Line2D.Double(x + 4, y + 2, x + 8 + s * 2, y + 7));
        g.draw(new java.awt.geom.Line2D.Double(x + 3, y + 2, x + 6 - s * 2, y + 7));
        g.draw(new java.awt.geom.Line2D.Double(x - 5, y + 2, x - 9 - s * 2, y + 6));
        g.draw(new java.awt.geom.Line2D.Double(x - 4, y + 2, x - 7 + s * 2, y + 7));
        g.fill(new Ellipse2D.Double(x - 9, y - 4, 3, 3));
        if (rudolph) {
            g.setColor(new Color(0xFF3A3A));
            g.fill(new Ellipse2D.Double(x + 11.5, y - 9.5, 2.6, 2.6));
            dot(g, x + 12.8, y - 8.2, 5, 0xFF3A3A, 0.7);
        }
    }

    // ================================================================== Weihnachtspullover
    /** Pixel motifs (rows of chars): R red bg, W white, G green, Y yellow, B brown, K red nose, L light (twinkles). */
    private static final String[] DEER = {
            "..W.W...W.W",
            "...W.....W.",
            "...WW...WW.",
            ".....WWW...",
            ".....WWWK..",
            "WWWWWWWW...",
            "WWWWWWWW...",
            "W.W...W.W..",
            "W.W...W.W.."};
    private static final String[] TREE = {
            "....Y....",
            "...GGG...",
            "..GLGGG..",
            "...GGG...",
            "..GGGLG..",
            ".GLGGGGG.",
            "..GGGGG..",
            ".GGGGLGG.",
            "GLGGGGGLG",
            "....B...."};
    private static final String[] FLAKE = {
            "..W.W..",
            "W..W..W",
            ".W.W.W.",
            "WWWWWWW",
            ".W.W.W.",
            "W..W..W",
            "..W.W.."};

    static void sweater(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        int cs = 5, cols = FW / cs, rows = FH / cs + 1;
        int[][] cell = new int[rows][cols];
        final int R = 0xB8141E, W = 0xF6F2EA, G = 0x1E7A38, Y = 0xFFD23A, B = 0x6A3A1A;
        for (int[] r : cell) java.util.Arrays.fill(r, R);
        // bands
        int y = 2;
        for (int x = 0; x < cols; x++) { cell[y][x] = W; cell[y + 1][x] = ((x / 2) % 2 == 0) ? G : W; cell[y + 2][x] = W; }
        y = 7;
        for (int x = 0; x < cols; x++) { int zz = Math.abs((x % 6) - 3); for (int d = 0; d < 4; d++) if (d == zz) cell[y + d][x] = W; }
        y = 13;
        stamp(cell, DEER, 2, y, W, G, Y, B, k);
        stampMirror(cell, DEER, cols - 13, y, W, G, Y, B, k);
        y = 25;
        for (int x = 0; x < cols; x++) { cell[y][x] = G; cell[y + 1][x] = (x % 4 == 1) ? W : G; cell[y + 2][x] = G; }
        y = 29;
        for (int i = 0; i < 3; i++) stamp(cell, TREE, 1 + i * 11, y, W, G, Y, B, k + i);
        y = 41;
        for (int x = 0; x < cols; x++) { cell[y][x] = W; cell[y + 1][x] = ((x / 2) % 2 == 0) ? W : R; }
        y = 44;
        for (int i = 0; i < 4; i++) stamp(cell, FLAKE, 1 + i * 8, y, W, G, Y, B, k);
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) knitStitch(g, c * cs, r * cs, cs, cell[r][c]);
        // twinkling "lights" glow
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (cell[r][c] == 0xFF3A3A || cell[r][c] == 0xFFD23A && r > 28 && r < 40) addDot(img, c * cs + 2.5, r * cs + 2.5, 5, cell[r][c], 0.25);
    }

    private static void stamp(int[][] cell, String[] m, int x0, int y0, int W, int G, int Y, int B, int k) {
        for (int r = 0; r < m.length; r++)
            for (int c = 0; c < m[r].length(); c++) put(cell, x0 + c, y0 + r, m[r].charAt(c), W, G, Y, B, k + r * 3 + c);
    }

    private static void stampMirror(int[][] cell, String[] m, int x0, int y0, int W, int G, int Y, int B, int k) {
        for (int r = 0; r < m.length; r++)
            for (int c = 0; c < m[r].length(); c++) put(cell, x0 + m[r].length() - 1 - c, y0 + r, m[r].charAt(c), W, G, Y, B, k + r * 3 + c);
    }

    private static void put(int[][] cell, int x, int y, char ch, int W, int G, int Y, int B, int seed) {
        if (y < 0 || y >= cell.length || x < 0 || x >= cell[0].length) return;
        switch (ch) {
            case 'W': cell[y][x] = W; break;
            case 'G': cell[y][x] = G; break;
            case 'Y': cell[y][x] = Y; break;
            case 'B': cell[y][x] = B; break;
            case 'K': cell[y][x] = 0xFF3A3A; break;
            case 'L': { int[] lc = {0xFFD23A, 0xFF3A3A, 0x3AA8FF, 0xF6F2EA}; cell[y][x] = lc[Math.floorMod(seed, 4)]; break; }
            default: break;
        }
    }

    /** One V stitch in a cs x cs cell. */
    private static void knitStitch(Graphics2D g, double x, double y, double cs, int c) {
        g.setColor(new Color(shade(c, 0.62)));
        g.fill(new Rectangle2D.Double(x, y, cs, cs));
        for (int s = -1; s <= 1; s += 2) {
            java.awt.geom.AffineTransform o = g.getTransform();
            g.translate(x + cs / 2 + s * cs * 0.22, y + cs * 0.5);
            g.rotate(-s * 0.55);
            g.setPaint(new GradientPaint(0, (float) (-cs * 0.6), new Color(mix(c, 0xFFFFFF, 0.18)), 0, (float) (cs * 0.6), new Color(shade(c, 0.85))));
            g.fill(new Ellipse2D.Double(-cs * 0.2, -cs * 0.62, cs * 0.4, cs * 1.24));
            g.setTransform(o);
        }
    }

    // ================================================================== Nordlicht-Weihnacht
    static void auroraXmas(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        field(img, (u, v, x, y) -> {
            int c = ramp(v, 0x03060F, 0x07122A, 0x0E2240, 0x183458);
            // curtains: bright lower edge following a moving wave, rays going up
            for (int b = 0; b < 2; b++) {
                double edgeY = (b == 0 ? 0.42 : 0.3) + 0.07 * Math.sin(u * 7 + ph + b * 2) + 0.03 * Math.sin(u * 17 - ph * 2 + b);
                double dv = edgeY - v;
                if (dv < -0.02) continue;
                double ray = 0.5 + 0.5 * Math.sin(u * 60 + Math.sin(u * 9 + ph) * 3 + b * 5);
                double fb = fbm(u * 8 + b * 3, t * 2, 8, 2, 3, 731 + b);
                double inten = Math.exp(-Math.max(0, dv) / (0.16 + 0.06 * fb)) * (0.35 + 0.65 * ray * fb) * smooth(-0.02, 0.01, dv);
                int ac = b == 0 ? mix(0x3AFF8A, 0xB05AFF, Math.min(1, dv * 3.5)) : mix(0x3AE8D8, 0xFF5AD8, Math.min(1, dv * 4));
                c = add(c, ac, inten * (b == 0 ? 0.95 : 0.6));
            }
            return c;
        });
        stars(g, ph, 40, 732, 150);
        // mountains
        Path2D m1 = new Path2D.Double();
        m1.moveTo(0, 190); m1.lineTo(26, 150); m1.lineTo(48, 172); m1.lineTo(82, 132); m1.lineTo(116, 176); m1.lineTo(138, 156); m1.lineTo(FW, 176); m1.lineTo(FW, FH); m1.lineTo(0, FH); m1.closePath();
        g.setPaint(new GradientPaint(0, 130, new Color(0x2A3A5A), 0, 200, new Color(0x121A2E)));
        g.fill(m1);
        Path2D caps = new Path2D.Double();
        caps.moveTo(70, 147); caps.lineTo(82, 132); caps.lineTo(96, 150); caps.lineTo(88, 147); caps.lineTo(82, 152); caps.lineTo(76, 146); caps.closePath();
        caps.moveTo(18, 162); caps.lineTo(26, 150); caps.lineTo(34, 160); caps.lineTo(26, 158); caps.closePath();
        caps.moveTo(131, 164); caps.lineTo(138, 156); caps.lineTo(146, 162); caps.lineTo(139, 161); caps.closePath();
        g.setColor(new Color(0xD8E4F8));
        g.fill(caps);
        // snowy hill
        Path2D hill = new Path2D.Double();
        hill.moveTo(0, 214); hill.quadTo(80, 188, FW, 214); hill.lineTo(FW, FH); hill.lineTo(0, FH); hill.closePath();
        g.setPaint(new GradientPaint(0, 190, new Color(0xE8F0FF), 0, FH, new Color(0x8AA8D0)));
        g.fill(hill);
        // aurora reflection on the snow
        addDot(img, 80, 206, 60, 0x3AFF8A, 0.12);
        // christmas tree on the hill with lights
        double tx = 80, ty = 206;
        pine(g, tx, ty, 70, 0x14482A, 0xF0F6FF, 5);
        Random rn = new Random(733);
        int[] lc = {0xFF3A3A, 0xFFD23A, 0x3AA8FF, 0xFF7AE0, 0x3AE86A};
        for (int i = 0; i < 18; i++) {
            double f = rn.nextDouble(), yy = ty - 10 - f * 50, half = (1 - f) * 22 + 3;
            double xx = tx + (rn.nextDouble() * 2 - 1) * half * 0.85;
            double tw = 0.5 + 0.5 * Math.sin(ph * (1 + i % 2) + i * 2.1);
            int c = lc[i % lc.length];
            addDot(img, xx, yy, 5, c, 0.45 * tw);
            g.setColor(new Color(mix(0x404040, c, 0.4 + 0.6 * tw)));
            g.fill(new Ellipse2D.Double(xx - 1.6, yy - 1.6, 3.2, 3.2));
        }
        addDot(img, tx, ty - 66, 14, 0xFFE07A, 0.6 + 0.2 * Math.sin(ph));
        g.setColor(new Color(0xFFE07A));
        g.fill(XmasTex.starShape(tx, ty - 66, 6, 2.6, 5));
        // presents under the tree
        int[][] gifts = {{-18, 0xE8323A}, {12, 0x3A8AFF}, {-6, 0x2EB84A}};
        for (int[] gf : gifts) {
            double gx = tx + gf[0], gy = ty - 2;
            g.setColor(new Color(gf[1]));
            g.fill(new Rectangle2D.Double(gx, gy - 7, 9, 8));
            g.setColor(new Color(0xFFE07A));
            g.fill(new Rectangle2D.Double(gx + 3.8, gy - 7, 1.4, 8));
        }
        snow(g, t, ph, 45, 734, 0.6);
    }
}
