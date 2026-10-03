package dev.lego.cosmetic;

import dev.lego.ui.Gx;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D.Double;
import java.awt.geom.Rectangle2D.Float;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Capes {
   static final int W = 512;
   static final int H = 256;
   static final int FW1 = 80;
   static final int FH1 = 128;
   static final int S = 2;
   static final int FW = 160;
   static final int FH = 256;
   static final Map<String, Capes.Spec> SPECS = new LinkedHashMap<>();

   private Capes() {
   }

   static void def(String var0, int var1, float var2, int var3, int var4, int var5, Capes.Painter var6) {
      SPECS.put(var0, new Capes.Spec(var0, Math.max(1, Math.min(16, var1)), var1 > 1 ? var2 : 0.0F, var3, var4, var5, var6));
   }

   private static Capes.Spec spec(String var0) {
      if (var0 == null) {
         return null;
      } else {
         String var1 = var0.startsWith("cape_") ? var0.substring(5) : var0;
         int var2 = var1.indexOf(35);
         if (var2 >= 0) {
            var1 = var1.substring(0, var2);
         }

         return SPECS.get(var1);
      }
   }

   public static int frames(String var0) {
      Capes.Spec var1 = spec(var0);
      return var1 == null ? 1 : var1.frames;
   }

   public static float fps(String var0) {
      Capes.Spec var1 = spec(var0);
      return var1 != null && var1.frames > 1 ? var1.fps : 0.0F;
   }

   public static String frameName(String var0, float var1) {
      int var2 = frames(var0);
      if (var2 <= 1) {
         return var0;
      } else {
         int var3 = Math.floorMod((int)Math.floor(var1 * fps(var0)), var2);
         return var3 == 0 ? var0 : var0 + "#" + var3;
      }
   }

   static CTex.T paint(String var0) {
      int var1 = 0;
      int var2 = var0.indexOf(35);
      if (var2 >= 0) {
         try {
            var1 = Integer.parseInt(var0.substring(var2 + 1));
         } catch (NumberFormatException var15) {
         }

         var0 = var0.substring(0, var2);
      }

      Capes.Spec var3 = SPECS.get(var0);
      if (var3 == null) {
         var3 = SPECS.get("lego");
      }

      int var4 = var3.frames;
      var1 = Math.floorMod(var1, var4);
      double var5 = (Math.PI * 2) * var1 / var4;
      BufferedImage var7 = new BufferedImage(160, 256, 2);
      Graphics2D var8 = var7.createGraphics();
      Gx.hints(var8);
      var8.setColor(new Color(var3.lining));
      var8.fillRect(0, 0, 160, 256);

      try {
         var3.p.paint(var8, var7, var5, var1, var4);
      } catch (RuntimeException var14) {
      }

      var8.dispose();
      cloth(var7, 0.9);
      trim(var7, var3);
      BufferedImage var9 = down(var7);
      grain(var9, 31);
      BufferedImage var10 = new BufferedImage(160, 256, 2);
      lining(var10, var3);
      cloth(var10, 0.7);
      trim(var10, var3);
      BufferedImage var11 = down(var10);
      grain(var11, 57);
      BufferedImage var12 = new BufferedImage(512, 256, 2);
      Graphics2D var13 = var12.createGraphics();
      var13.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      var13.setColor(new Color(var3.lining));
      var13.fillRect(0, 0, 512, 256);
      var13.drawImage(var9, 176, 0, 192, 176, null);
      var13.drawImage(var9, 192, 16, 80, 160, null);
      var13.drawImage(var9, 288, 16, 80, 160, null);
      var13.drawImage(var9, 8, 8, null);
      var13.drawImage(var11, 96, 8, null);
      edge(var13, 0, 8, 8, 128, var3, true);
      edge(var13, 88, 8, 8, 128, var3, true);
      edge(var13, 8, 0, 80, 8, var3, false);
      edge(var13, 88, 0, 80, 8, var3, false);
      var13.dispose();
      return new CTex.T(512, 256, var12.getRGB(0, 0, 512, 256, null, 0, 512));
   }

   private static void edge(Graphics2D var0, int var1, int var2, int var3, int var4, Capes.Spec var5, boolean var6) {
      if (var6) {
         var0.setPaint(new GradientPaint(var1, 0.0F, new Color(var5.trimHi), var1 + var3, 0.0F, new Color(var5.trimLo)));
      } else {
         var0.setPaint(new GradientPaint(0.0F, var2, new Color(var5.trimHi), 0.0F, var2 + var4, new Color(var5.trimLo)));
      }

      var0.fillRect(var1, var2, var3, var4);
      var0.setColor(new Color(255, 255, 255, 60));
      var0.setStroke(new BasicStroke(1.0F, 0, 0, 1.0F, new float[]{3.0F, 2.0F}, 0.0F));
      if (var6) {
         var0.drawLine(var1 + var3 / 2, var2 + 2, var1 + var3 / 2, var2 + var4 - 2);
      } else {
         var0.drawLine(var1 + 2, var2 + var4 / 2, var1 + var3 - 2, var2 + var4 / 2);
      }

      var0.setStroke(new BasicStroke(1.0F));
   }

   private static void lining(BufferedImage var0, Capes.Spec var1) {
      int[] var2 = px(var0);
      int var3 = var1.lining;
      int var4 = mix(var3, 16777215, 0.28);
      int var5 = mix(var3, 0, 0.4);

      for (int var6 = 0; var6 < 256; var6++) {
         for (int var7 = 0; var7 < 160; var7++) {
            double var8 = (var7 + 0.5) / 160.0;
            double var10 = (var6 + 0.5) / 256.0;
            int var12 = mix(var4, var5, var10 * 0.9);
            double var13 = Math.exp(-Math.pow((var8 * 0.8 + var10 * 0.6 - 0.55) / 0.12, 2.0)) * 0.2;
            var12 = add(var12, 16777215, var13);
            double var15 = Math.abs(((var8 * 5.0 + var10 * 3.1) % 1.0 + 1.0) % 1.0 - 0.5);
            double var17 = Math.abs(((var8 * 5.0 - var10 * 3.1) % 1.0 + 1.0) % 1.0 - 0.5);
            double var19 = Math.min(var15, var17);
            if (var19 < 0.045) {
               var12 = shade(var12, 0.78 + var19 * 4.5);
            } else {
               var12 = shade(var12, 1.04 - var19 * 0.2);
            }

            var2[var6 * 160 + var7] = 0xFF000000 | var12;
         }
      }
   }

   static void cloth(BufferedImage var0, double var1) {
      int[] var3 = px(var0);
      int var4 = var0.getWidth();
      int var5 = var0.getHeight();

      for (int var6 = 0; var6 < var5; var6++) {
         for (int var7 = 0; var7 < var4; var7++) {
            double var8 = (var7 + 0.5) / var4;
            double var10 = (var6 + 0.5) / var5;
            double var12 = 0.055 * Math.sin(var8 * Math.PI * 2.0 * 2.5 + 0.6 + var10 * 1.2) * (0.35 + 0.65 * var10)
               + 0.025 * Math.sin(var8 * Math.PI * 2.0 * 5.5 + 1.3);
            double var14 = Math.min(var8, 1.0 - var8);
            double var16 = Math.min(var10, 1.0 - var10);
            double var18 = 1.0 - 0.28 * Math.pow(1.0 - Math.min(1.0, var14 / 0.22), 2.0) - 0.22 * Math.pow(1.0 - Math.min(1.0, (1.0 - var10) / 0.25), 2.0);
            double var20 = 1.0 - 0.25 * Math.pow(1.0 - Math.min(1.0, var10 / 0.09), 2.0);
            double var22 = 1.0 + (var12 + (var18 * var20 - 1.0)) * var1;
            int var24 = var6 * var4 + var7;
            var3[var24] = var3[var24] & 0xFF000000 | shade(var3[var24], var22) & 16777215;
         }
      }
   }

   static void trim(BufferedImage var0, Capes.Spec var1) {
      Graphics2D var2 = var0.createGraphics();
      Gx.hints(var2);
      int var3 = var0.getWidth();
      int var4 = var0.getHeight();
      float var5 = 7.0F;

      for (int var6 = 0; var6 < 6; var6++) {
         var2.setColor(new Color(0, 0, 0, 22 - var6 * 3));
         var2.setStroke(new BasicStroke(2.0F));
         var2.drawRect((int)var5 + var6, (int)var5 + var6, var3 - 1 - 2 * ((int)var5 + var6), var4 - 1 - 2 * ((int)var5 + var6));
      }

      Float var8 = new Float(0.0F, 0.0F, var3, var4);
      Area var7 = new Area(var8);
      var7.subtract(new Area(new Float(var5, var5, var3 - 2.0F * var5, var4 - 2.0F * var5)));
      var2.setPaint(
         new LinearGradientPaint(
            0.0F,
            0.0F,
            var3 * 0.6F,
            var4,
            new float[]{0.0F, 0.3F, 0.55F, 0.8F, 1.0F},
            new Color[]{
               new Color(var1.trimHi),
               new Color(mix(var1.trimHi, var1.trimLo, 0.55)),
               new Color(var1.trimHi),
               new Color(var1.trimLo),
               new Color(mix(var1.trimHi, var1.trimLo, 0.4))
            }
         )
      );
      var2.fill(var7);
      var2.setColor(new Color(255, 255, 255, 110));
      var2.setStroke(new BasicStroke(1.2F));
      var2.draw(new Float(1.5F, 1.5F, var3 - 3, var4 - 3));
      var2.setColor(new Color(0, 0, 0, 120));
      var2.draw(new Float(var5 - 0.6F, var5 - 0.6F, var3 - 2.0F * var5 + 1.2F, var4 - 2.0F * var5 + 1.2F));
      var2.setColor(new Color(var1.trimHi & 16777215 | 1593835520, true));
      var2.setStroke(new BasicStroke(1.3F));
      var2.draw(new Float(var5 + 4.0F, var5 + 4.0F, var3 - 2.0F * var5 - 8.0F, var4 - 2.0F * var5 - 8.0F));
      var2.setPaint(new GradientPaint(0.0F, var5, new Color(0, 0, 0, 70), 0.0F, var5 + 16.0F, new Color(0, 0, 0, 0)));
      var2.fillRect((int)var5, (int)var5, var3 - 2 * (int)var5, 16);
      var2.dispose();
   }

   static BufferedImage down(BufferedImage var0) {
      int var1 = var0.getWidth() / 2;
      int var2 = var0.getHeight() / 2;
      int var3 = var0.getWidth();
      int[] var4 = px(var0);
      BufferedImage var5 = new BufferedImage(var1, var2, 2);
      int[] var6 = px(var5);

      for (int var7 = 0; var7 < var2; var7++) {
         for (int var8 = 0; var8 < var1; var8++) {
            int var9 = var7 * 2 * var3 + var8 * 2;
            int var10 = var4[var9];
            int var11 = var4[var9 + 1];
            int var12 = var4[var9 + var3];
            int var13 = var4[var9 + var3 + 1];
            int var14 = 0;
            int var15 = 0;
            int var16 = 0;
            int var17 = 0;

            for (int var21 : new int[]{var10, var11, var12, var13}) {
               var17 += var21 >>> 24;
               var14 += var21 >> 16 & 0xFF;
               var15 += var21 >> 8 & 0xFF;
               var16 += var21 & 0xFF;
            }

            var6[var7 * var1 + var8] = var17 / 4 << 24 | var14 / 4 << 16 | var15 / 4 << 8 | var16 / 4;
         }
      }

      return var5;
   }

   static void grain(BufferedImage var0, int var1) {
      int[] var2 = px(var0);
      int var3 = var0.getWidth();
      int var4 = var0.getHeight();

      for (int var5 = 0; var5 < var4; var5++) {
         for (int var6 = 0; var6 < var3; var6++) {
            double var7 = (hash(var6, var5, var1) - 0.5) * 0.05 + ((var6 + var5) % 3 == 0 ? -0.015 : 0.006);
            int var9 = var5 * var3 + var6;
            var2[var9] = var2[var9] & 0xFF000000 | shade(var2[var9], 1.0 + var7) & 16777215;
         }
      }
   }

   static int[] px(BufferedImage var0) {
      return ((DataBufferInt)var0.getRaster().getDataBuffer()).getData();
   }

   static void field(BufferedImage var0, Capes.PF var1) {
      int var2 = var0.getWidth();
      int var3 = var0.getHeight();
      int[] var4 = px(var0);

      for (int var5 = 0; var5 < var3; var5++) {
         for (int var6 = 0; var6 < var2; var6++) {
            var4[var5 * var2 + var6] = 0xFF000000 | var1.at((var6 + 0.5) / var2, (var5 + 0.5) / var3, var6, var5);
         }
      }
   }

   static double hash(int var0, int var1, int var2) {
      int var3 = var0 * 374761393 + var1 * 668265263 + var2 * 1442695041;
      var3 = (var3 ^ var3 >>> 13) * 1274126177;
      var3 ^= var3 >>> 16;
      return (var3 & 16777215) / 1.6777216E7;
   }

   static double hash1(int var0, int var1) {
      return hash(var0, var0 * 31 + 7, var1);
   }

   static double vn(double var0, double var2, int var4, int var5, int var6) {
      int var7 = (int)Math.floor(var0);
      int var8 = (int)Math.floor(var2);
      double var9 = var0 - var7;
      double var11 = var2 - var8;
      var9 = var9 * var9 * var9 * (var9 * (var9 * 6.0 - 15.0) + 10.0);
      var11 = var11 * var11 * var11 * (var11 * (var11 * 6.0 - 15.0) + 10.0);
      int var13 = var4 > 0 ? Math.floorMod(var7, var4) : var7;
      int var14 = var4 > 0 ? Math.floorMod(var7 + 1, var4) : var7 + 1;
      int var15 = var5 > 0 ? Math.floorMod(var8, var5) : var8;
      int var16 = var5 > 0 ? Math.floorMod(var8 + 1, var5) : var8 + 1;
      double var17 = hash(var13, var15, var6);
      double var19 = hash(var14, var15, var6);
      double var21 = hash(var13, var16, var6);
      double var23 = hash(var14, var16, var6);
      return var17 + (var19 - var17) * var9 + (var21 - var17) * var11 + (var17 - var19 - var21 + var23) * var9 * var11;
   }

   static double fbm(double var0, double var2, int var4, int var5, int var6, int var7) {
      double var8 = 0.0;
      double var10 = 0.5;
      double var12 = 0.0;
      double var14 = 1.0;

      for (int var16 = 0; var16 < var6; var16++) {
         var8 += var10 * vn(var0 * var14, var2 * var14, var4 * (int)var14, var5 * (int)var14, var7 + var16 * 17);
         var12 += var10;
         var10 *= 0.5;
         var14 *= 2.0;
      }

      return var8 / var12;
   }

   static double clamp01(double var0) {
      return var0 < 0.0 ? 0.0 : (var0 > 1.0 ? 1.0 : var0);
   }

   static double smooth(double var0, double var2, double var4) {
      double var6 = clamp01((var4 - var0) / (var2 - var0));
      return var6 * var6 * (3.0 - 2.0 * var6);
   }

   static int mix(int var0, int var1, double var2) {
      var2 = clamp01(var2);
      int var4 = var0 >> 16 & 0xFF;
      int var5 = var0 >> 8 & 0xFF;
      int var6 = var0 & 0xFF;
      int var7 = var1 >> 16 & 0xFF;
      int var8 = var1 >> 8 & 0xFF;
      int var9 = var1 & 0xFF;
      return (int)(var4 + (var7 - var4) * var2) << 16 | (int)(var5 + (var8 - var5) * var2) << 8 | (int)(var6 + (var9 - var6) * var2);
   }

   static int ramp(double var0, int... var2) {
      var0 = clamp01(var0) * (var2.length - 1);
      int var3 = Math.min(var2.length - 2, (int)var0);
      return mix(var2[var3], var2[var3 + 1], var0 - var3);
   }

   static int shade(int var0, double var1) {
      int var3 = (int)Math.min(255.0, Math.max(0.0, (var0 >> 16 & 0xFF) * var1));
      int var4 = (int)Math.min(255.0, Math.max(0.0, (var0 >> 8 & 0xFF) * var1));
      int var5 = (int)Math.min(255.0, Math.max(0.0, (var0 & 0xFF) * var1));
      return var0 & 0xFF000000 | var3 << 16 | var4 << 8 | var5;
   }

   static int add(int var0, int var1, double var2) {
      if (var2 <= 0.0) {
         return var0;
      } else {
         int var4 = (int)Math.min(255.0, (var0 >> 16 & 0xFF) + (var1 >> 16 & 0xFF) * var2);
         int var5 = (int)Math.min(255.0, (var0 >> 8 & 0xFF) + (var1 >> 8 & 0xFF) * var2);
         int var6 = (int)Math.min(255.0, (var0 & 0xFF) + (var1 & 0xFF) * var2);
         return var0 & 0xFF000000 | var4 << 16 | var5 << 8 | var6;
      }
   }

   static int over(int var0, int var1, double var2) {
      return var0 & 0xFF000000 | mix(var0, var1, var2);
   }

   static int hsv(double var0, double var2, double var4) {
      return Color.HSBtoRGB((float)(var0 - Math.floor(var0)), (float)clamp01(var2), (float)clamp01(var4)) & 16777215;
   }

   static Color col(int var0, double var1) {
      return new Color(var0 >> 16 & 0xFF, var0 >> 8 & 0xFF, var0 & 0xFF, (int)Math.round(clamp01(var1) * 255.0));
   }

   static void dot(Graphics2D var0, double var1, double var3, double var5, int var7, double var8) {
      if (!(var5 <= 0.05) && !(var8 <= 0.004)) {
         var0.setPaint(
            new RadialGradientPaint(
               (float)var1, (float)var3, (float)var5, new float[]{0.0F, 0.25F, 1.0F}, new Color[]{col(var7, var8), col(var7, var8 * 0.45), col(var7, 0.0)}
            )
         );
         var0.fill(new Double(var1 - var5, var3 - var5, var5 * 2.0, var5 * 2.0));
      }
   }

   static void star(Graphics2D var0, double var1, double var3, double var5, int var7, double var8) {
      if (!(var8 <= 0.01)) {
         dot(var0, var1, var3, var5 * 2.2, var7, var8 * 0.55);
         dot(var0, var1, var3, var5 * 0.9, 16777215, var8);
         if (var5 > 1.6) {
            var0.setStroke(new BasicStroke((float)Math.max(0.7, var5 * 0.22), 1, 1));
            var0.setPaint(new GradientPaint((float)(var1 - var5 * 3.2), (float)var3, col(var7, 0.0), (float)var1, (float)var3, col(16777215, var8 * 0.9), true));
            var0.draw(new java.awt.geom.Line2D.Double(var1 - var5 * 3.2, var3, var1 + var5 * 3.2, var3));
            var0.setPaint(new GradientPaint((float)var1, (float)(var3 - var5 * 3.2), col(var7, 0.0), (float)var1, (float)var3, col(16777215, var8 * 0.9), true));
            var0.draw(new java.awt.geom.Line2D.Double(var1, var3 - var5 * 3.2, var1, var3 + var5 * 3.2));
         }
      }
   }

   static void glowStroke(Graphics2D var0, Shape var1, int var2, float var3, float var4, double var5) {
      for (int var7 = 5; var7 >= 1; var7--) {
         var0.setColor(col(var2, var5 * 0.12 * (6 - var7) / 5.0 + 0.02));
         var0.setStroke(new BasicStroke(var3 + var4 * var7, 1, 1));
         var0.draw(var1);
      }

      var0.setColor(col(var2, var5));
      var0.setStroke(new BasicStroke(var3, 1, 1));
      var0.draw(var1);
      var0.setColor(col(mix(var2, 16777215, 0.7), var5));
      var0.setStroke(new BasicStroke(Math.max(0.6F, var3 * 0.4F), 1, 1));
      var0.draw(var1);
   }

   static void sweep(BufferedImage var0, double var1, double var3, double var5) {
      int[] var7 = px(var0);
      int var8 = var0.getWidth();
      int var9 = var0.getHeight();
      double var10 = -0.5 + var1 * 2.2;

      for (int var12 = 0; var12 < var9; var12++) {
         for (int var13 = 0; var13 < var8; var13++) {
            double var14 = (var13 + 0.5) / var8;
            double var16 = (var12 + 0.5) / var9;
            double var18 = var14 * 0.7 + var16 * 0.45 - var10;
            double var20 = Math.exp(-(var18 * var18) / (var3 * var3)) * var5;
            if (var20 > 0.004) {
               var7[var12 * var8 + var13] = add(var7[var12 * var8 + var13], 16777215, var20);
            }
         }
      }
   }

   static void addDot(BufferedImage var0, double var1, double var3, double var5, int var7, double var8) {
      if (!(var5 <= 0.1) && !(var8 <= 0.003)) {
         int[] var10 = px(var0);
         int var11 = var0.getWidth();
         int var12 = var0.getHeight();
         int var13 = Math.max(0, (int)(var1 - var5 * 2.0));
         int var14 = Math.min(var11 - 1, (int)(var1 + var5 * 2.0));
         int var15 = Math.max(0, (int)(var3 - var5 * 2.0));
         int var16 = Math.min(var12 - 1, (int)(var3 + var5 * 2.0));
         double var17 = 1.0 / (var5 * var5 * 0.5);

         for (int var19 = var15; var19 <= var16; var19++) {
            for (int var20 = var13; var20 <= var14; var20++) {
               double var21 = var20 + 0.5 - var1;
               double var23 = var19 + 0.5 - var3;
               double var25 = Math.exp(-(var21 * var21 + var23 * var23) * var17) * var8;
               if (var25 > 0.003) {
                  var10[var19 * var11 + var20] = add(var10[var19 * var11 + var20], var7, var25);
               }
            }
         }
      }
   }

   static {
      CapeArt.defineAll();
      CapeArt2.defineAll();
      Cos3Capes.defineAll();
      XmasCapes.defineAll();
   }

   interface PF {
      int at(double var1, double var3, int var5, int var6);
   }

   interface Painter {
      void paint(Graphics2D var1, BufferedImage var2, double var3, int var5, int var6);
   }

   static final class Spec {
      final String id;
      final int frames;
      final float fps;
      final int trimHi;
      final int trimLo;
      final int lining;
      final Capes.Painter p;

      Spec(String var1, int var2, float var3, int var4, int var5, int var6, Capes.Painter var7) {
         this.id = var1;
         this.frames = var2;
         this.fps = var3;
         this.trimHi = var4;
         this.trimLo = var5;
         this.lining = var6;
         this.p = var7;
      }
   }
}
