package de.bauloo.styles;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import net.fabricmc.loader.api.FabricLoader;

public final class StylesUiConfig {
   public static final int DEFAULT_ACCENT = 7722239;
   public static final StylesTheme.Background DEFAULT_BACKGROUND = StylesTheme.Background.GLASS;
   public static final int DEFAULT_OPACITY = 95;
   public static int accent = 7722239;
   public static StylesTheme.Background background = DEFAULT_BACKGROUND;
   public static int opacity = 95;
   public static boolean glow = true;
   public static boolean brackets = true;
   public static boolean hints = true;
   public static boolean themesTab;
   public static boolean outline = false;
   public static boolean outlineSelf = true;
   public static boolean outlineOthers = false;
   public static boolean outlineRainbow = false;
   public static int outlineColor = 16734920;
   public static int outlineWidth = 2;
   public static int shieldOpacity = 100;
   public static int totemSize = 100;
   public static int shieldSize = 100;
   public static int gapSize = 100;
   public static int crystalSpin = 100;
   public static int totemPopSize = 100;
   public static boolean hitboxCustom = false;
   public static int hitboxColor = 11766015;
   public static boolean crosshairCustom = false;
   public static int crosshairColor = 11766015;
   public static boolean blockOutlineCustom = false;
   public static String killEffect = "off";
   public static boolean killOnlyMine = true;
   public static boolean killSound = false;
   public static int killVolume = 100;
   public static boolean cosmeticsSeen = false;
   public static boolean noExplosionSound = false;
   public static int killColor = 11766015;
   public static int blockOutlineColor = 0;
   public static int blockOutlineWidth = 3;
   public static boolean crosshairGradient = false;
   public static int crosshairColor2 = 16734920;
   public static int gradientStrength = 100;
   public static boolean crystalBoxCustom = false;
   public static int crystalBoxColor = 11766015;
   public static boolean anchorGlowCustom = false;
   public static int anchorGlowColor = 11766015;
   public static boolean anchorShowFull = false;
   public static boolean anchorPixelGlow = false;
   public static boolean coreCustom = false;
   public static int coreColor = 16734920;
   public static boolean hitCustom = false;
   public static int hitColor = 11766015;
   public static int hitStrength = 55;
   public static boolean hitArmor = true;
   public static boolean glintCustom = false;
   public static int glintColor = 11766015;
   public static boolean glintGradient = false;
   public static int glintColor2 = 7722239;
   public static boolean glintRainbow = false;
   public static boolean hitRainbow = false;
   public static int fireHeight = 100;
   public static boolean spotifyHud = false;
   public static boolean spotifyPopup = false;
   public static boolean watermark = true;
   public static int watermarkStyle = 1;
   public static String socialLogo = "off";
   public static String socialName = "";
   public static float spotifyX = -1.0F;
   public static float spotifyY = -1.0F;
   public static float spotifyScale = 1.0F;
   public static boolean fireCustom = false;
   public static int fireColor = 7722239;
   public static boolean fogOff = false;
   public static boolean earlyAccess = true;
   public static boolean invClearBackground = false;
   public static boolean cobwebOutline = false;
   public static int timeOverride = -1;
   public static int cobwebOutlineWidth = 2;
   public static boolean showInTab = true;
   public static String updateSeen = "";
   public static String lastVersion = "";
   public static String lastNotes = "";
   private static boolean hadFile;
   public static String cosCape = "none";
   public static String cosHead = "none";
   public static String cosAura = "none";
   public static int cosColor = 11766015;
   public static boolean cosRainbow = false;
   public static boolean cosCapeElytra = true;
   public static boolean cosCapeTint = false;
   public static int fogDistance = 100;
   public static boolean fogCustom = false;
   public static int fogColor = 11766015;
   public static boolean grassCustom = false;
   public static boolean applyInstantly = false;
   public static int grassColor = 9551193;
   public static boolean shieldCustom = false;
   public static int shieldColor = 11766015;
   public static String presetExtras = "with";
   public static boolean presetBackup = true;
   private static final Set<String> MENU_KEYS = Set.of(
      "accent",
      "background",
      "opacity",
      "glow",
      "brackets",
      "hints",
      "themesTab",
      "favorites",
      "presetExtras",
      "presetBackup",
      "spotifyX",
      "spotifyY",
      "spotifyScale",
      "earlyAccess",
      "timeOverride",
      "updateSeen",
      "lastVersion",
      "invClearBackground",
      "showInTab",
      "cosCape",
      "cosHead",
      "cosAura",
      "cosColor",
      "cosRainbow",
      "cosCapeElytra",
      "cosCapeTint",
      "totemPopSize"
   );
   private static final Set<String> FAVORITES = new LinkedHashSet<>();
   private static boolean loaded;
   private static StylesTheme cached;
   private static String cachedKey = "";

