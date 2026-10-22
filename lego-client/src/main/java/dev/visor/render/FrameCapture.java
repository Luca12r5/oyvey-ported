package dev.visor.render;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Camera matrices of the current frame's world render, captured as the level renders. */
public final class FrameCapture {
	/** Camera rotation (world to view, no translation). */
	public static final Matrix4f VIEW = new Matrix4f();
	/** Projection used for the world, including view bobbing and nausea distortion. */
	public static final Matrix4f PROJECTION = new Matrix4f();
	public static Vec3 cameraPosition = Vec3.ZERO;
	public static boolean valid;

	private FrameCapture() {
	}

	public static void capture(Matrix4f view, Matrix4f projection, Vec3 camera) {
		VIEW.set(view);
		PROJECTION.set(projection);
		cameraPosition = camera;
		valid = true;
	}
}
