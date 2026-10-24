package dev.lego.games;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;

/** Labyrinth-Jagd: a gingerbread man eats sugar pearls in an icy maze, chased by snowmen. Candy canes turn the tables. */
public final class MazeChase extends GameView {
    private static final String[] MAP = {
            "###################",
            "#........#........#",
            "#o##.###.#.###.##o#",
            "#.................#",
            "#.##.#.#####.#.##.#",
            "#....#...#...#....#",
            "####.###.#.###.####",
            "####.#.......#.####",
            "####.#.##-##.#.####",
            "    ...#GGG#...    ",
            "####.#.#####.#.####",
            "####.#.......#.####",
            "####.#.#####.#.####",
            "#........#........#",
            "#.##.###.#.###.##.#",
            "#o.#.....P.....#.o#",
            "##.#.#.#####.#.#.##",
            "#....#...#...#....#",
            "#.######.#.######.#",
            "#.................#",
            "###################"};
    private static final int C = 19, R = 21;
    private static final float T = 13, W = 380, H = 290, OX = (W - C * T) / 2, OY = (H - R * T) / 2;
    private static final int[] DX = {1, 0, -1, 0}, DY = {0, 1, 0, -1};   // right, down, left, up
    private static final int[] HAT = {0xFFE3263B, 0xFF3F7BFF, 0xFF1FA35C, 0xFFB14DFF};
    private static final String[] NAMES = {"Frosti", "Flocke", "Eisbert", "Kufe"};
    private static final int HOUSE = 0, LEAVING = 1, NORMAL = 2, EATEN = 3, ENTERING = 4;
    private static final int[][] CORNER = {{17, -1}, {1, -1}, {17, 21}, {1, 21}};

    private static final class Ent {
        float x, y;
        int dir = 2, want = 2, mode;
        float houseT, speedMul = 1;
        boolean moving = true;
    }

    private final char[][] grid = new char[R][C];
    private final Ent player = new Ent();
    private final Ent[] ghosts = new Ent[4];
    private final GameFx fx = new GameFx();
    private int pellets, eatenChain, lives, level, pelletsEaten;
    private float frightT, modeT, anim, deathT = -1, clearT = -1, readyT, bonusT, chomp, hintT;
    private boolean chase;
    private int modeIdx;
    private static final float[] MODES = {7, 20, 7, 20, 5, 9999};

