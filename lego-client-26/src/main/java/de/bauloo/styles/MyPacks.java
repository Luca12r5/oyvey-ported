package de.bauloo.styles;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class MyPacks {
   private static final boolean PUBLIC = false;
   private static final Path CACHE = FabricLoader.getInstance().getConfigDir().resolve("bauloo-styles/mypacks");
   private static final Path INDEX = CACHE.resolve("index.json");
   private static final Path PREVIEWS = CACHE.resolve("previews");
   private static volatile List<MyPacks.Style> styles = List.of();
   private static volatile int version;
   private static volatile boolean scanning;
   private static volatile int scanned;
   private static volatile int total;
   private static volatile int packCount;
   private static boolean started;
   private static volatile Boolean allowed;
   private static volatile long checkedAt;
   private static volatile boolean checking;
   private static final Map<String, Identifier> TEXTURES = new HashMap<>();
   private static final Set<String> NO_PICTURE = Collections.synchronizedSet(new HashSet<>());
   private static volatile String lastSignature = "";

   private MyPacks() {
   }

   public static boolean shown() {
      if (!Boolean.getBoolean("bauloo.audit") && !StylesPresence.isVip()) {
         if ((allowed == null || System.currentTimeMillis() - checkedAt > 300000L) && !checking) {
            checking = true;
            Thread thread = new Thread(() -> {
               try {
                  allowed = StylesUploadServer.hasKeyRole();
               } catch (Exception var4) {
                  allowed = false;
               } finally {
                  checkedAt = System.currentTimeMillis();
                  checking = false;
               }
            }, "Bauloo Styles key check");
            thread.setDaemon(true);
            thread.start();
         }

         return Boolean.TRUE.equals(allowed);
      } else {
         return true;
      }
   }

   public static void recheck() {
      checkedAt = 0L;
      allowed = null;
   }

   public static List<MyPacks.Style> styles() {
      return styles;
   }

   public static int version() {
      return version;
   }

   public static boolean scanning() {
      return scanning;
   }

   public static int scanned() {
      return scanned;
   }

   public static int total() {
      return total;
   }

   public static int packCount() {
      return packCount;
   }

   public static MyPacks.Style byId(String id) {
      for (MyPacks.Style s : styles) {
         if (s.id().equals(id)) {
            return s;
         }
      }

      return null;
   }

   public static synchronized void ensureScanned() {
      if (!started || !scanning && !signature().equals(lastSignature)) {
         rescan();
      }
   }

   private static String signature() {
      StringBuilder sb = new StringBuilder(String.join(",", StylesPackFilter.categories())).append('|');
      Path folder = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");

      try (Stream<Path> list = Files.list(folder)) {
         list.filter(p -> {
            String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
            return n.endsWith(".zip") && !n.startsWith("bauloo_");
         }).sorted().forEach(p -> {
            try {
               sb.append(p.getFileName()).append(':').append(Files.size(p)).append(':').append(Files.getLastModifiedTime(p).toMillis()).append(';');
            } catch (IOException var3) {
            }
         });
      } catch (IOException var7) {
      }

      return sb.toString();
   }

   public static synchronized void rescan() {
      if (!scanning) {
         started = true;
         scanning = true;
         Thread thread = new Thread(MyPacks::scan, "Bauloo Styles pack scan");
         thread.setDaemon(true);
         thread.setPriority(1);
         thread.start();
      }
   }

   private static void scan() {
      lastSignature = signature();
      String cats = String.join(",", StylesPackFilter.categories());

      try {
         Path folder = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
         List<Path> zips = new ArrayList<>();
         if (Files.isDirectory(folder)) {
            try (Stream<Path> list = Files.list(folder)) {
               list.filter(p -> {
                  String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
                  return n.endsWith(".zip") && !n.startsWith("bauloo_") && Files.isRegularFile(p);
               }).sorted().forEach(zips::add);
            }
         }

         total = zips.size();
         scanned = 0;
         JsonObject cache = readIndex();
         if (!cats.equals(string(cache, "_cats"))) {
            cache = new JsonObject();
         }

         JsonObject next = new JsonObject();
         next.addProperty("_cats", cats);
         Map<String, MyPacks.Style> found = new TreeMap<>();

         for (Path zip : zips) {
            String key = zip.getFileName().toString();
            String stamp = Files.size(zip) + ":" + Files.getLastModifiedTime(zip).toMillis();
            JsonObject entry = cache.has(key) && cache.get(key).isJsonObject() ? cache.getAsJsonObject(key) : null;
            Map<String, String> parts;
            if (entry != null && stamp.equals(string(entry, "stamp"))) {
               parts = new TreeMap<>();

               for (Entry<String, JsonElement> e : entry.getAsJsonObject("parts").entrySet()) {
                  parts.put(e.getKey(), e.getValue().getAsString());
               }
            } else {
               parts = read(zip);
            }

            JsonObject out = new JsonObject();
            out.addProperty("stamp", stamp);
            JsonObject partsJson = new JsonObject();
            parts.forEach(partsJson::addProperty);
            out.add("parts", partsJson);
            next.add(key, out);

            for (Entry<String, String> part : parts.entrySet()) {
               String id = part.getKey() + "_" + part.getValue().substring(0, 12);
               found.putIfAbsent(id, new MyPacks.Style(id, part.getKey(), cleanName(key), key, part.getValue()));
            }

            scanned++;
            if (scanned % 5 == 0) {
               publish(found, zips.size());
            }
         }

         publish(found, zips.size());
         writeIndex(next);
         previews(folder);
      } catch (Exception var24) {
         BaulooStylesClient.LOGGER.warn("[MyPacks] scan failed: {}", var24.toString());
      } finally {
         scanning = false;
         version++;
      }
   }

   private static void publish(Map<String, MyPacks.Style> found, int packs) {
      List<MyPacks.Style> list = new ArrayList<>(found.values());
      list.sort((a, b) -> {
         int c = Integer.compare(order(a.category()), order(b.category()));
         return c != 0 ? c : a.name().compareToIgnoreCase(b.name());
      });
      styles = List.copyOf(list);
      packCount = packs;
      version++;
   }

   private static int order(String category) {
      BaulooStylesClient.Tab[] tabs = BaulooStylesClient.Tab.values();

      for (int i = 0; i < tabs.length; i++) {
         if (tabs[i].prefix.equals(category)) {
            return i;
         }
      }

      return tabs.length;
   }

   private static Map<String, String> read(Path zip) {
      Map<String, String> parts = new TreeMap<>();

      try (ZipFile file = new ZipFile(zip.toFile())) {
         Map<String, List<ZipEntry>> byCategory = new HashMap<>();

         for (ZipEntry entry : Collections.list(file.entries())) {
            if (!entry.isDirectory()) {
               for (String category : StylesPackFilter.categories()) {
                  String target = StylesPackFilter.target(entry.getName(), category);
                  if (target != null && target.endsWith(".png")) {
                     byCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(entry);
                  }
               }
            }
         }

         for (Entry<String, List<ZipEntry>> e : byCategory.entrySet()) {
            List<ZipEntry> entries = e.getValue();
            entries.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            for (ZipEntry entryx : entries) {
               md.update(StylesPackFilter.target(entryx.getName(), e.getKey()).getBytes(StandardCharsets.UTF_8));

               try (InputStream in = file.getInputStream(entryx)) {
                  md.update(in.readAllBytes());
               }
            }

            parts.put(e.getKey(), HexFormat.of().formatHex(md.digest()));
         }
      } catch (Exception var17) {
         BaulooStylesClient.LOGGER.warn("[MyPacks] could not read {}: {}", zip.getFileName(), var17.toString());
      }

      return parts;
   }

   private static void previews(Path folder) throws IOException {
      Files.createDirectories(PREVIEWS);

      for (MyPacks.Style s : styles) {
         Path png = PREVIEWS.resolve(s.id() + ".png");
         if (!Files.exists(png) && !NO_PICTURE.contains(s.id())) {
            Path zip = folder.resolve(s.pack());
            boolean made = "sky".equals(s.category()) ? StylesUploadServer.skyPreview(zip, png) : StylesAutoPreview.render(zip, s.category(), png);
            if (!made) {
               NO_PICTURE.add(s.id());
            }

            version++;
         }
      }
   }

   public static Identifier preview(String id) {
      Identifier ready = TEXTURES.get(id);
      if (ready != null) {
         return ready;
      } else {
         Path png = PREVIEWS.resolve(id + ".png");
         if (!Files.exists(png)) {
            return null;
         } else {
            try {
               Identifier texture = Identifier.fromNamespaceAndPath("bauloo_styles", "mypacks/" + id.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_"));
               NativeImage image = PreviewTexture.shrink(NativeImage.read(Files.readAllBytes(png)));
               Minecraft.getInstance().getTextureManager().register(texture, new PreviewTexture(texture, image));
               TEXTURES.put(id, texture);
               return texture;
            } catch (RuntimeException | IOException var5) {
               return null;
            }
         }
      }
   }

   public static String use(MyPacks.Style s) {
      try {
         Path folder = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
         Path target = StylesUploadManager.root().resolve(s.category());
         Files.createDirectories(target);
         String base = s.name().isEmpty() ? s.category() : s.name();
         Path zip = target.resolve(base + ".zip");
         if (!Files.exists(zip)) {
            if (StylesPackFilter.extract(folder.resolve(s.pack()), s.category(), zip) == 0) {
               return null;
            }

            Path png = PREVIEWS.resolve(s.id() + ".png");
            if (Files.exists(png)) {
               Files.copy(png, target.resolve(base + ".png"), StandardCopyOption.REPLACE_EXISTING);
            }

            StylesUploadManager.changed();
         }

         StylesUploadManager.sync(s.category());
         return StylesUploadManager.packIdFor(s.category(), zip);
      } catch (IOException var6) {
         BaulooStylesClient.LOGGER.warn("[MyPacks] could not use {}: {}", s.pack(), var6.toString());
         return null;
      }
   }

   static String cleanName(String file) {
      String n = file.replaceFirst("(?i)\\.zip$", "").replaceAll("§.", "").replace('_', ' ');
      n = n.replaceAll("^[\\s!#.\\-]+", "").replaceAll("\\s+", " ").trim();
      n = n.replaceAll("[\\\\/:*?\"<>|]", "");
      return n.length() > 40 ? n.substring(0, 40).trim() : n;
   }

   private static String string(JsonObject o, String key) {
      return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsString() : null;
   }

   private static JsonObject readIndex() {
      try {
         if (Files.exists(INDEX)) {
            return JsonParser.parseString(Files.readString(INDEX)).getAsJsonObject();
         }
      } catch (Exception var1) {
      }

      return new JsonObject();
   }

   private static void writeIndex(JsonObject index) {
      try {
         Files.createDirectories(CACHE);
         Files.writeString(INDEX, new Gson().toJson(index));
      } catch (IOException var2) {
         BaulooStylesClient.LOGGER.warn("[MyPacks] could not save the scan: {}", var2.toString());
      }
   }

   public record Style(String id, String category, String name, String pack, String hash) {
   }
}
