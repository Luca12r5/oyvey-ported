package dev.lego.cosmetic;

import dev.lego.cosmetic.Cos.A;
import dev.lego.cosmetic.Pets.P;
import dev.lego.cosmetic.Pets.Q4;

import static dev.lego.cosmetic.PetGeo.blob;
import static dev.lego.cosmetic.PetGeo.blob2;
import static dev.lego.cosmetic.PetGeo.cone;
import static dev.lego.cosmetic.PetGeo.cos;
import static dev.lego.cosmetic.PetGeo.sin;
import static dev.lego.cosmetic.Pets.col;
import static dev.lego.cosmetic.Pets.face;
import static dev.lego.cosmetic.Pets.fur;
import static dev.lego.cosmetic.Pets.fx;
import static dev.lego.cosmetic.Pets.gl;
import static dev.lego.cosmetic.Pets.head;
import static dev.lego.cosmetic.Pets.headTurn;
import static dev.lego.cosmetic.Pets.pose;
import static dev.lego.cosmetic.Pets.quad4;
import static dev.lego.cosmetic.Pets.root;

/** The individual pet models (pet space, see {@link Pets}). */
final class PetModels {
    private PetModels() { }

    // ================================================================== four-legged friends
    static void cat(G g, A a) {
        Q4 q = new Q4();
        q.body = fur("cat"); q.head = head("cat"); q.leg = col(0xFFF1E2); q.style = "cat";
        q.nose = 0xFF8FA8; q.noseR = 0.26f;
        q.ears = (gg, p) -> {
            for (int s = -1; s <= 1; s += 2) Pets.earPointy(gg, p, col(0xF7A451), col(0xFFB3C6), s * 1.85f, 2.05f, -0.25f, s, 1.08f, 1.95f, 10);
        };
        q.tail = (gg, p) -> Pets.tail(gg, p, new String[]{col(0xF7A451), col(0xF7A451), col(0xE88A3C), col(0xD9742B)},
                new float[]{0.47f, 0.44f, 0.41f, 0.39f}, 1.15f, 19, 30, 1f);
        quad4(g, pose(a), q);
    }

    static void shiba(G g, A a) {
        Q4 q = new Q4();
        q.body = fur("shiba"); q.head = head("shiba"); q.leg = col(0xFFF7EC); q.style = "dog";
        q.nose = 0x2A2024; q.noseR = 0.34f;
        q.ears = (gg, p) -> {
            for (int s = -1; s <= 1; s += 2) Pets.earPointy(gg, p, col(0xEE9748), col(0xFFF1E4), s * 1.8f, 2.0f, -0.2f, s, 1.12f, 1.7f, 9);
        };
        q.tail = (gg, p) -> Pets.tail(gg, p, new String[]{col(0xEE9748), col(0xEE9748), col(0xF4B070), col(0xFFF7EC)},
                new float[]{0.72f, 0.68f, 0.6f, 0.48f}, 1.0f, 44, 62, 0.8f);
        quad4(g, pose(a), q);
    }

    static void fox(G g, A a) {
        Q4 q = new Q4();
        q.body = fur("fox"); q.head = head("fox"); q.leg = col(0x4A2A22); q.style = "amber";
        q.nose = 0x2A2024; q.noseR = 0.3f;
        q.headRx = 3.45f;
        q.ears = (gg, p) -> {
            for (int s = -1; s <= 1; s += 2)
                Pets.earPointy(gg, p, col(0xF7863A), col(0xFFF3E8), col(0x4A2A22), s * 1.9f, 2.0f, -0.3f, s, 1.25f, 2.35f, 14);
        };
        q.tail = (gg, p) -> Pets.tail(gg, p, new String[]{col(0xF7863A), col(0xF7863A), col(0xF7863A), col(0xFFF6EC)},
                new float[]{0.85f, 1.12f, 1.12f, 0.8f}, 1.25f, 12, 18, 1f);
        quad4(g, pose(a), q);
    }

    static void redPanda(G g, A a) {
        Q4 q = new Q4();
        q.body = fur("redpanda"); q.head = head("redpanda"); q.leg = col(0x4A2418); q.style = "std";
        q.nose = 0x2A1A18; q.noseR = 0.3f;
        q.headRx = 3.5f;
        q.ears = (gg, p) -> {
            for (int s = -1; s <= 1; s += 2) Pets.earPointy(gg, p, col(0xD3602E), col(0xFFF6EA), s * 2.0f, 1.9f, -0.3f, s, 1.2f, 1.4f, 22);
        };
        String r = col(0xD3602E), d = col(0x7A3418);
        q.tail = (gg, p) -> Pets.tail(gg, p, new String[]{r, d, r, d, r}, new float[]{0.75f, 0.84f, 0.86f, 0.78f, 0.62f}, 1.0f, 9, 16, 1f);
        quad4(g, pose(a), q);
    }

    static void panda(G g, A a) {
        Q4 q = new Q4();
        q.body = fur("panda"); q.head = head("panda"); q.leg = fur("pdark"); q.style = "rim";
        q.nose = 0x2A2A33; q.noseR = 0.32f;
        q.bodyRx = 2.4f; q.headRx = 3.45f;
        q.ears = (gg, p) -> {
            for (int s = -1; s <= 1; s += 2) Pets.earRound(gg, p, fur("pdark"), null, s * 2.25f, 1.95f, -0.3f, s, 1.0f);
        };
        q.tail = (gg, p) -> blob(gg, col(0xFBFBF7), 0, 0.4f, 0, 0.6f, 0.6f, 0.5f, 7, 4);
        quad4(g, pose(a), q);
    }

    static void bear(G g, A a) {
        Q4 q = new Q4();
        q.body = fur("bear"); q.head = head("bear"); q.leg = col(0x9E6A40); q.style = "std";
        q.nose = 0x4A2A1E; q.noseR = 0.36f;
        q.bodyRx = 2.4f; q.headRx = 3.4f;
        q.ears = (gg, p) -> {
            for (int s = -1; s <= 1; s += 2) Pets.earRound(gg, p, col(0xB9804E), col(0xF0D2A8), s * 2.25f, 1.9f, -0.25f, s, 0.95f);
        };
        q.tail = (gg, p) -> blob(gg, col(0xB9804E), 0, 0.3f, 0, 0.55f, 0.55f, 0.5f, 7, 4);
        quad4(g, pose(a), q);
    }