    public MazeChase() { super("maze"); }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }

    @Override
    protected void reset() {
        lives = 3;
        level = 1;
        fx.clear();
        hintT = 0;
        loadLevel();
    }

    private void loadLevel() {
        pellets = 0;
        pelletsEaten = 0;
        for (int r = 0; r < R; r++)
            for (int c = 0; c < C; c++) {
                char ch = MAP[r].charAt(c);
                grid[r][c] = ch;
                if (ch == '.' || ch == 'o') pellets++;
            }
        bonusT = 0;
        clearT = -1;
        resetPositions();
    }

    private void resetPositions() {
        player.x = 9; player.y = 15; player.dir = 2; player.want = 2; player.moving = true;
        for (int i = 0; i < 4; i++) {
            Ent g = ghosts[i] == null ? (ghosts[i] = new Ent()) : ghosts[i];
            g.x = i == 0 ? 9 : 7 + i; g.y = i == 0 ? 7 : 9; g.dir = 2; g.mode = i == 0 ? NORMAL : HOUSE;
            g.houseT = i == 0 ? 0 : i * 3.2f / (1 + (level - 1) * 0.3f);
        }
        frightT = 0; eatenChain = 0; modeT = 0; modeIdx = 0; chase = false;
        deathT = -1;
        readyT = 1.6f;
    }

    private boolean open(int c, int r, boolean door) {
        if (r < 0 || r >= R) return false;
        if (c < 0 || c >= C) return r == 9;   // tunnel
        char ch = grid[r][c];
        return ch != '#' && (ch != '-' || door) && (ch != 'G' || door);
    }

    // ------------------------------------------------------------------ input
    @Override
    protected void onKey(int k) {
        int d = k == K_RIGHT || k == K_D ? 0 : k == K_DOWN || k == K_S ? 1 : k == K_LEFT || k == K_A ? 2 : k == K_UP || k == K_W ? 3 : -1;
        if (d < 0) return;
        player.want = d;
        if (d == (player.dir + 2) % 4) { player.dir = d; player.moving = true; }   // instant reverse
    }

    // ------------------------------------------------------------------ logic
    @Override
    protected void update(float dt) {
        fx.update(dt);
        hintT += dt;
        chomp += dt;
        if (readyT > 0) { readyT -= dt; return; }
        if (clearT >= 0) {
            clearT += dt;
            if (clearT > 2.2f) { level++; loadLevel(); fx.popup("Level " + level, W / 2, H / 2, 0xFFF4C152, 18); }
            return;
        }
        if (deathT >= 0) {
            deathT += dt;
            if (deathT > 1.8f) { if (lives <= 0) { gameOver(); return; } resetPositions(); }
            return;
        }
        float lvl = Math.min(1, (level - 1) / 6f);
        // modes
        if (frightT > 0) { frightT -= dt; if (frightT <= 0) eatenChain = 0; }
        else {
            modeT += dt;
            if (modeT > MODES[modeIdx]) { modeT = 0; modeIdx = Math.min(MODES.length - 1, modeIdx + 1); chase = modeIdx % 2 == 1; for (Ent g : ghosts) if (g.mode == NORMAL) g.dir = (g.dir + 2) % 4; }
        }
        // player
        float ps = (7.0f + lvl * 1.2f) * (frightT > 0 ? 1.1f : 1);
        move(player, ps * dt, -1);
        int pc = Math.round(player.x), pr = Math.round(player.y);
        if (pc >= 0 && pc < C && Math.abs(player.x - pc) < 0.3f && Math.abs(player.y - pr) < 0.3f) {
            char ch = grid[pr][pc];
            if (ch == '.') {
                grid[pr][pc] = ' ';
                score += 10; pellets--; pelletsEaten++;
                if (pelletsEaten % 2 == 0) Sound.play("minecraft:block.note_block.hat", 1.6f + (pelletsEaten % 4) * 0.1f, 0.12f);
                chomp = 0;
            } else if (ch == 'o') {
                grid[pr][pc] = ' ';
                score += 50; pellets--; pelletsEaten++;
                frightT = Math.max(2.5f, 7 - (level - 1) * 0.8f);
                eatenChain = 0;
                for (Ent g : ghosts) if (g.mode == NORMAL) g.dir = (g.dir + 2) % 4;
                fx.ring(cx(pc), cy(pr), 40, 0xFFFF4D6D, 0.5f);
                fx.burst(cx(pc), cy(pr), 16, 0xFFFFFFFF, 90, 2, 0.5f, 0, GameFx.SPARK);
                fx.shake(0.2f);
                fx.popup("Zuckerstange!", cx(pc), cy(pr) - 10, 0xFFFF7B9C, 9);
                Sound.play("minecraft:block.amethyst_block.chime", 1.0f, 0.9f);
            }
            if (pelletsEaten == 70 || pelletsEaten == 150) bonusT = 9;
            if (bonusT > 0 && pc == 9 && pr == 15 && pelletsEaten > 0 && Math.abs(player.x - 9) < 0.4f && Math.abs(player.y - 15) < 0.4f) {
                int pts = 100 * level + 100;
                score += pts;
                bonusT = 0;
                fx.confetti(cx(9), cy(15), 20, new int[]{0xFFE3263B, 0xFFF4C152, 0xFF1FA35C}, 120, 3);
                fx.popup("Geschenk! +" + pts, cx(9), cy(15) - 10, 0xFFF4C152, 10);
                Sound.play("minecraft:entity.player.levelup", 1.6f, 0.4f);
            }
            if (pellets <= 0) { clearT = 0; Sound.play("minecraft:ui.toast.challenge_complete", 1.3f, 0.35f); fx.popup("Geschafft!", W / 2, H / 2 - 10, 0xFFB8FF4F, 16); return; }
        }
        bonusT = Math.max(0, bonusT - dt);
        // ghosts
        for (int i = 0; i < 4; i++) {
            Ent g = ghosts[i];
            switch (g.mode) {
                case HOUSE:
                    g.houseT -= dt;
                    g.y = 9 + (float) Math.sin(anim * 5 + i) * 0.25f;
                    if (g.houseT <= 0) { g.mode = LEAVING; g.y = 9; }
                    continue;
                case LEAVING: {
                    float sp = 3.5f * dt;
                    if (Math.abs(g.x - 9) > 0.01f) g.x += Math.signum(9 - g.x) * Math.min(sp, Math.abs(9 - g.x));
                    else { g.y -= sp; if (g.y <= 7) { g.y = 7; g.x = 9; g.mode = NORMAL; g.dir = 2; } }
                    continue;
                }
                case ENTERING:
                    g.y += 6 * dt;
                    if (g.y >= 9) { g.y = 9; g.mode = LEAVING; }
                    continue;
                default:
            }
            boolean fright = frightT > 0 && g.mode == NORMAL;
            float gs = g.mode == EATEN ? 15 : fright ? 4.2f : (6.4f + lvl * 1.4f);
            if (Math.round(g.y) == 9 && (g.x < 4 || g.x > 14) && g.mode != EATEN) gs *= 0.55f;
            final int idx = i;
            move(g, gs * dt, idx);
            if (g.mode == EATEN && Math.abs(g.x - 9) < 0.1f && Math.abs(g.y - 7) < 0.1f) { g.x = 9; g.y = 7; g.mode = ENTERING; }
            // collision
            if (g.mode == NORMAL && Math.abs(g.x - player.x) + Math.abs(g.y - player.y) < 0.8f) {
                if (fright) {
                    g.mode = EATEN;
                    eatenChain++;
                    int pts = 100 << Math.min(4, eatenChain);
                    score += pts;
                    fx.burst(cx(g.x), cy(g.y), 26, 0xFFFFFFFF, 140, 2.6f, 0.6f, 60, GameFx.FLAKE);
                    fx.ring(cx(g.x), cy(g.y), 26, 0xFF9FD8FF, 0.4f);
                    fx.popup("+" + pts, cx(g.x), cy(g.y) - 8, 0xFF9FD8FF, 11);
                    fx.shake(0.3f);
                    Sound.play("minecraft:block.snow.break", 1.2f, 0.9f);
                    Sound.play("minecraft:entity.experience_orb.pickup", 0.8f + eatenChain * 0.2f, 0.4f);
                } else {
                    die();
                    return;
                }
            }
        }
    }

    private void die() {
        deathT = 0;
        lives--;
        fx.burst(cx(player.x), cy(player.y), 30, 0xFFC87A3A, 120, 3, 0.9f, 80, GameFx.SQUARE);
        fx.burst(cx(player.x), cy(player.y), 12, 0xFFFFFFFF, 90, 2, 0.7f, 0, GameFx.SPARK);
        fx.shake(0.8f);
        fx.flash(0xFFFF3B4E, 0.5f);
        Sound.play("minecraft:entity.player.hurt_freeze", 0.8f, 0.6f);
    }

    /** Moves along the grid; decisions are taken at every tile centre (ghostIdx < 0 = player). */
    private void move(Ent e, float dist, int ghostIdx) {
        for (int guard = 0; dist > 1e-5f && guard < 10; guard++) {
            int cT = Math.round(e.x), rT = Math.round(e.y);
            if (Math.abs(e.x - cT) < 1e-3f && Math.abs(e.y - rT) < 1e-3f) {
                e.x = cT; e.y = rT;
                if (ghostIdx < 0) {
                    if (open(cT + DX[e.want], rT + DY[e.want], false)) e.dir = e.want;
                    if (!open(cT + DX[e.dir], rT + DY[e.dir], false)) { e.moving = false; return; }
                    e.moving = true;
                } else e.dir = chooseDir(e, cT, rT, ghostIdx);
            }
            float pos = DX[e.dir] != 0 ? e.x : e.y;
            int sgn = DX[e.dir] != 0 ? DX[e.dir] : DY[e.dir];
            float target = sgn > 0 ? (float) Math.floor(pos + 1e-3f) + 1 : (float) Math.ceil(pos - 1e-3f) - 1;
            float st = Math.min(dist, Math.abs(target - pos));
            e.x += DX[e.dir] * st; e.y += DY[e.dir] * st;
            dist -= st;
            if (e.x < -1.001f) e.x += C + 1;
            if (e.x > C + 0.001f) e.x -= C + 1;
        }
    }

    private int chooseDir(Ent g, int c, int r, int idx) {
        boolean fright = frightT > 0 && g.mode == NORMAL;
        int tx, ty;
        if (g.mode == EATEN) { tx = 9; ty = 7; }
        else if (!chase) { tx = CORNER[idx][0]; ty = CORNER[idx][1]; }
        else {
            int pc = Math.round(player.x), pr = Math.round(player.y);
            switch (idx) {
                case 1: tx = pc + DX[player.dir] * 4; ty = pr + DY[player.dir] * 4; break;
                case 2: { float d = (pc - c) * (pc - c) + (pr - r) * (pr - r); if (d > 64) { tx = pc; ty = pr; } else { tx = CORNER[2][0]; ty = CORNER[2][1]; } break; }
                case 3: { int ax = pc + DX[player.dir] * 2, ay = pr + DY[player.dir] * 2; tx = ax * 2 - Math.round(ghosts[0].x); ty = ay * 2 - Math.round(ghosts[0].y); break; }
                default: tx = pc; ty = pr;
            }
        }
        int best = -1;
        float bd = Float.MAX_VALUE;
        int start = fright ? rnd.nextInt(4) : 0;
        for (int k = 0; k < 4; k++) {
            int d = fright ? (start + k) % 4 : new int[]{3, 2, 1, 0}[k];
            if (d == (g.dir + 2) % 4) continue;
            int nc = c + DX[d], nr = r + DY[d];
            if (!open(nc, nr, false)) continue;
            if (fright) return d;
            float dd = (nc - tx) * (nc - tx) + (nr - ty) * (nr - ty);
            if (dd < bd) { bd = dd; best = d; }
        }
        if (best < 0) best = (g.dir + 2) % 4;
        return best;
    }

    private float cx(float c) { return OX + c * T + T / 2; }
    private float cy(float r) { return OY + r * T + T / 2; }

    // ------------------------------------------------------------------ drawing
    @Override
    protected void render() {
        float dt = Math.min(0.05f, Gx.dt);
        anim += dt;
        boxV(0, 0, W, H, 0, 0xFF0A1330, 0xFF14224A);
        boolean flashWalls = clearT >= 0 && ((int) (clearT * 6)) % 2 == 0 && clearT > 0.3f;
        GameFx.sprite(this, "mz_walls" + (flashWalls ? 1 : 0), OX - 3, OY - 3, C * T + 6, R * T + 6, C * T + 6, R * T + 6, gr -> paintWalls(gr, flashWalls), 0xFFFFFFFF);

        Gx.push();
        Gx.translate(S(fx.sx), S(fx.sy));
        // pellets
        for (int r = 0; r < R; r++)
            for (int c = 0; c < C; c++) {
                char ch = grid[r][c];
                if (ch == '.') circle(cx(c), cy(r), 1.35f, 0xFFF4F8FF);
                else if (ch == 'o') {
                    float s = 1 + 0.12f * (float) Math.sin(anim * 6 + c);
                    Gx.glow(X(cx(c)), Y(cy(r)), S(11 * s), 0x55FF4D6D);
                    GameFx.sprite(this, "mz_cane", cx(c) - 6 * s, cy(r) - 6 * s, 12 * s, 12 * s, 24, 24, MazeChase::paintCane, 0xFFFFFFFF);
                }
            }
        if (bonusT > 0) {
            float s = 1 + 0.08f * (float) Math.sin(anim * 8);
            Gx.glow(X(cx(9)), Y(cy(15)), S(12), 0x55F4C152);
            GameFx.sprite(this, "sj_gift0", cx(9) - 6.5f * s, cy(15) - 6.5f * s, 13 * s, 13 * s, 40, 40, gr -> SantaJump.paintGift(gr, 0), Gx.withAlpha(0xFFFFFFFF, Math.min(1, bonusT)));
        }
        // ghosts
        for (int i = 0; i < 4; i++) ghost(ghosts[i], i);
        // player
        playerSprite();
        fx.draw(this, 0, 0);
        Gx.pop();

        // HUD (left / right of the maze)
        text("LEVEL", OX / 2, 24, 6, Fonts.BOLD, 0x99FFFFFF);
        text(String.valueOf(level), OX / 2, 38, 13, Fonts.BOLD, 0xFFF4C152);
        text("LEBEN", OX / 2, 62, 6, Fonts.BOLD, 0x99FFFFFF);
        for (int i = 0; i < lives; i++) GameFx.sprite(this, "mz_ginger0", OX / 2 - 7, 70 + i * 17, 14, 14, 30, 30, gr -> paintGinger(gr, 0), 0xFFFFFFFF);
        float rx = W - OX / 2;
        text("PERLEN", rx, 24, 6, Fonts.BOLD, 0x99FFFFFF);
        text(String.valueOf(pellets), rx, 38, 11, Fonts.BOLD, 0xFFF4F8FF);
        if (frightT > 0) {
            text("POWER", rx, 62, 6, Fonts.BOLD, 0x99FFFFFF);
            float f = frightT / Math.max(2.5f, 7 - (level - 1) * 0.8f);
            box(rx - 3, 70, 6, 60, 3, 0x44FFFFFF);
            box(rx - 3, 70 + 60 * (1 - f), 6, 60 * f, 3, frightT < 2 && ((int) (anim * 8)) % 2 == 0 ? 0xFFFFFFFF : 0xFFFF4D6D);
        }
        for (int i = 0; i < 4; i++) {
            float y = H - 70 + i * 15;
            circle(rx - 16, y, 3, HAT[i]);
            Gx.textMid(NAMES[i], X(rx - 10), Y(y), 5.8f * u, Fonts.MEDIUM, 0xAAFFFFFF);
        }
        if (readyT > 0 && state == State.PLAYING) {
            float a = Math.min(1, readyT * 2);
            GameFx.outlined(this, "Bereit?", cx(9), cy(11), 12, Gx.withAlpha(0xFFF4C152, a), Gx.withAlpha(0xFF000000, a * 0.8f));
        }
        if (state == State.PLAYING && hintT < 4) {
            float a = Math.min(1, 4 - hintT);
            text("Pfeiltasten / WASD", cx(9), cy(13) + 1, 6.5f, Fonts.BOLD, Gx.withAlpha(0xFFFFFFFF, a * 0.8f));
        }
        fx.drawPopups(this, 0, 0);
        fx.drawFlash(this, W, H);
    }

    private void ghost(Ent g, int i) {
        float x = cx(g.x), y = cy(g.y) + (g.mode == NORMAL ? (float) Math.sin(anim * 8 + i) * 0.5f : 0);
        float s = 17;
        boolean fright = frightT > 0 && (g.mode == NORMAL || g.mode == HOUSE || g.mode == LEAVING);
        if (g.mode == EATEN || g.mode == ENTERING) {
            final int d = g.dir;
            GameFx.sprite(this, "mz_eyes" + d + i, x - s / 2, y - s / 2, s, s, 30, 30, gr -> paintEyes(gr, d, HAT[i]), 0xFFFFFFFF);
            return;
        }
        if (fright) {
            boolean white = frightT < 2 && ((int) (anim * 8)) % 2 == 0;
            GameFx.sprite(this, "mz_fright" + (white ? 1 : 0), x - s / 2, y - s / 2 - 1, s, s, 30, 30, gr -> paintSnowman(gr, 0xFF3F7BFF, 0, true, white), 0xFFFFFFFF);
            return;
        }
        final int d = g.dir;
        Gx.glow(X(x), Y(y), S(12), Gx.withAlpha(HAT[i], 0.18f));
        GameFx.sprite(this, "mz_snow" + i + d, x - s / 2, y - s / 2 - 1, s, s, 30, 30, gr -> paintSnowman(gr, HAT[i], d, false, false), 0xFFFFFFFF);
    }

    private void playerSprite() {
        float x = cx(player.x), y = cy(player.y);
        float s = 16;
        if (deathT >= 0) {
            float t = Ease.clamp(deathT / 1.2f);
            if (t >= 1) return;
            Gx.push();
            Gx.scaleAt(X(x), Y(y), 1 - t);
            GameFx.sprite(this, "mz_ginger1", x - s / 2, y - s / 2, s, s, 30, 30, gr -> paintGinger(gr, 1), Gx.withAlpha(0xFFFFFFFF, 1 - t));
            Gx.pop();
            return;
        }
        int fr = player.moving && chomp < 0.12f ? 1 : 0;
        float bob = player.moving && state == State.PLAYING ? (float) Math.abs(Math.sin(anim * 14)) * 1.2f : 0;
        float lean = DX[player.dir] * 1.2f;
        if (frightT > 0) Gx.glow(X(x), Y(y), S(14), 0x44FF4D6D);
        final int f = fr;
        GameFx.sprite(this, "mz_ginger" + f, x - s / 2 + lean, y - s / 2 - bob, s, s, 30, 30, gr -> paintGinger(gr, f), 0xFFFFFFFF);
    }

    // ------------------------------------------------------------------ painters
    private static void paintWalls(Graphics2D g, boolean flash) {
        g.translate(3, 3);
        Area a = new Area();
        for (int r = 0; r < R; r++)
            for (int c = 0; c < C; c++)
                if (MAP[r].charAt(c) == '#') a.add(new Area(new Rectangle2D.Float(c * T, r * T, T, T)));
        // shrink by drawing a thick stroke in background color later
        g.setColor(flash ? new Color(0x3A5AA0) : new Color(0x16285A));
        g.fill(a);
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(159, 216, 255, flash ? 120 : 45));
        g.draw(a);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(flash ? Color.WHITE : new Color(0x9FD8FF));
        g.draw(a);
        // snow caps on top edges
        g.setColor(new Color(244, 248, 255, 215));
        for (int r = 0; r < R; r++)
            for (int c = 0; c < C; c++) {
                if (!cap(r, c)) continue;
                int e = c;
                while (e + 1 < C && cap(r, e + 1)) e++;
                g.fill(new RoundRectangle2D.Float(c * T + 1, r * T + 0.9f, (e - c + 1) * T - 2, 3.2f, 3, 3));
                c = e;
            }
        // door
        g.setColor(new Color(0xFF7BD5));
        g.fill(new Rectangle2D.Float(9 * T, 8 * T + T / 2 - 1.2f, T, 2.4f));
    }

    private static boolean cap(int r, int c) { return MAP[r].charAt(c) == '#' && (r == 0 || MAP[r - 1].charAt(c) != '#'); }

    private static void paintCane(Graphics2D g) {
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GeneralPath p = new GeneralPath();
        p.moveTo(9, 22); p.lineTo(9, 9); p.curveTo(9, 2, 18, 2, 18, 9);
        g.setColor(Color.WHITE);
        g.draw(p);
        g.setColor(new Color(0xE3263B));
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1, new float[]{2.6f, 2.6f}, 0));
        g.draw(p);
    }

    private static void paintGinger(Graphics2D g, int frame) {
        Color dough = new Color(0xC8793A), doughD = new Color(0x8E4F20), icing = Color.WHITE;
        Area body = GameFx.union(new Ellipse2D.Float(9, 1, 12, 12), new RoundRectangle2D.Float(10, 11, 10, 11, 5, 5),
                new RoundRectangle2D.Float(3, 12, 24, 5, 5, 5), new RoundRectangle2D.Float(9, 18, 5, 11, 5, 5), new RoundRectangle2D.Float(16, 18, 5, 11, 5, 5));
        g.setPaint(new GradientPaint(0, 0, dough, 0, 30, doughD));
        g.fill(body);
        g.setColor(new Color(0x6A3510));
        g.setStroke(new BasicStroke(0.8f));
        g.draw(body);
        g.setColor(icing);
        g.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // icing zigzags on arms/legs
        g.drawPolyline(new int[]{4, 5, 6, 7}, new int[]{14, 15, 14, 15}, 4);
        g.drawPolyline(new int[]{23, 24, 25, 26}, new int[]{14, 15, 14, 15}, 4);
        g.drawLine(10, 26, 13, 26); g.drawLine(17, 26, 20, 26);
        // eyes
        g.setColor(new Color(0x1B1B22));
        g.fill(new Ellipse2D.Float(11.6f, 4.5f, 2.2f, 2.4f));
        g.fill(new Ellipse2D.Float(16.2f, 4.5f, 2.2f, 2.4f));
        // mouth (chomping)
        if (frame == 1) { g.setColor(new Color(0x5A1E14)); g.fill(new Ellipse2D.Float(12.5f, 8, 5, 3.4f)); }
        else { g.setColor(icing); g.drawArc(12, 7, 6, 3.5f > 0 ? 3 : 3, 200, 140); }
        // buttons
        g.setColor(new Color(0xE3263B)); g.fill(new Ellipse2D.Float(13.8f, 13, 2.6f, 2.6f));
        g.setColor(new Color(0x1FA35C)); g.fill(new Ellipse2D.Float(13.8f, 17, 2.6f, 2.6f));
    }

    private static void paintSnowman(Graphics2D g, int hat, int dir, boolean fright, boolean white) {
        Color snow = fright ? (white ? Color.WHITE : new Color(0x7FA8FF)) : Color.WHITE, shade = fright ? (white ? new Color(0xC9DAF2) : new Color(0x2F55C8)) : new Color(0xB9CDEB);
        // body (melting bottom when frightened)
        Area body = GameFx.union(new Ellipse2D.Float(5, 12, 20, 16), new Ellipse2D.Float(8, 3, 14, 13));
        if (fright) body.add(new Area(new RoundRectangle2D.Float(4, 22, 22, 7, 6, 6)));
        g.setPaint(new GradientPaint(0, 4, snow, 0, 28, shade));
        g.fill(body);
        if (fright) {
            // drips
            g.setColor(shade);
            g.fill(new Ellipse2D.Float(6, 26, 4, 4)); g.fill(new Ellipse2D.Float(14, 27, 3, 3)); g.fill(new Ellipse2D.Float(21, 26, 4, 4));
            g.setColor(white ? new Color(0xE3263B) : Color.WHITE);
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.fill(new Ellipse2D.Float(11, 8, 2.6f, 2.6f));
            g.fill(new Ellipse2D.Float(17, 8, 2.6f, 2.6f));
            g.drawPolyline(new int[]{10, 12, 14, 16, 18, 20}, new int[]{14, 13, 14, 13, 14, 13}, 6);
            return;
        }
        // scarf
        g.setColor(new Color(hat));
        g.fill(new RoundRectangle2D.Float(7.5f, 13.5f, 15, 3.4f, 3, 3));
        g.fill(new RoundRectangle2D.Float(17, 15, 3.4f, 7, 2, 2));
        // eyes looking in direction
        float ex = DX[dir] * 1.2f, ey = DY[dir] * 1.0f;
        g.setColor(new Color(0x1B1B22));
        g.fill(new Ellipse2D.Float(11 + ex, 7 + ey, 2.8f, 2.8f));
        g.fill(new Ellipse2D.Float(16.2f + ex, 7 + ey, 2.8f, 2.8f));
        // angry brows
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(10, 6, 13.5f > 0 ? 13 : 13, 7); g.drawLine(20, 6, 17, 7);
        // carrot nose (points in direction)
        g.setColor(new Color(0xFF8A1E));
        int nx = dir == 0 ? 1 : dir == 2 ? -1 : 0;
        g.fillPolygon(new int[]{14, 16, 15 + nx * 6}, new int[]{10, 10, 12 + (dir == 1 ? 2 : 0)}, 3);
        // buttons
        g.setColor(new Color(0x2A2A30));
        g.fill(new Ellipse2D.Float(14, 19, 2, 2)); g.fill(new Ellipse2D.Float(14, 23, 2, 2));
        // top hat
        g.setColor(new Color(0x1B1B22));
        g.fill(new RoundRectangle2D.Float(7.5f, 3.5f, 15, 2.4f, 1, 1));
        g.fill(new RoundRectangle2D.Float(10, -0.5f, 10, 5, 1, 1));
        g.setColor(new Color(hat));
        g.fill(new Rectangle2D.Float(10, 2.4f, 10, 1.6f));
    }

    private static void paintEyes(Graphics2D g, int dir, int hat) {
        float ex = DX[dir] * 1.2f, ey = DY[dir] * 1.0f;
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Float(9, 8, 6, 6)); g.fill(new Ellipse2D.Float(15.5f, 8, 6, 6));
        g.setColor(new Color(0x1B1B22));
        g.fill(new Ellipse2D.Float(10.5f + ex, 9.5f + ey, 3, 3)); g.fill(new Ellipse2D.Float(17 + ex, 9.5f + ey, 3, 3));
        g.setColor(new Color(0xFF8A1E));
        g.fillPolygon(new int[]{14, 16, 15}, new int[]{14, 14, 19}, 3);
        g.setColor(new Color(0x1B1B22));
        g.fill(new RoundRectangle2D.Float(10, 1, 10, 5, 1, 1));
        g.fill(new RoundRectangle2D.Float(7.5f, 5, 15, 2, 1, 1));
        g.setColor(new Color(hat));
        g.fill(new Rectangle2D.Float(10, 3.8f, 10, 1.4f));
        g.setPaint(new RadialGradientPaint(new Point2D.Float(15, 15), 10, new float[]{0f, 1f}, new Color[]{new Color(255, 255, 255, 40), new Color(255, 255, 255, 0)}));
        g.fill(new Ellipse2D.Float(5, 5, 20, 20));
    }
}
