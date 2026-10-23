package dev.lego.compat;

import dev.lego.LegoClient;
import dev.lego.core.Mc;
import dev.lego.cosmetic.CosRender;
import dev.lego.emote.Cutscenes;
import dev.lego.visual.Visuals;
import dev.lego.visual.Visuals2;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;

/** HUD, world and entity-layer hooks for 26.3 (extract/submit render API). */
public final class RenderHooks {
   private RenderHooks() {
   }

   public static void register(Consumer<GuiGraphicsExtractor> overlays, Consumer<GuiGraphicsExtractor> hud) {
      HudElementRegistry.attachElementAfter(
         VanillaHudElements.MISC_OVERLAYS, Identifier.fromNamespaceAndPath("legoclient", "overlays"), (g, delta) -> {
            if (!VrMixinPlugin.vr()) overlays.accept(g);
         }
      );
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("legoclient", "hud"), (g, delta) -> hud.accept(g));
      HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, vanilla -> (g, delta) -> {
         if (Cutscenes.active()) return;
         if (!VrMixinPlugin.vr() && Visuals.crosshair != null && Visuals.crosshair.enabled && Mc.firstPerson() && !Mc.mc().gui.hud.isHidden()) {
            try {
               Visuals.crosshair.draw(g);
               return;
            } catch (Throwable ignored) {
            }
         }
         vanilla.extractRenderState(g, delta);
      });
      // Vivecraft draws the world per eye with its own cameras: no own world effects there.
      LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(ctx -> {
         if (!VrMixinPlugin.vr()) Visuals.render(ctx);
      });
      LevelRenderEvents.BEFORE_BLOCK_OUTLINE
         .register((ctx, outline) -> Visuals2.outline == null || !Visuals2.outline.enabled || !Visuals2.outline.replaceVanilla());
      LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
         if (renderer instanceof AvatarRenderer) {
            try {
               helper.register(CosRender.feature(renderer));
            } catch (Throwable e) {
               LegoClient.LOG("Kosmetik-Renderer: " + e);
            }
         }
      });
   }
}
