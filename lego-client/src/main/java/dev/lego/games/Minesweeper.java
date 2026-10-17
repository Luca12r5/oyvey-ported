package dev.lego.games;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Minesweeper with Lego bricks: the first click is always safe. */
public final class Minesweeper extends GameView {
    private static final int COLS = 12, ROWS = 10, MINES = 18, N = COLS * ROWS;
    private static final float CELL = 26, GX = 20, GY = 42;
    private static final int[] NUM = {0, 0xFF4DA3FF, 0xFF3DDC84, 0xFFFF5252, 0xFFB388FF, 0xFFFF8A65, 0xFF26C6DA, 0xFFE0E0E0, 0xFF9E9E9E};
    private static final int[] NUM_LIGHT = {0, 0xFF1565C0, 0xFF2E7D32, 0xFFD32F2F, 0xFF4527A0, 0xFF8D2A0E, 0xFF00838F, 0xFF212121, 0xFF616161};
    private static final int[] DEBRIS = {0xFFE3000B, 0xFFFFCD03, 0xFF0A5BC4, 0xFFA0A5A9, 0xFFFF8A00, 0xFF6C6E68};

    private final boolean[] mine = new boolean[N], open = new boolean[N], flag = new boolean[N];
    private final int[] adj = new int[N];
    private final float[] openAt = new float[N], flagAt = new float[N];
    private boolean generated, lost, won;
    private int flags, opened, boom = -1;
    private float startT, endSecs, endT, clock, boomAt;
    private final List<float[]> parts = new ArrayList<>();   // x, y, vx, vy, life, color, size

    public Minesweeper() { super("minesweeper"); }

    @Override protected float boardW() { return COLS * CELL + GX * 2; }
    @Override protected float boardH() { return GY + ROWS * CELL + 12; }

    @Override
    protected void reset() {
        for (int i = 0; i < N; i++) { mine[i] = open[i] = flag[i] = false; adj[i] = 0; openAt[i] = flagAt[i] = 0; }
        generated = lost = won = false;
        flags = opened = 0;
        boom = -1;
        startT = endSecs = endT = 0;
        parts.clear();
    }

    @Override protected String overTitle() { return won ? "Geschafft!" : "Verloren"; }

    private float secs() {
        if (!generated) return 0;
        if (won || lost) return endSecs;
        return Math.max(0, time - startT);
    }

    // ------------------------------------------------------------------ logic
    @Override
    protected void update(float dt) {
        if (won) { endT += dt; if (endT > 1.3f) gameOver(); }
        else if (lost) { endT += dt; if (endT > 1.8f) gameOver(false); }
    }

    private int cellAt(float x, float y) {
        int c = (int) Math.floor((x - GX) / CELL), r = (int) Math.floor((y - GY) / CELL);
        if (c < 0 || r < 0 || c >= COLS || r >= ROWS) return -1;
        return r * COLS + c;
    }

