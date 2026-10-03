package dev.lego.net;

import dev.lego.LegoClient;
import dev.spotifyhud.Json;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

/**
 * LEGO name tag styles (assets/legoclient/data/nametags.json, exported from
 * packages/shared). The colour maths matches tagCharColor() in the launcher
 * and website, so a tag looks the same everywhere. Frames are a launcher /
 * website feature; in game a tag shows its colours, animation, weight and icon.
 */
public final class NameTags {
   public record Style(String id, int[] colors, String animation, boolean bold, boolean italic, String icon, int iconColor) {
   }

   private static Map<String, Style> styles;

   private NameTags() {
   }

   private static synchronized Map<String, Style> styles() {
      if (styles != null) return styles;
      Map<String, Style> out = new HashMap<>();
      try (InputStream in = NameTags.class.getResourceAsStream("/assets/legoclient/data/nametags.json")) {
         if (in != null) {
            Object root = Json.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            if (root instanceof List<?> list) {
               for (Object o : list) {
                  List<Object> cols = Json.arr(o, "colors");
                  int[] c = new int[cols == null ? 0 : cols.size()];
                  for (int i = 0; i < c.length; i++) c[i] = hex(String.valueOf(cols.get(i)));
                  String id = Json.str(o, "id", "");
                  out.put(id, new Style(id, c, Json.str(o, "animation", "none"), Json.bool(o, "bold", false), Json.bool(o, "italic", false),
                     Json.str(o, "icon", null), hex(Json.str(o, "iconColor", "#ffffff"))));
               }
            }
         }
      } catch (Throwable e) {
         LegoClient.LOG("Name-Tag-Stile: " + e);
      }
      styles = out;
      return out;
   }

   static int hex(String s) {
      try {
         return Integer.parseInt(s.startsWith("#") ? s.substring(1) : s, 16) & 0xFFFFFF;
      } catch (NumberFormatException e) {
         return 0xFFFFFF;
      }
   }

   public static Style get(String id) {
      return id == null ? null : styles().get(id);
   }

   static int lerp(int a, int b, double t) {
      t = Math.max(0.0, Math.min(1.0, t));
      int r = (int)Math.round(((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
      int g = (int)Math.round(((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
      int bl = (int)Math.round((a & 255) + ((b & 255) - (a & 255)) * t);
      return r << 16 | g << 8 | bl;
   }

   /** Same algorithm as tagCharColor() in packages/shared/src/nametags.ts. */
   public static int charColor(Style s, int index, int length, double t) {
      int[] c = s.colors();
      if (c.length == 0) return 0xFFFFFF;
      if (c.length == 1) return c[0];
      double base = length <= 1 ? 0.0 : (double)index / (length - 1);
      switch (s.animation()) {
         case "rainbow":
            return sample(c, base * 0.999 + t * 0.5);
         case "scroll":
            return sample(c, base * 0.999 + t * 0.25);
         case "wave":
            return sample(c, 0.5 + 0.5 * Math.sin(base * Math.PI * 2.0 + t * 3.0));
         case "pulse":
            return lerp(c[0], c[c.length - 1], 0.5 + 0.5 * Math.sin(t * 4.0));
         case "shimmer": {
            double head = (t * 0.8) % 1.6 - 0.3;
            return lerp(c[0], c[c.length - 1], Math.max(0.0, 1.0 - Math.abs(base - head) * 5.0));
         }
         default:
            return sample(c, base * 0.999);
      }
   }

   private static int sample(int[] c, double pos) {
      double p = ((pos % 1.0) + 1.0) % 1.0;
      double seg = p * (c.length - 1);
      int i = Math.min(c.length - 2, (int)Math.floor(seg));
      return lerp(c[i], c[i + 1], seg - i);
   }

   /** Builds the coloured name component for a player name (+ optional custom text). */
   public static Component component(Style s, String name, String customTag) {
      String text = customTag == null || customTag.isBlank() ? name : name + " · " + customTag;
      double t = System.currentTimeMillis() / 1000.0;
      MutableComponent out = Component.literal("");
      if (s.icon() != null) {
         final int ic = s.iconColor();
         out.append(Component.literal(s.icon() + " ").withStyle(st -> st.withColor(TextColor.fromRgb(ic))));
      }
      int[] cps = text.codePoints().toArray();
      for (int i = 0; i < cps.length; i++) {
         final int color = charColor(s, i, cps.length, t);
         out.append(Component.literal(new String(Character.toChars(cps[i]))).withStyle(st -> st.withColor(TextColor.fromRgb(color)).withBold(s.bold()).withItalic(s.italic())));
      }
      return out;
   }
}
