package dev.spotifyhud;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import static dev.spotifyhud.SettingsUi.CH;
import static dev.spotifyhud.SettingsUi.CW;

/** Paints the settings card (Java2D, crisp at any GUI scale). */
final class SettingsPainter {
    private SettingsPainter() { }

    static final class Info {
        String detect = "";
        Color detectColor = Color.GRAY;
        String account = "";
        Color accountColor = Color.GRAY;
        long latency;
        String redirect = "";
        Object art;
        boolean caretOn;
    }

    private static final Color TXT = new Color(240, 240, 240);
    private static final Color SUB = new Color(160, 160, 166);
    private static final Color DIM = new Color(110, 110, 118);
    private static final Color LINE = new Color(255, 255, 255, 16);

    private static BufferedImage canvas;

    static int[] render(int pw, int ph, SettingsUi ui, List<SettingsUi.W> ws, Config cfg, Info info, double t) {
        if (canvas == null || canvas.getWidth() != pw || canvas.getHeight() != ph)
            canvas = new BufferedImage(pw, ph, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        try {
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, pw, ph);
            g.setComposite(AlphaComposite.SrcOver);
            HudPainter.hints(g);
            g.scale(pw / CW, ph / CH);
            paint(g, ui, ws, cfg, info, t, pw / CW);
        } finally {
            g.dispose();
        }
        return canvas.getRGB(0, 0, pw, ph, null, 0, pw);
    }

