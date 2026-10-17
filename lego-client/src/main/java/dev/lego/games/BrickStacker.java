package dev.lego.games;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;

/** Lego-Stapler: drop the sliding brick onto the tower. Overhang is cut off, perfect drops grow the brick back. */
public final class BrickStacker extends GameView {
    private static final float W = 400, H = 320, BH = 16, START_W = 120, BASE_Y = 290, PERFECT = 3.2f;
    private static final int[] COLORS = {0xFFC91A09, 0xFFFE8A18, 0xFFF2CD37, 0xFF4B9F4A, 0xFF36AEBF, 0xFF0055BF, 0xFF923978, 0xFFE4ADC8};

    private static final class Brick {
        float x, w, wob, flash;
        int color;
    }

    private static final class Chip {
        float x, y, w, vx, vy, t;
        int color;
    }

    private final List<Brick> tower = new ArrayList<>();
    private final List<Chip> chips = new ArrayList<>();
    private final GameFx fx = new GameFx();
    private final float[][] clouds = new float[8][3];
    private final float[][] stars = new float[60][3];
    private float curX, curW, dir, speed, cam, anim, dropT, missT, hintT;
    private int combo, colorIdx, bestCombo;
    private boolean missed;

    public BrickStacker() {
        super("stacker");
        Random r = new Random(5);
        for (float[] c : clouds) { c[0] = r.nextFloat() * W; c[1] = 60 + r.nextFloat() * 1400; c[2] = 0.6f + r.nextFloat() * 0.7f; }
        for (float[] s : stars) { s[0] = r.nextFloat() * W; s[1] = r.nextFloat() * H; s[2] = 0.3f + r.nextFloat() * 0.7f; }
    }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Punkte"; }

    @Override
    protected void reset() {
        tower.clear();
        chips.clear();
        fx.clear();
        colorIdx = new Random().nextInt(COLORS.length);
        Brick b = new Brick();
        b.w = START_W; b.x = W / 2 - START_W / 2; b.color = COLORS[colorIdx++ % COLORS.length];
        tower.add(b);
        combo = 0; bestCombo = 0;
        cam = 0; missed = false; missT = 0; hintT = 0;
        next();
    }

    private float topY(int level) { return BASE_Y - (level + 1) * BH; }

    private void next() {
        Brick top = tower.get(tower.size() - 1);
        curW = top.w;
        dir = tower.size() % 2 == 0 ? -1 : 1;
        curX = dir > 0 ? -curW : W;
        speed = Math.min(300, 118 + tower.size() * 4.2f);
        dropT = 0;
    }

    // ------------------------------------------------------------------ input
    @Override
    protected void onKey(int k) {
        if (k == K_SPACE || k == K_DOWN || k == K_S || k == K_ENTER) drop();
    }

    @Override
    protected void onClick(float x, float y, int button) { if (button == 0) drop(); }

