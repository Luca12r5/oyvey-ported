package dev.lego.games;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;

/**
 * Wagen-Abfahrt: an own pseudo-3D downhill cart race. Steer a Lego shopping cart down an endless hilly road,
 * jump over ramps for stunt points, collect studs - and don't crash into cones and crates.
 */
public final class CartHill extends GameView {
    private static final float W = 480, H = 300;
    private static final int SEG = 200, DRAW = 140, LANES = 3;
    private static final float ROAD = 1900, CAM_H = 950, DEPTH = 0.84f;

    private static final class Seg { float curve, y; int kind = -1; float ox; boolean taken; }

    private final List<Seg> road = new ArrayList<>();
    private final Set<Integer> keys = new HashSet<>();
    private float pos, speed, px, air, vy, crash, stunt, lastStunt;
    private int lives;
    private float shake;

    public CartHill() { super("carthill"); }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Punkte"; }
    @Override protected String overTitle() { return "Totalschaden!"; }

    private Seg seg(int i) {
        while (road.size() <= i + DRAW + 2) grow();
        return road.get(i);
    }

    private void grow() {
        // sections: straight / curve / hill, each 30-70 segments, with obstacles getting denser over time
        int n = 30 + rnd.nextInt(40);
        float curve = rnd.nextFloat() < 0.6f ? (rnd.nextFloat() - 0.5f) * 5f : 0;
        float hill = rnd.nextFloat() < 0.5f ? (rnd.nextFloat() - 0.4f) * 2600 : 0;
        float y0 = road.isEmpty() ? 0 : road.get(road.size() - 1).y;
        int base = road.size();
        for (int i = 0; i < n; i++) {
            Seg s = new Seg();
            float t = i / (float) n;
            float e = (float) (0.5 - 0.5 * Math.cos(Math.PI * t));
            s.curve = curve * (float) Math.sin(Math.PI * t);
            s.y = y0 + hill * e - (base + i) * 6f;           // always slightly downhill
            int idx = base + i;
            if (idx > 40 && i % 6 == 0) {
                float r = rnd.nextFloat();
                float density = Math.min(0.75f, 0.35f + idx / 6000f);
                if (r < density) {
                    s.kind = r < density * 0.45f ? 0 : r < density * 0.7f ? 1 : r < density * 0.85f ? 2 : 3;   // 0 coin, 1 cone, 2 crate, 3 ramp
                    s.ox = (rnd.nextInt(LANES) - 1) * 0.62f;
                }
            }
            road.add(s);
        }
    }

    @Override
    protected void reset() {
        road.clear();
        keys.clear();
        pos = 0; speed = 0; px = 0; air = 0; vy = 0; crash = 0; stunt = 0; lastStunt = 0; shake = 0;
        lives = 3;
        seg(0);
    }

    @Override protected void onKey(int k) { keys.add(k); }
    @Override protected void onKeyUp(int k) { keys.remove(k); }
    private boolean down(int a, int b) { return keys.contains(a) || keys.contains(b); }

    @Override
    protected void update(float dt) {
        shake = Math.max(0, shake - dt * 3);
        lastStunt = Math.max(0, lastStunt - dt);
        float max = 9000 + Math.min(5000, pos / 400);
        if (crash > 0) {
            crash -= dt;
            speed *= (float) Math.pow(0.2, dt);
        } else {
            if (down(K_W, K_UP)) speed += 4200 * dt; else speed += 1400 * dt;     // downhill: it rolls on its own
            if (down(K_S, K_DOWN)) speed -= 6000 * dt;
            float steer = (down(K_D, K_RIGHT) ? 1 : 0) - (down(K_A, K_LEFT) ? 1 : 0);
            px += steer * dt * 2.2f * Math.min(1, speed / 4000f) * (air > 0 ? 0.4f : 1);
        }
        Seg cur = seg((int) (pos / SEG));
        px -= cur.curve * dt * (speed / max) * 0.55f;                                // the curve pushes you outwards
        if (Math.abs(px) > 1.05f && air <= 0) speed *= (float) Math.pow(0.35, dt);   // off the road: snow slows you down
        px = Math.max(-2.2f, Math.min(2.2f, px));
        speed = Math.max(0, Math.min(max, speed));
        pos += speed * dt;
        score = (long) (pos / 100) + (long) stunt;
        // jump physics
        if (air > 0 || vy > 0) {
            vy -= 2600 * dt;
            air += vy * dt;
            stunt += dt * 40;
            if (air <= 0) {
                air = 0; vy = 0;
                if (lastStunt <= 0) lastStunt = 1.2f;
                dev.lego.ui.Sound.play("minecraft:block.anvil.land", 1.6f, 0.15f);
            }
        }
        // what is right in front of the cart
        for (int k = 1; k <= 2; k++) {
            Seg s = seg((int) (pos / SEG) + k);
            if (s.kind < 0 || s.taken || air > 60) continue;
            if (Math.abs(s.ox - px) > 0.28f) continue;
            float dz = (int) (pos / SEG + k) * SEG - pos;
            if (dz > SEG * 1.2f) continue;
            s.taken = true;
            switch (s.kind) {
                case 0 -> { stunt += 25; dev.lego.ui.Sound.play("minecraft:entity.experience_orb.pickup", 1.5f, 0.25f); }
                case 3 -> { vy = 900 + speed * 0.06f; air = 1; dev.lego.ui.Sound.play("minecraft:entity.firework_rocket.launch", 1.2f, 0.3f); }
                default -> {
                    crash = 1.1f; shake = 1; speed *= 0.25f; lives--;
                    dev.lego.ui.Sound.play("minecraft:entity.generic.explode", 1.4f, 0.3f);
                    if (lives <= 0) gameOver();
                }
            }
        }
    }

