package dev.lego.games;

import java.util.ArrayDeque;
import java.util.Random;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Sudoku with four difficulty levels (unique solutions), notes mode, live validation, timer, undo and hints. */
public final class Sudoku extends GameView {
    private static final float W = 400, H = 300, CS = 29, BG = 3, GX = 14, GY = 14, PX = 300, GS = 9 * CS + 2 * BG;
    private static final int SEL = 0xFF3FA9FF;
    private static final String[] DIFF = {"Leicht", "Mittel", "Schwer", "Experte"};
    private static final int[] CLUES = {40, 32, 27, 23};
    private static final int[] BASE = {1000, 2000, 3500, 5000};
    private static final int[] DIFF_COL = {0xFF4AD66D, 0xFF3FA9FF, 0xFFFFB02E, 0xFFFF4D5E};
    private static final int MAX_MISTAKES = 3;

    private final int[] sol = new int[81], cell = new int[81];
    private final boolean[] given = new boolean[81];
    private final int[] notes = new int[81];       // bit mask 1..9
    private final float[] pop = new float[81], wave = new float[81], wrongT = new float[81];
    private final ArrayDeque<int[]> undo = new ArrayDeque<>();
    private final GameFx fx = new GameFx();
    private int diff = -1, sel = 40, mistakes, hints;
    private float elapsed, anim, winT = -1;
    private boolean noteMode, lost;

    public Sudoku() { super("sudoku"); }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String overTitle() { return lost ? "Zu viele Fehler" : "Gelöst!"; }
    @Override protected String scoreLabel() { return "Punkte"; }

    @Override
    protected void reset() {
        diff = -1;
        fx.clear();
        java.util.Arrays.fill(cell, 0);
        java.util.Arrays.fill(notes, 0);
        java.util.Arrays.fill(given, false);
        undo.clear();
        mistakes = 0; hints = 0; elapsed = 0; winT = -1; lost = false; noteMode = false; sel = 40;
    }

    private void newPuzzle(int d) {
        diff = d;
        Random r = new Random();
        java.util.Arrays.fill(sol, 0);
        fill(sol, 0, r);
        System.arraycopy(sol, 0, cell, 0, 81);
        int[] order = new int[81];
        for (int i = 0; i < 81; i++) order[i] = i;
        for (int i = 80; i > 0; i--) { int j = r.nextInt(i + 1); int t = order[i]; order[i] = order[j]; order[j] = t; }
        int clues = 81;
        int[] tmp = new int[81];
        for (int k = 0; k < 81 && clues > CLUES[d]; k++) {
            int i = order[k];
            int keep = cell[i];
            cell[i] = 0;
            System.arraycopy(cell, 0, tmp, 0, 81);
            if (count(tmp, 2) != 1) cell[i] = keep;
            else clues--;
        }
        for (int i = 0; i < 81; i++) { given[i] = cell[i] != 0; notes[i] = 0; pop[i] = 0; wave[i] = Ease.clamp(((i / 9) + (i % 9)) * 0.04f) * -1; wrongT[i] = 0; }
        undo.clear();
        mistakes = 0; hints = 0; elapsed = 0;
        sel = 40;
        Sound.play("minecraft:block.note_block.chime", 1.2f, 0.4f);
    }

    // ------------------------------------------------------------------ solver (bit masks, MRV)
    private static boolean fill(int[] g, int pos, Random r) {
        if (pos == 81) return true;
        if (g[pos] != 0) return fill(g, pos + 1, r);
        int[] d = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        for (int i = 8; i > 0; i--) { int j = r.nextInt(i + 1); int t = d[i]; d[i] = d[j]; d[j] = t; }
        for (int v : d) if (ok(g, pos, v)) { g[pos] = v; if (fill(g, pos + 1, r)) return true; g[pos] = 0; }
        return false;
    }

    private static boolean ok(int[] g, int pos, int v) {
        int r = pos / 9, c = pos % 9, br = r / 3 * 3, bc = c / 3 * 3;
        for (int k = 0; k < 9; k++) {
            if (g[r * 9 + k] == v || g[k * 9 + c] == v) return false;
            if (g[(br + k / 3) * 9 + bc + k % 3] == v) return false;
        }
        return true;
    }

    private static int cand(int[] g, int pos) {
        int r = pos / 9, c = pos % 9, br = r / 3 * 3, bc = c / 3 * 3, used = 0;
        for (int k = 0; k < 9; k++) used |= (1 << g[r * 9 + k]) | (1 << g[k * 9 + c]) | (1 << g[(br + k / 3) * 9 + bc + k % 3]);
        return ~used & 0x3FE;
    }

