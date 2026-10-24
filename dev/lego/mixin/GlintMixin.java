package dev.lego.mixin;

import dev.lego.visual.Visuals;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {ItemRenderer.class},
   remap = false
)
public abstract class GlintMixin {
   @Inject(
      method = {"method_23181"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false,
      require = 0
   )
   private static void lego$glint(MultiBufferSource consumers, RenderType layer, boolean solid, boolean glint, CallbackInfoReturnable cir) {
      try {
         if (glint && Visuals.glint != null && Visuals.glint.enabled && Visuals.glint.shouldHideGlint()) {
            cir.setReturnValue(consumers.getBuffer(layer));
         }
      } catch (Throwable var6) {
      }
   }
}
