package dev.lego.cosmetic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.image.BufferedImage;
import java.util.Random;

/** Procedural textures of the vehicles (names "veh_*"). */
final class VehTex {
    private VehTex() { }

    static CTex.T tex(String name) {
        int s = 64;
        if (name.equals("veh_plate") || name.equals("veh_gauges") || name.equals("veh_deckart") || name.startsWith("veh_display")
                || name.startsWith("veh_num")) s = 128;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        dev.lego.ui.Gx.hints(g);
        try {
            if (!paint(g, name, s)) return null;
        } finally {
            g.dispose();
        }
        return CTex.of(img);
    }

    private static int hex(String name, int from) {
        try { return Integer.parseInt(name.substring(from, Math.min(name.length(), from + 6)), 16); } catch (RuntimeException e) { return 0xFF00FF; }
    }

    private static boolean paint(Graphics2D g, String name, int s) {
        if (name.startsWith("veh_display")) {
            int v = 0;
            int at = name.indexOf('@');
            if (at > 0) try { v = Integer.parseInt(name.substring(at + 1)); } catch (NumberFormatException ignored) { }
            g.setPaint(new GradientPaint(0, 0, new Color(10, 30, 44), 0, s, new Color(4, 12, 20)));
            g.fillRect(0, 0, s, s);
            g.setColor(new Color(90, 230, 255));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, s / 3));
            String num = String.valueOf(v);
            g.drawString(num, s * 3 / 5 - g.getFontMetrics().stringWidth(num), s * 2 / 3);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, s / 8));
            g.drawString("km/h", s * 5 / 8, s * 2 / 3);
            int bars = Math.min(5, (v + 9) / 10);
            for (int i = 0; i < 5; i++) { g.setColor(i < bars ? (i >= 4 ? new Color(255, 90, 70) : new Color(80, 255, 120)) : new Color(40, 70, 50)); g.fillRect(s / 5 + i * s / 9, s * 4 / 5, s / 12, s / 14); }
            return true;
        }
        if (name.startsWith("veh_c:")) {
            Color c = new Color(hex(name, 6));
            g.setPaint(new GradientPaint(0, 0, bright(c, 1.2f), 0, s, bright(c, 0.78f)));
            g.fillRect(0, 0, s, s);
            return true;
        }
        if (name.startsWith("veh_flat:")) {
            g.setColor(new Color(hex(name, 9)));
            g.fillRect(0, 0, s, s);
            noise(g, s, 12, 300);
            return true;
        }
        if (name.startsWith("veh_glow:")) {
            Color c = new Color(hex(name, 9));
            g.setColor(c);
            g.fillRect(0, 0, s, s);
            g.setPaint(new RadialGradientPaint(s / 2f, s / 2f, s * 0.6f, new float[]{0, 1}, new Color[]{new Color(255, 255, 255, 150), new Color(255, 255, 255, 0)}));
            g.fillRect(0, 0, s, s);
            return true;
        }
        if (name.startsWith("veh_paint:")) {
            // glossy car paint: sky reflection on top, sharp horizon line, darker body below
            Color c = new Color(hex(name, 10));
            g.setPaint(new LinearGradientPaint(0, 0, 0, s, new float[]{0, 0.3f, 0.44f, 0.46f, 0.7f, 1},
                    new Color[]{bright(c, 1.45f), bright(c, 1.18f), bright(c, 1.05f), bright(c, 0.62f), bright(c, 0.82f), bright(c, 0.5f)}));
            g.fillRect(0, 0, s, s);
            return true;
        }
        if (name.startsWith("veh_leather:")) {
            Color c = new Color(hex(name, 12));
            g.setColor(c);
            g.fillRect(0, 0, s, s);
            g.setPaint(new GradientPaint(0, 0, bright(c, 1.18f), 0, s, bright(c, 0.85f)));
            g.fillRect(0, 0, s, s);
            noise(g, s, 16, 700);
            g.setColor(bright(c, 1.5f));
            g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{2, 2}, 0));
            g.drawLine(0, 4, s, 4);
            g.drawLine(0, s - 5, s, s - 5);
            return true;
        }
        switch (name) {
            case "veh_tread": {
                g.setColor(new Color(30, 30, 34));
                g.fillRect(0, 0, s, s);
                g.setColor(new Color(14, 14, 16));
                for (int i = 0; i < s; i += 8) { g.fillRect(i, 0, 2, s); g.fillRect(i + 4, s / 3, 2, s / 3); }
                g.fillRect(0, s / 2 - 2, s, 3);
                return true;
            }
            case "veh_side": {
                g.setColor(new Color(36, 36, 40));
                g.fillRect(0, 0, s, s);
                g.setColor(new Color(58, 58, 64));
                g.fillRect(0, s / 2 - 3, s, 2);
                return true;
            }
            case "veh_metal": case "veh_rim": {
                g.setPaint(new GradientPaint(0, 0, new Color(236, 240, 246), s, s, new Color(126, 134, 146)));
                g.fillRect(0, 0, s, s);
                g.setColor(new Color(255, 255, 255, 40));
                for (int i = 0; i < s; i += 3) g.drawLine(0, i, s, i);
                return true;
            }
            case "veh_chrome": {
                g.setPaint(new LinearGradientPaint(0, 0, 0, s, new float[]{0, 0.4f, 0.5f, 0.62f, 1},
                        new Color[]{new Color(255, 255, 255), new Color(190, 204, 222), new Color(70, 76, 88), new Color(220, 226, 234), new Color(150, 156, 166)}));
                g.fillRect(0, 0, s, s);
                return true;
            }
            case "veh_alu": {
                g.setColor(new Color(186, 192, 202));
                g.fillRect(0, 0, s, s);
                Random r = new Random(5);
                for (int i = 0; i < 220; i++) {
                    g.setColor(r.nextBoolean() ? new Color(255, 255, 255, 50) : new Color(90, 96, 110, 40));
                    int y = r.nextInt(s);
                    g.drawLine(0, y, s, y);
                }
                return true;
            }
            case "veh_gun": {                         // dark anodised metal
                g.setPaint(new GradientPaint(0, 0, new Color(96, 100, 112), s, s, new Color(38, 40, 48)));
                g.fillRect(0, 0, s, s);
                return true;
            }
            case "veh_disc": {
                g.setColor(new Color(150, 154, 160));
                g.fillRect(0, 0, s, s);
                g.setColor(new Color(90, 92, 98));
                for (int i = 0; i < s; i += 6) g.fillOval(i + 1, (i * 7) % (s - 6) + 2, 3, 3);
                return true;
            }
            case "veh_carbon": {
                for (int y = 0; y < s; y += 4)
                    for (int x = 0; x < s; x += 4) {
                        boolean k = ((x + y) / 4 & 1) == 0;
                        g.setPaint(new GradientPaint(x, y, k ? new Color(58, 60, 68) : new Color(24, 25, 30), x + 4, y + 4, k ? new Color(30, 31, 36) : new Color(46, 48, 54)));
                        g.fillRect(x, y, 4, 4);
                    }
                return true;
            }
            case "veh_under": {
                g.setColor(new Color(22, 23, 27));
                g.fillRect(0, 0, s, s);
                noise(g, s, 10, 200);
                return true;
            }
            case "veh_interior": {
                g.setColor(new Color(34, 32, 34));
                g.fillRect(0, 0, s, s);
                noise(g, s, 16, 900);
                return true;
            }
            case "veh_grille": {
                g.setColor(new Color(16, 16, 20));
                g.fillRect(0, 0, s, s);
                g.setColor(new Color(62, 64, 72));
                g.setStroke(new BasicStroke(1.4f));
                for (int y = 0; y < s; y += 6)
                    for (int x = (y / 6 % 2) * 4; x < s; x += 8) g.drawOval(x, y, 6, 5);
                return true;
            }
            case "veh_gauges": {
                g.setColor(new Color(18, 18, 22));
                g.fillRect(0, 0, s, s);
                for (int i = 0; i < 2; i++) {
                    int cx = s / 4 + i * s / 2, cy = s / 2, r = s / 5;
                    g.setColor(new Color(40, 42, 50));
                    g.fillOval(cx - r - 3, cy - r - 3, 2 * r + 6, 2 * r + 6);
                    g.setColor(new Color(12, 12, 16));
                    g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
                    g.setColor(new Color(255, 255, 255, 200));
                    for (int k = 0; k <= 8; k++) {
                        double a = Math.toRadians(225 - k * 33.75);
                        g.drawLine(cx + (int) (Math.cos(a) * r * 0.7), cy - (int) (Math.sin(a) * r * 0.7), cx + (int) (Math.cos(a) * r * 0.92), cy - (int) (Math.sin(a) * r * 0.92));
                    }
                    g.setColor(new Color(255, 70, 40));
                    g.setStroke(new BasicStroke(2f));
                    double a = Math.toRadians(i == 0 ? 120 : 60);
                    g.drawLine(cx, cy, cx + (int) (Math.cos(a) * r * 0.85), cy - (int) (Math.sin(a) * r * 0.85));
                }
                return true;
            }
            case "veh_display_dyn": {
                g.setPaint(new GradientPaint(0, 0, new Color(10, 30, 44), 0, s, new Color(4, 12, 20)));
                g.fillRect(0, 0, s, s);
                g.setColor(new Color(90, 230, 255));
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, s / 3));
                g.drawString("25", s / 5, s * 2 / 3);
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, s / 8));
                g.drawString("km/h", s * 3 / 5, s * 2 / 3);
                for (int i = 0; i < 5; i++) { g.setColor(i < 4 ? new Color(80, 255, 120) : new Color(40, 70, 50)); g.fillRect(s / 5 + i * s / 9, s * 4 / 5, s / 12, s / 14); }
                return true;
            }
            case "veh_plate": {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, s, s);
                g.setColor(new Color(20, 60, 170));
                g.fillRect(0, 0, s / 8, s);
                g.setColor(new Color(20, 20, 24));
                g.setStroke(new BasicStroke(3));
                g.drawRect(2, s / 4 + 2, s - 4, s / 2 - 4);
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, s / 4));
                g.drawString("LEGO 1", s / 6, s * 5 / 8);
                return true;
            }
            case "veh_rubber": {
                g.setColor(new Color(30, 31, 36));
                g.fillRect(0, 0, s, s);
                g.setColor(new Color(52, 54, 60));
                for (int i = 0; i < s; i += 5) g.fillRect(0, i, s, 2);
                return true;
            }
            case "veh_gripr": {                     // ribbed handle grip
                g.setColor(new Color(26, 26, 30));
                g.fillRect(0, 0, s, s);
                g.setColor(new Color(60, 62, 70));
                for (int i = 0; i < s; i += 6) g.fillRect(0, i, s, 2);
                return true;
            }
            case "veh_foam": {
                g.setColor(new Color(34, 34, 38));
                g.fillRect(0, 0, s, s);
                noise(g, s, 26, 1600);
                return true;
            }
            case "veh_grip": case "veh_griptape": {
                g.setColor(new Color(34, 35, 40));
                g.fillRect(0, 0, s, s);
                noise(g, s, 14, 1400);
                return true;
            }
            case "veh_deck": case "veh_ply": {
                // plywood edge: stacked veneers
                Color[] cs = {new Color(222, 176, 118), new Color(200, 60, 50), new Color(230, 190, 130), new Color(60, 120, 200), new Color(214, 168, 110)};
                for (int i = 0; i < s; i++) { g.setColor(cs[(i * cs.length) / s]); g.drawLine(0, i, s, i); }
                return true;
            }
            case "veh_deckart": {                   // bottom graphic of the skateboard (v across, u along)
                g.setPaint(new GradientPaint(0, 0, new Color(255, 205, 3), s, 0, new Color(255, 120, 20)));
                g.fillRect(0, 0, s, s);
                g.setColor(new Color(227, 0, 11));
                for (int i = -s; i < s * 2; i += 22) g.fillPolygon(new int[]{i, i + 10, i + 10 + s, i + s}, new int[]{0, 0, s, s}, 4);
                g.setColor(Color.WHITE);
                g.fillRoundRect(s / 2 - 22, s / 2 - 16, 44, 32, 10, 10);
                g.setColor(new Color(227, 0, 11));
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
                g.drawString("LEGO", s / 2 - 22, s / 2 + 7);
                return true;
            }
            case "veh_urethane": {
                g.setPaint(new GradientPaint(0, 0, new Color(255, 248, 226), 0, s, new Color(226, 212, 178)));
                g.fillRect(0, 0, s, s);
                return true;
            }
            case "veh_pu": {                        // translucent-looking coloured PU wheel
                g.setPaint(new GradientPaint(0, 0, new Color(120, 255, 214), 0, s, new Color(20, 170, 140)));
                g.fillRect(0, 0, s, s);
                return true;
            }
            case "veh_glass": {
                g.setPaint(new GradientPaint(0, 0, new Color(210, 238, 255, 110), s, s, new Color(90, 150, 210, 80)));
                g.fillRect(0, 0, s, s);
                g.setColor(new Color(255, 255, 255, 120));
                g.fillRect(s / 6, 0, s / 10, s);
                g.fillRect(s / 6 + s / 7, 0, s / 30, s);
                return true;
            }
            case "veh_tint": {
                g.setPaint(new GradientPaint(0, 0, new Color(60, 70, 90, 180), 0, s, new Color(20, 24, 34, 200)));
                g.fillRect(0, 0, s, s);
                g.setColor(new Color(255, 255, 255, 70));
                g.fillRect(s / 5, 0, s / 12, s);
                return true;
            }
            case "veh_mirror": {
                g.setPaint(new GradientPaint(0, 0, new Color(200, 225, 255), s, s, new Color(80, 100, 130)));
                g.fillRect(0, 0, s, s);
                return true;
            }
            case "veh_cloud": {
                g.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255), 0, s, new Color(206, 220, 240)));
                g.fillRect(0, 0, s, s);
                return true;
            }
            case "veh_checker": {
                for (int y = 0; y < 8; y++)
                    for (int x = 0; x < 8; x++) {
                        g.setColor((x + y) % 2 == 0 ? Color.WHITE : new Color(20, 20, 24));
                        g.fillRect(x * 8, y * 8, 8, 8);
                    }
                return true;
            }
            case "veh_ufo": {                       // saucer hull with panel lines
                g.setPaint(new GradientPaint(0, 0, new Color(214, 222, 234), 0, s, new Color(120, 130, 146)));
                g.fillRect(0, 0, s, s);
                g.setColor(new Color(80, 88, 104, 150));
                for (int i = 0; i < s; i += 8) g.drawLine(i, 0, i, s);
                g.drawLine(0, s / 2, s, s / 2);
                return true;
            }
            case "veh_chain": {
                g.setColor(new Color(60, 62, 68));
                g.fillRect(0, 0, s, s);
                g.setColor(new Color(150, 154, 162));
                for (int i = 0; i < s; i += 8) g.fillRoundRect(i, s / 4, 6, s / 2, 3, 3);
                return true;
            }
            case "veh_num": {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, s, s);
                g.setColor(new Color(20, 20, 24));
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, s * 3 / 4));
                g.drawString("7", s / 3, s * 4 / 5);
                return true;
            }
            case "veh_basket": {
                g.setColor(new Color(150, 104, 58));
                g.fillRect(0, 0, s, s);
                g.setColor(new Color(110, 72, 36));
                for (int i = 0; i < s; i += 6) { g.drawLine(i, 0, i, s); g.drawLine(0, i + 3, s, i + 3); }
                return true;
            }
            default:
                return false;
        }
    }

    static Color bright(Color c, float k) {
        return new Color(Math.min(255, (int) (c.getRed() * k)), Math.min(255, (int) (c.getGreen() * k)), Math.min(255, (int) (c.getBlue() * k)));
    }

    private static void noise(Graphics2D g, int s, int alpha, int n) {
        Random r = new Random(3);
        for (int i = 0; i < n; i++) {
            g.setColor(r.nextBoolean() ? new Color(255, 255, 255, alpha) : new Color(0, 0, 0, alpha));
            g.fillRect(r.nextInt(s), r.nextInt(s), 1, 1);
        }
    }
}
