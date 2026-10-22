package dev.visor.render;

import org.joml.Vector3f;

/** Plain screen space: layout pixels are window pixels. */
public final class ScreenSurface implements Surface {
	public static final ScreenSurface INSTANCE = new ScreenSurface();

	private ScreenSurface() {
	}

	@Override
	public void map(float x, float y, Vector3f out) {
		out.set(x, y, 0.0F);
	}

	@Override
	public float segmentLength() {
		return 0.0F;
	}
}
