package de.bauloo.styles.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import de.bauloo.styles.StylesUiConfig;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({ScreenEffectRenderer.class})
public class TotemPopSizeMixin {
   @WrapOperation(
      method = {"submit"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;renderItemActivationAnimation(Lcom/mojang/blaze3d/vertex/PoseStack;FLnet/minecraft/client/renderer/SubmitNodeCollector;)V"
      )}
   )
   private void baulooStyles$popSize(ScreenEffectRenderer self, PoseStack matrices, float tickProgress, SubmitNodeCollector queue, Operation<Void> original) {
      StylesUiConfig.load();
      if (StylesUiConfig.totemPopSize == 100) {
         original.call(new Object[]{self, matrices, tickProgress, queue});
      } else {
         float factor = StylesUiConfig.totemPopSize / 100.0F;
         matrices.pushPose();
         matrices.scale(factor, factor, 1.0F);

         try {
            original.call(new Object[]{self, matrices, tickProgress, queue});
         } finally {
            matrices.popPose();
         }
      }
   }
}
