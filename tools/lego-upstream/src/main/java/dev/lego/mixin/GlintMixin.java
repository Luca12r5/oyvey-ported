package dev.lego.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.lego.visual.Visuals;
import net.minecraft.class_918;
import net.minecraft.class_1921;
import net.minecraft.class_4597;

/** ItemRenderer.getItemGlintConsumer: hide enchantment glint when GlintColor has "Glint deaktivieren" on. */
@Mixin(value = class_918.class, remap = false)
public abstract class GlintMixin {
    @SuppressWarnings("unchecked")
    @Inject(method = "method_23181", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void lego$glint(class_4597 consumers, class_1921 layer, boolean solid, boolean glint, CallbackInfoReturnable cir) {
        try {
            if (glint && Visuals.glint != null && Visuals.glint.enabled && Visuals.glint.shouldHideGlint()) {
                cir.setReturnValue(consumers.method_73477(layer));
            }
        } catch (Throwable ignored) { }
    }
}
