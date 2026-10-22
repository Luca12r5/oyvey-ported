package dev.visor.headset;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Vector3f;

/**
 * A smooth, procedurally built headset in the style of a modern standalone VR headset: a
 * squircle-shaped white shell with rounded edges and a gently domed front, three glossy sensor
 * pills, a soft face cushion with a nose cut-out, two glass lenses and the strap.
 *
 * <p>Units are blocks. +Y is up, the lenses look out along -Z, the origin is the centre of the
 * plane where the cushion touches the face (so +Z points into the head).</p>
 */
public final class HeadsetMesh {
	// Shell dimensions.
	private static final double A = 0.290;          // half width
	private static final double B = 0.150;          // half height
	private static final double N = 3.4;            // superellipse exponent (squircle-ness)
	private static final double FRONT = -0.215;     // z of the front face edge
	private static final double BEVEL = 0.055;      // radius of the rounded front edge
	private static final double DOME = 0.014;       // how far the centre of the front bulges out
	private static final int SEGMENTS = 96;

	// Materials: colour + (specular strength, glossiness) stored in the UV channel.
	private static final Material SHELL = new Material(0xFFEDEEEB, 0.32F, 0.42F);
	private static final Material FACE = new Material(0xFFF1F2EF, 0.48F, 0.62F);
	private static final Material SENSOR = new Material(0xFF0D0E11, 0.95F, 0.93F);
	private static final Material SENSOR_RIM = new Material(0xFF34363B, 0.35F, 0.45F);
	private static final Material CAMERA = new Material(0xFF1A2333, 1.0F, 1.0F);
	private static final Material CAMERA_RING = new Material(0xFF5C6068, 0.8F, 0.85F);
	private static final Material IR = new Material(0xFF3A1418, 0.9F, 0.9F);
	private static final Material FOAM = new Material(0xFF1E1E21, 0.04F, 0.12F);
	private static final Material INNER = new Material(0xFF27282C, 0.10F, 0.25F);
	private static final Material GLASS = new Material(0xFF0A0F17, 1.0F, 1.0F);
	private static final Material BEZEL = new Material(0xFF9EA4AC, 0.75F, 0.80F);
	private static final Material STRAP_ARM = new Material(0xFFDADBD8, 0.28F, 0.40F);
	private static final Material FABRIC = new Material(0xFF46484E, 0.03F, 0.08F);
	private static final Material PAD = new Material(0xFF2B2C30, 0.05F, 0.12F);

	private static HeadsetMesh held;
	private static HeadsetMesh worn;

	private float[] positions = new float[3 * 4096];
	private float[] normals = new float[3 * 4096];
	private int[] colors = new int[4096];
	private float[] materials = new float[2 * 4096];
	private int vertexCount;
	private int[] quads = new int[4 * 4096];
	private float[] references = new float[3 * 4096];
	private int quadCount;

	private record Material(int color, float specular, float gloss) {
	}

	/** Reference direction used to make face normals point outwards. */
	private interface Outward {
		void at(double x, double y, double z, Vector3f out);
	}

	public static HeadsetMesh held() {
		if (held == null) {
			held = build(false);
		}
		return held;
	}

	public static HeadsetMesh worn() {
		if (worn == null) {
			worn = build(true);
		}
		return worn;
	}

	/** Emits all faces as quads in the entity vertex format. */
	public void emit(PoseStack.Pose pose, VertexConsumer consumer, int light) {
		for (int q = 0; q < this.quadCount; q++) {
			for (int k = 0; k < 4; k++) {
				int v = this.quads[q * 4 + k];
				consumer.addVertex(pose, this.positions[v * 3], this.positions[v * 3 + 1], this.positions[v * 3 + 2])
					.setColor(this.colors[v])
					.setUv(this.materials[v * 2], this.materials[v * 2 + 1])
					.setOverlay(OverlayTexture.NO_OVERLAY)
					.setLight(light)
					.setNormal(pose, this.normals[v * 3], this.normals[v * 3 + 1], this.normals[v * 3 + 2]);
			}
		}
	}

	public int quadCount() {
		return this.quadCount;
	}

	// ------------------------------------------------------------------------------------------
	// Construction

