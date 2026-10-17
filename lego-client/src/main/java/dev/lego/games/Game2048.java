package dev.lego.games;

import java.util.ArrayList;
import java.util.List;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** 2048 with Lego tiles: slide, merge, reach 2048 (and beyond). */
public final class Game2048 extends GameView {
    private static final int N = 4;
    private static final float GX = 12, GY = 12, GRID = 256, GAP = 8;
    private static final float CS = (GRID - GAP * (N + 1)) / N;
    private static final float MOVE_T = 0.12f;

    /** A tile: value, target cell, previous cell, animation timers. */
    private static final class Tile {
        int v, r, c;
        float fr, fc;           // previous position (animation start)
        float move = 1;         // 0..1 slide progress
        float pop = 1;          // 0..1 pop-in progress
        boolean merged, dying;
        Tile(int v, int r, int c) { this.v = v; this.r = r; this.c = c; this.fr = r; this.fc = c; }
    }

    private Tile[][] grid = new Tile[N][N];
    private final List<Tile> ghosts = new ArrayList<>();      // tiles sliding into a merge (removed after the slide)
    private final List<float[]> floats = new ArrayList<>();   // score popups: value, t
    private int moves, best;
    private boolean won;
    private float winT = -1, overDelay = -1, shake;

    public Game2048() { super("2048"); }

    @Override protected float boardW() { return 364; }
    @Override protected float boardH() { return 280; }
    @Override protected String overTitle() { return "Keine Züge mehr"; }

    @Override
    protected void reset() {
        grid = new Tile[N][N];
        ghosts.clear();
        floats.clear();
        moves = 0;
        best = 0;
        won = false;
        winT = -1;
        overDelay = -1;
        shake = 0;
        spawn();
        spawn();
    }

