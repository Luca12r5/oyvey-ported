package dev.lego.games;

import java.util.ArrayList;
import java.util.List;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Pong against a beatable AI; first to 7 wins. */
public final class Pong extends GameView {
    private static final float W = 380, H = 260;
    private static final float PW = 9, PH = 48, R = 5, MARGIN = 14;
    private static final int WIN = 7;
    private static final float BASE_SPEED = 205, MAX_SPEED = 470, AI_SPEED = 185;
    private static final int AI_COLOR = 0xFFC91A09;

    private float py, ay, bxp, byp, vx, vy, speed;
    private float serveT, aiTarget, aiThink, aiErr;
    private int pScore, aScore, hits;
    private boolean up, down, mouseMode, won;
    private float lastMy = -9999;
    private float pHit, aHit, goalFlash, goalSide, scorePopP, scorePopA, anim;
    private final float[][] trail = new float[10][2];
    private final List<float[]> parts = new ArrayList<>();   // x, y, vx, vy, t, color

    public Pong() { super("pong"); }

    @Override protected boolean hasAi() { return true; }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String overTitle() { return won ? "Gewonnen!" : "Verloren"; }

    @Override
    protected void reset() {
        py = ay = H / 2;
        pScore = aScore = 0;
        up = down = false;
        won = false;
        parts.clear();
        pHit = aHit = goalFlash = scorePopP = scorePopA = 0;
        aiTarget = H / 2;
        serve(rnd.nextBoolean() ? 1 : -1);
    }

    /** Ball to the center, launches towards dir (-1 = player, 1 = AI) after a short pause. */
    private void serve(int dir) {
        bxp = W / 2;
        byp = H / 2;
        speed = BASE_SPEED;
        hits = 0;
        serveT = 0.9f;
        float ang = (float) Math.toRadians(-25 + rnd.nextFloat() * 50);
        vx = dir * (float) Math.cos(ang) * speed;
        vy = (float) Math.sin(ang) * speed;
        for (float[] t : trail) { t[0] = bxp; t[1] = byp; }
    }

    @Override
    protected void onKey(int k) {
        if (k == K_UP || k == K_W) { up = true; mouseMode = false; }
        if (k == K_DOWN || k == K_S) { down = true; mouseMode = false; }
    }

    @Override
    protected void onKeyUp(int k) {
        if (k == K_UP || k == K_W) up = false;
        if (k == K_DOWN || k == K_S) down = false;
    }

    @Override
    protected void update(float dt) {
        // player paddle
        if (Math.abs(my - lastMy) > 0.5f && my >= 0) { if (lastMy > -9999) mouseMode = true; lastMy = my; }
        if (mouseMode && u > 0) {
            float target = (my - by) / u;
            py += (target - py) * Math.min(1, dt * 22);
        } else py += ((down ? 1 : 0) - (up ? 1 : 0)) * 300 * dt;
        py = clampPad(py);

        // AI: re-thinks a few times per second, with an aiming error, limited speed
        aiThink -= dt;
        if (aiThink <= 0) {
            aiThink = 0.1f + rnd.nextFloat() * 0.1f;
            if (vx > 0 && serveT <= 0) {
                aiTarget = predict() + aiErr;
            } else {
                aiTarget = H / 2 + (byp - H / 2) * 0.3f;
                aiErr = (rnd.nextFloat() - 0.5f) * (PH * 0.9f + hits * 4) * new float[]{1.9f, 1.25f, 0.8f, 0.35f}[aiLevel()];
            }
        }
        float d = aiTarget - ay;
        float maxMove = (AI_SPEED + Math.min(60, hits * 4)) * new float[]{0.62f, 0.85f, 1.05f, 1.35f}[aiLevel()] * dt;
        ay += Math.max(-maxMove, Math.min(maxMove, d * Math.min(1, dt * 10) * 6));
        ay = clampPad(ay);

        // effects
        pHit = Math.max(0, pHit - dt * 4);
        aHit = Math.max(0, aHit - dt * 4);
        scorePopP = Math.max(0, scorePopP - dt * 2.5f);
        scorePopA = Math.max(0, scorePopA - dt * 2.5f);
        for (float[] p : parts) { p[0] += p[2] * dt; p[1] += p[3] * dt; p[2] *= 0.96f; p[3] *= 0.96f; p[4] += dt; }
        parts.removeIf(p -> p[4] > 0.5f);

        if (serveT > 0) {
            serveT -= dt;
            for (float[] t : trail) { t[0] = bxp; t[1] = byp; }
            return;
        }
        for (int i = trail.length - 1; i > 0; i--) { trail[i][0] = trail[i - 1][0]; trail[i][1] = trail[i - 1][1]; }
        trail[0][0] = bxp;
        trail[0][1] = byp;

        int steps = 3;
        float h = dt / steps;
        for (int s = 0; s < steps; s++) {
            bxp += vx * h;
            byp += vy * h;
            if (byp < R) { byp = R; vy = Math.abs(vy); Sound.play("minecraft:block.note_block.hat", 1.7f, 0.12f); }
            if (byp > H - R) { byp = H - R; vy = -Math.abs(vy); Sound.play("minecraft:block.note_block.hat", 1.7f, 0.12f); }
            // player paddle
            float plx = MARGIN + PW;
            if (vx < 0 && bxp - R <= plx && bxp - R > MARGIN - 6 && Math.abs(byp - py) <= PH / 2 + R) {
                bounce(py, 1);
                bxp = plx + R;
                pHit = 1;
            }
            float arx = W - MARGIN - PW;
            if (vx > 0 && bxp + R >= arx && bxp + R < W - MARGIN + 6 && Math.abs(byp - ay) <= PH / 2 + R) {
                bounce(ay, -1);
                bxp = arx - R;
                aHit = 1;
            }
            if (bxp < -R * 2) { point(false); return; }
            if (bxp > W + R * 2) { point(true); return; }
        }
    }