	private static HeadsetMesh build(boolean withStrap) {
		HeadsetMesh mesh = new HeadsetMesh();
		mesh.shell();
		mesh.sensors();
		mesh.cushion();
		mesh.lenses();
		if (withStrap) {
			// While it is held the strap is out of view behind the hands; on the head it wraps around.
			mesh.strapArms(true);
			mesh.backStrap();
		}
		mesh.computeNormals();
		return mesh;
	}

	private static double sgnPow(double value, double exponent) {
		return Math.signum(value) * Math.pow(Math.abs(value), exponent);
	}

	/** Point on the squircle with half sizes (a, b) at parameter t. */
	private static double outlineX(double a, double t) {
		return a * sgnPow(Math.cos(t), 2.0 / N);
	}

	private static double outlineY(double b, double t) {
		return b * sgnPow(Math.sin(t), 2.0 / N);
	}

	/** Outward 2D normal of the squircle (a, b) at parameter t. */
	private static void outlineNormal(double a, double b, double t, double[] out) {
		double x = outlineX(a, t) / a;
		double y = outlineY(b, t) / b;
		double nx = sgnPow(x, N - 1) / a;
		double ny = sgnPow(y, N - 1) / b;
		double len = Math.sqrt(nx * nx + ny * ny);
		out[0] = len > 0 ? nx / len : Math.cos(t);
		out[1] = len > 0 ? ny / len : Math.sin(t);
	}

	/** z of the domed front face at (x, y); the dome fades to zero at the bevel edge. */
	private static double frontZ(double x, double y) {
		double a = A - BEVEL;
		double b = B - BEVEL;
		double s = Math.pow(Math.pow(Math.abs(x / a), N) + Math.pow(Math.abs(y / b), N), 1.0 / N);
		s = Math.min(1.0, s);
		return FRONT - DOME * (1.0 - s * s);
	}

	private void shell() {
		// Rings from the back edge, along the sides, around the rounded front edge, onto the dome.
		java.util.List<double[]> rings = new java.util.ArrayList<>(); // {inset, z, domeScale}
		rings.add(new double[]{0.012, -0.004, -1});
		rings.add(new double[]{0.004, -0.010, -1});
		rings.add(new double[]{0.000, -0.020, -1});
		rings.add(new double[]{0.000, FRONT + BEVEL, -1});
		for (int i = 1; i <= 7; i++) {
			double theta = Math.toRadians(90.0 * i / 7.0);
			rings.add(new double[]{BEVEL * (1.0 - Math.cos(theta)), FRONT + BEVEL - BEVEL * Math.sin(theta), -1});
		}
		for (double s : new double[]{0.86, 0.70, 0.52, 0.34, 0.17}) {
			rings.add(new double[]{BEVEL, 0, s});
		}
		Outward outward = (x, y, z, out) -> out.set((float) x, (float) y, (float) (z + 0.11)).normalize();
		int[][] ids = new int[rings.size()][SEGMENTS];
		for (int r = 0; r < rings.size(); r++) {
			double[] ring = rings.get(r);
			for (int i = 0; i < SEGMENTS; i++) {
				double t = 2.0 * Math.PI * i / SEGMENTS;
				double x;
				double y;
				double z;
				Material material = r >= 9 ? FACE : SHELL;
				if (ring[2] < 0) {
					x = outlineX(A - ring[0], t);
					y = outlineY(B - ring[0], t);
					z = ring[1];
				} else {
					x = outlineX(A - BEVEL, t) * ring[2];
					y = outlineY(B - BEVEL, t) * ring[2];
					z = frontZ(x, y);
				}
				ids[r][i] = this.vertex(x, y, z, material);
			}
		}
		for (int r = 0; r + 1 < rings.size(); r++) {
			for (int i = 0; i < SEGMENTS; i++) {
				int j = (i + 1) % SEGMENTS;
				this.quad(ids[r][i], ids[r][j], ids[r + 1][j], ids[r + 1][i], outward);
			}
		}
		int center = this.vertex(0, 0, FRONT - DOME, FACE);
		int last = rings.size() - 1;
		for (int i = 0; i < SEGMENTS; i++) {
			int j = (i + 1) % SEGMENTS;
			this.quad(ids[last][i], ids[last][j], center, center, outward);
		}
	}

