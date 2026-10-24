package dev.lego.cosmetic;

import static dev.lego.cosmetic.Cos3Geo.*;

import dev.lego.cosmetic.Cos.A;
import dev.lego.cosmetic.Cos.Rarity;
import dev.lego.cosmetic.Cos.Slot;

/** Premium back items (body space: body x -4..4, y -12..0, z -2..2; the back is at z = -2). */
final class Cos3Back {
    private Cos3Back() { }

    static void register() {
        Cos.add("back_c3_minime", "MiniMe-Anhänger", Slot.BACK, Rarity.LEGENDARY, (g, a) -> miniMe(g, a, false));
        Cos.add("back_c3_minime_chain", "MiniMe-Kette", Slot.BACK, Rarity.LEGENDARY, (g, a) -> miniMe(g, a, true));
        Cos.add("back_c3_foxtail", "Fuchsschwanz", Slot.BACK, Rarity.EPIC, Cos3Back::foxTail);
        Cos.add("back_c3_dragontail", "Drachenschwanz", Slot.BACK, Rarity.LEGENDARY, Cos3Back::dragonTail);
        Cos.add("back_c3_parrot", "Schulterpapagei", Slot.BACK, Rarity.EPIC, Cos3Back::parrot);
        Cos.add("back_c3_shoulderdragon", "Schulterdrache", Slot.BACK, Rarity.LEGENDARY, Cos3Back::shoulderDragon);
        Cos.add("back_c3_katana", "Katana", Slot.BACK, Rarity.EPIC, Cos3Back::katana);
        Cos.add("back_c3_jetpack2", "Jetpack V2", Slot.BACK, Rarity.LEGENDARY, Cos3Back::jetpack2);
        Cos.add("back_c3_lantern", "Gürtellaterne", Slot.BACK, Rarity.RARE, Cos3Back::lantern);
    }

    static float mv(A a) { return a.preview ? 0 : clamp(a.move, 0, 1); }

