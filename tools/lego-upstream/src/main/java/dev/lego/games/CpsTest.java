package dev.lego.games;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** CPS test: click as fast as possible for 10 seconds. Score = CPS * 10. */
public final class CpsTest extends GameView {
    private static final float W = 360, H = 260, DURATION = 10;
    private static final int BUCKETS = 20;   // 0.5 s each
    private static final float GRAPH_Y = 196, GRAPH_H = 50;

    private boolean running, finished;
    private long startNs;
    private int clicks;
    private final int[] buckets = new int[BUCKETS];
    private final List<float[]> ripples = new ArrayList<>();   // x, y, age, button
    private final List<float[]> parts = new ArrayList<>();     // x, y, vx, vy, age, life, size, color
    private float counterPop, finishT;
    private float graphMax = 12;

    public CpsTest() { super("cps"); }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String overTitle() { return "Fertig!"; }
    @Override protected String scoreLabel() { return "CPS"; }
    @Override protected String bestText(long best) { return String.format(Locale.ROOT, "%.1f", best / 10.0); }

    @Override
    protected String scoreText() {
        if (finished || state == State.OVER) return String.format(Locale.ROOT, "%.1f", score / 10.0);
        return String.format(Locale.ROOT, "%.1f", liveCps());
    }

    @Override
    protected void reset() {
        running = finished = false;
        clicks = 0;
        java.util.Arrays.fill(buckets, 0);
        ripples.clear();
        parts.clear();
        counterPop = finishT = 0;
        graphMax = 12;
    }

    private float elapsed() {
        if (!running) return 0;
        return Math.min(DURATION, (System.nanoTime() - startNs) / 1e9f);
    }

    private float liveCps() {
        if (!running || clicks == 0) return 0;
        return clicks / Math.max(1f, elapsed());
    }

    // ------------------------------------------------------------------ input
    @Override
    protected void onClick(float x, float y, int button) {
        if (finished) return;
        if (!running) {
            running = true;
            startNs = System.nanoTime();
        }
        float e = elapsed();
        if (e >= DURATION) return;
        clicks++;
        buckets[Math.min(BUCKETS - 1, (int) (e / (DURATION / BUCKETS)))]++;
        counterPop = 1;
        ripples.add(new float[]{x, y, 0, button});
        int col = button == 0 ? Style.accent : info.color;
        for (int i = 0; i < 5; i++) {
            float a = (float) (rnd.nextFloat() * Math.PI * 2), s = 30 + rnd.nextFloat() * 70;
            parts.add(new float[]{x, y, (float) Math.cos(a) * s, (float) Math.sin(a) * s - 20, 0, 0.35f + rnd.nextFloat() * 0.25f, 1.6f + rnd.nextFloat() * 1.6f, i % 2 == 0 ? col : 0xFFFFFFFF});
        }
        Sound.play("minecraft:ui.button.click", 1.5f + rnd.nextFloat() * 0.4f, 0.12f);
    }

    // ------------------------------------------------------------------ update
    @Override
    protected void update(float dt) {
        counterPop = Math.max(0, counterPop - dt * 6);
        for (float[] r : ripples) r[2] += dt;
        ripples.removeIf(r -> r[2] > 0.55f);
        for (float[] p : parts) {
            p[0] += p[2] * dt;
            p[1] += p[3] * dt;
            p[3] += 300 * dt;
            p[4] += dt;
        }
        parts.removeIf(p -> p[4] >= p[5]);
        int mb = 0;
        for (int b : buckets) mb = Math.max(mb, b * 2);
        graphMax += (Math.max(12, mb + 3) - graphMax) * Math.min(1, dt * 6);

        if (running && !finished && elapsed() >= DURATION) {
            finished = true;
            score = Math.round(clicks / DURATION * 10);
            Sound.play("minecraft:block.note_block.pling", 1.2f, 0.35f);
        }
        if (finished) {
            finishT += dt;
            if (finishT > 0.6f) gameOver(true);
        }
    }

