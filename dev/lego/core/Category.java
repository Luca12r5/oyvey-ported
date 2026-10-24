package dev.lego.core;

public enum Category {
   HUD("HUD", "Saubere Anzeigen auf dem Bildschirm", "layout", true),
   VISUALS("Visuals", "Himmel, Effekte, Filter, Partikel", "sparkle", true),
   UTILITY("Utility", "Legit Helfer: Sprint, InvSee, Wegpunkte ...", "wand", true),
   PERFORMANCE("Leistung", "Mehr FPS, weniger Ruckler", "boost", true),
   SPOTIFY("Spotify", "Spotify HUD + Karaoke-Lyrics", "music", true),
   COSMETICS("Kosmetik", "Umhänge, Flügel, Hüte ...", "shirt", false),
   EMOTES("Emotes", "Animationen für deinen Spieler", "emote", false),
   GAMES("Minispiele", "Spiele für Zwischendurch", "gamepad", false);

   public final String title;
   public final String subtitle;
   public final String icon;
   public final boolean listed;

   private Category(String nullxx, String nullxxx, String nullxxxx, boolean nullxxxxx) {
      this.title = nullxx;
      this.subtitle = nullxxx;
      this.icon = nullxxxx;
      this.listed = nullxxxxx;
   }
}
