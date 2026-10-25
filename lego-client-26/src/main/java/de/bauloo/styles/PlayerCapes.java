package de.bauloo.styles;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;

public final class PlayerCapes {
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();
   private static final long REFRESH_MS = 600000L;
   private static volatile Map<String, PlayerCapes.Info> list = Map.of();
   private static volatile long loadedAt;
   private static volatile boolean loading;
   private static final Map<String, AnimatedCape> CAPES = new HashMap<>();

   private PlayerCapes() {
   }

   public static Identifier texture(String name) {
      if (name != null && StylesPresence.isAdmin()) {
         refresh();
         PlayerCapes.Info info = list.get(name.toLowerCase(Locale.ROOT));
         if (info == null) {
            return null;
         } else {
            AnimatedCape cape = CAPES.computeIfAbsent(
               info.id(), id -> new AnimatedCape(Identifier.fromNamespaceAndPath("bauloo_styles", "cosmetics/player/" + id), frames(id), info.fps())
            );
            return cape.current();
         }
      } else {
         return null;
      }
   }

   private static void refresh() {
      if (!loading && System.currentTimeMillis() - loadedAt >= 600000L) {
         loading = true;
         Thread t = new Thread(() -> {
            try {
               HttpResponse<String> r = get("/admin/pcapes", BodyHandlers.ofString());
               if (r.statusCode() == 200) {
                  Map<String, PlayerCapes.Info> next = new HashMap<>();
                  JsonObject o = JsonParser.parseString(r.body()).getAsJsonObject();

                  for (String player : o.keySet()) {
                     JsonObject e = o.getAsJsonObject(player);
                     String id = e.get("id").getAsString();
                     if (id.matches("[a-z0-9_]{1,80}")) {
                        next.put(player.toLowerCase(Locale.ROOT), new PlayerCapes.Info(id, e.has("fps") ? e.get("fps").getAsInt() : 12));
                     }
                  }

                  list = next;
               }
            } catch (Exception var10) {
               BaulooStylesClient.LOGGER.warn("[PlayerCapes] list failed: {}", var10.toString());
            } finally {
               loadedAt = System.currentTimeMillis();
               loading = false;
            }
         }, "Bauloo player capes");
         t.setDaemon(true);
         t.start();
      }
   }

   private static AnimatedCape.FrameSource frames(final String id) {
      return new AnimatedCape.FrameSource() {
         private List<BufferedImage> images;

         private synchronized List<BufferedImage> images() throws IOException {
            if (this.images == null) {
               List<String> names = new ArrayList<>();
               Map<String, BufferedImage> byName = new HashMap<>();

               try {
                  HttpResponse<byte[]> r = PlayerCapes.get("/admin/pcape/" + id, BodyHandlers.ofByteArray());
                  if (r.statusCode() != 200) {
                     throw new IOException("cape " + id + ": " + r.statusCode());
                  }

                  ZipEntry e;
                  try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(r.body()))) {
                     while ((e = zip.getNextEntry()) != null) {
                        String n = e.getName().toLowerCase(Locale.ROOT);
                        if (n.endsWith(".png") || n.endsWith(".jpg")) {
                           byName.put(n, ImageIO.read(new ByteArrayInputStream(zip.readAllBytes())));
                           names.add(n);
                        }
                     }
                  }
               } catch (InterruptedException var9) {
                  Thread.currentThread().interrupt();
                  throw new IOException(var9);
               }

               names.sort(null);
               List<BufferedImage> out = new ArrayList<>();

               for (String n : names) {
                  out.add(byName.get(n));
               }

               this.images = out;
            }

            return this.images;
         }

         @Override
         public int count() {
            try {
               return this.images().size();
            } catch (IOException var2) {
               BaulooStylesClient.LOGGER.warn("[PlayerCapes] {}", var2.toString());
               return 0;
            }
         }

         @Override
         public BufferedImage read(int index) throws IOException {
            return this.images().get(index);
         }
      };
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

   private record Info(String id, int fps) {
   }
}
