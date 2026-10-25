// Minecraft 26.3 version of dev/visor/render/UiTexture.java (GpuFormat, mip upload without sub-rects).
package dev.visor.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.GpuFormat;

/** A texture owned by the Visor renderer (not registered in Minecraft's TextureManager). */
public final class UiTexture implements AutoCloseable {
	private final GpuTexture texture;
	private final GpuTextureView view;
	private final GpuSampler sampler;
	private final int width;
	private final int height;

	private UiTexture(GpuTexture texture, GpuTextureView view, GpuSampler sampler, int width, int height) {
		this.texture = texture;
		this.view = view;
		this.sampler = sampler;
		this.width = width;
		this.height = height;
	}

	/**
	 * Uploads an image. With {@code mipmaps} a full mip chain is generated (box filter on
	 * premultiplied colour), which keeps pictures clean when the panel is seen from a distance.
	 */
	public static UiTexture upload(String label, NativeImage image, boolean mipmaps, GpuSampler sampler) {
		GpuDevice device = RenderSystem.getDevice();
		// Every level halves both sides; the chain stops before the shorter side would reach zero.
		int levels = mipmaps ? 1 + (31 - Integer.numberOfLeadingZeros(Math.min(image.getWidth(), image.getHeight()))) : 1;
		GpuTexture texture = device.createTexture(
			() -> "Visor " + label,
			GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
			GpuFormat.RGBA8_UNORM,
			image.getWidth(),
			image.getHeight(),
			1,
			levels
		);
		CommandEncoder encoder = device.createCommandEncoder();
		encoder.writeToTexture(texture, image);
		NativeImage previous = image;
		for (int level = 1; level < levels; level++) {
			NativeImage next = downsample(previous);
			encoder.writeToTexture(texture, next, level, 0, 0, 0);
			if (previous != image) {
				previous.close();
			}
			previous = next;
		}
		if (previous != image) {
			previous.close();
		}
		return new UiTexture(texture, device.createTextureView(texture), sampler, image.getWidth(), image.getHeight());
	}

	private static NativeImage downsample(NativeImage source) {
		int width = Math.max(1, source.getWidth() / 2);
		int height = Math.max(1, source.getHeight() / 2);
		NativeImage target = new NativeImage(width, height, false);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float a = 0;
				float r = 0;
				float g = 0;
				float b = 0;
				int samples = 0;
				for (int dy = 0; dy < 2; dy++) {
					for (int dx = 0; dx < 2; dx++) {
						int sx = Math.min(source.getWidth() - 1, x * 2 + dx);
						int sy = Math.min(source.getHeight() - 1, y * 2 + dy);
						int argb = source.getPixel(sx, sy);
						float alpha = (argb >>> 24) / 255.0F;
						a += alpha;
						r += ((argb >> 16) & 0xFF) * alpha;
						g += ((argb >> 8) & 0xFF) * alpha;
						b += (argb & 0xFF) * alpha;
						samples++;
					}
				}
				int outA = Math.round(a / samples * 255.0F);
				int outR = a > 0 ? Math.round(r / a) : 0;
				int outG = a > 0 ? Math.round(g / a) : 0;
				int outB = a > 0 ? Math.round(b / a) : 0;
				target.setPixel(x, y, outA << 24 | outR << 16 | outG << 8 | outB);
			}
		}
		return target;
	}

	public GpuTextureView view() {
		return this.view;
	}

	public GpuSampler sampler() {
		return this.sampler;
	}

	public int width() {
		return this.width;
	}

	public int height() {
		return this.height;
	}

	@Override
	public void close() {
		this.view.close();
		this.texture.close();
	}
}
