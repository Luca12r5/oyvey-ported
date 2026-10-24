package dev.lego.cosmetic;

/**
 * Tiny geometry builder for cosmetics (no Minecraft classes).
 * Cosmetic space: units = model pixels (1/16 block), x = player's right, y = up, z = forward.
 * Emitted quads are already transformed; the sink draws them (Minecraft or the software preview).
 */
public final class G {
    public interface Sink {
        /** 4 corners (x,y,z), uv per corner, ARGB tint, emissive flag, face normal. */
        void quad(String tex, float[] p, float[] uv, int argb, boolean glow, float nx, float ny, float nz);
    }

    private final Sink sink;
    private float[] m = identity();
    private final java.util.ArrayDeque<float[]> stack = new java.util.ArrayDeque<>();
    public int color = 0xFFFFFFFF;
    public boolean glow = false;

    public G(Sink sink) { this.sink = sink; }

    // ------------------------------------------------------------------ matrix
    private static float[] identity() { return new float[]{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1}; }

    public void push() { stack.push(m.clone()); }
    public void pop() { m = stack.pop(); }

    private void mul(float[] b) {
        float[] r = new float[16];
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
                float s = 0;
                for (int k = 0; k < 4; k++) s += m[i * 4 + k] * b[k * 4 + j];
                r[i * 4 + j] = s;
            }
        m = r;
    }

    public G translate(float x, float y, float z) { mul(new float[]{1, 0, 0, x, 0, 1, 0, y, 0, 0, 1, z, 0, 0, 0, 1}); return this; }
    public G scale(float x, float y, float z) { mul(new float[]{x, 0, 0, 0, 0, y, 0, 0, 0, 0, z, 0, 0, 0, 0, 1}); return this; }
    public G scale(float s) { return scale(s, s, s); }
    public G rotX(float deg) { float c = (float) Math.cos(Math.toRadians(deg)), s = (float) Math.sin(Math.toRadians(deg)); mul(new float[]{1, 0, 0, 0, 0, c, -s, 0, 0, s, c, 0, 0, 0, 0, 1}); return this; }
    public G rotY(float deg) { float c = (float) Math.cos(Math.toRadians(deg)), s = (float) Math.sin(Math.toRadians(deg)); mul(new float[]{c, 0, s, 0, 0, 1, 0, 0, -s, 0, c, 0, 0, 0, 0, 1}); return this; }
    public G rotZ(float deg) { float c = (float) Math.cos(Math.toRadians(deg)), s = (float) Math.sin(Math.toRadians(deg)); mul(new float[]{c, -s, 0, 0, s, c, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1}); return this; }

    private float[] tp(float x, float y, float z) {
        return new float[]{m[0] * x + m[1] * y + m[2] * z + m[3], m[4] * x + m[5] * y + m[6] * z + m[7], m[8] * x + m[9] * y + m[10] * z + m[11]};
    }

    public G color(int argb) { color = argb; return this; }
    public G glow(boolean g) { glow = g; return this; }

    // ------------------------------------------------------------------ primitives
    /** Quad from 4 local points with uv (u,v per corner). */
    public void quad(String tex, float[] a, float[] b, float[] c, float[] d, float[] uv) {
        float[] A = tp(a[0], a[1], a[2]), B = tp(b[0], b[1], b[2]), C = tp(c[0], c[1], c[2]), D = tp(d[0], d[1], d[2]);
        float ux = B[0] - A[0], uy = B[1] - A[1], uz = B[2] - A[2];
        float vx = D[0] - A[0], vy = D[1] - A[1], vz = D[2] - A[2];
        float nx = uy * vz - uz * vy, ny = uz * vx - ux * vz, nz = ux * vy - uy * vx;
        float l = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (l < 1e-6f) { nx = 0; ny = 1; nz = 0; } else { nx /= l; ny /= l; nz /= l; }
        sink.quad(tex, new float[]{A[0], A[1], A[2], B[0], B[1], B[2], C[0], C[1], C[2], D[0], D[1], D[2]}, uv, color, glow, nx, ny, nz);
    }

    /** Rectangle in the local XY plane (facing +z), centered at (cx, cy); uv rect u0,v0 (top-left) .. u1,v1. */
    public void plane(String tex, float cx, float cy, float w, float h, float u0, float v0, float u1, float v1) {
        float x0 = cx - w / 2, x1 = cx + w / 2, y0 = cy - h / 2, y1 = cy + h / 2;
        quad(tex, new float[]{x0, y1, 0}, new float[]{x1, y1, 0}, new float[]{x1, y0, 0}, new float[]{x0, y0, 0},
                new float[]{u0, v0, u1, v0, u1, v1, u0, v1});
    }

    /** Plane whose top-left corner is the origin, extending +x and -y (like a flag hanging down). */
    public void planeTL(String tex, float w, float h, float u0, float v0, float u1, float v1) {
        quad(tex, new float[]{0, 0, 0}, new float[]{w, 0, 0}, new float[]{w, -h, 0}, new float[]{0, -h, 0},
                new float[]{u0, v0, u1, v0, u1, v1, u0, v1});
    }

    /** Axis aligned box; every face uses the same uv rect. */
    public void box(String tex, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1) {
        float[] uv = {u0, v0, u1, v0, u1, v1, u0, v1};
        quad(tex, new float[]{x0, y1, z1}, new float[]{x1, y1, z1}, new float[]{x1, y0, z1}, new float[]{x0, y0, z1}, uv); // front
        quad(tex, new float[]{x1, y1, z0}, new float[]{x0, y1, z0}, new float[]{x0, y0, z0}, new float[]{x1, y0, z0}, uv); // back
        quad(tex, new float[]{x0, y1, z0}, new float[]{x0, y1, z1}, new float[]{x0, y0, z1}, new float[]{x0, y0, z0}, uv); // left(-x)
        quad(tex, new float[]{x1, y1, z1}, new float[]{x1, y1, z0}, new float[]{x1, y0, z0}, new float[]{x1, y0, z1}, uv); // right(+x)
        quad(tex, new float[]{x0, y1, z0}, new float[]{x1, y1, z0}, new float[]{x1, y1, z1}, new float[]{x0, y1, z1}, uv); // top
        quad(tex, new float[]{x0, y0, z1}, new float[]{x1, y0, z1}, new float[]{x1, y0, z0}, new float[]{x0, y0, z0}, uv); // bottom
    }

    /** Box with separate uv rects: {front, back, left, right, top, bottom}, each {u0,v0,u1,v1}. */
    public void box6(String tex, float x0, float y0, float z0, float x1, float y1, float z1, float[][] f) {
        quad(tex, new float[]{x0, y1, z1}, new float[]{x1, y1, z1}, new float[]{x1, y0, z1}, new float[]{x0, y0, z1}, r(f[0]));
        quad(tex, new float[]{x1, y1, z0}, new float[]{x0, y1, z0}, new float[]{x0, y0, z0}, new float[]{x1, y0, z0}, r(f[1]));
        quad(tex, new float[]{x0, y1, z0}, new float[]{x0, y1, z1}, new float[]{x0, y0, z1}, new float[]{x0, y0, z0}, r(f[2]));
        quad(tex, new float[]{x1, y1, z1}, new float[]{x1, y1, z0}, new float[]{x1, y0, z0}, new float[]{x1, y0, z1}, r(f[3]));
        quad(tex, new float[]{x0, y1, z0}, new float[]{x1, y1, z0}, new float[]{x1, y1, z1}, new float[]{x0, y1, z1}, r(f[4]));
        quad(tex, new float[]{x0, y0, z1}, new float[]{x1, y0, z1}, new float[]{x1, y0, z0}, new float[]{x0, y0, z0}, r(f[5]));
    }

    private static float[] r(float[] q) { return new float[]{q[0], q[1], q[2], q[1], q[2], q[3], q[0], q[3]}; }

    /** Cylinder along y from y0 to y1 (open or capped), uv wraps horizontally. */
    public void cylinder(String tex, float r0, float r1, float y0, float y1, int seg, boolean caps, float u0, float v0, float u1, float v1) {
        for (int i = 0; i < seg; i++) {
            double a0 = Math.PI * 2 * i / seg, a1 = Math.PI * 2 * (i + 1) / seg;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0), c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            float uu0 = u0 + (u1 - u0) * i / seg, uu1 = u0 + (u1 - u0) * (i + 1) / seg;
            quad(tex, new float[]{c0 * r1, y1, s0 * r1}, new float[]{c1 * r1, y1, s1 * r1}, new float[]{c1 * r0, y0, s1 * r0}, new float[]{c0 * r0, y0, s0 * r0},
                    new float[]{uu0, v0, uu1, v0, uu1, v1, uu0, v1});
            if (caps) {
                float um = (u0 + u1) / 2, vm = (v0 + v1) / 2;
                quad(tex, new float[]{0, y1, 0}, new float[]{0, y1, 0}, new float[]{c1 * r1, y1, s1 * r1}, new float[]{c0 * r1, y1, s0 * r1},
                        new float[]{um, vm, um, vm, um, v0, um, v0});
                if (r0 > 0.01f) quad(tex, new float[]{0, y0, 0}, new float[]{c0 * r0, y0, s0 * r0}, new float[]{c1 * r0, y0, s1 * r0}, new float[]{0, y0, 0},
                        new float[]{um, vm, um, v1, um, v1, um, vm});
            }
        }
    }

    /** Flat ring (annulus) in the XZ plane at height y. */
    public void ring(String tex, float rIn, float rOut, float y, int seg, float u0, float v0, float u1, float v1) {
        for (int i = 0; i < seg; i++) {
            double a0 = Math.PI * 2 * i / seg, a1 = Math.PI * 2 * (i + 1) / seg;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0), c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            float uu0 = u0 + (u1 - u0) * i / seg, uu1 = u0 + (u1 - u0) * (i + 1) / seg;
            quad(tex, new float[]{c0 * rOut, y, s0 * rOut}, new float[]{c1 * rOut, y, s1 * rOut}, new float[]{c1 * rIn, y, s1 * rIn}, new float[]{c0 * rIn, y, s0 * rIn},
                    new float[]{uu0, v0, uu1, v0, uu1, v1, uu0, v1});
        }
    }

    /** Torus around the y axis. */
    public void torus(String tex, float R, float r, int seg, int side, float u0, float v0, float u1, float v1) {
        for (int i = 0; i < seg; i++) {
            double a0 = Math.PI * 2 * i / seg, a1 = Math.PI * 2 * (i + 1) / seg;
            for (int j = 0; j < side; j++) {
                double b0 = Math.PI * 2 * j / side, b1 = Math.PI * 2 * (j + 1) / side;
                float[] p00 = tor(R, r, a0, b0), p10 = tor(R, r, a1, b0), p11 = tor(R, r, a1, b1), p01 = tor(R, r, a0, b1);
                float uu0 = u0 + (u1 - u0) * i / seg, uu1 = u0 + (u1 - u0) * (i + 1) / seg;
                float vv0 = v0 + (v1 - v0) * j / side, vv1 = v0 + (v1 - v0) * (j + 1) / side;
                quad(tex, p00, p10, p11, p01, new float[]{uu0, vv0, uu1, vv0, uu1, vv1, uu0, vv1});
            }
        }
    }

    private static float[] tor(float R, float r, double a, double b) {
        double c = R + r * Math.cos(b);
        return new float[]{(float) (Math.cos(a) * c), (float) (r * Math.sin(b)), (float) (Math.sin(a) * c)};
    }

    /** UV sphere. */
    public void sphere(String tex, float r, int seg, int rings, float u0, float v0, float u1, float v1) {
        for (int i = 0; i < rings; i++) {
            double t0 = Math.PI * i / rings - Math.PI / 2, t1 = Math.PI * (i + 1) / rings - Math.PI / 2;
            for (int j = 0; j < seg; j++) {
                double a0 = Math.PI * 2 * j / seg, a1 = Math.PI * 2 * (j + 1) / seg;
                float[] p00 = sph(r, t0, a0), p01 = sph(r, t0, a1), p11 = sph(r, t1, a1), p10 = sph(r, t1, a0);
                float uu0 = u0 + (u1 - u0) * j / seg, uu1 = u0 + (u1 - u0) * (j + 1) / seg;
                float vv0 = v1 - (v1 - v0) * i / rings, vv1 = v1 - (v1 - v0) * (i + 1) / rings;
                quad(tex, p10, p11, p01, p00, new float[]{uu0, vv1, uu1, vv1, uu1, vv0, uu0, vv0});
            }
        }
    }

    private static float[] sph(float r, double t, double a) {
        return new float[]{(float) (Math.cos(t) * Math.cos(a) * r), (float) (Math.sin(t) * r), (float) (Math.cos(t) * Math.sin(a) * r)};
    }

    /** Two crossed planes (visible from any side), e.g. particles / flames. */
    public void cross(String tex, float cx, float cy, float cz, float w, float h, float u0, float v0, float u1, float v1) {
        push();
        translate(cx, cy, cz);
        plane(tex, 0, 0, w, h, u0, v0, u1, v1);
        rotY(90);
        plane(tex, 0, 0, w, h, u0, v0, u1, v1);
        pop();
    }
}
