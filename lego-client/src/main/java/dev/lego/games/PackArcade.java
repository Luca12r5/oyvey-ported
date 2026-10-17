package dev.lego.games;

import java.util.ArrayList;
import java.util.List;

import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Spiele-Paket 4b: Arcade & Geschicklichkeit. */
final class PackArcade {
    private PackArcade() { }
    static void boom() { Sound.play("minecraft:entity.generic.explode", 1.4f, 0.25f); }
    static void pop() { Sound.play("minecraft:entity.chicken.egg", 1.5f, 0.3f); }
}

// ====================================================================== Straßenfrosch
final class Frogger extends GameView {
    private static final int COLS = 13, ROWS = 11;
    private static final float C = 24;
    private int fx, fy, lives, level;
    private float rideX;
    private final float[] laneOff = new float[ROWS];
    private float hurt;

    Frogger() { super("frogger"); }
    @Override protected float boardW() { return COLS * C; }
    @Override protected float boardH() { return ROWS * C; }

    @Override protected void reset() { lives = 3; level = 1; home(); java.util.Arrays.fill(laneOff, 0); }
    private void home() { fx = COLS / 2; fy = ROWS - 1; rideX = 0; }

    /** lane type: 0 safe, 1 road, 2 river */
    private int type(int r) { return r == 0 || r == 5 || r == ROWS - 1 ? 0 : r < 5 ? 2 : 1; }
    private float speed(int r) { return ((r % 2 == 0) ? 1 : -1) * (1.4f + (r * 37 % 5) * 0.35f) * (0.85f + level * 0.15f); }
    private int len(int r) { return type(r) == 2 ? 3 + r % 2 : 1 + (r % 3 == 0 ? 1 : 0); }
    private int gap(int r) { return type(r) == 2 ? 3 : 4 - (r % 2); }

    /** Is lane cell position x (in cells, continuous) covered by an object? */
    private boolean covered(int r, float x) {
        float period = len(r) + gap(r);
        float p = ((x - laneOff[r]) % period + period) % period;
        return p < len(r);
    }

    @Override protected void onKey(int k) {
        if (hurt > 0) return;
        if (k == K_UP || k == K_W) fy--; else if (k == K_DOWN || k == K_S) fy = Math.min(ROWS - 1, fy + 1);
        else if (k == K_LEFT || k == K_A) fx = Math.max(0, fx - 1); else if (k == K_RIGHT || k == K_D) fx = Math.min(COLS - 1, fx + 1);
        else return;
        Sound.play("minecraft:block.wool.step", 1.4f, 0.3f);
        rideX = 0;
        if (fy <= 0) { score += 100 * level; level++; Sound.play("minecraft:entity.player.levelup", 1.4f, 0.3f); home(); }
    }

    private void die() { lives--; hurt = 0.8f; Sound.play("minecraft:entity.slime.squish", 1f, 0.4f); if (lives <= 0) gameOver(); }

    @Override protected void update(float dt) {
        for (int r = 0; r < ROWS; r++) laneOff[r] += speed(r) * dt;
        if (hurt > 0) { hurt -= dt; if (hurt <= 0) home(); return; }
        float x = fx + 0.5f + rideX;
        int t = type(fy);
        if (t == 1 && covered(fy, x)) die();
        if (t == 2) {
            if (!covered(fy, x)) die();
            else { rideX += speed(fy) * dt; if (fx + rideX < -0.4f || fx + rideX > COLS - 0.6f) die(); }
        }
        if (t == 2 && hurt <= 0 && Math.abs(rideX) >= 1) { int s = (int) Math.signum(rideX); fx += s; rideX -= s; }
    }

    @Override protected void render() {
        for (int r = 0; r < ROWS; r++) {
            int t = type(r);
            box(0, r * C, COLS * C, C, 0, t == 0 ? 0xFF3E8E41 : t == 1 ? 0xFF33363E : 0xFF1A5AA0);
            if (t == 0) continue;
            float period = len(r) + gap(r);
            for (float s = -period * 2; s < COLS + period; s += period) {
                float x0 = s + (laneOff[r] % period);
                if (t == 1) { int col = PackPuzzle.PAL[r % 6]; brick(x0 * C + 2, r * C + 4, len(r) * C - 4, C - 8, col); }
                else box(x0 * C + 1, r * C + 4, len(r) * C - 2, C - 8, 6, 0xFF8A5A30);
            }
        }
        float x = (fx + rideX) * C;
        if (hurt <= 0 || ((int) (hurt * 12)) % 2 == 0) {
            box(x + 4, fy * C + 4, C - 8, C - 8, 7, 0xFF39D353);
            circle(x + 8, fy * C + 7, 2.5f, 0xFFFFFFFF); circle(x + C - 8, fy * C + 7, 2.5f, 0xFFFFFFFF);
        }
        text("Leben " + lives + " · Level " + level, COLS * C / 2, C / 2, 7, Fonts.BOLD, 0xFFFFFFFF);
    }
}

// ====================================================================== Höhlenflug
final class CaveFlight extends GameView {
    private static final float W = 400, H = 260;
    private float y, vy, dist, seed;
    private boolean hold;
    private final List<float[]> smoke = new ArrayList<>();

