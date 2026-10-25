// Minecraft 26.3 version of dev/visor/headset/HeadsetRenderer.java (renderpearl pipeline, RenderSetup without buffer size).
package dev.visor.headset;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import dev.visor.VisorClient;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

/** Submits the smooth headset mesh with its glossy material shader. */
public final class HeadsetRenderer {
	public static final RenderPipeline PIPELINE = RenderPipelines.registerOptional(RenderPipeline.builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
		.withLocation(VisorClient.id("pipeline/headset"))
		.withVertexShader(VisorClient.id("core/headset"))
		.withFragmentShader(VisorClient.id("core/headset"))
		.withBindGroupLayout(BindGroupLayout.builder().withUniform("Sampler2", UniformType.COMBINED_IMAGE_SAMPLER).build())
		// The snippet brings no attachments; 26.3 checks them against the pass (one colour target, depth).
		.withColorTargetState(new ColorTargetState(java.util.Optional.empty(), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_ALL))
		.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
		.withVertexBinding(0, DefaultVertexFormat.ENTITY)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.withCull(false)
		.build());

	public static final RenderType RENDER_TYPE = RenderType.create("visor_headset", RenderSetup.builder(PIPELINE).useLightmap().createRenderSetup());

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
