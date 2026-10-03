package dev.lego.mixin;

import dev.lego.core.Mc;
import dev.lego.visual.Visuals;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {ClientPacketListener.class}
)
public abstract class NetworkHandlerMixin {
   @Inject(
      method = {"handleEntityEvent(Lnet/minecraft/network/protocol/game/ClientboundEntityEventPacket;)V"},
      at = {@At("TAIL")},
      require = 0
   )
   private void lego$status(ClientboundEntityEventPacket var1, CallbackInfo var2) {
      try {
         if (Mc.world() == null) {
            return;
         }

         Entity var3 = var1.getEntity(Mc.world());
         if (var3 instanceof LivingEntity) {
            Visuals.onStatus((LivingEntity)var3, var1.getEventId());
         }
      } catch (Throwable var4) {
      }
   }
}
