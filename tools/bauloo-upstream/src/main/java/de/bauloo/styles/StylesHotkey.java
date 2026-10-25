package de.bauloo.styles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_3675;
import net.minecraft.class_3675.class_306;
import net.minecraft.class_3675.class_307;

public final class StylesHotkey {
   private static class_306 key = class_3675.field_16237;
   private static boolean loaded;

   private StylesHotkey() {
   }

   public static class_306 get() {
      if (!loaded) {
         load();
      }

      return key;
   }

   public static void set(class_306 value) {
      key = value == null ? class_3675.field_16237 : value;
      loaded = true;
      save();
   }

   public static boolean isSet() {
      class_306 k = get();
      return k != null && !k.equals(class_3675.field_16237) && k.method_1444() >= 0;
   }

   public static String label() {
      return isSet() ? get().method_27445().getString() : "None";
   }

   private static Path file() {
      return FabricLoader.getInstance().getConfigDir().resolve("bauloo-styles.txt");
   }

   private static void save() {
      try {
         Files.writeString(file(), encode(key));
      } catch (IOException var1) {
      }
   }

   private static void load() {
      loaded = true;

      try {
         Path f = file();
         if (Files.exists(f)) {
            key = decode(Files.readString(f).trim());
         } else {
            key = class_307.field_1668.method_1447(344);
         }
      } catch (IOException ignored) {
         key = class_3675.field_16237;
      }
   }

   private static String encode(class_306 k) {
      return k != null && !k.equals(class_3675.field_16237) ? (k.method_1442() == class_307.field_1672 ? "MOUSE" : "KEYSYM") + ":" + k.method_1444() : "NONE";
   }

   private static class_306 decode(String s) {
      if (s != null && !s.equals("NONE")) {
         try {
            String[] parts = s.split(":");
            class_307 type = "MOUSE".equals(parts[0]) ? class_307.field_1672 : class_307.field_1668;
            return type.method_1447(Integer.parseInt(parts[1]));
         } catch (Exception e) {
            return class_3675.field_16237;
         }
      } else {
         return class_3675.field_16237;
      }
   }
}
