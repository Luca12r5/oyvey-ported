package de.bauloo.styles.mixin;

import de.bauloo.styles.ShieldContext;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemStackRenderState.class})
public class ShieldContextMixin {
   @Shadow
   ItemDisplayContext displayContext;

   @Inject(
      method = {"submit"},
      at = {@At("HEAD")}
   )
   private void baulooStyles$start(CallbackInfo ci) {
      ShieldContext.current = this.displayContext;
   }

   @Inject(
      method = {"submit"},
      at = {@At("RETURN")}
   )
   private void baulooStyles$end(CallbackInfo ci) {
      ShieldContext.current = ItemDisplayContext.NONE;
   }
}
