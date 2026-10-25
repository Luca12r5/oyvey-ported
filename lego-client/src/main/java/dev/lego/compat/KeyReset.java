package dev.lego.compat;

import dev.lego.LegoClient;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Once per install: puts the LEGO and Visor keys back to their defaults. Old 26.3 builds
 * registered raw GLFW numbers, which SDL3 read as other keys; Minecraft keeps a saved binding
 * even when the default changes, so Right Ctrl / Right Shift did nothing for those players.
 */
public final class KeyReset {
   private static boolean checked;

   private KeyReset() {
   }

   public static void once(Minecraft mc) {
      if (checked || mc.options == null) {
         return;
      }
      checked = true;
      try {
         Path flag = FabricLoader.getInstance().getConfigDir().resolve("legoclient").resolve("keys-v2.flag");
         if (Files.exists(flag)) {
            return;
         }
         StringBuilder reset = new StringBuilder();
         for (KeyMapping km : mc.options.keyMappings) {
            String name = km.getName();
            if ((name.startsWith("key.legoclient.") || name.startsWith("key.visor.")) && !km.isDefault()) {
               km.setKey(km.getDefaultKey());
               reset.append(reset.length() == 0 ? "" : ", ").append(name);
            }
         }
         KeyMapping.resetMapping();
         mc.options.save();
         Files.createDirectories(flag.getParent());
         Files.writeString(flag, "LEGO and Visor keys reset to their defaults once\n");
         LegoClient.LOG("Tasten zurückgesetzt: " + (reset.length() == 0 ? "keine nötig" : reset));
      } catch (Throwable e) {
         LegoClient.LOG("Tasten zurücksetzen: " + e);
      }
   }
}
