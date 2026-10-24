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
 * Halloween decoration kit for the whole client UI: floating embers, fog, bats, cobwebs,
 * pumpkins, witch hat, lanterns, tombstone buttons, spooky stripes.
 * All sprites are painted once (Java2D) and tinted on the GPU.
 */
public final class Halloween {
    private Halloween() { }

    public static final int ORANGE = 0xFFFF6B1A, PUMPKIN = 0xFFE84E0E, PURPLE = 0xFF8B2FC6,
            DARK_PURPLE = 0xFF4A1A6B, TOXIC = 0xFF3AE85D, BLOOD = 0xFFC41E1E, BONE = 0xFFE8DCC8;
    public static final int[] LANTERN_COLS = {0xFFFF6B1A, 0xFF8B2FC6, 0xFF3AE85D, 0xFFFF3B4E, 0xFFFFC94A};

    public static boolean on() { return UiSettings.halloween; }

    // ------------------------------------------------------------------ countdown
    public static String countdown() {
        java.time.LocalDate d = java.time.LocalDate.now();
        if (d.getMonthValue() == 10 && d.getDayOfMonth() == 31) return "Happy Halloween!";
        int y = d.getYear();
        java.time.LocalDate hw = java.time.LocalDate.of(
                d.getMonthValue() > 10 ? y + 1 : y, 10, 31);
        long n = java.time.temporal.ChronoUnit.DAYS.between(d, hw);
        return n == 1 ? "Morgen ist Halloween!" : "Noch " + n + " Tage bis Halloween";
    }

    // ------------------------------------------------------------------ particles (embers rising)
    private static final int N = 200;
    private static final float[] fx = new float[N], fy = new float[N], fz = new float[N], fs = new float[N];
    private static boolean seeded;

    private static void seed() {
        Random r = new Random(13);
        for (int i = 0; i < N; i++) { fx[i] = r.nextFloat(); fy[i] = r.nextFloat(); fz[i] = 0.25f + r.nextFloat() * 0.75f; fs[i] = r.nextFloat() * 100; }
        seeded = true;
    }

    /** Full-screen Halloween backdrop: purple fog, moon, embers, bats. */
    public static void backdrop(int W, int H, float a) {
        if (!on() || a <= 0.01f) return;
        Gx.pushAlpha(a);
        Gx.gradientV(0, 0, W, H, 0x9A180828, 0x7A0A0618);
        float t = (System.currentTimeMillis() % 1_000_000L) / 1000f;
        // moon
        int mx = Math.round(W * 0.82f), my = Math.round(H * 0.12f), mr = Math.round(Math.min(W, H) * 0.06f);
        Gx.glow(mx, my, mr * 4, Gx.withAlpha(0xFFF0E0B0, 0.15f));
        Gx.glow(mx, my, mr * 2, Gx.withAlpha(0xFFF8F0D0, 0.30f));
        Gx.Img moon = Gx.painted("hw_moon", 64, 64, Halloween::paintMoon);
        if (moon != null) Gx.image(moon, mx - mr, my - mr, mr * 2, mr * 2, 0xFFFFFFFF);
        fog(W, H);
        embers(W, H, 1f);
        bats(W, H, t);
        Gx.popAlpha();
    }

    /** Rising embers / dust motes (used in backdrop and over other views). */
    public static void embers(int W, int H, float a) {
        if (!on()) return;
        if (!seeded) seed();
        float dt = Math.min(0.05f, Gx.dt), t = (System.currentTimeMillis() % 1_000_000L) / 1000f;
        Gx.Img dot = Gx.painted("hw_ember", 32, 32, Halloween::paintEmber);
        if (dot == null) return;
        int S = Math.max(1, Math.min(W, H) / 540);
        for (int i = 0; i < N; i++) {
            float z = fz[i];
            fy[i] -= dt * (0.02f + 0.04f * z);
            fx[i] += dt * (0.015f * (float) Math.sin(t * 0.5 + fs[i]) * z);
            if (fy[i] < -0.03f) { fy[i] = 1.03f; fx[i] = (fx[i] * 7.31f + 0.37f) % 1f; }
            if (fx[i] > 1.02f) fx[i] -= 1.04f;
            int d = Math.max(2, Math.round((1.0f + 2.8f * z * z) * S));
            int x = Math.round(fx[i] * W), y = Math.round(fy[i] * H);
            float al = (0.25f + 0.5f * z) * a;
            int col = z > 0.7f ? ORANGE : (z > 0.4f ? 0xFFE8A040 : PURPLE);
            Gx.image(dot, x - d, y - d, d * 2, d * 2, Gx.withAlpha(col, al));
        }
    }

