package de.bauloo.styles.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.bauloo.styles.StylesUiConfig;
import net.minecraft.class_11659;
import net.minecraft.class_4587;
import net.minecraft.class_4603;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_4603.class)
public class TotemPopSizeMixin {
   @WrapOperation(
      method = "method_23067",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4603;method_70939(Lnet/minecraft/class_4587;FLnet/minecraft/class_11659;)V")
   )
   private void baulooStyles$popSize(class_4603 self, class_4587 matrices, float tickProgress, class_11659 queue, Operation<Void> original) {
      StylesUiConfig.load();
      if (StylesUiConfig.totemPopSize == 100) {
         original.call(new Object[]{self, matrices, tickProgress, queue});
      } else {
         float factor = StylesUiConfig.totemPopSize / 100.0F;
         matrices.method_22903();
         matrices.method_22905(factor, factor, 1.0F);

         try {
            original.call(new Object[]{self, matrices, tickProgress, queue});
         } finally {
            matrices.method_22909();
         }
      }
   }
}
