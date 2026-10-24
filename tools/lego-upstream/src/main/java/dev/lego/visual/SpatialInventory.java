package dev.lego.visual;

import dev.lego.core.Category;
import dev.lego.core.Module;
import dev.lego.core.Setting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class SpatialInventory extends Module {

    final Setting.Bool autoFov = add(new Setting.Bool("Auto-FOV", true));

    private Object spatialConfig;
    private Method saveMethod;
    private boolean resolved;

    public SpatialInventory() {
        super("spatialinventory", "Spatial Inventory", "Zeigt Inventar-Screens als 3D-Ebenen in der Welt an", Category.VISUALS);
        icon("box");
        fresh();
    }

    @Override public void onEnable() {
        resolve();
        setActive(true);
    }

    @Override public void onDisable() {
        setActive(false);
    }

    @Override public void tick() {
        if (!resolved) resolve();
        syncFov();
    }

    private void resolve() {
        resolved = true;
        try {
            Class<?> clientClass = Class.forName("org.tastytrash.spatialGUI.client.SpatialGUIClient");
            Field configField = clientClass.getDeclaredField("config");
            configField.setAccessible(true);
            spatialConfig = configField.get(null);
            if (spatialConfig != null) {
                saveMethod = spatialConfig.getClass().getMethod("save");
            }
        } catch (Throwable ignored) {
            spatialConfig = null;
        }
    }

    private void setActive(boolean active) {
        if (spatialConfig == null) return;
        try {
            Field enabledField = spatialConfig.getClass().getDeclaredField("enabled");
            enabledField.setAccessible(true);
            enabledField.set(spatialConfig, active);
            if (saveMethod != null) saveMethod.invoke(spatialConfig);
        } catch (Throwable ignored) {}
    }

    private void syncFov() {
        if (spatialConfig == null) return;
        try {
            Field fovField = null;
            for (Field f : spatialConfig.getClass().getDeclaredFields()) {
                if (f.getName().toLowerCase().contains("fov") || f.getType().getSimpleName().contains("AutoFov")) {
                    fovField = f; break;
                }
            }
            if (fovField == null) return;
            fovField.setAccessible(true);
            Object fovConfig = fovField.get(spatialConfig);
            if (fovConfig == null) return;
            Field en = fovConfig.getClass().getDeclaredField("enabled");
            en.setAccessible(true);
            en.set(fovConfig, autoFov.get());
        } catch (Throwable ignored) {}
    }
}
