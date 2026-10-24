package dev.lego.cosmetic;

/**
 * Geometry helpers for the vehicles. Every primitive here emits closed, outward wound quads
 * (counter clockwise seen from outside, so G's computed normal points outward) and never emits
 * two faces in the same plane on top of each other.
 */
final class VehGeo {
    private VehGeo() { }

    static final float[] UV = {0, 0, 1, 0, 1, 1, 0, 1};

    static float[] p(float x, float y, float z) { return new float[]{x, y, z}; }

    static int argb(int a, int rgb) { return (Math.max(0, Math.min(255, a)) << 24) | (rgb & 0xFFFFFF); }

    static float[] lerp(float[] a, float[] b, float t) { return p(lerp(a[0], b[0], t), lerp(a[1], b[1], t), lerp(a[2], b[2], t)); }

    /** Rotates the following geometry by 'deg' around an axis through 'pivot' that is tilted back by 'tilt' degrees. */
    static void steer(G g, float[] pivot, float tilt, float deg) {
        g.translate(pivot[0], pivot[1], pivot[2]);
        g.rotX(-tilt);
        g.rotY(deg);
        g.rotX(tilt);
        g.translate(-pivot[0], -pivot[1], -pivot[2]);
    }

    static float sin(double v) { return (float) Math.sin(v); }
    static float cos(double v) { return (float) Math.cos(v); }
    static float clamp(float v, float a, float b) { return v < a ? a : v > b ? b : v; }
    static float lerp(float a, float b, float t) { return a + (b - a) * t; }
    static float smooth(float e0, float e1, float x) { float t = clamp((x - e0) / (e1 - e0), 0, 1); return t * t * (3 - 2 * t); }

    private static boolean same(float[] a, float[] b) {
        return Math.abs(a[0] - b[0]) < 1e-5f && Math.abs(a[1] - b[1]) < 1e-5f && Math.abs(a[2] - b[2]) < 1e-5f;
    }

