package dev.lego.hud;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.ui.UiSettings;
import dev.spotifyhud.TexSlot;
import dev.spotifyhud.Theme;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;

/** Renders all HUD modules (async painted textures) and handles dragging / resizing / snapping. */
public final class HudManager {
    private HudManager() { }

    private static final Map<String, TexSlot> SLOTS = new HashMap<>();
    private static final Map<String, BufferedImage> CANVAS = new HashMap<>();

    private static HudModule dragging;
    private static double offX, offY;
    /** Snap guides of the current drag in GUI units: {x or -1, y or -1}. */
    public static double snapX = -1, snapY = -1;

    public static List<HudModule> modules() {
        List<HudModule> out = new ArrayList<>();
        for (Module m : Modules.ALL) if (m instanceof HudModule) out.add((HudModule) m);
        return out;
    }

    /** x, y, w, h in GUI units. */
    public static int[] bounds(HudModule m, int sw, int sh) {
        int w = Math.max(4, (int) Math.round(m.w() * m.scale));
        int h = Math.max(4, (int) Math.round(m.h() * m.scale));
        int x = (int) Math.round(m.fx * Math.max(0, sw - w));
        int y = (int) Math.round(m.fy * Math.max(0, sh - h));
        return new int[]{x, y, w, h};
    }

    public static void render(class_332 ctx, int sw, int sh, boolean editing, int mx, int my) {
        class_310 mc = Mc.mc();
        int s = Math.max(1, mc.method_22683().method_4495());
        String theme = Modules.hudTheme;
        int accentArgb = Modules.hudAccentArgb();
        String style = UiSettings.hudStyle + "|" + UiSettings.hudOpacity + "|" + UiSettings.hudShadow + "|" + UiSettings.hudAccentLine + "|" + UiSettings.hudIcons;
        for (HudModule m : modules()) {
            if (!m.enabled) continue;
            try {
                if (!editing && !m.visible()) continue;
                int[] b = bounds(m, sw, sh);
                boolean hover = editing && (m == dragging || (mx >= b[0] && my >= b[1] && mx < b[0] + b[2] && my < b[1] + b[3]));
                int pw = b[2] * s, ph = b[3] * s;
                if (pw > 4096 || ph > 4096) continue;
                String key = m.key() + "|" + theme + "|" + accentArgb + "|" + style + "|" + editing + hover + "|" + m.w() + "x" + m.h();
                TexSlot slot = SLOTS.computeIfAbsent(m.id, k -> new TexSlot(class_2960.method_60655("legoclient", "hud_" + k)));
                final double dw = m.w(), dh = m.h();
                final boolean fHover = hover, fEdit = editing;
                slot.draw(ctx, mc, b[0], b[1], b[2], b[3], pw, ph, key, () -> paint(m, pw, ph, dw, dh, theme, accentArgb, fEdit, fHover));
                m.overlay(ctx, b[0], b[1], b[2] / dw);
            } catch (Throwable t) {
                dev.lego.LegoClient.LOG("HUD " + m.id + ": " + t);
            }
        }
    }

