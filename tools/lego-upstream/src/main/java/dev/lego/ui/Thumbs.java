package dev.lego.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/** Asynchronously painted images (e.g. 3D cosmetic thumbnails) - never blocks the render thread. */
public final class Thumbs {
    private Thumbs() { }

    private static final ExecutorService POOL = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "LegoClient-Thumbs");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        return t;
    });
    private static final Map<String, int[]> READY = new HashMap<>();
    private static final Set<String> PENDING = new HashSet<>();

    /** @return the image, or null while it is still being painted */
    public static Gx.Img get(String key, int w, int h, Supplier<int[]> painter) {
        String k = key + "@" + w + "x" + h;
        int[] px;
        synchronized (READY) { px = READY.remove(k); }
        if (px != null) {
            final int[] fpx = px;
            return Gx.get("th:" + k, 0, () -> new int[][]{fpx, {w, h}});
        }
        Gx.Img cached = Gx.peek("th:" + k);
        if (cached != null) return cached;
        synchronized (READY) {
            if (PENDING.contains(k)) return null;
            PENDING.add(k);
        }
        POOL.execute(() -> {
            try {
                int[] r = painter.get();
                synchronized (READY) { READY.put(k, r); }
            } catch (Throwable t) {
                dev.lego.LegoClient.LOG("Vorschau-Fehler: " + t);
            } finally {
                synchronized (READY) { PENDING.remove(k); }
            }
        });
        return null;
    }
}
