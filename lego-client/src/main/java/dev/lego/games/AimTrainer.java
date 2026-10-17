package dev.lego.games;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Aim trainer: click the shrinking targets within 30 seconds. Points depend on how close to the center you hit. */
public final class AimTrainer extends GameView {
    private static final float W = 380, H = 270, TOP = 34, DURATION = 30, MAX_R = 18;

    private static final class Target {
        float x, y, age, life;
        Target(float x, float y, float life) { this.x = x; this.y = y; this.life = life; }
        float radius() {
            float grow = 0.14f;
            if (age < grow) return MAX_R * Math.max(0, Ease.outBack(age / grow));
            return MAX_R * Math.max(0, 1 - (age - grow) / (life - grow));
        }
    }

    private final List<Target> targets = new ArrayList<>();
    private final List<float[]> parts = new ArrayList<>();    // x, y, vx, vy, age, life, size, color
    private final List<float[]> rings = new ArrayList<>();    // x, y, age, r0, color (shockwave / expire)
    private final List<float[]> pops = new ArrayList<>();     // x, y, age, points
    private final List<float[]> misses = new ArrayList<>();   // x, y, age
    private int hits, shots, expired, combo, bestCombo;
    private float spawnT, comboPop;

    public AimTrainer() { super("aim"); }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String overTitle() { return "Zeit um!"; }

    @Override
    protected void reset() {
        targets.clear();
        parts.clear();
        rings.clear();
        pops.clear();
        misses.clear();
        hits = shots = expired = combo = bestCombo = 0;
        spawnT = 0.35f;
        comboPop = 0;
    }

    private int accuracy() { return shots == 0 ? 100 : Math.round(hits * 100f / shots); }

    // ------------------------------------------------------------------ spawning
    private void spawn() {
        float pad = MAX_R + 6;
        for (int tries = 0; tries < 30; tries++) {
            float x = pad + rnd.nextFloat() * (W - pad * 2);
            float y = TOP + pad + rnd.nextFloat() * (H - TOP - pad * 2);
            boolean ok = true;
            for (Target t : targets) {
                float dx = t.x - x, dy = t.y - y;
                if (dx * dx + dy * dy < (MAX_R * 2.6f) * (MAX_R * 2.6f)) { ok = false; break; }
            }
            if (ok || tries == 29) {
                float life = 1.45f - 0.3f * Math.min(1, time / DURATION);
                targets.add(new Target(x, y, life));
                return;
            }
        }
    }

    // ------------------------------------------------------------------ input
    @Override
    protected void onClick(float x, float y, int button) {
        if (button != 0) return;
        if (y < TOP - 4) return;   // HUD bar
        shots++;
        Target hit = null;
        float best = Float.MAX_VALUE;
        for (Target t : targets) {
            float r = t.radius();
            float dx = x - t.x, dy = y - t.y, d = (float) Math.sqrt(dx * dx + dy * dy);
            if (d <= r + 1.2f && d / Math.max(1, r) < best) { best = d / Math.max(1, r); hit = t; }
        }
        if (hit == null) {
            combo = 0;
            misses.add(new float[]{x, y, 0});
            Sound.play("minecraft:block.note_block.hat", 0.6f, 0.25f);
            return;
        }
        targets.remove(hit);
        int pts = best <= 0.34f ? 100 : best <= 0.67f ? 70 : 40;
        score += pts;
        hits++;
        combo++;
        bestCombo = Math.max(bestCombo, combo);
        comboPop = 1;
        int col = pts == 100 ? 0xFFFFC53D : pts == 70 ? Style.accent : 0xFFFFFFFF;
        pops.add(new float[]{hit.x, hit.y, 0, pts});
        rings.add(new float[]{hit.x, hit.y, 0, hit.radius(), col});
        for (int i = 0; i < 16; i++) {
            float a = (float) (rnd.nextFloat() * Math.PI * 2), s = 50 + rnd.nextFloat() * 120;
            int c = i % 3 == 0 ? 0xFFFFFFFF : (i % 3 == 1 ? info.color : col);
            parts.add(new float[]{hit.x, hit.y, (float) Math.cos(a) * s, (float) Math.sin(a) * s - 30, 0, 0.45f + rnd.nextFloat() * 0.35f, 2 + rnd.nextFloat() * 2.5f, c});
        }
        Sound.play("minecraft:entity.arrow.hit_player", pts == 100 ? 1.25f : pts == 70 ? 1.05f : 0.85f, 0.35f);
        if (pts == 100) Sound.play("minecraft:entity.player.attack.crit", 1.2f, 0.2f);
        if (targets.isEmpty()) spawnT = Math.min(spawnT, 0.08f);
    }

