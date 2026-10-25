package de.bauloo.styles.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import de.bauloo.styles.CrystalSeeThrough;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({EndCrystalRenderer.class})
public class CrystalSeeThroughMixin {
   @WrapOperation(
      method = {"submit"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/resources/Identifier;III)V"
      )},
      require = 0
   )
   private void baulooStyles$seeThrough(
      SubmitNodeCollector collector,
      Model<?> model,
      Object state,
      PoseStack poseStack,
      Identifier texture,
      int light,
      int overlay,
      int outline,
      Operation<Void> original
   ) {
      if (CrystalSeeThrough.active()) {
         submitTranslucent(collector, model, state, poseStack, light, overlay, outline);
      } else {
         original.call(new Object[]{collector, model, state, poseStack, texture, light, overlay, outline});
      }
   }

   private static <S> void submitTranslucent(
      SubmitNodeCollector collector, Model<?> model, Object state, PoseStack poseStack, int light, int overlay, int outline
   ) {
      collector.submitModel((Model) model, state, poseStack, CrystalSeeThrough.layer(), light, overlay, outline);
   }
}