    /** Runs on the painter thread. */
    private static int[] paint(HudModule m, int pw, int ph, double dw, double dh, String theme, int accentArgb, boolean editing, boolean hover) {
        BufferedImage img = CANVAS.get(m.id);
        if (img == null || img.getWidth() != pw || img.getHeight() != ph) {
            img = new BufferedImage(pw, ph, BufferedImage.TYPE_INT_ARGB);
            CANVAS.put(m.id, img);
        }
        Graphics2D g = img.createGraphics();
        try {
            g.setComposite(java.awt.AlphaComposite.Clear);
            g.fillRect(0, 0, pw, ph);
            g.setComposite(java.awt.AlphaComposite.SrcOver);
            dev.lego.ui.Gx.hints(g);
            double px = pw / dw;
            g.scale(px, ph / dh);
            Color accent = new Color(accentArgb, true);
            double r = Math.min(6, dh * 0.38);
            RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, dw, dh, r * 2, r * 2);
            if (m.panel()) {
                switch (UiSettings.hudStyle) {
                    case 2: // Spotify theme panels
                        Theme.paint(g, theme, shape, dw, dh, null, px);
                        g.setColor(new Color(255, 255, 255, 14));
                        g.setStroke(new BasicStroke((float) (1 / px)));
                        g.draw(new RoundRectangle2D.Double(0.5 / px, 0.5 / px, dw - 1 / px, dh - 1 / px, r * 2, r * 2));
                        break;
                    case 1: { // minimal: a soft, see-through background
                        int a = (int) Math.round(150 * UiSettings.hudOpacity);
                        g.setColor(new Color(10, 11, 15, Math.max(40, Math.min(255, a))));
                        g.fill(shape);
                        break;
                    }
                    default: { // clean
                        int a = (int) Math.round(255 * UiSettings.hudOpacity);
                        g.setColor(new Color(10, 11, 15, Math.max(0, Math.min(255, a))));
                        g.fill(shape);
                        g.setColor(new Color(255, 255, 255, (int) (18 + 10 * UiSettings.hudOpacity)));
                        g.setStroke(new BasicStroke((float) (1 / px)));
                        g.draw(new RoundRectangle2D.Double(0.5 / px, 0.5 / px, dw - 1 / px, dh - 1 / px, r * 2, r * 2));
                    }
                }
                if (UiSettings.hudAccentLine && UiSettings.hudStyle != 1) {
                    g.setColor(accent);
                    g.fill(new RoundRectangle2D.Double(0, dh * 0.22, 1.6, dh * 0.56, 1.6, 1.6));
                }
            }
            m.paint(g, accent);
            if (editing) {
                g.setColor(hover ? accent : new Color(255, 255, 255, 110));
                g.setStroke(new BasicStroke((float) (hover ? 1.3 : 0.8), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1f, new float[]{3f, 2f}, 0f));
                g.draw(new RoundRectangle2D.Double(0.6, 0.6, dw - 1.2, dh - 1.2, r * 2, r * 2));
            }
        } finally {
            g.dispose();
        }
        return img.getRGB(0, 0, pw, ph, null, 0, pw);
    }

    // ------------------------------------------------------------------ dragging / resizing / snapping
    public static HudModule at(double mx, double my, int sw, int sh) {
        List<HudModule> list = modules();
        for (int i = list.size() - 1; i >= 0; i--) {
            HudModule m = list.get(i);
            if (!m.enabled) continue;
            int[] b = bounds(m, sw, sh);
            if (mx >= b[0] && my >= b[1] && mx < b[0] + b[2] && my < b[1] + b[3]) return m;
        }
        return null;
    }

    public static boolean mouseDown(double mx, double my, int button, int sw, int sh) {
        HudModule m = at(mx, my, sw, sh);
        if (m == null) return false;
        if (button == 1) { m.resetPosition(); Modules.scheduleSave(); return true; }
        if (button != 0) return true;
        int[] b = bounds(m, sw, sh);
        dragging = m;
        offX = mx - b[0];
        offY = my - b[1];
        return true;
    }

    /** @param snap snap to screen center / edges / other modules (HUD editor) */
    public static boolean mouseDrag(double mx, double my, int sw, int sh, boolean snap) {
        if (dragging == null) return false;
        int[] b = bounds(dragging, sw, sh);
        double x = mx - offX, y = my - offY;
        snapX = -1;
        snapY = -1;
        if (snap) {
            double th = 4;
            List<double[]> xs = new ArrayList<>(), ys = new ArrayList<>();
            xs.add(new double[]{0, 0}); xs.add(new double[]{sw / 2.0 - b[2] / 2.0, sw / 2.0}); xs.add(new double[]{sw - b[2], sw});
            ys.add(new double[]{0, 0}); ys.add(new double[]{sh / 2.0 - b[3] / 2.0, sh / 2.0}); ys.add(new double[]{sh - b[3], sh});
            for (HudModule o : modules()) {
                if (o == dragging || !o.enabled) continue;
                int[] ob = bounds(o, sw, sh);
                xs.add(new double[]{ob[0], ob[0]}); xs.add(new double[]{ob[0] + ob[2] - b[2], ob[0] + ob[2]});
                xs.add(new double[]{ob[0] + ob[2] + 2, ob[0] + ob[2] + 2}); xs.add(new double[]{ob[0] - b[2] - 2, ob[0] - 2});
                ys.add(new double[]{ob[1], ob[1]}); ys.add(new double[]{ob[1] + ob[3] - b[3], ob[1] + ob[3]});
                ys.add(new double[]{ob[1] + ob[3] + 2, ob[1] + ob[3] + 2}); ys.add(new double[]{ob[1] - b[3] - 2, ob[1] - 2});
            }
            double bestX = th, bestY = th;
            for (double[] c : xs) if (Math.abs(x - c[0]) < bestX) { bestX = Math.abs(x - c[0]); x = c[0]; snapX = c[1]; }
            for (double[] c : ys) if (Math.abs(y - c[0]) < bestY) { bestY = Math.abs(y - c[0]); y = c[0]; snapY = c[1]; }
        }
        int freeX = sw - b[2], freeY = sh - b[3];
        dragging.fx = freeX > 0 ? Math.max(0, Math.min(1, x / freeX)) : 0;
        dragging.fy = freeY > 0 ? Math.max(0, Math.min(1, y / freeY)) : 0;
        return true;
    }

    public static boolean mouseDrag(double mx, double my, int sw, int sh) { return mouseDrag(mx, my, sw, sh, false); }

    public static boolean mouseUp() {
        snapX = snapY = -1;
        if (dragging == null) return false;
        dragging = null;
        Modules.scheduleSave();
        return true;
    }

    public static boolean scroll(double mx, double my, double v, int sw, int sh) {
        HudModule m = at(mx, my, sw, sh);
        if (m == null || v == 0) return false;
        int[] b = bounds(m, sw, sh);
        double relX = (mx - b[0]) / b[2], relY = (my - b[1]) / b[3];
        m.scale = Math.round(Math.max(0.4, Math.min(3.0, m.scale + (v > 0 ? 0.05 : -0.05))) * 100) / 100.0;
        int[] n = bounds(m, sw, sh);
        int freeX = sw - n[2], freeY = sh - n[3];
        m.fx = freeX > 0 ? Math.max(0, Math.min(1, (mx - relX * n[2]) / freeX)) : 0;
        m.fy = freeY > 0 ? Math.max(0, Math.min(1, (my - relY * n[3]) / freeY)) : 0;
        Modules.scheduleSave();
        return true;
    }

    public static void resetAll() {
        for (HudModule m : modules()) m.resetPosition();
        Modules.scheduleSave();
    }

    public static HudModule dragged() { return dragging; }
    public static boolean dragging() { return dragging != null; }
}