   private StylesUiConfig() {
   }

   public static StylesTheme theme() {
      load();
      String key = accent + ":" + background + ":" + opacity;
      if (cached == null || !key.equals(cachedKey)) {
         cached = StylesTheme.of(accent, background, opacity);
         cachedKey = key;
      }

      return cached;
   }

   public static boolean isFavorite(String folder) {
      load();
      return folder != null && FAVORITES.contains(folder);
   }

   public static void toggleFavorite(String folder) {
      load();
      if (folder != null) {
         if (!FAVORITES.remove(folder)) {
            FAVORITES.add(folder);
         }

         save();
      }
   }

   public static void resetTheme() {
      accent = 7722239;
      background = DEFAULT_BACKGROUND;
      opacity = 95;
      glow = true;
      brackets = true;
      hints = true;
      save();
   }

   private static Path file() {
      return FabricLoader.getInstance().getConfigDir().resolve("bauloo-styles-ui.properties");
   }

   public static boolean hadFile() {
      load();
      return hadFile;
   }

   public static void load() {
      if (!loaded) {
         loaded = true;
         Path f = file();
         if (Files.exists(f)) {
            hadFile = true;
            Properties p = new Properties();

            try (Reader in = Files.newBufferedReader(f)) {
               p.load(in);
            } catch (IOException e) {
               return;
            }

            try {
               accent = Integer.parseInt(p.getProperty("accent", Integer.toHexString(7722239)), 16) & 16777215;
            } catch (NumberFormatException ignored) {
               accent = 7722239;
            }

            try {
               background = StylesTheme.Background.valueOf(p.getProperty("background", DEFAULT_BACKGROUND.name()));
            } catch (IllegalArgumentException ignored) {
               background = DEFAULT_BACKGROUND;
            }

            if (background == StylesTheme.Background.NAVY) {
               background = StylesTheme.Background.GLASS;
            }

            try {
               opacity = Math.max(60, Math.min(100, Integer.parseInt(p.getProperty("opacity", "95"))));
            } catch (NumberFormatException ignored) {
               opacity = 95;
            }

            glow = Boolean.parseBoolean(p.getProperty("glow", "true"));
            brackets = Boolean.parseBoolean(p.getProperty("brackets", "true"));
            hints = Boolean.parseBoolean(p.getProperty("hints", "true"));
            themesTab = Boolean.parseBoolean(p.getProperty("themesTab", "false"));
            outline = Boolean.parseBoolean(p.getProperty("outline", "false"));
            outlineSelf = Boolean.parseBoolean(p.getProperty("outlineSelf", "true"));
            outlineOthers = Boolean.parseBoolean(p.getProperty("outlineOthers", "false"));
            outlineRainbow = Boolean.parseBoolean(p.getProperty("outlineRainbow", "false"));

            try {
               outlineWidth = Math.max(1, Math.min(6, Integer.parseInt(p.getProperty("outlineWidth", "2"))));
            } catch (NumberFormatException e) {
               outlineWidth = 2;
            }

            try {
               outlineColor = Integer.parseInt(p.getProperty("outlineColor", "FF5AC8"), 16) & 16777215;
            } catch (NumberFormatException ignored) {
               outlineColor = 16734920;
            }

            try {
               shieldOpacity = Math.max(15, Math.min(100, Integer.parseInt(p.getProperty("shieldOpacity", "100"))));
            } catch (NumberFormatException ignored) {
               shieldOpacity = 100;
            }

            try {
               totemSize = Math.max(20, Math.min(150, Integer.parseInt(p.getProperty("totemSize", "100"))));
            } catch (NumberFormatException ignored) {
               totemSize = 100;
            }

            try {
               shieldSize = Math.max(20, Math.min(150, Integer.parseInt(p.getProperty("shieldSize", "100"))));
            } catch (NumberFormatException ignored) {
               shieldSize = 100;
            }

            try {
               gapSize = Math.max(20, Math.min(150, Integer.parseInt(p.getProperty("gapSize", "100"))));
            } catch (NumberFormatException ignored) {
               gapSize = 100;
            }

            try {
               crystalSpin = Math.max(0, Math.min(1000, Integer.parseInt(p.getProperty("crystalSpin", "100"))));
            } catch (NumberFormatException ignored) {
               crystalSpin = 100;
            }

            try {
               totemPopSize = Math.max(20, Math.min(150, Integer.parseInt(p.getProperty("totemPopSize", "100"))));
            } catch (NumberFormatException ignored) {
               totemPopSize = 100;
            }

            hitboxCustom = Boolean.parseBoolean(p.getProperty("hitboxCustom", "false"));
            crosshairCustom = Boolean.parseBoolean(p.getProperty("crosshairCustom", "false"));
            blockOutlineCustom = Boolean.parseBoolean(p.getProperty("blockOutlineCustom", "false"));
            killEffect = p.getProperty("killEffect", "off");
            if ("shatter".equals(killEffect)) {
               killEffect = "off";
            }

            killOnlyMine = Boolean.parseBoolean(p.getProperty("killOnlyMine", "true"));
            killSound = Boolean.parseBoolean(p.getProperty("killSound", "false"));
            cosmeticsSeen = Boolean.parseBoolean(p.getProperty("cosmeticsSeen", "false"));

            try {
               killVolume = Math.max(5, Math.min(100, Integer.parseInt(p.getProperty("killVolume", "100"))));
            } catch (NumberFormatException ignored) {
               killVolume = 100;
            }

            noExplosionSound = Boolean.parseBoolean(p.getProperty("noExplosionSound", "false"));

            try {
               killColor = Integer.parseInt(p.getProperty("killColor", "B388FF"), 16) & 16777215;
            } catch (NumberFormatException e) {
               killColor = 11766015;
            }

            try {
               blockOutlineColor = Integer.parseInt(p.getProperty("blockOutlineColor", "000000"), 16) & 16777215;
               blockOutlineWidth = Math.max(1, Math.min(6, Integer.parseInt(p.getProperty("blockOutlineWidth", "3"))));
            } catch (NumberFormatException e) {
               blockOutlineColor = 0;
               blockOutlineWidth = 3;
            }

            crosshairGradient = Boolean.parseBoolean(p.getProperty("crosshairGradient", "false"));
            crystalBoxCustom = Boolean.parseBoolean(p.getProperty("crystalBoxCustom", "false"));
            anchorGlowCustom = Boolean.parseBoolean(p.getProperty("anchorGlowCustom", "false"));
            anchorShowFull = Boolean.parseBoolean(p.getProperty("anchorShowFull", "false"));
            anchorPixelGlow = Boolean.parseBoolean(p.getProperty("anchorPixelGlow", "false"));
            coreCustom = Boolean.parseBoolean(p.getProperty("coreCustom", "false"));
            hitCustom = Boolean.parseBoolean(p.getProperty("hitCustom", "false"));
            hitRainbow = Boolean.parseBoolean(p.getProperty("hitRainbow", "false"));
            hitArmor = Boolean.parseBoolean(p.getProperty("hitArmor", "true"));
            glintCustom = Boolean.parseBoolean(p.getProperty("glintCustom", "false"));
            glintGradient = Boolean.parseBoolean(p.getProperty("glintGradient", "false"));
            glintRainbow = Boolean.parseBoolean(p.getProperty("glintRainbow", "false"));

            try {
               glintColor = Integer.parseInt(p.getProperty("glintColor", "B388FF"), 16) & 16777215;
               glintColor2 = Integer.parseInt(p.getProperty("glintColor2", "75D4FF"), 16) & 16777215;
            } catch (NumberFormatException ignored) {
               glintColor = 11766015;
               glintColor2 = 7722239;
            }

            try {
               coreColor = Integer.parseInt(p.getProperty("coreColor", "FF5AC8"), 16) & 16777215;
               hitColor = Integer.parseInt(p.getProperty("hitColor", "B388FF"), 16) & 16777215;
               hitStrength = Math.max(20, Math.min(100, Integer.parseInt(p.getProperty("hitStrength", "55"))));
            } catch (NumberFormatException ignored) {
               coreColor = 16734920;
               hitColor = 11766015;
               hitStrength = 55;
            }

            try {
               anchorGlowColor = Integer.parseInt(p.getProperty("anchorGlowColor", "B388FF"), 16) & 16777215;
            } catch (NumberFormatException ignored) {
               anchorGlowColor = 11766015;
            }

            try {
               crystalBoxColor = Integer.parseInt(p.getProperty("crystalBoxColor", "B388FF"), 16) & 16777215;
            } catch (NumberFormatException ignored) {
               crystalBoxColor = 11766015;
            }

            try {
               crosshairColor2 = Integer.parseInt(p.getProperty("crosshairColor2", "FF5AC8"), 16) & 16777215;
            } catch (NumberFormatException ignored) {
               crosshairColor2 = 16734920;
            }

            try {
               gradientStrength = Math.max(50, Math.min(400, Integer.parseInt(p.getProperty("gradientStrength", "100"))));
            } catch (NumberFormatException ignored) {
               gradientStrength = 100;
            }

            try {
               hitboxColor = Integer.parseInt(p.getProperty("hitboxColor", "B388FF"), 16) & 16777215;
               crosshairColor = Integer.parseInt(p.getProperty("crosshairColor", "B388FF"), 16) & 16777215;
            } catch (NumberFormatException ignored) {
               hitboxColor = 11766015;
               crosshairColor = 11766015;
            }

            spotifyHud = Boolean.parseBoolean(p.getProperty("spotifyHud", "false"));
            spotifyPopup = Boolean.parseBoolean(p.getProperty("spotifyPopup", "false"));
            watermark = Boolean.parseBoolean(p.getProperty("watermark", "true"));
            watermarkStyle = "2".equals(p.getProperty("watermarkStyle", "1")) ? 2 : 1;
            socialLogo = p.getProperty("socialLogo", "off");
            socialName = p.getProperty("socialName", "");

            try {
               spotifyX = Float.parseFloat(p.getProperty("spotifyX", "-1"));
               spotifyY = Float.parseFloat(p.getProperty("spotifyY", "-1"));
               spotifyScale = Math.max(0.5F, Math.min(1.5F, Float.parseFloat(p.getProperty("spotifyScale", "1"))));
            } catch (NumberFormatException e) {
               spotifyX = -1.0F;
               spotifyY = -1.0F;
               spotifyScale = 1.0F;
            }

            try {
               fireHeight = Math.max(10, Math.min(100, Integer.parseInt(p.getProperty("fireHeight", "100"))));
               fireColor = Integer.parseInt(p.getProperty("fireColor", "75D4FF"), 16) & 16777215;
               shieldColor = Integer.parseInt(p.getProperty("shieldColor", "B388FF"), 16) & 16777215;
            } catch (NumberFormatException ignored) {
               fireHeight = 100;
               fireColor = 7722239;
               shieldColor = 11766015;
            }

            fireCustom = Boolean.parseBoolean(p.getProperty("fireCustom", "false"));
            fogOff = Boolean.parseBoolean(p.getProperty("fogOff", "false"));
            earlyAccess = Boolean.parseBoolean(p.getProperty("earlyAccess", "true"));
            updateSeen = p.getProperty("updateSeen", "");
            lastVersion = p.getProperty("lastVersion", "");
            lastNotes = p.getProperty("lastNotes", "");
            invClearBackground = Boolean.parseBoolean(p.getProperty("invClearBackground", "false"));
            cobwebOutline = Boolean.parseBoolean(p.getProperty("cobwebOutline", "false"));

            try {
               timeOverride = Math.max(-1, Math.min(23999, Integer.parseInt(p.getProperty("timeOverride", "-1"))));
            } catch (NumberFormatException ignored) {
               timeOverride = -1;
            }

            try {
               cobwebOutlineWidth = Math.max(1, Math.min(16, Integer.parseInt(p.getProperty("cobwebOutlineQuarters", "2"))));
            } catch (NumberFormatException ignored) {
               cobwebOutlineWidth = 2;
            }

            showInTab = Boolean.parseBoolean(p.getProperty("showInTab", "true"));
            cosCape = p.getProperty("cosCape", "none");
            cosHead = p.getProperty("cosHead", "none");
            cosAura = p.getProperty("cosAura", "none");
            cosRainbow = Boolean.parseBoolean(p.getProperty("cosRainbow", "false"));
            cosCapeElytra = Boolean.parseBoolean(p.getProperty("cosCapeElytra", "true"));
            cosCapeTint = Boolean.parseBoolean(p.getProperty("cosCapeTint", "false"));

            try {
               cosColor = Integer.parseInt(p.getProperty("cosColor", "B388FF"), 16) & 16777215;
            } catch (NumberFormatException ignored) {
               cosColor = 11766015;
            }

            fogCustom = Boolean.parseBoolean(p.getProperty("fogCustom", "false"));
            grassCustom = Boolean.parseBoolean(p.getProperty("grassCustom", "false"));
            applyInstantly = Boolean.parseBoolean(p.getProperty("applyInstantly", "false"));

            try {
               grassColor = Integer.parseInt(p.getProperty("grassColor", "91BD59"), 16) & 16777215;
            } catch (NumberFormatException ignored) {
               grassColor = 9551193;
            }

            try {
               fogDistance = Math.max(10, Math.min(100, Integer.parseInt(p.getProperty("fogDistance", "100"))));
               fogColor = Integer.parseInt(p.getProperty("fogColor", "B388FF"), 16) & 16777215;
            } catch (NumberFormatException ignored) {
               fogDistance = 100;
               fogColor = 11766015;
            }

            shieldCustom = Boolean.parseBoolean(p.getProperty("shieldCustom", "false"));
            presetExtras = p.getProperty("presetExtras", "with");
            if (!presetExtras.equals("ask") && !presetExtras.equals("without")) {
               presetExtras = "with";
            }

            presetBackup = Boolean.parseBoolean(p.getProperty("presetBackup", "true"));

            for (String folder : p.getProperty("favorites", "").split(",")) {
               if (!folder.isBlank()) {
                  FAVORITES.add(folder.trim());
               }
            }
         }
      }
   }

