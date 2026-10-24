package dev.lego.cosmetic;

import static dev.lego.cosmetic.VehGeo.*;

import dev.lego.cosmetic.Cos.A;

/** Vehicles with a handlebar: kick scooter, e-scooter, bicycle, moped, motorbike. */
final class VehRide {
    private VehRide() { }

    // ---- rider contact points (px, vehicle space); hands = {half grip distance, height, z}
    static final float KICK_TOP = 1.95f, ESC_TOP = 2.85f;
    static final float[] KICK_HAND = {5.25f, 17.3f, 7.8f}, ESC_HAND = {6.1f, 18.4f, 8.0f};
    static final float BIKE_SEAT = 13.0f, MOPED_SEAT = 12.6f, MOTO_SEAT = 13.2f;
    static final float[] BIKE_HAND = {5.45f, 14.6f, 7.8f}, MOPED_HAND = {5.6f, 15.6f, 10.6f}, MOTO_HAND = {4.45f, 13.95f, 10.05f};
    static final float GEAR = 0.45f, CRANK = 2.6f, PEDAL_X = 2.7f;
    static final float[] BB = {0, 4.4f, 2.6f};

    private static final String TREAD = "veh_tread", SIDE = "veh_side";

    /** Wheel (axle along x, centred at the origin of the current transform). */
    static void wheelAt(G g, float x, float y, float z, float R, float w, float rimR, int spokes, float spin, String rim, String disc) {
        g.push();
        g.translate(x, y, z);
        wheel(g, R, w, rimR, spokes, spin, TREAD, SIDE, rim, disc, 16);
        g.pop();
    }

    // ================================================================== kick scooter (Tretroller)
    static void kickScooter(G g, A a) {
        String alu = "veh_alu", red = "veh_c:E3242B", blk = "veh_flat:1C1D22";
        float wr = 1.7f, zf = 9.9f, zr = -8.4f;
        float spin = a.wheel * 1.8f;
        g.push();
        g.rotZ(-a.lean * 5);
        // deck (aluminium, grip tape on top)
        rbox(g, alu, -2.1f, 1.05f, -6.6f, 2.1f, 1.75f, 6.1f, 0.3f);
        box(g, "veh_griptape", -1.8f, 1.6f, -6.2f, 1.8f, KICK_TOP, 5.5f);
        box(g, red, -2.2f, 1.2f, -6.0f, 2.2f, 1.45f, 5.4f);                   // red side strip
        // rear wheel holder + wheel
        for (int s = -1; s <= 1; s += 2) beam(g, alu, p(s * 0.95f, 1.35f, -6.3f), p(s * 0.95f, wr, zr), 0.35f, 0.9f);
        wheelAt(g, 0, wr, zr, wr, 1.1f, 1.05f, 5, spin, "veh_pu", null);
        tube(g, "veh_chrome", p(-1.15f, wr, zr), p(1.15f, wr, zr), 0.22f, 0.22f, 6, true);
        // rear foot brake: fender over the wheel with a rubber step pad
        g.push();
        strip(g, blk, arc(0, wr, zr, wr + 0.35f, 70, 175, 8), 0.3f, 1.6f, p(1, 0, 0), true);
        box(g, "veh_rubber", -0.65f, wr + 2.05f, zr - 1.2f, 0.65f, wr + 2.35f, zr + 0.4f);
        g.pop();
        // neck
        beam(g, alu, p(0, 1.4f, 5.6f), p(0, 3.6f, 8.0f), 1.6f, 1.0f);
        // steering: head tube, fork, front wheel, T-bar
        float[] piv = {0, 5, 8.3f};
        g.push();
        steer(g, piv, 4, a.lean * 22);
        tube(g, red, p(0, 2.9f, 8.45f), p(0, 5.9f, 8.25f), 0.62f, 0.62f, 10, true);          // head tube
        rbox(g, alu, -1.15f, 2.55f, 8.0f, 1.15f, 3.2f, 9.0f, 0.2f);                                 // fork crown
        for (int s = -1; s <= 1; s += 2) beam(g, alu, p(s * 0.9f, 2.8f, 8.6f), p(s * 0.9f, wr, zf), 0.35f, 0.7f);
        wheelAt(g, 0, wr, zf, wr, 1.1f, 1.05f, 5, spin, "veh_pu", null);
        tube(g, "veh_chrome", p(-1.15f, wr, zf), p(1.15f, wr, zf), 0.22f, 0.22f, 6, true);
        tube(g, alu, p(0, 5.9f, 8.25f), p(0, 11.0f, 8.05f), 0.48f, 0.48f, 8, false);              // lower steerer
        tube(g, alu, p(0, 10.4f, 8.07f), p(0, 17.3f, 7.8f), 0.38f, 0.38f, 8, false);              // upper steerer
        g.push();
        g.translate(0, 10.6f, 8.06f);
        lathe(g, red, new float[][]{{0.01f, -0.5f}, {0.66f, -0.5f}, {0.7f, 0.5f}, {0.01f, 0.5f}}, 10);   // quick release clamp
        g.pop();
        beam(g, red, p(0.6f, 10.9f, 8.1f), p(0.9f, 9.2f, 8.7f), 0.25f, 0.2f);
        tube(g, alu, p(-5.6f, 17.3f, 7.8f), p(5.6f, 17.3f, 7.8f), 0.36f, 0.36f, 8, false);
        rbox(g, alu, -0.6f, 16.8f, 7.3f, 0.6f, 17.8f, 8.3f, 0.2f);
        for (int s = -1; s <= 1; s += 2) tube(g, "veh_foam", p(s * 3.9f, 17.3f, 7.8f), p(s * 6.6f, 17.3f, 7.8f), 0.62f, 0.62f, 10, true);
        g.pop();
        // kickstand (left)
        float fold = smooth(0.05f, 0.3f, a.move);
        beam(g, "veh_gun", p(-2.0f, 1.2f, -1.5f), lerp(p(-3.4f, 0.12f, -3.2f), p(-2.25f, 1.1f, -5.2f), fold), 0.3f, 0.3f);
        g.pop();
    }

