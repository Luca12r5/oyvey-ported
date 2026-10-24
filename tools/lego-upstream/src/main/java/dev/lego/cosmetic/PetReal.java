package dev.lego.cosmetic;

import dev.lego.cosmetic.Cos.A;
import dev.lego.cosmetic.Pets.P;
import dev.lego.cosmetic.Pets.Part;

import static dev.lego.cosmetic.PetGeo.blob;
import static dev.lego.cosmetic.PetGeo.cone;
import static dev.lego.cosmetic.PetGeo.cos;
import static dev.lego.cosmetic.PetGeo.sin;
import static dev.lego.cosmetic.PetRig.lerp;
import static dev.lego.cosmetic.PetRig.smooth;
import static dev.lego.cosmetic.Pets.col;
import static dev.lego.cosmetic.Pets.fur;
import static dev.lego.cosmetic.Pets.gl;
import static dev.lego.cosmetic.Pets.head;
import static dev.lego.cosmetic.Pets.pose;

/**
 * "Real" pets: stylised but anatomically proportioned animals that grow from a chibi baby (growth 0)
 * into a realistic adult (growth 1): longer legs and body, neck, smaller head and eyes, manes / antlers / wings.
 */
final class PetReal {
    private PetReal() { }

    /** Config of a realistic quadruped, all sizes are the ADULT sizes in px. */
    static final class R4 {
        String body, head, muzzle, legUp, legLo, paw, style = "wolf";
        int nose = 0x1E1A1E;
        float size = 1;
        float L = 5f, legX = 1.3f, zs = 3f, zh = -3f;
        float rx = 2f, ry = 2.2f, hipF = 0.9f;
        float neckLen = 2.3f, neckAng = 42, neckR = 1.2f;
        float skRx = 1.65f, skRy = 1.55f, skRz = 1.75f;
        float snLen = 2f, snR = 0.8f, snY = -0.45f, headDown = 0;
        float r1f = 0.7f, r2f = 0.45f, r1h = 1f, r2h = 0.48f, pawR = 0.55f, pawLen = 1.3f;
        boolean hoof;
        /** grazers (horse, deer) do not sit like a dog, they lower the head to graze instead. */
        boolean grazer;
        float span = 0.9f, eyeEl = 0.22f;
        Part ears, tail, headExtra, bodyExtra, neckExtra;
        float babyS = 0.56f, babyHead = 1.75f, babyLeg = 0.5f, babyLen = 0.72f, babySnout = 0.45f, babyNeck = 0.45f;
    }

