package dev.lego.games;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;

/** Asteroiden: neon vector space shooter - rotate, thrust, shoot splitting asteroids and saucers. */
public final class Asteroids extends GameView {
    private static final float W = 400, H = 290;
    private static final float[] RAD = {0, 7.5f, 14, 25};
    private static final int[] PTS = {0, 100, 50, 20};
    private static final int ANGLES = 48, ROCK_ANGLES = 24, VARIANTS = 6;
    private static final int NEON = 0xFF5CF2FF, ROCK = 0xFFC9A6FF, UFO_C = 0xFFFF5CA8, SHOT = 0xFFFFF27A;

    private static final class Rock {
        float x, y, vx, vy, ang, spin;
        int size, variant;
        float flash;
    }

    private static final class Shot {
        float x, y, vx, vy, life;
        boolean enemy;
    }

    private static final class Ufo {
        float x, y, vx, t, fireT;
        boolean small;
        int hp;
    }

    private final List<Rock> rocks = new ArrayList<>();
    private final List<Shot> shots = new ArrayList<>();
    private final GameFx fx = new GameFx();
    private Ufo ufo;
    private float x, y, vx, vy, ang, fireT, invul, deadT, waveT, ufoT, anim, thrustT, bannerT = -1;
    private boolean left, right, thrust, firing, dead;
    private int lives, wave;
    private long nextLife;

