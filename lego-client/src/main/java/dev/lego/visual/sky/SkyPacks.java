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
      ResourceManager var0 = Mc.mc().getResourceManager();
      ArrayList var1 = new ArrayList();
      ArrayList var2 = new ArrayList();
      Map var3 = var0.listResources("fabricskyboxes/sky", var0x -> var0x.getPath().endsWith(".json"));

      for (Entry var5 : (Iterable<Entry>) (Iterable<?>) (var3.entrySet())) {
         try (InputStream var6 = ((Resource)var5.getValue()).open()) {
            SkyPacks.Layer var7 = parseFsb(new String(var6.readAllBytes(), StandardCharsets.UTF_8));
            if (var7 != null) {
               var7.source = ((Identifier)var5.getKey()).toString();
               var1.add(var7);
            }
         } catch (Throwable var15) {
            LegoClient.LOG("Sky-JSON " + var5.getKey() + ": " + var15);
         }
      }

      Map var16 = var0.listResources("optifine/sky/world0", var0x -> var0x.getPath().endsWith(".properties"));

      for (Entry var19 : (Iterable<Entry>) (Iterable<?>) (var16.entrySet())) {
         try (InputStream var20 = ((Resource)var19.getValue()).open()) {
            Properties var8 = new Properties();
            var8.load(new BufferedReader(new InputStreamReader(var20, StandardCharsets.UTF_8)));
            SkyPacks.Layer var9 = parseOptifine((Identifier)var19.getKey(), var8);
            if (var9 != null) {
               var9.source = ((Identifier)var19.getKey()).toString();
               var2.add(var9);
            }
         } catch (Throwable var13) {
            LegoClient.LOG("Sky-Properties " + var19.getKey() + ": " + var13);
         }
      }

      ArrayList var18 = !var1.isEmpty() ? var1 : var2;
      ((java.util.List<SkyPacks.Layer>)(java.util.List<?>)var18).sort((var0x, var1x) -> Integer.compare(var0x.priority, var1x.priority));
      info = var18.isEmpty() ? "Kein Sky-Pack aktiv" : var18.size() + " Himmel-Ebene(n) aus " + (!var1.isEmpty() ? "FabricSkyboxes" : "OptiFine") + "-Format";
      LegoClient.LOG("Sky-Packs: " + info);
      return var18;
   }

   private static SkyPacks.Layer parseOptifine(Identifier var0, Properties var1) {
      String var2 = var0.getPath();
      String var3 = var2.substring(0, var2.lastIndexOf(47) + 1);
      String var4 = var2.substring(var2.lastIndexOf(47) + 1).replace(".properties", "");
      String var5 = var1.getProperty("source", var4 + ".png").trim();
      Identifier var6;
      if (var5.startsWith("./")) {
         var6 = Identifier.fromNamespaceAndPath(var0.getNamespace(), var3 + var5.substring(2));
      } else if (var5.contains(":")) {
         var6 = Identifier.parse(var5);
      } else if (var5.startsWith("assets/")) {
         String[] var7 = var5.substring(7).split("/", 2);
         var6 = Identifier.fromNamespaceAndPath(var7[0], var7[1]);
      } else {
         var6 = Identifier.fromNamespaceAndPath(var0.getNamespace(), var5.contains("/") ? var5 : var3 + var5);
      }

      if (!var6.getPath().endsWith(".png")) {
         var6 = Identifier.fromNamespaceAndPath(var6.getNamespace(), var6.getPath() + ".png");
      }

      SkyPacks.Layer var19 = new SkyPacks.Layer();
      var19.strip = var6;
      String var8 = var1.getProperty("startFadeIn");
      String var9 = var1.getProperty("endFadeIn");
      String var10 = var1.getProperty("startFadeOut");
      String var11 = var1.getProperty("endFadeOut");
      if (var8 != null && var9 != null && var11 != null) {
         int var12 = hm(var8);
         int var13 = hm(var9);
         int var14 = hm(var11);
         int var15 = var10 != null ? hm(var10) : ((var14 - (var13 - var12)) % 24000 + 24000) % 24000;
         var19.fade = new int[]{var12, var13, var15, var14};
      }

      var19.rotate = !"false".equalsIgnoreCase(var1.getProperty("rotate", "true").trim());

      try {
         var19.speed = Double.parseDouble(var1.getProperty("speed", "1").trim());
      } catch (NumberFormatException var18) {
      }

      String var20 = var1.getProperty("axis");
      if (var20 != null) {
         String[] var21 = var20.trim().split("\\s+");
         if (var21.length == 3) {
            try {
               var19.axis = new double[]{Double.parseDouble(var21[0]), Double.parseDouble(var21[1]), Double.parseDouble(var21[2])};
            } catch (NumberFormatException var17) {
            }
         }
      }

      String var22 = var1.getProperty("blend", "add").trim();
      var19.add = var22.equals("add");

      try {
         var19.priority = Integer.parseInt(var4.replaceAll("\\D", ""));
      } catch (NumberFormatException var16) {
      }

      return var19;
   }

   private static int hm(String var0) {
      String[] var1 = var0.trim().split(":");
      int var2 = Integer.parseInt(var1[0].trim());
      int var3 = var1.length > 1 ? Integer.parseInt(var1[1].trim()) : 0;
      return ((var2 * 1000 + var3 * 1000 / 60 - 6000) % 24000 + 24000) % 24000;
   }

   private static SkyPacks.Layer parseFsb(String var0) {
      Object var1 = Json.parse(var0);
      if (!(var1 instanceof Map var2)) {
         return null;
      } else {
         String var3 = Json.str(var1, "type", "");
         SkyPacks.Layer var4 = new SkyPacks.Layer();
         if (!var3.equals("single-sprite-square-textured") && !var3.equals("single-sprite-animated-square-textured")) {
            if (!var3.equals("square-textured")) {
               return null;
            }

            Map var11 = Json.obj(var1, "textures");
            if (var11 == null) {
               return null;
            }

            var4.faces = new Identifier[]{
               id(Json.str(var11, "bottom", "")),
               id(Json.str(var11, "top", "")),
               id(Json.str(var11, "south", "")),
               id(Json.str(var11, "west", "")),
               id(Json.str(var11, "north", "")),
               id(Json.str(var11, "east", ""))
            };
         } else {
            String var5 = Json.str(var1, "texture", null);
            if (var5 == null) {
               return null;
            }

            var4.strip = id(var5);
         }

         Map var12 = Json.obj(var1, "properties");
         if (var12 != null) {
            var4.priority = (int)Json.num(var12, "priority", 0L);
            Map var6 = Json.obj(var12, "fade");
            if (var6 != null && !Json.bool(var6, "alwaysOn", false) && var6.containsKey("startFadeIn")) {
               var4.fade = new int[]{
                  (int)Json.dbl(var6, "startFadeIn", 0.0),
                  (int)Json.dbl(var6, "endFadeIn", 0.0),
                  (int)Json.dbl(var6, "startFadeOut", 0.0),
                  (int)Json.dbl(var6, "endFadeOut", 0.0)
               };
            }

            Map var7 = Json.obj(var12, "rotation");
            if (var7 != null) {
               List var8 = Json.arr(var7, "axis");
               if (var8 != null && var8.size() == 3) {
                  var4.axis = new double[]{num(var8.get(0)), num(var8.get(1)), num(var8.get(2))};
                  double var9 = Math.sqrt(var4.axis[0] * var4.axis[0] + var4.axis[1] * var4.axis[1] + var4.axis[2] * var4.axis[2]);
                  var4.rotate = var9 > 1.0E-6;
               } else {
                  var4.rotate = false;
               }

               var4.speed = Json.dbl(var7, "rotationSpeed", 1.0);
            } else {
               var4.rotate = false;
            }
         }

         Map var13 = Json.obj(var1, "blend");
         var4.add = var13 != null && "add".equals(Json.str(var13, "type", ""));
         return var4;
      }
   }

   private static double num(Object var0) {
      return var0 instanceof Number ? ((Number)var0).doubleValue() : 0.0;
   }

   private static Identifier id(String var0) {
      if (var0 != null && !var0.isEmpty()) {
         return var0.contains(":") ? Identifier.parse(var0) : Identifier.fromNamespaceAndPath("minecraft", var0);
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

      public double alpha(long var1) {
         if (this.fade == null) {
            return 1.0;
         } else {
            double var3 = (var1 % 24000L + 24000L) % 24000L;
            double var5 = this.fade[0];
            double var7 = this.fade[1];
            double var9 = this.fade[2];
            double var11 = this.fade[3];
            if (in(var3, var7, var9)) {
               return 1.0;
            } else if (in(var3, var5, var7)) {
               return frac(var3, var5, var7);
            } else {
               return in(var3, var9, var11) ? 1.0 - frac(var3, var9, var11) : 0.0;
            }
         }
      }

      private static boolean in(double var0, double var2, double var4) {
         return var2 <= var4 ? var0 >= var2 && var0 <= var4 : var0 >= var2 || var0 <= var4;
      }

      private static double frac(double var0, double var2, double var4) {
         double var6 = ((var4 - var2) % 24000.0 + 24000.0) % 24000.0;
         if (var6 == 0.0) {
            return 1.0;
         } else {
            double var8 = ((var0 - var2) % 24000.0 + 24000.0) % 24000.0;
            return Math.max(0.0, Math.min(1.0, var8 / var6));
         }
      }
   }
}
