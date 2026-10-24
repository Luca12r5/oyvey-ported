package dev.lego.ui;

/**
 * Animated backgrounds behind the click GUI (chosen on the Design page). Everything is drawn in real screen
 * pixels (sharp in 4K) with cheap shapes, so the menu stays smooth.
 */
public final class BgFx {
    private BgFx() { }

    private static final String[] NAMES_DE = {"Keine", "Aurora", "Partikel", "Sterne", "Wellen", "Bokeh", "Synthwave", "Schnee", "Nebel", "Matrix", "Warp 3D", "Würfel 3D", "Bausteine 3D", "Tunnel 3D"};
    public static final int COUNT = NAMES_DE.length;
    public static String[] NAMES = NAMES_DE;
    public static void updateNames() {
        String[] n = new String[NAMES_DE.length];
        for (int i = 0; i < n.length; i++) n[i] = dev.lego.core.Lang.get("bgfx." + i, NAMES_DE[i]);
        NAMES = n;
    }

    private static float fract(double v) { return (float) (v - Math.floor(v)); }

    private static float hash(int i, int k) {
        int h = i * 374761393 + k * 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        return ((h ^ (h >>> 16)) & 0xFFFFFF) / (float) 0xFFFFFF;
    }

    /** Animation clipped into a window (x, y, w, h in pixels). */
    public static void drawIn(int x, int y, int w, int h, float a) {
        if (UiSettings.bgStyle <= 0) return;
        Gx.clip(x, y, w, h);
        Gx.push();
        Gx.translate(x, y);
        draw(w, h, a);
        Gx.pop();
        Gx.unclip();
    }

