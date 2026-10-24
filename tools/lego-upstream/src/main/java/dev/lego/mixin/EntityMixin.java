package dev.lego.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.lego.core.Mc;
import dev.lego.util.Utility;
import net.minecraft.class_1297;

/** Entity.changeLookDirection: while freelook is held, the mouse turns the camera, not you. */
@Mixin(value = class_1297.class, remap = false)
public abstract class EntityMixin {
    @Inject(method = "method_5872(DD)V", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void lego$look(double dx, double dy, CallbackInfo ci) {
        try {
            if ((Object) this == Mc.player() && Utility.freelook != null && Utility.freelook.active()) {
                Utility.freelook.look(dx, dy);
                ci.cancel();
            }
        } catch (Throwable ignored) { }
    }
}
