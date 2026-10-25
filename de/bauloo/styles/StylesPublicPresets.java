package de.bauloo.styles;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class StylesPublicPresets {
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();
   private static volatile List<StylesPublicPresets.Shared> list = List.of();
   private static volatile boolean loading;
   private static volatile String status = "";
   private static volatile int version;
   private static volatile boolean admin;

   private StylesPublicPresets() {
   }

   public static boolean admin() {
      return admin;
   }

   public static List<StylesPublicPresets.Shared> all() {
      return list;
   }

   public static boolean loading() {
      return loading;
   }

   public static void say(String text) {
      status = text;
   }

   public static String status() {
      return status;
   }

   public static int version() {
      return version;
   }

   public static void refresh() {
      String base = StylesCloud.publishUrl();
      if (base != null && !loading) {
         loading = true;
         run(() -> admin = StylesUploadServer.isAdmin());
         run(
            () -> {
               try {
                  HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/presets?device=" + StylesStats.deviceHash()))
                     .timeout(Duration.ofSeconds(10L))
                     .GET()
                     .build();
                  HttpResponse<String> response = HTTP.send(request, BodyHandlers.ofString());
                  if (response.statusCode() == 200) {
                     List<StylesPublicPresets.Shared> result = new ArrayList<>();

                     for (JsonElement element : JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonArray("presets")) {
                        JsonObject o = element.getAsJsonObject();
                        Map<BaulooStylesClient.Tab, String> styles = new EnumMap<>(BaulooStylesClient.Tab.class);
                        JsonObject s = o.getAsJsonObject("styles");

                        for (BaulooStylesClient.Tab tab : BaulooStylesClient.Tab.values()) {
                           if (s.has(tab.prefix)) {
                              styles.put(tab, s.get(tab.prefix).getAsString());
                           }
                        }

                        result.add(
                           new StylesPublicPresets.Shared(
                              o.get("id").getAsInt(),
                              o.get("name").getAsString(),
                              o.get("author").getAsString(),
                              styles,
                              StylesPresets.readExtras(o),
                              o.has("mine") && o.get("mine").getAsBoolean(),
                              PresetShare.readLocal(o.has("local") && o.get("local").isJsonObject() ? o.getAsJsonObject("local") : null)
                           )
                        );
                     }

                     list = result;
                     version++;
                  }
               } catch (Exception var16) {
                  status = "Could not load public presets";
               } finally {
                  loading = false;
               }
            }
         );
      }
   }

   public static void share(StylesPresets.Preset preset) {
      String base = StylesCloud.publishUrl();
      if (base == null) {
         status = "Sharing is not set up yet";
      } else {
         status = "Sharing...";
         run(() -> {
            try {
               send(base, "POST", PresetShare.body(preset, sayer()));
            } catch (Exception var3) {
               status = var3.getMessage() == null ? "Could not reach the server" : var3.getMessage();
            }
         });
      }
   }

   private static Consumer<String> sayer() {
      return text -> status = text;
   }

   public static void remove(StylesPublicPresets.Shared shared) {
      String base = StylesCloud.publishUrl();
      if (base != null) {
         JsonObject body = new JsonObject();
         body.addProperty("id", shared.id());
         body.addProperty("device", StylesStats.deviceHash());
         send(base, "DELETE", body);
      }
   }

   private static void send(String base, String method, JsonObject body) {
      status = method.equals("POST") ? "Sharing..." : "Removing...";
      run(
         () -> {
            try {
               Builder builder = HttpRequest.newBuilder(URI.create(base + "/presets"));
               String key = StylesUploadServer.savedKey();
               if (!key.isEmpty()) {
                  builder.header("Authorization", "Bearer " + key);
               }

               HttpRequest request = builder.header("Content-Type", "application/json")
                  .timeout(Duration.ofSeconds(15L))
                  .method(method, BodyPublishers.ofString(body.toString()))
                  .build();
               HttpResponse<String> response = HTTP.send(request, BodyHandlers.ofString());
               JsonObject answer = JsonParser.parseString(response.body()).getAsJsonObject();
               status = answer.has("message") ? answer.get("message").getAsString() : "";
            } catch (Exception var8) {
               status = "Could not reach the server";
            }

            refresh();
         }
      );
   }

   private static void run(Runnable task) {
      Thread thread = new Thread(task, "Bauloo Styles presets");
      thread.setDaemon(true);
      thread.start();
   }

   public record Shared(
      int id,
      String name,
      String author,
      Map<BaulooStylesClient.Tab, String> styles,
      Map<String, String> extras,
      boolean mine,
      Map<BaulooStylesClient.Tab, PresetShare.LocalFile> local
   ) {
   }
}
