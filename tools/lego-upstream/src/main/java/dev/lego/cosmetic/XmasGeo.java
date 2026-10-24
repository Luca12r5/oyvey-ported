package dev.lego.cosmetic;

import static dev.lego.cosmetic.Cos3Geo.*;

/** Shared geometry of the Christmas collection: swept tubes, light bulbs, bells, holly, bows, 3D stars and snowflakes. */
final class XmasGeo {
    private XmasGeo() { }

    /** Christmas light colours. */
    static final int[] LIGHTS = {0xFFFF3A3A, 0xFFFFD23A, 0xFF3AE86A, 0xFF3AA8FF, 0xFFFF7AE0, 0xFFFF9A2A};

    private static float[] p(float x, float y, float z) { return new float[]{x, y, z}; }

    /**
     * Tube swept along a spine with an elliptic / squircle cross section per ring (rx along the frame normal = x-ish,
     * rz along the binormal), exponent n (2 = ellipse). Closed at the end with a point if the last radius is 0.
     * uv: u around (0..1), v along the spine (0..vRep).
     */
    static void sweep(G g, String tex, float[][] spine, float[] rx, float[] rz, float[] n, int seg, float vRep) {
        int m = spine.length;
        float[][][] rings = new float[m][seg + 1][];
        float[] ref = {1, 0, 0};
        for (int j = 0; j < m; j++) {
            float[] a = spine[Math.max(0, j - 1)], b = spine[Math.min(m - 1, j + 1)];
            float tx = b[0] - a[0], ty = b[1] - a[1], tz = b[2] - a[2];
            float tl = (float) Math.sqrt(tx * tx + ty * ty + tz * tz);
            if (tl < 1e-6f) { tx = 0; ty = 1; tz = 0; tl = 1; }
            tx /= tl; ty /= tl; tz /= tl;
            float d = ref[0] * tx + ref[1] * ty + ref[2] * tz;
            float nx = ref[0] - d * tx, ny = ref[1] - d * ty, nz = ref[2] - d * tz;
            float nl = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (nl < 1e-4f) { nx = 0; ny = 0; nz = 1; nl = 1; }
            nx /= nl; ny /= nl; nz /= nl;
            ref = new float[]{nx, ny, nz};
            float bx = ty * nz - tz * ny, by = tz * nx - tx * nz, bz = tx * ny - ty * nx;
            for (int i = 0; i <= seg; i++) {
                float[] q = Geo.sq(Math.PI * 2 * i / seg, 1, n == null ? 2 : n[j]);
                float cx = q[0] * rx[j], cz = q[1] * rz[j];
                rings[j][i] = p(spine[j][0] + nx * cx + bx * cz, spine[j][1] + ny * cx + by * cz, spine[j][2] + nz * cx + bz * cz);
            }
        }
        for (int j = 0; j + 1 < m; j++) {
            float va = vRep * j / (m - 1), vb = vRep * (j + 1) / (m - 1);
            for (int i = 0; i < seg; i++) {
                float ua = (float) i / seg, ub = (float) (i + 1) / seg;
                g.quad(tex, rings[j + 1][i], rings[j + 1][i + 1], rings[j][i + 1], rings[j][i], new float[]{ua, vb, ub, vb, ub, va, ua, va});
            }
        }
    }

    /** Glowing christmas bulb pointing along +y from the origin (socket + glass), with a soft halo when lit. */
    static void bulb(G g, float s, int argb, float lit) {
        boolean og = g.glow;
        int oc = g.color;
        g.glow(false).color(0xFF2E5A34);
        g.cylinder("c3_metal", 0.26f * s, 0.3f * s, 0, 0.42f * s, 6, false, 0, 0, 1, 1);
        int c = lit > 0.5f ? argb : 0xFF000000 | Capes.mix(argb & 0xFFFFFF, 0x202020, 0.55 - lit * 0.5);
        g.glow(lit > 0.2f).color(0xFF000000 | Capes.mix(c & 0xFFFFFF, 0xFFFFFF, 0.15 * lit));
        Geo.ellipsoid(g, "xm_bulb", 0, 0.95f * s, 0, 0.38f * s, 0.62f * s, 0.38f * s, 8, 5);
        if (lit > 0.2f) halo(g, 0, 0.95f * s, 0, 2.3f * s, alpha(0.45 * lit, argb));
        g.glow(og).color(oc);
    }

