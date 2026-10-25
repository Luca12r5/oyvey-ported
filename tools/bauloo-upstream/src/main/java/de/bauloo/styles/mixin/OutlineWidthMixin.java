package de.bauloo.styles.mixin;

import de.bauloo.styles.PlayerOutline;
import net.minecraft.class_2960;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(class_761.class)
public class OutlineWidthMixin {
   @ModifyArg(
      method = "method_22710",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_10151;method_62941(Lnet/minecraft/class_2960;Ljava/util/Set;)Lnet/minecraft/class_279;"),
      index = 0
   )
   private class_2960 baulooStyles$width(class_2960 id) {
      return PlayerOutline.postEffect(id);
   }
}
