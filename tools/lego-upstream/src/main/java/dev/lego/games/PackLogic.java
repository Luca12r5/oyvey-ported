package dev.lego.games;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Spiele-Paket 4e: Logik, Brettspiele, Geschick. */
final class PackLogic {
    private PackLogic() { }
}

// ====================================================================== Schwerkraft-Lauf
final class GravityRun extends GameView {
    private static final float W = 400, H = 240, PX = 70, PS = 14;
    private final List<float[]> obs = new ArrayList<>();   // x, top(1)/bottom(0), h
    private float py, vy, spawn, speed;
    private boolean ceiling;

    GravityRun() { super("gravityrun"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Meter"; }

    @Override protected void reset() { obs.clear(); py = H - 20 - PS; vy = 0; ceiling = false; spawn = 1; speed = 150; }

    private void flip() { if (Math.abs(vy) < 1) { ceiling = !ceiling; vy = ceiling ? -1 : 1; Sound.play("minecraft:block.note_block.hat", ceiling ? 1.8f : 1.2f, 0.3f); } }
    @Override protected void onClick(float x, float y, int b) { flip(); }
    @Override protected void onKey(int k) { if (k == K_SPACE || k == K_UP || k == K_W || k == K_DOWN || k == K_S) flip(); }

    @Override protected void update(float dt) {
        speed += dt * 4;
        score = (long) (time * speed / 30);
        vy += (ceiling ? -900 : 900) * dt;
        py += vy * dt;
        if (py < 20) { py = 20; vy = 0; } if (py > H - 20 - PS) { py = H - 20 - PS; vy = 0; }
        spawn -= dt;
        if (spawn <= 0) { spawn = 0.7f + rnd.nextFloat() * 0.8f - Math.min(0.4f, time / 120); obs.add(new float[]{W + 10, rnd.nextBoolean() ? 1 : 0, 26 + rnd.nextFloat() * 60}); }
        for (float[] o : obs) o[0] -= speed * dt;
        obs.removeIf(o -> o[0] < -30);
        for (float[] o : obs) {
            float top = o[1] == 1 ? 20 : H - 20 - o[2], bot = top + o[2];
            if (PX + PS > o[0] && PX < o[0] + 18 && py + PS > top && py < bot) { PackArcade.boom(); gameOver(); return; }
        }
    }

    @Override protected void render() {
        boxV(0, 0, W, H, 0, 0xFF0A0A1A, 0xFF1A0A2A);
        box(0, 0, W, 20, 0, 0xFF3A2A5A); box(0, H - 20, W, 20, 0, 0xFF3A2A5A);
        for (float[] o : obs) { float top = o[1] == 1 ? 20 : H - 20 - o[2]; box(o[0], top, 18, o[2], 3, 0xFFFF2A6A); }
        brick(PX, py, PS, PS, 0xFF3AD8FF);
        text(score + " m", W - 40, 10, 7, Fonts.BOLD, 0xFFFFFFFF);
    }
}

// ====================================================================== Kugel-Labyrinth
final class BallMaze extends GameView {
    private static final int N = 11;
    private static final float C = 24, OX = 8, OY = 8;
    private boolean[][] wall;
    private float bx2, by2, vx, vy;
    private int level;
    private final List<int[]> holes = new ArrayList<>();

    BallMaze() { super("ballmaze"); }
    @Override protected float boardW() { return N * C + 16 + 110; }
    @Override protected float boardH() { return N * C + 16; }
    @Override protected String scoreLabel() { return "Level"; }
    @Override protected String overTitle() { return "In ein Loch gefallen"; }

    @Override protected void reset() { level = 0; next(); }

    private void next() {
        level++; score = level - 1;
        wall = new boolean[N][N];
        for (boolean[] r : wall) java.util.Arrays.fill(r, true);
        carve(1, 1);
        holes.clear();
        for (int i = 0; i < Math.min(8, level); i++) { int r = 1 + rnd.nextInt(N - 2), c = 1 + rnd.nextInt(N - 2); if (!wall[r][c] && !(r <= 2 && c <= 2) && !(r >= N - 3 && c >= N - 3)) holes.add(new int[]{r, c}); }
        bx2 = OX + C * 1.5f; by2 = OY + C * 1.5f; vx = vy = 0;
    }

    private void carve(int r, int c) {
        wall[r][c] = false;
        int[][] d = {{0, 2}, {0, -2}, {2, 0}, {-2, 0}};
        for (int i = 3; i > 0; i--) { int j = rnd.nextInt(i + 1); int[] t = d[i]; d[i] = d[j]; d[j] = t; }
        for (int[] q : d) { int rr = r + q[0], cc = c + q[1]; if (rr > 0 && cc > 0 && rr < N - 1 && cc < N - 1 && wall[rr][cc]) { wall[r + q[0] / 2][c + q[1] / 2] = false; carve(rr, cc); } }
    }

    private boolean solid(float x, float y) { int c = (int) ((x - OX) / C), r = (int) ((y - OY) / C); return r < 0 || c < 0 || r >= N || c >= N || wall[r][c]; }

    @Override protected void update(float dt) {
        // tilt follows the mouse position relative to the ball
        float tx = Math.max(-1, Math.min(1, (bmx() - bx2) / 80)), ty = Math.max(-1, Math.min(1, (bmy() - by2) / 80));
        vx += tx * 500 * dt; vy += ty * 500 * dt;
        vx *= 1 - 1.5f * dt; vy *= 1 - 1.5f * dt;
        float r = 6, nx = bx2 + vx * dt, ny = by2 + vy * dt;
        if (solid(nx - r, by2) || solid(nx + r, by2) || solid(nx - r, by2 - r + 1) || solid(nx + r, by2 + r - 1)) vx = -vx * 0.3f; else bx2 = nx;
        if (solid(bx2, ny - r) || solid(bx2, ny + r) || solid(bx2 - r + 1, ny - r) || solid(bx2 + r - 1, ny + r)) vy = -vy * 0.3f; else by2 = ny;
        for (int[] h : holes) if (Math.hypot(bx2 - (OX + h[1] * C + C / 2), by2 - (OY + h[0] * C + C / 2)) < 7) { PackPuzzle.bad(); gameOver(); return; }
        if (Math.hypot(bx2 - (OX + (N - 1.5f) * C), by2 - (OY + (N - 1.5f) * C)) < 9) { PackPuzzle.win(); next(); }
    }

    @Override protected void render() {
        box(OX, OY, N * C, N * C, 6, 0xFFE0C08A);
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) if (wall[r][c]) box(OX + c * C, OY + r * C, C, C, 2, 0xFF8A5A30);
        for (int[] h : holes) circle(OX + h[1] * C + C / 2, OY + h[0] * C + C / 2, 8, 0xFF101010);
        circle(OX + (N - 1.5f) * C, OY + (N - 1.5f) * C, 9, 0xFF39D353);
        circle(bx2, by2, 6, 0xFFB8C0CA); circle(bx2 - 2, by2 - 2, 2, 0xFFFFFFFF);
        float sx = N * C + 26;
        textL("Level " + level, sx, 30, 9, Fonts.BOLD, Style.text);
        textL("Maus = neigen", sx, 60, 6.6f, Fonts.MEDIUM, Style.sub);
        textL("Ins grüne Ziel,", sx, 80, 6.6f, Fonts.MEDIUM, Style.sub);
        textL("nicht in die Löcher", sx, 92, 6.6f, Fonts.MEDIUM, Style.sub);
    }
}

// ====================================================================== Kistenschieber (Sokoban)
final class Sokoban extends GameView {
    private static final String[][] LEVELS = {
            {"#######", "#.    #", "# $   #", "#  @  #", "#     #", "#######"},
            {"########", "#  .   #", "# $$ . #", "#  @   #", "#      #", "########"},
            {"#######", "#. .  #", "# $#$ #", "#  @  #", "# $ . #", "#######"},
            {"  #####", "###   #", "#.@$  #", "### $.#", "#.##$ #", "# # . ##", "#$ *$$.#", "#   .  #", "########"},
            {"########", "#      #", "# .**. #", "# $  $ #", "#  @   #", "########"},
            {"#########", "#   #   #", "# $ . $ #", "##.#@#.##", "# $ . $ #", "#   #   #", "#########"},
    };
    private char[][] map;
    private boolean[][] goal;
    private int pr, pc, level, moves;
    private float doneT = -1;

