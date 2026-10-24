package dev.lego.visual;

import dev.lego.core.Category;
import dev.lego.core.Module;
import dev.lego.core.Setting;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class SpatialInventory extends Module {
   final Setting.Bool autoFov = this.add(new Setting.Bool("Auto-FOV", true));
   private Object spatialConfig;
   private Method saveMethod;
   private boolean resolved;

   public SpatialInventory() {
      super("spatialinventory", "Spatial Inventory", "Zeigt Inventar-Screens als 3D-Ebenen in der Welt an", Category.VISUALS);
      this.icon("box");
      this.fresh();
   }

   @Override
   public void onEnable() {
      this.resolve();
      this.setActive(true);
   }

   @Override
   public void onDisable() {
      this.setActive(false);
   }

   @Override
   public void tick() {
      if (!this.resolved) {
         this.resolve();
      }

      this.syncFov();
   }

   private void resolve() {
      this.resolved = true;

      try {
         Class<?> clientClass = Class.forName("org.tastytrash.spatialGUI.client.SpatialGUIClient");
         Field configField = clientClass.getDeclaredField("config");
         configField.setAccessible(true);
         this.spatialConfig = configField.get(null);
         if (this.spatialConfig != null) {
            this.saveMethod = this.spatialConfig.getClass().getMethod("save");
         }
      } catch (Throwable var3) {
         this.spatialConfig = null;
      }
   }

   private void setActive(boolean active) {
      if (this.spatialConfig != null) {
         try {
            Field enabledField = this.spatialConfig.getClass().getDeclaredField("enabled");
            enabledField.setAccessible(true);
            enabledField.set(this.spatialConfig, active);
            if (this.saveMethod != null) {
               this.saveMethod.invoke(this.spatialConfig);
            }
         } catch (Throwable var3) {
         }
      }
   }

   private void syncFov() {
      if (this.spatialConfig != null) {
         try {
            Field fovField = null;

            for (Field f : this.spatialConfig.getClass().getDeclaredFields()) {
               if (f.getName().toLowerCase().contains("fov") || f.getType().getSimpleName().contains("AutoFov")) {
                  fovField = f;
                  break;
               }
            }

            if (fovField == null) {
               return;
            }

            fovField.setAccessible(true);
            Object fovConfig = fovField.get(this.spatialConfig);
            if (fovConfig == null) {
               return;
            }

            Field en = fovConfig.getClass().getDeclaredField("enabled");
            en.setAccessible(true);
            en.set(fovConfig, this.autoFov.get());
         } catch (Throwable var6) {
         }
      }
   }
}