    static void real4(G g, P p, R4 r) {
        float k = p.gr;
        float S = (p.preview ? r.babyS * 1.4f : lerp(r.babyS, 1, k)) * r.size;
        float hf = lerp(r.babyHead, 1, k), lf = lerp(r.babyLeg, 1, k), bf = lerp(r.babyLen, 1, k);
        float sf = lerp(r.babySnout, 1, k), nf = lerp(r.babyNeck, 1, k), fat = lerp(1.18f, 1, k);
        float L = r.L * lf, zs = r.zs * bf, zh = r.zh * bf;
        float rx = r.rx * fat, ry = r.ry * fat;
        float hipH = L * 0.96f + r.pawR * 0.85f;
        float bodyY = hipH + ry * 0.18f;
        float belly = bodyY - ry;
        p.stepBob = 0;
        float graze = 0;
        if (r.grazer) {
            graze = p.sit;
            p.sit = 0;
            p.bow = Math.max(p.bow, graze * 0.3f);
        }
        g.push();
        g.scale(S);
        Pets.root(g, p);
        PetRig.Frame f = PetRig.frame(p, hipH, zs, zh, L, belly);
        float thick = lerp(1.3f, 1, k);
        PetRig.LegStyle fr = new PetRig.LegStyle(r.legUp, r.legLo, r.paw, r.r1f * thick, r.r2f * thick, r.pawR * lerp(1.15f, 1, k));
        PetRig.LegStyle hi = new PetRig.LegStyle(r.legUp, r.legLo, r.paw, r.r1h * thick, r.r2h * thick, r.pawR * lerp(1.15f, 1, k));
        fr.hoof = hi.hoof = r.hoof;
        fr.pawLen = hi.pawLen = r.pawLen;
        fr.seg = hi.seg = 5;
        PetRig.legs(g, p, f, fr, hi, r.legX * lerp(1.1f, 1, k), zs, zh, hipH, L, 0.5f, 0.46f);

        g.push();
        f.apply(g);
        g.rotZ(p.roll);
        // ---- torso: chest + hips
        float chestZ = zs * 0.45f, hipZ = zh * 0.5f, half = (zs - zh) * 0.5f;
        g.push();
        g.translate(0, bodyY, (chestZ + hipZ) / 2);
        g.scale(1, 1, p.stretch);
        g.translate(0, 0, -(chestZ + hipZ) / 2);
        blob(g, r.body, 0, 0, chestZ, rx, ry, half * 0.95f, 9, 5);
        blob(g, r.body, 0, -ry * 0.05f, hipZ, rx * r.hipF, ry * r.hipF, half * 0.85f, 9, 5);
        if (r.bodyExtra != null) {
            g.push();
            g.translate(0, 0, 0);
            r.bodyExtra.draw(g, p);
            g.pop();
        }
        if (r.tail != null) {
            g.push();
            g.translate(0, ry * r.hipF * 0.45f, hipZ - half * 0.78f);
            g.scale(lerp(0.75f, 1, k));
            r.tail.draw(g, p);
            g.pop();
        }
        g.pop();
        // ---- neck
        float ang = r.neckAng + 32 * (p.is("sad") ? 1 : 0) + 18 * p.crouch + 38 * p.lie + 36 * p.sniff + 28 * p.bow
                - 10 * (p.is("curious") ? 1 : 0) - 10 * p.sit + 85 * graze;
        float nl = r.neckLen * nf * (1 + 0.25f * graze);
        float nbY = bodyY + ry * 0.42f, nbZ = chestZ + half * 0.55f;
        g.push();
        g.translate(0, nbY, nbZ);
        g.rotX(ang);
        float nr = r.neckR * lerp(1.1f, 1, k);
        blob(g, r.body, 0, nl * 0.5f, 0, nr, nl * 0.5f + nr * 0.55f, nr * 1.05f, 8, 5);
        if (r.neckExtra != null) r.neckExtra.draw(g, p);
        g.push();
        g.translate(0, nl * 0.12f + nr * 0.2f, 0);
        PetRig.collar(g, p, nr * 1.02f, 0.2f + 0.05f * k);
        g.pop();
        g.pop();
        float hy = nbY + cos(Math.toRadians(ang)) * nl, hz = nbZ + sin(Math.toRadians(ang)) * nl;
        // ---- head
        g.push();
        g.translate(0, hy - p.bob * L * 0.6f + r.skRy * (hf - 1) * 0.35f, hz + r.skRz * (hf - 1) * 0.3f);
        Pets.headTurn(g, p);
        g.rotX(p.nod + r.headDown * lerp(0.4f, 1, k) + graze * (45 + sin(p.t * 5) * 4));
        g.scale(hf);
        realHead(g, p, r, sf, k);
        g.pop();
        Pets.fx(g, p, hy + r.skRy * hf + 1.4f);
        g.pop();
        PetRig.sparkles(g, p, Math.max(half * 1.4f, rx + 1.2f));
        g.pop();
    }

    /** Skull + snout + nose + face layers (eyes / brows on the skull, mouth on the snout). */
    static void realHead(G g, P p, R4 r, float sf, float k) {
        blob(g, r.head, 0, 0, 0, r.skRx, r.skRy, r.skRz, 11, 8);
        float sl = r.snLen * sf, sc = r.skRz * 0.42f + sl * 0.5f;
        float srx = r.snR * lerp(1.1f, 1, k), sry = r.snR * 0.8f, srz = sl * 0.55f + r.snR * 0.3f;
        blob(g, r.muzzle, 0, r.snY, sc, srx, sry, srz, 8, 5);
        // nose
        blob(g, gl(r.nose), 0, r.snY + sry * 0.45f, sc + srz * 0.93f, srx * 0.42f, sry * 0.34f, srx * 0.3f, 6, 3);
        String style = PetRig.style(r.style, k);
        boolean og = g.glow;
        int oc = g.color;
        g.color(0xFFFFFFFF).glow(false);
        // upper face (brows, eyes) on the skull
        float v1 = 0.6f;
        float spanEl = r.span * (v1 * PetTex.FH) / PetTex.FW * r.skRx / r.skRy;
        float el0 = r.eyeEl + 0.458f * spanEl;
        PetGeo.decal(g, "pet_face:" + style + ":" + p.emo, 0, 0, 0, r.skRx, r.skRy, r.skRz, 0, el0, r.span, 0, 0, 1, v1, 1.035f);
        PetGeo.decal(g, Pets.eyes(style, p), 0, 0, 0, r.skRx, r.skRy, r.skRz, p.lookX * 0.08f, el0 + p.lookY * 0.05f, r.span, 0, 0, 1, v1, 1.05f);
        // mouth on the snout
        PetGeo.decal(g, "pet_face:" + style + ":" + p.emo, 0, r.snY, sc, srx, sry, srz, 0, -0.32f, 0.95f, 0.25f, 0.57f, 0.75f, 0.8f, 1.04f, 4, 3);
        g.color(oc).glow(og);
        if (r.ears != null) r.ears.draw(g, p);
        if (r.headExtra != null) r.headExtra.draw(g, p);
    }

