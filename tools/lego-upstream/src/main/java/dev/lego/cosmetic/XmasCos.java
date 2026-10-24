package dev.lego.cosmetic;

import static dev.lego.cosmetic.Cos3Geo.*;

import dev.lego.cosmetic.Cos.A;
import dev.lego.cosmetic.Cos.Rarity;
import dev.lego.cosmetic.Cos.Slot;

/**
 * Christmas collection ("xm_*"): registration, hats and face items (head space: head x -4..4, y 0..8, z -4..4, face at z = 4).
 * Back items, auras and wings live in {@link XmasBack}, pets and the sleigh in {@link XmasPets}, capes in {@link XmasCapes}.
 */
final class XmasCos {
    private XmasCos() { }

    static void registerAll() {
        // ---------------------------------------------------------------- hats
        Cos.add("xm_hat_antlers", "Rentier-Geweih", Slot.HAT, Rarity.EPIC, XmasCos::antlers);
        Cos.add("xm_hat_elf", "Elfenmütze", Slot.HAT, Rarity.EPIC, XmasCos::elfHat);
        Cos.add("xm_hat_lightcrown", "Lichterketten-Krone", Slot.HAT, Rarity.LEGENDARY, XmasCos::lightCrown);
        Cos.add("xm_hat_tophat", "Schneemann-Zylinder", Slot.HAT, Rarity.EPIC, XmasCos::topHat);
        Cos.add("xm_hat_earmuffs", "Lebkuchen-Ohrenschützer", Slot.HAT, Rarity.RARE, XmasCos::earmuffs);
        // ---------------------------------------------------------------- face
        Cos.add("xm_face_rednose", "Rentier-Nase", Slot.FACE, Rarity.EPIC, XmasCos::redNose);
        // ---------------------------------------------------------------- back, auras, wings
        XmasBack.register();
        // ---------------------------------------------------------------- capes (texture "cape_xm_*", see XmasCapes)
        cape("winterwald", "Winterwald", Rarity.EPIC);
        cape("lebkuchenhaus", "Lebkuchenhaus", Rarity.EPIC);
        cape("zuckerstange", "Zuckerstange", Rarity.RARE);
        cape("schlittenmond", "Rentierschlitten am Mond", Rarity.LEGENDARY);
        cape("pullover", "Weihnachtspullover", Rarity.RARE);
        cape("nordlicht", "Nordlicht-Weihnacht", Rarity.LEGENDARY);
        // ---------------------------------------------------------------- pets + vehicle
        XmasPets.register();
    }

    private static void cape(String id, String name, Rarity r) {
        Cos.ALL.add(new Cos.Item("xm_cape_" + id, name, Slot.CAPE, r, Models::capePreview, "cape_xm_" + id));
    }

    // ================================================================== reindeer antlers on a velvet headband
    /** Point on the headband arc (x-y plane) at angle th (radians, 0 = right ear, PI/2 = top). */
    static float[] bandPt(double th, float rx, float ry, float y0) {
        float[] q = Geo.sq(th, 1, 5);
        return new float[]{q[0] * rx, y0 + q[1] * ry, 0.3f};
    }

