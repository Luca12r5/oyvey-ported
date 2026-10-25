package de.bauloo.styles.mixin;

import de.bauloo.styles.Cosmetics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({AbstractClientPlayer.class})
public class CapeSkinMixin {
   @Inject(
      method = {"getSkin"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void baulooStyles$capeInSkin(CallbackInfoReturnable<PlayerSkin> cir) {
      PlayerSkin old = cir.getReturnValue();
      PlayerSkin swapped = Cosmetics.withCape((Avatar)this, old);
      if (swapped != old) {
         cir.setReturnValue(swapped);
      }
   }
}
