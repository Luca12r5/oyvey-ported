package dev.lego.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

/**
 * Christmas decoration kit for the whole client UI: snowfall, aurora sky, fairy lights, snow caps on panels,
 * Santa hat, gift boxes, candy cane stripes, baubles. All sprites are painted once (Java2D) and tinted on the GPU.
 */
public final class Xmas {
    private Xmas() { }

    public static final int RED = 0xFFE3263B, RED_DARK = 0xFF9E1024, GOLD = 0xFFF4C152, GREEN = 0xFF1FA35C, GREEN_DARK = 0xFF0E5E34,
            ICE = 0xFFBFE3FF, SNOW = 0xFFF4F8FF, NAVY = 0xFF0B1426;
    public static final int[] BULBS = {0xFFFF3B4E, 0xFFFFC94A, 0xFF3BE08A, 0xFF4AA8FF, 0xFFFF7BD5};

    /** Decorations on/off (design page). */
    public static boolean on() { return UiSettings.xmas; }

    // ------------------------------------------------------------------ countdown
    public static String countdown() {
        java.time.LocalDate d = java.time.LocalDate.now();
        int y = d.getYear();
        if (d.getMonthValue() == 12 && d.getDayOfMonth() >= 24 && d.getDayOfMonth() <= 26) return "Frohe Weihnachten!";
        java.time.LocalDate eve = java.time.LocalDate.of(d.getMonthValue() == 12 && d.getDayOfMonth() > 26 ? y + 1 : y, 12, 24);
        long n = java.time.temporal.ChronoUnit.DAYS.between(d, eve);
        return n == 1 ? "Morgen ist Heiligabend!" : "Noch " + n + " Tage bis Heiligabend";
    }

    // ------------------------------------------------------------------ snowfall
    private static final int N = 260;
    private static final float[] fx = new float[N], fy = new float[N], fz = new float[N], fs = new float[N];
    private static boolean seeded;

    private static void seed() {
        Random r = new Random(7);
        for (int i = 0; i < N; i++) { fx[i] = r.nextFloat(); fy[i] = r.nextFloat(); fz[i] = 0.25f + r.nextFloat() * 0.75f; fs[i] = r.nextFloat() * 100; }
        seeded = true;
    }

    /** Full-screen winter backdrop: navy tint, aurora, falling snow, snow drifts at the bottom. */
    public static void backdrop(int W, int H, float a) {
        if (!on() || a <= 0.01f) return;
        Gx.pushAlpha(a);
        Gx.gradientV(0, 0, W, H, 0x9A061022, 0x7A0B1A30);
        float t = (System.currentTimeMillis() % 1_000_000L) / 1000f;
        Gx.Img au = Gx.painted("xm_aurora", 512, 256, Xmas::paintAurora);
        if (au != null) {
            int ah = H / 2;
            float sway = (float) Math.sin(t * 0.15) * W * 0.03f;
            Gx.image(au, Math.round(-W * 0.1f + sway), -ah / 8, Math.round(W * 1.2f), ah, Gx.withAlpha(0xFFFFFFFF, 0.55f + 0.15f * (float) Math.sin(t * 0.4)));
        }
        drift(W, H);
        snow(W, H, 1f);
        Gx.popAlpha();
    }

    /** Falling snow only (also used over the HUD editor / games). */
    public static void snow(int W, int H, float a) {
        if (!on()) return;
        if (!seeded) seed();
        float dt = Math.min(0.05f, Gx.dt), t = (System.currentTimeMillis() % 1_000_000L) / 1000f;
        Gx.Img dot = Gx.painted("xm_flake", 32, 32, Xmas::paintFlake);
        Gx.Img cr = Gx.painted("xm_crystal", 64, 64, Xmas::paintCrystal);
        if (dot == null) return;
        int S = Math.max(1, Math.min(W, H) / 540);
        for (int i = 0; i < N; i++) {
            float z = fz[i];
            fy[i] += dt * (0.035f + 0.07f * z);
            fx[i] += dt * (0.01f * (float) Math.sin(t * 0.7 + fs[i]) * z + 0.004f);
            if (fy[i] > 1.03f) { fy[i] = -0.03f; fx[i] = (fx[i] * 7.31f + 0.37f) % 1f; }
            if (fx[i] > 1.02f) fx[i] -= 1.04f;
            int d = Math.max(2, Math.round((1.2f + 3.6f * z * z) * S));
            int x = Math.round(fx[i] * W), y = Math.round(fy[i] * H);
            float al = (0.35f + 0.6f * z) * a;
            if (z > 0.93f && cr != null) {
                int c = d * 3;
                Gx.image(cr, x - c / 2, y - c / 2, c, c, Gx.withAlpha(0xFFFFFFFF, al));
            } else Gx.image(dot, x - d, y - d, d * 2, d * 2, Gx.withAlpha(0xFFFFFFFF, al));
        }
    }

