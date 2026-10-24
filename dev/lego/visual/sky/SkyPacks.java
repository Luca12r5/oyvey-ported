package dev.lego.visual.sky;

import dev.lego.LegoClient;
import dev.lego.core.Mc;
import dev.spotifyhud.Json;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public final class SkyPacks {
   private static List<SkyPacks.Layer> layers = null;
   private static String info = "";

   private SkyPacks() {
   }

   public static void invalidate() {
      layers = null;
   }

   public static String info() {
      return info;
   }

   public static List<SkyPacks.Layer> layers() {
      if (layers == null) {
         try {
            layers = scan();
         } catch (Throwable var1) {
            layers = new ArrayList<>();
            info = "Fehler: " + var1.getMessage();
            LegoClient.LOG("Sky-Packs: " + var1);
         }
      }

      return layers;
   }

   private static List<SkyPacks.Layer> scan() {
      ResourceManager rm = Mc.mc().getResourceManager();
      List<SkyPacks.Layer> fsb = new ArrayList<>();
      List<SkyPacks.Layer> of = new ArrayList<>();
      Map<Identifier, Resource> json = rm.listResources("fabricskyboxes/sky", id -> id.getPath().endsWith(".json"));

      for (Entry<Identifier, Resource> e : json.entrySet()) {
         try (InputStream in = e.getValue().open()) {
            SkyPacks.Layer l = parseFsb(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            if (l != null) {
               l.source = e.getKey().toString();
               fsb.add(l);
            }
         } catch (Throwable var15) {
            LegoClient.LOG("Sky-JSON " + e.getKey() + ": " + var15);
         }
      }

      Map<Identifier, Resource> props = rm.listResources("optifine/sky/world0", id -> id.getPath().endsWith(".properties"));

      for (Entry<Identifier, Resource> e : props.entrySet()) {
         try (InputStream inx = e.getValue().open()) {
            Properties p = new Properties();
            p.load(new BufferedReader(new InputStreamReader(inx, StandardCharsets.UTF_8)));
            SkyPacks.Layer l = parseOptifine(e.getKey(), p);
            if (l != null) {
               l.source = e.getKey().toString();
               of.add(l);
            }
         } catch (Throwable var13) {
            LegoClient.LOG("Sky-Properties " + e.getKey() + ": " + var13);
         }
      }

      List<SkyPacks.Layer> out = !fsb.isEmpty() ? fsb : of;
      out.sort((a, b) -> Integer.compare(a.priority, b.priority));
      info = out.isEmpty() ? "Kein Sky-Pack aktiv" : out.size() + " Himmel-Ebene(n) aus " + (!fsb.isEmpty() ? "FabricSkyboxes" : "OptiFine") + "-Format";
      LegoClient.LOG("Sky-Packs: " + info);
      return out;
   }

   private static SkyPacks.Layer parseOptifine(Identifier file, Properties p) {
      String path = file.getPath();
      String dir = path.substring(0, path.lastIndexOf(47) + 1);
      String name = path.substring(path.lastIndexOf(47) + 1).replace(".properties", "");
      String src = p.getProperty("source", name + ".png").trim();
      Identifier tex;
      if (src.startsWith("./")) {
         tex = Identifier.fromNamespaceAndPath(file.getNamespace(), dir + src.substring(2));
      } else if (src.contains(":")) {
         tex = Identifier.parse(src);
      } else if (src.startsWith("assets/")) {
         String[] parts = src.substring(7).split("/", 2);
         tex = Identifier.fromNamespaceAndPath(parts[0], parts[1]);
      } else {
         tex = Identifier.fromNamespaceAndPath(file.getNamespace(), src.contains("/") ? src : dir + src);
      }

      if (!tex.getPath().endsWith(".png")) {
         tex = Identifier.fromNamespaceAndPath(tex.getNamespace(), tex.getPath() + ".png");
      }

      SkyPacks.Layer l = new SkyPacks.Layer();
      l.strip = tex;
      String a = p.getProperty("startFadeIn");
      String b = p.getProperty("endFadeIn");
      String c = p.getProperty("startFadeOut");
      String d = p.getProperty("endFadeOut");
      if (a != null && b != null && d != null) {
         int ia = hm(a);
         int ib = hm(b);
         int id = hm(d);
         int ic = c != null ? hm(c) : ((id - (ib - ia)) % 24000 + 24000) % 24000;
         l.fade = new int[]{ia, ib, ic, id};
      }

      l.rotate = !"false".equalsIgnoreCase(p.getProperty("rotate", "true").trim());

      try {
         l.speed = Double.parseDouble(p.getProperty("speed", "1").trim());
      } catch (NumberFormatException var18) {
      }

      String ax = p.getProperty("axis");
      if (ax != null) {
         String[] s = ax.trim().split("\\s+");
         if (s.length == 3) {
            try {
               l.axis = new double[]{Double.parseDouble(s[0]), Double.parseDouble(s[1]), Double.parseDouble(s[2])};
            } catch (NumberFormatException var17) {
            }
         }
      }

      String blend = p.getProperty("blend", "add").trim();
      l.add = blend.equals("add");

      try {
         l.priority = Integer.parseInt(name.replaceAll("\\D", ""));
      } catch (NumberFormatException var16) {
      }

      return l;
   }

   private static int hm(String s) {
      String[] p = s.trim().split(":");
      int h = Integer.parseInt(p[0].trim());
      int m = p.length > 1 ? Integer.parseInt(p[1].trim()) : 0;
      return ((h * 1000 + m * 1000 / 60 - 6000) % 24000 + 24000) % 24000;
   }

   private static SkyPacks.Layer parseFsb(String text) {
      Object root = Json.parse(text);
      if (!(root instanceof Map<String, Object> m)) {
         return null;
      } else {
         String type = Json.str(root, "type", "");
         SkyPacks.Layer l = new SkyPacks.Layer();
         if (!type.equals("single-sprite-square-textured") && !type.equals("single-sprite-animated-square-textured")) {
            if (!type.equals("square-textured")) {
               return null;
            }

            Map<String, Object> tx = Json.obj(root, "textures");
            if (tx == null) {
               return null;
            }

            l.faces = new Identifier[]{
               id(Json.str(tx, "bottom", "")),
               id(Json.str(tx, "top", "")),
               id(Json.str(tx, "south", "")),
               id(Json.str(tx, "west", "")),
               id(Json.str(tx, "north", "")),
               id(Json.str(tx, "east", ""))
            };
         } else {
            String t = Json.str(root, "texture", null);
            if (t == null) {
               return null;
            }

            l.strip = id(t);
         }

         Map<String, Object> props = Json.obj(root, "properties");
         if (props != null) {
            l.priority = (int)Json.num(props, "priority", 0L);
            Map<String, Object> fade = Json.obj(props, "fade");
            if (fade != null && !Json.bool(fade, "alwaysOn", false) && fade.containsKey("startFadeIn")) {
               l.fade = new int[]{
                  (int)Json.dbl(fade, "startFadeIn", 0.0),
                  (int)Json.dbl(fade, "endFadeIn", 0.0),
                  (int)Json.dbl(fade, "startFadeOut", 0.0),
                  (int)Json.dbl(fade, "endFadeOut", 0.0)
               };
            }

            Map<String, Object> rot = Json.obj(props, "rotation");
            if (rot != null) {
               List<Object> axis = Json.arr(rot, "axis");
               if (axis != null && axis.size() == 3) {
                  l.axis = new double[]{num(axis.get(0)), num(axis.get(1)), num(axis.get(2))};
                  double len = Math.sqrt(l.axis[0] * l.axis[0] + l.axis[1] * l.axis[1] + l.axis[2] * l.axis[2]);
                  l.rotate = len > 1.0E-6;
               } else {
                  l.rotate = false;
               }

               l.speed = Json.dbl(rot, "rotationSpeed", 1.0);
            } else {
               l.rotate = false;
            }
         }

         Map<String, Object> blend = Json.obj(root, "blend");
         l.add = blend != null && "add".equals(Json.str(blend, "type", ""));
         return l;
      }
   }

   private static double num(Object o) {
      return o instanceof Number ? ((Number)o).doubleValue() : 0.0;
   }

   private static Identifier id(String s) {
      if (s != null && !s.isEmpty()) {
         return s.contains(":") ? Identifier.parse(s) : Identifier.fromNamespaceAndPath("minecraft", s);
      } else {
         return null;
      }
   }

   public static final class Layer {
      public Identifier strip;
      public Identifier[] faces;
      public int[] fade;
      public double[] axis = new double[]{0.0, 0.0, 1.0};
      public double speed = 1.0;
      public boolean rotate = true;
      public boolean add;
      public int priority;
      public String source;

      public double alpha(long t) {
         if (this.fade == null) {
            return 1.0;
         } else {
            double tt = (t % 24000L + 24000L) % 24000L;
            double sIn = this.fade[0];
            double eIn = this.fade[1];
            double sOut = this.fade[2];
            double eOut = this.fade[3];
            if (in(tt, eIn, sOut)) {
               return 1.0;
            } else if (in(tt, sIn, eIn)) {
               return frac(tt, sIn, eIn);
            } else {
               return in(tt, sOut, eOut) ? 1.0 - frac(tt, sOut, eOut) : 0.0;
            }
         }
      }

      private static boolean in(double t, double a, double b) {
         return a <= b ? t >= a && t <= b : t >= a || t <= b;
      }

      private static double frac(double t, double a, double b) {
         double len = ((b - a) % 24000.0 + 24000.0) % 24000.0;
         if (len == 0.0) {
            return 1.0;
         } else {
            double d = ((t - a) % 24000.0 + 24000.0) % 24000.0;
            return Math.max(0.0, Math.min(1.0, d / len));
         }
      }
   }
}
