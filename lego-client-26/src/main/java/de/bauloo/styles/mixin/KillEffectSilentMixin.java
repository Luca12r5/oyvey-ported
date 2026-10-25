package de.bauloo.styles.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.bauloo.styles.KillEffect;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({LightningBolt.class})
public class KillEffectSilentMixin {
   @WrapOperation(
      method = {"tick"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/level/Level;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"
      )}
   )
   private void baulooStyles$silent(
      Level world, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch, boolean distance, Operation<Void> original
   ) {
      if (!KillEffect.SILENT.contains(this)) {
         original.call(new Object[]{world, x, y, z, sound, category, volume, pitch, distance});
      }
   }
}
