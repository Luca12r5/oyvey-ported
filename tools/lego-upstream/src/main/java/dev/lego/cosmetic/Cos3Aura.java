package dev.lego.cosmetic;

import static dev.lego.cosmetic.Cos3Geo.*;

import dev.lego.cosmetic.Cos.A;
import dev.lego.cosmetic.Cos.Rarity;
import dev.lego.cosmetic.Cos.Slot;

/** Premium auras and wings (body space). Auras keep a radius > 8.5 around the arms or stay below the hips. */
final class Cos3Aura {
    private Cos3Aura() { }

    static void register() {
        Cos.add("aura_c3_splash", "Wasser-Splash", Slot.AURA, Rarity.EPIC, Cos3Aura::splash);
        Cos.add("aura_c3_book", "Zauberbuch-Orbit", Slot.AURA, Rarity.LEGENDARY, Cos3Aura::bookOrbit);
        Cos.add("aura_c3_hexshield", "Hex-Schild", Slot.AURA, Rarity.EPIC, Cos3Aura::hexShield);
        Cos.add("aura_c3_tesla", "Tesla-Feld", Slot.AURA, Rarity.LEGENDARY, Cos3Aura::tesla);
        Cos.add("aura_c3_planets", "Mini-Sonnensystem", Slot.AURA, Rarity.LEGENDARY, Cos3Aura::planets);
        Cos.add("wings_c3_mech", "Mecha-Schwingen", Slot.WINGS, Rarity.LEGENDARY, Cos3Aura::mechWings);
        Cos.add("wings_c3_prism", "Prismenflügel", Slot.WINGS, Rarity.EPIC, Cos3Aura::prismWings);
    }

    static float mv(A a) { return a.preview ? 0 : clamp(a.move, 0, 1); }

