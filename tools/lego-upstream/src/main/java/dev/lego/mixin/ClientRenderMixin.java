package dev.lego.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.class_310;

/** MinecraftClient.render: one frame-time measurement per real frame for all UI animations and games. */
@Mixin(value = class_310.class, remap = false)
public abstract class ClientRenderMixin {
    @Inject(method = "method_1523(Z)V", at = @At("HEAD"), remap = false, require = 0)
    private void lego$frame(boolean tick, CallbackInfo ci) {
        dev.lego.ui.Gx.newFrame();
    }
}
