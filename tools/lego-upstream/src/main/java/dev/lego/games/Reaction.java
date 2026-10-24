package dev.lego.games;

import java.util.ArrayList;
import java.util.List;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Reaction test: 5 rounds, wait for green, click as fast as possible. Score = average ms (lower is better). */
public final class Reaction extends GameView {
    private static final float W = 360, H = 260, STRIP = 52;
    private static final int ROUNDS = 5;
    private static final int RED_TOP = 0xFF5A1622, RED_BOT = 0xFF2A0A12;
    private static final int GREEN_TOP = 0xFF2EE070, GREEN_BOT = 0xFF14A84C;
    private static final int EARLY_TOP = 0xFFE07A1E, EARLY_BOT = 0xFF9A4208;

    private enum Phase { WAIT, GO, EARLY, RESULT }

    private Phase phase = Phase.WAIT;
    private float phaseT, waitDur;
    private long greenNs;
    private int lastMs;
    private boolean fresh;
    private final List<Integer> results = new ArrayList<>();
    private final List<float[]> parts = new ArrayList<>();   // x, y, vx, vy, age, life, size, color
    /** Displayed background colors (eased towards the phase colors). */
    private int curTop = RED_TOP, curBot = RED_BOT;

    public Reaction() { super("reaction"); }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Schnitt"; }
    @Override protected String scoreText() { return results.isEmpty() ? "-" : average() + " ms"; }
    @Override protected String bestText(long best) { return best + " ms"; }
    @Override protected String overTitle() { return "Fertig!"; }

    @Override
    protected void reset() {
        results.clear();
        parts.clear();
        lastMs = 0;
        fresh = true;
        enterWait();
        curTop = RED_TOP;
        curBot = RED_BOT;
    }

    private void enterWait() {
        phase = Phase.WAIT;
        phaseT = 0;
        waitDur = 1.5f + rnd.nextFloat() * 2.5f;
    }

    private long average() {
        if (results.isEmpty()) return 0;
        long s = 0;
        for (int r : results) s += r;
        return Math.round(s / (double) results.size());
    }

    // ------------------------------------------------------------------ input
    @Override protected void onClick(float x, float y, int button) { press(x, y); }

    @Override
    protected void onKey(int k) {
        if (k == K_SPACE || k == K_ENTER) press(W / 2, (H - STRIP) / 2);
    }

    private void press(float x, float y) {
        if (fresh) return;   // the key / click that started the round
        switch (phase) {
            case WAIT -> {
                phase = Phase.EARLY;
                phaseT = 0;
                Sound.play("minecraft:block.note_block.bass", 0.6f, 0.4f);
            }
            case GO -> {
                long ns = System.nanoTime() - greenNs;
                lastMs = (int) Math.max(1, Math.round(ns / 1_000_000.0));
                results.add(lastMs);
                phase = Phase.RESULT;
                phaseT = 0;
                burst(x, y, rating(lastMs)[1]);
                Sound.play("minecraft:entity.experience_orb.pickup", 0.9f + Math.max(0, 450 - lastMs) / 450f * 0.9f, 0.35f);
            }
            case RESULT -> {
                if (results.size() < ROUNDS && phaseT > 0.3f) { enterWait(); Sound.click(); }
            }
            default -> { }
        }
    }

    private void burst(float x, float y, int col) {
        for (int i = 0; i < 26; i++) {
            float a = (float) (rnd.nextFloat() * Math.PI * 2), s = 50 + rnd.nextFloat() * 150;
            int c = i % 3 == 0 ? 0xFFFFFFFF : col;
            parts.add(new float[]{x, y, (float) Math.cos(a) * s, (float) Math.sin(a) * s - 40, 0, 0.6f + rnd.nextFloat() * 0.5f, 2 + rnd.nextFloat() * 3, c});
        }
    }

