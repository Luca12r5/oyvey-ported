package dev.lego.visual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.visual.sky.CustomSky;
import dev.lego.visual.sky.SkyExtras;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.class_1293;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_6880;
import net.minecraft.class_746;

/** World visuals: sky, color filter, jump circles, crystals, totem, particles, fullbright, zoom, crosshair ... */
public final class Visuals {
    private Visuals() { }

    public static CustomSky sky;
    public static ColorFilter filter;
    public static JumpCircles jumpCircles;
    public static Crystals crystals;
    public static TotemEffect totem;
    public static HitParticles hit;
    public static Trails trails;
    public static KillEffect kill;
    public static Fullbright fullbright;
    public static Zoom zoom;
    public static NoHurtCam noHurtCam;
    public static TimeChanger time;
    public static ClearWeather weather;
    public static Crosshair crosshair;
    public static CustomFov customFov;
    public static GlintColor glint;

    static final String[] PARTICLE_NAMES = {"Kritisch", "Magie", "Endstab", "Totem", "Herzen", "Flammen", "Seelenfeuer", "Feuerwerk", "Funken", "Glühen", "Schnee", "Noten", "Portal", "Kirschblüten"};
    static final String[] PARTICLE_IDS = {"crit", "enchanted_hit", "end_rod", "totem_of_undying", "heart", "flame", "soul_fire_flame", "firework", "electric_spark", "glow", "snowflake", "note", "portal", "cherry_leaves"};

    static String particle(Setting.Mode m) { return PARTICLE_IDS[Math.max(0, Math.min(PARTICLE_IDS.length - 1, m.index))]; }

    public static void registerAll() {
        sky = Modules.register(new CustomSky());
        SkyExtras.registerAll();
        filter = Modules.register(new ColorFilter());
        jumpCircles = Modules.register(new JumpCircles());
        crystals = Modules.register(new Crystals());
        totem = Modules.register(new TotemEffect());
        hit = Modules.register(new HitParticles());
        trails = Modules.register(new Trails());
        kill = Modules.register(new KillEffect());
        fullbright = Modules.register(new Fullbright());
        zoom = Modules.register(new Zoom());
        noHurtCam = Modules.register(new NoHurtCam());
        time = Modules.register(new TimeChanger());
        weather = Modules.register(new ClearWeather());
        crosshair = Modules.register(new Crosshair());
        customFov = Modules.register(new CustomFov());
        glint = Modules.register(new GlintColor());
        Visuals2.registerAll();
        NameFx.registerAll();
        Ambient.registerAll();
        WorldFx.registerAll();
        WorldEffects.register();
        Visuals3.registerAll();
    }

    /** Called from SkyRenderingMixin inside the vanilla sky pass. @return true = vanilla sky is replaced */
    /**
     * Sky pass hook (vanilla sky rendering). The custom sky is now drawn in the normal world pass instead
     * (see renderSky) - that path is the same one all other world visuals use and works reliably;
     * the vanilla sky simply stays underneath.
     */
    public static boolean renderSkyPass() { return false; }

    private static void renderSky() {
        boolean person = SkyExtras.person != null && SkyExtras.person.enabled;
        if ((sky == null || !sky.enabled) && !person) return;
        if (sky != null && sky.enabled) sky.render();
        if (person) SkyExtras.person.render();
        R3.flush();
    }

    @SuppressWarnings("unused")
    private static boolean renderSkyPassOld() {
        if (!Mc.inGame()) return false;
        boolean person = SkyExtras.person != null && SkyExtras.person.enabled;
        if ((sky == null || !sky.enabled) && !person) return false;
        boolean replace = sky != null && sky.replacesVanilla();
        dev.lego.perf.Perf.begin("Himmel");
        try {
            R3.beginSky(Mc.mc().method_22940().method_23000());
            if (sky.enabled) sky.render();
            if (person) SkyExtras.person.render();
        } catch (Throwable t) {
            LegoClient.LOG("Sky: " + t);
        } finally {
            R3.end();
            dev.lego.perf.Perf.end("Himmel");
        }
        return replace;
    }

    public static boolean skyReplaced() { return false; }

    /** Called from WorldRenderEvents.BEFORE_TRANSLUCENT. */
    public static void render(WorldRenderContext ctx) {
        if (!Mc.inGame()) return;
        if (!R3.begin(ctx)) return;
        try {
            float td = Mc.tickDelta();
            sec("Himmel", Visuals::renderSky);
            sec("Effekte", () -> {
                if (jumpCircles.enabled) jumpCircles.render(td);
                if (crystals.enabled) crystals.render(td);
                if (totem.enabled) totem.render();
                Visuals2.render(td);
                Visuals3.render(td);
            });
            boolean ext = (dev.lego.perf.Performance.boost != null && dev.lego.perf.Performance.boost.extreme()) || dev.lego.perf.Performance.lowMode();
            if (!ext) sec("Partikel", Ambient::render);
            if (!ext) sec("Weltpartikel", WorldFx::render);
            sec("Wegpunkte", () -> dev.lego.util.Utility.renderWorld(td));
            sec("Fake-Check", () -> { if (dev.lego.util.Utility.fakeCheck != null) dev.lego.util.Utility.fakeCheck.renderWorld(td); });
            sec("Namensschild", () -> NameFx.render(td));
            sec("Haustier", () -> dev.lego.cosmetic.PetWorld.render(td));
            sec("Fahrzeug", () -> dev.lego.cosmetic.VehicleWorld.render(td));
            sec("Emotes", () -> { dev.lego.emote.Emotes.renderBubble(td); dev.lego.emote.Cutscenes.renderFx(td); });
            sec("Chunk-Grenzen", Visuals::renderChunkBorders);
        } catch (Throwable t) {
            LegoClient.LOG("Render-Fehler: " + t);
        } finally {
            dev.lego.perf.Perf.begin("Welt-Zeichnen");
            R3.end();
            dev.lego.perf.Perf.end("Welt-Zeichnen");
        }
    }

    /** name -> {errors in the current window, window start ms, disabled until ms}: a section that keeps failing is paused instead of spamming the log every frame. */
    private static final Map<String, long[]> BREAKER = new HashMap<>();

