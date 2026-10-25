package de.bauloo.styles.mixin;

import de.bauloo.styles.AnchorGlow;
import de.bauloo.styles.AnchorPixelGlow;
import de.bauloo.styles.FireColor;
import de.bauloo.styles.ShieldColor;
import net.minecraft.class_1059;
import net.minecraft.class_2960;
import net.minecraft.class_4722;
import net.minecraft.class_7766.class_7767;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1059.class)
public class AnchorAtlasMixin {
   @Shadow
   @Final
   private class_2960 field_21749;

   @Inject(method = "method_45848", at = @At("TAIL"))
   private void baulooStyles$anchorGlow(class_7767 result, CallbackInfo ci) {
      if (class_1059.field_5275.equals(this.field_21749)) {
         AnchorGlow.applyLive();
         AnchorPixelGlow.applyLive();
         FireColor.atlasRebuilt();
      } else if (class_4722.field_21707.equals(this.field_21749)) {
         ShieldColor.atlasRebuilt();
      }
   }
}
