package dev.lego.cosmetic;

import static dev.lego.cosmetic.VehGeo.*;

import dev.lego.cosmetic.Cos.A;
import dev.lego.core.Mc;

/** 23 additional vehicles: brings the total from 12 to 40 (with 5 car color variants in Vehicles). */
final class VehExtra {
    private VehExtra() { }

    static boolean isNight() {
        long t = Math.floorMod(Mc.timeOfDay(), 24000L);
        return t >= 13000 && t < 23000;
    }

    private static void carXform(G g, A a) {
        g.translate(0, sin(a.time * 38) * 0.04f * (1 - a.move * 0.5f), 0);
        g.translate(0, VehCar.WR, 0);
        g.rotZ(-a.lean * 2.2f);
        g.translate(0, -VehCar.WR, 0);
    }

    // constants for new vehicle types
    static final float BOBBY_HIP = 3.2f;
    static final float LIMO_HIP = 5.5f, F1_HIP = 3.4f, MONSTER_HIP = 9.0f;
    static final float QUAD_SEAT = 10.0f, SNOW_SEAT = 9.8f, TRIKE_SEAT = 9.0f;
    static final float LONG_TOP = 2.3f, CARPET_TOP = 2.0f, SURF_TOP = 2.5f;
    static final float SEGWAY_TOP = 2.8f;
    static final float[] LIMO_HAND = {2.9f, 10.4f, 7.8f}, F1_HAND = {2.4f, 7.0f, 8.0f};
    static final float[] MONSTER_HAND = {3.2f, 14.5f, 8.0f}, QUAD_HAND = {4.0f, 11.5f, 9.0f};
    static final float[] SNOW_HAND = {4.0f, 11.5f, 9.0f}, SEGWAY_HAND = {2.5f, 18.0f, 4.0f};
    static final float[] TRIKE_HAND = {5.0f, 11.0f, 8.0f};

    // ================================================================== POLIZEIWAGEN (car + blue lights)
    static void police(G g, A a) {
        VehCar.car(g, a, 0xF2F2F2, 0x2A5BDF);
        g.push();
        carXform(g, a);
        rbox(g, "veh_gun", -3.2f, 11.2f, -2.5f, 3.2f, 11.8f, 2.5f, 0.2f);
        g.glow(true);
        boolean blue = (int) (a.time * 4) % 2 == 0;
        String bl = "veh_glow:2A5BFF", rd = "veh_glow:FF2A2A";
        ellipsoid(g, blue ? bl : rd, -1.8f, 12.2f, 0, 0.7f, 0.5f, 0.7f, 8, 5);
        ellipsoid(g, blue ? rd : bl, 1.8f, 12.2f, 0, 0.7f, 0.5f, 0.7f, 8, 5);
        g.glow(false);
        g.pop();
    }

    // ================================================================== TAXI (car + sign)
    static void taxi(G g, A a) {
        VehCar.car(g, a, 0xFFCD03, 0x1A1A1E);
        g.push();
        carXform(g, a);
        rbox(g, "veh_c:FFCD03", -1.8f, 11.3f, -0.5f, 1.8f, 12.5f, 1.5f, 0.4f);
        g.glow(true);
        box6(g, "veh_flat:0A0A0F", new String[]{null, null, null, null, "veh_glow:FFEE88", null}, -1.6f, 12.45f, -0.3f, 1.6f, 12.6f, 1.3f);
        g.glow(false);
        g.pop();
    }

    // ================================================================== BOBBY CAR (small toy car)
    static final float BOBBY_WR = 2.4f;

