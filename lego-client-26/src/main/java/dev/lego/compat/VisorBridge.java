package dev.lego.compat;

/** Minecraft 26.3: the Visor headset is not part of this build yet. */
public final class VisorBridge {
   private VisorBridge() {
   }

   public static boolean active() {
      return false;
   }

   public static void toggle() {
   }
}
