package dev.lego.games;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;

/** Alien-Abwehr: defend the base against waves of (original) alien critters. Shields, power-ups, mothership. */
public final class AlienDefense extends GameView {
    private static final float W = 380, H = 300, PY = 272, COLS = 9, ROWS = 5, SX = 30, SY = 22, AW = 22, AH = 16;
    private static final int[] TYPE_COL = {0xFFFF5CA8, 0xFF5CF2FF, 0xFF5CF2FF, 0xFFB8FF4F, 0xFFB8FF4F};
    private static final int[] TYPE_PTS = {30, 20, 20, 10, 10};
    private static final int P_TRIPLE = 0, P_RAPID = 1, P_SHIELD = 2, P_LIFE = 3;
    private static final String[] P_ICON = {"sparkle", "bolt", "shield", "heart-fill"};
    private static final int[] P_COL = {0xFFFFB02E, 0xFFFFF27A, 0xFF5CF2FF, 0xFFFF4D6D};
    private static final String[][][] ART = {
            {{"...XXXXX...", ".XXXXXXXXX.", "XXWOXXXWOXX", "XXXXXXXXXXX", "XXXXXXXXXXX", ".X.X.X.X.X.", "X.X.X.X.X.X", "..........."},
             {"...XXXXX...", ".XXXXXXXXX.", "XXWOXXXWOXX", "XXXXXXXXXXX", "XXXXXXXXXXX", "X.X.X.X.X.X", ".X.X.X.X.X.", "..........."}},
            {{"....XXX....", "..XXXXXXX..", ".XXWWWWWXX.", ".XWWWOOWWX.", ".XXWWWWWXX.", "..XXXXXXX..", ".X..X.X..X.", "X..X...X..X"},
             {"....XXX....", "..XXXXXXX..", ".XXWWWWWXX.", ".XWWOOWWWX.", ".XXWWWWWXX.", "..XXXXXXX..", "..X.X.X.X..", ".X.X...X.X."}},
            {{".....X.....", "....XXX....", "...XXXXX...", "..XWOXWOX..", ".XXXXXXXXX.", "XX.XXXXX.XX", "X...X.X...X", "...X...X..."},
             {".....X.....", "....XXX....", "...XXXXX...", "..XOWXOWX..", ".XXXXXXXXX.", "XX.XXXXX.XX", ".X..X.X..X.", "X...X.X...X"}}};

    private static final class Alien {
        int row, col, type;
        boolean alive = true;
        float hitT;
    }

    private static final class Shot {
        float x, y, vx, vy;
        boolean enemy;
    }

    private static final class Power {
        float x, y, t;
        int kind;
    }

    private final List<Alien> aliens = new ArrayList<>();
    private final List<Shot> shots = new ArrayList<>();
    private final List<Power> powers = new ArrayList<>();
    private final GameFx fx = new GameFx();
    private final boolean[][][] shields = new boolean[4][8][12];
    private static final float CELL = 3.2f, SHY = 226;
    private float fX, fY, fDir, stepT, px, fireT, deadT, invul, ufoX = -999, ufoT, ufoDir, bannerT = -1, anim, tripleT, rapidT, hintT;
    private boolean left, right, firing, dead, bubble;
    private int lives, wave, frame, ufoPts;