    /** Number of solutions (stops at limit). */
    private static int count(int[] g, int limit) {
        int best = -1, bm = 0, bc = 10;
        for (int i = 0; i < 81; i++) {
            if (g[i] != 0) continue;
            int m = cand(g, i), n = Integer.bitCount(m);
            if (n == 0) return 0;
            if (n < bc) { bc = n; best = i; bm = m; if (n == 1) break; }
        }
        if (best < 0) return 1;
        int total = 0;
        for (int v = 1; v <= 9; v++) {
            if ((bm & (1 << v)) == 0) continue;
            g[best] = v;
            total += count(g, limit - total);
            g[best] = 0;
            if (total >= limit) return total;
        }
        return total;
    }

    // ------------------------------------------------------------------ actions
    private void place(int v) {
        if (diff < 0 || winT >= 0 || given[sel]) return;
        if (noteMode && v != 0) {
            if (cell[sel] != 0) return;
            undo.push(new int[]{sel, cell[sel], notes[sel]});
            notes[sel] ^= 1 << v;
            pop[sel] = 0.6f;
            Sound.play("minecraft:block.note_block.hat", 1.8f, 0.15f);
            return;
        }
        if (cell[sel] == v) return;
        undo.push(new int[]{sel, cell[sel], notes[sel]});
        cell[sel] = v;
        notes[sel] = 0;
        if (v == 0) { Sound.play("minecraft:block.wood.hit", 1.5f, 0.2f); return; }
        pop[sel] = 1;
        if (v != sol[sel]) {
            mistakes++;
            wrongT[sel] = 1;
            fx.shake(0.35f);
            fx.popup("-100", cx(sel % 9), cy(sel / 9) - 8, 0xFFFF6B6B, 9);
            Sound.play("minecraft:block.note_block.bass", 0.6f, 0.5f);
            if (mistakes >= MAX_MISTAKES) { lost = true; winT = -1; gameOver(false); }
            return;
        }
        // clear notes of peers
        int r = sel / 9, c = sel % 9;
        for (int k = 0; k < 81; k++) if (k / 9 == r || k % 9 == c || (k / 27 == sel / 27 && (k % 9) / 3 == c / 3)) notes[k] &= ~(1 << v);
        Sound.play("minecraft:block.note_block.pling", 1.0f + v * 0.08f, 0.3f);
        checkUnits(sel);
        if (solved()) win();
    }

    private void checkUnits(int p) {
        int r = p / 9, c = p % 9;
        boolean row = true, col = true, box = true;
        for (int k = 0; k < 9; k++) {
            if (cell[r * 9 + k] != sol[r * 9 + k]) row = false;
            if (cell[k * 9 + c] != sol[k * 9 + c]) col = false;
            int bi = (r / 3 * 3 + k / 3) * 9 + c / 3 * 3 + k % 3;
            if (cell[bi] != sol[bi]) box = false;
        }
        int done = 0;
        for (int k = 0; k < 81; k++) {
            int kr = k / 9, kc = k % 9;
            boolean in = (row && kr == r) || (col && kc == c) || (box && kr / 3 == r / 3 && kc / 3 == c / 3);
            if (in) { wave[k] = -(Math.abs(kr - r) + Math.abs(kc - c)) * 0.035f; done++; }
        }
        if (done > 0) {
            fx.burst(cx(c), cy(r), 12, SEL, 90, 2, 0.5f, 0, GameFx.SPARK);
            Sound.play("minecraft:entity.experience_orb.pickup", 1.3f, 0.3f);
        }
        int v = cell[p], cnt = 0;
        for (int k = 0; k < 81; k++) if (cell[k] == v && sol[k] == v) cnt++;
        if (cnt == 9) fx.popup("Alle " + v + "er!", cx(c), cy(r) - 12, 0xFFF4C152, 9);
    }

    private boolean solved() { for (int i = 0; i < 81; i++) if (cell[i] != sol[i]) return false; return true; }

    private void win() {
        winT = 0;
        score = liveScore();
        for (int k = 0; k < 81; k++) wave[k] = -((k / 9) + (k % 9)) * 0.03f;
        for (int i = 0; i < 6; i++) fx.confetti(GX + 27 + i * 45, GY + 60 + (i % 2) * 110, 26, new int[]{0xFFE3263B, 0xFFF4C152, 0xFF1FA35C, 0xFF3FA9FF, 0xFFFFFFFF}, 190, 4);
        fx.shake(0.3f);
        Sound.play("minecraft:ui.toast.challenge_complete", 1.2f, 0.4f);
    }

