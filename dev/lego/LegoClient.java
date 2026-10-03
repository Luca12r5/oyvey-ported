package dev.lego;

import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.cosmetic.Cos;
import dev.lego.cosmetic.CosRender;
import dev.lego.cosmetic.PetWorld;
import dev.lego.cosmetic.VehicleWorld;
import dev.lego.discord.DiscordControl;
import dev.lego.discord.DiscordHud;
import dev.lego.emote.Cutscenes;
import dev.lego.emote.EmoteLib;
import dev.lego.emote.EmoteWheelView;
import dev.lego.emote.Emotes;
import dev.lego.games.Arcade;
import dev.lego.games.GameInfo;
import dev.lego.hud.HudEditorView;
import dev.lego.hud.HudManager;
import dev.lego.hud.HudModules;
import dev.lego.perf.Perf;
import dev.lego.perf.Performance;
import dev.lego.ui.Env;
import dev.lego.ui.Gx;
import dev.lego.ui.LegoScreen;
import dev.lego.ui.McBackend;
import dev.lego.ui.MenuView;
import dev.lego.ui.Toasts;
import dev.lego.ui.UiSettings;
import dev.lego.util.Utility;
import dev.lego.visual.Overlays;
import dev.lego.visual.Visuals;
import dev.lego.visual.Visuals2;
import dev.lego.visual.sky.SkyPacks;
import dev.spotifyhud.PlayerState;
import dev.spotifyhud.SpotifyHudMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionResult;

public final class LegoClient implements ClientModInitializer {
   public static final boolean DEBUG = Boolean.getBoolean("legoclient.debug");
   private static KeyMapping guiKey;
   private static KeyMapping zoomKey;
   private static KeyMapping emoteKey;
   private static KeyMapping invseeKey;
   private static KeyMapping ecseeKey;
   private static KeyMapping waypointKey;
   private static KeyMapping hudKey;
   private static SpotifyModule spotify;
   private static long joinedAt = 0L;
   private static final boolean[] lastDown = new boolean[512];
   private static final int[] MACRO_KEYS = new int[]{295, 296, 297, 298, 321, 322, 323, 324, 325, 326, 327, 328, 329};
   private static final boolean[] macroDown = new boolean[MACRO_KEYS.length];

   public static void LOG(String var0) {
      System.out.println("[LegoClient] " + var0);
   }

   public static long sessionSeconds() {
      return joinedAt == 0L ? 0L : (System.currentTimeMillis() - joinedAt) / 1000L;
   }

