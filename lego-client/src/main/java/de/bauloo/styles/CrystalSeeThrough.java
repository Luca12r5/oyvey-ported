package de.bauloo.styles;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

public final class CrystalSeeThrough {
   private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/end_crystal/end_crystal.png");
   private static final RenderType LAYER = RenderTypes.entityTranslucent(TEXTURE);
   private static volatile boolean active;
   private static Object checkedFor;

   private CrystalSeeThrough() {
   }

   public static boolean active() {
      Object manager = Minecraft.getInstance().getResourceManager();
      if (manager != checkedFor) {
         checkedFor = manager;
         check();
      }

      return active;
   }

   public static RenderType layer() {
      return LAYER;
   }

   private static void check() {
      boolean found = false;

      try {
         Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(TEXTURE);
         if (resource.isPresent()) {
            try (
               InputStream in = resource.get().open();
               NativeImage image = NativeImage.read(in);
            ) {
               int half = 0;

               for (int y = 0; y < image.getHeight() && !found; y++) {
                  for (int x = 0; x < image.getWidth(); x++) {
                     int a = image.getPixel(x, y) >>> 24 & 0xFF;
                     if (a > 8 && a < 247) {
                        if (++half > 16) {
                           found = true;
                           break;
                        }
                     }
                  }
               }
            }
         }
      } catch (Exception var12) {
         BaulooStylesClient.LOGGER.warn("See-through check failed: {}", var12.toString());
      }

      if (found != active) {
         BaulooStylesClient.LOGGER.info("Crystal texture is {}", found ? "see-through: drawn translucent" : "solid");
      }

      active = found;
   }
}
