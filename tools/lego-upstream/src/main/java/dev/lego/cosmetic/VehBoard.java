package dev.lego.cosmetic;

import static dev.lego.cosmetic.VehGeo.*;

import dev.lego.cosmetic.Cos.A;

/** Standing vehicles without handlebar: skateboard, hoverboard, UFO, cloud. */
final class VehBoard {
    private VehBoard() { }

    static final float SKATE_Y = 2.5f, SKATE_TH = 0.55f, SKATE_TOP = SKATE_Y + SKATE_TH;
    static final float HOVER_Y = 3.8f, HOVER_TH = 1.3f, HOVER_TOP = HOVER_Y + HOVER_TH;
    static final float UFO_Y = 3.0f, UFO_DECK = 3.4f, UFO_TOP = UFO_Y + UFO_DECK;
    static final float CLOUD_Y = 3.5f, CLOUD_TOP = 6.0f;

    // ================================================================== skateboard
    private static float skW(float z) {
        float az = Math.abs(z);
        if (az <= 10) return 3.6f;
        float t = (az - 10) / 3.05f;
        return 3.6f * (float) Math.sqrt(Math.max(0.015f, 1 - t * t));
    }

    private static float skKick(float z) {
        float az = Math.abs(z);
        if (az <= 8.4f) return 0;
        float t = (az - 8.4f) / 4.6f;
        return (z > 0 ? 2.3f : 2.0f) * t * t;
    }

    private static float concave(float x) { float t = x / 3.6f; return 0.32f * t * t; }

    static void skateboard(G g, A a) {
        g.push();
        float tilt = -a.lean * 6;
        // trucks (hangers turn against each other when carving)
        for (int s = -1; s <= 1; s += 2) truck(g, a, s * 8.3f, s * a.lean * 9);
        g.translate(0, 1.6f, 0);
        g.rotZ(tilt);
        g.translate(0, -1.6f, 0);
        // deck: concave, kicked nose / tail, rounded outline
        final int m = 6;
        loft(g, range(-12.97f, 12.97f, 0.9f), z -> {
            float w = skW(z), y0 = SKATE_Y + skKick(z);
            float[][] r = new float[2 * m + 2][];
            for (int i = 0; i <= m; i++) { float x = -w + 2 * w * i / m; r[i] = new float[]{x, y0 + concave(x)}; }
            for (int i = 0; i <= m; i++) { float x = w - 2 * w * i / m; r[m + 1 + i] = new float[]{x, y0 + SKATE_TH + concave(x)}; }
            return r;
        }, (k, j, z, pa, pb) -> j < m ? "veh_deckart" : j == m || j == 2 * m + 1 ? "veh_c:D9A86A" : "veh_griptape", true, true, -3.6f, 3.6f, true);
        // bolts
        for (int s = -1; s <= 1; s += 2)
            for (int bx = -1; bx <= 1; bx += 2)
                for (int bz = -1; bz <= 1; bz += 2) {
                    float x = bx * 0.75f, z = s * 8.3f + bz * 0.8f, y = SKATE_TOP + concave(x);
                    rbox(g, "veh_chrome", x - 0.2f, y - 0.12f, z - 0.2f, x + 0.2f, y + 0.12f, z + 0.2f, 0.07f);
                }
        // base plates + bushings (follow the deck tilt)
        for (int s = -1; s <= 1; s += 2) {
            float z = s * 8.3f;
            rbox(g, "veh_alu", -1.25f, 2.05f, z - 1.3f, 1.25f, SKATE_Y + 0.08f, z + 1.3f, 0.2f);
            g.push();
            g.translate(0, 1.55f, z - s * 0.25f);
            lathe(g, "veh_c:FFCD03", new float[][]{{0.01f, -0.3f}, {0.5f, -0.3f}, {0.55f, 0}, {0.5f, 0.55f}, {0.01f, 0.55f}}, 10);
            g.pop();
        }
        g.pop();
    }

