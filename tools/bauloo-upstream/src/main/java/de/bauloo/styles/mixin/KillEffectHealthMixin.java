package de.bauloo.styles.mixin;

import de.bauloo.styles.KillEffect;
import net.minecraft.class_1309;
import net.minecraft.class_2940;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1309.class)
public abstract class KillEffectHealthMixin {
   @Shadow
   @Final
   private static class_2940<Float> field_6247;

   @Shadow
   public abstract float method_6032();

   @Inject(method = "method_5674", at = @At("TAIL"))
   private void baulooStyles$health(class_2940<?> data, CallbackInfo ci) {
      if (field_6247.equals(data) && this.method_6032() <= 0.0F) {
         KillEffect.onDeath((class_1309)this);
      }
   }
}