    // ------------------------------------------------------------------ update
    @Override
    protected void update(float dt) {
        comboPop = Math.max(0, comboPop - dt * 4);
        for (Target t : targets) t.age += dt;
        for (int i = targets.size() - 1; i >= 0; i--) {
            Target t = targets.get(i);
            if (t.age >= t.life) {
                targets.remove(i);
                expired++;
                combo = 0;
                rings.add(new float[]{t.x, t.y, 0, 4, 0xFF8A909C});
            }
        }
        for (float[] p : parts) {
            p[0] += p[2] * dt;
            p[1] += p[3] * dt;
            p[3] += 360 * dt;
            p[2] *= 0.98f;
            p[4] += dt;
        }
        parts.removeIf(p -> p[4] >= p[5]);
        for (float[] r : rings) r[2] += dt;
        rings.removeIf(r -> r[2] > 0.45f);
        for (float[] p : pops) p[2] += dt;
        pops.removeIf(p -> p[2] > 0.8f);
        for (float[] m : misses) m[2] += dt;
        misses.removeIf(m -> m[2] > 0.5f);

        float prog = Math.min(1, time / DURATION);
        int maxTargets = prog < 0.3f ? 2 : 3;
        spawnT -= dt;
        if (targets.isEmpty() && spawnT > 0.12f) spawnT = 0.12f;
        if (spawnT <= 0 && targets.size() < maxTargets) {
            spawn();
            spawnT = 0.62f - 0.22f * prog + rnd.nextFloat() * 0.12f;
        }
        if (time + dt >= DURATION) {
            targets.clear();
            gameOver(true);
        }
    }

    // ------------------------------------------------------------------ render
    @Override
    protected void render() {
        // background: subtle grid + vignette glow
        boxV(0, 0, W, H, 12, Style.light ? 0xFFEEF0F5 : 0xFF10121A, Style.light ? 0xFFE0E3EA : 0xFF0A0B10);
        int grid = Style.light ? 0x0C000000 : 0x0AFFFFFF;
        for (float x = 20; x < W; x += 20) box(x, TOP, 0.6f, H - TOP, 0, grid);
        for (float y = TOP + 18; y < H; y += 20) box(0, y, W, 0.6f, 0, grid);
        Gx.glow(X(W / 2), Y(H / 2 + 10), S(170), Gx.withAlpha(info.color, 0.05f));

        // expire / shock rings
        for (float[] r : rings) {
            float k = r[2] / 0.45f, e = Ease.outCubic(k);
            float rad = r[3] + e * 16;
            Gx.ringAt(X(r[0]), Y(r[1]), S(rad), Math.max(1, S(2.2f * (1 - k) + 0.4f)), Gx.withAlpha((int) r[4], 0.8f * (1 - k)));
        }

        // targets
        for (Target t : targets) drawTarget(t);

        // particles
        for (float[] p : parts) {
            float a = 1 - p[4] / p[5];
            box(p[0] - p[6] / 2, p[1] - p[6] / 2, p[6], p[6], p[6] * 0.25f, Gx.withAlpha((int) p[7], a));
        }
        // miss markers
        for (float[] m : misses) {
            float k = m[2] / 0.5f, a = 1 - k, s = 3 + Ease.outCubic(k) * 2;
            Gx.push();
            Gx.pushAlpha(a);
            Gx.icon("x", X(m[0]), Y(m[1]), S(s * 2.4f), Style.danger);
            Gx.popAlpha();
            Gx.pop();
        }
        // floating points
        for (float[] p : pops) {
            float k = p[2] / 0.8f, a = k < 0.6f ? 1 : 1 - (k - 0.6f) / 0.4f;
            float sc = Ease.outBack(Math.min(1, p[2] / 0.18f));
            int pts = (int) p[3];
            int col = pts == 100 ? 0xFFFFC53D : pts == 70 ? Style.accent : 0xFFFFFFFF;
            Gx.push();
            Gx.scaleAt(X(p[0]), Y(p[1] - 16 - k * 14), 0.6f + 0.4f * sc);
            text("+" + pts, p[0], p[1] - 16 - k * 14, pts == 100 ? 11 : 9, Fonts.BOLD, Gx.withAlpha(col, a));
            Gx.pop();
        }

        hud();
        crosshair();
    }

