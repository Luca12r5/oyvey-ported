package dev.lego.hud;

import dev.lego.LegoClient;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.ui.Gx;
import dev.lego.ui.UiSettings;
import dev.spotifyhud.TexSlot;
import dev.spotifyhud.Theme;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Double;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

public final class HudManager {
   private static final Map<String, TexSlot> SLOTS = new HashMap<>();
   private static final Map<String, BufferedImage> CANVAS = new HashMap<>();
   private static HudModule dragging;
   private static double offX;
   private static double offY;
   public static double snapX = -1.0;
   public static double snapY = -1.0;

   private HudManager() {
   }

   public static List<HudModule> modules() {
      List<HudModule> out = new ArrayList<>();

      for (Module m : Modules.ALL) {
         if (m instanceof HudModule) {
            out.add((HudModule)m);
         }
      }

      return out;
   }

   public static int[] bounds(HudModule m, int sw, int sh) {
      int w = Math.max(4, (int)Math.round(m.w() * m.scale));
      int h = Math.max(4, (int)Math.round(m.h() * m.scale));
      int x = (int)Math.round(m.fx * Math.max(0, sw - w));
      int y = (int)Math.round(m.fy * Math.max(0, sh - h));
      return new int[]{x, y, w, h};
   }

   public static void render(GuiGraphics ctx, int sw, int sh, boolean editing, int mx, int my) {
      Minecraft mc = Mc.mc();
      int s = Math.max(1, mc.getWindow().getGuiScale());
      String theme = Modules.hudTheme;
      int accentArgb = Modules.hudAccentArgb();
      String style = UiSettings.hudStyle
         + "|"
         + UiSettings.hudOpacity
         + "|"
         + UiSettings.hudShadow
         + "|"
         + UiSettings.hudAccentLine
         + "|"
         + UiSettings.hudIcons;

      for (HudModule m : modules()) {
         if (m.enabled) {
            try {
               if (editing || m.visible()) {
                  int[] b = bounds(m, sw, sh);
                  boolean hover = editing && (m == dragging || mx >= b[0] && my >= b[1] && mx < b[0] + b[2] && my < b[1] + b[3]);
                  int pw = b[2] * s;
                  int ph = b[3] * s;
                  if (pw <= 4096 && ph <= 4096) {
                     String key = m.key() + "|" + theme + "|" + accentArgb + "|" + style + "|" + editing + hover + "|" + m.w() + "x" + m.h();
                     TexSlot slot = SLOTS.computeIfAbsent(m.id, k -> new TexSlot(Identifier.fromNamespaceAndPath("legoclient", "hud_" + k)));
                     double dw = m.w();
                     double dh = m.h();
                     slot.draw(ctx, mc, b[0], b[1], b[2], b[3], pw, ph, key, () -> paint(m, pw, ph, dw, dh, theme, accentArgb, editing, hover));
                     m.overlay(ctx, b[0], b[1], b[2] / dw);
                  }
               }
            } catch (Throwable var25) {
               LegoClient.LOG("HUD " + m.id + ": " + var25);
            }
         }
      }
   }

   private static int[] paint(HudModule m, int pw, int ph, double dw, double dh, String theme, int accentArgb, boolean editing, boolean hover) {
      BufferedImage img = CANVAS.get(m.id);
      if (img == null || img.getWidth() != pw || img.getHeight() != ph) {
         img = new BufferedImage(pw, ph, 2);
         CANVAS.put(m.id, img);
      }

      Graphics2D g = img.createGraphics();

      try {
         g.setComposite(AlphaComposite.Clear);
         g.fillRect(0, 0, pw, ph);
         g.setComposite(AlphaComposite.SrcOver);
         Gx.hints(g);
         double px = pw / dw;
         g.scale(px, ph / dh);
         Color accent = new Color(accentArgb, true);
         double r = Math.min(6.0, dh * 0.38);
         RoundRectangle2D shape = new Double(0.0, 0.0, dw, dh, r * 2.0, r * 2.0);
         if (m.panel()) {
            switch (UiSettings.hudStyle) {
               case 1: {
                  int a = (int)Math.round(150.0 * UiSettings.hudOpacity);
                  g.setColor(new Color(10, 11, 15, Math.max(40, Math.min(255, a))));
                  g.fill(shape);
                  break;
               }
               case 2:
                  Theme.paint(g, theme, shape, dw, dh, null, px);
                  g.setColor(new Color(255, 255, 255, 14));
                  g.setStroke(new BasicStroke((float)(1.0 / px)));
                  g.draw(new Double(0.5 / px, 0.5 / px, dw - 1.0 / px, dh - 1.0 / px, r * 2.0, r * 2.0));
                  break;
               default: {
                  int a = (int)Math.round(255.0 * UiSettings.hudOpacity);
                  g.setColor(new Color(10, 11, 15, Math.max(0, Math.min(255, a))));
                  g.fill(shape);
                  g.setColor(new Color(255, 255, 255, (int)(18.0 + 10.0 * UiSettings.hudOpacity)));
                  g.setStroke(new BasicStroke((float)(1.0 / px)));
                  g.draw(new Double(0.5 / px, 0.5 / px, dw - 1.0 / px, dh - 1.0 / px, r * 2.0, r * 2.0));
               }
            }

            if (UiSettings.hudAccentLine && UiSettings.hudStyle != 1) {
               g.setColor(accent);
               g.fill(new Double(0.0, dh * 0.22, 1.6, dh * 0.56, 1.6, 1.6));
            }
         }

         m.paint(g, accent);
         if (editing) {
            g.setColor(hover ? accent : new Color(255, 255, 255, 110));
            g.setStroke(new BasicStroke((float)(hover ? 1.3 : 0.8), 1, 1, 1.0F, new float[]{3.0F, 2.0F}, 0.0F));
            g.draw(new Double(0.6, 0.6, dw - 1.2, dh - 1.2, r * 2.0, r * 2.0));
         }
      } finally {
         g.dispose();
      }

      return img.getRGB(0, 0, pw, ph, null, 0, pw);
   }

