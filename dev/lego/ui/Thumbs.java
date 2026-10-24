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
   private static final ExecutorService POOL = Executors.newFixedThreadPool(2, var0 -> {
      Thread var1 = new Thread(var0, "LegoClient-Thumbs");
      var1.setDaemon(true);
      var1.setPriority(1);
      return var1;
   });
   private static final Map<String, int[]> READY = new HashMap<>();
   private static final Set<String> PENDING = new HashSet<>();

   private Thumbs() {
   }

   public static Gx.Img get(String var0, int var1, int var2, Supplier<int[]> var3) {
      String var4 = var0 + "@" + var1 + "x" + var2;
      int[] var5;
      synchronized (READY) {
         var5 = READY.remove(var4);
      }

      if (var5 != null) {
         return Gx.get("th:" + var4, 0, () -> new int[][]{var5, {var1, var2}});
      } else {
         Gx.Img var11 = Gx.peek("th:" + var4);
         if (var11 != null) {
            return var11;
         } else {
            synchronized (READY) {
               if (PENDING.contains(var4)) {
                  return null;
               }

               PENDING.add(var4);
            }

            POOL.execute(() -> {
               try {
                  int[] var2x = (int[])var3.get();
                  synchronized (READY) {
                     READY.put(var4, var2x);
                  }
               } catch (Throwable var18) {
                  LegoClient.LOG("Vorschau-Fehler: " + var18);
               } finally {
                  synchronized (READY) {
                     PENDING.remove(var4);
                  }
               }
            });
            return null;
         }
      }
   }
}