    private void hint() {
        if (diff < 0 || winT >= 0) return;
        int target = -1;
        if (!given[sel] && cell[sel] != sol[sel]) target = sel;
        else for (int i = 0; i < 81; i++) if (cell[i] != sol[i]) { target = i; break; }
        if (target < 0) return;
        hints++;
        sel = target;
        boolean nm = noteMode;
        noteMode = false;
        int m = mistakes;
        place(sol[target]);
        mistakes = m;
        noteMode = nm;
        fx.popup("Tipp -150", cx(target % 9), cy(target / 9) - 10, 0xFF9FD8FF, 9);
    }

    private void doUndo() {
        if (undo.isEmpty() || winT >= 0) return;
        int[] u0 = undo.pop();
        cell[u0[0]] = u0[1];
        notes[u0[0]] = u0[2];
        sel = u0[0];
        pop[sel] = 0.5f;
        Sound.play("minecraft:block.wood.hit", 1.2f, 0.2f);
    }

    private long liveScore() {
        if (diff < 0) return 0;
        return Math.max(100, BASE[diff] - (long) (elapsed * 2) - mistakes * 100L - hints * 150L);
    }

    // ------------------------------------------------------------------ input
    @Override
    protected void onKey(int k) {
        if (diff < 0) {
            if (k >= 49 && k <= 52) { newPuzzle(k - 49); }
            return;
        }
        int v = k >= 49 && k <= 57 ? k - 48 : k >= 321 && k <= 329 ? k - 320 : -1;
        if (v > 0) { place(v); return; }
        if (k == 48 || k == 320 || k == 259 || k == 261) place(0);
        else if (k == 78) noteMode = !noteMode;                 // N
        else if (k == 90 || k == 85) doUndo();                   // Z / U
        else if (k == 72) hint();                                // H
        else if (k == K_LEFT || k == K_A) sel = sel / 9 * 9 + (sel % 9 + 8) % 9;
        else if (k == K_RIGHT || k == K_D) sel = sel / 9 * 9 + (sel % 9 + 1) % 9;
        else if (k == K_UP || k == K_W) sel = (sel + 72) % 81;
        else if (k == K_DOWN || k == K_S) sel = (sel + 9) % 81;
    }

    @Override
    protected void onClick(float x, float y, int button) {
        if (diff < 0) {
            for (int d = 0; d < 4; d++) {
                float[] r = diffRect(d);
                if (x >= r[0] && x <= r[0] + r[2] && y >= r[1] && y <= r[1] + r[3]) { newPuzzle(d); return; }
            }
            return;
        }
        int hit = cellAt(x, y);
        if (hit >= 0) {
            sel = hit;
            if (button == 1) place(0);
            Sound.click();
            return;
        }
        int b = padAt(x, y);
        if (b >= 1 && b <= 9) place(b);
        else if (b == 10) noteMode = !noteMode;
        else if (b == 11) place(0);
        else if (b == 12) doUndo();
        else if (b == 13) hint();
    }

    private float[] diffRect(int d) { return new float[]{W / 2 - 150 + d * 76, 118, 70, 92}; }

    /** Pad button at board position: 1..9 digits, 10 notes, 11 erase, 12 undo, 13 hint; -1 none. */
    private int padAt(float x, float y) {
        for (int i = 0; i < 9; i++) {
            float bx0 = PX + (i % 3) * 30, by0 = 96 + (i / 3) * 30;
            if (x >= bx0 && x < bx0 + 27 && y >= by0 && y < by0 + 27) return i + 1;
        }
        for (int i = 0; i < 4; i++) {
            float bx0 = PX + (i % 2) * 45, by0 = 190 + (i / 2) * 27;
            if (x >= bx0 && x < bx0 + 42 && y >= by0 && y < by0 + 24) return 10 + i;
        }
        return -1;
    }

    // ------------------------------------------------------------------ logic
    @Override
    protected void update(float dt) {
        fx.update(dt);
        for (int i = 0; i < 81; i++) { pop[i] = Math.max(0, pop[i] - dt * 4); if (wave[i] < 1) wave[i] += dt * 1.6f; wrongT[i] = Math.max(0, wrongT[i] - dt * 2); }
        if (diff >= 0 && winT < 0) { elapsed += dt; score = liveScore(); }
        if (winT >= 0) {
            winT += dt;
            if (((int) (winT * 10)) % 3 == 0 && winT < 1.5f) fx.confetti(GX + rnd.nextFloat() * 270, GY + rnd.nextFloat() * 270, 3, new int[]{0xFFE3263B, 0xFFF4C152, 0xFF1FA35C, 0xFFFFFFFF}, 120, 3.5f);
            if (winT > 2.2f) gameOver();
        }
    }