    private static void fog(int W, int H) {
        Gx.Img d = Gx.painted("hw_fog", 1024, 160, Halloween::paintFog);
        if (d == null) return;
        int h = Math.round(H * 0.18f);
        float t = (System.currentTimeMillis() % 1_000_000L) / 1000f;
        float sway = (float) Math.sin(t * 0.1) * W * 0.02f;
        Gx.image(d, Math.round(-W * 0.05f + sway), H - h, Math.round(W * 1.1f), h, Gx.withAlpha(0xFFFFFFFF, 0.7f));
    }

    private static void bats(int W, int H, float t) {
        Gx.Img bat = Gx.painted("hw_bat", 64, 32, Halloween::paintBat);
        if (bat == null) return;
        for (int i = 0; i < 6; i++) {
            float phase = i * 1.7f;
            float bx = W * (0.1f + 0.8f * ((t * 0.03f + i * 0.17f) % 1f));
            float by = H * (0.08f + 0.15f * (float) Math.sin(t * 0.8 + phase));
            float sz = Math.min(W, H) * (0.02f + 0.01f * (float) Math.sin(phase));
            float wing = 0.7f + 0.3f * (float) Math.sin(t * 6 + phase);
            int bw = Math.round(sz * 2 * wing), bh = Math.round(sz);
            Gx.image(bat, Math.round(bx - bw / 2f), Math.round(by - bh / 2f), bw, bh, Gx.withAlpha(0xFF1A0A2A, 0.6f));
        }
    }

    // ------------------------------------------------------------------ panel decorations

    /** Cobweb on the top-left corner of a panel. */
    public static void cobweb(int x, int y, int w, float P) {
        if (!on()) return;
        Gx.Img web = Gx.painted("hw_web", 128, 128, Halloween::paintCobweb);
        if (web == null) return;
        int s = Math.round(P * 22);
        Gx.image(web, x - Math.round(s * 0.1f), y - Math.round(s * 0.1f), s, s, Gx.withAlpha(0xFFFFFFFF, 0.6f));
    }

    /** Hanging lanterns between two points (replaces fairy lights). */
    public static void lanterns(int x0, int y0, int x1, float P, float sag, int seed) {
        if (!on()) return;
        float t = (System.currentTimeMillis() % 1_000_000L) / 1000f;
        int n = Math.max(3, Math.round((x1 - x0) / (P * 40)));
        Gx.Img lantern = Gx.painted("hw_lantern", 32, 48, Halloween::paintLantern);
        int wire = Math.max(1, Math.round(P * 0.6f));
        int seg = n * 4;
        for (int i = 0; i < seg; i++) {
            float u0 = i / (float) seg, u1 = (i + 1) / (float) seg;
            float sx0 = x0 + (x1 - x0) * u0, sx1 = x0 + (x1 - x0) * u1;
            float k0 = (float) Math.sin(u0 * n * Math.PI), k1 = (float) Math.sin(u1 * n * Math.PI);
            float yy0 = y0 + Math.abs(k0) * sag, yy1 = y0 + Math.abs(k1) * sag;
            int steps = Math.max(1, Math.round(sx1 - sx0));
            int st = Math.max(2, wire * 5);
            for (int s = 0; s < steps; s += st) {
                float f = (s + st * 0.5f) / (float) steps;
                Gx.fill(Math.round(sx0 + s), Math.round(yy0 + (yy1 - yy0) * Math.min(1, f)), Math.min(st, steps - s), wire, 0xFF2A1540);
            }
        }
        for (int i = 0; i < n; i++) {
            float u = (i + 0.5f) / n;
            int bx = Math.round(x0 + (x1 - x0) * u);
            int by = Math.round(y0 + sag);
            int col = LANTERN_COLS[(i + seed) % LANTERN_COLS.length];
            float tw = 0.5f + 0.5f * (float) Math.sin(t * (1.8 + (i % 3) * 0.5) + i * 2.1 + seed);
            int gr = Math.round(P * (8 + 5 * tw));
            Gx.glow(bx, by + Math.round(P * 3), gr, Gx.withAlpha(col, 0.4f * tw));
            if (lantern != null) {
                int bw = Math.round(P * 5f), bh = Math.round(P * 7.5f);
                Gx.image(lantern, bx - bw / 2, by, bw, bh, Gx.mix(col, 0xFFFFFFFF, 0.2f * tw));
            }
        }
    }

