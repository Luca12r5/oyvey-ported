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

   /** Whether the Visor can be used in this build (always on 1.21.11). */
   public static boolean available() {
      return true;
   }

   /** The LEGO menu key: puts the headset on with the home panel (the LEGO modules), or takes it off. */
   public static void openMenu() {
      try {
         dev.visor.VisorController v = dev.visor.VisorController.get();
         if (v.state() == dev.visor.VisorController.State.OFF) {
            v.equip();
            v.showHome();
         } else if (v.state() == dev.visor.VisorController.State.ON) {
            v.remove();
         }
      } catch (Throwable ignored) {
      }
   }

   public static void toggle() {
      try {
         dev.visor.VisorController.get().toggle();
      } catch (Throwable ignored) {
      }
   }
}
