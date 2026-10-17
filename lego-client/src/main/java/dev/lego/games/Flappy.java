package dev.lego.games;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Flappy Lego: a little brick flutters through gaps between stacked Lego pillars. */
public final class Flappy extends GameView {
    private static final float W = 360, H = 280, GROUND = 254;
    private static final float BIRD_X = 88, BW = 24, BH = 17;
    private static final float PW = 40, ROW = 12, SPACING = 165;
    private static final float GRAVITY = 980, FLAP = -268, MAX_FALL = 420;
    private static final int[] COLORS = {0xFFC91A09, 0xFF0055BF, 0xFF4B9F4A, 0xFFFE8A18, 0xFF923978, 0xFF36AEBF};
    private static final int BIRD = 0xFFF2CD37;

    private static final class Pillar {
        float x, gapY, gap;
        int color;
        boolean passed;
    }

    private final List<Pillar> pillars = new ArrayList<>();
    private final List<float[]> puffs = new ArrayList<>();   // x, y, vx, vy, t
    private final float[][] clouds = new float[7][3];         // x, y, scale
    private float y, vy, scroll, wing, flash, anim, scorePop;
    private boolean flying;
    private int colorIdx;

    public Flappy() {
        super("flappy");
        java.util.Random r = new java.util.Random(7);
        for (int i = 0; i < clouds.length; i++) {
            clouds[i][0] = i * (W + 120) / clouds.length + r.nextFloat() * 30;
            clouds[i][1] = 22 + r.nextFloat() * 80;
            clouds[i][2] = 0.7f + r.nextFloat() * 0.6f;
        }
    }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Säulen"; }

    @Override
    protected void reset() {
        pillars.clear();
        puffs.clear();
        y = H * 0.42f;
        vy = 0;
        wing = 0;
        flash = 0;
        scorePop = 0;
        flying = false;
        colorIdx = rnd.nextInt(COLORS.length);
    }

    private void flap() {
        if (!flying) {
            flying = true;
            addPillar(W + 60);
        }
        vy = FLAP;
        wing = 1;
        for (int i = 0; i < 4; i++)
            puffs.add(new float[]{BIRD_X - 10, y + 4, -40 - rnd.nextFloat() * 50, 10 + rnd.nextFloat() * 40 - 20, 0});
        Sound.play("minecraft:entity.bat.takeoff", 1.6f + rnd.nextFloat() * 0.2f, 0.12f);
    }

    private void addPillar(float x) {
        Pillar p = new Pillar();
        p.x = x;
        p.gap = Math.max(70, 90 - score * 0.8f);
        float min = 34 + p.gap / 2, max = GROUND - 30 - p.gap / 2;
        p.gapY = min + rnd.nextFloat() * (max - min);
        p.color = COLORS[colorIdx++ % COLORS.length];
        pillars.add(p);
    }

    @Override
    protected void onKey(int k) {
        if (k == K_SPACE || k == K_UP || k == K_W) flap();
    }

    @Override
    protected void onClick(float x, float yy, int button) {
        if (button == 0) flap();
    }

    private float speed() { return 112 + Math.min(40, score * 1.2f); }