    private float clampPad(float y) { return Math.max(PH / 2 + 3, Math.min(H - PH / 2 - 3, y)); }

    private void bounce(float padY, int dir) {
        float off = Math.max(-1, Math.min(1, (byp - padY) / (PH / 2)));
        float ang = (float) Math.toRadians(off * 55);
        hits++;
        speed = Math.min(MAX_SPEED, speed * 1.07f);
        vx = dir * (float) Math.cos(ang) * speed;
        vy = (float) Math.sin(ang) * speed;
        int col = dir > 0 ? Style.accent : AI_COLOR;
        for (int i = 0; i < 8; i++) {
            float a = (float) ((rnd.nextFloat() - 0.5f) * Math.PI * 0.9f);
            parts.add(new float[]{bxp, byp, dir * (float) Math.cos(a) * (60 + rnd.nextFloat() * 90), (float) Math.sin(a) * (60 + rnd.nextFloat() * 90), 0, col});
        }
        Sound.play("minecraft:block.note_block.basedrum", 1.0f + Math.min(0.8f, hits * 0.05f), 0.3f);
        if (dir < 0) aiErr = 0;
    }

    /** Predicted ball y when it reaches the AI paddle (with wall bounces). */
    private float predict() {
        float x = bxp, y = byp, dvx = vx, dvy = vy;
        float tx = W - MARGIN - PW - R;
        if (dvx <= 0) return H / 2;
        float t = (tx - x) / dvx;
        float ny = y + dvy * t;
        float span = H - 2 * R;
        float m = ((ny - R) % (2 * span) + 2 * span) % (2 * span);
        return R + (m > span ? 2 * span - m : m);
    }

    private void point(boolean player) {
        goalFlash = 1;
        goalSide = player ? 1 : -1;
        for (float[] t : trail) { t[0] = W / 2; t[1] = H / 2; }
        if (player) {
            pScore++;
            score = pScore;
            scorePopP = 1;
            Sound.play("minecraft:entity.experience_orb.pickup", 1.2f, 0.35f);
        } else {
            aScore++;
            scorePopA = 1;
            Sound.play("minecraft:block.note_block.bass", 0.8f, 0.35f);
        }
        if (pScore >= WIN || aScore >= WIN) {
            won = pScore >= WIN;
            if (won) score = pScore + 5;
            bxp = W / 2;
            byp = H / 2;
            serveT = 99;
            gameOver();
            return;
        }
        serve(player ? 1 : -1);
    }

