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
   private void lego$cull(Entity var1, Frustum var2, double var3, double var5, double var7, CallbackInfoReturnable<Boolean> var9) {
      try {
         if (var9.getReturnValueZ()
            && Performance.culling != null
            && (Performance.culling.enabled || Performance.boost != null && Performance.boost.extreme())
            && !Performance.culling.shouldRender(var1, var3, var5, var7)) {
            var9.setReturnValue(false);
         }
      } catch (Throwable var11) {
      }
   }
}
