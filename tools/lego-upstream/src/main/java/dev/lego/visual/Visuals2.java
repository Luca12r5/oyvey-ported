package dev.lego.visual;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.hud.HudModules;
import dev.lego.ui.Fonts;
import dev.lego.ui.Tx;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_265;
import net.minecraft.class_2960;
import net.minecraft.class_3965;
import net.minecraft.class_746;

/** Additional world visuals: block outline, damage numbers, target marker, snow, fireflies, rings, projectile trails, camera. */
public final class Visuals2 {
    private Visuals2() { }

    public static BlockOutline outline;
    public static DamageNumbers damage;
    public static TargetMarker target;
    public static PlayerRings rings;
    public static ChinaHat chinaHat;
    public static ShaderHands shaderHands;
    public static ShaderSky shaderSky;
    public static ProjectileTrails projectiles;
    public static DynamicCamera camera;
    public static LowFire lowFire;

    public static void registerAll() {
        outline = Modules.register(new BlockOutline());
        damage = Modules.register(new DamageNumbers());
        target = Modules.register(new TargetMarker());
        rings = Modules.register(new PlayerRings());
        chinaHat = Modules.register(new ChinaHat());
        shaderHands = Modules.register(new ShaderHands());
        shaderSky = Modules.register(new ShaderSky());
        projectiles = Modules.register(new ProjectileTrails());
        camera = Modules.register(new DynamicCamera());
        lowFire = Modules.register(new LowFire());
        Overlays.registerAll();
    }

    public static void renderHud(int W, int H) {
        if (shaderHands != null && shaderHands.enabled) shaderHands.tick();
    }

    static void render(float td) {
        if (outline.enabled) outline.render();
        if (rings.enabled) rings.render(td);
        if (chinaHat.enabled) chinaHat.render(td);
        if (shaderSky.enabled) shaderSky.render(td);
        if (target.enabled) target.render(td);
        if (projectiles.enabled) projectiles.render();
        Visuals.renderKillBeams();
        if (damage.enabled) damage.render();
    }

    static void onAttack(class_1297 e) {
        if (damage.enabled) damage.lastAttack = System.currentTimeMillis();
    }

    // ================================================================== block outline
    public static final class BlockOutline extends Module {
        final Setting.Color color = add(new Setting.Color("Farbe", 0));
        final Setting.Num width = add(new Setting.Num("Liniendicke", 1, 6, 0.5, 2.5, ""));
        final Setting.Num fill = add(new Setting.Num("Füllung", 0, 60, 5, 15, "%"));
        final Setting.Bool pulse = add(new Setting.Bool("Pulsieren", true));

        BlockOutline() { super("blockoutline", "Block-Outline", "Schöner Rahmen und Füllung um den anvisierten Block", Category.VISUALS); icon("box"); fresh(); }

        /** Hook: return false to hide the vanilla outline. */
        public boolean replaceVanilla() { return enabled; }

        void render() {
            class_239 hit = Mc.mc().field_1765;
            if (!(hit instanceof class_3965) || hit.method_17783() != class_239.class_240.field_1332) return;
            class_2338 pos = ((class_3965) hit).method_17777();
            class_238 b;
            try {
                class_265 shape = Mc.world().method_8320(pos).method_26218(Mc.world(), pos);
                if (shape.method_1110()) return;
                b = shape.method_1107();
            } catch (Throwable t) { return; }
            double e = 0.002;
            double x0 = pos.method_10263() + b.field_1323 - e, y0 = pos.method_10264() + b.field_1322 - e, z0 = pos.method_10260() + b.field_1321 - e;
            double x1 = pos.method_10263() + b.field_1320 + e, y1 = pos.method_10264() + b.field_1325 + e, z1 = pos.method_10260() + b.field_1324 + e;
            int rgb = color.argb() & 0xFFFFFF;
            double p = pulse.get() ? 0.75 + 0.25 * Math.sin(System.currentTimeMillis() / 250.0) : 1;
            double dist = Math.sqrt(Math.pow((x0 + x1) / 2 - R3.camX(), 2) + Math.pow((y0 + y1) / 2 - R3.camY(), 2) + Math.pow((z0 + z1) / 2 - R3.camZ(), 2));
            double w = width.get() * 0.004 * Math.max(1, dist * 0.35);
            if (fill.get() > 0) R3.box(x0, y0, z0, x1, y1, z1, R3.argb(rgb, fill.get() / 100.0 * p));
            R3.boxEdges(x0, y0, z0, x1, y1, z1, w, R3.argb(rgb, 0.95 * p));
        }
    }

