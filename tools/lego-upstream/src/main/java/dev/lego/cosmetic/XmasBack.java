package dev.lego.cosmetic;

import static dev.lego.cosmetic.Cos3Geo.*;

import dev.lego.cosmetic.Cos.A;
import dev.lego.cosmetic.Cos.Rarity;
import dev.lego.cosmetic.Cos.Slot;

/**
 * Christmas back items, auras and wings (body space: body x -4..4, y -12..0, z -2..2, back at z = -2;
 * the head occupies x -4..4, y 0..8, z -4..4, arms x +-4..8, feet at y = -24).
 */
final class XmasBack {
    private XmasBack() { }

    static void register() {
        Cos.add("xm_back_scarf", "Weihnachtsschal", Slot.BACK, Rarity.RARE, XmasBack::scarf);
        Cos.add("xm_back_tree", "Mini-Weihnachtsbaum", Slot.BACK, Rarity.LEGENDARY, XmasBack::treePack);
        Cos.add("xm_back_candycanes", "Zuckerstangen", Slot.BACK, Rarity.EPIC, XmasBack::candyCanes);
        Cos.add("xm_back_gifts", "Geschenke-Stapel", Slot.BACK, Rarity.EPIC, XmasBack::giftStack);
        Cos.add("xm_aura_snow", "Schneegestöber", Slot.AURA, Rarity.EPIC, XmasBack::snowStorm);
        Cos.add("xm_aura_lights", "Lichterketten-Spirale", Slot.AURA, Rarity.LEGENDARY, XmasBack::lightSpiral);
        Cos.add("xm_aura_aurora", "Nordlicht-Band", Slot.AURA, Rarity.LEGENDARY, XmasBack::auroraBand);
        Cos.add("xm_wings_ice", "Eiskristall-Flügel", Slot.WINGS, Rarity.LEGENDARY, XmasBack::iceWings);
        Cos.add("xm_wings_angel", "Goldene Sternenflügel", Slot.WINGS, Rarity.LEGENDARY, XmasBack::starWings);
    }

    static float mv(A a) { return a.preview ? 0 : clamp(a.move, 0, 1); }

    // ================================================================== knitted scarf
    static void scarf(G g, A a) {
        float t = a.time, mv = mv(a);
        // thick knitted roll around the neck (stays below the head: y < 0)
        float[][] loop = densLoop(roundLoop(3.35f, 1.35f, 1.35f, 6), 0.6f);
        int n = loop.length, seg = 10;
        float rh = 0.66f, rv = 1.05f, yc = -1.2f;
        float[][][] ring = new float[n + 1][seg + 1][];
        for (int j = 0; j <= n; j++) {
            float[] L = loop[j % n];
            float ca = (float) Math.cos(L[2]), sa = (float) Math.sin(L[2]);
            float puff = 1 + 0.06f * sin(j * 2.1f);
            for (int i = 0; i <= seg; i++) {
                double ph = Math.PI * 2 * i / seg;
                float h = (float) Math.cos(ph) * rh * puff, v = (float) Math.sin(ph) * rv * puff;
                ring[j][i] = new float[]{L[0] + ca * h, yc + v, L[1] + sa * h};
            }
        }
        g.color(0xFFFFFFFF);
        for (int j = 0; j < n; j++) {
            float va = loop[j][3], vb = j + 1 < n ? loop[j + 1][3] : 1;
            for (int i = 0; i < seg; i++) {
                float ua = (float) i / seg, ub = (float) (i + 1) / seg;
                g.quad("xm_scarf", ring[j][i], ring[j][i + 1], ring[j + 1][i + 1], ring[j + 1][i], new float[]{ua, va, ub, va, ub, vb, ua, vb});
            }
        }
        // knot at the front right + two hanging tails with fringes
        g.color(0xFFFFFFFF);
        Geo.ellipsoid(g, "xm_scarf", 2.2f, -1.5f, 2.95f, 1.15f, 1.05f, 0.75f, 10, 6);
        for (int k = 0; k < 2; k++) {
            float len = k == 0 ? 8.2f : 6.8f, z0 = k == 0 ? 2.85f : 3.3f;
            float sw = sin(t * 1.6f + k * 1.3f) * 0.18f + mv * (0.6f + 0.2f * sin(t * 9 + k));
            g.push();
            g.translate(2.2f, -1.6f, 0);
            g.rotZ(k == 0 ? 7 : -13);
            int m = 8;
            float[][] yz = new float[m][];
            float[] w = new float[m];
            for (int i = 0; i < m; i++) {
                float f = i / (float) (m - 1);
                yz[i] = new float[]{-f * len, z0 + 0.25f * f + sw * f * f * 2.2f + 0.15f * sin(f * 5 + t * 1.2f + k)};
                w[i] = 2.0f + 0.15f * f;
            }
            ribbon(g, "xm_scarf", 0, yz, w, 0.36f);
            // fringe
            float[] e = yz[m - 1];
            g.color(0xFFF4F4F4);
            for (int f = 0; f < 5; f++) {
                float x = -0.85f + f * 0.425f;
                Geo.tube(g, "xm_knit", new float[]{x, e[0] + 0.1f, e[1]}, new float[]{x + sin(t * 2 + f) * 0.08f, e[0] - 0.9f, e[1] + sw * 0.4f}, 0.11f, 0.08f, 4);
            }
            g.color(0xFFFFFFFF);
            g.pop();
        }
    }

