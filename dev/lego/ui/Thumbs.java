package dev.lego.ui;

import dev.lego.LegoClient;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public final class Thumbs {
   private static final ExecutorService POOL = Executors.newFixedThreadPool(2, r -> {
      Thread t = new Thread(r, "LegoClient-Thumbs");
      t.setDaemon(true);
      t.setPriority(1);
      return t;
   });
   private static final Map<String, int[]> READY = new HashMap<>();
   private static final Set<String> PENDING = new HashSet<>();

   private Thumbs() {
   }

   public static Gx.Img get(String key, int w, int h, Supplier<int[]> painter) {
      String k = key + "@" + w + "x" + h;
      int[] px;
      synchronized (READY) {
         px = READY.remove(k);
      }

      if (px != null) {
         return Gx.get("th:" + k, 0, () -> new int[][]{px, {w, h}});
      } else {
         Gx.Img cached = Gx.peek("th:" + k);
         if (cached != null) {
            return cached;
         } else {
            synchronized (READY) {
               if (PENDING.contains(k)) {
                  return null;
               }

               PENDING.add(k);
            }

            POOL.execute(() -> {
               try {
                  int[] r = painter.get();
                  synchronized (READY) {
                     READY.put(k, r);
                  }
               } catch (Throwable var18) {
                  LegoClient.LOG("Vorschau-Fehler: " + var18);
               } finally {
                  synchronized (READY) {
                     PENDING.remove(k);
                  }
               }
            });
            return null;
         }
      }
   }
}
