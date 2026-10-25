package de.bauloo.styles;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;

public final class StylesReview {
   private static final String PAGE = "https://baulooprac.xyz/review.html#t=";
   private static final boolean PUBLIC = true;
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();
   private static volatile boolean busy;
   private static volatile String status;
   private static volatile long statusUntil;
   private static Boolean ownerPc;

   private StylesReview() {
   }

   public static boolean shown() {
      return true;
   }

   public static boolean busy() {
      return busy;
   }

   public static String status() {
      return System.currentTimeMillis() < statusUntil ? status : null;
   }

   private static void say(String text, long ms) {
      status = text;
      statusUntil = System.currentTimeMillis() + ms;
   }

   public static void open() {
      if (!busy) {
         busy = true;
         say("Opening feedback ...", 30000L);
         Thread thread = new Thread(() -> {
            try {
               String token = verify();
               Minecraft.getInstance().execute(() -> dev.lego.compat.McCompat.openLink("https://baulooprac.xyz/review.html#t=" + token));
               say("Opened in your browser", 6000L);
            } catch (StylesReview.Failure var5) {
               say(var5.getMessage(), 8000L);
            } catch (Exception var6) {
               BaulooStylesClient.LOGGER.warn("[Review] failed: {}", var6.toString());
               say("Feedback is not reachable right now", 8000L);
            } finally {
               busy = false;
            }
         }, "Bauloo Styles review");
         thread.setDaemon(true);
         thread.start();
      }
   }

   private static String verify() throws Exception {
      String base = StylesCloud.publishUrl();
      if (base == null) {
         throw new StylesReview.Failure("Feedback is not reachable right now");
      } else {
         JsonObject body = new JsonObject();
         body.addProperty("device", StylesStats.deviceHash());
         JsonObject answer = post(base + "/review/anon", body.toString());
         if (!answer.has("token")) {
            throw new StylesReview.Failure(answer.has("message") ? answer.get("message").getAsString() : "Feedback is not reachable right now");
         } else {
            return answer.get("token").getAsString();
         }
      }
   }

   private static JsonObject post(String url, String json) throws Exception {
      HttpRequest request = HttpRequest.newBuilder(URI.create(url))
         .timeout(Duration.ofSeconds(15L))
         .header("Content-Type", "application/json")
         .POST(BodyPublishers.ofString(json))
         .build();
      HttpResponse<String> response = HTTP.send(request, BodyHandlers.ofString());
      JsonObject answer = JsonParser.parseString(response.body()).getAsJsonObject();
      if (response.statusCode() != 200 && answer.has("message")) {
         throw new StylesReview.Failure(answer.get("message").getAsString());
      } else {
         return answer;
      }
   }

   private static final class Failure extends Exception {
      Failure(String message) {
         super(message);
      }
   }
}
