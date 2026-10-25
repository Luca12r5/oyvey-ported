package de.bauloo.styles.mixin;

import net.minecraft.class_2394;
import net.minecraft.class_2398;
import net.minecraft.class_702;
import net.minecraft.class_703;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_702.class)
public class ExplosionParticleMixin {
   @Inject(method = "method_3056(Lnet/minecraft/class_2394;DDDDDD)Lnet/minecraft/class_703;", at = @At("HEAD"), cancellable = true)
   private void baulooStyles$noExplosion(
      class_2394 effect, double x, double y, double z, double vx, double vy, double vz, CallbackInfoReturnable<class_703> cir
   ) {
      if (effect != null && (effect.method_10295() == class_2398.field_11236 || effect.method_10295() == class_2398.field_11221)) {
         cir.setReturnValue(null);
      }
   }
}
