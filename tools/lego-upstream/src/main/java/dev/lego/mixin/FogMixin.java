package dev.lego.mixin;

import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.lego.visual.Visuals;
import net.minecraft.class_4184;
import net.minecraft.class_5636;
import net.minecraft.class_638;
import net.minecraft.class_758;

@Mixin(value = class_758.class, remap = false)
public abstract class FogMixin {
    @Inject(method = "method_62185(Lnet/minecraft/class_4184;FLnet/minecraft/class_638;IF)Lorg/joml/Vector4f;", at = @At("RETURN"), remap = false, require = 0)
    private void lego$fog(class_4184 camera, float td, class_638 world, int viewDistance, float darkness, CallbackInfoReturnable<Vector4f> cir) {
        try {
            if (Visuals.sky == null || !Visuals.sky.enabled) return;
            if (camera.method_19334() != class_5636.field_27888 && camera.method_19334() != class_5636.field_60563) return;
            Vector4f v = cir.getReturnValue();
            if (v == null) return;
            double density = Visuals.sky.fogDensity.get();
            if (density > 0) {
                int fc = Visuals.sky.fogColor.argb() & 0xFFFFFF;
                float fr = ((fc >> 16) & 255) / 255f;
                float fg = ((fc >> 8) & 255) / 255f;
                float fb = (fc & 255) / 255f;
                float k = (float) (density / 100.0);
                v.x = v.x + (fr - v.x) * k;
                v.y = v.y + (fg - v.y) * k;
                v.z = v.z + (fb - v.z) * k;
            } else if (Visuals.sky.fog.get()) {
                int c = Visuals.sky.fogTint();
                if (c == 0) return;
                float k = 0.8f;
                v.x = v.x + (((c >> 16) & 255) / 255f - v.x) * k;
                v.y = v.y + (((c >> 8) & 255) / 255f - v.y) * k;
                v.z = v.z + ((c & 255) / 255f - v.z) * k;
            }
        } catch (Throwable ignored) { }
    }
}
