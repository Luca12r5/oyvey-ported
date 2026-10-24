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
   public static final String[] LEVELS = new String[]{"Leicht", "Mittel", "Schwer", "Profi"};
   private static final Map<String, Long> DIFF = new LinkedHashMap<>();

   public GameInfo(
      String id, String name, String icon, int color, String tagline, String howTo, String controls, boolean higherIsBetter, Supplier<? extends View> factory
   ) {
      this.id = id;
      this.name = name;
      this.icon = icon;
      this.color = color;
      this.tagline = tagline;
      this.howTo = howTo;
      this.controls = controls;
      this.higherIsBetter = higherIsBetter;
      this.factory = factory;
   }

   public static int difficulty(String id) {
      Long d = DIFF.get(id);
      return d == null ? 1 : (int)Math.max(0L, Math.min((long)(LEVELS.length - 1), d));
   }

   public static void setDifficulty(String id, int level) {
      DIFF.put(id, (long)Math.max(0, Math.min(LEVELS.length - 1, level)));
      Modules.scheduleSave();
   }

   public static void add(GameInfo g) {
      ALL.add(g);
   }

   public static GameInfo get(String id) {
      for (GameInfo g : ALL) {
         if (g.id.equals(id)) {
            return g;
         }
      }

      return null;
   }

   public static boolean hasBest(String id) {
      return BEST.containsKey(id);
   }

   public static long best(String id) {
      Long b = BEST.get(id);
      return b == null ? 0L : b;
   }

   public static long plays(String id) {
      Long b = PLAYS.get(id);
      return b == null ? 0L : b;
   }

   public static boolean submit(String id, long score) {
      GameInfo g = get(id);
      PLAYS.put(id, plays(id) + 1L);
      boolean high = g == null || g.higherIsBetter;
      if (high && score <= 0L) {
         Modules.scheduleSave();
         return false;
      } else {
         boolean better = !BEST.containsKey(id) || (high ? score > best(id) : score < best(id));
         if (better) {
            BEST.put(id, score);
         }

         Modules.scheduleSave();
         return better;
      }
   }

   public static void registerPersist() {
      Modules.persist("games", new Modules.Persist() {
         @Override
         public Object save() {
            Map<String, Object> m = new LinkedHashMap<>();
            Map<String, Object> b = new LinkedHashMap<>(GameInfo.BEST);
            Map<String, Object> p = new LinkedHashMap<>(GameInfo.PLAYS);
            m.put("best", b);
            m.put("plays", p);
            m.put("diff", new LinkedHashMap<>(GameInfo.DIFF));
            return m;
         }

         @Override
         public void load(Object o) {
            if (o instanceof Map) {
               Object b = ((Map)o).get("best");
               Object p = ((Map)o).get("plays");
               Object d = ((Map)o).get("diff");
               if (d instanceof Map) {
                  for (Entry<String, Object> e : ((Map)d).entrySet()) {
                     if (e.getValue() instanceof Number) {
                        GameInfo.DIFF.put(e.getKey(), ((Number)e.getValue()).longValue());
                     }
                  }
               }

               if (b instanceof Map) {
                  for (Entry<String, Object> ex : ((Map)b).entrySet()) {
                     if (ex.getValue() instanceof Number) {
                        GameInfo.BEST.put(ex.getKey(), ((Number)ex.getValue()).longValue());
                     }
                  }
               }

               if (p instanceof Map) {
                  for (Entry<String, Object> exx : ((Map)p).entrySet()) {
                     if (exx.getValue() instanceof Number) {
                        GameInfo.PLAYS.put(exx.getKey(), ((Number)exx.getValue()).longValue());
                     }
                  }
               }
            }
         }
      });
   }
}
