package dev.lego.ci;

import dev.lego.LegoClient;
import dev.lego.cosmetic.Cos;
import dev.lego.cosmetic.KnifeTricks;
import dev.lego.ui.LegoScreen;
import dev.lego.ui.MenuView;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.server.IntegratedServer;

/**
 * CI only (-Dlegoclient.ci.shots=1, set by apps/launcher/test/real-launch.ts --shots): once in
 * a world, walks through the client's looks and takes a screenshot of each (knife in hand,
 * knife instead of a sword, spinning, inspecting, Visor headset, menu, third person), then
 * quits. The test prints the pictures into the CI log so they can be checked.
 */
public final class CiShots {
   private static final boolean ON = System.getProperty("legoclient.ci.shots") != null;
   private static long joined;
   private static int step;
   private static boolean knifeKey;

   private CiShots() {
   }

   private static void shot(Minecraft mc, String name) {
      LegoClient.LOG("CI shot " + name);
      Screenshot.grab(mc.gameDirectory, mc.getMainRenderTarget(), msg -> LegoClient.LOG("CI shot saved " + name + ": " + msg.getString()));
   }

   private static void command(Minecraft mc, String cmd) {
      IntegratedServer server = mc.getSingleplayerServer();
      if (server == null) return;
      server.execute(() -> {
         try {
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), cmd);
         } catch (Throwable e) {
            LegoClient.LOG("CI command " + cmd + ": " + e);
         }
      });
   }

   /** Knife tricks key state for the CI run (or-ed with the real key). */
   public static boolean knifeKey() {
      return knifeKey;
   }

   public static void tick(Minecraft mc) {
      if (!ON) return;
      if (mc.player == null || mc.level == null) {
         joined = 0L;
         return;
      }
      long now = System.currentTimeMillis();
      if (joined == 0L) {
         joined = now;
         step = 0;
         LegoClient.LOG("CI in world");
      }
      float t = (now - joined) / 1000.0F;
      String player = mc.player.getName().getString();
      switch (step) {
         case 0 -> {
            if (t < 3.0F) return;
            command(mc, "time set 6000");
            command(mc, "weather clear");
            command(mc, "clear " + player);
            Cos.Item knife = Cos.get("knife_karambit_sunset");
            if (knife != null && !Cos.isEquipped(knife)) Cos.toggle(knife);
            Cos.Item cape = Cos.equipped(Cos.Slot.CAPE);
            if (cape == null) {
               Cos.Item any = Cos.get("cape_m_galaxy_crown");
               if (any != null) Cos.toggle(any);
            }
         }
         case 1 -> { if (t < 9.0F) return; shot(mc, "knife-hand"); }
         case 2 -> {
            if (t < 10.0F) return;
            command(mc, "give " + player + " minecraft:diamond_sword");
            dev.lego.cosmetic.KnifeView.ciReplaceSwords();
         }
         case 3 -> { if (t < 14.0F) return; shot(mc, "knife-sword"); }
         case 4 -> { if (t < 14.5F) return; knifeKey = true; }
         case 5 -> { if (t < 16.0F) return; shot(mc, "knife-spin"); knifeKey = false; }
         case 6 -> { if (t < 19.0F) return; knifeKey = true; }
         case 7 -> { if (t < 19.1F) return; knifeKey = false; }
         case 8 -> { if (t < 19.9F) return; shot(mc, "knife-inspect"); }
         case 9 -> { if (t < 22.0F) return; dev.visor.VisorController.get().toggle(); }
         case 10 -> { if (t < 28.0F) return; shot(mc, "visor"); }
         case 11 -> { if (t < 29.0F) return; dev.visor.VisorController.get().toggle(); }
         case 12 -> { if (t < 32.0F) return; LegoScreen.show(new MenuView()); }
         case 13 -> { if (t < 35.0F) return; shot(mc, "menu"); }
         case 14 -> { if (t < 36.0F) return; mc.setScreen(null); mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT); }
         case 15 -> { if (t < 40.0F) return; shot(mc, "third-person"); }
         case 16 -> {
            if (t < 42.0F) return;
            LegoClient.LOG("CI shots done");
            mc.stop();
         }
         default -> {
            return;
         }
      }
      step++;
   }
}