    /** Cheaper bulb for long light strings (open socket, lower poly glass). */
    static void bulbLite(G g, float s, int argb, float lit) {
        boolean og = g.glow;
        int oc = g.color;
        g.glow(false).color(0xFF2E5A34);
        g.cylinder("c3_metal", 0.26f * s, 0.3f * s, 0, 0.42f * s, 6, false, 0, 0, 1, 1);
        int c = 0xFF000000 | Capes.mix(0x282828, argb & 0xFFFFFF, 0.3 + 0.7 * lit);
        g.glow(lit > 0.3f).color(c);
        Geo.ellipsoid(g, "xm_bulb", 0, 0.95f * s, 0, 0.38f * s, 0.62f * s, 0.38f * s, 6, 4);
        if (lit > 0.3f) halo(g, 0, 0.95f * s, 0, 2.4f * s, alpha(0.5 * lit, argb));
        g.glow(og).color(oc);
    }

    /** Small round fairy light (glowing ball + halo). */
    static void fairy(G g, float x, float y, float z, float r, int argb, float lit) {
        boolean og = g.glow;
        int oc = g.color;
        int c = 0xFF000000 | Capes.mix(0x404040, argb & 0xFFFFFF, 0.55 + 0.45 * lit);
        g.glow(true).color(c);
        ball(g, "xm_bulb", x, y, z, r, 6);
        if (lit > 0.3f) halo(g, x, y, z, r * 5.5f, alpha(0.5 * lit, argb));
        g.glow(og).color(oc);
    }

    /** Twinkle 0..1 for light i at time t (smooth, varied per light). */
    static float twinkle(float t, int i) {
        double h = Capes.hash1(i, 91);
        float v = 0.5f + 0.5f * sin(t * (2.2 + h * 2.5) + h * 30);
        return 0.25f + 0.75f * smooth(v * 1.4f - 0.2f);
    }

    /** Golden bell hanging down from the origin (height ~1.5*s). */
    static void bell(G g, float s, float swing) {
        int oc = g.color;
        g.push();
        g.rotZ(swing);
        g.color(0xFFE8B92C);
        g.push();
        g.translate(0, -0.1f * s, 0);
        g.rotX(90);
        g.torus("xm_gold", 0.18f * s, 0.06f * s, 8, 4, 0, 0, 1, 1);
        g.pop();
        lathe(g, "xm_gold", new float[]{0, 0.28f * s, 0.42f * s, 0.5f * s, 0.62f * s, 0.8f * s, 0.84f * s, 0.7f * s, 0},
                new float[]{-0.2f * s, -0.24f * s, -0.4f * s, -0.7f * s, -1.05f * s, -1.3f * s, -1.4f * s, -1.44f * s, -1.44f * s}, 10);
        g.color(0xFF8A6A1A);
        ball(g, "c3_metal", 0, -1.48f * s, 0, 0.2f * s, 6);
        g.pop();
        g.color(oc);
    }

