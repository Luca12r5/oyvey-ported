package dev.lego.cosmetic;

import static dev.lego.cosmetic.Geo.*;

import dev.lego.cosmetic.Cos.A;
import dev.lego.cosmetic.Cos.Rarity;
import dev.lego.cosmetic.Cos.Slot;

/**
 * Layered 3D wings. Feathered wings: primaries, secondaries and two covert rows, every feather a curved strip.
 * Membrane wings: bone struts (arm + fingers) with translucent scalloped membranes. Plus butterfly, fairy and mech wings.
 * Body space: wings attach at the upper back (z = -2.3).
 */
final class Wings {
    private Wings() { }

    static void register() {
        Cos.add("wings_angel", "Engelsflügel", Slot.WINGS, Rarity.LEGENDARY, (g, a) -> feathered(g, a, "angel", 16, false, 0));
        Cos.add("wings_gold", "Goldflügel", Slot.WINGS, Rarity.LEGENDARY, (g, a) -> feathered(g, a, "gold", 16, false, 0));
        Cos.add("wings_phoenix", "Phönixflügel", Slot.WINGS, Rarity.LEGENDARY, (g, a) -> feathered(g, a, "phoenix", 17, true, 1));
        Cos.add("wings_fire", "Feuerflügel", Slot.WINGS, Rarity.EPIC, (g, a) -> feathered(g, a, "fire", 16, true, 2));
        Cos.add("wings_shadow", "Schattenflügel", Slot.WINGS, Rarity.EPIC, (g, a) -> feathered(g, a, "shadow", 16, false, 3));
        Cos.add("wings_owl", "Eulenflügel", Slot.WINGS, Rarity.RARE, (g, a) -> feathered(g, a, "owl", 15, false, 0));
        Cos.add("wings_rainbow", "Regenbogenflügel", Slot.WINGS, Rarity.EPIC, (g, a) -> feathered(g, a, "rainbow", 15, true, 4));
        Cos.add("wings_galaxy", "Galaxieflügel", Slot.WINGS, Rarity.LEGENDARY, (g, a) -> feathered(g, a, "galaxy", 17, true, 5));
        Cos.add("wings_crystal", "Kristallflügel", Slot.WINGS, Rarity.LEGENDARY, (g, a) -> feathered(g, a, "crystal", 16, true, 5));
        Cos.add("wings_demon", "Dämonenflügel", Slot.WINGS, Rarity.EPIC, (g, a) -> membrane(g, a, "demon", 18, 0xFF3A1A10, 0, false));
        Cos.add("wings_dragon", "Drachenflügel", Slot.WINGS, Rarity.LEGENDARY, (g, a) -> membrane(g, a, "dragon", 20, 0xFF5A2A0A, 0, false));
        Cos.add("wings_icedragon", "Eisdrachenflügel", Slot.WINGS, Rarity.LEGENDARY, (g, a) -> membrane(g, a, "icedragon", 19, 0xFFBFEFFF, 0xFF8AE8FF, true));
        Cos.add("wings_neon", "Neonflügel", Slot.WINGS, Rarity.EPIC, (g, a) -> membrane(g, a, "neon", 17, 0xFF1A0A2A, 0xFFFF5AF0, true));
        Cos.add("wings_bat", "Fledermausflügel", Slot.WINGS, Rarity.RARE, (g, a) -> membrane(g, a, "bat", 15, 0xFF2A1C16, 0, false));
        Cos.add("wings_butterfly", "Schmetterling", Slot.WINGS, Rarity.EPIC, HdWings::butterfly);
        Cos.add("wings_fairy", "Feenflügel", Slot.WINGS, Rarity.EPIC, HdWings::fairy);
        Cos.add("wings_cyber", "Cyberflügel", Slot.WINGS, Rarity.LEGENDARY, HdWings::cyber);
    }

    // ================================================================== shared pose
    static final class Pose {
        float raise, sweep, fold, flap, t, mv;
    }