    /** Witch hat sitting on something at (cx, top). */
    public static void witchHat(float cx, float top, float w, float tilt) {
        if (!on()) return;
        Gx.Img h = Gx.painted("hw_witch", 128, 128, Halloween::paintWitchHat);
        if (h == null) return;
        int s = Math.round(w);
        Gx.image(h, Math.round(cx - s * 0.5f + tilt), Math.round(top - s * 0.78f), s, s, 0xFFFFFFFF);
    }

    /** Jack-o-lantern pumpkin (replaces bauble). */
    public static void pumpkin(float cx, float cy, float r) {
        Gx.Img p = Gx.painted("hw_pumpkin", 64, 64, Halloween::paintPumpkin);
        if (p == null) return;
        int d = Math.round(r * 2);
        Gx.image(p, Math.round(cx - r), Math.round(cy - r), d, d, 0xFFFFFFFF);
    }

    /** Tombstone-style button face (replaces gift box). */
    public static void tombstone(int x, int y, int w, int h, int body, int detail, float lift, float P) {
        int r = Math.round(P * 7);
        int yy = Math.round(y - lift);
        Gx.shadow(x, yy, w, h, r, Math.round(P * (5 + lift * 0.6f)), 0.55f);
        int tr = Math.min(r, Math.round(w * 0.45f));
        Gx.rectV(x, yy, w, h, tr, Gx.mix(body, 0xFFFFFFFF, 0.08f), Gx.mix(body, 0xFF000000, 0.3f));
        int rb = Math.max(2, Math.round(w * 0.06f));
        Gx.fill(x + w / 2 - rb / 2, yy + Math.round(h * 0.15f), rb, Math.round(h * 0.55f), detail);
        Gx.fill(x + Math.round(w * 0.25f), yy + Math.round(h * 0.3f), Math.round(w * 0.5f), rb, detail);
        Gx.outline(x, yy, w, h, tr, Math.max(1, Math.round(P * 0.6f)), 0x22FFFFFF);
    }

    /** Spooky orange/purple stripes inside a pill (for switches). */
    public static void spookyStripes(int x, int y, int w, int h, float shift) {
        final int W = Math.max(8, w), H = Math.max(4, h);
        Gx.Img c = Gx.painted("hw_stripe" + W + "x" + H, W * 2, H * 2, g -> paintSpookyStripes(g, W * 2, H * 2));
        if (c == null) return;
        Gx.image(c, x, y, w, h, 0xFFFFFFFF);
    }

    // ================================================================== painters

    private static void paintEmber(Graphics2D g) {
        g.setPaint(new RadialGradientPaint(new Point2D.Float(16, 16), 16,
                new float[]{0f, 0.3f, 1f},
                new Color[]{new Color(255, 200, 80, 255), new Color(255, 120, 40, 180), new Color(200, 60, 20, 0)}));
        g.fill(new Ellipse2D.Float(0, 0, 32, 32));
    }

    private static void paintMoon(Graphics2D g) {
        g.setPaint(new RadialGradientPaint(new Point2D.Float(32, 32), 30,
                new float[]{0f, 0.5f, 0.85f, 1f},
                new Color[]{new Color(255, 248, 220), new Color(244, 232, 192), new Color(200, 180, 140, 180), new Color(160, 140, 100, 0)}));
        g.fill(new Ellipse2D.Float(2, 2, 60, 60));
        g.setColor(new Color(200, 185, 145, 50));
        g.fill(new Ellipse2D.Float(18, 14, 8, 8));
        g.fill(new Ellipse2D.Float(35, 24, 12, 10));
        g.fill(new Ellipse2D.Float(22, 36, 6, 6));
    }

