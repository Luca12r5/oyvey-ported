package dev.lego;

import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.cosmetic.Cos;
import dev.lego.cosmetic.CosRender;
import dev.lego.emote.EmoteLib;
import dev.lego.emote.EmoteWheelView;
import dev.lego.emote.Emotes;
import dev.lego.games.Arcade;
import dev.lego.games.GameInfo;
import dev.lego.hud.HudEditorView;
import dev.lego.hud.HudManager;
import dev.lego.hud.HudModules;
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
import dev.lego.visual.Visuals3;
import dev.lego.visual.Visuals;
import dev.lego.visual.Visuals2;
import dev.lego.visual.sky.SkyPacks;
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
import net.minecraft.class_1007;
import net.minecraft.class_1269;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3264;
import net.minecraft.class_3300;
import net.minecraft.class_332;
import net.minecraft.class_3883;
import net.minecraft.class_408;

/** Lego Client 2.0 - entry point (Fabric, Minecraft 1.21.11). */
public final class LegoClient implements ClientModInitializer {
    public static final boolean DEBUG = Boolean.getBoolean("legoclient.debug");
    public static void LOG(String msg) { if (DEBUG) System.out.println("[LegoClient] " + msg); }

    private static class_304 guiKey, zoomKey, emoteKey, invseeKey, ecseeKey, waypointKey, hudKey, spotifyKey, dcMuteKey, dcDeafKey;
    private static SpotifyModule spotify;
    private static long joinedAt = 0;
    private static boolean introShown = false;
    private static final boolean[] lastDown = new boolean[512];
    private static final int[] MACRO_KEYS = {295, 296, 297, 298, 321, 322, 323, 324, 325, 326, 327, 328, 329};
    private static final boolean[] macroDown = new boolean[MACRO_KEYS.length];

    public static long sessionSeconds() { return joinedAt == 0 ? 0 : (System.currentTimeMillis() - joinedAt) / 1000; }