	private void sensors() {
		// Two camera clusters and the depth sensor in the middle, as raised pills on the dome.
		double[][] pills = {{-0.165, 0.040, 0.130}, {0.0, 0.030, 0.096}, {0.165, 0.040, 0.130}};
		Outward forward = (x, y, z, out) -> out.set(0, 0, -1);
		for (double[] pill : pills) {
			double cx = pill[0];
			double w = pill[1];
			double h = pill[2];
			this.pill(cx, 0.004, w + 0.008, h + 0.008, 0.0012, SENSOR_RIM, forward);
			this.pill(cx, 0.004, w, h, 0.0026, SENSOR, forward);
			if (w > 0.035) {
				this.disc(cx, 0.004 + h * 0.24, 0.0105, 0.0036, CAMERA_RING, 20);
				this.disc(cx, 0.004 + h * 0.24, 0.0078, 0.0048, CAMERA, 20);
				this.disc(cx, 0.004 - h * 0.22, 0.0056, 0.0040, CAMERA, 16);
			} else {
				this.disc(cx, 0.004 + h * 0.18, 0.0060, 0.0040, IR, 16);
			}
		}
	}

	/** Rounded vertical pill hugging the front dome, raised by {@code lift}. */
	private void pill(double cx, double cy, double w, double h, double lift, Material material, Outward outward) {
		int segments = 28;
		double r = w * 0.5;
		double straight = Math.max(0, h * 0.5 - r);
		int center = this.vertex(cx, cy, frontZ(cx, cy) - lift, material);
		int[] ring = new int[segments];
		for (int i = 0; i < segments; i++) {
			double a = 2.0 * Math.PI * i / segments;
			double x = cx + r * Math.cos(a);
			double y = cy + r * Math.sin(a) + (Math.sin(a) >= 0 ? straight : -straight);
			ring[i] = this.vertex(x, y, frontZ(x, y) - lift, material);
		}
		for (int i = 0; i < segments; i++) {
			this.quad(center, center, ring[i], ring[(i + 1) % segments], outward);
		}
	}

	/** Small disc on the dome (camera lenses, IR emitter). */
	private void disc(double cx, double cy, double radius, double lift, Material material, int segments) {
		Outward forward = (x, y, z, out) -> out.set(0, 0, -1);
		int center = this.vertex(cx, cy, frontZ(cx, cy) - lift - radius * 0.25, material);
		int[] ring = new int[segments];
		for (int i = 0; i < segments; i++) {
			double a = 2.0 * Math.PI * i / segments;
			double x = cx + radius * Math.cos(a);
			double y = cy + radius * Math.sin(a);
			ring[i] = this.vertex(x, y, frontZ(x, y) - lift, material);
		}
		for (int i = 0; i < segments; i++) {
			this.quad(center, center, ring[i], ring[(i + 1) % segments], forward);
		}
	}

	private static double noseNotch(double x, double t) {
		double lower = Math.max(0.0, Math.min(1.0, -Math.sin(t) * 2.2));
		return 0.058 * Math.exp(-(x * x) / (0.042 * 0.042)) * lower;
	}

