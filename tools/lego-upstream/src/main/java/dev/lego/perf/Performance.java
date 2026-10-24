package dev.lego.perf;

import java.util.HashMap;
import java.util.Map;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Toasts;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_315;
import net.minecraft.class_4063;
import net.minecraft.class_4066;
import net.minecraft.class_638;
import net.minecraft.class_7172;

/**
 * FPS optimisations. Honest note: a client cannot lower your ping (that is network distance to the server);
 * these modules only raise FPS / smoothness.
 */
public final class Performance {
    private Performance() { }

    public static FpsBoost boost;
    public static Moltron moltron;
    public static EntityCulling culling;
    public static ParticleLimit particles;
    public static BackgroundFps background;
    public static FreeMemory memory;
    public static Monitor monitor;
    public static Guard guard;

    /** True while heavy extras should pause: either guard sees low FPS or Moltron is active. */
    public static boolean lowMode() { return (guard != null && guard.enabled && guard.low) || potato(); }

    /** True when Moltron potato mode is active. */
    public static boolean potato() { return moltron != null && moltron.enabled; }

    public static void registerAll() {
        boost = Modules.register(new FpsBoost());
        moltron = Modules.register(new Moltron());
        culling = Modules.register(new EntityCulling());
        particles = Modules.register(new ParticleLimit());
        background = Modules.register(new BackgroundFps());
        memory = Modules.register(new FreeMemory());
        monitor = Modules.register(new Monitor());
        guard = Modules.register(new Guard());
    }

    // ================================================================== preset
    public static final class FpsBoost extends Module {
        final Setting.Mode level = add(new Setting.Mode("Stufe", 1, "Leicht", "Stark", "Maximum", "EXTREM"));
        final Setting.Bool clouds = add(new Setting.Bool("Wolken aus", true));
        final Setting.Bool shadows = add(new Setting.Bool("Entity-Schatten aus", true));
        final Setting.Bool bobbing = add(new Setting.Bool("View-Bobbing aus", false));
        final Setting.Bool fovEffect = add(new Setting.Bool("FOV-Effekte aus", true));
        /** Original option values (as text) - persisted so a restart still restores them. */
        private final Map<String, String> saved = new HashMap<>();

        FpsBoost() {
            super("fpsboost", "FPS-Boost", "VSync aus, FPS unbegrenzt, Grafik auf Leistung · Stufe EXTREM holt das Maximum raus · Ausschalten stellt alles zurück", Category.PERFORMANCE);
            icon("boost"); fresh();
            Modules.persist("fpsboost", new Modules.Persist() {
                @Override public Object save() { return new java.util.LinkedHashMap<String, Object>(saved); }
                @SuppressWarnings("unchecked")
                @Override public void load(Object o) {
                    if (!(o instanceof Map)) return;
                    saved.clear();
                    for (Map.Entry<String, Object> e : ((Map<String, Object>) o).entrySet()) saved.put(e.getKey(), String.valueOf(e.getValue()));
                }
            });
        }

        /** Option entries: key -> (option, parser for the saved text). */
        private interface Parse { Object of(String s); }
        private final Map<String, Object[]> opts = new java.util.LinkedHashMap<>();

        @SuppressWarnings({"unchecked", "rawtypes"})
        private void set(String key, class_7172 opt, Object value, Parse parse) {
            if (opt == null) return;
            opts.put(key, new Object[]{opt, parse});
            Object cur = opt.method_41753();
            if (!saved.containsKey(key)) saved.put(key, cur instanceof Enum ? ((Enum<?>) cur).name() : String.valueOf(cur));
            try { opt.method_41748(value); } catch (Throwable t) { dev.lego.LegoClient.LOG("FPS-Boost " + key + ": " + t); }
        }

        private static final Parse INT = s -> (int) Double.parseDouble(s), BOOL = Boolean::parseBoolean, DBL = Double::parseDouble;

