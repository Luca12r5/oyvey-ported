package dev.lego.cosmetic;

import static dev.lego.cosmetic.VehGeo.*;

import dev.lego.cosmetic.Cos.A;

/** Four wheelers: single seat speedster (lofted curved body) and go-kart. */
final class VehCar {
    private VehCar() { }

    static boolean isNight() { return VehExtra.isNight(); }

    /** Hip height (leg pivot) when seated, handlebar {x, y, z}. */
    static final float CAR_HIP = 5.5f, KART_HIP = 3.6f;
    static final float[] CAR_HAND = {2.9f, 10.4f, 7.8f}, KART_HAND = {2.6f, 7.5f, 8.6f};

    static String hex(int c) { return String.format("%06X", c & 0xFFFFFF); }

    // ================================================================== sports car
    private static final float[] KZ = {-30, -28.5f, -25, -18.5f, -12, -6, 0, 6, 12, 19.5f, 25, 29, 31, 32};
    private static final float[] KHC = {7.4f, 9.4f, 10.5f, 10.9f, 11.1f, 11.3f, 11.3f, 11.4f, 11.5f, 10.3f, 9.3f, 8.2f, 7.5f, 6.2f};
    private static final float[] KCR = {7.4f, 9.8f, 11.4f, 12.5f, 11.9f, 11.7f, 11.7f, 11.7f, 11.9f, 12.2f, 11.1f, 9.0f, 7.5f, 6.2f};
    private static final float[] KW = {10.0f, 11.8f, 12.7f, 13.2f, 12.6f, 12.2f, 12.2f, 12.3f, 12.7f, 13.1f, 12.6f, 11.3f, 9.8f, 8.4f};
    private static final float[] KYB = {4.0f, 2.7f, 2.2f, 2.2f, 2.2f, 2.2f, 2.2f, 2.2f, 2.2f, 2.2f, 2.2f, 2.6f, 3.3f, 4.0f};
    static final float WR = 4.6f, WW = 3.4f, WX = 10.6f, WZF = 19.5f, WZR = -18.5f, ARCH = 5.5f;
    private static final float FLOOR = 3.0f;

    private static float cockpit(float z) { return smooth(-6.9f, -5.7f, z) * (1 - smooth(8.4f, 10.2f, z)); }

    private static float arch(float z) {
        float best = -1;
        for (float wz : new float[]{WZF, WZR}) {
            float dz = z - wz;
            if (Math.abs(dz) < ARCH) best = Math.max(best, WR + (float) Math.sqrt(ARCH * ARCH - dz * dz));
        }
        return best;
    }

    /** Right half of the body section (bottom centre -> top centre). */
    private static float[][] carHalf(float z) {
        float W = curve(z, KZ, KW), hc = curve(z, KZ, KHC), cr = curve(z, KZ, KCR), yb = curve(z, KZ, KYB);
        float sill = yb + 0.8f, ya = Math.max(sill, arch(z));
        float xin = Math.min(8.0f, W - 2.6f);
        float r = cockpit(z);
        float cw = Math.min(7.3f, W - 4.4f);
        float h = cr - ya;
        float y9 = cr, t10 = lerp(cr, hc, 0.45f);
        return new float[][]{
                {0, yb}, {xin - 0.4f, yb}, {xin, Math.max(ya, yb + 0.7f)},
                {W - 1.5f, ya}, {W, ya + h * 0.36f}, {W - 0.3f, ya + h * 0.74f},
                {W - 2.2f, y9}, {cw + 1.2f, t10}, {cw, t10 - 0.6f * r},
                {cw - 0.35f, lerp(t10, FLOOR, r)}, {Math.min(3.0f, cw - 0.7f), lerp(hc, FLOOR, r)}, {1.2f, lerp(hc, FLOOR, r)}, {0, lerp(hc, FLOOR, r)}};
    }

