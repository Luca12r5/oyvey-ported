package dev.lego.cosmetic;

import static dev.lego.cosmetic.Cos3Geo.*;

import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

import dev.lego.cosmetic.Cos.A;

/**
 * HD remaster of the butterfly, fairy and cyber wings (body space, wings attach at the upper back, z = -2.3).
 * Wing cards follow the painted outline exactly (solid silhouette with a rim and slight thickness) instead of
 * a rectangle with transparent corners.
 */
final class HdWings {
    private HdWings() { }

    private static float[] p(float x, float y, float z) { return new float[]{x, y, z}; }

    /** Outline of a painted path (texture pixels) as a polygon with at most max points. */
    static float[][] outline(Path2D path, double flat, int max) {
        List<float[]> pts = new ArrayList<>();
        double[] c = new double[6];
        for (PathIterator it = new FlatteningPathIterator(path.getPathIterator(null), flat); !it.isDone(); it.next()) {
            int k = it.currentSegment(c);
            if (k == PathIterator.SEG_MOVETO || k == PathIterator.SEG_LINETO) {
                float[] q = {(float) c[0], (float) c[1]};
                if (pts.isEmpty() || Math.hypot(q[0] - pts.get(pts.size() - 1)[0], q[1] - pts.get(pts.size() - 1)[1]) > 0.5) pts.add(q);
            }
        }
        if (pts.size() > 2 && Math.hypot(pts.get(0)[0] - pts.get(pts.size() - 1)[0], pts.get(0)[1] - pts.get(pts.size() - 1)[1]) < 1) pts.remove(pts.size() - 1);
        int step = Math.max(1, (int) Math.ceil(pts.size() / (double) max));
        List<float[]> out = new ArrayList<>();
        for (int i = 0; i < pts.size(); i += step) out.add(pts.get(i));
        return out.toArray(new float[0][]);
    }

    /**
     * Solid wing card: polygon in texture pixel space (tw x th) mapped to w x h model units (origin = texture top-left,
     * extending +x / -y), curved back by curve, with front + back faces (thickness d) and a rim.
     */
    static void card(G g, String tex, float[][] poly, float tw, float th, float w, float h, float curve, float d, String rimTex, int rimCol) {
        float[][] o = HdGeo.ccw(flipY(poly));
        int n = o.length;
        float[][] P = new float[n][];
        for (int i = 0; i < n; i++) P[i] = wp(o[i], tw, th, w, h, curve);
        List<int[]> tris = Cos3Geo.triangulate(o);
        int oc = g.color;
        for (int[] tr : tris) {
            float[] a = P[tr[0]], b = P[tr[1]], c = P[tr[2]];
            float[] uv = {uvx(o[tr[0]], tw), uvy(o[tr[0]], th), uvx(o[tr[1]], tw), uvy(o[tr[1]], th), uvx(o[tr[2]], tw), uvy(o[tr[2]], th)};
            g.quad(tex, off(a, d), off(b, d), off(c, d), off(c, d), new float[]{uv[0], uv[1], uv[2], uv[3], uv[4], uv[5], uv[4], uv[5]});
            g.quad(tex, off(c, -d), off(b, -d), off(a, -d), off(a, -d), new float[]{uv[4], uv[5], uv[2], uv[3], uv[0], uv[1], uv[0], uv[1]});
        }
        g.color(rimCol);
        for (int i = 0; i < n; i++) {
            float[] a = P[i], b = P[(i + 1) % n];
            g.quad(rimTex, off(a, -d * 1.6f), off(b, -d * 1.6f), off(b, d * 1.6f), off(a, d * 1.6f), new float[]{0, 0, 1, 0, 1, 1, 0, 1});
        }
        g.color(oc);
    }

    /** Texture y grows downwards; model y grows upwards. */
    private static float[][] flipY(float[][] o) {
        float[][] r = new float[o.length][];
        for (int i = 0; i < o.length; i++) r[i] = new float[]{o[i][0], -o[i][1]};
        return r;
    }

    private static float uvx(float[] q, float tw) { return q[0] / tw; }
    private static float uvy(float[] q, float th) { return -q[1] / th; }

    private static float[] wp(float[] q, float tw, float th, float w, float h, float curve) {
        float x = q[0] / tw * w, y = q[1] / th * h;
        return p(x, y, -curve * (x * x + y * y * 0.4f));
    }

    private static float[] off(float[] q, float d) { return p(q[0], q[1], q[2] + d); }

    // ================================================================== Schmetterling
    private static float[][] BUP, BLO, FAIRY;

