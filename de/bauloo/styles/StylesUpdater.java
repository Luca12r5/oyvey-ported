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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StylesUpdater implements PreLaunchEntrypoint {
   public static final Logger LOGGER = LoggerFactory.getLogger("Bauloo Styles Updater");
   static final String STYLES_ID = "bauloo_styles";
   static final String VERSION_URL = "https://raw.githubusercontent.com/Bauloo0/bauloo-styles-cloud/main/release/version.json";
   public static volatile String availableVersion;
   public static volatile List<String[]> availableNotes = List.of();
   public static volatile String installedVersion;

   @Override
   public void onPreLaunch() {
      Thread look = new Thread(StylesUpdater::check, "Bauloo Version Check");
      look.setDaemon(true);
      look.start();
   }

   private static List<String[]> notes(JsonObject json) {
      List<String[]> out = new ArrayList<>();

      try {
         if (json.has("notes") && json.get("notes").isJsonArray()) {
            for (JsonElement e : json.getAsJsonArray("notes")) {
               JsonArray row = e.getAsJsonArray();
               String tag = row.get(0).getAsString().toUpperCase(Locale.ROOT);
               String text = row.get(1).getAsString();
               if (tag.matches("NEW|FIX|GONE") && text.length() <= 60 && out.size() < 6) {
                  out.add(new String[]{tag, text});
               }
            }
         }
      } catch (RuntimeException var7) {
      }

      return out;
   }

   private static void check() {
      try {
         Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer("bauloo_styles");
         if (mod.isEmpty()) {
            return;
         }

         Version installed = mod.get().getMetadata().getVersion();
         installedVersion = installed.getFriendlyString();
         HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();
         HttpResponse<String> info = http.send(
            HttpRequest.newBuilder(
                  URI.create("https://raw.githubusercontent.com/Bauloo0/bauloo-styles-cloud/main/release/version.json?t=" + System.currentTimeMillis() / 60000L)
               )
               .timeout(Duration.ofSeconds(15L))
               .GET()
               .build(),
            BodyHandlers.ofString()
         );
         if (info.statusCode() != 200) {
            LOGGER.info("[Updater] version check answered {}", info.statusCode());
            return;
         }

         JsonObject json = JsonParser.parseString(info.body()).getAsJsonObject();
         String latestText = json.get("version").getAsString();
         if (SemanticVersion.parse(latestText).compareTo(installed) > 0) {
            availableNotes = notes(json);
            availableVersion = latestText;
            LOGGER.info("[Updater] Bauloo Styles {} is out (you have {})", latestText, installed.getFriendlyString());
         } else {
            LOGGER.info("[Updater] Bauloo Styles {} is up to date", installed.getFriendlyString());
         }
      } catch (Exception var6) {
         LOGGER.info("[Updater] version check skipped: {}", var6.toString());
      }
   }
}