    static void antlers(G g, A a) {
        float t = a.time;
        int n = 22;
        float[][] pts = new float[n + 1][];
        for (int i = 0; i <= n; i++) pts[i] = bandPt(Math.PI * i / n, 5.0f, 5.6f, 3.6f);
        float[] rr = new float[n + 1];
        java.util.Arrays.fill(rr, 0.42f);
        g.color(0xFFB8161E);
        limb(g, "xm_velvet", pts, rr, 7);
        for (int s = -1; s <= 1; s += 2) {
            float[] b = bandPt(s > 0 ? Math.PI / 3 : Math.PI * 2 / 3, 5.0f, 5.6f, 3.6f);
            g.push();
            g.translate(b[0], b[1] - 0.1f, b[2]);
            g.scale(s, 1, 1);
            // velvet socket
            g.color(0xFF6A4228);
            Geo.ellipsoid(g, "c3_fur", 0, 0.1f, 0, 0.75f, 0.6f, 0.75f, 8, 5);
            g.color(0xFFFFFFFF);
            float[][] beam = {{0, 0.2f, 0}, {0.45f, 1.4f, -0.15f}, {1.35f, 2.8f, -0.45f}, {2.15f, 4.0f, -0.55f}, {2.6f, 5.3f, -0.35f}, {2.55f, 6.5f, 0.05f}, {2.25f, 7.3f, 0.35f}};
            limb(g, "xm_antler", beam, new float[]{0.55f, 0.5f, 0.45f, 0.4f, 0.33f, 0.25f, 0.14f}, 7);
            // tines
            limb(g, "xm_antler", new float[][]{beam[1], {0.35f, 2.1f, 0.9f}, {0.2f, 2.6f, 1.5f}}, new float[]{0.36f, 0.26f, 0.12f}, 6);
            limb(g, "xm_antler", new float[][]{beam[2], {1.2f, 4.0f, 0.6f}, {1.05f, 4.9f, 1.05f}}, new float[]{0.34f, 0.25f, 0.12f}, 6);
            limb(g, "xm_antler", new float[][]{beam[3], {1.5f, 5.3f, -0.9f}, {1.25f, 6.2f, -1.0f}}, new float[]{0.3f, 0.22f, 0.1f}, 6);
            limb(g, "xm_antler", new float[][]{beam[4], {3.5f, 6.1f, -0.7f}, {3.9f, 6.7f, -0.7f}}, new float[]{0.26f, 0.18f, 0.09f}, 6);
            // bow + jingle bell hanging from the beam
            g.color(0xFFE0202A);
            Geo.ellipsoid(g, "xm_velvet", 1.62f, 2.95f, 0.05f, 0.34f, 0.28f, 0.34f, 8, 4);
            g.push();
            g.translate(1.62f, 2.75f, 0.1f);
            XmasGeo.bell(g, 0.9f, sin(t * 3.2f + s) * 16 + (a.preview ? 0 : a.move * sin(t * 12) * 20));
            g.pop();
            g.pop();
        }
        // sprig of holly on top of the band
        g.push();
        g.translate(0, 9.25f, 0.3f);
        g.rotX(18);
        XmasGeo.holly(g, 0.72f);
        g.pop();
    }

