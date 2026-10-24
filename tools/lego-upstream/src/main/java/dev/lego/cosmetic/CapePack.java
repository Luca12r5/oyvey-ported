package dev.lego.cosmetic;

import static dev.lego.cosmetic.Capes.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import dev.lego.cosmetic.Cos.Rarity;

/** Kosmetik-Paket 4: 60 capes (stripes, waves, emblems, flags, plasma, glitter ...), many of them animated. */
final class CapePack {
    private CapePack() { }

    private static final Rarity C = Rarity.COMMON, R = Rarity.RARE, E = Rarity.EPIC, L = Rarity.LEGENDARY;
    private static final java.util.List<Object[]> NAMES = new java.util.ArrayList<>();

    private static void cape(String id, String name, Rarity r, int frames, float fps, int trimHi, int trimLo, int lining, Painter p) {
        def("p4" + id, frames, fps, trimHi, trimLo, lining, p);
        NAMES.add(new Object[]{"p4" + id, name, r});
    }

    /** Called from Capes' static block (painters). */
    static void defineAll() {
        int gold = 0xFFE08A, goldLo = 0x9A6A12, silver = 0xFFFFFF, silverLo = 0x7A8494;
        // ---- vertical stripes (static)
        int[][] sv = {{0xE3000B, 0xFFFFFF}, {0x1B1B22, 0xFFCD03}, {0x006CB7, 0xFFFFFF}, {0x2E7D32, 0xF4F0E0}, {0x6A1B9A, 0xFFB0E0}};
        String[] svN = {"Streifen Rot-Weiß", "Streifen Schwarz-Gelb", "Streifen Blau-Weiß", "Streifen Grün-Creme", "Streifen Lila-Rosa"};
        for (int i = 0; i < sv.length; i++) { int[] c = sv[i]; cape("stripes" + i, svN[i], C, 1, 0, silver, silverLo, c[0], (g, im, ph, k, n) -> field(im, (u, v, x, y) -> ((int) (u * 7)) % 2 == 0 ? c[0] : c[1])); }
        // ---- diagonal stripes, scrolling
        int[][] dg = {{0xFF4FA3, 0xFFD23F}, {0x39D353, 0x0A2A12}, {0x4FC3FF, 0x0A1A3A}, {0xFF8A00, 0x2A0A00}, {0xFFFFFF, 0xE3000B}};
        String[] dgN = {"Bonbon-Streifen", "Neon-Diagonale Grün", "Neon-Diagonale Blau", "Warnstreifen", "Zuckerstangen-Cape"};
        for (int i = 0; i < dg.length; i++) { int[] c = dg[i]; cape("diag" + i, dgN[i], R, 16, 10, gold, goldLo, c[1], (g, im, ph, k, n) -> field(im, (u, v, x, y) -> { double s = fract(u * 4 + v * 2.5 - k / (double) n); return mix(c[0], c[1], smooth(0.45, 0.55, s) - smooth(0.95, 1.0, s)); })); }
        // ---- waves
        int[][] wv = {{0x0A2A6A, 0x2E86DE, 0x9FE8FF}, {0x3A0A2A, 0xC8327A, 0xFFB0D0}, {0x0A3A1A, 0x2EAD5A, 0xC8FFB0}, {0x2A0A00, 0xE0601A, 0xFFE07A}, {0x10102A, 0x6A4AE0, 0xE0D0FF}};
        String[] wvN = {"Meereswellen", "Rosa Wellen", "Grüne Wellen", "Sonnenwellen", "Violette Wellen"};
        for (int i = 0; i < wv.length; i++) { int[] c = wv[i]; cape("waves" + i, wvN[i], R, 16, 9, silver, silverLo, c[0], (g, im, ph, k, n) -> field(im, (u, v, x, y) -> { double w = Math.sin(v * 18 + Math.sin(u * 6 + ph) * 1.6 - ph * 2) * 0.5 + 0.5; return ramp(w * 0.85 + v * 0.15, c[0], c[1], c[2]); })); }
        // ---- checker
        int[][] ck = {{0x1B1B22, 0xF4F4F4}, {0xE3000B, 0x1B1B22}, {0x7A4AE0, 0x39D3C8}};
        String[] ckN = {"Schachbrett", "Rot-Schwarz kariert", "Retro-Karo"};
        for (int i = 0; i < ck.length; i++) { int[] c = ck[i]; cape("check" + i, ckN[i], C, 1, 0, silver, silverLo, c[0], (g, im, ph, k, n) -> field(im, (u, v, x, y) -> (((int) (u * 6) + (int) (v * 10)) % 2 == 0) ? c[0] : c[1])); }
        // ---- polka dots, pulsing
        int[][] pk = {{0xE3000B, 0xFFFFFF}, {0x1B1B22, 0xFF4FA3}, {0x006CB7, 0xFFCD03}, {0xFFE0EA, 0x4FC3FF}};
        String[] pkN = {"Punkte Rot", "Punkte Pink", "Punkte Gelb-Blau", "Pastell-Punkte"};
        for (int i = 0; i < pk.length; i++) { int[] c = pk[i]; cape("dots" + i, pkN[i], C, 8, 6, silver, silverLo, c[0], (g, im, ph, k, n) -> field(im, (u, v, x, y) -> {
            double gx = u * 5, gy = v * 8 + ((int) (u * 5) % 2) * 0.5;
            double dx = fract(gx) - 0.5, dy = fract(gy) - 0.5, r = 0.27 + 0.05 * Math.sin(ph + (int) gx * 1.3 + (int) gy);
            return Math.hypot(dx, dy * 0.85) < r ? c[1] : c[0]; })); }
        // ---- starfields
        int[][] sf = {{0x05061A, 0x1A1050, 0xFFFFFF}, {0x1A0A2A, 0x5A1A6A, 0xFFD0F0}, {0x001A1A, 0x0A4A5A, 0xBFFFF0}, {0x000000, 0x1A1A1A, 0xFFE07A}};
        String[] sfN = {"Sternennacht", "Lila Kosmos", "Polarnacht", "Goldene Sterne"};
        for (int i = 0; i < sf.length; i++) { int[] c = sf[i]; int seed = 11 + i; cape("stars" + i, sfN[i], E, 12, 6, gold, goldLo, c[0], (g, im, ph, k, n) -> {
            field(im, (u, v, x, y) -> ramp(v * 0.8 + fbm(u * 3, v * 5, 0, 0, 3, seed) * 0.4, c[0], c[1]));
            java.util.Random r = new java.util.Random(seed);
            for (int s = 0; s < 70; s++) {
                double sx = r.nextDouble() * FW, sy = r.nextDouble() * FH, sz = 0.8 + r.nextDouble() * 2.2, off = r.nextDouble() * 6.28;
                star(g, sx, sy, sz, c[2], 0.35 + 0.65 * (0.5 + 0.5 * Math.sin(ph + off)));
            }
        }); }
        // ---- emblems with a pulsing glow
        Object[][] em = {
                {"heart", "Großes Herz", 0x3A0A14, 0xB0122A, 0xFF4F7A},
                {"star", "Großer Stern", 0x0A1030, 0x2A3A9A, 0xFFD23F},
                {"bolt", "Blitz-Emblem", 0x10101A, 0x2A2A44, 0xFFF04A},
                {"diamond", "Diamant", 0x061A2A, 0x0A4A6A, 0x7CF7FF},
                {"moon", "Halbmond", 0x0A0A20, 0x1A2050, 0xFFF0C0},
                {"sun", "Sonne", 0xFF8A1A, 0xFFD23F, 0xFFFFFF},
                {"cross", "Ritterkreuz", 0xF4F4F4, 0xD8D8E0, 0xC0141C},
                {"brick", "Lego-Emblem", 0xFFCD03, 0xF2B800, 0xE3000B},
        };
        for (Object[] o : em) {
            String sh = (String) o[0];
            int bg0 = (int) o[2], bg1 = (int) o[3], fg = (int) o[4];
            cape("emb_" + sh, (String) o[1], E, 12, 8, gold, goldLo, bg0, (g, im, ph, k, n) -> {
                field(im, (u, v, x, y) -> mix(bg1, bg0, Math.hypot(u - 0.5, (v - 0.42) * 0.7) * 1.6));
                double pulse = 0.5 + 0.5 * Math.sin(ph);
                addDot(im, FW / 2.0, FH * 0.42, 70 + pulse * 10, fg & 0xFFFFFF, 0.25 + 0.2 * pulse);
                g.setColor(new Color(fg));
                g.fill(shape(sh, FW / 2.0, FH * 0.42, 52));
                g.setColor(new Color(255, 255, 255, 70));
                g.fill(shape(sh, FW / 2.0 - 4, FH * 0.42 - 6, 22));
            });
        }
        // ---- chevrons, moving down
        int[][] cv = {{0x1B1B22, 0x39D353}, {0x0A1A3A, 0xFF4FA3}, {0xF4F4F4, 0x006CB7}};
        String[] cvN = {"Pfeile Grün", "Pfeile Pink", "Pfeile Blau"};
        for (int i = 0; i < cv.length; i++) { int[] c = cv[i]; cape("chevron" + i, cvN[i], R, 16, 10, silver, silverLo, c[0], (g, im, ph, k, n) -> field(im, (u, v, x, y) -> fract(v * 6 - Math.abs(u - 0.5) * 2.4 - k / (double) n) < 0.42 ? c[1] : c[0])); }
        // ---- flags (static)
        Object[][] fl = {
                {"de", "Deutschland", new int[]{0x000000, 0xDD0000, 0xFFCE00}, true},
                {"at", "Österreich", new int[]{0xED2939, 0xFFFFFF, 0xED2939}, true},
                {"it", "Italien", new int[]{0x009246, 0xFFFFFF, 0xCE2B37}, false},
                {"fr", "Frankreich", new int[]{0x0055A4, 0xFFFFFF, 0xEF4135}, false},
                {"nl", "Niederlande", new int[]{0xAE1C28, 0xFFFFFF, 0x21468B}, true},
                {"be", "Belgien", new int[]{0x000000, 0xFDDA24, 0xEF3340}, false},
                {"pl", "Polen", new int[]{0xFFFFFF, 0xDC143C}, true},
                {"ua", "Ukraine", new int[]{0x0057B7, 0xFFD700}, true},
        };
        for (Object[] o : fl) {
            int[] c = (int[]) o[2];
            boolean horiz = (boolean) o[3];
            cape("flag_" + o[0], (String) o[1], C, 1, 0, silver, silverLo, c[0], (g, im, ph, k, n) -> field(im, (u, v, x, y) -> c[Math.min(c.length - 1, (int) ((horiz ? v : u) * c.length))]));
        }
        cape("flag_ch", "Schweiz", C, 1, 0, silver, silverLo, 0xDA291C, (g, im, ph, k, n) -> field(im, (u, v, x, y) -> {
            double dx = Math.abs(u - 0.5), dy = Math.abs(v - 0.45) * FH / FW;
            return (dx < 0.1 && dy < 0.32) || (dy < 0.1 && dx < 0.32) ? 0xFFFFFF : 0xDA291C; }));
        cape("flag_se", "Schweden", C, 1, 0, silver, silverLo, 0x006AA7, (g, im, ph, k, n) -> field(im, (u, v, x, y) -> (Math.abs(u - 0.38) < 0.09 || Math.abs(v - 0.42) < 0.055) ? 0xFECC00 : 0x006AA7));
        // ---- plasma / noise, animated
        int[][] pl = {{0x10002A, 0x7A2AE0, 0xFF6AF0, 0xFFFFFF}, {0x000A1A, 0x0A4A8A, 0x3AD8FF, 0xE0FFFF}, {0x0A1A00, 0x3A8A0A, 0xB8FF2A, 0xF8FFE0}, {0x1A0000, 0xA01A00, 0xFF8A1A, 0xFFF0B0}, {0x0A0A0A, 0x3A3A44, 0x9AA3B2, 0xFFFFFF}};
        String[] plN = {"Plasma", "Ozean-Plasma", "Toxisch", "Feuer-Plasma", "Rauch"};
        for (int i = 0; i < pl.length; i++) { int[] c = pl[i]; int seed = 40 + i; cape("plasma" + i, plN[i], L, 16, 8, gold, goldLo, c[0], (g, im, ph, k, n) -> field(im, (u, v, x, y) -> {
            double ox = Math.cos(ph) * 0.8, oy = Math.sin(ph) * 0.8;
            double q = fbm(u * 3 + ox, v * 5 + oy, 0, 0, 4, seed);
            double w = fbm(u * 3 + q * 2 - ox, v * 5 + q * 2 - oy, 0, 0, 4, seed + 7);
            return ramp(w, c[0], c[1], c[2], c[3]); })); }
        // ---- scales / hex
        int[][] sc = {{0x0A3A2A, 0x2AAE7A}, {0x3A0A0A, 0xD0402A}, {0x1A1A3A, 0x8A7AE0}};
        String[] scN = {"Smaragdschuppen", "Feuerschuppen", "Amethystschuppen"};
        for (int i = 0; i < sc.length; i++) { int[] c = sc[i]; cape("scales" + i, scN[i], E, 12, 6, gold, goldLo, c[0], (g, im, ph, k, n) -> field(im, (u, v, x, y) -> {
            double gx = u * 6, gy = v * 12, row = Math.floor(gy);
            double sx = fract(gx + (row % 2) * 0.5) - 0.5, sy = fract(gy);
            double d = Math.hypot(sx, sy * 0.9);
            double shine = 0.5 + 0.5 * Math.sin(ph - v * 6 + u * 2);
            return d < 0.5 ? mix(c[1], c[0], d * 1.6 + 0.2 * (1 - shine)) : c[0]; })); }
        // ---- gradients with glitter
        int[][] gl = {{0xFF4FA3, 0x7A2AE0}, {0x4FC3FF, 0x39D353}, {0xFFD23F, 0xFF4F2A}, {0x1B1B22, 0x4A4A5A}, {0xFFFFFF, 0x9FD8FF}};
        String[] glN = {"Glitzer Pink-Lila", "Glitzer Türkis", "Glitzer Sonnenuntergang", "Schwarzer Glitzer", "Eisglitzer"};
        for (int i = 0; i < gl.length; i++) { int[] c = gl[i]; int seed = 70 + i; cape("glitter" + i, glN[i], R, 8, 6, silver, silverLo, c[1], (g, im, ph, k, n) -> {
            field(im, (u, v, x, y) -> mix(c[0], c[1], v));
            java.util.Random r = new java.util.Random(seed);
            for (int s = 0; s < 120; s++) {
                double sx = r.nextDouble() * FW, sy = r.nextDouble() * FH, off = r.nextDouble() * 6.28;
                double a = Math.max(0, Math.sin(ph + off));
                if (a > 0.05) star(g, sx, sy, 0.8 + r.nextDouble() * 1.4, 0xFFFFFF, a);
            }
        }); }
    }

