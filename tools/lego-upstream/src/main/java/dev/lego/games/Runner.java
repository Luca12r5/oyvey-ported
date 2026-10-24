package dev.lego.games;

import java.util.ArrayList;
import java.util.List;

import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Endless Lego parkour runner: jump over brick cacti, duck under birds. */
public final class Runner extends GameView {
    private static final float W = 400, H = 240, GY = 196, PX = 56;
    private static final float GRAVITY = 1650, JUMP_V = -470, CUT_V = -170;

    // player
    private float py, vy, runPhase, duckAnim, squash;
    private boolean onGround, jumpHeld, duck, fastFall;
    private float jumpBuffer, coyote;
    // world
    private float speed, dist, nextSpawn, shake, flash, deadT, milestoneT;
    private boolean dead;
    private long lastMilestone;
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final List<float[]> parts = new ArrayList<>();   // x, y, vx, vy, life, max, size, color
    private final float[] cloudX = new float[5], cloudY = new float[5], cloudS = new float[5];

    private static final class Obstacle {
        float x, y, w, h;      // hitbox (board units), x = left edge
        boolean bird;
        int[] cols;            // cactus: brick count per column
        float colW;
        float phase;
    }

    public Runner() { super("runner"); }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Distanz"; }

    @Override
    protected void reset() {
        py = GY;
        vy = 0;
        onGround = true;
        jumpHeld = duck = fastFall = dead = false;
        jumpBuffer = coyote = 0;
        runPhase = duckAnim = squash = 0;
        speed = 165;
        dist = 0;
        nextSpawn = 260;
        shake = flash = deadT = milestoneT = 0;
        lastMilestone = 0;
        obstacles.clear();
        parts.clear();
        for (int i = 0; i < cloudX.length; i++) {
            cloudX[i] = i * (W / cloudX.length) + rnd.nextFloat() * 40;
            cloudY[i] = 22 + rnd.nextFloat() * 60;
            cloudS[i] = 0.7f + rnd.nextFloat() * 0.6f;
        }
    }

    // ------------------------------------------------------------------ input
    private void pressJump() {
        jumpHeld = true;
        jumpBuffer = 0.12f;
    }

    private void releaseJump() {
        jumpHeld = false;
        if (!onGround && vy < CUT_V) vy = CUT_V;
    }

    @Override
    protected void onKey(int k) {
        if (k == K_SPACE || k == K_UP || k == K_W) pressJump();
        if (k == K_DOWN || k == K_S) duck = true;
    }

    @Override
    protected void onKeyUp(int k) {
        if (k == K_SPACE || k == K_UP || k == K_W) releaseJump();
        if (k == K_DOWN || k == K_S) duck = false;
    }

    @Override protected void onClick(float x, float y, int button) { if (button == 0) pressJump(); }
    @Override protected void onRelease(float x, float y, int button) { if (button == 0) releaseJump(); }

    // ------------------------------------------------------------------ update
    @Override
    protected void update(float dt) {
        shake = Math.max(0, shake - dt * 2.2f);
        flash = Math.max(0, flash - dt * 2.5f);
        milestoneT = Math.max(0, milestoneT - dt);
        squash = Math.max(0, squash - dt * 5);
        updateParticles(dt);
        if (dead) {
            deadT += dt;
            if (deadT > 0.75f) gameOver();
            return;
        }

        speed = Math.min(430, speed + dt * 4.2f);
        float move = speed * dt;
        dist += move;
        score = (long) (dist / 10);
        if (score / 100 > lastMilestone) {
            lastMilestone = score / 100;
            milestoneT = 1.2f;
            Sound.play("minecraft:block.note_block.pling", 1.5f, 0.3f);
        }
        runPhase += dt * (8 + speed / 28);

        // clouds
        for (int i = 0; i < cloudX.length; i++) {
            cloudX[i] -= move * 0.12f * cloudS[i];
            if (cloudX[i] < -70) { cloudX[i] = W + 20 + rnd.nextFloat() * 80; cloudY[i] = 22 + rnd.nextFloat() * 60; cloudS[i] = 0.7f + rnd.nextFloat() * 0.6f; }
        }

        // jumping
        jumpBuffer = Math.max(0, jumpBuffer - dt);
        coyote = onGround ? 0.08f : Math.max(0, coyote - dt);
        if (jumpBuffer > 0 && (onGround || coyote > 0)) {
            vy = JUMP_V;
            onGround = false;
            coyote = 0;
            jumpBuffer = 0;
            squash = 0.6f;
            dust(PX + 8, GY, 5);
            Sound.play("minecraft:entity.chicken.egg", 1.6f + rnd.nextFloat() * 0.2f, 0.18f);
            if (!jumpHeld) vy = CUT_V * 1.6f;
        }
        if (!onGround) {
            fastFall = duck;
            vy += GRAVITY * (fastFall ? 2.6f : 1f) * dt;
            py += vy * dt;
            if (py >= GY) {
                py = GY;
                vy = 0;
                onGround = true;
                squash = 1;
                dust(PX + 8, GY, 7);
            }
        }
        duckAnim += ((duck && onGround ? 1 : 0) - duckAnim) * Math.min(1, dt * 22);
        if (onGround && rnd.nextFloat() < 0.18f) dust(PX + 3, GY, 1);

        // obstacles
        for (Obstacle o : obstacles) {
            o.x -= move * (o.bird ? 1.12f : 1f);
            o.phase += dt * 9;
        }
        obstacles.removeIf(o -> o.x + o.w < -30);
        nextSpawn -= move;
        if (nextSpawn <= 0) spawn();

        // collision (slightly forgiving hitbox)
        float[] hb = playerBox();
        for (Obstacle o : obstacles) {
            float ix = o.bird ? 4 : 2.5f, iy = o.bird ? 3 : 2.5f;
            if (hb[0] < o.x + o.w - ix && hb[0] + hb[2] > o.x + ix && hb[1] < o.y + o.h - iy && hb[1] + hb[3] > o.y + iy) {
                die();
                return;
            }
        }
    }

