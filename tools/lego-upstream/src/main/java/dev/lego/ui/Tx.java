package dev.lego.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.IntBuffer;

import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_1060;
import net.minecraft.class_2960;
import net.minecraft.class_310;

/** Texture helpers (render thread): register ARGB pixel arrays as textures, switch textures to linear filtering. */
public final class Tx {
    private Tx() { }

    private static boolean fast = true;
    private static boolean linearBroken = false;
    private static Object linearSampler;
    private static Field samplerField;

    public static class_1060 tm() { return class_310.method_1551().method_1531(); }

    /** Creates (or replaces) a texture from ARGB pixels. */
    public static class_1043 register(class_2960 id, int[] argb, int w, int h, boolean linear) {
        class_1011 img = new class_1011(w, h, true);
        fill(img, argb, w, h);
        class_1043 tex = new class_1043(() -> "legoclient/" + id.method_12832(), img);
        tm().method_4616(id, tex);
        tex.method_4524();
        if (linear) linear(tex);
        return tex;
    }

    /** Replaces the pixels of an existing texture of the same size. */
    public static void update(class_1043 tex, int[] argb, int w, int h) {
        fill(tex.method_4525(), argb, w, h);
        tex.method_4524();
    }

    public static void destroy(class_2960 id) {
        try { tm().method_4615(id); } catch (Throwable ignored) { }
    }

    private static void fill(class_1011 img, int[] argb, int w, int h) {
        int n = w * h;
        if (fast) {
            try {
                long ptr = img.method_67769();
                if (ptr != 0L) {
                    IntBuffer buf = org.lwjgl.system.MemoryUtil.memIntBuffer(ptr, n);
                    int[] row = new int[w];
                    for (int y = 0; y < h; y++) {
                        int o = y * w;
                        for (int x = 0; x < w; x++) {
                            int c = argb[o + x];
                            row[x] = (c & 0xFF00FF00) | ((c & 0xFF) << 16) | ((c >> 16) & 0xFF);
                        }
                        buf.put(row, 0, w);
                    }
                    return;
                }
            } catch (Throwable t) {
                fast = false;
            }
        }
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) img.method_61941(x, y, argb[y * w + x]);
    }

    /** Switches a texture to bilinear filtering (smooth when scaled). Silently does nothing if unsupported. */
    public static void linear(class_1044 tex) {
        if (linearBroken || tex == null) return;
        try {
            if (linearSampler == null) {
                Class<?> rs = Class.forName("com.mojang.blaze3d.systems.RenderSystem");
                Object cache = null;
                for (Method m : rs.getMethods()) {
                    if (m.getParameterCount() == 0 && java.lang.reflect.Modifier.isStatic(m.getModifiers())
                            && m.getReturnType().getName().equals("net.minecraft.class_12136")) {
                        cache = m.invoke(null);
                        break;
                    }
                }
                if (cache == null) throw new IllegalStateException("no sampler cache");
                Class<?> fm = Class.forName("com.mojang.blaze3d.textures.FilterMode");
                Object lin = null;
                for (Object c : fm.getEnumConstants()) if (((Enum<?>) c).name().equals("LINEAR")) lin = c;
                Method get = cache.getClass().getMethod("method_75294", fm);
                linearSampler = get.invoke(cache, lin);
                samplerField = class_1044.class.getDeclaredField("field_63613");
                samplerField.setAccessible(true);
            }
            samplerField.set(tex, linearSampler);
        } catch (Throwable t) {
            linearBroken = true;
            dev.lego.LegoClient.LOG("Linearer Filter nicht verfügbar: " + t);
        }
    }

    public static void linear(class_2960 id) {
        try { linear(tm().method_4619(id)); } catch (Throwable ignored) { }
    }
}
