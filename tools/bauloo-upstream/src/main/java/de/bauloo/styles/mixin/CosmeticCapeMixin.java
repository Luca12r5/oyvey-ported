package de.bauloo.styles.mixin;

import de.bauloo.styles.Cosmetics;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_11890;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1007.class)
public class CosmeticCapeMixin {
   @Inject(method = "method_62604(Lnet/minecraft/class_11890;Lnet/minecraft/class_10055;F)V", at = @At("TAIL"))
   private void baulooStyles$cosmeticCape(class_11890 player, class_10055 state, float tickDelta, CallbackInfo ci) {
      if (state.field_53520 != null && Cosmetics.capeFor(player) != null) {
         state.field_53520 = Cosmetics.withCape(player, state.field_53520);
         state.field_53532 = true;
      }
   }
}
