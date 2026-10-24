package dev.lego.cosmetic;

import static dev.lego.cosmetic.Cos3Geo.*;

import dev.lego.cosmetic.Cos.A;

/**
 * HD remaster of the classic face items (head space, face at z = +4, eyes at y ~ 3.5).
 * Frames are closed tubes around real lens outlines, lenses have thickness, temples run back to the ears.
 */
final class HdFace {
    private HdFace() { }

    private static float[] p(float x, float y, float z) { return new float[]{x, y, z}; }

    /** Temples (arms) from the frame corners back to the ears, with small hinges. */
    static void temples(G g, String tex, int col, float y, float x, float zFront) {
        int oc = g.color;
        g.color(col);
        for (int s = -1; s <= 1; s += 2) {
            float[][] pts = {{s * x, y, zFront}, {s * (4.25f), y + 0.05f, zFront - 0.8f}, {s * 4.3f, y + 0.1f, 0}, {s * 4.3f, y - 0.1f, -2.8f}, {s * 4.22f, y - 0.9f, -3.6f}};
            float[] rr = {0.2f, 0.18f, 0.17f, 0.17f, 0.15f};
            limb(g, tex, pts, rr, 5);
            cbox(g, tex, s * (x - 0.05f), y, zFront - 0.1f, 0.55f, 0.6f, 0.5f, 0.12f);
        }
        g.color(oc);
    }

    /** Lens (thin puffy disc of the outline) with a frame tube around it, centered at (cx, cy) in the face plane. */
    static void lensFrame(G g, float[][] outline, float cx, float cy, float z, String frameTex, int frameCol, float fr, String lensTex, int lensCol, boolean lensGlow) {
        g.push();
        g.translate(cx, cy, z);
        int oc = g.color;
        g.color(frameCol);
        HdGeo.loop(g, frameTex, outline, fr, fr * 1.15f, 2.6f, 6);
        g.color(lensCol).glow(lensGlow);
        HdGeo.puff(g, lensTex, HdGeo.offset(HdGeo.ccw(outline), -fr * 0.2f), 0.2f, 0.08f, 1);
        g.glow(false).color(oc);
        g.pop();
    }