    static void car(G g, A a, int paint, int stripe) {
        String P = "veh_paint:" + hex(paint), ST = "veh_paint:" + hex(stripe);
        g.push();
        g.translate(0, sin(a.time * 38) * 0.04f * (1 - a.move * 0.5f), 0);
        // ---- wheels (not affected by the body roll)
        float steer = a.lean * 16;
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2) {
                g.push();
                g.translate(sx * WX, WR, sz > 0 ? WZF : WZR);
                if (sz > 0) g.rotY(steer);
                carWheel(g, a.wheel, sx < 0, WR, WW, 3.2f, 8, "veh_c:D8141B");
                g.pop();
            }
        // ---- body (rolls a little in the turns)
        g.translate(0, WR, 0);
        g.rotZ(-a.lean * 2.2f);
        g.translate(0, -WR, 0);
        java.util.List<Float> zl = new java.util.ArrayList<>();
        for (float wz : new float[]{WZF, WZR}) {
            for (float z : range(wz - ARCH + 0.02f, wz + ARCH - 0.02f, 1.8f)) zl.add(z);
            zl.add(wz - ARCH - 0.3f);
            zl.add(wz + ARCH + 0.3f);
        }
        for (float z : new float[]{-30, -28.5f, -6.9f, -5.7f, 8.4f, 10.2f, 29, 31, 32}) zl.add(z);
        for (float z : range(-30, 32, 4.4f))
            if (Math.abs(z - WZF) > ARCH + 1.2f && Math.abs(z - WZR) > ARCH + 1.2f) zl.add(z);
        java.util.Collections.sort(zl);
        float[] zs = new float[zl.size()];
        int nz = 0;
        for (float z : zl) if (nz == 0 || z - zs[nz - 1] > 0.3f) zs[nz++] = z;
        zs = java.util.Arrays.copyOf(zs, nz);
        final int N = 13, NS = zs.length;
        loft(g, zs, z -> mirrorRing(carHalf(z)), (k, j, z, pa, pb) -> {
            if (k < 0 || k >= NS - 1) return P;
            int hs = j < N - 1 ? j : 2 * N - 3 - j;
            float r = cockpit(z);
            boolean inside = r > 0.3f || cockpit(z + 1.2f) > 0.3f;
            boolean isArch = arch(z) > 0 || arch(z + 1) > 0;
            if (hs == 0) return null;                     // flat floor pan: never visible from above the ground
            if (hs <= 2) return "veh_under";
            if (hs == 3) return isArch ? P : "veh_carbon";
            if (hs >= 8 && inside) return "veh_interior";
            if (hs == 10 && !inside) return ST;
            return P;
        }, true, true, 13.2f, 2.0f);

        // ---- headrest fairing behind the driver
        loft(g, range(-22, -5.4f, 2.4f), z -> {
            float t = (z + 22) / 16.6f;
            float rx = lerp(0.9f, 3.7f, (float) Math.sqrt(t)), top = lerp(10.6f, 16.3f, (float) Math.pow(t, 0.7));
            float base = 9.6f;
            float[][] o = new float[10][];
            o[0] = new float[]{0, base};
            o[1] = new float[]{rx, base};
            for (int i = 1; i <= 7; i++) {
                double an = Math.PI * i / 8;
                o[1 + i] = new float[]{(float) Math.cos(an) * rx, base + (float) Math.sin(an) * (top - base)};
            }
            o[9] = new float[]{-rx, base};
            return o;
        }, (k, j, z, pa, pb) -> P, false, true, 17, 9);