    /** One holly leaf (spiky outline), lying in the xy plane along +x, slightly folded along the midrib. */
    static void hollyLeaf(G g, float len) {
        float w = len * 0.36f;
        float[][] top = {{0, 0}, {len * 0.18f, w * 0.75f}, {len * 0.3f, w * 0.55f}, {len * 0.45f, w}, {len * 0.58f, w * 0.62f}, {len * 0.74f, w * 0.9f}, {len * 0.84f, w * 0.4f}, {len, 0}};
        int oc = g.color;
        g.color(0xFFFFFFFF);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.rotX(s * 14);
            float[][] poly = new float[top.length][];
            for (int i = 0; i < top.length; i++) poly[s > 0 ? i : top.length - 1 - i] = new float[]{top[i][0], s * top[i][1]};
            extrude(g, "xm_holly", poly, -0.05f, 0.05f, new float[]{0, s > 0 ? 0 : 0.5f, 1, s > 0 ? 0.5f : 1}, new float[]{0.1f, 0.4f, 0.2f, 0.6f});
            g.pop();
        }
        g.color(oc);
    }

    /** Holly sprig: three leaves and three glossy berries, centered at the origin, leaves spread in the xz plane. */
    static void holly(G g, float s) {
        float[] angs = {20, 150, 270};
        for (int i = 0; i < 3; i++) {
            g.push();
            g.rotY(angs[i]);
            g.rotZ(12);
            hollyLeaf(g, 2.4f * s);
            g.pop();
        }
        int oc = g.color;
        g.color(0xFFE0182A);
        ball(g, "xm_gloss", 0.25f * s, 0.32f * s, 0.12f * s, 0.34f * s, 8);
        ball(g, "xm_gloss", -0.28f * s, 0.3f * s, 0.2f * s, 0.32f * s, 8);
        ball(g, "xm_gloss", 0.0f, 0.36f * s, -0.3f * s, 0.33f * s, 8);
        g.color(oc);
    }

    /** Ribbon bow: two loops, knot and two tails, facing +z, centered at the origin. */
    static void bow(G g, int argb, float s) {
        int oc = g.color;
        g.color(argb);
        for (int k = -1; k <= 1; k += 2) {
            g.push();
            g.translate(k * 0.75f * s, 0.12f * s, 0);
            g.rotZ(k * 18);
            g.scale(1, 0.62f, 0.5f);
            g.rotX(90);
            g.torus("xm_ribbon", 0.62f * s, 0.2f * s, 10, 4, 0, 0, 1, 1);
            g.pop();
            // tails
            float[][] yz = {{-0.1f * s, 0.05f * s}, {-0.6f * s, 0.12f * s}, {-1.2f * s, 0.05f * s}};
            g.push();
            g.rotZ(k * 24);
            ribbon(g, "xm_ribbon", 0, yz, new float[]{0.42f * s, 0.46f * s, 0.5f * s}, 0.08f * s);
            g.pop();
        }
        ball(g, "xm_ribbon", 0, 0.05f * s, 0.08f * s, 0.3f * s, 8);
        g.color(oc);
    }

    /** Faceted 3D star in the xy plane (points up), depth d each side. */
    static void star(G g, String tex, float r, float d, int pts) {
        int n = pts * 2;
        float[][] o = new float[n][];
        for (int i = 0; i < n; i++) {
            double a = Math.PI / 2 + Math.PI * i / pts;
            float rr = i % 2 == 0 ? r : r * 0.45f;
            o[i] = p((float) Math.cos(a) * rr, (float) Math.sin(a) * rr, 0);
        }
        float[] f = p(0, 0, d), b = p(0, 0, -d);
        for (int i = 0; i < n; i++) {
            float[] A = o[i], B = o[(i + 1) % n];
            g.quad(tex, f, B, A, A, new float[]{0.5f, 0.5f, 0, 0, 1, 0, 1, 0});
            g.quad(tex, b, A, B, B, new float[]{0.5f, 0.5f, 0, 1, 1, 1, 1, 1});
        }
    }

    /** 3D snowflake from thin bars (xy plane), radius r. */
    static void flake(G g, String tex, float r, float th) {
        float w = r * 0.09f;
        for (int i = 0; i < 3; i++) {
            g.push();
            g.rotZ(i * 60);
            g.box(tex, -r, -w, -th, r, w, th, 0, 0, 1, 1);
            g.pop();
        }
        for (int i = 0; i < 6; i++) {
            g.push();
            g.rotZ(i * 60);
            g.translate(0, r * 0.62f, 0);
            g.box(tex, -r * 0.24f, -w * 0.8f, -th * 0.8f, r * 0.24f, w * 0.8f, th * 0.8f, 0, 0, 1, 1);
            g.pop();
        }
    }

    /** Twisted light string wire along points (dark green). */
    static void wire(G g, float[][] pts, float r) {
        int oc = g.color;
        g.color(0xFF1E3A22);
        float[] rr = new float[pts.length];
        java.util.Arrays.fill(rr, r);
        Geo.chain(g, "c3_metal", pts, rr, 3);
        g.color(oc);
    }
}