    @Override
    protected void update(float dt) {
        float sp = speed();
        scroll += sp * dt;
        wing = Math.max(0, wing - dt * 5);
        scorePop = Math.max(0, scorePop - dt * 3);
        for (float[] p : puffs) { p[0] += p[2] * dt; p[1] += p[3] * dt; p[4] += dt; }
        puffs.removeIf(p -> p[4] > 0.45f);
        if (!flying) {
            y = H * 0.42f + (float) Math.sin(time * 5) * 5;
            return;
        }
        vy = Math.min(MAX_FALL, vy + GRAVITY * dt);
        y += vy * dt;
        if (y < BH / 2) { y = BH / 2; vy = Math.max(vy, 0); }
        for (Pillar p : pillars) p.x -= sp * dt;
        pillars.removeIf(p -> p.x < -PW - 20);
        Pillar last = pillars.isEmpty() ? null : pillars.get(pillars.size() - 1);
        if (last == null || last.x < W + 20 - SPACING) addPillar(last == null ? W + 20 : last.x + SPACING);
        // scoring + collision (slightly forgiving hit box)
        float l = BIRD_X - BW / 2 + 3, r = BIRD_X + BW / 2 - 2, t = y - BH / 2 + 3, b = y + BH / 2 - 2;
        for (Pillar p : pillars) {
            if (!p.passed && p.x + PW < BIRD_X - BW / 2) {
                p.passed = true;
                score++;
                scorePop = 1;
                Sound.play("minecraft:entity.experience_orb.pickup", 1.3f + rnd.nextFloat() * 0.2f, 0.25f);
            }
            float cl = p.x - 3, cr = p.x + PW + 3;   // caps are a bit wider
            if (r > cl && l < cr && (t < p.gapY - p.gap / 2 || b > p.gapY + p.gap / 2)) { crash(); return; }
        }
        if (b >= GROUND) { y = GROUND - BH / 2 + 2; crash(); }
    }

    private void crash() {
        flash = 1;
        Sound.play("minecraft:entity.arrow.hit_player", 0.8f, 0.4f);
        gameOver();
    }

