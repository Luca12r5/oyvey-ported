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
   private volatile Consumer<String> errorListener = s -> {};
   public volatile String lastBrowseError;
   private final Object tokenLock = new Object();

   public SpotifyApi(Config cfg) {
      this.cfg = cfg;
      this.exec = Executors.newSingleThreadScheduledExecutor(r -> {
         Thread t = new Thread(r, "SpotifyHUD-API");
         t.setDaemon(true);
         return t;
      });
      this.artExec = Executors.newSingleThreadScheduledExecutor(r -> {
         Thread t = new Thread(r, "SpotifyHUD-Art");
         t.setDaemon(true);
         return t;
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

   public void setLightMode(boolean v) {
      this.lightMode = v;
   }

   public void setErrorListener(Consumer<String> l) {
      this.errorListener = l != null ? l : s -> {};
   }

   private void setStatus(String s) {
      this.status = s;
      this.statusTime = System.currentTimeMillis();
   }

   private synchronized void schedulePoll(long delayMs) {
      if (this.pollFuture != null) {
         this.pollFuture.cancel(false);
      }

      this.pollFuture = this.exec.schedule(this::pollSafe, Math.max(0L, delayMs), TimeUnit.MILLISECONDS);
   }

   private void pollSafe() {
      long next = 1000L;

      try {
         next = this.poll();
      } catch (Throwable var4) {
         SpotifyHudMod.LOG("Poll-Fehler: " + var4);
         next = 3000L;
      }

      this.schedulePoll(next);
   }

   private long poll() throws Exception {
      long now = System.currentTimeMillis();
      if (now < this.pauseUntil) {
         return this.pauseUntil - now;
      } else if (this.isLoggedIn() && !this.cfg.clientId.isEmpty()) {
         long t0 = System.currentTimeMillis();
         SpotifyApi.Resp r = this.request("GET", "/me/player?additional_types=episode", false);
         long t1 = System.currentTimeMillis();
         long half = Math.max(0L, Math.min(3000L, (t1 - t0) / 2L));
         this.latencyMs = this.latencyMs == 0L ? half : (this.latencyMs * 3L + half) / 4L;
         if (r.code == 429) {
            return this.retryAfter(r);
         } else if (r.code != 204 && (r.code != 200 || !r.body.isBlank())) {
            if (r.code != 200) {
               if (r.code == 401 || r.code == 403) {
                  this.setStatus(apiError(r, "Autorisierung fehlgeschlagen"));
               }

               return 4000L;
            } else {
               this.noDevice = false;
               Object root = Json.parse(r.body);
               this.applyPlayer(root, t1 - half);
               this.pollCounter++;
               PlayerState s = this.state;
               if (s.uri != null
                  && s.uri.startsWith("spotify:track:")
                  && (!s.uri.equals(this.likedCheckedFor) || this.pollCounter % 12 == 0)
                  && System.currentTimeMillis() >= this.lockLiked) {
                  this.checkLiked(s.uri);
               }

               if (this.lightMode) {
                  return 4000L;
               } else {
                  return s.playing ? 1000L : 2000L;
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

   private long retryAfter(SpotifyApi.Resp r) {
      long sec = 5L;

      try {
         if (r.retryAfter != null) {
            sec = Long.parseLong(r.retryAfter.trim());
         }
      } catch (NumberFormatException var5) {
      }

      sec = Math.max(1L, Math.min(120L, sec));
      this.pauseUntil = System.currentTimeMillis() + sec * 1000L;
      return sec * 1000L;
   }

   private synchronized void applyPlayer(Object root, long sampledAt) {
      long now = System.currentTimeMillis();
      PlayerState old = this.state;
      Map<String, Object> item = Json.obj(root, "item");
      Map<String, Object> device = Json.obj(root, "device");
      boolean playing = Json.bool(root, "is_playing", false);
      boolean shuffle = Json.bool(root, "shuffle_state", false);
      String repeat = Json.str(root, "repeat_state", "off");
      long progress = Json.num(root, "progress_ms", 0L);
      int volume = device != null && device.get("volume_percent") instanceof Number ? ((Number)device.get("volume_percent")).intValue() : -1;
      String type = Json.str(root, "currently_playing_type", "track");
      String uri = null;
      String id = null;
      String art = null;
      long duration = 0L;
      String title;
      String artist;
      boolean has;
      if (item != null) {
         has = true;
         title = Json.str(item, "name", "");
         uri = Json.str(item, "uri", null);
         id = Json.str(item, "id", null);
         duration = Json.num(item, "duration_ms", 0L);
         List<Object> images;
         if ("episode".equals(Json.str(item, "type", "track"))) {
            Map<String, Object> show = Json.obj(item, "show");
            artist = show != null ? Json.str(show, "name", "") : "";
            images = Json.arr(item, "images");
            if ((images == null || images.isEmpty()) && show != null) {
               images = Json.arr(show, "images");
            }
         } else {
            StringBuilder b = new StringBuilder();
            List<Object> artists = Json.arr(item, "artists");
            if (artists != null) {
               for (Object a : artists) {
                  String n = Json.str(a, "name", "");
                  if (!n.isEmpty()) {
                     if (b.length() > 0) {
                        b.append(", ");
                     }

                     b.append(n);
                  }
               }
            }

            artist = b.toString();
            Map<String, Object> album = Json.obj(item, "album");
            images = album != null ? Json.arr(album, "images") : null;
         }

         art = pickImage(images);
      } else if ("ad".equals(type)) {
         has = true;
         title = "Werbung";
         artist = "Spotify";
      } else {
         has = false;
         title = "";
         artist = "";
      }

      boolean sameTrack = uri != null ? uri.equals(old.uri) : old.uri == null && title.equals(old.title);
      if (now < this.lockPlaying) {
         playing = old.playing;
      }

      if (now < this.lockShuffle) {
         shuffle = old.shuffle;
      }

      if (now < this.lockRepeat) {
         repeat = old.repeat;
      }

      if (now < this.lockVolume && old.volume >= 0) {
         volume = old.volume;
      }

      long sampled = Math.min(now, sampledAt);
      if (now < this.lockProgress && sameTrack) {
         progress = old.currentProgress();
         sampled = now;
      }

      boolean liked = sameTrack && old.liked;
      if (!sameTrack) {
         this.lockLiked = 0L;
      }

      this.state = new PlayerState(has, id, uri, title, artist, art, duration, progress, sampled, playing, shuffle, repeat, volume, liked);
      if (art == null) {
         if (this.artImageUrl != null) {
            this.artImage = null;
            this.artImageUrl = null;
            this.artVersion++;
         }
      } else if (!art.equals(this.artImageUrl) && !art.equals(this.artLoadingUrl)) {
         String url = art;
         this.artLoadingUrl = url;
         this.artExec.execute(() -> {
            Object img = ArtLoader.load(url);
            if (url.equals(this.artLoadingUrl)) {
               this.artImage = img;
               this.artImageUrl = url;
               this.artLoadingUrl = null;
               this.artVersion++;
            }
         });
      }
   }

   private static String pickImage(List<Object> images) {
      if (images != null && !images.isEmpty()) {
         String best = null;
         long bestW = Long.MAX_VALUE;
         String largest = null;
         long largestW = -1L;

         for (Object o : images) {
            String url = Json.str(o, "url", null);
            if (url != null) {
               long w = Json.num(o, "width", 300L);
               if (w >= 200L && w < bestW) {
                  best = url;
                  bestW = w;
               }

               if (w > largestW) {
                  largest = url;
                  largestW = w;
               }
            }
         }

         return best != null ? best : largest;
      } else {
         return null;
      }
   }

   private void checkLiked(String uri) {
      try {
         SpotifyApi.Resp r = this.request("GET", "/me/library/contains?uris=" + enc(uri), false);
         if (r.code == 404 && uri.startsWith("spotify:track:")) {
            r = this.request("GET", "/me/tracks/contains?ids=" + enc(uri.substring(14)), false);
         }

         if (r.code == 200) {
            Object o = Json.parse(r.body);
            if (o instanceof List && !((List)o).isEmpty() && ((List)o).get(0) instanceof Boolean) {
               boolean v = (Boolean)((List)o).get(0);
               synchronized (this) {
                  if (uri.equals(this.state.uri) && System.currentTimeMillis() >= this.lockLiked) {
                     this.state = this.state.withLiked(v);
                  }
               }
            }

            this.likedCheckedFor = uri;
         } else if (r.code == 429) {
            this.retryAfter(r);
         } else {
            this.likedCheckedFor = uri;
         }
      } catch (Exception var8) {
         SpotifyHudMod.LOG("Like-Status Fehler: " + var8);
      }
   }

   public void togglePlay() {
      if (this.requireLogin()) {
         boolean play;
         synchronized (this) {
            play = !this.state.playing;
            this.state = this.state.withPlaying(play);
            this.lockPlaying = System.currentTimeMillis() + 2500L;
         }

         this.control("PUT", play ? "/me/player/play" : "/me/player/pause", () -> this.lockPlaying = 0L);
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

   public void seek(long ms) {
      if (this.requireLogin()) {
         synchronized (this) {
            long d = this.state.durationMs;
            ms = Math.max(0L, d > 0L ? Math.min(ms, d - 500L) : ms);
            this.state = this.state.withProgress(ms);
            this.lockProgress = System.currentTimeMillis() + 2500L;
         }

         this.control("PUT", "/me/player/seek?position_ms=" + ms, () -> this.lockProgress = 0L);
      }
   }

   public void toggleShuffle() {
      if (this.requireLogin()) {
         boolean v;
         synchronized (this) {
            v = !this.state.shuffle;
            this.state = this.state.withShuffle(v);
            this.lockShuffle = System.currentTimeMillis() + 2500L;
         }

         this.control("PUT", "/me/player/shuffle?state=" + v, () -> this.lockShuffle = 0L);
      }
   }

   public void cycleRepeat() {
      if (this.requireLogin()) {
         String v;
         synchronized (this) {
            String cur = this.state.repeat;
            v = "off".equals(cur) ? "context" : ("context".equals(cur) ? "track" : "off");
            this.state = this.state.withRepeat(v);
            this.lockRepeat = System.currentTimeMillis() + 2500L;
         }

         this.control("PUT", "/me/player/repeat?state=" + v, () -> this.lockRepeat = 0L);
      }
   }

   public void setVolume(int v, boolean immediate) {
      if (this.requireLogin()) {
         v = Math.max(0, Math.min(100, v));
         synchronized (this) {
            this.state = this.state.withVolume(v);
            this.lockVolume = System.currentTimeMillis() + 2500L + 500L;
            this.pendingVolume = v;
            if (this.volumeFuture != null) {
               this.volumeFuture.cancel(false);
            }

            this.volumeFuture = this.exec.schedule(this::flushVolume, immediate ? 0L : 180L, TimeUnit.MILLISECONDS);
         }
      }
   }

   private void flushVolume() {
      int v = this.pendingVolume;
      if (v >= 0) {
         this.pendingVolume = -1;
         this.lockVolume = System.currentTimeMillis() + 2500L;
         this.doControl("PUT", "/me/player/volume?volume_percent=" + v, () -> this.lockVolume = 0L, false);
      }
   }

   public void toggleMute() {
      int vol = this.state.volume;
      if (vol >= 0) {
         if (vol > 0) {
            this.volumeBeforeMute = vol;
            this.setVolume(0, true);
         } else {
            this.setVolume(this.volumeBeforeMute > 0 ? this.volumeBeforeMute : 50, true);
         }
      }
   }

   public void toggleLike() {
      if (this.requireLogin()) {
         String uri;
         boolean v;
         synchronized (this) {
            uri = this.state.uri;
            if (uri == null || !uri.startsWith("spotify:track:") && !uri.startsWith("spotify:episode:")) {
               this.setStatus("Dieser Titel kann nicht gespeichert werden");
               return;
            }

            v = !this.state.liked;
            this.state = this.state.withLiked(v);
            this.lockLiked = System.currentTimeMillis() + 60000L;
         }

         this.exec.execute(() -> {
            try {
               String method = v ? "PUT" : "DELETE";
               SpotifyApi.Resp r = this.request(method, "/me/library?uris=" + enc(uri), true);
               if ((r.code == 404 || r.code == 405) && uri.startsWith("spotify:track:")) {
                  r = this.request(method, "/me/tracks?ids=" + enc(uri.substring(14)), true);
               }

               if (r.code >= 200 && r.code < 300) {
                  this.likedCheckedFor = uri;
                  this.lockLiked = System.currentTimeMillis() + 5000L;
               } else {
                  this.lockLiked = 0L;
                  this.likedCheckedFor = null;
                  String msg = apiError(r, "Speichern fehlgeschlagen");
                  this.setStatus(msg);
                  this.errorListener.accept(msg);
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

   private void control(String method, String path, Runnable onFail) {
      this.exec.execute(() -> this.doControl(method, path, onFail, true));
   }

   private void doControl(String method, String path, Runnable onFail, boolean repoll) {
      try {
         SpotifyApi.Resp r = this.request(method, path, true);
         if (r.code >= 200 && r.code < 300) {
            if (repoll) {
               this.schedulePoll(!path.contains("next") && !path.contains("previous") ? 900L : 350L);
            }

            return;
         }

         if (onFail != null) {
            onFail.run();
         }

         if (r.code == 429) {
            this.retryAfter(r);
         }

         String fallback = r.code == 404
            ? "Kein aktives Spotify-Gerät gefunden"
            : (r.code == 403 ? "Aktion nicht erlaubt (Spotify Premium nötig?)" : "Fehler " + r.code);
         String msg = apiError(r, fallback);
         this.setStatus(msg);
         this.errorListener.accept(msg);
         this.schedulePoll(200L);
      } catch (Exception var8) {
         if (onFail != null) {
            onFail.run();
         }

         this.setStatus("Netzwerkfehler: " + var8.getMessage());
         this.schedulePoll(1000L);
      }
   }

   private static String apiError(SpotifyApi.Resp r, String fallback) {
      try {
         Object o = Json.parse(r.body);
         Map<String, Object> err = Json.obj(o, "error");
         String msg = err != null ? Json.str(err, "message", null) : Json.str(o, "error_description", null);
         if (msg != null && !msg.isBlank()) {
            String reason = err != null ? Json.str(err, "reason", "") : "";
            if ("PREMIUM_REQUIRED".equals(reason)) {
               return "Spotify Premium erforderlich";
            }

            if ("NO_ACTIVE_DEVICE".equals(reason)) {
               return "Kein aktives Spotify-Gerät";
            }

            return fallback + ": " + msg;
         }
      } catch (Exception var6) {
      }

      return fallback;
   }

   public List<String[]> search(String q) {
      List<String[]> out = new ArrayList<>();
      if (this.isLoggedIn() && q != null && !q.isBlank()) {
         try {
            SpotifyApi.Resp r = this.request("GET", "/search?type=track&limit=30&q=" + enc(q), false);
            if (r.code != 200) {
               this.lastBrowseError = apiError(r, "Fehler " + r.code);
               return out;
            }

            Map<String, Object> tr = Json.obj(Json.parse(r.body), "tracks");
            List<Object> items = Json.arr(tr, "items");
            if (items != null) {
               for (Object it : items) {
                  List<Object> ar = Json.arr(it, "artists");
                  String artist = ar != null && !ar.isEmpty() ? Json.str(ar.get(0), "name", "") : "";
                  out.add(new String[]{Json.str(it, "name", "?"), artist, Json.str(it, "uri", "")});
               }
            }

            this.lastBrowseError = null;
         } catch (Exception var10) {
            this.lastBrowseError = "Netzwerkfehler";
         }

         return out;
      } else {
         return out;
      }
   }

   public List<String[]> playlists() {
      List<String[]> out = new ArrayList<>();
      if (!this.isLoggedIn()) {
         return out;
      } else {
         try {
            SpotifyApi.Resp r = this.request("GET", "/me/playlists?limit=50", false);
            if (r.code == 401 || r.code == 403) {
               this.lastBrowseError = "Für Playlists einmal neu anmelden (H)";
               return out;
            }

            if (r.code != 200) {
               this.lastBrowseError = apiError(r, "Fehler " + r.code);
               return out;
            }

            List<Object> items = Json.arr(Json.parse(r.body), "items");
            if (items != null) {
               for (Object it : items) {
                  if (it != null) {
                     Map<String, Object> own = Json.obj(it, "owner");
                     out.add(new String[]{Json.str(it, "name", "?"), own == null ? "" : Json.str(own, "display_name", ""), Json.str(it, "uri", "")});
                  }
               }
            }

            this.lastBrowseError = null;
         } catch (Exception var7) {
            this.lastBrowseError = "Netzwerkfehler";
         }

         return out;
      }
   }

   public void playUri(String uri) {
      if (uri != null && !uri.isEmpty() && this.requireLogin()) {
         String body = uri.startsWith("spotify:track:") ? "{\"uris\":[\"" + uri + "\"]}" : "{\"context_uri\":\"" + uri + "\"}";
         this.exec.execute(() -> {
            try {
               SpotifyApi.Resp r = this.requestBody("PUT", "/me/player/play", body);
               if (r.code == 401 && this.refreshToken()) {
                  r = this.requestBody("PUT", "/me/player/play", body);
               }

               if (r.code >= 200 && r.code < 300) {
                  this.schedulePoll(700L);
                  return;
               }

               String msg = apiError(r, r.code == 404 ? "Kein aktives Spotify-Gerät gefunden" : "Fehler " + r.code);
               this.setStatus(msg);
               this.errorListener.accept(msg);
            } catch (Exception var4) {
               this.setStatus("Netzwerkfehler: " + var4.getMessage());
            }
         });
      }
   }

   private SpotifyApi.Resp requestBody(String method, String path, String json) throws IOException {
      this.ensureToken();
      HttpURLConnection c = (HttpURLConnection)URI.create("https://api.spotify.com/v1" + path).toURL().openConnection();

      SpotifyApi.Resp var8;
      try {
         c.setRequestMethod(method);
         c.setConnectTimeout(6000);
         c.setReadTimeout(8000);
         c.setRequestProperty("Authorization", "Bearer " + this.cfg.accessToken);
         c.setRequestProperty("Content-Type", "application/json");
         byte[] b = json.getBytes(StandardCharsets.UTF_8);
         c.setDoOutput(true);
         c.setFixedLengthStreamingMode(b.length);

         try (OutputStream o = c.getOutputStream()) {
            o.write(b);
         }

         SpotifyApi.Resp r = new SpotifyApi.Resp();
         r.code = c.getResponseCode();
         InputStream in = r.code >= 400 ? c.getErrorStream() : c.getInputStream();
         r.body = readAll(in);
         var8 = r;
      } finally {
         c.disconnect();
      }

      return var8;
   }

   private SpotifyApi.Resp request(String method, String path, boolean write) throws IOException {
      SpotifyApi.Resp r = this.requestOnce(method, path, write);
      if (r.code == 401 && this.refreshToken()) {
         r = this.requestOnce(method, path, write);
      }

      return r;
   }

   private SpotifyApi.Resp requestOnce(String method, String path, boolean write) throws IOException {
      this.ensureToken();
      HttpURLConnection c = (HttpURLConnection)URI.create("https://api.spotify.com/v1" + path).toURL().openConnection();

      SpotifyApi.Resp var7;
      try {
         c.setRequestMethod(method);
         c.setConnectTimeout(6000);
         c.setReadTimeout(8000);
         c.setUseCaches(false);
         c.setRequestProperty("Authorization", "Bearer " + this.cfg.accessToken);
         c.setRequestProperty("Accept", "application/json");
         if (write && !"DELETE".equals(method) && !"GET".equals(method)) {
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "application/json");
            c.setFixedLengthStreamingMode(0);
            c.getOutputStream().close();
         }

         SpotifyApi.Resp r = new SpotifyApi.Resp();
         r.code = c.getResponseCode();
         r.retryAfter = c.getHeaderField("Retry-After");
         InputStream in = r.code >= 400 ? c.getErrorStream() : c.getInputStream();
         r.body = readAll(in);
         var7 = r;
      } finally {
         c.disconnect();
      }

      return var7;
   }

   private static String readAll(InputStream in) throws IOException {
      if (in == null) {
         return "";
      } else {
         String var5;
         try (InputStream i = in) {
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];

            int n;
            while ((n = i.read(buf)) > 0) {
               o.write(buf, 0, n);
            }

            var5 = o.toString(StandardCharsets.UTF_8);
         }

         return var5;
      }
   }

   private static String enc(String s) {
      return URLEncoder.encode(s, StandardCharsets.UTF_8);
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
            Map<String, String> form = new HashMap<>();
            form.put("grant_type", "refresh_token");
            form.put("refresh_token", this.cfg.refreshToken);
            form.put("client_id", this.cfg.clientId);
            SpotifyApi.Resp r = this.tokenRequest(form);
            if (r.code == 200) {
               this.storeTokens(Json.parse(r.body));
               return true;
            }

            if (r.code == 400 || r.code == 401) {
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

   private void storeTokens(Object o) {
      String at = Json.str(o, "access_token", "");
      if (!at.isEmpty()) {
         this.cfg.accessToken = at;
         String rt = Json.str(o, "refresh_token", null);
         if (rt != null && !rt.isEmpty()) {
            this.cfg.refreshToken = rt;
         }

         this.cfg.expiresAt = System.currentTimeMillis() + Json.num(o, "expires_in", 3600L) * 1000L;
         this.cfg.save();
      }
   }

   private SpotifyApi.Resp tokenRequest(Map<String, String> form) throws IOException {
      StringBuilder b = new StringBuilder();

      for (Entry<String, String> e : form.entrySet()) {
         if (b.length() > 0) {
            b.append('&');
         }

         b.append(enc(e.getKey())).append('=').append(enc(e.getValue()));
      }

      byte[] data = b.toString().getBytes(StandardCharsets.UTF_8);
      HttpURLConnection c = (HttpURLConnection)URI.create("https://accounts.spotify.com/api/token").toURL().openConnection();

      SpotifyApi.Resp var6;
      try {
         c.setRequestMethod("POST");
         c.setConnectTimeout(8000);
         c.setReadTimeout(10000);
         c.setDoOutput(true);
         c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         c.setFixedLengthStreamingMode(data.length);

         try (OutputStream os = c.getOutputStream()) {
            os.write(data);
         }

         SpotifyApi.Resp r = new SpotifyApi.Resp();
         r.code = c.getResponseCode();
         r.body = readAll(r.code >= 400 ? c.getErrorStream() : c.getInputStream());
         var6 = r;
      } finally {
         c.disconnect();
      }

      return var6;
   }

   public String startLogin() {
      if (this.cfg.clientId != null && !this.cfg.clientId.isBlank()) {
         this.stopAuthServer();
         String verifier = this.randomString(64);

         String challenge;
         try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
            challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
         } catch (Exception var9) {
            this.setStatus("SHA-256 nicht verfügbar");
            return null;
         }

         String st = this.randomString(24);
         String redirect = this.cfg.redirectUri();
         String url = "https://accounts.spotify.com/authorize?client_id="
            + enc(this.cfg.clientId.trim())
            + "&response_type=code&redirect_uri="
            + enc(redirect)
            + "&code_challenge_method=S256&code_challenge="
            + enc(challenge)
            + "&scope="
            + enc(
               "user-read-playback-state user-modify-playback-state user-read-currently-playing user-library-read user-library-modify playlist-read-private playlist-read-collaborative"
            )
            + "&state="
            + enc(st);

         ServerSocket ss;
         try {
            ss = new ServerSocket();
            ss.setReuseAddress(false);
            ss.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), this.cfg.redirectPort));
            ss.setSoTimeout(1000);
         } catch (IOException var8) {
            this.handleBusyPort(this.cfg.redirectPort);
            return null;
         }

         this.authSocket = ss;
         this.authRunning = true;
         this.setStatus("Warte auf Login im Browser...");
         Thread t = new Thread(() -> this.runAuthServer(ss, st, verifier, redirect), "SpotifyHUD-Auth");
         t.setDaemon(true);
         t.start();
         return url;
      } else {
         this.setStatus("Bitte zuerst deine Spotify Client-ID eintragen");
         return null;
      }
   }

   private void handleBusyPort(int port) {
      this.setStatus("Port " + port + " ist belegt - prüfe...");
      Thread t = new Thread(
         () -> {
            String owner = PortInfo.owner(port);
            int free = PortInfo.findFree(port + 1, port + 200);
            String who = owner != null ? " von " + owner : " von einem anderen Programm";
            if (free > 0) {
               this.cfg.redirectPort = free;
               this.cfg.save();
               SpotifyHudMod.copyToClipboard(this.cfg.redirectUri());
               this.setStatus(
                  "Port "
                     + port
                     + " wird"
                     + who
                     + " benutzt. Neuer Port "
                     + free
                     + ": Redirect-URI "
                     + this.cfg.redirectUri()
                     + " (kopiert) im Dashboard hinzufügen, dann erneut Anmelden"
               );
            } else {
               this.setStatus("Port " + port + " wird" + who + " benutzt - bitte Programm schließen");
            }
         },
         "SpotifyHUD-Port"
      );
      t.setDaemon(true);
      t.start();
   }

   public void cancelLogin() {
      this.stopAuthServer();
      if (!this.isLoggedIn()) {
         this.setStatus("Login abgebrochen");
      }
   }

   private void stopAuthServer() {
      ServerSocket s = this.authSocket;
      this.authSocket = null;
      this.authRunning = false;
      if (s != null) {
         try {
            s.close();
         } catch (IOException var3) {
         }
      }
   }

   private void runAuthServer(ServerSocket ss, String expectedState, String verifier, String redirect) {
      long deadline = System.currentTimeMillis() + 300000L;

      try {
         while (this.authSocket == ss && System.currentTimeMillis() < deadline) {
            Socket s;
            try {
               s = ss.accept();
            } catch (SocketTimeoutException var33) {
               continue;
            } catch (IOException var34) {
               break;
            }

            try (Socket sock = s) {
               sock.setSoTimeout(5000);
               String line = readLine(sock.getInputStream());

               String h;
               do {
                  h = readLine(sock.getInputStream());
               } while (h != null && !h.isEmpty());

               if (line != null) {
                  String[] parts = line.split(" ");
                  if (parts.length >= 2) {
                     String target = parts[1];
                     String path = target.contains("?") ? target.substring(0, target.indexOf(63)) : target;
                     if (path.equals("/callback")) {
                        Map<String, String> q = parseQuery(target.contains("?") ? target.substring(target.indexOf(63) + 1) : "");
                        if (q.containsKey("error")) {
                           this.setStatus("Login abgelehnt: " + q.get("error"));
                           respond(sock, 200, page(false, "Login abgebrochen: " + q.get("error")));
                           break;
                        }

                        if (expectedState.equals(q.get("state")) && q.containsKey("code")) {
                           Map<String, String> form = new HashMap<>();
                           form.put("grant_type", "authorization_code");
                           form.put("code", q.get("code"));
                           form.put("redirect_uri", redirect);
                           form.put("client_id", this.cfg.clientId.trim());
                           form.put("code_verifier", verifier);
                           SpotifyApi.Resp r = this.tokenRequest(form);
                           if (r.code == 200) {
                              synchronized (this.tokenLock) {
                                 this.storeTokens(Json.parse(r.body));
                              }

                              this.setStatus("Verbunden");
                              respond(sock, 200, page(true, "Spotify HUD ist verbunden. Du kannst dieses Fenster schließen und zu Minecraft zurückkehren."));
                              this.schedulePoll(0L);
                           } else {
                              this.setStatus(apiError(r, "Token-Austausch fehlgeschlagen"));
                              respond(sock, 200, page(false, "Token-Austausch fehlgeschlagen (" + r.code + "): " + r.body));
                           }
                           break;
                        }

                        this.setStatus("Ungültige Antwort (state) - bitte erneut versuchen");
                        respond(sock, 400, page(false, "Ungültige Anfrage. Bitte erneut versuchen."));
                        break;
                     }

                     respond(sock, 404, "Not found");
                  }
               }
            } catch (IOException var36) {
               SpotifyHudMod.LOG("Callback-Fehler: " + var36);
            }
         }

         if (System.currentTimeMillis() >= deadline && !this.isLoggedIn()) {
            this.setStatus("Login-Zeit abgelaufen");
         }
      } finally {
         if (this.authSocket == ss) {
            this.authSocket = null;
         }

         this.authRunning = false;

         try {
            ss.close();
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

   private static String readLine(InputStream in) throws IOException {
      StringBuilder b = new StringBuilder();

      int c;
      while ((c = in.read()) != -1 && c != 10) {
         if (c != 13) {
            b.append((char)c);
         }

         if (b.length() > 8192) {
            break;
         }
      }

      return c == -1 && b.length() == 0 ? null : b.toString();
   }

   private static Map<String, String> parseQuery(String q) {
      Map<String, String> m = new HashMap<>();

      for (String kv : q.split("&")) {
         if (!kv.isEmpty()) {
            int i = kv.indexOf(61);
            String k = i < 0 ? kv : kv.substring(0, i);
            String v = i < 0 ? "" : kv.substring(i + 1);
            m.put(URLDecoder.decode(k, StandardCharsets.UTF_8), URLDecoder.decode(v, StandardCharsets.UTF_8));
         }
      }

      return m;
   }

   private static void respond(Socket s, int code, String html) throws IOException {
      byte[] body = html.getBytes(StandardCharsets.UTF_8);
      String head = "HTTP/1.1 "
         + code
         + (code == 200 ? " OK" : (code == 404 ? " Not Found" : " Bad Request"))
         + "\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: "
         + body.length
         + "\r\nConnection: close\r\n\r\n";
      OutputStream o = s.getOutputStream();
      o.write(head.getBytes(StandardCharsets.US_ASCII));
      o.write(body);
      o.flush();
   }

   private static String page(boolean ok, String msg) {
      String safe = msg.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
      return "<!doctype html><html><head><meta charset=utf-8><title>Spotify HUD</title></head><body style=\"background:#121212;color:#fff;font-family:Segoe UI,Arial,sans-serif;display:flex;align-items:center;justify-content:center;height:100vh;margin:0\"><div style=\"text-align:center\"><h1 style=\"color:"
         + (ok ? "#1ed760" : "#f15e6c")
         + "\">"
         + (ok ? "Verbunden" : "Fehler")
         + "</h1><p style=\"color:#b3b3b3\">"
         + safe
         + "</p></div></body></html>";
   }

   private String randomString(int len) {
      String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
      StringBuilder b = new StringBuilder(len);

      for (int i = 0; i < len; i++) {
         b.append(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
               .charAt(this.rnd.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".length()))
         );
      }

      return b.toString();
   }

   private static final class Resp {
      int code;
      String body = "";
      String retryAfter;
   }
}
