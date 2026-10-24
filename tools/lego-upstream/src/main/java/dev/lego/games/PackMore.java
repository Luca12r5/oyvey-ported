package dev.lego.games;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Spiele-Paket 4d: Solitär, Physik, Duelle, Klicker. */
final class PackMore {
    private PackMore() { }
}

// ====================================================================== Solitär (Klondike)
final class Klondike extends GameView {
    private static final String[] R = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "B", "D", "K"};
    private static final float CW = 34, CH = 48, GAP = 50, TOPY = 8, TABY = 66;
    /** card = suit * 13 + rank; face-up flag kept separately. */
    private final List<List<int[]>> tab = new ArrayList<>();   // {card, up}
    private final List<Integer> stock = new ArrayList<>(), waste = new ArrayList<>();
    private final int[] found = new int[4];                    // highest rank+1 per suit (0 = empty)
    private int selPile = -2, selIdx = -1;                      // -1 = waste, 0..6 tableau

    Klondike() { super("klondike"); }
    @Override protected float boardW() { return 7 * GAP + 6; }
    @Override protected float boardH() { return 300; }
    @Override protected String scoreLabel() { return "Karten"; }
    @Override protected String overTitle() { return score >= 52 ? "Gewonnen!" : "Aufgegeben"; }

    @Override protected void reset() {
        List<Integer> d = new ArrayList<>();
        for (int i = 0; i < 52; i++) d.add(i);
        Collections.shuffle(d, rnd);
        tab.clear(); stock.clear(); waste.clear();
        java.util.Arrays.fill(found, 0);
        for (int p = 0; p < 7; p++) { List<int[]> pile = new ArrayList<>(); for (int k = 0; k <= p; k++) pile.add(new int[]{d.remove(0), k == p ? 1 : 0}); tab.add(pile); }
        stock.addAll(d);
        selPile = -2; score = 0;
    }

    private static int suit(int c) { return c / 13; }
    private static int rank(int c) { return c % 13; }
    private static boolean red(int c) { return suit(c) >= 2; }

    private void drawCard(float x, float y, int c, boolean up, boolean sel) {
        if (!up) { box(x, y, CW, CH, 5, 0xFF1A4AA0); box(x + 3, y + 3, CW - 6, CH - 6, 4, 0xFF2A6AD0); return; }
        box(x, y, CW, CH, 5, sel ? 0xFFFFF0A0 : 0xFFF8F8F8);
        Gx.outline(X(x), Y(y), S(CW), S(CH), S(5), 1, 0x33000000);
        int col = red(c) ? 0xFFD01020 : 0xFF15161A;
        text(R[rank(c)], x + 8, y + 8, 7.5f, Fonts.BOLD, col);
        text(PackMind.SUITS[suit(c)], x + 26, y + 8, 7, Fonts.BOLD, col);
        text(PackMind.SUITS[suit(c)], x + CW / 2, y + 31, 14, Fonts.BOLD, col);
    }

    private float px(int p) { return 4 + p * GAP; }

    private float step(int pile) { int n = tab.get(pile).size(); return n <= 1 ? 16 : Math.min(16, (300 - TABY - CH - 4) / (n - 1)); }

    private boolean canTab(int card, int pile) {
        List<int[]> t = tab.get(pile);
        if (t.isEmpty()) return rank(card) == 12;
        int[] top = t.get(t.size() - 1);
        return top[1] == 1 && red(top[0]) != red(card) && rank(top[0]) == rank(card) + 1;
    }

    private boolean toFoundation(int card) {
        if (found[suit(card)] == rank(card)) { found[suit(card)]++; score++; Sound.play("minecraft:item.book.page_turn", 1.4f, 0.5f); if (score >= 52) { PackPuzzle.win(); gameOver(); } return true; }
        return false;
    }

    private void flipTops() { for (List<int[]> t : tab) if (!t.isEmpty()) t.get(t.size() - 1)[1] = 1; }

    @Override protected void onClick(float x, float y, int b) {
        // stock
        if (in(x, y, px(0), TOPY, CW, CH)) {
            if (stock.isEmpty()) { Collections.reverse(waste); stock.addAll(waste); waste.clear(); }
            else waste.add(stock.remove(stock.size() - 1));
            selPile = -2; PackPuzzle.click(); return;
        }
        // waste
        if (in(x, y, px(1), TOPY, CW, CH) && !waste.isEmpty()) {
            int c = waste.get(waste.size() - 1);
            if (b == 1 || selPile == -1) { if (toFoundation(c)) { waste.remove(waste.size() - 1); selPile = -2; return; } }
            selPile = -1; selIdx = 0; return;
        }
        // foundations
        for (int s = 0; s < 4; s++) if (in(x, y, px(3 + s), TOPY, CW, CH) && selPile != -2) {
            int c = selPile == -1 ? waste.get(waste.size() - 1) : tab.get(selPile).get(tab.get(selPile).size() - 1)[0];
            boolean single = selPile == -1 || selIdx == tab.get(selPile).size() - 1;
            if (single && toFoundation(c)) { if (selPile == -1) waste.remove(waste.size() - 1); else tab.get(selPile).remove(tab.get(selPile).size() - 1); flipTops(); }
            selPile = -2; return;
        }
        // tableau
        for (int p = 0; p < 7; p++) {
            if (x < px(p) || x > px(p) + CW) continue;
            List<int[]> t = tab.get(p);
            int hit = -1;
            for (int i = t.size() - 1; i >= 0; i--) if (in(x, y, px(p), TABY + i * step(p), CW, i == t.size() - 1 ? CH : step(p))) { hit = i; break; }
            if (selPile != -2 && selPile != p) {
                int moving = selPile == -1 ? waste.get(waste.size() - 1) : tab.get(selPile).get(selIdx)[0];
                if (canTab(moving, p)) {
                    if (selPile == -1) t.add(new int[]{waste.remove(waste.size() - 1), 1});
                    else { List<int[]> src = tab.get(selPile); while (src.size() > selIdx) t.add(src.remove(selIdx)); }
                    flipTops(); PackPuzzle.click();
                }
                selPile = -2; return;
            }
            if (hit < 0) { selPile = -2; return; }
            if (t.get(hit)[1] == 0) return;
            if (b == 1 && hit == t.size() - 1 && toFoundation(t.get(hit)[0])) { t.remove(hit); flipTops(); selPile = -2; return; }
            selPile = p; selIdx = hit; return;
        }
        selPile = -2;
    }

    @Override protected void onKey(int k) { if (k == K_ENTER) { score = Math.min(score, 51); gameOver(); } }
    @Override protected void update(float dt) { }

    @Override protected void render() {
        box(0, 0, boardW(), boardH(), 8, 0xFF1E6B3A);
        if (stock.isEmpty()) ring(px(0) + CW / 2, TOPY + CH / 2, 10, 1.5f, 0x66FFFFFF); else drawCard(px(0), TOPY, 0, false, false);
        if (!waste.isEmpty()) drawCard(px(1), TOPY, waste.get(waste.size() - 1), true, selPile == -1);
        for (int s = 0; s < 4; s++) {
            box(px(3 + s), TOPY, CW, CH, 5, 0x33FFFFFF);
            if (found[s] > 0) drawCard(px(3 + s), TOPY, s * 13 + found[s] - 1, true, false);
            else text(PackMind.SUITS[s], px(3 + s) + CW / 2, TOPY + CH / 2, 14, Fonts.BOLD, 0x55FFFFFF);
        }
        for (int p = 0; p < 7; p++) {
            List<int[]> t = tab.get(p);
            if (t.isEmpty()) box(px(p), TABY, CW, CH, 5, 0x22FFFFFF);
            for (int i = 0; i < t.size(); i++) drawCard(px(p), TABY + i * step(p), t.get(i)[0], t.get(i)[1] == 1, selPile == p && i >= selIdx);
        }
        text("Rechtsklick = auf Ablage · Enter = aufgeben", boardW() / 2, 294, 5.8f, Fonts.MEDIUM, 0x99FFFFFF);
    }
}

