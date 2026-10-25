package de.bauloo.styles.mixin;

import de.bauloo.styles.StylesUiConfig;
import net.minecraft.class_1113;
import net.minecraft.class_1144;
import net.minecraft.class_1140.class_11518;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1144.class)
public class ExplosionSoundMixin {
   @Inject(method = "method_4873(Lnet/minecraft/class_1113;)Lnet/minecraft/class_1140$class_11518;", at = @At("HEAD"), cancellable = true)
   private void baulooStyles$mute(class_1113 sound, CallbackInfoReturnable<class_11518> cir) {
      if (StylesUiConfig.noExplosionSound
         && sound != null
         && "minecraft".equals(sound.method_4775().method_12836())
         && "entity.generic.explode".equals(sound.method_4775().method_12832())) {
         cir.setReturnValue(class_11518.field_60956);
      }
   }
}
