package dev.lego.ui;

import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;

/** Gx backend that draws with Minecraft's DrawContext. */
public final class McBackend implements Gx.Backend {
    public static final McBackend INSTANCE = new McBackend();
    private class_332 ctx;
    private static int counter = 0;

    private McBackend() { }

    public static void begin(class_332 c) {
        INSTANCE.ctx = c;
        Gx.begin(INSTANCE);
    }

    @Override public int guiScale() {
        try { return class_310.method_1551().method_22683().method_4495(); } catch (Throwable t) { return 2; }
    }

    @Override public void push() { ctx.method_51448().pushMatrix(); }
    @Override public void pop() { ctx.method_51448().popMatrix(); }
    @Override public void translate(float x, float y) { ctx.method_51448().translate(x, y); }
    @Override public void scale(float x, float y) { ctx.method_51448().scale(x, y); }
    @Override public void fill(int x, int y, int w, int h, int argb) { ctx.method_25294(x, y, x + w, y + h, argb); }
    @Override public void gradV(int x, int y, int w, int h, int top, int bottom) { ctx.method_25296(x, y, x + w, y + h, top, bottom); }

    @Override
    public void tex(Gx.Img t, int x, int y, int w, int h, int u, int v, int rw, int rh, int argb) {
        ctx.method_25293(class_10799.field_56883, (class_2960) t.handle, x, y, u, v, w, h, rw, rh, t.w, t.h, argb);
    }

    /** Draws any registered texture (e.g. item atlas or our own dynamic textures). */
    public void texture(class_2960 id, int x, int y, int w, int h, int texW, int texH, int argb) {
        ctx.method_25293(class_10799.field_56883, id, x, y, 0, 0, w, h, texW, texH, texW, texH, argb);
    }

    @Override public void clip(int x, int y, int w, int h) { ctx.method_44379(x, y, x + w, y + h); }
    @Override public void unclip() { ctx.method_44380(); }

    @Override
    public Object register(int[] argb, int w, int h) {
        class_2960 id = class_2960.method_60655("legoclient", "ui/" + (counter++));
        TEX.put(id, Tx.register(id, argb, w, h, true));
        return id;
    }

    private static final java.util.Map<class_2960, net.minecraft.class_1043> TEX = new java.util.HashMap<>();

    @Override
    public void update(Gx.Img t, int[] argb) {
        net.minecraft.class_1043 tex = TEX.get((class_2960) t.handle);
        if (tex != null) Tx.update(tex, argb, t.w, t.h);
    }

    @Override public void destroy(Gx.Img t) { TEX.remove((class_2960) t.handle); Tx.destroy((class_2960) t.handle); }

    @Override
    public int[] screenPx() {
        try {
            var w = class_310.method_1551().method_22683();
            return new int[]{w.method_4489(), w.method_4506()};
        } catch (Throwable t) {
            return new int[]{1920, 1080};
        }
    }

    public class_332 ctx() { return ctx; }
}
