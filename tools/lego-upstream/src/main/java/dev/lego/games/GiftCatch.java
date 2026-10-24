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

/** Geschenke-Fänger: catch the falling presents with the sleigh, avoid coal. Speed ramps up. */
public final class GiftCatch extends GameView {
    private static final float W = 400, H = 280, GROUND = 258, SLEIGH_Y = 236, SW = 66;
    private static final int GIFT = 0, GOLD = 1, COAL = 2, STAR = 3, CLOCK = 4, HEART = 5;
    private static final int[] BULBS = {0xFFFF3B4E, 0xFFFFC94A, 0xFF3BE08A, 0xFF4AA8FF, 0xFFFF7BD5};

    private static final class Item {
        float x, y, vy, size, t, sway;
        int type, color;
    }

    private final List<Item> items = new ArrayList<>();
    private final List<Integer> pile = new ArrayList<>();
    private final GameFx fx = new GameFx();
    private float sx, svx, spawnT, squash, lean, anim, doubleT, slowT, hurtT, lastMx = -9999, hintT;
    private int lives, combo, caught, bestCombo;
    private boolean left, right, mouseMode;

    public GiftCatch() { super("giftcatch"); }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }

    @Override
    protected void reset() {
        items.clear();
        pile.clear();
        fx.clear();
        sx = W / 2; svx = 0; spawnT = 0.6f; squash = 0; lean = 0;
        lives = 3; combo = 0; caught = 0; bestCombo = 0;
        doubleT = slowT = hurtT = hintT = 0;
        left = right = false;
    }

    private float level() { return Math.min(1, time / 150f); }
    private int mult() { return (combo >= 25 ? 4 : combo >= 12 ? 3 : combo >= 5 ? 2 : 1) * (doubleT > 0 ? 2 : 1); }

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
    protected void update(float rdt) {
        fx.update(rdt);
        hintT += rdt;
        doubleT = Math.max(0, doubleT - rdt);
        slowT = Math.max(0, slowT - rdt);
        hurtT = Math.max(0, hurtT - rdt);
        squash = Math.max(0, squash - rdt * 4);
        float dt = rdt * (slowT > 0 ? 0.5f : 1);

        // sleigh movement
        if (Math.abs(mx - lastMx) > 0.5f && mx >= 0) { if (lastMx > -9999) mouseMode = true; lastMx = mx; }
        float prev = sx;
        if (mouseMode && u > 0) {
            float target = (mx - bx) / u;
            sx += (target - sx) * Math.min(1, rdt * 16);
        } else {
            float dir = (right ? 1 : 0) - (left ? 1 : 0);
            svx += (dir * 330 - svx) * Math.min(1, rdt * 10);
            sx += svx * rdt;
        }
        sx = Math.max(SW / 2 + 2, Math.min(W - SW / 2 - 2, sx));
        float v = (sx - prev) / rdt;
        lean += (Math.max(-1, Math.min(1, v / 300)) - lean) * Math.min(1, rdt * 10);

        // spawning
        spawnT -= dt;
        if (spawnT <= 0) {
            float lv = level();
            spawnT = Ease.lerp(1.05f, 0.34f, lv) * (0.75f + rnd.nextFloat() * 0.5f);
            spawn(lv);
        }
        // items
        for (int i = items.size() - 1; i >= 0; i--) {
            Item it = items.get(i);
            it.t += dt;
            it.y += it.vy * dt;
            it.x += (float) Math.cos(it.t * 2.2f + it.sway) * it.sway * 6 * dt;
            // catch
            float half = it.size / 2;
            if (it.y + half >= SLEIGH_Y - 8 && it.y - half <= SLEIGH_Y + 6 && Math.abs(it.x - sx) < SW / 2 + half * 0.4f) {
                items.remove(i);
                catchItem(it);
                continue;
            }
            if (it.y - half > GROUND) {
                items.remove(i);
                if (it.type == GIFT || it.type == GOLD) miss(it);
                else fx.burst(it.x, GROUND, 6, 0xFFE9F2FF, 50, 3, 0.4f, 100, GameFx.FLAKE);
            }
        }
    }

    private void spawn(float lv) {
        Item it = new Item();
        float r = rnd.nextFloat();
        float coalP = 0.12f + lv * 0.22f;
        if (r < coalP) it.type = COAL;
        else if (r < coalP + 0.04f) it.type = GOLD;
        else if (r < coalP + 0.065f) it.type = rnd.nextInt(3) == 0 && lives < 3 ? HEART : (rnd.nextBoolean() ? STAR : CLOCK);
        else it.type = GIFT;
        it.color = rnd.nextInt(5);
        it.size = it.type == GIFT ? 17 + rnd.nextFloat() * 5 : it.type == COAL ? 17 : 18;
        it.x = 16 + rnd.nextFloat() * (W - 32);
        it.y = -14;
        it.vy = Ease.lerp(72, 175, lv) * (0.85f + rnd.nextFloat() * 0.3f) * (it.type == GOLD ? 1.25f : 1);
        it.sway = rnd.nextFloat() * 2.5f;
        it.t = rnd.nextFloat() * 6;
        items.add(it);
    }

    private void catchItem(Item it) {
        squash = 1;
        float y = SLEIGH_Y - 14;
        switch (it.type) {
            case COAL:
                lives--;
                combo = 0;
                hurtT = 0.6f;
                fx.burst(it.x, y, 22, 0xFF2A2A30, 150, 4.5f, 0.7f, 200, GameFx.DOT);
                fx.burst(it.x, y, 10, 0xFFFF7A2E, 120, 2.2f, 0.5f, 100, GameFx.SPARK);
                fx.shake(0.7f);
                fx.flash(0xFFFF3B4E, 0.7f);
                fx.popup("Kohle!", it.x, y - 10, 0xFFFF6B6B, 12);
                if (!pile.isEmpty()) pile.remove(pile.size() - 1);
                Sound.play("minecraft:entity.generic.extinguish_fire", 0.8f, 0.4f);
                if (lives <= 0) gameOver();
                return;
            case STAR:
                doubleT = 7;
                fx.burst(it.x, y, 24, 0xFFF4C152, 170, 3, 0.8f, 0, GameFx.SPARK);
                fx.popup("Doppelte Punkte!", it.x, y - 14, 0xFFF4C152, 11);
                Sound.play("minecraft:block.amethyst_block.chime", 1.4f, 0.8f);
                return;
            case CLOCK:
                slowT = 6;
                fx.ring(it.x, y, 60, 0xFF7CF7FF, 0.6f);
                fx.popup("Zeitlupe!", it.x, y - 14, 0xFF7CF7FF, 11);
                Sound.play("minecraft:block.note_block.chime", 0.7f, 0.5f);
                return;
            case HEART:
                lives = Math.min(3, lives + 1);
                fx.burst(it.x, y, 16, 0xFFFF4D6D, 120, 3, 0.7f, 0, GameFx.SPARK);
                fx.popup("+1 Leben", it.x, y - 14, 0xFFFF6B8A, 11);
                Sound.play("minecraft:entity.player.levelup", 1.6f, 0.35f);
                return;
            default:
        }
        combo++;
        bestCombo = Math.max(bestCombo, combo);
        caught++;
        int base = it.type == GOLD ? 50 : 10;
        int pts = base * mult();
        score += pts;
        pile.add(it.type == GOLD ? 5 : it.color);
        if (pile.size() > 5) pile.remove(0);
        fx.confetti(it.x, y, it.type == GOLD ? 26 : 14, it.type == GOLD ? new int[]{0xFFF4C152, 0xFFFFE9A8, 0xFFFFFFFF} : BULBS, 150, 3.5f);
        fx.ring(it.x, y + 4, 26, it.type == GOLD ? 0xFFF4C152 : 0xFFFFFFFF, 0.3f);
        fx.popup("+" + pts, it.x, y - 8, it.type == GOLD ? 0xFFF4C152 : 0xFFFFFFFF, it.type == GOLD ? 13 : 10);
        if (combo == 5 || combo == 12 || combo == 25) { fx.popup("Combo x" + (combo >= 25 ? 4 : combo >= 12 ? 3 : 2) + "!", W / 2, 90, 0xFFFF7BD5, 15); fx.shake(0.25f); Sound.play("minecraft:entity.player.levelup", 1.3f, 0.3f); }
        Sound.play("minecraft:entity.item.pickup", 1.0f + Math.min(1f, combo * 0.03f), 0.35f);
    }

    private void miss(Item it) {
        lives--;
        combo = 0;
        hurtT = 0.5f;
        fx.confetti(it.x, GROUND - 4, 12, new int[]{0xFF8A8F99, 0xFFE9F2FF}, 90, 3);
        fx.burst(it.x, GROUND, 10, 0xFFE9F2FF, 70, 3, 0.5f, 60, GameFx.FLAKE);
        fx.popup("Verpasst!", it.x, GROUND - 20, 0xFFFFB0B0, 10);
        fx.shake(0.35f);
        Sound.play("minecraft:block.snow.break", 0.7f, 0.6f);
        if (lives <= 0) gameOver();
    }

    // ------------------------------------------------------------------ drawing
    @Override
    protected void render() {
        float dt = Math.min(0.05f, Gx.dt);
        anim += dt;
        if (state == State.READY) sx = W / 2 + (float) Math.sin(anim * 1.3f) * 80;

        // background
        GameFx.sprite(this, "gc_bg", 0, 0, W, H, 380, 280, GiftCatch::paintBackground, 0xFFFFFFFF);
        // light snow
        for (int i = 0; i < 40; i++) {
            float fx0 = (i * 97.3f + anim * (6 + i % 5 * 3)) % W, fy = (i * 53.7f + anim * (14 + i % 7 * 4)) % H;
            circle(fx0 + (float) Math.sin(anim + i) * 4, fy, 0.8f + (i % 3) * 0.5f, 0x88FFFFFF);
        }
        if (slowT > 0) box(0, 0, W, H, 0, Gx.withAlpha(0xFF7CF7FF, 0.08f + 0.04f * (float) Math.sin(anim * 6)));

        Gx.push();
        Gx.translate(S(fx.sx), S(fx.sy));
        // fairy lights
        lights();
        // items
        for (Item it : items) item(it);
        // sleigh
        sleigh();
        fx.draw(this, 0, 0);
        Gx.pop();

        // HUD
        for (int i = 0; i < 3; i++) {
            boolean on = i < lives;
            float pulse = on && lives == 1 ? 1 + 0.12f * (float) Math.sin(anim * 10) : 1;
            Gx.icon(on ? "heart-fill" : "heart", X(16 + i * 15), Y(34), 11 * u * pulse, on ? 0xFFFF4D6D : 0x55FFFFFF);
        }
        int m = mult();
        if (combo > 0 || m > 1) {
            float w = 70;
            box(W - w - 8, 25, w, 18, 9, m > 1 ? Gx.withAlpha(0xFFE3263B, 0.9f) : 0x66000000);
            text((m > 1 ? "x" + m + "  " : "") + combo + " Combo", W - w / 2 - 8, 34, 7, Fonts.BOLD, 0xFFFFFFFF);
        }
        float py = 50;
        if (doubleT > 0) { power(W - 8, py, "star-fill", 0xFFF4C152, doubleT / 7); py += 16; }
        if (slowT > 0) power(W - 8, py, "clock", 0xFF7CF7FF, slowT / 6);
        if (state == State.PLAYING && hintT < 3.5f) {
            float a = Math.min(1, 3.5f - hintT) * (0.6f + 0.4f * (float) Math.sin(anim * 5));
            box(W / 2 - 110, 120, 220, 22, 11, Gx.withAlpha(0xFF000000, 0.45f * a));
            text("Maus oder ← → – fang die Geschenke, keine Kohle!", W / 2, 131, 7.2f, Fonts.BOLD, Gx.withAlpha(0xFFFFFFFF, a));
        }
        fx.drawPopups(this, 0, 0);
        if (hurtT > 0) box(0, 0, W, H, 0, Gx.withAlpha(0xFFFF2040, hurtT * 0.25f));
        fx.drawFlash(this, W, H);
    }

    private void power(float right, float y, String icon, int col, float frac) {
        float w = 52;
        box(right - w, y - 7, w, 14, 7, 0x66000000);
        Gx.icon(icon, X(right - w + 8), Y(y), 8 * u, col);
        box(right - w + 15, y - 1.5f, (w - 21), 3, 1.5f, 0x33FFFFFF);
        box(right - w + 15, y - 1.5f, (w - 21) * frac, 3, 1.5f, col);
    }

    private void lights() {
        for (int seg = 0; seg < 3; seg++) {
            float x0 = seg * W / 3f, x1 = (seg + 1) * W / 3f;
            for (int i = 0; i <= 24; i++) {
                float t = i / 24f, lx = Ease.lerp(x0, x1, t), ly = 6 + (float) Math.sin(t * Math.PI) * 12;
                circle(lx, ly, 0.7f, 0xFF1A2A1E);
            }
            for (int i = 1; i < 6; i++) {
                float t = i / 6f, lx = Ease.lerp(x0, x1, t), ly = 7 + (float) Math.sin(t * Math.PI) * 12;
                int c = BULBS[(seg * 5 + i) % BULBS.length];
                float tw = 0.55f + 0.45f * (float) Math.sin(anim * 3 + seg * 2 + i * 1.7f);
                Gx.glow(X(lx), Y(ly + 3), S(9), Gx.withAlpha(c, 0.45f * tw));
                box(lx - 1.3f, ly - 1, 2.6f, 2.2f, 0.5f, 0xFF2A3A2E);
                boxV(lx - 2, ly + 0.8f, 4, 5.5f, 2, Gx.mix(c, 0xFFFFFFFF, 0.3f + 0.3f * tw), Gx.mix(c, 0xFF000000, 0.3f * (1 - tw)));
            }
        }
    }

    private void item(Item it) {
        float s = it.size;
        float wob = 1 + 0.05f * (float) Math.sin(it.t * 7);
        float x = it.x, y = it.y;
        switch (it.type) {
            case COAL:
                Gx.glow(X(x), Y(y), S(s), 0x33FF5A1E);
                GameFx.sprite(this, "gc_coal", x - s / 2, y - s / 2, s, s, 40, 40, GiftCatch::paintCoal, 0xFFFFFFFF);
                break;
            case STAR:
                Gx.glow(X(x), Y(y), S(s * 1.4f), 0x66F4C152);
                Gx.icon("star-fill", X(x), Y(y), s * u * wob, 0xFFF4C152);
                break;
            case CLOCK:
                Gx.glow(X(x), Y(y), S(s * 1.4f), 0x557CF7FF);
                circle(x, y, s / 2, 0xFF1C3350);
                Gx.icon("clock", X(x), Y(y), s * 0.8f * u, 0xFF7CF7FF);
                break;
            case HEART:
                Gx.glow(X(x), Y(y), S(s * 1.4f), 0x66FF4D6D);
                Gx.icon("heart-fill", X(x), Y(y), s * u * wob, 0xFFFF4D6D);
                break;
            default: {
                int c = it.type == GOLD ? 5 : it.color;
                if (it.type == GOLD) Gx.glow(X(x), Y(y), S(s * 1.5f), 0x66F4C152);
                float w = s * wob, h = s * (2 - wob);
                GameFx.spriteAt(this, "sj_gift" + c, s, s, 40, 40, gr -> SantaJump.paintGift(gr, c), x - w / 2, y - h / 2, w, h, 0xFFFFFFFF);
            }
        }
    }

    private void sleigh() {
        float sq = Ease.outCubic(squash);
        float w = SW * (1 + 0.1f * sq), h = 30 * (1 - 0.12f * sq);
        float x = sx - w / 2, y = SLEIGH_Y + 22 - h;
        // shadow on snow
        box(sx - SW * 0.45f, GROUND - 2, SW * 0.9f, 4, 2, 0x33000000);
        if (doubleT > 0) Gx.glow(X(sx), Y(SLEIGH_Y), S(50), Gx.withAlpha(0xFFF4C152, 0.25f));
        // gift pile (behind the front panel)
        for (int i = 0; i < pile.size(); i++) {
            int c = pile.get(i);
            float gx = sx - 20 + (i % 3) * 14 - lean * 3 * (i / 3 + 1), gy = SLEIGH_Y - 2 - (i / 3) * 9 + sq * 3;
            GameFx.sprite(this, "sj_gift" + c + "s", gx - 7, gy - 7, 14, 14, 40, 40, gr -> SantaJump.paintGift(gr, c), 0xFFFFFFFF);
        }
        GameFx.spriteAt(this, "gc_sleigh", SW, 30, 66, 30, GiftCatch::paintSleigh, x, y, w, h, 0xFFFFFFFF);
    }

    // ------------------------------------------------------------------ painters
    private static void paintBackground(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 0, new Color(0x0A1430), 0, 260, new Color(0x2A4E86)));
        g.fillRect(0, 0, 380, 280);
        Random r = new Random(12);
        for (int i = 0; i < 70; i++) {
            float x = r.nextFloat() * 380, y = r.nextFloat() * 170, s = 0.6f + r.nextFloat() * 1.4f;
            g.setColor(new Color(255, 255, 255, 60 + r.nextInt(150)));
            g.fill(new Ellipse2D.Float(x, y, s, s));
        }
        // moon
        g.setPaint(new RadialGradientPaint(new Point2D.Float(300, 70), 60, new float[]{0f, 1f}, new Color[]{new Color(255, 244, 214, 90), new Color(255, 244, 214, 0)}));
        g.fill(new Ellipse2D.Float(240, 10, 120, 120));
        g.setColor(new Color(0xFFF4D6));
        g.fill(new Ellipse2D.Float(286, 56, 28, 28));
        g.setColor(new Color(0, 0, 0, 25));
        g.fill(new Ellipse2D.Float(300, 62, 6, 6));
        // far hills
        g.setColor(new Color(0x1B3561));
        g.fill(new Ellipse2D.Float(-80, 170, 300, 160));
        g.fill(new Ellipse2D.Float(160, 160, 320, 170));
        g.setColor(new Color(0x234575));
        g.fill(new Ellipse2D.Float(60, 195, 320, 140));
        // fir trees
        for (int i = 0; i < 14; i++) {
            float tx = 6 + i * 28 + r.nextFloat() * 8, ty = 224 + r.nextFloat() * 8, s = 0.6f + r.nextFloat() * 0.6f;
            g.setColor(new Color(0x0D3A33));
            for (int k = 0; k < 3; k++) {
                float ky = ty - k * 9 * s, kw = (14 - k * 3.5f) * s;
                g.fillPolygon(new int[]{(int) tx, (int) (tx - kw), (int) (tx + kw)}, new int[]{(int) (ky - 14 * s), (int) ky, (int) ky}, 3);
            }
            g.setColor(new Color(230, 240, 255, 170));
            g.fillPolygon(new int[]{(int) tx, (int) (tx - 4 * s), (int) (tx + 4 * s)}, new int[]{(int) (ty - 32 * s), (int) (ty - 25 * s), (int) (ty - 25 * s)}, 3);
        }
        // cottages with lit windows
        float[] hx = {30, 150, 270};
        for (float x : hx) {
            float w = 46, h = 30, y = 244 - h;
            g.setColor(new Color(0x3A2A3A));
            g.fill(new Rectangle2D.Float(x, y, w, h));
            GeneralPath roof = new GeneralPath();
            roof.moveTo(x - 5, y + 1); roof.lineTo(x + w / 2, y - 18); roof.lineTo(x + w + 5, y + 1); roof.closePath();
            g.setColor(new Color(0x4A2E36));
            g.fill(roof);
            g.setColor(new Color(0xF4F8FF));
            g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine((int) (x - 4), (int) y, (int) (x + w / 2), (int) (y - 17));
            g.drawLine((int) (x + w / 2), (int) (y - 17), (int) (x + w + 4), (int) y);
            for (int k = 0; k < 2; k++) {
                float wx = x + 8 + k * 22, wy = y + 9;
                g.setPaint(new RadialGradientPaint(new Point2D.Float(wx + 4, wy + 4), 14, new float[]{0f, 1f}, new Color[]{new Color(255, 190, 90, 130), new Color(255, 190, 90, 0)}));
                g.fill(new Ellipse2D.Float(wx - 10, wy - 10, 28, 28));
                g.setColor(new Color(0xFFCF6E));
                g.fill(new Rectangle2D.Float(wx, wy, 8, 9));
                g.setColor(new Color(0x3A2A3A));
                g.fill(new Rectangle2D.Float(wx + 3.5f, wy, 1, 9));
            }
        }
        // snow ground
        GeneralPath snow = new GeneralPath();
        snow.moveTo(0, 280); snow.lineTo(0, 254);
        for (int x = 0; x <= 380; x += 10) snow.lineTo(x, 252 + Math.sin(x / 37.0) * 3);
        snow.lineTo(380, 280); snow.closePath();
        g.setPaint(new GradientPaint(0, 250, new Color(0xF4F8FF), 0, 280, new Color(0xB5CBEB)));
        g.fill(snow);
        g.setColor(new Color(255, 255, 255, 200));
        for (int i = 0; i < 60; i++) g.fill(new Ellipse2D.Float(r.nextFloat() * 380, 258 + r.nextFloat() * 20, 1.5f, 1.5f));
    }

    private static void paintSleigh(Graphics2D g) {
        // runners
        g.setColor(new Color(0xF4C152));
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GeneralPath run = new GeneralPath();
        run.moveTo(4, 27); run.lineTo(56, 27); run.curveTo(64, 27, 66, 20, 60, 17);
        g.draw(run);
        g.drawLine(14, 27, 14, 22); g.drawLine(44, 27, 44, 22);
        // body
        GeneralPath b = new GeneralPath();
        b.moveTo(2, 6); b.curveTo(0, 16, 6, 23, 16, 23); b.lineTo(52, 23); b.curveTo(60, 23, 64, 16, 62, 8); b.lineTo(56, 10); b.lineTo(10, 10); b.closePath();
        g.setPaint(new GradientPaint(0, 6, new Color(0xF0364A), 0, 23, new Color(0x8E0E20)));
        g.fill(b);
        g.setColor(new Color(0x5E0814));
        g.setStroke(new BasicStroke(1f));
        g.draw(b);
        // gold trim + swirl
        g.setColor(new Color(0xF4C152));
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(10, 11, 56, 11);
        g.drawArc(22, 13, 8, 7, 0, 300);
        g.drawArc(36, 13, 8, 7, 240, 300);
        g.setColor(new Color(255, 255, 255, 90));
        g.fill(new RoundRectangle2D.Float(12, 13, 6, 2, 2, 2));
        // front curl
        g.setColor(new Color(0xC51A2E));
        g.fill(new Ellipse2D.Float(57, 3, 8, 8));
        g.setColor(new Color(0xF4C152));
        g.fill(new Ellipse2D.Float(59.5f, 5.5f, 3, 3));
        // snow on edge
        g.setColor(new Color(0xF4F8FF));
        g.fill(new RoundRectangle2D.Float(1, 4, 12, 4, 4, 4));
    }

    private static void paintCoal(Graphics2D g) {
        GeneralPath p = new GeneralPath();
        p.moveTo(8, 14); p.lineTo(18, 5); p.lineTo(30, 8); p.lineTo(36, 20); p.lineTo(31, 33); p.lineTo(17, 36); p.lineTo(6, 28); p.closePath();
        g.setPaint(new GradientPaint(0, 5, new Color(0x4A4A55), 0, 36, new Color(0x121216)));
        g.fill(p);
        g.setColor(new Color(0x6A6A78));
        g.fill(new java.awt.Polygon(new int[]{18, 30, 22}, new int[]{5, 8, 14}, 3));
        g.setColor(new Color(255, 110, 40, 200));
        g.fill(new Ellipse2D.Float(14, 24, 4, 3));
        g.fill(new Ellipse2D.Float(26, 18, 3, 3));
        // angry face
        g.setColor(new Color(0xFF6A3A));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(13, 15, 18, 17); g.drawLine(29, 15, 24, 17);
        g.fill(new Ellipse2D.Float(14, 17, 3, 3));
        g.fill(new Ellipse2D.Float(24, 17, 3, 3));
        g.drawArc(16, 25, 10, 5, 0, 180);
    }
}
