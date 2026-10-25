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
   private List<AnimationState> field_64243;
   @Shadow
   @Final
   private Identifier field_21749;

   @Inject(
      method = {"cycleAnimationFrames"},
      at = {@At("HEAD")}
   )
   private void baulooStyles$tickItems(CallbackInfo ci) {
      if (this.field_64243 != null
         && !this.field_64243.isEmpty()
         && TextureAtlas.LOCATION_ITEMS.equals(this.field_21749)
         && ItemAnimations.sodiumExtraBlocksThem()) {
         for (AnimationState animator : this.field_64243) {
            animator.tick();
         }
      }
   }
}