    // ------------------------------------------------------------------ render
    @Override
    protected void render() {
        float e = elapsed();
        float prog = e / DURATION;
        // background panel with Lego studs
        boxV(0, 0, W, H, 12, Style.light ? 0xFFEDEFF4 : 0xFF11141B, Style.light ? 0xFFE1E4EB : 0xFF0A0C11);
        int studA = Style.light ? 0x10000000 : 0x0BFFFFFF;
        for (float y = 12; y < GRAPH_Y - 16; y += 18)
            for (float x = 12; x < W; x += 18) circle(x, y, 3, studA);
        float heat = running ? Math.min(1, liveCps() / 14f) : 0;
        Gx.glow(X(W / 2), Y(96), S(150), Gx.withAlpha(Style.accent, 0.06f + 0.12f * heat));

        // ripples
        for (float[] r : ripples) {
            float k = r[2] / 0.55f, ek = Ease.outCubic(k);
            int col = r[3] == 0 ? Style.accent : info.color;
            Gx.ringAt(X(r[0]), Y(r[1]), S(4 + ek * 26), Math.max(1, S(2.4f * (1 - k) + 0.3f)), Gx.withAlpha(col, 0.85f * (1 - k)));
            if (k < 0.4f) circle(r[0], r[1], 3 * (1 - k / 0.4f), Gx.withAlpha(0xFFFFFFFF, 0.8f * (1 - k / 0.4f)));
        }

        if (!running) {
            // start prompt
            float pulse = (float) (0.5 + 0.5 * Math.sin(time * 4));
            float cx = W / 2, cy = 92;
            Gx.ringAt(X(cx), Y(cy - 10), S(30 + pulse * 6), Math.max(1, S(1.2f)), Gx.withAlpha(Style.accent, 0.25f + 0.25f * (1 - pulse)));
            Gx.glow(X(cx), Y(cy - 10), S(46), Gx.withAlpha(Style.accent, 0.18f));
            circle(cx, cy - 10, 24, Gx.withAlpha(Style.accent, 0.16f));
            Gx.icon("mouse", X(cx), Y(cy - 10), S(22), Style.accent);
            text("Klick um zu starten", cx, cy + 34, 17, Fonts.BOLD, Style.text);
            text("10 Sekunden  •  Links- und Rechtsklick zählen", cx, cy + 52, 8, Fonts.MEDIUM, Style.sub);
        } else {
            // timer bar + remaining time
            float left = Math.max(0, DURATION - e);
            box(16, 14, W - 32, 5, 2.5f, Style.light ? 0x14000000 : 0x14FFFFFF);
            int bc = left < 3 ? Style.danger : Style.accent;
            box(16, 14, (W - 32) * (1 - prog), 5, 2.5f, bc);
            Gx.glow(X(16 + (W - 32) * (1 - prog)), Y(16.5f), S(9), Gx.withAlpha(bc, 0.6f));
            Gx.icon("clock", X(20), Y(31), S(9), Style.sub);
            Gx.textMid(String.format(Locale.ROOT, "%.2f s", left), X(28), Y(31), S(9), Fonts.BOLD, left < 3 ? Style.danger : Style.text);
            Gx.textRight("CPS " + String.format(Locale.ROOT, "%.1f", finished ? score / 10f : liveCps()), X(W - 16), Y(31), S(9), Fonts.BOLD, Style.accent);

            // big counter
            float pop = 1 + 0.14f * Ease.outCubic(counterPop);
            Gx.push();
            Gx.scaleAt(X(W / 2), Y(96), pop);
            Gx.glow(X(W / 2), Y(96), S(60), Gx.withAlpha(Style.accent, 0.1f + 0.15f * counterPop));
            text(String.valueOf(clicks), W / 2, 94, 52, Fonts.BOLD, Style.text);
            Gx.pop();
            text(finished ? "Zeit um!" : "KLICKS", W / 2, 132, 7.5f, Fonts.BOLD, finished ? Style.accent : Style.muted);
            if (finished) {
                float fe = Ease.outBack(Math.min(1, finishT / 0.3f));
                Gx.push();
                Gx.scaleAt(X(W / 2), Y(156), fe);
                String s = String.format(Locale.ROOT, "%.1f CPS", score / 10f);
                float w = Gx.width(s, S(11), Fonts.BOLD) / u + 24;
                box(W / 2 - w / 2, 146, w, 20, 10, Gx.withAlpha(Style.accent, 0.2f));
                text(s, W / 2, 156, 11, Fonts.BOLD, Style.accent);
                Gx.pop();
            }
        }

        graph(e);

        // particles
        for (float[] p : parts) {
            float a = 1 - p[4] / p[5];
            box(p[0] - p[6] / 2, p[1] - p[6] / 2, p[6], p[6], p[6] * 0.25f, Gx.withAlpha((int) p[7], a));
        }
    }

