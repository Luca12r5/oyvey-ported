package dev.lego.games;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;

/**
 * Lego City Rush: an own top-down city crime game. Drive or walk through a generated brick city, steal cars,
 * finish delivery jobs against the clock, collect coins - and lose the police when your wanted level rises.
 */
public final class CityRush extends GameView {
    private static final float W = 480, H = 300, T = 22;
    private static final int MW = 42, MH = 30;
    private static final int ROAD = 0, BUILD = 1, PARK = 2, WATER = 3;

    private int[][] map;
    private int[][] col;
    private final Set<Integer> keys = new HashSet<>();

    private static final class Car {
        float x, y, ang, speed, hp = 100, stun;
        int color;
        boolean police, player;
        int tx, ty, px, py;      // AI: target tile, previous tile
    }

    private final List<Car> cars = new ArrayList<>();
    private Car mine;                     // car you are driving (null = on foot)
    private float fx, fy, fang;           // on-foot position
    private float heat, bust, flash;
    private final List<float[]> coins = new ArrayList<>();
    private float[] pick, drop;
    private boolean carrying;
    private float jobTime;
    private final List<float[]> fx2 = new ArrayList<>();   // x, y, t, kind
    private boolean bigMap;

    public CityRush() { super("cityrush"); }

    @Override protected float boardW() { return W; }
    @Override protected float boardH() { return H; }
    @Override protected String scoreLabel() { return "Geld"; }
    @Override protected String scoreText() { return "$" + score; }
    @Override protected String overTitle() { return "Verhaftet!"; }

    // ------------------------------------------------------------------ world
    private int tile(float x, float y) {
        int tx = (int) Math.floor(x / T), ty = (int) Math.floor(y / T);
        if (tx < 0 || ty < 0 || tx >= MW || ty >= MH) return BUILD;
        return map[tx][ty];
    }

    private boolean solid(float x, float y, boolean onFoot) {
        int t = tile(x, y);
        return t == BUILD || t == WATER || (!onFoot && t == PARK);
    }

    private boolean road(int tx, int ty) { return tx >= 0 && ty >= 0 && tx < MW && ty < MH && map[tx][ty] == ROAD; }

    private void genMap() {
        map = new int[MW][MH];
        col = new int[MW][MH];
        int[] pal = {0xFFE3000B, 0xFFFFCD03, 0xFF006CB7, 0xFF4B9F4A, 0xFFF4F4F4, 0xFFFF8A00, 0xFF9B4DCA, 0xFF7A5230, 0xFF3FA9FF};
        for (int x = 0; x < MW; x++) for (int y = 0; y < MH; y++) {
            boolean r = x % 6 == 0 || y % 6 == 0 || x == MW - 1 || y == MH - 1;
            map[x][y] = r ? ROAD : BUILD;
        }
        // blocks: some become parks, the rest keep one building colour per block
        for (int bx = 0; bx < MW; bx += 6) for (int by = 0; by < MH; by += 6) {
            boolean park = rnd.nextFloat() < 0.18f;
            int c = pal[rnd.nextInt(pal.length)];
            for (int x = bx + 1; x < Math.min(MW - 1, bx + 6); x++) for (int y = by + 1; y < Math.min(MH - 1, by + 6); y++) {
                if (park) map[x][y] = PARK;
                col[x][y] = rnd.nextFloat() < 0.15f ? pal[rnd.nextInt(pal.length)] : c;
            }
        }
        // a river with bridges where roads cross it
        for (int y = 0; y < MH; y++) for (int x = 21; x <= 22; x++) if (map[x][y] != ROAD || y % 12 != 0) map[x][y] = WATER;
    }

    private float[] randomRoad() {
        for (int i = 0; i < 2000; i++) {
            int x = rnd.nextInt(MW), y = rnd.nextInt(MH);
            if (map[x][y] == ROAD) return new float[]{(x + 0.5f) * T, (y + 0.5f) * T};
        }
        return new float[]{T * 0.5f, T * 0.5f};
    }