    // ================================================================== e-scooter
    static void eScooter(G g, A a) {
        String body = "veh_c:2F323A", gun = "veh_gun", acc = "veh_c:1ED760";
        float wr = 2.3f, zf = 10.6f, zr = -9.6f;
        float spin = a.wheel * 1.3f;
        g.push();
        g.rotZ(-a.lean * 5);
        // deck with battery + rubber mat
        rbox(g, body, -2.5f, 1.45f, -7.2f, 2.5f, 2.6f, 6.6f, 0.45f);
        box(g, "veh_rubber", -2.05f, 2.45f, -6.6f, 2.05f, ESC_TOP, 6.0f);
        for (int s = -1; s <= 1; s += 2) box(g, acc, s < 0 ? -2.58f : 2.42f, 1.85f, -6.4f, s < 0 ? -2.42f : 2.58f, 2.05f, 5.6f);
        // rear: arms, wheel (disc brake), fender with brake light
        for (int s = -1; s <= 1; s += 2) beam(g, body, p(s * 1.05f, 2.0f, -6.9f), p(s * 1.05f, wr, zr), 0.4f, 1.0f);
        g.push();
        g.translate(0, wr, zr);
        g.rotY(180);
        wheel(g, wr, 1.3f, 1.45f, 5, -spin, TREAD, SIDE, "veh_gun", "veh_disc", 16);
        g.pop();
        tube(g, "veh_chrome", p(-1.35f, wr, zr), p(1.35f, wr, zr), 0.25f, 0.25f, 6, true);
        strip(g, body, arc(0, wr, zr, wr + 0.45f, 55, 192, 10), 0.28f, 1.7f, p(1, 0, 0), true);
        g.glow(true).color(argb((int) (200 + 55 * sin(a.time * 4)), 0xFFFFFF));
        float[] bl = arc(0, wr, zr, wr + 0.6f, 180, 180, 1)[0];
        rbox(g, "veh_glow:FF2A2A", -0.6f, bl[1] - 0.15f, bl[2] - 0.2f, 0.6f, bl[1] + 0.45f, bl[2] + 0.3f, 0.1f);
        g.glow(false).color(0xFFFFFFFF);
        // neck + folding joint
        beam(g, body, p(0, 2.0f, 5.9f), p(0, 4.5f, 8.5f), 1.8f, 1.2f);
        rbox(g, gun, -0.95f, 4.2f, 8.1f, 0.95f, 5.6f, 9.3f, 0.3f);
        tube(g, "veh_chrome", p(-1.1f, 4.9f, 8.4f), p(1.1f, 4.9f, 8.4f), 0.28f, 0.28f, 8, true);
        beam(g, acc, p(1.05f, 5.3f, 8.6f), p(1.05f, 4.0f, 7.5f), 0.22f, 0.3f);
        // steering assembly (turns around the slightly tilted stem)
        g.push();
        steer(g, p(0, 5.0f, 9.2f), 5, a.lean * 20);
        tube(g, body, p(0, 5.3f, 9.2f), p(0, 18.0f, 8.1f), 0.6f, 0.55f, 10, false);
        rbox(g, gun, -1.35f, 4.9f, 8.6f, 1.35f, 5.9f, 9.8f, 0.3f);                                     // fork crown
        for (int s = -1; s <= 1; s += 2) beam(g, gun, p(s * 1.05f, 5.2f, 9.4f), p(s * 1.05f, wr, zf), 0.45f, 0.7f);
        strip(g, body, arc(0, wr, zf, wr + 0.4f, 25, 150, 8), 0.26f, 1.6f, p(1, 0, 0), true);
        g.push();
        g.translate(0, wr, zf);
        wheel(g, wr, 1.3f, 1.5f, 0, spin, TREAD, SIDE, "veh_gun", null, 16);
        g.rotZ(-90);
        lathe(g, new String[]{gun, gun, acc, gun, gun}, new float[][]{{0.01f, -0.85f}, {1.1f, -0.85f}, {1.2f, -0.6f}, {1.2f, 0.6f}, {1.1f, 0.85f}, {0.01f, 0.85f}}, 14); // hub motor
        g.pop();
        // front light on the stem
        rbox(g, body, -0.75f, 14.9f, 8.3f, 0.75f, 16.4f, 9.2f, 0.25f);
        g.glow(true);
        ellipsoid(g, "veh_glow:FFFBE8", 0, 15.65f, 9.2f, 0.6f, 0.55f, 0.22f, 10, 5);
        g.glow(false);
        // T-bar with display, grips, brake lever, throttle, bell
        tube(g, gun, p(-7.0f, 18.4f, 8.0f), p(7.0f, 18.4f, 8.0f), 0.42f, 0.42f, 8, false);
        rbox(g, body, -1.4f, 17.6f, 7.2f, 1.4f, 18.95f, 8.9f, 0.35f);
        g.glow(true);
        box6(g, "veh_flat:0B1016", new String[]{null, null, null, null, VehicleWorld.displayTex(), null}, -1.0f, 18.85f, 7.45f, 1.0f, 19.05f, 8.65f);
        g.glow(false);
        for (int s = -1; s <= 1; s += 2) tube(g, "veh_gripr", p(s * 4.9f, 18.4f, 8.0f), p(s * 7.3f, 18.4f, 8.0f), 0.62f, 0.62f, 10, true);
        rbox(g, gun, -4.6f, 18.1f, 7.6f, -3.8f, 18.9f, 8.5f, 0.2f);
        beam(g, gun, p(-4.3f, 18.6f, 8.5f), p(-6.8f, 18.3f, 9.2f), 0.3f, 0.25f);                    // brake lever
        rbox(g, gun, 3.9f, 17.9f, 8.3f, 4.5f, 18.9f, 8.75f, 0.12f);                                   // throttle
        g.push();
        g.translate(-3.0f, 18.75f, 8.0f);
        lathe(g, "veh_chrome", new float[][]{{0.01f, 0}, {0.6f, 0}, {0.5f, 0.35f}, {0.01f, 0.45f}}, 10);   // bell
        g.pop();
        g.pop();
        // kickstand (left)
        float fold = smooth(0.05f, 0.3f, a.move);
        beam(g, gun, p(-2.5f, 1.75f, -0.8f), lerp(p(-3.8f, 0.12f, -2.8f), p(-2.65f, 1.4f, -5.2f), fold), 0.35f, 0.35f);
        g.pop();
    }