    // ------------------------------------------------------------------ drawing
    @Override
    protected void render() {
        float dt = Math.min(0.05f, Gx.dt);
        anim += dt;
        goalFlash = Math.max(0, goalFlash - dt * 1.8f);

        // field
        boxV(0, 0, W, H, 12, Style.light ? 0xFFE4E7EC : 0xFF0E1016, Style.light ? 0xFFDADDE3 : 0xFF0A0B0F);
        for (float y = 10; y < H; y += 16)
            for (float x = 10; x < W; x += 16) circle(x, y, 2.3f, Style.light ? 0x0D000000 : 0x06FFFFFF);
        // side glows
        Gx.glow(X(0), Y(H / 2), S(90), Gx.withAlpha(Style.accent, 0.08f + (goalSide < 0 ? goalFlash * 0.4f : 0)));
        Gx.glow(X(W), Y(H / 2), S(90), Gx.withAlpha(AI_COLOR, 0.08f + (goalSide > 0 ? goalFlash * 0.4f : 0)));
        // center line of little studs
        for (float y = 6; y < H; y += 14) box(W / 2 - 2, y, 4, 8, 2, Style.light ? 0x1A000000 : 0x14FFFFFF);
        Gx.ringAt(X(W / 2), Y(H / 2), S(34), Math.max(1, S(1.2f)), Style.light ? 0x14000000 : 0x10FFFFFF);

        // scores
        scoreNum(pScore, W / 2 - 34, scorePopP, Style.accent, "DU");
        scoreNum(aScore, W / 2 + 34, scorePopA, Style.light ? 0xFF15161A : 0xFFFFFFFF, "KI");
        // progress studs to 7
        for (int i = 0; i < WIN; i++) {
            circle(W / 2 - 22 - i * 7, 62, 2, i < pScore ? Style.accent : (Style.light ? 0x1A000000 : 0x18FFFFFF));
            circle(W / 2 + 22 + i * 7, 62, 2, i < aScore ? AI_COLOR : (Style.light ? 0x1A000000 : 0x18FFFFFF));
        }

        // paddles
        paddle(MARGIN, py, Style.accent, pHit, 1);
        paddle(W - MARGIN - PW, ay, AI_COLOR, aHit, -1);

        // particles
        for (float[] p : parts) {
            float a = 1 - p[4] / 0.5f;
            circle(p[0], p[1], 1.6f * a + 0.4f, Gx.withAlpha((int) p[5], a));
        }

        // ball
        if (state != State.OVER) {
            boolean blink = serveT > 0 && serveT < 5 && (int) (serveT * 8) % 2 == 0;
            if (serveT <= 0)
                for (int i = trail.length - 1; i >= 0; i--) {
                    float a = (1 - i / (float) trail.length) * 0.28f;
                    circle(trail[i][0], trail[i][1], R * (1 - i / (float) trail.length * 0.6f), Gx.withAlpha(vx > 0 ? Style.accent : AI_COLOR, a));
                }
            if (!blink) {
                Gx.glow(X(bxp), Y(byp), S(R * 4), 0x44FFFFFF);
                circle(bxp, byp + 0.6f, R, 0xFFC9CDD4);
                circle(bxp, byp, R, 0xFFFFFFFF);
                Gx.ringAt(X(bxp), Y(byp), S(R * 0.62f), Math.max(1, S(0.7f)), 0x33000000);
            }
        }
        if (state == State.PLAYING && serveT > 0 && pScore + aScore == 0) {
            float a = Ease.outCubic(Math.min(1, serveT / 0.3f));
            text("Erster bei 7 gewinnt!", W / 2, H / 2 + 50, 8, Fonts.BOLD, Gx.withAlpha(Style.sub, a));
        }
    }

    private void scoreNum(int v, float cx, float pop, int col, String label) {
        float s = 1 + 0.35f * Ease.outCubic(pop);
        Gx.push();
        Gx.scaleAt(X(cx), Y(34), s);
        if (pop > 0) Gx.glow(X(cx), Y(34), S(34), Gx.withAlpha(col, pop * 0.4f));
        text(String.valueOf(v), cx, 34, 30, Fonts.BOLD, Gx.withAlpha(col, 0.92f));
        Gx.pop();
        text(label, cx, 12, 6.5f, Fonts.BOLD, Style.muted);
    }

    /** Vertical Lego brick paddle with studs on the inner side. */
    private void paddle(float x, float cy, int col, float hit, int side) {
        float y = cy - PH / 2;
        float off = -side * hit * 2;
        x += off;
        Gx.glow(X(x + PW / 2), Y(cy), S(PH * 0.8f), Gx.withAlpha(col, 0.14f + hit * 0.3f));
        // studs pointing towards the field
        for (int i = 0; i < 4; i++) {
            float sy = y + PH * (i + 0.5f) / 4 - 3;
            float sx = side > 0 ? x + PW - 0.5f : x - 2.5f;
            box(sx, sy, 3, 6, 1, Gx.mix(col, 0xFFFFFFFF, 0.12f));
        }
        Gx.shadow(X(x), Y(y + 1), S(PW), S(PH), S(3), S(3), 0.4f);
        boxV(x, y, PW, PH, 3, Gx.mix(col, 0xFFFFFFFF, 0.2f), Gx.mix(col, 0xFF000000, 0.2f));
        box(x + 1.5f, y + 2, 1.5f, PH - 4, 0.75f, 0x40FFFFFF);
        Gx.outline(X(x), Y(y), S(PW), S(PH), S(3), Math.max(1, S(0.5f)), Gx.withAlpha(Gx.mix(col, 0xFF000000, 0.5f), 0.7f));
        if (hit > 0) box(x, y, PW, PH, 3, Gx.withAlpha(0xFFFFFFFF, hit * 0.4f));
    }
}
