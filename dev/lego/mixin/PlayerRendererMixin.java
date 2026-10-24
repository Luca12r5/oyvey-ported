package dev.lego.mixin;

import dev.lego.core.Mc;
import dev.lego.cosmetic.CosRender;
import dev.lego.visual.NameFx;
import java.util.Optional;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.ClientAsset.ResourceTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerSkin.Patch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {AvatarRenderer.class},
   remap = false
)
public abstract class PlayerRendererMixin {
   @Inject(
      method = {"method_62604(Lnet/minecraft/class_11890;Lnet/minecraft/class_10055;F)V"},
      at = {@At("TAIL")},
      remap = false,
      require = 0
   )
   private void lego$cape(Avatar player, AvatarRenderState state, float tickDelta, CallbackInfo ci) {
      try {
         if (player != Mc.player()) {
            NameFx.applyOther(state, player);
            return;
         }

         try {
            NameFx.apply(state);
         } catch (Throwable var7) {
         }

         Identifier tex = CosRender.capeTexture();
         if (tex == null) {
            return;
         }

         ResourceTexture info = new ResourceTexture(tex, tex);
         state.skin = state.skin.with(Patch.create(Optional.empty(), Optional.of(info), Optional.of(info), Optional.empty()));
         state.showCape = true;
      } catch (Throwable var8) {
      }
   }
}
