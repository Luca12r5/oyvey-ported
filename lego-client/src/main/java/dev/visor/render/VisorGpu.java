package dev.visor.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.visor.VisorClient;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.DynamicUniformStorage;
import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

/** Shared GPU plumbing: uniform buffers, projection buffers and small draw helpers. */
public final class VisorGpu {
	private static VisorGpu instance;

	final DynamicUniformStorage<RawUniform> uiUniforms = new DynamicUniformStorage<>("Visor UI UBO", 2 * 16, 8);
	final DynamicUniformStorage<RawUniform> handUniforms = new DynamicUniformStorage<>("Visor hands UBO", GhostHands.UBO_VEC4S * 16, 2);
	final DynamicUniformStorage<RawUniform> fxUniforms = new DynamicUniformStorage<>("Visor fx UBO", 4 * 16, 2);
	final DynamicUniformStorage<RawUniform> vrUniforms = new DynamicUniformStorage<>("Visor VR UBO", 10 * 16, 2);
	final PerspectiveProjectionMatrixBuffer worldProjection = new PerspectiveProjectionMatrixBuffer("visor world");
	final CachedOrthoProjectionMatrixBuffer screenProjection = new CachedOrthoProjectionMatrixBuffer("visor screen", -1000.0F, 1000.0F, true);
	private final ByteBuffer quadScratch = MemoryUtil.memAlloc(4 * 3 * 4 * 8).order(ByteOrder.nativeOrder());
	private final Set<RenderPipeline> failedPipelines = new HashSet<>();

	private VisorGpu() {
	}

	public static VisorGpu get() {
		if (instance == null) {
			instance = new VisorGpu();
		}
		return instance;
	}

	public void endFrame() {
		this.uiUniforms.endFrame();
		this.handUniforms.endFrame();
		this.fxUniforms.endFrame();
		this.vrUniforms.endFrame();
	}

	/** False (and logged once) when a pipeline's shaders failed to compile on this driver. */
	public boolean usable(RenderPipeline pipeline) {
		if (this.failedPipelines.contains(pipeline)) {
			return false;
		}
		if (!RenderSystem.getDevice().precompilePipeline(pipeline).isValid()) {
			this.failedPipelines.add(pipeline);
			VisorClient.LOGGER.error("Visor pipeline {} failed to compile; that layer will not be drawn", pipeline.getLocation());
			return false;
		}
		return true;
	}

	public void resetPipelineState() {
		this.failedPipelines.clear();
	}

	GpuBufferSlice transforms(Matrix4fc modelView) {
		return RenderSystem.getDynamicUniforms().writeTransform(modelView, new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());
	}

	/**
	 * Draws axis aligned quads given in normalized device coordinates (x0, y0, x1, y1 each) with a
	 * screen-space pipeline and one uniform block.
	 */
	void drawScreenQuads(RenderPipeline pipeline, GpuTextureView target, String uniformName, GpuBufferSlice uniform, float[] rects) {
		this.drawScreenQuads(pipeline, target, uniformName, uniform, rects, null, null, null);
	}

	/** Like {@link #drawScreenQuads(RenderPipeline, GpuTextureView, String, GpuBufferSlice, float[])}, with one texture bound. */
	void drawScreenQuads(
		RenderPipeline pipeline, GpuTextureView target, String uniformName, GpuBufferSlice uniform, float[] rects,
		@Nullable String samplerName, @Nullable GpuTextureView texture, @Nullable GpuSampler sampler
	) {
		int quads = rects.length / 4;
		if (quads == 0) {
			return;
		}
		ByteBuffer data = this.quadScratch.clear();
		for (int q = 0; q < quads; q++) {
			float x0 = rects[q * 4];
			float y0 = rects[q * 4 + 1];
			float x1 = rects[q * 4 + 2];
			float y1 = rects[q * 4 + 3];
			data.putFloat(x0).putFloat(y0).putFloat(0.0F);
			data.putFloat(x0).putFloat(y1).putFloat(0.0F);
			data.putFloat(x1).putFloat(y1).putFloat(0.0F);
			data.putFloat(x1).putFloat(y0).putFloat(0.0F);
		}
		data.flip();
		GpuBuffer vertices = DefaultVertexFormat.POSITION.uploadImmediateVertexBuffer(data);
		RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
		GpuBuffer indexBuffer = indices.getBuffer(quads * 6);
		try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Visor " + uniformName, target, OptionalInt.empty())) {
			pass.setPipeline(pipeline);
			pass.setUniform(uniformName, uniform);
			if (samplerName != null && texture != null && sampler != null) {
				pass.bindTexture(samplerName, texture, sampler);
			}
			pass.setVertexBuffer(0, vertices);
			pass.setIndexBuffer(indexBuffer, indices.type());
			pass.drawIndexed(0, 0, quads * 6, 1);
		}
	}

	/** A run of UI quads that share one image (or the white texture when {@code image} is null). */
	public record UiBatch(int firstVertex, int vertexCount, GpuTextureView image, GpuSampler sampler) {
	}

	/** Submits recorded UI quads: one pass, the font atlas in Sampler0 and each batch's image in Sampler1. */
	void drawUi(
		RenderPipeline pipeline, GpuTextureView target, @Nullable GpuTextureView depth, GpuBufferSlice projection, GpuBufferSlice transforms,
		GpuBufferSlice uniforms, ByteBuffer mesh, int vertexCount, UiTexture font, UiTexture white, java.util.List<UiBatch> batches
	) {
		GpuBuffer vertices = VisorPipelines.UI_FORMAT.uploadImmediateVertexBuffer(mesh);
		RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
		GpuBuffer indexBuffer = indices.getBuffer(vertexCount / 4 * 6);
		try (RenderPass pass = RenderSystem.getDevice()
				.createCommandEncoder()
				.createRenderPass(() -> "Visor UI", target, OptionalInt.empty(), depth, java.util.OptionalDouble.empty())) {
			pass.setPipeline(pipeline);
			pass.setUniform("Projection", projection);
			pass.setUniform("DynamicTransforms", transforms);
			pass.setUniform("VisorUi", uniforms);
			pass.setVertexBuffer(0, vertices);
			pass.setIndexBuffer(indexBuffer, indices.type());
			pass.bindTexture("Sampler0", font.view(), font.sampler());
			for (UiBatch batch : batches) {
				if (batch.image() != null) {
					pass.bindTexture("Sampler1", batch.image(), batch.sampler());
				} else {
					pass.bindTexture("Sampler1", white.view(), white.sampler());
				}
				pass.drawIndexed(0, batch.firstVertex() / 4 * 6, batch.vertexCount() / 4 * 6, 1);
			}
		}
	}

	/** A uniform block written from a flat list of vec4s. */
	public record RawUniform(float[] vec4s) implements DynamicUniformStorage.DynamicUniform {
		@Override
		public void write(ByteBuffer buffer) {
			Std140Builder builder = Std140Builder.intoBuffer(buffer);
			for (int i = 0; i + 3 < this.vec4s.length; i += 4) {
				builder.putVec4(this.vec4s[i], this.vec4s[i + 1], this.vec4s[i + 2], this.vec4s[i + 3]);
			}
		}

		@Override
		public boolean equals(Object other) {
			return this == other;
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}
	}
}
