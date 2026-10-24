package dev.lego.hud;

import java.awt.Color;
import java.awt.Graphics2D;

import dev.lego.core.Category;
import dev.lego.core.Module;
import dev.lego.ui.UiSettings;
import net.minecraft.class_332;

/**
 * A draggable HUD element. Size is given in design units; the manager scales it and paints it pixel-exact
 * on the painter thread (key() snapshots the game state on the render thread, paint() only uses the snapshot).
 */
public abstract class HudModule extends Module {
    public double fx, fy;       // position as fraction of free screen space
    public double scale = 1.0;
    public final double defX, defY;

    protected HudModule(String id, String name, String description, double fx, double fy) {
        super(id, name, description, Category.HUD);
        this.fx = fx;
        this.fy = fy;
        this.defX = fx;
        this.defY = fy;
    }

    /** Width / height in design units. */
    public abstract double w();
    public abstract double h();

    /** Changes whenever the painted content changes (keep it cheap, it is built every frame). */
    public abstract String key();

    /** Paints the content on top of the panel (design units). */
    public abstract void paint(Graphics2D g, Color accent);

    /** Draws Minecraft items etc. on top (GUI units, k = GUI units per design unit). */
    public void overlay(class_332 ctx, int x, int y, double k) { }

    /** False = no panel background (e.g. keystrokes draw their own keys). */
    public boolean panel() { return true; }

    /** Visible right now? (e.g. hide the armor HUD when nothing is equipped) */
    public boolean visible() { return true; }

    public void resetPosition() { fx = defX; fy = defY; scale = 1.0; }

    // ------------------------------------------------------------------ "value unit" panels (Essential style)
    protected static final float VAL = 8.4f, UNIT = 7.4f, PH = 16;

    protected static boolean icons() { return UiSettings.hudIcons; }

    protected static double simpleWidth(String value, String unit) {
        double w = 7 + (icons() ? 13 : 0) + H.w(value, H.BOLD, VAL) + (unit == null || unit.isEmpty() ? 0 : 3.2 + H.w(unit, H.REG, UNIT)) + 7;
        return Math.ceil(w / 4.0) * 4.0;       // steps of 4 units -> the texture size rarely changes
    }

    protected static void simplePaint(Graphics2D g, String icon, String value, String unit, double w, Color accent) {
        double x = 7;
        if (icons()) {
            H.icon(g, icon, x + 4.5, PH / 2, 9, accent);
            x += 13;
        }
        H.text(g, value, x, H.base(0, PH, VAL), H.BOLD, VAL, H.TXT);
        x += H.w(value, H.BOLD, VAL) + 3.2;
        if (unit != null && !unit.isEmpty()) H.text(g, unit, x, H.base(0, PH, VAL), H.REG, UNIT, H.SUB);
    }
}
