package dev.lego.games;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Spiele-Paket 4f: Sport, Reaktion, Gedächtnis. */
final class PackSkill {
    private PackSkill() { }
}

// ====================================================================== Zeitgefühl
final class TimeSense extends GameView {
    private float target, start = -1, result = -1;
    private int round;
    private final List<Float> errors = new ArrayList<>();

    TimeSense() { super("timesense"); }
    @Override protected float boardW() { return 380; }
    @Override protected float boardH() { return 230; }
    @Override protected String scoreLabel() { return "Punkte"; }
    @Override protected String overTitle() { return "5 Runden geschafft"; }

    @Override protected void reset() { round = 0; errors.clear(); next(); }
    private void next() { target = 3 + rnd.nextInt(8); start = -1; result = -1; }

    private void press() {
        if (result >= 0) { if (round >= 5) gameOver(); else next(); return; }
        if (start < 0) { start = time; Sound.play("minecraft:block.note_block.pling", 1f, 0.4f); return; }
        result = time - start;
        float err = Math.abs(result - target);
        errors.add(err);
        score += Math.max(0, Math.round(100 - err * 100));
        round++;
        Sound.play(err < 0.1f ? "minecraft:entity.player.levelup" : "minecraft:block.note_block.bell", 1.2f, 0.4f);
    }

    @Override protected void onClick(float x, float y, int b) { press(); }
    @Override protected void onKey(int k) { if (k == K_SPACE || k == K_ENTER) press(); }
    @Override protected void update(float dt) { }

    @Override protected void render() {
        text("Runde " + Math.min(5, round + (result >= 0 ? 0 : 1)) + " / 5", 190, 20, 8, Fonts.BOLD, Style.sub);
        text("Stoppe nach genau", 190, 55, 9, Fonts.MEDIUM, Style.text);
        text(String.format(Locale.ROOT, "%.0f Sekunden", target), 190, 85, 20, Fonts.BOLD, Style.accent);
        if (start < 0) text("Klick = Start", 190, 140, 10, Fonts.BOLD, Style.ok);
        else if (result < 0) { text("Läuft... (ohne Uhr!)", 190, 140, 10, Fonts.BOLD, Style.sub); float p = (float) Math.sin(time * 3) * 0.5f + 0.5f; circle(190, 175, 8 + p * 3, Gx.withAlpha(Style.accent, 0.5f)); }
        else {
            float err = result - target;
            text(String.format(Locale.ROOT, "%.2f s  (%s%.2f)", result, err >= 0 ? "+" : "", err), 190, 140, 14, Fonts.BOLD, Math.abs(err) < 0.15f ? Style.ok : Style.text);
            text("Klick = weiter", 190, 175, 7, Fonts.MEDIUM, Style.muted);
        }
    }
}

// ====================================================================== Elfmeter
final class Penalty extends GameView {
    private static final float W = 380, H = 260, GX = 90, GY = 50, GW = 200, GH = 90;
    private float aimX, aimY, kx, ky, kt = -1, keeperX, keeperTarget, power;
    private boolean charging;
    private int shots, goals;
    private String msg = "";

    Penalty() { super("penalty"); }
    @Override protected boolean hasAi() { return true; }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Tore"; }
    @Override protected String overTitle() { return goals + " von 10 Elfmetern verwandelt"; }

    @Override protected void reset() { shots = 0; goals = 0; kt = -1; keeperX = GX + GW / 2; msg = ""; }

    @Override protected void onClick(float x, float y, int b) { if (kt < 0) { charging = true; power = 0; } }
    @Override protected void onRelease(float x, float y, int b) {
        if (!charging) return;
        charging = false;
        float wob = (1 - Math.min(1, power)) * 0 + Math.max(0, power - 0.8f) * 120;
        kx = aimX + (rnd.nextFloat() - 0.5f) * wob; ky = aimY - Math.max(0, power - 0.8f) * 140;
        kt = 0; shots++;
        float skill = new float[]{0.25f, 0.45f, 0.65f, 0.82f}[aiLevel()];
        keeperTarget = rnd.nextFloat() < skill ? kx : GX + 20 + rnd.nextFloat() * (GW - 40);
        Sound.play("minecraft:entity.player.attack.strong", 1f, 0.5f);
    }

    @Override protected void update(float dt) {
        aimX = Math.max(GX - 30, Math.min(GX + GW + 30, bmx())); aimY = Math.max(GY - 30, Math.min(GY + GH, bmy()));
        if (charging) power = Math.min(1.2f, power + dt * 0.9f);
        if (kt < 0) return;
        kt += dt;
        keeperX += (keeperTarget - keeperX) * Math.min(1, dt * 4.5f);
        if (kt >= 0.5f && msg.isEmpty()) {
            boolean inGoal = kx > GX && kx < GX + GW && ky > GY && ky < GY + GH;
            boolean saved = inGoal && Math.abs(keeperX - kx) < 26;
            if (!inGoal) { msg = "Daneben!"; PackPuzzle.bad(); }
            else if (saved) { msg = "Gehalten!"; PackPuzzle.bad(); }
            else { msg = "TOR!"; goals++; score = goals; Sound.play("minecraft:entity.player.levelup", 1.3f, 0.4f); }
        }
        if (kt > 1.5f) { kt = -1; msg = ""; keeperX = GX + GW / 2; if (shots >= 10) gameOver(); }
    }

