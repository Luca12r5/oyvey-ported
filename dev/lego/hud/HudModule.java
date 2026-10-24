package dev.lego.hud;

import dev.lego.core.Category;
import dev.lego.core.Module;
import dev.lego.ui.UiSettings;
import java.awt.Color;
import java.awt.Graphics2D;
import net.minecraft.client.gui.GuiGraphics;

public abstract class HudModule extends Module {
   public double fx;
   public double fy;
   public double scale = 1.0;
   public final double defX;
   public final double defY;
   protected static final float VAL = 8.4F;
   protected static final float UNIT = 7.4F;
   protected static final float PH = 16.0F;

   protected HudModule(String id, String name, String description, double fx, double fy) {
      super(id, name, description, Category.HUD);
      this.fx = fx;
      this.fy = fy;
      this.defX = fx;
      this.defY = fy;
   }

   public abstract double w();

   public abstract double h();

   public abstract String key();

   public abstract void paint(Graphics2D var1, Color var2);

   public void overlay(GuiGraphics ctx, int x, int y, double k) {
   }

   public boolean panel() {
      return true;
   }

   public boolean visible() {
      return true;
   }

   public void resetPosition() {
      this.fx = this.defX;
      this.fy = this.defY;
      this.scale = 1.0;
   }

   protected static boolean icons() {
      return UiSettings.hudIcons;
   }

   protected static double simpleWidth(String value, String unit) {
      double w = 7 + (icons() ? 13 : 0) + H.w(value, 3, 8.4F) + (unit != null && !unit.isEmpty() ? 3.2 + H.w(unit, 1, 7.4F) : 0.0) + 7.0;
      return Math.ceil(w / 4.0) * 4.0;
   }

   protected static void simplePaint(Graphics2D g, String icon, String value, String unit, double w, Color accent) {
      double x = 7.0;
      if (icons()) {
         H.icon(g, icon, x + 4.5, 8.0, 9.0, accent);
         x += 13.0;
      }

      H.text(g, value, x, H.base(0.0, 16.0, 8.4F), 3, 8.4F, H.TXT);
      x += H.w(value, 3, 8.4F) + 3.2;
      if (unit != null && !unit.isEmpty()) {
         H.text(g, unit, x, H.base(0.0, 16.0, 8.4F), 1, 7.4F, H.SUB);
      }
   }
}
