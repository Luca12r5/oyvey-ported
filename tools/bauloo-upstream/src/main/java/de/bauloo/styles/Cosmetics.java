package de.bauloo.styles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_11890;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_8685;
import net.minecraft.class_12079.class_10726;
import net.minecraft.class_12079.class_12081;

public final class Cosmetics {
   public static final String[][] CAPES = new String[][]{
      {"tornado_night", "Tornado Night"},
      {"crimson", "Crimson"},
      {"vortex", "Vortex"},
      {"daydream", "Daydream"},
      {"snake_blade", "Snake Blade"},
      {"haunted_night", "Haunted Night"},
      {"pumpkin_king", "Pumpkin King"}
   };
   public static final String[][] MC_CAPES = new String[][]{
      {"minecon2011", "MINECON 2011", "953cac8b779fe41383e675ee2b86071a71658f2180f56fbce8aa315ea70e2ed6"},
      {"minecon2012", "MINECON 2012", "a2e8d97ec79100e90a75d369d1b3ba81273c4f82bc1b737e934eed4a854be1b6"},
      {"minecon2013", "MINECON 2013", "153b1a0dfcbae953cdeb6f2c2bf6bf79943239b1372780da44bcbb29273131da"},
      {"minecon2015", "MINECON 2015", "b0cc08840700447322d953a02b965f1d65a13a603bf64b17c803c21446fe1635"},
      {"minecon2016", "MINECON 2016", "e7dfea16dc83c97df01a12fabbd1216359c0cd0ea42f9999b6e97c584963e980"},
      {"migrator", "Migrator", "2340c0e03dd24a11b15a8b33c2a7e9e32abb2051b2481d0ba7defd635ca7a933"},
      {"vanilla", "Vanilla", "f9a76537647989f9a0b6d001e320dac591c359e9e61a31f4ce11c88f207f0ad4"},
      {"cherry", "Cherry Blossom", "afd553b39358a24edfe3b8a9a939fa5fa4faa4d9a9c3d6af8eafb377fa05c2bb"},
      {"anniversary", "15th Anniversary", "cd9d82ab17fd92022dbd4a86cde4c382a7540e117fae7b9a2853658505a80625"},
      {"pan", "Pan", "28de4a81688ad18b49e735a273e086c18f1e3966956123ccb574034c06f5d336"},
      {"common", "Common", "5ec930cdd2629c8771655c60eebeb867b4b6559b0e6d3bc71c40c96347fa03f0"},
      {"purpleheart", "Purple Heart", "cb40a92e32b57fd732a00fc325e7afb00a7ca74936ad50d8e860152e482cfbde"},
      {"follower", "Follower's", "569b7f2a1d00d26f30efe3f9ab9ac817b1e6d35f4f3cfb0324ef2d328223d350"},
      {"mcc", "MCC 15th Year", "56c35628fe1c4d59dd52561a3d03bfa4e1a76d397c8b9c476c2f77cb6aebb1df"},
      {"founder", "Founder's", "99aba02ef05ec6aa4d42db8ee43796d6cd50e4b2954ab29f0caeb85f96bf52a1"},
      {"menace", "Menace", "dbc21e222528e30dc88445314f7be6ff12d3aeebc3c192054fba7e3b3f8c77b1"},
      {"home", "Home", "1de21419009db483900da6298a1e6cbf9f1bc1523a0dcdc16263fab150693edd"},
      {"yearn", "Yearn", "308b32a9e303155a0b4262f9e5483ad4a22e3412e84fe8385a0bdd73dc41fa89"},
      {"copper", "Copper", "5e6f3193e74cd16cdd6637d9bae5484e3a37ff2a14c2d157c659a07810b1bdca"},
      {"zombiehorse", "Zombie Horse", "a3f6e4f14801f3ea55e3d95b9b4ef3b5e8802d947f669de93d6ec4b9354a436b"},
      {"builder", "Builder", "2c579968c64c1719740fd8c2a451461879b238002574fce48f7d1a7c36a1c7d4"},
      {"experience", "Minecraft Experience", "7658c5025c77cfac7574aab3af94a46a8886e3b7722a895255fbf22ab8652434"}
   };
   public static final String MC = "mc_";
   public static final String[][] HEADS = new String[][]{{"halo", "Halo"}, {"tophat", "Top Hat"}, {"pumpkin_head", "Pumpkin Head"}, {"witch_hat", "Witch Hat"}};
   public static final String[][] AURAS = new String[][]{{"sparkles", "Sparkles"}, {"bats", "Bats"}, {"souls", "Souls"}};
   private static final Map<Cosmetics.Slot, String> TRY_ON = new HashMap<>();
   private static String[][] merged = CAPES;
   private static String[][] mergedLocal;
   public static final class_2960 WHITE = class_2960.method_60655("bauloo_styles", "textures/cosmetics/white.png");
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).followRedirects(Redirect.NORMAL).build();
   private static final Map<String, class_2960> MC_READY = new ConcurrentHashMap<>();
   private static final Set<String> MC_LOADING = ConcurrentHashMap.newKeySet();
   public static final Map<String, Integer> MC_WIDTH = new ConcurrentHashMap<>();
   private static int colorCapeRgb = -1;
   private static final class_2960 COLOR_CAPE = class_2960.method_60655("bauloo_styles", "cosmetics/color_cape");

   private Cosmetics() {
   }

   private static String[][] withLocal() {
      String[][] local = LocalCapes.list();
      if (local != mergedLocal) {
         mergedLocal = local;
         String[][] all = new String[local.length + CAPES.length][];
         System.arraycopy(local, 0, all, 0, local.length);
         System.arraycopy(CAPES, 0, all, local.length, CAPES.length);
         merged = all;
      }

      return merged;
   }

   public static String[][] of(Cosmetics.Slot slot) {
      return switch (slot) {
         case CAPE -> withLocal();
         case MC_CAPE -> MC_CAPES;
         case HEAD -> HEADS;
         case AURA -> AURAS;
      };
   }

   public static String saved(Cosmetics.Slot slot) {
      StylesUiConfig.load();

      return switch (slot) {
         case CAPE -> StylesUiConfig.cosCape.startsWith("mc_") ? "none" : StylesUiConfig.cosCape;
         case MC_CAPE -> StylesUiConfig.cosCape.startsWith("mc_") ? StylesUiConfig.cosCape.substring("mc_".length()) : "none";
         case HEAD -> StylesUiConfig.cosHead;
         case AURA -> StylesUiConfig.cosAura;
      };
   }

   public static void save(Cosmetics.Slot slot, String key) {
      switch (slot) {
         case CAPE:
            StylesUiConfig.cosCape = key;
            break;
         case MC_CAPE:
            StylesUiConfig.cosCape = "none".equals(key) ? "none" : "mc_" + key;
            break;
         case HEAD:
            StylesUiConfig.cosHead = key;
            break;
         case AURA:
            StylesUiConfig.cosAura = key;
      }

      StylesUiConfig.save();
   }

   public static class_2960 capeFor(class_11890 player) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 == null) {
         return null;
      } else {
         return player == mc.field_1724 ? currentCapeTexture() : PlayerCapes.texture(player.method_5477().getString());
      }
   }

   public static class_8685 withCape(class_11890 player, class_8685 old) {
      class_2960 texture = old == null ? null : capeFor(player);
      if (texture == null) {
         return old;
      } else {
         boolean me = player == class_310.method_1551().field_1724;
         if (old.comp_1627() == null
            || !texture.equals(old.comp_1627().comp_3627())
            || me && StylesUiConfig.cosCapeElytra && old.comp_1628() != old.comp_1627()) {
            class_12081 cape = new class_10726(texture, texture);
            return new class_8685(old.comp_1626(), cape, me && StylesUiConfig.cosCapeElytra ? cape : old.comp_1628(), old.comp_1629(), old.comp_1630());
         } else {
            return old;
         }
      }
   }

   public static String current(Cosmetics.Slot slot) {
      String trying = TRY_ON.get(slot);
      String key = trying != null ? trying : saved(slot);
      return valid(slot, key) ? key : "none";
   }

   public static class_2960 currentCapeTexture() {
      String own = TRY_ON.get(Cosmetics.Slot.CAPE);
      String mc = TRY_ON.get(Cosmetics.Slot.MC_CAPE);
      if (own == null && mc == null) {
         String saved = saved(Cosmetics.Slot.CAPE);
         String savedMc = saved(Cosmetics.Slot.MC_CAPE);
         if (valid(Cosmetics.Slot.MC_CAPE, savedMc)) {
            return mcCape(savedMc);
         } else {
            return valid(Cosmetics.Slot.CAPE, saved) ? capeTexture(saved) : null;
         }
      } else if (mc != null) {
         return valid(Cosmetics.Slot.MC_CAPE, mc) ? mcCape(mc) : null;
      } else {
         return valid(Cosmetics.Slot.CAPE, own) ? capeTexture(own) : null;
      }
   }

   public static void tryOn(Cosmetics.Slot slot, String key) {
      TRY_ON.clear();
      if (slot != null) {
         TRY_ON.put(slot, key);
      }
   }

   public static boolean valid(Cosmetics.Slot slot, String key) {
      if (key == null) {
         return false;
      }

      for (String[] entry : of(slot)) {
         if (entry[0].equals(key)) {
            return true;
         }
      }

      return false;
   }

   public static String label(Cosmetics.Slot slot, String key) {
      for (String[] entry : of(slot)) {
         if (entry[0].equals(key)) {
            return entry[1];
         }
      }

      return "None";
   }

   public static int color() {
      StylesUiConfig.load();
      if (StylesUiConfig.cosRainbow) {
         float hue = (float)(System.currentTimeMillis() % 6000L) / 6000.0F;
         return StylesTheme.hsv(hue, 0.55F, 1.0F) & 16777215;
      } else {
         return StylesUiConfig.cosColor & 16777215;
      }
   }

   public static class_2960 icon(Cosmetics.Slot slot, String key) {
      if (slot == Cosmetics.Slot.MC_CAPE) {
         return mcCape(key);
      }

      String prefix = switch (slot) {
         case CAPE, MC_CAPE -> "cape_";
         case HEAD -> "head_";
         case AURA -> "aura_";
      };
      if (slot == Cosmetics.Slot.CAPE && "color".equals(key)) {
         return colorCape();
      } else if (slot == Cosmetics.Slot.CAPE && BuiltInCapes.has(key)) {
         class_2960 anim = BuiltInCapes.icon(key);
         return anim != null ? anim : WHITE;
      } else if (slot == Cosmetics.Slot.CAPE && key.startsWith("local_")) {
         class_2960 local = LocalCapes.icon(key);
         return local != null ? local : WHITE;
      } else {
         return class_2960.method_60655("bauloo_styles", "textures/cosmetics/icon/" + prefix + key + ".png");
      }
   }

   public static class_2960 capeTexture(String key) {
      if ("none".equals(key)) {
         return null;
      }

      if ("color".equals(key)) {
         return colorCape();
      }

      if (BuiltInCapes.has(key)) {
         try {
            class_2960 built = BuiltInCapes.current(key);
            if (built != null) {
               return built;
            }
         } catch (Throwable t) {
            BaulooStylesClient.LOGGER.warn("Cape {} failed: {}", key, t.toString());
         }

         return class_2960.method_60655("bauloo_styles", "textures/cosmetics/cape/bauloo.png");
      } else if (key.startsWith("local_")) {
         try {
            class_2960 local = LocalCapes.current(key);
            if (local != null) {
               return local;
            }
         } catch (Throwable t) {
            BaulooStylesClient.LOGGER.warn("Local cape {} failed: {}", key, t.toString());
         }

         return class_2960.method_60655("bauloo_styles", "textures/cosmetics/cape/bauloo.png");
      } else {
         return class_2960.method_60655("bauloo_styles", "textures/cosmetics/cape/" + key + ".png");
      }
   }

   public static class_2960 mcCape(String key) {
      class_2960 ready = MC_READY.get(key);
      if (ready == null && MC_LOADING.add(key)) {
         String hash = null;

         for (String[] entry : MC_CAPES) {
            if (entry[0].equals(key)) {
               hash = entry[2];
            }
         }

         if (hash == null) {
            return null;
         }

         String h = hash;
         CompletableFuture.runAsync(
            () -> {
               try {
                  Path file = FabricLoader.getInstance().getConfigDir().resolve("bauloo-styles/capes/" + h + ".png");
                  byte[] png;
                  if (Files.exists(file)) {
                     png = Files.readAllBytes(file);
                  } else {
                     HttpRequest request = HttpRequest.newBuilder(URI.create("https://textures.minecraft.net/texture/" + h))
                        .timeout(Duration.ofSeconds(20L))
                        .GET()
                        .build();
                     HttpResponse<byte[]> response = HTTP.send(request, BodyHandlers.ofByteArray());
                     if (response.statusCode() != 200 || response.body().length > 2000000) {
                        MC_LOADING.remove(key);
                        return;
                     }

                     png = response.body();
                     Files.createDirectories(file.getParent());
                     Files.write(file, png);
                  }

                  class_310.method_1551().method_63588(() -> {
                     try {
                        class_1011 image = class_1011.method_49277(png);
                        class_2960 id = class_2960.method_60655("bauloo_styles", "cosmetics/mc_" + key);
                        MC_WIDTH.put(key, image.method_4307());
                        class_310.method_1551().method_1531().method_4616(id, new class_1043(id::toString, image));
                        MC_READY.put(key, id);
                     } catch (IOException e) {
                        MC_LOADING.remove(key);
                     }
                  });
               } catch (Exception e) {
                  MC_LOADING.remove(key);
               }
            }
         );
         return null;
      } else {
         return ready;
      }
   }

   private static class_2960 colorCape() {
      int rgb = StylesUiConfig.cosColor & 16777215;
      if (rgb != colorCapeRgb) {
         colorCapeRgb = rgb;
         class_1011 img = new class_1011(128, 64, true);
         int light = StylesTheme.mix(0xFF000000 | rgb, -1, 0.25F);
         int deep = StylesTheme.mix(0xFF000000 | rgb, -16777216, 0.6F);

         for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 128; x++) {
               float t = x >= 44 ? y / 44.0F : Math.max(0.0F, Math.min(1.0F, (y - 2) / 32.0F));
               img.method_61941(x, y, 0xFF000000 | StylesTheme.mix(light, deep, t));
            }
         }

         for (int x = 2; x < 22; x++) {
            img.method_61941(x, 26, -1);
         }

         class_310.method_1551().method_1531().method_4616(COLOR_CAPE, new class_1043(COLOR_CAPE::toString, img));
      }

      return COLOR_CAPE;
   }

   public enum Slot {
      CAPE("Capes"),
      MC_CAPE("MC Capes"),
      HEAD("Head"),
      AURA("Aura");

      public final String title;

      Slot(String title) {
         this.title = title;
      }
   }
}
