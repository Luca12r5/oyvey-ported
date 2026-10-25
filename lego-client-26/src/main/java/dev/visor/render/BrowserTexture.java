// Minecraft 26.3 version of dev/visor/render/BrowserTexture.java (GpuFormat, raw RGBA upload).
package dev.visor.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.GpuFormat;
import dev.visor.VisorClient;
import dev.visor.browser.BrowserSession;
import org.jspecify.annotations.Nullable;

/**
 * The browser's newest pictures on the GPU: the page for the browser window, and the VR capture
 * for the VR view. Uploaded on the render thread by whoever draws first in a frame.
 */
public final class BrowserTexture {
	private static final BrowserTexture INSTANCE = new BrowserTexture();

	/** Render thread time spent uploading frames (for the performance check in the tests). */
	public static long uploadNanos;
	public static int uploads;

	private final Slot page = new Slot("Visor browser page");
	private final Slot vr = new Slot("Visor VR video");
	private long frameIndex = -1;

	private BrowserTexture() {
	}

	public static BrowserTexture get() {
		return INSTANCE;
	}

	/** Takes the newest frame of the browser, at most once per rendered frame. */
	public void update(long frame) {
		if (frame == this.frameIndex) {
			return;
		}
		this.frameIndex = frame;
		BrowserSession.Frame next = BrowserSession.get().takeFrame();
		if (next == null) {
			return;
		}
		long start = System.nanoTime();
		try {
			(next.vr() ? this.vr : this.page).upload(next);
		} catch (RuntimeException e) {
			VisorClient.LOGGER.warn("Could not show a browser frame", e);
		} finally {
			next.free();
			uploadNanos += System.nanoTime() - start;
			uploads++;
		}
	}

	/** The page picture, or null before the first one. */
	public @Nullable GpuTextureView page() {
		return this.page.view;
	}

	/** Counts the uploaded page pictures: a picture is new when this moved on. */
	public int pageVersion() {
		return this.page.version;
	}

	/** Average colours along the left, right, top and bottom edge of the page picture (ARGB). */
	public int[] pageEdges() {
		return this.page.edges;
	}

	/** The VR capture, or null before its first picture. */
	public @Nullable GpuTextureView vr() {
		return this.vr.view;
	}

	/** Which VR capture the VR picture belongs to. */
	public BrowserSession.@Nullable VrCapture capture() {
		return this.vr.capture;
	}

	public int vrWidth() {
		return this.vr.width;
	}

	public int vrHeight() {
		return this.vr.height;
	}

	/** Average colours along the edges of the VR picture (ARGB). */
	public int[] vrEdges() {
		return this.vr.edges;
	}

	/** The VR video ended: its picture goes, the next one starts fresh. */
	public void clearVr() {
		this.vr.close();
	}

	/** The browser closed: forget both pictures. */
	public void clear() {
		this.page.close();
		this.vr.close();
	}

	private static final class Slot {
		private final String label;
		private @Nullable GpuTexture texture;
		private @Nullable GpuTextureView view;
		private int width;
		private int height;
		private int version;
		private BrowserSession.@Nullable VrCapture capture;
		private int[] edges = {0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000};

		Slot(String label) {
			this.label = label;
		}

		void upload(BrowserSession.Frame frame) {
			GpuDevice device = RenderSystem.getDevice();
			if (this.texture == null || this.width != frame.width() || this.height != frame.height()) {
				this.close();
				this.texture = device.createTexture(
					() -> this.label, GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING, GpuFormat.RGBA8_UNORM, frame.width(), frame.height(), 1, 1
				);
				this.view = device.createTextureView(this.texture);
				this.width = frame.width();
				this.height = frame.height();
			}
			device.createCommandEncoder().writeToTexture(this.texture, frame.pixels(), 0, 0, 0, 0, frame.width(), frame.height());
			this.capture = frame.capture();
			this.edges = frame.edges();
			this.version++;
		}

		void close() {
			if (this.view != null) {
				this.view.close();
				this.view = null;
			}
			if (this.texture != null) {
				this.texture.close();
				this.texture = null;
			}
			this.width = 0;
			this.height = 0;
			this.capture = null;
		}
	}
}
