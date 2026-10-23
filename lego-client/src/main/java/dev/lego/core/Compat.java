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
   /** Vivecraft (real VR): own render effects, the Visor glasses and the knife hand swap stay off. */
   public static boolean vivecraft;

   private Compat() {
   }

   /** Own post effects and sky are off with VulkanMod and in real VR. */
   public static boolean noOwnRendering() {
      return vulkan || vivecraft;
   }

   public static void init() {
      try {
         vulkan = FabricLoader.getInstance().isModLoaded("vulkanmod");
      } catch (Throwable e) {
         vulkan = false;
      }
      try {
         vivecraft = FabricLoader.getInstance().isModLoaded("vivecraft");
      } catch (Throwable e) {
         vivecraft = false;
      }
      if (vivecraft) LegoClient.LOG("Vivecraft erkannt: VR-sicherer Modus (ohne Visor, Farbfilter, eigenen Himmel und Messer-Hand)");
      if (vulkan) LegoClient.LOG("VulkanMod erkannt: Kompatibilitätsmodus (ohne Farbfilter und eigenen Himmel)");
   }
}
