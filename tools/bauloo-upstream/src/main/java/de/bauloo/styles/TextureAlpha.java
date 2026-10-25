package de.bauloo.styles;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1011;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;

public final class TextureAlpha {
   private static final Map<String, Boolean> CACHE = new HashMap<>();
   private static Object cachedFor;

   private TextureAlpha() {
   }

   public static boolean hasHoles(class_2960 texture, double x, double y, double w, double h) {
      Object manager = class_310.method_1551().method_1478();
      if (manager != cachedFor) {
         cachedFor = manager;
         CACHE.clear();
      }

      String key = texture + "|" + x + "|" + y + "|" + w + "|" + h;
      Boolean known = CACHE.get(key);
      if (known != null) {
         return known;
      }

      boolean holes = false;

      try {
         Optional<class_3298> resource = class_310.method_1551().method_1478().method_14486(texture);
         if (resource.isPresent()) {
            try (InputStream in = resource.get().method_14482()) {
               class_1011 image = class_1011.method_4309(in);

               try {
                  int x0 = (int)(x * image.method_4307());
                  int y0 = (int)(y * image.method_4323());
                  int x1 = Math.min(image.method_4307(), (int)((x + w) * image.method_4307()));
                  int y1 = Math.min(image.method_4323(), (int)((y + h) * image.method_4323()));
                  int clear = 0;

                  for (int py = y0; py < y1 && !holes; py++) {
                     for (int px = x0; px < x1; px++) {
                        if ((image.method_61940(px, py) >>> 24 & 0xFF) < 16) {
                           if (++clear > 8) {
                              holes = true;
                              break;
                           }
                        }
                     }
                  }
               } catch (Throwable var25) {
                  if (image != null) {
                     try {
                        image.close();
                     } catch (Throwable var24) {
                        var25.addSuppressed(var24);
                     }
                  }

                  throw var25;
               }

               if (image != null) {
                  image.close();
               }
            }
         }
      } catch (Exception e) {
         BaulooStylesClient.LOGGER.warn("Alpha check of {} failed: {}", texture, e.toString());
      }

      CACHE.put(key, holes);
      return holes;
   }

   public static boolean shieldHasHoles() {
      return hasHoles(class_2960.method_60656("textures/entity/shield_base_nopattern.png"), 0.015625, 0.015625, 0.1875, 0.34375);
   }
}
