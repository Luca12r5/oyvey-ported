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
import dev.lego.hud.HudModules2;
import dev.lego.hud.HudPack;
import dev.lego.net.LegoSync;
import dev.lego.perf.Perf;
import dev.lego.perf.Performance;
import dev.lego.ui.Env;
import dev.lego.ui.Gx;
import dev.lego.ui.IntroView;
import dev.lego.ui.LegoScreen;
import dev.lego.ui.McBackend;
import dev.lego.ui.MenuView;
import dev.lego.ui.SpotifyView;
import dev.lego.ui.Toasts;
import dev.lego.ui.UiSettings;
import dev.lego.util.UtilPack;
import dev.lego.util.Utility;
import dev.lego.visual.Overlays;
import dev.lego.visual.Visuals;
import dev.lego.visual.Visuals2;
import dev.lego.visual.Visuals3;
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
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
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
   private static KeyMapping spotifyKey;
   private static KeyMapping dcMuteKey;
   private static KeyMapping dcDeafKey;
   private static SpotifyModule spotify;
   private static long joinedAt = 0L;
   private static boolean introShown = false;
   private static final boolean[] lastDown = new boolean[512];
   private static final int[] MACRO_KEYS = new int[]{295, 296, 297, 298, 321, 322, 323, 324, 325, 326, 327, 328, 329};
   private static final boolean[] macroDown = new boolean[MACRO_KEYS.length];

   public static void LOG(String msg) {
      if (DEBUG) {
         System.out.println("[LegoClient] " + msg);
      }
   }

   public static long sessionSeconds() {
      return joinedAt == 0L ? 0L : (System.currentTimeMillis() - joinedAt) / 1000L;
   }

   @Override
   public void onInitializeClient() {
      boolean standalone = FabricLoader.getInstance().isModLoaded("spotifyhud");
      if (!standalone) {
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
      HudModules2.registerAll();
      HudPack.registerAll();
      Visuals.registerAll();
      Utility.registerAll();
      UtilPack.registerAll();
      Performance.registerAll();
      Emotes.register();
      spotify = Modules.register(new SpotifyModule());
      Cos.register();
      EmoteLib.registerPersist();
      Arcade.registerAll();
      GameInfo.registerPersist();
      Modules.register(new DiscordControl());
      LegoSync.register();
      Modules.load();
      spotify.sync();
      Category cat = Category.register(Identifier.fromNamespaceAndPath("legoclient", "main"));
      guiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.legoclient.gui", 344, cat));
      zoomKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.legoclient.zoom", 67, cat));
      emoteKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.legoclient.emotes", 66, cat));
      invseeKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.legoclient.invsee", 86, cat));
      ecseeKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.legoclient.ecsee", 71, cat));
      waypointKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.legoclient.waypoint", 78, cat));
      hudKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.legoclient.hud", -1, cat));
      spotifyKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.legoclient.spotifymenu", 85, cat));
      dcMuteKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.legoclient.discordmute", -1, cat));
      dcDeafKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.legoclient.discorddeafen", -1, cat));
      setupEnv();
      ClientTickEvents.END_CLIENT_TICK.register(LegoClient::tick);
      ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
         if (!introShown && UiSettings.intro && screen instanceof TitleScreen) {
            if (System.getProperty("legoclient.introPlayed") != null) {
               introShown = true;
            } else {
               System.setProperty("legoclient.introPlayed", "1");
               introShown = true;
               client.execute(() -> client.setScreen(new LegoScreen(new IntroView(), screen)));
            }
         }
      });
      ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
         if (screen instanceof ChatScreen) {
            ScreenMouseEvents.allowMouseClick(screen).register((s, click) -> {
               try {
                  return !DiscordHud.click(click.x(), click.y(), click.button());
               } catch (Throwable var3x) {
                  return true;
               }
            });
         }
      });
      HudElementRegistry.attachElementAfter(
         VanillaHudElements.MISC_OVERLAYS, Identifier.fromNamespaceAndPath("legoclient", "overlays"), (ctx, tc) -> renderOverlays(ctx)
      );
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("legoclient", "hud"), (ctx, tc) -> renderHud(ctx));
      HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, old -> (ctx, tc) -> {
         if (!Cutscenes.active()) {
            if (Visuals.crosshair != null && Visuals.crosshair.enabled && Mc.firstPerson() && !Mc.mc().options.hideGui) {
               try {
                  Visuals.crosshair.draw(ctx);
               } catch (Throwable var4) {
                  old.render(ctx, tc);
               }
            } else {
               old.render(ctx, tc);
            }
         }
      });
      WorldRenderEvents.BEFORE_TRANSLUCENT.register(Visuals::render);
      WorldRenderEvents.BEFORE_BLOCK_OUTLINE
         .register((ctx, hit) -> Visuals2.outline == null || !Visuals2.outline.enabled || !Visuals2.outline.replaceVanilla());
      AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
         try {
            if (player == Mc.player()) {
               Visuals.onAttack(entity);
            }
         } catch (Throwable var6) {
         }

         return InteractionResult.PASS;
      });
      ClientReceiveMessageEvents.ALLOW_CHAT.register((msg, signed, sender, params, ts) -> Utility.allowMessage(msg, false));
      ClientReceiveMessageEvents.ALLOW_GAME.register((msg, overlay) -> Utility.allowMessage(msg, overlay));
      ClientReceiveMessageEvents.CHAT.register((msg, signed, sender, params, ts) -> Utility.onMessage(msg, false));
      ClientReceiveMessageEvents.GAME.register((msg, overlay) -> Utility.onMessage(msg, overlay));
      LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, ctx) -> {
         if (renderer instanceof AvatarRenderer) {
            try {
               helper.register(CosRender.feature(renderer));
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
         public void onResourceManagerReload(ResourceManager manager) {
            SkyPacks.invalidate();
         }
      });
      ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
         if (screen instanceof ChatScreen) {
            ScreenMouseEvents.allowMouseClick(screen).register((s, c) -> !safe(() -> HudManager.mouseDown(c.x(), c.y(), c.button(), s.width, s.height)));
            ScreenMouseEvents.allowMouseDrag(screen).register((s, c, dx, dy) -> !safe(() -> HudManager.mouseDrag(c.x(), c.y(), s.width, s.height, true)));
            ScreenMouseEvents.allowMouseRelease(screen).register((s, c) -> !safe(HudManager::mouseUp));
            ScreenMouseEvents.allowMouseScroll(screen).register((s, mx, my, hz, v) -> !safe(() -> HudManager.scroll(mx, my, v, s.width, s.height)));
            ScreenEvents.afterRender(screen).register((s, ctx, mx, my, d) -> {
               try {
                  HudManager.render(ctx, ctx.guiWidth(), ctx.guiHeight(), true, mx, my);
               } catch (Throwable var6) {
                  LOG("Chat-HUD: " + var6);
               }
            });
            ScreenEvents.remove(screen).register(s -> HudManager.mouseUp());
         }
      });
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
            String s = Mc.mc().keyboardHandler.getClipboard();
            return s == null ? "" : s;
         } catch (Throwable var1) {
            return "";
         }
      };
      Env.nowPlaying = () -> {
         try {
            PlayerState st = null;
            if (SpotifyHudMod.api() != null) {
               st = SpotifyHudMod.api().state();
            }

            if ((st == null || !st.hasTrack) && SpotifyHudMod.media() != null) {
               st = SpotifyHudMod.media().state();
            }

            return st != null && st.hasTrack && st.title != null ? st.title + (st.artist != null && !st.artist.isEmpty() ? " · " + st.artist : "") : "";
         } catch (Throwable var1) {
            return "";
         }
      };
      Env.loading = () -> {
         try {
            return Mc.mc().getOverlay() != null;
         } catch (Throwable var1) {
            return false;
         }
      };
      Env.openHudEditor = () -> LegoScreen.show(new HudEditorView());
      Env.copy = Mc::clipboard;
      Env.playEmote = Emotes::play;
      Env.playerPreview = CosRender::preview;
   }

   private static boolean safe(LegoClient.BoolAction a) {
      try {
         return a.run();
      } catch (Throwable var2) {
         LOG("Eingabe: " + var2);
         return false;
      }
   }

   private static void renderOverlays(GuiGraphics ctx) {
      try {
         if (Mc.mc().options != null && Mc.mc().options.hideGui) {
            return;
         }

         McBackend.begin(ctx);

         try {
            int ow = ctx.guiWidth() * Gx.S;
            int oh = ctx.guiHeight() * Gx.S;
            Overlays.render(ow, oh);
            Visuals2.renderHud(ow, oh);
            Visuals3.renderHud(ow, oh);
            if (Utility.auction != null) {
               Utility.auction.renderHud(ow, oh);
            }
         } finally {
            Gx.end();
         }
      } catch (Throwable var7) {
         LOG("Overlay-Fehler: " + var7);
      }
   }

   private static void renderHud(GuiGraphics ctx) {
      Perf.begin("HUD");

      try {
         Minecraft mc = Mc.mc();
         HudModules.ClickCounter.frame();
         if (!Cutscenes.active()) {
            boolean f1 = mc.options != null && mc.options.hideGui;
            boolean editor = mc.screen instanceof LegoScreen && ((LegoScreen)mc.screen).view() instanceof HudEditorView;
            if (!f1 && !(mc.screen instanceof ChatScreen) && !editor && !Cutscenes.active()) {
               HudManager.render(ctx, ctx.guiWidth(), ctx.guiHeight(), false, -1, -1);
            }

            if (!f1 && DiscordControl.hudOn()) {
               McBackend.begin(ctx);

               try {
                  DiscordHud.render(ctx.guiWidth() * Gx.S, ctx.guiHeight() * Gx.S);
               } finally {
                  Gx.end();
               }
            }

            if (Toasts.active()) {
               McBackend.begin(ctx);

               try {
                  Toasts.render(ctx.guiWidth() * Gx.S, ctx.guiHeight() * Gx.S);
               } finally {
                  Gx.end();
               }
            }

            if (Performance.monitor != null && Performance.monitor.enabled && !f1) {
               McBackend.begin(ctx);

               try {
                  Performance.monitor.draw(ctx.guiWidth() * Gx.S, ctx.guiHeight() * Gx.S);
               } finally {
                  Gx.end();
               }
            }

            if (Performance.moltron != null && Performance.moltron.enabled && !f1) {
               McBackend.begin(ctx);

               try {
                  Performance.moltron.drawHud(ctx.guiWidth() * Gx.S, ctx.guiHeight() * Gx.S);
               } finally {
                  Gx.end();
               }
            }

            if (mc.screen instanceof InventoryScreen && Utility.fightPreset != null) {
               McBackend.begin(ctx);

               try {
                  Utility.fightPreset.renderInventoryOverlay(ctx);
               } finally {
                  Gx.end();
               }
            }

            return;
         }

         McBackend.begin(ctx);

         try {
            Overlays.cutscene(ctx.guiWidth() * Gx.S, ctx.guiHeight() * Gx.S);
         } finally {
            Gx.end();
         }
      } catch (Throwable var72) {
         LOG("HUD-Fehler: " + var72);
         return;
      } finally {
         Perf.end("HUD");
      }
   }

   private static void tick(Minecraft client) {
      Perf.begin("Ticks");

      try {
         if (Mc.inGame() && joinedAt == 0L) {
            joinedAt = System.currentTimeMillis();
         }

         if (!Mc.inGame()) {
            joinedAt = 0L;
         }

         spotify.sync();

         try {
            HudPack.tick();
            UtilPack.tick();
         } catch (Throwable var12) {
            LOG("Paket-4-Tick: " + var12);
         }

         while (guiKey.consumeClick()) {
            if (client.screen == null) {
               LegoScreen.show(new MenuView());
            }
         }

         while (emoteKey.consumeClick()) {
            if (client.screen == null && Emotes.module.enabled && Mc.inGame()) {
               int code = -1;

               try {
                  code = KeyBindingHelper.getBoundKeyOf(emoteKey).getValue();
               } catch (Throwable var11) {
               }

               LegoScreen.show(new EmoteWheelView(code));
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
            if (client.screen == null) {
               LegoScreen.show(new HudEditorView());
            }
         }

         while (spotifyKey.consumeClick()) {
            if (client.screen == null) {
               LegoScreen.show(new SpotifyView());
            }
         }

         while (dcMuteKey.consumeClick()) {
            DiscordControl.toggleMute();
         }

         while (dcDeafKey.consumeClick()) {
            DiscordControl.toggleDeafen();
         }

         Visuals.zoom.held = zoomKey.isDown();
         if (client.screen == null) {
            for (Module m : Modules.ALL) {
               if (m.key >= 0 && m.key < lastDown.length) {
                  boolean down = Mc.keyDown(m.key);
                  if (down && !lastDown[m.key]) {
                     if (m.isToggleable()) {
                        m.toggle();
                        Modules.scheduleSave();
                        Toasts.show(m.icon, m.name, m.enabled ? "Aktiviert" : "Deaktiviert", m.enabled ? 0 : -7696487, 1400L);
                     } else if (!Utility.runAction(m) && m == Performance.memory) {
                        Performance.FreeMemory.run();
                     }
                  }

                  lastDown[m.key] = down;
               }
            }

            for (int i = 0; i < MACRO_KEYS.length; i++) {
               boolean down = Mc.keyDown(MACRO_KEYS[i]);
               if (down && !macroDown[i]) {
                  Utility.onKey(MACRO_KEYS[i]);
               }

               macroDown[i] = down;
            }
         }

         if (!Mc.inGame()) {
            if (Emotes.active()) {
               Emotes.stop();
            }
         } else {
            for (Module mx : Modules.ALL) {
               if (mx.enabled) {
                  try {
                     mx.tick();
                  } catch (Throwable var10) {
                     LOG("Tick " + mx.id + ": " + var10);
                  }
               }
            }

            Emotes.tick();
            CosRender.prewarm();
            PetWorld.tick();
            VehicleWorld.tick();
         }

         Modules.tickSave();
      } catch (Throwable var13) {
         LOG("Tick-Fehler: " + var13);
      } finally {
         Perf.end("Ticks");
      }
   }

   private static void drawSpeedo(int W, int H) {
      int kmh = VehicleWorld.kmh();
      int S = Gx.S;
      int w = 92 * S;
      int h = 30 * S;
      int x = W / 2 - w / 2;
      int y = H - 84 * S;
      Gx.rect(x, y, w, h, h / 2, -1072688874);
      Gx.outline(x, y, w, h, h / 2, Math.max(1, S / 2), 872415231);
      Gx.textCenter(kmh + " km/h", W / 2.0F, y + h / 2.0F - 2 * S, 10 * S, 3, -1);
      int bw = w - 24 * S;
      Gx.rect(x + 12 * S, y + h - 7 * S, bw, 3 * S, S, 872415231);
      Gx.rect(x + 12 * S, y + h - 7 * S, Math.max(S, Math.round(bw * Math.min(1.0F, kmh / 45.0F))), 3 * S, S, kmh > 35 ? -45730 : -14756000);
   }

   private interface BoolAction {
      boolean run();
   }
}
