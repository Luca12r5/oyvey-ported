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
   value = {AvatarRenderer.class}
)
public abstract class PlayerRendererMixin {
   @Inject(
      method = {"extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V"},
      at = {@At("TAIL")},
      require = 0
   )
   private void lego$cape(Avatar var1, AvatarRenderState var2, float var3, CallbackInfo var4) {
      try {
         if (var1 != Mc.player()) {
            return;
         }

         try {
            NameFx.apply(var2);
         } catch (Throwable var7) {
         }

         Identifier var5 = CosRender.capeTexture();
         if (var5 == null) {
            return;
         }

         ResourceTexture var6 = new ResourceTexture(var5, var5);
         var2.skin = var2.skin.with(Patch.create(Optional.empty(), Optional.of(var6), Optional.of(var6), Optional.empty()));
         var2.showCape = true;
      } catch (Throwable var8) {
      }
   }
}