    Sokoban() { super("sokoban"); }
    @Override protected float boardW() { return 400; }
    @Override protected float boardH() { return 270; }
    @Override protected String scoreLabel() { return "Level"; }
    @Override protected String overTitle() { return "Alle Level geschafft!"; }

    @Override protected void reset() { level = 0; score = 0; load(); }

    private void load() {
        String[] l = LEVELS[level];
        int w = 0; for (String s : l) w = Math.max(w, s.length());
        map = new char[l.length][w]; goal = new boolean[l.length][w];
        for (int r = 0; r < l.length; r++) for (int c = 0; c < w; c++) {
            char ch = c < l[r].length() ? l[r].charAt(c) : ' ';
            if (ch == '.' || ch == '*' || ch == '+') goal[r][c] = true;
            if (ch == '@' || ch == '+') { pr = r; pc = c; ch = ' '; }
            if (ch == '*') ch = '$';
            if (ch == '.') ch = ' ';
            map[r][c] = ch;
        }
        moves = 0; doneT = -1;
    }

    private boolean done() { for (int r = 0; r < map.length; r++) for (int c = 0; c < map[0].length; c++) if (goal[r][c] && map[r][c] != '$') return false; return true; }

    private void move(int dr, int dc) {
        if (doneT >= 0) return;
        int r = pr + dr, c = pc + dc;
        if (map[r][c] == '#') return;
        if (map[r][c] == '$') { int r2 = r + dr, c2 = c + dc; if (map[r2][c2] != ' ') return; map[r2][c2] = '$'; map[r][c] = ' '; Sound.play("minecraft:block.stone.place", 1.2f, 0.3f); }
        pr = r; pc = c; moves++;
        if (done()) { doneT = 0; score = level + 1; PackPuzzle.win(); }
    }

    @Override protected void onKey(int k) {
        if (k == K_UP || k == K_W) move(-1, 0); else if (k == K_DOWN || k == K_S) move(1, 0); else if (k == K_LEFT || k == K_A) move(0, -1); else if (k == K_RIGHT || k == K_D) move(0, 1);
        else if (k == K_R) load();
    }

    @Override protected void update(float dt) { if (doneT >= 0 && (doneT += dt) > 1) { level++; if (level >= LEVELS.length) gameOver(); else load(); } }

    @Override protected void render() {
        float C = Math.min(28, Math.min(380f / map[0].length, 230f / map.length)), ox = 200 - map[0].length * C / 2, oy = 125 - map.length * C / 2;
        for (int r = 0; r < map.length; r++) for (int c = 0; c < map[0].length; c++) {
            float x = ox + c * C, y = oy + r * C;
            if (map[r][c] == '#') { brick(x, y, C, C, 0xFF8A5A30); continue; }
            box(x, y, C, C, 0, Style.light ? 0xFFE6E8EE : 0xFF1C1F27);
            if (goal[r][c]) circle(x + C / 2, y + C / 2, C * 0.18f, 0xFF39D353);
            if (map[r][c] == '$') brick(x + 2, y + 2, C - 4, C - 4, goal[r][c] ? 0xFF2E9E4A : 0xFFFF8A00);
        }
        float x = ox + pc * C, y = oy + pr * C;
        circle(x + C / 2, y + C / 2, C * 0.36f, 0xFFFFCD03); circle(x + C / 2 - 3, y + C / 2 - 2, 1.5f, 0xFF1B1B22); circle(x + C / 2 + 3, y + C / 2 - 2, 1.5f, 0xFF1B1B22);
        textL("Level " + (level + 1) + "/" + LEVELS.length + " · Züge " + moves + " · R = neu", 8, 260, 6.8f, Fonts.BOLD, Style.sub);
        if (doneT >= 0) text("Geschafft!", 200, 30, 14, Fonts.BOLD, Style.ok);
    }
}

// ====================================================================== Nonogramm
final class Nonogram extends GameView {
    private static final int N = 8;
    private static final float C = 24, OX = 110, OY = 66;
    private boolean[][] sol = new boolean[N][N];
    private int[][] mark = new int[N][N];   // 0 leer, 1 voll, 2 Kreuz
    private final List<List<Integer>> rowClues = new ArrayList<>(), colClues = new ArrayList<>();
    private int mistakes, solved;

    Nonogram() { super("nonogram"); }
    @Override protected float boardW() { return 340; }
    @Override protected float boardH() { return 270; }
    @Override protected String scoreLabel() { return "Rätsel"; }
    @Override protected String overTitle() { return "3 Fehler - Rätsel verloren"; }

    @Override protected void reset() { solved = 0; mistakes = 0; generate(); }

    private void generate() {
        sol = new boolean[N][N]; mark = new int[N][N];
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) sol[r][c] = rnd.nextFloat() < 0.58f;
        rowClues.clear(); colClues.clear();
        for (int r = 0; r < N; r++) { List<Integer> l = new ArrayList<>(); int run = 0; for (int c = 0; c < N; c++) { if (sol[r][c]) run++; else if (run > 0) { l.add(run); run = 0; } } if (run > 0) l.add(run); rowClues.add(l); }
        for (int c = 0; c < N; c++) { List<Integer> l = new ArrayList<>(); int run = 0; for (int r = 0; r < N; r++) { if (sol[r][c]) run++; else if (run > 0) { l.add(run); run = 0; } } if (run > 0) l.add(run); colClues.add(l); }
    }

