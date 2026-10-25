// Minecraft 26.3 version of dev/visor/mixin/GuiMixin.java (the HUD is its own class in 26.3).
package dev.visor.mixin;

import dev.visor.VisorController;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
abstract class GuiMixin {
	/** The normal HUD (hotbar, crosshair, hearts) is replaced by the headset view. */
	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	private void visor$hideHud(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (VisorController.get().active()) {
			ci.cancel();
		}
	}
}
