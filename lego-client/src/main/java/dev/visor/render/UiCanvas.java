package dev.visor.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.visor.render.text.FontAtlas;
import dev.visor.render.text.SdfFont;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

/**
 * Immediate mode 2D drawing on a {@link Surface}. Everything is expressed in layout pixels; the
 * surface decides where those pixels end up (a curved panel in the world, or the screen).
 *
 * <p>Shapes are resolution independent: edges are computed per pixel in the shader, so a panel
 * stays crisp no matter how close the player walks up to it.</p>
 */
public final class UiCanvas implements AutoCloseable {
	private static final float MODE_FILL = 0.0F;
	private static final float MODE_STROKE = 1.0F;
	private static final float MODE_GLYPH = 2.0F;
	private static final float MODE_IMAGE = 3.0F;
	private static final float MODE_AURORA = 4.0F;
	private static final float MODE_RING = 5.0F;
	private static final float MODE_TRIANGLE = 6.0F;
	private static final float INF = Float.POSITIVE_INFINITY;

	private final UiMesh mesh = new UiMesh(8192);
	private final List<Batch> batches = new ArrayList<>();
	private final Vector3f scratch = new Vector3f();
	private Surface surface = ScreenSurface.INSTANCE;

	private float tx;
	private float ty;
	private float scale = 1.0F;
	private final float[] transformStack = new float[3 * 32];
	private int transformDepth;

	private float clipX0 = -INF;
	private float clipY0 = -INF;
	private float clipX1 = INF;
	private float clipY1 = INF;
	private final float[] clipStack = new float[4 * 32];
	private int clipDepth;

	private float alpha = 1.0F;
	private final float[] alphaStack = new float[32];
	private int alphaDepth;

	private GpuTextureView batchImage;
	private GpuSampler batchSampler;
	private int batchStart;

	// Scratch for quad emission: corner colours and per-vertex interpolation.
	private final int[] colors = new int[4];

	public void begin(Surface surface) {
		this.surface = surface;
		this.tx = 0.0F;
		this.ty = 0.0F;
		this.scale = 1.0F;
		this.transformDepth = 0;
		this.clipX0 = -INF;
		this.clipY0 = -INF;
		this.clipX1 = INF;
		this.clipY1 = INF;
		this.clipDepth = 0;
		this.alpha = 1.0F;
		this.alphaDepth = 0;
	}

	/** Switches surface without flushing; only valid between surfaces sharing the same projection. */
	public void setSurface(Surface surface) {
		this.surface = surface;
	}

	public Surface surface() {
		return this.surface;
	}

	public boolean isEmpty() {
		return this.mesh.vertexCount() == 0;
	}

	// ------------------------------------------------------------------------------------------
	// State stacks

	public void push() {
		this.transformStack[this.transformDepth * 3] = this.tx;
		this.transformStack[this.transformDepth * 3 + 1] = this.ty;
		this.transformStack[this.transformDepth * 3 + 2] = this.scale;
		this.transformDepth++;
	}

	public void pop() {
		this.transformDepth--;
		this.tx = this.transformStack[this.transformDepth * 3];
		this.ty = this.transformStack[this.transformDepth * 3 + 1];
		this.scale = this.transformStack[this.transformDepth * 3 + 2];
	}

	public void translate(float x, float y) {
		this.tx += x * this.scale;
		this.ty += y * this.scale;
	}

	/** Scales around a pivot (layout coordinates). */
	public void scale(float factor, float pivotX, float pivotY) {
		this.translate(pivotX, pivotY);
		this.scale *= factor;
		this.translate(-pivotX, -pivotY);
	}

	public void pushAlpha(float factor) {
		this.alphaStack[this.alphaDepth++] = this.alpha;
		this.alpha *= Math.max(0.0F, Math.min(1.0F, factor));
	}

	public void popAlpha() {
		this.alpha = this.alphaStack[--this.alphaDepth];
	}

	public float alpha() {
		return this.alpha;
	}

	/** Restricts drawing to a rectangle (layout coordinates), intersected with the current clip. */
	public void pushClip(float x, float y, float w, float h) {
		int base = this.clipDepth * 4;
		this.clipStack[base] = this.clipX0;
		this.clipStack[base + 1] = this.clipY0;
		this.clipStack[base + 2] = this.clipX1;
		this.clipStack[base + 3] = this.clipY1;
		this.clipDepth++;
		this.clipX0 = Math.max(this.clipX0, this.tx + x * this.scale);
		this.clipY0 = Math.max(this.clipY0, this.ty + y * this.scale);
		this.clipX1 = Math.min(this.clipX1, this.tx + (x + w) * this.scale);
		this.clipY1 = Math.min(this.clipY1, this.ty + (y + h) * this.scale);
	}

