package dev.lego.games;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Style;

/** Snake with Lego bricks: eat the studs, grow, don't bite yourself. */
public final class Snake extends GameView {
    private static final int C = 22, R = 16;       // columns, rows
    private static final float CELL = 16;
    private final ArrayDeque<int[]> body = new ArrayDeque<>();
    private final ArrayDeque<int[]> queue = new ArrayDeque<>();
    private int dx = 1, dy = 0;
    private int[] food;
    private float step = 0, speed = 0.12f;
    private final List<float[]> pops = new ArrayList<>();   // x, y, t

    public Snake() { super("snake"); }

    @Override protected float boardW() { return C * CELL; }
    @Override protected float boardH() { return R * CELL; }

    @Override
    protected void reset() {
        body.clear();
        queue.clear();
        for (int i = 0; i < 4; i++) body.addFirst(new int[]{4 + i, R / 2});
        dx = 1;
        dy = 0;
        speed = 0.12f;
        step = 0;
        pops.clear();
        placeFood();
    }

    private void placeFood() {
        while (true) {
            int[] f = {rnd.nextInt(C), rnd.nextInt(R)};
            boolean hit = false;
            for (int[] b : body) if (b[0] == f[0] && b[1] == f[1]) { hit = true; break; }
            if (!hit) { food = f; return; }
        }
    }

    @Override
    protected void onKey(int k) {
        int[] d = null;
        if (k == K_LEFT || k == K_A) d = new int[]{-1, 0};
        if (k == K_RIGHT || k == K_D) d = new int[]{1, 0};
        if (k == K_UP || k == K_W) d = new int[]{0, -1};
        if (k == K_DOWN || k == K_S) d = new int[]{0, 1};
        if (d == null || queue.size() > 2) return;
        int[] last = queue.isEmpty() ? new int[]{dx, dy} : queue.peekLast();
        if (d[0] == -last[0] && d[1] == -last[1]) return;
        if (d[0] == last[0] && d[1] == last[1]) return;
        queue.add(d);
    }

    @Override
    protected void update(float dt) {
        for (float[] p : pops) p[2] += dt;
        pops.removeIf(p -> p[2] > 0.5f);
        step += dt;
        if (step < speed) return;
        step -= speed;
        if (!queue.isEmpty()) { int[] d = queue.poll(); dx = d[0]; dy = d[1]; }
        int[] head = body.peekFirst();
        int nx = head[0] + dx, ny = head[1] + dy;
        if (nx < 0 || ny < 0 || nx >= C || ny >= R) { gameOver(); return; }
        boolean grow = nx == food[0] && ny == food[1];
        int i = 0, n = body.size();
        for (int[] b : body) {
            if (!grow && i == n - 1) break;
            if (b[0] == nx && b[1] == ny) { gameOver(); return; }
            i++;
        }
        body.addFirst(new int[]{nx, ny});
        if (grow) {
            score += 10;
            pops.add(new float[]{food[0], food[1], 0});
            speed = Math.max(0.055f, speed * 0.965f);
            dev.lego.ui.Sound.play("minecraft:entity.experience_orb.pickup", 1.2f + rnd.nextFloat() * 0.3f, 0.25f);
            placeFood();
        } else body.removeLast();
    }

    @Override
    protected void render() {
        // checker floor
        for (int y = 0; y < R; y++)
            for (int x = 0; x < C; x++)
                if ((x + y) % 2 == 0) box(x * CELL, y * CELL, CELL, CELL, 0, Style.light ? 0x0A000000 : 0x08FFFFFF);
        // food: pulsing yellow stud brick
        float pulse = 1 + 0.08f * (float) Math.sin(time * 8);
        float fs = CELL * 0.8f * pulse;
        brick(food[0] * CELL + (CELL - fs) / 2, food[1] * CELL + (CELL - fs) / 2, fs, fs, 0xFFFFCD03);
        // snake
        int n = body.size(), i = 0;
        float frac = state == State.PLAYING ? Math.min(1, step / speed) : 1;
        for (int[] b : body) {
            float t = i / (float) Math.max(1, n - 1);
            int col = Gx.mix(Style.accent, Gx.mix(Style.accent, 0xFF000000, 0.35f), t);
            float pad = i == 0 ? 0.5f : 1.2f + t * 1.2f;
            brick(b[0] * CELL + pad, b[1] * CELL + pad, CELL - pad * 2, CELL - pad * 2, col);
            if (i == 0) {
                float ex = b[0] * CELL + CELL / 2 + dx * 3, ey = b[1] * CELL + CELL / 2 + dy * 3;
                float ox = dy != 0 ? 3.2f : 0, oy = dx != 0 ? 3.2f : 0;
                circle(ex + ox, ey + oy, 1.8f, 0xFFFFFFFF);
                circle(ex - ox, ey - oy, 1.8f, 0xFFFFFFFF);
                circle(ex + ox + dx * 0.6f, ey + oy + dy * 0.6f, 0.9f, 0xFF111111);
                circle(ex - ox + dx * 0.6f, ey - oy + dy * 0.6f, 0.9f, 0xFF111111);
            }
            i++;
        }
        for (float[] p : pops) {
            float a = 1 - p[2] / 0.5f;
            text("+10", p[0] * CELL + CELL / 2, p[1] * CELL - p[2] * 30, 8, Fonts.BOLD, Gx.withAlpha(0xFFFFCD03, a));
        }
    }
}
