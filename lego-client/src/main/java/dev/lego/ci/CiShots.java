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
   private static dev.lego.ui.hub.HubView hub;
   private static final boolean ON = System.getProperty("legoclient.ci.shots") != null;
   private static long joined;
   private static int step;
   private static boolean knifeKey;
   private static long waitingSince;
   private static long lastReport;
   private static int stuckShots;

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
         // Not in the world yet: report which screen is open, and take a picture if it hangs.
         long w = System.currentTimeMillis();
         if (waitingSince == 0L) waitingSince = w;
         if (w - lastReport > 15000L) {
            lastReport = w;
            LegoClient.LOG("CI waiting " + (w - waitingSince) / 1000L + "s, screen " + (mc.screen == null ? "none" : mc.screen.getClass().getName()) + (mc.getOverlay() == null ? "" : ", overlay " + mc.getOverlay().getClass().getName()));
            if (w - waitingSince > 60000L && stuckShots < 3) {
               stuckShots++;
               shot(mc, "waiting-" + stuckShots);
            }
         }
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
            // No mobs: a slime killed the CI player once and the death screen blocked the Visor shot.
            command(mc, "difficulty peaceful");
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
         case 9 -> { if (t < 22.0F) return; dev.lego.compat.VisorBridge.toggle(); }
         case 10 -> {
            if (t < 28.0F) return;
            dev.visor.VisorController v = dev.visor.VisorController.get();
            dev.lego.LegoClient.LOG("CI visor: state=" + v.state() + " app=" + v.app() + " capture=" + dev.visor.render.FrameCapture.valid
               + " cam=" + dev.visor.render.FrameCapture.cameraPosition + " proj00=" + dev.visor.render.FrameCapture.PROJECTION.m00()
               + " proj22=" + dev.visor.render.FrameCapture.PROJECTION.m22() + " proj32=" + dev.visor.render.FrameCapture.PROJECTION.m32());
            shot(mc, "visor");
         }
         case 11 -> { if (t < 29.0F) return; dev.lego.compat.VisorBridge.toggle(); }
         case 12 -> { if (t < 32.0F) return; LegoScreen.show(new MenuView()); }
         case 13 -> { if (t < 35.0F) return; shot(mc, "menu"); }
         case 14 -> { if (t < 36.0F) return; mc.setScreen(null); mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT); }
         case 15 -> { if (t < 40.0F) return; shot(mc, "third-person"); }
         // Launcher emotes in the client: car and chair (third person, seen from the front).
         case 16 -> { if (t < 41.0F) return; dev.lego.emote.Emotes.play(dev.lego.emote.EmoteLib.index("lx_car")); }
         case 17 -> { if (t < 43.4F) return; shot(mc, "emote-car"); }
         case 18 -> { if (t < 48.0F) return; dev.lego.emote.Emotes.play(dev.lego.emote.EmoteLib.index("lx_chair")); }
         case 19 -> { if (t < 50.4F) return; shot(mc, "emote-chair"); }
         // Hub search: typing filters the cosmetics (the shot shows the search field and the result).
         case 20 -> {
            if (t < 52.0F) return;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            hub = new dev.lego.ui.hub.HubView(dev.lego.ui.hub.HubView.Section.COSMETICS);
            LegoScreen.show(hub);
         }
         case 21 -> { if (t < 54.0F) return; hub.chr("k"); hub.chr("r"); hub.chr("o"); }
         case 22 -> { if (t < 56.0F) return; shot(mc, "hub-search"); }
         // Custom TxT (Bauloo Styles): the category, a crystal and fire style, the Bauloo menu.
         case 23 -> {
            if (t < 58.0F) return;
            try {
               org.spongepowered.asm.mixin.MixinEnvironment.getCurrentEnvironment().audit();
               LegoClient.LOG("CI bauloo mixins audited");
            } catch (Throwable e) {
               LegoClient.LOG("CI bauloo mixin audit: " + e);
            }
            dev.lego.ui.hub.ModsPart.showCategory(dev.lego.core.Category.CUSTOM_TXT);
            LegoScreen.show(new dev.lego.ui.hub.HubView(dev.lego.ui.hub.HubView.Section.MODS));
         }
         case 24 -> { if (t < 61.0F) return; shot(mc, "custom-txt"); }
         case 25 -> {
            if (t < 62.0F) return;
            mc.setScreen(null);
            command(mc, "execute as " + player + " at " + player + " rotated as " + player + " run summon minecraft:end_crystal ^ ^ ^4");
            dev.lego.txt.TxtModules.ciDemo();
         }
         case 26 -> {
            if (t < 70.0F || mc.getOverlay() != null) return;
            LegoClient.LOG("CI custom txt packs: " + mc.getResourcePackRepository().getSelectedIds());
            shot(mc, "txt-crystal");
         }
         case 27 -> { if (t < 71.0F) return; dev.lego.txt.TxtModules.openMenu(); }
         case 28 -> {
            if (t < 75.0F) return;
            LegoClient.LOG("CI bauloo menu: " + (mc.screen == null ? "none" : mc.screen.getClass().getSimpleName()));
            shot(mc, "bauloo-menu");
         }
         case 29 -> {
            if (t < 76.0F) return;
            mc.setScreen(null);
            dev.lego.txt.TxtModules.ciReset();
         }
         case 30 -> {
            if (t < 80.0F || mc.getOverlay() != null) return;
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
