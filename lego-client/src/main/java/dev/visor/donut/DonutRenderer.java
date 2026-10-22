package dev.visor.donut;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.visor.VisorClient;
import dev.visor.render.UiResources;
import dev.visor.render.UiTexture;
import dev.visor.render.VisorGpu;
import dev.visor.render.VisorPipelines;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

/**
 * Draws Doctor Donut. The mesh lives on the GPU and is skinned in the vertex shader, so a frame
 * only uploads his bone matrices (a few kilobytes) instead of the whole mesh.
 */
public final class DonutRenderer implements AutoCloseable {
	public static final Identifier FRONT = VisorClient.id("textures/doctor_donut/front.png");
	public static final Identifier BACK = VisorClient.id("textures/doctor_donut/back.png");
	private static final Identifier[] TEXTURES = {FRONT, BACK};
	private static final int STRIDE = 16 * Float.BYTES;
	private static final int UNIFORM_SIZE = DonutModel.MAX_BONES * 16 * Float.BYTES + 8 * Float.BYTES;

	public static final VertexFormat FORMAT = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("UV0", VertexFormatElement.UV0)
		.add("BindNormal", VisorPipelines.BIND_NORMAL)
		.add("Joints", VisorPipelines.JOINTS)
		.add("Weights", VisorPipelines.WEIGHTS)
		.build();

	public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
		.withLocation(VisorClient.id("pipeline/doctor_donut"))
		.withVertexShader(VisorClient.id("core/doctor_donut"))
		.withFragmentShader(VisorClient.id("core/doctor_donut"))
		.withSampler("Sampler0")
		.withSampler("Sampler2")
		.withUniform("DonutData", UniformType.UNIFORM_BUFFER)
		.withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
		.withDepthWrite(true)
		.withCull(false)
		.withoutBlend()
		.withVertexFormat(FORMAT, VertexFormat.Mode.TRIANGLES)
		.build();

	/** Each caller gets its own bone buffer, so several poses can be drawn in the same frame. */
	public enum Slot { WORLD, PREVIEW, CARD }

	private final DonutModel model;
	private final GpuBuffer[] vertices;
	private final GpuBuffer[] indices;
	private final int[] indexCounts;
	private final GpuBuffer[] uniforms = new GpuBuffer[Slot.values().length];
	private final float[] matrices = new float[DonutModel.MAX_BONES * 16];
	private final ByteBuffer uniformData = MemoryUtil.memAlloc(UNIFORM_SIZE);

	public DonutRenderer(DonutModel model) {
		this.model = model;
		GpuDevice device = RenderSystem.getDevice();
		DonutModel.Part[] parts = model.parts();
		this.vertices = new GpuBuffer[parts.length];
		this.indices = new GpuBuffer[parts.length];
		this.indexCounts = new int[parts.length];
		for (int p = 0; p < parts.length; p++) {
			DonutModel.Part part = parts[p];
			ByteBuffer vertexData = MemoryUtil.memAlloc(part.vertexCount() * STRIDE);
			ByteBuffer indexData = MemoryUtil.memAlloc(part.indices().length * Integer.BYTES);
			try {
				DonutModel.writeVertices(part, vertexData);
				vertexData.flip();
				for (int index : part.indices()) {
					indexData.putInt(index);
				}
				indexData.flip();
				int number = p;
				this.vertices[p] = device.createBuffer(() -> "Visor Doctor Donut vertices " + number, GpuBuffer.USAGE_VERTEX, vertexData);
				this.indices[p] = device.createBuffer(() -> "Visor Doctor Donut indices " + number, GpuBuffer.USAGE_INDEX, indexData);
				this.indexCounts[p] = part.indices().length;
			} finally {
				MemoryUtil.memFree(vertexData);
				MemoryUtil.memFree(indexData);
			}
		}
		for (Slot slot : Slot.values()) {
			this.uniforms[slot.ordinal()] = device.createBuffer(
				() -> "Visor Doctor Donut bones " + slot, GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, UNIFORM_SIZE
			);
		}
	}

	public DonutModel model() {
		return this.model;
	}

	/** Both textures are loaded (they come with the UI resources). */
	public boolean texturesReady() {
		for (Identifier texture : TEXTURES) {
			if (UiResources.image(texture) == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Draws the model posed at {@code time} of {@code clip}. The model stands with its feet at the
	 * origin and looks along +Z; {@code modelView} puts it in front of the camera.
	 *
	 * @param clear      clear colour (to transparent) and depth first, for an off-screen picture
	 * @param blockLight block light 0-15 for the lightmap, or -1 to draw at full brightness
	 * @param glowColor  colour (ARGB) of the rim of light he gets while the pointer is on him
	 * @param glow       strength of that rim, 0 for none
	 */
	public void draw(
		Slot slot, GpuTextureView color, GpuTextureView depth, boolean clear, Matrix4fc modelView, GpuBufferSlice projection,
		String clip, double time, int blockLight, int skyLight, int glowColor, float glow
	) {
		if (!VisorGpu.get().usable(PIPELINE) || !this.texturesReady()) {
			return;
		}
		this.model.pose(clip, time, this.matrices);
		ByteBuffer data = this.uniformData.clear();
		for (int i = 0; i < this.model.boneCount() * 16; i++) {
			data.putFloat(this.matrices[i]);
		}
		data.position(DonutModel.MAX_BONES * 16 * Float.BYTES);
		data.putFloat(Math.max(0, blockLight)).putFloat(Math.max(0, skyLight)).putFloat(blockLight >= 0 ? 1.0F : 0.0F).putFloat(0.0F);
		data.putFloat(((glowColor >> 16) & 0xFF) / 255.0F).putFloat(((glowColor >> 8) & 0xFF) / 255.0F).putFloat((glowColor & 0xFF) / 255.0F)
			.putFloat(Math.max(0.0F, Math.min(1.0F, glow)));
		data.flip();

		GpuBuffer uniform = this.uniforms[slot.ordinal()];
		CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
		encoder.writeToBuffer(uniform.slice(), data);
		GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
			.writeTransform(modelView, new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());
		GpuTextureView lightmap = Minecraft.getInstance().gameRenderer.lightTexture().getTextureView();
		try (RenderPass pass = encoder.createRenderPass(
			() -> "Visor Doctor Donut", color, clear ? OptionalInt.of(0) : OptionalInt.empty(), depth, clear ? OptionalDouble.of(1.0) : OptionalDouble.empty()
		)) {
			pass.setPipeline(PIPELINE);
			pass.setUniform("Projection", projection);
			pass.setUniform("DynamicTransforms", transforms);
			pass.setUniform("DonutData", uniform);
			pass.bindTexture("Sampler2", lightmap, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
			for (int p = 0; p < this.vertices.length; p++) {
				UiTexture texture = UiResources.image(TEXTURES[p % TEXTURES.length]);
				pass.bindTexture("Sampler0", texture.view(), texture.sampler());
				pass.setVertexBuffer(0, this.vertices[p]);
				pass.setIndexBuffer(this.indices[p], VertexFormat.IndexType.INT);
				pass.drawIndexed(0, 0, this.indexCounts[p], 1);
			}
		}
	}

	@Override
	public void close() {
		for (GpuBuffer buffer : this.vertices) {
			buffer.close();
		}
		for (GpuBuffer buffer : this.indices) {
			buffer.close();
		}
		for (GpuBuffer buffer : this.uniforms) {
			buffer.close();
		}
		MemoryUtil.memFree(this.uniformData);
	}
}
