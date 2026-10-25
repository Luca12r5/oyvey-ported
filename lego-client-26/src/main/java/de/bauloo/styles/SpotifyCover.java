package de.bauloo.styles;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class SpotifyCover {
   public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("bauloo_styles", "spotify_cover");
   public static final int SIZE = 300;
   public static volatile String coverKey;
   public static volatile int color = -1;
   public static volatile long durationMs;
   private static volatile String wanted;
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).build();
   private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "Bauloo Styles Spotify cover");
      thread.setDaemon(true);
      return thread;
   });

   private SpotifyCover() {
   }

   public static void request(String artist, String title) {
      String key = artist + "\n" + title;
      wanted = key;
      durationMs = 0L;
      WORKER.submit(() -> fetch(artist, title, key));
   }

   private static void fetch(String artist, String title, String key) {
      try {
         if (fromSpotify(title, key)) {
            return;
         }

         String clean = title.replaceAll("\\s*[(\\[](feat|ft|with|prod)[^)\\]]*[)\\]]", "").replaceAll("\\s+-\\s+.*$", "").trim();
         String[] found = deezer(artist, title);
         if (found == null && !clean.equals(title)) {
            found = deezer(artist, clean);
         }

         if (found == null) {
            found = itunes(artist, title);
         }

         if (found == null && !clean.equals(title)) {
            found = itunes(artist, clean);
         }

         if (found == null || !key.equals(wanted)) {
            BaulooStylesClient.LOGGER.info("No cover found for {} - {}", artist, title);
            return;
         }

         durationMs = Long.parseLong(found[1]);
         HttpResponse<byte[]> art = HTTP.send(HttpRequest.newBuilder(URI.create(found[0])).timeout(Duration.ofSeconds(10L)).build(), BodyHandlers.ofByteArray());
         BufferedImage image = ImageIO.read(new ByteArrayInputStream(art.body()));
         if (image == null || !key.equals(wanted)) {
            return;
         }

         NativeImage cover = rounded(image);
         int main = mainColor(image);
         Minecraft.getInstance().schedule(() -> {
            if (!key.equals(wanted)) {
               cover.close();
            } else {
               Minecraft.getInstance().getTextureManager().register(TEXTURE, new DynamicTexture(TEXTURE::toString, cover));
               coverKey = key;
               color = main;
            }
         });
      } catch (Exception var9) {
         BaulooStylesClient.LOGGER.info("No cover for {} - {}: {}", new Object[]{artist, title, var9.toString()});
      }
   }

   private static boolean fromSpotify(String title, String key) {
      for (int attempt = 0; attempt < 3; attempt++) {
         try {
            Thread.sleep(attempt == 0 ? 800L : 1500L);
            if (!key.equals(wanted)) {
               return true;
            }

            Object[] cover = SpotifySmtc.cover();
            if (cover != null && title.equals(cover[0])) {
               BufferedImage image = ImageIO.read(new ByteArrayInputStream((byte[])cover[1]));
               if (image != null) {
                  show(image, key);
                  return true;
               }
            }
         } catch (Throwable var5) {
            return false;
         }
      }

      return false;
   }

   private static void show(BufferedImage image, String key) {
      NativeImage cover = rounded(image);
      int main = mainColor(image);
      Minecraft.getInstance().schedule(() -> {
         if (!key.equals(wanted)) {
            cover.close();
         } else {
            Minecraft.getInstance().getTextureManager().register(TEXTURE, new DynamicTexture(TEXTURE::toString, cover));
            coverKey = key;
            color = main;
         }
      });
   }

   private static String[] deezer(String artist, String title) throws Exception {
      JsonObject answer = get("https://api.deezer.com/search?limit=5&q=" + URLEncoder.encode(artist + " " + title, StandardCharsets.UTF_8));
      JsonArray data = answer.getAsJsonArray("data");
      if (data == null) {
         return null;
      } else {
         for (JsonElement element : data) {
            JsonObject song = element.getAsJsonObject();
            String name = song.getAsJsonObject("artist").get("name").getAsString();
            if (sameArtist(artist, name) && song.getAsJsonObject("album").has("cover_big")) {
               return new String[]{song.getAsJsonObject("album").get("cover_big").getAsString(), String.valueOf(song.get("duration").getAsLong() * 1000L)};
            }
         }

         return null;
      }
   }

   private static String[] itunes(String artist, String title) throws Exception {
      JsonObject answer = get(
         "https://itunes.apple.com/search?media=music&entity=song&limit=5&term=" + URLEncoder.encode(artist + " " + title, StandardCharsets.UTF_8)
      );
      JsonArray results = answer.getAsJsonArray("results");
      if (results == null) {
         return null;
      } else {
         for (JsonElement element : results) {
            JsonObject song = element.getAsJsonObject();
            if (sameArtist(artist, song.get("artistName").getAsString()) && song.has("artworkUrl100")) {
               return new String[]{
                  song.get("artworkUrl100").getAsString().replace("100x100bb", "300x300bb"),
                  String.valueOf(song.has("trackTimeMillis") ? song.get("trackTimeMillis").getAsLong() : 0L)
               };
            }
         }

         return null;
      }
   }

   private static boolean sameArtist(String spotify, String found) {
      String f = found.toLowerCase(Locale.ROOT);

      for (String part : spotify.toLowerCase(Locale.ROOT).split(",")) {
         String p = part.trim();
         if (!p.isEmpty() && (f.contains(p) || p.contains(f))) {
            return true;
         }
      }

      return false;
   }

   private static JsonObject get(String url) throws Exception {
      HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10L)).build(), BodyHandlers.ofString());
      return JsonParser.parseString(response.body()).getAsJsonObject();
   }

   private static int mainColor(BufferedImage image) {
      long r = 0L;
      long g = 0L;
      long b = 0L;
      int n = 0;

      for (int y = 0; y < image.getHeight(); y += 8) {
         for (int x = 0; x < image.getWidth(); x += 8) {
            int c = image.getRGB(x, y);
            r += c >> 16 & 0xFF;
            g += c >> 8 & 0xFF;
            b += c & 0xFF;
            n++;
         }
      }

      float[] hsb = Color.RGBtoHSB((int)(r / n), (int)(g / n), (int)(b / n), null);
      return Color.HSBtoRGB(hsb[0], Math.min(1.0F, hsb[1] * 1.4F + 0.15F), Math.max(hsb[2], 0.9F)) & 16777215;
   }

   private static NativeImage rounded(BufferedImage source) {
      NativeImage out = new NativeImage(300, 300, false);
      float radius = 42.0F;

      for (int y = 0; y < 300; y++) {
         for (int x = 0; x < 300; x++) {
            int rgb = source.getRGB(x * source.getWidth() / 300, y * source.getHeight() / 300);
            float cx = Math.max(radius - x - 0.5F, Math.max(0.0F, x + 0.5F - (300.0F - radius)));
            float cy = Math.max(radius - y - 0.5F, Math.max(0.0F, y + 0.5F - (300.0F - radius)));
            float outside = (float)Math.sqrt(Math.max(0.0F, cx) * Math.max(0.0F, cx) + Math.max(0.0F, cy) * Math.max(0.0F, cy)) - radius;
            int alpha = cx > 0.0F && cy > 0.0F ? Math.round(Math.max(0.0F, Math.min(1.0F, 0.5F - outside)) * 255.0F) : 255;
            out.setPixel(x, y, alpha << 24 | rgb & 16777215);
         }
      }

      return out;
   }
}