    private static void sec(String name, Runnable r) {
        long now = System.currentTimeMillis();
        long[] b = BREAKER.get(name);
        if (b != null && now < b[2]) return;
        dev.lego.perf.Perf.begin(name);
        try { r.run(); } catch (Throwable t) {
            if (b == null) { b = new long[3]; BREAKER.put(name, b); }
            if (now - b[1] > 10_000) { b[0] = 0; b[1] = now; }
            if (++b[0] <= 3) LegoClient.LOG(name + ": " + t);
            if (b[0] >= 5) { b[2] = now + 60_000; b[0] = 0; LegoClient.LOG(name + " wird 60 s pausiert (wiederholte Fehler)"); }
        } finally { dev.lego.perf.Perf.end(name); }
    }

    // ================================================================== color filter (post effect)
    public static final class ColorFilter extends Module {
        public static final String[] MODES = {"Kräftig", "Extrem", "Pastell", "Schwarz-Weiß", "Warm", "Kalt", "Vintage", "Lila", "Kino", "Dramatisch", "Traum", "Matrix"};
        public static final String[] IDS = {"vivid", "extreme", "pastel", "grayscale", "warm", "cold", "vintage", "purple", "cinematic", "dramatic", "dream", "matrix"};
        final Setting.Mode mode = add(new Setting.Mode("Filter", 0, MODES));

        ColorFilter() { super("colorfilter", "Farbsättigung & Filter", "Mehr Farbe, Kino-Look, Schwarz-Weiß, Warm, Kalt, Vintage ...", Category.VISUALS); icon("palette"); }

        @Override public void tick() {
            class_2960 want = class_2960.method_60655("legoclient", "filter_" + IDS[Math.max(0, Math.min(IDS.length - 1, mode.index))]);
            try {
                net.minecraft.class_757 gr = Mc.mc().field_1773;
                class_2960 cur = gr.method_62906();
                if (!want.equals(cur)) setPost(gr, want);
            } catch (Throwable t) {
                LegoClient.LOG("Filter-Fehler: " + t);
                setEnabled(false);
            }
        }

        private static java.lang.reflect.Method setPost;
        /** GameRenderer.setPostProcessor is private -> reflection (intermediary name at runtime). */
        private static void setPost(net.minecraft.class_757 gr, class_2960 id) throws Exception {
            if (setPost == null) {
                setPost = net.minecraft.class_757.class.getDeclaredMethod("method_62904", class_2960.class);
                setPost.setAccessible(true);
            }
            setPost.invoke(gr, id);
        }

        @Override public void onDisable() {
            try {
                net.minecraft.class_757 gr = Mc.mc().field_1773;
                class_2960 cur = gr.method_62906();
                if (cur != null && "legoclient".equals(cur.method_12836())) gr.method_62905();
            } catch (Throwable ignored) { }
        }
    }

    // ================================================================== jump circles
    public static final class JumpCircles extends Module {
        final Setting.Color color = add(new Setting.Color("Farbe", 11));
        final Setting.Num size = add(new Setting.Num("Größe", 0.5, 2.5, 0.1, 1.2, "x"));
        final Setting.Mode style = add(new Setting.Mode("Stil", 0, "Glow-Ring", "Doppelring", "Welle", "Nova", "Spirale", "Funkenregen", "Säule", "Pulsar", "Stern", "Blitze", "Aufsteigend", "Regenbogen"));
        final Setting.Bool everyone = add(new Setting.Bool("Alle Spieler", false));
        private final List<double[]> circles = new ArrayList<>();   // x, y, z, startMs
        private final Map<Integer, Boolean> ground = new HashMap<>();

        JumpCircles() { super("jumpcircles", "Jump Circles", "12 Effekte beim Springen: Ringe, Nova, Spirale, Funken, Blitze ...", Category.VISUALS); icon("jump"); }

        @Override public void tick() {
            class_746 me = Mc.player();
            if (me == null) return;
            List<class_1657> list = everyone.get() ? Mc.players() : List.of(me);
            for (class_1657 p : list) {
                int id = Mc.id(p);
                boolean og = Mc.onGround(p);
                Boolean was = ground.put(id, og);
                if (was != null && was && !og && Mc.velocity(p)[1] > 0.2) { if (circles.size() > 24) circles.remove(0); circles.add(new double[]{Mc.x(p), Mc.y(p) + 0.03, Mc.z(p), System.currentTimeMillis()}); }
            }
            if (ground.size() > 200) ground.clear();
        }

        private void ringFlat(double[] c, double r, double w, int rgb, double alpha, double rot) {
            int seg = 48;
            for (int i = 0; i < seg; i++) {
                double a0 = rot + Math.PI * 2 * i / seg, a1 = rot + Math.PI * 2 * (i + 1) / seg;
                int col = color.argb(i / (double) seg) & 0xFFFFFF;
                if (!color.rainbow()) col = rgb;
                int ci = R3.argb(col, 0), co = R3.argb(col, alpha);
                double c0 = Math.cos(a0), s0 = Math.sin(a0), c1 = Math.cos(a1), s1 = Math.sin(a1);
                R3.quad(c[0] + c0 * (r - w * 3), c[1], c[2] + s0 * (r - w * 3), c[0] + c0 * r, c[1], c[2] + s0 * r,
                        c[0] + c1 * r, c[1], c[2] + s1 * r, c[0] + c1 * (r - w * 3), c[1], c[2] + s1 * (r - w * 3), ci, co, co, ci);
                R3.quad(c[0] + c0 * r, c[1], c[2] + s0 * r, c[0] + c0 * (r + w), c[1], c[2] + s0 * (r + w),
                        c[0] + c1 * (r + w), c[1], c[2] + s1 * (r + w), c[0] + c1 * r, c[1], c[2] + s1 * r, co, R3.argb(col, 0), R3.argb(col, 0), co);
            }
        }

        private static double ease(double t) { return 1 - Math.pow(1 - Math.max(0, Math.min(1, t)), 3); }