        /** Lower an Integer option to at most max. */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private void capInt(String key, class_7172 opt, int max) {
            if (opt == null) return;
            Object cur = opt.method_41753();
            if (cur instanceof Integer && (Integer) cur > max) set(key, opt, max, INT);
        }

        @Override public void onEnable() {
            if (Mc.mc() == null || Mc.options() == null) return;     // game start: options are already stored in options.txt
            apply();
            Toasts.show("boost", "FPS-Boost an", level.get() + " · VSync aus, FPS unbegrenzt · wird beim Ausschalten zurückgesetzt", 0xFF1ED760, 3500);
        }

        void apply() {
            class_315 o = Mc.options();
            try {
                int l = level.index;
                // the biggest one first: VSync caps you at your monitor's refresh rate
                set("vsync", o.method_42433(), Boolean.FALSE, BOOL);
                set("maxFps", o.method_42524(), 260, INT);                         // 260 = unlimited
                set("particles", o.method_42475(), l == 0 ? class_4066.field_18198 : class_4066.field_18199, s -> class_4066.valueOf(s));
                if (clouds.get()) set("clouds", o.method_42528(), l == 0 ? class_4063.field_18163 : class_4063.field_18162, s -> class_4063.valueOf(s));
                if (shadows.get()) set("shadows", o.method_42435(), Boolean.FALSE, BOOL);
                if (bobbing.get() || l >= 2) set("bobView", o.method_42449(), Boolean.FALSE, BOOL);
                if (fovEffect.get()) set("fovEffects", o.method_42454(), 0.0, DBL);
                set("blend", o.method_41805(), l == 0 ? 2 : (l == 1 ? 1 : 0), INT);
                set("menuBlur", o.method_57702(), 0, INT);                         // blur behind menus = full screen pass
                set("improvedTransparency", o.method_75337(), Boolean.FALSE, BOOL);  // "fabulous" transparency
                capInt("weather", o.method_75333(), l == 0 ? 8 : 5);
                capInt("anisotropy", o.method_76247(), l == 0 ? 2 : 1);
                if (l >= 3) {                                                        // EXTREM: everything to the minimum
                    set("particles", o.method_42475(), class_4066.field_18199, s -> class_4066.valueOf(s));
                    set("clouds", o.method_42528(), class_4063.field_18162, s -> class_4063.valueOf(s));
                    set("shadows", o.method_42435(), Boolean.FALSE, BOOL);
                    set("chunkFade", o.method_76253(), 0.0, DBL);
                    capInt("weather", o.method_75333(), 3);
                    capInt("renderDist", o.method_42503(), 6);
                    capInt("simDist", o.method_42510(), 5);
                    capInt("cloudDist", o.method_71270(), 2);
                    set("entityDist", o.method_42517(), 0.5, DBL);
                    set("ao", o.method_41792(), Boolean.FALSE, BOOL);
                    set("cutoutLeaves", o.method_75334(), Boolean.FALSE, BOOL);
                    capInt("mipmaps", o.method_42563(), 0);
                    capInt("anisotropy", o.method_76247(), 1);
                } else if (l >= 1) {
                    set("entityDist", o.method_42517(), l == 1 ? 0.75 : 0.5, DBL);
                    set("ao", o.method_41792(), Boolean.FALSE, BOOL);
                    set("cutoutLeaves", o.method_75334(), Boolean.FALSE, BOOL);
                    capInt("mipmaps", o.method_42563(), l == 1 ? 2 : 0);
                    capInt("renderDist", o.method_42503(), l == 1 ? 12 : 8);
                    capInt("simDist", o.method_42510(), l == 1 ? 8 : 5);
                    capInt("cloudDist", o.method_71270(), 8);
                }
                o.method_1640();   // write options.txt
                Modules.scheduleSave();
            } catch (Throwable t) { dev.lego.LegoClient.LOG("FPS-Boost: " + t); }
        }