    private static void paintFog(Graphics2D g) {
        for (int layer = 0; layer < 3; layer++) {
            Path2D p = new Path2D.Double();
            p.moveTo(0, 160);
            double base = layer == 0 ? 50 : (layer == 1 ? 80 : 110);
            for (int x = 0; x <= 1024; x += 8) {
                double y = base + Math.sin(x / (120.0 + layer * 40) + layer * 1.5) * 18 + Math.sin(x / 50.0 + layer * 3) * 8;
                p.lineTo(x, y);
            }
            p.lineTo(1024, 160);
            p.closePath();
            int a = layer == 0 ? 40 : (layer == 1 ? 70 : 100);
            g.setPaint(new GradientPaint(0, (float) base - 10, new Color(60, 20, 80, a / 2), 0, 160, new Color(30, 10, 50, a)));
            g.fill(p);
        }
    }

    private static void paintBat(Graphics2D g) {
        g.setColor(new Color(20, 8, 30));
        g.fill(new Ellipse2D.Float(26, 10, 12, 14));
        GeneralPath lw = new GeneralPath();
        lw.moveTo(28, 14); lw.curveTo(16, 6, 4, 8, 0, 4); lw.curveTo(6, 16, 12, 20, 28, 18); lw.closePath();
        g.fill(lw);
        GeneralPath rw = new GeneralPath();
        rw.moveTo(36, 14); rw.curveTo(48, 6, 60, 8, 64, 4); rw.curveTo(58, 16, 52, 20, 36, 18); rw.closePath();
        g.fill(rw);
        g.setColor(new Color(255, 100, 30));
        g.fill(new Ellipse2D.Float(28, 13, 4, 4));
        g.fill(new Ellipse2D.Float(34, 13, 4, 4));
        g.setColor(new Color(20, 8, 30));
        g.fill(new java.awt.Polygon(new int[]{28, 26, 30}, new int[]{11, 4, 8}, 3));
        g.fill(new java.awt.Polygon(new int[]{36, 34, 38}, new int[]{11, 4, 8}, 3));
    }

    private static void paintCobweb(Graphics2D g) {
        g.setColor(new Color(220, 220, 230, 160));
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int rays = 8;
        double[] angles = new double[rays];
        for (int i = 0; i < rays; i++) {
            angles[i] = Math.PI / 2 * i / (rays - 1);
            int ex = (int) (Math.cos(angles[i]) * 120), ey = (int) (Math.sin(angles[i]) * 120);
            g.drawLine(0, 0, ex, ey);
        }
        for (int ring = 1; ring <= 5; ring++) {
            float r = ring * 22f;
            GeneralPath path = new GeneralPath();
            boolean first = true;
            for (int i = 0; i < rays; i++) {
                float dr = r + (float) Math.sin(i * 2.3 + ring) * 4;
                int px = (int) (Math.cos(angles[i]) * dr), py = (int) (Math.sin(angles[i]) * dr);
                if (first) { path.moveTo(px, py); first = false; } else path.lineTo(px, py);
            }
            g.setColor(new Color(220, 220, 230, 100 + ring * 10));
            g.draw(path);
        }
    }

    private static void paintWitchHat(Graphics2D g) {
        g.setPaint(new GradientPaint(20, 100, new Color(30, 10, 50), 110, 100, new Color(50, 20, 70)));
        g.fill(new Ellipse2D.Float(6, 88, 116, 30));
        GeneralPath cone = new GeneralPath();
        cone.moveTo(20, 98); cone.curveTo(30, 60, 50, 20, 72, 8); cone.curveTo(90, 20, 98, 50, 108, 98); cone.closePath();
        g.setPaint(new GradientPaint(40, 10, new Color(60, 20, 90), 90, 98, new Color(30, 8, 50)));
        g.fill(cone);
        g.setColor(new Color(200, 160, 50));
        g.fillRect(48, 80, 32, 12);
        g.setColor(new Color(240, 200, 80));
        g.fillRect(56, 78, 16, 16);
        g.setColor(new Color(60, 20, 90));
        g.fillRect(60, 82, 8, 8);
        GeneralPath tip = new GeneralPath();
        tip.moveTo(72, 8); tip.curveTo(78, 2, 95, 10, 98, 28); tip.curveTo(88, 16, 80, 12, 72, 8);
        g.setPaint(new GradientPaint(72, 8, new Color(60, 20, 90), 95, 30, new Color(40, 12, 65)));
        g.fill(tip);
    }