// ====================================================================== Zahlengedächtnis
final class NumberMemory extends GameView {
    private String number = "", input = "";
    private int digits;
    private float show, flash;
    private boolean wrong;

    NumberMemory() { super("numbermemory"); }
    @Override protected float boardW() { return 380; }
    @Override protected float boardH() { return 220; }
    @Override protected String scoreLabel() { return "Ziffern"; }
    @Override protected String overTitle() { return "Richtig wäre: " + number; }

    @Override protected void reset() { digits = 3; next(); }

    private void next() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digits; i++) sb.append(i == 0 ? 1 + rnd.nextInt(9) : rnd.nextInt(10));
        number = sb.toString(); input = ""; show = 1.2f + digits * 0.45f; wrong = false;
    }

    @Override public boolean chr(String s) { if (state == State.PLAYING && show <= 0) for (char c : s.toCharArray()) if (Character.isDigit(c) && input.length() < 30) input += c; return true; }

    @Override protected void onKey(int k) {
        if (show > 0) return;
        if (k == 259 && !input.isEmpty()) input = input.substring(0, input.length() - 1);
        if (k == K_ENTER) {
            if (input.equals(number)) { score = digits; digits++; flash = 0.6f; PackPuzzle.win(); next(); }
            else { wrong = true; PackPuzzle.bad(); gameOver(); }
        }
    }

    @Override protected void update(float dt) { show -= dt; flash = Math.max(0, flash - dt); }

    @Override protected void render() {
        text("Merke dir die Zahl (" + digits + " Ziffern)", 190, 30, 8, Fonts.BOLD, Style.sub);
        if (show > 0) {
            text(number, 190, 100, Math.min(28, 340f / digits * 1.6f), Fonts.BOLD, Style.text);
            box(40, 150, 300 * Math.max(0, show / (1.2f + digits * 0.45f)), 5, 2, Style.accent);
        } else {
            box(40, 80, 300, 44, 10, Style.light ? 0xFFE6E8EE : 0xFF1C1F27);
            text(input.isEmpty() ? "Zahl eintippen + Enter" : input, 190, 102, input.isEmpty() ? 9 : Math.min(18, 300f / Math.max(1, input.length()) * 1.6f), Fonts.BOLD, input.isEmpty() ? Style.muted : Style.accent);
        }
        if (flash > 0) text("Richtig!", 190, 170, 12, Fonts.BOLD, Gx.withAlpha(0xFF39D353, flash / 0.6f));
    }
}

// ====================================================================== Flipper
final class Pinball extends GameView {
    private static final float W = 260, H = 310, BR = 6;
    private float bxx, byy, vx, vy, la, ra, lav, rav;
    private boolean lUp, rUp;
    private int balls;
    private final float[][] bumpers = {{80, 90, 16}, {180, 90, 16}, {130, 140, 18}, {60, 170, 11}, {200, 170, 11}};
    private final float[] bumpFx = new float[5];

