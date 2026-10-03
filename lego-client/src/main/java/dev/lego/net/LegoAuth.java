package dev.lego.net;

import dev.lego.LegoClient;
import dev.lego.core.Mc;
import dev.spotifyhud.Json;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.User;

/**
 * Signs the running client in to the LEGO server and reports minigame results
 * (the server turns them into LEGO Coins, with a daily cap).
 *
 * Sign-in uses the same handshake as joining a Minecraft server: the server
 * hands out a random server id, the client tells Mojang's session server that
 * it joins that id, and the LEGO server asks Mojang whether this player
 * joined. The Minecraft access token is only ever sent to Mojang.
 */
public final class LegoAuth {
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(6)).build();
   private static final String MOJANG_JOIN = "https://sessionserver.mojang.com/session/minecraft/join";

   private static volatile String token;
   private static volatile long expiresAt;
   private static volatile CompletableFuture<String> pending;
   /** Last coins awarded for a game, shown on the game-over screen. */
   public static volatile int lastCoins = -1;
   public static volatile String lastError;

   private LegoAuth() {
   }

   private static HttpRequest.Builder req(String base, String path) {
      return HttpRequest.newBuilder(URI.create(base + path)).timeout(Duration.ofSeconds(10)).header("User-Agent", "LEGO-Client").header("Content-Type", "application/json");
   }

   private static Object post(String base, String path, Object body, String bearer) throws Exception {
      HttpRequest.Builder b = req(base, path).POST(HttpRequest.BodyPublishers.ofString(Json.write(body)));
      if (bearer != null) b.header("Authorization", "Bearer " + bearer);
      HttpResponse<String> res = HTTP.send(b.build(), HttpResponse.BodyHandlers.ofString());
      if (res.statusCode() / 100 != 2) throw new IllegalStateException("HTTP " + res.statusCode());
      return res.body().isEmpty() ? null : Json.parse(res.body());
   }

   /** A valid LEGO session token, signing in when needed. Never blocks the render thread. */
   public static synchronized CompletableFuture<String> session() {
      if (token != null && System.currentTimeMillis() < expiresAt - 60_000L) return CompletableFuture.completedFuture(token);
      if (pending != null && !pending.isDone()) return pending;
      String base = LegoNet.INSTANCE == null ? "" : LegoNet.INSTANCE.baseUrl();
      if (base.isEmpty()) return CompletableFuture.failedFuture(new IllegalStateException("Kein LEGO-Server konfiguriert"));
      User user = Mc.mc().getUser();
      String name = user.getName();
      UUID profile = user.getProfileId();
      String mcToken = user.getAccessToken();
      pending = CompletableFuture.supplyAsync(() -> {
         try {
            Object ch = post(base, "/api/auth/challenge", Map.of(), null);
            String challengeId = Json.str(ch, "challengeId", null);
            String serverId = Json.str(ch, "serverId", null);
            if (challengeId == null || serverId == null) throw new IllegalStateException("Ungültige Antwort");
            Map<String, Object> join = new LinkedHashMap<>();
            join.put("accessToken", mcToken);
            join.put("selectedProfile", profile.toString().replace("-", ""));
            join.put("serverId", serverId);
            HttpRequest j = HttpRequest.newBuilder(URI.create(MOJANG_JOIN)).timeout(Duration.ofSeconds(10)).header("Content-Type", "application/json")
               .POST(HttpRequest.BodyPublishers.ofString(Json.write(join))).build();
            int code = HTTP.send(j, HttpResponse.BodyHandlers.discarding()).statusCode();
            if (code / 100 != 2) throw new IllegalStateException("Mojang-Join abgelehnt (" + code + ")");
            Object v = post(base, "/api/auth/verify", Map.of("challengeId", challengeId, "username", name), null);
            String t = Json.str(v, "token", null);
            if (t == null) throw new IllegalStateException("Kein Token erhalten");
            token = t;
            expiresAt = Json.num(v, "expiresAt", System.currentTimeMillis() + 3_600_000L);
            lastError = null;
            return t;
         } catch (Exception e) {
            lastError = e.getMessage();
            throw new RuntimeException(e);
         }
      });
      return pending;
   }

   /** Sends a finished minigame to the server. Fire and forget; failures are only logged. */
   public static void reportGame(String gameId, long score, boolean record) {
      String id = gameId == null ? "" : gameId.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
      if (id.length() < 2 || id.length() > 32 || score < 0) return;
      String base = LegoNet.INSTANCE == null ? "" : LegoNet.INSTANCE.baseUrl();
      if (base.isEmpty()) return;
      lastCoins = -1;
      session().thenAcceptAsync(t -> {
         try {
            Object r = post(base, "/api/me/game-results", Map.of("gameId", id, "score", Math.min(score, 10_000_000L), "won", record), t);
            lastCoins = (int)Json.num(r, "coins", 0L);
         } catch (Exception e) {
            // An expired session is renewed on the next result.
            if (String.valueOf(e.getMessage()).contains("401")) token = null;
            lastError = e.getMessage();
            if (LegoClient.DEBUG) LegoClient.LOG("LegoAuth: " + e);
         }
      }).exceptionally(e -> {
         if (LegoClient.DEBUG) LegoClient.LOG("LegoAuth: " + e);
         return null;
      });
   }
}
