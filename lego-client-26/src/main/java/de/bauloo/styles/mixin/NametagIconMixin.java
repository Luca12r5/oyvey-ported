package de.bauloo.styles.mixin;

import de.bauloo.styles.StylesPresence;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {EntityRenderer.class},
   priority = 2000
)
public class NametagIconMixin {
   @Inject(
      method = {"extractRenderState"},
      at = {@At("TAIL")}
   )
   private void baulooStyles$tornado(Entity entity, EntityRenderState state, float tickProgress, CallbackInfo ci) {
      if (state.nameTag != null && entity instanceof Player) {
         Component icon = StylesPresence.icon(entity.getUUID());
         if (icon != null) {
            state.nameTag = Component.empty().append(icon).append(" ").append(state.nameTag);
         }
      }
   }
}
