package de.bauloo.styles;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

public final class WhatsNew {
   private WhatsNew() {
   }

   public static String installed() {
      return FabricLoader.getInstance().getModContainer("bauloo_styles").map(m -> m.getMetadata().getVersion().getFriendlyString().split("\\+")[0]).orElse("");
   }

   public static WhatsNew.Notes due() {
      StylesUiConfig.load();
      String now = installed();
      String last = StylesUiConfig.lastVersion;
      boolean audit = Boolean.getBoolean("bauloo.audit");
      if (audit || !now.isEmpty() && !now.equals(last)) {
         boolean existingUser = audit || StylesUiConfig.hadFile();
         StylesUiConfig.lastVersion = now;
         StylesUiConfig.save();

         try {
            Object root;
            try (InputStream in = WhatsNew.class.getResourceAsStream("/assets/bauloo_styles/whatsnew.json")) {
               if (in == null) {
                  return null;
               }

               String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
               String fingerprint = Integer.toHexString(text.replaceAll("\\s+", "").hashCode());
               boolean seen = fingerprint.equals(StylesUiConfig.lastNotes);
               StylesUiConfig.lastNotes = fingerprint;
               StylesUiConfig.save();
               if (existingUser && (!seen || audit)) {
                  JsonObject rootx = JsonParser.parseString(text).getAsJsonObject();
                  JsonObject hero = rootx.getAsJsonObject("hero");
                  List<String[]> items = new ArrayList<>();

                  for (JsonElement e : rootx.getAsJsonArray("items")) {
                     JsonArray a = e.getAsJsonArray();
                     items.add(new String[]{a.get(0).getAsString(), a.get(1).getAsString()});
                  }

                  String cape = hero.has("cape") ? hero.get("cape").getAsString() : "";
                  String open = rootx.has("open") ? rootx.get("open").getAsString() : "cosmetics";
                  return new WhatsNew.Notes(last, now, cape, hero.get("title").getAsString(), hero.get("text").getAsString(), open, items);
               }

               root = null;
            }

            return (WhatsNew.Notes)root;
         } catch (Exception var16) {
            BaulooStylesClient.LOGGER.warn("What's new could not be read: {}", var16.toString());
            return null;
         }
      } else {
         return null;
      }
   }

   public record Notes(String from, String to, String heroCape, String heroTitle, String heroText, String open, List<String[]> items) {
   }
}
