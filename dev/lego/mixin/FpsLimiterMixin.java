package dev.lego.mixin;

import com.mojang.blaze3d.platform.FramerateLimitTracker;
import dev.lego.perf.Performance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {FramerateLimitTracker.class},
   remap = false
)
public abstract class FpsLimiterMixin {
   @Inject(
      method = {"method_61937()I"},
      at = {@At("RETURN")},
      cancellable = true,
      remap = false,
      require = 0
   )
   private void lego$bg(CallbackInfoReturnable<Integer> var1) {
      try {
         if (Performance.background != null) {
            var1.setReturnValue(Performance.background.limit(var1.getReturnValueI()));
         }
      } catch (Throwable var3) {
      }
   }
}