    private void drop() {
        if (missed || time < 0.2f || dropT < 0.12f) return;
        Brick top = tower.get(tower.size() - 1);
        float l = Math.max(curX, top.x), r = Math.min(curX + curW, top.x + top.w);
        float y = topY(tower.size());
        int col = COLORS[colorIdx % COLORS.length];
        if (r - l <= 0.5f) {
            // complete miss
            missed = true;
            Chip c = new Chip();
            c.x = curX; c.y = y; c.w = curW; c.vx = dir * speed * 0.3f; c.vy = 0; c.color = col;
            chips.add(c);
            fx.shake(0.6f);
            Sound.play("minecraft:block.stone.break", 0.6f, 0.5f);
            return;
        }
        Brick b = new Brick();
        b.color = col;
        colorIdx++;
        boolean perfect = Math.abs(curX - top.x) <= PERFECT;
        if (perfect) {
            combo++;
            bestCombo = Math.max(bestCombo, combo);
            b.x = top.x; b.w = top.w;
            if (combo >= 3) {
                float grow = Math.min(8, START_W - b.w);
                if (grow > 0) { b.x -= grow / 2; b.w += grow; b.x = Math.max(4, Math.min(W - 4 - b.w, b.x)); fx.popup("Wächst!", W / 2, y - 30, 0xFF9FFFB5, 9); }
            }
            b.flash = 1;
            score += 1 + combo;
            fx.ring(b.x + b.w / 2, y + BH / 2, b.w * 0.75f, 0xFFFFFFFF, 0.45f);
            fx.popup(combo > 1 ? "Perfekt! x" + combo : "Perfekt!", b.x + b.w / 2, y - 12, 0xFFF4C152, 11 + Math.min(4, combo));
            fx.burst(b.x + b.w / 2, y + BH / 2, 14 + combo * 2, 0xFFFFE9A8, 120, 2.4f, 0.6f, 0, GameFx.SPARK);
            Sound.play("minecraft:block.note_block.pling", 0.8f + Math.min(1.2f, combo * 0.1f), 0.45f);
        } else {
            combo = 0;
            b.x = l; b.w = r - l;
            // cut-off piece
            Chip c = new Chip();
            c.color = col; c.y = y;
            if (curX < top.x) { c.x = curX; c.w = top.x - curX; c.vx = -40 - rnd.nextFloat() * 30; }
            else { c.x = r; c.w = curX + curW - r; c.vx = 40 + rnd.nextFloat() * 30; }
            c.vy = -40;
            chips.add(c);
            score += 1;
            fx.popup("+1", b.x + b.w / 2, y - 10, 0xFFFFFFFF, 9);
            Sound.play("minecraft:block.stone.place", 0.9f + rnd.nextFloat() * 0.2f, 0.5f);
        }
        // dust + wobble
        fx.burst(b.x + 2, y + BH, 5, 0xFFFFFFFF, 40, 2, 0.4f, -20, GameFx.DOT);
        fx.burst(b.x + b.w - 2, y + BH, 5, 0xFFFFFFFF, 40, 2, 0.4f, -20, GameFx.DOT);
        for (int i = Math.max(0, tower.size() - 5); i < tower.size(); i++) tower.get(i).wob = 1 - (tower.size() - i) * 0.18f;
        b.wob = 1;
        tower.add(b);
        int lv = tower.size() - 1;
        if (lv % 10 == 0) { fx.popup(lv + " Steine!", W / 2, y - 34, 0xFFFF7BD5, 15); fx.shake(0.2f); Sound.play("minecraft:entity.player.levelup", 1.2f, 0.4f); }
        next();
    }

    // ------------------------------------------------------------------ logic
    @Override
    protected void update(float dt) {
        fx.update(dt);
        hintT += dt;
        dropT += dt;
        for (Brick b : tower) { b.wob = Math.max(0, b.wob - dt * 2.5f); b.flash = Math.max(0, b.flash - dt * 3); }
        for (Chip c : chips) { c.t += dt; c.vy += 900 * dt; c.x += c.vx * dt; c.y += c.vy * dt; }
        chips.removeIf(c -> c.y - camY() > H + 100);
        if (missed) { missT += dt; if (missT > 0.9f) gameOver(); return; }
        curX += dir * speed * dt;
        if (curX > W - curW * 0.1f && dir > 0) dir = -1;
        if (curX < -curW * 0.9f && dir < 0) dir = 1;
        float want = Math.max(0, (tower.size() - 9) * BH);
        cam += (want - cam) * Math.min(1, dt * 5);
    }

    /** Camera offset in board units (positive = scrolled up). */
    private float camY() { return -cam; }