    /** Tail chain along -z with an optional tip part. */
    static void chain(G g, P p, String[] tex, float[] rad, float seg, float curl, float lift, float wag, Part tip) {
        g.push();
        g.rotY(p.tail * wag + p.tailSway);
        g.rotX(lift + p.tailLift);
        for (int i = 0; i < rad.length; i++) {
            g.rotX(curl);
            g.rotY(p.tail * 0.3f * wag + p.tailSway * 0.4f);
            blob(g, tex[Math.min(i, tex.length - 1)], 0, 0, -seg * 0.5f, rad[i], rad[i], seg * 0.62f + rad[i] * 0.3f, 6, 3);
            g.translate(0, 0, -seg * 0.82f);
        }
        if (tip != null) tip.draw(g, p);
        g.pop();
    }

    static String[] rep(String s, int n) { String[] a = new String[n]; java.util.Arrays.fill(a, s); return a; }

    // ================================================================== the animals
    static void wolf(G g, A a) {
        P p = pose(a);
        R4 r = new R4();
        r.body = fur("wolf"); r.head = head("wolf"); r.muzzle = col(0xDCDAD6); r.legUp = col(0x8E939E); r.legLo = col(0xB9BCC4); r.paw = col(0xD6D8DC);
        r.style = "wolf"; r.L = 5.4f; r.legX = 1.25f; r.zs = 3.0f; r.zh = -3.1f; r.rx = 1.95f; r.ry = 2.25f; r.hipF = 0.86f;
        r.neckLen = 2.3f; r.neckAng = 44; r.neckR = 1.3f;
        r.snLen = 2.1f; r.snR = 0.78f;
        r.ears = (gg, pp) -> {
            for (int s = -1; s <= 1; s += 2) Pets.earPointy(gg, pp, col(0x7E838E), col(0xE9E7E2), col(0x4E535E), s * 1.0f, 1.2f, -0.35f, s, 0.72f, 1.6f, 8);
        };
        r.neckExtra = (gg, pp) -> {
            float m = smooth(0.2f, 1, pp.gr);
            blob(gg, r.body, 0, 0.4f, 0.35f, 1.6f * m + 0.1f, 1.5f * m + 0.1f, 1.25f * m + 0.1f, 7, 4);   // ruff
        };
        String gw = col(0x8E939E), dk = col(0x4E535E), lt = col(0xDCDAD6);
        r.tail = (gg, pp) -> chain(gg, pp, new String[]{gw, gw, gw, dk, dk}, new float[]{0.5f, 0.7f, 0.78f, 0.7f, 0.48f}, 1.05f, 4, -38, 0.6f, null);
        real4(g, p, r);
    }

    static void retriever(G g, A a) {
        P p = pose(a);
        R4 r = new R4();
        r.body = fur("retr"); r.head = head("retr"); r.muzzle = col(0xEDC688); r.legUp = col(0xE3AC5E); r.legLo = col(0xEDBE78); r.paw = col(0xF0C888);
        r.style = "retr"; r.L = 4.5f; r.legX = 1.25f; r.zs = 2.8f; r.zh = -2.9f; r.rx = 2.0f; r.ry = 2.15f;
        r.neckLen = 2.0f; r.neckAng = 40; r.neckR = 1.2f;
        r.skRx = 1.72f; r.skRy = 1.62f; r.skRz = 1.75f; r.snLen = 1.8f; r.snR = 0.85f; r.snY = -0.5f; r.nose = 0x2A1E1E;
        r.babyS = 0.6f;
        r.ears = (gg, pp) -> {
            float droop = Math.max(0, 1 - pp.ear) * 15, perk = Math.max(0, pp.ear - 1) * 90;
            for (int s = -1; s <= 1; s += 2) {
                gg.push();
                gg.translate(s * 1.45f, 0.75f, -0.3f);
                gg.rotZ(s * (14 + droop - perk + pp.earTwitch * (s > 0 ? 1 : 0)) + sin(pp.t * 9) * 4 * pp.walkW);
                gg.rotX(-6);
                blob(gg, col(0xD69A4E), s * 0.12f, -0.95f, 0, 0.42f, 1.15f, 0.82f, 7, 4);
                gg.pop();
            }
        };
        r.neckExtra = (gg, pp) -> {
            float m = smooth(0.3f, 1, pp.gr);
            blob(gg, col(0xF2CF94), 0, 0.2f, 0.55f, 1.15f * m + 0.1f, 1.4f * m + 0.1f, 0.9f * m + 0.1f, 7, 4);   // chest feathering
        };
        String c1 = col(0xE3AC5E), c2 = col(0xF0C888);
        r.tail = (gg, pp) -> chain(gg, pp, new String[]{c1, c1, c2, c2, c2}, new float[]{0.42f, 0.58f, 0.66f, 0.58f, 0.4f}, 1.0f, 9, -8, 1.0f, null);
        real4(g, p, r);
    }

