package dev.lego.cosmetic;

import static dev.lego.cosmetic.Geo.*;

import dev.lego.cosmetic.Cos.A;
import dev.lego.cosmetic.Cos.Rarity;
import dev.lego.cosmetic.Cos.Slot;

/** Face items (head space, face at z = +4, eyes at y ~ 3.5) and back items (body space, back at z = -2). */
final class Models3 {
    private Models3() { }

    static void registerFace() {
        Cos.add("face_hearts", "Herzbrille", Slot.FACE, Rarity.RARE, HdFace::heartGlasses);
        Cos.add("face_stars", "Sternenbrille", Slot.FACE, Rarity.RARE, HdFace::starGlasses);
        Cos.add("face_monocle", "Monokel", Slot.FACE, Rarity.EPIC, HdFace::monocle);
        Cos.add("face_mustache", "Schnurrbart", Slot.FACE, Rarity.COMMON, HdFace::mustache);
        Cos.add("face_clown", "Clownsnase", Slot.FACE, Rarity.COMMON, HdFace::clownNose);
        Cos.add("face_visor", "Cyber-Visier", Slot.FACE, Rarity.LEGENDARY, HdFace::visor);
        Cos.add("face_masq", "Maskenball", Slot.FACE, Rarity.EPIC, HdFace::masquerade);
        Cos.add("face_ninja", "Ninja-Maske", Slot.FACE, Rarity.RARE, HdFace::ninja);
    }

    static void registerBack() {
        Cos.add("back_guitar", "E-Gitarre", Slot.BACK, Rarity.EPIC, HdBack::guitar);
        Cos.add("back_jetpack", "Jetpack", Slot.BACK, Rarity.LEGENDARY, HdBack::jetpack);
        Cos.add("back_shield", "Ritterschild", Slot.BACK, Rarity.RARE, HdBack::shield);
        Cos.add("back_quiver", "Köcher", Slot.BACK, Rarity.RARE, HdBack::quiver);
        Cos.add("back_rocket", "Rakete", Slot.BACK, Rarity.EPIC, HdBack::rocket);
        Cos.add("back_teddy", "Teddy", Slot.BACK, Rarity.COMMON, HdBack::teddy);
        Cos.add("back_balloon", "Herzballon", Slot.BACK, Rarity.RARE, HdBack::balloon);
    }

    // ================================================================== face
    private static void temples(G g, float y, float z) {
        for (int s = 1; s >= -1; s -= 2) g.box("felt_black", s * 4.12f - 0.16f, y - 0.22f, -3.5f, s * 4.12f + 0.16f, y + 0.22f, z, 0, 0, 1, 1);
    }

    static void heartGlasses(G g, A a) {
        float p = 1 + 0.06f * (float) Math.max(0, Math.sin(a.time * 6)) ;
        g.push();
        g.translate(0, 3.6f, 4.25f);
        g.scale(p, p, 1);
        g.plane("heart_glasses", 0, 0, 9.2f, 3.45f, 0, 0, 1, 1);
        g.pop();
        g.color(0xFFE0115F);
        temples(g, 4.1f, 4.2f);
        g.color(0xFFFFFFFF);
    }

    static void starGlasses(G g, A a) {
        g.push();
        g.translate(0, 3.6f, 4.25f);
        g.plane("star_glasses", 0, 0, 9.4f, 3.5f, 0, 0, 1, 1);
        g.glow(true);
        float tw = 0.5f + 0.5f * sin(a.time * 3);
        g.color(argb((int) (255 * tw), 0xFFFFFF));
        sprite(g, "spark", 3.6f, 1.2f, 0.2f, 1.2f + tw);
        g.color(argb((int) (255 * (1 - tw)), 0xFFFFFF));
        sprite(g, "spark", -2.2f, -1.0f, 0.2f, 1.0f + (1 - tw));
        g.glow(false).color(0xFFE8A010);
        g.pop();
        temples(g, 3.8f, 4.2f);
        g.color(0xFFFFFFFF);
    }

