package dev.lego.games;

import java.util.ArrayList;
import java.util.List;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Connect Four with Lego studs against an alpha-beta AI. */
public final class ConnectFour extends GameView {
    private static final int COLS = 7, ROWS = 6;
    /** Search depth per difficulty (Leicht also plays random moves sometimes). */
    private int depth() { return new int[]{1, 3, 5, 7}[aiLevel()]; }
    private static final float C = 38, GX = 14, GY = 50, TOPY = 25, PR = 15;
    private static final float PX = 300, PW = 88;
    private static final int YEL = 0xFFFFCD03, RED = 0xFFE3000B, BLUE = 0xFF0A5BC4;
    private static final int[] ORDER = {3, 2, 4, 1, 5, 0, 6};
    private static final int NONE = 0, WIN = 1, DRAW = 2, LOSS = 3;
    private static final int WIN_SCORE = 1_000_000;

    private final int[][] g = new int[ROWS][COLS];     // row 0 = top; 0 empty, 1 player, 2 AI
    private final float[] colHover = new float[COLS];
    private int turn = 1, result = NONE;
    private boolean playerStarts = true;
    private float think, endT, clock, winAt;
    private List<int[]> win4;
    // falling piece
    private int fr = -1, fc = -1, fwho, bounces;
    private float fy, fvy;
    private float previewX = -1, act1 = 1, act2 = 0;
    private int wins, draws, losses;

    /** Online duel against another Lego user (null = against the AI). */
    private final Duels.Duel duel;
    private int round = -1;

    public ConnectFour() { super("connect4"); duel = null; }

    public ConnectFour(Duels.Duel d) { super("connect4"); duel = d; playerStarts = d.iStart; }

    @Override protected void start() {
        if (duel != null && duel.oppLeft) { back(); return; }
        super.start();
        if (duel != null) round++;
    }

    @Override protected void back() { if (duel != null) duel.leave(); super.back(); }

    @Override public void removed() { if (duel != null) duel.leave(); super.removed(); }

    private String oppName() { return duel == null ? "KI" : duel.oppName; }

    @Override protected boolean hasAi() { return duel == null; }

    @Override protected float boardW() { return 400; }
    @Override protected float boardH() { return 294; }

    @Override
    protected void reset() {
        for (int[] r : g) java.util.Arrays.fill(r, 0);
        turn = playerStarts ? 1 : 2;
        result = NONE;
        think = 0;
        endT = 0;
        win4 = null;
        fr = fc = -1;
    }

    @Override
    protected String overTitle() {
        if (duel != null && duel.oppLeft && result == NONE) return duel.oppName + " ist gegangen";
        return result == WIN ? "Gewonnen!" : result == DRAW ? "Unentschieden" : "Verloren";
    }

    // ------------------------------------------------------------------ logic
    @Override
    protected void update(float dt) {
        if (fr >= 0) {
            fvy += 1700 * dt;
            fy += fvy * dt;
            float ty = GY + fr * C + C / 2;
            if (fy >= ty) {
                fy = ty;
                if (bounces < 2 && fvy > 160) {
                    if (bounces == 0) Sound.play("minecraft:block.stone.place", 1.5f + rnd.nextFloat() * 0.2f, 0.35f);
                    fvy = -fvy * 0.24f;
                    bounces++;
                } else land();
            }
            return;
        }
        if (result != NONE) {
            endT += dt;
            if (endT > 1.3f) gameOver(duel == null);
            return;
        }
        if (duel != null) {
            if (duel.oppLeft) { gameOver(false); return; }
            if (turn == 2) {
                Integer m = duel.take(round);
                if (m != null && m >= 0 && m < COLS && openRow(g, m) >= 0) drop(m, 2);
            }
            return;
        }
        if (turn == 2) {
            think += dt;
            if (think >= 0.45f) drop(aiMove(), 2);
        }
    }

    @Override
    protected void onClick(float x, float y, int button) {
        if (button != 0) return;
        int c = colAt(x, y);
        if (c >= 0) playerDrop(c);
    }

    @Override
    protected void onKey(int key) {
        if (key >= 49 && key <= 55) playerDrop(key - 49);
        else if (key >= 321 && key <= 327) playerDrop(key - 321);   // numpad
    }

    private void playerDrop(int c) {
        if (turn != 1 || result != NONE || fr >= 0 || c < 0 || c >= COLS || openRow(g, c) < 0) return;
        if (duel != null) duel.send(round, c);
        drop(c, 1);
    }

    private int colAt(float x, float y) {
        if (y < 0 || y > GY + ROWS * C + 6) return -1;
        int c = (int) Math.floor((x - GX) / C);
        return c >= 0 && c < COLS ? c : -1;
    }

