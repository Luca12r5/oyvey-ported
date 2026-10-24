package dev.lego.cosmetic;

import static dev.lego.cosmetic.Cos3Geo.*;

import dev.lego.cosmetic.Cos.A;

/**
 * HD remaster of the classic auras (body space, feet at y = -24): real 3D particles (flames, crystals, hearts,
 * notes, bubbles, petals, leaves, bricks) that grow in and out instead of flat sprites, plus soft glows.
 */
final class HdAuras {
    private HdAuras() { }

    private static float[] p(float x, float y, float z) { return new float[]{x, y, z}; }

    /** Soft glowing disc on the ground. */
    static void groundGlow(G g, int argb, float r) {
        boolean gl = g.glow;
        int c = g.color;
        g.glow(true).color(argb);
        g.push();
        g.translate(0, -23.9f, 0);
        g.rotX(-90);
        g.plane("hd_softglow", 0, 0, r * 2, r * 2, 0, 0, 1, 1);
        g.pop();
        g.glow(gl).color(c);
    }

    /** Life curve 0..1..0 for a particle phase (quick grow, slow fade). */
    static float life(float ph) { return smooth(ph * 5) * smooth((1 - ph) * 2.2f); }

    // ================================================================== Feuer-Aura
    static void fire(G g, A a) {
        float t = a.time;
        groundGlow(g, 0xAAFF6A1A, 11);
        // ring of flames licking upwards
        int n = 12;
        for (int i = 0; i < n; i++) {
            float ph = fract(t * 1.1f + i * 0.37f);
            double ang = Math.PI * 2 * i / n + t * 0.4f;
            float r = 7.4f + sin(i * 1.7) * 1.0f - ph * 1.2f;
            float s = life(ph);
            if (s < 0.03f) continue;
            g.push();
            g.translate(cos(ang) * r, -24 + ph * 10, sin(ang) * r);
            g.rotZ(sin(t * 5 + i) * 10);
            g.rotX(cos(t * 4 + i) * 8);
            Cos3Models.flame(g, 5.2f * s, 1.25f * s, 0xFFFF6A1A, 0xFFFFE89A);
            g.pop();
        }
        // embers rising
        for (int i = 0; i < 14; i++) {
            float ph = fract(t * 0.6f + i * 0.29f);
            double ang = i * 2.1 + t * 0.8f;
            float x = cos(ang) * (6 + ph * 3), y = -22 + ph * 26, z = sin(ang) * (6 + ph * 3);
            float s = (1 - ph);
            Cos3Models.sparkle(g, x, y, z, 0.35f * s + 0.1f, alpha(s, 0xFFB04A));
            if (i % 2 == 0) halo(g, x, y, z, 1.8f * s, alpha(0.5 * s, 0xFF7A2A));
        }
    }

