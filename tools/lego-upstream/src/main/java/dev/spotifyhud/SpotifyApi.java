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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Spotify Web API client (Authorization Code + PKCE, no client secret needed). */
public final class SpotifyApi {
    private static final String API = "https://api.spotify.com/v1";
    private static final String ACCOUNTS = "https://accounts.spotify.com";
    private static final String SCOPES = "user-read-playback-state user-modify-playback-state user-read-currently-playing user-library-read user-library-modify playlist-read-private playlist-read-collaborative";
    private static final long LOCK_MS = 2500;

    private final Config cfg;
    private final ScheduledExecutorService exec;
    private final ScheduledExecutorService artExec;
    private final SecureRandom rnd = new SecureRandom();

    private volatile PlayerState state = PlayerState.EMPTY;
    private volatile String status = "";
    private volatile long statusTime = 0;
    private volatile boolean noDevice = true;
    private volatile boolean authRunning = false;
    private volatile ServerSocket authSocket;
    private volatile long pauseUntil = 0;

    // Album art (java.awt.image.BufferedImage stored as Object so AWT is only touched in ArtLoader)
    private volatile Object artImage;
    private volatile String artImageUrl;
    private volatile String artLoadingUrl;
    private volatile int artVersion = 0;

    private long lockPlaying, lockShuffle, lockRepeat, lockVolume, lockProgress, lockLiked;
    private String likedCheckedFor;
    private int pollCounter = 0;
    private int volumeBeforeMute = 50;
    private ScheduledFuture<?> pollFuture;
    private ScheduledFuture<?> volumeFuture;
    private volatile int pendingVolume = -1;
    private volatile boolean lightMode = false;       // true while Windows media detection is active
    private volatile long latencyMs = 0;              // half round-trip time to the Spotify API
    private volatile java.util.function.Consumer<String> errorListener = s -> { };

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
        if (isLoggedIn()) setStatus("Verbunden");
        else setStatus("Nicht verbunden");
        schedulePoll(500);
    }

    // ------------------------------------------------------------------ getters
    public PlayerState state() { return state; }
    public String status() { return status; }
    public long statusTime() { return statusTime; }
    public boolean isLoggedIn() { return cfg.refreshToken != null && !cfg.refreshToken.isEmpty(); }
    public boolean authRunning() { return authRunning; }
    public boolean noDevice() { return noDevice; }
    public Object artImage() { return artImage; }
    public int artVersion() { return artVersion; }
    public long latencyMs() { return latencyMs; }
    public void setLightMode(boolean v) { lightMode = v; }
    public void setErrorListener(java.util.function.Consumer<String> l) { errorListener = l != null ? l : s -> { }; }

    private void setStatus(String s) { status = s; statusTime = System.currentTimeMillis(); }

    // ------------------------------------------------------------------ polling
    private synchronized void schedulePoll(long delayMs) {
        if (pollFuture != null) pollFuture.cancel(false);
        pollFuture = exec.schedule(this::pollSafe, Math.max(0, delayMs), TimeUnit.MILLISECONDS);
    }

    private void pollSafe() {
        long next = 1000;
        try {
            next = poll();
        } catch (Throwable t) {
            SpotifyHudMod.LOG("Poll-Fehler: " + t);
            next = 3000;
        }
        schedulePoll(next);
    }

    private long poll() throws Exception {
        long now = System.currentTimeMillis();
        if (now < pauseUntil) return pauseUntil - now;
        if (!isLoggedIn() || cfg.clientId.isEmpty()) {
            state = PlayerState.EMPTY;
            noDevice = true;
            return 2000;
        }
        long t0 = System.currentTimeMillis();
        Resp r = request("GET", "/me/player?additional_types=episode", false);
        long t1 = System.currentTimeMillis();
        long half = Math.max(0, Math.min(3000, (t1 - t0) / 2));
        latencyMs = latencyMs == 0 ? half : (latencyMs * 3 + half) / 4;
        if (r.code == 429) return retryAfter(r);
        if (r.code == 204 || (r.code == 200 && r.body.isBlank())) {
            noDevice = true;
            state = PlayerState.EMPTY;
            return 2500;
        }
        if (r.code != 200) {
            if (r.code == 401 || r.code == 403) setStatus(apiError(r, "Autorisierung fehlgeschlagen"));
            return 4000;
        }
        noDevice = false;
        Object root = Json.parse(r.body);
        applyPlayer(root, t1 - half);
        pollCounter++;
        PlayerState s = state;
        if (s.uri != null && s.uri.startsWith("spotify:track:")
                && (!s.uri.equals(likedCheckedFor) || pollCounter % 12 == 0)
                && System.currentTimeMillis() >= lockLiked) {
            checkLiked(s.uri);
        }
        if (lightMode) return 4000;
        return s.playing ? 1000 : 2000;
    }

    private long retryAfter(Resp r) {
        long sec = 5;
        try { if (r.retryAfter != null) sec = Long.parseLong(r.retryAfter.trim()); } catch (NumberFormatException ignored) { }
        sec = Math.max(1, Math.min(120, sec));
        pauseUntil = System.currentTimeMillis() + sec * 1000;
        return sec * 1000;
    }

    private synchronized void applyPlayer(Object root, long sampledAt) {
        long now = System.currentTimeMillis();
        PlayerState old = state;
        Map<String, Object> item = Json.obj(root, "item");
        Map<String, Object> device = Json.obj(root, "device");

        boolean playing = Json.bool(root, "is_playing", false);
        boolean shuffle = Json.bool(root, "shuffle_state", false);
        String repeat = Json.str(root, "repeat_state", "off");
        long progress = Json.num(root, "progress_ms", 0);
        int volume = device != null && device.get("volume_percent") instanceof Number
                ? ((Number) device.get("volume_percent")).intValue() : -1;
        String type = Json.str(root, "currently_playing_type", "track");

        String title, artist, uri = null, id = null, art = null;
        long duration = 0;
        boolean has;
        if (item != null) {
            has = true;
            title = Json.str(item, "name", "");
            uri = Json.str(item, "uri", null);
            id = Json.str(item, "id", null);
            duration = Json.num(item, "duration_ms", 0);
            List<Object> images;
            if ("episode".equals(Json.str(item, "type", "track"))) {
                Map<String, Object> show = Json.obj(item, "show");
                artist = show != null ? Json.str(show, "name", "") : "";
                images = Json.arr(item, "images");
                if ((images == null || images.isEmpty()) && show != null) images = Json.arr(show, "images");
            } else {
                StringBuilder b = new StringBuilder();
                List<Object> artists = Json.arr(item, "artists");
                if (artists != null) for (Object a : artists) {
                    String n = Json.str(a, "name", "");
                    if (n.isEmpty()) continue;
                    if (b.length() > 0) b.append(", ");
                    b.append(n);
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

        boolean sameTrack = uri != null ? uri.equals(old.uri) : (old.uri == null && title.equals(old.title));
        if (now < lockPlaying) playing = old.playing;
        if (now < lockShuffle) shuffle = old.shuffle;
        if (now < lockRepeat) repeat = old.repeat;
        if (now < lockVolume && old.volume >= 0) volume = old.volume;
        // progress_ms was valid when the server answered: compensate half the round trip ("ping")
        long sampled = Math.min(now, sampledAt);
        if (now < lockProgress && sameTrack) { progress = old.currentProgress(); sampled = now; }
        boolean liked = sameTrack && old.liked;
        if (!sameTrack) lockLiked = 0;

        state = new PlayerState(has, id, uri, title, artist, art, duration, progress, sampled, playing, shuffle,
                repeat, volume, liked);

        if (art == null) {
            if (artImageUrl != null) { artImage = null; artImageUrl = null; artVersion++; }
        } else if (!art.equals(artImageUrl) && !art.equals(artLoadingUrl)) {
            final String url = art;
            artLoadingUrl = url;
            artExec.execute(() -> {
                Object img = ArtLoader.load(url);
                if (url.equals(artLoadingUrl)) {
                    artImage = img;
                    artImageUrl = url;
                    artLoadingUrl = null;
                    artVersion++;
                }
            });
        }
    }

    private static String pickImage(List<Object> images) {
        if (images == null || images.isEmpty()) return null;
        String best = null; long bestW = Long.MAX_VALUE;
        String largest = null; long largestW = -1;
        for (Object o : images) {
            String url = Json.str(o, "url", null);
            if (url == null) continue;
            long w = Json.num(o, "width", 300);
            if (w >= 200 && w < bestW) { best = url; bestW = w; }
            if (w > largestW) { largest = url; largestW = w; }
        }
        return best != null ? best : largest;
    }

    private void checkLiked(String uri) {
        try {
            Resp r = request("GET", "/me/library/contains?uris=" + enc(uri), false);
            if (r.code == 404 && uri.startsWith("spotify:track:")) {
                r = request("GET", "/me/tracks/contains?ids=" + enc(uri.substring(14)), false);
            }
            if (r.code == 200) {
                Object o = Json.parse(r.body);
                if (o instanceof List && !((List<?>) o).isEmpty() && ((List<?>) o).get(0) instanceof Boolean) {
                    boolean v = (Boolean) ((List<?>) o).get(0);
                    synchronized (this) {
                        if (uri.equals(state.uri) && System.currentTimeMillis() >= lockLiked) state = state.withLiked(v);
                    }
                }
                likedCheckedFor = uri;
            } else if (r.code == 429) {
                retryAfter(r);
            } else {
                likedCheckedFor = uri; // don't hammer on unsupported scope etc.
            }
        } catch (Exception e) {
            SpotifyHudMod.LOG("Like-Status Fehler: " + e);
        }
    }

    // ------------------------------------------------------------------ actions (callable from any thread)
    public void togglePlay() {
        if (!requireLogin()) return;
        final boolean play;
        synchronized (this) {
            play = !state.playing;
            state = state.withPlaying(play);
            lockPlaying = System.currentTimeMillis() + LOCK_MS;
        }
        control("PUT", play ? "/me/player/play" : "/me/player/pause", () -> lockPlaying = 0);
    }

    public void next() {
        if (!requireLogin()) return;
        synchronized (this) { state = state.withProgress(0); lockProgress = 0; }
        control("POST", "/me/player/next", null);
    }

    public void previous() {
        if (!requireLogin()) return;
        synchronized (this) { state = state.withProgress(0); lockProgress = 0; }
        control("POST", "/me/player/previous", null);
    }

    public void seek(long ms) {
        if (!requireLogin()) return;
        synchronized (this) {
            long d = state.durationMs;
            ms = Math.max(0, d > 0 ? Math.min(ms, d - 500) : ms);
            state = state.withProgress(ms);
            lockProgress = System.currentTimeMillis() + LOCK_MS;
        }
        control("PUT", "/me/player/seek?position_ms=" + ms, () -> lockProgress = 0);
    }

    public void toggleShuffle() {
        if (!requireLogin()) return;
        final boolean v;
        synchronized (this) {
            v = !state.shuffle;
            state = state.withShuffle(v);
            lockShuffle = System.currentTimeMillis() + LOCK_MS;
        }
        control("PUT", "/me/player/shuffle?state=" + v, () -> lockShuffle = 0);
    }

    public void cycleRepeat() {
        if (!requireLogin()) return;
        final String v;
        synchronized (this) {
            String cur = state.repeat;
            v = "off".equals(cur) ? "context" : "context".equals(cur) ? "track" : "off";
            state = state.withRepeat(v);
            lockRepeat = System.currentTimeMillis() + LOCK_MS;
        }
        control("PUT", "/me/player/repeat?state=" + v, () -> lockRepeat = 0);
    }

    /** Sets volume optimistically; the request is debounced so dragging/scrolling doesn't spam the API. */
    public void setVolume(int v, boolean immediate) {
        if (!requireLogin()) return;
        v = Math.max(0, Math.min(100, v));
        synchronized (this) {
            state = state.withVolume(v);
            lockVolume = System.currentTimeMillis() + LOCK_MS + 500;
            pendingVolume = v;
            if (volumeFuture != null) volumeFuture.cancel(false);
            volumeFuture = exec.schedule(this::flushVolume, immediate ? 0 : 180, TimeUnit.MILLISECONDS);
        }
    }

    private void flushVolume() {
        int v = pendingVolume;
        if (v < 0) return;
        pendingVolume = -1;
        lockVolume = System.currentTimeMillis() + LOCK_MS;
        doControl("PUT", "/me/player/volume?volume_percent=" + v, () -> lockVolume = 0, false);
    }

    public void toggleMute() {
        int vol = state.volume;
        if (vol < 0) return;
        if (vol > 0) { volumeBeforeMute = vol; setVolume(0, true); }
        else setVolume(volumeBeforeMute > 0 ? volumeBeforeMute : 50, true);
    }

    public void toggleLike() {
        if (!requireLogin()) return;
        final String uri;
        final boolean v;
        synchronized (this) {
            uri = state.uri;
            if (uri == null || !(uri.startsWith("spotify:track:") || uri.startsWith("spotify:episode:"))) {
                setStatus("Dieser Titel kann nicht gespeichert werden");
                return;
            }
            v = !state.liked;
            state = state.withLiked(v);
            lockLiked = System.currentTimeMillis() + 60_000;
        }
        exec.execute(() -> {
            try {
                String method = v ? "PUT" : "DELETE";
                Resp r = request(method, "/me/library?uris=" + enc(uri), true);
                if ((r.code == 404 || r.code == 405) && uri.startsWith("spotify:track:")) {
                    r = request(method, "/me/tracks?ids=" + enc(uri.substring(14)), true);
                }
                if (r.code >= 200 && r.code < 300) {
                    likedCheckedFor = uri;
                    lockLiked = System.currentTimeMillis() + 5000;
                } else {
                    lockLiked = 0;
                    likedCheckedFor = null;
                    String msg = apiError(r, "Speichern fehlgeschlagen");
                    setStatus(msg);
                    errorListener.accept(msg);
                    schedulePoll(0);
                }
            } catch (Exception e) {
                lockLiked = 0;
                setStatus("Netzwerkfehler: " + e.getMessage());
            }
        });
    }

    private boolean requireLogin() {
        if (isLoggedIn()) return true;
        setStatus("Nicht verbunden - drücke H und melde dich an");
        return false;
    }

    private void control(String method, String path, Runnable onFail) {
        exec.execute(() -> doControl(method, path, onFail, true));
    }

    private void doControl(String method, String path, Runnable onFail, boolean repoll) {
        try {
            Resp r = request(method, path, true);
            if (r.code >= 200 && r.code < 300) {
                if (repoll) schedulePoll(path.contains("next") || path.contains("previous") ? 350 : 900);
                return;
            }
            if (onFail != null) onFail.run();
            if (r.code == 429) retryAfter(r);
            String fallback = r.code == 404 ? "Kein aktives Spotify-Gerät gefunden"
                    : r.code == 403 ? "Aktion nicht erlaubt (Spotify Premium nötig?)"
                    : "Fehler " + r.code;
            String msg = apiError(r, fallback);
            setStatus(msg);
            errorListener.accept(msg);
            schedulePoll(200);
        } catch (Exception e) {
            if (onFail != null) onFail.run();
            setStatus("Netzwerkfehler: " + e.getMessage());
            schedulePoll(1000);
        }
    }

    private static String apiError(Resp r, String fallback) {
        try {
            Object o = Json.parse(r.body);
            Map<String, Object> err = Json.obj(o, "error");
            String msg = err != null ? Json.str(err, "message", null) : Json.str(o, "error_description", null);
            if (msg != null && !msg.isBlank()) {
                String reason = err != null ? Json.str(err, "reason", "") : "";
                if ("PREMIUM_REQUIRED".equals(reason)) return "Spotify Premium erforderlich";
                if ("NO_ACTIVE_DEVICE".equals(reason)) return "Kein aktives Spotify-Gerät";
                return fallback + ": " + msg;
            }
        } catch (Exception ignored) { }
        return fallback;
    }


    // ------------------------------------------------------------------ in-game browser (search, playlists, play)
    /** Tracks for a search: {title, artist, uri}. Blocking - call from a background thread. */
    public java.util.List<String[]> search(String q) {
        java.util.List<String[]> out = new java.util.ArrayList<>();
        if (!isLoggedIn() || q == null || q.isBlank()) return out;
        try {
            Resp r = request("GET", "/search?type=track&limit=30&q=" + enc(q), false);
            if (r.code != 200) { lastBrowseError = apiError(r, "Fehler " + r.code); return out; }
            Map<String, Object> tr = Json.obj(Json.parse(r.body), "tracks");
            java.util.List<Object> items = Json.arr(tr, "items");
            if (items != null) for (Object it : items) {
                java.util.List<Object> ar = Json.arr(it, "artists");
                String artist = ar != null && !ar.isEmpty() ? Json.str(ar.get(0), "name", "") : "";
                out.add(new String[]{Json.str(it, "name", "?"), artist, Json.str(it, "uri", "")});
            }
            lastBrowseError = null;
        } catch (Exception e) { lastBrowseError = "Netzwerkfehler"; }
        return out;
    }

    /** Your playlists: {name, owner, uri}. Blocking. */
    public java.util.List<String[]> playlists() {
        java.util.List<String[]> out = new java.util.ArrayList<>();
        if (!isLoggedIn()) return out;
        try {
            Resp r = request("GET", "/me/playlists?limit=50", false);
            if (r.code == 401 || r.code == 403) { lastBrowseError = "Für Playlists einmal neu anmelden (H)"; return out; }
            if (r.code != 200) { lastBrowseError = apiError(r, "Fehler " + r.code); return out; }
            java.util.List<Object> items = Json.arr(Json.parse(r.body), "items");
            if (items != null) for (Object it : items) {
                if (it == null) continue;
                Map<String, Object> own = Json.obj(it, "owner");
                out.add(new String[]{Json.str(it, "name", "?"), own == null ? "" : Json.str(own, "display_name", ""), Json.str(it, "uri", "")});
            }
            lastBrowseError = null;
        } catch (Exception e) { lastBrowseError = "Netzwerkfehler"; }
        return out;
    }

    public volatile String lastBrowseError;

    /** Plays a track uri, or a playlist/album uri (context). */
    public void playUri(String uri) {
        if (uri == null || uri.isEmpty() || !requireLogin()) return;
        final String body = uri.startsWith("spotify:track:") ? "{\"uris\":[\"" + uri + "\"]}" : "{\"context_uri\":\"" + uri + "\"}";
        exec.execute(() -> {
            try {
                Resp r = requestBody("PUT", "/me/player/play", body);
                if (r.code == 401 && refreshToken()) r = requestBody("PUT", "/me/player/play", body);
                if (r.code >= 200 && r.code < 300) { schedulePoll(700); return; }
                String msg = apiError(r, r.code == 404 ? "Kein aktives Spotify-Gerät gefunden" : "Fehler " + r.code);
                setStatus(msg);
                errorListener.accept(msg);
            } catch (Exception e) { setStatus("Netzwerkfehler: " + e.getMessage()); }
        });
    }

    private Resp requestBody(String method, String path, String json) throws IOException {
        ensureToken();
        HttpURLConnection c = (HttpURLConnection) URI.create(API + path).toURL().openConnection();
        try {
            c.setRequestMethod(method);
            c.setConnectTimeout(6000);
            c.setReadTimeout(8000);
            c.setRequestProperty("Authorization", "Bearer " + cfg.accessToken);
            c.setRequestProperty("Content-Type", "application/json");
            byte[] b = json.getBytes(StandardCharsets.UTF_8);
            c.setDoOutput(true);
            c.setFixedLengthStreamingMode(b.length);
            try (java.io.OutputStream o = c.getOutputStream()) { o.write(b); }
            Resp r = new Resp();
            r.code = c.getResponseCode();
            InputStream in = r.code >= 400 ? c.getErrorStream() : c.getInputStream();
            r.body = readAll(in);
            return r;
        } finally { c.disconnect(); }
    }

    // ------------------------------------------------------------------ HTTP
    private static final class Resp {
        int code; String body = ""; String retryAfter;
    }

    private Resp request(String method, String path, boolean write) throws IOException {
        Resp r = requestOnce(method, path, write);
        if (r.code == 401 && refreshToken()) r = requestOnce(method, path, write);
        return r;
    }

    private Resp requestOnce(String method, String path, boolean write) throws IOException {
        ensureToken();
        HttpURLConnection c = (HttpURLConnection) URI.create(API + path).toURL().openConnection();
        try {
            c.setRequestMethod(method);
            c.setConnectTimeout(6000);
            c.setReadTimeout(8000);
            c.setUseCaches(false);
            c.setRequestProperty("Authorization", "Bearer " + cfg.accessToken);
            c.setRequestProperty("Accept", "application/json");
            if (write && !"DELETE".equals(method) && !"GET".equals(method)) {
                c.setDoOutput(true);
                c.setRequestProperty("Content-Type", "application/json");
                c.setFixedLengthStreamingMode(0);
                c.getOutputStream().close();
            }
            Resp r = new Resp();
            r.code = c.getResponseCode();
            r.retryAfter = c.getHeaderField("Retry-After");
            InputStream in = r.code >= 400 ? c.getErrorStream() : c.getInputStream();
            r.body = readAll(in);
            return r;
        } finally {
            c.disconnect();
        }
    }

    private static String readAll(InputStream in) throws IOException {
        if (in == null) return "";
        try (InputStream i = in) {
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = i.read(buf)) > 0) o.write(buf, 0, n);
            return o.toString(StandardCharsets.UTF_8);
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private final Object tokenLock = new Object();

    private void ensureToken() {
        synchronized (tokenLock) {
            if (System.currentTimeMillis() > cfg.expiresAt - 60_000) refreshToken();
        }
    }

    private boolean refreshToken() {
        synchronized (tokenLock) {
            return refreshTokenLocked();
        }
    }

    private boolean refreshTokenLocked() {
        if (!isLoggedIn()) return false;
        try {
            Map<String, String> form = new HashMap<>();
            form.put("grant_type", "refresh_token");
            form.put("refresh_token", cfg.refreshToken);
            form.put("client_id", cfg.clientId);
            Resp r = tokenRequest(form);
            if (r.code == 200) {
                storeTokens(Json.parse(r.body));
                return true;
            }
            if (r.code == 400 || r.code == 401) {
                cfg.accessToken = "";
                cfg.refreshToken = "";
                cfg.expiresAt = 0;
                cfg.save();
                state = PlayerState.EMPTY;
                setStatus("Sitzung abgelaufen - bitte erneut anmelden");
            }
        } catch (Exception e) {
            SpotifyHudMod.LOG("Token-Refresh fehlgeschlagen: " + e);
        }
        return false;
    }

    private void storeTokens(Object o) {
        String at = Json.str(o, "access_token", "");
        if (at.isEmpty()) return;
        cfg.accessToken = at;
        String rt = Json.str(o, "refresh_token", null);
        if (rt != null && !rt.isEmpty()) cfg.refreshToken = rt;
        cfg.expiresAt = System.currentTimeMillis() + Json.num(o, "expires_in", 3600) * 1000L;
        cfg.save();
    }

    private Resp tokenRequest(Map<String, String> form) throws IOException {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, String> e : form.entrySet()) {
            if (b.length() > 0) b.append('&');
            b.append(enc(e.getKey())).append('=').append(enc(e.getValue()));
        }
        byte[] data = b.toString().getBytes(StandardCharsets.UTF_8);
        HttpURLConnection c = (HttpURLConnection) URI.create(ACCOUNTS + "/api/token").toURL().openConnection();
        try {
            c.setRequestMethod("POST");
            c.setConnectTimeout(8000);
            c.setReadTimeout(10000);
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            c.setFixedLengthStreamingMode(data.length);
            try (OutputStream os = c.getOutputStream()) { os.write(data); }
            Resp r = new Resp();
            r.code = c.getResponseCode();
            r.body = readAll(r.code >= 400 ? c.getErrorStream() : c.getInputStream());
            return r;
        } finally {
            c.disconnect();
        }
    }

    // ------------------------------------------------------------------ login (PKCE + local callback server)
    /** Starts the login flow. Returns the authorize URL (or null if it could not start). */
    public String startLogin() {
        if (cfg.clientId == null || cfg.clientId.isBlank()) {
            setStatus("Bitte zuerst deine Spotify Client-ID eintragen");
            return null;
        }
        stopAuthServer();
        String verifier = randomString(64);
        String challenge;
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
            challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            setStatus("SHA-256 nicht verfügbar");
            return null;
        }
        String st = randomString(24);
        String redirect = cfg.redirectUri();
        String url = ACCOUNTS + "/authorize?client_id=" + enc(cfg.clientId.trim())
                + "&response_type=code"
                + "&redirect_uri=" + enc(redirect)
                + "&code_challenge_method=S256"
                + "&code_challenge=" + enc(challenge)
                + "&scope=" + enc(SCOPES)
                + "&state=" + enc(st);

        ServerSocket ss;
        try {
            ss = new ServerSocket();
            ss.setReuseAddress(false);
            ss.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), cfg.redirectPort));
            ss.setSoTimeout(1000);
        } catch (IOException e) {
            handleBusyPort(cfg.redirectPort);
            return null;
        }
        authSocket = ss;
        authRunning = true;
        setStatus("Warte auf Login im Browser...");
        Thread t = new Thread(() -> runAuthServer(ss, st, verifier, redirect), "SpotifyHUD-Auth");
        t.setDaemon(true);
        t.start();
        return url;
    }

    /** Port is in use: find out which program holds it, switch to a free port automatically and explain. */
    private void handleBusyPort(int port) {
        setStatus("Port " + port + " ist belegt - prüfe...");
        Thread t = new Thread(() -> {
            String owner = PortInfo.owner(port);
            int free = PortInfo.findFree(port + 1, port + 200);
            String who = owner != null ? " von " + owner : " von einem anderen Programm";
            if (free > 0) {
                cfg.redirectPort = free;
                cfg.save();
                SpotifyHudMod.copyToClipboard(cfg.redirectUri());
                setStatus("Port " + port + " wird" + who + " benutzt. Neuer Port " + free
                        + ": Redirect-URI " + cfg.redirectUri() + " (kopiert) im Dashboard hinzufügen, dann erneut Anmelden");
            } else {
                setStatus("Port " + port + " wird" + who + " benutzt - bitte Programm schließen");
            }
        }, "SpotifyHUD-Port");
        t.setDaemon(true);
        t.start();
    }

    public void cancelLogin() {
        stopAuthServer();
        if (!isLoggedIn()) setStatus("Login abgebrochen");
    }

    private void stopAuthServer() {
        ServerSocket s = authSocket;
        authSocket = null;
        authRunning = false;
        if (s != null) try { s.close(); } catch (IOException ignored) { }
    }

    private void runAuthServer(ServerSocket ss, String expectedState, String verifier, String redirect) {
        long deadline = System.currentTimeMillis() + 5 * 60_000;
        try {
            while (authSocket == ss && System.currentTimeMillis() < deadline) {
                Socket s;
                try { s = ss.accept(); } catch (SocketTimeoutException te) { continue; } catch (IOException closed) { break; }
                try (Socket sock = s) {
                    sock.setSoTimeout(5000);
                    String line = readLine(sock.getInputStream());
                    // drain headers
                    String h;
                    do { h = readLine(sock.getInputStream()); } while (h != null && !h.isEmpty());
                    if (line == null) continue;
                    String[] parts = line.split(" ");
                    if (parts.length < 2) continue;
                    String target = parts[1];
                    String path = target.contains("?") ? target.substring(0, target.indexOf('?')) : target;
                    if (!path.equals("/callback")) {
                        respond(sock, 404, "Not found");
                        continue;
                    }
                    Map<String, String> q = parseQuery(target.contains("?") ? target.substring(target.indexOf('?') + 1) : "");
                    if (q.containsKey("error")) {
                        setStatus("Login abgelehnt: " + q.get("error"));
                        respond(sock, 200, page(false, "Login abgebrochen: " + q.get("error")));
                        break;
                    }
                    if (!expectedState.equals(q.get("state")) || !q.containsKey("code")) {
                        setStatus("Ungültige Antwort (state) - bitte erneut versuchen");
                        respond(sock, 400, page(false, "Ungültige Anfrage. Bitte erneut versuchen."));
                        break;
                    }
                    Map<String, String> form = new HashMap<>();
                    form.put("grant_type", "authorization_code");
                    form.put("code", q.get("code"));
                    form.put("redirect_uri", redirect);
                    form.put("client_id", cfg.clientId.trim());
                    form.put("code_verifier", verifier);
                    Resp r = tokenRequest(form);
                    if (r.code == 200) {
                        synchronized (tokenLock) { storeTokens(Json.parse(r.body)); }
                        setStatus("Verbunden");
                        respond(sock, 200, page(true, "Spotify HUD ist verbunden. Du kannst dieses Fenster schließen und zu Minecraft zurückkehren."));
                        schedulePoll(0);
                    } else {
                        setStatus(apiError(r, "Token-Austausch fehlgeschlagen"));
                        respond(sock, 200, page(false, "Token-Austausch fehlgeschlagen (" + r.code + "): " + r.body));
                    }
                    break;
                } catch (IOException ioe) {
                    SpotifyHudMod.LOG("Callback-Fehler: " + ioe);
                }
            }
            if (System.currentTimeMillis() >= deadline && !isLoggedIn()) setStatus("Login-Zeit abgelaufen");
        } finally {
            if (authSocket == ss) authSocket = null;
            authRunning = false;
            try { ss.close(); } catch (IOException ignored) { }
        }
    }

    public void logout() {
        stopAuthServer();
        cfg.accessToken = "";
        cfg.refreshToken = "";
        cfg.expiresAt = 0;
        cfg.save();
        state = PlayerState.EMPTY;
        artImage = null;
        artImageUrl = null;
        artVersion++;
        setStatus("Abgemeldet");
    }

    private static String readLine(InputStream in) throws IOException {
        StringBuilder b = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            if (c == '\n') break;
            if (c != '\r') b.append((char) c);
            if (b.length() > 8192) break;
        }
        if (c == -1 && b.length() == 0) return null;
        return b.toString();
    }

    private static Map<String, String> parseQuery(String q) {
        Map<String, String> m = new HashMap<>();
        for (String kv : q.split("&")) {
            if (kv.isEmpty()) continue;
            int i = kv.indexOf('=');
            String k = i < 0 ? kv : kv.substring(0, i);
            String v = i < 0 ? "" : kv.substring(i + 1);
            m.put(URLDecoder.decode(k, StandardCharsets.UTF_8), URLDecoder.decode(v, StandardCharsets.UTF_8));
        }
        return m;
    }

    private static void respond(Socket s, int code, String html) throws IOException {
        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        String head = "HTTP/1.1 " + code + (code == 200 ? " OK" : code == 404 ? " Not Found" : " Bad Request") + "\r\n"
                + "Content-Type: text/html; charset=utf-8\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Connection: close\r\n\r\n";
        OutputStream o = s.getOutputStream();
        o.write(head.getBytes(StandardCharsets.US_ASCII));
        o.write(body);
        o.flush();
    }

    private static String page(boolean ok, String msg) {
        String safe = msg.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<!doctype html><html><head><meta charset=utf-8><title>Spotify HUD</title></head>"
                + "<body style=\"background:#121212;color:#fff;font-family:Segoe UI,Arial,sans-serif;display:flex;"
                + "align-items:center;justify-content:center;height:100vh;margin:0\"><div style=\"text-align:center\">"
                + "<h1 style=\"color:" + (ok ? "#1ed760" : "#f15e6c") + "\">" + (ok ? "Verbunden" : "Fehler") + "</h1>"
                + "<p style=\"color:#b3b3b3\">" + safe + "</p></div></body></html>";
    }

    private String randomString(int len) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder b = new StringBuilder(len);
        for (int i = 0; i < len; i++) b.append(chars.charAt(rnd.nextInt(chars.length())));
        return b.toString();
    }
}
