package de.bauloo.styles.mixin;

import de.bauloo.styles.GrassColor;
import net.minecraft.class_1959;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = class_1959.class, priority = 2000)
public class BiomeGrassColorMixin {
   @Inject(method = "method_8711", at = @At("RETURN"), cancellable = true)
   private void baulooStyles$grass(double x, double z, CallbackInfoReturnable<Integer> cir) {
      int color = GrassColor.shown();
      if (GrassColor.current() >= 0) {
         cir.setReturnValue(color);
      }
   }
}