    private List<Integer> neighbours(int i) {
        List<Integer> l = new ArrayList<>(8);
        int c = i % COLS, r = i / COLS;
        for (int dy = -1; dy <= 1; dy++)
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int cc = c + dx, rr = r + dy;
                if (cc >= 0 && rr >= 0 && cc < COLS && rr < ROWS) l.add(rr * COLS + cc);
            }
        return l;
    }

    private void generate(int safe) {
        List<Integer> forbidden = neighbours(safe);
        forbidden.add(safe);
        int placed = 0;
        while (placed < MINES) {
            int i = rnd.nextInt(N);
            if (mine[i] || forbidden.contains(i)) continue;
            mine[i] = true;
            placed++;
        }
        for (int i = 0; i < N; i++) {
            int n = 0;
            for (int j : neighbours(i)) if (mine[j]) n++;
            adj[i] = n;
        }
        generated = true;
        startT = time;
    }

    @Override
    protected void onClick(float x, float y, int button) {
        if (won || lost) return;
        int i = cellAt(x, y);
        if (i < 0) return;
        if (button == 1) {
            if (open[i]) return;
            flag[i] = !flag[i];
            flags += flag[i] ? 1 : -1;
            flagAt[i] = clock;
            Sound.play("minecraft:block.note_block.hat", flag[i] ? 1.6f : 1.2f, 0.35f);
            return;
        }
        if (flag[i]) return;
        if (!generated) generate(i);
        if (open[i]) {
            // chording
            if (adj[i] == 0) return;
            int f = 0;
            for (int j : neighbours(i)) if (flag[j]) f++;
            if (f != adj[i]) {
                Sound.play("minecraft:block.note_block.bass", 1.4f, 0.2f);
                return;
            }
            boolean any = false;
            for (int j : neighbours(i)) {
                if (flag[j] || open[j]) continue;
                if (mine[j]) { explode(j); return; }
                reveal(j, false);
                any = true;
            }
            if (any) { Sound.play("minecraft:block.stone.break", 1.35f + rnd.nextFloat() * 0.2f, 0.25f); checkWin(); }
            return;
        }
        if (mine[i]) { explode(i); return; }
        int n = reveal(i, true);
        Sound.play(n > 6 ? "minecraft:block.stone.break" : "minecraft:block.stone.hit", n > 6 ? 1.1f : 1.5f + rnd.nextFloat() * 0.2f, n > 6 ? 0.3f : 0.4f);
        checkWin();
    }

    /** Opens a cell (flood fill for zeros) and returns the number of opened cells. */
    private int reveal(int start, boolean stagger) {
        ArrayDeque<int[]> q = new ArrayDeque<>();
        q.add(new int[]{start, 0});
        int count = 0;
        while (!q.isEmpty()) {
            int[] e = q.poll();
            int i = e[0];
            if (open[i] || flag[i] || mine[i]) continue;
            open[i] = true;
            opened++;
            count++;
            openAt[i] = clock + (stagger ? Math.min(0.5f, e[1] * 0.035f) : 0);
            if (adj[i] == 0) for (int j : neighbours(i)) if (!open[j] && !flag[j]) q.add(new int[]{j, e[1] + 1});
        }
        return count;
    }

    private void checkWin() {
        if (opened < N - MINES) return;
        won = true;
        endSecs = Math.max(0, time - startT);
        score = Math.max(1, 1000 - (long) Math.floor(endSecs) * 5);
        for (int i = 0; i < N; i++)
            if (mine[i] && !flag[i]) { flag[i] = true; flagAt[i] = clock + 0.15f + rnd.nextFloat() * 0.35f; }
        flags = MINES;
        Sound.play("minecraft:entity.player.levelup", 1.2f, 0.4f);
    }

    private void explode(int i) {
        lost = true;
        boom = i;
        boomAt = clock;
        endSecs = Math.max(0, time - startT);
        int bc = i % COLS, br = i / COLS;
        for (int j = 0; j < N; j++) {
            if (!mine[j] || flag[j]) continue;
            open[j] = true;
            float d = (float) Math.hypot(j % COLS - bc, j / COLS - br);
            openAt[j] = j == i ? clock : clock + 0.15f + d * 0.05f;
        }
        float cx = GX + bc * CELL + CELL / 2, cy = GY + br * CELL + CELL / 2;
        for (int k = 0; k < 34; k++) {
            double a = rnd.nextDouble() * Math.PI * 2;
            float sp = 50 + rnd.nextFloat() * 170;
            parts.add(new float[]{cx, cy, (float) Math.cos(a) * sp, (float) Math.sin(a) * sp - 80, 0.7f + rnd.nextFloat() * 0.7f,
                    DEBRIS[rnd.nextInt(DEBRIS.length)], 2.5f + rnd.nextFloat() * 3});
        }
        Sound.play("minecraft:entity.generic.explode", 1.1f, 0.45f);
    }

    // ------------------------------------------------------------------ drawing
    @Override
    protected void render() {
        float dt = Gx.dt;
        clock += dt;
        float hx = (mx - bx) / u, hy = (my - by) / u;
        boolean active = state == State.PLAYING && !won && !lost;
        int hover = active ? cellAt(hx, hy) : -1;
        boolean chordHover = hover >= 0 && open[hover] && adj[hover] > 0;

        header();

        // screen shake after the explosion
        float shake = lost ? Math.max(0, 1 - (clock - boomAt) / 0.4f) : 0;
        Gx.push();
        if (shake > 0) Gx.translate(S((rnd.nextFloat() - 0.5f) * 5 * shake), S((rnd.nextFloat() - 0.5f) * 5 * shake));

        int ground = Style.light ? 0xFFD5D9E0 : 0xFF14161C, ground2 = Style.light ? 0xFFCED2DA : 0xFF111318;
        box(GX - 3, GY - 3, COLS * CELL + 6, ROWS * CELL + 6, 9, Style.light ? 0x12000000 : 0x0DFFFFFF);
        for (int i = 0; i < N; i++) {
            int c = i % COLS, r = i / COLS;
            float x = GX + c * CELL, y = GY + r * CELL, cx = x + CELL / 2, cy = y + CELL / 2;
            boolean shownOpen = open[i] && clock >= openAt[i];
            float pa = open[i] ? Ease.clamp((clock - openAt[i]) / 0.2f) : 0;
            if (open[i] && shownOpen) {
                int gc = (c + r) % 2 == 0 ? ground : ground2;
                if (mine[i] && i == boom) gc = 0xFFE3000B;
                else if (mine[i]) gc = Gx.mix(gc, 0xFFE3000B, 0.25f);
                if (chordHover && i == hover) gc = Gx.mix(gc, Style.accent, 0.15f);
                box(x + 0.6f, y + 0.6f, CELL - 1.2f, CELL - 1.2f, 4, gc);
                if (mine[i]) {
                    if (i == boom) Gx.glow(X(cx), Y(cy), S(30), Gx.withAlpha(0xFFFF8A00, 0.5f));
                    mineIcon(cx, cy, Ease.outBack(pa));
                } else if (adj[i] > 0) {
                    float s = 0.6f + 0.4f * Ease.outBack(pa);
                    int nc = (Style.light ? NUM_LIGHT : NUM)[adj[i]];
                    Gx.push();
                    Gx.scaleAt(X(cx), Y(cy), s);
                    text(String.valueOf(adj[i]), cx, cy, 11, Fonts.BOLD, Gx.withAlpha(nc, pa));
                    Gx.pop();
                }
            }
            if (!open[i] || pa < 1) {
                // brick (fading + shrinking while it is being revealed)
                float s = open[i] ? 1 - 0.4f * Ease.outCubic(pa) : 1;
                float a = open[i] ? 1 - pa : 1;
                int bc = (c + r) % 2 == 0 ? 0xFF959BA3 : 0xFF8C929A;
                if (won && mine[i]) bc = Gx.mix(bc, Style.ok, 0.45f * Ease.clamp((clock - flagAt[i]) / 0.3f));
                if (i == hover && !open[i]) bc = Gx.mix(bc, 0xFFFFFFFF, 0.25f);
                else if (chordHover && !open[i] && !flag[i] && isNeighbour(hover, i)) bc = Gx.mix(bc, Style.accent, 0.3f);
                Gx.push();
                Gx.pushAlpha(a);
                Gx.scaleAt(X(cx), Y(cy), s);
                float lift = i == hover && !open[i] ? 0.8f : 0;
                brick(x + 1, y + 1 - lift, CELL - 2, CELL - 2, bc);
                if (flag[i]) {
                    float fs = Ease.outBack((clock - flagAt[i]) / 0.25f);
                    if (fs > 0) {
                        Gx.icon("flag", X(cx + 0.6f), Y(cy + 0.8f - lift), S(12) * fs, 0x66000000);
                        Gx.icon("flag", X(cx), Y(cy - lift), S(12) * fs, 0xFFE3000B);
                    }
                    if (lost && !mine[i]) {   // wrong flag
                        float wa = Ease.clamp((clock - boomAt - 0.3f) / 0.2f);
                        Gx.icon("x", X(cx), Y(cy), S(17), Gx.withAlpha(0xFFFFFFFF, wa));
                    }
                }
                Gx.popAlpha();
                Gx.pop();
            }
        }

        // explosion
        if (lost) {
            float t = clock - boomAt;
            float bcx = GX + (boom % COLS) * CELL + CELL / 2, bcy = GY + (boom / COLS) * CELL + CELL / 2;
            if (t < 0.7f) {
                float p = t / 0.7f;
                Gx.glow(X(bcx), Y(bcy), S(20 + 70 * Ease.outCubic(p)), Gx.withAlpha(0xFFFFA23D, 0.8f * (1 - p)));
                Gx.ringAt(X(bcx), Y(bcy), S(6 + 80 * Ease.outCubic(p)), Math.max(1, S(3 * (1 - p) + 0.5f)), Gx.withAlpha(0xFFFFFFFF, 0.8f * (1 - p)));
            }
            for (float[] p : parts) {
                p[3] += 420 * dt;
                p[0] += p[2] * dt;
                p[1] += p[3] * dt;
                p[4] -= dt;
                if (p[4] <= 0) continue;
                float a = Math.min(1, p[4] / 0.3f);
                box(p[0] - p[6] / 2, p[1] - p[6] / 2, p[6], p[6], 1, Gx.withAlpha((int) p[5], a));
            }
            parts.removeIf(p -> p[4] <= 0);
        }
        Gx.pop();
    }

    private boolean isNeighbour(int a, int b) {
        int ac = a % COLS, ar = a / COLS, bc = b % COLS, br = b / COLS;
        return a != b && Math.abs(ac - bc) <= 1 && Math.abs(ar - br) <= 1;
    }

    private void mineIcon(float cx, float cy, float s) {
        if (s <= 0) return;
        float r = 5.6f * s;
        for (int k = 0; k < 8; k++) {
            double a = k * Math.PI / 4;
            circle(cx + (float) Math.cos(a) * r * 1.2f, cy + (float) Math.sin(a) * r * 1.2f, 1.3f * s, 0xFF1B1D22);
        }
        circle(cx, cy, r, 0xFF1B1D22);
        circle(cx, cy, r * 0.62f, 0xFF30333B);
        circle(cx - r * 0.35f, cy - r * 0.35f, r * 0.26f, 0xCCFFFFFF);
    }

    private void header() {
        float y = 8, h = 26;
        // flags left
        int left = MINES - flags;
        box(GX, y, 76, h, 13, Style.surface);
        Gx.icon("flag", X(GX + 16), Y(y + h / 2), S(12), 0xFFE3000B);
        Gx.textMid(String.valueOf(left), X(GX + 28), Y(y + h / 2), 10 * u, Fonts.BOLD, left < 0 ? Style.danger : Style.text);
        Gx.textRight("Minen", X(GX + 68), Y(y + h / 2 + 0.5f), 6 * u, Fonts.MEDIUM, Style.muted);
        // timer right
        float tw = 76, tx = GX + COLS * CELL - tw;
        int s = (int) secs();
        box(tx, y, tw, h, 13, Style.surface);
        Gx.icon("clock", X(tx + 16), Y(y + h / 2), S(11), Style.accent);
        Gx.textMid(String.format("%d:%02d", s / 60, s % 60), X(tx + 28), Y(y + h / 2), 10 * u, Fonts.BOLD, Style.text);
        // status middle
        String st;
        int sc = Style.sub;
        if (won) { st = "Alle Minen gefunden!"; sc = Style.ok; }
        else if (lost) { st = "BOOM! Mine erwischt"; sc = Style.danger; }
        else if (!generated) st = "Erster Klick ist sicher";
        else st = Math.round(opened * 100f / (N - MINES)) + "% aufgedeckt";
        text(st, GX + COLS * CELL / 2f, y + h / 2, 7.4f, Fonts.MEDIUM, sc);
        if (generated && !won && !lost) {
            float pw = 90, px = GX + COLS * CELL / 2f - pw / 2;
            box(px, y + h - 3, pw, 2, 1, Style.surface2);
            box(px, y + h - 3, pw * opened / (float) (N - MINES), 2, 1, Style.accent);
        }
    }
}
