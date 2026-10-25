package dev.lego.core;

import dev.lego.LegoClient;
import dev.lego.hud.HudModule;
import dev.spotifyhud.Json;
import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.loader.api.FabricLoader;

public final class Modules {
   public static final List<Module> ALL = new ArrayList<>();
   public static String theme = "nacht";
   public static String hudTheme = "standard";
   public static int hudAccent = 0;
   public static int accent = 3;
   private static volatile long saveAt = 0L;
   private static final Map<String, Modules.Persist> EXTRA = new LinkedHashMap<>();

   private Modules() {
   }

   public static <T extends Module> T register(T m) {
      ALL.add(m);
      m.captureDefaults();
      return m;
   }

   public static void persist(String key, Modules.Persist p) {
      EXTRA.put(key, p);
   }

   public static List<Module> of(Category c) {
      List<Module> out = new ArrayList<>();

      for (Module m : ALL) {
         if (m.category == c) {
            out.add(m);
         }
      }

      return out;
   }

   public static Module get(String id) {
      for (Module m : ALL) {
         if (m.id.equals(id)) {
            return m;
         }
      }

      return null;
   }

   public static int accentArgb() {
      return argbOf(accent);
   }

   public static int hudAccentArgb() {
      return argbOf(hudAccent);
   }

   private static int argbOf(int accent) {
      int i = Math.max(0, Math.min(Setting.Color.PALETTE.length - 1, accent));
      if (i == Setting.Color.PALETTE.length - 1) {
         double t = System.currentTimeMillis() / 2600.0;
         return 0xFF000000 | Color.HSBtoRGB((float)(t - Math.floor(t)), 0.7F, 1.0F) & 16777215;
      } else {
         return Setting.Color.PALETTE[i];
      }
   }

   private static Path path() {
      return FabricLoader.getInstance().getConfigDir().resolve("legoclient.json");
   }

   public static void scheduleSave() {
      saveAt = System.currentTimeMillis() + 700L;
   }

   private static long checkAt;
   private static int savedState;

   /**
    * Saves after a change. Besides explicit scheduleSave() calls, every 2 s the state of all
    * modules (on/off, key, settings, HUD position) is compared with what was saved last, so
    * changes from any place (Visor menu, key binds, games) are kept too.
    */
   public static void tickSave() {
      long now = System.currentTimeMillis();
      if (saveAt != 0L && now >= saveAt) {
         saveAt = 0L;
         save();
         return;
      }
      if (now >= checkAt) {
         checkAt = now + 2000L;
         int st = stateHash();
         if (st != savedState) save();
      }
   }

   private static int stateHash() {
      int h = 17;
      try {
         h = 31 * h + String.valueOf(theme).hashCode() + 7 * accent + 13 * hudAccent + String.valueOf(hudTheme).hashCode();
         for (Module m : ALL) {
            h = 31 * h + m.id.hashCode() + (m.enabled ? 1 : 0) + 3 * m.key;
            for (Setting st : m.settings) {
               Object v = st.save();
               h = 31 * h + (v == null ? 0 : v.hashCode());
            }
            if (m instanceof HudModule hm) {
               h = 31 * h + Double.hashCode(hm.fx) + Double.hashCode(hm.fy) + Double.hashCode(hm.scale);
            }
         }
      } catch (Throwable ignored) {
      }
      return h;
   }

