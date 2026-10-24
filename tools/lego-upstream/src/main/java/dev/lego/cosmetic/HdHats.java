package dev.lego.cosmetic;

import static dev.lego.cosmetic.Cos3Geo.*;

import dev.lego.cosmetic.Cos.A;

/**
 * HD remaster of the classic hats (ids / names unchanged, registered in {@link Models} / {@link Models2}).
 * Head space: head box x/z -4..4, y 0..8, face at z = +4. Rings that go around the head use a rounded square
 * ({@link Cos3Geo#band}) or a squircle (exponent 5, radius >= 4.75) so they never cut the head corners.
 */
final class HdHats {
    private HdHats() { }

    private static float[] p(float x, float y, float z) { return new float[]{x, y, z}; }

    // ================================================================== shared pieces
    /**
     * Hat brim with real thickness: inner edge follows a squircle (radius rIn, exponent nIn), outer edge a circle rOut
     * (scaled by zs in z). Height at angle a / radial fraction f is lift(a, f). Top + bottom + outer rolled edge.
     */
    interface Lift { float at(double a, float f); }

    static void brim(G g, String t, float rIn, float nIn, float rOut, float zs, float th, int seg, int rings, Lift lift) {
        float[][][] top = new float[seg + 1][rings + 1][], bot = new float[seg + 1][rings + 1][];
        for (int i = 0; i <= seg; i++) {
            double a = Math.PI * 2 * i / seg;
            float[] in = Geo.sq(a, rIn, nIn);
            float ox = cos(a) * rOut, oz = sin(a) * rOut * zs;
            for (int j = 0; j <= rings; j++) {
                float f = j / (float) rings;
                float x = in[0] + (ox - in[0]) * f, z = in[1] + (oz - in[1]) * f;
                float y = lift.at(a, f);
                float thj = th * (1 - 0.35f * f);
                top[i][j] = p(x, y + thj / 2, z);
                bot[i][j] = p(x, y - thj / 2, z);
            }
        }
        for (int i = 0; i < seg; i++) {
            float ua = (float) i / seg, ub = (float) (i + 1) / seg;
            for (int j = 0; j < rings; j++) {
                float va = (float) j / rings, vb = (float) (j + 1) / rings;
                g.quad(t, top[i][j + 1], top[i + 1][j + 1], top[i + 1][j], top[i][j], new float[]{ua, vb, ub, vb, ub, va, ua, va});
                g.quad(t, bot[i][j], bot[i + 1][j], bot[i + 1][j + 1], bot[i][j + 1], new float[]{ua, va, ub, va, ub, vb, ua, vb});
            }
            // rounded outer edge (half tube made of 3 quads)
            float[] T0 = top[i][rings], T1 = top[i + 1][rings], B0 = bot[i][rings], B1 = bot[i + 1][rings];
            double a0 = Math.PI * 2 * i / seg, a1 = Math.PI * 2 * (i + 1) / seg;
            float e = th * 0.42f;
            float[] M0 = p((T0[0] + B0[0]) / 2 + cos(a0) * e, (T0[1] + B0[1]) / 2, (T0[2] + B0[2]) / 2 + sin(a0) * e * zs);
            float[] M1 = p((T1[0] + B1[0]) / 2 + cos(a1) * e, (T1[1] + B1[1]) / 2, (T1[2] + B1[2]) / 2 + sin(a1) * e * zs);
            g.quad(t, T0, T1, M1, M0, new float[]{ua, 0.96f, ub, 0.96f, ub, 1, ua, 1});
            g.quad(t, M0, M1, B1, B0, new float[]{ua, 0.96f, ub, 0.96f, ub, 1, ua, 1});
            // inner edge
            g.quad(t, bot[i][0], bot[i + 1][0], top[i + 1][0], top[i][0], new float[]{ua, 0, ub, 0, ub, 0.04f, ua, 0.04f});
        }
    }

    /** Floppy / bent cone (sweep): straight part heights ys with radii r0 + exponent, then a tip curling along dir. */
    static float[][] bentSpine(float yBase, float h, int n, double bendDeg, double sideDeg, float bendStart) {
        float[][] sp = new float[n + 1][];
        float x = 0, y = yBase, z = 0;
        double step = h / n;
        for (int i = 0; i <= n; i++) {
            float f = i / (float) n;
            sp[i] = p(x, y, z);
            double b = f < bendStart ? 0 : Math.toRadians(bendDeg) * Math.pow((f - bendStart) / (1 - bendStart), 1.6);
            double sd = Math.toRadians(sideDeg);
            x += (float) (Math.sin(b) * Math.sin(sd) * step);
            z += (float) (-Math.sin(b) * Math.cos(sd) * step);
            y += (float) (Math.cos(b) * step);
        }
        return sp;
    }

    /** Ear made of a puffy outline with a softer inner part (xy plane, base at the origin, facing +z). */
    static void ear(G g, float[][] outer, float[][] inner, String tex, int outerCol, int innerCol, float depth) {
        int oc = g.color;
        g.color(outerCol);
        HdGeo.puff(g, tex, outer, depth, depth * 0.45f, 2);
        g.color(innerCol);
        g.push();
        g.translate(0, 0, depth * 0.38f);
        HdGeo.puff(g, "hd_velvet", inner, depth * 0.4f, depth * 0.18f, 1);
        g.pop();
        g.color(oc);
    }

    static float[][] earShape(float w, float h, float bulge, int n) {
        // rounded triangle: base from -w/2..w/2 at y=0, tip at (0,h), sides bulging outwards
        float[][] o = new float[n * 2 + 2][];
        int k = 0;
        o[k++] = p2(w / 2, 0);
        for (int i = 1; i <= n; i++) {
            float f = i / (float) (n + 1);
            float x = w / 2 * (1 - f), y = h * f;
            float b = bulge * (float) Math.sin(Math.PI * f);
            o[k++] = p2(x + b * 0.9f, y + b * 0.25f);
        }
        o[k++] = p2(0, h);
        for (int i = n; i >= 1; i--) {
            float f = i / (float) (n + 1);
            float x = -w / 2 * (1 - f), y = h * f;
            float b = bulge * (float) Math.sin(Math.PI * f);
            o[k++] = p2(x - b * 0.9f, y + b * 0.25f);
        }
        return java.util.Arrays.copyOf(o, k);
    }

    private static float[] p2(float x, float y) { return new float[]{x, y}; }

