package de.bauloo.styles;

import java.lang.reflect.Field;

public final class BlurCompat {
   private static Field field;
   private static boolean looked;
   private static Boolean original;

   private BlurCompat() {
   }

   public static void apply() {
      Field f = field();
      if (f != null) {
         try {
            if (StylesUiConfig.invClearBackground) {
               if (original == null) {
                  original = f.getBoolean(null);
               }

               f.setBoolean(null, false);
            } else if (original != null) {
               f.setBoolean(null, original);
               original = null;
            }
         } catch (RuntimeException | ReflectiveOperationException var2) {
            BaulooStylesClient.LOGGER.warn("Could not switch Blur+ container blur: {}", var2.toString());
         }
      }
   }

   private static Field field() {
      if (!looked) {
         looked = true;

         try {
            field = Class.forName("eu.midnightdust.blur.config.BlurConfig").getField("blurContainers");
         } catch (RuntimeException | ReflectiveOperationException var1) {
            field = null;
         }
      }

      return field;
   }
}
