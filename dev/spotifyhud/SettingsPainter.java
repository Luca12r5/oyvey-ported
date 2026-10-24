package dev.spotifyhud;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D.Double;
import java.awt.image.BufferedImage;
import java.util.List;

final class SettingsPainter {
   private static final Color TXT = new Color(240, 240, 240);
   private static final Color SUB = new Color(160, 160, 166);
   private static final Color DIM = new Color(110, 110, 118);
   private static final Color LINE = new Color(255, 255, 255, 16);
   private static BufferedImage canvas;

   private SettingsPainter() {
   }

   static int[] render(int var0, int var1, SettingsUi var2, List<SettingsUi.W> var3, Config var4, SettingsPainter.Info var5, double var6) {
      if (canvas == null || canvas.getWidth() != var0 || canvas.getHeight() != var1) {
         canvas = new BufferedImage(var0, var1, 2);
      }

      Graphics2D var8 = canvas.createGraphics();

      try {
         var8.setComposite(AlphaComposite.Clear);
         var8.fillRect(0, 0, var0, var1);
         var8.setComposite(AlphaComposite.SrcOver);
         HudPainter.hints(var8);
         var8.scale(var0 / 360.0, var1 / 230.0);
         paint(var8, var2, var3, var4, var5, var6, var0 / 360.0);
      } finally {
         var8.dispose();
      }

      return canvas.getRGB(0, 0, var0, var1, null, 0, var0);
   }