    private static int openRow(int[][] b, int c) {
        for (int r = ROWS - 1; r >= 0; r--) if (b[r][c] == 0) return r;
        return -1;
    }

    private void drop(int c, int who) {
        if (c < 0) return;
        int r = openRow(g, c);
        if (r < 0) return;
        g[r][c] = who;
        fr = r;
        fc = c;
        fwho = who;
        fy = TOPY;
        fvy = 90;
        bounces = 0;
        turn = 3 - who;
        think = 0;
        Sound.play("minecraft:ui.button.click", who == 1 ? 1.4f : 1.2f, 0.15f);
    }

    private void land() {
        int r = fr, c = fc, who = fwho;
        fr = fc = -1;
        List<int[]> w = line(g, r, c, who);
        if (w != null) {
            win4 = w;
            winAt = clock;
            result = who == 1 ? WIN : LOSS;
            if (who == 1) {
                wins++;
                score = 10 + empty() / 2;
                Sound.play("minecraft:entity.player.levelup", 1.3f, 0.35f);
            } else {
                losses++;
                score = 0;
            }
            playerStarts = !playerStarts;
        } else if (empty() == 0) {
            result = DRAW;
            draws++;
            score = 3;
            playerStarts = !playerStarts;
        }
    }

    private int empty() {
        int n = 0;
        for (int[] row : g) for (int v : row) if (v == 0) n++;
        return n;
    }

    /** All pieces of the longest line (>= 4) through (r, c) of player who, or null. */
    private static List<int[]> line(int[][] b, int r, int c, int who) {
        int[][] dirs = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        for (int[] d : dirs) {
            List<int[]> l = new ArrayList<>();
            l.add(new int[]{r, c});
            for (int s = -1; s <= 1; s += 2) {
                int rr = r + d[0] * s, cc = c + d[1] * s;
                while (rr >= 0 && rr < ROWS && cc >= 0 && cc < COLS && b[rr][cc] == who) {
                    l.add(new int[]{rr, cc});
                    rr += d[0] * s;
                    cc += d[1] * s;
                }
            }
            if (l.size() >= 4) return l;
        }
        return null;
    }