    // ================================================================== bicycle
    static final float BR = 5.4f, BRIM = 4.75f;
    static final float[] RA = {0, 5.4f, -4.2f}, FA = {0, 5.4f, 13.0f};

    static void bikeWheel(G g, float spin) {
        g.push();
        g.rotX(spin);
        g.rotZ(-90);
        lathe(g, new String[]{SIDE, TREAD, TREAD, SIDE}, new float[][]{{BRIM, -0.3f}, {BR - 0.25f, -0.4f}, {BR, 0}, {BR - 0.25f, 0.4f}, {BRIM, 0.3f}}, 22);
        lathe(g, "veh_alu", new float[][]{{BRIM, 0.3f}, {BRIM - 0.5f, 0.22f}, {BRIM - 0.5f, -0.22f}, {BRIM, -0.3f}}, 22);
        lathe(g, "veh_gun", new float[][]{{0.01f, -0.75f}, {0.3f, -0.75f}, {0.7f, -0.65f}, {0.7f, -0.5f}, {0.32f, -0.45f}, {0.32f, 0.45f}, {0.7f, 0.5f},
                {0.7f, 0.65f}, {0.3f, 0.75f}, {0.01f, 0.75f}}, 10);
        int n = 20;
        for (int i = 0; i < n; i++) {
            double an = Math.PI * 2 * i / n, ar = an + (i % 2 == 0 ? 0.28 : -0.28);
            float hy = (i % 4 < 2) ? -0.58f : 0.58f;
            tube(g, "veh_chrome", p((float) Math.cos(an) * 0.6f, hy, (float) Math.sin(an) * 0.6f),
                    p((float) Math.cos(ar) * (BRIM - 0.45f), 0, (float) Math.sin(ar) * (BRIM - 0.45f)), 0.07f, 0.07f, 3, false);
        }
        g.pop();
    }

