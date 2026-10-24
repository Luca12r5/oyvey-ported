package dev.spotifyhud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_408;

/**
 * Spotify HUD for Minecraft 1.21.11 (Fabric).
 * class_304 KeyBinding, class_304$class_11900 KeyBinding.Category, class_2960 Identifier,
 * class_310 MinecraftClient, class_408 ChatScreen
 */
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
    private static volatile long saveAt = 0;
    private static volatile boolean openSettings = false;

    private static class_304 openKey, lyricsKey, playKey, nextKey, prevKey, likeKey;

    public static void LOG(String msg) {
        if (Boolean.getBoolean("legoclient.debug")) System.out.println("[SpotifyHUD] " + msg);
    }

    public static SpotifyApi api() { return api; }
    public static Media media() { return media; }
    public static Lyrics lyrics() { return lyrics; }
    public static Config config() { return config; }

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

        class_304.class_11900 cat = class_304.class_11900.method_74698(class_2960.method_60655("spotifyhud", "main"));
        openKey = KeyBindingHelper.registerKeyBinding(new class_304("key.spotifyhud.open", GLFW_KEY_H, cat));
        lyricsKey = KeyBindingHelper.registerKeyBinding(new class_304("key.spotifyhud.lyrics", GLFW_KEY_K, cat));
        playKey = KeyBindingHelper.registerKeyBinding(new class_304("key.spotifyhud.playpause", UNBOUND, cat));
        nextKey = KeyBindingHelper.registerKeyBinding(new class_304("key.spotifyhud.next", UNBOUND, cat));
        prevKey = KeyBindingHelper.registerKeyBinding(new class_304("key.spotifyhud.previous", UNBOUND, cat));
        likeKey = KeyBindingHelper.registerKeyBinding(new class_304("key.spotifyhud.like", UNBOUND, cat));

        ClientTickEvents.END_CLIENT_TICK.register(SpotifyHudMod::onTick);
        HudElementRegistry.addLast(class_2960.method_60655("spotifyhud", "hud"), (ctx, tickCounter) -> renderHud(ctx));

        // Chat: HUD can be clicked, dragged and resized with the mouse wheel
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!(screen instanceof class_408)) return;
            ScreenMouseEvents.allowMouseClick(screen).register((s, click) ->
                    !safe(() -> chatInteraction.mouseDown(click.comp_4798(), click.comp_4799(), click.method_74245(), s.field_22789, s.field_22790)));
            ScreenMouseEvents.allowMouseDrag(screen).register((s, click, dx, dy) ->
                    !safe(() -> chatInteraction.mouseDrag(click.comp_4798(), click.comp_4799(), s.field_22789, s.field_22790)));
            ScreenMouseEvents.allowMouseRelease(screen).register((s, click) -> !safe(chatInteraction::mouseUp));
            ScreenMouseEvents.allowMouseScroll(screen).register((s, mx, my, hz, v) ->
                    !safe(() -> chatInteraction.scroll(mx, my, v, s.field_22789, s.field_22790)));
            ScreenEvents.afterRender(screen).register((s, ctx, mx, my, d) -> {
                try { chatInteraction.render(ctx, mx, my, ctx.method_51421(), ctx.method_51443()); }
                catch (Throwable t) { LOG("Chat-Renderfehler: " + t); }
            });
            ScreenEvents.remove(screen).register(s -> chatInteraction.mouseUp());
        });
        LOG("geladen");
    }

    // ------------------------------------------------------------------ used by the Lego client HUD editor
    public static boolean editorDown(double mx, double my, int b, int sw, int sh) { return chatInteraction != null && chatInteraction.mouseDown(mx, my, b, sw, sh); }
    public static boolean editorDrag(double mx, double my, int sw, int sh) { return chatInteraction != null && chatInteraction.mouseDrag(mx, my, sw, sh); }
    public static boolean editorUp() { return chatInteraction != null && chatInteraction.mouseUp(); }
    public static boolean editorScroll(double mx, double my, double v, int sw, int sh) { return chatInteraction != null && chatInteraction.scroll(mx, my, v, sw, sh); }
    public static void editorRender(class_332 ctx, int mx, int my, int sw, int sh) { if (chatInteraction != null) chatInteraction.render(ctx, mx, my, sw, sh); }
    public static boolean initialized() { return config != null; }
    /** Set by the Lego client HUD editor (it renders the HUD itself). */
    public static volatile boolean editorOpen = false;

    private interface BoolAction { boolean run(); }

    private static boolean safe(BoolAction a) {
        try { return a.run(); } catch (Throwable t) { LOG("Eingabefehler: " + t); return false; }
    }

    private static void onTick(class_310 client) {
        media.tick();
        if (config.lyrics) lyrics.update(media.state());
        if (saveAt != 0 && System.currentTimeMillis() >= saveAt) { saveAt = 0; config.save(); }
        if (openSettings) {
            openSettings = false;
            if (!(client.field_1755 instanceof SettingsScreen)) client.method_1507(new SettingsScreen(config, media, hud));
        }
        while (openKey.method_1436()) {
            if (client.field_1755 == null) client.method_1507(new SettingsScreen(config, media, hud));
        }
        while (lyricsKey.method_1436()) toggleLyrics();
        while (playKey.method_1436()) media.togglePlay();
        while (nextKey.method_1436()) media.next();
        while (prevKey.method_1436()) media.previous();
        while (likeKey.method_1436()) media.toggleLike();
    }

    /** Opens the settings on the next tick (safe to call from inside input handlers). */
    public static void requestSettings() { openSettings = true; }

    public static void toggleLyrics() {
        config.lyrics = !config.lyrics;
        config.save();
        media.toast(config.lyrics ? "Lyrics an" : "Lyrics aus");
    }

    static boolean lyricsVisible() {
        return config.lyrics && media.state().hasTrack;
    }

    /** Debounced config save (used while resizing with the mouse wheel). */
    static void scheduleSave() { saveAt = System.currentTimeMillis() + 800; }

    static void copyToClipboard(String s) {
        try {
            class_310 mc = class_310.method_1551();
            mc.execute(() -> {
                try { mc.field_1774.method_1455(s); } catch (Throwable ignored) { }
            });
        } catch (Throwable ignored) { }
    }

    static void onBridgeCommandFailed(String cmd, String err) {
        if (err != null && !err.isEmpty()) LOG("Befehl '" + cmd + "' fehlgeschlagen: " + err);
        media.onBridgeFailed(cmd);
    }

    private static void renderHud(class_332 ctx) {
        try {
            if (!config.visible) return;
            class_310 mc = class_310.method_1551();
            if (mc.field_1690 != null && mc.field_1690.field_1842) return;        // F1
            if (mc.field_1755 instanceof SettingsScreen) return;                   // screen draws its own copy
            if (mc.field_1755 instanceof class_408) return;                       // chat draws the interactive copy
            if (editorOpen || (mc.field_1755 != null && mc.field_1755.getClass().getName().endsWith("HudEditorScreen"))) return;
            drawAll(ctx, ctx.method_51421(), ctx.method_51443(), false, null, false, -1, -1);
        } catch (Throwable t) {
            LOG("HUD-Renderfehler: " + t);
        }
    }

    /** Draws HUD, lyrics and the toast message. */
    static void drawAll(class_332 ctx, int sw, int sh, boolean interactive, HudLayout.Region hover, boolean lyricsHover,
                        double progressPreview, int volumePreview) {
        if (!config.visible) return;
        PlayerState st = media.state();
        if (!interactive && config.hideWhenIdle && !st.hasTrack) return;
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
            v.showTime = st.durationMs > 0;
            v.art = media.art();
        } else {
            v.title = "Kein Song erkannt";
            v.artist = smtc.fatal() != null && !api.isLoggedIn() ? "Erkennung nicht verfügbar - H drücken"
                    : "Starte Spotify - wird automatisch erkannt";
            v.showTime = false;
            v.art = null;
        }
        return v;
    }
}
