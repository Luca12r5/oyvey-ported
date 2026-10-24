package dev.spotifyhud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

public final class SmtcBridge {
   private static final long LOCK_MS = 900L;
   private volatile Process process;
   private volatile OutputStream stdin;
   private volatile boolean running;
   private volatile PlayerState state = PlayerState.EMPTY;
   private volatile boolean hasSession;
   private volatile boolean isSpotify;
   private volatile String app = "";
   private volatile boolean canShuffle;
   private volatile boolean canRepeat;
   private volatile boolean canSeek;
   private volatile boolean volumeSupported;
   private volatile boolean muted;
   private volatile String fatal;
   private volatile long latencyMs = 0L;
   private volatile long lastMessage = 0L;
   private volatile Object art;
   private volatile String artKey;
   private volatile int artVersion;
   private long lockPlaying;
   private long lockProgress;
   private long lockShuffle;
   private long lockRepeat;
   private long lockVolume;

   public boolean available() {
      return this.running && this.fatal == null;
   }

   public boolean active() {
      return this.available() && this.hasSession && this.state.hasTrack;
   }

   public boolean isSpotify() {
      return this.isSpotify;
   }

   public String app() {
      return this.app;
   }

   public PlayerState state() {
      return this.state;
   }

   public Object art() {
      return this.art;
   }

   public int artVersion() {
      return this.artVersion;
   }

   public boolean canShuffle() {
      return this.canShuffle;
   }

   public boolean canRepeat() {
      return this.canRepeat;
   }

   public boolean canSeek() {
      return this.canSeek;
   }

   public boolean volumeSupported() {
      return this.volumeSupported && this.state.volume >= 0;
   }

   public String fatal() {
      return this.fatal;
   }

   public long latencyMs() {
      return this.latencyMs;
   }

   public static boolean isWindows() {
      return System.getProperty("os.name", "").toLowerCase().contains("win");
   }

   public void start() {
      if (!isWindows()) {
         this.fatal = "Nur unter Windows verfügbar";
      } else {
         Thread t = new Thread(this::supervise, "SpotifyHUD-Media");
         t.setDaemon(true);
         t.start();
      }
   }

   private void supervise() {
      int failures = 0;

      while (true) {
         long started = System.currentTimeMillis();

         try {
            this.runOnce();
         } catch (Throwable var8) {
            SpotifyHudMod.LOG("Media-Bridge Fehler: " + var8);
         }

         this.running = false;
         this.hasSession = false;
         this.state = PlayerState.EMPTY;
         if (System.currentTimeMillis() - started < 15000L) {
            failures++;
         } else {
            failures = 0;
         }

         long wait = Math.min(60000L, 2000L * Math.max(1, failures));

         try {
            Thread.sleep(wait);
         } catch (InterruptedException var7) {
            return;
         }
      }
   }

   private Path extractScript() throws IOException {
      Path dir = FabricLoader.getInstance().getConfigDir().resolve("spotifyhud");
      Files.createDirectories(dir);
      Path p = dir.resolve("smtc.ps1");

      byte[] data;
      try (InputStream in = SmtcBridge.class.getResourceAsStream("/assets/spotifyhud/smtc.ps1")) {
         if (in == null) {
            throw new IOException("smtc.ps1 fehlt im Jar");
         }

         data = in.readAllBytes();
      }

      byte[] withBom = new byte[data.length + 3];
      withBom[0] = -17;
      withBom[1] = -69;
      withBom[2] = -65;
      System.arraycopy(data, 0, withBom, 3, data.length);
      if (!Files.exists(p) || !Arrays.equals(Files.readAllBytes(p), withBom)) {
         Files.write(p, withBom);
      }

      return p;
   }

