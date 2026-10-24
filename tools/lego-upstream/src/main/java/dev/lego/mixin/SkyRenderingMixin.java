package dev.lego.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.lego.visual.Visuals;
import net.minecraft.class_4587;
import net.minecraft.class_9975;

/** SkyRendering: the custom sky is drawn inside the vanilla sky pass (correct camera, behind everything) and replaces it. */
@Mixin(value = class_9975.class, remap = false)
public abstract class SkyRenderingMixin {
    @Inject(method = "method_62302(I)V", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void lego$top(int color, CallbackInfo ci) {
        try { if (Visuals.renderSkyPass()) ci.cancel(); } catch (Throwable ignored) { }
    }

    @Inject(method = "method_62306(Lnet/minecraft/class_4587;FI)V", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void lego$glow(class_4587 m, float a, int c, CallbackInfo ci) {
        try { if (Visuals.skyReplaced() || dev.lego.perf.Performance.potato()) ci.cancel(); } catch (Throwable ignored) { }
    }

    @Inject(method = "method_62307(Lnet/minecraft/class_4587;FFFLnet/minecraft/class_12131;FF)V", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void lego$bodies(class_4587 m, float a, float b, float c, net.minecraft.class_12131 phase, float d, float e, CallbackInfo ci) {
        try { if (Visuals.skyReplaced() || dev.lego.perf.Performance.potato()) ci.cancel(); } catch (Throwable ignored) { }
    }

    @Inject(method = "method_62305()V", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void lego$dark(CallbackInfo ci) {
        try { if (Visuals.skyReplaced()) ci.cancel(); } catch (Throwable ignored) { }
    }
}
