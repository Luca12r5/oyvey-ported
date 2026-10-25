package de.bauloo.styles.mixin;

import de.bauloo.styles.KillEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntity.class})
public class KillEffectMixin {
   @Inject(
      method = {"handleEntityEvent"},
      at = {@At("HEAD")}
   )
   private void baulooStyles$died(byte status, CallbackInfo ci) {
      if (status == 3) {
         KillEffect.onDeath((LivingEntity)this);
      }
   }
}