    private Car spawnCar(boolean police) {
        Car c = new Car();
        float[] p = randomRoad();
        c.x = p[0]; c.y = p[1];
        c.tx = c.px = (int) (c.x / T); c.ty = c.py = (int) (c.y / T);
        int[] pal = {0xFFE3000B, 0xFF006CB7, 0xFFFFCD03, 0xFFF4F4F4, 0xFF1E1E22, 0xFF4B9F4A, 0xFFFF6FB5, 0xFFFF8A00};
        c.color = police ? 0xFFF4F4F4 : pal[rnd.nextInt(pal.length)];
        c.police = police;
        cars.add(c);
        return c;
    }

    private void newJob() {
        pick = randomRoad();
        do { drop = randomRoad(); } while (Math.hypot(drop[0] - pick[0], drop[1] - pick[1]) < 300);
        carrying = false;
        jobTime = 0;
    }

    @Override
    protected void reset() {
        genMap();
        cars.clear();
        coins.clear();
        fx2.clear();
        keys.clear();
        for (int i = 0; i < 22; i++) spawnCar(false);
        for (int i = 0; i < 26; i++) { float[] p = randomRoad(); coins.add(new float[]{p[0] + (rnd.nextFloat() - 0.5f) * 8, p[1] + (rnd.nextFloat() - 0.5f) * 8}); }
        // start on a long straight street inside the city, car pointing along the road
        int sx = 6 * (1 + rnd.nextInt(MW / 6 - 2)), sy = 6 * (1 + rnd.nextInt(MH / 6 - 2)) + 3;
        mine = spawnCar(false);
        mine.x = (sx + 0.5f) * T; mine.y = (sy + 0.5f) * T;
        mine.ang = (float) (Math.PI / 2);
        mine.player = true;
        fx = mine.x - 14; fy = mine.y;
        heat = 0; bust = 0;
        newJob();
    }

    // ------------------------------------------------------------------ input
    @Override protected void onKey(int k) {
        keys.add(k);
        if (k == 70) toggleCar();          // F
        if (k == 77) bigMap = !bigMap;     // M
    }

    @Override protected void onKeyUp(int k) { keys.remove(k); }

    private boolean down(int a, int b) { return keys.contains(a) || keys.contains(b); }

    private void toggleCar() {
        if (mine != null) {
            // get out next to the car
            float sx = (float) -Math.sin(mine.ang) * 14, sy = (float) Math.cos(mine.ang) * 14;
            fx = mine.x + sx; fy = mine.y + sy;
            if (solid(fx, fy, true)) { fx = mine.x - sx; fy = mine.y - sy; }
            mine.player = false;
            mine.speed = 0;
            mine.stun = 9999;               // parked
            mine = null;
            return;
        }
        Car best = null;
        double bd = 26;
        for (Car c : cars) { double d = Math.hypot(c.x - fx, c.y - fy); if (d < bd && !c.police) { bd = d; best = c; } }
        if (best != null) {
            if (best.stun < 9000 && best.speed > 1) heat = Math.min(5, heat + 0.6f);   // stealing a moving car is noticed
            mine = best;
            mine.player = true;
            mine.stun = 0;
            dev.lego.ui.Sound.play("minecraft:block.iron_door.close", 1.4f, 0.3f);
        }
    }

