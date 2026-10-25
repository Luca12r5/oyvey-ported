package de.bauloo.styles.mixin;

import de.bauloo.styles.StylesUiConfig;
import net.minecraft.class_11659;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_4587;
import net.minecraft.class_759;
import net.minecraft.class_811;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_759.class)
public class HeldTotemSizeMixin {
   @Unique
   private boolean baulooStyles$scaled;

   @Inject(
      method = "method_3233(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V",
      at = @At("HEAD")
   )
   private void baulooStyles$scaleTotem(
      class_1309 entity, class_1799 stack, class_811 context, class_4587 matrices, class_11659 queue, int light, CallbackInfo ci
   ) {
      StylesUiConfig.load();
      int size = !context.method_29998()
         ? 100
         : (
            stack.method_31574(class_1802.field_8288)
               ? StylesUiConfig.totemSize
               : (
                  stack.method_31574(class_1802.field_8255)
                     ? StylesUiConfig.shieldSize
                     : (!stack.method_31574(class_1802.field_8463) && !stack.method_31574(class_1802.field_8367) ? 100 : StylesUiConfig.gapSize)
               )
         );
      this.baulooStyles$scaled = size != 100;
      if (this.baulooStyles$scaled) {
         float f = size / 100.0F;
         matrices.method_22903();
         matrices.method_22905(f, f, f);
      }
   }

   @Inject(
      method = "method_3233(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V",
      at = @At("RETURN")
   )
   private void baulooStyles$restore(class_1309 entity, class_1799 stack, class_811 context, class_4587 matrices, class_11659 queue, int light, CallbackInfo ci) {
      if (this.baulooStyles$scaled) {
         this.baulooStyles$scaled = false;
         matrices.method_22909();
      }
   }
}