    private static void paintPumpkin(Graphics2D g) {
        g.setPaint(new RadialGradientPaint(new Point2D.Float(32, 34), 28,
                new float[]{0f, 0.5f, 1f},
                new Color[]{new Color(255, 140, 20), new Color(230, 100, 10), new Color(180, 60, 0)}));
        g.fill(new Ellipse2D.Float(6, 14, 24, 36));
        g.fill(new Ellipse2D.Float(16, 10, 30, 44));
        g.fill(new Ellipse2D.Float(34, 14, 24, 36));
        g.setPaint(new RadialGradientPaint(new Point2D.Float(32, 32), 24,
                new float[]{0f, 0.7f, 1f},
                new Color[]{new Color(255, 160, 30), new Color(240, 110, 15), new Color(200, 70, 5)}));
        g.fill(new Ellipse2D.Float(12, 12, 40, 40));
        g.setColor(new Color(20, 10, 0));
        g.fill(new java.awt.Polygon(new int[]{22, 20, 28}, new int[]{24, 32, 30}, 3));
        g.fill(new java.awt.Polygon(new int[]{42, 36, 44}, new int[]{24, 32, 30}, 3));
        GeneralPath mouth = new GeneralPath();
        mouth.moveTo(20, 38); mouth.lineTo(24, 36); mouth.lineTo(28, 40); mouth.lineTo(32, 36);
        mouth.lineTo(36, 40); mouth.lineTo(40, 36); mouth.lineTo(44, 38);
        mouth.lineTo(42, 44); mouth.lineTo(22, 44); mouth.closePath();
        g.fill(mouth);
        g.setPaint(new RadialGradientPaint(new Point2D.Float(32, 34), 18,
                new float[]{0f, 0.5f, 1f},
                new Color[]{new Color(255, 200, 50, 60), new Color(255, 150, 20, 30), new Color(255, 100, 0, 0)}));
        g.fill(new Ellipse2D.Float(14, 20, 36, 30));
        g.setColor(new Color(80, 120, 40));
        g.fillRoundRect(30, 6, 6, 10, 3, 3);
    }

    private static void paintLantern(Graphics2D g) {
        g.setColor(new Color(80, 80, 80));
        g.setStroke(new BasicStroke(2f));
        g.drawArc(8, 0, 16, 12, 0, 180);
        g.setPaint(new RadialGradientPaint(new Point2D.Float(16, 28), 18,
                new float[]{0f, 0.4f, 1f},
                new Color[]{new Color(255, 255, 255), new Color(240, 240, 240, 230), new Color(180, 180, 180, 200)}));
        g.fill(new Ellipse2D.Float(4, 12, 24, 34));
        g.setPaint(new RadialGradientPaint(new Point2D.Float(16, 26), 10,
                new float[]{0f, 1f},
                new Color[]{new Color(255, 255, 255, 180), new Color(255, 255, 255, 0)}));
        g.fill(new Ellipse2D.Float(6, 14, 20, 30));
    }

    private static void paintSpookyStripes(Graphics2D g, int w, int h) {
        RoundRectangle2D pill = new RoundRectangle2D.Float(0, 0, w, h, h, h);
        g.setClip(pill);
        g.setColor(new Color(0x8B, 0x2F, 0xC6));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(0xFF, 0x6B, 0x1A));
        int s = Math.max(6, h / 2);
        for (int x = -h; x < w + h; x += s * 2) {
            GeneralPath p = new GeneralPath();
            p.moveTo(x, h); p.lineTo(x + s, h); p.lineTo(x + s + h, 0); p.lineTo(x + h, 0); p.closePath();
            g.fill(p);
        }
        g.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 70), 0, h, new Color(0, 0, 0, 80)));
        g.fillRect(0, 0, w, h);
    }
}
