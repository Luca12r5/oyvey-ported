package dev.visor.donut;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Doctor Donut's rigged mesh, read from the compact {@code .ddm} file: two textured parts (front
 * and back), a skeleton and animation clips stored as densely sampled skin matrices.
 */
public final class DonutModel {
	private static final int MAGIC = 0x44444D31;
	/** Bones the shader has room for. */
	public static final int MAX_BONES = 64;
	/** Floats per vertex in the file: position, normal, uv. */
	private static final int FILE_FLOATS = 8;

	public record Part(float[] vertices, int[] joints, float[] weights, int[] indices) {
		public int vertexCount() {
			return this.vertices.length / FILE_FLOATS;
		}
	}

	public record Clip(float seconds, float[][] frames) {
	}

	/** Size of the character standing in his rest pose: height above his feet and body radius. */
	public record Bounds(float height, float radius) {
	}

	private final String[] bones;
	private final Part[] parts;
	private final Map<String, Clip> clips;
	private final Bounds bounds;

	private DonutModel(String[] bones, Part[] parts, Map<String, Clip> clips) {
		this.bones = bones;
		this.parts = parts;
		this.clips = clips;
		this.bounds = measure(parts);
	}

	public int boneCount() {
		return this.bones.length;
	}

	public Bounds bounds() {
		return this.bounds;
	}

	/** The radius leaves out the outermost fifth of the vertices (the hands), so it hugs the body. */
	private static Bounds measure(Part[] parts) {
		int count = 0;
		for (Part part : parts) {
			count += part.vertexCount();
		}
		float[] radii = new float[count];
		float top = 0.0F;
		int i = 0;
		for (Part part : parts) {
			for (int v = 0; v < part.vertexCount(); v++) {
				float x = part.vertices[v * FILE_FLOATS];
				float y = part.vertices[v * FILE_FLOATS + 1];
				float z = part.vertices[v * FILE_FLOATS + 2];
				top = Math.max(top, y);
				radii[i++] = (float) Math.sqrt(x * x + z * z);
			}
		}
		Arrays.sort(radii);
		return new Bounds(top, radii[Math.min(radii.length - 1, (int) (radii.length * 0.8F))]);
	}

	public Part[] parts() {
		return this.parts;
	}

	/** Length of a clip in seconds, or 0 when there is no such clip. */
	public double duration(String clip) {
		Clip found = this.clips.get(clip);
		return found == null ? 0.0 : found.seconds;
	}

