package dev.lego.core;

public enum Category {
    HUD("HUD", "Saubere Anzeigen auf dem Bildschirm", "layout", true),
    VISUALS("Visuals", "Himmel, Effekte, Filter, Partikel", "sparkle", true),
    UTILITY("Utility", "Legit Helfer: Sprint, InvSee, Wegpunkte ...", "wand", true),
    PERFORMANCE("Leistung", "Mehr FPS, weniger Ruckler", "boost", true),
    SPOTIFY("Spotify", "Spotify HUD + Karaoke-Lyrics", "music", true),
    COSMETICS("Kosmetik", "Umhänge, Flügel, Hüte ...", "shirt", false),
    EMOTES("Emotes", "Animationen für deinen Spieler", "emote", false),
    GAMES("Minispiele", "Spiele für Zwischendurch", "gamepad", false),
    /** Single world particle effects - controlled together by the "Welt-Effekte" module. */
    WORLD_FX("Welt-Effekte", "Partikel in der Welt", "sparkle", false);

    public final String title, subtitle, icon;
    /** Shown in the mod menu list. */
    public final boolean listed;
    Category(String title, String subtitle, String icon, boolean listed) { this.title = title; this.subtitle = subtitle; this.icon = icon; this.listed = listed; }
    public String displayTitle() { String t = Lang.get("cat." + name().toLowerCase()); return t != null ? t : title; }
    public String displaySubtitle() { String t = Lang.get("cat." + name().toLowerCase() + ".sub"); return t != null ? t : subtitle; }
}