    static void bike(G g, A a) {
        String fr = "veh_c:2FA58C", cr = "veh_chrome", gun = "veh_gun", brown = "veh_leather:6A4424";
        float spin = a.wheel * 0.55f;
        float crank = Vehicles.pedalAngle(a.wheel);
        g.push();
        g.rotZ(-a.lean * 6);
        // rear wheel + cog
        g.push();
        g.translate(RA[0], RA[1], RA[2]);
        bikeWheel(g, spin);
        g.pop();
        g.push();
        g.translate(1.25f, RA[1], RA[2]);
        g.rotX(spin);
        g.rotZ(-90);
        lathe(g, gun, new float[][]{{0.01f, -0.12f}, {0.95f, -0.12f}, {0.95f, 0.12f}, {0.01f, 0.12f}}, 12);
        g.pop();
        // frame (step-through city bike)
        pipe(g, fr, new float[][]{BB, {0, 11.8f, -0.25f}}, 0.36f, 8, false);
        pipe(g, fr, new float[][]{{0, 10.4f, 10.95f}, {0, 7.4f, 8.8f}, {0, 5.3f, 5.4f}, {0, 4.5f, 2.9f}}, 0.4f, 8, false);
        pipe(g, fr, new float[][]{{0, 11.8f, 10.6f}, {0, 9.2f, 8.1f}, {0, 7.8f, 4.4f}, {0, 7.6f, 1.4f}}, 0.32f, 8, false);
        for (int s = -1; s <= 1; s += 2) {
            pipe(g, fr, new float[][]{{s * 0.3f, 4.4f, 2.3f}, {s * 0.78f, 5.4f, -4.2f}}, 0.24f, 6, false);
            pipe(g, fr, new float[][]{{s * 0.22f, 11.1f, -0.1f}, {s * 0.78f, 5.4f, -4.2f}}, 0.22f, 6, false);
        }
        tube(g, fr, p(0, 9.6f, 11.28f), p(0, 12.75f, 10.3f), 0.52f, 0.52f, 10, true);          // head tube
        tube(g, fr, p(-0.9f, BB[1], BB[2]), p(0.9f, BB[1], BB[2]), 0.55f, 0.55f, 10, true);    // bottom bracket shell
        tube(g, cr, p(0, 11.5f, -0.2f), p(0, 12.3f, -0.43f), 0.27f, 0.27f, 6, false);          // seat post
        // saddle with springs
        loft(g, range(-2.5f, 2.9f, 0.6f), z -> {
            float t = (z + 2.5f) / 5.4f;
            float w = t < 0.12f ? lerp(1.6f, 2.3f, t / 0.12f) : lerp(2.3f, 0.55f, smooth(0.2f, 0.85f, t));
            float top = BIKE_SEAT - 0.05f - 0.25f * sin(t * Math.PI) + (t < 0.1f ? (0.1f - t) * 1.5f : 0), bot = 11.95f;
            int n = 12;
            float[][] r = new float[n][];
            for (int i = 0; i < n; i++) {
                double an = -Math.PI / 2 + Math.PI * 2 * i / n;
                float c = (float) Math.cos(an), sn = (float) Math.sin(an);
                r[i] = new float[]{c * w, sn < 0 ? bot + (1 + sn) * (top - bot) * 0.5f : bot + (top - bot) * (0.5f + 0.5f * (float) Math.sqrt(sn))};
            }
            return r;
        }, (k, j, z, pa, pb) -> brown, true, true, BIKE_SEAT, 11.9f);
        for (int s = -1; s <= 1; s += 2) tube(g, cr, p(s * 1.2f, 11.7f, -1.9f), p(s * 1.2f, 12.25f, -1.9f), 0.25f, 0.25f, 6, true);
        tube(g, cr, p(-1.2f, 11.75f, -1.9f), p(1.2f, 11.75f, -1.9f), 0.12f, 0.12f, 4, false);
        // rear rack + fender + light
        for (int s = -1; s <= 1; s += 2) {
            pipe(g, cr, new float[][]{{s * 1.9f, 10.9f, -1.6f}, {s * 1.9f, 10.9f, -9.4f}, {s * 0.9f, 5.4f, -4.4f}}, 0.16f, 6, true);
        }
        for (int i = 0; i < 3; i++) tube(g, cr, p(-1.9f, 10.9f, -3.6f - i * 2.4f), p(1.9f, 10.9f, -3.6f - i * 2.4f), 0.14f, 0.14f, 4, false);
        strip(g, fr, arc(0, RA[1], RA[2], BR + 0.55f, 20, 200, 12), 0.2f, 1.4f, p(1, 0, 0), true);
        g.glow(true);
        rbox(g, "veh_glow:FF2A2A", -0.6f, 10.2f, -9.75f, 0.6f, 10.75f, -9.35f, 0.12f);
        g.glow(false);
        // crankset
        g.push();
        g.translate(1.25f, BB[1], BB[2]);
        g.rotX(crank);
        g.rotZ(-90);
        lathe(g, new String[]{gun, "veh_alu", gun}, new float[][]{{0.01f, -0.12f}, {2.0f, -0.12f}, {2.0f, 0.12f}, {0.01f, 0.12f}}, 18);
        g.pop();
        g.push();
        g.translate(0, BB[1], BB[2]);
        g.rotX(crank);
        tube(g, "veh_alu", p(-1.75f, 0, 0), p(1.75f, 0, 0), 0.3f, 0.3f, 6, true);
        beam(g, "veh_alu", p(1.65f, 0, 0), p(1.65f, -CRANK, 0), 0.4f, 0.55f, 0.35f, 0.45f, p(0, 0, 1));
        beam(g, "veh_alu", p(-1.65f, 0, 0), p(-1.65f, CRANK, 0), 0.4f, 0.55f, 0.35f, 0.45f, p(0, 0, 1));
        g.pop();
        float[][] ped = Vehicles.pedals("veh_bike", a.wheel);
        for (float[] pp : ped) {
            float sx = Math.signum(pp[0]);
            tube(g, "veh_alu", p(sx * 1.7f, pp[1], pp[2]), p(sx * 1.95f, pp[1], pp[2]), 0.14f, 0.14f, 4, false);
            rbox(g, "veh_flat:1C1D22", pp[0] - 0.8f, pp[1] - 0.2f, pp[2] - 0.6f, pp[0] + 0.8f, pp[1] + 0.2f, pp[2] + 0.6f, 0.1f);
        }
        chain(g, crank);
        // kickstand (left)
        float fold = smooth(0.05f, 0.3f, a.move);
        beam(g, gun, p(-0.7f, 4.6f, -0.8f), lerp(p(-2.6f, 0.12f, -1.8f), p(-0.9f, 4.4f, -4.6f), fold), 0.3f, 0.3f);
        // ---- steering: fork, front wheel, fender, bars, lamp, basket
        g.push();
        steer(g, p(0, 11.2f, 10.75f), 17.8f, a.lean * 22);
        rbox(g, fr, -1.1f, 9.1f, 10.8f, 1.1f, 9.8f, 11.8f, 0.2f);
        for (int s = -1; s <= 1; s += 2) pipe(g, fr, new float[][]{{s * 0.75f, 9.5f, 11.3f}, {s * 0.75f, 7.3f, 12.25f}, {s * 0.8f, FA[1], FA[2]}}, 0.24f, 6, false);
        g.push();
        g.translate(FA[0], FA[1], FA[2]);
        bikeWheel(g, spin);
        g.pop();
        strip(g, fr, arc(0, FA[1], FA[2], BR + 0.55f, 5, 160, 11), 0.2f, 1.4f, p(1, 0, 0), true);
        tube(g, cr, p(0, 12.6f, 10.35f), p(0, 14.2f, 10.1f), 0.3f, 0.3f, 6, true);
        pipe(g, cr, new float[][]{{-5.9f, 14.6f, 7.2f}, {-4.6f, 14.6f, 8.8f}, {-2.5f, 14.35f, 10.1f}, {0, 14.2f, 10.25f}, {2.5f, 14.35f, 10.1f}, {4.6f, 14.6f, 8.8f}, {5.9f, 14.6f, 7.2f}}, 0.22f, 6, false);
        for (int s = -1; s <= 1; s += 2) tube(g, brown, p(s * 4.85f, 14.6f, 8.65f), p(s * 6.05f, 14.6f, 7.0f), 0.45f, 0.45f, 8, true);
        g.push();
        g.translate(-2.6f, 14.55f, 10.1f);
        lathe(g, cr, new float[][]{{0.01f, 0}, {0.55f, 0}, {0.45f, 0.35f}, {0.01f, 0.42f}}, 8);
        g.pop();
        g.push();
        g.translate(0, 9.4f, 12.3f);
        g.rotX(90);
        lathe(g, cr, new float[][]{{0.01f, -0.9f}, {0.45f, -0.9f}, {0.8f, -0.1f}, {0.8f, 0}}, 10);
        g.glow(true);
        lathe(g, "veh_glow:FFF6D8", new float[][]{{0.8f, 0}, {0.01f, 0.15f}}, 10);
        g.glow(false);
        g.pop();
        // wicker basket on a small carrier
        float bz0 = 12.2f, bz1 = 16.4f, by0 = 11.6f, by1 = 14.4f;
        box(g, "veh_basket", -3.0f, by0, bz0, 3.0f, by0 + 0.3f, bz1);
        box(g, "veh_basket", -3.0f, by0 + 0.3f, bz0, 3.0f, by1, bz0 + 0.3f);
        box(g, "veh_basket", -3.0f, by0 + 0.3f, bz1 - 0.3f, 3.0f, by1, bz1);
        box(g, "veh_basket", -3.0f, by0 + 0.3f, bz0 + 0.3f, -2.7f, by1 - 0.02f, bz1 - 0.3f);
        box(g, "veh_basket", 2.7f, by0 + 0.3f, bz0 + 0.3f, 3.0f, by1 - 0.02f, bz1 - 0.3f);
        pipe(g, cr, new float[][]{{-1.5f, by0 - 0.1f, bz0 + 0.5f}, {-0.8f, 9.8f, 12.0f}}, 0.12f, 4, false);
        pipe(g, cr, new float[][]{{1.5f, by0 - 0.1f, bz0 + 0.5f}, {0.8f, 9.8f, 12.0f}}, 0.12f, 4, false);
        g.pop();
        g.pop();
    }

