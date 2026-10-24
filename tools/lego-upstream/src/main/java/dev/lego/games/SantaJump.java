package dev.lego.games;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;

/** Weihnachtsmann-Sprung: endless climber - Santa bounces up on clouds and chimneys and collects gifts. */
public final class SantaJump extends GameView {
    private static final float W = 460, H = 320;
    private static final float GRAVITY = 980, JUMP = -440, SUPER = -800, PW = 46, PH = 13, SANTA = 30;
    private static final int CLOUD = 0, MOVING = 1, ICE = 2, CHIMNEY = 3;
    private static final int[] GIFT_BOX = {0xFFE3263B, 0xFF1FA35C, 0xFF3F7BFF, 0xFFB14DFF, 0xFFFFB02E};
    private static final int[] GIFT_RIB = {0xFFF4C152, 0xFFF4F8FF, 0xFFF4C152, 0xFFF4F8FF, 0xFFE3263B};

    private static final class Plat {
        float x, y, w = PW, vx, t, fall;
        int type;
        boolean broken, used;
    }

    private static final class Gift {
        float x, y, t;
        int color;
        boolean gold;
    }

    private static final class Storm {
        float x, y, vx, t;
        boolean dead;
        float deadT;
    }

    private final List<Plat> plats = new ArrayList<>();
    private final List<Gift> gifts = new ArrayList<>();
    private final List<Storm> storms = new ArrayList<>();
    private final GameFx fx = new GameFx();
    private final float[][] stars = new float[80][3];
    private float x, y, vx, vy, cam, genY, maxH, squash, face = 1, trail;
    private float comboT, anim, deadT, hintT, superT, lastMx = -9999;
    private int combo, bonus;
    private boolean left, right, mouseMode, dying;

