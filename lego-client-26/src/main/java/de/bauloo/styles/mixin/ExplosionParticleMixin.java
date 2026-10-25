package de.bauloo.styles.mixin;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ParticleEngine.class})
public class ExplosionParticleMixin {
   @Inject(
      method = {"createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void baulooStyles$noExplosion(
      ParticleOptions effect, double x, double y, double z, double vx, double vy, double vz, CallbackInfoReturnable<Particle> cir
   ) {
      if (effect != null && (effect.getType() == ParticleTypes.EXPLOSION || effect.getType() == ParticleTypes.EXPLOSION_EMITTER)) {
         cir.setReturnValue(null);
      }
   }
}
