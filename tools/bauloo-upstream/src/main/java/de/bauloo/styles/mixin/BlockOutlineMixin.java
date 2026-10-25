package de.bauloo.styles.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.bauloo.styles.BlockOutline;
import net.minecraft.class_12074;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_761.class)
public class BlockOutlineMixin {
   private static final float HIGH_CONTRAST_WIDTH = 7.0F;

   @WrapOperation(
      method = "method_62210",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_761;method_22712(Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;DDDLnet/minecraft/class_12074;IF)V"
      )
   )
   private void baulooStyles$outline(
      class_761 self, class_4587 matrices, class_4588 lines, double x, double y, double z, class_12074 state, int color, float width, Operation<Void> original
   ) {
      if (!BlockOutline.active()) {
         original.call(new Object[]{self, matrices, lines, x, y, z, state, color, width});
      } else if (width != 7.0F) {
         original.call(new Object[]{self, matrices, lines, x, y, z, state, BlockOutline.color(), BlockOutline.width()});
      }
   }
}
