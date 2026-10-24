package dev.spotifyhud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Automatic "now playing" detection on Windows via the System Media Transport Controls (same data as the
 * Windows media flyout). Runs a small hidden PowerShell helper (assets/spotifyhud/smtc.ps1) and talks to it
 * over stdin/stdout. No Spotify login needed.
 */
public final class SmtcBridge {
    private static final long LOCK_MS = 900;

    private volatile Process process;
    private volatile OutputStream stdin;
    private volatile boolean running;

    private volatile PlayerState state = PlayerState.EMPTY;
    private volatile boolean hasSession;
    private volatile boolean isSpotify;
    private volatile String app = "";
    private volatile boolean canShuffle, canRepeat, canSeek;
    private volatile boolean volumeSupported;
    private volatile boolean muted;
    private volatile String fatal;
    private volatile long latencyMs = 0;          // measured bridge delay (EMA)
    private volatile long lastMessage = 0;

    private volatile Object art;                   // BufferedImage (AWT only touched in ArtLoader)
    private volatile String artKey;
    private volatile int artVersion;

    private long lockPlaying, lockProgress, lockShuffle, lockRepeat, lockVolume;

    public boolean available() { return running && fatal == null; }
    public boolean active() { return available() && hasSession && state.hasTrack; }
    public boolean isSpotify() { return isSpotify; }
    public String app() { return app; }
    public PlayerState state() { return state; }
    public Object art() { return art; }
    public int artVersion() { return artVersion; }
    public boolean canShuffle() { return canShuffle; }
    public boolean canRepeat() { return canRepeat; }
    public boolean canSeek() { return canSeek; }
    public boolean volumeSupported() { return volumeSupported && state.volume >= 0; }
    public String fatal() { return fatal; }
    public long latencyMs() { return latencyMs; }

    public static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    public void start() {
        if (!isWindows()) { fatal = "Nur unter Windows verfügbar"; return; }
        Thread t = new Thread(this::supervise, "SpotifyHUD-Media");
        t.setDaemon(true);
        t.start();
    }

    private void supervise() {
        int failures = 0;
        while (true) {
            long started = System.currentTimeMillis();
            try {
                runOnce();
            } catch (Throwable t) {
                SpotifyHudMod.LOG("Media-Bridge Fehler: " + t);
            }
            running = false;
            hasSession = false;
            state = PlayerState.EMPTY;
            if (System.currentTimeMillis() - started < 15000) failures++; else failures = 0;
            long wait = Math.min(60000, 2000L * Math.max(1, failures));
            try { Thread.sleep(wait); } catch (InterruptedException e) { return; }
        }
    }

    private Path extractScript() throws IOException {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("spotifyhud");
        Files.createDirectories(dir);
        Path p = dir.resolve("smtc.ps1");
        byte[] data;
        try (InputStream in = SmtcBridge.class.getResourceAsStream("/assets/spotifyhud/smtc.ps1")) {
            if (in == null) throw new IOException("smtc.ps1 fehlt im Jar");
            data = in.readAllBytes();
        }
        // UTF-8 BOM so Windows PowerShell 5.1 reads the file as UTF-8
        byte[] withBom = new byte[data.length + 3];
        withBom[0] = (byte) 0xEF; withBom[1] = (byte) 0xBB; withBom[2] = (byte) 0xBF;
        System.arraycopy(data, 0, withBom, 3, data.length);
        if (!Files.exists(p) || !java.util.Arrays.equals(Files.readAllBytes(p), withBom)) Files.write(p, withBom);
        return p;
    }