	private void cushion() {
		// Profile of the foam ring: {inset from the shell outline, z}. Outer wall, soft face side, inner wall.
		// Inset so a rim of the white shell frames the foam, like on a real headset.
		double[][] profile = {
			{0.026, -0.006}, {0.023, 0.010}, {0.024, 0.026}, {0.029, 0.038}, {0.037, 0.045}, {0.047, 0.047},
			{0.056, 0.044}, {0.063, 0.035}, {0.067, 0.020}, {0.068, 0.004}, {0.068, -0.006},
		};
		double centerInset = 0.047;
		double centerZ = 0.020;
		int[][] ids = new int[profile.length][SEGMENTS];
		float[][] refs = new float[profile.length * SEGMENTS][];
		double[] normal2d = new double[2];
		for (int p = 0; p < profile.length; p++) {
			for (int i = 0; i < SEGMENTS; i++) {
				double t = 2.0 * Math.PI * i / SEGMENTS;
				double inset = profile[p][0];
				double x = outlineX(A - inset, t);
				double y = outlineY(B - inset, t) + noseNotch(x, t);
				ids[p][i] = this.vertex(x, y, profile[p][1], FOAM);
				outlineNormal(A - inset, B - inset, t, normal2d);
				double radial = -(inset - centerInset);
				double dz = profile[p][1] - centerZ;
				refs[p * SEGMENTS + i] = new float[]{(float) (normal2d[0] * radial), (float) (normal2d[1] * radial), (float) dz};
			}
		}
		for (int p = 0; p + 1 < profile.length; p++) {
			for (int i = 0; i < SEGMENTS; i++) {
				int j = (i + 1) % SEGMENTS;
				float[] ref = refs[p * SEGMENTS + i];
				float[] ref2 = refs[(p + 1) * SEGMENTS + j];
				Vector3f r = new Vector3f(ref[0] + ref2[0], ref[1] + ref2[1], ref[2] + ref2[2]);
				this.quad(ids[p][i], ids[p][j], ids[p + 1][j], ids[p + 1][i], (x, y, z, out) -> out.set(r));
			}
		}
		// Dark inner plate behind the lenses, closing the opening.
		int[] ring = new int[SEGMENTS];
		for (int i = 0; i < SEGMENTS; i++) {
			double t = 2.0 * Math.PI * i / SEGMENTS;
			double x = outlineX(A - 0.068, t);
			double y = outlineY(B - 0.068, t) + noseNotch(x, t);
			ring[i] = this.vertex(x, y, -0.006, INNER);
		}
		int center = this.vertex(0, 0.01, -0.006, INNER);
		for (int i = 0; i < SEGMENTS; i++) {
			this.quad(center, center, ring[i], ring[(i + 1) % SEGMENTS], (x, y, z, out) -> out.set(0, 0, 1));
		}
	}

	private void lenses() {
		for (int side = -1; side <= 1; side += 2) {
			double cx = side * 0.100;
			double cy = 0.008;
			int segments = 40;
			Outward back = (x, y, z, out) -> out.set(0, 0, 1);
			// Bezel ring.
			int[] outer = new int[segments];
			int[] inner = new int[segments];
			for (int i = 0; i < segments; i++) {
				double a = 2.0 * Math.PI * i / segments;
				outer[i] = this.vertex(cx + 0.056 * Math.cos(a), cy + 0.056 * Math.sin(a), -0.004, BEZEL);
				inner[i] = this.vertex(cx + 0.047 * Math.cos(a), cy + 0.047 * Math.sin(a), -0.0015, BEZEL);
			}
			for (int i = 0; i < segments; i++) {
				int j = (i + 1) % segments;
				this.quad(outer[i], outer[j], inner[j], inner[i], back);
			}
			// Slightly convex glass.
			int[] glass = new int[segments];
			int[] glassMid = new int[segments];
			for (int i = 0; i < segments; i++) {
				double a = 2.0 * Math.PI * i / segments;
				glass[i] = this.vertex(cx + 0.047 * Math.cos(a), cy + 0.047 * Math.sin(a), -0.0015, GLASS);
				glassMid[i] = this.vertex(cx + 0.025 * Math.cos(a), cy + 0.025 * Math.sin(a), 0.0020, GLASS);
			}
			int center = this.vertex(cx, cy, 0.0030, GLASS);
			Outward dome = (x, y, z, out) -> out.set((float) (x - cx) * 6.0F, (float) (y - cy) * 6.0F, 1.0F);
			for (int i = 0; i < segments; i++) {
				int j = (i + 1) % segments;
				this.quad(glass[i], glass[j], glassMid[j], glassMid[i], dome);
				this.quad(glassMid[i], glassMid[j], center, center, dome);
			}
		}
	}