    static void hamster(G g, A a) {
        Q4 q = new Q4();
        q.body = fur("hamster"); q.head = head("hamster"); q.leg = col(0xFFE4D6); q.style = "std";
        q.nose = 0xFF8FA8; q.noseR = 0.24f;
        q.bodyY = 2.3f; q.bodyRx = 2.7f; q.bodyRy = 2.25f; q.bodyRz = 2.5f;
        q.headY = 5.25f; q.headZ = 0.75f; q.headRx = 3.7f; q.headRy = 2.85f; q.headRz = 2.85f;
        q.legR = 0.55f; q.legH = 0.8f; q.legX = 1.4f;
        q.span = 0.95f;
        q.ears = (gg, p) -> {
            for (int s = -1; s <= 1; s += 2) Pets.earRound(gg, p, col(0xF0B064), col(0xFFB3C6), s * 2.1f, 2.0f, -0.4f, s, 0.78f);
        };
        q.bodyExtra = (gg, p) -> {
            // holding a sunflower seed
            gg.push();
            // holding a little strawberry
            gg.translate(0, 0.55f, q.bodyRz + 0.05f);
            gg.rotX(-10);
            blob(gg, "pet_berry", 0, 0, 0.1f, 0.62f, 0.7f, 0.5f, 6, 4);
            gg.push();
            gg.translate(0, 0.55f, 0.1f);
            gg.scale(1, 0.5f, 1);
            cone(gg, col(0x5CC45A), 0.5f, 0.6f, 6, false);
            gg.pop();
            for (int s = -1; s <= 1; s += 2) blob(gg, col(0xFFE4D6), s * 0.62f, -0.05f, 0.15f, 0.36f, 0.32f, 0.32f, 6, 3);
            gg.pop();
        };
        q.tail = (gg, p) -> blob(gg, col(0xF0B064), 0, 0.3f, 0, 0.4f, 0.4f, 0.4f, 6, 4);
        quad4(g, pose(a), q);
    }

    static void bunny(G g, A a) {
        P pp = pose(a);
        // bunnies hop when they move: crouch, launch (stretch), fly, land (squash); legs do not trot
        float[] h = PetRig.hop(pp);
        pp.y += h[0] * (2.2f + pp.gr * 1.5f);
        pp.sy *= 1 + h[1] * 0.14f;
        pp.sx *= 1 - h[1] * 0.07f;
        pp.pitch = h[2] * 14 - Math.max(0, -h[1]) * 6;
        pp.walkW = 0;
        pp.runW = 0;
        pp.bob = 0;
        Q4 q = new Q4();
        q.body = fur("bunny"); q.head = head("bunny"); q.leg = col(0xFFFBF5); q.style = "std";
        q.nose = 0xFF8FA8; q.noseR = 0.24f;
        q.bodyRx = 2.4f; q.bodyRy = 2.1f;
        q.headY = 5.9f;
        q.ears = (gg, p) -> {
            for (int s = -1; s <= 1; s += 2) {
                gg.push();
                gg.translate(s * 1.15f, 2.2f, -0.45f);
                float droop = Math.max(0, 1 - p.ear);
                gg.rotZ(-s * (7 + droop * 72 + p.earTwitch * (s > 0 ? 1 : 0)) + sin(p.t * 1.6f + s) * 2);
                gg.rotX(-8 - droop * 42 + (p.ear > 1 ? 6 : 0));
                gg.scale(1, 1 + p.gr * 0.15f, 1);
                blob(gg, col(0xF1DCC6), 0, 2.0f, 0, 0.78f, 2.35f, 0.44f, 8, 6);
                blob(gg, col(0xFFB7C8), 0, 2.0f, 0.24f, 0.46f, 1.85f, 0.24f, 6, 4);
                gg.pop();
            }
        };
        q.tail = (gg, p) -> blob(gg, col(0xFFFFFF), 0, 0.5f, 0, 0.8f, 0.8f, 0.7f, 7, 4);
        quad4(g, pp, q);
    }

    static void unicorn(G g, A a) {
        Q4 q = new Q4();
        q.body = fur("unicorn"); q.head = head("unicorn"); q.leg = col(0xFFF4FF); q.style = "gem";
        q.legH = 1.55f; q.legR = 0.62f; q.bodyY = 2.95f; q.headY = 6.45f; q.headZ = 1.1f;
        q.ears = (gg, p) -> {
            for (int s = -1; s <= 1; s += 2) Pets.earPointy(gg, p, col(0xFFFBFF), col(0xFFC8E4), s * 1.95f, 1.95f, -0.45f, s, 0.8f, 1.4f, 18);
        };
        final int[] rainbow = {0xFF9BC8, 0xFFC98A, 0xFFF0A0, 0xA8F0C0, 0x9CD2FF, 0xC8A8FF};
        q.headExtra = (gg, p) -> {
            // golden horn
            gg.push();
            gg.translate(0, 2.55f, 0.95f);
            gg.rotX(18);
            gg.glow(true);
            cone(gg, "pet_horn", 0.5f + p.gr * 0.15f, 2.3f + p.gr * 1.6f + (p.lvl >= 5 ? 0.6f : 0), 8, false);
            gg.glow(false);
            gg.pop();
            // mane: forelock + along the back of the head
            // flowing locks hugging the back of the head, each tilted along the curve
            for (int i = 0; i < 4; i++) {
                double an = Math.toRadians(84 + i * 34);
                gg.push();
                gg.translate(-0.12f + (i % 2) * 0.24f, sin(an) * 2.8f, cos(an) * 2.65f - 0.1f);
                gg.rotX((float) (90 - Math.toDegrees(an)));
                gg.rotZ(sin(p.t * 2 + i) * 4);
                blob(gg, col(rainbow[i + 1]), 0, 0.1f, 0, 1.1f, 0.6f, 1.35f, 6, 3);
                gg.pop();
            }
            // forelock
            gg.push();
            gg.translate(0.75f, 2.35f, 1.25f);
            gg.rotZ(-35);
            blob(gg, col(rainbow[0]), 0, 0, 0, 0.75f, 0.55f, 0.6f, 7, 4);
            gg.pop();
        };
        q.tail = (gg, p) -> Pets.tail(gg, p, new String[]{col(rainbow[0]), col(rainbow[4])},
                new float[]{0.7f, 0.8f}, 1.3f, -10, 30, 0.8f);
        quad4(g, pose(a), q);
    }

