package dev.lego.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Cinematic start animation with real 3D bricks (see {@link BrickModel}):
 *  0.0-2.2  darkness, a spotlight searches a pile of bricks that trembles harder and harder (heartbeat)
 *  2.2      the pile explodes - bricks tumble through the air
 *  2.2-5.4  they whirl around in a huge 3D vortex and are pulled one by one into the logo
 *  5.6      LEGO CLIENT slams in: flash, shock wave, sparks, camera shake; a brick floor builds itself
 *  8.6      fade to black, then the title screen.
 * Waits until Minecraft's own loading screen is gone, so it is never shown half covered.
 */
public final class IntroView extends View {
    private static final String[][] FONT = {
            {"L", "10000", "10000", "10000", "10000", "10000", "10000", "11111"},
            {"E", "11111", "10000", "10000", "11110", "10000", "10000", "11111"},
            {"G", "01111", "10000", "10000", "10011", "10001", "10001", "01111"},
            {"O", "01110", "10001", "10001", "10001", "10001", "10001", "01110"},
            {"C", "01111", "10000", "10000", "10000", "10000", "10000", "01111"},
            {"I", "11111", "00100", "00100", "00100", "00100", "00100", "11111"},
            {"N", "10001", "11001", "10101", "10011", "10001", "10001", "10001"},
            {"T", "11111", "00100", "00100", "00100", "00100", "00100", "00100"}};
    private static final int[] LEGO_COLS = {0xFFE3000B, 0xFFFFCD03, 0xFF006CB7, 0xFF4B9F4A};
    private static final int[] LEGO_COLS_HW = {0xFFFF6B1A, 0xFF8B2FC6, 0xFF3AE85D, 0xFFC41E1E};
    private static final float BURST = 2.2f, SLAM = 5.6f, FADE = 8.6f, END = 9.4f;
    /** Front view, slightly from above: bricks of one row touch, rows sit exactly one brick height on each other. */
    private static final double FINAL_YAW = 0, FINAL_PITCH = 22;
    /** Visible height of one stacked brick row relative to the brick width (front face 1.2 x cos 22deg / width 2). */
    private static final float ROW = 0.556f;
    /** Sprite size relative to the brick width (the model is 2 units wide inside a 4.6 unit sprite). */
    private static final float SPRITE = 2.3f;

    private static final class Brick {
        float sx, sy, tx, ty, s, delay, orbitA, orbitR, orbitH, spinY, spinP, yaw0, pitch0;
        int row;
        int color;
        boolean floor;
        float x, y, scale, yaw, pitch, depth;
    }

    private final List<Brick> bricks = new ArrayList<>();
    private final List<float[]> sparks = new ArrayList<>();
    private long start = -1;
    private int lastW = -1, lastH = -1, landed, beats;
    private boolean done, slammed, burst;
    private float logoTop, logoBottom;

    /** Diagnostics: how often an intro was created / actually started this session. */
    public static int created, started;
    private final List<Brick> order = new ArrayList<>();

    public IntroView() { BrickModel.prepare(); created++; }

    @Override public boolean blur() { return false; }
    @Override public boolean backdrop() { return false; }
    @Override public float openMs() { return 1; }
    @Override public float closeMs() { return 1; }
    @Override public float designW() { return 1000; }
    @Override public float designH() { return 600; }

    private static String[] glyph(char c) {
        for (String[] g : FONT) if (g[0].charAt(0) == c) return g;
        return null;
    }

