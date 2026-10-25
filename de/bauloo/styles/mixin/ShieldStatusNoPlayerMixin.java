package de.bauloo.styles.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(
   targets = {"walksy/shieldstatus/manager/ShieldStateManager"},
   remap = false
)
public class ShieldStatusNoPlayerMixin {
   @Inject(
      method = {"isCoolingDown"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void baulooStyles$noPlayer(Player player, CallbackInfoReturnable<Boolean> cir) {
      if (Minecraft.getInstance().player == null) {
         cir.setReturnValue(false);
      }
   }

   @Inject(
      method = {"getCooldownProgress"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void baulooStyles$noPlayerProgress(Player player, CallbackInfoReturnable<Float> cir) {
      if (Minecraft.getInstance().player == null) {
         cir.setReturnValue(0.0F);
      }
   }
}
