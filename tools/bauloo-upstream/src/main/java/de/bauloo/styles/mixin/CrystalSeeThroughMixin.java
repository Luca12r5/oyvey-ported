package de.bauloo.styles.mixin;

import de.bauloo.styles.CrystalSeeThrough;
import net.minecraft.class_1921;
import net.minecraft.class_892;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(class_892.class)
public class CrystalSeeThroughMixin {
   @ModifyArg(
      method = "method_3908(Lnet/minecraft/class_10014;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_11659;method_73489(Lnet/minecraft/class_3879;Ljava/lang/Object;Lnet/minecraft/class_4587;Lnet/minecraft/class_1921;IIILnet/minecraft/class_11683$class_11792;)V"
      ),
      index = 3,
      require = 0
   )
   private class_1921 baulooStyles$seeThrough(class_1921 layer) {
      return CrystalSeeThrough.active() ? CrystalSeeThrough.layer() : layer;
   }
}
