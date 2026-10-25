package de.bauloo.styles;

public final class StylesTheme {
   public static final StylesTheme.Preset[] PRESETS = new StylesTheme.Preset[]{
      new StylesTheme.Preset("Glass", 7722239, StylesTheme.Background.GLASS),
      new StylesTheme.Preset("Halloween", 16747039, StylesTheme.Background.HALLOWEEN),
      new StylesTheme.Preset("Crimson", 16734830, StylesTheme.Background.TINTED),
      new StylesTheme.Preset("Amethyst", 11766015, StylesTheme.Background.TINTED),
      new StylesTheme.Preset("Amber", 16758087, StylesTheme.Background.TINTED),
      new StylesTheme.Preset("Sakura", 16751823, StylesTheme.Background.TINTED),
      new StylesTheme.Preset("Mono", 15263978, StylesTheme.Background.BLACK)
   };
   public static final int[] SWATCHES = new int[]{7722239, 5213439, 11766015, 16751823, 16734830, 16758087, 16770140, 15263978};
   public final int backdrop;
   public final int frame;
   public final int header;
   public final int rail;
   public final int footer;
   public final int base;
   public final int surface;
   public final int surface2;
   public final int surfaceHover;
   public final int border;
   public final int borderDim;
   public final int borderStrong;
   public final int accent;
   public final int accentBright;
   public final int accentDark;
   public final int accentTint;
   public final int onAccent;
   public final int text;
   public final int textSoft;
   public final int textDim;
   public final int good = -9183056;
   public final int onGood = -16378866;
   public final int gold = -12454;
   public final boolean spooky;

   private StylesTheme(int accentRgb, StylesTheme.Background background, int opacityPercent) {
      this.spooky = background == StylesTheme.Background.HALLOWEEN;
      int accentColor = accentRgb & 16777215;
      int bg0;
      int frameRgb;
      int headerRgb;
      int railRgb;
      int footerRgb;
      int surfaceRgb;
      int surface2Rgb;
      int lineRgb;
      int lineDimRgb;
      int textRgb;
      int softRgb;
      int dimRgb;
      switch (background) {
         case NAVY:
         case GLASS:
            bg0 = 723984;
            frameRgb = 1052950;
            headerRgb = 1052950;
            railRgb = 1447708;
            footerRgb = 1052950;
            surfaceRgb = 1776674;
            surface2Rgb = 2434605;
            lineRgb = 3421759;
            lineDimRgb = 2303019;
            textRgb = 16053751;
            softRgb = 12370122;
            dimRgb = 8159372;
            break;
         case TINTED:
         default:
            float[] hsv = toHsv(accentColor);
            float h = hsv[0];
            float s = Math.min(1.0F, hsv[1] * 1.1F);
            bg0 = hsv(h, 0.45F * s, 0.055F);
            frameRgb = hsv(h, 0.4F * s, 0.085F);
            headerRgb = hsv(h, 0.38F * s, 0.11F);
            railRgb = hsv(h, 0.4F * s, 0.07F);
            footerRgb = hsv(h, 0.4F * s, 0.065F);
            surfaceRgb = hsv(h, 0.36F * s, 0.1F);
            surface2Rgb = hsv(h, 0.34F * s, 0.14F);
            lineRgb = hsv(h, 0.38F * s, 0.27F);
            lineDimRgb = hsv(h, 0.36F * s, 0.17F);
            textRgb = hsv(h, 0.05F * s, 0.98F);
            softRgb = hsv(h, 0.16F * s, 0.78F);
            dimRgb = hsv(h, 0.22F * s, 0.54F);
            break;
         case BLACK:
            bg0 = 526346;
            frameRgb = 986898;
            headerRgb = 1381656;
            railRgb = 789518;
            footerRgb = 723725;
            surfaceRgb = 1250070;
            surface2Rgb = 1710623;
            lineRgb = 3355450;
            lineDimRgb = 2236968;
            textRgb = 15790323;
            softRgb = 11908541;
            dimRgb = 7631998;
            break;
         case HALLOWEEN:
            bg0 = 722448;
            frameRgb = 1248023;
            headerRgb = 1707806;
            railRgb = 1116693;
            footerRgb = 985363;
            surfaceRgb = 1708064;
            surface2Rgb = 2299178;
            lineRgb = 3875904;
            lineDimRgb = 2496299;
            textRgb = 16774118;
            softRgb = 14073807;
            dimRgb = 9797010;
      }

      int alpha = Math.max(60, Math.min(100, opacityPercent)) * 255 / 100;
      alpha = alpha * 82 / 100;
      this.backdrop = argb(80, bg0);
      this.frame = argb(alpha, frameRgb);
      this.header = argb(alpha, headerRgb);
      this.rail = argb(alpha, railRgb);
      this.footer = argb(alpha, footerRgb);
      this.base = argb(255, bg0);
      this.surface = argb(255, surfaceRgb);
      this.surface2 = argb(255, surface2Rgb);
      this.surfaceHover = argb(255, mix(surfaceRgb, textRgb, 0.06F));
      this.border = argb(255, lineRgb);
      this.borderDim = argb(255, lineDimRgb);
      this.borderStrong = argb(255, mix(lineRgb, accentColor, 0.25F));
      this.accent = argb(255, accentColor);
      this.accentBright = argb(255, mix(accentColor, 16777215, 0.45F));
      this.accentDark = argb(255, mix(accentColor, bg0, 0.55F));
      this.accentTint = argb(46, accentColor);
      this.onAccent = argb(255, mix(bg0, 0, 0.3F));
      this.text = argb(255, textRgb);
      this.textSoft = argb(255, softRgb);
      this.textDim = argb(255, dimRgb);
   }

