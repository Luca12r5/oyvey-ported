package dev.lego.cosmetic;

import static dev.lego.cosmetic.Cos3Geo.*;

import dev.lego.cosmetic.Cos.A;
import dev.lego.cosmetic.Cos.Rarity;
import dev.lego.cosmetic.Cos.Slot;
import dev.lego.cosmetic.Pets.P;

/**
 * Christmas pets (pet space: px, origin at the feet, pet faces +z) and the Santa sleigh
 * (vehicle space: origin on the ground below the player, z forward; the player sits, hips at {@link #SLEIGH_HIP}).
 */
final class XmasPets {
    private XmasPets() { }

    /** Hip (leg pivot) height of the rider in the sleigh; the seat surface is 2 px lower. */
    static final float SLEIGH_HIP = 8.0f;

    static void register() {
        Cos.add("xm_pet_snowman", "Schneemann", Slot.PET, Rarity.EPIC, Pets.grow(XmasPets::snowman, 1.5f, 3.8f));
        Cos.add("xm_pet_reindeer", "Rentier", Slot.PET, Rarity.LEGENDARY, XmasPets::reindeer);
        Cos.add("xm_pet_gingerbread", "Lebkuchenmann", Slot.PET, Rarity.EPIC, Pets.grow(XmasPets::gingerbread, 1.5f, 3.6f));
        Cos.add("xm_pet_giftmonster", "Geschenk-Monster", Slot.PET, Rarity.LEGENDARY, Pets.grow(XmasPets::giftMonster, 1.45f, 4.2f));
        Cos.add("xm_pet_penguin", "Pinguin mit Mütze", Slot.PET, Rarity.EPIC, Pets.grow(XmasPets::santaPenguin, 1.55f, 3.4f));
        Cos.add("xm_veh_sleigh", "Weihnachtsschlitten", Slot.VEHICLE, Rarity.LEGENDARY, XmasPets::sleigh);
    }

    private static float[] p3(float x, float y, float z) { return new float[]{x, y, z}; }

