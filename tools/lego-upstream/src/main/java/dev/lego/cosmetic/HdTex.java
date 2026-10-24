package dev.lego.cosmetic;

import static dev.lego.cosmetic.Capes.add;
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
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/**
 * Textures of the HD remaster of the classic cosmetics ("hd_*"). Most are white based materials with fine detail
 * (fibres, grain, brushing, micro scratches) that are tinted with g.color; lighting comes from the renderer.
 */
final class HdTex {
    private HdTex() { }

    static double frac(double v) { return v - Math.floor(v); }

    static CTex.T paint(String name) {
        switch (name) {
            // ------------------------------------------------------------ fabrics
            case "hd_felt": return pix(128, 128, (u, v, x, y) -> {
                double n = fbm(u * 12, v * 12, 12, 12, 4, 1001);
                double fib = fbm(u * 64, v * 64, 64, 64, 2, 1002);
                return 0xFF000000 | shade(0xFFFFFF, 0.8 + 0.12 * n + 0.08 * fib + 0.03 * (hash(x, y, 1003) - 0.5));
            });
            case "hd_velvet": return pix(128, 128, (u, v, x, y) -> {
                double n = fbm(u * 8, v * 8, 8, 8, 3, 1011);
                double pile = fbm(u * 48, v * 48, 48, 48, 2, 1012);
                return 0xFF000000 | shade(0xFFFFFF, 0.74 + 0.16 * n + 0.1 * pile);
            });
            case "hd_cotton": return pix(128, 128, (u, v, x, y) -> {
                boolean wx = ((x / 2) + (y / 2)) % 2 == 0;
                double n = fbm(u * 10, v * 10, 10, 10, 3, 1021);
                return 0xFF000000 | shade(0xFFFFFF, (wx ? 0.97 : 0.9) * (0.9 + 0.1 * n));
            });
            case "hd_knit": return pix(128, 128, (u, v, x, y) -> {
                // rib knit: columns of V stitches
                double cu = u * 24, cv = v * 16;
                double fx = frac(cu) - 0.5, fy = frac(cv + Math.abs(fx) * 0.9);
                double st = 0.72 + 0.28 * Math.sin(Math.PI * fy) * (1 - Math.abs(fx) * 1.2);
                double gap = Math.abs(fx) > 0.44 ? 0.8 : 1;
                return 0xFF000000 | shade(0xFFFFFF, st * gap * (0.94 + 0.08 * fbm(u * 6, v * 6, 6, 6, 2, 1031)));
            });
            case "hd_fur": return pix(128, 128, (u, v, x, y) -> {
                double s = fbm(u * 40, v * 5, 40, 5, 3, 1041), c = fbm(u * 10, v * 10, 10, 10, 3, 1042);
                return 0xFF000000 | shade(0xFFFFFF, 0.76 + 0.2 * s + 0.08 * c);
            });
            case "hd_fluff": return pix(128, 128, (u, v, x, y) -> {
                double n = fbm(u * 16, v * 16, 16, 16, 4, 1051);
                double curl = 0.5 + 0.5 * Math.sin((u * 50 + n * 8) + Math.cos(v * 50 + n * 7));
                return 0xFF000000 | shade(0xFFFFFF, 0.8 + 0.12 * n + 0.08 * curl);
            });
            case "hd_satin": return pix(64, 128, (u, v, x, y) -> {
                double s = 0.72 + 0.22 * Math.pow(Math.max(0, Math.sin(u * Math.PI * 2 + v * 3)), 4) + 0.06 * Math.sin(v * 50);
                return 0xFF000000 | shade(0xFFFFFF, Math.min(1, s));
            });
            case "hd_leather": return pix(128, 128, (u, v, x, y) -> {
                double n = fbm(u * 20, v * 20, 20, 20, 4, 1061);
                double cell = Math.abs(fbm(u * 32, v * 32, 32, 32, 2, 1062) - 0.5);
                double crease = cell < 0.03 ? 0.86 : 1;
                double scr = fbm(u * 3, v * 3, 3, 3, 3, 1063);
                return 0xFF000000 | shade(0xFFFFFF, (0.8 + 0.14 * n + 0.08 * scr) * crease);
            });
            case "hd_stitch": return pix(128, 16, (u, v, x, y) -> {
                boolean dash = (x % 8) < 5 && Math.abs(v - 0.5) < 0.22;
                double base = 0.72 + 0.1 * fbm(u * 16, v, 16, 1, 2, 1071);
                return 0xFF000000 | (dash ? 0xF4E6C8 : shade(0xFFFFFF, base));
            });
            // ------------------------------------------------------------ metals / hard materials
            case "hd_gold": return metal(0xFFF6C8, 0xF0C24A, 0xB0781A, 0x5A3608, 1081);
            case "hd_silver": return metal(0xFFFFFF, 0xD4DAE4, 0x8A94A6, 0x3A4250, 1082);
            case "hd_steel": return pix(128, 128, (u, v, x, y) -> {
                double br = fbm(u * 2, v * 90, 2, 90, 2, 1091);
                double env = 0.5 + 0.5 * Math.sin(v * Math.PI * 2 * 1.5 + 0.8);
                return 0xFF000000 | shade(0xFFFFFF, 0.62 + 0.26 * env + 0.12 * br);
            });
            case "hd_chrome": return pix(64, 128, (u, v, x, y) -> {
                double h = 1 - v;
                int c = h > 0.52 ? mix(0xBFD8FF, 0xFFFFFF, (h - 0.52) / 0.48) : ramp(h / 0.52, 0x2A2E36, 0x6A707C, 0xE8ECF4);
                return 0xFF000000 | add(c, 0xFFFFFF, Math.exp(-Math.pow((h - 0.55) / 0.02, 2)) * 0.6);
            });
            case "hd_plastic": return pix(64, 64, (u, v, x, y) -> 0xFF000000 | shade(0xFFFFFF, 0.9 + 0.06 * fbm(u * 4, v * 4, 4, 4, 2, 1101) + 0.02 * (hash(x, y, 1102) - 0.5)));
            case "hd_rubber": return pix(64, 64, (u, v, x, y) -> 0xFF000000 | shade(0xFFFFFF, 0.78 + 0.1 * fbm(u * 24, v * 24, 24, 24, 2, 1111)));
            case "hd_gem": return pix(64, 64, (u, v, x, y) -> {
                // facets: angular sectors with different brightness + sparkle
                double a = Math.atan2(v - 0.5, u - 0.5), d = Math.hypot(u - 0.5, v - 0.5);
                int sec = (int) Math.floor((a + Math.PI) / (Math.PI / 4)) + (d > 0.3 ? 8 : 0);
                double b = 0.62 + 0.38 * hash(sec, 3, 1121);
                double sp = Math.exp(-(Math.pow(u - 0.34, 2) + Math.pow(v - 0.3, 2)) / 0.003);
                return 0xFF000000 | add(shade(0xFFFFFF, b), 0xFFFFFF, sp);
            });
            case "hd_glass": return pix(64, 64, (u, v, x, y) -> {
                double streak = Math.exp(-Math.pow((u * 1.2 + v * 0.8 - 0.55) / 0.06, 2)) * 0.6 + Math.exp(-Math.pow((u * 1.2 + v * 0.8 - 0.78) / 0.025, 2)) * 0.5;
                return argb(add(0xBFE8FF, 0xFFFFFF, streak), 0.2 + streak * 0.7);
            });
            case "hd_lens": return pix(128, 64, (u, v, x, y) -> {
                // dark tinted lens with a sky reflection and a sharp diagonal highlight
                int c = ramp(v, 0x3A4A7A, 0x161A2A, 0x0A0A12, 0x2A1A3A);
                double streak = Math.exp(-Math.pow((u * 0.9 - v * 0.55 - 0.08) / 0.05, 2)) * 0.55 + Math.exp(-Math.pow((u * 0.9 - v * 0.55 - 0.22) / 0.018, 2)) * 0.4;
                double top = Math.exp(-Math.pow(v / 0.18, 2)) * 0.25;
                return argb(add(c, 0xFFFFFF, streak + top), 0.93);
            });
            case "hd_bone": return pix(64, 128, (u, v, x, y) -> {
                double n = fbm(u * 6, v * 14, 6, 14, 3, 1131);
                int c = mix(0xF4ECD8, 0xC8B89A, v * 0.6 + n * 0.3);
                return 0xFF000000 | shade(c, 0.9 + 0.12 * Math.sin(v * Math.PI * 2 * 9 + n * 3));
            });
            case "hd_horn": return pix(64, 256, (u, v, x, y) -> {
                double n = fbm(u * 6, v * 30, 6, 30, 3, 1141);
                double ring = 0.82 + 0.18 * Math.pow(Math.abs(Math.sin(v * Math.PI * 14 + n * 1.5)), 0.6);
                int c = ramp(v, 0x2A0406, 0x6A0E12, 0xB4201E, 0xF0542E);
                return 0xFF000000 | add(shade(c, ring * (0.85 + 0.25 * n)), 0xFFC08A, Math.pow(v, 6) * 0.25);
            });
            case "hd_ivory": return pix(64, 256, (u, v, x, y) -> {
                double n = fbm(u * 6, v * 24, 6, 24, 3, 1151);
                double ring = 0.86 + 0.14 * Math.pow(Math.abs(Math.sin(v * Math.PI * 10 + n * 1.2)), 0.5);
                int c = ramp(v, 0x8A7250, 0xD8C8A2, 0xFFF6E0);
                return 0xFF000000 | shade(c, ring);
            });
            case "hd_wood": return pix(128, 128, (u, v, x, y) -> {
                double n = fbm(u * 3, v * 14, 3, 14, 4, 1161);
                double gr = 0.5 + 0.5 * Math.sin((u * 26 + n * 7) * Math.PI);
                return 0xFF000000 | shade(ramp(gr, 0x6A3A1A, 0xA2622E, 0xC88A4A), 0.9 + 0.12 * n);
            });
            case "hd_maple": return pix(128, 256, (u, v, x, y) -> {
                // guitar body: sunburst over flamed maple
                double n = fbm(u * 4, v * 30, 4, 30, 3, 1171);
                double flame = 0.5 + 0.5 * Math.sin(v * 120 + n * 9);
                double d = Math.hypot((u - 0.5) * 1.6, (v - 0.55) * 1.1);
                int base = mix(0xFFB02A, 0xE0501A, Capes.smooth(0.2, 0.55, d));
                base = mix(base, 0x2A0A06, Capes.smooth(0.45, 0.72, d));
                return 0xFF000000 | shade(base, 0.86 + 0.18 * flame);
            });
            case "hd_brick": return pix(64, 64, (u, v, x, y) -> 0xFF000000 | shade(0xFFFFFF, 0.93 + 0.04 * fbm(u * 3, v * 3, 3, 3, 2, 1181) + (hash(x, y, 1182) > 0.995 ? -0.06 : 0)));
            case "hd_lego_logo": return img(64, 64, g -> {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, 64, 64);
                g.setColor(new Color(0xC8C8C8));
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 15));
                g.drawString("LEGO", 12, 38);
            });
            // ------------------------------------------------------------ nature
            case "hd_petal": return pix(64, 64, (u, v, x, y) -> {
                double vein = Math.exp(-Math.pow((u - 0.5) / 0.03, 2)) * 0.12 + 0.05 * Math.sin(u * 40) * (1 - v);
                double base = 0.72 + 0.28 * v;
                return 0xFF000000 | shade(0xFFFFFF, Math.min(1, base - vein + 0.06 * fbm(u * 8, v * 8, 8, 8, 2, 1191)));
            });
            case "hd_leaf": return pix(64, 64, (u, v, x, y) -> {
                double mid = Math.exp(-Math.pow((v - 0.5) / 0.03, 2));
                double side = Math.exp(-Math.pow(frac((u * 5 + Math.abs(v - 0.5) * 3)) - 0.5, 2) / 0.004) * 0.15;
                int c = mix(0x2E8A34, 0x7ACB54, 0.3 + 0.5 * fbm(u * 5, v * 5, 5, 5, 3, 1201));
                return 0xFF000000 | add(shade(c, 0.95 - side), 0xC8F0A0, mid * 0.5);
            });
            case "hd_leafw": return pix(64, 64, (u, v, x, y) -> {
                double mid = Math.exp(-Math.pow((v - 0.5) / 0.03, 2));
                double side = Math.exp(-Math.pow(frac((u * 5 + Math.abs(v - 0.5) * 3)) - 0.5, 2) / 0.004) * 0.15;
                double n = fbm(u * 5, v * 5, 5, 5, 3, 1202);
                return 0xFF000000 | add(shade(0xFFFFFF, 0.78 + 0.18 * n - side), 0xFFFFFF, mid * 0.25);
            });
            case "hd_vine": return pix(64, 32, (u, v, x, y) -> 0xFF000000 | shade(mix(0x3A6A22, 0x6A9A3A, fbm(u * 10, v * 4, 10, 4, 3, 1211)), 0.85 + 0.15 * Math.sin(u * 60)));
            case "hd_mush": return pix(256, 128, (u, v, x, y) -> {
                // red cap, glossy, darker to the rim (v = 1 at the rim)
                double n = fbm(u * 10, v * 6, 10, 6, 3, 1221);
                return 0xFF000000 | shade(mix(0xF0342A, 0x9A1210, Math.pow(v, 1.5) * 0.8), 0.9 + 0.12 * n);
            });
            case "hd_gill": return pix(256, 32, (u, v, x, y) -> 0xFF000000 | shade(0xF0E6D2, 0.78 + 0.22 * Math.abs(Math.sin(u * Math.PI * 64)) - 0.2 * v));
            // ------------------------------------------------------------ decorated
            case "hd_party": return img(256, 128, g -> {
                g.setColor(new Color(0x2AB8F0));
                g.fillRect(0, 0, 256, 128);
                int[] cs = {0xFF3A8A, 0xFFE24A, 0x7A4AFF, 0x3AE07A};
                g.setStroke(new BasicStroke(12f));
                for (int i = -4; i < 12; i++) {
                    g.setColor(new Color(cs[(i + 8) % 4]));
                    g.drawLine(i * 32, 128, i * 32 + 96, 0);
                }
                java.util.Random r = new java.util.Random(1231);
                for (int i = 0; i < 70; i++) {
                    g.setColor(i % 2 == 0 ? Color.WHITE : new Color(0xFFF08A));
                    double s = 3 + r.nextDouble() * 4;
                    g.fill(new Ellipse2D.Double(r.nextDouble() * 256, r.nextDouble() * 128, s, s));
                }
            });
            case "hd_wizard": return img(256, 256, g -> {
                g.setPaint(new GradientPaint(0, 0, new Color(0x3A2AB0), 0, 256, new Color(0x1A1060)));
                g.fillRect(0, 0, 256, 256);
                java.util.Random r = new java.util.Random(1241);
                for (int i = 0; i < 26; i++) {
                    double x = r.nextDouble() * 256, y = 20 + r.nextDouble() * 220, s = 7 + r.nextDouble() * 10;
                    g.setColor(new Color(0xFFD84A));
                    if (i % 3 == 0) {
                        Path2D m = new Path2D.Double();
                        m.append(new Ellipse2D.Double(x - s, y - s, s * 2, s * 2), false);
                        g.fill(m);
                        g.setColor(new Color(0x2A1E8A));
                        g.fill(new Ellipse2D.Double(x - s * 0.55, y - s * 1.1, s * 2, s * 2));
                    } else g.fill(XmasTex.starShape(x, y, s, s * 0.42, 5));
                }
                for (int i = 0; i < 200; i++) {
                    g.setColor(col(0xFFFFFF, 0.3 + r.nextDouble() * 0.5));
                    g.fill(new Ellipse2D.Double(r.nextDouble() * 256, r.nextDouble() * 256, 1.4, 1.4));
                }
            });
            case "hd_witchband": return img(256, 32, g -> {
                g.setPaint(new GradientPaint(0, 0, new Color(0x5AE07A), 0, 32, new Color(0x1A7A3A)));
                g.fillRect(0, 0, 256, 32);
                g.setColor(col(0x000000, 0.25));
                for (int i = 0; i < 256; i += 4) g.drawLine(i, 0, i, 32);
                g.setColor(col(0xFFFFFF, 0.35));
                g.fillRect(0, 3, 256, 2);
            });
            case "hd_hatband": return img(256, 32, g -> {
                g.setPaint(new GradientPaint(0, 0, new Color(0xE0303A), 0, 32, new Color(0x7A0A12)));
                g.fillRect(0, 0, 256, 32);
                g.setColor(col(0x000000, 0.22));
                for (int i = 0; i < 32; i += 3) g.drawLine(0, i, 256, i);
                g.setColor(col(0xFFFFFF, 0.3));
                g.fillRect(0, 4, 256, 2);
            });
            case "hd_teddyface": return img(128, 128, g -> {
                g.setColor(new Color(0xB07A48));
                g.fillRect(0, 0, 128, 128);
                g.setColor(new Color(0x1A0E08));
                g.fill(new Ellipse2D.Double(32, 40, 16, 18));
                g.fill(new Ellipse2D.Double(80, 40, 16, 18));
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(36, 43, 6, 6));
                g.fill(new Ellipse2D.Double(84, 43, 6, 6));
                g.setColor(col(0xFF7A9A, 0.5));
                g.fill(new Ellipse2D.Double(20, 64, 20, 12));
                g.fill(new Ellipse2D.Double(88, 64, 20, 12));
            });
            case "hd_target": return img(256, 256, g -> {
                // knight shield face (quartered blue / white with a golden cross)
                g.setColor(new Color(0x1E4AB8));
                g.fillRect(0, 0, 256, 256);
                g.setColor(new Color(0xF2F2F6));
                g.fillRect(0, 0, 128, 128);
                g.fillRect(128, 128, 128, 128);
                g.setColor(col(0x000000, 0.15));
                java.util.Random r = new java.util.Random(1251);
                for (int i = 0; i < 400; i++) g.fill(new Ellipse2D.Double(r.nextDouble() * 256, r.nextDouble() * 256, 2, 2));
            });
            case "hd_rocket": return img(128, 256, g -> {
                g.setPaint(new GradientPaint(0, 0, new Color(0xFFFFFF), 128, 0, new Color(0xE4E8EE)));
                g.fillRect(0, 0, 128, 256);
                g.setColor(col(0x000000, 0.18));
                g.setStroke(new BasicStroke(1.4f));
                for (int y = 30; y < 256; y += 46) g.drawLine(0, y, 128, y);
                for (int y = 12; y < 256; y += 23)
                    for (int x = 4; x < 128; x += 16) g.fill(new Ellipse2D.Double(x, y, 2.4, 2.4));
                g.setColor(new Color(0xD81E28));
                g.fillRect(0, 200, 128, 14);
            });
            case "hd_note": return img(64, 64, g -> {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, 64, 64);
            });
            case "hd_rune": return img(512, 64, g -> {
                // glowing runes band (u around the circle)
                g.setColor(new Color(0, 0, 0, 0));
                g.fillRect(0, 0, 512, 64);
                g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                java.util.Random r = new java.util.Random(1261);
                for (int i = 0; i < 16; i++) {
                    double cx = i * 32 + 16;
                    Path2D p = new Path2D.Double();
                    int strokes = 2 + r.nextInt(3);
                    for (int k = 0; k < strokes; k++) {
                        p.moveTo(cx + (r.nextDouble() - 0.5) * 18, 12 + r.nextDouble() * 10);
                        p.lineTo(cx + (r.nextDouble() - 0.5) * 18, 36 + r.nextDouble() * 16);
                    }
                    p.moveTo(cx - 9, 32);
                    p.lineTo(cx + 9, 26 + r.nextDouble() * 12);
                    g.setColor(col(0xC8A0FF, 0.35));
                    g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.draw(p);
                    g.setColor(Color.WHITE);
                    g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.draw(p);
                }
            });
            case "hd_bubble": return pix(64, 64, (u, v, x, y) -> {
                // thin film: iridescent rim, clear center, highlight
                double h = frac(u * 2 + v * 1.3 + 0.2 * Math.sin(v * 9));
                int c = Capes.hsv(h, 0.45, 1);
                double spec = Math.exp(-(Math.pow(u - 0.3, 2) + Math.pow(v - 0.3, 2)) / 0.004);
                return argb(add(c, 0xFFFFFF, spec), 0.28 + spec * 0.7);
            });
            case "hd_softglow": return pix(64, 64, (u, v, x, y) -> {
                double d = Math.hypot(u - 0.5, v - 0.5) * 2;
                return argb(0xFFFFFF, Math.pow(Math.max(0, 1 - d), 2.2));
            });
            case "hd_trail": return pix(128, 16, (u, v, x, y) -> argb(0xFFFFFF, Math.pow(u, 1.6) * Math.pow(Math.sin(Math.PI * v), 1.5)));
            case "hd_gloss": return pix(64, 64, (u, v, x, y) -> {
                double d = Math.hypot(u - 0.32, v - 0.3);
                return 0xFF000000 | add(shade(0xFFFFFF, 0.8 + 0.2 * (1 - v)), 0xFFFFFF, Math.exp(-d * d / 0.004) * 0.8);
            });
            case "hd_foxear": return pix(64, 64, (u, v, x, y) -> {
                double n = fbm(u * 24, v * 4, 24, 4, 3, 1311);
                double tip = Capes.smooth(0.42, 0.18, v + (n - 0.5) * 0.12);
                int c = mix(mix(0xF07A28, 0xD8561A, v), 0x2A1A14, tip);
                return 0xFF000000 | shade(c, 0.82 + 0.25 * n);
            });
            case "hd_mustache": return pix(64, 64, (u, v, x, y) -> {
                double hair = fbm(u * 60, v * 4, 60, 4, 2, 1271);
                return 0xFF000000 | shade(0xFFFFFF, 0.7 + 0.3 * hair);
            });
            case "hd_mask": return img(128, 64, g -> {
                g.setPaint(new GradientPaint(0, 0, new Color(0xFFFFFF), 0, 64, new Color(0xD8D8E0)));
                g.fillRect(0, 0, 128, 64);
                g.setColor(col(0x000000, 0.1));
                java.util.Random r = new java.util.Random(1281);
                for (int i = 0; i < 90; i++) {
                    double x = r.nextDouble() * 128, y = r.nextDouble() * 64;
                    g.draw(new Ellipse2D.Double(x, y, 5, 3));
                }
                g.setColor(col(0xFFFFFF, 0.8));
                for (int i = 0; i < 40; i++) g.fill(new Ellipse2D.Double(r.nextDouble() * 128, r.nextDouble() * 64, 1.5, 1.5));
            });
            case "hd_screen": return pix(256, 64, (u, v, x, y) -> {
                // cyber visor: dark glass with scan lines and a HUD grid
                double scan = (y % 3 == 0) ? 0.75 : 1;
                boolean grid = (x % 16 == 0) || (y % 16 == 0);
                int c = mix(0x0A0616, 0x2A0A3A, v);
                if (grid) c = add(c, 0xFF3A8A, 0.18);
                double edge = Math.exp(-Math.pow((v - 0.08) / 0.05, 2)) * 0.35;
                return 0xFF000000 | add(shade(c, scan), 0xFF7AB0, edge);
            });
            case "hd_ninja": return pix(128, 64, (u, v, x, y) -> {
                double n = fbm(u * 16, v * 8, 16, 8, 3, 1291);
                double fold = 0.9 + 0.1 * Math.sin(v * 30 + n * 4);
                return 0xFF000000 | shade(0xFFFFFF, (0.78 + 0.16 * n) * fold);
            });
            case "hd_quiver": return pix(128, 128, (u, v, x, y) -> {
                double n = fbm(u * 16, v * 16, 16, 16, 3, 1301);
                boolean band = Math.abs(v - 0.15) < 0.04 || Math.abs(v - 0.85) < 0.04;
                double tool = 0.5 + 0.5 * Math.sin(u * 60 + v * 30);
                int c = band ? 0xE8B94A : mix(0x6A3A1A, 0x8A5028, n);
                if (!band && Math.abs(v - 0.5) < 0.2) c = shade(c, 0.85 + 0.15 * tool);
                return 0xFF000000 | c;
            });
            default: return null;
        }
    }

    /** Polished metal (colored, not tinted): environment reflection bands + micro scratches. */
    static CTex.T metal(int hi, int mid, int lo, int deep, int seed) {
        return pix(128, 128, (u, v, x, y) -> {
            double env = 0.5 + 0.5 * Math.sin(v * Math.PI * 2 * 1.2 + Math.sin(u * Math.PI * 2) * 0.6);
            double scr = fbm(u * 3, v * 80, 3, 80, 2, seed);
            int c = ramp(Math.min(1, env * 0.85 + scr * 0.2), deep, lo, mid, hi);
            double spark = hash(x, y, seed + 1) > 0.997 ? 0.6 : 0;
            return 0xFF000000 | add(c, 0xFFFFFF, spark + Math.exp(-Math.pow((env - 0.95) / 0.05, 2)) * 0.35);
        });
    }
}
