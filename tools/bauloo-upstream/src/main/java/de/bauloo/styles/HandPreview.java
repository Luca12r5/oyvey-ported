package de.bauloo.styles;

import net.minecraft.class_1799;

public final class HandPreview {
   private static volatile class_1799 stack;

   private HandPreview() {
   }

   public static class_1799 stack() {
      return stack;
   }

   public static void show(class_1799 item) {
      stack = item;
   }

   public static void clear() {
      stack = null;
   }
}