   public static void save() {
      load();
      Properties p = toProperties();

      try (Writer out = Files.newBufferedWriter(file())) {
         p.store(out, "Bauloo Styles - menu look and favorites");
      } catch (IOException var6) {
      }
   }

   public static Map<String, String> extras() {
      Properties p = toProperties();
      Map<String, String> result = new TreeMap<>();

      for (String key : p.stringPropertyNames()) {
         if (!MENU_KEYS.contains(key)) {
            result.put(key, p.getProperty(key));
         }
      }

      return result;
   }

   public static void applyExtras(Map<String, String> extras) {
      Properties p = new Properties();
      Properties current = toProperties();

      for (String key : MENU_KEYS) {
         if (current.getProperty(key) != null) {
            p.setProperty(key, current.getProperty(key));
         }
      }

      extras.forEach((keyx, value) -> {
         if (!MENU_KEYS.contains(keyx) && !keyx.startsWith("_")) {
            p.setProperty(keyx, value);
         }
      });

      try (Writer out = Files.newBufferedWriter(file())) {
         p.store(out, "Bauloo Styles - menu look and favorites");
      } catch (IOException ignored) {
         return;
      }

      FAVORITES.clear();
      loaded = false;
      load();
   }

   private static Path backupFile() {
      return FabricLoader.getInstance().getConfigDir().resolve("bauloo-styles-extras-backup.properties");
   }

