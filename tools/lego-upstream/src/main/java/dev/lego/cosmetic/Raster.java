package dev.lego.cosmetic;

import java.util.ArrayList;
import java.util.List;

/**
 * Small software renderer for cosmetic thumbnails (no GPU, runs on the painter thread):
 * perspective camera, z-buffer, bilinear texture sampling, simple lighting, 2x supersampling.
 */
public final class Raster {
    private Raster() { }

    private static final class Tri {
        float[] p = new float[9];   // camera space x,y,z
        float[] uv = new float[6];
        CTex.T tex;
        int color;
        boolean glow;
        float[] vs = new float[3], sp = new float[3];   // per vertex diffuse light + specular
        float depth;
    }

    /**
     * @param yaw   camera yaw in degrees (0 = looking at the player's front, 180 = back)
     * @param pitch camera pitch in degrees (positive = from above)
     */
    public static int[] render(Cos.Item item, int w, int h, float yaw, float pitch, float time, boolean mannequin, int bgArgb) {
        return render(item, null, w, h, yaw, pitch, time, mannequin, bgArgb);
    }

    /** Mannequin in an emote pose (pose array as in EmoteLib, radians, NaN = rest). */
    public static int[] renderPose(float[] pose, int w, int h, float yaw, float pitch, int bgArgb) {
        return render(null, pose, w, h, yaw, pitch, 0, true, bgArgb);
    }

    /** Light setup (camera space): key light from the upper left front, cool fill from the right, rim from behind. */
    private static final float[] KEY = norm(-0.42f, 0.72f, 0.55f), FILL = norm(0.75f, 0.1f, 0.45f), HALF = norm(-0.42f, 0.72f, 1.55f);

    private static float[] norm(float x, float y, float z) { float l = (float) Math.sqrt(x * x + y * y + z * z); return new float[]{x / l, y / l, z / l}; }

