package dev.lego.ui;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public final class Env {
   public static Supplier<String> nowPlaying = () -> "";
   public static Supplier<String> clipboard = () -> "";
   public static Consumer<String> copy = s -> {};
   public static Supplier<String> playerName = () -> "Spieler";
   public static Supplier<Boolean> inWorld = () -> true;
   public static String version = "3.4.0";
   public static String mcVersion = "1.21.11";
   public static Runnable openHudEditor = () -> {};
   public static IntConsumer playEmote = i -> {};
   public static Env.PlayerPreview playerPreview = (x, y, w, h, yaw, pitch) -> false;
   public static BooleanSupplier loading = () -> false;

   private Env() {
   }

   public interface PlayerPreview {
      boolean draw(int var1, int var2, int var3, int var4, float var5, float var6);
   }
}