   public static HudModule at(double mx, double my, int sw, int sh) {
      List<HudModule> list = modules();

      for (int i = list.size() - 1; i >= 0; i--) {
         HudModule m = list.get(i);
         if (m.enabled) {
            int[] b = bounds(m, sw, sh);
            if (mx >= b[0] && my >= b[1] && mx < b[0] + b[2] && my < b[1] + b[3]) {
               return m;
            }
         }
      }

      return null;
   }

   public static boolean mouseDown(double mx, double my, int button, int sw, int sh) {
      HudModule m = at(mx, my, sw, sh);
      if (m == null) {
         return false;
      } else if (button == 1) {
         m.resetPosition();
         Modules.scheduleSave();
         return true;
      } else if (button != 0) {
         return true;
      } else {
         int[] b = bounds(m, sw, sh);
         dragging = m;
         offX = mx - b[0];
         offY = my - b[1];
         return true;
      }
   }

   public static boolean mouseDrag(double mx, double my, int sw, int sh, boolean snap) {
      if (dragging == null) {
         return false;
      } else {
         int[] b = bounds(dragging, sw, sh);
         double x = mx - offX;
         double y = my - offY;
         snapX = -1.0;
         snapY = -1.0;
         if (snap) {
            double th = 4.0;
            List<double[]> xs = new ArrayList<>();
            List<double[]> ys = new ArrayList<>();
            xs.add(new double[]{0.0, 0.0});
            xs.add(new double[]{sw / 2.0 - b[2] / 2.0, sw / 2.0});
            xs.add(new double[]{sw - b[2], sw});
            ys.add(new double[]{0.0, 0.0});
            ys.add(new double[]{sh / 2.0 - b[3] / 2.0, sh / 2.0});
            ys.add(new double[]{sh - b[3], sh});

            for (HudModule o : modules()) {
               if (o != dragging && o.enabled) {
                  int[] ob = bounds(o, sw, sh);
                  xs.add(new double[]{ob[0], ob[0]});
                  xs.add(new double[]{ob[0] + ob[2] - b[2], ob[0] + ob[2]});
                  xs.add(new double[]{ob[0] + ob[2] + 2, ob[0] + ob[2] + 2});
                  xs.add(new double[]{ob[0] - b[2] - 2, ob[0] - 2});
                  ys.add(new double[]{ob[1], ob[1]});
                  ys.add(new double[]{ob[1] + ob[3] - b[3], ob[1] + ob[3]});
                  ys.add(new double[]{ob[1] + ob[3] + 2, ob[1] + ob[3] + 2});
                  ys.add(new double[]{ob[1] - b[3] - 2, ob[1] - 2});
               }
            }

            double bestX = th;
            double bestY = th;

            for (double[] c : xs) {
               if (Math.abs(x - c[0]) < bestX) {
                  bestX = Math.abs(x - c[0]);
                  x = c[0];
                  snapX = c[1];
               }
            }

            for (double[] cx : ys) {
               if (Math.abs(y - cx[0]) < bestY) {
                  bestY = Math.abs(y - cx[0]);
                  y = cx[0];
                  snapY = cx[1];
               }
            }
         }

         int freeX = sw - b[2];
         int freeY = sh - b[3];
         dragging.fx = freeX > 0 ? Math.max(0.0, Math.min(1.0, x / freeX)) : 0.0;
         dragging.fy = freeY > 0 ? Math.max(0.0, Math.min(1.0, y / freeY)) : 0.0;
         return true;
      }
   }

   public static boolean mouseDrag(double mx, double my, int sw, int sh) {
      return mouseDrag(mx, my, sw, sh, false);
   }

   public static boolean mouseUp() {
      snapY = -1.0;
      snapX = -1.0;
      if (dragging == null) {
         return false;
      } else {
         dragging = null;
         Modules.scheduleSave();
         return true;
      }
   }

   public static boolean scroll(double mx, double my, double v, int sw, int sh) {
      HudModule m = at(mx, my, sw, sh);
      if (m != null && v != 0.0) {
         int[] b = bounds(m, sw, sh);
         double relX = (mx - b[0]) / b[2];
         double relY = (my - b[1]) / b[3];
         m.scale = Math.round(Math.max(0.4, Math.min(3.0, m.scale + (v > 0.0 ? 0.05 : -0.05))) * 100.0) / 100.0;
         int[] n = bounds(m, sw, sh);
         int freeX = sw - n[2];
         int freeY = sh - n[3];
         m.fx = freeX > 0 ? Math.max(0.0, Math.min(1.0, (mx - relX * n[2]) / freeX)) : 0.0;
         m.fy = freeY > 0 ? Math.max(0.0, Math.min(1.0, (my - relY * n[3]) / freeY)) : 0.0;
         Modules.scheduleSave();
         return true;
      } else {
         return false;
      }
   }

   public static void resetAll() {
      for (HudModule m : modules()) {
         m.resetPosition();
      }

      Modules.scheduleSave();
   }

   public static HudModule dragged() {
      return dragging;
   }

   public static boolean dragging() {
      return dragging != null;
   }
}
