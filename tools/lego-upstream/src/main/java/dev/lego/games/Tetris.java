package dev.lego.games;

import java.util.ArrayList;
import java.util.List;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Tetris with Lego bricks: 10x20 grid, hold, next preview, ghost piece, levels. */
public final class Tetris extends GameView {
    private static final int COLS = 10, ROWS = 20, K_C = 67;
    private static final float CELL = 12, GX = 130, GY = 14;
    private static final float LX = 12, RX = 262, PW = 106;
    private static final String[][] SHAPES = {
            {"....", "####", "....", "...."},   // I
            {"##", "##"},                       // O
            {".#.", "###", "..."},              // T
            {".##", "##.", "..."},              // S
            {"##.", ".##", "..."},              // Z
            {"#..", "###", "..."},              // J
            {"..#", "###", "..."},              // L
    };
    private static final int[] COLORS = {0xFF36AEBF, 0xFFF2CD37, 0xFF923978, 0xFF4B9F4A, 0xFFC91A09, 0xFF0055BF, 0xFFFE8A18};
    private static final int[][] KICKS = {{0, 0}, {-1, 0}, {1, 0}, {0, -1}, {-2, 0}, {2, 0}, {-1, -1}, {1, -1}};

    private int[][] grid = new int[ROWS][COLS];   // 0 = empty, else type + 1
    private int type, rot, px, py;
    private boolean active;
    private int hold = -1;
    private boolean canHold;
    private final List<Integer> bag = new ArrayList<>(), queue = new ArrayList<>();
    private int lines, level;
    private float fall, lockT;
    private int lockResets;
    private final List<Integer> clearing = new ArrayList<>();
    private float clearT = -1;
    private float visX, visY, shake, levelT = -1, holdPop;
    private String banner;
    private float bannerT = -1;
    private final List<float[]> parts = new ArrayList<>();   // x, y, vx, vy, t, color

    public Tetris() { super("tetris"); }

    @Override protected float boardW() { return 380; }
    @Override protected float boardH() { return 268; }

    @Override
    protected void reset() {
        grid = new int[ROWS][COLS];
        bag.clear();
        queue.clear();
        parts.clear();
        clearing.clear();
        clearT = -1;
        hold = -1;
        lines = 0;
        level = 1;
        fall = 0;
        shake = 0;
        levelT = -1;
        bannerT = -1;
        while (queue.size() < 4) queue.add(nextFromBag());
        spawn(queue.remove(0));
    }

    private int nextFromBag() {
        if (bag.isEmpty()) for (int i = 0; i < 7; i++) bag.add(rnd.nextInt(bag.size() + 1), i);
        return bag.remove(0);
    }

    private void spawn(int t) {
        type = t;
        rot = 0;
        int n = SHAPES[t].length;
        px = (COLS - n) / 2;
        py = t == 0 ? -1 : 0;
        active = true;
        canHold = true;
        fall = 0;
        lockT = 0;
        lockResets = 0;
        visX = px;
        visY = py - 1.5f;
        if (!fits(type, rot, px, py)) {
            active = false;
            gameOver();
        }
    }

    /** Cells {x, y} of a piece relative to its box. */
    private static int[][] cells(int t, int r) {
        String[] s = SHAPES[t];
        int n = s.length;
        int[][] out = new int[4][];
        int k = 0;
        for (int y = 0; y < n; y++)
            for (int x = 0; x < n; x++)
                if (s[y].charAt(x) == '#') {
                    int cx = x, cy = y;
                    for (int i = 0; i < (r & 3); i++) { int tx = n - 1 - cy; cy = cx; cx = tx; }
                    out[k++] = new int[]{cx, cy};
                }
        return out;
    }

    private boolean fits(int t, int r, int x, int y) {
        for (int[] c : cells(t, r)) {
            int gx = x + c[0], gy = y + c[1];
            if (gx < 0 || gx >= COLS || gy >= ROWS) return false;
            if (gy >= 0 && grid[gy][gx] != 0) return false;
        }
        return true;
    }

    private int ghostY() {
        int y = py;
        while (fits(type, rot, px, y + 1)) y++;
        return y;
    }