    private static float[] sub(float[] a, float[] b) { return new float[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]}; }
    private static float[] cross(float[] a, float[] b) { return new float[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]}; }
    private static float dot(float[] a, float[] b) { return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]; }
    private static float[] norm(float[] a) {
        float l = (float) Math.sqrt(dot(a, a));
        return l < 1e-9f ? new float[]{0, 1, 0} : new float[]{a[0] / l, a[1] / l, a[2] / l};
    }

    /** Quad a-b-c-d, counter clockwise seen from outside. Degenerate corners (triangles) are rotated so G gets a valid normal. */
    static void q(G g, String t, float[] a, float[] b, float[] c, float[] d, float[] uv) {
        float[][] v = {a, b, c, d};
        for (int k = 0; k < 4; k++) {
            float[] A = v[k], B = v[(k + 1) & 3], D = v[(k + 3) & 3];
            if (!same(A, B) && !same(A, D) && !same(B, D)) {
                float[] n = cross(sub(B, A), sub(D, A));
                if (dot(n, n) < 1e-12f) continue;
                int k1 = (k + 1) & 3, k2 = (k + 2) & 3, k3 = (k + 3) & 3;
                g.quad(t, v[k], v[k1], v[k2], v[k3], new float[]{uv[k * 2], uv[k * 2 + 1], uv[k1 * 2], uv[k1 * 2 + 1], uv[k2 * 2], uv[k2 * 2 + 1], uv[k3 * 2], uv[k3 * 2 + 1]});
                return;
            }
        }
    }

    static void q(G g, String t, float[] a, float[] b, float[] c, float[] d) { q(g, t, a, b, c, d, UV); }

    /** Quad oriented automatically so its normal points away from {@code inside}. */
    static void qo(G g, String t, float[] a, float[] b, float[] c, float[] d, float[] inside, float[] uv) {
        float[] n = cross(sub(c, a), sub(d, b));
        float[] ctr = {(a[0] + b[0] + c[0] + d[0]) / 4, (a[1] + b[1] + c[1] + d[1]) / 4, (a[2] + b[2] + c[2] + d[2]) / 4};
        if (dot(n, sub(ctr, inside)) >= 0) q(g, t, a, b, c, d, uv);
        else q(g, t, a, d, c, b, new float[]{uv[0], uv[1], uv[6], uv[7], uv[4], uv[5], uv[2], uv[3]});
    }

    static void tri(G g, String t, float[] a, float[] b, float[] c) { q(g, t, a, b, c, c, new float[]{0, 0, 1, 0, 1, 1, 1, 1}); }

    // ------------------------------------------------------------------ boxes
    /** Axis aligned box, outward. */
    static void box(G g, String t, float x0, float y0, float z0, float x1, float y1, float z1) {
        float[] c = {(x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2};
        float[][] P = new float[8][];
        for (int i = 0; i < 8; i++) P[i] = p((i & 1) == 0 ? x0 : x1, (i & 2) == 0 ? y0 : y1, (i & 4) == 0 ? z0 : z1);
        int[][] F = {{0, 1, 3, 2}, {4, 5, 7, 6}, {0, 2, 6, 4}, {1, 3, 7, 5}, {0, 1, 5, 4}, {2, 3, 7, 6}};
        for (int[] f : F) qo(g, t, P[f[0]], P[f[1]], P[f[2]], P[f[3]], c, UV);
    }

    /** Box with per-face textures: {front(+z), back(-z), left(-x), right(+x), top(+y), bottom(-y)} (null = default). */
    static void box6(G g, String t, String[] faces, float x0, float y0, float z0, float x1, float y1, float z1) {
        float[] c = {(x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2};
        float[][] P = new float[8][];
        for (int i = 0; i < 8; i++) P[i] = p((i & 1) == 0 ? x0 : x1, (i & 2) == 0 ? y0 : y1, (i & 4) == 0 ? z0 : z1);
        // front / back: u along x (+x on the right side of the image seen from outside), v top->bottom
        int[][] F = {{4, 5, 7, 6}, {1, 0, 2, 3}, {0, 4, 6, 2}, {5, 1, 3, 7}, {6, 7, 3, 2}, {0, 1, 5, 4}};
        for (int i = 0; i < 6; i++) {
            String tx = faces != null && faces[i] != null ? faces[i] : t;
            int[] f = F[i];
            // corners listed as bottom-left, bottom-right, top-right, top-left (seen from outside) -> CCW
            // cosmetic space is left handed (x = the model's right): flip u so images read correctly from outside
            // top: image top towards +z, readable for someone standing behind it
            q(g, tx, P[f[0]], P[f[1]], P[f[2]], P[f[3]], i == 4 ? new float[]{0, 0, 1, 0, 1, 1, 0, 1} : new float[]{1, 1, 0, 1, 0, 0, 1, 0});
        }
    }

    /** Chamfered box (bevel b), outward. */
    static void rbox(G g, String t, float x0, float y0, float z0, float x1, float y1, float z1, float b) {
        b = Math.min(b, Math.min(x1 - x0, Math.min(y1 - y0, z1 - z0)) * 0.49f);
        float[] lo = {x0 + b, y0 + b, z0 + b}, hi = {x1 - b, y1 - b, z1 - b};
        float[] c = {(x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2};
        int[][] sq = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
        float[] mid = {0.3f, 0.3f, 0.7f, 0.3f, 0.7f, 0.7f, 0.3f, 0.7f};
        for (int ax = 0; ax < 3; ax++)
            for (int s = -1; s <= 1; s += 2) {
                int u = (ax + 1) % 3, v = (ax + 2) % 3;
                float[][] pts = new float[4][];
                for (int k = 0; k < 4; k++) {
                    float[] pp = new float[3];
                    pp[ax] = (s < 0 ? lo[ax] : hi[ax]) + s * b;
                    pp[u] = sq[k][0] < 0 ? lo[u] : hi[u];
                    pp[v] = sq[k][1] < 0 ? lo[v] : hi[v];
                    pts[k] = pp;
                }
                qo(g, t, pts[0], pts[1], pts[2], pts[3], c, UV);
            }
        if (b <= 1e-4f) return;
        for (int a = 0; a < 3; a++)
            for (int cc = a + 1; cc < 3; cc++) {
                int tt = 3 - a - cc;
                for (int sa = -1; sa <= 1; sa += 2)
                    for (int sc = -1; sc <= 1; sc += 2) {
                        float[][] pts = new float[4][];
                        int[] ts = {-1, 1, 1, -1};
                        for (int k = 0; k < 4; k++) {
                            float[] pp = new float[3];
                            pp[a] = sa < 0 ? lo[a] : hi[a];
                            pp[cc] = sc < 0 ? lo[cc] : hi[cc];
                            pp[tt] = ts[k] < 0 ? lo[tt] : hi[tt];
                            if (k < 2) pp[a] += sa * b; else pp[cc] += sc * b;
                            pts[k] = pp;
                        }
                        qo(g, t, pts[0], pts[1], pts[2], pts[3], c, mid);
                    }
            }
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sy = -1; sy <= 1; sy += 2)
                for (int sz = -1; sz <= 1; sz += 2) {
                    float px = sx < 0 ? lo[0] : hi[0], py = sy < 0 ? lo[1] : hi[1], pz = sz < 0 ? lo[2] : hi[2];
                    float[] A = p(px + sx * b, py, pz), B = p(px, py + sy * b, pz), C = p(px, py, pz + sz * b);
                    qo(g, t, A, B, C, C, c, mid);
                }
    }

    /** Tapered rectangular beam from a to b (cross-section w x h at a, w1 x h1 at b); 'up' orients the h side. */
    static void beam(G g, String t, float[] a, float[] b, float w0, float h0, float w1, float h1, float[] up) {
        float[] d = norm(sub(b, a));
        float[] s = cross(d, up);
        if (dot(s, s) < 1e-8f) s = cross(d, new float[]{1, 0, 0});
        if (dot(s, s) < 1e-8f) s = cross(d, new float[]{0, 0, 1});
        s = norm(s);
        float[] u = norm(cross(s, d));
        float[][] P = new float[8][];
        for (int i = 0; i < 8; i++) {
            float[] base = (i & 4) == 0 ? a : b;
            float w = ((i & 4) == 0 ? w0 : w1) / 2, h = ((i & 4) == 0 ? h0 : h1) / 2;
            float sw = (i & 1) == 0 ? -w : w, sh = (i & 2) == 0 ? -h : h;
            P[i] = p(base[0] + s[0] * sw + u[0] * sh, base[1] + s[1] * sw + u[1] * sh, base[2] + s[2] * sw + u[2] * sh);
        }
        float[] c = {(a[0] + b[0]) / 2, (a[1] + b[1]) / 2, (a[2] + b[2]) / 2};
        int[][] F = {{0, 1, 3, 2}, {4, 5, 7, 6}, {0, 2, 6, 4}, {1, 3, 7, 5}, {0, 1, 5, 4}, {2, 3, 7, 6}};
        for (int[] f : F) qo(g, t, P[f[0]], P[f[1]], P[f[2]], P[f[3]], c, UV);
    }

    static final float[] UP = {0, 1, 0};

    static void beam(G g, String t, float[] a, float[] b, float w, float h) { beam(g, t, a, b, w, h, w, h, UP); }

    // ------------------------------------------------------------------ tubes
    /** Closed tube from a to b (radius r0 -> r1), optional end caps. */
    static void tube(G g, String t, float[] a, float[] b, float r0, float r1, int n, boolean caps) {
        float[] d = norm(sub(b, a));
        float[] ref = Math.abs(d[1]) < 0.9f ? new float[]{0, 1, 0} : new float[]{1, 0, 0};
        float[] u = norm(cross(d, ref)), v = cross(d, u);
        float[][] A = new float[n][], B = new float[n][];
        for (int i = 0; i < n; i++) {
            double an = Math.PI * 2 * i / n;
            float c = (float) Math.cos(an), s = (float) Math.sin(an);
            float ox = u[0] * c + v[0] * s, oy = u[1] * c + v[1] * s, oz = u[2] * c + v[2] * s;
            A[i] = p(a[0] + ox * r0, a[1] + oy * r0, a[2] + oz * r0);
            B[i] = p(b[0] + ox * r1, b[1] + oy * r1, b[2] + oz * r1);
        }
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            float[] axisPt = {(a[0] + b[0]) / 2, (a[1] + b[1]) / 2, (a[2] + b[2]) / 2};
            // use the local axis point for the orientation test
            float[] mid = {(A[i][0] + A[j][0] + B[i][0] + B[j][0]) / 4, (A[i][1] + A[j][1] + B[i][1] + B[j][1]) / 4, (A[i][2] + A[j][2] + B[i][2] + B[j][2]) / 4};
            float tt = dot(sub(mid, a), d);
            float[] ap = {a[0] + d[0] * tt, a[1] + d[1] * tt, a[2] + d[2] * tt};
            qo(g, t, A[i], A[j], B[j], B[i], ap, new float[]{(float) i / n, 0, (float) (i + 1) / n, 0, (float) (i + 1) / n, 1, (float) i / n, 1});
        }
        if (caps) {
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                float[] inA = {a[0] + d[0], a[1] + d[1], a[2] + d[2]}, inB = {b[0] - d[0], b[1] - d[1], b[2] - d[2]};
                if (r0 > 1e-3f) qo(g, t, a, A[i], A[j], A[j], inA, UV);
                if (r1 > 1e-3f) qo(g, t, b, B[i], B[j], B[j], inB, UV);
            }
        }
    }

    /** Round pipe through a polyline (rings are shared at the joints, one smooth closed pipe). */
    static void pipe(G g, String t, float[][] pts, float r, int n, boolean caps) {
        float[][] sec = new float[n][];
        for (int i = 0; i < n; i++) sec[i] = new float[]{cos(Math.PI * 2 * i / n) * r, sin(Math.PI * 2 * i / n) * r};
        sweep(g, t, pts, sec, null, caps);
    }

    /** Rectangular strip (w along 'side' reference, h across) through a polyline. */
    static void strip(G g, String t, float[][] pts, float w, float h, float[] ref, boolean caps) {
        float[][] sec = {{-w / 2, -h / 2}, {w / 2, -h / 2}, {w / 2, h / 2}, {-w / 2, h / 2}};
        sweep(g, t, pts, sec, ref, caps);
    }

    /**
     * Sweeps a convex cross-section (s, u offsets) along a polyline. The s axis is perpendicular to the path and to
     * 'ref' (default: the most perpendicular world axis).
     */
    static void sweep(G g, String t, float[][] pts, float[][] sec, float[] ref, boolean caps) {
        int m = pts.length, n = sec.length;
        float[][][] R = new float[m][n][];
        float[][] ctr = new float[m][];
        float[] prevSd = null;
        for (int k = 0; k < m; k++) {
            float[] a = pts[Math.max(0, k - 1)], b = pts[Math.min(m - 1, k + 1)];
            float[] d = norm(sub(b, a));
            float[] sd;
            if (ref == null && prevSd != null) {
                // parallel transport: keep the previous side vector, made perpendicular to the new direction
                float k2 = dot(prevSd, d);
                sd = p(prevSd[0] - d[0] * k2, prevSd[1] - d[1] * k2, prevSd[2] - d[2] * k2);
                if (dot(sd, sd) < 1e-6f) sd = cross(d, new float[]{0, 0, 1});
            } else {
                float[] rf = ref;
                if (rf == null) rf = Math.abs(d[1]) < 0.8f ? new float[]{0, 1, 0} : new float[]{1, 0, 0};
                sd = cross(d, rf);
                if (dot(sd, sd) < 1e-6f) sd = cross(d, new float[]{0, 0, 1});
            }
            sd = norm(sd);
            prevSd = sd;
            float[] up = norm(cross(sd, d));
            // miter: widen the ring at a bend so the pipe keeps its thickness
            float sc = 1;
            if (k > 0 && k < m - 1) {
                float[] d0 = norm(sub(pts[k], pts[k - 1])), d1 = norm(sub(pts[k + 1], pts[k]));
                float c = dot(d0, d1);
                sc = 1f / (float) Math.max(0.5, Math.sqrt((1 + c) / 2));
            }
            ctr[k] = pts[k];
            for (int i = 0; i < n; i++) {
                float s = sec[i][0], u = sec[i][1] * 1;
                // scale only the component along the bend (approximation: both)
                R[k][i] = p(pts[k][0] + (sd[0] * s + up[0] * u) * sc, pts[k][1] + (sd[1] * s + up[1] * u) * sc, pts[k][2] + (sd[2] * s + up[2] * u) * sc);
            }
        }
        for (int k = 0; k + 1 < m; k++) {
            float[] hint = {(ctr[k][0] + ctr[k + 1][0]) / 2, (ctr[k][1] + ctr[k + 1][1]) / 2, (ctr[k][2] + ctr[k + 1][2]) / 2};
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                qo(g, t, R[k][i], R[k][j], R[k + 1][j], R[k + 1][i], hint,
                        new float[]{(float) i / n, (float) k / (m - 1), (float) (i + 1) / n, (float) k / (m - 1), (float) (i + 1) / n, (float) (k + 1) / (m - 1), (float) i / n, (float) (k + 1) / (m - 1)});
            }
        }
        if (caps) {
            float[] in0 = pts[1], in1 = pts[m - 2];
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                qo(g, t, pts[0], R[0][i], R[0][j], R[0][j], in0, UV);
                qo(g, t, pts[m - 1], R[m - 1][i], R[m - 1][j], R[m - 1][j], in1, UV);
            }
        }
    }

    /** Points on an arc in the y/z plane around (y, z) = (cy, cz), angles in degrees (0 = +z forward, 90 = up). */
    static float[][] arc(float x, float cy, float cz, float r, float a0, float a1, int n) {
        float[][] o = new float[n + 1][];
        for (int i = 0; i <= n; i++) {
            double a = Math.toRadians(a0 + (a1 - a0) * i / n);
            o[i] = p(x, cy + (float) Math.sin(a) * r, cz + (float) Math.cos(a) * r);
        }
        return o;
    }

    /** Torus around the local y axis (outward wound). */
    static void torus(G g, String t, float R, float r, int seg, int side) {
        float[][] prof = new float[side + 1][];
        for (int i = 0; i <= side; i++) {
            double a = -Math.PI / 2 + Math.PI * 2 * i / side;
            prof[i] = new float[]{R + (float) Math.cos(a) * r, (float) Math.sin(a) * r};
        }
        lathe(g, t, prof, seg);
    }

    /** Ellipsoid (G's sphere is outward wound). */
    static void ball(G g, String t, float x, float y, float z, float r, int seg, int rings) {
        g.push();
        g.translate(x, y, z);
        g.sphere(t, r, seg, rings, 0, 0, 1, 1);
        g.pop();
    }

    static void ellipsoid(G g, String t, float x, float y, float z, float rx, float ry, float rz, int seg, int rings) {
        g.push();
        g.translate(x, y, z);
        g.scale(rx, ry, rz);
        g.sphere(t, 1, seg, rings, 0, 0, 1, 1);
        g.pop();
    }

    // ------------------------------------------------------------------ lathe / loft
    /**
     * Surface of revolution around the local y axis. prof = {r, y} points; the outside of the surface is on the
     * right hand side when walking along the profile (r to the right, y up).
     * tex[i] = texture of segment i (or one texture if tex.length == 1).
     */
    static void lathe(G g, String[] tex, float[][] prof, int seg) {
        int n = prof.length;
        float total = 0;
        float[] acc = new float[n];
        for (int i = 1; i < n; i++) {
            float dr = prof[i][0] - prof[i - 1][0], dy = prof[i][1] - prof[i - 1][1];
            total += (float) Math.sqrt(dr * dr + dy * dy);
            acc[i] = total;
        }
        for (int i = 0; i + 1 < n; i++) {
            String t = tex[Math.min(i, tex.length - 1)];
            float v0 = total > 0 ? acc[i] / total : 0, v1 = total > 0 ? acc[i + 1] / total : 1;
            for (int j = 0; j < seg; j++) {
                double a0 = Math.PI * 2 * j / seg, a1 = Math.PI * 2 * (j + 1) / seg;
                float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0), c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
                float r0 = prof[i][0], y0 = prof[i][1], r1 = prof[i + 1][0], y1 = prof[i + 1][1];
                float u0 = (float) j / seg, u1 = (float) (j + 1) / seg;
                q(g, t, p(c0 * r1, y1, s0 * r1), p(c1 * r1, y1, s1 * r1), p(c1 * r0, y0, s1 * r0), p(c0 * r0, y0, s0 * r0),
                        new float[]{u0, v1, u1, v1, u1, v0, u0, v0});
            }
        }
    }

    static void lathe(G g, String t, float[][] prof, int seg) { lathe(g, new String[]{t}, prof, seg); }

    /** Cross section generator for {@link #loft}: ring of (x, y) points, counter clockwise seen from +z. */
    interface Sec { float[][] at(float z); }

    /** Texture chooser: segment j of the ring at section k (between zs[k] and zs[k+1]). */
    interface TexFn { String at(int k, int j, float z, float[] a, float[] b); }

    /**
     * Lofted closed surface through cross sections at increasing z. uv: u along z (z0..z1), v = (vTop - y) / (vTop - vBot).
     * Caps are triangle fans around the section centroid (use convex end sections).
     */
    static void loft(G g, float[] zs, Sec sec, TexFn tex, boolean capStart, boolean capEnd, float vTop, float vBot) {
        loft(g, zs, sec, tex, capStart, capEnd, vTop, vBot, false);
    }

    /** vByX: v follows x (v = (vTop - x) / (vTop - vBot)) instead of the height. */
    static void loft(G g, float[] zs, Sec sec, TexFn tex, boolean capStart, boolean capEnd, float vTop, float vBot, boolean vByX) {
        loft(g, zs, sec, tex, capStart, capEnd, vTop, vBot, vByX, false);
    }

    /** pairedCaps: close the ends by joining point i with point n-1-i (for thin crescent shaped sections). */
    static void loft(G g, float[] zs, Sec sec, TexFn tex, boolean capStart, boolean capEnd, float vTop, float vBot, boolean vByX, boolean pairedCaps) {
        int vc = vByX ? 0 : 1;
        float[][][] S = new float[zs.length][][];
        for (int k = 0; k < zs.length; k++) S[k] = sec.at(zs[k]);
        float zl = zs[0], zr = zs[zs.length - 1];
        for (int k = 0; k + 1 < zs.length; k++) {
            float[][] a = S[k], b = S[k + 1];
            int n = a.length;
            for (int j = 0; j < n; j++) {
                int j1 = (j + 1) % n;
                String t = tex.at(k, j, zs[k], a[j], a[j1]);
                if (t == null) continue;
                float[] A = p(a[j][0], a[j][1], zs[k]), B = p(a[j1][0], a[j1][1], zs[k]), C = p(b[j1][0], b[j1][1], zs[k + 1]), D = p(b[j][0], b[j][1], zs[k + 1]);
                q(g, t, A, B, C, D, new float[]{uvU(zs[k], zl, zr), uvV(A[vc], vTop, vBot), uvU(zs[k], zl, zr), uvV(B[vc], vTop, vBot),
                        uvU(zs[k + 1], zl, zr), uvV(C[vc], vTop, vBot), uvU(zs[k + 1], zl, zr), uvV(D[vc], vTop, vBot)});
            }
        }
        if (pairedCaps) {
            if (capStart) pairCap(g, S[0], zs[0], tex.at(-1, 0, zs[0], S[0][0], S[0][0]), 1);
            if (capEnd) pairCap(g, S[zs.length - 1], zs[zs.length - 1], tex.at(zs.length - 1, 0, zs[zs.length - 1], S[0][0], S[0][0]), -1);
            return;
        }
        if (capStart) cap(g, S[0], zs[0], tex.at(-1, 0, zs[0], S[0][0], S[0][0]), false, vTop, vBot);
        if (capEnd) cap(g, S[zs.length - 1], zs[zs.length - 1], tex.at(zs.length - 1, 0, zs[zs.length - 1], S[0][0], S[0][0]), true, vTop, vBot);
    }

    private static void pairCap(G g, float[][] s, float z, String t, int inward) {
        if (t == null) return;
        int n = s.length;
        for (int i = 0; i < n / 2 - 1; i++) {
            float[] a = p(s[i][0], s[i][1], z), b = p(s[i + 1][0], s[i + 1][1], z), c = p(s[n - 2 - i][0], s[n - 2 - i][1], z), d = p(s[n - 1 - i][0], s[n - 1 - i][1], z);
            float[] in = {(a[0] + b[0] + c[0] + d[0]) / 4, (a[1] + b[1] + c[1] + d[1]) / 4, z + inward};
            qo(g, t, a, b, c, d, in, UV);
        }
    }

    private static float uvU(float z, float z0, float z1) { return z1 == z0 ? 0 : (z - z0) / (z1 - z0); }
    private static float uvV(float y, float t, float b) { return clamp((t - y) / (t - b), 0, 1); }

    private static void cap(G g, float[][] s, float z, String t, boolean front, float vTop, float vBot) {
        if (t == null) return;
        float cx = 0, cy = 0;
        for (float[] pt : s) { cx += pt[0]; cy += pt[1]; }
        cx /= s.length; cy /= s.length;
        float[] c = p(cx, cy, z);
        for (int j = 0; j < s.length; j++) {
            float[] a = p(s[j][0], s[j][1], z), b = p(s[(j + 1) % s.length][0], s[(j + 1) % s.length][1], z);
            float[] uv = {0.5f, uvV(cy, vTop, vBot), 0.5f, uvV(a[1], vTop, vBot), 0.5f, uvV(b[1], vTop, vBot), 0.5f, uvV(b[1], vTop, vBot)};
            if (front) q(g, t, c, a, b, b, uv);
            else q(g, t, c, b, a, a, new float[]{uv[0], uv[1], uv[4], uv[5], uv[2], uv[3], uv[2], uv[3]});
        }
    }

    /** Mirror a right half profile (bottom center -> top center, x >= 0) into a full CCW ring. */
    static float[][] mirrorRing(float[][] half) {
        int n = half.length;
        // right half goes bottom -> top (CCW on the +x side); the left half comes back top -> bottom
        float[][] r = new float[n * 2 - 2][];
        for (int i = 0; i < n; i++) r[i] = new float[]{half[i][0], half[i][1]};
        for (int i = n - 2; i >= 1; i--) r[n + (n - 2 - i)] = new float[]{-half[i][0], half[i][1]};
        return r;
    }

    /** Smooth interpolation through keyframes (Catmull-Rom, clamped). */
    static float curve(float x, float[] xs, float[] vs) {
        int n = xs.length;
        if (x <= xs[0]) return vs[0];
        if (x >= xs[n - 1]) return vs[n - 1];
        int i = 0;
        while (i < n - 2 && x > xs[i + 1]) i++;
        float t = (x - xs[i]) / (xs[i + 1] - xs[i]);
        float p0 = vs[Math.max(0, i - 1)], p1 = vs[i], p2 = vs[i + 1], p3 = vs[Math.min(n - 1, i + 2)];
        float m1 = (p2 - p0) * 0.5f * (xs[i + 1] - xs[i]) / Math.max(1e-4f, xs[Math.min(n - 1, i + 1)] - xs[Math.max(0, i - 1)]) * 2;
        float m2 = (p3 - p1) * 0.5f * (xs[i + 1] - xs[i]) / Math.max(1e-4f, xs[Math.min(n - 1, i + 2)] - xs[i]) * 2;
        float t2 = t * t, t3 = t2 * t;
        return (2 * t3 - 3 * t2 + 1) * p1 + (t3 - 2 * t2 + t) * m1 + (-2 * t3 + 3 * t2) * p2 + (t3 - t2) * m2;
    }

    /** Evenly spaced floats a..b (inclusive) with at most 'step' spacing, merged with extra breakpoints. */
    static float[] range(float a, float b, float step, float... extra) {
        java.util.TreeSet<Float> s = new java.util.TreeSet<>();
        int n = Math.max(1, (int) Math.ceil((b - a) / step));
        for (int i = 0; i <= n; i++) s.add(a + (b - a) * i / n);
        for (float e : extra) if (e > a && e < b) s.add(e);
        // drop values closer than 0.05 to each other
        float[] out = new float[s.size()];
        int k = 0;
        float last = Float.NEGATIVE_INFINITY;
        for (float f : s) { if (f - last > 0.05f || k == 0) { out[k++] = f; last = f; } }
        return java.util.Arrays.copyOf(out, k);
    }

    // ------------------------------------------------------------------ wheels
    /**
     * Wheel in local space: axle along x, center at the origin, outer face towards +x.
     * spokes: 0 = solid disc face (hub motor / kart), >0 = spokes. Spins by 'spin' degrees (forward rolling).
     */
    static void wheel(G g, float R, float w, float rimR, int spokes, float spin, String tyre, String side, String rim, String disc, int seg) {
        g.push();
        g.rotX(spin);
        g.rotZ(-90);                      // lathe axis y -> +x
        float h = w / 2;
        float sh = Math.min(0.9f, (R - rimR) * 0.45f);
        // tyre: inner sidewall -> tread -> outer sidewall
        lathe(g, new String[]{side, tyre, tyre, tyre, side}, new float[][]{
                {rimR + 0.05f, -h + 0.2f}, {R - sh, -h}, {R, -h + sh * 0.7f},
                {R, h - sh * 0.7f}, {R - sh, h}, {rimR + 0.05f, h - 0.2f}}, seg);
        // rim lip (outer) + barrel (visible from the face side, faces the axle)
        float lipY = h - 0.12f;
        lathe(g, rim, new float[][]{{rimR + 0.05f, h - 0.2f}, {rimR - 0.1f, lipY + 0.02f}, {rimR - 0.35f, h - 0.45f}}, seg);
        lathe(g, rim, new float[][]{{rimR - 0.35f, h - 0.45f}, {rimR - 0.35f, -h + 0.3f}}, seg);
        // back wall (dark): front side faces the spokes, the back side closes the wheel towards the inside
        lathe(g, side, new float[][]{{rimR - 0.35f, -h + 0.45f}, {0.01f, -h + 0.45f}}, seg);
        lathe(g, side, new float[][]{{0.01f, -h + 0.3f}, {rimR - 0.35f, -h + 0.3f}, {rimR + 0.05f, -h + 0.2f}}, seg);
        if (disc != null) {
            float dr = rimR - 0.75f, dy = ((-h + 0.45f) + (h - 0.35f)) / 2, dt = Math.min(0.25f, (h - 0.35f - (-h + 0.45f)) / 2 - 0.1f);
            lathe(g, disc, new float[][]{{0.01f, dy - dt}, {dr, dy - dt}, {dr, dy + dt}, {0.01f, dy + dt}}, Math.max(8, seg * 2 / 3));
        }
        float fy = h - 0.35f;              // spoke face plane
        if (spokes > 0) {
            for (int i = 0; i < spokes; i++) {
                double an = Math.PI * 2 * i / spokes;
                float c = (float) Math.cos(an), s = (float) Math.sin(an);
                float r0 = Math.max(0.7f, rimR * 0.22f), r1 = rimR - 0.3f;
                float[] a = p(c * r0, fy + 0.1f, s * r0), b = p(c * r1, fy - 0.25f, s * r1);
                beam(g, rim, a, b, Math.max(0.35f, rimR * 0.2f), 0.35f, Math.max(0.25f, rimR * 0.1f), 0.3f, p(0, 1, 0));
            }
            // hub + centre cap
            float hr = Math.max(0.8f, rimR * 0.3f);
            lathe(g, new String[]{rim, rim, "veh_flat:2A2C33"}, new float[][]{{hr, fy - 0.4f}, {hr, fy + 0.15f}, {hr * 0.55f, fy + 0.35f}, {0.01f, fy + 0.4f}}, Math.max(8, seg / 2));
        } else {
            // solid face (dished)
            lathe(g, new String[]{rim, rim, "veh_flat:2A2C33"}, new float[][]{{rimR - 0.35f, h - 0.45f}, {rimR * 0.55f, fy}, {rimR * 0.3f, fy + 0.1f}, {0.01f, fy + 0.3f}}, seg);
        }
        g.pop();
    }
}
