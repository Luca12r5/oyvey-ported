package dev.visor.render;

import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * A panel floating in front of the player: a vertical cylinder section (curved around the viewer,
 * like the home panel) or a flat plane when the radius is 0. Coordinates are camera relative
 * world space, measured in blocks.
 */
public final class PanelSurface implements Surface {
	/** Point on the surface at the centre of the layout. */
	public final Vector3f center = new Vector3f();
	/** Panel frame: right along the panel, up along the panel, forward = from the viewer into the panel. */
	public final Vector3f right = new Vector3f(1, 0, 0);
	public final Vector3f up = new Vector3f(0, 1, 0);
	public final Vector3f forward = new Vector3f(0, 0, -1);
	/** Curvature radius in blocks (0 = flat). */
	public float radius;
	/** Size of one layout pixel in blocks. */
	public float pixelSize = 0.002F;
	public float width;
	public float height;

	public PanelSurface(float width, float height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * @param yaw       Minecraft yaw (degrees) of the direction from the viewer into the panel
	 * @param elevation angle of that direction above the horizon (degrees); a panel below eye level
	 *                  uses a negative value so it faces up towards the eyes
	 */
	public void setOrientation(float yaw, float elevation) {
		float yawRad = yaw * Mth.DEG_TO_RAD;
		float elevationRad = elevation * Mth.DEG_TO_RAD;
		// Minecraft yaw 0 looks towards +Z, yaw 90 towards -X.
		float fx = -Mth.sin(yawRad);
		float fz = Mth.cos(yawRad);
		this.forward.set(fx * Mth.cos(elevationRad), Mth.sin(elevationRad), fz * Mth.cos(elevationRad)).normalize();
		this.right.set(-fz, 0.0F, fx).normalize();
		this.up.set(this.right).cross(this.forward).normalize();
	}

	@Override
	public void map(float x, float y, Vector3f out) {
		float u = (x - this.width * 0.5F) * this.pixelSize;
		float v = (y - this.height * 0.5F) * this.pixelSize;
		out.set(this.center);
		if (this.radius > 0.0F) {
			float theta = u / this.radius;
			out.fma(this.radius * Mth.sin(theta), this.right);
			out.fma(-(this.radius - this.radius * Mth.cos(theta)), this.forward);
		} else {
			out.fma(u, this.right);
		}
		out.fma(-v, this.up);
	}

	@Override
	public float segmentLength() {
		return this.radius > 0.0F ? 24.0F : 0.0F;
	}

	/**
	 * Intersects a ray (camera relative, same space as {@link #center}) with the panel.
	 *
	 * @return layout coordinates of the hit (may lie outside the layout), or {@code null} on a miss
	 */
	public float @Nullable [] raycast(Vector3f origin, Vector3f direction) {
		float u;
		float v;
		if (this.radius > 0.0F) {
			// The cylinder axis passes through (center - forward * radius) along the panel's up vector.
			Vector3f axisPoint = new Vector3f(this.center).fma(-this.radius, this.forward);
			Vector3f rel = new Vector3f(origin).sub(axisPoint);
			Vector3f d = new Vector3f(direction).fma(-direction.dot(this.up), this.up);
			Vector3f o = new Vector3f(rel).fma(-rel.dot(this.up), this.up);
			float a = d.dot(d);
			float b = 2.0F * o.dot(d);
			float c = o.dot(o) - this.radius * this.radius;
			float disc = b * b - 4.0F * a * c;
			if (a < 1.0E-6F || disc < 0.0F) {
				return null;
			}
			float root = Mth.sqrt(disc);
			float near = (-b - root) / (2.0F * a);
			if (near > 0.0F && this.withinArc(new Vector3f(rel).fma(near, direction))) {
				// Seen from outside (behind the panel): the ray meets the panel's back first.
				return null;
			}
			float t = (-b + root) / (2.0F * a);
			if (t <= 0.0F) {
				return null;
			}
			Vector3f hit = new Vector3f(rel).fma(t, direction);
			float front = hit.dot(this.forward);
			if (front <= 0.0F) {
				return null;
			}
			u = (float) Math.atan2(hit.dot(this.right), front) * this.radius;
			v = -hit.dot(this.up);
		} else {
			float denom = direction.dot(this.forward);
			if (denom < 1.0E-6F) {
				// Parallel, or looking at the panel from behind: only the front can be pointed at.
				return null;
			}
			float t = new Vector3f(this.center).sub(origin).dot(this.forward) / denom;
			if (t <= 0.0F) {
				return null;
			}
			Vector3f hit = new Vector3f(origin).fma(t, direction).sub(this.center);
			u = hit.dot(this.right);
			v = -hit.dot(this.up);
		}
		return new float[]{u / this.pixelSize + this.width * 0.5F, v / this.pixelSize + this.height * 0.5F};
	}

	/** Whether a point on the cylinder (relative to the axis point) lies on the panel itself. */
	private boolean withinArc(Vector3f point) {
		float front = point.dot(this.forward);
		if (front <= 0.0F) {
			return false;
		}
		float u = (float) Math.atan2(point.dot(this.right), front) * this.radius;
		float v = -point.dot(this.up);
		return Math.abs(u) <= this.width * 0.5F * this.pixelSize && Math.abs(v) <= this.height * 0.5F * this.pixelSize;
	}
}