    /** Bicycle chain: links running around the chain ring and the rear cog (right side). */
    private static void chain(G g, float crank) {
        float rr = 2.0f, rc = 0.95f, x = 1.25f;
        java.util.List<float[]> path = new java.util.ArrayList<>();
        for (float[] q : arc(x, BB[1], BB[2], rr, -90, 90, 10)) path.add(q);
        for (float[] q : arc(x, RA[1], RA[2], rc, 90, 270, 6)) path.add(q);
        path.add(path.get(0));
        int m = path.size();
        float[] acc = new float[m];
        for (int i = 1; i < m; i++) {
            float[] a = path.get(i - 1), b = path.get(i);
            acc[i] = acc[i - 1] + (float) Math.sqrt((b[1] - a[1]) * (b[1] - a[1]) + (b[2] - a[2]) * (b[2] - a[2]));
        }
        float L = acc[m - 1];
        int n = 34;
        float step = L / n, phase = (float) Math.toRadians(crank) * rr;
        for (int i = 0; i < n; i++) {
            float s0 = ((i * step - phase) % L + L) % L;
            float[] a = at(path, acc, s0), b = at(path, acc, Math.min(L, s0 + step * 0.78f));
            beam(g, "veh_gun", a, b, 0.44f, 0.28f);
        }
    }

    private static float[] at(java.util.List<float[]> path, float[] acc, float s) {
        int i = 1;
        while (i < acc.length - 1 && acc[i] < s) i++;
        float t = acc[i] == acc[i - 1] ? 0 : (s - acc[i - 1]) / (acc[i] - acc[i - 1]);
        return lerp(path.get(i - 1), path.get(i), clamp(t, 0, 1));
    }

