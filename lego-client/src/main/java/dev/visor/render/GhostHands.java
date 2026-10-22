package dev.visor.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Hand-tracking style hands. Each hand is a small skeleton (wrist, palm, five fingers) posed in
 * view space, projected to the screen as tapered capsules and shaded by {@code hands.fsh}.
 *
 * <p>Hand space (right hand): +X towards the pinky, +Y from the wrist towards the fingers,
 * +Z out of the back of the hand. The left hand mirrors X. Units are blocks (= metres).</p>
 */
public final class GhostHands {
	static final int MAX_SEGMENTS = 48;
	static final int MAX_PARTS = 14;
	static final int MAX_DECOS = 6;
	static final int UBO_VEC4S = 4 + MAX_SEGMENTS * 2 + MAX_PARTS + MAX_DECOS * 2;

	private static final float[][] FINGER_BASE = {
		{-0.026F, 0.082F, 0.000F},
		{-0.006F, 0.088F, 0.002F},
		{0.013F, 0.083F, 0.000F},
		{0.030F, 0.072F, -0.004F},
	};
	private static final float[][] FINGER_LENGTH = {
		{0.042F, 0.025F, 0.021F},
		{0.046F, 0.029F, 0.022F},
		{0.043F, 0.027F, 0.021F},
		{0.034F, 0.021F, 0.019F},
	};
	private static final float[][] FINGER_RADIUS = {
		{0.0098F, 0.0090F, 0.0082F, 0.0072F},
		{0.0100F, 0.0092F, 0.0084F, 0.0074F},
		{0.0095F, 0.0087F, 0.0080F, 0.0070F},
		{0.0085F, 0.0078F, 0.0072F, 0.0064F},
	};
	private static final float[] FINGER_SPREAD = {-8.0F, -2.0F, 4.0F, 11.0F};
	private static final Vector3f THUMB_BASE = new Vector3f(-0.020F, 0.020F, -0.008F);
	private static final float[] THUMB_RADIUS = {0.0150F, 0.0112F, 0.0100F, 0.0086F};

	/** Finger poses: flexion of the three joints of index, middle, ring, pinky (degrees). */
	public static final Pose RELAXED = new Pose(
		new float[][]{{12, 18, 10}, {16, 22, 12}, {20, 26, 14}, {24, 28, 14}}, new Vector3f(-0.060F, 0.078F, -0.024F), 0.0F
	);
	public static final Pose POINT = new Pose(
		new float[][]{{4, 6, 3}, {32, 62, 34}, {42, 70, 36}, {48, 72, 36}}, new Vector3f(-0.046F, 0.064F, -0.034F), 0.0F
	);
	public static final Pose PINCH = new Pose(
		new float[][]{{30, 46, 26}, {26, 36, 20}, {30, 40, 20}, {34, 42, 20}}, new Vector3f(), 1.0F
	);
	public static final Pose CURLED = new Pose(
		new float[][]{{40, 58, 30}, {44, 62, 32}, {48, 64, 32}, {52, 64, 30}}, new Vector3f(-0.040F, 0.058F, -0.040F), 0.0F
	);

	private final float[] data = new float[UBO_VEC4S * 4];
	private final List<PartBuild> parts = new ArrayList<>();
	private final List<float[]> decos = new ArrayList<>();
	private int segmentCount;
	private float minX;
	private float minY;
	private float maxX;
	private float maxY;

	// Projection state for the current frame.
	private final Matrix4f projection = new Matrix4f();
	private float screenWidth;
	private float screenHeight;
	private float focal;

	/**
	 * One posed hand. {@code orientation} maps hand space to view space, {@code wrist} is the
	 * wrist position in view space (camera at the origin looking down -Z).
	 */
	public static final class HandState {
		public final Vector3f wrist = new Vector3f();
		public final Matrix3f orientation = new Matrix3f();
		public boolean left;
		public float alpha = 1.0F;
		public float point;
		public float pinch;
		public float curl;
		public float idle;
		/** View-space point where the pinch happens, written by {@link #addHand}. */
		public final Vector3f pinchPoint = new Vector3f();
		public final Vector3f indexTip = new Vector3f();
	}

	public record Pose(float[][] flex, Vector3f thumbTip, float pinchThumb) {
	}

