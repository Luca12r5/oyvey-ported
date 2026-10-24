package dev.lego.games;

import java.util.ArrayList;
import java.util.List;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Breakout with Lego bricks: paddle, ball, bricks (some need two hits), 3 lives, endless levels. */
public final class Breakout extends GameView {
    private static final float W = 360, H = 290, TOP = 22;
    private static final int COLS = 10, ROWS = 6;
    private static final float BW = 32, BH = 13, GAP = 2, X0 = 11, Y0 = 36;
    private static final float PY = 268, PH = 9, R = 4.5f;
    private static final int[] ROW_COLORS = {0xFFC91A09, 0xFFFE8A18, 0xFFF2CD37, 0xFF4B9F4A, 0xFF0055BF, 0xFF923978};
    private static final int HARD = 0xFFA0A5A9;

    private static final class Brick {
        float x, y;
        int hp, maxHp, color, row, col;
        float flash;
        boolean alive = true;
    }

    private final List<Brick> bricks = new ArrayList<>();
    private final List<float[]> parts = new ArrayList<>();   // x, y, vx, vy, t, color, size
    private final List<float[]> pops = new ArrayList<>();    // x, y, t, value
    private final float[][] trail = new float[10][2];
    private float padX, padW, ballX, ballY, vx, vy, speed, stuckT;
    private boolean stuck, left, right, mouseMode;
    private float lastMx = -9999;
    private int lives, level, combo;
    private float levelAnim, bannerT = -1, lifeFlash, padHit;

