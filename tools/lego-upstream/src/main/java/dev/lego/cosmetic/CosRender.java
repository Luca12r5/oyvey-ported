package dev.lego.cosmetic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import dev.lego.LegoClient;
import dev.lego.core.Mc;
import dev.lego.ui.Gx;
import dev.lego.ui.McBackend;
import dev.lego.ui.Tx;
import net.minecraft.class_10017;
import net.minecraft.class_10042;
import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1309;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4608;
import net.minecraft.class_591;
import net.minecraft.class_630;
import net.minecraft.class_746;

/** Minecraft side of the cosmetics: feature renderer for your own player, HD textures, cape texture, GUI preview. */
public final class CosRender {
    private CosRender() { }

    // ------------------------------------------------------------------ textures
    private static final Map<String, class_2960> TEX = new HashMap<>();
    /** Painter thread for textures that are not ready yet (never paint on the render thread). */
    private static final java.util.concurrent.ExecutorService PAINT = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Lego-Cosmetics");
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY - 1);
        return t;
    });
    private static final java.util.Set<String> QUEUED = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private static String idName(String name) {
        return name.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_./-]", "_");
    }

    /** Skin of your own player (set every frame by the feature renderer) for "@skin" (MiniMe). */
    static class_2960 skin;
    private static final Map<String, Integer> MODE = new HashMap<>();

    /** Render mode for a texture: glass / soft alpha -> translucent, everything else cutout (no sorting -> no flicker). */
    static int mode(String name) {
        Integer m = MODE.get(name);
        if (m != null) return m;
        if (name.equals("@skin")) { MODE.put(name, dev.lego.visual.R3.CUTOUT); return dev.lego.visual.R3.CUTOUT; }
        CTex.T t = CTex.peek(name);
        if (t == null) return dev.lego.visual.R3.CUTOUT;
        int soft = 0;
        for (int i = 0; i < t.argb.length; i += 3) { int a = t.argb[i] >>> 24; if (a > 12 && a < 235) soft++; }
        m = soft > t.argb.length / 3 * 0.04 ? dev.lego.visual.R3.TRANSLUCENT : dev.lego.visual.R3.CUTOUT;
        MODE.put(name, m);
        return m;
    }

    /** Registered texture id for a CTex name, or null while it is still being painted (render thread). */
    public static class_2960 texture(String name) {
        if (name.equals("@skin")) return skin;
        class_2960 id = TEX.get(name);
        if (id != null) return id;
        CTex.T t = CTex.peek(name);
        if (t == null) {
            if (QUEUED.add(name)) PAINT.execute(() -> { try { CTex.get(name); } catch (Throwable e) { LegoClient.LOG("Kosmetik-Textur " + name + ": " + e); } });
            return null;
        }
        QUEUED.remove(name);
        id = class_2960.method_60655("legoclient", "cosmetic/" + idName(name));
        try { Tx.register(id, t.argb, t.w, t.h, true); } catch (Throwable e) { LegoClient.LOG("Kosmetik-Textur " + name + ": " + e); }
        TEX.put(name, id);
        return id;
    }

    private static String capeBase;
    private static class_2960 lastCape;

    /** Texture of the equipped cape (current animation frame) or null. */
    public static class_2960 capeTexture() {
        Cos.Item c = Cos.equipped(Cos.Slot.CAPE);
        String base = c == null ? null : c.cape;
        if (!java.util.Objects.equals(base, capeBase)) {
            // free the frames of the previous cape
            if (capeBase != null) {
                java.util.Iterator<Map.Entry<String, class_2960>> it = TEX.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, class_2960> e = it.next();
                    if (e.getKey().equals(capeBase) || e.getKey().startsWith(capeBase + "#")) { Tx.destroy(e.getValue()); it.remove(); }
                }
            }
            capeBase = base;
            lastCape = null;
            if (base != null) for (int k = 0; k < Capes.frames(base); k++) texture(k == 0 ? base : base + "#" + k);   // queue all frames
        }
        if (base == null) return null;
        if (base.equals("cape_spotify") || base.equals("spotify")) {
            class_2960 live = SpotifyCape.texture();
            if (live != null) return live;
        }
        class_2960 t = texture(Capes.frameName(base, System.currentTimeMillis() / 1000f));
        if (t != null) lastCape = t;
        return lastCape;
    }

    private static int warmed = -1;

    /** Paints the textures of the equipped items in the background, so equipping never stutters. */
    public static void prewarm() {
        if (warmed == Cos.version) return;
        warmed = Cos.version;
        Thread t = new Thread(() -> {
            try {
                java.util.Set<String> names = new java.util.LinkedHashSet<>();
                G g = new G((tex, p, uv, argb, gl, nx, ny, nz) -> names.add(tex));
                Cos.A a = new Cos.A();
                Cos.renderEquipped(g, a, true);
                Cos.renderEquipped(g, a, false);
                Cos.Item c = Cos.equipped(Cos.Slot.CAPE);
                if (c != null && c.cape != null) names.add(c.cape);
                for (String n : names) CTex.get(n);
            } catch (Throwable e) {
                LegoClient.LOG("Kosmetik vorbereiten: " + e);
            }
        }, "Lego-Cosmetics");
        t.setDaemon(true);
        t.start();
    }

    // ------------------------------------------------------------------ feature renderer
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static class_3887 feature(class_3883 ctx) { return new Feature(ctx); }

    static final class Feature extends class_3887<class_10055, class_591> {
        Feature(class_3883<class_10055, class_591> ctx) { super(ctx); }

        @Override
        public void method_4199(class_4587 matrices, class_11659 queue, int light, class_10055 state, float a1, float a2) {
            dev.lego.perf.Perf.begin("Kosmetik");
            try {
                class_746 me = Mc.player();
                if (me == null) return;
                if (state.field_53528 != Mc.id(me)) { drawOther(matrices, queue, light, state, method_17165()); return; }
                if (!Cos.anyEquipped()) return;
                if (me.method_5767()) return;       // invisible
                Cos.A anim = new Cos.A();
                anim.time = state.field_53328 / 20f;
                anim.sneak = state.field_53410;
                anim.move = Math.min(1f, state.field_53451 * 1.4f);
                try { skin = state.field_53520.comp_1626().comp_3627(); } catch (Throwable ignored) { }
                final boolean helmet = !Mc.empty(Mc.armor(39)), chest = !Mc.empty(Mc.armor(38)), legs = !Mc.empty(Mc.armor(37));
                Cos.slotTransform = (g, slot) -> {
                    switch (slot) {
                        case HAT: if (helmet) { g.translate(0, 0.9f, 0); g.scale(1.1f); } break;
                        case FACE: if (helmet) g.translate(0, 0, 0.9f); break;
                        case BACK: case WINGS: if (chest) g.translate(0, 0, -1.1f); break;
                        case AURA: if (chest || legs) g.scale(1.08f, 1, 1.08f); break;
                        default:
                    }
                };
                flagsKey = (helmet ? 1 : 0) | (chest ? 2 : 0) | (legs ? 4 : 0);
                class_591 model = method_17165();
                draw(matrices, queue, light, model.field_3391, anim, false);
                draw(matrices, queue, light, model.field_3398, anim, true);
            } catch (Throwable t) {
                if (LegoClient.DEBUG) LegoClient.LOG("Kosmetik-Render: " + t);
            } finally {
                dev.lego.perf.Perf.end("Kosmetik");
            }
        }
    }

    /** Cosmetics of another Lego user (Lego-Sync), drawn on their model. */
    private static void drawOther(class_4587 matrices, class_11659 queue, int light, class_10055 state, class_591 model) {
        if (Mc.world() == null) return;
        net.minecraft.class_1297 other = Mc.world().method_8469(state.field_53528);
        if (other == null) return;
        java.util.List<String> ids = dev.lego.net.LegoSync.cosmetics(other.method_5667());
        if (ids.isEmpty()) return;
        Cos.A anim = new Cos.A();
        anim.time = state.field_53328 / 20f;
        anim.sneak = state.field_53410;
        anim.move = Math.min(1f, state.field_53451 * 1.4f);
        for (int pass = 0; pass < 2; pass++) {
            final boolean head = pass == 1;
            Map<String, List<Q>> normal = new LinkedHashMap<>(), glow = new LinkedHashMap<>();
            G g = new G((tex, p, uv, argb, gl, nx, ny, nz) -> (gl ? glow : normal).computeIfAbsent(tex, k -> new ArrayList<>()).add(new Q(p, uv, argb, nx, ny, nz)));
            Cos.renderList(g, anim, head, ids);
            if (normal.isEmpty() && glow.isEmpty()) continue;
            matrices.method_22903();
            (head ? model.field_3398 : model.field_3391).method_22703(matrices);
            matrices.method_22905(-1 / 16f, -1 / 16f, -1 / 16f);
            submit(matrices, queue, normal, false, light);
            submit(matrices, queue, glow, true, light);
            matrices.method_22909();
        }
    }

    private static final class Q {
        final float[] p, uv, n;
        /** smoothed vertex normals (12 floats) or null = face normal. */
        float[] vn;
        final int argb;
        Q(float[] p, float[] uv, int argb, float nx, float ny, float nz) { this.p = p; this.uv = uv; this.argb = argb; this.n = new float[]{nx, ny, nz}; }
    }

    /** Built geometry is reused for ~33 ms: the quads do not depend on the camera or the body pose, so rebuilding + smoothing them every frame only costs FPS. */
    private static final class Built {
        long t; int ver, mq, flags; boolean sneak;
        Map<String, List<Q>> normal, glow;
    }
    private static final Built[] BUILT = new Built[2];
    private static int flagsKey;

    private static void draw(class_4587 matrices, class_11659 queue, int light, class_630 part, Cos.A anim, boolean head) {
        long now = System.currentTimeMillis();
        int mq = Math.round(anim.move * 10);
        Built b = BUILT[head ? 1 : 0];
        if (b == null || now - b.t > 33 || b.ver != Cos.version || b.sneak != anim.sneak || b.mq != mq || b.flags != flagsKey) {
            Map<String, List<Q>> nrm = new LinkedHashMap<>(), glw = new LinkedHashMap<>();
            G g = new G((tex, p, uv, argb, gl, nx, ny, nz) -> (gl ? glw : nrm).computeIfAbsent(tex, k -> new ArrayList<>()).add(new Q(p, uv, argb, nx, ny, nz)));
            try { Cos.renderEquipped(g, anim, head); } finally { Cos.slotTransform = head ? null : Cos.slotTransform; }
            smooth(nrm);
            b = new Built();
            b.t = now; b.ver = Cos.version; b.mq = mq; b.flags = flagsKey; b.sneak = anim.sneak; b.normal = nrm; b.glow = glw;
            BUILT[head ? 1 : 0] = b;
        }
        Map<String, List<Q>> normal = b.normal, glow = b.glow;
        if (normal.isEmpty() && glow.isEmpty()) return;
        matrices.method_22903();
        part.method_22703(matrices);
        // cosmetic space (px, x right / y up / z forward) -> model space (1/16 block, x left / y down / z back)
        matrices.method_22905(-1 / 16f, -1 / 16f, -1 / 16f);
        submit(matrices, queue, normal, false, light);
        submit(matrices, queue, glow, true, light);
        matrices.method_22909();
    }

    /** Smooth shading: per vertex normals averaged over neighbouring quads of the same texture (see {@link Smooth}). */
    private static void smooth(Map<String, List<Q>> quads) {
        int n = 0;
        for (List<Q> l : quads.values()) n += l.size();
        if (n == 0 || n > 20000) return;
        float[][] P = new float[n][], N = new float[n][];
        int[] grp = new int[n];
        Q[] all = new Q[n];
        int i = 0;
        for (Map.Entry<String, List<Q>> e : quads.entrySet()) {
            int h = Smooth.group(e.getKey(), false);
            for (Q q : e.getValue()) { all[i] = q; P[i] = q.p; N[i] = q.n; grp[i] = h; i++; }
        }
        float[] vn = Smooth.normals(P, N, grp, n);
        for (int k = 0; k < n; k++) all[k].vn = java.util.Arrays.copyOfRange(vn, k * 12, k * 12 + 12);
    }

    private static void submit(class_4587 matrices, class_11659 queue, Map<String, List<Q>> quads, boolean glow, int light) {
        for (Map.Entry<String, List<Q>> e : quads.entrySet()) {
            class_2960 tex = texture(e.getKey());
            if (tex == null) continue;
            int md = glow ? dev.lego.visual.R3.GLOW : mode(e.getKey());
            class_1921 layer = md == dev.lego.visual.R3.GLOW ? class_12249.method_76002(tex) : md == dev.lego.visual.R3.TRANSLUCENT ? class_12249.method_76000(tex) : class_12249.method_75994(tex);
            final List<Q> list = e.getValue();
            final int li = glow ? 0xF000F0 : light;
            queue.method_73483(matrices, layer, (entry, vc) -> {
                for (Q q : list) for (int k = 0; k < 4; k++) v(vc, entry, q, k, li);   // single winding (layers are not culled -> no z-fighting)
            });
        }
    }

    private static void v(class_4588 vc, class_4587.class_4665 entry, Q q, int k, int light) {
        vc.method_56824(entry, q.p[k * 3], q.p[k * 3 + 1], q.p[k * 3 + 2])
                .method_39415(q.argb)
                .method_22913(q.uv[k * 2], q.uv[k * 2 + 1])
                .method_22922(class_4608.field_21444)
                .method_60803(light)
                .method_60831(entry, q.vn != null ? q.vn[k * 3] : q.n[0], q.vn != null ? q.vn[k * 3 + 1] : q.n[1], q.vn != null ? q.vn[k * 3 + 2] : q.n[2]);
    }

    // ------------------------------------------------------------------ models in world space (pets, vehicles)
    /**
     * Draws a cosmetic-space model into the world (inside R3.begin/end).
     * Model space: px, x = right, y = up, z = forward; placed at (x,y,z) facing Minecraft yaw, scaled.
     */
    public static void drawWorld(Cos.Model model, Cos.A a, double x, double y, double z, float yawDeg, float pitchDeg, float rollDeg, float scale, int light) {
        final double th = Math.toRadians(yawDeg);
        final double s = scale / 16.0;
        final double fx = -Math.sin(th), fz = Math.cos(th);        // forward
        final double rx = -Math.cos(th), rz = -Math.sin(th);       // right
        final double cp = Math.cos(Math.toRadians(pitchDeg)), sp = Math.sin(Math.toRadians(pitchDeg));
        final double cr = Math.cos(Math.toRadians(rollDeg)), sr = Math.sin(Math.toRadians(rollDeg));
        final Map<String, List<Object[]>> groups = new LinkedHashMap<>();
        G g = new G((tex, p, uv, argb, glow, nx, ny, nz) -> {
            double[] w = new double[12];
            for (int k = 0; k < 4; k++) {
                double[] q = local(p[k * 3], p[k * 3 + 1], p[k * 3 + 2], cp, sp, cr, sr);
                w[k * 3] = x + (q[0] * rx + q[2] * fx) * s;
                w[k * 3 + 1] = y + q[1] * s;
                w[k * 3 + 2] = z + (q[0] * rz + q[2] * fz) * s;
            }
            double[] n = local(nx, ny, nz, cp, sp, cr, sr);
            float[] wn = {(float) (n[0] * rx + n[2] * fx), (float) n[1], (float) (n[0] * rz + n[2] * fz)};
            groups.computeIfAbsent((glow ? "!" : "") + tex, k -> new ArrayList<>()).add(new Object[]{w, uv, argb, wn});
        });
        try { model.render(g, a); } catch (Throwable t) { if (LegoClient.DEBUG) LegoClient.LOG("Modell: " + t); }
        // one batch per texture (few layer switches = fast)
        for (Map.Entry<String, List<Object[]>> e : groups.entrySet()) {
            boolean glow = e.getKey().startsWith("!");
            String name = glow ? e.getKey().substring(1) : e.getKey();
            class_2960 id = texture(name);
            if (id == null) continue;
            int md = glow ? dev.lego.visual.R3.GLOW : mode(name);
            for (Object[] q : e.getValue()) {
                float[] wn = (float[]) q[3];
                dev.lego.visual.R3.texQuad(id, md, (double[]) q[0], (float[]) q[1], (Integer) q[2], light, wn[0], wn[1], wn[2]);
            }
        }
    }

    /** Pitch (nose down = positive) around x, then roll around z. */
    private static double[] local(double x, double y, double z, double cp, double sp, double cr, double sr) {
        double y1 = y * cp + z * sp, z1 = -y * sp + z * cp;
        double x2 = x * cr - y1 * sr, y2 = x * sr + y1 * cr;
        return new double[]{x2, y2, z1};
    }

    /** Packed light at a block position. */
    public static int light(double x, double y, double z) {
        try {
            return net.minecraft.class_761.method_23794(Mc.world(), net.minecraft.class_2338.method_49637(x, y, z));
        } catch (Throwable t) { return 0xF000F0; }
    }

    // ------------------------------------------------------------------ GUI preview of your own player
    private static java.lang.reflect.Method stateOf;

    /** Draws your player into a pixel rectangle (Gx must be active on McBackend). */
    public static boolean preview(int x, int y, int w, int h, float yaw, float pitch) {
        class_746 p = Mc.player();
        if (p == null || !(Gx.B instanceof McBackend)) return false;
        try {
            if (stateOf == null) {
                stateOf = net.minecraft.class_490.class.getDeclaredMethod("method_48472", class_1309.class);
                stateOf.setAccessible(true);
            }
            class_10017 st = (class_10017) stateOf.invoke(null, p);
            if (st instanceof class_10042) {
                class_10042 ls = (class_10042) st;
                ls.field_53446 = 180 + yaw;
                ls.field_53447 = 0;
                ls.field_53448 = pitch;
            }
            class_332 ctx = ((McBackend) Gx.B).ctx();
            int S = Gx.S;
            int x1 = x / S, y1 = y / S, x2 = (x + w) / S, y2 = (y + h) / S;
            float size = Math.min((x2 - x1) * 0.9f, (y2 - y1) / Math.max(1.2f, st.field_53330 * 1.15f)) ;
            Gx.push();
            Gx.B.scale(S, S);
            Quaternionf rot = new Quaternionf().rotateZ((float) Math.PI);
            Quaternionf cam = new Quaternionf();
            ctx.method_70856(st, size, new Vector3f(0, st.field_53330 / 2f + 0.0625f, 0), rot, cam, x1, y1, x2, y2);
            Gx.pop();
            return true;
        } catch (Throwable t) {
            if (LegoClient.DEBUG) LegoClient.LOG("Vorschau: " + t);
            return false;
        }
    }
}
