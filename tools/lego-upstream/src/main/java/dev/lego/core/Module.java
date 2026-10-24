package dev.lego.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Base class of all client modules. */
public abstract class Module {
    public final String id, name, description;
    public final Category category;
    public boolean enabled;
    /** GLFW key code toggling the module (-1 = none). */
    public int key = -1;
    /** Hidden modules are not shown in the click GUI (for performance or decluttering). */
    public boolean hidden;
    /** User-defined display name (null = use default name). */
    public String customName;
    public final List<Setting> settings = new ArrayList<>();
    public String icon;
    /** Shows a "NEU" badge in the mod menu. */
    public boolean fresh = false;
    private final Map<String, Object> defaults = new LinkedHashMap<>();
    private boolean defaultOn;

    protected Module(String id, String name, String description, Category category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.icon = category.icon;
    }

    protected <T extends Setting> T add(T s) { settings.add(s); return s; }

    public String displayName() {
        if (customName != null && !customName.isEmpty()) return customName;
        String t = Lang.get("mod." + id);
        return t != null ? t : name;
    }

    public String displayDesc() {
        String t = Lang.get("mod." + id + ".desc");
        return t != null ? t : description;
    }

    public Module icon(String i) { icon = i; return this; }
    public Module fresh() { fresh = true; return this; }

    public void setEnabled(boolean v) {
        if (v == enabled) return;
        enabled = v;
        try {
            if (v) onEnable(); else onDisable();
        } catch (Throwable t) {
            dev.lego.LegoClient.LOG("Fehler in " + id + ": " + t);
        }
    }

    public void toggle() { setEnabled(!enabled); }

    /** Modules that are "tools" (actions) show a button instead of a toggle. */
    public boolean isToggleable() { return true; }

    public void onEnable() { }
    public void onDisable() { }
    /** Called every client tick while enabled. */
    public void tick() { }

    // ------------------------------------------------------------------ defaults
    void captureDefaults() {
        defaultOn = enabled;
        for (Setting s : settings) { Object v = s.save(); if (v != null) defaults.put(s.name, v); }
    }

    public void resetSettings() {
        for (Setting s : settings) if (defaults.containsKey(s.name)) s.load(defaults.get(s.name));
    }

    public boolean defaultOn() { return defaultOn; }
}
