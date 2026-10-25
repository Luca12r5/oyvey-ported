package de.bauloo.styles;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.class_2960;

public final class BuiltInCapes {
   public static final String[][] LIST = new String[][]{
      {"tornado_night", "Tornado Night", "yes"},
      {"crimson", "Crimson", "yes"},
      {"vortex", "Vortex", "yes"},
      {"daydream", "Daydream", "yes"},
      {"snake_blade", "Snake Blade", "no"},
      {"haunted_night", "Haunted Night", "no"},
      {"pumpkin_king", "Pumpkin King", "no"}
   };
   private static final int FRAMES = 36;
   private static final int FPS = 12;
   private static final Map<String, AnimatedCape> CAPES = new HashMap<>();
   private static final Map<String, AnimatedIcon> ICONS = new HashMap<>();
   private static String active;

   private BuiltInCapes() {
   }

   public static boolean has(String key) {
      for (String[] e : LIST) {
         if (e[0].equals(key)) {
            return true;
         }
      }

      return false;
   }

   private static AnimatedCape.FrameSource frames(String key) {
      return new AnimatedCape.FrameSource() {
         @Override
         public int count() {
            return 36;
         }

         @Override
         public BufferedImage read(int index) throws IOException {
            String path = String.format("/assets/bauloo_styles/textures/cosmetics/capes/%s/f%02d.jpg", key, index);

            try (InputStream in = BuiltInCapes.class.getResourceAsStream(path)) {
               return in == null ? null : ImageIO.read(in);
            }
         }
      };
   }

   public static class_2960 current(String key) {
      if (active != null && !active.equals(key)) {
         AnimatedCape old = CAPES.remove(active);
         if (old != null) {
            old.release();
         }
      }

      active = key;
      boolean tint = true;

      for (String[] e : LIST) {
         if (e[0].equals(key)) {
            tint = "yes".equals(e[2]);
         }
      }

      boolean tintable = tint;
      return CAPES.computeIfAbsent(key, k -> new AnimatedCape(class_2960.method_60655("bauloo_styles", "cosmetics/builtin/" + k), frames(k), 12, tintable))
         .current();
   }

   public static class_2960 icon(String key) {
      return ICONS.computeIfAbsent(key, k -> new AnimatedIcon(class_2960.method_60655("bauloo_styles", "cosmetics/builtin_icon/" + k), frames(k), 12))
         .current();
   }
}
