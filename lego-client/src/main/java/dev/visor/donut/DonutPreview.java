package dev.visor.donut;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import dev.visor.render.UiCanvas;
import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

/**
 * A live, dancing Doctor Donut for a spot in the UI. He is rendered off screen at a few times the
 * layout size (so his edges stay smooth) and the picture is drawn like any other image.
 */
public final class DonutPreview implements AutoCloseable {
	private static final float SUPERSAMPLE = 2.5F;
	private static final float FOV = 26.0F;

	private final PerspectiveProjectionMatrixBuffer projection = new PerspectiveProjectionMatrixBuffer("Visor Doctor Donut preview");
	private final Matrix4f projectionMatrix = new Matrix4f();
	private final Matrix4f view = new Matrix4f();
	private final DonutRenderer.Slot slot;
	private @Nullable GpuTexture color;
	private @Nullable GpuTexture depth;
	private @Nullable GpuTextureView colorView;
	private @Nullable GpuTextureView depthView;

	public DonutPreview() {
		this(DonutRenderer.Slot.PREVIEW);
	}

	/** @param slot bone buffer of its own, for a second picture drawn in the same frame */
	public DonutPreview(DonutRenderer.Slot slot) {
		this.slot = slot;
	}

	/**
	 * Draws him standing on the bottom edge of the given area, turned by {@code yaw} degrees.
	 *
	 * @return false while the model is still loading (nothing was drawn)
	 */
	public boolean draw(UiCanvas c, float x, float y, float w, float h, double time, float yaw) {
		return this.draw(c, x, y, w, h, "Dance", time, yaw);
	}

	/** Same, doing {@code clip} (one of his animations) instead of dancing. */
	public boolean draw(UiCanvas c, float x, float y, float w, float h, String clip, double time, float yaw) {
		DrDonutt donut = DrDonutt.get();
		DonutRenderer renderer = donut.renderer();
		if (renderer == null || !renderer.texturesReady() || w < 4 || h < 4) {
			return false;
		}
		this.ensureSize(Math.round(w * SUPERSAMPLE), Math.round(h * SUPERSAMPLE));
		// Camera a few blocks in front of him at chest height, framing him from head to toe.
		float distance = 1.05F / (float) Math.tan(Math.toRadians(FOV * 0.5F));
		this.projectionMatrix.setPerspective((float) Math.toRadians(FOV), w / h, 0.1F, 50.0F);
		this.view.setLookAt(0.0F, 1.0F, distance, 0.0F, 0.95F, 0.0F, 0.0F, 1.0F, 0.0F).rotateY((float) Math.toRadians(yaw));
		renderer.draw(
			this.slot, this.colorView, this.depthView, true, this.view, this.projection.getBuffer(this.projectionMatrix),
			clip, time, -1, 0, 0, 0.0F
		);
		// Render targets are stored bottom row first: flip vertically.
		c.imagePremultiplied(this.colorView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR), x, y, w, h, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0xFFFFFFFF);
		return true;
	}

	private void ensureSize(int width, int height) {
		if (this.color != null && this.color.getWidth(0) == width && this.color.getHeight(0) == height) {
			return;
		}
		this.close();
		GpuDevice device = RenderSystem.getDevice();
		this.color = device.createTexture(
			() -> "Visor Doctor Donut preview", GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING, TextureFormat.RGBA8, width, height, 1, 1
		);
		this.depth = device.createTexture(
			() -> "Visor Doctor Donut preview depth", GpuTexture.USAGE_RENDER_ATTACHMENT, TextureFormat.DEPTH32, width, height, 1, 1
		);
		this.colorView = device.createTextureView(this.color);
		this.depthView = device.createTextureView(this.depth);
	}

	@Override
	public void close() {
		if (this.colorView != null) {
			this.colorView.close();
			this.depthView.close();
			this.color.close();
			this.depth.close();
		}
		this.colorView = null;
		this.depthView = null;
		this.color = null;
		this.depth = null;
	}
}
