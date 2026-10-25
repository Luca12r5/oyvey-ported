package de.bauloo.styles.mixin;

import de.bauloo.styles.KillEffect;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1309.class)
public class KillEffectMixin {
   @Inject(method = "method_5711", at = @At("HEAD"))
   private void baulooStyles$died(byte status, CallbackInfo ci) {
      if (status == 3) {
         KillEffect.onDeath((class_1309)(Object)this);
      }
   }
}
