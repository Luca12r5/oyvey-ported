package dev.lego.cosmetic;

import static dev.lego.cosmetic.Capes.col;
import static dev.lego.cosmetic.Capes.fbm;
import static dev.lego.cosmetic.Capes.hash;
import static dev.lego.cosmetic.Capes.mix;
import static dev.lego.cosmetic.Capes.ramp;
import static dev.lego.cosmetic.Capes.shade;
import static dev.lego.cosmetic.Tex2.argb;
import static dev.lego.cosmetic.Tex2.img;
import static dev.lego.cosmetic.Tex2.pix;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/** Textures of the Christmas collection ("xm_*"). Most are white based materials tinted with g.color. */
final class XmasTex {
    private XmasTex() { }

    static CTex.T paint(String name) {
        switch (name) {
            // ------------------------------------------------------------ fabrics
            case "xm_felt": return pix(64, 64, (u, v, x, y) -> {
                double n = fbm(u * 10, v * 10, 10, 10, 4, 301);
                return 0xFF000000 | shade(0xFFFFFF, 0.82 + 0.16 * n + 0.05 * (hash(x, y, 302) - 0.5));
            });
            case "xm_velvet": return pix(64, 64, (u, v, x, y) -> {
                double n = fbm(u * 6, v * 6, 6, 6, 3, 311);
                double sheen = 0.5 + 0.5 * Math.sin(u * Math.PI * 2 * 2 + n * 2);
                return 0xFF000000 | shade(0xFFFFFF, 0.72 + 0.2 * n + 0.1 * sheen);
            });
            case "xm_fluff": return pix(64, 64, (u, v, x, y) -> {
                double n = fbm(u * 16, v * 16, 16, 16, 4, 321);
                double curl = 0.5 + 0.5 * Math.sin((u * 30 + n * 6) + Math.cos(v * 30 + n * 5));
                return 0xFF000000 | shade(0xFFFFFF, 0.8 + 0.14 * n + 0.08 * curl);
            });
            case "xm_knit": return knit(-1);
            case "xm_scarf": return knit(0);
            case "xm_sweater": return knit(1);
            case "xm_ribbon": return pix(32, 32, (u, v, x, y) -> {
                double sh = 0.78 + 0.22 * Math.exp(-Math.pow((u - 0.42) / 0.16, 2)) + 0.05 * Math.sin(v * 40);
                double edge = (u < 0.08 || u > 0.92) ? 0.8 : 1;
                return 0xFF000000 | shade(0xFFFFFF, Math.min(1, sh * edge));
            });
            case "xm_silk": return pix(64, 32, (u, v, x, y) -> {
                double s = 0.35 + 0.5 * Math.pow(Math.max(0, Math.sin(u * Math.PI * 2 * 3)), 6) + 0.25 * Math.pow(Math.max(0, Math.sin(u * Math.PI * 2 * 3 + 1.2)), 16);
                return 0xFF000000 | shade(0xFFFFFF, Math.min(1, s + 0.05 * v));
            });
            case "xm_elfcuff": return img(256, 32, g -> {
                g.setPaint(new GradientPaint(0, 0, new Color(0xE8323A), 0, 32, new Color(0x9A1218)));
                g.fillRect(0, 0, 256, 32);
                g.setColor(new Color(0xFFD86A));
                g.fillRect(0, 0, 256, 3);
                g.fillRect(0, 29, 256, 3);
                Path2D z = new Path2D.Double();
                z.moveTo(0, 22);
                for (int i = 0; i <= 16; i++) z.lineTo(i * 16, i % 2 == 0 ? 22 : 9);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(z);
                g.setColor(new Color(0x2E9A48));
                for (int i = 0; i < 16; i++) g.fill(new Ellipse2D.Double(i * 16 + 5.5, (i % 2 == 0) ? 7 : 17, 5, 5));
            });
            case "xm_sack": return pix(64, 64, (u, v, x, y) -> {
                boolean wx = (x / 2 + y / 2) % 2 == 0;
                double n = fbm(u * 8, v * 8, 8, 8, 3, 331);
                double m = (wx ? 0.92 : 0.8) * (0.85 + 0.25 * n) * (((x % 2) == 0) ? 1 : 0.95);
                return 0xFF000000 | shade(0xFFFFFF, m);
            });
            // ------------------------------------------------------------ hard materials
            case "xm_candy": return pix(64, 128, (u, v, x, y) -> {
                double s = frac(u * 2 + v * 4);
                int c = s < 0.32 ? 0xE0202A : (s > 0.42 && s < 0.47 ? 0xF05A5A : (s > 0.72 && s < 0.78 ? 0x3AB86A : 0xFFFAF4));
                double ed = Math.min(Math.abs(s - 0.32), Math.abs(s));
                if (ed < 0.015) c = mix(c, 0xF08A8A, 0.5);
                return 0xFF000000 | shade(c, 0.94 + 0.06 * Math.sin(v * 30));
            });
            case "xm_antler": return pix(32, 128, (u, v, x, y) -> {
                double groove = 0.9 + 0.1 * Math.sin(u * Math.PI * 2 * 7 + fbm(u * 4, v * 8, 4, 8, 2, 341) * 4);
                double knob = fbm(u * 6, v * 20, 6, 20, 3, 342);
                int c = mix(0x8A5A34, 0xF0E0C0, Math.pow(v, 1.4) * 0.9);
                return 0xFF000000 | shade(c, groove * (0.82 + 0.3 * knob));
            });
            case "xm_pine": return img(64, 64, g -> {
                g.setColor(new Color(0x1E5A30));
                g.fillRect(0, 0, 64, 64);
                java.util.Random r = new java.util.Random(351);
                g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < 420; i++) {
                    double x = r.nextDouble() * 64, y = r.nextDouble() * 64, l = 4 + r.nextDouble() * 5, a = Math.PI / 2 + (r.nextDouble() - 0.5) * 1.1;
                    int c = ramp(r.nextDouble(), 0x16482A, 0x2E7A44, 0x4AA860, 0x7ACB84);
                    g.setColor(new Color(c));
                    for (int w = -1; w <= 1; w++) {
                        double xx = x + w * 64;
                        g.drawLine((int) xx, (int) y, (int) (xx + Math.cos(a) * l), (int) (y + Math.sin(a) * l));
                    }
                }
            });
            case "xm_bark": return pix(32, 64, (u, v, x, y) -> 0xFF000000 | shade(0x6A4222, 0.7 + 0.35 * fbm(u * 8, v * 2, 8, 2, 3, 361)));
            case "xm_wood": return pix(64, 64, (u, v, x, y) -> {
                double n = fbm(u * 2, v * 10, 2, 10, 4, 371);
                double gr = 0.5 + 0.5 * Math.sin((v * 22 + n * 6) * Math.PI);
                return 0xFF000000 | shade(0xFFFFFF, 0.72 + 0.18 * gr + 0.1 * n);
            });
            case "xm_coal": return pix(32, 32, (u, v, x, y) -> 0xFF000000 | shade(0x2A2A30, 0.6 + 0.8 * fbm(u * 6, v * 6, 6, 6, 3, 381) + (hash(x, y, 382) > 0.93 ? 0.8 : 0)));
            case "xm_carrot": return pix(32, 64, (u, v, x, y) -> {
                double ring = 0.88 + 0.12 * Math.sin(v * 60 + fbm(u * 4, v * 4, 4, 4, 2, 391) * 3);
                return 0xFF000000 | shade(mix(0xFF8A1A, 0xF06A0A, v), ring);
            });
            case "xm_snow": return pix(64, 64, (u, v, x, y) -> {
                double n = fbm(u * 8, v * 8, 8, 8, 4, 401);
                int c = mix(0xDCE8F8, 0xFFFFFF, 0.4 + 0.6 * n);
                if (hash(x, y, 402) > 0.975) c = 0xFFFFFF;
                if (hash(x, y, 403) > 0.992) c = 0xBFE4FF;
                return 0xFF000000 | c;
            });
            case "xm_ginger": return pix(64, 64, (u, v, x, y) -> {
                double n = fbm(u * 8, v * 8, 8, 8, 4, 411);
                int c = ramp(n, 0x8A4A1E, 0xB0642A, 0xC87A38);
                if (hash(x, y, 412) > 0.97) c = shade(c, 0.75);
                if (hash(x, y, 413) > 0.985) c = mix(c, 0xE8B070, 0.5);
                return 0xFF000000 | c;
            });
            case "xm_gingerman": return gingerMan();
            case "xm_holly": return img(64, 32, g -> {
                g.setPaint(new GradientPaint(0, 0, new Color(0x3AA050), 0, 32, new Color(0x1A5A28)));
                g.fillRect(0, 0, 64, 32);
                g.setColor(new Color(0x9AD88A));
                g.setStroke(new BasicStroke(1.4f));
                g.drawLine(2, 16, 62, 16);
                g.setStroke(new BasicStroke(0.8f));
                for (int i = 0; i < 6; i++) { g.drawLine(8 + i * 9, 16, 13 + i * 9, 6); g.drawLine(8 + i * 9, 16, 13 + i * 9, 26); }
                g.setColor(col(0xFFFFFF, 0.25));
                g.fill(new Ellipse2D.Double(10, 5, 30, 7));
            });
            case "xm_gloss": return pix(32, 32, (u, v, x, y) -> {
                double d = Math.hypot(u - 0.32, v - 0.3);
                return 0xFF000000 | Capes.add(shade(0xFFFFFF, 0.7 + 0.3 * (1 - v)), 0xFFFFFF, Math.exp(-d * d / 0.004) * 0.9);
            });
            case "xm_bulb": return pix(32, 32, (u, v, x, y) -> {
                double d = Math.hypot(u - 0.5, v - 0.5);
                double spec = Math.exp(-(Math.pow(u - 0.3, 2) + Math.pow(v - 0.35, 2)) / 0.006);
                return 0xFF000000 | Capes.add(shade(0xFFFFFF, 0.78 + 0.22 * (1 - d * 1.4)), 0xFFFFFF, spec);
            });
            case "xm_flake": return img(64, 64, g -> {
                g.translate(32, 32);
                for (int pass = 0; pass < 2; pass++) {
                    g.setColor(pass == 0 ? col(0x9ADCFF, 0.45) : Color.WHITE);
                    g.setStroke(new BasicStroke(pass == 0 ? 5.5f : 2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    for (int i = 0; i < 6; i++) {
                        g.drawLine(0, 0, 0, -28);
                        g.drawLine(0, -10, -6, -16); g.drawLine(0, -10, 6, -16);
                        g.drawLine(0, -19, -5, -24); g.drawLine(0, -19, 5, -24);
                        g.rotate(Math.PI / 3);
                    }
                }
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(-4, -4, 8, 8));
            });
            case "xm_aurora": return pix(128, 64, (u, v, x, y) -> {
                // u along the band (periodic), v = 0 top .. 1 bottom edge
                double st = fbm(u * 24, 0.5, 24, 0, 3, 421);
                double fold = 0.5 + 0.5 * Math.sin(u * Math.PI * 2 * 9 + st * 3);
                double bottom = Math.exp(-Math.pow((1 - v) / 0.1, 2));
                double body = Math.pow(v, 1.8) * (0.45 + 0.55 * st) * (0.6 + 0.4 * fold);
                int c = ramp(v, 0xB45AFF, 0x5A8AFF, 0x3AE8D8, 0x5AFF8A, 0xC8FFD8);
                double a = Math.min(1, body * 1.1 + bottom * 0.7) * (v > 0.97 ? (1 - v) / 0.03 : 1);
                return argb(Capes.add(c, 0xFFFFFF, bottom * 0.5), a);
            });
            case "xm_gold": return pix(64, 64, (u, v, x, y) -> {
                double s = 0.5 + 0.5 * Math.sin(u * 9 + v * 4);
                return 0xFF000000 | ramp(0.25 + 0.6 * s * (0.8 + 0.2 * hash(x / 4, y / 4, 431)), 0x9A6A12, 0xE8B92C, 0xFFF1A8, 0xFFFFFF);
            });
            case "xm_red_lacquer": return pix(32, 128, (u, v, x, y) -> {
                double sh = Math.exp(-Math.pow((u - 0.3) / 0.12, 2)) * 0.45 + 0.6 + 0.08 * Math.sin(v * 3);
                return 0xFF000000 | shade(0xC8141E, Math.min(1.35, sh));
            });
            case "xm_sleighside": return sleighSide();
            case "xm_wind": return pix(128, 16, (u, v, x, y) -> {
                double w = Math.pow(Math.sin(Math.PI * v), 2) * (0.35 + 0.65 * fbm(u * 12, v * 2, 12, 2, 3, 461));
                return argb(0xF4FAFF, w * Math.pow(Math.sin(Math.PI * u), 0.6) * 0.75);
            });
            default:
                if (name.startsWith("xm_gift:")) return gift(name.substring(8));
                return null;
        }
    }

