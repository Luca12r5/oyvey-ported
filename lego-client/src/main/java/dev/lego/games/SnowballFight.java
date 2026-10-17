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

/** Schneeballschlacht: pull back with the mouse (slingshot) and throw snowballs at elves popping up behind snow walls. */
public final class SnowballFight extends GameView {
    private static final float W = 400, H = 270, G = 400, BALL = 3.6f, ROUND = 60;
    private static final float OX = 64, OY = 192;                      // throw origin
    private static final float[] SLOT_X = {150, 222, 294, 360}, SLOT_G = {238, 222, 204, 184};
    private static final float WALL_W = 38, WALL_H = 20, ELF_H = 30;

    private static final class Elf {
        int slot;
        float t, up, stay, hitT = -1;
        boolean gold, deer, thrown;
    }

    private static final class Ball {
        float x, y, vx, vy, t;
        boolean enemy;
    }

    private static final class Balloon {
        float x, y, t;
        boolean hit;
        float hitT;
    }

    private final Elf[] elves = new Elf[4];
    private final List<Ball> balls = new ArrayList<>();
    private final GameFx fx = new GameFx();
    private final float[][] splats = new float[6][4];   // x, y, size, life
    private Balloon balloon;
    private float spawnT, reload, throwAnim, anim, balloonT, hintT;
    private float dragX, dragY, curX, curY;
    private boolean aiming;
    private int combo, hits, throwsN, bestCombo;

    public SnowballFight() { super("snowball"); }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String overTitle() { return "Zeit um!"; }

    @Override
    protected void reset() {
        for (int i = 0; i < elves.length; i++) elves[i] = null;
        balls.clear();
        fx.clear();
        for (float[] s : splats) s[3] = 0;
        balloon = null;
        spawnT = 0.8f; reload = 0; throwAnim = 0; balloonT = 9; hintT = 0;
        aiming = false;
        combo = hits = throwsN = bestCombo = 0;
    }

    private float left() { return Math.max(0, ROUND - time); }
    private int mult() { return Math.min(5, 1 + combo / 3); }

    private static float groundAt(float x) {
        float g = 246;
        for (int i = 0; i < SLOT_X.length; i++) {
            float dx = (x - SLOT_X[i]) / 62f;
            if (Math.abs(dx) < 1.6f) g = Math.min(g, SLOT_G[i] + dx * dx * 22);
        }
        if (x < 92) g = Math.min(g, 240);
        return g;
    }

    // ------------------------------------------------------------------ input
    @Override
    protected void onClick(float x, float y, int button) {
        if (button != 0) return;
        aiming = true;
        dragX = curX = x;
        dragY = curY = y;
    }

    @Override protected void onDrag(float x, float y) { if (aiming) { curX = x; curY = y; } }

    @Override
    protected void onRelease(float x, float y, int button) {
        if (!aiming) return;
        aiming = false;
        curX = x; curY = y;
        float[] v = launchVel();
        if (v == null) return;
        if (reload > 0) return;
        Ball b = new Ball();
        b.x = OX; b.y = OY; b.vx = v[0]; b.vy = v[1];
        balls.add(b);
        reload = 0.32f;
        throwAnim = 1;
        throwsN++;
        Sound.play("minecraft:entity.snowball.throw", 0.8f + v[2] * 0.4f, 0.5f);
    }

