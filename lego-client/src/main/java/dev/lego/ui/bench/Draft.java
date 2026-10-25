package dev.lego.ui.bench;

import dev.lego.LegoClient;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The work bench edits a draft, never the modules themselves: switching modules on or off,
 * moving sliders, picking modes, colours, keys and Custom TxT styles only changes the draft.
 * {@link #apply()} puts every change in at once (all Custom TxT styles with one resource reload).
 */
public final class Draft {
   /** Wanted on/off state per module (only modules whose state was clicked). */
   private final Map<Module, Boolean> enabled = new LinkedHashMap<>();
   /** Wanted key per module. */
   private final Map<Module, Integer> keys = new LinkedHashMap<>();
   /** Scratch copy per setting, edited by the inspector; changed while its value differs. */
   private final Map<Setting, Setting> copies = new IdentityHashMap<>();
   private final Map<Setting, Module> owner = new IdentityHashMap<>();
   /** Custom TxT: wanted style per group (folder, mypack:/vippack: id or StylesBatch.VANILLA). */
   private final Map<de.bauloo.styles.BaulooStylesClient.Tab, String> styles = new LinkedHashMap<>();
   private final Map<de.bauloo.styles.BaulooStylesClient.Tab, String> styleNames = new LinkedHashMap<>();
   /** Custom TxT cosmetics: wanted key per slot. */
   private final Map<de.bauloo.styles.Cosmetics.Slot, String> cosmetics = new LinkedHashMap<>();

   /** The style a group shows in the studio: planned, else the one that is on. */
   public String style(de.bauloo.styles.BaulooStylesClient.Tab tab, String current) {
      String s = this.styles.get(tab);
      return s != null ? s : current;
   }

   public boolean stylePlanned(de.bauloo.styles.BaulooStylesClient.Tab tab) {
      return this.styles.containsKey(tab);
   }

   /** Plans a style for a group; picking the one that is on already drops the plan. */
   public void pickStyle(de.bauloo.styles.BaulooStylesClient.Tab tab, String folder, String name, String current) {
      String want = folder == null ? dev.lego.txt.StylesBatch.VANILLA : folder;
      String now = current == null ? dev.lego.txt.StylesBatch.VANILLA : current;
      if (want.equals(now)) {
         this.styles.remove(tab);
         this.styleNames.remove(tab);
      } else {
         this.styles.put(tab, want);
         this.styleNames.put(tab, name);
      }
   }

   public String cosmetic(de.bauloo.styles.Cosmetics.Slot slot) {
      String s = this.cosmetics.get(slot);
      return s != null ? s : de.bauloo.styles.Cosmetics.saved(slot);
   }

   public boolean cosmeticPlanned(de.bauloo.styles.Cosmetics.Slot slot) {
      return this.cosmetics.containsKey(slot);
   }

   public void pickCosmetic(de.bauloo.styles.Cosmetics.Slot slot, String key) {
      if (key.equals(de.bauloo.styles.Cosmetics.saved(slot))) {
         this.cosmetics.remove(slot);
      } else {
         this.cosmetics.put(slot, key);
      }
   }

   public boolean on(Module m) {
      Boolean b = this.enabled.get(m);
      return b != null ? b : m.enabled;
   }

   public void toggle(Module m) {
      boolean next = !this.on(m);
      if (next == m.enabled) {
         this.enabled.remove(m);
      } else {
         this.enabled.put(m, next);
      }
   }

   public int key(Module m) {
      Integer k = this.keys.get(m);
      return k != null ? k : m.key;
   }

   public void setKey(Module m, int key) {
      if (key == m.key) {
         this.keys.remove(m);
      } else {
         this.keys.put(m, key);
      }
   }

   /** The setting as the inspector shows and edits it (actions stay the real thing). */
   public Setting edit(Module m, Setting real) {
      if (real instanceof Setting.Action) {
         return real;
      }
      Setting c = this.copies.get(real);
      if (c == null) {
         c = copy(real);
         if (c == null) {
            return real;
         }
         this.copies.put(real, c);
         this.owner.put(real, m);
      }
      return c;
   }

   /** Fresh copies for settings without a change (their real value may have moved meanwhile). */
   public void refresh(Module m) {
      for (Setting s : m.settings) {
         Setting c = this.copies.get(s);
         if (c != null && !changed(s, c)) {
            this.copies.remove(s);
            this.owner.remove(s);
         }
      }
   }

   public void undo(Module m, Setting real) {
      this.copies.remove(real);
      this.owner.remove(real);
   }

   private static boolean changed(Setting real, Setting copy) {
      return !Objects.equals(real.save(), copy.save());
   }

   public boolean changed(Module m) {
      if (this.enabled.containsKey(m) || this.keys.containsKey(m)) {
         return true;
      }
      for (Setting s : m.settings) {
         Setting c = this.copies.get(s);
         if (c != null && changed(s, c)) {
            return true;
         }
      }
      return false;
   }

   /** One line per change, for the plan bar at the bottom. */
   public List<Change> changes() {
      List<Change> out = new ArrayList<>();
      for (Map.Entry<Module, Boolean> e : this.enabled.entrySet()) {
         Module m = e.getKey();
         out.add(new Change(m, null, (e.getValue() ? "+ " : "− ") + m.displayName(), e.getValue() ? 1 : -1, null, null));
      }
      for (Map.Entry<Module, Integer> e : this.keys.entrySet()) {
         out.add(new Change(e.getKey(), null, e.getKey().displayName() + ": Taste " + dev.lego.ui.Keys.name(e.getValue()), 0, null, null));
      }
      for (Map.Entry<Setting, Module> e : this.owner.entrySet()) {
         Setting s = e.getKey();
         Setting c = this.copies.get(s);
         if (c != null && changed(s, c)) {
            Module m = e.getValue();
            out.add(new Change(m, s, m.displayName() + ": " + s.displayName() + " " + shown(s) + " → " + shown(c), 0, null, null));
         }
      }
      for (Map.Entry<de.bauloo.styles.BaulooStylesClient.Tab, String> e : this.styles.entrySet()) {
         String name = this.styleNames.getOrDefault(e.getKey(), "");
         out.add(new Change(null, null, e.getKey().title + ": " + (dev.lego.txt.StylesBatch.VANILLA.equals(e.getValue()) ? "Vanilla" : name), 2, e.getKey(), null));
      }
      for (Map.Entry<de.bauloo.styles.Cosmetics.Slot, String> e : this.cosmetics.entrySet()) {
         out.add(new Change(null, null, de.bauloo.styles.Cosmetics.label(e.getKey(), e.getValue()), 3, null, e.getKey()));
      }
      return out;
   }

   public int count() {
      return this.changes().size();
   }

   public boolean empty() {
      if (!this.enabled.isEmpty() || !this.keys.isEmpty() || !this.styles.isEmpty() || !this.cosmetics.isEmpty()) {
         return false;
      }
      for (Map.Entry<Setting, Setting> e : this.copies.entrySet()) {
         if (changed(e.getKey(), e.getValue())) {
            return false;
         }
      }
      return true;
   }

   /** Drops one change (the x on a chip). */
   public void drop(Change c) {
      if (c.tab() != null) {
         this.styles.remove(c.tab());
         this.styleNames.remove(c.tab());
      } else if (c.slot() != null) {
         this.cosmetics.remove(c.slot());
      } else if (c.setting() != null) {
         this.undo(c.module(), c.setting());
      } else if (c.direction() != 0) {
         this.enabled.remove(c.module());
      } else {
         this.keys.remove(c.module());
      }
   }

   public void clear() {
      this.styles.clear();
      this.styleNames.clear();
      this.cosmetics.clear();
      this.enabled.clear();
      this.keys.clear();
      this.copies.clear();
      this.owner.clear();
   }

   /**
    * Puts every change in at once: values first, then keys, then on/off. Custom TxT styles only
    * take their new state here; their sync (TxtModules.sync, next tick) loads all picked packs
    * with a single resource reload.
    */
   public int apply() {
      int n = 0;
      java.util.Set<Module> touched = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
      for (Map.Entry<Setting, Setting> e : new ArrayList<>(this.copies.entrySet())) {
         if (changed(e.getKey(), e.getValue())) {
            try {
               e.getKey().load(e.getValue().save());
               touched.add(this.owner.get(e.getKey()));
               n++;
            } catch (Throwable t) {
               LegoClient.LOG("Werkbank: " + e.getKey().name + ": " + t);
            }
         }
      }
      for (Module m : touched) {
         if (m instanceof dev.lego.txt.TxtExtras.Extra x) {
            x.push();
         }
      }
      if (!this.styles.isEmpty()) {
         dev.lego.txt.StylesBatch.apply(new LinkedHashMap<>(this.styles));
         n += this.styles.size();
      }
      for (Map.Entry<de.bauloo.styles.Cosmetics.Slot, String> e : this.cosmetics.entrySet()) {
         de.bauloo.styles.Cosmetics.save(e.getKey(), e.getValue());
         n++;
      }
      for (Map.Entry<Module, Integer> e : this.keys.entrySet()) {
         e.getKey().key = e.getValue();
         n++;
      }
      for (Map.Entry<Module, Boolean> e : this.enabled.entrySet()) {
         Module m = e.getKey();
         if (m instanceof dev.lego.txt.TxtModules.StyleModule) {
            m.enabled = e.getValue();
         } else {
            m.setEnabled(e.getValue());
         }
         n++;
      }
      this.clear();
      Modules.scheduleSave();
      LegoClient.LOG("Werkbank: " + n + " Änderungen angewendet");
      return n;
   }

   static String shown(Setting s) {
      if (s instanceof Setting.Bool b) return b.value ? "an" : "aus";
      if (s instanceof Setting.Num n) return n.display();
      if (s instanceof Setting.Mode m) return m.get();
      if (s instanceof Setting.Color c) return c.name();
      if (s instanceof Setting.Text t) return "\"" + (t.value.length() > 10 ? t.value.substring(0, 10) + "…" : t.value) + "\"";
      return "";
   }

   private static Setting copy(Setting s) {
      Setting c;
      if (s instanceof Setting.Bool b) {
         c = new Setting.Bool(b.name, b.value);
      } else if (s instanceof Setting.Num n) {
         c = new Setting.Num(n.name, n.min, n.max, n.step, n.value, n.unit);
      } else if (s instanceof Setting.Mode m) {
         c = new Setting.Mode(m.name, m.index, m.options);
      } else if (s instanceof Setting.Color col) {
         Setting.Color cc = new Setting.Color(col.name, col.index);
         cc.hue = col.hue;
         cc.sat = col.sat;
         cc.bri = col.bri;
         c = cc;
      } else if (s instanceof Setting.Text t) {
         c = new Setting.Text(t.name, t.value, t.max, t.hint);
      } else {
         return null;
      }
      return c;
   }

   /** direction: 1 on, -1 off, 0 value/key, 2 Custom TxT style, 3 Custom TxT cosmetic. */
   public record Change(Module module, Setting setting, String label, int direction, de.bauloo.styles.BaulooStylesClient.Tab tab, de.bauloo.styles.Cosmetics.Slot slot) {
   }
}