    private static void paint(Graphics2D g, SettingsUi ui, List<SettingsUi.W> ws, Config cfg, Info info, double t, double px) {
        BufferedImage art = info.art instanceof BufferedImage ? (BufferedImage) info.art : null;
        Color accent = Theme.accent(cfg.theme, art);

        // ---- card
        RoundRectangle2D card = new RoundRectangle2D.Double(0, 0, CW, CH, 22, 22);
        g.setPaint(new GradientPaint(0, 0, new Color(24, 24, 30, 246), 0, (float) CH, new Color(12, 12, 16, 246)));
        g.fill(card);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(40, 10), 170f, new float[]{0f, 1f},
                new Color[]{alpha(accent, 40), alpha(accent, 0)}));
        g.fill(card);
        g.setColor(new Color(255, 255, 255, 22));
        g.setStroke(new BasicStroke((float) (1 / px)));
        g.draw(new RoundRectangle2D.Double(0.5 / px, 0.5 / px, CW - 1 / px, CH - 1 / px, 22, 22));

        // ---- header
        g.setPaint(new GradientPaint(14, 10, accent, 36, 32, accent.darker()));
        g.fill(new Ellipse2D.Double(14, 10, 22, 22));
        g.setColor(new Color(10, 10, 10));
        noteIcon(g, 25, 21, 0.9);
        g.setFont(HudPainter.uiFont(true, 12.5f));
        g.setColor(TXT);
        g.drawString("Spotify HUD", 44, 22.5f);
        g.setFont(HudPainter.uiFont(false, 7.6f));
        g.setColor(SUB);
        g.drawString("Einstellungen", 44, 31.5f);
        g.setColor(LINE);
        g.fill(new Rectangle2D.Double(12, 40, CW - 24, 1 / px));
        g.fill(new Rectangle2D.Double(100, 46, 1 / px, 158));

        // ---- widgets
        for (SettingsUi.W w : ws) {
            boolean hov = w.id.equals(ui.hover);
            switch (w.kind) {
                case CLOSE: close(g, w, hov); break;
                case TAB: tab(g, w, hov, accent); break;
                case THEME: themeCard(g, w, hov, accent, art, px); break;
                case ANIM: animCard(g, w, hov, accent, t); break;
                case TOGGLE: toggle(g, w, hov, accent); break;
                case SLIDER: slider(g, w, hov || w.id.equals(ui.dragging), accent); break;
                case BUTTON: button(g, w, hov, accent); break;
                case FIELD: field(g, w, hov, accent, info.caretOn, ui.selectAll); break;
                default: break;
            }
        }

        // ---- per-tab texts
        g.setFont(HudPainter.uiFont(true, 9.5f));
        g.setColor(TXT);
        double cx = 108;
        switch (ui.tab) {
            case 0:
                g.drawString("Thema", (float) cx, 57);
                small(g, "Wirkt sofort auf HUD und Lyrics.", cx, 186, SUB);
                small(g, "\"Cover\" = verschwommenes Albumcover als Hintergrund.", cx, 197, DIM);
                break;
            case 1:
                g.drawString("Animation", (float) cx, 57);
                break;
            case 2:
                small(g, "Automatische Latenz: " + info.latency + " ms  -  Sync verschiebt die Lyrics zusätzlich.", cx, 200, DIM);
                break;
            case 3:
                small(g, "Im Chat (T): ziehen = verschieben, Mausrad = Größe,", cx, 184, SUB);
                small(g, "Rechtsklick = Lyrics an/aus, Zahnrad = dieses Menü.", cx, 195, SUB);
                break;
            default:
                g.drawString("Spotify-Konto (optional)", (float) cx, 57);
                small(g, info.account, cx, 68, info.accountColor);
                small(g, "Nur für das Herz nötig - alles andere funktioniert automatisch.", cx, 148, SUB);
                small(g, "Redirect-URI im Dashboard: " + info.redirect, cx, 160, SUB);
                small(g, "1. Dashboard  2. App + URI + Web API  3. Client-ID  4. Anmelden", cx, 176, DIM);
        }

        // ---- footer
        g.setColor(LINE);
        g.fill(new Rectangle2D.Double(12, 206, CW - 24, 1 / px));
        g.setColor(info.detectColor);
        g.fill(new Ellipse2D.Double(16, 214, 5, 5));
        small(g, info.detect, 25, 219, SUB);
        g.setFont(HudPainter.uiFont(false, 7f));
        g.setColor(DIM);
        String ver = "v1.2";
        g.drawString(ver, (float) (CW - 16 - g.getFontMetrics().getStringBounds(ver, g).getWidth()), 219);
    }

    // ------------------------------------------------------------------ widgets
    private static void close(Graphics2D g, SettingsUi.W w, boolean hov) {
        if (hov) {
            g.setColor(new Color(255, 255, 255, 26));
            g.fill(new Ellipse2D.Double(w.x, w.y, w.w, w.h));
        }
        g.setColor(hov ? TXT : SUB);
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double cx = w.x + w.w / 2, cy = w.y + w.h / 2, r = 3.6;
        g.draw(new Line2D.Double(cx - r, cy - r, cx + r, cy + r));
        g.draw(new Line2D.Double(cx - r, cy + r, cx + r, cy - r));
    }

    private static void tab(Graphics2D g, SettingsUi.W w, boolean hov, Color accent) {
        if (w.selected || hov) {
            g.setColor(new Color(255, 255, 255, w.selected ? 22 : 12));
            g.fill(new RoundRectangle2D.Double(w.x, w.y, w.w, w.h, 10, 10));
        }
        if (w.selected) {
            g.setColor(accent);
            g.fill(new RoundRectangle2D.Double(w.x, w.y + 5, 2.4, w.h - 10, 2.4, 2.4));
        }
        g.setColor(w.selected ? TXT : (hov ? new Color(220, 220, 224) : SUB));
        tabIcon(g, w.label, w.x + 12, w.y + w.h / 2, w.selected ? accent : g.getColor());
        g.setColor(w.selected ? TXT : (hov ? new Color(220, 220, 224) : SUB));
        g.setFont(HudPainter.uiFont(w.selected, 8.6f));
        g.drawString(w.label, (float) (w.x + 22), (float) (w.y + w.h / 2 + 3));
    }

    private static void tabIcon(Graphics2D g, String tab, double cx, double cy, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (tab) {
            case "Design": { // palette
                g.draw(new Ellipse2D.Double(cx - 4, cy - 4, 8, 8));
                g.fill(new Ellipse2D.Double(cx - 2.2, cy - 2.4, 1.6, 1.6));
                g.fill(new Ellipse2D.Double(cx + 0.6, cy - 2.4, 1.6, 1.6));
                g.fill(new Ellipse2D.Double(cx - 2.6, cy + 0.4, 1.6, 1.6));
                break;
            }
            case "Animation": { // sparkle
                Path2D p = new Path2D.Double();
                double r = 4.2;
                p.moveTo(cx, cy - r); p.quadTo(cx, cy, cx + r, cy); p.quadTo(cx, cy, cx, cy + r);
                p.quadTo(cx, cy, cx - r, cy); p.quadTo(cx, cy, cx, cy - r);
                g.fill(p);
                break;
            }
            case "Lyrics": { // lines
                g.draw(new Line2D.Double(cx - 4, cy - 3, cx + 4, cy - 3));
                g.draw(new Line2D.Double(cx - 4, cy, cx + 2, cy));
                g.draw(new Line2D.Double(cx - 4, cy + 3, cx + 3.2, cy + 3));
                break;
            }
            case "HUD": { // window
                g.draw(new RoundRectangle2D.Double(cx - 4.5, cy - 3.5, 9, 7, 2.5, 2.5));
                g.fill(new Rectangle2D.Double(cx - 3, cy + 0.6, 6, 1.1));
                break;
            }
            default: { // person
                g.draw(new Ellipse2D.Double(cx - 1.9, cy - 4.2, 3.8, 3.8));
                g.draw(new java.awt.geom.Arc2D.Double(cx - 4, cy + 0.2, 8, 7, 0, 180, java.awt.geom.Arc2D.OPEN));
            }
        }
    }

    private static void themeCard(Graphics2D g, SettingsUi.W w, boolean hov, Color accent, BufferedImage art, double px) {
        double ph = w.h - 14;
        RoundRectangle2D box = new RoundRectangle2D.Double(w.x, w.y, w.w, ph, 9, 9);
        java.awt.Shape old = g.getClip();
        java.awt.geom.AffineTransform at = g.getTransform();
        g.clip(box);
        g.setColor(new Color(40, 44, 52));
        g.fill(box);
        g.translate(w.x, w.y);
        Theme.paint(g, w.ref, new RoundRectangle2D.Double(0, 0, w.w, ph, 9, 9), w.w, ph, art, px);
        // mini HUD sketch
        g.setColor(new Color(255, 255, 255, 60));
        g.fill(new RoundRectangle2D.Double(5, 6, 13, 13, 3, 3));
        g.setColor(new Color(255, 255, 255, 210));
        g.fill(new RoundRectangle2D.Double(22, 7.5, 24, 3, 3, 3));
        g.setColor(new Color(255, 255, 255, 110));
        g.fill(new RoundRectangle2D.Double(22, 13, 16, 2.4, 2.4, 2.4));
        g.setColor(new Color(255, 255, 255, 50));
        g.fill(new RoundRectangle2D.Double(5, 24, w.w - 10, 2, 2, 2));
        Color ta = Theme.accent(w.ref, art);
        g.setColor(ta);
        g.fill(new RoundRectangle2D.Double(5, 24, (w.w - 10) * 0.45, 2, 2, 2));
        g.setTransform(at);
        g.setClip(old);
        if (w.selected || hov) {
            g.setColor(w.selected ? accent : new Color(255, 255, 255, 90));
            g.setStroke(new BasicStroke(w.selected ? 1.6f : 1f));
            g.draw(new RoundRectangle2D.Double(w.x - 0.8, w.y - 0.8, w.w + 1.6, ph + 1.6, 10, 10));
        }
        if (w.selected) check(g, w.x + w.w - 7, w.y + 7, accent);
        g.setFont(HudPainter.uiFont(w.selected, 7.3f));
        g.setColor(w.selected ? TXT : SUB);
        centerText(g, w.label, w.x + w.w / 2, w.y + w.h - 3);
    }

    private static void animCard(Graphics2D g, SettingsUi.W w, boolean hov, Color accent, double t) {
        double ph = w.h - 13;
        Anim.icon(g, w.ref, w.x, w.y, ph, t, accent);
        // Anim.icon draws s*1.6 wide; our card is w.w wide, so draw frame around that area
        double iw = ph * 1.6;
        if (w.selected || hov) {
            g.setColor(w.selected ? accent : new Color(255, 255, 255, 90));
            g.setStroke(new BasicStroke(w.selected ? 1.6f : 1f));
            g.draw(new RoundRectangle2D.Double(w.x - 0.8, w.y - 0.8, iw + 1.6, ph + 1.6, 7, 7));
        }
        if (w.selected) check(g, w.x + iw - 6, w.y + 6, accent);
        g.setFont(HudPainter.uiFont(w.selected, 7.1f));
        g.setColor(w.selected ? TXT : SUB);
        centerText(g, w.label, w.x + iw / 2, w.y + w.h - 3);
    }

    private static void check(Graphics2D g, double cx, double cy, Color accent) {
        g.setColor(accent);
        g.fill(new Ellipse2D.Double(cx - 4.2, cy - 4.2, 8.4, 8.4));
        g.setColor(new Color(10, 10, 10));
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D p = new Path2D.Double();
        p.moveTo(cx - 2, cy + 0.1);
        p.lineTo(cx - 0.5, cy + 1.6);
        p.lineTo(cx + 2.2, cy - 1.4);
        g.draw(p);
    }

    private static void toggle(Graphics2D g, SettingsUi.W w, boolean hov, Color accent) {
        if (hov) {
            g.setColor(new Color(255, 255, 255, 10));
            g.fill(new RoundRectangle2D.Double(w.x - 4, w.y - 2, w.w + 8, w.h + 4, 10, 10));
        }
        g.setFont(HudPainter.uiFont(false, 8.8f));
        g.setColor(TXT);
        boolean hasSub = w.sub != null && !w.sub.isEmpty();
        g.drawString(w.label, (float) w.x, (float) (w.y + (hasSub ? 9.5 : w.h / 2 + 3)));
        if (hasSub) {
            g.setFont(HudPainter.uiFont(false, 7f));
            g.setColor(SUB);
            g.drawString(w.sub, (float) w.x, (float) (w.y + 19.5));
        }
        double sw = 22, sh = 12, sx = w.x + w.w - sw, sy = w.y + (w.h - sh) / 2;
        g.setColor(w.on ? accent : new Color(80, 80, 88));
        g.fill(new RoundRectangle2D.Double(sx, sy, sw, sh, sh, sh));
        double kx = w.on ? sx + sw - sh + 1.5 : sx + 1.5;
        g.setColor(new Color(0, 0, 0, 60));
        g.fill(new Ellipse2D.Double(kx, sy + 1.9, sh - 3, sh - 3));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(kx, sy + 1.5, sh - 3, sh - 3));
    }

    private static void slider(Graphics2D g, SettingsUi.W w, boolean hov, Color accent) {
        g.setFont(HudPainter.uiFont(false, 8.8f));
        g.setColor(TXT);
        g.drawString(w.label, (float) w.x, (float) (w.y + 7));
        g.setFont(HudPainter.uiFont(true, 8f));
        g.setColor(hov ? accent : SUB);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(w.sub, (float) (w.x + w.w - fm.getStringBounds(w.sub, g).getWidth()), (float) (w.y + 7));
        double ty = w.y + 14, th = 3.4;
        g.setColor(new Color(255, 255, 255, 36));
        g.fill(new RoundRectangle2D.Double(w.x, ty, w.w, th, th, th));
        double v = Math.max(0, Math.min(1, w.value));
        g.setColor(accent);
        g.fill(new RoundRectangle2D.Double(w.x, ty, Math.max(th, w.w * v), th, th, th));
        double kx = w.x + w.w * v, ky = ty + th / 2, r = hov ? 4.4 : 3.8;
        g.setColor(new Color(0, 0, 0, 80));
        g.fill(new Ellipse2D.Double(kx - r, ky - r + 0.5, 2 * r, 2 * r));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(kx - r, ky - r, 2 * r, 2 * r));
    }

    private static void button(Graphics2D g, SettingsUi.W w, boolean hov, Color accent) {
        RoundRectangle2D r = new RoundRectangle2D.Double(w.x, w.y, w.w, w.h, w.h, w.h);
        if (w.selected) {
            g.setColor(hov ? accent.brighter() : accent);
            g.fill(r);
        } else {
            g.setColor(new Color(255, 255, 255, hov ? 34 : 20));
            g.fill(r);
            g.setColor(new Color(255, 255, 255, 30));
            g.setStroke(new BasicStroke(0.7f));
            g.draw(r);
        }
        g.setFont(HudPainter.uiFont(true, 7.8f));
        g.setColor(w.selected ? new Color(12, 12, 12) : TXT);
        centerText(g, w.label, w.x + w.w / 2, w.y + w.h / 2 + 2.8);
    }

    private static void field(Graphics2D g, SettingsUi.W w, boolean hov, Color accent, boolean caret, boolean selectAll) {
        g.setFont(HudPainter.uiFont(false, 7f));
        g.setColor(SUB);
        g.drawString(w.label, (float) w.x, (float) (w.y - 3));
        RoundRectangle2D r = new RoundRectangle2D.Double(w.x, w.y, w.w, w.h, 8, 8);
        g.setColor(new Color(0, 0, 0, 90));
        g.fill(r);
        g.setColor(w.selected ? accent : new Color(255, 255, 255, hov ? 60 : 30));
        g.setStroke(new BasicStroke(w.selected ? 1.2f : 0.8f));
        g.draw(r);
        String txt = w.sub == null ? "" : w.sub;
        g.setFont(HudPainter.uiFont(false, 8.2f));
        FontMetrics fm = g.getFontMetrics();
        double maxW = w.w - 12;
        String shown = txt;
        while (shown.length() > 0 && fm.getStringBounds(shown, g).getWidth() > maxW) shown = shown.substring(1);
        double tw = fm.getStringBounds(shown, g).getWidth();
        if (w.selected && selectAll && !txt.isEmpty()) {
            g.setColor(alpha(accent, 90));
            g.fill(new Rectangle2D.Double(w.x + 6, w.y + 4, tw, w.h - 8));
        }
        if (txt.isEmpty() && !w.selected) {
            g.setColor(DIM);
            g.drawString(w.label.equals("Port") ? "8888" : "hier einfügen (Strg+V)", (float) (w.x + 6), (float) (w.y + w.h / 2 + 3));
        } else {
            g.setColor(TXT);
            g.drawString(shown, (float) (w.x + 6), (float) (w.y + w.h / 2 + 3));
        }
        if (w.selected && caret) {
            g.setColor(TXT);
            g.fill(new Rectangle2D.Double(w.x + 6.5 + tw, w.y + 5, 0.8, w.h - 10));
        }
    }

    // ------------------------------------------------------------------ helpers
    private static void small(Graphics2D g, String s, double x, double baseline, Color c) {
        if (s == null || s.isEmpty()) return;
        g.setFont(HudPainter.uiFont(false, 7.2f));
        g.setColor(c);
        FontMetrics fm = g.getFontMetrics();
        double max = CW - 14 - x;
        String t = s;
        if (fm.getStringBounds(t, g).getWidth() > max) {
            while (t.length() > 1 && fm.getStringBounds(t + "…", g).getWidth() > max) t = t.substring(0, t.length() - 1);
            t = t + "…";
        }
        g.drawString(t, (float) x, (float) baseline);
    }

    private static void centerText(Graphics2D g, String s, double cx, double baseline) {
        double w = g.getFontMetrics().getStringBounds(s, g).getWidth();
        g.drawString(s, (float) (cx - w / 2), (float) baseline);
    }

    private static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }

    private static void noteIcon(Graphics2D g, double cx, double cy, double k) {
        g.fill(new Ellipse2D.Double(cx - 5 * k, cy + 1.2 * k, 3.6 * k, 2.8 * k));
        g.fill(new Ellipse2D.Double(cx + 1.2 * k, cy + 0.2 * k, 3.6 * k, 2.8 * k));
        g.setStroke(new BasicStroke((float) (0.9 * k)));
        g.draw(new Line2D.Double(cx - 1.8 * k, cy + 2.4 * k, cx - 1.8 * k, cy - 4.6 * k));
        g.draw(new Line2D.Double(cx + 4.4 * k, cy + 1.4 * k, cx + 4.4 * k, cy - 5.6 * k));
        g.fill(new java.awt.geom.Rectangle2D.Double(cx - 2.25 * k, cy - 5.6 * k, 7.1 * k, 1.5 * k));
    }
}