	public void begin(int width, int height, float fovDegrees) {
		this.screenWidth = width;
		this.screenHeight = height;
		float fov = fovDegrees * Mth.DEG_TO_RAD;
		this.projection.setPerspective(fov, (float) width / Math.max(1, height), 0.01F, 100.0F);
		this.focal = height * 0.5F / (float) Math.tan(fov * 0.5F);
		this.parts.clear();
		this.decos.clear();
		this.segmentCount = 0;
		this.minX = Float.MAX_VALUE;
		this.minY = Float.MAX_VALUE;
		this.maxX = -Float.MAX_VALUE;
		this.maxY = -Float.MAX_VALUE;
	}

	/** Builds the capsules of one hand. */
	public void addHand(HandState hand) {
		if (hand.alpha <= 0.001F) {
			return;
		}
		float mirror = hand.left ? -1.0F : 1.0F;
		float[][] flex = new float[4][3];
		for (int f = 0; f < 4; f++) {
			for (int j = 0; j < 3; j++) {
				float value = Mth.lerp(hand.point, RELAXED.flex[f][j], POINT.flex[f][j]);
				value = Mth.lerp(hand.curl, value, CURLED.flex[f][j]);
				value = Mth.lerp(hand.pinch, value, PINCH.flex[f][j]);
				// A little life: fingers breathe slightly out of phase.
				value += Mth.sin(hand.idle * 1.3F + f * 0.9F + j) * 1.6F;
				flex[f][j] = value;
			}
		}

		Vector3f[][] fingers = new Vector3f[4][];
		for (int f = 0; f < 4; f++) {
			fingers[f] = this.finger(f, flex[f]);
		}
		Vector3f indexTip = fingers[0][3];

		Vector3f thumbTip = new Vector3f(RELAXED.thumbTip).lerp(POINT.thumbTip, hand.point).lerp(CURLED.thumbTip, hand.curl);
		Vector3f pinchTarget = new Vector3f(indexTip).add(-0.004F, -0.002F, -0.006F);
		thumbTip.lerp(pinchTarget, hand.pinch);
		Vector3f[] thumb = this.thumb(thumbTip);

		// Palm and forearm.
		Vector3f wrist = new Vector3f(0.0F, 0.0F, 0.0F);
		List<float[]> palm = new ArrayList<>();
		palm.add(capsule(new Vector3f(0.001F, 0.018F, 0.0F), new Vector3f(0.003F, 0.060F, 0.0F), 0.030F, 0.027F));
		palm.add(capsule(wrist, fingers[0][0], 0.022F, 0.011F));
		palm.add(capsule(wrist, fingers[3][0], 0.020F, 0.010F));
		palm.add(capsule(fingers[0][0], fingers[3][0], 0.012F, 0.010F));
		List<float[]> forearm = List.<float[]>of(capsule(wrist, new Vector3f(0.0F, -0.24F, -0.035F), 0.025F, 0.034F));

		this.addPart(hand, mirror, forearm, 0.0F);
		this.addPart(hand, mirror, palm, 0.012F);
		this.addPart(hand, mirror, List.of(
			capsule(thumb[0], thumb[1], THUMB_RADIUS[0], THUMB_RADIUS[1]),
			capsule(thumb[1], thumb[2], THUMB_RADIUS[1], THUMB_RADIUS[2]),
			capsule(thumb[2], thumb[3], THUMB_RADIUS[2], THUMB_RADIUS[3])
		), 0.004F);
		for (int f = 0; f < 4; f++) {
			Vector3f[] p = fingers[f];
			float[] r = FINGER_RADIUS[f];
			this.addPart(hand, mirror, List.of(
				capsule(p[0], p[1], r[0], r[1]),
				capsule(p[1], p[2], r[1], r[2]),
				capsule(p[2], p[3], r[2], r[3])
			), 0.003F);
		}

		toView(hand, mirror, indexTip, hand.indexTip);
		Vector3f pinch = new Vector3f(indexTip).lerp(thumbTip, 0.5F).add(0.0F, 0.012F, -0.006F);
		toView(hand, mirror, pinch, hand.pinchPoint);
	}

	/** White capsule drawn on top of the hands (pinch indicator, pointer, ray). Points in view space. */
	public void addDeco(Vector3f a, Vector3f b, float radiusA, float radiusB, float alphaA, float alphaB) {
		float[] pa = this.project(a);
		float[] pb = this.project(b);
		if (pa == null || pb == null) {
			return;
		}
		this.addScreenDeco(pa[0], pa[1], pb[0], pb[1], radiusA * pa[2], radiusB * pb[2], alphaA, alphaB);
	}

