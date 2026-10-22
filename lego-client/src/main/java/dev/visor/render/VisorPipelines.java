package dev.visor.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.visor.VisorClient;
import net.minecraft.client.renderer.RenderPipelines;

/**
 * Render pipelines and the vertex format used by the Visor renderer.
 *
 * <p>The UI is drawn by a single "uber" shader: every quad carries its own shape description
 * (rounded rectangle, border, soft shadow, SDF glyph, image or procedural gradient), so a whole
 * panel - backgrounds, text, icons and pictures - goes to the GPU in a handful of draw calls while
 * keeping exact painter's order.</p>
 */
public final class VisorPipelines {
	/** Local pixel position relative to the shape centre (vec2). */
	public static final VertexFormatElement LOCAL = registerElement(3, 2);
	/** halfWidth, halfHeight, cornerRadius, stroke width / blur radius (vec4). */
	public static final VertexFormatElement SHAPE = registerElement(4, 4);
	/** mode, param1, param2, param3 (vec4). */
	public static final VertexFormatElement EXTRA = registerElement(5, 4);
	/** Skinned meshes: rest pose normal (vec3). */
	public static final VertexFormatElement BIND_NORMAL = registerElement(6, 3);
	/** Skinned meshes: four bone indices (vec4). */
	public static final VertexFormatElement JOINTS = registerElement(7, 4);
	/** Skinned meshes: the weights of those four bones (vec4). */
	public static final VertexFormatElement WEIGHTS = registerElement(8, 4);

	public static final VertexFormat UI_FORMAT = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("Color", VertexFormatElement.COLOR)
		.add("UV0", VertexFormatElement.UV0)
		.add("Local", LOCAL)
		.add("Shape", SHAPE)
		.add("Extra", EXTRA)
		.build();

	/** Panels, text and icons, in world space (curved surfaces) or screen space. */
	public static final RenderPipeline UI = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
		.withLocation(VisorClient.id("pipeline/ui"))
		.withVertexShader(VisorClient.id("core/ui"))
		.withFragmentShader(VisorClient.id("core/ui"))
		.withSampler("Sampler0")
		.withSampler("Sampler1")
		.withUniform("VisorUi", UniformType.UNIFORM_BUFFER)
		.withBlend(BlendFunction.TRANSLUCENT)
		.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
		.withDepthWrite(false)
		.withCull(false)
		.withVertexFormat(UI_FORMAT, VertexFormat.Mode.QUADS)
		.build();

	/** Same as {@link #UI} but hidden behind closer world geometry; used in third person. */
	public static final RenderPipeline UI_DEPTH = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
		.withLocation(VisorClient.id("pipeline/ui_depth"))
		.withVertexShader(VisorClient.id("core/ui"))
		.withFragmentShader(VisorClient.id("core/ui"))
		.withSampler("Sampler0")
		.withSampler("Sampler1")
		.withUniform("VisorUi", UniformType.UNIFORM_BUFFER)
		.withBlend(BlendFunction.TRANSLUCENT)
		.withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
		.withDepthWrite(false)
		.withCull(false)
		.withVertexFormat(UI_FORMAT, VertexFormat.Mode.QUADS)
		.build();

	/** Hand-tracking style "ghost hands", evaluated per pixel from a list of capsules. */
	public static final RenderPipeline HANDS = RenderPipeline.builder()
		.withLocation(VisorClient.id("pipeline/hands"))
		.withVertexShader(VisorClient.id("core/screen_quad"))
		.withFragmentShader(VisorClient.id("core/hands"))
		.withUniform("HandData", UniformType.UNIFORM_BUFFER)
		.withBlend(BlendFunction.TRANSLUCENT)
		.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
		.withDepthWrite(false)
		.withCull(false)
		.withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
		.build();

	/** Full screen headset effects: lens vignette, blackout and the lens mask while putting it on. */
	public static final RenderPipeline FX = RenderPipeline.builder()
		.withLocation(VisorClient.id("pipeline/fx"))
		.withVertexShader(VisorClient.id("core/screen_quad"))
		.withFragmentShader(VisorClient.id("core/fx"))
		.withUniform("FxData", UniformType.UNIFORM_BUFFER)
		.withBlend(BlendFunction.TRANSLUCENT)
		.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
		.withDepthWrite(false)
		.withCull(false)
		.withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
		.build();

	/** A VR video all around the player (see vr.fsh), drawn over the whole screen instead of the world. */
	public static final RenderPipeline VR = RenderPipeline.builder()
		.withLocation(VisorClient.id("pipeline/vr"))
		.withVertexShader(VisorClient.id("core/screen_quad"))
		.withFragmentShader(VisorClient.id("core/vr"))
		.withSampler("Sampler0")
		.withUniform("VrData", UniformType.UNIFORM_BUFFER)
		.withBlend(BlendFunction.TRANSLUCENT)
		.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
		.withDepthWrite(false)
		.withCull(false)
		.withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
		.build();

	private VisorPipelines() {
	}

	/**
	 * Registers an extra float vertex attribute. Other mods (shader loaders in particular) also add
	 * elements, so the id is picked from the free slots instead of being hard coded.
	 */
	private static VertexFormatElement registerElement(int uvIndex, int count) {
		for (int id = VertexFormatElement.MAX_COUNT - 1; id > 6; id--) {
			if (VertexFormatElement.byId(id) == null) {
				return VertexFormatElement.register(id, uvIndex, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, count);
			}
		}
		throw new IllegalStateException("No free vertex format element id left for Visor");
	}

	/** Forces class initialisation (and with it the element registration) at a predictable time. */
	public static void init() {
	}
}
