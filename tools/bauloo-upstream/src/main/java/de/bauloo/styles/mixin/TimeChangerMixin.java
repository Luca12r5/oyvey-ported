package de.bauloo.styles.mixin;

import de.bauloo.styles.StylesUiConfig;
import net.minecraft.class_1937;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1937.class)
public class TimeChangerMixin {
   @Inject(method = "method_8532", at = @At("HEAD"), cancellable = true)
   private void baulooStyles$time(CallbackInfoReturnable<Long> cir) {
      int time = StylesUiConfig.timeOverride;
      if (time >= 0 && (Object)this instanceof class_638) {
         cir.setReturnValue((long)time);
      }
   }
}