        /** Scale for the client's own world effects (particles etc.). */
        public float fxScale() { return !enabled ? 1f : level.index == 0 ? 0.8f : level.index == 1 ? 0.55f : level.index == 2 ? 0.3f : 0f; }

        /** EXTREM: the client's own world effects are switched off completely. */
        public boolean extreme() { return enabled && level.index >= 3; }

        /** Block entities (chests, signs, banners ...) further away than this are not drawn (0 = no limit). */
        public double blockEntityDist() { return !enabled ? 0 : level.index >= 3 ? 24 : level.index == 2 ? 40 : 0; }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override public void onDisable() {
            if (Mc.mc() == null || Mc.options() == null) return;
            class_315 o = Mc.options();
            // entries of older versions (before the table existed)
            java.util.Map<String, Object[]> all = new java.util.LinkedHashMap<>(opts);
            Object[][] known = {
                    {"vsync", o.method_42433(), BOOL}, {"maxFps", o.method_42524(), INT},
                    {"particles", o.method_42475(), (Parse) x -> class_4066.valueOf(x)}, {"clouds", o.method_42528(), (Parse) x -> class_4063.valueOf(x)},
                    {"shadows", o.method_42435(), BOOL}, {"bobView", o.method_42449(), BOOL}, {"fovEffects", o.method_42454(), DBL},
                    {"blend", o.method_41805(), INT}, {"menuBlur", o.method_57702(), INT},
                    {"improvedTransparency", o.method_75337(), BOOL}, {"weather", o.method_75333(), INT}, {"anisotropy", o.method_76247(), INT},
                    {"entityDist", o.method_42517(), DBL}, {"ao", o.method_41792(), BOOL}, {"cutoutLeaves", o.method_75334(), BOOL},
                    {"chunkFade", o.method_76253(), DBL}, {"mipmaps", o.method_42563(), INT}, {"renderDist", o.method_42503(), INT}, {"simDist", o.method_42510(), INT}, {"cloudDist", o.method_71270(), INT}};
            for (Object[] k : known) all.putIfAbsent((String) k[0], new Object[]{k[1], k[2]});
            for (Map.Entry<String, String> e : saved.entrySet()) {
                Object[] op = all.get(e.getKey());
                if (op == null || op[0] == null) continue;
                try { ((class_7172) op[0]).method_41748(((Parse) op[1]).of(e.getValue())); } catch (Throwable t) { dev.lego.LegoClient.LOG("FPS-Boost restore " + e.getKey() + ": " + t); }
            }
            try { o.method_1640(); } catch (Throwable ignored) { }
            saved.clear();
            Modules.scheduleSave();
            Toasts.show("boost", "FPS-Boost aus", "Grafik-Einstellungen wiederhergestellt", 0xFF8A8F99, 2500);
        }
    }

    // ================================================================== MOLTRON - potato mode
    public static final class Moltron extends Module {
        final Setting.Bool killVisuals = add(new Setting.Bool("Visuelle Module pausieren", true));
        final Setting.Num renderDist = add(new Setting.Num("Render-Distanz", 2, 6, 1, 2, " Chunks"));
        final Setting.Num simDist = add(new Setting.Num("Sim-Distanz", 2, 5, 1, 2, " Chunks"));
        final Setting.Num entityDist = add(new Setting.Num("Entity-Distanz", 0.25, 1.0, 0.25, 0.25, "x"));
        private final Map<String, String> saved = new HashMap<>();
        private final java.util.Set<String> disabledMods = new java.util.HashSet<>();
        private static final java.util.Set<String> KEEP = java.util.Set.of(
                "fullbright", "zoom", "nohurtcam", "lowfire", "crosshair", "timechanger", "clearweather", "blockoutline", "damagenum");

