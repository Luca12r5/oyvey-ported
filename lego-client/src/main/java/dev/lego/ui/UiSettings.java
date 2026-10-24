package dev.lego.ui;

import dev.lego.core.Lang;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.spotifyhud.Json;
import java.util.LinkedHashMap;
import java.util.Map;

public final class UiSettings {
   public static final String[] HUD_STYLES = new String[]{"Clean", "Minimal", "Spotify"};
   public static int hudStyle = 0;
   public static double hudOpacity = 0.55;
   public static boolean hudIcons = false;
   public static boolean hudShadow = true;
   public static boolean hudAccentLine = false;
   public static boolean sounds = true;
   public static boolean blur = true;
   public static double uiScale = 0.75;
   public static double animSpeed = 2.0;
   public static boolean animations = true;
   public static boolean xmas = true;
   /** Corner rounding of the whole UI (1 = as designed). */
   public static double roundness = 1.4;
   public static boolean halloween = true;
   public static int bgStyle = 1;
   public static double bgIntensity = 0.75;
   public static double glass = 0.55;
   public static boolean intro = true;
   public static boolean guiLine = true;
   public static String bgImage = "";
   public static int guiStyle = 0;
   public static final String[] GUI_STYLES = new String[]{"Standard", "Modern", "Glass"};
   public static int language = 0;

   private UiSettings() {
   }

   public static float anim() {
      return animations ? (float)animSpeed : 60.0F;
   }

   public static void register() {
      Modules.persist("ui", new Modules.Persist() {
         @Override
         public Object save() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("hudStyle", (long)UiSettings.hudStyle);
            m.put("hudOpacity", UiSettings.hudOpacity);
            m.put("hudIcons", UiSettings.hudIcons);
            m.put("hudShadow", UiSettings.hudShadow);
            m.put("hudAccentLine", UiSettings.hudAccentLine);
            m.put("sounds", UiSettings.sounds);
            m.put("blur", UiSettings.blur);
            m.put("uiScale", UiSettings.uiScale);
            m.put("animSpeed", UiSettings.animSpeed);
            m.put("animations", UiSettings.animations);
            m.put("xmas", UiSettings.xmas);
            m.put("roundness", UiSettings.roundness);
            m.put("halloween", UiSettings.halloween);
            m.put("bgStyle", (long)UiSettings.bgStyle);
            m.put("bgIntensity", UiSettings.bgIntensity);
            m.put("glass", UiSettings.glass);
            m.put("intro", UiSettings.intro);
            m.put("guiLine", UiSettings.guiLine);
            m.put("bgImage", UiSettings.bgImage);
            m.put("guiStyle", (long)UiSettings.guiStyle);
            m.put("language", (long)UiSettings.language);
            return m;
         }

         @Override
         public void load(Object o) {
            UiSettings.hudStyle = (int)Math.max(0L, Math.min((long)(UiSettings.HUD_STYLES.length - 1), Json.num(o, "hudStyle", UiSettings.hudStyle)));
            UiSettings.hudOpacity = Math.max(0.0, Math.min(1.0, Json.dbl(o, "hudOpacity", UiSettings.hudOpacity)));
            UiSettings.hudIcons = Json.bool(o, "hudIcons", UiSettings.hudIcons);
            UiSettings.hudShadow = Json.bool(o, "hudShadow", UiSettings.hudShadow);
            UiSettings.hudAccentLine = Json.bool(o, "hudAccentLine", UiSettings.hudAccentLine);
            UiSettings.sounds = Json.bool(o, "sounds", UiSettings.sounds);
            UiSettings.blur = Json.bool(o, "blur", UiSettings.blur);
            UiSettings.uiScale = Math.max(0.6, Math.min(1.3, Json.dbl(o, "uiScale", UiSettings.uiScale)));
            UiSettings.animSpeed = Math.max(0.5, Math.min(3.0, Json.dbl(o, "animSpeed", UiSettings.animSpeed)));
            UiSettings.animations = Json.bool(o, "animations", UiSettings.animations);
            UiSettings.xmas = Json.bool(o, "xmas", UiSettings.xmas);
            UiSettings.roundness = Math.max(0.0, Math.min(2.5, Json.dbl(o, "roundness", UiSettings.roundness)));
            Gx.round = (float)UiSettings.roundness;
            UiSettings.halloween = Json.bool(o, "halloween", UiSettings.halloween);
            UiSettings.bgStyle = (int)Math.max(0L, Math.min((long)(BgFx.COUNT - 1), Json.num(o, "bgStyle", UiSettings.bgStyle)));
            UiSettings.bgIntensity = Math.max(0.1, Math.min(1.0, Json.dbl(o, "bgIntensity", UiSettings.bgIntensity)));
            UiSettings.intro = Json.bool(o, "intro", UiSettings.intro);
            UiSettings.guiLine = Json.bool(o, "guiLine", UiSettings.guiLine);
            UiSettings.glass = Math.max(0.15, Math.min(1.0, Json.dbl(o, "glass", UiSettings.glass)));
            UiSettings.bgImage = Json.str(o, "bgImage", UiSettings.bgImage);
            UiSettings.guiStyle = (int)Math.max(0L, Math.min((long)(UiSettings.GUI_STYLES.length - 1), Json.num(o, "guiStyle", UiSettings.guiStyle)));
            UiSettings.language = (int)Math.max(0L, Math.min((long)(Lang.LANGUAGES.length - 1), Json.num(o, "language", UiSettings.language)));
            Lang.setLanguage(UiSettings.language);
            BgFx.updateNames();
            Style.updateThemeNames();
            Setting.Color.updateNames();
            Sound.uiSounds = UiSettings.sounds;
         }
      });
   }
}
