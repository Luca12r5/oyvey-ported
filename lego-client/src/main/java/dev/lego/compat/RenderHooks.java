package dev.lego.compat;

import dev.lego.LegoClient;
import dev.lego.core.Mc;
import dev.lego.cosmetic.CosRender;
import dev.lego.emote.Cutscenes;
import dev.lego.visual.Visuals;
import dev.lego.visual.Visuals2;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;

/** HUD, world and entity-layer hooks (1.21.11). lego-client-26 has its own copy for 26.3's render API. */
public final class RenderHooks {
   private RenderHooks() {
   }

   public static void register(Consumer<GuiGraphics> overlays, Consumer<GuiGraphics> hud) {
      HudElementRegistry.attachElementAfter(
         VanillaHudElements.MISC_OVERLAYS, Identifier.fromNamespaceAndPath("legoclient", "overlays"), (var0, var1x) -> {
            // Full-screen overlays make no sense on a VR GUI plane (Vivecraft).
            if (!dev.lego.compat.VrMixinPlugin.vr()) overlays.accept(var0);
         }
      );
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("legoclient", "hud"), (var0, var1x) -> hud.accept(var0));
      HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, var0 -> (var1x, var2x) -> {
         if (!Cutscenes.active()) {
            if (!dev.lego.compat.VrMixinPlugin.vr() && Visuals.crosshair != null && Visuals.crosshair.enabled && Mc.firstPerson() && !Mc.mc().options.hideGui) {
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
      // In real VR Vivecraft draws the world once per eye with its own cameras: no own world effects there.
      WorldRenderEvents.BEFORE_TRANSLUCENT.register(var0 -> {
         if (!dev.lego.compat.VrMixinPlugin.vr()) Visuals.render(var0);
      });
      WorldRenderEvents.BEFORE_BLOCK_OUTLINE
         .register((var0, var1x) -> Visuals2.outline == null || !Visuals2.outline.enabled || !Visuals2.outline.replaceVanilla());
      LivingEntityFeatureRendererRegistrationCallback.EVENT.register((var0, var1x, var2x, var3x) -> {
         if (var1x instanceof AvatarRenderer) {
            try {
               var2x.register(CosRender.feature(var1x));
            } catch (Throwable var5) {
               LegoClient.LOG("Kosmetik-Renderer: " + var5);
            }
         }
      });
   }
}