    // ================================================================== snowman (hops)
    static void snowman(G g, A a) {
        P p = Pets.pose(a);
        float[] h = PetRig.hop(p);
        p.y += h[0] * 1.8f;
        p.stepBob = 0;
        float sq = -h[1] * 0.12f + sin(p.t * 2.2f) * 0.015f;
        g.push();
        Pets.root(g, p);
        g.color(0xFFFFFFFF);
        // bottom ball
        g.push();
        g.scale(1 + sq, 1 - sq, 1 + sq);
        Geo.ellipsoid(g, "xm_snow", 0, 2.25f, 0, 2.45f, 2.3f, 2.4f, 16, 10);
        g.pop();
        float lag = h[1] * 0.25f;
        g.push();
        g.translate(0, 2.25f * (1 - sq) + 0.1f, 0);
        g.rotZ(p.waddle * 0.6f + sin(p.t * 1.5f) * 2);
        // middle ball + coal buttons
        Geo.ellipsoid(g, "xm_snow", 0, 2.75f - lag * 0.4f, 0, 1.85f, 1.8f, 1.8f, 14, 9);
        g.color(0xFF2A2A30);
        for (int i = 0; i < 3; i++) {
            double el = 0.55 - i * 0.45;
            ball(g, "xm_coal", 0, 2.75f - lag * 0.4f + (float) Math.sin(el) * 1.8f, (float) Math.cos(el) * 1.8f + 0.02f, 0.24f, 6);
        }
        // stick arms
        for (int s = -1; s <= 1; s += 2) {
            boolean wave = s > 0 && (p.is("happy") || p.is("excited") || p.is("love"));
            float ang = wave ? 55 + sin(p.t * 7) * 22 : 20 + sin(p.t * 2 + s) * 6 + h[2] * 30;
            if (p.is("sad")) ang = -25;
            g.push();
            g.translate(s * 1.6f, 3.25f, 0);
            g.rotZ(s * ang);
            g.scale(s, 1, 1);
            g.color(0xFF6A4222);
            limb(g, "xm_bark", new float[][]{{0, 0, 0}, {1.4f, 0.1f, 0}, {2.9f, 0.35f, 0.1f}}, new float[]{0.2f, 0.17f, 0.12f}, 5);
            limb(g, "xm_bark", new float[][]{{2.2f, 0.25f, 0.05f}, {2.8f, 0.95f, 0.1f}}, new float[]{0.1f, 0.07f}, 4);
            limb(g, "xm_bark", new float[][]{{2.5f, 0.3f, 0.05f}, {3.1f, -0.2f, 0.2f}}, new float[]{0.09f, 0.06f}, 4);
            g.pop();
        }
        g.color(0xFFFFFFFF);
        // head
        g.push();
        g.translate(0, 5.55f - lag, 0);
        Pets.headTurn(g, p);
        float hr = 1.5f;
        Geo.ellipsoid(g, "xm_snow", 0, 0, 0, hr, hr * 0.97f, hr, 14, 9);
        // coal eyes (blink, follow the look)
        float bl = p.closed ? 0.15f : 1;
        g.color(0xFF1E1E24);
        for (int s = -1; s <= 1; s += 2) {
            double az = s * 0.36 + p.lookX * 0.12, el = 0.28 + p.lookY * 0.08;
            g.push();
            g.translate((float) (Math.sin(az) * Math.cos(el)) * hr, (float) Math.sin(el) * hr, (float) (Math.cos(az) * Math.cos(el)) * hr);
            g.scale(1, bl, 1);
            g.sphere("xm_coal", 0.24f, 6, 4, 0, 0, 1, 1);
            g.pop();
        }
        // coal smile (frown when sad)
        float curve = p.is("sad") ? -0.18f : p.is("angry") ? -0.06f : 0.2f;
        for (int i = 0; i < 5; i++) {
            double az = (i - 2) * 0.24, el = -0.28 - curve * (1 - Math.pow((i - 2) / 2.0, 2)) + curve;
            ball(g, "xm_coal", (float) (Math.sin(az) * Math.cos(el)) * hr * 0.99f, (float) Math.sin(el) * hr * 0.99f, (float) (Math.cos(az) * Math.cos(el)) * hr * 0.99f, 0.13f, 4);
        }
        // carrot nose
        g.push();
        g.translate(0, 0.02f, hr * 0.92f);
        g.rotX(90);
        g.color(0xFFFFFFFF);
        lathe(g, "xm_carrot", new float[]{0.3f, 0.28f, 0.2f, 0.1f, 0}, new float[]{0, 0.4f, 0.9f, 1.4f, 1.8f}, 8);
        g.pop();
        // little top hat
        g.push();
        g.translate(0, hr * 0.84f, -0.1f);
        g.rotZ(-12);
        g.color(0xFF26262E);
        lathe(g, "xm_silk", new float[]{0, 1.55f, 1.62f, 1.5f, 1.0f, 0.95f, 1.02f, 0}, new float[]{0.0f, 0.0f, 0.12f, 0.22f, 0.25f, 1.8f, 1.9f, 1.9f}, 16);
        g.color(0xFFD01A2A);
        Cos3Models.flatRing(g, "xm_ribbon", 0.9f, 1.03f, 0.35f, 0.75f, 16);
        g.pop();
        g.pop();
        // scarf around the neck
        g.push();
        g.translate(0, 4.35f - lag * 0.7f, 0);
        g.color(0xFFFFFFFF);
        g.torus("xm_scarf", 1.2f, 0.42f, 16, 6, 0, 0, 1, 1);
        g.translate(0.8f, -0.1f, 0.9f);
        ribbon(g, "xm_scarf", 0, new float[][]{{0, 0.1f}, {-0.9f, 0.45f}, {-1.8f, 0.55f + h[2] * 0.4f}}, new float[]{0.7f, 0.75f, 0.8f}, 0.18f);
        g.pop();
        g.pop();
        Pets.fx(g, p, 9.6f);
        g.pop();
    }

