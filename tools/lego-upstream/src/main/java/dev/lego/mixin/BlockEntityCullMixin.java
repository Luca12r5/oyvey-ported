package dev.lego.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_824;

/** BlockEntityRenderManager.getRenderState: FPS-Boost (Maximum / EXTREM) skips far away chests, signs, banners ... */
@Mixin(value = class_824.class, remap = false)
public abstract class BlockEntityCullMixin {
    @Shadow(remap = false) private class_243 field_63069;

    @Inject(method = "method_74348(Lnet/minecraft/class_2586;FLnet/minecraft/class_11683$class_11792;)Lnet/minecraft/class_11954;",
            at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void lego$cull(class_2586 be, float td, net.minecraft.class_11683.class_11792 crumble, CallbackInfoReturnable<Object> cir) {
        try {
            double max = 0;
            dev.lego.perf.Performance.FpsBoost b = dev.lego.perf.Performance.boost;
            if (b != null) max = b.blockEntityDist();
            dev.lego.perf.Performance.Moltron m = dev.lego.perf.Performance.moltron;
            if (m != null) max = Math.max(max, m.blockEntityDist());
            if (max <= 0 || field_63069 == null) return;
            net.minecraft.class_2338 p = be.method_11016();
            double dx = p.method_10263() + 0.5 - field_63069.field_1352, dy = p.method_10264() + 0.5 - field_63069.field_1351, dz = p.method_10260() + 0.5 - field_63069.field_1350;
            if (dx * dx + dy * dy + dz * dz > max * max) cir.setReturnValue(null);
        } catch (Throwable ignored) { }
    }
}