        void render(float td) {
            long now = System.currentTimeMillis();
            double sz = size.get();
            Iterator<double[]> it = circles.iterator();
            while (it.hasNext()) {
                double[] c = it.next();
                double t = (now - c[3]) / 1000.0;
                if (t >= 1.2) { it.remove(); continue; }
                double tn = Math.min(1, t);
                double fade = 1 - tn;
                int rgb = color.argb((c[3] % 1000) / 1000.0) & 0xFFFFFF;
                double w = 0.09 * sz;
                java.util.Random rnd = new java.util.Random((long) c[3]);
                switch (style.index) {
                    case 0: // Glow-Ring
                        ringFlat(c, sz * (0.25 + 0.75 * ease(tn)), w, rgb, 0.85 * fade, 0);
                        break;
                    case 1: // Doppelring
                        for (int k = 0; k < 2; k++) {
                            double tk = Math.max(0, Math.min(1, tn * (1 + k * 0.25) - k * 0.12));
                            if (tk > 0) ringFlat(c, sz * (0.25 + 0.75 * ease(tk)) * (1 - k * 0.18), w, rgb, 0.85 * (1 - tk), 0);
                        }
                        break;
                    case 2: // Welle: drei Ringe hintereinander
                        for (int k = 0; k < 3; k++) {
                            double tk = Math.max(0, Math.min(1, tn * 1.3 - k * 0.18));
                            if (tk > 0) ringFlat(c, sz * (0.2 + 0.9 * ease(tk)), w * 0.9, rgb, 0.8 * (1 - tk), 0);
                        }
                        break;
                    case 3: { // Nova: heller Blitz in der Mitte + schneller Ring
                        double e = ease(tn * 1.4);
                        ringFlat(c, sz * (0.15 + 1.2 * e), w * 1.6, rgb, 0.95 * fade * fade, 0);
                        double fr = sz * 0.5 * (1 - tn);
                        int core = R3.argb(0xFFFFFF, 0.8 * fade);
                        R3.quad(c[0] - fr, c[1] + 0.01, c[2] - fr, c[0] + fr, c[1] + 0.01, c[2] - fr, c[0] + fr, c[1] + 0.01, c[2] + fr, c[0] - fr, c[1] + 0.01, c[2] + fr, core);
                        break;
                    }
                    case 4: { // Spirale: zwei Arme drehen sich nach außen
                        for (int arm = 0; arm < 2; arm++) {
                            for (int i = 0; i < 40; i++) {
                                double f = i / 39.0;
                                double ang = arm * Math.PI + f * 5 + t * 6;
                                double r = sz * (0.15 + 1.0 * f) * ease(tn * 1.2);
                                double a = Math.max(0, 1 - tn) * (1 - f * 0.6);
                                R3.cube(c[0] + Math.cos(ang) * r, c[1] + 0.04, c[2] + Math.sin(ang) * r, 0.1 * sz, R3.argb(rgb, a));
                            }
                        }
                        break;
                    }
                    case 5: { // Funkenregen: Funken fliegen nach außen und fallen
                        for (int i = 0; i < 28; i++) {
                            double ang = rnd.nextDouble() * Math.PI * 2, sp = (0.5 + rnd.nextDouble()) * sz, up = 1.2 + rnd.nextDouble() * 1.4;
                            double x = c[0] + Math.cos(ang) * sp * tn, z = c[2] + Math.sin(ang) * sp * tn;
                            double y = c[1] + up * t - 3.2 * t * t;
                            if (y < c[1]) continue;
                            R3.cube(x, y, z, 0.07 * sz + 0.03, R3.argb(rgb, fade));
                        }
                        break;
                    }
                    case 6: { // Säule: Licht steigt auf
                        double r = sz * 0.55, h = 0.4 + 2.2 * ease(tn);
                        int seg = 24;
                        for (int i = 0; i < seg; i++) {
                            double a0 = Math.PI * 2 * i / seg, a1 = Math.PI * 2 * (i + 1) / seg;
                            R3.quad(c[0] + Math.cos(a0) * r, c[1], c[2] + Math.sin(a0) * r, c[0] + Math.cos(a1) * r, c[1], c[2] + Math.sin(a1) * r,
                                    c[0] + Math.cos(a1) * r * 0.7, c[1] + h, c[2] + Math.sin(a1) * r * 0.7, c[0] + Math.cos(a0) * r * 0.7, c[1] + h, c[2] + Math.sin(a0) * r * 0.7,
                                    R3.argb(rgb, 0.45 * fade), R3.argb(rgb, 0.45 * fade), R3.argb(rgb, 0), R3.argb(rgb, 0));
                        }
                        ringFlat(c, r, w, rgb, 0.8 * fade, 0);
                        break;
                    }
                    case 7: // Pulsar: vier dichte Ringe
                        for (int k = 0; k < 4; k++) {
                            double tk = Math.max(0, Math.min(1, tn - k * 0.09));
                            if (tk > 0) ringFlat(c, sz * (0.2 + 0.8 * ease(tk)), w * 0.6, rgb, 0.9 * (1 - tk), 0);
                        }
                        break;
                    case 8: { // Stern: rotierende Strahlen
                        int spokes = 8;
                        double len = sz * 1.1 * ease(tn * 1.3), rot = t * 3;
                        for (int i = 0; i < spokes; i++) {
                            double a = rot + Math.PI * 2 * i / spokes;
                            R3.line(c[0], c[1] + 0.05, c[2], c[0] + Math.cos(a) * len, c[1] + 0.05, c[2] + Math.sin(a) * len, 0.07 * sz,
                                    R3.argb(rgb, 0.9 * fade), R3.argb(rgb, 0));
                        }
                        ringFlat(c, len * 0.35, w, rgb, 0.7 * fade, rot);
                        break;
                    }
                    case 9: { // Blitze: Zickzack-Linien nach außen
                        for (int i = 0; i < 6; i++) {
                            double a = rnd.nextDouble() * Math.PI * 2, px = c[0], pz = c[2];
                            int segs = 5;
                            for (int s = 1; s <= segs; s++) {
                                double rr = sz * 1.2 * ease(tn * 1.5) * s / segs;
                                double jit = (rnd.nextDouble() - 0.5) * 0.5;
                                double nx = c[0] + Math.cos(a + jit) * rr, nz = c[2] + Math.sin(a + jit) * rr;
                                R3.line(px, c[1] + 0.06, pz, nx, c[1] + 0.06, nz, 0.05 * sz, R3.argb(0xFFFFFF, 0.9 * fade), R3.argb(rgb, 0.9 * fade));
                                px = nx; pz = nz;
                            }
                        }
                        break;
                    }
                    case 10: // Aufsteigende Ringe
                        for (int k = 0; k < 4; k++) {
                            double tk = Math.max(0, Math.min(1, tn * 1.2 - k * 0.12));
                            if (tk <= 0) continue;
                            double[] cc = {c[0], c[1] + tk * 1.8, c[2]};
                            ringFlat(cc, sz * 0.7 * (1 - 0.3 * tk), w * 0.8, rgb, 0.85 * (1 - tk), t * 2 + k);
                        }
                        break;
                    default: { // Regenbogen-Explosion
                        for (int k = 0; k < 4; k++) {
                            double tk = Math.max(0, Math.min(1, tn * 1.2 - k * 0.08));
                            if (tk <= 0) continue;
                            int col = java.awt.Color.HSBtoRGB((float) ((k * 0.17 + t) % 1.0), 0.75f, 1f) & 0xFFFFFF;
                            ringFlat(c, sz * (0.2 + 1.0 * ease(tk)), w, col, 0.85 * (1 - tk), 0);
                        }
                        for (int i = 0; i < 16; i++) {
                            double ang = rnd.nextDouble() * Math.PI * 2, sp = (0.4 + rnd.nextDouble() * 0.9) * sz;
                            int col = java.awt.Color.HSBtoRGB((float) rnd.nextDouble(), 0.7f, 1f) & 0xFFFFFF;
                            R3.cube(c[0] + Math.cos(ang) * sp * tn, c[1] + 0.8 * t - 1.6 * t * t + 0.1, c[2] + Math.sin(ang) * sp * tn, 0.08 * sz + 0.03, R3.argb(col, fade));
                        }
                    }
                }
            }
        }
    }
    // ================================================================== end crystals
    public static final class Crystals extends Module {
        final Setting.Color color = add(new Setting.Color("Farbe", 5));
        final Setting.Num glowSize = add(new Setting.Num("Aura-Größe", 0.3, 2, 0.1, 0.8, "x"));
        final Setting.Num pulse = add(new Setting.Num("Pulsieren", 0, 2, 0.1, 1, "x"));
        final Setting.Bool beam = add(new Setting.Bool("Lichtstrahl", true));
        final Setting.Num beamHeight = add(new Setting.Num("Strahl-Höhe", 2, 20, 1, 5, ""));
        final Setting.Bool groundGlow = add(new Setting.Bool("Boden-Glow", true));
        final Setting.Bool orbitRings = add(new Setting.Bool("Orbit-Ringe", false));
        final Setting.Bool edges = add(new Setting.Bool("Würfel-Kanten", false));
        final Setting.Color color2 = add(new Setting.Color("Farbe 2 (Verlauf)", 7));
        final Setting.Bool gradient = add(new Setting.Bool("Verlauf benutzen", false));

