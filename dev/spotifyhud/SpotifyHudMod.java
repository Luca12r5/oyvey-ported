package dev.spotifyhud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.resources.Identifier;

public final class SpotifyHudMod implements ClientModInitializer {
   private static final int GLFW_KEY_H = 72;
   private static final int GLFW_KEY_K = 75;
   private static final int UNBOUND = -1;
   private static Config config;
   private static SpotifyApi api;
   private static SmtcBridge smtc;
   private static Media media;
   private static Lyrics lyrics;
   private static HudRenderer hud;
   private static HudInteraction chatInteraction;
   private static volatile long saveAt = 0L;
   private static volatile boolean openSettings = false;
   private static KeyMapping openKey;
   private static KeyMapping lyricsKey;
   private static KeyMapping playKey;
   private static KeyMapping nextKey;
   private static KeyMapping prevKey;
   private static KeyMapping likeKey;
   public static volatile boolean editorOpen = false;

   public static void LOG(String var0) {
      System.out.println("[SpotifyHUD] " + var0);
   }

   public static SpotifyApi api() {
      return api;
   }

   public static Media media() {
      return media;
   }

   public static Lyrics lyrics() {
      return lyrics;
   }

   public static Config config() {
      return config;
   }

   @Override
   public void onInitializeClient() {
      config = Config.load();
      api = new SpotifyApi(config);
      smtc = new SmtcBridge();
      media = new Media(smtc, api);
      lyrics = new Lyrics();
      hud = new HudRenderer();
      chatInteraction = new HudInteraction(config, hud);
      smtc.start();
      Category var1 = Category.register(Identifier.fromNamespaceAndPath("spotifyhud", "main"));
      openKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.spotifyhud.open", 72, var1));
      lyricsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.spotifyhud.lyrics", 75, var1));
      playKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.spotifyhud.playpause", -1, var1));
      nextKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.spotifyhud.next", -1, var1));
      prevKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.spotifyhud.previous", -1, var1));
      likeKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.spotifyhud.like", -1, var1));
      ClientTickEvents.END_CLIENT_TICK.register(SpotifyHudMod::onTick);
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("spotifyhud", "hud"), (var0, var1x) -> renderHud(var0));
      ScreenEvents.AFTER_INIT
         .register(
            (var0, var1x, var2, var3) -> {
               if (var1x instanceof ChatScreen) {
                  ScreenMouseEvents.allowMouseClick(var1x)
                     .register((var0x, var1xx) -> !safe(() -> chatInteraction.mouseDown(var1xx.x(), var1xx.y(), var1xx.button(), var0x.width, var0x.height)));
                  ScreenMouseEvents.allowMouseDrag(var1x)
                     .register((var0x, var1xx, var2x, var4) -> !safe(() -> chatInteraction.mouseDrag(var1xx.x(), var1xx.y(), var0x.width, var0x.height)));
                  ScreenMouseEvents.allowMouseRelease(var1x).register((var0x, var1xx) -> !safe(chatInteraction::mouseUp));
                  ScreenMouseEvents.allowMouseScroll(var1x)
                     .register((var0x, var1xx, var3x, var5, var7) -> !safe(() -> chatInteraction.scroll(var1xx, var3x, var7, var0x.width, var0x.height)));
                  ScreenEvents.afterRender(var1x).register((var0x, var1xx, var2x, var3x, var4) -> {
                     try {
                        chatInteraction.render(var1xx, var2x, var3x, var1xx.guiWidth(), var1xx.guiHeight());
                     } catch (Throwable var6) {
                        LOG("Chat-Renderfehler: " + var6);
                     }
                  });
                  ScreenEvents.remove(var1x).register(var0x -> chatInteraction.mouseUp());
               }
            }
         );
      LOG("geladen");
   }

   public static boolean editorDown(double var0, double var2, int var4, int var5, int var6) {
      return chatInteraction != null && chatInteraction.mouseDown(var0, var2, var4, var5, var6);
   }

   public static boolean editorDrag(double var0, double var2, int var4, int var5) {
      return chatInteraction != null && chatInteraction.mouseDrag(var0, var2, var4, var5);
   }

   public static boolean editorUp() {
      return chatInteraction != null && chatInteraction.mouseUp();
   }

   public static boolean editorScroll(double var0, double var2, double var4, int var6, int var7) {
      return chatInteraction != null && chatInteraction.scroll(var0, var2, var4, var6, var7);
   }

   public static void editorRender(GuiGraphics var0, int var1, int var2, int var3, int var4) {
      if (chatInteraction != null) {
         chatInteraction.render(var0, var1, var2, var3, var4);
      }
   }

   public static boolean initialized() {
      return config != null;
   }

   private static boolean safe(SpotifyHudMod.BoolAction var0) {
      try {
         return var0.run();
      } catch (Throwable var2) {
         LOG("Eingabefehler: " + var2);
         return false;
      }
   }

   private static void onTick(Minecraft var0) {
      media.tick();
      if (config.lyrics) {
         lyrics.update(media.state());
      }

      if (saveAt != 0L && System.currentTimeMillis() >= saveAt) {
         saveAt = 0L;
         config.save();
      }

      if (openSettings) {
         openSettings = false;
         if (!(var0.screen instanceof SettingsScreen)) {
            var0.setScreen(new SettingsScreen(config, media, hud));
         }
      }

      while (openKey.consumeClick()) {
         if (var0.screen == null) {
            var0.setScreen(new SettingsScreen(config, media, hud));
         }
      }

      while (lyricsKey.consumeClick()) {
         toggleLyrics();
      }

      while (playKey.consumeClick()) {
         media.togglePlay();
      }

      while (nextKey.consumeClick()) {
         media.next();
      }

      while (prevKey.consumeClick()) {
         media.previous();
      }

      while (likeKey.consumeClick()) {
         media.toggleLike();
      }
   }

   public static void requestSettings() {
      openSettings = true;
   }

   public static void toggleLyrics() {
      config.lyrics = !config.lyrics;
      config.save();
      media.toast(config.lyrics ? "Lyrics an" : "Lyrics aus");
   }

   static boolean lyricsVisible() {
      return config.lyrics && media.state().hasTrack;
   }

   static void scheduleSave() {
      saveAt = System.currentTimeMillis() + 800L;
   }

   static void copyToClipboard(String var0) {
      try {
         Minecraft var1 = Minecraft.getInstance();
         var1.execute(() -> {
            try {
               var1.keyboardHandler.setClipboard(var0);
            } catch (Throwable var3) {
            }
         });
      } catch (Throwable var2) {
      }
   }

   static void onBridgeCommandFailed(String var0, String var1) {
      if (var1 != null && !var1.isEmpty()) {
         LOG("Befehl '" + var0 + "' fehlgeschlagen: " + var1);
      }

      media.onBridgeFailed(var0);
   }

   private static void renderHud(GuiGraphics var0) {
      try {
         if (!config.visible) {
            return;
         }

         Minecraft var1 = Minecraft.getInstance();
         if (var1.options != null && var1.options.hideGui) {
            return;
         }

         if (var1.screen instanceof SettingsScreen) {
            return;
         }

         if (var1.screen instanceof ChatScreen) {
            return;
         }

         if (editorOpen || var1.screen != null && var1.screen.getClass().getName().endsWith("HudEditorScreen")) {
            return;
         }

         drawAll(var0, var0.guiWidth(), var0.guiHeight(), false, null, false, -1.0, -1);
      } catch (Throwable var2) {
         LOG("HUD-Renderfehler: " + var2);
      }
   }

   static void drawAll(GuiGraphics var0, int var1, int var2, boolean var3, HudLayout.Region var4, boolean var5, double var6, int var8) {
      if (config.visible) {
         PlayerState var9 = media.state();
         if (var3 || !config.hideWhenIdle || var9.hasTrack) {
            int[] var10 = hud.bounds(var1, var2, config);
            hud.render(var0, var10[0], var10[1], var10[2], var10[3], buildView(var3, var4, var6, var8));
            int[] var11 = null;
            if (config.lyrics && var9.hasTrack) {
               lyrics.update(var9);
               var11 = hud.lyricsBounds(var10, var1, var2, config);
               hud.renderLyrics(var0, var11, lyrics.current(), var9, config, var3, var5);
            }

            hud.renderToast(var0, var10, var11, var2, media.toast(), media.toastAt());
         }
      }
   }

   static HudPainter.View buildView(boolean var0, HudLayout.Region var1, double var2, int var4) {
      HudPainter.View var5 = new HudPainter.View();
      PlayerState var6 = media.state();
      var5.st = var6;
      var5.interactive = var0;
      var5.hover = var1;
      var5.progressPreview = var2;
      var5.volumePreview = var4;
      var5.showControls = true;
      if (var6.hasTrack) {
         var5.title = var6.title;
         var5.artist = var6.artist;
         var5.showTime = var6.durationMs > 0L;
         var5.art = media.art();
      } else {
         var5.title = "Kein Song erkannt";
         var5.artist = smtc.fatal() != null && !api.isLoggedIn() ? "Erkennung nicht verfügbar - H drücken" : "Starte Spotify - wird automatisch erkannt";
         var5.showTime = false;
         var5.art = null;
      }

      return var5;
   }

   private interface BoolAction {
      boolean run();
   }
}
