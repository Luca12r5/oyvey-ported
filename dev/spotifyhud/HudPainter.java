package dev.spotifyhud;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D.Double;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

public final class HudPainter {
   private static final Color PANEL_EDGE = new Color(255, 255, 255, 13);
   private static final Color PANEL_EDGE_HOVER = new Color(255, 255, 255, 60);
   static final Color TITLE = new Color(234, 236, 235);
   static final Color ARTIST = new Color(179, 179, 179);
   private static final Color TIME = new Color(167, 167, 167);
   private static final Color TRACK = new Color(62, 63, 63, 235);
   private static final Color FILL = new Color(247, 249, 247);
   private static final Color GREEN = Theme.GREEN;
   private static final Color ICON = new Color(248, 250, 250);
   private static final Color SPEAKER = new Color(255, 255, 255, 170);
   private static final Color HOVER_BG = new Color(255, 255, 255, 26);
   private static final Color ART_EMPTY = new Color(40, 40, 40);
   private static final Color NOTE = new Color(127, 127, 127);
   static final int LYRICS_H = 40;
   static final double R = 15.0;
   private static String family;
   private static String semiboldFamily;
   private static final Map<String, Font> FONT_CACHE = new HashMap<>();
   private static final HudPainter.Layers HUD = new HudPainter.Layers();
   private static final HudPainter.Layers LYR = new HudPainter.Layers();
   private static BufferedImage lastArtSrc;
   private static BufferedImage lastArtScaled;
   private static int lastArtSize;

   private HudPainter() {
   }

