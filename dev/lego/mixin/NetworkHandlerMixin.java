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
   value = {ClientPacketListener.class},
   remap = false
)
public abstract class NetworkHandlerMixin {
   @Inject(
      method = {"method_11148(Lnet/minecraft/class_2663;)V"},
      at = {@At("TAIL")},
      remap = false,
      require = 0
   )
   private void lego$status(ClientboundEntityEventPacket packet, CallbackInfo ci) {
      try {
         if (Mc.world() == null) {
            return;
         }

         Entity e = packet.getEntity(Mc.world());
         if (e instanceof LivingEntity) {
            Visuals.onStatus((LivingEntity)e, packet.getEventId());
         }
      } catch (Throwable var4) {
      }
   }
}