    // ================================================================== floating damage numbers
    public static final class DamageNumbers extends Module {
        final Setting.Bool onlyMine = add(new Setting.Bool("Nur eigene Treffer", true));
        final Setting.Num size = add(new Setting.Num("Größe", 0.5, 2, 0.1, 1, "x"));
        final Setting.Bool hearts = add(new Setting.Bool("In Herzen anzeigen", true));
        private final Map<Integer, Float> hp = new HashMap<>();
        private final List<double[]> nums = new ArrayList<>();   // x, y, z, t0, amount, crit
        long lastAttack = 0;

        DamageNumbers() { super("damagenumbers", "Schadenszahlen", "Fliegende Zahlen zeigen, wie viel Schaden du machst", Category.VISUALS); icon("number"); fresh(); }

        @Override public void tick() {
            class_746 me = Mc.player();
            if (me == null) return;
            Map<Integer, Float> now = new HashMap<>();
            for (class_1297 e : Mc.entities()) {
                if (!(e instanceof class_1309) || e == me) continue;
                if (me.method_5858(e) > 32 * 32) continue;
                class_1309 l = (class_1309) e;
                float h = Mc.health(l) + Mc.absorption(l);
                now.put(Mc.id(e), h);
                Float old = hp.get(Mc.id(e));
                if (old != null && h < old - 0.01f) {
                    boolean mine = e == HudModules.CombatTracker.lastTarget() && System.currentTimeMillis() - lastAttack < 1200;
                    if (onlyMine.get() && !mine) continue;
                    double[] p = {Mc.x(e) + (Math.random() - 0.5) * 0.6, Mc.y(e) + Mc.height(e) + 0.1, Mc.z(e) + (Math.random() - 0.5) * 0.6,
                            System.currentTimeMillis(), old - h, Mc.velocity(me)[1] < -0.05 ? 1 : 0};
                    nums.add(p);
                }
            }
            hp.clear();
            hp.putAll(now);
        }

        void render() {
            long t = System.currentTimeMillis();
            Iterator<double[]> it = nums.iterator();
            while (it.hasNext()) {
                double[] n = it.next();
                double a = (t - n[3]) / 1100.0;
                if (a >= 1) { it.remove(); continue; }
                double amount = hearts.get() ? n[4] / 2 : n[4];
                String s = (amount >= 10 ? String.valueOf(Math.round(amount)) : String.format(java.util.Locale.ROOT, "%.1f", amount)) + (hearts.get() ? "❤" : "");
                int col = n[5] > 0 ? 0xFFFFC53D : (amount >= 4 ? 0xFFFF4D5E : 0xFFFFFFFF);
                class_2960 tex = TextTex.get(s, col);
                double pop = a < 0.15 ? 0.6 + a / 0.15 * 0.6 : 1.2 - Math.min(0.2, (a - 0.15));
                double h = 0.35 * size.get() * pop;
                double w = h * TextTex.aspect(s, col);
                R3.billboard(tex, n[0], n[1] + a * 0.8, n[2], w, h, 0, 0, 1, 1, R3.argb(0xFFFFFF, a > 0.7 ? (1 - a) / 0.3 : 1));
            }
        }
    }

    /** Cached text textures for world billboards. */
    public static final class TextTex {
        private static final Map<String, class_2960> TEX = new HashMap<>();
        private static final Map<String, Double> ASPECT = new HashMap<>();
        private static int n = 0;

