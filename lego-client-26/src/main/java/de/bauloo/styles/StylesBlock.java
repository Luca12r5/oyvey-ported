package de.bauloo.styles;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;

public final class StylesBlock {
   private static final String URL = "https://raw.githubusercontent.com/Bauloo0/bauloo-styles-cloud/main/blocked.json";
   private static volatile List<String> ids = List.of();
   private static volatile List<String> names = List.of();
   private static boolean packsCleared;

   private StylesBlock() {
   }

   private static Path cache() {
      return FabricLoader.getInstance().getConfigDir().resolve("bauloo-styles").resolve("blocked.json");
   }

   public static void start() {
      try {
         if (Files.exists(cache())) {
            read(Files.readString(cache()));
         }
      } catch (Exception var1) {
      }

      Thread t = new Thread(
         () -> {
            try {
               HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();
               HttpResponse<String> r = http.send(
                  HttpRequest.newBuilder(
                        URI.create("https://raw.githubusercontent.com/Bauloo0/bauloo-styles-cloud/main/blocked.json?t=" + System.currentTimeMillis() / 60000L)
                     )
                     .timeout(Duration.ofSeconds(15L))
                     .GET()
                     .build(),
                  BodyHandlers.ofString()
               );
               if (r.statusCode() == 200) {
                  read(r.body());
                  Files.createDirectories(cache().getParent());
                  Files.writeString(cache(), r.body());
               } else if (r.statusCode() == 404) {
                  read("{}");
               }
            } catch (Exception var2) {
            }

            Minecraft.getInstance().schedule(StylesBlock::enforce);
         },
         "Bauloo Styles block list"
      );
      t.setDaemon(true);
      t.start();
   }

   private static void read(String json) {
      JsonObject o = JsonParser.parseString(json).getAsJsonObject();
      ids = list(o.get("uuids"), true);
      names = list(o.get("names"), false);
   }

   private static List<String> list(JsonElement e, boolean uuid) {
      List<String> out = new ArrayList<>();
      if (e instanceof JsonArray) {
         for (JsonElement x : (JsonArray)e) {
            String s = x.getAsString().toLowerCase(Locale.ROOT);
            out.add(uuid ? s.replace("-", "") : s);
         }
      }

      return List.copyOf(out);
   }

   public static boolean isBlocked() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.getUser() != null) {
         UUID uuid = mc.getUser().getProfileId();
         if (uuid != null && ids.contains(uuid.toString().replace("-", "").toLowerCase(Locale.ROOT))) {
            return true;
         } else {
            String name = mc.getUser().getName();
            return name != null && names.contains(name.toLowerCase(Locale.ROOT));
         }
      } else {
         return false;
      }
   }

   public static void enforce() {
      if (isBlocked() && !packsCleared) {
         packsCleared = true;
         Minecraft mc = Minecraft.getInstance();
         PackRepository packs = mc.getResourcePackRepository();
         List<String> enabled = new ArrayList<>(packs.getSelectedIds());
         boolean changed = enabled.removeIf(
            id -> id.startsWith("bauloo_styles:") || id.startsWith("file/bauloo_cloud_") || id.startsWith("file/bauloo_upload_")
         );
         if (changed) {
            packs.setSelected(enabled);
            mc.options.updateResourcePacks(packs);
            mc.reloadResourcePacks();
         }

         BaulooStylesClient.LOGGER.info("Bauloo Styles is not available for this account");
      }
   }
}
