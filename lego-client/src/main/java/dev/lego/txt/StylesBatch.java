package dev.lego.txt;

import de.bauloo.styles.BaulooStylesClient.Tab;
import de.bauloo.styles.BaulooStylesConfigScreen;
import de.bauloo.styles.MyPacks;
import de.bauloo.styles.StylesCloud;
import de.bauloo.styles.VipPacks;
import dev.lego.LegoClient;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;

/**
 * Puts many style picks in at once, like APPLY in the Bauloo menu: styles from your own packs
 * ("mypack:") and VIP packs are turned into single-part packs first, cloud styles are downloaded,
 * then every group gets its pack with one resource reload.
 */
public final class StylesBatch {
   /** Vanilla for a group. */
   public static final String VANILLA = "\u0000vanilla";

   private StylesBatch() {
   }

   public static void apply(Map<Tab, String> picks) {
      if (picks.isEmpty()) {
         return;
      }
      Map<Tab, String> todo = new LinkedHashMap<>(picks);
      boolean convert = todo.values().stream().anyMatch(f -> f.startsWith("mypack:") || f.startsWith("vippack:"));
      List<CompletableFuture<Boolean>> downloads = new ArrayList<>();
      for (String f : todo.values()) {
         StylesCloud.Style cloud = StylesCloud.byFolder(f);
         if (cloud != null && !StylesCloud.isReady(cloud)) {
            downloads.add(StylesCloud.download(cloud));
         }
      }
      if (!convert && downloads.isEmpty()) {
         load(todo, new LinkedHashMap<>());
         return;
      }
      Thread t = new Thread(() -> {
         Map<Tab, String> ids = new LinkedHashMap<>();
         for (Map.Entry<Tab, String> e : todo.entrySet()) {
            String f = e.getValue();
            try {
               if (f.startsWith("mypack:")) {
                  MyPacks.Style s = MyPacks.byId(f.substring("mypack:".length()));
                  ids.put(e.getKey(), s == null ? null : MyPacks.use(s));
               } else if (f.startsWith("vippack:")) {
                  VipPacks.Style s = VipPacks.byId(f.substring("vippack:".length()));
                  ids.put(e.getKey(), s == null ? null : VipPacks.use(s));
               }
            } catch (Throwable ex) {
               LegoClient.LOG("TxT: " + f + ": " + ex);
               ids.put(e.getKey(), null);
            }
         }
         try {
            CompletableFuture.allOf(downloads.toArray(new CompletableFuture[0])).join();
         } catch (Throwable ex) {
            LegoClient.LOG("TxT: Download: " + ex);
         }
         Minecraft.getInstance().execute(() -> load(todo, ids));
      }, "LEGO TxT batch");
      t.setDaemon(true);
      t.start();
   }

   /** Render thread: one pack selection change, one reload. */
   private static void load(Map<Tab, String> picks, Map<Tab, String> ready) {
      Minecraft mc = Minecraft.getInstance();
      PackRepository packs = mc.getResourcePackRepository();
      packs.reload();
      List<String> enabled = new ArrayList<>(packs.getSelectedIds());
      int n = 0;
      for (Map.Entry<Tab, String> e : picks.entrySet()) {
         Tab tab = e.getKey();
         String folder = e.getValue();
         enabled.removeIf(id -> BaulooStylesConfigScreen.isPackOf(tab, id) || ready.containsKey(tab) && id.equals(ready.get(tab)));
         String id;
         if (VANILLA.equals(folder)) {
            id = null;
         } else if (ready.containsKey(tab)) {
            id = ready.get(tab) == null ? null : BaulooStylesConfigScreen.packId(ready.get(tab));
         } else {
            id = BaulooStylesConfigScreen.packId(folder);
         }
         if (id != null) {
            if (packs.getAvailableIds().contains(id)) {
               enabled.add(id);
            } else {
               LegoClient.LOG("Custom TxT: Paket " + id + " nicht gefunden");
            }
         }
         LegoClient.LOG("Custom TxT: " + tab.title + " = " + (id == null ? "Vanilla" : id));
         n++;
      }
      packs.setSelected(enabled);
      mc.options.updateResourcePacks(packs);
      mc.reloadResourcePacks();
      LegoClient.LOG("Custom TxT: " + n + " Gruppe(n), ein Reload");
   }
}