    // ------------------------------------------------------------------ render
    private float[] project(float x, float y, float z, float camX, float camY, float camZ) {
        float dz = Math.max(1, z - camZ);
        float sc = DEPTH / dz;
        return new float[]{W / 2 + sc * (x - camX) * W / 2, H / 2 - sc * (y - camY) * H / 2, sc * ROAD * W / 2};
    }

    @Override
    protected void render() {
        Gx.clip(X(0), Y(0), S(W), S(H));
        float sx0 = shake > 0 ? (float) Math.sin(time * 60) * 4 * shake : 0;
        // sky + mountains
        boxV(0, 0, W, H, 0, 0xFF4F8FD8, 0xFFBFE3FF);
        for (int i = 0; i < 7; i++) {
            float mx = ((i * 97 - pos * 0.004f) % 560 + 560) % 560 - 60;
            box(mx, H / 2 - 40 - (i % 3) * 12, 90, 60 + (i % 3) * 12, 30, 0xFF8FA9C9);
            box(mx + 30, H / 2 - 40 - (i % 3) * 12, 30, 12, 6, 0xFFF4F8FF);
        }
        int base = (int) (pos / SEG);
        float pct = (pos % SEG) / SEG;
        Seg b = seg(base);
        float camY = b.y + (seg(base + 1).y - b.y) * pct + CAM_H + air;
        float camX = px * ROAD;
        float camZ = pos - 0;
        float x = 0, dx = -b.curve * pct;
        float maxY = H;
        float[][] proj = new float[DRAW][];
        float[] segX = new float[DRAW];
        for (int n = 0; n < DRAW; n++) {
            int i = base + n;
            Seg s = seg(i);
            float z1 = i * (float) SEG, z2 = (i + 1) * (float) SEG;
            float[] p1 = project(x, s.y, z1, camX, camY, camZ);
            x += dx; dx += s.curve * 22;
            float[] p2 = project(x, seg(i + 1).y, z2, camX, camY, camZ);
            segX[n] = x;
            proj[n] = p1;
            if (p1[1] <= p2[1] || p2[1] >= maxY) continue;
            float top = p2[1], bot = Math.min(p1[1], maxY);
            boolean alt = (i / 3) % 2 == 0;
            box(0, top, W, bot - top + 1, 0, alt ? 0xFFEFF6FF : 0xFFE1EDFB);                         // snow
            float rw = p2[2] * 1.15f;
            box(p2[0] - rw + sx0, top, rw * 2, bot - top + 1, 0, alt ? 0xFFE3000B : 0xFFFFFFFF);       // rumble
            box(p2[0] - p2[2] + sx0, top, p2[2] * 2, bot - top + 1, 0, alt ? 0xFF4A4E58 : 0xFF52565F); // road
            if (alt) {
                float lw = Math.max(1, p2[2] * 0.03f);
                box(p2[0] - p2[2] / 3 - lw / 2 + sx0, top, lw, bot - top + 1, 0, 0xCCFFFFFF);
                box(p2[0] + p2[2] / 3 - lw / 2 + sx0, top, lw, bot - top + 1, 0, 0xCCFFFFFF);
            }
            maxY = top;
        }
        // obstacles back to front
        for (int n = DRAW - 1; n >= 1; n--) {
            Seg s = seg(base + n);
            if (s.kind < 0 || s.taken || proj[n] == null) continue;
            float[] p = proj[n];
            float sc = p[2] / (ROAD * W / 2);
            float ox = p[0] + s.ox * p[2] + sx0, oy = p[1];
            if (oy > H + 20 || oy < 0) continue;
            float u = Math.max(0.6f, p[2] * 0.12f);
            switch (s.kind) {
                case 0 -> { circle(ox, oy - u * 1.2f, u * 0.7f, 0xFFFFCD03); circle(ox - u * 0.2f, oy - u * 1.4f, u * 0.3f, 0xFFFFF2A0); }
                case 1 -> { box(ox - u * 0.6f, oy - u * 0.3f, u * 1.2f, u * 0.3f, 0, 0xFFFF6A00); box(ox - u * 0.35f, oy - u * 1.6f, u * 0.7f, u * 1.3f, u * 0.2f, 0xFFFF8A00); box(ox - u * 0.36f, oy - u * 1.05f, u * 0.72f, u * 0.25f, 0, 0xFFFFFFFF); }
                case 2 -> { box(ox - u, oy - u * 1.8f, u * 2, u * 1.8f, u * 0.15f, 0xFF9A6A3A); box(ox - u, oy - u * 1.0f, u * 2, u * 0.2f, 0, 0xFF6E4A28); }
                default -> { box(ox - u * 1.4f, oy - u * 0.8f, u * 2.8f, u * 0.8f, u * 0.2f, 0xFF3FA9FF); box(ox - u * 1.4f, oy - u * 0.8f, u * 2.8f, u * 0.2f, 0, 0xFFFFFFFF); text("▲", ox, oy - u * 0.4f, Math.max(4, u * 0.5f), Fonts.BOLD, 0xFFFFFFFF); }
            }
        }
        drawCart(W / 2 + sx0, H - 34 - air * 0.05f);
        hud();
        Gx.unclip();
    }

