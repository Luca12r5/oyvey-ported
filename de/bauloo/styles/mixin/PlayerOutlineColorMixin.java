package de.bauloo.styles.mixin;

import de.bauloo.styles.PlayerOutline;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({EntityRenderer.class})
public class PlayerOutlineColorMixin {
   @Redirect(
      method = {"extractRenderState"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/Entity;getTeamColor()I"
      )
   )
   private int baulooStyles$outlineColor(Entity entity) {
      return PlayerOutline.outlines(entity) ? PlayerOutline.color() : entity.getTeamColor();
   }
}
