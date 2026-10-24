package dev.spotifyhud;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

public final class Config {
   public static final double DEFAULT_X = 0.5;
   public static final double DEFAULT_Y = 0.012;
   public String clientId = "";
   public int redirectPort = 8888;
   public String accessToken = "";
   public String refreshToken = "";
   public long expiresAt = 0L;
   public double posX = 0.5;
   public double posY = 0.012;
   public double scale = 1.0;
   public boolean visible = true;
   /** Only show the HUD while something is playing (on by default since v2 of the config). */
   public boolean hideWhenIdle = true;
   public boolean lyrics = true;
   public int lyricsOffsetMs = 0;
   public boolean karaoke = true;
   public boolean wordGrow = true;
   public String theme = "standard";
   public String animation = "none";
   public double animIntensity = 0.7;
   public boolean animOnLyrics = true;

   private static Path path() {
      return FabricLoader.getInstance().getConfigDir().resolve("spotifyhud.json");
   }

   public String redirectUri() {
      return "http://127.0.0.1:" + this.redirectPort + "/callback";
   }

   public static Config load() {
      Config c = new Config();

      try {
         Path p = path();
         if (Files.exists(p)) {
            Object o = Json.parse(Files.readString(p, StandardCharsets.UTF_8));
            c.clientId = Json.str(o, "clientId", "").trim();
            c.redirectPort = (int)Json.num(o, "redirectPort", 8888L);
            c.accessToken = Json.str(o, "accessToken", "");
            c.refreshToken = Json.str(o, "refreshToken", "");
            c.expiresAt = Json.num(o, "expiresAt", 0L);
            c.posX = clamp01(Json.dbl(o, "posX", 0.5));
            c.posY = clamp01(Json.dbl(o, "posY", 0.012));
            c.scale = Math.max(0.5, Math.min(3.0, Json.dbl(o, "scale", 1.0)));
            c.visible = Json.bool(o, "visible", true);
            // Config v2: "no song" no longer covers the screen; older files switch to hiding once.
            c.hideWhenIdle = Json.num(o, "configVersion", 1L) < 2L || Json.bool(o, "hideWhenIdle", true);
            c.lyrics = Json.bool(o, "lyrics", true);
            c.lyricsOffsetMs = (int)Math.max(-5000L, Math.min(5000L, Json.num(o, "lyricsOffsetMs", 0L)));
            c.karaoke = Json.bool(o, "karaoke", true);
            c.wordGrow = Json.bool(o, "wordGrow", true);
            c.theme = Json.str(o, "theme", "standard");
            c.animation = Json.str(o, "animation", "none");
            c.animIntensity = Math.max(0.2, Math.min(1.0, Json.dbl(o, "animIntensity", 0.7)));
            c.animOnLyrics = Json.bool(o, "animOnLyrics", true);
            if (c.redirectPort < 1024 || c.redirectPort > 65535) {
               c.redirectPort = 8888;
            }
         }
      } catch (Throwable var3) {
         SpotifyHudMod.LOG("Konfiguration konnte nicht geladen werden: " + var3);
      }

      return c;
   }

   public synchronized void save() {
      try {
         Map<String, Object> m = new LinkedHashMap<>();
         m.put("clientId", this.clientId);
         m.put("redirectPort", (long)this.redirectPort);
         m.put("posX", this.posX);
         m.put("posY", this.posY);
         m.put("scale", this.scale);
         m.put("visible", this.visible);
         m.put("hideWhenIdle", this.hideWhenIdle);
         m.put("configVersion", 2L);
         m.put("lyrics", this.lyrics);
         m.put("lyricsOffsetMs", (long)this.lyricsOffsetMs);
         m.put("karaoke", this.karaoke);
         m.put("wordGrow", this.wordGrow);
         m.put("theme", this.theme);
         m.put("animation", this.animation);
         m.put("animIntensity", this.animIntensity);
         m.put("animOnLyrics", this.animOnLyrics);
         m.put("accessToken", this.accessToken);
         m.put("refreshToken", this.refreshToken);
         m.put("expiresAt", this.expiresAt);
         Path p = path();
         Files.createDirectories(p.getParent());
         Path tmp = p.resolveSibling("spotifyhud.json.tmp");
         Files.writeString(tmp, Json.write(m), StandardCharsets.UTF_8);
         Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING);
      } catch (Throwable var4) {
         SpotifyHudMod.LOG("Konfiguration konnte nicht gespeichert werden: " + var4);
      }
   }

   public static double clamp01(double v) {
      return Double.isNaN(v) ? 0.0 : Math.max(0.0, Math.min(1.0, v));
   }
}