    @Override protected void render() {
        boxV(0, 0, W, H, 0, 0xFF2E9E4A, 0xFF1E7A3A);
        for (int i = 0; i < 8; i++) box(0, i * 34, W, 17, 0, 0x0EFFFFFF);
        for (int i = 0; i <= 10; i++) line(GX + i * GW / 10, GY, GX + i * GW / 10, GY + GH, 0.5f, 0x55FFFFFF);
        for (int i = 0; i <= 5; i++) line(GX, GY + i * GH / 5, GX + GW, GY + i * GH / 5, 0.5f, 0x55FFFFFF);
        line(GX, GY, GX + GW, GY, 3, 0xFFF4F4F4); line(GX, GY, GX, GY + GH, 3, 0xFFF4F4F4); line(GX + GW, GY, GX + GW, GY + GH, 3, 0xFFF4F4F4);
        box(keeperX - 9, GY + GH - 40, 18, 40, 5, 0xFFFFCD03);
        circle(keeperX, GY + GH - 46, 7, 0xFFFFD0A0);
        line(keeperX - 9, GY + GH - 34, keeperX - 22, GY + GH - 46, 3, 0xFFFFCD03); line(keeperX + 9, GY + GH - 34, keeperX + 22, GY + GH - 46, 3, 0xFFFFCD03);
        float bx0 = W / 2, by0 = 225;
        if (kt >= 0) { float t = Math.min(1, kt / 0.5f); circle(bx0 + (kx - bx0) * t, by0 + (ky - by0) * t, 7 - t * 2, 0xFFFFFFFF); }
        else { circle(bx0, by0, 7, 0xFFFFFFFF); ring(aimX, aimY, 7, 1.2f, 0xFFFF3B4C); }
        if (charging) { box(20, 230, 100, 8, 4, 0x55000000); box(20, 230, 100 * Math.min(1, power / 1.2f), 8, 4, power > 0.8f ? 0xFFFF3B4C : 0xFF39D353); text("zu fest = ungenau", 70, 220, 5.5f, Fonts.MEDIUM, 0xCCFFFFFF); }
        if (!msg.isEmpty()) text(msg, W / 2, 160, 16, Fonts.BOLD, msg.startsWith("TOR") ? 0xFFFFE14D : 0xFFFFFFFF);
        textL("Schuss " + Math.min(10, shots + (kt < 0 ? 1 : 0)) + "/10 · Tore " + goals, 8, 12, 7.5f, Fonts.BOLD, 0xFFFFFFFF);
    }
}

// ====================================================================== Bowling
final class Bowling extends GameView {
    private static final float W = 220, H = 320;
    private final float[][] pins = new float[10][];   // x y down(0/1) vx vy
    private float bx2, by2, bvx, bvy, aimX = W / 2, curve, rollT = -1;
    private int frame, roll, frameScore;
    private boolean drag;
    private float dsx, dsy;
    private String msg = "";