    static void monocle(G g, A a) {
        g.push();
        g.translate(-1.9f, 3.7f, 4.25f);
        g.color(0xFFFFE08A);
        g.push();
        g.rotX(90);
        g.torus("gold", 1.45f, 0.2f, 20, 6, 0, 0, 1, 1);
        g.pop();
        g.color(0xFFFFFFFF);
        g.plane("monocle_glass", 0, 0, 2.9f, 2.9f, 0, 0, 1, 1);
        // chain hanging down
        float sw = sin(a.time * 1.8) * 0.3f;
        int n = 14;
        g.color(0xFFFFE08A);
        for (int i = 0; i < n; i++) {
            float t = i / (float) (n - 1);
            float x = -1.2f - t * 2.2f + sw * t, y = -1.4f - (float) Math.sin(t * Math.PI) * 2.8f - t * 0.5f;
            g.push();
            g.translate(x, y, 0.1f - t * 0.4f);
            g.sphere("gold", 0.17f, 5, 3, 0, 0, 1, 1);
            g.pop();
        }
        g.color(0xFFFFFFFF);
        g.pop();
    }

    static void mustache(G g, A a) {
        g.push();
        g.translate(0, 1.55f, 4.22f);
        g.rotZ(sin(a.time * 2.2) * 1.5f);
        float tw = Math.max(0, sin(a.time * 1.3)) * 0.1f;
        g.scale(1 + tw, 1, 1);
        // slightly curved: two halves angled back
        g.push();
        g.rotY(6);
        g.quad("mustache", new float[]{-3.8f, 1.15f, 0}, new float[]{0, 1.15f, 0}, new float[]{0, -1.15f, 0}, new float[]{-3.8f, -1.15f, 0}, new float[]{0, 0, 0.5f, 0, 0.5f, 1, 0, 1});
        g.pop();
        g.push();
        g.rotY(-6);
        g.quad("mustache", new float[]{0, 1.15f, 0}, new float[]{3.8f, 1.15f, 0}, new float[]{3.8f, -1.15f, 0}, new float[]{0, -1.15f, 0}, new float[]{0.5f, 0, 1, 0, 1, 1, 0.5f, 1});
        g.pop();
        g.pop();
    }

    static void clownNose(G g, A a) {
        float sq = 1 + 0.05f * sin(a.time * 3);
        g.push();
        g.translate(0, 2.6f, 4.9f);
        g.scale(sq, 1 / sq, sq);
        g.color(0xFFFF2A2A);
        g.sphere("gloss", 1.25f, 14, 9, 0, 0, 1, 1);
        g.color(0xFFFFFFFF);
        g.pop();
    }

    static void visor(G g, A a) {
        g.push();
        g.translate(0, 3.7f, 0);
        int seg = 10;
        float R = 5.0f, h = 2.8f;
        for (int i = 0; i < seg; i++) {
            double a0 = Math.PI * (0.12 + 0.76 * i / seg), a1 = Math.PI * (0.12 + 0.76 * (i + 1) / seg);
            float[] p0 = sq(a0, R, 5), p1 = sq(a1, R, 5);
            float u0 = 1 - i / (float) seg, u1 = 1 - (i + 1f) / seg;
            g.quad("visor", new float[]{p0[0], h / 2, p0[1]}, new float[]{p1[0], h / 2, p1[1]}, new float[]{p1[0], -h / 2, p1[1]}, new float[]{p0[0], -h / 2, p0[1]},
                    new float[]{u0, 0, u1, 0, u1, 1, u0, 1});
        }
        // scanner light sweeping left <-> right
        g.glow(true);
        float sweep = 0.5f + 0.5f * sin(a.time * 2.4);
        double an = Math.PI * (0.2 + 0.6 * sweep);
        float[] p = sq(an, R + 0.08f, 5);
        g.color(0xFFFF3A5A);
        g.push();
        g.translate(p[0], 0, p[1]);
        g.rotY((float) -Math.toDegrees(an) + 90);
        g.plane("glow_line", 0, 0, 1.6f, 1.3f, 0, 0, 1, 1);
        g.color(0xB3FF3A5A);
        g.plane("orb", 0, 0, 4f, 3f, 0, 0, 1, 1);
        g.pop();
        g.color(0xFF2AE6FF);
        for (int s = 1; s >= -1; s -= 2) {
            g.box("white", s * 4.5f - 0.15f, -0.2f, -1.4f, s * 4.5f + 0.15f, 0.2f, 1.2f, 0, 0, 1, 1);
        }
        g.glow(false);
        g.color(0xFF2A2E36);
        g.box("steel", -4.4f, -0.7f, -4.4f, -4.05f, 0.7f, -1.4f, 0, 0, 1, 1);
        g.box("steel", 4.05f, -0.7f, -4.4f, 4.4f, 0.7f, -1.4f, 0, 0, 1, 1);
        g.box("steel", -4.4f, -0.7f, -4.4f, 4.4f, 0.7f, -4.05f, 0, 0, 1, 1);
        g.color(0xFFFFFFFF);
        g.pop();
    }