    // ------------------------------------------------------------------ update
    @Override
    protected void update(float dt) {
        flash += dt;
        for (float[] f : fx2) f[2] += dt;
        fx2.removeIf(f -> f[2] > 0.8f);
        float px, py;
        if (mine != null) {
            drivePlayer(mine, dt);
            px = mine.x; py = mine.y;
            if (mine.hp <= 0) {
                fx2.add(new float[]{mine.x, mine.y, 0, 1});
                cars.remove(mine);
                fx = mine.x; fy = mine.y;
                mine = null;
                heat = Math.min(5, heat + 0.5f);
            }
        } else {
            walk(dt);
            px = fx; py = fy;
        }
        for (Car c : new ArrayList<>(cars)) {
            if (c.player) continue;
            if (c.police) police(c, px, py, dt); else traffic(c, dt);
        }
        collisions(px, py);
        // coins
        for (float[] c : coins) {
            if (Math.hypot(c[0] - px, c[1] - py) < 12) {
                score += 10;
                fx2.add(new float[]{c[0], c[1], 0, 0});
                float[] p = randomRoad();
                c[0] = p[0]; c[1] = p[1];
                dev.lego.ui.Sound.play("minecraft:entity.experience_orb.pickup", 1.4f, 0.25f);
            }
        }
        // delivery job
        jobTime += dt;
        if (!carrying && Math.hypot(pick[0] - px, pick[1] - py) < 16) { carrying = true; jobTime = 0; dev.lego.ui.Sound.play("minecraft:entity.item.pickup", 1f, 0.4f); }
        if (carrying) {
            float limit = limit();
            if (Math.hypot(drop[0] - px, drop[1] - py) < 16) {
                score += 100 + Math.max(0, Math.round((limit - jobTime) * 6));
                dev.lego.ui.Sound.play("minecraft:ui.toast.challenge_complete", 1.3f, 0.3f);
                newJob();
            } else if (jobTime > limit) newJob();
        }
        // wanted level: police arrive, fade when you are far away from them
        int stars = (int) Math.ceil(heat);
        int cops = 0;
        double nearest = 9999;
        for (Car c : cars) if (c.police) { cops++; nearest = Math.min(nearest, Math.hypot(c.x - px, c.y - py)); }
        if (cops < Math.min(4, stars) && rnd.nextFloat() < dt * 0.6f) {
            Car c = spawnCar(true);
            if (Math.hypot(c.x - px, c.y - py) < 180) cars.remove(c);
        }
        if (nearest > 220 || stars == 0) heat = Math.max(0, heat - dt * 0.08f);
        if (stars == 0) cars.removeIf(c -> c.police && Math.hypot(c.x - px, c.y - py) > 260);
        float mySpeed = mine == null ? 0 : Math.abs(mine.speed);
        if (stars > 0 && nearest < 20 && mySpeed < 18) bust += dt; else bust = Math.max(0, bust - dt);
        if (bust > 1.6f) gameOver();
    }

    private float limit() { return 20 + (float) Math.hypot(drop[0] - pick[0], drop[1] - pick[1]) / 22f; }

    private void drivePlayer(Car c, float dt) {
        boolean gas = down(K_W, K_UP), brake = down(K_S, K_DOWN), left = down(K_A, K_LEFT), right = down(K_D, K_RIGHT), hand = keys.contains(K_SPACE);
        if (gas) c.speed += 150 * dt;
        if (brake) c.speed -= (c.speed > 0 ? 260 : 90) * dt;
        c.speed *= (float) Math.pow(hand ? 0.25 : 0.55, dt);
        c.speed = Math.max(-50, Math.min(190, c.speed));
        float steer = (left ? -1 : 0) + (right ? 1 : 0);
        c.ang += steer * dt * 2.8f * Math.min(1, Math.abs(c.speed) / 60f) * Math.signum(c.speed == 0 ? 1 : c.speed) * (hand ? 1.6f : 1f);
        move(c, dt, false);
    }

    private void move(Car c, float dt, boolean ai) {
        float nx = c.x + (float) Math.cos(c.ang) * c.speed * dt, ny = c.y + (float) Math.sin(c.ang) * c.speed * dt;
        float fr = 9 * Math.signum(c.speed == 0 ? 1 : c.speed);
        if (!solid(nx + (float) Math.cos(c.ang) * fr, ny + (float) Math.sin(c.ang) * fr, false)) { c.x = nx; c.y = ny; return; }
        if (!ai && Math.abs(c.speed) > 40) {
            c.hp -= Math.abs(c.speed) * 0.12f;
            fx2.add(new float[]{c.x + (float) Math.cos(c.ang) * 10, c.y + (float) Math.sin(c.ang) * 10, 0, 2});
            dev.lego.ui.Sound.play("minecraft:entity.generic.explode", 1.8f, 0.15f);
        }
        // slide along the wall instead of sticking
        if (!solid(nx, c.y, false)) c.x = nx; else if (!solid(c.x, ny, false)) c.y = ny;
        c.speed *= -0.25f;
    }