    // ================================================================== MiniMe (skin mapped mini player on a chain)
    static void miniMe(G g, A a, boolean chainVariant) {
        float t = a.time, mv = mv(a);
        float[] pivot;
        if (!chainVariant) {
            // leather tab with a golden hook
            g.color(0xFFFFFFFF);
            bbox(g, "strap", 0.5f, -5.3f, -2.7f, 2.7f, -3.3f, -2.25f, 0.18f);
            g.color(0xFFFFD36A);
            ball(g, "c3_metal", 1.6f, -4.3f, -2.72f, 0.3f, 8);
            Geo.tube(g, "c3_metal", new float[]{1.6f, -4.3f, -2.7f}, new float[]{1.6f, -4.7f, -3.7f}, 0.14f, 0.14f, 6);
            ball(g, "c3_metal", 1.6f, -4.7f, -3.7f, 0.18f, 6);
            pivot = new float[]{1.6f, -4.75f, -3.7f};
        } else {
            // chain swag across the back between two glowing studs, the mini hangs from the middle
            float[] l = {-3.1f, -1.3f, -2.85f}, r = {3.1f, -1.3f, -2.85f};
            for (float[] s : new float[][]{l, r}) {
                g.color(0xFF2A2A34);
                bbox(g, "c3_metal", s[0] - 0.55f, s[1] - 0.55f, -2.95f, s[0] + 0.55f, s[1] + 0.55f, -2.25f, 0.15f);
                g.glow(true).color(0xFF6AE8FF);
                g.push();
                g.translate(s[0], s[1], -3.0f);
                g.rotX(90);
                gem(g, "c3_crystal", 0.38f, 0.3f, 0.1f, 6);
                g.pop();
                g.glow(false);
            }
            g.color(0xFFFFD36A);
            float sagD = 2.2f + sin(t * 1.3f) * 0.08f;
            chainPath(g, "c3_metal", sag(l, r, sagD, 8), 0.55f);
            float[] c = {0, -1.3f - sagD, -2.85f};
            Geo.tube(g, "c3_metal", c, new float[]{0, c[1] - 0.5f, -3.7f}, 0.13f, 0.13f, 6);
            g.glow(true).color(0xFF6AE8FF);
            g.push();
            g.translate(0, c[1] - 0.5f, -3.7f);
            gem(g, "c3_crystal", 0.35f, 0.35f, 0.35f, 6);
            g.pop();
            g.glow(false).color(0xFFFFD36A);
            pivot = new float[]{0, c[1] - 0.85f, -3.7f};
        }
        // swing (only backwards, never into the body)
        float sw = 5 + 5 * sin(t * 2.2f) + mv * 28;
        float side = sin(t * 1.7f) * 6;
        g.push();
        g.translate(pivot[0], pivot[1], pivot[2]);
        g.rotX(sw);
        g.rotZ(side);
        g.color(0xFFFFD36A);
        float drop = chainVariant ? 1.6f : 1.2f;
        chainLinks(g, "c3_metal", new float[]{0, 0, 0}, new float[]{0, -drop, 0}, 0.45f);
        g.push();
        g.translate(0, -drop - 0.15f, 0);
        g.rotX(90);
        g.torus("c3_metal", 0.28f, 0.08f, 10, 4, 0, 0, 1, 1);
        g.pop();
        g.translate(0, -drop - 0.35f - 2.13f, 0);
        g.scale(0.25f);
        g.rotY(180);
        float wave = sin(t * 2.6f);
        miniPlayer(g, "@skin", sin(t * 0.9f) * 18, sin(t * 1.3f) * 6, 14 * wave + mv * 20, -14 * wave - mv * 20, 8, 8, 12 * wave);
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== fox tail
    static float[][] tailPath(float[] start, int n, float segLen, float p0, float p1, float pw, float yawAmp, float yawPh, float pitchWave, float t) {
        float[][] pts = new float[n][];
        pts[0] = start.clone();
        for (int i = 1; i < n; i++) {
            float s = i / (float) (n - 1);
            double p = Math.toRadians(p0 + (p1 - p0) * Math.pow(s, pw) + pitchWave * sin(t * 1.8f - s * 4) * s);
            double w = Math.toRadians(yawAmp * s * sin(yawPh - s * 2.2f));
            float dx = (float) (Math.sin(w) * Math.cos(p)), dy = (float) Math.sin(p), dz = (float) (-Math.cos(w) * Math.cos(p));
            float[] q = pts[i - 1];
            pts[i] = new float[]{q[0] + dx * segLen, q[1] + dy * segLen, q[2] + dz * segLen};
        }
        return pts;
    }

    static void foxTail(G g, A a) {
        float t = a.time, mv = mv(a);
        float yaw = 16 * (1 - 0.5f * mv) + 10 * mv;
        float ph = t * (2.2f + mv * 5);
        int n = 13;
        float[][] pts = tailPath(new float[]{0, -10.2f, -3.3f}, n, 1.1f, -45, 100, 1.6f, yaw * 2, ph, 4, t);
        float[] rad = new float[n];
        for (int i = 0; i < n; i++) {
            float s = i / (float) (n - 1);
            rad[i] = (0.75f + 1.75f * (float) Math.pow(Math.sin(Math.PI * Math.pow(s, 0.8) * 0.92), 0.8)) * (1 - (float) Math.pow(s, 5) * 0.88f);
        }
        int split = 9;
        float[][] a1 = java.util.Arrays.copyOfRange(pts, 0, split + 1), a2 = java.util.Arrays.copyOfRange(pts, split, n);
        float[] r1 = java.util.Arrays.copyOfRange(rad, 0, split + 1), r2 = java.util.Arrays.copyOfRange(rad, split, n);
        g.color(0xFFE8761E);
        Geo.chain(g, "c3_fur", a1, r1, 12);
        ball(g, "c3_fur", pts[0][0], pts[0][1], pts[0][2], rad[0], 10);
        g.color(0xFFFBF6EE);
        Geo.chain(g, "c3_fur", a2, r2, 12);
        // fluffy tufts along the tail
        for (int i = 2; i < n - 1; i += 2) {
            float s = i / (float) (n - 1);
            g.color(s > 0.7f ? 0xFFFBF6EE : 0xFFF0862A);
            float[] p = pts[i];
            float ang = i * 1.9f;
            ball(g, "c3_fur", p[0] + cos(ang) * rad[i] * 0.55f, p[1] + sin(ang) * rad[i] * 0.4f, p[2], rad[i] * 0.62f, 8);
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== dragon tail
    static void dragonTail(G g, A a) {
        float t = a.time, mv = mv(a);
        int n = 17;
        float[][] pts = tailPath(new float[]{0, -10.0f, -3.8f}, n, 1.05f, -55, -15, 1, 30 * (1 + mv), t * (1.5f + mv * 3), 10, t);
        float[] rad = new float[n];
        for (int i = 0; i < n; i++) {
            float s = i / (float) (n - 1);
            rad[i] = 1.25f * (float) Math.pow(1 - s, 0.9) + 0.12f;
        }
        g.color(0xFF4A2E7A);
        Geo.chain(g, "c3_scale", pts, rad, 10);
        ball(g, "c3_scale", pts[0][0], pts[0][1], pts[0][2], rad[0], 10);
        // underside plates
        g.color(0xFFE8C88A);
        for (int i = 1; i < n - 2; i += 1) {
            float[] p = pts[i], q = pts[i + 1];
            float dx = q[0] - p[0], dy = q[1] - p[1], dz = q[2] - p[2];
            float ux = 0, uy = -dz, uz = dy;
            float ul = (float) Math.sqrt(uy * uy + uz * uz);
            uy /= ul; uz /= ul;
            g.push();
            g.translate((p[0] + q[0]) / 2 + ux, (p[1] + q[1]) / 2 - uy * rad[i] * 0.72f, (p[2] + q[2]) / 2 - uz * rad[i] * 0.72f);
            alignY(g, dx, dy, dz);
            float w = rad[i] * 1.1f;
            bbox(g, "c3_metal", -w / 2, -0.45f, -rad[i] * 0.42f, w / 2, 0.45f, rad[i] * 0.42f, 0.12f);
            g.pop();
        }
        // glowing spikes along the top
        for (int i = 1; i < n - 1; i += 2) {
            float s = i / (float) (n - 1);
            float[] p = pts[i], q = pts[i + 1];
            float dx = q[0] - p[0], dy = q[1] - p[1], dz = q[2] - p[2];
            float uy = -dz, uz = dy, ul = (float) Math.sqrt(uy * uy + uz * uz);
            uy /= ul; uz /= ul;
            g.push();
            g.translate(p[0], p[1] + uy * rad[i] * 0.8f, p[2] + uz * rad[i] * 0.8f);
            alignY(g, 0, uy - dy * 0.5f, uz - dz * 0.5f);
            float pul = 0.7f + 0.3f * sin(t * 3 - i * 0.5f);
            g.glow(true).color(alpha(pul, 0xC06AFF));
            shard(g, "c3_crystal", 0.22f + 0.28f * (1 - s), 0.6f + 0.9f * (1 - s));
            g.glow(false);
            g.pop();
        }
        // spade tip
        float[] e = pts[n - 1], d = pts[n - 2];
        g.push();
        g.translate(e[0], e[1], e[2]);
        alignY(g, e[0] - d[0], e[1] - d[1], e[2] - d[2]);
        g.translate(0, -0.3f, 0);
        g.glow(true).color(0xFFC06AFF);
        extrude(g, "c3_crystal", new float[][]{{0, -0.2f}, {1.0f, 0.55f}, {0.35f, 0.9f}, {0, 2.2f}, {-0.35f, 0.9f}, {-1.0f, 0.55f}}, -0.14f, 0.14f);
        g.glow(false);
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== shoulder parrot (right shoulder)
    static void parrot(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(6.2f, 1.0f, 0.2f);
        g.rotY(-12);
        float hop = Math.max(0, sin(t * 0.8f)) > 0.97f ? (sin(t * 0.8f) - 0.97f) * 12 : 0;
        g.translate(0, hop, 0);
        // feet + legs
        g.color(0xFF4A4A52);
        for (int s = -1; s <= 1; s += 2) {
            bbox(g, "white", s * 0.45f - 0.17f, 0, -0.35f, s * 0.45f + 0.17f, 0.28f, 0.75f, 0.1f);
            Geo.tube(g, "white", new float[]{s * 0.45f, 0.25f, 0.15f}, new float[]{s * 0.4f, 1.0f, 0}, 0.15f, 0.13f, 5);
        }
        // tail feathers
        g.push();
        g.translate(0, 1.4f, -1.1f);
        g.rotY(90);
        for (int k = -1; k <= 1; k += 2) {
            g.push();
            g.translate(0, 0, k * 0.12f);
            g.rotZ(-42 + k * 4 + sin(t * 1.3f) * 3);
            g.color(0xFFFFFFFF);
            extrude(g, "c3_parrotwing", new float[][]{{0, 0.25f}, {2.4f, 0.35f}, {4.4f, 0.1f}, {4.6f, -0.1f}, {2.4f, -0.35f}, {0, -0.3f}}, -0.06f, 0.06f);
            g.pop();
        }
        g.pop();
        // body
        g.color(0xFF1AB89A);
        Geo.ellipsoid(g, "c3_plume", 0, 2.0f, -0.1f, 1.05f, 1.55f, 1.15f, 14, 8);
        g.color(0xFFFFD23A);
        Geo.ellipsoid(g, "c3_plume", 0, 1.75f, 0.35f, 0.8f, 1.1f, 0.9f, 12, 7);
        // wings (flap burst now and then)
        float fl = (float) Math.pow(Math.max(0, sin(t * 1.1f)), 12) * 38 * (0.6f + 0.4f * sin(t * 18));
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 0.95f, 2.9f, 0.2f);
            g.rotZ(s * (6 + (s < 0 ? fl * 0.3f : fl)));
            g.rotY(90);
            g.rotZ(-12);
            g.color(0xFFFFFFFF);
            extrude(g, "c3_parrotwing", new float[][]{{-0.6f, 0.3f}, {0.4f, 0.6f}, {1.6f, 0.2f}, {2.9f, -1.3f}, {2.5f, -1.7f}, {1.0f, -1.5f}, {-0.4f, -0.7f}}, -0.12f, 0.12f);
            g.pop();
        }
        // head
        g.push();
        g.translate(0, 3.75f, 0.35f);
        g.rotY(sin(t * 0.9f) * 28);
        g.rotZ(sin(t * 0.6f + 1) * 10);
        g.color(0xFF22C8A8);
        g.sphere("c3_plume", 0.95f, 14, 8, 0, 0, 1, 1);
        for (int s = -1; s <= 1; s += 2) {
            g.color(0xFFF8F4EE);
            Geo.ellipsoid(g, "white", s * 0.52f, 0.02f, 0.48f, 0.42f, 0.48f, 0.4f, 10, 6);
            boolean blink = fract(t * 0.3f) > 0.96f;
            g.color(0xFF141418);
            Geo.ellipsoid(g, "white", s * 0.78f, 0.22f, 0.5f, 0.2f, blink ? 0.04f : 0.22f, 0.2f, 8, 5);
            if (!blink) {
                g.color(0xFFFFFFFF);
                ball(g, "white", s * 0.9f, 0.3f, 0.58f, 0.07f, 4);
            }
        }
        // beak
        g.color(0xFFFFB03A);
        limb(g, "c3_metal", new float[][]{{0, 0.05f, 0.8f}, {0, -0.1f, 1.25f}, {0, -0.45f, 1.38f}}, new float[]{0.36f, 0.24f, 0.07f}, 8);
        g.color(0xFFC8801A);
        limb(g, "c3_metal", new float[][]{{0, -0.35f, 0.75f}, {0, -0.5f, 1.02f}}, new float[]{0.26f, 0.1f}, 7);
        // crest
        float cr = 10 + 12 * (float) Math.pow(Math.max(0, sin(t * 0.7f)), 6);
        for (int k = 0; k < 3; k++) {
            g.push();
            g.translate(0, 0.8f, -0.05f - k * 0.2f);
            g.rotY(90);
            g.rotZ(95 + k * 22 - cr);
            g.color(k == 1 ? 0xFFFF7A2A : 0xFFFF4A3A);
            extrude(g, "c3_plume", new float[][]{{0, 0.18f}, {0.9f, 0.22f}, {1.5f, 0}, {0.9f, -0.2f}, {0, -0.15f}}, -0.07f, 0.07f);
            g.pop();
        }
        g.pop();
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== shoulder dragon (left shoulder)
    static void shoulderDragon(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(-6.1f, 0.95f, 0.0f);
        g.rotY(10);
        int body = 0xFF6A3AA8, belly = 0xFFF2D8A0;
        // legs
        g.color(body);
        for (int s = -1; s <= 1; s += 2)
            for (int f = 0; f < 2; f++) {
                float z = f == 0 ? 0.8f : -1.0f;
                limb(g, "c3_scale", new float[][]{{s * 0.55f, 1.2f, z}, {s * 0.7f, 0.5f, z + 0.1f}, {s * 0.62f, 0.15f, z + 0.3f}}, new float[]{0.3f, 0.2f, 0.17f}, 6);
            }
        Geo.ellipsoid(g, "c3_scale", 0, 1.5f, -0.2f, 0.95f, 1.0f, 1.55f, 14, 8);
        g.color(belly);
        Geo.ellipsoid(g, "c3_metal", 0, 1.22f, 0.05f, 0.72f, 0.72f, 1.25f, 12, 6);
        // tail curling down behind the shoulder
        g.color(body);
        float sw = sin(t * 1.4f) * 0.4f;
        limb(g, "c3_scale", new float[][]{{0, 1.4f, -1.5f}, {0.1f + sw * 0.3f, 1.0f, -2.4f}, {0.4f + sw, 0.3f, -2.9f}, {0.9f + sw, -0.5f, -2.85f}, {1.2f + sw, -1.0f, -2.5f}}, new float[]{0.55f, 0.42f, 0.3f, 0.18f, 0.08f}, 7);
        // neck
        float bob = sin(t * 1.6f) * 0.12f;
        limb(g, "c3_scale", new float[][]{{0, 1.9f, 0.9f}, {0, 2.7f, 1.35f}, {0, 3.3f + bob, 1.65f}}, new float[]{0.58f, 0.46f, 0.42f}, 8);
        // spikes along the back
        for (int i = 0; i < 4; i++) {
            g.push();
            g.translate(0, 2.35f - i * 0.12f, 0.5f - i * 0.6f);
            g.rotX(-20);
            g.glow(true).color(0xFFFFB03A);
            shard(g, "c3_crystal", 0.2f, 0.55f);
            g.glow(false);
            g.pop();
        }
        // wings
        float flap = sin(t * 2.2f) * 12 + (float) Math.pow(Math.max(0, sin(t * 0.7f)), 10) * 35;
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 0.65f, 2.2f, -0.1f);
            g.scale(s, 1, 1);
            if (s > 0) { g.rotZ(14 + flap * 0.3f); g.rotY(62); } else { g.rotZ(38 + flap); g.rotY(22); }
            g.rotX(-90);
            g.color(0xD89A5AE8);
            float[][] mem = {{0, 0}, {1.5f, -0.25f}, {3.3f, 0.3f}, {2.6f, 1.05f}, {2.3f, 2.0f}, {1.65f, 1.5f}, {1.0f, 2.2f}, {0.6f, 1.4f}, {0, 1.1f}};
            extrude(g, "c3_membrane", mem, -0.05f, 0.05f);
            g.color(body);
            Geo.chain(g, "c3_scale", new float[][]{{0, 0, 0}, {1.5f, -0.25f, 0}, {3.3f, 0.3f, 0}}, new float[]{0.2f, 0.15f, 0.07f}, 5);
            Geo.tube(g, "c3_scale", new float[]{1.5f, -0.25f, 0}, new float[]{2.3f, 2.0f, 0}, 0.1f, 0.05f, 4);
            Geo.tube(g, "c3_scale", new float[]{1.5f, -0.25f, 0}, new float[]{1.0f, 2.2f, 0}, 0.1f, 0.05f, 4);
            g.pop();
        }
        // head
        g.push();
        g.translate(0, 3.55f + bob, 1.85f);
        g.rotY(sin(t * 0.8f) * 25);
        g.rotX(-8 + sin(t * 1.1f) * 5);
        g.color(body);
        bbox(g, "c3_scale", -0.72f, -0.55f, -0.7f, 0.72f, 0.62f, 0.72f, 0.32f);
        bbox(g, "c3_scale", -0.48f, -0.5f, 0.5f, 0.48f, 0.18f, 1.65f, 0.2f);
        g.color(belly);
        bbox(g, "c3_metal", -0.4f, -0.62f, 0.3f, 0.4f, -0.3f, 1.55f, 0.12f);
        for (int s = -1; s <= 1; s += 2) {
            g.glow(true).color(0xFFFFE04A);
            Geo.ellipsoid(g, "white", s * 0.7f, 0.18f, 0.35f, 0.12f, 0.16f, 0.2f, 6, 4);
            g.glow(false).color(0xFFF2E8D0);
            g.push();
            g.translate(s * 0.38f, 0.5f, -0.35f);
            g.rotX(-118);
            g.rotZ(s * 12);
            shard(g, "c3_metal", 0.18f, 1.25f);
            g.pop();
        }
        // occasional ember breath
        float br = fract(t / 5f);
        if (br < 0.3f) {
            for (int i = 0; i < 4; i++) {
                float f = fract(br / 0.3f + i * 0.25f);
                halo(g, sin(i * 2.1f) * 0.2f * f, -0.2f + f * 0.3f, 1.9f + f * 2.4f, 0.6f + f * 1.2f, alpha((1 - f) * 0.9, f < 0.3f ? 0xFFE07A : 0xFF6A1A));
            }
        }
        g.pop();
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== katana on the back
    static float curve(float y) { return 0.012f * (y + 1.5f) * (y + 1.5f) - 0.2f; }

    static void katana(G g, A a) {
        float t = a.time;
        // leather strap across the back
        g.push();
        g.translate(0, -5.6f, -2.42f);
        g.rotZ(52);
        g.color(0xFFFFFFFF);
        bbox(g, "strap", -0.6f, -5.3f, -0.16f, 0.6f, 5.3f, 0.16f, 0.1f);
        g.pop();
        g.push();
        g.translate(1.0f, -6.0f, -3.35f);
        g.rotZ(-38);
        // scabbard (slightly curved, extruded profile)
        int n = 14;
        float y0 = -7.5f, y1 = 3.9f;
        float[][] poly = new float[n * 2 + 2][];
        for (int i = 0; i <= n; i++) {
            float y = y0 + (y1 - y0) * i / n;
            float w = 0.48f * (i == 0 ? 0.55f : 1);
            poly[i] = new float[]{curve(y) + w, y};
            poly[2 * n + 1 - i] = new float[]{curve(y) - w, y};
        }
        g.color(0xFF6A0E18);
        extrude(g, "c3_lacquer", poly, -0.3f, 0.3f, new float[]{0, 0, 1, 1}, new float[]{0.3f, 0, 0.4f, 1});
        // gold fittings
        g.color(0xFFFFC85A);
        float[] fy = {3.35f, -1.2f, -2.1f};
        for (float y : fy) bbox(g, "c3_metal", curve(y) - 0.6f, y - 0.25f, -0.4f, curve(y) + 0.6f, y + 0.25f, 0.4f, 0.12f);
        bbox(g, "c3_metal", curve(y0) - 0.55f, y0 - 0.2f, -0.38f, curve(y0) + 0.55f, y0 + 0.65f, 0.38f, 0.2f);
        // buckle holding it to the strap
        g.color(0xFF3A2A20);
        bbox(g, "strap", curve(-1.65f) - 0.35f, -1.95f, 0.2f, curve(-1.65f) + 0.35f, -1.35f, 0.75f, 0.1f);
        // guard
        float gy = 4.1f;
        g.push();
        g.translate(curve(gy), gy, 0);
        g.scale(1, 1, 0.55f);
        g.color(0xFF2A2A32);
        lathe(g, "c3_metal", new float[]{0, 1.35f, 1.35f, 0}, new float[]{-0.18f, -0.18f, 0.18f, 0.18f}, 16);
        g.color(0xFFFFC85A);
        g.torus("c3_metal", 1.35f, 0.1f, 16, 4, 0, 0, 1, 1);
        g.pop();
        g.color(0xFFFFC85A);
        float hx = curve(gy);
        bbox(g, "c3_metal", hx - 0.45f, 4.25f, -0.33f, hx + 0.45f, 4.65f, 0.33f, 0.1f);
        // handle
        g.color(0xFFFFFFFF);
        bbox(g, "c3_wrap", hx - 0.38f, 4.6f, -0.28f, hx + 0.38f, 8.5f, 0.28f, 0.14f);
        g.color(0xFFFFC85A);
        bbox(g, "c3_metal", hx - 0.43f, 8.45f, -0.33f, hx + 0.43f, 8.95f, 0.33f, 0.16f);
        // spirit bead + tassel hanging from the pommel (hangs straight down in body space)
        g.push();
        g.translate(hx, 8.95f, 0);
        g.rotZ(38);
        g.rotX(sin(t * 1.8f) * 8 + mv(a) * 25);
        g.color(0xFF9A2AE8);
        Geo.tube(g, "white", new float[]{0, 0, 0}, new float[]{0, -1.4f, 0}, 0.06f, 0.06f, 4);
        g.glow(true).color(0xFF6AE8FF);
        ball(g, "white", 0, -1.55f, 0, 0.28f, 8);
        halo(g, 0, -1.55f, 0, 1.6f, 0x806AE8FF);
        g.glow(false).color(0xFF9A2AE8);
        lathe(g, "c3_fur", new float[]{0.08f, 0.22f, 0.3f, 0.18f}, new float[]{-1.75f, -2.0f, -2.9f, -3.2f}, 8);
        g.pop();
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== jetpack v2
    static void jetpack2(G g, A a) {
        float t = a.time, mv = mv(a);
        g.color(0xFFE8ECF2);
        bbox(g, "c3_metal", -2.2f, -8.6f, -4.6f, 2.2f, -1.2f, -2.3f, 0.5f);
        g.color(0xFF2A2E38);
        bbox(g, "c3_metal", -1.7f, -1.5f, -4.1f, 1.7f, -0.8f, -2.5f, 0.2f);
        // energy core window
        float pul = 0.75f + 0.25f * sin(t * 3);
        g.push();
        g.translate(0, -4.6f, -4.62f);
        g.rotX(90);
        g.color(0xFF2A2E38);
        g.torus("c3_metal", 1.0f, 0.2f, 18, 5, 0, 0, 1, 1);
        g.pop();
        g.glow(true).color(alpha(pul, 0x4AD8FF));
        Geo.ellipsoid(g, "c3_crystal", 0, -4.6f, -4.6f, 0.9f, 0.9f, 0.25f, 14, 6);
        g.color(0xFF4AD8FF);
        bbox(g, "white", -0.18f, -8.0f, -4.72f, 0.18f, -5.8f, -4.45f, 0.05f);
        bbox(g, "white", -0.18f, -3.4f, -4.72f, 0.18f, -1.8f, -4.45f, 0.05f);
        g.glow(false);
        for (int s = -1; s <= 1; s += 2) {
            float x = s * 2.75f, z = -3.95f;
            g.push();
            g.translate(x, 0, z);
            g.color(0xFF3A3E4A);
            lathe(g, "c3_metal", new float[]{0, 1.0f, 1.42f, 1.5f, 1.5f, 1.42f, 1.0f, 0}, new float[]{-8.8f, -8.62f, -8.1f, -7.6f, -2.4f, -1.9f, -1.35f, -1.15f}, 18);
            g.color(0xFFFF8A2A);
            g.push(); g.translate(0, -3.0f, 0); g.torus("c3_metal", 1.5f, 0.14f, 18, 4, 0, 0, 1, 1); g.pop();
            g.push(); g.translate(0, -7.0f, 0); g.torus("c3_metal", 1.5f, 0.14f, 18, 4, 0, 0, 1, 1); g.pop();
            // nozzle
            g.color(0xFF22242C);
            lathe(g, "c3_metal", new float[]{0.95f, 0.72f, 0.86f, 1.08f, 0.95f}, new float[]{-8.7f, -9.1f, -9.8f, -10.45f, -10.5f}, 14);
            g.glow(true).color(0xFFFF6A1A);
            g.push(); g.translate(0, -10.35f, 0); g.torus("white", 0.85f, 0.1f, 14, 3, 0, 0, 1, 1); g.pop();
            // thruster flame
            float len = 1 + 0.18f * sin(t * 23 + s * 2) + 0.1f * sin(t * 37 + s) + mv * 0.5f;
            g.push();
            g.translate(0, -10.4f, 0);
            g.rotX(180);
            g.scale(1, len, 1);
            Cos3Models.flame(g, 3.0f, 0.78f, 0xB03A9AFF, 0xF0E8FFFF);
            g.pop();
            g.glow(false);
            // fin
            g.color(0xFFE8ECF2);
            g.push();
            g.translate(s * 1.35f, -8.0f, 0);
            g.scale(s, 1, 1);
            extrude(g, "c3_metal", new float[][]{{0, 3.2f}, {0.9f, 0.8f}, {0.9f, -1.2f}, {0, -0.4f}}, -0.14f, 0.14f);
            g.pop();
            g.pop();
            // exhaust puffs
            for (int i = 0; i < 4; i++) {
                float ph = fract(t * 1.6f + i * 0.25f + (s > 0 ? 0 : 0.12f));
                halo(g, x + sin(i * 2.3f + t) * 0.3f * ph, -12.0f - ph * 5, z - ph * 0.8f, 0.8f + ph * 1.6f, alpha((1 - ph) * 0.55, ph < 0.25f ? 0x9ADFFF : 0xB8C0D0));
            }
        }
        // status leds
        for (int i = 0; i < 3; i++) {
            boolean on = ((int) (t * 3) + i) % 3 != 0;
            g.glow(true).color(on ? (i == 1 ? 0xFFFF4A6A : 0xFF4AFF8A) : 0xFF20242A);
            ball(g, "white", -0.9f + i * 0.9f, -1.25f, -3.6f, 0.2f, 5);
        }
        g.glow(false).color(0xFFFFFFFF);
    }

    // ================================================================== lantern on the belt
    static void lantern(G g, A a) {
        float t = a.time, mv = mv(a);
        g.color(0xFFFFFFFF);
        bbox(g, "strap", -3.3f, -10.4f, -2.62f, -1.5f, -8.9f, -2.25f, 0.15f);
        g.color(0xFF3A3A42);
        Geo.tube(g, "c3_metal", new float[]{-2.4f, -9.6f, -2.6f}, new float[]{-2.4f, -9.9f, -3.7f}, 0.15f, 0.15f, 6);
        ball(g, "c3_metal", -2.4f, -9.9f, -3.7f, 0.2f, 6);
        g.push();
        g.translate(-2.4f, -10.0f, -3.7f);
        g.rotX(7 + 5 * sin(t * 2) + mv * 35);
        g.rotZ(sin(t * 1.6f) * 5);
        g.color(0xFF3A3A42);
        g.push();
        g.translate(0, -0.55f, 0);
        g.rotX(90);
        g.torus("c3_metal", 0.5f, 0.1f, 12, 4, 0, 0, 1, 1);
        g.pop();
        // roof
        g.push();
        g.translate(0, -1.2f, 0);
        g.rotY(45);
        lathe(g, "c3_metal", new float[]{0, 0.3f, 1.62f, 1.62f, 0}, new float[]{0.0f, 0.0f, -0.75f, -0.95f, -0.95f}, 4);
        g.pop();
        // plates
        bbox(g, "c3_metal", -1.1f, -2.35f, -1.1f, 1.1f, -2.05f, 1.1f, 0.08f);
        bbox(g, "c3_metal", -1.15f, -5.1f, -1.15f, 1.15f, -4.75f, 1.15f, 0.1f);
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sz = -1; sz <= 1; sz += 2)
                bbox(g, "c3_metal", sx * 0.95f - 0.14f, -4.8f, sz * 0.95f - 0.14f, sx * 0.95f + 0.14f, -2.3f, sz * 0.95f + 0.14f, 0.05f);
        // flame + glass
        float fl = 0.85f + 0.1f * sin(t * 13) + 0.05f * sin(t * 29);
        g.push();
        g.translate(0, -4.75f, 0);
        g.scale(1, fl, 1);
        Cos3Models.flame(g, 1.5f, 0.42f, 0xD0FF8A1A, 0xF8FFF0A0);
        g.pop();
        g.color(0xFFFFFFFF);
        g.glow(true);
        g.box("c3_lglass", -0.86f, -4.9f, -0.86f, 0.86f, -2.2f, 0.86f, 0, 0, 1, 1);
        halo(g, 0, -3.6f, 0, 2.8f, alpha(0.45 * fl, 0xFFB04A));
        g.glow(false);
        g.pop();
        // fireflies drawn to the light
        for (int i = 0; i < 3; i++) {
            float an = t * (1.2f + i * 0.3f) + i * 2.1f;
            Cos3Models.sparkle(g, -2.4f + cos(an) * 2.4f, -13.4f + sin(an * 1.7f) * 0.8f, -3.9f + sin(an) * 1.2f - 0.6f, 0.2f, 0xE0FFE87A);
        }
        g.color(0xFFFFFFFF);
    }
}
