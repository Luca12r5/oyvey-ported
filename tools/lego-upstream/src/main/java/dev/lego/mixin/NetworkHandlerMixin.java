package dev.lego.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.lego.core.Mc;
import dev.lego.visual.Visuals;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2663;
import net.minecraft.class_634;

/** ClientPlayNetworkHandler.onEntityStatus: 35 = totem pop, 3 = death (TAIL = main thread only). */
@Mixin(value = class_634.class, remap = false)
public abstract class NetworkHandlerMixin {
    @Inject(method = "method_11148(Lnet/minecraft/class_2663;)V", at = @At("TAIL"), remap = false, require = 0)
    private void lego$status(class_2663 packet, CallbackInfo ci) {
        try {
            if (Mc.world() == null) return;
            class_1297 e = packet.method_11469(Mc.world());
            if (e instanceof class_1309) Visuals.onStatus((class_1309) e, packet.method_11470());
        } catch (Throwable ignored) { }
    }
}
