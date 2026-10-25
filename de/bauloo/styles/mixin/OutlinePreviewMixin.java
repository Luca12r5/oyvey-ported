package de.bauloo.styles.mixin;

import de.bauloo.styles.OutlinePreview;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntityRenderer.class})
public class OutlinePreviewMixin {
   @Inject(
      method = {"extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V"},
      at = {@At("TAIL")}
   )
   private void baulooStyles$noName(LivingEntity entity, LivingEntityRenderState state, float tickProgress, CallbackInfo ci) {
      if (OutlinePreview.hideName) {
         state.nameTag = null;
      }
   }
}
