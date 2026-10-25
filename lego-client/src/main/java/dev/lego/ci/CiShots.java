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
   private static int visorSub;
   private static int keySub;
   private static long pressedAt;
   private static int sweep = -1;
   private static int sweepTicks;
   private static dev.lego.ui.bench.BenchView bench;
   /** Cape texture the Bauloo cape mixin last put on the local player (CI check). */
   public static volatile String baulooCape;
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
         // The LEGO menu key path (Right Ctrl in a world): headset with the home panel, or the click GUI.
         // Pressed as a real key (X11 test event from the CI script), like a player would:
         // Right Ctrl opens the click GUI (LEGO Werkbank), clicks only plan, ANWENDEN puts it all in.
         case 9 -> {
            if (t < 22.0F) return;
            if (keySub == 0) {
               LegoClient.LOG("CI press Control_R");
               keySub = 1;
               return;
            }
            if (keySub == 1) {
               if (t < 25.0F) return;
               Object view = mc.screen instanceof LegoScreen ls ? ls.view() : null;
               LegoClient.LOG("CI key gui: screen=" + (mc.screen == null ? "none" : mc.screen.getClass().getName()) + " view=" + (view == null ? "none" : view.getClass().getSimpleName()));
               bench = view instanceof dev.lego.ui.bench.BenchView b ? b : null;
               if (bench == null) {
                  bench = dev.lego.ui.bench.BenchView.on(dev.lego.core.Category.HUD);
                  LegoScreen.show(bench);
               }
               keySub = 2;
               return;
            }
            if (keySub == 2) {
               if (t < 26.5F) return;
               shot(mc, "bench");
               keySub = 3;
               return;
            }
            if (keySub == 3) {
               if (t < 27.0F) return;
               benchPlan();
               keySub = 4;
               return;
            }
            if (keySub == 4) {
               if (t < 29.0F) return;
               LegoClient.LOG("CI bench plan: " + dev.lego.ui.bench.BenchView.draft().count() + " changes");
               shot(mc, "bench-plan");
               keySub = 5;
               return;
            }
            if (keySub == 5) {
               if (t < 30.0F) return;
               bench.ciApply();
               LegoClient.LOG("CI bench applied, left: " + dev.lego.ui.bench.BenchView.draft().count());
               keySub = 6;
               return;
            }
            if (keySub == 6) {
               // after the single resource reload of the applied Custom TxT styles
               if (t < 31.5F || mc.getOverlay() != null) return;
               shot(mc, "bench-applied");
               keySub = 7;
               return;
            }
            if (t < 34.0F || mc.getOverlay() != null) return;
            mc.setScreen(null);
            // The Visor has its own key: Right Shift.
            LegoClient.LOG("CI press Shift_R");
            pressedAt = now;
         }
         case 10 -> {
            if (t < 42F || now - pressedAt < 4000L) return;
            dev.visor.VisorController v = dev.visor.VisorController.get();
            LegoClient.LOG("CI key visor on: state=" + v.state());
            if (v.state() == dev.visor.VisorController.State.OFF) {
               dev.lego.compat.VisorBridge.openMenu();
               return;
            }
            dev.lego.LegoClient.LOG("CI visor: state=" + v.state() + " app=" + v.app() + " capture=" + dev.visor.render.FrameCapture.valid
               + " cam=" + dev.visor.render.FrameCapture.cameraPosition + " proj00=" + dev.visor.render.FrameCapture.PROJECTION.m00()
               + " proj22=" + dev.visor.render.FrameCapture.PROJECTION.m22() + " proj32=" + dev.visor.render.FrameCapture.PROJECTION.m32());
            shot(mc, "visor");
         }
         // The LEGO modules in the headset menu.
         case 11 -> {
            if (t < 43F) return;
            if (visorSub == 0) {
               dev.visor.VisorController.get().mainPanel.showCategory(dev.visor.module.Category.HUD);
               visorSub = 1;
               pressedAt = now;
               return;
            }
            if (now - pressedAt < 2000L) return;
            dev.lego.LegoClient.LOG("CI visor lego modules: " + dev.visor.module.ModuleManager.of(dev.visor.module.Category.HUD).size());
            shot(mc, "visor-lego");
            LegoClient.LOG("CI press Shift_R");
            pressedAt = now;
         }
         case 12 -> {
            if (t < 49F || now - pressedAt < 3000L) return;
            LegoClient.LOG("CI key visor: state=" + dev.visor.VisorController.get().state());
            if (dev.lego.compat.VisorBridge.active()) dev.lego.compat.VisorBridge.toggle();
            LegoScreen.show(new MenuView());
         }
         case 13 -> { if (t < 52F) return; shot(mc, "menu"); }
         case 14 -> { if (t < 53F) return; mc.setScreen(null); mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT); }
         case 15 -> { if (t < 57F) return; shot(mc, "third-person"); }
         // Launcher emotes in the client: car and chair (third person, seen from the front).
         case 16 -> { if (t < 58F) return; dev.lego.emote.Emotes.play(dev.lego.emote.EmoteLib.index("lx_car")); }
         case 17 -> { if (t < 60.4F) return; shot(mc, "emote-car"); }
         case 18 -> { if (t < 65F) return; dev.lego.emote.Emotes.play(dev.lego.emote.EmoteLib.index("lx_chair")); }
         case 19 -> { if (t < 67.4F) return; shot(mc, "emote-chair"); }
         // Bauloo cape (Custom TxT cosmetics), seen from behind.
         case 20 -> {
            if (t < 69F) return;
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            baulooCape = null;
            de.bauloo.styles.Cosmetics.save(de.bauloo.styles.Cosmetics.Slot.CAPE, "tornado_night");
         }
         case 21 -> {
            if (t < 74F) return;
            LegoClient.LOG("CI bauloo cape: picked=" + de.bauloo.styles.Cosmetics.capeFor(mc.player) + " drawn=" + baulooCape);
            shot(mc, "bauloo-cape");
            de.bauloo.styles.Cosmetics.save(de.bauloo.styles.Cosmetics.Slot.CAPE, "none");
         }
         // Work bench search: typing filters the modules (the shot shows the search field and the result).
         case 22 -> {
            if (t < 75F) return;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            bench = dev.lego.ui.bench.BenchView.on(dev.lego.core.Category.HUD);
            LegoScreen.show(bench);
         }
         case 23 -> { if (t < 77F) return; bench.ciType("f"); bench.ciType("p"); bench.ciType("s"); }
         case 24 -> { if (t < 79F) return; shot(mc, "hub-search"); }
         // Custom TxT (Bauloo Styles): the category, a crystal and fire style, the Bauloo menu.
         case 25 -> {
            if (t < 81F) return;
            try {
               org.spongepowered.asm.mixin.MixinEnvironment.getCurrentEnvironment().audit();
               LegoClient.LOG("CI bauloo mixins audited");
            } catch (Throwable e) {
               LegoClient.LOG("CI bauloo mixin audit: " + e);
            }
            LegoScreen.show(dev.lego.ui.bench.BenchView.on(dev.lego.core.Category.CUSTOM_TXT));
         }
         case 26 -> { if (t < 84F) return; shot(mc, "custom-txt"); }
         case 27 -> {
            if (t < 85F) return;
            mc.setScreen(null);
            command(mc, "execute as " + player + " at " + player + " rotated as " + player + " run summon minecraft:end_crystal ^ ^ ^4");
            dev.lego.txt.TxtModules.ciDemo();
         }
         case 28 -> {
            if (t < 93F || mc.getOverlay() != null) return;
            LegoClient.LOG("CI custom txt packs: " + mc.getResourcePackRepository().getSelectedIds());
            shot(mc, "txt-crystal");
         }
         case 29 -> { if (t < 94F) return; dev.lego.txt.TxtModules.openMenu(); }
         case 30 -> {
            if (t < 98F) return;
            LegoClient.LOG("CI bauloo menu: " + (mc.screen == null ? "none" : mc.screen.getClass().getSimpleName()));
            shot(mc, "bauloo-menu");
         }
         case 31 -> {
            if (t < 99F) return;
            mc.setScreen(null);
            dev.lego.txt.TxtModules.ciReset();
         }
         // Every cosmetic once, worn while the cosmetics page of the work bench shows the player.
         case 32 -> {
            if (t < 103F || mc.getOverlay() != null) return;
            if (sweep < 0) {
               sweep = 0;
               Cos.unequipAll();
               LegoScreen.show(dev.lego.ui.bench.BenchView.at("COSMETICS"));
               LegoClient.LOG("CI cosmetic sweep: " + Cos.ALL.size() + " items");
               return;
            }
            if (++sweepTicks % 3 != 0) return;
            if (sweep > 0) {
               Cos.Item prev = Cos.ALL.get(sweep - 1);
               if (Cos.isEquipped(prev)) Cos.toggle(prev);
            }
            if (sweep < Cos.ALL.size()) {
               Cos.Item it = Cos.ALL.get(sweep++);
               LegoClient.LOG("CI cosmetic " + sweep + " " + it.id);
               try {
                  Cos.toggle(it);
               } catch (Throwable e) {
                  LegoClient.LOG("CI cosmetic " + it.id + ": " + e);
               }
               if (sweep == 40) shot(mc, "bench-cosmetics");
               return;
            }
            LegoClient.LOG("CI cosmetic sweep done");
            Cos.unequipAll();
            mc.setScreen(null);
         }
         case 33 -> {
            if (t < 103F || mc.getOverlay() != null) return;
            LegoClient.LOG("CI shots done");
            mc.stop();
         }
         default -> {
            return;
         }
      }
      step++;
   }

   /** CI: plans two HUD modules, a slider and two Custom TxT styles in the work bench. */
   private static void benchPlan() {
      dev.lego.ui.bench.Draft d = dev.lego.ui.bench.BenchView.draft();
      int n = 0;
      dev.lego.core.Module withNum = null;
      for (dev.lego.core.Module m : dev.lego.core.Modules.of(dev.lego.core.Category.HUD)) {
         if (m.hidden) continue;
         if (n < 2 && m.isToggleable() && !m.enabled) {
            d.toggle(m);
            n++;
         }
         if (withNum == null) {
            for (dev.lego.core.Setting s : m.settings) {
               if (s instanceof dev.lego.core.Setting.Num) {
                  withNum = m;
                  break;
               }
            }
         }
      }
      if (withNum != null) {
         bench.ciTools(withNum);
         for (dev.lego.core.Setting s : withNum.settings) {
            if (s instanceof dev.lego.core.Setting.Num) {
               dev.lego.core.Setting.Num c = (dev.lego.core.Setting.Num)d.edit(withNum, s);
               c.setFrac(c.frac() > 0.5 ? 0.2 : 0.8);
               break;
            }
         }
      }
      for (dev.lego.txt.TxtModules.StyleModule m : dev.lego.txt.TxtModules.STYLES) {
         String folder = m.tab == de.bauloo.styles.BaulooStylesClient.Tab.CRYSTAL ? "crystal_aqua" : m.tab == de.bauloo.styles.BaulooStylesClient.Tab.FIRE ? "fire_ultralow" : null;
         if (folder == null || m.indexOf(folder) < 0) continue;
         if (!d.on(m)) d.toggle(m);
         ((dev.lego.core.Setting.Mode)d.edit(m, m.style)).index = m.indexOf(folder);
      }
   }
}
