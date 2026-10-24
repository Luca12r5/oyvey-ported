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
   public final List<Setting> settings = new ArrayList<>();
   public String icon;
   public boolean fresh = false;
   private final Map<String, Object> defaults = new LinkedHashMap<>();
   private boolean defaultOn;

   protected Module(String var1, String var2, String var3, Category var4) {
      this.id = var1;
      this.name = var2;
      this.description = var3;
      this.category = var4;
      this.icon = var4.icon;
   }

   protected <T extends Setting> T add(T var1) {
      this.settings.add(var1);
      return (T)var1;
   }

   public Module icon(String var1) {
      this.icon = var1;
      return this;
   }

   public Module fresh() {
      this.fresh = true;
      return this;
   }

   public void setEnabled(boolean var1) {
      if (var1 != this.enabled) {
         this.enabled = var1;

         try {
            if (var1) {
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

      for (Setting var2 : this.settings) {
         Object var3 = var2.save();
         if (var3 != null) {
            this.defaults.put(var2.name, var3);
         }
      }
   }

   public void resetSettings() {
      for (Setting var2 : this.settings) {
         if (this.defaults.containsKey(var2.name)) {
            var2.load(this.defaults.get(var2.name));
         }
      }
   }

   public boolean defaultOn() {
      return this.defaultOn;
   }
}
