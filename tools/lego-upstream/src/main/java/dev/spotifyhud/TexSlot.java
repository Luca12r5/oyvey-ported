package dev.spotifyhud;

import java.nio.IntBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1060;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;

/**
 * A dynamic texture that is drawn 1 texel = 1 screen pixel.
 *
 * Performance (no FPS drops):
 *  - the Java2D painting runs on a background thread, never on the render thread;
 *  - the finished pixels are copied into the texture with one bulk memory copy (instead of one call per pixel);
 *  - the texture is only re-uploaded when its content key changed.
 */
public final class TexSlot {
    /** One shared painter thread: all Java2D painters use shared caches and must never run in parallel. */
    private static final ExecutorService PAINTER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "LegoClient-Painter");
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY - 1);
        return t;
    });
    private static volatile boolean fastUpload = true;

    final class_2960 id;
    private class_1043 tex;
    private int w, h;
    private String shownKey = null;

    // hand-over from the painter thread
    private volatile boolean busy;
    private volatile int[] ready;       // ABGR pixels
    private volatile String readyKey;
    private volatile int readyW, readyH;
    private String requestedKey;

    public TexSlot(class_2960 id) { this.id = id; }

    /**
     * @param k       content key; the painter only runs when it changes
     * @param painter produces ARGB pixels (pw * ph), runs on the painter thread
     */
    public void draw(class_332 ctx, class_310 mc, int x, int y, int gw, int gh, int pw, int ph, String k, Supplier<int[]> painter) {
        if (pw <= 0 || ph <= 0) return;
        String fullKey = pw + "x" + ph + "|" + k;

        // 1) take over a finished frame
        int[] px = ready;
        if (px != null && readyW == pw && readyH == ph) {
            ready = null;
            upload(mc, px, pw, ph);
            shownKey = readyKey;
        } else if (px != null) {
            ready = null; // stale size
        }

        // 2) request a new frame if the content changed
        if (!fullKey.equals(shownKey) && !busy && !fullKey.equals(requestedKey)) {
            busy = true;
            requestedKey = fullKey;
            final String key = fullKey;
            PAINTER.execute(() -> {
                try {
                    int[] argb = painter.get();
                    if (argb != null && argb.length == pw * ph) {
                        toAbgr(argb);
                        readyW = pw;
                        readyH = ph;
                        readyKey = key;
                        ready = argb;
                    }
                } catch (Throwable t) {
                    SpotifyHudMod.LOG("Painter-Fehler: " + t);
                } finally {
                    busy = false;
                }
            });
        } else if (fullKey.equals(requestedKey) && !busy && ready == null && !fullKey.equals(shownKey)) {
            requestedKey = null; // painter failed -> allow a retry next frame
        }

        // 3) draw what we have (while a resized frame is being painted, the old one is shown stretched)
        if (tex != null && shownKey != null) {
            ctx.method_25302(class_10799.field_56883, id, x, y, 0f, 0f, gw, gh, w, h, w, h);
        }
    }

    private void upload(class_310 mc, int[] abgr, int pw, int ph) {
        class_1060 tm = mc.method_1531();
        if (tex == null || w != pw || h != ph) {
            if (tex != null) tm.method_4615(id);
            class_1011 img = new class_1011(pw, ph, true);
            tex = new class_1043(() -> "legoclient_" + id.hashCode(), img);
            tm.method_4616(id, tex);
            w = pw;
            h = ph;
        }
        class_1011 img = tex.method_4525();
        boolean done = false;
        if (fastUpload) {
            try {
                long ptr = img.method_67769();
                if (ptr != 0L) {
                    IntBuffer buf = org.lwjgl.system.MemoryUtil.memIntBuffer(ptr, pw * ph);
                    buf.put(abgr, 0, pw * ph);
                    done = true;
                }
            } catch (Throwable t) {
                fastUpload = false;
                SpotifyHudMod.LOG("Schneller Upload nicht verfügbar, nutze Fallback: " + t);
            }
        }
        if (!done) {
            for (int yy = 0; yy < ph; yy++) {
                int row = yy * pw;
                for (int xx = 0; xx < pw; xx++) {
                    int c = abgr[row + xx];
                    // back to ARGB for setColorArgb
                    img.method_61941(xx, yy, (c & 0xFF00FF00) | ((c & 0xFF) << 16) | ((c >> 16) & 0xFF));
                }
            }
        }
        tex.method_4524();
    }

    /** ARGB -> ABGR (NativeImage RGBA memory layout, little endian). */
    private static void toAbgr(int[] px) {
        for (int i = 0; i < px.length; i++) {
            int c = px[i];
            px[i] = (c & 0xFF00FF00) | ((c & 0xFF) << 16) | ((c >> 16) & 0xFF);
        }
    }

    /** Clears everything (e.g. on resource reload). */
    public void invalidate() {
        shownKey = null;
        requestedKey = null;
    }
}
