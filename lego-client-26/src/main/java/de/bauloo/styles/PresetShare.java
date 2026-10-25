package de.bauloo.styles;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.EnumMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.minecraft.client.Minecraft;

public final class PresetShare {
   private static final String UPLOAD = "bauloo_upload_";
   private static final long MAX_FILE = 4194304L;
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();

   private PresetShare() {
   }

   public static Map<BaulooStylesClient.Tab, Path> localSources(StylesPresets.Preset preset) {
      Map<BaulooStylesClient.Tab, Path> out = new EnumMap<>(BaulooStylesClient.Tab.class);
      preset.styles().forEach((tab, folder) -> {
         if (folder != null && folder.startsWith("bauloo_upload_")) {
            for (Path file : StylesUploadManager.list(tab.prefix)) {
               if (!DrawScreen.isDrawing(file) && StylesUploadManager.packIdFor(tab.prefix, file).equals(folder)) {
                  out.put(tab, file);
                  break;
               }
            }
         }
      });
      return out;
   }

   public static int localCount(StylesPresets.Preset preset) {
      return localSources(preset).size();
   }

   public static JsonObject body(StylesPresets.Preset preset, Consumer<String> say) throws IOException, InterruptedException {
      String base = base();
      JsonObject styles = new JsonObject();
      preset.styles().forEach((tab, folder) -> {
         if (folder != null && !folder.startsWith("bauloo_upload_")) {
            styles.addProperty(tab.prefix, folder);
         }
      });
      JsonObject local = new JsonObject();
      Map<BaulooStylesClient.Tab, Path> sources = localSources(preset);
      int n = 0;

      for (Entry<BaulooStylesClient.Tab, Path> e : sources.entrySet()) {
         say.accept("Uploading your styles " + ++n + "/" + sources.size() + " ...");
         byte[] zip = zipOf(e.getValue());
         if (zip.length <= 4194304L) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/lfile"))
               .timeout(Duration.ofSeconds(60L))
               .header("Content-Type", "application/zip")
               .POST(BodyPublishers.ofByteArray(zip))
               .build();
            HttpResponse<String> response = HTTP.send(request, BodyHandlers.ofString());
            JsonObject answer = JsonParser.parseString(response.body()).getAsJsonObject();
            if (response.statusCode() != 200 || !answer.has("sha")) {
               throw new IOException(answer.has("message") ? answer.get("message").getAsString() : "Upload failed");
            }

            JsonObject file = new JsonObject();
            file.addProperty("sha", answer.get("sha").getAsString());
            file.addProperty("name", e.getValue().getFileName().toString().replaceFirst("\\.zip$", ""));
            local.add(e.getKey().prefix, file);
         } else {
            say.accept(e.getKey().title + " style is over 4 MB - left out");
         }
      }