    private void walk(float dt) {
        float vx = (down(K_D, K_RIGHT) ? 1 : 0) - (down(K_A, K_LEFT) ? 1 : 0), vy = (down(K_S, K_DOWN) ? 1 : 0) - (down(K_W, K_UP) ? 1 : 0);
        if (vx == 0 && vy == 0) return;
        float l = (float) Math.hypot(vx, vy);
        float sp = keys.contains(K_SPACE) ? 95 : 60;
        float nx = fx + vx / l * sp * dt, ny = fy + vy / l * sp * dt;
        fang = (float) Math.atan2(vy, vx);
        if (!solid(nx, fy, true)) fx = nx;
        if (!solid(fx, ny, true)) fy = ny;
    }

    private void traffic(Car c, float dt) {
        if (c.stun > 0) { c.stun -= dt; c.speed *= 0.9f; return; }
        float tx = (c.tx + 0.5f) * T, ty = (c.ty + 0.5f) * T;
        if (Math.hypot(tx - c.x, ty - c.y) < 2.5) {
            List<int[]> next = new ArrayList<>();
            int cx = c.tx, cy = c.ty;
            int[][] d = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            int sx = cx - c.px, sy = cy - c.py;
            for (int[] o : d) {
                if (!road(cx + o[0], cy + o[1]) || (cx + o[0] == c.px && cy + o[1] == c.py)) continue;
                next.add(o);
                if (o[0] == sx && o[1] == sy && rnd.nextFloat() < 0.6f) { next.clear(); next.add(o); break; }
            }
            int[] o = next.isEmpty() ? new int[]{-sx, -sy} : next.get(rnd.nextInt(next.size()));
            c.px = cx; c.py = cy;
            c.tx = cx + o[0]; c.ty = cy + o[1];
            if (!road(c.tx, c.ty)) { c.tx = cx; c.ty = cy; }
            tx = (c.tx + 0.5f) * T; ty = (c.ty + 0.5f) * T;
        }
        float want = (float) Math.atan2(ty - c.y, tx - c.x);
        c.ang = turn(c.ang, want, dt * 6);
        float target = 55;
        // brake for whatever is in front (also you)
        for (Car o : cars) {
            if (o == c) continue;
            float dx = o.x - c.x, dy = o.y - c.y;
            float ahead = dx * (float) Math.cos(c.ang) + dy * (float) Math.sin(c.ang);
            float side = Math.abs(-dx * (float) Math.sin(c.ang) + dy * (float) Math.cos(c.ang));
            if (ahead > 0 && ahead < 26 && side < 9) target = 0;
        }
        c.speed += (target - c.speed) * Math.min(1, dt * 3);
        c.x += (float) Math.cos(c.ang) * c.speed * dt;
        c.y += (float) Math.sin(c.ang) * c.speed * dt;
    }

    private void police(Car c, float px, float py, float dt) {
        if (c.stun > 0) { c.stun -= dt; c.speed *= 0.9f; move(c, dt, true); return; }
        float want = (float) Math.atan2(py - c.y, px - c.x);
        // feel around buildings: try the direct line, then +-45 / +-90 degrees
        float[] tries = {0, 0.8f, -0.8f, 1.6f, -1.6f};
        float best = want;
        for (float t : tries) {
            float a = want + t;
            if (!solid(c.x + (float) Math.cos(a) * 22, c.y + (float) Math.sin(a) * 22, false)) { best = a; break; }
        }
        c.ang = turn(c.ang, best, dt * 4);
        float dist = (float) Math.hypot(px - c.x, py - c.y);
        c.speed += ((dist < 30 ? 40 : 135) - c.speed) * Math.min(1, dt * 2);
        move(c, dt, true);
    }

    private static float turn(float a, float b, float max) {
        float d = (float) Math.atan2(Math.sin(b - a), Math.cos(b - a));
        return a + Math.max(-max, Math.min(max, d));
    }

