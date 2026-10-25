package de.bauloo.styles.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.bauloo.styles.BlockOutline;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({LevelRenderer.class})
public class BlockOutlineMixin {
   private static final float HIGH_CONTRAST_WIDTH = 7.0F;

   @WrapOperation(
      method = {"renderBlockOutline"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/LevelRenderer;renderHitOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;DDDLnet/minecraft/client/renderer/state/BlockOutlineRenderState;IF)V"
      )}
   )
   private void baulooStyles$outline(
      LevelRenderer self,
      PoseStack matrices,
      VertexConsumer lines,
      double x,
      double y,
      double z,
      BlockOutlineRenderState state,
      int color,
      float width,
      Operation<Void> original
   ) {
      if (!BlockOutline.active()) {
         original.call(self, matrices, lines, x, y, z, state, color, width);
      } else if (width != 7.0F) {
         original.call(self, matrices, lines, x, y, z, state, BlockOutline.color(), BlockOutline.width());
      }
   }
}
