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
   private static EntityDataAccessor<Float> field_6247;

   @Shadow
   public abstract float method_6032();

   @Inject(
      method = {"onSyncedDataUpdated"},
      at = {@At("TAIL")}
   )
   private void baulooStyles$health(EntityDataAccessor<?> data, CallbackInfo ci) {
      if (field_6247.equals(data) && this.method_6032() <= 0.0F) {
         KillEffect.onDeath((LivingEntity)this);
      }
   }
}