    private void collisions(float px, float py) {
        for (Car c : cars) {
            if (c == mine) continue;
            if (mine != null) {
                double d = Math.hypot(c.x - mine.x, c.y - mine.y);
                if (d < 15) {
                    float imp = Math.abs(mine.speed);
                    if (imp > 25) {
                        mine.hp -= imp * 0.08f;
                        c.stun = 2.5f;
                        c.hp -= imp * 0.2f;
                        if (!c.police) heat = Math.min(5, heat + imp / 260f); else heat = Math.min(5, heat + 0.4f);
                        fx2.add(new float[]{(c.x + mine.x) / 2, (c.y + mine.y) / 2, 0, 2});
                        dev.lego.ui.Sound.play("minecraft:entity.generic.explode", 1.6f, 0.18f);
                    }
                    float nx = (float) ((mine.x - c.x) / Math.max(0.01, d)), ny = (float) ((mine.y - c.y) / Math.max(0.01, d));
                    mine.x += nx * 3; mine.y += ny * 3;
                    mine.speed *= 0.4f;
                }
            } else if (Math.hypot(c.x - px, c.y - py) < 9 && Math.abs(c.speed) > 30 && !c.police) {
                // jump aside
                fx += (float) -Math.sin(c.ang) * 10; fy += (float) Math.cos(c.ang) * 10;
            }
        }
        cars.removeIf(c -> c.hp <= 0 && c != mine && !c.player);
    }

    // ------------------------------------------------------------------ render
    @Override
    protected void render() {
        float camX = mine != null ? mine.x : fx, camY = mine != null ? mine.y : fy;
        if (mine != null) { camX += (float) Math.cos(mine.ang) * mine.speed * 0.25f; camY += (float) Math.sin(mine.ang) * mine.speed * 0.25f; }
        // never show the void outside the map
        camX = Math.max(W / 2, Math.min(MW * T - W / 2, camX));
        camY = Math.max(H / 2, Math.min(MH * T - H / 2, camY));
        float ox = W / 2 - camX, oy = H / 2 - camY;
        Gx.clip(X(0), Y(0), S(W), S(H));
        int x0 = Math.max(0, (int) ((camX - W / 2) / T) - 1), x1 = Math.min(MW - 1, (int) ((camX + W / 2) / T) + 1);
        int y0 = Math.max(0, (int) ((camY - H / 2) / T) - 1), y1 = Math.min(MH - 1, (int) ((camY + H / 2) / T) + 1);
        // road colour is the background; only blocks, parks and water are drawn (cheap, flat shapes)
        box(0, 0, W, H, 0, 0xFF3A3D45);
        // sidewalks: one rectangle per city block instead of one per tile
        for (int bx6 = (x0 / 6) * 6; bx6 <= x1; bx6 += 6) for (int by6 = (y0 / 6) * 6; by6 <= y1; by6 += 6) {
            if (bx6 + 1 >= MW || by6 + 1 >= MH) continue;
            int ex = Math.min(MW - 1, bx6 + 6), ey = Math.min(MH - 1, by6 + 6);
            box((bx6 + 1) * T + ox - 2, (by6 + 1) * T + oy - 2, (ex - bx6 - 1) * T + 4, (ey - by6 - 1) * T + 4, 0, 0xFF6C6F78);
        }
        for (int tx = x0; tx <= x1; tx++) for (int ty = y0; ty <= y1; ty++) {
            float sx = tx * T + ox, sy = ty * T + oy;
            switch (map[tx][ty]) {
                case ROAD:
                    if (tx % 6 != 0 && ty % 6 == 0 && tx % 2 == 0) box(sx + 2, sy + T / 2 - 0.6f, T - 4, 1.2f, 0, 0xCCFFE14D);
                    if (ty % 6 != 0 && tx % 6 == 0 && ty % 2 == 0) box(sx + T / 2 - 0.6f, sy + 2, 1.2f, T - 4, 0, 0xCCFFE14D);
                    break;
                case PARK:
                    box(sx, sy, T, T, 0, 0xFF3E8E41);
                    if ((tx * 7 + ty * 13) % 5 == 0) circle(sx + T / 2, sy + T / 2, 6, 0xFF2C6B2F);
                    break;
                case WATER:
                    box(sx, sy, T, T, 0, ((tx + ty + (int) (time * 2)) & 1) == 0 ? 0xFF1F6FB8 : 0xFF2A7CC4);
                    break;
                default: {
                    int c = col[tx][ty];
                    box(sx + 1, sy + 1, T - 2, T - 2, 0, Gx.mix(c, 0xFF000000, 0.18f));
                    box(sx + 1, sy + 1, T - 2, T - 4, 0, c);
                    int st = Gx.mix(c, 0xFFFFFFFF, 0.25f);
                    box(sx + T * 0.22f, sy + T * 0.3f, 4, 4, 0, st);
                    box(sx + T * 0.6f, sy + T * 0.3f, 4, 4, 0, st);
                }
            }
        }        // coins, job markers
        for (float[] c : coins) {
            float b = (float) Math.sin(time * 5 + c[0]) * 1.2f;
            circle(c[0] + ox, c[1] + oy + b, 4, 0xFFFFCD03);
            circle(c[0] + ox - 1, c[1] + oy + b - 1, 2, 0xFFFFF2A0);
        }
        float[] goal = carrying ? drop : pick;
        float pulse = 10 + 3 * (float) Math.sin(time * 6);
        circle(goal[0] + ox, goal[1] + oy, pulse, carrying ? 0x6633D17A : 0x66FFB02E);
        circle(goal[0] + ox, goal[1] + oy, 5, carrying ? 0xFF33D17A : 0xFFFFB02E);
        // cars
        for (Car c : cars) drawCar(c, c.x + ox, c.y + oy);
        // you on foot
        if (mine == null) {
            circle(fx + ox + 1, fy + oy + 1.5f, 5, 0x55000000);
            circle(fx + ox, fy + oy, 5, 0xFF006CB7);
            circle(fx + ox + (float) Math.cos(fang) * 1.5f, fy + oy + (float) Math.sin(fang) * 1.5f, 3.2f, 0xFFFFCD9E);
            for (Car c : cars) if (!c.police && Math.hypot(c.x - fx, c.y - fy) < 26) { text("F", c.x + ox, c.y + oy - 16, 7, Fonts.BOLD, 0xFFFFFFFF); break; }
        }
        // effects
        for (float[] f : fx2) {
            float a = 1 - f[2] / 0.8f;
            if (f[3] == 0) text("+$10", f[0] + ox, f[1] + oy - f[2] * 30, 7, Fonts.BOLD, Gx.withAlpha(0xFFFFCD03, a));
            else circle(f[0] + ox, f[1] + oy, (f[3] == 1 ? 22 : 9) * (0.4f + f[2]), Gx.withAlpha(f[3] == 1 ? 0xFFFF6A2A : 0xFFFFD27A, a * 0.8f));
        }
        // off-screen arrow to the goal
        float gx = goal[0] + ox, gy = goal[1] + oy;
        if (gx < 10 || gy < 10 || gx > W - 10 || gy > H - 10) {
            float ang = (float) Math.atan2(gy - H / 2, gx - W / 2);
            float ax = W / 2 + (float) Math.cos(ang) * (H / 2 - 22), ay = H / 2 + (float) Math.sin(ang) * (H / 2 - 22);
            circle(ax, ay, 6, carrying ? 0xFF33D17A : 0xFFFFB02E);
            circle(ax + (float) Math.cos(ang) * 5, ay + (float) Math.sin(ang) * 5, 3, 0xFFFFFFFF);
        }
        hud();
        Gx.unclip();
    }

