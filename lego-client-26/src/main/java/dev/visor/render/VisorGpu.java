// Minecraft 26.3 version of dev/visor/render/VisorGpu.java (renderpearl: transient memory, compiled pipelines).
package dev.visor.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import dev.visor.VisorClient;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

/** Shared GPU plumbing: uniform buffers, projection buffers and small draw helpers. */
public final class VisorGpu {
	/** Offset alignment for uniform blocks (the largest minUniformBufferOffsetAlignment in practice). */
	private static final long UNIFORM_ALIGNMENT = 256L;
	private static final long VERTEX_ALIGNMENT = 16L;
	private static VisorGpu instance;

	final UniformStorage uiUniforms = new UniformStorage();
	final UniformStorage handUniforms = new UniformStorage();
	final UniformStorage fxUniforms = new UniformStorage();
	final UniformStorage vrUniforms = new UniformStorage();
	final WorldProjection worldProjection = new WorldProjection();
	final ScreenProjection screenProjection = new ScreenProjection();
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

	/** Uniforms live in the frame's transient memory: nothing to recycle by hand. */
	public void endFrame() {
	}

	/** False (and logged once) when a pipeline's shaders failed to compile on this driver. */
	public boolean usable(RenderPipeline pipeline) {
		if (this.failedPipelines.contains(pipeline)) {
			return false;
		}
		boolean ok;
		try {
			ok = RenderSystem.getCompiledPipelineNullable(pipeline) != null;
		} catch (RuntimeException e) {
			ok = false;
		}
		if (!ok) {
			this.failedPipelines.add(pipeline);
			VisorClient.LOGGER.error("Visor pipeline {} failed to compile; that layer will not be drawn", pipeline.getLocation());
		}
		return ok;
	}

	public void resetPipelineState() {
		this.failedPipelines.clear();
	}

	GpuBufferSlice transforms(Matrix4fc modelView) {
		return RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(modelView));
	}

	private static CommandEncoder encoder() {
		return RenderSystem.getDevice().createCommandEncoder();
	}

	static GpuBufferSlice uploadVertices(ByteBuffer data) {
		return encoder().transientMemory().uploadGpu(data, VERTEX_ALIGNMENT, GpuBuffer.USAGE_VERTEX);
	}

	static RenderPass pass(String label, GpuTextureView target, @Nullable GpuTextureView depth) {
		return depth == null
			? encoder().createRenderPass(() -> label, target, Optional.empty())
			: encoder().createRenderPass(() -> label, target, Optional.empty(), depth, OptionalDouble.empty());
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
		if (quads == 0 || !this.usable(pipeline)) {
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
		GpuBufferSlice vertices = uploadVertices(data);
		RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
		GpuBuffer indexBuffer = indices.getBuffer(quads * 6);
		try (RenderPass pass = pass("Visor " + uniformName, target, null)) {
			pass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
			pass.setUniform(uniformName, uniform);
			if (samplerName != null && texture != null && sampler != null) {
				pass.setUniform(samplerName, texture, sampler);
			}
			pass.setVertexBuffer(0, vertices);
			pass.setIndexBuffer(indexBuffer, indices.type());
			pass.drawIndexed(quads * 6, 1, 0, 0, 0);
		}
	}

	/** A run of UI quads that share one image (or the white texture when {@code image} is null). */
	public record UiBatch(int firstVertex, int vertexCount, GpuTextureView image, GpuSampler sampler) {
	}

	/** Submits recorded UI quads: one pass, the font atlas in Sampler0 and each batch's image in Sampler1. */
	void drawUi(
		RenderPipeline pipeline, GpuTextureView target, @Nullable GpuTextureView depth, GpuBufferSlice projection, GpuBufferSlice transforms,
		GpuBufferSlice uniforms, ByteBuffer mesh, int vertexCount, UiTexture font, UiTexture white, List<UiBatch> batches
	) {
		GpuBufferSlice vertices = uploadVertices(mesh);
		RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
		GpuBuffer indexBuffer = indices.getBuffer(vertexCount / 4 * 6);
		try (RenderPass pass = pass("Visor UI", target, depth)) {
			pass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
			pass.setUniform("Projection", projection);
			pass.setUniform("DynamicTransforms", transforms);
			pass.setUniform("VisorUi", uniforms);
			pass.setVertexBuffer(0, vertices);
			pass.setIndexBuffer(indexBuffer, indices.type());
			pass.setUniform("Sampler0", font.view(), font.sampler());
			for (UiBatch batch : batches) {
				if (batch.image() != null) {
					pass.setUniform("Sampler1", batch.image(), batch.sampler());
				} else {
					pass.setUniform("Sampler1", white.view(), white.sampler());
				}
				pass.drawIndexed(batch.vertexCount() / 4 * 6, 1, batch.firstVertex() / 4 * 6, 0, 0);
			}
		}
	}

	/** Uniform blocks for this frame (26.3: allocated from the frame's transient GPU memory). */
	public static final class UniformStorage {
		GpuBufferSlice writeUniform(RawUniform uniform) {
			ByteBuffer data = MemoryUtil.memAlloc(Math.max(16, uniform.vec4s().length * Float.BYTES)).order(ByteOrder.nativeOrder());
			try {
				uniform.write(data);
				data.flip();
				return encoder().transientMemory().uploadGpu(data, UNIFORM_ALIGNMENT, GpuBuffer.USAGE_UNIFORM);
			} finally {
				MemoryUtil.memFree(data);
			}
		}
	}

	/** The world camera's projection, as captured for this frame. */
	public static final class WorldProjection {
		private final ProjectionMatrixBuffer buffer = new ProjectionMatrixBuffer("visor world");

		public GpuBufferSlice getBuffer(Matrix4f projection) {
			return this.buffer.getBuffer(projection);
		}
	}

	/** Pixel coordinates, y down, like CachedOrthoProjectionMatrixBuffer(-1000, 1000, invertY) on 1.21.11. */
	public static final class ScreenProjection {
		private final ProjectionMatrixBuffer buffer = new ProjectionMatrixBuffer("visor screen");
		private final Projection projection = new Projection();

		public GpuBufferSlice getBuffer(float width, float height) {
			this.projection.setupOrtho(-1000.0F, 1000.0F, width, height, true);
			return this.buffer.getBuffer(this.projection);
		}
	}

	/** A uniform block written from a flat list of vec4s (std140: vec4s are tightly packed). */
	public record RawUniform(float[] vec4s) {
		public void write(ByteBuffer buffer) {
			for (int i = 0; i + 3 < this.vec4s.length; i += 4) {
				buffer.putFloat(this.vec4s[i]).putFloat(this.vec4s[i + 1]).putFloat(this.vec4s[i + 2]).putFloat(this.vec4s[i + 3]);
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