    private void spawn() {
        List<int[]> free = new ArrayList<>();
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) if (grid[r][c] == null) free.add(new int[]{r, c});
        if (free.isEmpty()) return;
        int[] p = free.get(rnd.nextInt(free.size()));
        Tile t = new Tile(rnd.nextFloat() < 0.9f ? 2 : 4, p[0], p[1]);
        t.pop = -0.6f;   // waits for the slide to finish before popping in
        grid[p[0]][p[1]] = t;
        best = Math.max(best, t.v);
    }

    @Override
    protected void onKey(int k) {
        if (overDelay >= 0) return;
        int dr = 0, dc = 0;
        if (k == K_LEFT || k == K_A) dc = -1;
        else if (k == K_RIGHT || k == K_D) dc = 1;
        else if (k == K_UP || k == K_W) dr = -1;
        else if (k == K_DOWN || k == K_S) dr = 1;
        else return;
        move(dr, dc);
    }

    private void finishAnims() {
        ghosts.clear();
        for (Tile[] row : grid) for (Tile t : row) if (t != null) { t.move = 1; t.pop = Math.max(t.pop, 1); }
    }

    private void move(int dr, int dc) {
        finishAnims();
        boolean moved = false;
        int gained = 0;
        for (Tile[] row : grid) for (Tile t : row) if (t != null) { t.fr = t.r; t.fc = t.c; t.merged = false; }
        for (int line = 0; line < N; line++) {
            int target = 0;
            Tile last = null;
            for (int i = 0; i < N; i++) {
                // walk each line starting at the side we move towards
                int idx = (dr > 0 || dc > 0) ? N - 1 - i : i;
                int r = dr != 0 ? idx : line, c = dc != 0 ? idx : line;
                Tile t = grid[r][c];
                if (t == null) continue;
                grid[r][c] = null;
                if (last != null && last.v == t.v && !last.merged) {
                    // merge t into last
                    last.v *= 2;
                    last.merged = true;
                    last.pop = -0.2f;
                    gained += last.v;
                    best = Math.max(best, last.v);
                    t.r = last.r;
                    t.c = last.c;
                    t.dying = true;
                    t.move = 0;
                    ghosts.add(t);
                    moved = true;
                    continue;
                }
                int tIdx = (dr > 0 || dc > 0) ? N - 1 - target : target;
                int nr = dr != 0 ? tIdx : line, nc = dc != 0 ? tIdx : line;
                if (nr != r || nc != c) moved = true;
                t.r = nr;
                t.c = nc;
                t.move = 0;
                grid[nr][nc] = t;
                last = t;
                target++;
            }
        }
        if (!moved) {
            shake = 1;
            for (Tile[] row : grid) for (Tile t : row) if (t != null) t.move = 1;
            return;
        }
        moves++;
        score += gained;
        if (gained > 0) {
            floats.add(new float[]{gained, 0});
            Sound.play("minecraft:block.note_block.pling", 0.9f + Math.min(1f, (float) (Math.log(gained) / Math.log(2)) / 11f), 0.25f);
        } else Sound.play("minecraft:block.wood.hit", 1.4f, 0.18f);
        spawn();
        if (!won && best >= 2048) { won = true; winT = 0; Sound.play("minecraft:ui.toast.challenge_complete", 1.1f, 0.35f); }
        if (!canMove()) overDelay = 0;
    }

    private boolean canMove() {
        for (int r = 0; r < N; r++)
            for (int c = 0; c < N; c++) {
                Tile t = grid[r][c];
                if (t == null) return true;
                if (c + 1 < N && grid[r][c + 1] != null && grid[r][c + 1].v == t.v) return true;
                if (r + 1 < N && grid[r + 1][c] != null && grid[r + 1][c].v == t.v) return true;
            }
        return false;
    }

    @Override
    protected void update(float dt) {
        if (winT >= 0) winT += dt;
        if (winT > 3.2f) winT = -1;
        for (float[] f : floats) f[1] += dt;
        floats.removeIf(f -> f[1] > 0.9f);
        if (overDelay >= 0) {
            overDelay += dt;
            if (overDelay > 0.7f) gameOver();
        }
    }

    // ------------------------------------------------------------------ drawing
    private static int tileColor(int v) {
        switch (v) {
            case 2: return 0xFFE4DDCB;
            case 4: return 0xFFF2CD37;
            case 8: return 0xFFFE8A18;
            case 16: return 0xFFE8562B;
            case 32: return 0xFFC91A09;
            case 64: return 0xFFD05098;
            case 128: return 0xFF923978;
            case 256: return 0xFF0055BF;
            case 512: return 0xFF36AEBF;
            case 1024: return 0xFF4B9F4A;
            case 2048: return 0xFFFFC53D;
            default: return 0xFF2B3440;
        }
    }

    private float cellX(float c) { return GX + GAP + c * (CS + GAP); }
    private float cellY(float r) { return GY + GAP + r * (CS + GAP); }

    private void drawTile(Tile t) {
        float mt = Ease.outCubic(t.move);
        float r = t.fr + (t.r - t.fr) * mt, c = t.fc + (t.c - t.fc) * mt;
        float x = cellX(c), y = cellY(r);
        float sc = 1;
        if (t.pop < 1) {
            float p = Math.max(0, t.pop);
            sc = t.merged ? 1 + 0.18f * (float) Math.sin(p * Math.PI) : Ease.outBack(p);
            if (!t.merged && t.pop <= 0) return;
        }
        int col = tileColor(t.v);
        boolean dark = Style.brightness(col) > 0.6;
        int txt = dark ? 0xFF3A3326 : 0xFFFFFFFF;
        Gx.push();
        Gx.scaleAt(X(x + CS / 2), Y(y + CS / 2), sc);
        if (t.v >= 128) Gx.glow(X(x + CS / 2), Y(y + CS / 2), S(CS * (t.v >= 2048 ? 1.1f : 0.8f)), Gx.withAlpha(col, t.v >= 2048 ? 0.55f : 0.25f));
        // brick body
        Gx.shadow(X(x), Y(y + 1.5f), S(CS), S(CS), S(8), S(3), 0.35f);
        boxV(x, y, CS, CS, 8, Gx.mix(col, 0xFFFFFFFF, 0.16f), Gx.mix(col, 0xFF000000, 0.14f));
        Gx.outline(X(x), Y(y), S(CS), S(CS), S(8), Math.max(1, S(0.9f)), Gx.withAlpha(Gx.mix(col, 0xFF000000, 0.5f), 0.6f));
        // four corner studs
        float sr = 4.2f, so = 10.5f;
        for (int i = 0; i < 4; i++) {
            float sx = i % 2 == 0 ? x + so : x + CS - so, sy = i < 2 ? y + so : y + CS - so;
            circle(sx, sy + 0.8f, sr, Gx.mix(col, 0xFF000000, 0.2f));
            circle(sx, sy, sr * 0.88f, Gx.mix(col, 0xFFFFFFFF, 0.2f));
        }
        String s = String.valueOf(t.v);
        float size = s.length() <= 2 ? 22 : s.length() == 3 ? 19 : s.length() == 4 ? 15.5f : 12.5f;
        text(s, x + CS / 2 + 0.4f, y + CS / 2 + 1.2f, size, Fonts.BOLD, Gx.withAlpha(0xFF000000, dark ? 0.08f : 0.25f));
        text(s, x + CS / 2, y + CS / 2, size, Fonts.BOLD, txt);
        Gx.pop();
    }

    @Override
    protected void render() {
        float dt = Math.min(0.05f, Gx.dt);
        // advance animations in real time (so they also finish in the overlays)
        for (Tile g : ghosts) g.move = Math.min(1, g.move + dt / MOVE_T);
        ghosts.removeIf(g -> g.move >= 1);
        for (Tile[] row : grid)
            for (Tile t : row)
                if (t != null) {
                    t.move = Math.min(1, t.move + dt / MOVE_T);
                    if (t.pop < 1) t.pop = Math.min(1, t.pop + dt / 0.16f);
                }
        shake = Math.max(0, shake - dt * 4);

        float sx = shake > 0 ? (float) Math.sin(shake * 30) * 2.5f * shake : 0;
        Gx.push();
        Gx.translate(S(sx), 0);
        // grid background
        Gx.shadow(X(GX), Y(GY), S(GRID), S(GRID), S(12), S(6), 0.4f);
        boxV(GX, GY, GRID, GRID, 12, Style.light ? 0xFFD3D6DD : 0xFF1A1D25, Style.light ? 0xFFC8CCD4 : 0xFF14161C);
        for (int r = 0; r < N; r++)
            for (int c = 0; c < N; c++) {
                float x = cellX(c), y = cellY(r);
                box(x, y, CS, CS, 8, Style.light ? 0x16000000 : 0x0CFFFFFF);
                for (int i = 0; i < 4; i++) {
                    float px = i % 2 == 0 ? x + 10.5f : x + CS - 10.5f, py = i < 2 ? y + 10.5f : y + CS - 10.5f;
                    circle(px, py, 3.4f, Style.light ? 0x10000000 : 0x08FFFFFF);
                }
            }
        for (Tile g : ghosts) drawTile(g);
        for (Tile[] row : grid) for (Tile t : row) if (t != null && !t.merged) drawTile(t);
        for (Tile[] row : grid) for (Tile t : row) if (t != null && t.merged) drawTile(t);
        Gx.pop();

        // side panel
        float px = GX + GRID + 12, pw = boardW() - px - 12;
        panel(px, GY, pw, 58, "HÖCHSTER STEIN", String.valueOf(best), tileColor(best));
        panel(px, GY + 66, pw, 58, "ZÜGE", String.valueOf(moves), Style.accent);
        // score popups
        for (float[] f : floats) {
            float a = 1 - Ease.outCubic(f[1] / 0.9f);
            text("+" + (int) f[0], px + pw / 2, GY + 150 - f[1] * 26, 12, Fonts.BOLD, Gx.withAlpha(Style.accent, a));
        }
        // goal hint
        float gy = GY + GRID - 76;
        box(px, gy, pw, 76, 10, Style.light ? 0x0C000000 : 0x0AFFFFFF);
        text("ZIEL", px + pw / 2, gy + 14, 6.5f, Fonts.BOLD, Style.muted);
        float bs = 34;
        float bxp = px + (pw - bs) / 2, byp = gy + 24;
        boxV(bxp, byp, bs, bs, 6, Gx.mix(0xFFFFC53D, 0xFFFFFFFF, 0.16f), Gx.mix(0xFFFFC53D, 0xFF000000, 0.14f));
        text("2048", bxp + bs / 2, byp + bs / 2, 8.5f, Fonts.BOLD, 0xFF3A3326);
        if (won) text("geschafft!", px + pw / 2, gy + 67, 6.5f, Fonts.BOLD, 0xFFFFC53D);

        // 2048 celebration
        if (winT >= 0) {
            float a = winT < 2.6f ? 1 : 1 - (winT - 2.6f) / 0.6f;
            float s = Ease.outBack(Math.min(1, winT / 0.45f));
            float cx = GX + GRID / 2, cy = GY + GRID / 2;
            Gx.pushAlpha(a);
            box(GX, GY, GRID, GRID, 12, Gx.rgba(0x000000, 0.6f));
            Gx.glow(X(cx), Y(cy), S(110), 0x66FFC53D);
            Gx.push();
            Gx.scaleAt(X(cx), Y(cy), s);
            text("2048!", cx + 1, cy - 4, 40, Fonts.BOLD, 0x66000000);
            text("2048!", cx, cy - 6, 40, Fonts.BOLD, 0xFFFFC53D);
            text("Weiter geht's!", cx, cy + 26, 10, Fonts.MEDIUM, 0xFFFFFFFF);
            Gx.pop();
            for (int i = 0; i < 18; i++) {
                double ang = i / 18.0 * Math.PI * 2 + winT;
                float d = 40 + winT * 60;
                circle(cx + (float) Math.cos(ang) * d, cy + (float) Math.sin(ang) * d, 2.5f, Gx.withAlpha(tileColor(2 << (i % 10)), 1 - winT / 3.2f));
            }
            Gx.popAlpha();
        }
    }

    private void panel(float x, float y, float w, float h, String label, String value, int col) {
        box(x, y, w, h, 10, Style.light ? 0x0C000000 : 0x0AFFFFFF);
        box(x, y + 12, 3, h - 24, 1.5f, col);
        text(label, x + w / 2, y + 16, 6.5f, Fonts.BOLD, Style.muted);
        text(value, x + w / 2, y + 38, 16, Fonts.BOLD, Style.text);
    }
}