    private static boolean wins(int[][] b, int r, int c, int who) {
        int[][] dirs = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        for (int[] d : dirs) {
            int n = 1;
            for (int s = -1; s <= 1; s += 2) {
                int rr = r + d[0] * s, cc = c + d[1] * s;
                while (rr >= 0 && rr < ROWS && cc >= 0 && cc < COLS && b[rr][cc] == who) { n++; rr += d[0] * s; cc += d[1] * s; }
            }
            if (n >= 4) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ AI (negamax + alpha-beta)
    private int aiMove() {
        if (aiLevel() == 0 && rnd.nextFloat() < 0.3f) {
            List<Integer> open = new ArrayList<>();
            for (int c = 0; c < COLS; c++) if (openRow(g, c) >= 0) open.add(c);
            if (!open.isEmpty()) return open.get(rnd.nextInt(open.size()));
        }
        int[][] b = new int[ROWS][];
        for (int r = 0; r < ROWS; r++) b[r] = g[r].clone();
        int best = Integer.MIN_VALUE;
        List<Integer> moves = new ArrayList<>();
        for (int c : ORDER) {
            int r = openRow(b, c);
            if (r < 0) continue;
            b[r][c] = 2;
            int s = wins(b, r, c, 2) ? WIN_SCORE + depth() : -negamax(b, depth() - 1, -Integer.MAX_VALUE, Integer.MAX_VALUE, 1);
            b[r][c] = 0;
            if (s > best) { best = s; moves.clear(); moves.add(c); }
            else if (s == best) moves.add(c);
        }
        return moves.isEmpty() ? -1 : moves.get(rnd.nextInt(moves.size()));
    }

    private int negamax(int[][] b, int depth, int alpha, int beta, int who) {
        boolean any = false;
        for (int c = 0; c < COLS; c++) if (b[0][c] == 0) { any = true; break; }
        if (!any) return 0;
        if (depth == 0) { int e = eval(b); return who == 2 ? e : -e; }
        int best = -Integer.MAX_VALUE;
        for (int c : ORDER) {
            int r = openRow(b, c);
            if (r < 0) continue;
            b[r][c] = who;
            int s = wins(b, r, c, who) ? WIN_SCORE + depth : -negamax(b, depth - 1, -beta, -alpha, 3 - who);
            b[r][c] = 0;
            if (s > best) best = s;
            if (s > alpha) alpha = s;
            if (alpha >= beta) break;
        }
        return best;
    }

    /** Heuristic from the AI's point of view. */
    private static int eval(int[][] b) {
        int s = 0;
        for (int r = 0; r < ROWS; r++) { if (b[r][3] == 2) s += 3; else if (b[r][3] == 1) s -= 3; }
        int[][] dirs = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                for (int[] d : dirs) {
                    int er = r + d[0] * 3, ec = c + d[1] * 3;
                    if (er < 0 || er >= ROWS || ec < 0 || ec >= COLS) continue;
                    int a = 0, p = 0;
                    for (int k = 0; k < 4; k++) {
                        int v = b[r + d[0] * k][c + d[1] * k];
                        if (v == 2) a++; else if (v == 1) p++;
                    }
                    if (a > 0 && p > 0) continue;
                    if (a == 3) s += 6; else if (a == 2) s += 2;
                    if (p == 3) s -= 7; else if (p == 2) s -= 2;
                }
        return s;
    }

    // ------------------------------------------------------------------ drawing
    private void piece(float cx, float cy, int col, float scale, float dim) {
        if (dim > 0) col = Gx.mix(col, 0xFF000000, dim);
        float r = PR * scale;
        circle(cx, cy + 1.4f * scale, r, Gx.mix(col, 0xFF000000, 0.5f));
        circle(cx, cy, r, Gx.mix(col, 0xFF000000, 0.08f));
        circle(cx, cy - 0.3f * scale, r * 0.86f, Gx.mix(col, 0xFFFFFFFF, 0.1f));
        // stud
        circle(cx, cy + 0.5f * scale, r * 0.52f, Gx.mix(col, 0xFF000000, 0.16f));
        circle(cx, cy - 0.3f * scale, r * 0.47f, Gx.mix(col, 0xFFFFFFFF, 0.2f));
        circle(cx - r * 0.16f, cy - r * 0.18f, r * 0.16f, Gx.withAlpha(0xFFFFFFFF, 0.55f * (1 - dim)));
    }

    private float cx(int c) { return GX + c * C + C / 2; }
    private float cy(int r) { return GY + r * C + C / 2; }

    @Override
    protected void render() {
        if (duel != null && state == State.READY) start();
        clock += Gx.dt;
        float k = 1f - (float) Math.exp(-Gx.dt * 14);
        float hx = (mx - bx) / u, hy = (my - by) / u;
        boolean myTurn = state == State.PLAYING && turn == 1 && result == NONE && fr < 0;
        int hc = myTurn ? colAt(hx, hy) : -1;
        if (hc >= 0 && openRow(g, hc) < 0) hc = -1;
        float wp = win4 != null ? Ease.outCubic((clock - winAt) / 0.4f) : 0;

        // column numbers (hidden behind the preview piece)
        for (int c = 0; c < COLS; c++) text(String.valueOf(c + 1), cx(c), TOPY, 7, Fonts.BOLD, Gx.withAlpha(Style.sub, 0.35f * (1 - colHover[c])));

        // frame (Lego blue plate)
        float fx = GX - 6, fyy = GY - 6, fw = COLS * C + 12, fh = ROWS * C + 12;
        Gx.shadow(X(fx), Y(fyy), S(fw), S(fh), S(14), S(8), 0.45f);
        boxV(fx, fyy, fw, fh, 14, Gx.mix(BLUE, 0xFFFFFFFF, 0.12f), Gx.mix(BLUE, 0xFF000000, 0.2f));
        Gx.outline(X(fx), Y(fyy), S(fw), S(fh), S(14), Math.max(1, S(0.8f)), Gx.withAlpha(0xFF000000, 0.35f));
        for (int c = 0; c < COLS; c++) {
            colHover[c] += ((c == hc ? 1 : 0) - colHover[c]) * k;
            if (colHover[c] > 0.01f) box(GX + c * C + 2, GY - 3, C - 4, ROWS * C + 6, 12, Gx.withAlpha(0xFFFFFFFF, 0.1f * colHover[c]));
        }
        int hole = Style.light ? 0xFF0C2250 : 0xFF06112A;
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                circle(cx(c), cy(r) - 0.8f, PR + 1.4f, Gx.mix(BLUE, 0xFF000000, 0.45f));
                circle(cx(c), cy(r), PR + 0.6f, Gx.mix(BLUE, 0xFFFFFFFF, 0.14f));
                circle(cx(c), cy(r), PR, hole);
            }

        // pieces
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                if (g[r][c] == 0 || (r == fr && c == fc)) continue;
                boolean inWin = inWin(r, c);
                float dim = win4 != null && !inWin ? 0.45f * wp : 0;
                float sc = 1;
                if (inWin) {
                    sc = 1 + 0.06f * (float) Math.sin((clock - winAt) * 7) * wp;
                    Gx.glow(X(cx(c)), Y(cy(r)), S(30), Gx.withAlpha(0xFFFFFFFF, 0.3f * wp));
                }
                piece(cx(c), cy(r), g[r][c] == 1 ? YEL : RED, sc, dim);
                if (inWin) Gx.ringAt(X(cx(c)), Y(cy(r)), S(PR + 2.2f), Math.max(1, S(1.6f)), Gx.withAlpha(0xFFFFFFFF, 0.9f * wp));
            }

        // falling piece
        if (fr >= 0) piece(cx(fc), fy, fwho == 1 ? YEL : RED, 1, 0);

        // hover preview + landing ghost
        if (hc >= 0) {
            float tx = cx(hc);
            previewX = previewX < 0 ? tx : previewX + (tx - previewX) * (1f - (float) Math.exp(-Gx.dt * 22));
            float bob = (float) Math.sin(clock * 5) * 1.2f;
            Gx.glow(X(previewX), Y(TOPY), S(26), Gx.withAlpha(YEL, 0.25f));
            piece(previewX, TOPY + bob, YEL, 1, 0);
            int lr = openRow(g, hc);
            float pa = 0.45f + 0.25f * (float) Math.sin(clock * 6);
            Gx.ringAt(X(tx), Y(cy(lr)), S(PR - 1), Math.max(1, S(1.4f)), Gx.withAlpha(YEL, pa * colHover[hc]));
        } else previewX = -1;

        // side panel
        act1 += ((turn == 1 && result == NONE ? 1 : 0) - act1) * k;
        act2 += ((turn == 2 && result == NONE ? 1 : 0) - act2) * k;
        playerCard(8, "Du", "Gelb", YEL, act1);
        playerCard(72, oppName(), duel == null ? "Rot" : "Online · Rot", RED, act2);

        String status;
        int sc = Style.text;
        if (result == WIN) { status = "Gewonnen!"; sc = Style.ok; }
        else if (result == LOSS) { status = "Verloren"; sc = Style.danger; }
        else if (result == DRAW) { status = "Unentschieden"; sc = Style.sub; }
        else if (turn == 2) status = (duel == null ? "KI denkt" : duel.oppName + " ist dran") + ".".repeat(1 + (int) (clock * 4) % 3);
        else status = "Du bist dran";
        float sy = 152;
        text(status, PX + PW / 2, sy, 9.5f, Fonts.BOLD, sc);
        text(result == NONE ? "Freie Felder: " + empty() : "Runde beendet", PX + PW / 2, sy + 14, 6.6f, Fonts.REGULAR, Style.sub);

        float ty = 180;
        box(PX, ty, PW, 60, 12, Style.surface);
        tally(PX + PW / 6f, ty, wins, "Siege", Style.ok);
        tally(PX + PW / 2f, ty, draws, "Remis", Style.sub);
        tally(PX + PW * 5 / 6f, ty, losses, "Niederl.", Style.danger);
        text("Tasten 1–7 werfen", PX + PW / 2, ty + 74, 6.2f, Fonts.MEDIUM, Style.muted);
    }