    static Pose pose(A a, float flapScale) {
        Pose p = new Pose();
        float t = a.time;
        float mv = a.preview ? 0 : Math.max(0, Math.min(1, a.move));
        float fold = a.sneak ? 1f : (a.preview ? 0.0f : 0.22f * (1 - mv));
        float slow = sin(t * 1.6) * 5 + sin(t * 0.7 + 1) * 3;
        float fast = sin(t * 7.5) * 26;
        float flap = (slow * (1 - mv) + fast * mv) * flapScale * (a.sneak ? 0.35f : 1f);
        p.raise = lerp(14, -62, fold) + flap + mv * 4;
        p.sweep = lerp(26, 8, fold) - mv * 10 + flap * 0.3f;
        p.fold = fold;
        p.flap = flap;
        p.t = t;
        p.mv = mv;
        return p;
    }

    private static void root(G g, Pose p, int side, float rootX, float rootY) {
        g.scale(side, 1, 1);
        g.translate(rootX, rootY, -2.35f);
        g.rotY(p.sweep);
        g.rotZ(p.raise);
        g.rotX(-8);
    }

    // ================================================================== feathered
    /**
     * @param fx 0 none, 1 phoenix flames, 2 fire embers, 3 shadow smoke, 4 rainbow hue, 5 sparkles
     */
    static void feathered(G g, A a, String style, float S, boolean glow, int fx) {
        Pose p = pose(a, 1f);
        String fea = "fea_" + style, cov = "cov_" + style;
        for (int side = 1; side >= -1; side -= 2) {
            g.push();
            root(g, p, side, 0.9f, -2.2f);
            g.glow(glow);
            float fold = p.fold;
            float spanK = 1 - 0.18f * fold;
            float bend = 3.5f + p.flap * 0.18f;
            // --- primaries
            int np = 9;
            for (int i = 0; i < np; i++) {
                float f = i / (float) (np - 1);
                float s = 0.58f + 0.42f * f;
                float th = lerp(16, 50, f) * (1 - 0.65f * fold);
                float L = S * (0.74f + 0.26f * sin(Math.PI * (0.3 + 0.55 * f))) * (fx == 2 ? 0.9f + 0.12f * sin(p.t * 9 + i * 1.7) : 1);
                tint(g, fx, s, 0.86f, p.t);
                feather(g, fea, edge(S, s, spanK, 0.1f + i * 0.012f), th, L, S * 0.17f, 4, bend);
            }
            // --- secondaries
            int ns = 11;
            for (int i = 0; i < ns; i++) {
                float f = i / (float) (ns - 1);
                float s = 0.03f + 0.57f * f;
                float th = lerp(-8, 16, f) * (1 - 0.5f * fold);
                float L = S * (0.54f + 0.14f * f) * (fx == 2 ? 0.9f + 0.12f * sin(p.t * 8 + i * 2.1) : 1);
                tint(g, fx, s, 0.93f, p.t);
                feather(g, fea, edge(S, s, spanK, 0.0f + i * 0.01f), th, L, S * 0.16f, 3, bend * 0.8f);
            }
            // --- greater coverts
            int nc = 13;
            for (int i = 0; i < nc; i++) {
                float f = i / (float) (nc - 1);
                float s = 0.02f + 0.9f * f;
                float th = (f < 0.62f ? lerp(-6, 16, f / 0.62f) : lerp(18, 55, (f - 0.62f) / 0.38f)) * (1 - 0.55f * fold);
                float L = S * (0.34f - 0.1f * f);
                tint(g, fx, s, 1f, p.t);
                feather(g, cov, edge(S, s, spanK, -0.22f), th, L, S * 0.15f, 2, bend * 0.5f);
            }
            // --- marginal coverts (leading edge)
            int nm = 14;
            for (int i = 0; i < nm; i++) {
                float f = i / (float) (nm - 1);
                float s = 0.0f + 0.96f * f;
                float th = lerp(0, 40, f) * (1 - 0.5f * fold);
                tint(g, fx, s, 1f, p.t);
                float[] e = edge(S, s, spanK, -0.42f);
                e[1] += 0.35f;
                feather(g, cov, e, th, S * (0.19f - 0.05f * f), S * 0.13f, 1, 0);
            }
            // --- wing arm: soft tube along the leading edge covering the feather roots
            {
                int m = 10;
                float[][] arm = new float[m + 1][];
                float[] rr = new float[m + 1], rz = new float[m + 1];
                for (int i = 0; i <= m; i++) {
                    float f = i / (float) m;
                    float[] e = edge(S, f * 0.97f, spanK, -0.3f);
                    arm[i] = new float[]{e[0], e[1] + 0.2f, e[2]};
                    rr[i] = S * 0.05f * (1.2f - f * 0.75f);
                    rz[i] = S * 0.045f * (1.2f - f * 0.75f);
                }
                tint(g, fx, 0.5f, 1f, p.t);
                XmasGeo.sweep(g, cov, arm, rr, rz, null, 6, 1);
            }
            g.color(0xFFFFFFFF);
            // --- effects
            if (fx == 1 || fx == 2) {
                g.glow(true);
                for (int i = 0; i < 7; i++) {
                    float ph = (p.t * 1.3f + i * 0.37f) % 1f;
                    float s = 0.25f + 0.7f * ((i * 0.61f) % 1f);
                    float[] e = edge(S, s, spanK, 0.3f);
                    float th = (float) Math.toRadians(lerp(-8, 60, s));
                    float L = S * (0.55f + 0.35f * s) + ph * 5;
                    float x = e[0] + (float) Math.sin(th) * L, y = e[1] - (float) Math.cos(th) * L - ph * 3;
                    g.color(argb((int) (230 * Math.sin(ph * Math.PI)), 0xFFFFFF));
                    sprite(g, "flame", x, y + 1.5f, e[2], 3.2f * (1 - ph * 0.5f));
                }
            } else if (fx == 3) {
                g.glow(true);
                for (int i = 0; i < 6; i++) {
                    float ph = (p.t * 0.6f + i / 6f) % 1f;
                    float s = 0.2f + 0.75f * ((i * 0.47f) % 1f);
                    float[] e = edge(S, s, spanK, 0.4f);
                    float L = S * 0.7f + ph * 6;
                    g.color(argb((int) (140 * Math.sin(ph * Math.PI)), 0xFFFFFF));
                    sprite(g, "orb_purple", e[0] + s * 4, e[1] - L, e[2] - 0.5f, 3 + ph * 3);
                }
            } else if (fx == 5) {
                g.glow(true);
                for (int i = 0; i < 8; i++) {
                    float tw = 0.5f + 0.5f * sin(p.t * 3 + i * 2.3f);
                    float s = (i * 0.37f) % 1f;
                    float[] e = edge(S, 0.1f + 0.85f * s, spanK, -0.8f);
                    g.color(argb((int) (255 * tw), 0xFFFFFF));
                    sprite(g, "spark", e[0] + 2, e[1] - S * (0.15f + 0.4f * ((i * 0.53f) % 1f)), e[2] - 0.6f, 1.2f + 1.6f * tw);
                }
            }
            g.color(0xFFFFFFFF).glow(false);
            g.pop();
        }
    }