	/** Band with a rounded rectangular cross-section following a path in the XZ plane. */
	private void band(double[][] path, double halfHeight, double halfThickness, double y, Material material) {
		double[][] section = {
			{1.0, -0.6}, {1.0, 0.6}, {0.75, 0.95}, {0.0, 1.0}, {-0.75, 0.95}, {-1.0, 0.6}, {-1.0, -0.6}, {-0.75, -0.95}, {0.0, -1.0}, {0.75, -0.95},
		};
		int count = path.length;
		int[][] ids = new int[count][section.length];
		for (int p = 0; p < count; p++) {
			double[] prev = path[Math.max(0, p - 1)];
			double[] next = path[Math.min(count - 1, p + 1)];
			double tx = next[0] - prev[0];
			double tz = next[1] - prev[1];
			double len = Math.sqrt(tx * tx + tz * tz);
			tx /= len;
			tz /= len;
			// Horizontal normal of the path (pointing away from the head on the outside).
			double nx = tz;
			double nz = -tx;
			for (int s = 0; s < section.length; s++) {
				double across = section[s][0] * halfThickness;
				double up = section[s][1] * halfHeight;
				ids[p][s] = this.vertex(path[p][0] + nx * across, y + up, path[p][1] + nz * across, material);
			}
		}
		for (int p = 0; p + 1 < count; p++) {
			double cx = (path[p][0] + path[p + 1][0]) * 0.5;
			double cz = (path[p][1] + path[p + 1][1]) * 0.5;
			for (int s = 0; s < section.length; s++) {
				int t = (s + 1) % section.length;
				this.quad(ids[p][s], ids[p][t], ids[p + 1][t], ids[p + 1][s], (x, yy, z, out) -> out.set((float) (x - cx), (float) (yy - y), (float) (z - cz)));
			}
		}
		// Close both ends.
		for (int end : new int[]{0, count - 1}) {
			int center = this.vertex(path[end][0], y, path[end][1], material);
			double[] other = path[end == 0 ? 1 : count - 2];
			float dx = (float) (path[end][0] - other[0]);
			float dz = (float) (path[end][1] - other[1]);
			for (int s = 0; s < section.length; s++) {
				this.quad(center, center, ids[end][s], ids[end][(s + 1) % section.length], (x, yy, z, out) -> out.set(dx, 0, dz));
			}
		}
	}

	/** Distance from the face plane (mesh origin) to the front of a Minecraft head, see HeadsetLayer. */
	static final double FACE_OFFSET = 0.2875;
	// Head-hugging strap: the blocky head is 0.5 wide (+ hat layer), so the strap follows a rounded rectangle.
	private static final double STRAP_HALF_WIDTH = 0.284;
	private static final double STRAP_BACK = FACE_OFFSET + 0.25 + 0.016 + 0.012;
	private static final double STRAP_CORNER = 0.075;
	private static final double ARM_END = 0.30;

	private void strapArms(boolean full) {
		for (int side = -1; side <= 1; side += 2) {
			double sx = side * STRAP_HALF_WIDTH;
			if (full) {
				// Rigid arms running along the sides of the head.
				this.band(new double[][]{{side * (A - 0.016), -0.150}, {sx, -0.090}, {sx, 0.05}, {sx, 0.18}, {sx, ARM_END}}, 0.030, 0.0085, 0.0, STRAP_ARM);
			} else {
				// Short arms, angled back and slightly outwards while the headset is held.
				double hx = side * (A - 0.012);
				this.band(new double[][]{{hx, -0.150}, {hx + side * 0.010, -0.090}, {hx + side * 0.020, -0.020}, {hx + side * 0.034, 0.045}}, 0.030, 0.0085, 0.0, STRAP_ARM);
			}
		}
	}

	private void backStrap() {
		// Fabric strap: from the end of one arm around the back of the head to the other arm.
		java.util.List<double[]> path = new java.util.ArrayList<>();
		double w = STRAP_HALF_WIDTH;
		double r = STRAP_CORNER;
		path.add(new double[]{-w, ARM_END - 0.02});
		path.add(new double[]{-w, STRAP_BACK - r});
		for (int i = 1; i <= 8; i++) {
			double u = Math.PI * 0.5 * i / 8;
			path.add(new double[]{-w + r - r * Math.cos(u), STRAP_BACK - r + r * Math.sin(u)});
		}
		for (int i = 0; i <= 8; i++) {
			double u = Math.PI * 0.5 * i / 8;
			path.add(new double[]{w - r + r * Math.sin(u), STRAP_BACK - r * (1 - Math.cos(u))});
		}
		path.add(new double[]{w, ARM_END - 0.02});
		this.band(path.toArray(double[][]::new), 0.034, 0.007, 0.0, FABRIC);
		// Padded piece at the back of the head.
		this.band(new double[][]{{-0.15, STRAP_BACK + 0.006}, {-0.05, STRAP_BACK + 0.009}, {0.05, STRAP_BACK + 0.009}, {0.15, STRAP_BACK + 0.006}}, 0.052, 0.010, -0.004, PAD);
	}

