package dev.lego.hud;

import dev.lego.core.Mc;
import dev.lego.core.Setting;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.util.Locale;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

/** Speedometer HUD: real speed (km/h, m/s or mph), acceleration and braking. */
final class Speedometer extends HudModule {
   final Setting.Mode unit = this.add(new Setting.Mode("Einheit", 0, "km/h", "m/s (Blöcke/s)", "mph"));
   final Setting.Mode measure = this.add(new Setting.Mode("Messung", 0, "Horizontal", "3D (inkl. Fallen/Fliegen)"));
   final Setting.Mode design = this.add(new Setting.Mode("Design", 0, "Rund-Tacho", "Balken", "Digital"));
   final Setting.Num max = this.add(new Setting.Num("Skala bis", 20.0, 300.0, 10.0, 60.0, ""));
   final Setting.Bool accelBar = this.add(new Setting.Bool("Beschleunigung/Bremsen", true));
   final Setting.Bool peakShow = this.add(new Setting.Bool("Höchstwert", false));
   final Setting.Color color = this.add(new Setting.Color("Farbe", 4));
   private final SpeedMath math = new SpeedMath(0.35);

   Speedometer() {
      super("speedometer", "Tacho", "Echte Geschwindigkeit aus deiner Bewegung oder deinem Fahrzeug (km/h, m/s, mph) mit Beschleunigung und Bremsanzeige", 0.88, 0.8);
      this.icon("speed");
      this.fresh();
   }

   @Override
   public void tick() {
      LocalPlayer p = Mc.player();
      if (p == null) {
         this.math.reset();
         return;
      }
      // Riding a boat/horse/minecart: measure the vehicle the player sits on.
      Entity e = p.getRootVehicle();
      this.math.sample(e.getX(), e.getY(), e.getZ(), System.nanoTime(), this.measure.index == 0);
   }

   private double shown() {
      return SpeedMath.convert(this.math.metresPerSecond(), this.unit.index);
   }

   private Color tint() {
      int i = Math.max(0, Math.min(Setting.Color.PALETTE.length - 1, this.color.index));
      if (i == Setting.Color.PALETTE.length - 1) {
         double t = System.currentTimeMillis() / 2600.0;
         return Color.getHSBColor((float)(t - Math.floor(t)), 0.7F, 1.0F);
      }
      return new Color(Setting.Color.PALETTE[i], false);
   }

   @Override
   public double w() {
      return switch (this.design.index) {
         case 0 -> 96.0;
         case 1 -> 132.0;
         default -> simpleWidth(String.format(Locale.ROOT, "%.0f", this.shown()), SpeedMath.unitName(this.unit.index)) + (this.accelBar.get() ? 10.0 : 0.0);
      };
   }

   @Override
   public double h() {
      return switch (this.design.index) {
         case 0 -> 72.0;
         case 1 -> 30.0;
         default -> 16.0;
      };
   }

   @Override
   public String key() {
      return String.format(Locale.ROOT, "%d|%.1f|%.1f|%b|%d|%.0f|%d", this.design.index, this.shown(), this.math.accel(), this.math.braking(), this.unit.index, this.max.get(), this.color.index == Setting.Color.PALETTE.length - 1 ? System.currentTimeMillis() / 100 : this.color.index);
   }

   @Override
   public void paint(Graphics2D g, Color accent) {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      double v = this.shown();
      String num = String.format(Locale.ROOT, v < 10 && this.unit.index == 1 ? "%.1f" : "%.0f", v);
      String u = SpeedMath.unitName(this.unit.index);
      Color c = this.tint();
      double frac = Math.max(0.0, Math.min(1.0, v / this.max.get()));
      Color accelColor = this.math.braking() ? new Color(239, 68, 68) : this.math.accel() > 0.3 ? new Color(52, 211, 153) : H.DIM;
      switch (this.design.index) {
         case 0 -> {
            double cx = 48.0, cy = 44.0, r = 32.0;
            g.setStroke(new BasicStroke(5.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(H.alpha(H.DIM, 120));
            g.draw(new Arc2D.Double(cx - r, cy - r, r * 2, r * 2, -30, 240, Arc2D.OPEN));
            g.setColor(c);
            g.draw(new Arc2D.Double(cx - r, cy - r, r * 2, r * 2, 210, -240 * frac, Arc2D.OPEN));
            g.setStroke(new BasicStroke(1.2F));
            for (int i = 0; i <= 8; i++) {
               double a = Math.toRadians(210 - 240.0 * i / 8);
               g.setColor(H.SUB);
               g.draw(new Line2D.Double(cx + Math.cos(a) * (r - 9), cy - Math.sin(a) * (r - 9), cx + Math.cos(a) * (r - 5), cy - Math.sin(a) * (r - 5)));
            }
            double na = Math.toRadians(210 - 240 * frac);
            g.setStroke(new BasicStroke(2.2F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(H.TXT);
            g.draw(new Line2D.Double(cx, cy, cx + Math.cos(na) * (r - 11), cy - Math.sin(na) * (r - 11)));
            H.center(g, num, cx, cy + 20.0, 3, 11.0F, H.TXT);
            H.center(g, u, cx, cy + 28.0, 1, 6.5F, H.SUB);
            if (this.accelBar.get()) H.round(g, cx - 3, cy - 3, 6, 6, 6, accelColor);
            if (this.peakShow.get()) H.text(g, String.format(Locale.ROOT, "max %.0f", SpeedMath.convert(this.math.peak(), this.unit.index)), 4, 10, 1, 6.0F, H.SUB);
         }
         case 1 -> {
            H.text(g, num, 7, H.base(0, 18, 9.5F), 3, 9.5F, H.TXT);
            H.text(g, u, 7 + H.w(num, 3, 9.5F) + 3, H.base(0, 18, 9.5F), 1, 7.0F, H.SUB);
            H.bar(g, 7, 20, this.w() - 14, 4, 2, H.alpha(H.DIM, 120));
            H.bar(g, 7, 20, (this.w() - 14) * frac, 4, 2, c);
            if (this.accelBar.get()) {
               double a = Math.max(-1.0, Math.min(1.0, this.math.accel() / 6.0));
               double mid = this.w() - 30;
               H.bar(g, mid, 7, 20, 3, 1.5, H.alpha(H.DIM, 120));
               H.bar(g, a >= 0 ? mid + 10 : mid + 10 + 10 * a, 7, 10 * Math.abs(a), 3, 1.5, accelColor);
            }
         }
         default -> {
            simplePaint(g, "speed", num, u, this.w(), accent);
            if (this.accelBar.get()) H.round(g, this.w() - 11, 5, 6, 6, 6, accelColor);
         }
      }
   }
}