        public static class_2960 get(String s, int argb) {
            String key = s + "|" + argb;
            class_2960 id = TEX.get(key);
            if (id != null) return id;
            float size = 48;
            int w = (int) Math.ceil(Fonts.width(s, Fonts.BOLD, size)) + 16, h = 64;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            dev.lego.ui.Gx.hints(g);
            g.setFont(Fonts.get(Fonts.BOLD, size));
            g.setColor(new Color(0, 0, 0, 170));
            for (int dx = -2; dx <= 2; dx++) for (int dy = -2; dy <= 3; dy++) g.drawString(s, 8 + dx, 48 + dy);
            g.setColor(new Color(argb, true));
            g.drawString(s, 8, 48);
            g.dispose();
            id = class_2960.method_60655("legoclient", "text/" + (n++));
            Tx.register(id, img.getRGB(0, 0, w, h, null, 0, w), w, h, true);
            TEX.put(key, id);
            ASPECT.put(key, w / (double) h);
            if (TEX.size() > 300) {        // simple cleanup
                for (class_2960 old : TEX.values()) Tx.destroy(old);
                TEX.clear();
                ASPECT.clear();
            }
            return id;
        }

        public static double aspect(String s, int argb) { Double a = ASPECT.get(s + "|" + argb); return a == null ? 2 : a; }
    }

    // ================================================================== target marker
    public static final class TargetMarker extends Module {
        final Setting.Color color = add(new Setting.Color("Farbe", 1));
        final Setting.Mode style = add(new Setting.Mode("Stil", 0, "Ring + Pfeil", "Nur Ring", "Nur Pfeil"));
        TargetMarker() { super("targetmarker", "Ziel-Markierung", "Markiert den Gegner, den du gerade bekämpfst", Category.VISUALS); icon("target"); fresh(); }

        void render(float td) {
            class_1297 t = HudModules.CombatTracker.lastTarget();
            if (t == null || !t.method_5805() || System.currentTimeMillis() - HudModules.CombatTracker.lastAttackAt() > 6000) return;
            double[] p = Mc.lerpPos(t, td);
            double time = System.currentTimeMillis() / 1000.0;
            int rgb = color.argb() & 0xFFFFFF;
            double r = Mc.width(t) * 0.9 + 0.2;
            if (style.index != 2) {
                double y = p[1] + (0.5 + 0.5 * Math.sin(time * 2.5)) * Mc.height(t);
                R3.ring(p[0], y, p[2], r - 0.05, r + 0.05, 40, R3.argb(rgb, 0.9), R3.argb(rgb, 0.9), time);
                R3.ring(p[0], y, p[2], r, r + 0.35, 40, R3.argb(rgb, 0.35), R3.argb(rgb, 0), time);
            }
            if (style.index != 1) {
                double top = p[1] + Mc.height(t) + 0.45 + Math.sin(time * 4) * 0.08;
                double a = time * 2, s = 0.18;
                double ca = Math.cos(a) * s, sa = Math.sin(a) * s;
                int c = R3.argb(rgb, 0.95);
                R3.quad(p[0] - ca, top + s * 1.4, p[2] - sa, p[0] + ca, top + s * 1.4, p[2] + sa, p[0], top, p[2], p[0], top, p[2], c);
                R3.quad(p[0] - sa, top + s * 1.4, p[2] + ca, p[0] + sa, top + s * 1.4, p[2] - ca, p[0], top, p[2], p[0], top, p[2], c);
            }
        }
    }