    private void die() {
        dead = true;
        deadT = 0;
        shake = 1;
        flash = 1;
        Sound.play("minecraft:entity.player.hurt", 1f, 0.35f);
        Sound.play("minecraft:block.stone.break", 0.9f, 0.3f);
        float cx = PX + 8, cy = py - 15;
        int[] cs = {Style.accent, 0xFFFFCD03, 0xFF2B6CD9, 0xFFFFFFFF};
        for (int i = 0; i < 22; i++) {
            float a = (float) (rnd.nextFloat() * Math.PI * 2), s = 60 + rnd.nextFloat() * 160;
            parts.add(new float[]{cx, cy, (float) Math.cos(a) * s, (float) Math.sin(a) * s - 80, 0, 0.7f + rnd.nextFloat() * 0.5f, 2 + rnd.nextFloat() * 3, cs[rnd.nextInt(cs.length)]});
        }
    }

    private void dust(float x, float y, int n) {
        int c = Style.light ? 0xFF9A8F80 : 0xFFB9B2A6;
        for (int i = 0; i < n; i++)
            parts.add(new float[]{x + rnd.nextFloat() * 8 - 4, y - 1, -speed * 0.3f - rnd.nextFloat() * 40, -20 - rnd.nextFloat() * 40, 0, 0.35f + rnd.nextFloat() * 0.25f, 1.2f + rnd.nextFloat() * 1.6f, c});
    }

    private void updateParticles(float dt) {
        for (float[] p : parts) {
            p[0] += p[2] * dt;
            p[1] += p[3] * dt;
            p[3] += 500 * dt;
            p[4] += dt;
            if (p[1] > GY + 3 && p[3] > 0) { p[1] = GY + 3; p[3] *= -0.35f; p[2] *= 0.7f; }
        }
        parts.removeIf(p -> p[4] >= p[5]);
    }

    private void spawn() {
        Obstacle o = new Obstacle();
        boolean birds = score > 220;
        if (birds && rnd.nextFloat() < 0.3f) {
            o.bird = true;
            o.w = 22;
            o.h = 13;
            boolean high = rnd.nextBoolean();
            o.y = high ? GY - 37 : GY - 21;     // high: duck under it, low: jump over it
            o.x = W + 10;
            o.phase = rnd.nextFloat() * 6;
        } else {
            int columns = 1 + rnd.nextInt(score > 120 ? 3 : 2);
            o.colW = 11 + rnd.nextInt(2) * 3;
            o.cols = new int[columns];
            int maxH = 1;
            for (int i = 0; i < columns; i++) {
                o.cols[i] = 1 + rnd.nextInt(score > 60 ? 4 : 3);
                maxH = Math.max(maxH, o.cols[i]);
            }
            o.w = columns * o.colW;
            o.h = maxH * 10;
            o.y = GY - o.h;
            o.x = W + 10;
        }
        obstacles.add(o);
        float min = 150 + speed * 0.55f;
        nextSpawn = min + rnd.nextFloat() * (140 + speed * 0.3f);
        if (rnd.nextFloat() < 0.12f && !o.bird) nextSpawn = Math.max(34, o.w + 22);   // occasional close pair
    }

