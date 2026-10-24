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
      ArrayList var0 = new ArrayList();

      for (Module var2 : Modules.ALL) {
         if (var2 instanceof HudModule) {
            var0.add((HudModule)var2);
         }
      }

      return var0;
   }

   public static int[] bounds(HudModule var0, int var1, int var2) {
      int var3 = Math.max(4, (int)Math.round(var0.w() * var0.scale));
      int var4 = Math.max(4, (int)Math.round(var0.h() * var0.scale));
      int var5 = (int)Math.round(var0.fx * Math.max(0, var1 - var3));
      int var6 = (int)Math.round(var0.fy * Math.max(0, var2 - var4));
      return new int[]{var5, var6, var3, var4};
   }

   public static void render(GuiGraphics var0, int var1, int var2, boolean var3, int var4, int var5) {
      Minecraft var6 = Mc.mc();
      int var7 = Math.max(1, var6.getWindow().getGuiScale());
      String var8 = Modules.hudTheme;
      int var9 = Modules.hudAccentArgb();
      String var10 = UiSettings.hudStyle
         + "|"
         + UiSettings.hudOpacity
         + "|"
         + UiSettings.hudShadow
         + "|"
         + UiSettings.hudAccentLine
         + "|"
         + UiSettings.hudIcons;

      for (HudModule var12 : modules()) {
         if (var12.enabled) {
            try {
               if (var3 || var12.visible()) {
                  int[] var13 = bounds(var12, var1, var2);
                  boolean var14 = var3
                     && (var12 == dragging || var4 >= var13[0] && var5 >= var13[1] && var4 < var13[0] + var13[2] && var5 < var13[1] + var13[3]);
                  int var15 = var13[2] * var7;
                  int var16 = var13[3] * var7;
                  if (var15 <= 4096 && var16 <= 4096) {
                     String var17 = var12.key() + "|" + var8 + "|" + var9 + "|" + var10 + "|" + var3 + var14 + "|" + var12.w() + "x" + var12.h();
                     TexSlot var18 = SLOTS.computeIfAbsent(var12.id, var0x -> new TexSlot(Identifier.fromNamespaceAndPath("legoclient", "hud_" + var0x)));
                     double var19 = var12.w();
                     double var21 = var12.h();
                     var18.draw(
                        var0,
                        var6,
                        var13[0],
                        var13[1],
                        var13[2],
                        var13[3],
                        var15,
                        var16,
                        var17,
                        () -> paint(var12, var15, var16, var19, var21, var8, var9, var3, var14)
                     );
                     var12.overlay(var0, var13[0], var13[1], var13[2] / var19);
                  }
               }
            } catch (Throwable var25) {
               LegoClient.LOG("HUD " + var12.id + ": " + var25);
            }
         }
      }
   }

   private static int[] paint(HudModule var0, int var1, int var2, double var3, double var5, String var7, int var8, boolean var9, boolean var10) {
      BufferedImage var11 = CANVAS.get(var0.id);
      if (var11 == null || var11.getWidth() != var1 || var11.getHeight() != var2) {
         var11 = new BufferedImage(var1, var2, 2);
         CANVAS.put(var0.id, var11);
      }

      Graphics2D var12 = var11.createGraphics();

      try {
         var12.setComposite(AlphaComposite.Clear);
         var12.fillRect(0, 0, var1, var2);
         var12.setComposite(AlphaComposite.SrcOver);
         Gx.hints(var12);
         double var13 = var1 / var3;
         var12.scale(var13, var2 / var5);
         Color var15 = new Color(var8, true);
         double var16 = Math.min(6.0, var5 * 0.38);
         Double var18 = new Double(0.0, 0.0, var3, var5, var16 * 2.0, var16 * 2.0);
         if (var0.panel()) {
            switch (UiSettings.hudStyle) {
               case 1:
                  break;
               case 2:
                  Theme.paint(var12, var7, var18, var3, var5, null, var13);
                  var12.setColor(new Color(255, 255, 255, 14));
                  var12.setStroke(new BasicStroke((float)(1.0 / var13)));
                  var12.draw(new Double(0.5 / var13, 0.5 / var13, var3 - 1.0 / var13, var5 - 1.0 / var13, var16 * 2.0, var16 * 2.0));
                  break;
               default:
                  int var19 = (int)Math.round(255.0 * UiSettings.hudOpacity);
                  var12.setColor(new Color(10, 11, 15, Math.max(0, Math.min(255, var19))));
                  var12.fill(var18);
                  var12.setColor(new Color(255, 255, 255, (int)(18.0 + 10.0 * UiSettings.hudOpacity)));
                  var12.setStroke(new BasicStroke((float)(1.0 / var13)));
                  var12.draw(new Double(0.5 / var13, 0.5 / var13, var3 - 1.0 / var13, var5 - 1.0 / var13, var16 * 2.0, var16 * 2.0));
            }

            if (UiSettings.hudAccentLine && UiSettings.hudStyle != 1) {
               var12.setColor(var15);
               var12.fill(new Double(0.0, var5 * 0.22, 1.6, var5 * 0.56, 1.6, 1.6));
            }
         }

         var0.paint(var12, var15);
         if (var9) {
            var12.setColor(var10 ? var15 : new Color(255, 255, 255, 110));
            var12.setStroke(new BasicStroke((float)(var10 ? 1.3 : 0.8), 1, 1, 1.0F, new float[]{3.0F, 2.0F}, 0.0F));
            var12.draw(new Double(0.6, 0.6, var3 - 1.2, var5 - 1.2, var16 * 2.0, var16 * 2.0));
         }
      } finally {
         var12.dispose();
      }

      return var11.getRGB(0, 0, var1, var2, null, 0, var1);
   }

   public static HudModule at(double var0, double var2, int var4, int var5) {
      List var6 = modules();

      for (int var7 = var6.size() - 1; var7 >= 0; var7--) {
         HudModule var8 = (HudModule)var6.get(var7);
         if (var8.enabled) {
            int[] var9 = bounds(var8, var4, var5);
            if (var0 >= var9[0] && var2 >= var9[1] && var0 < var9[0] + var9[2] && var2 < var9[1] + var9[3]) {
               return var8;
            }
         }
      }

      return null;
   }

   public static boolean mouseDown(double var0, double var2, int var4, int var5, int var6) {
      HudModule var7 = at(var0, var2, var5, var6);
      if (var7 == null) {
         return false;
      } else if (var4 == 1) {
         var7.resetPosition();
         Modules.scheduleSave();
         return true;
      } else if (var4 != 0) {
         return true;
      } else {
         int[] var8 = bounds(var7, var5, var6);
         dragging = var7;
         offX = var0 - var8[0];
         offY = var2 - var8[1];
         return true;
      }
   }

   public static boolean mouseDrag(double var0, double var2, int var4, int var5, boolean var6) {
      if (dragging == null) {
         return false;
      } else {
         int[] var7 = bounds(dragging, var4, var5);
         double var8 = var0 - offX;
         double var10 = var2 - offY;
         snapX = -1.0;
         snapY = -1.0;
         if (var6) {
            double var12 = 4.0;
            ArrayList var14 = new ArrayList();
            ArrayList var15 = new ArrayList();
            var14.add(new double[]{0.0, 0.0});
            var14.add(new double[]{var4 / 2.0 - var7[2] / 2.0, var4 / 2.0});
            var14.add(new double[]{var4 - var7[2], var4});
            var15.add(new double[]{0.0, 0.0});
            var15.add(new double[]{var5 / 2.0 - var7[3] / 2.0, var5 / 2.0});
            var15.add(new double[]{var5 - var7[3], var5});

            for (HudModule var17 : modules()) {
               if (var17 != dragging && var17.enabled) {
                  int[] var18 = bounds(var17, var4, var5);
                  var14.add(new double[]{var18[0], var18[0]});
                  var14.add(new double[]{var18[0] + var18[2] - var7[2], var18[0] + var18[2]});
                  var14.add(new double[]{var18[0] + var18[2] + 2, var18[0] + var18[2] + 2});
                  var14.add(new double[]{var18[0] - var7[2] - 2, var18[0] - 2});
                  var15.add(new double[]{var18[1], var18[1]});
                  var15.add(new double[]{var18[1] + var18[3] - var7[3], var18[1] + var18[3]});
                  var15.add(new double[]{var18[1] + var18[3] + 2, var18[1] + var18[3] + 2});
                  var15.add(new double[]{var18[1] - var7[3] - 2, var18[1] - 2});
               }
            }

            double var23 = var12;
            double var24 = var12;

            for (double[] var21 : var14) {
               if (Math.abs(var8 - var21[0]) < var23) {
                  var23 = Math.abs(var8 - var21[0]);
                  var8 = var21[0];
                  snapX = var21[1];
               }
            }

            for (double[] var26 : var15) {
               if (Math.abs(var10 - var26[0]) < var24) {
                  var24 = Math.abs(var10 - var26[0]);
                  var10 = var26[0];
                  snapY = var26[1];
               }
            }
         }

         int var22 = var4 - var7[2];
         int var13 = var5 - var7[3];
         dragging.fx = var22 > 0 ? Math.max(0.0, Math.min(1.0, var8 / var22)) : 0.0;
         dragging.fy = var13 > 0 ? Math.max(0.0, Math.min(1.0, var10 / var13)) : 0.0;
         return true;
      }
   }

   public static boolean mouseDrag(double var0, double var2, int var4, int var5) {
      return mouseDrag(var0, var2, var4, var5, false);
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

   public static boolean scroll(double var0, double var2, double var4, int var6, int var7) {
      HudModule var8 = at(var0, var2, var6, var7);
      if (var8 != null && var4 != 0.0) {
         int[] var9 = bounds(var8, var6, var7);
         double var10 = (var0 - var9[0]) / var9[2];
         double var12 = (var2 - var9[1]) / var9[3];
         var8.scale = Math.round(Math.max(0.4, Math.min(3.0, var8.scale + (var4 > 0.0 ? 0.05 : -0.05))) * 100.0) / 100.0;
         int[] var14 = bounds(var8, var6, var7);
         int var15 = var6 - var14[2];
         int var16 = var7 - var14[3];
         var8.fx = var15 > 0 ? Math.max(0.0, Math.min(1.0, (var0 - var10 * var14[2]) / var15)) : 0.0;
         var8.fy = var16 > 0 ? Math.max(0.0, Math.min(1.0, (var2 - var12 * var14[3]) / var16)) : 0.0;
         Modules.scheduleSave();
         return true;
      } else {
         return false;
      }
   }

   public static void resetAll() {
      for (HudModule var1 : modules()) {
         var1.resetPosition();
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