    Bowling() { super("bowling"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Kegel"; }
    @Override protected String overTitle() { return "10 Durchgänge: " + score + " Kegel"; }

    @Override protected void reset() { frame = 1; roll = 0; setPins(); ball(); }

    private void setPins() { int k = 0; for (int r = 0; r < 4; r++) for (int i = 0; i <= r; i++) pins[k++] = new float[]{W / 2 + (i - r / 2f) * 20, 60 - r * 17, 0, 0, 0}; }
    private void ball() { bx2 = W / 2; by2 = H - 30; bvx = bvy = 0; rollT = -1; curve = 0; }

    @Override protected void onClick(float x, float y, int b) { if (rollT < 0) { drag = true; dsx = x; dsy = y; } }
    @Override protected void onRelease(float x, float y, int b) {
        if (!drag) return;
        drag = false;
        float dy = dsy - y; if (dy < 10) return;
        bvy = -Math.min(420, 120 + dy * 3); bvx = (x - dsx) * 0.6f; curve = (x - dsx) * -0.8f;
        rollT = 0; Sound.play("minecraft:block.stone.place", 0.6f, 0.5f);
    }

    @Override protected void onKey(int k) { if (rollT < 0 && (k == K_LEFT || k == K_A)) bx2 = Math.max(30, bx2 - 6); if (rollT < 0 && (k == K_RIGHT || k == K_D)) bx2 = Math.min(W - 30, bx2 + 6); }

    @Override protected void update(float dt) {
        for (float[] p : pins) if (p[2] == 1) { p[0] += p[3] * dt; p[1] += p[4] * dt; p[3] *= 0.95f; p[4] *= 0.95f; }
        if (rollT < 0) return;
        rollT += dt;
        bvx += curve * dt; bx2 += bvx * dt; by2 += bvy * dt;
        if (bx2 < 22 || bx2 > W - 22) { bvx = 0; bx2 = Math.max(16, Math.min(W - 16, bx2)); }
        for (float[] p : pins) {
            if (Math.hypot(p[0] - bx2, p[1] - by2) < 13 && p[2] == 0) { p[2] = 1; p[3] = (p[0] - bx2) * 8 + bvx * 0.3f; p[4] = bvy * 0.5f; Sound.play("minecraft:block.wood.break", 1.3f, 0.4f); }
            if (p[2] == 1) for (float[] q : pins) if (q[2] == 0 && Math.hypot(p[0] - q[0], p[1] - q[1]) < 10 && Math.hypot(p[3], p[4]) > 40) { q[2] = 1; q[3] = p[3] * 0.7f + (q[0] - p[0]) * 4; q[4] = p[4] * 0.7f; }
        }
        if (by2 < -10 || rollT > 3) {
            int down = 0; for (float[] p : pins) if (p[2] == 1) down++;
            int got = down - frameScore;
            score += got; frameScore = down;
            msg = down == 10 && roll == 0 ? "STRIKE!" : down == 10 ? "SPARE!" : got + " Kegel";
            if (down == 10) Sound.play("minecraft:entity.player.levelup", 1.3f, 0.4f);
            roll++;
            if (roll >= 2 || down == 10) { frame++; roll = 0; frameScore = 0; if (frame > 10) { gameOver(); return; } setPins(); }
            else for (float[] p : pins) if (p[2] == 1) { p[0] = -100; p[3] = p[4] = 0; }
            ball();
        }
    }

    @Override protected void render() {
        box(0, 0, W, H, 0, 0xFF3A2414);
        boxV(20, 0, W - 40, H, 0, 0xFFE8C08A, 0xFFD8A868);
        for (int i = 0; i < 7; i++) line(40 + i * 23, 0, 40 + i * 23, H, 0.4f, 0x22000000);
        for (int i = 0; i < 7; i++) { float ax = 40 + i * 23; text("▲", ax, 240, 6, Fonts.BOLD, 0x55E3000B); }
        for (float[] p : pins) if (p[0] > -50) { if (p[2] == 0) { circle(p[0], p[1], 6, 0xFFF4F4F4); box(p[0] - 5, p[1] - 2, 10, 3, 1, 0xFFE3000B); } else circle(p[0], p[1], 5, 0x88F4F4F4); }
        circle(bx2, by2, 9, 0xFF1A2A6A); circle(bx2 - 2, by2 - 3, 1.5f, 0xFF0A0A20); circle(bx2 + 2, by2 - 3, 1.5f, 0xFF0A0A20);
        if (drag) line(bx2, by2, bx2 + (bmx() - dsx), by2 - (dsy - bmy()), 1.2f, 0x88FFFFFF);
        textL("Frame " + Math.min(10, frame) + "/10 · Wurf " + (roll + 1), 24, 12, 6.8f, Fonts.BOLD, 0xFF3A2414);
        if (!msg.isEmpty() && rollT < 0) text(msg, W / 2, 150, 12, Fonts.BOLD, 0xFF3A2414);
        if (rollT < 0) text("A/D verschieben · nach oben ziehen", W / 2, H - 8, 5.6f, Fonts.MEDIUM, 0xFF3A2414);
    }
}

// ====================================================================== Billard
final class Billiards extends GameView {
    private static final float W = 400, H = 230, R = 7, M = 18;
    private final List<float[]> balls = new ArrayList<>();   // x y vx vy color(-1 cue) potted
    private final float[][] pockets = {{M, M}, {W / 2, M - 3}, {W - M, M}, {M, H - M}, {W / 2, H - M + 3}, {W - M, H - M}};
    private boolean aiming;
    private int shotsLeft;

    Billiards() { super("billiards"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Kugeln"; }
    @Override protected String overTitle() { return potted() == 9 ? "Alle Kugeln versenkt!" : "Keine Stöße mehr"; }

    @Override protected void reset() {
        balls.clear();
        balls.add(new float[]{100, H / 2, 0, 0, -1, 0});
        int k = 0;
        for (int r = 0; r < 4 && k < 9; r++) for (int i = 0; i <= r && k < 9; i++) balls.add(new float[]{270 + r * R * 1.8f, H / 2 + (i - r / 2f) * R * 2.05f, 0, 0, k++ % 6, 0});
        balls.add(new float[]{270 + 4 * R * 1.8f, H / 2, 0, 0, 2, 0});
        shotsLeft = 15;
    }

    private int potted() { int n = 0; for (float[] b : balls) if (b[4] >= 0 && b[5] == 1) n++; return n; }
    private boolean moving() { for (float[] b : balls) if (b[5] == 0 && Math.hypot(b[2], b[3]) > 3) return true; return false; }

    @Override protected void onClick(float x, float y, int b) { if (!moving()) aiming = true; }
    @Override protected void onRelease(float x, float y, int b) {
        if (!aiming) return;
        aiming = false;
        float[] cue = balls.get(0);
        float dx = cue[0] - x, dy = cue[1] - y, d = (float) Math.min(140, Math.hypot(dx, dy)), n = (float) Math.hypot(dx, dy);
        if (n < 5) return;
        cue[2] = dx / n * d * 5; cue[3] = dy / n * d * 5; shotsLeft--;
        Sound.play("minecraft:block.wood.hit", 1.8f, 0.4f);
    }

    @Override protected void update(float dt) {
        for (int s = 0; s < 4; s++) {
            float h = dt / 4;
            for (float[] b : balls) {
                if (b[5] == 1) continue;
                b[0] += b[2] * h; b[1] += b[3] * h;
                float f = (float) Math.pow(0.45, h); b[2] *= f; b[3] *= f;
                if (b[0] < M + R) { b[0] = M + R; b[2] = Math.abs(b[2]) * 0.8f; } if (b[0] > W - M - R) { b[0] = W - M - R; b[2] = -Math.abs(b[2]) * 0.8f; }
                if (b[1] < M + R) { b[1] = M + R; b[3] = Math.abs(b[3]) * 0.8f; } if (b[1] > H - M - R) { b[1] = H - M - R; b[3] = -Math.abs(b[3]) * 0.8f; }
                for (float[] p : pockets) if (Math.hypot(b[0] - p[0], b[1] - p[1]) < 13) {
                    if (b[4] < 0) { b[0] = 100; b[1] = H / 2; b[2] = b[3] = 0; PackPuzzle.bad(); }
                    else { b[5] = 1; score = potted(); Sound.play("minecraft:entity.item.pickup", 0.9f, 0.4f); }
                }
            }
            for (int i = 0; i < balls.size(); i++) for (int j = i + 1; j < balls.size(); j++) {
                float[] a = balls.get(i), c = balls.get(j);
                if (a[5] == 1 || c[5] == 1) continue;
                float dx = c[0] - a[0], dy = c[1] - a[1], d = (float) Math.hypot(dx, dy);
                if (d < R * 2 && d > 0) {
                    float nx = dx / d, ny = dy / d, ov = R * 2 - d;
                    a[0] -= nx * ov / 2; a[1] -= ny * ov / 2; c[0] += nx * ov / 2; c[1] += ny * ov / 2;
                    float rel = (a[2] - c[2]) * nx + (a[3] - c[3]) * ny;
                    if (rel > 0) { a[2] -= rel * nx; a[3] -= rel * ny; c[2] += rel * nx; c[3] += rel * ny; Sound.play("minecraft:block.stone.hit", 1.8f, Math.min(0.5f, rel / 400)); }
                }
            }
        }
        if (!moving()) { if (potted() == 9) { score += shotsLeft * 2L; PackPuzzle.win(); gameOver(); } else if (shotsLeft <= 0) gameOver(); }
    }

    @Override protected void render() {
        box(0, 0, W, H, 12, 0xFF6A3A1A);
        box(M - 6, M - 6, W - 2 * M + 12, H - 2 * M + 12, 8, 0xFF1E7A3A);
        for (float[] p : pockets) circle(p[0], p[1], 11, 0xFF0A0A0A);
        for (float[] b : balls) if (b[5] == 0) { circle(b[0], b[1], R, b[4] < 0 ? 0xFFF8F8F8 : PackPuzzle.PAL[(int) b[4]]); circle(b[0] - 2, b[1] - 2, 2, 0x99FFFFFF); }
        float[] cue = balls.get(0);
        if (aiming) {
            float dx = cue[0] - bmx(), dy = cue[1] - bmy(), n = (float) Math.hypot(dx, dy);
            if (n > 0) { line(cue[0], cue[1], cue[0] + dx / n * 120, cue[1] + dy / n * 120, 0.6f, 0x88FFFFFF); line(cue[0] - dx / n * (12 + Math.min(140, n) * 0.3f), cue[1] - dy / n * (12 + Math.min(140, n) * 0.3f), cue[0] - dx / n * 120, cue[1] - dy / n * 120, 3, 0xFFC8A060); }
        }
        textL("Versenkt " + potted() + "/9 · Stöße " + shotsLeft, 22, 8, 6.6f, Fonts.BOLD, 0xFFFFFFFF);
    }
}

// ====================================================================== Sternenjäger
final class StarFighter extends GameView {
    private static final float W = 300, H = 320;
    private final List<float[]> shots = new ArrayList<>(), enemies = new ArrayList<>(), eShots = new ArrayList<>(), stars = new ArrayList<>();
    private float px = W / 2, cool, spawn;
    private int hp;

    StarFighter() { super("starfighter"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }

    @Override protected void reset() { shots.clear(); enemies.clear(); eShots.clear(); stars.clear(); for (int i = 0; i < 40; i++) stars.add(new float[]{rnd.nextFloat() * W, rnd.nextFloat() * H, 20 + rnd.nextFloat() * 80}); hp = 3; spawn = 1; }

    @Override protected void update(float dt) {
        px += (Math.max(12, Math.min(W - 12, bmx())) - px) * Math.min(1, dt * 12);
        cool -= dt;
        if (cool <= 0) { cool = 0.22f; shots.add(new float[]{px, H - 40}); }
        for (float[] s : stars) { s[1] += s[2] * dt; if (s[1] > H) { s[1] = 0; s[0] = rnd.nextFloat() * W; } }
        spawn -= dt * (1 + time / 40);
        if (spawn <= 0) { spawn = 0.9f; enemies.add(new float[]{20 + rnd.nextFloat() * (W - 40), -15, 40 + rnd.nextFloat() * 40, rnd.nextFloat() * 6, rnd.nextFloat() < 0.2f ? 3 : 1}); }
        for (float[] s : shots) s[1] -= 360 * dt;
        shots.removeIf(s -> s[1] < -10);
        for (float[] e : enemies) {
            e[1] += e[2] * dt; e[0] += (float) Math.sin(time * 2 + e[3]) * 40 * dt;
            if (rnd.nextFloat() < dt * 0.5f) eShots.add(new float[]{e[0], e[1] + 8});
        }
        for (float[] s : eShots) s[1] += 170 * dt;
        eShots.removeIf(s -> s[1] > H + 10);
        for (int i = enemies.size() - 1; i >= 0; i--) {
            float[] e = enemies.get(i);
            for (int j = shots.size() - 1; j >= 0; j--) { float[] s = shots.get(j); if (Math.abs(s[0] - e[0]) < 12 && Math.abs(s[1] - e[1]) < 10) { shots.remove(j); e[4]--; if (e[4] <= 0) { score += e[2] > 60 ? 30 : 20; PackArcade.boom(); enemies.remove(i); } break; } }
        }
        for (int i = enemies.size() - 1; i >= 0; i--) { float[] e = enemies.get(i); if (e[1] > H + 20) enemies.remove(i); else if (Math.abs(e[0] - px) < 16 && Math.abs(e[1] - (H - 30)) < 14) { enemies.remove(i); hit(); } }
        for (int i = eShots.size() - 1; i >= 0; i--) { float[] s = eShots.get(i); if (Math.abs(s[0] - px) < 10 && Math.abs(s[1] - (H - 30)) < 10) { eShots.remove(i); hit(); } }
    }

    private void hit() { hp--; Sound.play("minecraft:entity.generic.hurt", 1f, 0.5f); if (hp <= 0) gameOver(); }

    @Override protected void render() {
        box(0, 0, W, H, 0, 0xFF03040C);
        for (float[] s : stars) circle(s[0], s[1], s[2] > 70 ? 1.2f : 0.7f, 0xAAFFFFFF);
        for (float[] s : shots) box(s[0] - 1.2f, s[1] - 6, 2.4f, 8, 1, 0xFF7CF7FF);
        for (float[] s : eShots) circle(s[0], s[1], 2.5f, 0xFFFF6A6A);
        for (float[] e : enemies) { box(e[0] - 11, e[1] - 7, 22, 14, 5, e[4] >= 2 ? 0xFFB15CFF : 0xFFE3000B); circle(e[0], e[1], 3.5f, 0xFFFFE14D); }
        line(px, H - 44, px - 12, H - 22, 3, 0xFFD8DEE6); line(px, H - 44, px + 12, H - 22, 3, 0xFFD8DEE6); circle(px, H - 30, 4, 0xFF3AD8FF);
        circle(px, H - 18 + (float) Math.random() * 2, 3, 0xFFFFB030);
        for (int k = 0; k < 3; k++) circle(12 + k * 12, 12, 4, k < hp ? 0xFFFF3B4C : 0x55FFFFFF);
        text("Maus = steuern · schießt automatisch", W / 2, H - 6, 5.5f, Fonts.MEDIUM, 0x88FFFFFF);
    }
}

// ====================================================================== Seerosen-Sprung
final class LilyHop extends GameView {
    private static final float W = 380, H = 240;
    private final List<float[]> pads = new ArrayList<>();   // x, y, size, phase
    private float fx, fy, jumpT = -1, charge, camX, tx, ty, sx, sy;
    private boolean charging;
    private int onPad;

    LilyHop() { super("lilyhop"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Sprünge"; }
    @Override protected String overTitle() { return "Platsch! Ins Wasser gefallen"; }

    @Override protected void reset() {
        pads.clear(); camX = 0;
        float x = 40;
        for (int i = 0; i < 6; i++) { pads.add(new float[]{x, 120 + (rnd.nextFloat() - 0.5f) * 80, 16 + rnd.nextFloat() * 8, rnd.nextFloat() * 6}); x += 60 + rnd.nextFloat() * 50; }
        onPad = 0; fx = pads.get(0)[0]; fy = pads.get(0)[1]; jumpT = -1;
    }

    @Override protected void onClick(float x, float y, int b) { if (jumpT < 0) { charging = true; charge = 0; } }
    @Override protected void onRelease(float x, float y, int b) {
        if (!charging) return;
        charging = false;
        float dist = 30 + charge * 150;
        float[] next = onPad + 1 < pads.size() ? pads.get(onPad + 1) : pads.get(onPad);
        float dx = next[0] - fx, dy = next[1] - fy, n = (float) Math.max(1, Math.hypot(dx, dy));
        sx = fx; sy = fy; tx = fx + dx / n * dist; ty = fy + dy / n * dist; jumpT = 0;
        Sound.play("minecraft:entity.slime.jump", 1.3f, 0.4f);
    }

    @Override protected void update(float dt) {
        if (charging) charge = Math.min(1, charge + dt * 0.8f);
        for (float[] p : pads) p[1] += (float) Math.sin(time * 1.5 + p[3]) * 8 * dt;
        if (jumpT >= 0) {
            jumpT += dt * 2.2f;
            float t = Math.min(1, jumpT);
            fx = sx + (tx - sx) * t; fy = sy + (ty - sy) * t;
            if (jumpT >= 1) {
                jumpT = -1;
                int hitPad = -1;
                for (int i = 0; i < pads.size(); i++) { float[] p = pads.get(i); if (Math.hypot(fx - p[0], fy - p[1]) < p[2]) hitPad = i; }
                if (hitPad < 0) { Sound.play("minecraft:entity.generic.splash", 1f, 0.5f); gameOver(); return; }
                if (hitPad > onPad) { score += hitPad - onPad; Sound.play("minecraft:entity.experience_orb.pickup", 1.3f, 0.3f); }
                onPad = hitPad;
                float[] last = pads.get(pads.size() - 1);
                while (pads.size() < onPad + 6) { pads.add(new float[]{last[0] + 60 + rnd.nextFloat() * (50 + score), 120 + (rnd.nextFloat() - 0.5f) * 100, Math.max(10, 22 - score * 0.3f) + rnd.nextFloat() * 6, rnd.nextFloat() * 6}); last = pads.get(pads.size() - 1); }
            }
        } else { float[] p = pads.get(onPad); fy = p[1]; fx = p[0]; }
        camX += (fx - 80 - camX) * Math.min(1, dt * 3);
    }

    @Override protected void render() {
        boxV(0, 0, W, H, 0, 0xFF1A6A8A, 0xFF0A3A5A);
        for (int i = 0; i < 12; i++) box(((i * 73 - camX * 0.5f) % W + W) % W, (i * 41) % H, 26, 2, 1, 0x22FFFFFF);
        for (float[] p : pads) { float x = p[0] - camX; circle(x, p[1], p[2], 0xFF3E9E4A); box(x, p[1] - 2, p[2], 4, 0, 0xFF1A6A8A); circle(x - p[2] * 0.3f, p[1] - p[2] * 0.4f, 2.5f, 0xFFFF9AC0); }
        float jh = jumpT >= 0 ? (float) Math.sin(Math.min(1, jumpT) * Math.PI) * 26 : 0;
        float x = fx - camX;
        circle(x, fy - jh + 6, 9, 0x33000000);
        circle(x, fy - jh, 9, 0xFF39D353);
        circle(x - 4, fy - jh - 6, 3, 0xFFFFFFFF); circle(x + 4, fy - jh - 6, 3, 0xFFFFFFFF);
        circle(x - 4, fy - jh - 6, 1.4f, 0xFF1B1B22); circle(x + 4, fy - jh - 6, 1.4f, 0xFF1B1B22);
        if (charging) { box(20, H - 20, 100, 8, 4, 0x55000000); box(20, H - 20, 100 * charge, 8, 4, 0xFFFFCD03); }
        text("Halten = Sprungweite", W / 2, H - 14, 6, Fonts.MEDIUM, 0xAAFFFFFF);
    }
}

// ====================================================================== Kopfball (Keepy Uppy)
final class KeepyUppy extends GameView {
    private static final float W = 300, H = 300, BR = 14;
    private float bx2, by2, vx, vy, msgT;

    KeepyUppy() { super("keepyuppy"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Kicks"; }

    @Override protected void reset() { bx2 = W / 2; by2 = 80; vx = 0; vy = 0; }

    @Override protected void onClick(float x, float y, int b) {
        if (Math.hypot(x - bx2, y - by2) < BR + 8) {
            vy = -320 - Math.min(140, score * 4); vx = (bx2 - x) * 14 + (rnd.nextFloat() - 0.5f) * 40;
            score++; msgT = 0.3f;
            Sound.play("minecraft:block.wool.hit", 1.4f, 0.6f);
        }
    }

    @Override protected void update(float dt) {
        msgT = Math.max(0, msgT - dt);
        vy += (500 + Math.min(300, score * 6)) * dt;
        bx2 += vx * dt; by2 += vy * dt;
        if (bx2 < BR) { bx2 = BR; vx = Math.abs(vx) * 0.8f; } if (bx2 > W - BR) { bx2 = W - BR; vx = -Math.abs(vx) * 0.8f; }
        if (by2 < BR) { by2 = BR; vy = Math.abs(vy) * 0.5f; }
        if (by2 > H - BR) gameOver();
    }

    @Override protected void render() {
        boxV(0, 0, W, H, 0, 0xFF8AC8FF, 0xFFCFEFFF);
        box(0, H - 20, W, 20, 0, 0xFF3E9E4A);
        circle(bx2, by2, BR, 0xFFFFFFFF);
        for (int i = 0; i < 5; i++) { double a = time * 4 + i * 1.256; circle(bx2 + (float) Math.cos(a) * 7, by2 + (float) Math.sin(a) * 7, 3.5f, 0xFF1B1B22); }
        text(String.valueOf(score), W / 2, 40, 22, Fonts.BOLD, 0x881B1B22);
        if (score == 0) text("Klick auf den Ball!", W / 2, H / 2, 9, Fonts.BOLD, 0xFF1B1B22);
    }
}

// ====================================================================== Zahlen-Jagd (Schulte-Tabelle)
final class NumberHunt extends GameView {
    private static final int N = 5;
    private final int[] cells = new int[N * N];
    private int next;
    private float flash = -1;
    private int flashCell;

    NumberHunt() { super("numberhunt"); }
    @Override protected float boardW() { return 300; }
    @Override protected float boardH() { return 300; }
    @Override protected String scoreLabel() { return "Zeit"; }
    @Override protected String scoreText() { return String.format(Locale.ROOT, "%.1f s", time); }
    @Override protected String bestText(long best) { return String.format(Locale.ROOT, "%.1f s", best / 10.0); }
    @Override protected String overTitle() { return "Fertig in " + String.format(Locale.ROOT, "%.1f", time) + " s"; }

    @Override protected void reset() { List<Integer> l = new ArrayList<>(); for (int i = 1; i <= N * N; i++) l.add(i); java.util.Collections.shuffle(l, rnd); for (int i = 0; i < l.size(); i++) cells[i] = l.get(i); next = 1; }

    @Override protected void onClick(float x, float y, int b) {
        int c = (int) ((x - 10) / 56), r = (int) ((y - 10) / 56);
        if (c < 0 || r < 0 || c >= N || r >= N) return;
        int i = r * N + c;
        if (cells[i] == next) { next++; PackPuzzle.click(); if (next > N * N) { score = (long) (time * 10); PackPuzzle.win(); gameOver(); } }
        else { flash = 0.3f; flashCell = i; PackPuzzle.bad(); }
    }

    @Override protected void update(float dt) { if (flash >= 0) flash -= dt; }

    @Override protected void render() {
        for (int i = 0; i < N * N; i++) {
            float x = 10 + (i % N) * 56, y = 10 + (i / N) * 56;
            boolean done = cells[i] < next;
            box(x + 2, y + 2, 52, 52, 8, flash > 0 && i == flashCell ? 0xFFB0202A : done ? Gx.withAlpha(Style.ok, 0.35f) : Style.surface);
            text(String.valueOf(cells[i]), x + 28, y + 29, 16, Fonts.BOLD, done ? Style.muted : Style.text);
        }
        text("Klick 1 bis 25 der Reihe nach · nächste: " + Math.min(25, next), 150, 296, 6.4f, Fonts.BOLD, Style.sub);
    }
}

// ====================================================================== Muster-Gedächtnis
final class PatternMemory extends GameView {
    private int n = 4, lives;
    private boolean[] pattern, clicked;
    private float show;
    private int level, found;

    PatternMemory() { super("patternmemory"); }
    @Override protected float boardW() { return 300; }
    @Override protected float boardH() { return 310; }
    @Override protected String scoreLabel() { return "Level"; }

    @Override protected void reset() { level = 0; lives = 3; n = 3; next(); }

    private void next() {
        level++; score = level - 1;
        if (level % 3 == 0 && n < 7) n++;
        pattern = new boolean[n * n]; clicked = new boolean[n * n];
        int k = Math.min(n * n - 2, 2 + level);
        for (int i = 0; i < k; ) { int p = rnd.nextInt(n * n); if (!pattern[p]) { pattern[p] = true; i++; } }
        show = 1.4f; found = 0;
    }

    @Override protected void onClick(float x, float y, int b) {
        if (show > 0) return;
        float cs = 280f / n;
        int c = (int) ((x - 10) / cs), r = (int) ((y - 10) / cs);
        if (c < 0 || r < 0 || c >= n || r >= n) return;
        int i = r * n + c;
        if (clicked[i]) return;
        clicked[i] = true;
        if (pattern[i]) { found++; PackPuzzle.click(); int need = 0; for (boolean p : pattern) if (p) need++; if (found == need) { PackPuzzle.win(); next(); } }
        else { lives--; PackPuzzle.bad(); if (lives <= 0) gameOver(); else { show = 1.0f; clicked = new boolean[n * n]; found = 0; } }
    }

    @Override protected void update(float dt) { show -= dt; }

    @Override protected void render() {
        float cs = 280f / n;
        for (int i = 0; i < n * n; i++) {
            float x = 10 + (i % n) * cs, y = 10 + (i / n) * cs;
            boolean lit = show > 0 ? pattern[i] : clicked[i];
            int col = !lit ? Style.surface : pattern[i] ? 0xFF3FA9FF : 0xFFB0202A;
            box(x + 2, y + 2, cs - 4, cs - 4, 6, col);
        }
        text("Level " + level + " · Leben " + lives + (show > 0 ? " · Merken!" : " · Klick die Felder"), 150, 302, 6.6f, Fonts.BOLD, Style.sub);
    }
}

// ====================================================================== Autobahn
final class Highway extends GameView {
    private static final float W = 260, H = 320;
    private final List<float[]> cars = new ArrayList<>();   // lane, y, speed, color
    private float lane = 1, laneT = 1, roadY, spawn, speed;

    Highway() { super("highway"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Meter"; }

    @Override protected void reset() { cars.clear(); lane = laneT = 1; spawn = 0; speed = 200; }

    @Override protected void onKey(int k) { if ((k == K_LEFT || k == K_A) && laneT > 0) laneT--; if ((k == K_RIGHT || k == K_D) && laneT < 3) laneT++; }
    @Override protected void onClick(float x, float y, int b) { if (x < W / 2 && laneT > 0) laneT--; else if (x >= W / 2 && laneT < 3) laneT++; }

    private float lx(float l) { return 40 + l * 60; }

    @Override protected void update(float dt) {
        speed += dt * 6;
        score = (long) (time * speed / 20);
        lane += (laneT - lane) * Math.min(1, dt * 12);
        roadY = (roadY + speed * dt) % 40;
        spawn -= dt * speed / 200;
        if (spawn <= 0) { spawn = 0.55f; cars.add(new float[]{rnd.nextInt(4), -40, 60 + rnd.nextFloat() * 80, rnd.nextInt(6)}); }
        for (float[] c : cars) c[1] += (speed - c[2]) * dt;
        cars.removeIf(c -> c[1] > H + 50);
        for (float[] c : cars) if (Math.abs(lx(c[0]) - lx(lane)) < 30 && Math.abs(c[1] - (H - 60)) < 40) { PackArcade.boom(); gameOver(); return; }
    }

    private void car(float x, float y, int col) { box(x - 14, y - 22, 28, 44, 7, col); box(x - 10, y - 12, 20, 12, 3, 0xCC9FD8FF); box(x - 12, y + 12, 24, 6, 2, 0x55000000); }

    @Override protected void render() {
        box(0, 0, W, H, 0, 0xFF33363E);
        box(0, 0, 10, H, 0, 0xFF3E9E4A); box(W - 10, 0, 10, H, 0, 0xFF3E9E4A);
        for (int l = 1; l < 4; l++) for (float y = -40 + roadY; y < H; y += 40) box(lx(l) - 30 - 1.5f, y, 3, 20, 0, 0xCCF4F4F4);
        for (float[] c : cars) car(lx(c[0]), c[1], PackPuzzle.PAL[(int) c[3]]);
        car(lx(lane), H - 60, 0xFFF4F4F4);
        text(score + " m", W / 2, 14, 9, Fonts.BOLD, 0xFFFFFFFF);
    }
}

// ====================================================================== Goldgräber
final class GoldMiner extends GameView {
    private static final float W = 400, H = 280, OX = 200, OY = 40;
    private final List<float[]> items = new ArrayList<>();   // x y r value weight kind
    private float ang, len, hookState, grabbed = -1, left;
    private int dir = 1, level, goal;

    GoldMiner() { super("goldminer"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String overTitle() { return score >= goal ? "Ziel geschafft!" : "Ziel verfehlt (" + goal + ")"; }

    @Override protected void reset() { level = 0; score = 0; nextLevel(); }

    private void nextLevel() {
        level++; goal = (int) score + 300 + level * 150; left = 45; items.clear();
        for (int i = 0; i < 10 + level; i++) {
            float r = rnd.nextFloat(), x = 30 + rnd.nextFloat() * (W - 60), y = 110 + rnd.nextFloat() * (H - 130);
            if (r < 0.15f) items.add(new float[]{x, y, 22, 500, 5, 0});
            else if (r < 0.5f) items.add(new float[]{x, y, 9, 100, 1.5f, 0});
            else if (r < 0.62f) items.add(new float[]{x, y, 6, 400, 0.6f, 2});
            else items.add(new float[]{x, y, 14 + rnd.nextFloat() * 6, 20, 3.5f, 1});
        }
        len = 20; hookState = 0; grabbed = -1;
    }

    private void fire() { if (hookState == 0) { hookState = 1; Sound.play("minecraft:block.chain.place", 1.2f, 0.4f); } }
    @Override protected void onClick(float x, float y, int b) { fire(); }
    @Override protected void onKey(int k) { if (k == K_SPACE || k == K_DOWN || k == K_S) fire(); }

    @Override protected void update(float dt) {
        left -= dt;
        if (left <= 0) { left = 0; if (score >= goal) { PackPuzzle.win(); nextLevel(); } else gameOver(); return; }
        if (hookState == 0) { ang += dir * 70 * dt; if (Math.abs(ang) > 70) { dir = -dir; ang = Math.signum(ang) * 70; } return; }
        float rad = (float) Math.toRadians(ang);
        float hx = OX + (float) Math.sin(rad) * len, hy = OY + (float) Math.cos(rad) * len;
        if (hookState == 1) {
            len += 260 * dt;
            for (int i = 0; i < items.size(); i++) { float[] it = items.get(i); if (Math.hypot(hx - it[0], hy - it[1]) < it[2] + 4) { grabbed = i; hookState = 2; Sound.play("minecraft:block.stone.hit", 1f, 0.4f); break; } }
            if (hx < 0 || hx > W || hy > H) hookState = 2;
        } else {
            float w = grabbed >= 0 ? items.get((int) grabbed)[4] : 0.5f;
            len -= 260 / (1 + w) * dt;
            if (grabbed >= 0) { float[] it = items.get((int) grabbed); it[0] = hx; it[1] = hy + it[2]; }
            if (len <= 20) {
                len = 20; hookState = 0;
                if (grabbed >= 0) { float[] it = items.remove((int) grabbed); score += (long) it[3]; Sound.play(it[3] >= 400 ? "minecraft:entity.player.levelup" : "minecraft:entity.experience_orb.pickup", 1.2f, 0.4f); grabbed = -1; }
            }
        }
    }

    @Override protected void render() {
        boxV(0, 0, W, 50, 0, 0xFFFFD08A, 0xFFFFB060);
        boxV(0, 50, W, H - 50, 0, 0xFF8A5A30, 0xFF4A2A14);
        for (float[] it : items) {
            int col = it[5] == 0 ? 0xFFFFD23F : it[5] == 2 ? 0xFF5AF0E8 : 0xFF8A8A92;
            if (it[5] == 2) { circle(it[0], it[1], it[2], col); circle(it[0] - 2, it[1] - 2, 2, 0xFFFFFFFF); }
            else { circle(it[0], it[1], it[2], col); circle(it[0] - it[2] * 0.3f, it[1] - it[2] * 0.3f, it[2] * 0.3f, it[5] == 0 ? 0xFFFFF0A0 : 0xFFB0B0B8); }
        }
        float rad = (float) Math.toRadians(ang), hx = OX + (float) Math.sin(rad) * len, hy = OY + (float) Math.cos(rad) * len;
        line(OX, OY, hx, hy, 1, 0xFF1B1B22);
        line(hx - 6, hy, hx + 6, hy, 2, 0xFF5A6270); line(hx - 6, hy, hx - 6, hy + 6, 2, 0xFF5A6270); line(hx + 6, hy, hx + 6, hy + 6, 2, 0xFF5A6270);
        box(OX - 12, OY - 24, 24, 20, 5, 0xFFE3000B); circle(OX, OY - 30, 7, 0xFFFFD0A0);
        textL("Geld " + score + " / Ziel " + goal, 8, 12, 7.5f, Fonts.BOLD, 0xFF3A2414);
        textL(String.format(Locale.ROOT, "Level %d · %.0f s", level, left), 300, 12, 7.5f, Fonts.BOLD, 0xFF3A2414);
    }
}

// ====================================================================== Eisrutsche
final class IceSlide extends GameView {
    private static final int N = 9;
    private static final float C = 26, OX = 10, OY = 10;
    private boolean[][] rock;
    private int pr, pc, gr, gc, moves, level, par;
    private float doneT = -1;

    IceSlide() { super("iceslide"); }
    @Override protected float boardW() { return N * C + 20 + 120; }
    @Override protected float boardH() { return N * C + 20; }
    @Override protected String scoreLabel() { return "Level"; }
    @Override protected String overTitle() { return "Aufgegeben"; }

    @Override protected void reset() { level = 0; next(); }

    private void next() {
        level++; score = level - 1;
        while (true) {
            rock = new boolean[N][N];
            for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) if (rnd.nextFloat() < 0.16f) rock[r][c] = true;
            pr = rnd.nextInt(N); pc = rnd.nextInt(N); gr = rnd.nextInt(N); gc = rnd.nextInt(N);
            if (rock[pr][pc] || rock[gr][gc] || (pr == gr && pc == gc)) continue;
            int d = bfs();
            if (d >= 3 + Math.min(4, level / 2)) { par = d; break; }
        }
        moves = 0; doneT = -1;
    }

    private int[] slide(int r, int c, int dr, int dc) {
        while (true) {
            int nr = r + dr, nc = c + dc;
            if (nr < 0 || nc < 0 || nr >= N || nc >= N || rock[nr][nc]) return new int[]{r, c};
            r = nr; c = nc;
            if (r == gr && c == gc) return new int[]{r, c};
        }
    }

    private int bfs() {
        int[][] dist = new int[N][N];
        for (int[] d : dist) java.util.Arrays.fill(d, -1);
        java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
        q.add(new int[]{pr, pc}); dist[pr][pc] = 0;
        while (!q.isEmpty()) {
            int[] p = q.poll();
            if (p[0] == gr && p[1] == gc) return dist[p[0]][p[1]];
            for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) { int[] e = slide(p[0], p[1], d[0], d[1]); if (dist[e[0]][e[1]] < 0) { dist[e[0]][e[1]] = dist[p[0]][p[1]] + 1; q.add(e); } }
        }
        return -1;
    }

    @Override protected void onKey(int k) {
        if (doneT >= 0) return;
        int[] d = k == K_UP || k == K_W ? new int[]{-1, 0} : k == K_DOWN || k == K_S ? new int[]{1, 0} : k == K_LEFT || k == K_A ? new int[]{0, -1} : k == K_RIGHT || k == K_D ? new int[]{0, 1} : null;
        if (k == K_R) { next(); level--; score = level - 1; return; }
        if (d == null) return;
        int[] e = slide(pr, pc, d[0], d[1]);
        if (e[0] == pr && e[1] == pc) return;
        pr = e[0]; pc = e[1]; moves++;
        Sound.play("minecraft:block.glass.step", 1.4f, 0.3f);
        if (pr == gr && pc == gc) { doneT = 0; PackPuzzle.win(); }
    }

    @Override protected void onClick(float x, float y, int b) { }
    @Override protected void update(float dt) { if (doneT >= 0 && (doneT += dt) > 0.9f) next(); }

    @Override protected void render() {
        for (int r = 0; r < N; r++) for (int c = 0; c < N; c++) {
            float x = OX + c * C, y = OY + r * C;
            box(x, y, C, C, 0, ((r + c) % 2 == 0) ? 0xFFCFEFFF : 0xFFBFE6FA);
            if (rock[r][c]) { circle(x + C / 2, y + C / 2, C * 0.42f, 0xFF7A7A84); circle(x + C / 2 - 3, y + C / 2 - 3, 3, 0xFF9A9AA4); }
        }
        float gx = OX + gc * C + C / 2, gy = OY + gr * C + C / 2;
        box(gx - 8, gy - 8, 16, 16, 3, 0xFF39D353);
        text("⌂", gx, gy, 9, Fonts.BOLD, 0xFFFFFFFF);
        float x = OX + pc * C + C / 2, y = OY + pr * C + C / 2;
        circle(x, y, 9, 0xFF1B1B22); circle(x, y + 2, 6, 0xFFF4F4F4); box(x - 2, y - 2, 4, 3, 1, 0xFFFF8A00);
        float sx = N * C + 30;
        textL("Level " + level, sx, 30, 9, Fonts.BOLD, Style.text);
        textL("Züge " + moves + " (min. " + par + ")", sx, 50, 7, Fonts.MEDIUM, Style.sub);
        textL("Der Pinguin rutscht,", sx, 90, 6.4f, Fonts.MEDIUM, Style.sub);
        textL("bis er gegen einen", sx, 102, 6.4f, Fonts.MEDIUM, Style.sub);
        textL("Stein stößt.", sx, 114, 6.4f, Fonts.MEDIUM, Style.sub);
        textL("Pfeiltasten · R = neu", sx, 140, 6.4f, Fonts.MEDIUM, Style.muted);
    }
}
