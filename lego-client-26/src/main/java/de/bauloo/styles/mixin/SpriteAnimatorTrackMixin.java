package de.bauloo.styles.mixin;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import de.bauloo.styles.LiveRecolor;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteContents.AnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({SpriteContents.class})
public class SpriteAnimatorTrackMixin {
   @Inject(
      method = {"createAnimationState"},
      at = {@At("RETURN")}
   )
   private void baulooStyles$track(GpuBufferSlice slice, int index, CallbackInfoReturnable<AnimationState> cir) {
      if (cir.getReturnValue() != null) {
         LiveRecolor.ANIMATORS.put((SpriteContents)(Object)this, (AnimationState)cir.getReturnValue());
      }
   }
}