    // ================================================================== Frost-Aura
    static void frost(G g, A a) {
        float t = a.time;
        groundGlow(g, 0x887CF7FF, 11);
        // ice crystals growing out of the ground
        for (int i = 0; i < 9; i++) {
            double ang = Math.PI * 2 * i / 9 + 0.3;
            float r = 7.8f + (i % 3) * 0.9f;
            float grow = 0.75f + 0.25f * sin(t * 1.3f + i);
            g.push();
            g.translate(cos(ang) * r, -24.2f, sin(ang) * r);
            g.rotY((float) Math.toDegrees(-ang));
            g.rotZ(-14 + (i % 2) * 28);
            g.glow(true).color(0xDDA8ECFF);
            shard(g, "c3_crystal", 0.6f + (i % 3) * 0.15f, (2.6f + (i % 4) * 0.8f) * grow);
            g.rotZ(30);
            shard(g, "c3_crystal", 0.35f, 1.6f * grow);
            g.glow(false);
            g.pop();
        }
        // snowflakes swirling up (3D bars)
        for (int i = 0; i < 10; i++) {
            double ang = Math.PI * 2 * i / 10 + t * 0.7f;
            float ph = fract(t * 0.18f + i * 0.41f);
            float s = life(ph);
            if (s < 0.03f) continue;
            float r = 8.5f + sin(i * 2.3) * 0.8f;
            g.push();
            g.translate(cos(ang) * r, -23 + ph * 26, sin(ang) * r);
            g.rotY((float) Math.toDegrees(-ang) + 90 + t * 40);
            g.rotZ(t * 60 + i * 30);
            g.glow(true).color(0xFFE8F8FF);
            XmasGeo.flake(g, "white", (1.1f + (i % 3) * 0.35f) * s, 0.08f);
            g.glow(false);
            g.pop();
        }
        for (int i = 0; i < 10; i++) {
            double ang = i * 2.4 - t * 0.4;
            float tw = 0.5f + 0.5f * sin(t * 4 + i * 1.3f);
            Cos3Models.sparkle(g, cos(ang) * (6 + i % 4), -22 + (i * 2.3f) % 24, sin(ang) * (6 + i % 4), 0.3f + 0.3f * tw, alpha(tw, 0xFFFFFF));
        }
    }

