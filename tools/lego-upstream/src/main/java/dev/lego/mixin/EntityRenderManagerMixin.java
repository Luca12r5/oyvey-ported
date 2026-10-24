package dev.lego.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.lego.perf.Performance;
import net.minecraft.class_1297;
import net.minecraft.class_4604;
import net.minecraft.class_898;

/** EntityRenderManager.shouldRender: entity culling (distance + hidden behind blocks). */
@Mixin(value = class_898.class, remap = false)
public abstract class EntityRenderManagerMixin {
    @Inject(method = "method_3950(Lnet/minecraft/class_1297;Lnet/minecraft/class_4604;DDD)Z", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private void lego$cull(class_1297 e, class_4604 frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (cir.getReturnValueZ() && Performance.culling != null && (Performance.culling.enabled || (Performance.boost != null && Performance.boost.extreme()) || (Performance.moltron != null && Performance.moltron.enabled)) && !Performance.culling.shouldRender(e, x, y, z))
                cir.setReturnValue(false);
        } catch (Throwable ignored) { }
    }
}