        Crystals() { super("crystals", "Custom Kristalle", "Farbige Aura, Glow, Orbit-Ringe und Lichtstrahlen um End-Kristalle", Category.VISUALS); icon("gem"); }

        void render(float td) {
            double t = System.currentTimeMillis() / 1000.0;
            for (class_1297 e : Mc.entities()) {
                if (!Mc.isEndCrystal(e)) continue;
                double[] p = Mc.lerpPos(e, td);
                double dx = p[0] - R3.camX(), dz = p[2] - R3.camZ();
                if (dx * dx + dz * dz > 64 * 64) continue;
                double cx = p[0], cy = p[1] + 1.0, cz = p[2];
                double phase = Mc.id(e) * 0.13;
                int rgb = color.argb(phase) & 0xFFFFFF;
                int rgb2 = gradient.get() ? (color2.argb(phase + 0.3) & 0xFFFFFF) : rgb;
                double s = glowSize.get();
                double pv = pulse.get() > 0 ? 0.7 + 0.3 * Math.sin(t * 2.5 * pulse.get()) : 1;
                double r = 0.55 * s * pv;
                R3.box(cx - r, cy - r, cz - r, cx + r, cy + r, cz + r, R3.argb(rgb, 0.18 * pv));
                if (edges.get()) R3.boxEdges(cx - r, cy - r, cz - r, cx + r, cy + r, cz + r, 0.02 * s, R3.argb(rgb, 0.6 * pv));
                if (beam.get()) {
                    double bh = beamHeight.get();
                    R3.line(cx, cy, cz, cx, cy + bh, cz, 0.08 * s, R3.argb(rgb, 0.4 * pv), R3.argb(rgb2, 0));
                }
                if (groundGlow.get()) {
                    R3.ring(cx, p[1] + 0.02, cz, 0, 1.2 * s, 32, R3.argb(rgb, 0.3 * pv), R3.argb(rgb, 0), 0);
                }
                if (orbitRings.get()) {
                    double orbitR = 0.9 * s;
                    double spin = t * 1.5 + phase * 10;
                    R3.ring(cx, cy + Math.sin(spin) * 0.3, cz, orbitR - 0.03, orbitR, 24, R3.argb(rgb, 0.35 * pv), R3.argb(rgb2, 0.15 * pv), spin);
                    R3.ring(cx, cy + Math.sin(spin + 2) * 0.25, cz, orbitR * 0.7 - 0.02, orbitR * 0.7, 20, R3.argb(rgb2, 0.25 * pv), R3.argb(rgb, 0.1 * pv), -spin * 1.3);
                }
            }
        }
    }

