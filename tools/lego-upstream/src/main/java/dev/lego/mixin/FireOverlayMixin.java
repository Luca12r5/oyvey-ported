package dev.lego.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.lego.visual.Visuals2;
import net.minecraft.class_1058;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4603;

/** InGameOverlayRenderer.renderFireOverlay: "Low Fire" moves the fire overlay down. */
@Mixin(value = class_4603.class, remap = false)
public abstract class FireOverlayMixin {
    private static boolean lego$pushed;

    @Inject(method = "method_23070(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;Lnet/minecraft/class_1058;)V", at = @At("HEAD"), remap = false, require = 0)
    private static void lego$low(class_4587 matrices, class_4597 consumers, class_1058 sprite, CallbackInfo ci) {
        lego$pushed = false;
        try {
            if (Visuals2.lowFire != null && Visuals2.lowFire.enabled) {
                matrices.method_22903();
                matrices.method_22904(0, -Visuals2.lowFire.height(), 0);
                lego$pushed = true;
            }
        } catch (Throwable ignored) { }
    }

    @Inject(method = "method_23070(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;Lnet/minecraft/class_1058;)V", at = @At("RETURN"), remap = false, require = 0)
    private static void lego$lowEnd(class_4587 matrices, class_4597 consumers, class_1058 sprite, CallbackInfo ci) {
        if (lego$pushed) { lego$pushed = false; try { matrices.method_22909(); } catch (Throwable ignored) { } }
    }
}