        Moltron() {
            super("moltron", "Moltron", "Potato-Grafik: alles auf Minimum, visuelle Module aus, GPU-Last minimal · macht aus 200 FPS 500+", Category.PERFORMANCE);
            icon("boost"); fresh();
            Modules.persist("moltron", new Modules.Persist() {
                @Override public Object save() {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("opts", new java.util.LinkedHashMap<>(saved));
                    m.put("mods", new java.util.ArrayList<>(disabledMods));
                    return m;
                }
                @SuppressWarnings("unchecked")
                @Override public void load(Object o) {
                    if (!(o instanceof Map)) return;
                    Map<String, Object> m = (Map<String, Object>) o;
                    saved.clear(); disabledMods.clear();
                    Object os = m.get("opts");
                    if (os instanceof Map) for (Map.Entry<String, Object> e : ((Map<String, Object>) os).entrySet()) saved.put(e.getKey(), String.valueOf(e.getValue()));
                    Object ms = m.get("mods");
                    if (ms instanceof java.util.List) for (Object v : (java.util.List<?>) ms) disabledMods.add(String.valueOf(v));
                }
            });
        }

        private interface Parse { Object of(String s); }
        private final Map<String, Object[]> opts = new java.util.LinkedHashMap<>();
        private static final Parse INT = s -> (int) Double.parseDouble(s), BOOL = Boolean::parseBoolean, DBL = Double::parseDouble;

        @SuppressWarnings({"unchecked", "rawtypes"})
        private void set(String key, class_7172 opt, Object value, Parse parse) {
            if (opt == null) return;
            opts.put(key, new Object[]{opt, parse});
            Object cur = opt.method_41753();
            if (!saved.containsKey(key)) saved.put(key, cur instanceof Enum ? ((Enum<?>) cur).name() : String.valueOf(cur));
            try { opt.method_41748(value); } catch (Throwable t) { dev.lego.LegoClient.LOG("Moltron " + key + ": " + t); }
        }

