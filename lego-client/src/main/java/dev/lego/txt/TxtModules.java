package dev.lego.txt;

import de.bauloo.styles.BaulooStylesClient.Tab;
import de.bauloo.styles.BaulooStylesConfigScreen;
import dev.lego.core.Category;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
      // Every group whose wanted style changed goes into one reload (several picks applied at once).
      Map<Tab, String> batch = new LinkedHashMap<>();
      for (StyleModule m : STYLES) {
         m.sync(batch);
      }
      if (!batch.isEmpty()) {
         CustomTxt.selectAll(batch);
         for (StyleModule m : STYLES) {
            if (batch.containsKey(m.tab)) m.mirror();
         }
      }
   }

   /** CI: a crystal and a fire style. */
   public static void ciDemo() {
      for (StyleModule m : STYLES) {
         if (m.tab == Tab.CRYSTAL) m.want("crystal_aqua");
         if (m.tab == Tab.FIRE) m.want("fire_ultralow");
      }
   }

   /** CI: back to vanilla. */
   public static void ciReset() {
      for (StyleModule m : STYLES) {
         if (m.enabled) m.want(null);
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

      void sync(Map<Tab, String> batch) {
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
            batch.put(this.tab, wanted);
         }
      }

      /** Shows the state in the headset menu as well. */
      void mirror() {
         try {
            if (dev.visor.module.ModuleManager.get(this.id) instanceof dev.visor.module.LegoModule visor) {
               visor.mirror();
            }
         } catch (Throwable ignored) {
         }
      }

      /** Index of a style folder in the Style setting, -1 when this group has no such style. */
      public int indexOf(String folder) {
         for (int i = 0; i < this.folders.length; i++) {
            if (this.folders[i].equals(folder)) {
               return i;
            }
         }
         return -1;
      }

      /** Wants a style (null = vanilla); the next sync loads every wanted style with one reload. */
      public void want(String folder) {
         for (int i = 0; i < this.folders.length; i++) {
            if (this.folders[i].equals(folder)) {
               this.style.index = i;
            }
         }
         this.enabled = folder != null;
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