    Pinball() { super("pinball"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String overTitle() { return "Alle Kugeln verloren"; }

    @Override protected void reset() { balls = 3; launch(); }
    private void launch() { bxx = W - 16; byy = H - 60; vx = -40 - rnd.nextFloat() * 60; vy = -520; }

    @Override protected void onKey(int k) { if (k == K_LEFT || k == K_A) lUp = true; if (k == K_RIGHT || k == K_D) rUp = true; if (k == K_SPACE) { lUp = rUp = true; } }
    @Override protected void onKeyUp(int k) { if (k == K_LEFT || k == K_A) lUp = false; if (k == K_RIGHT || k == K_D) rUp = false; if (k == K_SPACE) { lUp = rUp = false; } }
    @Override protected void onClick(float x, float y, int b) { if (x < W / 2) lUp = true; else rUp = true; }
    @Override protected void onRelease(float x, float y, int b) { lUp = rUp = false; }

    /** flipper as segment from pivot, length 52; returns endpoint. */
    private float[] flipper(boolean left, float a) {
        float px = left ? 70 : W - 70, py = H - 40;
        float ang = (float) Math.toRadians(left ? 25 - a : 155 + a);
        return new float[]{px, py, px + (float) Math.cos(ang) * 52, py + (float) Math.sin(ang) * 52};
    }

    private void segment(float[] s, float angVel, boolean left) {
        float dx = s[2] - s[0], dy = s[3] - s[1], len2 = dx * dx + dy * dy;
        float t = Math.max(0, Math.min(1, ((bxx - s[0]) * dx + (byy - s[1]) * dy) / len2));
        float cx = s[0] + dx * t, cy = s[1] + dy * t, ddx = bxx - cx, ddy = byy - cy, d = (float) Math.hypot(ddx, ddy);
        if (d < BR + 3 && d > 0) {
            float nx = ddx / d, ny = ddy / d;
            bxx = cx + nx * (BR + 3); byy = cy + ny * (BR + 3);
            float sv = (float) Math.toRadians(angVel) * t * 52;   // surface speed
            float rel = vx * nx + vy * ny;
            if (rel < 0) { vx -= 1.5f * rel * nx; vy -= 1.5f * rel * ny; }
            if (sv > 0) { vx += nx * sv * 0.9f; vy += ny * sv * 0.9f; }
        }
    }

    @Override protected void update(float dt) {
        float nla = Math.max(0, Math.min(50, la + (lUp ? 900 : -500) * dt)), nra = Math.max(0, Math.min(50, ra + (rUp ? 900 : -500) * dt));
        lav = (nla - la) / dt; rav = (nra - ra) / dt; la = nla; ra = nra;
        for (int i = 0; i < 5; i++) bumpFx[i] = Math.max(0, bumpFx[i] - dt * 4);
        int sub = 4;
        for (int s = 0; s < sub; s++) {
            float h = dt / sub;
            vy += 420 * h;
            bxx += vx * h; byy += vy * h;
            if (bxx < BR) { bxx = BR; vx = Math.abs(vx) * 0.8f; } if (bxx > W - BR) { bxx = W - BR; vx = -Math.abs(vx) * 0.8f; }
            if (byy < BR) { byy = BR; vy = Math.abs(vy) * 0.8f; }
            for (int i = 0; i < 5; i++) {
                float[] bm = bumpers[i];
                float dx = bxx - bm[0], dy = byy - bm[1], d = (float) Math.hypot(dx, dy);
                if (d < bm[2] + BR && d > 0) {
                    float nx = dx / d, ny = dy / d;
                    bxx = bm[0] + nx * (bm[2] + BR); byy = bm[1] + ny * (bm[2] + BR);
                    float rel = vx * nx + vy * ny;
                    vx -= 2 * rel * nx; vy -= 2 * rel * ny;
                    vx += nx * 140; vy += ny * 140;
                    score += 100; bumpFx[i] = 1;
                    Sound.play("minecraft:block.note_block.bell", 1.2f + i * 0.1f, 0.3f);
                }
            }
            // slanted walls into the flippers
            segment(new float[]{0, H - 90, 70, H - 40}, 0, true);
            segment(new float[]{W, H - 90, W - 70, H - 40}, 0, false);
            segment(flipper(true, la), lav, true);
            segment(flipper(false, ra), rav, false);
            float sp = (float) Math.hypot(vx, vy);
            if (sp > 700) { vx *= 700 / sp; vy *= 700 / sp; }
        }
        if (byy > H + 10) { balls--; Sound.play("minecraft:block.note_block.bass", 0.7f, 0.4f); if (balls <= 0) gameOver(); else launch(); }
    }

    @Override protected void render() {
        boxV(0, 0, W, H, 0, 0xFF1A0A30, 0xFF0A1440);
        for (int i = 0; i < 5; i++) { float[] bm = bumpers[i]; if (bumpFx[i] > 0) circle(bm[0], bm[1], bm[2] + 6 * bumpFx[i], Gx.withAlpha(0xFFFFE14D, 0.5f * bumpFx[i])); circle(bm[0], bm[1], bm[2], PackPuzzle.PAL[i]); circle(bm[0], bm[1], bm[2] * 0.55f, 0xFFF4F4F4); }
        line(0, H - 90, 70, H - 40, 4, 0xFF9AA3AD);
        line(W, H - 90, W - 70, H - 40, 4, 0xFF9AA3AD);
        float[] l = flipper(true, la), r = flipper(false, ra);
        line(l[0], l[1], l[2], l[3], 8, 0xFFE3000B);
        line(r[0], r[1], r[2], r[3], 8, 0xFFE3000B);
        circle(bxx, byy, BR, 0xFFD8DEE6);
        circle(bxx - 2, byy - 2, 2, 0xFFFFFFFF);
        textL("Kugeln: " + balls, 6, 10, 7, Fonts.BOLD, 0xFFFFFFFF);
    }
}

// ====================================================================== Kanonen-Duell
final class CannonDuel extends GameView {
    private static final float W = 420, H = 260;
    private final float[] ground = new float[85];
    private float ang = 45, pow = 60, wind, think, aiAng, aiPow;
    private float shX, shY, shVx, shVy, boomX, boomY, boomT = -1;
    private boolean flying;
    private int turn, hp1, hp2;
    private final float p1x = 50, p2x = 370;

    CannonDuel() { super("cannons"); }
    @Override protected boolean hasAi() { return true; }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Treffer"; }
    @Override protected String overTitle() { return hp2 <= 0 ? "Gewonnen!" : "Verloren"; }

    @Override protected void reset() {
        float a = rnd.nextFloat() * 6, b = rnd.nextFloat() * 6;
        for (int i = 0; i < ground.length; i++) ground[i] = 190 - 35 * (float) Math.sin(i * 0.07 + a) - 20 * (float) Math.sin(i * 0.17 + b) - (i > 30 && i < 55 ? 30 : 0);
        hp1 = hp2 = 3; turn = 1; flying = false; newWind(); score = 0;
    }

    private void newWind() { wind = (rnd.nextFloat() - 0.5f) * 40; }
    private float gy(float x) { int i = (int) Math.max(0, Math.min(ground.length - 2, x / 5)); float f = x / 5 - i; return ground[i] + (ground[i + 1] - ground[i]) * f; }

    private void fire(float a, float p, boolean left) {
        float rad = (float) Math.toRadians(a);
        shX = left ? p1x : p2x; shY = gy(shX) - 12;
        shVx = (float) Math.cos(rad) * p * 4.2f * (left ? 1 : -1); shVy = -(float) Math.sin(rad) * p * 4.2f;
        flying = true; Sound.play("minecraft:entity.generic.explode", 2f, 0.3f);
    }

    /** Simulates a shot, returns landing x. */
    private float sim(float a, float p) {
        float rad = (float) Math.toRadians(a), x = p2x, y = gy(p2x) - 12, vx = -(float) Math.cos(rad) * p * 4.2f, vy = -(float) Math.sin(rad) * p * 4.2f;
        for (int i = 0; i < 2000; i++) { vy += 200 * 0.01f; vx += wind * 0.01f; x += vx * 0.01f; y += vy * 0.01f; if (x < 0 || x > W || y >= gy(x)) return x; }
        return x;
    }

    @Override protected void onKey(int k) {
        if (turn != 1 || flying) return;
        if (k == K_UP || k == K_W) ang = Math.min(85, ang + 2);
        if (k == K_DOWN || k == K_S) ang = Math.max(5, ang - 2);
        if (k == K_RIGHT || k == K_D) pow = Math.min(100, pow + 2);
        if (k == K_LEFT || k == K_A) pow = Math.max(10, pow - 2);
        if (k == K_SPACE || k == K_ENTER) fire(ang, pow, true);
    }

    @Override protected void onClick(float x, float y, int b) { if (turn == 1 && !flying && in(x, y, 170, 10, 80, 24)) fire(ang, pow, true); }

    @Override protected void update(float dt) {
        if (boomT >= 0 && (boomT += dt) > 0.6f) boomT = -1;
        if (flying) {
            for (int s = 0; s < 3; s++) {
                float h = dt / 3;
                shVy += 200 * h; shVx += wind * h; shX += shVx * h; shY += shVy * h;
                if (shX < -20 || shX > W + 20 || shY >= gy(Math.max(0, Math.min(W, shX)))) {
                    flying = false; boomX = shX; boomY = shY; boomT = 0; Sound.play("minecraft:entity.generic.explode", 1.2f, 0.35f);
                    for (int i = 0; i < ground.length; i++) { float d = Math.abs(i * 5 - shX); if (d < 16) ground[i] = Math.min(H - 5, ground[i] + (16 - d) * 0.6f); }
                    if (Math.abs(shX - p2x) < 18) { hp2--; score++; }
                    if (Math.abs(shX - p1x) < 18) hp1--;
                    if (hp1 <= 0 || hp2 <= 0) { if (hp2 <= 0) PackPuzzle.win(); else PackPuzzle.bad(); gameOver(); return; }
                    turn = 3 - turn; think = 0; newWind();
                    if (turn == 2) planAi();
                    break;
                }
            }
            return;
        }
        if (turn == 2 && (think += dt) > 1.0f) fire(aiAng, aiPow, false);
    }

    private void planAi() {
        float best = 1e9f;
        for (float a = 15; a <= 75; a += 3) for (float p = 20; p <= 100; p += 2) { float d = Math.abs(sim(a, p) - p1x); if (d < best) { best = d; aiAng = a; aiPow = p; } }
        float err = new float[]{14, 7, 3, 1}[aiLevel()];
        aiAng += (rnd.nextFloat() - 0.5f) * err; aiPow += (rnd.nextFloat() - 0.5f) * err;
    }

    @Override protected void render() {
        boxV(0, 0, W, H, 0, 0xFF8AC8FF, 0xFFE0F2FF);
        for (int i = 0; i < ground.length - 1; i++) box(i * 5, ground[i], 5.5f, H - ground[i], 0, 0xFF5DBB4E);
        for (int i = 0; i < 2; i++) {
            float x = i == 0 ? p1x : p2x, y = gy(x);
            box(x - 10, y - 10, 20, 10, 3, i == 0 ? 0xFF006CB7 : 0xFFE3000B);
            float a = (float) Math.toRadians(i == 0 ? ang : aiAng);
            line(x, y - 8, x + (float) Math.cos(a) * 16 * (i == 0 ? 1 : -1), y - 8 - (float) Math.sin(a) * 16, 3, 0xFF2A2E38);
            int hp = i == 0 ? hp1 : hp2;
            for (int k = 0; k < 3; k++) circle(x - 8 + k * 8, y - 26, 3, k < hp ? 0xFFFF3B4C : 0x55000000);
        }
        if (flying) circle(shX, shY, 3, 0xFF1B1B22);
        if (boomT >= 0) circle(boomX, boomY, 6 + boomT * 30, Gx.withAlpha(0xFFFF8A00, 1 - boomT / 0.6f));
        textL("Winkel " + Math.round(ang) + "°  Kraft " + Math.round(pow), 8, 14, 7.5f, Fonts.BOLD, 0xFF1B1B22);
        textL("Wind " + (wind > 0 ? "→ " : "← ") + Math.round(Math.abs(wind)), 8, 28, 7, Fonts.MEDIUM, 0xFF1B1B22);
        if (turn == 1 && !flying) button("Feuer!", 170, 10, 80, 24, 0xFFE3000B);
        else text(turn == 2 ? "Gegner zielt..." : "", 210, 22, 8, Fonts.BOLD, 0xFF1B1B22);
    }
}

// ====================================================================== Katapult
final class Catapult extends GameView {
    private static final float W = 420, H = 270, GROUND = 245, SX = 60, SY = 200;
    private final List<float[]> blocks = new ArrayList<>();   // x y w h vx vy alive(1) kind(0 block,1 target)
    private float bxx, byy, vx, vy, settle;
    private boolean drag, flying;
    private int shots, level;

    Catapult() { super("catapult"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String overTitle() { return "Keine Schüsse mehr"; }

    @Override protected void reset() { level = 1; build(); }

    private void build() {
        blocks.clear();
        float x0 = 250 + rnd.nextFloat() * 40;
        int towers = 2 + Math.min(2, level / 2);
        for (int t = 0; t < towers; t++) {
            float x = x0 + t * 45;
            int hgt = 2 + rnd.nextInt(3);
            for (int k = 0; k < hgt; k++) blocks.add(new float[]{x, GROUND - (k + 1) * 22, 14, 22, 0, 0, 1, 0});
            blocks.add(new float[]{x - 1, GROUND - hgt * 22 - 16, 16, 16, 0, 0, 1, 1});
        }
        shots = 3; reset2();
    }

    private void reset2() { bxx = SX; byy = SY; vx = vy = 0; flying = false; settle = 0; }

    private int targets() { int n = 0; for (float[] b : blocks) if (b[6] == 1 && b[7] == 1) n++; return n; }

    @Override protected void onClick(float x, float y, int b) { if (!flying && Math.hypot(x - SX, y - SY) < 30) drag = true; }
    @Override protected void onRelease(float x, float y, int b) {
        if (!drag) return;
        drag = false;
        float dx = SX - x, dy = SY - y, d = (float) Math.min(60, Math.hypot(dx, dy));
        if (d < 8) return;
        float n = (float) Math.hypot(dx, dy);
        vx = dx / n * d * 9; vy = dy / n * d * 9; flying = true; shots--;
        Sound.play("minecraft:entity.arrow.shoot", 0.8f, 0.4f);
    }

    @Override protected void update(float dt) {
        for (float[] b : blocks) {
            if (b[6] == 0) continue;
            b[5] += 500 * dt; b[1] += b[5] * dt; b[0] += b[4] * dt; b[4] *= 0.98f;
            float floor = GROUND;
            for (float[] o : blocks) if (o != b && o[6] == 1 && b[0] + b[2] > o[0] + 1 && b[0] < o[0] + o[2] - 1 && o[1] >= b[1] + b[3] - 4) floor = Math.min(floor, o[1]);
            if (b[1] + b[3] > floor) {
                if (b[5] > 260 && b[7] == 1) { b[6] = 0; score += 500; Sound.play("minecraft:entity.creeper.death", 1.2f, 0.4f); }
                b[1] = floor - b[3]; b[5] = 0; b[4] *= 0.6f;
            }
            if (b[0] < -40 || b[0] > W + 40) b[6] = 0;
        }
        if (flying) {
            vy += 500 * dt; bxx += vx * dt; byy += vy * dt;
            for (float[] b : blocks) if (b[6] == 1 && bxx + 7 > b[0] && bxx - 7 < b[0] + b[2] && byy + 7 > b[1] && byy - 7 < b[1] + b[3]) {
                if (b[7] == 1) { b[6] = 0; score += 500; Sound.play("minecraft:entity.creeper.death", 1.2f, 0.4f); }
                else { b[4] += vx * 0.6f; b[5] -= 80; score += 10; Sound.play("minecraft:block.wood.break", 1f, 0.4f); }
                vx *= 0.5f; vy *= 0.5f;
            }
            if (byy > GROUND - 7) { byy = GROUND - 7; vy = -vy * 0.3f; vx *= 0.7f; }
            if (bxx > W + 30 || (Math.abs(vx) < 15 && byy >= GROUND - 8)) { settle += dt; if (settle > 0.8f) after(); }
        }
    }

    private void after() {
        if (targets() == 0) { score += shots * 300L; level++; PackPuzzle.win(); build(); return; }
        if (shots <= 0) { gameOver(); return; }
        reset2();
    }

    @Override protected void render() {
        boxV(0, 0, W, H, 0, 0xFF8AC8FF, 0xFFD8F0FF);
        box(0, GROUND, W, H - GROUND, 0, 0xFF5DBB4E);
        line(SX - 8, GROUND, SX - 4, SY + 6, 4, 0xFF6A4A2A); line(SX + 8, GROUND, SX + 4, SY + 6, 4, 0xFF6A4A2A);
        for (float[] b : blocks) if (b[6] == 1) {
            if (b[7] == 1) { box(b[0], b[1], b[2], b[3], 2, 0xFF39D353); box(b[0] + 3, b[1] + 4, 3, 3, 0, 0xFF1B1B22); box(b[0] + b[2] - 6, b[1] + 4, 3, 3, 0, 0xFF1B1B22); box(b[0] + 5, b[1] + 9, b[2] - 10, 5, 0, 0xFF1B1B22); }
            else brick(b[0], b[1], b[2], b[3], 0xFFC8873A);
        }
        float x = bxx, y = byy;
        if (drag) { float dx = SX - bmx(), dy = SY - bmy(), n = (float) Math.hypot(dx, dy), d = Math.min(60, n); if (n > 0) { x = SX - dx / n * d; y = SY - dy / n * d; } line(SX - 4, SY, x, y, 1.5f, 0xFF3A2414); line(SX + 4, SY, x, y, 1.5f, 0xFF3A2414); }
        circle(x, y, 7, 0xFFE3000B);
        circle(x - 2, y - 2, 2, 0xAAFFFFFF);
        textL("Level " + level + " · Schüsse " + shots + " · Creeper " + targets(), 8, 12, 7.5f, Fonts.BOLD, 0xFF1B1B22);
    }
}

// ====================================================================== Ausweichen
final class Dodge extends GameView {
    private static final float W = 360, H = 280;
    private final List<float[]> rocks = new ArrayList<>();   // x y r vy
    private float px = W / 2, spawn;

    Dodge() { super("dodge"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Sekunden"; }
    @Override protected String scoreText() { return String.format(Locale.ROOT, "%.1f", time); }
    @Override protected String bestText(long best) { return String.format(Locale.ROOT, "%.1f", best / 10.0); }

    @Override protected void reset() { rocks.clear(); px = W / 2; spawn = 0; }

    @Override protected void update(float dt) {
        px += (Math.max(10, Math.min(W - 10, bmx())) - px) * Math.min(1, dt * 14);
        spawn -= dt * (1 + time / 15);
        if (spawn <= 0) { spawn = 0.35f; rocks.add(new float[]{rnd.nextFloat() * W, -10, 5 + rnd.nextFloat() * 10, 90 + rnd.nextFloat() * 80 + time * 4}); }
        for (float[] r : rocks) r[1] += r[3] * dt;
        rocks.removeIf(r -> r[1] > H + 20);
        for (float[] r : rocks) if (Math.hypot(r[0] - px, r[1] - (H - 20)) < r[2] + 8) { score = (long) (time * 10); PackArcade.boom(); gameOver(); return; }
    }

    @Override protected void render() {
        boxV(0, 0, W, H, 0, 0xFF101A30, 0xFF1A2A50);
        for (float[] r : rocks) { circle(r[0], r[1], r[2], 0xFF8A6A5A); circle(r[0] - r[2] * 0.3f, r[1] - r[2] * 0.3f, r[2] * 0.3f, 0xFFA88A7A); }
        brick(px - 10, H - 28, 20, 16, 0xFF3FA9FF);
        text(String.format(Locale.ROOT, "%.1f s", time), W / 2, 16, 10, Fonts.BOLD, 0xFFFFFFFF);
    }
}

// ====================================================================== Farbwechsel
final class ColorSwitch extends GameView {
    private static final float W = 240, H = 320;
    private static final int[] COL = {0xFFFF2A6A, 0xFFFFD23F, 0xFF3AD8FF, 0xFF9B4DFF};
    private float by2, vy, cam;
    private int color;
    private final List<float[]> rings = new ArrayList<>();   // y, speed, passed, switcherTaken

    ColorSwitch() { super("colorswitch"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }

    @Override protected void reset() { by2 = 260; vy = 0; cam = 0; color = 0; rings.clear(); for (int i = 0; i < 4; i++) rings.add(new float[]{120 - i * 170, 1 + rnd.nextFloat(), 0, 0}); }

    private void jump() { vy = -260; Sound.play("minecraft:block.note_block.hat", 1.5f, 0.2f); }
    @Override protected void onClick(float x, float y, int b) { jump(); }
    @Override protected void onKey(int k) { if (k == K_SPACE || k == K_UP || k == K_W) jump(); }

    @Override protected void update(float dt) {
        if (score > 0 || vy != 0) vy += 700 * dt;
        by2 += vy * dt;
        if (by2 - cam < H * 0.45f) cam = by2 - H * 0.45f;
        if (by2 - cam > H + 10) { gameOver(); return; }
        float top = 1e9f; for (float[] r : rings) top = Math.min(top, r[0]);
        if (top > cam - 200) rings.add(new float[]{top - 170, 1 + rnd.nextFloat() * (1 + score * 0.08f), 0, 0});
        rings.removeIf(r -> r[0] - cam > H + 80);
        for (float[] r : rings) {
            float rot = (float) (time * r[1] * 1.6);
            // ring of radius 46 thickness 7 at (W/2, r[0]); ball must cross at top and bottom
            for (float edge : new float[]{r[0] - 46, r[0] + 46}) {
                if (Math.abs(by2 - edge) < 7) {
                    double a = edge < r[0] ? -Math.PI / 2 : Math.PI / 2;
                    int seg = (int) Math.floor((((a - rot) % (Math.PI * 2)) + Math.PI * 4) % (Math.PI * 2) / (Math.PI / 2));
                    if (seg != color) { PackArcade.boom(); gameOver(); return; }
                }
            }
            if (r[2] == 0 && by2 < r[0] - 46) { r[2] = 1; score++; Sound.play("minecraft:entity.experience_orb.pickup", 1.3f, 0.3f); }
            if (r[3] == 0 && Math.abs(by2 - (r[0] - 85)) < 10) { r[3] = 1; int c; do c = rnd.nextInt(4); while (c == color); color = c; Sound.play("minecraft:block.amethyst_block.chime", 1.4f, 0.5f); }
        }
    }

    @Override protected void render() {
        box(0, 0, W, H, 0, 0xFF15121F);
        for (float[] r : rings) {
            float cy = r[0] - cam, rot = (float) (time * r[1] * 1.6);
            for (int i = 0; i < 40; i++) {
                double a = rot + Math.PI * 2 * i / 40.0;
                int seg = (int) Math.floor(((i / 40.0) * 4)) % 4;
                circle(W / 2 + (float) Math.cos(a) * 46, cy + (float) Math.sin(a) * 46, 4.2f, COL[seg]);
            }
            if (r[3] == 0) { float sy = r[0] - 85 - cam; for (int i = 0; i < 4; i++) circle(W / 2 + (i % 2 == 0 ? -3 : 3), sy + (i / 2 == 0 ? -3 : 3), 3.5f, COL[i]); }
        }
        circle(W / 2, by2 - cam, 7, COL[color]);
        text(String.valueOf(score), W / 2, 18, 14, Fonts.BOLD, 0xFFFFFFFF);
        if (vy == 0 && score == 0) text("Klick / Leertaste zum Springen", W / 2, H - 20, 6.6f, Fonts.MEDIUM, 0xAAFFFFFF);
    }
}

// ====================================================================== Ballon-Platzen
final class BalloonPop extends GameView {
    private static final float W = 380, H = 280;
    private final List<float[]> b = new ArrayList<>();      // x y speed color kind(0 balloon,1 bomb,2 gold) popT
    private float spawn, left;

    BalloonPop() { super("balloonpop"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String overTitle() { return "Zeit um!"; }

    @Override protected void reset() { b.clear(); spawn = 0; left = 45; }

    @Override protected void onClick(float x, float y, int bt) {
        for (int i = b.size() - 1; i >= 0; i--) {
            float[] o = b.get(i);
            if (o[5] >= 0 || Math.hypot(x - o[0], (y - o[1]) * 0.85f) > 15) continue;
            o[5] = 0;
            if (o[4] == 1) { score = Math.max(0, score - 30); PackArcade.boom(); }
            else { score += o[4] == 2 ? 50 : 10; PackArcade.pop(); }
            return;
        }
    }

    @Override protected void update(float dt) {
        left -= dt;
        if (left <= 0) { left = 0; gameOver(); return; }
        spawn -= dt * (1 + (45 - left) / 25);
        if (spawn <= 0) { spawn = 0.45f; float r = rnd.nextFloat(); b.add(new float[]{20 + rnd.nextFloat() * (W - 40), H + 20, 40 + rnd.nextFloat() * 50 + (45 - left) * 1.5f, rnd.nextInt(6), r < 0.12f ? 1 : r < 0.2f ? 2 : 0, -1}); }
        for (float[] o : b) { o[1] -= o[2] * dt; if (o[5] >= 0) o[5] += dt; }
        b.removeIf(o -> o[1] < -40 || o[5] > 0.3f);
    }

    @Override protected void render() {
        boxV(0, 0, W, H, 0, 0xFF8AC8FF, 0xFFFFF0E0);
        for (float[] o : b) {
            if (o[5] >= 0) { circle(o[0], o[1], 14 + o[5] * 40, Gx.withAlpha(0xFFFFFFFF, 1 - o[5] / 0.3f)); continue; }
            line(o[0], o[1] + 16, o[0] + (float) Math.sin(time * 3 + o[0]) * 3, o[1] + 34, 0.6f, 0xFF5A6270);
            if (o[4] == 1) { circle(o[0], o[1], 13, 0xFF1B1B22); text("!", o[0], o[1], 10, Fonts.BOLD, 0xFFFF3B4C); }
            else { int c = o[4] == 2 ? 0xFFFFD23F : PackPuzzle.PAL[(int) o[3]]; box(o[0] - 12, o[1] - 15, 24, 30, 12, c); circle(o[0] - 4, o[1] - 7, 3, 0x88FFFFFF); }
        }
        text(String.format(Locale.ROOT, "%.0f s", left), W - 30, 14, 9, Fonts.BOLD, left < 10 ? 0xFFFF3B4C : 0xFF1B1B22);
    }
}

// ====================================================================== Angeln
final class Fishing extends GameView {
    private static final float W = 380, H = 260;
    private static final String[] FISH = {"Kabeljau", "Lachs", "Tropenfisch", "Kugelfisch", "Alter Stiefel", "Goldfisch", "Riesenhecht"};
    private static final int[] PTS = {10, 20, 30, 25, 1, 60, 100};
    private int phase;          // 0 idle, 1 waiting, 2 biting, 3 caught/missed message
    private float timer, left, bob;
    private String msg = "";
    private int caught;

    Fishing() { super("fishing"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String overTitle() { return caught + " Fische gefangen"; }

    @Override protected void reset() { phase = 0; left = 60; caught = 0; msg = "Klick zum Auswerfen"; }

    @Override protected void onClick(float x, float y, int b) {
        if (phase == 0) { phase = 1; timer = 1.5f + rnd.nextFloat() * 3.5f; msg = "Warte, bis der Schwimmer zuckt..."; Sound.play("minecraft:entity.fishing_bobber.throw", 1f, 0.5f); }
        else if (phase == 1) { phase = 3; timer = 1; msg = "Zu früh gezogen!"; PackPuzzle.bad(); }
        else if (phase == 2) {
            int f = rnd.nextFloat() < 0.04f ? 6 : rnd.nextFloat() < 0.1f ? 5 : rnd.nextInt(5);
            score += PTS[f]; caught++; phase = 3; timer = 1.2f; msg = "Gefangen: " + FISH[f] + " (+" + PTS[f] + ")";
            Sound.play("minecraft:entity.fishing_bobber.retrieve", 1f, 0.5f);
        }
    }

    @Override protected void update(float dt) {
        left -= dt;
        if (left <= 0) { left = 0; gameOver(); return; }
        timer -= dt;
        if (phase == 1 && timer <= 0) { phase = 2; timer = 0.55f + (float) Math.random() * 0.2f; Sound.play("minecraft:entity.fishing_bobber.splash", 1f, 0.6f); }
        else if (phase == 2 && timer <= 0) { phase = 3; timer = 1; msg = "Der Fisch ist weg!"; }
        else if (phase == 3 && timer <= 0) { phase = 0; msg = "Klick zum Auswerfen"; }
        bob = phase == 2 ? 6 : (float) Math.sin(time * 2) * 1.2f;
    }

    @Override protected void render() {
        boxV(0, 0, W, 110, 0, 0xFFFFB070, 0xFFFFE0B0);
        boxV(0, 110, W, H - 110, 0, 0xFF2E86DE, 0xFF0A2A5A);
        box(0, 90, 90, 25, 0, 0xFF8A5A30);
        line(70, 90, 140, 30, 2, 0xFF6A4A2A);
        if (phase == 1 || phase == 2) { line(140, 30, 260, 112 + bob, 0.6f, 0xFFF4F4F4); circle(260, 112 + bob, 4, 0xFFE3000B); circle(260, 108 + bob, 2.5f, 0xFFF4F4F4); if (phase == 2) ring(260, 114, 10 + (float) Math.sin(time * 20) * 3, 1, 0xAAFFFFFF); }
        for (int i = 0; i < 4; i++) { float fx = (float) ((time * (20 + i * 9) + i * 97) % (W + 40)) - 20, fy = 150 + i * 25; box(fx, fy, 16, 7, 3, 0x553A6AA0); }
        text(msg, W / 2, 60, 9, Fonts.BOLD, phase == 2 ? 0xFFE3000B : 0xFF3A2414);
        textL(String.format(Locale.ROOT, "%.0f s · %d Fische", left, caught), 8, 12, 7.5f, Fonts.BOLD, 0xFF3A2414);
    }
}

// ====================================================================== Keks-Klicker
final class CookieClicker extends GameView {
    private static final String[] NAMES = {"Klick-Kraft", "Oma", "Bäckerei", "Fabrik", "Lego-Roboter"};
    private static final double[] BASE = {15, 50, 400, 2500, 12000}, CPS = {0, 1, 8, 50, 300};
    private final int[] owned = new int[5];
    private double cookies, total;
    private float left, pop;

    CookieClicker() { super("cookies"); }
    @Override protected float boardW() { return 420; }
    @Override protected float boardH() { return 280; }
    @Override protected String scoreLabel() { return "Kekse"; }
    @Override protected String overTitle() { return "Zeit um! " + fmt(total) + " Kekse gebacken"; }

    @Override protected void reset() { java.util.Arrays.fill(owned, 0); cookies = total = 0; left = 120; }

    private double price(int i) { return Math.floor(BASE[i] * Math.pow(1.15, owned[i])); }
    private double perClick() { return 1 + owned[0]; }
    private double cps() { double s = 0; for (int i = 1; i < 5; i++) s += owned[i] * CPS[i]; return s; }
    static String fmt(double d) { return d >= 1e6 ? String.format(Locale.ROOT, "%.2f Mio", d / 1e6) : String.valueOf((long) d); }

    private void add(double d) { cookies += d; total += d; score = (long) total; }

    @Override protected void onClick(float x, float y, int b) {
        if (Math.hypot(x - 110, y - 140) < 70) { add(perClick()); pop = 0.15f; Sound.play("minecraft:entity.generic.eat", 1.5f, 0.15f); return; }
        for (int i = 0; i < 5; i++) if (in(x, y, 240, 20 + i * 50, 165, 44) && cookies >= price(i)) { cookies -= price(i); owned[i]++; Sound.play("minecraft:entity.experience_orb.pickup", 1.2f, 0.3f); }
    }

    @Override protected void update(float dt) { add(cps() * dt); pop = Math.max(0, pop - dt); left -= dt; if (left <= 0) { left = 0; gameOver(); } }

    @Override protected void render() {
        float s = 1 + pop * 0.6f;
        circle(110, 140, 64 * s, 0xFFC8873A);
        circle(110, 140, 58 * s, 0xFFE0A858);
        for (int i = 0; i < 9; i++) circle(110 + (float) Math.cos(i * 2.1) * 34 * s * (i % 3 == 0 ? 0.4f : 1), 140 + (float) Math.sin(i * 2.1) * 34 * s * (i % 3 == 0 ? 0.4f : 1), 6 * s, 0xFF4A2A14);
        text(fmt(cookies) + " Kekse", 110, 30, 12, Fonts.BOLD, Style.text);
        text(fmt(cps()) + " pro Sekunde", 110, 50, 7, Fonts.MEDIUM, Style.sub);
        text(String.format(Locale.ROOT, "%.0f s", left), 110, 250, 9, Fonts.BOLD, left < 15 ? Style.danger : Style.sub);
        for (int i = 0; i < 5; i++) {
            boolean can = cookies >= price(i);
            box(240, 20 + i * 50, 165, 44, 8, can ? (in(bmx(), bmy(), 240, 20 + i * 50, 165, 44) ? Style.surfaceHover : Style.surface) : Gx.withAlpha(Style.surface, 0.5f));
            textL(NAMES[i] + " (" + owned[i] + ")", 248, 34 + i * 50, 7.5f, Fonts.BOLD, can ? Style.text : Style.muted);
            textL(fmt(price(i)) + " Kekse · " + (i == 0 ? "+1 pro Klick" : "+" + (int) CPS[i] + "/s"), 248, 50 + i * 50, 6.2f, Fonts.MEDIUM, can ? Style.ok : Style.muted);
        }
    }
}

// ====================================================================== Leiterspiel
final class Ladders extends GameView {
    private static final int[][] JUMPS = {{3, 22}, {8, 30}, {20, 41}, {28, 76}, {50, 67}, {71, 92}, {80, 99}, {17, 4}, {54, 34}, {62, 19}, {64, 60}, {87, 24}, {93, 73}, {95, 75}, {98, 79}};
    private static final float C = 26, OX = 8, OY = 8;
    private int me, ai, turn, die, lastWinner;
    private float anim, think;
    private String msg = "";

    Ladders() { super("ladders"); }
    @Override protected float boardW() { return 410; }
    @Override protected float boardH() { return 276; }
    @Override protected String scoreLabel() { return "Siege"; }
    @Override protected String overTitle() { return lastWinner == 1 ? "Gewonnen!" : "Die KI war schneller"; }

    @Override protected void reset() { me = ai = 0; turn = 1; die = 0; msg = "Klick auf den Würfel"; }

    private float[] pos(int sq) {
        if (sq <= 0) return new float[]{OX - 12, OY + 9 * C + C / 2};
        int i = sq - 1, row = i / 10, col = row % 2 == 0 ? i % 10 : 9 - i % 10;
        return new float[]{OX + col * C + C / 2, OY + (9 - row) * C + C / 2};
    }

    private int move(int p, int d, String who) {
        int n = p + d;
        if (n > 100) n = p;
        for (int[] j : JUMPS) if (j[0] == n) { msg = who + (j[1] > n ? " klettert die Leiter hoch!" : " rutscht die Schlange runter!"); n = j[1]; Sound.play(j[1] > j[0] ? "minecraft:entity.player.levelup" : "minecraft:block.note_block.bass", 1.2f, 0.4f); }
        return n;
    }

    private void roll() {
        die = 1 + rnd.nextInt(6); anim = 0.4f;
        Sound.play("minecraft:block.wood.hit", 1.6f, 0.35f);
        msg = "";
        if (turn == 1) { me = move(me, die, "Du"); if (me == 100) { lastWinner = 1; score++; PackPuzzle.win(); gameOver(); return; } }
        else { ai = move(ai, die, "KI"); if (ai == 100) { lastWinner = 2; PackPuzzle.bad(); gameOver(); return; } }
        if (die != 6) turn = 3 - turn;
        think = 0;
    }

    @Override protected void onClick(float x, float y, int b) { if (turn == 1 && anim <= 0 && in(x, y, 300, 40, 60, 60)) roll(); }
    @Override protected void onKey(int k) { if (turn == 1 && anim <= 0 && k == K_SPACE) roll(); }
    @Override protected void update(float dt) { anim = Math.max(0, anim - dt); if (turn == 2 && anim <= 0 && (think += dt) > 0.8f) roll(); }

    @Override protected void render() {
        for (int sq = 1; sq <= 100; sq++) { float[] p = pos(sq); box(p[0] - C / 2, p[1] - C / 2, C, C, 0, ((sq + (sq - 1) / 10) % 2 == 0) ? 0xFFF2E6C8 : 0xFFE0CFA0); text(String.valueOf(sq), p[0] - 6, p[1] - 7, 4.5f, Fonts.MEDIUM, 0x995A4020); }
        for (int[] j : JUMPS) { float[] a = pos(j[0]), b2 = pos(j[1]); line(a[0], a[1], b2[0], b2[1], 3, j[1] > j[0] ? 0xCC8A5A30 : 0xCC2E9E4A); circle(b2[0], b2[1], 2.5f, j[1] > j[0] ? 0xFF8A5A30 : 0xFF2E9E4A); }
        float[] pm = pos(me), pa = pos(ai);
        circle(pm[0] - 4, pm[1], 6, 0xFF006CB7); circle(pa[0] + 4, pa[1], 6, 0xFFE3000B);
        box(300, 40, 60, 60, 10, 0xFFF8F8F8);
        int v = anim > 0 ? 1 + rnd.nextInt(6) : Math.max(1, die);
        int[][] pip = {{}, {4}, {0, 8}, {0, 4, 8}, {0, 2, 6, 8}, {0, 2, 4, 6, 8}, {0, 2, 3, 5, 6, 8}};
        for (int p : pip[v]) circle(315 + (p % 3) * 15, 55 + (p / 3) * 15, 4.5f, 0xFF15161A);
        text(turn == 1 ? "Du würfelst" : "KI würfelt", 330, 120, 8, Fonts.BOLD, turn == 1 ? 0xFF006CB7 : 0xFFE3000B);
        text("Du: " + me + "   KI: " + ai, 330, 140, 7, Fonts.BOLD, Style.text);
        List<String> l = Fonts.wrap(msg, Fonts.MEDIUM, 6.4f * u, S(130), 3);
        float y = 170; for (String s : l) { text(s, 330, y, 6.4f, Fonts.MEDIUM, Style.sub); y += 11; }
        text("6 = nochmal würfeln", 330, 250, 6, Fonts.MEDIUM, Style.muted);
    }
}

// ====================================================================== Panzer-Duell
final class TankDuel extends GameView {
    private static final float W = 400, H = 280;
    private static final float[][] WALLS = {{120, 60, 20, 80}, {260, 140, 20, 80}, {170, 200, 60, 16}, {170, 64, 60, 16}};
    private float px, py, pa, ax, ay, aa, cool, aiCool, aiTurn;
    private int php, ahp;
    private final List<float[]> bullets = new ArrayList<>();   // x y vx vy owner bounces
    private boolean up, down, left, right;

    TankDuel() { super("tanks"); }
    @Override protected boolean hasAi() { return true; }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Treffer"; }
    @Override protected String overTitle() { return ahp <= 0 ? "Gewonnen!" : "Zerstört"; }

    @Override protected void reset() { px = 40; py = H / 2; ax = W - 40; ay = H / 2; php = ahp = 3; bullets.clear(); cool = aiCool = 0; score = 0; }

    private boolean blocked(float x, float y) {
        if (x < 10 || y < 10 || x > W - 10 || y > H - 10) return true;
        for (float[] w : WALLS) if (x > w[0] - 9 && x < w[0] + w[2] + 9 && y > w[1] - 9 && y < w[1] + w[3] + 9) return true;
        return false;
    }

    private boolean wall(float x, float y) { for (float[] w : WALLS) if (x > w[0] && x < w[0] + w[2] && y > w[1] && y < w[1] + w[3]) return true; return false; }

    @Override protected void onKey(int k) { if (k == K_W || k == K_UP) up = true; if (k == K_S || k == K_DOWN) down = true; if (k == K_A || k == K_LEFT) left = true; if (k == K_D || k == K_RIGHT) right = true; if (k == K_SPACE) shoot(); }
    @Override protected void onKeyUp(int k) { if (k == K_W || k == K_UP) up = false; if (k == K_S || k == K_DOWN) down = false; if (k == K_A || k == K_LEFT) left = false; if (k == K_D || k == K_RIGHT) right = false; }
    @Override protected void onClick(float x, float y, int b) { shoot(); }

    private void shoot() {
        if (cool > 0) return;
        cool = 0.45f;
        float a = (float) Math.atan2(bmy() - py, bmx() - px);
        bullets.add(new float[]{px + (float) Math.cos(a) * 12, py + (float) Math.sin(a) * 12, (float) Math.cos(a) * 260, (float) Math.sin(a) * 260, 1, 0});
        Sound.play("minecraft:entity.firework_rocket.blast", 1.6f, 0.3f);
    }

    @Override protected void update(float dt) {
        cool -= dt; aiCool -= dt;
        float mx = (right ? 1 : 0) - (left ? 1 : 0), my = (down ? 1 : 0) - (up ? 1 : 0);
        if (mx != 0 || my != 0) { float n = (float) Math.hypot(mx, my); float nx = px + mx / n * 90 * dt, ny = py + my / n * 90 * dt; if (!blocked(nx, py)) px = nx; if (!blocked(px, ny)) py = ny; pa = (float) Math.atan2(my, mx); }
        // AI: keep distance, strafe, shoot when line of sight
        int lv = aiLevel();
        aiTurn -= dt;
        if (aiTurn <= 0) { aiTurn = 0.6f + rnd.nextFloat(); aa = (float) (Math.atan2(py - ay, px - ax) + (rnd.nextBoolean() ? 1.6 : -1.6) + (rnd.nextFloat() - 0.5f)); }
        float sp = new float[]{45, 60, 75, 90}[lv];
        float nx = ax + (float) Math.cos(aa) * sp * dt, ny = ay + (float) Math.sin(aa) * sp * dt;
        if (!blocked(nx, ny)) { ax = nx; ay = ny; } else aiTurn = 0;
        boolean sight = true;
        for (int i = 1; i < 20; i++) { float t = i / 20f; if (wall(ax + (px - ax) * t, ay + (py - ay) * t)) sight = false; }
        if (sight && aiCool <= 0) {
            aiCool = new float[]{1.6f, 1.1f, 0.8f, 0.55f}[lv];
            float err = new float[]{0.3f, 0.18f, 0.09f, 0.03f}[lv];
            float a = (float) Math.atan2(py - ay, px - ax) + (rnd.nextFloat() - 0.5f) * err * 2;
            bullets.add(new float[]{ax + (float) Math.cos(a) * 12, ay + (float) Math.sin(a) * 12, (float) Math.cos(a) * 240, (float) Math.sin(a) * 240, 2, 0});
        }
        for (int i = bullets.size() - 1; i >= 0; i--) {
            float[] b = bullets.get(i);
            float ox = b[0], oy = b[1];
            b[0] += b[2] * dt; b[1] += b[3] * dt;
            boolean hitWall = wall(b[0], b[1]) || b[0] < 0 || b[0] > W || b[1] < 0 || b[1] > H;
            if (hitWall) {
                if (b[5] >= 1) { bullets.remove(i); continue; }
                b[5]++;
                if (wall(b[0], oy) || b[0] < 0 || b[0] > W) b[2] = -b[2]; else b[3] = -b[3];
                b[0] = ox; b[1] = oy;
                continue;
            }
            if (b[4] == 1 && Math.hypot(b[0] - ax, b[1] - ay) < 11) { ahp--; score++; bullets.remove(i); PackArcade.boom(); if (ahp <= 0) { PackPuzzle.win(); gameOver(); return; } continue; }
            if (b[4] == 2 && Math.hypot(b[0] - px, b[1] - py) < 11) { php--; bullets.remove(i); PackArcade.boom(); if (php <= 0) { gameOver(); return; } }
        }
    }

    private void tank(float x, float y, float body, float aim, int col) {
        box(x - 10, y - 10, 20, 20, 5, col);
        box(x - 6, y - 6, 12, 12, 3, Gx.mix(col, 0xFFFFFFFF, 0.25f));
        line(x, y, x + (float) Math.cos(aim) * 15, y + (float) Math.sin(aim) * 15, 3.5f, 0xFF2A2E38);
    }

    @Override protected void render() {
        box(0, 0, W, H, 0, 0xFFC8B080);
        for (float[] w : WALLS) brick(w[0], w[1], w[2], w[3], 0xFF8A5A30);
        for (float[] b : bullets) circle(b[0], b[1], 3, b[4] == 1 ? 0xFF006CB7 : 0xFFE3000B);
        tank(px, py, pa, (float) Math.atan2(bmy() - py, bmx() - px), 0xFF006CB7);
        tank(ax, ay, aa, (float) Math.atan2(py - ay, px - ax), 0xFFE3000B);
        for (int k = 0; k < 3; k++) { circle(14 + k * 12, 12, 4, k < php ? 0xFF006CB7 : 0x55000000); circle(W - 14 - k * 12, 12, 4, k < ahp ? 0xFFE3000B : 0x55000000); }
    }
}

// ====================================================================== Blasen-Shooter
final class BubbleShooter extends GameView {
    private static final int COLS = 11, ROWS = 14;
    private static final float R = 13, W = COLS * R * 2 + R, H = 300, LOSE = 260;
    private int[][] g = new int[ROWS][COLS];   // -1 empty
    private float sx, sy, svx, svy;
    private int cur, next, shots;
    private boolean flying;
    private final List<float[]> falling = new ArrayList<>();

    BubbleShooter() { super("bubbles"); }
    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String overTitle() { return clearAll() ? "Alles abgeräumt!" : "Die Blasen sind unten"; }

    private int colors() { return 5; }

    @Override protected void reset() {
        for (int[] r : g) java.util.Arrays.fill(r, -1);
        for (int r = 0; r < 5; r++) for (int c = 0; c < COLS - (r % 2); c++) g[r][c] = rnd.nextInt(colors());
        cur = rnd.nextInt(colors()); next = rnd.nextInt(colors()); flying = false; shots = 0; falling.clear();
    }

    private float cx(int r, int c) { return R + c * R * 2 + (r % 2) * R; }
    private float cy(int r) { return R + r * R * 1.73f; }
    private boolean clearAll() { for (int[] row : g) for (int v : row) if (v >= 0) return false; return true; }

    @Override protected void onClick(float x, float y, int b) {
        if (flying) return;
        float dx = x - W / 2, dy = Math.min(-20, y - (H - 18)), n = (float) Math.hypot(dx, dy);
        sx = W / 2; sy = H - 18; svx = dx / n * 420; svy = dy / n * 420; flying = true;
        Sound.play("minecraft:entity.chicken.egg", 1.2f, 0.3f);
    }

    private List<int[]> neighbors(int r, int c) {
        List<int[]> l = new ArrayList<>();
        int[][] d = r % 2 == 0 ? new int[][]{{0, -1}, {0, 1}, {-1, -1}, {-1, 0}, {1, -1}, {1, 0}} : new int[][]{{0, -1}, {0, 1}, {-1, 0}, {-1, 1}, {1, 0}, {1, 1}};
        for (int[] q : d) { int rr = r + q[0], cc = c + q[1]; if (rr >= 0 && rr < ROWS && cc >= 0 && cc < COLS - (rr % 2)) l.add(new int[]{rr, cc}); }
        return l;
    }

    private void snap() {
        int br = 0, bc = 0; float bd = 1e9f;
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS - (r % 2); c++) if (g[r][c] < 0) {
            boolean attached = r == 0;
            for (int[] n : neighbors(r, c)) if (g[n[0]][n[1]] >= 0) attached = true;
            if (!attached) continue;
            float d = (float) Math.hypot(sx - cx(r, c), sy - cy(r));
            if (d < bd) { bd = d; br = r; bc = c; }
        }
        g[br][bc] = cur;
        flying = false;
        // match
        List<int[]> grp = new ArrayList<>();
        boolean[][] seen = new boolean[ROWS][COLS];
        java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
        q.add(new int[]{br, bc}); seen[br][bc] = true;
        while (!q.isEmpty()) { int[] p = q.poll(); grp.add(p); for (int[] n : neighbors(p[0], p[1])) if (!seen[n[0]][n[1]] && g[n[0]][n[1]] == cur) { seen[n[0]][n[1]] = true; q.add(n); } }
        if (grp.size() >= 3) {
            for (int[] p : grp) { falling.add(new float[]{cx(p[0], p[1]), cy(p[0]), g[p[0]][p[1]], 0}); g[p[0]][p[1]] = -1; }
            score += grp.size() * 10L;
            PackArcade.pop();
            // drop floating
            boolean[][] keep = new boolean[ROWS][COLS];
            q.clear();
            for (int c = 0; c < COLS; c++) if (g[0][c] >= 0) { keep[0][c] = true; q.add(new int[]{0, c}); }
            while (!q.isEmpty()) { int[] p = q.poll(); for (int[] n : neighbors(p[0], p[1])) if (!keep[n[0]][n[1]] && g[n[0]][n[1]] >= 0) { keep[n[0]][n[1]] = true; q.add(n); } }
            for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS - (r % 2); c++) if (g[r][c] >= 0 && !keep[r][c]) { falling.add(new float[]{cx(r, c), cy(r), g[r][c], 0}); g[r][c] = -1; score += 20; }
        }
        shots++;
        if (shots % 7 == 0) {
            for (int r = ROWS - 1; r >= 2; r--) g[r] = g[r - 2].clone();
            for (int r = 0; r < 2; r++) { java.util.Arrays.fill(g[r], -1); for (int c = 0; c < COLS - (r % 2); c++) g[r][c] = rnd.nextInt(colors()); }
        }
        cur = next; next = rnd.nextInt(colors());
        if (clearAll()) { score += 500; PackPuzzle.win(); gameOver(); return; }
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) if (g[r][c] >= 0 && cy(r) > LOSE) { PackPuzzle.bad(); gameOver(); return; }
    }

    @Override protected void update(float dt) {
        for (float[] f : falling) { f[3] += 600 * dt; f[1] += f[3] * dt; }
        falling.removeIf(f -> f[1] > H + 20);
        if (!flying) return;
        for (int s = 0; s < 4; s++) {
            sx += svx * dt / 4; sy += svy * dt / 4;
            if (sx < R) { sx = R; svx = -svx; } if (sx > W - R) { sx = W - R; svx = -svx; }
            boolean hit = sy < R;
            for (int r = 0; r < ROWS && !hit; r++) for (int c = 0; c < COLS - (r % 2); c++) if (g[r][c] >= 0 && Math.hypot(sx - cx(r, c), sy - cy(r)) < R * 1.7f) { hit = true; break; }
            if (hit) { snap(); return; }
        }
    }

    private void bubble(float x, float y, int col) { circle(x, y, R - 1, PackPuzzle.PAL[col]); circle(x - 4, y - 4, 3.5f, 0x88FFFFFF); }

    @Override protected void render() {
        boxV(0, 0, W, H, 0, 0xFF101A30, 0xFF1A2A50);
        box(0, LOSE, W, 1, 0, 0x66FF3B4C);
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS - (r % 2); c++) if (g[r][c] >= 0) bubble(cx(r, c), cy(r), g[r][c]);
        for (float[] f : falling) bubble(f[0], f[1], (int) f[2]);
        if (!flying) { float dx = bmx() - W / 2, dy = Math.min(-20, bmy() - (H - 18)), n = (float) Math.hypot(dx, dy); for (int i = 1; i < 8; i++) circle(W / 2 + dx / n * i * 14, H - 18 + dy / n * i * 14, 1.5f, 0x66FFFFFF); }
        bubble(flying ? sx : W / 2, flying ? sy : H - 18, cur);
        circle(30, H - 14, 8, PackPuzzle.PAL[next]);
        textL("Nächste", 42, H - 14, 6, Fonts.MEDIUM, 0xAAFFFFFF);
    }
}