    public Breakout() { super("breakout"); }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }

    @Override
    protected void reset() {
        lives = 3;
        level = 1;
        padW = 60;
        padX = W / 2;
        left = right = false;
        parts.clear();
        pops.clear();
        lifeFlash = 0;
        build();
        bannerT = -1;
    }

    private void build() {
        bricks.clear();
        int hardRows = Math.min(4, 1 + (level - 1) / 2) + (level > 1 ? 1 : 0);
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                if (!pattern(level, r, c)) continue;
                Brick b = new Brick();
                b.row = r;
                b.col = c;
                b.x = X0 + c * (BW + GAP);
                b.y = Y0 + r * (BH + GAP);
                b.maxHp = b.hp = r < Math.min(hardRows, 2) || (level > 2 && r < hardRows && (c + r) % 3 == 0) ? 2 : 1;
                b.color = ROW_COLORS[r % ROW_COLORS.length];
                bricks.add(b);
            }
        levelAnim = 0;
        speed = 190 * (1 + 0.09f * (level - 1));
        combo = 0;
        stick();
    }

    private static boolean pattern(int level, int r, int c) {
        switch ((level - 1) % 4) {
            case 1: return Math.abs(c - 4.5f) <= r + 0.5f;                 // pyramid
            case 2: return (r + c) % 2 == 0 || r == 0 || r == ROWS - 1;     // checker
            case 3: return c != 4 && c != 5 || r % 2 == 0;                  // two towers
            default: return true;
        }
    }

    private void stick() {
        stuck = true;
        stuckT = 0;
        ballX = padX;
        ballY = PY - R - 0.5f;
        for (float[] t : trail) { t[0] = ballX; t[1] = ballY; }
    }

    private void launch() {
        if (!stuck) return;
        stuck = false;
        float ang = (float) Math.toRadians(-20 + rnd.nextFloat() * 40);
        vx = (float) Math.sin(ang) * speed;
        vy = -(float) Math.cos(ang) * speed;
        Sound.play("minecraft:block.note_block.hat", 1.4f, 0.25f);
    }

    // ------------------------------------------------------------------ input
    @Override
    protected void onKey(int k) {
        if (k == K_LEFT || k == K_A) { left = true; mouseMode = false; }
        if (k == K_RIGHT || k == K_D) { right = true; mouseMode = false; }
        if (k == K_SPACE || k == K_UP || k == K_W) launch();
    }

    @Override
    protected void onKeyUp(int k) {
        if (k == K_LEFT || k == K_A) left = false;
        if (k == K_RIGHT || k == K_D) right = false;
    }

    @Override
    protected void onClick(float x, float y, int button) {
        if (button == 0) launch();
    }

    // ------------------------------------------------------------------ logic
    @Override
    protected void update(float dt) {
        // paddle
        if (Math.abs(mx - lastMx) > 0.5f && mx >= 0) { if (lastMx > -9999) mouseMode = true; lastMx = mx; }
        if (mouseMode && u > 0) {
            float target = (mx - bx) / u;
            padX += (target - padX) * Math.min(1, dt * 25);
        } else {
            float dir = (right ? 1 : 0) - (left ? 1 : 0);
            padX += dir * 320 * dt;
        }
        padX = Math.max(padW / 2 + 2, Math.min(W - padW / 2 - 2, padX));
        padHit = Math.max(0, padHit - dt * 4);

        for (float[] p : parts) { p[0] += p[2] * dt; p[1] += p[3] * dt; p[3] += 300 * dt; p[4] += dt; }
        parts.removeIf(p -> p[4] > 0.8f);
        for (float[] p : pops) p[2] += dt;
        pops.removeIf(p -> p[2] > 0.8f);
        for (Brick b : bricks) b.flash = Math.max(0, b.flash - dt * 5);
        if (bannerT >= 0) { bannerT += dt; if (bannerT > 1.6f) bannerT = -1; }

        if (stuck) {
            ballX = padX;
            ballY = PY - R - 0.5f;
            stuckT += dt;
            if (stuckT > 2.5f) launch();
            for (float[] t : trail) { t[0] = ballX; t[1] = ballY; }
            return;
        }
        // trail
        for (int i = trail.length - 1; i > 0; i--) { trail[i][0] = trail[i - 1][0]; trail[i][1] = trail[i - 1][1]; }
        trail[0][0] = ballX;
        trail[0][1] = ballY;

        int steps = 4;
        float h = dt / steps;
        for (int s = 0; s < steps; s++) {
            ballX += vx * h;
            ballY += vy * h;
            // walls
            if (ballX < R) { ballX = R; vx = Math.abs(vx); wallSound(); }
            if (ballX > W - R) { ballX = W - R; vx = -Math.abs(vx); wallSound(); }
            if (ballY < TOP + R) { ballY = TOP + R; vy = Math.abs(vy); wallSound(); }
            // paddle
            if (vy > 0 && ballY + R >= PY && ballY - R < PY + PH && ballX > padX - padW / 2 - R && ballX < padX + padW / 2 + R) {
                float off = Math.max(-1, Math.min(1, (ballX - padX) / (padW / 2)));
                float ang = (float) Math.toRadians(off * 62);
                speed = Math.min(speed * 1.012f, 190 * (1 + 0.09f * (level - 1)) * 1.35f);
                vx = (float) Math.sin(ang) * speed;
                vy = -(float) Math.cos(ang) * speed;
                ballY = PY - R;
                combo = 0;
                padHit = 1;
                Sound.play("minecraft:block.note_block.basedrum", 1.2f, 0.3f);
            }
            // bricks
            for (Brick b : bricks) {
                if (!b.alive) continue;
                float cx = Math.max(b.x, Math.min(ballX, b.x + BW)), cy = Math.max(b.y, Math.min(ballY, b.y + BH));
                float dx = ballX - cx, dy = ballY - cy;
                if (dx * dx + dy * dy > R * R) continue;
                // side of impact: smaller penetration
                float penX = Math.min(ballX + R - b.x, b.x + BW - (ballX - R));
                float penY = Math.min(ballY + R - b.y, b.y + BH - (ballY - R));
                if (penX < penY) {
                    vx = ballX < b.x + BW / 2 ? -Math.abs(vx) : Math.abs(vx);
                } else {
                    vy = ballY < b.y + BH / 2 ? -Math.abs(vy) : Math.abs(vy);
                }
                hit(b);
                break;
            }
            if (ballY - R > H + 6) { lose(); return; }
        }
        if (bricks.stream().noneMatch(b -> b.alive)) nextLevel();
    }

    private void wallSound() { Sound.play("minecraft:block.note_block.hat", 1.8f, 0.12f); }

    private void hit(Brick b) {
        b.hp--;
        b.flash = 1;
        if (b.hp > 0) {
            Sound.play("minecraft:block.anvil.land", 1.9f, 0.12f);
            for (int i = 0; i < 4; i++) particle(b, HARD, 1.5f);
            return;
        }
        b.alive = false;
        combo++;
        int pts = (b.maxHp > 1 ? 20 : 10) * level + (combo > 1 ? (combo - 1) * 2 : 0);
        score += pts;
        pops.add(new float[]{b.x + BW / 2, b.y + BH / 2, 0, pts});
        for (int i = 0; i < 12; i++) particle(b, b.maxHp > 1 ? HARD : b.color, 2 + rnd.nextFloat() * 1.8f);
        Sound.play("minecraft:block.stone.break", 1.1f + Math.min(0.6f, combo * 0.06f), 0.3f);
    }

    private void particle(Brick b, int col, float size) {
        parts.add(new float[]{b.x + rnd.nextFloat() * BW, b.y + rnd.nextFloat() * BH, (rnd.nextFloat() - 0.5f) * 160, -30 - rnd.nextFloat() * 90, 0, col, size});
    }

    private void lose() {
        lives--;
        lifeFlash = 1;
        Sound.play("minecraft:entity.arrow.hit_player", 0.7f, 0.4f);
        if (lives <= 0) { gameOver(); return; }
        speed = 190 * (1 + 0.09f * (level - 1));
        stick();
    }

    private void nextLevel() {
        level++;
        score += 50L * (level - 1);
        bannerT = 0;
        Sound.play("minecraft:entity.player.levelup", 1.1f, 0.35f);
        build();
    }

    // ------------------------------------------------------------------ drawing
    @Override
    protected void render() {
        float dt = Math.min(0.05f, Gx.dt);
        levelAnim += dt;
        lifeFlash = Math.max(0, lifeFlash - dt * 1.5f);

        // background: subtle baseplate studs
        for (float y = TOP + 8; y < H; y += 16)
            for (float x = 8; x < W; x += 16) circle(x, y, 2.4f, Style.light ? 0x0E000000 : 0x07FFFFFF);
        // HUD
        box(8, TOP - 0.8f, W - 16, 0.8f, 0, Style.light ? 0x16000000 : 0x14FFFFFF);
        for (int i = 0; i < 3; i++) {
            boolean on = i < lives;
            float s = on ? 1 : 0.85f;
            int col = on ? 0xFFFF4D5E : (Style.light ? 0x33000000 : 0x33FFFFFF);
            if (!on && i == lives && lifeFlash > 0) col = Gx.mix(col, 0xFFFF4D5E, lifeFlash);
            Gx.icon(on ? "heart-fill" : "heart", X(12 + i * 15), Y(TOP / 2), 10 * u * s, col);
        }
        text("Level " + level, W - 30, TOP / 2, 7.5f, Fonts.BOLD, Style.accent);
        int left = 0;
        for (Brick b : bricks) if (b.alive) left++;
        text(left + " Steine", W / 2, TOP / 2, 6.8f, Fonts.MEDIUM, Style.sub);

        // bricks (staggered pop-in)
        for (Brick b : bricks) {
            if (!b.alive) continue;
            float t = Ease.stagger(levelAnim, b.row * COLS + b.col, 0.012f, 0.35f);
            if (t <= 0) continue;
            float sc = Ease.outBack(t);
            int col = b.maxHp > 1 ? (b.hp > 1 ? HARD : Gx.mix(HARD, 0xFF5A5F66, 0.35f)) : b.color;
            col = Gx.mix(col, 0xFFFFFFFF, b.flash * 0.6f);
            Gx.push();
            Gx.scaleAt(X(b.x + BW / 2), Y(b.y + BH / 2), sc);
            Gx.shadow(X(b.x), Y(b.y + 1), S(BW), S(BH), S(2.5f), S(2), 0.3f);
            brick(b.x, b.y, BW, BH, col);
            if (b.maxHp > 1 && b.hp == 1) {
                // cracks
                box(b.x + 7, b.y + 3, 1, 7, 0.5f, 0x88202226);
                box(b.x + 7, b.y + 9, 6, 1, 0.5f, 0x88202226);
                box(b.x + 22, b.y + 2, 1, 5, 0.5f, 0x88202226);
                box(b.x + 18, b.y + 6, 5, 1, 0.5f, 0x88202226);
            }
            Gx.pop();
        }

        // particles
        for (float[] p : parts) {
            float a = 1 - p[4] / 0.8f;
            box(p[0] - p[6] / 2, p[1] - p[6] / 2, p[6], p[6], 0.6f, Gx.withAlpha((int) p[5], a));
        }
        for (float[] p : pops) {
            float a = 1 - Ease.outCubic(p[2] / 0.8f);
            text("+" + (int) p[3], p[0], p[1] - p[2] * 22, 7.5f, Fonts.BOLD, Gx.withAlpha(0xFFFFFFFF, a));
        }

        // paddle: Lego plate with studs
        float px = padX - padW / 2, pyy = PY + padHit * 1.5f;
        Gx.glow(X(padX), Y(pyy + PH / 2), S(padW * 0.7f), Gx.withAlpha(Style.accent, 0.18f + padHit * 0.2f));
        Gx.shadow(X(px), Y(pyy + 1), S(padW), S(PH), S(3), S(3), 0.4f);
        boxV(px, pyy, padW, PH, 3, Gx.mix(Style.accent, 0xFFFFFFFF, 0.2f), Gx.mix(Style.accent, 0xFF000000, 0.2f));
        Gx.outline(X(px), Y(pyy), S(padW), S(PH), S(3), Math.max(1, S(0.5f)), Gx.withAlpha(Gx.mix(Style.accent, 0xFF000000, 0.5f), 0.7f));
        for (int i = 0; i < 6; i++) {
            float sx = px + padW * (i + 0.5f) / 6;
            box(sx - 3, pyy - 2.4f, 6, 3, 1, Gx.mix(Style.accent, 0xFFFFFFFF, 0.12f));
        }
        box(px + 2, pyy + 1.2f, padW - 4, 1.2f, 0.6f, 0x44FFFFFF);

        // ball + trail
        if (state != State.OVER || ballY < H) {
            if (!stuck)
                for (int i = trail.length - 1; i >= 0; i--) {
                    float a = (1 - i / (float) trail.length) * 0.3f;
                    circle(trail[i][0], trail[i][1], R * (1 - i / (float) trail.length * 0.6f), Gx.withAlpha(Style.accent, a));
                }
            Gx.glow(X(ballX), Y(ballY), S(R * 4), 0x44FFFFFF);
            circle(ballX, ballY, R, 0xFFFFFFFF);
            circle(ballX - R * 0.3f, ballY - R * 0.3f, R * 0.4f, 0xFFFFFFFF);
            circle(ballX + 0.4f, ballY + 0.6f, R * 0.75f, 0xFFE7EAF0);
            circle(ballX - R * 0.25f, ballY - R * 0.25f, R * 0.35f, 0xFFFFFFFF);
        }
        if (stuck && state == State.PLAYING) {
            float a = 0.55f + 0.45f * (float) Math.sin(levelAnim * 5);
            text("Leertaste oder Klick zum Abschießen", W / 2, PY - 34, 7, Fonts.BOLD, Gx.withAlpha(Style.sub, a));
        }
        if (lifeFlash > 0) box(0, 0, W, H, 12, Gx.withAlpha(0xFFFF4D5E, lifeFlash * 0.18f));

        // level banner
        if (bannerT >= 0) {
            float t = bannerT / 1.6f;
            float s = Ease.outBack(Math.min(1, bannerT / 0.35f));
            float a = t < 0.75f ? 1 : 1 - (t - 0.75f) / 0.25f;
            float cy = H * 0.6f;
            Gx.pushAlpha(a);
            Gx.push();
            Gx.scaleAt(X(W / 2), Y(cy), s);
            Gx.glow(X(W / 2), Y(cy), S(80), Gx.withAlpha(Style.accent, 0.3f));
            box(W / 2 - 60, cy - 16, 120, 32, 12, Gx.withAlpha(Style.accent, 0.95f));
            text("Level " + level, W / 2, cy, 13, Fonts.BOLD, Style.accentText);
            Gx.pop();
            Gx.popAlpha();
        }
    }
}
