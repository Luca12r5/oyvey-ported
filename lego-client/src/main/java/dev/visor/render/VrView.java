package dev.visor.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import dev.visor.browser.BrowserSession;
import dev.visor.vr.VrFormat;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/** Draws a VR video around the player over the whole screen (see vr.fsh). */
public final class VrView {
	private static final VrView INSTANCE = new VrView();
	private static final float[] FULL_SCREEN = {-1.0F, -1.0F, 1.0F, 1.0F};
	/** Half the width of the flat screen in VR, as the tangent of its half angle (about 76° wide). */
	private static final float FLAT_HALF_WIDTH = 0.78F;

	private final Matrix4f inverse = new Matrix4f();

	private VrView() {
	}

	public static VrView get() {
		return INSTANCE;
	}

	/**
	 * @param yaw     Minecraft yaw (degrees) the video's centre faces
	 * @param opacity 1 covers the world completely
	 */
	public void render(GpuTextureView target, int width, int height, float time, float opacity, float yaw) {
		VisorGpu gpu = VisorGpu.get();
		if (!gpu.usable(VisorPipelines.VR) || !FrameCapture.valid) {
			return;
		}
		BrowserTexture texture = BrowserTexture.get();
		GpuTextureView frame = texture.vr();
		BrowserSession.VrCapture capture = texture.capture();
		// The picture is wrapped the way it was captured, also while a new layout is on its way.
		VrFormat format = capture != null ? VrFormat.of(capture.format()) : VrFormat.SPHERE;
		boolean ready = frame != null && texture.vrWidth() > 0 && texture.vrHeight() > 0;

		this.inverse.set(FrameCapture.PROJECTION).mul(FrameCapture.VIEW).invert();
		Vector4f c0 = this.corner(-1.0F, -1.0F);
		Vector4f c1 = this.corner(1.0F, -1.0F);
		Vector4f c2 = this.corner(-1.0F, 1.0F);
		Vector4f c3 = this.corner(1.0F, 1.0F);
		float yawRad = yaw * Mth.DEG_TO_RAD;
		float fx = -Mth.sin(yawRad);
		float fz = Mth.cos(yawRad);
		int glow = average(texture.vrEdges());
		float texWidth = Math.max(1, texture.vrWidth());
		float texHeight = Math.max(1, texture.vrHeight());
		GpuBufferSlice uniform = gpu.vrUniforms.writeUniform(new VisorGpu.RawUniform(new float[]{
			c0.x, c0.y, c0.z, 0.0F,
			c1.x, c1.y, c1.z, 0.0F,
			c2.x, c2.y, c2.z, 0.0F,
			c3.x, c3.y, c3.z, 0.0F,
			fx, 0.0F, fz, 0.0F,
			-fz, 0.0F, fx, 0.0F,
			width, height, time, opacity,
			format.projection, FLAT_HALF_WIDTH, texWidth / texHeight, ready ? 1.0F : 0.0F,
			texWidth, texHeight, 1.0F / texWidth, 1.0F / texHeight,
			((glow >> 16) & 0xFF) / 255.0F, ((glow >> 8) & 0xFF) / 255.0F, (glow & 0xFF) / 255.0F, ready ? 1.0F : 0.0F
		}));
		GpuSampler sampler = format.projection == VrFormat.PROJECTION_SPHERE
			? RenderSystem.getSamplerCache().getSampler(AddressMode.REPEAT, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false)
			: RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
		GpuTextureView bound = ready ? frame : UiResources.white().view();
		gpu.drawScreenQuads(VisorPipelines.VR, target, "VrData", uniform, FULL_SCREEN, "Sampler0", bound, sampler);
	}

	/** World direction (unnormalised) of the view ray through a corner of the screen. */
	private Vector4f corner(float x, float y) {
		Vector4f point = new Vector4f(x, y, 1.0F, 1.0F);
		this.inverse.transform(point);
		return point.div(point.w);
	}

	private static int average(int[] colors) {
		int r = 0;
		int g = 0;
		int b = 0;
		for (int color : colors) {
			r += (color >> 16) & 0xFF;
			g += (color >> 8) & 0xFF;
			b += color & 0xFF;
		}
		int n = Math.max(1, colors.length);
		return (r / n) << 16 | (g / n) << 8 | b / n;
	}
}
