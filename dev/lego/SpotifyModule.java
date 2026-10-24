package dev.lego;

import dev.lego.core.Category;
import dev.lego.core.Module;
import dev.lego.core.Setting;
import dev.lego.ui.LegoScreen;
import dev.lego.ui.SpotifyView;
import dev.spotifyhud.Config;
import dev.spotifyhud.SpotifyHudMod;

public final class SpotifyModule extends Module {
   public SpotifyModule() {
      super("spotify", "Spotify HUD", "Song, Cover, Steuerung, Lyrics, Themes & Animationen", Category.SPOTIFY);
      this.add(new Setting.Action("Einstellungen", "Spotify-Einstellungen öffnen", SpotifyHudMod::requestSettings));
      this.add(new Setting.Action("Menü", "Suche & Playlists im Spiel öffnen", () -> LegoScreen.show(new SpotifyView())));
      this.add(new Setting.Action("Lyrics", "Lyrics an / aus", SpotifyHudMod::toggleLyrics));
   }

   @Override
   public void setEnabled(boolean v) {
      Config c = SpotifyHudMod.config();
      if (c != null) {
         this.enabled = v;
         c.visible = v;
         c.save();
      }
   }

   @Override
   public void tick() {
   }

   public void sync() {
      Config c = SpotifyHudMod.config();
      if (c != null) {
         this.enabled = c.visible;
      }
   }

   public boolean isOn() {
      Config c = SpotifyHudMod.config();
      return c != null && c.visible;
   }
}
