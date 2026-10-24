package dev.lego.ui;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Fonts {
   public static final int LIGHT = 0;
   public static final int REGULAR = 1;
   public static final int MEDIUM = 2;
   public static final int BOLD = 3;
   private static final String[] FILES = new String[]{"Poppins-Light.ttf", "Poppins-Regular.ttf", "Poppins-Medium.ttf", "Poppins-Bold.ttf"};
   private static final Font[] BASE = new Font[4];
   private static volatile boolean loaded;
   private static final Map<Long, Font> SIZED = new ConcurrentHashMap<>();
   private static final Map<String, Float> WIDTHS = new ConcurrentHashMap<>();
   public static final FontRenderContext FRC = new FontRenderContext(null, true, true);

   private Fonts() {
   }

   private static synchronized void load() {
      if (!loaded) {
         for (int i = 0; i < 4; i++) {
            try (InputStream in = Fonts.class.getResourceAsStream("/assets/legoclient/fonts/" + FILES[i])) {
               if (in != null) {
                  BASE[i] = Font.createFont(0, in);
               }
            } catch (Throwable var6) {
               BASE[i] = null;
            }

            if (BASE[i] == null) {
               BASE[i] = new Font("SansSerif", i == 3 ? 1 : 0, 12);
            }
         }

         loaded = true;
      }
   }

   public static Font get(int weight, float size) {
      if (!loaded) {
         load();
      }

      int w = Math.max(0, Math.min(3, weight));
      int q = Math.max(1, Math.round(size * 4.0F));
      long key = (long)w << 32 | q;
      return SIZED.computeIfAbsent(key, k -> BASE[w].deriveFont(q / 4.0F));
   }

   public static float width(String s, int weight, float size) {
      if (s != null && !s.isEmpty()) {
         String key = weight + "|" + Math.round(size * 4.0F) + "|" + s;
         Float cached = WIDTHS.get(key);
         if (cached != null) {
            return cached;
         } else {
            Rectangle2D r = get(weight, size).getStringBounds(s, FRC);
            float w = (float)r.getWidth();
            if (WIDTHS.size() > 20000) {
               WIDTHS.clear();
            }

            WIDTHS.put(key, w);
            return w;
         }
      } else {
         return 0.0F;
      }
   }

   public static float ascent(int weight, float size) {
      return get(weight, size).getLineMetrics("Hg", FRC).getAscent();
   }

   public static float descent(int weight, float size) {
      return get(weight, size).getLineMetrics("Hg", FRC).getDescent();
   }

   public static String ellipsize(String s, int weight, float size, float maxW) {
      if (s == null) {
         return "";
      } else if (width(s, weight, size) <= maxW) {
         return s;
      } else {
         String e = "…";
         int lo = 0;
         int hi = s.length();

         while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (width(s.substring(0, mid).trim() + e, weight, size) <= maxW) {
               lo = mid;
            } else {
               hi = mid - 1;
            }
         }

         return lo <= 0 ? e : s.substring(0, lo).trim() + e;
      }
   }

   public static List<String> wrap(String s, int weight, float size, float maxW, int maxLines) {
      List<String> out = new ArrayList<>();
      if (s != null && !s.isEmpty()) {
         String[] words = s.split(" ");
         StringBuilder line = new StringBuilder();

         for (int i = 0; i < words.length; i++) {
            String test = line.length() == 0 ? words[i] : line + " " + words[i];
            if (!(width(test, weight, size) <= maxW) && line.length() != 0) {
               out.add(line.toString());
               line.setLength(0);
               line.append(words[i]);
               if (out.size() == maxLines - 1) {
                  StringBuilder rest = new StringBuilder(line);

                  for (int j = i + 1; j < words.length; j++) {
                     rest.append(' ').append(words[j]);
                  }

                  out.add(ellipsize(rest.toString(), weight, size, maxW));
                  return out;
               }
            } else {
               line.setLength(0);
               line.append(test);
            }
         }

         if (line.length() > 0) {
            out.add(line.toString());
         }

         return out;
      } else {
         return out;
      }
   }
}
