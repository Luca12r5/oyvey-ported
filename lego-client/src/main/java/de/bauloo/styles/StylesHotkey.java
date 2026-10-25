package de.bauloo.styles;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class StylesHotkey {
   private static Key key = InputConstants.UNKNOWN;
   private static boolean loaded;

   private StylesHotkey() {
   }

   public static Key get() {
      if (!loaded) {
         load();
      }

      return key;
   }

   public static void set(Key value) {
      key = value == null ? InputConstants.UNKNOWN : value;
      loaded = true;
      save();
   }

   public static boolean isSet() {
      Key k = get();
      return k != null && !k.equals(InputConstants.UNKNOWN) && k.getValue() >= 0;
   }

   public static String label() {
      return isSet() ? get().getDisplayName().getString() : "None";
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
            key = Type.KEYSYM.getOrCreate(344);
         }
      } catch (IOException var1) {
         key = InputConstants.UNKNOWN;
      }
   }

   private static String encode(Key k) {
      return k != null && !k.equals(InputConstants.UNKNOWN) ? (k.getType() == Type.MOUSE ? "MOUSE" : "KEYSYM") + ":" + k.getValue() : "NONE";
   }

   private static Key decode(String s) {
      if (s != null && !s.equals("NONE")) {
         try {
            String[] parts = s.split(":");
            Type type = "MOUSE".equals(parts[0]) ? Type.MOUSE : Type.KEYSYM;
            return type.getOrCreate(Integer.parseInt(parts[1]));
         } catch (Exception var3) {
            return InputConstants.UNKNOWN;
         }
      } else {
         return InputConstants.UNKNOWN;
      }
   }
}
