package dev.lego.mixin;

import dev.lego.perf.Performance;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {ParticleEngine.class},
   remap = false
)
public abstract class ParticleManagerMixin {
   @Inject(
      method = {"method_3058(Lnet/minecraft/class_703;)V"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false,
      require = 0
   )
   private void lego$limit(Particle var1, CallbackInfo var2) {
      try {
         if (Performance.particles != null && !Performance.particles.allow()) {
            var2.cancel();
         }
      } catch (Throwable var4) {
      }
   }
}