    // ================================================================== moped (Vespa style)
    static void moped(G g, A a) {
        String P = "veh_paint:8FD9C8", cr = "veh_chrome", gun = "veh_gun";
        float wr = 3.4f, zr = -10.5f, zf = 13.6f;
        float spin = a.wheel * 0.9f;
        g.push();
        g.rotZ(-a.lean * 8);
        // rear wheel (mostly hidden by the cowls)
        wheelAt(g, 0, wr, zr, wr, 1.9f, 2.1f, 0, spin, "veh_rim", null);
        // rear cowl (bulbous side panels)
        float[] cz = {-15.6f, -13.5f, -9, -4, 0, 1.6f};
        float[] cw = {1.8f, 4.4f, 5.6f, 5.4f, 4.3f, 3.0f};
        float[] ct = {9.0f, 10.8f, 11.3f, 11.3f, 11.2f, 10.2f};
        float[] cb = {7.4f, 5.4f, 4.4f, 4.2f, 4.2f, 4.8f};
        loft(g, range(-15.6f, 1.6f, 1.1f), z -> {
            float w = curve(z, cz, cw), t = curve(z, cz, ct), b = curve(z, cz, cb);
            int n = 18;
            float[][] r = new float[n][];
            for (int i = 0; i < n; i++) {
                double an = -Math.PI / 2 + Math.PI * 2 * i / n;
                float[] q = Geo.sq(an, 1, 2.6f);
                float mid = b + (t - b) * 0.42f;
                float yy = q[1] < 0 ? b + (1 + q[1]) * (mid - b) : mid + q[1] * (t - mid);
                float fx = q[1] > 0 ? 1 - 0.38f * q[1] * q[1] : 1 - 0.12f * q[1] * q[1];   // narrow towards the seat, bulging sides
                r[i] = new float[]{q[0] * w * fx, yy};
            }
            return r;
        }, (k, j, z, pa, pb) -> P, true, true, 11.5f, 4f);
        // seat, grab rail, tail light
        rbox(g, "veh_leather:3A2A22", -3.3f, 10.9f, -11.0f, 3.3f, MOPED_SEAT, 2.2f, 0.8f);
        pipe(g, cr, new float[][]{{-2.6f, 11.4f, -11.0f}, {-2.6f, 12.4f, -12.6f}, {2.6f, 12.4f, -12.6f}, {2.6f, 11.4f, -11.0f}}, 0.2f, 6, true);
        g.glow(true);
        rbox(g, "veh_glow:FF2A2A", -1.2f, 8.0f, -16.0f, 1.2f, 9.3f, -15.0f, 0.3f);
        g.glow(false);
        // exhaust (right)
        g.push();
        g.translate(3.3f, 3.8f, -9.5f);
        g.rotX(-90);
        lathe(g, new String[]{cr, cr, cr, gun, gun, gun}, new float[][]{{0.01f, -3.6f}, {0.8f, -3.6f}, {1.05f, -2.8f}, {1.05f, 2.4f}, {0.35f, 2.8f}, {0.35f, 2.5f}, {0.01f, 2.5f}}, 12);
        g.pop();
        pipe(g, cr, new float[][]{{3.3f, 3.8f, -12.1f}, {3.3f, 3.8f, -13.6f}}, 0.3f, 6, true);
        // floor board with rubber strips
        rbox(g, P, -3.9f, 2.6f, -1.2f, 3.9f, 3.6f, 9.4f, 0.4f);
        for (int i = 0; i < 5; i++) box(g, "veh_rubber", -3.2f, 3.5f, -0.2f + i * 1.8f, 3.2f, 3.75f, 0.8f + i * 1.8f);
        box(g, gun, -2.4f, 1.3f, -2.0f, 2.4f, 2.7f, 6.0f);                                        // tunnel / engine mount
        // centre stand
        float fold = smooth(0.05f, 0.3f, a.move);
        for (int s = -1; s <= 1; s += 2) beam(g, gun, p(s * 2.2f, 2.0f, -3.0f), lerp(p(s * 2.6f, 0.1f, -3.8f), p(s * 2.4f, 1.4f, -6.2f), fold), 0.35f, 0.35f);
        // leg shield (curved, lofted along y)
        g.push();
        g.rotX(-90);                                   // local z = world y, local y = -world z
        loft(g, range(3.3f, 16.2f, 1.0f), y -> {
            float W = y < 9 ? lerp(3.9f, 5.4f, smooth(3.3f, 9, y)) : lerp(5.4f, 3.0f, smooth(9, 16.2f, y));
            float z0 = 8.9f - (y - 3.3f) * 0.12f, bulge = 1.1f;
            int n = 8;
            float[][] r = new float[2 * n + 2][];
            for (int i = 0; i <= n; i++) {
                float x = -W + 2 * W * i / n, t = x / W;
                r[i] = new float[]{x, -(z0 + bulge * (1 - t * t))};
            }
            for (int i = 0; i <= n; i++) {
                float x = W - 2 * W * i / n, t = x / W;
                r[n + 1 + i] = new float[]{x, -(z0 + bulge * (1 - t * t) - 0.45f)};
            }
            return r;
        }, (k, j, z, pa, pb) -> P, true, true, -12, -4, false, true);
        g.pop();
        beam(g, cr, p(0, 12.6f, 10.2f), p(0, 7.0f, 10.8f), 0.9f, 0.35f, 0.5f, 0.3f, p(0, 0, 1));    // horn cover strip
        // ---- steering assembly
        g.push();
        steer(g, p(0, 10, 11.9f), 14.4f, a.lean * 20);
        pipe(g, gun, new float[][]{{0, 7.9f, 12.8f}, {0, 16.0f, 10.5f}}, 0.45f, 8, false);
        beam(g, gun, p(-1.4f, 8.2f, 12.7f), p(-1.4f, wr, zf), 0.45f, 0.8f);                       // single sided link
        rbox(g, gun, -1.75f, 7.6f, 12.1f, 0.4f, 8.6f, 13.4f, 0.25f);
        strip(g, P, arc(0, wr, zf, wr + 0.5f, 10, 150, 10), 0.3f, 2.5f, p(1, 0, 0), true);
        wheelAt(g, 0, wr, zf, wr, 1.9f, 2.1f, 0, spin, "veh_rim", null);
        ellipsoid(g, P, 0, 16.4f, 10.4f, 2.6f, 1.0f, 1.9f, 14, 7);                                 // headset
        g.push();
        g.translate(0, 16.3f, 11.7f);
        g.rotX(90);
        lathe(g, cr, new float[][]{{0.01f, -0.6f}, {1.05f, -0.6f}, {1.15f, 0.1f}, {1.0f, 0.2f}}, 14);
        g.glow(true);
        lathe(g, "veh_glow:FFF6D8", new float[][]{{1.0f, 0.2f}, {0.01f, 0.45f}}, 14);
        g.glow(false);
        g.pop();
        ellipsoid(g, "veh_flat:15161A", 0, 17.5f, 9.6f, 0.95f, 0.3f, 0.75f, 10, 5);             // speedo
        tube(g, cr, p(-5.7f, 15.6f, 10.6f), p(5.7f, 15.6f, 10.6f), 0.35f, 0.35f, 8, false);
        for (int s = -1; s <= 1; s += 2) {
            tube(g, "veh_gripr", p(s * 4.6f, 15.6f, 10.6f), p(s * 6.6f, 15.6f, 10.6f), 0.62f, 0.62f, 10, true);
            pipe(g, cr, new float[][]{{s * 3.8f, 15.9f, 10.4f}, {s * 5.0f, 18.4f, 10.1f}}, 0.12f, 4, false);
            g.push();
            g.translate(s * 5.05f, 18.8f, 10.0f);
            g.rotX(-90);
            lathe(g, new String[]{cr, cr, "veh_mirror"}, new float[][]{{0.01f, -0.25f}, {0.95f, -0.25f}, {0.95f, 0.05f}, {0.01f, 0.1f}}, 12);
            g.pop();
        }
        g.pop();
        g.pop();
    }

