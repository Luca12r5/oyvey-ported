package de.bauloo.styles.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.bauloo.styles.BlockOutline;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {LevelRenderer.class},
   priority = 100
)
public abstract class BlockOutlineFirstMixin {
   @Inject(
      method = {"submitBlockOutline"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void baulooStyles$ourOutlineFirst(PoseStack matrices, SubmitNodeCollector collector, LevelRenderState world, CallbackInfo ci) {
      if (BlockOutline.active()) {
         ci.cancel();
         BlockOutlineRenderState outline = world.blockOutlineRenderState;
         if (outline != null) {
            Vec3 cam = world.cameraRenderState.pos;
            BlockPos pos = outline.pos();
            matrices.pushPose();
            matrices.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
            collector.submitShapeOutline(matrices, outline.shape(), RenderTypes.lines(), BlockOutline.color(), BlockOutline.width(), outline.isTranslucent());
            matrices.popPose();
         }
      }
   }
}