    static void bobby(G g, A a) {
        String red = "veh_c:D8141B", blk = "veh_flat:1C1D22", yel = "veh_c:FFCD03";
        g.push();
        g.translate(0, sin(a.time * 40) * 0.03f, 0);
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2) {
                g.push();
                g.translate(sx * 5.5f, BOBBY_WR, sz > 0 ? 7 : -7);
                if (sz > 0) g.rotY(a.lean * 20);
                VehCar.carWheel(g, a.wheel * 1.5f, sx < 0, BOBBY_WR, 2.0f, 1.5f, 0, null);
                g.pop();
            }
        rbox(g, red, -5.0f, 3.0f, -8.0f, 5.0f, 5.5f, 10.0f, 1.2f);
        rbox(g, red, -4.5f, 5.0f, -6.0f, 4.5f, 8.0f, 4.0f, 1.0f);
        rbox(g, blk, -3.5f, 4.5f, -5.0f, 3.5f, BOBBY_HIP + 12, 1.0f, 0.6f);
        tube(g, yel, p(-4.5f, 8.2f, 7.0f), p(4.5f, 8.2f, 7.0f), 0.5f, 0.5f, 8, true);
        boolean night = isNight();
        if (night) g.glow(true);
        for (int s = -1; s <= 1; s += 2)
            ellipsoid(g, night ? "veh_glow:FFF6D8" : "veh_flat:DDDDAA", s * 3.5f, 5.2f, 10.2f, 0.7f, 0.5f, 0.3f, 8, 4);
        if (night) g.glow(false);
        g.pop();
    }

    // ================================================================== MONSTER TRUCK (lifted car, huge wheels)
    static final float MT_WR = 7.5f, MT_WX = 11.0f;

    static void monster(G g, A a) {
        String P = "veh_c:2FA58C", gun = "veh_gun", cr = "veh_chrome", carbon = "veh_carbon";
        g.push();
        g.translate(0, sin(a.time * 34) * 0.08f * (1 - a.move * 0.4f), 0);
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2) {
                g.push();
                g.translate(sx * MT_WX, MT_WR, sz > 0 ? 16 : -16);
                if (sz > 0) g.rotY(a.lean * 14);
                VehCar.carWheel(g, a.wheel * 0.7f, sx < 0, MT_WR, 4.5f, 4.8f, 8, null);
                g.pop();
            }
        // suspension arms (4-link style)
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2) {
                float wz = sz > 0 ? 16 : -16;
                tube(g, gun, p(sx * 9.0f, 6.5f, wz - 2), p(sx * MT_WX, MT_WR - 1, wz), 0.55f, 0.45f, 6, false);
                tube(g, gun, p(sx * 9.0f, 6.5f, wz + 2), p(sx * MT_WX, MT_WR + 1, wz), 0.55f, 0.45f, 6, false);
                // shock absorber
                tube(g, cr, p(sx * 9.5f, 8.0f, wz), p(sx * MT_WX * 0.95f, MT_WR + 3, wz), 0.35f, 0.25f, 6, true);
                tube(g, "veh_c:D81E1E", p(sx * 9.5f, 8.0f, wz), p(sx * (9.5f + MT_WX * 0.95f) / 2, (8.0f + MT_WR + 3) / 2, wz), 0.2f, 0.2f, 6, true);
            }
        g.translate(0, MT_WR + 3.0f, 0);
        g.rotZ(-a.lean * 2.5f);
        g.translate(0, -MT_WR - 3.0f, 0);
        // body panels (more detailed)
        rbox(g, P, -9.0f, 6.0f, -18, 9.0f, 10.0f, 20, 1.2f);
        rbox(g, P, -8.5f, 9.5f, -16, 8.5f, 16.0f, 14, 1.0f);
        rbox(g, gun, -9.3f, 4.5f, -16, 9.3f, 6.5f, 18, 0.3f);
        // wheel arches (flared fenders)
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2) {
                float wz = sz > 0 ? 16 : -16;
                rbox(g, P, sx < 0 ? -10.5f : 7.5f, 6.0f, wz - 5, sx < 0 ? -7.5f : 10.5f, 9.0f, wz + 5, 0.6f);
            }
        // windows
        for (int s = -1; s <= 1; s += 2)
            box(g, "veh_glass", s < 0 ? -8.7f : 8.2f, 10.5f, -10, s < 0 ? -8.2f : 8.7f, 14.5f, 10);
        box(g, "veh_glass", -5.5f, 10.5f, 13.5f, 5.5f, 14.5f, 14.2f);
        box(g, "veh_glass", -5.5f, 10.5f, -16.3f, 5.5f, 14.5f, -15.5f);
        // roll cage
        for (int s = -1; s <= 1; s += 2) {
            pipe(g, gun, new float[][]{{s * 8.5f, 10.0f, -15}, {s * 8.5f, 17.5f, -14}, {s * 8.5f, 18.0f, 13}, {s * 8.5f, 10.0f, 14}}, 0.35f, 6, false);
            tube(g, gun, p(s * 8.5f, 14.0f, -14.5f), p(s * 8.5f, 14.0f, 13.5f), 0.25f, 0.25f, 6, false);
        }
        tube(g, gun, p(-8.5f, 17.5f, -5), p(8.5f, 17.5f, -5), 0.3f, 0.3f, 6, false);
        tube(g, gun, p(-8.5f, 17.8f, 5), p(8.5f, 17.8f, 5), 0.3f, 0.3f, 6, false);
        // light bar on top
        rbox(g, gun, -7.0f, 17.5f, -2, 7.0f, 18.5f, 8, 0.3f);
        // front push bar
        tube(g, cr, p(-8.0f, 6.5f, 20.0f), p(-8.0f, 12.0f, 20.5f), 0.4f, 0.4f, 8, true);
        tube(g, cr, p(8.0f, 6.5f, 20.0f), p(8.0f, 12.0f, 20.5f), 0.4f, 0.4f, 8, true);
        tube(g, cr, p(-8.0f, 12.0f, 20.5f), p(8.0f, 12.0f, 20.5f), 0.35f, 0.35f, 8, false);
        // interior
        rbox(g, "veh_leather:1A1A1E", -5.0f, 7.0f, -5.0f, 5.0f, MONSTER_HIP, 2.0f, 0.5f);
        rbox(g, "veh_interior", -7.0f, 11.0f, 6.0f, 7.0f, 14.5f, 9.0f, 0.4f);
        // grille
        box6(g, gun, new String[]{"veh_grille", null, null, null, null, null}, -5.5f, 6.5f, 19.5f, 5.5f, 10.0f, 20.5f);
        // lights
        boolean night = isNight();
        if (night) g.glow(true);
        for (int s = -1; s <= 1; s += 2) {
            rbox(g, night ? "veh_glow:FFF6D8" : "veh_flat:DDDDAA", s * 4 - 2.0f, 7.5f, 20.3f, s * 4 + 2.0f, 10.0f, 20.8f, 0.2f);
            rbox(g, night ? "veh_glow:FF2A2A" : "veh_flat:882222", s * 4 - 1.5f, 7.5f, -18.5f, s * 4 + 1.5f, 9.5f, -17.8f, 0.2f);
        }
        // roof light bar LEDs
        for (int i = -3; i <= 3; i++)
            rbox(g, night ? "veh_glow:FFF6D8" : "veh_flat:DDDDAA", i * 1.8f - 0.6f, 18.2f, 2.0f, i * 1.8f + 0.6f, 18.5f, 6.0f, 0.08f);
        if (night) g.glow(false);
        // exhaust stacks
        for (int s = -1; s <= 1; s += 2)
            tube(g, cr, p(s * 8.0f, 10.0f, -17), p(s * 8.0f, 18.0f, -17), 0.65f, 0.55f, 8, true);
        g.pop();
    }

    // ================================================================== FORMEL 1 (open-wheel racer)
    static final float F1_WR = 3.8f;

    static void f1(G g, A a) {
        String P = "veh_c:E3242B", cr = "veh_chrome", gun = "veh_gun", carbon = "veh_carbon";
        g.push();
        g.translate(0, sin(a.time * 42) * 0.03f * (1 - a.move * 0.5f), 0);
        for (int sx = -1; sx <= 1; sx += 2) {
            g.push(); g.translate(sx * 12.5f, F1_WR, 14); g.rotY(a.lean * 16);
            VehCar.carWheel(g, a.wheel, sx < 0, F1_WR, 2.8f, 2.4f, 5, null); g.pop();
            g.push(); g.translate(sx * 12.0f, F1_WR + 0.5f, -14);
            VehCar.carWheel(g, a.wheel, sx < 0, F1_WR + 0.5f, 3.5f, 2.8f, 5, null); g.pop();
        }
        g.translate(0, F1_WR, 0);
        g.rotZ(-a.lean * 3.0f);
        g.translate(0, -F1_WR, 0);
        loft(g, range(-20, 22, 2.5f), z -> {
            float t = (z + 20) / 42f;
            float w = lerp(1.5f, 3.8f, smooth(0, 0.3f, t)) - smooth(0.7f, 1, t) * 1.8f;
            float h = lerp(4.0f, 6.5f, smooth(0, 0.35f, t)) - smooth(0.6f, 1, t) * 2.5f;
            return new float[][]{{-w, 1.8f}, {w, 1.8f}, {w, h}, {0, h + 0.8f}, {-w, h}};
        }, (k, j, z, pa, pb) -> P, true, true, 7, 1.5f);
        rbox(g, "veh_leather:1A1A1E", -2.2f, 2.0f, -6, 2.2f, F1_HIP + 12, -1.0f, 0.4f);
        rbox(g, "veh_interior", -3.0f, 5.5f, 1.0f, 3.0f, 7.0f, 5.0f, 0.3f);
        tube(g, gun, p(0, 3.5f, 6.0f), p(0, 7.0f, 8.0f), 0.3f, 0.3f, 6, false);
        g.push();
        g.translate(0, 7.0f, 8.0f); g.rotX(-55); g.rotY(-a.lean * 80);
        torus(g, "veh_flat:15151A", 2.4f, 0.3f, 14, 5);
        g.pop();
        for (int s = -1; s <= 1; s += 2) {
            box(g, carbon, s * 3.0f, 2.0f, 12, s * 12.0f, 2.8f, 15);
            box(g, carbon, s * 3.0f, 2.0f, -12, s * 11.5f, 2.8f, -15);
        }
        for (int s = -1; s <= 1; s += 2) {
            beam(g, carbon, p(s * 6, 6.0f, -19), p(s * 6, 10.5f, -21), 0.5f, 1.5f, 0.5f, 1.0f, p(0, 0, 1));
        }
        g.push();
        g.translate(0, 10.8f, -21);
        g.rotY(90); g.rotZ(-6);
        box(g, carbon, -10.5f, -0.1f, -1.5f, 10.5f, 0.1f, 1.0f);
        g.pop();
        tube(g, cr, p(-3.0f, 3.2f, -19), p(3.0f, 3.2f, -19), 0.9f, 0.9f, 10, true);
        boolean night = isNight();
        if (night) g.glow(true);
        for (int s = -1; s <= 1; s += 2)
            ellipsoid(g, night ? "veh_glow:FFF6D8" : "veh_flat:DDDDAA", s * 1.5f, 4.5f, 22.2f, 0.8f, 0.5f, 0.3f, 8, 4);
        rbox(g, night ? "veh_glow:FF2A2A" : "veh_flat:882222", -1.8f, 5.0f, -20.3f, 1.8f, 6.5f, -19.5f, 0.15f);
        if (night) g.glow(false);
        g.pop();
    }

    // ================================================================== QUAD / ATV
    static final float QUAD_WR = 3.6f;

    static void quad(G g, A a) {
        String P = "veh_c:D81E1E", gun = "veh_gun", alu = "veh_alu";
        float spin = a.wheel * 0.9f;
        g.push();
        g.rotZ(-a.lean * 10);
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2) {
                g.push();
                g.translate(sx * 7.5f, QUAD_WR, sz > 0 ? 8.0f : -8.5f);
                if (sz > 0) g.rotY(a.lean * 16);
                VehCar.carWheel(g, spin, sx < 0, QUAD_WR, 3.0f, 2.2f, 0, null);
                g.pop();
            }
        rbox(g, P, -5.0f, 4.5f, -10, 5.0f, 9.5f, 10, 1.0f);
        rbox(g, gun, -5.3f, 3.5f, -8, 5.3f, 5.0f, 8, 0.3f);
        rbox(g, "veh_leather:1A1A1E", -3.2f, 8.8f, -6.0f, 3.2f, QUAD_SEAT, 1.0f, 0.5f);
        rbox(g, P, -4.0f, 8.0f, 7.0f, 4.0f, 12.5f, 11.0f, 0.8f);
        tube(g, gun, p(0, 10.0f, 11.0f), p(0, 11.5f, 9.0f), 0.4f, 0.4f, 6, false);
        tube(g, alu, p(-4.5f, 11.5f, 9.0f), p(4.5f, 11.5f, 9.0f), 0.35f, 0.35f, 8, true);
        for (int s = -1; s <= 1; s += 2)
            tube(g, "veh_gripr", p(s * 3.5f, 11.5f, 9.0f), p(s * 5.2f, 11.5f, 9.0f), 0.55f, 0.55f, 8, true);
        for (int s = -1; s <= 1; s += 2) {
            tube(g, gun, p(s * 5.0f, 5.0f, -8), p(s * 7.5f, QUAD_WR, -8.5f), 0.5f, 0.4f, 6, false);
            tube(g, gun, p(s * 5.0f, 5.0f, 8), p(s * 7.5f, QUAD_WR, 8.0f), 0.5f, 0.4f, 6, false);
        }
        boolean night = isNight();
        if (night) g.glow(true);
        for (int s = -1; s <= 1; s += 2)
            ellipsoid(g, night ? "veh_glow:FFF6D8" : "veh_flat:DDDDAA", s * 2.5f, 11.5f, 11.5f, 0.8f, 0.5f, 0.3f, 8, 4);
        rbox(g, night ? "veh_glow:FF2A2A" : "veh_flat:882222", -2.0f, 8.5f, -10.5f, 2.0f, 9.8f, -9.8f, 0.15f);
        if (night) g.glow(false);
        rbox(g, gun, 3.5f, 4.0f, -9.5f, 5.5f, 6.5f, -6.0f, 0.3f);
        pipe(g, "veh_chrome", new float[][]{{5.0f, 5.5f, -9.5f}, {5.0f, 5.5f, -11.0f}, {4.0f, 4.5f, -12.0f}}, 0.4f, 6, true);
        g.pop();
    }

    // ================================================================== LIMOUSINE (long luxury car)
    static void limo(G g, A a) {
        VehCar.car(g, a, 0x1A1A1E, 0xE8E8E8);
        g.push();
        carXform(g, a);
        rbox(g, "veh_c:1A1A1E", -12.0f, 2.5f, -38, 12.0f, 11.5f, -26, 1.0f);
        for (int s = -1; s <= 1; s += 2) {
            box(g, "veh_tint", s < 0 ? -12.2f : 11.7f, 7.5f, -36, s < 0 ? -11.7f : 12.2f, 10.5f, -28);
            g.push();
            g.translate(sx(s, 10.5f), 2.5f, -32);
            VehCar.carWheel(g, a.wheel, s < 0, VehCar.WR, VehCar.WW, 3.2f, 8, null);
            g.pop();
        }
        boolean night = isNight();
        if (night) g.glow(true);
        for (int s = -1; s <= 1; s += 2)
            rbox(g, night ? "veh_glow:FF2A2A" : "veh_flat:882222", s * 3 - 1, 7.0f, -38.5f, s * 3 + 1, 9.0f, -37.8f, 0.15f);
        if (night) g.glow(false);
        g.pop();
    }

    private static float sx(int s, float v) { return s < 0 ? -v : v; }

    // ================================================================== LONGBOARD (longer skateboard)
    static void longboard(G g, A a) {
        g.push();
        g.rotZ(-a.lean * 4);
        for (int s = -1; s <= 1; s += 2) VehBoard.truck(g, a, s * 10.5f, s * a.lean * 7);
        float y = 2.0f;
        rbox(g, "veh_deckart", -4.4f, y, -15, 4.4f, y + 0.5f, 15, 0.4f);
        box(g, "veh_griptape", -4.0f, y + 0.45f, -13, 4.0f, y + LONG_TOP - y, 13);
        g.pop();
    }

    // ================================================================== SURFBOARD (surfboard)
    static void surfboard(G g, A a) {
        g.push();
        g.translate(0, 1.5f + sin(a.time * 1.8) * 0.4f, 0);
        g.rotZ(-a.lean * 5);
        g.rotX(-a.move * 3);
        loft(g, range(-14, 14, 1.2f), z -> {
            float az = Math.abs(z);
            float w = az <= 8 ? 3.8f : 3.8f * (float) Math.sqrt(Math.max(0.01f, 1 - ((az - 8) / 6f) * ((az - 8) / 6f)));
            return new float[][]{{-w, 0}, {0, -0.3f}, {w, 0}, {0, 0.5f}};
        }, (k, j, z, pa, pb) -> j < 2 ? "veh_c:F2F2F2" : "veh_c:24C8E0", true, true, 0.5f, -0.3f);
        for (int i = 0; i < 3; i++) {
            float z = -12 + i * 1.5f;
            box(g, "veh_c:24C8E0", -0.08f, -0.3f, z, 0.08f, -1.2f, z + 0.6f);
        }
        g.pop();
    }

    // ================================================================== FLIEGENDER TEPPICH (magic carpet)
    static void carpet(G g, A a) {
        g.push();
        g.translate(0, 1.5f + sin(a.time * 2.0) * 0.3f, 0);
        g.rotZ(-a.lean * 6);
        g.rotX(-a.move * 5);
        for (int zz = -6; zz <= 6; zz += 2)
            for (int xx = -5; xx <= 5; xx += 2) {
                float wave = sin(a.time * 3 + xx * 0.3f + zz * 0.4f) * 0.25f;
                boolean edge = Math.abs(xx) >= 4 || Math.abs(zz) >= 5;
                boolean corner = Math.abs(xx) >= 4 && Math.abs(zz) >= 5;
                String col = corner ? "veh_c:FFD700" : edge ? "veh_c:8B1A1A" : ((xx + zz) % 4 == 0 ? "veh_c:C62828" : "veh_c:FFB300");
                box(g, col, xx - 1, wave - 0.08f, zz - 1, xx + 1, wave + 0.08f, zz + 1);
            }
        for (int s = -1; s <= 1; s += 2) {
            float z = s * 7;
            for (int x = -4; x <= 4; x += 2) {
                float swing = sin(a.time * 4 + x * 0.5f) * 0.5f;
                box(g, "veh_c:FFD700", x - 0.15f, -0.1f, z, x + 0.15f, -1.0f - swing, z + s * 0.8f);
            }
        }
        g.pop();
    }

    // ================================================================== HEXENBESEN (broomstick)
    static void broom(G g, A a) {
        String wood = "veh_c:8B6914", straw = "veh_c:E8C94A";
        g.push();
        g.translate(0, 4.0f + sin(a.time * 2.2) * 0.35f, 0);
        g.rotZ(-a.lean * 8);
        g.rotX(-a.move * 6);
        tube(g, wood, p(0, 0, -14), p(0, 0, 14), 0.5f, 0.45f, 8, true);
        for (int i = 0; i < 12; i++) {
            float an = (float) (Math.PI * 2 * i / 12);
            float sw = sin(a.time * 5 + i) * 0.3f;
            float x = cos(an) * 0.8f, y = sin(an) * 0.8f;
            tube(g, straw, p(x * 0.5f, y * 0.5f, -14), p(x + sw, y + sw * 0.5f, -18), 0.12f, 0.08f, 4, false);
        }
        rbox(g, "veh_c:4A3210", -0.8f, -0.2f, -14.5f, 0.8f, 0.6f, -13.5f, 0.15f);
        g.pop();
    }

    // ================================================================== SEGWAY
    static void segway(G g, A a) {
        String blk = "veh_flat:1C1D22", wht = "veh_c:E8E8E8";
        float wr = 3.2f, spin = a.wheel * 1.2f;
        g.push();
        g.rotZ(-a.lean * 6);
        for (int s = -1; s <= 1; s += 2)
            VehRide.wheelAt(g, s * 4.8f, wr, 0, wr, 1.8f, 2.0f, 0, spin, "veh_gun", null);
        tube(g, "veh_chrome", p(-5.2f, wr, 0), p(5.2f, wr, 0), 0.3f, 0.3f, 8, true);
        rbox(g, blk, -3.2f, 1.2f, -2.5f, 3.2f, SEGWAY_TOP, 2.5f, 0.5f);
        box(g, "veh_rubber", -2.8f, SEGWAY_TOP - 0.1f, -2.0f, 2.8f, SEGWAY_TOP + 0.15f, 2.0f);
        tube(g, wht, p(0, SEGWAY_TOP, 1.5f), p(0, 18.5f, 3.5f), 0.5f, 0.5f, 8, false);
        rbox(g, blk, -2.8f, 17.5f, 3.0f, 2.8f, 19.0f, 4.5f, 0.3f);
        tube(g, wht, p(-3.0f, 18.0f, 4.0f), p(3.0f, 18.0f, 4.0f), 0.35f, 0.35f, 8, true);
        for (int s = -1; s <= 1; s += 2)
            tube(g, "veh_gripr", p(s * 2.4f, 18.0f, 4.0f), p(s * 3.5f, 18.0f, 4.0f), 0.55f, 0.55f, 8, true);
        g.glow(true);
        rbox(g, "veh_glow:2AE0FF", -1.5f, 1.4f, -2.7f, 1.5f, 1.7f, 2.7f, 0.1f);
        g.glow(false);
        g.pop();
    }

    // ================================================================== SCHNEEMOBIL (snowmobile)
    static void snowmobile(G g, A a) {
        String P = "veh_c:FF4B00", blk = "veh_flat:1C1D22", gun = "veh_gun", cr = "veh_chrome";
        g.push();
        g.rotZ(-a.lean * 10);
        g.translate(0, sin(a.time * 36) * 0.04f, 0);
        rbox(g, P, -5.5f, 3.0f, -12, 5.5f, 9.0f, 14, 1.0f);
        rbox(g, blk, -5.8f, 2.0f, -10, 5.8f, 3.5f, 12, 0.3f);
        rbox(g, "veh_leather:1A1A1E", -3.5f, 8.5f, -8, 3.5f, SNOW_SEAT, 2, 0.6f);
        g.push();
        g.translate(0, 8.5f, -7);
        g.rotX(-12);
        rbox(g, "veh_leather:1A1A1E", -3.8f, 0, -1.5f, 3.8f, 6.5f, 0.1f, 0.5f);
        g.pop();
        rbox(g, P, -4.5f, 6.0f, 10, 4.5f, 11.0f, 16, 0.8f);
        tube(g, gun, p(0, 8.5f, 15), p(0, 11.5f, 9.0f), 0.4f, 0.4f, 6, false);
        tube(g, cr, p(-4.5f, 11.5f, 9.0f), p(4.5f, 11.5f, 9.0f), 0.35f, 0.35f, 8, true);
        for (int s = -1; s <= 1; s += 2)
            tube(g, "veh_gripr", p(s * 3.5f, 11.5f, 9.0f), p(s * 5.2f, 11.5f, 9.0f), 0.55f, 0.55f, 8, true);
        for (int s = -1; s <= 1; s += 2) {
            rbox(g, gun, s < 0 ? -7.5f : 5.0f, 0.5f, -12, s < 0 ? -5.0f : 7.5f, 1.5f, 14, 0.3f);
            rbox(g, cr, s < 0 ? -7.2f : 5.3f, 0.0f, -13, s < 0 ? -5.3f : 7.2f, 0.5f, 15, 0.15f);
        }
        rbox(g, blk, -3.5f, 1.0f, -14, 3.5f, 3.0f, -10, 0.4f);
        for (int i = 0; i < 8; i++)
            rbox(g, gun, -3.3f, 1.2f, -13.5f + i * 1.5f, 3.3f, 2.8f, -12.5f + i * 1.5f, 0.15f);
        boolean night = isNight();
        if (night) g.glow(true);
        for (int s = -1; s <= 1; s += 2)
            ellipsoid(g, night ? "veh_glow:FFF6D8" : "veh_flat:DDDDAA", s * 2.5f, 9.5f, 16.5f, 0.8f, 0.5f, 0.3f, 8, 4);
        rbox(g, night ? "veh_glow:FF2A2A" : "veh_flat:882222", -1.5f, 7.0f, -12.5f, 1.5f, 8.5f, -11.8f, 0.1f);
        if (night) g.glow(false);
        g.pop();
    }

    // ================================================================== DREIRAD (tricycle)
    static void dreirad(G g, A a) {
        String red = "veh_c:E3242B", gun = "veh_gun", cr = "veh_chrome";
        float spin = a.wheel * 1.4f, wr = 2.5f;
        g.push();
        g.rotZ(-a.lean * 5);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 5.0f, wr, -7);
            VehCar.carWheel(g, spin, s < 0, wr, 1.5f, 1.6f, 0, null);
            g.pop();
        }
        g.push();
        steer(g, p(0, 5.5f, 7.5f), 6, a.lean * 25);
        g.push(); g.translate(0, wr + 0.5f, 8);
        VehCar.carWheel(g, spin * 0.9f, false, wr + 0.5f, 1.8f, 2.0f, 0, null); g.pop();
        for (int s = -1; s <= 1; s += 2)
            tube(g, red, p(s * 0.7f, 5.0f, 7.8f), p(s * 0.7f, wr + 0.5f, 8), 0.3f, 0.3f, 6, false);
        tube(g, red, p(0, 5.5f, 7.5f), p(0, 11.0f, 8.0f), 0.4f, 0.4f, 8, false);
        tube(g, cr, p(-5.5f, 11.0f, 8.0f), p(5.5f, 11.0f, 8.0f), 0.35f, 0.35f, 8, true);
        for (int s = -1; s <= 1; s += 2)
            tube(g, "veh_gripr", p(s * 4.2f, 11.0f, 8.0f), p(s * 6.0f, 11.0f, 8.0f), 0.55f, 0.55f, 8, true);
        g.pop();
        tube(g, red, p(0, 4.5f, 6), p(0, 5.5f, -2), 0.5f, 0.4f, 8, false);
        tube(g, red, p(0, 5.5f, -2), p(0, 8.0f, -3), 0.4f, 0.4f, 8, false);
        rbox(g, "veh_leather:2A2224", -2.5f, 7.5f, -5, 2.5f, TRIKE_SEAT, -1.5f, 0.5f);
        for (int s = -1; s <= 1; s += 2)
            tube(g, red, p(s * 0.6f, 4.5f, 0), p(s * 4.5f, wr, -7), 0.3f, 0.3f, 6, false);
        tube(g, cr, p(-5.5f, wr, -7), p(5.5f, wr, -7), 0.25f, 0.25f, 6, true);
        boolean night = isNight();
        if (night) g.glow(true);
        rbox(g, night ? "veh_glow:FF2A2A" : "veh_flat:882222", -0.5f, 7.5f, -5.3f, 0.5f, 8.5f, -4.8f, 0.1f);
        if (night) g.glow(false);
        g.pop();
    }

    // ================================================================== ROLLSCHUHE (inline skates)
    static void inline(G g, A a) {
        String boot = "veh_c:2A5BDF", frame = "veh_alu";
        float spin = a.wheel * 3.0f;
        g.push();
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 1.6f, 0, 0);
            rbox(g, boot, -1.2f, 1.5f, -2, 1.2f, 5.5f, 3, 0.4f);
            rbox(g, frame, -0.6f, 0.3f, -3.5f, 0.6f, 1.2f, 4.5f, 0.15f);
            for (int i = 0; i < 4; i++) {
                float z = -2.5f + i * 2.0f;
                g.push();
                g.translate(0, 0.8f, z);
                g.rotZ(-90);
                lathe(g, new String[]{"veh_side", "veh_urethane", "veh_urethane", "veh_side"},
                        new float[][]{{0.01f, -0.35f}, {0.5f, -0.35f}, {0.75f, -0.2f}, {0.75f, 0.2f}, {0.5f, 0.35f}, {0.01f, 0.35f}}, 10);
                g.pop();
            }
            rbox(g, "veh_c:FF4B4B", -1.0f, 3.5f, 2.5f, 0.5f, 5.0f, 3.5f, 0.15f);
            g.pop();
        }
        g.pop();
    }

    // ================================================================== EINRAD (unicycle)
    static void einrad(G g, A a) {
        String cr = "veh_chrome", gun = "veh_gun";
        float wr = 4.5f, spin = a.wheel * 0.7f;
        g.push();
        g.rotZ(-a.lean * 8);
        g.push();
        g.translate(0, wr, 0);
        g.rotX(spin);
        g.rotZ(-90);
        lathe(g, new String[]{"veh_side", "veh_tread", "veh_tread", "veh_side"},
                new float[][]{{3.5f, -0.4f}, {wr - 0.3f, -0.5f}, {wr, 0}, {wr - 0.3f, 0.5f}, {3.5f, 0.4f}}, 22);
        int n = 16;
        for (int i = 0; i < n; i++) {
            double an = Math.PI * 2 * i / n;
            tube(g, cr, p(cos(an) * 0.35f, (i % 2 == 0 ? -0.3f : 0.3f), sin(an) * 0.35f),
                    p(cos(an) * 3.4f, 0, sin(an) * 3.4f), 0.06f, 0.06f, 3, false);
        }
        g.pop();
        tube(g, cr, p(0, wr, 0), p(0, wr + 11, 0), 0.35f, 0.35f, 8, false);
        rbox(g, "veh_leather:2A2224", -2.5f, wr + 10.5f, -1.8f, 2.5f, wr + 11.5f, 1.8f, 0.4f);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(0, wr, 0);
            g.rotX(spin + (s > 0 ? 0 : (float) Math.PI));
            float px = s * 2.2f, py = -4.2f;
            tube(g, cr, p(0, 0, 0), p(0, py, 0), 0.18f, 0.18f, 6, false);
            rbox(g, gun, -0.7f, py - 0.2f, -1.0f, 0.7f, py + 0.2f, 1.0f, 0.08f);
            g.pop();
        }
        g.pop();
    }

    // ================================================================== RENNRAD (racing bike - sleek variant of bike)
    static void rennrad(G g, A a) {
        String fr = "veh_c:E3242B", cr = "veh_chrome", gun = "veh_gun";
        float spin = a.wheel * 0.55f;
        g.push();
        g.rotZ(-a.lean * 6);
        g.push(); g.translate(VehRide.RA[0], VehRide.RA[1], VehRide.RA[2]); VehRide.bikeWheel(g, spin); g.pop();
        g.push(); g.translate(1.25f, VehRide.RA[1], VehRide.RA[2]); g.rotX(spin); g.rotZ(-90);
        lathe(g, gun, new float[][]{{0.01f, -0.12f}, {0.95f, -0.12f}, {0.95f, 0.12f}, {0.01f, 0.12f}}, 12); g.pop();
        pipe(g, fr, new float[][]{VehRide.BB, {0, 11.8f, -0.25f}}, 0.3f, 8, false);
        pipe(g, fr, new float[][]{{0, 12.0f, 10.5f}, {0, 7.5f, 9.0f}, {0, 5.0f, 5.5f}, {0, 4.3f, 2.8f}}, 0.3f, 8, false);
        pipe(g, fr, new float[][]{{0, 12.0f, 10.5f}, {0, 9.0f, 8.0f}, {0, 7.6f, 4.5f}, {0, 7.4f, 1.5f}}, 0.28f, 8, false);
        for (int s = -1; s <= 1; s += 2) {
            pipe(g, fr, new float[][]{{s * 0.3f, 4.4f, 2.3f}, {s * 0.78f, 5.4f, -4.2f}}, 0.22f, 6, false);
            pipe(g, fr, new float[][]{{s * 0.22f, 11.1f, -0.1f}, {s * 0.78f, 5.4f, -4.2f}}, 0.2f, 6, false);
        }
        tube(g, fr, p(0, 9.8f, 11.0f), p(0, 12.8f, 10.3f), 0.45f, 0.45f, 10, true);
        tube(g, fr, p(-0.9f, VehRide.BB[1], VehRide.BB[2]), p(0.9f, VehRide.BB[1], VehRide.BB[2]), 0.5f, 0.5f, 10, true);
        tube(g, cr, p(0, 11.5f, -0.2f), p(0, 13.0f, -0.5f), 0.22f, 0.22f, 6, false);
        rbox(g, "veh_leather:1A1A1E", -1.5f, 12.8f, -2.5f, 1.5f, 13.3f, 0, 0.3f);
        float crank = Vehicles.pedalAngle(a.wheel);
        g.push(); g.translate(0, VehRide.BB[1], VehRide.BB[2]); g.rotX(crank);
        tube(g, "veh_alu", p(-1.75f, 0, 0), p(1.75f, 0, 0), 0.25f, 0.25f, 6, true);
        beam(g, "veh_alu", p(1.65f, 0, 0), p(1.65f, -VehRide.CRANK, 0), 0.35f, 0.45f, 0.3f, 0.4f, p(0, 0, 1));
        beam(g, "veh_alu", p(-1.65f, 0, 0), p(-1.65f, VehRide.CRANK, 0), 0.35f, 0.45f, 0.3f, 0.4f, p(0, 0, 1));
        g.pop();
        float[][] ped = Vehicles.pedals("veh_bike", a.wheel);
        if (ped != null) for (float[] pp : ped) {
            float sx = Math.signum(pp[0]);
            rbox(g, "veh_flat:1C1D22", pp[0] - 0.7f, pp[1] - 0.15f, pp[2] - 0.5f, pp[0] + 0.7f, pp[1] + 0.15f, pp[2] + 0.5f, 0.08f);
        }
        g.push();
        steer(g, p(0, 11.5f, 10.75f), 17.8f, a.lean * 22);
        for (int s = -1; s <= 1; s += 2) pipe(g, fr, new float[][]{{s * 0.75f, 9.5f, 11.3f}, {s * 0.75f, 7.3f, 12.25f}, {s * 0.8f, VehRide.FA[1], VehRide.FA[2]}}, 0.22f, 6, false);
        g.push(); g.translate(VehRide.FA[0], VehRide.FA[1], VehRide.FA[2]); VehRide.bikeWheel(g, spin); g.pop();
        tube(g, cr, p(0, 12.6f, 10.35f), p(0, 13.5f, 10.1f), 0.25f, 0.25f, 6, true);
        pipe(g, cr, new float[][]{{-4.5f, 13.5f, 7.5f}, {-2.5f, 13.4f, 9.5f}, {0, 13.2f, 10.0f}, {2.5f, 13.4f, 9.5f}, {4.5f, 13.5f, 7.5f}}, 0.2f, 6, false);
        for (int s = -1; s <= 1; s += 2)
            tube(g, "veh_gripr", p(s * 3.8f, 13.5f, 8.2f), p(s * 5.0f, 13.5f, 7.2f), 0.42f, 0.42f, 8, true);
        g.pop();
        boolean night = isNight();
        if (night) g.glow(true);
        rbox(g, night ? "veh_glow:FF2A2A" : "veh_flat:882222", -0.5f, 10.2f, -9.75f, 0.5f, 10.65f, -9.35f, 0.1f);
        if (night) g.glow(false);
        g.pop();
    }

    // ================================================================== BMX (small BMX bike)
    static void bmx(G g, A a) {
        String fr = "veh_c:1ED760", cr = "veh_chrome", gun = "veh_gun";
        float spin = a.wheel * 0.7f, bwr = 3.8f;
        g.push();
        g.rotZ(-a.lean * 8);
        for (int sz = -1; sz <= 1; sz += 2) {
            float z = sz > 0 ? 9.5f : -6.0f;
            g.push(); g.translate(0, bwr, z);
            g.rotX(sz > 0 ? spin : spin);
            g.rotZ(-90);
            lathe(g, new String[]{"veh_side", "veh_tread", "veh_tread", "veh_side"},
                    new float[][]{{2.4f, -0.3f}, {bwr - 0.2f, -0.4f}, {bwr, 0}, {bwr - 0.2f, 0.4f}, {2.4f, 0.3f}}, 18);
            int n = 16;
            for (int i = 0; i < n; i++) {
                double an = Math.PI * 2 * i / n;
                float hy = (i % 4 < 2) ? -0.45f : 0.45f;
                tube(g, cr, p(cos(an) * 0.5f, hy, sin(an) * 0.5f), p(cos(an) * 2.3f, 0, sin(an) * 2.3f), 0.06f, 0.06f, 3, false);
            }
            g.pop();
        }
        pipe(g, fr, new float[][]{{0, 3.8f, 1.5f}, {0, 9.5f, -0.5f}}, 0.3f, 8, false);
        pipe(g, fr, new float[][]{{0, 9.0f, 7.5f}, {0, 6.5f, 5.5f}, {0, 4.5f, 2.5f}}, 0.3f, 8, false);
        for (int s = -1; s <= 1; s += 2) {
            pipe(g, fr, new float[][]{{s * 0.25f, 4.0f, 1.5f}, {s * 0.65f, bwr, -6.0f}}, 0.2f, 6, false);
            pipe(g, fr, new float[][]{{s * 0.2f, 9.0f, -0.5f}, {s * 0.65f, bwr, -6.0f}}, 0.18f, 6, false);
        }
        tube(g, fr, p(-0.75f, 3.8f, 1.5f), p(0.75f, 3.8f, 1.5f), 0.45f, 0.45f, 8, true);
        float crank = Vehicles.pedalAngle(a.wheel);
        g.push(); g.translate(0, 3.8f, 1.5f); g.rotX(crank);
        tube(g, "veh_alu", p(-1.5f, 0, 0), p(1.5f, 0, 0), 0.22f, 0.22f, 6, true);
        g.pop();
        float[][] ped = Vehicles.pedals("veh_bike", a.wheel);
        if (ped != null) for (float[] pp : ped)
            rbox(g, "veh_flat:1C1D22", pp[0] - 0.6f, pp[1] - 0.15f, pp[2] - 0.5f, pp[0] + 0.6f, pp[1] + 0.15f, pp[2] + 0.5f, 0.08f);
        tube(g, cr, p(0, 9.0f, -0.5f), p(0, 10.0f, -0.7f), 0.2f, 0.2f, 6, false);
        rbox(g, "veh_leather:1A1A1E", -2.0f, 9.8f, -2.5f, 2.0f, 10.3f, -0.5f, 0.3f);
        g.push();
        steer(g, p(0, 8.5f, 7.8f), 12, a.lean * 28);
        for (int s = -1; s <= 1; s += 2)
            pipe(g, fr, new float[][]{{s * 0.6f, 8.0f, 8.0f}, {s * 0.6f, bwr, 9.5f}}, 0.2f, 6, false);
        tube(g, fr, p(0, 7.5f, 8.3f), p(0, 14.5f, 7.8f), 0.35f, 0.35f, 8, false);
        tube(g, cr, p(-5.0f, 14.5f, 7.8f), p(5.0f, 14.5f, 7.8f), 0.3f, 0.3f, 8, true);
        for (int s = -1; s <= 1; s += 2)
            tube(g, "veh_gripr", p(s * 3.8f, 14.5f, 7.8f), p(s * 5.5f, 14.5f, 7.8f), 0.52f, 0.52f, 8, true);
        for (int s = -1; s <= 1; s += 2)
            tube(g, cr, p(s * 5.3f, bwr, 9.5f), p(s * 5.3f, bwr + 5.0f, 9.5f), 0.2f, 0.2f, 6, true);
        g.pop();
        g.pop();
    }

    // ================================================================== CABRIO (open-top car)
    static void cabrio(G g, A a) {
        VehCar.car(g, a, 0xC81030, 0xF2F2F2);
        g.push();
        carXform(g, a);
        box(g, "veh_interior", -7.0f, 9.8f, -5, 7.0f, 10.0f, 8);
        g.pop();
    }

    // ================================================================== HOT ROD
    static void hotrod(G g, A a) {
        VehCar.car(g, a, 0xA52A2A, 0xFFCD03);
        g.push();
        carXform(g, a);
        for (int s = -1; s <= 1; s += 2)
            pipe(g, "veh_chrome", new float[][]{{s * 5.0f, 6.0f, 28}, {s * 5.0f, 6.0f, 32}, {s * 5.5f, 12.0f, 35}}, 0.5f, 8, true);
        g.pop();
    }

    // ================================================================== LAMBORGHINI (angular supercar)
    static void lambo(G g, A a) {
        String P = "veh_paint:" + VehCar.hex(0xFFCD03), carbon = "veh_carbon", gun = "veh_gun";
        g.push();
        g.translate(0, sin(a.time * 40) * 0.035f * (1 - a.move * 0.5f), 0);
        float steer = a.lean * 16;
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2) {
                g.push();
                g.translate(sx * 11.0f, VehCar.WR, sz > 0 ? 20 : -19);
                if (sz > 0) g.rotY(steer);
                VehCar.carWheel(g, a.wheel, sx < 0, VehCar.WR, VehCar.WW, 3.2f, 8, "veh_c:D8141B");
                g.pop();
            }
        g.translate(0, VehCar.WR, 0);
        g.rotZ(-a.lean * 2.0f);
        g.translate(0, -VehCar.WR, 0);
        loft(g, range(-30, 34, 2.8f), z -> {
            float t = (z + 30) / 64f;
            float w = lerp(5.0f, 13.5f, smooth(0, 0.2f, t)) - smooth(0.85f, 1, t) * 5.5f;
            float hc = lerp(6.0f, 10.5f, smooth(0, 0.3f, t)) - smooth(0.5f, 1, t) * 2.5f;
            float yb = lerp(4.5f, 2.0f, smooth(0, 0.25f, t)) + smooth(0.8f, 1, t) * 2.0f;
            float wTop = w * 0.7f;
            return new float[][]{{0, yb}, {w * 0.4f, yb}, {w, yb + 1.5f}, {w, hc * 0.6f}, {wTop, hc}, {0, hc + 0.3f},
                    {-wTop, hc}, {-w, hc * 0.6f}, {-w, yb + 1.5f}, {-w * 0.4f, yb}};
        }, (k, j, z, pa, pb) -> {
            if (j == 0 || j == 9) return null;
            if (j == 1 || j == 8) return "veh_under";
            if (j >= 4 && j <= 5) return z > -5 && z < 10 ? "veh_glass" : P;
            return P;
        }, true, true, 11, 2);
        rbox(g, "veh_leather:1A1A1E", -4.3f, 2.6f, -4, 4.3f, 3.6f, 3.5f, 0.4f);
        rbox(g, "veh_interior", -7.0f, 7.5f, 8.5f, 7.0f, 11.0f, 11, 0.4f);
        tube(g, gun, p(0, 9.2f, 10), p(0, 10.2f, 8), 0.3f, 0.3f, 6, false);
        g.push(); g.translate(0, 10.4f, 7.8f); g.rotX(-58); g.rotY(-a.lean * 70);
        torus(g, "veh_leather:1A1A1E", 2.9f, 0.3f, 12, 4); g.pop();
        for (int s = -1; s <= 1; s += 2) {
            beam(g, carbon, p(s * 6, 8.0f, -25), p(s * 6, 12.0f, -27), 0.5f, 1.2f, 0.5f, 1.0f, p(0, 0, 1));
        }
        box(g, carbon, -10.8f, 12.2f, -29, 10.8f, 12.6f, -25);
        box(g, carbon, -10.0f, 2.0f, 30, 10.0f, 2.8f, 34);
        for (int s = -1; s <= 1; s += 2) ellipsoid(g, gun, s * 12.0f, 6.5f, -11, 0.7f, 1.3f, 3.0f, 8, 4);
        boolean night = isNight();
        if (night) g.glow(true);
        for (int s = -1; s <= 1; s += 2) {
            rbox(g, night ? "veh_glow:FFF6D8" : "veh_flat:DDDDAA", s * 3 - 2.5f, 4.5f, 33.5f, s * 3 + 2.5f, 6.5f, 34.2f, 0.15f);
            tube(g, night ? "veh_glow:FF2A2A" : "veh_flat:882222", p(s * 3.5f, 5.5f, -30.5f), p(s * 8.0f, 5.2f, -30.2f), 0.4f, 0.35f, 8, true);
        }
        if (night) g.glow(false);
        for (int s = -1; s <= 1; s += 2) {
            beam(g, gun, p(s * 11.5f, 9.5f, 8), p(s * 13.5f, 10.2f, 8.5f), 0.35f, 0.3f);
            ellipsoid(g, "veh_mirror", s * 14.0f, 10.3f, 8.5f, 1.2f, 0.65f, 0.6f, 8, 4);
        }
        // air intakes (side scoops)
        for (int s = -1; s <= 1; s += 2) {
            rbox(g, carbon, s < 0 ? -13.0f : 11.5f, 4.0f, -14, s < 0 ? -11.5f : 13.0f, 6.5f, -8, 0.2f);
            rbox(g, "veh_flat:0A0A0F", s < 0 ? -12.8f : 11.7f, 4.3f, -13.5f, s < 0 ? -11.7f : 12.8f, 6.2f, -8.5f, 0.1f);
        }
        // front splitter
        box(g, carbon, -12.5f, 1.8f, 32, 12.5f, 2.3f, 35.5f);
        // diffuser (rear bottom)
        for (int i = -3; i <= 3; i++)
            box(g, carbon, i * 3.0f - 1.0f, 1.8f, -30, i * 3.0f + 1.0f, 3.0f, -26);
        // exhaust tips (quad exhaust)
        for (int sx = -1; sx <= 1; sx += 2)
            for (int i = 0; i < 2; i++) {
                float xo = sx * (2.5f + i * 2.8f);
                tube(g, "veh_chrome", p(xo, 3.2f, -29), p(xo, 3.2f, -31.5f), 0.55f, 0.65f, 8, true);
                tube(g, "veh_flat:0A0A0F", p(xo, 3.2f, -29.5f), p(xo, 3.2f, -31.3f), 0.4f, 0.5f, 8, true);
            }
        // underglow
        g.glow(true);
        float glowPulse = 0.6f + 0.15f * sin(a.time * 3.5f);
        String ug = "veh_glow:" + VehCar.hex(0xFFCD03);
        rbox(g, ug, -10.0f, 1.6f, -24, 10.0f, 1.9f, 28, 0.05f);
        for (int s = -1; s <= 1; s += 2)
            rbox(g, ug, s < 0 ? -12.5f : 11.0f, 1.6f, -18, s < 0 ? -11.0f : 12.5f, 1.9f, 20, 0.05f);
        // exhaust flame when moving
        if (a.move > 0.3f) {
            float fl = a.move * (0.8f + 0.4f * sin(a.time * 22));
            for (int sx = -1; sx <= 1; sx += 2)
                for (int i = 0; i < 2; i++) {
                    float xo = sx * (2.5f + i * 2.8f);
                    float len = 1.5f + fl * 3.0f;
                    ellipsoid(g, "veh_glow:FF6600", xo, 3.2f, -31.5f - len * 0.5f, 0.35f, 0.35f, len * 0.5f, 6, 3);
                    ellipsoid(g, "veh_glow:FFCC00", xo, 3.2f, -31.5f - len * 0.25f, 0.2f, 0.2f, len * 0.3f, 6, 3);
                }
        }
        g.glow(false);
        g.pop();
    }
}
