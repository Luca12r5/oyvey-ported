package dev.lego.cosmetic;

import java.util.List;

/**
 * Geometry helpers of the HD remaster of the classic cosmetics: rounded ("puffy") extrusions, outlines
 * (hearts, stars, circles, rounded rectangles), closed tubes along outlines and tapered horns.
 */
final class HdGeo {
    private HdGeo() { }

    static float sin(double v) { return (float) Math.sin(v); }
    static float cos(double v) { return (float) Math.cos(v); }

    private static float[] p(float x, float y, float z) { return new float[]{x, y, z}; }

    // ------------------------------------------------------------------ outlines (xy, counter clockwise)
    static float[][] circle(float rx, float ry, int n) {
        float[][] o = new float[n][];
        for (int i = 0; i < n; i++) { double a = Math.PI * 2 * i / n; o[i] = new float[]{cos(a) * rx, sin(a) * ry}; }
        return o;
    }

    /** Heart centered at the origin, width ~2*s, pointing down. */
    static float[][] heart(float s, int n) {
        float[][] o = new float[n][];
        for (int i = 0; i < n; i++) {
            double t = Math.PI * 2 * i / n;
            double x = 16 * Math.pow(Math.sin(t), 3);
            double y = 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);
            o[i] = new float[]{(float) (-x / 16 * s), (float) ((y + 2.5) / 16 * s)};
        }
        return ccw(o);
    }

    /** Star with pts points (outer radius r, inner radius ri), first point up. */
    static float[][] star(float r, float ri, int pts) {
        float[][] o = new float[pts * 2][];
        for (int i = 0; i < pts * 2; i++) {
            double a = Math.PI / 2 + Math.PI * i / pts;
            float rr = i % 2 == 0 ? r : ri;
            o[i] = new float[]{cos(a) * rr, sin(a) * rr};
        }
        return o;
    }

    /** Rounded rectangle (half sizes hw, hh, corner radius rc). */
    static float[][] rrect(float hw, float hh, float rc, int per) {
        float[][] o = new float[4 * (per + 1)][];
        float[][] cs = {{hw - rc, hh - rc}, {-hw + rc, hh - rc}, {-hw + rc, -hh + rc}, {hw - rc, -hh + rc}};
        int k = 0;
        for (int c = 0; c < 4; c++)
            for (int i = 0; i <= per; i++) {
                double a = Math.PI / 2 * c + Math.PI / 2 * i / per;
                o[k++] = new float[]{cs[c][0] + cos(a) * rc, cs[c][1] + sin(a) * rc};
            }
        return o;
    }

    static float[][] ccw(float[][] o) {
        double area = 0;
        for (int i = 0; i < o.length; i++) { float[] a = o[i], b = o[(i + 1) % o.length]; area += a[0] * b[1] - b[0] * a[1]; }
        if (area >= 0) return o;
        float[][] r = new float[o.length][];
        for (int i = 0; i < o.length; i++) r[i] = o[o.length - 1 - i];
        return r;
    }

    /** Outline offset outwards by d (miter, clamped). Polygon must be counter clockwise. */
    static float[][] offset(float[][] o, float d) {
        int n = o.length;
        float[][] r = new float[n][];
        for (int i = 0; i < n; i++) {
            float[] a = o[(i + n - 1) % n], b = o[i], c = o[(i + 1) % n];
            float e1x = b[0] - a[0], e1y = b[1] - a[1], e2x = c[0] - b[0], e2y = c[1] - b[1];
            float l1 = (float) Math.max(1e-6, Math.hypot(e1x, e1y)), l2 = (float) Math.max(1e-6, Math.hypot(e2x, e2y));
            float n1x = e1y / l1, n1y = -e1x / l1, n2x = e2y / l2, n2y = -e2x / l2;
            float mx = n1x + n2x, my = n1y + n2y;
            float ml = (float) Math.hypot(mx, my);
            if (ml < 1e-4f) { mx = n1x; my = n1y; ml = 1; }
            mx /= ml; my /= ml;
            float cosh = mx * n1x + my * n1y;
            float s = d / Math.max(0.45f, cosh);
            r[i] = new float[]{b[0] + mx * s, b[1] + my * s};
        }
        return r;
    }

    // ------------------------------------------------------------------ puffy extrusion
    /**
     * Rounded extrusion of an outline (xy, any orientation) between z = -depth/2 and +depth/2 with a quarter round
     * bevel of radius round (steps segments). Caps map the outline bounding box on capUV {u0,v0,u1,v1}
     * (front seen from +z non-mirrored), rim / sides map sideUV.
     */
    static void puff(G g, String t, float[][] outline, float depth, float round, int steps, float[] capUV, float[] sideUV) {
        float[][] o = ccw(outline);
        round = Math.min(round, depth / 2 * 0.98f);
        int n = o.length;
        float minx = 1e9f, miny = 1e9f, maxx = -1e9f, maxy = -1e9f;
        for (float[] q : o) { minx = Math.min(minx, q[0]); maxx = Math.max(maxx, q[0]); miny = Math.min(miny, q[1]); maxy = Math.max(maxy, q[1]); }
        float bw = Math.max(1e-4f, maxx - minx), bh = Math.max(1e-4f, maxy - miny);
        // rings: back cap edge -> round -> side -> round -> front cap edge
        int rings = steps * 2 + 2;
        float[][][] R = new float[rings][][];
        float[] Z = new float[rings];
        float hz = depth / 2;
        int k = 0;
        for (int s = 0; s <= steps; s++) {
            double a = Math.PI / 2 * s / steps;          // 0 = back cap, PI/2 = side
            R[k] = offset(o, -round * (1 - (float) Math.sin(a)));
            Z[k++] = -hz + round * (1 - (float) Math.cos(a));
        }
        for (int s = steps; s >= 0; s--) {
            double a = Math.PI / 2 * s / steps;
            R[k] = offset(o, -round * (1 - (float) Math.sin(a)));
            Z[k++] = hz - round * (1 - (float) Math.cos(a));
        }
        for (int j = 0; j + 1 < rings; j++) {
            float va = sideUV[1] + (sideUV[3] - sideUV[1]) * j / (rings - 1), vb = sideUV[1] + (sideUV[3] - sideUV[1]) * (j + 1) / (rings - 1);
            for (int i = 0; i < n; i++) {
                int i2 = (i + 1) % n;
                float ua = sideUV[0] + (sideUV[2] - sideUV[0]) * i / n, ub = sideUV[0] + (sideUV[2] - sideUV[0]) * (i + 1) / n;
                float[] A = R[j][i], B = R[j][i2], C = R[j + 1][i2], D = R[j + 1][i];
                g.quad(t, p(A[0], A[1], Z[j]), p(B[0], B[1], Z[j]), p(C[0], C[1], Z[j + 1]), p(D[0], D[1], Z[j + 1]), new float[]{ua, va, ub, va, ub, vb, ua, vb});
            }
        }
        // caps
        float[][] back = R[0], front = R[rings - 1];
        List<int[]> tris = Cos3Geo.triangulate(back);
        for (int[] tr : tris) {
            float[] uvf = new float[8], uvb = new float[8];
            for (int q = 0; q < 3; q++) {
                float[] f = front[tr[q]];
                uvf[q * 2] = capUV[0] + (capUV[2] - capUV[0]) * (maxx - f[0]) / bw;
                uvf[q * 2 + 1] = capUV[1] + (capUV[3] - capUV[1]) * (maxy - f[1]) / bh;
                float[] b = back[tr[q]];
                uvb[q * 2] = capUV[0] + (capUV[2] - capUV[0]) * (b[0] - minx) / bw;
                uvb[q * 2 + 1] = capUV[1] + (capUV[3] - capUV[1]) * (maxy - b[1]) / bh;
            }
            float[] a = front[tr[0]], b = front[tr[1]], c = front[tr[2]];
            g.quad(t, p(a[0], a[1], hz), p(b[0], b[1], hz), p(c[0], c[1], hz), p(c[0], c[1], hz), new float[]{uvf[0], uvf[1], uvf[2], uvf[3], uvf[4], uvf[5], uvf[4], uvf[5]});
            a = back[tr[0]]; b = back[tr[1]]; c = back[tr[2]];
            g.quad(t, p(c[0], c[1], -hz), p(b[0], b[1], -hz), p(a[0], a[1], -hz), p(a[0], a[1], -hz), new float[]{uvb[4], uvb[5], uvb[2], uvb[3], uvb[0], uvb[1], uvb[0], uvb[1]});
        }
    }

    static void puff(G g, String t, float[][] outline, float depth, float round, int steps) {
        puff(g, t, outline, depth, round, steps, Cos3Geo.FULL, new float[]{0.45f, 0.45f, 0.55f, 0.55f});
    }

    // ------------------------------------------------------------------ closed tube along an outline (frames, rims)
    /** Closed tube along an xy outline at z = 0 with a (rx: in plane, rz: depth) cross section, squircle exponent ex. */
    static void loop(G g, String t, float[][] outline, float rx, float rz, float ex, int sides) {
        float[][] o = ccw(outline);
        int n = o.length;
        float[][][] ring = new float[n][sides + 1][];
        for (int i = 0; i < n; i++) {
            float[] a = o[(i + n - 1) % n], c = o[(i + 1) % n];
            float tx = c[0] - a[0], ty = c[1] - a[1];
            float tl = (float) Math.max(1e-6, Math.hypot(tx, ty));
            float nx = ty / tl, ny = -tx / tl;              // outward normal in plane
            for (int s = 0; s <= sides; s++) {
                float[] q = Geo.sq(Math.PI * 2 * s / sides, 1, ex);
                ring[i][s] = p(o[i][0] + nx * q[0] * rx, o[i][1] + ny * q[0] * rx, q[1] * rz);
            }
        }
        for (int i = 0; i < n; i++) {
            int i2 = (i + 1) % n;
            float ua = (float) i / n, ub = (float) (i + 1) / n;
            for (int s = 0; s < sides; s++) {
                float va = (float) s / sides, vb = (float) (s + 1) / sides;
                g.quad(t, ring[i][s], ring[i2][s], ring[i2][s + 1], ring[i][s + 1], new float[]{ua, va, ub, va, ub, vb, ua, vb});
            }
        }
    }

    // ------------------------------------------------------------------ curved horn / ear shapes
    /**
     * Tapered, curling horn from the origin: grows along +y and curls around z by curl degrees in total,
     * base radius r0, length len, rings seg. Ridge rings are modelled as a slight radius wobble.
     */
    static void horn(G g, String t, float r0, float len, float curl, float twist, int seg, int sides, float ridges) {
        float[][] sp = new float[seg + 1][];
        float[] rr = new float[seg + 1];
        float x = 0, y = 0, z = 0;
        double ang = 0, tw = 0;
        float step = len / seg;
        for (int i = 0; i <= seg; i++) {
            float f = i / (float) seg;
            sp[i] = new float[]{x, y, z};
            float wob = ridges > 0 ? 1 + 0.07f * (float) Math.cos(f * Math.PI * 2 * ridges) : 1;
            rr[i] = Math.max(0.02f, r0 * (1 - f * 0.94f) * wob);
            ang += Math.toRadians(curl) / seg;
            tw += Math.toRadians(twist) / seg;
            x += (float) Math.sin(ang) * step;
            y += (float) Math.cos(ang) * step;
            z += (float) Math.sin(tw) * step * 0.35f;
        }
        XmasGeo.sweep(g, t, sp, rr, rr, null, sides, 1);
        Cos3Geo.ball(g, t, 0, 0, 0, r0 * 0.98f, sides);
    }

    /** Soft glowing sprite helper that keeps the current color/glow state. */
    static void glowBall(G g, float x, float y, float z, float s, int argb) { Cos3Geo.halo(g, x, y, z, s, argb); }
}