	public void popClip() {
		this.clipDepth--;
		int base = this.clipDepth * 4;
		this.clipX0 = this.clipStack[base];
		this.clipY0 = this.clipStack[base + 1];
		this.clipX1 = this.clipStack[base + 2];
		this.clipY1 = this.clipStack[base + 3];
	}

	// ------------------------------------------------------------------------------------------
	// Shapes

	public void rect(float x, float y, float w, float h, int color) {
		this.roundRect(x, y, w, h, 0.0F, color);
	}

	public void roundRect(float x, float y, float w, float h, float radius, int color) {
		this.shape(x, y, w, h, radius, 0.0F, MODE_FILL, color, color, color, color);
	}

	/** Vertical gradient. */
	public void roundRectGradient(float x, float y, float w, float h, float radius, int top, int bottom) {
		this.shape(x, y, w, h, radius, 0.0F, MODE_FILL, top, top, bottom, bottom);
	}

	/** Horizontal gradient. */
	public void roundRectGradientH(float x, float y, float w, float h, float radius, int left, int right) {
		this.shape(x, y, w, h, radius, 0.0F, MODE_FILL, left, right, right, left);
	}

	/** Border drawn inside the rectangle. */
	public void roundRectStroke(float x, float y, float w, float h, float radius, float width, int color) {
		this.shape(x, y, w, h, radius, width, MODE_STROKE, color, color, color, color);
	}

	/** Soft shadow / glow of a rounded rectangle. */
	public void shadow(float x, float y, float w, float h, float radius, float blur, int color) {
		if (blur <= 0.0F) {
			this.roundRect(x, y, w, h, radius, color);
			return;
		}
		float halfW = w * 0.5F;
		float halfH = h * 0.5F;
		this.quad(
			x - blur, y - blur, x + w + blur, y + h + blur, 0, 0, 0, 0,
			-halfW - blur, -halfH - blur, halfW + blur, halfH + blur,
			color, color, color, color, halfW, halfH, radius, blur, MODE_FILL, 0, 0, 0
		);
	}

	public void circle(float cx, float cy, float radius, int color) {
		this.roundRect(cx - radius, cy - radius, radius * 2.0F, radius * 2.0F, radius, color);
	}

	public void circleStroke(float cx, float cy, float radius, float width, int color) {
		this.roundRectStroke(cx - radius, cy - radius, radius * 2.0F, radius * 2.0F, radius, width, color);
	}

	/** Arc of a ring; angles in radians, 0 = up, clockwise. */
	public void arc(float cx, float cy, float radius, float thickness, float start, float sweep, int color) {
		float r = radius + thickness;
		this.quad(
			cx - r, cy - r, cx + r, cy + r, 0, 0, 0, 0, -r, -r, r, r,
			color, color, color, color, radius, thickness, 0, 0, MODE_RING, start, sweep, 0
		);
	}

	/** Triangle pointing right with rounded corners (a "play" sign), {@code w} x {@code h} around the centre. */
	public void playTriangle(float cx, float cy, float w, float h, float radius, int color) {
		this.shape(cx - w * 0.5F, cy - h * 0.5F, w, h, radius, 0.0F, MODE_TRIANGLE, color, color, color, color);
	}

	/** Animated gradient art (see ui.fsh); {@code palette} 0-7. */
	public void aurora(float x, float y, float w, float h, float radius, float seed, int palette, int tint) {
		this.shape(x, y, w, h, radius, 0.0F, MODE_AURORA, tint, tint, tint, tint, seed, palette);
	}

	private void shape(float x, float y, float w, float h, float radius, float param, float mode, int tl, int tr, int br, int bl) {
		this.shape(x, y, w, h, radius, param, mode, tl, tr, br, bl, 0.0F, 0.0F);
	}

	private void shape(
		float x, float y, float w, float h, float radius, float param, float mode, int tl, int tr, int br, int bl, float extraY, float extraZ
	) {
		if (w <= 0.0F || h <= 0.0F) {
			return;
		}
		float halfW = w * 0.5F;
		float halfH = h * 0.5F;
		this.quad(x, y, x + w, y + h, 0, 0, 0, 0, -halfW, -halfH, halfW, halfH, tl, tr, br, bl, halfW, halfH, radius, param, mode, extraY, extraZ, 0);
	}

	// ------------------------------------------------------------------------------------------
	// Images

