package dev.lego.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.lego.perf.Performance;
import net.minecraft.class_9919;

/** InactivityFpsLimiter.update: lower FPS while the window is in the background. */
@Mixin(value = class_9919.class, remap = false)
public abstract class FpsLimiterMixin {
    @Inject(method = "method_61937()I", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private void lego$bg(CallbackInfoReturnable<Integer> cir) {
        try {
            if (Performance.background != null) cir.setReturnValue(Performance.background.limit(cir.getReturnValueI()));
        } catch (Throwable ignored) { }
    }
}
