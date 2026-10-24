package dev.lego.cosmetic;

/** Extra geometry helpers on top of {@link G} (all coordinates in the current local space of g). */
final class Geo {
    private Geo() { }

    static float lerp(float a, float b, float t) { return a + (b - a) * t; }
    static float sin(double v) { return (float) Math.sin(v); }
    static float cos(double v) { return (float) Math.cos(v); }

    /** Tapered tube from a to b (open), uv wraps around. */
    static void tube(G g, String tex, float[] a, float[] b, float r0, float r1, int sides) {
        tube(g, tex, a, b, r0, r1, sides, 0, 0, 1, 1);
    }

    static void tube(G g, String tex, float[] a, float[] b, float r0, float r1, int sides, float u0, float v0, float u1, float v1) {
        float dx = b[0] - a[0], dy = b[1] - a[1], dz = b[2] - a[2];
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-4f) return;
        dx /= len; dy /= len; dz /= len;
        float ux, uy, uz;
        if (Math.abs(dy) < 0.9f) { ux = dz; uy = 0; uz = -dx; } else { ux = 0; uy = -dz; uz = dy; }
        float ul = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= ul; uy /= ul; uz /= ul;
        float vx = dy * uz - dz * uy, vy = dz * ux - dx * uz, vz = dx * uy - dy * ux;
        for (int i = 0; i < sides; i++) {
            double t0 = Math.PI * 2 * i / sides, t1 = Math.PI * 2 * (i + 1) / sides;
            float c0 = (float) Math.cos(t0), s0 = (float) Math.sin(t0), c1 = (float) Math.cos(t1), s1 = (float) Math.sin(t1);
            float[] p00 = {a[0] + (ux * c0 + vx * s0) * r0, a[1] + (uy * c0 + vy * s0) * r0, a[2] + (uz * c0 + vz * s0) * r0};
            float[] p01 = {a[0] + (ux * c1 + vx * s1) * r0, a[1] + (uy * c1 + vy * s1) * r0, a[2] + (uz * c1 + vz * s1) * r0};
            float[] p10 = {b[0] + (ux * c0 + vx * s0) * r1, b[1] + (uy * c0 + vy * s0) * r1, b[2] + (uz * c0 + vz * s0) * r1};
            float[] p11 = {b[0] + (ux * c1 + vx * s1) * r1, b[1] + (uy * c1 + vy * s1) * r1, b[2] + (uz * c1 + vz * s1) * r1};
            float uu0 = u0 + (u1 - u0) * i / sides, uu1 = u0 + (u1 - u0) * (i + 1) / sides;
            g.quad(tex, p10, p11, p01, p00, new float[]{uu0, v0, uu1, v0, uu1, v1, uu0, v1});
        }
    }

    /** Smooth tube through a polyline of points with radii. */
    static void chain(G g, String tex, float[][] pts, float[] radii, int sides) {
        for (int i = 0; i + 1 < pts.length; i++) {
            float v0 = i / (float) (pts.length - 1), v1 = (i + 1) / (float) (pts.length - 1);
            tube(g, tex, pts[i], pts[i + 1], radii[i], radii[i + 1], sides, 0, v0, 1, v1);
        }
    }

    /** Triangle (as a degenerate quad). */
    static void tri(G g, String tex, float[] a, float[] b, float[] c, float[] uva, float[] uvb, float[] uvc) {
        g.quad(tex, a, b, c, c, new float[]{uva[0], uva[1], uvb[0], uvb[1], uvc[0], uvc[1], uvc[0], uvc[1]});
    }

    /** Camera facing-ish sprite: two crossed planes at a point. */
    static void sprite(G g, String tex, float x, float y, float z, float s) {
        g.cross(tex, x, y, z, s, s, 0, 0, 1, 1);
    }

    /** Sprite made of three planes (looks round from every angle). */
    static void sprite3(G g, String tex, float x, float y, float z, float s) {
        g.push();
        g.translate(x, y, z);
        g.plane(tex, 0, 0, s, s, 0, 0, 1, 1);
        g.rotY(60);
        g.plane(tex, 0, 0, s, s, 0, 0, 1, 1);
        g.rotY(60);
        g.plane(tex, 0, 0, s, s, 0, 0, 1, 1);
        g.pop();
    }

    /** Ellipsoid (scaled sphere). */
    static void ellipsoid(G g, String tex, float x, float y, float z, float rx, float ry, float rz, int seg, int rings) {
        g.push();
        g.translate(x, y, z);
        g.scale(rx, ry, rz);
        g.sphere(tex, 1, seg, rings, 0, 0, 1, 1);
        g.pop();
    }

    /** Rounded box approximated with a box and slightly inset caps. */
    static void box(G g, String tex, float x0, float y0, float z0, float x1, float y1, float z1) {
        g.box(tex, x0, y0, z0, x1, y1, z1, 0, 0, 1, 1);
    }

    static int argb(int a, int rgb) { return (Math.max(0, Math.min(255, a)) << 24) | (rgb & 0xFFFFFF); }

    static int hue(double h, double s, double v) { return 0xFF000000 | Capes.hsv(h, s, v); }

    /** Squircle cross section point (superellipse exponent n; n = 2 circle). */
    static float[] sq(double a, float r, float n) {
        double c = Math.cos(a), s = Math.sin(a);
        double e = 2.0 / n;
        return new float[]{(float) (Math.signum(c) * Math.pow(Math.abs(c), e) * r), (float) (Math.signum(s) * Math.pow(Math.abs(s), e) * r)};
    }

    /** Vertical tube with a squircle cross section (covers the square head nicely). */
    static void sqTube(G g, String tex, float r0, float r1, float y0, float y1, float n, int seg, float u0, float v0, float u1, float v1) {
        for (int i = 0; i < seg; i++) {
            double a0 = Math.PI * 2 * i / seg, a1 = Math.PI * 2 * (i + 1) / seg;
            float[] b0 = sq(a0, r0, n), b1 = sq(a1, r0, n), t0 = sq(a0, r1, n), t1 = sq(a1, r1, n);
            float uu0 = u0 + (u1 - u0) * i / seg, uu1 = u0 + (u1 - u0) * (i + 1) / seg;
            g.quad(tex, new float[]{t0[0], y1, t0[1]}, new float[]{t1[0], y1, t1[1]}, new float[]{b1[0], y0, b1[1]}, new float[]{b0[0], y0, b0[1]},
                    new float[]{uu0, v0, uu1, v0, uu1, v1, uu0, v1});
        }
    }

    /** Dome from a squircle base (y0) rounding to a circle at the top (y0 + h). */
    static void sqDome(G g, String tex, float r, float h, float y0, float n, int seg, int rings, float u0, float v0, float u1, float v1) {
        for (int j = 0; j < rings; j++) {
            double p0 = Math.PI / 2 * j / rings, p1 = Math.PI / 2 * (j + 1) / rings;
            float ra = r * (float) Math.cos(p0), rb = r * (float) Math.cos(p1);
            float ya = y0 + h * (float) Math.sin(p0), yb = y0 + h * (float) Math.sin(p1);
            float na = lerp(n, 2, (float) j / rings), nb = lerp(n, 2, (float) (j + 1) / rings);
            float va = v1 - (v1 - v0) * j / rings, vb = v1 - (v1 - v0) * (j + 1) / rings;
            for (int i = 0; i < seg; i++) {
                double a0 = Math.PI * 2 * i / seg, a1 = Math.PI * 2 * (i + 1) / seg;
                float[] b0 = sq(a0, ra, na), b1 = sq(a1, ra, na), t0 = sq(a0, rb, nb), t1 = sq(a1, rb, nb);
                float uu0 = u0 + (u1 - u0) * i / seg, uu1 = u0 + (u1 - u0) * (i + 1) / seg;
                g.quad(tex, new float[]{t0[0], yb, t0[1]}, new float[]{t1[0], yb, t1[1]}, new float[]{b1[0], ya, b1[1]}, new float[]{b0[0], ya, b0[1]},
                        new float[]{uu0, vb, uu1, vb, uu1, va, uu0, va});
            }
        }
    }
}