    // ================================================================== totem pop
    public static final class TotemEffect extends Module {
        final Setting.Color color = add(new Setting.Color("Farbe", 2));
        final Setting.Num rings = add(new Setting.Num("Ringe", 1, 8, 1, 3, ""));
        final Setting.Num ringSize = add(new Setting.Num("Ring-Größe", 0.5, 3.0, 0.1, 1.0, "x"));
        final Setting.Num ringSpeed = add(new Setting.Num("Ring-Tempo", 0.3, 3.0, 0.1, 1.0, "x"));
        final Setting.Num ringWidth = add(new Setting.Num("Ring-Dicke", 0.02, 0.3, 0.01, 0.06, ""));
        final Setting.Bool particles = add(new Setting.Bool("Extra Partikel", true));
        final Setting.Num particleCount = add(new Setting.Num("Partikel-Menge", 5, 100, 5, 40, ""));
        final Setting.Bool message = add(new Setting.Bool("Meldung mit Zähler", true));
        final Setting.Bool screenFlash = add(new Setting.Bool("Bildschirm-Blitz", true));
        final Setting.Bool screenIcon = add(new Setting.Bool("Totem-Icon auf dem Bildschirm", true));
        final Setting.Num iconSize = add(new Setting.Num("Icon-Größe", 0.5, 3.0, 0.1, 1.2, "x"));
        final Setting.Bool shockwave = add(new Setting.Bool("Schockwelle", true));
        private final List<double[]> pops = new ArrayList<>();
        private final Map<String, Integer> counts = new HashMap<>();
        private long screenPopAt;

        TotemEffect() { super("totem", "Custom Totem", "Ringe, Partikel, Pop-Zähler, Bildschirm-Icon und Schockwelle bei Totems", Category.VISUALS); icon("totem"); }

        public void onPop(class_1297 e) {
            double[] p = {Mc.x(e), Mc.y(e), Mc.z(e), System.currentTimeMillis(), Mc.height(e)};
            pops.add(p);
            if (Mc.isPlayer(e) && Mc.player() != null && Mc.id(e) == Mc.id(Mc.player())) screenPopAt = System.currentTimeMillis();
            if (particles.get()) {
                int count = particleCount.getI();
                for (int i = 0; i < count; i++) {
                    double a = Math.random() * Math.PI * 2, s = 0.2 + Math.random() * 0.4;
                    Mc.spawn(i % 3 == 0 ? "end_rod" : "totem_of_undying", p[0], p[1] + 1, p[2], Math.cos(a) * s, 0.2 + Math.random() * 0.5, Math.sin(a) * s);
                }
            }
            if (message.get() && Mc.isPlayer(e)) {
                String name = Mc.name(e);
                int n = counts.merge(name, 1, Integer::sum);
                dev.lego.ui.Toasts.show("totem", name + " hat gepoppt", n + " Totem" + (n == 1 ? "" : "s") + " bisher", 0xFFFFCD03, 3000);
            }
        }

        public void onDeath(class_1297 e) { if (Mc.isPlayer(e)) counts.remove(Mc.name(e)); }

        void render() {
            long now = System.currentTimeMillis();
            int nRings = rings.getI();
            double sz = ringSize.get();
            double spd = ringSpeed.get();
            double thick = ringWidth.get();
            Iterator<double[]> it = pops.iterator();
            while (it.hasNext()) {
                double[] p = it.next();
                double t = (now - p[3]) / (1300.0 / spd);
                if (t >= 1) { it.remove(); continue; }
                double e = 1 - Math.pow(1 - t, 3);
                for (int k = 0; k < nRings; k++) {
                    double frac = nRings > 1 ? (double) k / (nRings - 1) : 0;
                    double y = p[1] + p[4] * (0.2 + 0.6 * frac) + e * 0.6;
                    double r = (0.4 + e * (1.2 + frac * 0.6)) * sz;
                    int rgb = color.argb(frac * 0.4) & 0xFFFFFF;
                    R3.ring(p[0], y, p[2], r - thick, r, 40, R3.argb(rgb, 0.9 * (1 - t)), R3.argb(rgb, 0.3 * (1 - t)), t * 3 * spd + k);
                }
                if (shockwave.get() && t < 0.4) {
                    double sw = e * 3.0 * sz;
                    int rgb = color.argb(0) & 0xFFFFFF;
                    R3.ring(p[0], p[1] + 0.05, p[2], sw - 0.08, sw, 48, R3.argb(rgb, 0.5 * (1 - t / 0.4)), R3.argb(rgb, 0.1 * (1 - t / 0.4)), 0);
                }
                if (screenFlash.get() && t < 0.15) {
                    int rgb = color.argb(0) & 0xFFFFFF;
                    int W = Mc.mc().method_22683().method_4480(), H = Mc.mc().method_22683().method_4507();
                    dev.lego.ui.Gx.rect(0, 0, W, H, 0, (rgb & 0xFFFFFF) | ((int) (255 * (1 - t / 0.15) * 0.25) << 24));
                }
            }
        }

        public void drawScreenIcon(int W, int H) {
            if (!screenIcon.get()) return;
            long age = System.currentTimeMillis() - screenPopAt;
            if (age > 1200 || age < 0) return;
            float t = age / 1200f;
            float alpha = t < 0.15f ? t / 0.15f : 1 - (t - 0.15f) / 0.85f;
            float scale = 0.6f + 0.4f * (1 - (1 - Math.min(1, t * 4)) * (1 - Math.min(1, t * 4)));
            float k = (float) iconSize.get() * scale;
            int s = Math.round(W * 0.06f * k);
            int cx = W / 2, cy = H / 2 - s / 2 - Math.round(W * 0.04f);
            int rgb = color.argb(0);
            dev.lego.ui.Gx.pushAlpha(alpha * alpha);
            dev.lego.ui.Gx.push();
            dev.lego.ui.Gx.scaleAt(cx, cy + s / 2f, scale);
            dev.lego.ui.Gx.glow(cx, cy + s / 2, s, dev.lego.ui.Gx.withAlpha(rgb, 0.35f * alpha));
            dev.lego.ui.Gx.icon("totem", cx, cy + s / 2f, s * 0.7f, dev.lego.ui.Gx.withAlpha(rgb, 0.9f));
            dev.lego.ui.Gx.pop();
            dev.lego.ui.Gx.popAlpha();
        }
    }

    // ================================================================== particles
    public static final class HitParticles extends Module {
        final Setting.Mode type = add(new Setting.Mode("Partikel", 1, PARTICLE_NAMES));
        final Setting.Num amount = add(new Setting.Num("Menge", 1, 30, 1, 8, ""));
        HitParticles() { super("hitparticles", "Hit-Partikel", "Extra Partikel wenn du triffst", Category.VISUALS); icon("hit"); }
        public void onHit(class_1297 target) {
            String id = particle(type);
            double x = Mc.x(target), y = Mc.y(target) + Mc.height(target) * 0.6, z = Mc.z(target);
            for (int i = 0; i < amount.getI(); i++)
                Mc.spawn(id, x + (Math.random() - 0.5) * 0.6, y + (Math.random() - 0.5) * 0.6, z + (Math.random() - 0.5) * 0.6,
                        (Math.random() - 0.5) * 0.3, Math.random() * 0.25, (Math.random() - 0.5) * 0.3);
        }
    }

