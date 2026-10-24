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
   private void lego$cull(BlockEntity var1, float var2, CrumblingOverlay var3, CallbackInfoReturnable<Object> var4) {
      try {
         Performance.FpsBoost var5 = Performance.boost;
         if (var5 == null) {
            return;
         }

         double var6 = var5.blockEntityDist();
         if (var6 <= 0.0 || this.field_63069 == null) {
            return;
         }

         BlockPos var8 = var1.getBlockPos();
         double var9 = var8.getX() + 0.5 - this.field_63069.x;
         double var11 = var8.getY() + 0.5 - this.field_63069.y;
         double var13 = var8.getZ() + 0.5 - this.field_63069.z;
         if (var9 * var9 + var11 * var11 + var13 * var13 > var6 * var6) {
            var4.setReturnValue(null);
         }
      } catch (Throwable var15) {
      }
   }
}
