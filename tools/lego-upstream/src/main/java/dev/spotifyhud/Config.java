package dev.spotifyhud;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

import net.fabricmc.loader.api.FabricLoader;

/** Persistent settings: config/spotifyhud.json */
public final class Config {
    public static final double DEFAULT_X = 0.5;
    public static final double DEFAULT_Y = 0.012;

    public String clientId = "";
    public int redirectPort = 8888;
    public String accessToken = "";
    public String refreshToken = "";
    public long expiresAt = 0L;

    /** HUD position as fraction of the free screen space (0..1). */
    public double posX = DEFAULT_X;
    public double posY = DEFAULT_Y;
    public double scale = 1.0;
    public boolean visible = true;
    public boolean hideWhenIdle = false;
    public boolean lyrics = true;
    /** Manual lyrics sync correction in ms (positive = lyrics earlier). */
    public int lyricsOffsetMs = 0;
    /** Karaoke: highlight the word that is being sung. */
    public boolean karaoke = true;
    /** The active word grows a little. */
    public boolean wordGrow = true;
    public String theme = "standard";
    public String animation = "none";
    public double animIntensity = 0.7;
    public boolean animOnLyrics = true;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("spotifyhud.json");
    }

    public String redirectUri() {
        return "http://127.0.0.1:" + redirectPort + "/callback";
    }

    public static Config load() {
        Config c = new Config();
        try {
            Path p = path();
            if (Files.exists(p)) {
                Object o = Json.parse(Files.readString(p, StandardCharsets.UTF_8));
                c.clientId = Json.str(o, "clientId", "").trim();
                c.redirectPort = (int) Json.num(o, "redirectPort", 8888);
                c.accessToken = Json.str(o, "accessToken", "");
                c.refreshToken = Json.str(o, "refreshToken", "");
                c.expiresAt = Json.num(o, "expiresAt", 0L);
                c.posX = clamp01(Json.dbl(o, "posX", DEFAULT_X));
                c.posY = clamp01(Json.dbl(o, "posY", DEFAULT_Y));
                c.scale = Math.max(0.5, Math.min(3.0, Json.dbl(o, "scale", 1.0)));
                c.visible = Json.bool(o, "visible", true);
                c.hideWhenIdle = Json.bool(o, "hideWhenIdle", false);
                c.lyrics = Json.bool(o, "lyrics", true);
                c.lyricsOffsetMs = (int) Math.max(-5000, Math.min(5000, Json.num(o, "lyricsOffsetMs", 0)));
                c.karaoke = Json.bool(o, "karaoke", true);
                c.wordGrow = Json.bool(o, "wordGrow", true);
                c.theme = Json.str(o, "theme", "standard");
                c.animation = Json.str(o, "animation", "none");
                c.animIntensity = Math.max(0.2, Math.min(1.0, Json.dbl(o, "animIntensity", 0.7)));
                c.animOnLyrics = Json.bool(o, "animOnLyrics", true);
                if (c.redirectPort < 1024 || c.redirectPort > 65535) c.redirectPort = 8888;
            }
        } catch (Throwable t) {
            SpotifyHudMod.LOG("Konfiguration konnte nicht geladen werden: " + t);
        }
        return c;
    }

    public synchronized void save() {
        try {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("clientId", clientId);
            m.put("redirectPort", (long) redirectPort);
            m.put("posX", posX);
            m.put("posY", posY);
            m.put("scale", scale);
            m.put("visible", visible);
            m.put("hideWhenIdle", hideWhenIdle);
            m.put("lyrics", lyrics);
            m.put("lyricsOffsetMs", (long) lyricsOffsetMs);
            m.put("karaoke", karaoke);
            m.put("wordGrow", wordGrow);
            m.put("theme", theme);
            m.put("animation", animation);
            m.put("animIntensity", animIntensity);
            m.put("animOnLyrics", animOnLyrics);
            m.put("accessToken", accessToken);
            m.put("refreshToken", refreshToken);
            m.put("expiresAt", expiresAt);
            Path p = path();
            Files.createDirectories(p.getParent());
            Path tmp = p.resolveSibling("spotifyhud.json.tmp");
            Files.writeString(tmp, Json.write(m), StandardCharsets.UTF_8);
            Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING);
        } catch (Throwable t) {
            SpotifyHudMod.LOG("Konfiguration konnte nicht gespeichert werden: " + t);
        }
    }

    public static double clamp01(double v) {
        if (Double.isNaN(v)) return 0;
        return Math.max(0, Math.min(1, v));
    }
}