    private boolean complete() { for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) if (sol[r][c] && mark[r][c] != 1) return false; return true; }

    @Override protected void onClick(float x, float y, int b) {
        int c = (int) ((x - OX) / C), r = (int) ((y - OY) / C);
        if (x < OX || y < OY || c >= N || r >= N || mark[r][c] != 0) return;
        if (b == 1) { mark[r][c] = 2; return; }
        if (sol[r][c]) { mark[r][c] = 1; PackPuzzle.click(); if (complete()) { solved++; score = solved; PackPuzzle.win(); generate(); } }
        else { mark[r][c] = 2; mistakes++; PackPuzzle.bad(); if (mistakes >= 3) gameOver(); }
    }

    @Override protected void update(float dt) { }

    @Override protected void render() {
        for (int r = 0; r < N; r++) {
            List<Integer> l = rowClues.get(r);
            String s = l.isEmpty() ? "0" : l.toString().replaceAll("[\\[\\],]", "");
            Gx.textMid(s, X(OX - 6 - Gx.width(s, 7 * u, Fonts.BOLD) / u), Y(OY + r * C + C / 2), 7 * u, Fonts.BOLD, Style.text);
        }
        for (int c = 0; c < N; c++) { List<Integer> l = colClues.get(c); if (l.isEmpty()) l = List.of(0); for (int i = 0; i < l.size(); i++) text(String.valueOf(l.get(i)), OX + c * C + C / 2, OY - 8 - (l.size() - 1 - i) * 10, 7, Fonts.BOLD, Style.text); }
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) {
            float x = OX + c * C, y = OY + r * C;
            box(x + 1, y + 1, C - 2, C - 2, 3, mark[r][c] == 1 ? Style.accent : (Style.light ? 0xFFE6E8EE : 0xFF22252E));
            if (mark[r][c] == 2) { line(x + 7, y + 7, x + C - 7, y + C - 7, 1.5f, Style.muted); line(x + C - 7, y + 7, x + 7, y + C - 7, 1.5f, Style.muted); }
        }
        textL("Fehler: " + mistakes + "/3", 8, 12, 7, Fonts.BOLD, mistakes > 0 ? Style.danger : Style.sub);
        textL("Gelöst: " + solved, 8, 26, 7, Fonts.BOLD, Style.sub);
        textL("Rechtsklick = Kreuz", 8, 40, 6, Fonts.MEDIUM, Style.muted);
    }
}

// ====================================================================== Rechne 24
final class Make24 extends GameView {
    private final List<Double> nums = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();
    private int sel = -1, op = -1, solved;
    private float left, flash;
    private int[] start;

    Make24() { super("make24"); }
    @Override protected float boardW() { return 400; }
    @Override protected float boardH() { return 250; }
    @Override protected String scoreLabel() { return "Gelöst"; }
    @Override protected String overTitle() { return "Zeit um!"; }

    @Override protected void reset() { solved = 0; left = 120; deal(); }

    private static boolean solvable(List<Double> n) {
        if (n.size() == 1) return Math.abs(n.get(0) - 24) < 1e-6;
        for (int i = 0; i < n.size(); i++) for (int j = 0; j < n.size(); j++) {
            if (i == j) continue;
            List<Double> rest = new ArrayList<>();
            for (int k = 0; k < n.size(); k++) if (k != i && k != j) rest.add(n.get(k));
            double a = n.get(i), b = n.get(j);
            double[] cand = {a + b, a - b, a * b, b != 0 ? a / b : Double.NaN};
            for (double v : cand) { if (Double.isNaN(v)) continue; rest.add(v); if (solvable(rest)) return true; rest.remove(rest.size() - 1); }
        }
        return false;
    }

    private void deal() {
        List<Double> n;
        do { n = new ArrayList<>(); start = new int[4]; for (int i = 0; i < 4; i++) { start[i] = 1 + rnd.nextInt(9); n.add((double) start[i]); } } while (!solvable(n));
        restart();
    }

    private void restart() { nums.clear(); labels.clear(); for (int v : start) { nums.add((double) v); labels.add(String.valueOf(v)); } sel = -1; op = -1; }

    private String fmt(double v) { return Math.abs(v - Math.rint(v)) < 1e-9 ? String.valueOf((long) Math.rint(v)) : String.format(Locale.ROOT, "%.2f", v); }

    @Override protected void onClick(float x, float y, int b) {
        for (int i = 0; i < nums.size(); i++) if (in(x, y, 40 + i * 85, 60, 70, 70)) {
            if (sel < 0 || op < 0 || i == sel) { sel = i; op = -1; PackPuzzle.click(); return; }
            double a = nums.get(sel), c = nums.get(i);
            double r = op == 0 ? a + c : op == 1 ? a - c : op == 2 ? a * c : c == 0 ? Double.NaN : a / c;
            if (Double.isNaN(r)) return;
            int keep = Math.min(sel, i), drop = Math.max(sel, i);
            nums.set(keep, r); labels.set(keep, fmt(r)); nums.remove(drop); labels.remove(drop);
            sel = keep; op = -1; PackPuzzle.click();
            if (nums.size() == 1) { if (Math.abs(r - 24) < 1e-6) { solved++; score = solved; flash = 0.8f; PackPuzzle.win(); deal(); } else { PackPuzzle.bad(); restart(); } }
            return;
        }
        for (int k = 0; k < 4; k++) if (in(x, y, 70 + k * 70, 150, 50, 36) && sel >= 0) { op = k; PackPuzzle.click(); }
        if (in(x, y, 70, 200, 110, 28)) restart();
        if (in(x, y, 220, 200, 110, 28)) { left -= 10; deal(); }
    }

    @Override protected void update(float dt) { flash = Math.max(0, flash - dt); left -= dt; if (left <= 0) { left = 0; gameOver(); } }

    @Override protected void render() {
        text("Mach aus den Zahlen genau 24", 200, 20, 9, Fonts.BOLD, Style.text);
        for (int i = 0; i < nums.size(); i++) { brick(40 + i * 85, 60, 70, 70, i == sel ? 0xFFFFCD03 : 0xFF006CB7); text(labels.get(i), 75 + i * 85, 97, labels.get(i).length() > 3 ? 12 : 20, Fonts.BOLD, 0xFFFFFFFF); }
        String[] ops = {"+", "−", "×", "÷"};
        for (int k = 0; k < 4; k++) button(ops[k], 70 + k * 70, 150, 50, 36, op == k ? 0xFFE3000B : 0xFF5A6270);
        button("Neu anfangen", 70, 200, 110, 28, 0xFF5A6270);
        button("Überspringen (−10 s)", 220, 200, 110, 28, 0xFF5A6270);
        text(String.format(Locale.ROOT, "%.0f s", left), 370, 20, 9, Fonts.BOLD, left < 15 ? Style.danger : Style.sub);
        if (flash > 0) text("24!", 200, 45, 14, Fonts.BOLD, Gx.withAlpha(0xFF39D353, flash / 0.8f));
    }
}

// ====================================================================== Schlangen-Duell
final class SnakeDuel extends GameView {
    private static final int GW = 30, GH = 20;
    private static final float C = 13;
    private final java.util.ArrayDeque<int[]> me = new java.util.ArrayDeque<>(), ai = new java.util.ArrayDeque<>();
    private int dx, dy, adx, ady, winner;
    private int[] food;
    private float step;
    private final java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();

    SnakeDuel() { super("snakeduel"); }
    @Override protected boolean hasAi() { return true; }
    @Override protected float boardW() { return GW * C; }
    @Override protected float boardH() { return GH * C; }
    @Override protected String scoreLabel() { return "Länge"; }
    @Override protected String overTitle() { return winner == 1 ? "Die KI-Schlange ist gecrasht!" : winner == 2 ? "Gecrasht!" : "Beide gecrasht"; }