    private void drawCar(Car c, float x, float y) {
        float cs = (float) Math.cos(c.ang), sn = (float) Math.sin(c.ang);
        for (int k = -1; k <= 1; k++) circle(x + cs * k * 6 + 1.2f, y + sn * k * 6 + 1.8f, 6.2f, 0x55000000);
        int body = c.hp < 30 ? Gx.mix(c.color, 0xFF222222, 0.5f) : c.color;
        for (int k = -1; k <= 1; k++) circle(x + cs * k * 6, y + sn * k * 6, 6.2f, body);
        circle(x - cs * 1.5f, y - sn * 1.5f, 4.4f, Gx.mix(body, 0xFF000000, 0.35f));       // roof
        circle(x + cs * 3.5f, y + sn * 3.5f, 3f, 0xFF9FD8FF);                               // windscreen
        float lx = -sn * 3.6f, ly = cs * 3.6f;
        circle(x + cs * 11 + lx, y + sn * 11 + ly, 1.7f, 0xFFFFF6C8);
        circle(x + cs * 11 - lx, y + sn * 11 - ly, 1.7f, 0xFFFFF6C8);
        if (c.police) {
            boolean a = ((int) (flash * 8)) % 2 == 0;
            circle(x - cs * 1.5f + lx * 0.6f, y - sn * 1.5f + ly * 0.6f, 2f, a ? 0xFFFF3B3B : 0xFF3B6BFF);
            circle(x - cs * 1.5f - lx * 0.6f, y - sn * 1.5f - ly * 0.6f, 2f, a ? 0xFF3B6BFF : 0xFFFF3B3B);
        }
        if (c.player) circle(x, y - 13, 2, 0xFFFFFFFF);
    }

