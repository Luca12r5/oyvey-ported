package de.bauloo.styles.mixin;

import de.bauloo.styles.ItemAnimations;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.SpriteContents.AnimationState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({TextureAtlas.class})
public class ItemAnimationMixin {
   @Shadow
   private List<AnimationState> animatedTexturesStates;
   @Shadow
   @Final
   private Identifier location;

   @Inject(
      method = {"cycleAnimationFrames"},
      at = {@At("HEAD")}
   )
   private void baulooStyles$tickItems(CallbackInfo ci) {
      if (this.animatedTexturesStates != null
         && !this.animatedTexturesStates.isEmpty()
         && TextureAtlas.LOCATION_ITEMS.equals(this.location)
         && ItemAnimations.sodiumExtraBlocksThem()) {
         for (AnimationState animator : this.animatedTexturesStates) {
            animator.tick();
         }
      }
   }
}
