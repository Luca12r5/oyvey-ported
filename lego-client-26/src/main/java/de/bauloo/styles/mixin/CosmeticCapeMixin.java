package de.bauloo.styles.mixin;

import de.bauloo.styles.Cosmetics;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({AvatarRenderer.class})
public class CosmeticCapeMixin {
   @Inject(
      method = {"extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V"},
      at = {@At("TAIL")}
   )
   private void baulooStyles$cosmeticCape(Avatar player, AvatarRenderState state, float tickDelta, CallbackInfo ci) {
      if (state.skin != null && Cosmetics.capeFor(player) != null) {
         state.skin = Cosmetics.withCape(player, state.skin);
         state.showCape = true;
      }
   }
}
