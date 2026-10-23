package dev.lego.compat;

/** The Visor headset as seen from the LEGO code (a separate class per Minecraft version). */
public final class VisorBridge {
   private VisorBridge() {
   }

   /** Whether the Visor headset is on (or being put on/taken off). */
   public static boolean active() {
      try {
         return dev.visor.VisorController.get().state() != dev.visor.VisorController.State.OFF;
      } catch (Throwable e) {
         return false;
      }
   }

   public static void toggle() {
      try {
         dev.visor.VisorController.get().toggle();
      } catch (Throwable ignored) {
      }
   }
}