    // ================================================================== floppy elf hat with bell
    static void elfHat(G g, A a) {
        float t = a.time, mv = a.preview ? 0 : clamp(a.move, 0, 1);
        float sway = sin(t * 1.7f) * 0.22f + sin(t * 3.1f) * 0.06f;
        float back = 0.35f + mv * 0.35f + sin(t * 2.3f) * 0.05f;
        float[] ys = {5.3f, 7.0f, 8.6f, 9.6f, 10.5f};
        float[] r0 = {4.7f, 4.8f, 4.6f, 3.75f, 3.05f};
        float[] n0 = {6, 6, 4.5f, 3, 2.3f};
        int floppy = 10;
        int m = ys.length + floppy;
        float[][] sp = new float[m][];
        float[] rx = new float[m], rz = new float[m], nn = new float[m];
        for (int i = 0; i < ys.length; i++) { sp[i] = new float[]{0, ys[i], 0}; rx[i] = rz[i] = r0[i]; nn[i] = n0[i]; }
        float[] cur = sp[ys.length - 1].clone();
        for (int i = 0; i < floppy; i++) {
            float f = (i + 1) / (float) floppy;
            double ang = f * f * 2.3 * (0.8 + back * 0.6) + 0.15;
            double side = sway * f * 1.8 + 0.35 * f;
            float seg = 1.45f - 0.3f * f;
            float dx = (float) (Math.sin(side) * Math.sin(ang)), dy = (float) Math.cos(ang), dz = (float) (-Math.cos(side) * Math.sin(ang));
            cur = new float[]{cur[0] + dx * seg, cur[1] + dy * seg, cur[2] + dz * seg};
            int j = ys.length + i;
            sp[j] = cur;
            rx[j] = rz[j] = Math.max(0.08f, 2.75f * (1 - f) + 0.08f);
            nn[j] = 2;
        }
        g.color(0xFF2E9A48);
        XmasGeo.sweep(g, "xm_felt", sp, rx, rz, nn, 20, 3);
        // pointed tip + bell
        float[] e = sp[m - 1];
        g.push();
        g.translate(e[0], e[1] - 0.1f, e[2]);
        XmasGeo.bell(g, 1.05f, sin(t * 4.1f) * 20 + sway * 30);
        g.pop();
        // cuff: red band with a white zigzag + a fluffy rim
        g.color(0xFFFFFFFF);
        band(g, "xm_elfcuff", 2.6f, 2.1f, 0.62f, 4.9f, 6.7f, 0, 3);
        // two tiny golden bells on the cuff sides
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 5.45f, 5.2f, 0.4f);
            XmasGeo.bell(g, 0.55f, sin(t * 3 + s) * 12);
            g.pop();
        }
    }

    // ================================================================== crown of fairy lights
    static void lightCrown(G g, A a) {
        float t = a.time;
        float c = 2.6f, r = 2.1f, th = 0.45f;
        g.color(0xFFFFD36A);
        band(g, "xm_gold", c, r, th, 6.4f, 8.1f, 0, 4);
        g.color(0xFFFFE9A8);
        band(g, "xm_gold", c, r + th - 0.08f, 0.26f, 7.95f, 8.4f, 0, 4);
        band(g, "xm_gold", c, r + th - 0.08f, 0.26f, 6.2f, 6.6f, 0, 4);
        int chase = (int) Math.floor(t * 1.4f);
        for (int i = 0; i < 8; i++) {
            double ang = i * Math.PI / 4 + Math.PI / 8;
            float[] p = Cos3Models.loopPt(c, r + th * 0.5f, ang);
            g.push();
            g.translate(p[0], 8.35f, p[1]);
            g.rotY((float) -Math.toDegrees(ang) + 90);
            g.rotX(10);
            g.color(0xFFFFD36A);
            shard(g, "xm_gold", 0.28f, 1.1f);
            g.translate(0, 1.0f, 0);
            int col = XmasGeo.LIGHTS[(i + chase) % XmasGeo.LIGHTS.length];
            XmasGeo.bulb(g, 1.25f, col, XmasGeo.twinkle(t, i));
            g.pop();
            // gem between the prongs
            float[] q = Cos3Models.loopPt(c, r + th + 0.02f, ang + Math.PI / 8);
            g.push();
            g.translate(q[0], 7.2f, q[1]);
            g.rotY((float) -Math.toDegrees(ang + Math.PI / 8) + 90);
            g.rotX(90);
            g.glow(true).color(i % 2 == 0 ? 0xFFE0202A : 0xFF2EC85A);
            gem(g, "c3_crystal", 0.36f, 0.3f, 0.1f, 6);
            g.glow(false);
            g.pop();
        }
        // sagging light string around the band
        int n = 48;
        float[][] w = new float[n + 1][];
        for (int i = 0; i <= n; i++) {
            double an = Math.PI * 2 * i / n + Math.PI / 8;
            float[] p = Cos3Models.loopPt(c, r + th + 0.28f, an);
            float y = 8.25f - 1.25f * Math.abs(sin((an - Math.PI / 8) * 4));
            w[i] = new float[]{p[0], y, p[1]};
        }
        XmasGeo.wire(g, w, 0.09f);
        for (int i = 0; i < 8; i++) {
            double an = i * Math.PI / 4 + Math.PI / 4;
            float[] p = Cos3Models.loopPt(c, r + th + 0.42f, an);
            int col = XmasGeo.LIGHTS[(i + 3 + chase) % XmasGeo.LIGHTS.length];
            XmasGeo.fairy(g, p[0], 6.85f, p[1], 0.3f, col, XmasGeo.twinkle(t, i + 20));
        }
        // golden star floating above
        g.push();
        g.translate(0, 11.4f + sin(t * 2) * 0.3f, 0);
        g.rotY(t * 40);
        g.glow(true).color(0xFFFFD84A);
        XmasGeo.star(g, "xm_gold", 1.35f, 0.45f, 5);
        g.glow(false);
        halo(g, 0, 0, 0, 4.2f, alpha(0.5 + 0.2 * sin(t * 3), 0xFFE07A));
        g.pop();
        for (int i = 0; i < 4; i++) {
            float ph = fract(t * 0.5f + i * 0.25f), an = i * 90 + t * 40;
            Cos3Models.sparkle(g, cos(Math.toRadians(an)) * 1.8f * (1 + ph), 11.4f - ph * 2.5f, sin(Math.toRadians(an)) * 1.8f * (1 + ph), 0.25f, alpha(1 - ph, 0xFFF0B0));
        }
    }

    // ================================================================== snowman top hat with holly
    static void topHat(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(0, 8.0f, 0);
        g.rotZ(-6 + sin(t * 1.3f) * 0.8f);
        g.rotX(-3);
        g.color(0xFF2A2A34);
        lathe(g, "xm_silk", new float[]{0, 5.6f, 6.15f, 6.4f, 6.25f, 5.85f, 3.9f, 3.72f, 3.6f, 3.72f, 3.95f, 3.98f, 3.72f, 0},
                new float[]{0.05f, 0.05f, 0.15f, 0.4f, 0.64f, 0.66f, 0.56f, 0.9f, 4.0f, 6.4f, 7.2f, 7.42f, 7.62f, 7.62f}, 32);
        // red satin band + holly sprig
        g.color(0xFFD01A2A);
        Cos3Models.flatRing(g, "xm_ribbon", 3.3f, 3.84f, 1.0f, 2.3f, 28);
        g.push();
        g.rotY(-35);
        g.translate(3.95f, 1.75f, 0);
        g.rotZ(-78);
        XmasGeo.holly(g, 0.95f);
        g.pop();
        // snow on the crown and the brim
        g.color(0xFFFFFFFF);
        Geo.ellipsoid(g, "xm_snow", 0.2f, 7.62f, 0.1f, 3.55f, 0.6f, 3.5f, 16, 6);
        Geo.ellipsoid(g, "xm_snow", -1.3f, 8.05f, 0.6f, 1.4f, 0.55f, 1.3f, 10, 5);
        float[][] patches = {{4.7f, 0.6f, 1.1f}, {-3.0f, -3.9f, 1.3f}};
        for (float[] pch : patches) Geo.ellipsoid(g, "xm_snow", pch[0], 0.6f, pch[1], pch[2], 0.2f, pch[2] * 0.7f, 10, 4);
        g.pop();
        // a few snowflakes dancing around the hat
        for (int i = 0; i < 5; i++) {
            float ph = fract(t * 0.22f + i * 0.2f), an = i * 72 + t * 25;
            float x = cos(Math.toRadians(an)) * 7.2f, z = sin(Math.toRadians(an)) * 7.2f, y = 17 - ph * 10;
            g.push();
            g.translate(x, y, z);
            g.rotY(an + t * 60);
            g.glow(true).color(alpha(smooth(ph * 5) * smooth((1 - ph) * 5) * 0.95, 0xF0F8FF));
            g.cross("xm_flake", 0, 0, 0, 1.3f, 1.3f, 0, 0, 1, 1);
            g.glow(false);
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    // ================================================================== gingerbread earmuffs
    static void earmuffs(G g, A a) {
        float t = a.time;
        int n = 24;
        float[][] pts = new float[n + 1][];
        for (int i = 0; i <= n; i++) pts[i] = new float[]{bandPt(Math.PI * i / n, 5.0f, 5.9f, 3.4f)[0], bandPt(Math.PI * i / n, 5.0f, 5.9f, 3.4f)[1], 0};
        float[] rr = new float[n + 1];
        java.util.Arrays.fill(rr, 0.42f);
        g.color(0xFFFFFFFF);
        Geo.chain(g, "xm_candy", pts, rr, 8);
        float[][] gm = new float[XmasTex.GMAN.length][];
        for (int i = 0; i < gm.length; i++) gm[i] = new float[]{XmasTex.GMAN[i][0] * 2.35f, XmasTex.GMAN[i][1] * 2.35f};
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 5.0f, 3.4f, 0);
            // fluffy muff
            g.color(0xFFF6F2EE);
            Geo.ellipsoid(g, "xm_fluff", 0, 0, 0, 0.95f, 2.45f, 2.45f, 14, 9);
            // cookie on the outside, wobbling a little
            g.push();
            g.translate(s * 0.72f, 0, 0);
            g.rotY(s * 90);
            g.rotZ(sin(t * 2.2f + s) * 6);
            g.color(0xFFFFFFFF);
            extrude(g, "xm_gingerman", gm, -0.05f, 0.62f, FULL, new float[]{0.01f, 0.5f, 0.04f, 0.53f});
            g.pop();
            g.pop();
        }
    }

    // ================================================================== glowing reindeer nose
    static void redNose(G g, A a) {
        float t = a.time;
        float pul = 0.5f + 0.5f * sin(t * 3.4f);
        float s = 1 + 0.07f * pul;
        g.push();
        g.translate(0, 3.1f, 4.72f);
        g.scale(s);
        g.glow(true).color(0xFF000000 | Capes.mix(0xC0101C, 0xFF4A4A, pul));
        g.sphere("xm_gloss", 1.08f, 16, 10, 0, 0, 1, 1);
        g.color(0xE8FFFFFF);
        ball(g, "white", -0.38f, 0.42f, 0.86f, 0.2f, 6);
        g.glow(false);
        g.pop();
        halo(g, 0, 3.1f, 5.2f, 3.4f + pul * 1.6f, alpha(0.35 + 0.35 * pul, 0xFF3A3A));
        for (int i = 0; i < 3; i++) {
            float ph = fract(t * 0.6f + i / 3f);
            Cos3Models.sparkle(g, sin(i * 2.1f + t) * 1.5f, 3.4f + ph * 3, 5.6f + ph * 0.6f, 0.2f, alpha((1 - ph) * 0.9, 0xFF8A8A));
        }
        g.color(0xFFFFFFFF);
    }
}
