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
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
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

   static int[] render(int pw, int ph, SettingsUi ui, List<SettingsUi.W> ws, Config cfg, SettingsPainter.Info info, double t) {
      if (canvas == null || canvas.getWidth() != pw || canvas.getHeight() != ph) {
         canvas = new BufferedImage(pw, ph, 2);
      }

      Graphics2D g = canvas.createGraphics();

      try {
         g.setComposite(AlphaComposite.Clear);
         g.fillRect(0, 0, pw, ph);
         g.setComposite(AlphaComposite.SrcOver);
         HudPainter.hints(g);
         g.scale(pw / 360.0, ph / 230.0);
         paint(g, ui, ws, cfg, info, t, pw / 360.0);
      } finally {
         g.dispose();
      }

      return canvas.getRGB(0, 0, pw, ph, null, 0, pw);
   }

   private static void paint(Graphics2D g, SettingsUi ui, List<SettingsUi.W> ws, Config cfg, SettingsPainter.Info info, double t, double px) {
      BufferedImage art = info.art instanceof BufferedImage ? (BufferedImage)info.art : null;
      Color accent = Theme.accent(cfg.theme, art);
      RoundRectangle2D card = new Double(0.0, 0.0, 360.0, 230.0, 22.0, 22.0);
      g.setPaint(new GradientPaint(0.0F, 0.0F, new Color(24, 24, 30, 246), 0.0F, 230.0F, new Color(12, 12, 16, 246)));
      g.fill(card);
      g.setPaint(
         new RadialGradientPaint(
            new java.awt.geom.Point2D.Double(40.0, 10.0), 170.0F, new float[]{0.0F, 1.0F}, new Color[]{alpha(accent, 40), alpha(accent, 0)}
         )
      );
      g.fill(card);
      g.setColor(new Color(255, 255, 255, 22));
      g.setStroke(new BasicStroke((float)(1.0 / px)));
      g.draw(new Double(0.5 / px, 0.5 / px, 360.0 - 1.0 / px, 230.0 - 1.0 / px, 22.0, 22.0));
      g.setPaint(new GradientPaint(14.0F, 10.0F, accent, 36.0F, 32.0F, accent.darker()));
      g.fill(new java.awt.geom.Ellipse2D.Double(14.0, 10.0, 22.0, 22.0));
      g.setColor(new Color(10, 10, 10));
      noteIcon(g, 25.0, 21.0, 0.9);
      g.setFont(HudPainter.uiFont(true, 12.5F));
      g.setColor(TXT);
      g.drawString("Spotify HUD", 44.0F, 22.5F);
      g.setFont(HudPainter.uiFont(false, 7.6F));
      g.setColor(SUB);
      g.drawString("Einstellungen", 44.0F, 31.5F);
      g.setColor(LINE);
      g.fill(new java.awt.geom.Rectangle2D.Double(12.0, 40.0, 336.0, 1.0 / px));
      g.fill(new java.awt.geom.Rectangle2D.Double(100.0, 46.0, 1.0 / px, 158.0));

      for (SettingsUi.W w : ws) {
         boolean hov = w.id.equals(ui.hover);
         switch (w.kind) {
            case CLOSE:
               close(g, w, hov);
               break;
            case TAB:
               tab(g, w, hov, accent);
               break;
            case THEME:
               themeCard(g, w, hov, accent, art, px);
               break;
            case ANIM:
               animCard(g, w, hov, accent, t);
               break;
            case TOGGLE:
               toggle(g, w, hov, accent);
               break;
            case SLIDER:
               slider(g, w, hov || w.id.equals(ui.dragging), accent);
               break;
            case BUTTON:
               button(g, w, hov, accent);
               break;
            case FIELD:
               field(g, w, hov, accent, info.caretOn, ui.selectAll);
         }
      }

      g.setFont(HudPainter.uiFont(true, 9.5F));
      g.setColor(TXT);
      double cx = 108.0;
      switch (ui.tab) {
         case 0:
            g.drawString("Thema", (float)cx, 57.0F);
            small(g, "Wirkt sofort auf HUD und Lyrics.", cx, 186.0, SUB);
            small(g, "\"Cover\" = verschwommenes Albumcover als Hintergrund.", cx, 197.0, DIM);
            break;
         case 1:
            g.drawString("Animation", (float)cx, 57.0F);
            break;
         case 2:
            small(g, "Automatische Latenz: " + info.latency + " ms  -  Sync verschiebt die Lyrics zusätzlich.", cx, 200.0, DIM);
            break;
         case 3:
            small(g, "Im Chat (T): ziehen = verschieben, Mausrad = Größe,", cx, 184.0, SUB);
            small(g, "Rechtsklick = Lyrics an/aus, Zahnrad = dieses Menü.", cx, 195.0, SUB);
            break;
         default:
            g.drawString("Spotify-Konto (optional)", (float)cx, 57.0F);
            small(g, info.account, cx, 68.0, info.accountColor);
            small(g, "Nur für das Herz nötig - alles andere funktioniert automatisch.", cx, 148.0, SUB);
            small(g, "Redirect-URI im Dashboard: " + info.redirect, cx, 160.0, SUB);
            small(g, "1. Dashboard  2. App + URI + Web API  3. Client-ID  4. Anmelden", cx, 176.0, DIM);
      }

      g.setColor(LINE);
      g.fill(new java.awt.geom.Rectangle2D.Double(12.0, 206.0, 336.0, 1.0 / px));
      g.setColor(info.detectColor);
      g.fill(new java.awt.geom.Ellipse2D.Double(16.0, 214.0, 5.0, 5.0));
      small(g, info.detect, 25.0, 219.0, SUB);
      g.setFont(HudPainter.uiFont(false, 7.0F));
      g.setColor(DIM);
      String ver = "v1.2";
      g.drawString(ver, (float)(344.0 - g.getFontMetrics().getStringBounds(ver, g).getWidth()), 219.0F);
   }

   private static void close(Graphics2D g, SettingsUi.W w, boolean hov) {
      if (hov) {
         g.setColor(new Color(255, 255, 255, 26));
         g.fill(new java.awt.geom.Ellipse2D.Double(w.x, w.y, w.w, w.h));
      }

      g.setColor(hov ? TXT : SUB);
      g.setStroke(new BasicStroke(1.3F, 1, 1));
      double cx = w.x + w.w / 2.0;
      double cy = w.y + w.h / 2.0;
      double r = 3.6;
      g.draw(new java.awt.geom.Line2D.Double(cx - r, cy - r, cx + r, cy + r));
      g.draw(new java.awt.geom.Line2D.Double(cx - r, cy + r, cx + r, cy - r));
   }

   private static void tab(Graphics2D g, SettingsUi.W w, boolean hov, Color accent) {
      if (w.selected || hov) {
         g.setColor(new Color(255, 255, 255, w.selected ? 22 : 12));
         g.fill(new Double(w.x, w.y, w.w, w.h, 10.0, 10.0));
      }

      if (w.selected) {
         g.setColor(accent);
         g.fill(new Double(w.x, w.y + 5.0, 2.4, w.h - 10.0, 2.4, 2.4));
      }

      g.setColor(w.selected ? TXT : (hov ? new Color(220, 220, 224) : SUB));
      tabIcon(g, w.label, w.x + 12.0, w.y + w.h / 2.0, w.selected ? accent : g.getColor());
      g.setColor(w.selected ? TXT : (hov ? new Color(220, 220, 224) : SUB));
      g.setFont(HudPainter.uiFont(w.selected, 8.6F));
      g.drawString(w.label, (float)(w.x + 22.0), (float)(w.y + w.h / 2.0 + 3.0));
   }

   private static void tabIcon(Graphics2D g, String tab, double cx, double cy, Color c) {
      g.setColor(c);
      g.setStroke(new BasicStroke(1.1F, 1, 1));
      switch (tab) {
         case "Design":
            g.draw(new java.awt.geom.Ellipse2D.Double(cx - 4.0, cy - 4.0, 8.0, 8.0));
            g.fill(new java.awt.geom.Ellipse2D.Double(cx - 2.2, cy - 2.4, 1.6, 1.6));
            g.fill(new java.awt.geom.Ellipse2D.Double(cx + 0.6, cy - 2.4, 1.6, 1.6));
            g.fill(new java.awt.geom.Ellipse2D.Double(cx - 2.6, cy + 0.4, 1.6, 1.6));
            break;
         case "Animation":
            Path2D p = new java.awt.geom.Path2D.Double();
            double r = 4.2;
            p.moveTo(cx, cy - r);
            p.quadTo(cx, cy, cx + r, cy);
            p.quadTo(cx, cy, cx, cy + r);
            p.quadTo(cx, cy, cx - r, cy);
            p.quadTo(cx, cy, cx, cy - r);
            g.fill(p);
            break;
         case "Lyrics":
            g.draw(new java.awt.geom.Line2D.Double(cx - 4.0, cy - 3.0, cx + 4.0, cy - 3.0));
            g.draw(new java.awt.geom.Line2D.Double(cx - 4.0, cy, cx + 2.0, cy));
            g.draw(new java.awt.geom.Line2D.Double(cx - 4.0, cy + 3.0, cx + 3.2, cy + 3.0));
            break;
         case "HUD":
            g.draw(new Double(cx - 4.5, cy - 3.5, 9.0, 7.0, 2.5, 2.5));
            g.fill(new java.awt.geom.Rectangle2D.Double(cx - 3.0, cy + 0.6, 6.0, 1.1));
            break;
         default:
            g.draw(new java.awt.geom.Ellipse2D.Double(cx - 1.9, cy - 4.2, 3.8, 3.8));
            g.draw(new java.awt.geom.Arc2D.Double(cx - 4.0, cy + 0.2, 8.0, 7.0, 0.0, 180.0, 0));
      }
   }

   private static void themeCard(Graphics2D g, SettingsUi.W w, boolean hov, Color accent, BufferedImage art, double px) {
      double ph = w.h - 14.0;
      RoundRectangle2D box = new Double(w.x, w.y, w.w, ph, 9.0, 9.0);
      Shape old = g.getClip();
      AffineTransform at = g.getTransform();
      g.clip(box);
      g.setColor(new Color(40, 44, 52));
      g.fill(box);
      g.translate(w.x, w.y);
      Theme.paint(g, w.ref, new Double(0.0, 0.0, w.w, ph, 9.0, 9.0), w.w, ph, art, px);
      g.setColor(new Color(255, 255, 255, 60));
      g.fill(new Double(5.0, 6.0, 13.0, 13.0, 3.0, 3.0));
      g.setColor(new Color(255, 255, 255, 210));
      g.fill(new Double(22.0, 7.5, 24.0, 3.0, 3.0, 3.0));
      g.setColor(new Color(255, 255, 255, 110));
      g.fill(new Double(22.0, 13.0, 16.0, 2.4, 2.4, 2.4));
      g.setColor(new Color(255, 255, 255, 50));
      g.fill(new Double(5.0, 24.0, w.w - 10.0, 2.0, 2.0, 2.0));
      Color ta = Theme.accent(w.ref, art);
      g.setColor(ta);
      g.fill(new Double(5.0, 24.0, (w.w - 10.0) * 0.45, 2.0, 2.0, 2.0));
      g.setTransform(at);
      g.setClip(old);
      if (w.selected || hov) {
         g.setColor(w.selected ? accent : new Color(255, 255, 255, 90));
         g.setStroke(new BasicStroke(w.selected ? 1.6F : 1.0F));
         g.draw(new Double(w.x - 0.8, w.y - 0.8, w.w + 1.6, ph + 1.6, 10.0, 10.0));
      }

      if (w.selected) {
         check(g, w.x + w.w - 7.0, w.y + 7.0, accent);
      }

      g.setFont(HudPainter.uiFont(w.selected, 7.3F));
      g.setColor(w.selected ? TXT : SUB);
      centerText(g, w.label, w.x + w.w / 2.0, w.y + w.h - 3.0);
   }

   private static void animCard(Graphics2D g, SettingsUi.W w, boolean hov, Color accent, double t) {
      double ph = w.h - 13.0;
      Anim.icon(g, w.ref, w.x, w.y, ph, t, accent);
      double iw = ph * 1.6;
      if (w.selected || hov) {
         g.setColor(w.selected ? accent : new Color(255, 255, 255, 90));
         g.setStroke(new BasicStroke(w.selected ? 1.6F : 1.0F));
         g.draw(new Double(w.x - 0.8, w.y - 0.8, iw + 1.6, ph + 1.6, 7.0, 7.0));
      }

      if (w.selected) {
         check(g, w.x + iw - 6.0, w.y + 6.0, accent);
      }

      g.setFont(HudPainter.uiFont(w.selected, 7.1F));
      g.setColor(w.selected ? TXT : SUB);
      centerText(g, w.label, w.x + iw / 2.0, w.y + w.h - 3.0);
   }

   private static void check(Graphics2D g, double cx, double cy, Color accent) {
      g.setColor(accent);
      g.fill(new java.awt.geom.Ellipse2D.Double(cx - 4.2, cy - 4.2, 8.4, 8.4));
      g.setColor(new Color(10, 10, 10));
      g.setStroke(new BasicStroke(1.2F, 1, 1));
      Path2D p = new java.awt.geom.Path2D.Double();
      p.moveTo(cx - 2.0, cy + 0.1);
      p.lineTo(cx - 0.5, cy + 1.6);
      p.lineTo(cx + 2.2, cy - 1.4);
      g.draw(p);
   }

   private static void toggle(Graphics2D g, SettingsUi.W w, boolean hov, Color accent) {
      if (hov) {
         g.setColor(new Color(255, 255, 255, 10));
         g.fill(new Double(w.x - 4.0, w.y - 2.0, w.w + 8.0, w.h + 4.0, 10.0, 10.0));
      }

      g.setFont(HudPainter.uiFont(false, 8.8F));
      g.setColor(TXT);
      boolean hasSub = w.sub != null && !w.sub.isEmpty();
      g.drawString(w.label, (float)w.x, (float)(w.y + (hasSub ? 9.5 : w.h / 2.0 + 3.0)));
      if (hasSub) {
         g.setFont(HudPainter.uiFont(false, 7.0F));
         g.setColor(SUB);
         g.drawString(w.sub, (float)w.x, (float)(w.y + 19.5));
      }

      double sw = 22.0;
      double sh = 12.0;
      double sx = w.x + w.w - sw;
      double sy = w.y + (w.h - sh) / 2.0;
      g.setColor(w.on ? accent : new Color(80, 80, 88));
      g.fill(new Double(sx, sy, sw, sh, sh, sh));
      double kx = w.on ? sx + sw - sh + 1.5 : sx + 1.5;
      g.setColor(new Color(0, 0, 0, 60));
      g.fill(new java.awt.geom.Ellipse2D.Double(kx, sy + 1.9, sh - 3.0, sh - 3.0));
      g.setColor(Color.WHITE);
      g.fill(new java.awt.geom.Ellipse2D.Double(kx, sy + 1.5, sh - 3.0, sh - 3.0));
   }

   private static void slider(Graphics2D g, SettingsUi.W w, boolean hov, Color accent) {
      g.setFont(HudPainter.uiFont(false, 8.8F));
      g.setColor(TXT);
      g.drawString(w.label, (float)w.x, (float)(w.y + 7.0));
      g.setFont(HudPainter.uiFont(true, 8.0F));
      g.setColor(hov ? accent : SUB);
      FontMetrics fm = g.getFontMetrics();
      g.drawString(w.sub, (float)(w.x + w.w - fm.getStringBounds(w.sub, g).getWidth()), (float)(w.y + 7.0));
      double ty = w.y + 14.0;
      double th = 3.4;
      g.setColor(new Color(255, 255, 255, 36));
      g.fill(new Double(w.x, ty, w.w, th, th, th));
      double v = Math.max(0.0, Math.min(1.0, w.value));
      g.setColor(accent);
      g.fill(new Double(w.x, ty, Math.max(th, w.w * v), th, th, th));
      double kx = w.x + w.w * v;
      double ky = ty + th / 2.0;
      double r = hov ? 4.4 : 3.8;
      g.setColor(new Color(0, 0, 0, 80));
      g.fill(new java.awt.geom.Ellipse2D.Double(kx - r, ky - r + 0.5, 2.0 * r, 2.0 * r));
      g.setColor(Color.WHITE);
      g.fill(new java.awt.geom.Ellipse2D.Double(kx - r, ky - r, 2.0 * r, 2.0 * r));
   }

   private static void button(Graphics2D g, SettingsUi.W w, boolean hov, Color accent) {
      RoundRectangle2D r = new Double(w.x, w.y, w.w, w.h, w.h, w.h);
      if (w.selected) {
         g.setColor(hov ? accent.brighter() : accent);
         g.fill(r);
      } else {
         g.setColor(new Color(255, 255, 255, hov ? 34 : 20));
         g.fill(r);
         g.setColor(new Color(255, 255, 255, 30));
         g.setStroke(new BasicStroke(0.7F));
         g.draw(r);
      }

      g.setFont(HudPainter.uiFont(true, 7.8F));
      g.setColor(w.selected ? new Color(12, 12, 12) : TXT);
      centerText(g, w.label, w.x + w.w / 2.0, w.y + w.h / 2.0 + 2.8);
   }

   private static void field(Graphics2D g, SettingsUi.W w, boolean hov, Color accent, boolean caret, boolean selectAll) {
      g.setFont(HudPainter.uiFont(false, 7.0F));
      g.setColor(SUB);
      g.drawString(w.label, (float)w.x, (float)(w.y - 3.0));
      RoundRectangle2D r = new Double(w.x, w.y, w.w, w.h, 8.0, 8.0);
      g.setColor(new Color(0, 0, 0, 90));
      g.fill(r);
      g.setColor(w.selected ? accent : new Color(255, 255, 255, hov ? 60 : 30));
      g.setStroke(new BasicStroke(w.selected ? 1.2F : 0.8F));
      g.draw(r);
      String txt = w.sub == null ? "" : w.sub;
      g.setFont(HudPainter.uiFont(false, 8.2F));
      FontMetrics fm = g.getFontMetrics();
      double maxW = w.w - 12.0;
      String shown = txt;

      while (shown.length() > 0 && fm.getStringBounds(shown, g).getWidth() > maxW) {
         shown = shown.substring(1);
      }

      double tw = fm.getStringBounds(shown, g).getWidth();
      if (w.selected && selectAll && !txt.isEmpty()) {
         g.setColor(alpha(accent, 90));
         g.fill(new java.awt.geom.Rectangle2D.Double(w.x + 6.0, w.y + 4.0, tw, w.h - 8.0));
      }

      if (txt.isEmpty() && !w.selected) {
         g.setColor(DIM);
         g.drawString(w.label.equals("Port") ? "8888" : "hier einfügen (Strg+V)", (float)(w.x + 6.0), (float)(w.y + w.h / 2.0 + 3.0));
      } else {
         g.setColor(TXT);
         g.drawString(shown, (float)(w.x + 6.0), (float)(w.y + w.h / 2.0 + 3.0));
      }

      if (w.selected && caret) {
         g.setColor(TXT);
         g.fill(new java.awt.geom.Rectangle2D.Double(w.x + 6.5 + tw, w.y + 5.0, 0.8, w.h - 10.0));
      }
   }

   private static void small(Graphics2D g, String s, double x, double baseline, Color c) {
      if (s != null && !s.isEmpty()) {
         g.setFont(HudPainter.uiFont(false, 7.2F));
         g.setColor(c);
         FontMetrics fm = g.getFontMetrics();
         double max = 346.0 - x;
         String t = s;
         if (fm.getStringBounds(s, g).getWidth() > max) {
            while (t.length() > 1 && fm.getStringBounds(t + "…", g).getWidth() > max) {
               t = t.substring(0, t.length() - 1);
            }

            t = t + "…";
         }

         g.drawString(t, (float)x, (float)baseline);
      }
   }

   private static void centerText(Graphics2D g, String s, double cx, double baseline) {
      double w = g.getFontMetrics().getStringBounds(s, g).getWidth();
      g.drawString(s, (float)(cx - w / 2.0), (float)baseline);
   }

   private static Color alpha(Color c, int a) {
      return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
   }

   private static void noteIcon(Graphics2D g, double cx, double cy, double k) {
      g.fill(new java.awt.geom.Ellipse2D.Double(cx - 5.0 * k, cy + 1.2 * k, 3.6 * k, 2.8 * k));
      g.fill(new java.awt.geom.Ellipse2D.Double(cx + 1.2 * k, cy + 0.2 * k, 3.6 * k, 2.8 * k));
      g.setStroke(new BasicStroke((float)(0.9 * k)));
      g.draw(new java.awt.geom.Line2D.Double(cx - 1.8 * k, cy + 2.4 * k, cx - 1.8 * k, cy - 4.6 * k));
      g.draw(new java.awt.geom.Line2D.Double(cx + 4.4 * k, cy + 1.4 * k, cx + 4.4 * k, cy - 5.6 * k));
      g.fill(new java.awt.geom.Rectangle2D.Double(cx - 2.25 * k, cy - 5.6 * k, 7.1 * k, 1.5 * k));
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
