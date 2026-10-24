package dev.lego.mixin;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.lego.visual.Visuals;
import dev.lego.visual.Visuals2;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_757;

/** GameRenderer (class_757): zoom (getFov), "no hurt cam" + dynamic camera roll (tiltViewWhenHurt). */
@Mixin(value = class_757.class, remap = false)
public abstract class GameRendererMixin {
    @Inject(method = "method_3196(Lnet/minecraft/class_4184;FZ)F", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private void lego$zoom(class_4184 camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        try {
            float fov = cir.getReturnValue();
            float vs = dev.lego.cosmetic.VehicleWorld.speed();
            if (vs > 0.01f) fov *= 1 + 0.07f * vs;           // speed feeling on vehicles
            if (Visuals.customFov != null) fov = Visuals.customFov.apply(fov);
            if (Visuals2.camera != null) fov = Visuals2.camera.fovModify(fov);
            if (Visuals.zoom != null) fov = Visuals.zoom.apply(fov);
            cir.setReturnValue(fov);
        } catch (Throwable ignored) { }
    }

    @Inject(method = "method_3198(Lnet/minecraft/class_4587;F)V", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void lego$tilt(class_4587 matrices, float tickDelta, CallbackInfo ci) {
        try {
            if (Visuals2.camera != null) {
                float roll = Visuals2.camera.roll();
                if (Math.abs(roll) > 0.001f) matrices.method_22907(new Quaternionf().rotateZ((float) Math.toRadians(roll)));
            }
            if (Visuals.noHurtCam != null && Visuals.noHurtCam.enabled) ci.cancel();
        } catch (Throwable ignored) { }
    }
}
