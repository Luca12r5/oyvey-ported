package de.bauloo.styles.mixin;

import de.bauloo.styles.StylesUiConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Screen.class})
public class InventoryBackgroundMixin {
   @Inject(
      method = {"renderBackground"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void baulooStyles$clear(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      Minecraft client = Minecraft.getInstance();
      if (StylesUiConfig.invClearBackground && client.level != null && this instanceof AbstractContainerScreen) {
         client.gui.renderDeferredSubtitles();
         ci.cancel();
      }
   }
}
