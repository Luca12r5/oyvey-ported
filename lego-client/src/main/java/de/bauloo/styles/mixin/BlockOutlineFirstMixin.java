package de.bauloo.styles.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.bauloo.styles.BlockOutline;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
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
      method = {"renderBlockOutline"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void baulooStyles$ourOutlineFirst(BufferSource consumers, PoseStack matrices, boolean translucent, LevelRenderState world, CallbackInfo ci) {
      if (BlockOutline.active()) {
         BlockOutlineRenderState outline = world.blockOutlineRenderState;
         ci.cancel();
         if (outline != null && outline.isTranslucent() == translucent) {
            Vec3 cam = world.cameraRenderState.pos;
            BlockPos pos = outline.pos();
            VertexConsumer lines = consumers.getBuffer(RenderTypes.lines());
            ShapeRenderer.renderShape(
               matrices, lines, outline.shape(), pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z, BlockOutline.color(), BlockOutline.width()
            );
            consumers.endLastBatch();
         }
      }
   }
}