    private static int[] render(Cos.Item item, float[] pose, int w, int h, float yaw, float pitch, float time, boolean mannequin, int bgArgb) {
        final float cy = (float) Math.cos(Math.toRadians(yaw)), sy = (float) Math.sin(Math.toRadians(yaw));
        final float cp = (float) Math.cos(Math.toRadians(pitch)), sp = (float) Math.sin(Math.toRadians(pitch));
        float[] focus = focus(item == null ? null : item.slot);
        // 1) collect quads in camera space
        final List<float[]> qp = new ArrayList<>(), qn = new ArrayList<>(), quv = new ArrayList<>();
        final List<String> qt = new ArrayList<>();
        final List<int[]> qc = new ArrayList<>();
        G.Sink sink = (tex, p, uv, argb, glow, nx, ny, nz) -> {
            float[] q = new float[12];
            for (int i = 0; i < 4; i++) {
                float x = p[i * 3] - focus[0], y = p[i * 3 + 1] - focus[1], z = p[i * 3 + 2] - focus[2];
                float x1 = x * cy - z * sy, z1 = x * sy + z * cy;
                float y2 = y * cp - z1 * sp, z2 = y * sp + z1 * cp;
                q[i * 3] = x1; q[i * 3 + 1] = y2; q[i * 3 + 2] = z2;
            }
            float nx1 = nx * cy - nz * sy, nz1 = nx * sy + nz * cy;
            float ny2 = ny * cp - nz1 * sp, nz2 = ny * sp + nz1 * cp;
            qp.add(q);
            qn.add(new float[]{nx1, ny2, nz2});
            quv.add(uv);
            qt.add(tex);
            qc.add(new int[]{argb, glow ? 1 : 0});
        };
        G g = new G(sink);
        Cos.A a = new Cos.A();
        a.time = time;
        a.preview = true;
        boolean pet = item != null && item.slot == Cos.Slot.PET;
        boolean veh = item != null && item.slot == Cos.Slot.VEHICLE;
        Vehicles.Stance vst = veh ? Vehicles.stance(item.id) : null;
        boolean man = mannequin && !pet && !(veh && (vst == Vehicles.Stance.SIT || vst == Vehicles.Stance.PEDAL || vst == Vehicles.Stance.RIDE));
        if (man) mannequin(g, pose);
        if (item != null) {
            g.push();
            if (item.slot == Cos.Slot.CAPE) Models.capeGeometry(g, a, item.cape);
            else if (veh) { g.translate(0, -24 - Math.max(0, Vehicles.lift(item.id, time)), 0); item.model.render(g, a); }
            else item.model.render(g, a);
            g.pop();
        }
        if (man && item != null && item.slot != Cos.Slot.HAT && item.slot != Cos.Slot.FACE || pose != null) groundShadow(g);
        // 2) smooth vertex normals (per texture group) + lighting per vertex
        int n = qp.size();
        float[][] P = qp.toArray(new float[0][]), N = qn.toArray(new float[0][]);
        int[] grp = new int[n];
        for (int i = 0; i < n; i++) grp[i] = Smooth.group(qt.get(i), qc.get(i)[1] != 0);
        float[] vn = Smooth.normals(P, N, grp, n);
        List<Tri> tris = new ArrayList<>(n * 2);
        int[][] idx = {{0, 1, 2}, {0, 2, 3}};
        java.util.Map<String, CTex.T> texCache = new java.util.HashMap<>();
        for (int qi = 0; qi < n; qi++) {
            float[] q = P[qi], fnn = N[qi], uv = quv.get(qi);
            int argb = qc.get(qi)[0];
            boolean glow = qc.get(qi)[1] != 0;
            CTex.T t = texCache.computeIfAbsent(qt.get(qi), CTex::get);
            // two sided: face the normals towards the camera (camera sits on +z)
            float cx = (q[0] + q[3] + q[6] + q[9]) / 4, cyy = (q[1] + q[4] + q[7] + q[10]) / 4, cz = (q[2] + q[5] + q[8] + q[11]) / 4;
            float camD = distance(item == null ? null : item.slot);
            float vx = -cx, vy = -cyy, vz = camD - cz;
            float flip = fnn[0] * vx + fnn[1] * vy + fnn[2] * vz < 0 ? -1 : 1;
            float[] shade = new float[4], spc = new float[4];
            for (int k = 0; k < 4; k++) {
                if (glow) { shade[k] = 1f; continue; }
                float nx = vn[qi * 12 + k * 3] * flip, ny = vn[qi * 12 + k * 3 + 1] * flip, nz = vn[qi * 12 + k * 3 + 2] * flip;
                float d = nx * KEY[0] + ny * KEY[1] + nz * KEY[2];
                float f = nx * FILL[0] + ny * FILL[1] + nz * FILL[2];
                float hh = Math.max(0, nx * HALF[0] + ny * HALF[1] + nz * HALF[2]);
                float rim = 1 - Math.max(0, nz);
                float s = 0.36f + 0.62f * Math.max(0, d) + 0.16f * Math.max(0, f) + 0.2f * rim * rim * rim + 0.07f * Math.max(0, ny);
                float spec = (float) Math.pow(hh, 28) * 0.22f;
                shade[k] = s;
                spc[k] = spec;
            }
            for (int[] kk : idx) {
                Tri tr = new Tri();
                for (int j = 0; j < 3; j++) {
                    System.arraycopy(q, kk[j] * 3, tr.p, j * 3, 3);
                    tr.uv[j * 2] = uv[kk[j] * 2];
                    tr.uv[j * 2 + 1] = uv[kk[j] * 2 + 1];
                    tr.vs[j] = shade[kk[j]];
                    tr.sp[j] = spc[kk[j]];
                }
                tr.tex = t;
                tr.color = argb;
                tr.glow = glow;
                tr.depth = (tr.p[2] + tr.p[5] + tr.p[8]) / 3f;
                tris.add(tr);
            }
        }
        // painter order: far first (camera looks down -z, larger z = closer)
        tris.sort((t1, t2) -> Float.compare(t1.depth, t2.depth));

        int ss = 2, W = w * ss, H = h * ss;
        float[] zb = new float[W * H];
        java.util.Arrays.fill(zb, -1e9f);
        float[] cr = new float[W * H], cg = new float[W * H], cb = new float[W * H], ca = new float[W * H];
        float camDist = distance(item == null ? null : item.slot);
        float fov = (float) (H * 1.9);
        for (Tri t : tris) raster(t, W, H, camDist, fov, zb, cr, cg, cb, ca);

        int[] out = new int[w * h];
        float br = ((bgArgb >> 16) & 255) / 255f, bgg = ((bgArgb >> 8) & 255) / 255f, bb = (bgArgb & 255) / 255f, ba = (bgArgb >>> 24) / 255f;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                float r = 0, gg = 0, b = 0, al = 0;
                for (int sy2 = 0; sy2 < ss; sy2++)
                    for (int sx = 0; sx < ss; sx++) {
                        int i = (y * ss + sy2) * W + x * ss + sx;
                        r += cr[i]; gg += cg[i]; b += cb[i]; al += ca[i];
                    }
                int nn = ss * ss;
                r /= nn; gg /= nn; b /= nn; al /= nn;
                // composite over background (premultiplied accumulation)
                float oa = al + ba * (1 - al);
                float or = r + br * ba * (1 - al), og = gg + bgg * ba * (1 - al), ob = b + bb * ba * (1 - al);
                if (oa > 0.0001f) { or /= oa; og /= oa; ob /= oa; }
                out[y * w + x] = (clamp(oa) << 24) | (clamp(or) << 16) | (clamp(og) << 8) | clamp(ob);
            }
        return out;
    }

    /** Soft contact shadow under the mannequin's feet. */
    private static void groundShadow(G g) {
        int c = g.color;
        boolean gl = g.glow;
        g.color(0x9A000000).glow(true);
        g.quad("mannequin_shadow", new float[]{-9, -24.05f, -7}, new float[]{9, -24.05f, -7}, new float[]{9, -24.05f, 7}, new float[]{-9, -24.05f, 7}, new float[]{0, 0, 1, 0, 1, 1, 0, 1});
        g.color(c).glow(gl);
    }

    private static int clamp(float v) { return Math.max(0, Math.min(255, Math.round(v * 255))); }

    private static float[] focus(Cos.Slot s) {
        if (s == null) return new float[]{0, -5, 0};
        switch (s) {
            case HAT: return new float[]{0, 9f, 0};
            case FACE: return new float[]{0, 5f, 0};
            case VEHICLE: return new float[]{0, -17, 0};
            case PET: return new float[]{0, 5, 0};      // pets are drawn alone (pet space, origin at the feet)
            case AURA: return new float[]{0, -11, 0};
            case WINGS: return new float[]{0, -6, -4};
            case CAPE: case BACK: return new float[]{0, -7, -2};
            default: return new float[]{0, -6, 0};
        }
    }

    private static float distance(Cos.Slot s) {
        if (s == null) return 82;
        switch (s) {
            case HAT: return 40;
            case FACE: return 28;
            case VEHICLE: return 130;
            case PET: return 30;
            case WINGS: return 86;
            case AURA: return 66;
            default: return 48;
        }
    }

    private static void raster(Tri t, int W, int H, float camDist, float fov, float[] zb, float[] cr, float[] cg, float[] cb, float[] ca) {
        float[] sx = new float[3], sy = new float[3], iz = new float[3];
        for (int i = 0; i < 3; i++) {
            float z = camDist - t.p[i * 3 + 2];
            if (z < 1f) return;
            iz[i] = 1f / z;
            sx[i] = W / 2f - t.p[i * 3] * fov * iz[i];
            sy[i] = H / 2f - t.p[i * 3 + 1] * fov * iz[i];
        }
        float area = (sx[1] - sx[0]) * (sy[2] - sy[0]) - (sx[2] - sx[0]) * (sy[1] - sy[0]);
        if (Math.abs(area) < 1e-6f) return;
        int x0 = Math.max(0, (int) Math.floor(Math.min(sx[0], Math.min(sx[1], sx[2])))), x1 = Math.min(W - 1, (int) Math.ceil(Math.max(sx[0], Math.max(sx[1], sx[2]))));
        int y0 = Math.max(0, (int) Math.floor(Math.min(sy[0], Math.min(sy[1], sy[2])))), y1 = Math.min(H - 1, (int) Math.ceil(Math.max(sy[0], Math.max(sy[1], sy[2]))));
        float tr = ((t.color >> 16) & 255) / 255f, tg = ((t.color >> 8) & 255) / 255f, tb = (t.color & 255) / 255f, ta = (t.color >>> 24) / 255f;
        for (int y = y0; y <= y1; y++)
            for (int x = x0; x <= x1; x++) {
                float px = x + 0.5f, py = y + 0.5f;
                float w0 = ((sx[1] - px) * (sy[2] - py) - (sx[2] - px) * (sy[1] - py)) / area;
                float w1 = ((sx[2] - px) * (sy[0] - py) - (sx[0] - px) * (sy[2] - py)) / area;
                float w2 = 1 - w0 - w1;
                if (w0 < -0.0001f || w1 < -0.0001f || w2 < -0.0001f) continue;
                float izz = w0 * iz[0] + w1 * iz[1] + w2 * iz[2];
                int i = y * W + x;
                if (izz < zb[i] - 1e-7f) continue;
                float u = (w0 * t.uv[0] * iz[0] + w1 * t.uv[2] * iz[1] + w2 * t.uv[4] * iz[2]) / izz;
                float v = (w0 * t.uv[1] * iz[0] + w1 * t.uv[3] * iz[1] + w2 * t.uv[5] * iz[2]) / izz;
                int c = bilinear(t.tex, u, v);
                float a = ((c >>> 24) / 255f) * ta;
                if (a < 0.02f) continue;
                float sh = w0 * t.vs[0] + w1 * t.vs[1] + w2 * t.vs[2], spc = w0 * t.sp[0] + w1 * t.sp[1] + w2 * t.sp[2];
                float r = ((c >> 16) & 255) / 255f * tr * sh + spc, g = ((c >> 8) & 255) / 255f * tg * sh + spc, b = (c & 255) / 255f * tb * sh + spc;
                if (t.glow) { r *= 1.1f; g *= 1.1f; b *= 1.1f; }
                r = Math.min(1, r); g = Math.min(1, g); b = Math.min(1, b);
                // premultiplied "over"
                cr[i] = r * a + cr[i] * (1 - a);
                cg[i] = g * a + cg[i] * (1 - a);
                cb[i] = b * a + cb[i] * (1 - a);
                ca[i] = a + ca[i] * (1 - a);
                if (a > 0.5f) zb[i] = izz;
            }
    }

    private static int bilinear(CTex.T t, float u, float v) {
        float fx = u * t.w - 0.5f, fy = v * t.h - 0.5f;
        int x0 = (int) Math.floor(fx), y0 = (int) Math.floor(fy);
        float ax = fx - x0, ay = fy - y0;
        int c00 = px(t, x0, y0), c10 = px(t, x0 + 1, y0), c01 = px(t, x0, y0 + 1), c11 = px(t, x0 + 1, y0 + 1);
        int out = 0;
        for (int s = 0; s < 32; s += 8) {
            float a = ((c00 >>> s) & 255) * (1 - ax) + ((c10 >>> s) & 255) * ax;
            float b = ((c01 >>> s) & 255) * (1 - ax) + ((c11 >>> s) & 255) * ax;
            out |= (Math.round(a * (1 - ay) + b * ay) & 255) << s;
        }
        return out;
    }

    private static int px(CTex.T t, int x, int y) {
        x = Math.max(0, Math.min(t.w - 1, x));
        y = Math.max(0, Math.min(t.h - 1, y));
        return t.argb[y * t.w + x];
    }

    /** Neutral display mannequin (body space), optionally posed: soft two-tone vinyl with rounded edges. */
    static final int MQ_SKIN = 0xFF727989, MQ_SHIRT = 0xFF525A6C, MQ_PANTS = 0xFF434857;

    static void mannequin(G g, float[] pose) {
        int oc = g.color;
        float bend = pose == null || Float.isNaN(pose[3]) ? 0 : pose[3];
        g.push();
        if (bend != 0) { g.translate(0, -12, 0); g.rotX((float) Math.toDegrees(bend)); g.translate(0, 12, 0); }
        g.color(MQ_SKIN);
        part(g, pose, 0, 0, 0, 0, -4, 0, -4, 4, 8, 4, "mannequin_head");       // head
        float[] bodyPose = pose == null ? null : new float[]{Float.NaN, Float.NaN, Float.NaN, Float.NaN, pose[4], pose[5]};
        g.color(MQ_SHIRT);
        part(g, bodyPose, 3, 0, 0, 0, -4, -12, -2, 4, 0, 2, "mannequin");       // body (bend applied to the whole group)
        g.color(MQ_SKIN);
        part(g, pose, 6, 5, -2, 0, -1, -10, -2, 3, 2, 2, "mannequin");          // right arm (pivot at the shoulder)
        part(g, pose, 9, -5, -2, 0, -3, -10, -2, 1, 2, 2, "mannequin");         // left arm
        g.pop();
        g.color(MQ_PANTS);
        part(g, pose, 12, 1.9f, -12, 0, -2, -12, -2, 2, 0, 2, "mannequin"); // right leg
        part(g, pose, 15, -1.9f, -12, 0, -2, -12, -2, 2, 0, 2, "mannequin");
        g.color(oc);
    }

    private static void part(G g, float[] pose, int idx, float px, float py, float pz, float x0, float y0, float z0, float x1, float y1, float z1, String tex) {
        g.push();
        g.translate(px, py, pz);
        if (pose != null) {
            float p = pose[idx], yw = pose[idx + 1], r = pose[idx + 2];
            if (!Float.isNaN(r)) g.rotZ((float) Math.toDegrees(r));
            if (!Float.isNaN(yw)) g.rotY((float) Math.toDegrees(yw));
            if (!Float.isNaN(p)) g.rotX((float) Math.toDegrees(p));
        }
        Cos3Geo.bbox(g, tex, x0, y0, z0, x1, y1, z1, 0.45f);
        g.pop();
    }
}