    static double frac(double v) { return v - Math.floor(v); }

    /**
     * Knitted fabric: V stitches in columns. kind -1 plain white (tint), 0 red scarf with white / green stripes (along v),
     * 1 christmas sweater red with white band.
     */
    private static CTex.T knit(int kind) {
        int w = 64, h = kind == 0 ? 256 : 128;
        return pix(w, h, (u, v, x, y) -> {
            double cu = u * 8, cv = v * (kind == 0 ? 40 : 20);
            double fx = frac(cu) - 0.5, fy = frac(cv);
            // stitch: two slanted lobes forming a V
            double lobe = Math.abs(Math.abs(fx) * 2 - (1 - fy) * 0.9 - 0.05);
            double m = 1 - Math.min(1, lobe * 1.6);
            double groove = Math.abs(fx) < 0.05 ? 0.72 : 1;
            double sh = (0.72 + 0.3 * m) * groove * (0.95 + 0.06 * hash(x, y, 441));
            int c = 0xFFFFFF;
            if (kind == 0) {
                int row = (int) Math.floor(cv);
                int r = row % 20;
                c = (r == 3 || r == 4 || r == 15 || r == 16) ? 0xFFFFFF : (r == 9 || r == 10) ? 0x2E9A48 : 0xD8232C;
                if (row < 1 || row > 38) c = 0xF4F4F4;
            } else if (kind == 1) {
                int row = (int) Math.floor(cv);
                c = (row % 10 == 4 || row % 10 == 5) ? 0xFFFFFF : 0xC8202A;
            }
            return 0xFF000000 | shade(c, sh);
        });
    }

