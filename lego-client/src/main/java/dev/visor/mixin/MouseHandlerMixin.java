package dev.visor.mixin;

import dev.visor.VisorController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
abstract class MouseHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	private boolean mouseGrabbed;

	/** Clicks go to the headset UI instead of attacking / using items. */
	@Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
	private void visor$onButton(long window, MouseButtonInfo button, int action, CallbackInfo ci) {
		if (window != this.minecraft.getWindow().handle() || this.minecraft.getOverlay() != null || !this.mouseGrabbed) {
			return;
		}
		if (VisorController.get().onMouseButton(button.button(), action == 1)) {
			ci.cancel();
		}
	}

	/** Scrolling while pointing at a panel scrolls the panel, not the hotbar. */
	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void visor$onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
		if (window != this.minecraft.getWindow().handle() || this.minecraft.getOverlay() != null) {
			return;
		}
		double amount = vertical != 0.0 ? vertical : horizontal;
		if (VisorController.get().onScroll(amount)) {
			ci.cancel();
		}
	}
}
