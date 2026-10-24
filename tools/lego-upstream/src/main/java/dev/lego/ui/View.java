package dev.lego.ui;

/**
 * A full-screen UI drawn with {@link Gx} in physical pixels. Pure logic (no Minecraft classes), hosted by
 * {@link LegoScreen} in game and by the offline preview renderer during development.
 */
public abstract class View {
    /** Hosting screen: navigation + closing. */
    public interface Host {
        void close();
        void closeThen(Runnable after);
        void open(View next);
        boolean closing();
    }

    public Host host;
    /** Screen size + mouse in physical pixels; open progress 0..1 (runs back to 0 while closing). */
    public int W, H;
    public float mx = -1, my = -1, open = 0;
    /** Physical pixels per design unit. */
    public float P = 2;

    public float openMs() { return 300; }
    public float closeMs() { return 180; }
    public boolean blur() { return UiSettings.blur; }
    /** Winter backdrop (snow, aurora) behind the view. */
    public boolean backdrop() { return true; }
    /** Design size used to compute P: the whole UI scales with the window. */
    public float designW() { return 700; }
    public float designH() { return 430; }

    public final void layout(int w, int h) {
        W = w;
        H = h;
        P = Math.max(0.75f, Math.min(W / (designW() + 40), H / (designH() + 40)) * (float) UiSettings.uiScale);
    }

    public int p(double u) { return (int) Math.round(u * P); }
    public float pf(double u) { return (float) (u * P); }

    public abstract void draw();

    public boolean mouseDown(float x, float y, int button, boolean dbl) { return Kit.down(x, y, button); }
    public boolean mouseUp(float x, float y, int button) { return Kit.up(); }
    public boolean mouseDrag(float x, float y, int button) { return Kit.drag(x, y); }
    public boolean scroll(float x, float y, double amount) { return Kit.scroll(x, y, amount); }
    public boolean key(int key, int mods) { return false; }
    public boolean chr(String s) { return false; }
    public void onEscape() { if (host != null) host.close(); }
    public void tick() { }
    public void removed() { }

    public boolean hover(int rx, int ry, int rw, int rh) {
        return (host == null || !host.closing()) && mx >= rx && my >= ry && mx < rx + rw && my < ry + rh;
    }

    public static boolean in(float x, float y, int rx, int ry, int rw, int rh) { return x >= rx && y >= ry && x < rx + rw && y < ry + rh; }
}