    private void build() {
        bricks.clear();
        Random rnd = new Random(11);
        // every font pixel = 1 brick wide and 2 brick rows high, bricks touching each other
        float c1 = Math.min(W * 0.6f / 23f, H * 0.36f / (14 * ROW));
        float c2 = c1 * 0.5f;
        float y1 = H * 0.13f, y2 = y1 + 14 * ROW * c1 + c1 * 0.9f;
        logoTop = y1;
        addWord("LEGO", W / 2f, y1, c1, true, rnd);
        addWord("CLIENT", W / 2f, y2, c2, false, rnd);
        logoBottom = y2 + 14 * ROW * c2;
        // a two-row base plate under the logo, offset like a real brick wall
        float fs = c1 * 0.8f, fy = logoBottom + c1 * 0.6f;
        int n = Math.round(W * 0.64f / fs);
        for (int r = 0; r < 2; r++) for (int i = 0; i < n; i++) {
            Brick b = new Brick();
            b.tx = W / 2f - n * fs / 2f + i * fs + (r == 0 ? fs / 2f : 0) + fs / 2f;
            b.ty = fy + (1 - r) * fs * ROW + fs * ROW / 2f;
            b.sx = b.tx; b.sy = H + fs * 4;
            b.s = fs;
            b.row = 100 - r;
            b.color = Gx.mix(Halloween.on() ? 0xFF2A1540 : 0xFF39465F, Halloween.on() ? 0xFF3A1855 : 0xFF55648A, rnd.nextFloat());
            b.delay = SLAM - 0.1f + Math.abs(i - n / 2f) / (n / 2f) * 0.9f + r * 0.2f;
            b.floor = true;
            bricks.add(b);
        }
    }

    private void addWord(String word, float cx, float top, float cell, boolean big, Random rnd) {
        int cols = word.length() * 6 - 1;
        float left = cx - cols * cell / 2f;
        float rh = cell * ROW;
        for (int li = 0; li < word.length(); li++) {
            String[] g = glyph(word.charAt(li));
            if (g == null) continue;
            for (int r = 0; r < 7; r++) for (int c = 0; c < 5; c++) {
                if (g[1 + r].charAt(c) != '1') continue;
                for (int half = 0; half < 2; half++) {
                    int row = r * 2 + half;                       // 0 = top row
                    Brick b = new Brick();
                    b.tx = left + (li * 6 + c) * cell + cell / 2f;
                    b.ty = top + row * rh + rh / 2f;
                    b.row = row;
                    b.sx = W * (0.06f + 0.88f * rnd.nextFloat());
                    b.sy = H * (0.84f + 0.12f * rnd.nextFloat());
                    b.s = cell;
                    int[] pal = Halloween.on() ? LEGO_COLS_HW : LEGO_COLS;
                    b.color = Gx.mix(big ? pal[li % pal.length] : (Halloween.on() ? 0xFFDCD0E8 : 0xFFF2F4F8), 0xFF000000, rnd.nextFloat() * 0.08f);
                    // built like a real wall: bottom rows first, then upwards
                    float fromBottom = (13 - row) / 13f;
                    float order = Math.abs((b.tx - cx) / (cols * cell));
                    b.delay = 3.2f + (big ? 0 : 0.5f) + fromBottom * 1.5f + order * 0.35f + rnd.nextFloat() * 0.2f;
                    b.orbitA = (float) (rnd.nextFloat() * Math.PI * 2);
                    b.orbitR = 0.75f + 0.45f * rnd.nextFloat();
                    b.orbitH = (rnd.nextFloat() - 0.5f) * 0.5f;
                    b.spinY = (rnd.nextFloat() - 0.5f) * 900;
                    b.spinP = (rnd.nextFloat() - 0.5f) * 500;
                    b.yaw0 = rnd.nextFloat() * 360;
                    b.pitch0 = 35 + (rnd.nextFloat() - 0.5f) * 30;
                    bricks.add(b);
                }
            }
        }
    }
    private static float easeOutBack(float x) { float c1 = 1.5f, c3 = c1 + 1; return 1 + c3 * (float) Math.pow(x - 1, 3) + c1 * (float) Math.pow(x - 1, 2); }
    private static float easeOutCubic(float x) { return 1 - (float) Math.pow(1 - x, 3); }
    private static float easeInOut(float x) { return x < 0.5f ? 4 * x * x * x : 1 - (float) Math.pow(-2 * x + 2, 3) / 2; }

