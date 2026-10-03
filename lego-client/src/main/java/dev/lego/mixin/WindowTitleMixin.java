package dev.lego.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Shows "LEGO Client <version>" in the game window title (taskbar, alt-tab). */
@Mixin(
   value = {Minecraft.class}
)
public abstract class WindowTitleMixin {
   @Inject(
      method = {"createTitle()Ljava/lang/String;"},
      at = {@At("RETURN")},
      cancellable = true,
      require = 0
   )
   private void lego$title(CallbackInfoReturnable<String> cir) {
      try {
         String version = FabricLoader.getInstance().getModContainer("legoclient").map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("");
         int plus = version.indexOf('+');
         if (plus > 0) version = version.substring(0, plus);
         cir.setReturnValue("LEGO Client " + version + " · " + cir.getReturnValue());
      } catch (Throwable ignored) {
      }
   }
}
