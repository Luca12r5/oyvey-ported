package dev.spotifyhud;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.imageio.ImageIO;

final class ArtLoader {
   private ArtLoader() {
   }

   static Object load(String url) {
      try {
         if (!url.startsWith("https://")) {
            return null;
         } else {
            HttpURLConnection c = (HttpURLConnection)URI.create(url).toURL().openConnection();
            c.setConnectTimeout(6000);
            c.setReadTimeout(10000);

            byte[] data;
            try (InputStream in = c.getInputStream()) {
               ByteArrayOutputStream o = new ByteArrayOutputStream();
               byte[] buf = new byte[16384];

               int n;
               while ((n = in.read(buf)) > 0) {
                  o.write(buf, 0, n);
                  if (o.size() > 8388608) {
                     return null;
                  }
               }

               data = o.toByteArray();
            } finally {
               c.disconnect();
            }

            return decode(data);
         }
      } catch (Throwable var16) {
         SpotifyHudMod.LOG("Cover konnte nicht geladen werden: " + var16);
         return null;
      }
   }

   static Object decode(byte[] data) {
      try {
         BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
         if (img == null) {
            return null;
         } else {
            BufferedImage argb = new BufferedImage(img.getWidth(), img.getHeight(), 2);
            Graphics g = argb.getGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
            return argb;
         }
      } catch (Throwable var4) {
         SpotifyHudMod.LOG("Cover konnte nicht dekodiert werden: " + var4);
         return null;
      }
   }
}