    /** Gift wrapping paper: "RRGGBB:PPPPPP:k" base colour, pattern colour, pattern kind (0 dots, 1 stars, 2 stripes, 3 flakes). */
    private static CTex.T gift(String spec) {
        String[] p = spec.split(":");
        int base = Integer.parseInt(p[0], 16), pat = Integer.parseInt(p[1], 16), kind = p.length > 2 ? Integer.parseInt(p[2]) : 0;
        return img(64, 64, g -> {
            g.setPaint(new GradientPaint(0, 0, new Color(mix(base, 0xFFFFFF, 0.12)), 64, 64, new Color(mix(base, 0x000000, 0.18))));
            g.fillRect(0, 0, 64, 64);
            g.setColor(new Color(pat));
            if (kind == 2) {
                g.setStroke(new BasicStroke(5f));
                for (int i = -64; i < 128; i += 16) g.drawLine(i, 0, i + 64, 64);
            } else {
                for (int yy = 0; yy < 4; yy++)
                    for (int xx = 0; xx < 4; xx++) {
                        double cx = 8 + xx * 16 + (yy % 2) * 8, cy = 8 + yy * 16;
                        if (kind == 0) g.fill(new Ellipse2D.Double(cx - 3, cy - 3, 6, 6));
                        else if (kind == 1) g.fill(starShape(cx, cy, 5, 2.2, 5));
                        else {
                            g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            for (int k = 0; k < 3; k++) {
                                double a = k * Math.PI / 3;
                                g.drawLine((int) (cx - Math.cos(a) * 5), (int) (cy - Math.sin(a) * 5), (int) (cx + Math.cos(a) * 5), (int) (cy + Math.sin(a) * 5));
                            }
                        }
                    }
            }
            g.setColor(col(0xFFFFFF, 0.08));
            for (int i = 0; i < 64; i += 3) g.drawLine(0, i, 64, i);
        });
    }