    private static float colX(int c) { return GX + c * CS + (c / 3) * BG; }
    private static float rowY(int r) { return GY + r * CS + (r / 3) * BG; }
    private float cx(int c) { return colX(c) + CS / 2; }
    private float cy(int r) { return rowY(r) + CS / 2; }
    /** Cell index at board position or -1. */
    private static int cellAt(float x, float y) {
        for (int r = 0; r < 9; r++) {
            float ry = rowY(r);
            if (y < ry || y >= ry + CS) continue;
            for (int c = 0; c < 9; c++) { float rx = colX(c); if (x >= rx && x < rx + CS) return r * 9 + c; }
        }
        return -1;
    }

    // ------------------------------------------------------------------ drawing
    @Override
    protected void render() {
        float dt = Math.min(0.05f, Gx.dt);
        anim += dt;
        boolean lightT = Style.light;
        int bg0 = lightT ? 0xFFEFF2F8 : 0xFF0C1630, bg1 = lightT ? 0xFFE2E7F1 : 0xFF111E3E;
        boxV(0, 0, W, H, 0, bg0, bg1);
        // soft snowflakes in the background
        for (int i = 0; i < 16; i++) {
            float x = (i * 83.3f + anim * 4) % W, y = (i * 47.1f + anim * (5 + i % 4)) % H;
            circle(x, y, 1 + (i % 3) * 0.5f, lightT ? 0x14000000 : 0x18FFFFFF);
        }
        if (diff < 0 && state != State.READY) { chooser(); return; }
        if (diff < 0) { gridPreview(); return; }

        float mbx = (mx - bx) / u, mby = (my - by) / u;
        Gx.push();
        Gx.translate(S(fx.sx), S(fx.sy));
        int text = lightT ? 0xFF15161A : 0xFFF3F4F6, cellBg = lightT ? 0xFFFFFFFF : 0xFF16244A, accent = SEL;
        int userCol = lightT ? 0xFF1D5FD0 : 0xFF8FC8FF;
        // grid background
        Gx.shadow(X(GX), Y(GY), S(GS), S(GS), S(6), S(6), 0.4f);
        box(GX - 2, GY - 2, GS + 4, GS + 4, 6, lightT ? 0xFF2A3350 : 0xFF050A18);
        int sr = sel / 9, sc = sel % 9, sv = cell[sel];
        int hoverCell = cellAt(mbx, mby);
        for (int i = 0; i < 81; i++) {
            int r = i / 9, c = i % 9;
            float x = colX(c) + 0.5f, y = rowY(r) + 0.5f, w = CS - 1, h = CS - 1;
            int bgc = cellBg;
            boolean peer = r == sr || c == sc || (r / 3 == sr / 3 && c / 3 == sc / 3);
            if (peer) bgc = Gx.mix(bgc, accent, 0.1f);
            if (sv != 0 && cell[i] == sv) bgc = Gx.mix(cellBg, accent, 0.28f);
            if (i == sel) bgc = Gx.mix(cellBg, accent, 0.45f);
            else if (i == hoverCell) bgc = Gx.mix(bgc, lightT ? 0xFF000000 : 0xFFFFFFFF, 0.06f);
            float wv = wave[i];
            if (wv > 0 && wv < 1) bgc = Gx.mix(bgc, 0xFFF4C152, (float) Math.sin(wv * Math.PI) * 0.55f);
            if (wrongT[i] > 0) bgc = Gx.mix(bgc, 0xFFFF4D5E, wrongT[i] * 0.5f);
            box(x, y, w, h, 2.2f, bgc);
            int v = cell[i];
            if (v != 0) {
                boolean wrong = !given[i] && v != sol[i];
                float s = 1 + 0.35f * Ease.outCubic(pop[i]) + (wv > 0 && wv < 1 ? 0.18f * (float) Math.sin(wv * Math.PI) : 0);
                int col = given[i] ? text : wrong ? 0xFFFF5A6A : userCol;
                Gx.push();
                Gx.scaleAt(X(x + w / 2), Y(y + h / 2), s);
                text(String.valueOf(v), x + w / 2, y + h / 2, given[i] ? 15 : 14.5f, given[i] ? Fonts.BOLD : Fonts.MEDIUM, col);
                Gx.pop();
            } else if (notes[i] != 0) {
                for (int n = 1; n <= 9; n++) {
                    if ((notes[i] & (1 << n)) == 0) continue;
                    float nx = x + w * ((n - 1) % 3 + 0.5f) / 3f, ny = y + h * ((n - 1) / 3 + 0.5f) / 3f;
                    boolean hl = n == sv;
                    if (hl) circle(nx, ny, 3.8f, Gx.withAlpha(accent, 0.35f));
                    text(String.valueOf(n), nx, ny, 6.2f, Fonts.MEDIUM, hl ? text : (lightT ? 0xFF7A8090 : 0xFF8C9AB8));
                }
            }
        }
        // selection frame
        float selX = colX(sc), selY = rowY(sr);
        float ax = Ease.to("sd_selx", selX, 22), ay = Ease.to("sd_sely", selY, 22);
        Gx.outline(X(ax), Y(ay), S(CS), S(CS), S(3), Math.max(1, S(1.4f)), accent);
        fx.draw(this, 0, 0);
        Gx.pop();

        // side panel
        int dc = DIFF_COL[diff];
        box(PX, 14, 87, 20, 10, Gx.withAlpha(dc, 0.2f));
        text(DIFF[diff], PX + 43.5f, 24, 8, Fonts.BOLD, dc);
        int m = (int) elapsed / 60, s = (int) elapsed % 60;
        Gx.icon("clock", X(PX + 8), Y(47), 9 * u, Style.sub);
        Gx.textMid(String.format("%d:%02d", m, s), X(PX + 17), Y(47), 10 * u, Fonts.BOLD, text);
        for (int i = 0; i < MAX_MISTAKES; i++) {
            boolean used = i < mistakes;
            Gx.icon(used ? "x" : "heart-fill", X(PX + 58 + i * 11), Y(47), 8 * u, used ? 0xFFFF4D5E : Gx.withAlpha(0xFFFF4D6D, 0.9f));
        }
        text("Fehler " + mistakes + "/" + MAX_MISTAKES + "  •  Tipps " + hints, PX + 43.5f, 64, 6, Fonts.MEDIUM, Style.sub);
        // progress
        int filled = 0;
        for (int i = 0; i < 81; i++) if (cell[i] == sol[i]) filled++;
        box(PX, 74, 87, 4, 2, lightT ? 0x22000000 : 0x22FFFFFF);
        box(PX, 74, 87 * filled / 81f, 4, 2, dc);
        int hover = padAt(mbx, mby);
        for (int i = 0; i < 9; i++) {
            int v = i + 1, cnt = 0;
            for (int k = 0; k < 81; k++) if (cell[k] == v && sol[k] == v) cnt++;
            float bx0 = PX + (i % 3) * 30, by0 = 96 + (i / 3) * 30;
            boolean done = cnt >= 9, hv = hover == v;
            int bgc = done ? (lightT ? 0x10000000 : 0x10FFFFFF) : hv ? Gx.mix(cellBg, accent, 0.35f) : cellBg;
            box(bx0, by0, 27, 27, 6, bgc);
            if (noteMode && !done) Gx.outline(X(bx0), Y(by0), S(27), S(27), S(6), Math.max(1, S(0.8f)), Gx.withAlpha(accent, 0.6f));
            text(String.valueOf(v), bx0 + 13.5f, by0 + 12.5f, noteMode ? 10 : 13, Fonts.BOLD, done ? Style.muted : (noteMode ? Style.sub : text));
            if (!done) text(String.valueOf(9 - cnt), bx0 + 22, by0 + 22, 5, Fonts.MEDIUM, Style.muted);
        }
        String[] lab = {"Notizen", "Radieren", "Zurück", "Tipp"};
        String[] ico = {"wand", "trash", "reset", "bolt"};
        for (int i = 0; i < 4; i++) {
            float bx0 = PX + (i % 2) * 45, by0 = 190 + (i / 2) * 27;
            boolean on = i == 0 && noteMode, hv = hover == 10 + i;
            box(bx0, by0, 42, 24, 6, on ? SEL : hv ? Gx.mix(cellBg, accent, 0.3f) : cellBg);
            Gx.icon(ico[i], X(bx0 + 21), Y(by0 + 9), 8.5f * u, on ? 0xFFFFFFFF : Style.sub);
            text(lab[i] + (i == 0 ? (noteMode ? " an" : " aus") : ""), bx0 + 21, by0 + 18.5f, 5, Fonts.BOLD, on ? 0xFFFFFFFF : Style.sub);
        }
        text("1-9  •  N Notizen  •  H Tipp", PX + 43.5f, 256, 5.2f, Fonts.MEDIUM, Style.muted);
        text("Z Zurück  •  Entf Radieren", PX + 43.5f, 265, 5.2f, Fonts.MEDIUM, Style.muted);
        if (winT >= 0) {
            float t = Ease.outBack(Math.min(1, winT / 0.4f));
            Gx.push();
            Gx.scaleAt(X(GX + GS / 2), Y(GY + GS / 2), t);
            box(GX + GS / 2 - 70, GY + GS / 2 - 22, 140, 44, 14, Gx.withAlpha(0xFF0B1428, 0.92f));
            text("Gelöst!", GX + GS / 2, GY + GS / 2 - 5, 15, Fonts.BOLD, 0xFFF4C152);
            text(liveScore() + " Punkte", GX + GS / 2, GY + GS / 2 + 11, 7.5f, Fonts.MEDIUM, 0xFFFFFFFF);
            Gx.pop();
        }
        fx.drawPopups(this, 0, 0);
    }