   public static StylesTheme of(int accentRgb, StylesTheme.Background background, int opacityPercent) {
      return new StylesTheme(accentRgb, background, opacityPercent);
   }

   public static int argb(int alpha, int rgb) {
      return (alpha & 0xFF) << 24 | rgb & 16777215;
   }

   public static int withAlpha(int color, int alpha) {
      return argb(alpha, color);
   }

   public static int mix(int a, int b, float t) {
      int r = Math.round((a >> 16 & 0xFF) * (1.0F - t) + (b >> 16 & 0xFF) * t);
      int g = Math.round((a >> 8 & 0xFF) * (1.0F - t) + (b >> 8 & 0xFF) * t);
      int bl = Math.round((a & 0xFF) * (1.0F - t) + (b & 0xFF) * t);
      return r << 16 | g << 8 | bl;
   }

   public static int hsv(float h, float s, float v) {
      float hue = (h % 1.0F + 1.0F) % 1.0F * 6.0F;
      int sector = (int)Math.floor(hue);
      float f = hue - sector;
      float p = v * (1.0F - s);
      float q = v * (1.0F - s * f);
      float t = v * (1.0F - s * (1.0F - f));
      float r;
      float g;
      float b;
      switch (sector % 6) {
         case 0:
            r = v;
            g = t;
            b = p;
            break;
         case 1:
            r = q;
            g = v;
            b = p;
            break;
         case 2:
            r = p;
            g = v;
            b = t;
            break;
         case 3:
            r = p;
            g = q;
            b = v;
            break;
         case 4:
            r = t;
            g = p;
            b = v;
            break;
         default:
            r = v;
            g = p;
            b = q;
      }

      return Math.round(r * 255.0F) << 16 | Math.round(g * 255.0F) << 8 | Math.round(b * 255.0F);
   }

   public static float[] toHsv(int rgb) {
      float r = (rgb >> 16 & 0xFF) / 255.0F;
      float g = (rgb >> 8 & 0xFF) / 255.0F;
      float b = (rgb & 0xFF) / 255.0F;
      float max = Math.max(r, Math.max(g, b));
      float min = Math.min(r, Math.min(g, b));
      float d = max - min;
      float h = 0.0F;
      if (d > 0.0F) {
         if (max == r) {
            h = (g - b) / d % 6.0F;
         } else if (max == g) {
            h = (b - r) / d + 2.0F;
         } else {
            h = (r - g) / d + 4.0F;
         }

         h /= 6.0F;
         if (h < 0.0F) {
            h++;
         }
      }

      return new float[]{h, max == 0.0F ? 0.0F : d / max, max};
   }

   public static enum Background {
      NAVY,
      TINTED,
      BLACK,
      GLASS,
      HALLOWEEN;
   }

   public record Preset(String name, int accent, StylesTheme.Background background) {
   }
}