    static Path2D starShape(double cx, double cy, double r0, double r1, int pts) {
        Path2D s = new Path2D.Double();
        for (int i = 0; i < pts * 2; i++) {
            double a = -Math.PI / 2 + Math.PI * i / pts, r = i % 2 == 0 ? r0 : r1;
            if (i == 0) s.moveTo(cx + Math.cos(a) * r, cy + Math.sin(a) * r); else s.lineTo(cx + Math.cos(a) * r, cy + Math.sin(a) * r);
        }
        s.closePath();
        return s;
    }

    /** Gingerbread man outline in normalised coordinates (x -1..1, y -1..1, y up), shared by the geometry and the texture. */
    static final float[][] GMAN = {
            {0.0f, 1.0f}, {0.24f, 0.96f}, {0.36f, 0.8f}, {0.34f, 0.58f}, {0.22f, 0.46f}, {0.62f, 0.44f}, {0.86f, 0.36f}, {0.9f, 0.2f},
            {0.76f, 0.12f}, {0.36f, 0.12f}, {0.36f, -0.2f}, {0.62f, -0.72f}, {0.62f, -0.92f}, {0.44f, -1.0f}, {0.26f, -0.92f}, {0.0f, -0.46f},
            {-0.26f, -0.92f}, {-0.44f, -1.0f}, {-0.62f, -0.92f}, {-0.62f, -0.72f}, {-0.36f, -0.2f}, {-0.36f, 0.12f}, {-0.76f, 0.12f}, {-0.9f, 0.2f},
            {-0.86f, 0.36f}, {-0.62f, 0.44f}, {-0.22f, 0.46f}, {-0.34f, 0.58f}, {-0.36f, 0.8f}, {-0.24f, 0.96f}};

