package dev.visor.mixin;

import dev.visor.VisorController;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
abstract class GuiMixin {
	/** The normal HUD (hotbar, crosshair, hearts) is replaced by the headset view. */
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void visor$hideHud(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (VisorController.get().active()) {
			ci.cancel();
		}
	}
}