    private boolean inWin(int r, int c) {
        if (win4 == null) return false;
        for (int[] p : win4) if (p[0] == r && p[1] == c) return true;
        return false;
    }

    private void playerCard(float y, String name, String sub, int pc, float act) {
        float h = 56;
        if (act > 0.02f) Gx.glow(X(PX + PW / 2), Y(y + h / 2), S(56), Gx.withAlpha(pc, 0.14f * act));
        box(PX, y, PW, h, 12, Gx.mix(Style.surface, pc, 0.09f * act));
        Gx.outline(X(PX), Y(y), S(PW), S(h), S(12), Math.max(1, S(0.8f)), Gx.mix(Style.stroke, Gx.withAlpha(pc, 0.85f), act));
        piece(PX + 24, y + h / 2, pc, 0.85f, 0);
        Gx.textMid(Fonts.ellipsize(name, Fonts.BOLD, 10 * u, S(PW - 46 - 4)), X(PX + 46), Y(y + 22), 10 * u, Fonts.BOLD, Style.text);
        Gx.textMid(sub, X(PX + 46), Y(y + 36), 6.4f * u, Fonts.REGULAR, Style.sub);
        if (act > 0.02f) {
            float pulse = 0.6f + 0.4f * (float) Math.sin(clock * 6);
            circle(PX + PW - 10, y + 10, 2.6f, Gx.withAlpha(pc, act * pulse));
        }
    }

    private void tally(float cx, float y, int n, String label, int col) {
        text(String.valueOf(n), cx, y + 24, 13, Fonts.BOLD, Style.text);
        text(label, cx, y + 43, 5.4f, Fonts.MEDIUM, col);
    }
}