	/** Draws a whole texture with rounded corners, tinted by {@code tint}. */
	public void image(GpuTextureView texture, GpuSampler sampler, float x, float y, float w, float h, float radius, int tint) {
		this.image(texture, sampler, x, y, w, h, 0.0F, 0.0F, 1.0F, 1.0F, radius, tint);
	}

	public void image(
		GpuTextureView texture, GpuSampler sampler, float x, float y, float w, float h, float u0, float v0, float u1, float v1, float radius, int tint
	) {
		this.image(texture, sampler, x, y, w, h, u0, v0, u1, v1, radius, tint, false);
	}

	/** An image whose colours are premultiplied by alpha, such as a 3D scene rendered off screen. */
	public void imagePremultiplied(
		GpuTextureView texture, GpuSampler sampler, float x, float y, float w, float h, float u0, float v0, float u1, float v1, float radius, int tint
	) {
		this.image(texture, sampler, x, y, w, h, u0, v0, u1, v1, radius, tint, true);
	}

	private void image(
		GpuTextureView texture, GpuSampler sampler, float x, float y, float w, float h, float u0, float v0, float u1, float v1, float radius, int tint,
		boolean premultiplied
	) {
		if (w <= 0.0F || h <= 0.0F) {
			return;
		}
		this.useImage(texture, sampler);
		float halfW = w * 0.5F;
		float halfH = h * 0.5F;
		this.quad(
			x, y, x + w, y + h, u0, v0, u1, v1, -halfW, -halfH, halfW, halfH, tint, tint, tint, tint, halfW, halfH, radius, 0, MODE_IMAGE,
			premultiplied ? 1.0F : 0.0F, 0, 0
		);
	}

	private void useImage(GpuTextureView texture, GpuSampler sampler) {
		if (this.batchImage == null) {
			this.batchImage = texture;
			this.batchSampler = sampler;
		} else if (this.batchImage != texture || this.batchSampler != sampler) {
			this.closeBatch();
			this.batchImage = texture;
			this.batchSampler = sampler;
		}
	}

	private void closeBatch() {
		int end = this.mesh.vertexCount();
		if (end > this.batchStart) {
			this.batches.add(new Batch(this.batchStart, end - this.batchStart, this.batchImage, this.batchSampler));
		}
		this.batchStart = end;
		this.batchImage = null;
		this.batchSampler = null;
	}

	// ------------------------------------------------------------------------------------------
	// Text

	/** Draws a line of text with its baseline at {@code baseline}. Returns the pen position after it. */
	public float text(SdfFont font, String text, float x, float baseline, float size, int color) {
		return this.text(font, text, x, baseline, size, color, 0.0F, 0.0F, 0.0F);
	}

	/**
	 * @param weight   thickening in atlas pixels (positive = bolder)
	 * @param tracking extra spacing between letters in pixels
	 * @param softness 0 for crisp text, otherwise a blur radius in atlas pixels (text shadows)
	 */
	public float text(SdfFont font, String text, float x, float baseline, float size, int color, float weight, float tracking, float softness) {
		float pen = x;
		int previous = -1;
		for (int i = 0; i < text.length(); ) {
			int codepoint = text.codePointAt(i);
			i += Character.charCount(codepoint);
			if (previous >= 0) {
				pen += font.kerning(previous, codepoint) * size + tracking;
			}
			SdfFont.Glyph glyph = font.glyph(codepoint);
			if (glyph.visible()) {
				this.quad(
					pen + glyph.left() * size, baseline - glyph.top() * size, pen + glyph.right() * size, baseline - glyph.bottom() * size,
					glyph.u0(), glyph.v0(), glyph.u1(), glyph.v1(), 0, 0, 0, 0,
					color, color, color, color, 0, 0, 0, 0, MODE_GLYPH, weight, softness, 0
				);
			}
			pen += glyph.advance() * size;
			previous = codepoint;
		}
		return pen;
	}

	/** Text vertically centred on {@code centerY} (by cap height), starting at {@code x}. */
	public float textMiddle(SdfFont font, String text, float x, float centerY, float size, int color) {
		return this.text(font, text, x, centerY + font.capHeight * size * 0.5F, size, color);
	}

	public void textCentered(SdfFont font, String text, float centerX, float centerY, float size, int color) {
		this.textMiddle(font, text, centerX - font.width(text, size) * 0.5F, centerY, size, color);
	}

	public void textRight(SdfFont font, String text, float right, float centerY, float size, int color) {
		this.textMiddle(font, text, right - font.width(text, size), centerY, size, color);
	}

	/** Text that is cut with an ellipsis when wider than {@code maxWidth}. */
	public float textFit(SdfFont font, String text, float x, float centerY, float size, float maxWidth, int color) {
		return this.textMiddle(font, font.ellipsize(text, size, maxWidth), x, centerY, size, color);
	}