    static void arcticFox(G g, A a) {
        P p = pose(a);
        R4 r = new R4();
        r.body = fur("afox"); r.head = head("afox"); r.muzzle = col(0xFBFCFF); r.legUp = col(0xF0F2F8); r.legLo = col(0xE6EAF2); r.paw = col(0xFFFFFF);
        r.style = "afox"; r.size = 0.86f; r.L = 3.8f; r.legX = 1.1f; r.zs = 2.5f; r.zh = -2.5f; r.rx = 1.75f; r.ry = 1.95f;
        r.neckLen = 1.6f; r.neckAng = 46; r.neckR = 1.15f;
        r.skRx = 1.6f; r.skRy = 1.45f; r.skRz = 1.6f; r.snLen = 1.55f; r.snR = 0.62f; r.snY = -0.42f; r.nose = 0x1E1A22;
        r.r1f = 0.62f; r.r2f = 0.42f; r.r1h = 0.85f; r.r2h = 0.44f; r.pawR = 0.55f;
        r.babyS = 0.66f;
        r.ears = (gg, pp) -> {
            for (int s = -1; s <= 1; s += 2) Pets.earPointy(gg, pp, col(0xF0F2F8), col(0xD8DEEA), s * 0.95f, 1.1f, -0.3f, s, 0.7f, 1.05f, 14);
        };
        r.neckExtra = (gg, pp) -> blob(gg, r.body, 0, 0.35f, 0.3f, 1.55f, 1.35f, 1.3f, 8, 5);
        String w = col(0xF6F8FC), w2 = col(0xE6ECF6);
        r.tail = (gg, pp) -> chain(gg, pp, new String[]{w, w, w2, w, w}, new float[]{0.75f, 1.12f, 1.28f, 1.16f, 0.8f}, 1.15f, 9, -18, 0.8f, null);
        real4(g, p, r);
    }

    static void horse(G g, A a) {
        P p = pose(a);
        R4 r = new R4();
        r.body = fur("horse"); r.head = head("horse"); r.muzzle = col(0x8A5236); r.legUp = col(0xA3603A); r.legLo = col(0x4A2C1E); r.paw = gl(0x2A2020);
        r.style = "horse"; r.nose = 0x3A2A24; r.grazer = true;
        r.L = 7.2f; r.legX = 1.2f; r.zs = 3.6f; r.zh = -3.6f; r.rx = 2.15f; r.ry = 2.55f; r.hipF = 0.96f;
        r.neckLen = 3.8f; r.neckAng = 30; r.neckR = 1.12f;
        r.skRx = 1.3f; r.skRy = 1.4f; r.skRz = 1.45f; r.snLen = 2.8f; r.snR = 0.92f; r.snY = -0.3f; r.headDown = 38;
        r.r1f = 0.72f; r.r2f = 0.36f; r.r1h = 0.98f; r.r2h = 0.38f; r.pawR = 0.46f; r.hoof = true;
        r.span = 1.1f; r.eyeEl = 0.3f;
        r.babyS = 0.55f; r.babyLeg = 0.78f; r.babyHead = 1.6f; r.babyNeck = 0.6f; r.babySnout = 0.55f;
        String mane = col(0x241510);
        r.ears = (gg, pp) -> {
            for (int s = -1; s <= 1; s += 2) Pets.earPointy(gg, pp, col(0xA3603A), col(0x5A3420), s * 0.7f, 1.15f, -0.4f, s, 0.45f, 1.15f, 6);
            // forelock
            gg.push();
            gg.translate(0, 1.3f, 0.35f);
            gg.rotX(40 + sin(pp.t * 2) * 4);
            blob(gg, mane, 0, 0.3f, 0, 0.5f, 0.75f, 0.3f, 6, 3);
            gg.pop();
        };
        r.neckExtra = (gg, pp) -> {
            // mane along the back of the neck
            float m = lerp(0.5f, 1, pp.gr), nl = 3.8f * lerp(0.6f, 1, pp.gr);
            for (int i = 0; i < 5; i++) {
                float u = i / 4f;
                gg.push();
                gg.translate(0, nl * (0.05f + u * 0.95f), -1.0f);
                gg.rotZ(sin(pp.t * 3 + i) * 5 * (0.3f + pp.walkW));
                blob(gg, mane, 0, 0, 0, 0.32f * m, 0.62f * m, 0.55f * m, 5, 3);
                gg.pop();
            }
        };
        r.tail = (gg, pp) -> chain(gg, pp, rep(mane, 5), new float[]{0.4f, 0.58f, 0.64f, 0.56f, 0.4f}, 1.1f, -6 - 6 * pp.walkW, -58 + 25 * pp.runW, 0.5f, null);
        real4(g, p, r);
    }