    static void masquerade(G g, A a) {
        g.push();
        g.translate(0, 3.9f, 4.25f);
        g.plane("mask_masq", 0, 0, 9.6f, 4.8f, 0, 0, 1, 1);
        // feather plume on the side
        g.translate(3.6f, 1.8f, 0.1f);
        g.rotZ(-28 + sin(a.time * 2) * 5);
        g.plane("feather_plume", 0, 2.6f, 1.6f, 6f, 0, 1, 1, 0);
        g.rotZ(22);
        g.plane("feather_plume", 0, 2.2f, 1.4f, 5f, 0, 1, 1, 0);
        g.pop();
    }

    static void ninja(G g, A a) {
        g.push();
        g.color(0xFF22242C);
        sqTube(g, "cloth", 4.35f, 4.35f, -0.1f, 3.0f, 5, 20, 0, 0, 1, 1);
        g.color(0xFFC81E2A);
        sqTube(g, "cloth", 4.45f, 4.45f, 5.4f, 6.6f, 5, 20, 0, 0, 1, 1);
        // knot tails flutter behind
        float fl = sin(a.time * 4) * 10 + a.move * 25;
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 0.6f, 6.0f, -4.4f);
            g.rotY(s * 15);
            g.rotX(-20 - fl * 0.5f + s * 5);
            g.quad("cloth", new float[]{-0.5f, 0, 0}, new float[]{0.5f, 0, 0}, new float[]{0.4f, -5f, -0.2f}, new float[]{-0.6f, -5f, -0.2f}, new float[]{0, 0, 1, 0, 1, 1, 0, 1});
            g.pop();
        }
        g.color(0xFFFFFFFF);
        g.pop();
    }

    // ================================================================== back (body space: back surface at z = -2)
    static void guitar(G g, A a) {
        g.push();
        g.translate(0, -7, -2.9f);
        g.rotZ(-32 + sin(a.time * 1.5) * 1.2f);
        g.rotY(180);
        // body: flattened ellipsoids (lower + upper bout)
        g.color(0xFFFFFFFF);
        ellipsoid(g, "guitar_body", 0, -2.4f, 0, 3.6f, 3.4f, 0.8f, 16, 8);
        ellipsoid(g, "guitar_body", 0, 1.6f, 0, 2.7f, 2.5f, 0.78f, 16, 8);
        // neck + headstock
        g.box("wood", -0.55f, 3.5f, -0.25f, 0.55f, 12.5f, 0.35f, 0, 0, 1, 1);
        g.color(0xFF1A1A1E);
        g.box("plastic", -0.95f, 12.5f, -0.25f, 0.95f, 15f, 0.3f, 0, 0, 1, 1);
        g.color(0xFFFFFFFF);
        for (int i = 0; i < 3; i++)
            for (int s = -1; s <= 1; s += 2) g.box("steel", s * 0.95f - 0.25f, 12.9f + i * 0.7f, -0.05f, s * 0.95f + 0.25f, 13.2f + i * 0.7f, 0.15f, 0, 0, 1, 1);
        g.quad("strings", new float[]{-0.45f, 12.5f, 0.42f}, new float[]{0.45f, 12.5f, 0.42f}, new float[]{0.45f, -3.5f, 0.85f}, new float[]{-0.45f, -3.5f, 0.85f}, new float[]{0, 0, 1, 0, 1, 1, 0, 1});
        g.color(0xFF1A1A1E);
        g.box("plastic", -1.3f, -3.9f, 0.6f, 1.3f, -3.3f, 0.95f, 0, 0, 1, 1);
        g.pop();
        // strap across the chest
        g.color(0xFF2A1A12);
        g.push();
        g.rotZ(-40);
        g.box("leather_dark", -0.6f, -9, 2.05f, 0.6f, 3, 2.35f, 0, 0, 1, 1);
        g.box("leather_dark", -0.6f, -9, -2.35f, 0.6f, 3, -2.05f, 0, 0, 1, 1);
        g.pop();
        g.color(0xFFFFFFFF);
    }

    static void jetpack(G g, A a) {
        g.push();
        g.translate(0, -1.5f, -2.1f);
        g.color(0xFF3A3E48);
        g.box("steel", -3.4f, -8.5f, -1.6f, 3.4f, -0.8f, 0, 0, 0, 1, 1);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 2.1f, -1.5f, -2.9f);
            g.color(0xFFE8ECF2);
            g.cylinder("steel", 1.85f, 1.85f, -7.5f, 0, 14, false, 0, 0, 1, 1);
            g.push();
            g.scale(1, 0.6f, 1);
            g.sphere("steel", 1.85f, 14, 6, 0, 0.5f, 1, 1);
            g.pop();
            g.color(0xFFE3000B);
            g.cylinder("plastic", 1.9f, 1.9f, -2.2f, -1.3f, 14, false, 0, 0, 1, 1);
            g.color(0xFF2A2E36);
            g.cylinder("iron", 1.3f, 0.8f, -9.2f, -7.5f, 12, false, 0, 0, 1, 1);
            // flame
            float fl = 1 + 0.25f * sin(a.time * 22 + s) + a.move * 0.6f;
            g.glow(true);
            g.color(0xFFFFFFFF);
            g.cross("flame", 0, -9.2f - 2.6f * fl, 0, 1.9f, 5.2f * fl, 0, 1, 1, 0);
            g.color(0x99FFFFFF);
            g.cross("orb", 0, -9.4f, 0, 3.2f, 3.2f, 0, 0, 1, 1);
            for (int i = 0; i < 3; i++) {
                float ph = (a.time * 2.2f + i / 3f + (s > 0 ? 0.5f : 0)) % 1f;
                g.color(argb((int) (140 * (1 - ph)), 0xFFFFFF));
                sprite(g, "orb_purple", sin(i * 2.1 + a.time) * ph * 1.5f, -11 - ph * 7 * fl, cos(i * 2.1) * ph, 1.5f + ph * 3);
            }
            g.glow(false);
            g.pop();
        }
        g.color(0xFFFFFFFF);
        // straps
        for (int s = -1; s <= 1; s += 2) {
            g.color(0xFF2A2E36);
            g.box("leather_dark", s * 2.2f - 0.5f, -7, 2.0f, s * 2.2f + 0.5f, 0.35f, 2.3f, 0, 0, 1, 1);
            g.box("leather_dark", s * 2.2f - 0.5f, 0, -2.1f, s * 2.2f + 0.5f, 0.35f, 2.3f, 0, 0, 1, 1);
        }
        g.color(0xFFFFFFFF);
        g.pop();
    }

    static void shield(G g, A a) {
        g.push();
        g.translate(0, -6.2f, -2.5f);
        g.rotZ(8);
        g.push();
        g.rotX(90);
        g.color(0xFF9AA2B0);
        g.cylinder("steel", 6.2f, 6.2f, -0.2f, 0.6f, 28, false, 0, 0, 1, 1);
        g.color(0xFFFFFFFF);
        for (int i = 0; i < 28; i++) {
            double a0 = Math.PI * 2 * i / 28, a1 = Math.PI * 2 * (i + 1) / 28;
            for (float yy : new float[]{-0.2f, 0.6f})
                tri(g, "shield_face", new float[]{0, yy, 0}, new float[]{cos(a0) * 6.2f, yy, sin(a0) * 6.2f}, new float[]{cos(a1) * 6.2f, yy, sin(a1) * 6.2f},
                        new float[]{0.5f, 0.5f}, new float[]{0.5f + 0.5f * cos(a0), 0.5f + 0.5f * sin(a0)}, new float[]{0.5f + 0.5f * cos(a1), 0.5f + 0.5f * sin(a1)});
        }
        g.color(0xFFD8DCE4);
        g.torus("steel", 6.2f, 0.5f, 28, 6, 0, 0, 1, 1);
        g.color(0xFFFFE08A);
        g.translate(0, -0.8f, 0);
        g.sphere("gold", 1.3f, 10, 6, 0, 0, 1, 1);
        g.pop();
        g.color(0xFFFFFFFF);
        g.pop();
    }

    static void quiver(G g, A a) {
        g.push();
        g.translate(1.5f, -5.5f, -3.4f);
        g.rotZ(-24);
        g.color(0xFFFFFFFF);
        g.cylinder("leather", 1.8f, 1.6f, -6, 5, 14, true, 0, 0, 1, 1);
        g.color(0xFFFFE08A);
        g.cylinder("gold", 1.9f, 1.9f, 4, 4.8f, 14, false, 0, 0, 1, 1);
        g.cylinder("gold", 1.7f, 1.7f, -5.2f, -4.5f, 14, false, 0, 0, 1, 1);
        g.color(0xFFFFFFFF);
        for (int i = 0; i < 5; i++) {
            float ox = -0.9f + (i % 3) * 0.9f, oz = -0.5f + (i / 3) * 1.0f;
            float h = 7.4f + (i % 2) * 0.8f;
            g.push();
            g.translate(ox, 0, oz);
            g.rotZ((i - 2) * 3);
            g.box("wood", -0.12f, 2, -0.12f, 0.12f, h, 0.12f, 0, 0, 1, 1);
            g.plane("arrow_fletch", 0, h - 0.6f, 1.2f, 2.4f, 0, 0, 1, 1);
            g.rotY(90);
            g.plane("arrow_fletch", 0, h - 0.6f, 1.2f, 2.4f, 0, 0, 1, 1);
            g.pop();
        }
        g.pop();
        g.color(0xFF3A2618);
        g.push();
        g.rotZ(38);
        g.box("leather_dark", -0.5f, -9, 2.05f, 0.5f, 3, 2.3f, 0, 0, 1, 1);
        g.pop();
        g.color(0xFFFFFFFF);
    }

    static void rocket(G g, A a) {
        g.push();
        float shake = a.move * 0.15f * sin(a.time * 40);
        g.translate(shake, -4 + 0.2f * sin(a.time * 2), -4.6f);
        g.cylinder("rocket_body", 2.2f, 2.2f, -6, 3, 16, false, 0, 1, 1, 0);
        g.color(0xFFE3000B);
        g.cylinder("plastic", 2.2f, 0.05f, 3, 7, 16, false, 0, 0, 1, 1);
        g.color(0xFFFFFFFF);
        g.push();
        g.translate(0, 0.8f, -2.18f);
        g.rotY(180);
        g.plane("window", 0, 0, 2.2f, 2.2f, 0, 0, 1, 1);
        g.pop();
        g.color(0xFFE3000B);
        for (int i = 0; i < 4; i++) {
            g.push();
            g.rotY(45 + i * 90);
            g.quad("plastic", new float[]{0, -2.5f, 2.1f}, new float[]{0, -6.8f, 2.1f}, new float[]{0, -7.5f, 4.2f}, new float[]{0, -4.8f, 4.0f}, new float[]{0, 0, 1, 0, 1, 1, 0, 1});
            g.pop();
        }
        g.color(0xFF3A3E48);
        g.cylinder("iron", 1.3f, 1.6f, -7.2f, -6, 12, false, 0, 0, 1, 1);
        g.glow(true);
        float fl = 0.8f + 0.2f * sin(a.time * 25) + a.move * 0.8f;
        g.color(0xFFFFFFFF);
        g.cross("flame", 0, -7.2f - 2.4f * fl, 0, 2.2f, 4.8f * fl, 0, 1, 1, 0);
        g.color(0x88FFFFFF);
        g.cross("orb", 0, -7.4f, 0, 3.6f, 3.6f, 0, 0, 1, 1);
        g.glow(false).color(0xFFFFFFFF);
        g.pop();
    }

    static void teddy(G g, A a) {
        g.push();
        g.translate(0.8f, -4.2f, -4.2f);
        g.rotZ(sin(a.time * 1.6) * 4 + a.move * sin(a.time * 8) * 6);
        g.rotY(180);
        // body
        ellipsoid(g, "fur_brown", 0, -2.2f, 0, 2.4f, 2.8f, 1.9f, 12, 8);
        g.color(0xFFE8C898);
        ellipsoid(g, "fur_brown", 0, -2.4f, 1.1f, 1.5f, 1.8f, 0.9f, 10, 6);
        g.color(0xFFFFFFFF);
        // head
        ellipsoid(g, "fur_brown", 0, 1.8f, 0, 2.3f, 2.1f, 2.0f, 14, 9);
        g.push();
        g.translate(0, 1.6f, 2.02f);
        g.plane("teddy_face", 0, 0, 3.4f, 3.4f, 0, 0, 1, 1);
        g.pop();
        for (int s = -1; s <= 1; s += 2) {
            ellipsoid(g, "fur_brown", s * 1.7f, 3.6f, 0, 0.9f, 0.9f, 0.55f, 10, 6);
            ellipsoid(g, "fur_brown", s * 2.4f, -1.4f, 0.4f, 0.8f, 1.5f, 0.8f, 8, 6);
            ellipsoid(g, "fur_brown", s * 1.3f, -4.8f, 0.6f, 0.9f, 0.8f, 1.1f, 8, 6);
        }
        g.color(0xFFE0405A);
        g.push();
        g.translate(0, -0.2f, 1.6f);
        g.box("satin", -1.0f, -0.35f, 0, 1.0f, 0.35f, 0.5f, 0, 0, 1, 1);
        g.pop();
        g.color(0xFFFFFFFF);
        g.pop();
    }

    static void balloon(G g, A a) {
        float t = a.time;
        float bx = 6 + sin(t * 0.9) * 1.0f, by = 1.0f + sin(t * 1.3) * 0.8f, bz = -6 + cos(t * 0.7) * 1.0f;
        float[] hand = {2.5f, -9f, -2.3f};
        int n = 10;
        float[][] pts = new float[n][];
        float[] rad = new float[n];
        for (int i = 0; i < n; i++) {
            float k = i / (float) (n - 1);
            pts[i] = new float[]{lerp(hand[0], bx, k) + sin(k * Math.PI) * 0.8f * sin(t * 2), lerp(hand[1], by - 3.4f, k), lerp(hand[2], bz, k)};
            rad[i] = 0.06f;
        }
        g.color(0xFFF4F4F4);
        chain(g, "white", pts, rad, 3);
        g.push();
        g.translate(bx, by, bz);
        g.rotZ(sin(t * 1.1) * 8);
        g.rotY(t * 25);
        g.color(0xFFFF2A5A);
        // heart balloon: two spheres + cone
        ellipsoid(g, "gloss", -1.3f, 0.6f, 0, 1.9f, 1.9f, 1.6f, 16, 10);
        ellipsoid(g, "gloss", 1.3f, 0.6f, 0, 1.9f, 1.9f, 1.6f, 16, 10);
        g.push();
        g.rotZ(180);
        g.translate(0, -0.2f, 0);
        g.scale(1, 1, 0.8f);
        g.cylinder("gloss", 2.85f, 0.2f, 0, 3.4f, 14, false, 0, 0, 1, 1);
        g.pop();
        g.color(0xFFE0103A);
        g.cylinder("plastic", 0.3f, 0.45f, -3.6f, -3.1f, 6, true, 0, 0, 1, 1);
        g.color(0xFFFFFFFF);
        g.pop();
    }
}