    static void truck(G g, A a, float z, float yaw) {
        g.push();
        g.translate(0, 1.2f, z);
        g.rotY(yaw);
        beam(g, "veh_alu", p(-2.7f, 0, 0), p(2.7f, 0, 0), 0.62f, 0.7f);
        rbox(g, "veh_alu", -1.05f, -0.45f, -0.5f, 1.05f, 0.62f, 0.5f, 0.22f);
        tube(g, "veh_chrome", p(-4.0f, 0, 0), p(4.0f, 0, 0), 0.16f, 0.16f, 6, true);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 3.35f, 0, 0);
            if (s < 0) g.rotY(180);
            g.rotX(s < 0 ? -a.wheel * 2.3f : a.wheel * 2.3f);
            g.rotZ(-90);
            lathe(g, new String[]{"veh_gun", "veh_gun", "veh_urethane", "veh_urethane", "veh_urethane", "veh_urethane", "veh_urethane", "veh_urethane", "veh_gun", "veh_gun"},
                    new float[][]{{0.01f, -0.5f}, {0.45f, -0.5f}, {0.5f, -0.56f}, {1.0f, -0.56f}, {1.17f, -0.38f}, {1.2f, 0}, {1.17f, 0.38f}, {1.0f, 0.56f},
                            {0.5f, 0.56f}, {0.45f, 0.5f}, {0.01f, 0.5f}}, 14);
            g.pop();
        }
        g.pop();
    }

    // ================================================================== hoverboard
    private static float hvW(float z) {
        float az = Math.abs(z);
        if (az <= 8.5f) return 4.0f;
        float t = (az - 8.5f) / 4.55f;
        return 4.0f * (float) Math.sqrt(Math.max(0.02f, 1 - t * t));
    }

    private static float hvKick(float z) {
        if (z > 7.5f) { float t = (z - 7.5f) / 5.5f; return 1.5f * t * t; }
        if (z < -8.5f) { float t = (-8.5f - z) / 4.5f; return 0.7f * t * t; }
        return 0;
    }

    static void hoverboard(G g, A a) {
        float bob = Vehicles.hoverBob(a.time);
        g.push();
        g.translate(0, HOVER_Y + bob, 0);
        g.rotZ(-a.lean * 10);
        g.rotX(-a.move * 4);
        loft(g, range(-13.0f, 13.0f, 0.8f), z -> {
            float w = hvW(z), y = hvKick(z), e = Math.min(0.6f, w * 0.4f);
            return new float[][]{{-w + e, y}, {0, y}, {w - e, y}, {w, y + 0.45f}, {w, y + 0.85f}, {w - e, y + HOVER_TH}, {0, y + HOVER_TH}, {-w + e, y + HOVER_TH}, {-w, y + 0.85f}, {-w, y + 0.45f}};
        }, (k, j, z, pa, pb) -> j < 2 ? "veh_c:2A2D3A" : j >= 5 && j <= 6 ? "veh_paint:FF3EA5" : "veh_c:1B1D26", true, true, HOVER_TH + 0.5f, -0.2f);
        // glowing edge band
        float pulse = 0.75f + 0.25f * sin(a.time * 5);
        g.glow(true).color(argb((int) (255 * pulse), 0xFFFFFF));
        loft(g, range(-13.2f, 13.2f, 0.8f), z -> {
            float w = hvW(z * 13.0f / 13.25f) + 0.22f, y = hvKick(z * 13.0f / 13.25f);
            return new float[][]{{-w, y + 0.5f}, {w, y + 0.5f}, {w, y + 0.78f}, {-w, y + 0.78f}};
        }, (k, j, z, pa, pb) -> "veh_glow:24E0FF", true, true, 1, 0);
        g.glow(false).color(0xFFFFFFFF);
        // foot pads
        for (int s = -1; s <= 1; s += 2) rbox(g, "veh_rubber", -3.0f, HOVER_TH - 0.1f, s < 0 ? -7.6f : 2.0f, 3.0f, HOVER_TH + 0.28f, s < 0 ? -2.0f : 7.6f, 0.12f);
        // thruster pods
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(0, 0, s * 7);
            lathe(g, new String[]{"veh_gun", "veh_c:1B1D26", "veh_chrome", "veh_c:1B1D26"},
                    new float[][]{{0.01f, -1.4f}, {1.8f, -1.25f}, {2.5f, -0.75f}, {2.7f, -0.2f}, {2.6f, 0.3f}}, 16);
            float fl = 0.7f + 0.3f * sin(a.time * 11 + s);
            g.glow(true).color(argb((int) (255 * fl), 0xFFFFFF));
            lathe(g, "veh_glow:7CF7FF", new float[][]{{0.01f, -1.52f}, {1.5f, -1.52f}}, 16);
            g.color(argb((int) (150 * fl), 0xFFFFFF));
            g.translate(0, -1.2f, 0);
            torus(g, "veh_glow:24E0FF", 2.15f, 0.22f, 20, 6);
            g.glow(false).color(0xFFFFFFFF);
            g.pop();
        }
        g.pop();
    }

    // ================================================================== UFO
    static void ufo(G g, A a) {
        float bob = Vehicles.hoverBob(a.time) * 1.4f;
        g.push();
        g.translate(0, UFO_Y + bob, 0);
        g.rotZ(-a.lean * 8);
        String[] hullTex = {"veh_gun", "veh_gun", "veh_ufo", "veh_ufo", "veh_chrome", "veh_chrome", "veh_ufo", "veh_ufo", "veh_ufo", "veh_flat:2A2D36"};
        lathe(g, hullTex, new float[][]{{0.01f, -3.0f}, {4, -2.9f}, {10, -2.2f}, {15, -1.1f}, {19.3f, -0.3f}, {20, 0.25f}, {19.4f, 0.9f}, {15, 2.2f},
                {11.6f, 3.1f}, {10.9f, UFO_DECK}, {0.01f, UFO_DECK}}, 30);
        // rotating rim lights
        g.glow(true);
        int n = 14;
        for (int i = 0; i < n; i++) {
            double an = Math.PI * 2 * i / n + a.time * 1.5;
            float on = 0.5f + 0.5f * sin(a.time * 6 + i);
            g.color(argb((int) (150 + 105 * on), 0xFFFFFF));
            ellipsoid(g, i % 2 == 0 ? "veh_glow:7CFF6B" : "veh_glow:FFE14D", (float) Math.cos(an) * 19.1f, 0.4f, (float) Math.sin(an) * 19.1f, 0.75f, 0.55f, 0.75f, 8, 5);
        }
        g.color(0xFFFFFFFF);
        g.push();
        g.translate(0, -2.0f, 0);
        torus(g, "veh_glow:7CFF6B", 10.3f, 0.32f, 28, 5);
        g.pop();
        g.color(0x99FFFFFF);
        lathe(g, "veh_glow:7CFF6B", new float[][]{{0.01f, -3.1f}, {3.6f, -3.1f}}, 20);           // tractor beam emitter
        g.glow(false).color(0xFFFFFFFF);
        for (int i = 0; i < 3; i++) {
            double an = Math.PI * 2 * i / 3 + 0.5;
            ellipsoid(g, "veh_chrome", (float) Math.cos(an) * 13.5f, -1.5f, (float) Math.sin(an) * 13.5f, 1.3f, 0.7f, 1.3f, 10, 5);
        }
        // console in front of the pilot
        g.push();
        g.translate(0, UFO_DECK, 5.6f);
        rbox(g, "veh_gun", -2.6f, -0.1f, -0.8f, 2.6f, 9.0f, 0.8f, 0.4f);
        g.translate(0, 9.0f, 0);
        g.rotX(-30);
        rbox(g, "veh_gun", -3.2f, -0.4f, -1.4f, 3.2f, 0.3f, 1.4f, 0.25f);
        g.glow(true);
        for (int i = 0; i < 5; i++) {
            float on = (Math.floorMod((int) (a.time * 4) + i * 3, 5) < 3) ? 1 : 0.35f;
            g.color(argb((int) (255 * on), 0xFFFFFF));
            box(g, i % 2 == 0 ? "veh_glow:FF4B6E" : "veh_glow:7CF7FF", -2.4f + i * 1.0f, 0.25f, -0.4f, -1.8f + i * 1.0f, 0.45f, 0.3f);
        }
        g.glow(false).color(0xFFFFFFFF);
        g.pop();
        // dome ring, glass dome, antenna
        g.push();
        g.translate(0, UFO_DECK + 0.1f, 0);
        torus(g, "veh_chrome", 10.6f, 0.45f, 28, 6);
        g.pop();
        // glass cockpit collar (a closed shell: outside up, over the lip, inside down)
        float d = UFO_DECK;
        lathe(g, "veh_glass", new float[][]{{10.4f, d - 0.2f}, {11.0f, d + 2}, {10.7f, d + 4.5f}, {9.9f, d + 6.8f}, {9.6f, d + 7.0f},
                {9.4f, d + 6.6f}, {10.1f, d + 4.4f}, {10.3f, d + 2}, {9.8f, d - 0.15f}}, 30);
        g.push();
        g.translate(0, d + 6.95f, 0);
        torus(g, "veh_chrome", 9.75f, 0.28f, 30, 5);
        g.pop();
        // antenna on the back of the rim
        tube(g, "veh_chrome", p(0, d + 6.9f, -9.75f), p(0, d + 13.5f, -11.0f), 0.16f, 0.1f, 6, false);
        g.glow(true).color(argb((int) (160 + 95 * sin(a.time * 8)), 0xFFFFFF));
        ball(g, "veh_glow:FF4B6E", 0, d + 13.7f, -11.05f, 0.5f, 8, 5);
        g.glow(false).color(0xFFFFFFFF);
        g.pop();
    }

    // ================================================================== cloud
    static void cloud(G g, A a) {
        g.push();
        g.translate(0, CLOUD_Y + Vehicles.cloudBob(a.time), 0);
        // big flat bottomed puffs: centre, ring and a few on top; the top surface is ~2.5 px above the base
        PetGeo.blob2(g, "veh_cloud", 0, 0, 0, 8.5f, 2.6f, 1.4f, 10.5f, 16, 8);
        java.util.Random r = new java.util.Random(7);
        int n = 9;
        for (int i = 0; i < n; i++) {
            double an = Math.PI * 2 * i / n + r.nextFloat() * 0.3;
            float d = 9 + r.nextFloat() * 2.5f, s = 3.8f + r.nextFloat() * 1.8f;
            float wob = sin(a.time * 1.2 + i) * 0.25f;
            PetGeo.blob2(g, "veh_cloud", (float) Math.cos(an) * d * 0.8f, 0.3f + wob, (float) Math.sin(an) * d, s, s * 0.8f, s * 0.35f, s, 12, 7);
        }
        for (int i = 0; i < 4; i++) {
            double an = Math.PI * 2 * i / 4 + 0.7;
            float wob = sin(a.time * 1.5 + i * 2) * 0.2f;
            PetGeo.blob2(g, "veh_cloud", (float) Math.cos(an) * 8.5f, 2.2f + wob, (float) Math.sin(an) * 9.5f, 3.2f, 2.6f, 0.8f, 3.2f, 10, 6);
        }
        // little trailing puffs behind
        for (int i = 0; i < 3; i++) {
            float z = -15 - i * 3.2f - (a.time * 2 % 3.2f) * a.move, s = 2.2f - i * 0.55f;
            PetGeo.blob2(g, "veh_cloud", sin(a.time + i) * 1.5f, 0.6f, z, s, s * 0.8f, s * 0.5f, s, 10, 6);
        }
        g.pop();
    }
}