    private static void tint(G g, int fx, float f, float bright, float t) {
        if (fx == 4) {
            int c = hue(f * 0.75 + t * 0.12, 0.5, bright);
            g.color(c);
        } else {
            int v = (int) (255 * bright);
            g.color(0xFF000000 | (v << 16) | (v << 8) | v);
        }
    }

    /** Point on the leading edge (arm) at span s. */
    private static float[] edge(float S, float s, float spanK, float z) {
        float x = S * s * spanK;
        float y = S * (0.26f * sin(Math.PI * s * 0.92) + 0.1f * s);
        float zz = -S * 0.09f * sin(Math.PI * s) + z;
        return new float[]{x, y, zz};
    }

    /**
     * One feather: curved strip of `segs` segments hanging from p, rotated th degrees outward. Each segment has a
     * V shaped cross section (vane halves angled back from a raised shaft) so the feathers catch light like real ones.
     */
    private static void feather(G g, String tex, float[] p, float th, float len, float w, int segs, float bend) {
        g.push();
        g.translate(p[0], p[1], p[2]);
        g.rotZ(th);
        g.rotY(-6);
        int n = segs + (segs >= 3 ? 1 : 0);
        float l = len / n;
        float b = bend * segs / n;
        float cz = w * 0.12f;
        for (int j = 0; j < n; j++) {
            float v0 = j / (float) n, v1 = (j + 1) / (float) n;
            // taper the tip
            float w0 = w * (j == n - 1 && n > 1 ? 1f : 1f), w1 = w * (j == n - 1 ? 0.55f : 1f);
            g.quad(tex, new float[]{-w0 / 2, 0, -cz * 0.3f}, new float[]{0, 0, cz}, new float[]{0, -l, cz}, new float[]{-w1 / 2, -l, -cz * 0.3f},
                    new float[]{0, v0, 0.5f, v0, 0.5f, v1, 0.5f - 0.5f * w1 / w, v1});
            g.quad(tex, new float[]{0, 0, cz}, new float[]{w0 / 2, 0, -cz * 0.3f}, new float[]{w1 / 2, -l, -cz * 0.3f}, new float[]{0, -l, cz},
                    new float[]{0.5f, v0, 1, v0, 0.5f + 0.5f * w1 / w, v1, 0.5f, v1});
            g.translate(0, -l, 0);
            g.rotX(-b);
        }
        g.pop();
    }