    // ================================================================== Königskrone
    static void crown(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(0, 0, 0);
        float c = 2.6f, r = 2.12f, th = 0.42f;
        float y0 = 7.1f, y1 = 9.2f;
        // velvet cap inside the crown
        g.color(0xFFB0141E);
        Geo.sqDome(g, "hd_velvet", 4.55f, 2.7f, 8.4f, 5, 16, 4, 0, 0, 1, 1);
        g.color(0xFFFFFFFF);
        band(g, "hd_gold", c, r, th, y0, y1, 0, 4);
        // pearl row along the lower rim
        g.color(0xFFFFF4E8);
        for (int i = 0; i < 22; i++) {
            float[] q = Cos3Models.loopPt(c, r + th + 0.12f, Math.PI * 2 * i / 22);
            ball(g, "hd_gloss", q[0], y0 + 0.25f, q[1], 0.24f, 4);
        }
        g.color(0xFFFFE9A8);
        {
            int n = 36;
            float[][] rim = new float[n + 1][];
            for (int i = 0; i <= n; i++) { float[] q = Cos3Models.loopPt(c, r + th * 0.5f, Math.PI * 2 * i / n); rim[i] = p(q[0], y1, q[1]); }
            float[] rr = new float[n + 1];
            java.util.Arrays.fill(rr, th * 0.62f);
            XmasGeo.sweep(g, "hd_gold", rim, rr, rr, null, 4, 4);
        }
        // points (fleurons) with pearls
        for (int i = 0; i < 8; i++) {
            double an = Math.PI * 2 * i / 8 + Math.PI / 8;
            float[] q = Cos3Models.loopPt(c, r + th * 0.5f, an);
            boolean big = i % 2 == 0;
            float h = big ? 3.1f : 2.2f;
            g.push();
            g.translate(q[0], y1 - 0.25f, q[1]);
            g.rotY((float) -Math.toDegrees(an) + 90);
            g.rotX(-6);
            float[][] spike = {{-1.35f, 0}, {1.35f, 0}, {0.9f, h * 0.3f}, {0.42f, h * 0.62f}, {0.12f, h}, {-0.12f, h}, {-0.42f, h * 0.62f}, {-0.9f, h * 0.3f}};
            HdGeo.puff(g, "hd_gold", spike, 0.42f, 0.16f, 1);
            g.color(0xFFFFF6EE);
            ball(g, "hd_gloss", 0, h + 0.35f, 0, big ? 0.45f : 0.36f, 6);
            // little gem in the point
            g.glow(true).color(big ? 0xFFE8203A : 0xFF2A7AFF);
            g.push();
            g.translate(0, h * 0.34f, 0.18f);
            g.rotX(90);
            gem(g, "hd_gem", 0.36f, 0.26f, 0.1f, 6);
            g.pop();
            g.glow(false).color(0xFFFFFFFF);
            g.pop();
        }
        // big gems set in the band
        int[] gc = {0xFFE8203A, 0xFF1EB85A, 0xFF2A7AFF, 0xFFB04AFF};
        for (int i = 0; i < 8; i++) {
            double an = Math.PI * 2 * i / 8;
            float[] q = Cos3Models.loopPt(c, r + th + 0.02f, an);
            g.push();
            g.translate(q[0], (y0 + y1) / 2 + 0.05f, q[1]);
            g.rotY((float) -Math.toDegrees(an) + 90);
            g.color(0xFFFFE9A8);
            g.push();
            g.rotX(90);
            g.torus("hd_gold", 0.55f, 0.13f, 8, 3, 0, 0, 1, 1);
            g.pop();
            g.glow(true).color(gc[i % 4]);
            g.rotX(90);
            gem(g, "hd_gem", i % 2 == 0 ? 0.52f : 0.4f, 0.4f, 0.12f, 6);
            g.glow(false);
            g.pop();
        }
        // orb + cross on top
        g.color(0xFFFFFFFF);
        ball(g, "hd_gold", 0, 11.35f, 0, 0.62f, 10);
        g.color(0xFFFFE9A8);
        cbox(g, "hd_gold", 0, 12.55f, 0, 0.36f, 1.7f, 0.36f, 0.08f);
        cbox(g, "hd_gold", 0, 12.75f, 0, 1.2f, 0.36f, 0.36f, 0.08f);
        g.color(0xFFFFFFFF);
        // sparkles
        for (int i = 0; i < 3; i++) {
            float tw = Math.max(0, sin(t * 2.3f + i * 2.1f));
            double an = i * 2.1 + 0.4;
            float[] q = Cos3Models.loopPt(c, r + th + 0.4f, an);
            if (tw > 0.05f) Cos3Models.sparkle(g, q[0], 10.2f + i * 0.4f, q[1], 0.45f * tw, alpha(tw, 0xFFFFFF));
        }
        g.pop();
    }

    // ================================================================== Heiligenschein
    static void halo(G g, A a) {
        float t = a.time;
        float bob = sin(t * 2.2f) * 0.4f;
        g.push();
        g.translate(0, 11.6f + bob, 0);
        g.rotX(16 + sin(t * 1.1f) * 3);
        g.push();
        g.rotY(t * 30);
        g.glow(true).color(0xFFFFE9A0);
        g.torus("hd_gold", 4.55f, 0.46f, 40, 10, 0, 0, 4, 1);
        g.color(0xFFFFFFFF);
        g.torus("white", 4.55f, 0.16f, 40, 4, 0, 0, 1, 1);
        g.glow(false);
        g.pop();
        // glow shell
        float pulse = 0.7f + 0.3f * sin(t * 3);
        g.glow(true).color(alpha(0.35 * pulse, 0xFFE07A));
        g.torus("hd_softglow", 4.55f, 1.35f, 32, 6, 0.1f, 0.1f, 0.9f, 0.9f);
        g.glow(false);
        // light motes orbiting on the ring
        for (int i = 0; i < 6; i++) {
            double an = Math.PI * 2 * i / 6 + t * 1.2;
            float tw = 0.5f + 0.5f * sin(t * 4 + i * 1.9f);
            Cos3Models.sparkle(g, cos(an) * 4.55f, 0.1f, sin(an) * 4.55f, 0.35f + tw * 0.35f, 0xFFFFFFFF);
            Cos3Geo.halo(g, cos(an) * 4.55f, 0.1f, sin(an) * 4.55f, 1.6f + tw, alpha(0.4 * tw, 0xFFF0B0));
        }
        g.pop();
    }

    // ================================================================== Zylinder
    static void tophat(G g, A a) {
        g.push();
        g.translate(0, 8.05f, 0);
        g.rotZ(-4);
        g.color(0xFF1C1C22);
        brim(g, "hd_felt", 4.3f, 2, 7.0f, 0.92f, 0.36f, 32, 3, (an, f) -> 0.18f + f * f * 0.9f * (float) Math.pow(Math.abs(Math.cos(an)), 2));
        // crown with a slight waist, flat top with rounded edge
        lathe(g, "hd_felt", new float[]{4.35f, 4.28f, 4.18f, 4.24f, 4.42f, 4.42f, 4.2f, 3.4f, 0},
                new float[]{0.2f, 2.0f, 4.2f, 6.4f, 8.2f, 8.45f, 8.62f, 8.7f, 8.7f}, 28);
        // satin band + bow on the side
        g.color(0xFFB8141C);
        lathe(g, "hd_satin", new float[]{4.37f, 4.36f, 4.32f, 4.28f}, new float[]{0.3f, 0.9f, 1.6f, 2.1f}, 28, 0, 0, 1, 1);
        g.push();
        g.translate(4.35f, 1.2f, 0.8f);
        g.rotY(90);
        XmasGeo.bow(g, 0xFFB8141C, 0.65f);
        g.pop();
        g.pop();
    }