    @Override
    public void draw() {
        Gx.fill(0, 0, W, H, 0xFF000000);
        // wait for Minecraft's loading overlay and for the 3D brick sprites
        if (Env.loading.getAsBoolean() || !BrickModel.ready()) {
            if (start >= 0) start += 16;              // paused: the animation continues where it stopped
            return;
        }
        if (!BrickModel.upload(24)) return;          // all 3D sprites on the GPU before anything moves
        if (start < 0) { start = System.currentTimeMillis(); started++; }
        if (W != lastW || H != lastH) { lastW = W; lastH = H; build(); }
        float t = (System.currentTimeMillis() - start) / 1000f;
        if (t > END && !done) { done = true; host.close(); return; }
        float fade = t < 0.6f ? t / 0.6f : t > FADE ? Math.max(0, 1 - (t - FADE) / (END - FADE)) : 1;

        // ---------------------------------------------------------------- sound cues
        int beat = t < BURST ? (int) (t * (1.2f + t * 0.9f)) : beats;
        if (beat > beats && t < BURST - 0.1f) { beats = beat; Sound.play("minecraft:entity.warden.heartbeat", 0.9f + t * 0.15f, 0.55f); }
        if (!burst && t >= BURST) { burst = true; Sound.play("minecraft:entity.generic.explode", 0.6f, 0.5f); Sound.play("minecraft:entity.firework_rocket.blast", 0.8f, 0.4f); }
        if (!slammed && t >= SLAM) { slammed = true; Sound.play("minecraft:block.anvil.land", 0.55f, 0.55f); Sound.play("minecraft:ui.toast.challenge_complete", 1.0f, 0.45f); spawnSparks(); }

        // ---------------------------------------------------------------- camera
        float shake = 0;
        if (t > 0.8f && t < BURST) shake = (t - 0.8f) / (BURST - 0.8f) * 0.006f;
        if (t >= BURST && t < BURST + 0.6f) shake = 0.014f * (1 - (t - BURST) / 0.6f);
        if (t >= SLAM && t < SLAM + 0.8f) shake = 0.018f * (1 - (t - SLAM) / 0.8f);
        float camX = (float) Math.sin(t * 61) * shake * W, camY = (float) Math.cos(t * 47) * shake * H;
        float zoom = t < SLAM ? 1.25f - 0.17f * easeInOut(Math.min(1, t / SLAM)) : 1.08f - 0.08f * easeOutCubic(Math.min(1, (t - SLAM) / 3f));

        // ---------------------------------------------------------------- stage
        boolean hw = Halloween.on();
        Gx.gradientV(0, 0, W, H, Gx.withAlpha(hw ? 0xFF0A0618 : 0xFF0B1426, fade), Gx.withAlpha(hw ? 0xFF030208 : 0xFF020306, fade));
        float spot = t < BURST ? 0.5f + 0.5f * (float) Math.sin(t * 2.2) : 1;
        int spotX = Math.round(W / 2f + (float) Math.sin(t * 1.3) * W * (t < BURST ? 0.3f : 0.05f));
        Gx.glow(spotX, Math.round(t < BURST ? H * 0.85f : H * 0.38f), Math.round(Math.max(W, H) * (t < BURST ? 0.28f : 0.5f)), Gx.withAlpha(hw ? 0xFF8B2FC6 : 0xFF3A6BFF, 0.16f * spot * fade));
        Gx.glow(W / 2, Math.round(H * 0.95f), Math.round(Math.max(W, H) * 0.35f), Gx.withAlpha(hw ? 0xFFFF6B1A : 0xFFE3000B, 0.07f * fade));

        Gx.push();
        Gx.translate(camX, camY);
        Gx.scaleAt(W / 2f, H * 0.45f, zoom);

        // ---------------------------------------------------------------- update every brick (position, rotation, depth)
        int land = 0;
        float vx = W / 2f, vy = H * 0.42f;
        for (Brick b : bricks) {
            if (b.floor) {
                if (t < b.delay) { b.scale = 0; continue; }
                float f = Math.min(1, (t - b.delay) / 0.4f);
                b.x = b.tx; b.y = b.sy + (b.ty - b.sy) * easeOutCubic(f);
                b.yaw = (float) FINAL_YAW; b.pitch = (float) FINAL_PITCH; b.scale = 1; b.depth = 2;
                if (f >= 1) land++;
                continue;
            }
            if (t < BURST) {
                float tr = Math.max(0, (t - 0.8f) / (BURST - 0.8f));
                b.x = b.sx + (float) Math.sin(t * 40 + b.sx) * b.s * 0.08f * tr;
                b.y = b.sy + (float) Math.cos(t * 37 + b.sy) * b.s * 0.1f * tr;
                b.yaw = b.yaw0 + (float) Math.sin(t * 30 + b.sy) * 8 * tr; b.pitch = b.pitch0; b.scale = 1; b.depth = 0;
                continue;
            }
            // orbit position in the vortex (3D ring, rotating, slowly closing in)
            float ot = t - BURST;
            float a = b.orbitA + ot * (1.6f + 0.4f * b.orbitR);
            float rr = b.orbitR * (1 - 0.25f * Math.min(1, ot / 3f));
            float oz = (float) Math.sin(a);                         // depth -1..1
            float ox = vx + (float) Math.cos(a) * W * 0.36f * rr;
            float oy = vy + oz * H * 0.06f + b.orbitH * H * 0.5f;
            float oscale = 0.75f + 0.35f * (oz + 1) / 2;
            // burst: from the pile into the vortex
            float up = Math.min(1, ot / 0.9f);
            float bx = b.sx + (ox - b.sx) * easeOutCubic(up), by = b.sy + (oy - b.sy) * easeOutCubic(up) - (float) Math.sin(up * Math.PI) * H * 0.25f;
            float bs = 1 + (oscale - 1) * up;
            float f = Math.max(0, Math.min(1, (t - b.delay) / 0.85f));
            float e = easeOutCubic(Math.min(1, f / 0.8f));
            // fly to a point just above its place, then get pressed down onto the brick below (no gap)
            float hoverY = b.ty - b.s * ROW * 1.6f;
            b.x = bx + (b.tx - bx) * e;
            b.y = by + (hoverY - by) * e;
            if (f > 0.8f) {
                float p = (f - 0.8f) / 0.2f;
                b.y = hoverY + (b.ty - hoverY) * (p * p);
            }
            b.scale = bs + (1 - bs) * Math.min(1, e);
            float sp = 1 - Math.min(1, f * 1.15f);
            b.yaw = (float) (FINAL_YAW + (b.yaw0 + ot * b.spinY) * sp);
            b.pitch = (float) (FINAL_PITCH + (b.pitch0 - FINAL_PITCH + ot * b.spinP) * sp);
            b.depth = f >= 1 ? 1 : oz * (1 - f);
            if (f >= 1) {
                land++;
                // tiny "click" when it is pressed on
                float since = t - b.delay - 0.85f;
                if (since < 0.12f) b.y += (float) Math.sin(since / 0.12f * Math.PI) * b.s * 0.04f;
            }
        }
        if (land > landed) {
            if (land / 45 > landed / 45) Sound.play("minecraft:block.stone.place", 1.2f + (land % 7) * 0.06f, 0.25f);
            landed = land;
        }
        // far bricks first, landed logo bricks top row to bottom row
        if (order.size() != bricks.size()) { order.clear(); order.addAll(bricks); }
        order.sort((p, q) -> p.depth != q.depth ? Float.compare(p.depth, q.depth) : Float.compare(q.ty + q.row * 0.001f, p.ty + p.row * 0.001f));
        for (Brick b : order) {
            if (b.scale <= 0) continue;
            float size = b.s * SPRITE * b.scale;
            float dim = b.depth < 0 ? 0.55f + 0.45f * (1 + b.depth) : 1;
            int col = Gx.mix(0xFF000000, b.color, dim);
            Gx.image(BrickModel.sprite(b.yaw, b.pitch), Math.round(b.x - size / 2), Math.round(b.y - size / 2), Math.round(size), Math.round(size), fade >= 1 ? col : Gx.withAlpha(col, fade));
        }

        // ---------------------------------------------------------------- slam effects
        if (t >= SLAM) {
            float s = t - SLAM;
            if (s < 0.4f) Gx.fill(-W, -H, W * 3, H * 3, Gx.withAlpha(0xFFFFFFFF, 0.6f * (1 - s / 0.4f) * fade));
            int cy = Math.round((logoTop + logoBottom) / 2f);
            for (int k = 0; k < 2; k++) {
                float sk = s - k * 0.18f;
                if (sk < 0 || sk > 1.1f) continue;
                float rr = Math.max(W, H) * 0.9f * easeOutCubic(sk / 1.1f);
                int dot = Math.max(1, Math.round(Math.max(W, H) * 0.0028f * (1 - sk / 1.1f) + 1));
                int col = Gx.withAlpha(k == 0 ? 0xFFFFFFFF : 0xFF7FB4FF, 0.55f * (1 - sk / 1.1f) * fade);
                for (int d = 0; d < 90; d++) {
                    double ang = Math.PI * 2 * d / 90;
                    Gx.circle(Math.round(W / 2f + (float) Math.cos(ang) * rr), Math.round(cy + (float) Math.sin(ang) * rr), dot, col);
                }
            }
            float sweep = (s - 0.4f) / 1.4f;
            if (sweep > 0 && sweep < 1.2f) Gx.glow(Math.round(W * (0.1f + 0.85f * sweep)), cy, Math.round(Math.max(W, H) * 0.15f), Gx.withAlpha(0xFFFFFFFF, 0.28f * fade));
        }
        for (float[] p : sparks) {
            float age = t - p[4];
            if (age > 1.6f) continue;
            float px = p[0] + p[2] * age, py = p[1] + p[3] * age + 0.5f * H * 1.5f * age * age;
            int sparkCol = hw ? (age < 0.3f ? 0xFFE0C0FF : 0xFFFF6B1A) : (age < 0.3f ? 0xFFFFF3C0 : 0xFFFFB02E);
            Gx.circle(Math.round(px), Math.round(py), Math.max(1, Math.round(Math.max(W, H) * 0.0025f * (1 - age / 1.6f) + 1)), Gx.withAlpha(sparkCol, (1 - age / 1.6f) * fade));
        }
        Gx.pop();

        if (t > SLAM + 0.5f) {
            float g = Math.min(1, (t - SLAM - 0.5f) / 0.6f) * fade;
            float fs = Math.max(8, H * 0.02f);
            Gx.textSpaced("MINECRAFT 1.21.11", W / 2f, H * 0.9f, fs, Fonts.BOLD, Gx.withAlpha(hw ? 0xFFD0B8E0 : 0xFFB8C6E0, g), fs * 0.35f);
        }
        if (t < SLAM) {
            float fs = Math.max(7, H * 0.016f);
            Gx.textCenter("Klick, Leertaste oder Esc = überspringen", W / 2f, H * 0.97f, fs, Fonts.MEDIUM, Gx.withAlpha(0x44FFFFFF, fade));
        }
    }

    private void spawnSparks() {
        Random r = new Random(3);
        float cy = (logoTop + logoBottom) / 2f;
        for (int i = 0; i < 90; i++) {
            float a = (float) (r.nextFloat() * Math.PI * 2), sp = Math.max(W, H) * (0.25f + r.nextFloat() * 0.65f);
            sparks.add(new float[]{W / 2f + (r.nextFloat() - 0.5f) * W * 0.5f, cy + (r.nextFloat() - 0.5f) * H * 0.2f, (float) Math.cos(a) * sp, (float) Math.sin(a) * sp - H * 0.4f, SLAM});
        }
    }

    private void skip() { if (!done) { done = true; host.close(); } }

    @Override public boolean mouseDown(float x, float y, int button, boolean dbl) { skip(); return true; }
    @Override public boolean key(int key, int mods) { if (key == 32 || key == 256 || key == 257) { skip(); return true; } return false; }
    @Override public void onEscape() { skip(); }
}