    // ================================================================== rings under players
    public static final class PlayerRings extends Module {
        final Setting.Color color = add(new Setting.Color("Farbe", 3));
        final Setting.Num range = add(new Setting.Num("Reichweite", 8, 64, 4, 32, " Blöcke"));
        PlayerRings() { super("playerrings", "Spieler-Ringe", "Leuchtende Ringe unter anderen Spielern", Category.VISUALS); icon("ring"); fresh(); }
        void render(float td) {
            class_746 me = Mc.player();
            if (me == null) return;
            double t = System.currentTimeMillis() / 1000.0;
            for (class_1657 p : Mc.players()) {
                if (p == me || Mc.isSpectator(p) || me.method_5858(p) > range.get() * range.get()) continue;
                double[] pos = Mc.lerpPos(p, td);
                int rgb = color.argb(Mc.id(p) * 0.21) & 0xFFFFFF;
                R3.ring(pos[0], pos[1] + 0.02, pos[2], 0.55, 0.62, 36, R3.argb(rgb, 0.9), R3.argb(rgb, 0.9), 0);
                R3.ring(pos[0], pos[1] + 0.02, pos[2], 0.1, 0.55, 36, R3.argb(rgb, 0), R3.argb(rgb, 0.25 + 0.1 * Math.sin(t * 3)), 0);
            }
        }
    }

    // ================================================================== gooba-style china hat
    public static final class ChinaHat extends Module {
        final Setting.Color color = add(new Setting.Color("Farbe", 11));
        final Setting.Num radius = add(new Setting.Num("Radius", 0.45, 1.35, 0.05, 0.82, ""));
        final Setting.Num height = add(new Setting.Num("Höhe", 0.08, 0.55, 0.02, 0.28, ""));
        final Setting.Num alpha = add(new Setting.Num("Deckkraft", 0.05, 1.00, 0.05, 0.55, ""));
        final Setting.Bool outline = add(new Setting.Bool("Umriss", true));
        final Setting.Bool selfOnly = add(new Setting.Bool("Nur ich", true));

        ChinaHat() { super("chinahat", "China Hat", "Gooba-Style konischer Hut über dem Spieler", Category.VISUALS); icon("hat"); fresh(); }

        void render(float td) {
            class_746 me = Mc.player(); if (me == null) return;
            for (class_1657 p : Mc.players()) {
                if (selfOnly.get() && p != me) continue;
                if (Mc.isSpectator(p)) continue;
                double[] q;
                if (p == me && Mc.firstPerson()) {
                    q = new double[] { R3.camX(), R3.camY(), R3.camZ() };
                    q[1] -= 0.12;
                } else {
                    q = Mc.lerpPos(p, td);
                }
                double y = q[1] + Mc.height(p) + 0.16;
                double r = radius.get(), h = height.get();
                int seg = 96;
                for (int i = 0; i < seg; i++) {
                    double a0 = Math.PI * 2 * i / seg, a1 = Math.PI * 2 * (i + 1) / seg;
                    int c0 = color.argb(i / (double) seg) & 0xFFFFFF;
                    int c1 = color.argb((i + 1) / (double) seg) & 0xFFFFFF;
                    int ca = R3.argb(c0, alpha.get() * 0.55), cb = R3.argb(c1, alpha.get() * 0.55);
                    double x0 = q[0] + Math.cos(a0) * r, z0 = q[2] + Math.sin(a0) * r;
                    double x1 = q[0] + Math.cos(a1) * r, z1 = q[2] + Math.sin(a1) * r;
                    R3.quad(q[0], y + h, q[2], x0, y, z0, x1, y, z1, q[0], y + h, q[2],
                            R3.argb(c0, alpha.get()), ca, cb, R3.argb(c1, alpha.get()));
                    if (outline.get()) R3.line(x0, y, z0, x1, y, z1, 0.025, R3.argb(c0, 0.95), R3.argb(c1, 0.95));
                }
                if (outline.get()) R3.line(q[0], y + h, q[2], q[0] + r, y, q[2], 0.018,
                        R3.argb(color.argb() & 0xFFFFFF, 0.85), R3.argb(color.argb(0.2) & 0xFFFFFF, 0.85));
            }
        }
    }

    // ================================================================== shader hands GPU outline (ported from Gooba)
    public static boolean shaderHandsEnabled() { return shaderHands != null && shaderHands.enabled; }