    private static void drift(int W, int H) {
        Gx.Img d = Gx.painted("xm_drift", 1024, 160, Xmas::paintDrift);
        if (d == null) return;
        int h = Math.round(H * 0.14f);
        Gx.image(d, 0, H - h, W, h, 0xFFFFFFFF);
    }

    // ------------------------------------------------------------------ panel decorations
    /** Snow lying on top of a panel (x..x+w at its top edge y), with icicles. */
    public static void snowCap(int x, int y, int w, float P) {
        if (!on()) return;
        Gx.Img cap = Gx.painted("xm_cap", 512, 64, Xmas::paintCap);
        if (cap == null) return;
        int h = Math.round(P * 17);
        int tile = h * 8;
        int r = Math.round(P * 14);
        for (int cx = x + r / 2; cx < x + w - r / 2; cx += tile) {
            int tw = Math.min(tile, x + w - r / 2 - cx);
            Gx.push();
            Gx.clip(cx, y - h / 2, tw, h * 2);
            Gx.image(cap, cx, y - Math.round(h * 0.42f), tile, h, 0xFFFFFFFF);
            Gx.unclip();
            Gx.pop();
        }
    }

    /** String of twinkling fairy lights hanging between two points. */
    public static void lights(int x0, int y0, int x1, float P, float sag, int seed) {
        if (!on()) return;
        float t = (System.currentTimeMillis() % 1_000_000L) / 1000f;
        int n = Math.max(3, Math.round((x1 - x0) / (P * 34)));
        Gx.Img bulb = Gx.painted("xm_bulb", 32, 48, Xmas::paintBulb);
        int wire = Math.max(1, Math.round(P * 0.8f));
        // wire (catenary-ish)
        int seg = n * 4;
        for (int i = 0; i < seg; i++) {
            float u0 = i / (float) seg, u1 = (i + 1) / (float) seg;
            float sx0 = x0 + (x1 - x0) * u0, sx1 = x0 + (x1 - x0) * u1;
            float k0 = (float) Math.sin(u0 * n * Math.PI), k1 = (float) Math.sin(u1 * n * Math.PI);
            float yy0 = y0 + Math.abs(k0) * sag, yy1 = y0 + Math.abs(k1) * sag;
            int steps = Math.max(1, Math.round(sx1 - sx0));
            int st = Math.max(2, wire * 5);           // short horizontal dashes instead of one rect per pixel (perf)
            for (int s = 0; s < steps; s += st) {
                float f = (s + st * 0.5f) / (float) steps;
                Gx.fill(Math.round(sx0 + s), Math.round(yy0 + (yy1 - yy0) * Math.min(1, f)), Math.min(st, steps - s), wire, 0xFF1D3A24);
            }
        }
        for (int i = 0; i < n; i++) {
            float u = (i + 0.5f) / n;
            int bx = Math.round(x0 + (x1 - x0) * u);
            int by = Math.round(y0 + sag);
            int col = BULBS[(i + seed) % BULBS.length];
            float tw = 0.55f + 0.45f * (float) Math.sin(t * (2.1 + (i % 3) * 0.7) + i * 1.9 + seed);
            int gr = Math.round(P * (9 + 4 * tw));
            Gx.glow(bx, by + Math.round(P * 4), gr, Gx.withAlpha(col, 0.45f * tw));
            if (bulb != null) {
                int bw = Math.round(P * 5.2f), bh = Math.round(P * 7.8f);
                Gx.image(bulb, bx - bw / 2, by, bw, bh, Gx.mix(col, 0xFFFFFFFF, 0.25f * tw));
            }
        }
    }

