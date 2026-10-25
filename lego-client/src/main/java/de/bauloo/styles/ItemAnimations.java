package de.bauloo.styles;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.fabricmc.loader.api.FabricLoader;

public final class ItemAnimations {
   private static final boolean SODIUM_EXTRA = FabricLoader.getInstance().isModLoaded("sodium-extra");
   private static Method options;
   private static Field settings;
   private static Field animation;
   private static boolean broken;

   private ItemAnimations() {
   }

   public static boolean sodiumExtraBlocksThem() {
      if (SODIUM_EXTRA && !broken) {
         try {
            if (options == null) {
               Class<?> mod = Class.forName("me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod");
               options = mod.getMethod("options");
            }

            Object o = options.invoke(null);
            if (settings == null) {
               settings = o.getClass().getField("animationSettings");
            }

            Object s = settings.get(o);
            if (animation == null) {
               animation = s.getClass().getField("animation");
            }

            return !animation.getBoolean(s);
         } catch (RuntimeException | ReflectiveOperationException var2) {
            broken = true;
            BaulooStylesClient.LOGGER.info("[Styles] Sodium Extra animation switch not readable: {}", var2.toString());
            return false;
         }
      } else {
         return false;
      }
   }
}
