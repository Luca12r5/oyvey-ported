package dev.lego.txt;

import de.bauloo.styles.BaulooStylesClient.Tab;
import de.bauloo.styles.BaulooStylesConfigScreen;
import dev.lego.core.Category;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;

/** The "Custom TxT" category: one module per Bauloo style group plus the full Bauloo menu. */
public final class TxtModules {
   public static final List<StyleModule> STYLES = new ArrayList<>();
   private static final String UNSET = "\u0000unset";

   private TxtModules() {
   }

   public static void registerAll() {
      for (Tab tab : CustomTxt.TABS) {
         if (CustomTxt.styles(tab).length > 0) {
            STYLES.add(Modules.register(new StyleModule(tab)));
         }
      }
      Modules.register(new MenuModule());
   }

   /**
    * Keeps the modules and the selected resource packs in step: a style picked in the Bauloo menu
    * (or the pack screen) shows up here, a change here picks the pack. The pack selection in
    * options.txt stays the truth, like in Bauloo Styles itself.
    */
   public static void sync() {
      if (Minecraft.getInstance().getOverlay() != null) {
         return; // a resource reload is running
      }
      for (StyleModule m : STYLES) {
         m.sync();
      }
   }

   public static void openMenu() {
      Minecraft mc = Minecraft.getInstance();
      mc.setScreen(new BaulooStylesConfigScreen(mc.screen));
   }

   public static final class StyleModule extends Module {
      public final Tab tab;
      public final Setting.Mode style;
      private final String[] folders;
      private String applied = UNSET;

      StyleModule(Tab tab) {
         super("txt_" + tab.prefix, tab.title, "Bauloo-Styles für " + tab.title, Category.CUSTOM_TXT);
         this.tab = tab;
         String[][] styles = CustomTxt.styles(tab);
         this.folders = new String[styles.length];
         String[] names = new String[styles.length];
         for (int i = 0; i < styles.length; i++) {
            this.folders[i] = styles[i][0];
            names[i] = styles[i][1];
         }
         this.style = this.add(new Setting.Mode("Style", 0, names));
         this.icon("palette");
      }

      private String wanted() {
         return this.enabled ? this.folders[Math.max(0, Math.min(this.folders.length - 1, this.style.index))] : null;
      }

      void sync() {
         String current = CustomTxt.current(this.tab);
         if (!Objects.equals(current, this.applied)) {
            // First look, or picked somewhere else: take it over without reloading again.
            this.applied = current;
            this.enabled = current != null;
            for (int i = 0; i < this.folders.length; i++) {
               if (this.folders[i].equals(current)) {
                  this.style.index = i;
               }
            }
            this.mirror();
            return;
         }
         String wanted = this.wanted();
         if (!Objects.equals(wanted, this.applied)) {
            this.applied = wanted;
            CustomTxt.select(this.tab, wanted);
            this.mirror();
         }
      }

      /** Shows the state in the headset menu as well. */
      void mirror() {
         try {
            if (dev.visor.module.ModuleManager.get(this.id) instanceof dev.visor.module.TxtModule visor) {
               visor.mirror(this.enabled, this.style.index);
            }
         } catch (Throwable ignored) {
         }
      }

      /** Picks a style right away (CI and other callers). */
      public void pick(String folder) {
         for (int i = 0; i < this.folders.length; i++) {
            if (this.folders[i].equals(folder)) {
               this.style.index = i;
            }
         }
         this.enabled = folder != null;
         this.applied = this.wanted();
         CustomTxt.select(this.tab, this.applied);
         this.mirror();
      }
   }

   static final class MenuModule extends Module {
      MenuModule() {
         super("txt_menu", "Bauloo-Menü", "Alle Styles mit Vorschau, Extras, Cloud, Presets und Uploads", Category.CUSTOM_TXT);
         this.add(new Setting.Action("Menü", "Bauloo-Menü öffnen", TxtModules::openMenu));
         this.icon("palette");
      }

      @Override
      public boolean isToggleable() {
         return false;
      }
   }
}