    static void deer(G g, A a) {
        P p = pose(a);
        R4 r = new R4();
        boolean fawn = a.growth < 0.5f;
        r.body = fur(fawn ? "fawn" : "deer"); r.head = head("deer"); r.muzzle = col(0xB88458); r.legUp = col(0xA9714A); r.legLo = col(0xC08A5E); r.paw = gl(0x2A2020);
        r.style = "deer"; r.nose = 0x1A1414; r.grazer = true;
        r.L = 6.6f; r.legX = 1.02f; r.zs = 3.0f; r.zh = -3.1f; r.rx = 1.7f; r.ry = 2.1f; r.hipF = 0.95f;
        r.neckLen = 3.2f; r.neckAng = 26; r.neckR = 0.92f;
        r.skRx = 1.3f; r.skRy = 1.3f; r.skRz = 1.45f; r.snLen = 1.9f; r.snR = 0.68f; r.snY = -0.32f; r.headDown = 16;
        r.r1f = 0.55f; r.r2f = 0.28f; r.r1h = 0.8f; r.r2h = 0.3f; r.pawR = 0.38f; r.hoof = true;
        r.span = 1.05f; r.eyeEl = 0.26f;
        r.babyS = 0.55f; r.babyLeg = 0.75f; r.babyHead = 1.65f;
        final int lvl = Math.max(1, Math.min(5, a.level));
        r.ears = (gg, pp) -> {
            for (int s = -1; s <= 1; s += 2) Pets.earPointy(gg, pp, col(0xA9714A), col(0xF6E8DA), s * 0.95f, 0.85f, -0.4f, s, 0.72f, 1.75f, 55);
        };
        r.headExtra = (gg, pp) -> antlers(gg, pp, lvl, pp.gr);
        r.tail = (gg, pp) -> {
            gg.rotX(35 + pp.tailLift * 0.5f + (pp.is("excited") || pp.is("angry") ? 40 : 0));
            blob(gg, col(0xFFFFFF), 0, 0, -0.45f, 0.42f, 0.35f, 0.62f, 6, 3);
        };
        real4(g, p, r);
    }