    @Override protected void reset() {
        me.clear(); ai.clear(); q.clear();
        for (int i = 0; i < 4; i++) { me.addFirst(new int[]{3 + i, 5}); ai.addFirst(new int[]{GW - 4 - i, GH - 6}); }
        dx = 1; dy = 0; adx = -1; ady = 0; winner = 0; step = 0; placeFood();
    }

    private boolean occupied(int x, int y) { for (int[] p : me) if (p[0] == x && p[1] == y) return true; for (int[] p : ai) if (p[0] == x && p[1] == y) return true; return false; }
    private void placeFood() { do food = new int[]{rnd.nextInt(GW), rnd.nextInt(GH)}; while (occupied(food[0], food[1])); }
    private boolean free(int x, int y) { return x >= 0 && y >= 0 && x < GW && y < GH && !occupied(x, y); }

    @Override protected void onKey(int k) {
        int[] d = k == K_LEFT || k == K_A ? new int[]{-1, 0} : k == K_RIGHT || k == K_D ? new int[]{1, 0} : k == K_UP || k == K_W ? new int[]{0, -1} : k == K_DOWN || k == K_S ? new int[]{0, 1} : null;
        if (d != null && q.size() < 3) q.add(d);
    }

    private void aiDir() {
        int[] h = ai.peekFirst();
        int[][] dirs = {{adx, ady}, {ady, -adx}, {-ady, adx}};
        int lv = aiLevel();
        int[] best = dirs[0]; double bs = -1e9;
        for (int[] d : dirs) {
            int nx = h[0] + d[0], ny = h[1] + d[1];
            if (!free(nx, ny)) continue;
            double s = -(Math.abs(nx - food[0]) + Math.abs(ny - food[1]));
            if (lv >= 2) { int open = 0; for (int[] e : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) if (free(nx + e[0], ny + e[1])) open++; s += open * 2; }
            if (lv == 0) s += rnd.nextInt(12);
            if (s > bs) { bs = s; best = d; }
        }
        adx = best[0]; ady = best[1];
    }

    @Override protected void update(float dt) {
        step += dt;
        if (step < 0.11f) return;
        step = 0;
        if (!q.isEmpty()) { int[] d = q.poll(); if (d[0] != -dx || d[1] != -dy) { dx = d[0]; dy = d[1]; } }
        aiDir();
        int[] h = me.peekFirst(), a = ai.peekFirst();
        int nx = h[0] + dx, ny = h[1] + dy, ax = a[0] + adx, ay = a[1] + ady;
        boolean pDead = !free(nx, ny) || (nx == ax && ny == ay), aDead = !free(ax, ay) || (nx == ax && ny == ay);
        if (pDead || aDead) { winner = pDead && aDead ? 3 : pDead ? 2 : 1; if (winner == 1) PackPuzzle.win(); else PackArcade.boom(); score = me.size(); gameOver(); return; }
        me.addFirst(new int[]{nx, ny}); ai.addFirst(new int[]{ax, ay});
        boolean ate = false;
        if (nx == food[0] && ny == food[1]) { ate = true; Sound.play("minecraft:entity.experience_orb.pickup", 1.3f, 0.3f); } else me.removeLast();
        if (ax == food[0] && ay == food[1]) ate = true; else ai.removeLast();
        if (ate) placeFood();
        score = me.size();
    }

    @Override protected void render() {
        for (int y = 0; y < GH; y++) for (int x = 0; x < GW; x++) if ((x + y) % 2 == 0) box(x * C, y * C, C, C, 0, Style.light ? 0x0A000000 : 0x08FFFFFF);
        brick(food[0] * C + 2, food[1] * C + 2, C - 4, C - 4, 0xFFFFCD03);
        int i = 0; for (int[] p : me) { brick(p[0] * C + 1, p[1] * C + 1, C - 2, C - 2, i++ == 0 ? 0xFF3FA9FF : 0xFF006CB7); }
        i = 0; for (int[] p : ai) { brick(p[0] * C + 1, p[1] * C + 1, C - 2, C - 2, i++ == 0 ? 0xFFFF6A6A : 0xFFE3000B); }
        text("Du " + me.size() + " · KI " + ai.size(), GW * C / 2, 8, 6.5f, Fonts.BOLD, Style.sub);
    }
}

// ====================================================================== Squash
final class Squash extends GameView {
    private static final float W = 320, H = 300, PW = 60;
    private float bx2, by2, vx, vy, px;

