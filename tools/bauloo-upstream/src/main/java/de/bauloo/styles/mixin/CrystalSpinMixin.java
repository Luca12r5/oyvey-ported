package de.bauloo.styles.mixin;

import de.bauloo.styles.CrystalAge;
import de.bauloo.styles.StylesUiConfig;
import net.minecraft.class_10014;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_1511;
import net.minecraft.class_4587;
import net.minecraft.class_892;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = class_892.class, priority = 500)
public class CrystalSpinMixin {
   @Inject(method = "method_62417(Lnet/minecraft/class_1511;Lnet/minecraft/class_10014;F)V", at = @At("TAIL"))
   private void baulooStyles$spinSpeed(class_1511 crystal, class_10014 state, float tickProgress, CallbackInfo ci) {
      ((CrystalAge)state).baulooStyles$setRealAge(state.field_53328);
      if (StylesUiConfig.crystalSpin != 100) {
         state.field_53328 = state.field_53328 * CrystalAge.factor();
      }
   }

   @Inject(
      method = "method_3908(Lnet/minecraft/class_10014;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V",
      at = @At("HEAD")
   )
   private void baulooStyles$rememberOffset(class_10014 state, class_4587 matrices, class_11659 queue, class_12075 camera, CallbackInfo ci) {
      float real = ((CrystalAge)state).baulooStyles$realAge();
      CrystalAge.Offset.current = Float.isNaN(real) ? 0.0F : real - state.field_53328;
   }

   @ModifyVariable(method = "method_23155", at = @At("HEAD"), argsOnly = true)
   private static float baulooStyles$normalBob(float age) {
      return age + CrystalAge.Offset.current;
   }
}
