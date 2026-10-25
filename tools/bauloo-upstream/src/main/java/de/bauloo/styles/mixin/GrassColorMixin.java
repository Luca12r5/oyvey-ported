package de.bauloo.styles.mixin;

import de.bauloo.styles.GrassColor;
import net.minecraft.class_1163;
import net.minecraft.class_1920;
import net.minecraft.class_2338;
import net.minecraft.class_6539;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = class_1163.class, priority = 2000)
public class GrassColorMixin {
   @Inject(method = "method_4965", at = @At("HEAD"), cancellable = true)
   private static void baulooStyles$grass(class_1920 world, class_2338 pos, class_6539 resolver, CallbackInfoReturnable<Integer> cir) {
      if (resolver == class_1163.field_5665) {
         GrassColor.mixinCalls++;
         if (GrassColor.current() >= 0) {
            cir.setReturnValue(GrassColor.shown());
         }
      }
   }

   @Inject(method = "method_4962", at = @At("HEAD"), cancellable = true)
   private static void baulooStyles$grassFirst(class_1920 world, class_2338 pos, CallbackInfoReturnable<Integer> cir) {
      GrassColor.mixinCalls++;
      if (GrassColor.current() >= 0) {
         cir.setReturnValue(GrassColor.shown());
      }
   }
}
