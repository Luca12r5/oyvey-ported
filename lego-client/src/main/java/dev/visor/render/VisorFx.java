package dev.visor.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.textures.GpuTextureView;

/** Full screen headset effects (see fx.fsh). */
public final class VisorFx {
	/** Soft field-of-view edge of the lenses (0 = none, 1 = full). */
	public float vignette;
	/** Plain black over everything. */
	public float blackout;
	/** Lens openings: 0 = wide open, 1 = closed. */
	public float lensClosure;
	/** How much of the lens mask is applied at all. */
	public float lensMask;
	/** 0 = two lens openings, 1 = a single soft opening in the centre. */
	public float lensMerge;
	/** Colour bloom behind the boot logo. */
	public float glowR;
	public float glowG;
	public float glowB;
	public float glow;

	public boolean active() {
		return this.vignette > 0.001F || this.blackout > 0.001F || this.lensMask > 0.001F;
	}

	public void render(GpuTextureView target, int width, int height, float time) {
		if (!this.active()) {
			return;
		}
		VisorGpu gpu = VisorGpu.get();
		if (!gpu.usable(VisorPipelines.FX)) {
			return;
		}
		GpuBufferSlice uniform = gpu.fxUniforms.writeUniform(new VisorGpu.RawUniform(new float[]{
			width, height, time, 0.0F,
			this.vignette, this.blackout, this.lensClosure, this.lensMask,
			this.glowR, this.glowG, this.glowB, this.glow,
			this.lensMerge, 0.0F, 0.0F, 0.0F
		}));
		gpu.drawScreenQuads(VisorPipelines.FX, target, "FxData", uniform, new float[]{-1.0F, -1.0F, 1.0F, 1.0F});
	}

	public void reset() {
		this.vignette = 0.0F;
		this.blackout = 0.0F;
		this.lensClosure = 0.0F;
		this.lensMask = 0.0F;
		this.lensMerge = 0.0F;
		this.glow = 0.0F;
	}
}