    private void touched() {
        if (!fits(type, rot, px, py + 1) && lockResets < 15) { lockT = 0; lockResets++; }
    }

    // ------------------------------------------------------------------ input
    @Override
    protected void onKey(int k) {
        if (!active || clearT >= 0) return;
        if (k == K_LEFT || k == K_A) {
            if (fits(type, rot, px - 1, py)) { px--; touched(); Sound.play("minecraft:block.wood.hit", 1.8f, 0.08f); }
        } else if (k == K_RIGHT || k == K_D) {
            if (fits(type, rot, px + 1, py)) { px++; touched(); Sound.play("minecraft:block.wood.hit", 1.8f, 0.08f); }
        } else if (k == K_UP || k == K_W || k == 88) {
            rotate(1);
        } else if (k == 90) {
            rotate(3);
        } else if (k == K_DOWN || k == K_S) {
            if (fits(type, rot, px, py + 1)) { py++; score += 1; fall = 0; }
        } else if (k == K_SPACE) {
            if (time < 0.15f) return;   // the SPACE that started the round
            int g = ghostY();
            score += 2L * (g - py);
            py = g;
            visY = py;
            shake = 0.6f;
            lock();
        } else if (k == K_C) {
            if (!canHold) return;
            int cur = type;
            if (hold < 0) { hold = cur; spawn(queue.remove(0)); queue.add(nextFromBag()); }
            else { int h = hold; hold = cur; spawn(h); }
            canHold = false;
            holdPop = 1;
            Sound.play("minecraft:item.armor.equip_leather", 1.3f, 0.3f);
        }
    }

    private void rotate(int dir) {
        if (type == 1) return;
        int nr = (rot + dir) & 3;
        for (int[] kk : KICKS) {
            int kx = kk[0], ky = kk[1];
            if (fits(type, nr, px + kx, py + ky)) {
                rot = nr;
                px += kx;
                py += ky;
                touched();
                Sound.play("minecraft:block.wood.place", 1.6f, 0.12f);
                return;
            }
        }
    }

    // ------------------------------------------------------------------ logic
    private float gravity() { return Math.max(0.045f, 0.8f * (float) Math.pow(0.82, level - 1)); }

    @Override
    protected void update(float dt) {
        for (float[] p : parts) { p[0] += p[2] * dt; p[1] += p[3] * dt; p[3] += 260 * dt; p[4] += dt; }
        parts.removeIf(p -> p[4] > 0.9f);
        if (levelT >= 0) { levelT += dt; if (levelT > 1.6f) levelT = -1; }
        if (bannerT >= 0) { bannerT += dt; if (bannerT > 1.3f) bannerT = -1; }
        if (clearT >= 0) {
            clearT += dt;
            if (clearT >= 0.34f) finishClear();
            return;
        }
        if (!active) return;
        if (fits(type, rot, px, py + 1)) {
            fall += dt;
            lockT = 0;
            if (fall >= gravity()) { fall -= gravity(); py++; }
        } else {
            lockT += dt;
            if (lockT >= 0.5f) lock();
        }
    }

    private void lock() {
        boolean above = false;
        for (int[] c : cells(type, rot)) {
            int gx = px + c[0], gy = py + c[1];
            if (gy < 0) { above = true; continue; }
            grid[gy][gx] = type + 1;
        }
        active = false;
        Sound.play("minecraft:block.stone.place", 1.2f, 0.25f);
        if (above) { gameOver(); return; }
        clearing.clear();
        for (int y = 0; y < ROWS; y++) {
            boolean full = true;
            for (int x = 0; x < COLS; x++) if (grid[y][x] == 0) { full = false; break; }
            if (full) clearing.add(y);
        }
        if (!clearing.isEmpty()) {
            clearT = 0;
            int n = clearing.size();
            Sound.play(n >= 4 ? "minecraft:ui.toast.challenge_complete" : "minecraft:entity.experience_orb.pickup", n >= 4 ? 1.3f : 0.9f + n * 0.15f, 0.3f);
            for (int y : clearing)
                for (int x = 0; x < COLS; x++)
                    for (int i = 0; i < 2; i++)
                        parts.add(new float[]{GX + x * CELL + CELL / 2, GY + y * CELL + CELL / 2, (rnd.nextFloat() - 0.5f) * 140, -40 - rnd.nextFloat() * 110, 0, COLORS[grid[y][x] - 1]});
            return;
        }
        next();
    }

