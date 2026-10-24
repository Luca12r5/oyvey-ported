package dev.lego.cosmetic;

import static dev.lego.cosmetic.Capes.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/** Premium animated capes (own designs). Canvas FW x FH = 160 x 256, every animation loops seamlessly. */
final class Cos3Capes {
    private Cos3Capes() { }

    static void defineAll() {
        def("c3void", 16, 8, 0xD8B8FF, 0x2A0E48, 0x07030E, Cos3Capes::voidCloak);
        def("c3nether", 16, 8, 0xFF9A4A, 0x4A0E08, 0x1A0504, Cos3Capes::nether);
        def("c3heaven", 16, 8, 0xFFFFFF, 0xC89A2A, 0xFFF4D8, Cos3Capes::heaven);
        def("c3sunflower", 12, 6, 0xFFE27A, 0x6A8A2A, 0x2A5A9A, Cos3Capes::sunflower);
        def("c3pumpkin", 16, 10, 0xFFB04A, 0x3A1A08, 0x140A1E, Cos3Capes::pumpkin);
        def("c3ghost", 16, 8, 0xC8FFF4, 0x1A4A52, 0x08141E, Cos3Capes::ghost);
        def("c3skull", 16, 8, 0xC8FFB0, 0x1A2A18, 0x060806, Cos3Capes::skull);
        def("c3bolt_purple", 16, 10, 0x3A2A4A, 0x0E0A14, 0x07050C, (g, i, ph, k, n) -> neonBolt(g, i, ph, k, n, 0xC05AFF));
        def("c3bolt_blue", 16, 10, 0x2A3A4E, 0x0A0E16, 0x05070C, (g, i, ph, k, n) -> neonBolt(g, i, ph, k, n, 0x3AA8FF));
        def("c3bolt_green", 16, 10, 0x2A4A32, 0x0A140E, 0x050C07, (g, i, ph, k, n) -> neonBolt(g, i, ph, k, n, 0x3AFF7A));
        def("c3bolt_orange", 16, 10, 0x4E3A2A, 0x16100A, 0x0C0805, (g, i, ph, k, n) -> neonBolt(g, i, ph, k, n, 0xFF8A2A));
        def("c3embertree", 16, 8, 0xFFB86A, 0x3A1A0A, 0x120806, Cos3Capes::emberTree);
    }

