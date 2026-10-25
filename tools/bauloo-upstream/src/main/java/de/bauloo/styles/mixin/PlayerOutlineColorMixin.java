package de.bauloo.styles.mixin;

import de.bauloo.styles.PlayerOutline;
import net.minecraft.class_1297;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(class_897.class)
public class PlayerOutlineColorMixin {
   @Redirect(method = "method_62354", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1297;method_22861()I"))
   private int baulooStyles$outlineColor(class_1297 entity) {
      return PlayerOutline.outlines(entity) ? PlayerOutline.color() : entity.method_22861();
   }
}
