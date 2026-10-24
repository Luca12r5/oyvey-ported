package dev.lego.ui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import dev.lego.LegoClient;
import dev.lego.core.Modules;
import net.minecraft.class_2960;

/** User background image for the click GUI. */
public final class BgImage {
    private BgImage() { }

    private static class_2960 texId;
    private static Gx.Img cachedImg;
    private static String loadedPath = "";
    private static int imgW, imgH;

    public static void pick() {
        new Thread(() -> {
            try (org.lwjgl.system.MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
                org.lwjgl.PointerBuffer filters = stack.mallocPointer(4);
                filters.put(stack.UTF8("*.png"));
                filters.put(stack.UTF8("*.jpg"));
                filters.put(stack.UTF8("*.jpeg"));
                filters.put(stack.UTF8("*.bmp"));
                filters.flip();
                String result = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog(
                        "Hintergrundbild wählen", "", filters,
                        "Bilder (png, jpg, bmp)", false);
                if (result != null && !result.isEmpty()) {
                    UiSettings.bgImage = result;
                    Modules.scheduleSave();
                    loadedPath = "";
                }
            } catch (Throwable t) {
                LegoClient.LOG("Hintergrundbild-Dialog: " + t);
            }
        }, "bg-pick").start();
    }

    public static void clear() {
        loadedPath = "";
        if (texId != null) { Tx.destroy(texId); texId = null; cachedImg = null; }
    }

    private static void load(String path) {
        if (path.equals(loadedPath)) return;
        loadedPath = path;
        if (texId != null) { Tx.destroy(texId); texId = null; }
        if (path.isEmpty()) return;
        new Thread(() -> {
            try {
                BufferedImage raw = ImageIO.read(new File(path));
                if (raw == null) return;
                int w = raw.getWidth(), h = raw.getHeight();
                if (w > 2048 || h > 2048) {
                    double s = Math.min(2048.0 / w, 2048.0 / h);
                    w = (int) (w * s); h = (int) (h * s);
                }
                BufferedImage argb = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = argb.createGraphics();
                g.drawImage(raw, 0, 0, w, h, null);
                g.dispose();
                int[] px = argb.getRGB(0, 0, w, h, null, 0, w);
                imgW = w; imgH = h;
                class_2960 id = class_2960.method_60655("legoclient", "bg_user");
                int fw = w, fh = h;
                net.minecraft.class_310.method_1551().execute(() -> {
                    try { Tx.register(id, px, fw, fh, true); texId = id; cachedImg = new Gx.Img(id, fw, fh, 0); } catch (Throwable t) { LegoClient.LOG("BG-Textur: " + t); }
                });
            } catch (Throwable t) {
                LegoClient.LOG("Hintergrundbild laden: " + t);
            }
        }, "bg-load").start();
    }

    public static void render(int W, int H) {
        String path = UiSettings.bgImage;
        if (path.isEmpty()) return;
        load(path);
        if (texId == null || cachedImg == null) return;
        Gx.Img img = cachedImg;
        float ar = (float) imgW / imgH, sar = (float) W / H;
        int dw, dh;
        if (ar > sar) { dh = H; dw = Math.round(H * ar); }
        else { dw = W; dh = Math.round(W / ar); }
        Gx.image(img, (W - dw) / 2, (H - dh) / 2, dw, dh, 0xFFFFFFFF);
    }
}