    private static java.awt.Shape shape(String sh, double cx, double cy, double s) {
        Path2D p = new Path2D.Double();
        switch (sh) {
            case "heart" -> {
                for (int i = 0; i <= 40; i++) {
                    double t = Math.PI * 2 * i / 40;
                    double x = 16 * Math.pow(Math.sin(t), 3) / 16, y = -(13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t)) / 16;
                    if (i == 0) p.moveTo(cx + x * s, cy + y * s); else p.lineTo(cx + x * s, cy + y * s);
                }
            }
            case "star" -> {
                for (int i = 0; i < 10; i++) {
                    double an = -Math.PI / 2 + Math.PI * i / 5, r = i % 2 == 0 ? s : s * 0.42;
                    if (i == 0) p.moveTo(cx + Math.cos(an) * r, cy + Math.sin(an) * r); else p.lineTo(cx + Math.cos(an) * r, cy + Math.sin(an) * r);
                }
            }
            case "bolt" -> {
                double[][] b = {{0.15, -1}, {-0.55, 0.1}, {-0.05, 0.1}, {-0.25, 1}, {0.55, -0.15}, {0.05, -0.15}, {0.35, -1}};
                for (int i = 0; i < b.length; i++) if (i == 0) p.moveTo(cx + b[i][0] * s, cy + b[i][1] * s); else p.lineTo(cx + b[i][0] * s, cy + b[i][1] * s);
            }
            case "diamond" -> { p.moveTo(cx, cy - s); p.lineTo(cx + s * 0.7, cy - s * 0.3); p.lineTo(cx, cy + s); p.lineTo(cx - s * 0.7, cy - s * 0.3); }
            case "moon" -> {
                java.awt.geom.Area a = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(cx - s, cy - s, s * 2, s * 2));
                a.subtract(new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(cx - s * 0.45, cy - s * 1.15, s * 2, s * 2)));
                return a;
            }
            case "sun" -> {
                for (int i = 0; i < 24; i++) {
                    double an = Math.PI * 2 * i / 24, r = i % 2 == 0 ? s : s * 0.72;
                    if (i == 0) p.moveTo(cx + Math.cos(an) * r, cy + Math.sin(an) * r); else p.lineTo(cx + Math.cos(an) * r, cy + Math.sin(an) * r);
                }
            }
            case "cross" -> {
                double w = s * 0.32;
                p.append(new java.awt.geom.Rectangle2D.Double(cx - w / 2, cy - s, w, s * 2), false);
                p.append(new java.awt.geom.Rectangle2D.Double(cx - s * 0.7, cy - s * 0.45, s * 1.4, w), false);
                return p;
            }
            default -> {
                p.append(new java.awt.geom.RoundRectangle2D.Double(cx - s, cy - s * 0.55, s * 2, s * 1.1, s * 0.2, s * 0.2), false);
                for (int i = -1; i <= 1; i += 2) p.append(new java.awt.geom.Ellipse2D.Double(cx + i * s * 0.5 - s * 0.28, cy - s * 0.85, s * 0.56, s * 0.4), false);
                return p;
            }
        }
        p.closePath();
        return p;
    }

    private static double fract(double v) { return v - Math.floor(v); }

    static void register() {
        Capes.frames("cape_lego");   // runs Capes' static block -> defineAll()
        for (Object[] o : NAMES) Cos.cape((String) o[0], (String) o[1], (Rarity) o[2]);
    }
}
