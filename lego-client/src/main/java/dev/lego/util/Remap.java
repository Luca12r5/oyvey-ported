package dev.lego.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

/**
 * Resolves Minecraft member names for reflection. The names are written in the
 * stable intermediary namespace and mapped to whatever the running game uses
 * (intermediary in production, Mojang names in a development run).
 */
public final class Remap {
   private Remap() {
   }

   private static MappingResolver resolver() {
      return FabricLoader.getInstance().getMappingResolver();
   }

   /** @param owner intermediary class, e.g. {@code net.minecraft.class_757} */
   public static String method(String owner, String name, String descriptor) {
      return resolver().mapMethodName("intermediary", owner, name, descriptor);
   }

   public static String field(String owner, String name, String descriptor) {
      return resolver().mapFieldName("intermediary", owner, name, descriptor);
   }

   public static String clazz(String intermediaryClass) {
      return resolver().mapClassName("intermediary", intermediaryClass);
   }
}