	public static DonutModel read(InputStream stream) throws IOException {
		if (stream == null) {
			throw new IOException("Doctor Donut model is missing");
		}
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(stream))) {
			if (in.readInt() != MAGIC) {
				throw new IOException("Unsupported Doctor Donut model");
			}
			int boneCount = count(in, MAX_BONES);
			int partCount = count(in, 8);
			int clipCount = count(in, 32);
			String[] bones = new String[boneCount];
			for (int i = 0; i < boneCount; i++) {
				bones[i] = in.readUTF();
			}
			Part[] parts = new Part[partCount];
			for (int p = 0; p < partCount; p++) {
				int vertexCount = count(in, 100_000);
				int indexCount = count(in, 300_000);
				if (indexCount % 3 != 0) {
					throw new IOException("Incomplete triangle");
				}
				float[] vertices = new float[vertexCount * FILE_FLOATS];
				int[] joints = new int[vertexCount * 4];
				float[] weights = new float[vertexCount * 4];
				int[] indices = new int[indexCount];
				for (int v = 0; v < vertexCount; v++) {
					for (int c = 0; c < FILE_FLOATS; c++) {
						vertices[v * FILE_FLOATS + c] = finite(in);
					}
					for (int c = 0; c < 4; c++) {
						int joint = in.readInt();
						if (joint < 0 || joint >= boneCount) {
							throw new IOException("Invalid bone reference");
						}
						joints[v * 4 + c] = joint;
					}
					float total = 0.0F;
					for (int c = 0; c < 4; c++) {
						float weight = finite(in);
						if (weight < 0.0F || weight > 1.0F) {
							throw new IOException("Invalid skin weight");
						}
						weights[v * 4 + c] = weight;
						total += weight;
					}
					if (Math.abs(total - 1.0F) > 0.001F) {
						throw new IOException("Skin weights do not add up to one");
					}
				}
				for (int i = 0; i < indexCount; i++) {
					indices[i] = in.readInt();
					if (indices[i] < 0 || indices[i] >= vertexCount) {
						throw new IOException("Invalid mesh index");
					}
				}
				parts[p] = new Part(vertices, joints, weights, indices);
			}
			Map<String, Clip> clips = new LinkedHashMap<>();
			for (int c = 0; c < clipCount; c++) {
				String name = in.readUTF();
				float seconds = finite(in);
				if (seconds <= 0.0F || seconds > 60.0F) {
					throw new IOException("Invalid clip length");
				}
				int frameCount = count(in, 1801);
				if (frameCount < 2) {
					throw new IOException("A clip needs at least two samples");
				}
				float[][] frames = new float[frameCount][boneCount * 16];
				for (float[] frame : frames) {
					for (int i = 0; i < frame.length; i++) {
						frame[i] = finite(in);
					}
				}
				if (clips.put(name, new Clip(seconds, frames)) != null) {
					throw new IOException("Duplicate clip " + name);
				}
			}
			if (in.read() != -1) {
				throw new IOException("Unexpected data after the model");
			}
			return new DonutModel(bones, parts, Map.copyOf(clips));
		}
	}

	private static int count(DataInputStream in, int maximum) throws IOException {
		int value = in.readInt();
		if (value < 1 || value > maximum) {
			throw new IOException("Invalid count in model");
		}
		return value;
	}

	private static float finite(DataInputStream in) throws IOException {
		float value = in.readFloat();
		if (!Float.isFinite(value)) {
			throw new IOException("Non-finite value in model");
		}
		return value;
	}

	/**
	 * Writes the skin matrices of {@code clip} at {@code time} (looping) into {@code target}, 16
	 * column-major floats per bone. An unknown clip gives the rest pose.
	 */
	public void pose(String clip, double time, float[] target) {
		Clip found = this.clips.get(clip);
		if (found == null) {
			Arrays.fill(target, 0.0F);
			for (int b = 0; b < this.bones.length; b++) {
				for (int j = 0; j < 4; j++) {
					target[b * 16 + j * 5] = 1.0F;
				}
			}
			return;
		}
		double wrapped = ((time % found.seconds) + found.seconds) % found.seconds;
		float frame = (float) (wrapped / found.seconds * (found.frames.length - 1));
		int a = Math.min((int) frame, found.frames.length - 2);
		float t = frame - a;
		float[] from = found.frames[a];
		float[] to = found.frames[a + 1];
		int length = this.bones.length * 16;
		for (int i = 0; i < length; i++) {
			target[i] = from[i] + (to[i] - from[i]) * t;
		}
	}

	/** Vertex data of one part for the GPU: position, uv, normal, joints, weights (16 floats). */
	public static void writeVertices(Part part, ByteBuffer out) {
		for (int v = 0; v < part.vertexCount(); v++) {
			int src = v * FILE_FLOATS;
			out.putFloat(part.vertices[src]).putFloat(part.vertices[src + 1]).putFloat(part.vertices[src + 2]);
			out.putFloat(part.vertices[src + 6]).putFloat(part.vertices[src + 7]);
			out.putFloat(part.vertices[src + 3]).putFloat(part.vertices[src + 4]).putFloat(part.vertices[src + 5]);
			for (int c = 0; c < 4; c++) {
				out.putFloat(part.joints[v * 4 + c]);
			}
			for (int c = 0; c < 4; c++) {
				out.putFloat(part.weights[v * 4 + c]);
			}
		}
	}
}