    // ------------------------------------------------------------------ update
    @Override
    protected void update(float dt) {
        fresh = false;
        phaseT += dt;
        for (float[] p : parts) {
            p[0] += p[2] * dt;
            p[1] += p[3] * dt;
            p[3] += 380 * dt;
            p[2] *= 0.985f;
            p[4] += dt;
        }
        parts.removeIf(p -> p[4] >= p[5]);
        switch (phase) {
            case WAIT -> {
                if (phaseT >= waitDur) {
                    phase = Phase.GO;
                    phaseT = 0;
                    greenNs = System.nanoTime();
                    curTop = GREEN_TOP;   // green must appear instantly
                    curBot = GREEN_BOT;
                    Sound.play("minecraft:block.note_block.pling", 1.8f, 0.35f);
                }
            }
            case EARLY -> { if (phaseT >= 1.0f) enterWait(); }
            case RESULT -> {
                if (results.size() >= ROUNDS) {
                    if (phaseT >= 1.5f) {
                        score = average();
                        gameOver(true);
                    }
                } else if (phaseT >= 1.8f) enterWait();
            }
            default -> { }
        }
    }

    // ------------------------------------------------------------------ helpers
    /** {text, color} of a reaction time. */
    private static int[] rating(int ms) {
        if (ms < 180) return new int[]{0, 0xFFFFC53D};
        if (ms < 230) return new int[]{1, 0xFF1ED760};
        if (ms < 280) return new int[]{2, 0xFF3FC7FF};
        if (ms < 350) return new int[]{3, 0xFFFFE14D};
        return new int[]{4, 0xFFFF7A45};
    }

    private static final String[] RATING = {"Blitzschnell!", "Sehr gut!", "Gut", "Okay", "Langsam"};

