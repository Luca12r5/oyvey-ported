// Minecraft 26.3 version of dev/visor/render/VisorPipelines.java (renderpearl pipelines and vertex formats).
package dev.visor.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import dev.visor.VisorClient;
import java.util.Optional;
import net.minecraft.client.renderer.RenderPipelines;

/**
 * Render pipelines and the vertex format used by the Visor renderer.
 *
 * <p>The UI is drawn by a single "uber" shader: every quad carries its own shape description
 * (rounded rectangle, border, soft shadow, SDF glyph, image or procedural gradient), so a whole
 * panel - backgrounds, text, icons and pictures - goes to the GPU in a handful of draw calls while
 * keeping exact painter's order.</p>
 *
 * <p>26.3: attributes are named entries of the vertex format (no global element registry any
 * more); the shader inputs match them by order (locations are added when the shaders are merged).</p>
 */
public final class VisorPipelines {
	/** Same layout as on 1.21.11: position, colour, uv, then local (vec2), shape (vec4), extra (vec4). */
	public static final VertexFormat UI_FORMAT = VertexFormat.builder(0)
		.addAttribute("Position", GpuFormat.RGB32_FLOAT)
		.addAttribute("Color", GpuFormat.RGBA8_UNORM)
		.addAttribute("UV0", GpuFormat.RG32_FLOAT)
		.addAttribute("Local", GpuFormat.RG32_FLOAT)
		.addAttribute("Shape", GpuFormat.RGBA32_FLOAT)
		.addAttribute("Extra", GpuFormat.RGBA32_FLOAT)
		.build();

	/** Panels, text and icons, in world space (curved surfaces) or screen space. */
	public static final RenderPipeline UI = RenderPipelines.registerOptional(ui("pipeline/ui", Optional.empty()));

	/** Same as {@link #UI} but hidden behind closer world geometry; used in third person. */
	public static final RenderPipeline UI_DEPTH = RenderPipelines.registerOptional(
		ui("pipeline/ui_depth", Optional.of(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false)))
	);

	/** Hand-tracking style "ghost hands", evaluated per pixel from a list of capsules. */
	public static final RenderPipeline HANDS = RenderPipelines.registerOptional(screen("pipeline/hands", "core/hands", "HandData", false));

	/** Full screen headset effects: lens vignette, blackout and the lens mask while putting it on. */
	public static final RenderPipeline FX = RenderPipelines.registerOptional(screen("pipeline/fx", "core/fx", "FxData", false));

	/** A VR video all around the player (see vr.fsh), drawn over the whole screen instead of the world. */
	public static final RenderPipeline VR = RenderPipelines.registerOptional(screen("pipeline/vr", "core/vr", "VrData", true));

	private VisorPipelines() {
	}

	private static RenderPipeline ui(String location, Optional<DepthStencilState> depth) {
		return RenderPipeline.builder()
			.withLocation(VisorClient.id(location))
			.withVertexShader(VisorClient.id("core/ui"))
			.withFragmentShader(VisorClient.id("core/ui"))
			.withBindGroupLayout(BindGroupLayout.builder()
				.withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
				.withUniform("Projection", UniformType.UNIFORM_BUFFER)
				.withUniform("VisorUi", UniformType.UNIFORM_BUFFER)
				.withUniform("Sampler0", UniformType.COMBINED_IMAGE_SAMPLER)
				.withUniform("Sampler1", UniformType.COMBINED_IMAGE_SAMPLER)
				.build())
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withDepthStencilState(depth)
			.withCull(false)
			.withVertexBinding(0, UI_FORMAT)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.build();
	}

	private static RenderPipeline screen(String location, String fragment, String uniform, boolean sampler) {
		BindGroupLayout.Builder layout = BindGroupLayout.builder().withUniform(uniform, UniformType.UNIFORM_BUFFER);
		if (sampler) {
			layout.withUniform("Sampler0", UniformType.COMBINED_IMAGE_SAMPLER);
		}
		return RenderPipeline.builder()
			.withLocation(VisorClient.id(location))
			.withVertexShader(VisorClient.id("core/screen_quad"))
			.withFragmentShader(VisorClient.id(fragment))
			.withBindGroupLayout(layout.build())
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withDepthStencilState(Optional.empty())
			.withCull(false)
			.withVertexBinding(0, DefaultVertexFormat.POSITION)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.build();
	}

	/** Forces class initialisation (and with it the pipeline registration) at a predictable time. */
	public static void init() {
	}
}
