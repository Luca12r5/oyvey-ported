package dev.visor.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.lwjgl.system.MemoryUtil;

/** CPU side vertex storage in {@link VisorPipelines#UI_FORMAT} layout (64 bytes per vertex). */
final class UiMesh implements AutoCloseable {
	static final int STRIDE = 64;
	private ByteBuffer buffer;
	private int vertices;

	UiMesh(int initialVertices) {
		this.buffer = MemoryUtil.memAlloc(initialVertices * STRIDE).order(ByteOrder.nativeOrder());
	}

	int vertexCount() {
		return this.vertices;
	}

	void reset() {
		this.vertices = 0;
	}

	void ensure(int extraVertices) {
		int needed = (this.vertices + extraVertices) * STRIDE;
		if (needed > this.buffer.capacity()) {
			int capacity = Math.max(needed, this.buffer.capacity() * 2);
			this.buffer = MemoryUtil.memRealloc(this.buffer, capacity).order(ByteOrder.nativeOrder());
		}
	}

	void vertex(
		float x, float y, float z, int argb, float u, float v, float localX, float localY,
		float shapeX, float shapeY, float shapeZ, float shapeW, float mode, float extraY, float extraZ, float extraW
	) {
		this.ensure(1);
		int base = this.vertices * STRIDE;
		ByteBuffer b = this.buffer;
		b.putFloat(base, x);
		b.putFloat(base + 4, y);
		b.putFloat(base + 8, z);
		b.put(base + 12, (byte) (argb >> 16));
		b.put(base + 13, (byte) (argb >> 8));
		b.put(base + 14, (byte) argb);
		b.put(base + 15, (byte) (argb >>> 24));
		b.putFloat(base + 16, u);
		b.putFloat(base + 20, v);
		b.putFloat(base + 24, localX);
		b.putFloat(base + 28, localY);
		b.putFloat(base + 32, shapeX);
		b.putFloat(base + 36, shapeY);
		b.putFloat(base + 40, shapeZ);
		b.putFloat(base + 44, shapeW);
		b.putFloat(base + 48, mode);
		b.putFloat(base + 52, extraY);
		b.putFloat(base + 56, extraZ);
		b.putFloat(base + 60, extraW);
		this.vertices++;
	}

	/** View of the written vertices, valid until the next write. */
	ByteBuffer data() {
		return MemoryUtil.memByteBuffer(MemoryUtil.memAddress(this.buffer), this.vertices * STRIDE);
	}

	@Override
	public void close() {
		MemoryUtil.memFree(this.buffer);
	}
}
