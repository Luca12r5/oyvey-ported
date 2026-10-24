package dev.lego.visual;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;

/**
 * Immediate-mode 3D helper for world visuals (Minecraft 1.21.11).
 * - colored quads: vanilla "debug quads" layer (position + color, no fog, depth tested, both sides)
 * - textured billboards: entity translucent layer
 * - sky: own layer with the end-sky pipeline (position/texture/color, no fog, no depth write)
 * All coordinates are world coordinates.
 */
public final class R3 {
    private R3() { }

    private static class_4597 consumers;
    private static class_4587.class_4665 entry;
    private static double cx, cy, cz;
    private static float camYaw, camPitch;
    private static class_4588 quads;
    private static class_1921 quadLayer;
    private static final java.util.Set<class_1921> used = new java.util.LinkedHashSet<>();

    /**
     * Batching: vertices are collected per render layer and written in one go at end()/flush().
     * Asking the immediate buffer source for a different layer flushes the previous one, so emitting
     * directly caused one draw call (+ buffer upload) per particle whenever textures alternated.
     */
    private static final class Batch {
        float[] d = new float[10 * 64];
        int n;          // floats used
        void v(double x, double y, double z, int argb, float u, float v, int light, float nx, float ny, float nz) {
            if (n + 10 > d.length) d = java.util.Arrays.copyOf(d, d.length * 2);
            float[] a = d; int i = n;
            a[i] = (float) (x - cx); a[i + 1] = (float) (y - cy); a[i + 2] = (float) (z - cz);
            a[i + 3] = Float.intBitsToFloat(argb); a[i + 4] = u; a[i + 5] = v; a[i + 6] = Float.intBitsToFloat(light);
            a[i + 7] = nx; a[i + 8] = ny; a[i + 9] = nz;
            n = i + 10;
        }
    }
    private static final Map<class_1921, Batch> BATCH = new java.util.LinkedHashMap<>();
    private static final java.util.ArrayDeque<Batch> POOL = new java.util.ArrayDeque<>();
    private static class_1921 lastLayer;
    private static Batch lastBatch;

    private static Batch batch(class_1921 layer) {
        if (layer == lastLayer) return lastBatch;
        Batch b = BATCH.get(layer);
        if (b == null) {
            b = POOL.isEmpty() ? new Batch() : POOL.pop();
            b.n = 0;
            BATCH.put(layer, b);
        }
        lastLayer = layer; lastBatch = b;
        return b;
    }

    /** Vertices emitted this frame (for the performance monitor). */
    public static int lastVertices, lastLayers;

    private static void emit() {
        int verts = 0;
        for (Map.Entry<class_1921, Batch> e : BATCH.entrySet()) {
            Batch b = e.getValue();
            if (b.n == 0) continue;
            class_4588 vc = consumers.method_73477(e.getKey());
            float[] a = b.d;
            for (int i = 0; i < b.n; i += 10) {
                vc.method_56824(entry, a[i], a[i + 1], a[i + 2])
                        .method_39415(Float.floatToRawIntBits(a[i + 3]))
                        .method_22913(a[i + 4], a[i + 5])
                        .method_22922(net.minecraft.class_4608.field_21444)
                        .method_60803(Float.floatToRawIntBits(a[i + 6]))
                        .method_60831(entry, a[i + 7], a[i + 8], a[i + 9]);
            }
            verts += b.n / 10;
            if (consumers instanceof class_4597.class_4598) ((class_4597.class_4598) consumers).method_22994(e.getKey());
        }
        lastVertices = verts; lastLayers = BATCH.size();
        for (Batch b : BATCH.values()) { b.n = 0; if (POOL.size() < 64) POOL.push(b); }
        BATCH.clear();
        lastLayer = null; lastBatch = null;
    }

    public static boolean begin(WorldRenderContext ctx) {
        consumers = ctx.consumers();
        class_4587 m = ctx.matrices();
        if (consumers == null || m == null) return false;
        entry = m.method_23760();
        class_243 cam = ctx.worldState().field_63082.field_63078;
        cx = cam.field_1352;
        cy = cam.field_1351;
        cz = cam.field_1350;
        try {
            net.minecraft.class_4184 c = dev.lego.core.Mc.mc().field_1773.method_19418();
            camYaw = c.method_19330();
            camPitch = c.method_19329();
        } catch (Throwable t) { camYaw = 0; camPitch = 0; }
        quads = null;
        used.clear();
        BATCH.clear(); lastLayer = null; lastBatch = null;
        return true;
    }

