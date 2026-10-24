package dev.lego.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.lego.util.Utility;
import net.minecraft.class_4184;

/** Camera.update: freelook replaces the rotation the camera takes from the player. */
@Mixin(value = class_4184.class, remap = false)
public abstract class CameraMixin {
    @Shadow(remap = false) protected abstract void method_19325(float yaw, float pitch);
    @Shadow(remap = false) protected abstract void method_19327(double x, double y, double z);

    private final double[] lego$cam = new double[5];

    /** Cutscene emotes: the camera flies on its own path. */
    @Inject(method = "method_19321(Lnet/minecraft/class_1937;Lnet/minecraft/class_1297;ZZF)V", at = @At("TAIL"), remap = false, require = 0)
    private void lego$cutscene(net.minecraft.class_1937 world, net.minecraft.class_1297 focus, boolean thirdPerson, boolean inverse, float td, CallbackInfo ci) {
        try {
            if (dev.lego.emote.Cutscenes.camera(td, lego$cam)) {
                method_19325((float) lego$cam[3], (float) lego$cam[4]);
                method_19327(lego$cam[0], lego$cam[1], lego$cam[2]);
            }
        } catch (Throwable ignored) { }
    }

    @ModifyArg(method = "method_19321(Lnet/minecraft/class_1937;Lnet/minecraft/class_1297;ZZF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4184;method_19325(FF)V"), index = 0, remap = false, require = 0)
    private float lego$yaw(float yaw) {
        return Utility.freelook != null && Utility.freelook.active() ? Utility.freelook.yaw() : yaw;
    }

    @ModifyArg(method = "method_19321(Lnet/minecraft/class_1937;Lnet/minecraft/class_1297;ZZF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4184;method_19325(FF)V"), index = 1, remap = false, require = 0)
    private float lego$pitch(float pitch) {
        return Utility.freelook != null && Utility.freelook.active() ? Utility.freelook.pitch() : pitch;
    }
}