   public static void applyPresetExtras(Map<String, String> extras) {
      String applied = readBackup().getProperty("_applied", "");
      boolean own = !String.valueOf(extras().toString().hashCode()).equals(applied);
      if (presetBackup && own) {
         backupExtras();
      }

      applyExtras(extras);
      if (hasBackup()) {
         Properties p = readBackup();
         p.setProperty("_applied", String.valueOf(extras().toString().hashCode()));
         writeBackup(p);
      }
   }

   private static Properties readBackup() {
      Properties p = new Properties();
      if (Files.exists(backupFile())) {
         try (Reader in = Files.newBufferedReader(backupFile())) {
            p.load(in);
         } catch (IOException var6) {
         }
      }

      return p;
   }

   private static void writeBackup(Properties p) {
      try (Writer out = Files.newBufferedWriter(backupFile())) {
         p.store(out, "Bauloo Styles - your Extras before a preset replaced them");
      } catch (IOException var6) {
      }
   }

   public static void backupExtras() {
      Properties p = new Properties();
      p.putAll(extras());

      try (Writer out = Files.newBufferedWriter(backupFile())) {
         p.store(out, "Bauloo Styles - your Extras before a preset replaced them");
      } catch (IOException var6) {
      }
   }

   public static boolean hasBackup() {
      return Files.exists(backupFile());
   }