    /** > 0 while drawing inside the vanilla sky pass (camera at the origin, fixed radius). */
    public static double skyRadius = 0;

    /** Begin drawing inside the vanilla sky pass: camera-relative coordinates, rotation comes from the sky's model-view. */
    public static void beginSky(class_4597 imm) {
        consumers = imm;
        entry = new class_4587().method_23760();
        cx = cy = cz = 0;
        skyRadius = 90;
        try {
            net.minecraft.class_4184 c = dev.lego.core.Mc.mc().field_1773.method_19418();
            camYaw = c.method_19330();
            camPitch = c.method_19329();
        } catch (Throwable t) { camYaw = 0; camPitch = 0; }
        quads = null;
        used.clear();
        BATCH.clear(); lastLayer = null; lastBatch = null;
    }

    public static double camX() { return cx; }
    public static double camY() { return cy; }
    public static double camZ() { return cz; }
    public static float camYaw() { return camYaw; }
    public static float camPitch() { return camPitch; }

    private static Batch q() {
        if (quadLayer == null) quadLayer = class_12249.method_76023();   // RenderLayers.debugQuads()
        return batch(quadLayer);
    }

    // no-depth-test layer for through-walls rendering
    private static class_1921 noDepthLayer;
    private static boolean noDepthBroken;
    private static Batch qNoDepth() {
        if (noDepthLayer == null && !noDepthBroken) {
            try {
                java.lang.reflect.Field snippetField = net.minecraft.class_10799.class.getDeclaredField("field_56860");
                snippetField.setAccessible(true);
                com.mojang.blaze3d.pipeline.RenderPipeline.Snippet snippet = (com.mojang.blaze3d.pipeline.RenderPipeline.Snippet) snippetField.get(null);
                com.mojang.blaze3d.pipeline.RenderPipeline pipeline = dev.lego.visual.shader.ShaderUtil.register(
                    com.mojang.blaze3d.pipeline.RenderPipeline.builder(snippet)
                        .withLocation("legoclient:pipeline/no_depth_quads")
                        .withCull(false)
                        .withDepthTestFunction(com.mojang.blaze3d.platform.DepthTestFunction.NO_DEPTH_TEST)
                        .withDepthWrite(false)
                        .build());
                class_12247 setup = class_12247.method_75927(pipeline).method_75937().method_75938();
                if (layerOf == null) {
                    layerOf = class_1921.class.getDeclaredMethod("method_75940", String.class, class_12247.class);
                    layerOf.setAccessible(true);
                }
                noDepthLayer = (class_1921) layerOf.invoke(null, "legoclient_no_depth", setup);
                dev.lego.LegoClient.LOG("No-depth render layer created successfully");
            } catch (Throwable t) {
                noDepthBroken = true;
                dev.lego.LegoClient.LOG("No-depth layer failed, using debugQuads: " + t);
                t.printStackTrace();
            }
        }
        return batch(noDepthLayer != null ? noDepthLayer : (quadLayer != null ? quadLayer : (quadLayer = class_12249.method_76023())));
    }

    private static void v(Batch vc, double x, double y, double z, int argb) {
        vc.v(x, y, z, argb, 0f, 0f, 0xF000F0, 0f, 1f, 0f);
    }

    /** Colored quad (4 corners, any winding; the layer is double sided). */
    public static void quad(double x1, double y1, double z1, double x2, double y2, double z2,
                            double x3, double y3, double z3, double x4, double y4, double z4, int c1, int c2, int c3, int c4) {
        Batch vc = q();
        v(vc, x1, y1, z1, c1);
        v(vc, x2, y2, z2, c2);
        v(vc, x3, y3, z3, c3);
        v(vc, x4, y4, z4, c4);
    }