    public static final class ShaderHands extends Module {
        final Setting.Mode mode = add(new Setting.Mode("Modus", 0, "Glow", "Outline", "Neon", "Galaxy"));
        final Setting.Color color = add(new Setting.Color("Glow-Farbe", 6));
        final Setting.Num strength = add(new Setting.Num("Stärke", 5, 40, 1, 20, ""));
        final Setting.Num outlineWidth = add(new Setting.Num("Outline-Dicke", 1, 6, 1, 1, "px"));
        final Setting.Bool useItemColor = add(new Setting.Bool("Item-Farbe", false));
        final Setting.Bool transparency = add(new Setting.Bool("Transparenz", false));
        final Setting.Num transAlpha = add(new Setting.Num("Transparenz-Stärke", 5, 80, 5, 30, "%"));

        ShaderHands() { super("shaderhands", "Shader Hands", "GPU Glow-Outline auf den Händen mit verschiedenen Modi", Category.VISUALS); icon("hand"); fresh(); }

        @Override public void onEnable() {
            dev.lego.visual.shader.HandEffect he = dev.lego.visual.shader.HandEffect.getInstance();
            he.setEnabled(true);
            applySettings(he);
        }
        @Override public void onDisable() {
            dev.lego.visual.shader.HandEffect.getInstance().setEnabled(false);
        }
        public void tick() {
            if (!enabled) return;
            dev.lego.visual.shader.HandEffect he = dev.lego.visual.shader.HandEffect.getInstance();
            applySettings(he);
        }
        private void applySettings(dev.lego.visual.shader.HandEffect he) {
            int mi = mode.index;
            float str = strength.getF();
            if (mi == 2) str *= 1.6f;
            else if (mi == 3) str *= 0.7f;
            he.setGlowStrength(str);
            he.setOutlineEnabled(mi != 3);
            he.setOutlineWidth((int) outlineWidth.get());
            he.setGlowMode(mi == 1 ? 0 : mi == 2 ? 3 : mi == 3 ? 1 : 2);
            int rgb = color.argb() | 0xFF000000;
            if (mi == 3) {
                float t = System.currentTimeMillis() / 3000f;
                rgb = java.awt.Color.HSBtoRGB(t % 1f, 0.6f, 1f) | 0xFF000000;
            }
            he.setGlowColors(rgb);
            he.setUseItemColor(useItemColor.get());
            he.setTransparencyEnabled(transparency.get());
            he.setTransparencyAlpha(transAlpha.getF() / 100f);
        }
    }

    // ================================================================== shader sky (gooba-inspired procedural modes)
    public static final class ShaderSky extends Module {
        final Setting.Mode mode = add(new Setting.Mode("Modus", 0, "Aurora", "Goobaa", "Schnee", "Himmel", "Sterne", "Glow"));
        final Setting.Num strength = add(new Setting.Num("Stärke", 0.05, 1.00, 0.05, 0.85, ""));
        final Setting.Num stars = add(new Setting.Num("Sterne", 0.00, 1.00, 0.05, 0.65, ""));
        final Setting.Bool vanillaSky = add(new Setting.Bool("Vanilla-Himmel", false));

        ShaderSky() { super("shadersky", "Shader Sky", "Gooba-Style Shader-Himmel mit Aurora, Sternen und mehr", Category.VISUALS); icon("sky"); fresh(); }

