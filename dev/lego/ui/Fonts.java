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
         for (int var0 = 0; var0 < 4; var0++) {
            try (InputStream var1 = Fonts.class.getResourceAsStream("/assets/legoclient/fonts/" + FILES[var0])) {
               if (var1 != null) {
                  BASE[var0] = Font.createFont(0, var1);
               }
            } catch (Throwable var6) {
               BASE[var0] = null;
            }

            if (BASE[var0] == null) {
               BASE[var0] = new Font("SansSerif", var0 == 3 ? 1 : 0, 12);
            }
         }

         loaded = true;
      }
   }

   public static Font get(int var0, float var1) {
      if (!loaded) {
         load();
      }

      int var2 = Math.max(0, Math.min(3, var0));
      int var3 = Math.max(1, Math.round(var1 * 4.0F));
      long var4 = (long)var2 << 32 | var3;
      return SIZED.computeIfAbsent(var4, var2x -> BASE[var2].deriveFont(var3 / 4.0F));
   }

   public static float width(String var0, int var1, float var2) {
      if (var0 != null && !var0.isEmpty()) {
         String var3 = var1 + "|" + Math.round(var2 * 4.0F) + "|" + var0;
         Float var4 = WIDTHS.get(var3);
         if (var4 != null) {
            return var4;
         } else {
            Rectangle2D var5 = get(var1, var2).getStringBounds(var0, FRC);
            float var6 = (float)var5.getWidth();
            if (WIDTHS.size() > 20000) {
               WIDTHS.clear();
            }

            WIDTHS.put(var3, var6);
            return var6;
         }
      } else {
         return 0.0F;
      }
   }

   public static float ascent(int var0, float var1) {
      return get(var0, var1).getLineMetrics("Hg", FRC).getAscent();
   }

   public static float descent(int var0, float var1) {
      return get(var0, var1).getLineMetrics("Hg", FRC).getDescent();
   }

   public static String ellipsize(String var0, int var1, float var2, float var3) {
      if (var0 == null) {
         return "";
      } else if (width(var0, var1, var2) <= var3) {
         return var0;
      } else {
         String var4 = "…";
         int var5 = 0;
         int var6 = var0.length();

         while (var5 < var6) {
            int var7 = (var5 + var6 + 1) / 2;
            if (width(var0.substring(0, var7).trim() + var4, var1, var2) <= var3) {
               var5 = var7;
            } else {
               var6 = var7 - 1;
            }
         }

         return var5 <= 0 ? var4 : var0.substring(0, var5).trim() + var4;
      }
   }

   public static List<String> wrap(String var0, int var1, float var2, float var3, int var4) {
      ArrayList var5 = new ArrayList();
      if (var0 != null && !var0.isEmpty()) {
         String[] var6 = var0.split(" ");
         StringBuilder var7 = new StringBuilder();

         for (int var8 = 0; var8 < var6.length; var8++) {
            String var9 = var7.length() == 0 ? var6[var8] : var7 + " " + var6[var8];
            if (!(width(var9, var1, var2) <= var3) && var7.length() != 0) {
               var5.add(var7.toString());
               var7.setLength(0);
               var7.append(var6[var8]);
               if (var5.size() == var4 - 1) {
                  StringBuilder var10 = new StringBuilder(var7);

                  for (int var11 = var8 + 1; var11 < var6.length; var11++) {
                     var10.append(' ').append(var6[var11]);
                  }

                  var5.add(ellipsize(var10.toString(), var1, var2, var3));
                  return var5;
               }
            } else {
               var7.setLength(0);
               var7.append(var9);
            }
         }

         if (var7.length() > 0) {
            var5.add(var7.toString());
         }

         return var5;
      } else {
         return var5;
      }
   }
}
