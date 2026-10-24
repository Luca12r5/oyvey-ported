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
         Thread var1 = new Thread(this::supervise, "SpotifyHUD-Media");
         var1.setDaemon(true);
         var1.start();
      }
   }

   private void supervise() {
      int var1 = 0;

      while (true) {
         long var2 = System.currentTimeMillis();

         try {
            this.runOnce();
         } catch (Throwable var8) {
            SpotifyHudMod.LOG("Media-Bridge Fehler: " + var8);
         }

         this.running = false;
         this.hasSession = false;
         this.state = PlayerState.EMPTY;
         if (System.currentTimeMillis() - var2 < 15000L) {
            var1++;
         } else {
            var1 = 0;
         }

         long var4 = Math.min(60000L, 2000L * Math.max(1, var1));

         try {
            Thread.sleep(var4);
         } catch (InterruptedException var7) {
            return;
         }
      }
   }

   private Path extractScript() throws IOException {
      Path var1 = FabricLoader.getInstance().getConfigDir().resolve("spotifyhud");
      Files.createDirectories(var1);
      Path var2 = var1.resolve("smtc.ps1");

      byte[] var3;
      try (InputStream var4 = SmtcBridge.class.getResourceAsStream("/assets/spotifyhud/smtc.ps1")) {
         if (var4 == null) {
            throw new IOException("smtc.ps1 fehlt im Jar");
         }

         var3 = var4.readAllBytes();
      }

      byte[] var9 = new byte[var3.length + 3];
      var9[0] = -17;
      var9[1] = -69;
      var9[2] = -65;
      System.arraycopy(var3, 0, var9, 3, var3.length);
      if (!Files.exists(var2) || !Arrays.equals(Files.readAllBytes(var2), var9)) {
         Files.write(var2, var9);
      }

      return var2;
   }

   private void runOnce() throws Exception {
      Path var1 = this.extractScript();
      String var2 = System.getenv("SystemRoot");
      String var3 = var2 != null ? var2 + "\\System32\\WindowsPowerShell\\v1.0\\powershell.exe" : "powershell.exe";
      if (!Files.exists(Path.of(var3))) {
         var3 = "powershell.exe";
      }

      ProcessBuilder var4 = new ProcessBuilder(
         var3, "-NoLogo", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-WindowStyle", "Hidden", "-File", var1.toString()
      );
      var4.redirectErrorStream(false);
      Process var5 = var4.start();
      this.process = var5;
      this.stdin = var5.getOutputStream();
      this.running = true;
      this.fatal = null;
      Thread var6 = new Thread(() -> {
         String var2x;
         try (BufferedReader var1x = new BufferedReader(new InputStreamReader(var5.getErrorStream(), StandardCharsets.UTF_8))) {
            while ((var2x = var1x.readLine()) != null) {
               if (!var2x.isBlank()) {
                  SpotifyHudMod.LOG("Media-Bridge: " + var2x.trim());
               }
            }
         } catch (IOException var6x) {
         }
      }, "SpotifyHUD-Media-err");
      var6.setDaemon(true);
      var6.start();

      try {
         String var8;
         try (BufferedReader var7 = new BufferedReader(new InputStreamReader(var5.getInputStream(), StandardCharsets.UTF_8))) {
            while ((var8 = var7.readLine()) != null) {
               var8 = var8.trim();
               if (!var8.isEmpty()) {
                  if (var8.charAt(0) == '\ufeff') {
                     var8 = var8.substring(1);
                  }

                  try {
                     this.handle(Json.parse(var8));
                  } catch (Throwable var20) {
                     SpotifyHudMod.LOG("Media-Bridge: ungültige Zeile: " + var20);
                  }
               }
            }
         }
      } finally {
         this.running = false;

         try {
            var5.destroy();
         } catch (Throwable var18) {
         }

         this.process = null;
         this.stdin = null;
      }
   }

   private void handle(Object var1) {
      String var2 = Json.str(var1, "t", "");
      long var3 = System.currentTimeMillis();
      this.lastMessage = var3;
      long var5 = Json.num(var1, "now", 0L);
      if (var5 > 0L) {
         long var7 = Math.max(0L, Math.min(2000L, var3 - var5));
         this.latencyMs = this.latencyMs == 0L ? var7 : (this.latencyMs * 7L + var7) / 8L;
      }

      switch (var2) {
         case "hello":
            this.volumeSupported = Json.bool(var1, "vol", false);
            break;
         case "fatal":
            this.fatal = Json.str(var1, "msg", "Fehler");
            SpotifyHudMod.LOG("Media-Bridge: " + this.fatal);
            break;
         case "log":
            SpotifyHudMod.LOG("Media-Bridge: " + Json.str(var1, "msg", ""));
            break;
         case "ack":
            if (!Json.bool(var1, "ok", false)) {
               String var9 = Json.str(var1, "cmd", "");
               String var10 = Json.str(var1, "err", "");
               SpotifyHudMod.onBridgeCommandFailed(var9, var10);
               if (var9.startsWith("toggle") || var9.equals("play") || var9.equals("pause")) {
                  this.lockPlaying = 0L;
               }

               if (var9.startsWith("seek")) {
                  this.lockProgress = 0L;
               }

               if (var9.startsWith("shuffle")) {
                  this.lockShuffle = 0L;
               }

               if (var9.startsWith("repeat")) {
                  this.lockRepeat = 0L;
               }

               if (var9.startsWith("vol") || var9.equals("mute")) {
                  this.lockVolume = 0L;
               }
            }
            break;
         case "thumb":
            this.handleThumb(var1);
            break;
         case "state":
            this.handleState(var1, var3);
      }
   }

   private void handleThumb(Object var1) {
      String var2 = Json.str(var1, "key", "");
      String var3 = Json.str(var1, "data", "");
      Object var4 = null;
      if (!var3.isEmpty()) {
         try {
            var4 = ArtLoader.decode(Base64.getDecoder().decode(var3));
         } catch (Throwable var6) {
            var4 = null;
         }
      }

      this.art = var4;
      this.artKey = var2;
      this.artVersion++;
   }

   private synchronized void handleState(Object var1, long var2) {
      if (!Json.bool(var1, "has", false)) {
         this.hasSession = false;
         this.state = PlayerState.EMPTY;
      } else {
         this.hasSession = true;
         this.app = Json.str(var1, "app", "");
         this.isSpotify = this.app.toLowerCase().contains("spotify");
         String var4 = Json.str(var1, "title", "");
         String var5 = Json.str(var1, "artist", "");
         int var6 = (int)Json.num(var1, "status", 0L);
         boolean var7 = var6 == 4;
         long var8 = Json.num(var1, "pos", 0L);
         long var10 = Math.max(0L, Json.num(var1, "dur", 0L));
         long var12 = Json.num(var1, "upd", 0L);
         long var14 = var12 > 946684800000L && var12 <= var2 + 1000L ? var12 : var2 - this.latencyMs;
         Object var16 = var1 instanceof Map ? ((Map)var1).get("shuffle") : null;
         Object var17 = var1 instanceof Map ? ((Map)var1).get("repeat") : null;
         boolean var18 = var16 instanceof Boolean && (Boolean)var16;
         String var19 = var17 instanceof Number ? (((Number)var17).intValue() == 1 ? "track" : (((Number)var17).intValue() == 2 ? "context" : "off")) : "off";
         int var20 = (int)Json.num(var1, "vol", -1L);
         this.muted = var20 >= 1000;
         int var21 = var20 < 0 ? -1 : (this.muted ? 0 : Math.min(100, var20));
         this.canShuffle = Json.bool(var1, "cShuf", false);
         this.canRepeat = Json.bool(var1, "cRep", false);
         this.canSeek = Json.bool(var1, "cSeek", false);
         PlayerState var22 = this.state;
         boolean var23 = var4.equals(var22.title) && var5.equals(var22.artist);
         if (var2 < this.lockPlaying) {
            var7 = var22.playing;
         }

         if (var2 < this.lockShuffle) {
            var18 = var22.shuffle;
         }

         if (var2 < this.lockRepeat) {
            var19 = var22.repeat;
         }

         if (var2 < this.lockVolume && var22.volume >= 0) {
            var21 = var22.volume;
         }

         if (var2 < this.lockProgress && var23) {
            var8 = var22.currentProgress();
            var14 = var2;
         }

         if (!var23) {
            this.lockProgress = 0L;
         }

         boolean var24 = !var4.isEmpty() || !var5.isEmpty();
         String var25 = "smtc:" + var4 + "|" + var5;
         this.state = new PlayerState(var24, null, var25, var4, var5, null, var10, var8, var14, var7, var18, var19, var21, var22.liked && var23);
         if (!var23 && this.artKey != null && !this.artKey.equals(var4 + "|" + var5)) {
            this.art = null;
            this.artKey = null;
            this.artVersion++;
         }
      }
   }

   private void send(String var1) {
      OutputStream var2 = this.stdin;
      if (var2 != null) {
         try {
            synchronized (this) {
               var2.write((var1 + "\n").getBytes(StandardCharsets.UTF_8));
               var2.flush();
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

   public synchronized void seek(long var1) {
      long var3 = this.state.durationMs;
      var1 = Math.max(0L, var3 > 0L ? Math.min(var1, var3 - 300L) : var1);
      this.state = this.state.withProgress(var1);
      this.lockProgress = System.currentTimeMillis() + 1500L;
      this.send("seek " + var1);
   }

   public synchronized void toggleShuffle() {
      boolean var1 = !this.state.shuffle;
      this.state = this.state.withShuffle(var1);
      this.lockShuffle = System.currentTimeMillis() + 900L;
      this.send("shuffle " + var1);
   }

   public synchronized void cycleRepeat() {
      String var1 = this.state.repeat;
      String var2 = "off".equals(var1) ? "context" : ("context".equals(var1) ? "track" : "off");
      this.state = this.state.withRepeat(var2);
      this.lockRepeat = System.currentTimeMillis() + 900L;
      this.send("repeat " + ("track".equals(var2) ? 1 : ("context".equals(var2) ? 2 : 0)));
   }

   public synchronized void setVolume(int var1) {
      var1 = Math.max(0, Math.min(100, var1));
      this.state = this.state.withVolume(var1);
      this.lockVolume = System.currentTimeMillis() + 1200L;
      this.send("vol " + var1);
   }

   public synchronized void toggleMute() {
      this.state = this.state.withVolume(this.muted ? Math.max(1, this.state.volume) : 0);
      this.lockVolume = System.currentTimeMillis() + 900L;
      this.send("mute");
   }

   public void shutdown() {
      Process var1 = this.process;
      if (var1 != null) {
         var1.destroy();
      }
   }
}