    private void drawTarget(Target t) {
        float r = t.radius();
        if (r < 0.6f) return;
        float lifeK = t.age / t.life;
        int red = info.color, acc = Style.accent;
        Gx.glow(X(t.x), Y(t.y), S(r * 2.1f), Gx.withAlpha(red, 0.28f));
        circle(t.x, t.y + r * 0.08f, r * 1.02f, 0x55000000);                 // drop shadow
        circle(t.x, t.y, r, Gx.mix(red, 0xFF000000, 0.12f));
        circle(t.x, t.y, r * 0.84f, 0xFFF4F5F7);
        circle(t.x, t.y, r * 0.67f, red);
        circle(t.x, t.y, r * 0.5f, 0xFFF4F5F7);
        circle(t.x, t.y, r * 0.34f, acc);
        circle(t.x, t.y - r * 0.08f, r * 0.2f, Gx.mix(acc, 0xFFFFFFFF, 0.55f));   // glossy center stud
        // remaining-life arc hint: thin ring that fades as the target is about to vanish
        if (lifeK > 0.6f) {
            float a = (lifeK - 0.6f) / 0.4f;
            Gx.ringAt(X(t.x), Y(t.y), S(r + 2.5f), Math.max(1, S(0.8f)), Gx.withAlpha(Style.danger, 0.6f * a));
        }
    }

    private void hud() {
        float prog = Math.min(1, time / DURATION);
        float left = Math.max(0, DURATION - time);
        // top bar background
        box(0, 0, W, TOP - 4, 12, Style.light ? 0x0F000000 : 0x40000000);
        box(0, 12, W, TOP - 16, 0, Style.light ? 0x0F000000 : 0x40000000);
        box(0, TOP - 4, W, 0.7f, 0, Style.stroke);
        // time bar
        int barCol = left < 5 ? Gx.mix(Style.danger, 0xFFFFFFFF, (float) (0.5 + 0.5 * Math.sin(time * 12)) * 0.3f) : Style.accent;
        box(0, TOP - 5.5f, W, 3, 0, Style.light ? 0x14000000 : 0x14FFFFFF);
        box(0, TOP - 5.5f, W * (1 - prog), 3, 0, barCol);
        Gx.glow(X(W * (1 - prog)), Y(TOP - 4), S(10), Gx.withAlpha(barCol, 0.5f));

        float cy = (TOP - 5) / 2f;
        // timer (center)
        String tt = String.format(Locale.ROOT, "%.1f s", left);
        Gx.icon("clock", X(W / 2 - 24), Y(cy), S(9), left < 5 ? Style.danger : Style.sub);
        Gx.textMid(tt, X(W / 2 - 17), Y(cy), S(10), Fonts.BOLD, left < 5 ? Style.danger : Style.text);
        // hits (left)
        stat(10, cy, "target", "Treffer", String.valueOf(hits));
        stat(84, cy, "crosshair", "Genauigkeit", accuracy() + "%");
        // combo (right)
        float pop = 1 + 0.25f * Ease.outCubic(comboPop);
        String cs = "x" + combo;
        float cw = Gx.width(cs, S(10), Fonts.BOLD) / u;
        float rx = W - 10;
        Gx.push();
        Gx.scaleAt(X(rx - cw / 2), Y(cy), pop);
        Gx.textRight(cs, X(rx), Y(cy), S(10), Fonts.BOLD, combo >= 5 ? 0xFFFFC53D : combo > 0 ? Style.accent : Style.muted);
        Gx.pop();
        Gx.textRight("COMBO", X(rx - cw - 5), Y(cy), S(6), Fonts.BOLD, Style.muted);
        if (combo >= 5) Gx.icon("flame", X(rx - cw - 40), Y(cy), S(9), 0xFFFFA53D);
    }

    private void stat(float x, float cy, String icon, String label, String value) {
        Gx.icon(icon, X(x + 5), Y(cy), S(9), Style.sub);
        Gx.textMid(label, X(x + 13), Y(cy - 4.5f), S(5.4f), Fonts.BOLD, Style.muted);
        Gx.textMid(value, X(x + 13), Y(cy + 4.2f), S(8.5f), Fonts.BOLD, Style.text);
    }

    private void crosshair() {
        float gx = (mx - bx) / u, gy = (my - by) / u;
        if (state != State.PLAYING || gx < 0 || gy < TOP || gx > W || gy > H) return;
        int x = Math.round(mx), y = Math.round(my);
        int len = S(4.5f), gap = S(2.2f), th = Math.max(1, S(0.9f));
        int sh = 0x88000000, c = 0xFFFFFFFF;
        for (int pass = 0; pass < 2; pass++) {
            int o = pass == 0 ? Math.max(1, S(0.5f)) : 0, col = pass == 0 ? sh : c;
            int t2 = th + (pass == 0 ? 2 * o : 0);
            Gx.fill(x - gap - len - o, y - t2 / 2, len + 2 * o, t2, col);
            Gx.fill(x + gap - o, y - t2 / 2, len + 2 * o, t2, col);
            Gx.fill(x - t2 / 2, y - gap - len - o, t2, len + 2 * o, col);
            Gx.fill(x - t2 / 2, y + gap - o, t2, len + 2 * o, col);
        }
        Gx.circle(x, y, Math.max(1, S(0.8f)), Style.accent);
    }
}
