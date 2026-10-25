package dev.lego.txt;

import de.bauloo.styles.BaulooStylesClient;
import de.bauloo.styles.BaulooStylesClient.Tab;
import dev.lego.LegoClient;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;

/**
 * Custom TxT: picks a Bauloo style (a built-in resource pack) per group, exactly like the
 * Bauloo menu does it (BaulooStylesConfigScreen.applyAndRefresh), without opening the menu.
 */
public final class CustomTxt {
   /** Groups that ship styles in the jar (the cloud slots are in the Bauloo menu). */
   public static final Tab[] TABS = {
      Tab.CRYSTAL, Tab.ANCHOR, Tab.ARMOR, Tab.SWORD, Tab.GLOWSTONE, Tab.PICKAXE,
      Tab.INVENTORY, Tab.FIRE, Tab.DEEPSLATE, Tab.STONE, Tab.SNOW, Tab.GRASS
   };

   private CustomTxt() {
   }

   static String packId(String folder) {
      if (folder.startsWith("bauloo_upload_")) {
         return folder.endsWith("_folder") ? "file/" + folder : "file/" + folder + ".zip";
      }
      return folder.startsWith("bauloo_cloud_") ? "file/" + folder + ".zip" : "bauloo_styles:" + folder;
   }

   static boolean isPackOf(Tab tab, String id) {
      return id.startsWith("bauloo_styles:" + tab.prefix + "_")
         || id.startsWith("file/bauloo_upload_" + tab.prefix + "_")
         || id.startsWith("file/bauloo_cloud_" + tab.prefix + "_");
   }

   /** Folder of the style picked for this group (a built-in, cloud or uploaded pack), null for vanilla. */
   public static String current(Tab tab) {
      for (String id : Minecraft.getInstance().getResourcePackRepository().getSelectedIds()) {
         if (isPackOf(tab, id)) {
            int colon = id.indexOf(':');
            String folder = colon >= 0 ? id.substring(colon + 1) : id.substring("file/".length());
            return folder.endsWith(".zip") ? folder.substring(0, folder.length() - 4) : folder;
         }
      }
      return null;
   }

   /** Picks a style for this group (null = vanilla) and reloads the resource packs. */
   public static void select(Tab tab, String folder) {
      Minecraft mc = Minecraft.getInstance();
      PackRepository packs = mc.getResourcePackRepository();
      packs.reload();
      List<String> enabled = new ArrayList<>(packs.getSelectedIds());
      enabled.removeIf(id -> isPackOf(tab, id));
      if (folder != null) {
         String id = packId(folder);
         if (packs.getAvailableIds().contains(id)) {
            enabled.add(id);
         } else {
            LegoClient.LOG("Custom TxT: Paket " + id + " nicht gefunden");
         }
      }
      packs.setSelected(enabled);
      mc.options.updateResourcePacks(packs);
      mc.reloadResourcePacks();
      LegoClient.LOG("Custom TxT: " + tab.title + " = " + (folder == null ? "Vanilla" : folder));
   }

   /** The built-in styles of a group: {folder, name}. */
   public static String[][] styles(Tab tab) {
      return BaulooStylesClient.stylesOf(tab);
   }
}
