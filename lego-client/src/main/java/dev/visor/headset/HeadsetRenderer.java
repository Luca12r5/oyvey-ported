package dev.visor.headset;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.visor.VisorClient;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

/** Submits the smooth headset mesh with its glossy material shader. */
public final class HeadsetRenderer {
	public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
		.withLocation(VisorClient.id("pipeline/headset"))
		.withVertexShader(VisorClient.id("core/headset"))
		.withFragmentShader(VisorClient.id("core/headset"))
		.withSampler("Sampler2")
		.withVertexFormat(DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS)
		.withCull(false)
		.build();

	public static final RenderType RENDER_TYPE = RenderType.create(
		"visor_headset", RenderSetup.builder(PIPELINE).useLightmap().bufferSize(262144).createRenderSetup()
	);

	private HeadsetRenderer() {
	}

	/**
	 * @param worn true for the version with the full strap around the head
	 */
	public static void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, boolean worn) {
		HeadsetMesh mesh = worn ? HeadsetMesh.worn() : HeadsetMesh.held();
		collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, consumer) -> mesh.emit(pose, consumer, light));
	}
}