    private void runOnce() throws Exception {
        Path script = extractScript();
        String sysRoot = System.getenv("SystemRoot");
        String ps = sysRoot != null ? sysRoot + "\\System32\\WindowsPowerShell\\v1.0\\powershell.exe" : "powershell.exe";
        if (!Files.exists(Path.of(ps))) ps = "powershell.exe";
        ProcessBuilder pb = new ProcessBuilder(ps, "-NoLogo", "-NoProfile", "-NonInteractive",
                "-ExecutionPolicy", "Bypass", "-WindowStyle", "Hidden", "-File", script.toString());
        pb.redirectErrorStream(false);
        Process p = pb.start();
        process = p;
        stdin = p.getOutputStream();
        running = true;
        fatal = null;

        // drain stderr so the process never blocks
        Thread err = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8))) {
                String l;
                while ((l = r.readLine()) != null) if (!l.isBlank()) SpotifyHudMod.LOG("Media-Bridge: " + l.trim());
            } catch (IOException ignored) { }
        }, "SpotifyHUD-Media-err");
        err.setDaemon(true);
        err.start();

        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.charAt(0) == '﻿') line = line.substring(1);
                try {
                    handle(Json.parse(line));
                } catch (Throwable t) {
                    SpotifyHudMod.LOG("Media-Bridge: ungültige Zeile: " + t);
                }
            }
        } finally {
            running = false;
            try { p.destroy(); } catch (Throwable ignored) { }
            process = null;
            stdin = null;
        }
    }

    private void handle(Object m) {
        String t = Json.str(m, "t", "");
        long now = System.currentTimeMillis();
        lastMessage = now;
        long sent = Json.num(m, "now", 0);
        if (sent > 0) {
            long d = Math.max(0, Math.min(2000, now - sent));
            latencyMs = latencyMs == 0 ? d : (latencyMs * 7 + d) / 8;
        }
        switch (t) {
            case "hello":
                volumeSupported = Json.bool(m, "vol", false);
                break;
            case "fatal":
                fatal = Json.str(m, "msg", "Fehler");
                SpotifyHudMod.LOG("Media-Bridge: " + fatal);
                break;
            case "log":
                SpotifyHudMod.LOG("Media-Bridge: " + Json.str(m, "msg", ""));
                break;
            case "ack":
                if (!Json.bool(m, "ok", false)) {
                    String cmd = Json.str(m, "cmd", "");
                    String err = Json.str(m, "err", "");
                    SpotifyHudMod.onBridgeCommandFailed(cmd, err);
                    // release optimistic locks so the real state shows again
                    if (cmd.startsWith("toggle") || cmd.equals("play") || cmd.equals("pause")) lockPlaying = 0;
                    if (cmd.startsWith("seek")) lockProgress = 0;
                    if (cmd.startsWith("shuffle")) lockShuffle = 0;
                    if (cmd.startsWith("repeat")) lockRepeat = 0;
                    if (cmd.startsWith("vol") || cmd.equals("mute")) lockVolume = 0;
                }
                break;
            case "thumb":
                handleThumb(m);
                break;
            case "state":
                handleState(m, now);
                break;
            default:
                break;
        }
    }

    private void handleThumb(Object m) {
        String key = Json.str(m, "key", "");
        String data = Json.str(m, "data", "");
        Object img = null;
        if (!data.isEmpty()) {
            try { img = ArtLoader.decode(Base64.getDecoder().decode(data)); } catch (Throwable t) { img = null; }
        }
        art = img;
        artKey = key;
        artVersion++;
    }

    private synchronized void handleState(Object m, long now) {
        if (!Json.bool(m, "has", false)) {
            hasSession = false;
            state = PlayerState.EMPTY;
            return;
        }
        hasSession = true;
        app = Json.str(m, "app", "");
        isSpotify = app.toLowerCase().contains("spotify");
        String title = Json.str(m, "title", "");
        String artist = Json.str(m, "artist", "");
        int status = (int) Json.num(m, "status", 0);
        boolean playing = status == 4;
        long pos = Json.num(m, "pos", 0);
        long dur = Math.max(0, Json.num(m, "dur", 0));
        long upd = Json.num(m, "upd", 0);
        // LastUpdatedTime is the moment 'pos' was valid (same machine clock) -> exact interpolation.
        long sampled = (upd > 946684800000L && upd <= now + 1000) ? upd : now - latencyMs;
        Object sh = m instanceof java.util.Map ? ((java.util.Map<?, ?>) m).get("shuffle") : null;
        Object rp = m instanceof java.util.Map ? ((java.util.Map<?, ?>) m).get("repeat") : null;
        boolean shuffle = sh instanceof Boolean && (Boolean) sh;
        String repeat = rp instanceof Number ? (((Number) rp).intValue() == 1 ? "track" : ((Number) rp).intValue() == 2 ? "context" : "off") : "off";
        int volRaw = (int) Json.num(m, "vol", -1);
        muted = volRaw >= 1000;
        int volume = volRaw < 0 ? -1 : (muted ? 0 : Math.min(100, volRaw));
        canShuffle = Json.bool(m, "cShuf", false);
        canRepeat = Json.bool(m, "cRep", false);
        canSeek = Json.bool(m, "cSeek", false);

        PlayerState old = state;
        boolean sameTrack = title.equals(old.title) && artist.equals(old.artist);
        if (now < lockPlaying) playing = old.playing;
        if (now < lockShuffle) shuffle = old.shuffle;
        if (now < lockRepeat) repeat = old.repeat;
        if (now < lockVolume && old.volume >= 0) volume = old.volume;
        if (now < lockProgress && sameTrack) { pos = old.currentProgress(); sampled = now; }
        if (!sameTrack) lockProgress = 0;

        boolean has = !title.isEmpty() || !artist.isEmpty();
        String uri = "smtc:" + title + "|" + artist;
        state = new PlayerState(has, null, uri, title, artist, null, dur, pos, sampled, playing, shuffle, repeat,
                volume, old.liked && sameTrack);
        if (!sameTrack && artKey != null && !artKey.equals(title + "|" + artist)) {
            // cover of the previous song must not stay visible
            art = null;
            artKey = null;
            artVersion++;
        }
    }

    // ------------------------------------------------------------------ commands
    private void send(String cmd) {
        OutputStream o = stdin;
        if (o == null) return;
        try {
            synchronized (this) {
                o.write((cmd + "\n").getBytes(StandardCharsets.UTF_8));
                o.flush();
            }
        } catch (IOException e) {
            SpotifyHudMod.LOG("Media-Bridge: senden fehlgeschlagen: " + e);
        }
    }

    public synchronized void togglePlay() {
        state = state.withPlaying(!state.playing);
        lockPlaying = System.currentTimeMillis() + LOCK_MS;
        send("toggle");
    }

    public void next() { send("next"); }
    public void previous() { send("prev"); }

    public synchronized void seek(long ms) {
        long d = state.durationMs;
        ms = Math.max(0, d > 0 ? Math.min(ms, d - 300) : ms);
        state = state.withProgress(ms);
        lockProgress = System.currentTimeMillis() + 1500;
        send("seek " + ms);
    }

    public synchronized void toggleShuffle() {
        boolean v = !state.shuffle;
        state = state.withShuffle(v);
        lockShuffle = System.currentTimeMillis() + LOCK_MS;
        send("shuffle " + v);
    }

    public synchronized void cycleRepeat() {
        String cur = state.repeat;
        String v = "off".equals(cur) ? "context" : "context".equals(cur) ? "track" : "off";
        state = state.withRepeat(v);
        lockRepeat = System.currentTimeMillis() + LOCK_MS;
        send("repeat " + ("track".equals(v) ? 1 : "context".equals(v) ? 2 : 0));
    }

    public synchronized void setVolume(int v) {
        v = Math.max(0, Math.min(100, v));
        state = state.withVolume(v);
        lockVolume = System.currentTimeMillis() + 1200;
        send("vol " + v);
    }

    public synchronized void toggleMute() {
        state = state.withVolume(muted ? Math.max(1, state.volume) : 0);
        lockVolume = System.currentTimeMillis() + 900;
        send("mute");
    }

    public void shutdown() {
        Process p = process;
        if (p != null) p.destroy();
    }
}
