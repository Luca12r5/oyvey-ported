package dev.visor.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.visor.VisorController;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
abstract class KeyboardHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	/** While a key binding is being set or a search box is focused, keys go to the headset UI. */
	@Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
	private void visor$captureKey(long window, int action, KeyEvent event, CallbackInfo ci) {
		if (window == this.minecraft.getWindow().handle() && VisorController.get().onKey(event, action)) {
			ci.cancel();
		}
	}

	@Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
	private void visor$captureChar(long window, CharacterEvent event, CallbackInfo ci) {
		if (window == this.minecraft.getWindow().handle() && VisorController.get().onChar(event)) {
			ci.cancel();
		}
	}

	/** Escape closes headset menus / takes the headset off instead of opening the pause menu. */
	@WrapOperation(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pauseGame(Z)V"))
	private void visor$escape(Minecraft minecraft, boolean pauseOnly, Operation<Void> original) {
		if (!VisorController.get().onEscape()) {
			original.call(minecraft, pauseOnly);
		}
	}
}