    private void finishClear() {
        int n = clearing.size();
        for (int y : clearing) {
            for (int yy = y; yy > 0; yy--) grid[yy] = grid[yy - 1];
            grid[0] = new int[COLS];
        }
        clearing.clear();
        clearT = -1;
        score += (long) new int[]{0, 100, 300, 500, 800}[Math.min(4, n)] * level;
        banner = n == 1 ? null : n == 2 ? "Doppel!" : n == 3 ? "Dreifach!" : "TETRIS!";
        if (banner != null) bannerT = 0;
        int oldLevel = level;
        lines += n;
        level = 1 + lines / 10;
        if (level > oldLevel) { levelT = 0; Sound.play("minecraft:entity.player.levelup", 1.2f, 0.3f); }
        next();
    }

    private void next() {
        spawn(queue.remove(0));
        queue.add(nextFromBag());
    }

    // ------------------------------------------------------------------ drawing
    private void cell(float x, float y, float s, int col) {
        brick(x + 0.3f, y + 0.3f, s - 0.6f, s - 0.6f, col);
    }

    @Override
    protected void render() {
        float dt = Math.min(0.05f, Gx.dt);
        float k = 1 - (float) Math.exp(-dt * 28);
        visX += (px - visX) * k;
        visY += (py - visY) * k;
        if (Math.abs(px - visX) > 3) visX = px;
        shake = Math.max(0, shake - dt * 3);
        holdPop = Math.max(0, holdPop - dt * 4);

        float sy = shake > 0 ? Ease.outCubic(shake) * 2.2f : 0;
        // grid panel
        Gx.shadow(X(GX - 4), Y(GY - 4), S(COLS * CELL + 8), S(ROWS * CELL + 8), S(8), S(6), 0.4f);
        boxV(GX - 4, GY - 4, COLS * CELL + 8, ROWS * CELL + 8, 8, Style.light ? 0xFFD7DAE0 : 0xFF181B22, Style.light ? 0xFFCDD1D8 : 0xFF121419);
        Gx.push();
        Gx.translate(0, S(sy));
        for (int y = 0; y < ROWS; y++)
            for (int x = 0; x < COLS; x++)
                circle(GX + x * CELL + CELL / 2, GY + y * CELL + CELL / 2, 2.6f, Style.light ? 0x12000000 : 0x0AFFFFFF);

        // locked bricks
        for (int y = 0; y < ROWS; y++) {
            boolean clr = clearT >= 0 && clearing.contains(y);
            for (int x = 0; x < COLS; x++) {
                int v = grid[y][x];
                if (v == 0) continue;
                if (clr) {
                    float t = clearT / 0.34f;
                    float sc = 1 - Ease.inOutCubic(Math.max(0, (t - 0.35f) / 0.65f));
                    int col = Gx.mix(COLORS[v - 1], 0xFFFFFFFF, (float) Math.min(1, t * 3));
                    float cs = CELL * sc;
                    if (cs > 0.5f) cell(GX + x * CELL + (CELL - cs) / 2, GY + y * CELL + (CELL - cs) / 2, cs, col);
                } else cell(GX + x * CELL, GY + y * CELL, CELL, COLORS[v - 1]);
            }
            if (clr) {
                float a = 1 - clearT / 0.34f;
                Gx.glow(X(GX + COLS * CELL / 2), Y(GY + y * CELL + CELL / 2), S(70), Gx.withAlpha(0xFFFFFFFF, a * 0.35f));
            }
        }

        if (active && state != State.OVER) {
            // ghost
            int g = ghostY();
            for (int[] c : cells(type, rot)) {
                float x = GX + (px + c[0]) * CELL, y = GY + (g + c[1]) * CELL;
                if (g + c[1] < 0) continue;
                box(x + 0.8f, y + 0.8f, CELL - 1.6f, CELL - 1.6f, 2.5f, Gx.withAlpha(COLORS[type], 0.13f));
                Gx.outline(X(x + 0.8f), Y(y + 0.8f), S(CELL - 1.6f), S(CELL - 1.6f), S(2.5f), Math.max(1, S(0.7f)), Gx.withAlpha(COLORS[type], 0.55f));
            }
            // active piece (eased position)
            Gx.clip(X(GX - 4), Y(GY), S(COLS * CELL + 8), S(ROWS * CELL + 4));
            for (int[] c : cells(type, rot)) {
                float x = GX + (visX + c[0]) * CELL, y = GY + (visY + c[1]) * CELL;
                Gx.glow(X(x + CELL / 2), Y(y + CELL / 2), S(CELL * 1.1f), Gx.withAlpha(COLORS[type], 0.16f));
            }
            for (int[] c : cells(type, rot)) cell(GX + (visX + c[0]) * CELL, GY + (visY + c[1]) * CELL, CELL, COLORS[type]);
            Gx.unclip();
        }
        Gx.pop();

        // particles
        for (float[] p : parts) {
            float a = 1 - p[4] / 0.9f;
            float s = 2.6f * (1 - p[4] / 0.9f * 0.5f);
            box(p[0] - s / 2, p[1] - s / 2, s, s, 0.8f, Gx.withAlpha((int) p[5], a));
        }

        // left panel: hold, level, lines
        panelBox(LX, GY - 4, PW, 66, "HALTEN");
        if (hold >= 0) {
            float sc = 1 + 0.15f * holdPop;
            Gx.push();
            Gx.scaleAt(X(LX + PW / 2), Y(GY + 36), sc);
            Gx.pushAlpha(canHold ? 1 : 0.4f);
            mini(hold, LX + PW / 2, GY + 36, 10);
            Gx.popAlpha();
            Gx.pop();
        } else text("C drücken", LX + PW / 2, GY + 36, 6.5f, Fonts.MEDIUM, Style.muted);
        stat(LX, GY + 70, "LEVEL", String.valueOf(level), Style.accent);
        stat(LX, GY + 120, "REIHEN", String.valueOf(lines), 0xFF36AEBF);
        // progress to next level
        float prog = (lines % 10) / 10f;
        box(LX + 10, GY + 160, PW - 20, 3, 1.5f, Style.light ? 0x18000000 : 0x14FFFFFF);
        if (prog > 0) box(LX + 10, GY + 160, (PW - 20) * prog, 3, 1.5f, Style.accent);
        // controls help
        float hy = GY + 184;
        String[][] help = {{"chevron-left", "chevron-right", "bewegen"}, {"chevron-up", null, "drehen"}, {"chevron-down", null, "schneller"}, {"#Leer", null, "fallen lassen"}, {"#C", null, "halten"}};
        for (String[] h : help) {
            float kx = LX + 4;
            kx += keycap(kx, hy, h[0]) + 2;
            if (h[1] != null) kx += keycap(kx, hy, h[1]) + 2;
            Gx.textMid(h[2], X(LX + 42), Y(hy), 6.4f * u, Fonts.REGULAR, Style.sub);
            hy += 13;
        }

        // right panel: next pieces
        panelBox(RX, GY - 4, PW, 170, "NÄCHSTE");
        mini(queue.get(0), RX + PW / 2, GY + 38, 11);
        box(RX + 14, GY + 64, PW - 28, 0.8f, 0, Style.light ? 0x14000000 : 0x10FFFFFF);
        for (int i = 1; i < 3; i++) {
            Gx.pushAlpha(0.75f - (i - 1) * 0.2f);
            mini(queue.get(i), RX + PW / 2, GY + 42 + i * 40, 8);
            Gx.popAlpha();
        }
        // speed indicator
        panelBox(RX, GY + 174, PW, 62, "TEMPO");
        int bars = Math.min(10, level);
        for (int i = 0; i < 10; i++) {
            float bh = 4 + i * 1.8f;
            box(RX + 14 + i * 8f, GY + 226 - bh, 5f, bh, 1.5f, i < bars ? Gx.mix(0xFF4B9F4A, 0xFFC91A09, i / 9f) : (Style.light ? 0x16000000 : 0x12FFFFFF));
        }

        // banners
        if (bannerT >= 0 && banner != null) {
            float t = bannerT / 1.3f;
            float s = Ease.outBack(Math.min(1, bannerT / 0.3f));
            float a = t < 0.75f ? 1 : 1 - (t - 0.75f) / 0.25f;
            float cy = GY + ROWS * CELL * 0.4f - bannerT * 8;
            Gx.pushAlpha(a);
            Gx.push();
            Gx.scaleAt(X(GX + COLS * CELL / 2), Y(cy), s);
            Gx.glow(X(GX + COLS * CELL / 2), Y(cy), S(60), Gx.withAlpha(Style.accent, 0.35f));
            text(banner, GX + COLS * CELL / 2 + 0.6f, cy + 1, 16, Fonts.BOLD, 0x88000000);
            text(banner, GX + COLS * CELL / 2, cy, 16, Fonts.BOLD, 0xFFFFFFFF);
            Gx.pop();
            Gx.popAlpha();
        }
        if (levelT >= 0) {
            float t = levelT / 1.6f;
            float s = Ease.outBack(Math.min(1, levelT / 0.35f));
            float a = t < 0.7f ? 1 : 1 - (t - 0.7f) / 0.3f;
            float cy = GY + ROWS * CELL * 0.55f;
            Gx.pushAlpha(a);
            Gx.push();
            Gx.scaleAt(X(GX + COLS * CELL / 2), Y(cy), s);
            box(GX + 12, cy - 15, COLS * CELL - 24, 30, 10, Gx.withAlpha(Style.accent, 0.92f));
            text("Level " + level, GX + COLS * CELL / 2, cy, 13, Fonts.BOLD, Style.accentText);
            Gx.pop();
            Gx.popAlpha();
        }
    }

