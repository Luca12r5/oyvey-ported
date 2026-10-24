package dev.lego.games;

import dev.lego.core.Modules;
import dev.lego.ui.View;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

public final class GameInfo {
   public final String id;
   public final String name;
   public final String icon;
   public final String tagline;
   public final String howTo;
   public final String controls;
   public final int color;
   public final boolean higherIsBetter;
   public final Supplier<? extends View> factory;
   public static final List<GameInfo> ALL = new ArrayList<>();
   private static final Map<String, Long> BEST = new LinkedHashMap<>();
   private static final Map<String, Long> PLAYS = new LinkedHashMap<>();

   public GameInfo(String var1, String var2, String var3, int var4, String var5, String var6, String var7, boolean var8, Supplier<? extends View> var9) {
      this.id = var1;
      this.name = var2;
      this.icon = var3;
      this.color = var4;
      this.tagline = var5;
      this.howTo = var6;
      this.controls = var7;
      this.higherIsBetter = var8;
      this.factory = var9;
   }

   public static void add(GameInfo var0) {
      ALL.add(var0);
   }

   public static GameInfo get(String var0) {
      for (GameInfo var2 : ALL) {
         if (var2.id.equals(var0)) {
            return var2;
         }
      }

      return null;
   }

   public static boolean hasBest(String var0) {
      return BEST.containsKey(var0);
   }

   public static long best(String var0) {
      Long var1 = BEST.get(var0);
      return var1 == null ? 0L : var1;
   }

   public static long plays(String var0) {
      Long var1 = PLAYS.get(var0);
      return var1 == null ? 0L : var1;
   }

   public static boolean submit(String var0, long var1) {
      GameInfo var3 = get(var0);
      PLAYS.put(var0, plays(var0) + 1L);
      boolean var4 = var3 == null || var3.higherIsBetter;
      if (var4 && var1 <= 0L) {
         Modules.scheduleSave();
         return false;
      } else {
         boolean var5 = !BEST.containsKey(var0) || (var4 ? var1 > best(var0) : var1 < best(var0));
         if (var5) {
            BEST.put(var0, var1);
         }

         Modules.scheduleSave();
         return var5;
      }
   }

   public static void registerPersist() {
      Modules.persist("games", new Modules.Persist() {
         @Override
         public Object save() {
            LinkedHashMap var1 = new LinkedHashMap();
            LinkedHashMap var2 = new LinkedHashMap<>(GameInfo.BEST);
            LinkedHashMap var3 = new LinkedHashMap<>(GameInfo.PLAYS);
            var1.put("best", var2);
            var1.put("plays", var3);
            return var1;
         }

         @Override
         public void load(Object var1) {
            if (var1 instanceof Map) {
               Object var2 = ((Map)var1).get("best");
               Object var3 = ((Map)var1).get("plays");
               if (var2 instanceof Map) {
                  for (Entry var5 : ((Map)var2).entrySet()) {
                     if (var5.getValue() instanceof Number) {
                        GameInfo.BEST.put((String)var5.getKey(), ((Number)var5.getValue()).longValue());
                     }
                  }
               }

               if (var3 instanceof Map) {
                  for (Entry var7 : ((Map)var3).entrySet()) {
                     if (var7.getValue() instanceof Number) {
                        GameInfo.PLAYS.put((String)var7.getKey(), ((Number)var7.getValue()).longValue());
                     }
                  }
               }
            }
         }
      });
   }
}