    /** x, y, w, h of the player's hitbox. */
    private float[] playerBox() {
        if (duckAnim > 0.5f) return new float[]{PX - 3, py - 17, 24, 16};
        return new float[]{PX + 2, py - 30, 13, 29};
    }

    // ------------------------------------------------------------------ render
    @Override
    protected void render() {
        float t = time;
        float sx = 0, sy = 0;
        if (shake > 0) {
            float s = shake * shake * 5;
            sx = (float) Math.sin(t * 91) * s;
            sy = (float) Math.cos(t * 77) * s;
        }
        Gx.push();
        Gx.translate(S(sx), S(sy));

        // sky
        int skyTop = Style.light ? 0xFFBFD9F2 : 0xFF121A2B;
        int skyBot = Style.light ? 0xFFF4E8D8 : 0xFF232033;
        int g1 = Style.light ? 0xFFC9B79A : 0xFF2A2621, g2 = Style.light ? 0xFFB09C7E : 0xFF1A1714;
        boxV(0, GY - 14, W, H - GY + 14, 12, g1, g2);
        boxV(0, 0, W, GY, 12, skyTop, skyBot);
        box(0, GY - 14, W, 15, 0, skyBot);
        // sun / moon glow
        Gx.glow(X(W - 70), Y(52), S(70), Gx.withAlpha(Style.light ? 0xFFFFE6A0 : Style.accent, Style.light ? 0.7f : 0.22f));
        circle(W - 70, 52, 13, Style.light ? 0xFFFFE08A : Gx.mix(Style.accent, 0xFFF4F1E6, 0.85f));
        if (!Style.light) { circle(W - 74, 49, 2.5f, 0x18000000); circle(W - 65, 57, 1.8f, 0x18000000); }

        // clouds
        for (int i = 0; i < cloudX.length; i++) cloud(cloudX[i], cloudY[i], cloudS[i]);

        // mountains (stepped brick pyramids, two parallax layers)
        mountains(dist * 0.08f, 150, 0.6f, Style.light ? 0xFFB5C6DD : 0xFF1F2638);
        mountains(dist * 0.2f + 97, 110, 0.85f, Style.light ? 0xFF94ABCA : 0xFF2B3452);

        // ground
        box(0, GY - 1.2f, W, 2.4f, 0, Style.light ? 0xFF7F6E55 : 0xFF6D6254);
        float off = dist % 26;
        for (float x = -off; x < W + 26; x += 26) box(x, GY + 7, 12, 2, 1, Style.light ? 0x40000000 : 0x30FFFFFF);
        float off2 = (dist * 1.0f) % 61;
        for (float x = -off2; x < W + 61; x += 61) {
            box(x + 20, GY + 17, 5, 2, 1, Style.light ? 0x30000000 : 0x22FFFFFF);
            box(x + 44, GY + 29, 8, 2, 1, Style.light ? 0x26000000 : 0x1AFFFFFF);
        }

        // obstacles
        for (Obstacle o : obstacles) {
            if (o.bird) bird(o);
            else cactus(o);
        }

        // player
        if (!dead || deadT < 0.08f) player();
        else {
            float a = Math.max(0, 1 - deadT * 3);
            if (a > 0) { Gx.pushAlpha(a); player(); Gx.popAlpha(); }
        }

        // particles
        for (float[] p : parts) {
            float a = 1 - p[4] / p[5];
            box(p[0] - p[6] / 2, p[1] - p[6] / 2, p[6], p[6], p[6] * 0.25f, Gx.withAlpha((int) p[7], a));
        }
        Gx.pop();

        // HUD: distance counter (top right) + speed
        String sc = String.format("%05d", score);
        boolean ms = milestoneT > 0 && ((int) (milestoneT * 8)) % 2 == 0;
        int hud = ms ? Style.accent : (Style.light ? 0xFF3A3F4A : 0xFFE6E8EE);
        box(W - 86, 8, 78, 22, 11, Style.light ? 0x66FFFFFF : 0x55000000);
        Gx.icon("sprint", X(W - 73), Y(19), S(10), Gx.withAlpha(Style.accent, 0.9f));
        Gx.textMid(sc, X(W - 62), Y(19), S(10), Fonts.BOLD, hud);
        if (GameInfo.hasBest(info.id)) {
            String hi = "HI " + String.format("%05d", GameInfo.best(info.id));
            Gx.textRight(hi, X(W - 92), Y(19), S(7), Fonts.MEDIUM, Style.light ? 0x99000000 : 0x99FFFFFF);
        }
        float mult = speed / 165f;
        box(8, 8, 50, 22, 11, Style.light ? 0x66FFFFFF : 0x55000000);
        Gx.icon("speed", X(20), Y(19), S(10), Gx.withAlpha(Style.accent, 0.9f));
        Gx.textMid(String.format(java.util.Locale.ROOT, "x%.1f", mult), X(28), Y(19), S(8), Fonts.BOLD, Style.light ? 0xFF3A3F4A : 0xFFE6E8EE);
        if (milestoneT > 0) {
            float k = Math.min(1, (1.2f - milestoneT) / 0.25f), a = Math.min(1, milestoneT / 0.3f);
            text(lastMilestone * 100 + "!", W / 2, 40 - 6 * k, 14, Fonts.BOLD, Gx.withAlpha(Style.accent, a));
        }

        if (flash > 0) box(-10, -10, W + 20, H + 20, 0, Gx.withAlpha(0xFFFFFFFF, flash * flash * 0.75f));
        if (dead) box(-10, -10, W + 20, H + 20, 0, Gx.withAlpha(0xFFFF2030, Math.min(0.18f, deadT * 0.4f)));
    }

