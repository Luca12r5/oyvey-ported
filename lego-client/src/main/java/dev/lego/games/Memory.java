package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Memory with 8 pairs of icon cards. */
public final class Memory extends GameView {
    private static final String[] ICONS = {"heart-fill", "star-fill", "bolt", "music", "crown", "gem", "flame", "snow"};
    private static final int[] COLORS = {0xFFFF4D6D, 0xFFFFCD03, 0xFF3D7BFF, 0xFFB57BFF, 0xFFFF6FB5, 0xFF2EE59D, 0xFFFF7A1A, 0xFF7FDBFF};
    private static final int PAIRS = 8, N = 16;
    private static final float CARD = 64, GAP = 8, GX = 14, GY = 14;
    private static final float PX = 314, PW = 80;

    private final int[] kind = new int[N];
    private final boolean[] up = new boolean[N], done = new boolean[N];
    private final float[] flip = new float[N], hoverA = new float[N], doneAt = new float[N];
    private int first = -1, second = -1, moves, pairs;
    private float wrongT, endT, clock, dealAt, finishSecs;

    public Memory() { super("memory"); }

    @Override protected float boardW() { return 408; }
    @Override protected float boardH() { return GY * 2 + CARD * 4 + GAP * 3; }

    @Override
    protected void reset() {
        for (int i = 0; i < N; i++) kind[i] = i / 2;
        for (int i = N - 1; i > 0; i--) { int j = rnd.nextInt(i + 1); int t = kind[i]; kind[i] = kind[j]; kind[j] = t; }
        for (int i = 0; i < N; i++) { up[i] = done[i] = false; flip[i] = 0; doneAt[i] = 0; }
        first = second = -1;
        moves = pairs = 0;
        wrongT = endT = finishSecs = 0;
        dealAt = clock;
    }

    @Override protected String overTitle() { return "Geschafft!"; }

    // ------------------------------------------------------------------ logic
    @Override
    protected void update(float dt) {
        if (second >= 0) {
            wrongT -= dt;
            if (wrongT <= 0) flipBack();
        }
        if (pairs == PAIRS) {
            endT += dt;
            if (endT > 1.1f) gameOver();
        }
    }

    private void flipBack() {
        if (first >= 0) up[first] = false;
        if (second >= 0) up[second] = false;
        first = second = -1;
        Sound.play("minecraft:item.book.page_turn", 0.9f, 0.4f);
    }

    private int cardAt(float x, float y) {
        int c = (int) Math.floor((x - GX) / (CARD + GAP)), r = (int) Math.floor((y - GY) / (CARD + GAP));
        if (c < 0 || r < 0 || c > 3 || r > 3) return -1;
        if (x - GX - c * (CARD + GAP) > CARD || y - GY - r * (CARD + GAP) > CARD) return -1;
        return r * 4 + c;
    }

    @Override
    protected void onClick(float x, float y, int button) {
        if (button != 0 || pairs == PAIRS) return;
        int i = cardAt(x, y);
        if (i < 0 || up[i] || done[i]) return;
        if (second >= 0) flipBack();                // resolve a pending mismatch immediately
        up[i] = true;
        Sound.play("minecraft:item.book.page_turn", 1.3f, 0.5f);
        if (first < 0) { first = i; return; }
        second = i;
        moves++;
        if (kind[first] == kind[second]) {
            done[first] = done[second] = true;
            doneAt[first] = doneAt[second] = clock + 0.18f;
            pairs++;
            first = second = -1;
            Sound.play("minecraft:entity.experience_orb.pickup", 0.9f + pairs * 0.08f, 0.4f);
            if (pairs == PAIRS) {
                finishSecs = time;
                score = Math.max(10, 500 - moves * 15L - (long) Math.floor(finishSecs));
                Sound.play("minecraft:entity.player.levelup", 1.2f, 0.4f);
            }
        } else {
            wrongT = 0.7f;
        }
    }

    // ------------------------------------------------------------------ drawing
    @Override
    protected void render() {
        clock += Gx.dt;
        float k = 1f - (float) Math.exp(-Gx.dt * 14);
        float fk = Gx.dt / 0.26f;
        float hx = (mx - bx) / u, hy = (my - by) / u;
        int hover = state == State.PLAYING && pairs < PAIRS ? cardAt(hx, hy) : -1;

        for (int i = 0; i < N; i++) {
            float target = up[i] || done[i] ? 1 : 0;
            flip[i] = flip[i] < target ? Math.min(target, flip[i] + fk) : Math.max(target, flip[i] - fk);
            hoverA[i] += ((i == hover && !up[i] && !done[i] ? 1 : 0) - hoverA[i]) * k;
            float x = GX + (i % 4) * (CARD + GAP), y = GY + (i / 4) * (CARD + GAP);
            float deal = Ease.outBack((clock - dealAt - i * 0.03f) / 0.35f);
            if (deal <= 0) continue;
            card(i, x, y, deal);
        }
        panel();
    }

