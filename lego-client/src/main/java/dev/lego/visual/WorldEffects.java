package dev.lego.visual;

import dev.lego.core.Category;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import java.util.ArrayList;
import java.util.List;

public final class WorldEffects extends Module {
   private final List<Module> fx = new ArrayList<>();
   private final List<Setting.Bool> on = new ArrayList<>();
   private final List<Boolean> last = new ArrayList<>();
   private boolean[] snapshot;

   private WorldEffects(List<Module> effects) {
      super(
         "worldeffects",
         "Welt-Effekte",
         "Alle Partikel in der Welt in einem Modul: Glühwürmchen, Vogelschwarm, Schnee, Laternen ... einzeln an/aus",
         Category.VISUALS
      );
      this.icon("sparkle");
      this.fresh();
      this.enabled = true;

      for (Module m : effects) {
         this.fx.add(m);
         this.on.add(this.add(new Setting.Bool(m.name, m.enabled)));
         this.last.add(m.enabled);
      }

      this.add(new Setting.Action("Alle", "Alle aus", () -> {
         for (Setting.Bool b : this.on) {
            b.value = false;
         }
      }));
   }

   public static void register() {
      List<Module> list = new ArrayList<>();

      for (Module m : Modules.ALL) {
         if (m.category == Category.WORLD_FX) {
            list.add(m);
         }
      }

      Modules.register(new WorldEffects(list));
   }

   @Override
   public void tick() {
      for (int i = 0; i < this.fx.size(); i++) {
         Module m = this.fx.get(i);
         Setting.Bool b = this.on.get(i);
         if (b.value != this.last.get(i)) {
            m.setEnabled(b.value);
            Modules.scheduleSave();
         } else if (m.enabled != b.value) {
            b.value = m.enabled;
         }

         this.last.set(i, b.value);
      }
   }

   @Override
   public void onDisable() {
      this.snapshot = new boolean[this.fx.size()];

      for (int i = 0; i < this.fx.size(); i++) {
         this.snapshot[i] = this.fx.get(i).enabled;
         this.fx.get(i).setEnabled(false);
      }
   }

   @Override
   public void onEnable() {
      if (this.snapshot != null) {
         for (int i = 0; i < this.fx.size() && i < this.snapshot.length; i++) {
            this.fx.get(i).setEnabled(this.snapshot[i]);
         }
      }
   }
}