    // ================================================================== membrane
    private static float[][] memPoints(float S, float fold, float flap) {
        float[] O = {0, 0, 0};
        float a1 = (float) Math.toRadians(34 - fold * 10);
        float[] E = {O[0] + 0.42f * S * cos(a1), O[1] + 0.42f * S * sin(a1), 0};
        float a2 = (float) Math.toRadians(-6 - fold * 22);
        float[] W = {E[0] + 0.44f * S * cos(a2), E[1] + 0.44f * S * sin(a2), -0.6f};
        float[] ang = {30, -6, -40, -72};
        float[] len = {0.64f, 0.74f, 0.62f, 0.5f};
        float[][] out = new float[7][];
        out[0] = O; out[1] = E; out[2] = W;
        for (int k = 0; k < 4; k++) {
            float an = (float) Math.toRadians(lerp(ang[k], -95, fold * 0.35f) - flap * 0.1f * k);
            float L = len[k] * S * (1 - 0.15f * fold);
            float d = L;
            out[3 + k] = new float[]{W[0] + L * cos(an), W[1] + L * sin(an), W[2] - 0.12f * d};
        }
        return out;
    }

    static void membrane(G g, A a, String style, float S, int boneColor, int edgeGlow, boolean glowMem) {
        Pose p = pose(a, 1.2f);
        float[][] ref = memPoints(S, 0, 0);
        float[][] pt = memPoints(S, p.fold, p.flap);
        float[] B = {0.4f, -0.8f * S, 0.2f}, Bref = B;
        String mem = "mem_" + style;
        float minX = -0.05f * S, maxX = 1.35f * S, maxY = 0.62f * S, minY = -0.95f * S;
        for (int side = 1; side >= -1; side -= 2) {
            g.push();
            root(g, p, side, 1.0f, -1.6f);
            // edge polyline (actual + reference for uv)
            java.util.List<float[]> e = new java.util.ArrayList<>(), er = new java.util.ArrayList<>();
            for (int k = 0; k < 4; k++) {
                float[] ta = pt[3 + k], tb = k < 3 ? pt[4 + k] : B;
                float[] ra = ref[3 + k], rb = k < 3 ? ref[4 + k] : Bref;
                int sub = 5;
                for (int i = 0; i < sub; i++) {
                    float t = i / (float) sub;
                    e.add(scallop(ta, tb, pt[2], t, k < 3 ? 0.26f : 0.12f));
                    er.add(scallop(ra, rb, ref[2], t, k < 3 ? 0.26f : 0.12f));
                }
            }
            e.add(B); er.add(Bref);
            e.add(pt[0]); er.add(ref[0]);
            e.add(pt[1]); er.add(ref[1]);
            float[] W = pt[2], Wr = ref[2];
            g.glow(glowMem);
            g.color(0xFFFFFFFF);
            for (int i = 0; i + 1 < e.size(); i++) {
                float[] uw = uv(Wr, minX, maxX, minY, maxY), u1 = uv(er.get(i), minX, maxX, minY, maxY), u2 = uv(er.get(i + 1), minX, maxX, minY, maxY);
                tri(g, mem, W, e.get(i), e.get(i + 1), uw, u1, u2);
            }
            // bones
            g.glow(false);
            g.color(boneColor);
            tube(g, "bone", pt[0], pt[1], 0.62f, 0.5f, 6);
            tube(g, "bone", pt[1], pt[2], 0.5f, 0.38f, 6);
            for (int k = 0; k < 4; k++) {
                float[] tip = pt[3 + k];
                float[] mid = {lerp(W[0], tip[0], 0.5f), lerp(W[1], tip[1], 0.5f) + 0.4f, lerp(W[2], tip[2], 0.5f) - 0.25f};
                tube(g, "bone", W, mid, 0.34f, 0.24f, 5);
                tube(g, "bone", mid, tip, 0.24f, 0.06f, 5);
            }
            g.push();
            g.translate(pt[1][0], pt[1][1], pt[1][2]);
            g.sphere("bone", 0.6f, 6, 4, 0, 0, 1, 1);
            g.pop();
            g.push();
            g.translate(W[0], W[1], W[2]);
            g.sphere("bone", 0.5f, 6, 4, 0, 0, 1, 1);
            g.pop();
            // thumb claw
            g.color(0xFFE8E0D0);
            tube(g, "horn", W, new float[]{W[0] + 0.6f, W[1] + 1.9f, W[2] - 0.2f}, 0.34f, 0.02f, 5);
            if (edgeGlow != 0) {
                g.glow(true);
                g.color(edgeGlow);
                for (int i = 0; i + 1 < e.size() - 3; i++) tube(g, "white", e.get(i), e.get(i + 1), 0.16f, 0.16f, 4);
                for (int k = 0; k < 4; k++) tube(g, "white", W, pt[3 + k], 0.1f, 0.05f, 4);
            }
            g.color(0xFFFFFFFF).glow(false);
            g.pop();
        }
    }

