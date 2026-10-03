package dev.lego.ui;

import dev.lego.core.Modules;
import dev.spotifyhud.Json;
import java.util.LinkedHashMap;

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

   private UiSettings() {
   }

   public static float anim() {
      return animations ? (float)animSpeed : 60.0F;
   }

   public static void register() {
      Modules.persist("ui", new Modules.Persist() {
         @Override
         public Object save() {
            LinkedHashMap var1 = new LinkedHashMap();
            var1.put("hudStyle", (long)UiSettings.hudStyle);
            var1.put("hudOpacity", UiSettings.hudOpacity);
            var1.put("hudIcons", UiSettings.hudIcons);
            var1.put("hudShadow", UiSettings.hudShadow);
            var1.put("hudAccentLine", UiSettings.hudAccentLine);
            var1.put("sounds", UiSettings.sounds);
            var1.put("blur", UiSettings.blur);
            var1.put("uiScale", UiSettings.uiScale);
            var1.put("animSpeed", UiSettings.animSpeed);
            var1.put("animations", UiSettings.animations);
            var1.put("xmas", UiSettings.xmas);
            return var1;
         }

         @Override
         public void load(Object var1) {
            UiSettings.hudStyle = (int)Math.max(0L, Math.min((long)(UiSettings.HUD_STYLES.length - 1), Json.num(var1, "hudStyle", UiSettings.hudStyle)));
            UiSettings.hudOpacity = Math.max(0.0, Math.min(1.0, Json.dbl(var1, "hudOpacity", UiSettings.hudOpacity)));
            UiSettings.hudIcons = Json.bool(var1, "hudIcons", UiSettings.hudIcons);
            UiSettings.hudShadow = Json.bool(var1, "hudShadow", UiSettings.hudShadow);
            UiSettings.hudAccentLine = Json.bool(var1, "hudAccentLine", UiSettings.hudAccentLine);
            UiSettings.sounds = Json.bool(var1, "sounds", UiSettings.sounds);
            UiSettings.blur = Json.bool(var1, "blur", UiSettings.blur);
            UiSettings.uiScale = Math.max(0.6, Math.min(1.3, Json.dbl(var1, "uiScale", UiSettings.uiScale)));
            UiSettings.animSpeed = Math.max(0.5, Math.min(3.0, Json.dbl(var1, "animSpeed", UiSettings.animSpeed)));
            UiSettings.animations = Json.bool(var1, "animations", UiSettings.animations);
            UiSettings.xmas = Json.bool(var1, "xmas", UiSettings.xmas);
            Sound.uiSounds = UiSettings.sounds;
         }
      });
   }
}