    static void miniDragon(G g, A a) {
        P pp = pose(a);
        Q4 q = new Q4();
        q.body = fur("dragon"); q.head = head("dragon"); q.leg = col(0x9A80EC); q.style = "amber";
        q.nose = 0x8367E0; q.noseR = 0.16f; q.noseEl = -0.1f;
        q.ears = (gg, p) -> {
            for (int s = -1; s <= 1; s += 2) {
                gg.push();
                gg.translate(s * 1.3f, 2.3f, -0.5f);
                gg.rotZ(-s * 18);
                gg.rotX(-30);
                cone(gg, col(0xFFE9B0), 0.45f, 1.5f, 8, false);
                gg.pop();
                // little fin ears
                Pets.earPointy(gg, p, col(0xAE92F7), col(0xFFC9E8), s * 2.5f, 1.2f, -0.4f, s, 0.7f, 1.3f, 55);
            }
        };
        q.bodyExtra = (gg, p) -> {
            float flap = p.flap * (p.is("sleepy") ? 6 : 26 + 20 * p.runW);
            float ws = 1 + p.gr * 0.5f + (p.lvl >= 5 ? 0.2f : 0);
            for (int s = -1; s <= 1; s += 2) {
                gg.push();
                gg.translate(s * 0.9f, 1.45f, -0.5f);
                gg.scale(s * ws, ws, ws);
                gg.rotY(30);
                gg.rotZ(22 + flap);
                gg.planeTL("pet_dragon_wing", 4.2f, 3.2f, 0, 0, 1, 1);
                gg.pop();
            }
            // back spikes
            for (int i = 0; i < 3; i++) {
                gg.push();
                gg.translate(0, 1.85f - i * 0.25f, -0.4f - i * 0.9f);
                gg.rotX(-20 - i * 10);
                gg.scale(0.5f, 1, 1);
                cone(gg, col(0xFFD6A8), 0.45f, 0.8f, 6, false);
                gg.pop();
            }
        };
        q.tail = (gg, p) -> Pets.tail(gg, p, new String[]{col(0xAE92F7), col(0xAE92F7), col(0x9A80EC), col(0xFFB3DA)},
                new float[]{0.7f, 0.56f, 0.44f, 0.4f}, 1.1f, 6, 8, 1.2f);
        quad4(g, pp, q);
    }

    // ================================================================== round birds
    /** Beak pointing forward; open 0..1. */
    private static void beak(G g, float y, float z, float s, float open, int top, int bot) {
        g.push();
        g.translate(0, y, z);
        g.push();
        g.rotX(90 - open * 18);
        g.scale(1, 1, 0.62f);
        cone(g, gl(top), 0.62f * s, 1.0f * s, 8, true);
        g.pop();
        g.push();
        g.translate(0, -0.12f * s, 0);
        g.rotX(90 + open * 26);
        g.scale(1, 1, 0.5f);
        cone(g, gl(bot), 0.5f * s, 0.72f * s, 8, true);
        g.pop();
        g.pop();
    }

    private static float beakOpen(P p) {
        switch (p.emo) {
            case "excited": return 0.6f + 0.4f * Math.abs(sin(p.t * 7));
            case "happy": return 0.45f;
            case "angry": return 0.25f + 0.2f * Math.abs(sin(p.t * 12));
            case "curious": return 0.2f;
            default: return 0.05f;
        }
    }

    /** Wing flap amplitude by emotion (deg). */
    private static float wingFlap(P p) {
        switch (p.emo) {
            case "excited": return 55;
            case "happy": return 22;
            case "love": return 14;
            case "angry": return 30;
            case "sad": case "sleepy": return 3;
            default: return 10;
        }
    }

    /** Waddling feet (legacy name, used by the mushroom too). */
    private static void feet2(G g, P p, String tex, float x, float z, float r) {
        PetRig.feet(g, p, tex, x, z, r, 0.9f);
    }

    /** Thin bird legs (visible once grown) from the body down to the feet. */
    private static void birdLegs(G g, P p, String tex, float x, float h) {
        if (h < 0.2f) return;
        for (int s = -1; s <= 1; s += 2) {
            float c = PetRig.frac(p.t * PetRig.WADDLE_HZ + (s > 0 ? 0 : 0.5f));
            float lift = p.walkW * Math.max(0, sin(Math.PI * 2 * c)) * 0.45f;
            g.push();
            g.translate(s * x, 0.3f + lift, 0.3f);
            g.cylinder(tex, 0.2f, 0.16f, 0, h + 0.2f, 5, false, 0, 0, 1, 1);
            g.pop();
        }
    }

