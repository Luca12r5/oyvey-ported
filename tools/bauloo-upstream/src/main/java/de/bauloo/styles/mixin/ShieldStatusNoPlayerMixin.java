package de.bauloo.styles.mixin;

import net.minecraft.class_1657;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "walksy/shieldstatus/manager/ShieldStateManager", remap = false)
public class ShieldStatusNoPlayerMixin {
   @Inject(method = "isCoolingDown", at = @At("HEAD"), cancellable = true, require = 0)
   private void baulooStyles$noPlayer(class_1657 player, CallbackInfoReturnable<Boolean> cir) {
      if (class_310.method_1551().field_1724 == null) {
         cir.setReturnValue(false);
      }
   }

   @Inject(method = "getCooldownProgress", at = @At("HEAD"), cancellable = true, require = 0)
   private void baulooStyles$noPlayerProgress(class_1657 player, CallbackInfoReturnable<Float> cir) {
      if (class_310.method_1551().field_1724 == null) {
         cir.setReturnValue(0.0F);
      }
   }
}
