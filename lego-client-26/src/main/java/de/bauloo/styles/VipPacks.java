package de.bauloo.styles;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class VipPacks {
   private static final Set<String> ROLES = Set.of("vip", "media", "uploader", "admin");
   private static final Path DIR = FabricLoader.getInstance().getConfigDir().resolve("bauloo-styles/vip");
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();
   private static volatile List<VipPacks.Style> styles = List.of();
   private static volatile int version;
   private static volatile boolean loading;
   private static volatile long loadedAt;
   private static volatile String status = "";
   private static volatile Boolean allowed;
   private static volatile long checkedAt;
   private static volatile boolean checking;
   private static final Map<String, Identifier> TEXTURES = new HashMap<>();
   private static final Set<String> PREVIEW_LOADING = ConcurrentHashMap.newKeySet();
   private static volatile String keyRole;

   private VipPacks() {
   }

   public static boolean shown() {
      if ((allowed == null || System.currentTimeMillis() - checkedAt > 300000L) && !checking) {
         checking = true;
         run(() -> {
            try {
               String role = StylesUploadServer.keyRole();
               keyRole = role;
               allowed = role != null && ROLES.contains(role);
            } catch (Exception var4) {
               allowed = false;
            } finally {
               checkedAt = System.currentTimeMillis();
               checking = false;
            }
         });
      }

      return Boolean.TRUE.equals(allowed);
   }

   public static boolean earlyActive() {
      return StylesUiConfig.earlyAccess && shown();
   }

   public static boolean admin() {
      shown();
      return "admin".equals(keyRole);
   }

   public static List<VipPacks.Style> styles() {
      return styles;
   }

   public static int version() {
      return version;
   }

   public static boolean loading() {
      return loading;
   }

   public static String status() {
      return status;
   }

   public static VipPacks.Style byId(String id) {
      for (VipPacks.Style s : styles) {
         if (s.id().equals(id)) {
            return s;
         }
      }

      return null;
   }

   public static void refresh(boolean force) {
      if (!loading && (force || System.currentTimeMillis() - loadedAt >= 600000L)) {
         loading = true;
         run(
            () -> {
               try {
                  HttpResponse<String> r = get("/vip/index", BodyHandlers.ofString());
                  if (r.statusCode() == 200) {
                     List<VipPacks.Style> out = new ArrayList<>();

                     for (JsonElement e : JsonParser.parseString(r.body()).getAsJsonObject().getAsJsonArray("styles")) {
                        JsonObject o = e.getAsJsonObject();
                        String id = o.get("id").getAsString();
                        if (id.matches("[a-z0-9_]{1,80}")) {
                           out.add(
                              new VipPacks.Style(
                                 id,
                                 o.get("category").getAsString(),
                                 o.get("name").getAsString(),
                                 o.get("sha256").getAsString(),
                                 o.get("size").getAsLong(),
                                 o.has("preview") ? o.get("preview").getAsString().replaceAll("[^0-9a-f]", "") : ""
                              )
                           );
                        }
                     }

                     styles = List.copyOf(out);
                     status = "";
                     loadedAt = System.currentTimeMillis();
                     version++;
                     return;
                  }

                  status = message(r.body(), "Bauloo's Packs are not reachable right now");
               } catch (Exception var9) {
                  status = "Bauloo's Packs are not reachable right now";
                  return;
               } finally {
                  loadedAt = System.currentTimeMillis();
                  loading = false;
               }
            }
         );
      }
   }

   public static Identifier preview(String id) {
      Identifier ready = TEXTURES.get(id);
      if (ready != null) {
         return ready;
      } else {
         VipPacks.Style s = byId(id);
         if (s != null && PREVIEW_LOADING.add(id)) {
            run(() -> {
               try {
                  String stamp = s.preview().isEmpty() ? s.sha256().substring(0, 8) : s.preview();
                  Path png = DIR.resolve("previews").resolve(id + "_" + stamp + ".png");
                  byte[] bytes;
                  if (Files.exists(png)) {
                     bytes = Files.readAllBytes(png);
                  } else {
                     HttpResponse<byte[]> r = get("/vip/preview/" + id, BodyHandlers.ofByteArray());
                     if (r.statusCode() != 200 || r.body().length < 8) {
                        return;
                     }

                     bytes = r.body();
                     Files.createDirectories(png.getParent());
                     Files.write(png, bytes);
                  }

                  Minecraft.getInstance().execute(() -> {
                     try {
                        Identifier texture = Identifier.fromNamespaceAndPath("bauloo_styles", "vip/" + id);
                        NativeImage image = PreviewTexture.shrink(NativeImage.read(bytes));
                        Minecraft.getInstance().getTextureManager().register(texture, new PreviewTexture(texture, image));
                        TEXTURES.put(id, texture);
                     } catch (RuntimeException | IOException var4x) {
                     }
                  });
               } catch (Exception var6) {
                  PREVIEW_LOADING.remove(id);
               }
            });
            return null;
         } else {
            return null;
         }
      }
   }

   public static String use(VipPacks.Style s) {
      try {
         Path zip = DIR.resolve("packs").resolve(s.id() + ".zip");
         if (!Files.exists(zip) || !sha256(Files.readAllBytes(zip)).equals(s.sha256())) {
            status = "Downloading " + s.name() + " ...";
            HttpResponse<byte[]> r = get("/vip/pack/" + s.id(), BodyHandlers.ofByteArray());
            if (r.statusCode() != 200 || !sha256(r.body()).equals(s.sha256())) {
               status = r.statusCode() == 403 ? "VIP only" : "Download failed";
               return null;
            }

            Files.createDirectories(zip.getParent());
            Files.write(zip, r.body());
         }

         Path folder = StylesUploadManager.root().resolve(s.category());
         Files.createDirectories(folder);
         String clean = s.name().replaceAll("[^A-Za-z0-9 ._()-]", "").trim();
         Path target = folder.resolve((clean.isEmpty() ? s.id() : clean) + ".zip");
         if (!Files.exists(target)) {
            if (StylesPackFilter.extract(zip, s.category(), target) == 0) {
               status = "Nothing usable in this style";
               return null;
            }

            StylesUploadManager.changed();
         }

         StylesUploadManager.sync(s.category());
         status = "";
         return StylesUploadManager.packIdFor(s.category(), target);
      } catch (Exception var5) {
         BaulooStylesClient.LOGGER.warn("[VipPacks] {} could not be used: {}", s.id(), var5.toString());
         status = "Download failed";
         return null;
      }
   }

   private static <T> HttpResponse<T> get(String path, BodyHandler<T> handler) throws IOException, InterruptedException {
      String base = StylesCloud.publishUrl();
      if (base == null) {
         throw new IOException("no server");
      } else {
         HttpRequest request = HttpRequest.newBuilder(URI.create(base + path))
            .header("Authorization", "Bearer " + StylesUploadServer.savedKey())
            .header("X-Device", StylesUploadServer.deviceId())
            .timeout(Duration.ofSeconds(60L))
            .GET()
            .build();
         return HTTP.send(request, handler);
      }
   }

   private static String message(String body, String fallback) {
      try {
         JsonObject o = JsonParser.parseString(body).getAsJsonObject();
         return o.has("message") ? o.get("message").getAsString() : fallback;
      } catch (RuntimeException var3) {
         return fallback;
      }
   }

   private static String sha256(byte[] data) {
      try {
         return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data)).toLowerCase(Locale.ROOT);
      } catch (NoSuchAlgorithmException var2) {
         return "";
      }
   }

   private static void run(Runnable task) {
      Thread t = new Thread(task, "Bauloo Styles VIP packs");
      t.setDaemon(true);
      t.start();
   }

   public record Style(String id, String category, String name, String sha256, long size, String preview) {
   }
}