	/** Decoration given directly in screen pixels. */
	public void addScreenDeco(float ax, float ay, float bx, float by, float radiusA, float radiusB, float alphaA, float alphaB) {
		if (this.decos.size() >= MAX_DECOS) {
			return;
		}
		this.decos.add(new float[]{ax, ay, bx, by, radiusA, radiusB, alphaA, alphaB});
		float r = Math.max(radiusA, radiusB) + 3.0F;
		this.extend(Math.min(ax, bx) - r, Math.min(ay, by) - r, Math.max(ax, bx) + r, Math.max(ay, by) + r);
	}

	/** Projects a view-space point to screen pixels; [x, y, pixelsPerBlock] or null when behind. */
	public float[] project(Vector3f view) {
		float depth = -view.z;
		if (depth < 0.02F) {
			return null;
		}
		Vector4f clip = this.projection.transform(new Vector4f(view, 1.0F));
		float x = (clip.x / clip.w * 0.5F + 0.5F) * this.screenWidth;
		float y = (0.5F - clip.y / clip.w * 0.5F) * this.screenHeight;
		return new float[]{x, y, this.focal / depth};
	}

	public void render(GpuTextureView target, float globalAlpha, float outlineWidth, float fillAlpha) {
		if (this.parts.isEmpty() && this.decos.isEmpty()) {
			return;
		}
		VisorGpu gpu = VisorGpu.get();
		if (!gpu.usable(VisorPipelines.HANDS)) {
			return;
		}
		// Far parts first: the shader composites in this order.
		this.parts.sort(Comparator.comparingDouble((PartBuild part) -> part.depth).reversed());
		float[] d = this.data;
		java.util.Arrays.fill(d, 0.0F);
		int partCount = Math.min(this.parts.size(), MAX_PARTS);
		int decoCount = Math.min(this.decos.size(), MAX_DECOS);
		put(d, 0, partCount, decoCount, this.screenHeight, globalAlpha);
		float scale = Math.max(1.0F, this.screenHeight / 1080.0F);
		put(d, 1, fillAlpha, outlineWidth * scale, 1.15F * scale, 0.20F);
		put(d, 2, 0.055F, 0.058F, 0.065F, 0.55F);
		put(d, 3, 1.0F, 1.0F, 1.0F, 6.0F * scale);
		int segBase = 4;
		int segRBase = segBase + MAX_SEGMENTS;
		int partBase = segRBase + MAX_SEGMENTS;
		int decoBase = partBase + MAX_PARTS;
		int decoRBase = decoBase + MAX_DECOS;
		int seg = 0;
		for (int p = 0; p < partCount; p++) {
			PartBuild part = this.parts.get(p);
			int first = seg;
			for (float[] s : part.segments) {
				if (seg >= MAX_SEGMENTS) {
					break;
				}
				put(d, segBase + seg, s[0], s[1], s[2], s[3]);
				put(d, segRBase + seg, s[4], s[5], 0.0F, 0.0F);
				seg++;
			}
			put(d, partBase + p, first, seg - first, part.smoothing, part.alpha);
		}
		for (int i = 0; i < decoCount; i++) {
			float[] deco = this.decos.get(i);
			put(d, decoBase + i, deco[0], deco[1], deco[2], deco[3]);
			put(d, decoRBase + i, deco[4], deco[5], deco[6], deco[7]);
		}
		GpuBufferSlice uniform = gpu.handUniforms.writeUniform(new VisorGpu.RawUniform(d.clone()));
		float pad = outlineWidth * scale + 8.0F;
		float x0 = Math.max(0.0F, this.minX - pad);
		float y0 = Math.max(0.0F, this.minY - pad);
		float x1 = Math.min(this.screenWidth, this.maxX + pad);
		float y1 = Math.min(this.screenHeight, this.maxY + pad);
		if (x1 <= x0 || y1 <= y0) {
			return;
		}
		gpu.drawScreenQuads(VisorPipelines.HANDS, target, "HandData", uniform, new float[]{
			x0 / this.screenWidth * 2.0F - 1.0F, 1.0F - y1 / this.screenHeight * 2.0F,
			x1 / this.screenWidth * 2.0F - 1.0F, 1.0F - y0 / this.screenHeight * 2.0F
		});
	}

	// ------------------------------------------------------------------------------------------