    private void hud() {
        // speedometer (real km/h of the car you drive)
        if (mine != null) {
            int kmh = Math.round(Math.abs(mine.speed) * 1.2f);
            box(W - 86, H - 40, 78, 32, 8, 0xC0101116);
            text(kmh + " km/h", W - 47, H - 30, 9, Fonts.BOLD, 0xFFFFFFFF);
            box(W - 80, H - 18, 66, 4, 2, 0x33FFFFFF);
            box(W - 80, H - 18, 66 * Math.min(1, kmh / 228f), 4, 2, kmh > 160 ? 0xFFFF4D5E : 0xFF1ED760);
            // damage
            box(8, H - 18, 70, 6, 3, 0x33FFFFFF);
            box(8, H - 18, 70 * Math.max(0, mine.hp) / 100f, 6, 3, mine.hp > 40 ? 0xFF1ED760 : 0xFFFF4D5E);
            text("Auto", 43, H - 26, 6, Fonts.MEDIUM, 0xAAFFFFFF);
        } else text("Zu Fuß · F = einsteigen", 70, H - 16, 6.5f, Fonts.MEDIUM, 0xCCFFFFFF);
        // wanted stars
        int stars = (int) Math.ceil(heat);
        for (int i = 0; i < 5; i++) { float sx = W - 74 + i * 14; circle(sx, 14, 5, i < stars ? 0xFFFFCD03 : 0x33FFFFFF); if (i < stars) circle(sx - 1, 13, 2, 0xFFFFF2A0); }
        if (bust > 0.2f) text("Polizei hält dich fest …", W / 2, 40, 9, Fonts.BOLD, 0xFFFF4D5E);
        // job
        String job = carrying ? String.format(java.util.Locale.ROOT, "Liefern! %.0f s", Math.max(0, limit() - jobTime)) : "Paket abholen";
        box(W / 2 - 60, 6, 120, 18, 9, 0xC0101116);
        text(job, W / 2, 15, 7.5f, Fonts.BOLD, carrying ? 0xFF33D17A : 0xFFFFB02E);
        minimap();
    }

    private void minimap() {
        float s = bigMap ? 5f : 1.6f;
        float mw = MW * s, mh = MH * s;
        float mx = bigMap ? (W - mw) / 2 : 8, my = bigMap ? (H - mh) / 2 : 8;
        box(mx - 2, my - 2, mw + 4, mh + 4, 4, 0xD0101116);
        for (int x = 0; x < MW; x += 6) box(mx + x * s + s * 0.2f, my, s * 0.6f, mh, 0, 0x55FFFFFF);
        for (int y = 0; y < MH; y += 6) box(mx, my + y * s + s * 0.2f, mw, s * 0.6f, 0, 0x55FFFFFF);
        box(mx + 21 * s, my, 2 * s, mh, 0, 0x883FA9FF);
        float[] goal = carrying ? drop : pick;
        circle(mx + goal[0] / T * s, my + goal[1] / T * s, bigMap ? 4 : 2.2f, carrying ? 0xFF33D17A : 0xFFFFB02E);
        for (Car c : cars) if (c.police) circle(mx + c.x / T * s, my + c.y / T * s, bigMap ? 3 : 1.6f, 0xFFFF3B3B);
        float px = mine != null ? mine.x : fx, py = mine != null ? mine.y : fy;
        circle(mx + px / T * s, my + py / T * s, bigMap ? 4 : 2.2f, 0xFFFFFFFF);
        if (bigMap) text("M = Karte schließen", W / 2, my + mh + 10, 7, Fonts.MEDIUM, 0xCCFFFFFF);
    }
}