    // ================================================================== water splash
    static void splash(G g, A a) {
        float t = a.time;
        // swirling water streams around the hips
        for (int k = 0; k < 2; k++) {
            int n = 40;
            float R = k == 0 ? 6.1f : 7.0f;
            float[][] pts = new float[n + 1][];
            for (int i = 0; i <= n; i++) {
                double an = Math.PI * 2 * i / n;
                float y = k == 0 ? -13.4f + 0.45f * sin(an * 3 + t * 3) : -14.2f + 0.35f * sin(an * 4 - t * 2.5f);
                pts[i] = new float[]{(float) Math.cos(an) * R, y, (float) Math.sin(an) * R};
            }
            float[] rr = new float[n + 1];
            for (int i = 0; i <= n; i++) rr[i] = (k == 0 ? 0.3f : 0.18f) * (0.75f + 0.25f * sin(i * Math.PI * 2 / n * 5 + t * 4));
            g.color(k == 0 ? 0xB84AB8FF : 0xC8B8E8FF);
            Geo.chain(g, "white", pts, rr, 6);
        }
        // splash crowns
        for (int i = 0; i < 2; i++) {
            float f = fract(t * 0.55f + i * 0.5f);
            float r = 5.2f + f * 4.2f, h = 2.1f * (1 - f) + 0.3f;
            g.color(alpha((1 - f) * 0.95, 0xFFFFFF));
            g.cylinder("c3_splash", r, r * 1.12f, -15.0f, -15.0f + h, 44, false, 0, 0, 1, 1);
        }
        // droplets jumping in arcs
        for (int i = 0; i < 12; i++) {
            float p = fract(t * 0.5f + i / 12f);
            float ang = i * 30 + t * 20;
            float rad = 9.1f + 2.2f * p, y = -14 + 8.5f * 4 * p * (1 - p);
            float sc = smooth(p / 0.08f) * smooth((1 - p) / 0.08f);
            if (sc < 0.02f) continue;
            g.push();
            g.rotY(ang);
            g.translate(rad, y, 0);
            alignY(g, 2.5f, 34 * (1 - 2 * p), 0);
            g.scale(sc * (0.8f + 0.3f * (i % 3) / 2f));
            g.color(0xC87AD4FF);
            lathe(g, "white", new float[]{0, 0.42f, 0.5f, 0.34f, 0.14f, 0}, new float[]{-0.5f, -0.35f, -0.05f, 0.35f, 0.7f, 0.95f}, 10);
            g.color(0xF0FFFFFF);
            ball(g, "white", 0.18f, 0, 0.44f, 0.13f, 5);
            g.pop();
        }
        // ground ripples
        for (int i = 0; i < 3; i++) {
            float f = fract(t * 0.4f + i / 3f);
            g.push();
            g.translate(0, -23.85f + i * 0.02f, 0);
            g.rotX(-90);
            g.color(alpha((1 - f) * 0.8, 0xFFFFFF));
            float s = 9 + f * 18;
            g.plane("c3_ripple", 0, 0, s, s, 0, 0, 1, 1);
            g.pop();
        }
        // spray sparkles
        for (int i = 0; i < 8; i++) {
            float f = fract(t * 0.9f + i * 0.125f), an = i * 45 + t * 40;
            Cos3Models.sparkle(g, cos(Math.toRadians(an)) * (6.5f + f * 3), -14.5f + f * 2, sin(Math.toRadians(an)) * (6.5f + f * 3), 0.22f, alpha(1 - f, 0xE8FAFF));
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== orbiting magic book
    static void bookOrbit(G g, A a) {
        float t = a.time;
        float ang = t * 32;
        for (int j = 1; j <= 7; j++) {
            double an = Math.toRadians(ang - j * 7);
            Cos3Models.sparkle(g, (float) Math.cos(an) * 11.2f, -3 + sin(t * 1.7f - j * 0.2f) * 0.6f + sin(j * 1.7f) * 0.5f, (float) -Math.sin(an) * 11.2f, 0.28f - j * 0.025f, alpha(0.9 - j * 0.11, 0xD8A8FF));
        }
        g.push();
        g.rotY(ang);
        g.translate(11.2f, -3 + sin(t * 1.7f) * 0.6f, 0);
        g.rotY(90);
        g.rotX(-28);
        g.rotZ(sin(t * 1.3f) * 5);
        g.scale(1.35f);
        float[] plain = {0.01f, 0.01f, 0.04f, 0.04f};
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.rotY(-18 * s);
            g.color(0xFFFFFFFF);
            float xa = s > 0 ? 0 : -3.25f, xb = s > 0 ? 3.25f : 0;
            bbox(g, "c3_cover", xa, -2.25f, -0.4f, xb, 2.25f, -0.16f, 0.08f);
            float pa = s > 0 ? 0.1f : -3.0f, pb = s > 0 ? 3.0f : -0.1f;
            g.box6("c3_page", pa, -2.05f, -0.2f, pb, 2.05f, 0.3f, new float[][]{s > 0 ? new float[]{0, 0, 1, 1} : new float[]{1, 0, 0, 1}, plain, plain, plain, plain, plain});
            g.pop();
        }
        g.color(0xFF5A1A7A);
        Geo.tube(g, "c3_cover", new float[]{0, -2.25f, -0.3f}, new float[]{0, 2.25f, -0.3f}, 0.3f, 0.3f, 8);
        // flipping page
        float f = fract(t * 0.45f);
        if (f < 0.6f) {
            float th = 18 + 144 * smooth(f / 0.6f);
            g.push();
            g.translate(0, 0, 0.32f);
            g.rotY(-th);
            g.color(0xFFFFFFFF);
            float lift = sin(Math.PI * f / 0.6f) * 0.5f;
            g.quad("c3_page", new float[]{0, 2.0f, 0}, new float[]{1.45f, 2.0f, lift}, new float[]{1.45f, -2.0f, lift}, new float[]{0, -2.0f, 0}, new float[]{0, 0, 0.5f, 0, 0.5f, 1, 0, 1});
            g.quad("c3_page", new float[]{1.45f, 2.0f, lift}, new float[]{2.9f, 2.0f, lift * 0.6f}, new float[]{2.9f, -2.0f, lift * 0.6f}, new float[]{1.45f, -2.0f, lift}, new float[]{0.5f, 0, 1, 0, 1, 1, 0.5f, 1});
            g.pop();
        }
        // glyphs rising out of the pages
        g.glow(true);
        for (int k = 0; k < 3; k++) {
            float gp = fract(t * 0.6f + k / 3f);
            g.color(alpha(Math.sin(Math.PI * gp) * 0.95, k == 1 ? 0xFFD86A : 0xC88AFF));
            g.push();
            g.translate(sin(k * 2.1f + gp * 2) * 1.3f, 0.6f + gp * 4.5f, 0.9f + gp * 0.9f);
            g.rotY(sin(t * 2 + k) * 25);
            float sz = 0.9f + gp * 0.7f;
            g.plane("c3_glyphs", 0, 0, sz, sz, k / 4f, 0, (k + 1) / 4f, 1);
            g.pop();
        }
        g.glow(false);
        halo(g, 0, 0.4f, 1.0f, 3.2f, 0x50B86AFF);
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== hexagon energy shield
    static void hexShield(G g, A a) {
        float t = a.time;
        float[][] hex = new float[6][];
        for (int k = 0; k < 6; k++) {
            double an = Math.toRadians(30 + 60 * k);
            hex[k] = new float[]{(float) Math.cos(an) * 1.55f, (float) Math.sin(an) * 1.55f};
        }
        g.glow(true);
        for (int r = 0; r < 3; r++) {
            float y = -1.5f - r * 5.2f, R = 10.5f - Math.abs(r - 1) * 0.8f;
            float dir = r % 2 == 0 ? 1 : -1;
            float ang0 = dir * t * 18 + r * 18;
            for (int i = 0; i < 10; i++) {
                float pulse = 0.5f + 0.5f * sin(t * 2.5f + i * 0.9f + r * 1.7f);
                boolean hit = Capes.hash((int) Math.floor(t * 2), i + r * 10, 3) > 0.93;
                g.push();
                g.rotY(ang0 + i * 36);
                g.translate(R, y, 0);
                g.rotY(90);
                g.rotX((r - 1) * 18);
                g.color(hit ? 0xF0FFFFFF : alpha(0.35 + 0.45 * pulse, r == 1 ? 0x4AE8FF : 0x6AA8FF));
                extrude(g, "c3_hex", hex, -0.1f, 0.1f, FULL, new float[]{0.48f, 0.04f, 0.52f, 0.08f});
                g.pop();
            }
        }
        g.glow(false);
        // soft ground ring
        g.push();
        g.translate(0, -23.8f, 0);
        g.rotY(t * 20);
        g.glow(true).color(0x604AE8FF);
        g.torus("white", 9.8f, 0.12f, 40, 3, 0, 0, 1, 1);
        g.glow(false);
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== tesla field
    static void tesla(G g, A a) {
        float t = a.time;
        float R = 10;
        float[][] orb = new float[3][];
        double[] an = new double[3];
        for (int i = 0; i < 3; i++) {
            an[i] = Math.toRadians(t * 40 + i * 120);
            orb[i] = new float[]{(float) Math.cos(an[i]) * R, -4 + sin(t * 1.3f + i * 2) * 2.5f - i * 1.2f, (float) -Math.sin(an[i]) * R};
        }
        for (int i = 0; i < 3; i++) {
            float[] p = orb[i];
            g.glow(true).color(0xFFFFFFFF);
            ball(g, "white", p[0], p[1], p[2], 0.45f, 8);
            g.color(0x704AD8FF);
            ball(g, "c3_crystal", p[0], p[1], p[2], 1.0f, 12);
            g.push();
            g.translate(p[0], p[1], p[2]);
            g.rotX(60 + i * 20);
            g.rotY(t * 200);
            g.color(0xFF8AE8FF);
            g.torus("white", 1.35f, 0.07f, 16, 3, 0, 0, 1, 1);
            g.pop();
            halo(g, p[0], p[1], p[2], 2.8f, 0x704AD8FF);
            g.glow(false);
            // arc to the next orb along the circle (never through the body)
            int j = (i + 1) % 3;
            int step = (int) Math.floor(t * 8);
            if (Capes.hash(step, i, 9) < 0.35) continue;
            float[] q = orb[j];
            float[] prev = p;
            for (int s = 1; s <= 3; s++) {
                double m = an[i] + Math.toRadians(120) * s / 3;
                float yy = p[1] + (q[1] - p[1]) * s / 3f;
                float[] cur = s == 3 ? q : new float[]{(float) Math.cos(m) * R, yy, (float) -Math.sin(m) * R};
                arc(g, prev, cur, 1.1f, 0.07f, i * 7 + s, t, 0xC04AD8FF);
                prev = cur;
            }
        }
        // crackle at the feet
        int step = (int) Math.floor(t * 6);
        for (int k = 0; k < 3; k++) {
            if (Capes.hash(step, k, 21) < 0.4) continue;
            double a0 = Capes.hash(step, k, 22) * Math.PI * 2;
            arc(g, new float[]{(float) Math.cos(a0) * 5.6f, -23.6f, (float) Math.sin(a0) * 5.6f}, new float[]{(float) Math.cos(a0 + 0.7) * 5.6f, -23.6f, (float) Math.sin(a0 + 0.7) * 5.6f}, 0.8f, 0.05f, step + k, t, 0xB04AD8FF);
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== mini solar system around the waist
    static void planets(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(0, -6, 0);
        g.rotX(10);
        g.rotZ(-6);
        float[] R = {9.5f, 12, 14.5f}, sp = {40, 28, 19}, pr = {0.8f, 1.15f, 1.5f};
        String[] tex = {"c3_planet_ice", "c3_planet_lava", "c3_planet_gas"};
        for (int i = 0; i < 3; i++) {
            g.glow(true).color(0x60E8F0FF);
            g.torus("white", R[i], 0.05f, 56, 3, 0, 0, 1, 1);
            g.glow(false);
            g.push();
            g.rotY(t * sp[i] + i * 130);
            g.translate(R[i], 0, 0);
            g.push();
            g.rotY(-t * 70);
            g.color(0xFFFFFFFF);
            g.sphere(tex[i], pr[i], 16, 8, 0, 0, 1, 1);
            g.pop();
            if (i == 1) halo(g, 0, 0, 0, pr[i] * 4, 0x60FF6A2A);
            if (i == 2) {
                g.push();
                g.rotZ(22);
                g.color(0xFFFFF0D8);
                g.ring("c3_ring", pr[i] * 1.35f, pr[i] * 2.1f, 0, 24, 0, 0, 1, 1);
                g.pop();
            }
            if (i == 0) {
                g.push();
                g.rotY(t * 150);
                g.translate(1.5f, 0.2f, 0);
                g.color(0xFFD8D8E0);
                g.sphere("c3_metal", 0.3f, 8, 4, 0, 0, 1, 1);
                g.pop();
            }
            g.pop();
        }
        // twinkling stars on the orbits
        for (int i = 0; i < 10; i++) {
            double an = Math.toRadians(i * 36 + t * 8);
            float r = R[i % 3] + (i % 2 == 0 ? 0.5f : -0.5f);
            Cos3Models.sparkle(g, (float) Math.cos(an) * r, sin(i * 2.7f) * 0.8f, (float) Math.sin(an) * r, 0.25f, alpha(0.5 + 0.5 * sin(t * 3 + i), 0xFFFFFF));
        }
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== mecha wings
    static void mechWings(G g, A a) {
        float t = a.time, mv = mv(a);
        float flap = sin(t * 1.6f) * 7 * (1 - mv) + sin(t * 6) * 16 * mv;
        float raise = Math.max(0, (a.sneak ? 10 : 35) + flap);
        float sweep = a.sneak ? 55 : 26 - mv * 8;
        float spread = 0.5f + 0.5f * sin(t * 0.8f);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 1.2f, -2.2f, -3.6f);
            g.scale(s * 1.3f, 1.3f, 1.3f);
            g.rotY(sweep);
            g.rotZ(raise);
            g.color(0xFF2E323C);
            ball(g, "c3_metal", 0, 0, 0, 0.75f, 10);
            g.glow(true).color(0xFF4AE8FF);
            g.push(); g.rotX(90); g.torus("white", 0.78f, 0.1f, 14, 3, 0, 0, 1, 1); g.pop();
            g.glow(false);
            // upper arm
            g.color(0xFF3A3E4A);
            bbox(g, "c3_metal", 0.3f, -0.45f, -0.35f, 4.6f, 0.45f, 0.35f, 0.2f);
            blades(g, t, 0, new float[]{1.3f, 2.6f, 3.9f}, new float[]{6.4f, 7.2f, 8.0f}, -12, 6, spread);
            g.translate(4.5f, 0, 0);
            g.color(0xFF2E323C);
            ball(g, "c3_metal", 0, 0, 0, 0.55f, 8);
            g.glow(true).color(0xFF4AE8FF);
            ball(g, "white", 0, 0, 0.5f, 0.2f, 5);
            g.glow(false);
            g.rotZ(-30 + flap * 0.4f);
            g.color(0xFF3A3E4A);
            bbox(g, "c3_metal", 0.2f, -0.38f, -0.3f, 5.2f, 0.38f, 0.3f, 0.18f);
            blades(g, t, 3, new float[]{1.0f, 2.3f, 3.6f, 4.9f}, new float[]{8.6f, 8.8f, 8.2f, 7.0f}, 8, 11, spread);
            // tip claw
            g.push();
            g.translate(5.2f, 0, 0);
            g.rotZ(-90);
            g.glow(true).color(0xFF4AE8FF);
            shard(g, "c3_crystal", 0.3f, 1.3f);
            g.glow(false);
            g.pop();
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    private static void blades(G g, float t, int idx0, float[] xs, float[] lens, float fan0, float fanStep, float spread) {
        for (int i = 0; i < xs.length; i++) {
            float L = lens[i];
            g.push();
            g.translate(xs[i], -0.2f, 0);
            g.rotZ(fan0 + i * fanStep * (0.8f + 0.4f * spread));
            float[][] blade = {{-0.55f, 0}, {0.55f, 0}, {0.5f, -L * 0.78f}, {0.05f, -L}, {-0.35f, -L * 0.84f}};
            g.color(0xFFE4E8F0);
            extrude(g, "c3_metal", blade, -0.16f, 0.16f);
            float[][] edge = {{0.28f, -0.4f}, {0.62f, -0.4f}, {0.57f, -L * 0.78f}, {0.08f, -L * 1.02f}, {0.3f, -L * 0.78f}};
            float pul = 0.7f + 0.3f * sin(t * 3 - (idx0 + i) * 0.6f);
            g.glow(true).color(alpha(pul, 0x4AE8FF));
            extrude(g, "white", edge, -0.22f, 0.22f);
            g.glow(false);
            g.pop();
        }
    }

    // ================================================================== prism wings (floating crystal shards)
    static void prismWings(G g, A a) {
        float t = a.time, mv = mv(a);
        float flap = sin(t * 1.5f) * 6 * (1 - mv) + sin(t * 6) * 14 * mv;
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 1.0f, -2.4f, -3.4f);
            g.scale(s, 1, 1);
            g.rotY(a.sneak ? 50 : 24);
            g.rotZ(flap);
            int n = 9;
            for (int i = 0; i < n; i++) {
                float an = 78 - i * 17;
                float L = 4 + 6.5f * sin(Math.PI * (i + 0.6) / (n + 0.2));
                float base = 0.9f + 0.3f * sin(t * 2 + i * 0.8f);
                double ar = Math.toRadians(an);
                g.push();
                g.rotX(-42 * Math.max(0, (float) Math.sin(ar)));
                g.translate((float) Math.cos(ar) * base, (float) Math.sin(ar) * base, 0);
                g.rotZ(an - 90);
                g.scale(1, 1, 0.55f);
                int c = Capes.hsv(0.52 + i * 0.045 + 0.03 * Math.sin(t), 0.5, 1);
                g.glow(true).color(argb(200, c));
                shard(g, "c3_crystal", 0.55f + 0.15f * sin(i * 1.3f), L);
                g.pop();
                if (i % 2 == 0) {
                    float fx = (float) Math.cos(ar) * (base + L + 0.8f), fy = (float) Math.sin(ar) * (base + L + 0.8f);
                    g.push();
                    g.rotX(-42 * Math.max(0, (float) Math.sin(ar)));
                    g.translate(fx, fy + sin(t * 2.4f + i) * 0.35f, 0);
                    g.rotY(t * 90 + i * 40);
                    g.color(argb(230, c));
                    gem(g, "c3_crystal", 0.35f, 0.5f, 0.5f, 4);
                    g.pop();
                }
            }
            g.glow(false);
            halo(g, 0, 0, 0, 1.8f, 0x70B8E8FF);
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }
}
