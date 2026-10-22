package dev.visor.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import dev.visor.VisorController;
import dev.visor.render.FrameCapture;
import dev.visor.render.VisorOverlay;
import dev.visor.vr.VrMode;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
	/** Advance the headset timeline before anything of this frame is drawn. */
	@Inject(method = "render", at = @At("HEAD"))
	private void visor$beginFrame(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
		VisorController.get().beginFrame(deltaTracker.getGameTimeDeltaPartialTick(true));
	}

	/**
	 * Inside a VR video the world is completely covered: it is not drawn at all then (that leaves
	 * the graphics card to the video). The camera of the frame is still noted for the VR view.
	 */
	@WrapOperation(
		method = "renderLevel",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V")
	)
	private void visor$skipHiddenWorld(
		LevelRenderer levelRenderer, GraphicsResourceAllocator allocator, DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera,
		Matrix4f viewRotation, Matrix4f projection, Matrix4f cullingProjection, GpuBufferSlice fog, Vector4f fogColor, boolean renderSky,
		Operation<Void> original
	) {
		if (VrMode.get().showsVideo()) {
			FrameCapture.capture(viewRotation, projection, camera.position());
			return;
		}
		original.call(levelRenderer, allocator, deltaTracker, renderBlockOutline, camera, viewRotation, projection, cullingProjection, fog, fogColor, renderSky);
	}

	/** Third person: panels are drawn while the world's depth buffer still exists. */
	@Inject(
		method = "renderLevel",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V", shift = At.Shift.AFTER)
	)
	private void visor$renderWorldPhase(DeltaTracker deltaTracker, CallbackInfo ci) {
		VisorOverlay.get().renderWorldPhase();
	}

	/** Draw the headset layer after the world and its post effects, before the HUD and screens. */
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;endFrame()V"))
	private void visor$renderOverlay(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
		VisorOverlay.get().render();
	}
}