        @Override public void onEnable() {
            if (Mc.mc() == null || Mc.options() == null) return;
            class_315 o = Mc.options();
            try {
                set("vsync", o.method_42433(), Boolean.FALSE, BOOL);
                set("maxFps", o.method_42524(), 260, INT);
                set("particles", o.method_42475(), class_4066.field_18199, s -> class_4066.valueOf(s));
                set("clouds", o.method_42528(), class_4063.field_18162, s -> class_4063.valueOf(s));
                set("shadows", o.method_42435(), Boolean.FALSE, BOOL);
                set("bobView", o.method_42449(), Boolean.FALSE, BOOL);
                set("fovEffects", o.method_42454(), 0.0, DBL);
                set("blend", o.method_41805(), 0, INT);
                set("menuBlur", o.method_57702(), 0, INT);
                set("improvedTransparency", o.method_75337(), Boolean.FALSE, BOOL);
                set("weather", o.method_75333(), 0, INT);
                set("anisotropy", o.method_76247(), 1, INT);
                set("chunkFade", o.method_76253(), 0.0, DBL);
                set("renderDist", o.method_42503(), renderDist.getI(), INT);
                set("simDist", o.method_42510(), simDist.getI(), INT);
                set("cloudDist", o.method_71270(), 2, INT);
                set("entityDist", o.method_42517(), entityDist.get(), DBL);
                set("ao", o.method_41792(), Boolean.FALSE, BOOL);
                set("cutoutLeaves", o.method_75334(), Boolean.FALSE, BOOL);
                set("mipmaps", o.method_42563(), 0, INT);
                o.method_1640();
            } catch (Throwable t) { dev.lego.LegoClient.LOG("Moltron: " + t); }

            if (killVisuals.get()) {
                disabledMods.clear();
                for (Module m : Modules.ALL) {
                    if (m == this || !m.enabled) continue;
                    if ((m.category == Category.VISUALS || m.category == Category.WORLD_FX) && !KEEP.contains(m.id)) {
                        disabledMods.add(m.id);
                        m.setEnabled(false);
                    }
                }
                if (boost != null && boost.enabled) { disabledMods.add(boost.id); boost.setEnabled(false); }
            }

            new Thread(System::gc, "Moltron-GC").start();
            Modules.scheduleSave();
            Toasts.show("moltron", "MOLTRON AN", "Potato-Grafik · RD " + renderDist.getI() + " · " + disabledMods.size() + " Module pausiert", 0xFF1ED760, 4000);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override public void onDisable() {
            if (Mc.mc() == null || Mc.options() == null) { disabledMods.clear(); saved.clear(); return; }
            class_315 o = Mc.options();
            java.util.Map<String, Object[]> all = new java.util.LinkedHashMap<>(opts);
            Object[][] known = {
                    {"vsync", o.method_42433(), BOOL}, {"maxFps", o.method_42524(), INT},
                    {"particles", o.method_42475(), (Parse) x -> class_4066.valueOf(x)}, {"clouds", o.method_42528(), (Parse) x -> class_4063.valueOf(x)},
                    {"shadows", o.method_42435(), BOOL}, {"bobView", o.method_42449(), BOOL}, {"fovEffects", o.method_42454(), DBL},
                    {"blend", o.method_41805(), INT}, {"menuBlur", o.method_57702(), INT},
                    {"improvedTransparency", o.method_75337(), BOOL}, {"weather", o.method_75333(), INT}, {"anisotropy", o.method_76247(), INT},
                    {"entityDist", o.method_42517(), DBL}, {"ao", o.method_41792(), BOOL}, {"cutoutLeaves", o.method_75334(), BOOL},
                    {"chunkFade", o.method_76253(), DBL}, {"mipmaps", o.method_42563(), INT}, {"renderDist", o.method_42503(), INT},
                    {"simDist", o.method_42510(), INT}, {"cloudDist", o.method_71270(), INT}};
            for (Object[] k : known) all.putIfAbsent((String) k[0], new Object[]{k[1], k[2]});
            for (Map.Entry<String, String> e : saved.entrySet()) {
                Object[] op = all.get(e.getKey());
                if (op == null || op[0] == null) continue;
                try { ((class_7172) op[0]).method_41748(((Parse) op[1]).of(e.getValue())); } catch (Throwable t) { dev.lego.LegoClient.LOG("Moltron restore " + e.getKey() + ": " + t); }
            }
            try { o.method_1640(); } catch (Throwable ignored) { }

            int reEnabled = 0;
            for (String id : disabledMods) {
                Module m = Modules.get(id);
                if (m != null && !m.enabled) { m.setEnabled(true); reEnabled++; }
            }
            saved.clear();
            disabledMods.clear();
            Modules.scheduleSave();
            Toasts.show("moltron", "MOLTRON AUS", "Grafik zurückgesetzt · " + reEnabled + " Module reaktiviert", 0xFF8A8F99, 3000);
        }

        public double blockEntityDist() { return enabled ? 16 : 0; }

        public void drawHud(int W, int H) {
            if (!enabled) return;
            int S = dev.lego.ui.Gx.S;
            float fs = 8f * S;
            int fps = dev.lego.core.Mc.fps();
            String txt = "POTATO  " + fps + " FPS";
            int tw = Math.round(dev.lego.ui.Gx.width(txt, fs, dev.lego.ui.Fonts.BOLD)) + Math.round(16 * S);
            int h = Math.round(18 * S), r = Math.round(6 * S);
            int x = W - tw - Math.round(8 * S), y = Math.round(8 * S);
            dev.lego.ui.Gx.rect(x, y, tw, h, r, 0xCC111111);
            int col = fps >= 240 ? 0xFF1ED760 : fps >= 120 ? 0xFFFFC53D : fps >= 60 ? 0xFFFF9500 : 0xFFFF4D5E;
            dev.lego.ui.Gx.rect(x, y, Math.round(4 * S), h, r, col);
            dev.lego.ui.Gx.textCenter(txt, x + tw / 2f + Math.round(2 * S), y + h / 2f, fs, dev.lego.ui.Fonts.BOLD, col);
        }
    }