    /** vx, vy, power of the current pull; null if too short. */
    private float[] launchVel() {
        float dx = dragX - curX, dy = dragY - curY;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 6) return null;
        float p = Math.min(1, len / 90f);
        float sp = 150 + 300 * p;
        return new float[]{dx / len * sp, dy / len * sp, p};
    }

    // ------------------------------------------------------------------ logic
    @Override
    protected void update(float dt) {
        fx.update(dt);
        hintT += dt;
        reload = Math.max(0, reload - dt);
        throwAnim = Math.max(0, throwAnim - dt * 4);
        for (float[] s : splats) s[3] = Math.max(0, s[3] - dt * 0.55f);
        if (left() <= 0) { gameOver(); return; }

        // elves
        float diff = Ease.clamp(time / ROUND);
        spawnT -= dt;
        if (spawnT <= 0) {
            spawnT = Ease.lerp(1.1f, 0.5f, diff) * (0.7f + rnd.nextFloat() * 0.6f);
            int s = rnd.nextInt(4);
            if (elves[s] == null) {
                Elf e = new Elf();
                e.slot = s;
                e.stay = Ease.lerp(2.0f, 1.05f, diff) * (0.8f + rnd.nextFloat() * 0.4f);
                float r = rnd.nextFloat();
                e.gold = r < 0.08f;
                e.deer = !e.gold && r < 0.2f;
                elves[s] = e;
            }
        }
        for (int i = 0; i < elves.length; i++) {
            Elf e = elves[i];
            if (e == null) continue;
            e.t += dt;
            if (e.hitT >= 0) { e.hitT += dt; e.up = Math.max(0, e.up - dt * 3); if (e.hitT > 0.6f) elves[i] = null; continue; }
            if (e.t < 0.22f) e.up = Ease.outBack(e.t / 0.22f);
            else if (e.t < 0.22f + e.stay) e.up = 1;
            else { e.up = Math.max(0, 1 - (e.t - 0.22f - e.stay) / 0.2f); if (e.up <= 0) { elves[i] = null; continue; } }
            if (!e.deer && !e.thrown && e.t > 0.22f + e.stay * 0.55f && rnd.nextFloat() < 0.012f + diff * 0.02f) {
                e.thrown = true;
                Ball b = new Ball();
                b.enemy = true;
                b.x = SLOT_X[i] + 6; b.y = SLOT_G[i] - WALL_H - 18;
                float T = 1.25f - diff * 0.25f, tx = 58, ty = 186;
                b.vx = (tx - b.x) / T;
                b.vy = (ty - b.y) / T - 0.5f * G * T;
                balls.add(b);
                Sound.play("minecraft:entity.snowball.throw", 1.3f, 0.3f);
            }
        }
        // balloon
        balloonT -= dt;
        if (balloon == null && balloonT <= 0) { balloon = new Balloon(); balloon.x = W + 20; balloon.y = 40 + rnd.nextFloat() * 40; balloonT = 12 + rnd.nextFloat() * 8; }
        if (balloon != null) {
            balloon.t += dt;
            if (balloon.hit) { balloon.hitT += dt; balloon.y += 90 * dt * (1 + balloon.hitT * 4); if (balloon.hitT > 1) balloon = null; }
            else { balloon.x -= 38 * dt; if (balloon.x < -30) balloon = null; }
        }

        // balls
        for (int i = balls.size() - 1; i >= 0; i--) {
            Ball b = balls.get(i);
            b.t += dt;
            b.vy += G * dt;
            b.x += b.vx * dt;
            b.y += b.vy * dt;
            if (!b.enemy && b.t % 0.05f < dt) fx.add(b.x, b.y, 0, 0, 0.25f, 1.6f, 0x88FFFFFF, GameFx.DOT, 0, 0);
            if (collide(b)) { balls.remove(i); continue; }
            if (b.x > W + 20 || b.x < -20 || b.y > H + 20) { balls.remove(i); if (!b.enemy) miss(); }
        }
    }

    /** true = the ball is used up. */
    private boolean collide(Ball b) {
        if (b.enemy) {
            if (b.x < 84 && b.y > 168) {
                splat(b.x, b.y);
                for (float[] s : splats) if (s[3] <= 0.05f) { s[0] = 60 + rnd.nextFloat() * (W - 120); s[1] = 50 + rnd.nextFloat() * (H - 110); s[2] = 38 + rnd.nextFloat() * 26; s[3] = 1; break; }
                time += 3;
                fx.popup("-3 s", 70, 160, 0xFFFF8A8A, 12);
                fx.shake(0.6f);
                combo = 0;
                Sound.play("minecraft:block.snow.break", 0.6f, 0.9f);
                return true;
            }
            for (Ball o : balls) {
                if (o.enemy || o == b) continue;
                float dx = o.x - b.x, dy = o.y - b.y;
                if (dx * dx + dy * dy < 81) {
                    o.t = 99;
                    o.x = -999;
                    score += 5L * mult();
                    fx.burst(b.x, b.y, 18, 0xFFFFFFFF, 130, 3, 0.5f, 200, GameFx.FLAKE);
                    fx.ring(b.x, b.y, 18, 0xFF9FD8FF, 0.3f);
                    fx.popup("Abgefangen! +" + 5 * mult(), b.x, b.y - 8, 0xFF9FD8FF, 9);
                    Sound.play("minecraft:block.powder_snow.break", 1.4f, 0.6f);
                    return true;
                }
            }
        } else {
            // elves
            for (int i = 0; i < elves.length; i++) {
                Elf e = elves[i];
                if (e == null || e.hitT >= 0 || e.up < 0.35f) continue;
                float top = SLOT_G[i] - WALL_H - ELF_H * e.up;
                if (b.x > SLOT_X[i] - 11 && b.x < SLOT_X[i] + 11 && b.y > top - 2 && b.y < SLOT_G[i] - WALL_H + 2) { hitElf(e, b); return true; }
            }
            if (balloon != null && !balloon.hit) {
                float dx = b.x - balloon.x, dy = b.y - (balloon.y - 6);
                if (dx * dx + dy * dy < 150) {
                    balloon.hit = true;
                    combo++;
                    int pts = 40 * mult();
                    score += pts;
                    fx.confetti(balloon.x, balloon.y, 30, new int[]{0xFFE3263B, 0xFFF4C152, 0xFF1FA35C, 0xFFFFFFFF}, 170, 4);
                    fx.popup("Ballon! +" + pts, balloon.x, balloon.y - 14, 0xFFF4C152, 12);
                    fx.shake(0.3f);
                    Sound.play("minecraft:entity.firework_rocket.blast", 1.2f, 0.5f);
                    return true;
                }
            }
        }
        // walls
        for (int i = 0; i < SLOT_X.length; i++) {
            float wx = SLOT_X[i] - WALL_W / 2, wy = SLOT_G[i] - WALL_H;
            if (b.x > wx && b.x < wx + WALL_W && b.y > wy && b.y < SLOT_G[i] + 6) { splat(b.x, b.y); if (!b.enemy) miss(); return true; }
        }
        if (b.x > 20 && b.x < 84 && b.y > 196 && !b.enemy) return false;
        if (b.y > groundAt(b.x)) { splat(b.x, groundAt(b.x)); if (!b.enemy) miss(); return true; }
        return false;
    }

    private void hitElf(Elf e, Ball b) {
        e.hitT = 0;
        float x = SLOT_X[e.slot], y = SLOT_G[e.slot] - WALL_H - ELF_H * 0.6f;
        fx.burst(b.x, b.y, 16, 0xFFFFFFFF, 140, 3.2f, 0.55f, 250, GameFx.FLAKE);
        if (e.deer) {
            combo = 0;
            score = Math.max(0, score - 30);
            fx.popup("Nicht das Rentier! -30", x, y - 16, 0xFFFF8A8A, 10);
            fx.shake(0.4f);
            Sound.play("minecraft:entity.goat.hurt", 1.2f, 0.5f);
            return;
        }
        combo++;
        hits++;
        bestCombo = Math.max(bestCombo, combo);
        int base = 10 + e.slot * 5 + (e.gold ? 40 : 0);
        int pts = base * mult();
        score += pts;
        if (e.gold) { time -= 3; fx.popup("+3 s", x, y - 30, 0xFF9FFFB5, 10); }
        fx.popup("+" + pts + (mult() > 1 ? " x" + mult() : ""), x, y - 14, e.gold ? 0xFFF4C152 : 0xFFFFFFFF, 11);
        fx.burst(x, y - 6, 8, e.gold ? 0xFFF4C152 : 0xFFFFE066, 90, 2.2f, 0.6f, 0, GameFx.SPARK);
        fx.shake(0.18f);
        if (combo % 5 == 0) { fx.popup(combo + "er Serie!", W / 2, 60, 0xFFFF7BD5, 15); Sound.play("minecraft:entity.player.levelup", 1.4f, 0.3f); }
        Sound.play("minecraft:entity.player.hurt_freeze", 1.3f, 0.5f);
        Sound.play("minecraft:entity.experience_orb.pickup", 1.0f + Math.min(1f, combo * 0.05f), 0.3f);
    }

    private void splat(float x, float y) {
        fx.burst(x, y, 10, 0xFFFFFFFF, 90, 2.8f, 0.45f, 300, GameFx.FLAKE);
        Sound.play("minecraft:block.snow.hit", 1.2f, 0.4f);
    }

    private void miss() { combo = 0; }

    // ------------------------------------------------------------------ drawing
    @Override
    protected void render() {
        float dt = Math.min(0.05f, Gx.dt);
        anim += dt;
        GameFx.sprite(this, "sb_bg", 0, 0, W, H, 400, 270, SnowballFight::paintSky, 0xFFFFFFFF);
        // balloon
        if (balloon != null) {
            float by0 = balloon.y + (float) Math.sin(balloon.t * 2) * 4;
            Gx.pushAlpha(balloon.hit ? Math.max(0, 1 - balloon.hitT) : 1);
            GameFx.sprite(this, "sb_balloon" + (balloon.hit ? 1 : 0), balloon.x - 12, by0 - 22, 24, 40, 24, 40, gr -> paintBalloon(gr, balloon.hit), 0xFFFFFFFF);
            Gx.popAlpha();
        }
        Gx.push();
        Gx.translate(S(fx.sx), S(fx.sy));
        // slots, far to near
        for (int i = SLOT_X.length - 1; i >= 0; i--) {
            Elf e = elves[i];
            float wy = SLOT_G[i] - WALL_H;
            if (e != null) elf(e, i, wy);
            GameFx.sprite(this, "sb_hill", SLOT_X[i] - 100, SLOT_G[i] - 3, 200, 70, 200, 70, SnowballFight::paintHill, 0xFFFFFFFF);
            GameFx.sprite(this, "sb_wall", SLOT_X[i] - WALL_W / 2 - 2, wy - 4, WALL_W + 4, WALL_H + 8, 42, 28, SnowballFight::paintWall, 0xFFFFFFFF);
        }
        // own fort
        box(0, 240, W, 40, 0, 0xFFE8F0FC);
        kid();
        GameFx.sprite(this, "sb_fort", 16, 196, 74, 50, 74, 50, SnowballFight::paintFort, 0xFFFFFFFF);
        // balls
        for (Ball b : balls) {
            if (b.x < -100) continue;
            circle(b.x + 0.8f, b.y + 1, BALL, 0x33203050);
            circle(b.x, b.y, BALL, b.enemy ? 0xFFDDE8F8 : 0xFFFFFFFF);
            circle(b.x - 1, b.y - 1, BALL * 0.45f, 0xFFFFFFFF);
        }
        // aim preview
        if (aiming && state == State.PLAYING) {
            float[] v = launchVel();
            if (v != null) {
                float x = OX, y = OY, vx = v[0], vy = v[1];
                for (int k = 0; k < 22; k++) {
                    for (int s = 0; s < 4; s++) { vy += G * 0.012f; x += vx * 0.012f; y += vy * 0.012f; }
                    if (y > groundAt(x) || x > W) break;
                    float a = (1 - k / 22f) * (reload > 0 ? 0.35f : 0.9f);
                    circle(x, y, 2.1f - k * 0.04f, Gx.withAlpha(0xFF1A3F91, a * 0.5f));
                    circle(x, y, 1.4f - k * 0.03f, Gx.withAlpha(0xFFFFFFFF, a));
                }
                // power meter next to the kid
                int pc = Gx.mix(0xFF4AD66D, 0xFFFF4D5E, v[2]);
                box(20, 150, 6, 34, 3, 0x66000000);
                box(20, 150 + 34 * (1 - v[2]), 6, 34 * v[2], 3, pc);
            }
            // pull indicator
            circle(dragX, dragY, 3, 0x66FFFFFF);
            Gx.ringAt(X(dragX), Y(dragY), S(7), Math.max(1, S(1)), 0x66FFFFFF);
            circle(curX, curY, 2.5f, 0xAAFFFFFF);
        }
        fx.draw(this, 0, 0);
        Gx.pop();

        // splats on the "screen"
        for (float[] s : splats) {
            if (s[3] <= 0) continue;
            float a = Math.min(1, s[3] * 1.6f), sz = s[2] * (1 + 0.15f * (1 - Ease.clamp((1 - s[3]) * 8)));
            GameFx.sprite(this, "sb_splat", s[0] - sz / 2, s[1] - sz / 2 + (1 - s[3]) * 20, sz, sz, 64, 64, SnowballFight::paintSplat, Gx.withAlpha(0xFFFFFFFF, a));
        }

        // HUD: time bar + stats
        float lf = left() / ROUND;
        box(8, 8, W - 16, 7, 3.5f, 0x55000000);
        int tc = lf < 0.2f ? Gx.mix(0xFFFF4D5E, 0xFFFFFFFF, 0.5f + 0.5f * (float) Math.sin(anim * 12)) : 0xFF9FD8FF;
        box(8, 8, (W - 16) * lf, 7, 3.5f, tc);
        box(W / 2 - 18, 18, 36, 15, 7.5f, 0x66000000);
        text(String.format(java.util.Locale.ROOT, "%.0f s", Math.ceil(left())), W / 2, 25.5f, 7.5f, Fonts.BOLD, 0xFFFFFFFF);
        box(8, 18, 104, 15, 7.5f, 0x66000000);
        text("Treffer " + hits + (throwsN > 0 ? "  •  " + Math.round(100f * hits / throwsN) + " %" : ""), 60, 25.5f, 6.8f, Fonts.MEDIUM, 0xFFFFFFFF);
        if (combo >= 2) {
            box(W - 78, 18, 70, 15, 7.5f, Gx.withAlpha(0xFFE3263B, 0.9f));
            text("Serie " + combo + "  x" + mult(), W - 43, 25.5f, 6.8f, Fonts.BOLD, 0xFFFFFFFF);
        }
        if (reload > 0) box(OX - 10, 212, 20 * (1 - reload / 0.32f), 2, 1, 0xAAFFFFFF);
        if (state == State.PLAYING && hintT < 4 && !aiming) {
            float a = Math.min(1, 4 - hintT) * (0.6f + 0.4f * (float) Math.sin(anim * 5));
            box(W / 2 - 120, 110, 240, 22, 11, Gx.withAlpha(0xFF000000, 0.45f * a));
            text("Maus gedrückt halten, zurückziehen & loslassen!", W / 2, 121, 7.4f, Fonts.BOLD, Gx.withAlpha(0xFFFFFFFF, a));
        }
        fx.drawPopups(this, 0, 0);
        fx.drawFlash(this, W, H);
    }

    private void elf(Elf e, int i, float wy) {
        float x = SLOT_X[i];
        float top = wy - ELF_H * e.up + (e.hitT >= 0 ? e.hitT * 10 : 0);
        String key = e.deer ? "sb_deer" : e.gold ? "sb_elf_g" : "sb_elf";
        boolean hit = e.hitT >= 0;
        if (e.gold) Gx.glow(X(x), Y(top + 10), S(26), 0x44F4C152);
        float wob = hit ? 0 : (float) Math.sin(e.t * 9) * 0.6f;
        GameFx.sprite(this, key + (hit ? "h" : ""), x - 14 + wob, top - 4, 28, ELF_H + 8, 28, 38, gr -> paintElf(gr, e.deer, e.gold, hit), 0xFFFFFFFF);
        if (hit && !e.deer) {
            for (int k = 0; k < 3; k++) {
                double a = anim * 6 + k * 2.1;
                Gx.icon("star-fill", X(x + (float) Math.cos(a) * 10), Y(top + 2 + (float) Math.sin(a) * 3), 5 * u, 0xFFFFE066);
            }
        }
    }

    private void kid() {
        float lift = Ease.outCubic(throwAnim) * 3;
        float aimLean = aiming ? -1.5f : 0;
        GameFx.sprite(this, "sb_kid" + (throwAnim > 0.3f ? 1 : 0), 38 + aimLean, 164 - lift, 34, 44, 34, 44, gr -> paintKid(gr, throwAnim > 0.3f), 0xFFFFFFFF);
    }

    // ------------------------------------------------------------------ painters
    private static void paintSky(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 0, new Color(0x6FA8E8), 0, 200, new Color(0xD9ECFF)));
        g.fillRect(0, 0, 400, 270);
        g.setPaint(new RadialGradientPaint(new Point2D.Float(330, 50), 70, new float[]{0f, 1f}, new Color[]{new Color(255, 250, 225, 200), new Color(255, 250, 225, 0)}));
        g.fill(new Ellipse2D.Float(260, -20, 140, 140));
        g.setColor(new Color(0xFFF8DC));
        g.fill(new Ellipse2D.Float(318, 38, 24, 24));
        // far mountains
        g.setColor(new Color(0xA9C7EC));
        GeneralPath m = new GeneralPath();
        m.moveTo(0, 190); m.lineTo(40, 140); m.lineTo(80, 165); m.lineTo(140, 110); m.lineTo(200, 160); m.lineTo(260, 120); m.lineTo(320, 150); m.lineTo(370, 115); m.lineTo(400, 135); m.lineTo(400, 270); m.lineTo(0, 270); m.closePath();
        g.fill(m);
        g.setColor(new Color(0xF4F8FF));
        GeneralPath caps = new GeneralPath();
        caps.moveTo(128, 121); caps.lineTo(140, 110); caps.lineTo(154, 124); caps.lineTo(146, 121); caps.lineTo(140, 126); caps.lineTo(134, 120); caps.closePath();
        caps.moveTo(250, 128); caps.lineTo(260, 120); caps.lineTo(272, 130); caps.lineTo(262, 128); caps.closePath();
        caps.moveTo(360, 123); caps.lineTo(370, 115); caps.lineTo(382, 124); caps.lineTo(371, 122); caps.closePath();
        g.fill(caps);
        // clouds
        g.setColor(new Color(255, 255, 255, 200));
        g.fill(GameFx.union(new Ellipse2D.Float(40, 40, 40, 18), new Ellipse2D.Float(58, 30, 36, 24), new Ellipse2D.Float(80, 40, 36, 18)));
        g.fill(GameFx.union(new Ellipse2D.Float(190, 60, 34, 14), new Ellipse2D.Float(204, 52, 30, 20), new Ellipse2D.Float(222, 60, 30, 14)));
        // pine forest line
        Random r = new Random(3);
        for (int i = 0; i < 30; i++) {
            float tx = i * 14 + r.nextFloat() * 6, ty = 186 + r.nextFloat() * 10, s = 0.5f + r.nextFloat() * 0.4f;
            g.setColor(new Color(0x5E86B3));
            g.fillPolygon(new int[]{(int) tx, (int) (tx - 9 * s), (int) (tx + 9 * s)}, new int[]{(int) (ty - 34 * s), (int) ty, (int) ty}, 3);
        }
        g.setColor(new Color(0xCFE0F5));
        g.fillRect(0, 192, 400, 80);
    }

    private static void paintHill(Graphics2D g) {
        GeneralPath p = new GeneralPath();
        p.moveTo(0, 70);
        for (int x = 0; x <= 200; x += 5) {
            float dx = (x - 100) / 62f;
            p.lineTo(x, 3 + dx * dx * 22);
        }
        p.lineTo(200, 70); p.closePath();
        g.setPaint(new GradientPaint(0, 0, new Color(0xFFFFFF), 0, 60, new Color(0xB9CFEE)));
        g.fill(p);
        g.setColor(new Color(150, 180, 225, 120));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(p);
    }

    private static void paintWall(Graphics2D g) {
        // stacked snow blocks
        g.setColor(new Color(60, 90, 140, 50));
        g.fill(new RoundRectangle2D.Float(2, 8, 40, 20, 6, 6));
        for (int row = 0; row < 2; row++)
            for (int c = 0; c < 3 - row; c++) {
                float x = 2 + c * 13 + row * 6.5f, y = 16 - row * 9;
                RoundRectangle2D b = new RoundRectangle2D.Float(x, y, 12.5f, 10, 5, 5);
                g.setPaint(new GradientPaint(0, y, new Color(0xFFFFFF), 0, y + 10, new Color(0xC6D8F2)));
                g.fill(b);
                g.setColor(new Color(150, 180, 225));
                g.setStroke(new BasicStroke(0.8f));
                g.draw(b);
            }
        g.setColor(new Color(0xF4F8FF));
        g.fill(new RoundRectangle2D.Float(0, 24, 42, 4, 4, 4));
    }

    private static void paintFort(Graphics2D g) {
        GeneralPath p = new GeneralPath();
        p.moveTo(0, 50); p.lineTo(0, 20); p.curveTo(4, 10, 14, 10, 18, 16); p.curveTo(24, 6, 36, 6, 42, 16); p.curveTo(48, 8, 60, 10, 66, 20); p.lineTo(74, 50); p.closePath();
        g.setPaint(new GradientPaint(0, 8, new Color(0xFFFFFF), 0, 50, new Color(0xB8CDEB)));
        g.fill(p);
        g.setColor(new Color(150, 180, 225));
        g.setStroke(new BasicStroke(1f));
        g.draw(p);
        // snowball stockpile
        for (int i = 0; i < 4; i++) {
            float x = 8 + i * 8 - (i == 3 ? 12 : 0), y = i == 3 ? 36 : 42;
            g.setPaint(new RadialGradientPaint(new Point2D.Float(x + 2, y + 2), 5, new float[]{0f, 1f}, new Color[]{Color.WHITE, new Color(0xC9DAF2)}));
            g.fill(new Ellipse2D.Float(x, y, 8, 8));
        }
    }

    private static void paintKid(Graphics2D g, boolean throwing) {
        Color coat = new Color(0x2F6FE0), coatD = new Color(0x1A3F91);
        // body
        g.setPaint(new GradientPaint(0, 20, coat, 0, 44, coatD));
        g.fill(new RoundRectangle2D.Float(8, 22, 18, 20, 8, 8));
        // scarf
        g.setColor(new Color(0xE3263B));
        g.fill(new RoundRectangle2D.Float(8, 20, 18, 5, 4, 4));
        g.fill(new RoundRectangle2D.Float(8, 22, 5, 10, 3, 3));
        // head
        g.setColor(new Color(0xFFD9BC));
        g.fill(new Ellipse2D.Float(9, 6, 16, 16));
        g.setColor(new Color(0x1B1B22));
        g.fill(new Ellipse2D.Float(18, 12, 2.4f, 2.8f));
        g.setColor(new Color(255, 120, 120, 120));
        g.fill(new Ellipse2D.Float(19, 16, 4, 2.4f));
        g.setColor(new Color(0x6A3A1A));
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(17, 15, 5, 3, 200, 140);
        // bobble hat
        g.setColor(new Color(0xE3263B));
        g.fill(new java.awt.geom.Arc2D.Float(8, 3, 18, 16, 0, 180, java.awt.geom.Arc2D.CHORD));
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Float(8, 9, 18, 4, 4, 4));
        g.fill(new Ellipse2D.Float(14, 0, 6, 6));
        // arm
        g.setColor(coat);
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (throwing) g.drawLine(22, 26, 31, 16);
        else g.drawLine(12, 27, 5, 21);
        g.setColor(Color.WHITE);
        if (!throwing) { g.fill(new Ellipse2D.Float(0.5f, 15, 7, 7)); g.setColor(new Color(0xC9DAF2)); g.fill(new Ellipse2D.Float(3, 19, 3, 2)); }
    }

    private static void paintElf(Graphics2D g, boolean deer, boolean gold, boolean hit) {
        if (deer) {
            // reindeer head with antlers
            g.setColor(new Color(0x6E4622));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(9, 10, 5, 2); g.drawLine(6, 5, 2, 5); g.drawLine(19, 10, 23, 2); g.drawLine(22, 5, 26, 5);
            g.setPaint(new GradientPaint(0, 8, new Color(0xA0703C), 0, 30, new Color(0x6E4622)));
            g.fill(new Ellipse2D.Float(6, 8, 16, 18));
            g.fill(new RoundRectangle2D.Float(8, 22, 12, 16, 6, 6));
            g.setColor(new Color(0xD9B48A));
            g.fill(new Ellipse2D.Float(9, 16, 10, 9));
            g.setColor(new Color(0xE3263B));
            g.fill(new Ellipse2D.Float(11, 18, 6, 5));
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Float(15, 19, 1.5f, 1.5f));
            g.setColor(new Color(0x1B1B22));
            g.fill(new Ellipse2D.Float(9, 13, 3, 3));
            g.fill(new Ellipse2D.Float(16, 13, 3, 3));
            return;
        }
        Color hat = gold ? new Color(0xF4C152) : new Color(0x1FA35C), hatD = gold ? new Color(0xB8862A) : new Color(0x0E5E34);
        // body
        g.setPaint(new GradientPaint(0, 24, hat, 0, 38, hatD));
        g.fill(new RoundRectangle2D.Float(7, 24, 14, 14, 6, 6));
        g.setColor(new Color(0xE3263B));
        g.fill(new RoundRectangle2D.Float(6, 23, 16, 4, 3, 3));
        // ears
        g.setColor(new Color(0xF7C9A5));
        g.fillPolygon(new int[]{7, 1, 8}, new int[]{15, 11, 19}, 3);
        g.fillPolygon(new int[]{21, 27, 20}, new int[]{15, 11, 19}, 3);
        // head
        g.fill(new Ellipse2D.Float(6, 10, 16, 15));
        // face
        g.setColor(new Color(0x1B1B22));
        if (hit) {
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(9, 14, 12, 17); g.drawLine(12, 14, 9, 17);
            g.drawLine(16, 14, 19, 17); g.drawLine(19, 14, 16, 17);
            g.setColor(Color.WHITE);
            g.fill(GameFx.union(new Ellipse2D.Float(5, 11, 12, 10), new Ellipse2D.Float(11, 14, 10, 9)));
        } else {
            g.fill(new Ellipse2D.Float(9.5f, 14, 2.6f, 3));
            g.fill(new Ellipse2D.Float(16, 14, 2.6f, 3));
            g.setColor(new Color(0xB5452F));
            g.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawArc(11, 17, 6, 4, 200, 140);
            g.setColor(new Color(255, 120, 120, 120));
            g.fill(new Ellipse2D.Float(7.5f, 18, 3.5f, 2));
            g.fill(new Ellipse2D.Float(17.5f, 18, 3.5f, 2));
        }
        // pointy hat with bell
        GeneralPath h = new GeneralPath();
        h.moveTo(5, 12); h.curveTo(8, 2, 14, -1, 25, 3); h.lineTo(22, 6); h.curveTo(22, 8, 23, 10, 23, 12); h.closePath();
        g.setPaint(new GradientPaint(5, 0, hat, 23, 12, hatD));
        g.fill(h);
        g.setColor(gold ? Color.WHITE : new Color(0xF4C152));
        g.fill(new Ellipse2D.Float(23, 1, 5, 5));
        g.setColor(new Color(0xE3263B));
        g.fill(new RoundRectangle2D.Float(4.5f, 10, 19, 3.5f, 3, 3));
    }

    private static void paintBalloon(Graphics2D g, boolean popped) {
        if (!popped) {
            g.setPaint(new RadialGradientPaint(new Point2D.Float(9, 7), 14, new float[]{0f, 1f}, new Color[]{new Color(0xFF7A88), new Color(0xC51A2E)}));
            g.fill(new Ellipse2D.Float(2, 0, 20, 24));
            g.setColor(new Color(255, 255, 255, 150));
            g.fill(new Ellipse2D.Float(6, 4, 5, 7));
        }
        g.setColor(new Color(0x445066));
        g.setStroke(new BasicStroke(0.7f));
        g.drawLine(12, 24, 12, 30);
        // gift hanging below
        g.setColor(new Color(0x1FA35C));
        g.fill(new RoundRectangle2D.Float(6, 29, 12, 10, 3, 3));
        g.setColor(new Color(0xF4C152));
        g.fillRect(11, 29, 2, 10);
        g.fillRect(6, 32, 12, 2);
    }

    private static void paintSplat(Graphics2D g) {
        Random r = new Random(5);
        g.setColor(new Color(255, 255, 255, 235));
        g.fill(new Ellipse2D.Float(14, 14, 36, 34));
        for (int i = 0; i < 9; i++) {
            double a = i * Math.PI * 2 / 9 + r.nextDouble() * 0.4;
            float d = 18 + r.nextFloat() * 10, s = 7 + r.nextFloat() * 7;
            g.fill(new Ellipse2D.Float(32 + (float) Math.cos(a) * d - s / 2, 32 + (float) Math.sin(a) * d - s / 2, s, s));
        }
        g.setColor(new Color(200, 220, 245, 200));
        g.fill(new Ellipse2D.Float(24, 34, 18, 8));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Float(22, 20, 10, 8));
    }
}
