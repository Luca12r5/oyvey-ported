package dev.lego.core;

import dev.lego.LegoClient;
import dev.lego.hud.HudModule;
import dev.spotifyhud.Json;
import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.loader.api.FabricLoader;

public final class Modules {
   public static final List<Module> ALL = new ArrayList<>();
   public static String theme = "winter";
   public static String hudTheme = "standard";
   public static int hudAccent = 0;
   public static int accent = 1;
   private static volatile long saveAt = 0L;
   private static final Map<String, Modules.Persist> EXTRA = new LinkedHashMap<>();

   private Modules() {
   }

   public static <T extends Module> T register(T var0) {
      ALL.add(var0);
      var0.captureDefaults();
      return (T)var0;
   }

   public static void persist(String var0, Modules.Persist var1) {
      EXTRA.put(var0, var1);
   }

   public static List<Module> of(Category var0) {
      ArrayList var1 = new ArrayList();

      for (Module var3 : ALL) {
         if (var3.category == var0) {
            var1.add(var3);
         }
      }

      return var1;
   }

   public static Module get(String var0) {
      for (Module var2 : ALL) {
         if (var2.id.equals(var0)) {
            return var2;
         }
      }

      return null;
   }

   public static int accentArgb() {
      return argbOf(accent);
   }

   public static int hudAccentArgb() {
      return argbOf(hudAccent);
   }

   private static int argbOf(int var0) {
      int var1 = Math.max(0, Math.min(Setting.Color.PALETTE.length - 1, var0));
      if (var1 == Setting.Color.PALETTE.length - 1) {
         double var2 = System.currentTimeMillis() / 2600.0;
         return 0xFF000000 | Color.HSBtoRGB((float)(var2 - Math.floor(var2)), 0.7F, 1.0F) & 16777215;
      } else {
         return Setting.Color.PALETTE[var1];
      }
   }

   private static Path path() {
      return FabricLoader.getInstance().getConfigDir().resolve("legoclient.json");
   }

   public static void scheduleSave() {
      saveAt = System.currentTimeMillis() + 700L;
   }

   public static void tickSave() {
      if (saveAt != 0L && System.currentTimeMillis() >= saveAt) {
         saveAt = 0L;
         save();
      }
   }

   public static synchronized void save() {
      try {
         LinkedHashMap var0 = new LinkedHashMap();
         var0.put("theme", theme);
         var0.put("designVersion", 3L);
         var0.put("hudTheme", hudTheme);
         var0.put("hudAccent", (long)hudAccent);
         var0.put("accent", (long)accent);
         LinkedHashMap var1 = new LinkedHashMap();

         for (Module var3 : ALL) {
            LinkedHashMap var4 = new LinkedHashMap();
            var4.put("on", var3.enabled);
            var4.put("key", (long)var3.key);
            LinkedHashMap var5 = new LinkedHashMap();

            for (Setting var7 : var3.settings) {
               Object var8 = var7.save();
               if (var8 != null) {
                  var5.put(var7.name, var8);
               }
            }

            var4.put("settings", var5);
            if (var3 instanceof HudModule var16) {
               var4.put("x", var16.fx);
               var4.put("y", var16.fy);
               var4.put("scale", var16.scale);
            }

            var1.put(var3.id, var4);
         }

         var0.put("modules", var1);

         for (Entry var13 : EXTRA.entrySet()) {
            try {
               Object var15 = ((Modules.Persist)var13.getValue()).save();
               if (var15 != null) {
                  var0.put((String)var13.getKey(), var15);
               }
            } catch (Throwable var9) {
               LegoClient.LOG("Speichern " + (String)var13.getKey() + ": " + var9);
            }
         }

         Path var12 = path();
         Files.createDirectories(var12.getParent());
         Path var14 = var12.resolveSibling("legoclient.json.tmp");
         Files.writeString(var14, Json.write(var0), StandardCharsets.UTF_8);
         Files.move(var14, var12, StandardCopyOption.REPLACE_EXISTING);
      } catch (Throwable var10) {
         LegoClient.LOG("Speichern fehlgeschlagen: " + var10);
      }
   }

   public static void load() {
      try {
         Path var0 = path();
         if (!Files.exists(var0)) {
            return;
         }

         Object var1 = Json.parse(Files.readString(var0, StandardCharsets.UTF_8));
         theme = Json.str(var1, "theme", theme);
         accent = (int)Json.num(var1, "accent", accent);
         hudTheme = Json.str(var1, "hudTheme", hudTheme);
         hudAccent = (int)Json.num(var1, "hudAccent", hudAccent);
         if (Json.num(var1, "designVersion", 0L) < 3L) {
            hudTheme = theme;
            hudAccent = accent;
            theme = "winter";
            accent = 1;
         }

         if (var1 instanceof Map) {
            for (Entry var3 : EXTRA.entrySet()) {
               Object var4 = ((Map)var1).get(var3.getKey());
               if (var4 != null) {
                  try {
                     ((Modules.Persist)var3.getValue()).load(var4);
                  } catch (Throwable var9) {
                     LegoClient.LOG("Laden " + (String)var3.getKey() + ": " + var9);
                  }
               }
            }
         }

         Map var11 = Json.obj(var1, "modules");
         if (var11 == null) {
            return;
         }

         for (Module var13 : ALL) {
            Object var5 = var11.get(var13.id);
            if (var5 instanceof Map) {
               var13.key = (int)Json.num(var5, "key", var13.key);
               Map var6 = Json.obj(var5, "settings");
               if (var6 != null) {
                  for (Setting var8 : var13.settings) {
                     if (var6.containsKey(var8.name)) {
                        var8.load(var6.get(var8.name));
                     }
                  }
               }

               if (var13 instanceof HudModule var14) {
                  var14.fx = Math.max(0.0, Math.min(1.0, Json.dbl(var5, "x", var14.fx)));
                  var14.fy = Math.max(0.0, Math.min(1.0, Json.dbl(var5, "y", var14.fy)));
                  var14.scale = Math.max(0.4, Math.min(3.0, Json.dbl(var5, "scale", var14.scale)));
               }

               boolean var15 = Json.bool(var5, "on", var13.enabled);
               if (var13.isToggleable() && var15 != var13.enabled) {
                  var13.setEnabled(var15);
               }
            }
         }
      } catch (Throwable var10) {
         LegoClient.LOG("Laden fehlgeschlagen: " + var10);
      }
   }

   public interface Persist {
      Object save();

      void load(Object var1);
   }
}