    private void drawCart(float cx, float cy) {
        float lean = ((down(K_D, K_RIGHT) ? 1 : 0) - (down(K_A, K_LEFT) ? 1 : 0)) * 4;
        boolean blink = crash > 0 && ((int) (time * 14)) % 2 == 0;
        circle(cx, cy + 18 + air * 0.05f, 22 - Math.min(14, air * 0.02f), 0x44000000);                      // shadow
        int body = blink ? 0xFFFFFFFF : 0xFFE3000B;
        box(cx - 24 + lean, cy - 6, 48, 20, 4, Gx.mix(body, 0xFF000000, 0.25f));
        box(cx - 22 + lean, cy - 8, 44, 18, 4, body);
        for (int i = 0; i < 5; i++) box(cx - 18 + i * 9 + lean, cy - 6, 2, 14, 0, 0x66FFFFFF);              // basket bars
        box(cx - 10 + lean * 1.4f, cy - 26, 20, 18, 6, 0xFFFFCD03);                                          // minifig body
        circle(cx + lean * 1.6f, cy - 32, 7, 0xFFFFD27A);                                                      // head
        circle(cx - 2 + lean * 1.6f, cy - 33, 1.2f, 0xFF111111);
        circle(cx + 2 + lean * 1.6f, cy - 33, 1.2f, 0xFF111111);
        circle(cx - 18, cy + 14, 4, 0xFF1E1E22);
        circle(cx + 18, cy + 14, 4, 0xFF1E1E22);
    }

    private void hud() {
        int kmh = Math.round(speed / 70f);
        box(W - 92, H - 40, 84, 32, 8, 0xC0101116);
        text(kmh + " km/h", W - 50, H - 30, 9, Fonts.BOLD, 0xFFFFFFFF);
        box(W - 86, H - 18, 72, 4, 2, 0x33FFFFFF);
        box(W - 86, H - 18, 72 * Math.min(1, speed / 14000f), 4, 2, 0xFF1ED760);
        box(8, 8, 94, 22, 8, 0xC0101116);
        text(Math.round(pos / 100) + " m", 55, 19, 8, Fonts.BOLD, 0xFFFFFFFF);
        for (int i = 0; i < 3; i++) circle(W - 20 - i * 14, 18, 5, i < lives ? 0xFFFF4D5E : 0x33FFFFFF);
        if (air > 40) text("Sprung! +" + Math.round(stunt), W / 2, 40, 10, Fonts.BOLD, 0xFFFFCD03);
        else if (lastStunt > 0) text("Gelandet!", W / 2, 40, 9, Fonts.BOLD, Gx.withAlpha(0xFF1ED760, Math.min(1, lastStunt)));
        if (crash > 0) text("Crash!", W / 2, 60, 14, Fonts.BOLD, 0xFFFF4D5E);
    }
}