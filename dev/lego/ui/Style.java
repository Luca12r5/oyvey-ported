package dev.lego.ui;

import dev.lego.core.Lang;
import dev.lego.core.Modules;

public final class Style {
   public static final String[] THEMES = new String[]{
      "winter",
      "standard",
      "midnight",
      "glass",
      "purple",
      "ocean",
      "sunset",
      "christmas",
      "light",
      "neon",
      "cyberpunk",
      "forest",
      "cherry",
      "gold",
      "aqua",
      "ash",
      "carbon",
      "halloween",
      "volcano",
      "lavender",
      "nord",
      "mint",
      "rose",
      "copper",
      "blood",
      "storm"
   };
   private static final String[] THEME_NAMES_DE = new String[]{
      "Winterzauber",
      "Standard",
      "Mitternacht",
      "Glas",
      "Lila",
      "Ozean",
      "Sunset",
      "Weihnachten",
      "Hell",
      "Neon",
      "Cyberpunk",
      "Wald",
      "Kirschblüte",
      "Gold",
      "Aqua",
      "Asche",
      "Carbon",
      "Halloween",
      "Vulkan",
      "Lavendel",
      "Nord",
      "Minze",
      "Rose",
      "Kupfer",
      "Blut",
      "Sturm"
   };
   public static String[] THEME_NAMES = THEME_NAMES_DE;
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

   public static void updateThemeNames() {
      String[] n = new String[THEME_NAMES_DE.length];

      for (int i = 0; i < n.length; i++) {
         n[i] = Lang.get("theme." + i, THEME_NAMES_DE[i]);
      }

      THEME_NAMES = n;
   }

   public static int[] palette(String theme) {
      String old = Modules.theme;
      Modules.theme = theme;
      update();
      int[] r = new int[]{bg, surface, surface2, text, sub, surfaceHover};
      Modules.theme = old;
      update();
      return r;
   }

   public static void update() {
      accent = Modules.accentArgb();
      String t = Modules.theme;
      light = "light".equals(t);
      switch (t) {
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
         case "halloween":
            bg = -233567720;
            bg2 = -233042398;
            surface = -14544336;
            surface2 = -13887428;
            surfaceHover = -13099446;
            break;
         case "light":
            bg = -168561417;
            bg2 = -169219088;
            surface = -1;
            surface2 = -920843;
            surfaceHover = -1644306;
            break;
         case "neon":
            bg = -234552048;
            bg2 = -234354666;
            surface = -15987682;
            surface2 = -15592920;
            surfaceHover = -15066570;
            break;
         case "cyberpunk":
            bg = -233568238;
            bg2 = -233042918;
            surface = -14545376;
            surface2 = -13888472;
            surfaceHover = -13100492;
            break;
         case "forest":
            bg = -234089460;
            bg2 = -233759727;
            surface = -15457772;
            surface2 = -15061990;
            surfaceHover = -14600671;
            break;
         case "cherry":
            bg = -233041896;
            bg2 = -232516576;
            surface = -13887450;
            surface2 = -13099472;
            surfaceHover = -12311238;
            break;
         case "gold":
            bg = -233435128;
            bg2 = -232909300;
            surface = -14279666;
            surface2 = -13622254;
            surfaceHover = -12833256;
            break;
         case "aqua":
            bg = -234351592;
            bg2 = -234087390;
            surface = -15850968;
            surface2 = -15586252;
            surfaceHover = -15190466;
            break;
         case "ash":
            bg = -233301992;
            bg2 = -232907234;
            surface = -14277082;
            surface2 = -13750738;
            surfaceHover = -13092808;
            break;
         case "carbon":
            bg = -233959406;
            bg2 = -233564648;
            surface = -15197154;
            surface2 = -14802394;
            surfaceHover = -14275536;
            break;
         case "volcano":
            bg = -233042936;
            bg2 = -232517620;
            surface = -13888496;
            surface2 = -13100524;
            surfaceHover = -12312550;
            break;
         case "lavender":
            bg = -233566176;
            bg2 = -233040344;
            surface = -14541774;
            surface2 = -14015938;
            surfaceHover = -13358004;
            break;
         case "nord":
            bg = -232773582;
            bg2 = -232247240;
            surface = -13748672;
            surface2 = -13222326;
            surfaceHover = -12695466;
            break;
         case "mint":
            bg = -234220012;
            bg2 = -233955812;
            surface = -15587806;
            surface2 = -15192020;
            surfaceHover = -14795722;
            break;
         case "rose":
            bg = -233173484;
            bg2 = -232647652;
            surface = -14018526;
            surface2 = -13361620;
            surfaceHover = -12573130;
            break;
         case "copper":
            bg = -233303536;
            bg2 = -232777706;
            surface = -14148070;
            surface2 = -13490656;
            surfaceHover = -12701656;
            break;
         case "blood":
            bg = -233306104;
            bg2 = -232649716;
            surface = -13890034;
            surface2 = -13102060;
            surfaceHover = -12314086;
            break;
         case "storm":
            bg = -233827304;
            bg2 = -233432030;
            surface = -14933464;
            surface2 = -14538190;
            surfaceHover = -14011330;
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

   public static double brightness(int argb) {
      int r = argb >> 16 & 0xFF;
      int g = argb >> 8 & 0xFF;
      int b = argb & 0xFF;
      return (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
   }
}
