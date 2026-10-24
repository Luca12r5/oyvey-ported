package dev.lego.mixin;

import dev.lego.core.Mc;
import dev.lego.cosmetic.VehicleWorld;
import dev.lego.emote.Emotes;
import dev.lego.net.LegoSync;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {PlayerModel.class},
   remap = false
)
public abstract class PlayerModelMixin {
   @Inject(
      method = {"method_62110(Lnet/minecraft/class_10055;)V"},
      at = {@At("TAIL")},
      remap = false,
      require = 0
   )
   private void lego$emote(AvatarRenderState state, CallbackInfo ci) {
      try {
         if (Mc.player() == null) {
            return;
         }

         HumanoidModel<?> m = (HumanoidModel<?>)this;
         if (state.id != Mc.id(Mc.player())) {
            Entity other = Mc.world() == null ? null : Mc.world().getEntity(state.id);
            if (other == null) {
               return;
            }

            String emote = LegoSync.emote(other.getUUID());
            if (emote != null) {
               Emotes.poseOther(emote, LegoSync.emoteStart(other.getUUID()), m.head, m.body, m.rightArm, m.leftArm, m.rightLeg, m.leftLeg);
            }

            return;
         }

         if (Emotes.active()) {
            Emotes.pose(m.head, m.body, m.rightArm, m.leftArm, m.rightLeg, m.leftLeg);
         }

         VehicleWorld.pose(m.head, m.body, m.rightArm, m.leftArm, m.rightLeg, m.leftLeg);
      } catch (Throwable var6) {
      }
   }
}