    /** Resamples a closed loop {x, z, angle} so no segment is longer than maxLen; adds v = arc length fraction as [3]. */
    static float[][] densLoop(float[][] in, float maxLen) {
        java.util.List<float[]> out = new java.util.ArrayList<>();
        int n = in.length;
        for (int i = 0; i < n; i++) {
            float[] a = in[i], b = in[(i + 1) % n];
            float l = (float) Math.hypot(b[0] - a[0], b[1] - a[1]);
            int k = Math.max(1, (int) Math.ceil(l / maxLen));
            float ab = b[2] < a[2] - 1 ? b[2] + (float) (Math.PI * 2) : b[2];
            for (int j = 0; j < k; j++) {
                float f = j / (float) k;
                out.add(new float[]{a[0] + (b[0] - a[0]) * f, a[1] + (b[1] - a[1]) * f, a[2] + (ab - a[2]) * f, 0});
            }
        }
        float per = 0;
        float[] acc = new float[out.size()];
        for (int i = 0; i < out.size(); i++) {
            acc[i] = per;
            float[] a = out.get(i), b = out.get((i + 1) % out.size());
            per += (float) Math.hypot(b[0] - a[0], b[1] - a[1]);
        }
        for (int i = 0; i < out.size(); i++) out.get(i)[3] = acc[i] / per;
        return out.toArray(new float[0][]);
    }

