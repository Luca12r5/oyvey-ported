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

   public static void LOG(String msg) {
      if (Boolean.getBoolean("legoclient.debug")) {
         System.out.println("[SpotifyHUD] " + msg);
      }
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
      Category cat = Category.register(Identifier.fromNamespaceAndPath("spotifyhud", "main"));
      openKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.spotifyhud.open", 72, cat));
      lyricsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.spotifyhud.lyrics", 75, cat));
      playKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.spotifyhud.playpause", -1, cat));
      nextKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.spotifyhud.next", -1, cat));
      prevKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.spotifyhud.previous", -1, cat));
      likeKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.spotifyhud.like", -1, cat));
      ClientTickEvents.END_CLIENT_TICK.register(SpotifyHudMod::onTick);
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("spotifyhud", "hud"), (ctx, tickCounter) -> renderHud(ctx));
      ScreenEvents.AFTER_INIT
         .register(
            (client, screen, w, h) -> {
               if (screen instanceof ChatScreen) {
                  ScreenMouseEvents.allowMouseClick(screen)
                     .register((s, click) -> !safe(() -> chatInteraction.mouseDown(click.x(), click.y(), click.button(), s.width, s.height)));
                  ScreenMouseEvents.allowMouseDrag(screen)
                     .register((s, click, dx, dy) -> !safe(() -> chatInteraction.mouseDrag(click.x(), click.y(), s.width, s.height)));
                  ScreenMouseEvents.allowMouseRelease(screen).register((s, click) -> !safe(chatInteraction::mouseUp));
                  ScreenMouseEvents.allowMouseScroll(screen).register((s, mx, my, hz, v) -> !safe(() -> chatInteraction.scroll(mx, my, v, s.width, s.height)));
                  ScreenEvents.afterRender(screen).register((s, ctx, mx, my, d) -> {
                     try {
                        chatInteraction.render(ctx, mx, my, ctx.guiWidth(), ctx.guiHeight());
                     } catch (Throwable var6) {
                        LOG("Chat-Renderfehler: " + var6);
                     }
                  });
                  ScreenEvents.remove(screen).register(s -> chatInteraction.mouseUp());
               }
            }
         );
      LOG("geladen");
   }

   public static boolean editorDown(double mx, double my, int b, int sw, int sh) {
      return chatInteraction != null && chatInteraction.mouseDown(mx, my, b, sw, sh);
   }

   public static boolean editorDrag(double mx, double my, int sw, int sh) {
      return chatInteraction != null && chatInteraction.mouseDrag(mx, my, sw, sh);
   }

   public static boolean editorUp() {
      return chatInteraction != null && chatInteraction.mouseUp();
   }

   public static boolean editorScroll(double mx, double my, double v, int sw, int sh) {
      return chatInteraction != null && chatInteraction.scroll(mx, my, v, sw, sh);
   }

   public static void editorRender(GuiGraphics ctx, int mx, int my, int sw, int sh) {
      if (chatInteraction != null) {
         chatInteraction.render(ctx, mx, my, sw, sh);
      }
   }

   public static boolean initialized() {
      return config != null;
   }

   private static boolean safe(SpotifyHudMod.BoolAction a) {
      try {
         return a.run();
      } catch (Throwable var2) {
         LOG("Eingabefehler: " + var2);
         return false;
      }
   }

   private static void onTick(Minecraft client) {
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
         if (!(client.screen instanceof SettingsScreen)) {
            client.setScreen(new SettingsScreen(config, media, hud));
         }
      }

      while (openKey.consumeClick()) {
         if (client.screen == null) {
            client.setScreen(new SettingsScreen(config, media, hud));
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

   static void copyToClipboard(String s) {
      try {
         Minecraft mc = Minecraft.getInstance();
         mc.execute(() -> {
            try {
               mc.keyboardHandler.setClipboard(s);
            } catch (Throwable var3) {
            }
         });
      } catch (Throwable var2) {
      }
   }

   static void onBridgeCommandFailed(String cmd, String err) {
      if (err != null && !err.isEmpty()) {
         LOG("Befehl '" + cmd + "' fehlgeschlagen: " + err);
      }

      media.onBridgeFailed(cmd);
   }

   private static void renderHud(GuiGraphics ctx) {
      try {
         if (!config.visible) {
            return;
         }

         Minecraft mc = Minecraft.getInstance();
         if (mc.options != null && mc.options.hideGui) {
            return;
         }

         if (mc.screen instanceof SettingsScreen) {
            return;
         }

         if (mc.screen instanceof ChatScreen) {
            return;
         }

         if (editorOpen || mc.screen != null && mc.screen.getClass().getName().endsWith("HudEditorScreen")) {
            return;
         }

         drawAll(ctx, ctx.guiWidth(), ctx.guiHeight(), false, null, false, -1.0, -1);
      } catch (Throwable var2) {
         LOG("HUD-Renderfehler: " + var2);
      }
   }

   static void drawAll(
      GuiGraphics ctx, int sw, int sh, boolean interactive, HudLayout.Region hover, boolean lyricsHover, double progressPreview, int volumePreview
   ) {
      if (config.visible) {
         PlayerState st = media.state();
         if (interactive || !config.hideWhenIdle || st.hasTrack) {
            int[] hb = hud.bounds(sw, sh, config);
            hud.render(ctx, hb[0], hb[1], hb[2], hb[3], buildView(interactive, hover, progressPreview, volumePreview));
            int[] lb = null;
            if (config.lyrics && st.hasTrack) {
               lyrics.update(st);
               lb = hud.lyricsBounds(hb, sw, sh, config);
               hud.renderLyrics(ctx, lb, lyrics.current(), st, config, interactive, lyricsHover);
            }

            hud.renderToast(ctx, hb, lb, sh, media.toast(), media.toastAt());
         }
      }
   }

   static HudPainter.View buildView(boolean interactive, HudLayout.Region hover, double progressPreview, int volumePreview) {
      HudPainter.View v = new HudPainter.View();
      PlayerState st = media.state();
      v.st = st;
      v.interactive = interactive;
      v.hover = hover;
      v.progressPreview = progressPreview;
      v.volumePreview = volumePreview;
      v.showControls = true;
      if (st.hasTrack) {
         v.title = st.title;
         v.artist = st.artist;
         v.showTime = st.durationMs > 0L;
         v.art = media.art();
      } else {
         v.title = "Kein Song erkannt";
         v.artist = smtc.fatal() != null && !api.isLoggedIn() ? "Erkennung nicht verfügbar - H drücken" : "Starte Spotify - wird automatisch erkannt";
         v.showTime = false;
         v.art = null;
      }

      return v;
   }

   private interface BoolAction {
      boolean run();
   }
}
