package de.bauloo.styles;

public interface CrystalAge {
   float baulooStyles$realAge();

   void baulooStyles$setRealAge(float var1);

   static float factor() {
      StylesUiConfig.load();
      return Math.max(0.001F, StylesUiConfig.crystalSpin / 100.0F);
   }

   public static final class Offset {
      public static volatile float current;

      private Offset() {
      }
   }
}