	/** Draws a named icon centred on (cx, cy). */
	public void icon(String name, boolean filled, float cx, float cy, float size, int color) {
		FontAtlas atlas = UiResources.atlas();
		SdfFont font = filled ? atlas.iconsFill : atlas.icons;
		int codepoint = font.icon(name);
		if (codepoint < 0) {
			return;
		}
		SdfFont.Glyph glyph = font.glyph(codepoint);
		float pen = cx - glyph.advance() * size * 0.5F;
		float baseline = cy + (font.ascender + font.descender) * 0.5F * size;
		if (glyph.visible()) {
			this.quad(
				pen + glyph.left() * size, baseline - glyph.top() * size, pen + glyph.right() * size, baseline - glyph.bottom() * size,
				glyph.u0(), glyph.v0(), glyph.u1(), glyph.v1(), 0, 0, 0, 0,
				color, color, color, color, 0, 0, 0, 0, MODE_GLYPH, 0, 0, 0
			);
		}
	}

	// ------------------------------------------------------------------------------------------
	// Quad emission: transform, clip, subdivide for curved surfaces.

	private void quad(
		float x0, float y0, float x1, float y1,
		float u0, float v0, float u1, float v1,
		float lx0, float ly0, float lx1, float ly1,
		int tl, int tr, int br, int bl,
		float sx, float sy, float sz, float sw,
		float mode, float ey, float ez, float ew
	) {
		float px0 = this.tx + x0 * this.scale;
		float py0 = this.ty + y0 * this.scale;
		float px1 = this.tx + x1 * this.scale;
		float py1 = this.ty + y1 * this.scale;
		if (px1 <= this.clipX0 || px0 >= this.clipX1 || py1 <= this.clipY0 || py0 >= this.clipY1 || px1 <= px0 || py1 <= py0) {
			return;
		}
		float fx0 = px0 < this.clipX0 ? (this.clipX0 - px0) / (px1 - px0) : 0.0F;
		float fx1 = px1 > this.clipX1 ? (this.clipX1 - px0) / (px1 - px0) : 1.0F;
		float fy0 = py0 < this.clipY0 ? (this.clipY0 - py0) / (py1 - py0) : 0.0F;
		float fy1 = py1 > this.clipY1 ? (this.clipY1 - py0) / (py1 - py0) : 1.0F;

		this.colors[0] = tl;
		this.colors[1] = tr;
		this.colors[2] = br;
		this.colors[3] = bl;
		float segment = this.surface.segmentLength();
		float width = (fx1 - fx0) * (px1 - px0);
		int slices = segment > 0.0F ? Math.max(1, (int) Math.ceil(width / segment)) : 1;
		this.mesh.ensure(slices * 4);
		for (int s = 0; s < slices; s++) {
			float a = fx0 + (fx1 - fx0) * s / slices;
			float b = fx0 + (fx1 - fx0) * (s + 1) / slices;
			this.vertex(a, fy0, px0, py0, px1, py1, u0, v0, u1, v1, lx0, ly0, lx1, ly1, sx, sy, sz, sw, mode, ey, ez, ew);
			this.vertex(a, fy1, px0, py0, px1, py1, u0, v0, u1, v1, lx0, ly0, lx1, ly1, sx, sy, sz, sw, mode, ey, ez, ew);
			this.vertex(b, fy1, px0, py0, px1, py1, u0, v0, u1, v1, lx0, ly0, lx1, ly1, sx, sy, sz, sw, mode, ey, ez, ew);
			this.vertex(b, fy0, px0, py0, px1, py1, u0, v0, u1, v1, lx0, ly0, lx1, ly1, sx, sy, sz, sw, mode, ey, ez, ew);
		}
	}

	private void vertex(
		float fx, float fy, float px0, float py0, float px1, float py1,
		float u0, float v0, float u1, float v1, float lx0, float ly0, float lx1, float ly1,
		float sx, float sy, float sz, float sw, float mode, float ey, float ez, float ew
	) {
		this.surface.map(px0 + (px1 - px0) * fx, py0 + (py1 - py0) * fy, this.scratch);
		int color = bilerp(this.colors, fx, fy);
		if (this.alpha < 1.0F) {
			int a = Math.round((color >>> 24) * this.alpha);
			color = (color & 0x00FFFFFF) | a << 24;
		}
		this.mesh.vertex(
			this.scratch.x, this.scratch.y, this.scratch.z, color,
			u0 + (u1 - u0) * fx, v0 + (v1 - v0) * fy,
			lx0 + (lx1 - lx0) * fx, ly0 + (ly1 - ly0) * fy,
			sx, sy, sz, sw, mode, ey, ez, ew
		);
	}

