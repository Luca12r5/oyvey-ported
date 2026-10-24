package dev.lego.core;

import dev.lego.LegoClient;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Module {
   public final String id;
   public final String name;
   public final String description;
   public final Category category;
   public boolean enabled;
   public int key = -1;
   public boolean hidden;
   public String customName;
   public final List<Setting> settings = new ArrayList<>();
   public String icon;
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

   protected <T extends Setting> T add(T s) {
      this.settings.add(s);
      return s;
   }

   public String displayName() {
      if (this.customName != null && !this.customName.isEmpty()) {
         return this.customName;
      } else {
         String t = Lang.get("mod." + this.id);
         return t != null ? t : this.name;
      }
   }

   public String displayDesc() {
      String t = Lang.get("mod." + this.id + ".desc");
      return t != null ? t : this.description;
   }

   public Module icon(String i) {
      this.icon = i;
      return this;
   }

   public Module fresh() {
      this.fresh = true;
      return this;
   }

   public void setEnabled(boolean v) {
      if (v != this.enabled) {
         this.enabled = v;

         try {
            if (v) {
               this.onEnable();
            } else {
               this.onDisable();
            }
         } catch (Throwable var3) {
            LegoClient.LOG("Fehler in " + this.id + ": " + var3);
         }
      }
   }

   public void toggle() {
      this.setEnabled(!this.enabled);
   }

   public boolean isToggleable() {
      return true;
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void tick() {
   }

   void captureDefaults() {
      this.defaultOn = this.enabled;

      for (Setting s : this.settings) {
         Object v = s.save();
         if (v != null) {
            this.defaults.put(s.name, v);
         }
      }
   }

   public void resetSettings() {
      for (Setting s : this.settings) {
         if (this.defaults.containsKey(s.name)) {
            s.load(this.defaults.get(s.name));
         }
      }
   }

   public boolean defaultOn() {
      return this.defaultOn;
   }
}
