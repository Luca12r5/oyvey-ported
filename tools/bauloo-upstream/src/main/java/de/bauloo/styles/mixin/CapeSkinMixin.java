package de.bauloo.styles.mixin;

import de.bauloo.styles.Cosmetics;
import net.minecraft.class_11890;
import net.minecraft.class_742;
import net.minecraft.class_8685;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_742.class)
public class CapeSkinMixin {
   @Inject(method = "method_52814", at = @At("RETURN"), cancellable = true)
   private void baulooStyles$capeInSkin(CallbackInfoReturnable<class_8685> cir) {
      class_8685 old = (class_8685)cir.getReturnValue();
      class_8685 swapped = Cosmetics.withCape((class_11890)this, old);
      if (swapped != old) {
         cir.setReturnValue(swapped);
      }
   }
}
