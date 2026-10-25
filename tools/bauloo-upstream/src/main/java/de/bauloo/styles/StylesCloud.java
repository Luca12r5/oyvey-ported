package de.bauloo.styles;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1011;
import net.minecraft.class_2960;
import net.minecraft.class_310;

public final class StylesCloud {
   public static final String OWNER = "Bauloo0";
   public static final String REPO = "bauloo-styles-cloud";
   public static final String FOLDER_PREFIX = "bauloo_cloud_";
   private static final String BASE = "https://raw.githubusercontent.com/Bauloo0/bauloo-styles-cloud/main/";
   private static final String[] MIRRORS = new String[]{
      "https://raw.githubusercontent.com/Bauloo0/bauloo-styles-cloud/main/",
      "https://cdn.jsdelivr.net/gh/Bauloo0/bauloo-styles-cloud@main/",
      "https://bauloo-styles-cloud.bauloo-styles-cloud-worker.workers.dev/raw/"
   };
   private static final String[] MIRROR_NAMES = new String[]{"GitHub", "jsDelivr", "the Bauloo server"};
   private static volatile int mirror;
   private static final long MAX_PACK_BYTES = 99614720L;
   private static final long MAX_UNPACKED_BYTES = 419430400L;
   private static final long MAX_PREVIEW_BYTES = 2097152L;
   private static final long REFRESH_MS = 60000L;
   private static final int NEW_DAYS = 7;
   private static final Pattern ID = Pattern.compile("^[a-z][a-z0-9]{1,15}_[a-z0-9_]{1,40}$");
   private static final Pattern SHA = Pattern.compile("^[0-9a-f]{64}$");
   private static final String META = "{\"pack\": {\"description\": \"Bauloo cloud style - by bauloo\", \"pack_format\": 75, \"min_format\": 65, \"max_format\": 9999}}\n";
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).followRedirects(Redirect.NORMAL).build();
   private static final Pattern WORKER = Pattern.compile("^https://[a-z0-9-]+\\.[a-z0-9-]+\\.workers\\.dev$");
   private static volatile List<StylesCloud.Style> styles = List.of();
   private static volatile String publishUrl;
   private static volatile int version;
   private static volatile long lastFetch;
   private static volatile boolean fetching;
   private static volatile boolean cacheLoaded;
   private static final Map<String, CompletableFuture<Boolean>> DOWNLOADS = new ConcurrentHashMap<>();
   private static final Set<String> PREVIEWS_READY = ConcurrentHashMap.newKeySet();
   private static final Set<String> PREVIEWS_LOADING = ConcurrentHashMap.newKeySet();
   private static final long PREVIEW_MAX_AGE_MS = 21600000L;

   static <T> HttpResponse<T> getFile(String path, BodyHandler<T> handler, Duration timeout, Predicate<HttpResponse<T>> ok) {
      HttpResponse<T> last = null;
      int start = mirror;

      for (int k = 0; k < MIRRORS.length; k++) {
         int m = (start + k) % MIRRORS.length;

         try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(MIRRORS[m] + path))
               .header("User-Agent", "bauloo-styles-mod")
               .timeout(timeout)
               .GET()
               .build();
            HttpResponse<T> response = HTTP.send(request, handler);
            last = response;
            if (ok.test(response)) {
               if (m != mirror) {
                  BaulooStylesClient.LOGGER.info("[Cloud] using {} for the cloud files", MIRROR_NAMES[m]);
                  mirror = m;
               }

               return response;
            }
         } catch (Exception e) {
            BaulooStylesClient.LOGGER.info("[Cloud] {} not reachable for {}: {}", new Object[]{MIRROR_NAMES[m], path, e.toString()});
         }
      }

      return last;
   }

   private StylesCloud() {
   }

   public static boolean enabled() {
      return !"OWNER_NOT_SET".equals("Bauloo0");
   }

   public static int version() {
      return version;
   }

   private static Path dir() {
      return FabricLoader.getInstance().getConfigDir().resolve("bauloo-styles/cloud");
   }

   private static Path packFile(StylesCloud.Style s) {
      return FabricLoader.getInstance().getGameDir().resolve("resourcepacks").resolve(s.folder() + ".zip");
   }

   public static void refresh(boolean force) {
      if (enabled()) {
         if (!cacheLoaded) {
            loadCache();
         }

         long now = System.currentTimeMillis();
         if (!fetching && (force || now - lastFetch >= 60000L)) {
            fetching = true;
            HttpRequest api = HttpRequest.newBuilder(URI.create("https://api.github.com/repos/Bauloo0/bauloo-styles-cloud/contents/index.json?ref=main"))
               .header("Accept", "application/vnd.github.raw+json")
               .header("User-Agent", "bauloo-styles-mod")
               .timeout(Duration.ofSeconds(15L))
               .GET()
               .build();
            HTTP.sendAsync(api, BodyHandlers.ofString(StandardCharsets.UTF_8))
               .exceptionally(error -> null)
               .thenApplyAsync(
                  response -> response != null && response.statusCode() == 200
                     ? response
                     : getFile(
                        "index.json",
                        BodyHandlers.ofString(StandardCharsets.UTF_8),
                        Duration.ofSeconds(15L),
                        r -> r.statusCode() == 200 && parse(r.body()) != null
                     )
               )
               .thenAccept(response -> {
                  if (response != null && response.statusCode() == 200) {
                     List<StylesCloud.Style> parsed = parse(response.body());
                     if (parsed != null) {
                        try {
                           Files.createDirectories(dir());
                           Files.writeString(dir().resolve("index.json"), response.body());
                        } catch (IOException var3x) {
                        }

                        publish(parsed);
                     }
                  }
               })
               .whenComplete((ignored, error) -> {
                  lastFetch = System.currentTimeMillis();
                  fetching = false;
               });
         }
      }
   }

   private static void loadCache() {
      cacheLoaded = true;
      Path cached = dir().resolve("index.json");
      if (Files.exists(cached)) {
         try {
            List<StylesCloud.Style> parsed = parse(Files.readString(cached));
            if (parsed != null) {
               publish(parsed);
            }
         } catch (IOException var2) {
         }
      }
   }

   private static void publish(List<StylesCloud.Style> parsed) {
      if (!parsed.equals(styles)) {
         styles = List.copyOf(parsed);
         version++;
      }
   }

   public static String publishUrl() {
      if (!cacheLoaded && enabled()) {
         loadCache();
      }

      return publishUrl;
   }

   private static List<StylesCloud.Style> parse(String json) {
      try {
         JsonObject root = JsonParser.parseString(json).getAsJsonObject();
         String url = string(root, "publishUrl");
         publishUrl = url != null && WORKER.matcher(url).matches() ? url : null;
         if (CloudCategories.read(root)) {
            class_310.method_1551().method_63588(BaulooStylesClient.Tab::bindCloudSlots);
         }

         JsonArray array = root.getAsJsonArray("styles");
         List<StylesCloud.Style> result = new ArrayList<>();

         for (JsonElement element : array) {
            if (element.isJsonObject()) {
               JsonObject o = element.getAsJsonObject();
               String id = string(o, "id");
               String category = string(o, "category");
               String name = string(o, "name");
               String sha = string(o, "sha256");
               String added = string(o, "added");
               long size = o.has("size") ? o.get("size").getAsLong() : -1L;
               if (id != null
                  && category != null
                  && name != null
                  && sha != null
                  && ID.matcher(id).matches()
                  && id.startsWith(category + "_")
                  && CloudCategories.known(category)
                  && SHA.matcher(sha).matches()
                  && size > 0L
                  && size <= 99614720L
                  && !name.isBlank()
                  && name.length() <= 40) {
                  boolean preview = o.has("preview") && ("previews/" + id + ".png").equals(string(o, "preview"));
                  boolean early = o.has("early") && o.get("early").isJsonPrimitive() && o.get("early").getAsBoolean();
                  result.add(new StylesCloud.Style(id, category, name.trim(), sha, size, preview, added == null ? "" : added, early));
               }
            }
         }

         result.sort(Comparator.comparing(StylesCloud.Style::added).reversed());
         return result;
      } catch (Exception e) {
         return null;
      }
   }

   private static String string(JsonObject o, String key) {
      return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsString() : null;
   }

   public static List<StylesCloud.Style> of(String category) {
      if (!cacheLoaded && enabled()) {
         loadCache();
      }

      List<StylesCloud.Style> result = new ArrayList<>();
      boolean early = VipPacks.earlyActive();

      for (StylesCloud.Style s : styles) {
         if (s.category().equals(category) && (early || !s.early())) {
            result.add(s);
         }
      }

      return result;
   }

   public static StylesCloud.Style byFolder(String folder) {
      if (folder != null && folder.startsWith("bauloo_cloud_")) {
         String id = folder.substring("bauloo_cloud_".length());

         for (StylesCloud.Style s : styles) {
            if (s.id().equals(id)) {
               return s;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static boolean isReady(StylesCloud.Style s) {
      try {
         Path marker = dir().resolve("packs").resolve(s.id() + ".v2.sha");
         return Files.exists(packFile(s)) && Files.exists(marker) && Files.readString(marker).trim().equals(s.sha256());
      } catch (IOException e) {
         return false;
      }
   }

   public static boolean isDownloading(StylesCloud.Style s) {
      return DOWNLOADS.containsKey(s.id());
   }

   public static CompletableFuture<Boolean> download(StylesCloud.Style s) {
      return isReady(s)
         ? CompletableFuture.completedFuture(true)
         : DOWNLOADS.computeIfAbsent(s.id(), id -> CompletableFuture.<Boolean>supplyAsync(() -> fetchPack(s)).whenComplete((ok, error) -> DOWNLOADS.remove(id)));
   }

   private static boolean fetchPack(StylesCloud.Style s) {
      Path temp = null;

      try {
         Files.createDirectories(dir().resolve("packs"));
         temp = Files.createTempFile("bauloo-cloud-", ".zip");
         Path file = temp;
         HttpResponse<Path> response = getFile(
            "packs/" + s.id() + ".zip", BodyHandlers.ofFile(temp), Duration.ofMinutes(3L), r -> r.statusCode() == 200 && matches(file, s)
         );
         if (response == null || response.statusCode() != 200 || Files.size(temp) != s.size()) {
            BaulooStylesClient.LOGGER
               .warn(
                  "Cloud style {}: download failed (status {}, {} bytes)",
                  new Object[]{s.id(), response == null ? "no connection" : response.statusCode(), Files.size(temp)}
               );
            return false;
         } else if (!sha256(temp).equals(s.sha256())) {
            BaulooStylesClient.LOGGER.warn("Cloud style {}: checksum does not match, ignored", s.id());
            return false;
         } else {
            Path target = packFile(s);
            Files.createDirectories(target.getParent());
            Path cleaned = Files.createTempFile("bauloo-cloud-clean-", ".zip");
            if (!copyTexturesOnly(temp, cleaned, s.category())) {
               Files.deleteIfExists(cleaned);
               BaulooStylesClient.LOGGER.warn("Cloud style {}: pack rejected", s.id());
               return false;
            } else {
               Files.move(cleaned, target, StandardCopyOption.REPLACE_EXISTING);
               Files.writeString(dir().resolve("packs").resolve(s.id() + ".v2.sha"), s.sha256());
               return true;
            }
         }
      } catch (Exception e) {
         BaulooStylesClient.LOGGER.warn("Cloud style {}: {}", s.id(), e.toString());
         return false;
      } finally {
         if (temp != null) {
            try {
               Files.deleteIfExists(temp);
            } catch (IOException var18) {
            }
         }
      }
   }

   private static boolean copyTexturesOnly(Path source, Path destination, String category) throws IOException {
      long unpacked = 0L;
      int kept = 0;
      byte[] buffer = new byte[65536];

      try (
         ZipInputStream in = new ZipInputStream(Files.newInputStream(source));
         ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(destination));
      ) {
         ZipEntry entry;
         while ((entry = in.getNextEntry()) != null) {
            String name = entry.getName().replace('\\', '/');
            if (!entry.isDirectory() && !name.startsWith("/") && !name.contains("..")) {
               boolean allowed = "sky".equals(category)
                  ? name.startsWith("assets/minecraft/optifine/sky/")
                  : name.startsWith("assets/") || name.equals("pack.png");
               if (allowed) {
                  out.putNextEntry(new ZipEntry(name));

                  int read;
                  while ((read = in.read(buffer)) > 0) {
                     unpacked += read;
                     if (unpacked > 419430400L) {
                        return false;
                     }

                     out.write(buffer, 0, read);
                  }

                  out.closeEntry();
                  kept++;
               }
            }
         }

         out.putNextEntry(new ZipEntry("pack.mcmeta"));
         out.write(
            "{\"pack\": {\"description\": \"Bauloo cloud style - by bauloo\", \"pack_format\": 75, \"min_format\": 65, \"max_format\": 9999}}\n"
               .getBytes(StandardCharsets.UTF_8)
         );
         out.closeEntry();
      }

      return kept > 0;
   }

   private static boolean matches(Path file, StylesCloud.Style s) {
      try {
         return Files.size(file) == s.size() && sha256(file).equals(s.sha256());
      } catch (Exception e) {
         return false;
      }
   }

   private static String sha256(Path file) throws Exception {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      try (InputStream in = Files.newInputStream(file)) {
         byte[] buffer = new byte[65536];

         int read;
         while ((read = in.read(buffer)) > 0) {
            digest.update(buffer, 0, read);
         }
      }

      return HexFormat.of().formatHex(digest.digest());
   }

   public static class_2960 preview(StylesCloud.Style s) {
      class_2960 id = class_2960.method_60655("bauloo_styles", "cloud/" + s.id() + "_" + s.sha256().substring(0, 8));
      if (PREVIEWS_READY.contains(id.method_12832())) {
         return id;
      }

      if (s.hasPreview() && PREVIEWS_LOADING.add(id.method_12832())) {
         CompletableFuture.runAsync(() -> loadPreview(s, id));
      }

      return null;
   }

   private static void loadPreview(StylesCloud.Style s, class_2960 id) {
      try {
         Path cached = dir().resolve("previews").resolve(id.method_12832().substring("cloud/".length()) + ".png");
         byte[] bytes = null;
         boolean fresh = Files.exists(cached) && System.currentTimeMillis() - Files.getLastModifiedTime(cached).toMillis() < 21600000L;
         if (!fresh) {
            try {
               HttpResponse<byte[]> response = getFile(
                  "previews/" + s.id() + ".png", BodyHandlers.ofByteArray(), Duration.ofSeconds(20L), r -> r.statusCode() == 200 && r.body().length > 8
               );
               if (response != null && response.statusCode() == 200 && response.body().length <= 2097152L) {
                  bytes = response.body();
                  Files.createDirectories(cached.getParent());

                  try (OutputStream out = Files.newOutputStream(cached)) {
                     out.write(bytes);
                  }
               }
            } catch (IOException var11) {
            }
         }

         if (bytes == null) {
            if (!Files.exists(cached)) {
               PREVIEWS_LOADING.remove(id.method_12832());
               return;
            }

            bytes = Files.readAllBytes(cached);
         }

         byte[] png = bytes;
         class_310.method_1551().method_63588(() -> {
            try {
               class_1011 image = PreviewTexture.shrink(class_1011.method_49277(png));
               class_310.method_1551().method_1531().method_4616(id, new PreviewTexture(id, image));
               PREVIEWS_READY.add(id.method_12832());
            } catch (IOException e) {
               BaulooStylesClient.LOGGER.warn("Cloud style {}: preview unreadable", s.id());
            }
         });
      } catch (Exception e) {
         PREVIEWS_LOADING.remove(id.method_12832());
      }
   }

   public record Style(String id, String category, String name, String sha256, long size, boolean hasPreview, String added, boolean early) {
      public String folder() {
         return "bauloo_cloud_" + this.id;
      }

      public boolean isNew() {
         try {
            return !LocalDate.parse(this.added).isBefore(LocalDate.now().minusDays(7L));
         } catch (Exception e) {
            return false;
         }
      }
   }
}