	private static int bilerp(int[] c, float fx, float fy) {
		if (c[0] == c[1] && c[1] == c[2] && c[2] == c[3]) {
			return c[0];
		}
		int top = lerpColor(c[0], c[1], fx);
		int bottom = lerpColor(c[3], c[2], fx);
		return lerpColor(top, bottom, fy);
	}

	public static int lerpColor(int a, int b, float t) {
		if (t <= 0.0F) {
			return a;
		}
		if (t >= 1.0F) {
			return b;
		}
		int aa = a >>> 24;
		int ar = (a >> 16) & 0xFF;
		int ag = (a >> 8) & 0xFF;
		int ab = a & 0xFF;
		int ba = b >>> 24;
		int br = (b >> 16) & 0xFF;
		int bg = (b >> 8) & 0xFF;
		int bb = b & 0xFF;
		return Math.round(aa + (ba - aa) * t) << 24
			| Math.round(ar + (br - ar) * t) << 16
			| Math.round(ag + (bg - ag) * t) << 8
			| Math.round(ab + (bb - ab) * t);
	}

	// ------------------------------------------------------------------------------------------
	// GPU submission

	/**
	 * Draws everything recorded since the last flush into {@code target}.
	 *
	 * @param modelView  view rotation for world surfaces, identity for screen space
	 * @param projection projection uniform matching the surfaces that were drawn
	 */
	public void flush(GpuTextureView target, Matrix4fc modelView, GpuBufferSlice projection, float time, float textGamma) {
		this.flush(target, null, modelView, projection, time, textGamma, false);
	}

	/**
	 * @param depth     when not null, the UI is depth tested against this buffer (without writing
	 *                  to it), so world geometry in front of a panel hides it
	 * @param backfaces draw the back of every surface plain black (panels in the world)
	 */
	public void flush(
		GpuTextureView target, GpuTextureView depth, Matrix4fc modelView, GpuBufferSlice projection, float time, float textGamma, boolean backfaces
	) {
		this.closeBatch();
		int vertexCount = this.mesh.vertexCount();
		if (vertexCount == 0 || !UiResources.ready()) {
			this.mesh.reset();
			this.batches.clear();
			this.batchStart = 0;
			return;
		}
		VisorGpu gpu = VisorGpu.get();
		RenderPipeline pipeline = depth != null ? VisorPipelines.UI_DEPTH : VisorPipelines.UI;
		if (gpu.usable(pipeline)) {
			FontAtlas atlas = UiResources.atlas();
			GpuBufferSlice uniforms = gpu.uiUniforms.writeUniform(new VisorGpu.RawUniform(new float[]{
				atlas.width, atlas.height, atlas.range, time,
				textGamma, backfaces ? 1.0F : 0.0F, 0.0F, 0.0F
			}));
			GpuBufferSlice transforms = gpu.transforms(modelView);
			GpuBuffer vertices = VisorPipelines.UI_FORMAT.uploadImmediateVertexBuffer(this.mesh.data());
			RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
			GpuBuffer indexBuffer = indices.getBuffer(vertexCount / 4 * 6);
			UiTexture white = UiResources.white();
			UiTexture font = UiResources.atlasTexture();
			try (RenderPass pass = RenderSystem.getDevice()
					.createCommandEncoder()
					.createRenderPass(() -> "Visor UI", target, OptionalInt.empty(), depth, OptionalDouble.empty())) {
				pass.setPipeline(pipeline);
				pass.setUniform("Projection", projection);
				pass.setUniform("DynamicTransforms", transforms);
				pass.setUniform("VisorUi", uniforms);
				pass.setVertexBuffer(0, vertices);
				pass.setIndexBuffer(indexBuffer, indices.type());
				pass.bindTexture("Sampler0", font.view(), font.sampler());
				for (Batch batch : this.batches) {
					if (batch.image != null) {
						pass.bindTexture("Sampler1", batch.image, batch.sampler);
					} else {
						pass.bindTexture("Sampler1", white.view(), white.sampler());
					}
					pass.drawIndexed(0, batch.firstVertex / 4 * 6, batch.vertexCount / 4 * 6, 1);
				}
			}
		}
		this.mesh.reset();
		this.batches.clear();
		this.batchStart = 0;
	}

	@Override
	public void close() {
		this.mesh.close();
	}

	private record Batch(int firstVertex, int vertexCount, GpuTextureView image, GpuSampler sampler) {
	}
}
