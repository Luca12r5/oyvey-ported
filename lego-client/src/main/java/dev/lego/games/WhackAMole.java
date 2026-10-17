package dev.lego.games;

import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;

/** Hau den Maulwurf: Lego moles pop out of 9 holes - bonk them, don't hit the golden bombs. 45 seconds. */
public final class WhackAMole extends GameView {
    private static final float W = 360, H = 260;
    private final float[] up = new float[9], timer = new float[9], hitFx = new float[9];
    private final int[] kind = new int[9];      // 0 mole, 1 golden mole (x3), 2 bomb
    private float spawn, left;
    private int combo;

    public WhackAMole() { super("whackamole"); }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String overTitle() { return "Zeit um!"; }

    @Override
    protected void reset() {
        for (int i = 0; i < 9; i++) { up[i] = 0; timer[i] = 0; hitFx[i] = 0; }
        spawn = 0.6f; left = 45; combo = 0;
    }

    private float hx(int i) { return 70 + (i % 3) * 110; }
    private float hy(int i) { return 70 + (i / 3) * 70; }

    @Override
    protected void update(float dt) {
        left -= dt;
        if (left <= 0) { left = 0; gameOver(); return; }
        float pace = 1 + (45 - left) / 30f;
        spawn -= dt * pace;
        if (spawn <= 0) {
            spawn = 0.5f + rnd.nextFloat() * 0.5f;
            int i = rnd.nextInt(9);
            if (timer[i] <= 0) { timer[i] = 0.9f + rnd.nextFloat() * 0.6f / pace; float r = rnd.nextFloat(); kind[i] = r < 0.1f ? 1 : r < 0.22f ? 2 : 0; }
        }
        for (int i = 0; i < 9; i++) {
            hitFx[i] = Math.max(0, hitFx[i] - dt * 2.5f);
            if (timer[i] > 0) { timer[i] -= dt; up[i] = Math.min(1, up[i] + dt * 7); if (timer[i] <= 0 && kind[i] != 2) combo = 0; }
            else up[i] = Math.max(0, up[i] - dt * 7);
        }
    }

    @Override
    protected void onClick(float x, float y, int b) {
        for (int i = 0; i < 9; i++) {
            if (up[i] < 0.5f || timer[i] <= 0) continue;
            if (Math.hypot(x - hx(i), y - (hy(i) - 14)) > 24) continue;
            timer[i] = 0;
            hitFx[i] = 1;
            if (kind[i] == 2) { score = Math.max(0, score - 50); combo = 0; dev.lego.ui.Sound.play("minecraft:entity.generic.explode", 1.5f, 0.3f); }
            else { combo++; score += (kind[i] == 1 ? 30 : 10) * Math.min(5, 1 + combo / 4); dev.lego.ui.Sound.play("minecraft:entity.item.pickup", 1.4f + Math.min(0.6f, combo * 0.05f), 0.35f); }
            return;
        }
        combo = 0;
    }

    @Override
    protected void render() {
        boxV(0, 0, W, H, 10, 0xFF5DBB4E, 0xFF3E8E41);
        for (int i = 0; i < 9; i++) {
            float x = hx(i), y = hy(i);
            circle(x, y + 6, 26, 0xFF2D5A23);
            circle(x, y + 4, 24, 0xFF20140A);
            float h = up[i] * 26;
            if (h > 1) {
                int body = kind[i] == 2 ? 0xFF1E1E22 : kind[i] == 1 ? 0xFFFFCD03 : 0xFF8A5A32;
                Gx.clip(X(x - 26), Y(y - 60), S(52), S(64));
                box(x - 15, y - h - 4, 30, 30, 10, body);
                if (kind[i] == 2) { circle(x, y - h + 8, 10, 0xFF2A2A2E); box(x - 1, y - h - 10, 2, 8, 0, 0xFFFF8A00); circle(x, y - h - 11, 2.5f, 0xFFFFE14D); }
                else {
                    circle(x - 6, y - h + 4, 3, 0xFFFFFFFF); circle(x + 6, y - h + 4, 3, 0xFFFFFFFF);
                    circle(x - 6, y - h + 4.5f, 1.5f, 0xFF111111); circle(x + 6, y - h + 4.5f, 1.5f, 0xFF111111);
                    circle(x, y - h + 10, 3, 0xFFFF8FB0);
                    box(x - 4, y - h - 6, 8, 4, 2, Gx.mix(body, 0xFFFFFFFF, 0.3f));
                }
                Gx.unclip();
            }
            box(x - 26, y + 4, 52, 10, 5, 0xFF2D5A23);
            if (hitFx[i] > 0) text(kind[i] == 2 ? "-50" : "BONK!", x, y - 40 - (1 - hitFx[i]) * 16, 9, Fonts.BOLD, Gx.withAlpha(kind[i] == 2 ? 0xFFFF4D5E : 0xFFFFFFFF, hitFx[i]));
        }
        text(String.format(java.util.Locale.ROOT, "%.0f s", left), W - 34, 16, 9, Fonts.BOLD, left < 10 ? 0xFFFF4D5E : 0xFFFFFFFF);
        if (combo >= 4) text("Combo x" + Math.min(5, 1 + combo / 4), 50, 16, 8, Fonts.BOLD, 0xFFFFCD03);
    }
}