   private static void initFonts() {
      if (family == null) {
         HashSet var0 = new HashSet();

         try {
            var0.addAll(Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
         } catch (Throwable var6) {
         }

         String[] var1 = new String[]{"Segoe UI", "Helvetica Neue", "Arial", "Liberation Sans", "DejaVu Sans"};
         family = "SansSerif";

         for (String var5 : var1) {
            if (var0.contains(var5)) {
               family = var5;
               break;
            }
         }

         semiboldFamily = var0.contains("Segoe UI Semibold") ? "Segoe UI Semibold" : null;
      }
   }

   public static Font font(String var0, boolean var1, float var2) {
      initFonts();
      String var3;
      int var4;
      if (var1 && semiboldFamily != null) {
         var3 = semiboldFamily;
         var4 = 0;
      } else {
         var3 = family;
         var4 = var1 ? 1 : 0;
      }

      String var5 = var3 + var4 + var2;
      Font var6 = FONT_CACHE.computeIfAbsent(var5, var4x -> {
         HashMap var5x = new HashMap();
         var5x.put(TextAttribute.FAMILY, var3);
         var5x.put(TextAttribute.SIZE, var2);
         if (var4 == 1) {
            var5x.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
         }

         if (var1) {
            var5x.put(TextAttribute.TRACKING, 0.02F);
         }

         return new Font(var5x);
      });
      if (var0 != null && var6.canDisplayUpTo(var0) != -1) {
         HashMap var7 = new HashMap();
         var7.put(TextAttribute.FAMILY, "Dialog");
         var7.put(TextAttribute.SIZE, var2);
         if (var1) {
            var7.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
         }

         return new Font(var7);
      } else {
         return var6;
      }
   }

   public static Font uiFont(boolean var0, float var1) {
      initFonts();
      String var2 = "ui" + family + var0 + var1;
      return FONT_CACHE.computeIfAbsent(var2, var2x -> {
         HashMap var3 = new HashMap();
         var3.put(TextAttribute.FAMILY, var0 && semiboldFamily != null ? semiboldFamily : family);
         var3.put(TextAttribute.SIZE, var1);
         if (var0 && semiboldFamily == null) {
            var3.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
         }

         return new Font(var3);
      });
   }

   public static void hints(Graphics2D var0) {
      var0.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var0.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      var0.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      var0.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var0.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      var0.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      var0.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      var0.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
   }

   private static Graphics2D clear(BufferedImage var0) {
      Graphics2D var1 = var0.createGraphics();
      var1.setComposite(AlphaComposite.Clear);
      var1.fillRect(0, 0, var0.getWidth(), var0.getHeight());
      var1.setComposite(AlphaComposite.SrcOver);
      hints(var1);
      return var1;
   }

   private static BufferedImage ensure(BufferedImage var0, int var1, int var2) {
      return var0 != null && var0.getWidth() == var1 && var0.getHeight() == var2 ? var0 : new BufferedImage(var1, var2, 2);
   }

   private static int[] compose(
      HudPainter.Layers var0,
      int var1,
      int var2,
      double var3,
      HudPainter.Style var5,
      Object var6,
      boolean var7,
      double var8,
      String var10,
      Consumer<Graphics2D> var11
   ) {
      BufferedImage var12 = var6 instanceof BufferedImage ? (BufferedImage)var6 : null;
      double var13 = var1 / 207.0;
      String var15 = var5.theme + "|" + var1 + "x" + var2 + "|" + ("cover".equals(var5.theme) ? System.identityHashCode(var12) : 0);
      if (var0.bg == null || !var15.equals(var0.bgKey)) {
         var0.bg = ensure(var0.bg, var1, var2);
         Graphics2D var16 = clear(var0.bg);
         var16.scale(var13, var2 / var3);
         Theme.paint(var16, var5.theme, new Double(0.0, 0.0, 207.0, var3, 15.0, 15.0), 207.0, var3, var12, var13);
         var16.dispose();
         var0.bgKey = var15;
      }

      if (var0.fg == null || !var10.equals(var0.fgKey) || var0.fg.getWidth() != var1 || var0.fg.getHeight() != var2) {
         var0.fg = ensure(var0.fg, var1, var2);
         Graphics2D var21 = clear(var0.fg);
         var21.scale(var13, var2 / var3);
         var11.accept(var21);
         var21.dispose();
         var0.fgKey = var10;
      }

      var0.canvas = ensure(var0.canvas, var1, var2);
      Graphics2D var22 = clear(var0.canvas);
      var22.drawImage(var0.bg, 0, 0, null);
      if (var7 && Anim.active(var5.anim)) {
         Graphics2D var17 = (Graphics2D)var22.create();
         var17.scale(var13, var2 / var3);
         var17.clip(new Double(0.0, 0.0, 207.0, var3, 15.0, 15.0));

         try {
            Anim.draw(var17, var5.anim, var8, 207.0, var3, var5.intensity, Theme.accent(var5.theme, var12));
         } finally {
            var17.dispose();
         }
      }

      var22.drawImage(var0.fg, 0, 0, null);
      var22.dispose();
      return var0.canvas.getRGB(0, 0, var1, var2, null, 0, var1);
   }

   static int[] render(HudPainter.View var0, int var1, int var2, HudPainter.Style var3, double var4, String var6) {
      Color var7 = Theme.accent(var3.theme, var0.art instanceof BufferedImage ? (BufferedImage)var0.art : null);
      return compose(
         HUD, var1, var2, 70.0, var3, var0.art, true, var4, var6 + "|" + var3.theme + var7.getRGB(), var3x -> drawHud(var3x, var0, var1 / 207.0, var7)
      );
   }

   private static void drawHud(Graphics2D var0, HudPainter.View var1, double var2, Color var4) {
      PlayerState var5 = var1.st;
      var0.setColor(!var1.interactive || var1.hover != HudLayout.Region.PANEL && var1.hover != HudLayout.Region.SETTINGS ? PANEL_EDGE : PANEL_EDGE_HOVER);
      var0.setStroke(new BasicStroke((float)(1.0 / var2)));
      double var6 = 0.5 / var2;
      var0.draw(new Double(var6, var6, 207.0 - 2.0 * var6, 70.0 - 2.0 * var6, 15.0, 15.0));
      Double var8 = new Double(9.0, 9.0, 37.0, 37.0, 8.0, 8.0);
      if (var1.art instanceof BufferedImage var9) {
         int var10 = Math.max(1, (int)Math.round(37.0 * var2));
         BufferedImage var11 = roundedArt(var9, var10, 4.0 * var2);
         AffineTransform var12 = new AffineTransform();
         var12.translate(9.0, 9.0);
         var12.scale(37.0 / var11.getWidth(), 37.0 / var11.getHeight());
         var0.drawImage(var11, var12, null);
      } else {
         var0.setColor(ART_EMPTY);
         var0.fill(var8);
         drawNote(var0, 27.5, 27.5);
      }

      double var23 = 0.0;
      if (var1.showTime) {
         long var24 = var5.durationMs;
         long var13 = var1.progressPreview >= 0.0 ? (long)(var1.progressPreview * var24) : var5.currentProgress();
         String var15 = PlayerState.fmt(var13) + " / " + PlayerState.fmt(var24);
         var0.setFont(font(var15, false, 7.8F));
         var23 = var0.getFontMetrics().getStringBounds(var15, var0).getWidth();
         var0.setColor(TIME);
         var0.drawString(var15, (float)(197.0 - var23), 29.0F);
      }

      String var25 = var1.title == null ? "" : var1.title;
      var0.setFont(font(var25, true, 9.9F));
      var0.setColor(TITLE);
      double var26 = 145.0 - (var1.interactive ? 9 : 0);
      var0.drawString(ellipsize(var0, var25, var26), 53.0F, 18.2F);
      String var14 = var1.artist == null ? "" : var1.artist;
      var0.setFont(font(var14, false, 8.1F));
      var0.setColor(ARTIST);
      double var27 = 144.0 - (var23 > 0.0 ? var23 + 6.0 : 0.0);
      var0.drawString(ellipsize(var0, var14, var27), 53.0F, 29.0F);
      if (var1.interactive) {
         if (var1.hover == HudLayout.Region.SETTINGS) {
            var0.setColor(HOVER_BG);
            var0.fill(new java.awt.geom.Ellipse2D.Double(194.2, 3.1999999999999993, 10.0, 10.0));
         }

         var0.setColor(var1.hover == HudLayout.Region.SETTINGS ? ICON : new Color(255, 255, 255, 150));
         drawGear(var0, 199.2, 8.2, 3.1);
      }

      double var17 = var1.progressPreview >= 0.0 ? var1.progressPreview : (var5.durationMs > 0L ? (double)var5.currentProgress() / var5.durationMs : 0.0);
      boolean var19 = var1.interactive && (var1.hover == HudLayout.Region.PROGRESS || var1.progressPreview >= 0.0);
      bar(var0, 52.5, 36.6, 146.5, 3.4, var17, var19, var4);
      if (var1.showControls) {
         int var20 = var1.volumePreview >= 0 ? var1.volumePreview : var5.volume;
         boolean var21 = var1.interactive && (var1.hover == HudLayout.Region.VOLUME || var1.volumePreview >= 0);
         hoverBg(var0, var1, HudLayout.Region.SPEAKER, 14.0);
         drawSpeaker(var0, 14.0, 57.0, var20);
         bar(var0, 21.0, 55.4, 37.5, 3.2, var20 < 0 ? 0.0 : var20 / 100.0, var21, var4);
         hoverBg(var0, var1, HudLayout.Region.PREV, 88.8);
         hoverBg(var0, var1, HudLayout.Region.PLAY, 103.3);
         hoverBg(var0, var1, HudLayout.Region.NEXT, 118.0);
         hoverBg(var0, var1, HudLayout.Region.REPEAT, 167.2);
         hoverBg(var0, var1, HudLayout.Region.SHUFFLE, 181.3);
         hoverBg(var0, var1, HudLayout.Region.LIKE, 195.8);
         var0.setColor(ICON);
         drawPrev(var0, 88.8, 57.0);
         if (var5.playing) {
            drawPause(var0, 103.3, 57.0);
         } else {
            drawPlay(var0, 103.3, 57.0);
         }

         drawNext(var0, 118.0, 57.0);
         boolean var22 = !"off".equals(var5.repeat);
         var0.setColor(var22 ? var4 : ICON);
         drawRepeat(var0, 167.2, 57.0, "track".equals(var5.repeat));
         if (var22) {
            dot(var0, 167.2);
         }

         var0.setColor(var5.shuffle ? var4 : ICON);
         drawShuffle(var0, 181.3, 57.0);
         if (var5.shuffle) {
            dot(var0, 181.3);
         }

         var0.setColor(var5.liked ? var4 : ICON);
         drawHeart(var0, 195.8, 57.0);
      }
   }

   public static void drawGear(Graphics2D var0, double var1, double var3, double var5) {
      java.awt.geom.Path2D.Double var7 = new java.awt.geom.Path2D.Double();
      byte var8 = 8;

      for (int var9 = 0; var9 < var8 * 2; var9++) {
         double var10 = (Math.PI * 2) * var9 / (var8 * 2);
         double var12 = (Math.PI * 2) * (var9 + 1) / (var8 * 2);
         double var14 = var9 % 2 == 0 ? var5 : var5 * 0.76;
         double var16 = var1 + Math.cos(var10) * var14;
         double var18 = var3 + Math.sin(var10) * var14;
         double var20 = var1 + Math.cos(var12) * var14;
         double var22 = var3 + Math.sin(var12) * var14;
         if (var9 == 0) {
            var7.moveTo(var16, var18);
         } else {
            var7.lineTo(var16, var18);
         }

         var7.lineTo(var20, var22);
      }

      var7.closePath();
      Area var24 = new Area(var7);
      var24.subtract(new Area(new java.awt.geom.Ellipse2D.Double(var1 - var5 * 0.36, var3 - var5 * 0.36, var5 * 0.72, var5 * 0.72)));
      var0.fill(var24);
   }

   static int[] renderLyrics(HudPainter.LyricsView var0, int var1, int var2, HudPainter.Style var3, double var4, String var6) {
      Color var7 = Theme.accent(var3.theme, var0.art instanceof BufferedImage ? (BufferedImage)var0.art : null);
      return compose(
         LYR,
         var1,
         var2,
         40.0,
         var3,
         var0.art,
         var3.animOnLyrics,
         var4,
         var6 + "|" + var3.theme + var7.getRGB(),
         var4x -> drawLyrics(var4x, var0, var1 / 207.0, var3, var7)
      );
   }

   private static void drawLyrics(Graphics2D var0, HudPainter.LyricsView var1, double var2, HudPainter.Style var4, Color var5) {
      var0.setColor(var1.interactive && var1.hover ? PANEL_EDGE_HOVER : PANEL_EDGE);
      var0.setStroke(new BasicStroke((float)(1.0 / var2)));
      double var6 = 0.5 / var2;
      var0.draw(new Double(var6, var6, 207.0 - 2.0 * var6, 40.0 - 2.0 * var6, 15.0, 15.0));
      Shape var8 = var0.getClip();
      var0.clip(new java.awt.geom.Rectangle2D.Double(3.0, 1.5, 201.0, 37.0));
      double var9 = 187.0;
      if (var1.message != null) {
         var0.setFont(font(var1.message, false, 8.2F));
         var0.setColor(ARTIST);
         centered(var0, ellipsize(var0, var1.message, var9), 23.5);
         var0.setClip(var8);
      } else {
         double var11 = Math.max(0.0, Math.min(1.0, var1.anim));
         double var13 = 1.0 - Math.pow(1.0 - var11, 3.0);
         double var15 = (1.0 - var13) * 12.0;
         String[] var17 = var1.words.length > 0 ? var1.words : new String[]{var1.curText.isEmpty() ? "♪" : var1.curText};
         String var18 = String.join(" ", var17);
         float var19 = 9.6F;
         var0.setFont(font(var18, true, var19));

         while (var19 > 7.4F && width(var0, var17) > var9) {
            var19 -= 0.3F;
            var0.setFont(font(var18, true, var19));
         }

         int var20 = var17.length;
         if (width(var0, var17) > var9 && var17.length > 1) {
            var20 = splitPoint(var0, var17);
         }

         boolean var21 = var20 < var17.length;
         if (!var21) {
            drawLine(var0, var1.prev, false, 11.0 + var15, 0.42 * var13, var9);
         }

         double[] var22 = var21 ? new double[]{19.2 + var15, 29.0 + var15} : new double[]{24.5 + var15};
         int[][] var23 = var21 ? new int[][]{{0, var20}, {var20, var17.length}} : new int[][]{{0, var17.length}};
         float var24 = (float)(0.55 + 0.45 * var13);

         for (int var25 = 0; var25 < var23.length; var25++) {
            drawKaraokeRow(var0, var17, var23[var25][0], var23[var25][1], var22[var25], var1, var4, var5, var24, var19);
         }

         drawLine(var0, var1.next, false, (var21 ? 37.6 : 35.5) + var15, 0.55 * var13, var9);
         var0.setClip(var8);
      }
   }

   private static double width(Graphics2D var0, String[] var1) {
      FontMetrics var2 = var0.getFontMetrics();
      double var3 = 0.0;
      double var5 = var2.getStringBounds(" ", var0).getWidth();

      for (int var7 = 0; var7 < var1.length; var7++) {
         var3 += var2.getStringBounds(var1[var7], var0).getWidth() + (var7 > 0 ? var5 : 0.0);
      }

      return var3;
   }

   private static int splitPoint(Graphics2D var0, String[] var1) {
      double var2 = width(var0, var1);
      double var4 = 0.0;
      double var6 = var0.getFontMetrics().getStringBounds(" ", var0).getWidth();

      for (int var8 = 0; var8 < var1.length; var8++) {
         var4 += var0.getFontMetrics().getStringBounds(var1[var8], var0).getWidth() + var6;
         if (var4 >= var2 / 2.0) {
            return Math.max(1, Math.min(var1.length - 1, var8 + 1));
         }
      }

      return var1.length / 2;
   }

   private static void drawKaraokeRow(
      Graphics2D var0, String[] var1, int var2, int var3, double var4, HudPainter.LyricsView var6, HudPainter.Style var7, Color var8, float var9, float var10
   ) {
      FontMetrics var11 = var0.getFontMetrics();
      double var12 = var11.getStringBounds(" ", var0).getWidth();
      double var14 = var7.grow ? 1.0 + 0.18 * easeOut(var6.grow) : 1.0;
      double[] var16 = new double[var3 - var2];
      double[] var17 = new double[var3 - var2];
      double var18 = 0.0;

      for (int var20 = var2; var20 < var3; var20++) {
         var16[var20 - var2] = var11.getStringBounds(var1[var20], var0).getWidth();
         var17[var20 - var2] = var16[var20 - var2] * (var7.karaoke && var20 == var6.wordIdx ? var14 : 1.0);
         var18 += var17[var20 - var2] + (var20 > var2 ? var12 : 0.0);
      }

      double var48 = 187.0;
      double var22 = (207.0 - Math.min(var18, var48)) / 2.0;
      double var24 = 4.5;
      double var26 = var10 * 1.45;
      Color var28 = new Color(var8.getRed(), var8.getGreen(), var8.getBlue(), (int)(38.0F * var9));
      var0.setColor(var28);
      var0.fill(new Double(var22 - var24, var4 - var10 * 1.02, Math.min(var18, var48) + 2.0 * var24, var26, var26, var26));

      for (int var29 = var2; var29 < var3; var29++) {
         String var30 = var1[var29];
         double var31 = var16[var29 - var2];
         double var33 = var22 + (var17[var29 - var2] - var31) / 2.0;
         boolean var36 = var7.karaoke && var29 == var6.wordIdx;
         Color var35;
         if (!var7.karaoke) {
            var35 = TITLE;
         } else if (var29 < var6.wordIdx) {
            var35 = TITLE;
         } else if (var36) {
            var35 = var8;
         } else {
            var35 = new Color(255, 255, 255, 120);
         }

         if (!var36) {
            var0.setColor(new Color(var35.getRed(), var35.getGreen(), var35.getBlue(), (int)(var35.getAlpha() * var9)));
            var0.drawString(var30, (float)var33, (float)var4);
         } else {
            AffineTransform var39 = var0.getTransform();
            double var40 = var33 + var31 / 2.0;
            double var42 = var4 - var10 * 0.35;
            var0.translate(var40, var42);
            var0.scale(var14, var14);
            var0.translate(-var40, -var42);
            var0.setColor(new Color(var35.getRed(), var35.getGreen(), var35.getBlue(), (int)(70.0F * var9)));

            for (double[] var47 : new double[][]{{-0.35, 0.0}, {0.35, 0.0}, {0.0, -0.35}, {0.0, 0.35}}) {
               var0.drawString(var30, (float)(var33 + var47[0]), (float)(var4 + var47[1]));
            }

            var0.setColor(new Color(var35.getRed(), var35.getGreen(), var35.getBlue(), (int)(255.0F * var9)));
            var0.drawString(var30, (float)var33, (float)var4);
            var0.setTransform(var39);
         }

         var22 += var17[var29 - var2] + var12;
      }
   }

   private static double easeOut(double var0) {
      var0 = Math.max(0.0, Math.min(1.0, var0));
      return 1.0 - Math.pow(1.0 - var0, 3.0);
   }

   private static void drawLine(Graphics2D var0, String var1, boolean var2, double var3, double var5, double var7) {
      if (var1 != null && !var1.isEmpty() && !(var5 <= 0.01)) {
         float var9 = var2 ? 9.6F : 7.6F;
         var0.setFont(font(var1, var2, var9));
         Color var10 = var2 ? TITLE : ARTIST;
         var0.setColor(new Color(var10.getRed(), var10.getGreen(), var10.getBlue(), (int)Math.round(255.0 * Math.min(1.0, var5))));
         centered(var0, ellipsize(var0, var1, var7), var3);
      }
   }

   private static void centered(Graphics2D var0, String var1, double var2) {
      double var4 = var0.getFontMetrics().getStringBounds(var1, var0).getWidth();
      var0.drawString(var1, (float)((207.0 - var4) / 2.0), (float)var2);
   }

   private static BufferedImage roundedArt(BufferedImage var0, int var1, double var2) {
      if (var0 == lastArtSrc && var1 == lastArtSize && lastArtScaled != null) {
         return lastArtScaled;
      } else {
         BufferedImage var4 = scaledArt(var0, var1);
         BufferedImage var5 = new BufferedImage(var1, var1, 2);
         Graphics2D var6 = var5.createGraphics();
         var6.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var6.setColor(Color.WHITE);
         var6.fill(new Double(0.0, 0.0, var1, var1, var2 * 2.0, var2 * 2.0));
         var6.setComposite(AlphaComposite.SrcIn);
         var6.drawImage(var4, 0, 0, null);
         var6.dispose();
         lastArtSrc = var0;
         lastArtSize = var1;
         lastArtScaled = var5;
         return var5;
      }
   }

   private static BufferedImage scaledArt(BufferedImage var0, int var1) {
      BufferedImage var2 = var0;
      int var3 = var0.getWidth();

      int var4;
      for (var4 = var0.getHeight(); var3 / 2 >= var1 && var4 / 2 >= var1; var2 = resize(var2, var3, var4)) {
         var3 /= 2;
         var4 /= 2;
      }

      if (var3 != var1 || var4 != var1) {
         var2 = resize(var2, var1, var1);
      }

      return var2;
   }

   private static BufferedImage resize(BufferedImage var0, int var1, int var2) {
      BufferedImage var3 = new BufferedImage(var1, var2, 2);
      Graphics2D var4 = var3.createGraphics();
      var4.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      var4.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      var4.drawImage(var0, 0, 0, var1, var2, null);
      var4.dispose();
      return var3;
   }

   private static String ellipsize(Graphics2D var0, String var1, double var2) {
      FontMetrics var4 = var0.getFontMetrics();
      if (var4.getStringBounds(var1, var0).getWidth() <= var2) {
         return var1;
      } else {
         String var5 = "…";
         int var6 = 0;
         int var7 = var1.length();

         while (var6 < var7) {
            int var8 = (var6 + var7 + 1) / 2;
            if (var4.getStringBounds(var1.substring(0, var8).trim() + var5, var0).getWidth() <= var2) {
               var6 = var8;
            } else {
               var7 = var8 - 1;
            }
         }

         return var1.substring(0, var6).trim() + var5;
      }
   }

   private static void bar(Graphics2D var0, double var1, double var3, double var5, double var7, double var9, boolean var11, Color var12) {
      var9 = Math.max(0.0, Math.min(1.0, var9));
      var0.setColor(TRACK);
      var0.fill(new Double(var1, var3, var5, var7, var7, var7));
      double var13 = var5 * var9;
      if (var13 >= 0.35) {
         var0.setColor(var11 ? var12 : FILL);
         double var15 = Math.min(var7, var13);
         var0.fill(new Double(var1, var3, var13, var7, var15, var7));
      }

      if (var11) {
         double var22 = var1 + var5 * var9;
         double var17 = var3 + var7 / 2.0;
         double var19 = 3.0;
         var0.setColor(new Color(0, 0, 0, 70));
         var0.fill(new java.awt.geom.Ellipse2D.Double(var22 - var19 - 0.2, var17 - var19 + 0.4, 2.0 * var19 + 0.4, 2.0 * var19 + 0.4));
         var0.setColor(Color.WHITE);
         var0.fill(new java.awt.geom.Ellipse2D.Double(var22 - var19, var17 - var19, 2.0 * var19, 2.0 * var19));
      }
   }

   private static void hoverBg(Graphics2D var0, HudPainter.View var1, HudLayout.Region var2, double var3) {
      if (var1.interactive && var1.hover == var2) {
         var0.setColor(HOVER_BG);
         var0.fill(new java.awt.geom.Ellipse2D.Double(var3 - 5.6, 51.4, 11.2, 11.2));
      }
   }

   private static void dot(Graphics2D var0, double var1) {
      var0.fill(new java.awt.geom.Ellipse2D.Double(var1 - 0.65, 61.1, 1.3, 1.3));
   }

   private static void drawPause(Graphics2D var0, double var1, double var3) {
      double var5 = 4.8;
      double var7 = 1.35;
      double var9 = 1.35;
      var0.fill(new Double(var1 - var9 / 2.0 - var7, var3 - var5 / 2.0, var7, var5, 0.5, 0.5));
      var0.fill(new Double(var1 + var9 / 2.0, var3 - var5 / 2.0, var7, var5, 0.5, 0.5));
   }

   private static void drawPlay(Graphics2D var0, double var1, double var3) {
      java.awt.geom.Path2D.Double var5 = new java.awt.geom.Path2D.Double();
      double var6 = 5.2;
      double var8 = 4.6;
      var5.moveTo(var1 - var8 / 2.0 + 0.4, var3 - var6 / 2.0);
      var5.lineTo(var1 + var8 / 2.0 + 0.4, var3);
      var5.lineTo(var1 - var8 / 2.0 + 0.4, var3 + var6 / 2.0);
      var5.closePath();
      var0.fill(var5);
   }

   private static void drawPrev(Graphics2D var0, double var1, double var3) {
      double var5 = 4.6;
      var0.fill(new Double(var1 - 2.9, var3 - var5 / 2.0, 1.0, var5, 0.4, 0.4));
      java.awt.geom.Path2D.Double var7 = new java.awt.geom.Path2D.Double();
      var7.moveTo(var1 + 2.7, var3 - var5 / 2.0);
      var7.lineTo(var1 - 1.7, var3);
      var7.lineTo(var1 + 2.7, var3 + var5 / 2.0);
      var7.closePath();
      var0.fill(var7);
   }

   private static void drawNext(Graphics2D var0, double var1, double var3) {
      double var5 = 4.6;
      var0.fill(new Double(var1 + 1.9, var3 - var5 / 2.0, 1.0, var5, 0.4, 0.4));
      java.awt.geom.Path2D.Double var7 = new java.awt.geom.Path2D.Double();
      var7.moveTo(var1 - 2.7, var3 - var5 / 2.0);
      var7.lineTo(var1 + 1.7, var3);
      var7.lineTo(var1 - 2.7, var3 + var5 / 2.0);
      var7.closePath();
      var0.fill(var7);
   }

   private static void drawRepeat(Graphics2D var0, double var1, double var3, boolean var5) {
      double var6 = 5.4;
      double var8 = 4.0;
      var0.setStroke(new BasicStroke(0.95F, 1, 1));
      java.awt.geom.Path2D.Double var10 = new java.awt.geom.Path2D.Double();
      double var11 = var1 - var6 / 2.0;
      double var13 = var1 + var6 / 2.0;
      double var15 = var3 - var8 / 2.0;
      double var17 = var3 + var8 / 2.0;
      double var19 = 1.0;
      var10.moveTo(var13 - 1.9, var15);
      var10.lineTo(var11 + var19, var15);
      var10.quadTo(var11, var15, var11, var15 + var19);
      var10.lineTo(var11, var17 - var19);
      var10.quadTo(var11, var17, var11 + var19, var17);
      var10.lineTo(var13 - var19, var17);
      var10.quadTo(var13, var17, var13, var17 - var19);
      var10.lineTo(var13, var15 + var19 + 0.3);
      var0.draw(var10);
      java.awt.geom.Path2D.Double var21 = new java.awt.geom.Path2D.Double();
      var21.moveTo(var13 - 1.9, var15 - 1.35);
      var21.lineTo(var13 - 0.2, var15);
      var21.lineTo(var13 - 1.9, var15 + 1.35);
      var21.closePath();
      var0.fill(var21);
      if (var5) {
         Font var22 = new Font("SansSerif", 1, 1).deriveFont(3.2F);
         var0.setFont(var22);
         FontMetrics var23 = var0.getFontMetrics();
         String var24 = "1";
         double var25 = var23.getStringBounds(var24, var0).getWidth();
         var0.drawString(var24, (float)(var1 - var25 / 2.0), (float)(var3 + 1.15));
      }
   }

   private static void drawShuffle(Graphics2D var0, double var1, double var3) {
      var0.setStroke(new BasicStroke(0.9F, 1, 1));
      double var5 = var1 - 2.9;
      double var7 = var1 + 2.1;
      double var9 = var3 - 1.7;
      double var11 = var3 + 1.7;
      java.awt.geom.Path2D.Double var13 = new java.awt.geom.Path2D.Double();
      var13.moveTo(var5, var9);
      var13.curveTo(var1 - 0.8, var9, var1 - 0.4, var11, var7, var11);
      var0.draw(var13);
      java.awt.geom.Path2D.Double var14 = new java.awt.geom.Path2D.Double();
      var14.moveTo(var5, var11);
      var14.curveTo(var1 - 0.8, var11, var1 - 0.4, var9, var7, var9);
      var0.draw(var14);
      arrowHead(var0, var7 + 0.9, var9);
      arrowHead(var0, var7 + 0.9, var11);
   }

   private static void arrowHead(Graphics2D var0, double var1, double var3) {
      java.awt.geom.Path2D.Double var5 = new java.awt.geom.Path2D.Double();
      var5.moveTo(var1 - 1.6, var3 - 1.25);
      var5.lineTo(var1, var3);
      var5.lineTo(var1 - 1.6, var3 + 1.25);
      var5.closePath();
      var0.fill(var5);
   }

   private static void drawHeart(Graphics2D var0, double var1, double var3) {
      double var5 = 5.4;
      double var7 = 4.7;
      double var9 = var1 - var5 / 2.0;
      double var11 = var3 - var7 / 2.0 + 0.1;
      java.awt.geom.Path2D.Double var13 = new java.awt.geom.Path2D.Double();
      var13.moveTo(var1, var11 + var7);
      var13.curveTo(var9 - 0.2, var11 + var7 * 0.55, var9 - 0.1, var11 + 0.1, var9 + var5 * 0.27, var11);
      var13.curveTo(var9 + var5 * 0.42, var11 - 0.05, var1 - 0.05, var11 + 0.6, var1, var11 + 1.1);
      var13.curveTo(var1 + 0.05, var11 + 0.6, var9 + var5 * 0.58, var11 - 0.05, var9 + var5 * 0.73, var11);
      var13.curveTo(var9 + var5 + 0.1, var11 + 0.1, var9 + var5 + 0.2, var11 + var7 * 0.55, var1, var11 + var7);
      var13.closePath();
      var0.fill(var13);
   }

   private static void drawSpeaker(Graphics2D var0, double var1, double var3, int var5) {
      var0.setColor(SPEAKER);
      java.awt.geom.Path2D.Double var6 = new java.awt.geom.Path2D.Double();
      double var7 = var1 - 3.0;
      var6.moveTo(var7, var3 - 1.1);
      var6.lineTo(var7 + 1.3, var3 - 1.1);
      var6.lineTo(var7 + 3.0, var3 - 2.6);
      var6.lineTo(var7 + 3.0, var3 + 2.6);
      var6.lineTo(var7 + 1.3, var3 + 1.1);
      var6.lineTo(var7, var3 + 1.1);
      var6.closePath();
      var0.fill(var6);
      var0.setStroke(new BasicStroke(0.75F, 1, 1));
      if (var5 == 0) {
         double var9 = var1 + 1.7;
         var0.draw(new java.awt.geom.Line2D.Double(var9 - 0.1, var3 - 1.2, var9 + 2.2, var3 + 1.2));
         var0.draw(new java.awt.geom.Line2D.Double(var9 - 0.1, var3 + 1.2, var9 + 2.2, var3 - 1.2));
      } else {
         var0.draw(new java.awt.geom.Arc2D.Double(var1 - 1.4, var3 - 1.6, 3.2, 3.2, -55.0, 110.0, 0));
         if (var5 > 50 || var5 < 0) {
            var0.draw(new java.awt.geom.Arc2D.Double(var1 - 2.1, var3 - 2.8, 5.6, 5.6, -55.0, 110.0, 0));
         }
      }
   }

   private static void drawNote(Graphics2D var0, double var1, double var3) {
      var0.setColor(NOTE);
      var0.fill(new java.awt.geom.Ellipse2D.Double(var1 - 5.5, var3 + 2.2, 4.4, 3.4));
      var0.fill(new java.awt.geom.Ellipse2D.Double(var1 + 1.6, var3 + 0.8, 4.4, 3.4));
      var0.setStroke(new BasicStroke(1.1F));
      var0.draw(new java.awt.geom.Line2D.Double(var1 - 1.6, var3 + 3.8, var1 - 1.6, var3 - 5.0));
      var0.draw(new java.awt.geom.Line2D.Double(var1 + 5.5, var3 + 2.4, var1 + 5.5, var3 - 6.4));
      java.awt.geom.Path2D.Double var5 = new java.awt.geom.Path2D.Double();
      var5.moveTo(var1 - 1.6 - 0.55, var3 - 5.0);
      var5.lineTo(var1 + 5.5 + 0.55, var3 - 6.4);
      var5.lineTo(var1 + 5.5 + 0.55, var3 - 4.6);
      var5.lineTo(var1 - 1.6 - 0.55, var3 - 3.2);
      var5.closePath();
      var0.fill(var5);
   }

   private static final class Layers {
      BufferedImage canvas;
      BufferedImage bg;
      BufferedImage fg;
      String bgKey = "";
      String fgKey = "";
   }

   static final class LyricsView {
      String prev = "";
      String next = "";
      String[] words = new String[0];
      String curText = "";
      int wordIdx = -1;
      double grow = 1.0;
      double anim = 1.0;
      String message;
      boolean interactive;
      boolean hover;
      Object art;
   }

   static final class Style {
      String theme = "standard";
      String anim = "none";
      double intensity = 0.7;
      boolean animOnLyrics = true;
      boolean karaoke = true;
      boolean grow = true;
   }

   static final class View {
      PlayerState st;
      Object art;
      String title;
      String artist;
      boolean showTime;
      boolean showControls;
      HudLayout.Region hover;
      boolean interactive;
      double progressPreview = -1.0;
      int volumePreview = -1;
   }
}