    // ================================================================== Weihnachtsmütze
    static void santa(G g, A a) {
        float t = a.time, mv = a.preview ? 0 : clamp(a.move, 0, 1);
        float sway = sin(t * 1.6f) * 0.2f + mv * 0.25f;
        float[] ys = {5.4f, 7.0f, 8.6f, 9.7f};
        float[] r0 = {4.72f, 4.8f, 4.55f, 3.7f};
        float[] n0 = {5.5f, 5.5f, 4.2f, 2.8f};
        int floppy = 10, m = ys.length + floppy;
        float[][] sp = new float[m][];
        float[] rx = new float[m], nn = new float[m];
        for (int i = 0; i < ys.length; i++) { sp[i] = p(0, ys[i], 0); rx[i] = r0[i]; nn[i] = n0[i]; }
        float[] cur = sp[ys.length - 1].clone();
        for (int i = 0; i < floppy; i++) {
            float f = (i + 1) / (float) floppy;
            double ang = f * f * 2.5 + 0.25;
            double side = 1.35 + sway * f;
            float seg = 1.2f - 0.35f * f;
            cur = new float[]{cur[0] + (float) (Math.sin(side) * Math.sin(ang)) * seg, cur[1] + (float) Math.cos(ang) * seg, cur[2] - (float) (Math.cos(side) * Math.sin(ang)) * seg};
            int j = ys.length + i;
            sp[j] = cur;
            rx[j] = Math.max(0.1f, 3.4f * (1 - f) + 0.1f);
            nn[j] = 2;
        }
        g.color(0xFFC8141E);
        XmasGeo.sweep(g, "hd_velvet", sp, rx, rx, nn, 22, 3);
        // fluffy cuff: a thick soft roll around the head
        g.color(0xFFF8F8FA);
        {
            int n = 40;
            float[][] roll = new float[n + 1][];
            float[] rr = new float[n + 1], rz = new float[n + 1];
            for (int i = 0; i <= n; i++) {
                double an = Math.PI * 2 * i / n;
                float[] q = Cos3Models.loopPt(2.6f, 2.45f, an);
                roll[i] = p(q[0], 5.7f + 0.08f * sin(an * 7), q[1]);
                rr[i] = 0.95f + 0.07f * sin(an * 11);
                rz[i] = 1.25f + 0.07f * cos(an * 9);
            }
            XmasGeo.sweep(g, "hd_fluff", roll, rr, rz, null, 10, 8);
        }
        // pom pom
        float[] e = sp[m - 1];
        g.push();
        g.translate(e[0], e[1] - 0.3f, e[2]);
        g.rotZ(sin(t * 3) * 10);
        ball(g, "hd_fluff", 0, -0.9f, 0, 1.45f, 12);
        for (int i = 0; i < 8; i++) {
            double an = i * 2.4;
            ball(g, "hd_fluff", cos(an) * 1.0f, -0.9f + sin(i * 1.3) * 0.9f, sin(an) * 1.0f, 0.7f, 6);
        }
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Hexenhut
    static void witch(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(0, 7.35f, 0);
        g.rotZ(-5);
        g.color(0xFF3A1A5A);
        brim(g, "hd_felt", 4.75f, 5, 8.6f, 1f, 0.34f, 40, 4, (an, f) -> 0.2f + f * 0.25f + f * f * 0.55f * sin(an * 3 + 1) - f * f * 0.4f * (float) Math.max(0, sin(an)));
        // crooked cone
        float[][] sp = bentSpine(0.3f, 10.5f, 14, 70, 200, 0.45f);
        int m = sp.length;
        float[] rx = new float[m], nn = new float[m];
        for (int i = 0; i < m; i++) {
            float f = i / (float) (m - 1);
            rx[i] = Math.max(0.06f, 4.72f * (float) Math.pow(1 - f, 1.25) + 0.05f);
            nn[i] = f < 0.1f ? 5 : 2;
            if (i > 0 && i < m - 1) { sp[i][0] += sin(f * 9) * 0.12f; }
        }
        XmasGeo.sweep(g, "hd_felt", sp, rx, rx, nn, 22, 3);
        // band + buckle
        g.color(0xFFFFFFFF);
        Geo.sqTube(g, "hd_witchband", 4.82f, 4.62f, 0.35f, 1.95f, 5, 24, 0, 0, 3, 1);
        g.push();
        g.translate(0, 1.15f, 4.86f);
        g.color(0xFFFFD86A);
        HdGeo.loop(g, "hd_gold", HdGeo.rrect(1.15f, 0.9f, 0.3f, 3), 0.22f, 0.2f, 3, 6);
        cbox(g, "hd_gold", 0, 0, 0.05f, 0.25f, 1.5f, 0.25f, 0.05f);
        g.pop();
        // tiny star charm dangling from the tip
        float[] e = sp[m - 1];
        g.push();
        g.translate(e[0], e[1], e[2]);
        g.rotZ(sin(t * 2.4f) * 14);
        g.color(0xFFFFD86A);
        Geo.tube(g, "hd_gold", p(0, 0, 0), p(0, -1.6f, 0), 0.05f, 0.05f, 4);
        g.translate(0, -2.2f, 0);
        g.rotY(t * 60);
        g.glow(true);
        XmasGeo.star(g, "hd_gold", 0.7f, 0.25f, 5);
        g.glow(false);
        g.pop();
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Katzenohren
    static void catEars(G g, A a) {
        float t = a.time;
        float twitch = (float) Math.max(0, Math.sin(t * 3.1f) - 0.85f) * 55;
        float[][] out = earShape(4.6f, 4.9f, 0.4f, 5), in = earShape(2.9f, 3.3f, 0.22f, 4);
        for (int s = 1; s >= -1; s -= 2) {
            g.push();
            g.scale(s, 1, 1);
            g.translate(2.25f, 7.75f, 0.2f);
            g.rotZ(-12 - (s > 0 ? twitch : 0));
            g.rotY(-12);
            ear(g, out, shift(in, 0, 0.4f), "hd_fur", 0xFF5A5262, 0xFFFF9AB6, 1.05f);
            // fluffy tufts inside the ear
            g.color(0xFFF4EEF4);
            for (int i = 0; i < 3; i++) Geo.ellipsoid(g, "hd_fur", -0.35f + i * 0.35f, 0.75f + i * 0.25f, 0.62f, 0.22f, 0.8f, 0.15f, 5, 3);
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    static float[][] shift(float[][] o, float dx, float dy) {
        float[][] r = new float[o.length][];
        for (int i = 0; i < o.length; i++) r[i] = new float[]{o[i][0] + dx, o[i][1] + dy};
        return r;
    }

    // ================================================================== Hasenohren
    static void bunnyEars(G g, A a) {
        float t = a.time, mv = a.preview ? 0 : a.move;
        for (int s = 1; s >= -1; s -= 2) {
            g.push();
            g.scale(s, 1, 1);
            g.translate(1.85f, 7.6f, 0.2f);
            g.rotZ(-9);
            float flop = (s > 0 ? 55 : 8) + sin(t * 2.2f + s) * 6 + mv * 14;
            float[][] sp = bentSpine(0, 9.4f, 10, flop, s > 0 ? 180 : 170, 0.55f);
            int m = sp.length;
            float[] rx = new float[m], rz = new float[m];
            for (int i = 0; i < m; i++) {
                float f = i / (float) (m - 1);
                float w = (float) Math.sin(Math.PI * Math.min(1, f * 0.92f + 0.08f));
                rx[i] = Math.max(0.08f, 0.55f + 1.05f * w);
                rz[i] = Math.max(0.06f, 0.5f * w + 0.18f);
            }
            g.color(0xFFF6F2F4);
            XmasGeo.sweep(g, "hd_fur", sp, rx, rz, null, 14, 2);
            // pink inner ear (thinner, pushed forward)
            float[][] sp2 = new float[m][];
            float[] rx2 = new float[m], rz2 = new float[m];
            for (int i = 0; i < m; i++) {
                float f = i / (float) (m - 1);
                sp2[i] = new float[]{sp[i][0], sp[i][1], sp[i][2] + rz[i] * 0.72f};
                rx2[i] = Math.max(0.05f, rx[i] * 0.55f * (float) Math.sin(Math.PI * Math.min(1, 0.1f + f * 0.9f)));
                rz2[i] = 0.12f;
            }
            // shift the inner along the frame normal of the ear: approximate by following the ear's local "front"
            g.color(0xFFFF9EBC);
            XmasGeo.sweep(g, "hd_velvet", frontOf(sp, rz, 0.72f), rx2, rz2, null, 10, 1);
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    /** Offsets a bent spine towards its front side (for inner ear surfaces). */
    static float[][] frontOf(float[][] sp, float[] rz, float k) {
        int m = sp.length;
        float[][] out = new float[m][];
        for (int i = 0; i < m; i++) {
            float[] a0 = sp[Math.max(0, i - 1)], b0 = sp[Math.min(m - 1, i + 1)];
            float tx = b0[0] - a0[0], ty = b0[1] - a0[1], tz = b0[2] - a0[2];
            float l = (float) Math.sqrt(tx * tx + ty * ty + tz * tz);
            if (l < 1e-5f) { out[i] = sp[i]; continue; }
            tx /= l; ty /= l; tz /= l;
            // front = +z projected perpendicular to the tangent
            float d = tz;
            float fx = -d * tx, fy = -d * ty, fz = 1 - d * tz;
            float fl = (float) Math.sqrt(fx * fx + fy * fy + fz * fz);
            if (fl < 1e-4f) { fx = 0; fy = 1; fz = 0; fl = 1; }
            out[i] = new float[]{sp[i][0] + fx / fl * rz[i] * k, sp[i][1] + fy / fl * rz[i] * k, sp[i][2] + fz / fl * rz[i] * k};
        }
        return out;
    }

    // ================================================================== Teufelshörner
    static void horns(G g, A a) {
        float t = a.time;
        for (int s = 1; s >= -1; s -= 2) {
            g.push();
            g.scale(s, 1, 1);
            g.translate(2.5f, 7.75f, 1.0f);
            g.rotZ(-26);
            g.rotY(-8);
            g.color(0xFFFFFFFF);
            HdGeo.horn(g, "hd_horn", 1.15f, 6.4f, -95, 30, 16, 12, 7);
            g.pop();
        }
    }

    // ================================================================== Kopfhörer
    static void headphones(G g, A a) {
        float t = a.time;
        float beat = (float) Math.pow(Math.max(0, Math.sin(t * 7.5)), 6);
        // band (flattened tube over the head) + padded underside
        int n = 18;
        float[][] band = new float[n + 1][], pad = new float[n - 5][];
        for (int i = 0; i <= n; i++) {
            double an = Math.PI * i / n;
            float[] q = Geo.sq(an, 1, 3.2f);
            band[i] = p(q[0] * 5.4f, 3.6f + q[1] * 5.75f, 0);
        }
        float[] rx = new float[n + 1], rz = new float[n + 1];
        java.util.Arrays.fill(rx, 0.38f);
        java.util.Arrays.fill(rz, 0.95f);
        g.color(0xFF22242C);
        XmasGeo.sweep(g, "hd_rubber", band, rx, rz, null, 10, 1);
        for (int i = 3; i <= n - 3; i++) {
            double an = Math.PI * i / n;
            float[] q = Geo.sq(an, 1, 3.2f);
            pad[i - 3] = p(q[0] * 4.95f, 3.6f + q[1] * 5.3f, 0);
        }
        float[] pr = new float[pad.length], pz = new float[pad.length];
        java.util.Arrays.fill(pr, 0.34f);
        java.util.Arrays.fill(pz, 0.75f);
        g.color(0xFF3A3C46);
        XmasGeo.sweep(g, "hd_leather", pad, pr, pz, null, 10, 2);
        for (int s = 1; s >= -1; s -= 2) {
            g.push();
            g.translate(s * 4.05f, 3.3f, 0);
            g.rotZ(s * -90);
            // slider arm
            g.color(0xFFB8BEC8);
            cbox(g, "hd_chrome", 0, 1.2f, 0, 0.5f, 0.3f, 0.3f, 0.08f);
            // ear cushion (leather torus) + cup shell
            g.color(0xFF2E3038);
            g.push();
            g.translate(0, 0.35f, 0);
            g.torus("hd_leather", 2.05f, 0.5f, 22, 8, 0, 0, 3, 1);
            g.pop();
            g.color(0xFF1A1B21);
            lathe(g, "hd_plastic", new float[]{2.6f, 2.75f, 2.72f, 2.5f, 2.0f, 0}, new float[]{0.6f, 0.9f, 1.6f, 2.05f, 2.25f, 2.3f}, 24);
            // chrome ring + glowing led ring pulsing to the beat
            g.color(0xFFD8DEE8);
            g.push();
            g.translate(0, 1.95f, 0);
            g.torus("hd_chrome", 2.35f, 0.14f, 24, 4, 0, 0, 1, 1);
            g.pop();
            g.glow(true).color(0xFF000000 | Capes.mix(0x14A848, 0x7CFFA8, beat));
            g.push();
            g.translate(0, 2.28f, 0);
            g.torus("white", 1.35f, 0.13f, 20, 4, 0, 0, 1, 1);
            g.pop();
            g.glow(false);
            Cos3Geo.halo(g, 0, 2.5f, 0, 3.2f + beat * 1.2f, alpha(0.2 + 0.25 * beat, 0x3AFF7A));
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Fuchsohren
    static void foxEars(G g, A a) {
        float twitch = (float) Math.max(0, Math.sin(a.time * 2.7f) - 0.8f) * 60;
        float[][] out = earShape(4.3f, 5.3f, 0.3f, 6), in = earShape(2.6f, 3.4f, 0.15f, 4);
        for (int s = 1; s >= -1; s -= 2) {
            g.push();
            g.scale(s, 1, 1);
            g.translate(2.35f, 7.7f, 0.1f);
            g.rotZ(-15 - (s > 0 ? twitch : 0));
            g.rotY(-14);
            g.rotX(-6);
            g.color(0xFFFFFFFF);
            HdGeo.puff(g, "hd_foxear", out, 1.05f, 0.45f, 2, new float[]{0, 0, 1, 1}, new float[]{0.4f, 0.6f, 0.6f, 0.7f});
            g.color(0xFFF6EEE6);
            g.push();
            g.translate(0, 0.4f, 0.45f);
            HdGeo.puff(g, "hd_fur", in, 0.45f, 0.2f, 1);
            for (int i = 0; i < 4; i++) Geo.ellipsoid(g, "hd_fur", -0.5f + i * 0.33f, 0.9f + (i % 2) * 0.3f, 0.28f, 0.2f, 0.95f, 0.14f, 5, 3);
            g.pop();
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Cowboyhut
    static void cowboy(G g, A a) {
        g.push();
        g.translate(0, 6.9f, 0);
        g.rotZ(-4);
        g.color(0xFF9A6236);
        brim(g, "hd_leather", 4.75f, 5, 8.7f, 1.08f, 0.38f, 36, 4, (an, f) -> {
            float side = (float) Math.pow(Math.abs(Math.cos(an)), 1.6);
            float front = (float) Math.max(0, Math.sin(an));
            return 0.45f + f * f * (2.6f * side - 0.7f * front * (1 - side) - 0.3f * (float) Math.max(0, -Math.sin(an)) * (1 - side));
        });
        // crown: squircle tube + pinched top (dent + front pinches)
        int seg = 26, rows = 7;
        float[][][] ring = new float[rows + 1][seg + 1][];
        for (int j = 0; j <= rows; j++) {
            float f = j / (float) rows;
            for (int i = 0; i <= seg; i++) {
                double an = Math.PI * 2 * i / seg;
                float r = 4.72f - 0.55f * f - (f > 0.55f ? 1.6f * (float) Math.pow((f - 0.55f) / 0.45f, 2) : 0);
                float pinch = f > 0.5f ? 0.55f * (float) Math.pow(Math.max(0, Math.sin(an)), 6) * (f - 0.5f) * 2 : 0;
                float[] q = Geo.sq(an, r - pinch, 5 - f * 2);
                float y = 0.3f + 5.4f * (float) Math.sin(f * Math.PI / 2 * 0.95);
                float dent = f > 0.85f ? -0.9f * (float) Math.pow(Math.max(0, Math.cos(an) * Math.cos(an)), 0.5) * (f - 0.85f) / 0.15f : 0;
                ring[j][i] = p(q[0], y + dent, q[1]);
            }
        }
        for (int j = 0; j < rows; j++)
            for (int i = 0; i < seg; i++)
                g.quad("hd_leather", ring[j + 1][i], ring[j + 1][i + 1], ring[j][i + 1], ring[j][i],
                        new float[]{i / (float) seg, 1 - (j + 1f) / rows, (i + 1f) / seg, 1 - (j + 1f) / rows, (i + 1f) / seg, 1 - (float) j / rows, i / (float) seg, 1 - (float) j / rows});
        // top: a crease (two slanted halves meeting in the dent)
        float[] cTop = p(0, ring[rows][0][1] - 0.6f, 0);
        for (int i = 0; i < seg; i++) g.quad("hd_leather", cTop, ring[rows][i + 1], ring[rows][i], ring[rows][i], new float[]{0.5f, 0.5f, 0.5f, 0, 0.5f, 0, 0.5f, 0});
        // band with stitching + silver concho
        g.color(0xFF3A2414);
        Geo.sqTube(g, "hd_leather", 4.79f, 4.64f, 0.55f, 1.55f, 5, 26, 0, 0, 2, 1);
        g.color(0xFFFFFFFF);
        g.push();
        g.translate(0, 1.05f, 4.82f);
        g.color(0xFFD8DEE8);
        g.rotX(90);
        lathe(g, "hd_silver", new float[]{0, 0.55f, 0.72f, 0.7f, 0}, new float[]{0.36f, 0.3f, 0.12f, 0, 0}, 14);
        g.pop();
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Bommelmütze
    static void beanie(G g, A a) {
        float t = a.time, mv = a.preview ? 0 : a.move;
        g.push();
        g.translate(0, 5.0f, 0);
        g.color(0xFF2E62D8);
        Geo.sqDome(g, "hd_knit", 4.72f, 4.3f, 1.9f, 5, 26, 6, 0, 0, 3, 1);
        Geo.sqTube(g, "hd_knit", 4.72f, 4.72f, 0.3f, 1.9f, 5, 26, 0, 0, 3, 0.3f);
        // stripe
        g.color(0xFFF2F2F6);
        Geo.sqTube(g, "hd_knit", 4.76f, 4.62f, 3.0f, 3.75f, 4.6f, 26, 0, 0, 3, 0.2f);
        // folded cuff (thick rounded rim)
        g.color(0xFF2452B8);
        for (int k = 0; k < 3; k++) {
            float rr = 5.02f - Math.abs(k - 1) * 0.12f;
            Geo.sqTube(g, "hd_knit", rr, k == 2 ? 4.8f : 5.02f - Math.abs(k) * 0.12f, -0.25f + k * 0.8f, 0.55f + k * 0.8f, 5, 26, 0, 0, 4, 0.3f);
        }
        Geo.sqTube(g, "hd_knit", 4.8f, 4.8f, -0.25f, -0.25f + 0.001f, 5, 26, 0, 0, 1, 1);
        // pom pom (bounces)
        float wob = sin(t * 3) * 6 + mv * 10;
        g.translate(0, 6.0f, 0);
        g.rotZ(wob);
        g.rotX(sin(t * 2.2) * 5);
        g.translate(0, 1.15f, 0);
        g.color(0xFFF6F6FA);
        ball(g, "hd_fluff", 0, 0, 0, 1.75f, 14);
        for (int i = 0; i < 10; i++) {
            double an = i * 2.4, el = (i % 3 - 1) * 0.7;
            ball(g, "hd_fluff", cos(an) * cos(el) * 1.25f, sin(el) * 1.25f + 0.2f, sin(an) * cos(el) * 1.25f, 0.75f, 6);
        }
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Blumenkranz
    static void flower(G g, int petals, float r, int col, int center, float cup) {
        int oc = g.color;
        float[][] petal = {{0, 0}, {r * 0.4f, r * 0.45f}, {r * 0.28f, r * 0.9f}, {0, r}, {-r * 0.28f, r * 0.9f}, {-r * 0.4f, r * 0.45f}};
        g.color(col);
        for (int i = 0; i < petals; i++) {
            g.push();
            g.rotY(360f / petals * i);
            g.rotX(-90 + cup);
            HdGeo.puff(g, "hd_petal", petal, 0.16f, 0.07f, 1, new float[]{0, 0, 1, 1}, new float[]{0.4f, 0.4f, 0.6f, 0.6f});
            g.pop();
        }
        g.color(center);
        ball(g, "hd_gem", 0, 0.1f, 0, r * 0.28f, 6);
        g.color(oc);
    }

    static void leaf(G g, float len, int col) {
        float w = len * 0.42f;
        float[][] o = {{0, 0}, {len * 0.25f, w * 0.8f}, {len * 0.55f, w}, {len * 0.82f, w * 0.6f}, {len, 0}, {len * 0.82f, -w * 0.6f}, {len * 0.55f, -w}, {len * 0.25f, -w * 0.8f}};
        int oc = g.color;
        g.color(col);
        HdGeo.puff(g, "hd_leafw", o, 0.14f, 0.06f, 1, new float[]{0, 0, 1, 1}, new float[]{0.4f, 0.4f, 0.6f, 0.6f});
        g.color(oc);
    }

    static void flowerCrown(G g, A a) {
        float t = a.time;
        int n = 32;
        float[][] vine = new float[n + 1][];
        for (int i = 0; i <= n; i++) {
            double an = Math.PI * 2 * i / n;
            float[] q = Cos3Models.loopPt(2.0f, 3.25f, an);
            vine[i] = p(q[0], 8.0f + 0.18f * sin(an * 5), q[1]);
        }
        float[] vr = new float[n + 1];
        java.util.Arrays.fill(vr, 0.34f);
        g.color(0xFFFFFFFF);
        XmasGeo.sweep(g, "hd_vine", vine, vr, vr, null, 6, 6);
        int[] cols = {0xFFFF8AB8, 0xFFFFFFFF, 0xFFFFD84A, 0xFF8AB8FF, 0xFFE8A0FF};
        int flowers = 7;
        for (int i = 0; i < flowers; i++) {
            double an = Math.PI * 2 * i / flowers + 0.3;
            float[] q = Cos3Models.loopPt(2.0f, 3.45f, an);
            g.push();
            g.translate(q[0], 8.35f, q[1]);
            g.rotY((float) -Math.toDegrees(an) + 90);
            g.rotX(55 + sin(t * 1.8f + i) * 4);
            float s = 1.5f + 0.35f * ((i * 5) % 3) / 2f;
            flower(g, i % 3 == 1 ? 6 : 5, s, cols[i % cols.length], i % 5 == 2 ? 0xFFFF8A2A : 0xFFFFD02A, 22);
            g.pop();
            // two leaves beside each flower
            for (int k = -1; k <= 1; k += 2) {
                if (k > 0 || i % 2 == 1) continue;
                double an2 = an + k * 0.24;
                float[] q2 = Cos3Models.loopPt(2.0f, 3.3f, an2);
                g.push();
                g.translate(q2[0], 8.1f, q2[1]);
                g.rotY((float) -Math.toDegrees(an2) + 90 + (k > 0 ? 180 : 0) + 90);
                g.rotZ(20);
                g.rotX(40);
                leaf(g, 1.5f, 0xFF4AA842);
                g.pop();
            }
        }
        // a butterfly visiting now and then
        float ph = fract(t * 0.12f);
        if (ph < 0.6f) {
            double an = ph * 4;
            g.push();
            g.translate(cos(an) * 6.5f, 11 + sin(t * 3) * 0.6f, sin(an) * 6.5f);
            g.rotY((float) -Math.toDegrees(an));
            float flap = 30 + 45 * Math.abs(sin(t * 14));
            for (int s = -1; s <= 1; s += 2) {
                g.push();
                g.rotZ(s * flap);
                g.color(0xFFFFB02A);
                g.push();
                g.translate(s * 0.7f, 0, 0);
                g.rotX(90);
                HdGeo.puff(g, "hd_petal", HdGeo.circle(0.7f, 0.5f, 10), 0.06f, 0.02f, 1);
                g.pop();
                g.pop();
            }
            g.color(0xFF2A1A10);
            Geo.ellipsoid(g, "hd_plastic", 0, 0, 0, 0.12f, 0.12f, 0.55f, 5, 3);
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Wikingerhelm
    static void viking(G g, A a) {
        g.push();
        g.translate(0, 6.1f, 0);
        g.color(0xFFB8BEC8);
        Geo.sqDome(g, "hd_steel", 4.82f, 4.3f, 0.8f, 5, 26, 6, 0, 0, 1, 1);
        Geo.sqTube(g, "hd_steel", 4.82f, 4.82f, -0.2f, 0.8f, 5, 26, 0, 0, 1, 1);
        // bronze band + crest strips
        g.color(0xFFD89A4A);
        Geo.sqTube(g, "hd_gold", 5.0f, 5.0f, -0.55f, 0.95f, 5, 26, 0, 0, 1, 1);
        Geo.sqTube(g, "hd_gold", 4.86f, 5.0f, -0.55f, -0.54f, 5, 26, 0, 0, 1, 1);
        for (int k = 0; k < 2; k++) {
            g.push();
            g.rotY(k * 90);
            int n = 12;
            float[][] sp = new float[n + 1][];
            for (int i = 0; i <= n; i++) {
                double an = Math.PI * i / n;
                float[] q = Geo.sq(an, 1, 2.6f);
                sp[i] = p(q[0] * 4.95f, 0.8f + q[1] * 4.42f, 0);
            }
            float[] r = new float[n + 1], rz = new float[n + 1];
            java.util.Arrays.fill(r, 0.18f);
            java.util.Arrays.fill(rz, 0.55f);
            XmasGeo.sweep(g, "hd_gold", sp, r, rz, null, 6, 1);
            g.pop();
        }
        // rivets
        g.color(0xFFE8C080);
        for (int i = 0; i < 16; i++) {
            float[] q = Geo.sq(Math.PI * 2 * i / 16 + 0.1, 5.08f, 5);
            ball(g, "hd_gold", q[0], 0.2f, q[1], 0.2f, 6);
        }
        // nose guard
        g.color(0xFFD89A4A);
        float[][] nose = {{-0.55f, 0.4f}, {0.55f, 0.4f}, {0.42f, -2.2f}, {0, -2.6f}, {-0.42f, -2.2f}};
        g.push();
        g.translate(0, 0, 5.05f);
        HdGeo.puff(g, "hd_gold", nose, 0.4f, 0.15f, 2);
        g.pop();
        // horns
        g.color(0xFFFFFFFF);
        for (int s = 1; s >= -1; s -= 2) {
            g.push();
            g.scale(s, 1, 1);
            g.translate(4.8f, 1.9f, 0.2f);
            g.rotZ(-78);
            g.rotY(-22);
            HdGeo.horn(g, "hd_ivory", 1.2f, 7.0f, -62, -20, 14, 12, 5);
            g.color(0xFFD89A4A);
            g.push();
            g.translate(0, 0.35f, 0);
            g.torus("hd_gold", 1.18f, 0.22f, 14, 5, 0, 0, 1, 1);
            g.pop();
            g.color(0xFFFFFFFF);
            g.pop();
        }
        g.pop();
    }

    // ================================================================== Propellermütze
    static void propeller(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(0, 6.2f, 0);
        int[] segCol = {0xFFE3000B, 0xFFFFCD03, 0xFF006CB7, 0xFF00A03A, 0xFFE3000B, 0xFFFFCD03};
        for (int k = 0; k < 6; k++) {
            g.color(segCol[k]);
            domeSector(g, "hd_plastic", 4.86f, 3.5f, 5, k * Math.PI / 3, (k + 1) * Math.PI / 3, 6, 6);
        }
        g.color(0xFFF2F2F2);
        Geo.sqTube(g, "hd_plastic", 4.9f, 4.9f, -0.1f, 0.5f, 5, 24, 0, 0, 1, 1);
        // visor (curved, with thickness)
        g.color(0xFF1E5AC8);
        g.push();
        g.translate(0, 0.25f, 0);
        int vs = 12;
        for (int i = 0; i < vs; i++) {
            double a0 = Math.PI * (0.18 + 0.64 * i / vs), a1 = Math.PI * (0.18 + 0.64 * (i + 1) / vs);
            float[] in0 = Geo.sq(a0, 4.9f, 5), in1 = Geo.sq(a1, 4.9f, 5);
            float r0 = 8.2f - 2.2f * (float) Math.pow(Math.abs(Math.cos(a0)), 2), r1 = 8.2f - 2.2f * (float) Math.pow(Math.abs(Math.cos(a1)), 2);
            float[] A = p(in0[0], 0, in0[1]), B = p(in1[0], 0, in1[1]), C = p(cos(a1) * r1, -0.7f, sin(a1) * r1), D = p(cos(a0) * r0, -0.7f, sin(a0) * r0);
            float d = 0.18f;
            g.quad("hd_plastic", up(D, d), up(C, d), up(B, d), up(A, d), new float[]{0, 1, 1, 1, 1, 0, 0, 0});
            g.quad("hd_plastic", A, B, C, D, new float[]{0, 0, 1, 0, 1, 1, 0, 1});
            g.quad("hd_plastic", D, C, up(C, d), up(D, d), new float[]{0, 0, 1, 0, 1, 1, 0, 1});
        }
        g.pop();
        // hub + spinning blades
        g.color(0xFFFFCD03);
        g.translate(0, 3.45f, 0);
        lathe(g, "hd_plastic", new float[]{0.7f, 0.42f, 0.35f, 0.35f}, new float[]{0, 0.3f, 0.5f, 1.4f}, 10);
        g.translate(0, 1.4f, 0);
        g.color(0xFFE3000B);
        ball(g, "hd_gloss", 0, 0.1f, 0, 0.62f, 10);
        g.rotY(t * (a.preview ? 260 : 720 + a.move * 900));
        int[] cols = {0xFFE3000B, 0xFF006CB7, 0xFF00A03A};
        float[][] blade = {{0.45f, -0.35f}, {2.6f, -0.9f}, {4.7f, -0.85f}, {5.4f, -0.3f}, {5.4f, 0.35f}, {4.7f, 0.85f}, {2.6f, 0.75f}, {0.45f, 0.35f}};
        for (int b = 0; b < 3; b++) {
            g.push();
            g.rotY(b * 120);
            g.rotX(90 - 16);
            g.color(cols[b]);
            HdGeo.puff(g, "hd_plastic", blade, 0.2f, 0.08f, 1);
            g.pop();
        }
        g.pop();
        g.color(0xFFFFFFFF);
    }

    private static float[] up(float[] q, float d) { return p(q[0], q[1] + d, q[2]); }

    /** Sector of a squircle dome (angles a0..a1). */
    static void domeSector(G g, String t, float r, float h, float n, double a0, double a1, int seg, int rings) {
        for (int j = 0; j < rings; j++) {
            double p0 = Math.PI / 2 * j / rings, p1 = Math.PI / 2 * (j + 1) / rings;
            float ra = r * cos(p0), rb = r * cos(p1);
            float ya = h * sin(p0), yb = h * sin(p1);
            float na = Geo.lerp(n, 2, (float) j / rings), nb = Geo.lerp(n, 2, (float) (j + 1) / rings);
            for (int i = 0; i < seg; i++) {
                double b0 = a0 + (a1 - a0) * i / seg, b1 = a0 + (a1 - a0) * (i + 1) / seg;
                float[] q00 = Geo.sq(b0, ra, na), q01 = Geo.sq(b1, ra, na), q10 = Geo.sq(b0, rb, nb), q11 = Geo.sq(b1, rb, nb);
                g.quad(t, p(q10[0], yb, q10[1]), p(q11[0], yb, q11[1]), p(q01[0], ya, q01[1]), p(q00[0], ya, q00[1]), new float[]{0, 0, 1, 0, 1, 1, 0, 1});
            }
        }
    }

    // ================================================================== Kochmütze
    static void chef(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(0, 6.4f, 0);
        // pleated tube
        int seg = 64, rows = 3;
        for (int j = 0; j < rows; j++) {
            float y0 = 0.8f + 4.6f * j / rows, y1 = 0.8f + 4.6f * (j + 1) / rows;
            for (int i = 0; i < seg; i++) {
                double a0 = Math.PI * 2 * i / seg, a1 = Math.PI * 2 * (i + 1) / seg;
                float w0 = 0.32f * (float) Math.abs(Math.sin(a0 * 8)), w1 = 0.32f * (float) Math.abs(Math.sin(a1 * 8));
                float g0 = 0.1f * j / rows, g1 = 0.1f * (j + 1) / rows;
                float[] b0 = Geo.sq(a0, 4.8f + w0 + g0, 5), b1 = Geo.sq(a1, 4.8f + w1 + g0, 5), t0 = Geo.sq(a0, 4.8f + w0 + g1, 5), t1 = Geo.sq(a1, 4.8f + w1 + g1, 5);
                g.color(0xFFF8F8FA);
                g.quad("hd_cotton", p(t0[0], y1, t0[1]), p(t1[0], y1, t1[1]), p(b1[0], y0, b1[1]), p(b0[0], y0, b0[1]), new float[]{0, 0, 0.1f, 0, 0.1f, 1, 0, 1});
            }
        }
        // band
        g.color(0xFFE8E8EE);
        Geo.sqTube(g, "hd_cotton", 5.0f, 5.0f, -0.2f, 1.3f, 5, 26, 0, 0, 3, 0.3f);
        // puffy top: ring of soft puffs + center puff (breathing)
        float puff = 1 + sin(t * 1.5f) * 0.03f;
        g.color(0xFFFFFFFF);
        for (int i = 0; i < 7; i++) {
            double an = Math.PI * 2 * i / 7 + 0.3;
            Geo.ellipsoid(g, "hd_cotton", cos(an) * 3.1f, 6.4f, sin(an) * 3.1f, 2.6f * puff, 2.2f * puff, 2.6f * puff, 14, 9);
        }
        Geo.ellipsoid(g, "hd_cotton", 0, 7.35f, 0, 3.4f * puff, 2.5f * puff, 3.4f * puff, 16, 10);
        g.pop();
    }

    // ================================================================== Partyhut
    static void party(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(1.1f, 7.95f, 0.2f);
        g.rotZ(-13 + sin(t * 2.2f) * 4);
        g.color(0xFFFFFFFF);
        lathe(g, "hd_party", new float[]{3.35f, 3.1f, 2.4f, 1.5f, 0.7f, 0.12f}, new float[]{0.1f, 0.9f, 3.0f, 5.4f, 7.6f, 9.1f}, 24, 0, 1, 2, 0);
        // bottom frill
        g.color(0xFFFFE24A);
        int n = 40;
        float[][] fr = new float[n + 1][];
        for (int i = 0; i <= n; i++) {
            double an = Math.PI * 2 * i / n;
            fr[i] = p(cos(an) * 3.4f, 0.25f + 0.22f * sin(an * 10), sin(an) * 3.4f);
        }
        float[] rr = new float[n + 1];
        java.util.Arrays.fill(rr, 0.42f);
        XmasGeo.sweep(g, "hd_satin", fr, rr, rr, null, 8, 8);
        // pom pom of paper strips
        g.translate(0, 9.3f, 0);
        g.color(0xFFFF4A9A);
        ball(g, "hd_fluff", 0, 0, 0, 0.95f, 10);
        int[] cs = {0xFFFF4A9A, 0xFF3AC8FF, 0xFFFFE24A, 0xFF6AFF7A, 0xFFB06AFF};
        for (int i = 0; i < 12; i++) {
            g.push();
            g.rotY(i * 97);
            g.rotZ(20 + (i % 4) * 22 + sin(t * 3 + i) * 5);
            g.color(cs[i % 5]);
            cbox(g, "hd_satin", 0, 1.2f, 0, 0.35f, 1.8f, 0.06f, 0.02f);
            g.pop();
        }
        g.pop();
        // confetti (3D, tumbling)
        int[] cols = {0xFFFF3A8A, 0xFF3AC8FF, 0xFFFFE24A, 0xFF6AFF7A, 0xFFB06AFF};
        for (int i = 0; i < 14; i++) {
            float ph = fract(t * 0.4f + i / 14f);
            double an = i * 2.4 + t * 0.5;
            float sc = Math.min(1, sin(ph * Math.PI) * 2.2f);
            if (sc <= 0.05f) continue;
            g.push();
            g.translate(cos(an) * (4.5f + i % 3), 20 - ph * 15, sin(an) * (4.5f + i % 3));
            g.rotY(t * 200 + i * 40);
            g.rotX(t * 150 + i * 70);
            g.scale(sc);
            g.color(cols[i % 5]);
            if (i % 3 == 0) Geo.ellipsoid(g, "hd_plastic", 0, 0, 0, 0.3f, 0.3f, 0.06f, 6, 3);
            else cbox(g, "hd_plastic", 0, 0, 0, 0.8f, 0.45f, 0.06f, 0.02f);
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Zaubererhut
    static void wizard(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(0, 7.3f, 0);
        g.rotZ(-5);
        g.color(0xFF3A2AB0);
        brim(g, "hd_felt", 4.75f, 5, 9.2f, 1, 0.34f, 40, 4, (an, f) -> 0.2f + f * f * (0.5f * sin(an * 3 + 1) - 0.35f));
        g.color(0xFFFFFFFF);
        float sway = sin(t * 1.4f) * 10;
        float[][] sp = bentSpine(0.2f, 13.5f, 16, 95 + sway, 200 + sway, 0.4f);
        int m = sp.length;
        float[] rx = new float[m], nn = new float[m];
        for (int i = 0; i < m; i++) {
            float f = i / (float) (m - 1);
            rx[i] = Math.max(0.06f, 4.74f * (float) Math.pow(1 - f, 1.3) + 0.05f);
            nn[i] = f < 0.1f ? 5 : 2;
        }
        XmasGeo.sweep(g, "hd_wizard", sp, rx, rx, nn, 24, 2);
        // gold band with a crescent moon emblem
        g.color(0xFFFFD86A);
        Geo.sqTube(g, "hd_gold", 4.83f, 4.6f, 0.35f, 1.7f, 5, 24, 0, 0, 3, 1);
        g.push();
        g.translate(0, 1.05f, 4.9f);
        float[][] moon = new float[20][];
        for (int i = 0; i < 10; i++) {
            double an = Math.PI * 0.2 + Math.PI * 1.6 * i / 9;
            moon[i] = new float[]{cos(an + Math.PI / 2) * 1.2f, sin(an + Math.PI / 2) * 1.2f};
            moon[19 - i] = new float[]{cos(an + Math.PI / 2) * 0.9f + 0.45f * (float) Math.sin(an) * 0, sin(an + Math.PI / 2) * 0.9f + 0.1f};
        }
        for (int i = 0; i < 10; i++) moon[19 - i][0] += 0.45f;
        g.glow(true);
        HdGeo.puff(g, "hd_gold", moon, 0.3f, 0.1f, 1);
        g.glow(false);
        g.pop();
        // star at the tip + drifting sparkles
        float[] e = sp[m - 1];
        g.push();
        g.translate(e[0], e[1] + 0.2f, e[2]);
        g.rotY(t * 70);
        g.glow(true).color(0xFFFFE27A);
        XmasGeo.star(g, "hd_gold", 1.0f, 0.3f, 5);
        g.glow(false);
        Cos3Geo.halo(g, 0, 0, 0, 3.6f, alpha(0.5 + 0.2 * sin(t * 4), 0xFFE07A));
        g.pop();
        g.pop();
        for (int i = 0; i < 6; i++) {
            float ph = fract(t * 0.35f + i / 6f);
            double an = i * 1.05 + t * 0.9;
            float s = sin(ph * Math.PI);
            Cos3Models.sparkle(g, cos(an) * (6.5f + ph * 2), 9 + ph * 9, sin(an) * (6.5f + ph * 2), 0.5f * s, alpha(s, 0xFFF0B0));
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Alien-Antennen
    static void alien(G g, A a) {
        float t = a.time, mv = a.preview ? 0 : a.move;
        // headband
        int n = 20;
        float[][] bandPts = new float[n + 1][];
        for (int i = 0; i <= n; i++) bandPts[i] = XmasCos.bandPt(Math.PI * i / n, 5.0f, 5.55f, 3.5f);
        float[] br = new float[n + 1], bz = new float[n + 1];
        java.util.Arrays.fill(br, 0.3f);
        java.util.Arrays.fill(bz, 0.62f);
        g.color(0xFF2A8A3A);
        XmasGeo.sweep(g, "hd_plastic", bandPts, br, bz, null, 8, 1);
        for (int s = 1; s >= -1; s -= 2) {
            float[] b = XmasCos.bandPt(s > 0 ? Math.PI * 0.32 : Math.PI * 0.68, 5.0f, 5.55f, 3.5f);
            int k = 10;
            float[][] pts = new float[k + 1][];
            float[] rad = new float[k + 1];
            for (int i = 0; i <= k; i++) {
                float f = i / (float) k;
                float sw = sin(t * 3.2f + s + f * 1.5f) * 1.3f * f * f + mv * f * f * 1.5f;
                pts[i] = p(b[0] + s * f * 2.2f + sw * 0.5f, b[1] + f * 6.8f, b[2] + sw * 0.8f - f * f * 0.9f);
                rad[i] = 0.3f - f * 0.12f;
            }
            g.color(0xFF4ADE5A);
            // base socket
            ball(g, "hd_plastic", b[0], b[1] + 0.1f, b[2], 0.55f, 8);
            limb(g, "hd_plastic", pts, rad, 7);
            // spring rings
            g.color(0xFF2A8A3A);
            for (int i = 1; i < k; i += 2) {
                g.push();
                g.translate(pts[i][0], pts[i][1], pts[i][2]);
                float[] d = pts[i + 1];
                alignY(g, d[0] - pts[i][0], d[1] - pts[i][1], d[2] - pts[i][2]);
                g.torus("hd_plastic", rad[i] + 0.06f, 0.07f, 10, 3, 0, 0, 1, 1);
                g.pop();
            }
            float[] tip = pts[k];
            float pulse = 0.75f + 0.25f * sin(t * 4 + s);
            g.push();
            g.translate(tip[0], tip[1] + 0.85f, tip[2]);
            g.color(0xFF7CFF6A);
            ball(g, "hd_bubble", 0, 0, 0, 1.15f, 12);
            g.glow(true).color(0xFF000000 | Capes.mix(0x3AE84A, 0xE8FFE0, pulse));
            ball(g, "white", 0, 0, 0, 0.6f * pulse, 8);
            g.glow(false);
            Cos3Geo.halo(g, 0, 0, 0, 4.4f * pulse, alpha(0.45 * pulse, 0x7CFF6A));
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Große Schleife
    static void bow(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(1.9f, 8.9f, 1.3f);
        g.rotZ(-14 + sin(t * 2) * 2);
        g.rotY(-12);
        g.scale(1.3f);
        g.color(0xFFFF4A8E);
        float[][] wing = new float[18][];
        wing[0] = new float[]{0.3f, -0.6f};
        for (int i = 0; i < 16; i++) {
            double an = Math.toRadians(-150 + 300 * i / 15.0);
            wing[i + 1] = new float[]{2.25f + cos(an) * 1.55f, sin(an) * 1.75f};
        }
        wing[17] = new float[]{0.3f, 0.6f};
        for (int s = 1; s >= -1; s -= 2) {
            g.push();
            g.scale(s, 1, 1);
            g.rotZ(10);
            g.rotY(-12);
            HdGeo.puff(g, "hd_satin", wing, 1.25f, 0.55f, 2);
            g.pop();
            // tails with notched ends
            g.push();
            g.translate(s * 0.35f, -0.4f, 0.2f);
            g.rotZ(s * 24 + sin(t * 2.5f + s) * 5);
            float[][] tail = {{-0.62f, 0}, {0.62f, 0}, {0.95f, -3.1f}, {0.2f, -2.6f}, {-0.45f, -3.2f}};
            HdGeo.puff(g, "hd_satin", tail, 0.32f, 0.12f, 1);
            g.pop();
        }
        // knot
        g.color(0xFFE0307A);
        Geo.ellipsoid(g, "hd_satin", 0, 0, 0.05f, 0.8f, 1.0f, 0.78f, 12, 8);
        g.color(0xFFFFFFFF);
        g.pop();
    }

    // ================================================================== Pilzhut
    static void mushroom(G g, A a) {
        float t = a.time;
        float bob = sin(t * 2) * 0.15f;
        g.push();
        g.translate(0, 7.3f + bob, 0);
        g.rotZ(sin(t * 1.3f) * 2);
        // cap: lathe with the rim rolled under
        float[] r = {5.4f, 6.55f, 6.85f, 6.7f, 6.1f, 5.1f, 3.6f, 1.8f, 0};
        float[] y = {0.35f, 0.25f, 0.75f, 1.6f, 2.8f, 3.9f, 4.7f, 5.1f, 5.2f};
        g.color(0xFFFFFFFF);
        lathe(g, "hd_mush", reverse(r), reverse(y), 30, 0, 0, 1, 1);
        // gills underneath
        lathe(g, "hd_gill", new float[]{5.4f, 4.4f}, new float[]{0.35f, 0.9f}, 30);
        // white spots (flattened domes on the cap surface)
        java.util.Random rn = new java.util.Random(42);
        for (int i = 0; i < 13; i++) {
            double an = i * 2.39996;
            float f = i == 0 ? 0 : 0.25f + 0.65f * (float) Math.sqrt(i / 13f);
            // position on the profile
            float kf = 2 + (1 - f) * 5.6f;
            int k = Math.min(7, (int) kf);
            float fr = kf - k;
            float rr = r[k] + (r[k + 1] - r[k]) * fr, yy = y[k] + (y[k + 1] - y[k]) * fr;
            float tr = r[k + 1] - r[k], ty = y[k + 1] - y[k];
            float nx = cos(an), nz = sin(an);
            g.push();
            g.translate(nx * rr, yy, nz * rr);
            alignY(g, nx * ty, -tr, nz * ty);
            float s = 0.55f + rn.nextFloat() * 0.35f;
            g.color(0xFFFFF8EE);
            Geo.ellipsoid(g, "hd_cotton", 0, 0, 0, s, 0.22f, s * 0.9f, 8, 3);
            g.pop();
        }
        g.pop();
        g.color(0xFFFFFFFF);
    }

    static float[] reverse(float[] a) {
        float[] r = new float[a.length];
        for (int i = 0; i < a.length; i++) r[i] = a[a.length - 1 - i];
        return r;
    }
}
