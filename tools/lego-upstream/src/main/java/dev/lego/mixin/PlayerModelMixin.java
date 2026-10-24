package dev.lego.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.lego.core.Mc;
import dev.lego.emote.Emotes;
import net.minecraft.class_10055;
import net.minecraft.class_572;
import net.minecraft.class_591;

/** PlayerEntityModel.setAngles(PlayerEntityRenderState): applies emote poses to your own model. */
@Mixin(value = class_591.class, remap = false)
public abstract class PlayerModelMixin {
    @Inject(method = "method_62110(Lnet/minecraft/class_10055;)V", at = @At("TAIL"), remap = false, require = 0)
    private void lego$emote(class_10055 state, CallbackInfo ci) {
        try {
            if (Mc.player() == null) return;
            class_572<?> m = (class_572<?>) (Object) this;
            if (state.field_53528 != Mc.id(Mc.player())) {
                // another Lego user playing an emote (Lego-Sync)
                net.minecraft.class_1297 other = Mc.world() == null ? null : Mc.world().method_8469(state.field_53528);
                if (other == null) return;
                String emote = dev.lego.net.LegoSync.emote(other.method_5667());
                if (emote != null) Emotes.poseOther(emote, dev.lego.net.LegoSync.emoteStart(other.method_5667()), m.field_3398, m.field_3391, m.field_3401, m.field_27433, m.field_3392, m.field_3397);
                return;
            }
            if (Emotes.active()) Emotes.pose(m.field_3398, m.field_3391, m.field_3401, m.field_27433, m.field_3392, m.field_3397);
            dev.lego.cosmetic.VehicleWorld.pose(m.field_3398, m.field_3391, m.field_3401, m.field_27433, m.field_3392, m.field_3397);
        } catch (Throwable ignored) { }
    }
}
