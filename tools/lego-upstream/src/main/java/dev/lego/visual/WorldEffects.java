package dev.lego.visual;

import java.util.ArrayList;
import java.util.List;

import dev.lego.core.Category;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;

/** One module for all particles in the world (fireflies, birds, snow, lanterns ...): switch each effect on/off here. */
public final class WorldEffects extends Module {
    private final List<Module> fx = new ArrayList<>();
    private final List<Setting.Bool> on = new ArrayList<>();
    private final List<Boolean> last = new ArrayList<>();
    private boolean[] snapshot;

    private WorldEffects(List<Module> effects) {
        super("worldeffects", "Welt-Effekte", "Alle Partikel in der Welt in einem Modul: Glühwürmchen, Vogelschwarm, Schnee, Laternen ... einzeln an/aus", Category.VISUALS);
        icon("sparkle");
        fresh();
        enabled = true;
        for (Module m : effects) {
            fx.add(m);
            on.add(add(new Setting.Bool(m.name, m.enabled)));
            last.add(m.enabled);
        }
        add(new Setting.Action("Alle", "Alle aus", () -> { for (Setting.Bool b : on) b.value = false; }));
    }

    /** Call after Ambient + WorldFx registered their effects. */
    public static void register() {
        List<Module> list = new ArrayList<>();
        for (Module m : Modules.ALL) if (m.category == Category.WORLD_FX) list.add(m);
        Modules.register(new WorldEffects(list));
    }

    @Override public void tick() {
        for (int i = 0; i < fx.size(); i++) {
            Module m = fx.get(i);
            Setting.Bool b = on.get(i);
            if (b.value != last.get(i)) { m.setEnabled(b.value); Modules.scheduleSave(); }
            else if (m.enabled != b.value) b.value = m.enabled;
            last.set(i, b.value);
        }
    }

    @Override public void onDisable() {
        snapshot = new boolean[fx.size()];
        for (int i = 0; i < fx.size(); i++) { snapshot[i] = fx.get(i).enabled; fx.get(i).setEnabled(false); }
    }

    @Override public void onEnable() {
        if (snapshot == null) return;
        for (int i = 0; i < fx.size() && i < snapshot.length; i++) fx.get(i).setEnabled(snapshot[i]);
    }
}