package dev.visor.headset;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.visor.VisorController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

/** Draws the headset on the local player's head (third person, inventory preview). */
public final class HeadsetLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	public HeadsetLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
		super(parent);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || state.id != minecraft.player.getId() || state.isInvisible || !VisorController.get().wornOnHead()) {
			return;
		}
		PlayerModel model = this.getParentModel();
		poseStack.pushPose();
		model.root().translateAndRotate(poseStack);
		model.getHead().translateAndRotate(poseStack);
		// Model space is y-down; the mesh is y-up with the lenses along -Z: a half turn around Z fixes that.
		poseStack.translate(0.0F, -4.4F / 16.0F, (float) -HeadsetMesh.FACE_OFFSET);
		poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		HeadsetRenderer.submit(poseStack, collector, light, true);
		poseStack.popPose();
	}
}
