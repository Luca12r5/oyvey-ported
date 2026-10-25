package de.bauloo.styles;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

public final class TextureAlpha {
   private static final Map<String, Boolean> CACHE = new HashMap<>();
   private static Object cachedFor;

   private TextureAlpha() {
   }

   public static boolean hasHoles(Identifier texture, double x, double y, double w, double h) {
      Object manager = Minecraft.getInstance().getResourceManager();
      if (manager != cachedFor) {
         cachedFor = manager;
         CACHE.clear();
      }

      String key = texture + "|" + x + "|" + y + "|" + w + "|" + h;
      Boolean known = CACHE.get(key);
      if (known != null) {
         return known;
      } else {
         boolean holes = false;

         try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(texture);
            if (resource.isPresent()) {
               try (
                  InputStream in = resource.get().open();
                  NativeImage image = NativeImage.read(in);
               ) {
                  int x0 = (int)(x * image.getWidth());
                  int y0 = (int)(y * image.getHeight());
                  int x1 = Math.min(image.getWidth(), (int)((x + w) * image.getWidth()));
                  int y1 = Math.min(image.getHeight(), (int)((y + h) * image.getHeight()));
                  int clear = 0;

                  for (int py = y0; py < y1 && !holes; py++) {
                     for (int px = x0; px < x1; px++) {
                        if ((image.getPixel(px, py) >>> 24 & 0xFF) < 16) {
                           if (++clear > 8) {
                              holes = true;
                              break;
                           }
                        }
                     }
                  }
               }
            }
         } catch (Exception var27) {
            BaulooStylesClient.LOGGER.warn("Alpha check of {} failed: {}", texture, var27.toString());
         }

         CACHE.put(key, holes);
         return holes;
      }
   }

   public static boolean shieldHasHoles() {
      return hasHoles(Identifier.withDefaultNamespace("textures/entity/shield_base_nopattern.png"), 0.015625, 0.015625, 0.1875, 0.34375);
   }
}
