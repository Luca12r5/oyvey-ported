package dev.lego.discord;

import com.sun.jna.Pointer;
import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Setting;
import dev.lego.ui.Toasts;
import java.lang.reflect.Method;
import net.minecraft.network.chat.Component;

public final class DiscordControl extends Module {
   private static final int VK_CTRL = 17;
   private static final int VK_SHIFT = 16;
   private static final int VK_ALT = 18;
   public static boolean muted;
   public static boolean deafened;
   public static DiscordControl instance;
   private static final String[] LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");
   final Setting.Num posX = this.add(new Setting.Num("Position X", 0.0, 95.0, 1.0, 2.0, "%"));
   final Setting.Num posY = this.add(new Setting.Num("Position Y", 0.0, 95.0, 1.0, 38.0, "%"));
   final Setting.Num scale = this.add(new Setting.Num("Größe", 0.7, 2.0, 0.1, 1.0, "x"));
   final Setting.Mode mods = this.add(new Setting.Mode("Kürzel", 0, "Strg+Umschalt", "Strg+Alt", "Alt+Umschalt"));
   final Setting.Mode muteKey = this.add(new Setting.Mode("Taste Stumm", 12, LETTERS));
   final Setting.Mode deafKey = this.add(new Setting.Mode("Taste Taub", 3, LETTERS));
   final Setting.Bool compact = this.add(new Setting.Bool("Kompakt", false));
   private boolean guided;

   public DiscordControl() {
      super(
         "discord",
         "Discord-Steuerung",
         "Mikro & Ton von Discord im Spiel schalten (Knöpfe im Chat anklicken oder Tasten belegen) - einmal in Discord das Kürzel eintragen",
         Category.UTILITY
      );
      this.icon("mute");
      this.fresh();
      instance = this;
      this.add(new Setting.Action("Stumm", "Mikrofon stumm / an", DiscordControl::toggleMute));
      this.add(new Setting.Action("Taub", "Ton komplett aus / an", DiscordControl::toggleDeafen));
      this.add(new Setting.Action("Anleitung", "So richtest du Discord ein", DiscordControl::guide));
   }

   @Override
   public void onEnable() {
      if (!this.guided) {
         this.guided = true;
         guide();
      }
   }

   public static String combo(boolean mute) {
      if (instance == null) {
         return "";
      } else {
         String m = instance.mods.get();
         return m + "+" + (mute ? instance.muteKey.get() : instance.deafKey.get());
      }
   }

   public static void guide() {
      String[] lines = new String[]{
         "§9Discord-Steuerung einrichten (einmalig):",
         "§71. Discord öffnen → Zahnrad (Benutzereinstellungen) → §fTastenbelegung",
         "§72. §fTastenbelegung hinzufügen§7 → Aktion §fStummschalten umschalten§7 → Kürzel §f" + combo(true),
         "§73. Nochmal §fTastenbelegung hinzufügen§7 → §fTaubschalten umschalten§7 → Kürzel §f" + combo(false),
         "§74. Fertig! Im Chat (T) die Knöpfe anklicken oder in den Mod-Einstellungen eine Taste belegen."
      };

      try {
         for (String l : lines) {
            Mc.mc().gui.getChat().addMessage(Component.literal(l));
         }
      } catch (Throwable var5) {
      }

      Toasts.show(
         "discord", "Discord einrichten", "Kürzel " + combo(true) + " / " + combo(false) + " in Discord eintragen - Anleitung im Chat", -10983950, 6000L
      );
   }

   public static boolean hudOn() {
      return instance != null && instance.enabled;
   }

   static double fx() {
      return instance.posX.get() / 100.0;
   }

   static double fy() {
      return instance.posY.get() / 100.0;
   }

   static float sc() {
      return instance.scale.getF();
   }

   static boolean compactHud() {
      return instance.compact.get();
   }

   public static void toggleMute() {
      if (press(instance == null ? 'M' : instance.muteKey.get().charAt(0))) {
         muted = !muted;
         Toasts.show("discord", "Discord", muted ? "Mikrofon stumm" : "Mikrofon an", muted ? -1228219 : -12868259, 1800L);
      }
   }

   public static void toggleDeafen() {
      if (press(instance == null ? 'D' : instance.deafKey.get().charAt(0))) {
         deafened = !deafened;
         Toasts.show("discord", "Discord", deafened ? "Taub – du hörst nichts" : "Ton wieder an", deafened ? -1228219 : -12868259, 1800L);
      }
   }

   static boolean press(char letter) {
      if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
         Toasts.show("discord", "Discord", "Geht nur unter Windows", -1228219, 2500L);
         return false;
      } else {
         try {
            Class<?> f = Class.forName("com.sun.jna.Function");
            Object fn = f.getMethod("getFunction", String.class, String.class).invoke(null, "user32", "keybd_event");
            Method inv = f.getMethod("invokeVoid", Object[].class);
            int mi = instance == null ? 0 : instance.mods.index;
            int[] mod = mi == 0 ? new int[]{17, 16} : (mi == 1 ? new int[]{17, 18} : new int[]{18, 16});
            int[] down = new int[]{mod[0], mod[1], Character.toUpperCase(letter)};
            new Thread(() -> {
               try {
                  for (int k : down) {
                     inv.invoke(fn, (Object)new Object[]{(byte)k, (byte)0, 0, Pointer.NULL});
                  }

                  Thread.sleep(60L);

                  for (int i = down.length - 1; i >= 0; i--) {
                     inv.invoke(fn, (Object)new Object[]{(byte)down[i], (byte)0, 2, Pointer.NULL});
                  }
               } catch (Throwable var7x) {
                  LegoClient.LOG("Discord: " + var7x);
               }
            }, "Lego-Discord").start();
            return true;
         } catch (Throwable var7) {
            LegoClient.LOG("Discord: " + var7);
            Toasts.show("discord", "Discord", "Konnte die Tasten nicht senden", -1228219, 2500L);
            return false;
         }
      }
   }
}
