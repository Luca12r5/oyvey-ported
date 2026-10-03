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

   static Object load(String var0) {
      try {
         if (!var0.startsWith("https://")) {
            return null;
         } else {
            HttpURLConnection var1 = (HttpURLConnection)URI.create(var0).toURL().openConnection();
            var1.setConnectTimeout(6000);
            var1.setReadTimeout(10000);

            byte[] var2;
            try (InputStream var3 = var1.getInputStream()) {
               ByteArrayOutputStream var4 = new ByteArrayOutputStream();
               byte[] var5 = new byte[16384];

               int var6;
               while ((var6 = var3.read(var5)) > 0) {
                  var4.write(var5, 0, var6);
                  if (var4.size() > 8388608) {
                     return null;
                  }
               }

               var2 = var4.toByteArray();
            } finally {
               var1.disconnect();
            }

            return decode(var2);
         }
      } catch (Throwable var16) {
         SpotifyHudMod.LOG("Cover konnte nicht geladen werden: " + var16);
         return null;
      }
   }

   static Object decode(byte[] var0) {
      try {
         BufferedImage var1 = ImageIO.read(new ByteArrayInputStream(var0));
         if (var1 == null) {
            return null;
         } else {
            BufferedImage var2 = new BufferedImage(var1.getWidth(), var1.getHeight(), 2);
            Graphics var3 = var2.getGraphics();
            var3.drawImage(var1, 0, 0, null);
            var3.dispose();
            return var2;
         }
      } catch (Throwable var4) {
         SpotifyHudMod.LOG("Cover konnte nicht dekodiert werden: " + var4);
         return null;
      }
   }
}
