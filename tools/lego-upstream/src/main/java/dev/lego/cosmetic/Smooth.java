package dev.lego.cosmetic;

import java.util.HashMap;

/**
 * Smooth vertex normals for cosmetic meshes: every quad corner gets the average of the face normals of all quads
 * (same texture group) that share that corner position and are within the crease angle. Curved surfaces (spheres,
 * lathes, sweeps, bevels) then shade smoothly while hard edges (boxes, 90 degree creases) stay crisp.
 */
final class Smooth {
    private Smooth() { }

    /** cos of the crease angle: faces meeting at a sharper angle are not smoothed together (~65 degrees). */
    static final float CREASE = 0.42f;

    /** Smoothing group for no smoothing at all (faceted gems / crystals keep their facets). */
    static final int FLAT = Integer.MIN_VALUE;

    /** Smoothing group of a texture (FLAT for faceted materials). */
    static int group(String tex, boolean glow) {
        if (tex.contains("crystal") || tex.contains("gem")) return FLAT;
        return tex.hashCode() * 7 + (glow ? 1 : 0);
    }

    /**
     * @param p   quad corners, 12 floats each
     * @param n   face normals, 3 floats each
     * @param grp smoothing group per quad (e.g. texture hash); only equal groups are merged
     * @param cnt number of quads
     * @return vertex normals, 12 floats per quad (unit length)
     */
    static float[] normals(float[][] p, float[][] n, int[] grp, int cnt) {
        HashMap<Long, int[]> map = new HashMap<>(cnt * 3);
        long[] keys = new long[cnt * 4];
        for (int q = 0; q < cnt; q++) {
            if (grp[q] == FLAT) continue;
            float[] pp = p[q];
            for (int k = 0; k < 4; k++) {
                long key = key(pp[k * 3], pp[k * 3 + 1], pp[k * 3 + 2], grp[q]);
                keys[q * 4 + k] = key;
                int[] l = map.get(key);
                if (l == null) { l = new int[5]; map.put(key, l); }
                else if (l[0] + 1 >= l.length) { l = java.util.Arrays.copyOf(l, l.length * 2); map.put(key, l); }
                // avoid adding the same quad twice for degenerate (triangle) quads
                if (l[0] == 0 || l[l[0]] != q) l[++l[0]] = q;
            }
        }
        float[] out = new float[cnt * 12];
        for (int q = 0; q < cnt; q++) {
            float[] fn = n[q];
            if (grp[q] == FLAT) {
                for (int k = 0; k < 4; k++) { out[q * 12 + k * 3] = fn[0]; out[q * 12 + k * 3 + 1] = fn[1]; out[q * 12 + k * 3 + 2] = fn[2]; }
                continue;
            }
            for (int k = 0; k < 4; k++) {
                int[] l = map.get(keys[q * 4 + k]);
                float sx = 0, sy = 0, sz = 0;
                for (int i = 1; i <= l[0]; i++) {
                    float[] o = n[l[i]];
                    if (o[0] * fn[0] + o[1] * fn[1] + o[2] * fn[2] < CREASE) continue;
                    sx += o[0]; sy += o[1]; sz += o[2];
                }
                float len = (float) Math.sqrt(sx * sx + sy * sy + sz * sz);
                if (len < 1e-5f) { sx = fn[0]; sy = fn[1]; sz = fn[2]; len = 1; }
                out[q * 12 + k * 3] = sx / len;
                out[q * 12 + k * 3 + 1] = sy / len;
                out[q * 12 + k * 3 + 2] = sz / len;
            }
        }
        return out;
    }

    private static long key(float x, float y, float z, int g) {
        long qx = Math.round(x * 40) & 0xFFFFF, qy = Math.round(y * 40) & 0xFFFFF, qz = Math.round(z * 40) & 0xFFFFF;
        return ((qx << 40) | (qy << 20) | qz) * 31 + g;
    }
}