    // ================================================================== motorbike (sport bike)
    static void motorbike(G g, A a) {
        String P = "veh_paint:FF7A00", blk = "veh_flat:1A1B20", alu = "veh_c:A9B0BC", gun = "veh_gun", cr = "veh_chrome", gold = "veh_c:E0A21A";
        float wr = 4.4f, zr = -10.0f, zf = 15.2f;
        float spin = a.wheel * 0.7f;
        g.push();
        g.rotZ(-a.lean * 14);
        // rear wheel
        g.push();
        g.translate(0, wr, zr);
        g.rotY(180);
        wheel(g, wr, 2.8f, 3.1f, 5, -spin, TREAD, SIDE, "veh_c:1A1B20", "veh_disc", 14);
        g.pop();
        // swing arm, frame spars, rear shock
        for (int s = -1; s <= 1; s += 2) {
            strip(g, alu, new float[][]{{s * 2.0f, 7.4f, -1.4f}, {s * 1.85f, 5.8f, -6.0f}, {s * 1.75f, wr, zr}}, 1.3f, 0.55f, p(1, 0, 0), true);
            strip(g, alu, new float[][]{{s * 1.5f, 13.3f, 10.2f}, {s * 3.0f, 11.3f, 4.0f}, {s * 2.9f, 9.2f, -0.2f}, {s * 2.2f, 7.4f, -1.6f}}, 2.0f, 0.8f, p(1, 0, 0), true);
        }
        tube(g, cr, p(-2.3f, 7.4f, -1.5f), p(2.3f, 7.4f, -1.5f), 0.35f, 0.35f, 8, true);
        tube(g, gold, p(0, 7.3f, -4.3f), p(0, 11.3f, -1.8f), 0.55f, 0.55f, 8, true);
        // engine block, cylinder head, radiator, side cover
        rbox(g, gun, -2.7f, 3.4f, -1.8f, 2.7f, 9.0f, 7.0f, 0.8f);
        g.push();
        g.translate(0, 8.4f, 5.4f);
        g.rotX(28);
        rbox(g, alu, -2.5f, 0, -1.9f, 2.5f, 3.2f, 1.9f, 0.4f);
        for (int i = 0; i < 4; i++) box(g, alu, -2.8f, 0.3f + i * 0.7f, -2.1f, 2.8f, 0.6f + i * 0.7f, 2.1f);
        g.pop();
        box6(g, blk, new String[]{"veh_grille", null, null, null, null, null}, -3.0f, 6.2f, 6.9f, 3.0f, 11.2f, 7.8f);
        g.push();
        g.translate(2.7f, 5.8f, 2.0f);
        g.rotZ(-90);
        lathe(g, new String[]{alu, alu, cr}, new float[][]{{2.0f, -0.3f}, {2.0f, 0.2f}, {1.2f, 0.45f}, {0.01f, 0.5f}}, 14);
        g.pop();
        // exhaust: headers under the engine to a side silencer
        for (int s = -1; s <= 1; s += 2)
            pipe(g, cr, new float[][]{{s * 1.1f, 7.0f, 7.6f}, {s * 1.1f, 3.6f, 8.1f}, {s * 0.9f, 2.4f, 5.0f}, {1.3f, 2.3f, 0.5f}, {2.3f, 2.6f, -2.2f}, {3.3f + s * 0.25f, 4.5f, -4.8f}}, 0.4f, 6, true);
        float[] s0 = p(3.3f, 4.6f, -4.8f), s1 = p(3.9f, 7.3f, -10.6f);
        tube(g, "veh_carbon", s0, s1, 1.15f, 0.95f, 12, true);
        tube(g, cr, lerp(s0, s1, -0.04f), lerp(s0, s1, 0.05f), 1.22f, 1.22f, 12, true);
        tube(g, cr, lerp(s0, s1, 0.97f), lerp(s0, s1, 1.06f), 1.0f, 0.75f, 12, true);
        tube(g, blk, lerp(s0, s1, 1.05f), lerp(s0, s1, 1.07f), 0.5f, 0.5f, 8, true);
        // chain (left)
        g.push();
        g.translate(-1.6f, wr, zr);
        g.rotX(spin);
        g.rotZ(-90);
        lathe(g, gun, new float[][]{{0.01f, -0.15f}, {1.8f, -0.15f}, {1.8f, 0.15f}, {0.01f, 0.15f}}, 16);
        g.pop();
        strip(g, "veh_chain", new float[][]{{-1.6f, wr + 1.85f, zr}, {-1.6f, 7.9f, -0.2f}}, 0.3f, 0.46f, p(1, 0, 0), true);
        strip(g, "veh_chain", new float[][]{{-1.6f, wr - 1.85f, zr}, {-1.6f, 5.6f, -0.2f}}, 0.3f, 0.46f, p(1, 0, 0), true);
        // foot pegs
        for (int s = -1; s <= 1; s += 2) {
            beam(g, alu, p(s * 2.4f, 7.2f, -1.9f), p(s * 2.9f, 6.2f, -3.4f), 0.5f, 0.35f);
            tube(g, "veh_rubber", p(s * 2.8f, 6.2f, -3.2f), p(s * 4.3f, 6.2f, -3.2f), 0.3f, 0.3f, 6, true);
        }
        // fuel tank
        loft(g, range(0.4f, 8.8f, 1.2f), z -> {
            float t = (z - 0.4f) / 8.4f;
            float w = lerp(3.0f, 4.3f, smooth(0, 0.35f, t)) - smooth(0.7f, 1, t) * 1.3f;
            float top = lerp(13.2f, 14.9f, smooth(0, 0.45f, t)) - smooth(0.75f, 1, t) * 0.9f, bot = 10.4f;
            int n = 16;
            float[][] r = new float[n][];
            for (int i = 0; i < n; i++) {
                double an = -Math.PI / 2 + Math.PI * 2 * i / n;
                float[] q = Geo.sq(an, 1, 2.5f);
                r[i] = new float[]{q[0] * w, q[1] < 0 ? bot + (1 + q[1]) * 1.2f : bot + 1.2f + q[1] * (top - bot - 1.2f)};
            }
            return r;
        }, (k, j, z, pa, pb) -> P, true, true, 15, 10);
        g.push();
        g.translate(0, 14.65f, 5.4f);
        lathe(g, alu, new float[][]{{0.01f, 0}, {0.9f, 0}, {0.9f, 0.45f}, {0.01f, 0.5f}}, 10);   // filler cap
        g.pop();
        // seat + tail
        rbox(g, "veh_leather:1A1A1E", -2.9f, 11.9f, -5.6f, 2.9f, MOTO_SEAT, 1.3f, 0.6f);
        loft(g, range(-15.2f, -4.6f, 1.5f), z -> {
            float t = (z + 15.2f) / 10.6f;
            float w = lerp(1.2f, 3.3f, t), bot = lerp(13.0f, 10.4f, t), top = lerp(14.5f, 13.0f, t * t) + (t > 0.2f && t < 0.75f ? 0.6f * sin((t - 0.2f) / 0.55f * Math.PI) : 0);
            return new float[][]{{0, bot}, {w * 0.8f, bot}, {w, bot + (top - bot) * 0.4f}, {w * 0.85f, top - 0.2f}, {w * 0.4f, top}, {0, top}, {-w * 0.4f, top}, {-w * 0.85f, top - 0.2f}, {-w, bot + (top - bot) * 0.4f}, {-w * 0.8f, bot}};
        }, (k, j, z, pa, pb) -> P, true, true, 15, 10);
        g.glow(true);
        ellipsoid(g, "veh_glow:FF2A2A", 0, 13.7f, -15.15f, 1.0f, 0.35f, 0.3f, 10, 5);
        g.glow(false);
        beam(g, blk, p(0, 12.2f, -13.0f), p(0, 9.4f, -15.6f), 1.6f, 0.3f, 1.6f, 0.3f, p(0, 0, 1));    // plate holder
        box6(g, blk, new String[]{null, "veh_plate", null, null, null, null}, -2.0f, 8.2f, -16.0f, 2.0f, 10.2f, -15.6f);
        // ---- steering: forks, front wheel, fender, fairing, screen, clip-ons
        g.push();
        steer(g, p(0, 9.1f, 13.1f), 24, a.lean * 14);
        for (int s = -1; s <= 1; s += 2) {
            tube(g, gold, p(s * 1.75f, 14.0f, 10.9f), p(s * 1.75f, 8.4f, 13.4f), 0.62f, 0.62f, 10, true);
            tube(g, blk, p(s * 1.75f, 8.8f, 13.2f), p(s * 1.75f, wr, zf), 0.45f, 0.45f, 8, true);
            rbox(g, "veh_c:D8141B", s < 0 ? -1.9f : 0.9f, wr + 1.1f, zf - 2.4f, s < 0 ? -0.9f : 1.9f, wr + 2.6f, zf - 0.9f, 0.2f);   // calipers
        }
        rbox(g, alu, -2.6f, 13.4f, 10.2f, 2.6f, 14.1f, 11.7f, 0.25f);                             // top clamp
        g.push();
        g.translate(0, wr, zf);
        wheel(g, wr, 2.2f, 3.1f, 5, spin, TREAD, SIDE, "veh_c:1A1B20", "veh_disc", 14);
        g.pop();
        strip(g, P, arc(0, wr, zf, wr + 0.45f, 25, 150, 10), 0.25f, 2.0f, p(1, 0, 0), true);
        for (int s = -1; s <= 1; s += 2) {
            tube(g, alu, p(s * 1.6f, 13.95f, 10.6f), p(s * 4.2f, 13.95f, 10.1f), 0.3f, 0.3f, 6, false);
            tube(g, "veh_gripr", p(s * 3.6f, 14.0f, 10.2f), p(s * 5.3f, 14.0f, 9.9f), 0.55f, 0.55f, 10, true);
        }
        g.pop();
        // fairing (fixed to the frame), windscreen, headlights, mirrors
        loft(g, range(8.0f, 18.4f, 1.3f), z -> {
            float t = (z - 8.0f) / 10.4f;
            float w = lerp(4.4f, 1.4f, t * t), top = lerp(16.0f, 13.2f, t * t), bot = curve(z, new float[]{8, 10.5f, 12, 18.4f}, new float[]{8.4f, 9.2f, 10.0f, 10.8f});
            float mid = (top + bot) / 2;
            float h = top - bot, y2 = bot + h * 0.25f, y3 = bot + h * 0.6f, y4 = bot + h * 0.88f;
            return new float[][]{{0, bot}, {w * 0.7f, bot}, {w, y2}, {w, y3}, {w * 0.8f, y4}, {w * 0.35f, top}, {0, top},
                    {-w * 0.35f, top}, {-w * 0.8f, y4}, {-w, y3}, {-w, y2}, {-w * 0.7f, bot}};
        }, (k, j, z, pa, pb) -> P, true, true, 16.5f, 5.5f);
        g.push();
        g.translate(0, 15.1f, 12.6f);
        g.rotX(-40);
        rbox(g, "veh_tint", -2.6f, 0, -0.1f, 2.6f, 4.2f, 0.1f, 0.05f);
        g.pop();
        g.glow(true);
        for (int s = -1; s <= 1; s += 2) ellipsoid(g, "veh_glow:FFF6D8", s * 1.25f, 12.6f, 17.4f, 1.05f, 0.5f, 0.85f, 10, 5);
        g.glow(false);
        for (int s = -1; s <= 1; s += 2) {
            beam(g, blk, p(s * 3.6f, 15.0f, 11.0f), p(s * 5.0f, 15.6f, 11.4f), 0.3f, 0.3f);
            ellipsoid(g, blk, s * 5.4f, 15.7f, 11.4f, 1.0f, 0.55f, 0.5f, 10, 5);
            ellipsoid(g, "veh_mirror", s * 5.4f, 15.7f, 10.95f, 0.85f, 0.45f, 0.12f, 10, 5);
        }
        g.pop();
    }
}