    // ================================================================== entity culling (called from EntityRenderManagerMixin)
    public static final class EntityCulling extends Module {
        final Setting.Bool occlusion = add(new Setting.Bool("Hinter Wänden nicht rendern", true));
        final Setting.Num itemDist = add(new Setting.Num("Items / XP bis", 8, 64, 4, 32, " Blöcke"));
        final Setting.Num mobDist = add(new Setting.Num("Mobs bis", 16, 128, 8, 96, " Blöcke"));
        final Setting.Bool keepPlayers = add(new Setting.Bool("Spieler immer rendern", true));
        private final Map<Integer, long[]> cache = new HashMap<>();
        private long frame = 0;

        EntityCulling() { super("entityculling", "Entity-Culling", "Rendert Mobs/Items nicht, wenn sie weit weg oder hinter Blöcken sind", Category.PERFORMANCE); icon("entity"); enabled = true; fresh(); }

        @Override public void tick() {
            frame++;
            if (frame % 200 == 0) cache.entrySet().removeIf(e -> frame - e.getValue()[0] > 100);
        }

        /** @return false = skip rendering. Camera position cx/cy/cz. */
        public boolean shouldRender(class_1297 e, double cx, double cy, double cz) {
            if (e == Mc.player()) return true;
            if (e instanceof class_1657 && keepPlayers.get()) return true;
            if (e.method_5851()) return true;              // glowing entities stay visible
            double dx = Mc.x(e) - cx, dy = Mc.y(e) + Mc.height(e) / 2 - cy, dz = Mc.z(e) - cz;
            double d2 = dx * dx + dy * dy + dz * dz;
            String t = Mc.typeId(e);
            boolean small = t.equals("item") || t.equals("experience_orb") || t.equals("arrow");
            boolean ext = boost != null && boost.extreme();
            double max = small ? (ext ? Math.min(16, itemDist.get()) : itemDist.get()) : (ext ? Math.min(48, mobDist.get()) : mobDist.get());
            if (!(e instanceof class_1657) && d2 > max * max) return false;
            if (!occlusion.get() || d2 < 16) return true;
            float w = Mc.width(e), h = Mc.height(e);
            if (w > 3 || h > 4) return true;                 // big entities (dragons, boats of boats ...) - never cull
            int id = Mc.id(e);
            long[] c = cache.get(id);
            if (c != null && frame - c[0] < 4) return c[1] == 1;
            class_638 world = Mc.world();
            boolean vis = world == null
                    || visible(world, cx, cy, cz, Mc.x(e), Mc.y(e) + h * 0.9, Mc.z(e))
                    || visible(world, cx, cy, cz, Mc.x(e), Mc.y(e) + h * 0.2, Mc.z(e))
                    || visible(world, cx, cy, cz, Mc.x(e) + w / 2, Mc.y(e) + h / 2, Mc.z(e) + w / 2)
                    || visible(world, cx, cy, cz, Mc.x(e) - w / 2, Mc.y(e) + h / 2, Mc.z(e) - w / 2);
            cache.put(id, new long[]{frame, vis ? 1 : 0});
            return vis;
        }