        void render(float td) {
            double cx = R3.camX(), cy = R3.camY(), cz = R3.camZ();
            float t = System.currentTimeMillis() / 1000f;
            double a = strength.get();
            int base;
            switch (mode.index) {
                case 0: base = 0x53FFD6; break;
                case 1: base = 0xFF5CE8; break;
                case 2: base = 0xE8F6FF; break;
                case 3: base = 0x58A7FF; break;
                case 4: base = 0xFFFFFF; break;
                default: base = 0xFFD66A; break;
            }
            if (!vanillaSky.get()) drawSkyBox(cx, cy, cz, base, a, t);
            if (mode.index == 0 || mode.index == 1 || mode.index == 5) {
                for (int i = 0; i < 9; i++) {
                    double ph = t * (0.25 + i * 0.03) + i * 0.9;
                    double rr = 46 + i * 6;
                    int col = java.awt.Color.HSBtoRGB((float) ((ph * 0.08 + i * 0.11) % 1), 0.55f, 1f) & 0xFFFFFF;
                    R3.ring(cx + Math.cos(ph) * 6, cy + 36 + i * 1.2, cz + Math.sin(ph) * 6, rr, rr + 3.8, 128, R3.argb(col, 0), R3.argb(col, 0.35 * a), ph);
                }
            }
            int count = (int) (stars.get() * 140);
            for (int i = 0; i < count; i++) {
                double h = hash(i, 13), h2 = hash(i, 31), h3 = hash(i, 71);
                double ang = h * Math.PI * 2 + t * 0.005;
                double rr = 35 + h2 * 95;
                double sy = cy + 18 + h3 * 78;
                double tw = 0.45 + 0.55 * Math.sin(t * 2.2 + i);
                R3.glowBillboard(TextTex.get("✦", 0xFFFFFFFF), cx + Math.cos(ang) * rr, sy, cz + Math.sin(ang) * rr,
                        0.45 + h * 0.75, R3.argb(0xFFFFFF, 0.36 * a * tw));
            }
        }

        private static void drawSkyBox(double cx, double cy, double cz, int base, double a, float t) {
            double r = 150, top = cy + 92, low = cy - 8;
            int cTop = R3.argb(base, 0.52 * a), cLow = R3.argb(0x050810, 0.10 * a), cMid = R3.argb(base, 0.28 * a);
            R3.quad(cx - r, top, cz - r, cx + r, top, cz - r, cx + r, top, cz + r, cx - r, top, cz + r, cTop);
            R3.quad(cx - r, low, cz - r, cx + r, low, cz - r, cx + r, top, cz - r, cx - r, top, cz - r, cLow, cLow, cTop, cTop);
            R3.quad(cx + r, low, cz - r, cx + r, low, cz + r, cx + r, top, cz + r, cx + r, top, cz - r, cLow, cLow, cTop, cTop);
            R3.quad(cx + r, low, cz + r, cx - r, low, cz + r, cx - r, top, cz + r, cx + r, top, cz + r, cLow, cLow, cTop, cTop);
            R3.quad(cx - r, low, cz + r, cx - r, low, cz - r, cx - r, top, cz - r, cx - r, top, cz + r, cLow, cLow, cTop, cTop);
            double mid = cy + 18;
            R3.ring(cx, mid + 10 + Math.sin(t * 0.6) * 2, cz, 35, 142, 128, R3.argb(base, 0.06 * a), cMid, t * 0.025);
        }

        private static double hash(int i, int salt) { long x = i * 1103515245L + salt * 12345L; x ^= (x >>> 16); return (x & 0xFFFF) / 65535.0; }
    }

