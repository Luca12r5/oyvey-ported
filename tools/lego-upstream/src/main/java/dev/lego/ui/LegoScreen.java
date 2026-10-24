package dev.lego.ui;

import dev.lego.LegoClient;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1041;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;

/** Minecraft screen hosting a {@link View}: open/close animation, pixel-exact input, blur background. */
public class LegoScreen extends class_437 implements View.Host {
    private final View view;
    private final class_437 parent;
    private long openedAt = 0, closeAt = 0;
    private boolean closing = false;
    private float openAtClose = 1;
    private Runnable afterClose;

    public LegoScreen(View view, class_437 parent) {
        super(class_2561.method_43470("Lego Client"));
        this.view = view;
        this.parent = parent;
        view.host = this;
    }

    public static void show(View v) { class_310.method_1551().method_1507(new LegoScreen(v, null)); }

    public View view() { return view; }

    @Override public boolean method_25421() { return false; }       // shouldPause
    @Override public boolean method_25422() { return false; }       // vanilla ESC handling off (we animate)

    @Override
    public void method_25420(class_332 ctx, int mouseX, int mouseY, float delta) {    // renderBackground
        if (!view.blur()) return;
        try { super.method_25420(ctx, mouseX, mouseY, delta); } catch (Throwable ignored) { }
    }

    @Override
    public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {       // render
        long now = System.currentTimeMillis();
        if (openedAt == 0) openedAt = now;
        if (closing) {
            float t = (now - closeAt) / (view.closeMs() / UiSettings.anim());
            view.open = Math.max(0, 1 - t) * openAtClose;
            if (t >= 1) { finish(); return; }
        } else {
            view.open = Math.min(1, (now - openedAt) / (view.openMs() / UiSettings.anim()));
        }
        McBackend.begin(ctx);
        dev.lego.perf.Perf.begin("Menü");
        try {
            Style.update();
            Kit.frame();
            view.layout(this.field_22789 * Gx.S, this.field_22790 * Gx.S);
            updateMouse(mouseX, mouseY);
            if (view.backdrop()) {
                if (Halloween.on()) Halloween.backdrop(view.W, view.H, Math.min(1, view.open * 1.4f));
                else Xmas.backdrop(view.W, view.H, Math.min(1, view.open * 1.4f));
            }
            BgImage.render(view.W, view.H);
            view.draw();
        } catch (Throwable t) {
            LegoClient.LOG("GUI-Fehler (" + view.getClass().getSimpleName() + "): " + t);
            if (LegoClient.DEBUG) t.printStackTrace();
        } finally {
            Gx.end();
            dev.lego.perf.Perf.end("Menü");
        }
    }

    private void updateMouse(int mouseX, int mouseY) {
        try {
            class_310 mc = class_310.method_1551();
            class_1041 w = mc.method_22683();
            double fx = (double) w.method_4489() / Math.max(1, w.method_4480());
            double fy = (double) w.method_4506() / Math.max(1, w.method_4507());
            view.mx = (float) (mc.field_1729.method_1603() * fx);
            view.my = (float) (mc.field_1729.method_1604() * fy);
        } catch (Throwable t) {
            view.mx = mouseX * Gx.S;
            view.my = mouseY * Gx.S;
        }
    }

    @Override public void method_25393() { try { view.tick(); } catch (Throwable t) { LegoClient.LOG("GUI-Tick: " + t); } }
    @Override public void method_25432() { try { view.removed(); } catch (Throwable ignored) { } }

    // ------------------------------------------------------------------ host
    @Override public void close() { closeThen(null); }

    @Override
    public void closeThen(Runnable after) {
        if (closing) return;
        closing = true;
        openAtClose = view.open;
        closeAt = System.currentTimeMillis();
        afterClose = after;
    }

    @Override public void open(View next) { class_310.method_1551().method_1507(new LegoScreen(next, this.parent)); }
    @Override public boolean closing() { return closing; }

    private void finish() {
        class_310 mc = class_310.method_1551();
        if (afterClose != null) {
            Runnable r = afterClose;
            afterClose = null;
            mc.method_1507(null);
            r.run();
        } else {
            mc.method_1507(parent);
        }
    }

    @Override public void method_25419() { close(); }    // close()

    // ------------------------------------------------------------------ input (converted to pixels)
    private float px(double v) { return (float) (v * Gx.S); }

    @Override
    public boolean method_25402(class_11909 click, boolean dbl) {
        if (closing) return true;
        try { view.mouseDown(px(click.comp_4798()), px(click.comp_4799()), click.method_74245(), dbl); }
        catch (Throwable t) { LegoClient.LOG("Klick: " + t); }
        return true;
    }

    @Override
    public boolean method_25406(class_11909 click) {
        try { view.mouseUp(px(click.comp_4798()), px(click.comp_4799()), click.method_74245()); } catch (Throwable t) { LegoClient.LOG("Klick: " + t); }
        return true;
    }

    @Override
    public boolean method_25403(class_11909 click, double dx, double dy) {
        try { view.mouseDrag(px(click.comp_4798()), px(click.comp_4799()), click.method_74245()); } catch (Throwable t) { LegoClient.LOG("Ziehen: " + t); }
        return true;
    }

    @Override
    public boolean method_25401(double x, double y, double h, double v) {
        try { view.scroll(px(x), px(y), v); } catch (Throwable t) { LegoClient.LOG("Scroll: " + t); }
        return true;
    }

    @Override
    public boolean method_25404(class_11908 key) {
        int k = key.comp_4795();
        if (closing) return true;
        try {
            if (view.key(k, key.comp_4797())) return true;
            if (k == 256) { view.onEscape(); return true; }
        } catch (Throwable t) { LegoClient.LOG("Taste: " + t); }
        return true;
    }

    @Override
    public boolean method_16803(class_11908 key) {
        try { return view.key(-1 - key.comp_4795(), key.comp_4797()); } catch (Throwable t) { return false; }
    }

    @Override
    public boolean method_25400(class_11905 in) {
        try { return view.chr(in.method_74226()); } catch (Throwable t) { return false; }
    }
}
