package dev.spotifyhud;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class SpotifyApi {
   private static final String API = "https://api.spotify.com/v1";
   private static final String ACCOUNTS = "https://accounts.spotify.com";
   private static final String SCOPES = "user-read-playback-state user-modify-playback-state user-read-currently-playing user-library-read user-library-modify playlist-read-private playlist-read-collaborative";
   private static final long LOCK_MS = 2500L;
   private final Config cfg;
   private final ScheduledExecutorService exec;
   private final ScheduledExecutorService artExec;
   private final SecureRandom rnd = new SecureRandom();
   private volatile PlayerState state = PlayerState.EMPTY;
   private volatile String status = "";
   private volatile long statusTime = 0L;
   private volatile boolean noDevice = true;
   private volatile boolean authRunning = false;
   private volatile ServerSocket authSocket;
   private volatile long pauseUntil = 0L;
   private volatile Object artImage;
   private volatile String artImageUrl;
   private volatile String artLoadingUrl;
   private volatile int artVersion = 0;
   private long lockPlaying;
   private long lockShuffle;
   private long lockRepeat;
   private long lockVolume;
   private long lockProgress;
   private long lockLiked;
   private String likedCheckedFor;
   private int pollCounter = 0;
   private int volumeBeforeMute = 50;
   private ScheduledFuture<?> pollFuture;
   private ScheduledFuture<?> volumeFuture;
   private volatile int pendingVolume = -1;
   private volatile boolean lightMode = false;
   private volatile long latencyMs = 0L;
   private volatile Consumer<String> errorListener = var0 -> {};
   public volatile String lastBrowseError;
   private final Object tokenLock = new Object();

   public SpotifyApi(Config var1) {
      this.cfg = var1;
      this.exec = Executors.newSingleThreadScheduledExecutor(var0 -> {
         Thread var1x = new Thread(var0, "SpotifyHUD-API");
         var1x.setDaemon(true);
         return var1x;
      });
      this.artExec = Executors.newSingleThreadScheduledExecutor(var0 -> {
         Thread var1x = new Thread(var0, "SpotifyHUD-Art");
         var1x.setDaemon(true);
         return var1x;
      });
      if (this.isLoggedIn()) {
         this.setStatus("Verbunden");
      } else {
         this.setStatus("Nicht verbunden");
      }

      this.schedulePoll(500L);
   }

   public PlayerState state() {
      return this.state;
   }

   public String status() {
      return this.status;
   }

   public long statusTime() {
      return this.statusTime;
   }

   public boolean isLoggedIn() {
      return this.cfg.refreshToken != null && !this.cfg.refreshToken.isEmpty();
   }

   public boolean authRunning() {
      return this.authRunning;
   }

   public boolean noDevice() {
      return this.noDevice;
   }

   public Object artImage() {
      return this.artImage;
   }

   public int artVersion() {
      return this.artVersion;
   }

   public long latencyMs() {
      return this.latencyMs;
   }

   public void setLightMode(boolean var1) {
      this.lightMode = var1;
   }

   public void setErrorListener(Consumer<String> var1) {
      this.errorListener = var1 != null ? var1 : var0 -> {};
   }

   private void setStatus(String var1) {
      this.status = var1;
      this.statusTime = System.currentTimeMillis();
   }

   private synchronized void schedulePoll(long var1) {
      if (this.pollFuture != null) {
         this.pollFuture.cancel(false);
      }

      this.pollFuture = this.exec.schedule(this::pollSafe, Math.max(0L, var1), TimeUnit.MILLISECONDS);
   }

   private void pollSafe() {
      long var1 = 1000L;

      try {
         var1 = this.poll();
      } catch (Throwable var4) {
         SpotifyHudMod.LOG("Poll-Fehler: " + var4);
         var1 = 3000L;
      }

      this.schedulePoll(var1);
   }

   private long poll() throws Exception {
      long var1 = System.currentTimeMillis();
      if (var1 < this.pauseUntil) {
         return this.pauseUntil - var1;
      } else if (this.isLoggedIn() && !this.cfg.clientId.isEmpty()) {
         long var3 = System.currentTimeMillis();
         SpotifyApi.Resp var5 = this.request("GET", "/me/player?additional_types=episode", false);
         long var6 = System.currentTimeMillis();
         long var8 = Math.max(0L, Math.min(3000L, (var6 - var3) / 2L));
         this.latencyMs = this.latencyMs == 0L ? var8 : (this.latencyMs * 3L + var8) / 4L;
         if (var5.code == 429) {
            return this.retryAfter(var5);
         } else if (var5.code != 204 && (var5.code != 200 || !var5.body.isBlank())) {
            if (var5.code != 200) {
               if (var5.code == 401 || var5.code == 403) {
                  this.setStatus(apiError(var5, "Autorisierung fehlgeschlagen"));
               }

               return 4000L;
            } else {
               this.noDevice = false;
               Object var10 = Json.parse(var5.body);
               this.applyPlayer(var10, var6 - var8);
               this.pollCounter++;
               PlayerState var11 = this.state;
               if (var11.uri != null
                  && var11.uri.startsWith("spotify:track:")
                  && (!var11.uri.equals(this.likedCheckedFor) || this.pollCounter % 12 == 0)
                  && System.currentTimeMillis() >= this.lockLiked) {
                  this.checkLiked(var11.uri);
               }

               if (this.lightMode) {
                  return 4000L;
               } else {
                  return var11.playing ? 1000L : 2000L;
               }
            }
         } else {
            this.noDevice = true;
            this.state = PlayerState.EMPTY;
            return 2500L;
         }
      } else {
         this.state = PlayerState.EMPTY;
         this.noDevice = true;
         return 2000L;
      }
   }

   private long retryAfter(SpotifyApi.Resp var1) {
      long var2 = 5L;

      try {
         if (var1.retryAfter != null) {
            var2 = Long.parseLong(var1.retryAfter.trim());
         }
      } catch (NumberFormatException var5) {
      }

      var2 = Math.max(1L, Math.min(120L, var2));
      this.pauseUntil = System.currentTimeMillis() + var2 * 1000L;
      return var2 * 1000L;
   }

   private synchronized void applyPlayer(Object var1, long var2) {
      long var4 = System.currentTimeMillis();
      PlayerState var6 = this.state;
      Map var7 = Json.obj(var1, "item");
      Map var8 = Json.obj(var1, "device");
      boolean var9 = Json.bool(var1, "is_playing", false);
      boolean var10 = Json.bool(var1, "shuffle_state", false);
      String var11 = Json.str(var1, "repeat_state", "off");
      long var12 = Json.num(var1, "progress_ms", 0L);
      int var14 = var8 != null && var8.get("volume_percent") instanceof Number ? ((Number)var8.get("volume_percent")).intValue() : -1;
      String var15 = Json.str(var1, "currently_playing_type", "track");
      String var18 = null;
      String var19 = null;
      String var20 = null;
      long var21 = 0L;
      String var16;
      String var17;
      boolean var23;
      if (var7 != null) {
         var23 = true;
         var16 = Json.str(var7, "name", "");
         var18 = Json.str(var7, "uri", null);
         var19 = Json.str(var7, "id", null);
         var21 = Json.num(var7, "duration_ms", 0L);
         List var24;
         if ("episode".equals(Json.str(var7, "type", "track"))) {
            Map var31 = Json.obj(var7, "show");
            var17 = var31 != null ? Json.str(var31, "name", "") : "";
            var24 = Json.arr(var7, "images");
            if ((var24 == null || var24.isEmpty()) && var31 != null) {
               var24 = Json.arr(var31, "images");
            }
         } else {
            StringBuilder var25 = new StringBuilder();
            List var26 = Json.arr(var7, "artists");
            if (var26 != null) {
               for (Object var28 : var26) {
                  String var29 = Json.str(var28, "name", "");
                  if (!var29.isEmpty()) {
                     if (var25.length() > 0) {
                        var25.append(", ");
                     }

                     var25.append(var29);
                  }
               }
            }

            var17 = var25.toString();
            Map var33 = Json.obj(var7, "album");
            var24 = var33 != null ? Json.arr(var33, "images") : null;
         }

         var20 = pickImage(var24);
      } else if ("ad".equals(var15)) {
         var23 = true;
         var16 = "Werbung";
         var17 = "Spotify";
      } else {
         var23 = false;
         var16 = "";
         var17 = "";
      }

      boolean var30 = var18 != null ? var18.equals(var6.uri) : var6.uri == null && var16.equals(var6.title);
      if (var4 < this.lockPlaying) {
         var9 = var6.playing;
      }

      if (var4 < this.lockShuffle) {
         var10 = var6.shuffle;
      }

      if (var4 < this.lockRepeat) {
         var11 = var6.repeat;
      }

      if (var4 < this.lockVolume && var6.volume >= 0) {
         var14 = var6.volume;
      }

      long var32 = Math.min(var4, var2);
      if (var4 < this.lockProgress && var30) {
         var12 = var6.currentProgress();
         var32 = var4;
      }

      boolean var34 = var30 && var6.liked;
      if (!var30) {
         this.lockLiked = 0L;
      }

      this.state = new PlayerState(var23, var19, var18, var16, var17, var20, var21, var12, var32, var9, var10, var11, var14, var34);
      if (var20 == null) {
         if (this.artImageUrl != null) {
            this.artImage = null;
            this.artImageUrl = null;
            this.artVersion++;
         }
      } else if (!var20.equals(this.artImageUrl) && !var20.equals(this.artLoadingUrl)) {
         String var35 = var20;
         this.artLoadingUrl = var35;
         this.artExec.execute(() -> {
            Object var2x = ArtLoader.load(var35);
            if (var35.equals(this.artLoadingUrl)) {
               this.artImage = var2x;
               this.artImageUrl = var35;
               this.artLoadingUrl = null;
               this.artVersion++;
            }
         });
      }
   }

   private static String pickImage(List<Object> var0) {
      if (var0 != null && !var0.isEmpty()) {
         String var1 = null;
         long var2 = Long.MAX_VALUE;
         String var4 = null;
         long var5 = -1L;

         for (Object var8 : var0) {
            String var9 = Json.str(var8, "url", null);
            if (var9 != null) {
               long var10 = Json.num(var8, "width", 300L);
               if (var10 >= 200L && var10 < var2) {
                  var1 = var9;
                  var2 = var10;
               }

               if (var10 > var5) {
                  var4 = var9;
                  var5 = var10;
               }
            }
         }

         return var1 != null ? var1 : var4;
      } else {
         return null;
      }
   }

   private void checkLiked(String var1) {
      try {
         SpotifyApi.Resp var2 = this.request("GET", "/me/library/contains?uris=" + enc(var1), false);
         if (var2.code == 404 && var1.startsWith("spotify:track:")) {
            var2 = this.request("GET", "/me/tracks/contains?ids=" + enc(var1.substring(14)), false);
         }

         if (var2.code == 200) {
            Object var3 = Json.parse(var2.body);
            if (var3 instanceof List && !((List)var3).isEmpty() && ((List)var3).get(0) instanceof Boolean) {
               boolean var4 = (Boolean)((List)var3).get(0);
               synchronized (this) {
                  if (var1.equals(this.state.uri) && System.currentTimeMillis() >= this.lockLiked) {
                     this.state = this.state.withLiked(var4);
                  }
               }
            }

            this.likedCheckedFor = var1;
         } else if (var2.code == 429) {
            this.retryAfter(var2);
         } else {
            this.likedCheckedFor = var1;
         }
      } catch (Exception var8) {
         SpotifyHudMod.LOG("Like-Status Fehler: " + var8);
      }
   }

   public void togglePlay() {
      if (this.requireLogin()) {
         boolean var1;
         synchronized (this) {
            var1 = !this.state.playing;
            this.state = this.state.withPlaying(var1);
            this.lockPlaying = System.currentTimeMillis() + 2500L;
         }

         this.control("PUT", var1 ? "/me/player/play" : "/me/player/pause", () -> this.lockPlaying = 0L);
      }
   }

   public void next() {
      if (this.requireLogin()) {
         synchronized (this) {
            this.state = this.state.withProgress(0L);
            this.lockProgress = 0L;
         }

         this.control("POST", "/me/player/next", null);
      }
   }

   public void previous() {
      if (this.requireLogin()) {
         synchronized (this) {
            this.state = this.state.withProgress(0L);
            this.lockProgress = 0L;
         }

         this.control("POST", "/me/player/previous", null);
      }
   }

   public void seek(long var1) {
      if (this.requireLogin()) {
         synchronized (this) {
            long var4 = this.state.durationMs;
            var1 = Math.max(0L, var4 > 0L ? Math.min(var1, var4 - 500L) : var1);
            this.state = this.state.withProgress(var1);
            this.lockProgress = System.currentTimeMillis() + 2500L;
         }

         this.control("PUT", "/me/player/seek?position_ms=" + var1, () -> this.lockProgress = 0L);
      }
   }

   public void toggleShuffle() {
      if (this.requireLogin()) {
         boolean var1;
         synchronized (this) {
            var1 = !this.state.shuffle;
            this.state = this.state.withShuffle(var1);
            this.lockShuffle = System.currentTimeMillis() + 2500L;
         }

         this.control("PUT", "/me/player/shuffle?state=" + var1, () -> this.lockShuffle = 0L);
      }
   }

   public void cycleRepeat() {
      if (this.requireLogin()) {
         String var1;
         synchronized (this) {
            String var3 = this.state.repeat;
            var1 = "off".equals(var3) ? "context" : ("context".equals(var3) ? "track" : "off");
            this.state = this.state.withRepeat(var1);
            this.lockRepeat = System.currentTimeMillis() + 2500L;
         }

         this.control("PUT", "/me/player/repeat?state=" + var1, () -> this.lockRepeat = 0L);
      }
   }

   public void setVolume(int var1, boolean var2) {
      if (this.requireLogin()) {
         var1 = Math.max(0, Math.min(100, var1));
         synchronized (this) {
            this.state = this.state.withVolume(var1);
            this.lockVolume = System.currentTimeMillis() + 2500L + 500L;
            this.pendingVolume = var1;
            if (this.volumeFuture != null) {
               this.volumeFuture.cancel(false);
            }

            this.volumeFuture = this.exec.schedule(this::flushVolume, var2 ? 0L : 180L, TimeUnit.MILLISECONDS);
         }
      }
   }

   private void flushVolume() {
      int var1 = this.pendingVolume;
      if (var1 >= 0) {
         this.pendingVolume = -1;
         this.lockVolume = System.currentTimeMillis() + 2500L;
         this.doControl("PUT", "/me/player/volume?volume_percent=" + var1, () -> this.lockVolume = 0L, false);
      }
   }

   public void toggleMute() {
      int var1 = this.state.volume;
      if (var1 >= 0) {
         if (var1 > 0) {
            this.volumeBeforeMute = var1;
            this.setVolume(0, true);
         } else {
            this.setVolume(this.volumeBeforeMute > 0 ? this.volumeBeforeMute : 50, true);
         }
      }
   }

   public void toggleLike() {
      if (this.requireLogin()) {
         String var1;
         boolean var2;
         synchronized (this) {
            var1 = this.state.uri;
            if (var1 == null || !var1.startsWith("spotify:track:") && !var1.startsWith("spotify:episode:")) {
               this.setStatus("Dieser Titel kann nicht gespeichert werden");
               return;
            }

            var2 = !this.state.liked;
            this.state = this.state.withLiked(var2);
            this.lockLiked = System.currentTimeMillis() + 60000L;
         }

         this.exec.execute(() -> {
            try {
               String var3 = var2 ? "PUT" : "DELETE";
               SpotifyApi.Resp var4 = this.request(var3, "/me/library?uris=" + enc(var1), true);
               if ((var4.code == 404 || var4.code == 405) && var1.startsWith("spotify:track:")) {
                  var4 = this.request(var3, "/me/tracks?ids=" + enc(var1.substring(14)), true);
               }

               if (var4.code >= 200 && var4.code < 300) {
                  this.likedCheckedFor = var1;
                  this.lockLiked = System.currentTimeMillis() + 5000L;
               } else {
                  this.lockLiked = 0L;
                  this.likedCheckedFor = null;
                  String var5 = apiError(var4, "Speichern fehlgeschlagen");
                  this.setStatus(var5);
                  this.errorListener.accept(var5);
                  this.schedulePoll(0L);
               }
            } catch (Exception var6) {
               this.lockLiked = 0L;
               this.setStatus("Netzwerkfehler: " + var6.getMessage());
            }
         });
      }
   }

   private boolean requireLogin() {
      if (this.isLoggedIn()) {
         return true;
      } else {
         this.setStatus("Nicht verbunden - drücke H und melde dich an");
         return false;
      }
   }

   private void control(String var1, String var2, Runnable var3) {
      this.exec.execute(() -> this.doControl(var1, var2, var3, true));
   }

   private void doControl(String var1, String var2, Runnable var3, boolean var4) {
      try {
         SpotifyApi.Resp var5 = this.request(var1, var2, true);
         if (var5.code >= 200 && var5.code < 300) {
            if (var4) {
               this.schedulePoll(!var2.contains("next") && !var2.contains("previous") ? 900L : 350L);
            }

            return;
         }

         if (var3 != null) {
            var3.run();
         }

         if (var5.code == 429) {
            this.retryAfter(var5);
         }

         String var6 = var5.code == 404
            ? "Kein aktives Spotify-Gerät gefunden"
            : (var5.code == 403 ? "Aktion nicht erlaubt (Spotify Premium nötig?)" : "Fehler " + var5.code);
         String var7 = apiError(var5, var6);
         this.setStatus(var7);
         this.errorListener.accept(var7);
         this.schedulePoll(200L);
      } catch (Exception var8) {
         if (var3 != null) {
            var3.run();
         }

         this.setStatus("Netzwerkfehler: " + var8.getMessage());
         this.schedulePoll(1000L);
      }
   }

   private static String apiError(SpotifyApi.Resp var0, String var1) {
      try {
         Object var2 = Json.parse(var0.body);
         Map var3 = Json.obj(var2, "error");
         String var4 = var3 != null ? Json.str(var3, "message", null) : Json.str(var2, "error_description", null);
         if (var4 != null && !var4.isBlank()) {
            String var5 = var3 != null ? Json.str(var3, "reason", "") : "";
            if ("PREMIUM_REQUIRED".equals(var5)) {
               return "Spotify Premium erforderlich";
            }

            if ("NO_ACTIVE_DEVICE".equals(var5)) {
               return "Kein aktives Spotify-Gerät";
            }

            return var1 + ": " + var4;
         }
      } catch (Exception var6) {
      }

      return var1;
   }

   public List<String[]> search(String var1) {
      ArrayList var2 = new ArrayList();
      if (this.isLoggedIn() && var1 != null && !var1.isBlank()) {
         try {
            SpotifyApi.Resp var3 = this.request("GET", "/search?type=track&limit=30&q=" + enc(var1), false);
            if (var3.code != 200) {
               this.lastBrowseError = apiError(var3, "Fehler " + var3.code);
               return var2;
            }

            Map var4 = Json.obj(Json.parse(var3.body), "tracks");
            List var5 = Json.arr(var4, "items");
            if (var5 != null) {
               for (Object var7 : var5) {
                  List var8 = Json.arr(var7, "artists");
                  String var9 = var8 != null && !var8.isEmpty() ? Json.str(var8.get(0), "name", "") : "";
                  var2.add(new String[]{Json.str(var7, "name", "?"), var9, Json.str(var7, "uri", "")});
               }
            }

            this.lastBrowseError = null;
         } catch (Exception var10) {
            this.lastBrowseError = "Netzwerkfehler";
         }

         return var2;
      } else {
         return var2;
      }
   }

   public List<String[]> playlists() {
      ArrayList var1 = new ArrayList();
      if (!this.isLoggedIn()) {
         return var1;
      } else {
         try {
            SpotifyApi.Resp var2 = this.request("GET", "/me/playlists?limit=50", false);
            if (var2.code == 401 || var2.code == 403) {
               this.lastBrowseError = "Für Playlists einmal neu anmelden (H)";
               return var1;
            }

            if (var2.code != 200) {
               this.lastBrowseError = apiError(var2, "Fehler " + var2.code);
               return var1;
            }

            List var3 = Json.arr(Json.parse(var2.body), "items");
            if (var3 != null) {
               for (Object var5 : var3) {
                  if (var5 != null) {
                     Map var6 = Json.obj(var5, "owner");
                     var1.add(new String[]{Json.str(var5, "name", "?"), var6 == null ? "" : Json.str(var6, "display_name", ""), Json.str(var5, "uri", "")});
                  }
               }
            }

            this.lastBrowseError = null;
         } catch (Exception var7) {
            this.lastBrowseError = "Netzwerkfehler";
         }

         return var1;
      }
   }

   public void playUri(String var1) {
      if (var1 != null && !var1.isEmpty() && this.requireLogin()) {
         String var2 = var1.startsWith("spotify:track:") ? "{\"uris\":[\"" + var1 + "\"]}" : "{\"context_uri\":\"" + var1 + "\"}";
         this.exec.execute(() -> {
            try {
               SpotifyApi.Resp var2x = this.requestBody("PUT", "/me/player/play", var2);
               if (var2x.code == 401 && this.refreshToken()) {
                  var2x = this.requestBody("PUT", "/me/player/play", var2);
               }

               if (var2x.code >= 200 && var2x.code < 300) {
                  this.schedulePoll(700L);
                  return;
               }

               String var3 = apiError(var2x, var2x.code == 404 ? "Kein aktives Spotify-Gerät gefunden" : "Fehler " + var2x.code);
               this.setStatus(var3);
               this.errorListener.accept(var3);
            } catch (Exception var4) {
               this.setStatus("Netzwerkfehler: " + var4.getMessage());
            }
         });
      }
   }

   private SpotifyApi.Resp requestBody(String var1, String var2, String var3) throws IOException {
      this.ensureToken();
      HttpURLConnection var4 = (HttpURLConnection)URI.create("https://api.spotify.com/v1" + var2).toURL().openConnection();

      SpotifyApi.Resp var8;
      try {
         var4.setRequestMethod(var1);
         var4.setConnectTimeout(6000);
         var4.setReadTimeout(8000);
         var4.setRequestProperty("Authorization", "Bearer " + this.cfg.accessToken);
         var4.setRequestProperty("Content-Type", "application/json");
         byte[] var5 = var3.getBytes(StandardCharsets.UTF_8);
         var4.setDoOutput(true);
         var4.setFixedLengthStreamingMode(var5.length);

         try (OutputStream var6 = var4.getOutputStream()) {
            var6.write(var5);
         }

         SpotifyApi.Resp var16 = new SpotifyApi.Resp();
         var16.code = var4.getResponseCode();
         InputStream var7 = var16.code >= 400 ? var4.getErrorStream() : var4.getInputStream();
         var16.body = readAll(var7);
         var8 = var16;
      } finally {
         var4.disconnect();
      }

      return var8;
   }

   private SpotifyApi.Resp request(String var1, String var2, boolean var3) throws IOException {
      SpotifyApi.Resp var4 = this.requestOnce(var1, var2, var3);
      if (var4.code == 401 && this.refreshToken()) {
         var4 = this.requestOnce(var1, var2, var3);
      }

      return var4;
   }

   private SpotifyApi.Resp requestOnce(String var1, String var2, boolean var3) throws IOException {
      this.ensureToken();
      HttpURLConnection var4 = (HttpURLConnection)URI.create("https://api.spotify.com/v1" + var2).toURL().openConnection();

      SpotifyApi.Resp var7;
      try {
         var4.setRequestMethod(var1);
         var4.setConnectTimeout(6000);
         var4.setReadTimeout(8000);
         var4.setUseCaches(false);
         var4.setRequestProperty("Authorization", "Bearer " + this.cfg.accessToken);
         var4.setRequestProperty("Accept", "application/json");
         if (var3 && !"DELETE".equals(var1) && !"GET".equals(var1)) {
            var4.setDoOutput(true);
            var4.setRequestProperty("Content-Type", "application/json");
            var4.setFixedLengthStreamingMode(0);
            var4.getOutputStream().close();
         }

         SpotifyApi.Resp var5 = new SpotifyApi.Resp();
         var5.code = var4.getResponseCode();
         var5.retryAfter = var4.getHeaderField("Retry-After");
         InputStream var6 = var5.code >= 400 ? var4.getErrorStream() : var4.getInputStream();
         var5.body = readAll(var6);
         var7 = var5;
      } finally {
         var4.disconnect();
      }

      return var7;
   }

   private static String readAll(InputStream var0) throws IOException {
      if (var0 == null) {
         return "";
      } else {
         String var5;
         try (InputStream var1 = var0) {
            ByteArrayOutputStream var2 = new ByteArrayOutputStream();
            byte[] var3 = new byte[8192];

            int var4;
            while ((var4 = var1.read(var3)) > 0) {
               var2.write(var3, 0, var4);
            }

            var5 = var2.toString(StandardCharsets.UTF_8);
         }

         return var5;
      }
   }

   private static String enc(String var0) {
      return URLEncoder.encode(var0, StandardCharsets.UTF_8);
   }

   private void ensureToken() {
      synchronized (this.tokenLock) {
         if (System.currentTimeMillis() > this.cfg.expiresAt - 60000L) {
            this.refreshToken();
         }
      }
   }

   private boolean refreshToken() {
      synchronized (this.tokenLock) {
         return this.refreshTokenLocked();
      }
   }

   private boolean refreshTokenLocked() {
      if (!this.isLoggedIn()) {
         return false;
      } else {
         try {
            HashMap var1 = new HashMap();
            var1.put("grant_type", "refresh_token");
            var1.put("refresh_token", this.cfg.refreshToken);
            var1.put("client_id", this.cfg.clientId);
            SpotifyApi.Resp var2 = this.tokenRequest(var1);
            if (var2.code == 200) {
               this.storeTokens(Json.parse(var2.body));
               return true;
            }

            if (var2.code == 400 || var2.code == 401) {
               this.cfg.accessToken = "";
               this.cfg.refreshToken = "";
               this.cfg.expiresAt = 0L;
               this.cfg.save();
               this.state = PlayerState.EMPTY;
               this.setStatus("Sitzung abgelaufen - bitte erneut anmelden");
            }
         } catch (Exception var3) {
            SpotifyHudMod.LOG("Token-Refresh fehlgeschlagen: " + var3);
         }

         return false;
      }
   }

   private void storeTokens(Object var1) {
      String var2 = Json.str(var1, "access_token", "");
      if (!var2.isEmpty()) {
         this.cfg.accessToken = var2;
         String var3 = Json.str(var1, "refresh_token", null);
         if (var3 != null && !var3.isEmpty()) {
            this.cfg.refreshToken = var3;
         }

         this.cfg.expiresAt = System.currentTimeMillis() + Json.num(var1, "expires_in", 3600L) * 1000L;
         this.cfg.save();
      }
   }

   private SpotifyApi.Resp tokenRequest(Map<String, String> var1) throws IOException {
      StringBuilder var2 = new StringBuilder();

      for (Entry var4 : var1.entrySet()) {
         if (var2.length() > 0) {
            var2.append('&');
         }

         var2.append(enc((String)var4.getKey())).append('=').append(enc((String)var4.getValue()));
      }

      byte[] var15 = var2.toString().getBytes(StandardCharsets.UTF_8);
      HttpURLConnection var16 = (HttpURLConnection)URI.create("https://accounts.spotify.com/api/token").toURL().openConnection();

      SpotifyApi.Resp var6;
      try {
         var16.setRequestMethod("POST");
         var16.setConnectTimeout(8000);
         var16.setReadTimeout(10000);
         var16.setDoOutput(true);
         var16.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         var16.setFixedLengthStreamingMode(var15.length);

         try (OutputStream var5 = var16.getOutputStream()) {
            var5.write(var15);
         }

         SpotifyApi.Resp var17 = new SpotifyApi.Resp();
         var17.code = var16.getResponseCode();
         var17.body = readAll(var17.code >= 400 ? var16.getErrorStream() : var16.getInputStream());
         var6 = var17;
      } finally {
         var16.disconnect();
      }

      return var6;
   }

   public String startLogin() {
      if (this.cfg.clientId != null && !this.cfg.clientId.isBlank()) {
         this.stopAuthServer();
         String var1 = this.randomString(64);

         String var2;
         try {
            byte[] var3 = MessageDigest.getInstance("SHA-256").digest(var1.getBytes(StandardCharsets.US_ASCII));
            var2 = Base64.getUrlEncoder().withoutPadding().encodeToString(var3);
         } catch (Exception var9) {
            this.setStatus("SHA-256 nicht verfügbar");
            return null;
         }

         String var10 = this.randomString(24);
         String var4 = this.cfg.redirectUri();
         String var5 = "https://accounts.spotify.com/authorize?client_id="
            + enc(this.cfg.clientId.trim())
            + "&response_type=code&redirect_uri="
            + enc(var4)
            + "&code_challenge_method=S256&code_challenge="
            + enc(var2)
            + "&scope="
            + enc(
               "user-read-playback-state user-modify-playback-state user-read-currently-playing user-library-read user-library-modify playlist-read-private playlist-read-collaborative"
            )
            + "&state="
            + enc(var10);

         ServerSocket var6;
         try {
            var6 = new ServerSocket();
            var6.setReuseAddress(false);
            var6.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), this.cfg.redirectPort));
            var6.setSoTimeout(1000);
         } catch (IOException var8) {
            this.handleBusyPort(this.cfg.redirectPort);
            return null;
         }

         this.authSocket = var6;
         this.authRunning = true;
         this.setStatus("Warte auf Login im Browser...");
         Thread var7 = new Thread(() -> this.runAuthServer(var6, var10, var1, var4), "SpotifyHUD-Auth");
         var7.setDaemon(true);
         var7.start();
         return var5;
      } else {
         this.setStatus("Bitte zuerst deine Spotify Client-ID eintragen");
         return null;
      }
   }

   private void handleBusyPort(int var1) {
      this.setStatus("Port " + var1 + " ist belegt - prüfe...");
      Thread var2 = new Thread(
         () -> {
            String var2x = PortInfo.owner(var1);
            int var3 = PortInfo.findFree(var1 + 1, var1 + 200);
            String var4 = var2x != null ? " von " + var2x : " von einem anderen Programm";
            if (var3 > 0) {
               this.cfg.redirectPort = var3;
               this.cfg.save();
               SpotifyHudMod.copyToClipboard(this.cfg.redirectUri());
               this.setStatus(
                  "Port "
                     + var1
                     + " wird"
                     + var4
                     + " benutzt. Neuer Port "
                     + var3
                     + ": Redirect-URI "
                     + this.cfg.redirectUri()
                     + " (kopiert) im Dashboard hinzufügen, dann erneut Anmelden"
               );
            } else {
               this.setStatus("Port " + var1 + " wird" + var4 + " benutzt - bitte Programm schließen");
            }
         },
         "SpotifyHUD-Port"
      );
      var2.setDaemon(true);
      var2.start();
   }

   public void cancelLogin() {
      this.stopAuthServer();
      if (!this.isLoggedIn()) {
         this.setStatus("Login abgebrochen");
      }
   }

   private void stopAuthServer() {
      ServerSocket var1 = this.authSocket;
      this.authSocket = null;
      this.authRunning = false;
      if (var1 != null) {
         try {
            var1.close();
         } catch (IOException var3) {
         }
      }
   }

   private void runAuthServer(ServerSocket var1, String var2, String var3, String var4) {
      long var5 = System.currentTimeMillis() + 300000L;

      try {
         while (this.authSocket == var1 && System.currentTimeMillis() < var5) {
            Socket var7;
            try {
               var7 = var1.accept();
            } catch (SocketTimeoutException var33) {
               continue;
            } catch (IOException var34) {
               break;
            }

            try (Socket var8 = var7) {
               var8.setSoTimeout(5000);
               String var9 = readLine(var8.getInputStream());

               String var10;
               do {
                  var10 = readLine(var8.getInputStream());
               } while (var10 != null && !var10.isEmpty());

               if (var9 != null) {
                  String[] var11 = var9.split(" ");
                  if (var11.length >= 2) {
                     String var12 = var11[1];
                     String var13 = var12.contains("?") ? var12.substring(0, var12.indexOf(63)) : var12;
                     if (var13.equals("/callback")) {
                        Map var14 = parseQuery(var12.contains("?") ? var12.substring(var12.indexOf(63) + 1) : "");
                        if (var14.containsKey("error")) {
                           this.setStatus("Login abgelehnt: " + (String)var14.get("error"));
                           respond(var8, 200, page(false, "Login abgebrochen: " + (String)var14.get("error")));
                           break;
                        }

                        if (var2.equals(var14.get("state")) && var14.containsKey("code")) {
                           HashMap var15 = new HashMap();
                           var15.put("grant_type", "authorization_code");
                           var15.put("code", (String)var14.get("code"));
                           var15.put("redirect_uri", var4);
                           var15.put("client_id", this.cfg.clientId.trim());
                           var15.put("code_verifier", var3);
                           SpotifyApi.Resp var16 = this.tokenRequest(var15);
                           if (var16.code == 200) {
                              synchronized (this.tokenLock) {
                                 this.storeTokens(Json.parse(var16.body));
                              }

                              this.setStatus("Verbunden");
                              respond(var8, 200, page(true, "Spotify HUD ist verbunden. Du kannst dieses Fenster schließen und zu Minecraft zurückkehren."));
                              this.schedulePoll(0L);
                           } else {
                              this.setStatus(apiError(var16, "Token-Austausch fehlgeschlagen"));
                              respond(var8, 200, page(false, "Token-Austausch fehlgeschlagen (" + var16.code + "): " + var16.body));
                           }
                           break;
                        }

                        this.setStatus("Ungültige Antwort (state) - bitte erneut versuchen");
                        respond(var8, 400, page(false, "Ungültige Anfrage. Bitte erneut versuchen."));
                        break;
                     }

                     respond(var8, 404, "Not found");
                  }
               }
            } catch (IOException var36) {
               SpotifyHudMod.LOG("Callback-Fehler: " + var36);
            }
         }

         if (System.currentTimeMillis() >= var5 && !this.isLoggedIn()) {
            this.setStatus("Login-Zeit abgelaufen");
         }
      } finally {
         if (this.authSocket == var1) {
            this.authSocket = null;
         }

         this.authRunning = false;

         try {
            var1.close();
         } catch (IOException var30) {
         }
      }
   }

   public void logout() {
      this.stopAuthServer();
      this.cfg.accessToken = "";
      this.cfg.refreshToken = "";
      this.cfg.expiresAt = 0L;
      this.cfg.save();
      this.state = PlayerState.EMPTY;
      this.artImage = null;
      this.artImageUrl = null;
      this.artVersion++;
      this.setStatus("Abgemeldet");
   }

   private static String readLine(InputStream var0) throws IOException {
      StringBuilder var1 = new StringBuilder();

      int var2;
      while ((var2 = var0.read()) != -1 && var2 != 10) {
         if (var2 != 13) {
            var1.append((char)var2);
         }

         if (var1.length() > 8192) {
            break;
         }
      }

      return var2 == -1 && var1.length() == 0 ? null : var1.toString();
   }

   private static Map<String, String> parseQuery(String var0) {
      HashMap var1 = new HashMap();

      for (String var5 : var0.split("&")) {
         if (!var5.isEmpty()) {
            int var6 = var5.indexOf(61);
            String var7 = var6 < 0 ? var5 : var5.substring(0, var6);
            String var8 = var6 < 0 ? "" : var5.substring(var6 + 1);
            var1.put(URLDecoder.decode(var7, StandardCharsets.UTF_8), URLDecoder.decode(var8, StandardCharsets.UTF_8));
         }
      }

      return var1;
   }

   private static void respond(Socket var0, int var1, String var2) throws IOException {
      byte[] var3 = var2.getBytes(StandardCharsets.UTF_8);
      String var4 = "HTTP/1.1 "
         + var1
         + (var1 == 200 ? " OK" : (var1 == 404 ? " Not Found" : " Bad Request"))
         + "\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: "
         + var3.length
         + "\r\nConnection: close\r\n\r\n";
      OutputStream var5 = var0.getOutputStream();
      var5.write(var4.getBytes(StandardCharsets.US_ASCII));
      var5.write(var3);
      var5.flush();
   }

   private static String page(boolean var0, String var1) {
      String var2 = var1.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
      return "<!doctype html><html><head><meta charset=utf-8><title>Spotify HUD</title></head><body style=\"background:#121212;color:#fff;font-family:Segoe UI,Arial,sans-serif;display:flex;align-items:center;justify-content:center;height:100vh;margin:0\"><div style=\"text-align:center\"><h1 style=\"color:"
         + (var0 ? "#1ed760" : "#f15e6c")
         + "\">"
         + (var0 ? "Verbunden" : "Fehler")
         + "</h1><p style=\"color:#b3b3b3\">"
         + var2
         + "</p></div></body></html>";
   }

   private String randomString(int var1) {
      StringBuilder var2 = new StringBuilder(var1);

      for (int var3 = 0; var3 < var1; var3++) {
         var2.append(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
               .charAt(this.rnd.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".length()))
         );
      }

      return var2.toString();
   }

   private static final class Resp {
      int code;
      String body = "";
      String retryAfter;
   }
}