    private void cloud(float x, float y, float s) {
        int c = Style.light ? 0xCCFFFFFF : 0x22FFFFFF;
        box(x, y, 46 * s, 12 * s, 6 * s, c);
        box(x + 9 * s, y - 7 * s, 22 * s, 12 * s, 6 * s, c);
        box(x + 22 * s, y - 4 * s, 16 * s, 10 * s, 5 * s, c);
    }

    private void mountains(float scroll, float period, float scale, int col) {
        float off = scroll % (period * 2);
        for (float x = -off - period; x < W + period * 2; x += period) {
            int seed = (int) Math.floor((x + scroll) / period + 0.5f);
            float hgt = (40 + ((seed * 37) & 31)) * scale;
            int steps = 5;
            float stepH = hgt / steps;
            for (int i = 0; i < steps; i++) {
                float w = (period * 0.9f) * (1 - i / (float) steps);
                float bx0 = x + (period * 0.9f - w) / 2;
                float yy = GY - (i + 1) * stepH;
                box(bx0, yy, w, stepH + 1, 1.5f, Gx.mix(col, Style.light ? 0xFFFFFFFF : 0xFF000000, i * 0.03f));
                box(bx0, yy, w, 1.2f, 0.6f, Gx.withAlpha(0xFFFFFFFF, Style.light ? 0.35f : 0.06f));   // top highlight
                if (i == steps - 1) {
                    float sw0 = Math.min(8, w * 0.4f);
                    box(bx0 + w / 2 - sw0 / 2, yy - 2.4f, sw0, 3, 1.2f, Gx.mix(col, 0xFFFFFFFF, 0.08f));   // stud
                }
            }
        }
    }

    private void cactus(Obstacle o) {
        int base = 0xFF2DB84C;
        for (int c = 0; c < o.cols.length; c++) {
            float cx = o.x + c * o.colW;
            for (int i = 0; i < o.cols[c]; i++) {
                int col = Gx.mix(base, (i + c) % 2 == 0 ? 0xFF1E8F3A : 0xFF4ACF62, 0.35f);
                brick(cx + 0.3f, GY - (i + 1) * 10, o.colW - 0.6f, 10, col);
            }
        }
        // soft shadow on the ground
        box(o.x - 2, GY - 0.5f, o.w + 4, 3, 1.5f, 0x33000000);
    }

    private void bird(Obstacle o) {
        float x = o.x, y = o.y;
        float flap = (float) Math.sin(o.phase);
        int body = 0xFFE3000B, wing = 0xFFB0000A;
        // shadow on the ground
        box(x + 4, GY - 0.5f, 14, 2.5f, 1.2f, 0x22000000);
        // back wing
        float wy = flap * 6;
        box(x + 8, y + 3 - Math.max(0, wy), 8, Math.abs(wy) + 3, 1.5f, Gx.mix(wing, 0xFF000000, 0.2f));
        // body
        boxV(x + 3, y + 3, 17, 8, 3, Gx.mix(body, 0xFFFFFFFF, 0.2f), body);
        // head
        boxV(x, y + 1, 8, 7, 2.5f, Gx.mix(body, 0xFFFFFFFF, 0.2f), body);
        box(x + 1.5f, y - 0.8f, 4, 2, 0.8f, Gx.mix(body, 0xFFFFFFFF, 0.3f));   // stud
        // beak
        box(x - 4, y + 4, 5, 2.6f, 1, 0xFFFFCD03);
        // eye
        circle(x + 2.6f, y + 3.6f, 1.1f, 0xFFFFFFFF);
        circle(x + 2.3f, y + 3.6f, 0.55f, 0xFF111111);
        // tail
        box(x + 19, y + 3, 4, 3, 1, wing);
        // front wing
        float fy = -flap * 7;
        if (fy < 0) box(x + 7, y + 5 + fy, 9, -fy + 2, 1.5f, wing);
        else box(x + 7, y + 6, 9, fy + 2, 1.5f, wing);
    }