    @Override
    public void onInitializeClient() {
        // Spotify HUD (built in) - skipped if the standalone Spotify HUD mod is installed too
        boolean standalone = FabricLoader.getInstance().isModLoaded("spotifyhud");
        if (!standalone) {
            try { new dev.spotifyhud.SpotifyHudMod().onInitializeClient(); } catch (Throwable t) { LOG("Spotify HUD Start fehlgeschlagen: " + t); }
        } else {
            LOG("Separater Spotify HUD Mod gefunden - eingebaute Version wird nicht gestartet.");
        }

        UiSettings.register();
        HudModules.registerAll();
        dev.lego.hud.HudModules2.registerAll();
        dev.lego.hud.HudPack.registerAll();
        Visuals.registerAll();
        Utility.registerAll();
        dev.lego.util.UtilPack.registerAll();
        Performance.registerAll();
        Emotes.register();
        spotify = Modules.register(new SpotifyModule());
        Cos.register();
        EmoteLib.registerPersist();
        Arcade.registerAll();
        GameInfo.registerPersist();
        Modules.register(new dev.lego.discord.DiscordControl());
        dev.lego.net.LegoSync.register();
        Modules.load();
        spotify.sync();

        class_304.class_11900 cat = class_304.class_11900.method_74698(class_2960.method_60655("legoclient", "main"));
        guiKey = KeyBindingHelper.registerKeyBinding(new class_304("key.legoclient.gui", 344, cat));        // Right Shift
        zoomKey = KeyBindingHelper.registerKeyBinding(new class_304("key.legoclient.zoom", 67, cat));       // C
        emoteKey = KeyBindingHelper.registerKeyBinding(new class_304("key.legoclient.emotes", 66, cat));    // B
        invseeKey = KeyBindingHelper.registerKeyBinding(new class_304("key.legoclient.invsee", 86, cat));   // V
        ecseeKey = KeyBindingHelper.registerKeyBinding(new class_304("key.legoclient.ecsee", 71, cat));     // G
        waypointKey = KeyBindingHelper.registerKeyBinding(new class_304("key.legoclient.waypoint", 78, cat)); // N
        hudKey = KeyBindingHelper.registerKeyBinding(new class_304("key.legoclient.hud", -1, cat));        // unbound
        spotifyKey = KeyBindingHelper.registerKeyBinding(new class_304("key.legoclient.spotifymenu", 85, cat)); // U: Spotify search + playlists
        dcMuteKey = KeyBindingHelper.registerKeyBinding(new class_304("key.legoclient.discordmute", -1, cat));
        dcDeafKey = KeyBindingHelper.registerKeyBinding(new class_304("key.legoclient.discorddeafen", -1, cat));

        setupEnv();

        ClientTickEvents.END_CLIENT_TICK.register(LegoClient::tick);
        // brick start animation, once, on the first title screen
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (introShown || !dev.lego.ui.UiSettings.intro || !(screen instanceof net.minecraft.class_442)) return;
            // JVM-wide flag: plays once per game start, even if two copies of the client are installed
            if (System.getProperty("legoclient.introPlayed") != null) { introShown = true; return; }
            System.setProperty("legoclient.introPlayed", "1");
            introShown = true;
            client.execute(() -> client.method_1507(new LegoScreen(new dev.lego.ui.IntroView(), screen)));
        });
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!(screen instanceof class_408)) return;
            ScreenMouseEvents.allowMouseClick(screen).register((s, click) -> {
                try { return !dev.lego.discord.DiscordHud.click(click.comp_4798(), click.comp_4799(), click.method_74245()); } catch (Throwable ignored) { return true; }
            });
        });

        // HUD: overlays under the vanilla HUD, modules + toasts on top
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, class_2960.method_60655("legoclient", "overlays"), (ctx, tc) -> renderOverlays(ctx));
        HudElementRegistry.addLast(class_2960.method_60655("legoclient", "hud"), (ctx, tc) -> renderHud(ctx));
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, old -> (ctx, tc) -> {
            if (dev.lego.emote.Cutscenes.active()) return;
            if (Visuals.crosshair != null && Visuals.crosshair.enabled && Mc.firstPerson() && !Mc.mc().field_1690.field_1842) {
                try { Visuals.crosshair.draw(ctx); } catch (Throwable t) { old.render(ctx, tc); }
            } else old.render(ctx, tc);
        });

        WorldRenderEvents.BEFORE_TRANSLUCENT.register(Visuals::render);
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((ctx, hit) -> !(Visuals2.outline != null && Visuals2.outline.enabled && Visuals2.outline.replaceVanilla()));

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            try { if (player == Mc.player()) Visuals.onAttack(entity); } catch (Throwable ignored) { }
            return class_1269.field_5811;
        });

        // chat: filters + mention ping
        ClientReceiveMessageEvents.ALLOW_CHAT.register((msg, signed, sender, params, ts) -> Utility.allowMessage(msg, false));
        ClientReceiveMessageEvents.ALLOW_GAME.register((msg, overlay) -> Utility.allowMessage(msg, overlay));
        ClientReceiveMessageEvents.CHAT.register((msg, signed, sender, params, ts) -> Utility.onMessage(msg, false));
        ClientReceiveMessageEvents.GAME.register((msg, overlay) -> Utility.onMessage(msg, overlay));

        // cosmetics on your own player
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, ctx) -> {
            if (renderer instanceof class_1007) {
                try { helper.register(CosRender.feature((class_3883) renderer)); } catch (Throwable t) { LOG("Kosmetik-Renderer: " + t); }
            }
        });

        // sky packs: re-read after resource pack reload
        ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override public class_2960 getFabricId() { return class_2960.method_60655("legoclient", "skypacks"); }
            @Override public void method_14491(class_3300 manager) { SkyPacks.invalidate(); }
        });

        // chat: drag the HUD modules (quick edit)
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!(screen instanceof class_408)) return;
            ScreenMouseEvents.allowMouseClick(screen).register((s, c) ->
                    !safe(() -> HudManager.mouseDown(c.comp_4798(), c.comp_4799(), c.method_74245(), s.field_22789, s.field_22790)));
            ScreenMouseEvents.allowMouseDrag(screen).register((s, c, dx, dy) ->
                    !safe(() -> HudManager.mouseDrag(c.comp_4798(), c.comp_4799(), s.field_22789, s.field_22790, true)));
            ScreenMouseEvents.allowMouseRelease(screen).register((s, c) -> !safe(HudManager::mouseUp));
            ScreenMouseEvents.allowMouseScroll(screen).register((s, mx, my, hz, v) ->
                    !safe(() -> HudManager.scroll(mx, my, v, s.field_22789, s.field_22790)));
            ScreenEvents.afterRender(screen).register((s, ctx, mx, my, d) -> {
                try { HudManager.render(ctx, ctx.method_51421(), ctx.method_51443(), true, mx, my); } catch (Throwable t) { LOG("Chat-HUD: " + t); }
            });
            ScreenEvents.remove(screen).register(s -> HudManager.mouseUp());
        });
        LOG("geladen - " + Modules.ALL.size() + " Module, " + Cos.ALL.size() + " Kosmetik-Items, " + EmoteLib.ALL.size() + " Emotes");
    }

    private static void setupEnv() {
        Env.version = "3.4.0";
        Env.playerName = () -> {
            try {
                if (Mc.player() != null) return Mc.name(Mc.player());
                return Mc.mc().method_1548().method_1676();
            } catch (Throwable t) { return "Spieler"; }
        };
        Env.inWorld = Mc::inGame;
        Env.clipboard = () -> {
            try { String s = Mc.mc().field_1774.method_1460(); return s == null ? "" : s; } catch (Throwable t) { return ""; }
        };
        Env.nowPlaying = () -> {
            try {
                dev.spotifyhud.PlayerState st = null;
                if (dev.spotifyhud.SpotifyHudMod.api() != null) st = dev.spotifyhud.SpotifyHudMod.api().state();
                if ((st == null || !st.hasTrack) && dev.spotifyhud.SpotifyHudMod.media() != null) st = dev.spotifyhud.SpotifyHudMod.media().state();
                if (st == null || !st.hasTrack || st.title == null) return "";
                return st.title + (st.artist == null || st.artist.isEmpty() ? "" : " · " + st.artist);
            } catch (Throwable t) { return ""; }
        };
        Env.loading = () -> { try { return Mc.mc().method_18506() != null; } catch (Throwable e) { return false; } };
        Env.openHudEditor = () -> LegoScreen.show(new HudEditorView());
        Env.copy = Mc::clipboard;
        Env.playEmote = Emotes::play;
        Env.playerPreview = CosRender::preview;
    }

    private interface BoolAction { boolean run(); }

    private static boolean safe(BoolAction a) {
        try { return a.run(); } catch (Throwable t) { LOG("Eingabe: " + t); return false; }
    }

    private static void renderOverlays(class_332 ctx) {
        try {
            if (Mc.mc().field_1690 != null && Mc.mc().field_1690.field_1842) return;         // F1
            McBackend.begin(ctx);
            try {
                int ow = ctx.method_51421() * Gx.S, oh = ctx.method_51443() * Gx.S;
                Overlays.render(ow, oh);
                Visuals2.renderHud(ow, oh);
                Visuals3.renderHud(ow, oh);
                if (dev.lego.util.Utility.auction != null) dev.lego.util.Utility.auction.renderHud(ow, oh);
            } finally { Gx.end(); }
        } catch (Throwable t) {
            LOG("Overlay-Fehler: " + t);
        }
    }

    private static void renderHud(class_332 ctx) {
        dev.lego.perf.Perf.begin("HUD");
        try {
            class_310 mc = Mc.mc();
            HudModules.ClickCounter.frame();
            if (dev.lego.emote.Cutscenes.active()) {
                McBackend.begin(ctx);
                try { Overlays.cutscene(ctx.method_51421() * Gx.S, ctx.method_51443() * Gx.S); } finally { Gx.end(); }
                return;
            }
            boolean f1 = mc.field_1690 != null && mc.field_1690.field_1842;
            boolean editor = mc.field_1755 instanceof LegoScreen && ((LegoScreen) mc.field_1755).view() instanceof HudEditorView;
            if (!f1 && !(mc.field_1755 instanceof class_408) && !editor && !dev.lego.emote.Cutscenes.active())
                HudManager.render(ctx, ctx.method_51421(), ctx.method_51443(), false, -1, -1);
            // speedometer removed
            if (!f1 && dev.lego.discord.DiscordControl.hudOn()) {
                McBackend.begin(ctx);
                try { dev.lego.discord.DiscordHud.render(ctx.method_51421() * Gx.S, ctx.method_51443() * Gx.S); } finally { Gx.end(); }
            }
            if (Toasts.active()) {
                McBackend.begin(ctx);
                try { Toasts.render(ctx.method_51421() * Gx.S, ctx.method_51443() * Gx.S); } finally { Gx.end(); }
            }
            if (Performance.monitor != null && Performance.monitor.enabled && !f1) {
                McBackend.begin(ctx);
                try { Performance.monitor.draw(ctx.method_51421() * Gx.S, ctx.method_51443() * Gx.S); } finally { Gx.end(); }
            }
            if (Performance.moltron != null && Performance.moltron.enabled && !f1) {
                McBackend.begin(ctx);
                try { Performance.moltron.drawHud(ctx.method_51421() * Gx.S, ctx.method_51443() * Gx.S); } finally { Gx.end(); }
            }
            if (mc.field_1755 instanceof net.minecraft.class_490 && dev.lego.util.Utility.fightPreset != null) {
                McBackend.begin(ctx);
                try { dev.lego.util.Utility.fightPreset.renderInventoryOverlay(ctx); } finally { Gx.end(); }
            }
        } catch (Throwable t) {
            LOG("HUD-Fehler: " + t);
        } finally {
            dev.lego.perf.Perf.end("HUD");
        }
    }

    private static void tick(class_310 client) {
        dev.lego.perf.Perf.begin("Ticks");
        try {
            if (Mc.inGame() && joinedAt == 0) joinedAt = System.currentTimeMillis();
            if (!Mc.inGame()) joinedAt = 0;
            spotify.sync();
            try { dev.lego.hud.HudPack.tick(); dev.lego.util.UtilPack.tick(); } catch (Throwable t) { LOG("Paket-4-Tick: " + t); }

            while (guiKey.method_1436()) if (client.field_1755 == null) LegoScreen.show(new MenuView());
            while (emoteKey.method_1436()) {
                if (client.field_1755 == null && Emotes.module.enabled && Mc.inGame()) {
                    int code = -1;
                    try { code = KeyBindingHelper.getBoundKeyOf(emoteKey).method_1444(); } catch (Throwable ignored) { }
                    LegoScreen.show(new EmoteWheelView(code));
                }
            }
            while (invseeKey.method_1436()) Utility.invsee.use();
            while (ecseeKey.method_1436()) Utility.ecsee.use();
            while (waypointKey.method_1436()) if (Utility.waypoints.enabled) Utility.Waypoints.addHere();
            while (hudKey.method_1436()) if (client.field_1755 == null) LegoScreen.show(new HudEditorView());
            while (spotifyKey.method_1436()) if (client.field_1755 == null) LegoScreen.show(new dev.lego.ui.SpotifyView());
            while (dcMuteKey.method_1436()) dev.lego.discord.DiscordControl.toggleMute();
            while (dcDeafKey.method_1436()) dev.lego.discord.DiscordControl.toggleDeafen();
            Visuals.zoom.held = zoomKey.method_1434();

            // module key binds + macro keys (only while playing)
            if (client.field_1755 == null) {
                for (Module m : Modules.ALL) {
                    if (m.key < 0 || m.key >= lastDown.length) continue;
                    boolean down = Mc.keyDown(m.key);
                    if (down && !lastDown[m.key]) {
                        if (m.isToggleable()) {
                            m.toggle();
                            Modules.scheduleSave();
                            Toasts.show(m.icon, m.name, m.enabled ? "Aktiviert" : "Deaktiviert", m.enabled ? 0 : 0xFF8A8F99, 1400);
                        } else if (!Utility.runAction(m) && m == Performance.memory) Performance.FreeMemory.run();
                    }
                    lastDown[m.key] = down;
                }
                for (int i = 0; i < MACRO_KEYS.length; i++) {
                    boolean down = Mc.keyDown(MACRO_KEYS[i]);
                    if (down && !macroDown[i]) Utility.onKey(MACRO_KEYS[i]);
                    macroDown[i] = down;
                }
            }

            if (Mc.inGame()) {
                for (Module m : Modules.ALL) {
                    if (!m.enabled) continue;
                    try { m.tick(); } catch (Throwable t) { LOG("Tick " + m.id + ": " + t); }
                }
                Emotes.tick();
                CosRender.prewarm();
                dev.lego.cosmetic.PetWorld.tick();
                dev.lego.cosmetic.VehicleWorld.tick();
            } else if (Emotes.active()) {
                Emotes.stop();
            }
            Modules.tickSave();
        } catch (Throwable t) {
            LOG("Tick-Fehler: " + t);
        } finally {
            dev.lego.perf.Perf.end("Ticks");
        }
    }

    /** Speedometer at the bottom centre while you ride a vehicle (real km/h). */
    private static void drawSpeedo(int W, int H) {
        int kmh = dev.lego.cosmetic.VehicleWorld.kmh();
        int S = Gx.S;
        int w = 92 * S, h = 30 * S, x = W / 2 - w / 2, y = H - 84 * S;
        Gx.rect(x, y, w, h, h / 2, 0xC0101116);
        Gx.outline(x, y, w, h, h / 2, Math.max(1, S / 2), 0x33FFFFFF);
        Gx.textCenter(kmh + " km/h", W / 2f, y + h / 2f - 2 * S, 10 * S, dev.lego.ui.Fonts.BOLD, 0xFFFFFFFF);
        int bw = w - 24 * S;
        Gx.rect(x + 12 * S, y + h - 7 * S, bw, 3 * S, S, 0x33FFFFFF);
        Gx.rect(x + 12 * S, y + h - 7 * S, Math.max(S, Math.round(bw * Math.min(1f, kmh / 45f))), 3 * S, S, kmh > 35 ? 0xFFFF4D5E : 0xFF1ED760);
    }
}