    /** A Santa hat sitting on something at (cx, top) with the given width. */
    public static void hat(float cx, float top, float w, float tilt) {
        if (!on()) return;
        Gx.Img h = Gx.painted("xm_hat", 128, 128, Xmas::paintHat);
        if (h == null) return;
        int s = Math.round(w);
        Gx.image(h, Math.round(cx - s * 0.46f + tilt), Math.round(top - s * 0.72f), s, s, 0xFFFFFFFF);
    }

    /** Christmas bauble. */
    public static void bauble(float cx, float cy, float r, int color) {
        Gx.Img b = Gx.painted("xm_bauble", 64, 64, Xmas::paintBauble);
        if (b == null) return;
        int d = Math.round(r * 2);
        Gx.image(b, Math.round(cx - r), Math.round(cy - r), d, d, color);
    }

    /** Gift box button face (box, ribbon, bow). */
    public static void gift(int x, int y, int w, int h, int box, int ribbon, float lift, float P) {
        int r = Math.round(P * 7);
        int yy = Math.round(y - lift);
        Gx.shadow(x, yy, w, h, r, Math.round(P * (5 + lift * 0.6f)), 0.45f);
        Gx.rectV(x, yy, w, h, r, Gx.mix(box, 0xFFFFFFFF, 0.12f), Gx.mix(box, 0xFF000000, 0.25f));
        int rb = Math.max(2, Math.round(w * 0.14f));
        Gx.fill(x + w / 2 - rb / 2, yy, rb, h, ribbon);
        Gx.fill(x, yy + Math.round(h * 0.3f) - rb / 2, w, rb, ribbon);
        Gx.fill(x + w / 2 - rb / 2, yy, Math.max(1, rb / 4), h, Gx.withAlpha(0xFFFFFFFF, 0.25f));
        Gx.Img bow = Gx.painted("xm_bow", 128, 72, Xmas::paintBow);
        if (bow != null) {
            int bw = Math.round(w * 0.62f), bh = bw * 72 / 128;
            Gx.image(bow, x + w / 2 - bw / 2, yy + Math.round(h * 0.3f) - bh + Math.round(bh * 0.35f), bw, bh, ribbon);
        }
        Gx.outline(x, yy, w, h, r, Math.max(1, Math.round(P * 0.6f)), 0x22FFFFFF);
    }

    /** Candy cane stripes inside a pill (for switches / progress bars). */
    public static void candy(int x, int y, int w, int h, float shift) {
        final int W = Math.max(8, w), H = Math.max(4, h);
        Gx.Img c = Gx.painted("xm_candy" + W + "x" + H, W * 2, H * 2, g -> paintCandy(g, W * 2, H * 2));
        if (c == null) return;
        Gx.image(c, x, y, w, h, 0xFFFFFFFF);
    }

    // ================================================================== painters
    private static void paintFlake(Graphics2D g) {
        g.setPaint(new RadialGradientPaint(new Point2D.Float(16, 16), 16, new float[]{0f, 0.35f, 1f},
                new Color[]{new Color(255, 255, 255, 255), new Color(235, 245, 255, 190), new Color(220, 235, 255, 0)}));
        g.fill(new Ellipse2D.Float(0, 0, 32, 32));
    }