   public static boolean restoreBackup() {
      if (!hasBackup()) {
         return false;
      }

      Properties p = readBackup();
      Map<String, String> extras = new TreeMap<>();

      for (String key : p.stringPropertyNames()) {
         if (!key.startsWith("_")) {
            extras.put(key, p.getProperty(key));
         }
      }

      applyExtras(extras);
      p.remove("_applied");
      writeBackup(p);
      return true;
   }

   private static Properties toProperties() {
      load();
      Properties p = new Properties();
      p.setProperty("accent", String.format("%06X", accent & 16777215));
      p.setProperty("background", background.name());
      p.setProperty("opacity", String.valueOf(opacity));
      p.setProperty("glow", String.valueOf(glow));
      p.setProperty("brackets", String.valueOf(brackets));
      p.setProperty("hints", String.valueOf(hints));
      p.setProperty("themesTab", String.valueOf(themesTab));
      p.setProperty("outline", String.valueOf(outline));
      p.setProperty("outlineSelf", String.valueOf(outlineSelf));
      p.setProperty("outlineOthers", String.valueOf(outlineOthers));
      p.setProperty("outlineRainbow", String.valueOf(outlineRainbow));
      p.setProperty("outlineColor", String.format("%06X", outlineColor & 16777215));
      p.setProperty("outlineWidth", String.valueOf(outlineWidth));
      p.setProperty("shieldOpacity", String.valueOf(shieldOpacity));
      p.setProperty("totemSize", String.valueOf(totemSize));
      p.setProperty("shieldSize", String.valueOf(shieldSize));
      p.setProperty("gapSize", String.valueOf(gapSize));
      p.setProperty("crystalSpin", String.valueOf(crystalSpin));
      p.setProperty("totemPopSize", String.valueOf(totemPopSize));
      p.setProperty("hitboxCustom", String.valueOf(hitboxCustom));
      p.setProperty("hitboxColor", String.format("%06X", hitboxColor & 16777215));
      p.setProperty("crosshairCustom", String.valueOf(crosshairCustom));
      p.setProperty("blockOutlineCustom", String.valueOf(blockOutlineCustom));
      p.setProperty("killEffect", killEffect);
      p.setProperty("killOnlyMine", String.valueOf(killOnlyMine));
      p.setProperty("killSound", String.valueOf(killSound));
      p.setProperty("killVolume", String.valueOf(killVolume));
      p.setProperty("cosmeticsSeen", String.valueOf(cosmeticsSeen));
      p.setProperty("noExplosionSound", String.valueOf(noExplosionSound));
      p.setProperty("killColor", String.format("%06X", killColor & 16777215));
      p.setProperty("blockOutlineColor", String.format("%06X", blockOutlineColor & 16777215));
      p.setProperty("blockOutlineWidth", String.valueOf(blockOutlineWidth));
      p.setProperty("crosshairColor", String.format("%06X", crosshairColor & 16777215));
      p.setProperty("crosshairGradient", String.valueOf(crosshairGradient));
      p.setProperty("crosshairColor2", String.format("%06X", crosshairColor2 & 16777215));
      p.setProperty("gradientStrength", String.valueOf(gradientStrength));
      p.setProperty("crystalBoxCustom", String.valueOf(crystalBoxCustom));
      p.setProperty("anchorGlowCustom", String.valueOf(anchorGlowCustom));
      p.setProperty("anchorShowFull", String.valueOf(anchorShowFull));
      p.setProperty("anchorPixelGlow", String.valueOf(anchorPixelGlow));
      p.setProperty("coreCustom", String.valueOf(coreCustom));
      p.setProperty("coreColor", String.format("%06X", coreColor & 16777215));
      p.setProperty("hitCustom", String.valueOf(hitCustom));
      p.setProperty("hitColor", String.format("%06X", hitColor & 16777215));
      p.setProperty("hitStrength", String.valueOf(hitStrength));
      p.setProperty("hitRainbow", String.valueOf(hitRainbow));
      p.setProperty("hitArmor", String.valueOf(hitArmor));
      p.setProperty("glintCustom", String.valueOf(glintCustom));
      p.setProperty("glintColor", String.format("%06X", glintColor & 16777215));
      p.setProperty("glintGradient", String.valueOf(glintGradient));
      p.setProperty("glintColor2", String.format("%06X", glintColor2 & 16777215));
      p.setProperty("glintRainbow", String.valueOf(glintRainbow));
      p.setProperty("anchorGlowColor", String.format("%06X", anchorGlowColor & 16777215));
      p.setProperty("crystalBoxColor", String.format("%06X", crystalBoxColor & 16777215));
      p.setProperty("fireHeight", String.valueOf(fireHeight));
      p.setProperty("spotifyHud", String.valueOf(spotifyHud));
      p.setProperty("spotifyPopup", String.valueOf(spotifyPopup));
      p.setProperty("watermark", String.valueOf(watermark));
      p.setProperty("watermarkStyle", String.valueOf(watermarkStyle));
      p.setProperty("socialLogo", socialLogo);
      p.setProperty("socialName", socialName);
      p.setProperty("spotifyX", String.valueOf(spotifyX));
      p.setProperty("spotifyY", String.valueOf(spotifyY));
      p.setProperty("spotifyScale", String.valueOf(spotifyScale));
      p.setProperty("fireCustom", String.valueOf(fireCustom));
      p.setProperty("fogOff", String.valueOf(fogOff));
      p.setProperty("earlyAccess", String.valueOf(earlyAccess));
      p.setProperty("updateSeen", updateSeen);
      p.setProperty("lastVersion", lastVersion);
      p.setProperty("lastNotes", lastNotes);
      p.setProperty("invClearBackground", String.valueOf(invClearBackground));
      p.setProperty("cobwebOutline", String.valueOf(cobwebOutline));
      p.setProperty("timeOverride", String.valueOf(timeOverride));
      p.setProperty("cobwebOutlineQuarters", String.valueOf(cobwebOutlineWidth));
      p.setProperty("showInTab", String.valueOf(showInTab));
      p.setProperty("cosCape", cosCape);
      p.setProperty("cosHead", cosHead);
      p.setProperty("cosAura", cosAura);
      p.setProperty("cosColor", String.format("%06X", cosColor & 16777215));
      p.setProperty("cosRainbow", String.valueOf(cosRainbow));
      p.setProperty("cosCapeElytra", String.valueOf(cosCapeElytra));
      p.setProperty("cosCapeTint", String.valueOf(cosCapeTint));
      p.setProperty("fogDistance", String.valueOf(fogDistance));
      p.setProperty("fogCustom", String.valueOf(fogCustom));
      p.setProperty("fogColor", String.format("%06X", fogColor & 16777215));
      p.setProperty("grassCustom", String.valueOf(grassCustom));
      p.setProperty("applyInstantly", String.valueOf(applyInstantly));
      p.setProperty("grassColor", String.format("%06X", grassColor & 16777215));
      p.setProperty("fireColor", String.format("%06X", fireColor & 16777215));
      p.setProperty("shieldCustom", String.valueOf(shieldCustom));
      p.setProperty("shieldColor", String.format("%06X", shieldColor & 16777215));
      p.setProperty("presetExtras", presetExtras);
      p.setProperty("presetBackup", String.valueOf(presetBackup));
      p.setProperty("favorites", String.join(",", FAVORITES));
      return p;
   }
}