    Squash() { super("squash"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Treffer"; }

    @Override protected void reset() { bx2 = W / 2; by2 = 100; vx = 120 * (rnd.nextBoolean() ? 1 : -1); vy = 160; px = W / 2; }

    @Override protected void update(float dt) {
        px += (Math.max(PW / 2, Math.min(W - PW / 2, bmx())) - px) * Math.min(1, dt * 18);
        bx2 += vx * dt; by2 += vy * dt;
        if (bx2 < 6) { bx2 = 6; vx = Math.abs(vx); } if (bx2 > W - 6) { bx2 = W - 6; vx = -Math.abs(vx); }
        if (by2 < 6) { by2 = 6; vy = Math.abs(vy); Sound.play("minecraft:block.wood.hit", 1.3f, 0.25f); }
        if (vy > 0 && by2 > H - 26 && by2 < H - 14 && Math.abs(bx2 - px) < PW / 2 + 5) {
            vy = -Math.abs(vy) * 1.04f; vx += (bx2 - px) * 4; score++;
            Sound.play("minecraft:block.note_block.hat", 1.2f + Math.min(0.8f, score * 0.03f), 0.4f);
        }
        if (by2 > H + 10) gameOver();
    }

    @Override protected void render() {
        box(0, 0, W, H, 0, 0xFF1C2A40);
        box(0, 0, W, 4, 0, 0xFFE3000B);
        box(px - PW / 2, H - 20, PW, 8, 4, 0xFF39D353);
        circle(bx2, by2, 6, 0xFFFFFFFF);
        text(String.valueOf(score), W / 2, H / 2, 30, Fonts.BOLD, 0x22FFFFFF);
    }
}

// ====================================================================== Abfahrtsski
final class Ski extends GameView {
    private static final float W = 320, H = 300;
    private final List<float[]> obj = new ArrayList<>();   // x y kind(0 tree,1 flag-left,2 rock) passed
    private float sx = W / 2, speed, spawn, lean;
    private int lives;

    Ski() { super("ski"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Meter"; }

    @Override protected void reset() { obj.clear(); sx = W / 2; speed = 120; spawn = 0; lives = 3; }

    @Override protected void update(float dt) {
        float tx = Math.max(10, Math.min(W - 10, bmx()));
        lean = Math.max(-1, Math.min(1, (tx - sx) / 40));
        sx += (tx - sx) * Math.min(1, dt * 5);
        speed = Math.min(360, speed + dt * 6);
        score = (long) (time * speed / 25);
        spawn -= dt * speed / 120;
        if (spawn <= 0) {
            spawn = 0.45f;
            float r = rnd.nextFloat();
            if (r < 0.25f) { float gx = 40 + rnd.nextFloat() * (W - 120); obj.add(new float[]{gx, H + 20, 1, 0}); }
            else obj.add(new float[]{rnd.nextFloat() * W, H + 20, r < 0.85f ? 0 : 2, 0});
        }
        for (float[] o : obj) o[1] -= speed * dt;
        for (float[] o : obj) {
            if (o[3] != 0) continue;
            if (o[2] == 1) {
                if (o[1] < 70) { o[3] = 1; if (sx > o[0] && sx < o[0] + 60) { score += 0; time += 1; Sound.play("minecraft:entity.experience_orb.pickup", 1.4f, 0.3f); } }
            } else if (Math.abs(o[0] - sx) < 9 && Math.abs(o[1] - 70) < 9) { o[3] = 1; lives--; speed *= 0.5f; PackPuzzle.bad(); if (lives <= 0) { gameOver(); return; } }
        }
        obj.removeIf(o -> o[1] < -30);
    }

    @Override protected void render() {
        box(0, 0, W, H, 0, 0xFFF4F8FF);
        for (float[] o : obj) {
            if (o[2] == 0) { box(o[0] - 1.5f, o[1] + 4, 3, 6, 0, 0xFF6A4A2A); circle(o[0], o[1], 9, 0xFF2E7D32); circle(o[0], o[1] - 6, 6, 0xFF3E9E4A); }
            else if (o[2] == 1) { line(o[0], o[1] - 12, o[0], o[1] + 4, 1, 0xFFE3000B); line(o[0] + 60, o[1] - 12, o[0] + 60, o[1] + 4, 1, 0xFF006CB7); box(o[0], o[1] - 12, 7, 5, 0, 0xFFE3000B); box(o[0] + 53, o[1] - 12, 7, 5, 0, 0xFF006CB7); }
            else { circle(o[0], o[1], 8, 0xFF8A8A92); circle(o[0] - 2, o[1] - 2, 3, 0xFFB0B0B8); }
        }
        line(sx - 6 + lean * 3, 78, sx + 6 + lean * 3, 62, 1.6f, 0xFF1B1B22);
        line(sx - 2 + lean * 3, 80, sx + 9 + lean * 3, 64, 1.6f, 0xFF1B1B22);
        circle(sx, 66, 5, 0xFFE3000B); circle(sx, 59, 3.5f, 0xFFFFD0A0);
        text(score + " m · Leben " + lives, W / 2, 14, 7, Fonts.BOLD, 0xFF1B1B22);
        text("Durch die Tore = Bonus", W / 2, H - 10, 6, Fonts.MEDIUM, 0xFF5A6270);
    }
}

// ====================================================================== Rohr-Puzzle
final class PipePuzzle extends GameView {
    private static final int N = 6;
    private static final float C = 40, OX = 20, OY = 14;
    /** bit mask of openings: 1 up, 2 right, 4 down, 8 left */
    private int[][] pipe = new int[N][N];
    private boolean[][] wet = new boolean[N][N];
    private int moves, solved;

    PipePuzzle() { super("pipes"); }
    @Override protected float boardW() { return 400; }
    @Override protected float boardH() { return 268; }
    @Override protected String scoreLabel() { return "Gelöst"; }
    @Override protected String overTitle() { return solved + " Rätsel gelöst"; }

    @Override protected void reset() { solved = 0; generate(); }

    private static int rot(int m) { return ((m << 1) | (m >> 3)) & 15; }

    private void generate() {
        pipe = new int[N][N];
        boolean[][] seen = new boolean[N][N];
        java.util.ArrayDeque<int[]> st = new java.util.ArrayDeque<>();
        st.push(new int[]{0, 0}); seen[0][0] = true;
        while (!st.isEmpty()) {
            int[] p = st.peek();
            List<int[]> opts = new ArrayList<>();
            int[][] d = {{-1, 0, 1, 4}, {0, 1, 2, 8}, {1, 0, 4, 1}, {0, -1, 8, 2}};
            for (int[] q : d) { int r = p[0] + q[0], c = p[1] + q[1]; if (r >= 0 && c >= 0 && r < N && c < N && !seen[r][c]) opts.add(new int[]{r, c, q[2], q[3]}); }
            if (opts.isEmpty()) { st.pop(); continue; }
            int[] o = opts.get(rnd.nextInt(opts.size()));
            pipe[p[0]][p[1]] |= o[2]; pipe[o[0]][o[1]] |= o[3]; seen[o[0]][o[1]] = true; st.push(new int[]{o[0], o[1]});
        }
        pipe[0][0] |= 8;   // source enters from the left
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) { int k = rnd.nextInt(4); for (int i = 0; i < k; i++) pipe[r][c] = rot(pipe[r][c]); }
        moves = 0; flow();
    }

    private void flow() {
        wet = new boolean[N][N];
        if ((pipe[0][0] & 8) == 0) return;
        java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
        q.add(new int[]{0, 0}); wet[0][0] = true;
        int[][] d = {{-1, 0, 1, 4}, {0, 1, 2, 8}, {1, 0, 4, 1}, {0, -1, 8, 2}};
        while (!q.isEmpty()) {
            int[] p = q.poll();
            for (int[] e : d) { int r = p[0] + e[0], c = p[1] + e[1]; if ((pipe[p[0]][p[1]] & e[2]) != 0 && r >= 0 && c >= 0 && r < N && c < N && (pipe[r][c] & e[3]) != 0 && !wet[r][c]) { wet[r][c] = true; q.add(new int[]{r, c}); } }
        }
    }

    private boolean allWet() { for (boolean[] r : wet) for (boolean b : r) if (!b) return false; return true; }

    @Override protected void onClick(float x, float y, int b) {
        int c = (int) ((x - OX) / C), r = (int) ((y - OY) / C);
        if (x < OX || y < OY || c >= N || r >= N) return;
        pipe[r][c] = rot(pipe[r][c]); if (b == 1) { pipe[r][c] = rot(rot(pipe[r][c])); }
        moves++; Sound.play("minecraft:block.iron_trapdoor.close", 1.6f, 0.2f);
        flow();
        if (allWet()) { solved++; score = solved; PackPuzzle.win(); generate(); }
    }

    @Override protected void onKey(int k) { if (k == K_ENTER) gameOver(); }
    @Override protected void update(float dt) { }

    @Override protected void render() {
        box(OX - 14, OY + C / 2 - 5, 14, 10, 2, 0xFF2E86DE);
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) {
            float x = OX + c * C, y = OY + r * C, cx = x + C / 2, cy = y + C / 2;
            box(x + 1, y + 1, C - 2, C - 2, 5, Style.light ? 0xFFE6E8EE : 0xFF1C1F27);
            int col = wet[r][c] ? 0xFF2E86DE : 0xFF9AA3AD, m = pipe[r][c];
            if ((m & 1) != 0) box(cx - 5, y, 10, C / 2, 0, col);
            if ((m & 4) != 0) box(cx - 5, cy, 10, C / 2, 0, col);
            if ((m & 8) != 0) box(x, cy - 5, C / 2, 10, 0, col);
            if ((m & 2) != 0) box(cx, cy - 5, C / 2, 10, 0, col);
            circle(cx, cy, 6, col);
        }
        int n = 0; for (boolean[] r : wet) for (boolean b : r) if (b) n++;
        textL("Wasser: " + n + "/" + N * N, 280, 40, 8, Fonts.BOLD, Style.text);
        textL("Gelöst: " + solved, 280, 60, 8, Fonts.BOLD, Style.sub);
        textL("Klick dreht ein Rohr.", 280, 100, 6.4f, Fonts.MEDIUM, Style.sub);
        textL("Alle Rohre müssen", 280, 112, 6.4f, Fonts.MEDIUM, Style.sub);
        textL("Wasser bekommen.", 280, 124, 6.4f, Fonts.MEDIUM, Style.sub);
        textL("Enter = beenden", 280, 150, 6.4f, Fonts.MEDIUM, Style.muted);
    }
}

// ====================================================================== Farb-Sortierer (Water Sort)
final class WaterSort extends GameView {
    private static final int CAP = 4;
    private final List<List<Integer>> tubes = new ArrayList<>();
    private int sel = -1, level, moves;
    private float doneT = -1;

    WaterSort() { super("watersort"); }
    @Override protected float boardW() { return 400; }
    @Override protected float boardH() { return 260; }
    @Override protected String scoreLabel() { return "Level"; }
    @Override protected String overTitle() { return "Aufgegeben"; }

    @Override protected void reset() { level = 0; next(); }

    private void next() {
        level++; score = level - 1;
        int colors = Math.min(6, 2 + level);
        List<Integer> all = new ArrayList<>();
        for (int c = 0; c < colors; c++) for (int i = 0; i < CAP; i++) all.add(c);
        do java.util.Collections.shuffle(all, rnd); while (sortedAlready(all, colors));
        tubes.clear();
        for (int t = 0; t < colors; t++) tubes.add(new ArrayList<>(all.subList(t * CAP, t * CAP + CAP)));
        tubes.add(new ArrayList<>()); tubes.add(new ArrayList<>());
        sel = -1; moves = 0; doneT = -1;
    }

    private boolean sortedAlready(List<Integer> all, int colors) { for (int t = 0; t < colors; t++) { int v = all.get(t * CAP); for (int i = 1; i < CAP; i++) if (all.get(t * CAP + i) != v) return false; } return true; }

    private boolean solved() { for (List<Integer> t : tubes) { if (t.isEmpty()) continue; if (t.size() != CAP) return false; for (int v : t) if (v != t.get(0)) return false; } return true; }

    private float tx(int i) { return 200 - tubes.size() * 22 + i * 44 + 8; }

    @Override protected void onClick(float x, float y, int b) {
        if (doneT >= 0) return;
        for (int i = 0; i < tubes.size(); i++) if (in(x, y, tx(i), 60, 28, 140)) {
            if (sel < 0) { if (!tubes.get(i).isEmpty()) sel = i; return; }
            if (sel == i) { sel = -1; return; }
            List<Integer> from = tubes.get(sel), to = tubes.get(i);
            int top = from.get(from.size() - 1);
            if (to.size() < CAP && (to.isEmpty() || to.get(to.size() - 1) == top)) {
                while (!from.isEmpty() && from.get(from.size() - 1) == top && to.size() < CAP) to.add(from.remove(from.size() - 1));
                moves++; Sound.play("minecraft:item.bucket.empty", 1.4f, 0.3f);
                if (solved()) { doneT = 0; PackPuzzle.win(); }
            } else PackPuzzle.bad();
            sel = -1; return;
        }
    }

    @Override protected void onKey(int k) { if (k == K_R) { level--; next(); } if (k == K_ENTER) gameOver(); }
    @Override protected void update(float dt) { if (doneT >= 0 && (doneT += dt) > 1) next(); }

    @Override protected void render() {
        for (int i = 0; i < tubes.size(); i++) {
            float x = tx(i), y = i == sel ? 48 : 60;
            box(x - 2, y - 2, 32, 144, 12, Style.light ? 0x33000000 : 0x55FFFFFF);
            box(x, y, 28, 140, 11, Style.light ? 0xFFF4F4F4 : 0xFF101218);
            List<Integer> t = tubes.get(i);
            for (int k = 0; k < t.size(); k++) box(x + 3, y + 140 - 3 - (k + 1) * 33, 22, 32, k == 0 ? 9 : 2, PackPuzzle.PAL[t.get(k)]);
        }
        text("Level " + level + " · Züge " + moves, 200, 20, 8, Fonts.BOLD, Style.text);
        text("Gieß gleiche Farben zusammen · R = neu mischen · Enter = beenden", 200, 230, 6.2f, Fonts.MEDIUM, Style.sub);
        if (doneT >= 0) text("Sortiert!", 200, 40, 12, Fonts.BOLD, Style.ok);
    }
}

// ====================================================================== Dame
final class Checkers extends GameView {
    private static final float C = 32, OX = 10, OY = 6;
    /** 0 leer, 1 deiner, 2 KI, +2 = Dame (3 / 4) */
    private int[] b = new int[64];
    private int sel = -1, turn, chainFrom = -1, ended;
    private float think;

    Checkers() { super("checkers"); }
    @Override protected boolean hasAi() { return true; }
    @Override protected float boardW() { return 8 * C + 20 + 120; }
    @Override protected float boardH() { return 8 * C + 12; }
    @Override protected String scoreLabel() { return "Steine"; }
    @Override protected String overTitle() { return ended == 1 ? "Gewonnen!" : "Verloren"; }

    @Override protected void reset() {
        b = new int[64];
        for (int i = 0; i < 64; i++) { int r = i / 8, c = i % 8; if ((r + c) % 2 == 1) { if (r < 3) b[i] = 2; else if (r > 4) b[i] = 1; } }
        turn = 1; sel = -1; chainFrom = -1; ended = 0; think = 0;
    }

    private static int owner(int v) { return v == 0 ? 0 : (v == 1 || v == 3) ? 1 : 2; }
    private static boolean king(int v) { return v >= 3; }

    /** moves: {from, to, captured or -1} */
    private static List<int[]> moves(int[] b, int who, int only) {
        List<int[]> caps = new ArrayList<>(), plain = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            if (owner(b[i]) != who || (only >= 0 && i != only)) continue;
            int r = i / 8, c = i % 8;
            int[] drs = king(b[i]) ? new int[]{-1, 1} : who == 1 ? new int[]{-1} : new int[]{1};
            for (int dr : drs) for (int dc : new int[]{-1, 1}) {
                int r1 = r + dr, c1 = c + dc, r2 = r + 2 * dr, c2 = c + 2 * dc;
                if (r1 < 0 || r1 > 7 || c1 < 0 || c1 > 7) continue;
                if (b[r1 * 8 + c1] == 0) plain.add(new int[]{i, r1 * 8 + c1, -1});
                else if (owner(b[r1 * 8 + c1]) == 3 - who && r2 >= 0 && r2 <= 7 && c2 >= 0 && c2 <= 7 && b[r2 * 8 + c2] == 0) caps.add(new int[]{i, r2 * 8 + c2, r1 * 8 + c1});
            }
        }
        return caps.isEmpty() ? (only >= 0 ? caps : plain) : caps;
    }

    private static boolean apply(int[] b, int[] m) {
        int v = b[m[0]]; b[m[0]] = 0; b[m[1]] = v;
        if (m[2] >= 0) b[m[2]] = 0;
        int r = m[1] / 8;
        if (v == 1 && r == 0) b[m[1]] = 3;
        if (v == 2 && r == 7) b[m[1]] = 4;
        return m[2] >= 0;
    }

    private int count(int who) { int n = 0; for (int v : b) if (owner(v) == who) n++; return n; }

    private static int eval(int[] b) { int s = 0; for (int i = 0; i < 64; i++) { int v = b[i]; if (v == 2) s += 10 + i / 8; if (v == 4) s += 25; if (v == 1) s -= 10 + (7 - i / 8); if (v == 3) s -= 25; } return s; }

    private int search(int[] bd, int depth, int who, int a, int be) {
        if (depth == 0) return eval(bd);
        List<int[]> ms = moves(bd, who, -1);
        if (ms.isEmpty()) return who == 2 ? -1000 : 1000;
        for (int[] m : ms) {
            int[] c = bd.clone(); apply(c, m);
            int v = search(c, depth - 1, 3 - who, a, be);
            if (who == 2) a = Math.max(a, v); else be = Math.min(be, v);
            if (a >= be) break;
        }
        return who == 2 ? a : be;
    }

    private void aiMove() {
        List<int[]> ms = moves(b, 2, chainFrom);
        if (ms.isEmpty()) { endTurn(); return; }
        int lv = aiLevel();
        int[] pick = ms.get(rnd.nextInt(ms.size()));
        if (lv > 0) {
            int best = Integer.MIN_VALUE, depth = new int[]{1, 2, 4, 6}[lv];
            for (int[] m : ms) { int[] c = b.clone(); apply(c, m); int v = search(c, depth - 1, 1, Integer.MIN_VALUE, Integer.MAX_VALUE) + rnd.nextInt(2); if (v > best) { best = v; pick = m; } }
        }
        boolean cap = apply(b, pick);
        Sound.play("minecraft:block.wood.place", 1.2f, 0.35f);
        if (cap && !moves(b, 2, pick[1]).isEmpty()) { chainFrom = pick[1]; think = 0.2f; return; }
        endTurn();
    }

    private void endTurn() {
        chainFrom = -1; turn = 3 - turn; think = 0; sel = -1;
        if (count(1) == 0 || moves(b, 1, -1).isEmpty() && turn == 1) { ended = 2; PackPuzzle.bad(); gameOver(); return; }
        if (count(2) == 0 || moves(b, 2, -1).isEmpty() && turn == 2) { ended = 1; score = count(1); PackPuzzle.win(); gameOver(); }
    }

    @Override protected void onClick(float x, float y, int bt) {
        if (turn != 1) return;
        int c = (int) ((x - OX) / C), r = (int) ((y - OY) / C);
        if (x < OX || y < OY || c > 7 || r > 7) return;
        int i = r * 8 + c;
        if (owner(b[i]) == 1 && chainFrom < 0) { sel = i; return; }
        if (sel < 0) return;
        for (int[] m : moves(b, 1, chainFrom >= 0 ? chainFrom : -1)) if (m[0] == sel && m[1] == i) {
            boolean cap = apply(b, m);
            Sound.play("minecraft:block.wood.place", 1.4f, 0.35f);
            if (cap && !moves(b, 1, i).isEmpty()) { chainFrom = i; sel = i; return; }
            endTurn(); return;
        }
    }

    @Override protected void update(float dt) { if (turn == 2 && (think += dt) > 0.6f) aiMove(); }

    @Override protected void render() {
        List<int[]> legal = turn == 1 ? moves(b, 1, chainFrom) : List.of();
        for (int i = 0; i < 64; i++) {
            int r = i / 8, c = i % 8;
            float x = OX + c * C, y = OY + r * C;
            box(x, y, C, C, 0, (r + c) % 2 == 0 ? 0xFFF2E6C8 : 0xFF8A5A30);
            for (int[] m : legal) if (m[0] == sel && m[1] == i) circle(x + C / 2, y + C / 2, 5, 0x9939D353);
            int v = b[i];
            if (v != 0) {
                int col = owner(v) == 1 ? 0xFFF4F4F4 : 0xFF1B1B22;
                circle(x + C / 2, y + C / 2 + 1.5f, C * 0.38f, 0x77000000);
                circle(x + C / 2, y + C / 2, C * 0.38f, i == sel ? 0xFFFFE14D : col);
                ring(x + C / 2, y + C / 2, C * 0.26f, 1, owner(v) == 1 ? 0x33000000 : 0x33FFFFFF);
                if (king(v)) text("♛", x + C / 2, y + C / 2, 10, Fonts.BOLD, 0xFFFFC53D);
            }
        }
        float sx = 8 * C + 30;
        textL("Du (weiß): " + count(1), sx, 30, 8, Fonts.BOLD, Style.text);
        textL("KI (schwarz): " + count(2), sx, 48, 8, Fonts.BOLD, Style.text);
        textL(turn == 1 ? "Du bist dran" : "KI denkt...", sx, 80, 8, Fonts.BOLD, turn == 1 ? Style.ok : Style.sub);
        textL("Schlagen ist Pflicht", sx, 120, 6.2f, Fonts.MEDIUM, Style.sub);
        textL("Mehrfach-Sprünge möglich", sx, 132, 6.2f, Fonts.MEDIUM, Style.sub);
    }
}

// ====================================================================== Mühle
final class Mill extends GameView {
    private static final float[][] P = new float[24][];
    private static final int[][] ADJ = new int[24][];
    private static final int[][] MILLS = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {9, 10, 11}, {12, 13, 14}, {15, 16, 17}, {18, 19, 20}, {21, 22, 23},
            {0, 9, 21}, {3, 10, 18}, {6, 11, 15}, {1, 4, 7}, {16, 19, 22}, {8, 12, 17}, {5, 13, 20}, {2, 14, 23}};
    static {
        float[] xs = {20, 50, 80}, size = {200, 140, 80};
        int k = 0;
        // standard numbering: rows top to bottom
        float[][] pts = {{0, 0}, {3, 0}, {6, 0}, {1, 1}, {3, 1}, {5, 1}, {2, 2}, {3, 2}, {4, 2}, {0, 3}, {1, 3}, {2, 3}, {4, 3}, {5, 3}, {6, 3}, {2, 4}, {3, 4}, {4, 4}, {1, 5}, {3, 5}, {5, 5}, {0, 6}, {3, 6}, {6, 6}};
        for (float[] p : pts) P[k++] = new float[]{20 + p[0] * 38, 14 + p[1] * 38};
        List<int[]> edges = new ArrayList<>();
        int[][] lines = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {9, 10, 11}, {12, 13, 14}, {15, 16, 17}, {18, 19, 20}, {21, 22, 23}, {0, 9, 21}, {3, 10, 18}, {6, 11, 15}, {1, 4, 7}, {16, 19, 22}, {8, 12, 17}, {5, 13, 20}, {2, 14, 23}};
        for (int[] l : lines) { edges.add(new int[]{l[0], l[1]}); edges.add(new int[]{l[1], l[2]}); }
        for (int i = 0; i < 24; i++) { List<Integer> a = new ArrayList<>(); for (int[] e : edges) { if (e[0] == i) a.add(e[1]); if (e[1] == i) a.add(e[0]); } ADJ[i] = a.stream().mapToInt(Integer::intValue).toArray(); }
        if (xs.length + size.length < 0) throw new IllegalStateException();
    }

    private int[] b = new int[24];
    private int[] hand = new int[3], onBoard = new int[3];
    private int turn, sel = -1, ended;
    private boolean removing;
    private float think;

    Mill() { super("mill"); }
    @Override protected boolean hasAi() { return true; }
    @Override protected float boardW() { return 400; }
    @Override protected float boardH() { return 260; }
    @Override protected String overTitle() { return ended == 1 ? "Gewonnen!" : "Verloren"; }

    @Override protected void reset() { b = new int[24]; hand = new int[]{0, 9, 9}; onBoard = new int[3]; turn = 1; sel = -1; removing = false; ended = 0; think = 0; }

    private boolean inMill(int[] bd, int p) { for (int[] m : MILLS) if ((m[0] == p || m[1] == p || m[2] == p) && bd[m[0]] != 0 && bd[m[0]] == bd[m[1]] && bd[m[1]] == bd[m[2]]) return true; return false; }

    private boolean canFly(int who) { return hand[who] == 0 && onBoard[who] == 3; }

    private List<int[]> moves(int who) {
        List<int[]> l = new ArrayList<>();
        if (hand[who] > 0) { for (int i = 0; i < 24; i++) if (b[i] == 0) l.add(new int[]{-1, i}); return l; }
        for (int i = 0; i < 24; i++) if (b[i] == who) {
            if (canFly(who)) { for (int j = 0; j < 24; j++) if (b[j] == 0) l.add(new int[]{i, j}); }
            else for (int j : ADJ[i]) if (b[j] == 0) l.add(new int[]{i, j});
        }
        return l;
    }

    private List<Integer> removable(int victim) {
        List<Integer> all = new ArrayList<>(), free = new ArrayList<>();
        for (int i = 0; i < 24; i++) if (b[i] == victim) { all.add(i); if (!inMill(b, i)) free.add(i); }
        return free.isEmpty() ? all : free;
    }

    /** returns true when a mill was formed */
    private boolean doMove(int[] m, int who) {
        if (m[0] < 0) { hand[who]--; onBoard[who]++; } else b[m[0]] = 0;
        b[m[1]] = who;
        Sound.play("minecraft:block.stone.place", 1.3f, 0.35f);
        return inMill(b, m[1]);
    }

    private void remove(int p, int victim) { b[p] = 0; onBoard[victim]--; Sound.play("minecraft:entity.item.pickup", 0.8f, 0.4f); }

    private void endTurn() {
        turn = 3 - turn; think = 0; sel = -1;
        for (int w = 1; w <= 2; w++) if ((hand[w] == 0 && onBoard[w] < 3) || (turn == w && moves(w).isEmpty())) { ended = 3 - w; score = ended == 1 ? onBoard[1] * 10L : 0; if (ended == 1) PackPuzzle.win(); else PackPuzzle.bad(); gameOver(); return; }
    }

    private int scoreMove(int[] m) {
        int[] c = b.clone(); if (m[0] >= 0) c[m[0]] = 0; c[m[1]] = 2;
        int s = inMill(c, m[1]) ? 100 : 0;
        int[] d = b.clone(); if (m[0] >= 0) d[m[0]] = 0; d[m[1]] = 1;   // blocks player mill?
        if (inMill(d, m[1])) s += 60;
        for (int[] mill : MILLS) { int mine = 0, empty = 0; for (int p : mill) { if (c[p] == 2) mine++; if (c[p] == 0) empty++; } if (mine == 2 && empty == 1) s += 15; }
        return s + rnd.nextInt(5);
    }

    @Override protected void update(float dt) {
        if (turn != 2 || (think += dt) < 0.7f) return;
        List<int[]> ms = moves(2);
        if (ms.isEmpty()) { endTurn(); return; }
        int lv = aiLevel();
        int[] best = ms.get(rnd.nextInt(ms.size()));
        if (lv > 0 || rnd.nextBoolean()) { int bs = -1; for (int[] m : ms) { int s = scoreMove(m) * (lv == 0 ? 0 : 1) + (lv == 1 ? rnd.nextInt(40) : 0); if (s > bs) { bs = s; best = m; } } }
        if (doMove(best, 2)) {
            List<Integer> rem = removable(1);
            int pick = rem.get(rnd.nextInt(rem.size()));
            if (lv >= 2) for (int p : rem) { for (int[] mill : MILLS) { int mine = 0; for (int q : mill) if (b[q] == 1) mine++; if (mine == 2 && (mill[0] == p || mill[1] == p || mill[2] == p)) pick = p; } }
            remove(pick, 1);
        }
        endTurn();
    }

    private int at(float x, float y) { for (int i = 0; i < 24; i++) if (Math.hypot(x - P[i][0], y - P[i][1]) < 12) return i; return -1; }

    @Override protected void onClick(float x, float y, int bt) {
        if (turn != 1) return;
        int p = at(x, y);
        if (p < 0) return;
        if (removing) { if (removable(2).contains(p)) { remove(p, 2); removing = false; endTurn(); } return; }
        if (hand[1] > 0) { if (b[p] == 0) { if (doMove(new int[]{-1, p}, 1)) removing = true; else endTurn(); } return; }
        if (b[p] == 1) { sel = p; return; }
        if (sel >= 0 && b[p] == 0) for (int[] m : moves(1)) if (m[0] == sel && m[1] == p) { if (doMove(m, 1)) removing = true; else endTurn(); sel = -1; return; }
    }

    @Override protected void render() {
        int lc = Style.light ? 0xFF5A4020 : 0xFFC8A060;
        for (int[] m : MILLS) { line(P[m[0]][0], P[m[0]][1], P[m[2]][0], P[m[2]][1], 1.6f, lc); }
        for (int i = 0; i < 24; i++) {
            circle(P[i][0], P[i][1], 3, lc);
            if (b[i] != 0) { circle(P[i][0], P[i][1] + 1.5f, 10, 0x66000000); circle(P[i][0], P[i][1], 10, i == sel ? 0xFFFFE14D : b[i] == 1 ? 0xFFF4F4F4 : 0xFF1B1B22); }
            if (removing && b[i] == 2 && removable(2).contains(i)) ring(P[i][0], P[i][1], 12, 1.5f, 0xFFFF3B4C);
        }
        float sx = 290;
        textL("Du (weiß)", sx, 30, 8, Fonts.BOLD, Style.text);
        textL("Hand " + hand[1] + " · Brett " + onBoard[1], sx, 44, 6.6f, Fonts.MEDIUM, Style.sub);
        textL("KI (schwarz)", sx, 70, 8, Fonts.BOLD, Style.text);
        textL("Hand " + hand[2] + " · Brett " + onBoard[2], sx, 84, 6.6f, Fonts.MEDIUM, Style.sub);
        String st = turn != 1 ? "KI ist dran..." : removing ? "Mühle! Nimm einen Stein" : hand[1] > 0 ? "Setze einen Stein" : canFly(1) ? "Du darfst springen" : "Ziehe einen Stein";
        textL(st, sx, 120, 7, Fonts.BOLD, removing ? Style.danger : Style.ok);
        textL("3 in einer Linie = Mühle", sx, 160, 6, Fonts.MEDIUM, Style.muted);
    }
}