    // ================================================================== Herz-Aura
    static void hearts(G g, A a) {
        float t = a.time;
        float[][] heart = HdGeo.heart(1.4f, 20);
        int n = 8;
        int[] cols = {0xFFFF2A6A, 0xFFFF6A9A, 0xFFE8103A};
        for (int i = 0; i < n; i++) {
            float ph = fract(t * 0.3f + i / (float) n);
            float s = life(ph);
            if (s < 0.03f) continue;
            double ang = Math.PI * 2 * i / n + t * 0.6f;
            float r = 8.5f + sin(t * 1.5f + i) * 0.8f;
            float beat = 1 + 0.14f * (float) Math.pow(Math.max(0, Math.sin(t * 7 + i)), 4);
            g.push();
            g.translate(cos(ang) * r, -22 + ph * 26, sin(ang) * r);
            g.rotY((float) Math.toDegrees(-ang) + 90 + sin(t * 2 + i) * 25);
            g.rotZ(sin(t * 3 + i) * 12);
            g.scale(s * beat * (1 + (i % 3) * 0.2f));
            g.color(cols[i % 3]);
            HdGeo.puff(g, "hd_gloss", heart, 1.1f, 0.5f, 2);
            g.pop();
            halo(g, cos(ang) * r, -22 + ph * 26, sin(ang) * r, 4.5f * s, alpha(0.3 * s, 0xFF4A8A));
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Sternen-Orbit
    static void orbit(G g, A a) {
        float t = a.time;
        int[] cols = {0xFFC77DFF, 0xFF7CF7FF, 0xFFFFF0B0};
        for (int i = 0; i < 3; i++) {
            g.push();
            g.translate(0, -9, 0);
            g.rotX(25 + i * 55);
            g.rotY(t * (70 + i * 25) + i * 120);
            // comet: glowing 3D star + fading trail of shrinking sparkles
            g.push();
            g.translate(11, 0, 0);
            g.rotY(t * 120);
            g.rotX(90);
            g.glow(true).color(cols[i]);
            XmasGeo.star(g, "white", 1.3f, 0.45f, 5);
            g.glow(false);
            g.pop();
            halo(g, 11, 0, 0, 5.5f, alpha(0.55, cols[i] & 0xFFFFFF));
            for (int k = 1; k < 9; k++) {
                g.push();
                g.rotY(-k * 6.5f);
                float f = 1 - k / 9f;
                Cos3Models.sparkle(g, 11, 0, 0, 0.55f * f, alpha(f, cols[i] & 0xFFFFFF));
                if (k % 2 == 0) halo(g, 11, 0, 0, 3.2f * f, alpha(0.35 * f, cols[i] & 0xFFFFFF));
                g.pop();
            }
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Gewitter-Aura
    static void storm(G g, A a) {
        float t = a.time;
        // dark storm cloud ring above the head (puffy ellipsoids)
        g.push();
        g.translate(0, 5.5f, 0);
        g.rotY(t * 18);
        for (int i = 0; i < 12; i++) {
            double ang = Math.PI * 2 * i / 12;
            float r = 8.0f + (i % 2) * 1.2f;
            float bob = sin(t * 1.4f + i) * 0.4f;
            g.color(0xFF4A5268);
            Geo.ellipsoid(g, "hd_fluff", cos(ang) * r, bob + (i % 3) * 0.5f, sin(ang) * r, 2.3f, 1.7f, 2.1f, 10, 6);
            g.color(0xFF6A7490);
            Geo.ellipsoid(g, "hd_fluff", cos(ang + 0.25) * (r - 0.6f), bob + 1.3f, sin(ang + 0.25) * (r - 0.6f), 1.6f, 1.3f, 1.6f, 8, 5);
        }
        // flicker light inside the cloud
        float fl = (float) Math.pow(Math.max(0, sin(t * 11) * sin(t * 3.7f)), 3);
        g.pop();
        halo(g, 0, 5.5f, 0, 18, alpha(0.35 * fl, 0x9AE8FF));
        // lightning bolts striking down to the ground
        for (int i = 0; i < 5; i++) {
            float cycle = fract(t * 1.3f + i * 0.29f);
            if (cycle > 0.36f) continue;
            double ang = Math.PI * 2 * ((i * 0.61f + Math.floor(t * 1.3f + i * 0.37f) * 0.29f) % 1.0);
            float r = 7.5f + (i % 2) * 1.2f;
            float[] top = p(cos(ang) * (r + 1), 4.5f, sin(ang) * (r + 1)), bot = p(cos(ang + 0.3) * (r + 1.5f), -24, sin(ang + 0.3) * (r + 1.5f));
            float k = 1 - cycle / 0.36f;
            arc(g, top, bot, 3.4f, 0.22f * (0.5f + k), i * 13 + 5, t, alpha(0.5 + 0.5 * k, 0x9AE8FF));
            halo(g, bot[0], -23.5f, bot[2], 6 * k, alpha(0.6 * k, 0x9AE8FF));
        }
        // rain drops
        for (int i = 0; i < 18; i++) {
            float ph = fract(t * 1.8f + i * 0.137f);
            double ang = i * 2.39;
            float r = 5 + (i % 4) * 1.3f;
            g.glow(true).color(0x889AD8FF);
            g.push();
            g.translate(cos(ang) * (r + 2), 4 - ph * 28, sin(ang) * (r + 2));
            Geo.ellipsoid(g, "white", 0, 0, 0, 0.07f, 0.6f, 0.07f, 4, 2);
            g.pop();
            g.glow(false);
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Kirschblüten-Wind
    static void petal(G g, int col, float s) {
        float[][] o = {{0, -1.0f * s}, {0.55f * s, -0.55f * s}, {0.62f * s, 0.2f * s}, {0.3f * s, 0.9f * s}, {0.08f * s, 0.75f * s}, {0, 1.0f * s}, {-0.08f * s, 0.75f * s}, {-0.3f * s, 0.9f * s}, {-0.62f * s, 0.2f * s}, {-0.55f * s, -0.55f * s}};
        int oc = g.color;
        g.color(col);
        // two halves slightly cupped
        for (int k = -1; k <= 1; k += 2) {
            g.push();
            g.rotY(k * 12);
            float[][] half = new float[6][];
            int j = 0;
            half[j++] = new float[]{0, -1.0f * s};
            for (int i = 1; i <= 4; i++) half[j++] = new float[]{k * o[i][0], o[i][1]};
            half[j++] = new float[]{0, 1.0f * s};
            HdGeo.puff(g, "hd_petal", half, 0.08f, 0.03f, 1, new float[]{0, 0, 1, 1}, new float[]{0.4f, 0.4f, 0.6f, 0.6f});
            g.pop();
        }
        g.color(oc);
    }

    static void sakura(G g, A a) {
        float t = a.time;
        int n = 16;
        int[] cols = {0xFFFFB8D0, 0xFFFFD4E4, 0xFFFF9ABE};
        for (int i = 0; i < n; i++) {
            float ph = fract(t * 0.22f + i * 0.137f);
            float s = life(ph);
            if (s < 0.03f) continue;
            double ang = i * 2.39 + t * 0.9 + ph * 3;
            float r = 6 + (i % 4) * 1.4f;
            g.push();
            g.translate(cos(ang) * r, 6 - ph * 30, sin(ang) * r);
            g.rotY(t * 90 + i * 40);
            g.rotX(t * 70 + i * 25);
            petal(g, cols[i % 3], 1.7f * s);
            g.pop();
        }
        // a few blossoms drifting
        for (int i = 0; i < 3; i++) {
            float ph = fract(t * 0.12f + i / 3f);
            float s = life(ph);
            if (s < 0.05f) continue;
            double ang = i * 2.1 + t * 0.5;
            g.push();
            g.translate(cos(ang) * 8.5f, 2 - ph * 26, sin(ang) * 8.5f);
            g.rotX(40 + t * 30);
            g.rotZ(t * 50 + i * 90);
            g.scale(s);
            HdHats.flower(g, 5, 1.3f, 0xFFFFC0D8, 0xFFFFE06A, 30);
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Musik-Aura
    static void note(G g, boolean dbl) {
        // eighth note (or beamed pair): tilted oval heads, stems, flag
        Geo.ellipsoid(g, "hd_gloss", 0, 0, 0, 0.75f, 0.52f, 0.42f, 10, 6);
        Geo.tube(g, "hd_plastic", p(0.62f, 0.1f, 0), p(0.62f, 3.0f, 0), 0.12f, 0.12f, 5);
        if (dbl) {
            Geo.ellipsoid(g, "hd_gloss", 2.0f, 0.4f, 0, 0.75f, 0.52f, 0.42f, 10, 6);
            Geo.tube(g, "hd_plastic", p(2.62f, 0.5f, 0), p(2.62f, 3.4f, 0), 0.12f, 0.12f, 5);
            bbox(g, "hd_plastic", 0.5f, 2.6f, -0.14f, 2.74f, 3.2f, 0.14f, 0.06f);
        } else {
            float[][] fl = {{0.62f, 3.0f}, {0.62f, 2.2f}, {1.5f, 1.4f}, {1.7f, 1.8f}};
            HdGeo.puff(g, "hd_plastic", fl, 0.2f, 0.08f, 1);
        }
    }

    static void music(G g, A a) {
        float t = a.time;
        int[] cols = {0xFF6AE0FF, 0xFFFF6AD0, 0xFFFFE24A, 0xFF8AFF6A};
        for (int i = 0; i < 9; i++) {
            float ph = fract(t * 0.35f + i / 9f);
            float s = life(ph);
            if (s < 0.03f) continue;
            double ang = Math.PI * 2 * i / 9 + t * 0.5;
            float r = 8 + sin(ph * 6 + i) * 1;
            float bounce = (float) Math.pow(Math.abs(sin(t * 4 + i)), 3) * 0.12f;
            g.push();
            g.translate(cos(ang) * r, -18 + ph * 24, sin(ang) * r);
            g.rotY((float) Math.toDegrees(-ang) + 90);
            g.rotZ(sin(t * 5 + i) * 15);
            g.scale(s * (1 + bounce));
            g.glow(true).color(cols[i % 4]);
            note(g, i % 3 == 0);
            g.glow(false);
            g.pop();
            halo(g, cos(ang) * r, -17 + ph * 24, sin(ang) * r, 4 * s, alpha(0.3 * s, cols[i % 4] & 0xFFFFFF));
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Seifenblasen
    static void bubbles(G g, A a) {
        float t = a.time;
        for (int i = 0; i < 12; i++) {
            float ph = fract(t * 0.2f + i * 0.173f);
            float grow = smooth(ph * 6);
            boolean pop = ph > 0.94f;
            double ang = i * 2.1 + ph * 2;
            float r = 6 + (i % 3) * 2;
            float s = (1.2f + (i % 4) * 0.45f) * grow;
            float wob = 1 + 0.06f * sin(t * 6 + i);
            float x = cos(ang) * r + sin(t * 2 + i) * 0.8f, y = -23 + ph * 30, z = sin(ang) * r;
            if (pop) {
                // pop: ring of droplets
                float k = (ph - 0.94f) / 0.06f;
                for (int d = 0; d < 6; d++) {
                    double da = Math.PI * 2 * d / 6;
                    Cos3Models.sparkle(g, x + cos(da) * s * (1 + k), y + sin(da) * s * (1 + k), z, 0.25f * (1 - k), alpha(1 - k, 0xDDF4FF));
                }
                continue;
            }
            g.push();
            g.translate(x, y, z);
            g.scale(wob, 1 / wob, wob);
            g.color(0xFFFFFFFF);
            g.sphere("hd_bubble", s, 14, 8, 0, 0, 1, 1);
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Regenbogen-Ring
    static void rainbowRing(G g, A a) {
        float t = a.time;
        for (int ring = 0; ring < 2; ring++) {
            g.push();
            g.translate(0, ring == 0 ? -12 + sin(t * 1.5f) * 2 : -20 + sin(t * 1.5f + 2) * 1.5f, 0);
            g.rotX(ring == 0 ? 8 * sin(t) : -6);
            float R = ring == 0 ? 10 : 8.5f;
            int seg = 40;
            // rainbow tube: segments with shifting hue
            for (int i = 0; i < seg; i++) {
                double a0 = Math.PI * 2 * i / seg, a1 = Math.PI * 2 * (i + 1) / seg;
                g.glow(true).color(Geo.hue(i / (float) seg + t * 0.2f * (ring == 0 ? 1 : -1), 0.62, 1));
                Geo.tube(g, "white", p(cos(a0) * R, 0, sin(a0) * R), p(cos(a1) * R, 0, sin(a1) * R), 0.32f, 0.32f, 6);
            }
            g.color(alpha(0.3, 0xFFFFFF));
            g.torus("hd_softglow", R, 1.1f, 32, 4, 0.1f, 0.1f, 0.9f, 0.9f);
            g.glow(false);
            g.pop();
        }
        for (int i = 0; i < 8; i++) {
            double an = Math.PI * 2 * i / 8 + t * 1.2;
            float tw = 0.5f + 0.5f * sin(t * 4 + i);
            Cos3Models.sparkle(g, cos(an) * 10, -12 + sin(t * 1.5f) * 2 + 0.6f, sin(an) * 10, 0.3f + 0.4f * tw, Geo.hue(i / 8.0 + t * 0.2, 0.3, 1));
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Leere-Aura
    static void voidAura(G g, A a) {
        float t = a.time;
        groundGlow(g, 0xAA8A2AFF, 10);
        // swirling accretion disc on the ground
        g.push();
        g.translate(0, -23.7f, 0);
        g.rotY(-t * 50);
        g.glow(true).color(0xCCB04AFF);
        for (int k = 0; k < 3; k++) {
            g.push();
            g.rotY(k * 120);
            int n = 16;
            float[][] sp = new float[n + 1][];
            float[] rr = new float[n + 1], rz = new float[n + 1];
            for (int i = 0; i <= n; i++) {
                float f = i / (float) n;
                double an = f * Math.PI * 1.3;
                float r = 9.5f - f * 5.5f;
                sp[i] = p(cos(an) * r, 0.1f + f * 0.2f, sin(an) * r);
                rr[i] = 0.9f * (1 - f) + 0.1f;
                rz[i] = 0.06f;
            }
            XmasGeo.sweep(g, "c3_beam", sp, rr, rz, null, 4, 1);
            g.pop();
        }
        g.glow(false);
        g.pop();
        // dark orbs spiralling inwards (black core + purple rim glow)
        for (int i = 0; i < 12; i++) {
            float ph = fract(t * 0.35f + i / 12f);
            double ang = i * 2.39 - ph * 5 - t * 0.4;
            float r = 11 * (1 - ph) + 2;
            float s = smooth(ph * 3) * (1 - ph * 0.6f);
            float x = cos(ang) * r, y = -22 + ph * 20 + sin(i) * 2, z = sin(ang) * r;
            g.color(0xFF0A0612);
            ball(g, "hd_gloss", x, y, z, 0.55f * s + 0.05f, 8);
            halo(g, x, y, z, 2.8f * s, alpha(0.6 * s, 0xB04AFF));
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Glitzerstaub
    static void sparkle(G g, A a) {
        float t = a.time;
        int[] cols = {0xFFFFFFFF, 0xFFFFE8A0, 0xFFB8E8FF, 0xFFFFB8E8};
        for (int i = 0; i < 20; i++) {
            float ph = fract(t * 0.25f + i * 0.0913f);
            double ang = i * 2.39 + t * 0.3;
            float r = 5 + (i * 7 % 5);
            float tw = 0.4f + 0.6f * Math.abs(sin(t * 3 + i * 1.7f));
            float s = life(ph) * tw;
            if (s < 0.03f) continue;
            int c = cols[i % 4];
            float x = cos(ang) * r, y = 4 - ph * 28, z = sin(ang) * r;
            g.push();
            g.translate(x, y, z);
            g.rotY(t * 90 + i * 30);
            g.glow(true).color(c);
            // four point star gem
            gem(g, "white", 0.32f * s, 1.4f * s, 1.4f * s, 4);
            g.rotX(90);
            gem(g, "white", 0.32f * s, 0.95f * s, 0.95f * s, 4);
            g.glow(false);
            g.pop();
            if (i % 2 == 0) halo(g, x, y, z, 2.4f * s, alpha(0.4 * s, c & 0xFFFFFF));
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Herbstlaub
    static void leaves(G g, A a) {
        float t = a.time;
        int[] cols = {0xFFE8A030, 0xFFE8602A, 0xFFC8301E, 0xFFB8C83A};
        for (int i = 0; i < 14; i++) {
            float ph = fract(t * 0.18f + i * 0.157f);
            float s = life(ph);
            if (s < 0.03f) continue;
            double ang = i * 2.2 + t * 0.7 + ph * 2.5;
            float r = 7 + (i % 3) * 1.5f;
            g.push();
            g.translate(cos(ang) * r, 4 - ph * 28, sin(ang) * r);
            g.rotY(t * 80 + i * 50);
            g.rotZ(sin(t * 2 + i) * 40);
            g.rotX(sin(t * 1.3f + i) * 30);
            g.scale(s * 1.6f);
            g.push();
            g.translate(-1.1f, 0, 0);
            HdHats.leaf(g, 2.4f, cols[i % 4]);
            g.pop();
            // stem
            g.color(0xFF6A4020);
            Geo.tube(g, "hd_wood", p(-1.1f, 0, 0), p(-1.8f, -0.2f, 0), 0.07f, 0.05f, 3);
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Runenkreis
    static void runes(G g, A a) {
        float t = a.time;
        float pulse = 0.8f + 0.2f * sin(t * 2.5f);
        g.push();
        g.translate(0, -23.8f, 0);
        g.rotY(t * 25);
        // two glowing rings + rune band standing up slightly
        g.glow(true).color(alpha(pulse, 0xC8A0FF));
        g.torus("white", 10.5f, 0.12f, 48, 3, 0, 0, 1, 1);
        g.torus("white", 8.2f, 0.09f, 40, 3, 0, 0, 1, 1);
        g.color(alpha(0.95 * pulse, 0xFFFFFF));
        lathe(g, "hd_rune", new float[]{9.4f, 9.4f}, new float[]{0.05f, 1.2f}, 40, 0, 1, 3, 0);
        g.color(alpha(0.25 * pulse, 0x9A6AFF));
        g.ring("hd_softglow", 7.5f, 11.5f, 0.02f, 40, 0.1f, 0.5f, 0.9f, 0.5f);
        // hexagram lines on the ground
        g.color(alpha(0.8 * pulse, 0xC8A0FF));
        for (int k = 0; k < 6; k++) {
            double a0 = Math.PI * 2 * k / 6, a1 = Math.PI * 2 * (k + 2) / 6;
            Geo.tube(g, "white", p(cos(a0) * 8.2f, 0.05f, sin(a0) * 8.2f), p(cos(a1) * 8.2f, 0.05f, sin(a1) * 8.2f), 0.07f, 0.07f, 3);
        }
        g.glow(false);
        g.pop();
        // floating rune stones rising
        for (int i = 0; i < 6; i++) {
            float ph = fract(t * 0.3f + i / 6f);
            float s = life(ph);
            if (s < 0.03f) continue;
            double an = Math.PI * 2 * i / 6 + t * 0.4;
            g.push();
            g.translate(cos(an) * 9.4f, -22.5f + ph * 16, sin(an) * 9.4f);
            g.rotY((float) Math.toDegrees(-an) + 90 + t * 40);
            g.scale(s * 1.4f);
            g.color(0xFF3A3448);
            bbox(g, "hd_steel", -0.75f, -1.0f, -0.25f, 0.75f, 1.0f, 0.25f, 0.18f);
            g.glow(true).color(0xFFD8B0FF);
            g.push();
            g.translate(0, 0, 0.27f);
            cbox(g, "white", 0, 0.1f, 0, 0.14f, 1.3f, 0.04f, 0.02f);
            g.rotZ(40);
            cbox(g, "white", 0.25f, 0.25f, 0, 0.12f, 0.8f, 0.04f, 0.02f);
            g.pop();
            g.glow(false);
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Stein-Orbit (lego bricks)
    static void brick(G g, int col, int w) {
        int oc = g.color;
        g.color(col);
        float hw = w * 0.8f;
        bbox(g, "hd_brick", -hw, -0.7f, -0.8f, hw, 0.7f, 0.8f, 0.1f);
        for (int i = 0; i < w; i++)
            for (int k = 0; k < 2; k++) {
                g.push();
                g.translate(-hw + 0.8f + i * 1.6f, 0.7f, k == 0 ? -0.4f : 0.4f);
                lathe(g, "hd_brick", new float[]{0.3f, 0.3f, 0.26f, 0}, new float[]{-0.02f, 0.3f, 0.36f, 0.37f}, 8);
                g.pop();
            }
        g.color(oc);
    }

    static void bricks(G g, A a) {
        float t = a.time;
        int[] cols = {0xFFE3000B, 0xFFFFCD03, 0xFF006CB7, 0xFF00852B};
        for (int i = 0; i < 8; i++) {
            double an = Math.PI * 2 * i / 8 + t * 0.8;
            float y = -10 + sin(t * 1.6f + i * 0.8f) * 3;
            g.push();
            g.translate(cos(an) * 10, y, sin(an) * 10);
            g.rotY(t * 60 + i * 45);
            g.rotX(sin(t + i) * 25);
            g.scale(1.45f);
            brick(g, cols[i % 4], i % 3 == 0 ? 1 : 2);
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }
}
