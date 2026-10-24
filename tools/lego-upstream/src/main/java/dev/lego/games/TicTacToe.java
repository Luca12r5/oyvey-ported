package dev.lego.games;

import java.util.ArrayList;
import java.util.List;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Tic-Tac-Toe against a (nearly) perfect minimax AI. */
public final class TicTacToe extends GameView {
    private static final float CELL = 80, GX = 16, GY = 16, TILE = 74;
    private static final float PX = 272, PW = 94;
    private static final int[][] LINES = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};
    private static final int NONE = 0, WIN = 1, DRAW = 2, LOSS = 3;

    private final int[] b = new int[9];              // 0 empty, 1 player (X), 2 AI (O)
    private final float[] placed = new float[9];     // render clock at placement
    private final float[] hoverA = new float[9];
    private int turn = 1, result = NONE;
    private int[] winLine;
    private float think, endT, winAt, clock;
    private float act1 = 1, act2 = 0;
    private boolean playerStarts = true;
    private int wins, draws, losses;

    /** Online duel against another Lego user (null = against the AI). */
    private final Duels.Duel duel;
    private int round = -1;

    public TicTacToe() { super("tictactoe"); duel = null; }

    public TicTacToe(Duels.Duel d) { super("tictactoe"); duel = d; playerStarts = d.iStart; }

    @Override protected void start() {
        if (duel != null && duel.oppLeft) { back(); return; }
        super.start();
        if (duel != null) round++;
    }

    @Override protected void back() { if (duel != null) duel.leave(); super.back(); }

    @Override public void removed() { if (duel != null) duel.leave(); super.removed(); }

    @Override protected boolean hasAi() { return duel == null; }

    @Override protected float boardW() { return 380; }
    @Override protected float boardH() { return 266; }

    @Override
    protected void reset() {
        for (int i = 0; i < 9; i++) { b[i] = 0; placed[i] = 0; }
        winLine = null;
        result = NONE;
        think = 0;
        endT = 0;
        turn = playerStarts ? 1 : 2;
    }

    @Override
    protected String overTitle() {
        if (duel != null && duel.oppLeft && result == NONE) return duel.oppName + " ist gegangen";
        return result == WIN ? "Gewonnen!" : result == DRAW ? "Unentschieden" : "Verloren";
    }

    // ------------------------------------------------------------------ logic
    @Override
    protected void update(float dt) {
        if (result != NONE) {
            endT += dt;
            if (endT > 1.15f) gameOver(duel == null);
            return;
        }
        if (duel != null) {
            if (duel.oppLeft) { gameOver(false); return; }
            if (turn == 2) {
                Integer m = duel.take(round);
                if (m != null && m >= 0 && m < 9 && b[m] == 0) place(m, 2);
            }
            return;
        }
        if (turn == 2) {
            think += dt;
            if (think >= 0.35f) place(aiMove(), 2);
        }
    }

    @Override
    protected void onClick(float x, float y, int button) {
        if (button != 0 || turn != 1 || result != NONE) return;
        int i = cellAt(x, y);
        if (i >= 0 && b[i] == 0) {
            if (duel != null) duel.send(round, i);
            place(i, 1);
        }
    }

    private int cellAt(float x, float y) {
        int c = (int) Math.floor((x - GX) / CELL), r = (int) Math.floor((y - GY) / CELL);
        if (c < 0 || r < 0 || c > 2 || r > 2) return -1;
        return r * 3 + c;
    }

    private void place(int i, int who) {
        if (i < 0 || b[i] != 0) return;
        b[i] = who;
        placed[i] = clock;
        Sound.play("minecraft:block.note_block.pling", who == 1 ? 1.5f : 1.05f, 0.3f);
        int[] l = findWin(b);
        if (l != null) {
            winLine = l;
            winAt = clock + (who == 1 ? 0.3f : 0.4f);
            result = who == 1 ? WIN : LOSS;
            if (who == 1) { wins++; score = 3; Sound.play("minecraft:entity.player.levelup", 1.3f, 0.35f); }
            else { losses++; score = 0; }
            playerStarts = !playerStarts;
        } else if (full(b)) {
            result = DRAW;
            draws++;
            score = 1;
            playerStarts = !playerStarts;
            Sound.play("minecraft:block.note_block.chime", 0.9f, 0.3f);
        } else {
            turn = 3 - who;
            think = 0;
        }
    }

    private static int[] findWin(int[] s) {
        for (int[] l : LINES) if (s[l[0]] != 0 && s[l[0]] == s[l[1]] && s[l[1]] == s[l[2]]) return l;
        return null;
    }

    private static boolean full(int[] s) {
        for (int v : s) if (v == 0) return false;
        return true;
    }

    private int aiMove() {
        List<Integer> empty = new ArrayList<>();
        for (int i = 0; i < 9; i++) if (b[i] == 0) empty.add(i);
        if (empty.isEmpty()) return -1;
        float slip = new float[]{0.55f, 0.25f, 0.08f, 0f}[aiLevel()];
        if (rnd.nextFloat() < slip) return empty.get(rnd.nextInt(empty.size()));   // slips: the AI is beatable (not on "Profi")
        int best = Integer.MIN_VALUE;
        List<Integer> bestMoves = new ArrayList<>();
        for (int i : empty) {
            b[i] = 2;
            int s = minimax(false, 1);
            b[i] = 0;
            if (s > best) { best = s; bestMoves.clear(); bestMoves.add(i); }
            else if (s == best) bestMoves.add(i);
        }
        return bestMoves.get(rnd.nextInt(bestMoves.size()));
    }

    private int minimax(boolean aiTurn, int depth) {
        int[] w = findWin(b);
        if (w != null) return b[w[0]] == 2 ? 10 - depth : depth - 10;
        if (full(b)) return 0;
        int best = aiTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (int i = 0; i < 9; i++) {
            if (b[i] != 0) continue;
            b[i] = aiTurn ? 2 : 1;
            int s = minimax(!aiTurn, depth + 1);
            b[i] = 0;
            best = aiTurn ? Math.max(best, s) : Math.min(best, s);
        }
        return best;
    }

    // ------------------------------------------------------------------ drawing
    private static int oColor() {
        int a = Style.accent;
        int dr = ((a >> 16) & 255) - 0xFF, dg = ((a >> 8) & 255) - 0xB0, db = (a & 255) - 0x2E;
        return dr * dr + dg * dg + db * db < 150 * 150 ? 0xFF3FA9FF : 0xFFFFB02E;
    }

    private int pieceColor(int who) { return who == 1 ? Style.accent : oColor(); }

    /** Thick line with round caps (board units). */
    private void seg(float x1, float y1, float x2, float y2, float th, int col) {
        float px1 = bx + x1 * u, py1 = by + y1 * u, px2 = bx + x2 * u, py2 = by + y2 * u;
        float len = (float) Math.hypot(px2 - px1, py2 - py1);
        int r = Math.max(1, Math.round(th * u / 2));
        int n = Math.max(1, (int) Math.ceil(len / Math.max(1f, r * 0.3f)));
        for (int i = 0; i <= n; i++) {
            float t = i / (float) n;
            Gx.circle(Math.round(px1 + (px2 - px1) * t), Math.round(py1 + (py2 - py1) * t), r, col);
        }
    }

    private void drawX(float cx, float cy, float arm, float th, float t, int col) {
        float p1 = Ease.outCubic(t / 0.16f), p2 = Ease.outCubic((t - 0.13f) / 0.16f);
        if (p1 > 0) seg(cx - arm, cy - arm, cx - arm + 2 * arm * p1, cy - arm + 2 * arm * p1, th, col);
        if (p2 > 0) seg(cx + arm, cy - arm, cx + arm - 2 * arm * p2, cy - arm + 2 * arm * p2, th, col);
    }

    private void drawO(float cx, float cy, float rad, float th, float t, int col) {
        float p = Ease.outCubic(t / 0.3f);
        if (p <= 0) return;
        if (p >= 1) { Gx.ringAt(X(cx), Y(cy), S(rad + th / 2), S(th), col); return; }
        int r = Math.max(1, Math.round(th * u / 2));
        float arc = (float) (Math.PI * 2 * p);
        int n = Math.max(2, (int) Math.ceil(arc * rad * u / Math.max(1f, r * 0.3f)));
        for (int i = 0; i <= n; i++) {
            double a = -Math.PI / 2 + arc * i / n;
            Gx.circle(Math.round(bx + (cx + (float) Math.cos(a) * rad) * u), Math.round(by + (cy + (float) Math.sin(a) * rad) * u), r, col);
        }
    }

    private void glyph(int who, float cx, float cy, float size, float t, int col) {
        if (who == 1) drawX(cx, cy, size, size * 0.38f, t, col);
        else drawO(cx, cy, size * 0.95f, size * 0.38f, t, col);
    }

    @Override
    protected void render() {
        if (duel != null && state == State.READY) start();
        clock += Gx.dt;
        float k = 1f - (float) Math.exp(-Gx.dt * 14);
        float hx = (mx - bx) / u, hy = (my - by) / u;
        int hover = state == State.PLAYING && turn == 1 && result == NONE ? cellAt(hx, hy) : -1;
        if (hover >= 0 && b[hover] != 0) hover = -1;
        int tile = Style.light ? 0xFFFFFFFF : 0xFF171920;
        boolean winShown = winLine != null && clock >= winAt;
        int winCol = winLine != null ? pieceColor(b[winLine[0]]) : Style.accent;
        float wp = winLine != null ? Ease.outCubic((clock - winAt) / 0.3f) : 0;

        // grid plate
        box(GX - 5, GY - 5, CELL * 3 + 4, CELL * 3 + 4, 16, Style.light ? 0x0D000000 : 0x0BFFFFFF);
        for (int i = 0; i < 9; i++) {
            hoverA[i] += ((i == hover ? 1 : 0) - hoverA[i]) * k;
            float x = GX + (i % 3) * CELL + (CELL - TILE) / 2 - 3, y = GY + (i / 3) * CELL + (CELL - TILE) / 2 - 3;
            float lift = 1.5f * hoverA[i];
            boolean inWin = winShown && (winLine[0] == i || winLine[1] == i || winLine[2] == i);
            int col = Gx.mix(tile, Style.accent, 0.10f * hoverA[i]);
            if (inWin) col = Gx.mix(tile, winCol, 0.2f * wp);
            box(x, y + 2.2f, TILE, TILE, 13, Style.light ? 0x16000000 : 0x55000000);
            boxV(x, y - lift, TILE, TILE, 13, Gx.mix(col, 0xFFFFFFFF, Style.light ? 0 : 0.05f), col);
            Gx.outline(X(x), Y(y - lift), S(TILE), S(TILE), S(13), Math.max(1, S(0.6f)),
                    inWin ? Gx.withAlpha(winCol, 0.6f * wp) : Gx.mix(Style.stroke, Gx.withAlpha(Style.accent, 0.5f), hoverA[i]));
            float cx = x + TILE / 2, cy = y + TILE / 2 - lift;
            if (b[i] != 0) {
                int pc = pieceColor(b[i]);
                float t = clock - placed[i];
                Gx.glow(X(cx), Y(cy), S(40), Gx.withAlpha(pc, (inWin ? 0.32f : 0.10f) * Ease.outCubic(t / 0.3f)));
                glyph(b[i], cx, cy, 20, t, pc);
            } else if (hoverA[i] > 0.02f) {
                glyph(1, cx, cy, 20, 1, Gx.mix(col, Style.accent, 0.32f * hoverA[i]));
            }
        }

        // winning line
        if (winShown) {
            float x1 = GX + (winLine[0] % 3) * CELL + CELL / 2 - 3, y1 = GY + (winLine[0] / 3) * CELL + CELL / 2 - 3;
            float x2 = GX + (winLine[2] % 3) * CELL + CELL / 2 - 3, y2 = GY + (winLine[2] / 3) * CELL + CELL / 2 - 3;
            float dx = x2 - x1, dy = y2 - y1, len = (float) Math.hypot(dx, dy);
            float ex = dx / len * CELL * 0.36f, ey = dy / len * CELL * 0.36f;
            float sx = x1 - ex, sy = y1 - ey, tx = x2 + ex, ty = y2 + ey;
            float lx = sx + (tx - sx) * wp, ly = sy + (ty - sy) * wp;
            for (int i = 0; i <= 6; i++) {
                float f = i / 6f * wp;
                Gx.glow(X(sx + (tx - sx) * f), Y(sy + (ty - sy) * f), S(26), Gx.withAlpha(winCol, 0.22f));
            }
            seg(sx, sy, lx, ly, 7.5f, Gx.mix(winCol, 0xFF000000, 0.35f));
            seg(sx, sy, lx, ly, 5f, Gx.mix(winCol, 0xFFFFFFFF, 0.45f));
        }

        // side panel
        act1 += ((turn == 1 && result == NONE ? 1 : 0) - act1) * k;
        act2 += ((turn == 2 && result == NONE ? 1 : 0) - act2) * k;
        playerCard(GY, "Du", "Spieler", 1, act1);
        playerCard(GY + 66, duel == null ? "KI" : duel.oppName, duel == null ? "Computer" : "Online", 2, act2);

        String status;
        int sc = Style.text;
        if (result == WIN) { status = "Gewonnen!"; sc = Style.ok; }
        else if (result == LOSS) { status = "Verloren"; sc = Style.danger; }
        else if (result == DRAW) { status = "Unentschieden"; sc = Style.sub; }
        else if (turn == 2) status = (duel == null ? "KI denkt" : duel.oppName + " ist dran") + ".".repeat(1 + (int) (clock * 4) % 3);
        else status = "Du bist dran";
        float sy = GY + 150;
        text(status, PX + PW / 2, sy, 9.5f, Fonts.BOLD, sc);
        text(result == NONE ? (turn == 1 ? "Setze dein X" : "Bitte warten") : "Runde beendet", PX + PW / 2, sy + 14, 6.8f, Fonts.REGULAR, Style.sub);

        float ty = GY + 180;
        box(PX, ty, PW, 60, 12, Style.surface);
        tally(PX + PW / 6f, ty, wins, "Siege", Style.ok);
        tally(PX + PW / 2f, ty, draws, "Remis", Style.sub);
        tally(PX + PW * 5 / 6f, ty, losses, "Niederl.", Style.danger);
    }

    private void playerCard(float y, String name, String sub, int who, float act) {
        int pc = pieceColor(who);
        float h = 58;
        if (act > 0.02f) Gx.glow(X(PX + PW / 2), Y(y + h / 2), S(58), Gx.withAlpha(pc, 0.16f * act));
        box(PX, y, PW, h, 12, Gx.mix(Style.surface, pc, 0.10f * act));
        Gx.outline(X(PX), Y(y), S(PW), S(h), S(12), Math.max(1, S(0.8f)), Gx.mix(Style.stroke, Gx.withAlpha(pc, 0.85f), act));
        box(PX + 8, y + 13, 32, 32, 9, Gx.withAlpha(pc, 0.14f));
        glyph(who, PX + 24, y + 29, 8, 1, pc);
        Gx.textMid(Fonts.ellipsize(name, Fonts.BOLD, 10 * u, S(PW - 48 - 4)), X(PX + 48), Y(y + 23), 10 * u, Fonts.BOLD, Style.text);
        Gx.textMid(sub, X(PX + 48), Y(y + 37), 6.4f * u, Fonts.REGULAR, Style.sub);
        if (act > 0.02f) {
            float pulse = 0.6f + 0.4f * (float) Math.sin(clock * 6);
            circle(PX + PW - 10, y + 10, 2.6f, Gx.withAlpha(pc, act * pulse));
        }
    }

    private void tally(float cx, float y, int n, String label, int col) {
        text(String.valueOf(n), cx, y + 24, 13, Fonts.BOLD, Style.text);
        text(label, cx, y + 43, 5.6f, Fonts.MEDIUM, col);
    }
}