    private static float[] scallop(float[] a, float[] b, float[] w, float t, float pull) {
        float mx = (a[0] + b[0]) / 2, my = (a[1] + b[1]) / 2, mz = (a[2] + b[2]) / 2;
        float cx = mx + (w[0] - mx) * pull, cy = my + (w[1] - my) * pull, cz = mz + (w[2] - mz) * pull;
        float u = 1 - t;
        return new float[]{u * u * a[0] + 2 * u * t * cx + t * t * b[0], u * u * a[1] + 2 * u * t * cy + t * t * b[1], u * u * a[2] + 2 * u * t * cz + t * t * b[2]};
    }

    private static float[] uv(float[] p, float minX, float maxX, float minY, float maxY) {
        return new float[]{(p[0] - minX) / (maxX - minX), (maxY - p[1]) / (maxY - minY)};
    }

    // ================================================================== butterfly
    static void butterfly(G g, A a) {
        float t = a.time;
        float mv = a.preview ? 0 : a.move;
        float flutter = a.sneak ? 72 : lerp(18 + 22 * (0.5f + 0.5f * sin(t * 2.6)), 20 + 45 * (0.5f + 0.5f * sin(t * 12)), mv);
        for (int side = 1; side >= -1; side -= 2) {
            g.push();
            g.scale(side, 1, 1);
            g.translate(0.5f, 2.5f, -2.4f);
            g.rotY(flutter);
            g.rotZ(8);
            grid(g, "wing_butterfly2", 13.5f, 17, 5, 6, 0.014f, true);
            g.pop();
        }
        // body / antennae hint between the wings
        g.color(0xFF15101C);
        tube(g, "felt_black", new float[]{0, 2.5f, -2.55f}, new float[]{0, -9, -2.55f}, 0.7f, 0.4f, 6);
        g.color(0xFFFFFFFF);
    }