        /** Voxel walk (Amanatides-Woo) - true if no opaque full block is in between. */
        private static boolean visible(class_638 w, double x0, double y0, double z0, double x1, double y1, double z1) {
            double dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
            int x = (int) Math.floor(x0), y = (int) Math.floor(y0), z = (int) Math.floor(z0);
            int ex = (int) Math.floor(x1), ey = (int) Math.floor(y1), ez = (int) Math.floor(z1);
            int sx = dx > 0 ? 1 : -1, sy = dy > 0 ? 1 : -1, sz = dz > 0 ? 1 : -1;
            double tdx = dx == 0 ? Double.MAX_VALUE : Math.abs(1 / dx), tdy = dy == 0 ? Double.MAX_VALUE : Math.abs(1 / dy), tdz = dz == 0 ? Double.MAX_VALUE : Math.abs(1 / dz);
            double tmx = dx == 0 ? Double.MAX_VALUE : ((sx > 0 ? (x + 1 - x0) : (x0 - x)) * tdx);
            double tmy = dy == 0 ? Double.MAX_VALUE : ((sy > 0 ? (y + 1 - y0) : (y0 - y)) * tdy);
            double tmz = dz == 0 ? Double.MAX_VALUE : ((sz > 0 ? (z + 1 - z0) : (z0 - z)) * tdz);
            class_2338.class_2339 pos = new class_2338.class_2339();
            for (int i = 0; i < 256; i++) {
                if (x == ex && y == ey && z == ez) return true;
                if (tmx < tmy && tmx < tmz) { x += sx; tmx += tdx; }
                else if (tmy < tmz) { y += sy; tmy += tdy; }
                else { z += sz; tmz += tdz; }
                if (x == ex && y == ey && z == ez) return true;
                pos.method_10103(x, y, z);
                if (w.method_8320(pos).method_26216()) return false;
            }
            return true;
        }
    }

    // ================================================================== particles (ParticleManagerMixin)
    public static final class ParticleLimit extends Module {
        final Setting.Num perTick = add(new Setting.Num("Max. neue pro Tick", 20, 400, 10, 120, ""));
        private int count = 0;
        ParticleLimit() { super("particlelimit", "Partikel-Limit", "Begrenzt Partikel-Explosionen (TNT, Farmen) - weniger Ruckler", Category.PERFORMANCE); icon("particles"); enabled = true; fresh(); }
        @Override public void tick() { count = 0; }
        private long tickId;
        public boolean allow() {
            long id = System.currentTimeMillis() / 50;
            if (id != tickId) { tickId = id; count = 0; }
            boolean ext = (boost != null && boost.extreme()) || lowMode();
            if (!enabled && !ext) return true;
            return ++count <= (enabled ? (ext ? Math.min(40, perTick.getI()) : perTick.getI()) : 40);
        }
    }

    // ================================================================== background fps (InactivityFpsLimiterMixin)
    public static final class BackgroundFps extends Module {
        final Setting.Num fps = add(new Setting.Num("FPS im Hintergrund", 5, 60, 5, 15, ""));
        BackgroundFps() { super("backgroundfps", "Hintergrund-FPS", "Weniger FPS, wenn Minecraft nicht im Vordergrund ist (spart Strom & Hitze)", Category.PERFORMANCE); icon("fps"); enabled = true; fresh(); }
        public int limit(int vanilla) {
            if (!enabled || Mc.windowFocused()) return vanilla;
            return Math.min(vanilla, fps.getI());
        }
    }

    // ================================================================== freeze / crash guard
    public static final class Guard extends Module {
        final Setting.Bool memory = add(new Setting.Bool("RAM-Wächter", true));
        final Setting.Bool lowFps = add(new Setting.Bool("Bei Rucklern Extras pausieren", true));
        final Setting.Num limit = add(new Setting.Num("Pausieren unter", 10, 60, 5, 25, " FPS"));
        volatile boolean low;
        private long lowSince = 0, okSince = 0, nextGc = 0, nextCheck = 0;

        Guard() {
            super("guard", "Freeze-Schutz", "Gibt bei knappem RAM Speicher frei und pausiert Extras bei Rucklern, damit Spiel und Client nicht einfrieren oder abstürzen", Category.PERFORMANCE);
            icon("shield"); enabled = true; fresh();
        }

