package de.bauloo.styles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class StylesPackFilter {
   public static final String[] CATEGORIES = new String[]{
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
      "inventory"
   };
   private static final String TEX = "assets/minecraft/textures/";
   private static final long MAX_UNPACKED = 419430400L;
   private static final String META = "{\"pack\": {\"description\": \"Bauloo uploaded style - by bauloo\", \"pack_format\": 75, \"min_format\": 65, \"max_format\": 9999}}\n";
   private static final Pattern OLD_ARMOR = Pattern.compile(
      "^assets/minecraft/textures/models/armor/(leather|chainmail|iron|gold|diamond|netherite|turtle)_layer_([12])(_overlay)?\\.png$"
   );
   private static final Pattern ARMOR_ITEM = Pattern.compile(
      "^(leather|chainmail|iron|gold|golden|diamond|netherite|turtle)_(helmet|chestplate|leggings|boots)\\.png$"
   );
   private static final Pattern TOOL_ITEM = Pattern.compile("^(wood|wooden|stone|iron|gold|golden|diamond|netherite|copper)_(sword|pickaxe|axe)\\.png$");
   private static final Pattern STONE = Pattern.compile("^(stone|coal_ore|iron_ore|gold_ore|copper_ore|diamond_ore|emerald_ore|lapis_ore|redstone_ore)\\.png$");
   private static final Pattern DEEPSLATE = Pattern.compile("^[a-z_]*deepslate[a-z_]*\\.png$");
   private static final Pattern HOTBAR = Pattern.compile(
      "^assets/minecraft/textures/gui/sprites/hud/hotbar(_selection|_offhand_left|_offhand_right|_attack_indicator_background|_attack_indicator_progress)?\\.png$"
   );

   private StylesPackFilter() {
   }

   public static String[] categories() {
      List<String> all = new ArrayList<>(List.of(CATEGORIES));

      for (CloudCategories.Def d : CloudCategories.all()) {
         all.add(d.id());
      }

      return all.toArray(new String[0]);
   }

   static String target(String rawName, String category) {
      String name = rawName.replace('\\', '/');
      if (name.startsWith("/") || name.contains("..")) {
         return null;
      }

      if (name.endsWith(".png.mcmeta")) {
         String png = target(name.substring(0, name.length() - ".mcmeta".length()), category);
         return png == null ? null : png + ".mcmeta";
      }

      String lower = name.toLowerCase(Locale.ROOT);
      switch (category) {
         case "crystal":
            if (lower.startsWith("assets/minecraft/textures/entity/end_crystal/")) {
               return name;
            }

            if (lower.equals("assets/minecraft/textures/entity/endercrystal/endercrystal.png")) {
               return "assets/minecraft/textures/entity/end_crystal/end_crystal.png";
            }

            if (lower.equals("assets/minecraft/textures/entity/endercrystal/endercrystal_beam.png")) {
               return "assets/minecraft/textures/entity/end_crystal/end_crystal_beam.png";
            }

            if (lower.equals("assets/minecraft/textures/item/end_crystal.png") || lower.equals("assets/minecraft/textures/items/end_crystal.png")) {
               return "assets/minecraft/textures/item/end_crystal.png";
            }
            break;
         case "anchor":
            if (lower.startsWith("assets/minecraft/textures/block/respawn_anchor_")) {
               return name;
            }
            break;
         case "armor":
            if (lower.startsWith("assets/minecraft/textures/entity/equipment/humanoid/")
               || lower.startsWith("assets/minecraft/textures/entity/equipment/humanoid_leggings/")) {
               return name;
            }

            Matcher old = OLD_ARMOR.matcher(lower);
            if (old.matches()) {
               String material = switch (old.group(1)) {
                  case "turtle" -> "turtle_scute";
                  default -> old.group(1);
               };
               String folder = "1".equals(old.group(2)) ? "humanoid" : "humanoid_leggings";
               String overlay = old.group(3) == null ? "" : "_overlay";
               return "assets/minecraft/textures/entity/equipment/" + folder + "/" + material + overlay + ".png";
            }

            for (String items : new String[]{"assets/minecraft/textures/item/", "assets/minecraft/textures/items/"}) {
               if (lower.startsWith(items)) {
                  String file = lower.substring(items.length());
                  if (ARMOR_ITEM.matcher(file).matches()) {
                     return "assets/minecraft/textures/item/" + file.replace("gold_", "golden_");
                  }

                  if (file.startsWith("empty_armor_slot_")) {
                     return "assets/minecraft/textures/item/" + file;
                  }
               }
            }
            break;
         case "fire":
            if (lower.equals("assets/minecraft/textures/block/fire_0.png") || lower.equals("assets/minecraft/textures/block/fire_1.png")) {
               return name;
            }

            if (lower.equals("assets/minecraft/textures/blocks/fire_layer_0.png")) {
               return "assets/minecraft/textures/block/fire_0.png";
            }

            if (lower.equals("assets/minecraft/textures/blocks/fire_layer_1.png")) {
               return "assets/minecraft/textures/block/fire_1.png";
            }
            break;
         case "sky":
            if (lower.startsWith("assets/minecraft/optifine/sky/")) {
               return name;
            }

            if (lower.startsWith("assets/minecraft/mcpatcher/sky/")) {
               return "assets/minecraft/optifine/sky/" + name.substring("assets/minecraft/mcpatcher/sky/".length());
            }
            break;
         case "obsidian":
            for (String blocks : new String[]{"assets/minecraft/textures/block/", "assets/minecraft/textures/blocks/"}) {
               if (lower.equals(blocks + "obsidian.png") || lower.equals(blocks + "crying_obsidian.png")) {
                  return "assets/minecraft/textures/block/" + lower.substring(blocks.length());
               }
            }
            break;
         case "glowstone":
            for (String blocks : new String[]{"assets/minecraft/textures/block/", "assets/minecraft/textures/blocks/"}) {
               if (lower.equals(blocks + "glowstone.png")) {
                  return "assets/minecraft/textures/block/glowstone.png";
               }
            }
            break;
         case "gap":
            for (String items : new String[]{"assets/minecraft/textures/item/", "assets/minecraft/textures/items/"}) {
               if (lower.equals(items + "golden_apple.png") || lower.equals(items + "apple_golden.png")) {
                  return "assets/minecraft/textures/item/golden_apple.png";
               }

               if (lower.equals(items + "enchanted_golden_apple.png")) {
                  return "assets/minecraft/textures/item/enchanted_golden_apple.png";
               }
            }
            break;
         case "hotbar":
            if (HOTBAR.matcher(lower).matches()) {
               return name;
            }
            break;
         case "pearl":
            for (String items : new String[]{"assets/minecraft/textures/item/", "assets/minecraft/textures/items/"}) {
               if (lower.equals(items + "ender_pearl.png")) {
                  return "assets/minecraft/textures/item/ender_pearl.png";
               }
            }
            break;
         case "stone":
         case "deepslate":
            for (String blocks : new String[]{"assets/minecraft/textures/block/", "assets/minecraft/textures/blocks/"}) {
               if (lower.startsWith(blocks)) {
                  String file = lower.substring(blocks.length());
                  if (("stone".equals(category) ? STONE : DEEPSLATE).matcher(file).matches()) {
                     return "assets/minecraft/textures/block/" + file;
                  }
               }
            }
            break;
         case "snow":
            for (String blocks : new String[]{"assets/minecraft/textures/block/", "assets/minecraft/textures/blocks/"}) {
               if (lower.equals(blocks + "snow.png") || lower.equals(blocks + "powder_snow.png")) {
                  return "assets/minecraft/textures/block/" + lower.substring(blocks.length());
               }

               if (lower.equals(blocks + "grass_block_snow.png") || lower.equals(blocks + "grass_side_snowed.png")) {
                  return "assets/minecraft/textures/block/grass_block_snow.png";
               }
            }
            break;
         case "inventory":
            if ((lower.startsWith("assets/minecraft/textures/gui/container/") || lower.startsWith("assets/minecraft/textures/gui/sprites/container/"))
               && lower.endsWith(".png")) {
               return name;
            }
            break;
         case "totem":
            for (String items : new String[]{"assets/minecraft/textures/item/", "assets/minecraft/textures/items/"}) {
               if (lower.equals(items + "totem_of_undying.png") || lower.equals(items + "totem.png")) {
                  return "assets/minecraft/textures/item/totem_of_undying.png";
               }
            }
            break;
         case "sword":
         case "pickaxe":
         case "axe":
            for (String items : new String[]{"assets/minecraft/textures/item/", "assets/minecraft/textures/items/"}) {
               if (lower.startsWith(items)) {
                  Matcher tool = TOOL_ITEM.matcher(lower.substring(items.length()));
                  if (tool.matches() && tool.group(2).equals(category)) {
                     String material = switch (tool.group(1)) {
                        case "gold" -> "golden";
                        case "wood" -> "wooden";
                        default -> tool.group(1);
                     };
                     return "assets/minecraft/textures/item/" + material + "_" + category + ".png";
                  }
               }
            }
            break;
         default:
            return CloudCategories.target(lower, category);
      }

      return null;
   }

   public static Map<String, Integer> analyze(Path zip) throws IOException {
      Map<String, Integer> found = new LinkedHashMap<>();
      String[] categories = categories();

      for (String category : categories) {
         found.put(category, 0);
      }

      ZipEntry entry;
      try (ZipInputStream in = new ZipInputStream(Files.newInputStream(zip))) {
         while ((entry = in.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
               for (String category : categories) {
                  String t = target(entry.getName(), category);
                  if (t != null && t.endsWith(".png")) {
                     found.merge(category, 1, Integer::sum);
                  }
               }
            }
         }
      }

      return found;
   }

   public static int extract(Path zip, String category, Path destination) throws IOException {
      long unpacked = 0L;
      int textures = 0;
      Set<String> written = new HashSet<>();
      byte[] buffer = new byte[65536];
      Path temp = Files.createTempFile(destination.getParent(), "filter-", ".zip");

      try (
         ZipInputStream in = new ZipInputStream(Files.newInputStream(zip));
         ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(temp));
      ) {
         ZipEntry entry;
         while ((entry = in.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
               String t = target(entry.getName(), category);
               if (t != null && written.add(t.toLowerCase(Locale.ROOT))) {
                  out.putNextEntry(new ZipEntry(t));

                  int read;
                  while ((read = in.read(buffer)) > 0) {
                     unpacked += read;
                     if (unpacked > 419430400L) {
                        throw new IOException("Pack is too big once unpacked");
                     }

                     out.write(buffer, 0, read);
                  }

                  out.closeEntry();
                  if (t.endsWith(".png")) {
                     textures++;
                  }
               }
            }
         }

         out.putNextEntry(new ZipEntry("pack.mcmeta"));
         out.write(
            "{\"pack\": {\"description\": \"Bauloo uploaded style - by bauloo\", \"pack_format\": 75, \"min_format\": 65, \"max_format\": 9999}}\n"
               .getBytes(StandardCharsets.UTF_8)
         );
         out.closeEntry();
      } catch (IOException e) {
         Files.deleteIfExists(temp);
         throw e;
      }

      if (textures == 0) {
         Files.deleteIfExists(temp);
         return 0;
      } else {
         Files.move(temp, destination, StandardCopyOption.REPLACE_EXISTING);
         return textures;
      }
   }
}