    public SantaJump() {
        super("santajump");
        Random r = new Random(4);
        for (float[] s : stars) { s[0] = r.nextFloat() * W; s[1] = r.nextFloat() * H; s[2] = 0.3f + r.nextFloat() * 0.7f; }
    }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }

    @Override
    protected void reset() {
        plats.clear();
        gifts.clear();
        storms.clear();
        fx.clear();
        x = W / 2; y = -20; vx = 0; vy = JUMP;
        cam = y - H + 70;
        maxH = 0; bonus = 0; combo = 0; comboT = 0; squash = 0; deadT = 0; hintT = 0; superT = 0;
        left = right = dying = false;
        // ground: a row of snowy rooftops
        Plat g = new Plat(); g.x = 0; g.y = 0; g.w = W; g.type = CLOUD; plats.add(g);
        genY = 0;
        generate();
    }

    private float height() { return Math.max(0, -y) / 5f; }

    private void generate() {
        while (genY > cam - 80) {
            float h = -genY / 5f;
            float gap = Math.min(92, 34 + h * 0.018f + rnd.nextFloat() * (18 + Math.min(34, h * 0.01f)));
            genY -= gap;
            Plat p = new Plat();
            p.y = genY;
            p.x = 6 + rnd.nextFloat() * (W - PW - 12);
            float r = rnd.nextFloat();
            float movP = Math.min(0.35f, h * 0.0004f), chimP = 0.07f;
            if (h > 40 && r < chimP) { p.type = CHIMNEY; p.w = 44; }
            else if (h > 60 && r < chimP + movP) { p.type = MOVING; p.vx = (rnd.nextBoolean() ? 1 : -1) * (30 + Math.min(70, h * 0.02f) + rnd.nextFloat() * 20); }
            else p.type = CLOUD;
            plats.add(p);
            // gifts sit above some clouds
            if (p.type != CHIMNEY && rnd.nextFloat() < 0.22f) {
                Gift gf = new Gift();
                gf.x = p.x + p.w / 2; gf.y = p.y - 20 - rnd.nextFloat() * 16;
                gf.color = rnd.nextInt(GIFT_BOX.length);
                gf.gold = rnd.nextFloat() < 0.1f;
                gf.t = rnd.nextFloat() * 6;
                gifts.add(gf);
            }
            // decoy: fragile ice between two real steps
            if (h > 25 && rnd.nextFloat() < Math.min(0.4f, 0.1f + h * 0.0003f) && gap > 50) {
                Plat d = new Plat();
                d.type = ICE;
                d.y = genY + gap * 0.5f;
                d.x = 6 + rnd.nextFloat() * (W - PW - 12);
                plats.add(d);
            }
            // storm clouds
            if (h > 180 && rnd.nextFloat() < Math.min(0.09f, 0.02f + h * 0.00004f)) {
                Storm s = new Storm();
                s.y = genY - gap * 0.5f;
                s.x = 30 + rnd.nextFloat() * (W - 60);
                s.vx = (rnd.nextBoolean() ? 1 : -1) * (20 + rnd.nextFloat() * 30);
                s.t = rnd.nextFloat() * 5;
                storms.add(s);
            }
        }
    }

    // ------------------------------------------------------------------ input
    @Override
    protected void onKey(int k) {
        if (k == K_LEFT || k == K_A) { left = true; mouseMode = false; }
        if (k == K_RIGHT || k == K_D) { right = true; mouseMode = false; }
    }

    @Override
    protected void onKeyUp(int k) {
        if (k == K_LEFT || k == K_A) left = false;
        if (k == K_RIGHT || k == K_D) right = false;
    }

    // ------------------------------------------------------------------ logic
    @Override
    protected void update(float dt) {
        fx.update(dt);
        squash = Math.max(0, squash - dt * 5);
        comboT = Math.max(0, comboT - dt);
        if (comboT <= 0) combo = 0;
        superT = Math.max(0, superT - dt);
        hintT += dt;
        if (dying) {
            deadT += dt;
            vy = Math.min(900, vy + GRAVITY * dt);
            y += vy * dt;
            if (deadT > 0.9f) gameOver();
            return;
        }
        // horizontal control
        if (Math.abs(mx - lastMx) > 0.5f && mx >= 0) { if (lastMx > -9999) mouseMode = true; lastMx = mx; }
        float target;
        if (mouseMode && u > 0) {
            float mxB = (mx - bx) / u;
            float d = mxB - x;
            target = Math.max(-260, Math.min(260, d * 6));
        } else target = ((right ? 1 : 0) - (left ? 1 : 0)) * 230;
        vx += (target - vx) * Math.min(1, dt * 12);
        if (Math.abs(vx) > 20) face = Math.signum(vx);
        x += vx * dt;
        if (x < -SANTA / 2) x += W + SANTA;
        if (x > W + SANTA / 2) x -= W + SANTA;

        float prevY = y;
        vy = Math.min(700, vy + GRAVITY * dt);
        y += vy * dt;
        trail += dt;
        if (superT > 0 && trail > 0.03f) {
            trail = 0;
            fx.add(x + (rnd.nextFloat() - 0.5f) * 12, y - 6, 0, 20, 0.6f, 3 + rnd.nextFloat() * 3, 0xFFF4C152, GameFx.SPARK, 0, 1);
        }

        // platforms
        for (Plat p : plats) {
            p.t += dt;
            if (p.type == MOVING) {
                p.x += p.vx * dt;
                if (p.x < 4) { p.x = 4; p.vx = Math.abs(p.vx); }
                if (p.x + p.w > W - 4) { p.x = W - 4 - p.w; p.vx = -Math.abs(p.vx); }
            }
            if (p.broken) { p.fall += dt; continue; }
            if (vy > 0 && prevY <= p.y + 1 && y >= p.y && x + 9 > p.x && x - 9 < p.x + p.w) {
                if (p.type == ICE) {
                    p.broken = true;
                    fx.burst(p.x + p.w / 2, p.y + 4, 16, 0xFFBFE3FF, 110, 3.2f, 0.7f, 420, GameFx.SQUARE);
                    Sound.play("minecraft:block.glass.break", 1.4f + rnd.nextFloat() * 0.2f, 0.25f);
                    continue;
                }
                y = p.y;
                boolean sup = p.type == CHIMNEY && Math.abs(x - (p.x + p.w * 0.68f)) < 11;
                vy = sup ? SUPER : JUMP;
                squash = 1;
                fx.burst(x, p.y + 2, sup ? 18 : 7, sup ? 0xFFB7C0CE : 0xFFF4F8FF, sup ? 90 : 60, sup ? 5 : 3, 0.55f, sup ? -60 : 40, GameFx.FLAKE);
                if (sup) {
                    superT = 1.1f;
                    fx.shake(0.45f);
                    fx.ring(x, p.y, 34, 0xFFF4C152, 0.45f);
                    fx.popup("Schornstein-Turbo!", x, p.y - 26, 0xFFF4C152, 10);
                    Sound.play("minecraft:entity.firework_rocket.launch", 1.1f, 0.4f);
                } else Sound.play("minecraft:block.snow.step", 1.2f + rnd.nextFloat() * 0.3f, 0.4f);
                if (!p.used && p != plats.get(0)) p.used = true;
            }
        }
        // gifts
        for (int i = gifts.size() - 1; i >= 0; i--) {
            Gift g = gifts.get(i);
            g.t += dt;
            float gy = g.y + (float) Math.sin(g.t * 3) * 2.5f;
            if (Math.abs(g.x - x) < 16 && Math.abs(gy - (y - 14)) < 20) {
                gifts.remove(i);
                combo = comboT > 0 ? combo + 1 : 1;
                comboT = 2.2f;
                int pts = (g.gold ? 150 : 25) * Math.min(combo, 8);
                bonus += pts;
                fx.confetti(g.x, gy, 18, g.gold ? new int[]{0xFFF4C152, 0xFFFFE9A8, 0xFFFFFFFF} : new int[]{GIFT_BOX[g.color], GIFT_RIB[g.color], 0xFFFFFFFF}, 170, 4);
                fx.ring(g.x, gy, 20, g.gold ? 0xFFF4C152 : 0xFFFFFFFF, 0.35f);
                fx.popup("+" + pts + (combo > 1 ? "  x" + Math.min(combo, 8) : ""), g.x, gy - 10, g.gold ? 0xFFF4C152 : 0xFFFFFFFF, combo > 1 ? 11 : 9.5f);
                Sound.play("minecraft:entity.experience_orb.pickup", 1.0f + Math.min(0.9f, combo * 0.1f), 0.35f);
            }
        }
        // storm clouds
        for (Storm s : storms) {
            s.t += dt;
            if (s.dead) { s.deadT += dt; s.y += 260 * dt * s.deadT * 3; continue; }
            s.x += s.vx * dt;
            if (s.x < 22 || s.x > W - 22) s.vx = -s.vx;
            float dx = x - s.x, dy = (y - 14) - s.y;
            if (Math.abs(dx) < 22 && Math.abs(dy) < 17) {
                if (vy > 0 && y - 6 < s.y) {
                    s.dead = true;
                    vy = JUMP * 1.1f;
                    squash = 1;
                    bonus += 100;
                    fx.burst(s.x, s.y, 22, 0xFF6E7890, 140, 5, 0.6f, 120, GameFx.DOT);
                    fx.popup("+100 Gewitter weg!", s.x, s.y - 16, 0xFF9FD8FF, 10);
                    fx.shake(0.35f);
                    Sound.play("minecraft:entity.generic.explode", 1.6f, 0.25f);
                } else die(true);
            }
        }
        // camera
        float want = y - H * 0.46f;
        if (want < cam) cam += (want - cam) * Math.min(1, dt * 9);
        maxH = Math.max(maxH, height());
        score = (long) maxH + bonus;
        generate();
        plats.removeIf(p -> p.y - cam > H + 60 || p.fall > 1.5f);
        gifts.removeIf(g -> g.y - cam > H + 40);
        storms.removeIf(s -> s.y - cam > H + 60);
        if (y - cam > H + 20) die(false);
    }

    private void die(boolean zap) {
        if (dying) return;
        dying = true;
        deadT = 0;
        fx.shake(0.8f);
        fx.flash(zap ? 0xFFBFE3FF : 0xFFE3263B, 0.8f);
        if (zap) { vy = -200; Sound.play("minecraft:entity.lightning_bolt.thunder", 1.5f, 0.3f); }
        else Sound.play("minecraft:entity.player.hurt", 0.8f, 0.4f);
    }

    // ------------------------------------------------------------------ drawing
    @Override
    protected void render() {
        float dt = Math.min(0.05f, Gx.dt);
        anim += dt;
        if (state == State.READY) { y = -20 + (float) -Math.abs(Math.sin(anim * 3)) * 70; cam = -H + 70 - 20; squash = Math.max(0, 1 - (float) Math.abs(Math.sin(anim * 3)) * 6); }
        float alt = Math.max(0, -cam) / 5f;

        // sky: night blue -> violet -> deep space
        float k1 = Ease.clamp(alt / 600f), k2 = Ease.clamp((alt - 600) / 900f);
        int top = Gx.mix(Gx.mix(0xFF0E1C3F, 0xFF1B0F3A, k1), 0xFF04040C, k2);
        int bot = Gx.mix(Gx.mix(0xFF2D5C9A, 0xFF4B2A6E, k1), 0xFF120A2A, k2);
        boxV(0, 0, W, H, 0, top, bot);
        // stars (parallax)
        float starA = 0.35f + 0.65f * Ease.clamp(alt / 300f);
        for (float[] s : stars) {
            float sy = ((s[1] - cam * 0.08f * s[2]) % H + H) % H;
            float tw = 0.6f + 0.4f * (float) Math.sin(anim * 2 + s[0]);
            circle(s[0], sy, 0.6f + s[2] * 0.9f, Gx.withAlpha(0xFFFFFFFF, starA * tw * s[2]));
        }
        // moon
        float my = 60 - cam * 0.02f;
        Gx.glow(X(W - 62), Y(my), S(80), 0x33FFF4D6);
        circle(W - 62, my, 20, 0xFFFFF4D6);
        circle(W - 55, my - 6, 4, 0x18000000);
        circle(W - 70, my + 5, 3, 0x14000000);
        // aurora higher up
        if (alt > 200) {
            float a = Ease.clamp((alt - 200) / 400f) * (1 - k2 * 0.6f);
            Gx.pushAlpha(a);
            GameFx.sprite(this, "sj_aurora", 0, 20, W, 140, 460, 140, SantaJump::paintAurora, Gx.withAlpha(0xFFFFFFFF, 0.6f + 0.2f * (float) Math.sin(anim * 0.7f)));
            Gx.popAlpha();
        }
        // town silhouette near the ground (parallax)
        float townY = (0 - cam) * 0.6f + H * 0.4f - 30;
        if (townY < H + 10) {
            GameFx.sprite(this, "sj_town", 0, townY - 70, W, 110, 460, 110, SantaJump::paintTown, 0xFFFFFFFF);
            if (townY + 40 < H) box(0, townY + 40, W, H - townY - 40, 0, 0xFF0B1631);
        }

        Gx.push();
        Gx.translate(S(fx.sx), S(fx.sy));
        // platforms
        for (Plat p : plats) plat(p);
        // gifts
        for (Gift g : gifts) {
            float gy = g.y + (float) Math.sin(g.t * 3) * 2.5f - cam;
            if (gy < -30 || gy > H + 30) continue;
            Gx.glow(X(g.x), Y(gy), S(18), g.gold ? 0x55F4C152 : 0x26FFFFFF);
            int c = g.gold ? 5 : g.color;
            GameFx.sprite(this, "sj_gift" + c, g.x - 9, gy - 9, 18, 18, 40, 40, gr -> paintGift(gr, c), 0xFFFFFFFF);
        }
        // storms
        for (Storm s : storms) storm(s);
        fx.draw(this, 0, cam);
        santa();
        Gx.pop();

        // HUD
        fx.drawPopups(this, 0, cam);
        box(8, 8, 74, 22, 11, 0x66000000);
        Gx.icon("sprint", X(20), Y(19), 10 * u, 0xFFBFE3FF);
        text((int) maxH + " m", 50, 19, 9, Fonts.BOLD, 0xFFFFFFFF);
        if (combo > 1 && comboT > 0) {
            float a = Math.min(1, comboT * 2);
            box(W - 86, 8, 78, 22, 11, Gx.withAlpha(0xFFE3263B, 0.85f * a));
            text("Combo x" + Math.min(combo, 8), W - 47, 19, 8.5f, Fonts.BOLD, Gx.withAlpha(0xFFFFFFFF, a));
            box(W - 80, 26, 66 * comboT / 2.2f, 2, 1, Gx.withAlpha(0xFFFFFFFF, a * 0.8f));
        }
        if (state == State.PLAYING && hintT < 3.5f) {
            float a = Math.min(1, (3.5f - hintT)) * (0.6f + 0.4f * (float) Math.sin(anim * 5));
            box(W / 2 - 92, H - 44, 184, 22, 11, Gx.withAlpha(0xFF000000, 0.4f * a));
            text("← → / A D oder Maus zum Lenken", W / 2, H - 33, 7.5f, Fonts.BOLD, Gx.withAlpha(0xFFFFFFFF, a));
        }
        fx.drawFlash(this, W, H);
    }

    private void plat(Plat p) {
        float py = p.y - cam + (p.broken ? p.fall * p.fall * 300 : 0);
        if (py < -30 || py > H + 40) return;
        if (p == plats.get(0) && p.w >= W) {
            // snowy rooftop ground
            boxV(0, py, W, H, 0, 0xFFE8F1FF, 0xFFA9C4EA);
            for (float sx = 8; sx < W; sx += 34) circle(sx, py + 2, 10, 0xFFF4F8FF);
            return;
        }
        float a = p.broken ? Math.max(0, 1 - p.fall * 1.4f) : 1;
        Gx.pushAlpha(a);
        switch (p.type) {
            case ICE:
                GameFx.sprite(this, "sj_ice" + (p.broken ? 1 : 0), p.x, py - 3, p.w, PH + 2, 46, 15, gr -> paintIce(gr, p.broken), 0xFFFFFFFF);
                break;
            case CHIMNEY: {
                float pulse = 0.5f + 0.5f * (float) Math.sin(p.t * 5);
                GameFx.sprite(this, "sj_chim", p.x - 2, py - 26, p.w + 4, 44, 48, 44, SantaJump::paintChimney, 0xFFFFFFFF);
                Gx.glow(X(p.x + p.w * 0.68f), Y(py - 22), S(14 + pulse * 4), Gx.withAlpha(0xFFFFB02E, 0.25f + pulse * 0.2f));
                if (rnd.nextFloat() < 0.15f && state == State.PLAYING) fx.add(p.x + p.w * 0.68f, p.y - 26, (rnd.nextFloat() - 0.5f) * 8, -18, 1.2f, 3 + rnd.nextFloat() * 2, 0x66C8D0DC, GameFx.DOT, -4, 0.2f);
                break;
            }
            default: {
                float bob = p.type == MOVING ? (float) Math.sin(p.t * 4) * 1.2f : 0;
                GameFx.sprite(this, "sj_cloud" + p.type, p.x - 3, py - 5 + bob, p.w + 6, PH + 8, 52, 21, gr -> paintCloud(gr, p.type == MOVING), 0xFFFFFFFF);
            }
        }
        Gx.popAlpha();
    }

    private void storm(Storm s) {
        float sy = s.y - cam;
        if (sy < -40 || sy > H + 40) return;
        boolean zap = ((int) (s.t * 3)) % 4 == 0;
        float a = s.dead ? Math.max(0, 1 - s.deadT * 2) : 1;
        Gx.pushAlpha(a);
        if (zap && !s.dead) Gx.glow(X(s.x), Y(sy + 14), S(34), 0x44BFE3FF);
        GameFx.sprite(this, "sj_storm" + (zap ? 1 : 0) + (s.dead ? 1 : 0), s.x - 25, sy - 17, 50, 42, 50, 42, gr -> paintStorm(gr, zap && !s.dead, s.dead), 0xFFFFFFFF);
        Gx.popAlpha();
    }

    private void santa() {
        float sy = y - cam;
        float sq = Ease.outCubic(squash);
        float stretch = vy < -200 ? Math.min(0.12f, (-vy - 200) / 3000f) : 0;
        float w = SANTA * (1 + 0.22f * sq - stretch), h = SANTA * (1 - 0.2f * sq + stretch);
        if (superT > 0) Gx.glow(X(x), Y(sy - h / 2), S(34), Gx.withAlpha(0xFFF4C152, superT * 0.4f));
        boolean r = face > 0;
        if (dying) { Gx.pushAlpha(Math.max(0, 1 - deadT)); }
        GameFx.spriteAt(this, "sj_santa" + (r ? 1 : 0) + (dying ? 1 : 0), SANTA, SANTA, 100, 100, gr -> paintSanta(gr, r, dying), x - w / 2, sy - h, w, h, 0xFFFFFFFF);
        // wrap-around ghost
        if (x < SANTA / 2) GameFx.spriteAt(this, "sj_santa" + (r ? 1 : 0) + (dying ? 1 : 0), SANTA, SANTA, 100, 100, gr -> paintSanta(gr, r, dying), x + W - w / 2, sy - h, w, h, 0xFFFFFFFF);
        if (x > W - SANTA / 2) GameFx.spriteAt(this, "sj_santa" + (r ? 1 : 0) + (dying ? 1 : 0), SANTA, SANTA, 100, 100, gr -> paintSanta(gr, r, dying), x - W - w / 2, sy - h, w, h, 0xFFFFFFFF);
        if (dying) Gx.popAlpha();
    }

    // ------------------------------------------------------------------ painters
    private static void paintSanta(Graphics2D g, boolean right, boolean hurt) {
        if (!right) { g.translate(100, 0); g.scale(-1, 1); }
        Color red = new Color(0xE3263B), redD = new Color(0x9E1024), fur = new Color(0xF4F8FF), furD = new Color(0xC9D6EA);
        g.setStroke(new BasicStroke(2.2f));
        // boots
        g.setColor(new Color(0x1B1B22));
        g.fill(new RoundRectangle2D.Float(28, 86, 18, 13, 7, 7));
        g.fill(new RoundRectangle2D.Float(54, 86, 20, 13, 7, 7));
        // sack on the back
        g.setPaint(new GradientPaint(0, 40, new Color(0xA0703C), 0, 80, new Color(0x6E4622)));
        g.fill(new Ellipse2D.Float(6, 44, 30, 36));
        g.setColor(new Color(0x5A3A1A));
        g.fill(new RoundRectangle2D.Float(14, 40, 14, 7, 4, 4));
        // body
        g.setPaint(new GradientPaint(0, 44, red, 0, 92, redD));
        g.fill(new RoundRectangle2D.Float(22, 44, 58, 48, 30, 30));
        g.setPaint(new GradientPaint(0, 80, fur, 0, 92, furD));
        g.fill(new RoundRectangle2D.Float(23, 80, 56, 10, 10, 10));
        g.setColor(new Color(0x16161C));
        g.fill(new Rectangle2D.Float(22, 66, 58, 8));
        g.setColor(new Color(0xF4C152));
        g.setStroke(new BasicStroke(2.6f));
        g.draw(new Rectangle2D.Float(54, 64.5f, 11, 11));
        g.setColor(fur);
        g.fill(new Rectangle2D.Float(49, 44, 6, 22));
        // arm (front) raised a bit
        g.setPaint(new GradientPaint(70, 44, red, 90, 60, redD));
        g.fill(new RoundRectangle2D.Float(70, 46, 16, 26, 14, 14));
        g.setColor(fur);
        g.fill(new RoundRectangle2D.Float(69, 66, 18, 7, 6, 6));
        g.setColor(new Color(0x1FA35C));
        g.fill(new Ellipse2D.Float(70, 70, 15, 13));
        // head
        g.setPaint(new RadialGradientPaint(new Point2D.Float(55, 28), 22, new float[]{0f, 1f}, new Color[]{new Color(0xFFD9BC), new Color(0xE9AE8C)}));
        g.fill(new Ellipse2D.Float(36, 14, 36, 34));
        // beard
        GeneralPath b = new GeneralPath();
        b.moveTo(34, 32); b.curveTo(34, 62, 50, 70, 56, 70); b.curveTo(66, 70, 78, 60, 76, 32);
        b.curveTo(70, 40, 62, 42, 56, 40); b.curveTo(48, 42, 40, 40, 34, 32); b.closePath();
        g.setPaint(new GradientPaint(0, 32, fur, 0, 70, furD));
        g.fill(b);
        // moustache + nose
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Float(47, 34, 12, 8));
        g.fill(new Ellipse2D.Float(58, 34, 12, 8));
        g.setColor(new Color(0xF08A7A));
        g.fill(new Ellipse2D.Float(55, 29, 8, 8));
        // eyes
        g.setColor(new Color(0x1B1B22));
        if (hurt) {
            g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(47, 23, 53, 29); g.drawLine(53, 23, 47, 29);
            g.drawLine(63, 23, 69, 29); g.drawLine(69, 23, 63, 29);
        } else {
            g.fill(new Ellipse2D.Float(48, 23, 6, 7));
            g.fill(new Ellipse2D.Float(64, 23, 6, 7));
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Float(50, 24, 2, 2));
            g.fill(new Ellipse2D.Float(66, 24, 2, 2));
        }
        g.setColor(new Color(255, 120, 120, 90));
        g.fill(new Ellipse2D.Float(41, 30, 8, 5));
        // hat: droops backwards
        GeneralPath h = new GeneralPath();
        h.moveTo(36, 20); h.curveTo(38, 2, 58, -2, 70, 8); h.curveTo(76, 12, 74, 18, 72, 20); h.closePath();
        g.setPaint(new GradientPaint(40, 0, red, 70, 20, redD));
        g.fill(h);
        GeneralPath tip = new GeneralPath();
        tip.moveTo(40, 12); tip.curveTo(30, 4, 18, 6, 12, 16); tip.lineTo(20, 20); tip.curveTo(26, 14, 32, 14, 40, 18); tip.closePath();
        g.fill(tip);
        g.setColor(fur);
        g.fill(new Ellipse2D.Float(4, 12, 14, 14));
        g.setPaint(new GradientPaint(0, 14, fur, 0, 24, furD));
        g.fill(new RoundRectangle2D.Float(32, 15, 44, 10, 10, 10));
    }

    private static void paintCloud(Graphics2D g, boolean moving) {
        Color top = moving ? new Color(0xFFF1D8) : new Color(0xFFFFFF), bot = moving ? new Color(0xE3B98F) : new Color(0xB8CCEA);
        java.awt.Shape p = GameFx.union(new Ellipse2D.Float(2, 6, 18, 14), new Ellipse2D.Float(12, 1, 18, 16), new Ellipse2D.Float(24, 2, 16, 15),
                new Ellipse2D.Float(33, 6, 17, 14), new RoundRectangle2D.Float(5, 9, 42, 11, 10, 10));
        g.setColor(new Color(20, 30, 70, 60));
        g.translate(0, 1.2);
        g.fill(p);
        g.translate(0, -1.2);
        g.setPaint(new GradientPaint(0, 2, top, 0, 20, bot));
        g.fill(p);
        g.setColor(new Color(255, 255, 255, 200));
        g.fill(new Ellipse2D.Float(15, 3.5f, 9, 4));
        g.fill(new Ellipse2D.Float(27, 4.5f, 7, 3));
        if (moving) {
            g.setColor(new Color(0xE3263B));
            g.fill(new Ellipse2D.Float(8, 15, 3, 3));
            g.setColor(new Color(0x1FA35C));
            g.fill(new Ellipse2D.Float(25, 16, 3, 3));
            g.setColor(new Color(0xF4C152));
            g.fill(new Ellipse2D.Float(40, 15, 3, 3));
        }
    }

    private static void paintIce(Graphics2D g, boolean broken) {
        RoundRectangle2D r = new RoundRectangle2D.Float(1, 3, 44, 10, 5, 5);
        g.setPaint(new GradientPaint(0, 3, new Color(220, 242, 255, 230), 0, 13, new Color(120, 180, 235, 220)));
        g.fill(r);
        g.setColor(new Color(255, 255, 255, 200));
        g.fill(new RoundRectangle2D.Float(4, 4, 36, 2.2f, 2, 2));
        g.setColor(new Color(70, 130, 200, 200));
        g.setStroke(new BasicStroke(0.9f));
        g.drawLine(12, 4, 16, 9); g.drawLine(16, 9, 14, 13); g.drawLine(28, 3, 25, 8); g.drawLine(25, 8, 30, 12); g.drawLine(38, 5, 36, 10);
        g.draw(r);
        // icicles
        g.setColor(new Color(200, 232, 255, 220));
        for (int i = 0; i < 5; i++) { int x = 6 + i * 8; g.fillPolygon(new int[]{x, x + 3, x + 1}, new int[]{13, 13, 15 + (i % 2) * 1}, 3); }
    }

    private static void paintChimney(Graphics2D g) {
        // roof slab
        g.setPaint(new GradientPaint(0, 26, new Color(0x8A3B2E), 0, 42, new Color(0x5A2219)));
        g.fill(new RoundRectangle2D.Float(2, 28, 44, 14, 4, 4));
        g.setColor(new Color(0x3E160F));
        for (int i = 0; i < 6; i++) g.fillRect(4 + i * 7, 35, 1, 6);
        // snow on the roof
        g.setPaint(new GradientPaint(0, 24, Color.WHITE, 0, 32, new Color(0xC9DAF2)));
        g.fill(new RoundRectangle2D.Float(0, 24, 48, 7, 7, 7));
        // chimney
        g.setPaint(new GradientPaint(26, 0, new Color(0xB24A38), 40, 0, new Color(0x7A2A1E)));
        g.fill(new Rectangle2D.Float(26, 6, 14, 20));
        g.setColor(new Color(0x5A1E14));
        g.setStroke(new BasicStroke(0.8f));
        for (int r = 0; r < 4; r++) { g.drawLine(26, 10 + r * 5, 40, 10 + r * 5); int off = r % 2 == 0 ? 33 : 30; g.drawLine(off, 10 + r * 5, off, 15 + r * 5); }
        g.setColor(new Color(0x3A120C));
        g.fill(new Rectangle2D.Float(24, 3, 18, 5));
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Float(23, 1, 20, 4, 4, 4));
        // arrow hint
        g.setColor(new Color(0xFFD27A));
        g.fillPolygon(new int[]{33, 29, 37}, new int[]{-1 + 0, 3, 3}, 3);
    }

    private static void paintStorm(Graphics2D g, boolean zap, boolean dead) {
        java.awt.Shape p = GameFx.union(new Ellipse2D.Float(3, 12, 20, 16), new Ellipse2D.Float(13, 4, 22, 20), new Ellipse2D.Float(27, 10, 20, 17),
                new RoundRectangle2D.Float(6, 16, 38, 12, 11, 11));
        g.setPaint(new GradientPaint(0, 4, dead ? new Color(0x8C94A6) : new Color(0x5B6378), 0, 28, dead ? new Color(0x5A6070) : new Color(0x2A2F3E)));
        g.fill(p);
        g.setColor(new Color(255, 255, 255, 40));
        g.fill(new Ellipse2D.Float(17, 7, 10, 4));
        // angry eyes
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Float(16, 14, 7, 7));
        g.fill(new Ellipse2D.Float(28, 14, 7, 7));
        g.setColor(new Color(0x11131A));
        g.fill(new Ellipse2D.Float(18.5f, 16.5f, 3.4f, 3.4f));
        g.fill(new Ellipse2D.Float(29.5f, 16.5f, 3.4f, 3.4f));
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(15, 12, 23, 15); g.drawLine(36, 12, 28, 15);
        g.drawArc(21, 22, 9, 5, 0, 180);
        if (zap) {
            GeneralPath l = new GeneralPath();
            l.moveTo(24, 27); l.lineTo(19, 34); l.lineTo(24, 34); l.lineTo(20, 42); l.lineTo(30, 32); l.lineTo(25, 32); l.lineTo(29, 27); l.closePath();
            g.setColor(new Color(0xFFE66B));
            g.fill(l);
        }
    }

    static void paintGift(Graphics2D g, int c) {
        int box = c == 5 ? 0xFFF4C152 : GIFT_BOX[c], rib = c == 5 ? 0xFFFFFFFF : GIFT_RIB[c];
        g.setPaint(new GradientPaint(0, 12, new Color(Gx.mix(box, 0xFFFFFFFF, 0.2f)), 0, 38, new Color(Gx.mix(box, 0xFF000000, 0.3f))));
        g.fill(new RoundRectangle2D.Float(5, 14, 30, 24, 5, 5));
        g.setPaint(new GradientPaint(0, 9, new Color(Gx.mix(box, 0xFFFFFFFF, 0.3f)), 0, 17, new Color(box)));
        g.fill(new RoundRectangle2D.Float(3, 10, 34, 8, 4, 4));
        g.setColor(new Color(rib));
        g.fill(new Rectangle2D.Float(17, 10, 6, 28));
        g.setStroke(new BasicStroke(3.2f));
        g.drawOval(9, 2, 10, 9);
        g.drawOval(21, 2, 10, 9);
        g.setColor(new Color(255, 255, 255, 90));
        g.fill(new Rectangle2D.Float(7, 19, 3, 16));
    }

    private static void paintTown(Graphics2D g) {
        Random r = new Random(9);
        // back hills
        g.setColor(new Color(0x132447));
        g.fill(new Ellipse2D.Float(-60, 40, 220, 120));
        g.fill(new Ellipse2D.Float(130, 30, 240, 140));
        g.fill(new Ellipse2D.Float(250, 44, 260, 120));
        // snowy fir trees
        for (int i = 0; i < 14; i++) {
            float tx = 8 + i * 33 + r.nextFloat() * 10, ty = 56 + r.nextFloat() * 10, s = 0.7f + r.nextFloat() * 0.5f;
            g.setColor(new Color(0x0E3B34));
            g.fillPolygon(new int[]{(int) tx, (int) (tx - 10 * s), (int) (tx + 10 * s)}, new int[]{(int) (ty - 26 * s), (int) (ty + 6), (int) (ty + 6)}, 3);
            g.setColor(new Color(220, 235, 255, 180));
            g.fillPolygon(new int[]{(int) tx, (int) (tx - 4 * s), (int) (tx + 4 * s)}, new int[]{(int) (ty - 26 * s), (int) (ty - 16 * s), (int) (ty - 16 * s)}, 3);
        }
        // houses
        float[] hx = {10, 70, 150, 215, 262, 312, 366, 414};
        for (int i = 0; i < hx.length; i++) {
            float w = 38 + r.nextFloat() * 14, h = 26 + r.nextFloat() * 18, x = hx[i], y = 110 - h;
            g.setColor(new Color(0x1A2B52));
            g.fill(new Rectangle2D.Float(x, y, w, h));
            GeneralPath roof = new GeneralPath();
            roof.moveTo(x - 4, y); roof.lineTo(x + w / 2, y - 18); roof.lineTo(x + w + 4, y); roof.closePath();
            g.setColor(new Color(0x243766));
            g.fill(roof);
            g.setColor(new Color(0xEAF2FF));
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine((int) (x - 3), (int) y, (int) (x + w / 2), (int) (y - 17));
            g.drawLine((int) (x + w / 2), (int) (y - 17), (int) (x + w + 3), (int) y);
            for (int wi = 0; wi < 2; wi++) {
                float wx = x + 7 + wi * (w - 21), wy = y + 7;
                g.setPaint(new RadialGradientPaint(new Point2D.Float(wx + 3.5f, wy + 4), 12, new float[]{0f, 1f}, new Color[]{new Color(255, 200, 90, 120), new Color(255, 200, 90, 0)}));
                g.fill(new Ellipse2D.Float(wx - 8, wy - 7, 23, 23));
                g.setColor(new Color(0xFFD27A));
                g.fill(new Rectangle2D.Float(wx, wy, 7, 8));
            }
        }
        g.setPaint(new GradientPaint(0, 104, new Color(0xDDE9FB), 0, 110, new Color(0xA9C4EA)));
        g.fill(new Rectangle2D.Float(0, 106, 460, 4));
    }

    private static void paintAurora(Graphics2D g) {
        int[][] cols = {{60, 255, 170}, {90, 200, 255}, {190, 120, 255}};
        for (int band = 0; band < 3; band++) {
            int[] c = cols[band];
            for (int x = 0; x < 460; x += 2) {
                double y = 50 + band * 24 + Math.sin(x / 45.0 + band * 1.7) * 18 + Math.sin(x / 17.0 + band) * 4;
                int a = (int) (80 * Math.sin(Math.PI * x / 460.0));
                g.setPaint(new GradientPaint(x, (float) (y - 40), new Color(c[0], c[1], c[2], 0), x, (float) y, new Color(c[0], c[1], c[2], a)));
                g.fillRect(x, (int) (y - 40), 2, 40);
                g.setPaint(new GradientPaint(x, (float) y, new Color(c[0], c[1], c[2], a), x, (float) (y + 10), new Color(c[0], c[1], c[2], 0)));
                g.fillRect(x, (int) y, 2, 10);
            }
        }
    }
}