    // ================================================================== reindeer (walks, glowing red nose)
    static void reindeer(G g, A a) {
        P p = Pets.pose(a);
        PetReal.R4 r = new PetReal.R4();
        r.body = Pets.fur("deer"); r.head = Pets.head("deer"); r.muzzle = Pets.col(0xB88458); r.legUp = Pets.col(0x9A6440); r.legLo = Pets.col(0xB07A50); r.paw = Pets.gl(0x2A2020);
        r.style = "deer"; r.nose = 0xE0202A; r.grazer = true;
        r.L = 6.2f; r.legX = 1.05f; r.zs = 3.0f; r.zh = -3.1f; r.rx = 1.8f; r.ry = 2.15f; r.hipF = 0.95f;
        r.neckLen = 3.0f; r.neckAng = 28; r.neckR = 0.98f;
        r.skRx = 1.32f; r.skRy = 1.3f; r.skRz = 1.45f; r.snLen = 1.8f; r.snR = 0.7f; r.snY = -0.32f; r.headDown = 14;
        r.r1f = 0.58f; r.r2f = 0.3f; r.r1h = 0.82f; r.r2h = 0.32f; r.pawR = 0.4f; r.hoof = true;
        r.span = 1.05f; r.eyeEl = 0.26f;
        r.babyS = 0.56f; r.babyLeg = 0.75f; r.babyHead = 1.6f;
        final float k = p.gr;
        r.ears = (gg, pp) -> {
            for (int s = -1; s <= 1; s += 2) Pets.earPointy(gg, pp, Pets.col(0x9A6440), Pets.col(0xF6E8DA), s * 1.0f, 0.8f, -0.45f, s, 0.7f, 1.6f, 62);
        };
        r.headExtra = (gg, pp) -> {
            reindeerAntlers(gg, pp);
            // glowing nose on the tip of the snout
            float sf = PetRig.lerp(r.babySnout, 1, pp.gr);
            float sl = r.snLen * sf, sc = r.skRz * 0.42f + sl * 0.5f;
            float srx = r.snR * PetRig.lerp(1.1f, 1, pp.gr), sry = r.snR * 0.8f, srz = sl * 0.55f + r.snR * 0.3f;
            float pul = 0.5f + 0.5f * sin(pp.t * 3.4f);
            float nr = srx * 0.52f * (1 + 0.08f * pul);
            float ny = r.snY + sry * 0.42f, nz = sc + srz * 0.9f;
            boolean og = gg.glow;
            int oc = gg.color;
            gg.glow(true).color(0xFF000000 | Capes.mix(0xC8101C, 0xFF4A4A, pul));
            Geo.ellipsoid(gg, "xm_gloss", 0, ny, nz, nr * 1.15f, nr, nr, 10, 6);
            halo(gg, 0, ny, nz + 0.2f, 2.4f + pul, alpha(0.35 + 0.3 * pul, 0xFF3A3A));
            gg.glow(og).color(oc);
        };
        r.neckExtra = (gg, pp) -> {
            // red collar with a golden bell
            float nr = r.neckR * PetRig.lerp(1.1f, 1, pp.gr);
            gg.push();
            gg.translate(0, r.neckLen * PetRig.lerp(r.babyNeck, 1, pp.gr) * 0.2f, 0);
            gg.rotX(-10);
            gg.color(0xFFD01A2A);
            gg.torus("xm_velvet", nr * 1.02f, 0.24f, 14, 5, 0, 0, 1, 1);
            gg.translate(0, -0.15f, nr * 1.02f + 0.2f);
            XmasGeo.bell(gg, 0.55f, sin(pp.t * 5) * 18 * (0.3f + pp.walkW));
            gg.color(0xFFFFFFFF);
            gg.pop();
        };
        r.bodyExtra = (gg, pp) -> {
            // saddle blanket
            float bf = PetRig.lerp(r.babyLen, 1, pp.gr), fat = PetRig.lerp(1.18f, 1, pp.gr);
            float cz = (r.zs * bf * 0.45f + r.zh * bf * 0.5f) / 2, rx = r.rx * fat, ry = r.ry * fat;
            gg.color(0xFFC8141E);
            Geo.ellipsoid(gg, "xm_velvet", 0, ry * 0.38f, cz, rx * 1.07f, ry * 0.72f, 1.55f * bf, 12, 6);
            gg.color(0xFFFFD36A);
            Geo.ellipsoid(gg, "xm_gold", 0, ry * 0.38f - 0.05f, cz, rx * 1.1f, ry * 0.5f, 1.62f * bf, 12, 3);
            gg.color(0xFFFFFFFF);
        };
        r.tail = (gg, pp) -> {
            gg.rotX(35 + pp.tailLift * 0.5f + (pp.is("excited") || pp.is("angry") ? 40 : 0));
            PetGeo.blob(gg, Pets.col(0xFFFFFF), 0, 0, -0.45f, 0.45f, 0.38f, 0.62f, 6, 3);
        };
        PetReal.real4(g, p, r);
        if (k > 1) g.color(0xFFFFFFFF);
    }

