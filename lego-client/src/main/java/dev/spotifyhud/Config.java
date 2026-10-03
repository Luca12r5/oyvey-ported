package dev.spotifyhud;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
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
   public boolean hideWhenIdle = false;
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
      Config var0 = new Config();

      try {
         Path var1 = path();
         if (Files.exists(var1)) {
            Object var2 = Json.parse(Files.readString(var1, StandardCharsets.UTF_8));
            var0.clientId = Json.str(var2, "clientId", "").trim();
            var0.redirectPort = (int)Json.num(var2, "redirectPort", 8888L);
            var0.accessToken = Json.str(var2, "accessToken", "");
            var0.refreshToken = Json.str(var2, "refreshToken", "");
            var0.expiresAt = Json.num(var2, "expiresAt", 0L);
            var0.posX = clamp01(Json.dbl(var2, "posX", 0.5));
            var0.posY = clamp01(Json.dbl(var2, "posY", 0.012));
            var0.scale = Math.max(0.5, Math.min(3.0, Json.dbl(var2, "scale", 1.0)));
            var0.visible = Json.bool(var2, "visible", true);
            var0.hideWhenIdle = Json.bool(var2, "hideWhenIdle", false);
            var0.lyrics = Json.bool(var2, "lyrics", true);
            var0.lyricsOffsetMs = (int)Math.max(-5000L, Math.min(5000L, Json.num(var2, "lyricsOffsetMs", 0L)));
            var0.karaoke = Json.bool(var2, "karaoke", true);
            var0.wordGrow = Json.bool(var2, "wordGrow", true);
            var0.theme = Json.str(var2, "theme", "standard");
            var0.animation = Json.str(var2, "animation", "none");
            var0.animIntensity = Math.max(0.2, Math.min(1.0, Json.dbl(var2, "animIntensity", 0.7)));
            var0.animOnLyrics = Json.bool(var2, "animOnLyrics", true);
            if (var0.redirectPort < 1024 || var0.redirectPort > 65535) {
               var0.redirectPort = 8888;
            }
         }
      } catch (Throwable var3) {
         SpotifyHudMod.LOG("Konfiguration konnte nicht geladen werden: " + var3);
      }

      return var0;
   }

   public synchronized void save() {
      try {
         LinkedHashMap var1 = new LinkedHashMap();
         var1.put("clientId", this.clientId);
         var1.put("redirectPort", (long)this.redirectPort);
         var1.put("posX", this.posX);
         var1.put("posY", this.posY);
         var1.put("scale", this.scale);
         var1.put("visible", this.visible);
         var1.put("hideWhenIdle", this.hideWhenIdle);
         var1.put("lyrics", this.lyrics);
         var1.put("lyricsOffsetMs", (long)this.lyricsOffsetMs);
         var1.put("karaoke", this.karaoke);
         var1.put("wordGrow", this.wordGrow);
         var1.put("theme", this.theme);
         var1.put("animation", this.animation);
         var1.put("animIntensity", this.animIntensity);
         var1.put("animOnLyrics", this.animOnLyrics);
         var1.put("accessToken", this.accessToken);
         var1.put("refreshToken", this.refreshToken);
         var1.put("expiresAt", this.expiresAt);
         Path var2 = path();
         Files.createDirectories(var2.getParent());
         Path var3 = var2.resolveSibling("spotifyhud.json.tmp");
         Files.writeString(var3, Json.write(var1), StandardCharsets.UTF_8);
         Files.move(var3, var2, StandardCopyOption.REPLACE_EXISTING);
      } catch (Throwable var4) {
         SpotifyHudMod.LOG("Konfiguration konnte nicht gespeichert werden: " + var4);
      }
   }

   public static double clamp01(double var0) {
      return Double.isNaN(var0) ? 0.0 : Math.max(0.0, Math.min(1.0, var0));
   }
}
