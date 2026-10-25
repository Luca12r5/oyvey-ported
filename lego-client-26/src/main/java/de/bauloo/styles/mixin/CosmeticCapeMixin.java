package de.bauloo.styles.mixin;

import de.bauloo.styles.Cosmetics;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// After the LEGO cape (PlayerRendererMixin, same TAIL): a picked Bauloo cape wins.
@Mixin(value = {AvatarRenderer.class}, priority = 1100)
public class CosmeticCapeMixin {
   @Inject(
      method = {"extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V"},
      at = {@At("TAIL")}
   )
   private void baulooStyles$cosmeticCape(Avatar player, AvatarRenderState state, float tickDelta, CallbackInfo ci) {
      if (state.skin != null && Cosmetics.capeFor(player) != null) {
         state.skin = Cosmetics.withCape(player, state.skin);
         state.showCape = true;
         dev.lego.ci.CiShots.baulooCape = state.skin.cape() == null ? null : state.skin.cape().texturePath().toString();
      }
   }
}