    /** Antlers growing with the level: nubs, spikes, fork, crown. */
    static void antlers(G g, P p, int lvl, float k) {
        if (lvl < 2) return;
        String bone = col(0xE8D7B8);
        float len = lvl == 2 ? 0.55f : lvl == 3 ? 1.6f : lvl == 4 ? 2.5f : 3.3f;
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 0.6f, 1.05f, -0.1f);
            g.rotZ(-s * 22);
            g.rotX(-18);
            g.cylinder(bone, 0.17f, 0.11f, 0, len, 5, false, 0, 0, 1, 1);
            if (lvl >= 4) {
                // tines
                for (int i = 0; i < (lvl == 5 ? 3 : 2); i++) {
                    g.push();
                    g.translate(0, len * (0.35f + i * 0.25f), 0);
                    g.rotX(48);
                    g.rotZ(-s * 10);
                    g.cylinder(bone, 0.1f, 0.05f, 0, len * (0.42f - i * 0.08f), 4, false, 0, 0, 1, 1);
                    g.pop();
                }
                g.push();
                g.translate(0, len, 0);
                g.rotZ(-s * 25);
                g.cylinder(bone, 0.1f, 0.04f, 0, len * 0.35f, 4, false, 0, 0, 1, 1);
                g.pop();
            } else {
                g.translate(0, len, 0);
                blob(g, bone, 0, 0, 0, 0.14f, 0.14f, 0.14f, 4, 2);
            }
            g.pop();
        }
    }

    static void lion(G g, A a) {
        P p = pose(a);
        R4 r = new R4();
        r.body = fur("lion"); r.head = head("lion"); r.muzzle = col(0xF6E3C0); r.legUp = col(0xE0A965); r.legLo = col(0xE8B879); r.paw = col(0xF0CC96);
        r.style = "lion"; r.nose = 0xB0645A;
        r.L = 4.9f; r.legX = 1.45f; r.zs = 3.2f; r.zh = -3.3f; r.rx = 2.3f; r.ry = 2.4f; r.hipF = 0.88f;
        r.neckLen = 1.9f; r.neckAng = 50; r.neckR = 1.5f;
        r.skRx = 1.9f; r.skRy = 1.8f; r.skRz = 1.9f; r.snLen = 1.4f; r.snR = 1.05f; r.snY = -0.6f;
        r.r1f = 0.95f; r.r2f = 0.62f; r.r1h = 1.2f; r.r2h = 0.6f; r.pawR = 0.75f; r.pawLen = 1.15f;
        r.span = 0.95f;
        final String mane = col(0x9A5626), mane2 = col(0xB86A2E);
        r.ears = (gg, pp) -> {
            for (int s = -1; s <= 1; s += 2) Pets.earRound(gg, pp, col(0xD89A55), col(0xF6E3C0), s * 1.35f, 1.3f, -0.35f, s, 0.58f);
        };
        r.headExtra = (gg, pp) -> {
            // mane tufts around the face, grows in with age
            float m = smooth(0.15f, 1, pp.gr);
            if (m <= 0.01f) return;
            for (int i = 0; i < 10; i++) {
                double an = Math.PI * 2 * i / 10 + 0.3;
                float wob = sin(pp.t * 2.2f + i) * 0.06f;
                gg.push();
                gg.translate(cos(an) * 1.75f, sin(an) * 1.7f + 0.1f, -0.55f);
                gg.rotZ((float) Math.toDegrees(an) - 90);
                gg.rotX(-25);
                gg.scale(m * (1 + wob));
                cone(gg, i % 2 == 0 ? mane : mane2, 0.95f, 2.0f, 4, false);
                gg.pop();
            }
            blob(gg, mane, 0, 0.1f, -0.9f, 2.1f * m + 0.4f, 2.1f * m + 0.4f, 1.3f * m + 0.3f, 7, 4);
        };
        r.neckExtra = (gg, pp) -> {
            float m = smooth(0.15f, 1, pp.gr);
            if (m > 0.01f) blob(gg, mane, 0, 1.2f, -0.2f, 2.0f * m, 1.9f * m + 0.3f, 1.9f * m, 7, 4);
        };
        String t = col(0xE0A965);
        r.tail = (gg, pp) -> chain(gg, pp, rep(t, 5), new float[]{0.3f, 0.27f, 0.25f, 0.24f, 0.22f}, 1.4f, -7, -15, 0.8f,
                (g2, p2) -> blob(g2, mane, 0, 0, 0.1f, 0.48f, 0.48f, 0.62f, 5, 3));
        real4(g, p, r);
    }

    static void tiger(G g, A a) {
        P p = pose(a);
        R4 r = new R4();
        r.body = fur("tiger"); r.head = head("tiger"); r.muzzle = col(0xFFF6EA); r.legUp = fur("tiger"); r.legLo = col(0xF0892C); r.paw = col(0xFFF1E0);
        r.style = "tiger"; r.nose = 0xC8646A;
        r.L = 5.0f; r.legX = 1.4f; r.zs = 3.3f; r.zh = -3.4f; r.rx = 2.15f; r.ry = 2.3f; r.hipF = 0.9f;
        r.neckLen = 1.9f; r.neckAng = 52; r.neckR = 1.4f;
        r.skRx = 1.85f; r.skRy = 1.72f; r.skRz = 1.85f; r.snLen = 1.35f; r.snR = 1.0f; r.snY = -0.58f;
        r.r1f = 0.92f; r.r2f = 0.6f; r.r1h = 1.15f; r.r2h = 0.58f; r.pawR = 0.72f; r.pawLen = 1.15f;
        r.span = 0.95f;
        r.ears = (gg, pp) -> {
            for (int s = -1; s <= 1; s += 2) Pets.earRound(gg, pp, col(0xE0801E), col(0xFFF6EA), s * 1.3f, 1.25f, -0.3f, s, 0.55f);
            // white cheek ruffs
            float m = lerp(0.4f, 1, pp.gr);
            for (int s = -1; s <= 1; s += 2) {
                gg.push();
                gg.translate(s * 1.5f, -0.55f, -0.2f);
                gg.rotY(-s * 35);
                gg.rotZ(-s * 118);
                gg.scale(1, 1, 0.5f);
                cone(gg, col(0xFFF6EA), 0.5f * m, 0.75f * m, 4, false);
                gg.pop();
            }
        };
        String o = col(0xF0892C), d = col(0x241A1C);
        r.tail = (gg, pp) -> chain(gg, pp, new String[]{o, d, o, d, o}, new float[]{0.36f, 0.34f, 0.33f, 0.32f, 0.3f}, 1.35f, -5, -18, 0.8f, null);
        real4(g, p, r);
    }

    static void drake(G g, A a) {
        P p = pose(a);
        R4 r = new R4();
        final int lvl = Math.max(1, Math.min(5, a.level));
        r.body = fur("drake"); r.head = head("drake"); r.muzzle = fur("drake"); r.legUp = col(0x7C5CD6); r.legLo = col(0x6A4CC0); r.paw = col(0x5A40A8);
        r.style = "drake"; r.nose = 0x4E3A9E;
        r.L = 4.6f; r.legX = 1.6f; r.zs = 3.2f; r.zh = -3.3f; r.rx = 2.2f; r.ry = 2.3f; r.hipF = 0.95f;
        r.neckLen = 3.0f; r.neckAng = 36; r.neckR = 1.05f;
        r.skRx = 1.55f; r.skRy = 1.4f; r.skRz = 1.8f; r.snLen = 2.0f; r.snR = 0.85f; r.snY = -0.3f;
        r.r1f = 0.8f; r.r2f = 0.55f; r.r1h = 1.15f; r.r2h = 0.62f; r.pawR = 0.68f; r.pawLen = 1.5f;
        r.babyS = 0.58f;
        final String horn = col(0xF2E2B8), spike = col(0xF2D9A0);
        final boolean fire = lvl >= 3 && (p.is("angry") || p.is("excited"));
        r.ears = (gg, pp) -> {
            // horns grow with the level
            float hl = 0.8f + lvl * 0.45f;
            for (int s = -1; s <= 1; s += 2) {
                gg.push();
                gg.translate(s * 0.75f, 1.0f, -0.6f);
                gg.rotZ(-s * 18);
                gg.rotX(-55);
                cone(gg, horn, 0.38f, hl, 7, false);
                gg.pop();
                if (lvl >= 5) {
                    gg.push();
                    gg.translate(s * 1.25f, 0.4f, -0.7f);
                    gg.rotZ(-s * 60);
                    gg.rotX(-50);
                    cone(gg, horn, 0.26f, hl * 0.55f, 6, false);
                    gg.pop();
                }
            }
        };
        r.headExtra = (gg, pp) -> {
            if (!fire) return;
            // little flames / sparks from the mouth
            for (int i = 0; i < 4; i++) {
                float ph = PetRig.frac(pp.t * 2.2f + i * 0.25f);
                int al = (int) (230 * (1 - ph));
                PetGeo.sprite(gg, "pet_star", sin(pp.t * 9 + i) * 0.3f, -0.4f + ph * 0.6f, 3.6f + ph * 3.2f, 0.6f + ph * 1.2f, (al << 24) | (i % 2 == 0 ? 0xFF9A2A : 0xFFD24A));
            }
        };
        r.bodyExtra = (gg, pp) -> {
            // back spikes
            for (int i = 0; i < 5; i++) {
                gg.push();
                gg.translate(0, 2.2f - Math.abs(i - 1.5f) * 0.12f, 2.2f - i * 1.3f);
                gg.rotX(-25);
                gg.scale(0.45f, 1, 1);
                cone(gg, spike, 0.5f, 0.75f + (lvl >= 4 ? 0.35f : 0), 5, false);
                gg.pop();
            }
            // wings: small in babies, huge at level 5; folded when idle, flapping when running / excited
            float span = lerp(2.4f, 6.2f, pp.gr) + (lvl >= 5 ? 1.8f : 0);
            float spread = Math.max(Math.max(pp.runW, pp.is("excited") ? 1 : 0), lvl >= 5 ? 0.35f + 0.65f * pp.walkW : 0.25f * pp.walkW);
            float[] fl = PetRig.flap(pp, 1.6f);
            float flap = fl[0] * 38 * spread;
            String wt = "pet_dwing:4E3A9E:" + (lvl >= 5 ? "E07AE8" : "B89AF5");
            for (int s = -1; s <= 1; s += 2) {
                gg.push();
                gg.translate(s * 1.3f, 1.9f, 1.6f);
                gg.scale(s, 1, 1);
                gg.rotY(lerp(-70, 5, spread));
                gg.rotZ(lerp(-8, 22, spread) + flap);
                gg.rotX(lerp(70, 88, spread));
                gg.planeTL(wt, span, span * 0.72f, 0, 0, 1, 1);
                gg.pop();
            }
        };
        String b = col(0x7C5CD6), b2 = col(0x6A4CC0);
        r.tail = (gg, pp) -> chain(gg, pp, new String[]{b, b, b2, b2, b2, b2}, new float[]{0.85f, 0.7f, 0.56f, 0.45f, 0.36f, 0.28f}, 1.3f, 3, -14, 0.9f,
                (g2, p2) -> {
                    g2.push();
                    g2.rotX(-90);
                    g2.scale(1, 1, 0.3f);
                    cone(g2, spike, 0.7f, 1.2f, 4, false);
                    g2.pop();
                });
        real4(g, p, r);
    }

    // ================================================================== eagle (perches, walks, flies)
    static void eagle(G g, A a) {
        P p = pose(a);
        float k = p.gr;
        float S = p.preview ? 1.0f : lerp(0.72f, 1.4f, k);
        float fly = smooth(0.15f, 0.5f, p.sp);
        float[] fl = PetRig.flap(p, 2.1f);
        // glide phases at full speed
        float glide = PetRig.window(PetRig.frac(p.t / 3.1f), 0.45f, 0.95f, 0.1f) * p.runW;
        float amp = (p.is("sleepy") ? 12 : 42) * (1 - glide * 0.85f);
        float stretchW = PetRig.window(PetRig.frac(p.t / 14f), 0.62f, 0.76f, 0.04f) * p.idle * (p.is("sleepy") ? 0 : 1);
        float exc = p.is("excited") ? 1 : 0;
        float spread = Math.max(fly, Math.max(stretchW, exc * 0.8f));
        p.stepBob = 0;
        g.push();
        g.scale(S);
        float alt = fly * (6.5f + fl[1] * 0.9f * (1 - glide));
        g.translate(0, alt, 0);
        Pets.root(g, p);
        float legLen = lerp(1.2f, 1.9f, k);
        // legs + talons (tucked back in flight)
        String yel = gl(0xF2B83A);
        for (int s = -1; s <= 1; s += 2) {
            float c = PetRig.frac(p.t * PetRig.WADDLE_HZ + (s > 0 ? 0 : 0.5f));
            float lift = (1 - fly) * p.walkW * Math.max(0, sin(Math.PI * 2 * c)) * 0.5f;
            g.push();
            g.translate(s * 0.75f, legLen + 0.25f, -0.2f);
            g.rotX(fly * 70 - (1 - fly) * p.walkW * sin(Math.PI * 2 * c) * 22);
            g.translate(0, lift, 0);
            g.cylinder(yel, 0.2f, 0.17f, -legLen, 0, 5, false, 0, 0, 1, 1);
            g.translate(0, -legLen, 0);
            for (int t = -1; t <= 1; t++) {
                g.push();
                g.rotY(t * 28);
                blob(g, yel, 0, -0.1f, 0.4f, 0.14f, 0.12f, 0.45f, 5, 2);
                g.pop();
            }
            g.pop();
        }
        // body
        float bodyUp = lerp(40, 4, fly) + p.waddle * 0;   // perched = upright, flying = horizontal
        g.push();
        g.translate(0, legLen + 1.6f, 0);
        g.rotZ(p.waddle * (1 - fly) * 0.8f + p.roll);
        g.rotX(-bodyUp);
        float bR = lerp(1.35f, 1.45f, k);
        blob(g, fur("eagle"), 0, 0, 0, bR, bR * 1.05f, lerp(2.0f, 2.7f, k), 11, 6);
        // tail fan
        g.push();
        g.translate(0, 0.1f, -lerp(1.8f, 2.5f, k));
        g.rotX(80 - fly * 70 + p.tailLift * 0.3f);
        g.rotY(180 + p.tailSway * 0.5f);
        g.plane("pet_tailfan:F4F0E8:D8D0C0", 0, -lerp(1.0f, 1.5f, k), lerp(1.9f, 2.6f, k), lerp(2.0f, 3.0f, k), 0, 0, 1, 1);
        g.pop();
        // wings
        float span = lerp(3.2f, 7.4f, k), chord = lerp(1.9f, 3.1f, k);
        float fold = 1 - spread;
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * bR * 0.8f, bR * 0.55f, 0.9f);
            g.scale(s, 1, 1);
            float flap = fly > 0.01f ? fl[0] * amp * fly : 0;
            float stretch = stretchW * (25 + sin(p.t * 3) * 8) + exc * (30 + sin(p.t * 12) * 25);
            g.rotY(80 * fold);
            g.rotZ(lerp(-12, 6, spread) + flap + stretch - (p.is("sad") ? 10 : 0));
            g.rotX(90 - fold * 10);
            g.planeTL("pet_feather:5B3B25:2A1A10", span * lerp(0.62f, 1, spread), chord, 0, 0, 1, 1);
            g.pop();
        }
        g.pop();
        // head (white) with hooked beak
        g.push();
        float hy = legLen + 1.6f + lerp(1.9f, 0.5f, fly), hz = lerp(0.9f, 2.9f, fly) * lerp(1, 1.2f, k);
        g.translate(0, hy - fl[1] * 0.2f * fly, hz);
        Pets.headTurn(g, p);
        float hs = lerp(1.45f, 1f, k);
        g.scale(hs);
        float rx = 1.15f, ry = 1.1f, rz = 1.2f;
        blob(g, fur("eagleh"), 0, 0, 0, rx, ry, rz, 12, 8);
        String st = PetRig.style("eagle", k);
        boolean og = g.glow;
        g.color(0xFFFFFFFF).glow(false);
        PetGeo.decal(g, "pet_face:" + st + ":" + p.emo, 0, 0, 0, rx, ry, rz, 0, 0.25f, 1.15f, 0, 0, 1, 0.6f, 1.035f);
        PetGeo.decal(g, Pets.eyes(st, p), 0, 0, 0, rx, ry, rz, p.lookX * 0.08f, 0.25f + p.lookY * 0.05f, 1.15f, 0, 0, 1, 0.6f, 1.05f);
        g.glow(og);
        // hooked beak: upper cone bending down + tip
        g.push();
        g.translate(0, -0.1f, rz * 0.8f);
        g.rotX(80);
        g.scale(1, 1, 0.8f);
        cone(g, gl(0xF2B83A), 0.42f, 0.95f, 7, true);
        g.pop();
        g.push();
        g.translate(0, -0.25f, rz * 0.8f + 0.85f);
        g.rotX(160);
        cone(g, gl(0x3A2A20), 0.14f, 0.35f, 5, false);
        g.pop();
        float open = p.is("excited") ? 0.5f + 0.5f * Math.abs(sin(p.t * 7)) : p.is("angry") ? 0.6f : 0;
        g.push();
        g.translate(0, -0.35f, rz * 0.75f);
        g.rotX(95 + open * 25);
        g.scale(1, 1, 0.5f);
        cone(g, gl(0xE8A82A), 0.32f, 0.6f, 6, false);
        g.pop();
        g.pop();
        Pets.fx(g, p, hy + ry * hs + 1.3f);
        g.pop();
        if (fly < 0.5f) PetRig.sparkles(g, p, 3.2f);
    }
}