    private static void paintCrystal(Graphics2D g) {
        g.translate(32, 32);
        g.setColor(new Color(255, 255, 255, 235));
        g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 6; i++) {
            g.drawLine(0, 0, 0, -27);
            g.drawLine(0, -12, -7, -19);
            g.drawLine(0, -12, 7, -19);
            g.drawLine(0, -20, -5, -25);
            g.drawLine(0, -20, 5, -25);
            g.rotate(Math.PI / 3);
        }
        g.setPaint(new RadialGradientPaint(new Point2D.Float(0, 0), 10, new float[]{0f, 1f}, new Color[]{new Color(255, 255, 255, 255), new Color(255, 255, 255, 0)}));
        g.fill(new Ellipse2D.Float(-10, -10, 20, 20));
    }

    private static void paintAurora(Graphics2D g) {
        Random r = new Random(3);
        int[][] cols = {{60, 255, 170}, {80, 200, 255}, {170, 110, 255}};
        for (int band = 0; band < 3; band++) {
            int[] c = cols[band];
            double ph = r.nextDouble() * 6, amp = 20 + band * 8, base = 70 + band * 30;
            for (int x = 0; x < 512; x += 2) {
                double y = base + Math.sin(x / 80.0 + ph) * amp + Math.sin(x / 31.0 + ph * 2) * 8;
                double fade = Math.sin(Math.PI * x / 512.0);
                int a = (int) (70 * fade);
                g.setPaint(new GradientPaint(x, (float) (y - 60), new Color(c[0], c[1], c[2], 0), x, (float) y, new Color(c[0], c[1], c[2], a)));
                g.fillRect(x, (int) (y - 60), 2, 60);
                g.setPaint(new GradientPaint(x, (float) y, new Color(c[0], c[1], c[2], a), x, (float) (y + 14), new Color(c[0], c[1], c[2], 0)));
                g.fillRect(x, (int) y, 2, 14);
            }
        }
    }

    private static void paintDrift(Graphics2D g) {
        for (int layer = 0; layer < 2; layer++) {
            Path2D p = new Path2D.Double();
            p.moveTo(0, 160);
            Random r = new Random(11 + layer);
            double base = layer == 0 ? 70 : 100;
            for (int x = 0; x <= 1024; x += 8) {
                double y = base + Math.sin(x / (140.0 + layer * 60) + layer * 2) * 22 + Math.sin(x / 47.0 + layer) * 6;
                p.lineTo(x, y);
            }
            p.lineTo(1024, 160);
            p.closePath();
            g.setPaint(new GradientPaint(0, (float) base - 20, layer == 0 ? new Color(200, 222, 255, 140) : new Color(244, 248, 255, 235),
                    0, 160, layer == 0 ? new Color(120, 150, 210, 180) : new Color(190, 212, 245, 255)));
            g.fill(p);
            if (layer == 1) {
                g.setColor(new Color(255, 255, 255, 180));
                for (int i = 0; i < 90; i++) { float x = r.nextFloat() * 1024, y = 110 + r.nextFloat() * 50; g.fill(new Ellipse2D.Float(x, y, 2, 2)); }
            }
        }
    }

    private static void paintCap(Graphics2D g) {
        Path2D p = new Path2D.Double();
        p.moveTo(0, 34);
        Random r = new Random(5);
        // soft lumpy top (smooth curves)
        double prev = 10;
        for (int x = 0; x <= 512; x += 32) {
            double y = 6 + Math.sin(x / 61.0) * 4 + r.nextDouble() * 4;
            p.quadTo(x - 16, Math.min(prev, y) - 3, x, y);
            prev = y;
        }
        // bottom edge with icicles
        p.lineTo(512, 34);
        for (int x = 512; x >= 0; x -= 8) {
            boolean drip = x % 56 == 0 || x % 88 == 24;
            if (drip) { double d = 14 + (x * 7 % 16); p.lineTo(x + 3, 35); p.lineTo(x, 34 + d); p.lineTo(x - 3, 35); }
            else p.lineTo(x, 33 + Math.sin(x / 23.0) * 2.5);
        }
        p.closePath();
        g.setColor(new Color(40, 60, 110, 90));
        g.translate(0, 3);
        g.fill(p);
        g.translate(0, -3);
        g.setPaint(new GradientPaint(0, 4, new Color(255, 255, 255), 0, 50, new Color(186, 212, 248)));
        g.fill(p);
        g.setColor(new Color(255, 255, 255, 200));
        g.setStroke(new BasicStroke(2f));
        for (int x = 10; x < 512; x += 37) g.drawLine(x, 12, x + 12, 11);
        g.setColor(new Color(150, 190, 240, 120));
        for (int x = 0; x < 512; x += 3) g.fillRect(x, 30 + (int) (Math.sin(x / 23.0) * 2.5), 3, 2);
    }

    private static void paintBulb(Graphics2D g) {
        g.setColor(new Color(60, 70, 60));
        g.fillRoundRect(10, 0, 12, 10, 3, 3);
        GeneralPath b = new GeneralPath();
        b.moveTo(16, 8);
        b.curveTo(30, 12, 30, 40, 16, 47);
        b.curveTo(2, 40, 2, 12, 16, 8);
        g.setPaint(new RadialGradientPaint(new Point2D.Float(12, 22), 26, new float[]{0f, 0.3f, 1f},
                new Color[]{new Color(255, 255, 255), new Color(255, 255, 255, 240), new Color(200, 200, 200, 230)}));
        g.fill(b);
        g.setColor(new Color(255, 255, 255, 230));
        g.fill(new Ellipse2D.Float(10, 16, 5, 11));
    }

    private static void paintHat(Graphics2D g) {
        // cone
        GeneralPath c = new GeneralPath();
        c.moveTo(18, 92);
        c.curveTo(30, 40, 60, 14, 96, 22);
        c.curveTo(112, 26, 118, 44, 114, 60);
        c.curveTo(104, 50, 92, 48, 88, 58);
        c.curveTo(96, 70, 104, 80, 110, 92);
        c.closePath();
        g.setPaint(new GradientPaint(20, 30, new Color(0xFF4A5A), 100, 90, new Color(0x9E1024)));
        g.fill(c);
        g.setColor(new Color(0, 0, 0, 40));
        g.setStroke(new BasicStroke(3f));
        g.draw(new java.awt.geom.QuadCurve2D.Float(40, 80, 60, 50, 92, 30));
        // pom-pom
        g.setPaint(new RadialGradientPaint(new Point2D.Float(110, 60), 14, new float[]{0f, 1f}, new Color[]{Color.WHITE, new Color(205, 220, 240)}));
        g.fill(new Ellipse2D.Float(98, 50, 26, 26));
        // fur brim
        RoundRectangle2D brim = new RoundRectangle2D.Float(8, 86, 112, 26, 26, 26);
        g.setPaint(new GradientPaint(0, 86, Color.WHITE, 0, 112, new Color(200, 216, 240)));
        g.fill(brim);
        g.setColor(new Color(255, 255, 255, 200));
        Random r = new Random(2);
        for (int i = 0; i < 40; i++) g.fill(new Ellipse2D.Float(10 + r.nextFloat() * 106, 88 + r.nextFloat() * 20, 5, 5));
    }

    private static void paintBauble(Graphics2D g) {
        g.setColor(new Color(200, 170, 80));
        g.fillRoundRect(26, 2, 12, 9, 3, 3);
        g.setPaint(new RadialGradientPaint(new Point2D.Float(24, 26), 36, new float[]{0f, 0.25f, 0.7f, 1f},
                new Color[]{new Color(255, 255, 255), new Color(250, 250, 250), new Color(170, 170, 170), new Color(90, 90, 90)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE));
        g.fill(new Ellipse2D.Float(6, 9, 52, 52));
        g.setColor(new Color(255, 255, 255, 170));
        g.fill(new Ellipse2D.Float(16, 18, 10, 14));
        g.setColor(new Color(255, 255, 255, 90));
        g.setStroke(new BasicStroke(2.2f));
        g.drawArc(10, 26, 44, 14, 180, 180);
    }

    private static void paintBow(Graphics2D g) {
        g.setColor(Color.WHITE);
        GeneralPath l = new GeneralPath();
        l.moveTo(64, 50); l.curveTo(40, 10, 6, 14, 14, 44); l.curveTo(20, 60, 44, 60, 64, 50);
        GeneralPath r = new GeneralPath();
        r.moveTo(64, 50); r.curveTo(88, 10, 122, 14, 114, 44); r.curveTo(108, 60, 84, 60, 64, 50);
        g.fill(l); g.fill(r);
        g.setColor(new Color(0, 0, 0, 60));
        g.setStroke(new BasicStroke(3f));
        g.draw(new java.awt.geom.QuadCurve2D.Float(24, 40, 40, 28, 58, 46));
        g.draw(new java.awt.geom.QuadCurve2D.Float(104, 40, 88, 28, 70, 46));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Float(54, 40, 20, 18));
        g.fill(new java.awt.Polygon(new int[]{58, 48, 58}, new int[]{54, 72, 70}, 3));
        g.fill(new java.awt.Polygon(new int[]{70, 80, 70}, new int[]{54, 72, 70}, 3));
    }

    private static void paintCandy(Graphics2D g, int w, int h) {
        RoundRectangle2D pill = new RoundRectangle2D.Float(0, 0, w, h, h, h);
        g.setClip(pill);
        g.setColor(new Color(0xE3263B));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(255, 255, 255));
        int s = Math.max(6, h / 2);
        for (int x = -h; x < w + h; x += s * 2) {
            GeneralPath p = new GeneralPath();
            p.moveTo(x, h); p.lineTo(x + s, h); p.lineTo(x + s + h, 0); p.lineTo(x + h, 0); p.closePath();
            g.fill(p);
        }
        g.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 90), 0, h, new Color(0, 0, 0, 70)));
        g.fillRect(0, 0, w, h);
    }
}
