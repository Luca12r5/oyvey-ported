package dev.lego.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A "LEGO" button in the pause menu: opens the click GUI without any key. */
@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
   protected PauseScreenMixin(Component title) {
      super(title);
   }

   @Inject(method = "init", at = @At("TAIL"), require = 0)
   private void lego$benchButton(CallbackInfo ci) {
      this.addRenderableWidget(
         Button.builder(Component.literal("LEGO"), b -> dev.lego.ui.LegoScreen.show(dev.lego.ui.bench.BenchView.at(null)))
            .bounds(this.width - 66, 6, 60, 20)
            .build()
      );
   }
}