    public AlienDefense() { super("invaders"); }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }

    @Override
    protected void reset() {
        shots.clear();
        powers.clear();
        fx.clear();
        lives = 3; wave = 0;
        px = W / 2; dead = false; invul = 0; bubble = false; tripleT = rapidT = 0; hintT = 0;
        left = right = firing = false;
        buildShields();
        nextWave();
    }

    private void buildShields() {
        for (int s = 0; s < 4; s++)
            for (int r = 0; r < 8; r++)
                for (int c = 0; c < 12; c++) {
                    boolean on = true;
                    if (r == 0 && (c < 2 || c > 9)) on = false;
                    if (r == 1 && (c < 1 || c > 10)) on = false;
                    if (r >= 5 && c >= 3 && c <= 8) on = false;
                    shields[s][r][c] = on;
                }
    }

    private float shieldX(int s) { return 46 + s * 88 - 6 * CELL; }

    private void nextWave() {
        wave++;
        aliens.clear();
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                Alien a = new Alien();
                a.row = r; a.col = c; a.type = r == 0 ? 0 : r < 3 ? 1 : 2;
                aliens.add(a);
            }
        fX = 30; fY = 44 + Math.min(40, (wave - 1) * 8); fDir = 1;
        ufoT = 14 + rnd.nextFloat() * 8;
        bannerT = 0;
        if (wave > 1) Sound.play("minecraft:entity.player.levelup", 0.9f, 0.4f);
    }

    private int alive() { int n = 0; for (Alien a : aliens) if (a.alive) n++; return n; }

    // ------------------------------------------------------------------ input
    @Override
    protected void onKey(int k) {
        if (k == K_LEFT || k == K_A) left = true;
        if (k == K_RIGHT || k == K_D) right = true;
        if (k == K_SPACE || k == K_UP || k == K_W) firing = true;
    }

    @Override
    protected void onKeyUp(int k) {
        if (k == K_LEFT || k == K_A) left = false;
        if (k == K_RIGHT || k == K_D) right = false;
        if (k == K_SPACE || k == K_UP || k == K_W) firing = false;
    }

    // ------------------------------------------------------------------ logic
    @Override
    protected void update(float dt) {
        fx.update(dt);
        hintT += dt;
        if (bannerT >= 0) { bannerT += dt; if (bannerT > 1.8f) bannerT = -1; }
        fireT = Math.max(0, fireT - dt);
        invul = Math.max(0, invul - dt);
        tripleT = Math.max(0, tripleT - dt);
        rapidT = Math.max(0, rapidT - dt);

        // player
        if (dead) {
            deadT += dt;
            if (deadT > 1.4f) { if (lives <= 0) { gameOver(); return; } dead = false; invul = 1.6f; px = W / 2; }
        } else {
            px += ((right ? 1 : 0) - (left ? 1 : 0)) * 175 * dt;
            px = Math.max(14, Math.min(W - 14, px));
            if (firing && fireT <= 0) fire();
        }

        // formation
        int n = alive();
        float speed = 14 + (ROWS * COLS - n) * 1.25f + wave * 3.5f + (n <= 3 ? 40 : 0);
        fX += fDir * speed * dt;
        float minX = 999, maxX = -999, maxY = -999;
        for (Alien a : aliens) if (a.alive) { minX = Math.min(minX, fX + a.col * SX); maxX = Math.max(maxX, fX + a.col * SX + AW); maxY = Math.max(maxY, fY + a.row * SY + AH); }
        if ((fDir > 0 && maxX > W - 6) || (fDir < 0 && minX < 6)) { fDir = -fDir; fY += 8; }
        stepT += dt * (0.8f + speed / 30f);
        if (stepT > 1) { stepT = 0; frame ^= 1; Sound.play("minecraft:block.note_block.bass", frame == 0 ? 0.6f : 0.55f, 0.18f); }
        for (Alien a : aliens) a.hitT = Math.max(0, a.hitT - dt);
        // aliens chew through shields
        if (maxY > SHY) for (Alien a : aliens) if (a.alive) eraseShield(fX + a.col * SX + AW / 2, fY + a.row * SY + AH / 2, AW / 2 + 1);
        if (maxY >= PY - 8 && !dead) { lives = 0; crash(); }

        // alien fire
        float rate = 0.6f + wave * 0.2f + (ROWS * COLS - n) * 0.02f;
        if (n > 0 && rnd.nextFloat() < rate * dt) {
            List<Alien> bottom = new ArrayList<>();
            for (int c = 0; c < COLS; c++) {
                Alien low = null;
                for (Alien a : aliens) if (a.alive && a.col == c && (low == null || a.row > low.row)) low = a;
                if (low != null) bottom.add(low);
            }
            // prefer aliens above the player
            Alien sh = bottom.get(rnd.nextInt(bottom.size()));
            if (rnd.nextFloat() < 0.5f) for (Alien a : bottom) if (Math.abs(fX + a.col * SX + AW / 2 - px) < 22) sh = a;
            Shot s = new Shot();
            s.enemy = true; s.x = fX + sh.col * SX + AW / 2; s.y = fY + sh.row * SY + AH; s.vy = 120 + wave * 8;
            shots.add(s);
        }

        // mothership
        ufoT -= dt;
        if (ufoX < -900 && ufoT <= 0) { ufoDir = rnd.nextBoolean() ? 1 : -1; ufoX = ufoDir > 0 ? -30 : W + 30; ufoPts = 50 * (2 + rnd.nextInt(5)); Sound.play("minecraft:block.beacon.ambient", 2f, 0.5f); }
        if (ufoX > -900) { ufoX += ufoDir * 70 * dt; if (ufoX < -40 || ufoX > W + 40) { ufoX = -999; ufoT = 16 + rnd.nextFloat() * 10; } }

        // shots
        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot s = shots.get(i);
            s.x += s.vx * dt; s.y += s.vy * dt;
            if (s.y < 16 || s.y > H + 4 || s.x < -4 || s.x > W + 4) { shots.remove(i); continue; }
            if (hitShield(s.x, s.y)) { shots.remove(i); fx.burst(s.x, s.y, 5, s.enemy ? 0xFFFF7BD5 : 0xFF7CF7FF, 50, 1.6f, 0.3f, 0, GameFx.SQUARE); continue; }
            if (s.enemy) {
                if (!dead && invul <= 0 && Math.abs(s.x - px) < 12 && s.y > PY - 8 && s.y < PY + 8) {
                    shots.remove(i);
                    if (bubble) { bubble = false; invul = 0.8f; fx.ring(px, PY, 30, 0xFF5CF2FF, 0.4f); fx.popup("Schild!", px, PY - 20, 0xFF5CF2FF, 9); Sound.play("minecraft:item.shield.block", 1.2f, 0.5f); }
                    else crash();
                }
                continue;
            }
            boolean used = false;
            for (Alien a : aliens) {
                if (!a.alive) continue;
                float ax = fX + a.col * SX, ay = fY + a.row * SY;
                if (s.x > ax && s.x < ax + AW && s.y > ay && s.y < ay + AH) { kill(a, ax + AW / 2, ay + AH / 2); used = true; break; }
            }
            if (!used && ufoX > -900 && Math.abs(s.x - ufoX) < 16 && Math.abs(s.y - 28) < 7) {
                score += ufoPts;
                fx.burst(ufoX, 28, 30, 0xFFFF5CA8, 160, 2.6f, 0.7f, 0, GameFx.SQUARE);
                fx.ring(ufoX, 28, 40, 0xFFFF5CA8, 0.5f);
                fx.popup("Mutterschiff! +" + ufoPts, ufoX, 40, 0xFFFF5CA8, 12);
                fx.shake(0.4f);
                Sound.play("minecraft:entity.generic.explode", 1.3f, 0.4f);
                ufoX = -999; ufoT = 16 + rnd.nextFloat() * 10;
                used = true;
            }
            if (used) shots.remove(i);
        }
        // shot vs shot
        for (Shot a : shots) if (!a.enemy) for (Shot b : shots) if (b.enemy && Math.abs(a.x - b.x) < 3 && Math.abs(a.y - b.y) < 6) { a.y = -99; b.y = H + 99; fx.burst(a.x, (a.y + b.y) / 2, 4, 0xFFFFFFFF, 40, 1.5f, 0.2f, 0, GameFx.SPARK); }
        // power-ups
        for (int i = powers.size() - 1; i >= 0; i--) {
            Power p = powers.get(i);
            p.t += dt; p.y += 60 * dt;
            if (!dead && Math.abs(p.x - px) < 16 && Math.abs(p.y - PY) < 10) { powers.remove(i); collect(p); continue; }
            if (p.y > H + 10) powers.remove(i);
        }
        if (n == 0) nextWave();
    }

    private void fire() {
        int mine = 0;
        for (Shot s : shots) if (!s.enemy) mine++;
        boolean rapid = rapidT > 0, triple = tripleT > 0;
        if (mine >= (rapid ? 6 : triple ? 3 : 2)) return;
        fireT = rapid ? 0.14f : 0.38f;
        for (int k = triple ? -1 : 0; k <= (triple ? 1 : 0); k++) {
            Shot s = new Shot();
            s.x = px + k * 4; s.y = PY - 10; s.vy = -340; s.vx = k * 60;
            shots.add(s);
        }
        fx.add(px, PY - 11, 0, 0, 0.1f, 4, 0xFF7CF7FF, GameFx.SPARK, 0, 0);
        Sound.play("minecraft:block.note_block.bit", 1.8f, 0.2f);
    }

    private void kill(Alien a, float cx, float cy) {
        a.alive = false;
        int pts = TYPE_PTS[a.row];
        score += pts;
        int col = TYPE_COL[a.row];
        fx.burst(cx, cy, 16, col, 120, 2.4f, 0.55f, 60, GameFx.SQUARE);
        fx.ring(cx, cy, 16, col, 0.3f);
        fx.popup("+" + pts, cx, cy - 6, col, 8);
        fx.shake(0.07f);
        Sound.play("minecraft:entity.generic.explode", 2f, 0.12f);
        if (rnd.nextFloat() < 0.07f) {
            Power p = new Power();
            p.x = cx; p.y = cy;
            float r = rnd.nextFloat();
            p.kind = r < 0.35f ? P_TRIPLE : r < 0.7f ? P_RAPID : r < 0.9f ? P_SHIELD : P_LIFE;
            powers.add(p);
        }
    }

    private void collect(Power p) {
        String name;
        switch (p.kind) {
            case P_TRIPLE: tripleT = 9; name = "Dreifachschuss!"; break;
            case P_RAPID: rapidT = 9; name = "Schnellfeuer!"; break;
            case P_SHIELD: bubble = true; name = "Schutzschild!"; break;
            default: lives = Math.min(5, lives + 1); name = "+1 Leben!";
        }
        fx.popup(name, px, PY - 24, P_COL[p.kind], 11);
        fx.burst(px, PY - 6, 18, P_COL[p.kind], 120, 2.4f, 0.6f, 0, GameFx.SPARK);
        Sound.play("minecraft:block.amethyst_block.chime", 1.3f, 0.8f);
    }

    private void crash() {
        if (dead) return;
        dead = true; deadT = 0;
        lives = Math.max(0, lives - 1);
        fx.burst(px, PY, 36, 0xFF7CF7FF, 170, 3, 0.9f, 80, GameFx.SQUARE);
        fx.burst(px, PY, 20, 0xFFFFB347, 110, 2.6f, 0.7f, 0, GameFx.SPARK);
        fx.ring(px, PY, 44, 0xFF7CF7FF, 0.5f);
        fx.shake(0.85f);
        fx.flash(0xFFFF3B4E, 0.6f);
        tripleT = rapidT = 0;
        Sound.play("minecraft:entity.generic.explode", 0.8f, 0.5f);
    }

    private boolean hitShield(float x, float y) {
        for (int s = 0; s < 4; s++) {
            float sx = shieldX(s);
            int c = (int) Math.floor((x - sx) / CELL), r = (int) Math.floor((y - SHY) / CELL);
            if (c < 0 || c >= 12 || r < 0 || r >= 8 || !shields[s][r][c]) continue;
            shields[s][r][c] = false;
            for (int k = 0; k < 4; k++) {
                int rr = r + rnd.nextInt(3) - 1, cc = c + rnd.nextInt(3) - 1;
                if (rr >= 0 && rr < 8 && cc >= 0 && cc < 12) shields[s][rr][cc] = false;
            }
            return true;
        }
        return false;
    }

    private void eraseShield(float x, float y, float rad) {
        for (int s = 0; s < 4; s++) {
            float sx = shieldX(s);
            for (int r = 0; r < 8; r++)
                for (int c = 0; c < 12; c++)
                    if (shields[s][r][c] && Math.abs(sx + c * CELL + CELL / 2 - x) < rad && Math.abs(SHY + r * CELL + CELL / 2 - y) < rad) shields[s][r][c] = false;
        }
    }

    // ------------------------------------------------------------------ drawing
    @Override
    protected void render() {
        float dt = Math.min(0.05f, Gx.dt);
        anim += dt;
        GameFx.sprite(this, "ad_bg", 0, 0, W, H, 380, 300, AlienDefense::paintBg, 0xFFFFFFFF);
        Gx.push();
        Gx.translate(S(fx.sx), S(fx.sy));
        // mothership
        if (ufoX > -900) {
            Gx.glow(X(ufoX), Y(28), S(26), 0x44FF5CA8);
            GameFx.sprite(this, "ad_mother" + ((int) (anim * 6) % 2), ufoX - 17, 20, 34, 16, 34, 16, gr -> paintMother(gr, (int) (anim * 6) % 2), 0xFFFFFFFF);
        }
        // aliens
        for (Alien a : aliens) {
            if (!a.alive) continue;
            float ax = fX + a.col * SX, ay = fY + a.row * SY;
            final int t = a.type, fr = frame, row = a.row;
            float bob = (float) Math.sin(anim * 3 + a.col * 0.6f) * 0.8f;
            Gx.glow(X(ax + AW / 2), Y(ay + AH / 2), S(14), Gx.withAlpha(TYPE_COL[row], 0.14f));
            GameFx.sprite(this, "ad_alien" + t + fr + row, ax, ay + bob, AW, AH, 22, 16, gr -> paintAlien(gr, t, fr, TYPE_COL[row]), 0xFFFFFFFF);
        }
        // shields
        for (int s = 0; s < 4; s++) {
            float sx = shieldX(s);
            for (int r = 0; r < 8; r++)
                for (int c = 0; c < 12; c++)
                    if (shields[s][r][c]) box(sx + c * CELL, SHY + r * CELL, CELL + 0.2f, CELL + 0.2f, 0, r == 0 || !shields[s][r - 1][c] ? 0xFFF4F8FF : Gx.mix(0xFFBFE3FF, 0xFF7FA8D8, r / 8f));
        }
        // shots
        for (Shot s : shots) {
            if (s.enemy) {
                float zig = ((int) (s.y / 5) % 2 == 0) ? -1 : 1;
                Gx.glow(X(s.x), Y(s.y), S(6), 0x66FF5CA8);
                box(s.x - 1 + zig * 0.6f, s.y - 5, 2, 3, 0.5f, 0xFFFF9ACD);
                box(s.x - 1 - zig * 0.6f, s.y - 2, 2, 3, 0.5f, 0xFFFF9ACD);
            } else {
                Gx.glow(X(s.x), Y(s.y), S(6), 0x667CF7FF);
                box(s.x - 1, s.y - 5, 2, 8, 1, 0xFFE8FDFF);
            }
        }
        // power-ups
        for (Power p : powers) {
            float s = 1 + 0.1f * (float) Math.sin(p.t * 8);
            Gx.glow(X(p.x), Y(p.y), S(14), Gx.withAlpha(P_COL[p.kind], 0.4f));
            circle(p.x, p.y, 7 * s, 0xFF10142A);
            Gx.ringAt(X(p.x), Y(p.y), S(7 * s), Math.max(1, S(1.2f)), P_COL[p.kind]);
            Gx.icon(P_ICON[p.kind], X(p.x), Y(p.y), 8 * u, P_COL[p.kind]);
        }
        // player
        if (!dead && (invul <= 0 || ((int) (anim * 12)) % 2 == 0)) {
            if (bubble) { Gx.glow(X(px), Y(PY), S(22), 0x335CF2FF); Gx.ringAt(X(px), Y(PY - 1), S(15), Math.max(1, S(1)), 0x885CF2FF); }
            if (tripleT > 0 || rapidT > 0) Gx.glow(X(px), Y(PY), S(18), Gx.withAlpha(tripleT > 0 ? 0xFFFFB02E : 0xFFFFF27A, 0.3f));
            GameFx.sprite(this, "ad_player", px - 13, PY - 11, 26, 18, 26, 18, AlienDefense::paintPlayer, 0xFFFFFFFF);
        }
        fx.draw(this, 0, 0);
        Gx.pop();

        // HUD
        for (int i = 0; i < lives; i++) GameFx.sprite(this, "ad_player", 8 + i * 17, 6, 15, 10.4f, 26, 18, AlienDefense::paintPlayer, 0xDDFFFFFF);
        GameFx.outlined(this, "Welle " + wave, W - 32, 12, 7.5f, 0xFFB8FF4F, 0xAA000000);
        float py = 24;
        if (tripleT > 0) { bar(W - 8, py, P_ICON[P_TRIPLE], P_COL[P_TRIPLE], tripleT / 9); py += 12; }
        if (rapidT > 0) bar(W - 8, py, P_ICON[P_RAPID], P_COL[P_RAPID], rapidT / 9);
        if (bannerT >= 0) {
            float t = bannerT / 1.8f, s = Ease.outBack(Math.min(1, bannerT / 0.35f)), a = t < 0.7f ? 1 : 1 - (t - 0.7f) / 0.3f;
            Gx.pushAlpha(a);
            Gx.push();
            Gx.scaleAt(X(W / 2), Y(H * 0.55f), s);
            GameFx.outlined(this, "WELLE " + wave, W / 2, H * 0.55f, 20, 0xFFB8FF4F, 0xCC0A1A00);
            Gx.pop();
            Gx.popAlpha();
        }
        if (state == State.PLAYING && hintT < 3.5f) {
            float a = Math.min(1, 3.5f - hintT) * (0.6f + 0.4f * (float) Math.sin(anim * 5));
            text("← → bewegen  •  Leertaste schießen", W / 2, H * 0.66f, 7.6f, Fonts.BOLD, Gx.withAlpha(0xFFFFFFFF, a));
        }
        fx.drawPopups(this, 0, 0);
        fx.drawFlash(this, W, H);
    }

    private void bar(float right, float y, String icon, int col, float frac) {
        float w = 48;
        box(right - w, y - 5, w, 10, 5, 0x66000000);
        Gx.icon(icon, X(right - w + 6), Y(y), 6.5f * u, col);
        box(right - w + 12, y - 1.2f, w - 17, 2.4f, 1.2f, 0x33FFFFFF);
        box(right - w + 12, y - 1.2f, (w - 17) * frac, 2.4f, 1.2f, col);
    }

    // ------------------------------------------------------------------ painters
    private static void paintAlien(Graphics2D g, int type, int frame, int argb) {
        String[] rows = ART[type][frame];
        Color top = new Color(Gx.mix(argb, 0xFFFFFFFF, 0.35f)), bot = new Color(Gx.mix(argb, 0xFF000000, 0.25f));
        float p = 2;
        for (int r = 0; r < rows.length; r++)
            for (int c = 0; c < rows[r].length(); c++) {
                char ch = rows[r].charAt(c);
                if (ch == '.') continue;
                float x = c * p, y = r * p;
                if (ch == 'X') g.setPaint(new GradientPaint(0, 0, top, 0, 16, bot));
                else if (ch == 'W') g.setColor(Color.WHITE);
                else g.setColor(new Color(0x12101E));
                g.fill(new RoundRectangle2D.Float(x, y, p + 0.15f, p + 0.15f, 0.8f, 0.8f));
            }
    }

    private static void paintPlayer(Graphics2D g) {
        // Lego-style defense cannon
        g.setPaint(new GradientPaint(0, 8, new Color(0x9FF7FF), 0, 18, new Color(0x1E8FB0)));
        GeneralPath base = new GeneralPath();
        base.moveTo(1, 17); base.lineTo(3, 9); base.lineTo(23, 9); base.lineTo(25, 17); base.closePath();
        g.fill(base);
        g.setColor(new Color(0x0E4E66));
        g.setStroke(new BasicStroke(0.7f));
        g.draw(base);
        g.setPaint(new GradientPaint(0, 3, new Color(0xFFFFFF), 0, 10, new Color(0x7CF7FF)));
        g.fill(new RoundRectangle2D.Float(10, 2, 6, 9, 2, 2));
        g.setColor(new Color(0xFFFFFF));
        g.fill(new RoundRectangle2D.Float(11.5f, 0, 3, 3, 1, 1));
        // studs
        g.setColor(new Color(0xBFFBFF));
        for (int i = 0; i < 3; i++) g.fill(new RoundRectangle2D.Float(4.5f + i * 6.8f + (i == 1 ? 20 : 0) * 0, 7.2f, 3.4f, 2, 1, 1));
        g.setColor(new Color(255, 80, 120));
        g.fill(new Ellipse2D.Float(5, 12, 2.4f, 2.4f));
        g.fill(new Ellipse2D.Float(18.6f, 12, 2.4f, 2.4f));
    }

    private static void paintMother(Graphics2D g, int frame) {
        GeneralPath body = new GeneralPath();
        body.moveTo(1, 10); body.curveTo(4, 5, 30, 5, 33, 10); body.curveTo(30, 14, 4, 14, 1, 10); body.closePath();
        g.setPaint(new GradientPaint(0, 5, new Color(0xFF9ACD), 0, 14, new Color(0xA0205A)));
        g.fill(body);
        g.setPaint(new GradientPaint(0, 0, new Color(0xE8FDFF), 0, 7, new Color(0x7CF7FF)));
        g.fill(new Ellipse2D.Float(11, 1, 12, 9));
        for (int i = 0; i < 5; i++) {
            g.setColor((i + frame) % 2 == 0 ? new Color(0xFFF27A) : new Color(0x5CF2FF));
            g.fill(new Ellipse2D.Float(5 + i * 5.5f, 9.3f, 2.2f, 2.2f));
        }
    }

    private static void paintBg(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 0, new Color(0x05040E), 0, 300, new Color(0x151032)));
        g.fillRect(0, 0, 380, 300);
        Random r = new Random(8);
        for (int i = 0; i < 180; i++) {
            float s = 0.4f + r.nextFloat() * 1.1f;
            g.setColor(new Color(255, 255, 255, 30 + r.nextInt(140)));
            g.fill(new Ellipse2D.Float(r.nextFloat() * 380, r.nextFloat() * 280, s, s));
        }
        g.setPaint(new RadialGradientPaint(new Point2D.Float(300, 90), 120, new float[]{0f, 1f}, new Color[]{new Color(180, 60, 140, 50), new Color(180, 60, 140, 0)}));
        g.fillRect(0, 0, 380, 300);
        // planet horizon with snow
        g.setPaint(new GradientPaint(0, 284, new Color(0x2A3A6E), 0, 300, new Color(0x121A3A)));
        g.fill(new Ellipse2D.Float(-200, 284, 780, 200));
        g.setColor(new Color(200, 225, 255, 160));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new Ellipse2D.Float(-200, 284, 780, 200));
    }
}