    /** Decorative grid shown behind the READY overlay. */
    private void gridPreview() {
        String demo = "530070000600195000098000060800060003400803001700020006060000280000419005000080079";
        box(GX - 2, GY - 2, GS + 4, GS + 4, 6, Style.light ? 0xFF2A3350 : 0xFF050A18);
        for (int i = 0; i < 81; i++) {
            int r = i / 9, c = i % 9;
            float x = colX(c) + 0.5f, y = rowY(r) + 0.5f;
            box(x, y, CS - 1, CS - 1, 2.2f, Style.light ? 0xFFFFFFFF : 0xFF16244A);
            char ch = demo.charAt(i);
            if (ch != '0') text(String.valueOf(ch), x + CS / 2 - 0.5f, y + CS / 2 - 0.5f, 14, Fonts.BOLD, Style.light ? 0xFF15161A : 0xFFF3F4F6);
        }
    }

    private void chooser() {
        text("Wähle den Schwierigkeitsgrad", W / 2, 78, 13, Fonts.BOLD, Style.text);
        text("Jedes Rätsel hat genau eine Lösung", W / 2, 96, 7.5f, Fonts.REGULAR, Style.sub);
        float mbx = (mx - bx) / u, mby = (my - by) / u;
        for (int d = 0; d < 4; d++) {
            float[] r = diffRect(d);
            boolean hv = mbx >= r[0] && mbx <= r[0] + r[2] && mby >= r[1] && mby <= r[1] + r[3];
            float h = Ease.to("sd_d" + d, hv ? 1 : 0, 14);
            float lift = h * 3;
            int col = DIFF_COL[d];
            Gx.shadow(X(r[0]), Y(r[1] - lift), S(r[2]), S(r[3]), S(10), S(4 + h * 4), 0.3f + h * 0.2f);
            boxV(r[0], r[1] - lift, r[2], r[3], 10, Gx.mix(Style.surface, col, 0.18f + 0.15f * h), Gx.mix(Style.surface, col, 0.05f));
            Gx.outline(X(r[0]), Y(r[1] - lift), S(r[2]), S(r[3]), S(10), Math.max(1, S(0.8f)), Gx.withAlpha(col, 0.4f + 0.5f * h));
            for (int k = 0; k <= d; k++) Gx.icon("star-fill", X(r[0] + r[2] / 2 + (k - d / 2f) * 9), Y(r[1] - lift + 22), 8 * u, col);
            text(DIFF[d], r[0] + r[2] / 2, r[1] - lift + 44, 9.5f, Fonts.BOLD, Style.text);
            text(CLUES[d] + " Vorgaben", r[0] + r[2] / 2, r[1] - lift + 59, 6.2f, Fonts.MEDIUM, Style.sub);
            text("bis " + BASE[d] + " P.", r[0] + r[2] / 2, r[1] - lift + 72, 6.2f, Fonts.MEDIUM, col);
            text(String.valueOf(d + 1), r[0] + r[2] - 8, r[1] - lift + 9, 5.5f, Fonts.BOLD, Style.muted);
        }
        text("Klick oder Taste 1-4", W / 2, 232, 7, Fonts.MEDIUM, Style.muted);
    }
}
