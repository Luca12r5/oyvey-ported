package dev.lego.compat;

/** The Visor headset as seen from the LEGO code (shared by 1.21.11 and 26.3). */
public final class VisorBridge {
   private static boolean reported;
   private static boolean warned;

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

   /** Whether the Visor can be used in this game: not next to Vivecraft (its mixins are off there). */
   public static boolean available() {
      return !dev.lego.core.Compat.vivecraft;
   }

   /**
    * Whether the headset can actually be drawn on this graphics card: its pipelines compiled. Logs
    * the result once, so the launcher console shows why the Visor stays invisible on a PC.
    */
   public static boolean usable() {
      boolean ui;
      boolean hands;
      boolean fx;
      try {
         dev.visor.render.VisorGpu gpu = dev.visor.render.VisorGpu.get();
         ui = gpu.usable(dev.visor.render.VisorPipelines.UI) && gpu.usable(dev.visor.render.VisorPipelines.UI_DEPTH);
         hands = gpu.usable(dev.visor.render.VisorPipelines.HANDS);
         fx = gpu.usable(dev.visor.render.VisorPipelines.FX);
      } catch (Throwable e) {
         ui = hands = fx = false;
         dev.lego.LegoClient.LOG("Visor: Pipelines nicht prüfbar: " + e);
      }
      if (!reported) {
         reported = true;
         dev.lego.LegoClient.LOG("Visor: backend=" + McCompat.gpuInfo() + ", ui=" + (ui ? "ok" : "fehlt") + ", hands=" + (hands ? "ok" : "fehlt") + ", fx=" + (fx ? "ok" : "fehlt"));
      }
      return ui;
   }

   /** Shown once when the headset cannot be drawn; the classic menu opens instead. */
   public static void warnUnusable() {
      if (!warned) {
         warned = true;
         dev.lego.ui.Toasts.show("glasses", "Visor", "Geht mit dieser Grafik nicht – klassisches Menü");
      }
   }

   /**
    * The LEGO menu key: puts the headset on with the home panel, or takes it off. When the headset
    * cannot appear (graphics, an open screen, no player), the classic click GUI opens instead, so
    * the key never does nothing.
    */
   public static void openMenu() {
      try {
         dev.visor.VisorController v = dev.visor.VisorController.get();
         if (v.state() == dev.visor.VisorController.State.ON) {
            v.remove();
            return;
         }
         if (v.state() == dev.visor.VisorController.State.OFF) {
            if (usable()) {
               v.equip();
               if (v.state() != dev.visor.VisorController.State.OFF) {
                  v.showHome();
                  return;
               }
               dev.lego.LegoClient.LOG("Visor: Aufsetzen abgelehnt (Screen offen oder kein Spieler)");
            } else {
               warnUnusable();
            }
            dev.lego.ui.LegoScreen.show(new dev.lego.ui.MenuView());
         }
      } catch (Throwable e) {
         dev.lego.LegoClient.LOG("Visor: " + e);
         dev.lego.ui.LegoScreen.show(new dev.lego.ui.MenuView());
      }
   }

   public static void toggle() {
      try {
         dev.visor.VisorController.get().toggle();
      } catch (Throwable ignored) {
      }
   }
}
