package dev.lego.hud;

import dev.lego.ui.Fonts;
import dev.lego.ui.Icons;
import dev.lego.ui.UiSettings;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D.Double;

public final class H {
   public static final Color TXT = new Color(245, 246, 248);
   public static final Color SUB = new Color(166, 171, 181);
   public static final Color DIM = new Color(110, 115, 125);
   public static final int REG = 1;
   public static final int MED = 2;
   public static final int BOLD = 3;

   private H() {
   }

   public static double w(String var0, int var1, float var2) {
      return Fonts.width(var0, var1, var2);
   }

   public static void text(Graphics2D var0, String var1, double var2, double var4, int var6, float var7, Color var8) {
      if (var1 != null && !var1.isEmpty()) {
         var0.setFont(Fonts.get(var6, var7));
         if (UiSettings.hudStyle == 1 && UiSettings.hudShadow) {
            var0.setColor(new Color(0, 0, 0, Math.min(170, var8.getAlpha())));
            var0.drawString(var1, (float)(var2 + var7 * 0.07), (float)(var4 + var7 * 0.08));
         }

         var0.setColor(var8);
         var0.drawString(var1, (float)var2, (float)var4);
      }
   }

   public static void right(Graphics2D var0, String var1, double var2, double var4, int var6, float var7, Color var8) {
      text(var0, var1, var2 - w(var1, var6, var7), var4, var6, var7, var8);
   }

   public static void center(Graphics2D var0, String var1, double var2, double var4, int var6, float var7, Color var8) {
      text(var0, var1, var2 - w(var1, var6, var7) / 2.0, var4, var6, var7, var8);
   }

   public static double base(double var0, double var2, float var4) {
      return var0 + var2 / 2.0 + var4 * 0.35;
   }

   public static void icon(Graphics2D var0, String var1, double var2, double var4, double var6, Color var8) {
      Graphics2D var9 = (Graphics2D)var0.create();
      var9.translate(var2 - var6 / 2.0, var4 - var6 / 2.0);
      var9.scale(var6 / 24.0, var6 / 24.0);
      Icons.paint(var9, var1, 24, var8);
      var9.dispose();
   }

   public static void round(Graphics2D var0, double var1, double var3, double var5, double var7, double var9, Color var11) {
      var0.setColor(var11);
      var0.fill(new Double(var1, var3, var5, var7, var9 * 2.0, var9 * 2.0));
   }

   public static void bar(Graphics2D var0, double var1, double var3, double var5, double var7, double var9, Color var11) {
      round(var0, var1, var3, var5, var7, var7 / 2.0, new Color(255, 255, 255, 30));
      double var12 = Math.max(0.0, Math.min(1.0, var9));
      if (var12 > 0.0) {
         round(var0, var1, var3, Math.max(var7, var5 * var12), var7, var7 / 2.0, var11);
      }
   }

   public static Color alpha(Color var0, int var1) {
      return new Color(var0.getRed(), var0.getGreen(), var0.getBlue(), Math.max(0, Math.min(255, var1)));
   }

   public static Color mix(Color var0, Color var1, double var2) {
      var2 = Math.max(0.0, Math.min(1.0, var2));
      return new Color(
         (int)(var0.getRed() + (var1.getRed() - var0.getRed()) * var2),
         (int)(var0.getGreen() + (var1.getGreen() - var0.getGreen()) * var2),
         (int)(var0.getBlue() + (var1.getBlue() - var0.getBlue()) * var2),
         (int)(var0.getAlpha() + (var1.getAlpha() - var0.getAlpha()) * var2)
      );
   }

   public static String ellipsize(String var0, double var1, int var3, float var4) {
      return Fonts.ellipsize(var0, var3, var4, (float)var1);
   }
}
