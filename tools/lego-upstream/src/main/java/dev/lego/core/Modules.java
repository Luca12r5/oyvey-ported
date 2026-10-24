package dev.lego.core;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.lego.LegoClient;
import dev.lego.hud.HudModule;
import dev.spotifyhud.Json;
import net.fabricmc.loader.api.FabricLoader;

/** Module registry + persistence (config/legoclient.json). */
public final class Modules {
    private Modules() { }

    public static final List<Module> ALL = new ArrayList<>();

    /** Client look (click GUI + HUD panels). */
    public static String theme = "winter";
    /** The HUD keeps its own look (it is not part of the Christmas redesign). */
    public static String hudTheme = "standard";
    public static int hudAccent = 0;
    public static int accent = 1;               // index in Setting.Color.PALETTE
    private static volatile long saveAt = 0;

    public static <T extends Module> T register(T m) { ALL.add(m); m.captureDefaults(); return m; }

    /** Extra persisted data (cosmetics, emotes, games, waypoints, UI settings ...). */
    public interface Persist {
        Object save();
        void load(Object o);
    }

    private static final Map<String, Persist> EXTRA = new LinkedHashMap<>();

    public static void persist(String key, Persist p) { EXTRA.put(key, p); }

    public static List<Module> of(Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : ALL) if (m.category == c) out.add(m);
        return out;
    }

    public static Module get(String id) {
        for (Module m : ALL) if (m.id.equals(id)) return m;
        return null;
    }

    public static int accentArgb() { return argbOf(accent); }

    public static int hudAccentArgb() { return argbOf(hudAccent); }

    private static int argbOf(int accent) {
        int i = Math.max(0, Math.min(Setting.Color.PALETTE.length - 1, accent));
        if (i == Setting.Color.PALETTE.length - 1) {
            double t = System.currentTimeMillis() / 2600.0;
            return 0xFF000000 | (java.awt.Color.HSBtoRGB((float) (t - Math.floor(t)), 0.7f, 1f) & 0xFFFFFF);
        }
        return Setting.Color.PALETTE[i];
    }

    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("legoclient.json"); }

    public static void scheduleSave() { saveAt = System.currentTimeMillis() + 700; }

    public static void tickSave() {
        if (saveAt != 0 && System.currentTimeMillis() >= saveAt) { saveAt = 0; save(); }
    }

    public static synchronized void save() {
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("theme", theme);
            root.put("designVersion", 3L);
            root.put("hudTheme", hudTheme);
            root.put("hudAccent", (long) hudAccent);
            root.put("accent", (long) accent);
            Map<String, Object> mods = new LinkedHashMap<>();
            for (Module m : ALL) {
                Map<String, Object> o = new LinkedHashMap<>();
                o.put("on", m.enabled);
                o.put("key", (long) m.key);
                if (m.hidden) o.put("hidden", true);
                if (m.customName != null) o.put("customName", m.customName);
                Map<String, Object> s = new LinkedHashMap<>();
                for (Setting st : m.settings) { Object v = st.save(); if (v != null) s.put(st.name, v); }
                o.put("settings", s);
                if (m instanceof HudModule) {
                    HudModule h = (HudModule) m;
                    o.put("x", h.fx);
                    o.put("y", h.fy);
                    o.put("scale", h.scale);
                }
                mods.put(m.id, o);
            }
            root.put("modules", mods);
            for (Map.Entry<String, Persist> e : EXTRA.entrySet()) {
                try { Object v = e.getValue().save(); if (v != null) root.put(e.getKey(), v); } catch (Throwable t) { LegoClient.LOG("Speichern " + e.getKey() + ": " + t); }
            }
            Path p = path();
            Files.createDirectories(p.getParent());
            Path tmp = p.resolveSibling("legoclient.json.tmp");
            Files.writeString(tmp, Json.write(root), StandardCharsets.UTF_8);
            Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING);
        } catch (Throwable t) {
            LegoClient.LOG("Speichern fehlgeschlagen: " + t);
        }
    }

    @SuppressWarnings("unchecked")
    public static void load() {
        try {
            Path p = path();
            if (!Files.exists(p)) return;
            Object root = Json.parse(Files.readString(p, StandardCharsets.UTF_8));
            theme = Json.str(root, "theme", theme);
            accent = (int) Json.num(root, "accent", accent);
            hudTheme = Json.str(root, "hudTheme", hudTheme);
            hudAccent = (int) Json.num(root, "hudAccent", hudAccent);
            if (Json.num(root, "designVersion", 0) < 3) { hudTheme = theme; hudAccent = accent; theme = "winter"; accent = 1; }   // 3.0: new Christmas look for everybody
            if (root instanceof Map) {
                for (Map.Entry<String, Persist> e : EXTRA.entrySet()) {
                    Object v = ((Map<String, Object>) root).get(e.getKey());
                    if (v != null) try { e.getValue().load(v); } catch (Throwable t) { LegoClient.LOG("Laden " + e.getKey() + ": " + t); }
                }
            }
            Map<String, Object> mods = Json.obj(root, "modules");
            if (mods == null) return;
            for (Module m : ALL) {
                Object o = mods.get(m.id);
                if (!(o instanceof Map)) continue;
                m.key = (int) Json.num(o, "key", m.key);
                m.hidden = Json.bool(o, "hidden", false);
                m.customName = Json.str(o, "customName", null);
                Map<String, Object> s = Json.obj(o, "settings");
                if (s != null) for (Setting st : m.settings) if (s.containsKey(st.name)) st.load(s.get(st.name));
                if (m instanceof HudModule) {
                    HudModule h = (HudModule) m;
                    h.fx = Math.max(0, Math.min(1, Json.dbl(o, "x", h.fx)));
                    h.fy = Math.max(0, Math.min(1, Json.dbl(o, "y", h.fy)));
                    h.scale = Math.max(0.4, Math.min(3, Json.dbl(o, "scale", h.scale)));
                }
                boolean on = Json.bool(o, "on", m.enabled);
                if (m.isToggleable() && on != m.enabled) m.setEnabled(on);
            }
        } catch (Throwable t) {
            LegoClient.LOG("Laden fehlgeschlagen: " + t);
        }
    }
}
