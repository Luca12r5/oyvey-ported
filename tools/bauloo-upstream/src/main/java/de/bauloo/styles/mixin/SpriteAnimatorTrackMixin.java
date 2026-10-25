package de.bauloo.styles.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import de.bauloo.styles.LiveRecolor;
import net.minecraft.class_7764;
import net.minecraft.class_7764.class_12298;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_7764.class)
public class SpriteAnimatorTrackMixin {
   @Inject(method = "method_45818", at = @At("RETURN"))
   private void baulooStyles$track(GpuBufferSlice slice, int index, CallbackInfoReturnable<class_12298> cir) {
      if (cir.getReturnValue() != null) {
         LiveRecolor.ANIMATORS.put((class_7764)(Object)this, (class_12298)cir.getReturnValue());
      }
   }
}