    /** Small key cap: icon name, or text when prefixed with '#'. Returns its width. */
    private float keycap(float x, float cy, String what) {
        boolean txt = what.startsWith("#");
        float w = txt ? Math.max(11, Gx.width(what.substring(1), 5.6f * u, Fonts.BOLD) / u + 7) : 11, h = 10.5f;
        box(x, cy - h / 2, w, h, 3, Style.light ? 0x18000000 : 0x18FFFFFF);
        box(x, cy + h / 2 - 1.2f, w, 1.2f, 0.6f, Style.light ? 0x14000000 : 0x10FFFFFF);
        if (txt) text(what.substring(1), x + w / 2, cy - 0.3f, 5.6f, Fonts.BOLD, Style.text);
        else Gx.icon(what, X(x + w / 2), Y(cy - 0.3f), 7 * u, Style.text);
        return w;
    }

    private void panelBox(float x, float y, float w, float h, String title) {
        box(x, y, w, h, 10, Style.light ? 0x0C000000 : 0x0AFFFFFF);
        text(title, x + w / 2, y + 11, 6.2f, Fonts.BOLD, Style.muted);
    }

    private void stat(float x, float y, String label, String value, int col) {
        box(x, y, PW, 44, 10, Style.light ? 0x0C000000 : 0x0AFFFFFF);
        box(x, y + 10, 3, 24, 1.5f, col);
        text(label, x + PW / 2, y + 12, 6.2f, Fonts.BOLD, Style.muted);
        text(value, x + PW / 2, y + 29, 14, Fonts.BOLD, Style.text);
    }

    /** Piece preview centered at (cx, cy). */
    private void mini(int t, float cx, float cy, float s) {
        int[][] cs = cells(t, 0);
        int minX = 9, maxX = -9, minY = 9, maxY = -9;
        for (int[] c : cs) { minX = Math.min(minX, c[0]); maxX = Math.max(maxX, c[0]); minY = Math.min(minY, c[1]); maxY = Math.max(maxY, c[1]); }
        float w = (maxX - minX + 1) * s, h = (maxY - minY + 1) * s;
        for (int[] c : cs) cell(cx - w / 2 + (c[0] - minX) * s, cy - h / 2 + (c[1] - minY) * s, s, COLORS[t]);
    }
}