    // ------------------------------------------------------------------ render
    @Override
    protected void render() {
        float mh = H - STRIP;
        // background colors (eased, except the instant switch to green)
        int tTop, tBot;
        switch (phase) {
            case GO -> { tTop = GREEN_TOP; tBot = GREEN_BOT; }
            case EARLY -> { tTop = EARLY_TOP; tBot = EARLY_BOT; }
            case RESULT -> {
                int base = Style.light ? 0xFF2A3040 : 0xFF161A24;
                tTop = Gx.mix(base, Style.accent, 0.22f);
                tBot = Gx.mix(base, 0xFF000000, 0.25f);
            }
            default -> { tTop = RED_TOP; tBot = RED_BOT; }
        }
        float k = Math.min(1, Gx.dt * 12);
        if (state != State.PLAYING) k = 1;
        curTop = Gx.mix(curTop, tTop, k);
        curBot = Gx.mix(curBot, tBot, k);
        boxV(0, 0, W, mh, 12, curTop, curBot);
        boxV(0, mh - 14, W, 14, 0, Gx.mix(curTop, curBot, (mh - 14) / mh), curBot);
        // Lego baseplate studs
        for (float y = 14; y < mh; y += 20)
            for (float x = 14; x < W; x += 20) {
                circle(x, y + 0.6f, 3.6f, 0x14000000);
                circle(x, y, 3.4f, 0x0CFFFFFF);
            }

        float cx = W / 2, cy = mh / 2 - 6;
        String roundTxt = "Runde " + Math.min(ROUNDS, results.size() + (phase == Phase.RESULT ? 0 : 1)) + " / " + ROUNDS;
        pill(roundTxt, cx, 16, 0x33000000, 0xDDFFFFFF);

        switch (phase) {
            case WAIT -> {
                float pulse = (float) (0.5 + 0.5 * Math.sin(time * 3.2));
                Gx.glow(X(cx), Y(cy - 22), S(52 + pulse * 8), Gx.withAlpha(0xFFFF4D5E, 0.22f + pulse * 0.1f));
                // spinning dots ring
                for (int i = 0; i < 8; i++) {
                    double a = time * 4 + i * Math.PI / 4;
                    float a2 = 0.25f + 0.75f * (i / 7f);
                    circle(cx + (float) Math.cos(a) * 17, cy - 22 + (float) Math.sin(a) * 17, 2.2f, Gx.withAlpha(0xFFFFB3BA, a2));
                }
                Gx.icon("hourglass", X(cx), Y(cy - 22), S(16), 0xFFFFD6DA);
                text("Warte auf Grün…", cx, cy + 16, 19, Fonts.BOLD, 0xFFFFFFFF);
                text("Klick erst, wenn das Feld grün wird", cx, cy + 34, 8.5f, Fonts.MEDIUM, 0xB3FFFFFF);
            }
            case GO -> {
                float e = Ease.outBack(Math.min(1, phaseT / 0.18f));
                for (int i = 0; i < 3; i++) {
                    float rt = ((phaseT * 1.6f) + i / 3f) % 1f;
                    Gx.ringAt(X(cx), Y(cy - 20), S(14 + rt * 70), Math.max(1, S(2.2f * (1 - rt))), Gx.withAlpha(0xFFFFFFFF, 0.5f * (1 - rt)));
                }
                Gx.glow(X(cx), Y(cy - 20), S(60), 0x55FFFFFF);
                Gx.push();
                Gx.scaleAt(X(cx), Y(cy), 0.6f + 0.4f * e);
                Gx.icon("bolt", X(cx), Y(cy - 20), S(24), 0xFFFFFFFF);
                text("JETZT KLICKEN!", cx, cy + 20, 26, Fonts.BOLD, 0xFFFFFFFF);
                Gx.pop();
            }
            case EARLY -> {
                float sh = (float) Math.sin(phaseT * 60) * 5 * Math.max(0, 1 - phaseT * 3);
                float e = Ease.outBack(Math.min(1, phaseT / 0.25f));
                Gx.push();
                Gx.translate(S(sh), 0);
                Gx.scaleAt(X(cx), Y(cy), 0.7f + 0.3f * e);
                Gx.icon("warning", X(cx), Y(cy - 22), S(24), 0xFFFFFFFF);
                text("Zu früh!", cx, cy + 16, 24, Fonts.BOLD, 0xFFFFFFFF);
                text("Warte auf Grün – die Runde wird wiederholt", cx, cy + 36, 8.5f, Fonts.MEDIUM, 0xCCFFFFFF);
                Gx.pop();
                // restart countdown bar
                float f = Math.min(1, phaseT / 1.0f);
                box(cx - 40, cy + 50, 80, 3, 1.5f, 0x40FFFFFF);
                box(cx - 40, cy + 50, 80 * (1 - f), 3, 1.5f, 0xFFFFFFFF);
            }
            case RESULT -> {
                int[] rt = rating(lastMs);
                float e = Ease.outBack(Math.min(1, phaseT / 0.3f));
                float count = Ease.outCubic(Math.min(1, phaseT / 0.35f));
                Gx.glow(X(cx), Y(cy - 8), S(80), Gx.withAlpha(rt[1], 0.22f));
                Gx.push();
                Gx.scaleAt(X(cx), Y(cy - 8), 0.55f + 0.45f * e);
                int shown = Math.round(lastMs * count);
                float nw = Gx.width(String.valueOf(shown), S(38), Fonts.BOLD) / u;
                float uw = Gx.width(" ms", S(16), Fonts.BOLD) / u;
                float left = cx - (nw + uw) / 2;
                Gx.textMid(String.valueOf(shown), X(left), Y(cy - 12), S(38), Fonts.BOLD, 0xFFFFFFFF);
                Gx.textMid(" ms", X(left + nw), Y(cy - 5), S(16), Fonts.BOLD, 0xB3FFFFFF);
                Gx.pop();
                float e2 = Ease.outCubic(Math.min(1, Math.max(0, phaseT - 0.15f) / 0.3f));
                Gx.pushAlpha(e2);
                String r = RATING[rt[0]];
                float rw = Gx.width(r, S(10), Fonts.BOLD) / u + 22;
                box(cx - rw / 2, cy + 18 + (1 - e2) * 6, rw, 18, 9, Gx.withAlpha(rt[1], 0.2f));
                text(r, cx, cy + 27 + (1 - e2) * 6, 10, Fonts.BOLD, rt[1]);
                String hint = results.size() >= ROUNDS ? "Auswertung…" : "Klick für die nächste Runde";
                text(hint, cx, cy + 50, 7.5f, Fonts.MEDIUM, 0x99FFFFFF);
                Gx.popAlpha();
            }
        }

        // particles
        for (float[] p : parts) {
            float a = 1 - p[4] / p[5];
            box(p[0] - p[6] / 2, p[1] - p[6] / 2, p[6], p[6], p[6] * 0.25f, Gx.withAlpha((int) p[7], a));
        }

        // bottom strip: round chips + running average
        int stripBg = Style.light ? 0xFFDDE0E7 : 0xFF111318;
        box(0, mh, W, STRIP, 12, stripBg);
        box(0, mh, W, 14, 0, stripBg);
        box(0, mh, W, 0.8f, 0, Style.stroke);
        float chipW = 46, chipH = 32, gap = 6, x0 = 12, y0 = mh + (STRIP - chipH) / 2;
        for (int i = 0; i < ROUNDS; i++) {
            float x = x0 + i * (chipW + gap);
            boolean done = i < results.size();
            boolean cur = i == results.size() && state == State.PLAYING;
            if (done) {
                int ms = results.get(i);
                int c = rating(ms)[1];
                float pop = i == results.size() - 1 && phase == Phase.RESULT ? Ease.outBack(Math.min(1, phaseT / 0.3f)) : 1;
                Gx.push();
                Gx.scaleAt(X(x + chipW / 2), Y(y0 + chipH / 2), pop);
                box(x, y0, chipW, chipH, 8, Gx.withAlpha(c, 0.16f));
                Gx.outline(X(x), Y(y0), S(chipW), S(chipH), S(8), Math.max(1, S(0.7f)), Gx.withAlpha(c, 0.55f));
                text(ms + "", x + chipW / 2, y0 + 13, 10, Fonts.BOLD, Style.text);
                text("ms", x + chipW / 2, y0 + 24, 6, Fonts.MEDIUM, c);
                Gx.pop();
            } else {
                box(x, y0, chipW, chipH, 8, Style.light ? 0xFFEEF0F4 : 0xFF1A1D24);
                if (cur) {
                    float pulse = (float) (0.5 + 0.5 * Math.sin(time * 5));
                    Gx.outline(X(x), Y(y0), S(chipW), S(chipH), S(8), Math.max(1, S(0.9f)), Gx.withAlpha(Style.accent, 0.5f + 0.5f * pulse));
                }
                text(String.valueOf(i + 1), x + chipW / 2, y0 + chipH / 2, 9, Fonts.BOLD, cur ? Style.accent : Style.muted);
            }
        }
        // average box
        float ax = x0 + ROUNDS * (chipW + gap) + 2, aw = W - 12 - ax;
        box(ax, y0, aw, chipH, 8, Gx.withAlpha(Style.accent, 0.14f));
        text("SCHNITT", ax + aw / 2, y0 + 9, 5.8f, Fonts.BOLD, Style.accent);
        text(results.isEmpty() ? "-" : average() + " ms", ax + aw / 2, y0 + 22, 10, Fonts.BOLD, Style.text);
    }

    private void pill(String s, float cx, float cy, int bg, int fg) {
        float w = Gx.width(s, S(7.5f), Fonts.BOLD) / u + 18;
        box(cx - w / 2, cy - 8, w, 16, 8, bg);
        text(s, cx, cy, 7.5f, Fonts.BOLD, fg);
    }
}
