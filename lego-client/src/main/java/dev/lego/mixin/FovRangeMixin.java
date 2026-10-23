package dev.lego.mixin;

import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Lets the FOV slider in the video options go up to 130 instead of 110 (vanilla range 30-110). */
@Mixin(
   value = {Options.class}
)
public abstract class FovRangeMixin {
   @Unique
   private static boolean lego$logged;

   @ModifyConstant(
      method = {"<init>"},
      constant = {@Constant(intValue = 110)},
      require = 0
   )
   private int lego$fovMax(int max) {
      if (!lego$logged) {
         lego$logged = true;
         System.out.println("[LEGO] FOV slider range extended to 130");
      }
      return 130;
   }
}
