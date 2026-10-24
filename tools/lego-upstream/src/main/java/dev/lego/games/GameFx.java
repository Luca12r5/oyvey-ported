package dev.lego.games;

import java.util.Random;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;

/**
 * Shared "juice" for mini games: pooled particles, floating score popups, trauma based screen shake and
 * screen flashes. Everything is in board units; draw with {@link #draw(GameView, float, float)}.
 */
final class GameFx {
    static final int DOT = 0, SQUARE = 1, SPARK = 2, RING = 3, FLAKE = 4, STREAK = 5;
    private static final int MAX = 700, MAX_POP = 24;

    private final Random rnd = new Random();
    // particles
    private final float[] px = new float[MAX], py = new float[MAX], vx = new float[MAX], vy = new float[MAX],
            life = new float[MAX], max = new float[MAX], size = new float[MAX], grav = new float[MAX], drag = new float[MAX];
    private final int[] col = new int[MAX], kind = new int[MAX];
    private int n = 0;
    // popups
    private final String[] popText = new String[MAX_POP];
    private final float[] popX = new float[MAX_POP], popY = new float[MAX_POP], popT = new float[MAX_POP], popS = new float[MAX_POP];
    private final int[] popC = new int[MAX_POP];
    private int pn = 0;
    // shake / flash
    private float trauma = 0, shakeT = 0, flash = 0;
    private int flashCol = 0xFFFFFFFF;
    float sx, sy;

    void clear() { n = 0; pn = 0; trauma = 0; flash = 0; sx = sy = 0; }

    // ------------------------------------------------------------------ emit
    void add(float x, float y, float vx0, float vy0, float lifeS, float sz, int color, int k, float gravity, float dragK) {
        int i;
        if (n < MAX) i = n++;
        else i = rnd.nextInt(MAX);
        px[i] = x; py[i] = y; vx[i] = vx0; vy[i] = vy0; life[i] = 0; max[i] = lifeS; size[i] = sz; col[i] = color; kind[i] = k; grav[i] = gravity; drag[i] = dragK;
    }

    /** Radial burst. */
    void burst(float x, float y, int count, int color, float speed, float sz, float lifeS, float gravity, int k) {
        for (int i = 0; i < count; i++) {
            double a = rnd.nextDouble() * Math.PI * 2;
            float s = speed * (0.35f + rnd.nextFloat() * 0.65f);
            add(x, y, (float) Math.cos(a) * s, (float) Math.sin(a) * s, lifeS * (0.6f + rnd.nextFloat() * 0.4f), sz * (0.6f + rnd.nextFloat() * 0.6f), color, k, gravity, 2.2f);
        }
    }

    /** Burst with random colors from a palette. */
    void confetti(float x, float y, int count, int[] palette, float speed, float sz) {
        for (int i = 0; i < count; i++) {
            double a = -Math.PI / 2 + (rnd.nextDouble() - 0.5) * Math.PI * 1.4;
            float s = speed * (0.4f + rnd.nextFloat() * 0.6f);
            add(x, y, (float) Math.cos(a) * s, (float) Math.sin(a) * s, 0.9f + rnd.nextFloat() * 0.5f, sz * (0.6f + rnd.nextFloat() * 0.7f), palette[rnd.nextInt(palette.length)], SQUARE, 260, 1.6f);
        }
    }

    void ring(float x, float y, float radius, int color, float lifeS) { add(x, y, 0, 0, lifeS, radius, color, RING, 0, 0); }

    void popup(String text, float x, float y, int color, float sizeUnits) {
        int i = pn < MAX_POP ? pn++ : 0;
        popText[i] = text; popX[i] = x; popY[i] = y; popT[i] = 0; popC[i] = color; popS[i] = sizeUnits;
    }

    void shake(float amount) { trauma = Math.min(1, trauma + amount); }
    void flash(int color, float amount) { flashCol = color; flash = Math.max(flash, amount); }
    float trauma() { return trauma; }

    // ------------------------------------------------------------------ update
    void update(float dt) {
        for (int i = 0; i < n; i++) {
            life[i] += dt;
            if (life[i] >= max[i]) { n--; copy(n, i); i--; continue; }
            float d = Math.max(0, 1 - drag[i] * dt);
            vx[i] *= d; vy[i] *= d;
            vy[i] += grav[i] * dt;
            px[i] += vx[i] * dt; py[i] += vy[i] * dt;
        }
        for (int i = 0; i < pn; i++) {
            popT[i] += dt;
            if (popT[i] > 1.1f) { pn--; popText[i] = popText[pn]; popX[i] = popX[pn]; popY[i] = popY[pn]; popT[i] = popT[pn]; popC[i] = popC[pn]; popS[i] = popS[pn]; i--; }
        }
        trauma = Math.max(0, trauma - dt * 1.6f);
        flash = Math.max(0, flash - dt * 3f);
        shakeT += dt;
        float s = trauma * trauma * 7;
        sx = s * (float) (Math.sin(shakeT * 83.1) * 0.6 + Math.sin(shakeT * 51.7) * 0.4);
        sy = s * (float) (Math.cos(shakeT * 71.3) * 0.6 + Math.sin(shakeT * 43.9) * 0.4);
    }

    private void copy(int from, int to) {
        px[to] = px[from]; py[to] = py[from]; vx[to] = vx[from]; vy[to] = vy[from]; life[to] = life[from]; max[to] = max[from];
        size[to] = size[from]; col[to] = col[from]; kind[to] = kind[from]; grav[to] = grav[from]; drag[to] = drag[from];
    }