    /** a = global fade (menu open animation). */
    public static void draw(int W, int H, float a) {
        int style = UiSettings.bgStyle;
        if (style <= 0 || style >= NAMES.length || a <= 0.01f) return;
        float k = (float) UiSettings.bgIntensity * a;
        double t = System.currentTimeMillis() / 1000.0;
        int acc = Style.accent & 0xFFFFFF, acc2 = Gx.mix(Style.accent, 0xFF6A5CFF, 0.6f) & 0xFFFFFF;
        int S = Math.max(W, H);
        switch (style) {
            case 1 -> { // Aurora: slow drifting light curtains
                for (int i = 0; i < 5; i++) {
                    float x = W * fract(0.13 + i * 0.21 + t * (0.010 + i * 0.004));
                    float y = H * (0.18f + 0.12f * (float) Math.sin(t * 0.3 + i * 1.7));
                    Gx.glow(Math.round(x), Math.round(y), Math.round(S * (0.32f + 0.06f * i)), Gx.rgba(i % 2 == 0 ? acc : acc2, 0.22f * k));
                }
                Gx.gradientV(0, 0, W, H / 2, Gx.rgba(acc2, 0.10f * k), 0);
            }
            case 2 -> { // Particles drifting upwards
                for (int i = 0; i < 90; i++) {
                    float x = W * hash(i, 1) + (float) Math.sin(t * 0.6 + i) * S * 0.01f;
                    float y = H * (1 - fract(hash(i, 2) + t * (0.02 + hash(i, 3) * 0.05)));
                    int r = Math.max(1, Math.round(S * (0.0015f + hash(i, 4) * 0.003f)));
                    Gx.circle(Math.round(x), Math.round(y), r, Gx.rgba(i % 3 == 0 ? acc : 0xFFFFFF, (0.25f + 0.5f * hash(i, 5)) * k));
                }
            }
            case 3 -> { // Stars + a shooting star
                for (int i = 0; i < 140; i++) {
                    float tw = 0.4f + 0.6f * (float) Math.abs(Math.sin(t * (0.8 + hash(i, 3) * 2) + i));
                    int r = Math.max(1, Math.round(S * (0.0008f + hash(i, 4) * 0.0016f)));
                    Gx.circle(Math.round(W * hash(i, 1)), Math.round(H * hash(i, 2)), r, Gx.rgba(0xFFFFFF, 0.75f * tw * k));
                }
                float p = fract(t / 6.0);
                if (p < 0.25f) {
                    float q = p / 0.25f;
                    for (int j = 0; j < 14; j++) {
                        float f = q - j * 0.012f;
                        if (f < 0) break;
                        Gx.circle(Math.round(W * (0.15f + 0.6f * f)), Math.round(H * (0.12f + 0.25f * f)), Math.max(1, Math.round(S * 0.002f * (1 - j / 14f))), Gx.rgba(0xFFFFFF, (1 - j / 14f) * k));
                    }
                }
            }
            case 4 -> { // Waves at the bottom
                int cols = 72, cw = W / cols + 1;
                for (int w = 0; w < 3; w++) {
                    for (int c = 0; c < cols; c++) {
                        double x = c / (double) cols;
                        float y = H * (0.68f + w * 0.08f) + (float) (Math.sin(x * 9 + t * (0.8 + w * 0.3) + w) * H * 0.03);
                        Gx.gradientV(c * W / cols, Math.round(y), cw, H - Math.round(y), Gx.rgba(w == 1 ? acc2 : acc, 0.16f * k), Gx.rgba(w == 1 ? acc2 : acc, 0.02f * k));
                    }
                }
            }
            case 5 -> { // Bokeh: big soft circles
                for (int i = 0; i < 22; i++) {
                    float x = W * fract(hash(i, 1) + t * (0.004 + hash(i, 2) * 0.01));
                    float y = H * (hash(i, 3) + 0.04f * (float) Math.sin(t * 0.5 + i));
                    int r = Math.round(S * (0.03f + hash(i, 4) * 0.06f));
                    Gx.circle(Math.round(x), Math.round(y), r, Gx.rgba(i % 2 == 0 ? acc : acc2, (0.05f + 0.07f * hash(i, 5)) * k));
                }
            }
            case 6 -> { // Synthwave: sun + scrolling horizon grid
                int hy = Math.round(H * 0.62f), sr = Math.round(S * 0.12f);
                Gx.glow(W / 2, hy - sr / 2, Math.round(sr * 2.4f), Gx.rgba(0xFF4FA3, 0.22f * k));
                Gx.circle(W / 2, hy - sr / 2, sr, Gx.rgba(0xFF6A9A, 0.55f * k));
                for (int i = 0; i < 6; i++) Gx.fill(W / 2 - sr, hy - sr / 2 + i * sr / 6 + sr / 10, sr * 2, Math.max(1, sr / 22 + i), Gx.rgba(0x0B0716, 0.7f * k));
                Gx.fill(0, hy, W, H - hy, Gx.rgba(0x12061F, 0.55f * k));
                for (int i = 0; i < 14; i++) {
                    float f = fract(i / 14.0 + t * 0.12);
                    int y = hy + Math.round((H - hy) * f * f);
                    Gx.fill(0, y, W, Math.max(1, Math.round(1 + f * 2)), Gx.rgba(0xFF4FD8, (0.15f + 0.45f * f) * k));
                }
            }
            case 7 -> { // Snow
                for (int i = 0; i < 130; i++) {
                    float sp = 0.04f + hash(i, 2) * 0.06f;
                    float y = H * fract(hash(i, 1) + t * sp);
                    float x = W * hash(i, 3) + (float) Math.sin(t * (0.6 + hash(i, 4)) + i) * S * 0.012f;
                    int r = Math.max(1, Math.round(S * (0.0012f + hash(i, 5) * 0.0025f)));
                    Gx.circle(Math.round(x), Math.round(y), r, Gx.rgba(0xFFFFFF, (0.35f + 0.45f * hash(i, 6)) * k));
                }
            }
            case 8 -> { // Nebula: colour shifting clouds
                for (int i = 0; i < 6; i++) {
                    float x = W * (0.5f + 0.42f * (float) Math.sin(t * 0.07 * (1 + i * 0.3) + i * 2.1));
                    float y = H * (0.5f + 0.38f * (float) Math.cos(t * 0.05 * (1 + i * 0.25) + i));
                    int col = java.awt.Color.HSBtoRGB(fract(t * 0.02 + i * 0.16), 0.65f, 1f) & 0xFFFFFF;
                    Gx.glow(Math.round(x), Math.round(y), Math.round(S * 0.35f), Gx.rgba(col, 0.16f * k));
                }
            }
            case 9 -> { // Matrix rain made of small blocks
                int cols = 46, cs = Math.max(2, W / cols), bs = Math.max(2, cs / 2);
                for (int c = 0; c < cols; c++) {
                    float head = fract(hash(c, 1) + t * (0.12 + hash(c, 2) * 0.18));
                    int len = 10 + Math.round(hash(c, 3) * 10);
                    for (int j = 0; j < len; j++) {
                        float y = (head - j * 0.022f);
                        if (y < 0) break;
                        float f = 1 - j / (float) len;
                        Gx.fill(c * cs + cs / 4, Math.round(y * H), bs, bs, Gx.rgba(j == 0 ? 0xD8FFE8 : 0x2BFF77, f * 0.55f * k));
                    }
                }
            }
            case 10 -> { // Warp: stars rushing towards you in 3D
                float cx = W / 2f, cy = H / 2f;
                for (int i = 0; i < 160; i++) {
                    float z = 1 - fract(hash(i, 1) + t * (0.18 + hash(i, 4) * 0.12));
                    float px = (hash(i, 2) - 0.5f) * 2, py = (hash(i, 3) - 0.5f) * 2;
                    float sx = cx + px / Math.max(0.04f, z) * S * 0.09f, sy = cy + py / Math.max(0.04f, z) * S * 0.09f;
                    if (sx < -20 || sy < -20 || sx > W + 20 || sy > H + 20) continue;
                    float near = 1 - z;
                    int r = Math.max(1, Math.round(S * 0.0035f * near * near + 1));
                    // short streak behind each star
                    float z2 = Math.min(1, z + 0.035f);
                    float tx = cx + px / Math.max(0.04f, z2) * S * 0.09f, tyy = cy + py / Math.max(0.04f, z2) * S * 0.09f;
                    for (int s = 1; s <= 3; s++) Gx.circle(Math.round(sx + (tx - sx) * s / 3f), Math.round(sy + (tyy - sy) * s / 3f), Math.max(1, r - s / 2), Gx.rgba(s == 1 ? 0xFFFFFF : acc, near * k * (0.5f - s * 0.12f)));
                    Gx.circle(Math.round(sx), Math.round(sy), r, Gx.rgba(0xFFFFFF, Math.min(1, near * 1.4f) * k));
                }
            }
            case 11 -> { // rotating wireframe cubes floating in depth
                for (int c = 0; c < 6; c++) {
                    float ccx = W * (0.15f + 0.7f * hash(c, 1)), ccy = H * (0.2f + 0.6f * hash(c, 2));
                    float size = S * (0.05f + 0.05f * hash(c, 3)) * (1 + 0.15f * (float) Math.sin(t + c));
                    double ay = t * (0.4 + hash(c, 4) * 0.5), ax = t * (0.3 + hash(c, 5) * 0.4);
                    float[][] pts = new float[8][];
                    for (int i = 0; i < 8; i++) {
                        double x = (i & 1) == 0 ? -1 : 1, y = (i & 2) == 0 ? -1 : 1, z = (i & 4) == 0 ? -1 : 1;
                        double x1 = x * Math.cos(ay) + z * Math.sin(ay), z1 = -x * Math.sin(ay) + z * Math.cos(ay);
                        double y1 = y * Math.cos(ax) - z1 * Math.sin(ax), z2 = y * Math.sin(ax) + z1 * Math.cos(ax);
                        float pr = (float) (3 / (4 + z2));
                        pts[i] = new float[]{ccx + (float) x1 * size * pr, ccy + (float) y1 * size * pr, (float) z2};
                    }
                    int[][] edges = {{0, 1}, {2, 3}, {4, 5}, {6, 7}, {0, 2}, {1, 3}, {4, 6}, {5, 7}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
                    for (int[] e : edges) {
                        float[] a2 = pts[e[0]], b2 = pts[e[1]];
                        float depth = 0.55f - (a2[2] + b2[2]) * 0.18f;
                        for (int s = 0; s <= 10; s++) {
                            float q = s / 10f;
                            Gx.circle(Math.round(a2[0] + (b2[0] - a2[0]) * q), Math.round(a2[1] + (b2[1] - a2[1]) * q), Math.max(1, Math.round(S * 0.0016f)), Gx.rgba(c % 2 == 0 ? acc : acc2, depth * k));
                        }
                    }
                    for (float[] p : pts) Gx.circle(Math.round(p[0]), Math.round(p[1]), Math.max(1, Math.round(S * 0.003f)), Gx.rgba(0xFFFFFF, (0.5f - p[2] * 0.2f) * k));
                }
            }
            case 12 -> { // Lego bricks floating through 3D space (far = small, dim, slow)
                int[] cols = {0xE3000B, 0xFFCD03, 0x006CB7, 0x4B9F4A, 0xF4F4F4};
                for (int i = 0; i < 34; i++) {
                    float depth = 0.25f + 0.75f * hash(i, 1);
                    float x = W * fract(hash(i, 2) + t * 0.012 * depth);
                    float y = H * (hash(i, 3) + 0.05f * (float) Math.sin(t * 0.6 * depth + i));
                    int s = Math.round(S * 0.035f * depth);
                    int col = cols[i % cols.length];
                    float al = (0.25f + 0.55f * depth) * k;
                    int bx = Math.round(x), by = Math.round(y);
                    Gx.rect(bx, by + Math.max(1, s / 6), s * 2, s, Math.max(1, s / 5), Gx.rgba(Gx.mix(col | 0xFF000000, 0xFF000000, 0.4f) & 0xFFFFFF, al));
                    Gx.rect(bx, by, s * 2, s, Math.max(1, s / 5), Gx.rgba(col, al));
                    for (int st = 0; st < 2; st++) Gx.circle(bx + s / 2 + st * s, by + s / 3, Math.max(1, s / 4), Gx.rgba(Gx.mix(col | 0xFF000000, 0xFFFFFFFF, 0.3f) & 0xFFFFFF, al));
                }
            }
            case 13 -> { // tunnel of glowing rings flying towards you
                float cx = W / 2f + (float) Math.sin(t * 0.5) * W * 0.05f, cy = H / 2f + (float) Math.cos(t * 0.4) * H * 0.05f;
                for (int i = 0; i < 18; i++) {
                    float z = 1 - fract(i / 18.0 + t * 0.15);
                    float r = S * 0.06f / Math.max(0.05f, z);
                    if (r > S * 1.4f) continue;
                    int dots = 40;
                    float near = 1 - z;
                    for (int d = 0; d < dots; d++) {
                        double ang = Math.PI * 2 * d / dots + t * 0.3 + i * 0.2;
                        Gx.circle(Math.round(cx + (float) Math.cos(ang) * r), Math.round(cy + (float) Math.sin(ang) * r), Math.max(1, Math.round(S * 0.003f * near + 1)), Gx.rgba(i % 2 == 0 ? acc : acc2, near * near * 0.8f * k));
                    }
                }
            }
            default -> { }
        }
    }
}