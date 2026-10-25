// Minecraft 26.3 version of dev/visor/VisorClient.java (SDL keys, LevelRenderEvents, render layer callback).
package dev.visor;

import dev.visor.browser.BrowserSession;
import dev.visor.browser.BrowserShortcuts;
import dev.visor.donut.DrDonutt;
import dev.visor.headset.HeadsetLayer;
import dev.visor.module.ModuleManager;
import dev.visor.render.UiResources;
import dev.visor.render.VisorPipelines;
import dev.visor.setting.SettingsStore;
import dev.visor.setting.VisorSettings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VisorClient implements ClientModInitializer {
	public static final String MOD_ID = "visor";
	public static final Logger LOGGER = LoggerFactory.getLogger("Visor");
	public static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(id("main"));

	public static KeyMapping toggleKey;

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitializeClient() {
		VisorPipelines.init();
		VisorSettings.init();
		ModuleManager.init();
		BrowserShortcuts.init();
		// GLFW right shift (344), translated to the SDL key of 26.3.
		toggleKey = dev.lego.compat.McCompat.registerKey(dev.lego.compat.McCompat.keyMapping("key.visor.toggle", 344, KEY_CATEGORY));
		ResourceLoader.get(PackType.CLIENT_RESOURCES)
			.registerReloadListener(id("ui_resources"), (ResourceManagerReloadListener) UiResources::reload);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			VisorController controller = VisorController.get();
			if (!controller.capturesKeyboard()) {
				while (toggleKey.consumeClick()) {
					controller.toggle();
				}
			}
			ModuleManager.tickKeybinds(client, controller.capturesKeyboard());
			DrDonutt.get().tick(client);
			SettingsStore.tick();
			BrowserSession.get().tick();
		});
		// In real VR (Vivecraft) the world is drawn per eye by Vivecraft: no own world rendering there.
		LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(context -> {
			if (!dev.lego.compat.VrMixinPlugin.vr()) DrDonutt.get().renderWorld();
		});
		// The headset browser belongs to the world: leaving it (or quitting) closes the browser.
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(() -> VisorController.get().closeBrowser(false)));
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			SettingsStore.saveNow();
			BrowserSession.get().shutdown();
		});
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
			if (renderer instanceof AvatarRenderer<?> avatar && !dev.lego.compat.VrMixinPlugin.vr()) {
				helper.register(new HeadsetLayer(avatar));
			}
		});
	}
}