        @Override public void tick() {
            long now = System.currentTimeMillis();
            if (now < nextCheck) return;
            nextCheck = now + 500;
            if (memory.get()) {
                Runtime r = Runtime.getRuntime();
                long used = r.totalMemory() - r.freeMemory(), max = r.maxMemory();
                if (used > max * 0.92 && now >= nextGc) {
                    nextGc = now + 45_000;
                    new Thread(System::gc, "Lego-Guard-GC").start();
                    Toasts.show("memory", "RAM fast voll", (used / (1024 * 1024)) + " / " + (max / (1024 * 1024)) + " MB - Speicher wird aufgeräumt", 0xFFFFCD03, 3500);
                }
            }
            if (!lowFps.get()) { low = false; return; }
            int fps = Mc.fps();
            if (fps > 0 && fps < limit.getI() && Mc.windowFocused()) {
                okSince = 0;
                if (lowSince == 0) lowSince = now;
                if (!low && now - lowSince > 3000) { low = true; Toasts.show("boost", "Extras pausiert", "Wegen niedriger FPS - kommt zurück, sobald es wieder flüssig ist", 0xFFFFCD03, 3000); }
            } else {
                lowSince = 0;
                if (low) { if (okSince == 0) okSince = now; if (now - okSince > 5000) low = false; }
            }
        }

        @Override public void onDisable() { low = false; }
    }
    // ================================================================== monitor
    public static final class Monitor extends Module {
        Monitor() { super("perfmonitor", "Leistungs-Monitor", "Zeigt, wie viele Millisekunden pro Bild jeder Teil des Clients kostet", Category.PERFORMANCE); icon("gauge"); fresh(); }
        public void draw(int W, int H) {
            int S = dev.lego.ui.Gx.S;
            float fs = 7.4f * S;
            java.util.Map<String, double[]> m = Perf.sections();
            int w = Math.round(150 * S), lh = Math.round(11 * S), h = lh * (m.size() + 2) + Math.round(8 * S);
            int x = W - w - Math.round(8 * S), y = H / 2 - h / 2;
            dev.lego.ui.Gx.rect(x, y, w, h, Math.round(6 * S), 0xC80A0B10);
            int fps = Mc.fps();
            dev.lego.ui.Gx.text("FPS " + fps + "  ·  " + String.format(java.util.Locale.ROOT, "%.1f ms", 1000.0 / Math.max(1, fps)), x + 6 * S, y + 4 * S, fs, dev.lego.ui.Fonts.BOLD, fps >= 60 ? 0xFF1ED760 : fps >= 30 ? 0xFFFFCD03 : 0xFFFF4D5E);
            int yy = y + 4 * S + lh + 2 * S;
            for (java.util.Map.Entry<String, double[]> e : m.entrySet()) {
                double v = e.getValue()[0];
                int col = v < 1 ? 0xFFB8BDC8 : v < 4 ? 0xFFFFCD03 : 0xFFFF4D5E;
                dev.lego.ui.Gx.text(e.getKey(), x + 6 * S, yy, fs, dev.lego.ui.Fonts.MEDIUM, 0xFFE8E9EC);
                dev.lego.ui.Gx.textRight(String.format(java.util.Locale.ROOT, "%.2f ms", v), x + w - 6 * S, yy + fs * 0.45f, fs, dev.lego.ui.Fonts.BOLD, col);
                yy += lh;
            }
        }
    }

    // ================================================================== memory
    public static final class FreeMemory extends Module {
        FreeMemory() {
            super("freememory", "RAM freigeben", "Räumt ungenutzten Arbeitsspeicher auf (bei Rucklern nach langem Spielen)", Category.PERFORMANCE);
            icon("memory"); fresh();
            add(new Setting.Action("Jetzt", "RAM freigeben", FreeMemory::run));
        }
        @Override public boolean isToggleable() { return false; }
        public static void run() {
            Runtime r = Runtime.getRuntime();
            long before = r.totalMemory() - r.freeMemory();
            new Thread(() -> {
                System.gc();
                long after = r.totalMemory() - r.freeMemory();
                long freed = Math.max(0, before - after) / (1024 * 1024);
                Toasts.show("memory", "RAM freigegeben", freed + " MB frei · " + (after / (1024 * 1024)) + " / " + (r.maxMemory() / (1024 * 1024)) + " MB belegt", 0xFF00A3DA, 3500);
            }, "Lego-GC").start();
        }
    }
}
