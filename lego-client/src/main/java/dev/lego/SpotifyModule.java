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
   public void setEnabled(boolean var1) {
      Config var2 = SpotifyHudMod.config();
      if (var2 != null) {
         this.enabled = var1;
         var2.visible = var1;
         var2.save();
      }
   }

   @Override
   public void tick() {
   }

   public void sync() {
      Config var1 = SpotifyHudMod.config();
      if (var1 != null) {
         this.enabled = var1.visible;
      }
   }

   public boolean isOn() {
      Config var1 = SpotifyHudMod.config();
      return var1 != null && var1.visible;
   }
}