    private static CTex.T gingerMan() {
        return img(128, 128, g -> {
            g.setColor(new Color(0x7A4018));
            g.fillRect(0, 0, 128, 128);
            Path2D s = new Path2D.Double();
            for (int i = 0; i < GMAN.length; i++) {
                double x = (GMAN[i][0] + 0.9) / 1.8 * 128, y = 64 - GMAN[i][1] * 64;
                if (i == 0) s.moveTo(x, y); else s.lineTo(x, y);
            }
            s.closePath();
            g.setPaint(new RadialGradientPaint(56, 50, 80, new float[]{0f, 0.7f, 1f}, new Color[]{new Color(0xD08A44), new Color(0xB86A2E), new Color(0x8A4A1E)}));
            g.fill(s);
            java.util.Random r = new java.util.Random(451);
            for (int i = 0; i < 90; i++) { g.setColor(col(0x5A2A0A, 0.35)); g.fill(new Ellipse2D.Double(r.nextDouble() * 128, r.nextDouble() * 128, 1.6, 1.6)); }
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // eyes + smile
            g.fill(new Ellipse2D.Double(51, 13, 8, 8));
            g.fill(new Ellipse2D.Double(69, 13, 8, 8));
            g.drawArc(52, 16, 24, 16, 200, 140);
            // wrist / ankle zigzags
            zig(g, 14, 44, 14, 62, 4);
            zig(g, 114, 44, 114, 62, 4);
            zig(g, 30, 108, 42, 120, 3);
            zig(g, 98, 108, 86, 120, 3);
            // buttons
            int[] bc = {0xE8323A, 0x2EB84A, 0xE8323A};
            for (int i = 0; i < 3; i++) {
                g.setColor(new Color(bc[i]));
                g.fill(new Ellipse2D.Double(58, 44 + i * 14, 12, 12));
                g.setColor(col(0xFFFFFF, 0.7));
                g.fill(new Ellipse2D.Double(60, 46 + i * 14, 4, 4));
            }
        });
    }

    private static void zig(Graphics2D g, double x0, double y0, double x1, double y1, int n) {
        Path2D p = new Path2D.Double();
        double dx = x1 - x0, dy = y1 - y0, l = Math.hypot(dx, dy), nx = -dy / l * 4, ny = dx / l * 4;
        for (int i = 0; i <= n * 2; i++) {
            double f = i / (double) (n * 2), s = i % 2 == 0 ? -1 : 1;
            double x = x0 + dx * f + nx * s, y = y0 + dy * f + ny * s;
            if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
        }
        g.setColor(Color.WHITE);
        g.draw(p);
    }

    /** Sleigh side panel: red lacquer with golden scroll ornaments (u along the panel, v top..bottom). */
    private static CTex.T sleighSide() {
        return img(256, 64, g -> {
            g.setPaint(new GradientPaint(0, 0, new Color(0xE8323A), 0, 64, new Color(0x7A0A12)));
            g.fillRect(0, 0, 256, 64);
            g.setColor(col(0xFFFFFF, 0.18));
            g.fillRect(0, 8, 256, 6);
            g.setColor(new Color(0xFFD86A));
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(0, 4, 256, 4);
            g.drawLine(0, 58, 256, 58);
            for (int i = 0; i < 4; i++) {
                double cx = 32 + i * 64;
                Path2D sc = new Path2D.Double();
                sc.moveTo(cx - 24, 32);
                sc.curveTo(cx - 20, 14, cx - 4, 14, cx - 4, 28);
                sc.curveTo(cx - 4, 36, cx - 14, 36, cx - 12, 28);
                g.draw(sc);
                Path2D sc2 = new Path2D.Double();
                sc2.moveTo(cx + 24, 32);
                sc2.curveTo(cx + 20, 50, cx + 4, 50, cx + 4, 36);
                sc2.curveTo(cx + 4, 28, cx + 14, 28, cx + 12, 36);
                g.draw(sc2);
                g.fill(new Ellipse2D.Double(cx - 3, 29, 6, 6));
            }
        });
    }
}