    static void chick(G g, A a) {
        P p = pose(a);
        float k = p.gr;
        g.push();
        root(g, p);
        g.translate(0, p.waddleBob, 0);
        float legH = k * 1.3f;
        feet2(g, p, gl(0xFFA43C), 1.1f, 0.4f, 0.55f + k * 0.1f);
        birdLegs(g, p, gl(0xFFA43C), 1.0f, legH);
        // grows into a little hen: longer legs, oval body, comb + wattle, tail feathers
        float by = 3.15f + legH - p.lie * 0.5f, rx = 3.0f, ry = 2.85f + k * 0.2f, rz = 2.85f + k * 0.6f;
        g.push();
        g.translate(0, by, 0);
        g.rotZ(p.waddle);
        g.rotY(p.waddle * 0.4f);
        // wings (body space, before the head turn)
        float fl = wingFlap(p), fs = p.is("excited") ? 18 : 9;
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 2.7f, 0.1f, -0.2f);
            g.rotZ(s * (12 + fl * (0.5f + 0.5f * sin(p.t * fs))) + (p.is("sad") ? -s * 10 : 0) + s * p.walkW * 10 * Math.abs(sin(p.t * 9)));
            blob(g, col(0xFFD84A), s * 0.1f, -0.9f, 0, 0.45f, 1.2f + k * 0.3f, 0.95f + k * 0.5f, 7, 4);
            g.pop();
        }
        // tail feathers
        g.push();
        g.translate(0, 0.3f + k * 0.8f, -rz + 0.2f);
        g.rotX(-30 - k * 25 + p.tailSway * 0.5f);
        blob(g, col(k > 0.5f ? 0xFFC83A : 0xFFD84A), 0, 0, -0.3f, 0.8f + k * 0.3f, 0.5f + k * 0.9f, 0.6f + k * 0.4f, 7, 4);
        g.pop();
        g.rotY(p.headYaw * 0.6f);
        g.rotX(p.headPitch * 0.5f + p.walkW * 6 * sin(p.t * Math.PI * 2 * PetRig.WADDLE_HZ * 2));
        g.rotZ(p.headRoll);
        blob(g, fur("chick"), 0, 0, 0, rx, ry, rz, 16, 11);
        face(g, p, PetRig.style("beak", k), 0, 0, 0, rx, ry, rz, 0.95f, 0.28f);
        beak(g, sin(0.16f) * ry, cos(0.16f) * rz * 0.95f, 0.85f, beakOpen(p), 0xFFA43C, 0xF08A2A);
        if (k > 0.35f) {
            // comb + wattle
            float m = Math.min(1, (k - 0.35f) / 0.45f);
            for (int i = -1; i <= 1; i++) blob(g, gl(0xE8303A), 0, ry - 0.1f + (1 - Math.abs(i)) * 0.35f * m, 0.9f + i * 0.7f, 0.25f * m + 0.05f, 0.55f * m + 0.05f, 0.4f * m + 0.05f, 5, 3);
            blob(g, gl(0xE8303A), 0, -0.6f, rz * 0.95f + 0.15f, 0.25f * m, 0.45f * m, 0.2f * m, 5, 3);
        } else {
            // chick tuft
            for (int i = -1; i <= 1; i++) {
                g.push();
                g.translate(i * 0.3f, ry - 0.2f, 0.2f);
                g.rotZ(-i * 28 + sin(p.t * 3) * 6);
                blob(g, col(0xFFE46A), 0, 0.55f, 0, 0.26f, 0.7f, 0.26f, 6, 4);
                g.pop();
            }
        }
        g.pop();
        fx(g, p, by + ry + 1.5f);
        g.pop();
    }

    static void penguin(G g, A a) {
        P p = pose(a);
        float k = p.gr;
        g.push();
        root(g, p);
        g.translate(0, p.waddleBob * 0.7f, 0);
        feet2(g, p, gl(0xFFA43C), 1.2f, 0.3f, 0.6f + k * 0.15f);
        // grows taller and slimmer (emperor penguin), yellow neck patches
        float by = 3.55f + k * 1.1f - p.lie * 0.6f, rx = 2.75f, ry = 3.3f + k * 1.3f, rz = 2.55f;
        g.push();
        g.translate(0, by, 0);
        g.rotZ(p.waddle * 1.2f);
        g.rotY(p.waddle * 0.5f);
        float fl = wingFlap(p), fs = p.is("excited") ? 16 : 7;
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 2.45f, 0.5f + k * 0.5f, -0.1f);
            // flippers balance the waddle
            g.rotZ(s * (14 + fl * (0.5f + 0.5f * sin(p.t * fs)) + p.walkW * 18) - p.waddle * 0.8f + (p.is("sad") ? -s * 8 : 0));
            blob(g, col(0x34426A), s * 0.15f, -1.2f - k * 0.4f, 0, 0.42f, 1.6f + k * 0.6f, 0.9f, 7, 4);
            g.pop();
        }
        g.rotY(p.headYaw * 0.6f);
        g.rotX(p.headPitch * 0.4f);
        g.rotZ(p.headRoll * 0.8f);
        blob(g, fur("penguin"), 0, 0, 0, rx, ry, rz, 18, 12);
        face(g, p, PetRig.style("beak", k), 0, (ry - 3.3f) * 0.6f, 0, rx, 3.3f, rz, 0.9f, 0.42f);
        beak(g, sin(0.3f) * 3.3f + (ry - 3.3f) * 0.6f, cos(0.3f) * rz * 0.95f, 0.85f + k * 0.2f, beakOpen(p), 0xFFA43C, 0xF08A2A);
        if (k > 0.4f) {
            float m = Math.min(1, (k - 0.4f) / 0.4f);
            for (int s = -1; s <= 1; s += 2) blob(g, gl(0xFFC23A), s * 1.9f, ry * 0.45f, 1.2f, 0.35f * m, 0.8f * m, 0.5f * m, 5, 3);
        }
        g.pop();
        fx(g, p, by + ry + 1.2f);
        g.pop();
    }

    static void owl(G g, A a) {
        P p = pose(a);
        float k = p.gr;
        // owls take off when you move faster
        float fly = PetRig.smooth(0.45f, 0.85f, p.sp);
        float[] fs2 = PetRig.flap(p, 2.4f);
        g.push();
        root(g, p);
        g.translate(0, fly * (5 + fs2[1] * 0.8f) + p.waddleBob * (1 - fly), 0);
        if (fly < 0.9f) feet2(g, p, gl(0xF2B24A), 1.0f, 0.5f, 0.45f + k * 0.1f);
        float by = 2.75f + k * 0.4f - p.lie * 0.4f;
        g.push();
        g.translate(0, by, 0);
        g.rotZ(p.waddle * (1 - fly));
        g.rotX(fly * 25);
        blob(g, fur("owl"), 0, 0, 0, 2.8f, 2.6f + k * 0.6f, 2.6f, 14, 8);
        float fl = wingFlap(p), fs = p.is("excited") ? 16 : 8;
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 2.5f, 1.0f, -0.3f);
            float ground = s * (8 + fl * (0.5f + 0.5f * sin(p.t * fs))) + (p.is("sad") ? -s * 6 : 0);
            float air = s * (70 + fs2[0] * 55);
            g.rotZ(ground * (1 - fly) + air * fly);
            blob(g, col(0x8A6040), s * 0.15f, -1.1f - k * 0.3f, 0, 0.5f, 1.6f + k * 0.7f + fly * 0.6f, 1.3f + k * 0.3f, 7, 4);
            g.pop();
        }
        g.pop();
        // head (relatively smaller when grown)
        float hs = 1 - k * 0.12f;
        g.push();
        g.translate(0, 5.75f + k * 0.7f - p.lie * 0.7f, 0.25f);
        headTurn(g, p);
        g.scale(hs);
        float rx = 3.2f, ry = 2.6f, rz = 2.75f;
        blob(g, head("owl"), 0, 0, 0, rx, ry, rz, 17, 11);
        face(g, p, PetRig.style("owl", k), 0, 0, 0, rx, ry, rz, 1.0f, 0f);
        g.push();
        g.translate(0, -0.25f, rz * 0.96f);
        g.rotX(150);
        g.scale(1, 1, 0.7f);
        cone(g, gl(0xF2B24A), 0.42f, 1.0f, 8, true);
        g.pop();
        for (int s = -1; s <= 1; s += 2) Pets.earPointy(g, p, col(0x8A6040), col(0xA7784F), s * 2.1f, 1.7f, -0.2f, s, 0.75f + k * 0.15f, 1.6f + k * 0.9f, 28);
        g.pop();
        fx(g, p, 5.75f + k * 0.7f + 2.6f * hs + 1.4f);
        g.pop();
    }

    // ================================================================== frog / axolotl / turtle
    static void frog(G g, A a) {
        P p = pose(a);
        // frogs hop when they move: squash, launch with stretched legs, land
        float[] h = PetRig.hop(p);
        p.y += h[0] * 2.4f;
        p.sy *= 1 + h[1] * 0.12f;
        p.sx *= 1 - h[1] * 0.06f;
        p.stepBob = 0;
        float air = h[2];
        g.push();
        root(g, p);
        g.rotX(-air * 18);
        String green = col(0x86D160), light = col(0xA6E07E);
        float squat = p.lie * 0.4f + Math.max(0, -h[1]) * 0.35f;
        // back legs (folded, stretched back while airborne) + feet
        for (int s = -1; s <= 1; s += 2) {
            blob(g, green, s * 2.15f, 1.05f - squat - air * 0.3f, -0.9f - air * 1.0f, 1.05f - air * 0.2f, 0.95f - air * 0.25f, 1.5f + air * 0.9f, 7, 4);
            blob(g, light, s * 2.45f, 0.22f + air * 0.1f, 0.35f - air * 2.6f, 0.85f, 0.24f, 1.15f, 8, 4);
            // front arms
            g.push();
            g.translate(s * 1.3f, 1.4f - squat, 1.35f);
            g.rotX(s * p.walk * 0.6f);
            blob(g, green, 0, -0.6f, 0, 0.42f, 0.8f, 0.42f, 6, 3);
            blob(g, light, 0, -1.25f, 0.2f, 0.55f, 0.18f, 0.55f, 5, 3);
            g.pop();
        }
        blob(g, fur("frog"), 0, 2.1f - squat, -0.3f, 2.6f, 2.0f, 2.45f, 11, 6);
        // head
        g.push();
        g.translate(0, 4.25f - squat * 1.6f, 0.55f);
        headTurn(g, p);
        float rx = 3.5f, ry = 2.2f, rz = 2.85f;
        blob(g, head("frog"), 0, 0, 0, rx, ry, rz, 15, 10);
        // mouth / blush layer on the head front
        boolean og = g.glow;
        PetGeo.decal(g, "pet_face:blob:" + p.emo, 0, 0, 0, rx, ry, rz, 0, 0.28f, 1.0f, 0, 0, 1, 1, 1.035f);
        // eye bumps: each carries one half of the eye texture
        String eyes = Pets.eyes("blob", p);
        for (int s = -1; s <= 1; s += 2) {
            float bx = s * 1.55f, byy = 1.65f, bz = 0.5f, r = 1.3f;
            blob(g, green, bx, byy, bz, r, r * 0.95f, r, 9, 6);
            float u0 = s > 0 ? 14 / 256f : 114 / 256f;
            PetGeo.decal(g, eyes, bx, byy, bz, r, r * 0.95f, r, p.lookX * 0.1f, 0.12f + p.lookY * 0.06f, 1.2f, u0, 20 / 192f, u0 + 128 / 256f, 148 / 192f, 1.05f);
        }
        g.glow(og);
        g.pop();
        fx(g, p, 7.2f);
        g.pop();
    }

    static void axolotl(G g, A a) {
        P p = pose(a);
        g.push();
        root(g, p);
        String pink = col(0xFFB8CE);
        for (int s = -1; s <= 1; s += 2) {
            for (int f = 0; f < 2; f++) {
                g.push();
                float z = f == 0 ? 0.9f : -2.1f;
                g.translate(s * 1.65f, 0.75f - p.lie * 0.3f, z);
                g.rotZ(-s * 35);
                g.rotX((f == 0 ? s : -s) * p.walk);
                blob(g, pink, 0, -0.35f, 0, 0.36f, 0.62f, 0.36f, 7, 4);
                g.pop();
            }
        }
        blob(g, fur("axolotl"), 0, 1.6f - p.lie * 0.35f, -0.6f, 2.0f, 1.45f, 2.9f, 12, 7);
        // tail fin
        g.push();
        g.translate(0, 1.85f - p.lie * 0.35f, -3.0f);
        g.rotY(p.tail * 1.2f + sin(p.t * 3) * 8 + sin(p.t * 9) * 16 * p.walkW);
        blob(g, col(0xFFC8D8), 0, 0.1f, -1.5f, 0.32f, 1.05f, 1.9f, 8, 5);
        g.pop();
        // head
        g.push();
        g.translate(0, 3.45f - p.lie * 0.6f, 1.35f);
        headTurn(g, p);
        float rx = 3.4f, ry = 2.35f, rz = 2.55f;
        blob(g, head("axolotl"), 0, 0, 0, rx, ry, rz, 15, 10);
        face(g, p, "blob", 0, 0, 0, rx, ry, rz, 1.05f, -0.02f);
        // gills
        float droop = Math.max(0, 1 - p.ear) * 30;
        for (int s = -1; s <= 1; s += 2)
            for (int i = 0; i < 3; i++) {
                g.push();
                g.translate(s * 2.75f, 1.0f - i * 0.8f, -0.6f);
                g.rotZ(-s * (62 - i * 42 + droop) + sin(p.t * 4 + i) * 7);
                g.scale(1, 1 + p.gr * 0.5f, 1);
                blob(g, col(0xF2779F), 0, 0.9f, 0, 0.32f, 1.05f, 0.28f, 6, 4);
                g.pop();
            }
        g.pop();
        fx(g, p, 7);
        g.pop();
    }

    static void turtle(G g, A a) {
        P p = pose(a);
        g.push();
        root(g, p);
        String skin = col(0x9FDB82);
        Pets.legs4(g, p, skin, 2.0f, 1.6f, -1.6f, 0.72f, 1.05f);
        float drop = p.lie * 0.6f;
        blob2(g, "pet_shell", 0, 1.95f - drop, -0.2f, 3.25f, 2.55f, 0.75f, 3.45f, 16, 10);
        // tail
        g.push();
        g.translate(0, 1.5f - drop, -3.4f);
        g.rotY(p.tail);
        g.rotX(-100);
        cone(g, skin, 0.4f, 0.9f, 6, false);
        g.pop();
        // head (retracts when sleepy)
        g.push();
        g.translate(0, 3.0f - drop, 3.45f - p.lie * 1.2f);
        headTurn(g, p);
        float rx = 2.65f, ry = 2.3f, rz = 2.3f;
        blob(g, head("turtle"), 0, 0, 0, rx, ry, rz, 17, 11);
        face(g, p, "std", 0, 0, 0, rx, ry, rz, 1.05f, 0.02f);
        g.pop();
        fx(g, p, 6.2f);
        g.pop();
    }

    // ================================================================== flyers & floaters
    static void bee(G g, A a) {
        P p = pose(a);
        g.push();
        root(g, p);
        g.translate(0, 4.3f * p.hover + sin(p.t * 2.6f) * 0.4f - p.flap * 0.18f, 0);
        g.rotX(p.sp * 16);
        // abdomen
        g.push();
        g.translate(0, -0.3f, -1.3f);
        g.rotX(-12 + sin(p.t * 2) * 4);
        blob(g, fur("bee"), 0, 0, -0.5f, 2.15f, 2.0f, 2.5f, 14, 8);
        g.push();
        g.translate(0, -0.3f, -2.9f);
        g.rotX(-95);
        cone(g, col(0x3A2A2A), 0.35f, 0.8f, 6, false);
        g.pop();
        g.pop();
        // wings
        float fl = p.flap * (p.is("sleepy") ? 10 : 34);
        int oc = g.color;
        g.color(0xD8FFFFFF).glow(true);
        for (int s = -1; s <= 1; s += 2)
            for (int w = 0; w < 2; w++) {
                g.push();
                g.translate(s * 0.7f, 1.6f, -0.9f - w * 0.6f);
                g.scale(s, 1, 1);
                g.rotY(35 + w * 20);
                g.rotZ(25 - w * 12 + fl);
                g.scale(1 + p.gr * 0.3f);
                g.plane("pet_wing", 1.4f - w * 0.3f, 0.5f, 2.8f - w * 0.7f, 1.8f - w * 0.5f, 0, 0, 1, 1);
                g.pop();
            }
        g.color(oc).glow(false);
        // head
        g.push();
        g.translate(0, 0.9f, 1.15f);
        headTurn(g, p);
        float rx = 2.6f, ry = 2.45f, rz = 2.35f;
        blob(g, head("bee"), 0, 0, 0, rx, ry, rz, 18, 11);
        face(g, p, "blob", 0, 0, 0, rx, ry, rz, 1.0f, 0f);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 0.8f, ry - 0.3f, 0.3f);
            float droop = Math.max(0, 1 - p.ear);
            g.rotZ(-s * (18 + droop * 40) + sin(p.t * 5 + s) * 6);
            g.rotX(-12 + droop * 30);
            g.cylinder(col(0x3A2A2A), 0.09f, 0.09f, 0, 1.4f, 6, false, 0, 0, 1, 1);
            blob(g, gl(0x3A2A2A), 0, 1.55f, 0, 0.36f, 0.36f, 0.36f, 7, 4);
            g.pop();
        }
        g.pop();
        // tiny legs
        for (int s = -1; s <= 1; s += 2) blob(g, col(0x3A2A2A), s * 0.8f, -1.9f, -0.4f, 0.22f, 0.5f, 0.22f, 6, 3);
        fx(g, p, 3.8f);
        g.pop();
    }

    static void bat(G g, A a) {
        P p = pose(a);
        g.push();
        root(g, p);
        // body rises on the down stroke
        g.translate(0, 4.6f * p.hover + sin(p.t * 3.0f) * 0.3f - p.flap * 0.45f, 0);
        g.rotX(p.sp * 14);
        float fl = p.flap * (p.is("sleepy") ? 8 : p.is("excited") ? 48 : 36 + p.sp * 10);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 1.9f, 0.7f, -0.5f);
            g.scale(s, 1, 1);
            g.rotY(22);
            g.rotZ(-8 + fl);
            g.scale(1 + p.gr * 0.4f);
            g.planeTL("pet_bat_wing", 4.6f, 2.3f, 0, 0, 1, 1);
            g.pop();
        }
        // feet
        for (int s = -1; s <= 1; s += 2) blob(g, col(0x4A3A6A), s * 0.8f, -2.5f, 0, 0.3f, 0.35f, 0.3f, 6, 3);
        g.push();
        headTurn(g, p);
        float rx = 2.8f, ry = 2.65f, rz = 2.55f;
        blob(g, head("bat"), 0, 0, 0, rx, ry, rz, 18, 12);
        face(g, p, "gem", 0, 0, 0, rx, ry, rz, 1.0f, 0.05f);
        // fangs
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 0.32f, -0.95f, rz * 0.93f);
            g.rotX(180);
            cone(g, col(0xFFFFFF), 0.14f, 0.38f, 5, false);
            g.pop();
        }
        for (int s = -1; s <= 1; s += 2) Pets.earPointy(g, p, col(0x6E5C94), col(0xFFB3D0), s * 1.55f, 1.9f, -0.2f, s, 1.05f, 2.2f, 16);
        g.pop();
        fx(g, p, 3.6f);
        g.pop();
    }

    static void ghostPet(G g, A a) {
        P p = pose(a);
        g.push();
        root(g, p);
        g.translate(0, 2.4f * p.hover + 0.8f + sin(p.t * 2) * 0.5f, 0);
        int oc = g.color;
        g.color(0xE8FFFFFF);
        // wavy skirt
        for (int k = 0; k < 8; k++) {
            double an = Math.PI * 2 * k / 8 + 0.2;
            float wy = sin(p.t * 4 + k * 1.3f) * 0.3f;
            blob(g, fur("ghost"), cos(an) * 2.05f, -0.2f + wy, sin(an) * 1.85f, 1.0f, 1.0f, 1.0f, 7, 4);
        }
        // little arms
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 2.8f, 1.6f, 0.4f);
            g.rotZ(-s * (30 + (p.is("excited") || p.is("happy") ? sin(p.t * 8) * 25 : 0) - (p.is("sad") ? 30 : 0)));
            blob(g, fur("ghost"), 0, -0.6f, 0, 0.55f, 0.95f, 0.55f, 7, 4);
            g.pop();
        }
        g.push();
        g.translate(0, 2.1f, 0);
        headTurn(g, p);
        float rx = 3.0f, ry = 3.1f, rz = 2.8f;
        blob(g, fur("ghost"), 0, 0, 0, rx, ry, rz, 18, 12);
        g.color(oc);
        face(g, p, "blob", 0, 0, 0, rx, ry, rz, 1.0f, 0.1f);
        g.pop();
        g.color(oc);
        fx(g, p, 6.4f);
        g.pop();
    }

    static void jelly(G g, A a) {
        P p = pose(a);
        g.push();
        root(g, p);
        float pulse = sin(p.t * (p.is("excited") ? 6 : 3));
        g.translate(0, 3.9f * p.hover + 0.6f + sin(p.t * 1.8f) * 0.6f + pulse * 0.2f, 0);
        int oc = g.color;
        boolean og = g.glow;
        // tentacles (ribbons)
        g.color(0xD0FFFFFF).glow(true);
        for (int k = 0; k < 6; k++) {
            double an = Math.PI * 2 * k / 6 + 0.5;
            float bx = cos(an) * 1.6f, bz = sin(an) * 1.6f;
            float len = (3.4f - (k % 2) * 0.6f) * (1 + p.gr * 0.45f);
            float px = 0, py = 0;
            for (int i = 0; i < 5; i++) {
                float t0 = i / 5f, t1 = (i + 1) / 5f;
                float x0 = sin(p.t * 3 + k + t0 * 4) * 0.45f * t0, x1 = sin(p.t * 3 + k + t1 * 4) * 0.45f * t1;
                float w0 = 0.3f * (1 - t0 * 0.6f), w1 = 0.3f * (1 - t1 * 0.6f);
                float y0 = -t0 * len, y1 = -t1 * len;
                for (int pl = 0; pl < 2; pl++) {
                    float cx = pl == 0 ? 1 : 0, cz = pl == 0 ? 0 : 1;
                    g.quad("pet_jelly", new float[]{bx + x0 - w0 * cx, y0, bz - w0 * cz}, new float[]{bx + x0 + w0 * cx, y0, bz + w0 * cz},
                            new float[]{bx + x1 + w1 * cx, y1, bz + w1 * cz}, new float[]{bx + x1 - w1 * cx, y1, bz - w1 * cz},
                            new float[]{0.2f, 0.6f + t0 * 0.3f, 0.3f, 0.6f + t0 * 0.3f, 0.3f, 0.6f + t1 * 0.3f, 0.2f, 0.6f + t1 * 0.3f});
                }
                px = x1; py = y1;
            }
            blob(g, "pet_jelly", bx + px, py, bz, 0.2f, 0.2f, 0.2f, 5, 3);
        }
        // frill
        for (int k = 0; k < 10; k++) {
            double an = Math.PI * 2 * k / 10;
            blob(g, "pet_jelly", cos(an) * 2.55f, 0.35f, sin(an) * 2.55f, 0.6f, 0.45f, 0.6f, 6, 3);
        }
        // bell
        g.color(0xDCFFFFFF);
        g.push();
        g.translate(0, 0.45f, 0);
        headTurn(g, p);
        float rx = 3.05f * (1 + pulse * 0.04f), ry = 2.9f * (1 - pulse * 0.05f), rz = 3.0f * (1 + pulse * 0.04f);
        blob2(g, "pet_jelly", 0, 0, 0, rx, ry, 0.55f, rz, 16, 10);
        g.color(oc).glow(false);
        face(g, p, "blob", 0, 0.9f, 0, rx, ry * 0.72f, rz, 1.0f, -0.05f);
        g.pop();
        g.color(oc).glow(og);
        fx(g, p, 4.4f);
        g.pop();
    }

    static void fish(G g, A a) {
        P p = pose(a);
        g.push();
        root(g, p);
        float cy = 4.25f + sin(p.t * 1.6f) * 0.3f, R = 3.9f;
        // fish
        g.push();
        g.translate(0, cy + sin(p.t * 2.2f) * 0.35f, 0.3f);
        g.rotY(sin(p.t * 0.8f) * 14 + p.headYaw * 0.8f);
        g.rotX(p.headPitch * 0.6f);
        g.rotZ(p.headRoll * 0.8f);
        float rx = 2.35f, ry = 2.15f, rz = 2.5f;
        // tail fin (vertical plane, wagging)
        g.push();
        g.translate(0, 0, -rz + 0.2f);
        g.rotY(90 + p.tail * 1.4f + sin(p.t * 7) * 18);
        g.plane("pet_fin", 1.2f, 0, 2.4f, 2.6f, 0, 0, 1, 1);
        g.pop();
        // dorsal fin
        g.push();
        g.translate(0, ry - 0.25f, -0.5f);
        g.rotX(-20 + sin(p.t * 3) * 6);
        blob(g, col(0xFFB070), 0, 0.45f, 0, 0.22f, 0.75f, 1.0f, 6, 4);
        g.pop();
        // side fins
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * (rx - 0.2f), -0.5f, 0.1f);
            g.rotZ(-s * (50 + sin(p.t * 6 + s) * 18));
            blob(g, col(0xFFB070), 0, -0.6f, 0, 0.18f, 0.7f, 0.45f, 6, 4);
            g.pop();
        }
        blob(g, fur("fish"), 0, 0, 0, rx, ry, rz, 16, 10);
        face(g, p, "blue", 0, 0, 0, rx, ry, rz, 0.95f, 0.05f);
        g.pop();
        // bubble (last - translucent)
        blob(g, "pet_bubble", 0, cy, 0, R, R, R, 18, 11);
        // tiny bubbles
        for (int k = 0; k < 2; k++) {
            float ph = ((p.t * 0.4f + k * 0.5f) % 1f + 1f) % 1f;
            blob(g, "pet_bubble", 1.2f - k * 2.1f, cy + R + ph * 2.5f, 0.3f, 0.3f + k * 0.12f, 0.3f + k * 0.12f, 0.3f + k * 0.12f, 6, 4);
        }
        fx(g, p, cy + R + 0.8f);
        g.pop();
    }

    static void cloud(G g, A a) {
        P p = pose(a);
        g.push();
        root(g, p);
        float hv = 2.2f * p.hover + 1.0f + sin(p.t * 1.7f) * 0.45f;
        g.translate(0, hv, 0);
        int oc = g.color;
        boolean angry = p.is("angry");
        g.color(angry ? 0xFFB4B8C8 : p.is("sad") ? 0xFFDCE2EE : 0xFFFFFFFF);
        String t = fur("cloud");
        g.push();
        headTurn(g, p);
        for (int s = -1; s <= 1; s += 2) {
            blob2(g, t, s * 2.6f, 0.8f, -0.2f, 1.9f, 1.7f, 0.9f, 1.8f, 10, 6);
            blob2(g, t, s * 1.15f, 3.0f, -0.6f, 1.75f, 1.6f, 1.2f, 1.6f, 10, 6);
        }
        blob2(g, t, 0, 1.6f, -1.4f, 2.3f, 2.2f, 1.2f, 1.6f, 10, 6);
        float rx = 3.1f, ry = 2.5f, rz = 2.6f;
        blob2(g, t, 0, 1.6f, 0, rx, ry, 1.1f, rz, 16, 10);
        g.color(oc);
        face(g, p, "blob", 0, 1.6f, 0, rx, ry, rz, 0.95f, -0.08f);
        g.pop();
        // weather
        if (p.is("sad") || angry) {
            for (int k = 0; k < 4; k++) {
                float ph = ((p.t * 1.3f + k * 0.27f) % 1f + 1f) % 1f;
                int al = (int) (230 * (1 - ph));
                float x = -1.8f + k * 1.2f, y = 0.5f - ph * hv * 0.9f, z = 0.4f * (k % 2);
                if (angry && k == 1) PetGeo.sprite(g, "pet_star", x, y, z, 1.2f, (al << 24) | 0xFFFFFF);
                else {
                    g.color((al << 24) | 0xFFFFFF).glow(true);
                    blob(g, gl(0x7FC8FF), x, y, z, 0.16f, 0.36f, 0.16f, 6, 3);
                    g.color(oc).glow(false);
                }
            }
        } else if (p.is("happy") || p.is("excited")) {
            for (int k = 0; k < 2; k++) {
                double an = p.t * 1.5 + k * Math.PI;
                PetGeo.sprite(g, "pet_star", cos(an) * 3.8f, 2.4f + sin(an * 2) * 0.8f, sin(an) * 2.0f, 0.9f, 0xE0FFFFFF);
            }
        }
        fx(g, p, 5.6f);
        g.pop();
    }

    // ================================================================== things with faces
    static void slime(G g, A a) {
        P p = pose(a);
        float[] h = PetRig.hop(p);
        p.y += h[0] * 2.0f;
        p.stepBob = 0;
        g.push();
        root(g, p);
        float sq = sin(p.t * 3) * 0.04f - h[1] * 0.16f;
        float rx = 3.7f * (1 + sq), ry = 3.0f * (1 - sq), rz = 3.4f * (1 + sq);
        g.push();
        g.translate(0, ry * 0.92f, 0);
        blob(g, gl(0x3FAE63), 0.6f, -0.4f, -0.9f, 1.45f, 1.25f, 1.35f, 7, 4);
        headTurn(g, p);
        int oc = g.color;
        g.color(0xC8FFFFFF);
        blob2(g, gl(0x8CF0A8), 0, 0, 0, rx, ry, ry * 0.85f, rz, 18, 12);
        g.color(oc);
        face(g, p, "blob", 0, 0, 0, rx, ry, rz, 1.0f, 0.02f);
        g.pop();
        fx(g, p, ry * 1.9f + 1.2f);
        g.pop();
    }

    static void mushroom(G g, A a) {
        P p = pose(a);
        g.push();
        root(g, p);
        feet2(g, p, col(0xE8CFA8), 0.95f, 0.1f, 0.55f);
        float drop = p.lie * 0.4f;
        float rx = 2.35f, ry = 2.3f, rz = 2.15f;
        g.push();
        g.translate(0, 2.45f - drop, 0);
        g.rotZ(sin(p.t * 11) * 5 * p.sp);
        blob(g, fur("mush"), 0, 0, 0, rx, ry, rz, 17, 11);
        face(g, p, "blob", 0, 0, 0, rx, ry, rz, 1.0f, 0.02f);
        // little arms
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 2.2f, -0.2f, 0.2f);
            g.rotZ(-s * (25 + (p.is("excited") ? Math.abs(sin(p.t * 7)) * 60 : 0) - (p.is("sad") ? 15 : 0)));
            blob(g, col(0xFFF1DC), 0, -0.55f, 0, 0.35f, 0.7f, 0.35f, 6, 4);
            g.pop();
        }
        // cap
        g.push();
        g.translate(0, 2.0f, -0.1f);
        g.rotY(p.headYaw * 0.4f);
        g.rotX(p.headPitch * 0.6f - 6);
        g.rotZ(p.headRoll * 0.8f + sin(p.t * 2.1f) * 3);
        blob2(g, "pet_cap", 0, 0, 0, 3.9f, 2.5f, 0.75f, 3.7f, 16, 10);
        g.pop();
        g.pop();
        fx(g, p, 7.4f);
        g.pop();
    }

    static void brickBuddy(G g, A a) {
        P p = pose(a);
        p.y += Math.abs(sin(p.t * 6)) * 0.8f * p.sp;
        g.push();
        root(g, p);
        String yel = gl(0xFFCD03), red = "pet_brick:E3000B";
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 1.3f, 0.95f, 0.2f);
            g.rotX(s * p.walk);
            blob(g, yel, 0, -0.45f, 0.2f, 0.6f, 0.55f, 0.8f, 7, 4);
            g.pop();
        }
        g.push();
        g.rotY(p.headYaw * 0.5f);
        g.translate(0, 3.0f, 0);
        g.rotX(p.headPitch * 0.5f);
        g.rotZ(p.headRoll * 0.7f);
        g.translate(0, -3.0f, 0);
        PetGeo.rbox(g, red, -3, 0.8f, -3, 3, 5.0f, 3, 0.4f);
        for (int ix = -1; ix <= 1; ix += 2)
            for (int iz = -1; iz <= 1; iz += 2) {
                g.push();
                g.translate(ix * 1.5f, 5.0f, iz * 1.5f);
                g.cylinder(red, 1.0f, 1.0f, -0.05f, 0.75f, 12, true, 0.2f, 0.1f, 0.8f, 0.9f);
                g.pop();
            }
        Pets.faceFlat(g, p, "blob", 0, 2.9f, 3.02f, 5.4f, 4.05f);
        // arms
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 3.0f, 3.0f, 0.3f);
            float wave = p.is("excited") ? 50 + sin(p.t * 9) * 40 : p.is("happy") ? 20 + sin(p.t * 4) * 12 : p.is("sad") ? -5 : 12;
            g.rotZ(-s * wave);
            blob(g, yel, s * 0.25f, -0.7f, 0, 0.42f, 0.85f, 0.42f, 7, 4);
            g.pop();
        }
        g.pop();
        fx(g, p, 7.2f);
        g.pop();
    }

    static void robot(G g, A a) {
        P p = pose(a);
        g.push();
        root(g, p);
        String metal = "pet_metal:C9D6F0", dark = "pet_metal:6A7A96", accent = gl(0x7FB8FF);
        // feet
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 1.25f, 0.9f, 0);
            g.rotX(s * p.walk);
            blob(g, dark, 0, -0.45f, 0.25f, 0.75f, 0.5f, 1.0f, 7, 4);
            g.pop();
        }
        // body
        PetGeo.rbox(g, metal, -2.0f, 1.0f, -1.7f, 2.0f, 3.9f, 1.7f, 0.45f);
        PetGeo.sprite(g, "pet_heart", 0, 2.45f, 1.74f, 1.3f, 0xFFFFFFFF);
        // arms
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 2.2f, 3.4f, 0);
            float wave = p.is("excited") ? 120 + sin(p.t * 10) * 30 : p.is("happy") ? 20 + sin(p.t * 4) * 10 : p.is("sad") ? 2 : 10;
            g.rotX(-s * p.walk);
            g.rotZ(-s * wave);
            g.cylinder(dark, 0.3f, 0.3f, -1.4f, 0, 8, false, 0, 0, 1, 1);
            blob(g, accent, 0, -1.6f, 0, 0.5f, 0.5f, 0.5f, 7, 4);
            g.pop();
        }
        // head
        g.push();
        g.translate(0, 6.35f - p.lie * 0.5f, 0.2f);
        headTurn(g, p);
        PetGeo.rbox(g, metal, -3.1f, -2.3f, -2.5f, 3.1f, 2.3f, 2.5f, 0.6f);
        PetGeo.flat(g, "pet_screen", 0, -0.05f, 2.52f, 5.2f, 3.7f);
        Pets.faceFlat(g, p, "led", 0, -0.05f, 2.55f, 4.9f, 3.68f);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 3.1f, 0, 0);
            g.rotZ(90);
            g.cylinder(accent, 0.85f, 0.85f, -0.35f, 0.35f, 10, true, 0, 0, 1, 1);
            g.pop();
        }
        g.push();
        g.translate(0, 2.3f, 0);
        g.rotZ(sin(p.t * 3) * 10 + (p.is("sad") ? 35 : 0));
        g.cylinder(dark, 0.1f, 0.1f, 0, 1.3f, 6, false, 0, 0, 1, 1);
        boolean og = g.glow;
        g.glow(true);
        int led = p.is("love") ? 0xFF6FC8 : p.is("angry") ? 0xFF5A5A : p.is("sleepy") ? 0x6A7AFF : 0x6FF3FF;
        blob(g, gl(led), 0, 1.6f, 0, 0.48f, 0.48f, 0.48f, 7, 4);
        g.glow(og);
        g.pop();
        g.pop();
        fx(g, p, 10.2f);
        g.pop();
    }
}
