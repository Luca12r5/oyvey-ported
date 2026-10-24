package dev.lego.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.lego.core.Mc;
import dev.lego.cosmetic.CosRender;
import net.minecraft.class_1007;
import net.minecraft.class_10055;
import net.minecraft.class_11890;
import net.minecraft.class_12079;
import net.minecraft.class_2960;
import net.minecraft.class_8685;

/** PlayerEntityRenderer.updateRenderState: swaps in the equipped HD Lego cape for your own player (only you see it). */
@Mixin(value = class_1007.class, remap = false)
public abstract class PlayerRendererMixin {
    @Inject(method = "method_62604(Lnet/minecraft/class_11890;Lnet/minecraft/class_10055;F)V", at = @At("TAIL"), remap = false, require = 0)
    private void lego$cape(class_11890 player, class_10055 state, float tickDelta, CallbackInfo ci) {
        try {
            if (player != Mc.player()) { dev.lego.visual.NameFx.applyOther(state, player); return; }
            try { dev.lego.visual.NameFx.apply(state); } catch (Throwable ignored) { }
            class_2960 tex = CosRender.capeTexture();
            if (tex == null) return;
            class_12079.class_10726 info = new class_12079.class_10726(tex, tex);
            state.field_53520 = state.field_53520.method_74883(
                    class_8685.class_11892.method_74885(Optional.empty(), Optional.of(info), Optional.of(info), Optional.empty()));
            state.field_53532 = true;
        } catch (Throwable ignored) { }
    }
}