    /** Curved card (grid of quads) from the local origin extending +x / -y with uv 0..1. */
    private static void grid(G g, String tex, float w, float h, int nx, int ny, float curve, boolean tl) {
        for (int j = 0; j < ny; j++)
            for (int i = 0; i < nx; i++) {
                float u0 = i / (float) nx, u1 = (i + 1) / (float) nx, v0 = j / (float) ny, v1 = (j + 1) / (float) ny;
                g.quad(tex, gp(w, h, u0, v0, curve), gp(w, h, u1, v0, curve), gp(w, h, u1, v1, curve), gp(w, h, u0, v1, curve),
                        new float[]{u0, v0, u1, v0, u1, v1, u0, v1});
            }
    }

    private static float[] gp(float w, float h, float u, float v, float curve) {
        float x = u * w, y = -v * h;
        return new float[]{x, y, -curve * (x * x + y * y * 0.4f)};
    }

    // ================================================================== fairy
    static void fairy(G g, A a) {
        float t = a.time;
        float mv = a.preview ? 0 : a.move;
        float fl = a.sneak ? 70 : lerp(22 + 18 * (0.5f + 0.5f * sin(t * 3.4)), 20 + 40 * (0.5f + 0.5f * sin(t * 16)), mv);
        g.glow(true);
        for (int side = 1; side >= -1; side -= 2) {
            for (int lobe = 0; lobe < 2; lobe++) {
                g.push();
                g.scale(side, 1, 1);
                g.translate(0.6f, lobe == 0 ? -1.5f : -4f, -2.4f);
                g.rotY(fl + (lobe == 0 ? 0 : 8) + sin(t * 3.4 + lobe) * 3);
                g.rotZ(lobe == 0 ? 28 : -34);
                float w = lobe == 0 ? 17 : 12, h = lobe == 0 ? 8.5f : 6;
                g.color(0xE6FFFFFF);
                for (int i = 0; i < 4; i++) {
                    float u0 = i / 4f, u1 = (i + 1) / 4f;
                    float z0 = -0.012f * (u0 * w) * (u0 * w), z1 = -0.012f * (u1 * w) * (u1 * w);
                    g.quad("wing_fairy", new float[]{u0 * w, h / 2, z0}, new float[]{u1 * w, h / 2, z1}, new float[]{u1 * w, -h / 2, z1}, new float[]{u0 * w, -h / 2, z0},
                            new float[]{u0, 0, u1, 0, u1, 1, u0, 1});
                }
                g.pop();
            }
        }
        // glitter trail
        for (int i = 0; i < 10; i++) {
            float ph = (t * 0.5f + i / 10f) % 1f;
            float x = sin(i * 2.7f) * 9, z = -4 - (i % 3) * 1.5f;
            g.color(argb((int) (255 * Math.sin(ph * Math.PI)), 0xFFFFFF));
            sprite(g, i % 2 == 0 ? "spark" : "orb_cyan", x + sin(t * 2 + i) * 1.2f, -2 - ph * 16, z, 1.6f * (1 - ph * 0.5f));
        }
        g.color(0xFFFFFFFF).glow(false);
    }

