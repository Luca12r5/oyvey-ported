package de.bauloo.styles;

import net.minecraft.client.Minecraft;

public final class BlockOutline {
   private BlockOutline() {
   }

   public static boolean active() {
      return StylesUiConfig.blockOutlineCustom;
   }

   public static int color() {
      return 0xFF000000 | StylesUiConfig.blockOutlineColor & 16777215;
   }

   public static float width() {
      float base = Minecraft.getInstance().getWindow().getAppropriateLineWidth();
      return base * Math.max(1, Math.min(6, StylesUiConfig.blockOutlineWidth));
   }
}
