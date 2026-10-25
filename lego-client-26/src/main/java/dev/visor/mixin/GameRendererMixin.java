// Minecraft 26.3 version of dev/visor/mixin/GameRendererMixin.java (render() / renderLevel() without parameters).
package dev.visor.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import dev.visor.VisorController;
import dev.visor.render.FrameCapture;
import dev.visor.render.VisorOverlay;
import dev.visor.vr.VrMode;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
	private static final String LEVEL_RENDER =
		"Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Lorg/joml/Vector4f;ZZ)V";

	/** The level projection of this frame (with view bobbing), as handed to the projection buffer. */
	@Unique
	private final Matrix4f visor$projection = new Matrix4f();

	/** Advance the headset timeline before anything of this frame is drawn. */
	@Inject(method = "update(Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"))
	private void visor$beginFrame(DeltaTracker deltaTracker, CallbackInfo ci) {
		VisorController.get().beginFrame(deltaTracker.getGameTimeDeltaPartialTick(true));
	}

	@ModifyArg(
		method = "renderLevel()V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;")
	)
	private Matrix4f visor$captureProjection(Matrix4f projection) {
		this.visor$projection.set(projection);
		return projection;
	}

	/**
	 * Remember the exact camera of this frame, so panels sit in the world without swimming. Inside a
	 * VR video the world is completely covered: it is not drawn at all then (that leaves the
	 * graphics card to the video).
	 */
	@WrapOperation(method = "renderLevel()V", at = @At(value = "INVOKE", target = LEVEL_RENDER))
	private void visor$skipHiddenWorld(
		LevelRenderer levelRenderer, GraphicsResourceAllocator allocator, boolean renderBlockOutline, CameraRenderState camera,
		GpuBufferSlice fog, Vector4f fogColor, boolean a, boolean b, Operation<Void> original
	) {
		FrameCapture.capture(camera.viewRotationMatrix, this.visor$projection, camera.pos);
		if (VrMode.get().showsVideo()) {
			return;
		}
		original.call(levelRenderer, allocator, renderBlockOutline, camera, fog, fogColor, a, b);
	}

	/** Third person: panels are drawn while the world's depth buffer still exists. */
	@Inject(method = "renderLevel()V", at = @At(value = "INVOKE", target = LEVEL_RENDER, shift = At.Shift.AFTER))
	private void visor$renderWorldPhase(CallbackInfo ci) {
		VisorOverlay.get().renderWorldPhase();
	}

	/** Draw the headset layer after the world and its post effects, before the HUD and screens. */
	@Inject(method = "render()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;endFrame()V"))
	private void visor$renderOverlay(CallbackInfo ci) {
		VisorOverlay.get().render();
	}
}