    // ------------------------------------------------------------------ drawing
    @Override
    protected void render() {
        float dt = Math.min(0.05f, Gx.dt);
        anim += dt;
        float alt = cam / BH;   // levels scrolled
        float k1 = Ease.clamp(alt / 40f), k2 = Ease.clamp((alt - 40) / 50f);
        int top = Gx.mix(Gx.mix(0xFF5FA3E6, 0xFFE0765A, k1), 0xFF0A0B24, k2);
        int bot = Gx.mix(Gx.mix(0xFFCDE6FA, 0xFFF7C58A, k1), 0xFF2A1E4E, k2);
        boxV(0, 0, W, H, 0, top, bot);
        if (k2 > 0) for (float[] s : stars) {
            float sy = ((s[1] + cam * 0.1f * s[2]) % H + H) % H;
            circle(s[0], sy, 0.6f + s[2], Gx.withAlpha(0xFFFFFFFF, k2 * s[2] * (0.6f + 0.4f * (float) Math.sin(anim * 2 + s[0]))));
        }
        // sun going down
        float sunY = 70 + k1 * 60 + cam * 0.05f;
        if (sunY < H + 40) {
            Gx.glow(X(W - 70), Y(sunY), S(70), Gx.withAlpha(0xFFFFE6A0, 0.5f * (1 - k2)));
            circle(W - 70, sunY, 16, Gx.withAlpha(Gx.mix(0xFFFFF3C4, 0xFFFF9A5A, k1), 1 - k2));
        }
        // clouds (parallax)
        for (float[] c : clouds) {
            float cy = H - c[1] + cam * 0.55f;
            if (cy < -30 || cy > H + 30) continue;
            float cx = ((c[0] + anim * 6 * c[2]) % (W + 80) + W + 80) % (W + 80) - 40;
            cloud(cx, cy, c[2], Gx.withAlpha(0xFFFFFFFF, 0.75f * (1 - k2 * 0.7f)));
        }
        // far city of bricks
        float cityY = BASE_Y + cam * 0.35f;
        if (cityY - 80 < H) {
            for (int i = 0; i < 11; i++) {
                float bw = 26 + (i * 37 % 17), bhh = 30 + (i * 53 % 50);
                box(i * 40 - 6, cityY - bhh, bw, bhh + 60, 2, Gx.withAlpha(Gx.mix(0xFF6D8FB8, 0xFF3A2A5A, k1), 0.55f));
            }
        }

        Gx.push();
        Gx.translate(S(fx.sx), S(fx.sy));
        // baseplate
        float baseY = BASE_Y + cam;
        if (baseY < H + 20) {
            boxV(-4, baseY, W + 8, 40, 0, 0xFF3FAE58, 0xFF237A3A);
            for (float sx = 4; sx < W; sx += 12) boxV(sx, baseY - 3, 8, 4, 1.2f, 0xFF6BD583, 0xFF3FAE58);
        }
        // tower
        int first = Math.max(0, (int) (cam / BH) - 2);
        for (int i = first; i < tower.size(); i++) {
            Brick b = tower.get(i);
            float y = topY(i) + cam;
            if (y > H + 20) continue;
            if (y < -BH * 2) break;
            float wob = (float) Math.sin(b.wob * 14) * b.wob * 1.4f;
            int col = Gx.mix(b.color, 0xFFFFFFFF, b.flash * 0.6f);
            sideBrick(b.x + wob, y, b.w, BH, col);
        }
        // height marks
        for (int lv = 10; lv < tower.size() + 12; lv += 10) {
            float y = topY(lv - 1) + cam;
            if (y < -10 || y > H + 10) continue;
            for (float dx = 0; dx < W; dx += 10) box(dx, y, 5, 0.8f, 0.4f, 0x55FFFFFF);
            box(W - 34, y - 8, 30, 12, 6, 0x66000000);
            text(String.valueOf(lv), W - 19, y - 2, 6.5f, Fonts.BOLD, 0xFFFFFFFF);
        }
        // current brick
        if (!missed && state != State.OVER) {
            float y = topY(tower.size()) + cam - 2 - (float) Math.sin(anim * 6) * 0.8f;
            int col = COLORS[colorIdx % COLORS.length];
            // drop guide
            Brick t = tower.get(tower.size() - 1);
            float l = Math.max(curX, t.x), r = Math.min(curX + curW, t.x + t.w);
            if (r > l) box(l, y + BH + 1.5f, r - l, 1.2f, 0.6f, 0x88FFFFFF);
            Gx.glow(X(curX + curW / 2), Y(y + BH / 2), S(curW * 0.6f), Gx.withAlpha(col, 0.18f));
            sideBrick(curX, y, curW, BH, col);
            if (Math.abs(curX - t.x) <= PERFECT) box(curX, y - 5, curW, 1.5f, 0.7f, 0xCCF4C152);
        }
        // chips
        for (Chip c : chips) {
            float a = Math.max(0, 1 - c.t * 0.9f);
            Gx.pushAlpha(a);
            float sq = 1 - Math.min(0.3f, c.t * 0.3f);
            sideBrick(c.x, c.y + cam, c.w, BH * sq, c.color);
            Gx.popAlpha();
        }
        fx.draw(this, 0, -cam);
        Gx.pop();

        // HUD
        box(8, 8, 86, 22, 11, 0x55000000);
        Gx.icon("brick", X(21), Y(19), 10 * u, 0xFFFFFFFF);
        text((tower.size() - 1) + " Steine", 58, 19, 8, Fonts.BOLD, 0xFFFFFFFF);
        if (combo >= 2) {
            box(W - 94, 8, 86, 22, 11, Gx.withAlpha(0xFFF4C152, 0.95f));
            text("Perfekt-Serie " + combo, W - 51, 19, 7.4f, Fonts.BOLD, 0xFF3A2A00);
        }
        if (state == State.PLAYING && hintT < 3.5f && tower.size() < 3) {
            float a = Math.min(1, 3.5f - hintT) * (0.6f + 0.4f * (float) Math.sin(anim * 5));
            box(W / 2 - 96, 44, 192, 22, 11, Gx.withAlpha(0xFF000000, 0.45f * a));
            text("Leertaste oder Klick zum Absetzen", W / 2, 55, 7.6f, Fonts.BOLD, Gx.withAlpha(0xFFFFFFFF, a));
        }
        fx.drawPopups(this, 0, -cam);
        fx.drawFlash(this, W, H);
    }

