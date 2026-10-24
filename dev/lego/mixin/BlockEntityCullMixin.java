package dev.lego.mixin;

import dev.lego.perf.Performance;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {BlockEntityRenderDispatcher.class},
   remap = false
)
public abstract class BlockEntityCullMixin {
   @Shadow(
      remap = false
   )
   private Vec3 field_63069;

   @Inject(
      method = {"method_74348(Lnet/minecraft/class_2586;FLnet/minecraft/class_11683$class_11792;)Lnet/minecraft/class_11954;"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false,
      require = 0
   )
   private void lego$cull(BlockEntity be, float td, CrumblingOverlay crumble, CallbackInfoReturnable<Object> cir) {
      try {
         double max = 0.0;
         Performance.FpsBoost b = Performance.boost;
         if (b != null) {
            max = b.blockEntityDist();
         }

         Performance.Moltron m = Performance.moltron;
         if (m != null) {
            max = Math.max(max, m.blockEntityDist());
         }

         if (max <= 0.0 || this.field_63069 == null) {
            return;
         }

         BlockPos p = be.getBlockPos();
         double dx = p.getX() + 0.5 - this.field_63069.x;
         double dy = p.getY() + 0.5 - this.field_63069.y;
         double dz = p.getZ() + 0.5 - this.field_63069.z;
         if (dx * dx + dy * dy + dz * dz > max * max) {
            cir.setReturnValue(null);
         }
      } catch (Throwable var16) {
      }
   }
}
