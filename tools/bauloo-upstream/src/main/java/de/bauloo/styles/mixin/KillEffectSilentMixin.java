package de.bauloo.styles.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.bauloo.styles.KillEffect;
import net.minecraft.class_1538;
import net.minecraft.class_1937;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1538.class)
public class KillEffectSilentMixin {
   @WrapOperation(
      method = "method_5773",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1937;method_8486(DDDLnet/minecraft/class_3414;Lnet/minecraft/class_3419;FFZ)V")
   )
   private void baulooStyles$silent(
      class_1937 world,
      double x,
      double y,
      double z,
      class_3414 sound,
      class_3419 category,
      float volume,
      float pitch,
      boolean distance,
      Operation<Void> original
   ) {
      if (!KillEffect.SILENT.contains(this)) {
         original.call(new Object[]{world, x, y, z, sound, category, volume, pitch, distance});
      }
   }
}