    private void player() {
        float x = PX, foot = py;
        int skin = 0xFFFFCD03, shirt = Style.accent, pants = 0xFF2B6CD9;
        int shirtD = Gx.mix(shirt, 0xFF000000, 0.25f), pantsD = Gx.mix(pants, 0xFF000000, 0.3f);
        float st = squash * (onGround ? 1 : -0.6f);   // >0: squashed (landing), <0: stretched
        // ground shadow
        float air = Math.max(0, GY - foot);
        float sw = Math.max(6, 18 - air * 0.12f);
        box(x + 8 - sw / 2, GY - 1, sw, 3, 1.5f, Gx.withAlpha(0xFF000000, 0.28f * Math.max(0.2f, 1 - air / 90)));

        Gx.push();
        float scY = 1 - st * 0.12f;
        Gx.translate(0, 0);
        if (duckAnim > 0.02f) {
            // ducking pose: body horizontal, head forward
            float d = duckAnim;
            float lp = (float) Math.sin(runPhase);
            box(x - 2 + lp * 2, foot - 5, 7, 5, 1.5f, pantsD);
            box(x + 6 - lp * 2, foot - 5, 7, 5, 1.5f, pants);
            float ty = foot - 5 - 9 * d - (1 - d) * 14;
            boxV(x - 4, ty, 20, 9, 2.5f, Gx.mix(shirt, 0xFFFFFFFF, 0.15f), shirtD);
            box(x + 10, ty + 5, 8, 3, 1.5f, skin);   // arm forward
            // head
            boxV(x + 14, ty - 3, 9, 9, 3, Gx.mix(skin, 0xFFFFFFFF, 0.2f), skin);
            box(x + 16, ty - 5, 5, 2.5f, 1, Gx.mix(skin, 0xFFFFFFFF, 0.25f));
            circle(x + 20.5f, ty + 0.8f, 0.9f, 0xFF1A1A1A);
            box(x + 19, ty + 3.2f, 3, 0.9f, 0.4f, 0xFF1A1A1A);
        } else {
            float h = 30 * scY;
            float k = h / 30f;
            float lp = onGround ? (float) Math.sin(runPhase) : 0.6f;
            float lift1 = onGround ? Math.max(0, lp) * 3 : 2, lift2 = onGround ? Math.max(0, -lp) * 3 : 0;
            // legs
            float legTop = foot - 10 * k;
            box(x + 2 + lp * 3, legTop, 6, 10 * k - lift1, 1.5f, pantsD);
            box(x + 8 - lp * 3, legTop, 6, 10 * k - lift2, 1.5f, pants);
            // hips
            box(x + 1.5f, legTop - 1, 13, 3, 1, pants);
            // back arm
            float ap = onGround ? -lp : -1;
            box(x + 5 + ap * 3, legTop - 11 * k, 4, 9 * k, 2, Gx.mix(shirtD, 0xFF000000, 0.15f));
            // torso (trapezoid-ish)
            float tt = legTop - 11 * k;
            boxV(x + 1, tt, 15, 11 * k, 2.5f, Gx.mix(shirt, 0xFFFFFFFF, 0.18f), shirtD);
            box(x + 3, tt + 2, 3, 3, 1, Gx.withAlpha(0xFFFFFFFF, 0.35f));   // logo dot
            // front arm + hand
            box(x + 8 - ap * 3, tt + 1, 4, 8 * k, 2, shirt);
            circle(x + 10 - ap * 3, tt + 1 + 8 * k, 2, skin);
            // head
            float ht = tt - 9 * k;
            box(x + 6, ht + 7 * k, 5, 2, 0.8f, skin);   // neck
            boxV(x + 3.5f, ht, 10, 8 * k, 3, Gx.mix(skin, 0xFFFFFFFF, 0.22f), skin);
            box(x + 5.5f, ht - 2.2f, 6, 2.6f, 1, Gx.mix(skin, 0xFFFFFFFF, 0.28f));   // stud
            // face
            circle(x + 11.2f, ht + 3, 0.9f, 0xFF1A1A1A);
            box(x + 9.6f, ht + 5.4f, 3, 0.9f, 0.4f, 0xFF1A1A1A);
        }
        Gx.pop();
    }
}