    /** Sparkline of clicks per 0.5 s (shown as CPS). */
    private void graph(float e) {
        float gx = 16, gw = W - 32, gy = GRAPH_Y, gh = GRAPH_H;
        box(gx - 6, gy - 12, gw + 12, gh + 20, 9, Style.light ? 0x0C000000 : 0x33000000);
        Gx.textMid("VERLAUF", X(gx), Y(gy - 5), S(5.4f), Fonts.BOLD, Style.muted);
        Gx.textRight("CPS / 0,5 s", X(gx + gw), Y(gy - 5), S(5.4f), Fonts.BOLD, Style.muted);
        float base = gy + gh - 2;
        float plotH = gh - 6;
        // guide lines
        for (int i = 1; i <= 2; i++) box(gx, base - plotH * i / 2f, gw, 0.5f, 0, Style.light ? 0x10000000 : 0x10FFFFFF);
        int cur = running ? Math.min(BUCKETS - 1, (int) (e / (DURATION / BUCKETS))) : -1;
        float bw = gw / BUCKETS;
        float[] px = new float[BUCKETS], py = new float[BUCKETS];
        int n = 0;
        for (int i = 0; i < BUCKETS; i++) {
            float cps = buckets[i] * 2;
            float h = plotH * Math.min(1, cps / graphMax);
            float x = gx + i * bw;
            boolean active = running && (i <= cur || finished);
            if (active && h > 0.5f) {
                int c = i == cur && !finished ? Gx.mix(Style.accent, 0xFFFFFFFF, 0.25f) : Style.accent;
                boxV(x + 1.2f, base - h, bw - 2.4f, h, 1.5f, Gx.withAlpha(c, 0.45f), Gx.withAlpha(c, 0.08f));
            } else box(x + 1.2f, base - 1, bw - 2.4f, 1, 0.5f, Style.light ? 0x14000000 : 0x14FFFFFF);
            if (active && (finished || i < cur)) {   // the running bucket is still incomplete
                px[n] = x + bw / 2;
                py[n] = base - h;
                n++;
            }
        }
        // line through the bucket tops (dotted interpolation)
        float step = Math.max(0.6f, 1.2f / u * 1.6f);
        for (int i = 0; i + 1 < n; i++) {
            float dx = px[i + 1] - px[i], dy = py[i + 1] - py[i];
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            for (float s = 0; s < len; s += step) {
                float t = s / len;
                circle(px[i] + dx * t, py[i] + dy * t, 0.9f, Style.accent);
            }
        }
        for (int i = 0; i < n; i++) {
            boolean last = i == n - 1 && !finished;
            circle(px[i], py[i], last ? 2.2f : 1.5f, last ? 0xFFFFFFFF : Style.accent);
            if (last) Gx.glow(X(px[i]), Y(py[i]), S(8), Gx.withAlpha(Style.accent, 0.6f));
        }
        // "now" marker
        if (running && !finished) {
            float nx = gx + gw * (e / DURATION);
            box(nx - 0.4f, gy - 1, 0.8f, gh - 1, 0, Gx.withAlpha(Style.accent, 0.35f));
        }
    }
}
