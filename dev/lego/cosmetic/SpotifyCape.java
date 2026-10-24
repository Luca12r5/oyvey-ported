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
      int[] px = pending;
      if (px != null) {
         pending = null;

         try {
            if (tex == null) {
               tex = Tx.register(ID, px, pw, ph, true);
            } else {
               Tx.update(tex, px, pw, ph);
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
      long last = System.nanoTime();

      while (System.currentTimeMillis() - wanted < 5000L) {
         long t0 = System.nanoTime();
         float dt = Math.min(0.2F, (float)(t0 - last) / 1.0E9F);
         last = t0;

         try {
            step(dt);
            if (pending == null) {
               LIVE.set(state);

               CTex.T t;
               try {
                  t = Capes.paint("spotify");
               } finally {
                  LIVE.remove();
               }

               pw = t.w;
               ph = t.h;
               pending = t.argb;
            }
         } catch (Throwable var15) {
            LegoClient.LOG("Spotify-Cape: " + var15);

            try {
               Thread.sleep(2000L);
            } catch (InterruptedException var13) {
               return;
            }
         }

         long took = (System.nanoTime() - t0) / 1000000L;

         try {
            Thread.sleep(Math.max(15L, 66L - took));
         } catch (InterruptedException var12) {
            return;
         }
      }
   }

   private static PlayerState player() {
      try {
         PlayerState st = null;
         if (SpotifyHudMod.api() != null) {
            st = SpotifyHudMod.api().state();
         }

         if ((st == null || !st.hasTrack) && SpotifyHudMod.media() != null) {
            st = SpotifyHudMod.media().state();
         }

         return st;
      } catch (Throwable var1) {
         return null;
      }
   }

   private static void step(float dt) {
      PlayerState st = player();
      SpotifyCape.Live L = state;
      boolean has = st != null && st.hasTrack;
      L.playing = has && st.playing;
      L.title = has ? st.title : "Kein Song";
      L.artist = has ? st.artist : "Spotify öffnen";
      L.progress = has && st.durationMs > 0L ? (double)st.currentProgress() / st.durationMs : 0.0;
      double sec = has ? st.currentProgress() / 1000.0 : System.currentTimeMillis() / 1000.0;
      String key = has ? (st.trackId != null ? st.trackId : st.title + st.artist) : "";
      int h = key.hashCode();
      double bpm = 86 + Math.floorMod(h, 60);
      double bt = sec * bpm / 60.0;
      double kick = Math.pow(Math.max(0.0, Math.cos(bt * Math.PI * 2.0)), 6.0);
      double snare = Math.pow(Math.max(0.0, Math.cos((bt + 0.5) * Math.PI * 2.0)), 10.0) * (Math.floorMod((int)Math.floor(bt), 2) == 1 ? 1.0 : 0.4);
      double section = 0.75 + 0.25 * Math.sin(sec / 17.0 + h);
      L.beat = L.playing ? kick : 0.0;

      for (int i = 0; i < 11; i++) {
         double f = i / 10.0;
         double target;
         if (L.playing) {
            double low = kick * (1.0 - f) * 1.1;
            double mid = snare * Math.exp(-Math.pow((f - 0.5) / 0.25, 2.0));
            double hi = (0.5 + 0.5 * Math.sin(sec * (9.0 + i * 1.7) + i * 2.3)) * 0.35 * f;
            double noise = 0.5 + 0.5 * Math.sin(sec * (3.1 + i * 0.73) + h * 0.001 + i);
            target = Math.min(1.0, (0.18 + low * 0.7 + mid * 0.6 + hi + noise * 0.25) * section);
         } else {
            target = 0.04;
         }

         double cur = L.bars[i];
         if (target > cur) {
            cur += (target - cur) * Math.min(1.0F, dt * 22.0F);
         } else {
            vel[i] = vel[i] + dt * 2.6;
            cur = Math.max(target, cur - vel[i] * dt);
         }

         if (target > cur - 0.001) {
            vel[i] = 0.0;
         }

         L.bars[i] = cur;
         L.peaks[i] = Math.max(cur, L.peaks[i] - dt * 0.35);
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
