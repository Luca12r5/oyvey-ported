package de.bauloo.styles.mixin;

import de.bauloo.styles.PlayerOutline;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_310.class)
public class PlayerOutlineMixin {
   @Inject(method = "method_27022", at = @At("HEAD"), cancellable = true)
   private void baulooStyles$playerOutline(class_1297 entity, CallbackInfoReturnable<Boolean> cir) {
      if (PlayerOutline.outlines(entity)) {
         cir.setReturnValue(true);
      }
   }
}
