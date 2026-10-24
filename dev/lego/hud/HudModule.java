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

   protected HudModule(String var1, String var2, String var3, double var4, double var6) {
      super(var1, var2, var3, Category.HUD);
      this.fx = var4;
      this.fy = var6;
      this.defX = var4;
      this.defY = var6;
   }

   public abstract double w();

   public abstract double h();

   public abstract String key();

   public abstract void paint(Graphics2D var1, Color var2);

   public void overlay(GuiGraphics var1, int var2, int var3, double var4) {
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

   protected static double simpleWidth(String var0, String var1) {
      double var2 = 7 + (icons() ? 13 : 0) + H.w(var0, 3, 8.4F) + (var1 != null && !var1.isEmpty() ? 3.2 + H.w(var1, 1, 7.4F) : 0.0) + 7.0;
      return Math.ceil(var2 / 4.0) * 4.0;
   }

   protected static void simplePaint(Graphics2D var0, String var1, String var2, String var3, double var4, Color var6) {
      double var7 = 7.0;
      if (icons()) {
         H.icon(var0, var1, var7 + 4.5, 8.0, 9.0, var6);
         var7 += 13.0;
      }

      H.text(var0, var2, var7, H.base(0.0, 16.0, 8.4F), 3, 8.4F, H.TXT);
      var7 += H.w(var2, 3, 8.4F) + 3.2;
      if (var3 != null && !var3.isEmpty()) {
         H.text(var0, var3, var7, H.base(0.0, 16.0, 8.4F), 1, 7.4F, H.SUB);
      }
   }
}