    CaveFlight() { super("caveflight"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Meter"; }

    @Override protected void reset() { y = H / 2; vy = 0; dist = 0; seed = rnd.nextFloat() * 100; smoke.clear(); }

    private float top(float x) { float d = dist + x; return 30 + 30 * (float) Math.sin(d * 0.012 + seed) + 18 * (float) Math.sin(d * 0.031 + seed * 2) + Math.min(50, dist / 200); }
    private float bot(float x) { float d = dist + x; return H - 30 - 30 * (float) Math.sin(d * 0.011 + seed + 2) - 18 * (float) Math.sin(d * 0.027 + seed * 3) - Math.min(50, dist / 200); }

    @Override protected void onClick(float x, float yy, int b) { hold = true; }
    @Override protected void onRelease(float x, float yy, int b) { hold = false; }
    @Override protected void onKey(int k) { if (k == K_SPACE || k == K_UP || k == K_W) hold = true; }
    @Override protected void onKeyUp(int k) { if (k == K_SPACE || k == K_UP || k == K_W) hold = false; }

    @Override protected void update(float dt) {
        float speed = 120 + Math.min(160, dist / 25);
        dist += speed * dt;
        vy += (hold ? -520 : 420) * dt;
        vy = Math.max(-240, Math.min(260, vy));
        y += vy * dt;
        score = (long) (dist / 10);
        if (rnd.nextFloat() < 0.5f) smoke.add(new float[]{80, y + 4, 0});
        for (float[] s : smoke) { s[0] -= speed * dt; s[2] += dt; }
        smoke.removeIf(s -> s[2] > 0.8f);
        if (y - 7 < top(90) || y + 7 > bot(90)) { PackArcade.boom(); gameOver(); }
    }

    @Override protected void render() {
        boxV(0, 0, W, H, 0, 0xFF1A1030, 0xFF3A1A40);
        for (float x = 0; x < W; x += 8) { float t = top(x), b = bot(x); box(x, 0, 8.5f, t, 0, 0xFF6A4A3A); box(x, b, 8.5f, H - b, 0, 0xFF6A4A3A); }
        for (float[] s : smoke) circle(s[0], s[1], 3 + s[2] * 6, Gx.withAlpha(0xFFFFFFFF, 0.3f * (1 - s[2] / 0.8f)));
        float tilt = vy / 600f;
        box(80, y - 6 + tilt * 4, 24, 12, 5, 0xFFE3000B);
        box(98, y - 4, 8, 6, 2, 0xFF9FD8FF);
        line(84, y - 9, 102, y - 9, 1.5f, 0xFF1B1B22);
        float rot = (float) Math.sin(time * 40) * 12;
        line(93 - rot, y - 11, 93 + rot, y - 11, 1.2f, 0xFF1B1B22);
        text(score + " m", W - 40, 14, 8, Fonts.BOLD, 0xFFFFFFFF);
    }
}

// ====================================================================== Raketenabwehr
final class MissileCommand extends GameView {
    private static final float W = 400, H = 270, GY = 250;
    private final List<float[]> missiles = new ArrayList<>();   // x0 y0 x y tx speed
    private final List<float[]> blasts = new ArrayList<>();     // x y t
    private final List<float[]> shots = new ArrayList<>();      // x y tx ty
    private final boolean[] city = new boolean[6];
    private float spawn, wave;

    MissileCommand() { super("missiles"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String overTitle() { return "Alle Städte zerstört"; }

    @Override protected void reset() { missiles.clear(); blasts.clear(); shots.clear(); java.util.Arrays.fill(city, true); spawn = 1; wave = 0; }

    private float cx(int i) { return 40 + i * 64 + (i >= 3 ? 10 : 0); }

    @Override protected void onClick(float x, float y, int b) { if (y < GY - 20) { shots.add(new float[]{W / 2, GY - 10, x, y}); Sound.play("minecraft:entity.firework_rocket.launch", 1.5f, 0.25f); } }

    @Override protected void update(float dt) {
        wave += dt;
        spawn -= dt * (1 + wave / 40);
        if (spawn <= 0) {
            spawn = 1.6f;
            int t = rnd.nextInt(6);
            missiles.add(new float[]{rnd.nextFloat() * W, 0, 0, 0, cx(t) + 12, 22 + wave * 0.6f + rnd.nextFloat() * 10});
            float[] m = missiles.get(missiles.size() - 1); m[2] = m[0]; m[3] = 0;
        }
        for (int i = shots.size() - 1; i >= 0; i--) {
            float[] s = shots.get(i);
            float dx = s[2] - s[0], dy = s[3] - s[1], d = (float) Math.hypot(dx, dy), step = 320 * dt;
            if (d <= step) { blasts.add(new float[]{s[2], s[3], 0}); shots.remove(i); Sound.play("minecraft:entity.firework_rocket.blast", 1.2f, 0.3f); }
            else { s[0] += dx / d * step; s[1] += dy / d * step; }
        }
        for (float[] bl : blasts) bl[2] += dt;
        blasts.removeIf(bl -> bl[2] > 1.1f);
        for (int i = missiles.size() - 1; i >= 0; i--) {
            float[] m = missiles.get(i);
            float dx = m[4] - m[0], dy = GY - 0, d = (float) Math.hypot(dx, dy);
            m[2] += dx / d * m[5] * dt; m[3] += dy / d * m[5] * dt;
            boolean dead = false;
            for (float[] bl : blasts) if (Math.hypot(m[2] - bl[0], m[3] - bl[1]) < radius(bl[2])) dead = true;
            if (dead) { missiles.remove(i); score += 25; continue; }
            if (m[3] >= GY - 4) {
                missiles.remove(i);
                PackArcade.boom();
                for (int c = 0; c < 6; c++) if (city[c] && Math.abs(cx(c) + 12 - m[2]) < 22) city[c] = false;
                boolean any = false; for (boolean b : city) any |= b;
                if (!any) gameOver();
            }
        }
    }

    private float radius(float t) { return t < 0.5f ? t / 0.5f * 26 : 26 * (1 - (t - 0.5f) / 0.6f); }

    @Override protected void render() {
        boxV(0, 0, W, H, 0, 0xFF060818, 0xFF1A1440);
        box(0, GY, W, H - GY, 0, 0xFFC8A060);
        for (int i = 0; i < 6; i++) if (city[i]) { brick(cx(i), GY - 14, 24, 14, 0xFF3FA9FF); box(cx(i) + 6, GY - 22, 8, 8, 2, 0xFF7AC8FF); }
        box(W / 2 - 12, GY - 14, 24, 14, 4, 0xFF9AA3AD);
        for (float[] m : missiles) { line(m[0], 0, m[2], m[3], 1, 0x88FF4D5E); circle(m[2], m[3], 2.2f, 0xFFFFE14D); }
        for (float[] s : shots) { line(W / 2, GY - 10, s[0], s[1], 1, 0x8839D353); circle(s[0], s[1], 2, 0xFFFFFFFF); }
        for (float[] bl : blasts) { float r = radius(bl[2]); circle(bl[0], bl[1], r, Gx.withAlpha(0xFFFFB030, 0.7f)); circle(bl[0], bl[1], r * 0.6f, 0xCCFFF6C0); }
        float hx = bmx(), hy = bmy();
        if (hy < GY - 20) { line(hx - 5, hy, hx + 5, hy, 1, 0xFFFFFFFF); line(hx, hy - 5, hx, hy + 5, 1, 0xFFFFFFFF); }
    }
}

// ====================================================================== Mondlandung
final class LunarLander extends GameView {
    private static final float W = 400, H = 270;
    private float x, y, vx, vy, ang, fuel, padX, crashT = -1, landT = -1;
    private boolean thrust, left, right;
    private int level;
    private final float[] ground = new float[41];

    LunarLander() { super("lander"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String overTitle() { return "Bruchlandung!"; }

    @Override protected void reset() { level = 1; setup(); }

    private void setup() {
        x = 40 + rnd.nextFloat() * 320; y = 30; vx = (rnd.nextFloat() - 0.5f) * 30; vy = 0; ang = 0; fuel = 100; crashT = -1; landT = -1;
        int pad = 4 + rnd.nextInt(32);
        padX = pad * 10;
        for (int i = 0; i <= 40; i++) ground[i] = 210 + rnd.nextFloat() * 45;
        for (int i = pad; i <= pad + 4 && i <= 40; i++) ground[i] = ground[pad];
    }

    private float gy(float px) { int i = (int) Math.max(0, Math.min(39, px / 10)); float f = px / 10 - i; return ground[i] + (ground[i + 1] - ground[i]) * f; }

    @Override protected void onKey(int k) { if (k == K_UP || k == K_W || k == K_SPACE) thrust = true; if (k == K_LEFT || k == K_A) left = true; if (k == K_RIGHT || k == K_D) right = true; }
    @Override protected void onKeyUp(int k) { if (k == K_UP || k == K_W || k == K_SPACE) thrust = false; if (k == K_LEFT || k == K_A) left = false; if (k == K_RIGHT || k == K_D) right = false; }

    @Override protected void update(float dt) {
        if (landT >= 0) { if ((landT += dt) > 1.5f) { level++; setup(); } return; }
        if (crashT >= 0) { if ((crashT += dt) > 1.2f) gameOver(); return; }
        if (left) ang -= 120 * dt; if (right) ang += 120 * dt;
        ang = Math.max(-80, Math.min(80, ang));
        float g = 22 + level * 2;
        vy += g * dt;
        if (thrust && fuel > 0) { float a = (float) Math.toRadians(ang); vx += Math.sin(a) * 55 * dt; vy -= Math.cos(a) * 55 * dt; fuel = Math.max(0, fuel - 14 * dt); }
        x += vx * dt; y += vy * dt;
        if (x < 0 || x > W) vx = -vx * 0.5f;
        x = Math.max(0, Math.min(W, x));
        if (y + 8 >= gy(x)) {
            boolean onPad = x >= padX && x <= padX + 40;
            if (onPad && vy < 30 && Math.abs(vx) < 18 && Math.abs(ang) < 12) { landT = 0; score += 100 + (long) fuel * 2 + level * 20; Sound.play("minecraft:entity.player.levelup", 1.3f, 0.35f); }
            else { crashT = 0; PackArcade.boom(); }
            y = gy(x) - 8;
        }
    }

    @Override protected void render() {
        box(0, 0, W, H, 0, 0xFF05060E);
        for (int i = 0; i < 40; i++) circle((i * 97) % W, (i * 53) % 150, 0.8f, 0x99FFFFFF);
        for (int i = 0; i < 40; i++) for (float px = i * 10; px < i * 10 + 10; px += 2) box(px, gy(px), 2.2f, H - gy(px), 0, 0xFF8A8A92);
        box(padX, gy(padX) - 2, 40, 3, 1, 0xFF39D353);
        if (crashT < 0) {
            float sa = (float) Math.sin(Math.toRadians(ang)), ca = (float) Math.cos(Math.toRadians(ang));
            box(x - 7, y - 8, 14, 12, 4, 0xFFD8DEE6);
            box(x - 4, y - 6, 8, 5, 2, 0xFF3FA9FF);
            line(x - 6, y + 4, x - 9, y + 9, 1.2f, 0xFFD8DEE6); line(x + 6, y + 4, x + 9, y + 9, 1.2f, 0xFFD8DEE6);
            line(x, y - 2, x + sa * 14, y - 2 - ca * 14, 1, 0x88FFFFFF);
            if (thrust && fuel > 0) circle(x - sa * 10, y + ca * 10 + (float) Math.random() * 2, 3, 0xFFFFB030);
        } else circle(x, y, 6 + crashT * 30, Gx.withAlpha(0xFFFF6A00, 1 - crashT));
        textL("Treibstoff " + Math.round(fuel) + "%", 8, 10, 7, Fonts.BOLD, fuel < 20 ? 0xFFFF4D5E : 0xFFFFFFFF);
        textL("Fall " + Math.round(vy) + "  Seite " + Math.round(vx), 8, 22, 7, Fonts.MEDIUM, vy > 30 ? 0xFFFF4D5E : 0xFF9AA3B2);
        textL("Level " + level, 340, 10, 7, Fonts.BOLD, 0xFFFFFFFF);
        if (landT >= 0) text("Sanft gelandet!", W / 2, 110, 14, Fonts.BOLD, 0xFF39D353);
    }
}

// ====================================================================== Lichtrenner (Tron)
final class LightCycles extends GameView {
    private static final int GW = 64, GH = 42;
    private static final float C = 6.25f;
    private int[][] g;
    private int px, py, pdx, pdy, ax, ay, adx, ady, winner, wins;
    private float step;
    private final java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();

    LightCycles() { super("lightcycles"); }
    @Override protected boolean hasAi() { return true; }
    @Override protected float boardW() { return GW * C; }
    @Override protected float boardH() { return GH * C; }
    @Override protected String scoreLabel() { return "Siege"; }
    @Override protected String overTitle() { return winner == 1 ? "Gewonnen!" : winner == 2 ? "Verloren" : "Beide gecrasht"; }

    @Override protected void reset() { g = new int[GW][GH]; px = 10; py = GH / 2; pdx = 1; pdy = 0; ax = GW - 11; ay = GH / 2; adx = -1; ady = 0; winner = 0; step = 0; q.clear(); score = wins; }

    @Override protected void onKey(int k) {
        int[] d = k == K_LEFT || k == K_A ? new int[]{-1, 0} : k == K_RIGHT || k == K_D ? new int[]{1, 0} : k == K_UP || k == K_W ? new int[]{0, -1} : k == K_DOWN || k == K_S ? new int[]{0, 1} : null;
        if (d != null && q.size() < 3) q.add(d);
    }

    private boolean free(int x, int y) { return x >= 0 && y >= 0 && x < GW && y < GH && g[x][y] == 0; }

    private int space(int x, int y, int limit) {
        if (!free(x, y)) return 0;
        boolean[][] seen = new boolean[GW][GH];
        java.util.ArrayDeque<int[]> f = new java.util.ArrayDeque<>();
        f.add(new int[]{x, y}); seen[x][y] = true;
        int n = 0;
        while (!f.isEmpty() && n < limit) {
            int[] p = f.poll(); n++;
            for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) { int a = p[0] + d[0], b = p[1] + d[1]; if (free(a, b) && !seen[a][b]) { seen[a][b] = true; f.add(new int[]{a, b}); } }
        }
        return n;
    }

    private void aiTurn() {
        int lv = aiLevel();
        int[][] dirs = {{adx, ady}, {ady, -adx}, {-ady, adx}};
        int[] best = dirs[0];
        int bs = -1;
        for (int[] d : dirs) {
            int s = lv == 0 ? (free(ax + d[0], ay + d[1]) ? 1 + rnd.nextInt(3) : 0) : space(ax + d[0], ay + d[1], lv == 1 ? 40 : lv == 2 ? 300 : 1200);
            if (lv >= 2 && s > 0) s += d == dirs[0] ? 2 : 0;
            if (lv == 3 && s > 0) s -= (Math.abs(ax + d[0] - px) + Math.abs(ay + d[1] - py)) / 4;
            if (s > bs) { bs = s; best = d; }
        }
        adx = best[0]; ady = best[1];
    }

    @Override protected void update(float dt) {
        step += dt;
        if (step < 0.055f) return;
        step = 0;
        if (!q.isEmpty()) { int[] d = q.poll(); if (d[0] != -pdx || d[1] != -pdy) { pdx = d[0]; pdy = d[1]; } }
        aiTurn();
        g[px][py] = 1; g[ax][ay] = 2;
        px += pdx; py += pdy; ax += adx; ay += ady;
        boolean pd = !free(px, py) || (px == ax && py == ay), ad = !free(ax, ay) || (px == ax && py == ay);
        if (pd || ad) {
            winner = pd && ad ? 3 : pd ? 2 : 1;
            if (winner == 1) { wins++; score = wins; Sound.play("minecraft:entity.player.levelup", 1.3f, 0.3f); } else PackArcade.boom();
            gameOver();
        }
    }

    @Override protected void render() {
        box(0, 0, GW * C, GH * C, 0, 0xFF05070F);
        for (int x = 0; x < GW; x += 4) box(x * C, 0, 0.6f, GH * C, 0, 0x2200A3DA);
        for (int y = 0; y < GH; y += 4) box(0, y * C, GW * C, 0.6f, 0, 0x2200A3DA);
        for (int x = 0; x < GW; x++) for (int y = 0; y < GH; y++) if (g[x][y] != 0) box(x * C, y * C, C, C, 0, g[x][y] == 1 ? 0xFF3AD8FF : 0xFFFF7A2A);
        box(px * C - 1, py * C - 1, C + 2, C + 2, 2, 0xFFFFFFFF);
        box(ax * C - 1, ay * C - 1, C + 2, C + 2, 2, 0xFFFFE0B0);
    }
}

// ====================================================================== Air Hockey
final class AirHockey extends GameView {
    private static final float W = 260, H = 300, PR = 15, BR = 9, GOAL = 80;
    private float bx2, by2, bvx, bvy, px, py, ppx, ppy, ax, ay, flash;
    private int sp, sa;

    AirHockey() { super("airhockey"); }
    @Override protected boolean hasAi() { return true; }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Tore"; }
    @Override protected String overTitle() { return sp > sa ? "Gewonnen " + sp + ":" + sa : "Verloren " + sp + ":" + sa; }

    @Override protected void reset() { sp = sa = 0; serve(true); px = W / 2; py = H - 40; ax = W / 2; ay = 40; }

    private void serve(boolean toAi) { bx2 = W / 2; by2 = H / 2; bvx = (rnd.nextFloat() - 0.5f) * 60; bvy = toAi ? -90 : 90; }

    private void hitBy(float mx2, float my2, float mvx, float mvy) {
        float dx = bx2 - mx2, dy = by2 - my2, d = (float) Math.hypot(dx, dy);
        if (d < PR + BR && d > 0) {
            float nx = dx / d, ny = dy / d;
            bx2 = mx2 + nx * (PR + BR); by2 = my2 + ny * (PR + BR);
            float rel = (bvx - mvx) * nx + (bvy - mvy) * ny;
            if (rel < 0) { bvx -= 1.9f * rel * nx; bvy -= 1.9f * rel * ny; Sound.play("minecraft:block.wood.hit", 1.6f, 0.4f); }
            float sp2 = (float) Math.hypot(bvx, bvy);
            if (sp2 > 520) { bvx *= 520 / sp2; bvy *= 520 / sp2; }
        }
    }

    @Override protected void update(float dt) {
        flash = Math.max(0, flash - dt);
        float tx = Math.max(PR, Math.min(W - PR, bmx())), ty = Math.max(H / 2 + PR, Math.min(H - PR, bmy()));
        float mvx = (tx - px) / dt, mvy = (ty - py) / dt;
        px = tx; py = ty;
        // AI
        float speed = new float[]{110, 170, 240, 330}[aiLevel()];
        float targetX = by2 < H / 2 + 20 ? bx2 : W / 2, targetY = by2 < H / 2 ? Math.min(by2 - 4, H / 2 - PR) : 40;
        if (bvy > 0 && by2 < H / 2) targetY = Math.max(PR, by2 - 18);
        float dxa = targetX - ax, dya = targetY - ay, da = (float) Math.hypot(dxa, dya);
        float avx = 0, avy = 0;
        if (da > 1) { float s = Math.min(da, speed * dt); avx = dxa / da * s / dt; avy = dya / da * s / dt; ax += dxa / da * s; ay += dya / da * s; }
        ay = Math.max(PR, Math.min(H / 2 - PR, ay));
        bx2 += bvx * dt; by2 += bvy * dt;
        bvx *= 1 - 0.25f * dt; bvy *= 1 - 0.25f * dt;
        if (bx2 < BR) { bx2 = BR; bvx = -bvx; } if (bx2 > W - BR) { bx2 = W - BR; bvx = -bvx; }
        boolean inGoal = Math.abs(bx2 - W / 2) < GOAL / 2;
        if (by2 < BR) { if (inGoal) { sp++; score = sp; flash = 0.6f; Sound.play("minecraft:entity.player.levelup", 1.4f, 0.3f); serve(true); } else { by2 = BR; bvy = -bvy; } }
        if (by2 > H - BR) { if (inGoal) { sa++; flash = 0.6f; PackPuzzle.bad(); serve(false); } else { by2 = H - BR; bvy = -bvy; } }
        hitBy(px, py, mvx, mvy);
        hitBy(ax, ay, avx, avy);
        if (sp >= 7 || sa >= 7) { score = sp > sa ? sp * 10 - sa : 0; gameOver(); }
    }

    @Override protected boolean clickToStart() { return true; }

    @Override protected void render() {
        box(0, 0, W, H, 12, 0xFFE8F4FF);
        line(0, H / 2, W, H / 2, 1.5f, 0xFFE3000B);
        ring(W / 2, H / 2, 30, 1.5f, 0xFFE3000B);
        box(W / 2 - GOAL / 2, 0, GOAL, 5, 2, 0xFF1B1B22);
        box(W / 2 - GOAL / 2, H - 5, GOAL, 5, 2, 0xFF1B1B22);
        circle(bx2, by2, BR, 0xFF1B1B22);
        circle(ax, ay, PR, 0xFFE3000B); circle(ax, ay, PR * 0.5f, 0xFFB0101A);
        circle(px, py, PR, 0xFF006CB7); circle(px, py, PR * 0.5f, 0xFF004A8A);
        text(sa + "", W - 16, H / 2 - 14, 12, Fonts.BOLD, 0x88E3000B);
        text(sp + "", W - 16, H / 2 + 14, 12, Fonts.BOLD, 0x88006CB7);
        if (flash > 0) text("TOR!", W / 2, H / 2, 20, Fonts.BOLD, Gx.withAlpha(0xFFFFCD03, flash / 0.6f));
    }
}

// ====================================================================== Plinko
final class Plinko extends GameView {
    private static final float W = 360, H = 290;
    private static final int[] BINS = {50, 20, 10, 5, 1, 5, 10, 20, 50};
    private final List<float[]> balls = new ArrayList<>();   // x y vx vy
    private int left;
    private float lastBin = -1, binFx;

    Plinko() { super("plinko"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String overTitle() { return "Alle Kugeln gespielt"; }

    @Override protected void reset() { balls.clear(); left = 10; }

    @Override protected void onClick(float x, float y, int b) {
        if (left <= 0 || y > 40) return;
        left--; balls.add(new float[]{Math.max(20, Math.min(W - 20, x)), 12, (rnd.nextFloat() - 0.5f) * 10, 0});
        PackArcade.pop();
    }

    private float pegX(int r, int c) { return 30 + c * 37.5f + (r % 2) * 18.75f; }
    private float pegY(int r) { return 55 + r * 22; }

    @Override protected void update(float dt) {
        binFx = Math.max(0, binFx - dt);
        for (int i = balls.size() - 1; i >= 0; i--) {
            float[] b = balls.get(i);
            b[3] += 380 * dt; b[0] += b[2] * dt; b[1] += b[3] * dt;
            if (b[0] < 6) { b[0] = 6; b[2] = Math.abs(b[2]) * 0.6f; } if (b[0] > W - 6) { b[0] = W - 6; b[2] = -Math.abs(b[2]) * 0.6f; }
            for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) {
                float dx = b[0] - pegX(r, c), dy = b[1] - pegY(r), d = (float) Math.hypot(dx, dy);
                if (d < 8 && d > 0) {
                    float nx = dx / d, ny = dy / d;
                    b[0] = pegX(r, c) + nx * 8; b[1] = pegY(r) + ny * 8;
                    float dot = b[2] * nx + b[3] * ny;
                    if (dot < 0) { b[2] -= 1.5f * dot * nx; b[3] -= 1.5f * dot * ny; b[2] += (rnd.nextFloat() - 0.5f) * 30; Sound.play("minecraft:block.note_block.hat", 1.6f + rnd.nextFloat() * 0.3f, 0.12f); }
                }
            }
            if (b[1] > H - 22) {
                int bin = Math.max(0, Math.min(8, (int) (b[0] / (W / 9))));
                score += BINS[bin]; lastBin = bin; binFx = 1;
                Sound.play(BINS[bin] >= 20 ? "minecraft:entity.player.levelup" : "minecraft:entity.experience_orb.pickup", 1.4f, 0.3f);
                balls.remove(i);
            }
        }
        if (left <= 0 && balls.isEmpty()) gameOver();
    }

    @Override protected void render() {
        boxV(0, 0, W, H, 0, 0xFF1A1440, 0xFF2A1A50);
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) if (pegX(r, c) < W - 10) circle(pegX(r, c), pegY(r), 3.2f, 0xFFD8DEE6);
        float bw = W / 9;
        for (int i = 0; i < 9; i++) {
            int col = BINS[i] >= 50 ? 0xFFFFCD03 : BINS[i] >= 20 ? 0xFFFF8A00 : BINS[i] >= 10 ? 0xFF3FA9FF : 0xFF5A6270;
            if (i == lastBin && binFx > 0) col = Gx.mix(col, 0xFFFFFFFF, binFx * 0.6f);
            box(i * bw + 2, H - 22, bw - 4, 20, 4, col);
            text(String.valueOf(BINS[i]), i * bw + bw / 2, H - 11, 8, Fonts.BOLD, 0xFF101010);
        }
        for (float[] b : balls) circle(b[0], b[1], 5, 0xFFE3000B);
        if (left > 0) { float x = Math.max(20, Math.min(W - 20, bmx())); circle(x, 14, 5, 0x88E3000B); }
        textL("Kugeln: " + left, 8, 10, 7, Fonts.BOLD, 0xFFFFFFFF);
    }
}

// ====================================================================== Korbwurf
final class Basketball extends GameView {
    private static final float W = 400, H = 280, BR = 9;
    private float bx2, by2, vx, vy, hx, hy, left, swish;
    private boolean flying, drag, scored;
    private float dsx, dsy;

    Basketball() { super("basketball"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String overTitle() { return "Zeit um!"; }

    @Override protected void reset() { left = 45; newBall(); hx = 300; hy = 90; }
    private void newBall() { bx2 = 70; by2 = 220; vx = vy = 0; flying = false; scored = false; }

    @Override protected void onClick(float x, float y, int b) { if (!flying && Math.hypot(x - bx2, y - by2) < 30) { drag = true; dsx = x; dsy = y; } }
    @Override protected void onRelease(float x, float y, int b) {
        if (!drag) return;
        drag = false;
        vx = (dsx - x) * 4.2f; vy = (dsy - y) * 4.2f;
        if (Math.hypot(vx, vy) > 40) { flying = true; Sound.play("minecraft:entity.snowball.throw", 1f, 0.3f); }
    }

    @Override protected void update(float dt) {
        left -= dt; swish = Math.max(0, swish - dt);
        if (left <= 0) { left = 0; gameOver(); return; }
        if (!flying) return;
        vy += 520 * dt; bx2 += vx * dt; by2 += vy * dt;
        // rim points
        for (float rx : new float[]{hx - 24, hx + 24}) {
            float dx = bx2 - rx, dy = by2 - hy, d = (float) Math.hypot(dx, dy);
            if (d < BR + 2 && d > 0) { float nx = dx / d, ny = dy / d; bx2 = rx + nx * (BR + 2); by2 = hy + ny * (BR + 2); float dot = vx * nx + vy * ny; if (dot < 0) { vx -= 1.6f * dot * nx; vy -= 1.6f * dot * ny; } Sound.play("minecraft:block.chain.hit", 1.4f, 0.3f); }
        }
        // backboard
        if (bx2 + BR > hx + 34 && by2 > hy - 60 && by2 < hy + 10 && vx > 0) { bx2 = hx + 34 - BR; vx = -vx * 0.6f; }
        if (!scored && vy > 0 && Math.abs(bx2 - hx) < 18 && Math.abs(by2 - hy) < 6) {
            scored = true; score += Math.hypot(hx - 70, hy - 220) > 260 ? 3 : 2; swish = 1; Sound.play("minecraft:entity.player.levelup", 1.5f, 0.3f);
        }
        if (by2 > H + 20 || bx2 > W + 20 || bx2 < -20) { if (scored) { hx = 220 + rnd.nextFloat() * 140; hy = 60 + rnd.nextFloat() * 90; } newBall(); }
    }

    @Override protected void render() {
        boxV(0, 0, W, H, 0, 0xFF203A60, 0xFF101A30);
        box(0, H - 30, W, 30, 0, 0xFFC8873A);
        box(hx + 34, hy - 60, 5, 70, 2, 0xFFF4F4F4);
        box(hx + 37, hy + 10, 4, H - 30 - hy - 10, 0, 0xFF5A6270);
        for (int i = 0; i < 5; i++) line(hx - 22 + i * 4, hy, hx - 14 + i * 7, hy + 22, 0.8f, 0xCCFFFFFF);
        line(hx - 24, hy, hx + 24, hy, 2.2f, 0xFFFF6A00);
        if (drag) { line(bx2, by2, bx2 + (dsx - bmx()), by2 + (dsy - bmy()), 1.2f, 0x88FFFFFF); }
        circle(bx2, by2, BR, 0xFFE8742A);
        line(bx2 - BR, by2, bx2 + BR, by2, 0.8f, 0xFF3A1A0A);
        if (swish > 0) text("Korb!", hx, hy - 30 - (1 - swish) * 20, 14, Fonts.BOLD, Gx.withAlpha(0xFFFFCD03, swish));
        textL(String.format(java.util.Locale.ROOT, "%.0f s", left), 8, 12, 9, Fonts.BOLD, left < 10 ? 0xFFFF4D5E : 0xFFFFFFFF);
        text("Ball ziehen und loslassen", 120, H - 15, 6.6f, Fonts.MEDIUM, 0xFF3A1A0A);
    }
}

// ====================================================================== Minigolf
final class MiniGolf extends GameView {
    private static final float W = 400, H = 280, BR = 5, HR = 8;
    private static final float[][][] WALLS = {
            {}, {{180, 60, 20, 160}}, {{120, 0, 20, 180}, {260, 100, 20, 180}}, {{150, 90, 100, 20}, {150, 170, 100, 20}},
            {{100, 60, 20, 220}, {200, 0, 20, 200}, {300, 80, 20, 200}}, {{170, 100, 60, 80}}};
    private static final float[][] START = {{50, 140}, {50, 220}, {50, 40}, {50, 140}, {50, 240}, {50, 50}}, HOLE = {{350, 140}, {350, 60}, {350, 240}, {350, 140}, {360, 40}, {350, 230}};
    private int hole, strokes, total;
    private float bxx, byy, vx, vy, sunk = -1;
    private boolean drag;

    MiniGolf() { super("minigolf"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Schläge"; }
    @Override protected String overTitle() { return "Runde vorbei: " + total + " Schläge"; }

    @Override protected void reset() { hole = 0; total = 0; place(); }
    private void place() { bxx = START[hole][0]; byy = START[hole][1]; vx = vy = 0; strokes = 0; sunk = -1; }

    private boolean moving() { return Math.hypot(vx, vy) > 2; }

    @Override protected void onClick(float x, float y, int b) { if (!moving() && sunk < 0) drag = true; }
    @Override protected void onRelease(float x, float y, int b) {
        if (!drag) return;
        drag = false;
        float dx = bxx - x, dy = byy - y, d = (float) Math.min(120, Math.hypot(dx, dy));
        if (d < 5) return;
        float n = (float) Math.hypot(dx, dy);
        vx = dx / n * d * 3.2f; vy = dy / n * d * 3.2f;
        strokes++; total++; score = total;
        Sound.play("minecraft:block.wood.hit", 1.5f, 0.4f);
    }

    @Override protected void update(float dt) {
        if (sunk >= 0) { if ((sunk += dt) > 1) { hole++; if (hole >= START.length) gameOver(); else place(); } return; }
        bxx += vx * dt; byy += vy * dt;
        float f = (float) Math.pow(0.35, dt);
        vx *= f; vy *= f;
        if (!moving()) { vx = vy = 0; }
        if (bxx < BR) { bxx = BR; vx = -vx; } if (bxx > W - BR) { bxx = W - BR; vx = -vx; }
        if (byy < BR) { byy = BR; vy = -vy; } if (byy > H - BR) { byy = H - BR; vy = -vy; }
        for (float[] w : WALLS[hole]) {
            if (bxx + BR > w[0] && bxx - BR < w[0] + w[2] && byy + BR > w[1] && byy - BR < w[1] + w[3]) {
                float ol = bxx + BR - w[0], orr = w[0] + w[2] - (bxx - BR), ot = byy + BR - w[1], ob = w[1] + w[3] - (byy - BR);
                float m = Math.min(Math.min(ol, orr), Math.min(ot, ob));
                if (m == ol) { bxx -= ol; vx = -Math.abs(vx); } else if (m == orr) { bxx += orr; vx = Math.abs(vx); } else if (m == ot) { byy -= ot; vy = -Math.abs(vy); } else { byy += ob; vy = Math.abs(vy); }
                Sound.play("minecraft:block.wood.hit", 1.8f, 0.2f);
            }
        }
        float[] h = HOLE[hole];
        if (Math.hypot(bxx - h[0], byy - h[1]) < HR && Math.hypot(vx, vy) < 260) { sunk = 0; Sound.play("minecraft:entity.player.levelup", 1.5f, 0.3f); }
    }

    @Override protected void render() {
        box(0, 0, W, H, 8, 0xFF3E9E4A);
        for (int i = 0; i < 20; i++) box((i * 41) % W, (i * 67) % H, 30, 2, 1, 0x1AFFFFFF);
        for (float[] w : WALLS[hole]) { box(w[0], w[1], w[2], w[3], 3, 0xFF8A5A30); box(w[0] + 2, w[1] + 2, w[2] - 4, w[3] - 4, 2, 0xFFA87040); }
        float[] h = HOLE[hole];
        circle(h[0], h[1], HR, 0xFF101010);
        line(h[0], h[1], h[0], h[1] - 28, 1, 0xFFF4F4F4);
        box(h[0], h[1] - 28, 12, 7, 0, 0xFFE3000B);
        if (drag) { float dx = bxx - bmx(), dy = bymY(); float n = (float) Math.hypot(dx, dy); float d = Math.min(120, n); if (n > 0) line(bxx, byy, bxx + dx / n * d * 0.6f, byy + dy / n * d * 0.6f, 1.6f, Gx.mix(0xFFFFFFFF, 0xFFFF3B4C, d / 120)); }
        if (sunk < 0) circle(bxx, byy, BR, 0xFFFFFFFF);
        textL("Loch " + (hole + 1) + "/" + START.length + " · Schläge " + strokes, 8, 10, 7.5f, Fonts.BOLD, 0xFFFFFFFF);
    }

    private float bymY() { return byy - bmy(); }
}

// ====================================================================== Bogenschießen
final class Archery extends GameView {
    private static final float W = 400, H = 260, TX = 340, TY = 130;
    private float aimY = 130, draw, ax, ay, avx, avy, wind, msgT;
    private boolean drawing, flying;
    private int arrows;
    private String msg = "";
    private final List<float[]> hits = new ArrayList<>();

    Archery() { super("archery"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String overTitle() { return "Köcher leer"; }

    @Override protected void reset() { arrows = 10; flying = false; hits.clear(); wind = (rnd.nextFloat() - 0.5f) * 60; }

    @Override protected void onClick(float x, float y, int b) { if (!flying) { drawing = true; draw = 0; } }
    @Override protected void onRelease(float x, float y, int b) {
        if (!drawing) return;
        drawing = false;
        float p = 260 + draw * 330;
        float ang = (float) Math.atan2(aimY - 150, 300);
        ax = 50; ay = 150; avx = (float) Math.cos(ang) * p; avy = (float) Math.sin(ang) * p;
        flying = true; arrows--;
        Sound.play("minecraft:entity.arrow.shoot", 1f, 0.4f);
    }

    @Override protected void update(float dt) {
        msgT = Math.max(0, msgT - dt);
        aimY = Math.max(20, Math.min(H - 20, bmy()));
        if (drawing) draw = Math.min(1, draw + dt * 0.9f);
        if (!flying) { if (arrows <= 0) gameOver(); return; }
        avy += 220 * dt; avx += 0; ay += wind * dt * 0.4f;
        ax += avx * dt; ay += avy * dt;
        if (ax >= TX) {
            float d = Math.abs(ay - TY);
            int pts = d < 6 ? 10 : d < 14 ? 8 : d < 24 ? 6 : d < 34 ? 4 : d < 45 ? 2 : 0;
            score += pts;
            msg = pts == 10 ? "Volltreffer! +10" : pts > 0 ? "+" + pts : "Daneben";
            msgT = 1.2f;
            if (pts > 0) hits.add(new float[]{TX, ay});
            Sound.play(pts > 0 ? "minecraft:entity.arrow.hit" : "minecraft:block.note_block.bass", 1.2f, 0.4f);
            flying = false;
            wind = (rnd.nextFloat() - 0.5f) * 60;
        } else if (ay > H || ay < -40) { flying = false; msg = "Daneben"; msgT = 1; wind = (rnd.nextFloat() - 0.5f) * 60; }
    }

    @Override protected void render() {
        boxV(0, 0, W, H, 0, 0xFF8AC8FF, 0xFFCFEFFF);
        box(0, 200, W, 60, 0, 0xFF5DBB4E);
        box(TX - 2, TY + 50, 4, 70, 0, 0xFF6A4A2A);
        int[] cols = {0xFFFFFFFF, 0xFF1B1B22, 0xFF3FA9FF, 0xFFE3000B, 0xFFFFCD03};
        float[] rs = {45, 34, 24, 14, 6};
        for (int i = 0; i < 5; i++) box(TX - 4, TY - rs[i], 8, rs[i] * 2, 3, cols[i]);
        for (float[] h : hits) line(h[0] - 14, h[1], h[0], h[1], 1.2f, 0xFF6A4A2A);
        // bow
        line(50, 110, 44, 150, 2, 0xFF8A5A30); line(44, 150, 50, 190, 2, 0xFF8A5A30);
        float pull = drawing ? draw * 16 : 0;
        line(50, 110, 50 - pull, 150, 0.7f, 0xFFF4F4F4); line(50 - pull, 150, 50, 190, 0.7f, 0xFFF4F4F4);
        if (!flying) { float ang = (float) Math.atan2(aimY - 150, 300); line(50 - pull, 150, 50 - pull + (float) Math.cos(ang) * 34, 150 + (float) Math.sin(ang) * 34, 1.3f, 0xFF6A4A2A); }
        else { float n = (float) Math.hypot(avx, avy); line(ax - avx / n * 16, ay - avy / n * 16, ax, ay, 1.3f, 0xFF6A4A2A); }
        if (drawing) { box(20, 230, 80, 8, 4, 0x55000000); box(20, 230, 80 * draw, 8, 4, Gx.mix(0xFF39D353, 0xFFFF3B4C, draw)); }
        textL("Pfeile: " + arrows, 8, 12, 8, Fonts.BOLD, 0xFF1B1B22);
        textL("Wind: " + (wind > 0 ? "↓ " : "↑ ") + Math.round(Math.abs(wind)), 8, 26, 7, Fonts.MEDIUM, 0xFF1B1B22);
        if (msgT > 0) text(msg, W / 2, 40, 13, Fonts.BOLD, Gx.withAlpha(0xFF1B1B22, Math.min(1, msgT)));
    }
}

// ====================================================================== Darts
final class Darts extends GameView {
    private static final float W = 360, H = 280, CX = 180, CY = 140, R = 115;
    private static final int[] NUM = {20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5};
    private int darts;
    private final List<float[]> stuck = new ArrayList<>();
    private String msg = "";
    private float msgT, wob;

    Darts() { super("darts"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String overTitle() { return "9 Darts geworfen"; }

    @Override protected void reset() { darts = 9; stuck.clear(); }

    private float sx() { return bmx() + (float) Math.sin(time * 2.3) * 9 + (float) Math.sin(time * 5.1) * 4; }
    private float sy() { return bmy() + (float) Math.cos(time * 1.9) * 9 + (float) Math.sin(time * 4.3) * 4; }

    private int points(float x, float y) {
        float dx = x - CX, dy = y - CY, d = (float) Math.hypot(dx, dy) / R;
        if (d < 0.05f) return 50;
        if (d < 0.1f) return 25;
        if (d > 1) return 0;
        double a = Math.toDegrees(Math.atan2(dx, -dy));
        int seg = (int) Math.floor(((a + 9 + 360) % 360) / 18);
        int v = NUM[seg];
        if (d > 0.93f) return v * 2;
        if (d > 0.55f && d < 0.62f) return v * 3;
        return v;
    }

    @Override protected void onClick(float x, float y, int b) {
        if (darts <= 0) return;
        float hx = sx(), hy = sy();
        int p = points(hx, hy);
        score += p; darts--;
        stuck.add(new float[]{hx, hy});
        msg = p == 0 ? "Daneben" : p == 50 ? "BULLSEYE! 50" : String.valueOf(p);
        msgT = 1;
        Sound.play("minecraft:entity.arrow.hit", 1.3f, 0.4f);
        if (darts == 0) wob = 1.2f;
    }

    @Override protected void update(float dt) { msgT = Math.max(0, msgT - dt); if (wob > 0 && (wob -= dt) <= 0) gameOver(); }

    @Override protected void render() {
        box(0, 0, W, H, 0, 0xFF2A1A12);
        circle(CX, CY, R + 14, 0xFF1B1B22);
        int px = Math.max(8, S(2 * R));
        Gx.Img img = dev.lego.ui.Thumbs.get("dartboard:" + px, px, px, () -> {
            int[] out = new int[px * px];
            for (int yy = 0; yy < px; yy++) for (int xx = 0; xx < px; xx++) {
                float dx = (xx + 0.5f) / px * 2 - 1, dy = (yy + 0.5f) / px * 2 - 1, d = (float) Math.hypot(dx, dy);
                if (d > 1) continue;
                int seg = (int) Math.floor(((Math.toDegrees(Math.atan2(dx, -dy)) + 9 + 360) % 360) / 18);
                int base = seg % 2 == 0 ? 0xFF1B1B22 : 0xFFF2E6C8, ring = seg % 2 == 0 ? 0xFFE3000B : 0xFF00852B;
                out[yy * px + xx] = d < 0.05f ? 0xFFE3000B : d < 0.1f ? 0xFF00852B : d > 0.93f || (d > 0.55f && d < 0.62f) ? ring : base;
            }
            return out;
        });
        if (img != null) Gx.image(img, X(CX - R), Y(CY - R), px, px, 0xFFFFFFFF);
        for (int i = 0; i < 20; i++) {
            double am = Math.toRadians(i * 18 - 90);
            text(String.valueOf(NUM[i]), CX + (float) Math.cos(am) * (R + 8), CY + (float) Math.sin(am) * (R + 8), 6, Fonts.BOLD, 0xFFFFFFFF);
        }
        for (float[] s : stuck) { circle(s[0], s[1], 2.2f, 0xFFFFCD03); line(s[0], s[1], s[0] + 6, s[1] - 8, 1, 0xFF9AA3AD); }
        if (darts > 0) { float x = sx(), y = sy(); ring(x, y, 5, 1, 0xFFFFFFFF); line(x - 8, y, x + 8, y, 0.6f, 0xFFFFFFFF); line(x, y - 8, x, y + 8, 0.6f, 0xFFFFFFFF); }
        textL("Darts: " + darts, 8, 12, 8, Fonts.BOLD, 0xFFFFFFFF);
        if (msgT > 0) text(msg, W - 60, 20, 12, Fonts.BOLD, Gx.withAlpha(0xFFFFCD03, msgT));
    }
}