    public static final class Trails extends Module {
        final Setting.Mode type = add(new Setting.Mode("Partikel", 2, PARTICLE_NAMES));
        final Setting.Bool onlyMoving = add(new Setting.Bool("Nur beim Laufen", true));
        Trails() { super("trails", "Trails", "Partikelspur hinter dir", Category.VISUALS); icon("trail"); }
        @Override public void tick() {
            class_746 p = Mc.player();
            if (p == null) return;
            double dx = Mc.x(p) - Mc.lastX(p), dz = Mc.z(p) - Mc.lastZ(p);
            if (onlyMoving.get() && dx * dx + dz * dz < 0.0004) return;
            Mc.spawn(particle(type), Mc.x(p) + (Math.random() - 0.5) * 0.3, Mc.y(p) + 0.1, Mc.z(p) + (Math.random() - 0.5) * 0.3, 0, 0.01, 0);
        }
    }

    public static final class KillEffect extends Module {
        final Setting.Mode type = add(new Setting.Mode("Partikel", 6, PARTICLE_NAMES));
        final Setting.Bool lightning = add(new Setting.Bool("Blitz-Strahl", true));
        private final List<double[]> beams = new ArrayList<>();
        KillEffect() { super("killeffect", "Kill-Effekt", "Partikel-Explosion und Lichtstrahl wenn dein Gegner stirbt", Category.VISUALS); icon("skull"); }
        public void onDeath(class_1297 e) {
            class_1297 lt = dev.lego.hud.HudModules.CombatTracker.lastTarget();
            if (lt == null || Mc.id(e) != Mc.id(lt)) return;
            if (System.currentTimeMillis() - dev.lego.hud.HudModules.CombatTracker.lastAttackAt() > 10000) return;
            String id = particle(type);
            for (int i = 0; i < 70; i++) {
                double a = Math.random() * Math.PI * 2, s = 0.1 + Math.random() * 0.35;
                Mc.spawn(id, Mc.x(e), Mc.y(e) + 0.2 + Math.random() * Mc.height(e), Mc.z(e), Math.cos(a) * s, Math.random() * 0.4, Math.sin(a) * s);
            }
            if (lightning.get()) beams.add(new double[]{Mc.x(e), Mc.y(e), Mc.z(e), System.currentTimeMillis()});
            dev.lego.util.Utility.onKill(e);
        }
        void render() {
            long now = System.currentTimeMillis();
            Iterator<double[]> it = beams.iterator();
            while (it.hasNext()) {
                double[] b = it.next();
                double t = (now - b[3]) / 900.0;
                if (t >= 1) { it.remove(); continue; }
                double a = 1 - t;
                int acc = dev.lego.core.Modules.accentArgb() & 0xFFFFFF;
                R3.line(b[0], b[1], b[2], b[0], b[1] + 40, b[2], 0.5 * a + 0.1, R3.argb(0xFFFFFF, 0.9 * a), R3.argb(acc, 0));
                R3.line(b[0], b[1], b[2], b[0], b[1] + 25, b[2], 1.4 * a, R3.argb(acc, 0.35 * a), R3.argb(acc, 0));
                R3.ring(b[0], b[1] + 0.05, b[2], t * 3, t * 3 + 0.25, 40, R3.argb(acc, 0.7 * a), R3.argb(acc, 0), 0);
            }
        }
    }

    // ================================================================== small helpers
    public static final class Fullbright extends Module {
        Fullbright() { super("fullbright", "Fullbright", "Alles hell (wie Nachtsicht, nur für dich)", Category.VISUALS); icon("sun"); }
        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override public void tick() {
            class_746 p = Mc.player();
            class_6880 nv = Mc.nightVision();
            if (p == null || nv == null) return;
            if (!p.method_6059(nv)) p.method_6092(new class_1293(nv, 20 * 60 * 60 * 24, 0, false, false, false));
        }
        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override public void onDisable() {
            class_746 p = Mc.player();
            class_6880 nv = Mc.nightVision();
            if (p != null && nv != null) p.method_6016(nv);
        }
    }

    public static final class Zoom extends Module {
        final Setting.Num factor = add(new Setting.Num("Zoom", 2, 12, 0.5, 4, "x"));
        final Setting.Bool smooth = add(new Setting.Bool("Weich", true));
        final Setting.Bool scroll = add(new Setting.Bool("Mausrad ändert Zoom", true));
        final Setting.Bool cinematic = add(new Setting.Bool("Filmische Kamera beim Zoomen", false));
        private double current = 1, extra = 0;
        public boolean held;
        private boolean savedSmooth;
        Zoom() { super("zoom", "Zoom", "Taste C halten zum Zoomen, Mausrad = stärker/schwächer", Category.VISUALS); enabled = true; icon("zoom"); }
        public boolean active() { return enabled && held && !Mc.screenOpen(); }
        /** Called by the FOV mixin. */
        public float apply(float fov) {
            double target = active() ? Math.max(1.1, factor.get() + extra) : 1;
            current = smooth.get() ? current + (target - current) * 0.25 : target;
            if (Math.abs(current - target) < 0.01) current = target;
            return (float) (fov / current);
        }
        /** Mouse wheel while zooming (returns true = consumed). */
        public boolean onScroll(double v) {
            if (!active() || !scroll.get()) return false;
            extra = Math.max(1 - factor.get(), Math.min(30, extra + (v > 0 ? 1 : -1) * Math.max(0.5, (factor.get() + extra) * 0.15)));
            return true;
        }
        @Override public void tick() {
            if (!held) extra = 0;
            if (cinematic.get()) {
                net.minecraft.class_315 o = Mc.options();
                if (active() && !o.field_1914) { savedSmooth = false; o.field_1914 = true; }
                else if (!active() && o.field_1914 && !savedSmooth) { o.field_1914 = false; savedSmooth = true; }
            }
        }
    }

