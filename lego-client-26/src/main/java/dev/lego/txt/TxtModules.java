// Minecraft 26.3 version of dev/lego/txt/TxtModules.java: Bauloo Styles is not ported to 26.3 yet.
package dev.lego.txt;

import dev.lego.core.Category;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import java.util.List;

/** The "Custom TxT" category on 26.3: says honestly that the styles come later. */
public final class TxtModules {
   private TxtModules() {
   }

   public static void registerAll() {
      Modules.register(new Module("txt_soon", "Custom TxT", "Bauloo Styles kommt bald auch für 26.3 – auf 1.21.11 ist es schon da", Category.CUSTOM_TXT) {
         @Override
         public boolean isToggleable() {
            return false;
         }
      }.icon("palette"));
   }

   public static void sync() {
   }

   public static List<dev.visor.module.Module> visorModules() {
      return List.of();
   }

   public static void ciDemo() {
   }

   public static void ciReset() {
   }

   public static void openMenu() {
   }
}
