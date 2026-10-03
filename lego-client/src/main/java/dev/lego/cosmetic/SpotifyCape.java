package dev.lego.cosmetic;

import dev.lego.LegoClient;
import dev.lego.ui.Tx;
import dev.spotifyhud.PlayerState;
import dev.spotifyhud.SpotifyHudMod;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class SpotifyCape {
   static final ThreadLocal<SpotifyCape.Live> LIVE = new ThreadLocal<>();
   private static Identifier ID;
   private static DynamicTexture tex;
   private static volatile int[] pending;
   private static volatile int pw;
   private static volatile int ph;
   private static volatile long wanted;
   private static Thread painter;
   private static final SpotifyCape.Live state = new SpotifyCape.Live();
   private static final double[] vel = new double[11];

   private SpotifyCape() {
   }

   static Identifier texture() {
      wanted = System.currentTimeMillis();
      if (ID == null) {
         ID = Identifier.fromNamespaceAndPath("legoclient", "cosmetic/cape_spotify_live");
      }

      ensurePainter();
      int[] var0 = pending;
      if (var0 != null) {
         pending = null;

         try {
            if (tex == null) {
               tex = Tx.register(ID, var0, pw, ph, true);
            } else {
               Tx.update(tex, var0, pw, ph);
            }
         } catch (Throwable var2) {
            LegoClient.LOG("Spotify-Cape: " + var2);
         }
      }

      return tex == null ? null : ID;
   }

   private static synchronized void ensurePainter() {
      if (painter == null || !painter.isAlive()) {
         painter = new Thread(SpotifyCape::loop, "Lego Spotify-Cape");
         painter.setDaemon(true);
         painter.setPriority(1);
         painter.start();
      }
   }

   private static void loop() {
      long var0 = System.nanoTime();

      while (System.currentTimeMillis() - wanted < 5000L) {
         long var2 = System.nanoTime();
         float var4 = Math.min(0.2F, (float)(var2 - var0) / 1.0E9F);
         var0 = var2;

         try {
            step(var4);
            if (pending == null) {
               LIVE.set(state);

               CTex.T var5;
               try {
                  var5 = Capes.paint("spotify");
               } finally {
                  LIVE.remove();
               }

               pw = var5.w;
               ph = var5.h;
               pending = var5.argb;
            }
         } catch (Throwable var15) {
            LegoClient.LOG("Spotify-Cape: " + var15);

            try {
               Thread.sleep(2000L);
            } catch (InterruptedException var13) {
               return;
            }
         }

         long var16 = (System.nanoTime() - var2) / 1000000L;

         try {
            Thread.sleep(Math.max(15L, 66L - var16));
         } catch (InterruptedException var12) {
            return;
         }
      }
   }

   private static PlayerState player() {
      try {
         PlayerState var0 = null;
         if (SpotifyHudMod.api() != null) {
            var0 = SpotifyHudMod.api().state();
         }

         if ((var0 == null || !var0.hasTrack) && SpotifyHudMod.media() != null) {
            var0 = SpotifyHudMod.media().state();
         }

         return var0;
      } catch (Throwable var1) {
         return null;
      }
   }

   private static void step(float var0) {
      PlayerState var1 = player();
      SpotifyCape.Live var2 = state;
      boolean var3 = var1 != null && var1.hasTrack;
      var2.playing = var3 && var1.playing;
      var2.title = var3 ? var1.title : "Kein Song";
      var2.artist = var3 ? var1.artist : "Spotify öffnen";
      var2.progress = var3 && var1.durationMs > 0L ? (double)var1.currentProgress() / var1.durationMs : 0.0;
      double var4 = var3 ? var1.currentProgress() / 1000.0 : System.currentTimeMillis() / 1000.0;
      String var6 = var3 ? (var1.trackId != null ? var1.trackId : var1.title + var1.artist) : "";
      int var7 = var6.hashCode();
      double var8 = 86 + Math.floorMod(var7, 60);
      double var10 = var4 * var8 / 60.0;
      double var12 = Math.pow(Math.max(0.0, Math.cos(var10 * Math.PI * 2.0)), 6.0);
      double var14 = Math.pow(Math.max(0.0, Math.cos((var10 + 0.5) * Math.PI * 2.0)), 10.0) * (Math.floorMod((int)Math.floor(var10), 2) == 1 ? 1.0 : 0.4);
      double var16 = 0.75 + 0.25 * Math.sin(var4 / 17.0 + var7);
      var2.beat = var2.playing ? var12 : 0.0;

      for (int var18 = 0; var18 < 11; var18++) {
         double var19 = var18 / 10.0;
         double var21;
         if (var2.playing) {
            double var23 = var12 * (1.0 - var19) * 1.1;
            double var25 = var14 * Math.exp(-Math.pow((var19 - 0.5) / 0.25, 2.0));
            double var27 = (0.5 + 0.5 * Math.sin(var4 * (9.0 + var18 * 1.7) + var18 * 2.3)) * 0.35 * var19;
            double var29 = 0.5 + 0.5 * Math.sin(var4 * (3.1 + var18 * 0.73) + var7 * 0.001 + var18);
            var21 = Math.min(1.0, (0.18 + var23 * 0.7 + var25 * 0.6 + var27 + var29 * 0.25) * var16);
         } else {
            var21 = 0.04;
         }

         double var31 = var2.bars[var18];
         if (var21 > var31) {
            var31 += (var21 - var31) * Math.min(1.0F, var0 * 22.0F);
         } else {
            vel[var18] = vel[var18] + var0 * 2.6;
            var31 = Math.max(var21, var31 - vel[var18] * var0);
         }

         if (var21 > var31 - 0.001) {
            vel[var18] = 0.0;
         }

         var2.bars[var18] = var31;
         var2.peaks[var18] = Math.max(var31, var2.peaks[var18] - var0 * 0.35);
      }
   }

   static final class Live {
      final double[] bars = new double[11];
      final double[] peaks = new double[11];
      String title = "";
      String artist = "";
      double progress;
      double beat;
      boolean playing;
   }
}