    public static final class NoHurtCam extends Module {
        NoHurtCam() { super("nohurtcam", "Kein Wackeln", "Kein Bildschirm-Wackeln bei Schaden", Category.VISUALS); icon("tilt"); }
    }

    public static final class TimeChanger extends Module {
        final Setting.Num time = add(new Setting.Num("Uhrzeit", 0, 24000, 250, 18000, ""));
        final Setting.Bool cycle = add(new Setting.Bool("Schneller Tag-Nacht-Zyklus", false));
        private double t = -1;
        TimeChanger() { super("timechanger", "Zeit ändern", "Tag oder Nacht nur für dich", Category.VISUALS); icon("moon"); }
        @Override public void tick() {
            if (cycle.get()) { if (t < 0) t = time.get(); t = (t + 40) % 24000; Mc.setTimeOfDay((long) t); }
            else { t = -1; Mc.setTimeOfDay((long) time.get()); }
        }
    }

    public static final class ClearWeather extends Module {
        ClearWeather() { super("clearweather", "Kein Regen", "Regen und Gewitter nur für dich aus", Category.VISUALS); icon("rain"); }
        @Override public void tick() { Mc.clearWeather(); }
    }

    // ================================================================== crosshair (replaces the vanilla crosshair)
    public static final class Crosshair extends Module {
        final Setting.Mode style = add(new Setting.Mode("Form", 0, "Kreuz", "Punkt", "Kreuz + Punkt", "Kreis", "T-Form", "Diamant", "Pfeile", "Dreieck"));
        final Setting.Color color = add(new Setting.Color("Farbe", 8));
        final Setting.Num size = add(new Setting.Num("Größe", 2, 10, 1, 5, ""));
        final Setting.Num gap = add(new Setting.Num("Abstand", 0, 6, 1, 2, ""));
        final Setting.Num thick = add(new Setting.Num("Dicke", 1, 3, 1, 1, ""));
        final Setting.Bool outline = add(new Setting.Bool("Umrandung", true));
        final Setting.Bool dynamic = add(new Setting.Bool("Auf Gegner rot", true));
        final Setting.Bool animSpin = add(new Setting.Bool("Rotation bei Bewegung", false));
        Crosshair() { super("crosshair", "Custom Crosshair", "Eigenes Fadenkreuz mit vielen Formen", Category.VISUALS); icon("crosshair"); }

        private float spinAngle;

        public void draw(class_332 ctx) {
            int cx = ctx.method_51421() / 2, cy = ctx.method_51443() / 2;
            int c = color.argb(), s = size.getI(), g = gap.getI(), t = thick.getI();
            if (dynamic.get() && Mc.mc().field_1692 instanceof class_1309) c = 0xFFFF4D5E;
            int o = 0xC0000000;
            String st = style.get();
            if (animSpin.get()) {
                class_746 p = Mc.player();
                if (p != null) {
                    double dx = Mc.x(p) - Mc.lastX(p), dz = Mc.z(p) - Mc.lastZ(p);
                    double speed = Math.sqrt(dx * dx + dz * dz);
                    spinAngle += (float) (speed * 40);
                }
            } else spinAngle *= 0.9f;
            if (st.equals("Kreuz") || st.equals("Kreuz + Punkt") || st.equals("T-Form")) {
                if (!st.equals("T-Form")) rect(ctx, cx - t / 2, cy - g - s, t, s, c, o);
                rect(ctx, cx - t / 2, cy + g + 1, t, s, c, o);
                rect(ctx, cx - g - s, cy - t / 2, s, t, c, o);
                rect(ctx, cx + g + 1, cy - t / 2, s, t, c, o);
            }
            if (st.equals("Punkt") || st.equals("Kreuz + Punkt")) rect(ctx, cx - t / 2, cy - t / 2, Math.max(1, t), Math.max(1, t), c, o);
            if (st.equals("Kreis")) {
                int r = s + g;
                for (int i = 0; i < 32; i++) {
                    double a = Math.PI * 2 * i / 32 + Math.toRadians(spinAngle);
                    int x = cx + (int) Math.round(Math.cos(a) * r), y = cy + (int) Math.round(Math.sin(a) * r);
                    rect(ctx, x, y, t, t, c, 0);
                }
                rect(ctx, cx, cy, 1, 1, c, 0);
            }
            if (st.equals("Diamant")) {
                int r = s + g;
                double ang = Math.toRadians(spinAngle);
                for (int i = 0; i < 4; i++) {
                    double a = Math.PI / 2 * i + ang;
                    int x = cx + (int) Math.round(Math.cos(a) * r), y = cy + (int) Math.round(Math.sin(a) * r);
                    int x2 = cx + (int) Math.round(Math.cos(a + Math.PI / 2) * r), y2 = cy + (int) Math.round(Math.sin(a + Math.PI / 2) * r);
                    for (int j = 0; j <= s; j++) {
                        float f = j / (float) Math.max(1, s);
                        int lx = (int) (x + (x2 - x) * f), ly = (int) (y + (y2 - y) * f);
                        rect(ctx, lx - t / 2, ly - t / 2, t, t, c, 0);
                    }
                }
                rect(ctx, cx, cy, 1, 1, c, 0);
            }
            if (st.equals("Pfeile")) {
                int r = g + s;
                double ang = Math.toRadians(spinAngle);
                for (int i = 0; i < 4; i++) {
                    double a = Math.PI / 2 * i + ang;
                    double perpL = a + Math.PI * 0.75, perpR = a - Math.PI * 0.75;
                    int tipX = cx + (int) Math.round(Math.cos(a) * r), tipY = cy + (int) Math.round(Math.sin(a) * r);
                    int armLen = Math.max(2, s / 2 + 1);
                    int lx = tipX + (int) Math.round(Math.cos(perpL) * armLen), ly = tipY + (int) Math.round(Math.sin(perpL) * armLen);
                    int rx = tipX + (int) Math.round(Math.cos(perpR) * armLen), ry = tipY + (int) Math.round(Math.sin(perpR) * armLen);
                    for (int j = 0; j <= armLen; j++) {
                        float f = j / (float) Math.max(1, armLen);
                        rect(ctx, (int) (tipX + (lx - tipX) * f) - t / 2, (int) (tipY + (ly - tipY) * f) - t / 2, t, t, c, 0);
                        rect(ctx, (int) (tipX + (rx - tipX) * f) - t / 2, (int) (tipY + (ry - tipY) * f) - t / 2, t, t, c, 0);
                    }
                }
            }
            if (st.equals("Dreieck")) {
                int r = s + g;
                double ang = Math.toRadians(spinAngle) - Math.PI / 2;
                int[] tx = new int[3], ty = new int[3];
                for (int i = 0; i < 3; i++) {
                    double a = ang + Math.PI * 2 / 3 * i;
                    tx[i] = cx + (int) Math.round(Math.cos(a) * r);
                    ty[i] = cy + (int) Math.round(Math.sin(a) * r);
                }
                for (int i = 0; i < 3; i++) {
                    int x1 = tx[i], y1 = ty[i], x2 = tx[(i + 1) % 3], y2 = ty[(i + 1) % 3];
                    int steps = Math.max(s * 2, 4);
                    for (int j = 0; j <= steps; j++) {
                        float f = j / (float) steps;
                        rect(ctx, (int) (x1 + (x2 - x1) * f) - t / 2, (int) (y1 + (y2 - y1) * f) - t / 2, t, t, c, 0);
                    }
                }
                rect(ctx, cx, cy, 1, 1, c, 0);
            }
        }

