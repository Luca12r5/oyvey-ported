package dev.lego.ui;

import dev.lego.core.Modules;

public final class Style {
   public static final String[] THEMES = new String[]{"winter", "standard", "midnight", "glass", "purple", "ocean", "sunset", "christmas", "light"};
   public static final String[] THEME_NAMES = new String[]{"Winterzauber", "Standard", "Mitternacht", "Glas", "Lila", "Ozean", "Sunset", "Weihnachten", "Hell"};
   public static int bg;
   public static int bg2;
   public static int surface;
   public static int surface2;
   public static int surfaceHover;
   public static int stroke;
   public static int strokeHi;
   public static int text;
   public static int sub;
   public static int muted;
   public static int accent;
   public static int accentText;
   public static int shadow;
   public static int danger;
   public static int ok;
   public static boolean light;
   public static int gold = -736942;

   private Style() {
   }

   public static int[] palette(String var0) {
      String var1 = Modules.theme;
      Modules.theme = var0;
      update();
      int[] var2 = new int[]{bg, surface, surface2, text, sub, surfaceHover};
      Modules.theme = var1;
      update();
      return var2;
   }

   public static void update() {
      accent = Modules.accentArgb();
      String var0 = Modules.theme;
      light = "light".equals(var0);
      switch (var0) {
         case "winter":
            bg = -267709400;
            bg2 = -267379659;
            surface = -401333695;
            surface2 = -333829296;
            surfaceHover = -266193312;
            break;
         case "midnight":
            bg = -234156000;
            bg2 = -233826256;
            surface = -15459018;
            surface2 = -15063740;
            surfaceHover = -14602670;
            break;
         case "glass":
            bg = -1206643172;
            bg2 = -1206248156;
            surface = 587202559;
            surface2 = 872415231;
            surfaceHover = 1090519039;
            break;
         case "purple":
            bg = -233501148;
            bg2 = -233041360;
            surface = -14608840;
            surface2 = -14017465;
            surfaceHover = -13426091;
            break;
         case "ocean":
            bg = -234219748;
            bg2 = -233955289;
            surface = -15652816;
            surface2 = -15388357;
            surfaceHover = -14992569;
            break;
         case "sunset":
            bg = -232911084;
            bg2 = -232385766;
            surface = -13887970;
            surface2 = -13165787;
            surfaceHover = -12443347;
            break;
         case "christmas":
            bg = -233825774;
            bg2 = -233496039;
            surface = -15194084;
            surface2 = -14798301;
            surfaceHover = -14271189;
            break;
         case "light":
            bg = -168561417;
            bg2 = -169219088;
            surface = -1;
            surface2 = -920843;
            surfaceHover = -1644306;
            break;
         default:
            bg = -234025454;
            bg2 = -233696488;
            surface = -15263714;
            surface2 = -14802905;
            surfaceHover = -14342094;
      }

      if (light) {
         stroke = 335544320;
         strokeHi = 704643072;
         text = -15395302;
         sub = -10788498;
         muted = -7301213;
         shadow = 805306368;
      } else {
         stroke = 318767103;
         strokeHi = 654311423;
         text = -789258;
         sub = -6117197;
         muted = -10327949;
         shadow = Integer.MIN_VALUE;
      }

      accentText = brightness(accent) > 0.62 ? -15658735 : -1;
      danger = -45730;
      ok = -14756000;
   }

   public static double brightness(int var0) {
      int var1 = var0 >> 16 & 0xFF;
      int var2 = var0 >> 8 & 0xFF;
      int var3 = var0 & 0xFF;
      return (0.299 * var1 + 0.587 * var2 + 0.114 * var3) / 255.0;
   }
}
