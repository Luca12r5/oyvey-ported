package de.bauloo.styles.mixin;

import de.bauloo.styles.CustomSky;
import net.minecraft.class_12131;
import net.minecraft.class_4587;
import net.minecraft.class_9975;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_9975.class)
public class CustomSkyMixin {
   @Inject(method = "method_62307", at = @At("HEAD"), require = 0)
   private void baulooStyles$customSky(
      class_4587 matrices, float sunAngle, float moonAngle, float starAngle, class_12131 moonPhase, float alpha, float starBrightness, CallbackInfo ci
   ) {
      try {
         CustomSky.render(matrices, sunAngle);
      } catch (Throwable t) {
         CustomSky.failed(t);
      }
   }
}
