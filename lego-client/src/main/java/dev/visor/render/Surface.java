package dev.visor.render;

import org.joml.Vector3f;

/**
 * Where a 2D layout lives. A surface maps layout pixels (origin top-left, y down) to the space
 * the canvas is flushed in: camera-relative world coordinates for panels, or screen pixels.
 */
public interface Surface {
	void map(float x, float y, Vector3f out);

	/** Longest quad edge (layout pixels) before quads are split to follow the surface; 0 = flat. */
	float segmentLength();
}