    // ================================================================== Sonnenbrille (aviator style)
    static void sunglasses(G g, A a) {
        float[][] lens = new float[22][];
        for (int i = 0; i < 22; i++) {
            double an = Math.PI * 2 * i / 22;
            float x = HdGeo.cos(an) * 1.85f, y = HdGeo.sin(an) * 1.25f;
            if (y < 0) { y *= 1.25f; x *= 1 - 0.18f * (float) Math.pow(-HdGeo.sin(an), 2) * (HdGeo.cos(an) > 0 ? 0.4f : 1); }
            lens[i] = new float[]{x, y};
        }
        for (int s = -1; s <= 1; s += 2) {
            float[][] l = new float[lens.length][];
            for (int i = 0; i < lens.length; i++) l[i] = new float[]{lens[i][0] * s, lens[i][1]};
            lensFrame(g, l, s * 2.1f, 3.55f, 4.45f, "hd_gold", 0xFFE8C26A, 0.17f, "hd_lens", 0xFFFFFFFF, false);
        }
        // double bridge
        g.color(0xFFE8C26A);
        Geo.chain(g, "hd_gold", new float[][]{{-0.45f, 4.55f, 4.45f}, {0, 4.7f, 4.5f}, {0.45f, 4.55f, 4.45f}}, new float[]{0.13f, 0.13f, 0.13f}, 5);
        Geo.chain(g, "hd_gold", new float[][]{{-0.35f, 3.95f, 4.5f}, {0, 3.75f, 4.55f}, {0.35f, 3.95f, 4.5f}}, new float[]{0.1f, 0.1f, 0.1f}, 5);
        // nose pads
        g.color(0xDDF0F4FA);
        for (int s = -1; s <= 1; s += 2) Geo.ellipsoid(g, "hd_glass", s * 0.55f, 2.75f, 4.2f, 0.18f, 0.32f, 0.14f, 5, 3);
        temples(g, "hd_gold", 0xFFE8C26A, 4.2f, 3.9f, 4.4f);
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Pixel-Brille (real voxels)
    private static final String[] PIXEL = {
            "XXXXXXXXXXXXXXXXXXX",
            "XXXXXXXXXXXXXXXXXXX",
            "..XWWXXXX...XWWXXXX",
            "..XWXXXXX...XWXXXXX",
            "...XXXXX.....XXXXX.",
            "....XXX.......XXX..",
    };

    static void pixelGlasses(G g, A a) {
        float px = 0.46f;
        int w = PIXEL[0].length();
        float x0 = -w * px / 2;
        float y0 = 4.95f;
        // gentle "deal with it" slide-in when the preview animates
        float slide = a.preview ? 0 : Math.max(0, 1 - a.time * 0.0f);
        g.push();
        g.translate(0, slide * 0, 4.3f);
        for (int r = 0; r < PIXEL.length; r++)
            for (int c = 0; c < w; c++) {
                char ch = PIXEL[r].charAt(c);
                if (ch == '.') continue;
                // mirror the columns so the design reads correctly from the front
                float x = -(x0 + c * px + px / 2);
                float y = y0 - r * px - px / 2;
                boolean white = ch == 'W';
                g.color(white ? 0xFFF4F4F4 : 0xFF0E0E12);
                g.box(white ? "hd_plastic" : "hd_rubber", x - px / 2 + 0.01f, y - px / 2 + 0.01f, -0.2f, x + px / 2 - 0.01f, y + px / 2 - 0.01f, white ? 0.26f : 0.2f, 0, 0, 1, 1);
            }
        g.pop();
        temples(g, "hd_rubber", 0xFF0E0E12, 4.5f, 4.15f, 4.3f);
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Herzbrille
    static void heartGlasses(G g, A a) {
        float beat = (float) Math.pow(Math.max(0, Math.sin(a.time * 6)), 4);
        float[][] heart = HdGeo.heart(2.0f, 28);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 2.15f, 3.55f, 4.5f);
            g.scale(1 + 0.08f * beat, 1 + 0.08f * beat, 1);
            g.rotZ(s * -6);
            g.color(0xFFFF2A6A);
            HdGeo.loop(g, "hd_plastic", heart, 0.3f, 0.32f, 2.4f, 6);
            g.color(0xCCFF6A9A);
            HdGeo.puff(g, "hd_glass", HdGeo.offset(HdGeo.ccw(heart), -0.1f), 0.2f, 0.08f, 1);
            g.pop();
        }
        g.color(0xFFFF2A6A);
        Geo.chain(g, "hd_plastic", new float[][]{{-0.55f, 4.35f, 4.5f}, {0, 4.55f, 4.55f}, {0.55f, 4.35f, 4.5f}}, new float[]{0.16f, 0.16f, 0.16f}, 5);
        temples(g, "hd_plastic", 0xFFFF2A6A, 4.3f, 4.05f, 4.45f);
        // little hearts popping out on every beat
        float ph = fract(a.time * 1.0f);
        if (ph < 0.8f) {
            float s = HdGeo.sin(ph / 0.8f * Math.PI);
            g.push();
            g.translate(4.6f + ph * 1.5f, 5.4f + ph * 3, 4.6f);
            g.scale(0.4f * s);
            g.glow(true).color(0xFFFF4A8A);
            HdGeo.puff(g, "hd_plastic", HdGeo.heart(1.4f, 16), 0.9f, 0.4f, 1);
            g.glow(false);
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Sternenbrille
    static void starGlasses(G g, A a) {
        float t = a.time;
        float[][] star = HdGeo.star(2.35f, 1.1f, 5);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 2.25f, 3.6f, 4.5f);
            g.rotZ(s * 8);
            g.color(0xFFFFC83A);
            HdGeo.loop(g, "hd_gold", star, 0.26f, 0.3f, 2.4f, 6);
            g.color(0xFF2A1A6A);
            HdGeo.puff(g, "hd_lens", HdGeo.offset(HdGeo.ccw(star), -0.1f), 0.2f, 0.08f, 1);
            g.pop();
        }
        g.color(0xFFFFC83A);
        Geo.chain(g, "hd_gold", new float[][]{{-0.55f, 4.2f, 4.5f}, {0, 4.4f, 4.55f}, {0.55f, 4.2f, 4.5f}}, new float[]{0.15f, 0.15f, 0.15f}, 5);
        temples(g, "hd_gold", 0xFFFFC83A, 4.1f, 4.1f, 4.45f);
        // twinkles on the star tips
        for (int i = 0; i < 3; i++) {
            float tw = Math.max(0, HdGeo.sin(t * 3 + i * 2.1f));
            if (tw < 0.05f) continue;
            float[] q = {i == 0 ? 2.25f : (i == 1 ? -2.25f : 4.2f), i == 2 ? 3.2f : 5.95f};
            Cos3Models.sparkle(g, q[0], q[1], 4.85f, 0.5f * tw, alpha(tw, 0xFFFFFF));
            halo(g, q[0], q[1], 4.85f, 2.0f * tw, alpha(0.4 * tw, 0xFFE07A));
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Monokel
    static void monocle(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(-1.95f, 3.7f, 4.4f);
        g.color(0xFFFFFFFF);
        HdGeo.loop(g, "hd_gold", HdGeo.circle(1.5f, 1.5f, 24), 0.2f, 0.24f, 2, 6);
        // knurled outer ring
        g.color(0xFFE8C26A);
        g.push();
        g.translate(0, 0, -0.05f);
        HdGeo.loop(g, "hd_gold", HdGeo.circle(1.78f, 1.78f, 24), 0.09f, 0.14f, 2, 4);
        g.pop();
        g.color(0xB8FFFFFF);
        HdGeo.puff(g, "hd_glass", HdGeo.circle(1.46f, 1.46f, 20), 0.16f, 0.06f, 1);
        g.pop();
        // chain: from the ring down to a clip at the collar (swings gently)
        float sw = sin(t * 1.8f) * 0.25f;
        float[] start = {-3.35f, 2.95f, 4.4f}, end = {-4.1f, -1.2f + sw * 0.2f, 3.6f + sw};
        float[][] pts = sag(start, end, 1.4f, 8);
        g.color(0xFFFFFFFF);
        chainPath(g, "hd_gold", pts, 0.34f);
        g.push();
        g.translate(end[0], end[1], end[2]);
        cbox(g, "hd_gold", 0, -0.3f, 0, 0.3f, 0.8f, 0.5f, 0.1f);
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Schnurrbart (handlebar)
    static void mustache(G g, A a) {
        float t = a.time;
        float tw = Math.max(0, sin(t * 1.3f)) * 0.08f;
        g.push();
        g.translate(0, 1.55f, 4.25f);
        g.rotZ(sin(t * 2.2f) * 1.2f);
        for (int s = -1; s <= 1; s += 2) {
            int n = 12;
            float[][] sp = new float[n + 1][];
            float[] rx = new float[n + 1], rz = new float[n + 1];
            for (int i = 0; i <= n; i++) {
                float f = i / (float) n;
                double curl = f > 0.6f ? (f - 0.6f) / 0.4f * Math.PI * 1.25 : 0;
                float x = s * (0.1f + f * 3.4f * (1 + tw));
                float y = -0.05f - (float) Math.sin(f * Math.PI * 0.9) * 0.55f + f * f * 0.55f;
                if (curl > 0) {
                    float cx = s * (0.1f + 0.6f * 3.4f), cy = y;
                    x = cx + s * (float) Math.sin(curl) * 0.75f * (1 + tw);
                    y = -0.05f - (float) Math.sin(0.6 * Math.PI * 0.9) * 0.55f + 0.36f * 0.55f + (1 - (float) Math.cos(curl)) * 0.6f;
                }
                sp[i] = p(x, y, 0.35f * (float) Math.sin(f * Math.PI) - f * 0.3f);
                float w = (float) Math.pow(Math.sin(Math.PI * Math.min(1, 0.15 + f * 0.95)), 0.7);
                rx[i] = Math.max(0.06f, 0.75f * w * (1 - 0.6f * f));
                rz[i] = Math.max(0.05f, 0.42f * w * (1 - 0.5f * f));
            }
            g.color(0xFF4A2A16);
            XmasGeo.sweep(g, "hd_mustache", sp, rz, rx, null, 10, 2);
        }
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Clownsnase
    static void clownNose(G g, A a) {
        float t = a.time;
        float honk = (float) Math.pow(Math.max(0, Math.sin(t * 1.7)), 12);
        float sq = 1 + 0.06f * sin(t * 3) - 0.12f * honk;
        g.push();
        g.translate(0, 2.6f, 4.95f);
        g.scale(sq + honk * 0.2f, 1 / sq, sq);
        g.color(0xFFF01A1E);
        g.sphere("hd_gloss", 1.3f, 20, 12, 0, 0, 1, 1);
        g.pop();
        // tiny sparkle on the highlight when it honks
        if (honk > 0.1f) Cos3Models.sparkle(g, -0.55f, 3.4f, 6.1f, 0.5f * honk, alpha(honk, 0xFFFFFF));
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Cyber-Visier
    static void visor(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(0, 3.7f, 0);
        // curved screen: sweep over the front half of the head (squircle arc) with a thick frame
        int n = 16;
        float[][] sp = new float[n + 1][];
        for (int i = 0; i <= n; i++) {
            double an = Math.PI * (0.1 + 0.8 * i / n);
            float[] q = Geo.sq(an, 4.85f, 5);
            sp[i] = p(q[0], 0, q[1]);
        }
        float[] rx = new float[n + 1], rz = new float[n + 1];
        for (int i = 0; i <= n; i++) { rx[i] = 0.32f; rz[i] = 1.55f; }
        g.color(0xFF2A2E38);
        XmasGeo.sweep(g, "hd_steel", sp, rx, rz, null, 8, 1);
        // glowing screen strip in front
        for (int i = 0; i <= n; i++) {
            double an = Math.PI * (0.13 + 0.74 * i / n);
            float[] q = Geo.sq(an, 5.12f, 5);
            sp[i] = p(q[0], 0, q[1]);
            rx[i] = 0.08f;
            rz[i] = 1.12f;
        }
        g.glow(true).color(0xFFFFFFFF);
        XmasGeo.sweep(g, "hd_screen", sp, rx, rz, null, 4, 1);
        // scanner light sweeping left <-> right
        float sweep = 0.5f + 0.5f * sin(t * 2.4f);
        double an = Math.PI * (0.2 + 0.6 * sweep);
        float[] q = Geo.sq(an, 5.25f, 5);
        g.color(0xFFFF3A5A);
        g.push();
        g.translate(q[0], 0, q[1]);
        g.rotY((float) -Math.toDegrees(an) + 90);
        bbox(g, "white", -0.35f, -0.85f, -0.05f, 0.35f, 0.85f, 0.12f, 0.08f);
        g.pop();
        halo(g, q[0], 0, q[1] + 0.2f, 3.6f, alpha(0.55, 0xFF3A5A));
        // ear emitters
        for (int s = -1; s <= 1; s += 2) {
            g.glow(false).color(0xFF3A3E4A);
            g.push();
            g.translate(s * 4.4f, 0, -0.6f);
            g.rotZ(90);
            lathe(g, "hd_chrome", new float[]{1.1f, 1.25f, 1.1f, 0.6f, 0}, new float[]{-0.4f, -0.1f, 0.35f, 0.55f, 0.56f}, 14);
            g.pop();
            g.glow(true).color(0xFF2AE6FF);
            g.push();
            g.translate(s * 4.97f, 0, -0.6f);
            g.rotZ(90);
            g.torus("white", 0.62f, 0.1f, 14, 3, 0, 0, 1, 1);
            g.pop();
            g.glow(false);
        }
        g.glow(false).color(0xFFFFFFFF);
        g.pop();
    }

    // ================================================================== Maskenball
    static void masquerade(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(0, 3.75f, 4.3f);
        // two eye rims made of broad flat bands + filigree tips
        for (int s = -1; s <= 1; s += 2) {
            float[][] eye = new float[20][];
            for (int i = 0; i < 20; i++) {
                double an = Math.PI * 2 * i / 20;
                float x = HdGeo.cos(an) * 1.45f, y = HdGeo.sin(an) * 0.85f;
                // lift the outer corner (cat eye)
                float out = x * s > 0 ? x * s / 1.45f : 0;
                eye[i] = new float[]{s * 2.0f + x, y + out * out * 0.6f};
            }
            g.color(0xFF6A2AB8);
            HdGeo.loop(g, "hd_mask", eye, 0.55f, 0.18f, 3, 6);
            // gold edge on the rim
            g.color(0xFFFFD86A);
            g.push();
            g.translate(0, 0, 0.14f);
            HdGeo.loop(g, "hd_gold", HdGeo.offset(HdGeo.ccw(eye), 0.55f), 0.08f, 0.08f, 2, 4);
            g.pop();
            // swirl wing tip
            int n = 10;
            float[][] sp = new float[n + 1][];
            float[] rr = new float[n + 1], rz = new float[n + 1];
            for (int i = 0; i <= n; i++) {
                float f = i / (float) n;
                double an = f * Math.PI * 1.4;
                sp[i] = p(s * (3.6f + 1.1f * (float) Math.sin(an)), 0.9f + 1.1f * (1 - (float) Math.cos(an)) * 0.6f - f * 0.6f, -0.2f - f * 0.6f);
                rr[i] = Math.max(0.05f, 0.45f * (1 - f));
                rz[i] = 0.12f;
            }
            g.color(0xFF6A2AB8);
            XmasGeo.sweep(g, "hd_mask", sp, rr, rz, null, 6, 1);
        }
        // bridge between the eyes
        g.color(0xFF6A2AB8);
        cbox(g, "hd_mask", 0, 0.25f, -0.05f, 1.2f, 0.8f, 0.3f, 0.12f);
        // jewel in the middle
        g.glow(true).color(0xFFFF3A8A);
        g.push();
        g.translate(0, 0.35f, 0.2f);
        g.rotX(90);
        gem(g, "hd_gem", 0.35f, 0.25f, 0.08f, 6);
        g.pop();
        g.glow(false);
        // feather plume on the side (curved sweeps with a shaft)
        g.push();
        g.translate(3.8f, 1.5f, -0.2f);
        g.rotZ(-22 + sin(t * 2) * 5);
        int[] fc = {0xFFB86AFF, 0xFF8A3ADA, 0xFFE8A0FF};
        for (int k = 0; k < 3; k++) {
            g.push();
            g.rotZ(-18 + k * 18);
            int n = 10;
            float[][] sp = new float[n + 1][];
            float[] rr = new float[n + 1], rz = new float[n + 1];
            for (int i = 0; i <= n; i++) {
                float f = i / (float) n;
                sp[i] = p(0.6f * f * f, f * 6.2f, -0.3f * f);
                rr[i] = Math.max(0.05f, 0.95f * (float) Math.sin(Math.PI * Math.min(1, f * 0.95 + 0.05)));
                rz[i] = 0.07f;
            }
            g.color(fc[k]);
            XmasGeo.sweep(g, "c3_plume", sp, rr, rz, null, 6, 1);
            g.pop();
        }
        g.pop();
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== Ninja-Maske
    static void ninja(G g, A a) {
        float t = a.time, mv = a.preview ? 0 : a.move;
        // lower face wrap (slightly draped under the chin)
        g.color(0xFF2E3242);
        band(g, "hd_ninja", 2.6f, 2.07f, 0.3f, -0.15f, 2.95f, 0, 4);
        // nose fold
        g.push();
        g.translate(0, 2.2f, 4.65f);
        g.color(0xFF2E3242);
        float[][] fold = {{-1.4f, 0.8f}, {1.4f, 0.8f}, {0.9f, -0.7f}, {-0.9f, -0.7f}};
        HdGeo.puff(g, "hd_ninja", fold, 0.3f, 0.12f, 1);
        g.pop();
        // head band with metal plate
        g.color(0xFFB81E2A);
        band(g, "hd_ninja", 2.6f, 2.07f, 0.3f, 5.2f, 6.7f, 0, 4);
        g.color(0xFFC0C6D0);
        g.push();
        g.translate(0, 5.95f, 4.72f);
        bbox(g, "hd_steel", -1.8f, -0.65f, -0.1f, 1.8f, 0.65f, 0.2f, 0.12f);
        g.color(0xFF3A3E48);
        cbox(g, "hd_steel", 0, 0, 0.22f, 1.6f, 0.12f, 0.05f, 0.02f);
        g.pop();
        // knot at the back + fluttering tails
        g.color(0xFFB81E2A);
        Geo.ellipsoid(g, "hd_ninja", 0, 5.95f, -4.75f, 0.9f, 0.6f, 0.5f, 8, 5);
        float fl = sin(t * 4) * 10 + mv * 25;
        for (int s = -1; s <= 1; s += 2) {
            int n = 9;
            float[][] sp = new float[n + 1][];
            float[] w = new float[n + 1], th = new float[n + 1];
            for (int i = 0; i <= n; i++) {
                float f = i / (float) n;
                double ang = Math.toRadians(62 - fl * 0.6 * f - s * 6);
                float wave = 0.25f * sin(t * 6 + f * 6 + s) * f;
                sp[i] = p(s * (0.35f + f * 1.2f) + wave, 5.95f - f * 5.2f * (float) Math.sin(ang), -4.9f - f * 5.2f * (float) Math.cos(ang));
                w[i] = 0.1f;
                th[i] = 0.55f - f * 0.15f;
            }
            XmasGeo.sweep(g, "hd_ninja", sp, w, th, null, 6, 2);
        }
        g.color(0xFFFFFFFF);
    }

}
