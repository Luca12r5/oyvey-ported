package de.bauloo.styles.mixin;

import de.bauloo.styles.ItemAnimations;
import java.util.List;
import net.minecraft.class_1059;
import net.minecraft.class_2960;
import net.minecraft.class_7764.class_12298;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1059.class)
public class ItemAnimationMixin {
   @Shadow
   private List<class_12298> field_64243;
   @Shadow
   @Final
   private class_2960 field_21749;

   @Inject(method = "method_4612", at = @At("HEAD"))
   private void baulooStyles$tickItems(CallbackInfo ci) {
      if (this.field_64243 != null && !this.field_64243.isEmpty() && class_1059.field_64467.equals(this.field_21749) && ItemAnimations.sodiumExtraBlocksThem()) {
         for (class_12298 animator : this.field_64243) {
            animator.method_76307();
         }
      }
   }
}
