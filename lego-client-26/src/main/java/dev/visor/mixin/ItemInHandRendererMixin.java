// Minecraft 26.3 version of dev/visor/mixin/ItemInHandRendererMixin.java (FirstPersonHandsAndItemsRenderer).
package dev.visor.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.visor.VisorController;
import dev.visor.headset.FirstPersonHeadset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
abstract class ItemInHandRendererMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	/**
	 * While the headset is involved the normal first person hands are replaced: by the arms that
	 * carry the headset during the animations, and by the ghost hands (drawn later) while it is on.
	 * 26.3 renders the submitted hand pass right after this method, so nothing has to be flushed here.
	 */
	@Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
	private void visor$replaceHands(
		float partialTick, PoseStack poseStack, SubmitNodeCollector collector, PlayerRenderState playerState,
		FirstPersonHandsAndItemsRenderState handsState, CallbackInfo ci
	) {
		VisorController controller = VisorController.get();
		VisorController.State state = controller.state();
		if (state == VisorController.State.OFF) {
			return;
		}
		ci.cancel();
		LocalPlayer player = this.minecraft.player;
		if (state == VisorController.State.ON || player == null || this.minecraft.level == null) {
			return;
		}
		int light = LightCoordsUtil.getLightCoords(this.minecraft.level, BlockPos.containing(player.getEyePosition(partialTick)));
		// Same subtle lag as vanilla's arms when turning, so the headset feels held.
		float xBob = Mth.lerp(partialTick, player.xBobO, player.xBob);
		float yBob = Mth.lerp(partialTick, player.yBobO, player.yBob);
		poseStack.pushPose();
		poseStack.rotate(Axis.XP.rotationDegrees((player.getViewXRot(partialTick) - xBob) * 0.1F));
		poseStack.rotate(Axis.YP.rotationDegrees((player.getViewYRot(partialTick) - yBob) * 0.1F));
		FirstPersonHeadset.render((float) controller.stateTime(), state == VisorController.State.REMOVING, poseStack, collector, light);
		poseStack.popPose();
	}
}