      JsonObject body = new JsonObject();
      body.addProperty("name", preset.name());
      body.addProperty("author", Minecraft.getInstance().getUser().getName());
      body.addProperty("device", StylesStats.deviceHash());
      body.add("styles", styles);
      body.add("local", local);
      JsonObject extras = new JsonObject();
      preset.extras().forEach(extras::addProperty);
      body.add("extras", extras);
      return body;
   }

   public static String makeCode(StylesPresets.Preset preset, Consumer<String> say) throws IOException, InterruptedException {
      JsonObject answer = post("/presets/code", body(preset, say));
      if (!answer.has("code")) {
         throw new IOException(answer.has("message") ? answer.get("message").getAsString() : "Could not make a code");
      } else {
         return answer.get("code").getAsString();
      }
   }

   public static String redeem(String code, Consumer<String> say) throws IOException, InterruptedException {
      JsonObject body = new JsonObject();
      body.addProperty("code", code);
      JsonObject answer = post("/presets/redeem", body);
      if (!answer.has("preset")) {
         throw new IOException(answer.has("message") ? answer.get("message").getAsString() : "Code not found");
      } else {
         JsonObject p = answer.getAsJsonObject("preset");
         Map<BaulooStylesClient.Tab, String> styles = readStyles(p.getAsJsonObject("styles"));
         styles.putAll(install(readLocal(p.getAsJsonObject("local")), say));
         String name = p.get("name").getAsString();
         String author = p.get("author").getAsString();
         StylesPresets.add(name, styles, StylesPresets.readExtras(p), author, true);
         return name;
      }
   }

   public static Map<BaulooStylesClient.Tab, String> readStyles(JsonObject s) {
      Map<BaulooStylesClient.Tab, String> styles = new EnumMap<>(BaulooStylesClient.Tab.class);
      if (s != null) {
         for (BaulooStylesClient.Tab tab : BaulooStylesClient.Tab.values()) {
            if (s.has(tab.prefix) && s.get(tab.prefix).isJsonPrimitive()) {
               styles.put(tab, s.get(tab.prefix).getAsString());
            }
         }
      }

      return styles;
   }

   public static Map<BaulooStylesClient.Tab, PresetShare.LocalFile> readLocal(JsonObject o) {
      Map<BaulooStylesClient.Tab, PresetShare.LocalFile> local = new EnumMap<>(BaulooStylesClient.Tab.class);
      if (o != null) {
         for (BaulooStylesClient.Tab tab : BaulooStylesClient.Tab.values()) {
            JsonElement e = o.get(tab.prefix);
            if (e != null && e.isJsonObject() && e.getAsJsonObject().has("sha") && e.getAsJsonObject().has("name")) {
               local.put(tab, new PresetShare.LocalFile(e.getAsJsonObject().get("sha").getAsString(), e.getAsJsonObject().get("name").getAsString()));
            }
         }
      }

      return local;
   }

   public static Map<BaulooStylesClient.Tab, String> install(Map<BaulooStylesClient.Tab, PresetShare.LocalFile> files, Consumer<String> say) throws IOException, InterruptedException {
      Map<BaulooStylesClient.Tab, String> out = new EnumMap<>(BaulooStylesClient.Tab.class);
      if (files.isEmpty()) {
         return out;
      } else {
         String base = base();
         int n = 0;

         for (Entry<BaulooStylesClient.Tab, PresetShare.LocalFile> e : files.entrySet()) {
            say.accept("Downloading styles " + ++n + "/" + files.size() + " ...");
            BaulooStylesClient.Tab tab = e.getKey();
            PresetShare.LocalFile file = e.getValue();
            if (file.sha().matches("[0-9a-f]{64}")) {
               HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/lfile/" + file.sha())).timeout(Duration.ofSeconds(60L)).GET().build();
               HttpResponse<byte[]> response = HTTP.send(request, BodyHandlers.ofByteArray());
               if (response.statusCode() == 200 && sha256(response.body()).equals(file.sha())) {
                  Path folder = StylesUploadManager.root().resolve(tab.prefix);
                  Files.createDirectories(folder);
                  String clean = file.name().replaceAll("[^A-Za-z0-9 ._()-]", "").trim();
                  if (clean.isEmpty()) {
                     clean = tab.prefix;
                  }

                  Path temp = Files.createTempFile("bauloo-shared-", ".zip");

                  try {
                     Files.write(temp, response.body());
                     Path target = folder.resolve(clean + ".zip");

                     for (int k = 2; Files.exists(target) && !sameFile(target, temp, tab.prefix); k++) {
                        target = folder.resolve(clean + " " + k + ".zip");
                     }

                     if (!Files.exists(target) && StylesPackFilter.extract(temp, tab.prefix, target) == 0) {
                        BaulooStylesClient.LOGGER.warn("[PresetShare] {} ({}): no textures for this collection", tab.prefix, file.name());
                        say.accept(tab.title + " style has no textures");
                     } else {
                        BaulooStylesClient.LOGGER.info("[PresetShare] installed {} as {}", file.name(), target.getFileName());
                        StylesUploadManager.changed();
                        StylesUploadManager.sync(tab.prefix);
                        out.put(tab, StylesUploadManager.packIdFor(tab.prefix, target));
                     }
                  } finally {
                     Files.deleteIfExists(temp);
                  }
               } else {
                  BaulooStylesClient.LOGGER
                     .warn("[PresetShare] {} ({}): status {}, {} bytes", new Object[]{tab.prefix, file.sha(), response.statusCode(), response.body().length});
                  say.accept(tab.title + " style could not be downloaded");
               }
            }
         }

         return out;
      }
   }

   private static boolean sameFile(Path existing, Path downloaded, String category) {
      try {
         Path probe = Files.createTempFile(existing.getParent(), "probe-", ".zip");

         boolean var4;
         try {
            StylesPackFilter.extract(downloaded, category, probe);
            var4 = Files.exists(probe) && Files.mismatch(probe, existing) == -1L;
         } finally {
            Files.deleteIfExists(probe);
         }

         return var4;
      } catch (IOException var9) {
         return false;
      }
   }

   private static JsonObject post(String path, JsonObject body) throws IOException, InterruptedException {
      HttpRequest request = HttpRequest.newBuilder(URI.create(base() + path))
         .timeout(Duration.ofSeconds(20L))
         .header("Content-Type", "application/json")
         .POST(BodyPublishers.ofString(body.toString()))
         .build();
      HttpResponse<String> response = HTTP.send(request, BodyHandlers.ofString());
      return JsonParser.parseString(response.body()).getAsJsonObject();
   }

   private static String base() throws IOException {
      String base = StylesCloud.publishUrl();
      if (base == null) {
         throw new IOException("Sharing is not set up yet");
      } else {
         return base;
      }
   }

   private static byte[] zipOf(Path source) throws IOException {
      if (!Files.isDirectory(source)) {
         return Files.readAllBytes(source);
      } else {
         ByteArrayOutputStream bytes = new ByteArrayOutputStream();

         try (
            ZipOutputStream out = new ZipOutputStream(bytes);
            Stream<Path> paths = Files.walk(source);
         ) {
            for (Path path : paths.toList()) {
               if (!Files.isDirectory(path)) {
                  out.putNextEntry(new ZipEntry(source.relativize(path).toString().replace('\\', '/')));
                  Files.copy(path, out);
                  out.closeEntry();
               }
            }
         }

         return bytes.toByteArray();
      }
   }

   private static String sha256(byte[] data) {
      try {
         return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data)).toLowerCase(Locale.ROOT);
      } catch (NoSuchAlgorithmException var2) {
         return "";
      }
   }

   public record LocalFile(String sha, String name) {
   }
}