   private void runOnce() throws Exception {
      Path script = this.extractScript();
      String sysRoot = System.getenv("SystemRoot");
      String ps = sysRoot != null ? sysRoot + "\\System32\\WindowsPowerShell\\v1.0\\powershell.exe" : "powershell.exe";
      if (!Files.exists(Path.of(ps))) {
         ps = "powershell.exe";
      }

      ProcessBuilder pb = new ProcessBuilder(
         ps, "-NoLogo", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-WindowStyle", "Hidden", "-File", script.toString()
      );
      pb.redirectErrorStream(false);
      Process p = pb.start();
      this.process = p;
      this.stdin = p.getOutputStream();
      this.running = true;
      this.fatal = null;
      Thread err = new Thread(() -> {
         String l;
         try (BufferedReader rx = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8))) {
            while ((l = rx.readLine()) != null) {
               if (!l.isBlank()) {
                  SpotifyHudMod.LOG("Media-Bridge: " + l.trim());
               }
            }
         } catch (IOException var6x) {
         }
      }, "SpotifyHUD-Media-err");
      err.setDaemon(true);
      err.start();

      try {
         String line;
         try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            while ((line = r.readLine()) != null) {
               line = line.trim();
               if (!line.isEmpty()) {
                  if (line.charAt(0) == '\ufeff') {
                     line = line.substring(1);
                  }

                  try {
                     this.handle(Json.parse(line));
                  } catch (Throwable var20) {
                     SpotifyHudMod.LOG("Media-Bridge: ungültige Zeile: " + var20);
                  }
               }
            }
         }
      } finally {
         this.running = false;

         try {
            p.destroy();
         } catch (Throwable var18) {
         }

         this.process = null;
         this.stdin = null;
      }
   }

   private void handle(Object m) {
      String t = Json.str(m, "t", "");
      long now = System.currentTimeMillis();
      this.lastMessage = now;
      long sent = Json.num(m, "now", 0L);
      if (sent > 0L) {
         long d = Math.max(0L, Math.min(2000L, now - sent));
         this.latencyMs = this.latencyMs == 0L ? d : (this.latencyMs * 7L + d) / 8L;
      }

      switch (t) {
         case "hello":
            this.volumeSupported = Json.bool(m, "vol", false);
            break;
         case "fatal":
            this.fatal = Json.str(m, "msg", "Fehler");
            SpotifyHudMod.LOG("Media-Bridge: " + this.fatal);
            break;
         case "log":
            SpotifyHudMod.LOG("Media-Bridge: " + Json.str(m, "msg", ""));
            break;
         case "ack":
            if (!Json.bool(m, "ok", false)) {
               String cmd = Json.str(m, "cmd", "");
               String err = Json.str(m, "err", "");
               SpotifyHudMod.onBridgeCommandFailed(cmd, err);
               if (cmd.startsWith("toggle") || cmd.equals("play") || cmd.equals("pause")) {
                  this.lockPlaying = 0L;
               }

               if (cmd.startsWith("seek")) {
                  this.lockProgress = 0L;
               }

               if (cmd.startsWith("shuffle")) {
                  this.lockShuffle = 0L;
               }

               if (cmd.startsWith("repeat")) {
                  this.lockRepeat = 0L;
               }

               if (cmd.startsWith("vol") || cmd.equals("mute")) {
                  this.lockVolume = 0L;
               }
            }
            break;
         case "thumb":
            this.handleThumb(m);
            break;
         case "state":
            this.handleState(m, now);
      }
   }

   private void handleThumb(Object m) {
      String key = Json.str(m, "key", "");
      String data = Json.str(m, "data", "");
      Object img = null;
      if (!data.isEmpty()) {
         try {
            img = ArtLoader.decode(Base64.getDecoder().decode(data));
         } catch (Throwable var6) {
            img = null;
         }
      }

      this.art = img;
      this.artKey = key;
      this.artVersion++;
   }

   private synchronized void handleState(Object m, long now) {
      if (!Json.bool(m, "has", false)) {
         this.hasSession = false;
         this.state = PlayerState.EMPTY;
      } else {
         this.hasSession = true;
         this.app = Json.str(m, "app", "");
         this.isSpotify = this.app.toLowerCase().contains("spotify");
         String title = Json.str(m, "title", "");
         String artist = Json.str(m, "artist", "");
         int status = (int)Json.num(m, "status", 0L);
         boolean playing = status == 4;
         long pos = Json.num(m, "pos", 0L);
         long dur = Math.max(0L, Json.num(m, "dur", 0L));
         long upd = Json.num(m, "upd", 0L);
         long sampled = upd > 946684800000L && upd <= now + 1000L ? upd : now - this.latencyMs;
         Object sh = m instanceof Map ? ((Map)m).get("shuffle") : null;
         Object rp = m instanceof Map ? ((Map)m).get("repeat") : null;
         boolean shuffle = sh instanceof Boolean && (Boolean)sh;
         String repeat = rp instanceof Number ? (((Number)rp).intValue() == 1 ? "track" : (((Number)rp).intValue() == 2 ? "context" : "off")) : "off";
         int volRaw = (int)Json.num(m, "vol", -1L);
         this.muted = volRaw >= 1000;
         int volume = volRaw < 0 ? -1 : (this.muted ? 0 : Math.min(100, volRaw));
         this.canShuffle = Json.bool(m, "cShuf", false);
         this.canRepeat = Json.bool(m, "cRep", false);
         this.canSeek = Json.bool(m, "cSeek", false);
         PlayerState old = this.state;
         boolean sameTrack = title.equals(old.title) && artist.equals(old.artist);
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

         if (now < this.lockProgress && sameTrack) {
            pos = old.currentProgress();
            sampled = now;
         }

         if (!sameTrack) {
            this.lockProgress = 0L;
         }

         boolean has = !title.isEmpty() || !artist.isEmpty();
         String uri = "smtc:" + title + "|" + artist;
         this.state = new PlayerState(has, null, uri, title, artist, null, dur, pos, sampled, playing, shuffle, repeat, volume, old.liked && sameTrack);
         if (!sameTrack && this.artKey != null && !this.artKey.equals(title + "|" + artist)) {
            this.art = null;
            this.artKey = null;
            this.artVersion++;
         }
      }
   }

   private void send(String cmd) {
      OutputStream o = this.stdin;
      if (o != null) {
         try {
            synchronized (this) {
               o.write((cmd + "\n").getBytes(StandardCharsets.UTF_8));
               o.flush();
            }
         } catch (IOException var6) {
            SpotifyHudMod.LOG("Media-Bridge: senden fehlgeschlagen: " + var6);
         }
      }
   }

   public synchronized void togglePlay() {
      this.state = this.state.withPlaying(!this.state.playing);
      this.lockPlaying = System.currentTimeMillis() + 900L;
      this.send("toggle");
   }

   public void next() {
      this.send("next");
   }

   public void previous() {
      this.send("prev");
   }

   public synchronized void seek(long ms) {
      long d = this.state.durationMs;
      ms = Math.max(0L, d > 0L ? Math.min(ms, d - 300L) : ms);
      this.state = this.state.withProgress(ms);
      this.lockProgress = System.currentTimeMillis() + 1500L;
      this.send("seek " + ms);
   }

   public synchronized void toggleShuffle() {
      boolean v = !this.state.shuffle;
      this.state = this.state.withShuffle(v);
      this.lockShuffle = System.currentTimeMillis() + 900L;
      this.send("shuffle " + v);
   }

   public synchronized void cycleRepeat() {
      String cur = this.state.repeat;
      String v = "off".equals(cur) ? "context" : ("context".equals(cur) ? "track" : "off");
      this.state = this.state.withRepeat(v);
      this.lockRepeat = System.currentTimeMillis() + 900L;
      this.send("repeat " + ("track".equals(v) ? 1 : ("context".equals(v) ? 2 : 0)));
   }

   public synchronized void setVolume(int v) {
      v = Math.max(0, Math.min(100, v));
      this.state = this.state.withVolume(v);
      this.lockVolume = System.currentTimeMillis() + 1200L;
      this.send("vol " + v);
   }

   public synchronized void toggleMute() {
      this.state = this.state.withVolume(this.muted ? Math.max(1, this.state.volume) : 0);
      this.lockVolume = System.currentTimeMillis() + 900L;
      this.send("mute");
   }

   public void shutdown() {
      Process p = this.process;
      if (p != null) {
         p.destroy();
      }
   }
}