    private void cloud(float x, float y, float s, int c) {
        circle(x, y, 10 * s, c);
        circle(x + 12 * s, y - 5 * s, 13 * s, c);
        circle(x + 26 * s, y, 10 * s, c);
        box(x, y - 1 * s, 26 * s, 10 * s, 5 * s, c);
    }

    /** Side view Lego brick with studs. */
    private void sideBrick(float x, float y, float w, float h, int col) {
        if (w < 0.5f) return;
        int studs = Math.max(1, Math.round(w / 15));
        float sw = Math.min(8, w * 0.6f), gap = w / studs;
        for (int i = 0; i < studs; i++) {
            float sx = x + gap * (i + 0.5f) - sw / 2;
            boxV(sx, y - 3.2f, sw, 4.2f, 1.2f, Gx.mix(col, 0xFFFFFFFF, 0.3f), col);
        }
        boxV(x, y, w, h, 2, Gx.mix(col, 0xFFFFFFFF, 0.16f), Gx.mix(col, 0xFF000000, 0.18f));
        box(x + 1.5f, y + 1.2f, Math.max(0, w - 3), 1.3f, 0.6f, 0x40FFFFFF);
        box(x, y + h - 2.2f, w, 2.2f, 1, Gx.withAlpha(Gx.mix(col, 0xFF000000, 0.35f), 0.6f));
        Gx.outline(X(x), Y(y), Math.max(1, S(w)), Math.max(1, S(h)), S(2), Math.max(1, S(0.55f)), Gx.withAlpha(Gx.mix(col, 0xFF000000, 0.55f), 0.7f));
    }
}
