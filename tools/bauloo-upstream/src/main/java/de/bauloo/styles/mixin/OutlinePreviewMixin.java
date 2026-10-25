package de.bauloo.styles.mixin;

import de.bauloo.styles.OutlinePreview;
import net.minecraft.class_10042;
import net.minecraft.class_1309;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_922.class)
public class OutlinePreviewMixin {
   @Inject(method = "method_62355(Lnet/minecraft/class_1309;Lnet/minecraft/class_10042;F)V", at = @At("TAIL"))
   private void baulooStyles$noName(class_1309 entity, class_10042 state, float tickProgress, CallbackInfo ci) {
      if (OutlinePreview.hideName) {
         state.field_53337 = null;
      }
   }
}