        private void rect(class_332 ctx, int x, int y, int w, int h, int c, int o) {
            if (outline.get() && o != 0) ctx.method_25294(x - 1, y - 1, x + w + 1, y + h + 1, o);
            ctx.method_25294(x, y, x + w, y + h, c);
        }
    }

    // ================================================================== custom fov
    public static final class CustomFov extends Module {
        final Setting.Num fov = add(new Setting.Num("FOV", 30, 160, 1, 90, "°"));
        CustomFov() { super("customfov", "Custom FOV", "Setze dein FOV von 30 bis 160", Category.VISUALS); icon("eye"); }
        public float apply(float original) {
            if (!enabled) return original;
            return (float) fov.get();
        }
    }

    // ================================================================== glint color
    public static final class GlintColor extends Module {
        public final Setting.Color color = add(new Setting.Color("Farbe", 6));
        public final Setting.Bool noGlint = add(new Setting.Bool("Glint deaktivieren", false));
        public final Setting.Num speed = add(new Setting.Num("Geschwindigkeit", 0.2, 3.0, 0.1, 1.0, "x"));
        public final Setting.Num brightness = add(new Setting.Num("Helligkeit", 20, 100, 5, 80, "%"));
        GlintColor() { super("glint", "Custom Glint", "Verzauberungs-Glint Farbe anpassen oder deaktivieren", Category.VISUALS); icon("enchant"); fresh(); }
        public boolean shouldHideGlint() { return enabled && noGlint.get(); }
        public int glintArgb() {
            int rgb = color.argb(System.currentTimeMillis() / 1000.0 * speed.get() * 0.3) & 0xFFFFFF;
            int a = (int) (255 * brightness.get() / 100.0);
            return (a << 24) | rgb;
        }
        public float[] glintRgbf() {
            int c = glintArgb();
            float b = (float) (brightness.get() / 100.0);
            return new float[]{((c >> 16) & 0xFF) / 255f * b, ((c >> 8) & 0xFF) / 255f * b, (c & 0xFF) / 255f * b};
        }
    }

    // ================================================================== events from mixins
    /** ClientPlayNetworkHandler.onEntityStatus hook (35 = totem pop, 3 = death). */
    public static void onStatus(class_1309 e, byte status) {
        try {
            if (status == 35 && totem.enabled) totem.onPop(e);
            if (status == 3) {
                if (kill.enabled) kill.onDeath(e);
                else if (e == dev.lego.hud.HudModules.CombatTracker.lastTarget()
                        && System.currentTimeMillis() - dev.lego.hud.HudModules.CombatTracker.lastAttackAt() < 5000) dev.lego.util.Utility.onKill(e);
                totem.onDeath(e);
            }
        } catch (Throwable t) {
            LegoClient.LOG("Status-Fehler: " + t);
        }
    }

    public static void onAttack(class_1297 target) {
        dev.lego.hud.HudModules.CombatTracker.onAttack(target);
        if (hit.enabled) hit.onHit(target);
        Visuals2.onAttack(target);
        dev.lego.util.Utility.onHit(target);
    }

    static void renderKillBeams() { if (kill.enabled) kill.render(); }

    private static void renderChunkBorders() {
        Module m = dev.lego.core.Modules.get("u4_chunkborder");
        if (m == null || !m.enabled) return;
        class_746 p = Mc.player();
        if (p == null) return;
        int cx = ((int) Math.floor(Mc.x(p))) >> 4 << 4;
        int cz = ((int) Math.floor(Mc.z(p))) >> 4 << 4;
        double y0 = Mc.y(p) - 2, y1 = Mc.y(p) + 8;
        int green = R3.argb(0x39D353, 0.55);
        int greenFade = R3.argb(0x39D353, 0.0);
        for (int dx = -1; dx <= 2; dx++) {
            for (int dz = -1; dz <= 2; dz++) {
                double x = cx + dx * 16, z = cz + dz * 16;
                R3.line(x, y0, cz, x, y1, cz, 0.03, green, greenFade);
                R3.line(cx, y0, z, cx, y1, z, 0.03, green, greenFade);
                R3.line(x, y0, cz + 16, x, y1, cz + 16, 0.03, green, greenFade);
                R3.line(cx + 16, y0, z, cx + 16, y1, z, 0.03, green, greenFade);
            }
        }
        int yellowC = R3.argb(0xFFCD03, 0.45);
        int yellowF = R3.argb(0xFFCD03, 0.0);
        for (int dx = -1; dx <= 2; dx++)
            for (int dz = -1; dz <= 2; dz++) {
                double x0 = cx + dx * 16, z0 = cz + dz * 16;
                double yb = Mc.y(p) + 0.01;
                R3.line(x0, yb, cz, x0, yb, cz + 16, 0.02, yellowC, yellowC);
                R3.line(cx, yb, z0, cx + 16, yb, z0, 0.02, yellowC, yellowC);
            }
    }
}