    private static synchronized void shapes() {
        if (BUP != null) return;
        Path2D up = new Path2D.Double();
        up.moveTo(6, 30); up.curveTo(90, -30, 250, -10, 246, 80); up.curveTo(242, 140, 150, 170, 8, 150); up.closePath();
        Path2D lo = new Path2D.Double();
        lo.moveTo(8, 150); lo.curveTo(120, 150, 210, 200, 190, 270); lo.curveTo(170, 330, 60, 320, 6, 230); lo.closePath();
        Path2D f = new Path2D.Double();
        f.moveTo(2, 64); f.curveTo(60, 0, 200, -10, 250, 40); f.curveTo(262, 70, 200, 112, 120, 110); f.curveTo(60, 108, 20, 90, 2, 64);
        BUP = outline(up, 1.5, 30);
        BLO = outline(lo, 1.5, 26);
        FAIRY = outline(f, 1.5, 30);
    }

    static void butterfly(G g, A a) {
        shapes();
        float t = a.time;
        float mv = a.preview ? 0 : a.move;
        float flutter = a.sneak ? 72 : Geo.lerp(18 + 22 * (0.5f + 0.5f * sin(t * 2.6f)), 20 + 45 * (0.5f + 0.5f * sin(t * 12)), mv);
        for (int side = 1; side >= -1; side -= 2) {
            g.push();
            g.scale(side, 1, 1);
            g.translate(0.55f, 2.5f, -2.9f);
            g.rotY(flutter);
            g.rotZ(8);
            g.color(0xFFFFFFFF);
            card(g, "wing_butterfly2", BUP, 256, 320, 13.5f, 17, 0.014f, 0.07f, "hd_rubber", 0xFF07060F);
            g.push();
            g.rotY(6);
            card(g, "wing_butterfly2", BLO, 256, 320, 13.5f, 17, 0.012f, 0.07f, "hd_rubber", 0xFF07060F);
            g.pop();
            g.pop();
        }
        // body: thorax + segmented abdomen + head with clubbed antennae
        g.color(0xFF1A1424);
        Geo.ellipsoid(g, "hd_fur", 0, 1.2f, -3.0f, 0.75f, 1.6f, 0.75f, 10, 6);
        for (int i = 0; i < 5; i++) Geo.ellipsoid(g, "hd_fur", 0, -1.2f - i * 1.25f, -3.0f + i * 0.05f, 0.62f - i * 0.07f, 0.75f, 0.62f - i * 0.07f, 8, 5);
        ball(g, "hd_fur", 0, 3.3f, -3.1f, 0.62f, 8);
        for (int s = -1; s <= 1; s += 2) {
            float sw = sin(t * 2.2f + s) * 0.3f;
            float[][] ant = {{s * 0.2f, 3.7f, -3.1f}, {s * 0.9f, 5.2f, -3.3f + sw}, {s * 1.5f, 6.4f, -3.6f + sw}};
            limb(g, "hd_plastic", ant, new float[]{0.08f, 0.07f, 0.06f}, 4);
            ball(g, "hd_gloss", ant[2][0], ant[2][1], ant[2][2], 0.22f, 6);
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Feenflügel
    static void fairy(G g, A a) {
        shapes();
        float t = a.time;
        float mv = a.preview ? 0 : a.move;
        float fl = a.sneak ? 70 : Geo.lerp(22 + 18 * (0.5f + 0.5f * sin(t * 3.4f)), 20 + 40 * (0.5f + 0.5f * sin(t * 16)), mv);
        for (int side = 1; side >= -1; side -= 2) {
            for (int lobe = 0; lobe < 2; lobe++) {
                g.push();
                g.scale(side, 1, 1);
                g.translate(0.6f, lobe == 0 ? -1.5f : -4f, -2.5f);
                g.rotY(fl + (lobe == 0 ? 0 : 8) + sin(t * 3.4f + lobe) * 3);
                g.rotZ(lobe == 0 ? 28 : -34);
                float w = lobe == 0 ? 17 : 12, h = lobe == 0 ? 8.5f : 6;
                g.translate(0, h / 2, 0);
                g.glow(true).color(0xE6FFFFFF);
                card(g, "wing_fairy", FAIRY, 256, 128, w, h, 0.012f, 0.04f, "white", 0xFFE8F4FF);
                // glowing veins
                g.color(0xFFB8F0FF);
                for (int k = 0; k < 3; k++) {
                    float[][] vein = new float[6][];
                    for (int i = 0; i < 6; i++) {
                        float f = i / 5f;
                        float x = f * w * (0.62f + 0.15f * k), y = -h * 0.5f + h * (0.1f * k + 0.25f) * (float) Math.sin(f * Math.PI * 0.8) + h * (0.1f - 0.2f * k) * f;
                        vein[i] = p(x, y, -0.012f * x * x + 0.06f);
                    }
                    Geo.chain(g, "white", vein, new float[]{0.08f, 0.07f, 0.06f, 0.05f, 0.04f, 0.03f}, 3);
                }
                g.glow(false);
                g.pop();
            }
        }
        // glitter trail (tiny glowing gems drifting down)
        for (int i = 0; i < 10; i++) {
            float ph = fract(t * 0.5f + i / 10f);
            float s = sin(ph * Math.PI);
            float x = sin(i * 2.7f) * 9 + sin(t * 2 + i) * 1.2f, z = -4 - (i % 3) * 1.5f;
            Cos3Models.sparkle(g, x, -2 - ph * 16, z, 0.4f * s, alpha(s, i % 2 == 0 ? 0xFFFFFF : 0x9AF0FF));
            if (i % 3 == 0) halo(g, x, -2 - ph * 16, z, 2.2f * s, alpha(0.4 * s, 0xB8F0FF));
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Cyberflügel
    static void cyber(G g, A a) {
        Wings.Pose p = Wings.pose(a, 0.8f);
        float S = 17;
        for (int side = 1; side >= -1; side -= 2) {
            g.push();
            g.scale(side, 1, 1);
            g.translate(1.0f, -2f, -2.45f);
            g.rotY(p.sweep);
            g.rotZ(p.raise);
            g.rotX(-8);
            float[] O = {0, 0, 0}, E = {S * 0.4f, S * 0.22f, -0.3f}, W = {S * 0.82f * (1 - 0.15f * p.fold), S * 0.26f, -0.8f};
            // arm frame: armoured tubes with joints
            g.color(0xFF3A404C);
            limb(g, "hd_steel", new float[][]{O, E}, new float[]{0.95f, 0.8f}, 8);
            limb(g, "hd_steel", new float[][]{E, W}, new float[]{0.8f, 0.55f}, 8);
            g.color(0xFFD8DEE8);
            for (float[] j : new float[][]{O, E, W}) ball(g, "hd_chrome", j[0], j[1], j[2], 1.05f, 10);
            g.glow(true).color(0xFF2AE6FF);
            limb(g, "white", new float[][]{{O[0], O[1] + 0.85f, O[2]}, {E[0], E[1] + 0.75f, E[2]}, {W[0], W[1] + 0.55f, W[2]}}, new float[]{0.12f, 0.12f, 0.1f}, 4);
            halo(g, E[0], E[1], E[2] - 0.9f, 2.8f + 0.6f * sin(p.t * 4), alpha(0.6, 0x2AE6FF));
            halo(g, W[0], W[1], W[2] - 0.9f, 2.4f + 0.6f * sin(p.t * 4 + 1), alpha(0.6, 0x2AE6FF));
            g.glow(false);
            // blades: bevelled plates tapering to a point with a glowing core line
            int n = 8;
            for (int i = 0; i < n; i++) {
                float f = i / (float) (n - 1);
                float[] at = {Geo.lerp(1.5f, W[0], f), Geo.lerp(0.5f, W[1], f) - 0.3f, Geo.lerp(0, W[2], f) - 0.2f + i * 0.06f};
                float th = Geo.lerp(-4, 62, f) * (1 - 0.6f * p.fold) + sin(p.t * 2 + i) * 1.5f;
                float L = S * (0.5f + 0.42f * sin(Math.PI * (0.25 + 0.6 * f)));
                float w = S * 0.07f;
                g.push();
                g.translate(at[0], at[1], at[2]);
                g.rotZ(th);
                g.rotY(-8);
                float[][] bl = {{-w, 0.2f}, {w, 0.2f}, {w * 0.95f, -L * 0.78f}, {0, -L}, {-w * 0.95f, -L * 0.78f}};
                g.color(0xFFC8CED8);
                HdGeo.puff(g, "hd_steel", bl, 0.36f, 0.14f, 1);
                g.glow(true).color(alpha(0.8 + 0.2 * sin(p.t * 5 + i), 0x2AE6FF));
                g.push();
                g.translate(0, 0, -0.2f);
                cbox(g, "white", 0, -L * 0.42f, 0, 0.14f, L * 0.72f, 0.06f, 0.02f);
                g.pop();
                g.glow(false);
                g.pop();
            }
            g.pop();
        }
        // back mount with thruster
        g.color(0xFF3A404C);
        bbox(g, "hd_steel", -2.2f, -5.5f, -3.3f, 2.2f, -0.8f, -2.05f, 0.3f);
        g.color(0xFFD8DEE8);
        g.push();
        g.translate(0, -3.1f, -3.3f);
        g.rotX(-90);
        lathe(g, "hd_chrome", new float[]{1.2f, 1.2f, 0.9f, 0}, new float[]{0, 0.35f, 0.45f, 0.46f}, 14);
        g.pop();
        g.glow(true).color(0xFF2AE6FF);
        g.push();
        g.translate(0, -3.1f, -3.78f);
        g.rotX(90);
        g.torus("white", 0.62f, 0.13f, 14, 3, 0, 0, 1, 1);
        g.pop();
        g.glow(false);
        halo(g, 0, -3.1f, -4.2f, 3.2f, alpha(0.5, 0x2AE6FF));
        g.color(0xFFFFFFFF);
    }
}