	// ------------------------------------------------------------------------------------------
	// Storage

	private int vertex(double x, double y, double z, Material material) {
		if (this.vertexCount == this.colors.length) {
			int capacity = this.colors.length * 2;
			this.positions = java.util.Arrays.copyOf(this.positions, capacity * 3);
			this.normals = java.util.Arrays.copyOf(this.normals, capacity * 3);
			this.colors = java.util.Arrays.copyOf(this.colors, capacity);
			this.materials = java.util.Arrays.copyOf(this.materials, capacity * 2);
		}
		int v = this.vertexCount++;
		this.positions[v * 3] = (float) x;
		this.positions[v * 3 + 1] = (float) y;
		this.positions[v * 3 + 2] = (float) z;
		this.colors[v] = material.color();
		this.materials[v * 2] = material.specular();
		this.materials[v * 2 + 1] = material.gloss();
		return v;
	}

	private void quad(int a, int b, int c, int d, Outward outward) {
		if (this.quadCount * 4 == this.quads.length) {
			this.quads = java.util.Arrays.copyOf(this.quads, this.quads.length * 2);
			this.references = java.util.Arrays.copyOf(this.references, this.references.length * 2);
		}
		int q = this.quadCount++;
		this.quads[q * 4] = a;
		this.quads[q * 4 + 1] = b;
		this.quads[q * 4 + 2] = c;
		this.quads[q * 4 + 3] = d;
		double x = 0;
		double y = 0;
		double z = 0;
		for (int v : new int[]{a, b, c, d}) {
			x += this.positions[v * 3] * 0.25;
			y += this.positions[v * 3 + 1] * 0.25;
			z += this.positions[v * 3 + 2] * 0.25;
		}
		Vector3f ref = new Vector3f();
		outward.at(x, y, z, ref);
		this.references[q * 3] = ref.x;
		this.references[q * 3 + 1] = ref.y;
		this.references[q * 3 + 2] = ref.z;
	}

	/** Smooth normals: area weighted, oriented face normals averaged per vertex. */
	private void computeNormals() {
		java.util.Arrays.fill(this.normals, 0, this.vertexCount * 3, 0.0F);
		Vector3f p0 = new Vector3f();
		Vector3f p1 = new Vector3f();
		Vector3f p2 = new Vector3f();
		Vector3f p3 = new Vector3f();
		Vector3f n = new Vector3f();
		for (int q = 0; q < this.quadCount; q++) {
			this.load(this.quads[q * 4], p0);
			this.load(this.quads[q * 4 + 1], p1);
			this.load(this.quads[q * 4 + 2], p2);
			this.load(this.quads[q * 4 + 3], p3);
			// Diagonal cross product handles quads and the degenerate fan quads alike.
			n.set(p2).sub(p0).cross(new Vector3f(p3).sub(p1));
			if (n.lengthSquared() < 1.0E-14F) {
				n.set(p1).sub(p0).cross(new Vector3f(p2).sub(p0));
			}
			if (n.dot(this.references[q * 3], this.references[q * 3 + 1], this.references[q * 3 + 2]) < 0) {
				n.negate();
			}
			for (int k = 0; k < 4; k++) {
				int v = this.quads[q * 4 + k];
				this.normals[v * 3] += n.x;
				this.normals[v * 3 + 1] += n.y;
				this.normals[v * 3 + 2] += n.z;
			}
		}
		for (int v = 0; v < this.vertexCount; v++) {
			n.set(this.normals[v * 3], this.normals[v * 3 + 1], this.normals[v * 3 + 2]);
			if (n.lengthSquared() > 0) {
				n.normalize();
			} else {
				n.set(0, 0, -1);
			}
			this.normals[v * 3] = n.x;
			this.normals[v * 3 + 1] = n.y;
			this.normals[v * 3 + 2] = n.z;
		}
	}

	private void load(int v, Vector3f out) {
		out.set(this.positions[v * 3], this.positions[v * 3 + 1], this.positions[v * 3 + 2]);
	}
}
