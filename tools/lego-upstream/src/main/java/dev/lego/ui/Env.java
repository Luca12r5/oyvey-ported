package dev.lego.ui;

import java.util.function.Supplier;

/** Game data for pure views (set by the client in game, defaults for the offline preview). */
public final class Env {
    private Env() { }

    public static Supplier<String> nowPlaying = () -> "";
    public static Supplier<String> clipboard = () -> "";
    /** Copies text to the clipboard (in game). */
    public static java.util.function.Consumer<String> copy = s -> { };
    public static Supplier<String> playerName = () -> "Spieler";
    public static Supplier<Boolean> inWorld = () -> true;
    public static String version = "3.4.0";
    public static String mcVersion = "1.21.11";
    /** Opens the HUD editor (in game). */
    public static Runnable openHudEditor = () -> { };
    /** Opens the TV (browser). */
    /** Plays an emote. */
    public static java.util.function.IntConsumer playEmote = i -> { };
    /** Draws the 3D player preview into a pixel rectangle (in game only). */
    public static PlayerPreview playerPreview = (x, y, w, h, yaw, pitch) -> false;

    public interface PlayerPreview {
        /** @return true if drawn */
        boolean draw(int x, int y, int w, int h, float yaw, float pitch);
    }
    /** True while Minecraft's own loading overlay is on screen. */
    public static java.util.function.BooleanSupplier loading = () -> false;
}
