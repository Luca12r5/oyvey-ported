package dev.lego.core;

import dev.lego.LegoClient;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Compatibility with other renderers. With VulkanMod (Vulkan instead of OpenGL) the client's own
 * post effects and the custom sky are switched off, because they use OpenGL-only render paths; HUD,
 * menus, cosmetics and emotes keep working through Minecraft's normal render types.
 */
public final class Compat {
   public static boolean vulkan;

   private Compat() {
   }

   public static void init() {
      try {
         vulkan = FabricLoader.getInstance().isModLoaded("vulkanmod");
      } catch (Throwable e) {
         vulkan = false;
      }
      if (vulkan) LegoClient.LOG("VulkanMod erkannt: Kompatibilitätsmodus (ohne Farbfilter und eigenen Himmel)");
   }
}