    /** Big branched antlers (always branched, grow with age). Head space of the realistic head. */
    static void reindeerAntlers(G g, P p) {
        float k = p.gr, S = 0.55f + 0.55f * k;
        int oc = g.color;
        g.color(0xFFFFFFFF);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 0.62f, 1.0f, -0.15f);
            g.scale(s * S, S, S);
            g.rotZ(-18);
            float[][] beam = {{0, 0, 0}, {0.3f, 1.1f, -0.3f}, {0.9f, 2.2f, -0.8f}, {1.4f, 3.2f, -0.9f}, {1.6f, 4.2f, -0.6f}, {1.4f, 5.0f, -0.2f}};
            limb(g, "xm_antler", beam, new float[]{0.24f, 0.22f, 0.19f, 0.16f, 0.12f, 0.06f}, 6);
            limb(g, "xm_antler", new float[][]{beam[1], {0.2f, 1.8f, 0.6f}, {0.1f, 2.2f, 1.0f}}, new float[]{0.15f, 0.11f, 0.05f}, 5);
            limb(g, "xm_antler", new float[][]{beam[2], {0.6f, 3.3f, 0.1f}, {0.5f, 3.9f, 0.4f}}, new float[]{0.14f, 0.1f, 0.05f}, 5);
            limb(g, "xm_antler", new float[][]{beam[3], {2.3f, 3.9f, -1.1f}, {2.6f, 4.4f, -1.1f}}, new float[]{0.13f, 0.09f, 0.04f}, 5);
            g.pop();
        }
        g.color(oc);
    }

    // ================================================================== gingerbread man (walks)
    static void gingerbread(G g, A a) {
        P p = Pets.pose(a);
        float ww = p.walkW;
        double cyc = Math.PI * 2 * PetRig.frac(p.t * 1.7f);
        float swing = sin(cyc) * 38 * ww;
        float bob = Math.abs(sin(cyc)) * 0.35f * ww + (p.is("excited") ? Math.abs(sin(p.t * 6.8f)) * 0.8f : 0);
        g.push();
        Pets.root(g, p);
        g.translate(0, bob, 0);
        g.rotZ(sin(cyc) * 4 * ww);
        g.color(0xFFFFFFFF);
        String tx = "xm_ginger";
        float th = 0.62f;
        // legs
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 0.95f, 3.3f, 0);
            g.rotX(s * swing - p.sit * 80);
            g.scale(1, 1, th);
            limb(g, tx, new float[][]{{0, 0, 0}, {s * 0.12f, -1.5f, 0}, {s * 0.25f, -2.75f, 0}}, new float[]{0.82f, 0.78f, 0.74f}, 8);
            icing(g, s * 0.25f, -2.3f, 0.78f);
            g.pop();
        }
        // body
        Geo.ellipsoid(g, tx, 0, 4.9f, 0, 1.95f, 2.15f, 0.78f, 14, 8);
        // gumdrop buttons
        int[] bc = {0xFFE0202A, 0xFF2EB84A, 0xFFE0202A};
        for (int i = 0; i < 3; i++) {
            g.color(bc[i]);
            float y = 5.9f - i * 0.95f;
            float zz = 0.78f * (float) Math.sqrt(Math.max(0.05, 1 - Math.pow((y - 4.9f) / 2.15f, 2)));
            Geo.ellipsoid(g, "xm_gloss", 0, y, zz + 0.05f, 0.34f, 0.3f, 0.2f, 8, 5);
        }
        g.color(0xFFFFFFFF);
        // arms
        for (int s = -1; s <= 1; s += 2) {
            boolean wave = s > 0 && (p.is("happy") || p.is("excited")) && ww < 0.3f;
            float raise = wave ? 75 + sin(p.t * 8) * 25 : sin(p.t * 2 + s) * 5 - 5;
            g.push();
            g.translate(s * 1.55f, 6.0f, 0);
            g.rotX(-s * swing * 0.8f);
            g.rotZ(s * raise);
            g.scale(1, 1, th);
            float[][] arm = {{0, 0, 0}, {s * 1.2f, -0.55f, 0}, {s * 2.3f, -0.95f, 0}};
            limb(g, tx, arm, new float[]{0.72f, 0.68f, 0.64f}, 8);
            icing2(g, s * 1.95f, -0.83f, 0.66f);
            g.pop();
        }
        // bow tie
        g.push();
        g.translate(0, 6.85f, 0.62f);
        XmasGeo.bow(g, 0xFFE0202A, 0.45f);
        g.pop();
        // head with icing face
        g.push();
        g.translate(0, 8.4f, 0);
        Pets.headTurn(g, p);
        g.color(0xFFFFFFFF);
        Geo.ellipsoid(g, tx, 0, 0, 0, 1.85f, 1.75f, 0.8f, 14, 8);
        float bl = p.closed ? 0.2f : 1;
        g.color(0xFFFFFFFF);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 0.62f + p.lookX * 0.12f, 0.35f + p.lookY * 0.1f, 0.7f);
            g.scale(1, bl, 0.5f);
            g.sphere("white", 0.27f, 8, 4, 0, 0, 1, 1);
            g.pop();
        }
        float curve = p.is("sad") ? -0.3f : 0.35f;
        int n = 7;
        float[][] m = new float[n][];
        for (int i = 0; i < n; i++) {
            float f = (i - (n - 1) / 2f) / ((n - 1) / 2f);
            float y = -0.45f - curve * (1 - f * f);
            m[i] = p3(f * 0.8f, y, 0.8f * (float) Math.sqrt(Math.max(0.05, 1 - Math.pow(f * 0.8 / 1.85, 2) - Math.pow(y / 1.75, 2))) + 0.04f);
        }
        float[] mr = new float[n];
        java.util.Arrays.fill(mr, 0.1f);
        limb(g, "white", m, mr, 4);
        // blush
        g.color(0xFFF08A8A);
        for (int s = -1; s <= 1; s += 2) Geo.ellipsoid(g, "white", s * 1.1f, -0.25f, 0.62f, 0.28f, 0.2f, 0.12f, 6, 3);
        g.pop();
        Pets.fx(g, p, 10.6f);
        g.pop();
        g.color(0xFFFFFFFF);
    }

    /** Icing zigzag ring around a vertical limb end (local limb space, flattened z already applied by the caller). */
    private static void icing(G g, float x, float y, float r) {
        int n = 12;
        float[][] pts = new float[n + 1][];
        for (int i = 0; i <= n; i++) {
            double an = Math.PI * 2 * i / n;
            pts[i] = p3(x + (float) Math.cos(an) * (r + 0.04f), y + (i % 2 == 0 ? 0.22f : -0.22f), (float) Math.sin(an) * (r + 0.04f));
        }
        float[] rr = new float[n + 1];
        java.util.Arrays.fill(rr, 0.09f);
        int oc = g.color;
        g.color(0xFFFFFFFF);
        Geo.chain(g, "white", pts, rr, 4);
        g.color(oc);
    }

    /** Icing zigzag ring around a horizontal limb (arm) end. */
    private static void icing2(G g, float x, float y, float r) {
        int n = 12;
        float[][] pts = new float[n + 1][];
        for (int i = 0; i <= n; i++) {
            double an = Math.PI * 2 * i / n;
            pts[i] = p3(x + (i % 2 == 0 ? 0.2f : -0.2f), y + (float) Math.cos(an) * (r + 0.04f), (float) Math.sin(an) * (r + 0.04f));
        }
        float[] rr = new float[n + 1];
        java.util.Arrays.fill(rr, 0.09f);
        int oc = g.color;
        g.color(0xFFFFFFFF);
        Geo.chain(g, "white", pts, rr, 4);
        g.color(oc);
    }

    // ================================================================== gift box monster (hops, lid chomps)
    static void giftMonster(G g, A a) {
        P p = Pets.pose(a);
        float[] h = PetRig.hop(p);
        p.y += h[0] * 1.6f;
        p.stepBob = 0;
        float sq = -h[1] * 0.1f;
        float W = 5.0f, H = 4.0f, D = 4.6f;
        g.push();
        Pets.root(g, p);
        // feet
        g.color(0xFFFFFFFF);
        for (int s = -1; s <= 1; s += 2) {
            float c = PetRig.frac(p.t * PetRig.WADDLE_HZ + (s > 0 ? 0 : 0.5f));
            float lift = p.walkW * Math.max(0, sin(Math.PI * 2 * c)) * 0.5f;
            PetGeo.blob(g, Pets.gl(0x2E9A48), s * 1.4f, 0.45f + lift, 0.4f, 0.75f, 0.5f, 1.0f, 8, 4);
        }
        g.push();
        g.translate(0, 0.75f, 0);
        g.scale(1 + sq, 1 - sq, 1 + sq);
        g.rotZ(p.waddle * 0.5f);
        // body
        g.color(0xFFFFFFFF);
        bbox(g, "xm_gift:D8202A:FFD24A:1", -W / 2, 0, -D / 2, W / 2, H, D / 2, 0.22f);
        float wr = 0.55f;
        g.color(0xFFFFD36A);
        g.box("xm_ribbon", -W / 2 - 0.08f, -0.08f, -wr, W / 2 + 0.08f, H + 0.08f, wr, 0, 0, 1, 1);
        // mouth interior + teeth (visible when the lid opens)
        g.color(0xFF3A0A10);
        g.box("xm_felt", -W / 2 + 0.3f, H - 0.35f, -D / 2 + 0.3f, W / 2 - 0.3f, H + 0.14f, D / 2 - 0.3f, 0, 0, 1, 1);
        float open = 12 + 26 * h[2] + (p.is("excited") ? 18 * Math.abs(sin(p.t * 7)) : 0) + (p.is("angry") ? 14 * Math.abs(sin(p.t * 12)) : 0)
                + (p.is("sleepy") ? -12 : 0) + 6 * sin(p.t * 1.7f);
        open = Math.max(0, open);
        float lit = Math.min(1, open / 25f);
        if (lit > 0.05f) {
            g.glow(true).color(alpha(0.9 * lit, 0xFFE07A));
            g.box("white", -W / 2 + 0.45f, H + 0.2f, -D / 2 + 0.45f, W / 2 - 0.45f, H + 0.25f, D / 2 - 0.45f, 0, 0, 1, 1);
            g.glow(false);
            halo(g, 0, H + 0.8f, 0, 5.5f * lit, alpha(0.45 * lit, 0xFFD86A));
            for (int i = 0; i < 4; i++) {
                float ph = fract(p.t * 0.7f + i * 0.25f);
                Cos3Models.sparkle(g, sin(i * 2.4f + p.t) * 1.4f, H + 0.5f + ph * 4, cos(i * 1.9f) * 1.2f, 0.28f, alpha((1 - ph) * lit, 0xFFF0B0));
            }
        }
        g.color(0xFFFFFFFF);
        for (int i = 0; i < 5; i++) {
            g.push();
            g.translate(-1.6f + i * 0.8f, H + 0.12f, D / 2 - 0.45f);
            PetGeo.cone(g, "white", 0.26f, 0.55f, 5, false);
            g.pop();
        }
        // face on the front
        Pets.faceFlat(g, p, "std", 0, H * 0.46f, D / 2 + 0.08f, W * 0.86f, H * 0.78f);
        // lid (hinged at the back)
        g.push();
        g.translate(0, H + 0.02f, -D / 2 - 0.15f);
        g.rotX(-open);
        g.translate(0, 0, D / 2 + 0.15f);
        float LW = W + 0.5f, LD = D + 0.5f, LH = 1.1f;
        bbox(g, "xm_gift:D8202A:FFD24A:1", -LW / 2, 0, -LD / 2, LW / 2, LH, LD / 2, 0.2f);
        g.color(0xFFFFD36A);
        g.box("xm_ribbon", -wr, -0.06f, -LD / 2 - 0.06f, wr, LH + 0.06f, LD / 2 + 0.06f, 0, 0, 1, 1);
        g.box("xm_ribbon", -LW / 2 - 0.12f, -0.12f, -wr, LW / 2 + 0.12f, LH + 0.12f, wr, 0, 0, 1, 1);
        // teeth hanging from the lid front
        g.color(0xFFFFFFFF);
        for (int i = 0; i < 4; i++) {
            g.push();
            g.translate(-1.2f + i * 0.8f, 0.02f, LD / 2 - 0.55f);
            g.rotX(180);
            PetGeo.cone(g, "white", 0.26f, 0.5f, 5, false);
            g.pop();
        }
        g.translate(0, LH + 0.12f, 0);
        XmasGeo.bow(g, 0xFFFFD36A, 1.25f);
        g.pop();
        g.pop();
        Pets.fx(g, p, H + 3.5f);
        g.pop();
        g.color(0xFFFFFFFF);
    }

    // ================================================================== penguin with a santa hat
    static void santaPenguin(G g, A a) {
        PetModels.penguin(g, a);
        P p = Pets.pose(a);
        float k = p.gr;
        g.push();
        Pets.root(g, p);
        g.translate(0, p.waddleBob * 0.7f, 0);
        float by = 3.55f + k * 1.1f - p.lie * 0.6f, ry = 3.3f + k * 1.3f;
        g.translate(0, by, 0);
        g.rotZ(p.waddle * 1.2f);
        g.rotY(p.waddle * 0.5f);
        g.rotY(p.headYaw * 0.6f);
        g.rotX(p.headPitch * 0.4f);
        g.rotZ(p.headRoll * 0.8f);
        // santa hat on the crown
        g.translate(0, ry * 0.78f, -0.25f);
        g.rotX(-14);
        g.rotZ(8);
        float sway = sin(p.t * 2.1f) * 0.25f + p.walkW * sin(p.t * 9) * 0.15f;
        int m = 9;
        float[][] sp = new float[m][];
        float[] rr = new float[m];
        float[] cur = {0, 0, 0};
        for (int i = 0; i < m; i++) {
            float f = i / (float) (m - 1);
            double ang = f * f * 1.9 + 0.1;
            if (i > 0) {
                float seg = 0.55f;
                cur = new float[]{cur[0] + (float) (Math.sin(sway * f) * Math.sin(ang)) * seg, cur[1] + (float) Math.cos(ang) * seg, cur[2] - (float) (Math.cos(sway * f) * Math.sin(ang)) * seg};
            }
            sp[i] = cur;
            rr[i] = 1.75f * (1 - f) + 0.06f;
        }
        g.color(0xFFD8202A);
        XmasGeo.sweep(g, "xm_felt", sp, rr, rr, null, 14, 2);
        g.color(0xFFFFFFFF);
        g.push();
        g.translate(0, 0.08f, 0);
        g.torus("xm_fluff", 1.72f, 0.42f, 18, 6, 0, 0, 1, 1);
        g.pop();
        float[] e = sp[m - 1];
        ball(g, "xm_fluff", e[0], e[1] - 0.1f, e[2], 0.5f, 8);
        g.pop();
    }

    // ================================================================== Santa sleigh
    /** Side panel outline in (z, y). */
    private static final float[][] SIDE = {
            {-8.4f, 2.5f}, {9.4f, 2.5f}, {10.9f, 3.3f}, {11.9f, 5.0f}, {12.3f, 7.0f}, {12.0f, 8.8f}, {11.1f, 9.7f}, {10.1f, 9.6f}, {9.6f, 8.9f},
            {9.9f, 8.2f}, {10.5f, 8.2f}, {10.6f, 7.4f}, {9.8f, 6.6f}, {7.5f, 6.4f}, {3.0f, 6.9f}, {-1.6f, 7.5f}, {-3.4f, 8.6f},
            {-4.8f, 11.2f}, {-6.2f, 12.9f}, {-7.8f, 13.4f}, {-9.2f, 12.6f}, {-9.4f, 11.4f}, {-8.6f, 10.8f}, {-8.0f, 11.5f}, {-8.9f, 8.0f}, {-8.9f, 4.0f}};

    static void sleigh(G g, A a) {
        float t = a.time, mv = clamp(a.move, 0, 1);
        g.push();
        g.rotZ(-a.lean * 4);
        g.translate(0, mv * 0.15f * Math.abs(sin(t * 7)), 0);
        // ---- runners (golden, curled at the front)
        float[][] run = {{0, 2.6f, -10.3f}, {0, 1.6f, -10.2f}, {0, 0.7f, -9.2f}, {0, 0.5f, -7.5f}, {0, 0.5f, 5.0f}, {0, 0.6f, 8.6f}, {0, 1.1f, 10.6f},
                {0, 2.4f, 12.3f}, {0, 4.4f, 13.4f}, {0, 6.6f, 13.8f}, {0, 8.3f, 13.2f}, {0, 9.0f, 12.2f}, {0, 8.7f, 11.2f}, {0, 7.9f, 10.9f}, {0, 7.4f, 11.4f}};
        for (int s = -1; s <= 1; s += 2) {
            float[][] pts = new float[run.length][];
            for (int i = 0; i < run.length; i++) pts[i] = new float[]{s * 4.7f, run[i][1], run[i][2]};
            float[] rr = new float[pts.length];
            java.util.Arrays.fill(rr, 0.42f);
            rr[rr.length - 1] = 0.3f;
            g.color(0xFFFFD36A);
            limb(g, "xm_gold", pts, rr, 6);
            // struts
            for (float z : new float[]{-6.2f, -0.5f, 5.2f}) {
                Geo.tube(g, "xm_gold", new float[]{s * 4.7f, 0.6f, z}, new float[]{s * 4.7f, 2.6f, z + 0.4f}, 0.26f, 0.26f, 6);
                ball(g, "xm_gold", s * 4.7f, 2.55f, z + 0.4f, 0.36f, 6);
            }
        }
        // ---- body: floor, side panels, seat, backrest, front board
        g.color(0xFF7A0A12);
        bbox(g, "xm_red_lacquer", -4.95f, 2.5f, -8.6f, 4.95f, 3.3f, 9.8f, 0.2f);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.rotY(-90);
            g.color(0xFFFFFFFF);
            float xa = s > 0 ? 4.95f : -5.55f, xb = xa + 0.6f;
            extrude(g, "xm_sleighside", SIDE, -xb, -xa, FULL, new float[]{0.0f, 0.05f, 0.02f, 0.07f});
            g.pop();
            // golden trim along the upper edge
            int[] idx = {13, 14, 15, 16, 17, 18, 19, 20};
            float[][] tp = new float[idx.length][];
            for (int i = 0; i < idx.length; i++) tp[i] = new float[]{s * 5.25f, SIDE[idx[i]][1] + 0.05f, SIDE[idx[i]][0]};
            float[] tr = new float[tp.length];
            java.util.Arrays.fill(tr, 0.2f);
            g.color(0xFFFFD36A);
            limb(g, "xm_gold", tp, tr, 5);
        }
        g.color(0xFFA01018);
        bbox(g, "xm_velvet", -4.5f, 3.2f, -3.2f, 4.5f, SLEIGH_HIP - 2, 3.6f, 0.45f);
        bbox(g, "xm_velvet", -4.4f, 5.5f, -4.0f, 4.4f, 14.2f, -2.45f, 0.5f);
        g.color(0xFFFFD36A);
        bbox(g, "xm_gold", -4.55f, 13.9f, -4.1f, 4.55f, 14.5f, -2.35f, 0.15f);
        g.color(0xFF9A0E16);
        bbox(g, "xm_red_lacquer", -4.9f, 3.2f, 9.3f, 4.9f, 5.5f, 10.1f, 0.2f);
        g.color(0xFFFFD36A);
        bbox(g, "xm_gold", -4.95f, 5.4f, 9.2f, 4.95f, 5.75f, 10.2f, 0.1f);
        // ---- gift sack on the rear deck
        g.color(0xFFB08858);
        Geo.ellipsoid(g, "xm_sack", 0, 7.9f, -6.3f, 3.5f, 3.4f, 2.3f, 12, 8);
        Geo.ellipsoid(g, "xm_sack", 1.4f, 9.6f, -6.6f, 2.0f, 2.0f, 1.6f, 8, 5);
        Geo.ellipsoid(g, "xm_sack", -1.3f, 9.4f, -6.1f, 1.9f, 2.1f, 1.5f, 8, 5);
        g.push();
        g.translate(0, 11.3f, -6.4f);
        lathe(g, "xm_sack", new float[]{1.4f, 0.8f, 0.7f, 1.0f, 1.25f}, new float[]{0, 0.6f, 1.0f, 1.5f, 1.9f}, 12);
        g.color(0xFFFFD36A);
        g.translate(0, 0.8f, 0);
        g.torus("xm_gold", 0.82f, 0.18f, 12, 4, 0, 0, 1, 1);
        g.pop();
        // presents peeking out
        g.push();
        g.translate(0.4f, 12.3f, -6.4f);
        g.rotZ(14);
        g.rotY(20);
        XmasBack.gift(g, "xm_gift:2E9A48:FFFFFF:0", 0xFFE0202A, 1.8f, 1.6f, 1.6f);
        g.pop();
        g.push();
        g.translate(-1.4f, 12.0f, -6.0f);
        g.rotZ(-22);
        g.rotY(-15);
        XmasBack.gift(g, "xm_gift:3A6AE8:E8F4FF:3", 0xFFFFD36A, 1.4f, 1.4f, 1.4f);
        g.pop();
        g.push();
        g.translate(1.6f, 11.8f, -6.9f);
        g.color(0xFFFFFFFF);
        float[][] cane = {{0, 0, 0}, {0.3f, 2.4f, 0}, {0.55f, 3.1f, 0}, {1.05f, 3.35f, 0}, {1.5f, 3.0f, 0}, {1.55f, 2.6f, 0}};
        limb(g, "xm_candy", cane, new float[]{0.26f, 0.26f, 0.26f, 0.26f, 0.26f, 0.26f}, 7);
        g.pop();
        // ---- lantern on a curved pole (front right)
        float[][] pole = {{5.25f, 9.2f, 11.0f}, {5.4f, 11.4f, 11.5f}, {5.5f, 13.0f, 12.4f}, {5.5f, 13.4f, 13.5f}};
        g.color(0xFF2A2A30);
        limb(g, "c3_metal", pole, new float[]{0.18f, 0.16f, 0.15f, 0.13f}, 6);
        g.push();
        g.translate(5.5f, 13.3f, 13.5f);
        g.rotX(sin(t * 2.2f) * 8 - mv * 10);
        g.rotZ(sin(t * 1.7f) * 6);
        g.color(0xFF2A2A30);
        g.push();
        g.translate(0, -0.35f, 0);
        g.rotX(90);
        g.torus("c3_metal", 0.3f, 0.07f, 8, 4, 0, 0, 1, 1);
        g.pop();
        lathe(g, "c3_metal", new float[]{0, 0.55f, 0.9f, 0.95f, 0.8f}, new float[]{-0.6f, -0.75f, -1.1f, -1.35f, -1.35f}, 8);
        float fl = 0.85f + 0.1f * sin(t * 11) + 0.05f * sin(t * 17.3f);
        g.glow(true).color(0xFFFFFFFF);
        g.cylinder("c3_lglass", 0.68f, 0.72f, -3.2f, -1.35f, 4, false, 0, 0, 1, 1);
        g.push();
        g.translate(0, -2.9f, 0);
        g.scale(1, fl, 1);
        Cos3Models.flame(g, 1.1f, 0.3f, 0xE8FF9A2A, 0xF0FFF4B0);
        g.pop();
        g.glow(false).color(0xFF2A2A30);
        lathe(g, "c3_metal", new float[]{0.8f, 0.95f, 0.8f, 0}, new float[]{-3.2f, -3.35f, -3.6f, -3.62f}, 8);
        halo(g, 0, -2.3f, 0, 5.5f, alpha(0.5 * fl, 0xFFC86A));
        g.pop();
        // ---- magic sparkle trail + snow spray when moving
        int nt = 5;
        for (int i = 0; i < nt; i++) {
            float ph = fract(t * 0.9f + i / (float) nt);
            float x = sin(i * 2.7f + t) * 3.5f, y = 2 + (float) Capes.hash1(i, 601) * 6 + ph * 2, z = -10 - ph * 9;
            float al = (0.25f + 0.75f * mv) * (1 - ph);
            if (a.preview) al = 0.7f * (1 - ph);
            Cos3Models.sparkle(g, x, y, z, 0.35f, alpha(al, i % 2 == 0 ? 0xFFE07A : 0xE8F4FF));
        }
        if (mv > 0.05f) {
            for (int s = -1; s <= 1; s += 2)
                for (int i = 0; i < 3; i++) {
                    float ph = fract(t * 2.2f + i / 3f + (s > 0 ? 0 : 0.17f));
                    halo(g, s * (4.7f + ph * 1.2f), 0.5f + ph * 1.2f, -8.5f - ph * 3, 1.2f + ph * 2, alpha(mv * (1 - ph) * 0.7, 0xFFFFFF));
                }
        }
        g.pop();
        g.color(0xFFFFFFFF);
    }
}