    // ------------------------------------------------------------------ draw
    /** Particles, offset by (-ox, -oy) (camera). */
    void draw(GameView g, float ox, float oy) {
        for (int i = 0; i < n; i++) {
            float t = life[i] / max[i], a = 1 - t;
            float x = px[i] - ox, y = py[i] - oy, s = size[i];
            int c = col[i];
            switch (kind[i]) {
                case SQUARE: g.box(x - s / 2, y - s / 2, s, s * 0.7f, s * 0.2f, Gx.withAlpha(c, Math.min(1, a * 1.6f))); break;
                case SPARK:
                    Gx.glow(g.X(x), g.Y(y), Math.max(1, g.S(s * 2.4f)), Gx.withAlpha(c, a * 0.5f));
                    g.circle(x, y, s * (1 - t * 0.6f), Gx.withAlpha(Gx.mix(c, 0xFFFFFFFF, 0.5f), a));
                    break;
                case RING: {
                    float r = s * Ease.outCubic(t);
                    Gx.ringAt(g.X(x), g.Y(y), Math.max(2, g.S(r)), Math.max(1, g.S(Math.max(0.6f, 2.2f * a))), Gx.withAlpha(c, a));
                    break;
                }
                case FLAKE: g.circle(x, y, s * (0.6f + 0.4f * a), Gx.withAlpha(c, a * 0.9f)); break;
                case STREAK: {
                    float l = Math.min(4, (Math.abs(vx[i]) + Math.abs(vy[i])) * 0.02f + 1);
                    float nx = vx[i], ny = vy[i], len = (float) Math.sqrt(nx * nx + ny * ny) + 0.001f;
                    for (int k = 0; k < 3; k++) g.circle(x - nx / len * k * s * l * 0.5f, y - ny / len * k * s * l * 0.5f, s * (1 - k * 0.25f), Gx.withAlpha(c, a * (1 - k * 0.3f)));
                    break;
                }
                default: g.circle(x, y, s * (0.5f + 0.5f * a), Gx.withAlpha(c, a)); break;
            }
        }
    }

    /** Floating popups (drawn above everything). */
    void drawPopups(GameView g, float ox, float oy) {
        for (int i = 0; i < pn; i++) {
            float t = popT[i] / 1.1f;
            float sc = t < 0.18f ? Ease.outBack(t / 0.18f) : 1;
            float a = t < 0.65f ? 1 : 1 - (t - 0.65f) / 0.35f;
            float x = popX[i] - ox, y = popY[i] - oy - Ease.outCubic(t) * 18;
            Gx.push();
            Gx.scaleAt(g.X(x), g.Y(y), Math.max(0.05f, sc));
            outlined(g, popText[i], x, y, popS[i], Gx.withAlpha(popC[i], a), Gx.withAlpha(0xFF10131C, a * 0.8f));
            Gx.pop();
        }
    }

    /** Screen flash over the whole board. */
    void drawFlash(GameView g, float w, float h) {
        if (flash > 0.01f) g.box(0, 0, w, h, 0, Gx.withAlpha(flashCol, flash * 0.45f));
    }

    /**
     * Cached Java2D sprite: painted once at the pixel size of (w x h) board units in a (dw x dh) design space,
     * drawn at (x, y) with size (w x h). tint = GPU color multiplier.
     */
    static void sprite(GameView g, String key, float x, float y, float w, float h, float dw, float dh, java.util.function.Consumer<java.awt.Graphics2D> paint, int tint) {
        int pw = Math.max(2, g.S(w)), ph = Math.max(2, g.S(h));
        Gx.Img im = Gx.painted(key, pw, ph, gr -> { gr.scale(pw / dw, ph / dh); paint.accept(gr); });
        Gx.image(im, g.X(x), g.Y(y), pw, ph, tint);
    }

    /** Sprite painted at base size (bw x bh) but drawn stretched into (x, y, w, h) (squash & stretch without repainting). */
    static void spriteAt(GameView g, String key, float bw, float bh, float dw, float dh, java.util.function.Consumer<java.awt.Graphics2D> paint, float x, float y, float w, float h, int tint) {
        int pw = Math.max(2, g.S(bw)), ph = Math.max(2, g.S(bh));
        Gx.Img im = Gx.painted(key, pw, ph, gr -> { gr.scale(pw / dw, ph / dh); paint.accept(gr); });
        Gx.image(im, g.X(x), g.Y(y), Math.max(1, g.S(w)), Math.max(1, g.S(h)), tint);
    }

    /** Union of shapes (avoids winding holes). */
    static java.awt.geom.Area union(java.awt.Shape... shapes) {
        java.awt.geom.Area a = new java.awt.geom.Area();
        for (java.awt.Shape s : shapes) a.add(new java.awt.geom.Area(s));
        return a;
    }

    static java.awt.Color c(int argb) { return new java.awt.Color(argb, true); }

    /** Bold text with a dark outline (readable on any background). */
    static void outlined(GameView g, String s, float x, float y, float size, int color, int outline) {
        float o = Math.max(0.6f, size * 0.07f);
        for (int k = 0; k < 8; k++) {
            double an = k * Math.PI / 4;
            g.text(s, x + (float) Math.cos(an) * o, y + (float) Math.sin(an) * o + o * 0.3f, size, Fonts.BOLD, outline);
        }
        g.text(s, x, y, size, Fonts.BOLD, color);
    }
}