   public static synchronized void save() {
      try {
         Map<String, Object> root = new LinkedHashMap<>();
         root.put("theme", theme);
         root.put("designVersion", 6L);
         root.put("hudTheme", hudTheme);
         root.put("hudAccent", (long)hudAccent);
         root.put("accent", (long)accent);
         Map<String, Object> mods = new LinkedHashMap<>();

         for (Module m : ALL) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("on", m.enabled);
            o.put("key", (long)m.key);
            if (m.hidden) {
               o.put("hidden", true);
            }

            if (m.customName != null) {
               o.put("customName", m.customName);
            }

            Map<String, Object> s = new LinkedHashMap<>();

            for (Setting st : m.settings) {
               Object v = st.save();
               if (v != null) {
                  s.put(st.name, v);
               }
            }

            o.put("settings", s);
            if (m instanceof HudModule h) {
               o.put("x", h.fx);
               o.put("y", h.fy);
               o.put("scale", h.scale);
            }

            mods.put(m.id, o);
         }

         root.put("modules", mods);

         for (Entry<String, Modules.Persist> e : EXTRA.entrySet()) {
            try {
               Object v = e.getValue().save();
               if (v != null) {
                  root.put(e.getKey(), v);
               }
            } catch (Throwable var9) {
               LegoClient.LOG("Speichern " + e.getKey() + ": " + var9);
            }
         }

         Path p = path();
         Files.createDirectories(p.getParent());
         Path tmp = p.resolveSibling("legoclient.json.tmp");
         Files.writeString(tmp, Json.write(root), StandardCharsets.UTF_8);
         Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING);
         savedState = stateHash();
      } catch (Throwable var10) {
         LegoClient.LOG("Speichern fehlgeschlagen: " + var10);
      }
   }

   public static void load() {
      try {
         Path p = path();
         if (!Files.exists(p)) {
            return;
         }

         Object root = Json.parse(Files.readString(p, StandardCharsets.UTF_8));
         theme = Json.str(root, "theme", theme);
         accent = (int)Json.num(root, "accent", accent);
         hudTheme = Json.str(root, "hudTheme", hudTheme);
         hudAccent = (int)Json.num(root, "hudAccent", hudAccent);
         long designVer = Json.num(root, "designVersion", 0L);
         if (designVer < 3L) {
            hudTheme = theme;
            hudAccent = accent;
            theme = "winter";
            accent = 1;
         }

         // Halloween edition: players still on the old default get the Nocturne look (orange accent).
         if (designVer < 4L && "winter".equals(theme)) {
            theme = "halloween";
            accent = 7;
         }

         // Round 11: the launcher's night look (NoRisk colours) for everybody still on the old default.
         if (designVer < 5L && "halloween".equals(theme)) {
            theme = "nacht";
            accent = 3;
         }

         // Round 24: no Halloween look any more; the orange accent becomes the calm blue.
         if (designVer < 6L) {
            if ("halloween".equals(theme)) theme = "nacht";
            if (accent == 7) accent = 3;
            if ("halloween".equals(hudTheme)) hudTheme = "nacht";
         }

         if (root instanceof Map) {
            for (Entry<String, Modules.Persist> e : EXTRA.entrySet()) {
               Object v = ((Map)root).get(e.getKey());
               if (v != null) {
                  try {
                     e.getValue().load(v);
                  } catch (Throwable var9) {
                     LegoClient.LOG("Laden " + e.getKey() + ": " + var9);
                  }
               }
            }
         }

         if (designVer < 6L) {
            dev.lego.ui.UiSettings.halloween = false;
            dev.lego.ui.UiSettings.xmas = false;
         }

         Map<String, Object> mods = Json.obj(root, "modules");
         if (mods == null) {
            return;
         }

         for (Module m : ALL) {
            Object o = mods.get(m.id);
            if (o instanceof Map) {
               m.key = (int)Json.num(o, "key", m.key);
               m.hidden = Json.bool(o, "hidden", false);
               m.customName = Json.str(o, "customName", null);
               Map<String, Object> s = Json.obj(o, "settings");
               if (s != null) {
                  for (Setting st : m.settings) {
                     if (s.containsKey(st.name)) {
                        st.load(s.get(st.name));
                     }
                  }
               }

               if (m instanceof HudModule h) {
                  h.fx = Math.max(0.0, Math.min(1.0, Json.dbl(o, "x", h.fx)));
                  h.fy = Math.max(0.0, Math.min(1.0, Json.dbl(o, "y", h.fy)));
                  h.scale = Math.max(0.4, Math.min(3.0, Json.dbl(o, "scale", h.scale)));
               }

               boolean on = Json.bool(o, "on", m.enabled);
               if (m.isToggleable() && on != m.enabled) {
                  m.setEnabled(on);
               }
            }
         }
      } catch (Throwable var10) {
         LegoClient.LOG("Laden fehlgeschlagen: " + var10);
      }
   }

   public interface Persist {
      Object save();

      void load(Object var1);
   }
}
