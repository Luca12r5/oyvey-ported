package dev.lego.games;

import dev.lego.ui.Fonts;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Schere Stein Papier — best of 5 against AI or duel. */
public final class RPS extends GameView {
    private static final String[] NAMES = {"Stein", "Schere", "Papier"};
    private static final int[][] BEATS = {{1, 0}, {2, 1}, {0, 2}};

    private final Duels.Duel duel;
    private int round = -1, myScore, oppScore;
    private int myPick = -1, oppPick = -1;
    private float revealAt, clock;
    private boolean revealed;

    public RPS() { super("rps"); duel = null; }
    public RPS(Duels.Duel d) { super("rps"); duel = d; }

    @Override protected void start() {
        if (duel != null && duel.oppLeft) { back(); return; }
        super.start();
        if (duel != null) round++;
    }
    @Override protected void back() { if (duel != null) duel.leave(); super.back(); }
    @Override public void removed() { if (duel != null) duel.leave(); super.removed(); }
    @Override protected boolean hasAi() { return duel == null; }
    @Override protected float boardW() { return 360; }
    @Override protected float boardH() { return 220; }

    @Override protected void reset() {
        myPick = oppPick = -1;
        revealed = false;
        revealAt = 0;
        clock = 0;
    }

    @Override protected String overTitle() {
        if (duel != null && duel.oppLeft) return duel.oppName + " ist gegangen";
        return myScore >= 3 ? "Gewonnen!" : "Verloren";
    }

    @Override protected void update(float dt) {
        clock += dt;
        if (state != State.PLAYING) return;
        if (duel != null && duel.oppLeft) { gameOver(false); return; }

        if (myPick >= 0 && oppPick < 0) {
            if (duel != null) {
                Integer m = duel.take(round);
                if (m != null && m >= 0 && m <= 2) oppPick = m;
            } else {
                int diff = GameInfo.difficulty("rps");
                if (diff <= 1) oppPick = (int) (Math.random() * 3);
                else oppPick = BEATS[myPick][1];
                if (diff == 2 && Math.random() < 0.35) oppPick = (int) (Math.random() * 3);
            }
        }

        if (myPick >= 0 && oppPick >= 0 && !revealed) {
            if (revealAt == 0) revealAt = clock + (duel != null ? 0.3f : 0.6f);
            if (clock >= revealAt) {
                revealed = true;
                int res = (myPick == oppPick) ? 0 : (BEATS[myPick][0] == oppPick ? 1 : -1);
                if (res == 1) { myScore++; Sound.play("minecraft:entity.experience_orb.pickup", 1.2f, 0.6f); }
                else if (res == -1) { oppScore++; Sound.play("minecraft:entity.villager.no", 0.8f, 0.6f); }
                else Sound.play("minecraft:block.note_block.hat", 1.0f, 0.4f);
            }
        }

        if (revealed && clock > revealAt + 1.5f) {
            if (myScore >= 3 || oppScore >= 3) {
                gameOver(duel == null);
                return;
            }
            myPick = oppPick = -1;
            revealed = false;
            revealAt = 0;
            if (duel != null) round++;
        }
    }

    @Override protected void onClick(float x, float y, int bt) {
        if (myPick >= 0 || state != State.PLAYING) return;
        float bw2 = S(90), bh2 = S(90), gap = S(12);
        float startX = boardW() / 2f - 1.5f * bw2 - gap;
        float by2 = S(60);
        for (int i = 0; i < 3; i++) {
            float bx = startX + i * (bw2 + gap);
            if (x >= bx && x <= bx + bw2 && y >= by2 && y <= by2 + bh2) {
                myPick = i;
                if (duel != null) duel.send(round, i);
                Sound.play("minecraft:ui.button.click", 1.0f, 0.5f);
                return;
            }
        }
    }

    @Override protected void render() {
        float cx = boardW() / 2f;

        text(myScore + " : " + oppScore, cx, S(10), S(14), Fonts.BOLD, 0xFFFFFFFF);
        text("Best of 5", cx, S(28), S(7), Fonts.REGULAR, 0x88FFFFFF);

        float bw2 = S(90), bh2 = S(90), gap = S(12);
        float startX = cx - 1.5f * bw2 - gap;
        float by2 = S(60);

        for (int i = 0; i < 3; i++) {
            float bx = startX + i * (bw2 + gap);
            boolean picked = myPick == i;
            int bg = picked ? 0x44FFFFFF : 0x11FFFFFF;
            box(bx, by2, bw2, bh2, S(12), bg);
            if (picked) box(bx, by2, bw2, bh2, S(12), Style.accent & 0x44FFFFFF);
            text(NAMES[i], bx + bw2 / 2f, by2 + bh2 / 2f, S(10), Fonts.BOLD, picked ? Style.accent : 0xCCFFFFFF);
        }

        if (revealed) {
            float ry = by2 + bh2 + S(16);
            int res = (myPick == oppPick) ? 0 : (BEATS[myPick][0] == oppPick ? 1 : -1);
            int col = res == 1 ? 0xFF1ED760 : res == -1 ? 0xFFFF4D5E : 0xFFFFC53D;
            String txt = res == 1 ? "Du gewinnst!" : res == -1 ? "Gegner gewinnt" : "Unentschieden";
            text(NAMES[myPick] + "  vs  " + NAMES[oppPick], cx, ry, S(9), Fonts.BOLD, col);
            text(txt, cx, ry + S(14), S(7.5f), Fonts.MEDIUM, col);
        } else if (myPick >= 0) {
            float ry = by2 + bh2 + S(16);
            text(duel != null ? "Warte auf " + duel.oppName + " ..." : "Gegner wählt ...", cx, ry, S(8), Fonts.MEDIUM, 0x88FFFFFF);
        }
    }
}
