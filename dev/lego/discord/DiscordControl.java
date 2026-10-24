package dev.lego.discord;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Module;
import dev.lego.core.Setting;
import dev.lego.ui.Toasts;
import java.lang.reflect.Method;

public final class DiscordControl extends Module {
   private static final int VK_CTRL = 17;
   private static final int VK_SHIFT = 16;
   private static final int VK_M = 77;
   private static final int VK_D = 68;
   public static boolean muted;
   public static boolean deafened;
   public static DiscordControl instance;
   final Setting.Num posX = this.add(new Setting.Num("Position X", 0.0, 95.0, 1.0, 2.0, "%"));
   final Setting.Num posY = this.add(new Setting.Num("Position Y", 0.0, 95.0, 1.0, 38.0, "%"));
   final Setting.Num scale = this.add(new Setting.Num("Größe", 0.7, 2.0, 0.1, 1.0, "x"));

   public DiscordControl() {
      super(
         "discord", "Discord-Steuerung", "HUD mit Knöpfen für Mikro und Ton - im Chat anklicken · nutzt Discords Kürzel Strg+Umschalt+M / D", Category.UTILITY
      );
      this.icon("mute");
      this.fresh();
      instance = this;
      this.add(new Setting.Action("Taub", "Ton komplett aus / an", DiscordControl::toggleDeafen));
      this.add(new Setting.Action("Stumm", "Mikrofon stumm / an", DiscordControl::toggleMute));
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

   public static void toggleMute() {
      if (press(77)) {
         muted = !muted;
         Toasts.show("discord", "Discord", muted ? "Mikrofon stumm" : "Mikrofon an", muted ? -1228219 : -12868259, 1800L);
      }
   }

   public static void toggleDeafen() {
      if (press(68)) {
         deafened = !deafened;
         Toasts.show("discord", "Discord", deafened ? "Taub – du hörst nichts" : "Ton wieder an", deafened ? -1228219 : -12868259, 1800L);
      }
   }

   static boolean press(int var0) {
      if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
         Toasts.show("discord", "Discord", "Geht nur unter Windows", -1228219, 2500L);
         return false;
      } else {
         try {
            Class var1 = Class.forName("com.sun.jna.Function");
            Object var2 = var1.getMethod("getFunction", String.class, String.class).invoke(null, "user32", "keybd_event");
            Method var3 = var1.getMethod("invokeVoid", Object[].class);
            int[] var4 = new int[]{17, 16, var0};
            new Thread(() -> {
               try {
                  for (int var6 : var4) {
                     var3.invoke(var2, (Object)new Object[]{(byte)var6, (byte)0, 0, null});
                  }

                  Thread.sleep(30L);

                  for (int var8 = var4.length - 1; var8 >= 0; var8--) {
                     var3.invoke(var2, (Object)new Object[]{(byte)var4[var8], (byte)0, 2, null});
                  }
               } catch (Throwable var7) {
                  LegoClient.LOG("Discord: " + var7);
               }
            }, "Lego-Discord").start();
            return true;
         } catch (Throwable var5) {
            LegoClient.LOG("Discord: " + var5);
            Toasts.show("discord", "Discord", "Konnte die Tasten nicht senden", -1228219, 2500L);
            return false;
         }
      }
   }
}
