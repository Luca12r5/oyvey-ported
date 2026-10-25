package de.bauloo.styles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import net.fabricmc.loader.api.FabricLoader;

public final class StylesPresets {
   public static final int MAX_NAME = 20;
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final List<StylesPresets.Preset> PRESETS = new ArrayList<>();
   private static boolean loaded;

   private StylesPresets() {
   }

   public static List<StylesPresets.Preset> all() {
      load();
      return PRESETS;
   }

   public static void add(String name, Map<BaulooStylesClient.Tab, String> styles, Map<String, String> extras) {
      add(name, styles, extras, null, false);
   }

   public static void add(String name, Map<BaulooStylesClient.Tab, String> styles, Map<String, String> extras, String author, boolean locked) {
      load();
      String clean = name.strip();
      if (clean.isEmpty()) {
         clean = "Set " + (PRESETS.size() + 1);
      }

      if (clean.length() > 20) {
         clean = clean.substring(0, 20);
      }

      String finalName = clean;
      PRESETS.removeIf(p -> p.name().equalsIgnoreCase(finalName));
      PRESETS.add(new StylesPresets.Preset(clean, new EnumMap<>(styles), new TreeMap<>(extras), author, locked));
      save();
   }

   public static void remove(StylesPresets.Preset preset) {
      load();
      PRESETS.remove(preset);
      save();
   }

   public static String nextName() {
      load();
      int n = PRESETS.size() + 1;

      while (true) {
         String name = "Set " + n;
         if (PRESETS.stream().noneMatch(p -> p.name().equalsIgnoreCase(name))) {
            return name;
         }

         n++;
      }
   }

   private static Path file() {
      return FabricLoader.getInstance().getConfigDir().resolve("bauloo-styles-presets.json");
   }

   private static void load() {
      if (!loaded) {
         loaded = true;
         Path f = file();
         if (Files.exists(f)) {
            try {
               for (JsonElement element : JsonParser.parseString(Files.readString(f, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("presets")) {
                  JsonObject o = element.getAsJsonObject();
                  Map<BaulooStylesClient.Tab, String> styles = new EnumMap<>(BaulooStylesClient.Tab.class);
                  JsonObject s = o.getAsJsonObject("styles");

                  for (BaulooStylesClient.Tab tab : BaulooStylesClient.Tab.values()) {
                     if (s.has(tab.prefix) && s.get(tab.prefix).isJsonPrimitive()) {
                        styles.put(tab, s.get(tab.prefix).getAsString());
                     }
                  }

                  String author = o.has("author") && o.get("author").isJsonPrimitive() ? o.get("author").getAsString() : null;
                  boolean locked = o.has("locked") && o.get("locked").getAsBoolean();
                  PRESETS.add(new StylesPresets.Preset(o.get("name").getAsString(), styles, readExtras(o), author, locked));
               }
            } catch (RuntimeException | IOException var10) {
               BaulooStylesClient.LOGGER.warn("Could not read presets: {}", var10.toString());
            }
         }
      }
   }

   static Map<String, String> readExtras(JsonObject o) {
      Map<String, String> extras = new TreeMap<>();
      if (o.has("extras") && o.get("extras").isJsonObject()) {
         for (Entry<String, JsonElement> entry : o.getAsJsonObject("extras").entrySet()) {
            if (entry.getValue().isJsonPrimitive()) {
               extras.put(entry.getKey(), entry.getValue().getAsString());
            }
         }
      }

      return extras;
   }

   private static void save() {
      JsonArray list = new JsonArray();

      for (StylesPresets.Preset preset : PRESETS) {
         JsonObject o = new JsonObject();
         o.addProperty("name", preset.name());
         JsonObject s = new JsonObject();
         preset.styles().forEach((tab, folder) -> s.addProperty(tab.prefix, folder));
         o.add("styles", s);
         JsonObject e = new JsonObject();
         preset.extras().forEach(e::addProperty);
         o.add("extras", e);
         if (preset.author() != null) {
            o.addProperty("author", preset.author());
         }

         if (preset.locked()) {
            o.addProperty("locked", true);
         }

         list.add(o);
      }

      JsonObject root = new JsonObject();
      root.add("presets", list);

      try {
         Files.writeString(file(), GSON.toJson((JsonElement)root), StandardCharsets.UTF_8);
      } catch (IOException var6) {
         BaulooStylesClient.LOGGER.warn("Could not save presets: {}", var6.toString());
      }
   }

   public record Preset(String name, Map<BaulooStylesClient.Tab, String> styles, Map<String, String> extras, String author, boolean locked) {
      public Preset(String name, Map<BaulooStylesClient.Tab, String> styles, Map<String, String> extras) {
         this(name, styles, extras, null, false);
      }
   }
}
