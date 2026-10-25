package de.bauloo.styles.mixin;

import de.bauloo.styles.PlayerOutline;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Minecraft.class})
public class PlayerOutlineMixin {
   @Inject(
      method = {"shouldEntityAppearGlowing"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void baulooStyles$playerOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
      if (PlayerOutline.outlines(entity)) {
         cir.setReturnValue(true);
      }
   }
}
