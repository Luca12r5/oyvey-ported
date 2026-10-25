package de.bauloo.styles;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CloudCategories {
   public static final int SLOTS = 8;
   private static final Pattern ID = Pattern.compile("^[a-z][a-z0-9]{1,15}$");
   private static final Pattern ICON = Pattern.compile("^[a-z0-9_.-]+:[a-z0-9_./-]+$");
   private static final Pattern PATH = Pattern.compile("^assets/[a-z0-9_./$-]+$");
   public static final Set<String> BUILT_IN = Set.of(
      "crystal",
      "anchor",
      "armor",
      "fire",
      "sky",
      "obsidian",
      "sword",
      "pickaxe",
      "glowstone",
      "gap",
      "pearl",
      "hotbar",
      "axe",
      "totem",
      "stone",
      "deepslate",
      "snow",
      "grass",
      "inventory",
      "cosmetics",
      "extra",
      "upload",
      "cloud",
      "mypack"
   );
   private static volatile List<CloudCategories.Def> defs = List.of();

   private CloudCategories() {
   }

   public static List<CloudCategories.Def> all() {
      return defs;
   }

   public static List<CloudCategories.Def> visible() {
      boolean early = VipPacks.earlyActive();
      boolean admin = VipPacks.admin();
      List<CloudCategories.Def> out = new ArrayList<>();

      for (CloudCategories.Def d : defs) {
         if ((!d.early() || early) && (!d.admin() || admin)) {
            out.add(d);
         }
      }

      return out;
   }

   public static CloudCategories.Def get(String id) {
      for (CloudCategories.Def d : defs) {
         if (d.id().equals(id)) {
            return d;
         }
      }

      return null;
   }

   public static boolean known(String category) {
      return BUILT_IN.contains(category) || get(category) != null;
   }

   public static boolean read(JsonObject root) {
      List<CloudCategories.Def> parsed = new ArrayList<>();
      Set<String> ids = new HashSet<>();
      Set<Integer> slots = new HashSet<>();

      for (String key : new String[]{"categories", "adminCategories"}) {
         if (root.has(key) && root.get(key).isJsonArray()) {
            for (JsonElement e : root.getAsJsonArray(key)) {
               try {
                  CloudCategories.Def d = parse(e.getAsJsonObject(), key.equals("adminCategories"));
                  if (d != null && ids.add(d.id()) && slots.add(d.slot())) {
                     parsed.add(d);
                  }
               } catch (RuntimeException var11) {
               }
            }
         }
      }

      parsed.sort((a, b) -> Integer.compare(a.slot(), b.slot()));
      boolean changed = !ids(parsed).equals(ids(defs));
      defs = List.copyOf(parsed);
      return changed;
   }

   public static void readJson(String json) {
      read(JsonParser.parseString(json).getAsJsonObject());
   }

   private static List<String> ids(List<CloudCategories.Def> list) {
      List<String> out = new ArrayList<>();

      for (CloudCategories.Def d : list) {
         out.add(d.slot() + ":" + d.id() + ":" + d.name() + ":" + d.icon());
      }

      return out;
   }

   private static CloudCategories.Def parse(JsonObject o, boolean admin) {
      String id = str(o, "id");
      String name = str(o, "name");
      int slot = o.has("slot") ? o.get("slot").getAsInt() : -1;
      if (id != null && ID.matcher(id).matches() && !BUILT_IN.contains(id) && slot >= 1 && slot <= 8 && name != null && !name.isBlank() && name.length() <= 20) {
         String icon = str(o, "icon");
         if (icon == null || !ICON.matcher(icon).matches()) {
            icon = "minecraft:paper";
         }

         JsonArray files = o.has("files") && o.get("files").isJsonArray() ? o.getAsJsonArray("files") : new JsonArray();
         List<CloudCategories.Rule> rules = new ArrayList<>();

         for (JsonElement f : files) {
            if (rules.size() >= 32 || !f.isJsonObject()) {
               break;
            }

            String match = str(f.getAsJsonObject(), "match");
            String to = str(f.getAsJsonObject(), "to");
            if (match != null && to != null && match.length() <= 300 && PATH.matcher(to).matches() && !to.contains("..")) {
               rules.add(new CloudCategories.Rule(Pattern.compile(match), to));
            }
         }

         if (rules.isEmpty()) {
            return null;
         }

         String detect = str(o, "detect");
         String type = "item";
         String top = null;
         String side = null;
         double[] crop = null;
         if (o.has("preview") && o.get("preview").isJsonObject()) {
            JsonObject p = o.getAsJsonObject("preview");
            type = "block".equals(str(p, "type")) ? "block" : "item";
            top = str(p, type.equals("block") ? "top" : "texture");
            side = str(p, "side");
            if (p.has("crop") && p.get("crop").isJsonArray() && p.getAsJsonArray("crop").size() == 4) {
               JsonArray c = p.getAsJsonArray("crop");
               double[] r = new double[]{c.get(0).getAsDouble(), c.get(1).getAsDouble(), c.get(2).getAsDouble(), c.get(3).getAsDouble()};
               if (r[0] >= 0.0 && r[1] >= 0.0 && r[2] > 0.0 && r[3] > 0.0 && r[0] + r[2] <= 1.0001 && r[1] + r[3] <= 1.0001) {
                  crop = r;
               }
            }
         }

         boolean early = o.has("early") && o.get("early").isJsonPrimitive() && o.get("early").getAsBoolean();
         return new CloudCategories.Def(id, slot, name.trim(), icon, List.copyOf(rules), detect, type, clean(top), clean(side), crop, early, admin);
      } else {
         return null;
      }
   }

   private static String clean(String path) {
      return path != null && PATH.matcher(path.toLowerCase(Locale.ROOT)).matches() && !path.contains("..") ? path.toLowerCase(Locale.ROOT) : null;
   }

   private static String str(JsonObject o, String key) {
      return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsString() : null;
   }

   public static String target(String lowerPath, String category) {
      CloudCategories.Def d = get(category);
      if (d == null) {
         return null;
      }

      for (CloudCategories.Rule r : d.rules()) {
         Matcher m = r.match().matcher(lowerPath);
         if (m.matches()) {
            try {
               String to = m.replaceFirst(r.to());
               if (PATH.matcher(to).matches() && !to.contains("..")) {
                  return to;
               }
            } catch (RuntimeException var7) {
            }
         }
      }

      return null;
   }

   public record Def(
      String id,
      int slot,
      String name,
      String icon,
      List<CloudCategories.Rule> rules,
      String detect,
      String previewType,
      String previewTop,
      String previewSide,
      double[] crop,
      boolean early,
      boolean admin
   ) {
   }

   public record Rule(Pattern match, String to) {
   }
}
