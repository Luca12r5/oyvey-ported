package dev.lego.cosmetic;

import static dev.lego.cosmetic.Cos3Geo.*;

import dev.lego.cosmetic.Cos.A;

/**
 * HD remaster of the classic back items (body space: neck at the origin, body x -4..4, y -12..0, back surface at z = -2).
 * Straps run over the shoulders and are offset from the body surface so nothing z-fights.
 */
final class HdBack {
    private HdBack() { }

    private static float[] p(float x, float y, float z) { return new float[]{x, y, z}; }

    /** Pair of shoulder straps from the item (at z = zBack, from y = yTop to yBottom on the back) over the shoulders to the chest. */
    static void straps(G g, int col, float x, float yBot, float zBack) {
        int oc = g.color;
        g.color(col);
        for (int s = -1; s <= 1; s += 2) {
            float[][] sp = {{s * x, yBot, zBack}, {s * x, -1.2f, -2.35f}, {s * x, 0.25f, -1.7f}, {s * x, 0.4f, 0}, {s * x, 0.25f, 1.7f}, {s * x, -1.2f, 2.3f}, {s * (x + 0.2f), -6.2f, 2.25f}};
            float[] rx = new float[sp.length], rz = new float[sp.length];
            java.util.Arrays.fill(rx, 0.12f);
            java.util.Arrays.fill(rz, 0.55f);
            XmasGeo.sweep(g, "hd_leather", sp, rx, rz, null, 6, 3);
            // buckle on the chest
            g.color(0xFFD8DEE8);
            g.push();
            g.translate(s * (x + 0.1f), -3.2f, 2.42f);
            HdGeo.loop(g, "hd_chrome", HdGeo.rrect(0.62f, 0.5f, 0.15f, 2), 0.08f, 0.08f, 2, 4);
            g.pop();
            g.color(col);
        }
        g.color(oc);
    }

    /** Diagonal strap across the chest and back (sash) from the right shoulder to the left hip. */
    static void sash(G g, int col, float angle) {
        int oc = g.color;
        g.color(col);
        g.push();
        g.rotZ(angle);
        float[][] sp = {{0, -12.5f, 2.32f}, {0, -1.5f, 2.32f}, {0, 0.9f, 1.4f}, {0, 1.3f, 0}, {0, 0.9f, -1.4f}, {0, -1.5f, -2.32f}, {0, -12.5f, -2.32f}};
        float[] rx = new float[sp.length], rz = new float[sp.length];
        java.util.Arrays.fill(rx, 0.12f);
        java.util.Arrays.fill(rz, 0.62f);
        XmasGeo.sweep(g, "hd_leather", sp, rz, rx, null, 6, 4);
        g.pop();
        g.color(oc);
    }