	private Vector3f[] finger(int index, float[] flex) {
		Vector3f base = new Vector3f(FINGER_BASE[index][0], FINGER_BASE[index][1], FINGER_BASE[index][2]);
		float spread = FINGER_SPREAD[index] * Mth.DEG_TO_RAD;
		Vector3f dir0 = new Vector3f(Mth.sin(spread), Mth.cos(spread), 0.0F);
		Vector3f axis = new Vector3f(0.0F, 0.0F, 1.0F).cross(dir0).normalize();
		Vector3f[] points = new Vector3f[4];
		points[0] = base;
		float angle = 0.0F;
		Vector3f current = new Vector3f(base);
		for (int j = 0; j < 3; j++) {
			angle += flex[j] * Mth.DEG_TO_RAD;
			Vector3f dir = new Vector3f(dir0).rotateAxis(angle, axis.x, axis.y, axis.z);
			current = new Vector3f(current).fma(FINGER_LENGTH[index][j], dir);
			points[j + 1] = current;
		}
		return points;
	}

	private Vector3f[] thumb(Vector3f tip) {
		Vector3f base = new Vector3f(THUMB_BASE);
		Vector3f span = new Vector3f(tip).sub(base);
		// Bulge the joints outwards (away from the palm, towards -X/-Z) for a natural curve.
		Vector3f bulge = new Vector3f(-0.62F, -0.18F, -0.40F).normalize();
		Vector3f j1 = new Vector3f(base).fma(0.42F, span).fma(0.013F, bulge);
		Vector3f j2 = new Vector3f(base).fma(0.74F, span).fma(0.008F, bulge);
		return new Vector3f[]{base, j1, j2, new Vector3f(tip)};
	}

	private static float[] capsule(Vector3f a, Vector3f b, float ra, float rb) {
		return new float[]{a.x, a.y, a.z, b.x, b.y, b.z, ra, rb};
	}

	private static void toView(HandState hand, float mirror, Vector3f local, Vector3f out) {
		out.set(local.x * mirror, local.y, local.z);
		hand.orientation.transform(out);
		out.add(hand.wrist);
	}

	private void addPart(HandState hand, float mirror, List<float[]> capsules, float smoothing) {
		if (this.parts.size() >= MAX_PARTS) {
			return;
		}
		PartBuild part = new PartBuild();
		part.alpha = hand.alpha;
		float depthSum = 0.0F;
		float pixelScale = 0.0F;
		Vector3f a = new Vector3f();
		Vector3f b = new Vector3f();
		for (float[] c : capsules) {
			toView(hand, mirror, new Vector3f(c[0], c[1], c[2]), a);
			toView(hand, mirror, new Vector3f(c[3], c[4], c[5]), b);
			// Keep geometry in front of the near plane (the forearm reaches behind the camera).
			clampDepth(a, b);
			clampDepth(b, a);
			float[] pa = this.project(a);
			float[] pb = this.project(b);
			if (pa == null || pb == null) {
				continue;
			}
			float ra = c[6] * pa[2];
			float rb = c[7] * pb[2];
			// The uneven capsule needs |ra - rb| < length.
			float length = Mth.sqrt((pb[0] - pa[0]) * (pb[0] - pa[0]) + (pb[1] - pa[1]) * (pb[1] - pa[1]));
			if (Math.abs(ra - rb) >= length) {
				float mean = (ra + rb) * 0.5F;
				ra = mean;
				rb = mean;
			}
			part.segments.add(new float[]{pa[0], pa[1], pb[0], pb[1], ra, rb});
			depthSum += -(a.z + b.z) * 0.5F;
			pixelScale += (pa[2] + pb[2]) * 0.5F;
			float r = Math.max(ra, rb);
			this.extend(Math.min(pa[0], pb[0]) - r, Math.min(pa[1], pb[1]) - r, Math.max(pa[0], pb[0]) + r, Math.max(pa[1], pb[1]) + r);
		}
		if (part.segments.isEmpty()) {
			return;
		}
		part.depth = depthSum / part.segments.size();
		part.smoothing = smoothing * pixelScale / part.segments.size();
		this.parts.add(part);
	}

	private static void clampDepth(Vector3f point, Vector3f other) {
		float near = -0.06F;
		if (point.z > near && other.z < near) {
			float t = (near - other.z) / (point.z - other.z);
			point.set(other).lerp(point, t);
		}
	}

	private void extend(float x0, float y0, float x1, float y1) {
		this.minX = Math.min(this.minX, x0);
		this.minY = Math.min(this.minY, y0);
		this.maxX = Math.max(this.maxX, x1);
		this.maxY = Math.max(this.maxY, y1);
	}

	private static void put(float[] data, int vec4, float x, float y, float z, float w) {
		int i = vec4 * 4;
		data[i] = x;
		data[i + 1] = y;
		data[i + 2] = z;
		data[i + 3] = w;
	}

	private static final class PartBuild {
		final List<float[]> segments = new ArrayList<>();
		float depth;
		float smoothing;
		float alpha;
	}
}
