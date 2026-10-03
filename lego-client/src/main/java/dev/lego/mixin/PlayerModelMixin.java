package dev.lego.mixin;

import dev.lego.core.Mc;
import dev.lego.cosmetic.VehicleWorld;
import dev.lego.emote.Emotes;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {PlayerModel.class}
)
public abstract class PlayerModelMixin {
   @Inject(
      method = {"setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V"},
      at = {@At("TAIL")},
      require = 0
   )
   private void lego$emote(AvatarRenderState var1, CallbackInfo var2) {
      try {
         if (Mc.player() == null || var1.id != Mc.id(Mc.player())) {
            return;
         }

         HumanoidModel var3 = (HumanoidModel)(Object)this;
         if (Emotes.active()) {
            Emotes.pose(var3.head, var3.body, var3.rightArm, var3.leftArm, var3.rightLeg, var3.leftLeg);
         }

         VehicleWorld.pose(var3.head, var3.body, var3.rightArm, var3.leftArm, var3.rightLeg, var3.leftLeg);
      } catch (Throwable var4) {
      }
   }
}
