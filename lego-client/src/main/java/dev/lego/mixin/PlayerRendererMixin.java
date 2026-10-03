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
            dev.lego.net.LegoNet.Entry e = dev.lego.net.LegoNet.get(var1.getUUID());
            if (e == null) {
               return;
            }
            if (dev.lego.net.LegoNet.tagsEnabled() && e.nameTag() != null && var2.nameTag != null) {
               dev.lego.net.NameTags.Style style = dev.lego.net.NameTags.get(e.nameTag());
               if (style != null) {
                  var2.nameTag = dev.lego.net.NameTags.component(style, var1.getName().getString(), e.customTag());
               }
            }
            String cape = dev.lego.net.LegoNet.cosmeticsEnabled() && e.items().get(dev.lego.cosmetic.Cos.Slot.CAPE) != null
               ? dev.lego.cosmetic.Cos.get(e.items().get(dev.lego.cosmetic.Cos.Slot.CAPE)).cape : null;
            Identifier tex = CosRender.capeTextureFor(cape);
            if (tex != null) {
               ResourceTexture rt = new ResourceTexture(tex, tex);
               var2.skin = var2.skin.with(Patch.create(Optional.empty(), Optional.of(rt), Optional.of(rt), Optional.empty()));
               var2.showCape = true;
            }
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
