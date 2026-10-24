package dev.lego.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.lego.core.Mc;
import dev.lego.visual.Visuals2;
import dev.lego.visual.Visuals3;
import dev.lego.visual.shader.HandEffect;
import net.minecraft.class_742;
import net.minecraft.class_746;
import net.minecraft.class_759;
import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_11659;

@Mixin(value = class_759.class, remap = false)
public abstract class HeldItemRendererMixin {

    private static boolean lego$needsEffect() {
        if (Visuals2.shaderHandsEnabled()) return true;
        if (Visuals3.totemGhost != null && Visuals3.totemGhost.enabled) {
            class_746 me = Mc.player();
            if (me == null) return false;
            net.minecraft.class_1799 main = me.method_6047();
            net.minecraft.class_1799 off = me.method_6079();
            boolean mainT = !Mc.empty(main) && Visuals3.totemGhost.shouldBeTransparent(main);
            boolean offT = !Mc.empty(off) && Visuals3.totemGhost.shouldBeTransparent(off);
            return mainT || offT;
        }
        return false;
    }

    @Inject(method = "method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_746;I)V",
            at = @At("HEAD"), remap = false, require = 0)
    private void lego$preRenderItem(float tickDelta, class_4587 matrices, class_11659 collector, class_746 player, int light, CallbackInfo ci) {
        try {
            if (lego$needsEffect()) {
                HandEffect.getInstance().captureBeforeHands();
            }
        } catch (Throwable ignored) { }
    }

    @Inject(method = "method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_746;I)V",
            at = @At("TAIL"), remap = false, require = 0)
    private void lego$postRenderItem(float tickDelta, class_4587 matrices, class_11659 collector, class_746 player, int light, CallbackInfo ci) {
        try {
            if (lego$needsEffect()) {
                HandEffect he = HandEffect.getInstance();
                he.captureAfterHands();
                he.compositeEffects();
            }
        } catch (Throwable ignored) { }
    }
}