    // ------------------------------------------------------------------ drawing
    @Override
    protected void render() {
        float dt = Math.min(0.05f, Gx.dt);
        anim += dt;
        flash = Math.max(0, flash - dt * 2.5f);
        if (state == State.READY) y = H * 0.42f + (float) Math.sin(anim * 4) * 5;

        // sky
        boxV(0, 0, W, GROUND + 12, 12, 0xFF2A5B93, 0xFF93C9E8);
        Gx.glow(X(W - 70), Y(52), S(90), 0x55FFF3C4);
        circle(W - 70, 52, 17, 0xFFFFF1B8);
        circle(W - 70, 52, 14, 0xFFFFF8DC);
        // clouds (slowest parallax)
        float wrap = W + 120;
        for (float[] c : clouds) {
            float cx = ((c[0] - scroll * 0.12f) % wrap + wrap) % wrap - 60;
            cloud(cx, c[1], c[2]);
        }
        // far hills
        hills(0.22f, 150, 70, GROUND - 26, 0xFF6FA7A0, 1);
        // near hills with studs
        hills(0.45f, 120, 52, GROUND - 6, 0xFF3F8C5A, 2);

        // pillars
        for (Pillar p : pillars) pillar(p);

        // ground: green Lego baseplate with scrolling studs
        box(0, H - 26, W, 26, 12, 0xFF6B4A2E);
        boxV(0, GROUND, W, 12, 0, 0xFF2E9A4E, 0xFF1F7A3A);
        box(0, GROUND, W, 2, 0, 0xFF5BC476);
        float so = scroll % 12;
        for (float sx = -so; sx < W + 12; sx += 12) {
            boxV(sx + 2.5f, GROUND - 3.5f, 7, 4.5f, 1.2f, 0xFF62C97E, 0xFF2E9A4E);
        }
        for (float sx = -(scroll % 24); sx < W + 24; sx += 24) if (sx > 6 && sx < W - 6) box(sx, GROUND + 12, 1.2f, H - GROUND - 16, 0, 0x33000000);
        box(0, GROUND + 12, W, 1.5f, 0, 0x44000000);

        // puffs
        for (float[] p : puffs) {
            float a = 1 - p[4] / 0.45f;
            circle(p[0], p[1], 2 + p[4] * 8, Gx.withAlpha(0xFFFFFFFF, a * 0.6f));
        }

        // bird
        bird();

        // score
        if (flying || state == State.OVER) {
            float s = 1 + 0.25f * Ease.outCubic(scorePop);
            Gx.push();
            Gx.scaleAt(X(W / 2), Y(34), s);
            String sc = String.valueOf(score);
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4;
                text(sc, W / 2 + (float) Math.cos(a) * 1.4f, 34 + (float) Math.sin(a) * 1.4f + 0.6f, 26, Fonts.BOLD, 0xAA1B2A3A);
            }
            text(sc, W / 2, 34, 26, Fonts.BOLD, 0xFFFFFFFF);
            Gx.pop();
        } else if (state == State.PLAYING) {
            float a = 0.6f + 0.4f * (float) Math.sin(anim * 5);
            box(W / 2 - 78, H * 0.62f - 12, 156, 24, 12, 0x55000000);
            text("Leertaste oder Klick zum Flattern", W / 2, H * 0.62f, 7.6f, Fonts.BOLD, Gx.withAlpha(0xFFFFFFFF, a));
        }
        if (flash > 0) box(0, 0, W, H, 0, Gx.withAlpha(0xFFFFFFFF, flash * 0.55f));
    }

    private void cloud(float x, float y, float s) {
        int c = 0xFFEEF4FA;
        circle(x, y, 11 * s, c);
        circle(x + 13 * s, y - 5 * s, 14 * s, c);
        circle(x + 28 * s, y, 11 * s, c);
        box(x, y - 1 * s, 28 * s, 11 * s, 5 * s, c);
    }

    private void hills(float par, float spacing, float radius, float baseY, int col, int seed) {
        float off = (scroll * par) % spacing;
        for (float hx = -off - spacing; hx < W + spacing; hx += spacing) {
            int idx = Math.floorMod(Math.round((hx + scroll * par) / spacing) * 31 + seed * 7, 5);
            float r = radius * (0.8f + idx * 0.1f);
            circle(hx + spacing / 2, baseY + r * 0.55f, r, col);
            // a few studs on the hill top
            if (seed == 2) {
                float top = baseY + r * 0.55f - r;
                box(hx + spacing / 2 - 9, top - 2.5f, 7, 3.5f, 1, Gx.mix(col, 0xFFFFFFFF, 0.12f));
                box(hx + spacing / 2 + 2, top - 2.5f, 7, 3.5f, 1, Gx.mix(col, 0xFFFFFFFF, 0.12f));
            }
        }
        box(0, baseY, W, GROUND - baseY + 1, 0, col);
    }

    /** Side view brick: gradient body, highlight, studs on top. */
    private void sideBrick(float x, float yy, float w, float h, int col, int studs) {
        if (studs > 0) {
            float sw = 7, gap = (w - studs * sw) / studs;
            for (int i = 0; i < studs; i++) {
                float sx = x + gap / 2 + i * (sw + gap);
                boxV(sx, yy - 3, sw, 4, 1.2f, Gx.mix(col, 0xFFFFFFFF, 0.25f), col);
            }
        }
        boxV(x, yy, w, h, 2, Gx.mix(col, 0xFFFFFFFF, 0.14f), Gx.mix(col, 0xFF000000, 0.16f));
        box(x + 1.5f, yy + 1, w - 3, 1.2f, 0.6f, Gx.withAlpha(0xFFFFFFFF, 0.28f));
        Gx.outline(X(x), Y(yy), S(w), S(h), S(2), Math.max(1, S(0.6f)), Gx.withAlpha(Gx.mix(col, 0xFF000000, 0.55f), 0.7f));
    }

    private void pillar(Pillar p) {
        float top = p.gapY - p.gap / 2, bot = p.gapY + p.gap / 2;
        int col = p.color, dark = Gx.mix(col, 0xFF000000, 0.12f);
        Gx.shadow(X(p.x), Y(-10), S(PW), S(top + 10), S(2), S(4), 0.25f);
        Gx.shadow(X(p.x), Y(bot), S(PW), S(GROUND - bot), S(2), S(4), 0.25f);
        // upper stack (builds up from the cap)
        int i = 0;
        for (float yy = top - ROW * 2; yy > -ROW; yy -= ROW, i++) sideBrick(p.x + (i % 2 == 0 ? 0 : 0), yy, PW, ROW, i % 2 == 0 ? col : dark, 0);
        sideBrick(p.x - 3, top - ROW, PW + 6, ROW, Gx.mix(col, 0xFFFFFFFF, 0.08f), 0);
        // lower stack
        i = 0;
        for (float yy = bot + ROW; yy < GROUND; yy += ROW, i++) sideBrick(p.x, yy, PW, ROW, i % 2 == 0 ? dark : col, 0);
        sideBrick(p.x - 3, bot, PW + 6, ROW, Gx.mix(col, 0xFFFFFFFF, 0.08f), 3);
    }

    private void bird() {
        float ang = flying || state == State.OVER ? Math.max(-24, Math.min(55, vy / MAX_FALL * 60)) : (float) Math.sin(anim * 4) * 4;
        int q = Math.round(ang / 3) * 3;
        int px = Math.max(8, S(40));
        double rad = Math.toRadians(q);
        Gx.glow(X(BIRD_X), Y(y), S(26), 0x22FFF3A0);
        Gx.Img img = Gx.painted("flappyBird" + q, px, px, g -> paintBird(g, px, rad));
        Gx.image(img, X(BIRD_X) - px / 2, Y(y) - px / 2, px, px, 0xFFFFFFFF);
        // flapping wing (rotated with the body)
        float wx = -4, wy = 2 + (wing > 0 ? -4 * wing : 0);
        float cos = (float) Math.cos(rad), sin = (float) Math.sin(rad);
        float rx = BIRD_X + wx * cos - wy * sin, ry = y + wx * sin + wy * cos;
        float wh = 4 + 3 * wing;
        boxV(rx - 5, ry - wh / 2, 10, wh, 2, 0xFFFFE27A, 0xFFE0A91C);
        Gx.outline(X(rx - 5), Y(ry - wh / 2), S(10), S(wh), S(2), Math.max(1, S(0.5f)), 0x88704F00);
    }

    private static void paintBird(Graphics2D g, int px, double rad) {
        float k = px / 40f;
        g.translate(px / 2.0, px / 2.0);
        g.rotate(rad);
        g.scale(k, k);
        Color base = new Color(BIRD, true), light = new Color(Gx.mix(BIRD, 0xFFFFFFFF, 0.3f), true), dark = new Color(Gx.mix(BIRD, 0xFF000000, 0.2f), true);
        Color line = new Color(Gx.mix(BIRD, 0xFF000000, 0.55f), true);
        g.setStroke(new BasicStroke(0.8f));
        // studs (side view)
        for (int i = 0; i < 2; i++) {
            RoundRectangle2D st = new RoundRectangle2D.Float(-9.5f + i * 11, -BH / 2 - 3.2f, 8, 4.2f, 2, 2);
            g.setPaint(new GradientPaint(0, -BH / 2 - 3, light, 0, -BH / 2, base));
            g.fill(st);
            g.setColor(line);
            g.draw(st);
        }
        // body
        RoundRectangle2D body = new RoundRectangle2D.Float(-BW / 2, -BH / 2, BW, BH, 5, 5);
        g.setPaint(new GradientPaint(0, -BH / 2, light, 0, BH / 2, dark));
        g.fill(body);
        g.setColor(new Color(255, 255, 255, 80));
        g.fill(new RoundRectangle2D.Float(-BW / 2 + 2, -BH / 2 + 1.2f, BW - 4, 1.6f, 1.6f, 1.6f));
        g.setColor(line);
        g.draw(body);
        // beak
        Path2D beak = new Path2D.Float();
        beak.moveTo(BW / 2 - 1, -1.5);
        beak.lineTo(BW / 2 + 6, 1.5);
        beak.lineTo(BW / 2 - 1, 4.5);
        beak.closePath();
        g.setColor(new Color(0xFE8A18));
        g.fill(beak);
        g.setColor(new Color(0x9A4A00));
        g.draw(beak);
        // eye
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Float(3.2f, -6.2f, 7, 7));
        g.setColor(new Color(0x7A5A00));
        g.draw(new Ellipse2D.Float(3.2f, -6.2f, 7, 7));
        g.setColor(new Color(0x15161A));
        g.fill(new Ellipse2D.Float(6.4f, -4.4f, 3.2f, 3.4f));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Float(7.2f, -4.1f, 1.1f, 1.1f));
        // blush
        g.setColor(new Color(255, 110, 80, 90));
        g.fill(new Ellipse2D.Float(2.5f, 1.8f, 5, 3));
    }
}