    // ================================================================== projectile trails
    public static final class ProjectileTrails extends Module {
        final Setting.Color color = add(new Setting.Color("Farbe", 11));
        final Setting.Num length = add(new Setting.Num("Länge", 5, 40, 1, 18, ""));
        private final Map<Integer, ArrayDeque<double[]>> trails = new HashMap<>();
        ProjectileTrails() { super("projectiletrails", "Geschoss-Spuren", "Leuchtende Spuren hinter Pfeilen, Perlen und Dreizacken", Category.VISUALS); icon("arrow"); fresh(); }
        private static boolean projectile(String id) {
            return id.equals("arrow") || id.equals("spectral_arrow") || id.equals("trident") || id.equals("ender_pearl") || id.equals("snowball")
                    || id.equals("egg") || id.equals("splash_potion") || id.equals("lingering_potion") || id.equals("wind_charge") || id.equals("fireball");
        }
        @Override public void tick() {
            java.util.Set<Integer> alive = new java.util.HashSet<>();
            for (class_1297 e : Mc.entities()) {
                if (!projectile(Mc.typeId(e))) continue;
                int id = Mc.id(e);
                alive.add(id);
                ArrayDeque<double[]> q = trails.computeIfAbsent(id, k -> new ArrayDeque<>());
                double[] last = q.peekLast();
                double[] now = {Mc.x(e), Mc.y(e), Mc.z(e)};
                if (last == null || Math.abs(last[0] - now[0]) + Math.abs(last[1] - now[1]) + Math.abs(last[2] - now[2]) > 0.02) q.add(now);
                while (q.size() > length.getI()) q.poll();
            }
            trails.keySet().retainAll(alive);
        }
        void render() {
            for (Map.Entry<Integer, ArrayDeque<double[]>> e : trails.entrySet()) {
                List<double[]> pts = new ArrayList<>(e.getValue());
                int n = pts.size();
                for (int i = 1; i < n; i++) {
                    double[] a = pts.get(i - 1), b = pts.get(i);
                    int rgb = color.argb(e.getKey() * 0.1 + i * 0.03) & 0xFFFFFF;
                    double fa = (i - 1) / (double) n, fb = i / (double) n;
                    R3.line(a[0], a[1], a[2], b[0], b[1], b[2], 0.08 + 0.1 * fb, R3.argb(rgb, 0.8 * fa), R3.argb(rgb, 0.8 * fb));
                }
            }
        }
    }

    // ================================================================== camera
    public static final class DynamicCamera extends Module {
        final Setting.Num strength = add(new Setting.Num("Neigung", 0.5, 4, 0.1, 1.5, "°"));
        final Setting.Bool landShake = add(new Setting.Bool("Landungs-Wackeln", true));
        final Setting.Bool sprintFov = add(new Setting.Bool("Sprint-FOV-Boost", false));
        final Setting.Num fovBoost = add(new Setting.Num("FOV-Boost", 1, 10, 1, 3, "°"));
        private float roll = 0, shake = 0, fovExtra = 0;
        private boolean wasOnGround;
        private double prevVelY;
        DynamicCamera() { super("dynamiccamera", "Dynamische Kamera", "Kamera-Neigung, Landungs-Wackeln und Sprint-FOV", Category.VISUALS); icon("tilt"); fresh(); }
        public float roll() {
            class_746 p = Mc.player();
            if (p == null || !enabled) { roll *= 0.8f; shake *= 0.85f; return roll + shake; }
            double[] v = Mc.velocity(p);
            double yaw = Math.toRadians(Mc.yaw(p));
            double side = v[0] * Math.cos(yaw) + v[2] * Math.sin(yaw);
            float target = (float) (-side * 8 * strength.get());
            target = Math.max(-strength.getF() * 2, Math.min(strength.getF() * 2, target));
            roll += (target - roll) * 0.08f;
            if (landShake.get()) {
                boolean ground = p.method_24828();
                if (ground && !wasOnGround && prevVelY < -0.4) {
                    shake = (float) Math.min(3.0, Math.abs(prevVelY) * 2.5);
                }
                wasOnGround = ground;
                prevVelY = v[1];
            }
            shake *= 0.82f;
            float shakeOff = shake * (float) Math.sin(System.currentTimeMillis() / 40.0);
            return roll + shakeOff;
        }
        public float fovModify(float fov) {
            if (!enabled || !sprintFov.get()) { fovExtra *= 0.85f; return fov + fovExtra; }
            class_746 p = Mc.player();
            float target = (p != null && p.method_5624()) ? fovBoost.getF() : 0;
            fovExtra += (target - fovExtra) * 0.12f;
            return fov + fovExtra;
        }
    }

    public static final class LowFire extends Module {
        final Setting.Num height = add(new Setting.Num("Absenken", 0.1, 0.6, 0.05, 0.3, ""));
        public double height() { return height.get(); }
        LowFire() { super("lowfire", "Low Fire", "Feuer-Overlay tiefer, damit du besser siehst", Category.VISUALS); icon("flame"); fresh(); }
    }
}
