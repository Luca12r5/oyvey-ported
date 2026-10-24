package dev.lego.mixin;

import dev.lego.perf.Performance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {EntityRenderDispatcher.class},
   remap = false
)
public abstract class EntityRenderManagerMixin {
   @Inject(
      method = {"method_3950(Lnet/minecraft/class_1297;Lnet/minecraft/class_4604;DDD)Z"},
      at = {@At("RETURN")},
      cancellable = true,
      remap = false,
      require = 0
   )
   private void lego$cull(Entity e, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
      try {
         if (cir.getReturnValueZ()
            && Performance.culling != null
            && (
               Performance.culling.enabled
                  || Performance.boost != null && Performance.boost.extreme()
                  || Performance.moltron != null && Performance.moltron.enabled
            )
            && !Performance.culling.shouldRender(e, x, y, z)) {
            cir.setReturnValue(false);
         }
      } catch (Throwable var11) {
      }
   }
}