    // ================================================================== cyber / mech
    static void cyber(G g, A a) {
        Pose p = pose(a, 0.8f);
        float S = 17;
        for (int side = 1; side >= -1; side -= 2) {
            g.push();
            root(g, p, side, 1.0f, -2f);
            // arm frame
            float[] O = {0, 0, 0}, E = {S * 0.4f, S * 0.22f, -0.3f}, W = {S * 0.82f * (1 - 0.15f * p.fold), S * 0.26f, -0.8f};
            g.color(0xFFFFFFFF);
            tube(g, "mech_frame", O, E, 0.9f, 0.75f, 6);
            tube(g, "mech_frame", E, W, 0.75f, 0.55f, 6);
            g.glow(true);
            g.color(0xFF2AE6FF);
            tube(g, "white", new float[]{O[0], O[1] + 0.8f, O[2]}, new float[]{E[0], E[1] + 0.7f, E[2]}, 0.14f, 0.14f, 4);
            tube(g, "white", new float[]{E[0], E[1] + 0.7f, E[2]}, new float[]{W[0], W[1] + 0.5f, W[2]}, 0.14f, 0.14f, 4);
            sprite(g, "orb_cyan", E[0], E[1], E[2] - 0.8f, 2.6f + 0.6f * sin(p.t * 4));
            sprite(g, "orb_cyan", W[0], W[1], W[2] - 0.8f, 2.2f + 0.6f * sin(p.t * 4 + 1));
            g.glow(false);
            // blades
            int n = 8;
            for (int i = 0; i < n; i++) {
                float f = i / (float) (n - 1);
                float[] at = {lerp(1.5f, W[0], f), lerp(0.5f, W[1], f) - 0.3f, lerp(0, W[2], f) - 0.2f + i * 0.05f};
                float th = lerp(-4, 62, f) * (1 - 0.6f * p.fold) + sin(p.t * 2 + i) * 1.5f;
                float L = S * (0.5f + 0.42f * sin(Math.PI * (0.25 + 0.6 * f)));
                g.push();
                g.translate(at[0], at[1], at[2]);
                g.rotZ(th);
                g.rotY(-8);
                float w = S * 0.14f;
                float[][] uvs = {{0, 0, 1, 1}, {0, 0, 1, 1}, {0, 0, 0.1f, 1}, {0.9f, 0, 1, 1}, {0, 0, 1, 0.05f}, {0, 0.95f, 1, 1}};
                g.color(0xFFFFFFFF);
                g.box6("mech_blade", -w / 2, -L, -0.18f, w / 2, 0, 0.18f, uvs);
                g.glow(true);
                g.color(argb(200 + (int) (55 * sin(p.t * 5 + i)), 0x2AE6FF));
                g.quad("glow_line", new float[]{-w / 2, -0.3f, -0.2f}, new float[]{w / 2, -0.3f, -0.2f}, new float[]{w / 2, -0.8f, -0.2f}, new float[]{-w / 2, -0.8f, -0.2f}, new float[]{0, 0, 1, 0, 1, 1, 0, 1});
                g.glow(false);
                g.pop();
            }
            g.color(0xFFFFFFFF);
            g.pop();
        }
        // back mount with thruster glow
        g.color(0xFFFFFFFF);
        g.box("mech_frame", -2.2f, -5.5f, -3.3f, 2.2f, -0.8f, -2.05f, 0, 0, 1, 1);
        g.glow(true);
        g.color(0xFF2AE6FF);
        g.push();
        g.translate(0, 0, -3.36f);
        g.plane("glow_line", 0, -3.1f, 3.4f, 0.7f, 0, 0, 1, 1);
        g.pop();
        g.glow(false);
        g.color(0xFFFFFFFF);
    }
}
