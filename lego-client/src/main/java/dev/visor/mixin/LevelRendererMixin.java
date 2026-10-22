package dev.visor.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import dev.visor.render.FrameCapture;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {
	/** Remember the exact camera of this frame, so panels sit in the world without swimming. */
	@Inject(method = "renderLevel", at = @At("HEAD"))
	private void visor$captureCamera(
		GraphicsResourceAllocator allocator,
		DeltaTracker deltaTracker,
		boolean renderBlockOutline,
		Camera camera,
		Matrix4f viewRotation,
		Matrix4f projection,
		Matrix4f cullingProjection,
		GpuBufferSlice fog,
		Vector4f fogColor,
		boolean renderSky,
		CallbackInfo ci
	) {
		FrameCapture.capture(viewRotation, projection, camera.position());
	}
}