        // ---- cockpit: seat, dash, steering wheel, windscreen
        rbox(g, "veh_leather:2A2224", -4.3f, 2.6f, -4.2f, 4.3f, 3.6f, 3.6f, 0.45f);            // cushion
        g.push();
        g.translate(0, 3.2f, -4.1f);
        g.rotX(-11);
        rbox(g, "veh_leather:2A2224", -4.3f, 0, -1.5f, 4.3f, 10.6f, 0.2f, 0.5f);               // backrest
        rbox(g, "veh_leather:C8102E", -4.8f, 0.4f, -1.2f, -3.6f, 8.5f, 1.3f, 0.4f);           // bolsters
        rbox(g, "veh_leather:C8102E", 3.6f, 0.4f, -1.2f, 4.8f, 8.5f, 1.3f, 0.4f);
        g.pop();
        rbox(g, "veh_interior", -7.2f, 7.6f, 9.0f, 7.2f, 11.2f, 11.4f, 0.5f);                  // dashboard
        box6(g, "veh_flat:15161A", new String[]{null, "veh_gauges", null, null, null, null}, -3.4f, 8.5f, 8.7f, 3.4f, 10.7f, 9.2f);
        tube(g, "veh_gun", p(0, 9.2f, 10), p(0, 10.2f, 8.1f), 0.35f, 0.35f, 6, false);
        g.push();
        g.translate(0, 10.4f, 7.8f);
        g.rotX(-58);
        g.rotY(-a.lean * 70);
        torus(g, "veh_leather:1A1A1E", 2.9f, 0.34f, 12, 4);
        for (int i = 0; i < 3; i++) {
            double an = Math.toRadians(90 + i * 120);
            beam(g, "veh_gun", p((float) Math.cos(an) * 0.6f, 0, (float) Math.sin(an) * 0.6f), p((float) Math.cos(an) * 2.7f, 0, (float) Math.sin(an) * 2.7f), 0.55f, 0.25f, 0.4f, 0.2f, UP);
        }
        lathe(g, new String[]{"veh_gun", "veh_c:D8141B"}, new float[][]{{0.9f, -0.35f}, {0.9f, 0.2f}, {0.01f, 0.32f}}, 10);
        g.pop();
        // wind deflector (three glass panels + frame)
        for (int i = -1; i <= 1; i++) {
            g.push();
            g.translate(i * 4.6f, 10.9f, 11.2f - Math.abs(i) * 0.9f);
            g.rotY(-i * 28);
            g.rotX(-38);
            box(g, "veh_glass", -2.4f, 0, -0.08f, 2.4f, 3.6f, 0.08f);
            g.pop();
        }
        // ---- mirrors
        for (int s = -1; s <= 1; s += 2) {
            beam(g, "veh_carbon", p(s * 11.4f, 11.5f, 9.4f), p(s * 13.4f, 12.6f, 9.9f), 0.45f, 0.35f);
            ellipsoid(g, P, s * 13.9f, 12.8f, 10.1f, 1.45f, 0.85f, 0.8f, 8, 5);
            ellipsoid(g, "veh_mirror", s * 13.9f, 12.8f, 9.45f, 1.2f, 0.66f, 0.2f, 8, 4);
        }
        // ---- side intakes
        for (int s = -1; s <= 1; s += 2) ellipsoid(g, "veh_flat:111216", s * 12.1f, 7.6f, -10.8f, 0.75f, 1.5f, 3.4f, 8, 4);
        // ---- rear wing
        for (int s = -1; s <= 1; s += 2) {
            beam(g, "veh_carbon", p(s * 6, 9.6f, -25.0f), p(s * 6, 14.3f, -27.2f), 0.5f, 1.6f, 0.5f, 1.2f, p(0, 0, 1));
            box(g, "veh_carbon", s < 0 ? -11.35f : 11.0f, 12.9f, -30.0f, s < 0 ? -11.0f : 11.35f, 16.2f, -25.0f);
        }
        g.push();
        g.translate(0, 14.6f, -27.4f);
        g.rotY(90);
        g.rotZ(-6);
        loft(g, new float[]{-11.1f, 11.1f}, z -> new float[][]{{2.3f, 0}, {1.1f, 0.4f}, {-0.6f, 0.65f}, {-1.9f, 0.5f}, {-2.4f, 0.05f}, {-1.9f, -0.28f}, {0, -0.25f}, {1.2f, -0.1f}},
                (k, j, z, pa, pb) -> "veh_carbon", true, true, 1, -1);
        g.pop();
        // ---- lights (bright only at night)
        boolean night = isNight();
        if (night) g.glow(true);
        for (int s = -1; s <= 1; s += 2) {
            ellipsoid(g, night ? "veh_glow:FFF6D8" : "veh_flat:DDDDAA", s * 7.9f, 7.6f, 28.3f, 2.3f, 0.95f, 2.2f, 8, 4);
            tube(g, night ? "veh_glow:FF2A2A" : "veh_flat:882222", p(s * 3.5f, 8.2f, -29.6f), p(s * 8.9f, 8.1f, -29.1f), 0.5f, 0.45f, 8, true);
        }
        tube(g, night ? "veh_glow:FF6A4A" : "veh_flat:884433", p(-3.5f, 8.2f, -29.6f), p(3.5f, 8.2f, -29.6f), 0.22f, 0.22f, 6, true);
        if (night) g.glow(false);
        // ---- front: grille, splitter; rear: plate, diffuser, exhausts
        box6(g, "veh_under", new String[]{"veh_grille", null, null, null, null, null}, -6.2f, 3.4f, 29.6f, 6.2f, 6.0f, 32.4f);
        box(g, "veh_carbon", -9.2f, 2.3f, 29.0f, 9.2f, 2.8f, 32.9f);
        box6(g, "veh_under", new String[]{null, "veh_plate", null, null, null, null}, -3.2f, 4.9f, -30.45f, 3.2f, 6.9f, -29.6f);
        box(g, "veh_carbon", -9.0f, 2.25f, -31.0f, 9.0f, 3.3f, -27.0f);
        for (int i = -2; i <= 2; i++) box(g, "veh_carbon", i * 3.2f - 0.12f, 3.3f, -31.0f, i * 3.2f + 0.12f, 4.4f, -28.0f);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 6.6f, 3.9f, -30.2f);
            g.rotX(-90);
            lathe(g, new String[]{"veh_chrome", "veh_chrome", "veh_under"}, new float[][]{{0.8f, -1.6f}, {0.8f, 1.1f}, {0.55f, 1.1f}, {0.55f, 0.6f}}, 10);
            lathe(g, "veh_under", new float[][]{{0.55f, 0.6f}, {0.01f, 0.6f}}, 10);
            g.pop();
        }
        g.pop();
    }

    /** Car / kart wheel at the current origin (axle along x); left wheels are mirrored so the rim faces outward. */
    static void carWheel(G g, float spin, boolean left, float R, float w, float rimR, int spokes, String caliper) {
        g.push();
        if (left) g.rotY(180);
        wheel(g, R, w, rimR, spokes, left ? -spin : spin, "veh_tread", "veh_side", "veh_rim", caliper != null ? "veh_disc" : null, 10);
        if (caliper != null) {
            g.rotX(left ? 45 : -45);
            float r0 = rimR * 0.5f, r1 = rimR - 0.45f;
            box(g, caliper, -0.75f, r0, -0.7f, 0.45f, r1, 0.7f);
        }
        g.pop();
    }

    // ================================================================== go-kart
    static void kart(G g, A a) {
        g.push();
        g.translate(0, sin(a.time * 45) * 0.06f, 0);
        String red = "veh_c:D81E1E", yel = "veh_c:FFCD03";
        // wheels
        for (int sx = -1; sx <= 1; sx += 2) {
            g.push();
            g.translate(sx * 9.2f, 2.2f, 10.5f);
            g.rotY(a.lean * 20);
            carWheel(g, a.wheel * 1.15f, sx < 0, 2.2f, 2.0f, 1.35f, 6, null);
            g.pop();
            g.push();
            g.translate(sx * 9.6f, 2.5f, -10f);
            carWheel(g, a.wheel, sx < 0, 2.5f, 3.0f, 1.55f, 6, null);
            g.pop();
        }
        // chassis tubes
        for (int s = -1; s <= 1; s += 2) {
            pipe(g, red, new float[][]{{s * 3.8f, 1.5f, -12.5f}, {s * 3.8f, 1.5f, 6}, {s * 2.9f, 1.8f, 12.5f}, {s * 2.0f, 2.5f, 15.6f}}, 0.38f, 6, true);
            pipe(g, red, new float[][]{{s * 3.8f, 1.6f, -5.5f}, {s * 8.8f, 1.9f, -5.2f}, {s * 9.0f, 1.9f, 5.2f}, {s * 3.8f, 1.6f, 6.5f}}, 0.32f, 6, false);
            pipe(g, "veh_gun", new float[][]{{s * 3.2f, 1.7f, 10.5f}, {s * 8.0f, 2.2f, 10.5f}}, 0.3f, 6, false);
            tube(g, "veh_gun", p(s * 8.1f, 1.2f, 10.7f), p(s * 8.1f, 3.3f, 10.3f), 0.28f, 0.28f, 6, true);   // kingpin
        }
        for (float z : new float[]{-12, -6, 3, 12})
            tube(g, red, p(-3.8f, z == 12 ? 1.8f : 1.5f, z), p(3.8f, z == 12 ? 1.8f : 1.5f, z), 0.3f, 0.3f, 6, false);
        pipe(g, red, new float[][]{{-2.0f, 2.5f, 15.6f}, {2.0f, 2.5f, 15.6f}}, 0.38f, 6, false);
        tube(g, "veh_chrome", p(-10.9f, 2.5f, -10), p(10.9f, 2.5f, -10), 0.42f, 0.42f, 8, true);       // rear axle
        pipe(g, "veh_chrome", new float[][]{{-7, 2.2f, -12.5f}, {-7, 2.8f, -15}, {7, 2.8f, -15}, {7, 2.2f, -12.5f}}, 0.36f, 6, true);
        box(g, "veh_alu", -3.8f, 1.32f, -6.2f, 3.8f, 1.6f, 13.0f);                                      // floor pan
        // bodywork
        for (int s = -1; s <= 1; s += 2)
            rbox(g, yel, s < 0 ? -8.9f : 5.0f, 1.6f, -6.6f, s < 0 ? -5.0f : 8.9f, 4.3f, 5.6f, 1.1f);       // side pods
        rbox(g, yel, -5.8f, 1.7f, 11.6f, 5.8f, 4.4f, 17.2f, 1.5f);                                       // nose
        rbox(g, yel, -3.2f, 1.7f, 11.2f, 3.2f, 7.0f, 12.4f, 0.4f);                                       // front panel
        box6(g, "veh_flat:F4F4F4", new String[]{"veh_num", null, null, null, null, null}, -2.3f, 2.3f, 12.3f, 2.3f, 6.4f, 12.65f);
        // seat
        rbox(g, "veh_flat:1A1B20", -3.6f, 1.45f, -5.6f, 3.6f, 2.6f, 1.6f, 0.6f);
        g.push();
        g.translate(0, 2.0f, -5.2f);
        g.rotX(-14);
        rbox(g, "veh_flat:1A1B20", -3.8f, 0, -1.3f, 3.8f, 9.6f, 0.1f, 0.6f);
        g.pop();
        for (int s = -1; s <= 1; s += 2) rbox(g, "veh_flat:1A1B20", s < 0 ? -4.3f : 3.4f, 1.6f, -5.6f, s < 0 ? -3.4f : 4.3f, 5.4f, 0.8f, 0.35f);
        // steering
        tube(g, "veh_gun", p(0, 3.0f, 12.0f), p(0, 7.3f, 8.8f), 0.3f, 0.3f, 6, false);
        g.push();
        g.translate(0, 7.5f, 8.6f);
        g.rotX(-52);
        g.rotY(-a.lean * 80);
        torus(g, "veh_flat:15151A", 2.6f, 0.36f, 16, 6);
        beam(g, "veh_gun", p(-2.5f, 0, 0), p(2.5f, 0, 0), 0.5f, 0.3f);
        lathe(g, new String[]{"veh_gun", yel}, new float[][]{{0.8f, -0.3f}, {0.8f, 0.2f}, {0.01f, 0.35f}}, 8);
        g.pop();
        // fuel tank between the legs
        g.push();
        g.translate(0, 2.4f, 13.4f);
        lathe(g, "veh_flat:E8E8E0", new float[][]{{0.01f, -0.9f}, {1.3f, -0.8f}, {1.4f, 1.2f}, {0.9f, 1.6f}, {0.5f, 1.7f}, {0.5f, 2.2f}, {0.01f, 2.2f}}, 10);
        g.pop();
        // engine (right rear)
        rbox(g, "veh_gun", 4.3f, 1.8f, -10.2f, 7.6f, 5.4f, -6.2f, 0.5f);
        for (int i = 0; i < 5; i++) box(g, "veh_alu", 4.0f, 5.4f + i * 0.55f, -9.4f, 7.9f, 5.65f + i * 0.55f, -6.9f);
        tube(g, "veh_alu", p(5.9f, 5.2f, -8.2f), p(5.9f, 8.3f, -8.2f), 0.9f, 0.9f, 8, true);
        g.push();
        g.translate(8.0f, 4.6f, -7.4f);
        g.rotZ(-90);
        lathe(g, new String[]{red, "veh_flat:222228", red}, new float[][]{{0.01f, -0.1f}, {1.3f, -0.1f}, {1.3f, 2.2f}, {0.01f, 2.2f}}, 10);
        g.pop();
        pipe(g, "veh_chrome", new float[][]{{5.9f, 6.6f, -10.4f}, {5.9f, 6.6f, -12.2f}, {4.6f, 5.4f, -13.6f}, {2.6f, 4.6f, -13.8f}}, 0.35f, 6, false);
        tube(g, "veh_chrome", p(3.0f, 4.6f, -13.8f), p(-2.4f, 4.6f, -13.8f), 0.8f, 0.8f, 8, true);
        g.push();
        g.translate(4.9f, 2.5f, -10);
        g.rotZ(-90);
        lathe(g, "veh_gun", new float[][]{{0.01f, -0.15f}, {1.6f, -0.15f}, {1.6f, 0.15f}, {0.01f, 0.15f}}, 12);   // sprocket
        g.pop();
        strip(g, "veh_chain", new float[][]{{4.9f, 4.1f, -10}, {4.9f, 3.4f, -8.2f}}, 0.25f, 0.5f, p(1, 0, 0), true);
        // checkered flag
        tube(g, "veh_alu", p(-7, 2.8f, -14.6f), p(-7, 17.5f, -14.6f), 0.18f, 0.18f, 6, true);
        for (int i = 0; i < 6; i++) {
            float x0 = -7 + 0.2f + i * 0.9f, x1 = x0 + 0.9f;
            float w0 = sin(a.time * 7 - i * 0.9) * 0.35f * i / 5f;
            g.push();
            g.translate(0, 0, w0);
            box6(g, "veh_checker", null, x0, 13.8f - i * 0.05f, -14.65f, x1, 17.2f - i * 0.05f, -14.55f);
            g.pop();
        }
        g.pop();
    }
}