   @Override
   public void onInitializeClient() {
      boolean var1 = FabricLoader.getInstance().isModLoaded("spotifyhud");
      if (!var1) {
         try {
            new SpotifyHudMod().onInitializeClient();
         } catch (Throwable var3) {
            LOG("Spotify HUD Start fehlgeschlagen: " + var3);
         }
      } else {
         LOG("Separater Spotify HUD Mod gefunden - eingebaute Version wird nicht gestartet.");
      }

      UiSettings.register();
      HudModules.registerAll();
      Visuals.registerAll();
      Utility.registerAll();
      Performance.registerAll();
      Emotes.register();
      spotify = Modules.register(new SpotifyModule());
      Cos.register();
      EmoteLib.registerPersist();
      Arcade.registerAll();
      GameInfo.registerPersist();
      Modules.register(new DiscordControl());
      Modules.load();
      spotify.sync();
      Category var2 = Category.register(Identifier.fromNamespaceAndPath("legoclient", "main"));
      guiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.legoclient.gui", 344, var2));
      zoomKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.legoclient.zoom", 67, var2));
      emoteKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.legoclient.emotes", 66, var2));
      invseeKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.legoclient.invsee", 86, var2));
      ecseeKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.legoclient.ecsee", 71, var2));
      waypointKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.legoclient.waypoint", 78, var2));
      hudKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.legoclient.hud", -1, var2));
      setupEnv();
      ClientTickEvents.END_CLIENT_TICK.register(LegoClient::tick);
      ScreenEvents.AFTER_INIT.register((var0, var1x, var2x, var3x) -> {
         if (var1x instanceof ChatScreen) {
            ScreenMouseEvents.allowMouseClick(var1x).register((var0x, var1xx) -> {
               try {
                  return !DiscordHud.click(var1xx.x(), var1xx.y(), var1xx.button());
               } catch (Throwable var3xx) {
                  return true;
               }
            });
         }
      });
      HudElementRegistry.attachElementAfter(
         VanillaHudElements.MISC_OVERLAYS, Identifier.fromNamespaceAndPath("legoclient", "overlays"), (var0, var1x) -> renderOverlays(var0)
      );
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("legoclient", "hud"), (var0, var1x) -> renderHud(var0));
      HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, var0 -> (var1x, var2x) -> {
         if (!Cutscenes.active()) {
            if (Visuals.crosshair != null && Visuals.crosshair.enabled && Mc.firstPerson() && !Mc.mc().options.hideGui) {
               try {
                  Visuals.crosshair.draw(var1x);
               } catch (Throwable var4) {
                  var0.render(var1x, var2x);
               }
            } else {
               var0.render(var1x, var2x);
            }
         }
      });
      WorldRenderEvents.BEFORE_TRANSLUCENT.register(Visuals::render);
      WorldRenderEvents.BEFORE_BLOCK_OUTLINE
         .register((var0, var1x) -> Visuals2.outline == null || !Visuals2.outline.enabled || !Visuals2.outline.replaceVanilla());
      AttackEntityCallback.EVENT.register((var0, var1x, var2x, var3x, var4) -> {
         try {
            if (var0 == Mc.player()) {
               Visuals.onAttack(var3x);
            }
         } catch (Throwable var6) {
         }

         return InteractionResult.PASS;
      });
      ClientReceiveMessageEvents.ALLOW_CHAT.register((var0, var1x, var2x, var3x, var4) -> Utility.allowMessage(var0, false));
      ClientReceiveMessageEvents.ALLOW_GAME.register((var0, var1x) -> Utility.allowMessage(var0, var1x));
      ClientReceiveMessageEvents.CHAT.register((var0, var1x, var2x, var3x, var4) -> Utility.onMessage(var0, false));
      ClientReceiveMessageEvents.GAME.register((var0, var1x) -> Utility.onMessage(var0, var1x));
      LivingEntityFeatureRendererRegistrationCallback.EVENT.register((var0, var1x, var2x, var3x) -> {
         if (var1x instanceof AvatarRenderer) {
            try {
               var2x.register(CosRender.feature(var1x));
            } catch (Throwable var5) {
               LOG("Kosmetik-Renderer: " + var5);
            }
         }
      });
      ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         @Override
         public Identifier getFabricId() {
            return Identifier.fromNamespaceAndPath("legoclient", "skypacks");
         }

         @Override
         public void onResourceManagerReload(ResourceManager var1) {
            SkyPacks.invalidate();
         }
      });
      ScreenEvents.AFTER_INIT
         .register(
            (var0, var1x, var2x, var3x) -> {
               if (var1x instanceof ChatScreen) {
                  ScreenMouseEvents.allowMouseClick(var1x)
                     .register((var0x, var1xx) -> !safe(() -> HudManager.mouseDown(var1xx.x(), var1xx.y(), var1xx.button(), var0x.width, var0x.height)));
                  ScreenMouseEvents.allowMouseDrag(var1x)
                     .register((var0x, var1xx, var2xx, var4) -> !safe(() -> HudManager.mouseDrag(var1xx.x(), var1xx.y(), var0x.width, var0x.height, true)));
                  ScreenMouseEvents.allowMouseRelease(var1x).register((var0x, var1xx) -> !safe(HudManager::mouseUp));
                  ScreenMouseEvents.allowMouseScroll(var1x)
                     .register((var0x, var1xx, var3xx, var5, var7) -> !safe(() -> HudManager.scroll(var1xx, var3xx, var7, var0x.width, var0x.height)));
                  ScreenEvents.afterRender(var1x).register((var0x, var1xx, var2xx, var3xx, var4) -> {
                     try {
                        HudManager.render(var1xx, var1xx.guiWidth(), var1xx.guiHeight(), true, var2xx, var3xx);
                     } catch (Throwable var6) {
                        LOG("Chat-HUD: " + var6);
                     }
                  });
                  ScreenEvents.remove(var1x).register(var0x -> HudManager.mouseUp());
               }
            }
         );
      LOG("geladen - " + Modules.ALL.size() + " Module, " + Cos.ALL.size() + " Kosmetik-Items, " + EmoteLib.ALL.size() + " Emotes");
   }

   private static void setupEnv() {
      Env.version = "3.4.0";
      Env.playerName = () -> {
         try {
            return Mc.player() != null ? Mc.name(Mc.player()) : Mc.mc().getUser().getName();
         } catch (Throwable var1) {
            return "Spieler";
         }
      };
      Env.inWorld = Mc::inGame;
      Env.clipboard = () -> {
         try {
            String var0 = Mc.mc().keyboardHandler.getClipboard();
            return var0 == null ? "" : var0;
         } catch (Throwable var1) {
            return "";
         }
      };
      Env.nowPlaying = () -> {
         try {
            PlayerState var0 = null;
            if (SpotifyHudMod.api() != null) {
               var0 = SpotifyHudMod.api().state();
            }

            if ((var0 == null || !var0.hasTrack) && SpotifyHudMod.media() != null) {
               var0 = SpotifyHudMod.media().state();
            }

            return var0 != null && var0.hasTrack && var0.title != null
               ? var0.title + (var0.artist != null && !var0.artist.isEmpty() ? " · " + var0.artist : "")
               : "";
         } catch (Throwable var1) {
            return "";
         }
      };
      Env.openHudEditor = () -> LegoScreen.show(new HudEditorView());
      Env.copy = Mc::clipboard;
      Env.playEmote = Emotes::play;
      Env.playerPreview = CosRender::preview;
   }

   private static boolean safe(LegoClient.BoolAction var0) {
      try {
         return var0.run();
      } catch (Throwable var2) {
         LOG("Eingabe: " + var2);
         return false;
      }
   }

   private static void renderOverlays(GuiGraphics var0) {
      try {
         if (Mc.mc().options != null && Mc.mc().options.hideGui) {
            return;
         }

         McBackend.begin(var0);

         try {
            Overlays.render(var0.guiWidth() * Gx.S, var0.guiHeight() * Gx.S);
         } finally {
            Gx.end();
         }
      } catch (Throwable var5) {
         LOG("Overlay-Fehler: " + var5);
      }
   }

   private static void renderHud(GuiGraphics var0) {
      Perf.begin("HUD");

      try {
         Minecraft var1 = Mc.mc();
         HudModules.ClickCounter.frame();
         if (!Cutscenes.active()) {
            boolean var2 = var1.options != null && var1.options.hideGui;
            boolean var3 = var1.screen instanceof LegoScreen && ((LegoScreen)var1.screen).view() instanceof HudEditorView;
            if (!var2 && !(var1.screen instanceof ChatScreen) && !var3 && !Cutscenes.active()) {
               HudManager.render(var0, var0.guiWidth(), var0.guiHeight(), false, -1, -1);
            }

            if (!var2 && DiscordControl.hudOn()) {
               McBackend.begin(var0);

               try {
                  DiscordHud.render(var0.guiWidth() * Gx.S, var0.guiHeight() * Gx.S);
               } finally {
                  Gx.end();
               }
            }

            if (Toasts.active()) {
               McBackend.begin(var0);

               try {
                  Toasts.render(var0.guiWidth() * Gx.S, var0.guiHeight() * Gx.S);
               } finally {
                  Gx.end();
               }
            }

            if (Performance.monitor != null && Performance.monitor.enabled && !var2) {
               McBackend.begin(var0);

               try {
                  Performance.monitor.draw(var0.guiWidth() * Gx.S, var0.guiHeight() * Gx.S);
               } finally {
                  Gx.end();
               }
            }

            return;
         }

         McBackend.begin(var0);

         try {
            Overlays.cutscene(var0.guiWidth() * Gx.S, var0.guiHeight() * Gx.S);
         } finally {
            Gx.end();
         }
      } catch (Throwable var42) {
         LOG("HUD-Fehler: " + var42);
         return;
      } finally {
         Perf.end("HUD");
      }
   }

   private static void tick(Minecraft var0) {
      Perf.begin("Ticks");

      try {
         if (Mc.inGame() && joinedAt == 0L) {
            joinedAt = System.currentTimeMillis();
         }

         if (!Mc.inGame()) {
            joinedAt = 0L;
         }

         spotify.sync();

         while (guiKey.consumeClick()) {
            if (var0.screen == null) {
               LegoScreen.show(new MenuView());
            }
         }

         while (emoteKey.consumeClick()) {
            if (var0.screen == null && Emotes.module.enabled && Mc.inGame()) {
               int var1 = -1;

               try {
                  var1 = KeyBindingHelper.getBoundKeyOf(emoteKey).getValue();
               } catch (Throwable var10) {
               }

               LegoScreen.show(new EmoteWheelView(var1));
            }
         }

         while (invseeKey.consumeClick()) {
            Utility.invsee.use();
         }

         while (ecseeKey.consumeClick()) {
            Utility.ecsee.use();
         }

         while (waypointKey.consumeClick()) {
            if (Utility.waypoints.enabled) {
               Utility.Waypoints.addHere();
            }
         }

         while (hudKey.consumeClick()) {
            if (var0.screen == null) {
               LegoScreen.show(new HudEditorView());
            }
         }

         Visuals.zoom.held = zoomKey.isDown();
         if (var0.screen == null) {
            for (Module var2 : Modules.ALL) {
               if (var2.key >= 0 && var2.key < lastDown.length) {
                  boolean var3 = Mc.keyDown(var2.key);
                  if (var3 && !lastDown[var2.key]) {
                     if (var2.isToggleable()) {
                        var2.toggle();
                        Modules.scheduleSave();
                        Toasts.show(var2.icon, var2.name, var2.enabled ? "Aktiviert" : "Deaktiviert", var2.enabled ? 0 : -7696487, 1400L);
                     } else if (!Utility.runAction(var2) && var2 == Performance.memory) {
                        Performance.FreeMemory.run();
                     }
                  }

                  lastDown[var2.key] = var3;
               }
            }

            for (int var14 = 0; var14 < MACRO_KEYS.length; var14++) {
               boolean var16 = Mc.keyDown(MACRO_KEYS[var14]);
               if (var16 && !macroDown[var14]) {
                  Utility.onKey(MACRO_KEYS[var14]);
               }

               macroDown[var14] = var16;
            }
         }

         if (!Mc.inGame()) {
            if (Emotes.active()) {
               Emotes.stop();
            }
         } else {
            for (Module var17 : Modules.ALL) {
               if (var17.enabled) {
                  try {
                     var17.tick();
                  } catch (Throwable var9) {
                     LOG("Tick " + var17.id + ": " + var9);
                  }
               }
            }

            Emotes.tick();
            CosRender.prewarm();
            PetWorld.tick();
            VehicleWorld.tick();
         }

         Modules.tickSave();
      } catch (Throwable var11) {
         LOG("Tick-Fehler: " + var11);
      } finally {
         Perf.end("Ticks");
      }
   }

   private interface BoolAction {
      boolean run();
   }
}
