package de.bauloo.styles;

import java.io.InputStream;
import java.util.Optional;
import net.minecraft.class_1011;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;

public final class CrystalSeeThrough {
   private static final class_2960 TEXTURE = class_2960.method_60656("textures/entity/end_crystal/end_crystal.png");
   private static final class_1921 LAYER = class_12249.method_76000(TEXTURE);
   private static volatile boolean active;
   private static Object checkedFor;

   private CrystalSeeThrough() {
   }

   public static boolean active() {
      Object manager = class_310.method_1551().method_1478();
      if (manager != checkedFor) {
         checkedFor = manager;
         check();
      }

      return active;
   }

   public static class_1921 layer() {
      return LAYER;
   }

   private static void check() {
      boolean found = false;

      try {
         Optional<class_3298> resource = class_310.method_1551().method_1478().method_14486(TEXTURE);
         if (resource.isPresent()) {
            try (InputStream in = resource.get().method_14482()) {
               class_1011 image = class_1011.method_4309(in);

               try {
                  int half = 0;

                  for (int y = 0; y < image.method_4323() && !found; y++) {
                     for (int x = 0; x < image.method_4307(); x++) {
                        int a = image.method_61940(x, y) >>> 24 & 0xFF;
                        if (a > 8 && a < 247) {
                           if (++half > 16) {
                              found = true;
                              break;
                           }
                        }
                     }
                  }
               } catch (Throwable var10) {
                  if (image != null) {
                     try {
                        image.close();
                     } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                     }
                  }

                  throw var10;
               }

               if (image != null) {
                  image.close();
               }
            }
         }
      } catch (Exception e) {
         BaulooStylesClient.LOGGER.warn("See-through check failed: {}", e.toString());
      }

      if (found != active) {
         BaulooStylesClient.LOGGER.info("Crystal texture is {}", found ? "see-through: drawn translucent" : "solid");
      }

      active = found;
   }
}