    public static void quad(double x1, double y1, double z1, double x2, double y2, double z2,
                            double x3, double y3, double z3, double x4, double y4, double z4, int c) {
        quad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, c, c, c, c);
    }

    /** Horizontal ring (annulus) around (x,y,z). */
    public static void ring(double x, double y, double z, double r0, double r1, int seg, int cInner, int cOuter, double rot) {
        for (int i = 0; i < seg; i++) {
            double a0 = rot + Math.PI * 2 * i / seg, a1 = rot + Math.PI * 2 * (i + 1) / seg;
            double c0 = Math.cos(a0), s0 = Math.sin(a0), c1 = Math.cos(a1), s1 = Math.sin(a1);
            quad(x + c0 * r0, y, z + s0 * r0, x + c0 * r1, y, z + s0 * r1, x + c1 * r1, y, z + s1 * r1, x + c1 * r0, y, z + s1 * r0,
                    cInner, cOuter, cOuter, cInner);
        }
    }

    /** Ring in an arbitrary plane given by two orthonormal axes u and v. */
    public static void ring3(double x, double y, double z, double[] u, double[] w, double r0, double r1, int seg, int c) {
        for (int i = 0; i < seg; i++) {
            double a0 = Math.PI * 2 * i / seg, a1 = Math.PI * 2 * (i + 1) / seg;
            double c0 = Math.cos(a0), s0 = Math.sin(a0), c1 = Math.cos(a1), s1 = Math.sin(a1);
            quad(x + (u[0] * c0 + w[0] * s0) * r0, y + (u[1] * c0 + w[1] * s0) * r0, z + (u[2] * c0 + w[2] * s0) * r0,
                    x + (u[0] * c0 + w[0] * s0) * r1, y + (u[1] * c0 + w[1] * s0) * r1, z + (u[2] * c0 + w[2] * s0) * r1,
                    x + (u[0] * c1 + w[0] * s1) * r1, y + (u[1] * c1 + w[1] * s1) * r1, z + (u[2] * c1 + w[2] * s1) * r1,
                    x + (u[0] * c1 + w[0] * s1) * r0, y + (u[1] * c1 + w[1] * s1) * r0, z + (u[2] * c1 + w[2] * s1) * r0, c);
        }
    }

    /** Small axis aligned cube. */
    public static void cube(double x, double y, double z, double s, int c) {
        double a = s / 2;
        box(x - a, y - a, z - a, x + a, y + a, z + a, c);
    }

    public static void box(double x0, double y0, double z0, double x1, double y1, double z1, int c) {
        quad(x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, c);
        quad(x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, c);
        quad(x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, c);
        quad(x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, c);
        quad(x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, c);
        quad(x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, c);
    }

    /** 3D line as a camera facing ribbon. */
    public static void line(double x1, double y1, double z1, double x2, double y2, double z2, double width, int c1, int c2) {
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        double mx = (x1 + x2) / 2 - cx, my = (y1 + y2) / 2 - cy, mz = (z1 + z2) / 2 - cz;
        double sx = dy * mz - dz * my, sy = dz * mx - dx * mz, sz = dx * my - dy * mx;
        double l = Math.sqrt(sx * sx + sy * sy + sz * sz);
        if (l < 1e-9) return;
        double k = width / 2 / l;
        sx *= k; sy *= k; sz *= k;
        quad(x1 - sx, y1 - sy, z1 - sz, x1 + sx, y1 + sy, z1 + sz, x2 + sx, y2 + sy, z2 + sz, x2 - sx, y2 - sy, z2 - sz, c1, c1, c2, c2);
    }

    /** The 12 edges of a box. */
    public static void boxEdges(double x0, double y0, double z0, double x1, double y1, double z1, double w, int c) {
        double[][] p = {{x0, y0, z0}, {x1, y0, z0}, {x1, y0, z1}, {x0, y0, z1}, {x0, y1, z0}, {x1, y1, z0}, {x1, y1, z1}, {x0, y1, z1}};
        int[][] e = {{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
        for (int[] i : e) line(p[i[0]][0], p[i[0]][1], p[i[0]][2], p[i[1]][0], p[i[1]][1], p[i[1]][2], w, c, c);
    }

    // no-depth variants (render through walls)
    private static void noDepthQuad(double x1, double y1, double z1, double x2, double y2, double z2,
                            double x3, double y3, double z3, double x4, double y4, double z4, int c) {
        Batch vc = qNoDepth();
        v(vc, x1, y1, z1, c); v(vc, x2, y2, z2, c); v(vc, x3, y3, z3, c); v(vc, x4, y4, z4, c);
    }
    public static void noDepthBox(double x0, double y0, double z0, double x1, double y1, double z1, int c) {
        noDepthQuad(x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, c);
        noDepthQuad(x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, c);
        noDepthQuad(x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, c);
        noDepthQuad(x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, c);
        noDepthQuad(x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, c);
        noDepthQuad(x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, c);
    }
    private static void noDepthLine(double x1, double y1, double z1, double x2, double y2, double z2, double width, int c) {
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        double mx = (x1 + x2) / 2 - cx, my = (y1 + y2) / 2 - cy, mz = (z1 + z2) / 2 - cz;
        double sx = dy * mz - dz * my, sy = dz * mx - dx * mz, sz = dx * my - dy * mx;
        double l = Math.sqrt(sx * sx + sy * sy + sz * sz);
        if (l < 1e-9) return;
        double k = width / 2 / l; sx *= k; sy *= k; sz *= k;
        Batch vc = qNoDepth();
        v(vc, x1 - sx, y1 - sy, z1 - sz, c); v(vc, x1 + sx, y1 + sy, z1 + sz, c);
        v(vc, x2 + sx, y2 + sy, z2 + sz, c); v(vc, x2 - sx, y2 - sy, z2 - sz, c);
    }
    public static void noDepthBoxEdges(double x0, double y0, double z0, double x1, double y1, double z1, double w, int c) {
        double[][] p = {{x0, y0, z0}, {x1, y0, z0}, {x1, y0, z1}, {x0, y0, z1}, {x0, y1, z0}, {x1, y1, z0}, {x1, y1, z1}, {x0, y1, z1}};
        int[][] e = {{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
        for (int[] i : e) noDepthLine(p[i[0]][0], p[i[0]][1], p[i[0]][2], p[i[1]][0], p[i[1]][1], p[i[1]][2], w, c);
    }

    // ------------------------------------------------------------------ textured
    private static void tv(Batch vc, double x, double y, double z, float u, float v, int argb) {
        vc.v(x, y, z, argb, u, v, 0xF000F0, 0f, 1f, 0f);
    }

    /** Texture render modes for {@link #texQuad}. */
    public static final int CUTOUT = 0, TRANSLUCENT = 1, GLOW = 2;

    /** Lit textured quad in world coordinates (single winding; the layers are not culled). */
    public static void texQuad(class_2960 texture, int mode, double[] p, float[] uv, int argb, int light, float nx, float ny, float nz) {
        Batch vc = layer(mode == GLOW ? class_12249.method_76002(texture) : mode == TRANSLUCENT ? class_12249.method_76000(texture) : class_12249.method_75994(texture));
        int li = mode == GLOW ? 0xF000F0 : light;
        for (int k = 0; k < 4; k++) lv(vc, p, uv, k, argb, li, nx, ny, nz);
    }

    private static void lv(Batch vc, double[] p, float[] uv, int k, int argb, int light, float nx, float ny, float nz) {
        vc.v(p[k * 3], p[k * 3 + 1], p[k * 3 + 2], argb, uv[k * 2], uv[k * 2 + 1], light, nx, ny, nz);
    }

    private static Batch layer(class_1921 layer) { return batch(layer); }

    /** Camera-facing textured quad (u0..u1, v0..v1 texture region). */
    public static void billboard(class_2960 texture, double x, double y, double z, double w, double h, float u0, float v0, float u1, float v1, int argb) {
        Batch vc = layer(class_12249.method_76000(texture));  // RenderLayers.entityTranslucent(texture)
        double yaw = Math.toRadians(camYaw), pitch = Math.toRadians(camPitch);
        double rx = -Math.cos(yaw), rz = -Math.sin(yaw);
        double ux = -Math.sin(yaw) * Math.sin(pitch), uy = Math.cos(pitch), uz = Math.cos(yaw) * Math.sin(pitch);
        double hw = w / 2, hh = h / 2;
        double[][] p = {
                {x - rx * hw - ux * hh, y - uy * hh, z - rz * hw - uz * hh, u0, v1},
                {x + rx * hw - ux * hh, y - uy * hh, z + rz * hw - uz * hh, u1, v1},
                {x + rx * hw + ux * hh, y + uy * hh, z + rz * hw + uz * hh, u1, v0},
                {x - rx * hw + ux * hh, y + uy * hh, z - rz * hw + uz * hh, u0, v0}};
        double[][] order = {p[0], p[1], p[2], p[3], p[3], p[2], p[1], p[0]};
        for (double[] c : order) tv(vc, c[0], c[1], c[2], (float) c[3], (float) c[4], argb);
    }

    /** Camera-facing quad rotated by angle (radians) around the view axis. */
    public static void billboardRot(class_2960 texture, double x, double y, double z, double size, double angle, int argb) {
        Batch vc = layer(class_12249.method_76000(texture));
        double yaw = Math.toRadians(camYaw), pitch = Math.toRadians(camPitch);
        double rx = -Math.cos(yaw), rz = -Math.sin(yaw);
        double ux = -Math.sin(yaw) * Math.sin(pitch), uy = Math.cos(pitch), uz = Math.cos(yaw) * Math.sin(pitch);
        double c = Math.cos(angle), s = Math.sin(angle), h = size / 2;
        // rotated basis
        double ax = (rx * c + ux * s) * h, ay = (uy * s) * h, az = (rz * c + uz * s) * h;
        double bx = (-rx * s + ux * c) * h, by = (uy * c) * h, bz = (-rz * s + uz * c) * h;
        double[][] p = {
                {x - ax - bx, y - ay - by, z - az - bz, 0, 1},
                {x + ax - bx, y + ay - by, z + az - bz, 1, 1},
                {x + ax + bx, y + ay + by, z + az + bz, 1, 0},
                {x - ax + bx, y - ay + by, z - az + bz, 0, 0}};
        double[][] order = {p[0], p[1], p[2], p[3], p[3], p[2], p[1], p[0]};
        for (double[] q : order) tv(vc, q[0], q[1], q[2], (float) q[3], (float) q[4], argb);
    }

    /** Additive-looking glow billboard (emissive layer). */
    public static void glowBillboard(class_2960 texture, double x, double y, double z, double size, int argb) {
        Batch vc = layer(class_12249.method_76002(texture));
        double yaw = Math.toRadians(camYaw), pitch = Math.toRadians(camPitch);
        double rx = -Math.cos(yaw), rz = -Math.sin(yaw);
        double ux = -Math.sin(yaw) * Math.sin(pitch), uy = Math.cos(pitch), uz = Math.cos(yaw) * Math.sin(pitch);
        double h = size / 2;
        double[][] p = {
                {x - rx * h - ux * h, y - uy * h, z - rz * h - uz * h, 0, 1},
                {x + rx * h - ux * h, y - uy * h, z + rz * h - uz * h, 1, 1},
                {x + rx * h + ux * h, y + uy * h, z + rz * h + uz * h, 1, 0},
                {x - rx * h + ux * h, y + uy * h, z - rz * h + uz * h, 0, 0}};
        double[][] order = {p[0], p[1], p[2], p[3], p[3], p[2], p[1], p[0]};
        for (double[] q : order) tv(vc, q[0], q[1], q[2], (float) q[3], (float) q[4], argb);
    }

    public static void billboard(class_2960 texture, double x, double y, double z, double size, int argb) {
        billboard(texture, x, y, z, size, size, 0, 0, 1, 1, argb);
    }

    // ------------------------------------------------------------------ sky
    private static final Map<class_2960, class_1921> SKY = new HashMap<>();
    private static java.lang.reflect.Method layerOf;
    private static boolean skyBroken = false;

    /** Render layer for sky textures: no fog, translucent, no depth write (falls back to entity translucent). */
    public static class_1921 skyLayer(class_2960 tex) {
        class_1921 l = SKY.get(tex);
        if (l != null) return l;
        if (!skyBroken) {
            try {
                class_12247 setup = class_12247.method_75927(class_10799.field_56875).method_75934("Sampler0", tex).method_75937().method_75938();
                if (layerOf == null) {
                    layerOf = class_1921.class.getDeclaredMethod("method_75940", String.class, class_12247.class);
                    layerOf.setAccessible(true);
                }
                l = (class_1921) layerOf.invoke(null, "legoclient_sky", setup);
            } catch (Throwable t) {
                skyBroken = true;
                dev.lego.LegoClient.LOG("Sky-Layer nicht verfügbar, nutze Ersatz: " + t);
            }
        }
        if (l == null) l = class_12249.method_76000(tex);
        SKY.put(tex, l);
        return l;
    }

    /** Textured quad on a sky layer (world coordinates, both windings). */
    public static void skyQuad(class_1921 layer, double[] p0, double[] p1, double[] p2, double[] p3, float[] uv, int argb) {
        Batch vc = layer(layer);
        double[][] p = {p0, p1, p2, p3};
        int[] order = {0, 1, 2, 3, 3, 2, 1, 0};
        for (int i : order) tv(vc, p[i][0], p[i][1], p[i][2], uv[i * 2], uv[i * 2 + 1], argb);
    }

    /** Draws everything that was emitted (only for the immediate buffer source). */
    public static void end() {
        try {
            if (consumers != null) emit();
        } catch (Throwable t) {
            BATCH.clear(); lastLayer = null; lastBatch = null;
            dev.lego.LegoClient.LOG("R3: " + t);
        } finally {
            quads = null;
            used.clear();
            consumers = null;
            skyRadius = 0;
        }
    }

    /** Flushes what has been drawn so far (keeps begin() state), e.g. to draw the sky before everything else. */
    public static void flush() {
        if (consumers != null) emit();
        used.clear();
        quads = null;
    }

    public static int argb(int rgb, double alpha) {
        int a = (int) Math.max(0, Math.min(255, Math.round(alpha * 255)));
        return (a << 24) | (rgb & 0xFFFFFF);
    }
}
