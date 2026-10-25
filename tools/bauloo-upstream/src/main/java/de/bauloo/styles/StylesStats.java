package de.bauloo.styles;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import net.minecraft.class_310;

public final class StylesStats {
   private static final long EVERY_MS = 600000L;
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();
   private static volatile long nextPing;

   private StylesStats() {
   }

   public static void tick() {
      long now = System.currentTimeMillis();
      if (now >= nextPing) {
         String base = StylesCloud.publishUrl();
         if (base == null) {
            nextPing = now + 30000L;
         } else {
            nextPing = now + 600000L;
            Thread thread = new Thread(() -> ping(base), "Bauloo Styles stats");
            thread.setDaemon(true);
            thread.start();
         }
      }
   }

   public static String deviceHash() {
      try {
         byte[] hash = MessageDigest.getInstance("SHA-256").digest(("bauloo-styles:" + StylesUploadServer.deviceId()).getBytes(StandardCharsets.UTF_8));
         return HexFormat.of().formatHex(hash);
      } catch (NoSuchAlgorithmException e) {
         throw new IllegalStateException(e);
      }
   }

   private static void ping(String base) {
      try {
         JsonObject body = new JsonObject();
         body.addProperty("id", deviceHash());
         body.addProperty("name", class_310.method_1551().method_1548().method_1676());
         HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/ping"))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(15L))
            .POST(BodyPublishers.ofString(body.toString()))
            .build();
         HTTP.send(request, BodyHandlers.discarding());
      } catch (Exception var3) {
      }
   }

   public static JsonObject fetch() {
      String base = StylesCloud.publishUrl();
      if (base == null) {
         return null;
      }

      try {
         HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/stats")).timeout(Duration.ofSeconds(10L)).GET().build();
         HttpResponse<String> response = HTTP.send(request, BodyHandlers.ofString());
         return response.statusCode() == 200 ? JsonParser.parseString(response.body()).getAsJsonObject() : null;
      } catch (Exception e) {
         return null;
      }
   }
}
