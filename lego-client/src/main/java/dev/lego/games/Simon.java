package dev.lego.games;

import java.util.ArrayList;
import java.util.List;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Simon says with four Lego bricks: repeat the growing color sequence. */
public final class Simon extends GameView {
    private static final int[] COLORS = {0xFF00AF4D, 0xFFE3000B, 0xFFFFCD03, 0xFF0A5BC4};
    private static final String[] NAMES = {"Grün", "Rot", "Gelb", "Blau"};
    private static final float[] PITCH = {0.707f, 0.891f, 1.059f, 1.335f};
    private static final float PAD = 132, GAP = 12, W = 380, PY = 12;
    private static final float PXL = (W - PAD * 2 - GAP) / 2;

    private enum Phase { WAIT, SHOW, INPUT, SUCCESS, FAIL }

    private final List<Integer> seq = new ArrayList<>();
    private Phase phase = Phase.WAIT;
    private float phaseT, clock, flash;
    private int idx, inIdx, failPad = -1;
    private boolean stepLit;
    private final float[] litT = new float[4], glow = new float[4], hoverA = new float[4], press = new float[4];

    public Simon() { super("simon"); }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return 328; }

    @Override
    protected void reset() {
        seq.clear();
        seq.add(rnd.nextInt(4));
        phase = Phase.WAIT;
        phaseT = 0;
        idx = inIdx = 0;
        failPad = -1;
        stepLit = false;
        flash = 0;
        for (int i = 0; i < 4; i++) litT[i] = 0;
    }

    @Override protected String overTitle() { return "Verloren"; }
    @Override protected String scoreLabel() { return "Runden"; }

    // ------------------------------------------------------------------ logic
    private float stepOn() { return Math.max(0.24f, 0.5f - seq.size() * 0.02f); }
    private float stepGap() { return Math.max(0.08f, 0.18f - seq.size() * 0.006f); }

    private void light(int pad, float dur) {
        litT[pad] = dur;
        press[pad] = 1;
        Sound.play("minecraft:block.note_block.harp", PITCH[pad], 0.5f);
        Sound.play("minecraft:block.note_block.bell", PITCH[pad], 0.12f);
    }

    @Override
    protected void update(float dt) {
        for (int i = 0; i < 4; i++) litT[i] = Math.max(0, litT[i] - dt);
        phaseT += dt;
        switch (phase) {
            case WAIT:
                if (phaseT > 0.8f) { phase = Phase.SHOW; phaseT = 0; idx = 0; stepLit = false; }
                break;
            case SHOW:
                if (!stepLit) { light(seq.get(idx), stepOn()); stepLit = true; }
                if (phaseT >= stepOn() + stepGap()) {
                    idx++;
                    phaseT = 0;
                    stepLit = false;
                    if (idx >= seq.size()) { phase = Phase.INPUT; inIdx = 0; }
                }
                break;
            case SUCCESS:
                if (phaseT > 0.9f) { seq.add(rnd.nextInt(4)); phase = Phase.WAIT; phaseT = 0; }
                break;
            case FAIL:
                if (phaseT > 1.1f) gameOver();
                break;
            default:
                break;
        }
    }

    private void pressPad(int pad) {
        if (phase != Phase.INPUT) return;
        light(pad, 0.25f);
        if (seq.get(inIdx) == pad) {
            inIdx++;
            if (inIdx >= seq.size()) {
                score = seq.size();
                phase = Phase.SUCCESS;
                phaseT = 0;
                flash = 1;
                Sound.play("minecraft:entity.experience_orb.pickup", 1.0f + Math.min(0.8f, seq.size() * 0.04f), 0.35f);
            }
        } else {
            failPad = pad;
            phase = Phase.FAIL;
            phaseT = 0;
            Sound.play("minecraft:block.note_block.didgeridoo", 0.6f, 0.6f);
        }
    }

    @Override
    protected void onClick(float x, float y, int button) {
        if (button != 0) return;
        int p = padAt(x, y);
        if (p >= 0) pressPad(p);
    }

    @Override
    protected void onKey(int key) {
        if (key >= 49 && key <= 52) pressPad(key - 49);
        else if (key >= 321 && key <= 324) pressPad(key - 321);
    }

    private int padAt(float x, float y) {
        for (int i = 0; i < 4; i++) {
            float px = padX(i), py = padY(i);
            if (x >= px && y >= py && x < px + PAD && y < py + PAD) {
                float dx = x - W / 2, dy = y - (PY + PAD + GAP / 2);
                if (dx * dx + dy * dy < 38 * 38) return -1;   // center hub
                return i;
            }
        }
        return -1;
    }

    private float padX(int i) { return PXL + (i % 2) * (PAD + GAP); }
    private float padY(int i) { return PY + (i / 2) * (PAD + GAP); }

    // ------------------------------------------------------------------ drawing
    @Override
    protected void render() {
        float dt = Gx.dt;
        clock += dt;
        float k = 1f - (float) Math.exp(-dt * 16);
        float hx = (mx - bx) / u, hy = (my - by) / u;
        boolean canInput = state == State.PLAYING && phase == Phase.INPUT;
        int hover = canInput ? padAt(hx, hy) : -1;
        flash = Math.max(0, flash - dt * 1.6f);

        for (int i = 0; i < 4; i++) {
            glow[i] += ((litT[i] > 0 ? 1 : 0) - glow[i]) * (litT[i] > 0 ? 1f - (float) Math.exp(-dt * 30) : k * 0.7f);
            hoverA[i] += ((i == hover ? 1 : 0) - hoverA[i]) * k;
            press[i] = Math.max(0, press[i] - dt * 5);
            pad(i);
        }

        // center hub
        float hcx = W / 2, hcy = PY + PAD + GAP / 2;
        int hub = Style.light ? 0xFFF3F4F7 : 0xFF0B0C10;
        circle(hcx, hcy, 40, hub);
        int ringCol = phase == Phase.FAIL ? Style.danger : phase == Phase.SUCCESS ? Style.ok : Style.accent;
        if (flash > 0) Gx.glow(X(hcx), Y(hcy), S(70), Gx.withAlpha(Style.ok, 0.5f * flash));
        circle(hcx, hcy, 33, Style.surface);
        float prog = phase == Phase.INPUT ? inIdx / (float) seq.size() : phase == Phase.SHOW ? (idx + (stepLit ? 1 : 0)) / (float) seq.size() : phase == Phase.SUCCESS ? 1 : 0;
        Gx.ringAt(X(hcx), Y(hcy), S(33), Math.max(1, S(2.2f)), Style.surface2);
        arc(hcx, hcy, 31.9f, 2.2f, prog, ringCol);
        text("RUNDE", hcx, hcy - 11, 5.6f, Fonts.BOLD, Style.muted);
        float bump = 1 + 0.25f * flash;
        Gx.push();
        Gx.scaleAt(X(hcx), Y(hcy + 5), bump);
        text(String.valueOf(seq.size()), hcx, hcy + 5, 17, Fonts.BOLD, Style.text);
        Gx.pop();

        // status
        String st;
        int sc = Style.text;
        switch (phase) {
            case WAIT: case SHOW: st = "Merken…"; sc = Style.sub; break;
            case INPUT: st = "Du bist dran!"; sc = Style.accent; break;
            case SUCCESS: st = "Richtig!"; sc = Style.ok; break;
            default: st = "Falsch! Richtig war " + NAMES[seq.get(inIdx)]; sc = Style.danger; break;
        }
        float sy = PY + PAD * 2 + GAP + 17;
        if (phase == Phase.INPUT) {
            float p = 1 + 0.04f * (float) Math.sin(clock * 5);
            Gx.push();
            Gx.scaleAt(X(W / 2), Y(sy), p);
            text(st, W / 2, sy, 10, Fonts.BOLD, sc);
            Gx.pop();
        } else text(st, W / 2, sy, 10, Fonts.BOLD, sc);
        // progress dots
        int n = seq.size();
        if (n <= 24) {
            float dw = 7, total = n * dw;
            for (int i = 0; i < n; i++) {
                float dx = W / 2 - total / 2 + dw * i + dw / 2;
                boolean filled = phase == Phase.INPUT ? i < inIdx : phase == Phase.SHOW ? i < idx + (stepLit ? 1 : 0) : phase == Phase.SUCCESS;
                circle(dx, sy + 14, 2, filled ? ringCol : Style.surface2);
            }
        } else text((phase == Phase.INPUT ? inIdx : 0) + " / " + n, W / 2, sy + 14, 6.5f, Fonts.MEDIUM, Style.sub);
    }

    private void pad(int i) {
        float x = padX(i), y = padY(i);
        int base = COLORS[i];
        float g = glow[i];
        boolean failHint = phase == Phase.FAIL && i == seq.get(inIdx) && ((int) (phaseT * 6) % 2 == 0);
        if (failHint) g = Math.max(g, 0.8f);
        boolean wrong = phase == Phase.FAIL && i == failPad;
        int dim = Gx.mix(base, Style.light ? 0xFFE9EBF0 : 0xFF0B0C10, 0.55f - 0.12f * hoverA[i]);
        int col = Gx.mix(dim, Gx.mix(base, 0xFFFFFFFF, 0.18f), g);
        float s = 1 - 0.035f * Ease.outCubic(press[i]) + 0.015f * hoverA[i];
        float shake = wrong ? (float) Math.sin(phaseT * 60) * 3 * Math.max(0, 1 - phaseT * 2) : 0;
        float cx = x + PAD / 2 + shake, cy = y + PAD / 2;

        if (g > 0.01f) Gx.glow(X(cx), Y(cy), S(PAD * 0.95f), Gx.withAlpha(base, 0.55f * g));
        Gx.push();
        Gx.scaleAt(X(cx), Y(cy), s);
        float px = x + shake;
        box(px, y + 3, PAD, PAD, 22, Gx.mix(col, 0xFF000000, 0.5f));
        box(px, y, PAD, PAD, 22, Gx.mix(col, 0xFF000000, 0.14f));
        box(px + 3, y + 2, PAD - 6, PAD - 7, 19, Gx.mix(col, 0xFFFFFFFF, 0.06f));
        Gx.outline(X(px), Y(y), S(PAD), S(PAD), S(22), Math.max(1, S(1)), Gx.withAlpha(Gx.mix(col, 0xFF000000, 0.5f), 0.8f));
        if (wrong) Gx.outline(X(px), Y(y), S(PAD), S(PAD), S(22), Math.max(1, S(2.5f)), Style.danger);
        // 2x2 studs
        for (int k = 0; k < 4; k++) {
            float sx = px + PAD * (k % 2 == 0 ? 0.3f : 0.7f), sy = y + PAD * (k < 2 ? 0.3f : 0.7f);
            circle(sx, sy + 2.2f, 15, Gx.mix(col, 0xFF000000, 0.3f));
            circle(sx, sy, 15, Gx.mix(col, 0xFFFFFFFF, 0.14f));
            circle(sx - 4.5f, sy - 4.5f, 3.6f, Gx.withAlpha(0xFFFFFFFF, 0.2f + 0.35f * g));
        }
        // key label in the outer corner
        float lx = i % 2 == 0 ? px + 16 : px + PAD - 16, ly = i < 2 ? y + 16 : y + PAD - 16;
        circle(lx, ly, 8, Gx.withAlpha(0xFF000000, 0.25f));
        text(String.valueOf(i + 1), lx, ly, 7.5f, Fonts.BOLD, Gx.withAlpha(0xFFFFFFFF, 0.75f));
        Gx.pop();
    }

    private void arc(float cx, float cy, float r, float th, float p, int col) {
        if (p <= 0) return;
        if (p >= 1) { Gx.ringAt(X(cx), Y(cy), S(r + th / 2), Math.max(1, S(th)), col); return; }
        int rad = Math.max(1, Math.round(th * u / 2));
        float a = (float) (Math.PI * 2 * p);
        int n = Math.max(2, (int) Math.ceil(a * r * u / Math.max(1f, rad * 0.4f)));
        for (int i = 0; i <= n; i++) {
            double ang = -Math.PI / 2 + a * i / n;
            Gx.circle(Math.round(bx + (cx + (float) Math.cos(ang) * r) * u), Math.round(by + (cy + (float) Math.sin(ang) * r) * u), rad, col);
        }
    }
}
