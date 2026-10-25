package de.bauloo.styles.mixin;

import de.bauloo.styles.KillEffect;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntity.class})
public abstract class KillEffectHealthMixin {
   @Shadow
   @Final
   private static EntityDataAccessor<Float> DATA_HEALTH_ID;

   @Shadow
   public abstract float getHealth();

   @Inject(
      method = {"onSyncedDataUpdated"},
      at = {@At("TAIL")}
   )
   private void baulooStyles$health(EntityDataAccessor<?> data, CallbackInfo ci) {
      if (DATA_HEALTH_ID.equals(data) && this.getHealth() <= 0.0F) {
         KillEffect.onDeath((LivingEntity)(Object)this);
      }
   }
}
