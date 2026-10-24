package dev.lego.mixin;

import dev.lego.core.Mc;
import dev.lego.visual.Visuals;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {MouseHandler.class},
   remap = false
)
public abstract class MouseMixin {
   @Inject(
      method = {"method_1598(JDD)V"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false,
      require = 0
   )
   private void lego$scroll(long window, double h, double v, CallbackInfo ci) {
      try {
         if (Mc.screenOpen() || v == 0.0) {
            return;
         }

         if (Visuals.zoom == null) {
            return;
         }

         if (Visuals.zoom.onScroll(v)) {
            ci.cancel();
         }
      } catch (Throwable var9) {
      }
   }
}