    // ================================================================== mini christmas tree backpack
    static void treePack(G g, A a) {
        float t = a.time, mv = mv(a);
        float Z = -6.0f;
        // back straps + crate
        g.color(0xFFC08850);
        bbox(g, "xm_wood", -2.9f, -11.0f, Z - 2.9f, 2.9f, -7.3f, -2.3f, 0.3f);
        g.color(0xFF7A4A28);
        for (int i = 0; i < 2; i++) {
            float y = -10.2f + i * 1.9f;
            bbox(g, "xm_wood", -3.0f, y - 0.25f, Z - 3.0f, 3.0f, y + 0.25f, -2.2f, 0.1f);
        }
        g.color(0xFFFFFFFF);
        Geo.ellipsoid(g, "xm_snow", 0, -7.3f, Z + 0.35f, 2.6f, 0.4f, 2.9f, 12, 4);
        // trunk
        g.color(0xFF6A4222);
        g.push();
        g.translate(0, 0, Z);
        g.cylinder("xm_bark", 0.62f, 0.55f, -7.4f, -6.2f, 8, false, 0, 0, 1, 1);
        g.pop();
        // tiers (sway a little when walking)
        float sway = sin(t * 1.4f) * 1.5f + mv * sin(t * 8) * 3;
        g.push();
        g.translate(0, -7.3f, Z);
        g.rotX(sway * 0.4f);
        g.rotZ(sway);
        g.translate(0, 7.3f, 0);
        float[][] tiers = {{-6.9f, -2.1f, 3.25f}, {-3.9f, 0.9f, 2.65f}, {-1.2f, 3.3f, 2.05f}, {1.4f, 5.3f, 1.4f}};
        int[] shades = {0xFF2E8A4A, 0xFF34985A, 0xFF3AA462, 0xFF44B06A};
        for (int i = 0; i < tiers.length; i++) {
            float y0 = tiers[i][0], y1 = tiers[i][1], R = tiers[i][2], h = y1 - y0;
            g.push();
            g.rotY(i * 23);
            g.color(shades[i]);
            lathe(g, "xm_pine", new float[]{0, R * 0.82f, R, R * 0.9f, R * 0.55f, R * 0.22f, 0}, new float[]{y0 + 0.25f, y0 - 0.05f, y0 + 0.2f, y0 + h * 0.18f, y0 + h * 0.5f, y0 + h * 0.82f, y1}, 14);
            g.pop();
        }
        // baubles
        int[] bc = {0xFFE0202A, 0xFFFFC83A, 0xFF3A8AFF, 0xFFE0202A, 0xFFC8C8D8, 0xFFFFC83A, 0xFFB03AE8};
        for (int i = 0; i < 9; i++) {
            int ti = i % 3;
            float y0 = tiers[ti][0], y1 = tiers[ti][1], R = tiers[ti][2];
            float f = 0.22f + 0.1f * (i / 3);
            float y = y0 + (y1 - y0) * f, rr = R * (0.95f - f * 1.05f) + 0.3f;
            double an = i * 2.4 + 0.6;
            float x = cos(an) * rr, z = sin(an) * rr;
            g.color(0xFFC8C8C8);
            Geo.tube(g, "c3_metal", new float[]{x, y + 0.55f, z}, new float[]{x, y + 0.3f, z}, 0.12f, 0.12f, 4);
            g.color(bc[i % bc.length]);
            ball(g, "xm_gloss", x, y, z, 0.42f, 8);
        }
        // twinkling fairy lights spiralling up
        for (int i = 0; i < 16; i++) {
            float f = i / 16f;
            float y = -6.6f + f * 11.2f;
            float rr = 0;
            for (float[] tr : tiers) if (y >= tr[0] - 0.1f && y <= tr[1]) { float u = (y - tr[0]) / (tr[1] - tr[0]); rr = Math.max(rr, tr[2] * (1 - u) * 0.95f + 0.2f); }
            if (rr < 0.35f) continue;
            double an = f * Math.PI * 2 * 3.2 + 1.1;
            XmasGeo.fairy(g, cos(an) * rr, y, sin(an) * rr, 0.24f, XmasGeo.LIGHTS[i % XmasGeo.LIGHTS.length], XmasGeo.twinkle(t, i));
        }
        // star
        g.push();
        g.translate(0, 6.05f, 0);
        g.rotY(sin(t * 0.8f) * 35);
        g.glow(true).color(0xFFFFD84A);
        XmasGeo.star(g, "xm_gold", 1.15f, 0.4f, 5);
        g.glow(false);
        halo(g, 0, 0, 0, 3.6f, alpha(0.45 + 0.2 * sin(t * 3), 0xFFE07A));
        g.pop();
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== two crossed candy canes
    static void candyCanes(G g, A a) {
        float t = a.time, mv = mv(a);
        // leather holder on the back
        g.color(0xFFE8C8A8);
        bbox(g, "strap", -1.7f, -9.6f, -2.52f, 1.7f, -6.2f, -2.06f, 0.18f);
        g.color(0xFFFFD36A);
        bbox(g, "c3_metal", -0.55f, -8.3f, -2.64f, 0.55f, -7.5f, -2.45f, 0.08f);
        for (int s = -1; s <= 1; s += 2) {
            float z = s > 0 ? -2.95f : -4.2f;
            float[] bot = {-s * 2.3f, -11.8f, z}, top = {s * 6.2f, 2.3f, z};
            float dx = top[0] - bot[0], dy = top[1] - bot[1], l = (float) Math.hypot(dx, dy);
            dx /= l; dy /= l;
            float px = dy * s, py = -dx * s;
            float R = 1.55f;
            float[] C = {top[0] + px * R, top[1] + py * R};
            int nh = 10;
            float[][] pts = new float[2 + nh][];
            pts[0] = bot;
            pts[1] = new float[]{bot[0] + dx * l * 0.5f, bot[1] + dy * l * 0.5f, z};
            for (int i = 0; i < nh; i++) {
                double th = Math.PI * (i + 1) / nh * 1.05;
                float cx = (float) (-px * Math.cos(th) + dx * Math.sin(th)), cy = (float) (-py * Math.cos(th) + dy * Math.sin(th));
                pts[2 + i] = new float[]{C[0] + cx * R, C[1] + cy * R, z};
            }
            pts[1] = top;
            float[] rr = new float[pts.length];
            java.util.Arrays.fill(rr, 0.62f);
            g.push();
            g.translate(0, -7.9f, 0);
            g.rotZ(sin(t * 1.2f + s) * 1.2f + mv * sin(t * 9) * 2);
            g.translate(0, 7.9f, 0);
            g.color(0xFFFFFFFF);
            limb(g, "xm_candy", pts, rr, 10);
            g.pop();
        }
        // ribbon bow + holly where they cross
        g.push();
        g.translate(0, -7.2f, -5.0f);
        g.rotY(180);
        XmasGeo.bow(g, 0xFF2EA84A, 1.1f);
        g.pop();
        g.push();
        g.translate(0, -6.3f, -5.1f);
        g.rotX(-80);
        XmasGeo.holly(g, 0.7f);
        g.pop();
    }

    // ================================================================== stack of presents
    static void gift(G g, String paper, int ribbonC, float w, float h, float d) {
        float x0 = -w / 2, x1 = w / 2, y0 = 0, y1 = h, z0 = -d / 2, z1 = d / 2;
        g.color(0xFFFFFFFF);
        bbox(g, paper, x0, y0, z0, x1, y1, z1, 0.14f);
        float wr = Math.min(w, d) * 0.1f + 0.2f;
        g.color(ribbonC);
        g.box("xm_ribbon", -wr, y0 - 0.06f, z0 - 0.06f, wr, y1 + 0.06f, z1 + 0.06f, 0, 0, 1, 1);
        g.box("xm_ribbon", x0 - 0.12f, y0 - 0.12f, -wr, x1 + 0.12f, y1 + 0.12f, wr, 0, 0, 1, 1);
    }

    static void giftStack(G g, A a) {
        float t = a.time, mv = mv(a);
        float wob = sin(t * 1.3f) * 1.2f + mv * sin(t * 9) * 2.5f;
        g.push();
        g.translate(0, -11.6f, -4.3f);
        gift(g, "xm_gift:D8202A:FFD24A:1", 0xFFFFD36A, 7.2f, 4.6f, 4.3f);
        g.translate(0, 4.6f, -0.25f);
        g.rotZ(wob * 0.5f);
        g.rotY(8);
        gift(g, "xm_gift:2E9A48:FFFFFF:0", 0xFFE0202A, 5.4f, 3.6f, 3.8f);
        g.translate(0.2f, 3.6f, -0.35f);
        g.rotZ(wob * 0.8f);
        g.rotY(-22);
        gift(g, "xm_gift:3A6AE8:E8F4FF:3", 0xFFF0F0F8, 3.4f, 2.5f, 2.8f);
        g.translate(0, 2.62f, 0);
        XmasGeo.bow(g, 0xFFF0F0F8, 0.95f);
        g.pop();
        // small gift tag on the big box
        g.push();
        g.translate(2.6f, -8.4f, -6.52f);
        g.rotZ(sin(t * 2) * 6 - 10);
        g.color(0xFFF4E8D0);
        bbox(g, "xm_felt", -0.6f, -1.3f, -0.08f, 0.6f, 0, 0.0f, 0.05f);
        g.pop();
        for (int i = 0; i < 4; i++) {
            float ph = fract(t * 0.4f + i * 0.25f);
            Cos3Models.sparkle(g, sin(i * 2.3f) * 3.8f, -7 + ph * 8, -4.5f - cos(i * 1.7f) * 2.8f, 0.28f, alpha((1 - ph) * 0.9, 0xFFF0B0));
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== snow storm aura
    static void snowStorm(G g, A a) {
        float t = a.time;
        // big 3D flakes
        for (int i = 0; i < 7; i++) {
            double h = Capes.hash1(i, 501);
            float ph = fract(t * (0.05f + 0.03f * (float) h) + i / 7f);
            float y = 9 - ph * 34;
            float an = (float) (i * 51.4 + t * (22 + h * 14));
            float R = 10 + 2.2f * (float) Capes.hash1(i, 502);
            float fade = smooth(ph * 8) * smooth((1 - ph) * 8);
            g.push();
            g.rotY(an);
            g.translate(R, y, 0);
            g.rotY(t * 70 + i * 40);
            g.rotX(sin(t + i) * 30);
            g.glow(true).color(alpha(0.92 * fade, 0xE8F6FF));
            XmasGeo.flake(g, "white", 1.0f + 0.45f * (float) h, 0.07f);
            g.glow(false);
            g.pop();
        }
        // swarm of small flakes
        g.color(0xFFFFFFFF);
        for (int i = 0; i < 34; i++) {
            double h = Capes.hash1(i, 511);
            float ph = fract(t * (0.09f + 0.06f * (float) h) + (float) Capes.hash1(i, 512));
            float y = 10 - ph * 35;
            float an = (float) (h * 360 + t * (35 + 25 * Capes.hash1(i, 513)) + ph * 120);
            float R = 9 + 4.5f * (float) Capes.hash1(i, 514);
            float fade = smooth(ph * 6) * smooth((1 - ph) * 6);
            double ar = Math.toRadians(an);
            float s = 0.6f + 0.7f * (float) Capes.hash1(i, 515);
            g.glow(true).color(alpha(0.9 * fade, 0xFFFFFF));
            g.push();
            g.translate(cos(ar) * R, y, sin(ar) * R);
            g.rotY(-an + t * 50);
            g.rotZ(t * 80 + i * 20);
            g.cross("xm_flake", 0, 0, 0, s, s, 0, 0, 1, 1);
            g.pop();
        }
        g.glow(false);
        // swirling wind ribbons
        for (int k = 0; k < 2; k++) {
            int n = 40;
            float[][] top = new float[n + 1][], bot = new float[n + 1][];
            for (int i = 0; i <= n; i++) {
                float f = i / (float) n;
                double an = f * Math.PI * 2.6 + t * (0.9 + k * 0.4) + k * Math.PI;
                float R = 10.5f + k * 1.3f + sin(f * 9 + t) * 0.5f;
                float y = -20 + f * 26 + k * 3;
                float wdt = 1.2f;
                top[i] = new float[]{cos(an) * R, y + wdt, sin(an) * R};
                bot[i] = new float[]{cos(an) * R, y - wdt, sin(an) * R};
            }
            g.glow(true).color(0xA0FFFFFF);
            for (int i = 0; i < n; i++) {
                float ua = (float) i / n, ub = (float) (i + 1) / n;
                g.quad("xm_wind", top[i], top[i + 1], bot[i + 1], bot[i], new float[]{ua, 0, ub, 0, ub, 1, ua, 1});
            }
            g.glow(false);
        }
        // snowy ground with little drifts
        g.push();
        g.translate(0, -23.9f, 0);
        g.rotX(-90);
        g.color(0x90FFFFFF);
        g.plane("orb", 0, 0, 24, 24, 0, 0, 1, 1);
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== fairy light spiral aura
    static void lightSpiral(G g, A a) {
        float t = a.time;
        float turns = 3.1f, y0 = -23, y1 = 10;
        int n = 90;
        float[][] w = new float[n + 1][];
        double rot = Math.toRadians(t * 18);
        for (int i = 0; i <= n; i++) {
            float f = i / (float) n;
            double an = f * Math.PI * 2 * turns + rot;
            float R = 10.4f - 1.6f * f + 0.15f * sin(f * 60);
            w[i] = new float[]{cos(an) * R, y0 + (y1 - y0) * f - 0.25f * Math.abs(sin(f * Math.PI * 2 * turns * 3.5f)), sin(an) * R};
        }
        g.color(0xFF1E3A22);
        float[] rr = new float[n + 1];
        java.util.Arrays.fill(rr, 0.11f);
        Geo.chain(g, "c3_metal", w, rr, 3);
        int nb = 22;
        for (int i = 0; i < nb; i++) {
            float f = (i + 0.5f) / nb;
            double an = f * Math.PI * 2 * turns + rot;
            float R = 10.4f - 1.6f * f;
            float y = y0 + (y1 - y0) * f - 0.3f;
            float wave = 0.5f + 0.5f * sin(i * 0.9f - t * 4.5f);
            float lit = 0.3f + 0.7f * smooth(wave * 1.5f - 0.25f);
            g.push();
            g.translate(cos(an) * R, y, sin(an) * R);
            g.rotY((float) -Math.toDegrees(an));
            g.rotZ(-150 + sin(t * 2 + i) * 8);
            XmasGeo.bulbLite(g, 1.05f, XmasGeo.LIGHTS[i % XmasGeo.LIGHTS.length], lit);
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== northern lights band
    static void auroraBand(G g, A a) {
        float t = a.time;
        for (int k = 0; k < 2; k++) {
            int n = 72;
            float[][] top = new float[n + 1][], bot = new float[n + 1][];
            double spin = t * (k == 0 ? 0.25 : -0.18);
            for (int i = 0; i <= n; i++) {
                double th = Math.PI * 2 * i / n;
                float R = (k == 0 ? 10.8f : 12.0f) + 0.9f * sin(th * 3 + t * 0.7f + k) + 0.35f * sin(th * 17 + t * 1.3f);
                float yb = k == 0 ? 1.8f + 1.5f * sin(th * 2 - t * 0.6f) : -15 + 1.8f * sin(th * 2 + t * 0.5f + 1);
                float hh = (k == 0 ? 6.5f : 5.0f) + 1.8f * sin(th * 4 + t * 0.9f + k * 2);
                double a0 = th + spin;
                float cx = cos(a0), cz = sin(a0);
                bot[i] = new float[]{cx * R, yb, cz * R};
                top[i] = new float[]{cx * (R + 0.9f), yb + hh, cz * (R + 0.9f)};
            }
            g.glow(true).color(k == 0 ? 0xF0FFFFFF : 0xC8FFFFFF);
            for (int i = 0; i < n; i++) {
                float ua = (float) i / n * 2, ub = (float) (i + 1) / n * 2;
                g.quad("xm_aurora", top[i], top[i + 1], bot[i + 1], bot[i], new float[]{ua, 0, ub, 0, ub, 1, ua, 1});
            }
        }
        g.glow(false);
        for (int i = 0; i < 10; i++) {
            float ph = fract(t * 0.3f + i * 0.1f);
            double an = i * 0.63 + t * 0.2;
            float tw = sin(Math.PI * ph);
            Cos3Models.sparkle(g, cos(an) * 11.5f, 5 + (float) Capes.hash1(i, 531) * 7 - 20 * (i % 2), sin(an) * 11.5f, 0.3f, alpha(tw, i % 3 == 0 ? 0xB8FFD8 : 0xE8F0FF));
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== ice crystal wings with fairy lights
    static void iceWings(G g, A a) {
        float t = a.time, mv = mv(a);
        float flap = sin(t * 1.5f) * 6 * (1 - mv) + sin(t * 6) * 14 * mv;
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 1.0f, -2.4f, -3.4f);
            g.scale(s, 1, 1);
            g.rotY(a.sneak ? 50 : 22);
            g.rotZ(flap);
            g.scale(1.3f);
            int n = 8;
            float[][] tips = new float[n][];
            for (int i = 0; i < n; i++) {
                float an = 80 - i * 17;
                float L = 4.5f + 7.5f * sin(Math.PI * (i + 0.6) / (n + 0.2));
                double ar = Math.toRadians(an);
                float lift = -40 * Math.max(0, (float) Math.sin(ar));
                g.push();
                g.rotX(lift);
                g.translate((float) Math.cos(ar) * 0.9f, (float) Math.sin(ar) * 0.9f, 0);
                g.rotZ(an - 90);
                g.scale(1, 1, 0.5f);
                g.glow(true).color(argb(215, Capes.mix(0xE8F8FF, 0x8AD0FF, i / (float) n)));
                shard(g, "c3_crystal", 0.62f + 0.14f * sin(i * 1.3f), L);
                // small side crystals
                if (i % 2 == 1) {
                g.push();
                g.translate(0, L * 0.45f, 0);
                g.rotZ(28);
                g.color(argb(200, 0xC8ECFF));
                shard(g, "c3_crystal", 0.28f, L * 0.32f);
                g.pop();
                }
                g.pop();
                float ca = (float) Math.cos(ar), sa = (float) Math.sin(ar);
                float rr = 0.9f + L + 0.6f;
                double lr = Math.toRadians(lift);
                tips[i] = new float[]{ca * rr, sa * rr * (float) Math.cos(lr), sa * rr * (float) Math.sin(lr)};
            }
            g.glow(false);
            // 3D snowflakes on every other tip
            for (int i = 0; i < n; i += 3) {
                float[] p = tips[i];
                g.push();
                g.translate(p[0], p[1] + sin(t * 2 + i) * 0.3f, p[2]);
                g.rotY(t * 60 + i * 30);
                g.glow(true).color(0xF0FFFFFF);
                XmasGeo.flake(g, "white", 0.85f, 0.06f);
                g.glow(false);
                g.pop();
            }
            // fairy light string along the leading edge (sagging between the upper tips)
            int m = 5;
            float[][] str = new float[m * 6 + 1][];
            int k = 0;
            for (int i = 0; i < m; i++) {
                float[] p0 = tips[i], p1 = tips[i + 1];
                float[] q0 = lerp3(new float[]{0, 0, 0}, p0, 0.72f), q1 = lerp3(new float[]{0, 0, 0}, p1, 0.72f);
                for (int j = 0; j < 6; j++) {
                    float f = j / 6f;
                    float sag = 4 * f * (1 - f) * 0.9f;
                    str[k++] = new float[]{q0[0] + (q1[0] - q0[0]) * f, q0[1] + (q1[1] - q0[1]) * f - sag, q0[2] + (q1[2] - q0[2]) * f - 0.25f};
                }
                if (i == m - 1) { float[] q = q1; str[k++] = new float[]{q[0], q[1], q[2] - 0.25f}; }
            }
            XmasGeo.wire(g, java.util.Arrays.copyOf(str, k), 0.07f);
            for (int i = 1; i < k; i += 4) {
                float[] q = str[i];
                XmasGeo.fairy(g, q[0], q[1] - 0.25f, q[2], 0.22f, XmasGeo.LIGHTS[(i / 3) % XmasGeo.LIGHTS.length], XmasGeo.twinkle(t, i + s * 7));
            }
            halo(g, 0, 0, 0, 2.0f, 0x70B8E8FF);
            g.pop();
        }
        for (int i = 0; i < 6; i++) {
            float ph = fract(t * 0.35f + i / 6f);
            Cos3Models.sparkle(g, (i % 2 == 0 ? 1 : -1) * (4 + 6 * (float) Capes.hash1(i, 541)), 2 - ph * 10, -5 - 2 * (float) Capes.hash1(i, 542), 0.28f, alpha(1 - ph, 0xE8F8FF));
        }
        g.color(0xFFFFFFFF);
    }

    static float[] lerp3(float[] a, float[] b, float f) { return new float[]{a[0] + (b[0] - a[0]) * f, a[1] + (b[1] - a[1]) * f, a[2] + (b[2] - a[2]) * f}; }

    // ================================================================== golden angel wings with hanging stars
    static void starWings(G g, A a) {
        Wings.feathered(g, a, "gold", 16.5f, false, 5);
        Wings.Pose p = Wings.pose(a, 1f);
        float S = 16.5f, spanK = 1 - 0.18f * p.fold, t = a.time;
        for (int side = 1; side >= -1; side -= 2) {
            g.push();
            g.scale(side, 1, 1);
            g.translate(0.9f, -2.2f, -2.35f);
            g.rotY(p.sweep);
            g.rotZ(p.raise);
            g.rotX(-8);
            float[] sp = {0.3f, 0.52f, 0.72f, 0.9f};
            for (int i = 0; i < sp.length; i++) {
                float s = sp[i];
                float x = S * s * spanK, y = S * (0.26f * sin(Math.PI * s * 0.92) + 0.1f * s), z = -S * 0.09f * sin(Math.PI * s) - 1.3f;
                float len = 2.2f + (i % 2) * 1.8f + s * 1.5f;
                float sw = sin(t * 2.1f + i * 1.3f) * 10;
                g.push();
                g.translate(x, y - 0.2f, z);
                g.rotX(-p.raise * 0.0f);
                g.rotZ(-p.raise + sw);
                g.color(0xFFFFE9A8);
                Geo.tube(g, "white", new float[]{0, 0, 0}, new float[]{0, -len, 0}, 0.05f, 0.05f, 3);
                g.translate(0, -len - 0.85f, 0);
                g.rotY(t * 50 + i * 60);
                g.glow(true).color(i % 2 == 0 ? 0xFFFFD84A : 0xFFFFF0B0);
                XmasGeo.star(g, "xm_gold", 0.95f, 0.32f, 5);
                g.glow(false);
                halo(g, 0, 0, 0, 2.6f, alpha(0.35 + 0.2 * sin(t * 3 + i), 0xFFE07A));
                g.pop();
            }
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }
}
