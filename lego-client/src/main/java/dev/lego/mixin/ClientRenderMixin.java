package dev.lego.mixin;

import dev.lego.ui.Gx;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {Minecraft.class},
   remap = false
)
public abstract class ClientRenderMixin {
   @Inject(
      method = {"method_1523(Z)V"},
      at = {@At("HEAD")},
      remap = false,
      require = 0
   )
   private void lego$frame(boolean var1, CallbackInfo var2) {
      Gx.newFrame();
   }
}
