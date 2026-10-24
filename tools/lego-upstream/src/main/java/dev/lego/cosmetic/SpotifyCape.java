package dev.lego.cosmetic;

import dev.lego.LegoClient;
import dev.lego.ui.Tx;
import net.minecraft.class_1043;
import net.minecraft.class_2960;

/**
 * The Spotify cape linked to the song that is playing: equalizer bars move with the song's beat
 * (tempo derived per track, synced to the playback position), title / artist / progress on the cape,
 * bars fall down when paused. Repainted on a background thread (~15 fps), uploaded on the render thread.
 */
public final class SpotifyCape {
    private SpotifyCape() { }

    static final class Live {
        final double[] bars = new double[11], peaks = new double[11];
        String title = "", artist = "";
        double progress, beat;
        boolean playing;
    }

    /** Set only on the painter thread while it paints the live frame. */
    static final ThreadLocal<Live> LIVE = new ThreadLocal<>();

    private static class_2960 ID;
    private static class_1043 tex;
    private static volatile int[] pending;
    private static volatile int pw, ph;
    private static volatile long wanted;          // last time the render thread asked for the texture
    private static Thread painter;
    private static final Live state = new Live();
    private static final double[] vel = new double[11];

    /** Live texture, or null (then the normal animated frames are used). Render thread. */
    static class_2960 texture() {
        wanted = System.currentTimeMillis();
        if (ID == null) ID = class_2960.method_60655("legoclient", "cosmetic/cape_spotify_live");
        ensurePainter();
        int[] px = pending;
        if (px != null) {
            pending = null;
            try {
                if (tex == null) tex = Tx.register(ID, px, pw, ph, true);
                else Tx.update(tex, px, pw, ph);
            } catch (Throwable t) { LegoClient.LOG("Spotify-Cape: " + t); }
        }
        return tex == null ? null : ID;
    }

    private static synchronized void ensurePainter() {
        if (painter != null && painter.isAlive()) return;
        painter = new Thread(SpotifyCape::loop, "Lego Spotify-Cape");
        painter.setDaemon(true);
        painter.setPriority(Thread.MIN_PRIORITY);
        painter.start();
    }

    private static void loop() {
        long last = System.nanoTime();
        while (System.currentTimeMillis() - wanted < 5000) {
            long t0 = System.nanoTime();
            float dt = Math.min(0.2f, (t0 - last) / 1e9f);
            last = t0;
            try {
                step(dt);
                if (pending == null) {
                    LIVE.set(state);
                    CTex.T t;
                    try { t = Capes.paint("spotify"); } finally { LIVE.remove(); }
                    pw = t.w; ph = t.h;
                    pending = t.argb;
                }
            } catch (Throwable e) {
                LegoClient.LOG("Spotify-Cape: " + e);
                try { Thread.sleep(2000); } catch (InterruptedException ie) { return; }
            }
            long took = (System.nanoTime() - t0) / 1_000_000;
            try { Thread.sleep(Math.max(15, 66 - took)); } catch (InterruptedException ie) { return; }
        }
    }

    private static dev.spotifyhud.PlayerState player() {
        try {
            dev.spotifyhud.PlayerState st = null;
            if (dev.spotifyhud.SpotifyHudMod.api() != null) st = dev.spotifyhud.SpotifyHudMod.api().state();
            if ((st == null || !st.hasTrack) && dev.spotifyhud.SpotifyHudMod.media() != null) st = dev.spotifyhud.SpotifyHudMod.media().state();
            return st;
        } catch (Throwable t) { return null; }
    }

    private static void step(float dt) {
        dev.spotifyhud.PlayerState st = player();
        Live L = state;
        boolean has = st != null && st.hasTrack;
        L.playing = has && st.playing;
        L.title = has ? st.title : "Kein Song";
        L.artist = has ? st.artist : "Spotify öffnen";
        L.progress = has && st.durationMs > 0 ? st.currentProgress() / (double) st.durationMs : 0;
        double sec = has ? st.currentProgress() / 1000.0 : System.currentTimeMillis() / 1000.0;
        String key = has ? (st.trackId != null ? st.trackId : st.title + st.artist) : "";
        int h = key.hashCode();
        double bpm = 86 + Math.floorMod(h, 60);                  // stable per song
        double bt = sec * bpm / 60.0;
        double kick = Math.pow(Math.max(0, Math.cos(bt * Math.PI * 2)), 6);
        double snare = Math.pow(Math.max(0, Math.cos((bt + 0.5) * Math.PI * 2)), 10) * (Math.floorMod((int) Math.floor(bt), 2) == 1 ? 1 : 0.4);
        double section = 0.75 + 0.25 * Math.sin(sec / 17.0 + h);    // song "parts" get louder / quieter
        L.beat = L.playing ? kick : 0;
        for (int i = 0; i < 11; i++) {
            double f = i / 10.0;
            double target;
            if (L.playing) {
                double low = kick * (1 - f) * 1.1, mid = snare * Math.exp(-Math.pow((f - 0.5) / 0.25, 2));
                double hi = (0.5 + 0.5 * Math.sin(sec * (9 + i * 1.7) + i * 2.3)) * 0.35 * f;
                double noise = 0.5 + 0.5 * Math.sin(sec * (3.1 + i * 0.73) + h * 0.001 + i);
                target = Math.min(1, (0.18 + low * 0.7 + mid * 0.6 + hi + noise * 0.25) * section);
            } else target = 0.04;
            // spring towards the target: fast attack, slow release (like a real analyser)
            double cur = L.bars[i];
            if (target > cur) cur += (target - cur) * Math.min(1, dt * 22);
            else { vel[i] += dt * 2.6; cur = Math.max(target, cur - vel[i] * dt); }
            if (target > cur - 0.001) vel[i] = 0;
            L.bars[i] = cur;
            L.peaks[i] = Math.max(cur, L.peaks[i] - dt * 0.35);
        }
    }
}