    // ================================================================== Lego-Rucksack
    static void legoBackpack(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(0, -1.3f + (a.preview ? 0 : a.move * 0.15f * Math.abs(sin(t * 8))), -2.15f);
        g.color(0xFFE3000B);
        bbox(g, "hd_brick", -3.3f, -8.4f, -3.6f, 3.3f, -0.2f, 0, 0.3f);
        // studs with the logo on top
        for (int ix = 0; ix < 2; ix++)
            for (int iz = 0; iz < 2; iz++) {
                g.push();
                g.translate(-1.6f + ix * 3.2f, -0.2f, -0.9f - iz * 1.8f);
                lathe(g, "hd_brick", new float[]{1.0f, 1.0f, 0.92f, 0}, new float[]{-0.02f, 0.72f, 0.84f, 0.86f}, 16);
                g.pop();
            }
        // front pocket brick (yellow 2x1) with a clasp
        g.color(0xFFFFCD03);
        bbox(g, "hd_brick", -2.6f, -7.6f, -4.3f, 2.6f, -3.9f, -3.55f, 0.22f);
        for (int ix = 0; ix < 2; ix++) {
            g.push();
            g.translate(-1.25f + ix * 2.5f, -3.9f, -3.95f);
            lathe(g, "hd_brick", new float[]{0.55f, 0.55f, 0.5f, 0}, new float[]{-0.02f, 0.4f, 0.48f, 0.5f}, 12);
            g.pop();
        }
        g.color(0xFF1A1A1E);
        cbox(g, "hd_plastic", 0, -4.3f, -4.38f, 1.2f, 0.5f, 0.2f, 0.06f);
        // side bottle pocket (blue)
        g.color(0xFF006CB7);
        bbox(g, "hd_brick", 3.25f, -8.0f, -3.0f, 4.05f, -4.6f, -0.7f, 0.18f);
        // zipper piping
        g.color(0xFF9A0008);
        g.push();
        g.translate(0, -4.3f, -3.62f);
        HdGeo.loop(g, "hd_rubber", HdGeo.rrect(3.1f, 3.9f, 0.4f, 3), 0.07f, 0.07f, 2, 4);
        g.pop();
        g.pop();
        // back plate so nothing floats, then straps
        straps(g, 0xFF2A2A30, 2.2f, -7.6f, -2.2f);
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Kristallschwert
    static void backSword(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(0.4f, -5.5f, -2.85f);
        g.rotZ(-40);
        // blade: diamond cross section tapering to a sharp tip
        int n = 10;
        float[][] sp = new float[n + 2][];
        float[] rx = new float[n + 2], rz = new float[n + 2], ex = new float[n + 2];
        for (int i = 0; i <= n; i++) {
            float f = i / (float) n;
            sp[i] = p(0, 2.2f + f * 13.5f, 0);
            rx[i] = 1.25f - f * 0.25f;
            rz[i] = 0.34f - f * 0.08f;
            ex[i] = 1.05f;
        }
        sp[n + 1] = p(0, 18.6f, 0);
        rx[n + 1] = 0.02f;
        rz[n + 1] = 0.02f;
        ex[n + 1] = 1.05f;
        g.glow(false).color(0xFFBDEBFF);
        XmasGeo.sweep(g, "c3_crystal", sp, rx, rz, ex, 4, 3);
        // glowing core + running rune shimmer
        float sh = fract(t * 0.5f);
        g.glow(true).color(0xFF7ADCFF);
        for (int i = 0; i <= n; i++) { rx[i] *= 0.28f; rz[i] *= 1.08f; }
        XmasGeo.sweep(g, "white", sp, rx, rz, ex, 4, 1);
        halo(g, 0, 2.2f + sh * 14, 0, 3.2f, alpha(0.55 * Math.sin(sh * Math.PI), 0x9AF6FF));
        g.glow(false);
        // guard: curved puffy cross guard with gems
        g.color(0xFF5A3AA0);
        float[][] guard = {{-3.4f, 0.1f}, {-2.2f, -0.35f}, {0, -0.55f}, {2.2f, -0.35f}, {3.4f, 0.1f}, {3.5f, 0.75f}, {2.2f, 0.5f}, {0, 0.7f}, {-2.2f, 0.5f}, {-3.5f, 0.75f}};
        g.push();
        g.translate(0, 1.7f, 0);
        HdGeo.puff(g, "hd_silver", guard, 0.9f, 0.3f, 2);
        g.glow(true).color(0xFF9AF6FF);
        g.push();
        g.translate(0, 0.1f, 0.4f);
        g.rotX(90);
        gem(g, "hd_gem", 0.45f, 0.35f, 0.1f, 6);
        g.pop();
        for (int s = -1; s <= 1; s += 2) ball(g, "hd_gem", s * 3.5f, 0.45f, 0, 0.35f, 6);
        g.glow(false);
        g.pop();
        // grip wrapped in leather (ridges)
        g.color(0xFF3A2418);
        lathe(g, "hd_leather", new float[]{0.42f, 0.5f, 0.44f, 0.52f, 0.44f, 0.52f, 0.44f, 0.52f, 0.44f, 0.42f}, new float[]{-2.9f, -2.5f, -2.1f, -1.7f, -1.3f, -0.9f, -0.5f, -0.1f, 0.3f, 1.2f}, 10);
        // pommel
        g.color(0xFFFFFFFF);
        ball(g, "hd_gold", 0, -3.45f, 0, 0.75f, 10);
        g.glow(true).color(0xFF7ADCFF);
        ball(g, "hd_gem", 0, -3.45f, 0.62f, 0.32f, 6);
        g.glow(false);
        g.pop();
        sash(g, 0xFF3A2418, -40);
        g.color(0xFFFFFFFF);
    }

    // ================================================================== E-Gitarre
    static float[][] guitarOutline() {
        // offset double cutaway body (y up = towards the neck), ~7 wide, ~9 tall
        float[][] half = {{0, -4.6f}, {1.6f, -4.45f}, {2.9f, -3.8f}, {3.55f, -2.6f}, {3.5f, -1.2f}, {2.9f, 0.1f}, {2.7f, 1.0f}, {3.1f, 2.1f}, {3.3f, 3.4f}, {2.6f, 4.2f}, {1.9f, 3.1f}, {1.1f, 2.4f}, {0, 2.3f}};
        float[][] o = new float[half.length * 2 - 2][];
        int k = 0;
        for (float[] q : half) o[k++] = new float[]{q[0], q[1]};
        for (int i = half.length - 2; i >= 1; i--) {
            float[] q = half[i];
            // left horn shorter (offset body)
            float y = q[1] > 1.5f ? q[1] - 0.8f * (q[1] - 1.5f) / 2.7f : q[1];
            o[k++] = new float[]{-q[0], y};
        }
        return o;
    }

    static void guitar(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(0, -7.2f, -3.15f);
        g.rotZ(-32 + sin(t * 1.5f) * 1.2f);
        g.rotY(180);
        g.color(0xFFFFFFFF);
        HdGeo.puff(g, "hd_maple", guitarOutline(), 1.25f, 0.45f, 2, new float[]{0, 0, 1, 1}, new float[]{0.05f, 0.9f, 0.95f, 0.95f});
        // pickguard
        g.color(0xFFF4F0E8);
        g.push();
        g.translate(0, 0, 0.66f);
        float[][] pg = {{-2.2f, -3.2f}, {-0.6f, -3.6f}, {1.6f, 2.0f}, {0.6f, 2.35f}, {-1.2f, 2.3f}, {-2.6f, 0.4f}};
        HdGeo.puff(g, "hd_plastic", pg, 0.1f, 0.04f, 1);
        g.pop();
        // pickups + bridge + knobs
        for (int i = 0; i < 3; i++) {
            g.color(0xFF141418);
            bbox(g, "hd_plastic", -0.95f, -1.9f + i * 1.45f, 0.7f, 0.95f, -1.35f + i * 1.45f, 0.95f, 0.1f);
        }
        g.color(0xFFD8DEE8);
        bbox(g, "hd_chrome", -1.05f, -3.35f, 0.62f, 1.05f, -2.75f, 0.95f, 0.1f);
        for (int i = 0; i < 3; i++) {
            g.push();
            g.translate(1.9f + i * 0.2f, -2.9f + i * 0.9f, 0.62f);
            g.rotX(90);
            g.color(0xFFE8E4DA);
            lathe(g, "hd_plastic", new float[]{0.42f, 0.42f, 0.34f, 0}, new float[]{0, -0.35f, -0.45f, -0.46f}, 10);
            g.pop();
        }
        // neck + fretboard + frets
        g.color(0xFFE0B070);
        bbox(g, "hd_wood", -0.55f, 2.2f, -0.35f, 0.55f, 12.6f, 0.3f, 0.15f);
        g.color(0xFF3A2010);
        g.box("hd_wood", -0.6f, 2.8f, 0.3f, 0.6f, 12.4f, 0.5f, 0, 0, 1, 1);
        g.color(0xFFD8DEE8);
        for (int i = 0; i < 12; i++) {
            float y = 12.2f - 9.2f * (1 - (float) Math.pow(0.94, i * 1.6)) / (1 - (float) Math.pow(0.94, 19.2));
            g.box("hd_chrome", -0.6f, y, 0.5f, 0.6f, y + 0.08f, 0.56f, 0, 0, 1, 1);
        }
        // headstock with tuners
        g.color(0xFF141418);
        float[][] hs = {{-0.6f, 12.4f}, {0.6f, 12.4f}, {1.2f, 13.6f}, {1.35f, 15.6f}, {0.2f, 16.1f}, {-0.9f, 15.3f}, {-1.0f, 13.4f}};
        HdGeo.puff(g, "hd_plastic", hs, 0.5f, 0.18f, 1);
        g.color(0xFFD8DEE8);
        for (int i = 0; i < 3; i++)
            for (int s = -1; s <= 1; s += 2) {
                g.push();
                g.translate(s * 1.2f, 13.5f + i * 0.8f, 0);
                g.rotZ(s * -90);
                lathe(g, "hd_chrome", new float[]{0.22f, 0.22f, 0.4f, 0.4f, 0}, new float[]{0, 0.5f, 0.55f, 0.85f, 0.9f}, 6);
                g.pop();
            }
        // strings
        g.color(0xFFF0F0F4);
        for (int i = 0; i < 6; i++) {
            float x = -0.4f + i * 0.16f;
            Geo.tube(g, "hd_chrome", p(x * 1.5f, -3.05f, 1.0f), p(x, 12.4f, 0.62f), 0.03f, 0.03f, 3);
        }
        g.pop();
        sash(g, 0xFF2A1A12, -40);
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Jetpack
    static void jetpack(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(0, -1.5f, -2.15f);
        // harness plate
        g.color(0xFF3A3E48);
        bbox(g, "hd_steel", -3.4f, -8.4f, -1.4f, 3.4f, -0.8f, 0, 0.3f);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 2.15f, -1.4f, -2.95f);
            // tank
            g.color(0xFFE8ECF2);
            lathe(g, "hd_chrome", new float[]{0, 1.1f, 1.62f, 1.85f, 1.85f, 1.7f, 1.3f}, new float[]{1.35f, 1.15f, 0.7f, 0.1f, -6.6f, -7.2f, -7.5f}, 18);
            // red bands
            g.color(0xFFE3000B);
            lathe(g, "hd_plastic", new float[]{1.88f, 1.95f, 1.95f, 1.88f}, new float[]{-1.4f, -1.5f, -2.2f, -2.3f}, 18);
            lathe(g, "hd_plastic", new float[]{1.88f, 1.95f, 1.95f, 1.88f}, new float[]{-5.3f, -5.4f, -5.9f, -6.0f}, 18);
            // gauge
            g.push();
            g.translate(s * 1.3f, -3.6f, -1.25f);
            g.rotY(s * 45 + 180);
            g.rotX(90);
            g.color(0xFF2A2E36);
            lathe(g, "hd_steel", new float[]{0.55f, 0.55f, 0.45f, 0}, new float[]{0, 0.3f, 0.34f, 0.34f}, 12);
            g.pop();
            // nozzle
            g.color(0xFF3A3E48);
            lathe(g, "hd_steel", new float[]{1.3f, 1.0f, 0.95f, 1.35f, 1.1f}, new float[]{-7.4f, -7.9f, -8.4f, -9.6f, -9.7f}, 14);
            // flame (layered glow cones), longer when moving
            float fl = 1 + 0.18f * sin(t * 22 + s) + (a.preview ? 0 : a.move * 0.6f);
            g.push();
            g.translate(0, -9.6f, 0);
            g.rotZ(180);
            Cos3Models.flame(g, 4.8f * fl, 1.0f, 0xFFFF8A2A, 0xFFFFF4C0);
            g.pop();
            halo(g, 0, -10.4f, 0, 4.2f, alpha(0.5, 0xFF9A3A));
            // smoke puffs
            for (int i = 0; i < 3; i++) {
                float ph = fract(t * 1.6f + i / 3f + (s > 0 ? 0.5f : 0));
                halo(g, sin(i * 2.1 + t) * ph * 1.4f, -12 - ph * 7 * fl, cos(i * 2.1) * ph, 1.5f + ph * 3.5f, alpha(0.3 * (1 - ph), 0xC8B8FF));
            }
            g.pop();
        }
        g.pop();
        straps(g, 0xFF2A2E36, 2.2f, -7.6f, -2.2f);
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Ritterschild (heater shield)
    static float[][] heater(float w, float h, int n) {
        float top = h * 0.42f, H = h;
        float[][] o = new float[n * 2 + 1][];
        int k = 0;
        for (int i = 0; i <= n; i++) {
            float f = i / (float) n;
            o[k++] = new float[]{w * (float) Math.pow(Math.cos(f * Math.PI / 2), 0.55), top - H * f};
        }
        for (int i = n - 1; i >= 0; i--) {
            float f = i / (float) n;
            o[k++] = new float[]{-w * (float) Math.pow(Math.cos(f * Math.PI / 2), 0.55), top - H * f};
        }
        return HdGeo.ccw(java.util.Arrays.copyOf(o, k));
    }

    static void shield(G g, A a) {
        g.push();
        g.translate(0, -5.4f, -3.0f);
        g.rotZ(8);
        g.rotY(180);
        float[][] o = heater(5.2f, 6.2f, 10);
        g.color(0xFFFFFFFF);
        HdGeo.puff(g, "hd_target", o, 0.9f, 0.3f, 2, new float[]{0, 0, 1, 1}, new float[]{0.3f, 0.3f, 0.32f, 0.32f});
        // steel rim
        g.color(0xFFD8DEE8);
        HdGeo.loop(g, "hd_steel", o, 0.42f, 0.62f, 2.6f, 6);
        // golden cross
        g.color(0xFFFFD86A);
        g.push();
        g.translate(0, 0, 0.5f);
        bbox(g, "hd_gold", -0.55f, -5.6f, -0.1f, 0.55f, 2.55f, 0.22f, 0.12f);
        bbox(g, "hd_gold", -4.9f, -0.2f, -0.1f, 4.9f, 0.9f, 0.22f, 0.12f);
        // boss
        g.translate(0, 0.35f, 0.15f);
        g.rotX(90);
        lathe(g, "hd_gold", new float[]{1.35f, 1.3f, 1.05f, 0.5f, 0}, new float[]{0, -0.3f, -0.7f, -0.95f, -1.0f}, 16);
        g.pop();
        // rivets
        g.color(0xFFE8ECF2);
        for (int i = 0; i < o.length; i += 2) ball(g, "hd_chrome", o[i][0] * 0.86f, o[i][1] * 0.86f + 0.1f, 0.5f, 0.18f, 5);
        g.pop();
        sash(g, 0xFF3A2418, 30);
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Köcher
    static void quiver(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(1.6f, -5.4f, -3.9f);
        g.rotZ(-24);
        g.color(0xFFFFFFFF);
        lathe(g, "hd_quiver", new float[]{0, 1.3f, 1.62f, 1.75f, 1.9f, 1.95f, 1.8f}, new float[]{-6.3f, -6.25f, -5.9f, -3, 2, 4.7f, 5.0f}, 18);
        g.color(0xFFE8B94A);
        g.push();
        g.translate(0, 4.75f, 0);
        g.torus("hd_gold", 1.9f, 0.2f, 18, 5, 0, 0, 1, 1);
        g.pop();
        // arrows: shafts, feathers (3 vanes), nocks
        int[] fc = {0xFFE8303A, 0xFFF4F4F4, 0xFF2A7AE8, 0xFFE8303A, 0xFFF4F4F4};
        for (int i = 0; i < 5; i++) {
            float ox = -0.85f + (i % 3) * 0.85f, oz = -0.55f + (i / 3) * 1.05f;
            float h = 7.8f + (i % 2) * 0.9f;
            g.push();
            g.translate(ox, 0, oz);
            g.rotZ((i - 2) * 3 + sin(t * 2 + i) * 0.8f);
            g.color(0xFFC89A5A);
            Geo.tube(g, "hd_wood", p(0, 1.5f, 0), p(0, h, 0), 0.12f, 0.12f, 5);
            g.color(fc[i]);
            for (int v = 0; v < 3; v++) {
                g.push();
                g.rotY(v * 120 + i * 17);
                float[][] vane = {{0.1f, h - 2.4f}, {0.75f, h - 2.1f}, {0.8f, h - 0.4f}, {0.1f, h - 0.2f}};
                g.rotY(90);
                HdGeo.puff(g, "hd_satin", vane, 0.05f, 0.02f, 1);
                g.pop();
            }
            g.color(0xFF2A2A30);
            ball(g, "hd_plastic", 0, h + 0.1f, 0, 0.17f, 5);
            g.pop();
        }
        g.pop();
        sash(g, 0xFF3A2618, 38);
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Rakete
    static void rocket(G g, A a) {
        float t = a.time;
        g.push();
        float shake = (a.preview ? 0 : a.move) * 0.15f * sin(t * 40);
        g.translate(shake, -4 + 0.2f * sin(t * 2), -4.9f);
        g.color(0xFFFFFFFF);
        lathe(g, "hd_rocket", new float[]{1.5f, 1.95f, 2.2f, 2.25f, 2.2f}, new float[]{-6.2f, -5.6f, -4.2f, 0, 3}, 20, 0, 1, 1, 0.1f);
        g.color(0xFFE3000B);
        lathe(g, "hd_gloss", new float[]{2.2f, 2.05f, 1.6f, 0.9f, 0.3f, 0}, new float[]{3, 4.5f, 5.9f, 7.0f, 7.6f, 7.7f}, 20);
        // porthole facing outwards (back)
        g.push();
        g.translate(0, 0.9f, -2.25f);
        g.rotY(180);
        g.color(0xFFD8DEE8);
        HdGeo.loop(g, "hd_chrome", HdGeo.circle(0.95f, 0.95f, 18), 0.22f, 0.2f, 2, 6);
        g.color(0xFF6ACBFF);
        HdGeo.puff(g, "hd_glass", HdGeo.circle(0.9f, 0.9f, 16), 0.2f, 0.08f, 1);
        g.pop();
        // fins
        g.color(0xFFE3000B);
        float[][] fin = {{2.0f, -1.8f}, {2.0f, -6.0f}, {4.4f, -7.4f}, {4.3f, -4.8f}};
        for (int i = 0; i < 4; i++) {
            g.push();
            g.rotY(45 + i * 90);
            g.rotY(-90);
            g.push();
            g.rotY(90);
            HdGeo.puff(g, "hd_gloss", fin, 0.35f, 0.12f, 1);
            g.pop();
            g.pop();
        }
        // nozzle + flame
        g.color(0xFF3A3E48);
        lathe(g, "hd_steel", new float[]{1.2f, 1.1f, 1.55f, 1.4f}, new float[]{-6.1f, -6.5f, -7.5f, -7.6f}, 16);
        float fl = 0.85f + 0.18f * sin(t * 25) + (a.preview ? 0 : a.move * 0.8f);
        g.push();
        g.translate(0, -7.5f, 0);
        g.rotZ(180);
        Cos3Models.flame(g, 5.2f * fl, 1.2f, 0xFFFF7A1A, 0xFFFFF4C0);
        g.pop();
        halo(g, 0, -8.4f, 0, 4.4f, alpha(0.55, 0xFF9A3A));
        g.pop();
        // mounting clamp to the back
        g.color(0xFF3A3E48);
        bbox(g, "hd_steel", -1.2f, -5.2f, -2.9f, 1.2f, -3.6f, -2.05f, 0.15f);
        straps(g, 0xFF2A2E36, 1.6f, -5.0f, -2.3f);
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Teddy
    static void teddy(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(0.8f, -4.2f, -4.5f);
        g.rotZ(sin(t * 1.6f) * 4 + (a.preview ? 0 : a.move * sin(t * 8) * 6));
        g.rotY(180);
        int fur = 0xFFB07A48, light = 0xFFE8C898;
        // body + belly
        g.color(fur);
        Geo.ellipsoid(g, "hd_fur", 0, -2.2f, 0, 2.4f, 2.8f, 1.9f, 16, 10);
        g.color(light);
        Geo.ellipsoid(g, "hd_fur", 0, -2.4f, 1.0f, 1.55f, 1.85f, 1.0f, 12, 8);
        // head
        g.color(fur);
        Geo.ellipsoid(g, "hd_fur", 0, 1.8f, 0, 2.35f, 2.15f, 2.05f, 18, 12);
        // muzzle + nose + eyes
        g.color(light);
        Geo.ellipsoid(g, "hd_fur", 0, 1.2f, 1.75f, 1.1f, 0.8f, 0.7f, 12, 8);
        g.color(0xFF1A0E08);
        Geo.ellipsoid(g, "hd_gloss", 0, 1.55f, 2.4f, 0.4f, 0.28f, 0.22f, 8, 5);
        for (int s = -1; s <= 1; s += 2) {
            ball(g, "hd_gloss", s * 0.85f, 2.4f, 1.8f, 0.3f, 8);
            // ears with inner
            g.color(fur);
            Geo.ellipsoid(g, "hd_fur", s * 1.7f, 3.6f, 0, 0.95f, 0.95f, 0.55f, 10, 6);
            g.color(light);
            Geo.ellipsoid(g, "hd_fur", s * 1.7f, 3.55f, 0.32f, 0.55f, 0.55f, 0.28f, 8, 5);
            // arms + legs with paw pads
            g.color(fur);
            Geo.ellipsoid(g, "hd_fur", s * 2.4f, -1.4f, 0.4f, 0.8f, 1.5f, 0.8f, 10, 6);
            Geo.ellipsoid(g, "hd_fur", s * 1.3f, -4.8f, 0.6f, 0.95f, 0.85f, 1.15f, 10, 6);
            g.color(light);
            Geo.ellipsoid(g, "hd_fur", s * 1.3f, -4.85f, 1.6f, 0.62f, 0.6f, 0.25f, 8, 5);
            g.color(0xFF1A0E08);
        }
        // bow tie
        g.push();
        g.translate(0, -0.25f, 1.7f);
        XmasGeo.bow(g, 0xFFE0405A, 0.7f);
        g.pop();
        // stitched seam on the belly
        g.color(0xFFFFFFFF);
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Herzballon
    static void balloon(G g, A a) {
        float t = a.time;
        float bx = 6 + sin(t * 0.9f) * 1.0f, by = 1.0f + sin(t * 1.3f) * 0.8f, bz = -6 + cos(t * 0.7f) * 1.0f;
        float[] hand = {2.5f, -9f, -2.3f};
        int n = 12;
        float[][] pts = new float[n][];
        float[] rad = new float[n];
        for (int i = 0; i < n; i++) {
            float k = i / (float) (n - 1);
            pts[i] = new float[]{Geo.lerp(hand[0], bx, k) + sin(k * Math.PI) * 0.8f * sin(t * 2), Geo.lerp(hand[1], by - 3.6f, k) - sin(k * Math.PI) * 0.6f, Geo.lerp(hand[2], bz, k)};
            rad[i] = 0.06f;
        }
        g.color(0xFFF4F4F4);
        Geo.chain(g, "hd_plastic", pts, rad, 4);
        g.push();
        g.translate(bx, by, bz);
        g.rotZ(sin(t * 1.1f) * 8);
        g.rotY(t * 25);
        g.color(0xFFFF2A5A);
        HdGeo.puff(g, "hd_gloss", HdGeo.heart(3.1f, 32), 2.6f, 1.25f, 4);
        // knot
        g.color(0xFFE0103A);
        lathe(g, "hd_gloss", new float[]{0.05f, 0.42f, 0.28f, 0.45f, 0}, new float[]{-2.75f, -3.0f, -3.25f, -3.5f, -3.6f}, 8);
        g.pop();
        g.color(0xFFFFFFFF);
    }
}