    public Asteroids() { super("asteroids"); }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }

    @Override
    protected void reset() {
        rocks.clear();
        shots.clear();
        fx.clear();
        ufo = null;
        lives = 3; wave = 0; nextLife = 10000;
        left = right = thrust = firing = false;
        spawnShip();
        nextWave();
    }

    private void spawnShip() {
        x = W / 2; y = H / 2; vx = vy = 0; ang = (float) -Math.PI / 2;
        invul = 2.2f; dead = false; deadT = 0;
    }

    private void nextWave() {
        wave++;
        int n = Math.min(10, 3 + wave);
        for (int i = 0; i < n; i++) {
            float rx, ry;
            do { rx = rnd.nextFloat() * W; ry = rnd.nextFloat() * H; } while (dist(rx, ry, x, y) < 90);
            addRock(rx, ry, 3, null);
        }
        ufoT = Math.max(8, 22 - wave * 2) + rnd.nextFloat() * 6;
        bannerT = 0;
    }

    private void addRock(float rx, float ry, int size, Rock parent) {
        Rock r = new Rock();
        r.x = rx; r.y = ry; r.size = size; r.variant = rnd.nextInt(VARIANTS);
        double a = rnd.nextDouble() * Math.PI * 2;
        float sp = (size == 3 ? 22 : size == 2 ? 42 : 66) * (1 + wave * 0.06f) * (0.7f + rnd.nextFloat() * 0.6f);
        r.vx = (float) Math.cos(a) * sp + (parent != null ? parent.vx * 0.4f : 0);
        r.vy = (float) Math.sin(a) * sp + (parent != null ? parent.vy * 0.4f : 0);
        r.ang = rnd.nextFloat() * 6.28f;
        r.spin = (rnd.nextFloat() - 0.5f) * 2.4f;
        rocks.add(r);
    }

    private static float dist(float ax, float ay, float bx, float by) {
        float dx = Math.abs(ax - bx), dy = Math.abs(ay - by);
        dx = Math.min(dx, W - dx); dy = Math.min(dy, H - dy);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    // ------------------------------------------------------------------ input
    @Override
    protected void onKey(int k) {
        if (k == K_LEFT || k == K_A) left = true;
        if (k == K_RIGHT || k == K_D) right = true;
        if (k == K_UP || k == K_W) thrust = true;
        if (k == K_SPACE) { if (!firing) fireT = 0; firing = true; }
        if ((k == K_DOWN || k == K_S || k == 340) && !dead) hyperspace();
    }

    @Override
    protected void onKeyUp(int k) {
        if (k == K_LEFT || k == K_A) left = false;
        if (k == K_RIGHT || k == K_D) right = false;
        if (k == K_UP || k == K_W) thrust = false;
        if (k == K_SPACE) firing = false;
    }

    private void hyperspace() {
        fx.burst(x, y, 20, NEON, 120, 2, 0.5f, 0, GameFx.SPARK);
        x = 20 + rnd.nextFloat() * (W - 40);
        y = 20 + rnd.nextFloat() * (H - 40);
        vx = vy = 0;
        invul = Math.max(invul, 0.4f);
        fx.ring(x, y, 30, NEON, 0.4f);
        Sound.play("minecraft:entity.enderman.teleport", 1.4f, 0.35f);
    }

    // ------------------------------------------------------------------ logic
    @Override
    protected void update(float dt) {
        fx.update(dt);
        if (bannerT >= 0) { bannerT += dt; if (bannerT > 1.8f) bannerT = -1; }
        invul = Math.max(0, invul - dt);
        fireT = Math.max(0, fireT - dt);

        if (dead) {
            deadT += dt;
            if (deadT > 1.6f) {
                if (lives <= 0) { gameOver(); return; }
                boolean clear = true;
                for (Rock r : rocks) if (dist(r.x, r.y, W / 2, H / 2) < RAD[r.size] + 50) clear = false;
                if (clear || deadT > 4) spawnShip();
            }
        } else {
            if (left) ang -= 4.4f * dt;
            if (right) ang += 4.4f * dt;
            float cs = (float) Math.cos(ang), sn = (float) Math.sin(ang);
            if (thrust) {
                vx += cs * 250 * dt; vy += sn * 250 * dt;
                thrustT += dt;
                if (thrustT > 0.02f) {
                    thrustT = 0;
                    fx.add(x - cs * 9, y - sn * 9, -cs * 90 + vx * 0.5f + (rnd.nextFloat() - 0.5f) * 40, -sn * 90 + vy * 0.5f + (rnd.nextFloat() - 0.5f) * 40,
                            0.35f, 1.8f + rnd.nextFloat(), rnd.nextBoolean() ? 0xFFFFB347 : 0xFFFF6A3D, GameFx.SPARK, 0, 1.5f);
                }
            }
            float sp = (float) Math.sqrt(vx * vx + vy * vy);
            if (sp > 240) { vx *= 240 / sp; vy *= 240 / sp; }
            float d = (float) Math.exp(-0.45f * dt);
            vx *= d; vy *= d;
            x = wrapX(x + vx * dt); y = wrapY(y + vy * dt);
            if (firing && fireT <= 0) fire();
        }
        // rocks
        for (Rock r : rocks) {
            r.x = wrapX(r.x + r.vx * dt); r.y = wrapY(r.y + r.vy * dt);
            r.ang += r.spin * dt;
            r.flash = Math.max(0, r.flash - dt * 6);
        }
        // shots
        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot s = shots.get(i);
            s.x = wrapX(s.x + s.vx * dt); s.y = wrapY(s.y + s.vy * dt);
            s.life -= dt;
            if (s.life <= 0) { shots.remove(i); continue; }
            boolean used = false;
            if (!s.enemy) {
                for (int k = rocks.size() - 1; k >= 0; k--) {
                    Rock r = rocks.get(k);
                    if (dist(s.x, s.y, r.x, r.y) < RAD[r.size] * 0.92f) { breakRock(k, s.vx, s.vy); used = true; break; }
                }
                if (!used && ufo != null && dist(s.x, s.y, ufo.x, ufo.y) < (ufo.small ? 8 : 13)) { killUfo(); used = true; }
            } else if (!dead && invul <= 0 && dist(s.x, s.y, x, y) < 7) { crash(); used = true; }
            if (used) shots.remove(i);
        }
        // ship vs rocks
        if (!dead && invul <= 0) {
            for (int k = rocks.size() - 1; k >= 0; k--) {
                Rock r = rocks.get(k);
                if (dist(x, y, r.x, r.y) < RAD[r.size] * 0.85f + 6) { breakRock(k, vx, vy); crash(); break; }
            }
            if (!dead && ufo != null && dist(x, y, ufo.x, ufo.y) < 16) { killUfo(); crash(); }
        }
        // ufo
        ufoT -= dt;
        if (ufo == null && ufoT <= 0 && wave >= 1) {
            ufo = new Ufo();
            ufo.small = wave >= 3 && rnd.nextFloat() < 0.45f;
            boolean fromLeft = rnd.nextBoolean();
            ufo.x = fromLeft ? -20 : W + 20;
            ufo.vx = (fromLeft ? 1 : -1) * (ufo.small ? 75 : 55);
            ufo.y = 30 + rnd.nextFloat() * (H - 60);
            ufo.fireT = 1.2f;
            ufoT = Math.max(9, 24 - wave * 2) + rnd.nextFloat() * 8;
            Sound.play("minecraft:block.beacon.activate", 1.8f, 0.3f);
        }
        if (ufo != null) {
            ufo.t += dt;
            ufo.x += ufo.vx * dt;
            ufo.y = wrapY(ufo.y + (float) Math.sin(ufo.t * 2.2f) * 40 * dt);
            ufo.fireT -= dt;
            if (ufo.fireT <= 0 && !dead) {
                ufo.fireT = ufo.small ? 0.9f : 1.3f;
                double a = ufo.small ? Math.atan2(y - ufo.y, x - ufo.x) + (rnd.nextFloat() - 0.5f) * 0.35f : rnd.nextDouble() * Math.PI * 2;
                Shot s = new Shot();
                s.enemy = true; s.x = ufo.x; s.y = ufo.y; s.vx = (float) Math.cos(a) * 170; s.vy = (float) Math.sin(a) * 170; s.life = 1.6f;
                shots.add(s);
                Sound.play("minecraft:block.note_block.bit", 0.7f, 0.25f);
            }
            if (ufo.x < -40 || ufo.x > W + 40) ufo = null;
        }
        if (rocks.isEmpty() && !dead) { waveT += dt; if (waveT > 1.2f) { waveT = 0; nextWave(); } }
        if (score >= nextLife) { nextLife += 10000; lives++; fx.popup("+1 Leben!", x, y - 16, 0xFF9FFFB5, 12); Sound.play("minecraft:entity.player.levelup", 1.5f, 0.4f); }
    }

    private void fire() {
        int mine = 0;
        for (Shot s : shots) if (!s.enemy) mine++;
        if (mine >= 6) return;
        fireT = 0.17f;
        float cs = (float) Math.cos(ang), sn = (float) Math.sin(ang);
        Shot s = new Shot();
        s.x = x + cs * 10; s.y = y + sn * 10; s.vx = cs * 380 + vx; s.vy = sn * 380 + vy; s.life = 0.85f;
        shots.add(s);
        fx.add(s.x, s.y, 0, 0, 0.12f, 4, SHOT, GameFx.SPARK, 0, 0);
        vx -= cs * 6; vy -= sn * 6;
        Sound.play("minecraft:block.note_block.hat", 2.0f, 0.25f);
    }

    private void breakRock(int k, float hvx, float hvy) {
        Rock r = rocks.remove(k);
        int pts = PTS[r.size];
        score += pts;
        fx.popup("+" + pts, r.x, r.y - RAD[r.size] * 0.5f, 0xFFFFFFFF, 8 + r.size);
        int n = 6 + r.size * 6;
        for (int i = 0; i < n; i++) {
            double a = rnd.nextDouble() * Math.PI * 2;
            float sp = 40 + rnd.nextFloat() * (60 + r.size * 30);
            fx.add(r.x, r.y, (float) Math.cos(a) * sp + hvx * 0.1f, (float) Math.sin(a) * sp + hvy * 0.1f, 0.5f + rnd.nextFloat() * 0.5f, 1.2f + rnd.nextFloat(), rnd.nextBoolean() ? ROCK : 0xFFFFFFFF, GameFx.STREAK, 0, 1.8f);
        }
        fx.ring(r.x, r.y, RAD[r.size] * 1.8f, ROCK, 0.4f);
        fx.shake(0.08f * r.size);
        Sound.play("minecraft:entity.generic.explode", 1.9f - r.size * 0.3f, 0.12f + r.size * 0.06f);
        if (r.size > 1) { addRock(r.x, r.y, r.size - 1, r); addRock(r.x, r.y, r.size - 1, r); }
    }

    private void killUfo() {
        int pts = ufo.small ? 1000 : 200;
        score += pts;
        fx.burst(ufo.x, ufo.y, 40, UFO_C, 190, 2.4f, 0.8f, 0, GameFx.STREAK);
        fx.ring(ufo.x, ufo.y, 40, UFO_C, 0.5f);
        fx.popup("UFO! +" + pts, ufo.x, ufo.y - 12, UFO_C, 12);
        fx.shake(0.5f);
        Sound.play("minecraft:entity.generic.explode", 1.2f, 0.4f);
        ufo = null;
    }

    private void crash() {
        dead = true;
        deadT = 0;
        lives--;
        fx.burst(x, y, 50, NEON, 200, 2.2f, 1.1f, 0, GameFx.STREAK);
        fx.burst(x, y, 24, 0xFFFFB347, 120, 2.8f, 0.8f, 0, GameFx.SPARK);
        fx.ring(x, y, 50, NEON, 0.6f);
        fx.shake(0.9f);
        fx.flash(0xFFFFFFFF, 0.5f);
        Sound.play("minecraft:entity.generic.explode", 0.8f, 0.5f);
    }

    private static float wrapX(float v) { return v < 0 ? v + W : v >= W ? v - W : v; }
    private static float wrapY(float v) { return v < 0 ? v + H : v >= H ? v - H : v; }

    // ------------------------------------------------------------------ drawing
    @Override
    protected void render() {
        float dt = Math.min(0.05f, Gx.dt);
        anim += dt;
        GameFx.sprite(this, "as_bg", 0, 0, W, H, 400, 290, Asteroids::paintSpace, 0xFFFFFFFF);
        // twinkling foreground stars
        for (int i = 0; i < 24; i++) {
            float sx = (i * 173.3f) % W, sy = (i * 91.7f + 13) % H;
            float tw = 0.5f + 0.5f * (float) Math.sin(anim * (1.5f + i % 4) + i);
            circle(sx, sy, 0.7f + (i % 3) * 0.3f, Gx.withAlpha(0xFFFFFFFF, 0.3f + 0.6f * tw));
        }
        Gx.push();
        Gx.translate(S(fx.sx), S(fx.sy));
        for (Rock r : rocks) rock(r);
        if (ufo != null) {
            final boolean sm = ufo.small;
            float s = sm ? 20 : 30;
            Gx.glow(X(ufo.x), Y(ufo.y), S(s), 0x33FF5CA8);
            GameFx.sprite(this, "as_ufo" + (((int) (ufo.t * 8)) % 3), ufo.x - s / 2 - 2, ufo.y - s * 0.35f - 2, s + 4, s * 0.7f + 4, 34, 24, gr -> paintUfo(gr, ((int) (ufo.t * 8)) % 3), 0xFFFFFFFF);
        }
        for (Shot s : shots) {
            int c = s.enemy ? UFO_C : SHOT;
            Gx.glow(X(s.x), Y(s.y), S(6), Gx.withAlpha(c, 0.5f));
            circle(s.x, s.y, 1.5f, 0xFFFFFFFF);
            circle(s.x - s.vx * 0.012f, s.y - s.vy * 0.012f, 1.1f, Gx.withAlpha(c, 0.6f));
        }
        fx.draw(this, 0, 0);
        if (!dead && (invul <= 0 || ((int) (anim * 10)) % 2 == 0 || state != State.PLAYING)) ship(x, y);
        Gx.pop();

        // HUD
        for (int i = 0; i < Math.min(lives, 6); i++) {
            int a = Math.round((float) ((-Math.PI / 2) / (Math.PI * 2) * ANGLES + ANGLES)) % ANGLES;
            GameFx.sprite(this, "as_ship" + a, 8 + i * 13, 8, 14, 14, 30, 30, gr -> paintShip(gr, a), 0xCCFFFFFF);
        }
        GameFx.outlined(this, "Welle " + wave, W - 34, 15, 8, NEON, 0xAA000000);
        if (bannerT >= 0) {
            float t = bannerT / 1.8f, s = Ease.outBack(Math.min(1, bannerT / 0.35f)), a = t < 0.7f ? 1 : 1 - (t - 0.7f) / 0.3f;
            Gx.pushAlpha(a);
            Gx.push();
            Gx.scaleAt(X(W / 2), Y(H * 0.3f), s);
            Gx.glow(X(W / 2), Y(H * 0.3f), S(70), 0x445CF2FF);
            GameFx.outlined(this, "WELLE " + wave, W / 2, H * 0.3f, 20, NEON, 0xCC001018);
            Gx.pop();
            Gx.popAlpha();
        }
        if (dead && lives > 0 && deadT > 0.8f) text("Mach dich bereit…", W / 2, H / 2 + 30, 8, Fonts.BOLD, Gx.withAlpha(0xFFFFFFFF, 0.6f + 0.4f * (float) Math.sin(anim * 6)));
        fx.drawPopups(this, 0, 0);
        fx.drawFlash(this, W, H);
    }

    private void rock(Rock r) {
        float rad = RAD[r.size];
        int a = Math.floorMod(Math.round(r.ang / 6.2831853f * ROCK_ANGLES), ROCK_ANGLES);
        final int sz = r.size, var = r.variant;
        String key = "as_rock" + sz + "_" + var + "_" + a;
        float box = rad * 2 + 6;
        for (int ox = -1; ox <= 1; ox++)
            for (int oy = -1; oy <= 1; oy++) {
                float cx = r.x + ox * W, cy = r.y + oy * H;
                if (cx + box < 0 || cx - box > W || cy + box < 0 || cy - box > H) continue;
                GameFx.sprite(this, key, cx - box / 2, cy - box / 2, box, box, 64, 64, gr -> paintRock(gr, sz, var, a), 0xFFFFFFFF);
            }
    }

    private void ship(float sx, float sy) {
        int a = Math.floorMod(Math.round(ang / 6.2831853f * ANGLES), ANGLES);
        if (thrust && state == State.PLAYING) Gx.glow(X(sx - (float) Math.cos(ang) * 8), Y(sy - (float) Math.sin(ang) * 8), S(10 + rnd.nextFloat() * 3), 0x66FF8A3D);
        Gx.glow(X(sx), Y(sy), S(18), 0x225CF2FF);
        for (int ox = -1; ox <= 1; ox++)
            for (int oy = -1; oy <= 1; oy++) {
                float cx = sx + ox * W, cy = sy + oy * H;
                if (cx + 14 < 0 || cx - 14 > W || cy + 14 < 0 || cy - 14 > H) continue;
                GameFx.sprite(this, "as_ship" + a, cx - 13, cy - 13, 26, 26, 30, 30, gr -> paintShip(gr, a), 0xFFFFFFFF);
            }
    }

    // ------------------------------------------------------------------ painters (neon vector look)
    private static void neon(Graphics2D g, java.awt.Shape s, int argb, float w) {
        Color c = new Color(argb);
        g.setStroke(new BasicStroke(w * 3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 40));
        g.draw(s);
        g.setStroke(new BasicStroke(w * 1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 110));
        g.draw(s);
        g.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(Gx.mix(argb, 0xFFFFFFFF, 0.45f)));
        g.draw(s);
    }

    private static void paintShip(Graphics2D g, int a) {
        g.translate(15, 15);
        g.rotate(a * Math.PI * 2 / ANGLES);
        GeneralPath p = new GeneralPath();
        p.moveTo(11, 0); p.lineTo(-8, -7.5); p.lineTo(-5, 0); p.lineTo(-8, 7.5); p.closePath();
        g.setColor(new Color(10, 40, 60, 200));
        g.fill(p);
        neon(g, p, NEON, 1.4f);
        g.setColor(new Color(0xFFFFFF));
        g.fill(new Ellipse2D.Float(1, -1.6f, 3.2f, 3.2f));
    }

    private static void paintRock(Graphics2D g, int size, int variant, int a) {
        g.translate(32, 32);
        g.rotate(a * Math.PI * 2 / ROCK_ANGLES);
        float scale = 32f / (RAD[size] + 3);
        Random r = new Random(variant * 31 + size * 7);
        int n = 9 + r.nextInt(3);
        GeneralPath p = new GeneralPath();
        for (int i = 0; i < n; i++) {
            double an = i * Math.PI * 2 / n;
            float rr = RAD[size] * (0.72f + r.nextFloat() * 0.34f) * scale;
            float px = (float) Math.cos(an) * rr, py = (float) Math.sin(an) * rr;
            if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
        }
        p.closePath();
        g.setColor(new Color(40, 20, 70, 170));
        g.fill(p);
        // crater lines
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(201, 166, 255, 70));
        for (int i = 0; i < 2; i++) {
            float cx = (r.nextFloat() - 0.5f) * RAD[size] * scale, cy = (r.nextFloat() - 0.5f) * RAD[size] * scale, cr = (2 + r.nextFloat() * 3) * scale * 0.6f;
            g.draw(new Ellipse2D.Float(cx - cr, cy - cr, cr * 2, cr * 2));
        }
        neon(g, p, ROCK, 1.5f * scale * 0.55f + 0.4f);
    }

    private static void paintUfo(Graphics2D g, int frame) {
        g.translate(2, 2);
        GeneralPath body = new GeneralPath();
        body.moveTo(0, 12); body.lineTo(8, 7); body.lineTo(22, 7); body.lineTo(30, 12); body.lineTo(22, 17); body.lineTo(8, 17); body.closePath();
        GeneralPath dome = new GeneralPath();
        dome.moveTo(9, 7); dome.curveTo(10, 0, 20, 0, 21, 7);
        g.setColor(new Color(60, 10, 40, 190));
        g.fill(body);
        neon(g, body, UFO_C, 1.2f);
        neon(g, dome, UFO_C, 1.2f);
        g.setStroke(new BasicStroke(0.9f));
        g.setColor(new Color(0xFFD1E8));
        g.drawLine(2, 12, 28, 12);
        for (int i = 0; i < 3; i++) {
            g.setColor(i == frame ? new Color(0xFFF27A) : new Color(255, 120, 190, 120));
            g.fill(new Ellipse2D.Float(8.5f + i * 5.5f, 13, 2.6f, 2.6f));
        }
    }

    private static void paintSpace(Graphics2D g) {
        g.setColor(new Color(0x05060F));
        g.fillRect(0, 0, 400, 290);
        g.setPaint(new RadialGradientPaint(new Point2D.Float(90, 70), 150, new float[]{0f, 1f}, new Color[]{new Color(90, 40, 150, 70), new Color(90, 40, 150, 0)}));
        g.fillRect(0, 0, 400, 290);
        g.setPaint(new RadialGradientPaint(new Point2D.Float(330, 230), 160, new float[]{0f, 1f}, new Color[]{new Color(20, 110, 150, 60), new Color(20, 110, 150, 0)}));
        g.fillRect(0, 0, 400, 290);
        Random r = new Random(21);
        for (int i = 0; i < 260; i++) {
            float sx = r.nextFloat() * 400, sy = r.nextFloat() * 290, s = r.nextFloat() < 0.9f ? 0.5f + r.nextFloat() * 0.6f : 1.2f + r.nextFloat();
            g.setColor(new Color(255, 255, 255, 40 + r.nextInt(150)));
            g.fill(new Ellipse2D.Float(sx, sy, s, s));
        }
        // distant planet
        g.setPaint(new RadialGradientPaint(new Point2D.Float(342, 52), 26, new float[]{0f, 1f}, new Color[]{new Color(0x6A4BC2), new Color(0x1B1240)}));
        g.fill(new Ellipse2D.Float(322, 34, 40, 40));
        g.setColor(new Color(180, 150, 255, 90));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new Ellipse2D.Float(310, 49, 64, 10));
    }
}
