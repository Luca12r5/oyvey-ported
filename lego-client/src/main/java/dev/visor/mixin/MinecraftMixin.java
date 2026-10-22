package dev.visor.mixin;

import dev.visor.VisorController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keyboard-bound attack/use keys are blocked too while the headset is on; walking stays free. */
@Mixin(Minecraft.class)
abstract class MinecraftMixin {
	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	private void visor$startAttack(CallbackInfoReturnable<Boolean> cir) {
		if (VisorController.get().blocksInteraction()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
	private void visor$continueAttack(boolean attacking, CallbackInfo ci) {
		if (VisorController.get().blocksInteraction()) {
			ci.cancel();
		}
	}

	@Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
	private void visor$startUseItem(CallbackInfo ci) {
		if (VisorController.get().blocksInteraction()) {
			ci.cancel();
		}
	}

	@Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true)
	private void visor$pickBlock(CallbackInfo ci) {
		if (VisorController.get().blocksInteraction()) {
			ci.cancel();
		}
	}
}