   private static void paint(Graphics2D var0, SettingsUi var1, List<SettingsUi.W> var2, Config var3, SettingsPainter.Info var4, double var5, double var7) {
      BufferedImage var9 = var4.art instanceof BufferedImage ? (BufferedImage)var4.art : null;
      Color var10 = Theme.accent(var3.theme, var9);
      Double var11 = new Double(0.0, 0.0, 360.0, 230.0, 22.0, 22.0);
      var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(24, 24, 30, 246), 0.0F, 230.0F, new Color(12, 12, 16, 246)));
      var0.fill(var11);
      var0.setPaint(
         new RadialGradientPaint(new java.awt.geom.Point2D.Double(40.0, 10.0), 170.0F, new float[]{0.0F, 1.0F}, new Color[]{alpha(var10, 40), alpha(var10, 0)})
      );
      var0.fill(var11);
      var0.setColor(new Color(255, 255, 255, 22));
      var0.setStroke(new BasicStroke((float)(1.0 / var7)));
      var0.draw(new Double(0.5 / var7, 0.5 / var7, 360.0 - 1.0 / var7, 230.0 - 1.0 / var7, 22.0, 22.0));
      var0.setPaint(new GradientPaint(14.0F, 10.0F, var10, 36.0F, 32.0F, var10.darker()));
      var0.fill(new java.awt.geom.Ellipse2D.Double(14.0, 10.0, 22.0, 22.0));
      var0.setColor(new Color(10, 10, 10));
      noteIcon(var0, 25.0, 21.0, 0.9);
      var0.setFont(HudPainter.uiFont(true, 12.5F));
      var0.setColor(TXT);
      var0.drawString("Spotify HUD", 44.0F, 22.5F);
      var0.setFont(HudPainter.uiFont(false, 7.6F));
      var0.setColor(SUB);
      var0.drawString("Einstellungen", 44.0F, 31.5F);
      var0.setColor(LINE);
      var0.fill(new java.awt.geom.Rectangle2D.Double(12.0, 40.0, 336.0, 1.0 / var7));
      var0.fill(new java.awt.geom.Rectangle2D.Double(100.0, 46.0, 1.0 / var7, 158.0));

      for (SettingsUi.W var13 : var2) {
         boolean var14 = var13.id.equals(var1.hover);
         switch (var13.kind) {
            case CLOSE:
               close(var0, var13, var14);
               break;
            case TAB:
               tab(var0, var13, var14, var10);
               break;
            case THEME:
               themeCard(var0, var13, var14, var10, var9, var7);
               break;
            case ANIM:
               animCard(var0, var13, var14, var10, var5);
               break;
            case TOGGLE:
               toggle(var0, var13, var14, var10);
               break;
            case SLIDER:
               slider(var0, var13, var14 || var13.id.equals(var1.dragging), var10);
               break;
            case BUTTON:
               button(var0, var13, var14, var10);
               break;
            case FIELD:
               field(var0, var13, var14, var10, var4.caretOn, var1.selectAll);
         }
      }

      var0.setFont(HudPainter.uiFont(true, 9.5F));
      var0.setColor(TXT);
      double var15 = 108.0;
      switch (var1.tab) {
         case 0:
            var0.drawString("Thema", (float)var15, 57.0F);
            small(var0, "Wirkt sofort auf HUD und Lyrics.", var15, 186.0, SUB);
            small(var0, "\"Cover\" = verschwommenes Albumcover als Hintergrund.", var15, 197.0, DIM);
            break;
         case 1:
            var0.drawString("Animation", (float)var15, 57.0F);
            break;
         case 2:
            small(var0, "Automatische Latenz: " + var4.latency + " ms  -  Sync verschiebt die Lyrics zusätzlich.", var15, 200.0, DIM);
            break;
         case 3:
            small(var0, "Im Chat (T): ziehen = verschieben, Mausrad = Größe,", var15, 184.0, SUB);
            small(var0, "Rechtsklick = Lyrics an/aus, Zahnrad = dieses Menü.", var15, 195.0, SUB);
            break;
         default:
            var0.drawString("Spotify-Konto (optional)", (float)var15, 57.0F);
            small(var0, var4.account, var15, 68.0, var4.accountColor);
            small(var0, "Nur für das Herz nötig - alles andere funktioniert automatisch.", var15, 148.0, SUB);
            small(var0, "Redirect-URI im Dashboard: " + var4.redirect, var15, 160.0, SUB);
            small(var0, "1. Dashboard  2. App + URI + Web API  3. Client-ID  4. Anmelden", var15, 176.0, DIM);
      }

      var0.setColor(LINE);
      var0.fill(new java.awt.geom.Rectangle2D.Double(12.0, 206.0, 336.0, 1.0 / var7));
      var0.setColor(var4.detectColor);
      var0.fill(new java.awt.geom.Ellipse2D.Double(16.0, 214.0, 5.0, 5.0));
      small(var0, var4.detect, 25.0, 219.0, SUB);
      var0.setFont(HudPainter.uiFont(false, 7.0F));
      var0.setColor(DIM);
      String var16 = "v1.2";
      var0.drawString(var16, (float)(344.0 - var0.getFontMetrics().getStringBounds(var16, var0).getWidth()), 219.0F);
   }

   private static void close(Graphics2D var0, SettingsUi.W var1, boolean var2) {
      if (var2) {
         var0.setColor(new Color(255, 255, 255, 26));
         var0.fill(new java.awt.geom.Ellipse2D.Double(var1.x, var1.y, var1.w, var1.h));
      }

      var0.setColor(var2 ? TXT : SUB);
      var0.setStroke(new BasicStroke(1.3F, 1, 1));
      double var3 = var1.x + var1.w / 2.0;
      double var5 = var1.y + var1.h / 2.0;
      double var7 = 3.6;
      var0.draw(new java.awt.geom.Line2D.Double(var3 - var7, var5 - var7, var3 + var7, var5 + var7));
      var0.draw(new java.awt.geom.Line2D.Double(var3 - var7, var5 + var7, var3 + var7, var5 - var7));
   }

   private static void tab(Graphics2D var0, SettingsUi.W var1, boolean var2, Color var3) {
      if (var1.selected || var2) {
         var0.setColor(new Color(255, 255, 255, var1.selected ? 22 : 12));
         var0.fill(new Double(var1.x, var1.y, var1.w, var1.h, 10.0, 10.0));
      }

      if (var1.selected) {
         var0.setColor(var3);
         var0.fill(new Double(var1.x, var1.y + 5.0, 2.4, var1.h - 10.0, 2.4, 2.4));
      }

      var0.setColor(var1.selected ? TXT : (var2 ? new Color(220, 220, 224) : SUB));
      tabIcon(var0, var1.label, var1.x + 12.0, var1.y + var1.h / 2.0, var1.selected ? var3 : var0.getColor());
      var0.setColor(var1.selected ? TXT : (var2 ? new Color(220, 220, 224) : SUB));
      var0.setFont(HudPainter.uiFont(var1.selected, 8.6F));
      var0.drawString(var1.label, (float)(var1.x + 22.0), (float)(var1.y + var1.h / 2.0 + 3.0));
   }

   private static void tabIcon(Graphics2D var0, String var1, double var2, double var4, Color var6) {
      var0.setColor(var6);
      var0.setStroke(new BasicStroke(1.1F, 1, 1));
      switch (var1) {
         case "Design":
            var0.draw(new java.awt.geom.Ellipse2D.Double(var2 - 4.0, var4 - 4.0, 8.0, 8.0));
            var0.fill(new java.awt.geom.Ellipse2D.Double(var2 - 2.2, var4 - 2.4, 1.6, 1.6));
            var0.fill(new java.awt.geom.Ellipse2D.Double(var2 + 0.6, var4 - 2.4, 1.6, 1.6));
            var0.fill(new java.awt.geom.Ellipse2D.Double(var2 - 2.6, var4 + 0.4, 1.6, 1.6));
            break;
         case "Animation":
            java.awt.geom.Path2D.Double var9 = new java.awt.geom.Path2D.Double();
            double var10 = 4.2;
            var9.moveTo(var2, var4 - var10);
            var9.quadTo(var2, var4, var2 + var10, var4);
            var9.quadTo(var2, var4, var2, var4 + var10);
            var9.quadTo(var2, var4, var2 - var10, var4);
            var9.quadTo(var2, var4, var2, var4 - var10);
            var0.fill(var9);
            break;
         case "Lyrics":
            var0.draw(new java.awt.geom.Line2D.Double(var2 - 4.0, var4 - 3.0, var2 + 4.0, var4 - 3.0));
            var0.draw(new java.awt.geom.Line2D.Double(var2 - 4.0, var4, var2 + 2.0, var4));
            var0.draw(new java.awt.geom.Line2D.Double(var2 - 4.0, var4 + 3.0, var2 + 3.2, var4 + 3.0));
            break;
         case "HUD":
            var0.draw(new Double(var2 - 4.5, var4 - 3.5, 9.0, 7.0, 2.5, 2.5));
            var0.fill(new java.awt.geom.Rectangle2D.Double(var2 - 3.0, var4 + 0.6, 6.0, 1.1));
            break;
         default:
            var0.draw(new java.awt.geom.Ellipse2D.Double(var2 - 1.9, var4 - 4.2, 3.8, 3.8));
            var0.draw(new java.awt.geom.Arc2D.Double(var2 - 4.0, var4 + 0.2, 8.0, 7.0, 0.0, 180.0, 0));
      }
   }

   private static void themeCard(Graphics2D var0, SettingsUi.W var1, boolean var2, Color var3, BufferedImage var4, double var5) {
      double var7 = var1.h - 14.0;
      Double var9 = new Double(var1.x, var1.y, var1.w, var7, 9.0, 9.0);
      Shape var10 = var0.getClip();
      AffineTransform var11 = var0.getTransform();
      var0.clip(var9);
      var0.setColor(new Color(40, 44, 52));
      var0.fill(var9);
      var0.translate(var1.x, var1.y);
      Theme.paint(var0, var1.ref, new Double(0.0, 0.0, var1.w, var7, 9.0, 9.0), var1.w, var7, var4, var5);
      var0.setColor(new Color(255, 255, 255, 60));
      var0.fill(new Double(5.0, 6.0, 13.0, 13.0, 3.0, 3.0));
      var0.setColor(new Color(255, 255, 255, 210));
      var0.fill(new Double(22.0, 7.5, 24.0, 3.0, 3.0, 3.0));
      var0.setColor(new Color(255, 255, 255, 110));
      var0.fill(new Double(22.0, 13.0, 16.0, 2.4, 2.4, 2.4));
      var0.setColor(new Color(255, 255, 255, 50));
      var0.fill(new Double(5.0, 24.0, var1.w - 10.0, 2.0, 2.0, 2.0));
      Color var12 = Theme.accent(var1.ref, var4);
      var0.setColor(var12);
      var0.fill(new Double(5.0, 24.0, (var1.w - 10.0) * 0.45, 2.0, 2.0, 2.0));
      var0.setTransform(var11);
      var0.setClip(var10);
      if (var1.selected || var2) {
         var0.setColor(var1.selected ? var3 : new Color(255, 255, 255, 90));
         var0.setStroke(new BasicStroke(var1.selected ? 1.6F : 1.0F));
         var0.draw(new Double(var1.x - 0.8, var1.y - 0.8, var1.w + 1.6, var7 + 1.6, 10.0, 10.0));
      }

      if (var1.selected) {
         check(var0, var1.x + var1.w - 7.0, var1.y + 7.0, var3);
      }

      var0.setFont(HudPainter.uiFont(var1.selected, 7.3F));
      var0.setColor(var1.selected ? TXT : SUB);
      centerText(var0, var1.label, var1.x + var1.w / 2.0, var1.y + var1.h - 3.0);
   }

   private static void animCard(Graphics2D var0, SettingsUi.W var1, boolean var2, Color var3, double var4) {
      double var6 = var1.h - 13.0;
      Anim.icon(var0, var1.ref, var1.x, var1.y, var6, var4, var3);
      double var8 = var6 * 1.6;
      if (var1.selected || var2) {
         var0.setColor(var1.selected ? var3 : new Color(255, 255, 255, 90));
         var0.setStroke(new BasicStroke(var1.selected ? 1.6F : 1.0F));
         var0.draw(new Double(var1.x - 0.8, var1.y - 0.8, var8 + 1.6, var6 + 1.6, 7.0, 7.0));
      }

      if (var1.selected) {
         check(var0, var1.x + var8 - 6.0, var1.y + 6.0, var3);
      }

      var0.setFont(HudPainter.uiFont(var1.selected, 7.1F));
      var0.setColor(var1.selected ? TXT : SUB);
      centerText(var0, var1.label, var1.x + var8 / 2.0, var1.y + var1.h - 3.0);
   }

   private static void check(Graphics2D var0, double var1, double var3, Color var5) {
      var0.setColor(var5);
      var0.fill(new java.awt.geom.Ellipse2D.Double(var1 - 4.2, var3 - 4.2, 8.4, 8.4));
      var0.setColor(new Color(10, 10, 10));
      var0.setStroke(new BasicStroke(1.2F, 1, 1));
      java.awt.geom.Path2D.Double var6 = new java.awt.geom.Path2D.Double();
      var6.moveTo(var1 - 2.0, var3 + 0.1);
      var6.lineTo(var1 - 0.5, var3 + 1.6);
      var6.lineTo(var1 + 2.2, var3 - 1.4);
      var0.draw(var6);
   }

   private static void toggle(Graphics2D var0, SettingsUi.W var1, boolean var2, Color var3) {
      if (var2) {
         var0.setColor(new Color(255, 255, 255, 10));
         var0.fill(new Double(var1.x - 4.0, var1.y - 2.0, var1.w + 8.0, var1.h + 4.0, 10.0, 10.0));
      }

      var0.setFont(HudPainter.uiFont(false, 8.8F));
      var0.setColor(TXT);
      boolean var4 = var1.sub != null && !var1.sub.isEmpty();
      var0.drawString(var1.label, (float)var1.x, (float)(var1.y + (var4 ? 9.5 : var1.h / 2.0 + 3.0)));
      if (var4) {
         var0.setFont(HudPainter.uiFont(false, 7.0F));
         var0.setColor(SUB);
         var0.drawString(var1.sub, (float)var1.x, (float)(var1.y + 19.5));
      }

      double var5 = 22.0;
      double var7 = 12.0;
      double var9 = var1.x + var1.w - var5;
      double var11 = var1.y + (var1.h - var7) / 2.0;
      var0.setColor(var1.on ? var3 : new Color(80, 80, 88));
      var0.fill(new Double(var9, var11, var5, var7, var7, var7));
      double var13 = var1.on ? var9 + var5 - var7 + 1.5 : var9 + 1.5;
      var0.setColor(new Color(0, 0, 0, 60));
      var0.fill(new java.awt.geom.Ellipse2D.Double(var13, var11 + 1.9, var7 - 3.0, var7 - 3.0));
      var0.setColor(Color.WHITE);
      var0.fill(new java.awt.geom.Ellipse2D.Double(var13, var11 + 1.5, var7 - 3.0, var7 - 3.0));
   }

   private static void slider(Graphics2D var0, SettingsUi.W var1, boolean var2, Color var3) {
      var0.setFont(HudPainter.uiFont(false, 8.8F));
      var0.setColor(TXT);
      var0.drawString(var1.label, (float)var1.x, (float)(var1.y + 7.0));
      var0.setFont(HudPainter.uiFont(true, 8.0F));
      var0.setColor(var2 ? var3 : SUB);
      FontMetrics var4 = var0.getFontMetrics();
      var0.drawString(var1.sub, (float)(var1.x + var1.w - var4.getStringBounds(var1.sub, var0).getWidth()), (float)(var1.y + 7.0));
      double var5 = var1.y + 14.0;
      double var7 = 3.4;
      var0.setColor(new Color(255, 255, 255, 36));
      var0.fill(new Double(var1.x, var5, var1.w, var7, var7, var7));
      double var9 = Math.max(0.0, Math.min(1.0, var1.value));
      var0.setColor(var3);
      var0.fill(new Double(var1.x, var5, Math.max(var7, var1.w * var9), var7, var7, var7));
      double var11 = var1.x + var1.w * var9;
      double var13 = var5 + var7 / 2.0;
      double var15 = var2 ? 4.4 : 3.8;
      var0.setColor(new Color(0, 0, 0, 80));
      var0.fill(new java.awt.geom.Ellipse2D.Double(var11 - var15, var13 - var15 + 0.5, 2.0 * var15, 2.0 * var15));
      var0.setColor(Color.WHITE);
      var0.fill(new java.awt.geom.Ellipse2D.Double(var11 - var15, var13 - var15, 2.0 * var15, 2.0 * var15));
   }

   private static void button(Graphics2D var0, SettingsUi.W var1, boolean var2, Color var3) {
      Double var4 = new Double(var1.x, var1.y, var1.w, var1.h, var1.h, var1.h);
      if (var1.selected) {
         var0.setColor(var2 ? var3.brighter() : var3);
         var0.fill(var4);
      } else {
         var0.setColor(new Color(255, 255, 255, var2 ? 34 : 20));
         var0.fill(var4);
         var0.setColor(new Color(255, 255, 255, 30));
         var0.setStroke(new BasicStroke(0.7F));
         var0.draw(var4);
      }

      var0.setFont(HudPainter.uiFont(true, 7.8F));
      var0.setColor(var1.selected ? new Color(12, 12, 12) : TXT);
      centerText(var0, var1.label, var1.x + var1.w / 2.0, var1.y + var1.h / 2.0 + 2.8);
   }

   private static void field(Graphics2D var0, SettingsUi.W var1, boolean var2, Color var3, boolean var4, boolean var5) {
      var0.setFont(HudPainter.uiFont(false, 7.0F));
      var0.setColor(SUB);
      var0.drawString(var1.label, (float)var1.x, (float)(var1.y - 3.0));
      Double var6 = new Double(var1.x, var1.y, var1.w, var1.h, 8.0, 8.0);
      var0.setColor(new Color(0, 0, 0, 90));
      var0.fill(var6);
      var0.setColor(var1.selected ? var3 : new Color(255, 255, 255, var2 ? 60 : 30));
      var0.setStroke(new BasicStroke(var1.selected ? 1.2F : 0.8F));
      var0.draw(var6);
      String var7 = var1.sub == null ? "" : var1.sub;
      var0.setFont(HudPainter.uiFont(false, 8.2F));
      FontMetrics var8 = var0.getFontMetrics();
      double var9 = var1.w - 12.0;
      String var11 = var7;

      while (var11.length() > 0 && var8.getStringBounds(var11, var0).getWidth() > var9) {
         var11 = var11.substring(1);
      }

      double var12 = var8.getStringBounds(var11, var0).getWidth();
      if (var1.selected && var5 && !var7.isEmpty()) {
         var0.setColor(alpha(var3, 90));
         var0.fill(new java.awt.geom.Rectangle2D.Double(var1.x + 6.0, var1.y + 4.0, var12, var1.h - 8.0));
      }

      if (var7.isEmpty() && !var1.selected) {
         var0.setColor(DIM);
         var0.drawString(var1.label.equals("Port") ? "8888" : "hier einfügen (Strg+V)", (float)(var1.x + 6.0), (float)(var1.y + var1.h / 2.0 + 3.0));
      } else {
         var0.setColor(TXT);
         var0.drawString(var11, (float)(var1.x + 6.0), (float)(var1.y + var1.h / 2.0 + 3.0));
      }

      if (var1.selected && var4) {
         var0.setColor(TXT);
         var0.fill(new java.awt.geom.Rectangle2D.Double(var1.x + 6.5 + var12, var1.y + 5.0, 0.8, var1.h - 10.0));
      }
   }

   private static void small(Graphics2D var0, String var1, double var2, double var4, Color var6) {
      if (var1 != null && !var1.isEmpty()) {
         var0.setFont(HudPainter.uiFont(false, 7.2F));
         var0.setColor(var6);
         FontMetrics var7 = var0.getFontMetrics();
         double var8 = 346.0 - var2;
         String var10 = var1;
         if (var7.getStringBounds(var1, var0).getWidth() > var8) {
            while (var10.length() > 1 && var7.getStringBounds(var10 + "…", var0).getWidth() > var8) {
               var10 = var10.substring(0, var10.length() - 1);
            }

            var10 = var10 + "…";
         }

         var0.drawString(var10, (float)var2, (float)var4);
      }
   }

   private static void centerText(Graphics2D var0, String var1, double var2, double var4) {
      double var6 = var0.getFontMetrics().getStringBounds(var1, var0).getWidth();
      var0.drawString(var1, (float)(var2 - var6 / 2.0), (float)var4);
   }

   private static Color alpha(Color var0, int var1) {
      return new Color(var0.getRed(), var0.getGreen(), var0.getBlue(), Math.max(0, Math.min(255, var1)));
   }

   private static void noteIcon(Graphics2D var0, double var1, double var3, double var5) {
      var0.fill(new java.awt.geom.Ellipse2D.Double(var1 - 5.0 * var5, var3 + 1.2 * var5, 3.6 * var5, 2.8 * var5));
      var0.fill(new java.awt.geom.Ellipse2D.Double(var1 + 1.2 * var5, var3 + 0.2 * var5, 3.6 * var5, 2.8 * var5));
      var0.setStroke(new BasicStroke((float)(0.9 * var5)));
      var0.draw(new java.awt.geom.Line2D.Double(var1 - 1.8 * var5, var3 + 2.4 * var5, var1 - 1.8 * var5, var3 - 4.6 * var5));
      var0.draw(new java.awt.geom.Line2D.Double(var1 + 4.4 * var5, var3 + 1.4 * var5, var1 + 4.4 * var5, var3 - 5.6 * var5));
      var0.fill(new java.awt.geom.Rectangle2D.Double(var1 - 2.25 * var5, var3 - 5.6 * var5, 7.1 * var5, 1.5 * var5));
   }

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
}
