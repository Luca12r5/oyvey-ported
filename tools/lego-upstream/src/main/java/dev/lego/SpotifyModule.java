package dev.lego;

import dev.lego.core.Category;
import dev.lego.core.Module;
import dev.lego.core.Setting;
import dev.spotifyhud.Config;
import dev.spotifyhud.SpotifyHudMod;

/** Spotify HUD inside the Lego client GUI (the HUD itself lives in dev.spotifyhud). */
public final class SpotifyModule extends Module {
    public SpotifyModule() {
        super("spotify", "Spotify HUD", "Song, Cover, Steuerung, Lyrics, Themes & Animationen", Category.SPOTIFY);
        add(new Setting.Action("Einstellungen", "Spotify-Einstellungen öffnen", SpotifyHudMod::requestSettings));
        add(new Setting.Action("Menü", "Suche & Playlists im Spiel öffnen", () -> dev.lego.ui.LegoScreen.show(new dev.lego.ui.SpotifyView())));
        add(new Setting.Action("Lyrics", "Lyrics an / aus", SpotifyHudMod::toggleLyrics));
    }

    @Override public void setEnabled(boolean v) {
        Config c = SpotifyHudMod.config();
        if (c == null) return;
        enabled = v;
        c.visible = v;
        c.save();
    }

    /** Keeps the toggle in sync with the Spotify settings (they can also be changed in the Spotify menu). */
    @Override public void tick() { }

    public void sync() {
        Config c = SpotifyHudMod.config();
        if (c != null) enabled = c.visible;
    }

    public boolean isOn() {
        Config c = SpotifyHudMod.config();
        return c != null && c.visible;
    }
}