    private void card(int i, float x, float y, float deal) {
        float cx = x + CARD / 2, cy = y + CARD / 2;
        float ang = (float) (Ease.inOutCubic(flip[i]) * Math.PI);
        float sx = Math.max(0.02f, Math.abs((float) Math.cos(ang)));
        float s = deal * (1 + 0.07f * (float) Math.sin(ang) + 0.04f * hoverA[i]);
        boolean front = flip[i] > 0.5f;
        float lift = 2 * hoverA[i] + 3 * (float) Math.sin(ang);
        float pop = done[i] ? Ease.clamp((clock - doneAt[i]) / 0.4f) : 0;
        if (pop > 0 && pop < 1) s *= 1 + 0.1f * (float) Math.sin(pop * Math.PI);

        Gx.push();
        Gx.translateExact(X(cx), Y(cy));
        Gx.B.scale(sx * s, s);
        Gx.translateExact(-X(cx), -Y(cy));
        // shadow
        box(x + 1, y + 2 + lift * 0.5f, CARD - 2, CARD - 1, 12, Style.light ? 0x1C000000 : 0x66000000);
        float yy = y - lift;
        if (front) {
            int col = COLORS[kind[i]];
            int face = Style.light ? 0xFFFFFFFF : 0xFF1C1F27;
            if (done[i]) face = Gx.mix(face, col, 0.12f);
            boxV(x, yy, CARD, CARD, 12, Gx.mix(face, 0xFFFFFFFF, Style.light ? 0 : 0.05f), face);
            Gx.outline(X(x), Y(yy), S(CARD), S(CARD), S(12), Math.max(1, S(done[i] ? 1.3f : 0.8f)), Gx.withAlpha(col, done[i] ? 0.9f : 0.45f));
            Gx.glow(X(cx), Y(yy + CARD / 2), S(done[i] ? 38 : 30), Gx.withAlpha(col, done[i] ? 0.4f : 0.25f));
            Gx.icon(ICONS[kind[i]], X(cx), Y(yy + CARD / 2), S(30), col);
            if (done[i] && pop > 0) {
                float ca = Ease.outBack(pop);
                circle(x + CARD - 9, yy + 9, 5.2f * ca, col);
                Gx.icon("check", X(x + CARD - 9), Y(yy + 9), S(7) * ca, 0xFF111111);
            }
        } else {
            int acc = Gx.mix(Style.accent, 0xFFFFFFFF, 0.14f * hoverA[i]);
            boxV(x, yy, CARD, CARD, 12, Gx.mix(acc, 0xFFFFFFFF, 0.16f), Gx.mix(acc, 0xFF000000, 0.18f));
            Gx.outline(X(x), Y(yy), S(CARD), S(CARD), S(12), Math.max(1, S(1)), Gx.withAlpha(Gx.mix(acc, 0xFF000000, 0.5f), 0.8f));
            box(x + 4, yy + 2, CARD - 8, 1.2f, 1, Gx.withAlpha(0xFFFFFFFF, 0.25f));
            for (int s2 = 0; s2 < 4; s2++) {
                float stx = x + CARD * (s2 % 2 == 0 ? 0.3f : 0.7f), sty = yy + CARD * (s2 < 2 ? 0.3f : 0.7f);
                circle(stx, sty + 1.3f, 8, Gx.mix(acc, 0xFF000000, 0.3f));
                circle(stx, sty, 8, Gx.mix(acc, 0xFFFFFFFF, 0.2f));
                circle(stx - 2.4f, sty - 2.4f, 2.2f, Gx.withAlpha(0xFFFFFFFF, 0.45f));
            }
        }
        Gx.pop();
        // burst ring when a pair is found
        if (done[i] && pop > 0 && pop < 1) {
            Gx.ringAt(X(cx), Y(cy), S(CARD * 0.5f + 16 * Ease.outCubic(pop)), Math.max(1, S(2 * (1 - pop))), Gx.withAlpha(COLORS[kind[i]], 0.8f * (1 - pop)));
        }
    }

    private void panel() {
        float y = GY;
        stat(y, "ZÜGE", String.valueOf(moves), -1);
        stat(y + 66, "PAARE", pairs + " / " + PAIRS, pairs / (float) PAIRS);
        int s = (int) (pairs == PAIRS ? finishSecs : time);
        stat(y + 132, "ZEIT", String.format("%d:%02d", s / 60, s % 60), -1);
        // found icons
        float gy = y + 202, cell = PW / 4;
        for (int i = 0; i < PAIRS; i++) {
            boolean found = false;
            for (int j = 0; j < N; j++) if (done[j] && kind[j] == i) { found = true; break; }
            float cx = PX + cell * (i % 4) + cell / 2, cy = gy + (i / 4) * 22 + 10;
            box(cx - 9, cy - 9, 18, 18, 6, found ? Gx.withAlpha(COLORS[i], 0.16f) : Style.surface);
            Gx.icon(ICONS[i], X(cx), Y(cy), S(10), found ? COLORS[i] : Gx.withAlpha(Style.muted, 0.5f));
        }
        String hint = pairs == PAIRS ? "Alle Paare gefunden!" : second >= 0 ? "Kein Paar" : first >= 0 ? "Noch eine Karte" : "Wähle eine Karte";
        text(hint, PX + PW / 2, gy + 60, 6.6f, Fonts.MEDIUM, pairs == PAIRS ? Style.ok : second >= 0 ? Style.danger : Style.sub);
    }

    private void stat(float y, String label, String value, float progress) {
        float h = 58;
        box(PX, y, PW, h, 12, Style.surface);
        text(label, PX + PW / 2, y + 15, 5.8f, Fonts.BOLD, Style.muted);
        text(value, PX + PW / 2, y + 34, 13, Fonts.BOLD, Style.text);
        if (progress >= 0) {
            float bw = PW - 20;
            box(PX + 10, y + h - 10, bw, 3, 1.5f, Style.surface2);
            if (progress > 0) box(PX + 10, y + h - 10, bw * progress, 3, 1.5f, Style.accent);
        }
    }
}
