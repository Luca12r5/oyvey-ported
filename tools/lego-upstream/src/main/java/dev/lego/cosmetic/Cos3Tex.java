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
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/** Textures of the premium cosmetics ("c3_*") and the preview skin ("@skin"). */
final class Cos3Tex {
    private Cos3Tex() { }

    static CTex.T paint(String name) {
        switch (name) {
            case "@skin": return skin();
            // ------------------------------------------------------------ materials (white based, tint with g.color)
            case "c3_beam": return pix(8, 8, (u, v, x, y) -> argb(0xFFFFFF, 0.34));
            case "c3_metal": return pix(64, 64, (u, v, x, y) -> 0xFF000000 | shade(mix(0xFFFFFF, 0xB4BAC6, v * 0.8), 0.93 + 0.07 * hash(x / 7, y, 11) + 0.04 * Math.sin(u * 9)));
            case "c3_crystal": return pix(64, 64, (u, v, x, y) -> {
                double f = 0.5 + 0.5 * Math.sin(u * Math.PI * 2 * 3 + v * 2);
                int c = mix(0xFFFFFF, 0x9AA4C8, 0.25 + 0.55 * v * (0.6 + 0.4 * f));
                double streak = Math.exp(-Math.pow((u * 1.3 - v * 0.7 - 0.2) / 0.05, 2)) * 0.7;
                return 0xFF000000 | Capes.add(c, 0xFFFFFF, streak);
            });
            case "c3_scale": return pix(64, 64, (u, v, x, y) -> {
                double sx = u * 8, sy = v * 8;
                double row = Math.floor(sy);
                double cx = sx + (row % 2 == 0 ? 0 : 0.5);
                double fx = cx - Math.floor(cx) - 0.5, fy = sy - row;
                double d = Math.hypot(fx, (fy - 0.1) * 1.1);
                double m = d < 0.5 ? 1.0 - d * 0.6 : 0.62;
                double rim = d > 0.42 && d < 0.5 ? 0.75 : 1;
                return 0xFF000000 | shade(0xF2F2F2, m * rim * (0.95 + 0.08 * hash(x, y, 5)));
            });
            case "c3_fur": return pix(64, 64, (u, v, x, y) -> {
                double s = fbm(u * 24, v * 3, 24, 3, 3, 8);
                return 0xFF000000 | shade(0xF6F6F6, 0.78 + 0.3 * s);
            });
            case "c3_plume": return pix(32, 64, (u, v, x, y) -> {
                double barb = 0.85 + 0.15 * Math.sin((v * 30 + Math.abs(u - 0.5) * 12));
                double shaft = Math.abs(u - 0.5) < 0.05 ? 0.7 : 1;
                return 0xFF000000 | shade(0xF6F6F6, barb * shaft);
            });
            case "c3_lacquer": return pix(32, 128, (u, v, x, y) -> {
                double sh = Math.exp(-Math.pow((u - 0.3) / 0.12, 2)) * 0.5 + 0.55 + 0.1 * Math.sin(v * 3);
                return 0xFF000000 | shade(0xFFFFFF, Math.min(1, sh));
            });
            case "c3_jet": return img(64, 64, g -> {
                g.setPaint(new GradientPaint(0, 0, new Color(0xFFFFFF), 64, 0, new Color(0xC4C8D0)));
                g.fillRect(0, 0, 64, 64);
                g.setColor(col(0x000000, 0.25));
                g.setStroke(new BasicStroke(1.5f));
                g.drawLine(0, 20, 64, 20); g.drawLine(0, 44, 64, 44);
                for (int i = 0; i < 4; i++) g.fill(new Ellipse2D.Double(6 + i * 16, 24, 3, 3));
            });
            case "c3_exhaust": return pix(16, 64, (u, v, x, y) -> argb(mix(0xFFFFFF, 0xFFFFFF, 0), Math.pow(1 - v, 1.6) * (0.75 + 0.25 * Math.sin(u * Math.PI * 6))));
            case "c3_eye": return img(32, 32, g -> {
                g.setColor(new Color(0x14141A));
                g.fillRect(0, 0, 32, 32);
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(5, 5, 11, 11));
                g.fill(new Ellipse2D.Double(19, 19, 5, 5));
            });
            case "c3_grille": return pix(64, 64, (u, v, x, y) -> {
                double dx = (x % 6) - 2.5, dy = (y % 6) - 2.5;
                return 0xFF000000 | (dx * dx + dy * dy < 3.5 ? 0x0A0A0E : shade(0x2E3038, 0.9 + 0.2 * v));
            });
            case "c3_eq": return pix(256, 32, (u, v, x, y) -> {
                int bar = x / 8;
                double hgt = 0.3 + 0.7 * hash(bar, 1, 9);
                boolean on = (1 - v) < hgt && (x % 8) < 6 && (y % 4) < 3;
                int c = Capes.hsv(u, 0.85, 1);
                return on ? 0xFF000000 | c : argb(c, 0.18);
            });
            // ------------------------------------------------------------ bandanas (strip wrapping the head, u around)
            case "c3_band_bolt": return band(0x12A84A, 0x0A5A2A, 0xB8FF6A, 0);
            case "c3_band_fire": return band(0xC8201A, 0x6A0A08, 0xFFB02A, 1);
            case "c3_band_ice": return band(0x2A7AD8, 0x0E2E6A, 0xD8F6FF, 2);
            // ------------------------------------------------------------ planets
            case "c3_planet_gas": return pix(128, 64, (u, v, x, y) -> {
                double w = fbm(u * 6, v * 3, 6, 0, 3, 21) - 0.5;
                double b = Math.sin((v + w * 0.12) * Math.PI * 9);
                int c = ramp(0.5 + 0.5 * b, 0xB8602A, 0xE8B070, 0xFFF0D0);
                double spot = Math.exp(-(Math.pow((u - 0.3) / 0.06, 2) + Math.pow((v - 0.62) / 0.05, 2)));
                return 0xFF000000 | mix(c, 0xC8401A, spot);
            });
            case "c3_planet_ice": return pix(128, 64, (u, v, x, y) -> {
                double n = fbm(u * 8, v * 4, 8, 0, 4, 33);
                int c = ramp(n, 0x1A4A9A, 0x3A9AE8, 0x9AE8FF, 0xFFFFFF);
                if (v < 0.12 || v > 0.88) c = mix(c, 0xFFFFFF, 0.8);
                return 0xFF000000 | c;
            });
            case "c3_planet_lava": return pix(128, 64, (u, v, x, y) -> {
                double n = fbm(u * 10, v * 5, 10, 0, 4, 44);
                double crack = Math.exp(-Math.pow((n - 0.5) / 0.035, 2));
                int c = mix(0x2A1410, 0x4A2418, n);
                return 0xFF000000 | Capes.add(c, 0xFF7A1A, crack * 1.4);
            });
            case "c3_ring": return pix(256, 16, (u, v, x, y) -> {
                double b = 0.55 + 0.45 * Math.sin(v * 40 + Math.sin(v * 11) * 2);
                double edge = Math.min(v, 1 - v) * 6;
                return argb(ramp(v, 0xE8D0A0, 0xFFF4DA, 0xC8A070), Math.min(1, edge) * (0.45 + 0.5 * b));
            });
            // ------------------------------------------------------------ glass
            case "c3_glass": return pix(64, 64, (u, v, x, y) -> {
                double s1 = Math.exp(-Math.pow((u - 0.18) / 0.03, 2)) * Math.exp(-Math.pow((v - 0.35) / 0.12, 2));
                double s2 = Math.exp(-Math.pow((u - 0.24) / 0.012, 2)) * Math.exp(-Math.pow((v - 0.42) / 0.18, 2));
                double rim = Math.pow(Math.abs(v - 0.5) * 2, 6) * 0.3;
                return argb(0xE8F8FF, 0.1 + rim + s1 * 0.7 + s2 * 0.6);
            });
            case "c3_lglass": return pix(32, 32, (u, v, x, y) -> {
                double d = Math.hypot(u - 0.5, v - 0.6);
                return argb(mix(0xFFF4C0, 0xFF9A2A, d * 1.6), 0.55 + 0.4 * Math.exp(-d * 4));
            });
            // ------------------------------------------------------------ katana
            case "c3_wrap": return pix(32, 128, (u, v, x, y) -> {
                double a = ((u * 2 + v * 8) % 1 + 1) % 1, b = ((u * 2 - v * 8) % 1 + 1) % 1;
                double d = Math.min(Math.abs(a - 0.5), Math.abs(b - 0.5));
                boolean diamond = Math.abs(a - 0.5) < 0.22 && Math.abs(b - 0.5) < 0.22;
                return 0xFF000000 | (diamond ? shade(0xF0E8D8, 0.9 + 0.1 * v) : shade(0x1A1420, 0.8 + d));
            });
            case "c3_blade": return pix(16, 128, (u, v, x, y) -> {
                double ham = 0.55 + 0.08 * Math.sin(v * 50) + 0.04 * Math.sin(v * 131);
                int c = u > ham ? mix(0xF4F8FF, 0xFFFFFF, (u - ham) * 3) : mix(0x8A94A6, 0xC8D0DC, u / ham);
                return 0xFF000000 | c;
            });
            // ------------------------------------------------------------ book
            case "c3_cover": return img(64, 64, g -> {
                g.setPaint(new GradientPaint(0, 0, new Color(0x5A1A7A), 64, 64, new Color(0x2A0A40)));
                g.fillRect(0, 0, 64, 64);
                g.setColor(new Color(0xF2C84A));
                g.setStroke(new BasicStroke(2.2f));
                g.draw(new RoundRectangle2D.Double(5, 5, 54, 54, 8, 8));
                g.draw(new Ellipse2D.Double(20, 20, 24, 24));
                g.fill(Tex3.starPath(32, 32, 9, 4));
                for (int i = 0; i < 4; i++) {
                    double cx = i % 2 == 0 ? 8 : 56, cy = i < 2 ? 8 : 56;
                    g.fill(new Ellipse2D.Double(cx - 3, cy - 3, 6, 6));
                }
            });
            case "c3_pages": return pix(16, 64, (u, v, x, y) -> 0xFF000000 | shade(0xF4E8C8, (y % 2 == 0) ? 0.86 : 1.0));
            case "c3_page": return img(64, 64, g -> {
                g.setColor(new Color(0xF8EED4));
                g.fillRect(0, 0, 64, 64);
                java.util.Random r = new java.util.Random(4);
                g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int l = 0; l < 8; l++) {
                    double x = 6;
                    while (x < 56) {
                        double w = 3 + r.nextInt(8);
                        g.setColor(col(l == 0 ? 0x9A2AC8 : 0x4A3A5A, 0.8));
                        g.draw(new java.awt.geom.Line2D.Double(x, 8 + l * 6.5, Math.min(58, x + w), 8 + l * 6.5));
                        x += w + 3;
                    }
                }
                g.setColor(col(0x9A2AC8, 0.9));
                g.draw(new Ellipse2D.Double(38, 42, 16, 16));
            });
            case "c3_glyphs": return img(256, 64, g -> {
                g.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < 4; i++) {
                    AffineTransform o = g.getTransform();
                    g.translate(i * 64 + 32, 32);
                    g.setPaint(new RadialGradientPaint(0, 0, 30, new float[]{0f, 1f}, new Color[]{col(0xFFFFFF, 0.35), col(0xFFFFFF, 0)}));
                    g.fill(new Ellipse2D.Double(-30, -30, 60, 60));
                    g.setColor(Color.WHITE);
                    glyph(g, i);
                    g.setTransform(o);
                }
            });
            // ------------------------------------------------------------ water
            case "c3_splash": return pix(128, 32, (u, v, x, y) -> {
                double top = 0.25 + 0.2 * Math.sin(u * Math.PI * 2 * 9) + 0.12 * Math.sin(u * Math.PI * 2 * 23 + 1);
                if (v < top) return argb(0xFFFFFF, 0);
                double a = 0.85 - (v - top) * 0.7;
                int c = mix(0xFFFFFF, 0x4AB8FF, (v - top) * 1.6);
                return argb(c, a);
            });
            case "c3_ripple": return pix(64, 64, (u, v, x, y) -> {
                double d = Math.hypot(u - 0.5, v - 0.5) * 2;
                double a = Math.exp(-Math.pow((d - 0.86) / 0.06, 2)) + 0.5 * Math.exp(-Math.pow((d - 0.62) / 0.05, 2));
                return argb(mix(0xFFFFFF, 0x7AD0FF, d), Math.min(1, a));
            });
            case "c3_drop": return img(32, 48, g -> {
                Path2D p = new Path2D.Double();
                p.moveTo(16, 2); p.curveTo(22, 16, 30, 24, 30, 32); p.curveTo(30, 42, 22, 46, 16, 46); p.curveTo(10, 46, 2, 42, 2, 32); p.curveTo(2, 24, 10, 16, 16, 2); p.closePath();
                g.setPaint(new RadialGradientPaint(16, 32, 16, new float[]{0f, 1f}, new Color[]{col(0xD8F4FF, 0.9), col(0x2A9AFF, 0.85)}));
                g.fill(p);
                g.setColor(col(0xFFFFFF, 0.95));
                g.fill(new Ellipse2D.Double(9, 26, 6, 9));
            });
            // ------------------------------------------------------------ misc
            case "c3_hex": return pix(64, 64, (u, v, x, y) -> {
                double px = (u - 0.5) * 2, py = (v - 0.5) * 2;
                double d = Math.max(Math.abs(px) * 0.866 + Math.abs(py) * 0.5, Math.abs(py));
                double edge = Math.exp(-Math.pow((d - 0.86) / 0.07, 2));
                double fill = d < 0.9 ? 0.28 + 0.2 * (1 - d) : 0;
                return argb(0xFFFFFF, Math.min(1, fill + edge));
            });
            case "c3_ray": return pix(8, 64, (u, v, x, y) -> argb(0xFFFFFF, Math.pow(v, 1.4) * (0.55 + 0.45 * Math.sin(u * Math.PI))));
            case "c3_visor": return pix(128, 32, (u, v, x, y) -> {
                double scan = (y % 3 == 0) ? 0.14 : 0;
                double edge = Math.pow(Math.abs(v - 0.5) * 2, 4);
                double streak = Math.exp(-Math.pow((u * 1.0 - v * 0.25 - 0.62) / 0.03, 2)) * 0.5;
                int c = Capes.mix(0x2AD8FF, 0xB86AFF, u);
                return argb(Capes.add(c, 0xFFFFFF, streak + edge * 0.6), 0.42 + scan + edge * 0.4 + streak);
            });
            case "c3_lens": return pix(32, 32, (u, v, x, y) -> {
                int c = Capes.ramp(v, 0x3A1A5A, 0x160A24, 0x08040E);
                double streak = Math.exp(-Math.pow((u * 0.8 + v * 0.6 - 0.55) / 0.05, 2)) * 0.45;
                return argb(Capes.add(c, 0xFFFFFF, streak), 0.9);
            });
            case "c3_membrane": return pix(64, 64, (u, v, x, y) -> {
                double vein = Math.exp(-Math.pow(Math.sin(u * Math.PI * 5 + v * 2) / 0.12, 2)) * 0.5;
                return argb(Capes.add(0xE8E8E8, 0xFFFFFF, vein) & 0xFFFFFF, 0.88 + vein * 0.2);
            });
            case "c3_parrotwing": return img(64, 32, g -> {
                g.setPaint(new java.awt.LinearGradientPaint(0, 0, 64, 0, new float[]{0f, 0.45f, 0.75f, 1f},
                        new Color[]{new Color(0x22C8A8), new Color(0x2A8AE8), new Color(0x6A3AD8), new Color(0xE8304A)}));
                g.fillRect(0, 0, 64, 32);
                g.setStroke(new BasicStroke(1f));
                for (int i = 0; i < 12; i++) {
                    g.setColor(col(0x000000, 0.22));
                    g.drawLine(8 + i * 5, 0, 14 + i * 5, 32);
                    g.setColor(col(0xFFFFFF, 0.12));
                    g.drawLine(9 + i * 5, 0, 15 + i * 5, 32);
                }
                g.setColor(col(0xFFE04A, 0.9));
                g.fillRect(0, 13, 18, 5);
            });
            case "c3_flamegrad": return pix(32, 64, (u, v, x, y) -> {
                double lick = 0.08 * Math.sin(u * Math.PI * 2 * 3 + v * 9);
                double f = Capes.clamp01(v + lick);
                int c = Capes.ramp(f, 0xFFFFF0, 0xFFE07A, 0xFFFFFF, 0xFFFFFF);
                return argb(c, Math.max(0.15, 1 - Math.pow(f, 1.6) * 0.8));
            });
            case "c3_pumpkin": return pix(64, 64, (u, v, x, y) -> {
                double rib = Math.abs(Math.sin(u * Math.PI * 8));
                return 0xFF000000 | shade(0xFFFFFF, 0.72 + 0.28 * Math.pow(rib, 0.5));
            });
            default: return null;
        }
    }

    /** Four original glyphs (used for magic effects). */
    static void glyph(Graphics2D g, int i) {
        Path2D p = new Path2D.Double();
        switch (i % 4) {
            case 0: p.moveTo(-14, 16); p.lineTo(0, -16); p.lineTo(14, 16); p.moveTo(-8, 4); p.lineTo(8, 4); p.moveTo(0, -16); p.lineTo(0, -22); break;
            case 1: p.append(new Ellipse2D.Double(-12, -12, 24, 24), false); p.moveTo(-18, 0); p.lineTo(18, 0); p.moveTo(0, 12); p.lineTo(0, 22); break;
            case 2: p.moveTo(-12, -16); p.lineTo(12, -16); p.lineTo(-12, 16); p.lineTo(12, 16); p.moveTo(-6, 0); p.lineTo(6, 0); break;
            default: p.moveTo(0, -18); p.lineTo(14, 0); p.lineTo(0, 18); p.lineTo(-14, 0); p.closePath(); p.moveTo(0, -8); p.lineTo(0, 8); break;
        }
        g.draw(p);
    }

    /** Fabric strip: base, dark, accent colors; pattern 0 = bolts, 1 = flames, 2 = snowflakes. */
    private static CTex.T band(int base, int dark, int accent, int pat) {
        return img(256, 32, g -> {
            g.setPaint(new GradientPaint(0, 0, new Color(mix(base, 0xFFFFFF, 0.12)), 0, 32, new Color(mix(base, dark, 0.5))));
            g.fillRect(0, 0, 256, 32);
            // woven texture
            for (int y = 0; y < 32; y += 2) { g.setColor(col(0x000000, 0.06)); g.drawLine(0, y, 256, y); }
            // hemmed edges
            g.setColor(col(dark, 0.9));
            g.fillRect(0, 0, 256, 3);
            g.fillRect(0, 29, 256, 3);
            g.setColor(col(0xFFFFFF, 0.35));
            g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{3f, 2f}, 0));
            g.drawLine(0, 5, 256, 5);
            g.drawLine(0, 27, 256, 27);
            g.setStroke(new BasicStroke(1f));
            for (int i = 0; i < 8; i++) {
                double cx = 16 + i * 32;
                AffineTransform o = g.getTransform();
                g.translate(cx, 16);
                if (pat == 0) {
                    Path2D b = new Path2D.Double();
                    b.moveTo(2, -10); b.lineTo(-5, 1); b.lineTo(0, 1); b.lineTo(-3, 10); b.lineTo(5, -2); b.lineTo(0, -2); b.lineTo(4, -10); b.closePath();
                    g.setColor(col(accent, 0.95));
                    g.fill(b);
                    g.setColor(col(0xFFFFFF, 0.9));
                    g.fill(new Ellipse2D.Double(10, -6, 3, 3));
                    g.fill(new Ellipse2D.Double(-14, 4, 2.5, 2.5));
                } else if (pat == 1) {
                    Path2D f = new Path2D.Double();
                    f.moveTo(-9, 13); f.curveTo(-10, 2, -2, 0, -3, -12); f.curveTo(4, -4, 9, 2, 8, 13); f.closePath();
                    g.setPaint(new GradientPaint(0, 13, new Color(0xFFE07A), 0, -12, new Color(accent)));
                    g.fill(f);
                    g.setColor(col(0xFFF6D0, 0.9));
                    g.fill(new Ellipse2D.Double(-3, 4, 5, 7));
                } else {
                    g.setColor(col(accent, 0.95));
                    g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    for (int k = 0; k < 3; k++) {
                        g.rotate(Math.PI / 3);
                        g.drawLine(0, -9, 0, 9);
                        g.drawLine(-3, -6, 0, -8); g.drawLine(3, -6, 0, -8);
                    }
                    g.setStroke(new BasicStroke(1f));
                }
                g.setTransform(o);
            }
        });
    }

    // ================================================================== preview skin (original character)
    /**
     * Original 64x64 preview skin in the vanilla layout: "Kiko" - teal hoodie with a yellow brick badge,
     * orange hair, dark trousers, white sneakers.
     */
    static CTex.T skin() {
        BufferedImage im = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        int[] p = Capes.px(im);
        int skinC = 0xE8B48A, hair = 0xE0701E, hairD = 0xA84A10, hood = 0x1FA8A0, hoodD = 0x137A74, pants = 0x2A2E48, pantsD = 0x1C1F32, shoe = 0xF2F2F2;
        // ---- head (8,8,8) at (0,0)
        fillBox(p, 0, 0, 8, 8, 8, skinC);
        rect(p, 0, 8, 32, 3, hair);            // hair band all around the top of the head sides
        rect(p, 8, 0, 8, 8, hair);             // top
        rect(p, 24, 8, 8, 7, hair);            // back
        rect(p, 0, 11, 2, 3, hair); rect(p, 22, 11, 2, 3, hair);   // side burns
        // face (front 8..16 x 8..16)
        rect(p, 8, 11, 8, 1, hairD);            // fringe shadow
        rect(p, 10, 8, 2, 4, hair); rect(p, 13, 8, 2, 4, hair);
        rect(p, 9, 12, 2, 2, 0xFFFFFF); rect(p, 13, 12, 2, 2, 0xFFFFFF);    // eyes
        rect(p, 10, 12, 1, 2, 0x2A5A3A); rect(p, 13, 12, 1, 2, 0x2A5A3A);  // pupils (green)
        rect(p, 11, 14, 2, 1, 0xC8805A);                                    // nose shade
        rect(p, 10, 15, 4, 1, 0x9A4A3A);                                    // smile
        rect(p, 9, 14, 1, 1, 0xF0A08A); rect(p, 14, 14, 1, 1, 0xF0A08A);    // cheeks
        // ---- hat layer: hood rim around the head (40,8)
        rect(p, 32, 8, 32, 1, hoodD);
        rect(p, 40, 0, 8, 2, hoodD);
        rect(p, 56, 8, 8, 8, argb2(hood));     // hood back
        rect(p, 32, 8, 2, 8, argb2(hood)); rect(p, 54, 8, 2, 8, argb2(hood));
        // ---- body (8,12,4) at (16,16)
        fillBox(p, 16, 16, 8, 12, 4, hood);
        rect(p, 20, 30, 8, 2, hoodD);                          // waistband
        rect(p, 32, 30, 8, 2, hoodD);
        rect(p, 23, 20, 2, 4, 0xF2F2F2);                       // drawstrings
        rect(p, 23, 20, 1, 5, 0xF2F2F2); rect(p, 24, 20, 1, 5, 0xF2F2F2);
        rect(p, 21, 25, 6, 3, hoodD);                          // pocket
        rect(p, 25, 21, 2, 2, 0xFFCD03);                       // brick badge
        rect(p, 25, 21, 1, 1, 0xFFE87A);
        // ---- right arm (4,12,4) at (40,16) and left arm at (32,48)
        fillBox(p, 40, 16, 4, 12, 4, hood);
        rect(p, 40, 29, 16, 1, hoodD);
        rect(p, 40, 30, 16, 2, skinC);
        fillBox(p, 32, 48, 4, 12, 4, hood);
        rect(p, 32, 61, 16, 1, hoodD);
        rect(p, 32, 62, 16, 2, skinC);
        // ---- legs (4,12,4) right at (0,16), left at (16,48)
        fillBox(p, 0, 16, 4, 12, 4, pants);
        rect(p, 0, 29, 16, 3, shoe); rect(p, 0, 29, 16, 1, 0xC8C8D0);
        rect(p, 4, 22, 1, 6, pantsD);
        fillBox(p, 16, 48, 4, 12, 4, pants);
        rect(p, 16, 61, 16, 3, shoe); rect(p, 16, 61, 16, 1, 0xC8C8D0);
        rect(p, 23, 54, 1, 6, pantsD);
        // subtle noise so the mini version does not look flat
        for (int y = 0; y < 64; y++)
            for (int x = 0; x < 64; x++) {
                int i = y * 64 + x;
                if ((p[i] >>> 24) == 0) continue;
                p[i] = (p[i] & 0xFF000000) | (shade(p[i], 0.94 + 0.1 * hash(x, y, 91)) & 0xFFFFFF);
            }
        return CTex.of(im);
    }

    private static int argb2(int rgb) { return 0xFF000000 | rgb; }

    private static void rect(int[] p, int x, int y, int w, int h, int c) {
        if ((c >>> 24) == 0) c |= 0xFF000000;
        for (int j = y; j < y + h; j++)
            for (int i = x; i < x + w; i++) if (i >= 0 && j >= 0 && i < 64 && j < 64) p[j * 64 + i] = c;
    }

    /** Fills all 6 faces of a vanilla box layout with c (slightly shaded per face). */
    private static void fillBox(int[] p, int U, int V, int w, int h, int d, int c) {
        rect(p, U + d, V, w, d, shade(c, 1.08));
        rect(p, U + d + w, V, w, d, shade(c, 0.8));
        rect(p, U, V + d, d, h, shade(c, 0.9));
        rect(p, U + d, V + d, w, h, c);
        rect(p, U + d + w, V + d, d, h, shade(c, 0.9));
        rect(p, U + 2 * d + w, V + d, w, h, shade(c, 0.86));
    }

    @SuppressWarnings("unused")
    private static Rectangle2D r(double x, double y, double w, double h) { return new Rectangle2D.Double(x, y, w, h); }
}
