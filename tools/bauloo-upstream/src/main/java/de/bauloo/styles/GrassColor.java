package de.bauloo.styles;

import java.util.Map;
import net.minecraft.class_310;

public final class GrassColor {
   public static volatile int packColor = -1;
   private static final Map<String, Integer> PACKS = Map.ofEntries(
      Map.entry("grass_lush", 7127881),
      Map.entry("grass_dark", 4094508),
      Map.entry("grass_lime", 10936410),
      Map.entry("grass_white", 15921906),
      Map.entry("grass_black", 3816000),
      Map.entry("grass_blue", 5934335),
      Map.entry("grass_ice", 10411253),
      Map.entry("grass_red", 14700624),
      Map.entry("grass_pink", 16031944),
      Map.entry("grass_purple", 11566335),
      Map.entry("grass_lavender", 13154544)
   );
   private static String state = "";
   public static volatile long mixinCalls;

   public static int current() {
      return packColor;
   }

   public static int shown() {
      int color = current();
      return color < 0 ? -1 : color | 0xFF000000;
   }

   private GrassColor() {
   }

   public static void applyLive() {
      StylesUiConfig.load();
      int pack = -1;
      class_310 client = class_310.method_1551();

      try {
         for (String id : client.method_1520().method_29210()) {
            int colon = id.indexOf(58);
            Integer color = PACKS.get(colon < 0 ? id : id.substring(colon + 1));
            if (color != null) {
               pack = color;
            }
         }
      } catch (Throwable var7) {
      }

      packColor = pack;
      String now = String.valueOf(pack);
      boolean changed = !now.equals(state);
      boolean first = state.isEmpty();
      class_310 mc = class_310.method_1551();
      boolean reload = changed && !first && mc.field_1769 != null && mc.field_1687 != null;
      if (changed) {
         state = now;
         if (reload) {
            mc.field_1769.method_3279();
         }
      }
   }
}