    // ================================================================== void cloak: swirling void + own runes
    static void voidCloak(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        final double cx = 80, cy = 118;
        field(img, (u, v, x, y) -> {
            double dx = x - cx, dy = (y - cy) * 0.82;
            double r = Math.hypot(dx, dy) + 1e-3;
            double th = Math.atan2(dy, dx) / (Math.PI * 2) + 0.5;
            double sp = th * 6 + Math.log(r) * 2.6 - t * 6;       // 6 periodic cells in angle, rotates one cell per loop
            double w = fbm(sp, Math.log(r) * 2.2, 6, 0, 4, 71);
            double arms = 0.5 + 0.5 * Math.sin((th * 3 + Math.log(r) * 0.9 - t) * Math.PI * 2 + (w - 0.5) * 3);
            double core = Math.exp(-r / 20);
            double dens = Math.pow(arms, 2.2) * (0.35 + 0.9 * w) * Math.exp(-r / 110);
            int c = ramp(v, 0x0A0414, 0x07030E, 0x040208);
            c = add(c, 0x6A1AC8, dens * 0.9);
            c = add(c, 0xD27AFF, Math.pow(dens, 2.4) * 1.2);
            // event horizon: dark core with a bright rim
            double rim = Math.exp(-Math.pow((r - 15) / 3.2, 2));
            if (r < 15) c = mix(c, 0x000000, smooth(15, 9, r));
            c = add(c, 0xF0C8FF, rim * (0.55 + 0.25 * Math.sin(ph * 2 + th * 12)));
            c = add(c, 0x3A0A6A, core * 0.2);
            if (hash(x, y, 72) > 0.993) c = add(c, 0xFFFFFF, 0.3 + 0.5 * hash(x, y, 73));
            return c;
        });
        // orbiting motes
        Random rn = new Random(77);
        for (int i = 0; i < 40; i++) {
            double r0 = 20 + rn.nextDouble() * 70, a0 = rn.nextDouble() * Math.PI * 2;
            double a = a0 + ph * (rn.nextBoolean() ? 1 : 2) * (40 / r0 > 1 ? 1 : 1);
            double x = cx + Math.cos(a) * r0, y = cy + Math.sin(a) * r0 * 1.2;
            dot(g, x, y, 1.6 + rn.nextDouble() * 1.8, 0xE0A8FF, 0.55 + 0.35 * Math.sin(ph + i));
        }
        // rune columns on both sides + a rune arc above the void, lighting up one after another
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int side = 0; side < 2; side++)
            for (int i = 0; i < 7; i++) {
                double x = side == 0 ? 22 : 138, y = 30 + i * 32;
                double lit = Math.pow(0.5 + 0.5 * Math.cos(ph - (i + side * 3.5) * 0.9), 3);
                rune(g, x, y, 7, (i * 3 + side * 5) % 8, 0xC87AFF, 0.35 + 0.65 * lit);
                if (lit > 0.05) addDot(img, x, y, 12, 0xA040FF, 0.35 * lit);
            }
        for (int i = 0; i < 5; i++) {
            double a = Math.PI * (1.15 + i * 0.175);
            double x = cx + Math.cos(a) * 54, y = cy + Math.sin(a) * 60;
            double lit = Math.pow(0.5 + 0.5 * Math.cos(ph * 2 - i * 1.2), 2);
            rune(g, x, y, 6, (i + 2) % 8, 0xF0D0FF, 0.4 + 0.6 * lit);
        }
    }

    /** Own rune alphabet (8 signs), drawn with a soft glow. */
    static void rune(Graphics2D g, double x, double y, double s, int kind, int rgb, double a) {
        Path2D p = new Path2D.Double();
        switch (kind % 8) {
            case 0: p.moveTo(-s, s); p.lineTo(0, -s); p.lineTo(s, s); p.closePath(); p.append(new Ellipse2D.Double(-s * 0.22, s * 0.1, s * 0.44, s * 0.44), false); break;
            case 1: p.moveTo(0, -s); p.lineTo(0, s); p.moveTo(-s, -s * 0.4); p.lineTo(0, 0); p.lineTo(s, -s * 0.4); break;
            case 2: p.append(new Ellipse2D.Double(-s * 0.6, -s * 0.6, s * 1.2, s * 1.2), false); p.moveTo(0, -s); p.lineTo(0, s); break;
            case 3: p.moveTo(-s, -s); p.lineTo(s, -s); p.lineTo(-s, s); p.lineTo(s, s); p.closePath(); p.moveTo(-s * 1.2, 0); p.lineTo(s * 1.2, 0); break;
            case 4: p.moveTo(0, -s); p.lineTo(s, 0); p.lineTo(0, s); p.lineTo(-s, 0); p.closePath(); p.moveTo(0, -s * 0.3); p.lineTo(0, s * 0.3); break;
            case 5: p.moveTo(-s, s); p.lineTo(-s, -s); p.lineTo(s * 0.6, -s * 0.2); p.moveTo(-s, 0); p.lineTo(s, s); break;
            case 6: p.moveTo(-s, -s * 0.2); p.quadTo(0, -s * 1.6, s, -s * 0.2); p.moveTo(0, -s * 0.8); p.lineTo(0, s); p.moveTo(-s * 0.6, s); p.lineTo(s * 0.6, s); break;
            default: p.moveTo(-s, 0); p.lineTo(s, 0); p.moveTo(-s * 0.5, -s); p.lineTo(s * 0.5, s); p.moveTo(s * 0.5, -s); p.lineTo(-s * 0.5, s); break;
        }
        Shape sh = AffineTransform.getTranslateInstance(x, y).createTransformedShape(p);
        glowStroke(g, sh, rgb, 1.5f, 1.2f, a);
    }

    // ================================================================== nether: basalt with flowing lava veins
    static void nether(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        field(img, (u, v, x, y) -> {
            double wv = fbm(u * 3, v * 5 - t * 5, 3, 5, 3, 81);
            double f = fbm(u * 4 + (wv - 0.5) * 1.2, v * 6 - t * 6, 4, 6, 5, 82);
            double ridge = 1 - Math.abs(f - 0.5) * 2;
            double vein = Math.pow(smooth(0.9, 0.99, ridge), 1.5);
            double rock = fbm(u * 10, v * 16, 10, 16, 3, 83);
            int c = ramp(rock, 0x140606, 0x2A0C0A, 0x3E1410, 0x4A1C16);
            c = shade(c, 0.7 + 0.5 * smooth(0.3, 0.9, ridge));
            double pulse = 0.8 + 0.2 * Math.sin(ph + v * 6);
            c = add(c, 0xFF3A0A, vein * 1.1 * pulse);
            c = add(c, 0xFFC04A, Math.pow(vein, 3) * 0.9 * pulse);
            c = add(c, 0xFF2A0A, smooth(0.55, 1.0, v) * 0.25);
            return c;
        });
        // glowing crimson crystals growing from the bottom
        Random rn = new Random(84);
        for (int i = 0; i < 7; i++) {
            double x = 18 + i * 21 + rn.nextDouble() * 8, h = 26 + rn.nextDouble() * 34, w = 7 + rn.nextDouble() * 5;
            double lean = (rn.nextDouble() - 0.5) * 0.5;
            Path2D p = new Path2D.Double();
            p.moveTo(x - w / 2, FH - 6); p.lineTo(x - w / 2 + lean * h * 0.6, FH - 6 - h * 0.75); p.lineTo(x + lean * h, FH - 6 - h); p.lineTo(x + w / 2 + lean * h * 0.6, FH - 6 - h * 0.75); p.lineTo(x + w / 2, FH - 6); p.closePath();
            addDot(img, x + lean * h * 0.5, FH - 6 - h * 0.5, h * 0.7, 0xFF2A4A, 0.35 + 0.15 * Math.sin(ph + i));
            g.setPaint(new GradientPaint((float) x, (float) (FH - h), new Color(0xFF6A7A), (float) (x + w), (float) FH, new Color(0x8A0A1E)));
            g.fill(p);
            g.setColor(col(0xFFD0D8, 0.7));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(new java.awt.geom.Line2D.Double(x, FH - 6, x + lean * h, FH - 6 - h));
        }
        // rising embers and ash
        for (int i = 0; i < 60; i++) {
            double x0 = rn.nextDouble() * FW, y0 = rn.nextDouble(), sp = 1 + rn.nextInt(2);
            double y = FH - ((y0 + sp * t) % 1) * (FH + 20) + 10;
            double x = x0 + Math.sin(ph * sp + i) * 6;
            boolean ember = i % 3 != 0;
            double life = ((y0 + sp * t) % 1);
            if (ember) dot(g, x, y, 2.2 + rn.nextDouble() * 1.5, 0xFF8A2A, 0.9 * (1 - life * 0.7));
            else dot(g, x, y, 1.6, 0x8A8A8A, 0.5);
        }
    }

    // ================================================================== heavenly gold with light veins
    static void heaven(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        field(img, (u, v, x, y) -> {
            int c = ramp(v, 0xFFF2CC, 0xF4D07A, 0xDCA23A, 0xA8701A);
            double cl = fbm(u * 4 + t * 4, v * 3, 4, 0, 4, 91);
            c = mix(c, 0xFFF8E0, smooth(0.5, 0.8, cl) * 0.45 * (1 - v));
            // rotating god rays from the top center
            double ang = Math.atan2(y + 40, x - 80);
            double ray = Math.pow(0.5 + 0.5 * Math.sin(ang * 22 + ph), 6) * Math.exp(-v * 2.2);
            c = add(c, 0xFFF6D0, ray * 0.35);
            return c;
        });
        // branching light veins (pulse travelling downwards)
        Random rn = new Random(92);
        for (int b = 0; b < 5; b++) {
            double x = 20 + b * 30 + rn.nextDouble() * 10, y = 0;
            Path2D p = new Path2D.Double();
            p.moveTo(x, y);
            double dir = rn.nextBoolean() ? 1 : -1;
            while (y < FH) {
                x += (rn.nextDouble() - 0.5) * 16 + dir * 2;
                if (x < 14 || x > FW - 14) dir = -dir;
                y += 10 + rn.nextDouble() * 12;
                p.lineTo(x, y);
                if (rn.nextDouble() < 0.3) {
                    Path2D q = new Path2D.Double();
                    q.moveTo(x, y);
                    double qx = x, qy = y;
                    for (int s = 0; s < 3; s++) { qx += (rn.nextBoolean() ? 1 : -1) * (5 + rn.nextDouble() * 8); qy += 7 + rn.nextDouble() * 8; q.lineTo(qx, qy); }
                    glowStroke(g, q, 0xFFC84A, 0.9f, 1.0f, 0.6);
                }
            }
            glowStroke(g, p, 0xFFB82A, 1.4f, 1.3f, 0.75);
            // travelling light pulses along the vein
            for (int s = 0; s < 2; s++) {
                double pos = ((t + s * 0.5 + b * 0.21) % 1);
                java.awt.geom.PathIterator it = p.getPathIterator(null, 1);
                double[] c = new double[6];
                double py = pos * FH, lx = 0, ly = 0;
                while (!it.isDone()) {
                    it.currentSegment(c);
                    if (c[1] >= py) {
                        double f = ly == c[1] ? 0 : (py - ly) / (c[1] - ly);
                        double px = lx + (c[0] - lx) * f;
                        addDot(img, px, py, 10, 0xFFFFFF, 0.8);
                        addDot(img, px, py, 22, 0xFFD060, 0.35);
                        break;
                    }
                    lx = c[0]; ly = c[1];
                    it.next();
                }
            }
        }
        // central sun sigil
        double cx = 80, cy = 96;
        addDot(img, cx, cy, 40, 0xFFE08A, 0.5 + 0.15 * Math.sin(ph));
        g.setColor(col(0xFFFFFF, 0.95));
        g.setStroke(new BasicStroke(2.2f));
        g.draw(new Ellipse2D.Double(cx - 18, cy - 18, 36, 36));
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6 + ph / 6;
            double r0 = 22, r1 = i % 2 == 0 ? 36 : 29;
            glowStroke(g, new java.awt.geom.Line2D.Double(cx + Math.cos(a) * r0, cy + Math.sin(a) * r0, cx + Math.cos(a) * r1, cy + Math.sin(a) * r1), 0xFFC84A, 1.8f, 1.2f, 0.9);
        }
        g.setPaint(new RadialGradientPaint((float) cx, (float) cy, 13, new float[]{0f, 1f}, new Color[]{Color.WHITE, new Color(0xFFD86A)}));
        g.fill(new Ellipse2D.Double(cx - 12, cy - 12, 24, 24));
        for (int i = 0; i < 30; i++) {
            double x = 12 + rn.nextDouble() * 136, y = 12 + rn.nextDouble() * 232;
            star(g, x, y, 1 + rn.nextDouble() * 1.6, 0xFFE8A0, Math.pow(Math.max(0, Math.sin(ph + rn.nextDouble() * 6.28)), 2));
        }
    }

    // ================================================================== sunflower art
    static void sunflower(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        field(img, (u, v, x, y) -> {
            int c = ramp(v, 0x3A7AD8, 0x6AAAF0, 0xB8E0FF, 0xE8F6FF);
            double cl = fbm(u * 3 + ph / (Math.PI * 2) * 3, v * 5, 3, 0, 4, 101);
            c = mix(c, 0xFFFFFF, smooth(0.55, 0.75, cl) * (v < 0.5 ? 0.8 : 0.3));
            double hill = 0.78 + 0.03 * Math.sin(u * 7 + 1);
            if (v > hill) c = mix(0x5AA83A, 0x2A6A1E, (v - hill) * 4 + 0.15 * hash(x / 3, y / 3, 102));
            return c;
        });
        double sway = Math.sin(ph) * 0.06;
        // stem and leaves
        Path2D stem = new Path2D.Double();
        stem.moveTo(78, FH);
        stem.curveTo(74, 200, 90 + sway * 60, 160, 80 + sway * 90, 108);
        g.setColor(new Color(0x2A7A22));
        g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(stem);
        g.setColor(new Color(0x5AB83A));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(stem);
        for (int s = -1; s <= 1; s += 2) {
            AffineTransform o = g.getTransform();
            g.translate(80 + s * 2, 190 - s * 14);
            g.rotate(s * (0.7 + sway * 2));
            Path2D leaf = new Path2D.Double();
            leaf.moveTo(0, 0); leaf.curveTo(s * 14, -16, s * 34, -10, s * 42, 0); leaf.curveTo(s * 30, 10, s * 12, 12, 0, 0);
            g.setPaint(new GradientPaint(0, -10, new Color(0x6AC84A), 0, 12, new Color(0x2A7A22)));
            g.fill(leaf);
            g.setColor(col(0x1A5A12, 0.8));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(new java.awt.geom.Line2D.Double(0, 0, s * 38, 0));
            g.setTransform(o);
        }
        // flower head
        double cx = 80 + sway * 90, cy = 96;
        addDot(img, cx, cy, 70, 0xFFE27A, 0.35);
        AffineTransform o = g.getTransform();
        g.translate(cx, cy);
        g.rotate(sway * 1.5);
        for (int ring = 0; ring < 2; ring++) {
            int np = 18;
            for (int i = 0; i < np; i++) {
                double a = (i + ring * 0.5) * Math.PI * 2 / np;
                AffineTransform o2 = g.getTransform();
                g.rotate(a);
                double len = ring == 0 ? 50 : 42, wid = ring == 0 ? 11 : 10;
                Path2D p = new Path2D.Double();
                p.moveTo(18, 0); p.curveTo(26, -wid, len - 6, -wid * 0.7, len, 0); p.curveTo(len - 6, wid * 0.7, 26, wid, 18, 0);
                g.setPaint(new GradientPaint(18, 0, new Color(ring == 0 ? 0xE89A0A : 0xF4B21A), (float) len, 0, new Color(ring == 0 ? 0xFFD23A : 0xFFE46A)));
                g.fill(p);
                g.setColor(col(0xB86A00, 0.5));
                g.setStroke(new BasicStroke(0.9f));
                g.draw(new java.awt.geom.Line2D.Double(22, 0, len - 6, 0));
                g.setTransform(o2);
            }
        }
        g.setPaint(new RadialGradientPaint(0, 0, 22, new float[]{0f, 0.7f, 1f}, new Color[]{new Color(0x3A1E0A), new Color(0x5A300E), new Color(0x7A4A12)}));
        g.fill(new Ellipse2D.Double(-22, -22, 44, 44));
        // fibonacci seeds
        for (int i = 0; i < 150; i++) {
            double r = Math.sqrt(i) * 1.75, a = i * 2.39996;
            if (r > 20) break;
            g.setColor(new Color(mix(0xC8902A, 0x2A1406, r / 22)));
            g.fill(new Ellipse2D.Double(Math.cos(a) * r - 0.9, Math.sin(a) * r - 0.9, 1.8, 1.8));
        }
        g.setTransform(o);
        // drifting pollen and a little bee path (own style: round striped bug)
        Random rn = new Random(103);
        for (int i = 0; i < 18; i++) {
            double a = rn.nextDouble() * 6.28 + ph, r = 60 + rn.nextDouble() * 20;
            dot(g, cx + Math.cos(a) * r * 0.8, cy + Math.sin(a) * r, 1.8, 0xFFF4A0, 0.8);
        }
        double bx = cx + Math.cos(ph) * 52, by = cy + 40 + Math.sin(ph * 2) * 14;
        g.setColor(new Color(0xFFD23A));
        g.fill(new Ellipse2D.Double(bx - 6, by - 4, 12, 8));
        g.setColor(new Color(0x2A1A0A));
        g.fill(new java.awt.geom.Rectangle2D.Double(bx - 2, by - 4, 2, 8));
        g.fill(new java.awt.geom.Rectangle2D.Double(bx + 2, by - 4, 2, 8));
        g.setColor(col(0xFFFFFF, 0.8));
        g.fill(new Ellipse2D.Double(bx - 4, by - 10, 6, 6));
        g.fill(new Ellipse2D.Double(bx, by - 10, 6, 6));
    }

    // ================================================================== halloween: pumpkin lantern
    static void pumpkin(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        final double fl = 0.8 + 0.12 * Math.sin(ph * 3) + 0.08 * Math.sin(ph * 7 + 1);
        field(img, (u, v, x, y) -> {
            int c = ramp(v, 0x1A0A2E, 0x2A1240, 0x3A1A30, 0x1A0A0E);
            double fog = fbm(u * 3 + t * 3, v * 2, 3, 0, 4, 111);
            c = add(c, 0x6A3A8A, smooth(0.5, 0.8, fog) * 0.25 * v);
            c = add(c, 0xFF7A1A, Math.exp(-Math.hypot(x - 80, y - 150) / 50) * 0.35 * fl);
            if (hash(x, y, 112) > 0.994 && v < 0.5) c = add(c, 0xFFFFFF, 0.5);
            return c;
        });
        // moon + bats crossing
        g.setPaint(new RadialGradientPaint(124, 38, 16, new float[]{0f, 1f}, new Color[]{new Color(0xFFF6D8), new Color(0xF0D890)}));
        g.fill(new Ellipse2D.Double(110, 24, 28, 28));
        for (int i = 0; i < 3; i++) {
            double bt = (t + i / 3.0) % 1;
            double bx = -20 + bt * (FW + 40), by = 40 + i * 22 + Math.sin(bt * Math.PI * 4) * 6;
            bat(g, bx, by, 6 + i, Math.sin(ph * 4 + i) > 0);
        }
        // pumpkin
        double cx = 80, cy = 150;
        for (int i = -2; i <= 2; i++) {
            double w = 34 - Math.abs(i) * 6;
            g.setPaint(new GradientPaint((float) (cx + i * 16 - w), (float) (cy - 40), new Color(Math.abs(i) == 2 ? 0xC8500A : 0xFF8A1A), (float) (cx + i * 16 + w), (float) (cy + 40), new Color(0xA8400A)));
            g.fill(new Ellipse2D.Double(cx + i * 15 - w, cy - 42, w * 2, 84));
        }
        g.setColor(new Color(0x4A6A1A));
        g.fill(new RoundRectangle2D.Double(cx - 5, cy - 56, 10, 18, 4, 4));
        // carved face (own shapes): crescent eyes, star nose, wavy grin
        Path2D face = new Path2D.Double();
        face.append(new Path2D.Double(tri(cx - 30, cy - 6, cx - 10, cy - 6, cx - 18, cy - 26)), false);
        face.append(new Path2D.Double(tri(cx + 30, cy - 6, cx + 10, cy - 6, cx + 18, cy - 26)), false);
        face.append(new Path2D.Double(tri(cx - 5, cy + 6, cx + 5, cy + 6, cx, cy - 3)), false);
        Path2D m = new Path2D.Double();
        m.moveTo(cx - 34, cy + 12);
        for (int i = 0; i <= 8; i++) m.lineTo(cx - 34 + i * 8.5, cy + 14 + (i % 2 == 0 ? 0 : 7) + Math.sin(i * 0.4) * 2);
        m.quadTo(cx, cy + 44, cx - 34, cy + 12);
        face.append(m, false);
        addDot(img, cx, cy + 6, 60, 0xFF9A2A, 0.35 * fl);
        g.setColor(new Color(mix(0x5A1A00, 0xFFE07A, fl)));
        g.fill(face);
        g.setPaint(new RadialGradientPaint((float) cx, (float) (cy + 4), 30, new float[]{0f, 1f}, new Color[]{col(0xFFFFE0, fl), col(0xFFB02A, 0)}));
        g.fill(face);
        glowStroke(g, face, 0xFFB03A, 0.8f, 1.4f, 0.8 * fl);
        // fireflies / sparks
        Random rn = new Random(113);
        for (int i = 0; i < 20; i++) {
            double x = rn.nextDouble() * FW, y0 = rn.nextDouble();
            double y = FH - ((y0 + t) % 1) * FH;
            dot(g, x + Math.sin(ph + i) * 5, y, 2, 0xFFB04A, 0.7);
        }
    }

    private static Path2D tri(double x0, double y0, double x1, double y1, double x2, double y2) {
        Path2D p = new Path2D.Double();
        p.moveTo(x0, y0); p.lineTo(x1, y1); p.lineTo(x2, y2); p.closePath();
        return p;
    }

    private static void bat(Graphics2D g, double x, double y, double s, boolean up) {
        Path2D p = new Path2D.Double();
        double wy = up ? -s * 0.9 : s * 0.4;
        p.moveTo(x, y - s * 0.3);
        p.quadTo(x - s, y + wy - s * 0.2, x - s * 2, y + wy);
        p.quadTo(x - s * 1.4, y + s * 0.2, x - s * 0.9, y + s * 0.5);
        p.quadTo(x - s * 0.5, y + s * 0.2, x, y + s * 0.6);
        p.quadTo(x + s * 0.5, y + s * 0.2, x + s * 0.9, y + s * 0.5);
        p.quadTo(x + s * 1.4, y + s * 0.2, x + s * 2, y + wy);
        p.quadTo(x + s, y + wy - s * 0.2, x, y - s * 0.3);
        g.setColor(new Color(0x0A0410));
        g.fill(p);
    }

    // ================================================================== halloween: cute ghost with glow ring
    static void ghost(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        field(img, (u, v, x, y) -> {
            int c = ramp(v, 0x0A1A2A, 0x10283A, 0x0E2230, 0x081420);
            double f = fbm(u * 3 - t * 3, v * 4, 3, 0, 4, 121);
            c = add(c, 0x2A8A8A, smooth(0.55, 0.85, f) * 0.22);
            if (hash(x, y, 122) > 0.993) c = add(c, 0xC8FFFF, 0.5);
            return c;
        });
        double cx = 80, cy = 112 + Math.sin(ph) * 8;
        // glow ring
        addDot(img, cx, cy, 70, 0x5AFFE0, 0.3);
        Ellipse2D ring = new Ellipse2D.Double(cx - 56, cy - 56, 112, 112);
        glowStroke(g, ring, 0x6AFFE8, 2.4f, 2.2f, 0.9);
        for (int i = 0; i < 10; i++) {
            double a = ph + i * Math.PI / 5;
            double r = 56;
            dot(g, cx + Math.cos(a) * r, cy + Math.sin(a) * r, 4, 0xE0FFFF, 0.9);
        }
        // ghost body
        Path2D b = new Path2D.Double();
        double w = 34, top = cy - 44, bot = cy + 36;
        b.moveTo(cx - w, bot);
        b.lineTo(cx - w, cy - 10);
        b.curveTo(cx - w, top - 4, cx + w, top - 4, cx + w, cy - 10);
        b.lineTo(cx + w, bot);
        int waves = 4;
        for (int i = 0; i < waves; i++) {
            double x0 = cx + w - i * (2 * w / waves), x1 = x0 - w / waves, x2 = x0 - 2 * w / waves;
            double wob = Math.sin(ph * 2 + i * 1.6) * 4;
            b.quadTo(x1, bot + 12 + wob, x2, bot);
        }
        b.closePath();
        g.setColor(col(0x3AFFE0, 0.25));
        g.setStroke(new BasicStroke(8f));
        g.draw(b);
        g.setPaint(new GradientPaint((float) cx, (float) top, new Color(0xFFFFFF), (float) cx, (float) bot, new Color(0xC8E8F0)));
        g.fill(b);
        // face
        double blink = (k % 8 == 5) ? 0.15 : 1;
        g.setColor(new Color(0x1A2030));
        g.fill(new Ellipse2D.Double(cx - 17, cy - 16, 11, 14 * blink));
        g.fill(new Ellipse2D.Double(cx + 6, cy - 16, 11, 14 * blink));
        g.setColor(Color.WHITE);
        if (blink > 0.5) { g.fill(new Ellipse2D.Double(cx - 14, cy - 14, 4, 4)); g.fill(new Ellipse2D.Double(cx + 9, cy - 14, 4, 4)); }
        g.setColor(col(0xFF8AA8, 0.7));
        g.fill(new Ellipse2D.Double(cx - 25, cy + 1, 10, 6));
        g.fill(new Ellipse2D.Double(cx + 15, cy + 1, 10, 6));
        g.setColor(new Color(0x1A2030));
        g.fill(new Ellipse2D.Double(cx - 5, cy + 2, 10, 8 + Math.sin(ph * 2) * 2));
        // tiny arms
        g.setColor(new Color(0xEAF6FA));
        g.fill(new Ellipse2D.Double(cx - w - 8, cy + 4 + Math.sin(ph * 2) * 3, 14, 9));
        g.fill(new Ellipse2D.Double(cx + w - 6, cy + 4 - Math.sin(ph * 2) * 3, 14, 9));
    }

    // ================================================================== halloween: glowing skull
    static void skull(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double pulse = 0.75 + 0.25 * Math.sin(ph);
        field(img, (u, v, x, y) -> {
            int c = ramp(v, 0x070A07, 0x0A0E0A, 0x060806);
            double d = Math.hypot(x - 80, (y - 108) * 0.9);
            c = add(c, 0x3AFF4A, Math.exp(-d / 46) * 0.3 * pulse);
            double grid = (x % 16 == 0 || y % 16 == 0) ? 0.05 : 0;
            return add(c, 0x3AFF4A, grid);
        });
        double cx = 80, cy = 104;
        Path2D sk = new Path2D.Double();
        sk.moveTo(cx - 34, cy + 6);
        sk.curveTo(cx - 40, cy - 50, cx + 40, cy - 50, cx + 34, cy + 6);
        sk.curveTo(cx + 34, cy + 20, cx + 22, cy + 22, cx + 20, cy + 34);
        sk.lineTo(cx - 20, cy + 34);
        sk.curveTo(cx - 22, cy + 22, cx - 34, cy + 20, cx - 34, cy + 6);
        sk.closePath();
        g.setPaint(new GradientPaint((float) cx, (float) (cy - 40), new Color(0xF4FFF0), (float) cx, (float) (cy + 34), new Color(0xB8C8B0)));
        g.fill(sk);
        glowStroke(g, sk, 0x6AFF6A, 1.6f, 2.0f, 0.9 * pulse);
        // jaw with teeth
        g.setColor(new Color(0xD8E4D0));
        g.fill(new RoundRectangle2D.Double(cx - 18, cy + 32, 36, 14, 8, 8));
        g.setColor(new Color(0x10160E));
        for (int i = 0; i < 5; i++) g.fill(new java.awt.geom.Rectangle2D.Double(cx - 15 + i * 7, cy + 33, 1.4, 11));
        g.fill(new java.awt.geom.Rectangle2D.Double(cx - 17, cy + 38, 34, 1.2));
        // eye sockets with glowing flames inside
        for (int s = -1; s <= 1; s += 2) {
            double ex = cx + s * 14, ey = cy + 2;
            g.setColor(new Color(0x0A100A));
            g.fill(new Ellipse2D.Double(ex - 10, ey - 9, 20, 18));
            double fl = 0.7 + 0.3 * Math.sin(ph * 3 + s);
            addDot(img, ex, ey, 16, 0x4AFF5A, 0.9 * fl);
            dot(g, ex, ey, 5, 0xEAFFEA, fl);
        }
        Path2D nose = tri(cx - 5, cy + 22, cx + 5, cy + 22, cx, cy + 12);
        g.setColor(new Color(0x0A100A));
        g.fill(nose);
        // cracks
        g.setColor(col(0x5A6A56, 0.9));
        g.setStroke(new BasicStroke(1.2f));
        Path2D cr = new Path2D.Double();
        cr.moveTo(cx + 8, cy - 38); cr.lineTo(cx + 12, cy - 28); cr.lineTo(cx + 6, cy - 22); cr.lineTo(cx + 10, cy - 14);
        g.draw(cr);
        // crossed bones
        for (int s = -1; s <= 1; s += 2) {
            AffineTransform o = g.getTransform();
            g.translate(cx, cy + 82);
            g.rotate(s * 0.6);
            g.setColor(new Color(0xD8E4D0));
            g.fill(new RoundRectangle2D.Double(-40, -4, 80, 8, 6, 6));
            for (int e = -1; e <= 1; e += 2) {
                g.fill(new Ellipse2D.Double(e * 40 - 7, -9, 10, 10));
                g.fill(new Ellipse2D.Double(e * 40 - 7, -1, 10, 10));
            }
            g.setTransform(o);
        }
        // rising green wisps
        Random rn = new Random(131);
        double tt = (double) k / n;
        for (int i = 0; i < 26; i++) {
            double x = rn.nextDouble() * FW, y0 = rn.nextDouble();
            double y = FH - ((y0 + tt) % 1) * FH;
            dot(g, x + Math.sin(ph + i) * 4, y, 2.2, 0x6AFF6A, 0.6);
        }
    }

    // ================================================================== dark neon lightning capes
    static void neonBolt(Graphics2D g, BufferedImage img, double ph, int k, int n, int neon) {
        final double pulse = 0.7 + 0.3 * Math.sin(ph);
        final boolean flick = k == 5 || k == 11;
        field(img, (u, v, x, y) -> {
            int c = ramp(v, 0x0C0C12, 0x09090E, 0x060609);
            // subtle carbon weave
            double wv = ((x / 4 + y / 4) % 2 == 0) ? 1.06 : 0.94;
            c = shade(c, wv);
            double d = Math.hypot(x - 80, y - 118);
            return add(c, neon, Math.exp(-d / 60) * 0.18 * pulse);
        });
        // glowing inset edge with a running light
        RoundRectangle2D edge = new RoundRectangle2D.Double(13, 13, FW - 26, FH - 26, 10, 10);
        glowStroke(g, edge, neon, 1.6f, 1.6f, 0.9 * pulse);
        double per = 2 * (FW - 26) + 2 * (FH - 26), pos = (ph / (Math.PI * 2)) * per;
        for (int s = 0; s < 2; s++) {
            double q = (pos + s * per / 2) % per;
            double x, y;
            double w = FW - 26, h = FH - 26;
            if (q < w) { x = 13 + q; y = 13; } else if (q < w + h) { x = 13 + w; y = 13 + q - w; } else if (q < 2 * w + h) { x = 13 + w - (q - w - h); y = 13 + h; } else { x = 13; y = 13 + h - (q - 2 * w - h); }
            addDot(img, x, y, 12, neon, 0.9);
            addDot(img, x, y, 4, 0xFFFFFF, 0.9);
        }
        // big lightning bolt
        Path2D b = new Path2D.Double();
        b.moveTo(96, 34); b.lineTo(46, 132); b.lineTo(76, 132); b.lineTo(58, 222); b.lineTo(116, 108); b.lineTo(84, 108); b.lineTo(108, 34); b.closePath();
        double a = flick ? 0.45 : pulse;
        addDot(img, 80, 128, 70, neon, 0.35 * a);
        g.setColor(new Color(mix(0x0A0A10, neon, 0.25)));
        g.fill(b);
        glowStroke(g, b, neon, 2.4f, 2.6f, a);
        g.setPaint(new GradientPaint(60, 40, col(0xFFFFFF, 0.25 * a), 100, 220, col(neon, 0.05)));
        g.fill(b);
        // sparks
        Random rn = new Random(141);
        for (int i = 0; i < 18; i++) {
            double x = 30 + rn.nextDouble() * 100, y = 30 + rn.nextDouble() * 200;
            double tw = Math.pow(Math.max(0, Math.sin(ph * 2 + rn.nextDouble() * 6.28)), 4);
            star(g, x, y, 1.2 + rn.nextDouble(), neon, tw);
        }
    }

    // ================================================================== ember tree with falling sparks
    static void emberTree(Graphics2D g, BufferedImage img, double ph, int k, int n) {
        final double t = (double) k / n;
        field(img, (u, v, x, y) -> {
            int c = ramp(v, 0x120A14, 0x1E0E12, 0x2A1210, 0x160A08);
            c = add(c, 0xFF5A1A, Math.exp(-Math.hypot(x - 80, y - 80) / 60) * 0.3);
            double ground = 0.86 + 0.02 * Math.sin(u * 6);
            if (v > ground) c = mix(0x1A0A06, 0x0A0403, (v - ground) * 5);
            return c;
        });
        Random rn = new Random(151);
        // trunk + branches (recursive)
        g.setColor(new Color(0x0A0506));
        java.util.List<double[]> tips = new java.util.ArrayList<>();
        branch(g, rn, 80, FH * 0.88, -Math.PI / 2, 40, 11, 0, tips);
        // glowing ember leaves at branch tips (breathing glow)
        for (int i = 0; i < tips.size(); i++) {
            double[] tp = tips.get(i);
            double a = 0.6 + 0.4 * Math.sin(ph + i * 0.7);
            addDot(img, tp[0], tp[1], 9, 0xFF6A1A, 0.5 * a);
            dot(g, tp[0], tp[1], 3.2, i % 3 == 0 ? 0xFFE07A : 0xFF8A2A, 0.9 * a);
        }
        // falling ember sparks
        for (int i = 0; i < 46; i++) {
            double[] tp = tips.get(rn.nextInt(tips.size()));
            double off = rn.nextDouble(), sp = 1 + rn.nextInt(2);
            double life = (off + t * sp) % 1;
            double x = tp[0] + Math.sin(life * 6 + i) * 8 + life * 10 * (rn.nextBoolean() ? 1 : -1);
            double y = tp[1] + life * (FH * 0.9 - tp[1]);
            double al = Math.sin(Math.PI * life);
            dot(g, x, y, 2.2, 0xFFA040, 0.9 * al);
            dot(g, x, y, 0.9, 0xFFFFE0, al);
        }
        // glow pool at the roots
        addDot(img, 80, FH * 0.88, 40, 0xFF5A1A, 0.3 + 0.1 * Math.sin(ph));
    }

    private static void branch(Graphics2D g, Random rn, double x, double y, double ang, double len, double w, int depth, java.util.List<double[]> tips) {
        double x1 = x + Math.cos(ang) * len, y1 = y + Math.sin(ang) * len;
        g.setStroke(new BasicStroke((float) w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D p = new Path2D.Double();
        p.moveTo(x, y);
        p.quadTo((x + x1) / 2 + (rn.nextDouble() - 0.5) * len * 0.3, (y + y1) / 2, x1, y1);
        g.setColor(new Color(0x0A0506));
        g.draw(p);
        if (depth < 3) {
            g.setColor(col(0xFF6A1A, 0.25));
            g.setStroke(new BasicStroke((float) Math.max(0.6, w * 0.2)));
            g.draw(AffineTransform.getTranslateInstance(w * 0.25, 0).createTransformedShape(p));
        }
        if (depth >= 5 || len < 6) { tips.add(new double[]{x1, y1}); return; }
        int kids = depth == 0 ? 3 : 2;
        for (int i = 0; i < kids; i++) {
            double da = (i - (kids - 1) / 2.0) * (0.55 + rn.nextDouble() * 0.3) + (rn.nextDouble() - 0.5) * 0.3;
            branch(g, rn, x1, y1, ang + da, len * (0.68 + rn.nextDouble() * 0.12), w * 0.66, depth + 1, tips);
        }
        if (depth >= 3) tips.add(new double[]{x1, y1});
    }
}
